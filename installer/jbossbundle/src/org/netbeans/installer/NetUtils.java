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

package org.netbeans.installer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class NetUtils {
    
    /** Creates a new instance of NetUtils */
    public NetUtils() {
    }

    public static InetAddress getHostAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch(Exception e) {
            // System.out.println("Ignore this exception, could not get address for this host");
            // e.printStackTrace();
            return null;
        }
    }

    public static String getHostName() {
        InetAddress addr = getHostAddress();
        return (addr != null ? addr.getHostName() : null);
    }

    public static InetAddress getLocalHostAddress() {
        InetAddress localAddr = null;
        try {
            localAddr = InetAddress.getByName(null);
            if (!localAddr.isLoopbackAddress()) {
                localAddr = null;
            }
        } catch (Throwable t) {
            // Catch all exceptions and return null to the caller
			// System.out.println("Ignore following exception: could not find localhost");
	// 		t.printStackTrace();
            localAddr = null;
        }
        return localAddr;
    }

    public static String getLocalHost() {
        InetAddress localAddr = getLocalHostAddress();
        return (localAddr != null ? localAddr.getHostName() : "localhost");
    }

    public static boolean isPortFreeClient(int port) {
        return isPortFreeClient(getHostName(), port);
    }

    public static boolean isPortFreeClient(String hostName, int port) {
        if (!isPortInValidRange(port)) {
            return false;
        }
        try {
            Socket socket = new Socket(hostName, port);
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            os.close();
            os = null;
            is.close();
            is = null;
            socket.close();
            socket = null;
        } catch (Exception e) {
            // Nobody is listening on this port
			// System.out.println("Ignore following exception: Will assume " + hostName + "/" + String.valueOf(port) + " is not in use");
          //   e.printStackTrace();
            return true;
        }
        return false;
    }

    public static boolean isPortFreeServer(int port) {
        return isPortFreeServer(null, port);
    }

    public static boolean isPortFreeServer(InetAddress addr, int port) {
        if (!isPortInValidRange(port)) {
            return false;
        }
        boolean isPortFree = true;
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(port, 0, addr);
        } catch (Exception e) {
            // Indicates port is in use or can not be used (for example <1024
            // for non-root user)
            // System.out.println("Ignore this exception, assuming " + addr + "/" + String.valueOf(port) + " is not free.");
          //   e.printStackTrace();
            isPortFree = false;
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (Exception e) {
                    // Error in closing the server socket. Port may be unusable
                    // System.out.println("Ignore this exception, could not close socket, assuming " + addr.toString() + "/" + String.valueOf(port) + " is not free.");
                    e.printStackTrace();
                    isPortFree = false;
                }
            }
        }
        return isPortFree;
    }

    private static boolean isPortInValidRange(int port) {
        if (port <= 0 && port > 65535) {
            return false;
        }
        return true;
    }

    public static int convertToInt(String str) {
        int port = -1;
        try {
            port = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            // The string specified is not an integer
        }
        return port;
    }


	static public boolean ValidatePort(String portNumber)
	{
    	String serverName = getHostName();
    	int intportNumber=0;
/**	if portnumber does not match the range then return false **/
    	if(portNumber == null || portNumber.length() == 0 || 
		portNumber.length() > 5)
	  return false;
	try
      	 {
            intportNumber = Integer.parseInt(portNumber);
      	 }
	catch (NumberFormatException dummy)
      	 {

      	 }
 
/**	if portnumber does not match the range then return false **/
	if(intportNumber <= 0 || intportNumber > 65535) return false;
	
        // use server socket method on Windows, client socket otherwise
        String os = System.getProperty("os.name");
	// if (com.sun.enterprise.installer.utilities.ValidateOS.OSName.indexOf("Windows")!=-1) 
        if (!os.startsWith("Windows")) {
      	    return isPortFreeServer(null, intportNumber);     
	} else {
	    return isPortFreeClient(serverName, intportNumber);
	}
	
	}


    // If return -1 for invalidPort 
    static public int findValidPort (String portNumber, int numOfTrials) {
    	String serverName = getHostName();
    	int validPortNumber=-1;
        int startPortNumber = -1;
        int MAX_TRIALS = 65535;
 
        if (numOfTrials < 0 || numOfTrials > MAX_TRIALS) {
           numOfTrials = MAX_TRIALS;
        }

    	if(portNumber == null || portNumber.length() == 0 || 
		portNumber.length() > 5)
	  return validPortNumber; // Return the invalid value here
	try
      	 {
            startPortNumber = Integer.parseInt(portNumber);
      	 }
	catch (NumberFormatException dummy)
      	 {

      	 }
        int endPortNumber = startPortNumber + numOfTrials;

        // use server socket method on Windows, client socket otherwise
        String os = System.getProperty("os.name");

        for (int i = startPortNumber; i < endPortNumber && i < MAX_TRIALS && i > 0 ; i++) {
            if (!os.startsWith("Windows")) {
          	    if (isPortFreeServer(null, i)) {
                        validPortNumber = i; 
                        break;
                    }
    	    } else {
    	        if (isPortFreeClient(serverName, i)) {
                        validPortNumber = i;
                        break;
                    }
    	    }
        }
        return validPortNumber;
	
    }


    public static void testPort(int port) {
        System.out.println("Method 1 Client socket: Port is free? " + isPortFreeClient(port));
        System.out.println("Method 2 Client socket on localhost: Port is free? " + isPortFreeClient(getLocalHost(), port));
        System.out.println("Method 3 Server socket: Port is free? " + isPortFreeServer(port));
        System.out.println("Method 4 Server socket on localhost: Port is free? " + isPortFreeServer(getLocalHostAddress(), port));
    }

/*
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            int port = convertToInt(args[i]);
            if (port != -1) {
                System.out.println("Now testing " + port);
                System.out.println("findValidPort() returns " + findValidPort(args[i], -1));
                // testPort(port);
                if (ValidatePort(args[i])) {
                    System.out.println("This port " + args[i] + " is unused");
                } else {
                    System.out.println("This port " + args[i] + " is not available");
                }
            } else {
                System.out.println("Ignoring parameter " + args[i]);
            }
        }
    }
*/

}
