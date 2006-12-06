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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * PortTypeOperationPanel.java
 *
 * Created on February 17, 2006, 4:15 PM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.websvc.core.jaxws.JaxWsUtils;
import org.netbeans.modules.websvc.customization.model.CustomizationComponentFactory;
import org.netbeans.modules.websvc.customization.model.DefinitionsCustomization;
import org.netbeans.modules.websvc.customization.model.EnableAsyncMapping;
import org.netbeans.modules.websvc.customization.model.EnableWrapperStyle;
import org.netbeans.modules.websvc.customization.model.JavaMethod;
import org.netbeans.modules.websvc.customization.model.PortTypeCustomization;
import org.netbeans.modules.websvc.customization.model.PortTypeOperationCustomization;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.WeakListeners;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author  Roderico Cruz
 */
public class PortTypeOperationPanel extends SaveableSectionInnerPanel{
    private Operation operation;
    private WSDLModel model;
    private boolean wsdlDirty;
    private DefaultItemListener dil;
    private ModelChangeListener modelListener;
    private ModelChangeListener primaryModelListener;
    private Definitions primaryDefinitions;
    private WSDLModel primaryModel;
    
    /** Creates new form PortTypeOperationPanel */
    public PortTypeOperationPanel(SectionView view, Operation operation,
            Node node, Definitions primaryDefinitions) {
        super(view);
        this.operation = operation;
        this.primaryDefinitions = primaryDefinitions;
        this.model = this.operation.getModel();
        this.primaryModel = this.primaryDefinitions.getModel();
        
        initComponents();
        if(!isClient(node)){
            enableAsyncMappingCB.setVisible(false);
        }
        portTypeLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        portTypeName.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        portTypeName.setText(getParentOfOperation(operation));
        this.enableAsyncMappingCB.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        this.enableWrapperStyleCB.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        javaMethodName.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        enableAsyncMappingCB.setToolTipText(NbBundle.getMessage(DefinitionsPanel.class, "TOOLTIP_ENABLE_ASYNC"));
        enableWrapperStyleCB.setToolTipText(NbBundle.getMessage(DefinitionsPanel.class, "TOOLTIP_ENABLE_WRAPPER"));
        syncButtons();
        syncJavaMethod();
        
        dil = new DefaultItemListener();
        ItemListener il = (ItemListener)WeakListeners.create(ItemListener.class, dil, defaultMethodCB);
        defaultMethodCB.addItemListener(il);
        
        modelListener = new ModelChangeListener();
        PropertyChangeListener pcl = WeakListeners.propertyChange(modelListener, model);
        model.addPropertyChangeListener(pcl);
        
        if(model != primaryModel){
            primaryModelListener = new ModelChangeListener();
            PropertyChangeListener l = WeakListeners.propertyChange(primaryModelListener, primaryModel);
            primaryModel.addPropertyChangeListener(l);
        }
 
        addModifier(javaMethodName);
        addModifier(defaultMethodCB);
        addModifier(enableAsyncMappingCB);
        addModifier(enableWrapperStyleCB);
        addValidatee(javaMethodName);
    }
    
    private String getParentOfOperation(Operation operation){
        PortType portType = (PortType)operation.getParent();
        return portType.getName();
    }
    
    class DefaultItemListener implements ItemListener{
        public void itemStateChanged(ItemEvent e) {
            if(defaultMethodCB.isSelected()){
                javaMethodName.setBackground(Color.LIGHT_GRAY);
                javaMethodName.setEnabled(false);
            } else{
                javaMethodName.setEnabled(true);
                javaMethodName.setBackground(Color.WHITE);
                javaMethodName.requestFocus();
            }
        }
    }
    
    class ModelChangeListener implements PropertyChangeListener{
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if (source instanceof EnableWrapperStyle){
                EnableWrapperStyle ews = (EnableWrapperStyle)source;
                WSDLComponent parent = ews.getParent();
                if(parent instanceof DefinitionsCustomization ||
                        parent instanceof PortTypeCustomization){
                    syncButtons();
                }
            } else if (source instanceof EnableAsyncMapping){
                EnableAsyncMapping eam = (EnableAsyncMapping)source;
                WSDLComponent parent = eam.getParent();
                if(parent instanceof DefinitionsCustomization ||
                        parent instanceof PortTypeCustomization){
                    syncButtons();
                }
            }
        }
    }
    
    private void syncJavaMethod(){
        List<PortTypeOperationCustomization> ee =
                operation.getExtensibilityElements(PortTypeOperationCustomization.class);
        if(ee.size() == 1){
            PortTypeOperationCustomization ptoc = ee.get(0);
            JavaMethod jm = ptoc.getJavaMethod();
            if(jm != null){
                setJavaMethod(jm.getName());
            } else{
                defaultMethodCB.setSelected(true);
                javaMethodName.setBackground(Color.LIGHT_GRAY);
                javaMethodName.setEnabled(false);
            }
        } else{
            defaultMethodCB.setSelected(true);
            javaMethodName.setBackground(Color.LIGHT_GRAY);
            javaMethodName.setEnabled(false);
        }
    }
    
    final public void syncButtons(){
        List<PortTypeOperationCustomization> ee =
                operation.getExtensibilityElements(PortTypeOperationCustomization.class);
        if(ee.size() == 1){
            PortTypeOperationCustomization ptoc = ee.get(0);
            EnableAsyncMapping eam = ptoc.getEnableAsyncMapping();
            if(eam != null){
                setEnableAsyncMapping(eam.isEnabled());
            } else{
                setEnableAsyncMapping(getAsyncMappingOfParent());
            }
            EnableWrapperStyle ews = ptoc.getEnableWrapperStyle();
            if(ews != null){
                setEnableWrapperStyle(ews.isEnabled());
            } else{
                setEnableWrapperStyle(getWrapperStyleOfParent());
            }
            
        } else{
            setEnableAsyncMapping(getAsyncMappingOfParent());
            setEnableWrapperStyle(getWrapperStyleOfParent());
            defaultMethodCB.setSelected(true);
            javaMethodName.setBackground(Color.LIGHT_GRAY);
            javaMethodName.setEnabled(false);
        }
    }
    
    private boolean getAsyncMappingOfParent(){
        boolean isAsyncMapping = false;
        PortType portType = (PortType)operation.getParent();
        List<PortTypeCustomization> ptcs = portType.getExtensibilityElements(PortTypeCustomization.class);
        if(ptcs.size() > 0) {  //there is a PortTypeCustomization
            PortTypeCustomization ptc = ptcs.get(0);
            EnableAsyncMapping asyncMapping = ptc.getEnableAsyncMapping();
            if(asyncMapping != null){ //there is an async mapping
                isAsyncMapping =  asyncMapping.isEnabled();
            }else{
                isAsyncMapping = getAsyncMappingFromDefinitions(primaryDefinitions);
            }
        } else{ //there is no PortTypeCustomization, look in Definitions
            isAsyncMapping = getAsyncMappingFromDefinitions(primaryDefinitions);
        }
        return isAsyncMapping;
    }
    
    private boolean getAsyncMappingFromDefinitions(Definitions definitions){
        List<DefinitionsCustomization> dcs = definitions.getExtensibilityElements(DefinitionsCustomization.class);
        if(dcs.size() > 0){
            DefinitionsCustomization dc = dcs.get(0);
            EnableAsyncMapping asyncMapping = dc.getEnableAsyncMapping();
            if(asyncMapping != null){
                return asyncMapping.isEnabled();
            }
        }
        return false;
    }
    
    private boolean getWrapperStyleFromDefinitions(Definitions definitions){
        List<DefinitionsCustomization> dcs = definitions.getExtensibilityElements(DefinitionsCustomization.class);
        if(dcs.size() > 0){
            DefinitionsCustomization dc = dcs.get(0);
            EnableWrapperStyle wrapperStyle = dc.getEnableWrapperStyle();
            if(wrapperStyle != null){
                return wrapperStyle.isEnabled();
            }
        }
        return true;
    }
    
    private boolean getWrapperStyleOfParent(){
        boolean isWrapperStyle = true;
        PortType portType = (PortType)operation.getParent();
        List<PortTypeCustomization> ptcs = portType.getExtensibilityElements(PortTypeCustomization.class);
        if(ptcs.size() > 0) {  //there is a PortTypeCustomization
            PortTypeCustomization ptc = ptcs.get(0);
            EnableWrapperStyle wrapperStyle = ptc.getEnableWrapperStyle();
            if(wrapperStyle != null){ //there is a wrapper style
                isWrapperStyle =  wrapperStyle.isEnabled();
            }else{
                isWrapperStyle = getWrapperStyleFromDefinitions(primaryDefinitions);
            }
        } else{ //there is no PortTypeCustomization, look in Definitions
            isWrapperStyle = getWrapperStyleFromDefinitions(primaryDefinitions);
        }
        return isWrapperStyle;
    }
    
    public void setEnableAsyncMapping(boolean enable){
        enableAsyncMappingCB.setSelected(enable);
    }
    
    public boolean getEnableAsyncMapping(){
        return enableAsyncMappingCB.isSelected();
    }
    
    public void setEnableWrapperStyle(boolean enable){
        enableWrapperStyleCB.setSelected(enable);
    }
    
    public boolean getEnableWrapperStyle(){
        return enableWrapperStyleCB.isSelected();
    }
    
    
    public void setJavaMethod(String method){
        javaMethodName.setText(method);
    }
    
    public JComponent getErrorComponent(String string) {
        return new JButton("error");
    }
    
    public void linkButtonPressed(Object object, String string) {
    }
    
    public void setValue(JComponent jComponent, Object object) {
        List <PortTypeOperationCustomization> ee =
                operation.getExtensibilityElements(PortTypeOperationCustomization.class);
        CustomizationComponentFactory factory = CustomizationComponentFactory.getDefault();
        if(jComponent == javaMethodName ||
                jComponent == defaultMethodCB){
            String text = javaMethodName.getText();
            if(text != null && !text.trim().equals("")
            && !defaultMethodCB.isSelected()){
                 if(!JaxWsUtils.isJavaIdentifier(text)){
                     return;
                 }
                if(ee.size() > 0){  //there is existing extensibility element
                    PortTypeOperationCustomization ptoc = ee.get(0);
                    JavaMethod jm = ptoc.getJavaMethod();
                    if(jm == null){  //there is no JavaMethod, create one
                        try{
                            jm = factory.createJavaMethod(model);
                            model.startTransaction();
                            jm.setName(text); //TODO Need to validate this before setting it
                            ptoc.setJavaMethod(jm);
                            wsdlDirty = true;
                        }finally{
                                model.endTransaction();
                        }
                    } else{ //javamethod already exists
                        //reset the JavaMethod
                        try{
                            model.startTransaction();
                            jm.setName(text);
                            
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                        }
                    }
                }else{  //there is no ExtensibilityElement
                    //create extensibility element and add JavaMethod
                    PortTypeOperationCustomization ptoc = factory.createPortTypeOperationCustomization(model);
                    JavaMethod jm = factory.createJavaMethod(model);
                    try{
                        model.startTransaction();
                        jm.setName(text);
                        ptoc.setJavaMethod(jm);
                        operation.addExtensibilityElement(ptoc);
                        wsdlDirty = true;
                    } finally{
                            model.endTransaction();
                    }
                }
            } else{ //no javamethod is specified
                if(ee.size() == 1){
                    try{
                        PortTypeOperationCustomization ptoc = ee.get(0);
                        JavaMethod jm = ptoc.getJavaMethod();
                        if(jm != null){
                            model.startTransaction();
                            ptoc.removeJavaMethod(jm);
                            //if(ptoc has no more children, remove it as well)
                            if(ptoc.getChildren().size() == 0){
                                operation.removeExtensibilityElement(ptoc);
                            }
                            wsdlDirty = true;
                        }
                    } finally{
                            model.endTransaction();
                    }
                }
            }
        } else if(jComponent == enableWrapperStyleCB){
            if(ee.size() > 0){ //there is an extensibility element
                PortTypeOperationCustomization poc = ee.get(0);
                EnableWrapperStyle ews = poc.getEnableWrapperStyle();
                if(ews == null){ //there is no EnableWrapperStyle, create one
                    try{
                        model.startTransaction();
                        ews = factory.createEnableWrapperStyle(model);
                        ews.setEnabled(this.getEnableWrapperStyle());
                        poc.setEnableWrapperStyle(ews);
                        wsdlDirty = true;
                    } finally{
                            model.endTransaction();
                    }
                } else{ //there is an EnableWrapperStyle, reset it
                    try{
                        model.startTransaction();
                        ews.setEnabled(this.getEnableWrapperStyle());
                        wsdlDirty = true;
                    } finally{
                            model.endTransaction();
                    }
                }
            } else{  //there is no extensibility element, add a new one and add a new
                //wrapper style element
                PortTypeOperationCustomization poc = factory.createPortTypeOperationCustomization(model);
                EnableWrapperStyle ews = factory.createEnableWrapperStyle(model);
                try{
                    model.startTransaction();
                    ews.setEnabled(this.getEnableWrapperStyle());
                    poc.setEnableWrapperStyle(ews);
                    operation.addExtensibilityElement(poc);
                    wsdlDirty = true;
                } finally{
                        model.endTransaction();
                }
            }
        } else if(jComponent == this.enableAsyncMappingCB){
            if(ee.size() > 0){ //there is an extensibility element
                PortTypeOperationCustomization poc = ee.get(0);
                EnableAsyncMapping eam = poc.getEnableAsyncMapping();
                if(eam == null){ //there is no EnableAsyncMapping, create one
                    try{
                        model.startTransaction();
                        eam = factory.createEnableAsyncMapping(model);
                        eam.setEnabled(this.getEnableAsyncMapping());
                        poc.setEnableAsyncMapping(eam);
                        wsdlDirty = true;
                    } finally{
                            model.endTransaction();
                    }
                } else{ //there is an EnableAsyncMapping, reset it
                    try{
                        model.startTransaction();
                        eam.setEnabled(this.getEnableAsyncMapping());
                        wsdlDirty = true;
                    } finally{
                            model.endTransaction();
                    }
                }
            } else{  //there is no extensibility element, add a new one and add a new
                //enable asyncmapping element
                PortTypeOperationCustomization poc =  factory.createPortTypeOperationCustomization(model);
                EnableAsyncMapping eam = factory.createEnableAsyncMapping(model);
                try{
                    model.startTransaction();
                    eam.setEnabled(this.getEnableAsyncMapping());
                    poc.setEnableAsyncMapping(eam);
                    operation.addExtensibilityElement(poc);
                    wsdlDirty = true;
                } finally{
                        model.endTransaction();
                }
            }
        }
    }
    
    public void documentChanged(JTextComponent comp, String val) {
        if(comp == javaMethodName){
            if(!JaxWsUtils.isJavaIdentifier(val)){
                getSectionView().getErrorPanel().
                        setError(new Error(Error.TYPE_FATAL,
                        Error.ERROR_MESSAGE, val, comp));
                return;
            }
        }
        getSectionView().getErrorPanel().clearError();
    }
    
    public void rollbackValue(JTextComponent source) {
        if(source == javaMethodName){
            String methodName = "";
            List <PortTypeOperationCustomization> ee =
                    operation.getExtensibilityElements(PortTypeOperationCustomization.class);
            if(ee.size() == 1){
                PortTypeOperationCustomization ptoc = ee.get(0);
                JavaMethod jm = ptoc.getJavaMethod();
                if(jm != null){
                    methodName = jm.getName();
                }
            }
            javaMethodName.setText(methodName);
        }
    }
    
    public boolean wsdlIsDirty() {
       
        return wsdlDirty;
    }
    
    public void save() {
         if(wsdlDirty){
           this.setModelDirty(model);
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        ewsButtonGroup = new javax.swing.ButtonGroup();
        eamButtonGroup = new javax.swing.ButtonGroup();
        jLabel4 = new javax.swing.JLabel();
        javaMethodName = new javax.swing.JTextField();
        defaultMethodCB = new javax.swing.JCheckBox();
        enableWrapperStyleCB = new javax.swing.JCheckBox();
        enableAsyncMappingCB = new javax.swing.JCheckBox();
        portTypeLabel = new javax.swing.JLabel();
        portTypeName = new javax.swing.JLabel();

        jLabel4.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_JAVA_METHOD_NAME"));
        jLabel4.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_JAVA_METHOD_NAME"));

        javaMethodName.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("TOOLTIP_PORTTYPE_METHOD"));
        javaMethodName.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        javaMethodName.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_JAVA_METHOD_NAME"));
        javaMethodName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_JAVA_METHOD_NAME"));

        defaultMethodCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_USE_DEFAULT").charAt(0));
        defaultMethodCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));
        defaultMethodCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultMethodCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        defaultMethodCB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));
        defaultMethodCB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));

        enableWrapperStyleCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_WRAPPER_STYLE").charAt(0));
        enableWrapperStyleCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_WRAPPER_STYLE"));
        enableWrapperStyleCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableWrapperStyleCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        enableWrapperStyleCB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_WRAPPER_STYLE"));
        enableWrapperStyleCB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_WRAPPER_STYLE"));

        enableAsyncMappingCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_ASYNC_CLIENT").charAt(0));
        enableAsyncMappingCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_ASYNC_MAPPING"));
        enableAsyncMappingCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableAsyncMappingCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        enableAsyncMappingCB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_ASYNC_MAPPING"));
        enableAsyncMappingCB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_ASYNC_MAPPING"));

        portTypeLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENCLOSING_PORTTYPE"));
        portTypeLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENCLOSING_PORTTYPE"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(22, 22, 22)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(enableAsyncMappingCB)
                    .add(enableWrapperStyleCB)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(portTypeLabel)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(javaMethodName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 132, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(14, 14, 14)
                                .add(defaultMethodCB))
                            .add(layout.createSequentialGroup()
                                .add(portTypeName)
                                .add(106, 106, 106)))))
                .addContainerGap(103, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(9, 9, 9)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(portTypeLabel)
                    .add(portTypeName))
                .add(15, 15, 15)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(javaMethodName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(defaultMethodCB))
                .add(15, 15, 15)
                .add(enableWrapperStyleCB)
                .add(12, 12, 12)
                .add(enableAsyncMappingCB)
                .addContainerGap(24, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox defaultMethodCB;
    private javax.swing.ButtonGroup eamButtonGroup;
    private javax.swing.JCheckBox enableAsyncMappingCB;
    private javax.swing.JCheckBox enableWrapperStyleCB;
    private javax.swing.ButtonGroup ewsButtonGroup;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JTextField javaMethodName;
    private javax.swing.JLabel portTypeLabel;
    private javax.swing.JLabel portTypeName;
    // End of variables declaration//GEN-END:variables
    
}
