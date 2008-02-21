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
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.CompletionCondition;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 */
public class ForEachNode extends BpelNode<ForEach> {
    
    public ForEachNode(ForEach reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public ForEachNode(ForEach reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.FOR_EACH;
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
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, PropertyType.NAME,
                "getName", "setName", null); // NOI18N
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                ForEach.COUNTER_NAME, PropertyType.COUNTER_NAME,
                "getCounterName", "setCounterName", null); // NOI18N
        //
        PropertyUtils.registerElementProperty(this, null, mainPropertySet,
                StartCounterValue.class, PropertyType.START_COUNTER_EXPR,
                "getStartCounterValue", "setStartCounterValue", null); // NOI18N
        //
        PropertyUtils.registerElementProperty(this, null, mainPropertySet,
                FinalCounterValue.class, PropertyType.FINAL_COUNTER_EXPR,
                "getFinalCounterValue", "setFinalCounterValue", null); // NOI18N
        //
        PropertyUtils.registerElementProperty(null, this, mainPropertySet,
                Branches.class, PropertyType.COMPLETION_CONDITION,
                "getCompletionCondition", "setCompletionCondition",     // NOI18N
                "removeCompletionCondition");                           // NOI18N
        //
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                PropertyType.COUNT_COMPLETED_BRANCHES_ONLY,
                "isCountCompletedOnly", "setCountCompletedOnly" ,       // NOI18N
                "removeCountCompletedOnly");                            // NOI18N
        //
        PropertyUtils.registerProperty(this, mainPropertySet,
                DOCUMENTATION, "getDocumentation", "setDocumentation", "removeDocumentation"); // NOI18N
        //
        return sheet;
    }
    
    //  Fix for #80613
    public void removeCompletionCondition() {
        ForEach forEach = getReference();
        if ( forEach != null ) {
            forEach.removeCompletionCondition();
        }
    }
    
    /**
     * Returns current Branches or null
     */
    private Branches getBranches() {
        ForEach forEachObj = getReference();
        if (forEachObj != null) {
            CompletionCondition cCond = forEachObj.getCompletionCondition();
            if (cCond != null) {
                Branches branches = cCond.getBranches();
                return branches;
            }
        }
        //
        return null;
    }
    
    /**
     * Checks if the CompletionCondition and Branches contaners exists.
     * Create them if they are absent and return the Branches.
     */
    private Branches createBranches() {
        ForEach forEachObj = getReference();
        if (forEachObj != null) {
            BPELElementsBuilder builder = forEachObj.getBpelModel().getBuilder();
            CompletionCondition cCond = forEachObj.getCompletionCondition();
            if (cCond == null) {
                cCond = builder.createCompletionCondition();
                forEachObj.setCompletionCondition(cCond);
                cCond = forEachObj.getCompletionCondition();
            }
            //
            Branches branches = cCond.getBranches();
            if (branches == null) {
                branches = builder.createBranches();
                cCond.setBranches(branches);
                branches = cCond.getBranches();
            }
            //
            return branches;
        }
        //
        return null;
    }
    
    public String getCompletionCondition() {
        Branches branches = getBranches();
        if (branches != null) {
            return branches.getContent();
        }
        //
        return null;
    }
    
    public void setCompletionCondition(String newValue) throws VetoException {
        String oldValue = getCompletionCondition();
        //
        if (oldValue != null  && newValue != null) {
            if (!oldValue.equals(newValue)) {
                Branches branches = getBranches();
                if (branches != null) {
                    branches.setContent(newValue);
                }
            }
        } else if (oldValue == null && newValue != null) {
            Branches branches = createBranches();
            if (branches != null) {
                branches.setContent(newValue);
            }
        } else if (oldValue != null && newValue == null) {
            assert false : "The null value should not be specified for the completion condition";
        }
    }
    
    public Boolean isCountCompletedOnly() {
        Branches branches = getBranches();
        if (branches != null) {
            TBoolean ccbo = branches.getCountCompletedBranchesOnly();
            if (TBoolean.YES.equals(ccbo)) {
                return Boolean.TRUE;
            }
        }
        //
        return Boolean.FALSE;
    }
    
    public void setCountCompletedOnly(Boolean newValue) {
        Boolean oldValue = isCountCompletedOnly();
        if (oldValue != null  && newValue != null) {
            if (!oldValue.equals(newValue)) {
                Branches branches = getBranches();
                setCountCompletedOnly(branches, newValue);
            }
        } else if (oldValue == null && newValue != null) {
            Branches branches = createBranches();
            setCountCompletedOnly(branches, newValue);
        } else if (oldValue != null && newValue == null) {
            assert false : "The null value should not be specified for the CountCompletedOnly";
        }
    }
    
    private void setCountCompletedOnly(Branches branches, Boolean newValue) {
        if (branches != null) {
            if (Boolean.TRUE.equals(newValue)) {
                branches.setCountCompletedBranchesOnly(TBoolean.YES);
            } else {
                branches.setCountCompletedBranchesOnly(TBoolean.NO);
            }
        }
    }
    
    public void removeCountCompletedOnly() {
        Branches branches = getBranches();
        if ( branches != null ) {
            branches.removeCountCompletedBranchesOnly();
        }
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
//            ActionType.GO_TO_SOURCE,
//            ActionType.GO_TO_DIAGRAMM,
            ActionType.GO_TO,
            ActionType.SEPARATOR,
            ActionType.ADD_FROM_PALETTE,
            ActionType.WRAP,
            ActionType.SEPARATOR,
            ActionType.MOVE_UP,
            ActionType.MOVE_DOWN,
            ActionType.SEPARATOR,
            ActionType.TOGGLE_BREAKPOINT,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
//            ActionType.SHOW_BPEL_MAPPER,
//            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
    
    public String getHelpId() {
        return getNodeType().getHelpId();
    }
    
    protected void updateComplexProperties(ChangeEvent event) {
        if (event instanceof EntityRemoveEvent) {
            BpelEntity oldEntityParent = event.getParent();
            if (oldEntityParent != null &&
                    oldEntityParent.equals(this.getReference())) {
                BpelEntity oldEntity =
                        ((EntityRemoveEvent)event).getOutOfModelEntity();
                if (oldEntity.getElementType() == CompletionCondition.class) {
                    updateProperty(PropertyType.COMPLETION_CONDITION);
                    updateProperty(PropertyType.COUNT_COMPLETED_BRANCHES_ONLY);
                }
            }
        } else if (event instanceof PropertyUpdateEvent) {
            BpelEntity parentEvent = event.getParent();
            if (parentEvent != null && parentEvent instanceof Branches) {
                ForEach forEach = getReference();
                if (forEach != null) {
                    CompletionCondition cc = forEach.getCompletionCondition();
                    if (cc != null) {
                        Branches brances = cc.getBranches();
                        if (brances != null && brances.equals(parentEvent)) {
                            updateProperty(PropertyType.COMPLETION_CONDITION);
                            updateProperty(PropertyType.COUNT_COMPLETED_BRANCHES_ONLY);
                        }
                    }
                }
            }
        }
    }
}
