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
import java.util.concurrent.Callable;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.FromPartContainer;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.MessageTypeReference;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.PortTypeReference;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.ImportRegistrationHelper;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.properties.props.CustomEditorProperty;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.netbeans.modules.bpel.properties.editors.OnEventCustomEditor;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

public class OnEventNode extends BpelNode<OnEvent> {
    
    public OnEventNode(OnEvent onEvent, Lookup lookup) {
        super(onEvent, lookup);
    }
    
    public OnEventNode(OnEvent onEvent, Children children, Lookup lookup) {
        super(onEvent, children, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.ON_EVENT;
    }
    
    @Override
            protected boolean isEventRequreUpdate(ChangeEvent event) {
        if (super.isEventRequreUpdate(event)) {
            return true;
        }
        
        //CorrelationContainer
        BpelEntity entity = event.getParent();
        if (entity == null) {
            return false;
        }
        Object ref = getReference();
        return  ref != null && ref == entity.getParent()
        && entity.getElementType() == CorrelationContainer.class;
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();

        if (getReference() == null) {
            return sheet;
        }
        //
        Sheet.Set messagePropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MESSAGE_SET);
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        CustomEditorProperty customizer = new CustomEditorProperty(this);
        messagePropertySet.put(customizer);
        //
        Node.Property property;
        PropertyUtils propUtil = PropertyUtils.getInstance();
        //
        property = propUtil.registerAttributeProperty(this, 
                messagePropertySet,
                PartnerLinkReference.PARTNER_LINK, PropertyType.PARTNER_LINK,
                "getPartnerLink", "setPartnerLink", null); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        property = propUtil.registerAttributeProperty(this, 
                messagePropertySet,
                PortTypeReference.PORT_TYPE, PropertyType.PORT_TYPE,
                "getPortType", "setPortType", "removePortType"); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        InstanceRef selfReference = new InstanceRef() {
            public Object getReference() {
                return OnEventNode.this.getReference();
            }
            public Object getAlternativeReference() {
                return OnEventNode.this;
            }
        };
        //
        property = propUtil.registerAttributeProperty(selfReference,
                messagePropertySet,
                OperationReference.OPERATION, PropertyType.OPERATION,
                "getOperation", "setOperationLocal", null); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        property = propUtil.registerAttributeProperty(selfReference, 
                messagePropertySet,
                VariableReference.VARIABLE, PropertyType.EVENT_VARIABLE_NAME,
                "getVariable", "setVariableLocal", "removeVariableLocal"); // NOI18N
        property.setValue("canEditAsText", Boolean.TRUE); // NOI18N
        //
        property = propUtil.registerAttributeProperty(selfReference, 
                messagePropertySet,
                MessageTypeReference.MESSAGE_TYPE, PropertyType.VARIABLE_TYPE_QNAME,
                "getVariableTypeQName", null, null); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        propUtil.registerProperty(this, mainPropertySet,
                DOCUMENTATION, "getDocumentation", "setDocumentation", "removeDocumentation"); // NOI18N
        //
        return sheet;
    }
    
    public Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        return new OnEventCustomEditor(this, editingMode);
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.ADD_FROM_PALETTE,
            ActionType.SEPARATOR,
//            ActionType.GO_TO_SOURCE,
//            ActionType.GO_TO_DIAGRAMM,
            ActionType.GO_TO,
            ActionType.GO_TO_REFERENCE,
            ActionType.SEPARATOR,
            ActionType.TOGGLE_BREAKPOINT,
            ActionType.SEPARATOR,
//            ActionType.CYCLE_MEX, // Issue 85553
//            ActionType.SEPARATOR,
            ActionType.SHOW_POPERTY_EDITOR,
            ActionType.SEPARATOR,
            
            ActionType.DEFINE_CORRELATION,
            ActionType.SEPARATOR,
            
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
    
    public void setOperationLocal( final WSDLReference<Operation> value )  throws Exception{
        final OnEvent event = getReference();
        if (event != null) {
            //
     
            event.getBpelModel().invoke(new Callable() {
                public Object call() throws Exception {
                    event.setOperation(value);
                    updateVarTypeAttribute(event);
                    return null;
                }
            }, this);
        }
    }
    
    public void setVariableLocal(final String newValue) throws Exception {
        if (newValue == null || newValue.length() == 0) {
            removeVariableLocal();
            return;
        }
        //
        final OnEvent event = getReference();
        if (event != null) {
            //
            // TODO: Check if it necessary to remove the FromParts
            // it maybe worth to ask if user wants to remove them before at the
            // validation time.
            //
            event.getBpelModel().invoke(new Callable() {
                public Object call() throws Exception {
                    event.setVariable(newValue);
                    //
                    // Remove all FromPart elements
                    FromPartContainer fromPartContainer = event.getFromPartContaner();
                    
                    FromPart[] fromPartArr = fromPartContainer == null 
                            ? null 
                            : fromPartContainer.getFromParts();
                    if (fromPartArr != null) {
                        for (int index = 0; index < fromPartArr.length; index++) {
                            fromPartContainer.removeFromPart(index);
                        }
                    }
                    //
                    updateVarTypeAttribute(event);
                    return null;
                }
            }, this);
        }
    }
    
    public void removeVariableLocal() throws Exception {
        final OnEvent event = getReference();
        if (event != null) {
            event.getBpelModel().invoke(new Callable() {
                public Object call() throws Exception {
                    event.removeVariable();
                    event.removeMessageType();
                    event.removeElement();
                    return null;
                }
            }, this);
        }
    }
    
    public static void updateVarTypeAttribute(OnEvent event) {
        String varName = event.getVariable();
        if (varName != null && varName.length() != 0) {
            WSDLReference<Operation> operRef = event.getOperation();
            if (operRef != null) {
                Operation oper = operRef.get();
                if (oper != null) {
                    Input input = oper.getInput();
                    if (input != null) {
                        NamedComponentReference<Message> msgRef =
                                input.getMessage();
                        if (msgRef != null) {
                            Message msg = msgRef.get();
                            if (msg != null) {
                                addTypeAttributes(event, msg);
                            }
                        }
                    }
                }
            }
        } else {
            event.removeMessageType();
            event.removeElement();
        }
    }
    
    private static void addTypeAttributes(final OnEvent event, final Message newMsg) {
        GlobalElement newElement = null;
        GlobalElement oldElement = null;
        Message oldMessage = null;
        //
        // TODO: Uncomment when the runtime will support the element attribute
        //
//        // Try to calculate the new Element type
//        Collection<Part> parts = newMsg.getParts();
//        if (parts.size() == 1){
//            Part part = parts.iterator().next();
//            if (part != null) {
//                NamedComponentReference<GlobalElement> elementRef = part.getElement();
//                if (elementRef != null) {
//                    newElement = elementRef.get();
//                }
//            }
//        }
//        //
//        // Try to calculate the old Element type
//        SchemaReference<GlobalElement> elementRef = event.getElement();
//        if (elementRef != null) {
//            oldElement = elementRef.get();
//        }
        //
        // Tyr to calculate the old Message type
        WSDLReference<Message> msgRef = event.getMessageType();
        if (msgRef != null) {
            oldMessage = msgRef.get();
        }
        //
        
        if ((oldMessage == null && oldElement == null) ||
                oldMessage != null && oldElement != null) {
            if (newElement != null) {
                setElementType(event, newElement);
            } else if (newMsg != null) {
                setMessageType(event, newMsg);
            }
        } else if (oldMessage == null && oldElement != null) {
            if (newElement != null) {
                if (!oldElement.equals(newElement)) {
                    setElementType(event, newElement);
                }
            } else if (newMsg != null) {
                setMessageType(event, newMsg);
            }
        } else if (oldMessage != null && oldElement == null) {
            if (newMsg != null) {
                if (!oldMessage.equals(newMsg)) {
                    setMessageType(event, newMsg);
                }
            } else if (newElement != null){
                setElementType(event, newElement);
            }
        }
    }
    
    private static void setMessageType(final OnEvent event, final Message message) {
        WSDLReference<Message> msgRef =
                event.createWSDLReference(message, Message.class);
        event.setMessageType(msgRef);
        //
        event.removeElement();
    }
    
    private static void setElementType(final OnEvent event, final GlobalElement element) {
        SchemaReference<GlobalElement> elementBpelRef =
                event.createSchemaReference(element, GlobalElement.class);
        event.setElement(elementBpelRef);
        //
        event.removeMessageType();
        //
        SchemaModel model = element.getModel();

        if (model != null){
            new ImportRegistrationHelper(event.getBpelModel()).addImport(model);
        }
    }
    
    public QName getVariableTypeQName() {
        QName result = null;
        OnEvent event = getReference();
        if (event != null) {
            WSDLReference<Message> msgRef = event.getMessageType();
            if (msgRef != null) {
                result = msgRef.getQName();
                if (result != null) {
                    return result;
                }
            }
            //
            SchemaReference<GlobalElement> elementRef = event.getElement();
            if (elementRef != null) {
                result = elementRef.getQName();
                if (result != null) {
                    return result;
                }
            }
        }
        //
        return null;
    }
    
    protected void updateComplexProperties(ChangeEvent event) {
        if (event instanceof PropertyUpdateEvent) {
            BpelEntity parentEvent = event.getParent();
            if (parentEvent != null && parentEvent.equals(this.getReference())) {
                String propName = event.getName();
                if (MessageTypeReference.MESSAGE_TYPE.equals(propName)) {
                    updateProperty(PropertyType.MESSAGE_TYPE);
                }
            }
        }
    }
    
    public String getHelpId() {
        return "org.netbeans.modules.bpel.properties.editors.OnEventCustomEditor"; //NOI18N
    }
}
