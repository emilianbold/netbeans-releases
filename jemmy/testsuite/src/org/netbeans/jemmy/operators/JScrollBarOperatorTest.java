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

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * A JUnit test for JScrollBarOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JScrollBarOperatorTest extends TestCase {
    /**
     * Stores the frame we use.
     */
    private JFrame frame;
    
    /**
     * Stores the scroll bar.
     */
    private JScrollBar scrollBar;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JScrollBarOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        scrollBar = new JScrollBar();
        frame.getContentPane().add(scrollBar);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
        frame.dispose();
        frame = null;
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(JScrollBarOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        
    }

    /**
     * Test findJScrollBar method.
     */
    public void testFindJScrollBar() {
    }

    /**
     * Test waitJScrollBar method.
     */
    public void testWaitJScrollBar() {
        
    }

    /**
     * Test scroll method.
     */
    public void testScroll() {
        
    }

    /**
     * Test scrollTo method.
     */
    public void testScrollTo() {
        
    }

    /**
     * Test scrollToValue method.
     */
    public void testScrollToValue() {
        
    }

    /**
     * Test scrollToMinimum method.
     */
    public void testScrollToMinimum() {
        
    }

    /**
     * Test scrollToMaximum method.
     */
    public void testScrollToMaximum() {
        
    }

    /**
     * Test getDecreaseButton method.
     */
    public void testGetDecreaseButton() {
        
    }

    /**
     * Test getIncreaseButton method.
     */
    public void testGetIncreaseButton() {
        
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        
    }

    /**
     * Test addAdjustmentListener method.
     */
    public void testAddAdjustmentListener() {
        
    }

    /**
     * Test getBlockIncrement method.
     */
    public void testGetBlockIncrement() {
        
    }

    /**
     * Test getMaximum method.
     */
    public void testGetMaximum() {
        
    }

    /**
     * Test getMinimum method.
     */
    public void testGetMinimum() {
        
    }

    /**
     * Test getModel method.
     */
    public void testGetModel() {
        
    }

    /**
     * Test getOrientation method.
     */
    public void testGetOrientation() {
        
    }

    /**
     * Test getUI method.
     */
    public void testGetUI() {
        
    }

    /**
     * Test getUnitIncrement method.
     */
    public void testGetUnitIncrement() {
        
    }

    /**
     * Test getValue method.
     */
    public void testGetValue() {
        
    }

    /**
     * Test getValueIsAdjusting method.
     */
    public void testGetValueIsAdjusting() {
        
    }

    /**
     * Test getVisibleAmount method.
     */
    public void testGetVisibleAmount() {
        
    }

    /**
     * Test removeAdjustmentListener method.
     */
    public void testRemoveAdjustmentListener() {
        
    }

    /**
     * Test setBlockIncrement method.
     */
    public void testSetBlockIncrement() {
        
    }

    /**
     * Test setMaximum method.
     */
    public void testSetMaximum() {
        
    }

    /**
     * Test setMinimum method.
     */
    public void testSetMinimum() {
        
    }

    /**
     * Test setModel method.
     */
    public void testSetModel() {
        
    }

    /**
     * Test setOrientation method.
     */
    public void testSetOrientation() {
        
    }

    /**
     * Test setUnitIncrement method.
     */
    public void testSetUnitIncrement() {
        
    }

    /**
     * Test setValue method.
     */
    public void testSetValue() {
        
    }

    /**
     * Test setValueIsAdjusting method.
     */
    public void testSetValueIsAdjusting() {
        
    }

    /**
     * Test setValues method.
     */
    public void testSetValues() {
        
    }

    /**
     * Test setVisibleAmount method.
     */
    public void testSetVisibleAmount() {
        
    }
}
