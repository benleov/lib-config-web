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
			body.println("<title>Simple Configuration</title>");

			Query query = request.getAddress().getQuery();

			// a post to update a setting
			if (request.getMethod().equalsIgnoreCase("POST")) {

				Query postQuery = request.getQuery();

				String command = postQuery.get("command");

				if ("exit".equals(command)) {
					
					notifyOnCommand(Command.EXIT); 
					
				} else {

					String id = postQuery.get("config_id");
					DisplayableConfiguration conf = config.get(id);

					if (conf != null) {

						boolean modified = false;
						String modifyedKey = null;

						for (String key : postQuery.keySet()) {
							if (conf.getKeys().contains(key)) {
								conf.setProperty(key, postQuery.get(key));
								modifyedKey = key;
								modified = true;
							}
						}

						if (modified) {
							notifyOnUpdate(conf, modifyedKey);
						}

						body.println("Configuration has been updated!");
						body.println("<a href='/'>Back</a>");
					} else {
						body.println("Invalid post submitted.");
					}
				}

			} else if (query.isEmpty()) {

				// display a list of all the configurations
				StringBuilder html = new StringBuilder();
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
				
				appendCommandForm(html);
				
				body.println(html.toString());
				
			} else {

				// unknown request

				String queried = query.get("config");

				DisplayableConfiguration curr = config.get(queried);
				if (curr != null) {
					body.print(displayConfigForm(queried, curr));
				} else {
					body.println("Cannot find config with that identifier.");
				}

			}

			body.println("</html>");

			body.close();
		} catch (IOException e) {
			logger.warn("IOException occured on handle.", e);
		}
	}

	private String displayConfigForm(String id, DisplayableConfiguration config) {
		StringBuilder html = new StringBuilder();
		html.append("<h3>");
		html.append("Configuration Form for ");
		html.append(config.getDisplayName());
		html.append("</h3>");
		html.append("<form action='.' method='post'>\n");
		html.append("<input type='hidden' name='config_id' value='");
		html.append("<input type='hidden' name='command' value='update'");
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
		

		
		
		return html.toString();
	}

	public void addListener(ContainerListener listener) {
		listeners.add(listener);
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
	
	private void appendCommandForm(StringBuilder html) {
		
		html.append("<h2>Server Commands</h2>");
		html.append("<form action='.' method='post'>");
		html.append("<input type='hidden' name='command' value='exit' />");
		html.append("<input type='submit' value='Shutdown' />");
		html.append("</form>");
	}
}
