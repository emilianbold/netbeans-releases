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

package org.netbeans.modules.cnd;

/**
 * MIME names.
 * We need these both in the loaders code and in the editor code
 * so we have a common definition here.
*/
public class MIMENames {

    /** C++ */
    public static final String CPLUSPLUS_MIME_TYPE = "text/x-c++"; //NOI18N

    /** C */
    public static final String C_MIME_TYPE = "text/x-c"; //NOI18N

    /** C/C++ Header */
    public static final String CHEADER_MIME_TYPE = "text/x-cheader"; //NOI18N

    /** Fortran */
    public static final String FORTRAN_MIME_TYPE = "text/x-fortran"; //NOI18N

    /** Makefiles */
    public static final String MAKEFILE_MIME_TYPE = "text/x-make"; //NOI18N

    /** Makefiles */
    public static final String SHELL_MIME_TYPE = "text/x-shell"; //NOI18N


    /** Visu x designer */
    public static final String VISU_MIME_TYPE = "text/x-visu"; //NOI18N

    /** Lex files */
    public static final String LEX_MIME_TYPE = "text/x-lex"; //NOI18N

    /** Yacc files */
    public static final String YACC_MIME_TYPE = "text/x-yacc"; //NOI18N

    /** SPARC Assembly files */
    public static final String ASM_MIME_TYPE = "text/x-sparc-asm"; //NOI18N

    /** ELF Executable files */
    public static final String ELF_EXE_MIME_TYPE = "application/x-executable+elf"; //NOI18N

    /** Generic Executable files */
    public static final String EXE_MIME_TYPE = "application/x-exe"; //NOI18N
    
    /** Generic Executable files */
    public static final String DLL_MIME_TYPE = "application/x-exe+dll"; //NOI18N

    /** ELF Core files */
    public static final String ELF_CORE_MIME_TYPE = "application/x-core+elf"; //NOI18N

    /** ELF Shared Object files */
    public static final String ELF_SHOBJ_MIME_TYPE = "application/x-shobj+elf"; //NOI18N

    /** ELF Object files */
    public static final String ELF_OBJECT_MIME_TYPE = "application/x-object+elf"; //NOI18N

    /** Generic ELF files (shouldn't be recognized anymore) */
    public static final String ELF_GENERIC_MIME_TYPE = "application/x-elf"; //NOI18N
}
