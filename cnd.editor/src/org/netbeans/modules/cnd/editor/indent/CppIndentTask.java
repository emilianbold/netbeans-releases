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
package org.netbeans.modules.cnd.editor.indent;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.spi.editor.CsmDocGeneratorProvider;
import org.netbeans.modules.cnd.spi.editor.CsmDocGeneratorProvider.Function;
import org.netbeans.modules.cnd.spi.editor.CsmDocGeneratorProvider.Parameter;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class CppIndentTask extends IndentSupport implements IndentTask {
    private Context context;
    private Document doc;

    public CppIndentTask(Context context) {
        this.context = context;
        doc = context.document();
    }

    @Override
    public void reindent() throws BadLocationException {
        if (codeStyle == null) {
            codeStyle = CodeStyle.getDefault(doc);
        }
        int caretOffset = context.caretOffset();
        int lineOffset = context.lineStartOffset(caretOffset);
        ts = CndLexerUtilities.getCppTokenSequence(doc, lineOffset, false, false);
        if (ts == null) {
            return;
        }
        int indent = indentLine(new TokenItem(ts, true), caretOffset);
        if (indent >= 0) {
            context.modifyIndent(lineOffset, indent);
        }
    }

    @Override
    public ExtraLock indentLock() {
        return null;
    }

    private TokenItem moveToFirstLineImportantToken(TokenItem token){
        TokenItem t = token;
        while(true) {
            if (t == null) {
                return token;
            }
            switch (t.getTokenID()){
                case NEW_LINE:
                case PREPROCESSOR_DIRECTIVE:
                    return token;
                case WHITESPACE:
                    break;
                default:
                    return t;
            }
            token = t;
            t = token.getNext();
        }
    }

    private int indentLine(TokenItem token, int caretOffset) {
        if (isPreprocessorLine(token)){
            // leave untouched for now, (bug#22570)
            return -1;
        }
        //if ((dotPos >= 1 && DocumentUtilities.getText(doc).charAt(dotPos-1) != '\\')
        //    || (dotPos >= 2 && DocumentUtilities.getText(doc).charAt(dotPos-2) == '\\')) {
        if (token.getTokenID() == CppTokenId.STRING_LITERAL || token.getTokenID() == CppTokenId.CHAR_LITERAL) {
            int start = token.getTokenSequence().offset();
            Token<CppTokenId> tok = token.getTokenSequence().token();
            if (start < caretOffset && caretOffset < start + tok.length()) {
                // if insede literal
                if (caretOffset >= start + 2 && tok.text().charAt(caretOffset - start - 2) == '\\') {
                    if (!(caretOffset > start + 2 && tok.text().charAt(caretOffset - start - 3) == '\\')) {
                        return -1;
                    }
                }
            }
        }
        if (token.getTokenID() == CppTokenId.NEW_LINE) {
            TokenItem prev = token.getPrevious();
            if (prev != null && prev.getTokenID() == CppTokenId.ESCAPED_LINE) {
                return -1;
            }
        }

        if (isMultiLineComment(token)) {
            if (caretOffset == token.getTokenSequence().offset()) {
                return findIndent(token);
            }
            // Indent the inner lines of the multi-line comment by one
            if (!getFormatLeadingStarInComment()) {
                return getTokenColumn(token) + 1;
            } else {
                int indent = getTokenColumn(token) + 1;
                try {
                    if (caretOffset - token.getTokenSequence().offset() == 4
                            && doc.getLength() > token.getTokenSequence().offset() + 6
                            && "/**\n*/".equals(doc.getText(token.getTokenSequence().offset(), 6))) { // NOI18N
                        Function function = CsmDocGeneratorProvider.getDefault().getFunction(doc, caretOffset);
                        if (function != null) {
                            StringBuilder buf = new StringBuilder();
                            buf.append("* " + NbBundle.getMessage(CppIndentTask.class, "DOCUMENT_HERE_TXT", function.getSignature()) + "\n"); // NOI18N
                            for (Parameter p : function.getParametes()) {
                                for (int i = 0; i < indent; i++) {
                                    buf.append(' ');
                                }
                                buf.append("* @param ").append(p.getName()).append('\n'); // NOI18N
                            }
                            if (!"void".equals(function.getReturnType())) { // NOI18N
                                for (int i = 0; i < indent; i++) {
                                    buf.append(' ');
                                }
                                buf.append("* @return ...").append('\n'); // NOI18N
                                }
                            for (int i = 0; i < indent; i++) {
                                buf.append(' ');
                            }
                            doc.insertString(caretOffset, buf.toString(), null);
                        }
                    } else {
                        if (!"*".equals(doc.getText(caretOffset, 1))) { // NOI18N
                            if (caretOffset > 0 && "\n".equals(doc.getText(caretOffset - 1, 1))) { // NOI18N
                                doc.insertString(caretOffset, "* ", null); // NOI18N
                            }
                        }
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return indent;
            }
        }
        return findIndent(moveToFirstLineImportantToken(token));
    }

     /** Is given token a preprocessor **/
     private boolean isPreprocessorLine(TokenItem token){
         if (token != null){
             return CppTokenId.PREPROCESSOR_KEYWORD_CATEGORY.equals(token.getTokenID().primaryCategory()) ||
                    CppTokenId.PREPROCESSOR_CATEGORY.equals(token.getTokenID().primaryCategory());
         }
         return false;
     }

    private boolean isMultiLineComment(TokenItem token) {
        return (token.getTokenID() == CppTokenId.BLOCK_COMMENT || token.getTokenID() == CppTokenId.DOXYGEN_COMMENT);
    }

    /** Check whether the given token is multi-line comment
     * that starts with a slash and an asterisk.
     */
    private boolean isCCDocComment(TokenItem token) {
        return isMultiLineComment(token);
    }

    /**
     * Find the indentation for the first token on the line.
     * The given token is also examined in some cases.
     */
    private int findIndent(TokenItem token) {
        int indent = -1; // assign invalid indent

        // First check the given token
        if (token != null) {
            switch (token.getTokenID()) {
                case ELSE:
                    TokenItem ifss = findIf(token);
                    if (ifss != null) {
                        indent = getTokenIndent(ifss);
                    }
                    break;

                case LBRACE:
                    TokenItem stmt = findStatement(token);
                    if (stmt == null) {
                        indent = 0;
                    } else {
                        switch (stmt.getTokenID()) {
                            case DO:
                            case FOR:
                            case IF:
                            case WHILE:
                            case ELSE:
                            case TRY:
                            case ASM:
                            case CATCH:
                                indent = getTokenIndent(stmt);
                                if (isHalfIndentNewlineBeforeBrace()){
                                    indent += getShiftWidth()/2;
                                }
                                break;
                            case SWITCH:
                                indent = getTokenIndent(stmt);
                                if (isHalfIndentNewlineBeforeBraceSwitch()){
                                    indent += getShiftWidth()/2;
                                }
                                break;

                            case LBRACE:
                                indent = getTokenIndent(stmt) + getShiftWidth();
                                break;

                            default:
                                stmt = findStatementStart(token);
                                if (stmt == null) {
                                    indent = 0;
                                } else if (stmt == token) {
                                    stmt = findStatement(token); // search for delimiter
                                    indent = (stmt != null) ? indent = getTokenIndent(stmt) : 0;
                                } else { // valid statement
                                    indent = getTokenIndent(stmt);
                                    switch (stmt.getTokenID()) {
                                        case LBRACE:
                                            indent += getShiftWidth();
                                            break;
                                    }
                                }
                        }
                    }
                    break;

                case RBRACE:
                    TokenItem rbmt = findMatchingToken(token, null, CppTokenId.LBRACE, true);
                    if (rbmt != null) { // valid matching left-brace
                        TokenItem t = findStatement(rbmt);
                        boolean forceFirstNonWhitespace = false;
                        if (t == null) {
                            t = rbmt; // will get indent of the matching brace
                        } else {
                            switch (t.getTokenID()) {
                                case SEMICOLON:
                                case LBRACE:
                                case RBRACE:
                                {
                                    t = rbmt;
                                    forceFirstNonWhitespace = true;
                                }
                            }
                        }
                        // the right brace must be indented to the first
                        // non-whitespace char - forceFirstNonWhitespace=true
                        if (forceFirstNonWhitespace) {
                            indent = getTokenColumnAfterBrace(t);
                        } else {
                            indent = getTokenIndent(t);
                        }
                        switch (t.getTokenID()){
                            case FOR:
                            case IF:
                            case WHILE:
                            case DO:
                            case ELSE:
                            case TRY:
                            case ASM:
                            case CATCH:
                                if (isHalfIndentNewlineBeforeBrace()){
                                    indent += getShiftWidth()/2;
                                }
                                break;
                            case SWITCH:
                                if (isHalfIndentNewlineBeforeBraceSwitch()){
                                    indent += getShiftWidth()/2;
                                }
                                break;
                        }
                    } else { // no matching left brace
                        indent = getTokenIndent(token); // leave as is
                    }
                    break;

                case CASE:
                case DEFAULT:
                    TokenItem swss = findSwitch(token);
                    if (swss != null) {
                        indent = getTokenIndent(swss);
                        if (indentCasesFromSwitch()) {
                            indent += getShiftWidth();
                        } else if (isHalfIndentNewlineBeforeBraceSwitch()) {
                            indent += getShiftWidth()/2;
                        }
                    }
                    break;
                case PUBLIC:
                case PRIVATE:
                case PROTECTED:
                    TokenItem cls = findClassifier(token);
                    if (cls != null) {
                        indent = getTokenIndent(cls);
                        if (isHalfIndentVisibility()) {
                            indent += getShiftWidth()/2;
                        }
                    }
                    break;
                case CLASS:
                case STRUCT:
                    TokenItem clsTemplate = findClassifierStart(token);
                    if (clsTemplate != null) {
                        indent = getTokenIndent(clsTemplate);
                    }
                    break;
            }
        }

        // If indent not found, search back for the first important token
        if (indent < 0) { // if not yet resolved
            TokenItem t = findImportantToken(token, null, true);
            if (t != null) { // valid important token
                switch (t.getTokenID()) {
                    case SEMICOLON: // semicolon found
                        TokenItem tt = findStatementStart(token);
                        // preprocessor tokens are not important (bug#22570)
                        if (tt !=null){
                            switch (tt.getTokenID()) {
                                case PUBLIC:
                                case PRIVATE:
                                case PROTECTED:
                                    indent = getTokenIndent(tt) + getShiftWidth();
                                    if (isHalfIndentVisibility()) {
                                        indent -= getShiftWidth()/2;
                                    }
                                    break;
                                case FOR:
                                    if (isForLoopSemicolon(t)) {
                                        if (alignMultilineFor()) {
                                            TokenItem lparen = getLeftParen(t, tt);
                                            if (lparen != null){
                                                indent = getTokenColumn(lparen)+1;
                                                break;
                                            }
                                        }
                                        indent = getTokenIndent(tt) + getFormatStatementContinuationIndent();
                                    } else {
                                        indent = getTokenIndent(tt);
                                    }
                                    break;
                                default:
                                    indent = getTokenIndent(tt);
                                    break;
                            }
                        }
                        break;

                    case LBRACE:
                        TokenItem lbss = findStatementStart(t, false);
                        if (lbss == null) {
                            lbss = t;
                        }
                        switch (lbss.getTokenID()){
                            case FOR:
                            case IF:
                            case WHILE:
                            case DO:
                            case ELSE:
                            case TRY:
                            case ASM:
                            case CATCH:
                            case SWITCH:
                                indent = getTokenIndent(lbss) + getShiftWidth();
                                break;
                            case NAMESPACE:
                                if (indentNamespace()) {
                                    indent = getTokenIndent(lbss) + getRightIndentDeclaration();
                                } else {
                                    indent = getTokenIndent(lbss);
                                }
                                break;
                            default:
                                indent = getTokenIndent(lbss) + getRightIndentDeclaration();
                                break;
                        }
                        break;

                    case RBRACE:
                        TokenItem t3 = findStatementStart(token, true);
                        if (t3 != null) {
                            indent = getTokenIndent(t3);
                        }
                        break;

                    case COLON:
                        TokenItem ttt = getVisibility(t);
                        if (ttt != null){
                            indent = getTokenIndent(ttt) + getRightIndentDeclaration();
                            if (isHalfIndentVisibility()) {
                                indent -= getShiftWidth()/2;
                            }
                        } else {
                            ttt = findAnyToken(t, null,
                                    new CppTokenId[] {CppTokenId.CASE,
                                    CppTokenId.DEFAULT,
                                    CppTokenId.QUESTION,
                                    CppTokenId.PRIVATE,
                                    CppTokenId.PROTECTED,
                                    CppTokenId.PUBLIC}, true);
                            if (ttt != null) {
                                switch(ttt.getTokenID()) {
                                    case QUESTION:
                                        indent = getTokenIndent(ttt) + getShiftWidth();
                                        break;
                                    case CASE:
                                    case DEFAULT:
                                        indent = getTokenIndent(ttt) + getRightIndentSwitch();
                                        break;
                                    default:
                                        // Indent of line with ':' plus one indent level
                                        indent = getTokenIndent(t);// + getShiftWidth();
                                }
                            }
                        }
                        break;

                    case QUESTION:
                        indent = getTokenIndent(t) + getShiftWidth();
                        break;
                    case DO:
                    case ELSE:
                        indent = getTokenIndent(t) + getRightIndent();
                        break;

                    case RPAREN:
                        // Try to find the matching left paren
                        TokenItem rpmt = findMatchingToken(t, null, CppTokenId.LPAREN, true);
                        if (rpmt != null) {
                            rpmt = findImportantToken(rpmt, null, true);
                            // Check whether there are the indent changing kwds
                            if (rpmt != null) {
                                switch (rpmt.getTokenID()) {
                                    case FOR:
                                    case IF:
                                    case WHILE:
                                        // Indent one level
                                        indent = getTokenIndent(rpmt) + getRightIndent();
                                        break;
                                    case IDENTIFIER:
                                        if (token != null && token.getTokenID() == CppTokenId.IDENTIFIER) {
                                            indent = getTokenIndent(t);
                                        }
                                        break;
                                }
                            }
                        }
                        if (indent < 0) {
                            indent = computeStatementIndent(t);
                        }
                        break;

                    case IDENTIFIER:
                        if (token != null && token.getTokenID() == CppTokenId.IDENTIFIER) {
                            indent = getTokenIndent(t);
                            break;
                        }
                        indent = computeStatementIndent(t);
                        break;

                    case COMMA:
                        if (isEnumComma(t)) {
                            indent = getTokenIndent(t);
                            break;
                        } else if (isFieldComma(t)) {
                            indent = getTokenIndent(t);
                            break;
                        }
                        indent = computeStatementIndent(t);
                        break;
                    default:
                        indent = computeStatementIndent(t);
                        break;
                }

                if (indent < 0) { // no indent found yet
                    indent = getTokenIndent(t);
                }
            }
        }

        if (indent < 0) { // no important token found
            indent = 0;
        }
        return indent;
    }

    private int computeStatementIndent(final TokenItem t) {
        int indent;
        // Find stmt start and add continuation indent
        TokenItem stmtStart = findStatementStart(t);
        indent = getTokenIndent(stmtStart);
        //int tindent = getTokenIndent(t);
        //if (tindent > indent)
        //    return tindent;

        if (stmtStart != null) {
            // Check whether there is a comma on the previous line end
            // and if so then also check whether the present
            // statement is inside array initialization statement
            // and not inside parents and if so then do not indent
            // statement continuation
            if (t != null && t.getTokenID() == CppTokenId.COMMA) {
                if (isArrayInitializationBraceBlock(t, null) &&
                    getLeftParen(t, stmtStart)==null) {
                    return indent;
                }
                TokenItem lparen = getLeftParen(t, stmtStart);
                if (lparen != null){
                    TokenItem prev = findImportantToken(lparen, null, true);
                    if (prev != null &&
                        prev.getTokenID() == CppTokenId.IDENTIFIER){
                        if (isStatement(stmtStart)) {
                            if (alignMultilineCallArgs()){
                                return getTokenColumn(lparen)+1;
                            }
                        } else {
                            if (alignMultilineMethodParams()){
                                return getTokenColumn(lparen)+1;
                            }
                        }
                    }
                }
            } else if ( (stmtStart.getTokenID() == CppTokenId.IF && alignMultilineIf()) ||
                        (stmtStart.getTokenID() == CppTokenId.WHILE && alignMultilineWhile()) ||
                        (stmtStart.getTokenID() == CppTokenId.FOR && alignMultilineFor())) {
                if (t != null){
                    TokenItem lparen = getLeftParen(t, stmtStart);
                    if (lparen != null){
                        return getTokenColumn(lparen)+1;
                    }
                }
            } else if (!isStatement(stmtStart)){
                return indent;
            }
            indent += getFormatStatementContinuationIndent();
        }
        return indent;
    }

    // for services
    public CppIndentTask(Document doc) {
        this.doc = doc;
    }

    /**
     * returns indentation for line containing given offset
     * @param offset offset on line
     * @return indentation of line containing offset
     */
    public int getLineIndentation(int caretOffset) {
        if (codeStyle == null) {
            codeStyle = CodeStyle.getDefault(doc);
        }
        int lineOffset;
        try {
            lineOffset = IndentUtils.lineStartOffset(doc, caretOffset);
        } catch (BadLocationException ex) {
            return 0;
        }
        ts = CndLexerUtilities.getCppTokenSequence(doc, lineOffset, false, false);
        if (ts == null) {
            return 0;
        }
        int indent = indentLine(new TokenItem(ts, true), caretOffset);
        return indent;
    }

    // for testing
    public void reindent(int caretOffset) throws BadLocationException {
        if (codeStyle == null) {
            codeStyle = CodeStyle.getDefault(doc);
        }
        int lineOffset = IndentUtils.lineStartOffset(doc, caretOffset);
        ts = CndLexerUtilities.getCppTokenSequence(doc, lineOffset, false, false);
        if (ts == null) {
            return;
        }
        int indent = indentLine(new TokenItem(ts, true), caretOffset);
        if (indent >= 0) {
            modifyIndent(lineOffset, indent);
        }
    }

    // for testing
    private void modifyIndent(int lineStartOffset, int newIndent) throws BadLocationException {
        // Determine old indent first together with oldIndentEndOffset
        int indent = 0;
        int tabSize = -1;
        CharSequence docText = doc.getText(0, doc.getLength());
        int oldIndentEndOffset = lineStartOffset;
        while (oldIndentEndOffset < docText.length()) {
            char ch = docText.charAt(oldIndentEndOffset);
            if (ch == '\n') {
                break;
            } else if (ch == '\t') {
                if (tabSize == -1) {
                    tabSize = IndentUtils.tabSize(doc);
                }
                // Round to next tab stop
                indent = (indent + tabSize) / tabSize * tabSize;
            } else if (Character.isWhitespace(ch)) {
                indent++;
            } else { // non-whitespace
                break;
            }
            oldIndentEndOffset++;
        }

        String newIndentString = IndentUtils.createIndentString(doc, newIndent);
        // Attempt to match the begining characters
        int offset = lineStartOffset;
        for (int i = 0; i < newIndentString.length() && lineStartOffset + i < oldIndentEndOffset; i++) {
            if (newIndentString.charAt(i) != docText.charAt(lineStartOffset + i)) {
                offset = lineStartOffset + i;
                newIndentString = newIndentString.substring(i);
                break;
            }
        }

        // Replace the old indent
        if (offset < oldIndentEndOffset) {
            doc.remove(offset, oldIndentEndOffset - offset);
        }
        if (newIndentString.length() > 0) {
            doc.insertString(offset, newIndentString, null);
        }
    }

}
