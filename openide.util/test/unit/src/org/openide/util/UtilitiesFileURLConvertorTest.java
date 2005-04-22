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

package org.openide.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import org.netbeans.junit.*;
import junit.textui.TestRunner;

/** Test <code>File</code> &#8596; <code>URL</code> conversion.
 * @author Jesse Glick
 * @see "#29711"
 */
public class UtilitiesFileURLConvertorTest extends NbTestCase {
    
    public UtilitiesFileURLConvertorTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(UtilitiesFileURLConvertorTest.class));
    }
    
    public void testFileURLConversion() throws Exception {
        File f = File.createTempFile("foo", ".txt");
        check(f);
        File tmp = f.getParentFile();
        f = new File(tmp, "with a space.txt");
        check(f);
        // May seem strange but (#27330) hashes in paths are not so unheard-of.
        // Importantly, some Unix VC system (which?) uses them.
        f = new File(tmp, "strange#characters.txt");
        check(f);
        f = new File(tmp, "stranger?characters.txt");
        check(f);
    }
    
    private void check(File f) throws Exception {
        f.deleteOnExit();
        URL u = Utilities.toURL(f);
        assertEquals("correct protocol", "file", u.getProtocol());
        System.err.println("URL=" + u);
        //assertTrue(u.toExternalForm().indexOf(f.getName()) != -1);
        File f2 = Utilities.toFile(u);
        assertEquals("converts back to same file", f, f2);
        assertEquals("no dangling references", null, u.getRef());
        assertEquals("no dangling queries", null, u.getQuery());
        OutputStream os = null;
        try {
            os = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            // OK, OS does not permit such a file
            return;         
        }
        os.write(1);
        os.write(2);
        os.write(3);
        os.write(4);
        os.write(5);
        os.close();
        URLConnection conn = u.openConnection();
        conn.connect();
        assertEquals("URL connection works", 5, conn.getContentLength());
    }
    
}
