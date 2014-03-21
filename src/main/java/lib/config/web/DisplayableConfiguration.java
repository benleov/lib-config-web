package lib.config.web;
import java.util.LinkedHashSet;

import lib.config.base.configuration.Configuration;

public interface DisplayableConfiguration extends Configuration {

	
	String getDisplayName();
	
	/**
	 * Order preserved set, as these they will be displayed on the page in this order.
	 * 
	 * @return
	 */
	LinkedHashSet<String> getKeys();


	
	
}
