/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.security;

import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.analysis.api.AnalyzerResponse;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.highlight.hints.ErrorInfoImpl;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Danila Sergeyev
 */
public class FunctionUsageAudit extends AbstractCodeAudit {
    private final FunctionsXmlService.Level level;
    private final FunctionsXmlService.Category category;
    private final String customType;
    
    public FunctionUsageAudit(FunctionsXmlService.Level level, FunctionsXmlService.Category category, String id, String name, String description, String defaultSeverity, String customType, boolean defaultEnabled, AuditPreferences myPreferences) {
        super(id, name, description, defaultSeverity, defaultEnabled, myPreferences);
        this.level = level;
        this.category = category;
        this.customType = customType;
    }
    
    @Override
    public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
        return kind == CsmErrorProvider.EditorEvent.FileBased;
    }
    
    @Override
    public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        CsmFile file = request.getFile();
        if (file != null) {
            if (request.isCancelled()) {
                return;
            }
            Document doc_ = request.getDocument();
            if (doc_ == null) {
                CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(file);
                doc_ = CsmUtilities.openDocument(ces);
            }
            final Document doc = doc_;
            
            visit(file.getDeclarations(), file, doc, request, response);
        }
    }
    
    private void visit(Collection<? extends CsmOffsetableDeclaration> decls, final CsmFile file, final Document doc, CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        for (CsmOffsetableDeclaration decl : decls) {
            if (request.isCancelled()) {
                return;
            }
            if (CsmKindUtilities.isClass(decl)) {
                visit(((CsmClass) decl).getMembers(), file, doc, request, response);
            } else if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                visit(((CsmNamespaceDefinition) decl).getDeclarations(), file, doc, request, response);
            } else if (CsmKindUtilities.isFunctionDeclaration(decl)) {
                visit(((CsmFunction) decl).getDefinition(), file, doc, request, response);
            } else if (CsmKindUtilities.isFunctionDefinition(decl)) {
                visit((CsmFunctionDefinition) decl, file, doc, request, response);
            }
        }
    }
    
    private void visit(CsmFunctionDefinition function, final CsmFile file, final Document doc, CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        if (function == null) {
            return;
        }
        if (!file.equals(function.getContainingFile())) {
            return;
        }
        
        for (CsmStatement statement : function.getBody().getStatements()) {
            if (request.isCancelled()) {
                return;
            }
            
            if (CsmKindUtilities.isCompoundStatement(statement)) {
                visit((CsmCompoundStatement) statement, file, doc, request, response);
            } else {
                visit(statement, file, doc, request, response);
            }
        }
    }
    
    private void visit(CsmCompoundStatement compoundStatement, final CsmFile file, final Document doc, CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        for (CsmStatement statement : compoundStatement.getStatements()) {
            if (request.isCancelled()) {
                return;
            }
            
            visit(statement, file, doc, request, response);
        }
    }
    
    private void visit(CsmStatement statement, final CsmFile file, final Document doc, CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        CsmReference reference = CsmReferenceResolver.getDefault().findReference(file, doc, statement.getStartOffset());
        if (reference != null) {
            if (!canBeUnsafe(reference.getText())) {
                return;
            }
            CsmObject referencedObject = reference.getReferencedObject();
            if (CsmKindUtilities.isFunction(referencedObject)) {
                CsmFunction function = (CsmFunction) referencedObject;
                String altText = getAlternativesIfUnsafe(function);
                if (altText != null) {
                    CsmErrorInfo.Severity severity = toSeverity(minimalSeverity());
                    String id = level.getLevel() + category.getName(); // NOI18N
                    String name = "(" + level + ") " + function.getName().toString(); // NOI18N
                    String description = (altText.isEmpty())?getDescription():(getDescription()+NbBundle.getMessage(FunctionUsageAudit.class, "FunctionUsageAudit.alternative", altText)); // NOI18N
                    if (response instanceof AnalyzerResponse) {
                        ((AnalyzerResponse) response).addError(AnalyzerResponse.AnalyzerSeverity.DetectedError, null, file.getFileObject(),
                            new ErrorInfoImpl(SecurityCheckProvider.NAME, getName(), id+"\n"+name+"\n"+description, severity, customType, reference.getStartOffset(), reference.getEndOffset())); // NOI18N
                    } else {
                        response.addError(new ErrorInfoImpl(SecurityCheckProvider.NAME, getName(), description, severity, customType, reference.getStartOffset(), reference.getEndOffset()));
                    }
                }
            }
        }
    }
    
    private boolean canBeUnsafe(CharSequence functionName) {
        for (FunctionsXmlService.RvsdFunction unsafeFunction : category.getFunctions()) {
            if (CharSequenceUtils.contentEquals(functionName, unsafeFunction.getName())) {
                return true;
            }
        }
        return false;
    }

    private String getAlternativesIfUnsafe(CsmFunction function) {
        final CharSequence functionName = function.getName();
        for (FunctionsXmlService.RvsdFunction unsafeFunction : category.getFunctions()) {
            if (CharSequenceUtils.contentEquals(functionName, unsafeFunction.getName())) {
                CsmFile srcFile = function.getContainingFile();
                for (CsmInclude include : CsmFileInfoQuery.getDefault().getIncludeStack(srcFile)) {
                    if (include.getIncludeName().toString().equals(unsafeFunction.getHeader())) {
                        return unsafeFunction.getAlternativesString();
                    }
                }
            }
        }
        return null;
    }
    
}