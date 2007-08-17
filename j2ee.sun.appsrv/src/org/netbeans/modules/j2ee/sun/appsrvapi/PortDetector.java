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
     *  This method accepts a hostname and port #.  It uses this information
     *  to attempt to connect to the port, send a test query, analyze the
     *  result to determine if the port is secure or unsecure (currently only
     *  http / https is supported).
     * it might emit a warning in the server log for GlassFish cases
     * No Harm, just an annoying warning, so we need to use this call only when really needed
     */
    public static boolean isSecurePort(String hostname, int port) throws IOException, ConnectException, SocketTimeoutException {
        // Open the socket w/ a 4 second timeout
        Socket socket = new Socket();
        try{
            socket.connect(new InetSocketAddress(hostname, port),4000);
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
        int byteRead = 0;
        byte[] input = new byte[8192];
        istream.read(input);
        
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
        } else if (response.indexOf("</html") != -1) {
            // New test added to resolve 106245
            // when the user has the IDE use a proxy (like webcache.foo.bar.com),
            // the response comes back as "d><title>....</html>".  It looks like
            // something eats the "<html><hea" off the front of the data that 
            // gets returned.
            //
            // This test makes an allowance for that behavior. I figure testing
            // the likely "last bit" is better than testing a bit that is close
            // to the data that seems to get eaten.
            //
            isSecure = false;
        } else if (response.indexOf("connection: ") != -1) {
            isSecure = false;
        }
        return isSecure;
    }
    
    
    public static void main(String[] args) throws IOException{
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        System.out.println("host: " + " port: " + port);
        System.out.println("isSecure: " + isSecurePort(host,port));
    }
    
    
    /**
     *  This is the test query used to ping the server in an attempt to
     *  determine if it is secure or not.
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
        (byte) 'G', (byte) 'E', (byte) 'T', (byte) ' ', (byte) '/', 
        // change the detector to request something that the monitor knows to filter
        //  out.  This will work-around 109891. Use the longest filtered prefix to 
        //  avoid false positives....
        (byte) 'c', (byte) 'o', (byte) 'm', (byte) '_', (byte) 's', (byte) 'u', 
        (byte) 'n', (byte) '_', (byte) 'w', (byte) 'e', (byte) 'b', (byte) '_',
        (byte) 'u', (byte) 'i',
        (byte) ' ',
        (byte) 'H', (byte) 'T', (byte) 'T', (byte) 'P', (byte) '/', (byte) '1',
        (byte) '.', (byte) '0', (byte)'\n', (byte)'\n'
    };
}

