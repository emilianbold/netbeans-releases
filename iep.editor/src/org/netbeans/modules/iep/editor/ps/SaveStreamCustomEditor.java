package org.netbeans.modules.iep.editor.ps;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.netbeans.modules.iep.editor.designer.GuiConstants;
import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodePropertyCustomizerState;
import org.netbeans.modules.tbls.editor.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.tbls.editor.table.NoExpressionDefaultMoveableRowTableModel;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.tbls.model.TcgPropertyType;
import org.openide.DialogDescriptor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

public class SaveStreamCustomEditor extends DefaultCustomEditor {

    private static final Logger mLog = Logger.getLogger(SaveStreamCustomEditor.class.getName());
    private boolean mValidOperatorSelection = false;

    /** Creates a new instance of SaveStreamCustomEditor */
    public SaveStreamCustomEditor() {
        super();
    }

    public Component getCustomEditor() {
        if (mEnv != null) {
            return new MyCustomizer(getPropertyType(), getOperatorComponent(), mEnv);
        }
        return new MyCustomizer(getPropertyType(), getOperatorComponent(), mCustomizerState);
    }

    private class MyCustomizer extends DefaultCustomizer {

        protected PropertyPanel mTableNamePanel;
        protected PropertyPanel mDbJndiNamePanel;
        protected PropertyPanel mIsPreserveTablePanel;

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
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mNamePanel.component[1], gbc);

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

            // table name
            Property globalId = mComponent.getProperty(PROP_TABLE_NAME);
            String globalIdLabel = NbBundle.getMessage(SaveStreamCustomEditor.class, "SaveStreamCustomEditor.TABLE_NAME");
            mTableNamePanel = PropertyPanel.createSingleLineTextPanelWithoutFilter(globalIdLabel, globalId, false);

            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mTableNamePanel.component[0], gbc);

            gbc.gridx = 4;
            gbc.gridy = 0;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mTableNamePanel.component[1], gbc);


            //isGlobal
            Property isGlobal = mComponent.getProperty(PROP_IS_PRESERVE_TABLE);
            String isGlobalLabel = NbBundle.getMessage(SaveStreamCustomEditor.class, "SaveStreamCustomEditor.IS_GLOBAL");
            mIsPreserveTablePanel = PropertyPanel.createCheckBoxPanel(isGlobalLabel, isGlobal);

            String isGlobalTooltip = NbBundle.getMessage(SaveStreamCustomEditor.class, "SaveStreamCustomEditor.IS_GLOBAL_TOOLTIP");
            mIsPreserveTablePanel.input[0].setToolTipText(isGlobalTooltip);

            gbc.gridx = 5;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mIsPreserveTablePanel.input[0], gbc);

            // database jndi name
            Property dbJndiNameProp = mComponent.getProperty(PROP_DATABASE_JNDI_NAME);
            String dbJndiNameStr = NbBundle.getMessage(SaveStreamCustomEditor.class, "SaveStreamCustomEditor.DATABASE_JNDI_NAME");
            mDbJndiNamePanel = PropertyPanel.createSingleLineTextPanelWithoutFilter(dbJndiNameStr, dbJndiNameProp, false);
            if (mDbJndiNamePanel.getStringValue() == null || mDbJndiNamePanel.getStringValue().equals("")) {
                mDbJndiNamePanel.setStringValue(DEFAULT_JNDINAME);

            }
            gbc.gridx = 3;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mDbJndiNamePanel.component[0], gbc);

            gbc.gridx = 4;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mDbJndiNamePanel.component[1], gbc);

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
        protected SelectPanel createSelectPanel(OperatorComponent component) {
            return new MySelectPanel(component);
        }

        @Override
        protected boolean isShowFromClause() {
            return false;
        }

        public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
            super.validateContent(evt);
            try {
                mTableNamePanel.validateContent(evt);
                mDbJndiNamePanel.validateContent(evt);
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
            mTableNamePanel.store();
            mDbJndiNamePanel.store();
            mIsPreserveTablePanel.store();
        }
    }

    class StreamInputChooserPanelPropertyListener implements PropertyChangeListener {

        private DialogDescriptor mDD;

        StreamInputChooserPanelPropertyListener(DialogDescriptor dd) {
            this.mDD = dd;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (StreamInputChooserPanel.PROP_SELECTED_INPUT_OPERATOR_COMPONENT.equals(evt.getPropertyName())) {
                if (evt.getNewValue() != null) {
                    mValidOperatorSelection = true;
                } else {
                    mValidOperatorSelection = false;
                }
            } else {
                mValidOperatorSelection = false;
            }

            this.mDD.setValid(mValidOperatorSelection);

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

    class MyTableModel extends NoExpressionDefaultMoveableRowTableModel {

        @Override
        public boolean isCellEditable(int row, int column) {

            return false;
        }
    }
}
