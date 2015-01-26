/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.spi.codemodel.impl;

import org.netbeans.modules.cnd.api.codemodel.CMComment;
import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMDiagnostic;
import org.netbeans.modules.cnd.api.codemodel.CMDiagnosticSet;
import org.netbeans.modules.cnd.api.codemodel.CMFile;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.api.codemodel.CMModel;
import org.netbeans.modules.cnd.api.codemodel.CMSourceLocation;
import org.netbeans.modules.cnd.api.codemodel.CMSourceRange;
import org.netbeans.modules.cnd.api.codemodel.CMToken;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;
import org.netbeans.modules.cnd.spi.codemodel.CMCommentImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMCursorImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMDiagnosticImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMDiagnosticSetImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMFileImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMIndexImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMSourceLocationImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMSourceRangeImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMTokenImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMTranslationUnitImplementation;

/**
 *
 * @author Vladimir Voskresensky
 */
public abstract class APIAccessor {

    private static APIAccessor INSTANCE;

    public static synchronized APIAccessor get() {
        if (INSTANCE == null) {
            Class<?> c = CMModel.class;
            try {
                Class.forName(c.getName(), true, c.getClassLoader());
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }
        assert INSTANCE != null : "Failed to create instance";
        return INSTANCE;
    }

    public static synchronized void register(APIAccessor accessor) {
        assert INSTANCE == null : "must be registered once " + INSTANCE;
        INSTANCE = accessor;
    }

    public abstract CMIndex createIndex(CMIndexImplementation impl);

    public abstract CMTranslationUnit createTranslationUnit(CMTranslationUnitImplementation impl);

    public abstract CMFile createFile(CMFileImplementation impl);

    public abstract CMComment createComment(CMCommentImplementation impl);

    public abstract CMCursor createCursor(CMCursorImplementation impl);

    public abstract CMToken createToken(CMTokenImplementation impl);

    public abstract CMDiagnostic createDiagnostic(CMDiagnosticImplementation impl);

    public abstract CMDiagnosticSet createDiagnosticSet(CMDiagnosticSetImplementation impl);

    public abstract CMSourceLocation createSourceLocation(CMSourceLocationImplementation impl);

    public abstract CMSourceRange createSourceRange(CMSourceRangeImplementation impl);

    /* access to implementations */
    public abstract CMFileImplementation getFileImpl(CMFile file);

    public abstract CMCursorImplementation getCursorImpl(CMCursor cursor);
    
    public abstract CMTokenImplementation getTokenImpl(CMToken token);

    public abstract CMIndexImplementation getIndexImpl(CMIndex idx);

    public abstract CMTranslationUnitImplementation getTUImpl(CMTranslationUnit tu);

    public abstract CMDiagnosticImplementation getDiagnosticImpl(CMDiagnostic diag);

    public abstract CMSourceLocationImplementation getSourceLocationImpl(CMSourceLocation loc);

    public abstract CMSourceRangeImplementation getSourceRangeImpl(CMSourceRange loc);
}
