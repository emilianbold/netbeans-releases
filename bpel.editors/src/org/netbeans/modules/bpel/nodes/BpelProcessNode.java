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

import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.nodes.dnd.BpelEntityPasteType;
import org.netbeans.modules.bpel.nodes.dnd.SequenceEntityPasteType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class BpelProcessNode extends BaseScopeNode<Process> {
    
    public BpelProcessNode(Process reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public BpelProcessNode(Process reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.PROCESS;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();

        if (getReference() == null) {
            return sheet;
        }
        //
        Sheet.Set mainPropertySet = 
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                Process.TARGET_NAMESPACE, TARGET_NAMESPACE, 
                "getTargetNamespace", "setTargetNamespace", null); // NOI18N
        //
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                ATOMIC_PROCESS, "isAtomic", "setAtomic", null); // NOI18N
        //
        PropertyUtils.registerProperty(this, mainPropertySet,
                DOCUMENTATION, "getDocumentation", "setDocumentation", "removeDocumentation"); // NOI18N
        //
        return sheet;
    }
    
    @Override
    protected boolean isDropNodeInstanceSupported(BpelNode childNode) {
        if (!isDropNodeSupported(childNode)) {
            return false;
        }
        
        if (childNode instanceof PartnerLink
            && getReference() != null 
            && getReference().getPartnerLinkContainer() != null) 
        {
            return true;
        }
        
        if (childNode instanceof Activity
            && getReference() != null 
            && getReference().getActivity() == null) 
        {
            return true;
        }
        
        
        return false;
    }
    
    public BpelEntityPasteType createSupportedPasteType(BpelNode childNode) {
        if (!isDropNodeInstanceSupported(childNode)) {
            return null;
        }
        if (childNode instanceof Activity) {
            return new SequenceEntityPasteType(getReference(),(BpelEntity)childNode.getReference());
        }
        
        return null;
    }
    
    @Override
    protected ActionType[] getActionsArray() {
        if (isModelReadOnly()) {
            return new ActionType[] {
//                ActionType.GO_TO_SOURCE,
//                ActionType.GO_TO_DIAGRAMM,
                ActionType.GO_TO,
                ActionType.SEPARATOR,
                ActionType.PROPERTIES
            };
        } 
        
        return new ActionType[] {
            ActionType.ADD_NEWTYPES,
            ActionType.SEPARATOR,
            ActionType.ADD_FROM_PALETTE,
            ActionType.SEPARATOR,
//            ActionType.GO_TO_SOURCE,
//            ActionType.GO_TO_DIAGRAMM,
            ActionType.GO_TO,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }

    @Override
    public ActionType[] getAddActionArray() {
        return new ActionType[] {
            ActionType.ADD_VARIABLE,
            ActionType.ADD_CORRELATION_SET,
            // ActionType.ADD_MESSAGE_EXCHANGE, // Issue 85553
            ActionType.ADD_PARTNER_LINK,
            ActionType.ADD_EVENT_HANDLERS,
            ActionType.ADD_FAULT_HANDLERS,
            ActionType.ADD_WSDL_IMPORT,
            ActionType.ADD_SCHEMA_IMPORT
        };
    }

    @Override
    protected String getImplHtmlDisplayName() {
        String name = getName();
        if (name == null) {
            return getNodeType().getDisplayName();
        }
//        name = name.replaceAll("&","&amp;"); // NOI18N
        return name;
    }
    
    public Boolean isAtomic() {
        if (TBoolean.YES.equals(getReference().isAtomic())) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
    
    public void setAtomic(Boolean newValue) {
        if (newValue == null) {
            // default atomic is no.
            getReference().setAtomic(TBoolean.NO);
        } else {
            getReference().setAtomic(
                    newValue == Boolean.TRUE ? TBoolean.YES : TBoolean.NO);
        }
    }
}
