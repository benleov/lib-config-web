package lib.config.web.container;

import lib.config.base.configuration.Configuration;

/**
 * 
 * Adapter class for ContainerListener.
 * 
 * @author Benjamin Leov
 *
 */
public abstract class ContainerListenerAdapter implements ContainerListener {

	public void onModifed(Configuration config, String key) {
		// override
	}

	public void onDelete(Configuration config, String key) {
		// override
	}

	public void onAdd(Configuration config, String key) {
		// override
	}

	public void onCommand(Command command) {
		// override
	}
}
