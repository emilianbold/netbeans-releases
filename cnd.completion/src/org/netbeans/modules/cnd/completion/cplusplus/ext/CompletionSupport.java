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

package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CompletionSupport {


    final Reference<Document> docRef;
    // not for external instantiation
    private CompletionSupport(Document doc) {
        docRef = new WeakReference<Document>(doc);
    }

    public static CompletionSupport get(JTextComponent component) {
        return get(component.getDocument());
    }
    
    public static CompletionSupport get(final Document doc) {
        CompletionSupport support = (CompletionSupport) doc.getProperty(CompletionSupport.class);
        if (support == null) {
            // for now accept only documents with known languages
            boolean valid = (CndLexerUtilities.getLanguage(doc) != null);
            if (valid) {
                support = new CompletionSupport(doc);
                doc.putProperty(CompletionSupport.class, support);
//                synchronized (doc) {
//                    support = (CompletionSupport) doc.getProperty(CompletionSupport.class);
//                    if (support == null) {
//                        doc.putProperty(CompletionSupport.class, support = new CompletionSupport(doc));
//                    }
//                }
            }
        }
        return support;
    }
    
    public boolean isIncludeCompletionEnabled(int offset) {
        TokenSequence<CppTokenId> ts = cppTokenSequence(offset, false, true);
        if (ts == null) {
            return false;
        }
        if (ts.token().id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
            @SuppressWarnings("unchecked")
            TokenSequence<CppTokenId> embedded = (TokenSequence<CppTokenId>) ts.embedded();
            if (embedded != null) {
                embedded.moveStart();
                embedded.moveNext();
                // skip the first #
                if (!embedded.moveNext()) {
                    return false;
                }
                if (shiftToNonWhite(embedded, false)) {
                    switch (ts.token().id()) {
                        case PREPROCESSOR_INCLUDE:
                        case PREPROCESSOR_INCLUDE_NEXT:
                            // completion enabled after #include(_next) keywords
                            return (ts.offset() + ts.token().length()) <= offset;
                    }
                }
            }
        }
        return false;
    }  
    
    private TokenSequence<CppTokenId> cppTokenSequence(int offset, boolean needPP, boolean backwardBias) {
        Document doc = this.docRef.get();
        if (doc == null) {
            return null;
        }
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        List<TokenSequence<?>> tsList = hi.embeddedTokenSequences(offset, backwardBias);
        // Go from inner to outer TSes
        for (int i = tsList.size() - 1; i >= 0; i--) {
            TokenSequence<?> ts = tsList.get(i);
            final Language<?> lang = ts.languagePath().innerLanguage();
            if (lang == CppTokenId.languageC() || lang == CppTokenId.languageCpp()
                 || (needPP && lang == CppTokenId.languagePreproc())) {
                @SuppressWarnings("unchecked")
                TokenSequence<CppTokenId> cppInnerTS = (TokenSequence<CppTokenId>) ts;
                return cppInnerTS;
            }
        }
        return null;
    }

    private boolean shiftToNonWhite(TokenSequence<CppTokenId> ts, boolean backward) {
        do {
            switch (ts.token().id()) {
                case WHITESPACE:
                case BLOCK_COMMENT:
                case ESCAPED_LINE:
                case ESCAPED_WHITESPACE:
                    break;
                default:
                    return true;
            }
        } while (backward ? ts.movePrevious() : ts.moveNext());
        return false;
    }
    
}
