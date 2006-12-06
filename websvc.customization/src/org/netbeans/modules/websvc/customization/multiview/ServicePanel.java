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
 * ServicePanel.java
 *
 * Created on February 19, 2006, 8:55 AM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.websvc.core.jaxws.JaxWsUtils;
import org.netbeans.modules.websvc.customization.model.CustomizationComponentFactory;
import org.netbeans.modules.websvc.customization.model.JavaClass;
import org.netbeans.modules.websvc.customization.model.ServiceCustomization;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.WeakListeners;
import org.netbeans.modules.xml.multiview.Error;

/**
 *
 * @author  Roderico Cruz
 */
public class ServicePanel extends SaveableSectionInnerPanel {
    private Service service;
    private WSDLModel model;
    private boolean wsdlDirty;
    private DefaultItemListener itemListener;
    
    /** Creates new form ServicePanel */
    public ServicePanel(SectionView view, Service service){
        super(view);
        this.service = service;
        this.model = this.service.getModel();
        initComponents();
        javaClassLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        javaClassText.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        defaultJavaClassCB.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        sync();
        
        itemListener = new DefaultItemListener();
        ItemListener il = (ItemListener)WeakListeners.create(ItemListener.class,
                itemListener,defaultJavaClassCB);
        defaultJavaClassCB.addItemListener(il);
        
        addModifier(javaClassText);
        addModifier(defaultJavaClassCB);
        addValidatee(javaClassText);
    }
    
    class DefaultItemListener implements ItemListener{
        public void itemStateChanged(ItemEvent e) {
            if(defaultJavaClassCB.isSelected()){
                javaClassText.setEnabled(false);
                javaClassText.setBackground(Color.LIGHT_GRAY);
            } else{
                javaClassText.setEnabled(true);
                javaClassText.setBackground(Color.WHITE);
                javaClassText.requestFocus();
            }
        }
    }
    
    private void sync(){
        List<ServiceCustomization> ee = service.getExtensibilityElements(ServiceCustomization.class);
        if(ee.size() > 0 ){
            ServiceCustomization sc = ee.get(0);
            JavaClass jc = sc.getJavaClass();
            if(jc != null){
                setJavaClass(jc.getName());
            } else{
                defaultJavaClassCB.setSelected(true);
                javaClassText.setEnabled(false);
                javaClassText.setBackground(Color.LIGHT_GRAY);
            }
        } else{
            defaultJavaClassCB.setSelected(true);
            javaClassText.setEnabled(false);
            javaClassText.setBackground(Color.LIGHT_GRAY);
        }
    }
    
    public void setJavaClass(String name){
        javaClassText.setText(name);
    }
    
    public JComponent getErrorComponent(String string) {
        return new javax.swing.JButton("error");
    }
    
    public void linkButtonPressed(Object object, String string) {
    }
    
    public void setValue(JComponent jComponent, Object object) {
        List <ServiceCustomization> ee =
                service.getExtensibilityElements(ServiceCustomization.class);
        CustomizationComponentFactory factory = CustomizationComponentFactory.getDefault();
        if(jComponent == javaClassText ||
                jComponent == defaultJavaClassCB ){
            String text = javaClassText.getText();
            if(text != null && !text.trim().equals("") &&
                    !defaultJavaClassCB.isSelected()){
                if(!JaxWsUtils.isJavaIdentifier(text)){
                    return;
                }
                if(ee.size() == 1){  //there is existing extensibility element
                    ServiceCustomization sc = ee.get(0);
                    JavaClass jc = sc.getJavaClass();
                    if(jc == null){
                        try{
                            jc = factory.createJavaClass(model);
                            model.startTransaction();
                            jc.setName(text); //TODO Need to validate this before setting it
                            sc.setJavaClass(jc);
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                        }
                    } else{ //javamethod already exists
                        //reset the JavaMethod
                        try{
                            model.startTransaction();
                            jc.setName(text);
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                        }
                    }
                    
                } else{  //there is no ExtensibilityElement
                    //create extensibility element and add JavaClass
                    ServiceCustomization sc = factory.createServiceCustomization(model);
                    JavaClass jc = factory.createJavaClass(model);
                    try{
                        model.startTransaction();
                        jc.setName(text);
                        sc.setJavaClass(jc);
                        service.addExtensibilityElement(sc);
                        wsdlDirty = true;
                    } finally{
                            model.endTransaction();
                    }
                }
            } else{ //text field is empty, no JavaClass customization
                if(ee.size() == 1){
                    ServiceCustomization sc = ee.get(0);
                    JavaClass jc = sc.getJavaClass();
                    if(jc != null){
                        try{
                            model.startTransaction();
                            sc.removeJavaClass(jc);
                            //if sc has no more children, remove it as well
                            if(sc.getChildren().size() == 0){
                                service.removeExtensibilityElement(sc);
                            }
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                        }
                    }
                }
            }
        }
    }
    
    public void documentChanged(JTextComponent comp, String val) {
        if(comp == javaClassText){
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
        if(source == javaClassText){
            String className = "";
            List <ServiceCustomization> ee =
                    service.getExtensibilityElements(ServiceCustomization.class);
            if(ee.size() == 1){
                ServiceCustomization sc = ee.get(0);
                JavaClass jc = sc.getJavaClass();
                if(jc != null){
                    className = jc.getName();
                }
            }
            javaClassText.setText(className);
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
        javaClassLabel = new javax.swing.JLabel();
        javaClassText = new javax.swing.JTextField();
        defaultJavaClassCB = new javax.swing.JCheckBox();

        javaClassLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_JAVA_CLASS"));
        javaClassLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_JAVA_CLASS"));

        javaClassText.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("TOOLTIP_SERVICE_CLASS"));
        javaClassText.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_JAVA_CLASS"));
        javaClassText.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_JAVA_CLASS"));

        defaultJavaClassCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_USE_DEFAULT").charAt(0));
        defaultJavaClassCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));
        defaultJavaClassCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultJavaClassCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        defaultJavaClassCB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));
        defaultJavaClassCB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(javaClassLabel)
                .add(21, 21, 21)
                .add(javaClassText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 172, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(20, 20, 20)
                .add(defaultJavaClassCB)
                .addContainerGap(70, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(31, 31, 31)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(javaClassLabel)
                    .add(javaClassText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(defaultJavaClassCB))
                .addContainerGap(29, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox defaultJavaClassCB;
    private javax.swing.JLabel javaClassLabel;
    private javax.swing.JTextField javaClassText;
    // End of variables declaration//GEN-END:variables
    
}
