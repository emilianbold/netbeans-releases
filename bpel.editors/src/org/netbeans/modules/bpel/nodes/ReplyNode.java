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

import java.awt.Component;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.FaultNameReference;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.PortTypeReference;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.properties.editors.ReplyCustomEditor;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.props.CustomEditorProperty;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;

/**
 * @author nk160297
 */
public class ReplyNode extends BpelNode<Reply> {
    
    public ReplyNode(Reply reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }

    public ReplyNode(Reply reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.REPLY;
    }
    
    @Override
    protected boolean isEventRequreUpdate(ChangeEvent event) {
        if (super.isEventRequreUpdate(event)) {
            return true;
        }
        BpelEntity entity = event.getParent();

        if (entity == null) {
            return false;
        }
        Object ref = getReference();
        return  ref != null && ref == entity.getParent() && entity.getElementType() == CorrelationContainer.class;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();

        if (getReference() == null) {
            return sheet;
        }
        //
        InstanceRef myInstanceRef = new InstanceRef() {
            public Object getReference() {
                return ReplyNode.this;
            }
            public Object getAlternativeReference() {
                return ReplyNode.this.getReference();
            }
        };
        //
        Sheet.Set mainPropertySet = 
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        CustomEditorProperty customizer = new CustomEditorProperty(this);
        mainPropertySet.put(customizer);
        //
        Node.Property property;
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        Sheet.Set messagePropertySet = 
                getPropertySet(sheet, Constants.PropertiesGroups.MESSAGE_SET);
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
        property = PropertyUtils.registerAttributeProperty(myInstanceRef, 
                messagePropertySet,
                null, OUTPUT, 
                "getOutputVariable", "setVariable", "removeVariable"); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        Sheet.Set faultPropertySet = 
                getPropertySet(sheet, Constants.PropertiesGroups.FAULT_SET);
        //
        property = PropertyUtils.registerAttributeProperty(this, 
                faultPropertySet,
                FaultNameReference.FAULT_NAME, FAULT_NAME_RO, 
                "getFaultName", null, null); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        property = PropertyUtils.registerAttributeProperty(myInstanceRef, 
                faultPropertySet,
                null, FAULT_VARIABLE_REF, 
                "getFaultVariable", "setVariable", "removeVariable"); // NOI18N
        property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        return sheet;
    }
    
    public BpelReference<VariableDeclaration> getOutputVariable() {
        Reply reply = getReference();
        if (reply != null) {
            QName faultName = reply.getFaultName();
            if (faultName == null) {
                return reply.getVariable();
            }
        }
        //
        return null;
    }
    
    public BpelReference<VariableDeclaration> getFaultVariable() {
        Reply reply = getReference();

        if (reply != null) {
            QName faultName = reply.getFaultName();
            if (faultName != null) {
                return reply.getVariable();
            }
        }
        return null;
    }
    
    public String getHelpId() {
        return getNodeType().getHelpId();
    }
    
    public Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        return new ReplyCustomEditor(this, editingMode);
    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.GO_TO_DIAGRAMM,
            ActionType.SEPARATOR,
            ActionType.WRAP,
            ActionType.SEPARATOR,
            ActionType.MOVE_UP,
            ActionType.MOVE_DOWN,
            ActionType.SEPARATOR,
            ActionType.SHOW_POPERTY_EDITOR,
            ActionType.SEPARATOR,
            ActionType.TOGGLE_BREAKPOINT,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
    }    

    protected void updateComplexProperties(ChangeEvent event) {
        if (event instanceof PropertyUpdateEvent) {
            BpelEntity parentEvent = event.getParent();
            if (parentEvent != null && parentEvent.equals(this.getReference())) {
                String propName = event.getName();
                if (VariableReference.VARIABLE.equals(propName)) {
                    updateProperty(PropertyType.FAULT_VARIABLE_REF);
                    updateProperty(PropertyType.OUTPUT);
                }
            }
        }
    }
}
