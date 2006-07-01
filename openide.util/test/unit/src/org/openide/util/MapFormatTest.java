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
package org.openide.util;

import java.util.HashMap;
import junit.framework.*;

/**
 *
 * @author Jaroslav Tulach
 */
public class MapFormatTest extends TestCase {

    public MapFormatTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    public void testFormatIssue67238() {

        HashMap args = new HashMap();
        args.put("NAME", "Jaroslav");

        MapFormat f = new MapFormat(args);
        f.setLeftBrace("__");
        f.setRightBrace("__");
        f.setExactMatch(false);
        String result = f.format("/*_____________________*/\n/*__NAME__*/");
        
        assertEquals("Should be ok: " + result, "/*_____________________*/\n/*Jaroslav*/", result);
    }
    
    public void testExectLineWithTheProblemFromFormatIssue67238 () {
        String s = "/*___________________________________________________________________________*/";
        
        HashMap args = new HashMap();
        args.put("NAME", "Jaroslav");

        MapFormat f = new MapFormat(args);
        f.setLeftBrace("__");
        f.setRightBrace("__");
        f.setExactMatch(false);
        String result = f.format(s);
        
        assertEquals("Should be ok: " + result, s, result);
        
    }
    
    public void testIssue67238() {
        final String s = "/*___________________________________________________________________________*/";
        HashMap args = new HashMap();
        args.put("NAME", "Jaroslav");
        MapFormat f = new MapFormat(args) {
            protected Object processKey(String key) {
                fail("There is no key in \"" + s + "\", processKey() should not be called with key:" + key);
                return "not defined";
            }
        };
        f.setLeftBrace("__");
        f.setRightBrace("__");
        f.setExactMatch(false);
        f.format(s);
    }
}
