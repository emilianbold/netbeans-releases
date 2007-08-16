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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * TestOutlineDynamic.java
 *
 * Created on February 1, 2004, 12:53 PM
 */

package org.netbeans.swing.outline;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.openide.util.Utilities;

/** Another Outline test app - this one allows dynamic adding and removal of
 * nodes and provides an editable column called "comment".
 *
 * @author  Tim Boudreau
 */
public class TestOutlineDynamic extends JFrame implements ActionListener {
    private Outline outline;
    private TreeModel treeMdl;
    static int nodeCount = 0;
    
    /** Creates a new instance of Test */
    public TestOutlineDynamic() {
        setDefaultCloseOperation (EXIT_ON_CLOSE);
        getContentPane().setLayout (new BorderLayout());
  
        treeMdl = createModel();
        
        OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeMdl, 
            new NodeRowModel(), true);
        
        outline = new Outline();
        
       // outline.setRenderDataProvider(new RenderData()); 
        
        outline.setRootVisible (true);
        
        outline.setModel (mdl);
        
        JPanel buttons = new JPanel();
        
        JLabel jl = new JLabel("Read the button tooltips");
        
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.add(jl);
        
        final JButton add = new JButton ("Add child");
        final JButton remove = new JButton ("Delete child");
        final JButton clear = new JButton("Clear");
        final JButton addDis = new JButton ("Add discontiguous");
        final JButton removeDis = new JButton ("Delete discontiguous");
        
        addDis.setEnabled(false);
        removeDis.setEnabled(false);
        removeDis.setToolTipText("To enable, select more than one immediate child node of the same parent node");
        addDis.setToolTipText("To enable, select a node with more than one child");
        add.setToolTipText("Add a child to the selected node");
        remove.setToolTipText("Delete the selected node");
        clear.setToolTipText("Clear the model, leaving only the root node");
        clear.setEnabled(false);
        
        add.addActionListener (this);
        remove.addActionListener(this);
        clear.addActionListener(this);
        addDis.addActionListener(this);
        removeDis.addActionListener(this);
        add.setName("add");
        remove.setName("remove");
        clear.setName("clear");
        addDis.setName("addDis");
        removeDis.setName("removeDis");
        buttons.add (add);
        buttons.add(remove);
        buttons.add(clear);
        buttons.add(addDis);
        buttons.add(removeDis);
        
        add.setEnabled(false);
        remove.setEnabled(false);
        
        outline.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    boolean en = outline.getSelectedRow() != -1;
                    add.setEnabled(en);
                    remove.setEnabled(en && outline.getSelectedRow() != 0);
                    clear.setEnabled(outline.getRowCount() > 1);
                    
                    ListSelectionModel m =  
                        outline.getSelectionModel();
                    //en = (m.getMinSelectionIndex() != m.getMaxSelectionIndex());
                    en = getSelectedNode() != null;
                    if (en) {
                        DefaultMutableTreeNode nd = getSelectedNode();
                        en = nd.getChildCount() > 1;
                    }
                    addDis.setEnabled(en);
                    
                    en = getSelectedNode() != null;
                    if (en) {
                        int[] sels = getSelectedIndices();
                        en = sels.length > 1;
                        if (sels.length > outline.getRowCount()) {
                            en = false;
                        }
                        if (en) {
                            DefaultMutableTreeNode lastParent = null;
                            for (int i=0; i < sels.length; i++) {
                                DefaultMutableTreeNode nd = (DefaultMutableTreeNode)
                                    outline.getValueAt(sels[i], 0);
                                if (nd == null) {
                                    en = false;
                                    break;
                                }
                                if (lastParent != null) {
                                    en &= nd.getParent() == lastParent;
                                    if (!en) {
                                        break;
                                    }
                                } else {
                                    lastParent = (DefaultMutableTreeNode) nd.getParent();
                                }
                            }
                        }
                        
                    }
                    
                    removeDis.setEnabled(en);
                }
        });
                
        getContentPane().add(new JScrollPane(outline), BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.EAST);
        
        setBounds (20, 20, 700, 400);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        JButton b = (JButton) e.getSource();
        DefaultMutableTreeNode n = getSelectedNode();
        DefaultTreeModel mdl = (DefaultTreeModel) treeMdl;
        
        if ("add".equals(b.getName())) {
            Node newNode = new Node();
            DefaultMutableTreeNode nd = new DefaultMutableTreeNode(newNode, true);
            n.add(nd);
            
            mdl.nodesWereInserted(n, new int[] {n.getChildCount()-1});
            
//            mdl.insertNodeInto(new DefaultMutableTreeNode(newNode, true), n, n.getChildCount());
            
        } else if ("remove".equals(b.getName())) {
            mdl.removeNodeFromParent(n);
        } else if ("clear".equals(b.getName())) {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) mdl.getRoot();
            root.removeAllChildren();
            nodeCount = 1;
            mdl.reload(root);
        } else if ("addDis".equals(b.getName())) {
            DefaultMutableTreeNode nd = getSelectedNode();
            int ch = nd.getChildCount();
            
            DefaultMutableTreeNode atStart = new DefaultMutableTreeNode(new Node(), true);
            DefaultMutableTreeNode atEnd = new DefaultMutableTreeNode(new Node(), true);
            
            nd.insert(atEnd, ch);
            nd.insert(atStart, 0);
            
            mdl.nodesWereInserted(nd, new int[] {0, nd.getChildCount()-1});
            
            
        } else if ("removeDis".equals(b.getName())) {
            int[] sels = getSelectedIndices();
            
            
            //they all have the same parent if the button is enabled
            DefaultMutableTreeNode aNode = (DefaultMutableTreeNode) 
                outline.getValueAt(sels[0], 0);
            
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) aNode.getParent();
            
            //reverse sort the selections, so we remove nodes from bottom to top
            for (int i=0; i < sels.length; i++) {
                sels[i] *= -1;
            }
            Arrays.sort(sels);
            for (int i=0; i < sels.length; i++) {
                sels[i] *= -1;
            }

            System.err.println("Going to remove " + Arrays.asList(Utilities.toObjectArray(sels)));
            
            ArrayList nodes = new ArrayList();
            int[] indices = new int[sels.length];
            //Build the list of nodes we'll play with before we start
            //modifying the model - we can't do it while we're going
            for (int i=0; i < sels.length; i++) {
                aNode = (DefaultMutableTreeNode) outline.getValueAt(sels[i], 0);
                System.err.println("To delete user object class " + aNode.getUserObject().getClass() + " = " + aNode.getUserObject());
                nodes.add (aNode);
                indices[i] = parent.getIndex(aNode);
            }
            
            System.err.println("Will really remove indices " + Arrays.asList(Utilities.toObjectArray(indices)));
            
            for (int i=0; i < nodes.size(); i++) {
                aNode = (DefaultMutableTreeNode) nodes.get(i);
                if (aNode.getParent() != parent) {
                    System.err.println(aNode + " not child of " + parent + " but of " + aNode.getParent());
                } else {
                    System.err.println("REMOVING " + aNode + " from parent");
                    parent.remove(aNode);
                    nodes.add(aNode);
                }
            }
            
            mdl.nodesWereRemoved(parent, indices, nodes.toArray());
            
        }
    }
    
    private int[] getSelectedIndices() {
        ListSelectionModel lsm = outline.getSelectionModel();
        int min = lsm.getMinSelectionIndex();
        int max = lsm.getMaxSelectionIndex();
        if (min == max) {
            return new int[] {min};
        }
        ArrayList al = new ArrayList();
        for (int i=min; i <= max; i++) {
            if (lsm.isSelectedIndex(i)) {
                al.add (new Integer(i));
            }
        }
        Integer[] ints = (Integer[]) al.toArray(new Integer[0]);
        return (int[]) Utilities.toPrimitiveArray(ints);
    }
    
    public DefaultMutableTreeNode getSelectedNode() {
        return ((DefaultMutableTreeNode) outline.getValueAt(
            outline.getSelectedRow(), 0));
    }
    
    /** A handy method to create a model to install into a JTree to compare
     * behavior of a real JTree's layout cache and ours */
    public static TreeModel createModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new Node());
        TreeModel treeMdl = new DefaultTreeModel (root, false);
        return treeMdl;
    }
    
    public static void main(String[] ignored) {
        try {
           //UIManager.setLookAndFeel (new javax.swing.plaf.metal.MetalLookAndFeel());
        } catch (Exception e) {}
        
        new TestOutlineDynamic().show();
    }
    
    
    private class NodeRowModel implements RowModel {
        
        public Class getColumnClass(int column) {
            switch (column) {
                case 0 : return Integer.class;
                case 1 : return String.class;
                default : assert false;
            }
            return null;
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public String getColumnName(int column) {
            return column == 0 ? "Hash code" : "Comment";
        }
        
        public Object getValueFor(Object node, int column) {
            Node n = (Node) ((DefaultMutableTreeNode) node).getUserObject();
            switch (column) {
                case 0 : return new Integer(node.hashCode());
                case 1 : return n.getComment();
                default : assert false;
            }
            return null;
        }
        
        public boolean isCellEditable(Object node, int column) {
            return column == 1;
        }
        
        public void setValueFor(Object node, int column, Object value) {
            if (column == 1) {
                ((Node) ((DefaultMutableTreeNode) node).getUserObject())
                    .setComment(value.toString());
            }
        }
    }
    
    private static class Node {
        int idx;
        private String comment = "no comment";
        public Node() {
            idx = nodeCount++;
        }
        
        public String getComment() {
            return comment;
        }
        
        public void setComment(String s) {
            comment = s;
        }        
        
        public String toString() {
            return "Node " + idx;
        }
    }
}
