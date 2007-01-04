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
        constText[EOF]                  ="";
        
        // 1 symbol:
        constText[FUN_LIKE_MACRO_LPAREN]="(";
        constText[ASSIGNEQUAL]          ="=";
        constText[DIVIDE]               ="/";
        constText[STAR]                 ="*";
        constText[MOD]                  ="%";
        constText[NOT]                  ="!";
        constText[AMPERSAND]            ="&";
        constText[BITWISEOR]            ="|";
        constText[BITWISEXOR]           ="^";
        constText[COLON]                =":";
        constText[LESSTHAN]             ="<";
        constText[GREATERTHAN]          =">";
        constText[MINUS]                ="-";
        constText[PLUS]                 ="+";
        constText[SHARP]                ="#";
        constText[SEMICOLON]            =";";
        constText[RPAREN]               =")";
        constText[DOLLAR]               ="$";
        constText[RCURLY]               ="}";
        constText[AT]                   ="@";
        constText[LPAREN]               ="(";
        constText[QUESTIONMARK]         ="?";
        constText[LCURLY]               ="{";
        constText[COMMA]                =",";
        constText[LSQUARE]              ="[";
        constText[RSQUARE]              ="]";
        constText[TILDE]                ="~";
        constText[DOT]                  =".";
        constText[BACK_SLASH]           ="\\";

        // 2 symbol:
        constText[BITWISEANDEQUAL]      ="&=";
        constText[AND]                  ="&&";
        constText[NOTEQUAL]             ="!=";
        constText[MODEQUAL]             ="%=";        
        constText[TIMESEQUAL]           ="*=";
        constText[DIVIDEEQUAL]          ="/=";
        constText[EQUAL]                ="==";
        constText[BITWISEOREQUAL]       ="|=";
        constText[OR]                   ="||";
        constText[BITWISEXOREQUAL]      ="^=";
        constText[SCOPE]                ="::";
        constText[LESSTHANOREQUALTO]    ="<=";
        constText[SHIFTLEFT]            ="<<";
        constText[GREATERTHANOREQUALTO] =">=";
        constText[SHIFTRIGHT]           =">>";
        constText[MINUSEQUAL]           ="-=";
        constText[MINUSMINUS]           ="--";
        constText[POINTERTO]            ="->";
        constText[PLUSEQUAL]            ="+=";
        constText[PLUSPLUS]             ="++";
        constText[DBL_SHARP]            ="##";
        constText[DOTMBR]               =".*";

        // 3 symbol:
        constText[SHIFTLEFTEQUAL]       ="<<=";
        constText[SHIFTRIGHTEQUAL]      =">>=";
        constText[POINTERTOMBR]         ="->*";
        constText[ELLIPSIS]             ="...";
        
        // more
        constText[DEFINED]              ="defined";
    }
    
    public String getText() {
        //assert(constText[getType()] != null) : "Not initialized ConstText for type " + getType();
        return constText[getType()];
    }
    
    public void setText(String t) {
        //assert(true) : "setText should not be called for ConstText token";
        /*String existingText = getText();
        if (existingText != null) {
            /*if (!existingText.equals(t)) {
                System.out.println(getType() + ", Old=" + existingText + ", New=" + t);
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
