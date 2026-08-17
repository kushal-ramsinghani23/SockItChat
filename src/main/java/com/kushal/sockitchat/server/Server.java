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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
/**
 *
 * @author kushal-ramsinghani
 */
public class Server {
    static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    static ExecutorService pool = Executors.newFixedThreadPool(100);

    public static void main(String[] args) {
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                pool.shutdown();
                pool.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                System.out.println("Shutdown interrupted: " + ex.getMessage());
                Thread.currentThread().interrupt();
            }
        }));
        
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            
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