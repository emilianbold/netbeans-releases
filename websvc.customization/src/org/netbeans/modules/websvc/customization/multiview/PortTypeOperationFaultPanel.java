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
 * PortTypeOperationFaultPanel.java
 *
 * Created on February 19, 2006, 8:44 AM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.core.wseditor.spi.SaveSetter;
import org.netbeans.modules.websvc.customization.model.CustomizationComponentFactory;
import org.netbeans.modules.websvc.customization.model.JavaClass;
import org.netbeans.modules.websvc.customization.model.PortTypeOperationFaultCustomization;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.WeakListeners;
import org.netbeans.modules.xml.multiview.Error;

/**
 *
 * @author  Roderico Cruz
 */
public class PortTypeOperationFaultPanel extends SaveableSectionInnerPanel {
    private Fault fault;
    private SaveSetter setter;
    private WSDLModel model;
    private boolean wsdlDirty;
    private DefaultItemListener defaultListener;
    /**
     * Creates new form PortTypeOperationFaultPanel
     */
    public PortTypeOperationFaultPanel(SectionView view, Fault fault){
        super(view);
        this.fault = fault;
        this.model = this.fault.getModel();
        initComponents();
        operationLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        operationName.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        operationName.setText(getParentOfFault(fault));
        javaClassLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        javaClassText.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        defaultJavaClassCB.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        
        sync();
        addModifier(javaClassText);
        addModifier(defaultJavaClassCB);
        addValidatee(javaClassText);
        
        defaultListener = new DefaultItemListener();
        ItemListener il = (ItemListener)WeakListeners.create(ItemListener.class, defaultListener,
                defaultJavaClassCB);
        defaultJavaClassCB.addItemListener(il);
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
    
    private String getParentOfFault(Fault fault){
        Operation op = (Operation)fault.getParent();
        return op.getName();
    }
    
    private void sync(){
        List<PortTypeOperationFaultCustomization> ee =
                fault.getExtensibilityElements(PortTypeOperationFaultCustomization.class);
        if(ee.size() == 1){
            PortTypeOperationFaultCustomization ptof = ee.get(0);
            JavaClass jc = ptof.getJavaClass();
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
    
    public String getJavaClass(){
        return javaClassText.getText();
    }
    
    public JComponent getErrorComponent(String string) {
        return new JButton("error");
    }
    
    public void linkButtonPressed(Object object, String string) {
    }
    
    public void setValue(JComponent jComponent, Object object) {
        List <PortTypeOperationFaultCustomization> ee =
                fault.getExtensibilityElements(PortTypeOperationFaultCustomization.class);
        CustomizationComponentFactory factory = CustomizationComponentFactory.getDefault();
        if(jComponent == javaClassText ||
                jComponent == defaultJavaClassCB ){
            String text = javaClassText.getText();
            if(text != null && !text.trim().equals("")
            && !defaultJavaClassCB.isSelected()){
                if(!JaxWsUtils.isJavaIdentifier(text)){
                    return;
                }
                if(ee.size() == 1){  //there is existing extensibility element
                    PortTypeOperationFaultCustomization ptofc = ee.get(0);
                    JavaClass jc = ptofc.getJavaClass();
                    if(jc == null){  //there is no JavaClass, create one
                        try{
                            jc = factory.createJavaClass(model);
                            model.startTransaction();
                            jc.setName(text); //TODO Need to validate this before setting it
                            ptofc.setJavaClass(jc);
                            wsdlDirty = true;
                        }finally{
                                model.endTransaction();
                        }
                    } else{ //javaclass already exists
                        //reset the JavaClass
                        try{
                            model.startTransaction();
                            jc.setName(text);
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                        }
                    }
                }else{  //there is no ExtensibilityElement
                    //create extensibility element and add JavaClass
                    PortTypeOperationFaultCustomization ptofc =
                            factory.createPortTypeOperationFaultCustomization(model);
                    JavaClass jc = factory.createJavaClass(model);
                    try{
                        model.startTransaction();
                        jc.setName(text);
                        ptofc.setJavaClass(jc);
                        fault.addExtensibilityElement(ptofc);
                        wsdlDirty = true;
                    } finally{
                            model.endTransaction();
                    }
                }
            } else{ //no JavaClass is specified, remove from the model if it is there
                if(ee.size() == 1){
                    try{
                        PortTypeOperationFaultCustomization ptofc = ee.get(0);
                        JavaClass jc = ptofc.getJavaClass();
                        if(jc != null){
                            model.startTransaction();
                            ptofc.removeJavaClass(jc);
                            //if(ptofc has no more children, remove it as well)
                            if(ptofc.getChildren().size() == 0){
                                fault.removeExtensibilityElement(ptofc);
                            }
                            
                            wsdlDirty = true;
                        }
                    } finally{
                            model.endTransaction();
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
            List <PortTypeOperationFaultCustomization> ee =
                    fault.getExtensibilityElements(PortTypeOperationFaultCustomization.class);
            if(ee.size() == 1){
                PortTypeOperationFaultCustomization ptc = ee.get(0);
                JavaClass jc = ptc.getJavaClass();
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
        operationLabel = new javax.swing.JLabel();
        operationName = new javax.swing.JLabel();

        javaClassLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_JAVA_CLASS"));
        javaClassLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_JAVA_CLASS"));

        javaClassText.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("TOOLTIP_PORTTYPE_FAULT_CLASS"));
        javaClassText.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_JAVA_CLASS"));
        javaClassText.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_JAVA_CLASS"));

        defaultJavaClassCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_USE_DEFAULT").charAt(0));
        defaultJavaClassCB.setText("Use Default");
        defaultJavaClassCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultJavaClassCB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        defaultJavaClassCB.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));
        defaultJavaClassCB.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_USE_DEFAULT"));

        operationLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENCLOSING_OPERATION"));
        operationLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("LBL_ENCLOSING_OPERATION"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(javaClassLabel)
                        .add(15, 15, 15)
                        .add(javaClassText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 227, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(20, 20, 20)
                        .add(defaultJavaClassCB))
                    .add(layout.createSequentialGroup()
                        .add(operationLabel)
                        .add(15, 15, 15)
                        .add(operationName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 165, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(operationLabel)
                    .add(operationName))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(javaClassLabel)
                    .add(javaClassText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(defaultJavaClassCB))
                .addContainerGap(19, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox defaultJavaClassCB;
    private javax.swing.JLabel javaClassLabel;
    private javax.swing.JTextField javaClassText;
    private javax.swing.JLabel operationLabel;
    private javax.swing.JLabel operationName;
    // End of variables declaration//GEN-END:variables
    
}
