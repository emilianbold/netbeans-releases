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

package org.netbeans.modules.versioning.system.cvss.util;

import junit.framework.TestCase;

import java.io.File;

/**
 * Tests for the Util class.
 *
 * @author Maros Sandor
 */
public class UtilTest extends TestCase {

    public static void testGetRelativePath() {
    }

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
