/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.netbeans.modules.tomcat5.config.Context;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

/** Implemtation of management task that provides info about progress
 *
 * @author  Radim Kubacki
 */
class TomcatManagerImpl implements ProgressObject, Runnable {
    
    /** RequestProcessor processor that serializes management tasks. */
    private static RequestProcessor rp;
    
    /** Returns shared RequestProcessor. */
    private static synchronized RequestProcessor rp () {
        if (rp == null) {
            rp = new RequestProcessor ("Tomcat management", 1); // NOI18N
        }
        return rp;
    }
    
    /** List of ProgressListener s. */
    private List lsnrs = new ArrayList ();
    
    /** Command that is executed on running server. */
    private String command;
    
    /** InputStream of application data. */
    private InputStream istream;
    
    private TomcatManager tm;
    
    /** TargetModuleID of module that is managed. */
    private TomcatModule tmId;

    public TomcatManagerImpl (TomcatManager tm) {
        this.tm = tm;
    }

    public void deploy (Target t, InputStream is, InputStream deplPlan) {
        Context ctx = Context.createGraph (deplPlan);
        String ctxPath = ctx.getAttributeValue ("path");   // NOI18N
        tmId = new TomcatModule (t, ctxPath);
        
        command = "deploy?path="+ctxPath; // NOI18N
        istream = is;
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    /** Deploys WAR file or directory to Tomcat using deplPlan as source 
     * of conetx configuration data.
     */
    public void install (Target t, File wmfile, File deplPlan) {
        String path = deplPlan.getAbsolutePath ();
        String ctxPath = null;
        try {
            ctxPath = deplPlan.toURL ().toExternalForm ();
        }
        catch (java.net.MalformedURLException e) {
            ctxPath = "file:"+path; // NOI18N
        }
        String docBase = null;
        try {
            docBase = wmfile.toURL ().toExternalForm ();
            if (docBase.endsWith ("/")) { // NOI18N
                docBase = docBase.substring (0, docBase.length ()-1);
            }
        }
        catch (java.net.MalformedURLException e) {
            docBase = "file:"+wmfile.getAbsolutePath (); // NOI18N
        }
        if (wmfile.isFile ()) {
            // WAR file
            docBase = "jar:"+docBase+"!/"; // NOI18N
        }
        command = "install?context="+ctxPath+"&war="+docBase; // NOI18N
        
        try {
            FileInputStream in = new FileInputStream (deplPlan);
            Context ctx = Context.createGraph (in);
            tmId = new TomcatModule (t, ctx.getAttributeValue ("path")); // NOI18N
        }
        catch (java.io.FileNotFoundException fnfe) {
            throw new RuntimeException (fnfe);
        }
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    void remove (TomcatModule tmId) {
        this.tmId = tmId;
        command = "remove?path="+tmId.getPath (); // NOI18N
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    /** JSR88 method. */
    public ClientConfiguration getClientConfiguration (TargetModuleID targetModuleID) {
        return null; // PENDING
    }
    
    /** JSR88 method. */
    public DeploymentStatus getDeploymentStatus () {
        return null; // PENDING
    }
    
    /** JSR88 method. */
    public TargetModuleID[] getResultTargetModuleIDs () {
        return new TargetModuleID [] { tmId };
    }
    
    /** JSR88 method. */
    public boolean isCancelSupported () {
        return false;
    }
    
    /** JSR88 method. */
    public void cancel () 
    throws OperationUnsupportedException {
        throw new OperationUnsupportedException ("cancel not supported in Tomcat deployment"); // NOI18N
    }
    
    /** JSR88 method. */
    public boolean isStopSupported () {
        return false;
    }
    
    /** JSR88 method. */
    public void stop () throws OperationUnsupportedException {
        throw new OperationUnsupportedException ("stop not supported in Tomcat deployment"); // NOI18N
    }
    
    /** JSR88 method. */
    public void addProgressListener (ProgressListener l) {
        lsnrs.add (l);
    }
    
    /** JSR88 method. */
    public void removeProgressListener (ProgressListener l) {
        lsnrs.remove (l);
    }
    
    private void fireProgressEvent (ProgressEvent e) {
        Iterator it = lsnrs.iterator ();
        while (it.hasNext ()) {
            ProgressListener l = (ProgressListener)it.next ();
            l.handleProgressEvent (e);
        }
    }
    
    /** Executes one management task. */
    public synchronized void run () {
        TomcatFactory.getEM ().log(ErrorManager.INFORMATIONAL, command);
        System.out.println(tm.getUri () + command);
        fireProgressEvent (new ProgressEvent (this, tmId, null)); // PENDING
        
        // similar to Tomcat's Ant task
        URLConnection conn = null;
        InputStreamReader reader = null;
        
        try {

            // Create a connection for this command
            conn = (new URL(tm.getUri () + command)).openConnection();
            HttpURLConnection hconn = (HttpURLConnection) conn;

            // Set up standard connection characteristics
            hconn.setAllowUserInteraction(false);
            hconn.setDoInput(true);
            hconn.setUseCaches(false);
            if (istream != null) {
                hconn.setDoOutput(true);
                hconn.setRequestMethod("PUT");   // NOI18N
                hconn.setRequestProperty("Content-Type", "application/octet-stream");   // NOI18N
//                if (contentLength >= 0) {
//                    hconn.setRequestProperty("Content-Length",   // NOI18N
//                                             "" + contentLength);
//                }
            } else {
                hconn.setDoOutput(false);
                hconn.setRequestMethod("GET"); // NOI18N
            }
            hconn.setRequestProperty("User-Agent", // NOI18N
                                     "NetBeansIDE-Tomcat-Manager/1.0"); // NOI18N

            // Set up an authorization header with our credentials
            String input = tm.getUsername () + ":" + tm.getPassword ();
            String output = new String(Base64.encode(input.getBytes()));
            hconn.setRequestProperty("Authorization", // NOI18N
                                     "Basic " + output); // NOI18N

            // Establish the connection with the server
            hconn.connect();

            // Send the request data (if any)
            if (istream != null) {
                BufferedOutputStream ostream =
                    new BufferedOutputStream(hconn.getOutputStream(), 1024);
                byte buffer[] = new byte[1024];
                while (true) {
                    int n = istream.read(buffer);
                    if (n < 0) {
                        break;
                    }
                    ostream.write(buffer, 0, n);
                }
                ostream.flush();
                ostream.close();
                istream.close();
            }

            // Process the response message
            reader = new InputStreamReader(hconn.getInputStream());
            StringBuffer buff = new StringBuffer();
            String error = null;
            boolean first = true;
            while (true) {
                int ch = reader.read();
                if (ch < 0) {
                    break;
                } else if ((ch == '\r') || (ch == '\n')) {
                    String line = buff.toString();
                    buff.setLength(0);
                    // PENDING : fireProgressEvent
                    TomcatFactory.getEM ().log(ErrorManager.INFORMATIONAL, line);
                    if (first) {
                        if (!line.startsWith("OK -")) { // NOI18N
                            error = line;
                        }
                        first = false;
                    }
                } else {
                    buff.append((char) ch);
                }
            }
            if (buff.length() > 0) {
                TomcatFactory.getEM ().log(ErrorManager.INFORMATIONAL, buff.toString());
            }
            if (error != null) {
                throw new Exception(error);
            }

        } catch (Exception e) {
// PENDING report error
            e.printStackTrace ();
            // throw t;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable u) {
                    ;
                }
                reader = null;
            }
            if (istream != null) {
                try {
                    istream.close();
                } catch (Throwable u) {
                    ;
                }
                istream = null;
            }
        }

    }
    
}
