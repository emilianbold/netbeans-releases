/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.installer.mac.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author dlm198383
 */
public class GetAvailablePort {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length>0) {
            if (args[0].equalsIgnoreCase("getHostName")) {
                System.out.println("" + getHostName());
            } else {
                System.out.println("" + getAvailablePort(new Integer(args[0]).intValue()));
            }
        }
    }
    public static boolean isPortAvailable(int port) {        
        // if the port is not in the allowed range - return false
        if ((port < 0) && (port > 65535)) {
            return false;
        }
        
        // if the port is not in the restricted list, we'll try to open a server
        // socket on it, if we fail, then someone is already listening on this port
        // and it is occupied
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(port);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    
                }
            }
        }
    }
    
    public static int getAvailablePort(int basePort) {
        // increment the port value until we find an available port or stumble into
        // the upper bound
        int port = basePort;
        while ((port < 65535) && !isPortAvailable(port)) {
            port++;
        }
        
        if (port == 65535) {
            port = 0;
            while ((port < basePort) && !isPortAvailable(port)) {
                port++;
            }
            
            if (port == basePort) {
                return -1;
            } else {
                return port;
            }
        } else {
            return port;
        }
    }

    public static String getHostName() {
        try{
            String hostName=InetAddress.getLocalHost().getHostName();
            if(hostName != null) {
                return hostName;
            }
        } catch (UnknownHostException e) {
            System.out.println("getHostName: " + e.getStackTrace());
        }
        return "localhost";
    }

}
