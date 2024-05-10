package main;

import server.EchoSocketHandlerCreator;
import server.Server;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        startThreadBasedEchoServer();
        //startNIOBasedEchoServer();

        System.out.println("Main finished");
    }

    private static void startThreadBasedEchoServer() throws IOException, InterruptedException {
        new Server(new EchoSocketHandlerCreator()).join();
    }
}
