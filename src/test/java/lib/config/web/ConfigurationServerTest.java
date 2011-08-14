package lib.config.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import lib.config.base.configuration.Configuration;
import lib.config.web.container.ContainerListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationServerTest {

	private static final Logger logger = LoggerFactory.getLogger(
			ConfigurationServerTest.class);
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStart() throws IOException, InterruptedException {
		
		// create test configuration
		DisplayableConfiguration testConfig = new DisplayableConfiguration() {
			private Map<String, String> properties = new HashMap<String, String>();
			
			@Override
			public String getProperty(String key) {
				return properties.get(key);
			}
			
			@Override
			public void setProperty(String key, String value) {
				properties.put(key, value);
			}
			
			@Override
			public String getId() {
				return "test_settings";
			}
			
			@Override
			public LinkedHashSet<String> getKeys() {
				LinkedHashSet<String> keys = new LinkedHashSet<String>();
				keys.add("timeout");
				keys.add("destination");
				keys.add("source_dir");
				return keys;
			}

			@Override
			public String getDisplayName() {
				return "Test Configuration";
			}
		};
		
		// the map enforces a unique id between the displayable configurations. 
		Map<String, DisplayableConfiguration> configs = new HashMap<String, 
				DisplayableConfiguration>();
		
		configs.put("test_config", testConfig);
		
		ConfigurationServer server = new ConfigurationServer(8080, configs);
		logger.debug("Pre server start");
		server.start();
		
		server.addListener(new ContainerListener() {
			
			@Override
			public void onModifed(Configuration config) {
				System.out.println("Configuration modified: " + config);
			}
		});
		
		// TODO sync this properly
		Thread.sleep(50000);
		
		logger.debug("Post server start");
	}

}
