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

import java.awt.Image;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.xam.Reference;

/**
 *
 * @author Vitaly Bychkov
 */
public class VariableReferenceNode extends BpelNode<BpelReference<VariableDeclaration>> {

    public VariableReferenceNode(BpelReference<VariableDeclaration> varRef, Children children, Lookup lookup) {
        super(varRef, children, lookup);
    }

    private VariableNode.DefaultTypeInfoProvider getTypeInfoProvider () {
        BpelReference<VariableDeclaration> varRef = getReference();
        return new VariableNode.DefaultTypeInfoProvider(varRef == null ? null : varRef.get());
    }

    public VariableReferenceNode(BpelReference<VariableDeclaration> varRef, Lookup lookup) {
        super(varRef, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.VARIABLE_REFERENCE;
    }
    
    public Image getIcon(int type) {
        return getNodeType().getImage(getTypeInfoProvider().getVariableStereotype());
    }
    
    protected String getNameImpl(){
        BpelReference<VariableDeclaration> varRef = getReference();
        String name = null;
        if (varRef != null) {
            name = varRef.getRefString();
        }
        return (name != null) ? name : "";
    }
    
    protected String getImplHtmlDisplayName() {
        String result = null;
        Object varType = getTypeInfoProvider().getVariableType();
        if (!(varType instanceof Reference)) {
            result = getName();
        } else {
            result = SoaUtil.getGrayString(
                    getName(),
                    " " + ((Reference)varType).getRefString()); // NOI18N
        }
        //
        return result == null ? "" : result;
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.SEPARATOR,
            ActionType.REMOVE,
        };
    }
}
