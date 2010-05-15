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

package org.netbeans.modules.bpel.debugger.ui.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.Position;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.SourcePath;
import org.netbeans.modules.bpel.debugger.api.variables.NamedValueHost;
import org.netbeans.modules.bpel.debugger.api.variables.SimpleValue;
import org.netbeans.modules.bpel.debugger.api.variables.SimpleVariable;
import org.netbeans.modules.bpel.debugger.api.variables.Value;
import org.netbeans.modules.bpel.debugger.api.variables.WsdlMessageValue;
import org.netbeans.modules.bpel.debugger.api.variables.WsdlMessageVariable;
import org.netbeans.modules.bpel.debugger.api.variables.XmlElementValue;
import org.netbeans.modules.bpel.debugger.api.variables.XmlElementVariable;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.util.NbBundle;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A helper class which is used to provide various information about BPEL 
 * variables.
 * 
 * @author Kirill Sorokin
 */
public class VariablesUtil {
    private BpelDebugger myDebugger;
    
    public VariablesUtil(
            final BpelDebugger debugger) {
        myDebugger = debugger;
    }
    
    // Display name ////////////////////////////////////////////////////////////
    public String getDisplayName(
            final Object object) {
        if (object instanceof NamedValueHost) {
            return ((NamedValueHost) object).getName();
        }

        if (object instanceof Node) {
            return ((Node) object).getNodeName();
        }

        return NbBundle.getMessage(
                VariablesUtil.class, "VU_CannotResolveDN", object); // NOI18N
    }
    
    // Icon base ///////////////////////////////////////////////////////////////
    public String getIconBase(
            final Object object) {
        
        if (object instanceof NamedValueHost) {
            if (object instanceof WsdlMessageVariable) {
                return WSDL_MESSAGE_VARIABLE_ICON;
            }
            
            if (object instanceof WsdlMessageValue.Part) {
                return WSDL_MESSAGE_PART_ICON;
            }
            
            if (object instanceof XmlElementVariable) {
                return XML_ELEMENT_VARIABLE_ICON;
            }
            
            if (object instanceof SimpleVariable) {
                return SIMPLE_VARIABLE_ICON;
            }
            
            return DEFAULT_VARIABLE_ICON;
        }
        
        if (object instanceof Node) {
            switch (((Node) object).getNodeType()) {
                case Node.ELEMENT_NODE: 
                    return ELEMENT_NODE_ICON;
                    
                case Node.ATTRIBUTE_NODE: 
                    return ATTRIBUTE_NODE_ICON;
                    
                case Node.TEXT_NODE: 
                    return TEXT_NODE_ICON;
                    
                case Node.CDATA_SECTION_NODE: 
                    return CDATA_NODE_ICON;
                    
                default: 
                    return DEFAULT_NODE_ICON;
            }
        }
        
        return DEFAULT_VARIABLE_ICON;
    }
    
    // Children ////////////////////////////////////////////////////////////////
    public Object[] getChildren(
            final Object object) {
        if (object.equals(TreeModel.ROOT)) {
            final ProcessInstance processInstance = 
                    myDebugger.getCurrentProcessInstance();

            if (processInstance != null) {
                return processInstance.getVariables();
            } else {
                return new Object[0];
            }
        }
        
        if (object instanceof NamedValueHost) {
            final Value value = ((NamedValueHost) object).getValue();
            
            if (value != null) {
                return getChildren(value);
            } else {
                return new Object[0];
            }
        }
        
        if (object instanceof Node) {
            return getChildren((Node) object);
        }
        
        return new Object[0];
    }
    
    private Object[] getChildren(
            final Node node) {
        final LinkedList<Object> result = new LinkedList<Object>();
        
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            final Element element = (Element) node;
            final NamedNodeMap attrs = element.getAttributes();
            
            final int length = attrs.getLength();
            for (int i = 0; i < length; i++) {
                final Node item = attrs.item(i);
                if (((item.getPrefix() != null) && 
                        item.getPrefix().equals("xmlns")) ||
                        item.getLocalName().equals("xmlns")) { // NOI18N
                    
                    continue;
                }
                
                result.add(item);
            }
        }
        
        final NodeList nodes = node.getChildNodes();
        final int nodeCount = nodes.getLength();
        if (nodeCount == 1 && 
                nodes.item(0).getNodeType() == Node.TEXT_NODE) {
            // Don't add any children - merge the child text node with 
            // its parent
        } else {
            for (int i = 0; i < nodeCount; i++) {
                final Node child = nodes.item(i);
                if (child.getNodeType() == Node.COMMENT_NODE) {
                    continue;
                }
                
                if (child.getNodeType() == Node.TEXT_NODE) {
                    if ((child.getNodeValue() == null) ||
                            child.getNodeValue().trim().equals("")) {
                        continue;
                    }
                }
                
                result.add(child);
            }
        }
        
        return result.toArray(new Object[result.size()]);
    }
    
    private Object[] getChildren(
            final Value value) {
        if (value instanceof SimpleValue) {
            return new Object[0];
        }
        
        if (value instanceof WsdlMessageValue) {
            return ((WsdlMessageValue) value).getParts();
        }
        
        if (value instanceof XmlElementValue) {
            return getChildren(
                    ((XmlElementValue) value).getElement());
        }
        
        return new Object[0];
    }
    
    // Type ////////////////////////////////////////////////////////////////////
    public String getType(
            final Object object) {
        String type = null;
        
        if (object instanceof NamedValueHost) {
            if (object instanceof WsdlMessageVariable) {
                type = getType((WsdlMessageVariable) object);
            }
            
            if (object instanceof WsdlMessageValue.Part) {
                type = getType((WsdlMessageValue.Part) object);
            }
            
            if (object instanceof XmlElementVariable) {
                type = getType((XmlElementVariable) object);
            }
            
            if (object instanceof SimpleVariable) {
                type = getType((SimpleVariable) object);
            }
            
            if (type == null) {
                return "";
            } else {
                return " " + type + " ";
            }
        }
        
        if (object instanceof Node) {
            type = getType((Node) object);
            
            if (type == null) {
                return "";
            } else {
                return " " + type + " ";
            }
        }
        
        return NbBundle.getMessage(
                VariablesUtil.class, "VU_CannotResolveType", object); // NOI18N
    }
    
    public String getTypeTooltip(
            final Object object) {
        if (object instanceof NamedValueHost) {
            if (object instanceof WsdlMessageVariable) {
                return " " + 
                        getTypeTooltip((WsdlMessageVariable) object) + " ";
            }

            if (object instanceof WsdlMessageValue.Part) {
                return " " + 
                        getTypeTooltip((WsdlMessageValue.Part) object) + " ";
            }

            if (object instanceof XmlElementVariable) {
                return " " + 
                        getTypeTooltip((XmlElementVariable) object) + " ";
            }

            if (object instanceof SimpleVariable) {
                return " " + 
                        getTypeTooltip((SimpleVariable) object) + " ";
            }
        }

        if (object instanceof Node) {
            return " " + getTypeTooltip((Node) object) + " ";
        }

        return NbBundle.getMessage(
                VariablesUtil.class, "VU_CannotResolveType", object); // NOI18N
    }
    
    private String getType(
            final WsdlMessageVariable object) {
        final QName qName = getTypeQName(object);
        
        if (qName != null) {
            return formatShort(qName);
        }
        
        return null;
    }
    
    private String getType(
            final WsdlMessageValue.Part object) {
        final QName qName = getTypeQName(object);
        
        if (qName != null) {
            return formatShort(qName);
        }
        
        return null;
    }
    
    private String getType(
            final XmlElementVariable object) {
        final QName qName = getTypeQName(object);
        
        if (qName != null) {
            return formatShort(qName);
        }
        
        return null;
    }
    
    private String getType(
            final SimpleVariable object) {
        final QName qName = getTypeQName(object);
        
        if (qName != null) {
            return formatShort(qName);
        }
        
        return null;
    }
    
    private String getType(
            final Node object) {
        switch (object.getNodeType()) {
            case Node.ELEMENT_NODE: 
                return NbBundle.getMessage(
                        VariablesUtil.class, "VU_TypeElement"); // NOI18N
                
            case Node.ATTRIBUTE_NODE: 
                return NbBundle.getMessage(
                        VariablesUtil.class, "VU_TypeAttribute"); // NOI18N
                
            case Node.TEXT_NODE: 
                return NbBundle.getMessage(
                        VariablesUtil.class, "VU_TypeText"); // NOI18N
                
            case Node.CDATA_SECTION_NODE: 
                return NbBundle.getMessage(
                        VariablesUtil.class, "VU_TypeCDATA"); // NOI18N
                
            default: 
                return null;
        }
    }
    
    private String getTypeTooltip(
            final WsdlMessageVariable object) {
        final QName qName = getTypeQName(object);
        
        if (qName != null) {
            return formatLong(qName);
        }
        
        return null;
    }
    
    private String getTypeTooltip(
            final WsdlMessageValue.Part object) {
        final QName qName = getTypeQName(object);
        
        if (qName != null) {
            return formatLong(qName);
        }
        
        return null;
    }
    
    private String getTypeTooltip(
            final XmlElementVariable object) {
        final QName qName = getTypeQName(object);
        
        if (qName != null) {
            return formatLong(qName);
        }
        
        return null;
    }
    
    private String getTypeTooltip(
            final SimpleVariable object) {
        final QName qName = getTypeQName(object);
        
        if (qName != null) {
            return formatLong(qName);
        }
        
        return null;
    }
    
    private String getTypeTooltip(
            final Node object) {
        return getType(object);
    }
    
    private QName getTypeQName(
            final WsdlMessageVariable object) {
        final String name = object.getName();
        
        final Variable variable = getBpelVariable(name);
        if (variable != null) {
            return variable.getMessageType().getQName();
        }
        
        return null;
    }
    
    private QName getTypeQName(
            final WsdlMessageValue.Part object) {
        final String partName = object.getName();
        final String messageName = 
                object.getMessage().getValueHost().getName();
        
        final Variable variable = getBpelVariable(messageName);
        if (variable != null) {
            final Part part = getWsdlMessagePart(variable, partName);
            
            if (part != null) {
                final QName qName;
                
                if (part.getType() != null) {
                    qName = part.getType().getQName();
                } else {
                    final NamedComponentReference<? extends GlobalType> type = 
                            part.getElement().get().getType();
                    
                    if (type != null) {
                        qName = type.getQName();
                    } else {
                        qName = part.getElement().getQName();
                    }
                }
                
                return qName;
            }
        }
        
        return null;
    }
    
    private QName getTypeQName(
            final XmlElementVariable object) {
        final String name = object.getName();
        
        final Variable variable = getBpelVariable(name);
        if (variable != null) {
            if (variable.getElement() != null) {
                return variable.getElement().getQName();
            } else {
                return variable.getType().getQName();
            }
        }
        
        return null;
    }
    
    private QName getTypeQName(
            final SimpleVariable object) {
        final String name = object.getName();
        
        final Variable variable = getBpelVariable(name);
        if (variable != null) {
            return variable.getType().getQName();
        }
        
        return null;
    }
    
    // Value ///////////////////////////////////////////////////////////////////
    public String getValue(
            final Object object) {
        if (object instanceof NamedValueHost) {
            if (object instanceof WsdlMessageVariable) {
                return getValue((WsdlMessageVariable) object);
            }

            if (object instanceof WsdlMessageValue.Part) {
                return getValue((WsdlMessageValue.Part) object);
            }

            if (object instanceof XmlElementVariable) {
                return getValue((XmlElementVariable) object);
            }

            if (object instanceof SimpleVariable) {
                return getValue((SimpleVariable) object);
            }
        }

        if (object instanceof Node) {
            return getValue((Node) object);
        }

        return NbBundle.getMessage(
                VariablesUtil.class, "VU_CannotResolveValue", object); // NOI18N
    }
    
    public String getValueTooltip(
            final Object object) {
        if (object instanceof NamedValueHost) {
            if (object instanceof WsdlMessageVariable) {
                return " " + 
                        getValueTooltip((WsdlMessageVariable) object) + " ";
            }

            if (object instanceof WsdlMessageValue.Part) {
                return " " + 
                        getValueTooltip((WsdlMessageValue.Part) object) + " ";
            }

            if (object instanceof XmlElementVariable) {
                return " " + 
                        getValueTooltip((XmlElementVariable) object) + " ";
            }

            if (object instanceof SimpleVariable) {
                return " " + 
                        getValueTooltip((SimpleVariable) object) + " ";
            }
        }

        if (object instanceof Node) {
            return " " + getValueTooltip((Node) object) + " ";
        }

        return NbBundle.getMessage(
                VariablesUtil.class, "VU_CannotResolveValue", object); // NOI18N
    }
    
    private String getValue(
            final WsdlMessageVariable object) {
        final WsdlMessageValue value = (WsdlMessageValue) object.getValue();
        
        if (value != null) {
            return NbBundle.getMessage(VariablesUtil.class, 
                    "VU_ValueWSDL", value.getParts().length); // NOI18N
        } else {
            return NbBundle.getMessage(VariablesUtil.class, 
                    "VU_ValueNotInitialized", getDisplayName(object)); // NOI18N
        }
    }
    
    private String getValue(
            final WsdlMessageValue.Part object) {
        if (object.getValue() == null) {
            return NbBundle.getMessage(VariablesUtil.class, 
                    "VU_ValueNotInitialized", getDisplayName(object)); // NOI18N
        }
        
        if (object.getValue() instanceof XmlElementValue) {
            return getValue(((XmlElementValue) object.getValue()).getElement());
        } else {
            return ((SimpleValue) object.getValue()).getValueAsString();
        }
    }
    
    private String getValue(
            final XmlElementVariable object) {
        if (object.getValue() != null) {
            return getValue(((XmlElementValue) object.getValue()).getElement());
        } else {
            return NbBundle.getMessage(VariablesUtil.class, 
                    "VU_ValueNotInitialized", getDisplayName(object)); // NOI18N
        }
    }
    
    private String getValue(
            final SimpleVariable object) {
        if (object.getValue() != null) {
            return ((SimpleValue) object.getValue()).getValueAsString();
        } else {
            return NbBundle.getMessage(VariablesUtil.class, 
                    "VU_ValueNotInitialized", getDisplayName(object)); // NOI18N
        }
    }
    
    private String getValue(
            final Node object) {
        if (object.getNodeType() == Node.ELEMENT_NODE) {
            if (XmlUtil.isTextOnlyNode(object)) {
                return object.getChildNodes().item(0).getNodeValue();
            }
            
            return NbBundle.getMessage(VariablesUtil.class, 
                    "VU_ValueXMLData"); // NOI18N
        }
        
        return object.getTextContent();
    }
    
    private String getValueTooltip(
            final WsdlMessageVariable object) {
        return getValue(object);
    }
    
    private String getValueTooltip(
            final WsdlMessageValue.Part object) {
        return getValue(object);
    }
    
    private String getValueTooltip(
            final XmlElementVariable object) {
        return getValue(object);
    }
    
    private String getValueTooltip(
            final SimpleVariable object) {
        return getValue(object);
    }
    
    private String getValueTooltip(
            final Node object) {
        return getValue(object);
    }
    
    // Read-only / read-write //////////////////////////////////////////////////
    public boolean isValueReadOnly(
            final Object object) {
        if (object instanceof NamedValueHost) {
            if (object instanceof WsdlMessageVariable) {
                return isValueReadOnly((WsdlMessageVariable) object);
            }

            if (object instanceof WsdlMessageValue.Part) {
                return isValueReadOnly((WsdlMessageValue.Part) object);
            }

            if (object instanceof XmlElementVariable) {
                return isValueReadOnly((XmlElementVariable) object);
            }

            if (object instanceof SimpleVariable) {
                return isValueReadOnly((SimpleVariable) object);
            }
        }

        if (object instanceof Node) {
            return isValueReadOnly((Node) object);
        }

        return true;
    }
    
    private boolean isValueReadOnly(
            final WsdlMessageVariable object) {
        return true;
    }
    
    private boolean isValueReadOnly(
            final WsdlMessageValue.Part object) {
        if (object.getValue() instanceof XmlElementValue) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean isValueReadOnly(
            final XmlElementVariable object) {
        return true;
    }
    
    private boolean isValueReadOnly(
            final SimpleVariable object) {
        return object.getValue() == null;
    }
    
    private boolean isValueReadOnly(
            final Node object) {
        if (object.getNodeType() == Node.ATTRIBUTE_NODE) {
            if (!(object.getNodeName().equals("xmlns") || // NOI18N
                  object.getNodeName().equals("targetNamespace") || // NOI18N
                  object.getNodeName().startsWith("xmlns:"))) { // NOI18N
                return false;
            }
        }

        return !XmlUtil.isTextOnlyNode(object);
    }
    
    // Value setters ///////////////////////////////////////////////////////////
    public void setValue(
            final Object object,
            final String value) {
        if (object instanceof NamedValueHost) {
            if (object instanceof WsdlMessageVariable) {
                // does nothing
            }

            if (object instanceof WsdlMessageValue.Part) {
                setValue((WsdlMessageValue.Part) object, value);
            }

            if (object instanceof XmlElementVariable) {
                // does nothing
            }

            if (object instanceof SimpleVariable) {
                setValue((SimpleVariable) object, value);
            }
        }

        if (object instanceof Node) {
            setValue((Node) object, value);
        }
    }
    
    private void setValue(
            final WsdlMessageValue.Part object,
            final String value) {
        if (!(object.getValue() instanceof SimpleValue)) {
            return;
        }
        
        final NamedValueHost message = object.getMessage().getValueHost();
                
        if (message instanceof WsdlMessageVariable) {
            ((WsdlMessageVariable) message).setPartSimpleValue(object, value);
        }
    }
    
    private void setValue(
            final SimpleVariable object,
            final String value) {
        object.setValue(value);
    }
    
    private void setValue(
            final Node object,
            final String value) {
        if (XmlUtil.isTextOnlyNode(object)) {
            setValue(object.getChildNodes().item(0), value);
            return;
        }
        
        final XmlElementValue xmlValue = XmlElementValue.Helper.find(object);
        final NamedValueHost valueHost = xmlValue.getValueHost();
        
        if (valueHost instanceof XmlElementVariable) {
            ((XmlElementVariable) valueHost).setNodeValue(object, value);
            return;
        }
        
        if (valueHost instanceof WsdlMessageValue.Part) {
            final WsdlMessageValue.Part part = 
                    (WsdlMessageValue.Part) valueHost;
            final NamedValueHost messageValueHost = 
                    part.getMessage().getValueHost();
            
            if (messageValueHost instanceof WsdlMessageVariable) {
                ((WsdlMessageVariable) messageValueHost).setPartNodeValue(
                        part, object, value);
            }
        }
        
        object.setNodeValue(value);
    }
    
    // Custom editor ///////////////////////////////////////////////////////////
    public boolean supportsCustomEditor(
            final Object object) {
        if (object instanceof NamedValueHost) {
            if (object instanceof WsdlMessageVariable) {
                return false;
            }

            if (object instanceof WsdlMessageValue.Part) {
                return ((NamedValueHost) object).getValue() != null;
            }

            if (object instanceof XmlElementVariable) {
                return ((NamedValueHost) object).getValue() != null;
            }

            if (object instanceof SimpleVariable) {
                return ((NamedValueHost) object).getValue() != null;
            }
        }

        if (object instanceof Node) {
            return true;
        }

        return false;
    }
    
    public String getCustomEditorValue(
            final Object object) {
        if (object instanceof NamedValueHost) {
            if (object instanceof WsdlMessageVariable) {
                return null; // shouldn't happen
            }
            
            if (object instanceof WsdlMessageValue.Part) {
                final Value value = ((NamedValueHost) object).getValue();
                
                if (value instanceof XmlElementValue) {
                    return XmlUtil.toString(
                            ((XmlElementValue) value).getElement());
                } else {
                    return getValue((WsdlMessageValue.Part) object);
                }
            }
            
            if (object instanceof XmlElementVariable) {
                final XmlElementValue value = (XmlElementValue) 
                        ((XmlElementVariable) object).getValue();
                
                return XmlUtil.toString(value.getElement());
            }
            
            if (object instanceof SimpleVariable) {
                return getValue((SimpleVariable) object);
            }
        }
        
        if (object instanceof Node) {
            final Node node = (Node) object;
            
            if ((node instanceof Element) && !XmlUtil.isTextOnlyNode(node)) {
                return XmlUtil.toString(node);
            }
            
            return ((Node) object).getTextContent();
        }
        
        return NbBundle.getMessage(
                VariablesUtil.class, "VU_CannotResolveValue", object); // NOI18N
    }
    
    public String getCustomEditorMimeType(
            final Object object) {
        if (object instanceof NamedValueHost) {
            if (object instanceof WsdlMessageVariable) {
                return null; // shouldn't happen
            }
            
            if (object instanceof WsdlMessageValue.Part) {
                final Value value = ((NamedValueHost) object).getValue();
                
                if (value instanceof XmlElementValue) {
                    return "text/xml"; // NOI18N
                } else {
                    return "text/plain"; // NOI18N
                }
            }
            
            if (object instanceof XmlElementVariable) {
                return "text/xml"; // NOI18N
            }
            
            if (object instanceof SimpleVariable) {
                return "text/plain"; // NOI18N
            }
        }
        
        if (object instanceof Node) {
            final Node node = (Node) object;
            
            if ((node instanceof Element) && !XmlUtil.isTextOnlyNode(node)) {
                return "text/xml";
            }
            
            return "text/plain"; // NOI18N
        }
        
        return "text/plain"; // NOI18N
    }
    
    // Miscellaneous ///////////////////////////////////////////////////////////
    public BpelModel getBpelModel() {
        final DebuggerEngine engine = DebuggerManager.getDebuggerManager().
                getCurrentEngine();
        
        if (engine == null) {
            return null;
        }
        
        final SourcePath sourcePath = 
                engine.lookupFirst(null, SourcePath.class);
        final ProcessInstance instance = myDebugger.getCurrentProcessInstance();
        
        if ((sourcePath == null) || (instance == null)) {
            return null;
        }
        
        return EditorUtil.getBpelModel(sourcePath.getSourcePath(
                instance.getProcess().getQName()));
    }
    
    /**
     * Returns an instance of {@link Variable} which corresponds to the given
     * name. This method searches through the BPEL OM, according to the current
     * activity XPath and checks variables from the process itself and all 
     * active scopes.
     * 
     * @param name Name of the variable.
     * @return Instance of {@link Variable} which corresponds to the given 
     *      name.
     */
    public Variable getBpelVariable(
            final String name) {
        final List<Variable> variables = new LinkedList<Variable>();
        
        final BpelModel model = getBpelModel();
        
        if (model == null) {
            return null;
        }
        
        VariableContainer varsContainer = model.getProcess().
                getVariableContainer();
        
        // Add the variables from the process
        if ((varsContainer != null) && (varsContainer.sizeOfVariable() > 0)) {
            variables.addAll(Arrays.asList(varsContainer.getVariables()));
        }
        
        final ProcessInstance currentInstance = 
                myDebugger.getCurrentProcessInstance();
        if (currentInstance == null) {
            return null;
        }
        
        final Position currentPosition = currentInstance.getCurrentPosition();
        if (currentPosition == null) {
            return null;
        }
        
        final String xpath = currentPosition.getXpath();
        
        int scopeIndex = xpath.indexOf("scope"); // NOI18N
        while (scopeIndex != -1) {
            final int index = xpath.indexOf("/", scopeIndex); // NOI18N
            
            final String scopeXpath = 
                    index == -1 ? xpath : xpath.substring(0, index);
            
            final Scope scope = getScopeEntity(scopeXpath);
            if (scope != null) {
                varsContainer = scope.getVariableContainer();
                
                if ((varsContainer != null) && 
                        (varsContainer.sizeOfVariable() > 0)) {
                    
                    variables.addAll(
                            Arrays.asList(varsContainer.getVariables()));
                }
            }
            
            scopeIndex = index == -1 ? 
                    index : 
                    xpath.indexOf("scope", index); // NOI18N
        }
        
        for (Variable variable: variables) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }
        
        return null;
    }
    
    public Scope getScopeEntity(
            final String xpath) {
        
        final BpelModel model = getBpelModel();
        
        if (model == null) {
            return null;
        }
        
        BpelEntity currentEntity = model.getProcess();
        
        final StringTokenizer tokenizer = 
                new StringTokenizer(xpath, "/"); // NOI18N
        while (tokenizer.hasMoreTokens()) {
            String name = tokenizer.nextToken();
            int offset = 1;
            
            if (name.equals("")) { // NOI18N
                continue;
            }
            
            final int colonIndex = name.indexOf(":"); // NOI18N
            if (colonIndex > -1) {
                name = name.substring(colonIndex + 1);
            }
            
            final int openingBracketIndex = name.indexOf("["); // NOI18N
            if (openingBracketIndex > -1) {
                final int closingBracketIndex = name.lastIndexOf("]"); // NOI18N
                
                offset = Integer.parseInt(name.substring(
                        openingBracketIndex + 1, 
                        closingBracketIndex));
                name = name.substring(0, openingBracketIndex);
            }
            
            if (name.equals("process")) { // NOI18N
                continue;
            }
            
            for (BpelEntity entity: currentEntity.getChildren()) {
                if (entity.getPeer().getNodeName().equals(name)) {
                    offset--;
                }
                
                if (offset == 0) {
                    currentEntity = entity;
                    break;
                }
            }
        }
        
        return (Scope) (currentEntity instanceof Scope ? currentEntity : null);
        
    }
    
    public Part getWsdlMessagePart(
            final Variable variable, 
            final String name) {
        final WSDLReference<Message> reference = variable.getMessageType();
        
        if (reference == null) {
            return null;
        }
        
        final Message message = reference.get();
        for (Part part: message.getParts()) {
            if (part.getName().equals(name)) {
                return part;
            }
        }
        
        return null;
    }
    
    public String formatShort(
            final QName qName) {
        return qName.getPrefix() + ":" + qName.getLocalPart(); // NOI18N
    }
    
    public String formatLong(
            final QName qName) {
        return "{" + qName.getNamespaceURI() + "} " + // NOI18N
                qName.getLocalPart();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String ICONS_ROOT =
            "org/netbeans/modules/bpel/debugger/ui/resources/" + // NOI18N
            "image/variables/"; // NOI18N
    
    private static final String DEFAULT_VARIABLE_ICON =
            "org/netbeans/modules/debugger/resources/" + // NOI18N
            "localsView/LocalVariable"; // NOI18N
    
    private static final String WSDL_MESSAGE_VARIABLE_ICON =
            ICONS_ROOT + "VARIABLE_MESSAGE"; // NOI18N
    
    private static final String WSDL_MESSAGE_PART_ICON =
            ICONS_ROOT + "MESSAGE_PART"; // NOI18N

    private static final String XML_ELEMENT_VARIABLE_ICON =
            ICONS_ROOT + "VARIABLE_XML_ELEMENT"; // NOI18N

    private static final String SIMPLE_VARIABLE_ICON =
            ICONS_ROOT + "VARIABLE_SIMPLE"; // NOI18N
    
    private static final String DEFAULT_NODE_ICON =
            ICONS_ROOT + "DEFAULT_NODE"; // NOI18N
    
    private static final String ELEMENT_NODE_ICON =
            ICONS_ROOT + "ELEMENT_NODE"; // NOI18N
            
    private static final String ATTRIBUTE_NODE_ICON =
            ICONS_ROOT + "ATTRIBUTE_NODE"; // NOI18N
    
    private static final String TEXT_NODE_ICON =
            ICONS_ROOT + "TEXT_NODE"; // NOI18N
    
    private static final String CDATA_NODE_ICON =
            ICONS_ROOT + "CDATA_NODE"; // NOI18N
}
