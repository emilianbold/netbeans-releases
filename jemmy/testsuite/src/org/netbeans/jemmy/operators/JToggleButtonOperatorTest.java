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
import javax.swing.JToggleButton;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JToggleButtonOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JToggleButtonOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private JFrame frame;
    
    /**
     * Stores the toggle button we use for testing.
     */
    private JToggleButton toggleButton;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JToggleButtonOperatorTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup for testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        toggleButton = new JToggleButton("JToggleButtonOperatorTest");
        toggleButton.setName("JToggleButtonOperatorTest");
        frame.add(toggleButton);
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
        TestSuite suite = new TestSuite(JToggleButtonOperatorTest.class);
        
        return suite;
    }

    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JToggleButtonOperator operator2 = new JToggleButtonOperator(operator1);
        assertNotNull(operator2);
        
        JToggleButtonOperator operator3 = new JToggleButtonOperator(operator1, new NameComponentChooser("JToggleButtonOperatorTest"));
        assertNotNull(operator3);

        JToggleButtonOperator operator4 = new JToggleButtonOperator(operator1, "JToggleButtonOperatorTest");
        assertNotNull(operator4);
    }
    
    /**
     * Test findJToggleButton method.
     */
    public void testFindJToggleButton() {
        frame.setVisible(true);
        
        JToggleButton toggleButton1 = JToggleButtonOperator.findJToggleButton(frame, "JToggleButtonOperatorTest", false, false);
        assertNotNull(toggleButton1);

        JToggleButton toggleButton2 = JToggleButtonOperator.findJToggleButton(frame, new NameComponentChooser("JToggleButtonOperatorTest"));
        assertNotNull(toggleButton2);
    }
    
    /**
     * Test waitJToggleButton method.
     */
    public void testWaitJToggleButton() {
        frame.setVisible(true);
        
        JToggleButton toggleButton1 = JToggleButtonOperator.waitJToggleButton(frame, "JToggleButtonOperatorTest", false, false);
        assertNotNull(toggleButton1);

        JToggleButton toggleButton2 = JToggleButtonOperator.waitJToggleButton(frame, new NameComponentChooser("JToggleButtonOperatorTest"));
        assertNotNull(toggleButton2);
    }
    
    /**
     * Test prepareToClick method.
     */
    public void testPrepareToClick() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JToggleButtonOperator operator2 = new JToggleButtonOperator(operator1);
        assertNotNull(operator2);
        
        operator2.makeComponentVisible();
    }
}
