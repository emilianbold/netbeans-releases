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
