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

import java.awt.Checkbox;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for CheckboxOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class CheckboxOperatorTest extends TestCase {
    /**
     * Stores the frame we use for testing.
     */
    private Frame frame;
    
    /**
     * Stores the button we use for testing.
     */
    private Checkbox checkbox;
    
    /**
     * Constructor.
     */
    public CheckboxOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     *
     * @throws Exception when a major problem occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        checkbox = new Checkbox("CheckboxOperatorTest");
        checkbox.setName("CheckboxOperatorTest");
        frame.add(checkbox);
        frame.pack();
    }

    /**
     * Cleanup after testing.
     *
     * @throws Exception when a major problem occurs.
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
        TestSuite suite = new TestSuite(CheckboxOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        CheckboxOperator operator1 = new CheckboxOperator(operator);
        assertNotNull(operator1);
        
        CheckboxOperator operator2 = new CheckboxOperator(operator, "CheckboxOperatorTest");
        assertNotNull(operator2);
        
        CheckboxOperator operator3 = new CheckboxOperator(operator, new NameComponentChooser("CheckboxOperatorTest"));
        assertNotNull(operator3);
    }

    /**
     * Test findCheckbox method.
     */
    public void testFindCheckbox() {
        frame.setVisible(true);
        
        Checkbox checkbox1 = CheckboxOperator.findCheckbox(frame, "CheckboxOperatorTest", false, false);
        assertNotNull(checkbox1);
        
        Checkbox checkbox2 = CheckboxOperator.findCheckbox(frame, new NameComponentChooser("CheckboxOperatorTest"));
        assertNotNull(checkbox2);
    }

    /**
     * Test waitCheckbox method.
     */
    public void testWaitCheckbox() {
        frame.setVisible(true);
        
        Checkbox checkbox1 = CheckboxOperator.waitCheckbox(frame, "CheckboxOperatorTest", false, false);
        assertNotNull(checkbox1);
        
        Checkbox checkbox2 = CheckboxOperator.waitCheckbox(frame, new NameComponentChooser("CheckboxOperatorTest"));
        assertNotNull(checkbox2);
    }

    /**
     * Test changeSelection method.
     */
    public void testChangeSelection() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        CheckboxOperator operator1 = new CheckboxOperator(operator);
        assertNotNull(operator1);
        
        operator1.setState(false);
        operator1.changeSelectionNoBlock(true);

        operator1.setState(true);
        operator1.changeSelection(false);
    }

    /**
     * Test changeSelectionNoBlock method.
     */
    public void testChangeSelectionNoBlock() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        CheckboxOperator operator1 = new CheckboxOperator(operator);
        assertNotNull(operator1);
        
        operator1.changeSelectionNoBlock(true);
    }

    /**
     * Test waitSelected method.
     */
    public void testWaitSelected() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        CheckboxOperator operator1 = new CheckboxOperator(operator);
        assertNotNull(operator1);
        
        operator1.waitSelected(false);
        operator1.setState(true);
        operator1.waitSelected(true);
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        CheckboxOperator operator1 = new CheckboxOperator(operator);
        assertNotNull(operator1);
        
        operator1.getDump();
    }

    /**
     * Test addItemListener method.
     */
    public void testAddItemListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        CheckboxOperator operator1 = new CheckboxOperator(operator);
        assertNotNull(operator1);
        
        ItemListenerTest listener = new ItemListenerTest();
        operator1.addItemListener(listener);
        operator1.removeItemListener(listener);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class ItemListenerTest implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
        }
    }

    /**
     * Test getCheckboxGroup method.
     */
    public void testGetCheckboxGroup() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        CheckboxOperator operator1 = new CheckboxOperator(operator);
        assertNotNull(operator1);
        
        operator1.getCheckboxGroup();
    }

    /**
     * Test getLabel method.
     */
    public void testGetLabel() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        CheckboxOperator operator1 = new CheckboxOperator(operator);
        assertNotNull(operator1);
        
        operator1.getLabel();
    }

    /**
     * Test getState method.
     */
    public void testGetState() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        CheckboxOperator operator1 = new CheckboxOperator(operator);
        assertNotNull(operator1);
        
        operator1.getState();
    }

    /**
     * Test setCheckboxGroup method.
     */
    public void testSetCheckboxGroup() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        CheckboxOperator operator1 = new CheckboxOperator(operator);
        assertNotNull(operator1);
        
        operator1.setCheckboxGroup(operator1.getCheckboxGroup());
    }

    /**
     * Test setLabel method.
     */
    public void testSetLabel() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        CheckboxOperator operator1 = new CheckboxOperator(operator);
        assertNotNull(operator1);
        
        operator1.setLabel(operator1.getLabel());
    }

    /**
     * Test setState method.
     */
    public void testSetState() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        CheckboxOperator operator1 = new CheckboxOperator(operator);
        assertNotNull(operator1);
        
        operator1.setState(operator1.getState());
    }
}
