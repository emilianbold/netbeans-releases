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

package org.netbeans.modules.uihandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jaroslav Tulach
 */
public class MemoryURL extends URLStreamHandler {

    static {
        class F implements URLStreamHandlerFactory {
            public URLStreamHandler createURLStreamHandler(String protocol) {
                if (protocol.startsWith("memory")) {
                    return new MemoryURL();
                }
                return null;
            }
        }
        F f = new F();
        URL.setURLStreamHandlerFactory(f);
    }
    
    private static Map<String,String> contents = new HashMap<String,String>();
    public static void registerURL(String u, String content) {
        contents.put(u, content);
    }

    protected URLConnection openConnection(URL u) throws IOException {
        return new MC(u);
    }
    
    private static final class MC extends URLConnection {
        private String values;
        
        public MC(URL u) {
            super(u);
        }

        public void connect() throws IOException {
            values = contents.remove(url.toExternalForm());
            if (values == null) {
                throw new IOException("No such content: " + url);
            }
        }

        public InputStream getInputStream() throws IOException {
            connect();
            return new ByteArrayInputStream(values.getBytes());
        }
        
        
    }
}
