package lib.config.web.container;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lib.config.base.configuration.Configuration;
import lib.config.web.DisplayableConfiguration;

import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides the web interface, and parses incoming commands.
 * 
 * @author Benjamin Leov
 *
 */
public class ConfigurationContainer implements Container {
	private static final Logger logger = LoggerFactory
			.getLogger(ConfigurationContainer.class);

	private final Map<String, DisplayableConfiguration> config;
	private final Set<ContainerListener> listeners;

	/**
	 * 
	 * 
	 * @param config
	 *            Displayable configurations, hashed on an Id. This does not
	 *            have to be the id of the configuration.
	 */
	public ConfigurationContainer(Map<String, DisplayableConfiguration> config) {
		this.config = config;
		this.listeners = new HashSet<ContainerListener>();
	}

	@Override
	public void handle(Request request, Response response) {
		try {

			long time = System.currentTimeMillis();

			response.setContentType("text/html");
			// response.set("Server", "ConfigurationServer/1.0 (Simple 4.0)");
			response.setDate("Date", time);
			response.setDate("Last-Modified", time);

			PrintStream body = response.getPrintStream();
			body.println("<html>");
			body.println("<title>Simple Configuration Server</title>");

			Query query = request.getAddress().getQuery();
			StringBuilder html = new StringBuilder();

			// command/navigation request
			if (request.getMethod().equalsIgnoreCase("GET")) {
				Query postQuery = request.getQuery();
				
				Command command = parseCommand(postQuery);
				
				// default index page
				if (query.isEmpty()) {

					// display a list of all the configurations

					html.append("<h2>Configurations</h2>");
					html.append("<ul>");

					for (String key : config.keySet()) {

						DisplayableConfiguration curr = config.get(key);
						html.append("<li>");
						html.append("<a href='?config=");
						html.append(key);
						html.append("'>");
						html.append(curr.getDisplayName());
						html.append("</a>");
						html.append("</li>");
					}

					html.append("</ul>");

					appendCommandsForm(html, Command.EXIT);

				} else {

					
					String queried = query.get("config");
					DisplayableConfiguration curr = config.get(queried);

					switch (command) {
					case ADD:
						// TODO display add form
						html.append("Dispaly add form");
						break;
					case UPDATE:
						// TODO display update form
						html.append("Dispaly update form");
						break;
					case DELETE:
						// TODO display delete form
						html.append("Dispaly delete form");
						break;
					case EXIT:
						notifyOnCommand(Command.EXIT);
						html.append("Server has now stopped.");
						break;
					case VIEW:
					default:
						// no command specified. Just view the config.

						if (curr != null) {
							// display the config form
							appendConfigForm(html, queried, curr);
							appendAllCommandsForm(html);
						} else {
							// unknown request
							appendError(html,
									"Cannot find config with that identifier.");
						}

						break;
					}
				}

			} else if (request.getMethod().equalsIgnoreCase("POST")) {
				// a post to update a setting

				Query postQuery = request.getQuery();
				Command command = parseCommand(postQuery);
				
				String id = postQuery.get("config_id");
				
				DisplayableConfiguration conf = config.get(id);

				if (conf == null) {
					appendError(html,
							"Invalid command received. No config specified.");
				} else {

					boolean modified = false;
					String modifyedKey = null;

					switch (command) {
					case DELETE:

						for (String key : postQuery.keySet()) {
							if (conf.removeProperty(key)) {
								notifyOnDelete(conf, key);
							}
						}

						html.append("Configuration has been updated!");
						html.append("<a href='/'>Back</a>");

						break;
					case ADD:

						for (String key : postQuery.keySet()) {
							conf.setProperty(key, postQuery.get(key));
							notifyOnAdd(conf, key);
						}

						html.append("Configuration has been updated!");
						html.append("<a href='/'>Back</a>");
						break;
					case UPDATE:

						// find any matching keys that have been updated
						for (String key : postQuery.keySet()) {
							if (conf.getKeys().contains(key)) {
								conf.setProperty(key, postQuery.get(key));
								modifyedKey = key;
								modified = true;
								break;
							}
						}

						if (modified) {
							notifyOnUpdate(conf, modifyedKey);
						}

						html.append("Configuration has been updated!");
						html.append("<a href='/'>Back</a>");

						break;
					default:
						appendError(html, "Invalid command received.");
						break;
					}
				}
			}

			body.print(html.toString());
			body.println("</html>");

			body.close();
		} catch (IOException e) {
			logger.warn("IOException occured on handle.", e);
		}
	}

	private Command parseCommand(Query query) {
		
		String commandStr = query.get("command");

		Command command = null;
		if (commandStr != null) {
			command = Command.valueOf(commandStr);
		}

		// default command, if not specified, is view
		if (command == null) {
			command = Command.VIEW;
		}

		return command;
	}

	private void appendError(StringBuilder html, String errorMessage) {
		html.append("<h2>Error</h2>");
		html.append("<p>" + errorMessage + "</p>");
	}

	private void appendConfigForm(StringBuilder html, String id,
			DisplayableConfiguration config) {
		html.append("<h3>");
		html.append("Configuration Form for ");
		html.append(config.getDisplayName());
		html.append("</h3>");
		html.append("<form action='.' method='post'>\n");
		
		html.append("<input type='hidden' name='command' value='"
				+ Command.UPDATE.toString() + "' />");
		
		// add the config id into the form
		html.append("<input type='hidden' name='config_id' value='");
		html.append(id);
		html.append("' />\n");

		for (String key : config.getKeys()) {
			html.append("<br />");
			html.append("<label>");
			html.append(key);
			html.append("</label>");
			html.append("<input type='text' name='" + key + "' ");

			String value = config.getProperty(key);

			if (value != null) {
				html.append("value='");
				html.append(value);
				html.append("'");
			}

			html.append("/>");
		}
		html.append("<br />");
		html.append("<input type='submit'/>");

		html.append("</form>");

	}

	private void appendCommandsForm(StringBuilder html, Command... commands) {

		html.append("<h2>Server Commands</h2>");

		for (Command curr : commands) {
			html.append("<form action='.' method='get'>");
			html.append("<input type='hidden' name='command' value='"
					+ curr.toString() + "' />");
			html.append("<input type='submit' value='" + curr.toString()
					+ "' />");
			html.append("</form>");
		}
	}

	private void appendAllCommandsForm(StringBuilder html) {
		appendCommandsForm(html, Command.values());
	}

	public void addListener(ContainerListener listener) {
		listeners.add(listener);
	}

	private void notifyOnAdd(Configuration config, String key) {
		for (ContainerListener curr : listeners) {
			curr.onAdd(config, key);
		}
	}

	private void notifyOnDelete(Configuration config, String key) {
		for (ContainerListener curr : listeners) {
			curr.onDelete(config, key);
		}
	}

	private void notifyOnUpdate(Configuration config, String key) {
		for (ContainerListener curr : listeners) {
			curr.onModifed(config, key);
		}
	}

	private void notifyOnCommand(Command command) {
		for (ContainerListener curr : listeners) {
			curr.onCommand(command);
		}
	}

}
