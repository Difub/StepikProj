package main;


import servlets.ResourcesServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Main
{
	public static void main(String[] args) throws Exception
	{

		Server server = new Server(8080);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.addServlet(new ServletHolder(new ResourcesServlet()), ResourcesServlet.PAGE_URL);

		server.setHandler(context);

		server.start();
		java.util.logging.Logger.getGlobal().info("Server started");
		server.join();
	}
}
