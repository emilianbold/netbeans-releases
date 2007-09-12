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
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.TypeContainer;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
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
            // The related object has been removed!
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
        // property.setValue("suppressCustomEditor", Boolean.TRUE); // NOI18N
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
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.GO_TO_DIAGRAMM,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES
        };
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
     public String getHelpId() {
        return "orch_elements_scope_add_catch"; //NOI18N
    }

}
