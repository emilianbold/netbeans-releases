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
}
