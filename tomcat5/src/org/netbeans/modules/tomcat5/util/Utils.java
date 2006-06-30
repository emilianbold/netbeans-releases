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

package org.netbeans.modules.tomcat5.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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
        // checking whether a socket can be created is not reliable enough, see #47048
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("localhost", port), timeout); // NOI18N
            socket.setSoTimeout(timeout);
            
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // request
            out.println("GET /netbeans-tomcat-status-test HTTP/1.1\n"); // NOI18N
            
            // response
            String text = in.readLine();
            if (text == null || !text.startsWith("HTTP/")) { // NOI18N
                return false; // not an http response
            }
            Map headerFileds = new HashMap();
            while ((text = in.readLine()) != null && text.length() > 0) {
                int colon = text.indexOf(':');
                if (colon <= 0) {
                    return false; // not an http header
                }
                String name = text.substring(0, colon).trim();
                String value = text.substring(colon + 1).trim();
                List list = (List)headerFileds.get(name);
                if (list == null) {
                    list = new ArrayList();
                    headerFileds.put(name, list);
                }
                list.add(value);
            }
            
            List/*<String>*/ server = (List/*<String>*/)headerFileds.get("Server"); // NIO18N
            if (server != null) {
                if (server.contains("Apache-Coyote/1.1")) { // NOI18N
                    if (headerFileds.get("X-Powered-By") == null) { // NIO18N
                        // if X-Powered-By header is set, it is probably jboss
                        return true;
                    }
                } else if (server.contains("Sun-Java-System/Web-Services-Pack-1.4")) {  // NOI18N
                    // it is probably Tomcat with JWSDP installed
                    return true;
                }
            }
            return false;
        } catch (IOException ioe) {
            return false;
        } finally {
            if (in != null) {
                try { in.close(); } catch (IOException ioe) { } // no op
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                try { socket.close(); } catch (IOException ioe) { } // no op
            }
        }
    }
}
