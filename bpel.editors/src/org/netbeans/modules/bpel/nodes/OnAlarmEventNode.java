/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.bpel.nodes;

import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.editors.api.BpelEditorConstants.AlarmEventType;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.DurationExpression;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.DeadlineExpression;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.RepeatEvery;
import org.netbeans.modules.bpel.model.api.TimeEvent;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;

/**
 * @author nk160297
 */
public final class OnAlarmEventNode extends BpelNode<OnAlarmEvent>
{
    /**{@inheritDoc}*/
    public OnAlarmEventNode(OnAlarmEvent reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public OnAlarmEventNode(OnAlarmEvent reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    /**{@inheritDoc}*/
    public NodeType getNodeType() {
        return NodeType.ALARM_EVENT_HANDLER;
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
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                PropertyType.ALARM_EVENT_TYPE,
                "getAlarmEventType", "setAlarmEventType"); // NOI18N
        //
        PropertyUtils.registerElementProperty(this, null, mainPropertySet,
                For.class, PropertyType.FOR_EXPRESSION,
                "getTimeEvent", "setTimeEvent", null); // NOI18N
        //
        PropertyUtils.registerElementProperty(this, null, mainPropertySet,
                DeadlineExpression.class, PropertyType.UNTIL_EXPRESSION,
                "getTimeEvent", "setTimeEvent", null); // NOI18N
        //
        PropertyUtils.registerElementProperty(this, null, mainPropertySet,
                RepeatEvery.class, PropertyType.REPEAT_EVERY_EXPRESSION,
                "getRepeatEvery", "setRepeatEvery", "removeRepeatEvery"); // NOI18N
        //
        updateAlarmEventTypeState(null, sheet.toArray());
        //
        return sheet;
    }
    
    public AlarmEventType getAlarmEventType() {
        OnAlarmEvent alarmEvent = getReference();
        if (alarmEvent == null) {
            return null;
        }
        TimeEvent timeEvent = alarmEvent.getTimeEvent();
        DurationExpression repeatEvery = alarmEvent.getRepeatEvery();
        //
        AlarmEventType result = AlarmEventType.INVALID;
        //
        if (timeEvent == null && repeatEvery == null) {
            result = AlarmEventType.NOT_ASSIGNED;
        } else if (timeEvent == null && repeatEvery != null) {
            result = AlarmEventType.REPEAT_TIME;
        } else if (timeEvent != null && repeatEvery == null) {
            if (timeEvent instanceof For) {
                result = AlarmEventType.FOR_TIME;
            } else if (timeEvent instanceof DeadlineExpression) {
                result = AlarmEventType.UNTIL_TIME;
            }
        } else if (timeEvent != null && repeatEvery != null) {
            if (timeEvent instanceof For) {
                result = AlarmEventType.FOR_REPEAT_TIME;
            } else if (timeEvent instanceof DeadlineExpression) {
                result = AlarmEventType.UNTIL_REPEAT_TIME;
            }
        }
        //
        return result;
    }
    
    public void setAlarmEventType(AlarmEventType newValue) throws Exception {
        AlarmEventType oldValue = getAlarmEventType();
        //
        if (newValue == oldValue) {
            return; // do nothing
        }
        //
        final OnAlarmEvent alarmEvent = getReference();
        if (alarmEvent == null) {
            return;
        }
        
        BpelModel model = alarmEvent.getBpelModel();
        final BPELElementsBuilder builder = model.getBuilder();
        //
        switch (newValue) {
        case REPEAT_TIME:
            model.invoke(new Callable() {
                public Object call() throws Exception {
                    if (alarmEvent.getRepeatEvery() == null) {
                        alarmEvent.setRepeatEvery(builder.createRepeatEvery());
                    }
                    alarmEvent.removeTimeEvent();
                    return null;
                }
            }, OnAlarmEventNode.this);
            break;
        case FOR_TIME:
            model.invoke(new Callable() {
                public Object call() throws Exception {
                    alarmEvent.removeRepeatEvery();
                    TimeEvent timeEvent = alarmEvent.getTimeEvent();
                    if (timeEvent == null || !(timeEvent instanceof For)) {
                        alarmEvent.setTimeEvent(builder.createFor());
                    }
                    return null;
                }
            }, OnAlarmEventNode.this);
            break;
        case UNTIL_TIME:
            model.invoke(new Callable() {
                public Object call() throws Exception {
                    alarmEvent.removeRepeatEvery();
                    TimeEvent timeEvent = alarmEvent.getTimeEvent();
                    if (timeEvent == null ||
                            !(timeEvent instanceof DeadlineExpression)) {
                        alarmEvent.setTimeEvent(builder.createUntil());
                    }
                    return null;
                }
            }, OnAlarmEventNode.this);
            break;
        case FOR_REPEAT_TIME:
            model.invoke(new Callable() {
                public Object call() throws Exception {
                    TimeEvent timeEvent = alarmEvent.getTimeEvent();
                    if (timeEvent == null || !(timeEvent instanceof For)) {
                        alarmEvent.setTimeEvent(builder.createFor());
                    }
                    if (alarmEvent.getRepeatEvery() == null) {
                        alarmEvent.setRepeatEvery(builder.createRepeatEvery());
                    }
                    return null;
                }
            }, OnAlarmEventNode.this);
            break;
        case UNTIL_REPEAT_TIME:
            model.invoke(new Callable() {
                public Object call() throws Exception {
                    TimeEvent timeEvent = alarmEvent.getTimeEvent();
                    if (timeEvent == null ||
                            !(timeEvent instanceof DeadlineExpression)) {
                        alarmEvent.setTimeEvent(builder.createUntil());
                    }
                    if (alarmEvent.getRepeatEvery() == null) {
                        alarmEvent.setRepeatEvery(builder.createRepeatEvery());
                    }
                    return null;
                }
            }, OnAlarmEventNode.this);
            break;
        case NOT_ASSIGNED:
        case INVALID:
            break;
        }
        //
        updateAlarmEventTypeState(getAlarmEventType(), getPropertySets());
    }
    
    public void updateAlarmEventTypeState(
            AlarmEventType alarmEventType, Node.PropertySet[] propSetArr) {
        Property prop;
        if (alarmEventType == null) {
            alarmEventType = getAlarmEventType();
        }
        //
        boolean isForHidden = true;
        boolean isUntilHidden = true;
        boolean isRepeatEveryHidden = true;
        //
        switch (alarmEventType) {
        case REPEAT_TIME:
            isForHidden = true;
            isUntilHidden = true;
            isRepeatEveryHidden = false;
            break;
        case FOR_TIME:
            isForHidden = false;
            isUntilHidden = true;
            isRepeatEveryHidden = true;
            break;
        case UNTIL_TIME:
            isForHidden = true;
            isUntilHidden = false;
            isRepeatEveryHidden = true;
            break;
        case FOR_REPEAT_TIME:
            isForHidden = false;
            isUntilHidden = true;
            isRepeatEveryHidden = false;
            break;
        case UNTIL_REPEAT_TIME:
            isForHidden = true;
            isUntilHidden = false;
            isRepeatEveryHidden = false;
            break;
        case NOT_ASSIGNED:
        case INVALID:
            isForHidden = true;
            isUntilHidden = true;
            isRepeatEveryHidden = true;
            break;
        }
        
        prop = PropertyUtils.lookForPropertyByType(
                propSetArr, PropertyType.FOR_EXPRESSION);
        if (prop != null) {
            prop.setHidden(isForHidden);
        }
        //
        prop = PropertyUtils.lookForPropertyByType(
                propSetArr, PropertyType.UNTIL_EXPRESSION);
        if (prop != null) {
            prop.setHidden(isUntilHidden);
        }
        //
        prop = PropertyUtils.lookForPropertyByType(
                propSetArr, PropertyType.REPEAT_EVERY_EXPRESSION);
        if (prop != null) {
            prop.setHidden(isRepeatEveryHidden);
        }
        //
        firePropertySetsChange(null, null);
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.ADD_FROM_PALETTE,
            ActionType.SEPARATOR,
            ActionType.GO_TO_SOURCE,
            ActionType.GO_TO_DIAGRAMM,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.SHOW_BPEL_MAPPER,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }
    public String getHelpId() {
        return "orch_elements_event_handler_onalarm"; //NOI18N
    }
}
