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
import javax.swing.JRadioButton;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JRadioButtonOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JRadioButtonOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the radio button.
     */
    private JRadioButton radioButton;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JRadioButtonOperatorTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup for testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        radioButton = new JRadioButton("JRadioButtonOperatorTest");
        radioButton.setName("JRadioButtonOperatorTest");
        frame.getContentPane().add(radioButton);
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
        TestSuite suite = new TestSuite(JRadioButtonOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JRadioButtonOperator operator1 = new JRadioButtonOperator(operator);
        assertNotNull(operator1);
        
        JRadioButtonOperator operator2 = new JRadioButtonOperator(operator, "JRadioButtonOperatorTest");
        assertNotNull(operator2);
        
        JRadioButtonOperator operator3 = new JRadioButtonOperator(operator, new NameComponentChooser("JRadioButtonOperatorTest"));
        assertNotNull(operator3);
        
        JRadioButtonOperator operator4 = new JRadioButtonOperator(radioButton);
        assertNotNull(operator4);
    }
    
    /**
     * Test findJRadioButton method.
     */
    public void testFindJRadioButton() {
        frame.setVisible(true);

        JRadioButton radioButton1 = JRadioButtonOperator.findJRadioButton(frame, "JRadioButtonOperatorTest", false, false);
        assertNotNull(radioButton1);
        
        JRadioButton radioButton2 = JRadioButtonOperator.findJRadioButton(frame, new NameComponentChooser("JRadioButtonOperatorTest"));
        assertNotNull(radioButton2);
    }
    
    /**
     * Test waitJRadioButton method.
     */
    public void testWaitJRadioButton() {
        frame.setVisible(true);

        JRadioButton radioButton1 = JRadioButtonOperator.waitJRadioButton(frame, "JRadioButtonOperatorTest", false, false);
        assertNotNull(radioButton1);
        
        JRadioButton radioButton2 = JRadioButtonOperator.waitJRadioButton(frame, new NameComponentChooser("JRadioButtonOperatorTest"));
        assertNotNull(radioButton2);
    }
}
