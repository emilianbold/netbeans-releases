/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
