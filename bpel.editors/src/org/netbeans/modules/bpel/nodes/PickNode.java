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
