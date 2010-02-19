/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
                new StringIntPair("A", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("ATest", ClassNameTextField.STATUS_VALID),
                new StringIntPair("abc", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("abcTest", ClassNameTextField.STATUS_VALID),
                new StringIntPair("Abc", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("AbcTest", ClassNameTextField.STATUS_VALID),
                new StringIntPair("abc2", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("Abc2", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("a2", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("A2", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("abc2.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("Abc2.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("a2.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("A2.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2a", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2A", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2a.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("2A.", ClassNameTextField.STATUS_INVALID),
                new StringIntPair("A.B", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("A.BTest", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a.B", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("A.b", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("a.b", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("A2.b", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("a2.b", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("A.b2", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
                new StringIntPair("A.b2Test", ClassNameTextField.STATUS_VALID),
                new StringIntPair("a.b2", ClassNameTextField.STATUS_VALID_END_NOT_TEST),
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
