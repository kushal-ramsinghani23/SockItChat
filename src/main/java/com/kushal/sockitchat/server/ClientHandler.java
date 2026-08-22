/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.kushal.sockitchat.server;

import com.kushal.sockitchat.common.UserContext;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private DataOutputStream out;
    private String username;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // Initialize streams
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            this.out = new DataOutputStream(clientSocket.getOutputStream());

            // First message from client is always the username
            this.username = in.readUTF();
            UserContext.currentUser.set(username);
            
            // Safe to add — out is fully initialized
            Server.clients.add(this);
            System.out.println(username + " connected! Total: " + Server.clients.size());

            // Read messages and broadcast to all other clients
            Thread readerThread = new Thread(() -> {
                try {
                    // No more while(readUTF != null)
                    // Instead — loop forever, catch EOFException to detect disconnect
                    while(true) {
                        String clientMessage = in.readUTF();
                        broadcast(clientMessage);
                    }
                } catch (IOException e) {
                    System.out.println(username + " disconnected.");
                } finally {
                    // remove dead handler — prevents broadcasting to closed sockets
                    Server.clients.remove(this);
                    
                    // clear ThreadLocal — prevents stale data leaking into reused pool threads
                    UserContext.currentUser.remove();
                    System.out.println(username + " removed. Total: " + Server.clients.size());
                }
            });

            readerThread.start();
            readerThread.join();
            
            UserContext.currentUser.remove();
            
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
                try {
                    ch.out.writeUTF(username + ": " + message);
                    ch.out.flush(); // for DataOutputStream we have to do this MANUALLY
                } catch (IOException ex) {
                    System.getLogger(ClientHandler.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        }
    }
}