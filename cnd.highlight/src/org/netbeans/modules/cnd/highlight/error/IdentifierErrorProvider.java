/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.highlight.error;

import java.util.EnumSet;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo.Severity;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmTemplateBasedReferencedObject;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.ui.NamedOption;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Provides information about unresolved identifiers.
 *
 * @author Alexey Vladykin
 */
@ServiceProviders({
    @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=1100),
    @ServiceProvider(service=CsmErrorProvider.class, position=30)
})
public class IdentifierErrorProvider extends CsmErrorProvider {

    private static final boolean ENABLED = CndUtils.getBoolean("cnd.identifier.error.provider", true); //NOI18N
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
    public OptionKind getKind() {
        return OptionKind.Boolean;
    }

    @Override
    public Object getDefaultValue() {
        return true;//!CndUtils.isReleaseMode();
    }
    
    @Override
    protected void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        long start = System.currentTimeMillis();
        for (CsmErrorProvider.RequestValidator p : Lookup.getDefault().lookupAll(CsmErrorProvider.RequestValidator.class)) {
            if(!p.isValid(this, request)) {
                return;
            }
        }
        Thread currentThread = Thread.currentThread();
        CsmFile file = request.getFile();
        currentThread.setName("Provider "+getName()+" prosess "+file.getAbsolutePath()); // NOI18N
        if (SHOW_TIMES) System.err.println("#@# Error Highlighting update() have started for file " + file.getAbsolutePath());
        CsmFileReferences.getDefault().accept(
                request.getFile(), new ReferenceVisitor(request, response),
                CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE);
        if (SHOW_TIMES) System.err.println("#@# Error Highlighting update() done in "+ (System.currentTimeMillis() - start) +"ms for file " + request.getFile().getAbsolutePath());
    }
    
    @Override
    public String getName() {
        return "unresolved-identifier"; //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(IdentifierErrorProvider.class, "Show-unresolved-identifier"); //NOI18N
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(IdentifierErrorProvider.class, "Show-unresolved-identifier-AD"); //NOI18N
    }

    private static class ReferenceVisitor implements CsmFileReferences.Visitor {

        private final CsmErrorProvider.Request request;
        private final CsmErrorProvider.Response response;
        private int foundError = 0;


        public ReferenceVisitor(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
            this.request = request;
            this.response = response;
        }

        @Override
        public void visit(CsmReferenceContext context) {
            if (MAX_ERROR_LIMIT >= 0 && foundError >= MAX_ERROR_LIMIT){
                return;
            }
            if (!request.isCancelled()) {
                CsmReference ref = context.getReference();
                final CsmObject referencedObject = ref.getReferencedObject();
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
                    } else if (CsmKindUtilities.isClassForwardDeclaration(ref.getOwner())) { // owner is needed
                        severity = Severity.WARNING;
                    } else if (CsmKindUtilities.isEnumForwardDeclaration(ref.getOwner())) { // owner is needed
                        severity = Severity.WARNING;
                    }
                    foundError++;
                    response.addError(new ErrorInfoImpl(
                            ref.getStartOffset(), ref.getEndOffset(),
                            ref.getText().toString(), severity, "HighlightProvider_IdentifierMissed"));// NOI18N
                } else if (referencedObject instanceof CsmTemplateBasedReferencedObject) {
                    if (CsmFileReferences.isAfterUnresolved(context)) {
                        return;
                    }
                    if (CsmFileReferences.isBuiltInBased(ref) || CsmFileReferences.isMacroBased(context)) {
                        return;
                    }
                    Severity severity = Severity.WARNING;
                    foundError++;
                    response.addError(new ErrorInfoImpl(
                            ref.getStartOffset(), ref.getEndOffset(),
                            ref.getText().toString(), severity, "HighlightProvider_IdentifierMissed")); //NOI18N
                } else if (CsmKindUtilities.isFunctionDefinition(referencedObject)) {
                    // check function definition without declaration
                    if (CsmReferenceResolver.getDefault().isKindOf(ref, EnumSet.of(CsmReferenceKind.DEFINITION))) {
                        if (((CsmFunction)referencedObject).getDeclaration() == null) {
                            Severity severity = Severity.ERROR;
                            foundError++;
                            //TODO: we can be more clever here and provide user with hint to create declaration
                            response.addError(new ErrorInfoImpl(
                                    ref.getStartOffset(), ref.getEndOffset(),
                                    ref.getText().toString(), severity, "HighlightProvider_FunDeclarationMissed")); //NOI18N
                        }
                    }
                } else if (false && referencedObject instanceof CsmFunction) {
                    // Check for function usages befor it's declaration
                    if (CsmReferenceResolver.getDefault().isKindOf(ref, EnumSet.of(CsmReferenceKind.DEFINITION,
                            CsmReferenceKind.DECLARATION,
                            CsmReferenceKind.IN_DEAD_BLOCK,
                            CsmReferenceKind.IN_PREPROCESSOR_DIRECTIVE))) {
                        return;
                    }
                    CsmFunction fun = (CsmFunction) referencedObject;
                    if (fun.getContainingFile() != ref.getContainingFile()) {
                        return;
                    }
                    if (fun.getStartOffset() <= ref.getStartOffset()) {
                        return;
                    }
                    if (!CsmKindUtilities.isGlobalFunction(fun)) {
                        return;
                    }
                    CsmFunction funDecl = fun.getDeclaration();
                    if(funDecl == null) {
                        return;
                    }
                    if (funDecl.getContainingFile() != ref.getContainingFile()) {
                        return;
                    }
                    if (funDecl.getStartOffset() <= ref.getStartOffset()) {
                        return;
                    }
                    Severity severity = Severity.WARNING;
                    foundError++;
                    response.addError(new ErrorInfoImpl(
                            ref.getStartOffset(), ref.getEndOffset(),
                            ref.getText().toString(), severity, 
                            "HighlightProvider_DeclarationAfterUsage")); //NOI18N
                }
            }
        }
    }

    private final static class ErrorInfoImpl implements CsmErrorInfo {

        private final int startOffset;
        private final int endOffset;
        private final String message;
        private final Severity severity;

        public ErrorInfoImpl(int startOffset, int endOffset, String name, Severity severity, String bundleKey) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.message = NbBundle.getMessage(IdentifierErrorProvider.class, bundleKey, name);
            this.severity = severity;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public Severity getSeverity() {
            return severity;
        }

        @Override
        public int getStartOffset() {
            return startOffset;
        }

        @Override
        public int getEndOffset() {
            return endOffset;
        }
    }
}
