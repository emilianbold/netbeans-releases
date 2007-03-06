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

import java.awt.event.KeyEvent;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.junit.NbTestSuite;

/** Test of HelpOperator.
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class HelpOperatorTest extends JellyTestCase {

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public HelpOperatorTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new HelpOperatorTest("testInvoke"));
        suite.addTest(new HelpOperatorTest("testContentsSelection"));
        suite.addTest(new HelpOperatorTest("testIndexFind"));
        suite.addTest(new HelpOperatorTest("testSearchFind"));
        suite.addTest(new HelpOperatorTest("testPreviousAndNext"));
        suite.addTest(new HelpOperatorTest("testPrint"));
        suite.addTest(new HelpOperatorTest("testPageSetup"));
        suite.addTest(new HelpOperatorTest("testClose"));
        return suite;
    }
    
    /** Print out test name. */
    public void setUp() {
        System.out.println("### "+getName()+" ###");
        // find help window if not found before
        if(help == null && !getName().equals("testInvoke")) {
            help = new HelpOperator();
        }
    }
    
    /** method called after each testcase
     */
    protected void tearDown() {
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    private static HelpOperator help;
    
    /** Test invoke  */
    public void testInvoke() {
        // push Escape key to close potentially open popup menu from previous execution
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        long oldTimeout = JemmyProperties.getCurrentTimeout("JMenuOperator.PushMenuTimeout");
        // increase time to open help window
        JemmyProperties.setCurrentTimeout("JMenuOperator.PushMenuTimeout", 60000);
        try {
            help = HelpOperator.invoke();
        } finally {
            // reset timeout
            JemmyProperties.setCurrentTimeout("JMenuOperator.PushMenuTimeout", oldTimeout);
        }
    }
    
    /** simple test case
     */
    public void testContentsSelection() {
        String text = help.getContentText();
        help.treeContents().selectRow(0);
        new EventTool().waitNoEvent(500);
        assertTrue(!text.equals(help.getContentText()));
    }
    
    /** simple test case
     */
    public void testIndexFind() {
        // first enter selects category in the tree
        help.indexFind("help");
        // second enter shows content
        help.indexFind("help");
        new EventTool().waitNoEvent(500);
        String text=help.getContentText();
        help.indexFind("menu");
        help.indexFind("menu");
        new EventTool().waitNoEvent(500);
        assertTrue(!text.equals(help.getContentText()));
    }
    
    /** simple test case
     */
    public void testSearchFind() {
        help.searchFind("help");
        new EventTool().waitNoEvent(500);
        String text=help.getContentText();
        help.searchFind("menu");
        new EventTool().waitNoEvent(500);
        assertTrue(!text.equals(help.getContentText()));
    }
    
    /** simple test case
     */
    public void testPreviousAndNext() throws InterruptedException {
        final String text = help.getContentText();
        help.back();
        new Waiter(new Waitable() {
            public Object actionProduced(Object oper) {
                return text.equals(help.getContentText()) ? null : Boolean.TRUE;
            }
            public String getDescription() {
                return("Text after back not equal to previous text"); // NOI18N
            }
        }).waitAction(null);
        help.next();
        new Waiter(new Waitable() {
            public Object actionProduced(Object oper) {
                return text.equals(help.getContentText()) ? Boolean.TRUE : null;
            }
            public String getDescription() {
                return("Text after next equal to previous text"); // NOI18N
            }
        }).waitAction(null);
    }
    
    /** simple test case
     */
    public void testPrint() {
        assertEquals("Print", help.btPrint().getToolTipText());
    }
    
    /** simple test case
     */
    public void testPageSetup() {
        assertEquals("Page Setup", help.btPageSetup().getToolTipText());
    }
    
    /** simple test case
     */
    public void testClose() {
        help.close();
    }
    
}
