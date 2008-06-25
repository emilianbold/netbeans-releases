/*
 * $Id$
 *
 * ---------------------------------------------------------------------------
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Manfred Riem (mriem@netbeans.org).
 *
 * The Original Software is the Jemmy library. The Initial Developer of the
 * Original Software is Alexandre Iline. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.jemmy.operators;



import java.awt.Rectangle;

import java.util.Comparator;

import javax.swing.JFrame;

import javax.swing.JTree;

import javax.swing.event.TreeExpansionEvent;

import javax.swing.event.TreeExpansionListener;

import javax.swing.event.TreeSelectionEvent;

import javax.swing.event.TreeSelectionListener;

import javax.swing.event.TreeWillExpandListener;

import javax.swing.tree.ExpandVetoException;

import javax.swing.tree.TreePath;

import junit.framework.Test;

import junit.framework.TestCase;

import junit.framework.TestSuite;

import org.netbeans.jemmy.operators.JTreeOperator.NoSuchPathException;

import org.netbeans.jemmy.operators.Operator.StringComparator;

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

        

        operator2.findRow("1", new StringComparatorTest(), 0);

    }

    

    /**

     * Inner class needed for testing.

     */

    public class StringComparatorTest implements StringComparator {

        public boolean equals(String caption, String match) {

            return true;

        }

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

        

        try {

            operator2.doCollapsePath(null);

            fail();

        }

        catch(NoSuchPathException exception) {

        }

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

        operator2.waitExpanded(0);

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

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getDump();

    }



    /**

     * Test addSelectionInterval method.

     */

    public void testAddSelectionInterval() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.addSelectionInterval(0, 0);

        operator2.removeSelectionInterval(0, 0);

    }



    /**

     * Test addSelectionPath method.

     */

    public void testAddSelectionPath() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.addSelectionPath(null);

        operator2.removeSelectionPath(null);

    }



    /**

     * Test addSelectionPaths method.

     */

    public void testAddSelectionPaths() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.addSelectionPaths(null);

        operator2.removeSelectionPaths(null);

    }



    /**

     * Test addSelectionRow method.

     */

    public void testAddSelectionRow() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.addSelectionRow(0);

        operator2.removeSelectionRow(0);

    }



    /**

     * Test addSelectionRows method.

     */

    public void testAddSelectionRows() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.addSelectionRows(null);

        operator2.removeSelectionRows(null);

    }



    /**

     * Test addTreeExpansionListener method.

     */

    public void testAddTreeExpansionListener() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        TreeExpansionListenerTest listener = new TreeExpansionListenerTest();

        operator2.addTreeExpansionListener(listener);

        operator2.removeTreeExpansionListener(listener);

    }

    

    /**

     * Inner class used for testing.

     */

    public class TreeExpansionListenerTest implements TreeExpansionListener {

        public void treeExpanded(TreeExpansionEvent event) {

        }



        public void treeCollapsed(TreeExpansionEvent event) {

        }

    }



    /**

     * Test addTreeSelectionListener method.

     */

    public void testAddTreeSelectionListener() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        TreeSelectionListenerTest listener = new TreeSelectionListenerTest();

        operator2.addTreeSelectionListener(listener);

        operator2.removeTreeSelectionListener(listener);

    }



    /**

     * Inner class used for testing.

     */

    public class TreeSelectionListenerTest implements TreeSelectionListener {

        public void valueChanged(TreeSelectionEvent e) {

        }

    }

    

    /**

     * Test addTreeWillExpandListener method.

     */

    public void testAddTreeWillExpandListener() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        TreeWillExpandListenerTest listener = new TreeWillExpandListenerTest();

        operator2.addTreeWillExpandListener(listener);

        operator2.removeTreeWillExpandListener(listener);

    }



    /**

     * Inner class used for testing.

     */

    public class TreeWillExpandListenerTest implements TreeWillExpandListener {

        public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {

        }



        public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {

        }

    }

    

    /**

     * Test cancelEditing method.

     */

    public void testCancelEditing() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.cancelEditing();

    }



    /**

     * Test clearSelection method.

     */

    public void testClearSelection() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.clearSelection();

    }



    /**

     * Test collapsePath method.

     */

    public void testCollapsePath() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.collapsePath(null);

    }



    /**

     * Test collapseRow method.

     */

    public void testCollapseRow() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.collapseRow(0);

    }



    /**

     * Test convertValueToText method.

     */

    public void testConvertValueToText() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        try {

            operator2.convertValueToText(null, true, true, true, 0, true);

        }

        catch(UnsupportedOperationException exception) {

        }

    }



    /**

     * Test expandPath method.

     */

    public void testExpandPath() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.expandPath(null);

    }



    /**

     * Test expandRow method.

     */

    public void testExpandRow() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.expandRow(0);

    }



    /**

     * Test fireTreeCollapsed method.

     */

    public void testFireTreeCollapsed() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.fireTreeCollapsed(null);

    }



    /**

     * Test fireTreeExpanded method.

     */

    public void testFireTreeExpanded() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.fireTreeExpanded(null);

    }



    /**

     * Test fireTreeWillCollapse method.

     */

    public void testFireTreeWillCollapse() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.fireTreeWillCollapse(null);

    }



    /**

     * Test fireTreeWillExpand method.

     */

    public void testFireTreeWillExpand() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.fireTreeWillExpand(null);

    }



    /**

     * Test getCellEditor method.

     */

    public void testGetCellEditor() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setCellEditor(operator2.getCellEditor());

    }



    /**

     * Test getCellRenderer method.

     */

    public void testGetCellRenderer() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setCellRenderer(operator2.getCellRenderer());

    }



    /**

     * Test getClosestPathForLocation method.

     */

    public void testGetClosestPathForLocation() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getClosestPathForLocation(0, 0);

    }



    /**

     * Test getClosestRowForLocation method.

     */

    public void testGetClosestRowForLocation() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getClosestRowForLocation(0, 0);

    }



    /**

     * Test getEditingPath method.

     */

    public void testGetEditingPath() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getEditingPath();

    }



    /**

     * Test getExpandedDescendants method.

     */

    public void testGetExpandedDescendants() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getExpandedDescendants(null);

    }



    /**

     * Test getInvokesStopCellEditing method.

     */

    public void testGetInvokesStopCellEditing() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setInvokesStopCellEditing(operator2.getInvokesStopCellEditing());

    }



    /**

     * Test getLastSelectedPathComponent method.

     */

    public void testGetLastSelectedPathComponent() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getLastSelectedPathComponent();

    }



    /**

     * Test getLeadSelectionPath method.

     */

    public void testGetLeadSelectionPath() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getLeadSelectionPath();

    }



    /**

     * Test getLeadSelectionRow method.

     */

    public void testGetLeadSelectionRow() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getLeadSelectionRow();

    }



    /**

     * Test getMaxSelectionRow method.

     */

    public void testGetMaxSelectionRow() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getMaxSelectionRow();

    }



    /**

     * Test getMinSelectionRow method.

     */

    public void testGetMinSelectionRow() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getMinSelectionRow();

    }



    /**

     * Test getModel method.

     */

    public void testGetModel() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setModel(operator2.getModel());

    }



    /**

     * Test getPathBounds method.

     */

    public void testGetPathBounds() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getPathBounds(null);

    }



    /**

     * Test getPathForLocation method.

     */

    public void testGetPathForLocation() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getPathForLocation(0, 0);

    }



    /**

     * Test getPathForRow method.

     */

    public void testGetPathForRow() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getPathForRow(0);

    }



    /**

     * Test getPreferredScrollableViewportSize method.

     */

    public void testGetPreferredScrollableViewportSize() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getPreferredScrollableViewportSize();

    }



    /**

     * Test getRowBounds method.

     */

    public void testGetRowBounds() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getRowBounds(0);

    }



    /**

     * Test getRowCount method.

     */

    public void testGetRowCount() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getRowCount();

    }



    /**

     * Test getRowForLocation method.

     */

    public void testGetRowForLocation() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getRowForLocation(0 ,0);

    }



    /**

     * Test getRowForPath method.

     */

    public void testGetRowForPath() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getRowForPath(null);

    }



    /**

     * Test getRowHeight method.

     */

    public void testGetRowHeight() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setRowHeight(operator2.getRowHeight());

    }



    /**

     * Test getScrollableBlockIncrement method.

     */

    public void testGetScrollableBlockIncrement() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getScrollableBlockIncrement(new Rectangle(0, 0), 0, 0);

    }



    /**

     * Test getScrollableTracksViewportHeight method.

     */

    public void testGetScrollableTracksViewportHeight() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getScrollableTracksViewportHeight();

    }



    /**

     * Test getScrollableTracksViewportWidth method.

     */

    public void testGetScrollableTracksViewportWidth() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getScrollableTracksViewportWidth();

    }



    /**

     * Test getScrollableUnitIncrement method.

     */

    public void testGetScrollableUnitIncrement() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getScrollableUnitIncrement(new Rectangle(0, 0), 0, 0);

    }



    /**

     * Test getScrollsOnExpand method.

     */

    public void testGetScrollsOnExpand() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setScrollsOnExpand(operator2.getScrollsOnExpand());

    }



    /**

     * Test getSelectionCount method.

     */

    public void testGetSelectionCount() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getSelectionCount();

    }



    /**

     * Test getSelectionModel method.

     */

    public void testGetSelectionModel() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setSelectionModel(operator2.getSelectionModel());

    }



    /**

     * Test getSelectionPath method.

     */

    public void testGetSelectionPath() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setSelectionPath(operator2.getSelectionPath());

    }



    /**

     * Test getSelectionPaths method.

     */

    public void testGetSelectionPaths() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setSelectionPaths(operator2.getSelectionPaths());

    }



    /**

     * Test getSelectionRows method.

     */

    public void testGetSelectionRows() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setSelectionRows(operator2.getSelectionRows());

    }



    /**

     * Test getShowsRootHandles method.

     */

    public void testGetShowsRootHandles() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getShowsRootHandles();

    }



    /**

     * Test getUI method.

     */

    public void testGetUI() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getUI();

    }



    /**

     * Test getVisibleRowCount method.

     */

    public void testGetVisibleRowCount() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.getVisibleRowCount();

    }



    /**

     * Test hasBeenExpanded method.

     */

    public void testHasBeenExpanded() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.hasBeenExpanded(null);

    }



    /**

     * Test isCollapsed method.

     */

    public void testIsCollapsed() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.isCollapsed(0);

        operator2.isCollapsed(null);

    }



    /**

     * Test isEditable method.

     */

    public void testIsEditable() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setEditable(operator2.isEditable());

    }



    /**

     * Test isEditing method.

     */

    public void testIsEditing() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.isEditing();

    }



    /**

     * Test isExpanded method.

     */

    public void testIsExpanded() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.isExpanded(null);

        operator2.isExpanded(0);

    }



    /**

     * Test isFixedRowHeight method.

     */

    public void testIsFixedRowHeight() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.isFixedRowHeight();

    }



    /**

     * Test isLargeModel method.

     */

    public void testIsLargeModel() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.isLargeModel();

    }



    /**

     * Test isPathEditable method.

     */

    public void testIsPathEditable() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.isPathEditable(null);

    }



    /**

     * Test isPathSelected method.

     */

    public void testIsPathSelected() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.isPathSelected(null);

    }



    /**

     * Test isRootVisible method.

     */

    public void testIsRootVisible() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setRootVisible(operator2.isRootVisible());

    }



    /**

     * Test isRowSelected method.

     */

    public void testIsRowSelected() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.isRowSelected(0);

    }



    /**

     * Test isSelectionEmpty method.

     */

    public void testIsSelectionEmpty() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.isSelectionEmpty();

    }



    /**

     * Test isVisible method.

     */

    public void testIsVisible() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.isVisible();

        operator2.isVisible(null);

    }



    /**

     * Test makeVisible method.

     */

    public void testMakeVisible() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.makeVisible(null);

    }



    /**

     * Test scrollPathToVisible method.

     */

    public void testScrollPathToVisible() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.scrollPathToVisible(null);

    }



    /**

     * Test scrollRowToVisible method.

     */

    public void testScrollRowToVisible() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.scrollRowToVisible(0);

    }



    /**

     * Test setLargeModel method.

     */

    public void testSetLargeModel() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);

        

        operator2.setLargeModel(true);

    }



    /**

     * Test setSelectionInterval method.

     */

    public void testSetSelectionInterval() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setSelectionInterval(0 ,0);

    }



    /**

     * Test setSelectionRow method.

     */

    public void testSetSelectionRow() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setSelectionRow(0);

    }



    /**

     * Test setShowsRootHandles method.

     */

    public void testSetShowsRootHandles() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setShowsRootHandles(false);

    }



    /**

     * Test setUI method.

     */

    public void testSetUI() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setUI(operator2.getUI());

    }



    /**

     * Test setVisibleRowCount method.

     */

    public void testSetVisibleRowCount() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.setVisibleRowCount(1);

    }



    /**

     * Test startEditingAtPath method.

     */

    public void testStartEditingAtPath() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.startEditingAtPath(null);

    }



    /**

     * Test stopEditing method.

     */

    public void testStopEditing() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.stopEditing();

    }



    /**

     * Test treeDidChange method.

     */

    public void testTreeDidChange() {

        frame.setVisible(true);

        

        JFrameOperator operator = new JFrameOperator();

        assertNotNull(operator);

        

        JTreeOperator operator2 = new JTreeOperator(operator);

        assertNotNull(operator2);



        operator2.treeDidChange();

    }

}

