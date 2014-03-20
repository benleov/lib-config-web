lib_config_web
==================================================

An example plugin that provides an extremely lightweight web front end to the configuration base library. It allows the user to update an ini style settings file via a dynamically created webpage. 

Dependencies
--------------------------------------

* [lib_config_base] (https://github.com/benleov/lib_config_base)
* [Simple Framework] (http://www.simpleframework.org/)
* [SLF4J](www.slf4j.org/‎) 

Usage
--------------------------------------

Below is an example of how a simple in-memory settings web interface can be created. Note that we could use a different configuration, so that the web interface updated a persistent source, such as an XML or INI file. 

```java
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
		server.start();

		// a listener can be added to the server that will be notified whenever
		// a configuration is changed
		server.addListener(new ContainerListener() {

			/**
			 * This gets notified whenever a configuration is modified.
			 */
			@Override
			public void onModifed(Configuration config) {
				logger.debug("Configuration modified: " + config);
			}
		});

		/*
		 * A real application would synchronise this properly, but for this
		 * example the thread simply waits here for a set time.
		 */
		Thread.sleep(50000);

		logger.debug("Server finished.");

```
