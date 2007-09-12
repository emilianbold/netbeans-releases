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
            // The related object has been removed!
            return sheet;
        }
        //
        Node.Property property;
        //
        Sheet.Set mainPropertySet =
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        PropertyUtils.registerAttributeProperty(this, mainPropertySet,
                NamedElement.NAME, NAME, "getName", "setName", null); // NOI18N
        //
        property = PropertyUtils.registerAttributeProperty(
                this, mainPropertySet,
                FaultNameReference.FAULT_NAME, FAULT_NAME,
                "getFaultName", "setFaultName", null); // NOI18N
        property.setValue("canEditAsText", Boolean.FALSE); // NOI18N
        //
        property = PropertyUtils.registerAttributeProperty(
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
        return sheet;
    }
    
    public VariableDeclaration getFaultVariable() {
        Throw throwObj = getReference();
        if (throwObj != null) {
            BpelReference<VariableDeclaration> varRef = throwObj.getFaultVariable();
            if (varRef != null) {
                VariableDeclaration varDecl = varRef.get();
                if (varDecl != null && varDecl instanceof VariableDeclaration) {
                    return (VariableDeclaration)varDecl;
                }
            }
        }
        //
        return null;
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
