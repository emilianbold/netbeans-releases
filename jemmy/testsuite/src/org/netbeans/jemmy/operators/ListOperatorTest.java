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

import java.awt.Frame;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for ListOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class ListOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private Frame frame;
    
    /**
     * Stores the list.
     */
    private List list;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public ListOperatorTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup before testing.
     *
     * @throws Exception when a major problem occurs.
     */
    protected void setUp() throws Exception {
        frame = new Frame();
        list = new List();
        list.setName("ListOperatorTest");
        list.add("Item 1");
        list.select(0);
        frame.add(list);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
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
        TestSuite suite = new TestSuite(ListOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);
        
        ListOperator operator2 = new ListOperator(operator, new NameComponentChooser("ListOperatorTest"));
        assertNotNull(operator2);
        
        ListOperator operator3 = new ListOperator(operator, "Item 1");
        assertNotNull(operator3);
    }

    /**
     * Test findList method.
     */
    public void testFindList() {
        frame.setVisible(true);
        
        List list1 = ListOperator.findList(frame, new NameComponentChooser("ListOperatorTest"));
        assertNotNull(list1);
    }

    /**
     * Test findItemIndex method.
     */
    public void testFindItemIndex() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.findItemIndex("Item 1");
    }

    /**
     * Test selectItem method.
     */
    public void testSelectItem() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.selectItem("Item 1");
        operator1.selectItem(0);
    }

    /**
     * Test selectItems method.
     */
    public void testSelectItems() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);
        
        operator1.selectItems(0, 0);
    }

    /**
     * Test waitItemsSelection method.
     */
    public void testWaitItemsSelection() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.selectItem(0);
        operator1.waitItemsSelection(0, 0, true);
    }

    /**
     * Test waitItemSelection method.
     */
    public void testWaitItemSelection() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.selectItem(0);
        operator1.waitItemSelection(0, true);
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.getDump();
    }

    /**
     * Test addActionListener method.
     */
    public void testAddActionListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        ActionListenerTest listener = new ActionListenerTest();
        operator1.addActionListener(listener);
        operator1.removeActionListener(listener);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class ActionListenerTest implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        }
    }

    /**
     * Test addItemListener method.
     */
    public void testAddItemListener() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
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
     * Test deselect method.
     */
    public void testDeselect() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.deselect(0);
    }

    /**
     * Test getItem method.
     */
    public void testGetItem() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.getItem(0);
    }

    /**
     * Test getItemCount method.
     */
    public void testGetItemCount() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.getItemCount();
    }

    /**
     * Test getItems method.
     */
    public void testGetItems() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.getItems();
    }

    /**
     * Test getMinimumSize method.
     */
    public void testGetMinimumSize() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.getMinimumSize(0);
    }

    /**
     * Test getPreferredSize method.
     */
    public void testGetPreferredSize() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.getPreferredSize(0);
    }

    /**
     * Test getRows method.
     */
    public void testGetRows() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.getRows();
    }

    /**
     * Test getSelectedIndex method.
     */
    public void testGetSelectedIndex() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.getSelectedIndex();
    }

    /**
     * Test getSelectedIndexes method.
     */
    public void testGetSelectedIndexes() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.getSelectedIndexes();
    }

    /**
     * Test getSelectedItem method.
     */
    public void testGetSelectedItem() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.getSelectedItem();
    }

    /**
     * Test getSelectedItems method.
     */
    public void testGetSelectedItems() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.getSelectedItems();
    }

    /**
     * Test getSelectedObjects method.
     */
    public void testGetSelectedObjects() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.getSelectedObjects();
    }

    /**
     * Test getVisibleIndex method.
     */
    public void testGetVisibleIndex() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.getVisibleIndex();
    }

    /**
     * Test isIndexSelected method.
     */
    public void testIsIndexSelected() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.isIndexSelected(0);
    }

    /**
     * Test isMultipleMode method.
     */
    public void testIsMultipleMode() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.isMultipleMode();
    }

    /**
     * Test makeVisible method.
     */
    public void testMakeVisible() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.makeVisible(0);
    }

    /**
     * Test remove method.
     */
    public void testRemove() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.remove(0);
        list.add("Item 1");
        operator1.remove("Item 1");
    }

    /**
     * Test removeAll method.
     */
    public void testRemoveAll() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.removeAll();
    }

    /**
     * Test replaceItem method.
     */
    public void testReplaceItem() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.replaceItem("Item 2", 0);
    }

    /**
     * Test select method.
     */
    public void testSelect() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.select(0);
    }

    /**
     * Test setMultipleMode method.
     */
    public void testSetMultipleMode() {
        frame.setVisible(true);
        
        FrameOperator operator = new FrameOperator();
        assertNotNull(operator);
        
        ListOperator operator1 = new ListOperator(operator);
        assertNotNull(operator1);

        operator1.setMultipleMode(true);
    }
}
