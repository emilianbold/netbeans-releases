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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.editor.javadoc;

import com.sun.javadoc.Doc;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;
import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.openide.util.Exceptions;

/**
 * Maps lexer javadoc tokens to Tags. Not thread safe.<p/>
 * <i>XXX This could be part of java.source's CompilationInfo in future. For now
 * the caching is implemented here using weak references.
 * See {@link DocPositionsManager}</i>
 * 
 * @author Jan Pokorsky
 */
public final class DocPositions {
    
    public static final String UNCLOSED_INLINE_TAG = "@UnclosedInlineTag"; // NOI18N
    public static final String NONAME_BLOCK_TAG = "@NonameTag"; // NOI18N
    
    private Map<Tag, int[]> positions; // inclusive, exclusive
    private List<TagEntry> sortedTags;
    private int blockSectionStart;
    
    // context that should be descarded after resolve()
    private Env env;
    
    // test properties
    static boolean isTestMode = false;
    String tokenSequenceDump;

    public static final DocPositions get(CompilationInfo javac, Doc javadoc, TokenSequence<JavadocTokenId> jdts) {
        return DocPositionsManager.get(javac).get(javadoc, jdts);
    }
    
    private DocPositions(Doc javadoc, TokenSequence<JavadocTokenId> jdts, Document doc) {
        this.env = new Env(jdts, doc, javadoc);
    }

    /**
     * Gets span of the tag.
     * @param tag a tag
     * @return span of the tag. The beginning offset inclusive and the ending offset
     *          exclusive
     */
    public int[] getTagSpan(Tag tag) {
        resolve();
        return positions.get(tag);
    }

    /**
     * Gets a tag on given offset or {@code null}.
     * @param offset offset token from a {@code TokenSequence<JavadocTokenId>}
     * @return the tag or {@code null}
     * @see #UNCLOSED_INLINE_TAG
     * @see #NONAME_BLOCK_TAG
     */
    public Tag getTag(int offset) {
        resolve();
        
        for (TagEntry te : sortedTags) {
            if (offset >= te.span[0] && offset < te.span[1]) {
                return te.tag();
            }
        }

        return null;
    }
    
    public int getBlockSectionStart() {
        resolve();
        return blockSectionStart;
    }
    
    List<? extends Tag> getTags() {
        resolve();
        List<Tag> tags = new ArrayList<Tag>(sortedTags.size());
        for (TagEntry entry : sortedTags) {
            tags.add(entry.tag());
        }
        return tags;
    }

    private void resolve() {
        if (this.positions != null) {
            return;
        }

        this.positions = new WeakHashMap<Tag, int[]>();
        this.sortedTags = new ArrayList<DocPositions.TagEntry>();
        try {
            env.prepare();
            if (env.javadoc == null || env.jdts.isEmpty()) {
                return;
            }

            TokenSequence<JavadocTokenId> jdts = env.jdts;
            Token<JavadocTokenId> token = null;
            Token<JavadocTokenId> prev = null;
            jdts.moveStart();
            boolean isFirstLineWS = true;
            while (jdts.moveNext()) {
                prev = token;
                token = jdts.token();
                if (token.id() == JavadocTokenId.TAG) {
                    if (prev == null || isFirstLineWS) {
                        // first token or first after white spaces in javadoc
                        // block tag
                        isFirstLineWS = false;
                        addBlockTag(token, jdts.offset());
                    } else if (prev.id() == JavadocTokenId.OTHER_TEXT) {
                        if (JavadocCompletionUtils.isLineBreak(prev)/*BR*/) {
                            // block tag
                            closeBlockTag(jdts.offset());
                            addBlockTag(token, jdts.offset());
                        } else if (JavadocCompletionUtils.isInlineTagStart(prev)/*{*/) {
                            // inline tag
                            addInlineTag();
                            token = jdts.token();
                        }
                    }
                } else if (isFirstLineWS && token.id() == JavadocTokenId.OTHER_TEXT && JavadocCompletionUtils.isFirstWhiteSpaceAtFirstLine(token)) {
                    // javadoc content starts with white spaces on first line behind /**
                    // continue
                } else {
                    isFirstLineWS = false;
                }
            }
            if (token != null) {
                closeBlockTag(jdts.offset() + token.length());
            }

            Collections.sort(this.sortedTags);
            if (env.btags.length > 0) {
                int[] span = this.positions.get(env.btags[0]);
                blockSectionStart = span[0];
            } else {
                blockSectionStart = 0;
            }
        } catch (Throwable t) {
            // for debug purposes
            tokenSequenceDump = String.valueOf(env.jdts);
            throw new IllegalStateException(
                    '\'' + env.javadoc.getRawCommentText() + "'\n" + this.toString(), // NOI18N
                    t);
        } finally {
            if (isTestMode) {
                tokenSequenceDump = String.valueOf(env.jdts);
            }
            env = null;
        }
    }

    private void addInlineTag() {
        TokenSequence<JavadocTokenId> jdts = env.jdts;
        Token<JavadocTokenId> token = jdts.token();
        Token<JavadocTokenId> prev;
        int anotherOpenBrace = 0;
        int startOffset = jdts.offset() - 1;
        int endOffset = -1;
        boolean isClosed = false;
        CharSequence tokenName = findTagName(jdts, jdts.index());
        STOP: while (jdts.moveNext()) {
            prev = token;
            token = jdts.token();
            if (token.id() == JavadocTokenId.TAG) {
                if (prev.id() == JavadocTokenId.OTHER_TEXT && JavadocCompletionUtils.isLineBreak(prev)) {
                    // unclosed tag
                    endOffset = jdts.offset();
                    jdts.movePrevious();
                    break;
                }
            } else if (token.id() == JavadocTokenId.OTHER_TEXT) {
                CharSequence text = token.text();
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if (c == '}') {
                        // close tag
                        if (anotherOpenBrace == 0) {
                            // real close brace
                            isClosed = true;
                            endOffset = jdts.offset() + i;
                            break STOP;
                        } else {
                            --anotherOpenBrace;
                        }
                    } else if (c == '{') {
                        // another open
                        ++anotherOpenBrace;
                    }
                }

            }
        }

        if (isClosed) {
            Tag tag = findNextInlineAtTag();
            if (tag != null && tag.name().contentEquals(tokenName)) {
                addTag(tag, new int[] {startOffset, endOffset}, false);
            }
        } else {
            // handle unclosed tag
            endOffset = endOffset < startOffset
                    ? jdts.offset() + token.length() // end of javadoc
                    : endOffset;
            UnclosedTag tag = new UnclosedTag(
                    tokenName.toString(), UNCLOSED_INLINE_TAG, env.javadoc);
            tag.text = JavadocCompletionUtils.getCharSequence(env.doc, startOffset, endOffset);
            addTag(tag, new int[] {startOffset, endOffset}, false);
            
        }
    }
    
    private Tag findNextInlineAtTag() {
        Tag tag = null;
        for (; env.iindex < env.itags.length; env.iindex++) {
            if (env.itags[env.iindex].name().startsWith("@")) { //NOI18N
                tag = env.itags[env.iindex++];
                break;
            }
        }
        return tag;
    }

    private void addBlockTag(Token<JavadocTokenId> token, int offset) {
        assert token.id() == JavadocTokenId.TAG;
        env.btag = env.btags[env.bindex];
        CharSequence tagName = findTagName(env.jdts, env.jdts.index());
        if (env.btag.name().contentEquals(tagName)) {
            ++env.bindex;
            env.iindex = 0;
            env.itags = env.btag.inlineTags();
        } else {
            // XXX this probably cannot occur
            env.btag = new UnclosedTag(token.text().toString(), NONAME_BLOCK_TAG, env.javadoc);
        }
        addTag(env.btag, new int[] {offset, -1}, true);
        env.bscan = true;
    }
    
    /**
     * Finds {@code JavadocTokenId.TAG} name. It handles special cases
     * like {@code @@@, @#, ...}.
     * @param jdts
     * @param index
     * @return
     */
    private static CharSequence findTagName(TokenSequence<JavadocTokenId> jdts, int index) {
        jdts.moveIndex(index);
        if (!jdts.moveNext() || jdts.token().id() != JavadocTokenId.TAG) {
            // index must point to TAG
            throw new IllegalArgumentException(index + ", "+ jdts.toString()); // NOI18N
        }
        StringBuilder name = new StringBuilder(jdts.token().text());
        // try look ahead to resolve @@, @., @#, @{, @}
        STOP: while (jdts.moveNext()) {
            Token<JavadocTokenId> token = jdts.token();
            if (token.id() != JavadocTokenId.OTHER_TEXT) {
                name.append(token.text());
                index = jdts.index();
            } else {
                CharSequence text = token.text();
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if (Character.isWhitespace(c)) {
                        break STOP;
                    } else {
                        // possible case "{}  \n"
                        name.append(c);
                    }
                }
            }
        }
        
        // rewind token sequence to last token containing a name part
        jdts.moveIndex(index);
        jdts.moveNext();
        return name;
    }

    private void closeBlockTag(int offset) {
        if (env.bscan) {
            int[] span = positions.get(env.btag);
            span[1] = offset;
            if (NONAME_BLOCK_TAG == env.btag.kind()) {
                ((UnclosedTag) env.btag).text = JavadocCompletionUtils.getCharSequence(env.doc, span[0], span[1]);
            }
            env.bscan = false;
        }
    }
    
    private void addTag(Tag tag, int[] span, boolean isBlockTag) {
        positions.put(tag, span);
        sortedTags.add(new TagEntry(tag, span, isBlockTag));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        if (positions != null) {
            for (TagEntry entry : sortedTags) {
                sb.append("\n ").append(entry); // NOI18N
            }
        } else {
            sb.append(" Not resolved yet."); // NOI18N
        }
        sb.append("\ntoken sequence dump: " + tokenSequenceDump);
        return sb.toString();
    }
    
    private static final class Env {

        public Env(TokenSequence<JavadocTokenId> jdts, Document doc, Doc javadoc) {
            this.jdts = jdts;
            this.doc = doc;
            this.wjavadoc = new  WeakReference<Doc>(javadoc);
        }
        
        private TokenSequence<JavadocTokenId> jdts;
        private Document doc;
        private final WeakReference<Doc> wjavadoc;
        private Doc javadoc;
        private Tag[] btags;
        // current block tag; may be NONAME_BLOCK_TAG
        private Tag btag;
        private int bindex;
        private boolean bscan = false;
        private Tag[] itags;
        private int iindex;
        
        void prepare() {
            this.javadoc = wjavadoc.get();
            if (this.javadoc != null) {
                this.btags = javadoc.tags();
                this.itags = javadoc.inlineTags();
            }
        }
    }

    private static final class UnclosedTag implements Tag {

        private static final Tag[] EMPTY_TAGS = new Tag[0];
        private final String name;
        private final Reference<Doc> wjavadoc;
        private final String kind;
        private CharSequence text;

        public UnclosedTag(String name, String kind, Doc javadoc) {
            this.name = name;
            this.kind = kind;
            this.wjavadoc = new WeakReference<Doc>(javadoc);
        }

        public String name() {
            return name;
        }

        public Doc holder() {
            return wjavadoc.get();
        }

        public String kind() {
            return kind;
        }

        public String text() {
            return text.toString();
        }

        public Tag[] inlineTags() {
            return EMPTY_TAGS;
        }

        public Tag[] firstSentenceTags() {
            return  EMPTY_TAGS;
        }

        public SourcePosition position() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return name + ":" + kind + ":" + text; // NOI18N
        }

    }
    
    private static final class TagEntry implements Comparable<TagEntry> {
        final Reference<Tag> wtag;
        final UnclosedTag utag;
        final boolean isBlock;
        final int[] span;

        public TagEntry(Tag tag ,int[] span, boolean isBlock) {
            this.wtag = new  WeakReference<Tag>(tag);
            // hard reference UnclosedTag otherwise its weak reference will be GCed soon
            this.utag = (UnclosedTag) (tag instanceof UnclosedTag ? tag : null);
            this.isBlock = isBlock;
            assert span.length == 2;
            this.span = span;
        }
        
        public Tag tag() {
            return wtag.get();
        }

        public int compareTo(TagEntry te) {
            if (te == TagEntry.this) {
                return 0;
            }
            
            // put inline tags before their block tag
            int res = span[1] - te.span[1];
            if (res == 0) {
                res = isBlock? 1: -1;
            }
            return res;
        }

        @Override
        public String toString() {
            return String.format("[%1$d,%2$d], block: %3$b, %4$s", // NOI18N
                    span[0], span[1], isBlock, tag());
        }
        
    }
    
    private final static class DocPositionsManager {
        
        private static final Map<CompilationUnitTree, DocPositionsManager> managers
                = new WeakHashMap<CompilationUnitTree, DocPositions.DocPositionsManager>();
        
        private final Map<Doc, DocPositions> cache = new WeakHashMap<Doc, DocPositions>();
        private final Reference<Document> doc;

        public DocPositionsManager(Document doc) {
            this.doc = new WeakReference<Document>(doc);
        }

        public static DocPositionsManager get(CompilationInfo javac) {
            try {
                Document doc = javac.getDocument();
                CompilationUnitTree cut = javac.getCompilationUnit();
                DocPositionsManager dpm = managers.get(cut);
                if (dpm == null) {
                    dpm = new DocPositionsManager(doc);
                    managers.put(cut, dpm);
                }
                return dpm;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        
        public DocPositions get(Doc javadoc, TokenSequence<JavadocTokenId> jdts) {
            DocPositions dp = cache.get(javadoc);
            if (dp == null) {
                dp = new DocPositions(javadoc, jdts, doc.get());
                cache.put(javadoc, dp);
            }
            return dp;
        }
        
    }
}
