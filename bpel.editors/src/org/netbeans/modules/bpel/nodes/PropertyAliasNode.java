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
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.properties.props.CustomEditorProperty;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.editors.PropertyAliasMainPanel2;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Node.Property;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class PropertyAliasNode extends BpelWSDLNode<PropertyAlias> {
    
    public PropertyAliasNode(PropertyAlias alias, Lookup lookup) {
        super(alias, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.CORRELATION_PROPERTY_ALIAS;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
//        PropertyAlias alias = getReference();
        //
        Sheet.Set mainPropertySet = 
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        CustomEditorProperty customizer = new CustomEditorProperty(this);
        mainPropertySet.put(customizer);
        //
        Property prop = null;
        PropertyUtils propUtil = PropertyUtils.getInstance();
        //
        prop = propUtil.registerCalculatedProperty(this, mainPropertySet,
                CORR_PROPERTY, "getCorrProperty", "setCorrProperty"); // NOI18N
        prop.setHidden(true);
        propUtil.registerCalculatedProperty(this, mainPropertySet,
                CORR_PROPERTY_NAME, "getCorrPropertyQName", null); // NOI18N
        prop = propUtil.registerCalculatedProperty(this, mainPropertySet,
                MESSAGE_TYPE, "getMessageType", "setMessageType"); // NOI18N
        prop.setHidden(true);
        propUtil.registerCalculatedProperty(this, mainPropertySet,
                MESSAGE_TYPE_NAME, "getMessageTypeQName", null); // NOI18N
        propUtil.registerCalculatedProperty(this, mainPropertySet,
                PART, "getPart", null); // NOI18N
//                PART, "getPart", "setPart"); // NOI18N
        propUtil.registerCalculatedProperty(this, mainPropertySet,
                QUERY, "getQuery", "setQuery"); // NOI18N
        return sheet;
    }
    
    public CorrelationProperty getCorrProperty() {
        PropertyAlias ref = getReference();
        NamedComponentReference<CorrelationProperty> cpReference = ref != null ?
                ref.getPropertyName() : null;
        return (cpReference == null) ? null : cpReference.get();
    }
    
    public void setCorrProperty(CorrelationProperty newCP) { 
        PropertyAlias propertyAlias = getReference();
        if (propertyAlias == null) {
            return;
        }
        NamedComponentReference<CorrelationProperty> cpReference = propertyAlias.
                createReferenceTo(newCP, CorrelationProperty.class);
        //
        WSDLModel model = propertyAlias.getModel();
        if (model.isIntransaction()) {
            propertyAlias.setPropertyName(cpReference);
        } else {
            model.startTransaction();
            try {
                propertyAlias.setPropertyName(cpReference);
            } finally {
                    model.endTransaction();
            }
        }
    }
    
    public QName getCorrPropertyQName() {
        PropertyAlias ref = getReference();
        if (ref == null) {
            return null;
        }
        NamedComponentReference<CorrelationProperty> cpReference = 
                ref.getPropertyName();
        return (cpReference == null) ? null : cpReference.getQName();
    }
    
    public Message getMessageType() {
        PropertyAlias ref = getReference();
        if (ref == null) {
            return null;
        }
        NamedComponentReference<Message> msgReference = ref.getMessageType();
        return (msgReference == null) ? null : msgReference.get();
    }
    
    public void setMessageType(Message newMessageType) { 
        PropertyAlias propertyAlias = getReference();
        if (propertyAlias == null) {
            return;
        }
        NamedComponentReference<Message> messageReference = propertyAlias.
                createReferenceTo(newMessageType, Message.class);
        //
        WSDLModel model = propertyAlias.getModel();
        if (model.isIntransaction()) {
            propertyAlias.setMessageType(messageReference);
        } else {
            model.startTransaction();
            try {
                propertyAlias.setMessageType(messageReference);
            } finally {
                    model.endTransaction();
            }
        }
    }
    
    public QName getMessageTypeQName() {
        PropertyAlias ref = getReference();
        if (ref == null) {
            return null;
        }
        NamedComponentReference<Message> msgReference = ref.getMessageType();
        return (msgReference == null) ? null : msgReference.getQName();
    }
 
    public String getPart() {
        PropertyAlias propertyAlias = getReference();
        return propertyAlias == null ? null : propertyAlias.getPart();
    }
    
    public void setPart(String newValue) {
        PropertyAlias propertyAlias = getReference();
        if (propertyAlias == null) {
            return;
        }
        //
        WSDLModel model = propertyAlias.getModel();
        if (model.isIntransaction()) {
            propertyAlias.setPart(newValue);
        } else {
            model.startTransaction();
            try {
                propertyAlias.setPart(newValue);
            } finally {
                    model.endTransaction();
            }
        }
    }
    
    public QName getElementQName() {
        PropertyAlias propertyAlias = getReference();
        NamedComponentReference<GlobalElement> element = propertyAlias == null ? null : propertyAlias.getElement();
        return element != null ? element.getQName() : null;
    }

    public QName getTypeQName() {
        PropertyAlias propertyAlias = getReference();
        NamedComponentReference<GlobalType> type = propertyAlias == null ? null : propertyAlias.getType();
        return type != null ? type.getQName() : null;
    }

    public String getQuery() {
        PropertyAlias propertyAlias = getReference();
        Query query = propertyAlias == null ? null : propertyAlias.getQuery();
        return query != null ? query.getContent() : null;
    }
    
    public void setQuery(String newValue) {
        PropertyAlias propertyAlias = getReference();
        if (propertyAlias == null) {
            return;
        }
        //
        WSDLModel model = propertyAlias.getModel();
        if (model.isIntransaction()) {
            Util.setQueryImpl(propertyAlias, newValue);
        } else {
            model.startTransaction();
            try {
                Util.setQueryImpl(propertyAlias, newValue);
            } finally {
                    model.endTransaction();
            }
        }
    }
    
    private boolean isRequirePropDescription() {
        Node node = getParentNode();
        return node instanceof BpelNode 
                && NodeType.IMPORT_WSDL.equals(((BpelNode)node).getNodeType());
    }
    
    private String getPropDescriptionPart() {
        PropertyAlias propertyAlias = getReference();
        if (propertyAlias == null) {
            return null;
        }
        
        NamedComponentReference<CorrelationProperty> corrProp 
                = propertyAlias.getPropertyName();
        if (corrProp == null || corrProp.get() == null) {
            return null;
        }
        
        String propDescription = null;
        propDescription = FOR_PROPERTY_EQ+corrProp.get().getName();
        return propDescription;
    }
    
    protected String getImplHtmlDisplayName() {
        
        
        String aliasName = null;
        QName msgType = getMessageTypeQName();
        aliasName = msgType == null ? EMPTY_STRING 
                :  MESSAGE_TYPE_EQ+msgType.getLocalPart(); 
        aliasName += getPart() == null ? EMPTY_STRING : PART_EQ+getPart(); 
        aliasName += getTypeQName() == null ? EMPTY_STRING 
                : TYPE_EQ+getTypeQName().toString(); 
        aliasName += getElementQName() == null ? EMPTY_STRING 
                : ELEMENT_EQ+getElementQName().toString(); 

        aliasName += getQuery() == null ? EMPTY_STRING : QUERY_EQ+getQuery();

        if (isRequirePropDescription()) {
            aliasName += getPropDescriptionPart() == null ? EMPTY_STRING 
                    : getPropDescriptionPart();
        }
        
        return aliasName;
    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.SHOW_POPERTY_EDITOR,
            ActionType.SEPARATOR,
            ActionType.DELETE_BPEL_EXT_FROM_WSDL,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }

    public Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        return new SimpleCustomEditor<PropertyAlias>(
                this, PropertyAliasMainPanel2.class, editingMode);
    }

    public void childrenUpdated(org.netbeans.modules.xml.xam.Component component) {
        PropertyAlias ref = getReference();
        if (ref == null) {
            return;
        }
        
        Query query = ref.getQuery();
        if (component.equals(query)) {
            updateName();
            updateAllProperties();
        }
        // TODO a
        // prop alis could became the children of another node
////        Node parentNode = getParentNode();
////        if (parentNode != null) {
////            parentNode = parentNode.getParentNode();
////        }
////        
////        if (parentNode != null) {
////            Children children = parentNode.getChildren();
////            if (children instanceof ImportWsdlChildren) {
////                ((ImportWsdlChildren)children).reload();
////            }
////        }
    }
    
}
