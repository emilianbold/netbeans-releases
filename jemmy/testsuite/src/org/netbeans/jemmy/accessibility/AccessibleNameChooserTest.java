/*
 * $Id$
 *
 * ----------------------------------------------------------------------------
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
 * ----------------------------------------------------------------------------
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 */
package org.netbeans.jemmy.accessibility;

import javax.swing.JButton;
import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * A JUnit test for AccessibleNameChooser.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class AccessibleNameChooserTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the button.
     */
    private JButton button;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public AccessibleNameChooserTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a serious error occurs.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        button = new JButton("Button");
        button.getAccessibleContext().setAccessibleName("Accessible");
        frame.getContentPane().add(button);
        frame.setLocationRelativeTo(null);
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
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(AccessibleNameChooserTest.class);
        
        return suite;
    }

    /**
     * Test checkContext method.
     */
    public void testCheckContext() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JButtonOperator operator1 = new JButtonOperator(operator, new AccessibleNameChooser("Accessible"));
        assertNotNull(operator1);
    }

    /**
     * Test getDescription method.
     */
    public void testGetDescription() {
        assertEquals("JComponent with \"Accessible\" accessible name", 
                new AccessibleNameChooser("Accessible").getDescription());
    }
}
