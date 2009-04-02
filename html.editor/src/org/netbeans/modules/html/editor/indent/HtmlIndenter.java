/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.editor.indent;

import javax.swing.text.BadLocationException;
import org.netbeans.modules.css.formatting.api.support.MarkupAbstractIndenter;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.html.lexer.HtmlTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.html.HtmlSyntaxSupport;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.modules.css.formatting.api.embedding.JoinedTokenSequence;
import org.netbeans.modules.css.formatting.api.support.IndenterContextData;
import org.netbeans.modules.editor.indent.spi.Context;

public class HtmlIndenter extends MarkupAbstractIndenter<HtmlTokenId> {

    private DTD dtd;

    public HtmlIndenter(Context context) {
        super(HtmlTokenId.language(), context);
        dtd = HtmlSyntaxSupport.get(getDocument()).getDTD();
        assert dtd != null : "cannot find any DTD - perhaps NbReaderProvider.setupReaders() was not called?";
    }

    private DTD getDTD() {
        return dtd;
    }

    @Override
    protected boolean isWhiteSpaceToken(Token<HtmlTokenId> token) {
        return token.id() == HtmlTokenId.WS ||
                (token.id() == HtmlTokenId.TEXT && token.text().toString().trim().length() == 0);
    }

    @Override
    protected boolean isOpenTagNameToken(Token<HtmlTokenId> token) {
        return token.id() == HtmlTokenId.TAG_OPEN ||
                (token.id() == HtmlTokenId.DECLARATION && token.text().toString().toUpperCase().startsWith("<!DOCTYPE"));
    }

    @Override
    protected boolean isCloseTagNameToken(Token<HtmlTokenId> token) {
        return token.id() == HtmlTokenId.TAG_CLOSE;
    }

    @Override
    protected boolean isStartTagSymbol(Token<HtmlTokenId> token) {
        return (token.id() == HtmlTokenId.TAG_OPEN_SYMBOL && token.text().toString().equals("<"));
    }

    @Override
    protected boolean isStartTagClosingSymbol(Token<HtmlTokenId> token) {
        return token.id() == HtmlTokenId.TAG_OPEN_SYMBOL &&
                token.text().toString().equals("</");
    }

    @Override
    protected boolean isEndTagSymbol(Token<HtmlTokenId> token) {
        return token.id() == HtmlTokenId.TAG_CLOSE_SYMBOL &&
                token.text().toString().equals(">");
    }

    @Override
    protected boolean isEndTagClosingSymbol(Token<HtmlTokenId> token) {
        return (token.id() == HtmlTokenId.TAG_CLOSE_SYMBOL &&
                token.text().toString().equals("/>")) ||
                (token.id() == HtmlTokenId.DECLARATION && token.text().toString().startsWith(">"));
    }

    @Override
    protected boolean isTagArgumentToken(Token<HtmlTokenId> token) {
        return token.id() == HtmlTokenId.ARGUMENT;
    }

    @Override
    protected boolean isBlockCommentToken(Token<HtmlTokenId> token) {
        return token.id() == HtmlTokenId.BLOCK_COMMENT;
    }

    @Override
    protected boolean isTagContentToken(Token<HtmlTokenId> token) {
        return token.id() == HtmlTokenId.TEXT;
    }

    @Override
    protected boolean isClosingTagOptional(String tagName) {
        Element elem = getDTD().getElement(tagName.toUpperCase());
        if (elem == null) {
            return false;
        }
        return elem.hasOptionalEnd();
    }

    @Override
    protected boolean isOpeningTagOptional(String tagName) {
        Element elem = getDTD().getElement(tagName.toUpperCase());
        if (elem == null) {
            return false;
        }
        return elem.hasOptionalStart();
    }

    @Override
    protected Boolean isEmptyTag(String tagName) {
        Element elem = getDTD().getElement(tagName.toUpperCase());
        if (elem == null) {
            return false;
        }
        return elem.isEmpty();
    }

    private static final String[] TAGS_WITH_UNFORMATTABLE_CONTENT = new String[]{"pre", "textarea"}; //NOI18N
    
    @Override
    protected boolean isTagContentUnformattable(String tagName) {
        for (String t : TAGS_WITH_UNFORMATTABLE_CONTENT) {
            if (t.equalsIgnoreCase(tagName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Set<String> getTagChildren(String tagName) {
        Element elem = getDTD().getElement(tagName.toUpperCase());
        if (elem == null) {
            return null;
        }
        Set<String> set = new TreeSet<String>();
        for (DTD.Element el : (Set<DTD.Element>)elem.getContentModel().getIncludes()) {
            if (el != null) {
                set.add(el.getName());
            }
        }
        for (DTD.Element el : (Set<DTD.Element>)elem.getContentModel().getExcludes()) {
            if (el != null) {
                set.remove(el.getName());
            }
        }
        for (DTD.Element el : (Set<DTD.Element>)elem.getContentModel().getContent().getPossibleElements()) {
            if (el != null) {
                set.add(el.getName());
            }
        }
        if (tagName.equalsIgnoreCase("HTML")) {
            // XXXXXXXXXXXXXXXXX TODO:
            set.add("BODY");
        }
        return set;
    }

    @Override
    protected boolean isPreservedLine(Token<HtmlTokenId> token, IndenterContextData<HtmlTokenId> context) {
        if (isBlockCommentToken(token)) {
            String comment = token.text().toString().trim();
            if (!comment.startsWith("<!--") && !comment.startsWith("-->")) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getPreservedLineInitialIndentation(JoinedTokenSequence<HtmlTokenId> ts)
            throws BadLocationException {
        int index = ts.index();
        boolean found = false;
        do {
            if (isBlockCommentToken(ts.token())) {
                String comment = ts.token().text().toString().trim();
                if (comment.startsWith("<!--")) {
                    found = true;
                    break;
                }
            } else {
                break;
            }
        } while (ts.movePrevious());
        int indent = 0;
        if (found) {
            int lineStart = Utilities.getRowStart(getDocument(), ts.offset());
            // TODO: can comment token start with spaces?? if yes then adjust
            // column to point to first non-whitespace
            int column = ts.offset();
            indent = column - lineStart;
        }
        ts.moveIndex(index);
        ts.moveNext();
        return indent;
    }

    private boolean isOpeningTag(JoinedTokenSequence<HtmlTokenId> ts) {
        int index = ts.index();
        boolean found = false;
        while (ts.moveNext()) {
            if (isEndTagSymbol(ts.currentTokenSequence().token())) {
                found = true;
                break;
            } else if (isEndTagClosingSymbol(ts.currentTokenSequence().token())) {
                break;
            }
        }
        ts.moveIndex(index);
        ts.moveNext();
        return found;
    }

    @Override
    protected boolean isForeignLanguageStartToken(Token<HtmlTokenId> token, JoinedTokenSequence<HtmlTokenId> ts) {
        return isOpenTagNameToken(token) &&
                (token.text().toString().equalsIgnoreCase("style") ||
                 token.text().toString().equalsIgnoreCase("script")) && isOpeningTag(ts);
    }

    @Override
    protected boolean isForeignLanguageEndToken(Token<HtmlTokenId> token, JoinedTokenSequence<HtmlTokenId> ts) {
        return isCloseTagNameToken(token) &&
                (token.text().toString().equalsIgnoreCase("style") ||
                 token.text().toString().equalsIgnoreCase("script"));
    }

}
