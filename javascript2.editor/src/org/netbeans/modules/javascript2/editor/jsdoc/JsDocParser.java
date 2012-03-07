/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.jsdoc;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.jsdoc.model.DescriptionElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.JsDocElement;
import org.netbeans.modules.javascript2.editor.jsdoc.model.JsDocElement.Type;
import org.netbeans.modules.javascript2.editor.jsdoc.model.JsDocElementUtils;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Parses jsDoc comment blocks and returns list of these blocks and contained {@code JsDocElement}s.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocParser {

    private static final Logger LOGGER = Logger.getLogger(JsDocParser.class.getName());

    /**
     * Parses given snapshot and returns list of all jsDoc blocks.
     * @param scriptText text to parse
     * @return list of blocks
     */
    public static Map<Integer, JsDocBlock> parse(Snapshot snapshot) {
        Map<Integer, JsDocBlock> blocks = new HashMap<Integer, JsDocBlock>();

        TokenSequence tokenSequence = snapshot.getTokenHierarchy().tokenSequence();
        if (tokenSequence == null || tokenSequence.language() != JsTokenId.language()) {
            return blocks;
        }

        Token<? extends JsTokenId> token;
        while (tokenSequence.moveNext() && (token = tokenSequence.token()) != null) {
            if (token.id() == JsTokenId.DOC_COMMENT) {
                JsDocCommentType commentType = getCommentType(token.text());
                LOGGER.log(Level.FINEST, "JsDocParser:comment block offset=[{0}-{1}],type={2},text={3}", new Object[]{
                    tokenSequence.offset(), tokenSequence.offset() + token.length(), commentType, token.text()});

                OffsetRange offsetRange = new OffsetRange(tokenSequence.offset(), tokenSequence.offset() + token.length());
                if (commentType == JsDocCommentType.DOC_NO_CODE_START
                        || commentType == JsDocCommentType.DOC_NO_CODE_END
                        || commentType == JsDocCommentType.DOC_SHARED_TAG_END) {
                    blocks.put(offsetRange.getEnd(), new JsDocBlock(offsetRange, commentType, Collections.<JsDocElement>emptyList()));
                    continue;
                } else {
                    blocks.put(offsetRange.getEnd(), parseCommentBlock(tokenSequence, offsetRange, commentType == JsDocCommentType.DOC_SHARED_TAG_START));
                    continue;
                }
            }
        }

        return blocks;
    }

    private static void storeCommentPart(List<CommentStrip> storage, String cleanedPart, int offset, StringBuilder link) {
        if (!"".equals(cleanedPart.trim())) { //NOI18N
            if (cleanedPart.startsWith("@link")) { //NOI18N
                link.insert(0, cleanedPart);
            } else if (link.length() > 0) {
                storage.add(new CommentStrip(cleanedPart + link.toString(), offset));
                link.delete(0, link.toString().length());
            } else {
                storage.add(new CommentStrip(cleanedPart + link.toString(), offset));
            }
        }
    }

    /**
     * Gets particular cleaned parts of comment.
     * @param comment whole comment block string
     * @return array of strings with cleaned
     */
    protected static CommentStrip[] getCleanedCommentStrips(String comment, int commentOffset) {
        List<CommentStrip> strips = new ArrayList<CommentStrip>();

        String processedText = comment;
        StringBuilder linkComment = new StringBuilder();
        int indexOfAt;
        while ((indexOfAt = processedText.lastIndexOf("@")) != -1) { //NOI18N
            String cleaned = cleanElementText(processedText.substring(indexOfAt));
            storeCommentPart(strips, cleaned, commentOffset + indexOfAt, linkComment);
            processedText = processedText.substring(0, indexOfAt);
        }

        // process the rest from string
        String cleaned = cleanElementText(processedText);
        storeCommentPart(strips, cleaned, 0, linkComment);
        Collections.reverse(strips);
        return strips.toArray(new CommentStrip[strips.size()]);
    }

    private static boolean isTextToken(Token<? extends JsDocTokenId> token) {
        return (token.id() != JsDocTokenId.ASTERISK && token.id() != JsDocTokenId.COMMENT_SHARED_BEGIN
                && token.id() != JsDocTokenId.COMMENT_START);
    }

    private static JsDocBlock parseCommentBlock(TokenSequence ts, OffsetRange range, boolean sharedTag) {
        List<JsDocElement> jsDocElements = new ArrayList<JsDocElement>();

        TokenSequence ets = ts.embedded();
        Token<? extends JsDocTokenId> token;
        Type type = null;
        boolean afterDescription = false;
        StringBuilder sb = new StringBuilder();
        int offset = ts.offset();
        while (ets.moveNext() && (token = ets.token()) != null) {
            if (!isTextToken(token)) {
                continue;
            }

            if (token.id() == JsDocTokenId.KEYWORD || token.id() == JsDocTokenId.COMMENT_END) {

                if (sb.toString().trim().isEmpty()) {
                    // simple tag
                    if (type != null) {
                        jsDocElements.add(JsDocElementUtils.createElementForType(type, "", -1));
                    }
                } else {
                    // store first description
                    if (!afterDescription) {
                        //TODO - distinguish description and inline comments
                        jsDocElements.add(DescriptionElement.create(Type.CONTEXT_SENSITIVE, sb.toString().trim()));
                    } else {
                        jsDocElements.add(JsDocElementUtils.createElementForType(type, sb.toString().trim(), offset));
                    }
                    sb = new StringBuilder();
                }

                while (ets.moveNext() && ets.token().id() == JsDocTokenId.WHITESPACE) {
                    continue;
                }

                offset = ets.offset();
                if (token.id() != JsDocTokenId.COMMENT_END) {
                    ets.movePrevious();
                }
                afterDescription = true;
                type = Type.fromString(token.text().toString());
            } else {
                sb.append(token.text());
            }
        }

        return new JsDocBlock(
                range,
                sharedTag ? JsDocCommentType.DOC_SHARED_TAG_START : JsDocCommentType.DOC_COMMON,
                jsDocElements);
    }

    private static JsDocCommentType getCommentType(CharSequence text) {
        //TODO - move that into some constatns holder
        if (CharSequenceUtilities.startsWith(text, "/**#")) { //NOI18N
            if (CharSequenceUtilities.textEquals(text, "/**#nocode+*/")) { //NOI18N
                return JsDocCommentType.DOC_NO_CODE_START;
            } else if (CharSequenceUtilities.textEquals(text, "/**#nocode-*/")) {
                return JsDocCommentType.DOC_NO_CODE_END;
            } else if (CharSequenceUtilities.startsWith(text, "/**#@+")) { //NOI18N
                return JsDocCommentType.DOC_SHARED_TAG_START;
            } else if (CharSequenceUtilities.textEquals(text, "/**#@-*/")) { //NOI18N
                return JsDocCommentType.DOC_SHARED_TAG_END;
            }
        }
        return JsDocCommentType.DOC_COMMON;
    }

    protected static String cleanElementText(String comment) {
        String cleaned = comment.trim();

        // clean " /** ", " */", " * " on all rows
        cleaned = cleaned.replaceAll("[\\s&&[^\r\n]]*(\\*)+(\\s)*|[/]$|(\\s)*[/*](\\*)+(\\s)*", ""); //NOI18N

        // replace enters by spaces
        cleaned = cleaned.replaceAll("\r?\n", " ").trim(); //NOI18N

        return cleaned;
    }

    protected static class CommentStrip {

        private final String strip;
        private final int offset;

        public CommentStrip(String strip, int offset) {
            this.strip = strip;
            this.offset = offset;
        }

        public int getOffset() {
            return offset;
        }

        public String getStrip() {
            return strip;
        }
    }
}
