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
 * Software is Sun Microsystems, Inc. Portions Copyright 2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

import junit.framework.TestCase;

/**
 *
 * @author  Marian Petras
 */
public class ClassNameTextFieldTest extends TestCase {

    public ClassNameTextFieldTest(String testName) {
        super(testName);
    }

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
