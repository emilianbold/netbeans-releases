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
import org.netbeans.modules.bpel.editors.api.BpelEditorConstants.AlarmType;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

import org.netbeans.modules.bpel.model.api.TimeEventHolder;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.DeadlineExpression;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.TimeEvent;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.18
 * @author nk160297
 */
public final class OnAlarmNode extends BpelNode<TimeEventHolder>
{
    /**{@inheritDoc}*/
    public OnAlarmNode(TimeEventHolder reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public OnAlarmNode(TimeEventHolder reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    /**{@inheritDoc}*/
    public NodeType getNodeType() {
        return NodeType.ALARM_HANDLER;
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
                PropertyType.ALARM_TYPE,
                "getAlarmType", "setAlarmType"); // NOI18N
        //
        PropertyUtils.registerElementProperty(this, null, mainPropertySet,
                For.class, PropertyType.FOR_EXPRESSION,
                "getTimeEvent", "setTimeEvent", null); // NOI18N
        //
        PropertyUtils.registerElementProperty(this, null, mainPropertySet,
                DeadlineExpression.class, PropertyType.UNTIL_EXPRESSION,
                "getTimeEvent", "setTimeEvent", null); // NOI18N
        //
        // mainPropertySet.put(new TimeEventTypeProperty(this));
        // mainPropertySet.put(new TimeEventValueProperty(this));
        //
        updateAlarmTypeState(null, sheet.toArray());
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
                }, OnAlarmNode.this);
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
                }, OnAlarmNode.this);
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
