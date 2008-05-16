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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.nodes.BpelNode;
import java.awt.Component;
import java.awt.Image;
import javax.swing.Action;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ElementReference;
import org.netbeans.modules.bpel.model.api.MessageTypeReference;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.properties.editors.VariableMainPanel;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.bpel.properties.props.CustomEditorProperty;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VariableTypeFilter;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VariableTypeInfoProvider;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.nodes.actions.DeleteVariableAction;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
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
    }
    
    public static QName getVariableQNameType(VariableDeclaration variable) {
        VariableStereotype variableStereotype = EditorUtil.getVariableStereotype(variable);
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
                result = SoaUtil.getGrayString(getName());
            }
        } else {
            Reference varType = getVariableType();
            if (varType == null) {
                result = getName();
            } else {
                result = SoaUtil.getGrayString(
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
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                VARIABLE_TYPE_QNAME, "getVariableQNameType", null); // NOI18N
        //
        PropertyUtils.registerProperty(this, mainPropertySet,
                DOCUMENTATION, "getDocumentation", "setDocumentation", "removeDocumentation"); // NOI18N
        //
        return sheet;
    }
    
    public VariableStereotype getVariableStereotype() {
        return EditorUtil.getVariableStereotype(getReference());
    }
    
    public Reference getVariableType() {
        VariableDeclaration variable = getReference();
        return variable == null ? null : EditorUtil.getVariableType(variable);
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
            ActionType.GO_TO_TYPE_SOURCE,
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
    
    public static class DefaultTypeInfoProvider implements VariableTypeInfoProvider {
        private VariableDeclaration myVar;
        
        public DefaultTypeInfoProvider(VariableDeclaration var) {
            myVar = var;
        }
        
        public VariableStereotype getVariableStereotype() {
            if (myVar != null) {
                return EditorUtil.getVariableStereotype(myVar);
            } else {
                return null;
            }
        }
        
        public Object getVariableType() {
            if (myVar != null) {
                return EditorUtil.getVariableType(myVar);
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
    
    public Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        SimpleCustomEditor customEditor = new SimpleCustomEditor<VariableDeclaration>(
                this, VariableMainPanel.class, editingMode);
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
