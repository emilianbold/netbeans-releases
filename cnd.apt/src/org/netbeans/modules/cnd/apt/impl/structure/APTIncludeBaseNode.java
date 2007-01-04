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
import java.util.logging.Level;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.utils.TokenBasedTokenStream;

/**
 * #include and #include_next base implementation
 * @author Vladimir Voskresensky
 */
public abstract class APTIncludeBaseNode extends APTTokenBasedNode 
                                          implements Serializable {
    private static final long serialVersionUID = -2311241687965334550L;
    // TODO: need to support pp-tokens as include stream 
    // expanded later based on macro map
    private Token includeFileToken = EMPTY_INCLUDE;   

    private int endOffset = 0;
    /** Copy constructor */
    /**package*/ APTIncludeBaseNode(APTIncludeBaseNode orig) {
        super(orig);
        this.includeFileToken = orig.includeFileToken;
    }
    
    /** Constructor for serialization */
    protected APTIncludeBaseNode() {
    }

    /**
     * Creates a new instance of APTIncludeBaseNode
     */
    protected APTIncludeBaseNode(Token token) {
        super(token);
    }    

    public int getEndOffset() {
        return endOffset;
    }
    
    public APT getFirstChild() {
        return null;
    }

    public void setFirstChild(APT child) {
        // do nothing
        assert (false) : "include doesn't support children";        
    }

    public boolean accept(Token token) {
        int ttype = token.getType();
        if (APTUtils.isEndDirectiveToken(ttype)) {
            endOffset = ((APTToken)token).getOffset();
            return false;
        }
        // eat all till END_PREPROC_DIRECTIVE            
        switch (token.getType()) {
            case APTTokenTypes.INCLUDE_STRING:
            case APTTokenTypes.SYS_INCLUDE_STRING:
                assert (includeFileToken == EMPTY_INCLUDE) : "must be init only once";
                this.includeFileToken = token;
                break;  
            case APTTokenTypes.COMMENT:
            case APTTokenTypes.CPP_COMMENT:
                // just skip comments, they are valid
                break;
            default:
                // we don't support macro expanding in #include yet...
                APTUtils.LOG.log(Level.SEVERE, "not supported #include token {0}", new Object[] { token });
                if (includeFileToken == EMPTY_INCLUDE) {
                    includeFileToken = new NotHandledInclude(token);
                }
        }
        return true;
    }

    public String getText() {
        String ret = super.getText();
        if (getInclide() != null) {
            ret += " INCLUDE{" + (isSystem() ? "<S> ":"<U> ") + getInclide()+"}";
        }
        return ret;
    }
    ////////////////////////////////////////////////////////////////////////////
    // impl of interfaces APTInclude and APTIncludeNext
    
    public TokenStream getInclide() {
        return new TokenBasedTokenStream(includeFileToken);
    }

    public String getFileName() {
        assert (includeFileToken != null);
        String file = includeFileToken.getText();
        int len = file.length();
        return len > 1 ? file.substring(1, len - 1) : "";
    }

    public boolean isSystem() {
        assert (includeFileToken != null);
        String file = includeFileToken.getText();
        return file.charAt(0) == '<'; // NOI18N
    }   
    
    private static final NotHandledInclude EMPTY_INCLUDE = new NotHandledInclude(null);
    
    //TODO: what about Serializable
    private static class NotHandledInclude extends APTTokenAbstact {
        private Token origToken;
        
        public NotHandledInclude(Token token) {
            origToken = token;
        }
        
        public String getText() {
            return "{not yet handling such includes - " + origToken + "}";
        }        
    };
}
