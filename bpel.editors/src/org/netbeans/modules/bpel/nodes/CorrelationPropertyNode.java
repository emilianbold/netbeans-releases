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

import java.awt.Component;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.properties.props.CustomEditorProperty;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.netbeans.modules.xml.xam.Reference;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.editors.CorrelationPropertyMainPanel;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class CorrelationPropertyNode extends BpelWSDLNode<CorrelationProperty> {
    
    public CorrelationPropertyNode(CorrelationProperty property, Children children, Lookup lookup) {
        super(property, children, lookup);
    }
    
    public CorrelationPropertyNode(CorrelationProperty property, Lookup lookup) {
        super(property, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.CORRELATION_PROPERTY;
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        CustomEditorProperty customizer = new CustomEditorProperty(this);
        mainPropertySet.put(customizer);
        //
        Property prop = null;
        //
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                NAME, "getPropertyName", "setPropertyName"); // NOI18N
        //
//        prop = PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
//                CORRELATON_PROPERTY_TYPE, "getType", "setType"); // NOI18N
//        prop.setHidden(true);
        //
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                CORRELATON_PROPERTY_TYPE_NAME, "getType", null); // NOI18N
//                CORRELATON_PROPERTY_TYPE_NAME, "getTypeQName", null); // NOI18N
        return sheet;
    }
    
    public String getPropertyName() {
        CorrelationProperty ref = getReference();
        return ref == null ? null : ref.getName();
    }
    
    public void setPropertyName(String newValue) {
        CorrelationProperty cp = getReference();
        if (cp == null) {
            return;
        }
        
        WSDLModel model = cp.getModel();
        if (model.isIntransaction()) {
            cp.setName(newValue);
        } else {
            model.startTransaction();
            try {
                cp.setName(newValue);
            } finally {
                    model.endTransaction();
            }
        }
    }
    
    public String getType() {
        CorrelationProperty ref = getReference();
        if ( ref == null) {
            return null;
        }
        
        String typeOrElement = "";
        Reference referenceTypeorElement = ref.getType();
        referenceTypeorElement = referenceTypeorElement == null 
                ? ref.getElement() 
                : referenceTypeorElement ;
        
        return referenceTypeorElement == null ? null : referenceTypeorElement.getRefString();
    }
    
    public void setType(GlobalSimpleType newType) {
        CorrelationProperty cp = getReference();
        if (cp == null) {
            return;
        }
        
        NamedComponentReference<GlobalType> typeReference = cp.
                createSchemaReference(newType, GlobalType.class);
        WSDLModel model = cp.getModel();
        if (model.isIntransaction()) {
            cp.setType(typeReference);
        } else {
            model.startTransaction();
            try {
                cp.setType(typeReference);
            } finally {
                    model.endTransaction();
            }
        }
    }
    
    public QName getTypeQName() {
        CorrelationProperty ref = getReference();
        if (ref == null) {
            return null;
        }
        
        NamedComponentReference<GlobalType> typeReference = ref.getType();
        if (typeReference != null && !typeReference.isBroken()) {
            return typeReference.getQName();
        } else {
            return null;
        }
    }
    

    private boolean isBpelExtNode() {
        Node parent = getParentNode();
        while (parent != null) {
            if (parent instanceof ImportWsdlNode) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }
    
    protected ActionType[] getActionsArray() {
        ActionType deleteActionType = isBpelExtNode() 
                ? ActionType.DELETE_BPEL_EXT_FROM_WSDL 
                : ActionType.DELETE_PROPERTY_ACTION;
        return new ActionType[] {
            ActionType.SHOW_POPERTY_EDITOR,
            ActionType.SEPARATOR,
            deleteActionType,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES,
        };
    }

    public Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        return new SimpleCustomEditor<CorrelationProperty>(
                this, CorrelationPropertyMainPanel.class, editingMode);
    }
}
