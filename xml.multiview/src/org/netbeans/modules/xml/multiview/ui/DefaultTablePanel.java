/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.multiview.ui;

// Swing
import javax.swing.table.AbstractTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

// Netbeans
import org.openide.util.NbBundle;

/** Generic panel containing the table and NEW - EDIT - DELETE buttons.
 *
 * @author  mk115033
 * Created on October 1, 2002, 3:52 PM 
 */
public class DefaultTablePanel extends javax.swing.JPanel {
    protected JButton moveUpButton, moveDownButton, sourceButton;
    private boolean reordable;
    private AbstractTableModel model;
    
    /** Creates a new TablePanel.
    * @param model AbstractTableModel for included table
    */
    public DefaultTablePanel(AbstractTableModel model) {
        this(model, false);
    }

    /** Creates a new TablePanel.
    * @param model AbstractTableModel for included table
    * @param reordable specifies whether the order of the rows is important(in DD filter-mappings for example the order of elements is important)
    * @param isSource specifies if there is a reasonable source file/link related to the table row
    */ 
    public DefaultTablePanel(AbstractTableModel model, final boolean reordable) {
        this.model=model;
        this.reordable=reordable;
        initComponents();
        setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        buttonPanel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        jTable1.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        //table = new SortableTable(new SortableTableModel(model));
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable1.setModel(model);
        javax.swing.table.JTableHeader header = jTable1.getTableHeader();
        add(header, java.awt.BorderLayout.NORTH);
        
        jTable1.getSelectionModel().addListSelectionListener
        (
                new ListSelectionListener()
                {
                        public void valueChanged(ListSelectionEvent e)
                        {
                                // ignore extra messages
                                if (e.getValueIsAdjusting())
                                {
                                        return;
                                }

                                if (((ListSelectionModel)e.getSource()).isSelectionEmpty())
                                {
                                        editButton.setEnabled(false);
                                        removeButton.setEnabled(false);
                                        if (reordable) {
                                            moveUpButton.setEnabled(false);
                                            moveDownButton.setEnabled(false);
                                        }
                                }
                                else
                                {
                                        editButton.setEnabled(true);
                                        removeButton.setEnabled(true);
                                        if (reordable) {
                                            int row = jTable1.getSelectedRow();
                                            if (row<jTable1.getModel().getRowCount()-1) moveDownButton.setEnabled(true);
                                            else moveDownButton.setEnabled(false);
                                            if (row>0) moveUpButton.setEnabled(true);
                                            else moveUpButton.setEnabled(false);
                                        }
                                }
                        }
                }
        );
        if (reordable) {
            moveUpButton = new JButton(NbBundle.getMessage(DefaultTablePanel.class,"LBL_Move_Up"));
            moveUpButton.setMnemonic(org.openide.util.NbBundle.getMessage(DefaultTablePanel.class, "LBL_Move_Up_mnem").charAt(0));
            moveDownButton = new JButton(NbBundle.getMessage(DefaultTablePanel.class,"LBL_Move_Down"));
            moveDownButton.setMnemonic(org.openide.util.NbBundle.getMessage(DefaultTablePanel.class, "LBL_Move_Down_mnem").charAt(0));
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(false);
            buttonPanel.add(moveUpButton);
            buttonPanel.add(moveDownButton);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jTable1 = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        setBackground(new java.awt.Color(255, 255, 255));
        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));
        addButton.setMnemonic(org.openide.util.NbBundle.getMessage(DefaultTablePanel.class, "LBL_Add_mnem").charAt(0));
        addButton.setText(org.openide.util.NbBundle.getBundle(DefaultTablePanel.class).getString("LBL_Add"));
        buttonPanel.add(addButton);

        editButton.setMnemonic(org.openide.util.NbBundle.getMessage(DefaultTablePanel.class, "LBL_Edit_mnem").charAt(0));
        editButton.setText(org.openide.util.NbBundle.getBundle(DefaultTablePanel.class).getString("LBL_Edit"));
        editButton.setEnabled(false);
        buttonPanel.add(editButton);

        removeButton.setMnemonic(org.openide.util.NbBundle.getMessage(DefaultTablePanel.class, "LBL_Remove_mnem").charAt(0));
        removeButton.setText(org.openide.util.NbBundle.getBundle(DefaultTablePanel.class).getString("LBL_Remove"));
        removeButton.setEnabled(false);
        buttonPanel.add(removeButton);

        add(buttonPanel, java.awt.BorderLayout.SOUTH);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jTable1.setBorder(new javax.swing.border.MatteBorder(null));
        jPanel1.add(jTable1, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton addButton;
    private javax.swing.JPanel buttonPanel;
    protected javax.swing.JButton editButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTable jTable1;
    protected javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    public AbstractTableModel getModel() {
        return model;
    }
    
    public void setButtons(boolean b1, boolean b2, boolean b3) {
        addButton.setEnabled(b1);
        editButton.setEnabled(b2);
        removeButton.setEnabled(b3);
    }
    
    public void setButtons(boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6) {
        this.setButtons(b1,b2,b3);
        moveUpButton.setEnabled(b4);
        moveDownButton.setEnabled(b5);
    }
    
    public boolean isReordable() {
        return reordable;
    }
    
    public void setSelectedRow(int row) {
        jTable1.setRowSelectionInterval(row,row);
    }
    
    public void setTitle(String title) {
        javax.swing.JLabel label = new javax.swing.JLabel(title);
        label.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        label.setBorder(new javax.swing.border.EmptyBorder(5,5,5,0));
        add(label, java.awt.BorderLayout.NORTH);
    }
    
    public javax.swing.JTable getTable() {
        return jTable1;
    }
}
