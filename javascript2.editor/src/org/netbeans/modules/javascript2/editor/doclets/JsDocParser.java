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
package org.netbeans.modules.javascript2.editor.doclets;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.CharSequences;

/**
 * Parses jsDoc comment blocks and returns list of these blocks and contained {@code JsDocElement}s.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocParser {

    private static final Logger LOGGER = Logger.getLogger(JsDocParser.class.getName());
//    private static final Pattern PATTERN_DOC_COMMENT = Pattern.compile("/\\*(?:.|[\\n\\r])*?\\*/");

    /**
     * Parses given script text and returns list of all jsDoc blocks.
     * @param scriptText text to parse
     * @return list of blocks
     */
    public static List<JsDocBlock> parse(Snapshot snapshot) {
        List<JsDocBlock> blocks = new LinkedList<JsDocBlock>();
        
        List<CommentBlock> commentBlocks = getCommentBlocks(snapshot);
        for (CommentBlock commentBlock : commentBlocks) {
            JsDocCommentType commentType = getCommentType(commentBlock.getContent());
            LOGGER.log(Level.FINE, "JsDocParser:comment block offset=[{0}-{1}],type={2},text={3}", new Object[]{
                commentBlock.getBeginOffset(), commentBlock.getEndOffset(), commentType, commentBlock.getContent()});
            
            // just continue in cases of traditional /* */ blocks
            if (commentType == JsDocCommentType.TRADITIONAL) {
                continue;
            }
        }

        
            

        return blocks;
    }

    private static List<CommentBlock> getCommentBlocks(Snapshot snapshot) {
        List<CommentBlock> blocks = new LinkedList<CommentBlock>();

        TokenSequence tokenSequence = LexUtilities.getJsTokenSequence(snapshot);
        if (tokenSequence == null) {
            return blocks;
        }

        while(tokenSequence.moveNext()) {
            Token<? extends JsTokenId> token = tokenSequence.token();
            if (token.id() == JsTokenId.BLOCK_COMMENT) {
                int startOffset = token.offset(snapshot.getTokenHierarchy());
                int endOffset = startOffset + token.length();
                blocks.add(new CommentBlock(startOffset, endOffset, token.toString()));
            }
        }
        tokenSequence.moveStart();
        return blocks;
    }

    private static JsDocCommentType getCommentType(String commentBlock) {
        assert commentBlock.length() >= 4;
        if (commentBlock.startsWith("/* ")) {
            return JsDocCommentType.TRADITIONAL;
        } else if (commentBlock.startsWith("/**#")) {
            if ("/**#nocode+*/".equals(commentBlock)) {
                return JsDocCommentType.DOC_NO_CODE_START;
            } else if ("/**#nocode-*/".equals(commentBlock)) {
                return JsDocCommentType.DOC_NO_CODE_END;
            } else if (commentBlock.startsWith("/**#@+")) {
                return JsDocCommentType.DOC_SHARED_TAG_START;
            } else if ("/**#@-*/".equals(commentBlock)) {
                return JsDocCommentType.DOC_SHARED_TAG_END;
            }
        }
        return JsDocCommentType.DOC_COMMON;
    }

//    private static JsDocBlock blockForComment(String commentBlock) {
//
//    }
    
    private static class CommentBlock {

        private final int beginOffset;
        private final int endOffset;
        private final String content;

        public CommentBlock(int beginOffset, int endOffset, String content) {
            this.beginOffset = beginOffset;
            this.endOffset = endOffset;
            this.content = content;
        }

        public int getBeginOffset() {
            return beginOffset;
        }

        public String getContent() {
            return content;
        }

        public int getEndOffset() {
            return endOffset;
        }
    }

}
