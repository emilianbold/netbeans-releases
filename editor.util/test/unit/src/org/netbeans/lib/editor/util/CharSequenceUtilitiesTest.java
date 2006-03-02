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

package org.netbeans.lib.editor.util;

import java.util.Random;
import junit.framework.TestCase;

public class CharSequenceUtilitiesTest extends TestCase {
    
    private static final int CHARS_LENGTH = 1000;
    private static final int SUBSTR_LENGTH = 10;
    private static final Random rnd = new Random(0);
    
    public CharSequenceUtilitiesTest(String testName) {
        super(testName);
    }
    
    public void testCharSequence() {
        char[] chars = new char[CHARS_LENGTH];
        char[] chars_2 = new char[CHARS_LENGTH];
        generateChars(chars);
        generateChars(chars_2);
        String string = new String(chars);
        String string_2 = new String(chars_2);
        
        // textEquals
        assertTrue(CharSequenceUtilities.textEquals(string, string));
        String s = new String(chars);
        assertTrue(CharSequenceUtilities.textEquals(string, s));
        assertTrue(CharSequenceUtilities.textEquals(string, string_2) == string.equals(string_2));
        
        // toString
        assertTrue(CharSequenceUtilities.toString(string).equals(string));
        
        try {
            CharSequenceUtilities.toString(string, -1, 0);
            assertTrue(false);
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharSequenceUtilities.toString(string, 0, CHARS_LENGTH + 1);
            assertTrue(false);
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            CharSequenceUtilities.toString(string, CHARS_LENGTH, 0);
            assertTrue(false);
        } catch (IndexOutOfBoundsException e) {
        }
        int start = CHARS_LENGTH / 5;
        int end = CHARS_LENGTH - start;
        assertTrue(CharSequenceUtilities.toString(string, start, end).equals(string.substring(start, end)));
        
        // append
        StringBuffer buf = new StringBuffer();
        CharSequenceUtilities.append(buf, string);
        CharSequenceUtilities.append(buf, string_2);
        StringBuffer buff = new StringBuffer();
        buff.append(string);
        buff.append(string_2);
        assertTrue(buff.toString().equals(buf.toString()));
        
        buf = new StringBuffer();
        CharSequenceUtilities.append(buf, string, start, end);
        assertTrue(buf.toString().equals(string.substring(start, end)));
        
        // indexOf
        char ch = string.charAt(start);
        assertTrue(string.indexOf(ch) == CharSequenceUtilities.indexOf(string, ch));
        assertTrue(string.indexOf(ch, 2 * start) == CharSequenceUtilities.indexOf(string, ch, 2 * start));
        
        String eta = string.substring(start, start + SUBSTR_LENGTH);
        assertTrue(string.indexOf(eta) == CharSequenceUtilities.indexOf(string, eta));
        eta = string.substring(2 * start, 2 * start + SUBSTR_LENGTH);
        assertTrue(string.indexOf(eta, start) == CharSequenceUtilities.indexOf(string, eta, start));
        
        // lastIndexOf
        assertTrue(string.lastIndexOf(ch) == CharSequenceUtilities.lastIndexOf(string, ch));
        assertTrue(string.lastIndexOf(ch, 2 * start) == CharSequenceUtilities.lastIndexOf(string, ch, 2 * start));
        
        eta = string.substring(start, start + SUBSTR_LENGTH);
        assertTrue(string.lastIndexOf(eta) == CharSequenceUtilities.lastIndexOf(string, eta));
        eta = string.substring(2 * start, 2 * start + SUBSTR_LENGTH);
        assertTrue(string.lastIndexOf(eta, CHARS_LENGTH) == CharSequenceUtilities.lastIndexOf(string, eta, CHARS_LENGTH));
        
        // trim
        buf = new StringBuffer();
        for (int x = 0; x < SUBSTR_LENGTH; x++) {
            buf.append(' ');
        }
        buf.append(string);
        for (int x = 0; x < SUBSTR_LENGTH; x++) {
            buf.append(' ');
        }
        assertTrue(CharSequenceUtilities.textEquals(string, CharSequenceUtilities.trim(buf.toString())));
        
        // startsWith
        assertTrue(CharSequenceUtilities.startsWith(string, string.substring(0, SUBSTR_LENGTH)));
        
        // endsWith
        assertTrue(CharSequenceUtilities.endsWith(string, string.substring(CHARS_LENGTH - SUBSTR_LENGTH)));
    }
    
    public void generateChars(char[] chars) {
        for (int x = 0; x < chars.length; x++) {
            chars[x] = (char) rnd.nextInt();
        }
    }
    
}
