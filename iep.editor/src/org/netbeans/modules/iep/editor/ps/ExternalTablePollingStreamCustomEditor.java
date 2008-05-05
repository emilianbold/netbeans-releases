package org.netbeans.modules.iep.editor.ps;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.iep.editor.designer.GuiConstants;
import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodePropertyCustomizerState;
import org.netbeans.modules.iep.editor.tcg.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.iep.editor.wizard.database.ColumnInfo;
import org.netbeans.modules.iep.editor.wizard.database.DatabaseTableSelectionWizardAction;
import org.netbeans.modules.iep.editor.wizard.database.DatabaseTableWizardConstants;
import org.netbeans.modules.iep.editor.wizard.database.TableInfo;
import org.netbeans.modules.iep.editor.wizard.database.TablePollingStreamWizardHelper;
import org.netbeans.modules.iep.model.ExternalTablePollingStreamOperatorComponent;
import org.netbeans.modules.iep.model.IEPComponentFactory;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.InputOperatorComponent;
import org.netbeans.modules.iep.model.InvokeStreamOperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
import org.netbeans.modules.iep.model.lib.TcgType;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

public class ExternalTablePollingStreamCustomEditor extends DefaultCustomEditor {
    private static final Logger mLog = Logger.getLogger(ExternalTablePollingStreamCustomEditor.class.getName());
    
    private boolean mValidOperatorSelection = false;
    
    private MyInputTableTreeModel mInputTableTreeTable;
    
    private InputSchemaTreePanel mInputSchemaTreePanel;
    
    private List<String> mFromClause;
    
    private String mWhereClause;
    
    private String[] mTimeUnitDisplayName = new String[] {};
            
    private String[] mTimeUnitCodeName = new String[] {};
    
    
    /** Creates a new instance of InvokeStreamCustomEditor */
    public ExternalTablePollingStreamCustomEditor() {
        super();
    }
    
    public Component getCustomEditor() {
        if (mEnv != null) {
            return new MyCustomizer(getPropertyType(), getOperatorComponent(), mEnv);
        }
        return new MyCustomizer(getPropertyType(), getOperatorComponent(), mCustomizerState);
    }
    
    private class MyCustomizer extends DefaultCustomizer {
        protected PropertyPanel mPollingIntervalPanel;
        protected PropertyPanel mPollingIntervalTimeUnitPanel;
        protected PropertyPanel mPollingRecordSizePanel;
        protected PropertyPanel mDatabaseJndiNamePanel;
        protected PropertyPanel mIsDeleteRecordsPanel;
        
        
        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, PropertyEnv env) {
            super(propertyType, component, env);
        }
        
        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, TcgComponentNodePropertyCustomizerState customizerState) {
            super(propertyType, component, customizerState);
        }
        
//        protected void initialize() {
//        	Property pollingInterval = mComponent.getProperty(ExternalTablePollingStreamOperatorComponent.PROP_POLLING_INTERVAL);
//        	if(pollingInterval != null) {
//        		mPollingInterval = pollingInterval.getValue();
//        	}
//        	Property pollingIntervalTimeUnit = mComponent.getProperty(ExternalTablePollingStreamOperatorComponent.PROP_POLLING_INTERVAL_TIME_UNIT);
//        	if(pollingIntervalTimeUnit != null) {
//        		mPollingIntervalUnit = pollingIntervalTimeUnit.getValue();
//        	}
//        	
//        	Property pollingRecordSize = mComponent.getProperty(ExternalTablePollingStreamOperatorComponent.PROP_POLLING_RECORD_SIZE);
//        	if(pollingRecordSize != null) {
//        		mRecordSize = pollingRecordSize.getValue();
//        	}
//        	
//        	Property databaseJndiName = mComponent.getProperty(ExternalTablePollingStreamOperatorComponent.PROP_DATABASE_JNDI_NAME);
//        	if(databaseJndiName != null) {
//        		mDatabaseJNDIName = databaseJndiName.getValue();
//        	}
//        	
//        	super.initialize();
//        	
//        }
        
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
            
            // polling interval
            Property pollingInterval = mComponent.getProperty(ExternalTablePollingStreamOperatorComponent.PROP_POLLING_INTERVAL);
            String pollingIntervalLabel = NbBundle.getMessage(ExternalTablePollingStreamCustomEditor.class, "ExternalTablePollingStreamCustomEditor.POLLING_INTERVAL");
//            List attributeList = mSelectPanel.getQuantityAttributeList();
//            attributeList.add(0, "");
//            mAttributePanel = PropertyPanel.createComboBoxPanel(attributeStr, attributeProp, (String[])attributeList.toArray(new String[0]), false);
            mPollingIntervalPanel = PropertyPanel.createSingleLineTextPanelWithoutFilter(pollingIntervalLabel, pollingInterval, false);
            
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mPollingIntervalPanel.component[0], gbc);
            
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mPollingIntervalPanel.component[1], gbc);
            
            //polling interval time unit

            Property pollingIntervalTimeUnit = mComponent.getProperty(ExternalTablePollingStreamOperatorComponent.PROP_POLLING_INTERVAL_TIME_UNIT);
            String pollingIntervalTimeUnitLabel = NbBundle.getMessage(ExternalTablePollingStreamCustomEditor.class, "ExternalTablePollingStreamCustomEditor.POLLING_INTERVAL_TIME_UNIT");
//            List attributeList = mSelectPanel.getQuantityAttributeList();
//            attributeList.add(0, "");
//            mAttributePanel = PropertyPanel.createComboBoxPanel(attributeStr, attributeProp, (String[])attributeList.toArray(new String[0]), false);
            mTimeUnitDisplayName = DatabaseTableWizardConstants.getTimeUnitInfosDisplayName().toArray(mTimeUnitDisplayName);
            mTimeUnitCodeName = DatabaseTableWizardConstants.getTimeUnitInfosCodeName().toArray(mTimeUnitCodeName);
            
            mPollingIntervalTimeUnitPanel = PropertyPanel.createComboBoxPanel(pollingIntervalTimeUnitLabel, 
                                                                              pollingIntervalTimeUnit, 
                                                                              DatabaseTableWizardConstants.getTimeUnitInfosDisplayName().toArray(mTimeUnitDisplayName), 
                                                                              DatabaseTableWizardConstants.getTimeUnitInfosCodeName().toArray(mTimeUnitCodeName), 
                                                                              false);
            if(pollingIntervalTimeUnit.getValue() == null || pollingIntervalTimeUnit.getValue().equals("")) {
            	mPollingIntervalTimeUnitPanel.setStringValue(DatabaseTableWizardConstants.TIMEUNIT_SECOND.getCodeName());
            }
            
//            gbc.gridx = 3;
//            gbc.gridy = 1;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            pane.add(mPollingIntervalTimeUnitPanel.component[0], gbc);
            
            gbc.gridx = 4;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mPollingIntervalTimeUnitPanel.component[1], gbc);

            JButton selectIEPProcessButton = new JButton(NbBundle.getMessage(ExternalTablePollingStreamCustomEditor.class, "ExternalTablePollingStreamCustomEditor.SELECT_TABLES"));
            selectIEPProcessButton.addActionListener(new SelectIEPProcessOperatorActionListener());
            //selectIEPProcessButton.setAction(SystemAction.get(DatabaseTableSelectionWizardAction.class));
            gbc.gridx = 5;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(selectIEPProcessButton, gbc);
            
            //second row
            
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
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mOutputSchemaNamePanel.component[0], gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mOutputSchemaNamePanel.component[1], gbc);
            
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
            
            //polling record size
            Property pollingRecordSize = mComponent.getProperty(ExternalTablePollingStreamOperatorComponent.PROP_POLLING_RECORD_SIZE);
            String pollingRecordSizeLabel = NbBundle.getMessage(ExternalTablePollingStreamCustomEditor.class, "ExternalTablePollingStreamCustomEditor.POLLING_RECORD_SIZE");
//            List attributeList = mSelectPanel.getQuantityAttributeList();
//            attributeList.add(0, "");
//            mAttributePanel = PropertyPanel.createComboBoxPanel(attributeStr, attributeProp, (String[])attributeList.toArray(new String[0]), false);
            mPollingRecordSizePanel = PropertyPanel.createSingleLineTextPanelWithoutFilter(pollingRecordSizeLabel, pollingRecordSize, false);
            
            gbc.gridx = 2;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mPollingRecordSizePanel.component[0], gbc);
            
            gbc.gridx = 3;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mPollingRecordSizePanel.component[1], gbc);
            
            
            //is delete records after polling
            Property isDeleteRecords = mComponent.getProperty(ExternalTablePollingStreamOperatorComponent.PROP_IS_DELETE_RECORDS);
            String isDeleteRecordsLabel = NbBundle.getMessage(ExternalTablePollingStreamCustomEditor.class, "ExternalTablePollingStreamCustomEditor.IS_DELETE_RECORDS");
//            List attributeList = mSelectPanel.getQuantityAttributeList();
//            attributeList.add(0, "");
//            mAttributePanel = PropertyPanel.createComboBoxPanel(attributeStr, attributeProp, (String[])attributeList.toArray(new String[0]), false);
            mIsDeleteRecordsPanel = PropertyPanel.createCheckBoxPanel(isDeleteRecordsLabel, isDeleteRecords);
            
            gbc.gridx = 4;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mIsDeleteRecordsPanel.input[0], gbc);
            
            //row 3
            //database jndi name
            Property databaseJndiName = mComponent.getProperty(ExternalTablePollingStreamOperatorComponent.PROP_DATABASE_JNDI_NAME);
            String databaseJndiNameLabel = NbBundle.getMessage(ExternalTablePollingStreamCustomEditor.class, "ExternalTablePollingStreamCustomEditor.DATABASE_JNDI_NAME");
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
                mPollingIntervalPanel.validateContent(evt);
                mPollingIntervalTimeUnitPanel.validateContent(evt);
                mPollingRecordSizePanel.validateContent(evt);
                mDatabaseJndiNamePanel.validateContent(evt);
                
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
            mPollingIntervalPanel.store();
            mPollingIntervalTimeUnitPanel.store();
            mPollingRecordSizePanel.store();
            mDatabaseJndiNamePanel.store();
            mIsDeleteRecordsPanel.store();
            
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
        
        class SelectIEPProcessOperatorActionListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                IEPModel model = getOperatorComponent().getModel();
                
                TablePollingStreamWizardHelper helper = new TablePollingStreamWizardHelper();
                WizardDescriptor wizardDescriptor = helper.createWizardDescriptor();
                
                Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
                dialog.setVisible(true);
                dialog.toFront();
                boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
                if (!cancelled) {
                    List<TableInfo> tables = (List) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_SELECTED_TABLES);
                    List<ColumnInfo> columns = (List) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_SELECTED_COLUMNS);
                    mWhereClause = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_JOIN_CONDITION);
                    String pollingInterval = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_POLLING_INTERVAL);
                    String pollingIntervalUnit = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_POLLING_INTERVAL_TIME_UNIT);
                    String recordSize = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_POLLING_RECORD_SIZE);
                    String databaseJNDIName = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_JNDI_NAME);
                    String isDeleteRecords = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_IS_DELETE_RECORDS);
                            
                    mPollingIntervalPanel.setStringValue(pollingInterval);
                    mPollingIntervalTimeUnitPanel.setStringValue(pollingIntervalUnit);
                    mPollingRecordSizePanel.setStringValue(recordSize);
                    mDatabaseJndiNamePanel.setStringValue(databaseJNDIName);
                    mIsDeleteRecordsPanel.setStringValue(isDeleteRecords);
                    
                    if(pollingIntervalUnit != null) {
                        mPollingIntervalTimeUnitPanel.setStringValue(pollingIntervalUnit);
                    }
                    
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
                    
                    Iterator<ColumnInfo> it = columns.iterator();
                    while(it.hasNext()) {
                        ColumnInfo column = it.next();
                        IEPComponentFactory factory = model.getFactory();
                        SchemaAttribute sa = factory.createSchemaAttribute(model);
                        String attrName  = mSelectPanel.generateUniqueAttributeName(column.getColumnName());
                        sa.setAttributeName(attrName);
                        String dataType = column.getColumnDataType();
                        sa.setAttributeType(dataType);
                        
                        int precision = column.getPrecision();
                        int scale = column.getScale();
                        sa.setAttributeSize("");
                        sa.setAttributeScale("");
                        
                        if(dataType.equalsIgnoreCase("CHAR")
                           || dataType.equalsIgnoreCase("VARCHAR")
                           || dataType.equalsIgnoreCase("DECIMAL") 
                           || dataType.equalsIgnoreCase("REAL")
                           || dataType.equalsIgnoreCase("DOUBLE")
                           ) {
                            if(precision != 0) {
                                sa.setAttributeSize(""+column.getPrecision());
                            } 
                        }
                        
                        if(dataType.equalsIgnoreCase("DECIMAL") 
                           || dataType.equalsIgnoreCase("REAL")
                           || dataType.equalsIgnoreCase("DOUBLE")
                           ) {
                            if(scale != 0) {
                                sa.setAttributeScale(""+column.getScale());
                            } 
                        }
                        
                        
                        attrs.add(sa);
                        expressionList.add(column.getQualifiedName());
                    }
                    
                    mSelectPanel.clearTable();
                    mSelectPanel.setAttributes(attrs);
                    mSelectPanel.setExpressions(expressionList);
                    MyInputTableTreeModel treeModel = new MyInputTableTreeModel(new DefaultMutableTreeNode("root"), mModel, mComponent, tables);
                    mInputSchemaTreePanel.setInputSchemaTreeModel(treeModel);
                    if(mWhereClause != null) {
                    	mWherePanel.setStringValue(mWhereClause);
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
