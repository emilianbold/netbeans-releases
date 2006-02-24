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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JRadioButtonMenuItemOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JRadioButtonMenuItemOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the menubar.
     */
    private JMenuBar menuBar;
    
    /**
     * Stores the menu.
     */
    private JMenu menu;
    
    /**
     * Stores the menu item.
     */
    private JRadioButtonMenuItem menuItem;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JRadioButtonMenuItemOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        menu = new JMenu("Menu");
        menuBar.add(menu);
        menuItem = new JRadioButtonMenuItem("Radio Button 1");
        menuItem.setName("Radio Button 1");
        menu.add(menuItem);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     */
    protected void tearDown() throws Exception {
        frame.setVisible(false);
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(JRadioButtonMenuItemOperatorTest.class);
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JMenuOperator operator1 = new JMenuOperator(operator);
        assertNotNull(operator1);
        
        operator1.showMenuItem("Radio Button 1");
        
        JRadioButtonMenuItemOperator operator2 = new JRadioButtonMenuItemOperator(operator);
        assertNotNull(operator2);

        JRadioButtonMenuItemOperator operator3 = new JRadioButtonMenuItemOperator(operator, new NameComponentChooser("Radio Button 1"));
        assertNotNull(operator3);

        JRadioButtonMenuItemOperator operator4 = new JRadioButtonMenuItemOperator(operator, "Radio Button 1");
        assertNotNull(operator4);

        JRadioButtonMenuItemOperator operator5 = new JRadioButtonMenuItemOperator(menuItem);
        assertNotNull(operator5);
    }
}
