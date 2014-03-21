package lib.config.web.container;

import lib.config.base.configuration.Configuration;

public interface ContainerListener {
	/**
	 * Called when a configuration key has been modified.
	 */
	void onModifed(Configuration config, String key);

	/**
	 * Called when a configuration key has been deleted.
	 */
	void onDelete(Configuration config, String key);

	/**
	 * Called when a configuration key has been added.
	 */
	void onAdd(Configuration config, String key);

	/**
	 * Called when the container has received a command.
	 * 
	 * @param command
	 *            The command.
	 */
	void onCommand(Command command);
}
