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
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodePropertyCustomizerState;
import org.netbeans.modules.tbls.editor.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.tbls.editor.table.ExpressionDefaultMoveableRowTableModel;
import org.netbeans.modules.iep.editor.wizard.database.ColumnInfo;
import org.netbeans.modules.iep.editor.wizard.database.DatabaseMetaDataHelper;
import org.netbeans.modules.iep.editor.wizard.database.DatabaseTableWizardConstants;
import org.netbeans.modules.iep.editor.wizard.database.TableInfo;
import org.netbeans.modules.iep.editor.wizard.database.replaystream.ReplayStreamWizardHelper;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.netbeans.modules.tbls.model.TcgPropertyType;
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
        protected PropertyPanel mIsPreserveLastFetchedRecordPanel;
        protected PropertyPanel mLastFetchedRecordTablePanel;

        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, PropertyEnv env) {
            super(propertyType, component, env);
        }

        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, TcgComponentNodePropertyCustomizerState customizerState) {
            super(propertyType, component, customizerState);
        }

        @Override
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
            Property nameProp = mComponent.getProperty(PROP_NAME);
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
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pane.add(mNamePanel.component[1], gbc);

            //database jndi name
            Property databaseJndiName = mComponent.getProperty(PROP_DATABASE_JNDI_NAME);
            String databaseJndiNameLabel = NbBundle.getMessage(ReplayStreamCustomEditor.class, "ExternalTablePollingStreamCustomEditor.DATABASE_JNDI_NAME");
            mDatabaseJndiNamePanel = PropertyPanel.createSingleLineTextPanelWithoutFilter(databaseJndiNameLabel, databaseJndiName, false);


            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mDatabaseJndiNamePanel.component[0], gbc);


            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pane.add(mDatabaseJndiNamePanel.component[1], gbc);

            //row 3
            // output schema
            Property outputSchemaNameProp = mComponent.getProperty(PROP_OUTPUT_SCHEMA_ID);
            String outputSchemaNameStr = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.OUTPUT_SCHEMA_NAME");
            mOutputSchemaNamePanel = PropertyPanel.createSingleLineTextPanel(outputSchemaNameStr, outputSchemaNameProp, false);
            if (mIsSchemaOwner) {
                if (mOutputSchemaNamePanel.getStringValue() == null || mOutputSchemaNamePanel.getStringValue().trim().equals("")) {
                    IEPModel model = mComponent.getModel();
                    String schemaName = NameGenerator.generateSchemaName(model.getPlanComponent().getSchemaComponentContainer());
                    mOutputSchemaNamePanel.setStringValue(schemaName);
                }
            } else {
                ((JTextField) mOutputSchemaNamePanel.input[0]).setEditable(false);
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
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pane.add(mOutputSchemaNamePanel.component[1], gbc);

            //record identifying prop
            Property recordIdentifyingColumnsSchema = mComponent.getProperty(PROP_RECORD_IDENTIFIER_COLUMNS_SCHEMA);
            String recordIdentifyingColumnsLabelStr = NbBundle.getMessage(ReplayStreamCustomEditor.class, "ReplayStreamCustomEditor.RECORD_IDENTIFYING_COLUMNS");
            List<String> columnList = (List<String>) new ArrayList<String>();
            String schemaName = recordIdentifyingColumnsSchema.getValue();
            if (schemaName != null) {
                IEPModel model = mComponent.getModel();
                SchemaComponentContainer scContainer = model.getPlanComponent().getSchemaComponentContainer();
                SchemaComponent recordIdColumnSchema = scContainer.findSchema(schemaName);
                if (recordIdColumnSchema != null) {
                    List<SchemaAttribute> saList = recordIdColumnSchema.getSchemaAttributes();
                    Iterator<SchemaAttribute> it = saList.iterator();
                    while (it.hasNext()) {
                        SchemaAttribute sa = it.next();
                        String cName = sa.getAttributeName();
                        columnList.add(cName);
                    }
                }
            }

            String displayColumnNames = GUIUtil.convertListToCommaSeperatedValues(columnList);

            JLabel recordIdentifyingColumnsLabel = new JLabel();
            org.openide.awt.Mnemonics.setLocalizedText(recordIdentifyingColumnsLabel, recordIdentifyingColumnsLabelStr);
            mRecordIdentifyingColumnsTextField = new JTextField(displayColumnNames);
            recordIdentifyingColumnsLabel.setLabelFor(mRecordIdentifyingColumnsTextField);

            // accessibility
            String acds_recordIdentifyingColumns = NbBundle.getMessage(ExternalTablePollingStreamCustomEditor.class, "ACDS_ReplayStreamCustomEditor.RECORD_IDENTIFIER_COLUMNS");
            mRecordIdentifyingColumnsTextField.getAccessibleContext().setAccessibleDescription(acds_recordIdentifyingColumns);


            gbc.gridx = 2;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(recordIdentifyingColumnsLabel, gbc);

            gbc.gridx = 3;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pane.add(mRecordIdentifyingColumnsTextField, gbc);

            mRecordIdentifyingColumnsTextField.setEditable(false);

            // last fetched records table name
            Property globalIdProp = mComponent.getProperty(PROP_LAST_FETCHED_RECORD_TABLE);
            String globalIdStr = NbBundle.getMessage(TableInputCustomEditor.class, "ReplayStreamCustomEditor.LAST_FETCHED_RECORDS_TABLE");
            mLastFetchedRecordTablePanel = PropertyPanel.createSingleLineTextPanel(globalIdStr, globalIdProp, false);
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mLastFetchedRecordTablePanel.component[0], gbc);

            gbc.gridx = 1;
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pane.add(mLastFetchedRecordTablePanel.component[1], gbc);

            //  isPreserveLastFetchedRecord
            Property isPreserveLastFetchedRecord = mComponent.getProperty(PROP_IS_PRESERVE_LAST_FETCHED_RECORD);
            String isPreserveLastFetchedRecordLabel = NbBundle.getMessage(ReplayStreamCustomEditor.class, "ReplayStreamCustomEditor.IS_PRESERVE_LAST_FETCHED_RECORD");
            mIsPreserveLastFetchedRecordPanel = PropertyPanel.createCheckBoxPanel(isPreserveLastFetchedRecordLabel, isPreserveLastFetchedRecord);

            String isPreserveLastFetchedRecordTooltip = NbBundle.getMessage(ReplayStreamCustomEditor.class, "ReplayStreamCustomEditor.IS_PRESERVE_LAST_FETCHED_RECORD_TOOLTIP");
            mIsPreserveLastFetchedRecordPanel.input[0].setToolTipText(isPreserveLastFetchedRecordTooltip);

            gbc.gridx = 2;
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mIsPreserveLastFetchedRecordPanel.input[0], gbc);

            JButton selectIEPProcessButton = new JButton();
            String btnLabel = NbBundle.getMessage(ReplayStreamCustomEditor.class, "ReplayStreamCustomEditor.SELECT_TABLES");
            org.openide.awt.Mnemonics.setLocalizedText(selectIEPProcessButton, btnLabel);
            String acds_selectTable = NbBundle.getMessage(ReplayStreamCustomEditor.class, "ACDS_ReplayStreamCustomEditor.selectTable");
            selectIEPProcessButton.getAccessibleContext().setAccessibleDescription(acds_selectTable);

            selectIEPProcessButton.addActionListener(new SelectReplayStreamTableActionListener());
            gbc.gridx = 3;
            gbc.gridy = 2;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(selectIEPProcessButton, gbc);

            return pane;
        }

        @Override
        protected SelectPanel createSelectPanel(OperatorComponent component) {
            return new MySelectPanel(component);
        }

        @Override
        protected InputSchemaTreePanel createInputSchemaTreePanel(IEPModel model, OperatorComponent component) {
            if (mInputTableTreeTable == null) {
                mInputTableTreeTable = new MyInputTableTreeModel(new DefaultMutableTreeNode("root"), model, component);
            }

            mInputSchemaTreePanel = new InputSchemaTreePanel(model, mInputTableTreeTable);
            return mInputSchemaTreePanel;
        }

        @Override
        protected boolean isShowFromClause() {
            return false;
        }

        @Override
        public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
            super.validateContent(evt);
            try {
                mDatabaseJndiNamePanel.validateContent(evt);

                // if there is no record identifer column specified
                //then it is an error 
                String recordIds = mRecordIdentifyingColumnsTextField.getText();
                if ((recordIds == null || recordIds.trim().equals(""))) {
                    String mustSpecifyRecordIdentiferColumn = NbBundle.getMessage(ExternalTablePollingStreamCustomEditor.class, "ReplayStreamCustomEditor.must_select_specify_record_identifier_columns");
                    throw new Exception(mustSpecifyRecordIdentiferColumn);
                }

                //if (isPreserveLastFetchedRecord) then lastFetchedRecordsTable != ""
                boolean isPreserveLastFetchedRecord = mIsPreserveLastFetchedRecordPanel.getBooleanValue();
                String globalId = mLastFetchedRecordTablePanel.getStringValue();
                if (isPreserveLastFetchedRecord && (globalId == null || globalId.trim().equals(""))) {
                    String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                            "ReplayStreamCustomEditor.LAST_FETCHED_RECORDS_TABLE_MUST_BE_DEFINED_IF_IS_PRESERVE_LAST_FETCHED_RECORD_SELECTED");
                    throw new PropertyVetoException(msg, evt);
                }

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
            mIsPreserveLastFetchedRecordPanel.store();
            mLastFetchedRecordTablePanel.store();

            IEPModel model = getOperatorComponent().getModel();

            //Store record identifying columns
            String recordIdColumns = mRecordIdentifyingColumnsTextField.getText();
            SchemaComponent recordIdSchema = GUIUtil.createRecordIdentifierSchema(recordIdColumns, mComponent, mOutputSchemaNamePanel.getStringValue(), mSelectPanel);

            if (recordIdSchema != null) {

                //existing record id schema should be removed
                Property recordIdentifyingColumnsSchema = mComponent.getProperty(PROP_RECORD_IDENTIFIER_COLUMNS_SCHEMA);
                String ridschemaName = recordIdentifyingColumnsSchema.getValue();
                if (ridschemaName != null && !ridschemaName.trim().equals("")) {
                    SchemaComponentContainer sc = model.getPlanComponent().getSchemaComponentContainer();
                    SchemaComponent sComp = sc.findSchema(ridschemaName);
                    if (sComp != null) {
                        model.startTransaction();
                        sc.removeSchemaComponent(sComp);
                        model.endTransaction();
                    }
                }

                model.startTransaction();
                SchemaComponentContainer sc = model.getPlanComponent().getSchemaComponentContainer();
                sc.addSchemaComponent(recordIdSchema);
                recordIdentifyingColumnsSchema.setValue(recordIdSchema.getName());
                model.endTransaction();
            }

            //store from clause
            if (mFromClause != null && mComponent.hasPropertyDefined(PROP_FROM_CLAUSE)) {
                mComponent.getModel().startTransaction();
                mComponent.setStringList(PROP_FROM_CLAUSE, mFromClause);
                mComponent.getModel().endTransaction();
            }

            //store where clause
            if (mWhereClause != null && mComponent.hasPropertyDefined(PROP_WHERE_CLAUSE)) {
                mComponent.getModel().startTransaction();
                mComponent.setString(PROP_WHERE_CLAUSE, mWhereClause);
                mComponent.getModel().endTransaction();
            }

        }

        class SelectReplayStreamTableActionListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                IEPModel model = getOperatorComponent().getModel();

                ReplayStreamWizardHelper helper = new ReplayStreamWizardHelper();
                WizardDescriptor wizardDescriptor = helper.createWizardDescriptor();


                Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
                dialog.getAccessibleContext().setAccessibleDescription(wizardDescriptor.getTitle());

                dialog.setVisible(true);
                dialog.toFront();
                boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
                if (!cancelled) {
                    List<TableInfo> tables = (List<TableInfo>) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_SELECTED_TABLES);
                    List<ColumnInfo> columns = (List<ColumnInfo>) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_SELECTED_COLUMNS);
                    mWhereClause = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_JOIN_CONDITION);
                    String databaseJNDIName = (String) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_JNDI_NAME);
                    List<ColumnInfo> recordIdentifyingColumns = (List<ColumnInfo>) wizardDescriptor.getProperty(DatabaseTableWizardConstants.PROP_POLLING_UNIQUE_RECORD_IDENTIFIER_COLUMNS);



                    mDatabaseJndiNamePanel.setStringValue(databaseJNDIName);


                    Iterator<TableInfo> tableIt = tables.iterator();
                    List<String> fromList = new ArrayList<String>();
                    while (tableIt.hasNext()) {
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
                    while (it.hasNext()) {
                        ColumnInfo column = it.next();
                        remainingColumns.remove(column);
                        String asColumnName = null;

                        asColumnName = GUIUtil.generateUniqueAsColumnName(column, usedupNames);

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
                    if (mWhereClause != null) {
                        mWherePanel.setStringValue(mWhereClause);
                    }

                    if (recordIdentifyingColumns != null && recordIdentifyingColumns.size() > 0) {
                        List<String> columnList = new ArrayList<String>();
                        attrs = new ArrayList<SchemaAttribute>();

                        it = recordIdentifyingColumns.iterator();
                        while (it.hasNext()) {
                            ColumnInfo column = it.next();
                            String asColumnName = fromColumnToAsColumnMap.get(column.getQualifiedName());
                            SchemaAttribute sa = DatabaseMetaDataHelper.createSchemaAttributeFromColumnInfo(column, asColumnName, model);
                            attrs.add(sa);
                            columnList.add(sa.getAttributeName());
                        }

                        mRecordIdentifyingColumnsTextField.setText(GUIUtil.convertListToCommaSeperatedValues(columnList));

//                        mRecordIdentifyingColumnsPanel.setStringValue(convertListToCommaSeperatedValues(columnList));
                    } else {
                        mRecordIdentifyingColumnsTextField.setText("");
                    }

                }
            }
        }

        class MySelectPanel extends SelectPanel {

            private static final long serialVersionUID = -4195259789503600814L;

            public MySelectPanel(OperatorComponent component) {
                super(component);
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

        class MyTableModel extends ExpressionDefaultMoveableRowTableModel {

            @Override
            public boolean isCellEditable(int row, int column) {
                //make data type editable since
                //if user selects a column which is not
                //of sql type as supported in iep
                //then he needs to explicity select a valid data type
                return super.isCellEditable(row, column);

            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                //to column is changed
                //we need to update record identifier column as well
                if (column == 1) {
                    String oldToColumn = (String) getValueAt(row, column);
                    //update gui
                    String recordIdColumns = mRecordIdentifyingColumnsTextField.getText();
                    if (!recordIdColumns.equals("")) {
                        recordIdColumns = recordIdColumns.replace(oldToColumn, (String) aValue);
                        mRecordIdentifyingColumnsTextField.setText(recordIdColumns);
                    }

                }
                super.setValueAt(aValue, row, column);
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
                Property fromClause = component.getProperty(SharedConstants.PROP_FROM_CLAUSE);

                if (fromClause != null) {
                    List tables = (List) fromClause.getPropertyType().getType().parse(fromClause.getValue());
                    if (tables != null) {
                        Iterator it = tables.iterator();
                        while (it.hasNext()) {
                            String tableQualifedName = (String) it.next();
                            DefaultMutableTreeNode inputNode = new DefaultMutableTreeNode(tableQualifedName);
                            this.mRoot.add(inputNode);
                        }
                    }
                }
            } catch (Exception e) {
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
                if (tables != null) {
                    Iterator<TableInfo> it = tables.iterator();
                    while (it.hasNext()) {
                        TableInfo table = it.next();
                        String tableQualifedName = table.getQualifiedName();
                        DefaultMutableTreeNode inputNode = new DefaultMutableTreeNode(tableQualifedName);
                        this.mRoot.add(inputNode);
                    }
                }
            } catch (Exception e) {
                mLog.log(Level.SEVERE, NbBundle.getMessage(InputSchemaTreeModel.class,
                        "InputSchemaTreeModel.FAIL_TO_BUILD_TREE_MODEL_FOR", component.getTitle()), e);
            }
        }
    }
}
