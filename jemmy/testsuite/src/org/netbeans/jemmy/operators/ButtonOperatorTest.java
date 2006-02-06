/*
 * $Id$
 *
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library. The Initial Developer of the 
 * Original Code is Alexandre Iline. All Rights Reserved.
 * 
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy.operators;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
    }

    /**
     * Cleanup after testing.
     */
    protected void tearDown() throws Exception {
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
        Dialog dialog1 = new Dialog(new Frame(), "Dialog");
        Button button1 = new Button("Button");
        button1.setName("Button");
        
        dialog1.add(button1);
        dialog1.pack();
        dialog1.setVisible(true);
        
        ButtonOperator operator = new ButtonOperator(button1);
        assertNotNull(operator);
        
        ContainerOperator operator2 = new ContainerOperator(dialog1);
        assertNotNull(operator2);
        
        ButtonOperator operator3 = new ButtonOperator(operator2);
        assertNotNull(operator3);
        
        ButtonOperator operator4 = new ButtonOperator(operator2, 
                new NameComponentChooser("Button"));
        assertNotNull(operator4);
        
        ButtonOperator operator5 = new ButtonOperator(operator2, "Button");
        assertNotNull(operator5);
    }
    
    /**
     * Test findButton method.
     */
    public void testFindButton() {
        Dialog dialog1 = new Dialog(new Frame(), "TestDialog");
        Button button1 = new Button("Button");
        button1.setName("Button");
        
        dialog1.add(button1);
        dialog1.pack();
        dialog1.setVisible(true);
        
        Button button2 = ButtonOperator.findButton(dialog1, "Button", false, false);
        assertNotNull(button2);
        assertEquals("Button", button2.getLabel());

        Button button3 = ButtonOperator.findButton(dialog1, new NameComponentChooser("Button"));
        assertNotNull(button2);
        assertEquals("Button", button2.getLabel());
        
        dialog1.setVisible(false);
        dialog1.dispose();
        dialog1 = null;
    }
    
    /**
     * Test waitButton method.
     */
    public void testWaitButton() {
        Dialog dialog1 = new Dialog(new Frame(), "TestDialog");
        Button button1 = new Button("Button");
        button1.setName("Button");
        
        dialog1.add(button1);
        dialog1.pack();
        dialog1.setVisible(true);
        
        Button button2 = ButtonOperator.waitButton(dialog1, "Button", false, false);
        assertNotNull(button2);
        assertEquals("Button", button2.getLabel());
        
        Button button3 = ButtonOperator.waitButton(dialog1, new NameComponentChooser("Button"));
        assertNotNull(button2);
        assertEquals("Button", button2.getLabel());
        
        dialog1.setVisible(false);
        dialog1.dispose();
        dialog1 = null;
    }
    
    /**
     * Test getDump method.
     */
    public void testGetDump() {
        Dialog dialog1 = new Dialog(new Frame(), "TestDialog");
        Button button1 = new Button("Button");
        
        dialog1.add(button1);
        dialog1.pack();
        dialog1.setVisible(true);
        
        ButtonOperator operator1 = new ButtonOperator(button1);
        assertNotNull(operator1);
        
        Hashtable hashtable1 = operator1.getDump();
        assertNotNull(hashtable1.get("Label"));
        
        dialog1.setVisible(true);
        dialog1.dispose();
        dialog1 = null;
    }
    
    /**
     * Test getActionCommand method.
     */
    public void testGetActionCommand() {
        Dialog dialog1 = new Dialog(new Frame(), "TestDialog");
        Button button1 = new Button("Button");
        
        dialog1.add(button1);
        dialog1.pack();
        dialog1.setVisible(true);
        
        ButtonOperator operator1 = new ButtonOperator(button1);
        assertNotNull(operator1);
        
        operator1.setActionCommand("TEST");
        assertEquals("TEST", operator1.getActionCommand());
        
        dialog1.setVisible(false);
        dialog1.dispose();
        dialog1 = null;
    }

    /**
     * Test getLabel method.
     */
    public void testGetLabel() {
        Dialog dialog1 = new Dialog(new Frame(), "TestDialog");
        Button button1 = new Button("Button");
        
        dialog1.add(button1);
        dialog1.pack();
        dialog1.setVisible(true);
        
        ButtonOperator operator1 = new ButtonOperator(button1);
        assertNotNull(operator1);
        
        operator1.setLabel("TEST");
        assertEquals("TEST", operator1.getLabel());
        
        dialog1.setVisible(false);
        dialog1.dispose();
        dialog1 = null;
    }
    
    /**
     * Test addActionListener method.
     */
    public void testAddActionListener() {
        Dialog dialog1 = new Dialog(new Frame(), "TestDialog");
        Button button1 = new Button("Button");
        
        dialog1.add(button1);
        dialog1.pack();
        dialog1.setVisible(true);
        
        ButtonOperator operator1 = new ButtonOperator(button1);
        assertNotNull(operator1);
        
        operator1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            }
        });
        assertTrue(button1.getActionListeners().length == 1);
        
        operator1.removeActionListener(button1.getActionListeners()[0]);
        assertTrue(button1.getActionListeners().length == 0);
        
        dialog1.setVisible(false);
        dialog1.dispose();
        dialog1 = null;
    }
    
    /**
     * Test push method.
     */
    public void testPush() {
        /*
        Dialog dialog1 = new Dialog(new Frame(), "TestDialog");
        Button button1 = new Button("Button");
        
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                ((Button) event.getSource()).setLabel("NewButton");
            }
        });
        
        dialog1.add(button1);
        dialog1.pack();
        dialog1.setVisible(true);
        
        ButtonOperator operator1 = new ButtonOperator(button1);
        assertNotNull(operator1);
        
        operator1.push();
        assertEquals("NewButton", button1.getLabel());

        dialog1.setVisible(false);
        dialog1.dispose();
        dialog1 = null;
        */
    }
    
    /**
     * Test pushNoBlock method.
     *
     * @review This test will fail if the system is overloaded. This is because
     *         the label will not be set correctly.
     */
    public void testPushNoBlock() {
        /*
        Dialog dialog1 = new Dialog(new Frame(), "TestDialog");
        Button button1 = new Button("Button");
        
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                ((Button) event.getSource()).setLabel("NewButton");
            }
        });
        
        dialog1.add(button1);
        dialog1.pack();
        dialog1.setVisible(true);
        
        ButtonOperator operator1 = new ButtonOperator(button1);
        assertNotNull(operator1);
        
        operator1.pushNoBlock();
        
        try {
            Thread.sleep(1000);
        }
        catch(InterruptedException ie) {}
        
        assertEquals("NewButton", button1.getLabel());

        dialog1.setVisible(false);
        dialog1.dispose();
        dialog1 = null;
         */
    }
    
    /**
     * Test press/release method.
     */
    public void testRelease() {
        /*
        Dialog dialog1 = new Dialog(new Frame(), "TestDialog");
        Button button1 = new Button("Button");
        
        button1.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent event) {
            }

            public void mousePressed(MouseEvent event) {
                ((Button) event.getSource()).setLabel("ButtonPressed");
            }

            public void mouseReleased(MouseEvent event) {
                ((Button) event.getSource()).setLabel("ButtonReleased");
            }

            public void mouseEntered(MouseEvent event) {
            }

            public void mouseExited(MouseEvent event) {
            }
        });

        dialog1.add(button1);
        dialog1.pack();
        dialog1.setVisible(true);
        
        ButtonOperator operator1 = new ButtonOperator(button1);
        assertNotNull(operator1);
        
        operator1.press();
        assertNotNull("ButtonPressed");
        
        operator1.release();
        assertNotNull("ButtonReleased");

        dialog1.setVisible(false);
        dialog1.dispose();
        dialog1 = null;
         */
    }
}
