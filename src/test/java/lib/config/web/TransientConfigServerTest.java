package lib.config.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import lib.config.base.configuration.Configuration;
import lib.config.web.container.Command;
import lib.config.web.container.ContainerListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransientConfigServerTest {

	private static final Logger logger = LoggerFactory
			.getLogger(TransientConfigServerTest.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStart() throws IOException, InterruptedException {

		/*
		 * 
		 */
		DisplayableConfiguration testConfig = new DisplayableConfiguration() {

			/*
			 * Here we are simply putting the settings in a HashMap, but it
			 * could be writing them to disk, or a database.
			 */
			private Map<String, String> properties = new HashMap<String, String>();

			@Override
			public String getProperty(String key) {
				return properties.get(key);
			}

			@Override
			public void setProperty(String key, String value) {
				properties.put(key, value);
			}

			/**
			 * A unique id for the settings.
			 */
			@Override
			public String getId() {
				return "test_settings";
			}

			/**
			 * This describes what settings are available to be modified, and
			 * thus what will be displayed on the webpage.
			 */
			@Override
			public LinkedHashSet<String> getKeys() {
				LinkedHashSet<String> keys = new LinkedHashSet<String>();
				keys.add("my_first_property");
				keys.add("my_second_property");
				keys.add("my_third_property");
				return keys;
			}

			/**
			 * This is what will be displayed as the title of the page.
			 */
			@Override
			public String getDisplayName() {
				return "Test Configuration";
			}

			@Override
			public void setId(String id) {
				//

			}
		};

		/*
		 * The id within the configs HashMap below is used to build the URL to
		 * access the settings display page. In this example to access the
		 * settings you should browse to:
		 * 
		 * http://localhost:8080/?config=my_settings_here
		 * 
		 * Multiple settings can be hosted at one time.
		 */

		Map<String, DisplayableConfiguration> configs = new HashMap<String, DisplayableConfiguration>();
		configs.put("my_settings_here", testConfig);

		// start the configuration server on port 8080, and host the supplied
		// configurations
		ConfigurationServer server = new ConfigurationServer(8080, configs);
		logger.debug("Starting server.");

		// a listener can be added to the server that will be notified whenever
		// a configuration is changed
		server.addListener(new ContainerListener() {

			/**
			 * This gets notified whenever a configuration is modified.
			 */
			@Override
			public void onModifed(Configuration config, String key) {
				logger.debug("Configuration modified: " + config);
			}

			@Override
			public void onCommand(Command command) {
				logger.debug("Server recieved command: " + command);
			}
		});
		
		server.start();

		logger.debug("Server finished.");
	}

}
