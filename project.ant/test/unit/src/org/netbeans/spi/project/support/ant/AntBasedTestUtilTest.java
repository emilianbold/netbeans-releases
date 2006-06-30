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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Properties;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.CollocationQueryImplementation;

/**
 * Test functionality of AntBasedTestUtil itself.
 * @author Jesse Glick
 */
public class AntBasedTestUtilTest extends NbTestCase {

    static {
        AntBasedTestUtilTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }

    /**
     * Create a new test suite.
     * @param name the suite name
     */
    public AntBasedTestUtilTest(String name) {
        super(name);
    }

    /**
     * Check that reported text diffs are actually accurate.
     * @throws Exception in case of an unexpected error
     */
    public void testCountTextDiffs() throws Exception {
        String f1 =
            "one\n" +
            "two\n" +
            "three\n" +
            "four\n" +
            "five\n" +
            "six\n" +
            "seven\n" +
            "eight\n" +
            "nine\n" +
            "ten\n";
        String f2 =
            "one\n" +
            // two deleted
            // three deleted
            "four\n" +
            "four #2\n" + // added
            "four #3\n" + // added
            "five\n" +
            "six six six\n" + // modified
            "sevvin'!\n" + // modified
            "eight\n" +
            "najn\n" + // modified
            "ten\n" +
            "ten #2\n"; // added
        int[] count = AntBasedTestUtil.countTextDiffs(new StringReader(f1), new StringReader(f2));
        assertEquals("should have three entries", 3, count.length);
        assertEquals("three lines modified", 3, count[0]);
        assertEquals("three lines added", 3, count[1]);
        assertEquals("two lines deleted", 2, count[2]);
    }
    
    public void testTestCollocationQueryImplementation() throws Exception {
        File root = new File(System.getProperty("java.io.tmpdir"));
        assertTrue("using absolute root " + root, root.isAbsolute());
        CollocationQueryImplementation cqi = AntBasedTestUtil.testCollocationQueryImplementation(root);
        File f1 = new File(root, "f1");
        File f2 = new File(root, "f2");
        File d1f1 = new File(new File(root, "d1"), "f1");
        File d2f1 = new File(new File(root, "d2"), "f1");
        File s = new File(root, "separate");
        File s1 = new File(s, "s1");
        File s2 = new File(s, "s2");
        File t = new File(root, "transient");
        File t1 = new File(t, "t1");
        File t2 = new File(t, "t2");
        assertTrue("f1 & f2 collocated", cqi.areCollocated(f1, f2));
        assertTrue("f1 & f2 collocated (reverse)", cqi.areCollocated(f2, f1));
        assertTrue("d1f1 & d2f1 collocated", cqi.areCollocated(d1f1, d2f1));
        assertTrue("s1 & s2 collocated", cqi.areCollocated(s1, s2));
        assertTrue("s & s1 collocated", cqi.areCollocated(s, s1));
        assertFalse("t1 & t2 not collocated", cqi.areCollocated(t1, t2));
        assertFalse("f1 & t1 not collocated", cqi.areCollocated(f1, t1));
        assertFalse("f1 & s1 not collocated", cqi.areCollocated(f1, s1));
        assertFalse("s1 & t1 not collocated", cqi.areCollocated(s1, t1));
        assertEquals("right root for f1", root, cqi.findRoot(f1));
        assertEquals("right root for f2", root, cqi.findRoot(f2));
        assertEquals("right root for d1f1", root, cqi.findRoot(d1f1));
        assertEquals("right root for d2f1", root, cqi.findRoot(d2f1));
        assertEquals("right root for s", s, cqi.findRoot(s));
        assertEquals("right root for s1", s, cqi.findRoot(s1));
        assertEquals("right root for s2", s, cqi.findRoot(s2));
        assertEquals("right root for t", null, cqi.findRoot(t));
        assertEquals("right root for t1", null, cqi.findRoot(t1));
        assertEquals("right root for t2", null, cqi.findRoot(t2));
    }
    
    public void testReplaceInFile() throws Exception {
        clearWorkDir();
        File workdir = getWorkDir();
        File props = new File(workdir, "test.properties");
        Properties p = new Properties();
        p.setProperty("key1", "val1");
        p.setProperty("key2", "val2");
        OutputStream os = new FileOutputStream(props);
        try {
            p.store(os, null);
        } finally {
            os.close();
        }
        assertEquals("two replacements", 2, AntBasedTestUtil.replaceInFile(props, "val", "value"));
        p.clear();
        InputStream is = new FileInputStream(props);
        try {
            p.load(is);
        } finally {
            is.close();
        }
        assertEquals("correct key1", "value1", p.getProperty("key1"));
        assertEquals("correct key2", "value2", p.getProperty("key2"));
    }
    
}
