package io.synchron.task1.adaptor;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.synchron.task1.model.UpdateContextObject;

@RestController
public class AdaptorController {
	
	@Autowired
	private AdaptorService adaptorService;
	
	@PostConstruct
	public void init() {
		adaptorService.init();
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/getSensors")
	public List<String> getSensorMachines() {
		return adaptorService.getSensorMachines();
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/getSensorData")
	public List<Map<String, String>> getSensorData(@RequestBody Map<String, String> params) {
		return adaptorService.getSensorData(params);
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/createEntity")
	public String createEntity(@RequestBody UpdateContextObject contextObject) {
		return adaptorService.createEntity(contextObject);
	}
	
	// Just for testing and debugging (to be used for convenience)
	/*@RequestMapping(method=RequestMethod.POST, value="/createEntityFromTriples")
	public String createEntityFromTriples(@RequestBody Map<String, String> input) {
		return adaptorService.createEntityFromTriples(input);
	}*/

}
