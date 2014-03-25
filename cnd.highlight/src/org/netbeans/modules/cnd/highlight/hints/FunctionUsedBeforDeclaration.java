/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.highlight.hints;

import java.util.EnumSet;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.analysis.api.AuditPreferences;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class FunctionUsedBeforDeclaration extends CodeAuditInfo {
    private FunctionUsedBeforDeclaration(String id, String name, String description, String defaultSeverity, boolean defaultEnabled, AuditPreferences myPreferences) {
        super(id, name, description, defaultSeverity, defaultEnabled, myPreferences);
    }
    static FunctionUsedBeforDeclaration create(AuditPreferences myPreferences) {
        String id = NbBundle.getMessage(FunctionUsedBeforDeclaration.class, "FunctionUsedBeforDeclaration.name"); // NOI18N
        String description = NbBundle.getMessage(FunctionUsedBeforDeclaration.class, "FunctionUsedBeforDeclaration.description"); // NOI18N
        return new FunctionUsedBeforDeclaration(id, id, description, "warning", false, myPreferences); // NOI18N
    }
    
    @Override
    void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        CsmFile file = request.getFile();
        if (file != null) {
            if (request.isCancelled()) {
                return;
            }
            CsmFileReferences.getDefault().accept(
                    request.getFile(), new ReferenceVisitor(request, response),
                    CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE);
        }
    }

    private class ReferenceVisitor implements CsmFileReferences.Visitor {

        private final CsmErrorProvider.Request request;
        private final CsmErrorProvider.Response response;


        public ReferenceVisitor(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
            this.request = request;
            this.response = response;
        }

        @Override
        public void visit(CsmReferenceContext context) {
            if (!request.isCancelled()) {
                CsmReference ref = context.getReference();
                if (CsmReferenceResolver.getDefault().isKindOf(ref, EnumSet.of(CsmReferenceKind.DIRECT_USAGE))) {
                    CsmObject referencedObject = ref.getReferencedObject();
                    if (CsmKindUtilities.isFunction(referencedObject)) {
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
                        CsmErrorInfo.Severity severity;
                        if ("error".equals(minimalSeverity())) { // NOI18N
                            severity = CsmErrorInfo.Severity.ERROR;
                        } else {
                            severity = CsmErrorInfo.Severity.WARNING;
                        }
                        String message = NbBundle.getMessage(FunctionUsedBeforDeclaration.class, "FunctionUsedBeforDeclaration.message", fun.getName()); // NOI18N
                        if (response instanceof AnalyzerResponse) {
                            ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, ref.getContainingFile().getFileObject(),
                                    new ErrorInfoImpl(getID()+"\n"+message, severity, ref.getStartOffset(), ref.getEndOffset())); // NOI18N
                        } else {
                            response.addError(new ErrorInfoImpl(message, severity, ref.getStartOffset(), ref.getEndOffset()));
                        }
                        
                    }
                }
            }
        }
    }
}
