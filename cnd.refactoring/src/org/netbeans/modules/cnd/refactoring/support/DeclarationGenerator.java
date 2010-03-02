/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.refactoring.support;

import org.netbeans.modules.cnd.api.model.CsmField;

/**
 * utilities to create declarations
 * @author Vladimir Voskresensky
 */
public final class DeclarationGenerator {

    public static final String INLINE_PROPERTY = "inline_method"; // NOI18N
    public static final String INSERT_CODE_INLINE_PROPERTY = "insert_code_inline_method"; // NOI18N

    public enum Kind {
        INLINE_DEFINITION,
        DECLARATION,
        EXTERNAL_DEFINITION
    }
    
    private DeclarationGenerator() {
    }

    public static String createGetter(CsmField field, String gName, Kind kind) {
        StringBuilder out = new StringBuilder();
        // type information is the first
        if (field.isStatic()) {
            out.append("static "); //NOI18N
        }
        out.append(field.getType().getText()).append(" "); //NOI18N
        // add name
        if (kind == Kind.EXTERNAL_DEFINITION) {
            // external definition needs class prefix
            out.append(field.getContainingClass().getName()).append("::"); // NOI18N
        }
        out.append(gName).append("() const "); // NOI18N
        if (kind == Kind.DECLARATION) {
            out.append(";"); //NOI18N
        } else {
            out.append("{ ").append("return ").append(field.getName()).append(";}"); // NOI18N
        }
        return out.toString();
    }

    public static String createSetter(CsmField field, String sName, Kind kind) {
        StringBuilder out = new StringBuilder();
        CharSequence fldName = field.getName();
        String paramName = GeneratorUtils.stripFieldPrefix(fldName.toString());
        out.append("\n"); // NOI18N
        // type information is the first
        if (field.isStatic()) {
            out.append("static "); //NOI18N
        }
        out.append("void "); //NOI18N
        // add name
        if (kind == Kind.EXTERNAL_DEFINITION) {
            // external definition needs class prefix
            out.append(field.getContainingClass().getName()).append("::"); // NOI18N
        }
        out.append(sName).append("("); // NOI18N
        // add parameter
        out.append(field.getType().getText());
        out.append(" ").append(paramName);// NOI18N
        out.append(")"); // NOI18N
        if (kind == Kind.DECLARATION) {
            out.append(";"); //NOI18N
        } else {
            out.append("{ ").append("this->").append(fldName).append("=").append(paramName).append(";}"); // NOI18N
        }
        return out.toString();
    }

}
