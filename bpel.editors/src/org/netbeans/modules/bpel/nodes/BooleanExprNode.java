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
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 * 
 * This Node to present tBoolean-expr elements
 *
 */
public class BooleanExprNode extends BpelNode<BooleanExpr> {
    
    public BooleanExprNode(BooleanExpr reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public BooleanExprNode(BooleanExpr reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }

    public NodeType getNodeType() {
        return NodeType.BOOLEAN_EXPR;
    }
 
    protected String getNameImpl(){
        BooleanExpr ref = getReference();
        String name = null;
        if (ref != null) {
            name = ref.getContent();
        }
        
        if (name != null && name.length() > MAX_CONTENT_NAME_LENGTH) {
            name = name.substring(0, MAX_CONTENT_NAME_LENGTH)+DOTS_SIGN;
        }
        return (name != null) ? name : ""; // NOI18N
    }

//    protected String getImplShortDescription() {
//        BooleanExpr ref = getReference();
//        String name = null;
//        if (ref != null) {
//            name = ref.getContent();
//        }
//        
//        return (name != null) ? name : ""; // NOI18N
//    }

    
}
