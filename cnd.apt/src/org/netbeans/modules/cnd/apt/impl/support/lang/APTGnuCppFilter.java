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
 * filter for GNU C++ language
 * @author Vladimir Voskresensky
 */
public class APTGnuCppFilter extends APTBaseLanguageFilter {
    
    public APTGnuCppFilter() {
        initialize();
    }
    
    private void initialize() {
        // TODO clean up!
        filter("operator", APTTokenTypes.LITERAL_OPERATOR);
        filter("alignof", APTTokenTypes.LITERAL_alignof);
        filter("__alignof__", APTTokenTypes.LITERAL___alignof__);
        filter("typeof", APTTokenTypes.LITERAL_typeof);
        filter("__typeof", APTTokenTypes.LITERAL___typeof);
        filter("__typeof__", APTTokenTypes.LITERAL___typeof__);
        filter("template", APTTokenTypes.LITERAL_template);
        filter("typedef", APTTokenTypes.LITERAL_typedef);
        filter("enum", APTTokenTypes.LITERAL_enum);
        filter("namespace", APTTokenTypes.LITERAL_namespace);
        filter("extern", APTTokenTypes.LITERAL_extern);
        filter("inline", APTTokenTypes.LITERAL_inline);
        filter("_inline", APTTokenTypes.LITERAL__inline);
        filter("__inline", APTTokenTypes.LITERAL___inline);
        filter("__inline__", APTTokenTypes.LITERAL___inline__);
        filter("virtual", APTTokenTypes.LITERAL_virtual);
        filter("explicit", APTTokenTypes.LITERAL_explicit);
        filter("friend", APTTokenTypes.LITERAL_friend);
        filter("_stdcall", APTTokenTypes.LITERAL__stdcall);
        filter("__stdcall", APTTokenTypes.LITERAL___stdcall);
        filter("typename", APTTokenTypes.LITERAL_typename);
        filter("auto", APTTokenTypes.LITERAL_auto);
        filter("register", APTTokenTypes.LITERAL_register);
        filter("static", APTTokenTypes.LITERAL_static);
        filter("mutable", APTTokenTypes.LITERAL_mutable);
        filter("const", APTTokenTypes.LITERAL_const);
        filter("__const", APTTokenTypes.LITERAL___const);
        filter("const_cast", APTTokenTypes.LITERAL_const_cast);
        filter("volatile", APTTokenTypes.LITERAL_volatile);
        filter("__volatile__", APTTokenTypes.LITERAL___volatile__);
        filter("char", APTTokenTypes.LITERAL_char);
        filter("wchar_t", APTTokenTypes.LITERAL_wchar_t);
        filter("bool", APTTokenTypes.LITERAL_bool);
        filter("short", APTTokenTypes.LITERAL_short);
        filter("int", APTTokenTypes.LITERAL_int);
        filter("long", APTTokenTypes.LITERAL_long);
        filter("signed", APTTokenTypes.LITERAL_signed);
        filter("__signed__", APTTokenTypes.LITERAL___signed__);
        filter("unsigned", APTTokenTypes.LITERAL_unsigned);
        filter("__unsigned__", APTTokenTypes.LITERAL___unsigned__);
        filter("float", APTTokenTypes.LITERAL_float);
        filter("double", APTTokenTypes.LITERAL_double);
        filter("void", APTTokenTypes.LITERAL_void);
        filter("_declspec", APTTokenTypes.LITERAL__declspec);
        filter("__declspec", APTTokenTypes.LITERAL___declspec);
        filter("class", APTTokenTypes.LITERAL_class);
        filter("struct", APTTokenTypes.LITERAL_struct);
        filter("union", APTTokenTypes.LITERAL_union);        
        filter("this", APTTokenTypes.LITERAL_this);
        filter("true", APTTokenTypes.LITERAL_true);
        filter("false", APTTokenTypes.LITERAL_false);
        filter("public", APTTokenTypes.LITERAL_public);
        filter("protected", APTTokenTypes.LITERAL_protected);
        filter("private", APTTokenTypes.LITERAL_private);
        filter("throw", APTTokenTypes.LITERAL_throw);
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
        filter("try", APTTokenTypes.LITERAL_try);
        filter("catch", APTTokenTypes.LITERAL_catch);
        filter("using", APTTokenTypes.LITERAL_using);
        filter("asm", APTTokenTypes.LITERAL_asm);
        filter("_asm", APTTokenTypes.LITERAL__asm);
        filter("__asm", APTTokenTypes.LITERAL___asm);
        filter("__asm__", APTTokenTypes.LITERAL___asm__);
        filter("sizeof", APTTokenTypes.LITERAL_sizeof);
        filter("dynamic_cast", APTTokenTypes.LITERAL_dynamic_cast);
        filter("static_cast", APTTokenTypes.LITERAL_static_cast);
        filter("reinterpret_cast", APTTokenTypes.LITERAL_reinterpret_cast);
        filter("new", APTTokenTypes.LITERAL_new);
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
        filter("delete", APTTokenTypes.LITERAL_delete);
        filter("_int64", APTTokenTypes.LITERAL__int64);
        filter("__int64", APTTokenTypes.LITERAL___int64);
        filter("__w64", APTTokenTypes.LITERAL___w64);
        filter("__extension__", APTTokenTypes.LITERAL___extension__);
        filter("__attribute__", APTTokenTypes.LITERAL___attribute__);
        filter("__restrict", APTTokenTypes.LITERAL___restrict);
        filter("__complex__", APTTokenTypes.LITERAL___complex);
        filter("__imag__", APTTokenTypes.LITERAL___imag);
        filter("__real__", APTTokenTypes.LITERAL___real); 
        filter("export", APTTokenTypes.LITERAL_export);            
    }
    
}
