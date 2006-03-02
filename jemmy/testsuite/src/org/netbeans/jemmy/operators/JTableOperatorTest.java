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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Hashtable;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.plaf.TableUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JTableOpertor.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JTableOperatorTest extends TestCase {
    /**
     * Stores the frame.
     */
    private JFrame frame;
    
    /**
     * Stores the table.
     */
    private JTable table;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JTableOperatorTest(String testName) {
        super(testName);
    }
    
    /**
     * Setup before testing.
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
        table.setName("JTableOperatorTest");
        JScrollPane scrollPane = new JScrollPane(table);
        
        frame.getContentPane().add(scrollPane);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }
    
    /**
     * Cleanup before testing.
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
        TestSuite suite = new TestSuite(JTableOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        JTableOperator operator2 = new JTableOperator(operator, new NameComponentChooser("JTableOperatorTest"));
        assertNotNull(operator2);
        
        operator2.selectCell(0, 0);
        
        JTableOperator operator3 = new JTableOperator(operator, "Mary");
        assertNotNull(operator3);
        
        JTableOperator operator4 = new JTableOperator(operator, "Mary", 0, 0);
        assertNotNull(operator4);
        
        JTableOperator operator5 = new JTableOperator(table);
        assertNotNull(operator5);
    }
    
    /**
     * Test findJTable method.
     */
    public void testFindJTable() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.selectCell(0, 0);
        
        JTable table1 = JTableOperator.findJTable(frame, new NameComponentChooser("JTableOperatorTest"));
        assertNotNull(table1);
        
        JTable table2 = JTableOperator.findJTable(frame, "Mary", true, true, 0, 0);
        assertNotNull(table2);
    }
    
    /**
     * Test waitJTable method.
     */
    public void testWaitJTable() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.selectCell(0, 0);
        
        JTable table1 = JTableOperator.waitJTable(frame, new NameComponentChooser("JTableOperatorTest"));
        assertNotNull(table1);
        
        JTable table2 = JTableOperator.waitJTable(frame, "Mary", true, true, 0, 0);
        assertNotNull(table2);
    }
    
    /**
     * Test findCell method.
     */
    public void testFindCell() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        Point point1 = operator1.findCell("Mary", 0);
        assertNotNull(point1);
        
        int[] rows = new int[1];
        rows[0] = 0;
        
        int[] columns = new int[1];
        columns[0] = 0;
        
        Point point2 = operator1.findCell(new NameComponentChooser("Mary"), rows, columns, 0);
        assertNotNull(point2);
    }
    
    /**
     * Test findCellRow method.
     */
    public void testFindCellRow() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        int index = operator1.findCellRow("Mary");
        assertEquals(0, index);
    }
    
    /**
     * Test findCellColumn method.
     */
    public void testFindCellColumn() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        int index = operator1.findCellColumn("Mary");
        assertEquals(0, index);
    }
    
    /**
     * Test clickOnCell method.
     */
    public void testClickOnCell() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.clickOnCell(0, 0);
        assertEquals(0, operator1.getSelectedRow());
        assertEquals(0, operator1.getSelectedColumn());
    }
    
    /**
     * Test clickForEdit method.
     */
    public void testClickForEdit() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.clickForEdit(0, 0);
    }
    
    /**
     * Test changeCellText method.
     */
    public void testChangeCellText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.changeCellText(0, 0, "NewText");
    }
    
    /**
     * Test changeCellObject method.
     */
    public void testChangeCellObject() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.changeCellObject(0, 0, (Object) "NewText");
    }
    
    /**
     * Test scrollToCell method.
     */
    public void testScrollToCell() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.scrollToCell(0, 0);
    }
    
    /**
     * Test findColumn method.
     */
    public void testFindColumn() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        int index = operator1.findColumn("First Name");
        assertEquals(0, index);
    }
    
    /**
     * Test callPopupOnCell method.
     */
    public void testCallPopupOnCell() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        /*
        JPopupMenu popup1 = operator1.callPopupOnCell(0, 0);
        assertNotNull(popup1);
         */
    }
    
    /**
     * Test getRenderedComponent method.
     */
    public void testGetRenderedComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        JComponent component1 = (JComponent) operator1.getRenderedComponent(0, 0);
        assertNotNull(component1);
    }
    
    /**
     * Test getPointToClick method.
     */
    public void testGetPointToClick() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        Point point = operator1.getPointToClick(0, 0);
        assertNotNull(point);
    }
    
    /**
     * Test getHeaderOperator method.
     */
    public void testGetHeaderOperator() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        JTableHeaderOperator operator2 = operator1.getHeaderOperator();
        assertNotNull(operator2);
    }
    
    /**
     * Test waitCellComponent method.
     */
    public void testWaitCellComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        try {
            operator1.waitCellComponent(new NameComponentChooser("1234"), 0, 0);
        }
        catch(TimeoutExpiredException exception) {
        }
    }
    
    /**
     * Test waitCell method.
     */
    public void testWaitCell() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.waitCell("Mary", 0, 0);
    }
    
    /**
     * Test getDump method.
     */
    public void testGetDump() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        Hashtable hashtable = operator1.getDump();
        assertNotNull(hashtable);
    }
    
    /**
     * Test addColumn method.
     */
    public void testAddColumn() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.addColumn(new TableColumn());
    }
    
    /**
     * Test addColumnSelectionInterval method.
     */
    public void testAddColumnSelectionInterval() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.addColumnSelectionInterval(0, 0);
    }
    
    /**
     * Test addRowSelectionInterval method.
     */
    public void testAddRowSelectionInterval() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.addRowSelectionInterval(0, 0);
    }
    
    /**
     * Test clearSelection method.
     */
    public void testClearSelection() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.clearSelection();
    }
    
    /**
     * Test columnAdded method.
     */
    public void testColumnAdded() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        TableColumnModel model = table.getColumnModel();
        operator1.columnAdded(new TableColumnModelEvent(model, 0, 0));
    }
    
    /**
     * Test columnAtPoint method.
     */
    public void testColumnAtPoint() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        int found = operator1.columnAtPoint(new Point(0, 0));
        assertEquals(0, found);
    }
    
    /**
     * Test columnMarginChanged method.
     */
    public void testColumnMarginChanged() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        ChangeEvent changeEvent = new ChangeEvent(table);
        operator1.columnMarginChanged(changeEvent);
    }
    
    /**
     * Test columnMoved method.
     */
    public void testColumnMoved() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        TableColumnModel model = table.getColumnModel();
        operator1.columnMoved(new TableColumnModelEvent(model, 0, 0));
    }
    
    /**
     * Test columnRemoved method.
     */
    public void testColumnRemoved() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        TableColumnModel model = table.getColumnModel();
        operator1.columnRemoved(new TableColumnModelEvent(model, 0, 0));
    }
    
    /**
     * Test columnSelectionChanged method.
     */
    public void testColumnSelectionChanged() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        ListSelectionEvent event = new ListSelectionEvent(table, 0, 0, true);
        operator1.columnSelectionChanged(event);
    }
    
    /**
     * Test convertColumnIndexToModel method.
     */
    public void testConvertColumnIndexToModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.convertColumnIndexToModel(0);
    }
    
    /**
     * Test convertColumnIndexToView method.
     */
    public void testConvertColumnIndexToView() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.convertColumnIndexToView(0);
    }
    
    /**
     * Test createDefaultColumnsFromModel method.
     */
    public void testCreateDefaultColumnsFromModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.createDefaultColumnsFromModel();
    }
    
    /**
     * Test editCellAt method.
     */
    public void testEditCellAt() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.editCellAt(0, 0);
        operator1.editCellAt(0, 0, null);
    }
    
    /**
     * Test editingCanceled method.
     */
    public void testEditingCanceled() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        ChangeEvent changeEvent = new ChangeEvent(table);
        operator1.editingCanceled(changeEvent);
    }
    
    /**
     * Test editingStopped method.
     */
    public void testEditingStopped() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        ChangeEvent changeEvent = new ChangeEvent(table);
        operator1.editingStopped(changeEvent);
    }
    
    /**
     * Test getAutoCreateColumnsFromModel method.
     */
    public void testGetAutoCreateColumnsFromModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setAutoCreateColumnsFromModel(true);
        assertTrue(operator1.getAutoCreateColumnsFromModel());

        operator1.setAutoCreateColumnsFromModel(false);
        assertTrue(!operator1.getAutoCreateColumnsFromModel());
    }
    
    /**
     * Test getAutoResizeMode method.
     */
    public void testGetAutoResizeMode() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setAutoResizeMode(2);
        assertEquals(2, operator1.getAutoResizeMode());

        operator1.setAutoResizeMode(1);
        assertEquals(1, operator1.getAutoResizeMode());
    }
    
    /**
     * Test getCellEditor method.
     */
    public void testGetCellEditor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
        operator1.setCellEditor(editor);
        assertEquals(editor, operator1.getCellEditor());
        
        operator1.getCellEditor(0, 0);
    }
    
    /**
     * Test getCellRect method.
     */
    public void testGetCellRect() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getCellRect(0, 0, false));
    }
    
    /**
     * Test getCellSelectionEnabled method.
     */
    public void testGetCellSelectionEnabled() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);

        operator1.setCellSelectionEnabled(true);
        assertTrue(operator1.getCellSelectionEnabled());
        
        operator1.setCellSelectionEnabled(false);
        assertTrue(!operator1.getCellSelectionEnabled());
    }
    
    /**
     * Test getColumn method.
     */
    public void testGetColumn() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        try {
            assertNull(operator1.getColumn("1"));
        } catch(JemmyException exception){
        }
    }
    
    /**
     * Test getColumnClass method.
     */
    public void testGetColumnClass() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getColumnClass(0));
    }
    
    /**
     * Test getColumnModel method.
     */
    public void testGetColumnModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        operator1.setColumnModel(model);
        assertEquals(model, operator1.getColumnModel());
    }
    
    /**
     * Test getColumnSelectionAllowed method.
     */
    public void testGetColumnSelectionAllowed() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setColumnSelectionAllowed(true);
        assertTrue(operator1.getColumnSelectionAllowed());

        operator1.setColumnSelectionAllowed(false);
        assertTrue(!operator1.getColumnSelectionAllowed());
    }
    
    /**
     * Test getDefaultEditor method.
     */
    public void testGetDefaultEditor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setDefaultEditor(String.class, 
            operator1.getDefaultEditor(String.class));
    }
    
    /**
     * Test getDefaultRenderer method.
     */
    public void testGetDefaultRenderer() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.getDefaultRenderer(String.class);
    }
    
    /**
     * Test getEditingColumn method.
     */
    public void testGetEditingColumn() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertEquals(-1, operator1.getEditingColumn());
    }
    
    /**
     * Test getEditingRow method.
     */
    public void testGetEditingRow() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertEquals(-1, operator1.getEditingRow());
    }
    
    /**
     * Test getEditorComponent method.
     */
    public void testGetEditorComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertNull(operator1.getEditorComponent());
    }
    
    /**
     * Test getGridColor method.
     */
    public void testGetGridColor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getGridColor());
    }
    
    /**
     * Test getIntercellSpacing method.
     */
    public void testGetIntercellSpacing() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getIntercellSpacing());
    }
    
    /**
     * Test getPreferredScrollableViewportSize method.
     */
    public void testGetPreferredScrollableViewportSize() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getPreferredScrollableViewportSize());
    }
    
    /**
     * Test getRowCount method.
     */
    public void testGetRowCount() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertEquals(5, operator1.getRowCount());
    }
    
    /**
     * Test getRowHeight method.
     */
    public void testGetRowHeight() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertEquals(16, operator1.getRowHeight());
    }
    
    /**
     * Test getRowMargin method.
     */
    public void testGetRowMargin() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertEquals(1, operator1.getRowMargin());
    }
    
    /**
     * Test getRowSelectionAllowed method.
     */
    public void testGetRowSelectionAllowed() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertTrue(operator1.getRowSelectionAllowed());
    }
    
    /**
     * Test getScrollableBlockIncrement method.
     */
    public void testGetScrollableBlockIncrement() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.getScrollableBlockIncrement(new Rectangle(0,0), 0, 0);
    }
    
    /**
     * Test getScrollableTracksViewportHeight method.
     */
    public void testGetScrollableTracksViewportHeight() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertTrue(!operator1.getScrollableTracksViewportHeight());
    }
    
    /**
     * Test getScrollableTracksViewportWidth method.
     */
    public void testGetScrollableTracksViewportWidth() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertTrue(operator1.getScrollableTracksViewportWidth());
    }
    
    /**
     * Test getScrollableUnitIncrement method.
     */
    public void testGetScrollableUnitIncrement() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.getScrollableUnitIncrement(new Rectangle(0, 0), 0, 0);
    }
    
    /**
     * Test getSelectedColumnCount method.
     */
    public void testGetSelectedColumnCount() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertEquals(0, operator1.getSelectedColumnCount());
    }
    
    /**
     * Test getSelectedColumns method.
     */
    public void testGetSelectedColumns() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getSelectedColumns());
    }
    
    /**
     * Test getSelectedRow method.
     */
    public void testGetSelectedRow() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertEquals(-1, operator1.getSelectedRow());
    }
    
    /**
     * Test getSelectedRowCount method.
     */
    public void testGetSelectedRowCount() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertEquals(0, operator1.getSelectedRowCount());
    }
    
    /**
     * Test getSelectedRows method.
     */
    public void testGetSelectedRows() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getSelectedRows());
    }
    
    /**
     * Test getSelectionBackground method.
     */
    public void testGetSelectionBackground() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getSelectionBackground());
    }
    
    /**
     * Test getSelectionForeground method.
     */
    public void testGetSelectionForeground() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getSelectionForeground());
    }
    
    /**
     * Test getSelectionModel method.
     */
    public void testGetSelectionModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getSelectionModel());
    }
    
    /**
     * Test getShowHorizontalLines method.
     */
    public void testGetShowHorizontalLines() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertTrue(operator1.getShowHorizontalLines());
    }
    
    /**
     * Test getShowVerticalLines method.
     */
    public void testGetShowVerticalLines() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertTrue(operator1.getShowVerticalLines());
    }
    
    /**
     * Test getUI method.
     */
    public void testGetUI() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertNotNull(operator1.getUI());
    }
    
    /**
     * Inner class we use for testing.
     */
    public class TableUITest extends TableUI {
    }
    
    /**
     * Test isCellEditable method.
     */
    public void testIsCellEditable() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertTrue(operator1.isCellEditable(0, 0));
    }
    
    /**
     * Test isColumnSelected method.
     */
    public void testIsColumnSelected() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertTrue(!operator1.isColumnSelected(0));
    }
    
    /**
     * Test isRowSelected method.
     */
    public void testIsRowSelected() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        assertTrue(!operator1.isRowSelected(0));
    }
    
    /**
     * Test moveColumn method.
     */
    public void testMoveColumn() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.moveColumn(0, 1);
    }
    
    /**
     * Test prepareEditor method.
     */
    public void testPrepareEditor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.prepareEditor(new DefaultCellEditor(new JTextField()), 0, 0);
    }
    
    /**
     * Test prepareRenderer method.
     */
    public void testPrepareRenderer() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.prepareRenderer(new DefaultTableCellRenderer(), 0, 0);
    }
    
    /**
     * Test removeColumn method.
     */
    public void testRemoveColumn() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.removeColumn(new TableColumn());
    }
    
    /**
     * Test removeColumnSelectionInterval method.
     */
    public void testRemoveColumnSelectionInterval() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.removeRowSelectionInterval(0, 1);
    }
    
    /**
     * Test removeEditor method.
     */
    public void testRemoveEditor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.removeEditor();
    }
    
    /**
     * Test removeRowSelectionInterval method.
     */
    public void testRemoveRowSelectionInterval() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.removeColumnSelectionInterval(0, 0);
    }
    
    /**
     * Test rowAtPoint method.
     */
    public void testRowAtPoint() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.rowAtPoint(new Point(0, 0));
    }
    
    /**
     * Test selectAll method.
     */
    public void testSelectAll() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.selectAll();
    }
    
    /**
     * Test setColumnSelectionInterval method.
     */
    public void testSetColumnSelectionInterval() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setColumnSelectionInterval(0, 0);
    }
    
    /**
     * Test setDefaultRenderer method.
     */
    public void testSetDefaultRenderer() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setDefaultRenderer(String.class, null);
    }
    
    /**
     * Test setEditingColumn method.
     */
    public void testSetEditingColumn() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setEditingColumn(0);
    }
    
    /**
     * Test setEditingRow method.
     */
    public void testSetEditingRow() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setEditingRow(0);
    }
    
    /**
     * Test setGridColor method.
     */
    public void testSetGridColor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setGridColor(Color.black);
    }
    
    /**
     * Test setIntercellSpacing method.
     */
    public void testSetIntercellSpacing() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setIntercellSpacing(new Dimension(1, 1));
    }
    
    /**
     * Test setModel method.
     */
    public void testSetModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setModel(operator1.getModel());
    }
    
    /**
     * Test setPreferredScrollableViewportSize method.
     */
    public void testSetPreferredScrollableViewportSize() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setPreferredScrollableViewportSize(null);
    }
    
    /**
     * Test setRowHeight method.
     */
    public void testSetRowHeight() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setRowHeight(1);
    }
    
    /**
     * Test setRowMargin method.
     */
    public void testSetRowMargin() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setRowMargin(1);
    }
    
    /**
     * Test setRowSelectionAllowed method.
     */
    public void testSetRowSelectionAllowed() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setRowSelectionAllowed(false);
    }
    
    /**
     * Test setRowSelectionInterval method.
     */
    public void testSetRowSelectionInterval() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setRowSelectionInterval(0, 0);
    }
    
    /**
     * Test setSelectionBackground method.
     */
    public void testSetSelectionBackground() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectionBackground(Color.blue);
    }
    
    /**
     * Test setSelectionForeground method.
     */
    public void testSetSelectionForeground() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectionForeground(Color.GREEN);
    }
    
    /**
     * Test setSelectionMode method.
     */
    public void testSetSelectionMode() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectionMode(0);
    }
    
    /**
     * Test setSelectionModel method.
     */
    public void testSetSelectionModel() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setSelectionModel(operator1.getSelectionModel());
    }
    
    /**
     * Test setShowGrid method.
     */
    public void testSetShowGrid() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setShowGrid(true);
    }
    
    /**
     * Test setShowHorizontalLines method.
     */
    public void testSetShowHorizontalLines() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setShowHorizontalLines(true);
    }
    
    /**
     * Test setShowVerticalLines method.
     */
    public void testSetShowVerticalLines() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setShowVerticalLines(false);
    }
    
    /**
     * Test setTableHeader method.
     */
    public void testSetTableHeader() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setTableHeader(operator1.getTableHeader());
    }
    
    /**
     * Test setUI method.
     */
    public void testSetUI() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setUI(operator1.getUI());
    }
    
    /**
     * Test setValueAt method.
     */
    public void testSetValueAt() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.setValueAt("1", 0, 0);
    }
    
    /**
     * Test tableChanged method.
     */
    public void testTableChanged() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.tableChanged(null);
    }
    
    /**
     * Test valueChanged method.
     */
    public void testValueChanged() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTableOperator operator1 = new JTableOperator(operator);
        assertNotNull(operator1);
        
        operator1.valueChanged(new ListSelectionEvent(this, 0, 0, false));
    }
}
