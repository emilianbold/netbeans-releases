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

package org.netbeans.modules.javahelp;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.*;
import java.util.*;

import org.openide.ErrorManager;
import org.openide.TopManager;
import org.openide.execution.NbfsStreamHandlerFactory;
import org.openide.util.NbBundle;

/** Handler & connection cribbed from NbResourceStreamHandler.
 * @author Jesse Glick
 */
final class NbDocsStreamHandler extends URLStreamHandler {
    
    static {
        // XXX #13529: register this from layer when possible...
        Installer.err.log("Registering nbdocs: protocol");
        NbfsStreamHandlerFactory.getDefault().register("nbdocs", new NbDocsStreamHandler()); // NOI18N
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
    
    /** weak ref to the special documentation classloader
     * used to handle nbdocs: requests
     */    
    private static Reference docsLoader = null; // Reference<ClassLoader>

    /** @return the classloader used to resolve
     * nbdocs: requests
     */
    private static ClassLoader getDocsLoader() {
        Reference r = docsLoader;
        ClassLoader l;
        if (r != null) {
            l = (ClassLoader) r.get();
        } else {
            l = null;
        }
        if (l == null) {
            l = new URLClassLoader(getDocsURLs(), TopManager.getDefault().systemClassLoader());
            if (! addedTmSysLoaderListener) {
                TopManager.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent ev) {
                        // XXX this is not an official property name...
                        if ("systemClassLoader".equals(ev.getPropertyName())) { // NOI18N
                            docsLoader = null;
                        }
                    }
                });
                addedTmSysLoaderListener = true;
            }
            docsLoader = new WeakReference(l);
        }
        return l;
    }
    private static boolean addedTmSysLoaderListener = false;

    /** extra URLs permitting documentation to be unpacked
     * into the docs/ folder of the IDE home or user dir
     */    
    private static URL[] docsURLs = null;
    /** Get the URLs needed for unpacked docs.
     * @return any applicable URLs (resource roots)
     */    
    private static URL[] getDocsURLs() {
        if (docsURLs == null) {
            List urls = new ArrayList(2);
            String nbhome = System.getProperty("netbeans.home");
            if (nbhome != null) {
                try {
                    // Java bug: if you do not canonicalize, it will make an absolute path,
                    // however file:/d:/nbdir/./docs/ is not treated as a valid URL for the
                    // URLClassLoader, for some reason.
                    URL url = new File(nbhome, "docs").getCanonicalFile().toURL(); // NOI18N
                    if (!url.toString().endsWith("/")) { // NOI18N
                        url = new URL(url.toString() + "/"); // NOI18N
                    }
                    urls.add(url);
                } catch (Exception e) {
                    Installer.err.notify(ErrorManager.WARNING, e);
                }
            }
            String nbuser = System.getProperty("netbeans.user");
            if (nbuser != null && !nbuser.equals(nbhome)) {
                try {
                    URL url = new File(nbuser, "docs").getCanonicalFile().toURL(); // NOI18N
                    if (!url.toString().endsWith("/")) { // NOI18N
                        url = new URL(url.toString() + "/"); // NOI18N
                    }
                    urls.add(url);
                } catch (Exception e) {
                    Installer.err.notify(ErrorManager.WARNING, e);
                }
            }
            Collections.reverse(urls); // nbuser docs should take precedence
            docsURLs = (URL[])urls.toArray(new URL[urls.size()]);
        }
        return docsURLs;
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
                // [PENDING] could probably be simplified (no need for a special ClassLoader at all)
                // if only NbBundle had a static method to enumerate locale suffixes...then could check
                // docsURLs directly, followed by nbresloc: URL or something.
                URL target;
                String ext, basename;
                int index = resource.lastIndexOf('.');
                if (index != -1) {
                    ext = resource.substring(index + 1);
                    basename = resource.substring(0, index).replace('/', '.');
                } else {
                    ext = null;
                    basename = resource.replace('/', '.');
                }
                try {
                    target = NbBundle.getLocalizedFile(basename, ext, Locale.getDefault(), getDocsLoader());
                } catch (MissingResourceException mre) {
                    IOException ioe = new IOException("cannot connect to " + url);
                    Installer.err.annotate(ioe, mre);
                    Installer.err.annotate(ioe, NbBundle.getMessage(NbDocsStreamHandler.class, "EXC_nbdocs_cannot_connect", url));
                    throw ioe;
                }
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

