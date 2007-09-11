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
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Template;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;

/**
 * Test functionality of internal URLs.
 * @author Jesse Glick
 */
public class NbURLStreamHandlerFactoryDeadlockTest extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", MyLkp.class.getName());
    }
    
    public NbURLStreamHandlerFactoryDeadlockTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        Main.initializeURLFactory();
        super.setUp();
    }

    @Override
    protected int timeOut() {
        return 10000;
    }
    
    
    
    public void testNbResourceStreamHandlerAndURLStreamHandlerFactoryMerging() throws Exception {
        URL url = new URL("https://www.netbeans.org");
        
        Class<?> c = getClass();
        assertClass(c);
    }
    
    private static void assertClass(Class<?> c) throws IOException {
        URL u = c.getResource(c.getSimpleName() + ".class");
        assertNotNull("Resource for " + c.getSimpleName() + " found", u);
        byte[] arr = new byte[4096];
        int r = u.openStream().read(arr);
        if (r <= 0) {
            fail("Should read something: " + r);
        }
    }
    
    public static class MyLkp extends AbstractLookup implements Runnable {
        @Override
        protected  void beforeLookup(Template<?> template) {
            RequestProcessor.getDefault().post(this).waitFinished();
        }
        
        public void run() {
            try {
                URL url = new URL("https://www.netbeans.org");
                assertClass(NbURLStreamHandlerFactoryDeadlockTest.class);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
