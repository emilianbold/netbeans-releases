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

import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.FaultNameReference;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.openide.util.Lookup;

/**
 *
 * @author nk160297
 */
public class ThrowNode extends BpelNode<Throw> {
    
    public ThrowNode(Throw reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.THROW;
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
        Node.Property property;
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        PropertyUtils propUtil = PropertyUtils.getInstance();
        //
        propUtil.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        property = propUtil.registerAttributeProperty(
                this, mainPropertySet,
                FaultNameReference.FAULT_NAME, FAULT_NAME,
                "getFaultName", "setFaultName", null); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        property = propUtil.registerAttributeProperty(
                new InstanceRef() {
                    public Object getReference() {
                        return ThrowNode.this;
                    }
                    public Object getAlternativeReference() {
                        return null;
                    }
                },
                mainPropertySet,
                Throw.FAULT_VARIABLE, FAULT_VARIABLE,
                "getFaultVariable", "setFaultVariable", null); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        propUtil.registerProperty(this, mainPropertySet,
                DOCUMENTATION, "getDocumentation", "setDocumentation", "removeDocumentation"); // NOI18N
        //
        return sheet;
    }

    // TODO correct according bean spec
    public BpelReference<VariableDeclaration> getFaultVariable() {
        Throw throwObj = getReference();
        return throwObj != null ? throwObj.getFaultVariable() : null;
    }

    public void setFaultVariable(VariableDeclaration newValue) {
        Throw throwObj = getReference();
        if (throwObj != null) {
            BpelReference<VariableDeclaration> varRef =
                    throwObj.createReference(newValue, VariableDeclaration.class);
            if (varRef != null) {
                throwObj.setFaultVariable(varRef);
            }
        }
    }
    
}
