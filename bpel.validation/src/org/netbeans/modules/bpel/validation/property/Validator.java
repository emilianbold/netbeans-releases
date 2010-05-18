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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.bpel.validation.property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xpath.ext.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.ext.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelHelper;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitorAdapter;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.Copy;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.SimpleBpelModelVisitorAdaptor;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.netbeans.modules.bpel.model.ext.Extensions;
import org.netbeans.modules.bpel.validation.core.BpelValidator;

/**
 * @author anjeleevich
 * @version 2008.10.15
 */
public class Validator extends BpelValidator {

    @Override
    protected SimpleBpelModelVisitor getVisitor() {
        return new PropertiesBpelModelVisitor();
    }
    
    private class PropertiesBpelModelVisitor extends SimpleBpelModelVisitorAdaptor {
        private Map<CorrelationProperty, List<PropertyAlias>> propertyAliasesMap;
        
        @Override
        public void visit(Copy copy) {
            To to = copy.getTo();
            From from = copy.getFrom();
            
            if (to != null) {
                checkTo(copy, to);
            }
            
            if (from != null) {
                checkFrom(copy, from);
            }
        }

        @Override
        public void visit(While whil) {
            checkContent(whil, whil.getCondition());
        }

        @Override
        public void visit(ElseIf elseIf) {
            checkContent(elseIf, elseIf.getCondition());
        }

        @Override
        public void visit(RepeatUntil repeatUntil) {
            checkContent(repeatUntil, repeatUntil.getCondition());
        }

        @Override
        public void visit(If iff) {
            checkContent(iff,iff.getCondition());
        }

        @Override
        public void visit(ForEach forEach) {
            checkContent(forEach, forEach.getStartCounterValue());
            checkContent(forEach, forEach.getFinalCounterValue());
        }
        
        private void checkContent(BpelEntity component, ContentElement contentElement) {
            if (contentElement == null) {
                return;
            }
            String content = contentElement.getContent();

            if (content == null) {
                return;
            }
            content = content.trim();
            Map<VariableDeclaration, Set<CorrelationProperty>> results = extractVariablesAndProperties(component, content);
            
            if (results == null) {
                return;
            }
            
            for (Map.Entry<VariableDeclaration, Set<CorrelationProperty>> entry : results.entrySet()) {
                VariableDeclaration variable = entry.getKey();
                Set<CorrelationProperty> propertiesSet = entry.getValue();

                if (propertiesSet == null) {
                    continue;
                }
                for (CorrelationProperty property : propertiesSet) {
                    checkVariableAndProperty(component, variable, property);
                }
            }
        }
        
        private void checkTo(Copy copy, To to) {
             BpelReference<VariableDeclaration> variableRef = to.getVariable();
             WSDLReference<CorrelationProperty> propertyRef = to.getProperty();
             
             if (variableRef == null) {
                return;
             }
             if (propertyRef == null) {
                return;
             }
             CorrelationProperty property = propertyRef.get();
             VariableDeclaration variable = variableRef.get();
             
             if (property == null) {
                 return;
             }
             if (variable == null) {
                 return;
             }
             checkVariableAndProperty(to, variable, property);
        }
        
        private void checkFrom(Copy copy, From from) {
             BpelReference<VariableDeclaration> variableRef = from.getVariable();
             String content = from.getContent();
             
             if (content != null && content.trim().length() > 0) {
                 checkContent(copy, from);
             }
             else {
                 WSDLReference<CorrelationProperty> propertyRef = from.getProperty();

                 if (variableRef == null) {
                    return;
                 }
                 if (propertyRef == null) {
                     return;
                 }
                 CorrelationProperty property = propertyRef.get();
                 VariableDeclaration variable = variableRef.get();

                 if (property == null) {
                     return;
                 }
                 if (variable == null) {
                    return;
                 }
                 checkVariableAndProperty(from, variable, property);
             }
        }
        
        private void checkVariableAndProperty(BpelEntity bpelEntity, VariableDeclaration variable, CorrelationProperty property) {
            collectPropertyAliases(bpelEntity);
            WSDLReference<Message> messageRef = variable.getMessageType();

            if (messageRef != null) {
                Message message = messageRef.get();

                if (message != null) {
                    checkPropertyAlias(bpelEntity, property, message);
                    return;
                }
            }
            SchemaReference<GlobalType> globalTypeRef = variable.getType();

            if (globalTypeRef != null) {
                GlobalType globalType = globalTypeRef.get();
            
                if (globalType != null) {
                    checkPropertyAlias(bpelEntity, property, globalType);
                    return;
                }
            }
            SchemaReference<GlobalElement> globalElementRef = variable.getElement();

            if (globalElementRef != null) {
                GlobalElement globalElement = globalElementRef.get();

                if (globalElement != null) {
                    checkPropertyAlias(bpelEntity, property, globalElement);
                    return;
                }
            }
        }
        
        private void collectPropertyAliases(BpelEntity bpelEntity) {
            if (propertyAliasesMap != null) {
                return;
            }
            propertyAliasesMap = new HashMap<CorrelationProperty, List<PropertyAlias>>();
            BpelModel model = bpelEntity.getBpelModel();

            if (model == null) {
                return;
            }
            Process process = model.getProcess();

            if (process == null) {
                return;
            }
            Import[] imports = process.getImports();

            if (imports == null || imports.length == 0) {
                return;
            }
            for (Import i : imports) {
                if (!i.getImportType().equals(Import.WSDL_IMPORT_TYPE)) {
                    continue;
                }
                WSDLModel wsdlModel = ImportHelper.getWsdlModel(i);

                if (wsdlModel == null) {
                    continue;
                }
                Definitions definitions = wsdlModel.getDefinitions();

                if (definitions == null) {
                    continue;
                }
                List<PropertyAlias> wsdlPropertyAliases = definitions.getExtensibilityElements(PropertyAlias.class);

                if (wsdlPropertyAliases == null) {
                    continue;
                }
                for (PropertyAlias propertyAlias : wsdlPropertyAliases) {
                    NamedComponentReference<CorrelationProperty> propertyRef = propertyAlias.getPropertyName();

                    if (propertyRef == null) {
                        continue;
                    }
                    CorrelationProperty property = propertyRef.get();

                    if (property == null) {
                        continue;
                    }
                    List<PropertyAlias> propertyAliasesList = propertyAliasesMap.get(property);

                    if (propertyAliasesList == null) {
                        propertyAliasesList = new ArrayList<PropertyAlias>();
                        propertyAliasesMap.put(property, propertyAliasesList);
                    }
                    propertyAliasesList.add(propertyAlias);
                }
            }
        }
        
        private void checkPropertyAlias(Component component, CorrelationProperty property, Message message) {
            List<PropertyAlias> propertyAliasesList = propertyAliasesMap.get(property);
            
            if (isPropertyAliasesListEmpty(component, property, propertyAliasesList)) {
                return;
            }
            for (PropertyAlias propertyAlias : propertyAliasesList) {
                String nmProperty = propertyAlias.getAnyAttribute(NM_PROPERTY_QNAME);
                NamedComponentReference<Message> propertyAliasMessageRef = propertyAlias.getMessageType();
                Message propertyAliasMessage = (propertyAliasMessageRef == null) ? null : propertyAliasMessageRef.get();
                
                if (nmProperty != null) {
                    if (propertyAliasMessage == null) {
                        return;
                    }
                }
                if (propertyAliasMessage == null) {
                    continue;
                }
                if (propertyAliasMessage == message) {
                    return;
                }
            }
            addError("FIX_NoAppropriatePropertyAliasForCorrelationProperty", component, property.getName()); // NOI18N
        }
        
        private void checkPropertyAlias(Component component, CorrelationProperty property, GlobalType type) {
            List<PropertyAlias> propertyAliasesList = propertyAliasesMap.get(property);
            
            if (isPropertyAliasesListEmpty(component, property, propertyAliasesList)) {
                return;
            }
            for (PropertyAlias propertyAlias : propertyAliasesList) {
                NamedComponentReference<GlobalType> propertyAliasTypeRef = propertyAlias.getType();

                if (propertyAliasTypeRef == null) {
                    continue;
                }
                GlobalType propertyAliasType = propertyAliasTypeRef.get();

                if (propertyAliasType == null) {
                    continue;
                }
                if (propertyAliasType == type) {
                    return;
                }
            }
            addError("FIX_NoAppropriatePropertyAliasForCorrelationProperty", component, property.getName()); // NOI18N
        }
        
        private void checkPropertyAlias(Component component, CorrelationProperty property, GlobalElement element) {
            List<PropertyAlias> propertyAliasesList = propertyAliasesMap.get(property);
            
            if (isPropertyAliasesListEmpty(component, property, propertyAliasesList)) {
                return;
            }
            for (PropertyAlias propertyAlias : propertyAliasesList) {
                NamedComponentReference<GlobalElement> propertyAliasElementRef = propertyAlias.getElement();

                if (propertyAliasElementRef == null) {
                    continue;
                }
                GlobalElement propertyAliasElement = propertyAliasElementRef.get();

                if (propertyAliasElement == null) {
                    continue;
                }
                if (propertyAliasElement == element) {
                    return;
                }
            }
            addError("FIX_NoAppropriatePropertyAliasForCorrelationProperty", component, property.getName()); // NOI18N
        }
        
        private boolean isPropertyAliasesListEmpty(Component component, CorrelationProperty property, List<PropertyAlias> propertyAliasesList) {
            if (propertyAliasesList == null || propertyAliasesList.isEmpty()) {
                addError("FIX_NoPropertyAliasForCorrelationProperty", component, property.getName()); // NOI18N
                return true;
            }
            return false;
        }
        
        private Map<VariableDeclaration, Set<CorrelationProperty>> extractVariablesAndProperties(BpelEntity entity, String content) {
            if (content == null) {
                return null;
            }
            content = content.trim();
            
            if (!content.contains("getVariableProperty")) { // NOI18N
                return null;
            }
            XPathModel xPathModel = XPathModelHelper.getInstance().newXPathModel();
            XPathExpression expression = null;
            
            try {
                expression = xPathModel.parseExpression(content);
            } catch (XPathException e) {
                return null;
            }
            if (expression == null) {
                return null;
            }
            XPathVariableAndPropertyFinder finder = new XPathVariableAndPropertyFinder(entity);
            expression.accept(finder);
            
            return finder.getResult();
        }
    }
    
    private static final QName NM_PROPERTY_QNAME = new QName(Extensions.NM_PROPERTY_EXT_URI, "nmProperty"); // NOI18N
    
    private class XPathVariableAndPropertyFinder extends XPathVisitorAdapter {
        private BpelEntity bpelEntity;
        private Map<VariableDeclaration, Set<CorrelationProperty>> result;
        
        XPathVariableAndPropertyFinder(BpelEntity entity) {
            bpelEntity = entity;
        }
        
        public Map<VariableDeclaration, Set<CorrelationProperty>> getResult() {
            return result;
        }
        
        @Override
        public void visit(XPathCoreFunction coreFunction) {
            visitChildren(coreFunction);
        }

        @Override
        public void visit(XPathCoreOperation coreOperation) {
            visitChildren(coreOperation);
        }

        @Override
        public void visit(XPathExtensionFunction extensionFunction) {
            QName qname = extensionFunction.getName();
            int childCount = extensionFunction.getChildCount();

            if (qname != null && childCount == 2) {
                String localPart = qname.getLocalPart();
            
                if (localPart != null && localPart.trim().equals("getVariableProperty")) { // NOI18N
                    XPathExpression arg1 = extensionFunction.getChild(0);
                    XPathExpression arg2 = extensionFunction.getChild(1);
                    
                    String variableName = ((arg1 != null) && (arg1 instanceof XPathStringLiteral)) ? ((XPathStringLiteral) arg1).getValue() : null;
                    String propertyName = ((arg2 != null) && (arg2 instanceof XPathStringLiteral)) ? ((XPathStringLiteral) arg2).getValue() : null;
                    VariableDeclaration variableDeclaration = getVariableDeclaration(bpelEntity, variableName);
                    CorrelationProperty property = getProperty(bpelEntity, propertyName);
                    addResult(variableDeclaration, property);
                }
            }
            visitChildren(extensionFunction);
        }
        
        private void addResult(VariableDeclaration variableDeclaration, CorrelationProperty property) {
            if (variableDeclaration == null) {
                return;
            }
            if (property == null) {
                return;
            }
            if (result == null) {
                result = new HashMap<VariableDeclaration, Set<CorrelationProperty>>();
            }
            Set<CorrelationProperty> set = result.get(variableDeclaration);

            if (set == null) {
                set = new HashSet<CorrelationProperty>();
                result.put(variableDeclaration, set);
            }
            set.add(property);
        }
    }
    
    private static VariableDeclaration getVariableDeclaration(BpelEntity bpelEntity, String variableName) {
        return new VisibilityScope(bpelEntity).lookForVariable(variableName);
    }
    
    private CorrelationProperty getProperty(BpelEntity bpelEntity, String propertyQName) {
        if (propertyQName == null) {
            addError("Property name is not specified", bpelEntity); // NOI18N
            return null;
        }
        propertyQName = propertyQName.trim();

        if (propertyQName.length() == 0) {
            addError("FIX_PropertyNameIsNotSpecified", bpelEntity); // NOI18N
            return null;
        }
        int index = propertyQName.indexOf(':'); // NOI18N

        if (index < 0) {
            addError("FIX_PropertyNameIsNotValidQName", bpelEntity, propertyQName); // NOI18N
            return null;
        }
        String prefix = propertyQName.substring(0, index).trim();
        String name = propertyQName.substring(index + 1).trim();
        
        if (prefix.length() == 0 || name.length() == 0) {
            addError("FIX_PropertyNameIsNotValidQName", bpelEntity, propertyQName); // NOI18N
            return null;
        }
        String namespace = bpelEntity.getNamespaceContext().getNamespaceURI(prefix);

        if (namespace == null) {
            addError("FIX_UnableToFindNamespaceForPrefix", bpelEntity, prefix); // NOI18N
            return null;
        }
        namespace = namespace.trim();

        if (namespace.length() == 0) {
            return null;
        }
        BpelModel bpelModel = bpelEntity.getBpelModel();

        if (bpelModel == null) {
            return null;
        }
        Process process = bpelModel.getProcess();

        if (process == null) {
            return null;
        }
        Import[] imports = process.getImports();

        if (imports == null || imports.length == 0) {
            return null;
        }
        for (Import i : imports) {
            if (!Import.WSDL_IMPORT_TYPE.equals(i.getImportType())) {
                continue;
            }
            if (!namespace.equals(i.getNamespace())) {
                continue;
            }
            WSDLModel wsdlModel = ImportHelper.getWsdlModel(i);

            if (wsdlModel == null) {
                continue;
            }
            Definitions definitions = wsdlModel.getDefinitions();

            if (definitions == null) {
                continue;
            }
            List<CorrelationProperty> wsdlCorrelationProperties = definitions.getExtensibilityElements(CorrelationProperty.class);

            if (wsdlCorrelationProperties == null) {
                continue;
            }
            for (CorrelationProperty correlationProperty : wsdlCorrelationProperties) {
                if (name.equals(correlationProperty.getName())) {
                    return correlationProperty;
                }
            }
        }
        addError("FIX_CorrelationPropertyIsNotDeclared", bpelEntity, propertyQName); // NOI18N
        return null;
    }
}
