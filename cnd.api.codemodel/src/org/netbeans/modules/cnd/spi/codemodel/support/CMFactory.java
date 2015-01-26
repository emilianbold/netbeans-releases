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
package org.netbeans.modules.cnd.spi.codemodel.support;

import org.netbeans.modules.cnd.api.codemodel.CMComment;
import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMDiagnostic;
import org.netbeans.modules.cnd.api.codemodel.CMDiagnosticSet;
import org.netbeans.modules.cnd.api.codemodel.CMFile;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.api.codemodel.CMSourceLocation;
import org.netbeans.modules.cnd.api.codemodel.CMSourceRange;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;
import org.netbeans.modules.cnd.api.codemodel.completion.CMCompletionChunk;
import org.netbeans.modules.cnd.api.codemodel.completion.CMCompletionResult;
import org.netbeans.modules.cnd.api.codemodel.completion.CMCompletionResultList;
import org.netbeans.modules.cnd.api.codemodel.visit.CMDeclaration;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntity;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntityReference;
import org.netbeans.modules.cnd.api.codemodel.visit.CMInclude;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitLocation;
import org.netbeans.modules.cnd.spi.codemodel.CMCommentImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMCursorImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMDiagnosticImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMDiagnosticSetImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMFileImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMIndexImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMSourceLocationImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMSourceRangeImplementation;
import org.netbeans.modules.cnd.spi.codemodel.CMTranslationUnitImplementation;
import org.netbeans.modules.cnd.spi.codemodel.completion.CMCompletionChunkImplementation;
import org.netbeans.modules.cnd.spi.codemodel.completion.CMCompletionResultImplementation;
import org.netbeans.modules.cnd.spi.codemodel.completion.CMCompletionResultListImplementation;
import org.netbeans.modules.cnd.spi.codemodel.impl.APIAccessor;
import org.netbeans.modules.cnd.spi.codemodel.impl.APICompletionAccessor;
import org.netbeans.modules.cnd.spi.codemodel.impl.APIIndexAccessor;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMDeclarationImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMEntityImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMEntityReferenceImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMIncludeImplementation;
import org.netbeans.modules.cnd.spi.codemodel.visit.CMVisitLocationImplementation;

/**
 *
 * @author Vladimir Voskresensky
 * @author Vladimir Kvashin
 */
public final class CMFactory {

    /**
     * factory methods for elements from code model API package
     */
    public final static class CoreAPI {

        public static CMComment createComment(CMCommentImplementation impl) {
            return APIAccessor.get().createComment(impl);
        }
        
        public static CMCursor createCursor(CMCursorImplementation impl) {
            return APIAccessor.get().createCursor(impl);
        }

        public static CMDiagnostic createDiagnostic(CMDiagnosticImplementation impl) {
            return APIAccessor.get().createDiagnostic(impl);
        }

        public static CMDiagnosticSet createDiagnosticSet(CMDiagnosticSetImplementation impl) {
            return APIAccessor.get().createDiagnosticSet(impl);
        }

        public static CMFile createFile(CMFileImplementation impl) {
            return APIAccessor.get().createFile(impl);
        }

        public static CMIndex createIndex(CMIndexImplementation impl) {
            return APIAccessor.get().createIndex(impl);
        }

        public static CMSourceLocation createSourceLocation(CMSourceLocationImplementation impl) {
            return APIAccessor.get().createSourceLocation(impl);
        }

        public static CMSourceRange createSourceRange(CMSourceRangeImplementation impl) {
            return APIAccessor.get().createSourceRange(impl);
        }

        public static CMTranslationUnit createTranslationUnit(CMTranslationUnitImplementation impl) {
            return APIAccessor.get().createTranslationUnit(impl);
        }

        private CoreAPI() {
        }
    }

    /**
     * factory methods for elements from code completion API package
     */
    public final static class CompletionAPI {

        public static CMCompletionResultList createCompletionResultList(CMCompletionResultListImplementation impl) {
            return APICompletionAccessor.get().createCompletionResultList(impl);
        }

        public static CMCompletionResult createCompletionResult(CMCompletionResultImplementation impl) {
            return APICompletionAccessor.get().createCompletionResult(impl);
        }

        public static CMCompletionChunk createCompletionResultList(CMCompletionChunkImplementation impl) {
            return APICompletionAccessor.get().createCompletionChunk(impl);
        }

        private CompletionAPI() {
        }
    }

    /**
     * factory methods for elements from index API package
     */
    public final static class IndexAPI {

        public static CMEntity createEntity(CMEntityImplementation impl) {
            return APIIndexAccessor.get().createEntity(impl);
        }

        public static CMDeclaration createDeclaration(CMDeclarationImplementation impl) {
            return APIIndexAccessor.get().createDeclaration(impl);
        }

        public static CMEntityReference createEntityReference(CMEntityReferenceImplementation impl) {
            return APIIndexAccessor.get().createEntityReference(impl);
        }

        public static CMInclude createInclude(CMIncludeImplementation impl) {
            return APIIndexAccessor.get().createInclude(impl);
        }

        public static CMVisitLocation createVisitLocation(CMVisitLocationImplementation impl) {
            return APIIndexAccessor.get().createVisitLocation(impl);
        }

        private IndexAPI() {
        }
    }

    private CMFactory() {
    }
}
