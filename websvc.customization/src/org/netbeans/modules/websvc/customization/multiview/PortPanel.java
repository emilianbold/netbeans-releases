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
 * PortPanel.java
 *
 * Created on February 19, 2006, 8:58 AM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.core.jaxws.JaxWsUtils;
import org.netbeans.modules.websvc.customization.model.JavaMethod;
import org.netbeans.modules.websvc.customization.model.PortCustomization;
import org.netbeans.modules.websvc.customization.model.Provider;
import org.netbeans.modules.websvc.customization.model.impl.JAXWSQName;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.netbeans.modules.xml.multiview.Error;

/**
 *
 * @author  Roderico Cruz
 */
public class PortPanel extends SaveableSectionInnerPanel {
    private Port port;
    private WSDLModel model;
    private boolean wsdlDirty;
    private DefaultItemListener defaultListener;
    private ProviderActionListener providerActionListener;
    private Node node;
    
    /** Creates new form PortPanel */
    public PortPanel(SectionView view, Port port,
            Node node) {
        super(view);
        this.port = port;
        this.model = this.port.getModel();
        this.node = node;
        initComponents();
        serviceLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        serviceName.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        serviceName.setText(getParentOfPort(port));
        defaultMethodCB.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        portAccessMethodText.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        portAccessLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        
        sync();
        
        defaultListener = new DefaultItemListener();
        ItemListener itemListener = (ItemListener)WeakListeners.create(ItemListener.class, defaultListener,
                defaultMethodCB);
        defaultMethodCB.addItemListener(itemListener);
        
        if(!isClient()){
            providerCB.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
            providerActionListener = new ProviderActionListener();
            ActionListener providerListener = (ActionListener)WeakListeners.create(ActionListener.class,
                    providerActionListener, providerCB);
            providerCB.addActionListener(providerListener);
        } else{
            providerCB.setVisible(false);
        }
        
        addModifier(portAccessMethodText);
        addModifier(defaultMethodCB);
        addValidatee(portAccessMethodText);
    }
    
    class DefaultItemListener implements ItemListener{
        public void itemStateChanged(ItemEvent e) {
            if(defaultMethodCB.isSelected()){
                portAccessMethodText.setEnabled(false);
                portAccessMethodText.setBackground(Color.LIGHT_GRAY);
            } else{
                portAccessMethodText.setEnabled(true);
                portAccessMethodText.setBackground(Color.WHITE);
                portAccessMethodText.requestFocus();
            }
        }
    }
    
    class ProviderActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == providerCB){
                if(providerCB.isSelected()){
                    NotifyDescriptor.Confirmation notifyDesc =
                            new NotifyDescriptor.Confirmation(NbBundle.getMessage
                            (ExternalBindingTablePanel.class, "WARN_PROVIDER_INTERFACE"),
                            NotifyDescriptor.YES_NO_OPTION);
                    DialogDisplayer.getDefault().notify(notifyDesc);
                    if((notifyDesc.getValue() == NotifyDescriptor.NO_OPTION)){
                        providerCB.setSelected(false);
                        return;
                    }
                }
                setValue(providerCB, null);
            }
        }
    }
    
    private boolean isClient(){
        Client client = (Client)node.getLookup().lookup(Client.class);
        return client != null;
    }
    
    private String getParentOfPort(Port port){
        Service service = (Service)port.getParent();
        return service.getName();
    }
    private void sync(){
        List<PortCustomization> ee = port.getExtensibilityElements(PortCustomization.class);
        if(ee.size() == 1){
            PortCustomization pc = ee.get(0);
            JavaMethod jm = pc.getJavaMethod();
            if(jm != null){
                setPortAccessMethod(jm.getName());
            } else{
                defaultMethodCB.setSelected(true);
                portAccessMethodText.setEnabled(false);
                portAccessMethodText.setBackground(Color.LIGHT_GRAY);
            }
            Provider provider = pc.getProvider();
            if(provider != null){
                if(provider.isEnabled()){
                    providerCB.setSelected(true);
                }else{
                    providerCB.setSelected(false);
                }
            } else{
                providerCB.setSelected(false);
            }
        } else{
            providerCB.setSelected(false);
            defaultMethodCB.setSelected(true);
            portAccessMethodText.setEnabled(false);
            portAccessMethodText.setBackground(Color.LIGHT_GRAY);
        }
    }
    
    public void setPortAccessMethod(String name){
        portAccessMethodText.setText(name);
    }
    
    public String getPortAccessMethod(){
        return portAccessMethodText.getText();
    }
    
    public void setProvider(boolean enable){
        providerCB.setSelected(enable);
    }
    
    public boolean isProvider(){
        return providerCB.isSelected();
    }
    
    public JComponent getErrorComponent(String string) {
        return new javax.swing.JButton("error");
    }
    
    public void linkButtonPressed(Object object, String string) {
    }
    
    public void setValue(JComponent jComponent, Object object) {
        List <PortCustomization> ee =
                port.getExtensibilityElements(PortCustomization.class);
        if(jComponent == providerCB){
            if(ee.size() == 1){ //there is a PortCustomization element
                PortCustomization pc = ee.get(0);
                Provider provider = pc.getProvider();
                if(isProvider()){ //provider is selected
                    if(provider == null){ //there is no provider
                        try{
                            provider = (Provider) model.getFactory().create(pc, JAXWSQName.PROVIDER.getQName());
                            model.startTransaction();
                            provider.setEnabled(true);
                            pc.setProvider(provider);
                            wsdlDirty = true;
                        } finally{
                            model.endTransaction();
                        }
                    }
                } else{ //provider is not selected, remove the Provider element
                    if(provider != null){
                        try{
                            model.startTransaction();
                            pc.removeProvider(provider);
                            //if there are no more children, remove PortCustomization
                            if(pc.getChildren().size() == 0){
                                port.removeExtensibilityElement(pc);
                            }
                            wsdlDirty = true;
                        } finally{
                            model.endTransaction();
                        }
                    }
                }
            } else{  //no port customization
                //if provider is set, create extensibility element and add Provider
                if(isProvider()){
                    WSDLComponentFactory factory = model.getFactory();
                    PortCustomization pc = (PortCustomization) factory.create(port,
                            JAXWSQName.BINDINGS.getQName());
                    Provider provider = (Provider) factory.create(pc, JAXWSQName.PROVIDER.getQName());
                    try{
                        model.startTransaction();
                        provider.setEnabled(true);
                        pc.setProvider(provider);
                        port.addExtensibilityElement(pc);
                        wsdlDirty = true;
                    } finally{
                        model.endTransaction();
                    }
                } 
            }
        } else if(jComponent == portAccessMethodText
                || jComponent == defaultMethodCB ){
            String text = portAccessMethodText.getText();
            if(text != null && !text.trim().equals("")
            && !defaultMethodCB.isSelected()){ //Java method was specified
                if(!JaxWsUtils.isJavaIdentifier(text)){
                    return;
                }
                if(ee.size() == 1){  //there is existing extensibility element
                    PortCustomization pc = ee.get(0);
                    JavaMethod jm = pc.getJavaMethod();
                    if(jm == null){ //no JavaMethod
                        try{
                            jm = (JavaMethod) model.getFactory().create(pc, JAXWSQName.METHOD.getQName());
                            model.startTransaction();
                            jm.setName(text); //TODO Need to validate this before setting it
                            pc.setJavaMethod(jm);
                            wsdlDirty = true;
                        } finally{
                            model.endTransaction();
                        }
                    } else{ //javamethod already exists
                        //reset the JavaMethod
                        try{
                            model.startTransaction();
                            jm.setName(text);
                            wsdlDirty = true;
                        }finally{
                            model.endTransaction();
                        }
                    }
                }else{  //there is no ExtensibilityElement
                    //create extensibility element and add JavaMethod
                    WSDLComponentFactory factory = model.getFactory();
                    PortCustomization pc = (PortCustomization) factory.create(port,
                            JAXWSQName.BINDINGS.getQName());
                    JavaMethod jm = (JavaMethod) factory.create(pc, JAXWSQName.METHOD.getQName());
                    try{
                        model.startTransaction();
                        jm.setName(text);
                        pc.setJavaMethod(jm);
                        port.addExtensibilityElement(pc);
                        
                        wsdlDirty = true;
                    }finally{
                        model.endTransaction();
                    }
                }
            } else{ //text is empty, use default
                if(ee.size() == 1){
                    PortCustomization pc = ee.get(0);
                    JavaMethod jm = pc.getJavaMethod();
                    if(jm != null){
                        try{
                            model.startTransaction();
                            pc.removeJavaMethod(jm);
                            //if there are no more children, remove PortCustomization
                            if(pc.getChildren().size() == 0){
                                port.removeExtensibilityElement(pc);
                            }
                            wsdlDirty = true;
                        }finally{
                            model.endTransaction();
                        }
                    }
                }
            }
        }
    }
    
    public void documentChanged(JTextComponent comp, String val) {
        if(comp == portAccessMethodText){
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
        if(source == portAccessMethodText){
            String methodName = "";
            List <PortCustomization> ee =
                    port.getExtensibilityElements(PortCustomization.class);
            if(ee.size() == 1){
                PortCustomization pc = ee.get(0);
                JavaMethod jm = pc.getJavaMethod();
                if(jm != null){
                    methodName = jm.getName();
                }
            }
            portAccessMethodText.setText(methodName);
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
        portAccessLabel = new javax.swing.JLabel();
        portAccessMethodText = new javax.swing.JTextField();
        providerCB = new javax.swing.JCheckBox();
        defaultMethodCB = new javax.swing.JCheckBox();
        serviceLabel = new javax.swing.JLabel();
        serviceName = new javax.swing.JLabel();

        portAccessLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_PORT_ACCESS_METHOD"));
        portAccessLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_PORT_ACCESS_METHOD"));

        portAccessMethodText.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("TOOLTIP_GET_PORT"));
        portAccessMethodText.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_PORT_ACCESS_METHOD"));
        portAccessMethodText.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_PORT_ACCESS_METHOD"));

        providerCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_USE_PROVIDER").charAt(0));
        providerCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_PROVIDER"));
        providerCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        providerCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        providerCB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_PROVIDER"));
        providerCB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_PROVIDER"));

        defaultMethodCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_USE_DEFAULT").charAt(0));
        defaultMethodCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));
        defaultMethodCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultMethodCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        defaultMethodCB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));
        defaultMethodCB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));

        serviceLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENCLOSING_SERVICE"));
        serviceLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENCLOSING_SERVICE"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(serviceLabel)
                            .add(portAccessLabel))
                        .add(14, 14, 14)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(portAccessMethodText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(20, 20, 20)
                                .add(defaultMethodCB))
                            .add(serviceName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 182, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(providerCB))
                .addContainerGap(77, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serviceLabel)
                    .add(serviceName))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 22, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(portAccessLabel)
                    .add(portAccessMethodText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(defaultMethodCB))
                .add(21, 21, 21)
                .add(providerCB)
                .add(19, 19, 19))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox defaultMethodCB;
    private javax.swing.JLabel portAccessLabel;
    private javax.swing.JTextField portAccessMethodText;
    private javax.swing.JCheckBox providerCB;
    private javax.swing.JLabel serviceLabel;
    private javax.swing.JLabel serviceName;
    // End of variables declaration//GEN-END:variables
    
}
