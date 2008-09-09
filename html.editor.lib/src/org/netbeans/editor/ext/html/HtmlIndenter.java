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
package org.netbeans.editor.ext.html;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.editor.ext.html.parser.AstNodeVisitor;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxParser;
import org.netbeans.editor.ext.html.parser.SyntaxParserListener;
import org.netbeans.editor.ext.html.parser.SyntaxTree;
import org.netbeans.modules.editor.indent.api.IndentUtils;

/**
 *
 * @author marekfukala
 */
public class HtmlIndenter {

    private final static Logger LOGGER = Logger.getLogger(HtmlIndenter.class.getName());
    private final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public static synchronized void indentEndTag(Document doc, LanguagePath languagePath, final int offset, String endTagName) {
        LOGGER.fine("Offset=" + offset);

        TokenSequence htmlTokenSequence = HTMLSyntaxSupport.getJoinedHtmlSequence(doc, languagePath);
        final String closeTagName = endTagName == null ? getCloseTagName(htmlTokenSequence, offset) : endTagName;

        if (closeTagName == null) {
            LOGGER.info("Cannot find close tag name for offset " + offset);
            return;
        }
        LOGGER.fine("Close tag name: '" + closeTagName + "'");

        SyntaxParser parser = SyntaxParser.get(doc, languagePath);

        AstNode node = SyntaxTree.makeTree(parser.elements());
        AstNode found = AstNodeUtils.findDescendant(node, offset);

        if (found != null && found.type() == AstNode.NodeType.ENDTAG && found.name().equalsIgnoreCase(closeTagName)) {
            LOGGER.fine("Found end tag node with matching name at the offset");
            //the end tag is already in the parser result, unlikely but possible
            AstNode tagNode = found.parent();
            AstNode pair = tagNode.children().get(0); //first node in the parent should be my pair
            if (pair.type() == AstNode.NodeType.OPEN_TAG) {
                LOGGER.fine("Found pair open tag " + pair.path() + " (" + pair.startOffset() + "-" + pair.endOffset());
                changeRowIndent(doc, pair.startOffset(), offset);
                return;
            }
        } else {
            LOGGER.fine("No corresponding end tag found on offset position - searching the whole parse result for unmatched open tags");

            final AstNode[] last = new AstNode[1];
            //parse result doesn't contain the just completed node, most situations
            //search the whole last result
            AstNodeUtils.visitChildren(node, new AstNodeVisitor() {

                public void visit(AstNode node) {
                    if (node.type() == AstNode.NodeType.UNMATCHED_TAG && node.name().equalsIgnoreCase(closeTagName)) {
                        AstNode unmatched = node.children().get(0); //first node is the unmatched tag
                        if (unmatched.type() == AstNode.NodeType.OPEN_TAG) {
                            LOGGER.fine("Found corresponding unmatched opentag " + unmatched.path() + " (" + unmatched.startOffset() + "-" + unmatched.endOffset());
                            //we found unmatched opentag with searched name
                            int unmatchedTagEndOffset = unmatched.endOffset();
                            if (unmatchedTagEndOffset < offset) {
                                //node doesn't overlap the close tag offset
                                if (last[0] == null) {
                                    last[0] = unmatched;
                                } else if (last[0].endOffset() < unmatched.endOffset()) {
                                    last[0] = unmatched;
                                }
                            }
                        }
                    }
                }
            });


            if (last[0] != null) {
                //we found a pair
                changeRowIndent(doc, last[0].startOffset(), offset);
                return;
            }

        }

        LOGGER.fine("Couldn't change proper pair, trying to find it lexically");
        //we weren't able to reindent it is likely that the parse result is not up-to-date
        //lets try to find the pair lexically.
        //it is very likely that the text near of the end tag position contains the open tag if there is any
        //so we do not need a stack to properly filter closed tags
        
        if(htmlTokenSequence == null) {
            //no html content
            return ;
        }
        
        int limit = 50; //limit the backward search to some reasonable range; 50 tokens seems to be enought
        htmlTokenSequence.move(offset);
        //lexer bug hack
        htmlTokenSequence.moveNext();
        htmlTokenSequence.moveNext();

        while (htmlTokenSequence.movePrevious() && limit-- > 0) {
            Token token = htmlTokenSequence.token();
            if (token.id() == HTMLTokenId.TAG_OPEN && token.text().toString().equalsIgnoreCase(closeTagName)) {
                //looks like we found it
                changeRowIndent(doc, htmlTokenSequence.offset(), offset);
                break;
            }
        }


    }

    private static void changeRowIndent(Document doc, int pairOffset, int offset) {
        try {
            int pairIndent = Utilities.getRowIndent((BaseDocument) doc, pairOffset);

            LOGGER.fine("Paired open tag indent level=" + pairIndent);

            String indentString = IndentUtils.createIndentString(doc, pairIndent);
            int rowStart = Utilities.getRowStart((BaseDocument) doc, offset);
            int textStart = Utilities.getFirstNonWhiteFwd((BaseDocument) doc, rowStart);

            doc.remove(rowStart, textStart - rowStart);
            doc.insertString(rowStart, indentString, null);

        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, "Error changing row indent", ex);
        }
    }


    private static String getCloseTagName(TokenSequence sequence, int offset) {
        if(sequence == null) {
            return  null;
        }
        
        sequence.move(offset);

        if (!sequence.moveNext()) {
            throw new IllegalArgumentException("no token on the position!"); //NOI18N
        }

        //try to find the close tag token backward
        Token token = sequence.token();
        //we are the end of a tag - may be close tag or open tag
        while (token.id() != HTMLTokenId.TAG_CLOSE && sequence.movePrevious()) {
            token = sequence.token();
            if (token.id() == HTMLTokenId.TAG_CLOSE) {
                //we found it!
                break;
            }

            if (token.id() == HTMLTokenId.TAG_OPEN_SYMBOL) {
                //out of scope
                break;
            }

        }

        if(token.id() == HTMLTokenId.TAG_CLOSE) {
            return token.text().toString();
        } else {
            //we didn't manage to find the close tag, strange since the
            //code is triggered only if a closing '>' symbol is added 
            //to a close tag.
            
            LOGGER.info("Cannot find the TAG_CLOSE token of the just typed end tag.\nStarting position is " + offset + ". Here is the searched token sequence:\n" + sequence.toString()); //NOI18N
            return null;
            
        }

            
    }
}
