package lib.config.web;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import org.simpleframework.http.Form;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

public class ConfigurationContainer implements Container {

	private final Map<String, DisplayableConfiguration> config;
	
	/**
	 * 
	 * 
	 * @param config Displayable configurations, hashed on an Id. This does not
	 *  have to be the id of the configuration.
	 */
	public ConfigurationContainer(Map<String, DisplayableConfiguration> config) {
		this.config = config;
	}
	
	@Override
	public void handle(Request request, Response response) {
		try {
			
			long time = System.currentTimeMillis();
			response.set("Content-Type", "text/html");
			response.set("Server", "ConfigurationServer/1.0 (Simple 4.0)");
			response.setDate("Date", time);
			response.setDate("Last-Modified", time);
			
			PrintStream body = response.getPrintStream();
			body.println("<html>");
			body.println("<title>Simple Configuration</title>");

			Query query = request.getAddress().getQuery();
			
			if(request.getMethod().equalsIgnoreCase("POST")) {
				
				Form form = request.getForm();
				String id = form.get("config_id");
				DisplayableConfiguration conf = config.get(id);
				
				if(conf != null) {
					for(String key : form.keySet()) {
						if(conf.getKeys().contains(key)) {
							conf.setProperty(key, form.get(key));
						} 
					}
					body.println("Configuration has been updated!");
				} else {
					body.println("Invalid post submitted.");
				}
				
			} else if(query.isEmpty()) {
				
				
				body.println("<h2>Configurations</h2>");
				body.println("<ul>");
				
				for(String key : config.keySet()) {
					
					DisplayableConfiguration curr = config.get(key);
					body.println("<li>");
					body.print("<a href='?config=");
					body.print(key);
					body.print("'>");
					body.print(curr.getDisplayName());
					body.print("</a>");
					body.println("</li>");
				}
				body.println("</ul>");
				;
			} else {
				
				String queried = query.get("config");
				
				DisplayableConfiguration curr = config.get(queried);
				if(curr != null) {
					body.print(displayConfigForm(queried, curr));
				} else {
					body.println("Cannot find config with that identifier.");
				}
				
			}
			
			body.println("</html>");

			body.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// Server could not write back to client
			e.printStackTrace();
		}
	}
	
	private String displayConfigForm(String id, DisplayableConfiguration config) {
		StringBuilder html = new StringBuilder();
		html.append("<h3>");
		html.append("Configuration Form for ");
		html.append(config.getDisplayName());
		html.append("</h3>");
		html.append("<form action='.' method='POST'>\n");
		html.append("<input type='hidden' name='config_id' value='");
		html.append(id);
		
		html.append("' />\n");
		
		for(String key : config.getKeys()) {
			html.append("<br />");
			html.append("<label>");
			html.append(key);
			html.append("</label>");
			html.append("<input type='text' name='" + key +"' ");
			
			String value = config.getProperty(key);
			
			if(value != null) 
			{
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

}
