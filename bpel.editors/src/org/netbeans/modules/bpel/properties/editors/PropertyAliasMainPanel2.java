/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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
package org.netbeans.modules.bpel.properties.editors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.nodes.CorrelationPropertyNode;
import org.netbeans.modules.bpel.properties.ImportWsdlRegistrationHelper;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.TypeContainer;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.properties.choosers.CorrelationPropertyChooserPanel;
import org.netbeans.modules.bpel.properties.editors.controls.CustomNodeEditor;
import org.netbeans.modules.bpel.properties.editors.controls.EditorLifeCycle;
import org.netbeans.modules.bpel.properties.editors.controls.EditorLifeCycleAdapter;
import org.netbeans.modules.bpel.properties.editors.controls.QNameIndicator;
import org.netbeans.modules.bpel.properties.editors.controls.TreeNodeChooser;
import org.netbeans.modules.bpel.properties.editors.controls.valid.DefaultValidator;
import org.netbeans.modules.bpel.properties.editors.controls.valid.NodeChooserDescriptor;
import org.netbeans.modules.bpel.properties.editors.controls.valid.ValidStateManager;
import org.netbeans.modules.bpel.properties.editors.controls.valid.ValidStateManager.ValidStateListener;
import org.netbeans.modules.bpel.properties.editors.controls.valid.Validator;
import org.netbeans.modules.bpel.properties.choosers.PropAliasTypeChooserPanel;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
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
        implements Validator.Provider, HelpCtx.Provider {
    
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
    
    public boolean initControls() {
        try {
            //
            PropAliasTypeChooserPanel typeChooser = (PropAliasTypeChooserPanel)pnlTypeChooser;
//            typeChooser.initControls();
            typeChooser.init(myEditor.getLookup());
//            typeChooser.init(Constants.CORRELATION_PROPERTY_ALIAS_STEREO_TYPE_FILTER, myEditor.getLookup());
            //
            typeChooser.setIncorrectNodeSelectionReasonKey("ERR_PROP_ALIAS_TYPE_NOT_SPECIFIED"); // NOI18N
            //
            PropertyAlias propAlias = myEditor.getModelNode().getReference();
            if (propAlias == null) {
                typeChooser.setSelectedType(null);
            } else {
                NamedComponentReference<GlobalElement> tmpElement = propAlias.getElement();
                NamedComponentReference<GlobalType> tmpType = propAlias.getType();
                NamedComponentReference<Message> tmpMessage = propAlias.getMessageType();
                if (tmpElement != null) {
                    typeChooser.setSelectedType(new TypeContainer(tmpElement.get()));
                } else if (tmpType != null) {
                    typeChooser.setSelectedType(new TypeContainer(tmpType.get()));
                } else if (tmpMessage != null) {
                    typeChooser.setSelectedType(new TypeContainer(tmpMessage.get()));
                } else {
                    typeChooser.setSelectedType(null);
                }
                
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
                
                //
                Query tmpQuery = propAlias.getQuery();
                if (tmpQuery != null) {
                    typeChooser.setQueryFld(tmpQuery.getContent());
                }
                
            }
            
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return true;
    }
    
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
                    NodeChooserDescriptor descriptor =
                            new NodeChooserDescriptor(chooser, title);
                    DialogDisplayer.getDefault().notify(descriptor);
                    if (descriptor.isOkHasPressed()) {
                        Object selectedObj = chooser.getSelectedValue();
                        if (selectedObj != null 
                                && selectedObj instanceof CorrelationPropertyNode) 
                        {
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
        ((EditorLifeCycle)pnlTypeChooser).createContent();
        PropAliasTypeChooserPanel chooserPanel = 
                (PropAliasTypeChooserPanel)pnlTypeChooser;
        Util.attachDefaultDblClickAction(chooserPanel, chooserPanel);
        //
        myEditor.getValidStateManager().addValidStateListener(
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
    
    public boolean applyNewValues() {
        try {
            PropertyAlias propAlias = myEditor.getModelNode().getReference();
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
            TypeContainer tc = ((PropAliasTypeChooserPanel)pnlTypeChooser).getSelectedType();
            //
            String queryContent = ((PropAliasTypeChooserPanel)pnlTypeChooser).getQueryContent();
            if (queryContent != null && !queryContent.trim().equals("")) {
                Util.setQueryImpl(propAlias,queryContent);
            }
            //
            if (tc != null) {
//                System.out.println("tc.getStereotype(): "+(tc.getStereotype()));
                switch (tc.getStereotype()) {
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
                        Message message = tc.getMessage();
                        NamedComponentReference<Message> messageRef =
                                propAlias.createReferenceTo(message, Message.class);
                        propAlias.setMessageType(messageRef);
                        propAlias.setElement(null);
                        propAlias.setType(null);
                        propAlias.setPart(null);
                        model = message.getModel();
                        
                        break;
                    case MESSAGE_PART:
//                        Part part = tc.getMessagePart();
                        Part part = tc.getMessagePart();
                        WSDLComponent partParent = part.getParent();
                        if  (partParent instanceof Message && part != null) {
                            NamedComponentReference<Message> messageParentPartRef =
                                propAlias.createReferenceTo((Message)partParent, Message.class);
                            propAlias.setMessageType(messageParentPartRef);
                            propAlias.setPart(part.getName());
                        }
                        propAlias.setElement(null);
                        propAlias.setType(null);
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
            myValidator = new DefaultValidator(myEditor) {
                
                public boolean doFastValidation() {
                    boolean isValid = true;
                    //
                    String property = fldProperty.getText();
//                    System.out.println("propName: "+fldProperty.getText());
                    if (property == null || property.length() == 0) {
                        addReasonKey("ERR_PROP_EMPTY"); //NOI18N
                        isValid = false;
                    } 
//                    if (property == null || property.length() == 0) {
//                        addReasonKey("ERR_NAME_EMPTY"); //NOI18N
//                        isValid = false;
//                    }
                    //
                    return isValid;
                }
                
                public boolean doDetailedValidation() {
                    boolean isValid = super.doDetailedValidation();
                    //
                    // Check that the variable name is unique
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
////                                    isValid = false;
////                                }
////                            }
////                        }
////                    }
                    //
                    return isValid;
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        bngVariableMetaType = new javax.swing.ButtonGroup();
        lblProperty = new javax.swing.JLabel();
        fldProperty = new javax.swing.JTextField();
        pnlTypeChooser = new PropAliasTypeChooserPanel(/*Constants.StereotypeFilter.ALL,*/myEditor.getLookup());
        lblErrorMessage = new javax.swing.JLabel();
        btnBrowseProp = new javax.swing.JButton();
        fldPropertyType = new QNameIndicator();
        lblPropertyType = new javax.swing.JLabel();

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_CreateNewVariableTitle"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_CreateNewVariableTitle"));
        lblProperty.setLabelFor(fldProperty);
        lblProperty.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Property"));
        lblProperty.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_LBL_VariableName"));
        lblProperty.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_LBL_VariableName"));

        fldProperty.setEditable(false);
        fldProperty.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Property"));
        fldProperty.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSD_LBL_Property"));

        pnlTypeChooser.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(""), org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_MapPropertyTo")));
        pnlTypeChooser.setFocusable(false);
        org.jdesktop.layout.GroupLayout pnlTypeChooserLayout = new org.jdesktop.layout.GroupLayout(pnlTypeChooser);
        pnlTypeChooser.setLayout(pnlTypeChooserLayout);
        pnlTypeChooserLayout.setHorizontalGroup(
            pnlTypeChooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 472, Short.MAX_VALUE)
        );
        pnlTypeChooserLayout.setVerticalGroup(
            pnlTypeChooserLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 254, Short.MAX_VALUE)
        );

        lblErrorMessage.setForeground(new java.awt.Color(255, 0, 0));
        lblErrorMessage.setFocusable(false);
        lblErrorMessage.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_ErrorLabel"));
        lblErrorMessage.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_ErrorLabel"));

        btnBrowseProp.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "BNT_Browse"));
        btnBrowseProp.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_BTN_Browse"));
        btnBrowseProp.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSD_BTN_Browse"));

        fldPropertyType.setEditable(false);
        fldPropertyType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fldPropertyTypeActionPerformed(evt);
            }
        });

        lblPropertyType.setLabelFor(fldPropertyType);
        lblPropertyType.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_Property_Type"));
        lblPropertyType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSN_LBL_Property_Type"));
        lblPropertyType.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class, "ACSD_LBL_Property_Type"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, pnlTypeChooser, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lblProperty)
                            .add(lblPropertyType))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(fldProperty, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 317, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(btnBrowseProp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .add(fldPropertyType, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .add(pnlTypeChooser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblErrorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 5, Short.MAX_VALUE)
                .addContainerGap())
        );
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

