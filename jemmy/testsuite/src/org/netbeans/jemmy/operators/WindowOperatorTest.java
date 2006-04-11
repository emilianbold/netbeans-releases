/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version 1.0
 * (the "License"). You may not use this file except in compliance with the
 * License. A copy of the License is available at http://www.sun.com/.
 *
 * The Original Code is the Jemmy library. The Initial Developer of the
 * Original Code is Alexandre Iline. All Rights Reserved.
 *
 * ---------------------------------------------------------------------------
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy.operators;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for WindowOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class WindowOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the sub window.
     */
    private Dialog subWindow;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public WindowOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        frame.setName("WindowOperatorTest");
        frame.setLocationRelativeTo(null);
        
        subWindow = new Dialog(frame, false);
        subWindow.setName("SubWindow");
        subWindow.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
        frame.dispose();
        frame = null;
        
        subWindow.setVisible(false);
        subWindow.dispose();
        subWindow = null;
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(WindowOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        subWindow.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);
        
        FrameOperator operator1 = new FrameOperator();
        assertNotNull(operator1);
        
        WindowOperator operator2 = new WindowOperator(operator1);
        assertNotNull(operator2);
        
        WindowOperator operator3 = new WindowOperator(operator1, new NameComponentChooser("SubWindow"));
        assertNotNull(operator3);
    }

    /**
     * Test findWindow method.
     */
    public void testFindWindow() {
        frame.setVisible(true);
        subWindow.setVisible(true);
        
        Window window1 = WindowOperator.findWindow(new NameComponentChooser("WindowOperatorTest"));
        assertNotNull(window1);
        
        Window window2 = WindowOperator.findWindow(frame, new NameComponentChooser("SubWindow"));
        assertNotNull(window2);
    }

    /**
     * Test waitWindow method.
     */
    public void testWaitWindow() {
        frame.setVisible(true);
        subWindow.setVisible(true);
        
        Window window1 = WindowOperator.waitWindow(new NameComponentChooser("WindowOperatorTest"));
        assertNotNull(window1);
        
        Window window2 = WindowOperator.waitWindow(frame, new NameComponentChooser("SubWindow"));
        assertNotNull(window2);
    }

    /**
     * Test activate method.
     */
    public void testActivate() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);
        
        operator.activate();
    }

    /**
     * Test close method.
     */
    public void testClose() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.close();
    }

    /**
     * Test move method.
     */
    public void testMove() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.move(100, 100);
    }

    /**
     * Test resize method.
     */
    public void testResize() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);
        
        operator.resize(100, 100);
    }

    /**
     * Test findSubWindow method.
     */
    public void testFindSubWindow() {
        frame.setVisible(true);
        subWindow.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        Window window = operator.findSubWindow(new NameComponentChooser("SubWindow"));
        assertNotNull(window);
    }

    /**
     * Test waitSubWindow method.
     */
    public void testWaitSubWindow() {
        frame.setVisible(true);
        subWindow.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        Window window = operator.waitSubWindow(new NameComponentChooser("SubWindow"));
        assertNotNull(window);
    }

    /**
     * Test waitClosed method.
     */
    public void testWaitClosed() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.close();
        operator.waitClosed();
    }

    /**
     * Test addWindowListener method.
     */
    public void testAddWindowListener() {
        
    }

    /**
     * Test applyResourceBundle method.
     */
    public void testApplyResourceBundle() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        try {
            operator.applyResourceBundle("");
        }
        catch(Exception exception) {
        }
    }

    /**
     * Test dispose method.
     */
    public void testDispose() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.dispose();
    }

    /**
     * Test getFocusOwner method.
     */
    public void testGetFocusOwner() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.getFocusOwner();
    }

    /**
     * Test getOwnedWindows method.
     */
    public void testGetOwnedWindows() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.getOwnedWindows();
    }

    /**
     * Test getOwner method.
     */
    public void testGetOwner() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.getOwner();
    }

    /**
     * Test getWarningString method.
     */
    public void testGetWarningString() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.getWarningString();
    }

    /**
     * Test pack method.
     */
    public void testPack() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.pack();
    }

    /**
     * Test removeWindowListener method.
     */
    public void testRemoveWindowListener() {
        
    }

    /**
     * Test toBack method.
     */
    public void testToBack() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.toBack();
    }

    /**
     * Test toFront method.
     */
    public void testToFront() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.toFront();
    }

    /**
     * Test isFocused method.
     */
    public void testIsFocused() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.isFocused();
    }

    /**
     * Test isActive method.
     */
    public void testIsActive() {
        frame.setVisible(true);
        
        WindowOperator operator = new WindowOperator();
        assertNotNull(operator);

        operator.isActive();
    }
}
