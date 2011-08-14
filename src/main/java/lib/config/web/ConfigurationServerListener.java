package lib.config.web;

import lib.config.base.configuration.Configuration;

public interface ConfigurationServerListener {

	void onConfigurationModified(Configuration config);
	
}
