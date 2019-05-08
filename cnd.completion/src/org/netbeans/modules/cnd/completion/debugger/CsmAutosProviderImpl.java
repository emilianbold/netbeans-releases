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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.completion.debugger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.text.*;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
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
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.completion.csm.CsmContext;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetResolver;
import org.netbeans.modules.cnd.spi.model.services.AutosProvider;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=AutosProvider.class)
public class CsmAutosProviderImpl implements AutosProvider {
    public static final boolean AUTOS_INCLUDE_MACROS = Boolean.getBoolean("debugger.autos.macros");

    @Override
    public Set<String> getAutos(final StyledDocument document, final int line) {
        CsmCacheManager.enter();
        try {
            if (line < 0 || document == null) {
                return Collections.emptySet();
            }

            CsmFile csmFile = CsmUtilities.getCsmFile(document, false, false);
            if (csmFile == null || !csmFile.isParsed()) {
                return null;
            }

            final Element lineRootElement = NbDocument.findLineRootElement(document);

            final Set<String> autos = new HashSet<String>();

            // add current line autos
            int startOffset = addAutos(csmFile, lineRootElement, line, document, autos);

            // add previous line autos
            if (line > 0) {
                final Element lineElem = lineRootElement.getElement(line-1);
                if (lineElem != null) {
                    final AtomicInteger prevOffset = new AtomicInteger(lineElem.getEndOffset());

                    document.render(new Runnable() {
                        @Override
                        public void run() {
                            TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(document, prevOffset.get(), false, true);
                            if (ts == null) {
                                return;
                            }
                            if (CndTokenUtilities.shiftToNonWhite(ts, true)) {
                                prevOffset.set(ts.offset());
                            }
                        }
                    });
                    int prevLine = NbDocument.findLineNumber(document, prevOffset.get());
                    addAutos(csmFile, lineRootElement, prevLine, document, autos);
                }
            }

            return autos;
        } finally {
            CsmCacheManager.leave();
        }
    }

    private static int addAutos(final CsmFile csmFile,
                                final Element lineRootElement,
                                final int line,
                                final StyledDocument document,
                                final Set<String> autos) {
        final Element lineElem = lineRootElement.getElement(line);
        if (lineElem == null) {
            return -1;
        }
        int lineStartOffset = lineElem.getStartOffset();
        CsmOffsetable statementStart = getStatement(csmFile, lineStartOffset);
        if (statementStart != null) {
            lineStartOffset = statementStart.getStartOffset();
        }

        int lineEndOffset = lineElem.getEndOffset();
        CsmOffsetable statementEnd = getStatement(csmFile, lineEndOffset);
        if (statementEnd != null) {
            lineEndOffset = statementEnd.getEndOffset();
        }

        final int startOffset = lineStartOffset;
        final int endOffset = lineEndOffset;
                
        final Set<Integer> arraysStartOffsets = new HashSet<Integer>();
        final Set<Integer> excludeOffsets = new HashSet<Integer>();
        
        CsmFileReferences.getDefault().accept(csmFile, null, new CsmFileReferences.Visitor() {
            @SuppressWarnings("fallthrough")
            @Override
            public void visit(CsmReferenceContext context) {
                CsmReference reference = context.getReference();
                if (startOffset <= reference.getStartOffset() && reference.getEndOffset() <= endOffset) {
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
                                    case LBRACKET:
                                        if (i > 0) {
                                            CsmReference prevReference = context.getReference(i-1);
                                            if (prevReference != null) {
                                                arraysStartOffsets.add(prevReference.getStartOffset());
                                            }
                                        }
                                    default: break outer;
                                }
                                if (i > 0) {
                                    sb.insert(0, token.fixedText());
                                    CsmReference prevReference = context.getReference(i-1);
                                    if (prevReference == null) {
                                        break outer;
                                    }
                                    sb.insert(0, prevReference.getText());
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
                    } else {
                        excludeOffsets.add(context.getReference().getStartOffset());
                    }
                }
            }

            @Override
            public boolean cancelled() {
                return false;
            }
        });
        
        //Parsing arrays' statements
        if (!arraysStartOffsets.isEmpty()) {
            
            document.render(new Runnable() {

                @Override
                public void run() {
                    for (Integer arrayStartOffset : arraysStartOffsets) {
                        try{
                            String res = matchChar(document, arrayStartOffset, endOffset, '[', ']', excludeOffsets);
                            if(res != null){
                                autos.add(res);
                            }
                        } catch(BadLocationException ex){}
                    }
                }
            });
        }
        
        return lineStartOffset;
    }
    
    private static String matchChar(Document document,
            int offset,
            int limit,
            char origin,
            char matching,
            Set<Integer> excludeOffsets) throws BadLocationException {
        int lookahead =  limit - offset;
        
        // check the character at the right from the caret
        Segment text = new Segment();
        document.getText(offset, lookahead, text);

        int count = 0;
        for(int i = 0 ; i < lookahead; i++) {
            if (origin == text.array[text.offset + i]) {
                count++;
            } else if (matching == text.array[text.offset + i]) {
                if (--count == 0) {
                    for (Integer excOffset : excludeOffsets) {
                        if( offset<=excOffset && excOffset<=(text.offset + i) ){
                            return null;
                        }
                    }
                    return text.subSequence(0, i + 1).toString();
                }
            }
        }
        
        return null;
    }

    private static CsmOffsetable getStatement(CsmFile csmFile, int offset) {
        CsmContext context = CsmOffsetResolver.findContext(csmFile, offset, null);
        CsmScope scope = context.getLastScope();

        for (CsmScopeElement csmScopeElement : scope.getScopeElements()) {
            if (CsmKindUtilities.isOffsetable(csmScopeElement)) {
                CsmOffsetable offs = (CsmOffsetable) csmScopeElement;
                if (offs.getEndOffset() >= offset) {
                    // avoid invalid and compound statements
                    if ((offs.getStartOffset() > offset) || CsmKindUtilities.isCompoundStatement(offs)) {
                        return null;
                    } else {
                        return offs;
                    }
                }
            }
        }
        return null;
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
