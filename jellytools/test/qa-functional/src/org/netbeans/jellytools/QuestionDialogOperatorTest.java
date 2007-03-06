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

package org.netbeans.jellytools;

import org.netbeans.junit.NbTestSuite;
/**
 * Test of org.netbeans.jellytools.QuestionDialogOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class QuestionDialogOperatorTest extends NbDialogOperatorTest {

    /** "Question" */
    private static final String TEST_DIALOG_TITLE =
            Bundle.getString("org.openide.text.Bundle", "LBL_SaveFile_Title");

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public QuestionDialogOperatorTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new QuestionDialogOperatorTest("testConstructorWithParameter"));
        suite.addTest(new QuestionDialogOperatorTest("testLblQuestion"));
        return suite;
    }
    
    /** Shows dialog to test. */
    protected void setUp() {
        showTestDialog(TEST_DIALOG_TITLE);
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Test constructor with text parameter. */
    public void testConstructorWithParameter() {
        new QuestionDialogOperator(TEST_DIALOG_LABEL).ok();
    }

    /** Test lblQuestion() method. */
    public void testLblQuestion() {
        QuestionDialogOperator qdo = new QuestionDialogOperator();
        String label = qdo.lblQuestion().getText();
        qdo.ok();
        assertEquals("Wrong label found.", TEST_DIALOG_LABEL, label);
    }
    
}
