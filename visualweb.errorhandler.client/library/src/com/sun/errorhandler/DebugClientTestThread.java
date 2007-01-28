/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */ 
package com.sun.errorhandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/*
 * DebugClientThread.java
 * Created on January 6, 2004, 1:05 PM
 */

/**
 * @author  Winston Prakash
 */
public class DebugClientTestThread extends Thread {
    BufferedReader in = null;
    PrintWriter out = null;
    
    JTextArea textArea = null;
    
    StringBuffer message = new StringBuffer();
    
    Socket clientSocket = null;
    
    boolean connected = false;
        boolean connectionFailed = false;
        
        public DebugClientTestThread(JTextArea textArea) {
        this.textArea = textArea;
    }
    
    public void run() {
        String line;
        
        try {
            clientSocket = new Socket("localhost", 4444);
            displayText("Connected to server\n");
            connected = true;
        } catch (UnknownHostException exc) {
            displayText("Could not connect. unknown host." + exc.getLocalizedMessage());
            connectionFailed = true;
        } catch (IOException exc) {
            displayText("Couldn't get I/O for the connection" + exc.getLocalizedMessage());
            connectionFailed = true;
        }
        
        try{
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException exc) {
            displayText("Could not get the socket I/O" + exc.getLocalizedMessage());
        }
        
        while(connected){
            try{
                line = in.readLine();
                if(line != null){
                    displayText(line);
                    message.append(line + "\n");
                }else{
                    connected = false;
                    displayText("Connection closed");
                }
            } catch (IOException exc) {
                displayText(exc.getLocalizedMessage());
                connected = false;
            }
        }
        
    }
    
    public boolean isConnected(){
        while (!connected){
             if(connectionFailed) return false;
        }
        return connected;
    }    
    public void disconnect(){
        if(connected){
            try{
                out.close();
                in.close();
                clientSocket.close();
            }catch (IOException exc) {
                displayText("Error occured while disconnecting socket" + exc.getLocalizedMessage());
            }
            out = null;
            in = null;
            clientSocket = null;
            connected = false;
        }
    }
    
    public void sendMessage(String text){
        if(out != null){
            out.println(text);
        }
    }
    
    public String getMessage(){
        return message.toString();
    }
    
    private void displayText(final String line){
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                textArea.append(line);
            }
        });
    }
}
