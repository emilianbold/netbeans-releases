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

package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.editors.api.utils.Util;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.nodes.VariableNode;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


public class GoToTypeSourceAction extends BpelNodeAction {
    
    private static final long serialVersionUID = 1L;
    
    
    protected String getBundleName() {
        return NbBundle.getMessage(GoToTypeSourceAction.class, 
                "CTL_GoToTypeSourceAction"); // NOI18N
    }
    
    
    public void performAction(Node[] nodes) {
        if (!enable(nodes)) {
            return;
        }
        
        VariableDeclaration variable = ((VariableNode) nodes[0]).getReference();
        
        if (variable == null) {
            return;
        }
        
        Util.goToDocumentComponentSource(getVariableType(variable));
    }
   
   
    private DocumentComponent getVariableType(
            VariableDeclaration variable) 
    {
        SchemaReference<GlobalType> typeReference = variable.getType();
        if (typeReference != null) {
            DocumentComponent result = typeReference.get();
            if (result != null) {
                return result;
            }
        } 
        
        WSDLReference<Message> wsdlReference = variable.getMessageType();
        if (wsdlReference != null) {
            DocumentComponent result = wsdlReference.get();
            if (result != null) {
                return result;
            }
        }
        
        SchemaReference<GlobalElement> elementReference = variable.getElement();
        if (elementReference != null) {
            DocumentComponent result = elementReference.get();
            if (result != null) {
                return result;
            }
        }
        
        return null;    
    }
    
    
    public boolean isChangeAction() {
        return false;
    }
    
    
    public boolean enable(BpelEntity[] entities) {
        if (!super.enable(entities)) return false;
        
        BpelEntity entity = entities[0];
        if (entity instanceof VariableDeclaration) {
            return Util.canGoToDocumentComponentSource(getVariableType(
                    (VariableDeclaration) entity));
        }
        
        return false;
    }

    
    public ActionType getType() {
        return ActionType.GO_TO_TYPE_SOURCE;
    }
    
    
    protected void performAction(BpelEntity[] bpelEntities) {
    }
}

