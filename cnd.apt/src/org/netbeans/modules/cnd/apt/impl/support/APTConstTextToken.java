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

package org.netbeans.modules.cnd.apt.impl.support;

import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;

/**
 *
 * @author gorrus
 */
public final class APTConstTextToken extends APTTokenAbstact implements APTTokenTypes {
    private final static String[] constText = new String[APTTokenTypes.LAST_CONST_TEXT_TOKEN];
    private final static CharSequence[] constTextID = new CharSequence[APTTokenTypes.LAST_CONST_TEXT_TOKEN];
    private static final int SHIFT = 8;
    private static final int TYPE_MASK = ~(1<<SHIFT);
    protected short type = INVALID_TYPE;
    protected short column;
    protected int offset;
    //protected int endOffset;
    protected int line;
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

        for (int i = 0; i < constText.length; i++) {
            String str = constText[i];
            constTextID[i] = CharSequenceKey.create(str);
            if (str != null) {
                if (i > LAST_CONST_TEXT_TOKEN) {
                    System.err.printf("APTConstTextToken: token %s [%d] is higher than LAST_CONST_TEXT_TOKEN [%d]\n", str, i, LAST_CONST_TEXT_TOKEN);
                }
            } else {
               // System.err.printf("APTConstTextToken: index [%d] does not have text \n", i);
            }
        }
        assert TYPE_MASK >= LAST_CONST_TEXT_TOKEN;
//        System.err.printf("APTConstTextToken: %d\n", LAST_CONST_TEXT_TOKEN);
    }
    
    @Override
    public String getText() {
        //assert(constText[getType()] != null) : "Not initialized ConstText for type " + getType(); // NOI18N
        return constText[getType()];
    }
    
    @Override
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

    @Override
    public CharSequence getTextID() {
        return constTextID[getType()];
    }

    @Override
    public int getEndOffset() {
        return getOffset() + getTextID().length();
        //return endOffset;
    }

    @Override
    public int getEndLine() {
        return getLine();
    }

    @Override
    public int getEndColumn() {
        return getColumn() + getTextID().length();
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public void setLine(int l) {
        line = l;
    }

    @Override
    public void setOffset(int o) {
        offset = o;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public void setType(int t) {
        assert t < LAST_CONST_TEXT_TOKEN;
        type = (short) t;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public void setColumn(int c) {
        assert c < Short.MAX_VALUE;
        column = (short) c;
    }
}
