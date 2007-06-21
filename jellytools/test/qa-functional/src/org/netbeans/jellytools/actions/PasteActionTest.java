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
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbTestSuite;

/** Test org.netbeans.jellytools.actions.PasteAction
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class PasteActionTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public PasteActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new PasteActionTest("testPerformPopup"));
        suite.addTest(new PasteActionTest("testPerformMenu"));
        suite.addTest(new PasteActionTest("testPerformAPI"));
        suite.addTest(new PasteActionTest("testPerformShortcut"));
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
    private static Node sample1Node;
    private static final String SAMPLE_FILE = "properties.properties";  //NOI18N
    private static final String PASTED_FILE = "properties1.properties";  //NOI18N
    
    public void setUp() {
        System.out.println("### "+getName()+" ###");  // NOI18N
        if(sample1Node == null) {
            sample1Node = new Node(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        }
        new CopyAction().perform(new Node(sample1Node, SAMPLE_FILE));
    }
    
    public void tearDown() {
        Node pastedNode = new Node(sample1Node, PASTED_FILE);
        new DeleteAction().perform(pastedNode); 
        new NbDialogOperator(confirmTitle).yes();
        pastedNode.waitNotPresent();
    }

    /** Test performPopup  */
    public void testPerformPopup() {
        new PasteAction().performPopup(sample1Node);
    }
    
    /** Test performMenu  */
    public void testPerformMenu() {
        new PasteAction().performMenu(sample1Node);
    }
    
    /** Test performAPI */
    public void testPerformAPI() {
        new PasteAction().performAPI(sample1Node);
    }
    
    /** Test performShortcut */
    public void testPerformShortcut() {
        new PasteAction().performShortcut(sample1Node);
    }
}
