/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * NetUtils.java
 *
 * Created on September 24, 2002, 2:46 PM
 */

package org.netbeans.xtest.util;

import java.net.*;

/**
 *
 * @author  mb115822
 */
public class NetUtils {
    
    /** private constructor - this class contains only static methods */
    private NetUtils() {
    }
    
    
    /** returns localhost IP address - if it does not exist,
     * return 127.0.0.1 as the IP.
     */
    public static String getLocalhostIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException uhe) {
            // infotmation was not found
            return "127.0.0.1";
        }
    }
    
    /** returns localhost name - without domain */
    public static String getLocalHostName() {
        try {
            String localHostName = InetAddress.getLocalHost().getCanonicalHostName();
            // now cut the name to the first dot
            int dotPosition = localHostName.indexOf('.');
            if (dotPosition == -1) {
                return localHostName;
            } else {
                // do the cut
                return localHostName.substring(0,dotPosition);
            }
        } catch (UnknownHostException uhe) {
            // infotmation was not found
            return "localhost";
        }
    }
    
    /** is network configured on this machine ?
     */
    public static boolean isNetConfigured() {
        try {
            String result = InetAddress.getLocalHost().getHostAddress();
            // test passed
            return true;
        } catch (UnknownHostException uhe) {
            // test failed
            return false;
        }
    }
    
}
