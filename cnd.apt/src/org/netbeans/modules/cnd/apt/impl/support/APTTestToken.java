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

package org.netbeans.modules.cnd.apt.impl.support;

import antlr.Token;
import antlr.TokenImpl;
import org.netbeans.modules.cnd.apt.support.APTToken;

/**
 * lightweigth Token implementation (to reduce memory used by APT)
 * @author Vladimir Voskresensky
 */
public class APTTestToken extends TokenImpl implements APTToken {

    private int offset;
    private int textID;

    public APTTestToken() {

    }

    public APTTestToken(Token token) {
        this(token, token.getType());
    }
    
    public APTTestToken(Token token, int ttype) {
        this.setColumn(token.getColumn());
        this.setFilename(token.getFilename());
        this.setLine(token.getLine());
        this.setText(token.getText());
        this.setType(ttype);
        if (token instanceof APTToken) {
            APTToken aptToken = (APTToken)token;
            this.setOffset(aptToken.getOffset());
            this.setEndOffset(aptToken.getEndOffset());
            this.setTextID(aptToken.getTextID());
        }        
    }
    
    public int getOffset() {
        return offset;
    }
      
    public void setOffset(int o) {
        offset = o;
    }
    
    public int getEndOffset() {
        return getOffset() + getText().length();
    }

    public void setEndOffset(int end) {
        // do nothing
    }
    
    public int getTextID() {
        return textID;
    }
    
    public void setTextID(int textID) {
        this.textID = textID;
    }
  
    public String getText() {
        // TODO: use shared string map
        String res = super.getText();
        return res;
    }
    
    public void setText(String text) {
        // TODO: use shared string map
        super.setText(text);
    }    
     
    public String toString() {
        return "[\"" + getText() + "\",<" + getType() + ">,line=" + getLine() + ",col=" + getColumn() + "]"+",offset="+getOffset();//+",file="+getFilename(); // NOI18N
    }

    public int getEndColumn() {
        return getColumn() + getText().length();
    }

    public void setEndColumn(int c) {
        // do nothing
    }

    public int getEndLine() {
        return getLine();
    }

    public void setEndLine(int l) {
        // do nothin
    }
}
