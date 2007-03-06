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

import java.awt.Toolkit;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.junit.NbTestSuite;

/** Test org.netbeans.jellytools.action.CopyAction
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class CopyActionTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public CopyActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new CopyActionTest("testPerformPopup"));
        suite.addTest(new CopyActionTest("testPerformMenu"));
        suite.addTest(new CopyActionTest("testPerformAPI"));
        suite.addTest(new CopyActionTest("testPerformShortcut"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    private Object clipboard1;
    private static Node node;
    
    public void setUp() {
        System.out.println("### "+getName()+" ###");  // NOI18N
        clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if(node == null) {
            node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java");
        }
    }
    
    public void tearDown() throws Exception {
        Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                    Object clipboard2 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                    return clipboard1 != clipboard2 ? Boolean.TRUE : null;
                }
                public String getDescription() {
                    return("Wait clipboard contains data");
                }
        });
        waiter.waitAction(null);
    }
    
    /** Test performPopup  */
    public void testPerformPopup() {
        new CopyAction().performPopup(node);
    }
    
    /** Test performMenu */
    public void testPerformMenu() {
        new CopyAction().performMenu(node);
    }
    
    /** Test performAPI */
    public void testPerformAPI() {
        new CopyAction().performAPI(node);
    }
    
    /** Test performShortcut  */
    public void testPerformShortcut() {
        new CopyAction().performShortcut(node);
    }
    
}
