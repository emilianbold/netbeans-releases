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
import antlr.TokenStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.ListBasedTokenStream;

/**
 * base class for #if, #elif directives
 * @author Vladimir Voskresensky
 */
public abstract class APTIfConditionBaseNode extends APTTokenAndChildBasedNode
                                            implements Serializable {
    private static final long serialVersionUID = 1068728941146083839L;
    private List condition;
    private int endOffset;
    
    /** Copy constructor */
    /**package*/APTIfConditionBaseNode(APTIfConditionBaseNode orig) {
        super(orig);
        this.condition = orig.condition;
    }
    
    /** Constructor for serialization */
    protected APTIfConditionBaseNode() {
    }
    
    /**
     * Creates a new instance of APTIfConditionBaseNode
     */
    protected APTIfConditionBaseNode(Token token) {
        super(token);
    }
    
    public String getText() {
        String text = super.getText();
        String condStr;
        if (condition != null) {
            condStr = APTUtils.toString(getCondition());
        } else {
            assert(false):"is it ok to have #if/#elif without condition?";
            condStr = "<no condition>";
        }
        return text + " CONDITION{" + condStr + "}";
    }
    
    /** provides APTIf and APTElif interfaces support */
    public TokenStream getCondition() {
        return condition != null ? new ListBasedTokenStream(condition) : APTUtils.EMPTY_STREAM;
    }
    
    public boolean accept(Token token) {
        assert (token != null);
        int ttype = token.getType();
        assert (!APTUtils.isEOF(ttype)) : "EOF must be handled in callers";
        // eat all till END_PREPROC_DIRECTIVE
        if (APTUtils.isEndDirectiveToken(ttype)) {
            endOffset = ((APTToken)token).getOffset();
            return false;
        } else if (!APTUtils.isCommentToken(ttype)) {
            if (condition == null) {
                condition = new ArrayList();
            }
            condition.add(token);
        }
        return true;
    }
    
    public int getEndOffset() {
        return endOffset;
    }
}
