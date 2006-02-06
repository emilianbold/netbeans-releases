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

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JCheckBoxMenuItemOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JCheckBoxMenuItemOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private JFrame frame;
    
    /**
     * Stores the menu bar we use for testing.
     */
    private JMenuBar menuBar;
    
    /**
     * Stores the checkBoxMenuItem we use for testing.
     */
    private JCheckBoxMenuItem checkBoxMenuItem;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JCheckBoxMenuItemOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        menuBar = new JMenuBar();
        checkBoxMenuItem = new JCheckBoxMenuItem("JCheckBoxMenuItemOperatorTest");
        checkBoxMenuItem.setName("JCheckBoxMenuItemOperatorTest");
        menuBar.add(checkBoxMenuItem);
        frame.setJMenuBar(menuBar);
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
        TestSuite suite = new TestSuite(JCheckBoxMenuItemOperatorTest.class);
        
        return suite;
    }

    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JMenuBarOperator operator2 = new JMenuBarOperator(operator1);
        assertNotNull(operator2);
        
        JCheckBoxMenuItemOperator operator3 = new JCheckBoxMenuItemOperator(operator2);
        assertNotNull(operator1);

        JCheckBoxMenuItemOperator operator4 = new JCheckBoxMenuItemOperator(operator2, new NameComponentChooser("JCheckBoxMenuItemOperatorTest"));
        assertNotNull(operator4);

        JCheckBoxMenuItemOperator operator5 = new JCheckBoxMenuItemOperator(operator2, "JCheckBoxMenuItemOperatorTest");
        assertNotNull(operator5);
    }
    
    /**
     * Test getState method.
     */
    public void testGetState() {
        frame.setVisible(true);
        
        JFrameOperator operator1 = new JFrameOperator();
        assertNotNull(operator1);
        
        JCheckBoxMenuItemOperator operator2 = new JCheckBoxMenuItemOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setState(true);
        assertTrue(operator2.getState());
        assertEquals(operator2.getState(), checkBoxMenuItem.getState());
        
        operator2.setState(false);
        assertTrue(!operator2.getState());
        assertEquals(operator2.getState(), checkBoxMenuItem.getState());
    }
}
