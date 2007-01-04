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

package org.netbeans.modules.cnd.classview.model;

import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.nodes.*;

import  org.netbeans.modules.cnd.api.model.*;


/**
 * @author Vladimir Kvasihn
 */
public class MemberNode extends ObjectNode {
    private int weight;

    public MemberNode(CsmMember mem) {
        super(mem, Children.LEAF);
        boolean isTemplate = false;
        String text = mem.getName();
        if( mem.getKind() == CsmDeclaration.Kind.CLASS ) {
            isTemplate = ((CsmClass) mem) .isTemplate();
        } else if( CsmKindUtilities.isFunction(mem) ) {
            CsmFunction fun = (CsmFunction) mem;
            isTemplate = fun.isTemplate();
            text = CVUtil.getSignature(fun);
            if( ((CsmMethod) mem).isConst() ) {
                text += " const";
            }
        } 
        if( CsmKindUtilities.isClassifier(mem) || CsmKindUtilities.isEnum(mem) || CsmKindUtilities.isTypedef(mem)) {
            weight = CLASSIFIER_WEIGHT;
        } else if( CsmKindUtilities.isFunction(mem) ) {
            weight = FUNCTION_WEIGHT;
        } else if( CsmKindUtilities.isVariable(mem) ) {
            weight = VARIABLE_WEIGHT;
        } else {
            weight = OTHER_WEIGHT;
        }
        
        String name = text + (isTemplate ? "<>" : "");
        setName(name);
        setDisplayName(name);
        setShortDescription(name);
    }
    
    public CsmMember getMember() {
        return (CsmMember) getObject();
    }

    protected int getWeight() {
        return weight;
    }
    
}
