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
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.properties.props.CustomEditorProperty;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.netbeans.modules.xml.xam.Reference;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.netbeans.modules.bpel.properties.editors.CorrelationPropertyMainPanel;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class CorrelationPropertyNode extends BpelWSDLNode<CorrelationProperty> {
    
    public CorrelationPropertyNode(CorrelationProperty property, Children children, Lookup lookup) {
        super(property, children, lookup);
    }
    
    public CorrelationPropertyNode(CorrelationProperty property, Lookup lookup) {
        super(property, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.CORRELATION_PROPERTY;
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        CustomEditorProperty customizer = new CustomEditorProperty(this);
        mainPropertySet.put(customizer);
        //
        Property prop = null;
        //
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                NAME, "getPropertyName", "setPropertyName"); // NOI18N
        //
//        prop = PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
//                CORRELATON_PROPERTY_TYPE, "getType", "setType"); // NOI18N
//        prop.setHidden(true);
        //
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                CORRELATON_PROPERTY_TYPE_NAME, "getType", null); // NOI18N
//                CORRELATON_PROPERTY_TYPE_NAME, "getTypeQName", null); // NOI18N
        return sheet;
    }
    
    public String getPropertyName() {
        CorrelationProperty ref = getReference();
        return ref == null ? null : ref.getName();
    }
    
    public void setPropertyName(String newValue) {
        CorrelationProperty cp = getReference();
        if (cp == null) {
            return;
        }
        
        WSDLModel model = cp.getModel();
        if (model.isIntransaction()) {
            cp.setName(newValue);
        } else {
            model.startTransaction();
            try {
                cp.setName(newValue);
            } finally {
                    model.endTransaction();
            }
        }
    }
    
    public String getType() {
        CorrelationProperty ref = getReference();
        if ( ref == null) {
            return null;
        }
        
        String typeOrElement = "";
        Reference referenceTypeorElement = ref.getType();
        referenceTypeorElement = referenceTypeorElement == null 
                ? ref.getElement() 
                : referenceTypeorElement ;
        
        return referenceTypeorElement == null ? null : referenceTypeorElement.getRefString();
    }
    
    public void setType(GlobalSimpleType newType) {
        CorrelationProperty cp = getReference();
        if (cp == null) {
            return;
        }
        
        NamedComponentReference<GlobalType> typeReference = cp.
                createSchemaReference(newType, GlobalType.class);
        WSDLModel model = cp.getModel();
        if (model.isIntransaction()) {
            cp.setType(typeReference);
        } else {
            model.startTransaction();
            try {
                cp.setType(typeReference);
            } finally {
                    model.endTransaction();
            }
        }
    }
    
    public QName getTypeQName() {
        CorrelationProperty ref = getReference();
        if (ref == null) {
            return null;
        }
        
        NamedComponentReference<GlobalType> typeReference = ref.getType();
        if (typeReference != null && !typeReference.isBroken()) {
            return typeReference.getQName();
        } else {
            return null;
        }
    }
    

    private boolean isBpelExtNode() {
        Node parent = getParentNode();
        while (parent != null) {
            if (parent instanceof ImportWsdlNode) {
                return true;
            }
            parent = parent.getParentNode();
        }
        return false;
    }
    
    protected ActionType[] getActionsArray() {
        ActionType deleteActionType = isBpelExtNode() 
                ? ActionType.DELETE_BPEL_EXT_FROM_WSDL 
                : ActionType.DELETE_PROPERTY_ACTION;
        return new ActionType[] {
            ActionType.SHOW_POPERTY_EDITOR,
            ActionType.SEPARATOR,
            deleteActionType,
            ActionType.SEPARATOR,
            ActionType.PROPERTIES,
        };
    }

    public Component getCustomizer(CustomNodeEditor.EditingMode editingMode) {
        return new SimpleCustomEditor<CorrelationProperty>(
                this, CorrelationPropertyMainPanel.class, editingMode);
    }
}
