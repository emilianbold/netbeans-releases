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
 * DefinitionsPanel.java
 *
 * Created on February 19, 2006, 8:33 AM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.websvc.customization.model.DefinitionsCustomization;
import org.netbeans.modules.websvc.customization.model.EnableAsyncMapping;
import org.netbeans.modules.websvc.customization.model.EnableMIMEContent;
import org.netbeans.modules.websvc.customization.model.EnableWrapperStyle;
import org.netbeans.modules.websvc.customization.model.JavaPackage;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.websvc.customization.model.CustomizationComponentFactory;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author  Roderico Cruz
 */
public class DefinitionsPanel extends SaveableSectionInnerPanel {
    private Definitions definitions;
    private WSDLModel model;
    private Node node;
    
    private boolean wsdlDirty;
    private boolean packageNameDirty;
    private DefinitionsActionListener listener;
    private DefaultItemListener defaultListener;
    
    /** Creates new form DefinitionsPanel */
    public DefinitionsPanel(SectionView view, Definitions definitions,
            Node node) {
        super(view);
        this.definitions = definitions;
        this.model = this.definitions.getModel();
        this.node = node;
        initComponents();
        if(!isClient(node)){
            enableAsyncMappingCB.setVisible(false);
        }
        this.enableAsyncMappingCB.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        this.enableMIMEContentCB.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        this.enableWrapperStyleCB.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        packageLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        packageNameText.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        defaultPackageCB.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        
        enableAsyncMappingCB.setToolTipText(NbBundle.getMessage(DefinitionsPanel.class, "TOOLTIP_ENABLE_ASYNC"));
        enableWrapperStyleCB.setToolTipText(NbBundle.getMessage(DefinitionsPanel.class, "TOOLTIP_ENABLE_WRAPPER"));
        enableMIMEContentCB.setToolTipText(NbBundle.getMessage(DefinitionsPanel.class, "TOOLTIP_ENABLE_MIME"));
        packageNameText.setToolTipText(NbBundle.getMessage(DefinitionsPanel.class, "TOOLTIP_PACKAGE"));
        wsdlDirty = false;
        packageNameDirty = false;
        setInitialPackage();
        sync();
        
        defaultListener = new DefaultItemListener();
        ItemListener itemListener = (ItemListener)WeakListeners.create(ItemListener.class, defaultListener,
                defaultPackageCB);
        defaultPackageCB.addItemListener(itemListener);
        
        addValidatee(packageNameText);
        
        listener = new DefinitionsActionListener();
        addModifier(packageNameText);
        addModifier(defaultPackageCB);
        
        ActionListener eamListener = (ActionListener)WeakListeners.create(ActionListener.class, listener, enableAsyncMappingCB);
        enableAsyncMappingCB.addActionListener(eamListener);
        ActionListener emcListener = (ActionListener)WeakListeners.create(ActionListener.class, listener, enableMIMEContentCB);
        enableMIMEContentCB.addActionListener(emcListener);
        ActionListener ewsListener = (ActionListener)WeakListeners.create(ActionListener.class, listener, enableWrapperStyleCB);
        enableWrapperStyleCB.addActionListener(ewsListener);
    }
    
    
    class DefinitionsActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            setValue((JComponent)e.getSource(), null);
        }
    }
    
    private void sync(){
        List <DefinitionsCustomization> ee =
                definitions.getExtensibilityElements(DefinitionsCustomization.class);
        if(ee.size() == 1){
            DefinitionsCustomization dc = ee.get(0);
            EnableAsyncMapping eam = dc.getEnableAsyncMapping();
            if(eam != null){
                setEnableAsyncMapping(eam.isEnabled());
            } else{ //default is false
                setEnableAsyncMapping(false);
            }
            
            EnableWrapperStyle ews = dc.getEnableWrapperStyle();
            if(ews != null){
                setEnableWrapperStyle(ews.isEnabled());
            } else{ //default is true
                setEnableWrapperStyle(true);
            }
            EnableMIMEContent emc = dc.getEnableMIMEContent();
            if(emc != null){
                setEnableMIMEContent(emc.isEnabled());
            } else{ //default is false
                setEnableMIMEContent(false);
            }
        } else{
            //no definitions bindings, set to defaults
            setEnableAsyncMapping(false);
            setEnableWrapperStyle(true);
            setEnableMIMEContent(false);
        }
    }
    
    private boolean useDefaultPackage(){
        return defaultPackageCB.isSelected();
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
    
    public void setEnableMIMEContent(boolean enable){
        enableMIMEContentCB.setSelected(enable);
    }
    
    public boolean getEnableMIMEContent(){
        return enableMIMEContentCB.isSelected();
    }
    
    public void setPackageName(String name){
        packageNameText.setText(name);
    }
    
    public String getPackageName(){
        return packageNameText.getText();
    }
    
    public JComponent getErrorComponent(String string) {
        return new JButton();
    }
    
    public void linkButtonPressed(Object object, String string) {
    }
    
    class DefaultItemListener implements ItemListener{
        public void itemStateChanged(ItemEvent e) {
            if(defaultPackageCB.isSelected()){
                packageNameText.setEnabled(false);
                packageNameText.setBackground(Color.LIGHT_GRAY);
            } else{
                packageNameText.setEnabled(true);
                packageNameText.setBackground(Color.WHITE);
                packageNameText.requestFocus();
            }
        }
    }
    
    public void setValue(JComponent jComponent, Object object) {
        List <DefinitionsCustomization> ee =
                definitions.getExtensibilityElements(DefinitionsCustomization.class);
        CustomizationComponentFactory factory = CustomizationComponentFactory.getDefault();
        if(jComponent == packageNameText || jComponent == defaultPackageCB){
            packageNameDirty = true;
        }
        //process Wrapper Style
        else if(jComponent == enableWrapperStyleCB){
            if(ee.size() == 1){ //there is an extensibility element
                DefinitionsCustomization dc = ee.get(0);
                EnableWrapperStyle ews = dc.getEnableWrapperStyle();
                if(ews == null){ //there is no EnableWrapperStyle, create one
                    try{
                        model.startTransaction();
                        ews = factory.createEnableWrapperStyle(model);
                        ews.setEnabled(this.getEnableWrapperStyle());
                        dc.setEnableWrapperStyle(ews);
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
                DefinitionsCustomization dc = factory.createDefinitionsCustomization(model);
                EnableWrapperStyle ews = factory.createEnableWrapperStyle(model);
                try{
                    model.startTransaction();
                    ews.setEnabled(this.getEnableWrapperStyle());
                    dc.setEnableWrapperStyle(ews);
                    definitions.addExtensibilityElement(dc);
                    wsdlDirty = true;
                } finally{
                    model.endTransaction();
                }
            }
        } else if(jComponent == enableAsyncMappingCB){  //process Async Mapping
            if(ee.size() == 1){ //there is an extensibility element
                DefinitionsCustomization dc = ee.get(0);
                EnableAsyncMapping eam = dc.getEnableAsyncMapping();
                if(eam == null){ //there is no EnableAsyncMapping, create one
                    try{
                        model.startTransaction();
                        eam = factory.createEnableAsyncMapping(model);
                        eam.setEnabled(this.getEnableAsyncMapping());
                        dc.setEnableAsyncMapping(eam);
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
                //async mapping element
                DefinitionsCustomization dc = factory.createDefinitionsCustomization(model);
                EnableAsyncMapping eam = factory.createEnableAsyncMapping(model);
                try{
                    model.startTransaction();
                    eam.setEnabled(this.getEnableAsyncMapping());
                    dc.setEnableAsyncMapping(eam);
                    definitions.addExtensibilityElement(dc);
                    wsdlDirty = true;
                } finally{
                    model.endTransaction();
                }
            }
        } else if(jComponent == enableMIMEContentCB){  //process MIME content
            if(ee.size() == 1){ //there is an extensibility element
                DefinitionsCustomization dc = ee.get(0);
                EnableMIMEContent emc = dc.getEnableMIMEContent();
                if(emc == null){ //there is no EnableMIMEContent, create one
                    try{
                        model.startTransaction();
                        emc = factory.createEnableMIMEContent(model);
                        emc.setEnabled(this.getEnableMIMEContent());
                        dc.setEnableMIMEContent(emc);
                        wsdlDirty = true;
                    } finally{
                        model.endTransaction();
                    }
                } else{ //there is an EnableMIMEContent, reset it
                    try{
                        model.startTransaction();
                        emc.setEnabled(this.getEnableMIMEContent());
                        wsdlDirty = true;
                    } finally{
                        model.endTransaction();
                    }
                }
            } else{  //there is no extensibility element, add a new one and add a new
                //MIME content element
                DefinitionsCustomization dc = factory.createDefinitionsCustomization(model);
                EnableMIMEContent emc = factory.createEnableMIMEContent(model);
                try{
                    model.startTransaction();
                    emc.setEnabled(this.getEnableMIMEContent());
                    dc.setEnableMIMEContent(emc);
                    definitions.addExtensibilityElement(dc);
                    
                    wsdlDirty = true;
                } finally{
                    model.endTransaction();
                }
            }
        }
    }
    
    public void documentChanged(JTextComponent comp, String val) {
        if(comp == packageNameText){
            if(!JavaUtilities.isValidPackageName(val)){
                getSectionView().getErrorPanel().
                        setError(new Error(Error.TYPE_FATAL,
                        Error.ERROR_MESSAGE, val, comp));
                return;
            }
        }
        getSectionView().getErrorPanel().clearError();
    }
    
    public void rollbackValue(JTextComponent source) {
        if(source == packageNameText){
            String pkg = "";
            Client c = (Client)node.getLookup().lookup(Client.class);
            if(c != null){
                pkg = c.getPackageName();
            } else{
                Service s = (Service)node.getLookup().lookup(Service.class);
                if(s != null){
                    pkg = s.getPackageName();
                }
            }
            packageNameText.setText(pkg);
        }
    }
    
    private void setInitialPackage(){
        Client c = (Client)node.getLookup().lookup(Client.class);
        if(c != null){
            if(c.isPackageNameForceReplace()){
                packageNameText.setText(c.getPackageName());
                defaultPackageCB.setSelected(false);
            } else{
                packageNameText.setEnabled(false);
                packageNameText.setBackground(Color.LIGHT_GRAY);
                defaultPackageCB.setSelected(true);
            }
        } else{
            Service s = (Service)node.getLookup().lookup(Service.class);
            if(s != null){
                if(s.isPackageNameForceReplace()){
                    packageNameText.setText(s.getPackageName());
                    defaultPackageCB.setSelected(false);
                } else{
                    packageNameText.setEnabled(false);
                    packageNameText.setBackground(Color.LIGHT_GRAY);
                    defaultPackageCB.setSelected(true);
                }
            }
        }
    }
    
    public boolean jaxwsIsDirty(){
        return packageNameDirty;
    }
    
    public boolean wsdlIsDirty() {
        return wsdlDirty;
    }
    
    public void save() {
        if(wsdlDirty){
            this.setModelDirty(model);
        }
        
        if(packageNameDirty){
            Client client = (Client)node.getLookup().lookup(Client.class);
            Service service = (Service)node.getLookup().lookup(Service.class);
            String packageName = getPackageName();
            if(useDefaultPackage() || packageName == null ||
                    packageName.trim().equals("")){
                if(client != null){
                    client.setPackageNameForceReplace(false);
                }else{
                    service.setPackageNameForceReplace(false);
                }
            }else{
                if(client != null){
                    client.setPackageName(packageName);
                    client.setPackageNameForceReplace(true);
                } else{
                    service.setPackageName(packageName);
                    service.setPackageNameForceReplace(true);
                }
            }
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
        emcButtonGroup = new javax.swing.ButtonGroup();
        packageLabel = new javax.swing.JLabel();
        packageNameText = new javax.swing.JTextField();
        enableWrapperStyleCB = new javax.swing.JCheckBox();
        enableAsyncMappingCB = new javax.swing.JCheckBox();
        enableMIMEContentCB = new javax.swing.JCheckBox();
        defaultPackageCB = new javax.swing.JCheckBox();

        packageLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_PACKAGE_NAME"));
        packageLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_PACKAGE_NAME"));

        enableWrapperStyleCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_WRAPPER_STYLE").charAt(0));
        enableWrapperStyleCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_WRAPPER_STYLE"));
        enableWrapperStyleCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableWrapperStyleCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        enableWrapperStyleCB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_WRAPPER_STYLE"));
        enableWrapperStyleCB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_WRAPPER_STYLE"));

        enableAsyncMappingCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_ASYNC_CLIENT").charAt(0));
        enableAsyncMappingCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_ASYNC_MAPPING"));
        enableAsyncMappingCB.setActionCommand(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_ASYNC_MAPPING"));
        enableAsyncMappingCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableAsyncMappingCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        enableAsyncMappingCB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_ASYNC_MAPPING"));
        enableAsyncMappingCB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_ASYNC_MAPPING"));

        enableMIMEContentCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_MIME_CONTENT").charAt(0));
        enableMIMEContentCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_MIME_CONTENT"));
        enableMIMEContentCB.setActionCommand(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_MIME_CONTENT"));
        enableMIMEContentCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableMIMEContentCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        enableMIMEContentCB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_MIME_CONTENT"));
        enableMIMEContentCB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENABLE_MIME_CONTENT"));

        defaultPackageCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_USE_DEFAULT").charAt(0));
        defaultPackageCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));
        defaultPackageCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultPackageCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        defaultPackageCB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));
        defaultPackageCB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(packageLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(packageNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 173, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(26, 26, 26)
                        .add(defaultPackageCB))
                    .add(enableWrapperStyleCB)
                    .add(enableMIMEContentCB)
                    .add(enableAsyncMappingCB))
                .addContainerGap(80, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(packageLabel)
                    .add(packageNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(defaultPackageCB))
                .add(20, 20, 20)
                .add(enableWrapperStyleCB)
                .add(19, 19, 19)
                .add(enableMIMEContentCB)
                .add(19, 19, 19)
                .add(enableAsyncMappingCB)
                .addContainerGap(23, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox defaultPackageCB;
    private javax.swing.ButtonGroup eamButtonGroup;
    private javax.swing.ButtonGroup emcButtonGroup;
    private javax.swing.JCheckBox enableAsyncMappingCB;
    private javax.swing.JCheckBox enableMIMEContentCB;
    private javax.swing.JCheckBox enableWrapperStyleCB;
    private javax.swing.ButtonGroup ewsButtonGroup;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JTextField packageNameText;
    // End of variables declaration//GEN-END:variables
    
}
