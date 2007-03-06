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
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;

/** Test org.netbeans.jellytools.actions.RenameAction
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class RenameActionTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public RenameActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new RenameActionTest("testPerformPopup"));
        suite.addTest(new RenameActionTest("testPerformAPI"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    private static final String RENAME_TITLE = Bundle.getString("org.openide.actions.Bundle", "CTL_RenameTitle");
    
    /** Test performPopup */
    public void testPerformPopup() {
        Node node = new Node(new FilesTabOperator().getProjectNode("SampleProject"), "build.xml"); // NOI18N
        new RenameAction().performPopup(node);
        new JDialogOperator(RENAME_TITLE).close();
    }
    
    /** Test performAPI */
    public void testPerformAPI() {
        Node node = new Node(new FilesTabOperator().getProjectNode("SampleProject"), "src|sample1|SampleClass1.java"); // NOI18N
        new RenameAction().performAPI(node);
        new JDialogOperator(RENAME_TITLE).close();
    }
    
}
