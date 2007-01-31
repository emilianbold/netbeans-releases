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

package org.netbeans.modules.cnd.apt.impl.structure;

import antlr.Token;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;

/**
 * #ifndef/#ifdef directives base implementation
 * @author Vladimir Voskresensky
 */
public abstract class APTIfdefConditionBaseNode extends APTTokenAndChildBasedNode 
                                                implements Serializable {
    private static final long serialVersionUID = -5900095440680811076L;
    private Token macroName;
    private int endOffset;
    
    /** Copy constructor */
    /**package*/ APTIfdefConditionBaseNode(APTIfdefConditionBaseNode orig) {
        super(orig);
        this.macroName = orig.macroName;
    }
    
    /** Constructor for serialization */
    protected APTIfdefConditionBaseNode() {
    }
    
    /** Creates a new instance of APTIfdefConditionBaseNode */
    protected APTIfdefConditionBaseNode(Token token) {
        super(token);
    }

    public boolean accept(Token token) {
        /** base implementation of #ifdef/#ifndef */        
        if (APTUtils.isID(token)) {
            assert (macroName == null) : "init macro name only once"; // NOI18N            
            this.macroName = token;
        }
        // eat all till END_PREPROC_DIRECTIVE     
        if (APTUtils.isEndDirectiveToken(token.getType())) {
            endOffset = ((APTToken)token).getOffset();
            return false;
        } else {
            return true;
        }
    }

    public int getEndOffset() {
        return endOffset;
    }
    
    public String getText() {
        assert (getToken() != null) : "must have valid preproc directive"; // NOI18N
        assert (getMacroName() != null) : "must have valid macro"; // NOI18N
        String retValue = super.getText();
        if (getMacroName() != null) {
            retValue += " MACRO{" + getMacroName() + "}"; // NOI18N
        }
        return retValue;
    }

    /** base implementation for #ifdef/#ifndef */
    public Token getMacroName() {
        return macroName;
    }    

}
