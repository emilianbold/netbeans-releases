/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.nodes;

import java.awt.Component;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Action;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.Constants.VariableStereotype;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ElementReference;
import org.netbeans.modules.bpel.model.api.MessageTypeReference;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.properties.editors.VariableMainPanel;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.bpel.properties.props.CustomEditorProperty;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.editors.controls.CustomNodeEditor.EditingMode;
import org.netbeans.modules.bpel.properties.editors.controls.filter.NodeChildFilter;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VariableTypeFilter;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VariableTypeInfoProvider;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.actions.DeleteVariableAction;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaModel;
//import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.netbeans.modules.xml.xam.Reference;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author nk160297
 */
public class VariableNode extends BpelNode<VariableDeclaration>
        implements VariableTypeInfoProvider {
    
    public VariableNode(VariableDeclaration var, Children children, Lookup lookup) {
        super(var, children, lookup);
    }
    
    public VariableNode(VariableDeclaration var, Lookup lookup) {
        super(var, lookup);
        //
//        // Check if the node should be considered as leaf.
//        Constants.VariableStereotype stereoType = getVariableStereotype(var);
//        if (stereoType != Constants.VariableStereotype.GLOBAL_TYPE) {
//            // Variables of the Not SIMPLE type can contain nested elements.
//            if (isChildrenAllowed()) {
//                Children.MUTEX.postWriteRequest(new Runnable() {
//                    public void run() {
//                        setChildren(new MyChildren());
//                    }
//                });
//            }
//        }
    }
    
    public static Constants.VariableStereotype
            getVariableStereotype(VariableDeclaration var) {
        if (var == null) {
            return null;
        }
        // if (currentStereotype != null) return currentStereotype;
        Constants.VariableStereotype currentStereotype = null;
        //
        SchemaReference<GlobalType> typeRef = var.getType();
        if (typeRef != null) {
            GlobalType type = typeRef.get();
            if (type != null) {
                currentStereotype = VariableStereotype.recognizeStereotype(type);
            } else {
                currentStereotype = Constants.VariableStereotype.GLOBAL_TYPE;
            }
        } else if (var.getMessageType() != null) {
            currentStereotype = Constants.VariableStereotype.MESSAGE;
        } else if (var.getElement() != null) {
            currentStereotype = Constants.VariableStereotype.GLOBAL_ELEMENT;
        } else {
            currentStereotype = Constants.VariableStereotype.MESSAGE;
        }
        return currentStereotype;
    }
    
    public static Reference getVariableType(VariableDeclaration variable) {
        VariableStereotype variableStereotype = getVariableStereotype(variable);
        //
        switch (variableStereotype) {
            case PRIMITIVE_TYPE:
            case GLOBAL_SIMPLE_TYPE:
            case GLOBAL_COMPLEX_TYPE:
            case GLOBAL_TYPE:
                return variable.getType();
            case MESSAGE:
                return variable.getMessageType();
            case GLOBAL_ELEMENT:
                return variable.getElement();
            default:
                return null;
        }
    }
    
    public static QName getVariableQNameType(VariableDeclaration variable) {
        VariableStereotype variableStereotype = getVariableStereotype(variable);
        //
        switch (variableStereotype) {
            case PRIMITIVE_TYPE:
            case GLOBAL_SIMPLE_TYPE:
            case GLOBAL_COMPLEX_TYPE:
            case GLOBAL_TYPE:
                SchemaReference<GlobalType> typeRef = variable.getType();
                if (typeRef != null) {
                    return typeRef.getQName();
                } else {
                    return null;
                }
            case MESSAGE:
                WSDLReference<Message> msgRef = variable.getMessageType();
                if (msgRef != null) {
                    return msgRef.getQName();
                } else {
                    return null;
                }
            case GLOBAL_ELEMENT:
                SchemaReference<GlobalElement> elementRef = variable.getElement();
                if (elementRef != null) {
                    return elementRef.getQName();
                } else {
                    return null;
                }
            default:
                return null;
        }
    }
    
    public NodeType getNodeType() {
        return NodeType.VARIABLE;
    }
    
    public Image getIcon(int type) {
        return getNodeType().getImage(getVariableStereotype());
    }
    
    protected String getNameImpl(){
        VariableDeclaration varDecl = getReference();
        String name = null;
        if (varDecl != null) {
            if (varDecl instanceof Variable) {
                name = ((Variable)varDecl).getName();
            } else {
                name = varDecl.getVariableName();
            }
        }
        return (name != null) ? name : "";
    }
    
//    protected String getImplShortDescription() {
//        VariableStereotype stereoType = getVariableStereotype();
//        
//        if (stereoType != null
//                && stereoType.equals(Constants.VariableStereotype.MESSAGE)) {
//            return NbBundle.getMessage(VariableNode.class,
//                    "LBL_VARIABLE_NODE_TOOLTIP", // NOI18N
//                    getName(),
//                    getVariableType() == null ? "" : getVariableType().getRefString()
//                    );
//            
//        }
//        
//        return NbBundle.getMessage(VariableNode.class,
//                "LBL_VARIABLE_NODE_TOOLTIP", // NOI18N
//                getName(),
//                getVariableQNameType() == null ? "" : getVariableQNameType().toString()
//                );
//    }
    
    protected String getImplHtmlDisplayName() {
        String result;
        VariableTypeFilter typeFilter =
                (VariableTypeFilter)getLookup().lookup(VariableTypeFilter.class);
        if (typeFilter != null) {
            //
            // This case is used by the Variable Chooser
            if (typeFilter.isTypeAllowed(this)) {
                result = getName();
            } else {
                result = Util.getGrayString(getName());
            }
        } else {
            Reference varType = getVariableType();
            if (varType == null) {
                result = getName();
            } else {
                result = Util.getGrayString(
                        getName(),
                        " " + getVariableType().getRefString()); // NOI18N
            }
        }
        //
        return result;
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        if (getReference() == null) {
            // The related object has been removed!
            return sheet;
        }
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        CustomEditorProperty customizer = new CustomEditorProperty(this);
        mainPropertySet.put(customizer);
        //
        VariableDeclaration varDecl = getReference();
        if (varDecl instanceof Variable) {
            PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                    NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        } else {
            PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                    NamedElement.NAME, NAME, "getVariableName", null, null); // NOI18N
        }
        //
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                VARIABLE_STEREOTYPE, "getVariableStereotype", null); // NOI18N
        //
//        Property prop = PropertyUtils.registerCalculatedProperty(this,
//                mainPropertySet, VARIABLE_TYPE,
//                "getVariableType", "setVariableType"); // NOI18N
//        prop.setHidden(true);
        //
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                VARIABLE_TYPE_QNAME, "getVariableQNameType", null); // NOI18N
        //
        return sheet;
    }
    
    public Constants.VariableStereotype getVariableStereotype() {
        return getVariableStereotype(getReference());
    }
    
    public Reference getVariableType() {
        VariableDeclaration variable = getReference();
        return variable == null ? null : getVariableType(variable);
    }
    
    public QName getVariableQNameType() {
        VariableDeclaration variable = getReference();
        return variable == null ? null : getVariableQNameType(variable);
    }
    
    public void setVariableType(Reference newValue) {
        VariableDeclaration varDecl = getReference();
        if (varDecl == null) {
            return;
        }
        if (!(varDecl instanceof Variable)) {
            return;
        }
        Variable variable = (Variable)varDecl;
        //
        VariableStereotype variableStereotype = getVariableStereotype();
        //
        switch (variableStereotype) {
            case PRIMITIVE_TYPE:
            case GLOBAL_SIMPLE_TYPE:
            case GLOBAL_COMPLEX_TYPE:
            case GLOBAL_TYPE:
                assert newValue instanceof SchemaReference;
                variable.setType((SchemaReference<GlobalType>)newValue);
                break;
            case MESSAGE:
                assert newValue instanceof WSDLReference;
                variable.setMessageType((WSDLReference<Message>)newValue);
                break;
            case GLOBAL_ELEMENT:
                assert newValue instanceof SchemaReference;
                variable.setElement((SchemaReference<GlobalElement>)newValue);
                break;
        }
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.FIND_USAGES,
            ActionType.SEPARATOR,
            ActionType.SHOW_POPERTY_EDITOR,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
    
    public Action createAction(ActionType actionType) {
        switch (actionType) {
            case REMOVE:
                return SystemAction.get(DeleteVariableAction.class);
            default:
                return super.createAction(actionType);
        }
    }
    
    private class MyChildren extends Children.Keys {
        
        public MyChildren() {
            super();
            // It's necesary to specify any key. Otherwise it doesn't work.
            setKeys(new Object[] {new Object()});
        }
        
        protected Node[] createNodes(Object key) {
            //
            // Variable has a type related nodes as a child.
            Node parentNode = getNode();
            if (parentNode != null) {
                assert parentNode instanceof VariableNode;
                VariableDeclaration var = ((VariableNode)parentNode).getReference();
                if (var == null) {
                    return new Node[0];
                }
                Lookup lookup = parentNode.getLookup();
                //
                NodeChildFilter filter = (NodeChildFilter)getLookup().
                        lookup(NodeChildFilter.class);
                //
                switch (((VariableNode)parentNode).getVariableStereotype()) {
                    case GLOBAL_TYPE:
                        return null;
                    case GLOBAL_ELEMENT: {
                        SchemaReference<GlobalElement> typeRef = var.getElement();
                        if (typeRef != null) {
                            GlobalElement element = typeRef.get();
                            SchemaModel schemaModel = (SchemaModel)element.getModel();
                            /*
                            if (element != null) {
                                CategorizedSchemaNodeFactory nodeFactory =
                                        new CategorizedSchemaNodeFactory(
                                        schemaModel, lookup);
                                //
                                Node elementTypeNode = nodeFactory.createNode(
                                        element);
                                //
                                if (filter == null ||
                                        filter.isPairAllowed(
                                        getNode(), elementTypeNode)) {
                                    return new Node[] {elementTypeNode};
                                }
                            } */
                        }
                        break;
                    }
                    case MESSAGE: {
                        WSDLReference<Message> typeRef = var.getMessageType();
                        if (typeRef != null) {
                            Message message = typeRef.get();
                            if (message != null) {
                                Collection<Part> parts = message.getParts();
                                ArrayList<Node> nodesList = new ArrayList<Node>();
                                for (Part part : parts) {
                                    Node node = PropertyNodeFactory
                                            .getInstance().createNode(NodeType
                                            .MESSAGE_PART, part, null, lookup);
                                    // Node node = new MessagePartNode(part, lookup);
                                    if (filter == null ||
                                            filter.isPairAllowed(getNode(), node)) {
                                        nodesList.add(node);
                                    }
                                }
                                Node[] nodesArr = nodesList.toArray(
                                        new Node[nodesList.size()]);
                                return nodesArr;
                            }
                        }
                    }
                }
            }
            return null;
        }
    }
    
    public static class DefaultTypeInfoProvider implements VariableTypeInfoProvider {
        private VariableDeclaration myVar;
        
        public DefaultTypeInfoProvider(VariableDeclaration var) {
            myVar = var;
        }
        
        public Constants.VariableStereotype getVariableStereotype() {
            if (myVar != null) {
                return VariableNode.getVariableStereotype(myVar);
            } else {
                return null;
            }
        }
        
        public Object getVariableType() {
            if (myVar != null) {
                return VariableNode.getVariableType(myVar);
            } else {
                return null;
            }
        }
        
        public QName getVariableQNameType() {
            if (myVar != null) {
                return VariableNode.getVariableQNameType(myVar);
            } else {
                return null;
            }
        }
    }
    
    public Component getCustomizer() {
        SimpleCustomEditor customEditor = new SimpleCustomEditor<VariableDeclaration>(
                this, VariableMainPanel.class, EditingMode.EDIT_INSTANCE);
        return customEditor;
    }
    
    protected void updateComplexProperties(ChangeEvent event) {
        if (event instanceof PropertyUpdateEvent ||
                event instanceof PropertyRemoveEvent) {
            BpelEntity parentEvent = event.getParent();
            if (parentEvent != null && parentEvent.equals(this.getReference())) {
                String propName = event.getName();
                if (Variable.TYPE.equals(propName) ||
                        MessageTypeReference.MESSAGE_TYPE.equals(propName) ||
                        ElementReference.ELEMENT.equals(propName)) {
                    updateProperty(PropertyType.VARIABLE_STEREOTYPE);
                    updateProperty(PropertyType.VARIABLE_TYPE_QNAME);
                }
            }
        }
    }
    
}
