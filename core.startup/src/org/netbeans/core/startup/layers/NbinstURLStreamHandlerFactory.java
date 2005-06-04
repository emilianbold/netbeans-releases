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
package org.netbeans.core.startup.layers;


import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.net.UnknownServiceException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;



/**
 * StreamHandlerFactory for nbinst protocol
 */
public class NbinstURLStreamHandlerFactory implements URLStreamHandlerFactory {

    /**
     * Creates URLStreamHandler for nbinst protocol
     * @param protocol
     * @return NbinstURLStreamHandler if the protocol is nbinst otherwise null
     */
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (NbinstURLMapper.PROTOCOL.equals(protocol)) {
            return new NbinstURLStreamHandler ();
        }
        return null;
    }

    /**
     * URLStreamHandler for nbinst protocol
     */
    private static class NbinstURLStreamHandler extends URLStreamHandler {

        /**
         * Creates URLConnection for URL with nbinst protocol.
         * @param u URL for which the URLConnection should be created
         * @return URLConnection
         * @throws IOException
         */
        protected URLConnection openConnection(URL u) throws IOException {
            return new NbinstURLConnection (u);
        }
    }

    /** URLConnection for URL with nbinst protocol.
     *
     */
    private static class NbinstURLConnection extends URLConnection {

        private FileObject fo;
        private InputStream iStream;
        private OutputStream oStream;

        /**
         * Creates new URLConnection
         * @param url the parameter for which the connection should be
         * created
         */
        public NbinstURLConnection (URL url) {
            super (url);
        }


        public void connect() throws IOException {
            if (fo == null) {
                FileObject[] decoded = NbinstURLMapper.decodeURL(this.url);
                if (decoded != null && decoded.length>0) {
                    fo = decoded[0];
                }
                else {
                    throw new FileNotFoundException("Cannot find: " + url); // NOI18N
                }
            }
            if (fo.isFolder()) {
                throw new UnknownServiceException();
            }
        }

        public int getContentLength() {
            try {
                this.connect();
                return (int) this.fo.getSize();     //May cause overflow long->int
            } catch (IOException e) {
                return -1;
            }
        }


        public InputStream getInputStream() throws IOException {
            this.connect();
            if (iStream == null) {
                iStream = fo.getInputStream();
            }
            return iStream;
        }


        public String getHeaderField (String name) {
            if ("content-type".equals(name)) {                  //NOI18N
                try {
                    this.connect();
                    return fo.getMIMEType();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ioe);
                }
            }
            return super.getHeaderField(name);
        }
    }
}
