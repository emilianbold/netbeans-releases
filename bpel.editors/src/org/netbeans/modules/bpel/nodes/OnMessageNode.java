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
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.PortTypeReference;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.props.CustomEditorProperty;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.editors.OnMessageCustomEditor;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author nk160297
 */
public class OnMessageNode extends BpelNode<OnMessage> {
    
    public OnMessageNode(OnMessage reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public OnMessageNode(OnMessage reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.MESSAGE_HANDLER;
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
    
//    protected String getImplShortDescription() {
//        OnMessage ref = getReference();
//        StringBuffer result = new StringBuffer();
//        result.append(getName());
//        result.append(ref == null || ref.getMessageExchange() == null ? EMPTY_STRING
//                : MESSAGE_EXCHANGE_EQ+ref.getMessageExchange().getRefString());
//        
//        return NbBundle.getMessage(OnMessageNode.class,
//                "LBL_ON_MESSAGE_NODE_TOOLTIP", // NOI18N
//                result.toString()
//                );
//    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        if (getReference() == null) {
            // The related object has been removed!
            return sheet;
        }
        //
        Sheet.Set messagePropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MESSAGE_SET);
        //
        CustomEditorProperty customizer = new CustomEditorProperty(this);
        messagePropertySet.put(customizer);
        //
        Node.Property property;
        //
        
// Issue 85553 start        
//        property = PropertyUtils.registerAttributeProperty(this,
//                messagePropertySet,
//                MessageExchangeReference.MESSAGE_EXCHANGE, MESSAGE_EXCHANGE,
//                "getMessageExchange", "setMessageExchange",  // NOI18N
//                "removeMessageExchange"); // NOI18N
//        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
// Issue 85553 end        
        
        //
        property = PropertyUtils.registerAttributeProperty(this,
                messagePropertySet,
                PartnerLinkReference.PARTNER_LINK, PARTNER_LINK,
                "getPartnerLink", "setPartnerLink", null); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        property = PropertyUtils.registerAttributeProperty(this,
                messagePropertySet,
                PortTypeReference.PORT_TYPE, PORT_TYPE,
                "getPortType", "setPortType", "removePortType"); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        property = PropertyUtils.registerAttributeProperty(this,
                messagePropertySet,
                OperationReference.OPERATION, OPERATION,
                "getOperation", "setOperation", null); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        property = PropertyUtils.registerAttributeProperty(this,
                messagePropertySet,
                VariableReference.VARIABLE, INPUT,
                "getVariable", "setVariable", "removeVariable"); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        return sheet;
    }
    
    public Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        return new OnMessageCustomEditor(this, editingMode);
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.ADD_FROM_PALETTE,
            ActionType.SEPARATOR,
            ActionType.GO_TO_SOURCE,
            ActionType.GO_TO_DIAGRAMM,
            ActionType.SEPARATOR,
//            ActionType.CYCLE_MEX, // Issue 85553
            ActionType.SHOW_POPERTY_EDITOR,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
}
