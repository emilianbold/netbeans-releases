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

/*
 * DebugClientThread.java
 * Created on January 6, 2004, 1:05 PM
 */
package com.sun.errorhandler;

/**
 * @author  Winston Prakash
 */
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class DebugClientThread extends Thread {
    BufferedReader in = null;
    PrintWriter out = null;
    
    StringBuffer message = new StringBuffer();
    
    Socket clientSocket = null;
    
    boolean connected = false;
    boolean connectionFailed = false;
    
    public DebugClientThread() {
    }
    
    protected static String errorHost = null ;
    private static int errorPort = 0 ;

    protected static void setErrorPort(String newPortVal) {
        errorPort = 24444 ;
        if ( newPortVal != null ) {
            try {
                errorPort = Integer.parseInt( newPortVal ) ;
            }
            catch (Exception e) {
                errorPort = 24444 ;
            }
            if ( errorPort < 1 || errorPort > 65536 ) {
                errorPort = 24444 ;
            }   
        }
    }
    protected static int getErrorPort() { 
        return errorPort ;
    }
    
    boolean triedToConnect = false ;
    public void run() {
        triedToConnect = true ;
        String line;
        
        if ( errorHost == null ) {
            displayText("no errorHost, will not talk to creator.") ;  //NOI18N
            connected = false ;
            connectionFailed = true ;
            return ;
        }
        
        try {
            clientSocket = new Socket(errorHost, errorPort);
            displayText("Connected to server " + errorHost + ", port " + errorPort );  //NOI18N
            connected = true;
        } catch (UnknownHostException exc) {
            displayText("Could not connect. unknown host " + errorHost + ", port " + errorPort + "; "  + exc.getLocalizedMessage());  //NOI18N
            connectionFailed = true;
        } catch (IOException exc) {
            displayText("Couldn't get I/O for the connection to " + errorHost + ", port " + errorPort + "; "   + exc.getLocalizedMessage());  //NOI18N
            connectionFailed = true;
        }
        
        if ( connectionFailed ) {
            connected = false ;
        }
        else {
            try{
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException exc) {
                displayText("Could not get the socket I/O" + exc.getLocalizedMessage()); //NOI18N
                connected = false; 
            }
        }
        
        while(connected){
            try{
                line = in.readLine();
                if(line != null){
                    displayText(line);
                    message.append(line + "\n"); //NOI18N
                }else{
                    connected = false;
                    displayText("Connection closed"); //NOI18N
                }
            } catch (IOException exc) {
                displayText(exc.getLocalizedMessage());
                connected = false;
            }
        }
    }
    
    protected boolean testConnected(){
        // Loop still a connection is established
        while (!connected){
            if(connectionFailed) return false;
            try {
                Thread.currentThread().sleep(250) ;
            }
            catch ( java.lang.InterruptedException iiee) {
                break ;
            }
        }
        return connected;
    }
    
    protected void disconnect(){
        if(connected){
            out.close();
            
            try {
                in.close();
            }
            catch(IOException exc) {
                // die quietly
            }
            try {    
                clientSocket.close();
            }catch (IOException exc) {
                displayText("Error occured while disconnecting socket" + exc.getLocalizedMessage()); //NOI18N
            }
            out = null;
            in = null;
            clientSocket = null;
            connected = false;
        }
    }
    
    protected void sendMessage(String text){
        if(out != null && testConnected()){
            out.println(text);
            out.flush();
        }
    }
    
    protected String getMessage(){
        String str = message.toString();
        message.delete(0,str.length()-1);
        return str;
    }
    
    private static void displayText(final String line){
        if ( ExceptionHandler.getDebugLevel() > 0 ) {
            System.out.println(line);
        }
    }
    
}
