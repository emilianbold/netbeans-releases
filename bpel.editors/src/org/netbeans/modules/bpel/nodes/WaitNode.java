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

import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.editors.api.BpelEditorConstants.AlarmType;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.Wait;

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.DeadlineExpression;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.TimeEvent;
import org.netbeans.modules.bpel.model.api.TimeEventHolder;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;

import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.10
 */
public final class WaitNode extends BpelNode<Wait> {
    
    /**{@inheritDoc}*/
    public WaitNode(Wait reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    /**{@inheritDoc}*/
    public NodeType getNodeType() {
        return NodeType.WAIT;
    }
    
    public String getHelpId() {
        return getNodeType().getHelpId();
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();

        if (getReference() == null) {
            return sheet;
        }
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        
        PropertyUtils propUtil = PropertyUtils.getInstance();
        propUtil.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, PropertyType.NAME,
                "getName", "setName", null); // NOI18N
        //
        propUtil.registerCalculatedProperty(this, mainPropertySet,
                PropertyType.ALARM_TYPE,
                "getAlarmType", "setAlarmType"); // NOI18N
        //
        propUtil.registerElementProperty(this, null, mainPropertySet,
                For.class, PropertyType.FOR_EXPRESSION,
                "getTimeEvent", "setTimeEvent", null); // NOI18N
        //
        propUtil.registerElementProperty(this, null, mainPropertySet,
                DeadlineExpression.class, PropertyType.UNTIL_EXPRESSION,
                "getTimeEvent", "setTimeEvent", null); // NOI18N
        //
        updateAlarmTypeState(null, sheet.toArray());
        //
        propUtil.registerProperty(this, mainPropertySet,
                DOCUMENTATION, "getDocumentation", "setDocumentation", "removeDocumentation"); // NOI18N
        //
        return sheet;
    }
    
    public AlarmType getAlarmType() {
        TimeEventHolder eventHolder = getReference();
        if (eventHolder == null) {
            return null;
        }
        TimeEvent timeEvent = eventHolder.getTimeEvent();
        //
        AlarmType result = AlarmType.INVALID;
        //
        if (timeEvent == null) {
            result = AlarmType.NOT_ASSIGNED;
        } else if (timeEvent != null) {
            if (timeEvent instanceof For) {
                result = AlarmType.FOR_TIME;
            } else if (timeEvent instanceof DeadlineExpression) {
                result = AlarmType.UNTIL_TIME;
            }
        }
        //
        return result;
    }
    
    public void setAlarmType(AlarmType newValue) throws Exception {
        AlarmType oldValue = getAlarmType();
        //
        if (newValue == oldValue) {
            return; // do nothing
        }
        //
        final TimeEventHolder eventHolder = getReference();
        if (eventHolder == null) {
            return;
        }
        
        BpelModel model = eventHolder.getBpelModel();
        final BPELElementsBuilder builder = model.getBuilder();
        //
        switch (newValue) {
            case FOR_TIME:
                model.invoke(new Callable() {
                    public Object call() throws Exception {
                        TimeEvent timeEvent = eventHolder.getTimeEvent();
                        if (timeEvent == null || !(timeEvent instanceof For)) {
                            eventHolder.setTimeEvent(builder.createFor());
                        }
                        return null;
                    }
                }, WaitNode.this);
                break;
            case UNTIL_TIME:
                model.invoke(new Callable() {
                    public Object call() throws Exception {
                        TimeEvent timeEvent = eventHolder.getTimeEvent();
                        if (timeEvent == null || 
                                !(timeEvent instanceof DeadlineExpression)) {
                            eventHolder.setTimeEvent(builder.createUntil());
                        }
                        return null;
                    }
                }, WaitNode.this);
                break;
            case NOT_ASSIGNED:
            case INVALID:
                break;
        }
        //
        updateAlarmTypeState(getAlarmType(), getPropertySets());
    }
    
    public void updateAlarmTypeState(
            AlarmType alarmType, Node.PropertySet[] propSetArr) {
        Property prop;
        if (alarmType == null) {
            alarmType = getAlarmType();
        }
        //
        boolean isForHidden = true;
        boolean isUntilHidden = true;
        //
        switch (alarmType) {
            case FOR_TIME:
                isForHidden = false;
                isUntilHidden = true;
                break;
            case UNTIL_TIME:
                isForHidden = true;
                isUntilHidden = false;
                break;
            case NOT_ASSIGNED:
            case INVALID:
                isForHidden = true;
                isUntilHidden = true;
                break;
        }
        
        PropertyUtils propUtil = PropertyUtils.getInstance();
        prop = propUtil.lookForPropertyByType(
                propSetArr, PropertyType.FOR_EXPRESSION);
        if (prop != null) {
            prop.setHidden(isForHidden);
        }
        //
        prop = propUtil.lookForPropertyByType(
                propSetArr, PropertyType.UNTIL_EXPRESSION);
        if (prop != null) {
            prop.setHidden(isUntilHidden);
        }
        //
        firePropertySetsChange(null, null);
    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
//            ActionType.GO_TO_SOURCE,
//            ActionType.GO_TO_DIAGRAMM,
            ActionType.GO_TO,
            ActionType.SEPARATOR,
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

}
