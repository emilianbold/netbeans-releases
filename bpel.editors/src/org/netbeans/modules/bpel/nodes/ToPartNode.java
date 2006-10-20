/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.design.nodes.DiagramExtInfo;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.ToPart;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.nodes.actions.ActionType;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class ToPartNode extends DiagramBpelNode<ToPart, DiagramExtInfo>{
    
    public ToPartNode(ToPart reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public ToPartNode(ToPart reference, Children children, Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.TO_PART;
    }

    protected String getImplHtmlDisplayName() {
        ToPart toPart = getReference();
        if (toPart == null) {
            return EMPTY_STRING;
        }
        
        BpelReference<VariableDeclaration> fromVar = toPart.getFromVariable();
        String resultString = fromVar == null ? null 
                : fromVar.getRefString(); 

        return resultString == null ? EMPTY_STRING 
                : NbBundle.getMessage(ToPartNode.class,"LBL_TO_PART_NODE", // NOI18N
                resultString);
    }
    
//    protected String getImplShortDescription() {
//        ToPart toPart = getReference();
//        StringBuffer result = new StringBuffer();
//        result.append(getName());
//        BpelReference<VariableDeclaration> fromVar = toPart == null ? null 
//                : toPart.getFromVariable();
//        result.append(fromVar == null ? EMPTY_STRING 
//                : FROM_VARIABLE_EQ+fromVar.getRefString()); 
//
//        return NbBundle.getMessage(ToPartNode.class,
//            "LBL_TO_PART_NODE_TOOLTIP", // NOI18N
//            result.toString()
//            );
//    }

    protected ActionType[] getActionsArray() {
        return new ActionType[] {
            ActionType.GO_TO_SOURCE,
            ActionType.SEPARATOR,
            ActionType.REMOVE
        };
    }
}
