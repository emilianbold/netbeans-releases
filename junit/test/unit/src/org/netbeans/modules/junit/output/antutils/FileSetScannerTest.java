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

package org.netbeans.modules.junit.output.antutils;

import java.io.File;
import junit.framework.*;
import org.netbeans.modules.junit.output.antutils.FileSetScanner.AntPattern;

/**
 *
 * @author  Marian Petras
 */
public class FileSetScannerTest extends TestCase {
    
    private static final String SEP = File.separator;
    
    
    public FileSetScannerTest(String testName) {
        super(testName);
    }
    
    
    public void testConstructor() {
        System.out.println("constructor");
        
        try {
            new AntPattern(new String[] {"ij", "kl"});
            new AntPattern(new String[] {"ij"});
            new AntPattern(new String[] {});
        } catch (IllegalArgumentException ex) {
            fail("The IllegalArgumentException should not be thrown");
        }
        
        try {
            new AntPattern(null);
            fail("The constructor should throw an IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            //OK
        }
    }
    
    public void testEquals() {
        System.out.println("equals");
        
        assertEquals(new AntPattern(new String[0]),
                     new AntPattern(new String[0]));
        
        assertNotEqual(new AntPattern(new String[0]),
                       null);
        
        assertEquals(new AntPattern(new String[] {"cat", "dog"}),
                     new AntPattern(new String[] {"cat", "dog"}));
        assertEquals(new AntPattern(new String[] {"cat", "dog"}),
                     new AntPattern(new String[] {new String("cat"), "dog"}));
        assertEquals(new AntPattern(new String[] {"cat", "dog"}),
                     new AntPattern(new String[] {"cat", new String("dog")}));
        assertEquals(new AntPattern(new String[] {"cat", "dog"}),
                     new AntPattern(new String[] {new String("cat"),
                                                  new String("dog")}));
        
        assertNotEqual(new AntPattern(new String[] {"cat", "dog"}),
                       new AntPattern(new String[] {"cat", "dog", "pig"}));
        assertNotEqual(new AntPattern(new String[] {"cat", "dog"}),
                       new AntPattern(new String[] {"cat", "cow"}));
        assertNotEqual(new AntPattern(new String[] {"cat", "dog"}),
                       new AntPattern(new String[] {"cat"}));
        assertNotEqual(new AntPattern(new String[] {"cat", "dog"}),
                       new AntPattern(new String[] {}));
        assertNotEqual(new AntPattern(new String[] {"cat"}),
                       new AntPattern(new String[] {"cow"}));
    }
    
    public void testParsePatternString() {
        System.out.println("parsePatternString");
        
        String patternString;
        AntPattern expResult;
        AntPattern result;
        
        FileSetScanner s =
                new FileSetScanner(new FileSet(new AntProject()));
        
        patternString = replaceSep("ab");
        expResult = new AntPattern(new String[] {"ab"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("**");
        expResult = new AntPattern(new String[] {"**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/cd");
        expResult = new AntPattern(new String[] {"ab", "cd"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/cd/");
        expResult = new AntPattern(new String[] {"ab", "cd", "**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/**");
        expResult = new AntPattern(new String[] {"ab", "**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/cd**");
        expResult = new AntPattern(new String[] {"ab", "cd**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/**cd");
        expResult = new AntPattern(new String[] {"ab", "**cd"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/cd*");
        expResult = new AntPattern(new String[] {"ab", "cd*"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/*cd");
        expResult = new AntPattern(new String[] {"ab", "*cd"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/c*d");
        expResult = new AntPattern(new String[] {"ab", "c*d"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/c*d*");
        expResult = new AntPattern(new String[] {"ab", "c*d*"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("**/cd");
        expResult = new AntPattern(new String[] {"**", "cd"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/");
        expResult = new AntPattern(new String[] {"ab", "**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("ab/*");
        expResult = new AntPattern(new String[] {"ab", "*"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("a?bc/def*g/*.java");
        expResult = new AntPattern(new String[] {"a?bc", "def*g", "*.java"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("abc/**/**/xyz");
        expResult = new AntPattern(new String[] {"abc", "**", "xyz"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);
        
        patternString = replaceSep("abc/**/**");
        expResult = new AntPattern(new String[] {"abc", "**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("**");
        expResult = new AntPattern(new String[] {"**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("**/**");
        expResult = new AntPattern(new String[] {"**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("**/**/**");
        expResult = new AntPattern(new String[] {"**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("**/xyz/*.java");
        expResult = new AntPattern(new String[] {"**", "xyz", "*.java"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("**/**/xyz/**");
        expResult = new AntPattern(new String[] {"**", "xyz", "**"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

        patternString = replaceSep("ab/**/cd/**/**/ef/gh");
        expResult = new AntPattern(new String[] {
                                        "ab", "**", "cd", "**", "ef", "gh"});
        result = s.parsePatternString(patternString);
        assertEquals(expResult, result);

    }
    
    public void testQuote() {
        assertEquals("", AntPattern.quote(""));
        assertEquals("abcd", AntPattern.quote("abcd"));
        assertEquals("ab\\*cd", AntPattern.quote("ab*cd"));
        assertEquals("\\^abcd", AntPattern.quote("^abcd"));
        assertEquals("abcd\\$", AntPattern.quote("abcd$"));
        assertEquals("ab\\|cd", AntPattern.quote("ab|cd"));
        assertEquals("a\\.bcd", AntPattern.quote("a.bcd"));
        assertEquals("a\\.\\*bcd", AntPattern.quote("a.*bcd"));
        assertEquals("ab\\[p-q\\]cd", AntPattern.quote("ab[p-q]cd"));
        assertEquals("ab\\(cd\\)\\*", AntPattern.quote("ab(cd)*"));
        assertEquals("a\\+bcd\\$", AntPattern.quote("a+bcd$"));
        assertEquals("abcd\\{4,\\}", AntPattern.quote("abcd{4,}"));
    }
    
    private static String replaceSep(String pattern) {
        return File.separatorChar == '/'
               ? pattern 
               : pattern.replace('/', File.separatorChar);
    }
    
    private static void assertNotEqual(Object a, Object b) {
        assertTrue((a == null) && (b != null)
                   || (a != null) && !a.equals(b));
    }

}
