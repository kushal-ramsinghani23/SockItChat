/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kushal.sockitchat.client;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
/**
 *
 * @author kushal-ramsinghani
 */
public class Client {
    public static void main(String[] args) {
        System.out.println("Connecting to the server...");
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try (Socket socket = new Socket("localhost", 5000);
             
             // Setup tools to write to and read from the server
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            System.out.println("Connected! Sending message to server...");
            
            // Read the message sent by server
            Thread readerThread = new Thread(() -> {
                try {
                    String connectedClientMessage;
                    while((connectedClientMessage = in.readLine()) != null) {
                        System.out.println("Message received from connected client: " + connectedClientMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            });
            
            // Write a message to the server
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
            System.out.println("Client error: " + e.getMessage());
        } catch (InterruptedException ex) {
            System.getLogger(Client.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}
