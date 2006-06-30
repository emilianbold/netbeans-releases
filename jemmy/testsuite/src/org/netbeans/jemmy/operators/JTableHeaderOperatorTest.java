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

import java.awt.Point;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JTableHeaderOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JTableHeaderOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the table.
     */
    private JTable table;
    
    /**
     * Stores the table header.
     */
    private JTableHeader header;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JTableHeaderOperatorTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup befor testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        
        String[] columns = {
            "First Name",
            "Last Name",
            "Sport",
            "# of Years",
            "Vegetarian"};
        
        Object[][] data = {
            {"Mary",   "Campione", "Snowboarding",  new Integer(5),  new Boolean(false)},
            {"Alison", "Huml",     "Rowing",        new Integer(3),  new Boolean(true)},
            {"Kathy",  "Walrath",  "Knitting",      new Integer(2),  new Boolean(false)},
            {"Sharon", "Zakhour",  "Speed reading", new Integer(20), new Boolean(true)},
            {"Philip", "Milne",    "Pool", new Integer(10), new Boolean(false)}
        };
        
        table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);
        header = table.getTableHeader();
        header.setName("JTableHeaderOperatorTest");
        
        frame.getContentPane().add(scrollPane);
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
        TestSuite suite = new TestSuite(JTableHeaderOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        JTableHeaderOperator operator2 = new JTableHeaderOperator(operator, new NameComponentChooser("JTableHeaderOperatorTest"));
        assertNotNull(operator2);
    }
    
    /**
     * Test selectColumn method.
     */
    public void testSelectColumn() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        operator1.selectColumn(0);
    }
    
    /**
     * Test selectColumns method.
     */
    public void testSelectColumns() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        int[] columns = new int[2];
        columns[0] = 0;
        columns[1] = 1;
        operator1.selectColumns(columns);
    }
    
    /**
     * Test moveColumn method.
     */
    public void testMoveColumn() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        operator1.moveColumn(0, 1);
    }
    
    /**
     * Test getPointToClick method.
     */
    public void testGetPointToClick() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        operator1.getPointToClick(0);
    }
    
    /**
     * Test setTable method.
     */
    public void testSetTable() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        JTable table = new JTable();
        operator1.setTable(table);
        assertEquals(table, operator1.getTable());
    }
    
    /**
     * Test setReorderingAllowed method.
     */
    public void testSetReorderingAllowed() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        operator1.setReorderingAllowed(true);
        assertTrue(operator1.getReorderingAllowed());
        
        operator1.setReorderingAllowed(false);
        assertTrue(!operator1.getReorderingAllowed());
    }
    
    /**
     * Test setResizingAllowed method.
     */
    public void testSetResizingAllowed() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        operator1.setResizingAllowed(true);
        assertTrue(operator1.getResizingAllowed());
        
        operator1.setResizingAllowed(false);
        assertTrue(!operator1.getResizingAllowed());
    }
    
    /**
     * Test getDraggedColumn method.
     */
    public void testGetDraggedColumn() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        TableColumn column = table.getTableHeader().getColumnModel().getColumn(1);
        operator1.setDraggedColumn(column);
        assertEquals(column, operator1.getDraggedColumn());
    }
    
    /**
     * Test getDraggedDistance method.
     */
    public void testGetDraggedDistance() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        operator1.setDraggedDistance(10);
        assertEquals(10, operator1.getDraggedDistance());
    }
    
    /**
     * Test getResizingColumn method.
     */
    public void testGetResizingColumn() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        TableColumn column = table.getTableHeader().getColumnModel().getColumn(1);
        operator1.setResizingColumn(column);
        assertEquals(column, operator1.getResizingColumn());
    }
    
    /**
     * Test setUpdateTableInRealTime method.
     */
    public void testSetUpdateTableInRealTime() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        TableColumn column = table.getTableHeader().getColumnModel().getColumn(1);
        operator1.setUpdateTableInRealTime(true);
        assertTrue(operator1.getUpdateTableInRealTime());

        operator1.setUpdateTableInRealTime(false);
        assertTrue(!operator1.getUpdateTableInRealTime());
    }
    
    /**
     * Test setDefaultRenderer method.
     */
    public void testSetDefaultRenderer() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        operator1.setDefaultRenderer(null);
        assertNull(operator1.getDefaultRenderer());
    }
    
    /**
     * Test columnAtPoint method.
     */
    public void testColumnAtPoint() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        operator1.columnAtPoint(new Point(0,0));
    }
    
    /**
     * Test getHeaderRect method.
     */
    public void testGetHeaderRect() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getHeaderRect(0));
    }
    
    /**
     * Test getUI method.
     */
    public void testGetUI() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        operator1.setUI(new TableHeaderUITest());
        assertNotNull(operator1.getUI());
    }
    
    /**
     * Inner class needed for testing.
     */
    public class TableHeaderUITest extends TableHeaderUI {
    }
    
    /**
     * Test setColumnModel method.
     */
    public void testSetColumnModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        operator1.setColumnModel(new DefaultTableColumnModel());
        assertNotNull(operator1.getColumnModel());
    }
    
    /**
     * Test columnAdded method.
     */
    public void testColumnAdded() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        TableColumnModel model = table.getColumnModel();
        operator1.columnAdded(new TableColumnModelEvent(model, 0, 1));
    }
    
    /**
     * Test columnRemoved method.
     */
    public void testColumnRemoved() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        TableColumnModel model = table.getColumnModel();
        operator1.columnRemoved(new TableColumnModelEvent(model, 0, 1));
    }
    
    /**
     * Test columnMoved method.
     */
    public void testColumnMoved() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        TableColumnModel model = table.getColumnModel();
        operator1.columnMoved(new TableColumnModelEvent(model, 0, 1));
    }
    
    /**
     * Test columnMarginChanged method.
     */
    public void testColumnMarginChanged() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        ChangeEvent event = new ChangeEvent(operator1);
        operator1.columnMarginChanged(event);
    }
    
    /**
     * Test columnSelectionChanged method.
     */
    public void testColumnSelectionChanged() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        ListSelectionEvent event = new ListSelectionEvent(operator1, 0, 0, false);
        operator1.columnSelectionChanged(event);
    }
    
    /**
     * Test resizeAndRepaint method.
     */
    public void testResizeAndRepaint() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableHeaderOperator operator1 = new JTableHeaderOperator(operator);
        assertNotNull(operator1);
        
        operator1.resizeAndRepaint();
    }
}
