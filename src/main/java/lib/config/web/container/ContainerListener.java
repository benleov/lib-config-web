package lib.config.web.container;

import lib.config.base.configuration.Configuration;

public interface ContainerListener {
	/**
	 * Called when the configuration has been modified.
	 */
	void onModifed(Configuration config);
	
}
