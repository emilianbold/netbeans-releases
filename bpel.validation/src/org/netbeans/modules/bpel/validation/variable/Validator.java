/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.bpel.validation.variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.validation.core.Expression;

import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitorAdaptor;
import org.netbeans.modules.bpel.validation.core.BpelValidator;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.03.03
 */
public final class Validator extends BpelValidator {

    @Override
    protected SimpleBpelModelVisitor getVisitor() {
        return new SimpleBpelModelVisitorAdaptor() {

            // # 147313
            @Override
            public void visit(Copy copy) {
                From from = copy.getFrom();

                if (from == null) {
                    return;
                }
                BpelReference<PartnerLink> ref1 = from.getPartnerLink();

                if (ref1 == null) {
                    return;
                }
                if (ref1.get() == null) {
                    return;
                }
//out();
//out();
//out("SEE: " + getName(copy.getParent()));

                if (from.getEndpointReference() == null) {
//out("  ret 1");
                    return;
                }
                To to = copy.getTo();

                if (to == null) {
//out("  ret 2");
                    return;
                }
                BpelReference<VariableDeclaration> ref2 = to.getVariable();

                if (ref2 == null) {
//out("  ret 3");
                    return;
                }
                VariableDeclaration declaration = ref2.get();

                if (declaration == null) {
//out("  ret 4");
                    return;
                }
                if ( !(declaration instanceof Variable)) {
//out("  ret 5");
                    return;
                }
                WSDLReference<Part> ref3 = to.getPart();
                Component type;

                if (ref3 != null && ref3.get() != null) {
//out("  PART");
                    type = getPartType(ref3.get());
                }
                else {
//out("  VARIABLE");
                    type = getVariableType((Variable) declaration);
                }
                if (checkServiceRefType(type)) {
                    return;
                }
                if (checkServiceRefElement((Variable) declaration)) {
                    return;
                }
//out("ERROR: type is not serviceref");
                addError("FIX_PartnerLink_mapped_to_Variable", copy, getName(copy.getParent())); // NOI18N
            }

            // # 178356
            private boolean checkServiceRefElement(Variable variable) {
//out();
//out("CHECK E: ");
                SchemaReference<GlobalElement> ref = variable.getElement();

                if (ref == null) {
                    return false;
                }
                return checkServiceRef(ref.get(), "service-ref"); // NOI18N
            }

            private boolean checkServiceRefType(Component component) {
//out();
//out("CHECK T: " + component.getClass().getName() + " ... " + getName(component));
                if ( !(component instanceof GlobalType)) {
                    return false;
                }
                return checkServiceRef((GlobalType) component, "ServiceRefType"); // NOI18N
            }

            private boolean checkServiceRef(Named component, String name) {
                if (component == null) {
                    return false;
                }
//out("name: " + component.getName());
//out("    : " + name);

                if ( !name.equals(component.getName())) {
                    return false;
                }
                Schema schema = ((SchemaComponent) component).getModel().getSchema();

                if (schema != null) {
                    String targetNamespace = schema.getTargetNamespace();

                    if ( !"http://docs.oasis-open.org/wsbpel/2.0/serviceref".equals(targetNamespace)) { // NOI18N
                        return false;
                    }
                }
//out();                 
//out("tns: " + targetNamespace);
//out();          
                return true;
            }

            // # 146846
            @Override
            public void visit(Process process) {
//out();
//out("PROCESS: " + process);
//out();
                checkVariableProperty(process, getWSDLModels(process));
            }

            private List<WSDLModel> getWSDLModels(Process process) {
                List<WSDLModel> models = new ArrayList<WSDLModel>();
                Import[] imports = process.getImports();

                if (imports == null) {
                    return models;
                }
                for (Import imp : imports) {
                    if (imp == null) {
                        continue;
                    }
                    WSDLModel model = ImportHelper.getWsdlModel(imp);

                    if (model == null) {
                        continue;
                    }
                    models.add(model);
                }
                return models;
            }

            private void checkVariableProperty(BpelEntity entity, List<WSDLModel> models) {
                checkVariablePropertyContent(entity, models);
                List<BpelEntity> children = entity.getChildren();

                for (BpelEntity child : children) {
                    checkVariableProperty(child, models);
                }
            }

            private void checkVariablePropertyContent(BpelEntity entity, List<WSDLModel> models) {
                if (!(entity instanceof ContentElement)) {
                    return;
                }
                ContentElement element = (ContentElement) entity;
                String value = element.getContent();

                if (value == null) {
                    return;
                }
                checkVariablePropertyString(entity, removeSpace(value), models);
            }

            private void checkVariablePropertyString(BpelEntity element, String value, List<WSDLModel> models) {
                int k = value.indexOf(":getVariableProperty("); // NOI18N

                if (k == -1) {
                    return;
                }
//out();
//out("VP: " + value);
                value = value.substring(k);
//out("  : " + value);
                int k1 = value.indexOf("','"); // NOI18N

                if (k1 == -1) {
                    return;
                }
                int k2 = value.indexOf("')"); // NOI18N

                if (k2 == -1) {
                    return;
                }
                checkProperty(element, value.substring(k1 + 2 + 1, k2), models);
                checkVariablePropertyString(element, value.substring(k2 + 2), models);
            }

            private void checkProperty(BpelEntity element, String property, List<WSDLModel> models) {
//out("      try: " + property);
                property = removePrefix(property);

                for (WSDLModel model : models) {
                    if (findProperty(property, model)) {
                        return;
                    }
                }
                addError("FIX_Incorrect_Property_Name", element, property); // NOI18N
            }

            private boolean findProperty(String name, WSDLModel model) {
                Definitions definitions = model.getDefinitions();

                if (definitions == null) {
                    return false;
                }
                List<ExtensibilityElement> elements = definitions.getExtensibilityElements();

                if (elements == null) {
                    return false;
                }
                for (ExtensibilityElement element : elements) {
//out("see: " + getName(element) + " " + element);
                    if (!(element instanceof CorrelationProperty)) {
                        continue;
                    }
                    CorrelationProperty property = (CorrelationProperty) element;

                    if (property.getName().equals(name)) {
                        return true;
                    }
                }
                return false;
            }

            private String removePrefix(String value) {
                if (value == null) {
                    return null;
                }
                int k = value.indexOf(":");

                if (k != -1) {
                    value = value.substring(k + 1);
                }
                return value;
            }

            private String removeSpace(String value) {
                StringBuilder builder = new StringBuilder();

                for (int i = 0; i < value.length(); i++) {
                    char ch = value.charAt(i);

                    if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
                        continue;
                    }
                    builder.append(ch);
                }
                return builder.toString();
            }

            // # 94195
            @Override
            public void visit(VariableContainer container) {
                Variable[] variables = container.getVariables();
//out();
//out("WE: " + container.getParent().getClass().getName());

                if (variables == null) {
                    return;
                }
                List<VariablePartInfo> infos = new ArrayList<VariablePartInfo>();

                for (Variable variable : variables) {
                    Iterator<Part> parts = getParts(variable);
                    VariablePartInfo info;

//out();
//out();
                    if (parts == null) {
                        info = new VariablePartInfo(variable, null);
//out("added Info: " + info);

                        // # 149735
                        if (noParts(variable)) {
                            info.setUsages(Usages.INITIALIZED);
                        }
                        if (getVariableType(variable) instanceof GlobalSimpleType) {
                            info.setUsages(Usages.INITIALIZED);
                        }
                        infos.add(info);
                    }
                    else {
                        while (parts.hasNext()) {
                            Part part = parts.next();
                            info = new VariablePartInfo(variable, part.getName());
//out();
//out("added info: " + info);
                            Component type = getPartType(part);
//out("      TYPE: " + getName(type));
//out();

                            if (type instanceof GlobalSimpleType) {
                                info.setUsages(Usages.INITIALIZED);
                            }
                            infos.add(info);
                        }
                    }
                }
                findVariables(container.getParent(), infos);
//out();
                boolean isInitialized;
                boolean isReturned;
                boolean isUsed;

                for (VariablePartInfo info : infos) {
//out("  " + info.getDisplayName());

                    isInitialized = info.isInitialized();
                    isReturned = info.isReturned();
                    isUsed = info.isUsed();

                    Variable variable = info.getVariable();
                    String variableName = variable.getName();
                    String partName = info.getPartName();

                    if ( !isUsed && !isReturned) {
                        if (partName == null) {
                            addWarning("FIX_Variable_not_used_returned", variable, variableName); // NOI18N
                        }
                        else {
                            addWarning("FIX_Variable_Part_not_used_returned", variable, variableName + "." + partName); // NOI18N
                        }
                    }
                    else if ( !isInitialized && isUsed) {
                        if (partName == null) {
                            addError("FIX_Variable_not_initialized_but_used", variable, variableName); // NOI18N
                        }
                        else {
                            addError("FIX_Variable_Part_not_initialized_but_used", variable, variableName + "." + partName); // NOI18N
                        }
                    }
                    else if ( !isInitialized && isReturned) {
                        if (partName == null) {
                            addWarning("FIX_Variable_not_initialized_but_returned", variable, variableName); // NOI18N
                        }
                        else {
                            addWarning("FIX_Variable_Part_not_initialized_but_returned", variable, variableName + "." + partName); // NOI18N
                        }
                    }
                }
            }

            private Iterator<Part> getParts(Variable variable) {
                Collection<Part> parts = getPartCollection(variable);

                if (parts == null) {
                    return null;
                }
                if (parts.size() == 0) {
                    return null;
                }
                return parts.iterator();
            }

            private boolean noParts(Variable variable) {
                Collection<Part> parts = getPartCollection(variable);

                if (parts == null) {
                    return false;
                }
                return parts.size() == 0;
            }

            private Collection<Part> getPartCollection(Variable variable) {
                WSDLReference<Message> ref = variable.getMessageType();

                if (ref == null) {
                    return null;
                }
                Message message = ref.get();

                if (message == null) {
                    return null;
                }
                return message.getParts();
            }

            private void findVariables(BpelEntity entity, List<VariablePartInfo> infos) {
//out("    see: " + getName(entity));
                checkEntity(entity, infos);
                Collection<BpelEntity> children = entity.getChildren();

                for (BpelEntity child : children) {
                    findVariables(child, infos);
                }
            }

            private void checkEntity(BpelEntity entity, List<VariablePartInfo> infos) {
                // initialized
                if (entity instanceof To || entity instanceof Receive || entity instanceof OnMessage) {
                    checkVariableReference((VariableReference) entity, infos, Usages.INITIALIZED);
                }
                if (entity instanceof ContentElement && entity instanceof To) {
                    checkContent((ContentElement) entity, infos, Usages.INITIALIZED);
                }
                if (entity instanceof Invoke) {
                    checkVariableDeclaration(((Invoke) entity).getOutputVariable(), null, infos, Usages.INITIALIZED);
                }
                if (entity instanceof OnEvent) {
                    checkVariable(((OnEvent) entity).getVariableName(), null, infos, Usages.INITIALIZED);
                }
                if (entity instanceof Catch) {
                    checkVariable(((Catch) entity).getFaultVariable(), null, infos, Usages.INITIALIZED);
                }
                // used
                if (entity instanceof From) {
                    checkVariableReference((VariableReference) entity, infos, Usages.USED);
                }
                if (entity instanceof ContentElement && !(entity instanceof To)) {
                    checkContent((ContentElement) entity, infos, Usages.USED);
                }
                if (entity instanceof Throw) {
                    checkVariableDeclaration(((Throw) entity).getFaultVariable(), null, infos, Usages.USED);
                }
                // returned
                if (entity instanceof Reply) {
                    checkVariableReference((VariableReference) entity, infos, Usages.RETURNED);
                }
                if (entity instanceof Invoke) {
                    checkVariableDeclaration(((Invoke) entity).getInputVariable(), null, infos, Usages.RETURNED);
                }
            }

            private void checkContent(ContentElement content, List<VariablePartInfo> infos, Usages usages) {
                String expression = content.getContent();
//out();
//out("=== CHECK content: " + expression);
//out("           usages: " + usages);

                for (VariablePartInfo info : infos) {
//out();
//out("         See INFO: " + info);
                    if ( !Expression.contains(expression, info.getVariable().getName(), info.getPartName())) {
                        continue;
                    }
//out("              SET: " + usages + " " + info);
                    info.setUsages(usages);
                }
            }

            private void checkVariableReference(VariableReference reference, List<VariablePartInfo> infos, Usages usages) {
                if (reference instanceof PartReference) {
                    checkVariableDeclaration(reference.getVariable(), ((PartReference) reference).getPart(), infos, usages);
                }
                else {
                    checkVariableDeclaration(reference.getVariable(), null, infos, usages);
                }
            }

            private void checkVariableDeclaration(BpelReference<VariableDeclaration> varRef, WSDLReference<Part> partRef, List<VariablePartInfo> infos, Usages usages) {
//if (usages == Usages.USED || usages == Usages.RETURNED) out("check V D: " + varRef);
                if (varRef == null) {
                    return;
                }
                VariableDeclaration declaration = varRef.get();
//if (usages == Usages.USED || usages == Usages.RETURNED) out("      V D: " + declaration);

                if (declaration == null) {
                    return;
                }
                String partName = null;

                if (partRef != null && partRef.get() != null && partRef.get().getName() != null) {
                    partName = partRef.get().getName();
                }
                checkVariable(declaration.getVariableName(), partName, infos, usages);
            }

            private void checkVariable(String variableName, String partName, List<VariablePartInfo> infos, Usages usages) {
                if (variableName == null) {
                    return;
                }
//out();
                for (VariablePartInfo info : infos) {
                    if ( !info.getVariable().getName().equals(variableName)) {
                        continue;
                    }
                    if (partName != null && !partName.equals(info.getPartName())) {
                        continue;
                    }
//out(" set ref: " + usages + " " + info);
                    info.setUsages(usages);
                }
            }

            // # 83632
            @Override
            public void visit(Flow flow) {
//out();
//out("Flow: " + flow);
                List<List<VariablePart>> list = new ArrayList<List<VariablePart>>();
                Collection<BpelEntity> children = flow.getChildren();

                for (BpelEntity child : children) {
                    List<VariablePart> variables = new ArrayList<VariablePart>();
                    findVariableParts(child, variables);

                    if (!variables.isEmpty()) {
                        list.add(variables);
                    }
                }
                for (int i = 0; i < list.size(); i++) {
                    for (int j = i + 1; j < list.size(); j++) {
                        VariablePart variablePart = getCommonVariable(list.get(i), list.get(j));

                        if (variablePart != null) {
                            addWarning("FIX_Variable_in_Flow", flow, flow.getName(), variablePart.toString()); // NOI18N
                            break;
                        }
                    }
                }
            }

            private VariablePart getCommonVariable(List<VariablePart> variables1, List<VariablePart> variables2) {
                for (VariablePart variable : variables1) {
                    if (variables2.contains(variable)) {
                        return variable;
                    }
                }
                return null;
            }

            private void findVariableParts(BpelEntity entity, List<VariablePart> variables) {
                if (entity instanceof Scope) {
                    return;
                }
                if (entity instanceof Pick) {
                    return;
                }
                if (entity instanceof Assign) {
                    findVariablePartsInAssign((Assign) entity, variables);
                }
                Collection<BpelEntity> children = entity.getChildren();

                for (BpelEntity child : children) {
                    findVariableParts(child, variables);
                }
            }

            private void findVariablePartsInAssign(Assign assign, List<VariablePart> variables) {
                Collection<Copy> copies = assign.getChildren(Copy.class);

                for (Copy copy : copies) {
                    To to = copy.getTo();
                    BpelReference<VariableDeclaration> ref = to.getVariable();

                    if (ref == null) {
                        continue;
                    }
                    VariableDeclaration declaration = ref.get();

                    if (declaration == null) {
                        continue;
                    }
                    variables.add(new VariablePart(declaration, getPart(to)));
                }
            }

            private Part getPart(To to) {
                WSDLReference<Part> ref = to.getPart();

                if (ref == null) {
                    return null;
                }
                return ref.get();
            }

            // # 135160
            @Override
            public void visit(Assign assign) {
                List<Copy> copies = list(assign.getChildren(Copy.class));

                for (int i = 0; i < copies.size(); i++) {
                    for (int j = i + 1; j < copies.size(); j++) {
                        checkCopies(copies.get(i), copies.get(j));
                    }
                }
            }

            private void checkCopies(Copy copy1, Copy copy2) {
//out();
//out("see: " + copy1 + " "  + copy2);
                if (checkTo(copy1.getTo(), copy2.getTo())) {
                    if (checkFrom(copy1.getFrom(), copy2.getFrom())) {
                        addError("FIX_duplicate_copies", copy1); // NOI18N
                        addError("FIX_duplicate_copies", copy2); // NOI18N
                    }
                    else {
                        addWarning("FIX_copies_execution", copy1); // NOI18N
                        addWarning("FIX_copies_execution", copy2); // NOI18N
                    }
                }
            }

            private boolean checkTo(To to1, To to2) {
                if (checkContent(to1, to2)) {
                    return true;
                }
                if (checkVariable(to1, to1, to2, to2)) {
                    return true;
                }
                if (checkPartnerLink(to1, to2)) {
                    return true;
                }
                return false;
            }

            private boolean checkPartnerLink(PartnerLinkReference partnerLinkReference1, PartnerLinkReference partnerLinkReference2) {
                // 1
                BpelReference<PartnerLink> partRef1 = partnerLinkReference1.getPartnerLink();

                if (partRef1 == null) {
                    return false;
                }
                PartnerLink partnerLink1 = partRef1.get();

                if (partnerLink1 == null) {
                    return false;
                }
                // 2
                BpelReference<PartnerLink> partRef2 = partnerLinkReference2.getPartnerLink();

                if (partRef2 == null) {
                    return false;
                }
                PartnerLink partnerLink2 = partRef2.get();

                if (partnerLink2 == null) {
                    return false;
                }
                return partnerLink1.equals(partnerLink2);
            }

            private boolean checkVariable(VariableReference variableReference1, PartReference partReference1, VariableReference variableReference2, PartReference partReference2) {
                // 1
                BpelReference<VariableDeclaration> varRef1 = variableReference1.getVariable();

                if (varRef1 == null) {
                    return false;
                }
                VariableDeclaration variable1 = varRef1.get();

                if (variable1 == null) {
                    return false;
                }
                WSDLReference<Part> partRef1 = partReference1.getPart();

                if (partRef1 == null) {
                    return false;
                }
                Part part1 = partRef1.get();

                if (part1 == null) {
                    return false;
                }
                // 2
                BpelReference<VariableDeclaration> varRef2 = variableReference2.getVariable();

                if (varRef2 == null) {
                    return false;
                }
                VariableDeclaration variable2 = varRef2.get();

                if (variable2 == null) {
                    return false;
                }
                WSDLReference<Part> partRef2 = partReference2.getPart();

                if (partRef2 == null) {
                    return false;
                }
                Part part2 = partRef2.get();

                if (part2 == null) {
                    return false;
                }
                return variable1.equals(variable2) && part1.equals(part2);
            }

            private boolean checkContent(ContentElement content1, ContentElement content2) {
                if (content1 == content2) {
                    return true;
                }
                if (content1 == null || content2 == null) {
                    return false;
                }

                String value1 = content1.getContent();

                if (value1 == null || value1.length() == 0) {
                    return false;
                }
                String value2 = content2.getContent();

                if (value2 == null || value2.length() == 0) {
                    return false;
                }
//out();
//out("value1: " + value1);
//out("value2: " + value2);
                return value1.equals(value2);
            }

            private boolean checkFrom(From from1, From from2) {
                if (checkContent(from1, from2)) {
                    return true;
                }
                if (checkVariable(from1, from1, from2, from2)) {
                    return true;
                }
                if (checkPartnerLink(from1, from2)) {
                    return true;
                }
                return false;
            }

            private List<Copy> list(Collection<Copy> collection) {
                List<Copy> list = new ArrayList<Copy>();

                if (collection == null) {
                    return list;
                }
                for (Copy copy : collection) {
                    list.add(copy);
                }
                return list;
            }

            @Override
            public void visit(OnMessage onMessage) {
                checkVariable(onMessage, onMessage, true);
            }

            @Override
            public void visit(Receive receive) {
                checkVariable(receive, receive, true);
            }

            @Override
            public void visit(Reply reply) {
                checkVariable(reply, reply, false);
            }

            // # 116242
            private void checkVariable(VariableReference variableReference, OperationReference operationReference, boolean isInput) {
                BpelReference<VariableDeclaration> ref2 = variableReference.getVariable();

                if (ref2 != null && ref2.get() != null) {
                    return;
                }
//out("NO VARIABLE");
                WSDLReference<Operation> ref = operationReference.getOperation();

                if (ref == null) {
                    return;
                }
                Operation operation = ref.get();

                if (operation == null) {
                    return;
                }
                OperationParameter parameter;

                if (isInput) {
                    parameter = operation.getInput();
                }
                else {
                    parameter = operation.getOutput();
                }
                if (parameter == null) {
                    return;
                }
                NamedComponentReference<Message> ref1 = parameter.getMessage();

                if (ref1 == null) {
                    return;
                }
                Message message = ref1.get();

                if (message == null) {
                    return;
                }
                Collection<Part> parts = message.getParts();

                if (parts == null) {
                    return;
                }
//out();
//out("SIZE: " + parts.size());
//out();
//              if (parts.size() != 0) {
//                    addWarning("FIX_SA00047", (Component) variableReference); // NOI18N
//              }
            }
        };
    }

    // -------------------------
    private class VariablePart {

        private VariableDeclaration myVariable;
        private Part myPart;

        VariablePart(VariableDeclaration variable, Part part) {
            myVariable = variable;
            myPart = part;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof VariablePart)) {
                return false;
            }
            VariablePart variablePart = (VariablePart) object;

            return variablePart.myVariable == myVariable && variablePart.myPart == myPart;
        }

        @Override
        public int hashCode() {
            if (myPart == null) {
                return myVariable.hashCode();
            }
            return myVariable.hashCode() * myPart.hashCode();
        }

        @Override
        public String toString() {
            if (myPart == null) {
                return getName(myVariable);
            }
            return getName(myVariable) + "." + myPart.getName(); // NOI18N
        }
    }

    // -----------------------------
    private enum Usages {
        RETURNED, USED, INITIALIZED;
    }

    // -----------------------------
    private class VariablePartInfo {

        VariablePartInfo(Variable variable, String partName) {
            myPartName = partName;
            myVariable = variable;
            myIsUsed = false;
            myIsReturned = false;
            myIsInitialized = false;
        }

        public Variable getVariable() {
            return myVariable;
        }

        public String getPartName() {
            return myPartName;
        }

        public boolean isUsed() {
            return myIsUsed;
        }

        public boolean isReturned() {
            return myIsReturned;
        }

        public boolean isInitialized() {
            return myIsInitialized;
        }

        public void setUsages(Usages usages) {
            if (usages == Usages.USED) {
                myIsUsed = true;
            }
            else if (usages == Usages.RETURNED) {
                myIsReturned = true;
            }
            else if (usages == Usages.INITIALIZED) {
                myIsInitialized = true;
            }
        }

        @Override
        public String toString() {
            return myVariable.getName() + (myPartName == null ? "" : "." + myPartName); // NOI18N
        }

        public String getDisplayName() {
            return toString() + "\t i: " + myIsInitialized + "\t u: " + myIsUsed + "\t r: " + myIsReturned; // NOI18N
        }

        private String myPartName;
        private Variable myVariable;
        private boolean myIsUsed;
        private boolean myIsReturned;
        private boolean myIsInitialized;
    }
}
