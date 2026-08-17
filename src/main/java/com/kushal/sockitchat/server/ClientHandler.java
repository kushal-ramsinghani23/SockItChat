/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.kushal.sockitchat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // Initialize streams
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);

            // First message from client is always the username
            this.username = in.readLine();

            // Safe to add — out is fully initialized
            Server.clients.add(this);
            System.out.println(username + " connected! Total: " + Server.clients.size());

            // Read messages and broadcast to all other clients
            Thread readerThread = new Thread(() -> {
                try {
                    String clientMessage;
                    while ((clientMessage = in.readLine()) != null) {
                        broadcast(clientMessage);
                    }
                } catch (IOException e) {
                    System.out.println(username + " disconnected.");
                }
            });

            readerThread.start();
            readerThread.join();

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Send message to all clients except sender
    private void broadcast(String message) {
        for (ClientHandler ch : Server.clients) {
            if (!ch.equals(this)) {
                ch.out.println(username + ": " + message);
            }
        }
    }
}