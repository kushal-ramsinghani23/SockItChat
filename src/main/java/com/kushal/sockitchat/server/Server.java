/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kushal.sockitchat.server;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
/**
 *
 * @author kushal-ramsinghani
 */
public class Server {
    public static void main(String[] args) {
        System.out.println("Server is waiting for a client connection...");
        
        try (ServerSocket serverSocket = new ServerSocket(5000);
             Socket clientSocket = serverSocket.accept();
             
             // Setup tools to read from and write to the client
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            System.out.println("Client connected!");
            
            // 1. Read the message sent by the client
            String clientMessage = in.readLine();
            System.out.println("Message received from client: " + clientMessage);
            
            // 2. Send a reply back to the client
            out.println("Hello back from Server!");
            
        } catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}
