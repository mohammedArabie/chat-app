package com.jets.chat.server;

import com.jets.chat.server.context.ServerManager;

import java.rmi.RemoteException;

public class Server {
    public static void main(String[] args) {
        try {
            ServerManager serverManager = ServerManager.getInstance();
            serverManager.startServer();

            // Needed when using exec:java to run the server
            // Not needed when using java to run
            Thread.currentThread().join();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
