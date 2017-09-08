package com.san.embedded.tomcat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.log4j.Logger;

public class EmbeddedServer {

	private static EmbeddedServer _this;
	private Logger logger = Logger.getLogger(this.getClass());

	public static EmbeddedServer getEmbeddedServer(int port) throws ServletException, FileNotFoundException {
		if (_this == null) {
			_this = new EmbeddedServer();
			_this.PORT = port;
			_this.setup();
		}
		return _this;
	}

	private int PORT = 8080;
	private Tomcat tomcat;

	private void setup() throws ServletException, FileNotFoundException {
		tomcat = new Tomcat();
		tomcat.setPort(PORT);
		tomcat.setBaseDir("");
		tomcat.getConnector();
		deployWarsFromCurrentPath(tomcat);
	}

	public void startServer() throws LifecycleException {
		if (tomcat.getServer() != null && tomcat.getServer().getState() != LifecycleState.STARTED) {
			tomcat.start();
			new Thread() {
				@Override
				public void run() {
					tomcat.getServer().await();
				}
			}.start();
			logger.info("Server started at port : " + PORT);
		}
	}

	public void stopServer() throws LifecycleException {
		if (tomcat.getServer() != null && tomcat.getServer().getState() != LifecycleState.DESTROYED && tomcat.getServer().getState() != LifecycleState.STOPPED) {
			tomcat.stop();
			tomcat.destroy();
		}
	}

	private void deployWarsFromCurrentPath(Tomcat tomcat) throws ServletException, FileNotFoundException {
		Map<String, String> warsMap = findWarsOnCurrentPath();
		for (String path : warsMap.keySet()) {
			StandardContext ctx = (StandardContext) tomcat.addWebapp(path, warsMap.get(path));
			logger.debug("War file : " + warsMap.get(path) + " deployed at context : " + path);
			WebResourceRoot resources = new StandardRoot(ctx);
			ctx.setResources(resources);
		}
	}

	private Map<String, String> findWarsOnCurrentPath() throws FileNotFoundException {
		Map<String, String> warsMap = new HashMap<String, String>();
		File root = new File("./webapps");
		boolean rootFound = false;
		logger.debug("webapps path : " + root.getAbsolutePath());
		if (!root.exists()) {
			throw new FileNotFoundException("Folder : " + root.getAbsolutePath() + " doesn't exists. ");
		}
		if (root.isDirectory()) {
			logger.debug("webapps is a folder.");
			for (File file : root.listFiles()) {
				String fileName = file.getName();
				if (file.isFile() && fileName.endsWith(".war")) {
					String name = fileName.substring(0, fileName.indexOf("."));
					logger.debug("File under current path : " + fileName + " is a valid war file.");
					if (!rootFound && name.equalsIgnoreCase("root")) {
						rootFound = true;
						warsMap.put("", fileName);
						logger.debug("War file : " + fileName + " will be deployed at root context.");
						break;
					} else {
						// warsMap.put("/" + name, fileName);
						logger.debug("War file : " + fileName + " will not be deployed.");
					}
				} else {
					logger.debug("File under current path : " + fileName + " is not a valid war file.");
				}
			}
		} else {
			logger.debug("webapp is not a folder on current path.");
		}
		if (!rootFound) {
			logger.info("No root.war file found at path : " + root.getAbsolutePath() + ". No deployable for web server.");
		}
		return warsMap;
	}

}
