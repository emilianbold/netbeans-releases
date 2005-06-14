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

package org.netbeans.modules.versioning.system.cvss.util;

import junit.framework.TestCase;

import java.io.File;

import org.openide.filesystems.FileUtil;

/**
 * Tests for the Util class.
 * 
 * @author Maros Sandor
 */
public class UtilTest extends TestCase {

    public static void testIsParentOrEqual() {

        assertTrue(Utils.isParentOrEqual(new File("/"), new File("/")));
        assertTrue(Utils.isParentOrEqual(new File("C:\\"), new File("C:\\")));
        assertTrue(Utils.isParentOrEqual(new File("/"), new File("/a")));
        assertTrue(Utils.isParentOrEqual(new File("C:\\"), new File("C:\\a")));
        assertTrue(Utils.isParentOrEqual(new File("/a"), new File("/a")));
        assertTrue(Utils.isParentOrEqual(new File("C:\\a"), new File("C:\\a")));
        assertTrue(Utils.isParentOrEqual(new File("/a/b"), new File("/a/b")));
        assertTrue(Utils.isParentOrEqual(new File("C:\\a\\b"), new File("C:\\a\\b")));
        assertTrue(Utils.isParentOrEqual(new File("/"), new File("/a/b")));
        assertTrue(Utils.isParentOrEqual(new File("C:\\"), new File("C:\\a\\b")));
        assertTrue(Utils.isParentOrEqual(new File("/a/b/c"), new File("/a/b/c/d/e/f")));
        assertTrue(Utils.isParentOrEqual(new File("C:\\a\\b\\c"), new File("C:\\a\\b\\c\\d\\e\\f")));

        assertFalse(Utils.isParentOrEqual(new File("/a"), new File("/")));
        assertFalse(Utils.isParentOrEqual(new File("/a/b"), new File("/")));
        assertFalse(Utils.isParentOrEqual(new File("/a/b/c/d"), new File("/a/b/c")));
        assertFalse(Utils.isParentOrEqual(new File("C:\\a\\b"), new File("C:\\c")));
        assertFalse(Utils.isParentOrEqual(new File("/a/b"), new File("/a/bc")));
        assertFalse(Utils.isParentOrEqual(new File("C:\\abc"), new File("C:\\abcxyz")));
    }
}
