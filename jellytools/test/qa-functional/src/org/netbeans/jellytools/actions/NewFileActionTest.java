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
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbTestSuite;

/** Test org.netbeans.jellytools.actions.NewFileAction
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class NewFileActionTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public NewFileActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new NewFileActionTest("testPerformPopup"));
        suite.addTest(new NewFileActionTest("testPerformMenu"));
        suite.addTest(new NewFileActionTest("testPerformAPI"));
        suite.addTest(new NewFileActionTest("testPerformShortcut"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Test performPopup */
    public void testPerformPopup() {
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        new NewFileAction().performPopup(node);
        new NewFileWizardOperator().close();
        // test constructor with parameter
        String javaClassLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes/Class.java");
        new NewFileAction(javaClassLabel).performPopup(node);
        new WizardOperator(javaClassLabel).close();
    }
    
    /** Test performMenu */
    public void testPerformMenu() {
        new NewFileAction().performMenu();
        new NewFileWizardOperator().close();
    }
    
    /** Test performAPI */
    public void testPerformAPI() {
        new NewFileAction().performAPI();
        new NewFileWizardOperator().close();
    }
    
    /** Test performShortcut */
    public void testPerformShortcut() {
        new NewFileAction().performShortcut();
        new NewFileWizardOperator().close();
        // On some linux it may happen autorepeat is activated and it 
        // opens dialog multiple times. So, we need to close all modal dialogs.
        // See issue http://www.netbeans.org/issues/show_bug.cgi?id=56672.
        closeAllModal();
    }
    
}
