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

package org.netbeans.modules.cnd.modelimpl.parser;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.impl.support.APTBaseToken;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 *
 * @author Dmitriy Ivanov
 */
public class CsmAST extends BaseAST implements Serializable {

    private static final long serialVersionUID = -1975495157952833337L;
    
    transient protected Token token = CsmToken.NIL;

    /** Creates a new instance of CsmAST */
    public CsmAST() {
    }

//    public CsmAST(Token tok) {
//        initialize(tok);
//    }


    public void initialize(int t, String txt) {
        token = new APTBaseToken();
        token.setType(t);
        token.setText(txt);
    }

    public void initialize(AST t) {
        if (t instanceof CsmAST)
            token = ((CsmAST)t).token;
        else {
            token = new CsmToken();
            token.setType(t.getType());
            token.setText(t.getText());
        }
    }

    public void initialize(Token tok) {
        token = tok;
    }

    /** Get the token text for this node */
    public String getText() {
        return token.getText();
    }

    /** Get the token type for this node */
    public int getType() {
        return token.getType();
    }

    public int getLine() {
        return token.getLine();
    }
    
    public int getColumn() {
        return token.getColumn();
    }

    public int getOffset() {
        if (token instanceof APTToken) {
            return ((APTToken)token).getOffset();
        } else {
            return 0;
        }
    }

    public int getEndOffset() {
        if (token instanceof APTToken) {
            return ((APTToken)token).getEndOffset();
        } else {
            return 0;
        }        
    }
    
    public int getEndLine() {
        if (token instanceof APTToken) {
            return ((APTToken)token).getEndLine();
        } else {
            return 0;
        }          
    }
    
    public int getEndColumn() {
        if (token instanceof APTToken) {
            return ((APTToken)token).getEndColumn();
        } else {
            return 0;
        }          
    }
    
    public String getFilename() {
        if (token instanceof APTToken) {
            return ((APTToken)token).getFilename();
        } else {
            return "<undef_file>";
        }
    }
    
    public String toString() {
        return token.toString();
    }

    public Token getToken() {
        return token;
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(token);
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        token = (Token) in.readObject();
    }    
}
