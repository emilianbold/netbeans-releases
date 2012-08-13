/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;
import org.openide.util.CharSequences;

/**
 *
 * @author gorrus
 */
public final class APTConstTextToken extends APTTokenAbstact implements APTTokenTypes {
    private static final int MAX_TEXT_ID = APTTokenTypes.LAST_LEXER_FAKE_RULE;
    final static String[] constText = new String[MAX_TEXT_ID];
    final static CharSequence[] constTextID = new CharSequence[MAX_TEXT_ID];
    final static Map<String, Integer> text2id = new HashMap<String, Integer>(MAX_TEXT_ID);
    
    private int type = INVALID_TYPE;
    private int column;
    private int offset;
    private int line;
    /**
     * Creates a new instance of APTConstTextToken
     */
    public APTConstTextToken() {
    }
    
    static {
        //setup const text values
        constText[EOF]                  =""; // NOI18N
        constText[END_PREPROC_DIRECTIVE]=""; // NOI18N
        
        // 1 symbol:
        constText[GRAVE_ACCENT]          ="`"; // NOI18N
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
        
        // literals from language filters
        
        // APTStdCFilter
        addConstText("alignof", APTTokenTypes.LITERAL_alignof); // NOI18N
        addConstText("typeof", APTTokenTypes.LITERAL_typeof); // NOI18N      
        addConstText("typedef", APTTokenTypes.LITERAL_typedef); // NOI18N
        addConstText("enum", APTTokenTypes.LITERAL_enum); // NOI18N       
        addConstText("extern", APTTokenTypes.LITERAL_extern); // NOI18N
        addConstText("inline", APTTokenTypes.LITERAL_inline); // NOI18N
        addConstText("_inline", APTTokenTypes.LITERAL__inline); // NOI18N        
        addConstText("_stdcall", APTTokenTypes.LITERAL__stdcall); // NOI18N
        addConstText("__stdcall", APTTokenTypes.LITERAL___stdcall); // NOI18N        
        addConstText("auto", APTTokenTypes.LITERAL_auto); // NOI18N
        addConstText("register", APTTokenTypes.LITERAL_register); // NOI18N
        addConstText("static", APTTokenTypes.LITERAL_static); // NOI18N        
        addConstText("const", APTTokenTypes.LITERAL_const); // NOI18N
        addConstText("__const", APTTokenTypes.LITERAL___const); // NOI18N
        addConstText("const_cast", APTTokenTypes.LITERAL_const_cast); // NOI18N
        addConstText("volatile", APTTokenTypes.LITERAL_volatile); // NOI18N
        addConstText("char", APTTokenTypes.LITERAL_char); // NOI18N
        addConstText("bool", APTTokenTypes.LITERAL_bool); // NOI18N
        addConstText("short", APTTokenTypes.LITERAL_short); // NOI18N
        addConstText("int", APTTokenTypes.LITERAL_int); // NOI18N
        addConstText("long", APTTokenTypes.LITERAL_long); // NOI18N
        addConstText("signed", APTTokenTypes.LITERAL_signed); // NOI18N
        addConstText("__signed__", APTTokenTypes.LITERAL___signed__); // NOI18N
        addConstText("unsigned", APTTokenTypes.LITERAL_unsigned); // NOI18N
        addConstText("__unsigned__", APTTokenTypes.LITERAL___unsigned__); // NOI18N
        addConstText("float", APTTokenTypes.LITERAL_float); // NOI18N
        addConstText("double", APTTokenTypes.LITERAL_double); // NOI18N
        addConstText("void", APTTokenTypes.LITERAL_void); // NOI18N
        addConstText("_declspec", APTTokenTypes.LITERAL__declspec); // NOI18N
        addConstText("__declspec", APTTokenTypes.LITERAL___declspec); // NOI18N   
        addConstText("struct", APTTokenTypes.LITERAL_struct); // NOI18N
        addConstText("union", APTTokenTypes.LITERAL_union); // NOI18N   
        addConstText("case", APTTokenTypes.LITERAL_case); // NOI18N
        addConstText("default", APTTokenTypes.LITERAL_default); // NOI18N
        addConstText("if", APTTokenTypes.LITERAL_if); // NOI18N
        addConstText("else", APTTokenTypes.LITERAL_else); // NOI18N
        addConstText("switch", APTTokenTypes.LITERAL_switch); // NOI18N
        addConstText("while", APTTokenTypes.LITERAL_while); // NOI18N
        addConstText("do", APTTokenTypes.LITERAL_do); // NOI18N
        addConstText("for", APTTokenTypes.LITERAL_for); // NOI18N
        addConstText("goto", APTTokenTypes.LITERAL_goto); // NOI18N
        addConstText("continue", APTTokenTypes.LITERAL_continue); // NOI18N
        addConstText("break", APTTokenTypes.LITERAL_break); // NOI18N
        addConstText("return", APTTokenTypes.LITERAL_return); // NOI18N        
        addConstText("asm", APTTokenTypes.LITERAL_asm); // NOI18N
        addConstText("_asm", APTTokenTypes.LITERAL__asm); // NOI18N
        addConstText("sizeof", APTTokenTypes.LITERAL_sizeof); // NOI18N
        addConstText("_cdecl", APTTokenTypes.LITERAL__cdecl); // NOI18N
        addConstText("__cdecl", APTTokenTypes.LITERAL___cdecl); // NOI18N
        addConstText("_near", APTTokenTypes.LITERAL__near); // NOI18N
        addConstText("__near", APTTokenTypes.LITERAL___near); // NOI18N
        addConstText("_far", APTTokenTypes.LITERAL__far); // NOI18N
        addConstText("__far", APTTokenTypes.LITERAL___far); // NOI18N
        addConstText("__interrupt", APTTokenTypes.LITERAL___interrupt); // NOI18N
        addConstText("pascal", APTTokenTypes.LITERAL_pascal); // NOI18N
        addConstText("_pascal", APTTokenTypes.LITERAL__pascal); // NOI18N
        addConstText("__pascal", APTTokenTypes.LITERAL___pascal); // NOI18N        
        addConstText("_int64", APTTokenTypes.LITERAL__int64); // NOI18N
        addConstText("__int64", APTTokenTypes.LITERAL___int64); // NOI18N
        addConstText("__w64", APTTokenTypes.LITERAL___w64); // NOI18N
        addConstText("__extension__", APTTokenTypes.LITERAL___extension__); // NOI18N
        addConstText("__restrict", APTTokenTypes.LITERAL___restrict); // NOI18N    
        addConstText("_Complex", APTTokenTypes.LITERAL__Complex); // NOI18N
        addConstText("_Imaginary", APTTokenTypes.LITERAL__Imaginary); // NOI18N
        
        // APTStdCppFilter
        addConstText("operator", APTTokenTypes.LITERAL_OPERATOR); // NOI18N
        addConstText("alignof", APTTokenTypes.LITERAL_alignof); // NOI18N
        addConstText("__alignof__", APTTokenTypes.LITERAL___alignof__); // NOI18N
        addConstText("typeof", APTTokenTypes.LITERAL_typeof); // NOI18N
        addConstText("__typeof", APTTokenTypes.LITERAL___typeof); // NOI18N
        addConstText("__typeof__", APTTokenTypes.LITERAL___typeof__); // NOI18N
        addConstText("template", APTTokenTypes.LITERAL_template); // NOI18N
        addConstText("typedef", APTTokenTypes.LITERAL_typedef); // NOI18N
        addConstText("enum", APTTokenTypes.LITERAL_enum); // NOI18N
        addConstText("namespace", APTTokenTypes.LITERAL_namespace); // NOI18N
        addConstText("extern", APTTokenTypes.LITERAL_extern); // NOI18N
        addConstText("inline", APTTokenTypes.LITERAL_inline); // NOI18N
        addConstText("_inline", APTTokenTypes.LITERAL__inline); // NOI18N
        addConstText("__inline", APTTokenTypes.LITERAL___inline); // NOI18N
        addConstText("__inline__", APTTokenTypes.LITERAL___inline__); // NOI18N
        addConstText("virtual", APTTokenTypes.LITERAL_virtual); // NOI18N
        addConstText("explicit", APTTokenTypes.LITERAL_explicit); // NOI18N
        addConstText("friend", APTTokenTypes.LITERAL_friend); // NOI18N
        addConstText("_stdcall", APTTokenTypes.LITERAL__stdcall); // NOI18N
        addConstText("__stdcall", APTTokenTypes.LITERAL___stdcall); // NOI18N
        addConstText("typename", APTTokenTypes.LITERAL_typename); // NOI18N
        addConstText("auto", APTTokenTypes.LITERAL_auto); // NOI18N
        addConstText("register", APTTokenTypes.LITERAL_register); // NOI18N
        addConstText("static", APTTokenTypes.LITERAL_static); // NOI18N
        addConstText("mutable", APTTokenTypes.LITERAL_mutable); // NOI18N
        addConstText("const", APTTokenTypes.LITERAL_const); // NOI18N
        addConstText("__const", APTTokenTypes.LITERAL___const); // NOI18N
        addConstText("const_cast", APTTokenTypes.LITERAL_const_cast); // NOI18N
        addConstText("volatile", APTTokenTypes.LITERAL_volatile); // NOI18N
        addConstText("__volatile__", APTTokenTypes.LITERAL___volatile__); // NOI18N
        addConstText("char", APTTokenTypes.LITERAL_char); // NOI18N
        addConstText("wchar_t", APTTokenTypes.LITERAL_wchar_t); // NOI18N
        addConstText("bool", APTTokenTypes.LITERAL_bool); // NOI18N
        addConstText("short", APTTokenTypes.LITERAL_short); // NOI18N
        addConstText("int", APTTokenTypes.LITERAL_int); // NOI18N
        addConstText("long", APTTokenTypes.LITERAL_long); // NOI18N
        addConstText("signed", APTTokenTypes.LITERAL_signed); // NOI18N
        addConstText("__signed__", APTTokenTypes.LITERAL___signed__); // NOI18N
        addConstText("unsigned", APTTokenTypes.LITERAL_unsigned); // NOI18N
        addConstText("__unsigned__", APTTokenTypes.LITERAL___unsigned__); // NOI18N
        addConstText("float", APTTokenTypes.LITERAL_float); // NOI18N
        addConstText("double", APTTokenTypes.LITERAL_double); // NOI18N
        addConstText("void", APTTokenTypes.LITERAL_void); // NOI18N
        addConstText("_declspec", APTTokenTypes.LITERAL__declspec); // NOI18N
        addConstText("__declspec", APTTokenTypes.LITERAL___declspec); // NOI18N
        addConstText("class", APTTokenTypes.LITERAL_class); // NOI18N
        addConstText("struct", APTTokenTypes.LITERAL_struct); // NOI18N
        addConstText("union", APTTokenTypes.LITERAL_union); // NOI18N        
        addConstText("this", APTTokenTypes.LITERAL_this); // NOI18N
        addConstText("true", APTTokenTypes.LITERAL_true); // NOI18N
        addConstText("false", APTTokenTypes.LITERAL_false); // NOI18N
        addConstText("public", APTTokenTypes.LITERAL_public); // NOI18N
        addConstText("protected", APTTokenTypes.LITERAL_protected); // NOI18N
        addConstText("private", APTTokenTypes.LITERAL_private); // NOI18N
        addConstText("throw", APTTokenTypes.LITERAL_throw); // NOI18N
        addConstText("case", APTTokenTypes.LITERAL_case); // NOI18N
        addConstText("default", APTTokenTypes.LITERAL_default); // NOI18N
        addConstText("if", APTTokenTypes.LITERAL_if); // NOI18N
        addConstText("else", APTTokenTypes.LITERAL_else); // NOI18N
        addConstText("switch", APTTokenTypes.LITERAL_switch); // NOI18N
        addConstText("while", APTTokenTypes.LITERAL_while); // NOI18N
        addConstText("do", APTTokenTypes.LITERAL_do); // NOI18N
        addConstText("for", APTTokenTypes.LITERAL_for); // NOI18N
        addConstText("goto", APTTokenTypes.LITERAL_goto); // NOI18N
        addConstText("continue", APTTokenTypes.LITERAL_continue); // NOI18N
        addConstText("break", APTTokenTypes.LITERAL_break); // NOI18N
        addConstText("return", APTTokenTypes.LITERAL_return); // NOI18N
        addConstText("try", APTTokenTypes.LITERAL_try); // NOI18N
        addConstText("catch", APTTokenTypes.LITERAL_catch); // NOI18N
        addConstText("using", APTTokenTypes.LITERAL_using); // NOI18N
        addConstText("asm", APTTokenTypes.LITERAL_asm); // NOI18N
        addConstText("_asm", APTTokenTypes.LITERAL__asm); // NOI18N
        addConstText("__asm", APTTokenTypes.LITERAL___asm); // NOI18N
        addConstText("__asm__", APTTokenTypes.LITERAL___asm__); // NOI18N
        addConstText("sizeof", APTTokenTypes.LITERAL_sizeof); // NOI18N
        addConstText("dynamic_cast", APTTokenTypes.LITERAL_dynamic_cast); // NOI18N
        addConstText("static_cast", APTTokenTypes.LITERAL_static_cast); // NOI18N
        addConstText("reinterpret_cast", APTTokenTypes.LITERAL_reinterpret_cast); // NOI18N
        addConstText("new", APTTokenTypes.LITERAL_new); // NOI18N
        addConstText("_cdecl", APTTokenTypes.LITERAL__cdecl); // NOI18N
        addConstText("__cdecl", APTTokenTypes.LITERAL___cdecl); // NOI18N
        addConstText("_near", APTTokenTypes.LITERAL__near); // NOI18N
        addConstText("__near", APTTokenTypes.LITERAL___near); // NOI18N
        addConstText("_far", APTTokenTypes.LITERAL__far); // NOI18N
        addConstText("__far", APTTokenTypes.LITERAL___far); // NOI18N
        addConstText("__interrupt", APTTokenTypes.LITERAL___interrupt); // NOI18N
        addConstText("pascal", APTTokenTypes.LITERAL_pascal); // NOI18N
        addConstText("_pascal", APTTokenTypes.LITERAL__pascal); // NOI18N
        addConstText("__pascal", APTTokenTypes.LITERAL___pascal); // NOI18N
        addConstText("delete", APTTokenTypes.LITERAL_delete); // NOI18N
        addConstText("_int64", APTTokenTypes.LITERAL__int64); // NOI18N
        addConstText("__int64", APTTokenTypes.LITERAL___int64); // NOI18N
        addConstText("__w64", APTTokenTypes.LITERAL___w64); // NOI18N
        addConstText("__extension__", APTTokenTypes.LITERAL___extension__); // NOI18N
        addConstText("__attribute__", APTTokenTypes.LITERAL___attribute__); // NOI18N
        addConstText("__restrict", APTTokenTypes.LITERAL___restrict); // NOI18N
        addConstText("__complex__", APTTokenTypes.LITERAL___complex__); // NOI18N
        addConstText("__imag__", APTTokenTypes.LITERAL___imag); // NOI18N
        addConstText("__real__", APTTokenTypes.LITERAL___real); // NOI18N      
        addConstText("export", APTTokenTypes.LITERAL_export); // NOI18N
        
        // APTGnuCppFilter
        addConstText("operator", APTTokenTypes.LITERAL_OPERATOR); // NOI18N
        addConstText("alignof", APTTokenTypes.LITERAL_alignof); // NOI18N
        addConstText("__alignof__", APTTokenTypes.LITERAL___alignof__); // NOI18N
        addConstText("typeof", APTTokenTypes.LITERAL_typeof); // NOI18N
        addConstText("__typeof", APTTokenTypes.LITERAL___typeof); // NOI18N
        addConstText("__typeof__", APTTokenTypes.LITERAL___typeof__); // NOI18N
        addConstText("template", APTTokenTypes.LITERAL_template); // NOI18N
        addConstText("typedef", APTTokenTypes.LITERAL_typedef); // NOI18N
        addConstText("enum", APTTokenTypes.LITERAL_enum); // NOI18N
        addConstText("namespace", APTTokenTypes.LITERAL_namespace); // NOI18N
        addConstText("extern", APTTokenTypes.LITERAL_extern); // NOI18N
        addConstText("inline", APTTokenTypes.LITERAL_inline); // NOI18N
        addConstText("_inline", APTTokenTypes.LITERAL__inline); // NOI18N
        addConstText("__inline", APTTokenTypes.LITERAL___inline); // NOI18N
        addConstText("__inline__", APTTokenTypes.LITERAL___inline__); // NOI18N
        addConstText("virtual", APTTokenTypes.LITERAL_virtual); // NOI18N
        addConstText("explicit", APTTokenTypes.LITERAL_explicit); // NOI18N
        addConstText("friend", APTTokenTypes.LITERAL_friend); // NOI18N
        addConstText("_stdcall", APTTokenTypes.LITERAL__stdcall); // NOI18N
        addConstText("__stdcall", APTTokenTypes.LITERAL___stdcall); // NOI18N
        addConstText("typename", APTTokenTypes.LITERAL_typename); // NOI18N
        addConstText("auto", APTTokenTypes.LITERAL_auto); // NOI18N
        addConstText("register", APTTokenTypes.LITERAL_register); // NOI18N
        addConstText("static", APTTokenTypes.LITERAL_static); // NOI18N
        addConstText("mutable", APTTokenTypes.LITERAL_mutable); // NOI18N
        addConstText("const", APTTokenTypes.LITERAL_const); // NOI18N
        addConstText("__const", APTTokenTypes.LITERAL___const); // NOI18N
        addConstText("__const__", APTTokenTypes.LITERAL___const__); // NOI18N
        addConstText("const_cast", APTTokenTypes.LITERAL_const_cast); // NOI18N
        addConstText("volatile", APTTokenTypes.LITERAL_volatile); // NOI18N
        addConstText("__volatile", APTTokenTypes.LITERAL___volatile); // NOI18N
        addConstText("__volatile__", APTTokenTypes.LITERAL___volatile__); // NOI18N
        addConstText("char", APTTokenTypes.LITERAL_char); // NOI18N
        addConstText("wchar_t", APTTokenTypes.LITERAL_wchar_t); // NOI18N
        addConstText("bool", APTTokenTypes.LITERAL_bool); // NOI18N
        addConstText("short", APTTokenTypes.LITERAL_short); // NOI18N
        addConstText("int", APTTokenTypes.LITERAL_int); // NOI18N
        addConstText("long", APTTokenTypes.LITERAL_long); // NOI18N
        addConstText("signed", APTTokenTypes.LITERAL_signed); // NOI18N
        addConstText("__signed", APTTokenTypes.LITERAL___signed); // NOI18N
        addConstText("__signed__", APTTokenTypes.LITERAL___signed__); // NOI18N
        addConstText("unsigned", APTTokenTypes.LITERAL_unsigned); // NOI18N
        addConstText("__unsigned__", APTTokenTypes.LITERAL___unsigned__); // NOI18N
        addConstText("float", APTTokenTypes.LITERAL_float); // NOI18N
        addConstText("double", APTTokenTypes.LITERAL_double); // NOI18N
        addConstText("void", APTTokenTypes.LITERAL_void); // NOI18N
        addConstText("_declspec", APTTokenTypes.LITERAL__declspec); // NOI18N
        addConstText("__declspec", APTTokenTypes.LITERAL___declspec); // NOI18N
        addConstText("class", APTTokenTypes.LITERAL_class); // NOI18N
        addConstText("struct", APTTokenTypes.LITERAL_struct); // NOI18N
        addConstText("union", APTTokenTypes.LITERAL_union); // NOI18N        
        addConstText("this", APTTokenTypes.LITERAL_this); // NOI18N
        addConstText("true", APTTokenTypes.LITERAL_true); // NOI18N
        addConstText("false", APTTokenTypes.LITERAL_false); // NOI18N
        addConstText("public", APTTokenTypes.LITERAL_public); // NOI18N
        addConstText("protected", APTTokenTypes.LITERAL_protected); // NOI18N
        addConstText("private", APTTokenTypes.LITERAL_private); // NOI18N
        addConstText("throw", APTTokenTypes.LITERAL_throw); // NOI18N
        addConstText("case", APTTokenTypes.LITERAL_case); // NOI18N
        addConstText("default", APTTokenTypes.LITERAL_default); // NOI18N
        addConstText("if", APTTokenTypes.LITERAL_if); // NOI18N
        addConstText("else", APTTokenTypes.LITERAL_else); // NOI18N
        addConstText("switch", APTTokenTypes.LITERAL_switch); // NOI18N
        addConstText("while", APTTokenTypes.LITERAL_while); // NOI18N
        addConstText("do", APTTokenTypes.LITERAL_do); // NOI18N
        addConstText("for", APTTokenTypes.LITERAL_for); // NOI18N
        addConstText("goto", APTTokenTypes.LITERAL_goto); // NOI18N
        addConstText("continue", APTTokenTypes.LITERAL_continue); // NOI18N
        addConstText("break", APTTokenTypes.LITERAL_break); // NOI18N
        addConstText("return", APTTokenTypes.LITERAL_return); // NOI18N
        addConstText("try", APTTokenTypes.LITERAL_try); // NOI18N
        addConstText("catch", APTTokenTypes.LITERAL_catch); // NOI18N
        addConstText("using", APTTokenTypes.LITERAL_using); // NOI18N
        addConstText("asm", APTTokenTypes.LITERAL_asm); // NOI18N
        addConstText("_asm", APTTokenTypes.LITERAL__asm); // NOI18N
        addConstText("__asm", APTTokenTypes.LITERAL___asm); // NOI18N
        addConstText("__asm__", APTTokenTypes.LITERAL___asm__); // NOI18N
        addConstText("sizeof", APTTokenTypes.LITERAL_sizeof); // NOI18N
        addConstText("dynamic_cast", APTTokenTypes.LITERAL_dynamic_cast); // NOI18N
        addConstText("static_cast", APTTokenTypes.LITERAL_static_cast); // NOI18N
        addConstText("reinterpret_cast", APTTokenTypes.LITERAL_reinterpret_cast); // NOI18N
        addConstText("new", APTTokenTypes.LITERAL_new); // NOI18N
        addConstText("_cdecl", APTTokenTypes.LITERAL__cdecl); // NOI18N
        addConstText("__cdecl", APTTokenTypes.LITERAL___cdecl); // NOI18N
        addConstText("_near", APTTokenTypes.LITERAL__near); // NOI18N
        addConstText("__near", APTTokenTypes.LITERAL___near); // NOI18N
        addConstText("_far", APTTokenTypes.LITERAL__far); // NOI18N
        addConstText("__far", APTTokenTypes.LITERAL___far); // NOI18N
        addConstText("__interrupt", APTTokenTypes.LITERAL___interrupt); // NOI18N
        addConstText("pascal", APTTokenTypes.LITERAL_pascal); // NOI18N
        addConstText("_pascal", APTTokenTypes.LITERAL__pascal); // NOI18N
        addConstText("__pascal", APTTokenTypes.LITERAL___pascal); // NOI18N
        addConstText("delete", APTTokenTypes.LITERAL_delete); // NOI18N
        addConstText("_int64", APTTokenTypes.LITERAL__int64); // NOI18N
        addConstText("__int64", APTTokenTypes.LITERAL___int64); // NOI18N
        addConstText("__w64", APTTokenTypes.LITERAL___w64); // NOI18N
        addConstText("__extension__", APTTokenTypes.LITERAL___extension__); // NOI18N
        addConstText("__attribute__", APTTokenTypes.LITERAL___attribute__); // NOI18N
        addConstText("__attribute", APTTokenTypes.LITERAL___attribute); // NOI18N
        addConstText("__restrict", APTTokenTypes.LITERAL___restrict); // NOI18N
        addConstText("__restrict__", APTTokenTypes.LITERAL___restrict__); // NOI18N
        addConstText("__complex__", APTTokenTypes.LITERAL___complex__); // NOI18N
        addConstText("__imag__", APTTokenTypes.LITERAL___imag); // NOI18N
        addConstText("__real__", APTTokenTypes.LITERAL___real); // NOI18N 
        addConstText("export", APTTokenTypes.LITERAL_export); // NOI18N
        addConstText("__thread", APTTokenTypes.LITERAL___thread); // NOI18N
        addConstText("__global", APTTokenTypes.LITERAL___global); // NOI18N
        addConstText("__hidden", APTTokenTypes.LITERAL___hidden); // NOI18N
        addConstText("__symbolic", APTTokenTypes.LITERAL___symbolic); // NOI18N
        addConstText("__decltype", APTTokenTypes.LITERAL___decltype); // NOI18N
        addConstText("__complex", APTTokenTypes.LITERAL___complex); // NOI18N
        addConstText("__forceinline", APTTokenTypes.LITERAL___forceinline); // NOI18N
        addConstText("__clrcall", APTTokenTypes.LITERAL___clrcall); // NOI18N
        addConstText("__try", APTTokenTypes.LITERAL___try); // NOI18N
        addConstText("__finally", APTTokenTypes.LITERAL___finally); // NOI18N
        addConstText("__forceinline", APTTokenTypes.LITERAL___forceinline); // NOI18N
        
        // APTGnuCFilter
        addConstText("__alignof__", APTTokenTypes.LITERAL___alignof__); // NOI18N
        addConstText("__asm", APTTokenTypes.LITERAL___asm); // NOI18N
        addConstText("__asm__", APTTokenTypes.LITERAL___asm__); // NOI18N
        addConstText("__attribute__", APTTokenTypes.LITERAL___attribute__); // NOI18N
        addConstText("__attribute", APTTokenTypes.LITERAL___attribute); // NOI18N
        addConstText("__complex__", APTTokenTypes.LITERAL___complex__); // NOI18N
        addConstText("__const", APTTokenTypes.LITERAL___const); // NOI18N
        addConstText("__const__", APTTokenTypes.LITERAL___const__); // NOI18N
        addConstText("__imag__", APTTokenTypes.LITERAL___imag); // NOI18N
        addConstText("__global", APTTokenTypes.LITERAL___global); // NOI18N
        addConstText("__hidden", APTTokenTypes.LITERAL___hidden); // NOI18N
        addConstText("__inline", APTTokenTypes.LITERAL___inline); // NOI18N
        addConstText("__inline__", APTTokenTypes.LITERAL___inline__); // NOI18N
        addConstText("__real__", APTTokenTypes.LITERAL___real); // NOI18N
        addConstText("restrict", APTTokenTypes.LITERAL_restrict); // NOI18N
        addConstText("__restrict", APTTokenTypes.LITERAL___restrict); // NOI18N
        addConstText("__restrict__", APTTokenTypes.LITERAL___restrict__); // NOI18N
        addConstText("__signed", APTTokenTypes.LITERAL___signed); // NOI18N
        addConstText("__signed__", APTTokenTypes.LITERAL___signed__); // NOI18N
        addConstText("__symbolic", APTTokenTypes.LITERAL___symbolic); // NOI18N
        addConstText("__thread", APTTokenTypes.LITERAL___thread); // NOI18N
        addConstText("__typeof", APTTokenTypes.LITERAL___typeof); // NOI18N
        addConstText("__typeof__", APTTokenTypes.LITERAL___typeof__); // NOI18N
        addConstText("__volatile", APTTokenTypes.LITERAL___volatile); // NOI18N
        addConstText("__volatile__", APTTokenTypes.LITERAL___volatile__); // NOI18N

        for (int i = 0; i < constText.length; i++) {
            String str = constText[i];
            if (str != null) {
                text2id.put(str, i);
            }
            constTextID[i] = CharSequences.create(str);
            if (str != null) {
                if (i > MAX_TEXT_ID) {
                    System.err.printf("APTConstTextToken: token %s [%d] is higher than MAX_TEXT_ID [%d]\n", str, i, MAX_TEXT_ID);
                }
            } else {
               // System.err.printf("APTConstTextToken: index [%d] does not have text \n", i);
            }
        }
//        assert TYPE_MASK >= LAST_CONST_TEXT_TOKEN;
//        System.err.printf("APTConstTextToken: %d\n", LAST_CONST_TEXT_TOKEN);
    }
    
    private static void addConstText(String text, int id) {
        assert constText[id] == null || constText[id].equals(text) : "Trying to redefine value " + text + " for already defined token type " + id + ", current value is " + constText[id];
        constText[id] = text;
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
        type = t;
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
        column = c;
    }
}
