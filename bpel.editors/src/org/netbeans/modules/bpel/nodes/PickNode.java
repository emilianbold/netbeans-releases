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

import javax.swing.Action;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.CreateInstanceActivity;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.nodes.actions.AddOnAlarmAction;
import org.netbeans.modules.bpel.nodes.actions.AddOnMessageAction;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author nk160297
 */
public class PickNode extends BpelNode<Pick> {
    
    public PickNode(Pick reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public PickNode(Pick reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.PICK;
    }
    
    public String getHelpId() {
        return getNodeType().getHelpId();
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
        
        InstanceRef myInstanceRef = new InstanceRef() {
            public Object getReference() {
                return PickNode.this;
            }
            public Object getAlternativeReference() {
                return null;
            }
        };
        
        PropertyUtils.registerAttributeProperty(myInstanceRef, mainPropertySet,
                CreateInstanceActivity.CREATE_INSTANCE, CREATE_INSTANCE,
                "getCreateInstance", "setCreateInstance", null); // NOI18N
        
        
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        return sheet;
    }
    
    protected ActionType[] getActionsArray() {
        if (isModelReadOnly()) {
            return new ActionType[] {
                ActionType.GO_TO_SOURCE,
                ActionType.GO_TO_DIAGRAMM,
                ActionType.SEPARATOR,
                ActionType.TOGGLE_BREAKPOINT,
                ActionType.SEPARATOR,
                ActionType.REMOVE,
                ActionType.SEPARATOR,
                ActionType.PROPERTIES 
            };
        }
        
        return new ActionType[] {
            ActionType.ADD_NEWTYPES,
            ActionType.SEPARATOR,
            ActionType.WRAP,
            ActionType.SEPARATOR,
            ActionType.GO_TO_SOURCE,
            ActionType.GO_TO_DIAGRAMM,
            ActionType.SEPARATOR,
            ActionType.MOVE_UP,
            ActionType.MOVE_DOWN,
            ActionType.SEPARATOR,
            ActionType.TOGGLE_BREAKPOINT,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
    
    public ActionType[] getAddActionArray() {
        return new ActionType[] {
            ActionType.ADD_ON_MESSAGE,
            ActionType.ADD_ON_ALARM
        };
    }
    
    public Action createAction(ActionType actionType) {
        Action action = null;
        switch (actionType) {
            case ADD_ON_MESSAGE: 
                action = SystemAction.get(AddOnMessageAction.class);
                break;
            case ADD_ON_ALARM: 
                action = SystemAction.get(AddOnAlarmAction.class);
                break;
            default: 
                action = super.createAction(actionType);
        }
        
        return action;
    }
    public Boolean getCreateInstance() {
        Pick pick = getReference();
        if (pick  != null) {
            TBoolean isCreateInstance = pick .getCreateInstance();
            if (TBoolean.YES.equals(isCreateInstance)) {
                return Boolean.TRUE;
            }
        }
        //
        return Boolean.FALSE;
    }
    
    public void setCreateInstance(Boolean newValue) {
        Pick pick  = getReference();
        if (pick  != null) {
            if (Boolean.TRUE.equals(newValue)) {
                pick .setCreateInstance(TBoolean.YES);
            } else {
                pick .setCreateInstance(TBoolean.NO);
            }
        }
    }
}
