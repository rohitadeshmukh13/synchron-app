package io.synchron.task1.adaptor;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.synchron.task1.model.UpdateContextObject;

@Service
public class AdaptorService {

	private AdaptorDAO dao;
	private static Logger logger = LoggerFactory.getLogger(AdaptorDAO.class);
	
	public void init() {
		dao = new AdaptorDAO();
		dao.init();
		logger.debug("Initialization successful!");
	}

	public String createEntity(UpdateContextObject contextObject) {
		logger.debug("createEntity called!");
		return dao.createEntity(contextObject);
	}

	public List<String> getSensorMachines() {
		logger.debug("getSensorMachines called!");
		return dao.getSensorMachines();
	}

	public List<Map<String, String>> getSensorData(Map<String, String> params) {
		String sensor = params.get("sensor");
		String timestamp1 = params.get("timestamp1");
		String timestamp2 = params.get("timestamp2");
		
		timestamp1 = "\"" + timestamp1 + "\"^^xsd:dateTime";
		timestamp2 = "\"" + timestamp2 + "\"^^xsd:dateTime";
		
		return dao.getSensorData(sensor, timestamp1, timestamp2);
	}
	
	/*public String createEntityFromTriples(Map<String, String> input) {
		logger.debug("createEntityFromTriples called!");
		return dao.createEntityFromTriples(input);
	}*/
}
