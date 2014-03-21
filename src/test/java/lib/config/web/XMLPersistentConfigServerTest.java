package lib.config.web;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lib.config.base.configuration.Configuration;
import lib.config.base.configuration.ConfigurationException;
import lib.config.base.configuration.ConfigurationList;
import lib.config.base.configuration.persist.impl.SimpleXMLPersister;
import lib.config.web.container.ContainerListenerAdapter;
import lib.config.web.impl.DisplayableBasicConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * SAMPLE configuration XML:
 * 
 * <configurationList>
 *   <configurations class="lib.config.base.configuration.impl.BasicConfiguration" id="test_config_one">
 *      <property key="some_key_one">some_value_one</property>
 *   </configurations>
 * </configurationList>
 *
 */

public class XMLPersistentConfigServerTest {

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
		 * The settings file the settings server will modify.
		 */
		final File settingsFile = new File("my_settings.xml");
		
		final SimpleXMLPersister<DisplayableBasicConfiguration> persister = 
				new SimpleXMLPersister<DisplayableBasicConfiguration>();
		
		Map<String, DisplayableConfiguration> configs = 
				new HashMap<String, DisplayableConfiguration>();
 
		try {

			/*
			 * Here the list of configurations is constructed.
			 */
			final ConfigurationList<DisplayableBasicConfiguration> list = persister
					.read(settingsFile);

			for (DisplayableBasicConfiguration config : list.getConfigurations()) {
				configs.put(config.getId(), config);
			}

			/*
			 * The id within the configs HashMap below is used to build the URL
			 * to access the settings display page. In this example to access
			 * the settings you should browse to:
			 * 
			 * http://localhost:8080/?config=my_settings_here
			 * 
			 * Multiple settings can be hosted at one time.
			 */

			// start the configuration server on port 8080, and host the
			// supplied
			// configurations
			ConfigurationServer server = new ConfigurationServer(8080, configs);

			logger.debug("Starting server.");

			// a listener can be added to the server that will be notified
			// whenever
			// a configuration is changed
			server.addListener(new ContainerListenerAdapter() {

				/**
				 * This gets notified whenever a configuration is modified.
				 */
				@Override
				public void onModifed(Configuration config, String key) {
					writeToFile();
				}

				@Override
				public void onDelete(Configuration config, String key) {
					writeToFile();
				}

				@Override
				public void onAdd(Configuration config, String key) {
					writeToFile();
				}

				public void writeToFile() {
					// notify the persister that the settings have been
					// modified, and save them

					try {
						persister.write(list, settingsFile);
					} catch (ConfigurationException e) {
						logger.error(
								"There was a problem trying to write to the settings file.",
								e);
					}
				}

			});

			server.start();

			logger.debug("Server finished.");
		} catch (ConfigurationException e) {

			// no file was found. No settings will be displayed to edit.
			logger.error(
					"No settings file was found. Cannot start web configuration.",
					e);
		}
	}
}
