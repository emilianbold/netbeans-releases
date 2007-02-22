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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.view.BindingConfigurationDialogPanel;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.localized.LocalizedTemplate;
import org.netbeans.modules.xml.wsdl.ui.view.wizard.localized.LocalizedTemplateGroup;
import org.netbeans.modules.xml.wsdl.ui.wizard.BindingGenerator;
import org.netbeans.modules.xml.wsdl.ui.wizard.WizardBindingConfigurationStep;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.NewType;

public class BindingAndServiceNewType extends NewType {
    private PortType mPortType;
    
    public BindingAndServiceNewType(WSDLComponent portType) {
        mPortType = (PortType) portType;
    }
    
    
    @Override
    public String getName() {
        return NbBundle.getMessage(BindingAndServiceNewType.class, "LBL_NewType_BindingAndService");
    }
    
    
    @Override
    public void create() throws IOException {
        final WSDLModel model = mPortType.getModel();
        FileObject fo = (FileObject) model.getModelSource().getLookup().lookup(FileObject.class);
        final BindingConfigurationDialogPanel panel = new BindingConfigurationDialogPanel(model);
        String bindingName = mPortType.getName() + "Binding";
        NameGenerator nameGen = NameGenerator.getInstance();
        if (nameGen.isBindingExists(bindingName, model)) {
            bindingName = nameGen.generateUniqueBindingName(bindingName, model);
        }
        panel.setBindingName(bindingName);
        
        
        String svcName = fo.getName() + "Service";
        Definitions def = model.getDefinitions();
        Collection<Service> services = def.getServices();
        Service service = null;
        if (services != null && !services.isEmpty()) {
            service = services.iterator().next();
            svcName = service.getName();
        }
        
        panel.setServiceName(svcName);
        String portName = panel.getBindingName() + "Port";
        if (service != null) {
            if (nameGen.isServicePortExists(portName, service)) {
                portName = nameGen.generateUniqueServicePortName(portName, service);
            }
        }
        panel.setServicePortName(portName);
        
        
        final DialogDescriptor descriptor = new DialogDescriptor(panel , "Generate Binding and Service Port", true, null);
        final PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getSource()== panel && evt.getPropertyName().equals(BindingConfigurationDialogPanel.APPLY_CHANGE)) {
                    descriptor.setValid(((Boolean) evt.getNewValue()).booleanValue());
                }
            }
        };
        panel.addPropertyChangeListener(pcl);
        // dialog's action listener
        ActionListener al = new ActionListener() {
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION) ||
                        evt.getSource().equals(DialogDescriptor.CANCEL_OPTION) ||
                        evt.getSource().equals(DialogDescriptor.CLOSED_OPTION)) {
                    panel.removePropertyChangeListener(pcl);
                }
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    String bindName = panel.getBindingName();
                    LocalizedTemplateGroup bindingType = panel.getBindingType();
                    //this could be null for a binding which does not have a sub type
                    LocalizedTemplate bindingSubType = panel.getBindingSubType();
                    String serviceName = panel.getServiceName();
                    String servicePortName = panel.getServicePortName();
                    Map configurationMap = new HashMap();
                    
                    configurationMap.put(WizardBindingConfigurationStep.BINDING_NAME, bindName);
                    configurationMap.put(WizardBindingConfigurationStep.BINDING_TYPE, bindingType);
                    
                    
                    configurationMap.put(WizardBindingConfigurationStep.BINDING_SUBTYPE, bindingSubType);
                    
                    //service and port
                    
                    configurationMap.put(WizardBindingConfigurationStep.SERVICE_NAME, serviceName);
                    configurationMap.put(WizardBindingConfigurationStep.SERVICEPORT_NAME, servicePortName);
                    model.startTransaction();
                    BindingGenerator generator = new BindingGenerator(model, mPortType, configurationMap);
                    generator.execute();
                    Binding binding = generator.getBinding();
                    Port port = generator.getPort();
                    
                    String targetNamespace = model.getDefinitions().getTargetNamespace(); 
                    if(binding != null) {
                        bindingSubType.getMProvider().postProcess(targetNamespace, binding);
                    }
                    if(port != null) {
                        bindingSubType.getMProvider().postProcess(targetNamespace, port);
                    }
                    
                    model.endTransaction();
                    ActionHelper.selectNode(binding);
                }
            }
        };
        descriptor.setButtonListener(al);
        descriptor.setValid(true);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        dialog.toFront();
    }
    
}
