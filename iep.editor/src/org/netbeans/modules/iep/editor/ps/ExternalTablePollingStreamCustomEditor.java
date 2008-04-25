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
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodePropertyCustomizerState;
import org.netbeans.modules.iep.editor.tcg.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.iep.editor.wizard.database.ColumnInfo;
import org.netbeans.modules.iep.editor.wizard.database.DatabaseTableSelectionWizardAction;
import org.netbeans.modules.iep.editor.wizard.database.DatabaseTableWizardConstants;
import org.netbeans.modules.iep.editor.wizard.database.TableInfo;
import org.netbeans.modules.iep.editor.wizard.database.TablePollingStreamWizardHelper;
import org.netbeans.modules.iep.model.IEPComponentFactory;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.InputOperatorComponent;
import org.netbeans.modules.iep.model.InvokeStreamOperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
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
    
    private String mFromClause;
    
    private String mWhereClause;
    
    private String mPollingInterval;
    
    private String mPollingIntervalUnit;
    
    private String mPollingRecordSize;
    
    private String mDatabaseJNDIName;
           
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
        protected PropertyPanel mAttributePanel;
        protected PropertyPanel mSizePanel;
        
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
            
            // struct
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(Box.createHorizontalStrut(20), gbc);
            
//            // called iep process
//            Property invokedProcessName = mComponent.getProperty(InvokeStreamOperatorComponent.PROP_EXTERNAL_IEP_PROCESS_QUALIFIED_NAME);
//            String invokedProcessNameLabel = NbBundle.getMessage(ExternalTablePollingStreamCustomEditor.class, "InvokeStreamCustomEditor.EXTERNAL_IEP_PROCESS_QUALIFIED_NAME");
////            List attributeList = mSelectPanel.getQuantityAttributeList();
////            attributeList.add(0, "");
////            mAttributePanel = PropertyPanel.createComboBoxPanel(attributeStr, attributeProp, (String[])attributeList.toArray(new String[0]), false);
//            mAttributePanel = PropertyPanel.createSingleLineTextPanelWithoutFilter(invokedProcessNameLabel, invokedProcessName, false);
//            
//            gbc.gridx = 3;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            pane.add(mAttributePanel.component[0], gbc);
//            
//            gbc.gridx = 4;
//            gbc.gridy = 0;
//            gbc.gridwidth = GridBagConstraints.RELATIVE;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 1.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            pane.add(mAttributePanel.component[1], gbc);
//
//            
//            ((JTextField)mAttributePanel.component[1]).setEnabled(false);
//            
            
            JButton selectIEPProcessButton = new JButton("...");
            selectIEPProcessButton.addActionListener(new SelectIEPProcessOperatorActionListener());
            //selectIEPProcessButton.setAction(SystemAction.get(DatabaseTableSelectionWizardAction.class));
            gbc.gridx = 5;
            gbc.gridy = 0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(selectIEPProcessButton, gbc);
            
//            // called iep operator
//            Property sizeProp = mComponent.getProperty(InvokeStreamOperatorComponent.PROP_EXTERNAL_OPERATOR_NAME);
//            String sizeStr = NbBundle.getMessage(ExternalTablePollingStreamCustomEditor.class, "InvokeStreamCustomEditor.EXTERNAL_OPERATOR_NAME");
////            mSizePanel = PropertyPanel.createFloatNumberPanel(sizeStr, sizeProp, false);
//            mSizePanel = PropertyPanel.createSingleLineTextPanel(sizeStr, sizeProp, false);
//            
//            gbc.gridx = 3;
//            gbc.gridy = 1;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            pane.add(mSizePanel.component[0], gbc);
//            
//            gbc.gridx = 4;
//            gbc.gridy = 1;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            pane.add(mSizePanel.component[1], gbc);
//            ((JTextField)mSizePanel.component[1]).setEnabled(false);
//            
            // glue
            gbc.gridx = 5;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pane.add(Box.createHorizontalGlue(), gbc);
            
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
            
            InputSchemaTreePanel panel = new InputSchemaTreePanel(model, mInputTableTreeTable);
            return panel;
        }

        
        @Override
        protected boolean isShowFromClause() {
            return false;
        }
        
        
        public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
            super.validateContent(evt);
            try {
                mAttributePanel.validateContent(evt);
                mSizePanel.validateContent(evt);
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
            mAttributePanel.store();
            mSizePanel.store();
            
            //store from clause
            
            if(mFromClause != null) {
                Property fromProp = mComponent.getProperty(FROM_CLAUSE_KEY);
                if(fromProp != null) {
                    mComponent.getModel().startTransaction();
                    fromProp.setValue(mFromClause);
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
            //store polling interval
            if(mPollingInterval != null) {
                Property pollingInterval = mComponent.getProperty("pollingInterval");
                if(pollingInterval != null) {
                    mComponent.getModel().startTransaction();
                    pollingInterval.setValue(mWhereClause);
                    mComponent.getModel().endTransaction();
                }
            }
                    
            //store polling interval time unit
            if(mPollingIntervalUnit != null) {
                Property pollingIntervalUnit = mComponent.getProperty("pollingIntervalTimeUnit");
                if(pollingIntervalUnit != null) {
                    mComponent.getModel().startTransaction();
                    pollingIntervalUnit.setValue(mPollingIntervalUnit);
                    mComponent.getModel().endTransaction();
                }
            }
            
            //store polling record size
             if(mPollingRecordSize != null) {
                Property pollingRecordSize = mComponent.getProperty("pollingRecordSize");
                if(pollingRecordSize != null) {
                    mComponent.getModel().startTransaction();
                    pollingRecordSize.setValue(mPollingRecordSize);
                    mComponent.getModel().endTransaction();
                }
            }
            
            //store database jndi name
            if(mDatabaseJNDIName != null) {
                Property databaseJNDIName = mComponent.getProperty("databaseJndiName");
                if(databaseJNDIName != null) {
                    mComponent.getModel().startTransaction();
                    databaseJNDIName.setValue(mDatabaseJNDIName);
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
                    mPollingInterval = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_POLLING_INTERVAL);
                    mPollingIntervalUnit = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_POLLING_INTERVAL_TIME_UNIT);
                    mPollingIntervalUnit = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_POLLING_RECORD_SIZE);
                    mDatabaseJNDIName = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_JNDI_NAME);
                    
                    StringBuffer fromClause = new StringBuffer();
                    Iterator<TableInfo> tableIt = tables.iterator();
                    while(tableIt.hasNext()) {
                        TableInfo table = tableIt.next();
                        String tableQName = table.getQualifiedName();
                        fromClause.append(tableQName);
                        if(tableIt.hasNext()) {
                            fromClause.append(",");
                        }
                    }
                    mFromClause = fromClause.toString();
                    
                    List<SchemaAttribute> attrs = new ArrayList<SchemaAttribute>();
                    List<String> expressionList = new ArrayList<String>();
                    
                    Iterator<ColumnInfo> it = columns.iterator();
                    while(it.hasNext()) {
                        ColumnInfo column = it.next();
                        IEPComponentFactory factory = model.getFactory();
                        SchemaAttribute sa = factory.createSchemaAttribute(model);
                        String attrName  = mSelectPanel.generateUniqueAttributeName(column.getColumnName());
                        sa.setAttributeName(attrName);
                        sa.setAttributeType(column.getColumnDataType());
                        sa.setAttributeSize(""+column.getPrecision());
                        sa.setAttributeScale(""+column.getScale());
                        
                        
                        attrs.add(sa);
                        expressionList.add(column.getQualifiedName());
                    }
                    
                    mSelectPanel.clearTable();
                    mSelectPanel.setAttributes(attrs);
                    mSelectPanel.setExpressions(expressionList);
                    
                    mInputTableTreeTable.refreshModel(tables);
                }
            }
        }
        
    }
    
    
    class MyInputTableTreeModel extends DefaultTreeModel {
        
        private final Logger mLog = Logger.getLogger(MyInputTableTreeModel.class.getName());
    
        private DefaultMutableTreeNode mRoot;
        
        public MyInputTableTreeModel(DefaultMutableTreeNode root, IEPModel model, OperatorComponent component) {
            super(root);
            this.mRoot = root;
            try {
                
            } catch(Exception e) {
                mLog.log(Level.SEVERE, NbBundle.getMessage(InputSchemaTreeModel.class, 
                        "InputSchemaTreeModel.FAIL_TO_BUILD_TREE_MODEL_FOR", component.getTitle()), e);
            }
        }
        
        public void refreshModel(Collection<TableInfo> tables) {
            if(tables != null) {
                Iterator<TableInfo> it = tables.iterator();
                while(it.hasNext()) {
                    TableInfo table = it.next();
                    String tableQualifedName = table.getQualifiedName();
                    DefaultMutableTreeNode inputNode = new DefaultMutableTreeNode(tableQualifedName);
                    this.mRoot.add(inputNode);
                }
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
    		if(column == 0) {
    			return true;
    		}
    		return false;
    	}
    }
}
