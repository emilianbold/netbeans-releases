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

package org.openide.loaders;

import java.util.Enumeration;
import junit.framework.TestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class ExtensionListTest extends TestCase {
    
    public ExtensionListTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddExtension() {
        ExtensionList instance = new ExtensionList();
        instance.addExtension("html");
        instance.addExtension("txt");
        instance.addExtension("java");
        Enumeration<String> en = instance.extensions();
        
        assertEquals("html", en.nextElement());
        assertEquals("java", en.nextElement());
        assertEquals("txt", en.nextElement());
        assertFalse(en.hasMoreElements());
        
        assertTrue(instance.isRegistered("x.java"));
        assertTrue(instance.isRegistered("x.html"));
        assertTrue(instance.isRegistered("x.txt"));
        assertFalse(instance.isRegistered("x.form"));
    }     

    public void testAddMime() {
        ExtensionList instance = new ExtensionList();
        instance.addMimeType("text/x-java");
        instance.addMimeType("text/html");
        instance.addMimeType("text/plain");
        Enumeration<String> en = instance.mimeTypes();
        
        assertEquals("text/html", en.nextElement());
        assertEquals("text/plain", en.nextElement());
        assertEquals("text/x-java", en.nextElement());
        assertFalse(en.hasMoreElements());
        
        
    }     
}
