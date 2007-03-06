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
package org.netbeans.jellytools.actions;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;

/** Test org.netbeans.jellytools.actions.DeleteAction
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class DeleteActionTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public DeleteActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new DeleteActionTest("testPerformPopup"));
        suite.addTest(new DeleteActionTest("testPerformMenu"));
        suite.addTest(new DeleteActionTest("testPerformAPI"));
        suite.addTest(new DeleteActionTest("testPerformShortcut"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    // "Confirm Object Deletion"
    private static final String confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
    private static Node node;
    
    public void setUp() {
        System.out.println("### "+getName()+" ###");  // NOI18N
        if(node == null) {
            node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
        }
    }
    
    public void tearDown() {
        new JDialogOperator(confirmTitle).close();
        // On some linux it may happen autorepeat is activated and it 
        // opens dialog multiple times. So, we need to close all modal dialogs.
        // See issue http://www.netbeans.org/issues/show_bug.cgi?id=56672.
        closeAllModal();
    }
    
    /** Test performPopup */
    public void testPerformPopup() {
        new DeleteAction().performPopup(node);
    }
    
    /** Test performMenu */
    public void testPerformMenu() {
        new DeleteAction().performMenu(node);
    }
    
    /** Test performAPI.  */
    public void testPerformAPI() {
        new DeleteAction().performAPI(node);
    }
    
    /** Test performShortcut. */
    public void testPerformShortcut() {
        new DeleteAction().performShortcut(node);
    }
}
