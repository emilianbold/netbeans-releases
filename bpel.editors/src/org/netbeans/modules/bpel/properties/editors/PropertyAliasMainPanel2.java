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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.nodes.CorrelationPropertyNode;
import org.netbeans.modules.bpel.properties.ImportWsdlRegistrationHelper;
import org.netbeans.modules.bpel.properties.PropAliasSelectionContainer;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.properties.choosers.CorrelationPropertyChooserPanel;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.bpel.properties.editors.controls.QNameIndicator;
import org.netbeans.modules.bpel.properties.editors.controls.TreeNodeChooser;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.bpel.properties.choosers.PropAliasTypeChooserPanel;
import org.netbeans.modules.soa.ui.form.InitialFocusProvider;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author  nk160297
 * @author changed by Vitaly Bychkov
 * @version 1.1
 */
public class PropertyAliasMainPanel2 extends EditorLifeCycleAdapter
        implements Validator.Provider, HelpCtx.Provider, InitialFocusProvider {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor<PropertyAlias> myEditor;
    private DefaultValidator myValidator;
    
    public PropertyAliasMainPanel2(CustomNodeEditor<PropertyAlias> editor) {
        this.myEditor = editor;
        createContent();
    }
    
    public HelpCtx getHelpCtx() {
        // Issue 84920
        return null;
    }
    
    private void bindControls2PropertyNames() {
////        fldProperty.putClientProperty(CustomNodeEditor.PROPERTY_BINDER, NAME);
    }
    
    @Override
    public boolean initControls() {
        try {
            //
            PropAliasTypeChooserPanel typeChooser = (PropAliasTypeChooserPanel)pnlTypeChooser;
            WSDLModel wsdlModel = myEditor.getEditedObject().getModel();
            typeChooser.init(wsdlModel, myEditor.getLookup());
            //
            PropertyAlias propAlias = myEditor.getEditedObject();
            if (propAlias == null) {
                typeChooser.setSelectedValue(null);
            } else {
                PropAliasSelectionContainer selection =
                        new PropAliasSelectionContainer(propAlias);
                typeChooser.setSelectedValue(selection);
                //
                NamedComponentReference<CorrelationProperty> property = propAlias.getPropertyName();
                if (property != null) {
                    myCorrProp = property.get();
                    if (myCorrProp != null) {
                        fldProperty.setText(myCorrProp.getName());
                        NamedComponentReference propTypeOrElement = myCorrProp.getType();
                        propTypeOrElement = propTypeOrElement == null
                                ? myCorrProp.getElement()
                                : propTypeOrElement;
                        if (propTypeOrElement != null) {
                            myPropTypeQname = propTypeOrElement.getQName();
                            ((QNameIndicator)fldPropertyType).setQName(myPropTypeQname);
                        }
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
        final Lookup lookup = myEditor.getLookup();
        //
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (btnBrowseProp.equals(event.getSource())) {
                    String title = NbBundle.getMessage(FormBundle.class
                            , "DLG_ChoosePropertyTitle");
                    CorrelationPropertyChooserPanel propertyChooser =
                            new CorrelationPropertyChooserPanel(lookup);
                    //
                    TreeNodeChooser chooser = new TreeNodeChooser(propertyChooser);
                    chooser.initControls();
                    //
                    DefaultDialogDescriptor descriptor =
                            new DefaultDialogDescriptor(chooser, title);
                    DialogDisplayer.getDefault().notify(descriptor);
                    if (descriptor.isOkHasPressed()) {
                        Object selectedObj = chooser.getSelectedValue();
                        if (selectedObj != null
                                && selectedObj instanceof CorrelationPropertyNode) {
                            myCorrProp = ((CorrelationPropertyNode)selectedObj).getReference();
                            if (myCorrProp == null) {
                                return;
                            }
                            fldProperty.setText(myCorrProp.getName());
                            NamedComponentReference propTypeOrElement = myCorrProp.getType();
                            propTypeOrElement = propTypeOrElement == null
                                    ? myCorrProp.getElement()
                                    : propTypeOrElement;
                            if (propTypeOrElement != null) {
                                myPropTypeQname = propTypeOrElement.getQName();
                                ((QNameIndicator)fldPropertyType).setQName(myPropTypeQname);
                            }
                        }
                        getValidator().revalidate(true);
                    }
                    chooser.afterClose();
                }
            }
        };
        //
        btnBrowseProp.addActionListener(listener);
        //
        PropAliasTypeChooserPanel chooserPanel = 
                (PropAliasTypeChooserPanel)pnlTypeChooser;
        chooserPanel.createContent();
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
            PropertyAlias propAlias = myEditor.getEditedObject();
            // set propertyName
            if (myCorrProp != null) {
                // import wsdl if need
                ImportWsdlRegistrationHelper importHelper
                        = new ImportWsdlRegistrationHelper(propAlias.getModel());
                importHelper.addImport(myCorrProp.getModel());
                //
                
                NamedComponentReference<CorrelationProperty> refCorrProp
                        = propAlias.createReferenceTo(myCorrProp,CorrelationProperty.class);
                propAlias.setPropertyName(refCorrProp);
            }
            
            Model model = null;
            //
            PropAliasSelectionContainer selection =
                    ((PropAliasTypeChooserPanel)pnlTypeChooser).getSelectedValue();
            //
            String queryContent = selection.getQueryContent();
            if (queryContent == null || queryContent.trim().equals("")) {
                propAlias.removeQuery();
            } else {
                Util.setQueryImpl(propAlias, queryContent);
            }
            //
            if (selection != null) {
//                System.out.println("tc.getStereotype(): "+(tc.getStereotype()));
                switch (selection.getTypeContainer().getStereotype()) {
                    case PRIMITIVE_TYPE:
                    case GLOBAL_SIMPLE_TYPE:
                    case GLOBAL_COMPLEX_TYPE:
                    case GLOBAL_TYPE:
////                        GlobalType gType = tc.getGlobalType();
////                        NamedComponentReference<GlobalType> gTypeRef =
////                                propAlias.createSchemaReference(
////                                gType, GlobalType.class);
////                        propAlias.setType(gTypeRef);
////                        propAlias.setElement(null);
//////                        propAlias.removeQuery();
////                        model = gType.getModel();
////
                        break;
                    case MESSAGE:
                        Message message = selection.getTypeContainer().getMessage();
                        NamedComponentReference<Message> msgRef =
                                propAlias.createReferenceTo(message, Message.class);
                        propAlias.setMessageType(msgRef);
                        //
                        Part part = selection.getMessagePart();
                        if (part == null) {
                            propAlias.setPart(null);
                        } else {
                            propAlias.setPart(part.getName());
                        }
                        //
                        propAlias.setElement(null);
                        propAlias.setType(null);
                        model = message.getModel();
//                        System.out.println("PropertyAliasMainPanel#applyNewValues: "+tc);
                        break;
                    case GLOBAL_ELEMENT:
////                        GlobalElement gElement = tc.getGlobalElement();
////                        NamedComponentReference<GlobalElement> elementRef =
////                                propAlias.createSchemaReference(
////                                gElement, GlobalElement.class);
////                        propAlias.setElement(elementRef);
////                        propAlias.setType(null);
////                        model = gElement.getModel();
                        break;
                }
            }
            //
            if (model != null){
                // TODO add import to wsdl file
                ImportWsdlRegistrationHelper importHelper
                        = new ImportWsdlRegistrationHelper(propAlias.getModel());
                importHelper.addImport(model);
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
                    String property = fldProperty.getText();
//                    System.out.println("propName: "+fldProperty.getText());
                    if (property == null || property.length() == 0) {
                        addReasonKey(Severity.ERROR, "ERR_PROP_EMPTY"); //NOI18N
                    }
//                    if (property == null || property.length() == 0) {
//                        addReasonKey("ERR_NAME_EMPTY"); //NOI18N
//                    }
                }

//                @Override
//                public void doDetailedValidation() {
//                    super.doDetailedValidation();
//                    //
//                    // Check that the variable name is unique
////                    VariableContainer vc = null;
////                    if (myEditor.getEditingMode() ==
////                            EditingMode.CREATE_NEW_INSTANCE) {
////                        VisibilityScope visScope = (VisibilityScope)myEditor.
////                                getLookup().lookup(VisibilityScope.class);
////                        if (visScope != null) {
////                            BaseScope scope = visScope.getClosestScope();
////                            vc = scope.getVariableContainer();
////                        }
//////                    } else {
//////                        Variable var = myEditor.getModelNode().getReference();
//////                        if (var != null) {
//////                            BpelContainer container = var.getParent();
//////                            if (container instanceof VariableContainer) {
//////                                vc = (VariableContainer)container;
//////                            }
//////                        }
//////                    }
////                        //
////                        if (vc != null) {
////                            String property = fldProperty.getText();
////                            Variable[] variables = vc.getVariables();
////                            for (Variable variable : variables) {
////                                if (varName.equals( variable.getName())){
////                                    addReasonKey("ERR_NOT_UNIQUE_VARIABLE_NAME"); //NOI18N
////                                }
////                            }
////                        }
////                    }
//                }
                
            };
        }
        return myValidator;
    }
    
    public Component getInitialFocusComponent() {
        PropAliasTypeChooserPanel typeChooser = (PropAliasTypeChooserPanel)pnlTypeChooser;
        return typeChooser.getTreeComponent();
    }

    public int getProviderPriority() {
        return 0;
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
        lblProperty = new javax.swing.JLabel();
        fldProperty = new javax.swing.JTextField();
        pnlTypeChooser = new PropAliasTypeChooserPanel();
        lblErrorMessage = new javax.swing.JLabel();
        btnBrowseProp = new javax.swing.JButton();
        fldPropertyType = new QNameIndicator();
        lblPropertyType = new javax.swing.JLabel();

        lblProperty.setLabelFor(fldProperty);
        lblProperty.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Property")); // NOI18N

        fldProperty.setEditable(false);

        pnlTypeChooser.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_MapPropertyTo"))); // NOI18N
        pnlTypeChooser.setFocusable(false);

        org.jdesktop.layout.GroupLayout pnlTypeChooserLayout = new org.jdesktop.layout.GroupLayout(pnlTypeChooser);
        pnlTypeChooser.setLayout(pnlTypeChooserLayout);
        pnlTypeChooserLayout.setHorizontalGroup(
            pnlTypeChooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 318, Short.MAX_VALUE)
        );
        pnlTypeChooserLayout.setVerticalGroup(
            pnlTypeChooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 232, Short.MAX_VALUE)
        );

        lblErrorMessage.setForeground(new java.awt.Color(255, 0, 0));
        lblErrorMessage.setFocusable(false);

        btnBrowseProp.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "BNT_Browse")); // NOI18N

        fldPropertyType.setEditable(false);
        fldPropertyType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fldPropertyTypeActionPerformed(evt);
            }
        });

        lblPropertyType.setLabelFor(fldPropertyType);
        lblPropertyType.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Property_Type")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pnlTypeChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblProperty)
                            .add(lblPropertyType))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(fldProperty, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnBrowseProp))
                            .add(fldPropertyType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblProperty)
                    .add(btnBrowseProp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(fldProperty, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblPropertyType)
                    .add(fldPropertyType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnlTypeChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        lblProperty.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_VariableName")); // NOI18N
        lblProperty.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_VariableName")); // NOI18N
        fldProperty.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Property")); // NOI18N
        fldProperty.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSD_LBL_Property")); // NOI18N
        lblErrorMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_ErrorLabel")); // NOI18N
        lblErrorMessage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_ErrorLabel")); // NOI18N
        btnBrowseProp.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_BTN_Browse")); // NOI18N
        btnBrowseProp.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSD_BTN_Browse")); // NOI18N
        lblPropertyType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Property_Type")); // NOI18N
        lblPropertyType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSD_LBL_Property_Type")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_AddCorrelationPropertyAlias")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_AddCorrelationPropertyAlias")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void fldPropertyTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fldPropertyTypeActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_fldPropertyTypeActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bngVariableMetaType;
    private javax.swing.JButton btnBrowseProp;
    private javax.swing.JTextField fldProperty;
    private javax.swing.JTextField fldPropertyType;
    private javax.swing.JLabel lblErrorMessage;
    private javax.swing.JLabel lblProperty;
    private javax.swing.JLabel lblPropertyType;
    private javax.swing.JPanel pnlTypeChooser;
    // End of variables declaration//GEN-END:variables
    
    private QName myPropTypeQname;
    private CorrelationProperty myCorrProp;

}

