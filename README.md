lib_config_web
==================================================

lib_config_web provides an extremely lightweight web front end to the configuration base library.  It generates an HTML form that the user can browse to, view and edit the settings, which may be in whatever format lib_config_base supports. Examples for memory based, INI and XML based settings are below.

Dependencies
--------------------------------------

* [lib_config_base] (https://github.com/benleov/lib_config_base) - Provides access to the underlying settings
* [Simple Framework] (http://www.simpleframework.org/) - Simple HTML server
* [SLF4J](www.slf4j.org/‎) - Logging facacde

Usage
--------------------------------------

Below is an example of how a simple in-memory settings web interface can be created. When running, it hosts a page on port 8080, which contains a form to update all settings within the settings file. 

```java
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

```

Here is an example of how the same web interface can be used for an ini file. It also runs on port 8080. Note that a settings file called "my_settings.ini" must be present. 

```java

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
			server.addListener(new ContainerListener() {

				/**
				 * This gets notified whenever a configuration is modified.
				 */
				@Override
				public void onModifed(Configuration config, String key) {

					// notify the persister that the settings have been
					// modified, and save them.
					try {
						persister.write(list, settingsFile);
					} catch (ConfigurationException e) {
						logger.error(
								"There was a problem trying to write to the settings file.",
								e);
					}
				}

				@Override
				public void onCommand(Command command) {
					logger.debug("Server recieved command: " + command);
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


```

And finally an example using XML settings. This requires a my_settings.xml file to exist. The settings file will look something like the following, once populated with settings.

```xml
<configurationList>
   <configurations class="lib.config.web.impl.DisplayableBasicConfiguration" id="test_config_one">
      <property key="some_key_one">two</property>
   </configurations>
</configurationList>

```

The code is as follows.

```java


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

```

