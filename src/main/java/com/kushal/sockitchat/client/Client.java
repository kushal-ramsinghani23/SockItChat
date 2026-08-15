/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kushal.sockitchat.client;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
/**
 *
 * @author kushal-ramsinghani
 */
public class Client {
    public static void main(String[] args) {
        System.out.println("Connecting to the server...");
        
        try (Socket socket = new Socket("localhost", 5000);
             
             // Setup tools to write to and read from the server
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            System.out.println("Connected! Sending message to server...");
            
            // 1. Send the hardcoded message to the server
            out.println("Hello");
            
            // 2. Read the reply from the server
            String serverMessage = in.readLine();
            System.out.println("Message received from server: " + serverMessage);
            
        } catch (Exception e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
