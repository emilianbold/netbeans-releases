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

package org.netbeans.modules.j2ee.weblogic9;

import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import org.openide.ErrorManager;

/** Utility class
* @author  Petr Jiricka, Pavel Buzek
*/
public class URLWait {

    /** Will wait until the URL is accessible and returns a valid resource
     * (response code other then 4xx or 5xx) or the timeout is reached.
     *
     * @return true if non error response was obtained
     */
    public static boolean waitForUrlReady(URL url, int timeout) {
        boolean success = false;
        String host = url.getHost();
        int port = url.getPort();
        try {
            InetAddress.getByName(host);
        }
        catch (UnknownHostException e) {
            return false;
        }
        return waitForURLConnection(url, timeout, 100);
    }
    
    private static boolean waitForURLConnection(URL url, int timeout, int retryTime) { 
        Connect connect = new Connect(url, retryTime); 
        Thread t = new Thread(connect);
        t.start();
        try {
            t.join(timeout);
        } catch(InterruptedException ie) {
        }
        if (t.isAlive()) {
            connect.finishLoop();
            t.interrupt();//for thread deadlock
        }
        return connect.getStatus();
    }

    private static class Connect implements Runnable  {
        private String host;
        private URL url;
        private int retryTime;
        private boolean status = false;
        private volatile boolean loop = true;        

        public Connect(URL url, int retryTime) {
            this.url = url;
            this.retryTime = retryTime; 
            host = url.getHost();
        } 

        public void finishLoop() {
            loop = false;
        }

        public void run() {
            try {
                InetAddress.getByName(host);
            } catch (UnknownHostException e) {
                return;
            }
            HttpURLConnection con = null;
            while (loop) {
                try {
                    con = (HttpURLConnection)url.openConnection();
                    int code = con.getResponseCode();
                    boolean error = (code == -1) || (code > 399 && code <600);
                    if (!error) {
                        status = true;
                        return;
                    }
                } catch (IOException ioe) {//nothing to do
                } finally {
                    if (con != null) con.disconnect();
                }
                try {
                    Thread.currentThread().sleep(retryTime);
                } catch(InterruptedException ie) {
                }
            }
        }

        boolean getStatus() {
            return status;
        }
    }
}
