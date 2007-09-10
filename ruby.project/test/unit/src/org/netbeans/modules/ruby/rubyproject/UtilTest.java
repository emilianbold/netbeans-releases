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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.rubyproject;

import junit.framework.TestCase;

/**
 *
 * @author Tor Norbye
 */
public class UtilTest extends TestCase {
    
    public UtilTest(String testName) {
        super(testName);
    }

    public void testContainsAnsiColors() {
        assertTrue(Util.containsAnsiColors("\033[32m3 examples, 0 failures\033[0m"));
        assertTrue(Util.containsAnsiColors("\033[1;35m3 examples, 0 failures\033[0m"));
    }

    public void testContainsMultiAnsiColors() {
        assertTrue(Util.containsAnsiColors("\033[4;36;1mSQL (0.000210)\033[0m    \033[0;1mSET SQL_AUTO_IS_NULL=0\033[0m"));
        assertTrue(Util.containsAnsiColors("\033[4;36;1mRadiant::ExtensionMeta Columns (0.001849)\033[0m    \033[0;1mSHOW FIELDS FROM extension_meta\033[0m"));
    }

    public void testStripAnsiColors() {
        assertEquals("3 examples, 0 failures", Util.stripAnsiColors("\033[32m3 examples, 0 failures\033[0m"));
        assertEquals("3 examples, 0 failures", Util.stripAnsiColors("\033[1;35m3 examples, 0 failures\033[0m"));
    }

    public void testStripAnsiMultiColors() {
        assertEquals("SQL (0.000210)    SET SQL_AUTO_IS_NULL=0", Util.stripAnsiColors("\033[4;36;1mSQL (0.000210)\033[0m    \033[0;1mSET SQL_AUTO_IS_NULL=0\033[0m"));
        assertEquals("Radiant::ExtensionMeta Columns (0.001849)    SHOW FIELDS FROM extension_meta", Util.stripAnsiColors("\033[4;36;1mRadiant::ExtensionMeta Columns (0.001849)\033[0m    \033[0;1mSHOW FIELDS FROM extension_meta\033[0m"));
    }
}
