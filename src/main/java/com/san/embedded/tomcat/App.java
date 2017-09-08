package com.san.embedded.tomcat;

import java.io.FileNotFoundException;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.log4j.Logger;

public class App {

	private static Logger logger = Logger.getLogger(App.class);

	public static void main(String[] args) throws ServletException, LifecycleException, FileNotFoundException {
		int port = 0;
		logger.info("Note : Put war file as root.war inside webapps folder on current path");
		boolean invalidDataFound = false;
		if (args.length < 1) {
			logger.error("Please provide server port.");
			invalidDataFound = true;
		} else {
			try {
				port = Integer.parseInt(args[0]);
				if (port < 1) {
					logger.error("Please provide valid server port.");
					invalidDataFound = true;
				}
			} catch (Exception e) {
				logger.error("Please provide valid server port.");
				invalidDataFound = true;
			}
		}
		if (invalidDataFound) {
			System.exit(0);
		}
		EmbeddedServer obj = EmbeddedServer.getEmbeddedServer(port);
		obj.startServer();
	}

}
