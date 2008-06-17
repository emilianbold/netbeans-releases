/*
 * TableInputConfigurationPanel.java
 *
 * Created on May 16, 2008, 6:09 PM
 */

package org.netbeans.modules.iep.editor.ps;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;

import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.dialog.NotifyHelper;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.netbeans.modules.iep.model.TableInputOperatorComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author  radval
 */
public class TableInputConfigurationPanel extends javax.swing.JPanel {

    private static final String CONFIG_INTERNAL_TABLE = "CONFIG_INTERNAL_TABLE";
    
    private static final String CONFIG_EXTERNAL_TABLE = "CONFIG_EXTERNAL_TABLE";
    
    private ConfigurationType internalTableType = new ConfigurationType(CONFIG_INTERNAL_TABLE, NbBundle.getMessage(TableInputConfigurationPanel.class, "TableInputConfigurationPanel_ConfigurationType_InternalTable"));
    private ConfigurationType externalTableType = new ConfigurationType(CONFIG_EXTERNAL_TABLE, NbBundle.getMessage(TableInputConfigurationPanel.class, "TableInputConfigurationPanel_ConfigurationType_ExternalTable"));
        
    private TableInputInternalTableConfigurationPanel mInternalTableConfigPanel;
    private TableInputExternalTableConfigurationPanel mExternalTableConfigPanel;

    private CardLayout mCardLayout;
    
    private TableInputOperatorComponent mComponent;
    
    private SelectPanel mSelectPanel;
    
    /** Creates new form TableInputConfigurationPanel */
    public TableInputConfigurationPanel(TableInputOperatorComponent op, SelectPanel selectPanel) {
        this.mComponent = op;
        this.mSelectPanel = selectPanel;
        initComponents();
        init();
        
    }

    public void validate(PropertyChangeEvent evt) throws PropertyVetoException {
     
        //validate name
        String nameStr = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.NAME");
        Property nameProp = mComponent.getProperty(SharedConstants.NAME_KEY);
        GUIValidationHelper.validateProperty(nameStr, getOperatorName(), nameProp, evt);
       
        GUIValidationHelper.validateForUniqueOperatorName(mComponent.getModel(), mComponent, getOperatorName(), evt);
      
        //validate schema
        Property outputSchemaNameProp = mComponent.getProperty(SharedConstants.OUTPUT_SCHEMA_ID_KEY);
        String outputSchemaNameStr = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.OUTPUT_SCHEMA_NAME");
        GUIValidationHelper.validateProperty(outputSchemaNameStr, getOutputSchemaName(), outputSchemaNameProp, evt);
        
        GUIValidationHelper.validateForUniqueSchemaName(mComponent.getModel(), mComponent, getOutputSchemaName(), evt);
        
        String globalId = getGlobalId();
        if ((globalId == null || globalId.trim().equals(""))) {
            String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                    "CustomEditor.GLOBAL_ID_MUST_BE_DEFINED_FOR_A_GLOBAL_ENTITY");
            throw new PropertyVetoException(msg, evt);
        }
    }
    
    public void store() {
        IEPModel model = mComponent.getModel();
        model.startTransaction();
        SchemaComponentContainer scContainer = model.getPlanComponent().getSchemaComponentContainer();
        
        try {
            // name
            mComponent.setName(getOperatorName());
            
            // schema
            String newSchemaName = getOutputSchemaName();;
            SchemaComponent outputSchema = mComponent.getOutputSchemaId();
            String schemaName = null;
            if(outputSchema != null) {
                schemaName = outputSchema.getName();
            }

            boolean schemaExist = schemaName != null && !schemaName.trim().equals("") && outputSchema != null;
            //ritList attributes = mSelectPanel.getAttributeMetadataAsList();
            List<SchemaAttribute> attrs = mSelectPanel.getAttributes();
            if (schemaExist) {
                if (!newSchemaName.equals(schemaName)) {
                    model.startTransaction();
                    SchemaComponent sc = model.getFactory().createSchema(model);
                    sc.setName(newSchemaName);
                    sc.setTitle(newSchemaName);
                    sc.setSchemaAttributes(attrs);


                    scContainer.addSchemaComponent(sc);
                    scContainer.removeSchemaComponent(outputSchema);

                    mComponent.setOutputSchemaId(sc);
                }else {
                    outputSchema.setSchemaAttributes(attrs);
                }
            } else {
                
                SchemaComponent sc = model.getFactory().createSchema(model);
                sc.setName(newSchemaName);
                sc.setTitle(newSchemaName);
                sc.setSchemaAttributes(attrs);

                scContainer.addSchemaComponent(sc);
                mComponent.setOutputSchemaId(sc);
            }
            
            //globalid
            mComponent.setGlobalId(getGlobalId());
            
            if(configurationTypeComboBox.getSelectedItem().equals(externalTableType)) {
                //external table
                mComponent.setExternalTableName(mExternalTableConfigPanel.getExternalTableName());
                //jndi name
                mComponent.setDatabaseJndiName(mExternalTableConfigPanel.getJNDIName());
            } else {
                //external table
                mComponent.setExternalTableName("");
                //jndi name
                mComponent.setDatabaseJndiName("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            NotifyHelper.reportError(e.getMessage());
        }
        
        model.endTransaction();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configurationPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        configurationTypeComboBox = new javax.swing.JComboBox();

        org.jdesktop.layout.GroupLayout configurationPanelLayout = new org.jdesktop.layout.GroupLayout(configurationPanel);
        configurationPanel.setLayout(configurationPanelLayout);
        configurationPanelLayout.setHorizontalGroup(
            configurationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 390, Short.MAX_VALUE)
        );
        configurationPanelLayout.setVerticalGroup(
            configurationPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 100, Short.MAX_VALUE)
        );

        jLabel1.setText(org.openide.util.NbBundle.getMessage(TableInputConfigurationPanel.class, "TableInputConfigurationPanel.jLabel1.text")); // NOI18N

        configurationTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        configurationTypeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                configurationTypeComboBoxItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(configurationTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 157, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(configurationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(configurationTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(configurationPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void configurationTypeComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_configurationTypeComboBoxItemStateChanged
    ConfigurationType type = (ConfigurationType) evt.getItem();
    mCardLayout.show(configurationPanel, type.getType());

}//GEN-LAST:event_configurationTypeComboBoxItemStateChanged

    private void init() {
        this.mInternalTableConfigPanel = new TableInputInternalTableConfigurationPanel();
        this.mExternalTableConfigPanel = new TableInputExternalTableConfigurationPanel(this.mComponent, this.mSelectPanel);
        
        mCardLayout = new CardLayout();
        
        this.configurationPanel.setLayout(mCardLayout);
        
        this.configurationPanel.add(this.mInternalTableConfigPanel, CONFIG_INTERNAL_TABLE);
        this.configurationPanel.add(this.mExternalTableConfigPanel, CONFIG_EXTERNAL_TABLE);
        
        Vector v = new Vector();
        
        
        v.add(internalTableType);
        v.add(externalTableType);
        
        configurationTypeComboBox.setModel(new DefaultComboBoxModel(v));
        
        mCardLayout.show(this.configurationPanel, CONFIG_INTERNAL_TABLE);
        
        //if any of the external table configuration is specified 
        //then it means we need to show external table configuration
        //option gui
        
        //external table name
        String externalTableName = mComponent.getExternalTableName();
        if(externalTableName != null && !externalTableName.trim().equals("")) {
            configurationTypeComboBox.setSelectedItem(externalTableType);
            mCardLayout.show(this.configurationPanel, CONFIG_EXTERNAL_TABLE);
            mExternalTableConfigPanel.setExternalTableName(externalTableName);
        }
        
        //jndi name
        String databaseJNDIName  = mComponent.getDatabaseJndiName();
        if(databaseJNDIName != null) {
            mExternalTableConfigPanel.setJNDIName(databaseJNDIName);
        }
        
        // name
        String operatorName = mComponent.getName();
        setOperatorName(operatorName);
        
        //output schema
        SchemaComponent sc = mComponent.getOutputSchemaId();
        if(sc != null) {
            String outputSchemaName = sc.getName();
            setOutputSchemaName(outputSchemaName);
        } else {
            if (mComponent.isSchemaOwner()) {
              IEPModel model = mComponent.getModel();
              String schemaName = NameGenerator.generateSchemaName(model.getPlanComponent().getSchemaComponentContainer());
              setOutputSchemaName(schemaName);
          }
        }
                
        
        //global id
        String globalId = mComponent.getGlobalId();
        setGlobalId(globalId);
        
        
        
    }
    
    private String getOperatorName() {
        if(configurationTypeComboBox.getSelectedItem().equals(internalTableType)) {
            return mInternalTableConfigPanel.getOperatorName();
        } else {
            return mExternalTableConfigPanel.getOperatorName();
        }
    }
    
    private void setOperatorName(String operatorName) {
            mInternalTableConfigPanel.setOperatorName(operatorName);
            mExternalTableConfigPanel.setOperatorName(operatorName);
    }
    
    private void setOutputSchemaName(String outputSchemaName) {
            mInternalTableConfigPanel.setOutputSchemaName(outputSchemaName);
            mExternalTableConfigPanel.setOutputSchemaName(outputSchemaName);
    }
    
    private String getOutputSchemaName() {
        
        if(configurationTypeComboBox.getSelectedItem().equals(internalTableType)) {
            return mInternalTableConfigPanel.getOutputSchemaName();
        } else {
            return mExternalTableConfigPanel.getOutputSchemaName();
        }
    }
    
    private void setGlobalId(String globalId) {
            mInternalTableConfigPanel.setGlobalId(globalId);
            mExternalTableConfigPanel.setGlobalId(globalId);
        
    }
    
    private String getGlobalId() {
        
        if(configurationTypeComboBox.getSelectedItem().equals(internalTableType)) {
            return mInternalTableConfigPanel.getGlobalId();
        } else {
            return mExternalTableConfigPanel.getGlobalId();
        }
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel configurationPanel;
    private javax.swing.JComboBox configurationTypeComboBox;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    
    class ConfigurationType {
        
        private String mType;
        
        private String mTypeDisplayName;
        
        ConfigurationType(String type, String typeDisplayName) {
            this.mType = type;
            this.mTypeDisplayName = typeDisplayName;
        }
        
        public String toString() {
            return this.mTypeDisplayName;
        }
        
        public String getType() {
            return this.mType;
        }
    }
}
