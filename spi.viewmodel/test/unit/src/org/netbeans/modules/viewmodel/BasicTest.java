/*
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
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.viewmodel;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.viewmodel.TreeModelNode;
import org.netbeans.modules.viewmodel.TreeModelRoot;
import org.netbeans.modules.viewmodel.TreeTable;
import org.netbeans.spi.viewmodel.*;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;



/**
 * Tests the JPDABreakpointEvent.resume() functionality.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class BasicTest  extends NbTestCase {

    private String helpID = "A test help ID"; // NOI18N

    public BasicTest (String s) {
        super (s);
    }

    public void testBasic () throws Exception {
        ArrayList l = new ArrayList ();
        CompoundModel cm = new CompoundModel ();
        l.add (cm);
        Models.CompoundModel mcm = Models.createCompoundModel(l, helpID);
        TreeTable tt = (TreeTable) Models.createView(mcm);
        waitFinished ();
        Node n = tt.getExplorerManager ().
            getRootContext ();
        checkNode (n, "");
        if (cm.exception != null)
            cm.exception.printStackTrace ();
        assertNull ("Threading problem", cm.exception);
        // TODO: Expansion test does not work - probably written in a bad way...
        //assertEquals ("nodeExpanded notification number", 3, cm.expandedTest.size ());
        //assertEquals ("nodeExpanded ", cm.toBeExpandedTest, cm.expandedTest);
        assertEquals(n.getValue("propertiesHelpID"), helpID);
    }
    
    private void checkNode (Node n, String name) {
        // init
        //assertEquals (null, n.getShortDescription ());
        Node[] ns = n.getChildren ().getNodes ();
        waitFinished ();
        
        ns = n.getChildren ().getNodes ();
        if (name.length () < 4) {
            assertEquals (name, 3, ns.length);
            checkNode (ns [0], name + "a");
            checkNode (ns [1], name + "b");
            checkNode (ns [2], name + "c");
        } else
            assertEquals (ns.length, 0);
        
        if (name.length () > 0) {
            //assertEquals (name, n.getName ());
            n.getDisplayName ();
            String sd = n.getShortDescription ();
            n.getActions (false);
            waitFinished ();
            assertEquals (name, n.getDisplayName ());
            assertEquals (name + "WWW", sd);
            assertEquals (1, n.getActions (false).length);
        }
    }

    static void waitFinished () {
        TreeModelNode.getRequestProcessor ().post (new Runnable () {
            public void run () {}
        }).waitFinished ();
    }
    
    
    public void testMnemonics() throws Exception {
        ArrayList l = new ArrayList ();
        CompoundModel cm = new CompoundModel ();
        l.add (cm);
        TestColumnModel tcm = new TestColumnModel();
        l.add(tcm);
        Models.CompoundModel mcm = Models.createCompoundModel(l);
        TreeTable tt = (TreeTable) Models.createView(mcm);
        Node.Property[] columns = tt.columns;
        assertEquals(2, columns.length);
        assertEquals(new Character('e'), columns[1].getValue("ColumnMnemonicCharTTV"));
    }
    
    public static class CompoundModel implements TreeModel, 
    NodeModel, NodeActionsProvider, TableModel, TreeExpansionModel {

    
        private Vector listeners = new Vector ();
        
        private Throwable exception;

        private Map callNumbers = new HashMap ();
        protected synchronized void addCall (String methodName, Object node) {
            Map m = (Map) callNumbers.get (methodName);
            if (m == null)
                callNumbers.put (methodName, m = new HashMap ());
            if (m.containsKey (node)) {
                Object info = m.get(node);
                if (info instanceof Exception) {
                    System.err.println ("Second call of " + methodName + " method for the same node " + node);
                    System.err.println("First was at:");
                    ((Exception) info).printStackTrace();
                    System.err.println("Second is:");
                    Thread.dumpStack();
                    m.put (node, new Integer(2));
                } else {
                    int numCalls = ((Integer) info).intValue() + 1;
                    System.err.println (numCalls+". call of " + methodName + " method for the same node " + node);
                    Thread.dumpStack();
                    m.put (node, new Integer(numCalls));
                }
            } else {
                m.put (node, new Exception());
            }
        }

        void checkThread () {
            try {
                assertTrue ("The right thread", TreeModelNode.getRequestProcessor ().isRequestProcessorThread ());
            } catch (Throwable t) {
                exception = t;
            }
            /*;
            Thread t = Thread.currentThread ();
            if ( t.getClass ().getName ().startsWith 
                    (RequestProcessor.class.getName ())
            ) exception = new Exception ();
             */
        }

        // TreeModel ...............................................................

        /** 
         * Returns the root node of the tree or null, if the tree is empty.
         *
         * @return the root node of the tree or null
         */
        public Object getRoot () {
            addCall ("getRoot", null);
            return ROOT;
        }

        /** 
         * Returns children for given parent on given indexes.
         *
         * @param   parent a parent of returned nodes
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve dchildren for given node type
         *
         * @return  children for given parent on given indexes
         */
        public Object[] getChildren (Object parent, int from, int to) 
        throws UnknownTypeException {
            addCall ("getChildren", parent);
            if (parent == ROOT)
                return new Object[] {"a", "b", "c"};
            if (parent instanceof String)
                return new Object[] {parent + "a", parent + "b", parent + "c"};
            throw new UnknownTypeException (parent);
        }

        /**
         * Returns number of children for given node.
         * 
         * @param   node the parent node
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve children for given node type
         *
         * @return  true if node is leaf
         */
        public int getChildrenCount (Object node) throws UnknownTypeException {
            addCall ("getChildrenCount", node);
            if (node == ROOT)
                return 3;
            if (node instanceof String)
                return 3;
            throw new UnknownTypeException (node);
        }

        /**
         * Returns true if node is leaf.
         * 
         * @throws  UnknownTypeException if this TreeModel implementation is not
         *          able to resolve dchildren for given node type
         * @return  true if node is leaf
         */
        public boolean isLeaf (Object node) throws UnknownTypeException {
            addCall ("isLeaf", node);
            if (node == ROOT)
                return false;
            if (node instanceof String)
                return ((String) node).length () > 3;
            throw new UnknownTypeException (node);
        }


        // NodeModel ...........................................................

        /**
         * Returns display name for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve display name for given node type
         * @return  display name for given node
         */
        public String getDisplayName (Object node) throws UnknownTypeException {
            addCall ("getDisplayName", node);
            //checkThread ();
            if (node instanceof String)
                return (String) node;
            throw new UnknownTypeException (node);
        }

        /**
         * Returns tooltip for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve tooltip for given node type
         * @return  tooltip for given node
         */
        public String getShortDescription (Object node) 
        throws UnknownTypeException {
            addCall ("getShortDescription", node);
            //checkThread (); Short description is called on AWT! How else we could display a tooltip?
            if (node == ROOT)
                return "";
            if (node instanceof String)
                return node + "WWW";
            throw new UnknownTypeException (node);
        }

        /**
         * Returns icon for given node.
         *
         * @throws  UnknownTypeException if this NodeModel implementation is not
         *          able to resolve icon for given node type
         * @return  icon for given node
         */
        public String getIconBase (Object node) 
        throws UnknownTypeException {
            addCall ("getIconBase", node);
            //checkThread ();
            if (node instanceof String)
                return node + "XXX";
            throw new UnknownTypeException (node);
        }


        // NodeActionsProvider .....................................................

        /**
         * Performs default action for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        public void performDefaultAction (Object node) throws UnknownTypeException {
        }

        /**
         * Returns set of actions for given node.
         *
         * @throws  UnknownTypeException if this NodeActionsProvider implementation 
         *          is not able to resolve actions for given node type
         * @return  display name for given node
         */
        public Action[] getActions (Object node) throws UnknownTypeException {
            //checkThread ();
            if (node == ROOT)
                return new Action [0];
            if (node instanceof String)
                return new Action[] {
                    new AbstractAction ((String) node) {
                        public void actionPerformed (ActionEvent ev) {
                            
                        }
                    },
                };
            throw new UnknownTypeException (node);
        }


        // ColumnsModel ............................................................

        /**
         * Returns sorted array of 
         * {@link org.netbeans.spi.viewmodel.ColumnModel}s.
         *
         * @return sorted array of ColumnModels
         */
        public ColumnModel[] getColumns () {
            return new ColumnModel [0];
        }


        // TableModel ..............................................................

        public Object getValueAt (Object node, String columnID) throws 
        UnknownTypeException {
            addCall ("getValueAt", node);
            checkThread ();
            if (node instanceof String) {
                if (columnID.equals ("1"))
                    return node + "1";
                if (columnID.equals ("2"))
                    return node + "2";
            }
            throw new UnknownTypeException (node);
        }

        public boolean isReadOnly (Object node, String columnID) throws 
        UnknownTypeException {
            addCall ("isReadOnly", node);
            checkThread ();
            if (node instanceof String) {
                if (columnID.equals ("1"))
                    return true;
                if (columnID.equals ("2"))
                    return true;
            }
            throw new UnknownTypeException (node);
        }

        public void setValueAt (Object node, String columnID, Object value) throws 
        UnknownTypeException {
            throw new UnknownTypeException (node);
        }


        // TreeExpansionModel ......................................................

        private Set toBeExpandedTest = new HashSet ();
        private Set expandedTest = new HashSet ();
        {
            toBeExpandedTest.add ("a");
            toBeExpandedTest.add ("ab");
            toBeExpandedTest.add ("abc");
        }
        
        /**
         * Defines default state (collapsed, expanded) of given node.
         *
         * @param node a node
         * @return default state (collapsed, expanded) of given node
         */
        public boolean isExpanded (Object node) throws UnknownTypeException {
            if (node instanceof String)
                return toBeExpandedTest.contains (node);
            throw new UnknownTypeException (node);
        }

        /**
         * Called when given node is expanded.
         *
         * @param node a expanded node
         */
        public void nodeExpanded (Object node) {
            if (!toBeExpandedTest.contains (node)) {
                System.err.println("This node should not be expanded: " + node);
                Thread.dumpStack();
            }
            expandedTest.add (node);
        }

        /**
         * Called when given node is collapsed.
         *
         * @param node a collapsed node
         */
        public void nodeCollapsed (Object node) {
            System.err.println("nodeCollapsed " + node);
            Thread.dumpStack();
        }


        // listeners ...............................................................

        /** 
         * Registers given listener.
         * 
         * @param l the listener to add
         */
        public void addModelListener (ModelListener l) {
            listeners.add (l);
        }

        /** 
         * Unregisters given listener.
         *
         * @param l the listener to remove
         */
        public void removeModelListener (ModelListener l) {
            listeners.remove (l);
        }
        
        public void fire () {
            Vector v = (Vector) listeners.clone ();
            int i, k = v.size ();
            for (i = 0; i < k; i++)
                ((ModelListener) v.get (i)).modelChanged (null);
        }
        
        public void fire (ModelEvent event) {
            Vector v = (Vector) listeners.clone ();
            int i, k = v.size ();
            for (i = 0; i < k; i++) {
                ((ModelListener) v.get (i)).modelChanged (event);
            }
        }
    }
    
    private static class TestColumnModel extends ColumnModel {
        public Class getType() {
            return String.class;
        }

        public String getDisplayName() {
            return "Test";
        }

        public Character getDisplayedMnemonic() {
            return new Character('e');
        }

        public String getID() {
            return "xx";
        }

    }
}
