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
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * A JUnit test for JTreeOperator.
 *
 * @author Manfred Riem (mriem@netbeans.org)
 * @version $Revision$
 */
public class JTreeOperatorTest extends TestCase {
    /**
     * Stores the frame we use.
     */
    private JFrame frame;
    
    /**
     * Stores the tree we use.
     */
    private JTree tree;
    
    /**
     * Constructor.
     *
     * @param testName the name of the test.
     */
    public JTreeOperatorTest(String testName) {
        super(testName);
    }

    /**
     * Setup before testing.
     */
    protected void setUp() throws Exception {
        frame = new JFrame();
        tree  = new JTree();
        tree.setName("JTreeOperatorTest");
        frame.getContentPane().add(tree);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    /**
     * Cleanu after testing.
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
        TestSuite suite = new TestSuite(JTreeOperatorTest.class);
        
        return suite;
    }
    
    /**
     * Test constructor.
     */
    public void testConstructor() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);
        
        JTreeOperator operator3 = new JTreeOperator(operator, new NameComponentChooser("JTreeOperatorTest"));
        assertNotNull(operator3);
        
        operator3.selectRow(0);
        
        JTreeOperator operator4 = new JTreeOperator(operator, "JTree");
        assertNotNull(operator4);
    }

    /**
     * Test findJTree method.
     */
    public void testFindJTree() {
        frame.setVisible(true);
        
        JTree tree1 = JTreeOperator.findJTree(frame, "JTree", false, false, 0);
        assertNotNull(tree1);
        
        JTree tree2 = JTreeOperator.findJTree(frame, new NameComponentChooser("JTreeOperatorTest"));
        assertNotNull(tree2);
    }

    /**
     * Test waitJTree method.
     */
    public void testWaitJTree() {
        frame.setVisible(true);
        
        JTree tree1 = JTreeOperator.waitJTree(frame, "JTree", false, false, 0);
        assertNotNull(tree1);
        
        JTree tree2 = JTreeOperator.waitJTree(frame, new NameComponentChooser("JTreeOperatorTest"));
        assertNotNull(tree2);
    }

    /**
     * Test doExpandPath method.
     */
    public void testDoExpandPath() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        operator2.doExpandPath(path);
    }

    /**
     * Test doExpandRow method.
     */
    public void testDoExpandRow() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        operator2.expandRow(0);
    }

    /**
     * Test doMakeVisible method.
     */
    public void testDoMakeVisible() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        operator2.doMakeVisible(path);
    }

    /**
     * Test getChildCount method.
     */
    public void testGetChildCount() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        assertEquals(3, operator2.getChildCount(node));
    }

    /**
     * Test getChildren method.
     */
    public void testGetChildren() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        assertNotNull(operator2.getChildren(node));
    }

    /**
     * Test getChild method.
     */
    public void testGetChild() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        assertNotNull(operator2.getChild(node, 0));
    }

    /**
     * Test getChildPath method.
     */
    public void testGetChildPath() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        assertNotNull(operator2.getChildPath(path, 0));
    }

    /**
     * Test getChildPaths method.
     */
    public void testGetChildPaths() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        assertNotNull(operator2.getChildPaths(path));
    }

    /**
     * Test getRoot method.
     */
    public void testGetRoot() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        assertNotNull(operator2.getRoot());
    }

    /**
     * Test findPath method.
     */
    public void testFindPath() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        assertNotNull(operator2.findPath("colors"));
    }

    /**
     * Test findRow method.
     */
    public void testFindRow() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        assertEquals(-1, operator2.findRow(new NameComponentChooser("colors")));
    }

    /**
     * Test doCollapsePath method.
     */
    public void testDoCollapsePath() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        operator2.doCollapsePath(path);
    }

    /**
     * Test doCollapseRow method.
     */
    public void testDoCollapseRow() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        operator2.doCollapseRow(0);
    }

    /**
     * Test selectPath method.
     */
    public void testSelectPath() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        operator2.selectPath(path);
    }

    /**
     * Test selectRow method.
     */
    public void testSelectRow() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        operator2.selectRow(0);
    }

    /**
     * Test selectPaths method.
     */
    public void testSelectPaths() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath[] paths = new TreePath[1];
        paths[0] = new TreePath(node);
        
        operator2.selectPaths(paths);
    }

    /**
     * Test getPointToClick method.
     */
    public void testGetPointToClick() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        operator2.getPointToClick(0);
    }

    /**
     * Test clickOnPath method.
     */
    public void testClickOnPath() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        operator2.clickOnPath(path);
    }

    /**
     * Test callPopupOnPaths method.
     */
    public void testCallPopupOnPaths() {
        
    }

    /**
     * Test callPopupOnPath method.
     */
    public void testCallPopupOnPath() {
        
    }

    /**
     * Test scrollToPath method.
     */
    public void testScrollToPath() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        operator2.scrollToPath(path);
    }

    /**
     * Test scrollToRow method.
     */
    public void testScrollToRow() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        operator2.scrollToRow(0);
    }

    /**
     * Test clickForEdit method.
     */
    public void testClickForEdit() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        // operator2.clickForEdit(path);
    }

    /**
     * Test getRenderedComponent method.
     */
    public void testGetRenderedComponent() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        operator2.getRenderedComponent(path);
    }

    /**
     * Test changePathText method.
     */
    public void testChangePathText() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        // operator2.changePathText(path, "BlaBla");
    }

    /**
     * Test changePathObject method.
     */
    public void testChangePathObject() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        // operator2.changePathObject(path, new Integer(1));
    }

    /**
     * Test waitExpanded method.
     */
    public void testWaitExpanded() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        operator2.waitExpanded(path);
    }

    /**
     * Test waitCollapsed method.
     */
    public void testWaitCollapsed() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        operator2.collapsePath(path);
        operator2.waitCollapsed(path);
    }

    /**
     * Test waitVisible method.
     */
    public void testWaitVisible() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        operator2.waitVisible(path);
    }

    /**
     * Test waitSelected method.
     */
    public void testWaitSelected() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);

        operator2.selectPath(path);
        operator2.waitSelected(path);
    }

    /**
     * Test waitRow method.
     */
    public void testWaitRow() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);

        operator2.waitRow("colors", 1);
    }

    /**
     * Test chooseSubnode method.
     */
    public void testChooseSubnode() {
        frame.setVisible(true);
        
        JFrameOperator operator = new JFrameOperator();
        assertNotNull(operator);
        
        JTreeOperator operator2 = new JTreeOperator(operator);
        assertNotNull(operator2);

        Object node = tree.getModel().getRoot();
        TreePath path = new TreePath(node);
        // operator2.chooseSubnode(path, "colors");
    }

    /**
     * Test getDump method.
     */
    public void testGetDump() {
        
    }

    /**
     * Test addSelectionInterval method.
     */
    public void testAddSelectionInterval() {
        
    }

    /**
     * Test addSelectionPath method.
     */
    public void testAddSelectionPath() {
        
    }

    /**
     * Test addSelectionPaths method.
     */
    public void testAddSelectionPaths() {
        
    }

    /**
     * Test addSelectionRow method.
     */
    public void testAddSelectionRow() {
        
    }

    /**
     * Test addSelectionRows method.
     */
    public void testAddSelectionRows() {
        
    }

    /**
     * Test addTreeExpansionListener method.
     */
    public void testAddTreeExpansionListener() {
        
    }

    /**
     * Test addTreeSelectionListener method.
     */
    public void testAddTreeSelectionListener() {
        
    }

    /**
     * Test addTreeWillExpandListener method.
     */
    public void testAddTreeWillExpandListener() {
        
    }

    /**
     * Test cancelEditing method.
     */
    public void testCancelEditing() {
        
    }

    /**
     * Test clearSelection method.
     */
    public void testClearSelection() {
        
    }

    /**
     * Test collapsePath method.
     */
    public void testCollapsePath() {
        
    }

    /**
     * Test collapseRow method.
     */
    public void testCollapseRow() {
        
    }

    /**
     * Test convertValueToText method.
     */
    public void testConvertValueToText() {
        
    }

    /**
     * Test expandPath method.
     */
    public void testExpandPath() {
        
    }

    /**
     * Test expandRow method.
     */
    public void testExpandRow() {
        
    }

    /**
     * Test fireTreeCollapsed method.
     */
    public void testFireTreeCollapsed() {
        
    }

    /**
     * Test fireTreeExpanded method.
     */
    public void testFireTreeExpanded() {
        
    }

    /**
     * Test fireTreeWillCollapse method.
     */
    public void testFireTreeWillCollapse() {
        
    }

    /**
     * Test fireTreeWillExpand method.
     */
    public void testFireTreeWillExpand() {
        
    }

    /**
     * Test getCellEditor method.
     */
    public void testGetCellEditor() {
        
    }

    /**
     * Test getCellRenderer method.
     */
    public void testGetCellRenderer() {
        
    }

    /**
     * Test getClosestPathForLocation method.
     */
    public void testGetClosestPathForLocation() {
        
    }

    /**
     * Test getClosestRowForLocation method.
     */
    public void testGetClosestRowForLocation() {
        
    }

    /**
     * Test getEditingPath method.
     */
    public void testGetEditingPath() {
        
    }

    /**
     * Test getExpandedDescendants method.
     */
    public void testGetExpandedDescendants() {
        
    }

    /**
     * Test getInvokesStopCellEditing method.
     */
    public void testGetInvokesStopCellEditing() {
        
    }

    /**
     * Test getLastSelectedPathComponent method.
     */
    public void testGetLastSelectedPathComponent() {
        
    }

    /**
     * Test getLeadSelectionPath method.
     */
    public void testGetLeadSelectionPath() {
        
    }

    /**
     * Test getLeadSelectionRow method.
     */
    public void testGetLeadSelectionRow() {
        
    }

    /**
     * Test getMaxSelectionRow method.
     */
    public void testGetMaxSelectionRow() {
        
    }

    /**
     * Test getMinSelectionRow method.
     */
    public void testGetMinSelectionRow() {
        
    }

    /**
     * Test getModel method.
     */
    public void testGetModel() {
        
    }

    /**
     * Test getPathBounds method.
     */
    public void testGetPathBounds() {
        
    }

    /**
     * Test getPathForLocation method.
     */
    public void testGetPathForLocation() {
        
    }

    /**
     * Test getPathForRow method.
     */
    public void testGetPathForRow() {
        
    }

    /**
     * Test getPreferredScrollableViewportSize method.
     */
    public void testGetPreferredScrollableViewportSize() {
        
    }

    /**
     * Test getRowBounds method.
     */
    public void testGetRowBounds() {
        
    }

    /**
     * Test getRowCount method.
     */
    public void testGetRowCount() {
        
    }

    /**
     * Test getRowForLocation method.
     */
    public void testGetRowForLocation() {
        
    }

    /**
     * Test getRowForPath method.
     */
    public void testGetRowForPath() {
        
    }

    /**
     * Test getRowHeight method.
     */
    public void testGetRowHeight() {
        
    }

    /**
     * Test getScrollableBlockIncrement method.
     */
    public void testGetScrollableBlockIncrement() {
        
    }

    /**
     * Test getScrollableTracksViewportHeight method.
     */
    public void testGetScrollableTracksViewportHeight() {
        
    }

    /**
     * Test getScrollableTracksViewportWidth method.
     */
    public void testGetScrollableTracksViewportWidth() {
        
    }

    /**
     * Test getScrollableUnitIncrement method.
     */
    public void testGetScrollableUnitIncrement() {
        
    }

    /**
     * Test getScrollsOnExpand method.
     */
    public void testGetScrollsOnExpand() {
        
    }

    /**
     * Test getSelectionCount method.
     */
    public void testGetSelectionCount() {
        
    }

    /**
     * Test getSelectionModel method.
     */
    public void testGetSelectionModel() {
        
    }

    /**
     * Test getSelectionPath method.
     */
    public void testGetSelectionPath() {
        
    }

    /**
     * Test getSelectionPaths method.
     */
    public void testGetSelectionPaths() {
        
    }

    /**
     * Test getSelectionRows method.
     */
    public void testGetSelectionRows() {
        
    }

    /**
     * Test getShowsRootHandles method.
     */
    public void testGetShowsRootHandles() {
        
    }

    /**
     * Test getUI method.
     */
    public void testGetUI() {
        
    }

    /**
     * Test getVisibleRowCount method.
     */
    public void testGetVisibleRowCount() {
        
    }

    /**
     * Test hasBeenExpanded method.
     */
    public void testHasBeenExpanded() {
        
    }

    /**
     * Test isCollapsed method.
     */
    public void testIsCollapsed() {
        
    }

    /**
     * Test isEditable method.
     */
    public void testIsEditable() {
        
    }

    /**
     * Test isEditing method.
     */
    public void testIsEditing() {
        
    }

    /**
     * Test isExpanded method.
     */
    public void testIsExpanded() {
        
    }

    /**
     * Test isFixedRowHeight method.
     */
    public void testIsFixedRowHeight() {
        
    }

    /**
     * Test isLargeModel method.
     */
    public void testIsLargeModel() {
        
    }

    /**
     * Test isPathEditable method.
     */
    public void testIsPathEditable() {
        
    }

    /**
     * Test isPathSelected method.
     */
    public void testIsPathSelected() {
        
    }

    /**
     * Test isRootVisible method.
     */
    public void testIsRootVisible() {
        
    }

    /**
     * Test isRowSelected method.
     */
    public void testIsRowSelected() {
        
    }

    /**
     * Test isSelectionEmpty method.
     */
    public void testIsSelectionEmpty() {
        
    }

    /**
     * Test isVisible method.
     */
    public void testIsVisible() {
        
    }

    /**
     * Test makeVisible method.
     */
    public void testMakeVisible() {
        
    }

    /**
     * Test removeSelectionInterval method.
     */
    public void testRemoveSelectionInterval() {
        
    }

    /**
     * Test removeSelectionPath method.
     */
    public void testRemoveSelectionPath() {
        
    }

    /**
     * Test removeSelectionPaths method.
     */
    public void testRemoveSelectionPaths() {
        
    }

    /**
     * Test removeSelectionRow method.
     */
    public void testRemoveSelectionRow() {
        
    }

    /**
     * Test removeSelectionRows method.
     */
    public void testRemoveSelectionRows() {
        
    }

    /**
     * Test removeTreeExpansionListener method.
     */
    public void testRemoveTreeExpansionListener() {
        
    }

    /**
     * Test removeTreeSelectionListener method.
     */
    public void testRemoveTreeSelectionListener() {
        
    }

    /**
     * Test removeTreeWillExpandListener method.
     */
    public void testRemoveTreeWillExpandListener() {
        
    }

    /**
     * Test scrollPathToVisible method.
     */
    public void testScrollPathToVisible() {
        
    }

    /**
     * Test scrollRowToVisible method.
     */
    public void testScrollRowToVisible() {
        
    }

    /**
     * Test setCellEditor method.
     */
    public void testSetCellEditor() {
        
    }

    /**
     * Test setCellRenderer method.
     */
    public void testSetCellRenderer() {
        
    }

    /**
     * Test setEditable method.
     */
    public void testSetEditable() {
        
    }

    /**
     * Test setInvokesStopCellEditing method.
     */
    public void testSetInvokesStopCellEditing() {
        
    }

    /**
     * Test setLargeModel method.
     */
    public void testSetLargeModel() {
        
    }

    /**
     * Test setModel method.
     */
    public void testSetModel() {
        
    }

    /**
     * Test setRootVisible method.
     */
    public void testSetRootVisible() {
        
    }

    /**
     * Test setRowHeight method.
     */
    public void testSetRowHeight() {
        
    }

    /**
     * Test setScrollsOnExpand method.
     */
    public void testSetScrollsOnExpand() {
        
    }

    /**
     * Test setSelectionInterval method.
     */
    public void testSetSelectionInterval() {
        
    }

    /**
     * Test setSelectionModel method.
     */
    public void testSetSelectionModel() {
        
    }

    /**
     * Test setSelectionPath method.
     */
    public void testSetSelectionPath() {
        
    }

    /**
     * Test setSelectionPaths method.
     */
    public void testSetSelectionPaths() {
        
    }

    /**
     * Test setSelectionRow method.
     */
    public void testSetSelectionRow() {
        
    }

    /**
     * Test setSelectionRows method.
     */
    public void testSetSelectionRows() {
        
    }

    /**
     * Test setShowsRootHandles method.
     */
    public void testSetShowsRootHandles() {
        
    }

    /**
     * Test setUI method.
     */
    public void testSetUI() {
        
    }

    /**
     * Test setVisibleRowCount method.
     */
    public void testSetVisibleRowCount() {
        
    }

    /**
     * Test startEditingAtPath method.
     */
    public void testStartEditingAtPath() {
        
    }

    /**
     * Test stopEditing method.
     */
    public void testStopEditing() {
        
    }

    /**
     * Test treeDidChange method.
     */
    public void testTreeDidChange() {
        
    }
    
}
