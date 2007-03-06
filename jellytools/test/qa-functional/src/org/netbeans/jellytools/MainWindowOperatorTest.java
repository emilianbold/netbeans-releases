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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import junit.textui.TestRunner;
import org.netbeans.jemmy.ComponentChooser;

import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import org.netbeans.junit.NbTestSuite;
import org.openide.awt.StatusDisplayer;
import org.openide.awt.Toolbar;

/**
 * Test of org.netbeans.jellytools.MainWindowOperator.
 */
public class MainWindowOperatorTest extends JellyTestCase {
    
    /** Instance of MainWindowOperator (singleton) to test. */
    private MainWindowOperator mainWindowOper;
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public MainWindowOperatorTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     * @return Test suite.
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(MainWindowOperatorTest.class);
        return suite;
    }
    
    
    /** Redirect output to log files, wait before each test case and
     * show dialog to test. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        mainWindowOper = MainWindowOperator.getDefault();
    }
    
    /** Tear down after test case. */
    protected void tearDown() {
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Test of getDefault() method. */
    public void testGetDefault() {
        MainWindowOperator.getDefault();
    }
    
    /** Test of testMenuBar method. */
    public void testMenuBar() {
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.menuBar();
    }
    
    /** Test of isMDI method. Tries to find second JFrame to decide whether IDE
     * is in joined mode. If not found then only main window exists. */
    public void testIsMDI() {
        final boolean isMDI = JFrameOperator.findJFrame(ComponentSearcher.getTrueChooser("JFrame"), 1) == null;
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                assertEquals("Wrong value from isMDI().", MainWindowOperator.getDefault().isMDI(), isMDI);
            }
        });
    }
    
    /** Test of isMDI method. Tries to find second JFrame to decide whether IDE
     * is in joined mode. If not found then only main window exists. */
    public void testIsCompactMode() {
        boolean isCompact = JFrameOperator.findJFrame(ComponentSearcher.getTrueChooser("JFrame"), 1) == null;
        assertEquals("Wrong value from isCompactMode().", mainWindowOper.isCompactMode(), isCompact);
    }

    /** Test of setMDI method. 
     * @deprecated setMDI() method replaced by setCompactMode() in recent builds
     */
    public void testSetMDI() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainWindowOperator mainWindowOper = MainWindowOperator.getDefault();
                boolean wasMDI = mainWindowOper.isMDI();
                // switch to SDI, if MDI is current
                if(mainWindowOper.isMDI()) {
                    mainWindowOper.setSDI();
                }
                mainWindowOper.setMDI();
                boolean isMDI = mainWindowOper.isMDI();
                // if at the beggining of the test was IDE in SDI, switch back
                if(!wasMDI) {
                    mainWindowOper.setSDI();
                }
                assertTrue("Not switched to MDI", isMDI);
            }
        });
    }

    /** Test of setMDI method. */
    public void testSetCompactMode() {
        boolean wasCompact = mainWindowOper.isCompactMode();
        // switch to Separate, if Compact is current
        if(mainWindowOper.isCompactMode()) {
            mainWindowOper.setSeparateMode();
        }
        mainWindowOper.setCompactMode();
        boolean isCompact = mainWindowOper.isCompactMode();
        // if at the beggining of the test was IDE in Separate, switch back
        if(!wasCompact) {
            mainWindowOper.setSeparateMode();
        }
        assertTrue("Not switched to Compact Window Mode", isCompact);
    }
    
    /** Test of setSDI method. 
     * @deprecated setSDI() method replaced by setSeparateMode() in recent builds
     */
    public void testSetSDI() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                boolean wasMDI = mainWindowOper.isMDI();
                // switch to MDI, if SDI is current
                if(!mainWindowOper.isMDI()) {
                    mainWindowOper.setMDI();
                }
                mainWindowOper.setSDI();
                boolean isMDI = mainWindowOper.isMDI();
                // if at the beggining of the test was IDE in MDI, switch back
                if(wasMDI) {
                    mainWindowOper.setMDI();
                }
                assertTrue("Not switched to SDI", !isMDI);
            }
        });
    }

    /** Test of setSeparateMode method. */
    public void testSetSeparateMode() {
        boolean wasCompact = mainWindowOper.isCompactMode();
        // switch to Compact, if Separate is current
        if(!mainWindowOper.isCompactMode()) {
            mainWindowOper.setCompactMode();
        }
        mainWindowOper.setSeparateMode();
        boolean isCompact = mainWindowOper.isCompactMode();
        // if at the beggining of the test was IDE in Compact, switch back
        if(wasCompact) {
            mainWindowOper.setCompactMode();
        }
        assertTrue("Not switched to Separate Window Mode", !isCompact);
    }
    
    /** Test of getStatusText method. */
    public void testGetSetStatusText() {
        String expectedText = "Hello World!!!";
        mainWindowOper.setStatusText(expectedText);
        String text = mainWindowOper.getStatusText();
        assertEquals("Wrong status text.", expectedText, text);
    }
    
    /** Test of waitStatusText method. */
    public void testWaitStatusText() {
        String expectedText = "Hello World!!!";
        StatusDisplayer.getDefault().setStatusText(expectedText);
        mainWindowOper.waitStatusText(expectedText);
    }
    
    /***************** methods for toolbars manipulation *******************/
    
    /** Test of getToolbar(int) method. */
    public void testGetToolbarInt() {
        mainWindowOper.getToolbar(0);
    }
    
    /** Test of getToolbar(String) method. */
    public void testGetToolbarString() {
        mainWindowOper.getToolbar("Build"); // NOI18N
    }
    
    /** Test of getToolbarCount method. */
    public void testGetToolbarCount() {
        assertEquals("Wrong toolbar count.", 6, mainWindowOper.getToolbarCount());
    }
    
    /** Test of getToolbarName method. */
    public void testGetToolbarName() {
        String toolbarName = mainWindowOper.getToolbarName(0);
        String expected = ((Toolbar)mainWindowOper.getToolbar(0).getSource()).getDisplayName();
        assertEquals("Wrong toolbar name", expected, toolbarName);
    }
    
    /** Test of getToolbarButton method. Finds Build toolbar and checks if
     * getToolbarButton(1) returns Build Main Project button. */
    public void testGetToolbarButtonInt() {
        ContainerOperator toolbarOper = mainWindowOper.getToolbar("Build"); // NOI18N
        String tooltip = mainWindowOper.getToolbarButton(toolbarOper, 1).getToolTipText();
        String expected = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_BuildMainProjectAction_Name");
        assertTrue("Wrong toolbar button.", tooltip.indexOf(expected) != -1);
    }
    
    /** Test of getToolbarButton method. Finds Build toolbar and checks if
     * getToolbarButton() finds Build All button. */
    public void testGetToolbarButtonString() {
        ContainerOperator toolbarOper = mainWindowOper.getToolbar("Build"); // NOI18N
        String buildMainProject = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_BuildMainProjectAction_Name");
        mainWindowOper.getToolbarButton(toolbarOper, buildMainProject);
    }

    /** Test of pushToolbarPopupMenu method. Pushes popup menu Edit
     * checks whether toolbar Edit dismissed and push again to enable it. */
    public void testPushToolbarPopupMenu() {
        int expectedToolbarsCount = mainWindowOper.getToolbarCount();
        // "File"
        String popupPath = Bundle.getString("org.netbeans.core.Bundle", "Toolbars/File");
        mainWindowOper.pushToolbarPopupMenu(popupPath);
        int actualToolbarCount = mainWindowOper.getToolbarCount();
        mainWindowOper.pushToolbarPopupMenu(popupPath);
        assertEquals("Toolbar popup menu not pushed. Toolbars count should differ:", expectedToolbarsCount, actualToolbarCount+1);
    }
    
    /** Test of pushToolbarPopupMenuNoBlock method.  */
    public void testPushToolbarPopupMenuNoBlock() {
        // at the time no item in menu is blocking so we use testPushToolbarPopupMenu
        int expectedToolbarsCount = mainWindowOper.getToolbarCount();
        // "File"
        String popupPath = Bundle.getString("org.netbeans.core.Bundle", "Toolbars/File");
        mainWindowOper.pushToolbarPopupMenuNoBlock(popupPath);
        new EventTool().waitNoEvent(500);
        int actualToolbarCount = mainWindowOper.getToolbarCount();
        mainWindowOper.pushToolbarPopupMenu(popupPath);
        assertEquals("Toolbar popup menu not pushed. Toolbars count should differ:", expectedToolbarsCount, actualToolbarCount+1);
    }

    /** Test of dragNDropToolbar method. Tries to move toolbar down and checks
     * whether main window is enlarged. */
    public void testDragNDropToolbar() {
        // need toolbar container to check drag and drop operation
        Component toolbarPool = mainWindowOper.findSubComponent(new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                return comp.getClass().getName().equals("org.openide.awt.ToolbarPool");
            }
            
            public String getDescription() {
                return "org.openide.awt.ToolbarPool";
            }
        });
        ContainerOperator toolbarOper = mainWindowOper.getToolbar(0);
        int heightOrig = toolbarPool.getHeight();
        mainWindowOper.dragNDropToolbar(toolbarOper, 0, 100);
        assertTrue("Toolbar not moved down - main window height the same.", 
                   heightOrig != toolbarPool.getHeight());
    }

    /** Test of MainWindowOperator.StatusTextTracer class. */
    public void testStatusTextTracer() {
        MainWindowOperator.StatusTextTracer stt = mainWindowOper.getStatusTextTracer();
        stt.start();
        // simulate compile action which produces at least two messages: "Compiling ..." and
        // "Finished ..."
        StatusDisplayer.getDefault().setStatusText("Compiling");
        StatusDisplayer.getDefault().setStatusText("Finished");
        //new CompileAction().performAPI();
        // waits for "Compiling" status text
        stt.waitText("Compiling");
        // waits for "Finished" status text
        stt.waitText("Finished");
        
        // order is not significant => following works as well
        stt.waitText("Finished");
        stt.waitText("Compiling");

        ArrayList list = stt.getStatusTextHistory();
        assertEquals("Method getStatusTextHistory returns wrong ArrayList.",
                                                       "Compiling", list.get(0));
        assertEquals("Method getStatusTextHistory returns wrong ArrayList.",
                                                        "Finished", list.get(1));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stt.printStatusTextHistory(new PrintStream(stream));
        assertTrue("Method printStatusTextHistory prints wrong values.", 
                                    stream.toString().indexOf("Compiling") > -1);  // NOI18N
        assertTrue("Method printStatusTextHistory prints wrong values.", 
                                    stream.toString().indexOf("Finished") > -1);  // NOI18N

        // to be order significant, set removedCompared parameter to true
        stt.waitText("Compiling", true);
        stt.waitText("Finished", true);

        // history was removed by above methods => need to produce a new messages
        StatusDisplayer.getDefault().setStatusText("Compiling");
        StatusDisplayer.getDefault().setStatusText("Finished");
        //new CompileAction().performAPI();

        // order is significant if removedCompared parameter is true =>
        // => following fails because Finished is shown as second
        stt.waitText("Finished", true);
        long oldTimeout = JemmyProperties.getCurrentTimeout("Waiter.WaitingTime");
        try {
            JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", 3000);
            stt.waitText("Compiling", true);
            fail("waitText() should fail because of wrong order.");
        } catch (JemmyException e) {
            // OK. It fails.
        } finally {
            JemmyProperties.setCurrentTimeout("Waiter.WaitingTime", oldTimeout);
        }

        String expectedText = "Should be traced.";
        StatusDisplayer.getDefault().setStatusText(expectedText);
        // stop tracing
        stt.stop();
        assertTrue("Text \""+expectedText+"\" not traced.", stt.contains(expectedText, false));
        stt.clear();
        assertTrue("clear() doesn't work.", !stt.contains(expectedText, false));
        expectedText = "Should not be traced.";
        StatusDisplayer.getDefault().setStatusText(expectedText);
        assertTrue("stop() doesn't work. Text \""+expectedText+"\" should not be traced.", 
                   !stt.contains(expectedText, false));
    }
}