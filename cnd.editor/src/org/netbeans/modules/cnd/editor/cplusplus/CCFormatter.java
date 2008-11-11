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
package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;

import org.netbeans.editor.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.Syntax;

import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.api.CodeStyle.Language;

/**
 * CC indentation services are located here.
 *
 * (Copied from from editor/libsrc/org/netbeans/editor/ext/java/JavaFormatter.java)
 */
public class CCFormatter extends ExtFormatter {

    public CCFormatter(Class kitClass) {
        super(kitClass);
    }

    @Override
    protected boolean acceptSyntax(Syntax syntax) {
        return (syntax instanceof CCSyntax);
    }

    @Override
    public boolean expandTabs() {
        if (CKit.class.equals(getKitClass())){
            return CodeStyle.getDefault(Language.C).expandTabToSpaces();
        } else {
            return CodeStyle.getDefault(Language.CPP).expandTabToSpaces();
        }
    }

    @Override
    public int getTabSize() {
        if (CKit.class.equals(getKitClass())){
            return CodeStyle.getDefault(Language.C).getTabSize();
        } else {
            return CodeStyle.getDefault(Language.CPP).getTabSize();
        }
    }

    @Override
    public int[] getReformatBlock(JTextComponent target, String typedText) {
        int[] ret = null;
        BaseDocument doc = Utilities.getDocument(target);
        int dotPos = target.getCaret().getDot();
        if (doc != null) {
            ret = getKeywordBasedReformatBlock(doc, dotPos, typedText);
            if (ret == null) {
                ret = super.getReformatBlock(target, typedText);
            }
        }

        return ret;
    }

    public static int[] getKeywordBasedReformatBlock(BaseDocument doc, int dotPos, String typedText) {
        /* Check whether the user has written the ending 'e'
         * of the first 'else' on the line.
         */
        int[] ret = null;
        if ("e".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "else")) { // NOI18N
                    ret = new int[]{fnw, fnw + 4};
                }
            } catch (BadLocationException e) {
            }

        } else if (":".equals(typedText)) { // NOI18N
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, "case")) { // NOI18N
                    ret = new int[]{fnw, fnw + 4};
                } else if (checkCase(doc, fnw, "default")) { // NOI18N
                    ret = new int[]{fnw, fnw + 7};
                } else if (checkCase(doc, fnw, "public")) { // NOI18N
                    ret = new int[]{fnw, fnw + 6};
                } else if (checkCase(doc, fnw, "protected")) { // NOI18N
                    ret = new int[]{fnw, fnw + 9};
                } else if (checkCase(doc, fnw, "private")) { // NOI18N
                    ret = new int[]{fnw, fnw + 7};
                }
            } catch (BadLocationException e) {
            }
        }
        if (ret == null && typedText != null &&
            typedText.length() == 1 && Character.isLetter(typedText.charAt(0))) {
            try {
                int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                if (checkCase(doc, fnw, typedText+"\n") || // NOI18N
                    dotPos == doc.getLength() && checkCase(doc, fnw, typedText)) { // NOI18N
                    ret = new int[]{fnw, fnw + 1};
                }
            } catch (BadLocationException e) {
            }
        }
        return ret;
    }

    private static boolean checkCase(BaseDocument doc, int fnw, String what) throws BadLocationException {
        return fnw >= 0 && fnw + what.length() <= doc.getLength() && what.equals(doc.getText(fnw, what.length()));
    }

    @Override
    protected void initFormatLayers() {
        if (CKit.class.equals(getKitClass())){
            addFormatLayer(new StripEndWhitespaceLayer(CodeStyle.Language.C));
            addFormatLayer(new CCLayer(CodeStyle.Language.C));
        } else {
            addFormatLayer(new StripEndWhitespaceLayer(CodeStyle.Language.CPP));
            addFormatLayer(new CCLayer(CodeStyle.Language.CPP));
        }
    }

    public FormatSupport createFormatSupport(FormatWriter fw) {
        return new CCFormatSupport(fw);
    }

    public class StripEndWhitespaceLayer extends AbstractFormatLayer {
        private CodeStyle.Language language;

        public StripEndWhitespaceLayer(CodeStyle.Language language) {
            super("cc-strip-whitespace-at-line-end"); // NOI18N
            this.language = language;
        }

        @Override
        protected FormatSupport createFormatSupport(FormatWriter fw) {
            return new CCFormatSupport(fw);
        }

        public void format(FormatWriter fw) {
            CCFormatSupport ccfs = (CCFormatSupport) createFormatSupport(fw);

            FormatTokenPosition pos = ccfs.getFormatStartPosition();
            if (ccfs.isIndentOnly()) {
            // don't do anything
            } else { // remove end-line whitespace
                while (pos.getToken() != null) {
                    pos = ccfs.removeLineEndWhitespace(pos);
                    if (pos.getToken() != null) {
                        pos = ccfs.getNextPosition(pos);
                    }
                }
            }
        }
    }

    public static final String IGNORE_IN_COMMENTS_MODE = "IgnoreInCommentMode"; //NOI18N
    
    public class CCLayer extends AbstractFormatLayer {
        private CodeStyle.Language language;

        public CCLayer(CodeStyle.Language language) {
            super("cc-layer"); // NOI18N
            this.language = language;
        }

        @Override
        protected FormatSupport createFormatSupport(FormatWriter fw) {
            return new CCFormatSupport(fw);
        }

        public void format(FormatWriter fw) {
            try {
                CCFormatSupport ccfs = (CCFormatSupport) createFormatSupport(fw);

                FormatTokenPosition pos = ccfs.getFormatStartPosition();

                if (ccfs.isIndentOnly()) {  // create indentation only
                    Boolean ignoreInCommentsMode = (Boolean) fw.getDocument().getProperty(IGNORE_IN_COMMENTS_MODE);

                    ccfs.indentLine(pos, Boolean.TRUE == ignoreInCommentsMode);

                } else { // regular formatting

                    while (pos != null) {

                        // Indent the current line
                        ccfs.indentLine(pos, false);

                        // Format the line by additional rules
                        formatLine(ccfs, pos);

                        // Goto next line
                        FormatTokenPosition pos2 = ccfs.findLineEnd(pos);
                        if (pos2 == null || pos2.getToken() == null) {
                            break;
                        } // the last line was processed

                        pos = ccfs.getNextPosition(pos2, javax.swing.text.Position.Bias.Forward);
                        if (pos == pos2) {
                            break;
                        } // in case there is no next position
                        if (pos == null || pos.getToken() == null) {
                            break;
                        } // there is nothing after the end of line

                        FormatTokenPosition fnw = ccfs.findLineFirstNonWhitespace(pos);
                        if (fnw != null) {
                            pos = fnw;
                        } else { // no non-whitespace char on the line
                            pos = ccfs.findLineStart(pos);
                        }
                    }
                }
            } catch (IllegalStateException e) {
            }
        }

        protected void formatLine(CCFormatSupport ccfs, FormatTokenPosition pos) {
            if (pos.getToken().getTokenID() == CCTokenContext.WHITESPACE) {
                if (pos.getToken().getImage().indexOf('\n') == 0) {
                    return;
                }
            }
            TokenItem token = ccfs.findLineStart(pos).getToken();
            if (ccfs.isPreprocessorLine(token)) {
                return;
            }
            boolean first = true;
            while (token != null) {
                if (!first && token.getTokenID() == CCTokenContext.WHITESPACE) {
                    if (token.getImage().indexOf('\n') >= 0) {
                        return;
                    }
                }
                first = false;
                /*                if (ccfs.findLineEnd(ccfs.getPosition(token, 0)).getToken() == token) {
                break; // at line end
                }
                 */
                if (token.getTokenContextPath() == ccfs.getTokenContextPath()) {
                    switch (token.getTokenID().getNumericID()) {
                        case CCTokenContext.LBRACE_ID: // '{'
                            token = processBrace(ccfs, token);
                            break;

                        case CCTokenContext.LPAREN_ID:
                            if (ccfs.getFormatSpaceBeforeMethodCallParenthesis()) {
                                TokenItem prevToken = token.getPrevious();
                                if (prevToken != null && prevToken.getTokenID() == CCTokenContext.IDENTIFIER) {
                                    if (ccfs.canInsertToken(token)) {
                                        ccfs.insertToken(token, ccfs.getWhitespaceTokenID(),
                                                ccfs.getWhitespaceTokenContextPath(), " "); // NOI18N
                                    }
                                }
                            } else {
                                // bugfix 9813: remove space before left parenthesis
                                TokenItem prevToken = token.getPrevious();
                                if (prevToken != null && prevToken.getTokenID() == CCTokenContext.WHITESPACE &&
                                        prevToken.getImage().length() == 1) {
                                    TokenItem prevprevToken = prevToken.getPrevious();
                                    if (prevprevToken != null && prevprevToken.getTokenID() == CCTokenContext.IDENTIFIER) {
                                        if (ccfs.canRemoveToken(prevToken)) {
                                            ccfs.removeToken(prevToken);
                                        }
                                    }
                                }
                            }
                            break;

                        case CCTokenContext.COMMA_ID:
                            TokenItem nextToken = token.getNext();
                            if (nextToken != null) {
                                if (ccfs.getFormatSpaceAfterComma()) {
                                    // insert a space if one isn't already there
                                    if (nextToken.getTokenID() != CCTokenContext.WHITESPACE) {
                                        ccfs.insertToken(nextToken,
                                                ccfs.getValidWhitespaceTokenID(),
                                                ccfs.getWhitespaceTokenContextPath(),
                                                " "); //NOI18N
                                    }
                                } else {
                                    if (nextToken.getTokenID() == CCTokenContext.WHITESPACE) {
                                        ccfs.removeToken(nextToken);
                                    }
                                }
                            }
                            break;
                    } // end switch
                }
                token = token.getNext();
            } //end while loop
        } //end formatLine()

        private TokenItem processBrace(CCFormatSupport ccfs, TokenItem token) {
            if (ccfs.isIndentOnly()) {
                return token;
            }
            if (ccfs.getFormatNewlineBeforeBrace() || ccfs.getFormatNewlineBeforeBraceDeclaration()) {
                FormatTokenPosition lbracePos = ccfs.getPosition(token, 0);
                // Look for first important token in backward direction
                FormatTokenPosition imp = ccfs.findImportant(lbracePos,
                        null, true, true); // stop on line start
                if (imp != null && imp.getToken().getTokenContextPath() == ccfs.getTokenContextPath()) {
                    switch (imp.getToken().getTokenID().getNumericID()) {
                        case CCTokenContext.BLOCK_COMMENT_ID:
                        case CCTokenContext.LINE_COMMENT_ID:
                            break; // comments are ignored

                        case CCTokenContext.RBRACKET_ID:
                            break; // array initializtion "ttt [] {...}"

                        case CCTokenContext.COMMA_ID:
                        case CCTokenContext.EQ_ID:
                        case CCTokenContext.LBRACE_ID:
                            // multi array initialization
                            //        static int[][] CONVERT_TABLE= { {3,5},
                            //            {1,2}, {2,3}, ...
                            break;

                        default:
                            // Check whether it isn't a "{ }" case
                            FormatTokenPosition next = ccfs.findImportant(
                                    lbracePos, null, true, false);
                            if (next == null || next.getToken() == null ||
                                    next.getToken().getTokenID() != CCTokenContext.RBRACE) {
                                // Insert new-line
                                if (isAddNewLine(ccfs, token)) {
                                    if (ccfs.canInsertToken(token)) {
                                        ccfs.insertToken(token, ccfs.getValidWhitespaceTokenID(),
                                                ccfs.getValidWhitespaceTokenContextPath(), "\n"); // NOI18N
                                        ccfs.removeLineEndWhitespace(imp);
                                        // bug fix: 10225 - reindent newly created line
                                        ccfs.indentLine(lbracePos, false);
                                        token = imp.getToken();
                                    }
                                } else {
                                    return removeNewLine(token, ccfs);
                                }
                            }
                            break;
                    }// end switch
                } else {
                    if (!isAddNewLine(ccfs, token)) {
                        return removeNewLine(token, ccfs);
                    }
                }
            } else {
                return removeNewLine(token, ccfs);
            }
            return token;
        }

        private boolean isAddNewLine(CCFormatSupport ccfs, TokenItem token) {
            TokenItem prev = ccfs.findImportantToken(token, null, true, true);
            if (prev == null) {
                return true;
            }
            switch (prev.getTokenID().getNumericID()) {
                case CCTokenContext.TRY_ID: // 'thy {'
                case CCTokenContext.ELSE_ID: // 'else {'
                case CCTokenContext.DO_ID:
                    return ccfs.getFormatNewlineBeforeBrace();
                case CCTokenContext.SEMICOLON_ID:
                    return true;
            }
            if (prev.getTokenID().getNumericID() == CCTokenContext.RPAREN_ID) {
                TokenItem imp = ccfs.findStatementStart(prev, false);
                if (imp != null) {
                    switch (imp.getTokenID().getNumericID()) {
                        case CCTokenContext.CATCH_ID: // 'catch (...) {'
                        case CCTokenContext.IF_ID:
                        case CCTokenContext.FOR_ID:
                        case CCTokenContext.WHILE_ID:
                            return ccfs.getFormatNewlineBeforeBrace();
                        case CCTokenContext.SWITCH_ID:
                            return ccfs.getFormatNewlineBeforeBraceSwitch();
                    }
                }
            }
            return ccfs.getFormatNewlineBeforeBraceDeclaration();
        }

        private TokenItem removeNewLine(TokenItem token, CCFormatSupport ccfs) {
            FormatTokenPosition lbracePos = ccfs.getPosition(token, 0);

            // Check that nothing exists before "{"
            if (ccfs.findNonWhitespace(lbracePos, null, true, true) != null) {
                return token;
            }
            // Check that nothing exists after "{", but ignore comments
            if (ccfs.getNextPosition(lbracePos) != null) {
                if (ccfs.findImportant(ccfs.getNextPosition(lbracePos), null, true, false) != null) {
                    return token;
                }
            }

            // check that on previous line is some stmt
            FormatTokenPosition ftp = ccfs.findLineStart(lbracePos); // find start of current line
            FormatTokenPosition endOfPreviousLine = ccfs.getPreviousPosition(ftp); // go one position back - means previous line
            if (endOfPreviousLine == null || endOfPreviousLine.getToken().getTokenID() != CCTokenContext.WHITESPACE) {
                return token;
            }
            ftp = ccfs.findLineStart(endOfPreviousLine); // find start of the previous line - now we have limit position
            ftp = ccfs.findImportant(lbracePos, ftp, false, true); // find something important till the limit
            if (ftp == null) {
                return token;
            }

            // check that previous line does not end with "{" or line comment
            ftp = ccfs.findNonWhitespace(endOfPreviousLine, null, true, true);
            if (ftp.getToken().getTokenID() == CCTokenContext.LINE_COMMENT || ftp.getToken().getTokenID() == CCTokenContext.LBRACE || ccfs.isPreprocessorLine(ftp.getToken())) {
                return token;
            }

            // now move the "{" to the end of previous line
            boolean remove = true;
            while (remove) {
                if (token.getPrevious() == endOfPreviousLine.getToken()) {
                    remove = false;
                }
                if (ccfs.canRemoveToken(token.getPrevious())) {
                    ccfs.removeToken(token.getPrevious());
                } else {
                    break;
                } // should never get here!
            }
            // insert one space before "{"
            if (ccfs.canInsertToken(token)) {
                ccfs.insertSpaces(token, 1);
            }
            return token;
        }
    } // end class CCLayer
}
