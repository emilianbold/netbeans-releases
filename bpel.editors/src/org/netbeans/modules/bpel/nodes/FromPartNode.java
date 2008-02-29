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

import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class FromPartNode extends BpelNode<FromPart>{
    
    public FromPartNode(FromPart reference, Lookup lookup) {
        super(reference, lookup);
    }

    public FromPartNode(FromPart reference, Children children, Lookup lookup) {
        super(reference, lookup);
    }

    public NodeType getNodeType() {
        return NodeType.FROM_PART;
    }
    
    protected String getImplHtmlDisplayName() {
        FromPart fromPart = getReference();
        if (fromPart == null) {
            return EMPTY_STRING;
        }
        BpelReference<VariableDeclaration> toVar = fromPart.getToVariable();
        String resultString = toVar == null ? null 
                : toVar.getRefString(); 

        return resultString == null ? EMPTY_STRING 
                : NbBundle.getMessage(FromPartNode.class,"LBL_FROM_PART_NODE", // NOI18N
                resultString);
    }
    
    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.SEPARATOR,
            ActionType.REMOVE
        };    
    }
}
