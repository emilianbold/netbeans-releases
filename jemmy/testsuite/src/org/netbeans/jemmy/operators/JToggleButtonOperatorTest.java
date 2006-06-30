/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
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
 * "Portions Copyrighted [year] [name of copyright owner]".
 *
 * The Original Software is the Jemmy library. The Initial Developer of the
 * Original Software is Alexandre Iline. All Rights Reserved.
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
        frame.getContentPane().add(toggleButton);
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
        
        operator2.prepareToClick();
        assertTrue(toggleButton.isVisible());
    }
}
