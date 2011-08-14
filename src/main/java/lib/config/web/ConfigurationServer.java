package lib.config.web;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lib.config.base.configuration.Configuration;
import lib.config.web.container.ConfigurationContainer;
import lib.config.web.container.ContainerListener;

import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationServer {

	private static final Logger logger = LoggerFactory
			.getLogger(ConfigurationServer.class);

	private int port;
	private Map<String, DisplayableConfiguration> configs;
	private Connection connection;
	private final Set<ContainerListener> listeners;

	public ConfigurationServer(int port,
			Map<String, DisplayableConfiguration> configs) {
		this.port = port;
		this.configs = configs;
		this.listeners = new HashSet<ContainerListener>();
	}

	public synchronized void start() throws IOException {

		if (connection == null) {
			ConfigurationContainer container = new ConfigurationContainer(
					configs);

			container.addListener(new ContainerListener() {

				@Override
				public void onModifed(Configuration config) {
					for (ContainerListener curr : listeners) {
						curr.onModifed(config);
					}
				}
			});

			Connection connection = new SocketConnection(container);
			SocketAddress address = new InetSocketAddress(port);
			connection.connect(address);
		} else {
			logger.warn("Attempt to start server ignored (already started).");
		}
	}

	public synchronized void stop() {

		if (connection != null) {
			try {
				connection.close();
			} catch (IOException e) {
				logger.warn("Exception occured while stopping server.", e);
			}
			connection = null;
		}
	}

	public void addListener(ContainerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ContainerListener listener) {
		listeners.remove(listener);
	}
}
