/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.completion.impl.xref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppAbstractTokenProcessor;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;

/**
 *
 * @author Sergey Grinev
 */
public class FileReferencesImpl extends CsmFileReferences  {

    public FileReferencesImpl() {
        /*System.err.println("FileReferencesImpl registered");
        CsmModelAccessor.getModel().addProgressListener(new CsmProgressAdapter() {

            @Override
            public void fileParsingStarted(CsmFile file) {
                System.err.println("remove cache for " + file);
                cache.remove(file);
            }

            public @Override void fileInvalidated(CsmFile file) {
                System.err.println("remove cache for " + file);
                cache.remove(file);
            }
        });*/
    }

//    private final Map<CsmFile, List<CsmReference>> cache = new HashMap<CsmFile, List<CsmReference>>();

    public void accept(CsmScope csmScope, Visitor visitor) {
        accept(csmScope, visitor, CsmReferenceKind.ALL);
    }

    public void accept(CsmScope csmScope, Visitor visitor, Set<CsmReferenceKind> kinds) {
        if (!CsmKindUtilities.isOffsetable(csmScope) && !CsmKindUtilities.isFile(csmScope)){
            return;
        }
        CsmFile csmFile = null;

        int start, end;

        if (CsmKindUtilities.isFile(csmScope)){
            csmFile = (CsmFile) csmScope;
        } else {
            csmFile = ((CsmOffsetable)csmScope).getContainingFile();
        }

        BaseDocument doc = ReferencesSupport.getDocument(csmFile);
        if (doc == null || !csmFile.isValid()) {
            // This rarely can happen:
            // 1. if file was put on reparse and scope we have here is already obsolete
            // TODO: find new scope if API would allow that one day
            // 2. renamed
            // TODO: search by unique name
            // 3. deleted
            return;
        }
        if (CsmKindUtilities.isFile(csmScope)) {
            start = 0;
            end = Math.max(0, doc.getLength() - 1);
        } else {
            start = ((CsmOffsetable)csmScope).getStartOffset();
            end = ((CsmOffsetable)csmScope).getEndOffset();
        }

        for (CsmReference ref : getIdentifierReferences(csmFile, doc, start,end, kinds)) {
            visitor.visit(ref);
        }
    }

    private List<CsmReference> getIdentifierReferences(CsmFile csmFile, BaseDocument doc, int start, int end,
                                                        Set<CsmReferenceKind> kinds) {
        boolean needAfterDereferenceUsages = kinds.contains(CsmReferenceKind.AFTER_DEREFERENCE_USAGE);
        boolean skipPreprocDirectives = !kinds.contains(CsmReferenceKind.IN_PREPROCESSOR_DIRECTIVE);
        Collection<CsmOffsetable> deadBlocks;
        if (!kinds.contains(CsmReferenceKind.IN_DEAD_BLOCK)) {
            deadBlocks = CsmFileInfoQuery.getDefault().getUnusedCodeBlocks(csmFile);
        } else {
            deadBlocks = Collections.<CsmOffsetable>emptyList();
        }
        try {
            doc.atomicLock();
            MyTP tp = new MyTP(csmFile, doc, skipPreprocDirectives, needAfterDereferenceUsages, deadBlocks);
            CndTokenUtilities.processTokens(tp, doc, start, end);
            return tp.references;
        } finally {
            doc.atomicUnlock();
        }
    }

    private static final class MyTP extends CppAbstractTokenProcessor {
        final List<CsmReference> references = new ArrayList<CsmReference>();
        private final Collection<CsmOffsetable> deadBlocks;
        private final boolean needAfterDereferenceUsages;
        private final boolean skipPreprocDirectives;
        private final CsmFile csmFile;
        private final BaseDocument doc;
        private CppTokenId lastID = null;

        MyTP(CsmFile csmFile, BaseDocument doc,
             boolean skipPreprocDirectives, boolean needAfterDereferenceUsages,
             Collection<CsmOffsetable> deadBlocks) {
            this.deadBlocks = deadBlocks;
            this.needAfterDereferenceUsages = needAfterDereferenceUsages;
            this.skipPreprocDirectives = skipPreprocDirectives;
            this.csmFile = csmFile;
            this.doc = doc;
        }

        @Override
        public boolean token(Token<CppTokenId> token, int tokenOffset) {
            boolean skip = false;
            boolean needEmbedding = false;
            switch (token.id()) {
                case PREPROCESSOR_DIRECTIVE:
                    needEmbedding = true;
                    break;
                case IDENTIFIER:
                case PREPROCESSOR_IDENTIFIER:
                {
                    if (!needAfterDereferenceUsages && lastID != null) {
                        switch (lastID) {
                            case DOT:
                            case DOTMBR:
                            case ARROW:
                            case ARROWMBR:
                            case SCOPE:
                                skip = true;
                        }
                    }
                    if (!skip && !deadBlocks.isEmpty()) {
                        skip = isInDeadBlock(tokenOffset, deadBlocks);
                    }
                    if (!skip) {
                        ReferenceImpl ref = ReferencesSupport.createReferenceImpl(csmFile, doc, tokenOffset, token);
                        references.add(ref);
                    }
                }
            }
            lastID = token.id();
            return needEmbedding;
        }
    }
    
    private static final class ReferenceStack {
        private final List<Character> brackets;
        private final List<List<CsmReference>> references;
        public ReferenceStack() {
            brackets = new ArrayList<Character>();
            references = new ArrayList<List<CsmReference>>();
        }
        public void open(char c) {
            if (c != '<' || !getReferences().isEmpty()) {
                brackets.add(c);
                references.add(new ArrayList<CsmReference>(1));
            }
        }
        public void close(char c) {
            if (c == '>') {
                char last = brackets.size() > 0?
                        peek(brackets) : '\u0000';
                if (match(last, c)) {
                    pop(brackets);
                }
                pop(references);
            } else {
                while (brackets.size() > 0) {
                    char last = brackets.get(brackets.size() - 1);
                    pop(brackets);
                    pop(references);
                    if (match(last, c)) break;
                }
            }
        }

        public void semicolon() {
            for (int i = 0; i < brackets.size() ; ++i) {
                if (brackets.get(i) == '<') {
                    while (i < brackets.size()) {
                        pop(brackets);
                        pop(references);
                    }
                    break;
                }
            }
            clearReferences();
        }

        public List<CsmReference> getReferences() {
            return references.size() > 0? 
                peek(references) : 
                Collections.EMPTY_LIST;
        }

        public void clearReferences() {
            if (references.size() > 0) {
                peek(references).clear();
            }
        }

        public void addReference(CsmReference ref) {
            if (references.size() == 0) {
                references.add(new ArrayList<CsmReference>(1));
            }
            peek(references).add(ref);
        }

        private static<T> T peek(List<T> list) {
            if (list.size() > 0) {
                return list.get(list.size() - 1);
            } else {
                return null;
            }
        }
        
        private static<T> T pop(List<T> list) {
            if (list.size() > 0) {
                return list.remove(list.size() - 1);
            } else {
                return null;
            }
        }

        private static boolean match(char l, char r) {
            return l == '(' && r == ')'
                    || l == '[' && r == ']'
                    || l == '{' && r == '}'
                    || l == '<' && r == '>';
        }

    }

    private static boolean isInDeadBlock(int startOffset, Collection<CsmOffsetable> deadBlocks) {
        for (CsmOffsetable csmOffsetable : deadBlocks) {
            if (csmOffsetable.getStartOffset() > startOffset) {
                return false;
            }
            if (csmOffsetable.getEndOffset() > startOffset) {
                return true;
            }
        }
        return false;
    }
}
