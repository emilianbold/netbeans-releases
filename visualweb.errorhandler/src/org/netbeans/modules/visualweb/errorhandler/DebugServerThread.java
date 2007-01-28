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

package org.netbeans.modules.visualweb.errorhandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;
import org.openide.modules.InstalledFileLocator;

/*
 * DebugServerThread.java
 * Created on January 6, 2004, 1:05 PM
 */

public class DebugServerThread extends Thread {
    StringBuffer message = new StringBuffer();

    boolean connected = false;
    boolean stopServer = false;
    Socket clientSocket = null;
    private static final String installPropsThresher = "config/com-sun-rave-install.properties"; //NOI18N

    Date date = new Date();

    ThreadGroup clientSocketThreads = new  ThreadGroup("Debug Client Threads");

    public DebugServerThread() {
    }

    public void run() {
        Thread.currentThread().setName("Creator Error Handler Listener") ; // NOI18N
        ServerSocket server = null;

        String line;

        // get the properties written by the installer.
        // first look in the new thresher location.  If not found, check the old location.

        File installPropsFile = InstalledFileLocator.getDefault().locate( installPropsThresher ,null,false ) ;

        int portNum = 24444 ; // default port number.

        // get the port number to use out of install.properties
        if( installPropsFile != null ){
            try{
                Properties installProps = new Properties();
                installProps.load(new FileInputStream(installPropsFile));
                String daPort = installProps.getProperty("errorhandlerPort "); //NOI18N
                if(daPort != null) {
                    // parse for Int
                    try {
                        portNum = Integer.parseInt(daPort ) ;
                    } catch ( java.lang.NumberFormatException wwww ) {
                        System.err.println("NumberFormantException with errorhandler portNum of " + daPort  );
                        wwww.printStackTrace() ;
                    }
                }
            } catch (IOException exc){
                exc.printStackTrace();
            }
        }

        try{
            server = new ServerSocket(portNum);
        } catch (IOException exc) {
            displayText("Could not listen on port"   + portNum + " " + exc.getLocalizedMessage() ) ;
            stopServer = true ;
        }

        while(!stopServer){
            try{
                if (server != null) {
                    clientSocket = server.accept();
                    //displayText("Error Handler Server: Accepted a connection\n");
                    Thread socketThread = new ClientSocketConnectionThread(clientSocketThreads,"connection" + date.getTime(), clientSocket);
                    socketThread.setPriority(Thread.MIN_PRIORITY);
                    socketThread.start();
                } else {
                    Thread.sleep(1500);
                }
            } catch (IOException exc) {
                displayText("Accept failed: port " + portNum + " " + exc.getLocalizedMessage()); // NOI18N
            } catch (InterruptedException exc) {
            }
        }
    }

    public void stopServer(){
        if (clientSocketThreads.activeCount() > 0){
            Thread[] socketThreads = new Thread[clientSocketThreads.activeCount()];
            clientSocketThreads.enumerate(socketThreads);
            for(int i=0; i< socketThreads.length; i++){
                ((ClientSocketConnectionThread)socketThreads[i]).disconnect();
                socketThreads[i].interrupt();
            }
        }
        stopServer = true;
    }

    public void sendMessage(String text){
        if (clientSocketThreads.activeCount() > 0){
            Thread[] socketThreads = new Thread[clientSocketThreads.activeCount()];
            clientSocketThreads.enumerate(socketThreads);
            for(int i=0; i< socketThreads.length; i++){
                ((ClientSocketConnectionThread)socketThreads[i]).sendMessage(text);
            }
        }
    }
    
    private void displayText(final String line){
        //DebugServerTestWindow.displayMessage(line);
        System.out.println(line);
    }
    
    public static void main(String args[]) {
        DebugServerThread serverThread = new DebugServerThread();
        serverThread.start();
    }
}
