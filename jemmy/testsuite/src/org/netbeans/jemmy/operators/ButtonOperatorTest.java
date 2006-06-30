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

import java.awt.Button;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class ButtonOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private Frame frame;
    
    /**
     * Stores the button we use for testing.
     */
    private Button button;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public ButtonOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        button = new Button("ButtonOperatorTest");
        button.setName("ButtonOperatorTest");
        
        frame.add(button);
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
     *
     * @return the test suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(ButtonOperatorTest.class);
        return suite;
    }
    
    /**
     * Test constructor method.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);
        
        ButtonOperator operator2 = new ButtonOperator(operator, new NameComponentChooser("ButtonOperatorTest"));
        assertNotNull(operator2);
        
        ButtonOperator operator3 = new ButtonOperator(operator, "ButtonOperatorTest");
        assertNotNull(operator3);
    }
    
    /**
     * Test findButton method.
     */
    public void testFindButton() {
        frame.setVisible(true);
        
        Button button1 = ButtonOperator.findButton(frame, "ButtonOperatorTest", false, false);
        assertNotNull(button1);

        Button button2 = ButtonOperator.findButton(frame, new NameComponentChooser("ButtonOperatorTest"));
        assertNotNull(button2);
    }
    
    /**
     * Test waitButton method.
     */
    public void testWaitButton() {
        frame.setVisible(true);
        
        Button button1 = ButtonOperator.waitButton(frame, "ButtonOperatorTest", false, false);
        assertNotNull(button1);

        Button button2 = ButtonOperator.waitButton(frame, new NameComponentChooser("ButtonOperatorTest"));
        assertNotNull(button2);
    }
    
    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);

        Hashtable hashtable1 = operator1.getDump();
        assertNotNull(hashtable1.get("Label"));
    }
    
    /**
     * Test getActionCommand method.
     */
    public void testGetActionCommand() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setActionCommand("TEST");
        assertEquals("TEST", operator1.getActionCommand());
    }

    /**
     * Test getLabel method.
     */
    public void testGetLabel() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);

        operator1.setLabel("TEST");
        assertEquals("TEST", operator1.getLabel());
    }
    
    /**
     * Test addActionListener method.
     */
    public void testAddActionListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);

        operator1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            }
        });
        assertTrue(button.getActionListeners().length == 1);
        
        operator1.removeActionListener(button.getActionListeners()[0]);
        assertTrue(button.getActionListeners().length == 0);
    }
    
    /**
     * Test push method.
     */
    public void testPush() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);
        
        operator1.push();
    }
    
    /**
     * Test pushNoBlock method.
     */
    public void testPushNoBlock() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);

        operator1.pushNoBlock();
        operator1.push();
    }
    
    /**
     * Test press/release method.
     */
    public void testRelease() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ButtonOperator operator1 = new ButtonOperator(operator);
        assertNotNull(operator1);

        operator1.press();
        operator1.release();
    }
}
