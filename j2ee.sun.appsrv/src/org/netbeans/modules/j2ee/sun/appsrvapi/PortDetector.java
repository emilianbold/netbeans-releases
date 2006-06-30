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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.appsrvapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
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
    public static boolean isSecurePortGlassFish(String host, int port) throws ConnectException {
        boolean isSecure = false;
        
        int i =0;
        
        try {
            Socket socket = new Socket(host,port);
            socket.setSoTimeout(5000); // 5 seconds
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
            //      ex.printStackTrace();
            throw ex; //Status is unknown
        } catch (SocketTimeoutException ex){
           // ex.printStackTrace();
             ConnectException ce = new ConnectException();
            ce.initCause(ex);
             throw ce; //status unknow at this point
        } catch (SocketException ex){
           // ex.printStackTrace();
            if (ex.getMessage().indexOf("broken pipe") != -1){ //NOI18N
                isSecure = true;
            }
        } catch (IOException ex) {
           // ex.printStackTrace();
            if (ex.getMessage().indexOf("end of file") != -1){ //NOI18N
                isSecure = true;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            ConnectException ce = new ConnectException();
            ce.initCause(ex);
             throw ce; //status unknow at this point
        }
       // System.out.println("is secure"+isSecure);
        return isSecure;
    }
    
    /**
     *	This method accepts a hostname and port #.  It uses this information
     *	to attempt to connect to the port, send a test query, analyze the
     *	result to determine if the port is secure or unsecure (currently only
     *	http / https is supported).
     * it might emit a warning in the server log for GlassFish cases
     * No Harm, just an annoying warning, so we need to use this call only when really needed
     */
    public static boolean isSecurePort(String hostname, int port) throws IOException, ConnectException, SocketTimeoutException {
	// Open the socket w/ a 4 second timeout
	Socket socket = new Socket();
        try{
            socket.connect(new InetSocketAddress(hostname, port), 4000);
        } catch (SocketException ex){// this could be bug 70020 due ot SOCKs proxy not having locahost
            String socksNonProxyHosts=System.getProperty("socksNonProxyHosts");
            if ((socksNonProxyHosts!=null) && (socksNonProxyHosts.indexOf("localhost")<0)) {
                String localhost;
                if (socksNonProxyHosts.length()>0) localhost="|localhost"; else localhost="localhost";
                System.setProperty("socksNonProxyHosts",  socksNonProxyHosts+localhost);
                ConnectException ce = new ConnectException();
                ce.initCause(ex);
                throw ce; //status unknow at this point
                //next call, we'll be ok and it will really detect if we are secure or not
            }

            
        }

	// Send an https query (w/ trailing http query)
	java.io.OutputStream ostream = socket.getOutputStream();
	ostream.write(TEST_QUERY);

	// Get the result
	java.io.InputStream istream = socket.getInputStream();
	int count=0;
	while (count<20) {
	    // Wait up to 4 seconds
	    try {
		if (istream.available() > 0) {
		    break;
		}
		Thread.sleep(200);
	    } catch (InterruptedException ex) {
	    }
	    count++;
	}

	byte[] input = new byte[istream.available()];
	int len = istream.read(input);

	// Close the socket
	socket.close();

	// Determine protocol from result
	// Can't read https response w/ OpenSSL (or equiv), so use as
	// default & try to detect an http response.
	String response = new String(input).toLowerCase();
	boolean isSecure = true;
	if (response.length() == 0) {
	    //isSecure = false; 
            throw new ConnectException();
	} else if (response.startsWith("http/1.")) {
	    isSecure = false;
	} else if (response.indexOf("<html") != -1) {
	    isSecure = false;
	} else if (response.indexOf("connection: ") != -1) {
	    isSecure = false;
	}
	return isSecure;
    }




    /**
     *	This is the test query used to ping the server in an attempt to
     *	determine if it is secure or not.
     */
    public static byte [] TEST_QUERY = new byte [] {
	// The following SSL query is from nmap (http://www.insecure.org)
	// This HTTPS request should work for most (all?) https servers
	(byte)0x16, (byte)0x03, (byte)0x00, (byte)0x00, (byte) 'S', (byte)0x01,
	(byte)0x00, (byte)0x00, (byte) 'O', (byte)0x03, (byte)0x00, (byte) '?',
	(byte) 'G', (byte)0xd7, (byte)0xf7, (byte)0xba, (byte) ',', (byte)0xee,
	(byte)0xea, (byte)0xb2, (byte) '`', (byte) '~', (byte)0xf3, (byte)0x00,
	(byte)0xfd, (byte)0x82, (byte) '{', (byte)0xb9, (byte)0xd5, (byte)0x96,
	(byte)0xc8, (byte) 'w', (byte)0x9b, (byte)0xe6, (byte)0xc4, (byte)0xdb,
	(byte) '<', (byte) '=', (byte)0xdb, (byte) 'o', (byte)0xef, (byte)0x10,
	(byte) 'n', (byte)0x00, (byte)0x00, (byte) '(', (byte)0x00, (byte)0x16,
	(byte)0x00, (byte)0x13, (byte)0x00, (byte)0x0a, (byte)0x00, (byte) 'f',
	(byte)0x00, (byte)0x05, (byte)0x00, (byte)0x04, (byte)0x00, (byte) 'e',
	(byte)0x00, (byte) 'd', (byte)0x00, (byte) 'c', (byte)0x00, (byte) 'b',
	(byte)0x00, (byte) 'a', (byte)0x00, (byte) '`', (byte)0x00, (byte)0x15,
	(byte)0x00, (byte)0x12, (byte)0x00, (byte)0x09, (byte)0x00, (byte)0x14,
	(byte)0x00, (byte)0x11, (byte)0x00, (byte)0x08, (byte)0x00, (byte)0x06,
	(byte)0x00, (byte)0x03, (byte)0x01, (byte)0x00,
	// The following is a HTTP request, some HTTP servers won't
	// respond unless the following is also sent
	(byte) 'G', (byte) 'E', (byte) 'T', (byte) ' ', (byte) '/', (byte) ' ',
	(byte) 'H', (byte) 'T', (byte) 'T', (byte) 'P', (byte) '/', (byte) '1',
	(byte) '.', (byte) '0', (byte)'\n', (byte)'\n'
    };    
}
