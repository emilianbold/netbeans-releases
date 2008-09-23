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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.gsf;

import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Formatter;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.css.lexer.api.CSSTokenId;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.openide.util.Exceptions;

/**
 *
 * @author marek
 * 
 * @todo fix the section reformatting so it can reformat multiple embedded pieces of css
 */
public class CSSFormatter implements Formatter {

    public boolean needsParserResult() {
        return false;
    }

    public void reformat(Context context, CompilationInfo info) {
        reindent(context);
    }

    public void reindent(final Context context) {
        final BaseDocument bdoc = (BaseDocument) context.document();

        bdoc.runAtomic(new Runnable() {

            public void run() {
                //XXX still using the old formatting API, the new one is a subject of change in next version of netbeans.
                int startOffset = context.startOffset();
                int endOffset = context.endOffset();
                try {
                    int indentLevel = IndentUtils.indentLevelSize(bdoc);
                    int lastLine = Utilities.getLineOffset(bdoc, bdoc.getLength());
                    int indents[] = new int[lastLine + 1];
                    int indentShift[] = new int[lastLine + 1];
                    boolean formattableLines[] = new boolean[lastLine + 1];
                    int lineBegin = Utilities.getRowStart(bdoc, startOffset);
                    //int firstLineOffset = Utilities.getLineOffset(bdoc, startOffset);
                    //int lastLineOffset = Utilities.getLineOffset(bdoc, endOffset);

                    //XXX wrong impl - it needs to operate on multiple token sequences???
                    //does the infrastructure call me separately for each ts or just once?
                    TokenHierarchy th = TokenHierarchy.get(bdoc);

                    for (LanguagePath languagePath : (Set<LanguagePath>) th.languagePaths()) {
                        if (languagePath.innerLanguage() == CSSTokenId.language()) {
                            for (TokenSequence ts : (List<TokenSequence>) th.tokenSequenceList(languagePath, 0, bdoc.getLength())) {
                                TextBounds tsBounds = findTokenSequenceBounds(bdoc, ts);

                                if (tsBounds.getAbsoluteEnd() < startOffset || tsBounds.getAbsoluteStart() > endOffset || tsBounds.getStartPos() == -1) { // empty CSS section
                                    continue;
                                }

                                if (startOffset == endOffset) {
                                    //enter pressed - indent just the line under cursor
                                    if (lineBegin == 1) {
                                        //pressed on first line - do not indent
                                        return;
                                    }

                                    //go back and find first line with ident(ifier) or '{' char - and indent accordingly 
                                    // ident = 0
                                    // { = +1
                                    ts.move(lineBegin);
                                    while (ts.movePrevious()) {
                                        Token t = ts.token();
                                        if (t.id() == CSSTokenId.IDENT ||
                                                t.id() == CSSTokenId.RBRACE) {
                                            //we are on a line with css rule item e.g. color:red; 
                                            //where the property name is of IDENT token type
                                            //..or.. we are just after end of a rule ("}" token)
                                            //=> use the same indent level

                                            // TODO - use IndentUtils here instead of Utilities!
                                            int indent = Utilities.getRowIndent(bdoc, t.offset(th));
                                            context.modifyIndent(lineBegin, indent);
                                            return;
                                        } else if (t.id() == CSSTokenId.LBRACE) {
                                            // just rule beginning before current position - increase indent
                                            int indent = Utilities.getRowIndent(bdoc, t.offset(th));

                                            //XXX or should I use the this.indentSize() instead????
                                            context.modifyIndent(lineBegin, indent + indentLevel);
                                            return;
                                        }
                                    }
                                } else {
                                    //user peformed reformat on a selection or the whole text

                                    int firstLineOffset = tsBounds.getStartLine();
                                    int lastLineOffset = tsBounds.getEndLine();

                                    int firstLineOriginalIndent = Utilities.getRowIndent(bdoc, tsBounds.getStartPos());

                                    for (int line = firstLineOffset; line <= lastLineOffset; line++) {
                                        indentShift[line] = firstLineOriginalIndent;
                                        formattableLines[line] = true;
                                    }

                                    //contains <line_start_offset; indent_level> pairs
                                    int indent = 0;
                                    for (int line = firstLineOffset; line <= lastLineOffset; line++) {
                                        int indents_index = line;
                                        if (indents_index == 0) {
                                            //do we start inside a rule?

                                            Token[] ruleBoundaries = findRule(ts, lineBegin);
                                            if (ruleBoundaries != null) {
                                                //yes, we start in a rule
                                                indent = Utilities.getRowIndent(bdoc, ruleBoundaries[0].offset(th));
                                                //and add the content indent
                                                indent = indent + indentLevel;
                                            }
                                        }

                                        //identify the line content
                                        int lStart = Utilities.getRowStartFromLineOffset(bdoc, line);
                                        int lEnd = Utilities.getRowEnd(bdoc, lStart);
                                        ts.move(lStart);
                                        int ruleStart = -1;
                                        int ruleEnd = -1;
                                        int ident = -1;
                                        while (ts.moveNext() && ts.offset() < lEnd) {
                                            Token t = ts.token();
                                            if (t.id() == CSSTokenId.LBRACE) {
                                                ruleStart = ts.offset();
                                            } else if (t.id() == CSSTokenId.RBRACE) {
                                                ruleEnd = ts.offset();
                                            } else if (t.id() == CSSTokenId.IDENT) {
                                                ident = ts.offset();
                                            }
                                        }

                                        if (ruleStart != -1 && ruleEnd == -1) {
                                            //rule started
                                            indents[indents_index] = indent;
                                            //increase indent of the content
                                            indent = indent + indentLevel;
                                        } else if (ruleStart == -1 && ruleEnd != -1) {
                                            //rule ended

                                            //test if there is an identifier before the rule closing symbol
                                            //in such case do not lower the indent of the line
                                            if (ident != -1 && ident < ruleEnd) {
                                                //sg. like "color: red; }" on the line
                                                indents[indents_index] = indent;
                                                indent = indent - indentLevel;
                                            } else {
                                                indent = indent - indentLevel;
                                                indents[indents_index] = indent;
                                            }
                                        } else {
                                            //just text inside or outside of a rule
                                            indents[indents_index] = indent;
                                        }

                                    }
                                }
                            }
                        }

                    }

                    int firstLineWithinFormattingRange = Utilities.getLineOffset(bdoc, startOffset);
                    int lastLineWithinFormattingRange = Utilities.getLineOffset(bdoc, endOffset);

                    //apply the formatting
                    for (int line = firstLineWithinFormattingRange; line <= lastLineWithinFormattingRange; line++) {
                        if (formattableLines[line]) {
                            int lStart = Utilities.getRowStartFromLineOffset(bdoc, line);
                            context.modifyIndent(lStart, indents[line] + indentShift[line]);
                        }
                    }

                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
        });
    }

    /** 
     * @param ts - tokenSequence
     * @param offset - where to start the search
     * @return an array of two Tokens which represents the curly brackets of
     * css rule.
     */
    private Token[] findRule(TokenSequence ts, int offset) {
        ts.move(offset);
        while (ts.movePrevious()) {
            Token t = ts.token();
            if (t.id() == CSSTokenId.RBRACE) {
                //we found } => we are out of a rule
                return null;
            } else if (t.id() == CSSTokenId.LBRACE) {
                //we are in a rule -> find the end of the rule
                while (ts.moveNext()) {
                    Token t2 = ts.token();
                    if (t2.id() == CSSTokenId.RBRACE) {
                        //end of the rule
                        return new Token[]{t, t2};
                    }
                }
                
                break;
            }

        }
        //din't find anything reasonable
        return null;
    }

    public int indentSize() {
        return 4;
    }

    public int hangingIndentSize() {
        return 4;
    }

    private TextBounds findTokenSequenceBounds(BaseDocument doc, TokenSequence tokenSequence) throws BadLocationException {
        tokenSequence.moveStart();
        
        if (!tokenSequence.moveNext()){
            return new TextBounds(0, 0); // empty token sequence
        }
        
        int absoluteStart = tokenSequence.offset();
        tokenSequence.moveEnd();
        tokenSequence.movePrevious();
        int absoluteEnd = tokenSequence.offset() + tokenSequence.token().length();

//         trim whitespaces from both ends
        while (isWSToken(tokenSequence.token())) {
            if (!tokenSequence.movePrevious()) {
                return new TextBounds(absoluteStart, absoluteEnd); // a block of empty text
            }
        }

        int whiteSpaceSuffixLen = 0;

        while (Character.isWhitespace(tokenSequence.token().text().charAt(tokenSequence.token().length() - 1 - whiteSpaceSuffixLen))) {
            whiteSpaceSuffixLen++;
        }

        int languageBlockEnd = tokenSequence.offset() + tokenSequence.token().length() - whiteSpaceSuffixLen;

        tokenSequence.moveStart();

        do {
            tokenSequence.moveNext();
        } while (isWSToken(tokenSequence.token()));

        int whiteSpacePrefixLen = 0;

        while (Character.isWhitespace(tokenSequence.token().text().charAt(whiteSpacePrefixLen))) {
            whiteSpacePrefixLen++;
        }

        int languageBlockStart = tokenSequence.offset() + whiteSpacePrefixLen;
        int firstLineOfTheLanguageBlock = Utilities.getLineOffset(doc, languageBlockStart);
        int lastLineOfTheLanguageBlock = Utilities.getLineOffset(doc, languageBlockEnd);
        return new TextBounds(absoluteStart, absoluteEnd, languageBlockStart, languageBlockEnd, firstLineOfTheLanguageBlock, lastLineOfTheLanguageBlock);
    }

    protected boolean isWSToken(Token token) {
        return isOnlyWhiteSpaces(token.text());
    }

    protected boolean isOnlyWhiteSpaces(CharSequence txt) {
        for (int i = 0; i < txt.length(); i++) {
            if (!Character.isWhitespace(txt.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static class TextBounds {

        private int absoluteStart; // start offset regardless of white spaces
        private int absoluteEnd; // end --
        private int startPos = -1;
        private int endPos = -1;
        private int startLine = -1;
        private int endLine = -1;

        public TextBounds(int absoluteStart, int absoluteEnd) {
            this.absoluteStart = absoluteStart;
            this.absoluteEnd = absoluteEnd;
        }

        public TextBounds(int absoluteStart, int absoluteEnd, int startPos, int endPos, int startLine, int endLine) {
            this.absoluteStart = absoluteStart;
            this.absoluteEnd = absoluteEnd;
            this.startPos = startPos;
            this.endPos = endPos;
            this.startLine = startLine;
            this.endLine = endLine;
        }

        public int getEndPos() {
            return endPos;
        }

        public int getStartPos() {
            return startPos;
        }

        public int getEndLine() {
            return endLine;
        }

        public int getStartLine() {
            return startLine;
        }

        public int getAbsoluteEnd() {
            return absoluteEnd;
        }

        public int getAbsoluteStart() {
            return absoluteStart;
        }

        @Override
        public String toString() {
            return "pos " + startPos + "-" + endPos + ", lines " + startLine + "-" + endLine; //NOI18N
        }
    }
}
