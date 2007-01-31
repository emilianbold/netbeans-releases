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

import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;

/**
 *
 * @author gorrus
 */
public class APTConstTextToken extends APTTokenAbstact implements APTTokenTypes {
    private final static String[] constText = new String[300];
    
    protected int type = INVALID_TYPE;
    protected int offset;
    //protected int endOffset;
    protected int line;
    protected int column;
    /**
     * Creates a new instance of APTConstTextToken
     */
    public APTConstTextToken() {
    }
    
    static {
        //setup const text values
        constText[EOF]                  =""; // NOI18N
        
        // 1 symbol:
        constText[FUN_LIKE_MACRO_LPAREN]="("; // NOI18N
        constText[ASSIGNEQUAL]          ="="; // NOI18N
        constText[DIVIDE]               ="/"; // NOI18N
        constText[STAR]                 ="*"; // NOI18N
        constText[MOD]                  ="%"; // NOI18N
        constText[NOT]                  ="!"; // NOI18N
        constText[AMPERSAND]            ="&"; // NOI18N
        constText[BITWISEOR]            ="|"; // NOI18N
        constText[BITWISEXOR]           ="^"; // NOI18N
        constText[COLON]                =":"; // NOI18N
        constText[LESSTHAN]             ="<"; // NOI18N
        constText[GREATERTHAN]          =">"; // NOI18N
        constText[MINUS]                ="-"; // NOI18N
        constText[PLUS]                 ="+"; // NOI18N
        constText[SHARP]                ="#"; // NOI18N
        constText[SEMICOLON]            =";"; // NOI18N
        constText[RPAREN]               =")"; // NOI18N
        constText[DOLLAR]               ="$"; // NOI18N
        constText[RCURLY]               ="}"; // NOI18N
        constText[AT]                   ="@"; // NOI18N
        constText[LPAREN]               ="("; // NOI18N
        constText[QUESTIONMARK]         ="?"; // NOI18N
        constText[LCURLY]               ="{"; // NOI18N
        constText[COMMA]                =","; // NOI18N
        constText[LSQUARE]              ="["; // NOI18N
        constText[RSQUARE]              ="]"; // NOI18N
        constText[TILDE]                ="~"; // NOI18N
        constText[DOT]                  ="."; // NOI18N
        constText[BACK_SLASH]           ="\\"; // NOI18N

        // 2 symbol:
        constText[BITWISEANDEQUAL]      ="&="; // NOI18N
        constText[AND]                  ="&&"; // NOI18N
        constText[NOTEQUAL]             ="!="; // NOI18N
        constText[MODEQUAL]             ="%="; // NOI18N        
        constText[TIMESEQUAL]           ="*="; // NOI18N
        constText[DIVIDEEQUAL]          ="/="; // NOI18N
        constText[EQUAL]                ="=="; // NOI18N
        constText[BITWISEOREQUAL]       ="|="; // NOI18N
        constText[OR]                   ="||"; // NOI18N
        constText[BITWISEXOREQUAL]      ="^="; // NOI18N
        constText[SCOPE]                ="::"; // NOI18N
        constText[LESSTHANOREQUALTO]    ="<="; // NOI18N
        constText[SHIFTLEFT]            ="<<"; // NOI18N
        constText[GREATERTHANOREQUALTO] =">="; // NOI18N
        constText[SHIFTRIGHT]           =">>"; // NOI18N
        constText[MINUSEQUAL]           ="-="; // NOI18N
        constText[MINUSMINUS]           ="--"; // NOI18N
        constText[POINTERTO]            ="->"; // NOI18N
        constText[PLUSEQUAL]            ="+="; // NOI18N
        constText[PLUSPLUS]             ="++"; // NOI18N
        constText[DBL_SHARP]            ="##"; // NOI18N
        constText[DOTMBR]               =".*"; // NOI18N

        // 3 symbol:
        constText[SHIFTLEFTEQUAL]       ="<<="; // NOI18N
        constText[SHIFTRIGHTEQUAL]      =">>="; // NOI18N
        constText[POINTERTOMBR]         ="->*"; // NOI18N
        constText[ELLIPSIS]             ="..."; // NOI18N
        
        // more
        constText[DEFINED]              ="defined"; // NOI18N
    }
    
    public String getText() {
        //assert(constText[getType()] != null) : "Not initialized ConstText for type " + getType(); // NOI18N
        return constText[getType()];
    }
    
    public void setText(String t) {
        //assert(true) : "setText should not be called for ConstText token"; // NOI18N
        /*String existingText = getText();
        if (existingText != null) {
            /*if (!existingText.equals(t)) {
                System.out.println(getType() + ", Old=" + existingText + ", New=" + t); // NOI18N
            }*/
            //assert(existingText.equals(t));
        /*} else {
            constText[getType()] = t;
        }*/
    }

    public int getEndOffset() {
        return getOffset() + getText().length();
        //return endOffset;
    }

    public int getEndLine() {
        return getLine();
    }

    public int getEndColumn() {
        return getColumn() + getText().length();
    }

    public int getColumn() {
        return column;
    }

    public void setLine(int l) {
        line = l;
    }

    public void setOffset(int o) {
        offset = o;
    }

    public int getOffset() {
        return offset;
    }

    public void setType(int t) {
        type = t;
    }

    public int getType() {
        return type;
    }

    public int getLine() {
        return line;
    }

    public void setColumn(int c) {
        column = c;
    }
}
