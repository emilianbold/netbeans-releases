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
 * EjbJarRootCustomizer.java
 *
 * Created on October 1, 2003, 3:40 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.awt.event.ActionListener;

//DEPLOYMENT API
import javax.enterprise.deploy.spi.DConfigBean;
import javax.swing.event.TableModelListener;

import org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.CmpResource;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptor;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptors;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.PmInuse;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SchemaGeneratorProperties;
import org.netbeans.modules.j2ee.sun.share.configbean.EjbJarRoot;
import org.netbeans.modules.j2ee.sun.share.configbean.StorageBeanFactory;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanCustomizer;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.CustomizerTitlePanel;


/**
 *
 * @author  Rajeshwar Patil
 */
public class EjbJarRootCustomizer extends BeanCustomizer
	implements TableModelListener, org.netbeans.modules.j2ee.sun.share.Constants {

    private EjbJarRoot theBean;
    private MessageDestinationPanel msgDstnPanel;
    private WebserviceDescriptionPanel websrvcDescPanel;
    private PmDescriptorPanel pmDescptrPanel;
    private PropertyPanel cmpPropertyPanel;
    private PropertyPanel schmaGnrtrPropPanel;

    static final ResourceBundle bundle = 
        ResourceBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.Bundle"); // NOI18N


    /** Creates new customizer EjbJarRootCustomizer */
    public EjbJarRootCustomizer() {
        initComponents();
        createPanels();
    }
    
    public void setObject(Object bean) {
                super.setObject(bean);
		// Only do this if the bean is actually changing.
		if(theBean != bean) {
			if(theBean != null) {
				// commit old object
			}

			if(bean instanceof EjbJarRoot) {
				theBean = (EjbJarRoot) bean;
				setComponentValues();
			}
		}
                initializing = false;
    }

    
    private void createPanels(){
		// Add title panel
		CustomizerTitlePanel titlePanel = new CustomizerTitlePanel();
		titlePanel.setCustomizerTitle(bundle.getString("LBL_SunEjbJar")); //NOI18N
		add(titlePanel, titlePanel.getConstraints(), 0);		
		
        MessageDestinationModel msgDstnModel = new MessageDestinationModel();
        msgDstnModel.addTableModelListener(this);
        msgDstnPanel = new MessageDestinationPanel(msgDstnModel);
        msgDstnPanel.getAccessibleContext().setAccessibleName(bundle.getString("MessageDestination_Acsbl_Name"));             //NOI18N
        msgDstnPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("MessageDestination_Acsbl_Desc"));      //NOI18N
        tabbedPanel.addTab(bundle.getString("LBL_MessageDestination"), // NOI18N
            msgDstnPanel);

        WebserviceDescriptionModel websrvcDescModel = 
            new WebserviceDescriptionModel();
        websrvcDescModel.addTableModelListener(this);
        websrvcDescPanel = new WebserviceDescriptionPanel(websrvcDescModel);
        websrvcDescPanel.getAccessibleContext().setAccessibleName(bundle.getString("WebserviceDescription_Acsbl_Name"));             //NOI18N
        websrvcDescPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("WebserviceDescription_Acsbl_Desc"));      //NOI18N
        tabbedPanel.addTab(bundle.getString("LBL_WebserviceDescription"), // NOI18N
            websrvcDescPanel);

        PmDescriptorModel pmDescptrModel =  new PmDescriptorModel();
        pmDescptrModel.addTableModelListener(this);
        pmDescptrPanel = new PmDescriptorPanel(pmDescptrModel);
        pmDescptrPanel.getAccessibleContext().setAccessibleName(bundle.getString("PmDescriptor_Acsbl_Name"));             //NOI18N
        pmDescptrPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("PmDescriptor_Acsbl_Desc"));      //NOI18N
        tabbedPanel.addTab(bundle.getString("LBL_PmDescriptor"),       // NOI18N
            pmDescptrPanel);

        PropertyModel cmpPropertyModel = new PropertyModel("CmpResource");// NOI18N
        cmpPropertyModel.addTableModelListener(this);
        cmpPropertyPanel = new PropertyPanel(cmpPropertyModel, 
                bundle.getString("CmpProperty_Acsbl_Name"), bundle.getString("CmpProperty_Acsbl_Desc"), "AS_CFG_Cmp_Resource_Property");  //NOI18N 
        cmpPropertyPanel.getAccessibleContext().setAccessibleName(bundle.getString("CmpProperty_Acsbl_Name"));             //NOI18N
        cmpPropertyPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("CmpProperty_Acsbl_Desc"));      //NOI18N
        tabbedPanel.addTab(bundle.getString("LBL_CmpProperty"),        // NOI18N 
            cmpPropertyPanel);

        PropertyModel schmaGnrtrPropModel = new PropertyModel("SchemaGenerator"); // NOI18N
        schmaGnrtrPropModel.addTableModelListener(this);
        schmaGnrtrPropPanel = new PropertyPanel(schmaGnrtrPropModel,
                bundle.getString("SchemaGeneratorProperty_Acsbl_Name"), bundle.getString("SchemaGeneratorProperty_Acsbl_Desc"), "AS_CFG_Schema_Generator_Property");  //NOI18N
        schmaGnrtrPropPanel.getAccessibleContext().setAccessibleName(bundle.getString("SchemaGeneratorProperty_Acsbl_Name"));             //NOI18N
        schmaGnrtrPropPanel.getAccessibleContext().setAccessibleDescription(bundle.getString("SchemaGeneratorProperty_Acsbl_Desc"));      //NOI18N
        tabbedPanel.addTab(bundle.getString("LBL_SchemaGeneratorProperty"), // NOI18N
            schmaGnrtrPropPanel);
    }


    public Collection getErrors(){
        if(validationSupport == null) assert(false);
        ArrayList errors = new ArrayList();

        String property = nameTextField.getText();
        errors.addAll(validationSupport.validate(property,
            "/sun-ejb-jar/enterprise-beans/name",                       //NOI18N
                bundle.getString("LBL_Name")));         //NOI18N

        //Cmp Resource field Validation
        //FIXME: cmpBeanPresentInJar flag needs to be set based on cmp bean
        //presence in ejb jar
        boolean cmpBeanPresentInJar = true;
        if(cmpBeanPresentInJar){
            boolean cmpResourcePresent = isCmpResourcePresent();
            if(cmpResourcePresent){
                property = jndiNameField.getText();
                errors.addAll(validationSupport.validate(property,
                    "/sun-ejb-jar/enterprise-beans/cmp-resource/jndi-name",     //NOI18N
                        bundle.getString("LBL_Jndi_Name")));                    //NOI18N     

                boolean dfltResrcPrncplPresent = isDfltResrcPrncplPresent();
                if(dfltResrcPrncplPresent){
                    property = dfltResrcPrncplNameTextField.getText();
                    errors.addAll(validationSupport.validate(property,
                        "/sun-ejb-jar/enterprise-beans/cmp-resource/default-resource-principal/name", //NOI18N
                            bundle.getString("LBL_Name")));                         //NOI18N

                    property = dfltResrcPrncplPaswrdTextField.getText();
                    errors.addAll(validationSupport.validate(property,
                        "/sun-ejb-jar/enterprise-beans/cmp-resource/default-resource-principal/password", //NOI18N
                            bundle.getString("LBL_Password")));                     //NOI18N
                }

                property = (String)crteTblAtDplyComboBox.getSelectedItem();
                errors.addAll(validationSupport.validate(property,
                    "/sun-ejb-jar/enterprise-beans/cmp-resource/create-tables-at-deploy", //NOI18N
                        bundle.getString("LBL_Create_Table_At_Deploy")));   //NOI18N

                property = (String)drpTblAtUndplyComboBox.getSelectedItem();
                errors.addAll(validationSupport.validate(property,
                    "/sun-ejb-jar/enterprise-beans/cmp-resource/drop-tables-at-undeploy", //NOI18N
                        bundle.getString("LBL_Drop_Table_At_Undeploy")));   //NOI18N

                property = dbVndrNameTextField.getText();
                errors.addAll(validationSupport.validate(property,
                    "/sun-ejb-jar/enterprise-beans/cmp-resource/database-vendor-name",    //NOI18N
                        bundle.getString("LBL_Database_Vendor_Name")));         //NOI18N
            }
        }
        return errors;
    }


    public void tableChanged(javax.swing.event.TableModelEvent e) {
        notifyChange();
    }


    public String getHelpId() {
        return "AS_CFG_EjbJarRoot";                                     //NOI18N
    }

    private void setComponentValues() {
        //initialize all the elements
        nameTextField.setText(theBean.getName());
        uniqueIdTextField.setText(theBean.getUniqueId());
        uniqueIdTextField.setEditable(false);
        
        //initialize messageDestination webserviceDescription cmpresource 
        //pmdescriptors
        msgDstnPanel.setModel(theBean,
            theBean.getMessageDestination());

        websrvcDescPanel.setModel(theBean,
            theBean.getWebserviceDescription());
        
        setPmDescriptorsValues();

        setCmpResourceValues();
    }


    private void setPmDescriptorsValues(){
        PmDescriptors pmDescriptors = theBean.getPmDescriptors();
        if(pmDescriptors == null || pmDescriptors.sizePmDescriptor() == 0){
            pmDescptrPanel.setModel(theBean,null);
        }else{
            PmDescriptor[] pmDescptr = pmDescriptors.getPmDescriptor();
            pmDescptrPanel.setModel(theBean, pmDescptr);

            //Initialize pmInUse selection list
            if(pmDescptr != null){
                //initialize the combobox
                String pmIdentifier = null;
                for(int i=0; i<pmDescptr.length; i++){
                    pmInUseComboBox.addItem(pmDescptr[i].getPmIdentifier());
                }
                
                //make the selection
                PmInuse pmInuse = pmDescriptors.getPmInuse();
                if(pmInuse == null){
                    //select the first element in the list
                    pmInUseComboBox.setSelectedIndex(0);
                }else{
                    String identifier = pmInuse.getPmIdentifier();
                    pmInUseComboBox.setSelectedItem(identifier);
                }
            }
        }
    }

    
    private void setCmpResourceValues(){
        CmpResource cmpResource = theBean.getCmpResource();
        if(cmpResource == null){
            cmpPropertyPanel.setModel(theBean, null);
            schmaGnrtrPropPanel.setModel(theBean,null);
        }else{
            jndiNameField.setText(cmpResource.getJndiName());
            
            crteTblAtDplyComboBox.setSelectedItem(
                cmpResource.getCreateTablesAtDeploy());
            
            drpTblAtUndplyComboBox.setSelectedItem(
                cmpResource.getDropTablesAtUndeploy());
            
            dbVndrNameTextField.setText(cmpResource.getDatabaseVendorName());
            
            DefaultResourcePrincipal dfltResPrncpl = 
                cmpResource.getDefaultResourcePrincipal();
            if(dfltResPrncpl != null){
                dfltResrcPrncplNameTextField.setText(dfltResPrncpl.getName());
                dfltResrcPrncplPaswrdTextField.setText(
                    dfltResPrncpl.getPassword());
            }
            
            cmpPropertyPanel.setModel(theBean,
                cmpResource.getPropertyElement());

            SchemaGeneratorProperties schemaProps = 
                cmpResource.getSchemaGeneratorProperties();
            if(schemaProps != null){
                schmaGnrtrPropPanel.setModel(theBean,
                    schemaProps.getPropertyElement());
            }else{
                schmaGnrtrPropPanel.setModel(theBean,null);
            }
        }
    }

   
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panel1 = new java.awt.Panel();
        generalPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        uniqueIdLabel = new javax.swing.JLabel();
        uniqueIdTextField = new javax.swing.JTextField();
        pmInUsePanel = new javax.swing.JPanel();
        pmInUseLabel = new javax.swing.JLabel();
        pmInUseComboBox = new javax.swing.JComboBox();
        cmpResourcePanel = new javax.swing.JPanel();
        jndiNameField = new javax.swing.JTextField();
        jndiNameLabel = new javax.swing.JLabel();
        crteTblAtDplyLabel = new javax.swing.JLabel();
        drpTblAtUndplyLabel = new javax.swing.JLabel();
        dbVndrNameLabel = new javax.swing.JLabel();
        dbVndrNameTextField = new javax.swing.JTextField();
        crteTblAtDplyComboBox = new javax.swing.JComboBox();
        drpTblAtUndplyComboBox = new javax.swing.JComboBox();
        dfltResrcPrncplNameLabel = new javax.swing.JLabel();
        dfltResrcPrncplNameTextField = new javax.swing.JTextField();
        dfltResrcPrncplPaswrdLabel = new javax.swing.JLabel();
        dfltResrcPrncplPaswrdTextField = new javax.swing.JTextField();
        tabbedPanel = new javax.swing.JTabbedPane();
        cmpResourceLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setMinimumSize(new java.awt.Dimension(271, 169));
        setNextFocusableComponent(jndiNameField);
        setPreferredSize(new java.awt.Dimension(271, 169));
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });

        generalPanel.setLayout(new java.awt.GridBagLayout());

        generalPanel.setBorder(new javax.swing.border.EtchedBorder());
        generalPanel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                generalPanelFocusGained(evt);
            }
        });

        nameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Name").charAt(0));
        nameLabel.setLabelFor(nameTextField);
        nameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Name_1"));
        nameLabel.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameLabelFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        generalPanel.add(nameLabel, gridBagConstraints);
        nameLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Name_Acsbl_Name"));
        nameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Ejb_Module_Name_Acsbl_Desc"));

        nameTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Ejb_Module_Name_Tool_Tip"));
        nameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                nameFocusGained(evt);
            }
        });
        nameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameTextFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        generalPanel.add(nameTextField, gridBagConstraints);
        nameTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Name_Acsbl_Name"));
        nameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Ejb_Module_Name_Acsbl_Desc"));

        uniqueIdLabel.setLabelFor(uniqueIdTextField);
	  uniqueIdLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Unique_Id").charAt(0));
        uniqueIdLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Unique_ID"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        generalPanel.add(uniqueIdLabel, gridBagConstraints);
        uniqueIdLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Unique_ID_Acsbl_Name"));
        uniqueIdLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Unique_ID_Acsbl_Desc"));

        uniqueIdTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Unique_ID_Tool_Tip"));
        uniqueIdTextField.setFocusAccelerator(' ');
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        generalPanel.add(uniqueIdTextField, gridBagConstraints);
        uniqueIdTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Unique_ID_Acsbl_Name"));
        uniqueIdTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Unique_ID_Acsbl_Desc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(28, 5, 5, 5);
        add(generalPanel, gridBagConstraints);

        pmInUsePanel.setLayout(new java.awt.GridBagLayout());

        pmInUsePanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        pmInUseLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Pm_In_Use").charAt(0));
        pmInUseLabel.setLabelFor(pmInUseComboBox);
        pmInUseLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Pm_In_Use"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        pmInUsePanel.add(pmInUseLabel, gridBagConstraints);
        pmInUseLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pm_In_Use_Acsbl_Name"));
        pmInUseLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pm_In_Use_Acsbl_Desc"));

        pmInUseComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pm_In_Use_Tool_Tip"));
        pmInUseComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                pmInUseItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        pmInUsePanel.add(pmInUseComboBox, gridBagConstraints);
        pmInUseComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pm_In_Use_Acsbl_Name"));
        pmInUseComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Pm_In_Use_Acsbl_Desc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(pmInUsePanel, gridBagConstraints);

        cmpResourcePanel.setLayout(new java.awt.GridBagLayout());

        cmpResourcePanel.setBorder(new javax.swing.border.EtchedBorder());
        jndiNameField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cmp_Resource_Jndi_Name_Tool_Tip"));
        jndiNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jndiNameActionPerformed(evt);
            }
        });
        jndiNameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jndiNameFocusGained(evt);
            }
        });
        jndiNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jndiNameFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        cmpResourcePanel.add(jndiNameField, gridBagConstraints);
        jndiNameField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jndi_Name_Acsbl_Name"));
        jndiNameField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cmp_Resource_Jndi_Name_Acsbl_Desc"));

        jndiNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Jndi_Name").charAt(0));
        jndiNameLabel.setLabelFor(jndiNameField);
        jndiNameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Jndi_Name_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        cmpResourcePanel.add(jndiNameLabel, gridBagConstraints);
        jndiNameLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Jndi_Name_Acsbl_Name"));
        jndiNameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Cmp_Resource_Jndi_Name_Acsbl_Desc"));

        crteTblAtDplyLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Create_Table_At_Deploy").charAt(0));
        crteTblAtDplyLabel.setLabelFor(crteTblAtDplyComboBox);
        crteTblAtDplyLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Create_Table_At_Deploy_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 0);
        cmpResourcePanel.add(crteTblAtDplyLabel, gridBagConstraints);
        crteTblAtDplyLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Create_Table_At_Deploy_Acsbl_Name"));
        crteTblAtDplyLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Create_Table_At_Deploy_Acsbl_Desc"));

        drpTblAtUndplyLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Drop_Table_At_Undeploy").charAt(0));
        drpTblAtUndplyLabel.setLabelFor(drpTblAtUndplyComboBox);
        drpTblAtUndplyLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Drop_Table_At_Undeploy_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 5);
        cmpResourcePanel.add(drpTblAtUndplyLabel, gridBagConstraints);
        drpTblAtUndplyLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Drop_Table_At_Undeploy_Acsbl_Name"));
        drpTblAtUndplyLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Drop_Table_At_Undeploy_Acsbl_Desc"));

        dbVndrNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Database_Vendor_Name").charAt(0));
        dbVndrNameLabel.setLabelFor(dbVndrNameTextField);
        dbVndrNameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Database_Vendor_Name_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        cmpResourcePanel.add(dbVndrNameLabel, gridBagConstraints);
        dbVndrNameLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Database_Vendor_Name_Acsbl_Name"));
        dbVndrNameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Database_Vendor_Name_Acsbl_Desc"));

        dbVndrNameTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Database_Vendor_Name_Tool_Tip"));
        dbVndrNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbVndrNameActionPerformed(evt);
            }
        });
        dbVndrNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                dbVndrNameFocusGained(evt);
            }
        });
        dbVndrNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dbVndrNameTextFieldKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 5);
        cmpResourcePanel.add(dbVndrNameTextField, gridBagConstraints);
        dbVndrNameTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Database_Vendor_Name_Acsbl_Name"));
        dbVndrNameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Database_Vendor_Name_Acsbl_Desc"));

        crteTblAtDplyComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "true", "false" }));
        crteTblAtDplyComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Create_Table_At_Deploy_Tool_Tip"));
        crteTblAtDplyComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                crteTblAtDplyComboBoxItemStateChanged(evt);
            }
        });
        crteTblAtDplyComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                crteTblAtDplyComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 0);
        cmpResourcePanel.add(crteTblAtDplyComboBox, gridBagConstraints);
        crteTblAtDplyComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Create_Table_At_Deploy_Acsbl_Name"));
        crteTblAtDplyComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Create_Table_At_Deploy_Acsbl_Desc"));

        drpTblAtUndplyComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", "true", "false" }));
        drpTblAtUndplyComboBox.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Drop_Table_At_Undeploy_Tool_Tip"));
        drpTblAtUndplyComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                drpTblAtUndplyComboBoxItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 5);
        cmpResourcePanel.add(drpTblAtUndplyComboBox, gridBagConstraints);
        drpTblAtUndplyComboBox.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Drop_Table_At_Undeploy_Acsbl_Name"));
        drpTblAtUndplyComboBox.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Drop_Table_At_Undeploy_Acsbl_Desc"));

        dfltResrcPrncplNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Dflt_Res_Prncpl_Name").charAt(0));
        dfltResrcPrncplNameLabel.setLabelFor(dfltResrcPrncplNameTextField);
        dfltResrcPrncplNameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Default_Res_Prncpl_Name_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 5, 0, 5);
        cmpResourcePanel.add(dfltResrcPrncplNameLabel, gridBagConstraints);
        dfltResrcPrncplNameLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Name_Acsbl_Name"));
        dfltResrcPrncplNameLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Name_Acsbl_Desc"));

        dfltResrcPrncplNameTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Name_Tool_Tip"));
        dfltResrcPrncplNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dfltResrcPrncplNameActionPerformed(evt);
            }
        });
        dfltResrcPrncplNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                dfltResrcPrncplNameFocusGained(evt);
            }
        });
        dfltResrcPrncplNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dfltResrcPrncplNameKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 0, 5);
        cmpResourcePanel.add(dfltResrcPrncplNameTextField, gridBagConstraints);
        dfltResrcPrncplNameTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Name_Acsbl_Name"));
        dfltResrcPrncplNameTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Name_Acsbl_Desc"));

        dfltResrcPrncplPaswrdLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("MNC_Password").charAt(0));
        dfltResrcPrncplPaswrdLabel.setLabelFor(dfltResrcPrncplPaswrdTextField);
        dfltResrcPrncplPaswrdLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Default_Res_Prncpl_Password_1"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        cmpResourcePanel.add(dfltResrcPrncplPaswrdLabel, gridBagConstraints);
        dfltResrcPrncplPaswrdLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Password_Acsbl_Name"));
        dfltResrcPrncplPaswrdLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Password_Acsbl_Desc"));

        dfltResrcPrncplPaswrdTextField.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Password_Tool_Tip"));
        dfltResrcPrncplPaswrdTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dfltResrcPrncplPaswrdActionPerformed(evt);
            }
        });
        dfltResrcPrncplPaswrdTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                dfltResrcPrncplPaswrdFocusGained(evt);
            }
        });
        dfltResrcPrncplPaswrdTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dfltResrcPrncplPaswrdKeyReleased(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        cmpResourcePanel.add(dfltResrcPrncplPaswrdTextField, gridBagConstraints);
        dfltResrcPrncplPaswrdTextField.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Password_Acsbl_Name"));
        dfltResrcPrncplPaswrdTextField.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("Dflt_Res_Prncpl_Password_Acsbl_Desc"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(cmpResourcePanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        add(tabbedPanel, gridBagConstraints);

        cmpResourceLabel.setLabelFor(cmpResourcePanel);
        cmpResourceLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/share/configbean/customizers/ejbmodule/Bundle").getString("LBL_Cmp_Resource"));
        cmpResourceLabel.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 5, 0, 5);
        add(cmpResourceLabel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void crteTblAtDplyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_crteTblAtDplyComboBoxActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_crteTblAtDplyComboBoxActionPerformed

    private void dfltResrcPrncplPaswrdKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dfltResrcPrncplPaswrdKeyReleased
        // Add your handling code here:
        String item = dfltResrcPrncplPaswrdTextField.getText();
        DefaultResourcePrincipal dfltResrcPrncpl = getDefaultResourcePrincipal();
        if((EMPTY_STRING.equals(item)) || (null == item)){
            dfltResrcPrncpl.setPassword(null);
            updateDefltResrcPrncpl();
        }else{
            dfltResrcPrncpl.setPassword(item);
        }
        notifyChange();
        validateEntries();
    }//GEN-LAST:event_dfltResrcPrncplPaswrdKeyReleased

    private void dfltResrcPrncplPaswrdFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dfltResrcPrncplPaswrdFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_dfltResrcPrncplPaswrdFocusGained

    private void dfltResrcPrncplPaswrdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dfltResrcPrncplPaswrdActionPerformed
        // Add your handling code here:
        notifyChange();
        validateEntries();
    }//GEN-LAST:event_dfltResrcPrncplPaswrdActionPerformed

    private void dfltResrcPrncplNameKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dfltResrcPrncplNameKeyReleased
        // Add your handling code here:
        String item = dfltResrcPrncplNameTextField.getText();
        DefaultResourcePrincipal dfltResrcPrncpl = getDefaultResourcePrincipal();
        if((EMPTY_STRING.equals(item)) || (null == item)){
            dfltResrcPrncpl.setName(null);
            updateDefltResrcPrncpl();
        }else{
            dfltResrcPrncpl.setName(item);
        }
        notifyChange();
        validateEntries();
    }//GEN-LAST:event_dfltResrcPrncplNameKeyReleased

    private void dfltResrcPrncplNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dfltResrcPrncplNameFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_dfltResrcPrncplNameFocusGained

    private void dfltResrcPrncplNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dfltResrcPrncplNameActionPerformed
        // Add your handling code here:
        notifyChange();
        validateEntries();
    }//GEN-LAST:event_dfltResrcPrncplNameActionPerformed

    private void nameLabelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameLabelFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_nameLabelFocusGained

    private void generalPanelFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_generalPanelFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_generalPanelFocusGained

    private void dbVndrNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbVndrNameActionPerformed
        // Add your handling code here:
        notifyChange();
        validateEntries();
    }//GEN-LAST:event_dbVndrNameActionPerformed

    private void jndiNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jndiNameActionPerformed
        // Add your handling code here:
        notifyChange();
        validateEntries();        
    }//GEN-LAST:event_jndiNameActionPerformed

    private void dbVndrNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dbVndrNameFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_dbVndrNameFocusGained

    private void jndiNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jndiNameFocusGained
        // Add your handling code here:
        validateEntries();        
    }//GEN-LAST:event_jndiNameFocusGained

    private void nameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_nameFocusGained
        // Add your handling code here:
        validateEntries();        
    }//GEN-LAST:event_nameFocusGained

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        // Add your handling code here:
        validateEntries();
    }//GEN-LAST:event_formFocusGained

    private void crteTblAtDplyComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_crteTblAtDplyComboBoxItemStateChanged
        // Add your handling code here:
        String item = (String)crteTblAtDplyComboBox.getSelectedItem();
        CmpResource cmpResource = getCmpResource();
        if((EMPTY_STRING.equals(item)) || (null == item)){
            cmpResource.setCreateTablesAtDeploy(null);
            updateCmpResource();
        }else{
            cmpResource.setCreateTablesAtDeploy(item);
        }
        notifyChange();
        validateEntries();
    }//GEN-LAST:event_crteTblAtDplyComboBoxItemStateChanged

    private void drpTblAtUndplyComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_drpTblAtUndplyComboBoxItemStateChanged
        // Add your handling code here:
        String item = (String)drpTblAtUndplyComboBox.getSelectedItem();
        CmpResource cmpResource = getCmpResource();
        if((EMPTY_STRING.equals(item)) || (null == item)){
            cmpResource.setDropTablesAtUndeploy(null);
            updateCmpResource();
        }else{
            cmpResource.setDropTablesAtUndeploy(item);
        }
        notifyChange();
        validateEntries();
    }//GEN-LAST:event_drpTblAtUndplyComboBoxItemStateChanged

    private void dbVndrNameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dbVndrNameTextFieldKeyReleased
        // Add your handling code here:
        String item = dbVndrNameTextField.getText();
        CmpResource cmpResource = getCmpResource();
        if((EMPTY_STRING.equals(item)) || (null == item)){
            cmpResource.setDatabaseVendorName(null);
            updateCmpResource();
        }else{
            cmpResource.setDatabaseVendorName(item);
        }
        notifyChange();
        validateEntries();
    }//GEN-LAST:event_dbVndrNameTextFieldKeyReleased

    private void jndiNameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jndiNameFieldKeyReleased
        // Add your handling code here:
        String item = jndiNameField.getText();
        CmpResource cmpResource = getCmpResource();
        if((EMPTY_STRING.equals(item)) || (null == item)){
            cmpResource.setJndiName(null);
            updateCmpResource();
        }else{
            cmpResource.setJndiName(item);
        }
        notifyChange();
        validateEntries();
    }//GEN-LAST:event_jndiNameFieldKeyReleased

    private void nameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTextFieldKeyReleased
        // Add your handling code here:
        String item = nameTextField.getText();
        try{
            if(EMPTY_STRING.equals(item)){
                theBean.setName(null);
            }else{
                theBean.setName(item);
            }
        }catch(java.beans.PropertyVetoException exception){
        }
        notifyChange();
        validateEntries();
    }//GEN-LAST:event_nameTextFieldKeyReleased

    private void pmInUseItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_pmInUseItemStateChanged
        // Add your handling code here:
        String item = (String)pmInUseComboBox.getSelectedItem();
        PmInuse pmInuse = getPmInuse();
        PmDescriptors pmDescriptors = getPmDescriptors();
        PmDescriptor[] pmDescriptor = pmDescriptors.getPmDescriptor();
        if(pmDescriptor != null){
            for(int i=0; i<pmDescriptor.length; i++){
                if(item.equals(pmDescriptor[i].getPmIdentifier())){
                    pmInuse.setPmIdentifier(pmDescriptor[i].getPmIdentifier());
                    pmInuse.setPmVersion(pmDescriptor[i].getPmVersion());
                }
            }
        }
        notifyChange();
        validateEntries();
    }//GEN-LAST:event_pmInUseItemStateChanged


    private DefaultResourcePrincipal getDefaultResourcePrincipal(){
        CmpResource cmpResource = getCmpResource();
        DefaultResourcePrincipal defaultResourcePrincipal =
            cmpResource.getDefaultResourcePrincipal();
        if(defaultResourcePrincipal == null){
            defaultResourcePrincipal = StorageBeanFactory.getDefault().createDefaultResourcePrincipal();
            cmpResource.setDefaultResourcePrincipal(defaultResourcePrincipal);
        }
        return defaultResourcePrincipal;
    }


    private CmpResource getCmpResource(){
        CmpResource cmpResource = theBean.getCmpResource();
        if(cmpResource == null){
            cmpResource = StorageBeanFactory.getDefault().createCmpResource();
            try{
                theBean.setCmpResource(cmpResource);
            }catch(java.beans.PropertyVetoException exception){

            }
        }
        return cmpResource;
    }


    private PmInuse getPmInuse(){
        PmDescriptors pmDescriptors = getPmDescriptors();
        PmInuse pmInuse = pmDescriptors.getPmInuse();
        if(pmInuse == null){
            pmInuse = StorageBeanFactory.getDefault().createPmInuse();
            pmDescriptors.setPmInuse(pmInuse);
        }
        return pmInuse;
    }


    private PmDescriptors getPmDescriptors(){
        PmDescriptors pmDescriptors = theBean.getPmDescriptors();
        if(pmDescriptors == null){
           pmDescriptors = StorageBeanFactory.getDefault().createPmDescriptors();
            try{
                theBean.setPmDescriptors(pmDescriptors);
            }catch(java.beans.PropertyVetoException exception){

            }
        }
        return pmDescriptors;
    }


    private boolean isDfltResrcPrncplPresent(){
        boolean dfltResrcPrncplPresent = false;
        String property = dfltResrcPrncplNameTextField.getText();
        while(true){
            if((property != null) && (property.length() != 0)){
                dfltResrcPrncplPresent = true;
                break;
            }
            
            property = dfltResrcPrncplPaswrdTextField.getText();
            if((property != null) && (property.length() != 0)){
                dfltResrcPrncplPresent = true;
                break;
            }
            break;
        }
        return dfltResrcPrncplPresent;
    }


    private boolean isCmpResourcePresent(){
        boolean cmpResourcePresent = false;
        String property = jndiNameField.getText();
        while(true){
            if((property != null) && (property.length() != 0)){
                cmpResourcePresent = true;
                break;
            }

            if(isDfltResrcPrncplPresent()){
                cmpResourcePresent = true;
                break;
            }

            property = dbVndrNameTextField.getText();
            if((property != null) && (property.length() != 0)){
                cmpResourcePresent = true;
                break;
            }

            String item = (String)crteTblAtDplyComboBox.getSelectedItem();
            if( (item != null) && (!item.equals("")) ){
                cmpResourcePresent = true;
                break;
            }

            item = (String)drpTblAtUndplyComboBox.getSelectedItem();
            if( (item != null) && (!item.equals("")) ){
                cmpResourcePresent = true;
                break;
            }
            break;
        }
        return cmpResourcePresent;
    }


    private void updateDefltResrcPrncpl(){
        DefaultResourcePrincipal dfltResrcPrncpl = getDefaultResourcePrincipal();

        if(dfltResrcPrncpl.getName() != null) return;
        if(dfltResrcPrncpl.getPassword() != null) return;

        getCmpResource().setDefaultResourcePrincipal(null);
        updateCmpResource();
    }


    private void updateCmpResource(){
        CmpResource cmpResource = getCmpResource();

        if(cmpResource.getJndiName() != null) return;
        if(cmpResource.getCreateTablesAtDeploy() != null) return;
        if(cmpResource.getDropTablesAtUndeploy() != null) return;
        if(cmpResource.getDatabaseVendorName() != null) return;
        if(cmpResource.getDefaultResourcePrincipal() != null) return;
        if((cmpResource.getPropertyElement() != null) &&
                (cmpResource.getPropertyElement().length > 0)) return;
        if(cmpResource.getSchemaGeneratorProperties() != null){
            if((cmpResource.getSchemaGeneratorProperties().getPropertyElement() != null) &&
                    (cmpResource.getSchemaGeneratorProperties().getPropertyElement().length > 0)) return;
        }

        try{
            theBean.setCmpResource(null);
        }catch(java.beans.PropertyVetoException exception){

        }
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel cmpResourceLabel;
    private javax.swing.JPanel cmpResourcePanel;
    private javax.swing.JComboBox crteTblAtDplyComboBox;
    private javax.swing.JLabel crteTblAtDplyLabel;
    private javax.swing.JLabel dbVndrNameLabel;
    private javax.swing.JTextField dbVndrNameTextField;
    private javax.swing.JLabel dfltResrcPrncplNameLabel;
    private javax.swing.JTextField dfltResrcPrncplNameTextField;
    private javax.swing.JLabel dfltResrcPrncplPaswrdLabel;
    private javax.swing.JTextField dfltResrcPrncplPaswrdTextField;
    private javax.swing.JComboBox drpTblAtUndplyComboBox;
    private javax.swing.JLabel drpTblAtUndplyLabel;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JTextField jndiNameField;
    private javax.swing.JLabel jndiNameLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private java.awt.Panel panel1;
    private javax.swing.JComboBox pmInUseComboBox;
    private javax.swing.JLabel pmInUseLabel;
    private javax.swing.JPanel pmInUsePanel;
    protected javax.swing.JTabbedPane tabbedPanel;
    private javax.swing.JLabel uniqueIdLabel;
    private javax.swing.JTextField uniqueIdTextField;
    // End of variables declaration//GEN-END:variables
}
