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
