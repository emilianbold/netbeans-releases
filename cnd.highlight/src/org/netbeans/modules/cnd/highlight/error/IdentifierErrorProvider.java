/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.highlight.error;

import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo.Severity;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmTemplateBasedReferencedObject;
import org.netbeans.modules.cnd.highlight.semantic.SemanticHighlighter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.NbBundle;

/**
 * Provides information about unresolved identifiers.
 *
 * @author Alexey Vladykin
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider.class, position=30)
public class IdentifierErrorProvider extends CsmErrorProvider {

    private static final boolean ENABLED =
            CndUtils.getBoolean("cnd.identifier.error.provider", true); //NOI18N
    private static final boolean SHOW_TIMES = Boolean.getBoolean("cnd.identifier.error.provider.times");

    private static final int MAX_ERROR_LIMIT;
    static {
        String limit = System.getProperty("cnd.highlighting.error.limit"); // NOI18N
        int userInput = 100;
        if (limit != null) {
            try {
                userInput = Integer.parseInt(limit);
            } catch (Exception e) {
                // skip
            }
        }
        MAX_ERROR_LIMIT = userInput;
    }

    @Override
    protected boolean validate(CsmErrorProvider.Request request) {
        return super.validate(request) && ENABLED && !disableAsLibraryHeaderFile(request.getFile()) && request.getFile().isParsed();
    }

    @Override
    public boolean isEnabledByDefault() {
        return !CndUtils.isReleaseMode();
    }
    
    @Override
    protected void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        long start = System.currentTimeMillis();
        if (SemanticHighlighter.isVeryBigDocument(request.getDocument())) {
            return;
        }
        if (SHOW_TIMES) System.err.println("#@# Error Highlighting update() have started for file " + request.getFile().getAbsolutePath());
        CsmFileReferences.getDefault().accept(
                request.getFile(), new ReferenceVisitor(request, response),
                CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE);
        if (SHOW_TIMES) System.err.println("#@# Error Highlighting update() done in "+ (System.currentTimeMillis() - start) +"ms for file " + request.getFile().getAbsolutePath());
    }
    
    public String getName() {
        return "unresolved-identifier"; //NOI18N
    }

    private static class ReferenceVisitor implements CsmFileReferences.Visitor {

        private final CsmErrorProvider.Request request;
        private final CsmErrorProvider.Response response;
        private int foundError = 0;


        public ReferenceVisitor(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
            this.request = request;
            this.response = response;
        }

        public void visit(CsmReferenceContext context) {
            if (MAX_ERROR_LIMIT >= 0 && foundError >= MAX_ERROR_LIMIT){
                return;
            }
            CsmReference ref = context.getReference();
            final CsmObject referencedObject = ref.getReferencedObject();
            if (!request.isCancelled()) {
                if (referencedObject == null) {
                    if (CsmFileReferences.isAfterUnresolved(context)) {
                        return;
                    }
                    if (CsmFileReferences.isBuiltInBased(ref) || CsmFileReferences.isMacroBased(context)) {
                        return;
                    }
                    Severity severity = Severity.ERROR;

                    if (CsmFileReferences.isTemplateBased(context)) {
                        severity = Severity.WARNING;
                    } else if (CsmKindUtilities.isClassForwardDeclaration(ref.getOwner())) {
                        severity = Severity.WARNING;
                    }
                    foundError++;
                    response.addError(new IdentifierErrorInfo(
                            ref.getStartOffset(), ref.getEndOffset(),
                            ref.getText().toString(), severity));
                } else if (referencedObject instanceof CsmTemplateBasedReferencedObject) {
                    if (CsmFileReferences.isAfterUnresolved(context)) {
                        return;
                    }
                    Severity severity = Severity.WARNING;
                    foundError++;
                    response.addError(new IdentifierErrorInfo(
                            ref.getStartOffset(), ref.getEndOffset(),
                            ref.getText().toString(), severity));
                }
            }
        }
    }

    private static class IdentifierErrorInfo implements CsmErrorInfo {

        private final int startOffset;
        private final int endOffset;
        private final String message;
        private final Severity severity;

        public IdentifierErrorInfo(int startOffset, int endOffset, String name, Severity severity) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.message = NbBundle.getMessage(IdentifierErrorProvider.class,
                    "HighlightProvider_IdentifierMissed", name); //NOI18N
            this.severity = severity;
        }

        public String getMessage() {
            return message;
        }

        public Severity getSeverity() {
            return severity;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

    }
}
