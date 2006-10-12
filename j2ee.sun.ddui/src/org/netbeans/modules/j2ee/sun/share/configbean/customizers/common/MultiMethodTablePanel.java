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
/*
 * MultiMethodTablePanel.java
 *
 * Created on February 4, 2005, 4:13 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;


/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public abstract class MultiMethodTablePanel extends MethodTablePanel {

    private MultiMethodTableModel model ;

    /** Creates new form MultiMethodTablePanel */
    public MultiMethodTablePanel(MultiMethodTableModel model){
        super(model);
        this.model = model;
        initComponents();
    }


    public MultiMethodTablePanel(){
        super();
        this.model = getMultiMethodTableModel();
        initComponents();
    }


    protected void setData(){
        ///super.setData();
        selectionComboBox.setModel(new javax.swing.DefaultComboBoxModel(model.getSelections()));
        methodTable.setModel(model);
    }


    protected abstract MultiMethodTableModel  getMultiMethodTableModel();

    protected abstract String getSelectionLabelText();
    protected abstract String getSelectionLabelAcsblName();
    protected abstract String getSelectionLabelAcsblDesc();
    protected abstract char getSelectionLabelMnemonic();
    protected abstract String getSelectionComboAcsblName();
    protected abstract String getSelectionComboToolTip();
    protected abstract String getSelectionComboAcsblDesc();
    protected abstract String getTablePaneAcsblName();
    protected abstract String getTablePaneAcsblDesc();
    protected abstract String getTablePaneToolTip();


    protected MethodTableModel  getMethodTableModel(){
        return getMultiMethodTableModel();
    }


    protected void initComponents() {
        if(model == null) return;

        setLayout(new GridBagLayout());

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                panelResized(evt);
            }
        });

        GridBagConstraints gridBagConstraints;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6,6,0,5);
        selectionPanel = getSelectionPanel();
        add(selectionPanel, gridBagConstraints);

        methodTable = new TableWithToolTips();

        JTableHeader header = methodTable.getTableHeader();
        ColumnHeaderToolTips tips = new ColumnHeaderToolTips();

        // Assign a tooltip for each of the columns
        header.addMouseMotionListener(tips);

        methodTable.setModel(model);
        methodTable.setRowSelectionAllowed(false);
        methodTable.setColumnSelectionAllowed(false);
        methodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        methodTable.setTableHeader(methodTable.getTableHeader());
        javax.swing.table.TableColumnModel columnModel = methodTable.getColumnModel();
        javax.swing.table.TableColumn column = columnModel.getColumn(2);
        javax.swing.table.TableCellEditor celleditor = 
                new javax.swing.DefaultCellEditor(new javax.swing.JTextField());
        column.setCellEditor(celleditor);
        
        //adjustColumnWidth(1, false);
        //adjustColumnWidth(methodTable, 2, "description field template", false);   //NOI18N
        ///setColumnColor();
        
        tablePane = new javax.swing.JScrollPane(methodTable);
        tablePane.setOpaque(true);
        tablePane.setToolTipText(getTablePaneToolTip());
        add(tablePane, getTableGridBagConstraints()); 
        tablePane.getAccessibleContext().setAccessibleName(getTablePaneAcsblName());
        tablePane.getAccessibleContext().setAccessibleDescription(getTablePaneAcsblDesc());
     }

    private javax.swing.JPanel selectionPanel;
    private javax.swing.JComboBox selectionComboBox;
    private javax.swing.JLabel selectionLabel;


    private void selectionItemStateChanged(java.awt.event.ItemEvent evt) {                                                        
        String selection = (String)selectionComboBox.getSelectedItem();
        this.model = getMultiMethodTableModel();
        ///setData(this.model);
        methodTable.setModel(this.model);
        model.setSelection(selection);
        //adjustColumnWidth(1, false);
        //adjustColumnWidth(methodTable, 2, "description field template", false);   //NOI18N
    }


    void setColumnColor(){
        TableCellRenderer rend = methodTable.getCellRenderer(0, 0);
        ///Component comp = rend.getTableCellRendererComponent(this, v, false, false, 0, 0);
        ///DisabledBackgroundColor  = ColorTools.brighter(DisabledBackgroundColor, -0.03);
        JComponent comp = (JComponent)rend;
        comp.setBackground(DisabledBackgroundColor);
        comp.setOpaque(true);
    }


     private JPanel getSelectionPanel(){
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new GridBagLayout());
  
        selectionLabel = new javax.swing.JLabel();
        selectionLabel.setText(getSelectionLabelText());
        selectionLabel.setLabelFor(selectionComboBox);
        selectionLabel.setDisplayedMnemonic(getSelectionLabelMnemonic());
//        selectionLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0,0,0,0);
        selectionPanel.add(selectionLabel, gridBagConstraints);
        selectionLabel.getAccessibleContext().setAccessibleName(getSelectionLabelAcsblName());
        selectionLabel.getAccessibleContext().setAccessibleDescription(getSelectionLabelAcsblDesc());

        selectionComboBox = new JComboBox();
        String[] selections = model.getSelections();
        selectionComboBox.setModel(new javax.swing.DefaultComboBoxModel(selections));
        selectionComboBox.setToolTipText(getSelectionComboToolTip());
        selectionComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                selectionItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0,6,0,0);
        selectionPanel.add(selectionComboBox, gridBagConstraints);
        selectionComboBox.getAccessibleContext().setAccessibleName(getSelectionComboAcsblName());
        selectionComboBox.getAccessibleContext().setAccessibleDescription(getSelectionComboAcsblDesc());
        return selectionPanel;
    }


     private void panelResized(java.awt.event.ComponentEvent evt) {
        int width = (int)methodTable.getPreferredSize().getWidth();
        int height = (int)this.getSize().getHeight()* 3/4  ;
        tablePane.setMinimumSize(new java.awt.Dimension(width, height));
        tablePane.setPreferredSize(new java.awt.Dimension(width, height));
        updateUI();
    }
}
