/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
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

        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        
        add(tablePane, gridBagConstraints); 
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


        selectionLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        GridBagConstraints gridBagConstraints;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 1, 3, 1);
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
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

        gridBagConstraints.ipadx = 72;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 1, 1);
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
