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

package org.netbeans.modules.bpel.mapper.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.ExtensibleElements;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.model.ext.editor.api.Editor;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperties;
import org.netbeans.modules.bpel.model.ext.editor.api.NMProperty;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class PropertiesNode implements PropertiesConstants {
    private AbstractVariableDeclaration variable;
    private Map<Object, List<Object>> childrenMap = null;
    private boolean leftTreeFlag;
    
    public PropertiesNode(AbstractVariableDeclaration variable, 
            boolean leftTreeFlag) 
    {
        this.variable = variable;
        this.leftTreeFlag = leftTreeFlag;
    }

    public static String getDisplayName() {
        return NbBundle.getMessage(PropertiesNode.class, "PROPERTIES_NODE");
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    public boolean isMessageTypeVariable() {
        WSDLReference<Message> messageRef = variable.getMessageType();
        return (messageRef != null) && (messageRef.get() != null);
    }
    
    public List<Object> getChildren(BpelDesignContext context) {
        return getChildren(context, this);
    }
    
    public List<Object> getChildren(BpelDesignContext context, Object parent) {
        if (childrenMap == null) {
            loadChildren(context);
        }
        return childrenMap.get(parent);
    }
    
    private void loadChildren(BpelDesignContext context) {
        BpelModel bpelModel = context.getBpelModel();
        Process process = bpelModel.getProcess();
        
        Import[] imports = process.getImports();
        
        Message message = getMessage(variable);
        GlobalType type = getType(variable);
        GlobalElement element = getElement(variable);

        Set<CorrelationProperty> allProperties 
                = new HashSet<CorrelationProperty>();
        
        Set<CorrelationProperty> filteredProperties 
                = new HashSet<CorrelationProperty>();

        if (message != null || type != null || element != null) {
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
                
                List<CorrelationProperty> properties = definitions
                        .getExtensibilityElements(CorrelationProperty.class);
                
                if (properties != null) {
                    allProperties.addAll(properties);
                }
                
                List<PropertyAlias> propertyAliases = definitions
                        .getExtensibilityElements(PropertyAlias.class);

                if (propertyAliases == null) {
                    continue;
                }
                
                if (message != null) {
                    for (PropertyAlias propertyAlias : propertyAliases) {
                        Message propertyAliasMessage 
                                = getMessage(propertyAlias);
                        
                        if (propertyAlias.getAnyAttribute(
                                PropertiesConstants.NM_PROPERTY_QNAME) != null)
                        {
                            if (propertyAliasMessage == null 
                                    || message == propertyAliasMessage) 
                            {
                                addProperty(filteredProperties, propertyAlias);
                            }
                        } else if (message == propertyAliasMessage) {
                            addProperty(filteredProperties, propertyAlias);
                        }
                    }
                } else if (type != null) {
                    for (PropertyAlias propertyAlias : propertyAliases) {
                        if (propertyAlias.getAnyAttribute(
                                PropertiesConstants.NM_PROPERTY_QNAME) != null)
                        {
                            continue;
                        }
                        
                        if (type == getType(propertyAlias)) {
                            addProperty(filteredProperties, propertyAlias);
                        }
                    }
                } else if (element != null) {
                    for (PropertyAlias propertyAlias : propertyAliases) {
                        if (element == getElement(propertyAlias)) {
                            addProperty(filteredProperties, propertyAlias);
                        }
                    }
                }
            }
        }
        
        filteredProperties.retainAll(allProperties);
        
        Map<String, CorrelationProperty> sortedProperties 
                = new TreeMap<String, CorrelationProperty>();
        
        for (CorrelationProperty property : filteredProperties) {
            String qName = PropertiesUtils.getQName(property, context);
            if (qName != null) {
                sortedProperties.put(qName, property);
            }
        }
        
        List<Object> children = new ArrayList<Object>(sortedProperties.values());
        
        if (isMessageTypeVariable()) {
            children.addAll(getNMProperties());
        }
        
        childrenMap = new HashMap<Object, List<Object>>();
        childrenMap.put(this, children);
        
        if (isMessageTypeVariable()) {
            FileSystem fileSystem = Repository.getDefault()
                    .getDefaultFileSystem();
            FileObject nmPropertiesFileObject = fileSystem
                    .findResource(PropertiesNode.ROOT_FOLDER);

            if (nmPropertiesFileObject != null) {
                FileObject[] fileObjectChildren = nmPropertiesFileObject
                        .getChildren();

                if (fileObjectChildren != null) {
                    for (FileObject fileObjectChild : fileObjectChildren) {
                        loadChildren(fileObjectChild, childrenMap);
                        children.add(fileObjectChild);
                    }
                }
            }
        }
    }
    
    private List<NMProperty> getNMProperties() {
        List<NMProperty> result = new ArrayList<NMProperty>();

        if (!(variable instanceof ExtensibleElements)) {
            return result;
        }

        ExtensibleElements extenstible = (ExtensibleElements) variable;
        List<Editor> editorsList = extenstible.getChildren(Editor.class);

        if (editorsList == null || editorsList.isEmpty()) {
            return result;
        }

        Editor editor = editorsList.get(0);
        if (editor == null) {
            return result;
        }
    
        NMProperties nmProperties = editor.getNMProperties();
        if (nmProperties == null) {
            return result;
        }

        NMProperty[] nmPropertyArray = nmProperties.getNMProperties();
        if (nmPropertyArray == null || nmPropertyArray.length == 0) {
            return result;
        }

        if (leftTreeFlag) {
            for (NMProperty nmProperty : nmPropertyArray) {
                String nmPropertyName = nmProperty.getNMProperty();
                if (nmProperty.getSource() == Source.FROM 
                        && nmPropertyName != null 
                        && nmPropertyName.trim().length() > 0) {
                     result.add(nmProperty);
                }
            }
        } else {
            for (NMProperty nmProperty : nmPropertyArray) {
                String nmPropertyName = nmProperty.getNMProperty();
                if (nmProperty.getSource() == Source.TO 
                        && nmPropertyName != null 
                        && nmPropertyName.trim().length() > 0) {
                    result.add(nmProperty);
                }
            }
        }
        
        return result;
    }
    
    private void loadChildren(FileObject parentFile, 
            Map<Object, List<Object>> childrenMap) 
    {
        List<Object> children = null;
        
        if (parentFile.isFolder()) {
            FileObject[] fileObjectChildren = parentFile.getChildren();
            if (fileObjectChildren != null) {
                for (FileObject fileObjectChild : fileObjectChildren) {
                    children = addChild(children, fileObjectChild);
                    loadChildren(fileObjectChild, childrenMap);
                }
            }
        }
        
        if (children != null) {
            childrenMap.put(parentFile, children);
        }
    }
    
    private static List<Object> addChild(List<Object> children, Object child) {
        if (children == null) {
            children = new ArrayList<Object>();
        }
        children.add(child);
        return children;
    }
    
    private static Message getMessage(AbstractVariableDeclaration variable) {
        WSDLReference<Message> messageRef = variable.getMessageType();
        return (messageRef == null) ? null : messageRef.get();
    }
    
    private static GlobalType getType(AbstractVariableDeclaration variable) {
        SchemaReference<GlobalType> typeRef = variable.getType();
        return (typeRef == null) ? null : typeRef.get();
    }
    
    private static GlobalElement getElement(AbstractVariableDeclaration variable) {
        SchemaReference<GlobalElement> elementRef = variable.getElement();
        return (elementRef == null) ? null : elementRef.get();
    }
    
    private static Message getMessage(PropertyAlias propertyAlias) {
        NamedComponentReference<Message> reference 
                = propertyAlias.getMessageType();
        return (reference == null) ? null : reference.get();
    }
    
    private static GlobalElement getElement(PropertyAlias propertyAlias) {
        NamedComponentReference<GlobalElement> reference 
                = propertyAlias.getElement();
        return (reference == null) ? null : reference.get();
    }

    private static GlobalType getType(PropertyAlias propertyAlias) {
        NamedComponentReference<GlobalType> reference 
                = propertyAlias.getType();
        return (reference == null) ? null : reference.get();
    }
    
    private static void addProperty(Set<CorrelationProperty> propertiesSet, 
            PropertyAlias propertyAlias)
    {
        NamedComponentReference<CorrelationProperty> reference = propertyAlias
                .getPropertyName();
        
        if (reference != null) {
            CorrelationProperty correlationProperty = reference.get();

            if (correlationProperty != null) {
                propertiesSet.add(correlationProperty);
            }
        }
    }

}
