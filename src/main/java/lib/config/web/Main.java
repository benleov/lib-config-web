package lib.config.web;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class Main {

	/**.
	 * 
	 * The displayble configurations must be defined in code, and passed to 
	 */	
	public static void main(String[] args) throws Exception {
		
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
				return "transfer";
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
				return "Transfer Configuration";
			}


		};
		
		// the map enforces a unique id between the displayable configurations. 
		Map<String, DisplayableConfiguration> configs = new HashMap<String, 
				DisplayableConfiguration>();
		
		configs.put("test_config", testConfig);
		
		ConfigurationServer server = new ConfigurationServer(8080, configs);
		server.start();

	}
}
