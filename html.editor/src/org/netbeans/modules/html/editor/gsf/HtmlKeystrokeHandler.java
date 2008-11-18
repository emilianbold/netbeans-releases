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

package org.netbeans.modules.html.editor.gsf;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.html.HTMLSyntaxSupport;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.KeystrokeHandler;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.html.editor.gsf.HtmlParserResult;

/**
 *
 * @author marekfukala
 */
public class HtmlKeystrokeHandler implements KeystrokeHandler {

    //not used. HTMLKit coveres this functionality
    public boolean beforeCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    //not used. HTMLKit coveres this functionality
    public boolean afterCharInserted(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    //not used. HTMLKit coveres this functionality
    public boolean charBackspaced(Document doc, int caretOffset, JTextComponent target, char ch) throws BadLocationException {
        return false;
    }

    //not used. HTMLKit coveres this functionality
    public int beforeBreak(Document doc, int caretOffset, JTextComponent target) throws BadLocationException {
        return  -1;
    }

    //not used. HTMLBracesMatching coveres this functionality
    public OffsetRange findMatching(Document doc, int caretOffset) {
        return OffsetRange.NONE;
    }

    public List<OffsetRange> findLogicalRanges(CompilationInfo info, int caretOffset) {
        ArrayList<OffsetRange> ranges = new ArrayList<OffsetRange>(2);

        //include the text under the carat to the ranges.
        //I need to do it this lexical way since we do not
        //add the text nodes into the ast due to performance reasons
        TokenSequence ts = HTMLSyntaxSupport.getJoinedHtmlSequence(info.getDocument());
        ts.move(caretOffset);
        if(ts.moveNext() || ts.movePrevious()) {
            Token token = ts.token();

            if(token.id() == HTMLTokenId.TEXT) {
                int from = ts.offset();
                int to = from + token.text().length();

                //first add the range of trimmed text, then the whole text range
                CharSequence text = token.text();
                int trimmed_from = from;
                for(int i = 0; i < text.length(); i++) {
                    char ch = text.charAt(i);
                    if(!Character.isWhitespace(ch)) {
                        trimmed_from = trimmed_from + i;
                        break;
                    }
                }
                int trimmed_to = to;
                for(int i = text.length() - 1; i >= 0 ; i--) {
                    char ch = text.charAt(i);
                    if(!Character.isWhitespace(ch)) {
                        trimmed_to = from + i + 1;
                        break;
                    }
                }

                if(trimmed_from != from || trimmed_to != to) {
                    ranges.add(new OffsetRange(trimmed_from, trimmed_to));
                }

                ranges.add(new OffsetRange(ts.offset(), ts.offset() + token.length()));
            }
        }

        HtmlParserResult result = (HtmlParserResult)info.getEmbeddedResult("text/html", caretOffset);
        AstNode root = result.root();

        if(root != null) {
            //find leaf at the position
            AstNode node = AstNodeUtils.findDescendant(root, astOffset(result.getTranslatedSource(), caretOffset));
            if(node != null) {
                //go through the tree and add all parents with, eliminate duplicate nodes
                do {
                    int from = node.startOffset();
                    int to = node.endOffset();

                    OffsetRange last = ranges.isEmpty() ? null : ranges.get(ranges.size() - 1);
                    //skip duplicated ranges
                    if(last == null || !(last.getStart() == from && last.getEnd() == to)) {
                        ranges.add(new OffsetRange(from, to));
                    }
                } while ((node = node.parent()) != null);
            }
        }

        //the bottom most element represents the whole parse tree, replace it by the document
        //range since they doesn't need to be the same
        if(!ranges.isEmpty()) {
            ranges.set(ranges.size() - 1, new OffsetRange(0, info.getDocument().getLength()));
        }

        return ranges;
    }

     private int astOffset(TranslatedSource source, int offset) {
        return source == null ? offset : source.getAstOffset(offset);
    }

    //TODO implement
    public int getNextWordOffset(Document doc, int caretOffset, boolean reverse) {
        return -1;
    }

}
