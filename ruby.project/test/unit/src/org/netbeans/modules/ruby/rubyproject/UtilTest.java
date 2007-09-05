/*
 * UtilTest.java
 * JUnit based test
 *
 * Created on September 4, 2007, 5:01 PM
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
