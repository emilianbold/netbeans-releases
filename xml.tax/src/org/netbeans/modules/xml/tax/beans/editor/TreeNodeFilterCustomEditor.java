/*
 * TreeNodeFilterCustomEditor.java -- synopsis.
 *
 *
 * SUN PROPRIETARY/CONFIDENTIAL:  INTERNAL USE ONLY.
 *
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
package org.netbeans.modules.xml.tax.beans.editor;

import java.util.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

import org.netbeans.tax.*;
import org.netbeans.tax.traversal.TreeNodeFilter;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TreeNodeFilterCustomEditor extends JPanel implements EnhancedCustomPropertyEditor {
    /** */
    private static final boolean DEBUG      = false;
    /** */
    private static final boolean DEBUG_TREE = false;

    /** */
    private static final long serialVersionUID = 1767193347881681541L;    

    
    /** */
    private static final Map publicNodeTypeNamesMap = new HashMap();


    //
    // Static initialization
    //
        
    static {
        publicNodeTypeNamesMap.put (TreeNode.class, Util.getString ("NAME_Any_Node_Type"));
        publicNodeTypeNamesMap.put (TreeParentNode.class, Util.getString ("NAME_Any_Parent_Node_Type"));
        publicNodeTypeNamesMap.put (TreeCharacterData.class, Util.getString ("NAME_Any_Character_Data_Node_Type"));
        publicNodeTypeNamesMap.put (TreeReference.class, Util.getString ("NAME_Any_Reference_Node_Type"));
//          publicNodeTypeNamesMap.put (TreeEntityReference.class, Util.getString ("NAME_Any_Entity_Reference_Node_Type"));
        publicNodeTypeNamesMap.put (TreeNodeDecl.class, Util.getString ("NAME_Any_Declaration_Node_Type"));

        publicNodeTypeNamesMap.put (TreeComment.class, Util.getString ("NAME_Comment_Node_Type"));
        publicNodeTypeNamesMap.put (TreeProcessingInstruction.class, Util.getString ("NAME_Processing_Instruction_Node_Type"));
        publicNodeTypeNamesMap.put (TreeText.class, Util.getString ("NAME_Text_Node_Type"));
        publicNodeTypeNamesMap.put (TreeCDATASection.class, Util.getString ("NAME_CDATA_Section_Node_Type"));
        publicNodeTypeNamesMap.put (TreeElement.class, Util.getString ("NAME_Element_Node_Type"));
        publicNodeTypeNamesMap.put (TreeAttribute.class, Util.getString ("NAME_Attribute_Node_Type"));
//          publicNodeTypeNamesMap.put (TreeDocument.class, Util.getString ("NAME_Document_Node_Type"));
//          publicNodeTypeNamesMap.put (TreeDTD.class, Util.getString ("NAME_DTD_Node_Type"));
        publicNodeTypeNamesMap.put (TreeConditionalSection.class, Util.getString ("NAME_Conditional_Section_Node_Type"));
        publicNodeTypeNamesMap.put (TreeDocumentType.class, Util.getString ("NAME_Document_Type_Node_Type"));
        publicNodeTypeNamesMap.put (TreeGeneralEntityReference.class, Util.getString ("NAME_General_Entity_Reference_Node_Type"));
        publicNodeTypeNamesMap.put (TreeParameterEntityReference.class, Util.getString ("NAME_Parameter_Entity_Reference_Node_Type"));
        publicNodeTypeNamesMap.put (TreeElementDecl.class, Util.getString ("NAME_Element_Declaration_Node_Type"));
        publicNodeTypeNamesMap.put (TreeEntityDecl.class, Util.getString ("NAME_Entity_Declaration_Node_Type"));
        publicNodeTypeNamesMap.put (TreeAttlistDecl.class, Util.getString ("NAME_Attlist_Declaration_Node_Type"));
        publicNodeTypeNamesMap.put (TreeNotationDecl.class, Util.getString ("NAME_Notation_Declaration_Node_Type"));
    }


    /** */
    private final TreeNodeFilter filter;
    
    /** */
    private final List nodeTypesList;

    /** */
    private NodeTypesTableModel tableModel;


    //
    // init
    //
    
    
    /** Creates new TreeNodeFilterEditor */
    public TreeNodeFilterCustomEditor (TreeNodeFilter filter) {
        this.filter = filter;
        this.nodeTypesList = new LinkedList (Arrays.asList (filter.getNodeTypes()));
        
        initComponents();
        ownInitComponents();
    }


    /**
     */
    private void ownInitComponents () {
        tableModel = (NodeTypesTableModel)nodeTypesTable.getModel();

        ListSelectionModel selModel = nodeTypesTable.getSelectionModel();
        selModel.addListSelectionListener (new ListSelectionListener () {
                public void valueChanged (ListSelectionEvent e) {
                    if (e.getValueIsAdjusting())
                        return;
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    if (lsm.isSelectionEmpty()) {
                        removeButton.setEnabled (false);
                    } else {
                        removeButton.setEnabled (true);
                    }
                }
            });
            
//          Object[] array = publicNodeTypeNamesMap.keySet().toArray();
//          for (int i = 0; i < array.length; i++) {
//              array[i] = new NamedClass ((Class)array[i]);
//          }
//          Arrays.sort (array, new NamedClassComparator());
//          JComboBox cb = new JComboBox (array);

        JComboBox cb = new JComboBox (getPublicNodeTypesInheritanceTree());
        cb.setEditable (false);
        DefaultCellEditor dce = new DefaultCellEditor (cb);
//          dce.setClickCountToStart (2);
        nodeTypesTable.getColumnModel().getColumn (0).setCellEditor (dce);     
    }

    
    /**
     * @return Returns the property value that is result of the CustomPropertyEditor.
     * @exception InvalidStateException when the custom property editor does not represent valid property value
     *            (and thus it should not be set)
     */
    public Object getPropertyValue () throws IllegalStateException {
        short acceptPolicy = acceptRadioButton.isSelected() ?
            TreeNodeFilter.ACCEPT_TYPES :
            TreeNodeFilter.REJECT_TYPES;
        Class[] nodeTypes = (Class[])nodeTypesList.toArray (new Class[0]);

        return new TreeNodeFilter (nodeTypes, acceptPolicy);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        acceptPolicyGroup = new javax.swing.ButtonGroup();
        acceptPolicyPanel = new javax.swing.JPanel();
        acceptRejectLabel = new javax.swing.JLabel();
        acceptRadioButton = new javax.swing.JRadioButton();
        acceptRadioButton.setSelected(filter.getAcceptPolicy() == TreeNodeFilter.ACCEPT_TYPES);
        rejectRadioButton = new javax.swing.JRadioButton();
        rejectRadioButton.setSelected(filter.getAcceptPolicy() == TreeNodeFilter.REJECT_TYPES);
        tablePanel = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        nodeTypesTable = new javax.swing.JTable();
        nodeTypesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        
        
        setLayout(new java.awt.BorderLayout());
        
        acceptPolicyPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        acceptRejectLabel.setText(Util.getString("LBL_acceptReject"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        acceptPolicyPanel.add(acceptRejectLabel, gridBagConstraints1);
        
        acceptRadioButton.setText(Util.getString("LBL_showItRadioButton"));
        acceptPolicyGroup.add(acceptRadioButton);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        acceptPolicyPanel.add(acceptRadioButton, gridBagConstraints1);
        
        rejectRadioButton.setText(Util.getString("LBL_hideItRadioButton"));
        acceptPolicyGroup.add(rejectRadioButton);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(0, 12, 0, 11);
        gridBagConstraints1.weightx = 1.0;
        acceptPolicyPanel.add(rejectRadioButton, gridBagConstraints1);
        
        add(acceptPolicyPanel, java.awt.BorderLayout.NORTH);
        
        tablePanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints2;
        
        nodeTypesTable.setModel(new NodeTypesTableModel());
        nodeTypesTable.setPreferredScrollableViewportSize(new java.awt.Dimension(300, 200));
        tableScrollPane.setViewportView(nodeTypesTable);
        
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints2.insets = new java.awt.Insets(11, 12, 0, 0);
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        tablePanel.add(tableScrollPane, gridBagConstraints2);
        
        addButton.setText(Util.getString("LBL_addButton"));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets(11, 11, 0, 11);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTH;
        tablePanel.add(addButton, gridBagConstraints2);
        
        removeButton.setText(Util.getString("LBL_removeButton"));
        removeButton.setEnabled(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        
        gridBagConstraints2 = new java.awt.GridBagConstraints();
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints2.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.insets = new java.awt.Insets(5, 11, 11, 11);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTH;
        tablePanel.add(removeButton, gridBagConstraints2);
        
        add(tablePanel, java.awt.BorderLayout.CENTER);
        
    }//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        // Add your handling code here:
        int sel = nodeTypesTable.getSelectedRow();
        if (sel != -1) {
            tableModel.removeRow (sel);

            int numRows = nodeTypesTable.getModel().getRowCount();
            if (numRows > 0) {
                sel = Math.min (sel, numRows - 1);
                nodeTypesTable.getSelectionModel().setSelectionInterval (sel, sel);
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        // Add your handling code here:
        nodeTypesList.add (TreeNode.class);
        tableModel.fireTableDataChanged();        
    }//GEN-LAST:event_addButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup acceptPolicyGroup;
    private javax.swing.JPanel acceptPolicyPanel;
    private javax.swing.JLabel acceptRejectLabel;
    private javax.swing.JRadioButton acceptRadioButton;
    private javax.swing.JRadioButton rejectRadioButton;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JTable nodeTypesTable;
    private javax.swing.JButton addButton;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables


    
    //
    // class RowKeyListener
    //
    
    /** deletes whole row by pressing DELETE on row column. */
    private class RowKeyListener extends KeyAdapter {
        
        /** */
        private JTable table;
        
        
        //
        // init
        //
        
        public RowKeyListener (JTable table) {
            this.table = table;
        }
        
        
        //
        // itself
        //
        
        /**
         */
        public void keyReleased (KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                tableModel.removeRow (table.getSelectedRow());
            }
        }
    }


    //
    // class NodeTypesTableModel
    //

    /**
     *
     */
    private class NodeTypesTableModel extends AbstractTableModel {

        private static final long serialVersionUID =-1438087942670592779L;
        
        /**
         */
        public void removeRow (int row) {
            nodeTypesList.remove (row);
            fireTableDataChanged();        
        }


        /** Returns the number of rows in the model */
        public int getRowCount () {
            return nodeTypesList.size();
        }

        /** Returns the number of columns in the model */
        public int getColumnCount () {
            return 1;
        }

        /** Returns the class for a model. */
        public Class getColumnClass (int index) {
            return Class.class;
        }

	/**
	 */
        public Object getValueAt (int row, int column) {
            Object retVal = new Item (new NamedClass ((Class)nodeTypesList.get (row)));

            if ( DEBUG ) {
                Util.debug ("<-- getValue: row    = " + row); // NOI18N
                Util.debug ("<-- getValue: column = " + column); // NOI18N
                Util.debug ("<-- getValue: " + retVal.getClass().getName() + " -- '" + retVal + "'"); // NOI18N
            }            

            return retVal;
        }

	/**
	 */
        public void setValueAt (Object val, int row, int column) {
            if ( row >= nodeTypesList.size() ) {
                // fixed ArrayIndexOutOfBounds on nodeTypesList.set (row, type);
                //   1) select last row of multi row table
                //   2) try to edit -- show combo box
                //   3) remove this row
                //   4) click to another row
                //   5) exception occur
                return;
            }

            Class type = null;

            if ( DEBUG ) {
                Util.debug ("--> setValue: " + val.getClass().getName() + " -- '" + val + "'"); // NOI18N
                Util.debug ("--> setValue: row    = " + row); // NOI18N
                Util.debug ("--> setValue: column = " + column); // NOI18N
            }

            if ( val instanceof String ) {
                try {
                    type = Class.forName (val.toString());
                } catch (ClassNotFoundException exc) {
                    // DO NOTHING
                }
            } else if ( val instanceof Item ) {
                type = ((Item)val).clazz.clazz;
            } else if ( val instanceof NamedClass ) {
                type = ((NamedClass)val).clazz;
            }
            
            if ( ( type == null ) ||
                 ( TreeNodeFilter.isValidNodeType (type) == false ) ) {
                Util.notifyWarning (Util.getString ("MSG_invalidNodeType", val));
                return;
            }
            nodeTypesList.set (row, type);
        }

	/**
	 */
        public String getColumnName (int column) {
            return Util.getString ("LBL_nodeType");
        }

        /** Returns true for all cells which are editable. For a 
         * a new cell is editable only name field.
         */
        public boolean isCellEditable (int rowIndex, int columnIndex) {
            return true;
        }

    } // end: class NodeTypesTableModel


    //
    // NamedClass
    //
    
    /**
     *
     */
    private static class NamedClass {

        /** */
        private final Class clazz;
        
        /** */
        public NamedClass (Class clazz) {
            this.clazz = clazz;
        }


        /**
         */
        public String toString () {
            String name = (String)publicNodeTypeNamesMap.get (clazz);

            if ( name == null ) {
                name = clazz.getName();
            }

            return name;
        }

        /**
         */
        public boolean equals (Object obj) {
            if ( (obj instanceof NamedClass) == false ) {
                return false;
            }
            NamedClass peer = (NamedClass)obj;
            return clazz.equals (peer.clazz);
        }

    } // end: class NamedClass


    //
    // NamedClassComparator
    //
    
    /**
     *
     */
    private static class NamedClassComparator implements Comparator {

        /**
         */
        public int compare (Object obj1, Object obj2) throws ClassCastException {
            return (obj1.toString().compareTo (obj2.toString()));
        }

        /**
         */
        public boolean equals (Object obj) {
            return ( obj instanceof NamedClassComparator );
        }

    } // end: class NamedClassComparator



    //
    // InheritanceTree
    //

    /** */
    private static Vector publicNodeTypesInheritanceTree;


    /**
     */
    private static Vector getPublicNodeTypesInheritanceTree () {
        if ( publicNodeTypesInheritanceTree == null ) {
            if ( DEBUG_TREE ) {
                Util.debug ("Init Set"); // NOI18N
            }
            
            Item rootItem = new Item();
            Object[] array = publicNodeTypeNamesMap.keySet().toArray();
            for (int i = 0; i < array.length; i++) {
                Class clazz = (Class)array[i];
                Item.insertItemIntoLayer (rootItem.layer, Item.getItem (clazz));
                
                if ( clazz.isInterface() ) {
                    for (int j = 0; j < i; j++) {
                        Item.insertItemIntoLayer (rootItem.layer, Item.getItem ((Class)array[j]));
                    }
                }
            }
            
            publicNodeTypesInheritanceTree = new Vector();
            fillPublicNodeTypesInheritanceTree (rootItem.layer, ""); // NOI18N

            Item.itemMap.clear();
            Item.itemMap = null;
            rootItem = null;
        }

        return publicNodeTypesInheritanceTree;
    }
    
    /**
     */
    private static void fillPublicNodeTypesInheritanceTree (Set layer, String prefix) {
        Iterator it = layer.iterator();
        while ( it.hasNext() ) {
            Item item = (Item) it.next();
            String itemPrefix = ""; // NOI18N
            if ( prefix.length() != 0 ) {
                if ( it.hasNext() ) {
                    itemPrefix = prefix + "|- "; // NOI18N
                } else {
                    itemPrefix = prefix + "`- "; // NOI18N
                }
            }
            Item newItem = new Item (item, itemPrefix);
            
            if ( DEBUG_TREE ) {
                Util.debug (">>" + newItem.toString() + "<<"); // NOI18N
            }

            publicNodeTypesInheritanceTree.add (newItem);

            String newPrefix;
            if ( prefix.length() == 0 ) {
                newPrefix = "   "; // NOI18N
            } else {
                if ( it.hasNext() ) {
                    newPrefix = prefix + "|   "; // NOI18N
                } else {
                    newPrefix = prefix + "    "; // NOI18N
                }
            }
            fillPublicNodeTypesInheritanceTree (item.layer, newPrefix);
        }
    }


    /**
     *
     */
    private static class Item {
        /** */
        private static Map itemMap;

        /** */
        private final NamedClass clazz;
        /** */
        private final Set        layer;
        /** */
        private final String     prefix;

        /** */
        private Item (NamedClass clazz, Set layer, String prefix) {
            this.clazz  = clazz;
            this.layer  = layer;
            this.prefix = prefix;
        }

        /** */
        private Item (Item item, String prefix) {
            this (item.clazz, null, prefix);
        }

        /** */
        private Item (NamedClass clazz) {
            this (clazz, new TreeSet (new NamedClassComparator()), new String());
        }

        /** */
        private Item () {
            this (new NamedClass (null));
        }        


        /**
         */
        public String toString () {
            return prefix + clazz.toString();
        }


        /**
         */
        public boolean equals (Object obj) {
            if ( (obj instanceof Item) == false ) {
                return false;
            }

            if ( DEBUG ) {
                Util.debug ("___ Item::equals: this = " + this); // NOI18N
                Util.debug ("___     ::equals: obj  = " + obj); // NOI18N
            }

            Item peer = (Item)obj;
            return clazz.equals (peer.clazz);
        }


        /**
         */
        private static Item getItem (Class clazz) {
            if ( itemMap == null ) {
                itemMap = new HashMap();
            }
            
            Item item = (Item) itemMap.get (clazz);
            if ( item == null ) {
                itemMap.put (clazz, item = new Item (new NamedClass (clazz)));
            }
            return item;
        }

        /**
         */
        private static void insertItemIntoLayer (Set layer, Item newItem) {
            if ( DEBUG_TREE ) {
                Util.debug ("\n\nInsert newItem : " + newItem); // NOI18N
                Util.debug ("       Item : set = " + layer); // NOI18N
            }

            boolean inserted = false;

            Object[] array = layer.toArray();
            for (int i = 0; i < array.length; i++) {
                Item item = (Item) array[i];
            
                if ( DEBUG_TREE ) {
                    Util.debug ("       Item : item [" + i + "] = " + item); // NOI18N
                }

                if ( item.clazz.clazz == newItem.clazz.clazz ) { // previously inserted
                    if ( DEBUG_TREE ) {
                        Util.debug ("       Item : #1 -= [ ITEM.clazz.clazz == NEW_ITEM.clazz.clazz   => IGNORE insert ]=-"); // NOI18N
                    }
                
                    // DO NOTHING
                    inserted = true;
                } else if ( item.clazz.clazz.isAssignableFrom (newItem.clazz.clazz) ) { // II.
                    if ( DEBUG_TREE ) {
                        Util.debug ("       Item : #2 -= [ NEW_ITEM is subclass of actual ITEM   => insert to ITEM.LAYER ]=-"); // NOI18N
                    }

                    insertItemIntoLayer (item.layer, newItem);
                    inserted = true;
                } else if ( newItem.clazz.clazz.isAssignableFrom (item.clazz.clazz) ) { // I.
                    if ( DEBUG_TREE ) {
                        Util.debug ("       Item : #3 -= [ actual ITEM '" + item + "' is subclass of NEW_ITEM   => item REMOVED & insert to NEW_ITEM.LAYER ]=-"); // NOI18N
                    }
                
                    if ( newItem.clazz.clazz.isInterface() == false ) {
                        layer.remove (item);
                        insertItemIntoLayer (newItem.layer, item);
                    }
                }
            }

            if ( inserted == false ) { // III.
                if ( DEBUG_TREE ) {
                    Util.debug ("       Item : #4 -= [ item '" + newItem + "' INSERTED into " + layer + " ] =-"); // NOI18N
                }

                layer.add (newItem);
            }
        }

    }


    // debug
    public static final void main (String[] args) throws Exception {
        Vector vector = getPublicNodeTypesInheritanceTree();

//          Iterator it = vector.iterator();
//          System.out.println ("+==================================="); // NOI18N
//          while (it.hasNext()) {
//              System.out.println ("-= [ " + it.next().toString() + " ] =-"); // NOI18N
//          }
    }

}
