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

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ListUI;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.operators.JListOperator.ListItemChooser;
import org.netbeans.jemmy.operators.JListOperator.NoSuchItemException;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.netbeans.jemmy.util.RegExComparator;

/**
 * A JUnit test for JListOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JListOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the list.
     */
    private JList list;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JListOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        String[] listData = {"one", "two", "three", "four"};
        list = new JList(listData);
        list.setName("JListOperatorTest");
        list.setSelectedIndex(0);
        frame.getContentPane().add(new JScrollPane(list));
        frame.setSize(300, 200);
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
        TestSuite suite = new TestSuite(JListOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        JListOperator operator2 = new JListOperator(operator, new NameComponentChooser("JListOperatorTest"));
        assertNotNull(operator2);
        
        JListOperator operator3 = new JListOperator(operator, "one");
        assertNotNull(operator3);
    }

    /**
     * Test findJList method.
     */
    public void testFindJList() {
        frame.setVisible(true);
        
        JList list1 = JListOperator.findJList(frame, new NameComponentChooser("JListOperatorTest"));
        assertNotNull(list1);
        
        JList list2 = JListOperator.findJList(frame, "one", false, false, 0);
        assertNotNull(list2);
    }

    /**
     * Test waitJList method.
     */
    public void testWaitJList() {
        frame.setVisible(true);
        
        JList list1 = JListOperator.waitJList(frame, new NameComponentChooser("JListOperatorTest"));
        assertNotNull(list1);
        
        JList list2 = JListOperator.waitJList(frame, "one", false, false, 0);
        assertNotNull(list2);
    }

    /**
     * Test getClickPoint method.
     */
    public void testGetClickPoint() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getClickPoint(0);
    }

    /**
     * Test getRenderedComponent method.
     */
    public void testGetRenderedComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getRenderedComponent(0);
    }

    /**
     * Test findItemIndex method.
     */
    public void testFindItemIndex() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.findItemIndex("one");
        operator1.findItemIndex("one", false, false);
        operator1.findItemIndex(new NameComponentChooser("one"));
        operator1.findItemIndex(new ListItemChooserTest(), 2);
        operator1.findItemIndex(new ListItemChooserTest());
    }
    
    /**
     * Inner class needed for testing.
     */
    public class ListItemChooserTest implements ListItemChooser {
        public boolean checkItem(JListOperator oper, int index) {
            return false;
        }

        public String getDescription() {
            return "";
        }
    }

    /**
     * Test clickOnItem method.
     */
    public void testClickOnItem() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.clickOnItem("one");
        operator1.clickOnItem("one", false, false);
        operator1.clickOnItem("one", new RegExComparator());
        
        try {
            operator1.clickOnItem("blabla");
            fail();
        }
        catch(NoSuchItemException exception) {
        }
    }

    /**
     * Test scrollToItem method.
     */
    public void testScrollToItem() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToItem(0);
        operator1.scrollToItem("one", false, false);
        operator1.scrollToItem("one", new RegExComparator());
    }

    /**
     * Test selectItem method.
     */
    public void testSelectItem() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.selectItem("one");
        operator1.selectItem(0);
        
        String[] items = new String[1];
        items[0] = "one";
        operator1.selectItem(items);
    }

    /**
     * Test selectItems method.
     */
    public void testSelectItems() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        int[] items = new int[1];
        items[0] = 0;
        operator1.selectItems(items);
    }

    /**
     * Test waitItemsSelection method.
     */
    public void testWaitItemsSelection() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.waitItemSelection(0, true);
    }

    /**
     * Test waitItemSelection method.
     */
    public void testWaitItemSelection() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        int[] items = new int[1];
        items[0] = 0;
        operator1.waitItemsSelection(items, true);
    }

    /**
     * Test waitItem method.
     */
    public void testWaitItem() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.waitItem("one", 0);
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getDump();
    }

    /**
     * Test addListSelectionListener method.
     */
    public void testAddListSelectionListener() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        ListSelectionListenerTest listener = new ListSelectionListenerTest();
        operator1.addListSelectionListener(listener);
        operator1.removeListSelectionListener(listener);
    }
    
    /**
     * Inner class needed for testing.
     */
    public class ListSelectionListenerTest implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
        }
    }

    /**
     * Test addSelectionInterval method.
     */
    public void testAddSelectionInterval() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.addSelectionInterval(0, 0);
        operator1.removeSelectionInterval(0, 0);
    }

    /**
     * Test clearSelection method.
     */
    public void testClearSelection() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.clearSelection();
    }

    /**
     * Test ensureIndexIsVisible method.
     */
    public void testEnsureIndexIsVisible() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.ensureIndexIsVisible(0);
    }

    /**
     * Test getAnchorSelectionIndex method.
     */
    public void testGetAnchorSelectionIndex() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getAnchorSelectionIndex();
    }

    /**
     * Test getCellBounds method.
     */
    public void testGetCellBounds() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getCellBounds(0, 0);
    }

    /**
     * Test getCellRenderer method.
     */
    public void testGetCellRenderer() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.setCellRenderer(new DefaultListCellRenderer());  
        operator1.getCellRenderer();
    }

    /**
     * Test getFirstVisibleIndex method.
     */
    public void testGetFirstVisibleIndex() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getFirstVisibleIndex();
    }

    /**
     * Test getFixedCellHeight method.
     */
    public void testGetFixedCellHeight() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.setFixedCellHeight(10);
        operator1.getFixedCellHeight();
    }

    /**
     * Test getFixedCellWidth method.
     */
    public void testGetFixedCellWidth() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.setFixedCellWidth(10);
        operator1.getFixedCellWidth();
    }

    /**
     * Test getLastVisibleIndex method.
     */
    public void testGetLastVisibleIndex() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getLastVisibleIndex();
    }

    /**
     * Test getLeadSelectionIndex method.
     */
    public void testGetLeadSelectionIndex() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getLeadSelectionIndex();
    }

    /**
     * Test getMaxSelectionIndex method.
     */
    public void testGetMaxSelectionIndex() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getMaxSelectionIndex();
    }

    /**
     * Test getMinSelectionIndex method.
     */
    public void testGetMinSelectionIndex() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getMinSelectionIndex();
    }

    /**
     * Test getPreferredScrollableViewportSize method.
     */
    public void testGetPreferredScrollableViewportSize() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getPreferredScrollableViewportSize();
    }

    /**
     * Test getPrototypeCellValue method.
     */
    public void testGetPrototypeCellValue() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.setPrototypeCellValue("1");
        operator1.getPrototypeCellValue();
    }

    /**
     * Test getScrollableBlockIncrement method.
     */
    public void testGetScrollableBlockIncrement() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getScrollableBlockIncrement(new Rectangle(100,  100), 0, 0);
    }

    /**
     * Test getScrollableTracksViewportHeight method.
     */
    public void testGetScrollableTracksViewportHeight() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getScrollableTracksViewportHeight();
    }

    /**
     * Test getScrollableTracksViewportWidth method.
     */
    public void testGetScrollableTracksViewportWidth() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getScrollableTracksViewportWidth();
    }

    /**
     * Test getScrollableUnitIncrement method.
     */
    public void testGetScrollableUnitIncrement() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getScrollableUnitIncrement(new Rectangle(100, 100), 0, 0);
    }

    /**
     * Test getSelectedIndex method.
     */
    public void testGetSelectedIndex() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectedIndex(0);
        operator1.getSelectedIndex();
    }

    /**
     * Test getSelectedIndices method.
     */
    public void testGetSelectedIndices() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        int[] indices = new int[1];
        indices[0] = 0;
        
        operator1.setSelectedIndices(indices);
        operator1.getSelectedIndices();
    }

    /**
     * Test getSelectedValue method.
     */
    public void testGetSelectedValue() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);

        operator1.setSelectedValue("one", true);
        operator1.getSelectedValue();
    }

    /**
     * Test getSelectedValues method.
     */
    public void testGetSelectedValues() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.getSelectedValues();
    }

    /**
     * Test getSelectionBackground method.
     */
    public void testGetSelectionBackground() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectionBackground(Color.black);
        operator1.getSelectionBackground();
    }

    /**
     * Test getSelectionForeground method.
     */
    public void testGetSelectionForeground() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);

        operator1.setSelectionForeground(Color.white);
        operator1.getSelectionForeground();
    }

    /**
     * Test getSelectionMode method.
     */
    public void testGetSelectionMode() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectionMode(0);
        operator1.getSelectionMode();
    }

    /**
     * Test getSelectionModel method.
     */
    public void testGetSelectionModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);

        operator1.setSelectionModel(new DefaultListSelectionModel());
        operator1.getSelectionModel();
    }

    /**
     * Test getUI method.
     */
    public void testGetUI() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);

        operator1.setUI(new ListUITest());
        operator1.getUI();
    }

    /**
     * Inner class needed for testing.
     */
    public class ListUITest extends ListUI {
        public int locationToIndex(JList list, Point location) {
            return -1;
        }

        public Point indexToLocation(JList list, int index) {
            return null;
        }

        public Rectangle getCellBounds(JList list, int index1, int index2) {
            return null;
        }
    }

    /**
     * Test getValueIsAdjusting method.
     */
    public void testGetValueIsAdjusting() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);

        operator1.setValueIsAdjusting(true);
        operator1.getValueIsAdjusting();
    }
    
    /**
     * Test getVisibleRowCount method.
     */
    public void testGetVisibleRowCount() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);
        
        operator1.setVisibleRowCount(1);
        operator1.getVisibleRowCount();
    }

    /**
     * Test indexToLocation method.
     */
    public void testIndexToLocation() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);

        operator1.indexToLocation(0);
    }

    /**
     * Test isSelectedIndex method.
     */
    public void testIsSelectedIndex() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);

        operator1.isSelectedIndex(0);
    }

    /**
     * Test isSelectionEmpty method.
     */
    public void testIsSelectionEmpty() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);

        operator1.isSelectionEmpty();
    }

    /**
     * Test locationToIndex method.
     */
    public void testLocationToIndex() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);

        operator1.locationToIndex(new Point(10, 10));
    }

    /**
     * Test setListData method.
     */
    public void testSetListData() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);

        String[] listData = {"one", "two", "three", "four"};
        operator1.setListData(listData);
        operator1.setListData(new Vector());
    }

    /**
     * Test setModel method.
     */
    public void testSetModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);

        operator1.setModel(new DefaultListModel());
    }

    /**
     * Test setSelectionInterval method.
     */
    public void testSetSelectionInterval() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JListOperator operator1 = new JListOperator(operator);
        assertNotNull(operator1);

        operator1.setSelectionInterval(0, 0);
    }
}
