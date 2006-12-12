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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * Making sure our framework can replace already registered factories.
 * 
 * @author Jaroslav Tulach
 */
public class NbURLStreamHandlerFactoryWhenSetTest extends NbURLStreamHandlerFactoryTest {
    static {
        // preregister some strange factory:
        java.net.URL.setURLStreamHandlerFactory(new SomeURLStreamHandFact());
    }
    
    
    public NbURLStreamHandlerFactoryWhenSetTest(String s) {
        super(s);
    }
    
    public void testDefaultImpleDelegatesToPreviousURLFactory() throws Exception {
        URL u = new URL("jarda://ClassLoaderCacheContent.properties/");
        
        byte[] arr = new byte[100000];
        int len = u.openStream().read(arr);
        if (len < 50000) {
            fail("Should be able to read at least 50KB: " + len);
        }
    }
    
    private static class SomeURLStreamHandFact implements URLStreamHandlerFactory {
        
        
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if ("jarda".equals(protocol)) {
                return new H();
            }
            return null;
        }
    }
    
    private static class H extends URLStreamHandler {
        protected URLConnection openConnection(URL u) throws IOException {
            return getClass().getResource(u.getHost()).openConnection();
        }
    }
}
