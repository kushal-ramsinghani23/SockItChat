/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kushal.sockitchat.server;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/**
 *
 * @author kushal-ramsinghani
 */
public class Server {
    public static void main(String[] args) {
        System.out.println("Server is waiting for a client connection...");
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try (ServerSocket serverSocket = new ServerSocket(5000);
             Socket clientSocket = serverSocket.accept();
             
             // Setup tools to read from and write to the client
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            System.out.println("Client connected!");
            
            // Read the message sent by client
            Thread readerThread = new Thread(() -> {
                try {
                    String clientMessage;
                    while((clientMessage = in.readLine()) != null) {
                        System.out.println("Message received from client: " + clientMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            });
           
            // Write a message to the client
            Thread writerThread = new Thread(() -> {
                try {
                    String message;
                    while((message = br.readLine()) != null) {
                        out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            });
            
            readerThread.start();
            writerThread.start();
            
            readerThread.join();
            writerThread.join();
            
            
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } catch (InterruptedException ex) {
            System.getLogger(Server.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}
