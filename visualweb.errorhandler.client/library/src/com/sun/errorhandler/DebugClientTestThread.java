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
