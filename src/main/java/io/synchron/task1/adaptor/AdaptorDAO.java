package io.synchron.task1.adaptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.synchron.task1.commons.Literals;
import io.synchron.task1.model.Attribute;
import io.synchron.task1.model.ContextElement;
import io.synchron.task1.model.Entity;
import io.synchron.task1.model.Metadata;
import io.synchron.task1.model.UpdateContextObject;


public class AdaptorDAO {

	private Model rdfModel;
	private static Logger logger = LoggerFactory.getLogger(AdaptorDAO.class);

	public AdaptorDAO() {}

	public void init() {

		rdfModel = ModelFactory.createDefaultModel();

		// load vocabulary followed by the data into the created rdfModel
		File vocabFolder = new File(Literals.VOCABULARY_DIRECTORY);
		File dataFolder = new File(Literals.DATA_DIRECTORY);


		loadAllFilesFromDirectory(vocabFolder);
		logger.debug("RDF vocabulary loaded!");
		loadAllFilesFromDirectory(dataFolder);
		logger.debug("RDF data loaded!");

		// write for testing
		if(logger.isDebugEnabled()) {
			try {
				rdfModel.write(new PrintWriter(new File(Literals.RDF_OUT_DIRECTORY + Literals.RDF_OUT_FILENAME)), "TTL");
				logger.debug("rdf_store.ttl created successfully!");
			} catch (FileNotFoundException e) {
				logger.error(Literals.ERROR_WRITING_RDFMODEL);
			}
		}

		// sync available data with IoT Broker
		syncAtStartup();

		logger.debug("Initialization successful!");
	}

	private void loadAllFilesFromDirectory(final File directory) {
		for (final File element : directory.listFiles()) {
			if (element.isDirectory()) {
				loadAllFilesFromDirectory(element);
			} else {
				logger.debug("Loading triples from file {}", element.getName());
				rdfModel.read(element.getAbsolutePath());
			}
		} 
	}

	// When the SynchronApp starts up, sync all the semantic data available in the rdfModel with the IoT Broker
	private void syncAtStartup() {
		// There are 2 ways to sync:
		// 1: If IoT Broker cares about dependencies: 
		// Fetch a resource, check its properties; 
		// If all literals, make a REST call to IoT Broker to sync
		// Else, call the the function to fetch the resource recursively - this will go on until literal is encountered.
		// 2: If IoT Broker doesn't care about dependencies i.e. data can be sent to the broker in any order <USED HERE>
		// Fetch all the resources in any order, construct JSON request object and make REST calls to the IoT Broker for each JSON request object.

		// Fetch all the resources first of all types
		String queryString = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
						"SELECT ?s ?o " +
						"WHERE {" +
						"      	 ?s rdf:type ?o . " +
						"      }";

		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, rdfModel);
		//try (QueryExecution qexec = QueryExecutionFactory.create(query, rdfModel)) {
		ResultSet results = qexec.execSelect() ;
		while (results.hasNext())
		{
			QuerySolution soln = results.nextSolution();
			Resource subjectResource = soln.getResource("s");
			Resource objectResource = soln.getResource("o");

			UpdateContextObject contextObject = getContextObjFromResources(subjectResource, objectResource);

			// contextObject is ready. Sync it with the IoT Broker now.
			logger.debug("Trying to sync [{}] with the IoT Broker...", subjectResource.getURI());
			// REST call
			updateContextAtIoTBroker(contextObject);
			logger.debug("The resource [{}] synchronized with the IoT Broker successfully!!", subjectResource.getURI());
		}
		// all resources sync'ed.
		logger.debug("All resources synchronized with the IoT Broker successfully!!");
		//	}
	}	// syncAtStartup() end

	private UpdateContextObject getContextObjFromResources(Resource subjectResource, Resource objectResource) {

		UpdateContextObject contextObject = new UpdateContextObject();
		ContextElement contextElement = new ContextElement();
		contextObject.setContextElements(Arrays.asList(contextElement));
		List<Attribute> attributes = new ArrayList<>();
		Entity entity = new Entity(subjectResource.getURI(), objectResource.getURI());
		contextElement.setEntityId(entity);

		// for each resource, fetch its properties
		StmtIterator itr = subjectResource.listProperties();
		while(itr.hasNext()) {
			Statement st = itr.next();
			String sub = st.getSubject().toString();
			String pred = st.getPredicate().toString();
			String obj = st.getObject().toString();
			if(st.getObject().isLiteral()) {
				obj = st.getObject().asLiteral().getValue().toString();
			}
			logger.debug("Subject : {}, Predicate : {}, Object : {} ", sub, pred, obj);
			if(!"http://www.w3.org/1999/02/22-rdf-syntax-ns#type".equals(pred)) {
				Attribute attr = new Attribute(pred, pred, obj);
				// If the value for this property is a literal, include metadata too
				if(st.getObject().isLiteral()) {
					String datatype = st.getObject().asLiteral().getDatatypeURI();
					Metadata metadata = new Metadata(datatype, datatype, datatype);
					attr.setMetadata(Arrays.asList(metadata));
				}
				attributes.add(attr);
			}
		}
		// all properties added to attributes list
		contextElement.setAttributes(attributes);

		return contextObject;
	}

	private void updateContextAtIoTBroker(UpdateContextObject contextObject) {
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost("http://localhost:8060/ngsi10/updateContext");

		httppost.addHeader("Content-Type", "application/json; charset=utf8");
		// Request parameters and other properties.

		ObjectMapper mapper = new ObjectMapper();
		String contextObjectJSON;
		try {
			contextObjectJSON = mapper.writeValueAsString(contextObject);
			logger.debug("HTTP POST body JSON : \n {}", contextObjectJSON);

			StringEntity contextObjectEntity = new StringEntity(contextObjectJSON);
			httppost.setEntity(contextObjectEntity);

			//Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			logger.debug("HTTP POST response status code HTTP {}, message : {}", response.getStatusLine().getStatusCode(), response.getStatusLine());
			if(200 == response.getStatusLine().getStatusCode()) {
				logger.debug("HTTP POST to IoT Broker successful!");
			}
			else {
				logger.debug("HTTP POST to IoT Broker UNsuccessful!");
			}
		} catch (JsonProcessingException e) {
			logger.error("Error in updateContextAtIoTBroker!", e);
		} catch (UnsupportedEncodingException e) {
			logger.error("Error in updateContextAtIoTBroker!", e);
		} catch (ClientProtocolException e) {
			logger.error("Error in updateContextAtIoTBroker!", e);
		} catch (IOException e) {
			logger.error("Error in updateContextAtIoTBroker!", e);
		}
	}

	/*public String createEntityFromTriples(Map<String, String> input) {
		String id = input.get("id");
		String triples = input.get("triples");
		try {
			//rdfModel.read(triples);
			rdfModel.read(new ByteArrayInputStream(triples.getBytes()), null);
		} catch (Exception e) {
			logger.debug("Error occured in createEntityFromTriples: {}", e);
			return "Error: Invalid Input.";
		}
		// Entity added to rdf store
		// Now, retrieve it, build updateContextObject and sync it with IoT Broker
		String queryString = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
						"SELECT ?o " +
						"WHERE {" +
						"      	 <" + id + "> rdf:type ?o . " +
						"      }";

		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, rdfModel);
		//try (QueryExecution qexec = QueryExecutionFactory.create(query, rdfModel)) {
		ResultSet results = qexec.execSelect() ;
		while (results.hasNext())
		{
			QuerySolution soln = results.nextSolution();
			Resource subjectResource = rdfModel.createResource(id);
			Resource objectResource = soln.getResource("o") ;

			UpdateContextObject contextObject = getContextObjFromResources(subjectResource, objectResource);

			// contextObject is ready. Sync it with the IoT Broker now.
			logger.debug("Trying to sync [{}] with the IoT Broker...", subjectResource.getURI());
			// REST call
			updateContextAtIoTBroker(contextObject);
			logger.debug("The resource [{}] synchronized with the IoT Broker successfully!!", subjectResource.getURI());
		}

		return "Success";
	}*/

	public String createEntity(UpdateContextObject contextObject) {
		List<ContextElement> contextElements = contextObject.getContextElements();
		if(contextElements == null || contextElements.isEmpty()) {
			return "Error: Invalid Input";
		}

		for(ContextElement contextElement : contextElements) {
			if(contextElement == null) {
				return "Error: Invalid Input";
			}
			Entity entity = contextElement.getEntityId();
			Resource entityResource = rdfModel.createResource(entity.getId());
			List<Attribute> attributes = contextElement.getAttributes();
			if(attributes == null) {
				return "Error: Invalid Input";
			}
			for(Attribute attribute : attributes) {
				Property attrTypeProp = ResourceFactory.createProperty(attribute.getType());
				List<Metadata> metadataList = attribute.getMetadata();
				if(metadataList == null) {
					return "Error: Invalid Input";
				}
				if(metadataList.isEmpty()) {
					// object is a resource
					Resource attrValResource = rdfModel.createResource(attribute.getContextValue());
					rdfModel.add(entityResource, attrTypeProp, attrValResource);
				}
				else {
					// object is a literal
					// There can be just 1 metadata element
					Metadata metadata = metadataList.get(0);

					RDFDatatype datatype = NodeFactory.getType(metadata.getName());
					rdfModel.add(entityResource, attrTypeProp, ResourceFactory.createTypedLiteral(attribute.getContextValue(), datatype));
					if(logger.isDebugEnabled()) {
						try {
							rdfModel.write(new PrintWriter(new File(Literals.RDF_OUT_DIRECTORY + Literals.RDF_OUT_FILENAME)), "TTL");
							logger.debug("rdfModel written!");
						} catch (FileNotFoundException e) {
							logger.error("Error writing rdf model");
							return "Error";
						}
					}
				}
			}
		}
		logger.debug("Success! contextObject written to the local triple store.");

		// Sync contextObject with the IoT Broker now.
		logger.debug("Trying to sync contextObject with the IoT Broker...");
		// REST call
		updateContextAtIoTBroker(contextObject);
		logger.debug("The contextObject synchronized with the IoT Broker successfully!!");

		return "Success";
	}

	public List<String> getSensorMachines() {
		List<String> machines = new ArrayList<>();

		String queryString = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
						"PREFIX WeidmullerMetadata: <http://www.agtinternational.com/ontologies/WeidmullerMetadata#> " +
						"SELECT ?machine " +
						"WHERE {" +
						"      	 ?machine rdf:type WeidmullerMetadata:MoldingMachine . " +
						"      }";

		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, rdfModel);
		ResultSet results = qexec.execSelect() ;
		while (results.hasNext())
		{
			QuerySolution soln = results.nextSolution();
			Resource machineResource = soln.getResource("machine");
			machines.add(machineResource.toString());
		}

		return machines;
	}

	public List<Map<String, String>> getSensorData(String sensor, String timestamp1, String timestamp2) {
		List<Map<String, String>> sensorDataList = new ArrayList<>();

		String queryString = 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
						"PREFIX WeidmullerMetadata: <http://www.agtinternational.com/ontologies/WeidmullerMetadata#> " +
						"PREFIX I4.0: <http://www.agtinternational.com/ontologies/I4.0#> " +
						"PREFIX ssnOld: <http://purl.oclc.org/NET/ssnx/ssn#> " +
						"PREFIX IoTCore: <http://www.agtinternational.com/ontologies/IoTCore#> " +
						"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
						"SELECT ?observedProperty ?observationResult " +
						"WHERE {" +
						"		 ?grp a I4.0:MoldingMachineObservationGroup ." +
						"      	 ?grp I4.0:machine <" + sensor + "> . " +
						"      	 ?grp ssnOld:observationResultTime ?timestampObj . " +
						"      	 ?timestampObj IoTCore:valueLiteral ?timestamp . " +
						"      	 FILTER (" + timestamp1 + " < ?timestamp ) . " +
						"      	 FILTER (?timestamp < " + timestamp2 + ") . " +
						"      	 ?grp I4.0:contains ?observation . " +
						"      	 ?observation ssnOld:observedProperty ?observedProperty . " +
						"      	 ?observation ssnOld:observationResult ?output . " +
						"      	 ?output ssnOld:hasValue ?val . " +
						"      	 ?val IoTCore:valueLiteral ?observationResult . " +
						"      }";

		Query query = QueryFactory.create(queryString) ;
		logger.debug("getSensorData Query: {}", query.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, rdfModel);
		ResultSet results = qexec.execSelect() ;
		while (results.hasNext())
		{
			QuerySolution soln = results.nextSolution();
			Resource observedProperty = soln.getResource("observedProperty");
			String observationResult = soln.getLiteral("observationResult").toString();
			Map<String, String> sensorData = new HashMap<>();
			sensorData.put("observedProperty", observedProperty.toString());
			sensorData.put("observationResult", observationResult);
			sensorDataList.add(sensorData);
		}

		return sensorDataList;
	}

}
