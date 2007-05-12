/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
