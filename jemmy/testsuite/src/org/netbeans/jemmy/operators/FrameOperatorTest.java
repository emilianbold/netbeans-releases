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

import java.awt.Frame;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for FrameOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class FrameOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public FrameOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a major problem occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        frame.setTitle("FrameOperatorTest");
        frame.setName("FrameOperatorTest");
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when a major problem occurs.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
        frame.dispose();
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(FrameOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        FrameOperator operator2 = new FrameOperator(new NameComponentChooser("FrameOperatorTest"));
        assertNotNull(operator2);
        
        FrameOperator operator3 = new FrameOperator("FrameOperatorTest");
        assertNotNull(operator3);
    }

    /**
     * Test waitTitle method.
     */
    public void testWaitTitle() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);

        operator.setTitle("Title");
        operator.waitTitle("Title");
    }

    /**
     * Test iconify method.
     */
    public void testIconify() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.iconify();
    }

    /**
     * Test deiconify method.
     */
    public void testDeiconify() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.deiconify();
    }

    /**
     * Test maximize method.
     */
    public void testMaximize() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.maximize();
    }

    /**
     * Test demaximize method.
     */
    public void testDemaximize() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.demaximize();
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.getDump();
    }

    /**
     * Test setIconImage method.
     */
    public void testSetIconImage() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.setIconImage(operator.getIconImage());
    }

    /**
     * Test setMenuBar method.
     */
    public void testSetMenuBar() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.setMenuBar(operator.getMenuBar());
    }

    /**
     * Test setResizable method.
     */
    public void testSetResizable() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.setResizable(operator.isResizable());
    }

    /**
     * Test setState method.
     */
    public void testSetState() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.setState(operator.getState());
    }

    /**
     * Test setTitle method.
     */
    public void testSetTitle() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        operator.setTitle(operator.getTitle());
    }
}
