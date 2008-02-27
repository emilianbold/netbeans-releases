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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.properties.editors;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.bpel.properties.ImportWsdlRegistrationHelper;
import static org.netbeans.modules.bpel.properties.PropertyType.NAME;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.TypeContainer;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.properties.choosers.TypeChooserPanel;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.EditorLifeCycle;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor.EditingMode;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;

/**
 * @author  nk160297
 * @author changed by Vitaly Bychkov
 * @version 1.1
 */
public class CorrelationPropertyMainPanel extends EditorLifeCycleAdapter
        implements Validator.Provider, HelpCtx.Provider {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor<CorrelationProperty> myEditor;
    private DefaultValidator myValidator;
    
    public CorrelationPropertyMainPanel(CustomNodeEditor<CorrelationProperty> editor) {
        this.myEditor = editor;
        createContent();
    }
    
    public HelpCtx getHelpCtx() {
      // Issue 84921
      return null;
    }

    private void bindControls2PropertyNames() {
        fldPropertyName.putClientProperty(CustomNodeEditor.PROPERTY_BINDER, NAME);
    }
    
    @Override
    public boolean initControls() {
        try {
            //
            TypeChooserPanel typeChooser
                    = (TypeChooserPanel)pnlTypeChooser;
            typeChooser.init(Constants.CORRELATION_PROPERTY_STEREO_TYPE_FILTER
                    , myEditor.getLookup());
            typeChooser.hideChbShowImportedOnly(true);
            typeChooser.setIncorrectNodeSelectionReasonKey("ERR_PROPERTY_TYPE_NOT_SPECIFIED"); // NOI18N
            //
            CorrelationProperty corrProp = myEditor.getEditedObject();
            if (corrProp == null) {
                typeChooser.setSelectedValue(null);
            } else {
                NamedComponentReference<GlobalElement> tmpElement = corrProp.getElement();
                if (tmpElement != null) {
                    typeChooser.setSelectedValue(new TypeContainer(tmpElement.get()));
                } else {
                    NamedComponentReference<GlobalType> tmpType = corrProp.getType();
                    if (tmpType != null) {
                        typeChooser.setSelectedValue(new TypeContainer(tmpType.get()));
                    } else {
                        typeChooser.setSelectedValue(null);
                    }
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return true;
    }
    
    @Override
    public void createContent() {
        initComponents();
        bindControls2PropertyNames();
        //
        ((EditorLifeCycle)pnlTypeChooser).createContent();
        TypeChooserPanel chooserPanel = (TypeChooserPanel)pnlTypeChooser;
        Util.attachDefaultDblClickAction(chooserPanel, chooserPanel);
        //
        myEditor.getValidStateManager(true).addValidStateListener(
                new ValidStateListener() {
            public void stateChanged(ValidStateManager source, boolean isValid) {
                if (source.isValid()) {
                    lblErrorMessage.setText("");
                } else {
                    lblErrorMessage.setText(source.getHtmlReasons());
                }
            }
        });
    }
    
    @Override
    public boolean applyNewValues() {
        try {
            CorrelationProperty corrProp = myEditor.getEditedObject();
            Model model = null;
            //
            TypeContainer tc = ((TypeChooserPanel)pnlTypeChooser).getSelectedValue();
            
            if (tc != null) {
                switch (tc.getStereotype()) {
                    case PRIMITIVE_TYPE:
                    case GLOBAL_SIMPLE_TYPE:
                    case GLOBAL_COMPLEX_TYPE:
                    case GLOBAL_TYPE:
                        GlobalType gType = tc.getGlobalType();
                        NamedComponentReference<GlobalType> typeReference = corrProp.
                                createSchemaReference(gType, GlobalType.class);
                        corrProp.setType(typeReference);
                        model = gType.getModel();
                        
                        break;
                    case MESSAGE:
//                        Message message = tc.getMessage();
//                        WSDLReference<Message> messageRef =
//                                var.createWSDLReference(message, Message.class);
//                        var.setMessageType(messageRef);
//                        var.removeElement();
//                        var.removeType();
//                        model = message.getModel();
                        
                        break;
                    case GLOBAL_ELEMENT:
                        GlobalElement gElement = tc.getGlobalElement();
                        NamedComponentReference<GlobalElement> elementReference
                                = corrProp.createSchemaReference(gElement,GlobalElement.class);
                        corrProp.setElement(elementReference);
                        model = gElement.getModel();
                        
                        break;
                }
            }
            //
            if (model != null){
                
                ImportWsdlRegistrationHelper importHelper
                        = new ImportWsdlRegistrationHelper(corrProp.getModel());
                importHelper.addImport(model);
////                BpelModel bpelModel = (BpelModel) myEditor.getLookup().lookup(BpelModel.class);
////                System.out.println("corrProp bpelModel: "+bpelModel);
////                importHelper.addImport2BpelModel(model, bpelModel);
//                new ImportRegistrationHelper(var.getBpelModel()).addImport(model);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return true;
    }
    
    public DefaultValidator getValidator() {
        if (myValidator == null) {
            myValidator = new DefaultValidator(myEditor, ErrorMessagesBundle.class) {
                
                public void doFastValidation() {
                    String propName = fldPropertyName.getText();
                    if (propName == null || propName.length() == 0) {
                        addReasonKey(Severity.ERROR, "ERR_PROP_NAME_EMPTY"); //NOI18N
                    } else {
                        boolean isCorrectName = Util.isNCName(propName);
                        if (!isCorrectName) {
                            addReasonKey(Severity.ERROR, "ERR_PROP_NAME_INVALID"); //NOI18N;
                        }
                        
                        if (isCorrectName) {
                            CorrelationProperty corrProp = myEditor.getEditedObject();
                            WSDLModel wsdlModel = null;
                            if (corrProp != null) {
                                wsdlModel = corrProp.getModel();
                            }
                            if (wsdlModel != null) {
                                if (myEditor.getEditingMode() == EditingMode.CREATE_NEW_INSTANCE 
                                        || (myEditor.getEditingMode() ==
                                        EditingMode.EDIT_INSTANCE && !propName.equals(corrProp.getName()))) 
                                {
                                    isCorrectName = Util.isUniquePropertyName(wsdlModel, propName);
                                }
                            }
                        }
                        
                        if (!isCorrectName) {
                            addReasonKey(Severity.ERROR, 
                                    "ERR_PROP_NAME_NOT_UNIQUE"); //NOI18N;
                        }
                    }
                }
                
//                @Override
//                public void doDetailedValidation() {
//                    super.doDetailedValidation();
//                    //
//                    // Check that the property name is unique
//                }
                
            };
        }
        return myValidator;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bngVariableMetaType = new javax.swing.ButtonGroup();
        lblPropertyName = new javax.swing.JLabel();
        fldPropertyName = new javax.swing.JTextField();
        fldPropertyName.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                getValidator().revalidate(true);
            }
            public void keyReleased(KeyEvent e) {
                getValidator().revalidate(true);
            }
            public void keyTyped(KeyEvent e) {
                getValidator().revalidate(true);
            }
        });
        pnlTypeChooser = new TypeChooserPanel(Constants.CORRELATION_PROPERTY_STEREO_TYPE_FILTER
            , myEditor.getLookup());
        lblErrorMessage = new javax.swing.JLabel();

        lblPropertyName.setLabelFor(fldPropertyName);
        lblPropertyName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_VariableName")); // NOI18N

        pnlTypeChooser.setFocusable(false);

        org.jdesktop.layout.GroupLayout pnlTypeChooserLayout = new org.jdesktop.layout.GroupLayout(pnlTypeChooser);
        pnlTypeChooser.setLayout(pnlTypeChooserLayout);
        pnlTypeChooserLayout.setHorizontalGroup(
            pnlTypeChooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 438, Short.MAX_VALUE)
        );
        pnlTypeChooserLayout.setVerticalGroup(
            pnlTypeChooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 268, Short.MAX_VALUE)
        );

        lblErrorMessage.setForeground(new java.awt.Color(255, 0, 0));
        lblErrorMessage.setFocusable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, pnlTypeChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(lblPropertyName)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fldPropertyName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPropertyName)
                    .add(fldPropertyName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnlTypeChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        lblPropertyName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_VariableName")); // NOI18N
        lblPropertyName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_VariableName")); // NOI18N
        lblErrorMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_ErrorLabel")); // NOI18N
        lblErrorMessage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_ErrorLabel")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_AddCorrelationProperty")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_AddCorrelationProperty")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bngVariableMetaType;
    private javax.swing.JTextField fldPropertyName;
    private javax.swing.JLabel lblErrorMessage;
    private javax.swing.JLabel lblPropertyName;
    private javax.swing.JPanel pnlTypeChooser;
    // End of variables declaration//GEN-END:variables
}
