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

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JComboBoxOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JComboBoxOperatorTest extends TestCase {
    /**
     * Stores the dialog we use.
     */
    private JDialog dialog;
    
    /**
     * Stores the combo box.
     */
    private JComboBox comboBox;
    
    /**
     * Constructor.
     */
    public JComboBoxOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup for testing.
     */
    protected void setUp() throws Exception {
        dialog = new JDialog(new JFrame(), "JComboBox");
        comboBox = new JComboBox();
        comboBox.setName("JComboBoxOperatorTest");
        dialog.add(comboBox);
        comboBox.addItem("1");
        comboBox.addItem("2");
        dialog.pack();
        dialog.setLocationRelativeTo(null);
    }

    /**
     * Cleanup after testing.
     */
    protected void tearDown() throws Exception {
        dialog.setVisible(false);
        dialog.dispose();
        dialog = null;
    }

    /**
     * Suite method.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(JComboBoxOperatorTest.class);
        
        return suite;
    }

    /**
     * Test setEditable method.
     */
    public void testSetEditable() {
        dialog.setVisible(true);
        
        JDialogOperator   operator1 = new JDialogOperator();
        assertNotNull(operator1);
        
        JComboBoxOperator operator2 = new JComboBoxOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setEditable(true);
        
        JComboBox combobox2 = JComboBoxOperator.waitJComboBox(dialog, new NameComponentChooser("JComboBoxOperatorTest"), 0);
        assertTrue(combobox2.isEditable());

        operator2.setEditable(false);
        
        JComboBox combobox3 = JComboBoxOperator.waitJComboBox(dialog, new NameComponentChooser("JComboBoxOperatorTest"), 0);
        assertTrue(!combobox3.isEditable());
    }
    
    /**
     * Test setSelectedIndex method.
     */
    public void setSelectedIndex() {
        dialog.setVisible(true);

        JDialogOperator   operator1 = new JDialogOperator();
        assertNotNull(operator1);
        
        JComboBoxOperator operator2 = new JComboBoxOperator(operator1);
        assertNotNull(operator2);
        
        operator2.setSelectedIndex(2);
        
        JComboBoxOperator operator3 = new JComboBoxOperator(operator1);
        assertNotNull(operator3);
        
        assertEquals(2, comboBox.getSelectedIndex());
    }
}
