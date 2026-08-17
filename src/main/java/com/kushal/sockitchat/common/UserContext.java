/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kushal.sockitchat.common;

/**
 *
 * @author kushal-ramsinghani
 */
public class UserContext {
    public static final ThreadLocal<String> currentUser = new ThreadLocal<>();
}
