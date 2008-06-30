/*
 * DataSetDatabindingElement.java
 *
 * Created on June 3, 2008, 4:20 PM
 */
package org.netbeans.modules.vmd.midp.propertyeditors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.midp.components.MidpDocumentSupport;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.categories.CommandsCategoryCD;
import org.netbeans.modules.vmd.midp.components.categories.DatabindingCategoryCD;
import org.netbeans.modules.vmd.midp.components.databinding.DataSetAbstractCD;
import org.netbeans.modules.vmd.midp.components.databinding.DataSetConnectorCD;
import org.netbeans.modules.vmd.midp.components.databinding.IndexableDataSetCD;
import org.netbeans.modules.vmd.midp.components.databinding.MidpDatabindingSupport;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;

/**
 *
 * @author Karol Harezlak
 */
public class DatabindingElementUI extends javax.swing.JPanel {

    private static String NULL = "<null>"; //TODO Localized
    private DesignPropertyEditor propertyEditor;
    private JRadioButton radioButton;
    private static String INDEXABLE =  "[IndexableDataSet]"; //NOI18N
    private static String DATASET = "[DataSet]"; //NOI18N

    /** Creates new form DataSetDatabindingElement */
    DatabindingElementUI(DesignPropertyEditor propertyEditor, final JRadioButton radioButton) {
        this.propertyEditor = propertyEditor;
        this.radioButton = radioButton;
        initComponents();
        jComboBoxDatasets.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateExpressionPreview();
                updateWarning();
                updateNextPreviousCommands();
            }
        });
        updateExpressionPreview();
        ComponentFocusAdapter focusListener = new ComponentFocusAdapter();
        jTextFieldExpression.addFocusListener(focusListener);
        jComboBoxCommandUpdate.addFocusListener(focusListener);
        jComboBoxDatasets.addFocusListener(focusListener);
        jTextFieldExpression.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                updateWarning();
                updateExpressionPreview();
            }
        });
        radioButton.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (!radioButton.isSelected()) {
                    jLabelWarning.setText(null);
                } else {
                    updateWarning();
                }
            }
        });
        updateNextPreviousCommands();

    }
    

    private boolean updateWarning() {

        if (radioButton.isSelected()) {
            if (jComboBoxDatasets.getSelectedItem() == null || jComboBoxDatasets.getSelectedItem() == NULL) {
                jLabelWarning.setText("Warning: Dataset not Selected.");
                return false;
            } else if (jTextFieldExpression.getText() == null || jTextFieldExpression.getText().length() == 0) {
                jLabelWarning.setText("Warning: Empty expression.");
                return false;
            } else {
                jLabelWarning.setText(null);
            }
        } else {
            jLabelWarning.setText(null);
        }
        return true;
    }

    private void updateExpressionPreview() {
        if (jComboBoxDatasets.getSelectedItem() != null && jComboBoxDatasets.getSelectedItem() != NULL) {
            jTextFieldExpression.setEnabled(true);
            jComboBoxCommandUpdate.setEnabled(true);
            jLabelPreview.setText(cleanUpDataSetName(jComboBoxDatasets.getSelectedItem().toString()) + "." + jTextFieldExpression.getText()); //NOI18N
        } else {
            jComboBoxCommandUpdate.setEnabled(false);
            jTextFieldExpression.setEnabled(false);
        }
    }
    
    private void updateNextPreviousCommands() {
        String name = (String) jComboBoxDatasets.getSelectedItem();
        if (name != null && name.contains(INDEXABLE)) {
            jComboBoxCommandsIndexablePrevious.setEnabled(true);
            jComboBoxIndexableNext.setEnabled(true);
        } else if (name != null && name.contains(DATASET)){
            jComboBoxCommandsIndexablePrevious.setEnabled(false);
            jComboBoxCommandsIndexablePrevious.setSelectedItem(NULL);
            jComboBoxIndexableNext.setEnabled(false);
            jComboBoxIndexableNext.setSelectedItem(NULL);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboBoxDatasets = new javax.swing.JComboBox();
        jTextFieldExpression = new javax.swing.JTextField();
        jLabelPreview = new javax.swing.JLabel();
        jLabelWarning = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jComboBoxIndexableNext = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jComboBoxCommandsIndexablePrevious = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jComboBoxCommandUpdate = new javax.swing.JComboBox();

        setMaximumSize(new java.awt.Dimension(0, 0));
        setMinimumSize(new java.awt.Dimension(100, 100));
        setPreferredSize(new java.awt.Dimension(390, 200));

        jTabbedPane1.setMinimumSize(new java.awt.Dimension(0, 0));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(0, 0));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jPanel1.border.title"))); // NOI18N
        jPanel1.setFocusable(false);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel3.text")); // NOI18N

        jTextFieldExpression.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jTextFieldExpression.text")); // NOI18N
        jTextFieldExpression.setEnabled(false);

        jLabelPreview.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabelPreview.text_1")); // NOI18N

        jLabelWarning.setForeground(new java.awt.Color(255, 0, 0));
        jLabelWarning.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabelWarning.text_1")); // NOI18N
        jLabelWarning.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabelPreview, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                    .add(jTextFieldExpression, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jComboBoxDatasets, 0, 272, Short.MAX_VALUE)))
            .add(jLabelWarning, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jComboBoxDatasets, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jTextFieldExpression, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jLabelPreview))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 41, Short.MAX_VALUE)
                .add(jLabelWarning))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel4.setPreferredSize(new java.awt.Dimension(0, 0));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jPanel3.border.title"))); // NOI18N
        jPanel3.setFocusable(false);
        jPanel3.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel3.setPreferredSize(new java.awt.Dimension(0, 0));

        jLabel6.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel6.text")); // NOI18N

        jComboBoxIndexableNext.setEnabled(false);

        jLabel7.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel7.text")); // NOI18N

        jComboBoxCommandsIndexablePrevious.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jComboBoxIndexableNext, 0, 275, Short.MAX_VALUE)
                    .add(jComboBoxCommandsIndexablePrevious, 0, 275, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(jComboBoxIndexableNext, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jComboBoxCommandsIndexablePrevious, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jPanel2.border.title"))); // NOI18N
        jPanel2.setFocusable(false);

        jLabel8.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel8.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jLabel5.text")); // NOI18N

        jComboBoxCommandUpdate.setEnabled(false);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(67, 67, 67)
                .add(jLabel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE))
            .add(jPanel2Layout.createSequentialGroup()
                .add(jLabel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBoxCommandUpdate, 0, 275, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(jComboBoxCommandUpdate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(41, 41, 41)
                .add(jLabel8))
        );

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(DatabindingElementUI.class, "DatabindingElementUI.jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    public void updateComponent(final DesignComponent component) {
        component.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                final DesignDocument document = component.getDocument();

                if (document == null) {
                    return;
                }
                jComboBoxDatasets.setModel(new Model(component, DatabindingCategoryCD.TYPEID));
                jComboBoxCommandUpdate.setModel(new Model(component, CommandsCategoryCD.TYPEID));
                jComboBoxCommandsIndexablePrevious.setModel(new Model(component, CommandsCategoryCD.TYPEID));
                jComboBoxIndexableNext.setModel(new Model(component, CommandsCategoryCD.TYPEID));
                
                DesignComponent connector = MidpDatabindingSupport.getConnector(component, propertyEditor.getPropertyNames().get(0));
                if (connector != null) {
                    radioButton.setSelected(true);
                    String dataSetName =  createDataSetName((String) connector.getParentComponent().readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue(), connector.getParentComponent());
                    jComboBoxDatasets.setSelectedItem(dataSetName);
                    jTextFieldExpression.setText((String) connector.readProperty(DataSetConnectorCD.PROP_EXPRESSION).getPrimitiveValue());
                    
                    setCommandComboBox(connector, jComboBoxCommandUpdate, DataSetConnectorCD.PROP_UPDATE_COMMAND);
                    setCommandComboBox(connector, jComboBoxIndexableNext, DataSetConnectorCD.PROP_NEXT_COMMAND);
                    setCommandComboBox(connector, jComboBoxCommandsIndexablePrevious, DataSetConnectorCD.PROP_PREVIOUS_COMMAND);
                    
                    if (jComboBoxDatasets.getSelectedItem() != null) {
                        jLabelPreview.setText(jComboBoxDatasets.getSelectedItem().toString()+"."+jTextFieldExpression.getText());
                    }
                }
            }
        });
        updateExpressionPreview();
        updateWarning();
    }
    
    private void setCommandComboBox(DesignComponent connector, JComboBox comboBox, String propertyName) {
        DesignComponent command = connector.readProperty(propertyName).getComponent();
        if (command != null) {
            String commandName = (String) command.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue();
            comboBox.setSelectedItem(commandName);
        }
        
    }

    public void saveToModel(final DesignComponent component) {
        final DesignDocument document = component.getDocument();
        if (!updateWarning()) {
            return;
        }
        document.getTransactionManager().writeAccess(new Runnable() {

            public void run() {
                DesignComponent connector = MidpDatabindingSupport.getConnector(component, propertyEditor.getPropertyNames().get(0));
                String selectedDataSet = cleanUpDataSetName((String) jComboBoxDatasets.getSelectedItem());
                String selectedUpdateCommand = (String) jComboBoxCommandUpdate.getSelectedItem();
                String selectedNextCommand = (String) jComboBoxIndexableNext.getSelectedItem();
                String selectedPreviousCommand = (String) jComboBoxCommandsIndexablePrevious.getSelectedItem();
                Collection<DesignComponent> dataSets = MidpDocumentSupport.getCategoryComponent(document, DatabindingCategoryCD.TYPEID).getComponents();
               
                for (DesignComponent dataSet : dataSets) {
                    if (dataSet.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue().equals(selectedDataSet)) {
                        if (connector == null) {
                            connector = document.createComponent(DataSetConnectorCD.TYPEID);
                            connector.writeProperty(DataSetConnectorCD.PROP_BINDED_PROPERTY, MidpTypes.createStringValue(propertyEditor.getPropertyNames().get(0)));
                            dataSet.addComponent(connector);
                        }
                        connector.writeProperty(DataSetConnectorCD.PROP_COMPONENT_ID, MidpTypes.createLongValue(component.getComponentID()));
                        connector.writeProperty(DataSetConnectorCD.PROP_EXPRESSION, MidpTypes.createStringValue(jTextFieldExpression.getText())); //NOI18N
                        if (!selectedDataSet.equalsIgnoreCase(NULL)) {
                            saveCommands(document, connector, selectedUpdateCommand, DataSetConnectorCD.PROP_UPDATE_COMMAND);
                            saveCommands(document, connector, selectedNextCommand, DataSetConnectorCD.PROP_NEXT_COMMAND);
                            saveCommands(document, connector, selectedPreviousCommand,DataSetConnectorCD.PROP_PREVIOUS_COMMAND);
                        }
                        break;
                    }
                }
            }
        });
    }
    
    private void saveCommands(DesignDocument document,  DesignComponent connector, String commandName, String propertyName) {
        assert document != null || connector != null || propertyName != null;
        Collection<DesignComponent> commands = MidpDocumentSupport.getCategoryComponent(document, CommandsCategoryCD.TYPEID).getComponents();
        for (DesignComponent command : commands) {
            PropertyValue  value = command.readProperty(ClassCD.PROP_INSTANCE_NAME);
            if (value != PropertyValue.createNull() && value.getPrimitiveValue().equals(commandName)) {
                connector.writeProperty(propertyName, PropertyValue.createComponentReference(command));
                break;
            }
        }
    }

    public void resetValuesInModel(final DesignComponent component) {
        final DesignDocument document = component.getDocument();
        document.getTransactionManager().writeAccess(new Runnable() {

            public void run() {
                DesignComponent connector = MidpDatabindingSupport.getConnector(component, propertyEditor.getPropertyNames().get(0));
                if (connector != null) {
                    document.deleteComponent(connector);
                }
            }
        });
        jComboBoxCommandUpdate.setSelectedItem(NULL);
        jComboBoxDatasets.setSelectedItem(NULL);
        jComboBoxCommandsIndexablePrevious.setSelectedItem(NULL);
        jComboBoxIndexableNext.setSelectedItem(NULL);
        jTextFieldExpression.setText(null);
        jTextFieldExpression.setEnabled(false);
        jLabelPreview.setText(null);
        updateWarning();
        updateNextPreviousCommands();
    }
    
    private String createDataSetName(String name, DesignComponent c) {
        DescriptorRegistry registry = c.getDocument().getDescriptorRegistry();
        if (registry.isInHierarchy(IndexableDataSetCD.TYPEID, c.getType())) {
            name = name + " " + INDEXABLE; //NOI18N
        } else if (registry.isInHierarchy(DataSetAbstractCD.TYPEID, c.getType())) {
            name = name + " " + DATASET; //NOI18N
        }
        return name;
    }
    
    private String cleanUpDataSetName(String name) {
        if (name != null) {
            name = name.replace(INDEXABLE, "").replace(DATASET, "").trim(); //NOI18N
        }
        return name;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxCommandUpdate;
    private javax.swing.JComboBox jComboBoxCommandsIndexablePrevious;
    private javax.swing.JComboBox jComboBoxDatasets;
    private javax.swing.JComboBox jComboBoxIndexableNext;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelPreview;
    private javax.swing.JLabel jLabelWarning;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextFieldExpression;
    // End of variables declaration//GEN-END:variables

    private class Model implements ComboBoxModel {

        private final List<String> names;
        private WeakReference compRef;
        private String selectedItem;
        private TypeID categoryType;

        Model(DesignComponent component, TypeID categoryType) {
            this.categoryType = categoryType;
            this.compRef = new WeakReference(component);
            this.names = new ArrayList<String>();
            this.names.add(NULL);
            Collection<DesignComponent> components = MidpDocumentSupport.getCategoryComponent(component.getDocument(), categoryType).getComponents();
            for (DesignComponent c : components) {
                String name = (String) c.readProperty(ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue();
                if (name != null && !name.trim().equals("")) { //NOI18N
                    name = createDataSetName(name, c);
                    names.add(name);
                }
            }
        }

        public void setSelectedItem(final Object item) {
            final DesignComponent component = (DesignComponent) compRef.get();
            if (component == null) {
                return;
            }
            if (item instanceof String) {
                component.getDocument().getTransactionManager().readAccess(new Runnable() {
                    public void run() {                  
                        String name = (String) item;
                        name = cleanUpDataSetName(name);
                        Collection<DesignComponent> components = MidpDocumentSupport.getCategoryComponent(component.getDocument(), categoryType).getComponents();
                        for (DesignComponent c : components) {
                            PropertyValue  value = c.readProperty(ClassCD.PROP_INSTANCE_NAME);
                            if ( value != PropertyValue.createNull() && value.getPrimitiveValue().equals(name) || name.equals(NULL)) {
                                selectedItem = (String) item;
                                break;
                            }
                        }
                    }
                });
            } else if (item == null) {
                this.selectedItem = NULL;
            } else {
                throw new IllegalArgumentException("Setting argumant is not String type"); //NOI18N

            }
        }

        public Object getSelectedItem() {
            return this.selectedItem;

        }

        public int getSize() {
            return names.size();
        }

        public Object getElementAt(int index) {
            return names.get(index);
        }

        public void addListDataListener(ListDataListener l) {
        }

        public void removeListDataListener(ListDataListener l) {
        }

        
    }
    

    private class ComponentFocusAdapter extends FocusAdapter {

        @Override
        public void focusGained(FocusEvent e) {
            radioButton.setSelected(true);
        }
            
    }
    
}
