package lib.config.web;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;


public class ConfigurationServer {

	private int port;
	Map<String, DisplayableConfiguration> configs;
	
	public ConfigurationServer(int port, Map<String, DisplayableConfiguration> configs) {
		this.port = port;
		this.configs = configs;
	}
	
	public void start() throws IOException {		
		Container container = new ConfigurationContainer(configs);
		Connection connection = new SocketConnection(container);
		SocketAddress address = new InetSocketAddress(port);
		connection.connect(address);
		
		
	}
	
}
