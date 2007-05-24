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

package org.netbeans.modules.cnd.apt.support;

import antlr.CommonToken;
import antlr.Token;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.utils.TextCache;

/**
 * token to be used in APT infrastructure
 * @author Vladimir Voskresensky
 */
public class APTBaseToken extends CommonToken implements APTToken {
    private static final long serialVersionUID = 2834353662691067170L;
    
    private int offset;
    /**
     * Creates a new instance of APTBaseToken
     */
    public APTBaseToken() {
    }

    public APTBaseToken(Token token) {
        this(token, token.getType());
    }
    
    public APTBaseToken(Token token, int ttype) {
        this.setColumn(token.getColumn());
        this.setFilename(token.getFilename());
        this.setLine(token.getLine());
        
        // This constructor is used with the existing tokens so do not use setText here, 
        // because we do not need to go through APTStringManager once again
        text = token.getText();
        
        this.setType(ttype);
        if (token instanceof APTToken) {
            APTToken aptToken = (APTToken)token;
            this.setOffset(aptToken.getOffset());
            this.setEndOffset(aptToken.getEndOffset());
            this.setEndColumn(aptToken.getEndColumn());
            this.setEndLine(aptToken.getEndLine());
            this.setTextID(aptToken.getTextID());
        }
    }
    
    public APTBaseToken(String text) {
        this.setText(text);
    }
    
    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int o) {
        this.offset = o;
    }

    public int getEndOffset() {
        return getOffset() + getText().length();
    }

    public void setEndOffset(int end) {
        // do nothing
    }
    
    public int getTextID() {
//        return textID;
        return -1;
    }
    
    public void setTextID(int textID) {
//        this.textID = textID;
    }
  
    public String getText() {
        // TODO: get from shared string map
        String res = super.getText();
        return res;
    }
    
    public void setText(String t) {
        if (APTTraceFlags.APT_SHARE_TEXT) {
            t = TextCache.getString(t);
        }
        super.setText(t);
    }
    
    public String toString() {
        return "[\"" + getText() + "\",<" + getType() + ">,line=" + getLine() + ",col=" + getColumn() + "]" + ",offset="+getOffset()+",file="+getFilename(); // NOI18N
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
        // do nothing
    }
}
