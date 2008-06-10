package org.netbeans.modules.iep.editor.ps;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.modules.iep.editor.designer.GuiConstants;
import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodePropertyCustomizerState;
import org.netbeans.modules.iep.editor.tcg.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.iep.editor.wizard.database.ColumnInfo;
import org.netbeans.modules.iep.editor.wizard.database.DatabaseMetaDataHelper;
import org.netbeans.modules.iep.editor.wizard.database.DatabaseTableWizardConstants;
import org.netbeans.modules.iep.editor.wizard.database.TableInfo;
import org.netbeans.modules.iep.editor.wizard.database.TablePollingStreamWizardHelper;
import org.netbeans.modules.iep.editor.wizard.database.replaystream.ReplayStreamWizardHelper;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.ReplayStreamOperatorComponent;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

public class ReplayStreamCustomEditor extends DefaultCustomEditor {
    private static final Logger mLog = Logger.getLogger(ReplayStreamCustomEditor.class.getName());
    
    private boolean mValidOperatorSelection = false;
    
    private MyInputTableTreeModel mInputTableTreeTable;
    
    private InputSchemaTreePanel mInputSchemaTreePanel;
    
    private List<String> mFromClause;
    
    private String mWhereClause;
    
    private String[] mTimeUnitDisplayName = new String[] {};
            
    private String[] mTimeUnitCodeName = new String[] {};
    
    
    /** Creates a new instance of InvokeStreamCustomEditor */
    public ReplayStreamCustomEditor() {
        super();
    }
    
    public Component getCustomEditor() {
        if (mEnv != null) {
            return new MyCustomizer(getPropertyType(), getOperatorComponent(), mEnv);
        }
        return new MyCustomizer(getPropertyType(), getOperatorComponent(), mCustomizerState);
    }
    
    private class MyCustomizer extends DefaultCustomizer {
        protected JTextField mRecordIdentifyingColumnsTextField;
        protected PropertyPanel mDatabaseJndiNamePanel;
        
        private SchemaComponent mRecordIdentifyingColumnsSchema;
        
        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, PropertyEnv env) {
            super(propertyType, component, env);
        }
        
        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, TcgComponentNodePropertyCustomizerState customizerState) {
            super(propertyType, component, customizerState);
        }
        
        protected JPanel createPropertyPanel() throws Exception {
            
            JPanel pane = new JPanel();
            String msg = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.DETAILS");
            pane.setBorder(new CompoundBorder(
                    new TitledBorder(LineBorder.createGrayLineBorder(), msg, TitledBorder.LEFT, TitledBorder.TOP),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)));
            pane.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);
            
            // name
            Property nameProp = mComponent.getProperty(NAME_KEY);
            String nameStr = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.NAME");
            mNamePanel = PropertyPanel.createSingleLineTextPanel(nameStr, nameProp, false);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mNamePanel.component[0], gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mNamePanel.component[1], gbc);
            
            // output schema
            Property outputSchemaNameProp = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY);
            String outputSchemaNameStr = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.OUTPUT_SCHEMA_NAME");
            mOutputSchemaNamePanel = PropertyPanel.createSingleLineTextPanel(outputSchemaNameStr, outputSchemaNameProp, false);
            if (mIsSchemaOwner) {
                if (mOutputSchemaNamePanel.getStringValue() == null || mOutputSchemaNamePanel.getStringValue().trim().equals("")) {
                    IEPModel model = mComponent.getModel();
                        String schemaName = NameGenerator.generateSchemaName(model.getPlanComponent().getSchemaComponentContainer());
                    mOutputSchemaNamePanel.setStringValue(schemaName);
                }
            } else {
                ((JTextField)mOutputSchemaNamePanel.input[0]).setEditable(false);
            }
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mOutputSchemaNamePanel.component[0], gbc);
            
            
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mOutputSchemaNamePanel.component[1], gbc);
            
            
            
            JButton selectIEPProcessButton = new JButton(NbBundle.getMessage(ReplayStreamCustomEditor.class, "ExternalTablePollingStreamCustomEditor.SELECT_TABLES"));
            selectIEPProcessButton.addActionListener(new SelectReplayStreamTableActionListener());
            //selectIEPProcessButton.setAction(SystemAction.get(DatabaseTableSelectionWizardAction.class));
            gbc.gridx = 4;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(selectIEPProcessButton, gbc);

//            // struct
//            gbc.gridx = 2;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            pane.add(Box.createHorizontalStrut(20), gbc);
//            
            
            
            //row 3
            
            //record identifying columns
            //database jndi name
            Property databaseJndiName = mComponent.getProperty(ReplayStreamOperatorComponent.PROP_DATABASE_JNDI_NAME);
            String databaseJndiNameLabel = NbBundle.getMessage(ReplayStreamCustomEditor.class, "ExternalTablePollingStreamCustomEditor.DATABASE_JNDI_NAME");
//            List attributeList = mSelectPanel.getQuantityAttributeList();
//            attributeList.add(0, "");
//            mAttributePanel = PropertyPanel.createComboBoxPanel(attributeStr, attributeProp, (String[])attributeList.toArray(new String[0]), false);
            mDatabaseJndiNamePanel = PropertyPanel.createSingleLineTextPanelWithoutFilter(databaseJndiNameLabel, databaseJndiName, false);
            
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mDatabaseJndiNamePanel.component[0], gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mDatabaseJndiNamePanel.component[1], gbc);
            
           
            //record identifying prop
            Property recordIdentifyingColumnsSchema = mComponent.getProperty(ReplayStreamOperatorComponent.PROP_RECORD_IDENTIFIER_COLUMNS_SCHEMA);
            String recordIdentifyingColumnsLabelStr = NbBundle.getMessage(ReplayStreamCustomEditor.class, "ExternalTablePollingStreamCustomEditor.RECORD_IDENTIFYING_COLUMNS");
            
            
            
            List<String> columnList = (List<String>) new ArrayList<String>();
            
            String schemaName = recordIdentifyingColumnsSchema.getValue();
            if(schemaName != null) {
                IEPModel model = mComponent.getModel();
                SchemaComponentContainer scContainer = model.getPlanComponent().getSchemaComponentContainer();
                SchemaComponent recordIdColumnSchema  = scContainer.findSchema(schemaName);
                if(recordIdColumnSchema != null) {
                    List<SchemaAttribute> saList = recordIdColumnSchema.getSchemaAttributes();
                    Iterator<SchemaAttribute> it = saList.iterator();
                    while(it.hasNext()) {
                        SchemaAttribute sa = it.next();
                        String cName = sa.getAttributeName();
                        columnList.add(cName);
                    }
                }
            }
                    
            String displayColumnNames = convertListToCommaSeperatedValues(columnList);
//            mRecordIdentifyingColumnsPanel = PropertyPanel.createSingleLineTextPanelWithoutFilter(recordIdentifyingColumnsLabel, recordIdentifyingColumns, false);
//            mRecordIdentifyingColumnsPanel.setStringValue();
            
            JLabel recordIdentifyingColumnsLabel = new JLabel(recordIdentifyingColumnsLabelStr);
            mRecordIdentifyingColumnsTextField = new JTextField(displayColumnNames); 
            gbc.gridx = 2;
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
//            pane.add(mRecordIdentifyingColumnsPanel.component[0], gbc);
            pane.add(recordIdentifyingColumnsLabel, gbc);
            
            gbc.gridx = 3;
            gbc.gridy = 2;
            gbc.gridwidth = 3;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
//            pane.add(mRecordIdentifyingColumnsPanel.component[1], gbc);
            pane.add(mRecordIdentifyingColumnsTextField, gbc);
            
//            ((JTextField) mRecordIdentifyingColumnsPanel.component[1]).setEditable(false);
            mRecordIdentifyingColumnsTextField.setEditable(false);
                    
//            gbc.gridx = 6;
//            gbc.gridy = 2;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 1.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            pane.add(mIsDeleteRecordsPanel.component[1], gbc);
//            
            
                    
            
            // glue
//            gbc.gridx = 5;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 1.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            pane.add(Box.createHorizontalGlue(), gbc);
            
            return pane;
        }
        
        @Override
        protected SelectPanel createSelectPanel(IEPModel model, OperatorComponent component) {
            return new MySelectPanel(model, component);
        }

        @Override
        protected InputSchemaTreePanel createInputSchemaTreePanel(IEPModel model, OperatorComponent component) {
            if(mInputTableTreeTable == null) {
                mInputTableTreeTable = new MyInputTableTreeModel(new DefaultMutableTreeNode("root"), model, component);
            }
            
            mInputSchemaTreePanel = new InputSchemaTreePanel(model, mInputTableTreeTable);
            return mInputSchemaTreePanel;
        }

        
        @Override
        protected boolean isShowFromClause() {
            return false;
        }
        
        
        public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
            super.validateContent(evt);
            try {
                mDatabaseJndiNamePanel.validateContent(evt);
//                mRecordIdentifyingColumnsPanel.validateContent(evt);
                
            } catch (Exception e) {
                String msg = e.getMessage();
                mStatusLbl.setText(msg);
                mStatusLbl.setIcon(GuiConstants.ERROR_ICON);
                throw new PropertyVetoException(msg, evt);
            }
            
            //validate for atleast one input and that input should be used
            //in from clause and expressions are columns from input
        }
        
        public void setValue() {
            super.setValue();
            
//            mRecordIdentifyingColumnsPanel.store();
            mDatabaseJndiNamePanel.store();
            
            
            
            //store record identifying columns
            Property recordIdentifyingColumnsSchema = mComponent.getProperty(ReplayStreamOperatorComponent.PROP_RECORD_IDENTIFIER_COLUMNS_SCHEMA);
            String ridschemaName = recordIdentifyingColumnsSchema.getValue();
            if(mRecordIdentifyingColumnsSchema != null) {
                IEPModel model = getOperatorComponent().getModel();
                model.startTransaction();
                SchemaComponentContainer sc = model.getPlanComponent().getSchemaComponentContainer();
                //remove previous record identifer schema component if
                //any
                if(ridschemaName != null && !ridschemaName.trim().equals("")) {
                    SchemaComponent sComp = sc.findSchema(ridschemaName);
                    if(sComp != null) {
                        sc.removeSchemaComponent(sComp);
                    }
                }
                sc.addSchemaComponent(mRecordIdentifyingColumnsSchema);
                recordIdentifyingColumnsSchema.setValue(mRecordIdentifyingColumnsSchema.getName());
                model.endTransaction();
            } else {
                IEPModel model = getOperatorComponent().getModel();
                model.startTransaction();
                
                if(ridschemaName != null && !ridschemaName.trim().equals("")) {
                    SchemaComponentContainer sc = model.getPlanComponent().getSchemaComponentContainer();
                    SchemaComponent sComp = sc.findSchema(ridschemaName);
                    if(sComp != null) {
                        sc.removeSchemaComponent(sComp);
                    }
                }
                
                recordIdentifyingColumnsSchema.setValue("");
                model.endTransaction();
            }
            
//            String columns = mRecordIdentifyingColumnsPanel.getStringValue();
//            StringTokenizer st = new StringTokenizer(columns, ",");
//            List<String> cList = new ArrayList<String>();
//            
//            while(st.hasMoreElements()) {
//                String c = (String) st.nextElement();
//                cList.add(c);
//            }
//            
//            String val = recordIdentifyingColumns.getPropertyType().getType().format(cList);
//            mComponent.getModel().startTransaction();
//            recordIdentifyingColumns.setValue(val);
//            mComponent.getModel().endTransaction();
//            
            //store from clause
            
            if(mFromClause != null) {
                Property fromProp = mComponent.getProperty(FROM_CLAUSE_KEY);
                if(fromProp != null) {
                    mComponent.getModel().startTransaction();
                    fromProp.setValue(fromProp.getPropertyType().getType().format(mFromClause));
                    mComponent.getModel().endTransaction();
                }
            }
            
            //store where clause
            if(mWhereClause != null) {
                Property whereClause = mComponent.getProperty(WHERE_CLAUSE_KEY);
                if(whereClause != null) {
                    mComponent.getModel().startTransaction();
                    whereClause.setValue(mWhereClause);
                    mComponent.getModel().endTransaction();
                }
            }

        }
        
        private String convertListToCommaSeperatedValues(List<String> list) {
            StringBuffer strBuf = new StringBuffer();
            Iterator<String> it = list.iterator();
            while(it.hasNext()) {
                String value = it.next();
                strBuf.append(value);
                if(it.hasNext()) {
                    strBuf.append(",");
                }
            }
            
            return strBuf.toString();
        }
        
        
       
        
        private String generateUniqueAsColumnName(ColumnInfo column, 
                                                  Set<String> nameSet) {
            String baseName = column.getColumnName();
            
            String newName = baseName;
            
            int counter = 0;
            while(nameSet.contains(newName)) {
                newName = baseName + "_" + counter;
                counter++;
            }
            return newName;
        
        }
        
        private Set<String> getColumnNames(List<ColumnInfo> remainingColumns) {
            Set<String> nameSet = new HashSet<String>();
            
            for (int i = 0; i < remainingColumns.size(); i++) {
                ColumnInfo c = remainingColumns.get(i);
                String colName = c.getColumnName();
                nameSet.add(colName);
            }
            
            return nameSet;
        }
        
        
        class SelectReplayStreamTableActionListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                IEPModel model = getOperatorComponent().getModel();
                
                ReplayStreamWizardHelper helper = new ReplayStreamWizardHelper();
                WizardDescriptor wizardDescriptor = helper.createWizardDescriptor();
                
                Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
                dialog.setVisible(true);
                dialog.toFront();
                boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
                if (!cancelled) {
                    List<TableInfo> tables = (List) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_SELECTED_TABLES);
                    List<ColumnInfo> columns = (List) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_SELECTED_COLUMNS);
                    mWhereClause = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_JOIN_CONDITION);
                    String databaseJNDIName = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_JNDI_NAME);
                    List<ColumnInfo> recordIdentifyingColumns =  (List) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_POLLING_UNIQUE_RECORD_IDENTIFIER_COLUMNS);
                    
                    
                    
                    mDatabaseJndiNamePanel.setStringValue(databaseJNDIName);
                    
                    
                    Iterator<TableInfo> tableIt = tables.iterator();
                    List<String> fromList = new ArrayList<String>(); 
                    while(tableIt.hasNext()) {
                        TableInfo table = tableIt.next();
                        String tableQName = table.getQualifiedName();
                        fromList.add(tableQName);
                    }
                    
                    mFromClause = fromList;
                    
                    List<SchemaAttribute> attrs = new ArrayList<SchemaAttribute>();
                    List<String> expressionList = new ArrayList<String>();
                    
                    Map<String, String> fromColumnToAsColumnMap = new HashMap<String, String>();
                    
                    List<ColumnInfo> remainingColumns = new ArrayList<ColumnInfo>(columns);
                    Set<String> usedupNames = new HashSet<String>();
                    
                    int counter = 0;
                    //go through user selected columns
                    Iterator<ColumnInfo> it = columns.iterator();
                    while(it.hasNext()) {
                        ColumnInfo column = it.next();
                        remainingColumns.remove(column);
                        String asColumnName = null;
                        
                        asColumnName = generateUniqueAsColumnName(column, usedupNames);
                            
                        usedupNames.add(asColumnName);
                        SchemaAttribute sa = DatabaseMetaDataHelper.createSchemaAttributeFromColumnInfo(column, asColumnName, model);
                        fromColumnToAsColumnMap.put(column.getQualifiedName(), sa.getAttributeName());
                        attrs.add(sa);
                        expressionList.add(column.getQualifiedName());
                        counter++;
                    }
                    
                    mSelectPanel.clearTable();
                    mSelectPanel.setAttributes(attrs);
                    mSelectPanel.setExpressions(expressionList);
                    MyInputTableTreeModel treeModel = new MyInputTableTreeModel(new DefaultMutableTreeNode("root"), mModel, mComponent, tables);
                    mInputSchemaTreePanel.setInputSchemaTreeModel(treeModel);
                    if(mWhereClause != null) {
                        mWherePanel.setStringValue(mWhereClause);
                    }
                    
                    if(recordIdentifyingColumns != null && recordIdentifyingColumns.size() > 0) {
                        List<String> skipSchemaNames = new ArrayList<String>();
                        skipSchemaNames.add(mOutputSchemaNamePanel.getStringValue());
                        String schemaName = NameGenerator.generateSchemaName(model.getPlanComponent().getSchemaComponentContainer(), skipSchemaNames);
                        mRecordIdentifyingColumnsSchema = model.getFactory().createSchema(model);
                        mRecordIdentifyingColumnsSchema.setName(schemaName);
                        
                        List<String> columnList = new ArrayList<String>();
                        attrs = new ArrayList<SchemaAttribute>();
                        
                        it = recordIdentifyingColumns.iterator();
                        while(it.hasNext()) {
                            ColumnInfo column = it.next();
                            String asColumnName = fromColumnToAsColumnMap.get(column.getQualifiedName());
                            SchemaAttribute sa = DatabaseMetaDataHelper.createSchemaAttributeFromColumnInfo(column, asColumnName, model);
                            attrs.add(sa);
                            columnList.add(sa.getAttributeName());
                        }
                        
                        mRecordIdentifyingColumnsSchema.setSchemaAttributes(attrs);
                        mRecordIdentifyingColumnsTextField.setText(convertListToCommaSeperatedValues(columnList));
                        
//                        mRecordIdentifyingColumnsPanel.setStringValue(convertListToCommaSeperatedValues(columnList));
                    } else {
                        mRecordIdentifyingColumnsSchema = null;
                        mRecordIdentifyingColumnsTextField.setText("");
                    }
                    
                }
            }
        }
        
    }
    
    
    class MyInputTableTreeModel extends DefaultTreeModel {
        
        private final Logger mLog = Logger.getLogger(MyInputTableTreeModel.class.getName());
    
        private DefaultMutableTreeNode mRoot;
        
        public MyInputTableTreeModel(DefaultMutableTreeNode root, 
                                     IEPModel model, 
                                     OperatorComponent component) {
            super(root);
            this.mRoot = root;
            try {
                Property fromClause = component.getProperty(SharedConstants.FROM_CLAUSE_KEY);
                
                if(fromClause != null) {
                    List tables = (List) fromClause.getPropertyType().getType().parse(fromClause.getValue());
                    if(tables != null) {
                        Iterator it = tables.iterator();
                        while(it.hasNext()) {
                            String tableQualifedName = (String) it.next();
                            DefaultMutableTreeNode inputNode = new DefaultMutableTreeNode(tableQualifedName);
                            this.mRoot.add(inputNode);
                        }
                    }
                }
            } catch(Exception e) {
                mLog.log(Level.SEVERE, NbBundle.getMessage(InputSchemaTreeModel.class, 
                        "InputSchemaTreeModel.FAIL_TO_BUILD_TREE_MODEL_FOR", component.getTitle()), e);
            }
        }
        
        
        public MyInputTableTreeModel(DefaultMutableTreeNode root, 
                                     IEPModel model, 
                                     OperatorComponent component,
                                     Collection<TableInfo> tables) {
            super(root);
            this.mRoot = root;
            try {
                if(tables != null) {
                    Iterator<TableInfo> it = tables.iterator();
                    while(it.hasNext()) {
                        TableInfo table = it.next();
                        String tableQualifedName = table.getQualifiedName();
                        DefaultMutableTreeNode inputNode = new DefaultMutableTreeNode(tableQualifedName);
                        this.mRoot.add(inputNode);
                    }
                }
            } catch(Exception e) {
                mLog.log(Level.SEVERE, NbBundle.getMessage(InputSchemaTreeModel.class, 
                        "InputSchemaTreeModel.FAIL_TO_BUILD_TREE_MODEL_FOR", component.getTitle()), e);
            }
        }
        
        
        
    }
    
    class MySelectPanel extends SelectPanel {

        /**
         * 
         */
        private static final long serialVersionUID = -4195259789503600814L;

        public MySelectPanel(IEPModel model, OperatorComponent component) {
            super(model, component);
        }
        
        @Override
        protected boolean isAddEmptyRow() {
            return false;
        }

                @Override
                protected boolean isShowButtons() {
                    return false;
                }
        
                
        @Override
        protected DefaultMoveableRowTableModel createTableModel() {
            return new MyTableModel();
        }
    }
    
    class MyTableModel extends DefaultMoveableRowTableModel {
        
        @Override
        public boolean isCellEditable(int row, int column) {
            if(column == 0 || column == 1) {
                return true;
            }
            return false;
        }
    }
}
