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

package org.netbeans.modules.cnd.apt.impl.support.lang;

import org.netbeans.modules.cnd.apt.support.APTTokenTypes;

/**
 * filter for Std C language
 * @author Vladimir Voskresensky
 */
public class APTStdCFilter extends APTBaseLanguageFilter {
    
    /**
     * Creates a new instance of APTStdCFilter
     */
    public APTStdCFilter() {
        initialize();
    }

    private void initialize() {
        // TODO: clean up!!!
        filter("alignof", APTTokenTypes.LITERAL_alignof);
        filter("typeof", APTTokenTypes.LITERAL_typeof);      
        filter("typedef", APTTokenTypes.LITERAL_typedef);
        filter("enum", APTTokenTypes.LITERAL_enum);       
        filter("extern", APTTokenTypes.LITERAL_extern);
        filter("inline", APTTokenTypes.LITERAL_inline);
        filter("_inline", APTTokenTypes.LITERAL__inline);        
        filter("_stdcall", APTTokenTypes.LITERAL__stdcall);
        filter("__stdcall", APTTokenTypes.LITERAL___stdcall);        
        filter("auto", APTTokenTypes.LITERAL_auto);
        filter("register", APTTokenTypes.LITERAL_register);
        filter("static", APTTokenTypes.LITERAL_static);        
        filter("const", APTTokenTypes.LITERAL_const);
        filter("__const", APTTokenTypes.LITERAL___const);
        filter("const_cast", APTTokenTypes.LITERAL_const_cast);
        filter("volatile", APTTokenTypes.LITERAL_volatile);
        filter("char", APTTokenTypes.LITERAL_char);
        filter("wchar_t", APTTokenTypes.LITERAL_wchar_t);
        filter("bool", APTTokenTypes.LITERAL_bool);
        filter("short", APTTokenTypes.LITERAL_short);
        filter("int", APTTokenTypes.LITERAL_int);
        filter("long", APTTokenTypes.LITERAL_long);
        filter("unsigned", APTTokenTypes.LITERAL_unsigned);
        filter("__unsigned__", APTTokenTypes.LITERAL___unsigned__);
        filter("float", APTTokenTypes.LITERAL_float);
        filter("double", APTTokenTypes.LITERAL_double);
        filter("void", APTTokenTypes.LITERAL_void);
        filter("_declspec", APTTokenTypes.LITERAL__declspec);
        filter("__declspec", APTTokenTypes.LITERAL___declspec);   
        filter("struct", APTTokenTypes.LITERAL_struct);
        filter("union", APTTokenTypes.LITERAL_union);   
        filter("true", APTTokenTypes.LITERAL_true);
        filter("false", APTTokenTypes.LITERAL_false);        
        filter("case", APTTokenTypes.LITERAL_case);
        filter("default", APTTokenTypes.LITERAL_default);
        filter("if", APTTokenTypes.LITERAL_if);
        filter("else", APTTokenTypes.LITERAL_else);
        filter("switch", APTTokenTypes.LITERAL_switch);
        filter("while", APTTokenTypes.LITERAL_while);
        filter("do", APTTokenTypes.LITERAL_do);
        filter("for", APTTokenTypes.LITERAL_for);
        filter("goto", APTTokenTypes.LITERAL_goto);
        filter("continue", APTTokenTypes.LITERAL_continue);
        filter("break", APTTokenTypes.LITERAL_break);
        filter("return", APTTokenTypes.LITERAL_return);        
        filter("asm", APTTokenTypes.LITERAL_asm);
        filter("_asm", APTTokenTypes.LITERAL__asm);
        filter("sizeof", APTTokenTypes.LITERAL_sizeof);
        filter("_cdecl", APTTokenTypes.LITERAL__cdecl);
        filter("__cdecl", APTTokenTypes.LITERAL___cdecl);
        filter("_near", APTTokenTypes.LITERAL__near);
        filter("__near", APTTokenTypes.LITERAL___near);
        filter("_far", APTTokenTypes.LITERAL__far);
        filter("__far", APTTokenTypes.LITERAL___far);
        filter("__interrupt", APTTokenTypes.LITERAL___interrupt);
        filter("pascal", APTTokenTypes.LITERAL_pascal);
        filter("_pascal", APTTokenTypes.LITERAL__pascal);
        filter("__pascal", APTTokenTypes.LITERAL___pascal);        
        filter("_int64", APTTokenTypes.LITERAL__int64);
        filter("__int64", APTTokenTypes.LITERAL___int64);
        filter("__w64", APTTokenTypes.LITERAL___w64);
        filter("__extension__", APTTokenTypes.LITERAL___extension__);
        filter("__restrict", APTTokenTypes.LITERAL___restrict);    
        
    }    
}
