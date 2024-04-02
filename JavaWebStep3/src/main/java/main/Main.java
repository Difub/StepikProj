package main;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import servlets.SignInServlet;
import servlets.SignUpServlet;


public class Main {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        handler.addServletWithMapping(SignUpServlet.class, "/signup");
        handler.addServletWithMapping(SignInServlet.class, "/signin");

        server.start();
        System.out.println("Server started");

        server.join();
    }
}
