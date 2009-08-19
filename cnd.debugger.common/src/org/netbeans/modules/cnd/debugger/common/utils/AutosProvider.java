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

package org.netbeans.modules.cnd.debugger.common.utils;

import java.lang.String;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmForStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmIfStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmSwitchStatement;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.completion.csm.CsmContext;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author Egor Ushakov
 */
public class AutosProvider {
    public static boolean AUTOS_INCLUDE_MACROS = Boolean.getBoolean("debugger.autos.macros");

    private AutosProvider() {
    }

    public static Set<String> getAutos(final StyledDocument document, int offset) {
        if (document == null) {
            return Collections.emptySet();
        }
        CsmFile csmFile = CsmUtilities.getCsmFile(document, false);
        if (csmFile == null || !csmFile.isParsed()) {
            return Collections.emptySet();
        }
        CsmContext context = CsmOffsetResolver.findContext(csmFile, offset, null);
        CsmScope scope = context.getLastScope();
        if (scope != null) {
            CsmOffsetable previous = null;
            final List<int[]> spans = new ArrayList<int[]>();
            for (CsmScopeElement csmScopeElement : scope.getScopeElements()) {
                if (CsmKindUtilities.isOffsetable(csmScopeElement)) {
                    CsmOffsetable offs = (CsmOffsetable) csmScopeElement;
                    if (offs.getEndOffset() >= offset) {
                        if (previous != null) {
                            spans.add(getInterestedStatementOffsets(previous));
                        }
                        spans.add(getInterestedStatementOffsets(offs));
                        break;
                    } else {
                        previous = offs;
                    }
                }
            }
            final Set<String> autos = new HashSet<String>();
            if (!spans.isEmpty()) {
                CsmFileReferences.getDefault().accept(scope, new CsmFileReferences.Visitor() {
                    public void visit(CsmReferenceContext context) {
                        CsmReference reference = context.getReference();
                        for (int[] span : spans) {
                            if (span[0] <= reference.getStartOffset() && reference.getEndOffset() <= span[1]) {
                                CsmObject referencedObject = reference.getReferencedObject();
                                if (CsmKindUtilities.isVariable(referencedObject) && !filterAuto((CsmVariable)referencedObject)) {
                                    StringBuilder sb = new StringBuilder(reference.getText());
                                    if (context.size() > 1) {
                                        outer: for (int i = context.size()-1; i >= 0; i--) {
                                            CppTokenId token = context.getToken(i);
                                            switch (token) {
                                                case DOT:
                                                case ARROW:
                                                case SCOPE:
                                                    break;
                                                default: break outer;
                                            }
                                            if (i > 0) {
                                                sb.insert(0, token.fixedText());
                                                sb.insert(0, context.getReference(i-1).getText());
                                            }
                                        }
                                    }
                                    autos.add(sb.toString());
                                } else if (AUTOS_INCLUDE_MACROS && CsmKindUtilities.isMacro(referencedObject)) {
                                    String txt = reference.getText().toString();
                                    int[] macroExpansionSpan = CsmMacroExpansion.getMacroExpansionSpan(document, reference.getStartOffset(), false);
                                    if (macroExpansionSpan != null && macroExpansionSpan[0] != macroExpansionSpan[1]) {
                                        try {
                                            txt = document.getText(macroExpansionSpan[0], macroExpansionSpan[1] - macroExpansionSpan[0]);
                                        } catch (BadLocationException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                    }
                                    autos.add(txt);
                                }
                            }
                        }
                    }
                });
            }
            return autos;
        }
        return Collections.emptySet();
    }

    private static boolean filterAuto(CsmScopeElement object) {
        CsmScope scope = object.getScope();
        return CsmKindUtilities.isNamespace(scope) && "std".equals(((CsmNamespace)scope).getQualifiedName().toString()); // NOI18N
    }

    private static int[] getInterestedStatementOffsets(CsmOffsetable offs) {
        if (CsmKindUtilities.isStatement(offs)) {
            switch (((CsmStatement)offs).getKind()) {
                case IF:
                    offs = ((CsmIfStatement)offs).getCondition();
                    break;
                case SWITCH:
                    offs = ((CsmSwitchStatement)offs).getCondition();
                    break;
                case WHILE:
                case DO_WHILE:
                    offs = ((CsmLoopStatement)offs).getCondition();
                    break;
                case FOR:
                    offs = ((CsmForStatement)offs).getCondition();
                    break;
            }
        }
        return new int[]{offs.getStartOffset(), offs.getEndOffset()};
    }
}
