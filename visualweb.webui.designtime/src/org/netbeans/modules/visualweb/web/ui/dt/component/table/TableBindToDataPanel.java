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
package org.netbeans.modules.visualweb.web.ui.dt.component.table;
import com.sun.data.provider.TableDataProvider;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignEvent;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.event.DesignContextListener;
import com.sun.rave.designtime.faces.FacesDesignProject;
import com.sun.rave.web.ui.component.TableRowGroup;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import com.sun.rave.propertyeditors.binding.data.AddDataProviderDialog;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Table bind to data Customizer panel
 * @author  Winston Prakash
 */

// XXX - Lot of code duplication between TableBindToDataPanel
//       and TableCustomizerMainPanel - revisit (Winston)

public class TableBindToDataPanel extends javax.swing.JPanel implements DesignContextListener{

    private DesignBean designBean = null;

    private TableDesignState tableDesignState;
    private TableRowGroupDesignState tableRowGroupDesignState;

    private DefaultListModel selectedColumnListModel = new DefaultListModel();
    private DefaultListModel availableColumnListModel = new DefaultListModel();

    private DefaultComboBoxModel dataProviderComboBoxModel = new DefaultComboBoxModel();

    private Map dataProviderList = new HashMap();

    private TableDataProviderDesignState currentTableDataProviderDesignState;

    private TableColumnDesignState currentTableColumnDesignState;

    private DesignContext[] designContexts;

    public TableBindToDataPanel(DesignBean bean){
        designBean = bean;
        //designContexts = designBean.getDesignContext().getProject().getDesignContexts();
        designContexts =  getDesignContexts(designBean);
        initComponents();
        initialize();
        // For Shortfin we removed the Server Navigator window.
        // Add Data provider dialogs depends on it. So hide it for Shortfin - Winston
        addDataProviderButton.setVisible(false);
    }

    // For performance improvement. No need to get all the contexts in the project
    private DesignContext[] getDesignContexts(DesignBean designBean){
        DesignProject designProject = designBean.getDesignContext().getProject();
        DesignContext[] contexts;
        if (designProject instanceof FacesDesignProject) {
            contexts = ((FacesDesignProject)designProject).findDesignContexts(new String[] {
                "request",
                "session",
                "application"
            });
        } else {
            contexts = new DesignContext[0];
        }
        DesignContext[] designContexts = new DesignContext[contexts.length + 1];
        designContexts[0] = designBean.getDesignContext();
        System.arraycopy(contexts, 0, designContexts, 1, contexts.length);
        return designContexts;
    }

    /**
     * Initialize the Panel with design state data
     */
    private void initialize(){
        // Create the Table & TableRowGroup and load their states.
        // User can bring up this customizer panel from Table as well from Table Row Group
        if(designBean.getInstance() instanceof TableRowGroup){
            tableDesignState = new TableDesignState(designBean.getBeanParent(), designBean);
        }else{
            tableDesignState = new TableDesignState(designBean);
        }
        tableDesignState.loadState();
        tableRowGroupDesignState = tableDesignState.getTableRowGroupDesignState();
        tableRowGroupDesignState.loadState();
        
        // Get the design beans of type  TableDataProvider and populate the
        // TableDataProvider  ComboBox
        
        DesignBean currentModelBean = tableRowGroupDesignState.getDataProviderBean();
        for (int i = 0; i < designContexts.length; i++) {
            DesignBean[] dpBeans = designContexts[i].getBeansOfType(TableDataProvider.class);
            for (int j = 0; j < dpBeans.length; j++) {
                DesignBean tableDataProvider = dpBeans[j];
                if(tableDataProvider.getInstance()  instanceof TableDataProvider){
                    TableDataProviderDesignState tableDataProviderDesignState = new TableDataProviderDesignState(tableDataProvider);
                    if(currentModelBean == tableDataProvider){
                        currentTableDataProviderDesignState = tableDataProviderDesignState;
                        tableDataProviderDesignState.setColumnDesignStates(tableRowGroupDesignState.getColumnDesignStates());
                        tableDataProviderDesignState.setSelectedColumnNames(tableRowGroupDesignState.getSelectedColumnNames());
                    }
                    tableDataProviderDesignState.initialize();
                    dataProviderList.put(tableDataProvider, tableDataProviderDesignState);
                    dataProviderComboBoxModel.addElement(tableDataProvider);
                }
            }
        }
        cbxTableDataprovider.setRenderer(new DPComboRenderer());
        cbxTableDataprovider.setModel(dataProviderComboBoxModel);
        setTableDataProviderDesignState(currentTableDataProviderDesignState);
        cbxTableDataprovider.setSelectedItem(currentModelBean);
    }
    
    public void addNotify(){
        super.addNotify();
        // Add context listener to all available contexts. This needed to know when a
        // Data Provider bean is created (needed for the feature Add data provider)
        // The Data Provider is not added by this dialog, but instructs Server Navigator
        // to add one by invoking "Add to form" action of the data source node.
        for (int i = 0; i < designContexts.length; i++) {
            //System.out.println("Adding context Listeners - " + contexts[i].getDisplayName());
            designContexts[i].addDesignContextListener(this);
        }
    }
    
    public void removeNotify(){
        // Make sure the added listeners to contexts are removed
        // Surprisingly Design Time (insync imp) doesn't use Weak References to take care
        // of this automatically
        for (int i = 0; i < designContexts.length; i++) {
            //System.out.println("Removing context Listeners - " + contexts[i].getDisplayName());
            designContexts[i].removeDesignContextListener(this);
        }
        super.removeNotify();
    }
    
    /**
     * Initialize using selected and available columns from the TableDataProvider desin state
     */
    private void setTableDataProviderDesignState(TableDataProviderDesignState tblDataProviderDesignState){
        currentTableDataProviderDesignState = tblDataProviderDesignState;
        selectedColumnListModel = currentTableDataProviderDesignState.getSelectedColumnListModel();
        selectedColumns.setModel(selectedColumnListModel);
        if(selectedColumnListModel.size() > 0) {
            selectedColumns.setSelectedIndex(0);
        }
        
        availableColumnListModel = currentTableDataProviderDesignState.getAvailableColumnListModel();
        availableColumns.setModel(availableColumnListModel);
        if(availableColumnListModel.size() > 0) {
            availableColumns.setSelectedIndex(0);
        }
    }
    
    
    public boolean isModified() {
        return true;
    }
    
    /**
     * Apply the changes from the customizer panel to the Design time state and then save the sate.
     */
    public Result applyChanges() {
        // Clear the design state values
        tableDesignState.clearProperties();
        tableRowGroupDesignState.clearProperties();
        
        // Save the Data model and Column Information to the design state
        tableRowGroupDesignState.setSelectedColumnNames(currentTableDataProviderDesignState.getSelectedColumnNames());
        tableRowGroupDesignState.setAvailableColumnNames(currentTableDataProviderDesignState.getAvailableColumnNames());
        tableRowGroupDesignState.setColumnDesignStates(currentTableDataProviderDesignState.getColumnDesignStates());
        if(currentTableDataProviderDesignState.getDataProviderBean() != tableRowGroupDesignState.getDataProviderBean()){
            tableRowGroupDesignState.setDataProviderBean(currentTableDataProviderDesignState.getDataProviderBean(), false);
        }
        
        // Persist the design sate now
        tableDesignState.saveState();
        
        Result result = new Result(true);
        return result;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jList1 = new javax.swing.JList();
        addRemoveColumnsPanel = new javax.swing.JPanel();
        getDataPanel = new javax.swing.JPanel();
        lblTableDataProvider = new javax.swing.JLabel();
        cbxTableDataprovider = new javax.swing.JComboBox();
        addDataProviderButton = new javax.swing.JButton();
        columnSelectionpanel = new javax.swing.JPanel();
        scrollAvailable = new javax.swing.JScrollPane();
        availableColumns = new javax.swing.JList();
        scrollSelected = new javax.swing.JScrollPane();
        selectedColumns = new javax.swing.JList();
        middleButtons = new javax.swing.JPanel();
        addRemoveButtonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        addReoveAllButtonPanel = new javax.swing.JPanel();
        removeAllButton = new javax.swing.JButton();
        addAllButton = new javax.swing.JButton();
        rightButtons = new javax.swing.JPanel();
        upDownButtonPanel = new javax.swing.JPanel();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        newColumnButton = new javax.swing.JButton();
        lblAvailableColumns = new javax.swing.JLabel();
        lblSelectedColumns = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        addRemoveColumnsPanel.setLayout(new java.awt.BorderLayout());

        getDataPanel.setLayout(new java.awt.BorderLayout(5, 5));

        getDataPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lblTableDataProvider.setDisplayedMnemonic('G');
        lblTableDataProvider.setLabelFor(cbxTableDataprovider);
        lblTableDataProvider.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("TABLE_DATA_PROVIDER_TITLE"));
        lblTableDataProvider.setVerifyInputWhenFocusTarget(false);
        getDataPanel.add(lblTableDataProvider, java.awt.BorderLayout.WEST);

        cbxTableDataprovider.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxTableDataproviderItemStateChanged(evt);
            }
        });

        getDataPanel.add(cbxTableDataprovider, java.awt.BorderLayout.CENTER);
        cbxTableDataprovider.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("DP_COMBO_ACCESS_DESC"));

        addDataProviderButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ADD_DATAPROVIDER_BUTTON_MNEMONIC").charAt(0));
        addDataProviderButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ADD_DP_BUTTON_LBL"));
        addDataProviderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDataProviderButtonActionPerformed(evt);
            }
        });

        getDataPanel.add(addDataProviderButton, java.awt.BorderLayout.EAST);

        addRemoveColumnsPanel.add(getDataPanel, java.awt.BorderLayout.NORTH);

        columnSelectionpanel.setLayout(new java.awt.GridBagLayout());

        scrollAvailable.setMinimumSize(new java.awt.Dimension(150, 150));
        scrollAvailable.setPreferredSize(new java.awt.Dimension(150, 150));
        scrollAvailable.setViewportView(availableColumns);
        availableColumns.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("AVAILABLE_COL_ACCESS_DESC"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 9, 6);
        columnSelectionpanel.add(scrollAvailable, gridBagConstraints);

        scrollSelected.setMinimumSize(new java.awt.Dimension(150, 150));
        scrollSelected.setPreferredSize(new java.awt.Dimension(150, 150));
        selectedColumns.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                selectedColumnsValueChanged(evt);
            }
        });

        scrollSelected.setViewportView(selectedColumns);
        selectedColumns.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SELECTED_COL_ACCESS_DESC"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 9, 0);
        columnSelectionpanel.add(scrollSelected, gridBagConstraints);

        middleButtons.setLayout(new java.awt.GridBagLayout());

        addRemoveButtonPanel.setLayout(new java.awt.GridLayout(2, 0, 0, 5));

        addButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ADD_TO_BUTTON_LBL"));
        addButton.setMargin(new java.awt.Insets(2, 7, 2, 7));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        addRemoveButtonPanel.add(addButton);
        addButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ADD_COLUMN_ACCESS_DESC"));

        removeButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("REMOVE_FROM_BUTTON_LBL"));
        removeButton.setMargin(new java.awt.Insets(2, 7, 2, 7));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        addRemoveButtonPanel.add(removeButton);
        removeButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("REMOVE_COLUMN_ACCESS_DESC"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        middleButtons.add(addRemoveButtonPanel, gridBagConstraints);

        addReoveAllButtonPanel.setLayout(new java.awt.GridLayout(2, 0, 0, 5));

        removeAllButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("REMOVE_ALL_BUTTON_LBL"));
        removeAllButton.setMargin(new java.awt.Insets(2, 7, 2, 7));
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });

        addReoveAllButtonPanel.add(removeAllButton);
        removeAllButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("REMOVE_ALL_COLUMN_ACCESS_DESC"));

        addAllButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ADD_ALL_BUTTON_MNEMONIC").charAt(0));
        addAllButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ADD_ALL_BUTTON"));
        addAllButton.setMargin(new java.awt.Insets(2, 7, 2, 7));
        addAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAllButtonActionPerformed(evt);
            }
        });

        addReoveAllButtonPanel.add(addAllButton);
        addAllButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ADD_ALL_COLUMN_ACCESS_DESC"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 1.0;
        middleButtons.add(addReoveAllButtonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        columnSelectionpanel.add(middleButtons, gridBagConstraints);

        rightButtons.setLayout(new java.awt.GridBagLayout());

        upDownButtonPanel.setLayout(new java.awt.GridLayout(2, 0, 0, 5));

        upButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("UP_BUTTON_MNEMONIC").charAt(0));
        upButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("UP_BUTTON_LBL"));
        upButton.setMargin(new java.awt.Insets(2, 7, 2, 7));
        upButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upButtonActionPerformed(evt);
            }
        });

        upDownButtonPanel.add(upButton);
        upButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("MOVE_COLUMN_UP_ACCESS_DESC"));

        downButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("DOWN_BUTTON_MNEMONIC").charAt(0));
        downButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("DOWN_BUTON_LBL"));
        downButton.setMargin(new java.awt.Insets(2, 7, 2, 7));
        downButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downButtonActionPerformed(evt);
            }
        });

        upDownButtonPanel.add(downButton);
        downButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("MOVE_COLUMN_DOWN_ACCESS_DESC"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        rightButtons.add(upDownButtonPanel, gridBagConstraints);

        newColumnButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("NEW_BUTTON_MNEMONIC").charAt(0));
        newColumnButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("NEW_BUTTON_LABEL"));
        newColumnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newColumnButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 1.0;
        rightButtons.add(newColumnButton, gridBagConstraints);
        newColumnButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ADD_NEW_COLUMN_ACCESS_DESC"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 9, 6);
        columnSelectionpanel.add(rightButtons, gridBagConstraints);

        lblAvailableColumns.setDisplayedMnemonic('b');
        lblAvailableColumns.setLabelFor(availableColumns);
        lblAvailableColumns.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("AVAILABLE_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 6);
        columnSelectionpanel.add(lblAvailableColumns, gridBagConstraints);

        lblSelectedColumns.setDisplayedMnemonic('S');
        lblSelectedColumns.setLabelFor(selectedColumns);
        lblSelectedColumns.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SELECTED_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 6, 0);
        columnSelectionpanel.add(lblSelectedColumns, gridBagConstraints);

        addRemoveColumnsPanel.add(columnSelectionpanel, java.awt.BorderLayout.CENTER);

        add(addRemoveColumnsPanel, java.awt.BorderLayout.CENTER);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void cbxTableDataproviderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxTableDataproviderItemStateChanged
        if(evt.getStateChange() == evt.SELECTED){
            setTableDataProviderDesignState((TableDataProviderDesignState)dataProviderList.get(evt.getItem()));
        }
    }//GEN-LAST:event_cbxTableDataproviderItemStateChanged
    
    private void addDataProviderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDataProviderButtonActionPerformed
        AddDataProviderDialog addDataProviderDialog = new AddDataProviderDialog();
        addDataProviderDialog.showDialog();
    }//GEN-LAST:event_addDataProviderButtonActionPerformed
    
    private void selectedColumnsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_selectedColumnsValueChanged
        if(!evt.getValueIsAdjusting()){
        }
    }//GEN-LAST:event_selectedColumnsValueChanged
    
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        int selectedIndex = availableColumns.getSelectedIndex();
        Object[] selections = availableColumns.getSelectedValues();
        for(int i=0; i< selections.length; i++){
            if (!selectedColumnListModel.contains(selections[i])){
                selectedColumnListModel.addElement(selections[i]);
                selectedColumns.setSelectedValue(selections[i],true);
            }
            availableColumnListModel.removeElement(selections[i]);
        }
        if(availableColumnListModel.size() == 0) {
            addButton.setEnabled(false);
            addAllButton.setEnabled(false);
        }else{
            if(--selectedIndex >= 0) {
                availableColumns.setSelectedIndex(selectedIndex);
            }else{
                availableColumns.setSelectedIndex(availableColumnListModel.size()-1);
            }
        }
        removeButton.setEnabled(true);
        removeAllButton.setEnabled(true);
    }//GEN-LAST:event_addButtonActionPerformed
    
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int selectedIndex = selectedColumns.getSelectedIndex();
        Object[] selections = selectedColumns.getSelectedValues();
        for(int i=0; i< selections.length; i++){
            if (!availableColumnListModel.contains(selections[i])){
                availableColumnListModel.addElement(selections[i]);
                availableColumns.setSelectedValue(selections[i],true);
            }
            selectedColumnListModel.removeElement(selections[i]);
        }
        if(selectedColumnListModel.size() == 0) {
            removeButton.setEnabled(false);
            removeAllButton.setEnabled(false);
        }else{
            if(--selectedIndex >= 0) {
                selectedColumns.setSelectedIndex(selectedIndex);
            }else{
                selectedColumns.setSelectedIndex(selectedColumnListModel.size()-1);
            }
        }
        addButton.setEnabled(true);
        addAllButton.setEnabled(true);
    }//GEN-LAST:event_removeButtonActionPerformed
    
    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        for(int i=0; i< selectedColumnListModel.size(); i++){
            if (!availableColumnListModel.contains(selectedColumnListModel.getElementAt(i))){
                availableColumnListModel.addElement(selectedColumnListModel.getElementAt(i));
                availableColumns.setSelectedIndex(i);
            }
        }
        selectedColumnListModel.removeAllElements();
        addButton.setEnabled(true);
        addAllButton.setEnabled(true);
        removeButton.setEnabled(false);
        removeAllButton.setEnabled(false);
    }//GEN-LAST:event_removeAllButtonActionPerformed
    
    private void addAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAllButtonActionPerformed
        for(int i=0; i< availableColumnListModel.size(); i++){
            if (!selectedColumnListModel.contains(availableColumnListModel.getElementAt(i))){
                selectedColumnListModel.addElement(availableColumnListModel.getElementAt(i));
                selectedColumns.setSelectedIndex(i);
            }
        }
        availableColumnListModel.removeAllElements();
        removeButton.setEnabled(true);
        removeAllButton.setEnabled(true);
        addButton.setEnabled(false);
        addAllButton.setEnabled(false);
    }//GEN-LAST:event_addAllButtonActionPerformed
    
    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        int index = selectedColumns.getSelectedIndex();
        if(index > 0){
            Object currentObject = selectedColumnListModel.get(index);
            Object prevObject = selectedColumnListModel.get(index-1);
            selectedColumnListModel.setElementAt(currentObject, index-1);
            selectedColumnListModel.setElementAt(prevObject, index);
            selectedColumns.setSelectedIndex(index-1);
        }
    }//GEN-LAST:event_upButtonActionPerformed
    
    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        int index = selectedColumns.getSelectedIndex();
        if(index < selectedColumnListModel.getSize()){
            Object currentObject = selectedColumnListModel.get(index);
            Object prevObject = selectedColumnListModel.get(index+1);
            selectedColumnListModel.setElementAt(currentObject, index+1);
            selectedColumnListModel.setElementAt(prevObject, index);
            selectedColumns.setSelectedIndex(index+1);
        }
    }//GEN-LAST:event_downButtonActionPerformed
    
    private void newColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newColumnButtonActionPerformed
        String name = currentTableDataProviderDesignState.getUniqueColumnName( "column");
        TableColumnDesignState colDesignState = new TableColumnDesignState(name, name,  "text"); //NOI18N
        currentTableDataProviderDesignState.addColumnDesignStates(colDesignState);
        selectedColumns.setSelectedValue(name, true);
    }//GEN-LAST:event_newColumnButtonActionPerformed
    
    // Renderer for the Data Provider Combobox
    class DPComboRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if(value instanceof DesignBean){
                DesignBean dataProviderBean = (DesignBean)value;
                if(!((TableDataProviderDesignState)dataProviderList.get(dataProviderBean)).isBroken()){
                    setText( "<html><p><b>" + dataProviderBean.getInstanceName() +  "</b>  &nbsp; (<i>" +  //NOI18N
                            dataProviderBean.getDesignContext().getDisplayName() +  "</i>)</P></html>"); //NOI18N
                }else{
                    setText( "<html><P><font color=\"#FF0000\"><b>" + dataProviderBean.getInstanceName() +  "</b></font>  &nbsp; (<i>" +  //NOI18N
                            dataProviderBean.getDesignContext().getDisplayName() +  "</i>)</p></html>"); //NOI18N
                }
            }
            return this;
        }
    }
    
    // Implementation of DesignContextListener
    
    public void beanCreated(DesignBean designBean){
        // This is not enough. The instance name and cached rowset are yet set.
        // It would be nice if this event is fired after creation is fully completed
        if (designBean.getInstance() instanceof TableDataProvider){
            //System.out.println("Bean Created - " + designBean.getInstanceName());    
        }
    }
    
    public void instanceNameChanged(DesignBean designBean, String oldInstanceName){
        if (designBean.getInstance() instanceof TableDataProvider){
            //System.out.println("Instance Name changed - " + oldInstanceName + " to " + designBean.getInstanceName());
            cbxTableDataprovider.repaint();
        }
    }
    
    public void propertyChanged(DesignProperty prop, Object oldValue){
        if ((prop.getDesignBean().getInstance() instanceof TableDataProvider)){
            //System.out.println("Bean property Changed - "  + prop.getDesignBean().getInstanceName());
            //System.out.println("Property Name - "  + prop.getPropertyDescriptor().getDisplayName());
            if (prop.getPropertyDescriptor().getDisplayName().equals( "CachedRowSet")){
                if (!dataProviderList.keySet().contains(prop.getDesignBean())){
                    TableDataProviderDesignState tableDataProviderDesignState = new TableDataProviderDesignState(prop.getDesignBean());
                    currentTableDataProviderDesignState = tableDataProviderDesignState;
                    tableDataProviderDesignState.initialize();
                    dataProviderList.put(prop.getDesignBean(), tableDataProviderDesignState);
                    dataProviderComboBoxModel.addElement(prop.getDesignBean());
                    setTableDataProviderDesignState(currentTableDataProviderDesignState);
                    cbxTableDataprovider.setSelectedItem(prop.getDesignBean());
                }
            }
        }
    }
    
    public void beanChanged(DesignBean designBean){
    }
    
    public void contextActivated(DesignContext context){}
    
    public void contextDeactivated(DesignContext context){}
    
    public void contextChanged(DesignContext context){}
    
    public void beanDeleted(DesignBean designBean){}
    
    public void beanMoved(DesignBean designBean, DesignBean oldParent, Position pos){}
    
    public void beanContextActivated(DesignBean designBean){}
    
    public void beanContextDeactivated(DesignBean designBean){}
    
    public void eventChanged(DesignEvent event){}
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code Varibles ">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAllButton;
    private javax.swing.JButton addButton;
    private javax.swing.JButton addDataProviderButton;
    private javax.swing.JPanel addRemoveButtonPanel;
    private javax.swing.JPanel addRemoveColumnsPanel;
    private javax.swing.JPanel addReoveAllButtonPanel;
    private javax.swing.JList availableColumns;
    private javax.swing.JComboBox cbxTableDataprovider;
    private javax.swing.JPanel columnSelectionpanel;
    private javax.swing.JButton downButton;
    private javax.swing.JPanel getDataPanel;
    private javax.swing.JList jList1;
    private javax.swing.JLabel lblAvailableColumns;
    private javax.swing.JLabel lblSelectedColumns;
    private javax.swing.JLabel lblTableDataProvider;
    private javax.swing.JPanel middleButtons;
    private javax.swing.JButton newColumnButton;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JPanel rightButtons;
    private javax.swing.JScrollPane scrollAvailable;
    private javax.swing.JScrollPane scrollSelected;
    private javax.swing.JList selectedColumns;
    private javax.swing.JButton upButton;
    private javax.swing.JPanel upDownButtonPanel;
    // End of variables declaration//GEN-END:variables
    // </editor-fold>
}
