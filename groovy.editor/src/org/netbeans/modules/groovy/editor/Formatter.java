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
package org.netbeans.modules.groovy.editor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.options.CodeStyle;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.openide.util.Exceptions;


/**
 * Formatting and indentation for Groovy.
 * 
 * @todo tab press is just 2
 * @todo bodies of case statements (in switch) are not indented 
 * @todo formats javadoc
 * 
 * @author Tor Norbye
 * @author Martin Adamek
 * @author Gopalakrishnan Sankaran
 */
public class Formatter implements org.netbeans.modules.gsf.api.Formatter {
    private boolean isGspDocument;
    private CodeStyle codeStyle;
    private int rightMarginOverride = -1;

    public Formatter() {
        this.codeStyle = null;
    }
    
    public Formatter(CodeStyle codeStyle, int rightMarginOverride) {
        assert codeStyle != null;
        this.codeStyle = codeStyle;
        this.rightMarginOverride = rightMarginOverride;
    }
    

    public boolean needsParserResult() {
        return false;
    }

    public void reindent(Context context) {
        if (codeStyle != null) {
            reindent(context, null, true);
        } else {
            Formatter f = new Formatter(CodeStyle.get(context.document()), -1);
            f.reindent(context, null, true);
        }
    }

    public void reformat(Context context, CompilationInfo compilationInfo) {
        if (codeStyle != null) {
            reindent(context, compilationInfo, false);
        } else {
            Formatter f = new Formatter(CodeStyle.get(context.document()), -1);
            f.reindent(context, compilationInfo, false);
        }
    }
    
    public int indentSize() {
        if (codeStyle != null) {
            return codeStyle.getIndentSize();
        } else {
            return CodeStyle.get((Document) null).getIndentSize();
        }
    }
    
    public int hangingIndentSize() {
        if (codeStyle != null) {
            return codeStyle.getContinuationIndentSize();
        } else {
            return CodeStyle.get((Document) null).getContinuationIndentSize();
        }
    }

    /** Compute the initial balance of brackets at the given offset. */
    private int getFormatStableStart(BaseDocument doc, int offset) {
        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);
        if (ts == null) {
            return 0;
        }

        ts.move(offset);

        if (!ts.movePrevious()) {
            return 0;
        }

        // Look backwards to find a suitable context - a class, module or method definition
        // which we will assume is properly indented and balanced
        do {
            Token<?extends GroovyTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == GroovyTokenId.LITERAL_class) {
                return ts.offset();
            }
        } while (ts.movePrevious());

        return ts.offset();
    }
    
    private int getTokenBalanceDelta(TokenId id, Token<? extends GroovyTokenId> token,
            BaseDocument doc, TokenSequence<? extends GroovyTokenId> ts, boolean includeKeywords) {
        if (id == GroovyTokenId.IDENTIFIER) {
            // In some cases, the [ shows up as an identifier, for example in this expression:
            //  for k, v in sort{|a1, a2| a1[0].id2name <=> a2[0].id2name}
            if (token.length() == 1) {
                char c = token.text().charAt(0);
                if (c == '[') {
                    return 1;
                } else if (c == ']') {
                    // I've seen "]" come instead of a RBRACKET too - for example in RHTML:
                    // <%if session[:user]%>
                    return -1;
                }
            }
        } else if (id == GroovyTokenId.LPAREN || id == GroovyTokenId.LBRACKET || id == GroovyTokenId.LBRACE) {
            return 1;
        } else if (id == GroovyTokenId.RPAREN || id == GroovyTokenId.RBRACKET || id == GroovyTokenId.RBRACE) {
            return -1;
        } else if (includeKeywords) {
            if (LexUtilities.isBeginToken(id, doc, ts)) {
                return 1;
            } else if (id == GroovyTokenId.RBRACE) {
                return -1;
            }
        }

        return 0;
    }
    
    // TODO RHTML - there can be many discontiguous sections, I've gotta process all of them on the given line
    private int getTokenBalance(BaseDocument doc, int begin, int end, boolean includeKeywords) {
        int balance = 0;

        if (isGspDocument) {
            TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
            // Probably an GSP file - gotta process it in sections since I can have lines
            // made up of both whitespace, groovy, html and delimiters and all groovy sections
            // can affect the token balance
            TokenSequence<?> t = th.tokenSequence();
            if (t == null) {
                return 0;
            }
            t.move(begin);
            if (!t.moveNext()) {
                return 0;
            }
            
            do {
                Token<?> token = t.token();
                TokenId id = token.id();
                
                if (id.primaryCategory().equals("groovy")) { // NOI18N
                    TokenSequence<? extends GroovyTokenId> ts = t.embedded(GroovyTokenId.language());
                    ts.move(begin);
                    ts.moveNext();
                    do {
                        Token<?extends GroovyTokenId> groovyToken = ts.token();
                        if (groovyToken == null) {
                            break;
                        }
                        TokenId groovyId = groovyToken.id();

                        balance += getTokenBalanceDelta(groovyId, groovyToken, doc, ts, includeKeywords);
                    } while (ts.moveNext() && (ts.offset() < end));
                }

            } while (t.moveNext() && (t.offset() < end));
        } else {
            TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, begin);
            if (ts == null) {
                return 0;
            }
            
            ts.move(begin);

            if (!ts.moveNext()) {
                return 0;
            }

            do {
                Token<?extends GroovyTokenId> token = ts.token();
                TokenId id = token.id();
                
                balance += getTokenBalanceDelta(id, token, doc, ts, includeKeywords);
            } while (ts.moveNext() && (ts.offset() < end));
        }

        return balance;
    }

    // This method will indent lines beginning with * by 1 space
    private boolean isJavaDocComment(BaseDocument doc, int offset, int endOfLine) 
            throws BadLocationException {
        int pos = Utilities.getRowFirstNonWhite(doc, offset);
        if(pos != -1) {
            Token<?extends GroovyTokenId> token = LexUtilities.getToken(doc, pos);
            if(token != null) {
                TokenId id = token.id();
                if(id == GroovyTokenId.BLOCK_COMMENT) {
                    String text = doc.getText(offset, endOfLine - offset);
                    if(text.trim().startsWith("*")) {
                        return true;
                    }                        
                }
            }
        }        
        return false;
    }

    private boolean isInLiteral(BaseDocument doc, int offset) throws BadLocationException {
        // TODO: Handle arrays better
        // %w(January February March April May June July
        //    August September October November December)
        // I should indent to the same level

        // Can't reformat these at the moment because reindenting a line
        // that is a continued string array causes incremental lexing errors
        // (which further screw up formatting)
        int pos = Utilities.getRowFirstNonWhite(doc, offset);
        //int pos = offset;

        if (pos != -1) {
            // I can't look at the first position on the line, since
            // for a string array that is indented, the indentation portion
            // is recorded as a blank identifier
            Token<?extends GroovyTokenId> token = LexUtilities.getToken(doc, pos);

            if (token != null) {
                TokenId id = token.id();
                // If we're in a string literal (or regexp or documentation) leave
                // indentation alone!
                if ((id == GroovyTokenId.STRING_LITERAL) ||
                        id == GroovyTokenId.DOCUMENTATION ||
                        (id == GroovyTokenId.QUOTED_STRING_LITERAL) ||
                        (id == GroovyTokenId.REGEXP_LITERAL)) {
                    // No indentation for literal strings in Groovy, since they can
                    // contain newlines. Leave it as is.
                    return true;
                }
                
                if (id == GroovyTokenId.STRING_END || id == GroovyTokenId.QUOTED_STRING_END) {
                    // Possibly a heredoc
                    TokenSequence<? extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, pos);
                    ts.move(pos);
                    OffsetRange range = LexUtilities.findHeredocBegin(ts, token);
                    if (range != OffsetRange.NONE) {
                        String text = doc.getText(range.getStart(), range.getLength());
                        if (text.startsWith("<<-")) { // NOI18N
                            return false;
                        } else {
                            return true;
                        }
                    }
                }
            } else {
                // No Groovy token -- leave the formatting alone!
                return true;
            }
        } else {
            // Empty line inside a string, documentation etc. literal?
            Token<?extends GroovyTokenId> token = LexUtilities.getToken(doc, offset);

            if (token != null) {
                TokenId id = token.id();
                // If we're in a string literal (or regexp or documentation) leave
                // indentation alone!
                if ((id == GroovyTokenId.STRING_LITERAL) ||
                        id == GroovyTokenId.DOCUMENTATION ||
                        (id == GroovyTokenId.QUOTED_STRING_LITERAL) ||
                        (id == GroovyTokenId.REGEXP_LITERAL)) {
                    // No indentation for literal strings in Groovy, since they can
                    // contain newlines. Leave it as is.
                    return true;
                }
            }
        }

        return false;
    }
    
    /** 
     * Get the first token on the given line. 
     */
    private Token<? extends GroovyTokenId> getFirstToken(BaseDocument doc, int offset) throws BadLocationException {
        int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);

        if (lineBegin != -1) {
            if (isGspDocument) {
                TokenSequence<? extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, lineBegin);
                if (ts != null) {
                    ts.moveNext();
                    Token<?extends GroovyTokenId> token = ts.token();
                    while (token != null && token.id() == GroovyTokenId.WHITESPACE) {
                        if (!ts.moveNext()) {
                            return null;
                        }
                        token = ts.token();
                    }
                    return token;
                }
            } else {
                return LexUtilities.getToken(doc, lineBegin);
            }
        }
        
        return null;
    }

    private boolean isEndIndent(BaseDocument doc, int offset) throws BadLocationException {
        int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);

        if (lineBegin != -1) {
            Token<?extends GroovyTokenId> token = getFirstToken(doc, offset);
            
            if (token == null) {
                return false;
            }
            
            TokenId id = token.id();

            // If the line starts with an end-marker, such as "end", "}", "]", etc.,
            // find the corresponding opening marker, and indent the line to the same
            // offset as the beginning of that line.
            return (LexUtilities.isIndentToken(id) && !LexUtilities.isBeginToken(id, doc, offset)) ||
                id == GroovyTokenId.RBRACE || id == GroovyTokenId.RBRACKET || id == GroovyTokenId.RPAREN;
        }
        
        return false;
    }
    
    private boolean isLineContinued(BaseDocument doc, int offset, int bracketBalance) throws BadLocationException {
        // TODO RHTML - this isn't going to work for rhtml embedded strings...
        offset = Utilities.getRowLastNonWhite(doc, offset);
        if (offset == -1) {
            return false;
        }

        
        TokenSequence<?extends GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);

        if (ts == null) {
            return false;
        }
        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<?extends GroovyTokenId> token = ts.token();

        if (token != null) {
            TokenId id = token.id();
            
            // http://www.netbeans.org/issues/show_bug.cgi?id=115279
            boolean isContinuationOperator = (id == GroovyTokenId.NONUNARY_OP || id == GroovyTokenId.DOT);
            
            if (ts.offset() == offset && token.length() > 1 && token.text().toString().startsWith("\\")) {
                // Continued lines have different token types
                isContinuationOperator = true;
            }
            
            if (token.length() == 1 && id == GroovyTokenId.IDENTIFIER && token.text().toString().equals(",")) {
                // If there's a comma it's a continuation operator, but inside arrays, hashes or parentheses
                // parameter lists we should not treat it as such since we'd "double indent" the items, and
                // NOT the first item (where there's no comma, e.g. you'd have
                //  foo(
                //    firstarg,
                //      secondarg,  # indented both by ( and hanging indent ,
                //      thirdarg)
                if (bracketBalance == 0) {
                    isContinuationOperator = true;
                }
            }
            
            if (isContinuationOperator) {
                // Make sure it's not a case like this:
                //    alias eql? ==
                // or
                //    def ==
                token = LexUtilities.getToken(doc, Utilities.getRowFirstNonWhite(doc, offset));
                if (token != null) {
                    id = token.id();
                    if (id == GroovyTokenId.LBRACE || id == GroovyTokenId.ANY_KEYWORD && token.text().toString().equals("alias")) { // NOI18N
                        return false;
                    }
                }

                return true;
            } else if (id == GroovyTokenId.ANY_KEYWORD) {
                String text = token.text().toString();
                if ("or".equals(text) || "and".equals(text)) { // NOI18N
                    return true;
                }
            }
        }

        return false;
    }

    private void reindent(final Context context, CompilationInfo info, final boolean indentOnly) {
        assert codeStyle != null;
        
        Document document = context.document();
        final int endOffset = Math.min(context.endOffset(), document.getLength());
        isGspDocument = false;

        try {
            final BaseDocument doc = (BaseDocument)document; // document.getText(0, document.getLength())

            final int startOffset = Utilities.getRowStart(doc, context.startOffset());
            final int lineStart = startOffset;//Utilities.getRowStart(doc, startOffset);
            int initialOffset = 0;
            int initialIndent = 0;
            if (startOffset > 0) {
                int prevOffset = Utilities.getRowStart(doc, startOffset-1);
                initialOffset = getFormatStableStart(doc, prevOffset);
                initialIndent = GsfUtilities.getLineIndent(doc, initialOffset);
            }
            
            // Build up a set of offsets and indents for lines where I know I need
            // to adjust the offset. I will then go back over the document and adjust
            // lines that are different from the intended indent. By doing piecemeal
            // replacements in the document rather than replacing the whole thing,
            // a lot of things will work better: breakpoints and other line annotations
            // will be left in place, semantic coloring info will not be temporarily
            // damaged, and the caret will stay roughly where it belongs.
            final List<Integer> offsets = new ArrayList<Integer>();
            final List<Integer> indents = new ArrayList<Integer>();

            // When we're formatting sections, include whitespace on empty lines; this
            // is used during live code template insertions for example. However, when
            // wholesale formatting a whole document, leave these lines alone.
            boolean indentEmptyLines = (startOffset != 0 || endOffset != doc.getLength());

            boolean includeEnd = endOffset == doc.getLength() || indentOnly;
            
            // TODO - remove initialbalance etc.
            computeIndents(doc, initialIndent, initialOffset, endOffset, info, 
                    offsets, indents, indentEmptyLines, includeEnd, indentOnly);

            doc.runAtomic(new Runnable() {
                public void run() {
                    try {
                        // Iterate in reverse order such that offsets are not affected by our edits
                        assert indents.size() == offsets.size();
                        for (int i = indents.size() - 1; i >= 0; i--) {
                            int indent = indents.get(i);
                            int lineBegin = offsets.get(i);

                            if (lineBegin < lineStart) {
                                // We're now outside the region that the user wanted reformatting;
                                // these offsets were computed to get the correct continuation context etc.
                                // for the formatter
                                break;
                            }

                            if (lineBegin == lineStart && i > 0) {
                                // Look at the previous line, and see how it's indented
                                // in the buffer.  If it differs from the computed position,
                                // offset my computed position (thus, I'm only going to adjust
                                // the new line position relative to the existing editing.
                                // This avoids the situation where you're inserting a newline
                                // in the middle of "incorrectly" indented code (e.g. different
                                // size than the IDE is using) and the newline position ending
                                // up "out of sync"
                                int prevOffset = offsets.get(i-1);
                                int prevIndent = indents.get(i-1);
                                int actualPrevIndent = GsfUtilities.getLineIndent(doc, prevOffset);
                                if (actualPrevIndent != prevIndent) {
                                    // For blank lines, indentation may be 0, so don't adjust in that case
                                    if (!(Utilities.isRowEmpty(doc, prevOffset) || Utilities.isRowWhite(doc, prevOffset))) {
                                        indent = actualPrevIndent + (indent-prevIndent);
                                    }
                                }
                            }

                            // Adjust the indent at the given line (specified by offset) to the given indent
                            int currentIndent = GsfUtilities.getLineIndent(doc, lineBegin);

                            if (currentIndent != indent) {
                                context.modifyIndent(lineBegin, indent);
                            }
                        }

                        if (!indentOnly && codeStyle.reformatComments()) {
                            reformatComments(doc, startOffset, endOffset);
                        }
                    } catch (BadLocationException ble) {
                        Exceptions.printStackTrace(ble);
                    }
                }
            });
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    public void computeIndents(BaseDocument doc, int initialIndent, int startOffset, int endOffset, CompilationInfo info,
            List<Integer> offsets,
            List<Integer> indents,
            boolean indentEmptyLines, boolean includeEnd, boolean indentOnly
        ) {
        // PENDING:
        // The reformatting APIs in NetBeans should be lexer based. They are still
        // based on the old TokenID apis. Once we get a lexer version, convert this over.
        // I just need -something- in place until that is provided.

        try {
            // Algorithm:
            // Iterate over the range.
            // Accumulate a token balance ( {,(,[, and keywords like class, case, etc. increases the balance, 
            //      },),] and "end" decreases it
            // If the line starts with an end marker, indent the line to the level AFTER the token
            // else indent the line to the level BEFORE the token (the level being the balance * indentationSize)
            // Compute the initial balance and indentation level and use that as a "base".
            // If the previous line is not "done" (ends with a comma or a binary operator like "+" etc.
            // add a "hanging indent" modifier.
            // At the end of the day, we're recording a set of line offsets and indents.
            // This can be used either to reformat the buffer, or indent a new line.

            // State:
            int offset = Utilities.getRowStart(doc, startOffset); // The line's offset
            int end = endOffset;
            
            int indentSize = codeStyle.getIndentSize();
            int hangingIndentSize = codeStyle.getContinuationIndentSize();
            
            // Pending - apply comment formatting too?


            // Build up a set of offsets and indents for lines where I know I need
            // to adjust the offset. I will then go back over the document and adjust
            // lines that are different from the intended indent. By doing piecemeal
            // replacements in the document rather than replacing the whole thing,
            // a lot of things will work better: breakpoints and other line annotations
            // will be left in place, semantic coloring info will not be temporarily
            // damaged, and the caret will stay roughly where it belongs.

            // The token balance at the offset
            int balance = 0;
            // The bracket balance at the offset ( parens, bracket, brace )
            int bracketBalance = 0;
            boolean continued = false;
            boolean indentHtml = false;
            if (isGspDocument) {
                indentHtml = codeStyle.indentHtml();
            }

            while ((!includeEnd && offset < end) || (includeEnd && offset <= end)) {
                int indent; // The indentation to be used for the current line

                int hangingIndent = continued ? (hangingIndentSize) : 0;

                if (isGspDocument && !indentOnly) {
                    // Pick up the indentation level assigned by the HTML indenter; gets HTML structure
                    initialIndent = GsfUtilities.getLineIndent(doc, offset);
                }
                
                if (isInLiteral(doc, offset)) {
                    // Skip this line - leave formatting as it is prior to reformatting 
                    indent = GsfUtilities.getLineIndent(doc, offset);

                    if (isGspDocument && indentHtml && balance > 0) {
                        indent += balance * indentSize;
                    }
                } else if (isEndIndent(doc, offset)) {
                    indent = (balance-1) * indentSize + hangingIndent + initialIndent;
                } else {
                    indent = balance * indentSize + hangingIndent + initialIndent;
                }
                
                int endOfLine = Utilities.getRowEnd(doc, offset) + 1;
                
                if (isJavaDocComment(doc,offset, endOfLine)) {
                    indent ++;
                }
                
                if (indent < 0) {
                    indent = 0;
                }
                
                int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);

                // Insert whitespace on empty lines too -- needed for abbreviations expansion
                if (lineBegin != -1 || indentEmptyLines) {
                    // Don't do a hanging indent if we're already indenting beyond the parent level?
                    
                    indents.add(Integer.valueOf(indent));
                    offsets.add(Integer.valueOf(offset));
                }



                if (lineBegin != -1) {
                    balance += getTokenBalance(doc, lineBegin, endOfLine, true);
                    bracketBalance += getTokenBalance(doc, lineBegin, endOfLine, false);
                    continued = isLineContinued(doc, offset, bracketBalance);
                }

                offset = endOfLine;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }
    
    void reformatComments(BaseDocument doc, int start, int end) {
        int rightMargin = rightMarginOverride != -1 ? rightMarginOverride : codeStyle.getRightMargin();

//        ReflowParagraphAction action = new ReflowParagraphAction();
//        action.reflowComments(doc, start, end, rightMargin);
    }

}
