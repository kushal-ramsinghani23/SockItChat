/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kushal.sockitchat.server;
import static com.kushal.sockitchat.server.Server.clients;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
/**
 *
 * @author kushal-ramsinghani
 */
public class ClientHandler implements Runnable{
    Socket clientSocket;
    PrintWriter out;
    
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    
    @Override
    public void run() {
        Server.clients.add(this);
        
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);

            Thread readerThread = new Thread(() -> {
                try {
                    String clientMessage;
                    while((clientMessage = in.readLine()) != null) {
                        System.out.println("Broadcasting to " + Server.clients.size() + " clients");
                        broadcast(clientMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            });
            readerThread.start();
            readerThread.join();
           
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } catch (InterruptedException ex) {
            System.getLogger(Server.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
    }
    
    private void broadcast(String message) {
        for(ClientHandler ch: Server.clients) {
            if(!(ch.equals(this)))
                ch.out.println(message);
        }
    }
    
}


