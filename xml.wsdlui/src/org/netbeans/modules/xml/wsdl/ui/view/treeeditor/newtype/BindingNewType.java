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
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype;

import java.awt.Dialog;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.bindingsupport.template.localized.LocalizedTemplateGroup;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.view.BindingConfigurationDialogPanel;
import org.netbeans.modules.xml.wsdl.ui.wizard.BindingGenerator;
import org.netbeans.modules.xml.wsdl.ui.wizard.WizardBindingConfigurationStep;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

public class BindingNewType extends NewType {

    private WSDLComponent mDef = null;

    public BindingNewType(WSDLComponent def) {
        mDef = def;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(BindingNewType.class, "LBL_NewType_Binding");
    }

    @Override
    public void create() throws IOException {
        WSDLModel model = mDef.getModel();
        
        BindingConfigurationDialogPanel panel = new BindingConfigurationDialogPanel(model);
        final DialogDescriptor descriptor = new DialogDescriptor(panel,
                NbBundle.getMessage(BindingNewType.class, "LBL_Create_Binding"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(BindingNewType.class),
                null);
        panel.setDialogDescriptor(descriptor);


        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
        dlg.setVisible(true);

        if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
            String bindName = panel.getBindingName();
            LocalizedTemplateGroup bindingType = panel.getBindingType();
            //this could be null for a binding which does not have a sub type
            LocalizedTemplate bindingSubType = panel.getBindingSubType();
            String serviceName = panel.getServiceName();
            String servicePortName = panel.getServicePortName();
            Map configurationMap = new HashMap();
            
            boolean createServicePort = panel.canAutoCreateServicePort();

            configurationMap.put(WizardBindingConfigurationStep.BINDING_NAME, bindName);
            configurationMap.put(WizardBindingConfigurationStep.BINDING_TYPE, bindingType);


            configurationMap.put(WizardBindingConfigurationStep.BINDING_SUBTYPE, bindingSubType);

            //service and port
            configurationMap.put(WizardBindingConfigurationStep.SERVICE_NAME, serviceName);
            configurationMap.put(WizardBindingConfigurationStep.SERVICEPORT_NAME, servicePortName);
            configurationMap.put(WizardBindingConfigurationStep.AUTO_CREATE_SERVICEPORT, createServicePort);
            
            PortType pt = panel.getPortType();
            model.startTransaction();
            BindingGenerator generator = new BindingGenerator(model, pt, configurationMap);
            generator.execute();
            Binding binding = generator.getBinding();
            Port port = generator.getPort();

            String targetNamespace = model.getDefinitions().getTargetNamespace();
            if (binding != null) {
                bindingSubType.getMProvider().postProcess(targetNamespace, binding);
            }
            
            if (createServicePort) {
                if (port != null) {
                    bindingSubType.getMProvider().postProcess(targetNamespace, port);
                }
            }

            model.endTransaction();
            ActionHelper.selectNode(binding);
        }
    }

}
