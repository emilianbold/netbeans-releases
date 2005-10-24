/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.appsrvapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;


/* new algo to test of an app server(8.1 and 9.0) is secured or not
 * @author ludo champenois, and Jean Francois Arcand
 *
 **/

public class PortDetector {
    
    /**
     *	This method accepts a hostname and port #.  It uses this information
     *	to attempt to connect to the port, send a test query, analyze the
     *	result to determine if the port is secure or unsecure (currently only
     *	http / https is supported).
     */
    public static boolean isSecurePort(String host, int port) {
        boolean isSecure = false;
        
        int i =0;
        
        try {
            Socket socket = new Socket(host,port);
            socket.setSoTimeout(10000); // 10 seconds
            OutputStream os = socket.getOutputStream();
            os.write("GET / HTTP/1.1\n".getBytes()); //NOI18N
            os.write( ("host: " + host + "\n").getBytes() ); //NOI18N
            os.write("\n".getBytes()); //NOI18N
            
            InputStream is = socket.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));
            
            boolean found = false;
            String line = null;
            while ((line = bis.readLine()) != null) {
                i++;
                if ( i > 5 ) break; //we have the header
            }
            
            // We are reading the certificate.
            if ( i == 1 ){
                isSecure = true;
            }
        } catch (ConnectException ex){
           // ex.printStackTrace();
                isSecure = false;
        } catch (SocketTimeoutException ex){
          //  ex.printStackTrace();
            if (ex.getMessage().indexOf("Read") != -1 && i == 0){ //NOI18N
                isSecure = true;
            }
        } catch (SocketException ex){
          //  ex.printStackTrace();
            if (ex.getMessage().indexOf("broken pipe") != -1){ //NOI18N
                isSecure = true;
            }
            isSecure = true;
        } catch (IOException ex) {
          //  ex.printStackTrace();
            if (ex.getMessage().indexOf("end of file") != -1){ //NOI18N
                isSecure = true;
            }
        } catch (Throwable ex) {
           // ex.printStackTrace();
        }
       // System.out.println("is secure"+isSecure);
        return isSecure;
    }
    
    
}
