/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.text.indent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence;
import org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter;
import org.netbeans.modules.xml.text.folding.TokenElement;
import org.netbeans.modules.xml.text.folding.TokenElement.TokenType;
import org.netbeans.modules.xml.text.folding.XmlFoldManager;

/**
 * New XML formatter based on Lexer APIs.
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class XMLLexerFormatter extends TagBasedLexerFormatter {

    private static final String TAG_OPENING_PREFIX = "<"; //NOI18N
    private static final String TAG_CLOSING_PREFIX = "</"; //NOI18N

    private final LanguagePath languagePath;
    private int spacesPerTab = 4;

    public XMLLexerFormatter(LanguagePath languagePath) {
        this.languagePath = languagePath;
    }

    @Override
    protected boolean isOpeningTag(JoinedTokenSequence jts, int tagTokenOffset) {
        Token token = getTokenAtOffset(jts, tagTokenOffset);
        return token != null
                && token.id() == XMLTokenId.TAG
                && token.text().toString().startsWith(TAG_OPENING_PREFIX)
                && !token.text().toString().startsWith(TAG_CLOSING_PREFIX);
    }

    @Override
    protected boolean isClosingTag(JoinedTokenSequence jts, int tagTokenOffset) {
        Token token = getTokenAtOffset(jts, tagTokenOffset);
        return token != null
                && token.id() == XMLTokenId.TAG
                && token.text().toString().startsWith(TAG_CLOSING_PREFIX);
    }

    @Override
    protected boolean areTagNamesEqual(String tagName1, String tagName2) {
        return tagName1.equalsIgnoreCase(tagName2);
    }

    @Override
    protected boolean isClosingTagRequired(BaseDocument doc, String tagName) {
        return true;
    }

    @Override
    protected boolean isUnformattableToken(JoinedTokenSequence jts, int tagTokenOffset) {
        Token token = getTokenAtOffset(jts, tagTokenOffset);

        if (token.id() == XMLTokenId.BLOCK_COMMENT || token.id() == XMLTokenId.CDATA_SECTION) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean isUnformattableTag(String tag) {
        return false;
    }

    @Override
    protected boolean isTopLevelLanguage(BaseDocument doc) {
        return true;
    }

    protected LanguagePath supportedLanguagePath() {
        return languagePath;
    }

    @Override
    protected String extractTagName(JoinedTokenSequence jts, int tagTokenOffset) {
        Token token = getTokenAtOffset(jts, tagTokenOffset);
        String tagImage = token.text().toString();
        int startIndex = -1;

        if (isOpeningTag(jts, tagTokenOffset)) {
            startIndex = TAG_OPENING_PREFIX.length();
        } else if (isClosingTag(jts, tagTokenOffset)) {
            startIndex = TAG_CLOSING_PREFIX.length();
        }

        if (startIndex >= 0) {
            String tagName = tagImage.substring(startIndex);
            return tagName;
        }
        return null;
    }

    @Override
    protected int getTagEndingAtPosition(JoinedTokenSequence jts,
            int position) throws BadLocationException {
        if (position >= 0) {
            int originalOffset = jts.offset();
            jts.move(position);
            jts.moveNext();
            Token token = jts.token();

            if (token.id() == XMLTokenId.TAG &&
                    !token.text().toString().endsWith("/>")) { //NOI18N

                while (jts.movePrevious()) {
                    int tokenOffset = jts.offset();

                    if (isOpeningTag(jts, tokenOffset) || isClosingTag(jts, tokenOffset)) {
                        int r = jts.offset();
                        jts.move(originalOffset);
                        jts.moveNext();
                        return r;
                    }
                }
            }
            jts.move(originalOffset);
            jts.moveNext();
        }
        return -1;
    }

    @Override
    protected int getTagEndOffset(JoinedTokenSequence jts, int tagStartOffset) {
        int originalOffset = jts.offset();
        jts.move(tagStartOffset);
        jts.moveNext();
        jts.moveNext();
        boolean thereAreMoreTokens = true;

        while (thereAreMoreTokens && jts.token().id() != XMLTokenId.TAG) {
            thereAreMoreTokens &= jts.moveNext();
        }

        int r = jts.offset() + jts.token().length();
        jts.move(originalOffset);
        jts.moveNext();
        return thereAreMoreTokens ? r : -1;
    }

    @Override
    protected int getOpeningSymbolOffset(JoinedTokenSequence jts, int tagTokenOffset) {
        int originalOffset = jts.offset();
        jts.move(tagTokenOffset);
        boolean thereAreMoreTokens = true;

        do {
            thereAreMoreTokens = jts.movePrevious();
        } while (thereAreMoreTokens && jts.token().id() != XMLTokenId.TAG);

        if (thereAreMoreTokens) {
            int r = jts.offset();
            jts.move(originalOffset);
            jts.moveNext();
            return r;
        }
        jts.move(originalOffset);
        jts.moveNext();
        return -1;
    }

// # 170343
    @Override
    public void reformat(Context context, final int startOffset, final int endOffset)
            throws BadLocationException {
        final BaseDocument doc = (BaseDocument) context.document();
        doc.render(new Runnable() {

            public void run() {
                doReformat(doc, startOffset, endOffset);
            }
        });
    }

    public BaseDocument doReformat(BaseDocument doc, int startOffset, int endOffset) {
        spacesPerTab = IndentUtils.indentLevelSize(doc);
        try {
            List<TokenIndent> tags = getTags(doc, startOffset, endOffset);
            for (int i = tags.size() - 1; i >= 0; i--) {
                if (tags.get(i).isPreserveIndent()) {
                    continue;
                }
                TokenElement tag = tags.get(i).getToken();

                int so = tag.getStartOffset();
                int lineOffset = Utilities.getLineOffset(doc, so);
                String tagName = tag.getName();
                if (tagName.startsWith("</")) {
                    /* For Eg:-
                     *
                     * <abc><xyz>123</xyz> //skip format for 'xyz'
                     *
                     * <xyz><abc>123</abc></xyz> //do format for 'xyz' and skip for 'abc'
                     *
                     */
                    Element docElem = doc.getDefaultRootElement().getElement(lineOffset);
                    String lineStr = doc.getText(docElem.getStartOffset(),
                            docElem.getEndOffset() - docElem.getStartOffset());
                    int ndx = lineStr.lastIndexOf(tagName);
                    if (ndx != -1) {
                        lineStr = lineStr.substring(0, ndx);
                        int ndx2 = lineStr.lastIndexOf("<" + tagName.substring(2));
                        if (ndx2 == -1) {//no start found in this line, so indent this tag
                            changePrettyText(doc, tag, so);
                        } else {
                            lineStr = lineStr.substring(ndx2 + 1);
                            ndx2 = lineStr.indexOf("<");
                            if (ndx2 != -1) {//indent this tag if it contains another tag
                                changePrettyText(doc, tag, so);
                            }
                        }
                    }
                } else {
                    changePrettyText(doc, tag, so);
                }
            }
        } catch (BadLocationException ble) {
            //ignore exception
        } catch (IOException iox) {
            //ignore exception
        } finally {
            //((AbstractDocument)doc).readUnlock();
        }
        return doc;
    }

    private void changePrettyText(BaseDocument doc, TokenElement tag, int so) throws BadLocationException {
        //i expected the call IndentUtils.createIndentString() to return
        //the correct string for the indent level, but it doesn't.
        //so this is just a workaround.
        String newIndentText = IndentUtils.createIndentString(doc,
                tag.getIndentLevel()*spacesPerTab);
        //String newIndentText = formatter.getIndentString(doc, tag.getIndentLevel());
        int previousEndOffset = Utilities.getFirstNonWhiteBwd(doc, so) + 1;
        String temp = doc.getText(previousEndOffset, so - previousEndOffset);
        if(temp.indexOf("\n") != -1){
            int i = Utilities.getRowFirstNonWhite(doc, so);
            int rowStart = Utilities.getRowStart(doc, so);
            doc.insertString(so, newIndentText, null);
            doc.remove(rowStart, i - rowStart);
        }
        else {
            doc.insertString(so, "\n" + newIndentText, null);
        }
    }

    /**
     * This is the core of the formatting algorithm.  It was originally derived
     * from {@link XmlFoldManager#createFolds(org.netbeans.spi.editor.fold.FoldHierarchyTransaction)}.
     * Like that method, this method parses the document using lexer.  Rather
     * than creating folds though, this method reformats by manipulating the
     * whitespace tokens.  To do this it keeps track of the nesting level of the
     * XML and the use of the xml:space attribute.  Together they are used to
     * calculate how much each token should be indented by.
     */
    private List<TokenIndent> getTags(BaseDocument basedoc, int startOffset, int endOffset)
            throws BadLocationException, IOException {
        List<TokenIndent> tags = new ArrayList<TokenIndent>();

        // List to keep track of whether whitespace is to be preserved at each
        // level of nesting.  By default whitesapce is not preserved.
        LinkedList<Boolean> preserveNesting_outdent = new LinkedList<Boolean>();
        preserveNesting_outdent.add(Boolean.FALSE);

        // flag that is true if whitespace is currently
        // to not be changed.  That is, xml:space
        // was last set to "preserve".
        boolean preserveWhitespace = false;

        // flag to indicate if the current
        // argument is xml:space
        boolean settingSpaceValue = false;

        int indentLevel = -1;
        basedoc.readLock();
        try {
            TokenHierarchy tokenHierarchy = TokenHierarchy.get(basedoc);
            TokenSequence<XMLTokenId> tokenSequence = tokenHierarchy.tokenSequence();
            org.netbeans.api.lexer.Token<XMLTokenId> token = tokenSequence.token();
            // Add the text token, if any, before xml declaration to document node
            if (token != null && token.id() == XMLTokenId.TEXT) {
                if (tokenSequence.moveNext()) {
                    token = tokenSequence.token();
                }
            }
            int currentTokensSize = 0;
            Stack<TokenElement> stack = new Stack<TokenElement>();
            while (tokenSequence.moveNext()) {
                token = tokenSequence.token();
                XMLTokenId tokenId = token.id();
                String image = token.text().toString();
                if (tokenSequence.offset() > endOffset) {
                    break;
                }
                boolean tokenInSelectionRange = tokenSequence.offset() >= startOffset;
                TokenType tokenType = TokenType.TOKEN_WHITESPACE;
                switch (tokenId) {
                    case TAG: { // Tag is encountered and the required level of indenting determined.
                                // The tokens are only assessed if they are in the selection
                                // range, which is the whole document if no text is selected.
                        int len = image.length();
                        if (image.charAt(len - 1) == '>') {// '/>'
                            if (len == 2) {
                                if (!preserveWhitespace) {
                                    --indentLevel;
                                }
                                if (!stack.empty()) {
                                    stack.pop();
                                }
                            }
                        } else {
                            tokenType = TokenType.TOKEN_ELEMENT_START_TAG;
                            if (image.startsWith("</")) {
                                int begin = currentTokensSize;
                                int end = begin + image.length();

                                boolean preservingWhitespaceOnClose = preserveNesting_outdent.removeLast();
                                if (indentLevel < 0) {
                                    indentLevel = 0;
                                }
                                if (tokenInSelectionRange) {
                                    TokenElement tag = new TokenElement(tokenType, image, begin, end, indentLevel);
                                    tags.add(new TokenIndent(tag, preservingWhitespaceOnClose));
                                }
                                if ( !preserveNesting_outdent.isEmpty() && !preserveNesting_outdent.getLast()) {
                                    --indentLevel;
                                }
                            } else {
                                String tagName = image.substring(1);
                                int begin = currentTokensSize;
                                int end = begin + image.length();
                                preserveWhitespace = !preserveNesting_outdent.isEmpty() && preserveNesting_outdent.getLast();
                                if (!preserveWhitespace) {
                                    ++indentLevel;
                                }
                                preserveNesting_outdent.add(preserveWhitespace);
                                if (tokenInSelectionRange) {
                                    TokenElement tag = new TokenElement(tokenType, tagName, begin, end, indentLevel);
                                    tags.add(new TokenIndent(tag, preserveWhitespace));
                                }
                            }
                            settingSpaceValue = false;
                        }
                        break;
                    }
                    case BLOCK_COMMENT:
                    case CDATA_SECTION:
                    case PI_START:
                    case PI_TARGET:
                    case PI_CONTENT:
                    case PI_END:
                    case TEXT:
                    case CHARACTER:
                    case WS:
                    case OPERATOR:
                    case DECLARATION:
                        break; //Do nothing for above case's
                    case ARGUMENT: //attribute of an element
                        settingSpaceValue = token.text().equals("xml:space");
                        break;
                    case VALUE:
                        if (settingSpaceValue) {
                            if (token.text().equals("\"preserve\"")) {
                                preserveWhitespace = true;
                            } else if (token.text().equals("\"default\"")) {
                                preserveWhitespace = false;
                            }
                            preserveNesting_outdent.set(preserveNesting_outdent.size() - 1, preserveWhitespace);
                            settingSpaceValue = false;
                        }
                        break;

                    case ERROR:
                    case EOL:
                    default:
                        throw new IOException("Invalid token found in document: "
                                + "Please use the text editor to resolve the issues...");
                }
                currentTokensSize += image.length();
            }
        } finally {
            basedoc.readUnlock();
        }
        return tags;
    }

    public boolean isOneLiner(int start, int end, BaseDocument doc) {
        try {
            return Utilities.getLineOffset(doc, start) ==
                    Utilities.getLineOffset(doc, end);
        } catch (BadLocationException ex) {
            //Exceptions.printStackTrace(ex);
            return false;
        }
    }

    /**
     * The formatter needs to keep track of when it can remove whitespace and
     * when it must preserve whitespace as defined by the xml:space attribute.
     * This class associates a flag that defines whether whitespace is to be
     * preserved with the other token data that is used in the code folding
     * algorithm.
     */
    private class TokenIndent {

        private TokenElement token;
        private boolean preserveIndent;

        public TokenIndent(TokenElement token, boolean preserveIndent) {
            this.token = token;
            this.preserveIndent = preserveIndent;
        }

        public TokenElement getToken() {
            return token;
        }

        public boolean isPreserveIndent() {
            return preserveIndent;
        }

        public void setPreserveIndent(boolean preserveIndent) {
            this.preserveIndent = preserveIndent;
        }

        @Override
        public String toString() {
            return "TokenIndent: name=" + token.getName() + " preserveIndent=" + preserveIndent;
        }
    }
}
