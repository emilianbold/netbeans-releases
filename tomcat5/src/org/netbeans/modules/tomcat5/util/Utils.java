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

package org.netbeans.modules.tomcat5.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;

/**
 * Utility class.
 *
 * @author sherold
 */
public class Utils {
    
    /** Creates a new instance of Utils */
    private Utils() {
    }
    
    /** Return URL representation of the specified file. */
    public static URL fileToUrl(File file) throws MalformedURLException {
        URL url = file.toURI().toURL();
        if (FileUtil.isArchiveFile(url)) {
            url = FileUtil.getArchiveRoot(url);
        }
        return url;
    }
    
    /** Return string representation of the specified URL. */
    public static String urlToString(URL url) {
        if ("jar".equals(url.getProtocol())) { // NOI18N
            URL fileURL = FileUtil.getArchiveFile(url);
            if (FileUtil.getArchiveRoot(fileURL).equals(url)) {
                // really the root
                url = fileURL;
            } else {
                // some subdir, just show it as is
                return url.toExternalForm();
            }
        }
        if ("file".equals(url.getProtocol())) { // NOI18N
            File f = new File(URI.create(url.toExternalForm()));
            return f.getAbsolutePath();
        }
        else {
            return url.toExternalForm();
        }
    }
    
    /** Dump the specified output stream in the specified fileObject through the 
     document */
    public static void saveDoc(FileObject fileObject, final OutputStream out) {
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fileObject);
            if (dataObject != null) {
                EditorCookie editor = (EditorCookie)dataObject.getCookie(EditorCookie.class);
                final StyledDocument doc;
                doc = editor.openDocument();
                NbDocument.runAtomic(doc, new Runnable() {
                    public void run() {
                        try {
                            doc.remove(0, doc.getLength());
                            doc.insertString(0, out.toString(), null);
                        } catch (BadLocationException ble) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ble);
                        }
                    }
                });
                SaveCookie cookie = (SaveCookie)dataObject.getCookie(SaveCookie.class);
                cookie.save();
            } else {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Cannot find the data object."); // NOI18N
            }
        } catch (DataObjectNotFoundException donfe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, donfe);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
    }
    
    /** Return true if the specified port is free, false otherwise. */
    public static boolean isPortFree(int port) {
        ServerSocket soc = null;
        try {
            soc = new ServerSocket(port);
            return true;
        } catch (IOException ioe) {
            return false;
        } finally {
            if (soc != null) {
                try {
                    soc.close();
                } catch (IOException ex) { // no op
                }
            }
        }
    }
    
    /** Return true if a Tomcat server is running on the specifed port */
    public static boolean pingTomcat(int port, int timeout) {
        Pinger test = new Pinger(port);
        Thread t = new Thread(test);
        t.start();
        try {
            t.join(timeout);
        } catch(InterruptedException ie) {
        }
        if (t.isAlive()) {
            t.interrupt(); // for thread deadlock
        }
        return test.result();
    }
    
    private static class Pinger implements Runnable {
        private int port;
        private boolean result;

        public Pinger(int port) {
            this.port = port;
        }

        public void run() {
            // Issue #47048 - on some windowsXP boxes previous test, which
            // used Socket to determine whether Tomcat is running sometimes 
            // failed. The HttpURLConnection is used instead now. The 
            // requestURI is "/netbeans-tomcat-status-test" to make it 
            // possible for the http monitor to filter it out.            
            URL url = null;
            try {
                url = new URL("http://localhost:" + port  + "/netbeans-tomcat-status-test"); //NOI18N
            } catch(MalformedURLException e) {
                return;
            }
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection)url.openConnection();
                con.getResponseCode();
                // check whether it is really a Tomcat server
                Map headerFileds = con.getHeaderFields();
                List/*<String>*/ server = (List/*<String>*/)headerFileds.get("Server"); // NIO18N
                if (server != null) {
                    if (server.contains("Apache-Coyote/1.1")) { // NOI18N
                        if (headerFileds.get("X-Powered-By") == null) { // NIO18N
                            // if X-Powered-By header is set, it is probably jboss
                            result = true;
                            return;
                        }
                    } else if (server.contains("Sun-Java-System/Web-Services-Pack-1.4")) {  // NOI18N
                        // it is probably Tomcat with JWSDP installed
                        result = true;
                        return;
                    }
                }
                result = false;
            } catch (IOException ioe) { // no op
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }
        
        public final boolean result() {
            return result;
        }
    }
}
