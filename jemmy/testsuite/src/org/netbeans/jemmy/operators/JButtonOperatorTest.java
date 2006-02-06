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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;
import javax.swing.JButton;
import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JButtonOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JButtonOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private JFrame frame;
    
    /**
     * Stores the button we use for testing.
     */
    private JButton button;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JButtonOperatorTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup for testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        button = new JButton("JButtonOperatorTest");
        button.setName("JButtonOperatorTest");
        frame.add(button);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }
    
    /**
     * Cleanup for testing.
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
        TestSuite suite = new TestSuite(JButtonOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor method.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        JButtonOperator operator2 = new JButtonOperator(operator1);
        assertNotNull(operator2);
        
        JButtonOperator operator3 = new JButtonOperator(operator1, new NameComponentChooser("JButtonOperatorTest"));
        assertNotNull(operator3);

        JButtonOperator operator4 = new JButtonOperator(operator1, "JButtonOperatorTest");
        assertNotNull(operator4);
    }
    
    /**
     * Test findJButton method.
     */
    public void testFindJButton() {
        frame.setVisible(true);
        
        JButton button1 = JButtonOperator.findJButton(frame, new NameComponentChooser("JButtonOperatorTest"));
        assertNotNull(button1);
        
        JButton button2 = JButtonOperator.findJButton(frame, "JButtonOperatorTest", false, false);
        assertNotNull(button2);
    }
    
    /**
     * Test waitJButton method.
     */
    public void testWaitJButton() {
        frame.setVisible(true);
        
        JButton button1 = JButtonOperator.waitJButton(frame, new NameComponentChooser("JButtonOperatorTest"));
        assertNotNull(button1);
        
        JButton button2 = JButtonOperator.waitJButton(frame, "JButtonOperatorTest", false, false);
        assertNotNull(button2);
    }
    
    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JButtonOperator operator2 = new JButtonOperator(operator1);
        assertNotNull(operator2);
        
        Hashtable hashtable = operator2.getDump();
        assertEquals("false", hashtable.get(JButtonOperator.IS_DEFAULT_DPROP));
        
        frame.getRootPane().setDefaultButton(button);
        hashtable = operator2.getDump();
        assertEquals("true", hashtable.get(JButtonOperator.IS_DEFAULT_DPROP));
        assertTrue(operator2.isDefaultButton());
    }
    
    /**
     * Test isDefaultCapable method.
     */
    public void testIsDefaultCapable() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JButtonOperator operator2 = new JButtonOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setDefaultCapable(true);
        assertEquals(true, button.isDefaultCapable());
        assertEquals(operator2.isDefaultCapable(), button.isDefaultCapable());
        
        operator2.setDefaultCapable(false);
        assertEquals(false, button.isDefaultCapable());
        assertEquals(operator2.isDefaultCapable(), button.isDefaultCapable());
    }
    
    /**
     * Test prepareToClick method.
     *
     * @todo This will fail if I hide the button first. Should it make the
     *       button visible if it is not visible?
     */
    public void testPrepareToClick() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JButtonOperator operator2 = new JButtonOperator(operator1);
        assertNotNull(operator2);
        
        operator2.prepareToClick();
        
        JButtonOperator operator3 = new JButtonOperator(operator1);
        assertNotNull(operator3);
        assertTrue(operator3.isVisible());
    }
    
    /**
     * Test for issue #72187.
     */
    public void testIssue72187() {
        frame.setVisible(true);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        button.setVisible(false);
                    }
                });
            }
        });
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JButtonOperator operator2 = new JButtonOperator(operator1);
        assertNotNull(operator2);
        
        operator2.press();
        operator2.release();
        assertTrue(!operator2.isVisible());
        assertTrue(!button.isVisible());
        
        button.setVisible(true);
        JButtonOperator operator3 = new JButtonOperator(operator1);
        assertNotNull(operator3);
        
        operator3.doClick();
        assertTrue(!button.isVisible());
        
        button.setVisible(true);
        JButtonOperator operator4 = new JButtonOperator(operator1);
        assertNotNull(operator4);

        /*
        operator4.push();
        assertTrue(!operator4.isVisible());
        assertTrue(!button.isVisible());
         */
    }
}
