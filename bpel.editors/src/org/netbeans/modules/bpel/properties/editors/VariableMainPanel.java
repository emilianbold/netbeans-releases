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

import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.properties.ImportRegistrationHelper;
import static org.netbeans.modules.bpel.properties.PropertyType.NAME;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.properties.TypeContainer;
import org.netbeans.modules.bpel.properties.choosers.TypeChooserPanel;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.EditorLifeCycle;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.bpel.properties.Constants.StereotypeFilter;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor.EditingMode;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Reference;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * @author nk160297
 */
public class VariableMainPanel extends EditorLifeCycleAdapter
        implements Validator.Provider {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor<Variable> myEditor;
    private DefaultValidator myValidator;
    
    public VariableMainPanel(CustomNodeEditor<Variable> editor) {
        this.myEditor = editor;
        createContent();
    }
    
    private void bindControls2PropertyNames() {
        fldVariableName.putClientProperty(CustomNodeEditor.PROPERTY_BINDER, NAME);
    }
    
    @Override
    public boolean initControls() {
        try {
            //
            TypeChooserPanel typeChooser = (TypeChooserPanel)pnlTypeChooser;
            typeChooser.init(StereotypeFilter.ALL, myEditor.getLookup());
            //
            Variable var = myEditor.getEditedObject();
            Reference typeRef = org.netbeans.modules.bpel.editors.api.EditorUtil.getVariableType(var);
            //
            if (typeRef == null) {
                typeChooser.setSelectedValue(null);
            } else {
                TypeContainer tc = new TypeContainer(typeRef);
                typeChooser.setSelectedValue(tc);
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
            Variable var = myEditor.getEditedObject();
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
                        SchemaReference<GlobalType> gTypeRef =
                                var.createSchemaReference(
                                gType, GlobalType.class);
                        var.setType(gTypeRef);
                        var.removeElement();
                        var.removeMessageType();
                        model = gType.getModel();
                        
                        break;
                    case MESSAGE:
                        Message message = tc.getMessage();
                        WSDLReference<Message> messageRef =
                                var.createWSDLReference(message, Message.class);
                        var.setMessageType(messageRef);
                        var.removeElement();
                        var.removeType();
                        model = message.getModel();
                        
                        break;
                    case GLOBAL_ELEMENT:
                        GlobalElement gElement = tc.getGlobalElement();
                        SchemaReference<GlobalElement> elementRef =
                                var.createSchemaReference(
                                gElement, GlobalElement.class);
                        var.setElement(elementRef);
                        var.removeType();
                        var.removeMessageType();
                        model = gElement.getModel();
                        break;
                }
            }
            //
            if (model != null){
                new ImportRegistrationHelper(var.getBpelModel()).addImport(model);
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
                    String varName = fldVariableName.getText();
                    if (varName == null || varName.length() == 0) {
                        addReasonKey(Severity.ERROR, "ERR_NAME_EMPTY"); //NOI18N
                    }
                }
                
                @Override
                public void doDetailedValidation() {
                    super.doDetailedValidation();
                    //
                    // Check that the variable name is unique
                    VariableContainer vc = null;
                    if (myEditor.getEditingMode() ==
                            EditingMode.CREATE_NEW_INSTANCE) {
                        VisibilityScope visScope = (VisibilityScope)myEditor.
                                getLookup().lookup(VisibilityScope.class);
                        if (visScope != null) {
                            BaseScope scope = visScope.getClosestScope();
                            vc = scope.getVariableContainer();
                        }
//                    } else {
// A VetoException will be thorown if the name isn't unique.                        
//                        Variable var = myEditor.getModelNode().getReference();
//                        if (var != null) {
//                            BpelContainer container = var.getParent();
//                            if (container instanceof VariableContainer) {
//                                vc = (VariableContainer)container;
//                            }
//                        }
//                    }
                        //
                        if (vc != null) {
                            String varName = fldVariableName.getText();
                            Variable[] variables = vc.getVariables();
                            for (Variable variable : variables) {
                                if (varName.equals( variable.getName())){
                                    addReasonKey(Severity.ERROR, 
                                            "ERR_NOT_UNIQUE_VARIABLE_NAME"); //NOI18N
                                }
                            }
                        }
                    }
                }
                
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
        lblVariableName = new javax.swing.JLabel();
        fldVariableName = new javax.swing.JTextField();
        pnlTypeChooser = new TypeChooserPanel();
        lblErrorMessage = new javax.swing.JLabel();

        lblVariableName.setLabelFor(fldVariableName);
        lblVariableName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_VariableName")); // NOI18N

        pnlTypeChooser.setFocusable(false);

        org.jdesktop.layout.GroupLayout pnlTypeChooserLayout = new org.jdesktop.layout.GroupLayout(pnlTypeChooser);
        pnlTypeChooser.setLayout(pnlTypeChooserLayout);
        pnlTypeChooserLayout.setHorizontalGroup(
            pnlTypeChooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 394, Short.MAX_VALUE)
        );
        pnlTypeChooserLayout.setVerticalGroup(
            pnlTypeChooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 308, Short.MAX_VALUE)
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
                    .add(org.jdesktop.layout.GroupLayout.LEADING, pnlTypeChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(lblVariableName)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fldVariableName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 342, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblVariableName)
                    .add(fldVariableName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnlTypeChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        lblVariableName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_VariableName")); // NOI18N
        lblVariableName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_VariableName")); // NOI18N
        lblErrorMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_ErrorLabel")); // NOI18N
        lblErrorMessage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_ErrorLabel")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Variable_Editor")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Variable_Editor")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bngVariableMetaType;
    private javax.swing.JTextField fldVariableName;
    private javax.swing.JLabel lblErrorMessage;
    private javax.swing.JLabel lblVariableName;
    private javax.swing.JPanel pnlTypeChooser;
    // End of variables declaration//GEN-END:variables
}
