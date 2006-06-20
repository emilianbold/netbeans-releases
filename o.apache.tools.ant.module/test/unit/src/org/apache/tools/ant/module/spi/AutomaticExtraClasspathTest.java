/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.spi;

import java.util.logging.Level;
import junit.framework.TestCase;
import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.URLMapper;
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

    protected void setUp() throws Exception {
        URL u = getClass().getResource("AutomaticExtraClasspathTest.xml");
        FileSystem fs = new XMLFileSystem(u);
        fo = fs.findResource("testAutoProvider");
        assertNotNull("There is the resource", fo);
        bad = fs.findResource("brokenURL");
        assertNotNull("There is the bad", bad);
    }

    protected void tearDown() throws Exception {
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
        
        if (log.toString().indexOf("IllegalStateException") == -1) {
            fail("IllegalStateException shall be thrown:\n" + log);
        }
    }

    public void testFailIfTheFileDoesNotExists() throws Exception {
        URL u = new File(getWorkDir(), "does-not-exists.txt").toURI().toURL();
        wd = u;
        
        CharSequence log = Log.enable("", Level.WARNING);
        Object value = fo.getAttribute("instanceCreate");
        assertNull("no provider created: " + value, value);
        
        if (log.toString().indexOf("IllegalStateException") == -1) {
            fail("IllegalStateException shall be thrown:\n" + log);
        }
    }
    
}
