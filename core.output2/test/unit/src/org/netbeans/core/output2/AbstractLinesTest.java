/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.output2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.*;

/**
 *
 * @author mkleint
 */
public class AbstractLinesTest extends TestCase {
    
    public AbstractLinesTest(String testName) {
        super(testName);
    }
    
    public void testEscapePattern() throws Exception {
        doTestSingle("[", "hello[world", "helloworld]");
        doTestSingle("[a-z]", "hello[a-z]world", "helloworld");
        doTestSingle("(abc*ef)", "xx(abc*ef)xx", "abcdef");
        doTestSingle("(abc*ef", "xx(abc*efxx", "abcdef");
        doTestSingle("^abc", "xx(^abc*efxx", "abcdef");
        doTestSingle("\\d", "xx\\defxx", "8475");
    }
    
    private void doTestSingle(String find, String success, String failure) {
        Pattern patt = AbstractLines.escapePattern(find);
        assertTrue(patt.matcher(success).find());
        assertFalse(patt.matcher(failure).find());
    }
    
}
