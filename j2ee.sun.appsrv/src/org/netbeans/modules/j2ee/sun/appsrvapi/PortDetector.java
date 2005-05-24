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
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException ;


public class PortDetector {

    /**
     *	This method accepts a hostname and port #.  It uses this information
     *	to attempt to connect to the port, send a test query, analyze the
     *	result to determine if the port is secure or unsecure (currently only
     *	http / https is supported).
     */
    public static boolean isSecurePort(String hostname, int port) throws IOException, ConnectException, SocketTimeoutException {
	// Open the socket w/ a 4 second timeout
	Socket socket = new Socket();
	socket.connect(new InetSocketAddress(hostname, port), 4000);

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
	    isSecure = false;
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
