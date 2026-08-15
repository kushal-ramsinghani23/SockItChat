/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kushal.sockitchat.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author kushal-ramsinghani
 */
public class Server {
    static List<ClientHandler> clients = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("Server is waiting for a client connection...");
        
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
                    
            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected!");
                
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                
                new Thread(clientHandler).start();
            } 
            
        } catch(IOException e) {
            System.out.println("Server Exception: " + e);
        }
    }
}