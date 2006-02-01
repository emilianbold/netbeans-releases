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
import javax.swing.JPasswordField;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JPasswordFieldOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JPasswordFieldOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private JFrame frame;
    
    /**
     * Stores the password field we use for testing.
     */
    private JPasswordField passwordField;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JPasswordFieldOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        passwordField = new JPasswordField("JPasswordFieldOperatorTest");
        passwordField.setName("JPasswordFieldOperatorTest");
        frame.add(passwordField);
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
        TestSuite suite = new TestSuite(JPasswordFieldOperatorTest.class);
        
        return suite;
    }

    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JPasswordFieldOperator operator3 = new JPasswordFieldOperator(operator1, new NameComponentChooser("JPasswordFieldOperatorTest"));
        assertNotNull(operator3);

        JPasswordFieldOperator operator4 = new JPasswordFieldOperator(operator1);
        assertNotNull(operator4);

        JPasswordFieldOperator operator5 = new JPasswordFieldOperator(operator1, "JPasswordFieldOperatorTest");
        assertNotNull(operator5);
    }
    
    /**
     * Test findJPasswordField method.
     */
    public void testFindJPasswordField() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JPasswordField passwordField1 = JPasswordFieldOperator.findJPasswordField(frame, new NameComponentChooser("JPasswordFieldOperatorTest"));
        assertNotNull(passwordField1);

        JPasswordField passwordField2 = JPasswordFieldOperator.findJPasswordField(frame, "JPasswordFieldOperatorTest", false, false);
        assertNotNull(passwordField2);
    }

    /**
     * Test waitJPasswordField method.
     */
    public void testWaitJPasswordField() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JPasswordField passwordField1 = JPasswordFieldOperator.waitJPasswordField(frame, new NameComponentChooser("JPasswordFieldOperatorTest"));
        assertNotNull(passwordField1);

        JPasswordField passwordField2 = JPasswordFieldOperator.waitJPasswordField(frame, "JPasswordFieldOperatorTest", false, false);
        assertNotNull(passwordField2);
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JPasswordFieldOperator operator2 = new JPasswordFieldOperator(operator1, new NameComponentChooser("JPasswordFieldOperatorTest"));
        assertNotNull(operator2);
        
        operator2.setEchoChar('%');
        assertEquals("%", operator2.getDump().get(JPasswordFieldOperator.ECHO_CHAR_DPROP));
    }

    /**
     * Test echoCharIsSet method.
     */
    public void testEchoCharIsSet() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JPasswordFieldOperator operator3 = new JPasswordFieldOperator(operator1, new NameComponentChooser("JPasswordFieldOperatorTest"));
        assertNotNull(operator3);
        
        assertTrue(operator3.echoCharIsSet());
        assertTrue(passwordField.echoCharIsSet());
        
        operator3.setEchoChar('a');
        assertEquals('a', operator3.getEchoChar());
        assertEquals(operator3.getEchoChar(), passwordField.getEchoChar());
    }

    /**
     * Test getPassword method.
     */
    public void testGetPassword() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JPasswordFieldOperator operator3 = new JPasswordFieldOperator(operator1, new NameComponentChooser("JPasswordFieldOperatorTest"));
        assertNotNull(operator3);
        
        passwordField.setText("hallo");
        assertEquals(operator3.getPassword()[0], 'h');
        assertEquals(operator3.getPassword()[1], 'a');
        assertEquals(operator3.getPassword()[2], 'l');
        assertEquals(operator3.getPassword()[3], 'l');
        assertEquals(operator3.getPassword()[4], 'o');
    }
}
