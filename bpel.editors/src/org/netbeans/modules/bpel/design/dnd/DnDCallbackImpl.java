/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.design.dnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.design.model.PartnerLinkHelper;
import org.netbeans.modules.bpel.editors.api.dnd.DnDCallback;
import org.netbeans.modules.bpel.model.api.ActivityHolder;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.soa.dndbridge.api.DnDHandler;
import org.netbeans.modules.soa.dndbridge.api.DropResult;
import org.netbeans.modules.soa.dndbridge.api.VariableType;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CompositeActivity;
import org.netbeans.modules.bpel.model.api.ExtendableActivity;
import org.netbeans.modules.bpel.model.api.FaultHandler;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.properties.ImportRegistrationHelper;
import org.netbeans.modules.bpel.properties.editors.WsdlWrapper;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELComponentFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author alexey
 */
public class DnDCallbackImpl implements DnDCallback {

    private BpelModel model;
    private DnDHandler handler;
    private UniqueId entity;

    public DnDCallbackImpl(BpelEntity entity, BpelModel model, DnDHandler handler) {
        this.entity = entity.getUID();
        this.model = model;
        this.handler = handler;

    }

    public void preDrop() {
    }

    public void postDrop() {
//        Scope scope = null;
        Process process = model.getProcess();
        if (process == null) {
            return;
        }
        
//        SwingUtilities.invokeLater(new Runnable() { public void run() {
            
        Invoke invoke = (Invoke) model.getEntity(entity);
        Scope scope = null;
            
        Map<String, VariableType> variableTypesMap 
                = collectVariableTypes(invoke);
        
        Set<String> existingVariableNames 
                = new HashSet<String>(variableTypesMap.keySet());
        List<VariableType> variableTypes = new ArrayList<VariableType>(
                variableTypesMap.values());

        for (int i = variableTypes.size() - 1; i >= 0; i--) {
            if (variableTypes.get(i) == null) {
                variableTypes.remove(i);
            }
        }
        
        if (variableTypes.isEmpty()) {
            // Should we show some error message here? 
            return;
        }
        
        ImportRegistrationHelper irh = new ImportRegistrationHelper(model);
        
        String bpelTargetNamespace = process.getTargetNamespace();
        
        try {
            DropResult res = handler.handleDrop(model.getModelSource(), 
                    variableTypes, bpelTargetNamespace);
            
            if (res == null) {
                throw new CanceledException();
            }
            
            Operation operation = res.getOperation();
            if (operation == null) {
                throw new UnsupportedDropResultException();
            }

            //find an PortType of operation to invoke
            PortType pt = (PortType) res.getOperation().getParent();
            if (pt == null) {
                throw new UnsupportedDropResultException();
            }

            //create new PLT
            PartnerLinkType plt = createPltFor(pt);

            //extract parnter role
            Role partnerRole = plt.getRole1();

            //calculate bpel parner link name
            String partnerLinkName = pt.getName();

            //add imports for wsdls containing out PortType and PartnerLinkType
            irh.addImport(pt.getModel());
            irh.addImport(plt.getModel());

            //add partner link to bpel
            PartnerLinkContainer partnerLinkContainer = process
                    .getPartnerLinkContainer();
            if (partnerLinkContainer == null) {
                partnerLinkContainer = model.getBuilder()
                        .createPartnerLinkContainer();
                process.setPartnerLinkContainer(partnerLinkContainer);
            }
            
            PartnerLink[] partnerLinks = partnerLinkContainer.getPartnerLinks();

            PartnerLink partnerLink = model.getBuilder()
                    .createPartnerLink();
            partnerLink.setPartnerLinkType(partnerLink
                    .createWSDLReference(plt, PartnerLinkType.class));
            partnerLink.setPartnerRole(partnerLink
                    .createWSDLReference(partnerRole, Role.class));
            
            try {
                partnerLink.setName(createPartnerLinkName(partnerLinkName, 
                        partnerLinks));
            } catch (VetoException ex1) {
                // name is invalid
                // let's try default name
                try {
                    partnerLink.setName(createPartnerLinkName(null, 
                            partnerLinks));
                } catch (VetoException ex2) {
                    // never happens
                    // do nothing
                }
            }
            
            partnerLinkContainer.addPartnerLink(partnerLink);

            invoke.setOperation(invoke.createWSDLReference(res.getOperation(), 
                    Operation.class));
            invoke.setPartnerLink(invoke.createReference(partnerLink, 
                    PartnerLink.class));
            invoke.setPortType(invoke.createWSDLReference(pt, PortType.class));
            
            Variable inputVariable = getVariable(irh, process, invoke, 
                    res.getInput(), existingVariableNames, 
                    "inputVariable"); // NOI18N

            if (inputVariable != null) {
                invoke.setInputVariable(invoke.createReference(inputVariable, 
                        VariableDeclaration.class));
            }
            
            Variable outputVariable = getVariable(irh, process, invoke, 
                    res.getOutput(), existingVariableNames, 
                    "outputVariable"); // NOI18N

            if (outputVariable != null) {
                invoke.setOutputVariable(invoke.createReference(
                        outputVariable, VariableDeclaration.class));
            }
            
            if (res.getFault() != null) {
                BpelContainer parent = invoke.getParent();
                
                if (parent instanceof CompositeActivity) {
                    CompositeActivity compositeActivity = (CompositeActivity) 
                            parent;
                    ExtendableActivity[] activities = compositeActivity
                            .getActivities();
                    int index = -1;
                    if (activities != null) {
                        for (int i = 0; i < activities.length; i++) {
                            if (activities[i] == invoke) {
                                index = i;
                                break;
                            }
                        }
                    }
                    
                    if (index > 0) {
                        invoke = (Invoke) invoke.cut();
                        scope = model.getBuilder().createScope();
                        scope.setActivity(invoke);
                        compositeActivity.insertActivity(scope, index);
                    }
                } else if (parent instanceof ActivityHolder) {
                    ActivityHolder activityHolder = (ActivityHolder) parent;
                    invoke = (Invoke) invoke.cut();
                    scope = model.getBuilder().createScope();
                    scope.setActivity(invoke);
                    activityHolder.setActivity(scope);
                } 
                
                if (scope != null) {
                    FaultHandlers faultHandlers = model.getBuilder()
                            .createFaultHandlers();
                    scope.setFaultHandlers(faultHandlers);

                    Catch catchActivity = model.getBuilder().createCatch();
                    faultHandlers.addCatch(catchActivity);

                    Message message = res.getFault().getMessage();
                    String name = calculateNewVariableName(res.getFault()
                            .getVariableName(), "faultVariable", // NOI18N
                            existingVariableNames); 

                    irh.addImport(message.getModel());
                    catchActivity.setFaultMessageType(catchActivity
                            .createWSDLReference(message, Message.class));

                    boolean tryAnotherName = false;
                    try {
                        catchActivity.setFaultVariable(name);
                    } catch (VetoException ex) {
                        tryAnotherName = true;
                        // Invalid name. Will try another name;
                    }

                    if (tryAnotherName) {
                        name = calculateNewVariableName(null, 
                                "faultVariable", existingVariableNames); // NOI18N 
                        try {
                            catchActivity.setFaultVariable(name);
                        } catch (VetoException ex) {
                            tryAnotherName = true;
                            // Invalid name. Will try another name;
                        }
                    }
                }
            }
        } catch (CanceledException ex) {
            if (scope != null) {
                scope.cut();
            } else if (invoke != null) {
                invoke.cut();
            }
        } catch (UnsupportedDropResultException ex) {
            if (scope != null) {
                scope.cut();
            } else if (invoke != null) {
                invoke.cut();
            }
        }
    }
    
    private Map<String, VariableType> collectVariableTypes(Invoke invoke) {
        Map<String, VariableType> result = new HashMap<String, VariableType>();
        
        BpelContainer bpelContainer = invoke.getParent();
        
        while (bpelContainer != null) {
            VariableContainer variableContainer = null;
            
            if (bpelContainer instanceof Scope) {
                variableContainer = ((Scope) bpelContainer)
                        .getVariableContainer();
            } else if (bpelContainer instanceof Process) {
                variableContainer = ((Process) bpelContainer)
                        .getVariableContainer();
            }

           Variable[] variables = (variableContainer == null) ? null
                   : variableContainer.getVariables();
            
            if (variables != null && variables.length > 0) {
                for (Variable variable : variables) {
                    String name = variable.getName();
                    
                    WSDLReference<Message> messageRef = variable
                            .getMessageType();
                    Message message = (messageRef == null) ? null
                            : messageRef.get();
                    
                    if (name != null && name.trim().length() > 0) {
                        VariableType variableType = (message == null) ? null
                                : new MyVariableType(variable);
                        
                        if (!result.containsKey(name)) {
                            result.put(name, variableType);
                        }
                    }
                }
            }
            
            bpelContainer = bpelContainer.getParent();
        }
        
        return result;
    }
            
    
    private Variable getVariable(
            ImportRegistrationHelper importRegistrationHelper, 
            Process process,
            Invoke invoke, 
            VariableType variableType,
            Set<String> existingVariableNames,
            String defaultVariableName) 
    {
        Variable variable = null;
        
        if (variableType instanceof MyVariableType) {
            variable = ((MyVariableType) variableType).getVariable();
        } else if (variableType != null) {
            BpelModel bpelModel = process.getBpelModel();
                    
            String name = calculateNewVariableName(
                    variableType.getVariableName(),
                    defaultVariableName, existingVariableNames);
            existingVariableNames.add(name);
            
            Message message = variableType.getMessage();

            importRegistrationHelper.addImport(message.getModel());
            
            VariableContainer variableContainer = process
                    .getVariableContainer();
            
            if (variableContainer == null) {
                variableContainer = bpelModel.getBuilder()
                        .createVariableContainer();
                process.setVariableContainer(variableContainer);
            }
            
            variable = bpelModel.getBuilder().createVariable();
            variable.setMessageType(variable.createWSDLReference(message, 
                    Message.class));
            
            boolean tryAnotherName = false;
            try {
                variable.setName(name);
            } catch (VetoException ex) {
                // Invalid name. Will try another name.
                tryAnotherName = true;
            }
            
            if (tryAnotherName) {
                name = calculateNewVariableName(null, defaultVariableName, 
                        existingVariableNames);
                try {
                    variable.setName(name);
                } catch (VetoException ex) {
                    // Never happens. Do nothing.
                }
            }
                    
            variableContainer.addVariable(variable);
        }
        
        return variable;
    }
    
    private String calculateNewVariableName(String name, String defaultName,
            Set<String> existingVariableNames) 
    {
        if (name == null) {
            name = defaultName; 
        } else {
            name = name.trim();
            if (name.length() == 0) {
                name = defaultName;
            }
        }
        
        String resultName = null;

        if (!existingVariableNames.contains(name)) {
            resultName = name;
        } else {
            int i = 1;
            do {
                resultName = name + i;
                i++;
            } while (existingVariableNames.contains(resultName));
        }
        
        return resultName;
    }

    private PartnerLinkType createPltFor(PortType pt) {
        FileObject fo = pt.getModel().getModelSource().getLookup().lookup(FileObject.class);

        if (fo == null) {
            return null;
        }

        WsdlWrapper wrapper = new WsdlWrapper(fo.getParent(), fo.getName() + "Wrapper", true);
        WSDLModel wsdlModel = wrapper.getModel();
        Definitions defs = wsdlModel.getDefinitions();

        if (defs == null) {
            return null;
        }

        BPELComponentFactory factory = new BPELComponentFactory(wsdlModel);
        PartnerLinkType plt = factory.createPartnerLinkType(defs);
        //

        try {
            wsdlModel.startTransaction();

            plt.setName(pt.getName() + "PartnerLinkType"); //NOL18N
            //
            defs.addExtensibilityElement(plt);

            Import imp = wsdlModel.getFactory().createImport();
            imp.setLocation(fo.getNameExt());
            imp.setNamespace(pt.getModel().getDefinitions().getTargetNamespace());
            defs.addImport(imp);
            Role role = factory.createRole(imp);
            plt.setRole1(role);
            NamedComponentReference<PortType> pt_ref =
                    role.createReferenceTo(
                    pt, PortType.class);

            role.setPortType(pt_ref);
            role.setName(pt.getName() + "Provider"); //NOL18N

        } finally {
            wsdlModel.endTransaction();
        }
        PartnerLinkHelper.saveModel(wsdlModel);
        
        return plt;
    }
    
    private String createPartnerLinkName(String desiredName, 
            PartnerLink[] parnterLinks) 
    {
        if (desiredName == null) {
            desiredName = DEFAULT_PARNER_LINK_NAME;
        } else {
            desiredName = desiredName.trim();
            if (desiredName.length() == 0) {
                desiredName = DEFAULT_PARNER_LINK_NAME;
            }
        }
        
        if (parnterLinks == null || parnterLinks.length == 0) {
            return desiredName;
        }
        
        String resultName = desiredName;
        
        if (isPartnerLinkNameExists(resultName, parnterLinks)) {
            int index = 0;
            do {
                index++;
                resultName = desiredName + index;
            } while (isPartnerLinkNameExists(resultName, parnterLinks));
        }
        
        return resultName;
    }
    
    private boolean isPartnerLinkNameExists(String name, 
            PartnerLink[] partnerLinks) 
    {
        assert name != null;
        assert name.length() != 0;
        assert partnerLinks != null;
        assert partnerLinks.length != 0;
        
        for (PartnerLink partnerLink : partnerLinks) {
            if (name.equals(partnerLink.getName())) {
                return true;
            }
        }
        return false;
    }

    class MyVariableType extends VariableType {

        Variable var;

        public MyVariableType(Variable var) {
            super(var.getMessageType().get(), var.getName());
            this.var = var;
        }

        private Variable getVariable() {
            return var;
        }
    }

    private static class CanceledException extends Exception {
        CanceledException() {
            super("Wizard was canceled"); // NOI18N
        }
    }
    
    private static class UnsupportedDropResultException extends Exception {
        UnsupportedDropResultException() {
            super("Unsupported drop result"); // NOI18N
        }
    }
    
    private static final String DEFAULT_PARNER_LINK_NAME = "NewPartnerLink";
}
