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

package org.netbeans.core.startup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/**
 * Proxying stream handler factory. Currently searches Lookup for registered
 * factories and delegates to them. But #20838 suggests using JNDI instead,
 * in which case registering them via Lookup would be deprecated.
 * @author Jesse Glick
 */
final class NbURLStreamHandlerFactory implements URLStreamHandlerFactory, LookupListener {
    private static final Logger LOG = Logger.getLogger(NbURLStreamHandlerFactory.class.getName());
    
    private Lookup.Result<URLStreamHandlerFactory> r = null;
    private URLStreamHandlerFactory[] handlers = null;
    private URLStreamHandlerFactory delegate;
    
    public NbURLStreamHandlerFactory() {}
    
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals("jar") || protocol.equals("file") || // NOI18N
                protocol.equals("http") || protocol.equals("resource")) { // NOI18N
            // Well-known handlers in JRE. Do not try to initialize lookup, etc.
            return null;
        }
        
        if (protocol.equals("nbfs")) { // NOI18N
             return FileUtil.nbfsURLStreamHandler();
        }
        
        if (protocol.equals(NbResourceStreamHandler.PROTOCOL_SYSTEM_RESOURCE) ||
                   protocol.equals(NbResourceStreamHandler.PROTOCOL_LOCALIZED_SYSTEM_RESOURCE)) {
           return new NbResourceStreamHandler();
        }
        
        URLStreamHandlerFactory d = delegate;
        if (d != null) {
            URLStreamHandler h = d.createURLStreamHandler(protocol);
            if (h != null) {
                return h;
            }
        }
        
        URLStreamHandlerFactory[] _handlers;
        synchronized (this) {
            if (r == null) {
                r = Lookup.getDefault().lookupResult(URLStreamHandlerFactory.class);
                r.addLookupListener(this);
                resultChanged(null);
            }
            _handlers = handlers;
        }
        if (_handlers == null) {
            // Too early during startup (#75422)
            return null;
        }
        for (int i = 0; i < _handlers.length; i++) {
            URLStreamHandler h = _handlers[i].createURLStreamHandler(protocol);
            if (h != null) {
                return h;
            }
        }
        return null;
    }
    
    public void resultChanged(LookupEvent ev) {
        Collection<? extends URLStreamHandlerFactory> c = r.allInstances();
        synchronized (this) {
            handlers = c.toArray(new URLStreamHandlerFactory[0]);
        }
    }

    void registerUsingReflection(Error e) {
        LOG.log(Level.CONFIG, "Problems registering URLStreamHandlerFactory, trying reflection", e); // NOI18N
        try {
            URLStreamHandlerFactory prev = null;
            for (Field f : URL.class.getDeclaredFields()) {
                LOG.log(Level.FINEST, "Found field {0}", f);
                if (f.getType() == URLStreamHandlerFactory.class) {
                    LOG.log(Level.FINEST, "Clearing field {0}");
                    f.setAccessible(true);
                    prev = (URLStreamHandlerFactory)f.get(null);
                    LOG.log(Level.CONFIG, "Previous value was {0}", prev);
                    f.set(null, null);
                    LOG.config("Field is supposed to be empty");
                    break;
                }
            }
            URL.setURLStreamHandlerFactory(this);
            delegate = prev;
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, 
                "No way to register URLStreamHandlerFactory; NetBeans is unlikely to work", 
                t
            ); // NOI18N
        }
    }
    
    /** Stream handler for internal resource-based URLs.
     * Copied with modifications from org.openide.execution - that version is now
     * deprecated and handles only deprecated protocols.
     * @author Jesse Glick
     */
    private static final class NbResourceStreamHandler extends URLStreamHandler {
        
        public NbResourceStreamHandler() {}
        
        public static final String PROTOCOL_SYSTEM_RESOURCE = "nbres"; // NOI18N
        public static final String PROTOCOL_LOCALIZED_SYSTEM_RESOURCE = "nbresloc"; // NOI18N
        
        public URLConnection openConnection(URL u) throws IOException {
            if (u.getProtocol().equals(PROTOCOL_SYSTEM_RESOURCE)) {
                return new Connection(u, false);
            } else if (u.getProtocol().equals(PROTOCOL_LOCALIZED_SYSTEM_RESOURCE)) {
                return new Connection(u, true);
            } else {
                throw new IOException("Bad protocol: " + u.getProtocol()); // NOI18N
            }
        }
        
        private static class Connection extends URLConnection {
            
            private final boolean localized;
            
            // A real connection to delegate to. Non-null if successfully connected.
            private URLConnection real;
            
            private IOException exception = null;
            
            public Connection(URL u, boolean localized) {
                super(u);
                this.localized = localized;
            }
            
            /** Tries to get a URL from this resource from the proper classloader,
             * localizing first if requested.
             * Also opens the URL to make a connection; this connection, <code>real</code>,
             * will be delegated to for all operations.
             */
            public synchronized void connect() throws IOException {
                if (exception != null) {
                    // See tryToConnect().
                    IOException e = exception;
                    exception = null;
                    throw e;
                }
                if (! connected) {
                    String resource = url.getPath();
                    if (resource.length() > 0 && resource.charAt(0) == '/') resource = resource.substring(1); // NOI18N
                    ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                    URL target;
                    URL t1 = loader.getResource(resource);
                    if (localized) {
                        // Find the suffix insertion point.
                        // XXX #29580: should have a shared API for this
                        int dotIndex = resource.lastIndexOf('.');
                        if (dotIndex < resource.lastIndexOf('/')) {
                            dotIndex = -1;
                        }
                        String base, ext;
                        if (dotIndex != -1) {
                            base = resource.substring(0, dotIndex);
                            ext = resource.substring(dotIndex);
                        } else {
                            base = resource;
                            ext = "";
                        }
                        target = null;
                        Iterator<String> suffixes = NbBundle.getLocalizingSuffixes();
                        while (suffixes.hasNext()) {
                            String suffix = suffixes.next();
                            target = "".equals(suffix)? t1: loader.getResource(base + suffix + ext);
                            if (target != null) {
                                break;
                            }
                        }
                    } else {
                        target = t1;
                    }
                    if (target == null) {
                        throw new IOException(NbBundle.getMessage(NbURLStreamHandlerFactory.class, "EXC_nbres_cannot_connect", url));
                    }
                    real = target.openConnection();
                    real.connect();
                    connected = true;
                }
            }
            
            /** Try to connect; but if it does not work, oh well.
             * Ideally this would be quite unnecessary.
             * Unfortunately much code, inclduing the Swing editor kits,
             * gets header fields and so on without ever calling connect().
             * These methods cannot even throw exceptions so it is a mess.
             * E.g. if you display a nbres: URL in the ICE browser, it is fine:
             * it calls connect() according to the specification, then
             * getContentType() produces text/html as expected.
             * But using the SwingBrowser default implementation, it goes
             * ahead and calls getContentType() immediately. So we have
             * to try to connect and get the right content type then too.
             * This complicated the timing of error reporting.
             */
            private void tryToConnect() {
                if (connected || exception != null) return;
                try {
                    connect();
                } catch (IOException ioe) {
                    exception = ioe;
                }
            }
            
            public String getHeaderField(int n) {
                tryToConnect();
                if (connected)
                    return real.getHeaderField(n);
                else
                    return null;
            }
            
            public String getHeaderFieldKey(int n) {
                tryToConnect();
                if (connected)
                    return real.getHeaderFieldKey(n);
                else
                    return null;
            }
            
            public String getHeaderField(String key) {
                tryToConnect();
                if (connected) {
                    return real.getHeaderField(key);
                }
                return null;
            }
            
            public InputStream getInputStream() throws IOException {
                connect();
                return real.getInputStream();
            }
            
            public OutputStream getOutputStream() throws IOException {
                connect();
                return real.getOutputStream();
            }
            
            // Should not be required, but they are:
            
            public String getContentType() {
                tryToConnect();
                if (connected)
                    return real.getContentType();
                else
                    return "application/octet-stream"; // NOI18N
            }
            
            public int getContentLength() {
                tryToConnect();
                if (connected)
                    return real.getContentLength();
                else
                    return 0;
            }

            public @Override long getLastModified() {
                if (connected && /* #96928 */ !real.getURL().toExternalForm().contains("http:")) { // NOI18N
                    return real.getLastModified();
                }
                return 0L;
            }

            // [PENDING] might be some more methods it would be useful to delegate, possibly
            
        }
        
    }
    
}
