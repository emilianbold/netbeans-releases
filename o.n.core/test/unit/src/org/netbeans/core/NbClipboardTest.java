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
package org.netbeans.core;

import org.netbeans.junit.NbTestCase;

/** Basic tests on NbClipboard
 *
 * @author Jaroslav Tulach
 */
public class NbClipboardTest extends NbTestCase {
    
    public NbClipboardTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        System.getProperties().remove("netbeans.slow.system.clipboard.hack");
    }

    protected void tearDown() throws Exception {
    }

    public void testDefaultOnJDK15AndLater() {
        if (System.getProperty("java.version").startsWith("1.4")) {
            return;
        }
        
        NbClipboard ec = new NbClipboard();
        assertTrue("By default we still do use slow hacks", ec.slowSystemClipboard);
    }
    public void testPropOnJDK15AndLater() {
        if (System.getProperty("java.version").startsWith("1.4")) {
            return;
        }
        
        System.setProperty("netbeans.slow.system.clipboard.hack", "false");
        
        NbClipboard ec = new NbClipboard();
        assertFalse("Property overrides default", ec.slowSystemClipboard);
        assertEquals("sun.awt.datatransfer.timeout is now 1000", "1000", System.getProperty("sun.awt.datatransfer.timeout"));
    }
}
