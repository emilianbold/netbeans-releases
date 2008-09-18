/*
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.ImageHyperlink;
import com.sun.rave.web.ui.component.TableRowGroup;
import com.sun.rave.web.ui.component.TextArea;
import com.sun.rave.web.ui.component.TextField;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.StaticText;
import com.sun.rave.web.ui.component.Hyperlink;
import com.sun.rave.web.ui.component.Checkbox;
import com.sun.rave.web.ui.component.RadioButton;
import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.DropDown;
import com.sun.rave.web.ui.component.PanelGroup;
import com.sun.rave.web.ui.component.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.visualweb.propertyeditors.binding.data.AddDataProviderDialog;

/**
 * Main Panel of the Table Customizer
 * @author  Winston Prakash
 */

public class TableCustomizerMainPanel extends javax.swing.JPanel implements DesignContextListener{
    
    private DesignBean designBean = null;
    
    private TableDesignState tableDesignState;
    private TableRowGroupDesignState tableRowGroupDesignState;
    
    private DefaultListModel selectedColumnListModel = new DefaultListModel();
    private DefaultListModel availableColumnListModel = new DefaultListModel();
    
    private DefaultComboBoxModel dataProviderComboBoxModel = new DefaultComboBoxModel();
    
    private Map dataProviderList = new HashMap();
    
    private TableDataProviderDesignState currentTableDataProviderDesignState;
    
    private TableColumnDesignState currentTableColumnDesignState;
    
    private List componentTypes = new ArrayList();
    
    private DesignContext[] designContexts;
    
    DesignBean currentModelBean;
    
    String[] hAlignValues = {"left", "center", "right", "justify"};
    String[] vAlignValues = {"top", "middle", "bottom"};
    
    public TableCustomizerMainPanel(DesignBean bean){
        designBean = bean;
        //designContexts = designBean.getDesignContext().getProject().getDesignContexts();
        designContexts = getDesignContexts(designBean);
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
        
        // Populate the Component Type Combo Box
        // XXX - Revisit and replace
        DefaultComboBoxModel componentTypeComboBoxModel = new DefaultComboBoxModel();
        componentTypes.add(StaticText.class);
        componentTypeComboBoxModel.addElement(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Static_Text"));
        componentTypes.add(Label.class);
        componentTypeComboBoxModel.addElement(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Label"));
        componentTypes.add(TextField.class);
        componentTypeComboBoxModel.addElement(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Text_Field"));
        componentTypes.add(TextArea.class);
        componentTypeComboBoxModel.addElement(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Text_Area"));
        componentTypes.add(Button.class);
        componentTypeComboBoxModel.addElement(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Button"));
        componentTypes.add(Hyperlink.class);
        componentTypeComboBoxModel.addElement(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Hyperlink"));
        componentTypes.add(ImageHyperlink.class);
        componentTypeComboBoxModel.addElement(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Image_Hyperlink"));
        componentTypes.add(DropDown.class);
        componentTypeComboBoxModel.addElement(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Drop_Down_List"));
        componentTypes.add(Checkbox.class);
        componentTypeComboBoxModel.addElement(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Checkbox"));
        componentTypes.add(RadioButton.class);
        componentTypeComboBoxModel.addElement(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Radio_Button"));
        componentTypes.add(ImageComponent.class);
        componentTypeComboBoxModel.addElement(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Image"));
        //componentTypes.add(RadioButtonGroup.class);
        //componentTypes.add(CheckboxGroup.class);
        componentTypes.add(PanelGroup.class);
        componentTypeComboBoxModel.addElement(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class, "Group_Panel"));
        componentTypes.add(Message.class);
        componentTypeComboBoxModel.addElement(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Message"));
        
        cbxComponentType.setModel(componentTypeComboBoxModel);
        
        cbxTableDataprovider.setRenderer(new DPComboRenderer());
        cbxTableDataprovider.addItem(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("dpRetrievingMessage"));
        
        // Initialize the Pagination Information
        cbEnablePagination.setSelected(tableDesignState.isPaginationEnabled());
        
        txtPageRows.setText(String.valueOf(tableRowGroupDesignState.getRows()));
        
        // Initialize the table information
        
        txtTableTitle.setText(tableDesignState.getTitle());
        txtTableSummary.setText(tableDesignState.getSummary());
        txtTableFooter.setText(tableDesignState.getFooterText());
        cbSelectAllRowsButton.setSelected(tableDesignState.isSelectMultipleButtonShown());
        cbDeselectAllRowsButton.setSelected(tableDesignState.isDeselectMultipleButtonShown());
        cbClearSortButton.setSelected(tableDesignState.isClearTableSortButtonShown());
        cbSortPanelButton.setSelected(tableDesignState.isSortPanelToggleButtonShown());
        
        // Initialize the Table Row Group information.
        txtEmptyDataMsg.setText(tableRowGroupDesignState.getEmptyDataMsg());
        currentModelBean = tableRowGroupDesignState.getDataProviderBean();
        
        cbxVertAlign.setModel(new DefaultComboBoxModel(new String[] {
            java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ALIGN_NOT_SET"),
            java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("VALIGN_TOP"),
            java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("VALIGN_MIDDLE"),
            java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("VALIGN_BOTTOM")
        }));
        cbxHorzAlign.setModel(new DefaultComboBoxModel(new String[] {
            java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ALIGN_NOT_SET"),
            java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("HALIGN_LEFT"),
            java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("HALIGN_CENTER"),
            java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("VALIGN_RIGHT"),
            java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("VALIGN_JUSTIFY")
                    
        }));
        
        // Get the design beans of type  TableDataProvider and populate the
        // TableDataProvider  ComboBox
        
        Thread dataProviderNodeThread = new Thread(new Runnable() {
            //SwingUtilities.invokeLater(new Runnable() {
            public void run(){
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
                    // Allow to Object List as Data to the table
                    
                    DesignBean[] objectListBeans = designContexts[i].getBeansOfType(List.class);
                    for (int j = 0; j < objectListBeans.length; j++) {
                        DesignBean objectList = objectListBeans[j];
                        TableDataProviderDesignState tableDataProviderDesignState = new TableDataProviderDesignState(objectList);
                        if(currentModelBean == objectList){
                            currentTableDataProviderDesignState = tableDataProviderDesignState;
                            tableDataProviderDesignState.setColumnDesignStates(tableRowGroupDesignState.getColumnDesignStates());
                            tableDataProviderDesignState.setSelectedColumnNames(tableRowGroupDesignState.getSelectedColumnNames());
                        }
                        tableDataProviderDesignState.initialize();
                        dataProviderList.put(objectList, tableDataProviderDesignState);
                        dataProviderComboBoxModel.addElement(objectList);
                    }
                    
                    // Allow to Object Array as Data to the table
                    DesignBean[] objectArrayBeans = designContexts[i].getBeansOfType(Object[].class);
                    for (int j = 0; j < objectArrayBeans.length; j++) {
                        DesignBean objectArray = objectArrayBeans[j];
                        TableDataProviderDesignState tableDataProviderDesignState = new TableDataProviderDesignState(objectArray);
                        if(currentModelBean == objectArray){
                            currentTableDataProviderDesignState = tableDataProviderDesignState;
                            tableDataProviderDesignState.setColumnDesignStates(tableRowGroupDesignState.getColumnDesignStates());
                            tableDataProviderDesignState.setSelectedColumnNames(tableRowGroupDesignState.getSelectedColumnNames());
                        }
                        tableDataProviderDesignState.initialize();
                        dataProviderList.put(objectArray, tableDataProviderDesignState);
                        dataProviderComboBoxModel.addElement(objectArray);
                    }
                }

                if (currentTableDataProviderDesignState == null) {
                    // XXX #138226 Logs illegal state, instead of NPE.
                    info(new IllegalStateException("There is no table data provider design state for design contexts, designContexts="
                            + (designContexts == null ? null : java.util.Arrays.asList(designContexts)))); // NOI18N
                } else {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run(){
                            cbxTableDataprovider.setModel(dataProviderComboBoxModel);
                            setTableDataProviderDesignState(currentTableDataProviderDesignState);
                            cbxTableDataprovider.setSelectedItem(currentModelBean);
                        }
                    });
                }
            }
        });
        dataProviderNodeThread.setPriority(Thread.MIN_PRIORITY);
        dataProviderNodeThread.start();
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
        saveCurrentTableColumnDesignStateValues();
        tableRowGroupDesignState.setSelectedColumnNames(currentTableDataProviderDesignState.getSelectedColumnNames());
        tableRowGroupDesignState.setAvailableColumnNames(currentTableDataProviderDesignState.getAvailableColumnNames());
        tableRowGroupDesignState.setColumnDesignStates(currentTableDataProviderDesignState.getColumnDesignStates());
        if(currentTableDataProviderDesignState.getDataProviderBean() != tableRowGroupDesignState.getDataProviderBean()){
            tableRowGroupDesignState.setDataProviderBean(currentTableDataProviderDesignState.getDataProviderBean(), false);
        }
        
        // Save the Pagination information to the state
        tableDesignState.setPaginationEnabled(cbEnablePagination.isSelected());
        try{
            tableRowGroupDesignState.setRows(Integer.parseInt(txtPageRows.getText().trim()));
        }catch (Exception exc){
            tableRowGroupDesignState.setRows(5);
        }
        
        // Set the table information to the Table design state
        tableDesignState.setTitle(txtTableTitle.getText());
        tableDesignState.setSummary(txtTableSummary.getText());
        tableDesignState.setFooterText(txtTableFooter.getText());
        
        tableDesignState.setSelectMultipleButtonShown(cbSelectAllRowsButton.isSelected());
        tableDesignState.setDeselectMultipleButtonShown(cbDeselectAllRowsButton.isSelected());
        tableDesignState.setClearTableSortButtonShown(cbClearSortButton.isSelected());
        tableDesignState.setSortPanelToggleButtonShown(cbSortPanelButton.isSelected());
        
        // Set the Table Row Group information to the TableRowGroup design state
        tableRowGroupDesignState.setEmptyDataMsg(txtEmptyDataMsg.getText());
        
        // Persist the design sate now
        tableDesignState.saveState();
        
        Result result = new Result(true);
        return result;
    }
    
    /**
     * Load the vales for the Columns tab from  the current TableClumn design state
     */
    private void loadCurrentTableColumnDesignStateValues(){
        if(currentTableColumnDesignState != null){
            txtHeader.setText(currentTableColumnDesignState.getHeader());
            txtFooter.setText(currentTableColumnDesignState.getFooter());
            txtValueExpression.setText(currentTableColumnDesignState.getValueExpression());
            if(componentTypes.indexOf(currentTableColumnDesignState.getChildType()) >= 0){
                cbxComponentType.setSelectedIndex(componentTypes.indexOf(currentTableColumnDesignState.getChildType()));
            }
            int hAlignIndex = 0;
            String hAlignVal = currentTableColumnDesignState.getHorizontalAlign();
            if(hAlignVal != null){
                for (int i=0; i< hAlignValues.length; i++){
                    if (hAlignValues[i].equals(hAlignVal.trim())){
                        hAlignIndex = i + 1;
                        break;
                    }
                }
            }
            cbxHorzAlign.setSelectedIndex(hAlignIndex);
            int vAlignIndex = 0;
            String vAlignVal = currentTableColumnDesignState.getVerticalAlign();
            if(vAlignVal != null){
                for (int i=0; i< vAlignValues.length; i++){
                    if (vAlignValues[i].equals(vAlignVal.trim())){
                        vAlignIndex = i + 1;
                        break;
                    }
                }
            }
            cbxVertAlign.setSelectedIndex(vAlignIndex);
            
            widthField.setText(currentTableColumnDesignState.getWidth());
            
            if(currentTableColumnDesignState.isSortAllowed()){
                cbSortable.setEnabled(true);
                cbSortable.setSelected(currentTableColumnDesignState.isSortable());
            }else{
                cbSortable.setSelected(false);
                cbSortable.setEnabled(false);
            }
        }
    }
    
    /**
     * Save the vales from the Columns tab to  the current TableClumn design state
     */
    private void saveCurrentTableColumnDesignStateValues(){
        if(currentTableColumnDesignState != null){
            currentTableColumnDesignState.clearProperties();
            currentTableColumnDesignState.setHeader(txtHeader.getText().trim());
            currentTableColumnDesignState.setFooter(txtFooter.getText().trim());
            currentTableColumnDesignState.setValueExpression(txtValueExpression.getText().trim());
            currentTableColumnDesignState.setChildType((Class)componentTypes.get(cbxComponentType.getSelectedIndex()));
            
            if (cbxHorzAlign.getSelectedIndex() > 0){
                currentTableColumnDesignState.setHorizontalAlign(hAlignValues[cbxHorzAlign.getSelectedIndex()-1]);
            }else{
                currentTableColumnDesignState.setHorizontalAlign("");
            }
            
            if (cbxVertAlign.getSelectedIndex() > 0){
                currentTableColumnDesignState.setVerticalAlign(vAlignValues[cbxVertAlign.getSelectedIndex()-1]);
            }else{
                currentTableColumnDesignState.setVerticalAlign("");
            }
            currentTableColumnDesignState.setSortable(cbSortable.isSelected());
            currentTableColumnDesignState.setWidth(widthField.getText().trim());
        }
    }
    
    private void clearTableColumnDesignStateValues(){
        txtHeader.setText( "");
        txtFooter.setText( "");
        txtValueExpression.setText( "");
        cbxComponentType.setSelectedIndex(0);
        cbxHorzAlign.setSelectedIndex(0);
        cbxVertAlign.setSelectedIndex(0);
        cbSortable.setSelected(false);
        widthField.setText( "");
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
        tableLayoutPanel = new javax.swing.JTabbedPane();
        columnsPanel = new javax.swing.JPanel();
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
        columnDetailPanel = new javax.swing.JPanel();
        lblColumnDetails = new javax.swing.JLabel();
        cbxHorzAlign = new javax.swing.JComboBox();
        lblHorzAlign = new javax.swing.JLabel();
        lblHeaderText = new javax.swing.JLabel();
        lblFooterText = new javax.swing.JLabel();
        txtHeader = new javax.swing.JTextField();
        txtFooter = new javax.swing.JTextField();
        lblComponentType = new javax.swing.JLabel();
        cbxComponentType = new javax.swing.JComboBox();
        lblValueExpression = new javax.swing.JLabel();
        txtValueExpression = new javax.swing.JTextField();
        lblVertAlign = new javax.swing.JLabel();
        cbxVertAlign = new javax.swing.JComboBox();
        cbSortable = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        widthField = new javax.swing.JTextField();
        optionsPanel = new javax.swing.JPanel();
        topPanel = new javax.swing.JPanel();
        lblTableTitle = new javax.swing.JLabel();
        lblTableSummary = new javax.swing.JLabel();
        txtTableTitle = new javax.swing.JTextField();
        txtTableSummary = new javax.swing.JTextField();
        txtTableFooter = new javax.swing.JTextField();
        lblTableFooter = new javax.swing.JLabel();
        lblEmptyDataMsg = new javax.swing.JLabel();
        txtEmptyDataMsg = new javax.swing.JTextField();
        bottomPanel = new javax.swing.JPanel();
        optionPanel2 = new javax.swing.JPanel();
        cbSelectAllRowsButton = new javax.swing.JCheckBox();
        cbDeselectAllRowsButton = new javax.swing.JCheckBox();
        pageSizePanel = new javax.swing.JPanel();
        lblPageSize = new javax.swing.JLabel();
        txtPageRows = new javax.swing.JTextField();
        cbEnablePagination = new javax.swing.JCheckBox();
        cbClearSortButton = new javax.swing.JCheckBox();
        cbSortPanelButton = new javax.swing.JCheckBox();
        fillPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        columnsPanel.setLayout(new java.awt.BorderLayout(5, 5));

        addRemoveColumnsPanel.setLayout(new java.awt.BorderLayout());

        getDataPanel.setLayout(new java.awt.BorderLayout(5, 5));

        getDataPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lblTableDataProvider.setDisplayedMnemonic('G');
        lblTableDataProvider.setLabelFor(cbxTableDataprovider);
        lblTableDataProvider.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("TABLE_DATA_PROVIDER_TITLE"));
        lblTableDataProvider.setVerifyInputWhenFocusTarget(false);
        getDataPanel.add(lblTableDataProvider, java.awt.BorderLayout.WEST);
        lblTableDataProvider.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("GET_DATA_FROM_ACCESS_DESC"));

        cbxTableDataprovider.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxTableDataproviderItemStateChanged(evt);
            }
        });

        getDataPanel.add(cbxTableDataprovider, java.awt.BorderLayout.CENTER);
        cbxTableDataprovider.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("GET_DATA_FROM_ACCESS_DESC"));

        addDataProviderButton.setMnemonic(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class, "ADD_DATAPROVIDER_BUTTON_MNEMONIC").charAt(0));
        addDataProviderButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ADD_DP_BUTTON_LBL"));
        addDataProviderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addDataProviderButtonActionPerformed(evt);
            }
        });

        getDataPanel.add(addDataProviderButton, java.awt.BorderLayout.EAST);
        addDataProviderButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ADD_DATA_PROVIDER_ACCESS_DESC"));

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

        addButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ADD_BUTTON_MNEMONIC").charAt(0));
        addButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ADD_TO_BUTTON_LBL"));
        addButton.setMargin(new java.awt.Insets(2, 7, 2, 7));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        addRemoveButtonPanel.add(addButton);
        addButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ADD_COLUMN_ACCESS_DESC"));

        removeButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("REMOVE_BUTTON_MNEMONIC").charAt(0));
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

        removeAllButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("REMOVE_ALL_BUTTON_MNEMONIC").charAt(0));
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

        lblAvailableColumns.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("AVAUILABLE_COLUMN_DISPLAYED_MNEMONIC").charAt(0));
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
        lblAvailableColumns.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("AVAILABLE_LBL"));

        lblSelectedColumns.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SELECTED_COLUMN_DISPLAYED_MNEMONIC").charAt(0));
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

        columnsPanel.add(addRemoveColumnsPanel, java.awt.BorderLayout.CENTER);

        columnDetailPanel.setLayout(new java.awt.GridBagLayout());

        columnDetailPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        lblColumnDetails.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("COLUMN_DETAILS_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        columnDetailPanel.add(lblColumnDetails, gridBagConstraints);

        cbxHorzAlign.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<not set>", "Left", "Center", "Right" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        columnDetailPanel.add(cbxHorzAlign, gridBagConstraints);
        cbxHorzAlign.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("HORIZ_ALIGN_ACCESS_DESC"));

        lblHorzAlign.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("HORIZONTAL_ALIGN_DISPLAYED_MNEMONIC").charAt(0));
        lblHorzAlign.setLabelFor(cbxHorzAlign);
        lblHorzAlign.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("HORIZONTAL_ALIGN_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
        columnDetailPanel.add(lblHorzAlign, gridBagConstraints);

        lblHeaderText.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("HEADER_TEXT_MNEMONIC").charAt(0));
        lblHeaderText.setLabelFor(txtHeader);
        lblHeaderText.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("HEADER_TEXT_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
        columnDetailPanel.add(lblHeaderText, gridBagConstraints);

        lblFooterText.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("COLUMN_FOOTER_DISPLAYED_MNEMONIC").charAt(0));
        lblFooterText.setLabelFor(txtFooter);
        lblFooterText.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("FOOTER_TEXT_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
        columnDetailPanel.add(lblFooterText, gridBagConstraints);

        txtHeader.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtHeaderFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        columnDetailPanel.add(txtHeader, gridBagConstraints);
        txtHeader.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("HEADER_TEXT_ACCESS_DESC"));

        txtFooter.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtFooterFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        columnDetailPanel.add(txtFooter, gridBagConstraints);
        txtFooter.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("FOOTER_COLUMN_ACCESS_DESC"));

        lblComponentType.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("COMPONENT_TYPE_DISPLAYED_MNEMONIC").charAt(0));
        lblComponentType.setLabelFor(cbxComponentType);
        lblComponentType.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("COMPONENT_TYPE_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
        columnDetailPanel.add(lblComponentType, gridBagConstraints);
        lblComponentType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class, "SELECT_COLUMN_TYPE_ACCESS_DESC"));

        cbxComponentType.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbxComponentTypeItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        columnDetailPanel.add(cbxComponentType, gridBagConstraints);
        cbxComponentType.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SELECT_COLUMN_TYPE_ACCESS_DESC"));

        lblValueExpression.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("VALUE_TYPE_TYPE_DISPLAYED_MNEMONIC").charAt(0));
        lblValueExpression.setLabelFor(txtValueExpression);
        lblValueExpression.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("VALUE_EXPRESSION_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
        columnDetailPanel.add(lblValueExpression, gridBagConstraints);

        txtValueExpression.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtValueExpressionFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        columnDetailPanel.add(txtValueExpression, gridBagConstraints);
        txtValueExpression.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("COLUMN_EXPRESSION_ACCESS_DESC"));

        lblVertAlign.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("VERTICAL_ALIGN_DISPLAYED_MNEMONIC").charAt(0));
        lblVertAlign.setLabelFor(cbxVertAlign);
        lblVertAlign.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("VERTICAL_ALIGN_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 9);
        columnDetailPanel.add(lblVertAlign, gridBagConstraints);

        cbxVertAlign.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<not set>", "Top", "Middle", "Bottom" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        columnDetailPanel.add(cbxVertAlign, gridBagConstraints);
        cbxVertAlign.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("VERT_ALIGN_ACCESS_DESC"));

        cbSortable.setMnemonic(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class, "SORT_CHECKBOX_MNEMONIC").charAt(0));
        cbSortable.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SORTABLE_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 0, 0);
        columnDetailPanel.add(cbSortable, gridBagConstraints);
        cbSortable.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("MAKE_COLUMN_SORTABLE_ACCESS_DESC"));

        jLabel1.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class, "WIDTH_MNEMONIC").charAt(0));
        jLabel1.setLabelFor(widthField);
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("WIDTH_LABEL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
        columnDetailPanel.add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class, "WIDTH_ACCESS_DESC"));

        widthField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                widthFieldActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        columnDetailPanel.add(widthField, gridBagConstraints);
        widthField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("WIDTH_ACCESS_DESC"));

        columnsPanel.add(columnDetailPanel, java.awt.BorderLayout.SOUTH);

        tableLayoutPanel.addTab(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("COLUMNS_TAB_TITLE"), columnsPanel);

        optionsPanel.setLayout(new java.awt.BorderLayout(5, 5));

        topPanel.setLayout(new java.awt.GridBagLayout());

        lblTableTitle.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("TABLE_TITLE_DISPLAYED_MNEMONIC").charAt(0));
        lblTableTitle.setLabelFor(txtTableTitle);
        lblTableTitle.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("TITLE_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 10);
        topPanel.add(lblTableTitle, gridBagConstraints);

        lblTableSummary.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("TABLE_SUMMARY_DISPLAYED_MNEMONIC").charAt(0));
        lblTableSummary.setLabelFor(txtTableSummary);
        lblTableSummary.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SUMMARY_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 10);
        topPanel.add(lblTableSummary, gridBagConstraints);
        lblTableSummary.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class, "EMPTY_MESSAGE_ACCESS_DESC"));

        txtTableTitle.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtTableTitleFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        topPanel.add(txtTableTitle, gridBagConstraints);
        txtTableTitle.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("TITLE_ACCESS_DESC"));

        txtTableSummary.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtTableSummaryFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 10);
        topPanel.add(txtTableSummary, gridBagConstraints);
        txtTableSummary.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SUMMARY_ACCESS_DESC"));

        txtTableFooter.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtTableFooterFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 10);
        topPanel.add(txtTableFooter, gridBagConstraints);
        txtTableFooter.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("FOOTER_ACCESS_DESC"));

        lblTableFooter.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("TABLE_FOOTER_DISPLAYED_MNEMONIC").charAt(0));
        lblTableFooter.setLabelFor(txtTableSummary);
        lblTableFooter.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("FOOTER_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 10);
        topPanel.add(lblTableFooter, gridBagConstraints);

        lblEmptyDataMsg.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class, "EMPTY_MESSAGE_DISPLAYED_MNEMONIC").charAt(0));
        lblEmptyDataMsg.setLabelFor(txtEmptyDataMsg);
        lblEmptyDataMsg.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("EMPTY_DATA_MSG_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 10, 0, 10);
        topPanel.add(lblEmptyDataMsg, gridBagConstraints);

        txtEmptyDataMsg.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtEmptyDataMsgFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 10);
        topPanel.add(txtEmptyDataMsg, gridBagConstraints);
        txtEmptyDataMsg.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("EMPTY_MESSAGE_ACCESS_DESC"));

        optionsPanel.add(topPanel, java.awt.BorderLayout.NORTH);

        bottomPanel.setLayout(new java.awt.BorderLayout());

        optionPanel2.setLayout(new java.awt.GridBagLayout());

        cbSelectAllRowsButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SHOW_SELECT_ALL_ROWS_MNEMONIC").charAt(0));
        cbSelectAllRowsButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SELECT_ALL_ROWS_BUTTON_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        optionPanel2.add(cbSelectAllRowsButton, gridBagConstraints);
        cbSelectAllRowsButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SHOW_SELECT_ALL_ACCESS_DESC"));

        cbDeselectAllRowsButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SHOW_DESELECT_ALL_ROWS_MNEMONIC").charAt(0));
        cbDeselectAllRowsButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("DESELECT_ALL_ROWS_BUTTON_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 0, 0);
        optionPanel2.add(cbDeselectAllRowsButton, gridBagConstraints);
        cbDeselectAllRowsButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SHOW_DESELECT_ALL_ACCESS_DESC"));

        pageSizePanel.setLayout(new java.awt.BorderLayout(5, 5));

        lblPageSize.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class, "PAGINATION_MNEMONIC").charAt(0));
        lblPageSize.setLabelFor(txtPageRows);
        lblPageSize.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("PAGE_SIZE_LBL"));
        lblPageSize.setEnabled(false);
        pageSizePanel.add(lblPageSize, java.awt.BorderLayout.WEST);
        lblPageSize.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class, "PAGE_ROWS_ACCESS_DESC"));

        txtPageRows.setEnabled(false);
        txtPageRows.setPreferredSize(new java.awt.Dimension(70, 20));
        pageSizePanel.add(txtPageRows, java.awt.BorderLayout.CENTER);
        txtPageRows.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("PAGE_ROWS_ACCESS_DESC"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 35, 0, 0);
        optionPanel2.add(pageSizePanel, gridBagConstraints);

        cbEnablePagination.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ENABLE_PAGINATION_MNEMONIC").charAt(0));
        cbEnablePagination.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ENABLE_PAGINATION_LBL"));
        cbEnablePagination.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cbEnablePaginationStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 0, 0);
        optionPanel2.add(cbEnablePagination, gridBagConstraints);
        cbEnablePagination.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("ENABLE_PAGINATION_ACCESS_DESC"));

        cbClearSortButton.setMnemonic(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class, "CLEAR_SORT_CHECKBOX_MNEMONIC").charAt(0));
        cbClearSortButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SHOW_CLEAR_SORT_BUTTON_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 0, 0);
        optionPanel2.add(cbClearSortButton, gridBagConstraints);
        cbClearSortButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SHOW_CLEAR_SORT_ACCESS_DESC"));

        cbSortPanelButton.setMnemonic(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class, "SHOW_SORT_CHECKBOX_MNEMONIC").charAt(0));
        cbSortPanelButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SHOW_SORT_PANEL_TOGGLE_BUTTON"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 0, 0);
        optionPanel2.add(cbSortPanelButton, gridBagConstraints);
        cbSortPanelButton.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("SHOW_TOGGLE_SORT_ACCESS_DESC"));

        bottomPanel.add(optionPanel2, java.awt.BorderLayout.NORTH);

        bottomPanel.add(fillPanel, java.awt.BorderLayout.CENTER);

        optionsPanel.add(bottomPanel, java.awt.BorderLayout.CENTER);

        tableLayoutPanel.addTab(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/table/Bundle").getString("OPTIONS_TAB_TITLE"), optionsPanel);

        add(tableLayoutPanel, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents
    
    
    private void txtEmptyDataMsgFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtEmptyDataMsgFocusLost
        tableRowGroupDesignState.setEmptyDataMsg(txtEmptyDataMsg.getText());
    }//GEN-LAST:event_txtEmptyDataMsgFocusLost
    
    private void txtTableFooterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTableFooterFocusLost
        tableDesignState.setFooterText(txtTableFooter.getText());
    }//GEN-LAST:event_txtTableFooterFocusLost
    
    private void txtTableSummaryFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTableSummaryFocusLost
        tableDesignState.setSummary(txtTableSummary.getText());
    }//GEN-LAST:event_txtTableSummaryFocusLost
    
    private void txtTableTitleFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtTableTitleFocusGained
        tableDesignState.setTitle(txtTableTitle.getText());
    }//GEN-LAST:event_txtTableTitleFocusGained
    
    private void widthFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_widthFieldActionPerformed
        currentTableColumnDesignState.setWidth(widthField.getText().trim());
    }//GEN-LAST:event_widthFieldActionPerformed
    
    private void txtValueExpressionFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtValueExpressionFocusLost
        currentTableColumnDesignState.setValueExpression(txtValueExpression.getText().trim());
    }//GEN-LAST:event_txtValueExpressionFocusLost
    
    private void txtHeaderFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtHeaderFocusLost
        currentTableColumnDesignState.setHeader(txtHeader.getText().trim());
    }//GEN-LAST:event_txtHeaderFocusLost
    
    private void txtFooterFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtFooterFocusLost
        currentTableColumnDesignState.setFooter(txtFooter.getText().trim());
    }//GEN-LAST:event_txtFooterFocusLost
    
    private void addDataProviderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addDataProviderButtonActionPerformed
        AddDataProviderDialog addDataProviderDialog = new AddDataProviderDialog();
        addDataProviderDialog.showDialog();
    }//GEN-LAST:event_addDataProviderButtonActionPerformed
    
    private void cbxComponentTypeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxComponentTypeItemStateChanged
        if (evt.getStateChange() != evt.DESELECTED){
            // In some strange situation this callback is called before currentTableColumnDesignState
            // is initialized. So lets be safe
            if (currentTableColumnDesignState != null){
                boolean allowed = true;
                Class compType = (Class)componentTypes.get(cbxComponentType.getSelectedIndex());
                if((compType == RadioButton.class) && (currentTableColumnDesignState.getColumnType() != String.class)){
                    allowed = false;
                }else if((compType == ImageComponent.class) && (!((currentTableColumnDesignState.getColumnType() == String.class)
                        || currentTableColumnDesignState.getColumnType() == java.net.URL.class))){
                    allowed = false;
                }
                if(allowed){
                    currentTableColumnDesignState.setChildType(compType);
                    txtValueExpression.setText(currentTableColumnDesignState.getValueExpression());
                }else{
                    String message = org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Incorrect_component_type_msg") + currentTableColumnDesignState.getColumnType().getName();
                    String title = org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "Incorrect_component_type_title");
                    JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
                    cbxComponentType.setSelectedIndex(0);
                    
                }
                System.out.println( "Child Type = " + compType.toString());
                System.out.println( "Column Type = " + currentTableColumnDesignState.getColumnType().toString());
            }
        }
    }//GEN-LAST:event_cbxComponentTypeItemStateChanged
    
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
    
    private void newColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newColumnButtonActionPerformed
        String name = currentTableDataProviderDesignState.getUniqueColumnName(org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "column"));
        TableColumnDesignState colDesignState = new TableColumnDesignState(name, name, org.openide.util.NbBundle.getMessage(TableCustomizerMainPanel.class,  "text")); //NOI18N
        currentTableDataProviderDesignState.addColumnDesignStates(colDesignState);
        selectedColumns.setSelectedValue(name, true);
    }//GEN-LAST:event_newColumnButtonActionPerformed
    
    private void selectedColumnsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_selectedColumnsValueChanged
        if(!evt.getValueIsAdjusting()){
            saveCurrentTableColumnDesignStateValues();
            currentTableColumnDesignState = currentTableDataProviderDesignState.getTableColumnDesignState((String)selectedColumns.getSelectedValue());
            if(currentTableColumnDesignState != null){
                loadCurrentTableColumnDesignStateValues();
            }else{
                clearTableColumnDesignStateValues();
            }
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
    
    private void cbxTableDataproviderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbxTableDataproviderItemStateChanged
        if(evt.getStateChange() == evt.SELECTED && (evt.getItem() instanceof DesignBean)){
            setTableDataProviderDesignState((TableDataProviderDesignState)dataProviderList.get(evt.getItem()));
        }
    }//GEN-LAST:event_cbxTableDataproviderItemStateChanged
    
    private void cbEnablePaginationStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cbEnablePaginationStateChanged
        txtPageRows.setEnabled(cbEnablePagination.isSelected());
        lblPageSize.setEnabled(cbEnablePagination.isSelected());
    }//GEN-LAST:event_cbEnablePaginationStateChanged
    
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
            }else if(value instanceof String){
                setText((String) value);
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
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JCheckBox cbClearSortButton;
    private javax.swing.JCheckBox cbDeselectAllRowsButton;
    private javax.swing.JCheckBox cbEnablePagination;
    private javax.swing.JCheckBox cbSelectAllRowsButton;
    private javax.swing.JCheckBox cbSortPanelButton;
    private javax.swing.JCheckBox cbSortable;
    private javax.swing.JComboBox cbxComponentType;
    private javax.swing.JComboBox cbxHorzAlign;
    private javax.swing.JComboBox cbxTableDataprovider;
    private javax.swing.JComboBox cbxVertAlign;
    private javax.swing.JPanel columnDetailPanel;
    private javax.swing.JPanel columnSelectionpanel;
    private javax.swing.JPanel columnsPanel;
    private javax.swing.JButton downButton;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JPanel getDataPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JLabel lblAvailableColumns;
    private javax.swing.JLabel lblColumnDetails;
    private javax.swing.JLabel lblComponentType;
    private javax.swing.JLabel lblEmptyDataMsg;
    private javax.swing.JLabel lblFooterText;
    private javax.swing.JLabel lblHeaderText;
    private javax.swing.JLabel lblHorzAlign;
    private javax.swing.JLabel lblPageSize;
    private javax.swing.JLabel lblSelectedColumns;
    private javax.swing.JLabel lblTableDataProvider;
    private javax.swing.JLabel lblTableFooter;
    private javax.swing.JLabel lblTableSummary;
    private javax.swing.JLabel lblTableTitle;
    private javax.swing.JLabel lblValueExpression;
    private javax.swing.JLabel lblVertAlign;
    private javax.swing.JPanel middleButtons;
    private javax.swing.JButton newColumnButton;
    private javax.swing.JPanel optionPanel2;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JPanel pageSizePanel;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JPanel rightButtons;
    private javax.swing.JScrollPane scrollAvailable;
    private javax.swing.JScrollPane scrollSelected;
    private javax.swing.JList selectedColumns;
    private javax.swing.JTabbedPane tableLayoutPanel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JTextField txtEmptyDataMsg;
    private javax.swing.JTextField txtFooter;
    private javax.swing.JTextField txtHeader;
    private javax.swing.JTextField txtPageRows;
    private javax.swing.JTextField txtTableFooter;
    private javax.swing.JTextField txtTableSummary;
    private javax.swing.JTextField txtTableTitle;
    private javax.swing.JTextField txtValueExpression;
    private javax.swing.JButton upButton;
    private javax.swing.JPanel upDownButtonPanel;
    private javax.swing.JTextField widthField;
    // End of variables declaration//GEN-END:variables
    // </editor-fold>


    private static void info(Exception ex) {
        Logger.getLogger(TableCustomizerMainPanel.class.getName()).log(Level.INFO, null, ex);
    }
}
