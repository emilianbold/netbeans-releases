/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import junit.framework.*;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.junit.ClassNameTextField;

/**
 *
 * @author  Marian Petras
 */
public class ClassNameTextFieldTest extends TestCase {
    
    public ClassNameTextFieldTest(String testName) {
        super(testName);
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(ClassNameTextFieldTest.class);
        
        return suite;
    }

    /**
     */
    private class StringIntPair {
        private final String str;
        private final int value;
        StringIntPair(String str, int value) {
            this.str = str;
            this.value = value;
        }
    }
    
    /**
     * Test of determineStatus method, of class org.netbeans.modules.junit.ClassNameTextField.
     */
    public void testDetermineStatus() {
        StringIntPair[] testData = new StringIntPair[] {
                new StringIntPair("", ClassNameTextField.STATUS_BEFORE_PART),
                new StringIntPair("A", ClassNameTextField.STATUS_VALID),
                new StringIntPair("abc", ClassNameTextField.STATUS_VALID),
                new StringIntPair("Abc", ClassNameTextField.STATUS_VALID),
                new StringIntPair("abc2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("Abc2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("A2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("abc2.", ClassNameTextField.STATUS_BEFORE_PART),
                new StringIntPair("Abc2.", ClassNameTextField.STATUS_BEFORE_PART),
                new StringIntPair("a2.", ClassNameTextField.STATUS_BEFORE_PART),
                new StringIntPair("A2.", ClassNameTextField.STATUS_BEFORE_PART),
                new StringIntPair("2a", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2A", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2a.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2A.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("A.B", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a.B", ClassNameTextField.STATUS_VALID),
                new StringIntPair("A.b", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a.b", ClassNameTextField.STATUS_VALID),
                new StringIntPair("A2.b", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a2.b", ClassNameTextField.STATUS_VALID),
                new StringIntPair("A.b2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a.b2", ClassNameTextField.STATUS_VALID),
        };
        assertEquals(
                "check determined status if no text is passed",
                ClassNameTextField.STATUS_BEFORE_PART,
                new ClassNameTextField().determineStatus());
        for (int i = 0; i < testData.length; i++) {
            assertEquals(
                    "check determined status for text \"" + testData[i].str + '"',
                    testData[i].value,
                    new ClassNameTextField(testData[i].str).determineStatus());
        }
    }

    /**
     * Test of getStatus method, of class org.netbeans.modules.junit.ClassNameTextField.
     */
    public void testGetStatus() {
        StringIntPair[] testData = new StringIntPair[] {
                new StringIntPair("", ClassNameTextField.STATUS_EMPTY),
                new StringIntPair("A", ClassNameTextField.STATUS_VALID),
                new StringIntPair("abc", ClassNameTextField.STATUS_VALID),
                new StringIntPair("Abc", ClassNameTextField.STATUS_VALID),
                new StringIntPair("abc2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("Abc2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("A2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("abc2.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("Abc2.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("a2.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("A2.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2a", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2A", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2a.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2A.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("A.B", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a.B", ClassNameTextField.STATUS_VALID),
                new StringIntPair("A.b", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a.b", ClassNameTextField.STATUS_VALID),
                new StringIntPair("A2.b", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a2.b", ClassNameTextField.STATUS_VALID),
                new StringIntPair("A.b2", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a.b2", ClassNameTextField.STATUS_VALID),
        };
        assertEquals(
                "check status if no parameter passed",
                ClassNameTextField.STATUS_EMPTY,
                new ClassNameTextField().getStatus());
        for (int i = 0; i < testData.length; i++) {
            assertEquals(
                    "check status for text \"" + testData[i].str + '"',
                    testData[i].value,
                    new ClassNameTextField(testData[i].str).getStatus());
        }
    }

}
