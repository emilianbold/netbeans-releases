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

package org.netbeans.modules.javahelp;

import java.io.*;
import java.net.*;
import java.util.*;

import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Handler & connection cribbed from NbResourceStreamHandler.
 * @author Jesse Glick
 */
final class NbDocsStreamHandler extends URLStreamHandler {
    
    public static final class Factory implements URLStreamHandlerFactory {
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if (protocol.equals("nbdocs")) { // NOI18N
                return new NbDocsStreamHandler();
            } else {
                return null;
            }
        }
    }
    
    /** Make a URLConnection for nbdocs: URLs.
     * @param u the URL
     * @throws IOException if the wrong protocol
     * @return the connection
     */
    protected URLConnection openConnection(URL u) throws IOException {
        if (u.getProtocol().equals("nbdocs")) { // NOI18N
            return new NbDocsURLConnection(u);
        } else {
            throw new IOException("mismatched protocol"); // NOI18N
        }
    }
    
    /** A URL connection that reads from the docs classloader.
     */
    private static final class NbDocsURLConnection extends URLConnection {
        
        /** underlying URL connection
         */
        private URLConnection real = null;
        
        /** any associated exception while handling
         */
        private IOException exception = null;
        
        /** Make the connection.
         * @param u URL to connect to
         */
        public NbDocsURLConnection(URL u) {
            super(u);
        }
        
        /** Connect to the URL.
         * Actually look up and open the underlying connection.
         * @throws IOException for the usual reasons
         */
        public synchronized void connect() throws IOException {
            if (exception != null) {
                IOException e = exception;
                exception = null;
                throw e;
            }
            if (! connected) {
                String resource = url.getFile();
                if (resource.startsWith("/")) resource = resource.substring(1); //NOI18N
                URL target;
                String ext, basename;
                int index = resource.lastIndexOf('.');
                if (index != -1 && index > resource.lastIndexOf('/')) {
                    ext = resource.substring(index + 1);
                    basename = resource.substring(0, index).replace('/', '.');
                } else {
                    ext = null;
                    basename = resource.replace('/', '.');
                }
                try {
                    target = NbBundle.getLocalizedFile(basename, ext);
                } catch (MissingResourceException mre) {
                    // OK, try file.
                    File f = InstalledFileLocator.getDefault().locate("docs/" + resource, null, true); // NOI18N
                    if (f != null) {
                        target = Utilities.toURL(f);
                    } else {
                        IOException ioe = new IOException("cannot connect to " + url + ": " + mre);
                        Installer.err.annotate(ioe, NbBundle.getMessage(NbDocsStreamHandler.class, "EXC_nbdocs_cannot_connect", url));
                        Installer.err.annotate(ioe, mre);
                        throw ioe;
                    }
                }
                //System.err.println("loading from " + target);
                real = target.openConnection();
                real.connect();
                connected = true;
            }
        }
        
        /** Maybe connect, if not keep track of the problem.
         */
        private void tryToConnect() {
            if (connected || exception != null) return;
            try {
                connect();
            } catch (IOException ioe) {
                exception = ioe;
            }
        }
        
        /** Get a URL header.
         * @param n index of the header
         * @return the header value
         */
        public String getHeaderField(int n) {
            tryToConnect();
            if (connected)
                return real.getHeaderField(n);
            else
                return null;
        }
        
        /** Get the name of a header.
         * @param n the index
         * @return the header name
         */
        public String getHeaderFieldKey(int n) {
            tryToConnect();
            if (connected)
                return real.getHeaderFieldKey(n);
            else
                return null;
        }
        
        /** Get a header by name.
         * @param key the header name
         * @return the value
         */
        public String getHeaderField(String key) {
            tryToConnect();
            if (connected)
                return real.getHeaderField(key);
            else
                return null;
        }
        
        /** Get an input stream on the connection.
         * @throws IOException for the usual reasons
         * @return a stream to the object
         */
        public InputStream getInputStream() throws IOException {
            connect();
            return real.getInputStream();
        }
        
        /** Get an output stream on the object.
         * @throws IOException for the usual reasons
         * @return an output stream writing to it
         */
        public OutputStream getOutputStream() throws IOException {
            connect();
            return real.getOutputStream();
        }
        
        /** Get the type of the content.
         * @return the MIME type
         */
        public String getContentType() {
            tryToConnect();
            if (connected)
                return real.getContentType();
            else
                return "application/octet-stream"; // NOI18N
        }
        
        /** Get the length of content.
         * @return the length in bytes
         */
        public int getContentLength() {
            tryToConnect();
            if (connected)
                return real.getContentLength();
            else
                return 0;
        }
        
    }
    
}

