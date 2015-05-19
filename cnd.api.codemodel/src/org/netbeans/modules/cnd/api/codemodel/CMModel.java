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
package org.netbeans.modules.cnd.api.codemodel;

import java.util.Collection;
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
import org.netbeans.modules.cnd.spi.codemodel.impl.APIAccessor;
import org.netbeans.modules.cnd.spi.codemodel.support.SPIUtilities;

/**
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public final class CMModel {

    private CMModel() {
    }
    
    public static CMTranslationUnit findTranslationUnit(CMFile file) {
        return null;
    }
    
    public static Collection<CMIndex> getIndices(CMFile file) {
        return SPIUtilities.getIndices(file.getURI());
    }

    public static Collection<CMIndex> getIndices() {
        return SPIUtilities.getIndices();
    }

    //<editor-fold defaultstate="collapsed" desc="hidden impl">
    static {
        APIAccessor.register(new APIAccessorImpl());
    }
    
    private static final class APIAccessorImpl extends APIAccessor {
        @Override
        public CMIndex createIndex(CMIndexImplementation impl) {
            return CMIndex.fromImpl(impl);
        }
        
        @Override
        public CMTranslationUnit createTranslationUnit(CMTranslationUnitImplementation impl) {
            return CMTranslationUnit.fromImpl(impl);
        }
        
        @Override
        public CMFile createFile(CMFileImplementation impl) {
            return CMFile.fromImpl(impl);
        }

        @Override
        public CMComment createComment(CMCommentImplementation impl) {
            return new CMComment(impl);
        }
        
        @Override
        public CMCursor createCursor(CMCursorImplementation impl) {
            return CMCursor.fromImpl(impl);
        }

        @Override
        public CMToken createToken(CMTokenImplementation impl) {
            return CMToken.fromImpl(impl);
        }

        @Override
        public CMDiagnostic createDiagnostic(CMDiagnosticImplementation impl) {
            return CMDiagnostic.fromImpl(impl);
        }

        @Override
        public CMDiagnosticSet createDiagnosticSet(CMDiagnosticSetImplementation impl) {
            return new CMDiagnosticSet(impl);
        }

        @Override
        public CMSourceLocation createSourceLocation(CMSourceLocationImplementation impl) {
            return CMSourceLocation.fromImpl(impl);
        }

        @Override
        public CMSourceRange createSourceRange(CMSourceRangeImplementation impl) {
            return CMSourceRange.fromImpl(impl);
        }
        
        @Override
        public CMFileImplementation getFileImpl(CMFile file) {
            return file.getImpl();
        }

        @Override
        public CMCursorImplementation getCursorImpl(CMCursor cursor) {
            return cursor.getImpl();
        }

        @Override
        public CMTokenImplementation getTokenImpl(CMToken token) {
            return token.getImpl();
        }

        @Override
        public CMIndexImplementation getIndexImpl(CMIndex idx) {
            return idx.getImpl();
        }

        @Override
        public CMTranslationUnitImplementation getTUImpl(CMTranslationUnit tu) {
            return tu.getImpl();
        }

        @Override
        public CMDiagnosticImplementation getDiagnosticImpl(CMDiagnostic diag) {
            return diag.getImpl();
        }

        @Override
        public CMSourceLocationImplementation getSourceLocationImpl(CMSourceLocation loc) {
            return loc.getImpl();
        }

        @Override
        public CMSourceRangeImplementation getSourceRangeImpl(CMSourceRange range) {
            return range.getImpl();
        }
    }
    //</editor-fold>
}
