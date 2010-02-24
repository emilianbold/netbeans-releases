/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.highlight.semantic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.model.tasks.CaretAwareCsmFileTaskFactory;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository.Interrupter;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.highlight.InterrupterImpl;
import org.netbeans.modules.cnd.highlight.semantic.options.SemanticHighlightingOptions;
import org.netbeans.modules.cnd.model.tasks.CsmFileTaskFactory;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.FontColorProvider;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Sergey Grinev
 */
public final class MarkOccurrencesHighlighter extends HighlighterBase {

    private static AttributeSet defaultColors;

    public static OffsetsBag getHighlightsBag(Document doc) {
        if (doc == null) {
            return null;
        }

        OffsetsBag bag = (OffsetsBag) doc.getProperty(MarkOccurrencesHighlighter.class);

        if (bag == null) {
            doc.putProperty(MarkOccurrencesHighlighter.class, bag = new OffsetsBag(doc, false));

            final OffsetsBag bagFin = bag;
            DocumentListener l = new DocumentListener() {

                public void insertUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                }

                public void removeUpdate(DocumentEvent e) {
                    bagFin.removeHighlights(e.getOffset(), e.getOffset(), false);
                }

                public void changedUpdate(DocumentEvent e) {
                }
            };

            doc.addDocumentListener(l);
        }

        return bag;
    }

    private void clean() {
        Document doc = getDocument();
        if (doc != null) {
            getHighlightsBag(doc).clear();
            OccurrencesMarkProvider.get(doc).setOccurrences(Collections.<Mark>emptySet());
        }
    }

    public MarkOccurrencesHighlighter(Document doc) {
        super(doc);
        init(doc);
    }
    public static final Color ES_COLOR = new Color(175, 172, 102);
    private boolean valid = true;
    // PhaseRunner

    public void run(Phase phase) {
        InterrupterImpl interrupter = new InterrupterImpl();
        try {
            addCancelListener(interrupter);
            runImpl(phase, interrupter);
        } finally {
            removeCancelListener(interrupter);
        }
    }

    public void runImpl(Phase phase, Interrupter interruptor) {
        if (!SemanticHighlightingOptions.instance().getEnableMarkOccurrences()) {
            clean();
            valid = false;
            return;
        }

        if (phase == Phase.CLEANUP) {
            clean();
        } else {
            BaseDocument doc = getDocument();

            if (doc == null) {
                clean();
                return;
            }

            CsmFile file = CsmUtilities.getCsmFile(doc, false, false);
            FileObject fo = CsmUtilities.getFileObject(doc);

            if (file == null || fo == null) {
                // this can happen if MO was triggered right before closing project
                clean();
                return;
            }

            int lastPosition = CaretAwareCsmFileTaskFactory.getLastPosition(fo);

            // Check existance of related document
            // And if it exist and check should we use its caret position or not
            Document doc2 = (Document)doc.getProperty(Document.class);
            if(doc2 != null) {
                boolean useOwnCarretPosition = true;
                Object obj = doc.getProperty(CsmFileTaskFactory.USE_OWN_CARET_POSITION);
                if (obj != null) {
                    useOwnCarretPosition = (Boolean) obj;
                }
                if (!useOwnCarretPosition) {
                    FileObject fo2 = CsmUtilities.getFileObject(doc2);
                    if(fo2 != null) {
                        lastPosition = getDocumentOffset(doc, getFileOffset(doc2, CaretAwareCsmFileTaskFactory.getLastPosition(fo2)));
                    }
                }
            }

            if (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) == null) {
                HighlightsSequence hs = getHighlightsBag(doc).getHighlights(0, doc.getLength() - 1);
                while (hs.moveNext()) {
                    if (lastPosition >= hs.getStartOffset() && lastPosition <= hs.getEndOffset()) {
                        // cursor is still in the marked area, so previous result is valid
                        return;
                    }
                }
            }

            Collection<CsmReference> out = getOccurrences(doc, file, lastPosition, interruptor);
            if (out.isEmpty()) {
                if (!SemanticHighlightingOptions.instance().getKeepMarks()) {
                    clean();
                }
            } else {
                OffsetsBag obag = new OffsetsBag(doc);
                obag.clear();

                for (CsmReference csmReference : out) {
                    int usages[][] = CsmMacroExpansion.getUsages(doc, csmReference.getStartOffset());
                    if (usages != null) {
                        for (int i = 0; i < usages.length; i++) {
                            int startOffset = usages[i][0];
                            int endOffset = usages[i][1];
                            if (startOffset < doc.getLength() && endOffset > 0 && startOffset < endOffset) {
                                obag.addHighlight((startOffset > 0) ? startOffset : 0, (endOffset < doc.getLength()) ? endOffset : doc.getLength(), defaultColors);
                            }
                        }
                    } else {
                        int startOffset = getDocumentOffset(doc, csmReference.getStartOffset());
                        int endOffset = getDocumentOffset(doc, csmReference.getEndOffset());
                        if (startOffset < doc.getLength() && endOffset > 0 && startOffset < endOffset) {
                            obag.addHighlight((startOffset > 0) ? startOffset : 0, (endOffset < doc.getLength()) ? endOffset : doc.getLength(), defaultColors);
                        }
                    }
                }

                getHighlightsBag(doc).setHighlights(obag);
                OccurrencesMarkProvider.get(doc).setOccurrences(
                        OccurrencesMarkProvider.createMarks(doc, out, ES_COLOR, NbBundle.getMessage(MarkOccurrencesHighlighter.class, "LBL_ES_TOOLTIP")));
            }
        }
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isHighPriority() {
        return true;
    }

    /* package-local */ static Collection<CsmReference> getOccurrences(AbstractDocument doc, CsmFile file, int position, Interrupter interrupter) {
        position = getFileOffset(doc, position);
        Collection<CsmReference> out = Collections.<CsmReference>emptyList();
        // check if offset is in preprocessor conditional block
        if (isPreprocessorConditionalBlock(doc, position)) {
            return getPreprocReferences(doc, file, position, interrupter);
        }
        if (file != null && file.isParsed()) {
            CsmReference ref = CsmReferenceResolver.getDefault().findReference(file, position);
            if (ref != null && ref.getReferencedObject() != null) {
                out = CsmReferenceRepository.getDefault().getReferences(ref.getReferencedObject(), file, CsmReferenceKind.ALL, interrupter);
            }
        }
        return out;
    }

    private static int getFileOffset(Document doc, int documentOffset) {
        return CsmMacroExpansion.getOffsetInOriginalText(doc, documentOffset);
    }

    private static int getDocumentOffset(Document doc, int fileOffset) {
        return CsmMacroExpansion.getOffsetInExpandedText(doc, fileOffset);
    }

    @Override
    protected void updateFontColors(FontColorProvider provider) {
        defaultColors = provider.getColor(FontColorProvider.Entity.MARK_OCCURENCES);
    }

    private static boolean isPreprocessorConditionalBlock(AbstractDocument doc, int offset) {
        if (doc == null) {
            return false;
        }
        doc.readLock();
        try {
            TokenSequence<CppTokenId> ts = cppTokenSequence(doc, offset, false);
            if (ts != null && ts.language() == CppTokenId.languagePreproc()) {
                int[] span = getPreprocConditionalOffsets(ts);
                if (isIn(span, offset)) {
                    return true;
                }
            }
        } finally {
            doc.readUnlock();
        }
        return false;
    }

    /**
     * returns offset pair (#-start, keyword-end), token stream is positioned on keyword token
     * @param ts
     * @return
     */
    private static int[] getPreprocConditionalOffsets(TokenSequence<CppTokenId> ts) {
        ts.moveStart();
        ts.moveNext(); // move to starting #
        int start = ts.offset();
        while (ts.moveNext()) {
            switch (ts.token().id()) {
                case PREPROCESSOR_START:
                case WHITESPACE:
                case BLOCK_COMMENT:
                case ESCAPED_LINE:
                case ESCAPED_WHITESPACE:
                    // skip them
                    break;
                case PREPROCESSOR_IF:
                case PREPROCESSOR_IFDEF:
                case PREPROCESSOR_IFNDEF:
                case PREPROCESSOR_ELIF:
                case PREPROCESSOR_ELSE:
                case PREPROCESSOR_ENDIF:
                    // found
                    int end = ts.offset() + ts.token().length();
                    return new int[]{start, end};
                default:
                    // not found interested directive
                    return null;
            }
        }
        return null;
    }

    private static TokenSequence<CppTokenId> cppTokenSequence(Document doc, int offset, boolean backwardBias) {
        return CndLexerUtilities.getCppTokenSequence(doc, offset, true, backwardBias);
    }

    private static final class ConditionalBlock {

        private final List<int[]> directivePositions = new ArrayList<int[]>(4);
        private final List<ConditionalBlock> nested = new ArrayList<ConditionalBlock>(4);
        private final ConditionalBlock parent;

        public ConditionalBlock(ConditionalBlock parent) {
            this.parent = parent;
        }

        public void addDirective(int[] span) {
            directivePositions.add(span);
        }

        public ConditionalBlock startNestedBlock(int[] span) {
            ConditionalBlock nestedBlock = new ConditionalBlock(this);
            nestedBlock.addDirective(span);
            nested.add(nestedBlock);
            return nestedBlock;
        }

        public ConditionalBlock getParent() {
            return parent;
        }

        public List<int[]> getDirectives() {
            return Collections.unmodifiableList(directivePositions);
        }
    }

    private static Collection<CsmReference> getPreprocReferences(AbstractDocument doc, CsmFile file, int searchOffset, Interrupter interrupter) {
        TokenSequence<CppTokenId> origPreprocTS = cppTokenSequence(doc, searchOffset, false);
        if (origPreprocTS == null || origPreprocTS.language() != CppTokenId.languagePreproc()) {
            return Collections.<CsmReference>emptyList();
        }
        doc.readLock();
        try {
            TokenHierarchy<AbstractDocument> th = TokenHierarchy.get(doc);
            List<TokenSequence<?>> ppSequences = th.tokenSequenceList(origPreprocTS.languagePath(), 0, doc.getLength());
            ConditionalBlock top = new ConditionalBlock(null);
            ConditionalBlock current = new ConditionalBlock(top);
            ConditionalBlock offsetContainer = null;
            for (TokenSequence<?> ts : ppSequences) {
                if (interrupter != null && interrupter.cancelled()) {
                    return Collections.<CsmReference>emptyList();
                }
                @SuppressWarnings("unchecked")
                TokenSequence<CppTokenId> ppTS = (TokenSequence<CppTokenId>) ts;
                int[] span = getPreprocConditionalOffsets(ppTS);
                if (span != null) {
                    switch (ppTS.token().id()) {
                        case PREPROCESSOR_IF:
                        case PREPROCESSOR_IFDEF:
                        case PREPROCESSOR_IFNDEF:
                            current = current.startNestedBlock(span);
                            break;
                        case PREPROCESSOR_ELIF:
                        case PREPROCESSOR_ELSE:
                        case PREPROCESSOR_ENDIF:
                            current.addDirective(span);
                            break;
                        default:
                            assert false : "unexpected token " + ts.token();
                    }
                    if (offsetContainer == null && isIn(span, searchOffset)) {
                        offsetContainer = current;
                    }
                    if (ppTS.token().id() == CppTokenId.PREPROCESSOR_ENDIF) {
                        // finished block, pop previous
                        current = current.getParent();
                        if (current == null) {
                            // unbalanced
                            return toRefs(offsetContainer);
                        }
                    }
                }
            }
            return toRefs(offsetContainer);
        } finally {
            doc.readUnlock();
        }
    }

    private static boolean isIn(int[] span, int offset) {
        return span != null && span[0] <= offset && offset <= span[1];
    }

    private static Collection<CsmReference> toRefs(ConditionalBlock block) {
        if (block == null || block.getDirectives().isEmpty()) {
            return Collections.<CsmReference>emptyList();
        }
        List<int[]> directives = block.getDirectives();
        Collection<CsmReference> out = new ArrayList<CsmReference>(directives.size());
        for (int[] directive : directives) {
            out.add(new PreprocRef(directive[0], directive[1]));
        }
        return out;
    }

    private static final class PreprocRef implements CsmReference {
        private final int start;
        private final int end;

        public PreprocRef(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public CsmReferenceKind getKind() {
            throw new UnsupportedOperationException("Must not be called"); //NOI18N
        }

        public CsmObject getReferencedObject() {
            throw new UnsupportedOperationException("Must not be called"); //NOI18N
        }

        public CsmObject getOwner() {
            throw new UnsupportedOperationException("Must not be called"); //NOI18N
        }

        public CsmFile getContainingFile() {
            throw new UnsupportedOperationException("Must not be called"); //NOI18N
        }

        public int getStartOffset() {
            return start;
        }

        public int getEndOffset() {
            return end;
        }

        public Position getStartPosition() {
            throw new UnsupportedOperationException("Must not be called"); //NOI18N
        }

        public Position getEndPosition() {
            throw new UnsupportedOperationException("Must not be called"); //NOI18N
        }

        public CharSequence getText() {
            throw new UnsupportedOperationException("Not supported yet."); //NOI18N
        }
        
    }
}
