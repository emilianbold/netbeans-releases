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
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.FaultNameReference;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.TypeContainer;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class CatchNode extends BpelNode<Catch> {
    
    public CatchNode(Catch reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public CatchNode(Catch reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.CATCH;
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
        Node.Property property = PropertyUtils.registerAttributeProperty(this, 
                mainPropertySet,
                FaultNameReference.FAULT_NAME, FAULT_NAME, 
                "getFaultName", "setFaultName", "removeFaultName"); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        property = PropertyUtils.registerAttributeProperty(this,
                mainPropertySet,
                Catch.FAULT_VARIABLE, FAULT_VARIABLE_NAME,
                "getFaultVariable", "setFaultVariable", "removeFaultVariable"); // NOI18N
        property.setValue("canEditAsText", Boolean.TRUE); // NOI18N
        //
        property = PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                FAULT_VARIABLE_TYPE, "getFaultVariableType", "setFaultVariableType"); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        PropertyUtils.registerProperty(this, mainPropertySet,
                DOCUMENTATION, "getDocumentation", "setDocumentation", "removeDocumentation"); // NOI18N
        //
        return sheet;
    }
    
    public TypeContainer getFaultVariableType() {
        Catch aCatch = getReference();
        if (aCatch == null) {
            return null;
        }
        SchemaReference<GlobalElement> elementRef = aCatch.getFaultElement();
        if (elementRef != null) {
            GlobalElement element = elementRef.get();
            if (element != null) {
                return new TypeContainer(element);
            }
        }
        //
        WSDLReference<Message> messageRef = aCatch.getFaultMessageType();
        if (messageRef != null) {
            Message message = messageRef.get();
            if (message != null) {
                return new TypeContainer(message);
            }
        }
        //
        return null;
    }
    
    public void setFaultVariableType(final TypeContainer typeContainer) throws Exception {
        final Catch aCatch = getReference();
        if (aCatch == null) {
            return;
        }
        BpelModel model = aCatch.getBpelModel();
        //
        switch (typeContainer.getStereotype()) {
            case MESSAGE:
                model.invoke(new Callable() {
                    public Object call() throws Exception {
                        aCatch.removeFaultElement();
                        Message message = typeContainer.getMessage();
                        WSDLReference<Message> messageRef =
                                aCatch.createWSDLReference(message, Message.class);
                        aCatch.setFaultMessageType(messageRef);
                        return null;
                    }
                }, CatchNode.this);
                break;
            case GLOBAL_ELEMENT:
                model.invoke(new Callable() {
                    public Object call() throws Exception {
                        aCatch.removeFaultMessageType();
                        GlobalElement gElement = typeContainer.getGlobalElement();
                        SchemaReference<GlobalElement> gElementRef =
                                aCatch.createSchemaReference(gElement, GlobalElement.class);
                        aCatch.setFaultElement(gElementRef);
                        return null;
                    }
                }, CatchNode.this);
                break;
            default:
                assert false : "The Schema Global Type isn't allowed here";
                break;
        }
    }
    
    protected void updateComplexProperties(ChangeEvent event) {
        if (event instanceof PropertyUpdateEvent || 
                event instanceof PropertyRemoveEvent) {
            BpelEntity parentEvent = event.getParent();
            if (parentEvent != null && parentEvent.equals(this.getReference())) {
                String propName = event.getName();
                if (Catch.FAULT_ELEMENT.equals(propName) ||
                        Catch.FAULT_MESSAGE_TYPE.equals(propName)) {
                    updateProperty(PropertyType.FAULT_VARIABLE_TYPE);
                } 
            }
        }
    }

    @Override
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.ADD_FROM_PALETTE,
            ActionType.SEPARATOR,
//            ActionType.GO_TO_SOURCE,
//            ActionType.GO_TO_DIAGRAMM,
            ActionType.GO_TO,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
    
     public String getHelpId() {
        return "orch_elements_scope_add_catch"; //NOI18N
    }

}
