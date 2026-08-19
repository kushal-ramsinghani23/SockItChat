/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kushal.sockitchat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        System.out.println("Connecting to the server...");

        // Keyboard reader — outside try so System.in isn't closed
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try (Socket socket = new Socket("localhost", 5000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Register username — server reads this as first message
            System.out.print("Enter username: ");
            out.println(br.readLine());

            // Receive and display messages from other clients
            Thread readerThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            });

            // Read keyboard input and send to server
            Thread writerThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = br.readLine()) != null) {
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}