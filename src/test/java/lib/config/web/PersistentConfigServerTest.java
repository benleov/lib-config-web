package lib.config.web;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import lib.config.base.configuration.Configuration;
import lib.config.base.configuration.ConfigurationException;
import lib.config.base.configuration.ConfigurationList;
import lib.config.base.configuration.factory.ConfigurationFactory;
import lib.config.base.configuration.impl.BasicConfiguration;
import lib.config.base.configuration.persist.impl.IniPersister;
import lib.config.web.container.Command;
import lib.config.web.container.ContainerListenerAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentConfigServerTest {

	private static final Logger logger = LoggerFactory
			.getLogger(TransientConfigServerTest.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	// TODO:
	@Test
	public void testStart() throws IOException, InterruptedException {

		/*
		 * The settings file the settings server will modify.
		 */
		final File settingsFile = new File("my_settings.ini");
		Map<String, DisplayableConfiguration> configs = new HashMap<String, DisplayableConfiguration>();

		/*
		 * There is no way (without resorting to reflection) to construct a
		 * parameterized variable Because of this, we need to supply a factory
		 * to the persister so it knows how to construct the loaded display
		 * configurations
		 */

		final IniPersister<DisplayableConfiguration> persister = new IniPersister<DisplayableConfiguration>(
				new ConfigurationFactory<DisplayableConfiguration>() {

					class BasicDisplayable extends BasicConfiguration implements
							DisplayableConfiguration {

						private String name;

						@Override
						public String getDisplayName() {
							return name;
						}

						@Override
						public LinkedHashSet<String> getKeys() {
							return new LinkedHashSet<String>(super.getKeys());
						}

					}

					// this converts a basic configuration to a displayable
					// configuration
					@Override
					public DisplayableConfiguration buildConfiguration(
							String name) {
						BasicDisplayable bd = new BasicDisplayable();
						bd.name = name;
						return bd;
					}

				});

		try {

			/*
			 * Here the list of configurations is constructed.
			 */
			final ConfigurationList<DisplayableConfiguration> list = persister
					.read(settingsFile);

			for (DisplayableConfiguration config : list.getConfigurations()) {
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
