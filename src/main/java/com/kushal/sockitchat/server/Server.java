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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            
            // Create once — fixed pool of 100 threads
            ExecutorService pool = Executors.newFixedThreadPool(100);
            
            while(true) {
                Socket clientSocket = serverSocket.accept();
                
                // Submit a task — pool assigns it to a free thread
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                pool.execute(clientHandler);
            } 
            
        } catch(IOException e) {
            System.out.println("Server Exception: " + e);
        }
    }
}