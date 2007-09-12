/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.bpel.nodes.actions;

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
                new ImportRegistrationHelper(correlationSet.getBpelModel())
                .addImport(property.getModel());
            }
        }
        //
        correlationSet.setProperties(newCorrPropRefList);
    }
    
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
                    public boolean doFastValidation() {
                        return true;
                    }
                    
                    public boolean doDetailedValidation() {
                        Set<CorrelationProperty> newCpSet =
                                propChooser.getSelectedValue();
                        //
                        for (CorrelationProperty newCp : newCpSet) {
                            if (newCp == null) {
                                continue;
                            }
                            //
                            if (currCpList.contains(newCp)) {
                                addReasonKey("ERR_NOT_UNIQUE_CORR_PROP", newCp.getName()); // NOI18N
                                return false;
                            }
                        }
                        //
                        return true;
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
