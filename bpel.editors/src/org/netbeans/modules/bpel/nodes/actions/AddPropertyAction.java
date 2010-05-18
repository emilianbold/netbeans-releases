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
package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.properties.ImportRegistrationHelper;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.bpel.nodes.CorrelationSetNode;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.choosers.CorrelationPropertyChooserPanel;
import org.netbeans.modules.bpel.properties.editors.controls.TreeNodeChooser;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.soa.ui.form.valid.ValidationExtension;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 14 April 2006
 */
public class AddPropertyAction extends BpelNodeAction {
    private static final long serialVersionUID = 1L;
    
    protected String getBundleName() {
        return NbBundle.getMessage(getClass(), "CTL_AddPropertyAction"); // NOI18N
    }
    
    public ActionType getType() {
        return ActionType.ADD_PROPERTY;
    }
    
    protected void performAction(BpelEntity[] bpelEntities) {
    }
    
    @Override
    public void performAction(Node[] nodes) {
        final CorrelationSet correlationSet =
                (CorrelationSet) ((CorrelationSetNode)nodes[0]).getReference();
        if (correlationSet == null) {
            return;
        }
        Lookup lookup = nodes[0].getLookup();
        Set<CorrelationProperty> cpSet = chooseProperty(correlationSet, lookup);
        if (cpSet == null) {
            return; // It usually means that user pressed the Cancel button
        }
        //
        List<WSDLReference<CorrelationProperty>> oldCorrPropRefList =
                correlationSet.getProperties();
        //
        List<WSDLReference<CorrelationProperty>> newCorrPropRefList = null;
        if (oldCorrPropRefList != null) {
            newCorrPropRefList =
                    new ArrayList<WSDLReference<CorrelationProperty>>(
                    oldCorrPropRefList );
        } else {
            newCorrPropRefList =
                    new ArrayList<WSDLReference<CorrelationProperty>>();
        }
        //
        for (CorrelationProperty property : cpSet) {
            if (property != null) {
                WSDLReference<CorrelationProperty> newPropRef =
                        correlationSet.createWSDLReference(property,
                        CorrelationProperty.class);
                newCorrPropRefList.add(newPropRef);
                //
                new ImportRegistrationHelper(correlationSet.getBpelModel()).addImport(property.getModel());
            }
        }
        //
        correlationSet.setProperties(newCorrPropRefList);
    }
    
    @Override
    protected boolean enable(BpelEntity[] bpelEntities) {
        if (!super.enable(bpelEntities)) {
            return false;
        }
        
        BpelEntity bpelEntity = bpelEntities[0];
        
        return (bpelEntity instanceof CorrelationSet);
    }
    
    public static Set<CorrelationProperty> chooseProperty(
            CorrelationSet correlationSet, Lookup lookup) {
        //
        List<CorrelationProperty> cpList = new ArrayList<CorrelationProperty>();
        List<WSDLReference<CorrelationProperty>> cpRefList =
                correlationSet.getProperties();
        if (cpRefList != null) {
            for (WSDLReference<CorrelationProperty> cPropRef : cpRefList) {
                CorrelationProperty cp = cPropRef.get();
                if (cp != null) {
                    cpList.add(cp);
                }
            }
        }
        //
        return chooseProperty(correlationSet, cpList, lookup);
    }
    
    public static Set<CorrelationProperty> chooseProperty(
            final CorrelationSet correlationSet,
            final List<CorrelationProperty> currCpList, Lookup lookup) {
        assert correlationSet != null;
        //
        String dialogTitle = NbBundle.getMessage(
                FormBundle.class, "DLG_ChoosePropertyTitle"); // NOI18N
        //
        final CorrelationPropertyChooserPanel propChooser =
                new CorrelationPropertyChooserPanel();
        //
        // Construct a validation extension which is intended to prevent
        // duplicate Properties in the CorrelationSet.
        ValidationExtension validationExt = new ValidationExtension() {
            public Validator getExtensionValidator() {
                Validator validator = new DefaultValidator(
                        propChooser, ErrorMessagesBundle.class) {
                    public void doFastValidation() {
                    }
                    
                    @Override
                    public void doDetailedValidation() {
                        Set<CorrelationProperty> newCpSet =
                                propChooser.getSelectedValue();
                        //
                        for (CorrelationProperty newCp : newCpSet) {
                            if (newCp == null) {
                                continue;
                            }
                            //
                            if (currCpList.contains(newCp)) {
                                addReasonKey(Severity.ERROR, 
                                        "ERR_NOT_UNIQUE_CORR_PROP", 
                                        newCp.getName()); // NOI18N
                            }
                        }
                    }
                    
                };
                return validator;
            }
        };
        lookup = new ExtendedLookup(lookup, validationExt);
        propChooser.setLookup(lookup);
        //
        TreeNodeChooser chooser = new TreeNodeChooser(propChooser);
        chooser.initControls();
        //
        DefaultDialogDescriptor descriptor = 
                new DefaultDialogDescriptor(chooser, dialogTitle);
        
        Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        //
        Set<CorrelationProperty> cpSet = null;
        if (descriptor.isOkHasPressed()) {
            cpSet = propChooser.getSelectedValue();
            //
            for (CorrelationProperty property : cpSet) {
                if (correlationSet != null && property != null) {
                    BpelModel bpelModel = correlationSet.getBpelModel();
                    WSDLModel wsdlModel = property.getModel();
                    if (bpelModel != null && wsdlModel != null){
                        new ImportRegistrationHelper(bpelModel).addImport(wsdlModel);
                    }
                }
            }
        }
        return cpSet;
    }
    
    
}
