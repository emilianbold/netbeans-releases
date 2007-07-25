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
package org.netbeans.modules.websvc.wsitconf.design;

import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.javamodel.ServiceChangeListener;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Grebac
 */
public class WsitServiceChangeListener implements ServiceChangeListener {
    
    Service service;
    FileObject implFile;
    Project project;
    
    public WsitServiceChangeListener(Service service, FileObject implFile, Project project) {
        this.service = service;
        this.implFile = implFile;
        this.project = project;
    }
    
    public void propertyChanged(String propertyName, String oldValue, String newValue) {
        //        System.out.println("receieved propertychangeevent for propertyName="+propertyName);
    }
    
    public void operationAdded(MethodModel method) {
        
        Binding binding = WSITModelSupport.getBinding(service, implFile, project, false, null);
        if (binding != null) {
            BindingOperation bO = getBindingOperation(binding, method.getOperationName());
            if(bO == null){
                bO = Util.generateOperation(binding, Util.getPortType(binding), method.getOperationName(), method.getImplementationClass());
            }
            if (SecurityPolicyModelHelper.isSecurityEnabled(binding)) {
                String profile = ProfilesModelHelper.getSecurityProfile(binding);
                ProfilesModelHelper.setMessageLevelSecurityProfilePolicies(bO, profile);
            }
            WSITModelSupport.save(binding);
        }
    }
    
    private BindingOperation getBindingOperation(Binding binding, String operationName ){
        Collection<BindingOperation> bindingOperations = binding.getBindingOperations();
        for(BindingOperation bindingOperation : bindingOperations){
            if(bindingOperation.getName().equals(operationName)){
                return bindingOperation;
            }
        }
        return null;
    }
    
    public void operationRemoved(MethodModel method) {
        Binding binding = WSITModelSupport.getBinding(service, implFile, project, false, null);
        if (binding != null) {
            WSDLModel model = binding.getModel();
            String methodName = method.getOperationName();
            Definitions d = model.getDefinitions();
            Collection<Message> messages = d.getMessages();
            Collection<BindingOperation> bOperations = binding.getBindingOperations();
            PortType portType = (PortType) d.getPortTypes().toArray()[0];
            Collection<Operation> operations = portType.getOperations();
            
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            
            try {
                for (BindingOperation bOperation : bOperations) {
                    if (methodName.equals(bOperation.getName())) {
                        ProfilesModelHelper.setMessageLevelSecurityProfilePolicies(bOperation, ComboConstants.NONE);
                        binding.removeBindingOperation(bOperation);
                    }
                }
                
                for (Operation o : operations) {
                    if (methodName.equals(o.getName())) {
                        portType.removeOperation(o);
                    }
                }
                
                for (Message m : messages) {
                    if (methodName.equals(m.getName()) || (methodName + "Response").equals(m.getName())) {
                        d.removeMessage(m);
                    }
                }
            } finally {
                if (!isTransaction) {
                    model.endTransaction();
                }
            }
            WSITModelSupport.save(binding);
        }
    }
    
    public void operationChanged(MethodModel oldMethod, MethodModel newMethod) { }
    
}
