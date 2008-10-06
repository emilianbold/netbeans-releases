/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.utils;

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

    /** Fortran */
    public static final String FORTRAN_MIME_TYPE = "text/x-fortran"; //NOI18N

    /** Makefiles */
    public static final String MAKEFILE_MIME_TYPE = "text/x-make"; //NOI18N

    /** Shell */
    public static final String SHELL_MIME_TYPE = "text/sh"; //NOI18N


    /** Visu x designer */
    public static final String VISU_MIME_TYPE = "text/x-visu"; //NOI18N

    /** Lex files */
    public static final String LEX_MIME_TYPE = "text/x-lex"; //NOI18N

    /** Yacc files */
    public static final String YACC_MIME_TYPE = "text/x-yacc"; //NOI18N

    /** SPARC Assembly files */
    public static final String ASM_MIME_TYPE = "text/x-asm"; //NOI18N

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
