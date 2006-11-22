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

package org.apache.tools.ant.module.spi;

import java.util.logging.Level;
import java.io.File;
import java.net.URL;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.XMLFileSystem;

/**
 *
 * @author Jaroslav Tulach
 */
public class AutomaticExtraClasspathTest extends NbTestCase {
    private static URL wd;
    
    
    FileObject fo, bad;
    
    public AutomaticExtraClasspathTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        URL u = getClass().getResource("AutomaticExtraClasspathTest.xml");
        FileSystem fs = new XMLFileSystem(u);
        fo = fs.findResource("testAutoProvider");
        assertNotNull("There is the resource", fo);
        bad = fs.findResource("brokenURL");
        assertNotNull("There is the bad", bad);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    public static URL getWD() {
        return wd;
    }
    
    public void testReadWorkDir() throws Exception {
        URL u = getWorkDir().toURI().toURL();
        wd = u;
        
        Object value = fo.getAttribute("instanceCreate");
        assertTrue("provider created: " + value, value instanceof AutomaticExtraClasspathProvider);
        
        AutomaticExtraClasspathProvider auto = (AutomaticExtraClasspathProvider)value;
        File[] arr = auto.getClasspathItems();
        assertNotNull(arr);
        assertEquals("One item", 1, arr.length);
        assertEquals("It is our work dir", getWorkDir(), arr[0]);
    }

    public void testBadURL() throws Exception {
        CharSequence log = Log.enable("", Level.WARNING);
        Object value = bad.getAttribute("instanceCreate");
        assertNull("no provider created: " + value, value);
        
        if (log.toString().indexOf("IllegalArgumentException") == -1) {
            fail("IllegalArgumentException shall be thrown:\n" + log);
        }
    }

    public void testFailIfTheFileDoesNotExists() throws Exception {
        URL u = new File(getWorkDir(), "does-not-exists.txt").toURI().toURL();
        wd = u;
        
        CharSequence log = Log.enable("", Level.WARNING);
        Object value = fo.getAttribute("instanceCreate");
        assertNull("no provider created: " + value, value);
        
        if (log.toString().indexOf("FileNotFoundException") == -1) {
            fail("FileNotFoundException shall be thrown:\n" + log);
        }
    }
    
}
