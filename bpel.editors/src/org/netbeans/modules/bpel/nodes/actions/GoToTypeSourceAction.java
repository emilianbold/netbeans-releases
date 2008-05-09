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

package org.netbeans.modules.bpel.nodes.actions;

import org.netbeans.modules.bpel.nodes.actions.BpelNodeAction;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
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
        
        EditorUtil.goToDocumentComponentSource(getVariableType(variable));
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
            return EditorUtil.canGoToDocumentComponentSource(getVariableType(
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

