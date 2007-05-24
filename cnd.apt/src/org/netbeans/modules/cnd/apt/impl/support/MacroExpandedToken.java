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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.apt.impl.support;

import antlr.Token;
import java.io.ObjectStreamException;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.support.APTBaseToken;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 * token as wrapper to present macro expansion 
 * on deserialization is substituted by presenter
 * @author Vladimir Voskresensky
 */
public class MacroExpandedToken implements APTToken, Serializable {
    
    private static final long serialVersionUID = -5975409234096997015L;
    transient private final APTToken from;
    transient private final APTToken to;
    transient private final APTToken endOffsetToken;
    
    /** constructor for serialization **/
    protected MacroExpandedToken() {
        from = null;
        to = null;
        endOffsetToken = null;
    }
    
    public MacroExpandedToken(Token from, Token to, Token endOffsetToken) {
        if (!(from instanceof APTToken)) {
            assert (false);
            throw new IllegalStateException("why 'from' is not APTToken?"); // NOI18N
        }        
        this.from = (APTToken)from;
        if (!(to instanceof APTToken)) {
            assert (false);
            throw new IllegalStateException("why 'to' is not APTToken?"); // NOI18N
        }  
        this.to = (APTToken)to;
        if (!(endOffsetToken instanceof APTToken)) {
            assert (false);
            throw new IllegalStateException("why 'endOffsetToken' is not APTToken?"); // NOI18N
        }          
        this.endOffsetToken = (APTToken)endOffsetToken;
    }
    
    ////////////////////////////////////////////////////////
    // delegate to original token (before expansion)
    
    public int getOffset() {
        return from.getOffset();
    }
    
    public void setOffset(int o) {
        throw new UnsupportedOperationException("setOffset must not be used"); // NOI18N
    }
    
    public int getColumn() {
        return from.getColumn();
    }
    
    public void setColumn(int c) {
        throw new UnsupportedOperationException("setColumn must not be used"); // NOI18N
    }
    
    public int getLine() {
        return from.getLine();
    }
    
    public void setLine(int l) {
        throw new UnsupportedOperationException("setLine must not be used"); // NOI18N
    }
    
    public String getFilename() {
        return from.getFilename();
    }
    
    public void setFilename(String name) {
        throw new UnsupportedOperationException("setFilename must not be used"); // NOI18N
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // delegate to expanded result
    
    public String getText() {
        return to.getText();
    }
    
    public void setText(String t) {
        throw new UnsupportedOperationException("setText must not be used"); // NOI18N
    }
    
    public int getTextID() {
        return to.getTextID();
    }
    
    public void setTextID(int id) {
        throw new UnsupportedOperationException("setTextID must not be used"); // NOI18N
    }

    public int getType() {
        return to.getType();
    }
    
    public void setType(int t) {
        throw new UnsupportedOperationException("setType must not be used"); // NOI18N
    }

    public int getEndOffset() {
        return endOffsetToken.getEndOffset();
    }
    
    public void setEndOffset(int o) {
        throw new UnsupportedOperationException("setEndOffset must not be used"); // NOI18N
    } 

    public int getEndColumn() {
        return endOffsetToken.getEndColumn();
    }

    public void setEndColumn(int c) {
        throw new UnsupportedOperationException("setEndColumn must not be used"); // NOI18N
    }

    public int getEndLine() {
        return endOffsetToken.getEndLine();
    }

    public void setEndLine(int l) {
        throw new UnsupportedOperationException("setEndLine must not be used"); // NOI18N
    }

    public String toString() {
        String retValue;
        
        retValue = super.toString();
        retValue += "\n\tEXPANDING OF {" + from + "}\n\tTO {" + to + "}"; // NOI18N
        return retValue;
    }
    
    //////////////////////////////////////////////////////////////////////////////
    // serialization support
        
    protected Object writeReplace() throws ObjectStreamException {
        Object replacement = new SerializedMacroToken(this);
        return replacement;
    }    
    
    // replacement class to prevent serialization of 
    // "from", "to", "endOffset" tokens
    private static final class SerializedMacroToken extends APTBaseToken 
                                                    implements APTToken, Serializable {
        private static final long serialVersionUID = -3616605756675245730L;
        private int endOffset;
        private int endLine;
        private int endColumn;
        
        public SerializedMacroToken(MacroExpandedToken orig) {
            super(orig);
        }

        public void setEndOffset(int end) {
            endOffset = end;
        }

        public int getEndOffset() {
            return endOffset;
        }   

        public void setEndLine(int l) {
            this.endLine = l;
        }

        public void setEndColumn(int c) {
            this.endColumn = c;
        }

        public int getEndLine() {
            return endLine;
        }

        public int getEndColumn() {
            return endColumn;
        }       
    }
}
