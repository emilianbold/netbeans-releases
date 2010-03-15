/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl.parser;

import org.netbeans.modules.cnd.antlr.BaseAST;
import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.support.APTBaseToken;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 *
 * @author Dmitriy Ivanov
 */
public class CsmAST extends BaseAST implements Serializable {

    private static final long serialVersionUID = -1975495157952833337L;
    private static final Token NIL = new APTBaseToken();
    transient protected Token token = NIL;

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

        if (t instanceof CsmAST) {
            token = ((CsmAST)t).token;
        } else {
            assert false;
//            token = new CsmToken();
//            token.setType(t.getType());
//            token.setText(t.getText());
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
            return "<undef_file>"; // NOI18N
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
