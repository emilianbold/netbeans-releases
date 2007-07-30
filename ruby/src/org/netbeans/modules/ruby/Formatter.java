/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;

import org.jruby.ast.ArgumentNode;
import org.jruby.ast.CommentNode;
import org.jruby.ast.Node;
import org.jruby.ast.visitor.rewriter.DefaultFormatHelper;
import org.jruby.ast.visitor.rewriter.FormatHelper;
import org.jruby.ast.visitor.rewriter.ReWriteVisitor;
import org.jruby.ast.visitor.rewriter.ReWriterFactory;
import org.jruby.ast.visitor.rewriter.utils.ReWriterContext;
import org.netbeans.api.gsf.EditorOptions;
import org.netbeans.api.gsf.FormattingPreferences;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.util.Exceptions;


/**
 * Formatting and indentation for Ruby.
 * 
 * @todo Handle RHTML!
 * @todo Create unit tests
 * @todo Use in complete file reindentation, then diff with original formatted ruby source
 *    and see where I've gotta improve matters
 * @todo Use configuration object to pass in Ruby conventions
 * @todo Use the provided parse tree, if any, to for example check heredoc nodes
 *   and see if they are indentable.
 * @todo If you select a complete line, the endOffset is on a new line; adjust it back
 * @todo If line ends with \ I definitely have a line continuation!
 * @todo Reflow comment text? http://ruby.netbeans.org/issues/show_bug.cgi?id=11553

 *
 * @author Tor Norbye
 */
public class Formatter implements org.netbeans.api.gsf.Formatter {
    private static final int KEEP_INDENT = -1;

    public Formatter() {
    }
    

    public void reformat(Document document, ParserResult result, FormattingPreferences preferences,
        Caret caret) {
        // TODO - preserve caret position
        RubyParseResult parseResult = (RubyParseResult)result;

        if ((parseResult == null) || (parseResult.getRealRoot() == null)) {
            // just reindent instead
            reindent(document, 0, document.getLength(), result, preferences);

            return;
        }

        if (!parseResult.isCommentsAdded()) {
            new StructureAnalyzer().addComments(parseResult);
        }

        BaseDocument doc = (BaseDocument)document;

        StringWriter stringWriter = new StringWriter(document.getLength() * 2);
        PrintWriter output = new PrintWriter(stringWriter);
        String source = parseResult.getSource();
        FormatHelper formatHelper = new DefaultFormatHelper();
        ReWriterContext context = new ReWriterContext(output, source, formatHelper);
        ReWriterFactory factory = new ReWriterFactory(context);

        //ReWriteVisitor visitor = factory.createReWriteVisitor();
        ReWriteVisitor visitor = new CommentRewriter(context);

        Node root = parseResult.getRealRoot();
        root.accept(visitor);
        visitor.flushStream();

        String reformatted = stringWriter.toString();

        try {
            doc.atomicLock();
            doc.replace(0, doc.getLength(), reformatted, null);
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        } finally {
            doc.atomicUnlock();
        }
    }
    
    public int indentSize() {
        return 2;
    }
    
    public int hangingIndentSize() {
        return 2;
    }

    private static class CommentRewriter extends ReWriteVisitor {
        public CommentRewriter(ReWriterContext config) {
            super(config);
        }

        @Override
        public void visitNode(Node iVisited) {
            if (iVisited == null) {
                return;
            }

            printCommentsBefore(iVisited);

            if (iVisited instanceof ArgumentNode) {
                print(((ArgumentNode)iVisited).getName());
            } else {
                iVisited.accept(this);
            }

            printCommentsAfter(iVisited);
            config.setLastPosition(iVisited.getPosition());
        }

        private static int getEndLine(Node n) {
            return n.getPosition().getEndLine();
        }

        private static int getStartLine(Node n) {
            return n.getPosition().getStartLine();
        }

        //        protected void printNewlineAndIndentation() {
        //                if (config.hasHereDocument()) config.fetchHereDocument().print();
        //
        //                print('\n');
        //                config.getIndentor().printIndentation(config.getOutput());
        //        }
        private void printCommentsBefore(Node iVisited) {
            for (Iterator it = iVisited.getComments().iterator(); it.hasNext();) {
                CommentNode n = (CommentNode)it.next();

                if (getStartLine(n) < getStartLine(iVisited)) {
                    String comment = n.getContent();
                    visitNode(n);
                    print(comment);
                    printNewlineAndIndentation();
                }
            }
        }

        @Override
        protected boolean printCommentsAfter(Node iVisited) {
            boolean hasComment = false;

            for (Iterator it = iVisited.getComments().iterator(); it.hasNext();) {
                CommentNode n = (CommentNode)it.next();

                if (getStartLine(n) >= getEndLine(iVisited)) {
                    print(' ');
                    visitNode(n);

                    String comment = n.getContent();
                    print(comment);
                    hasComment = true;
                }
            }

            return hasComment;
        }
    }
    
    /** Compute the initial balance of brackets at the given offset. */
    private int getFormatStableStart(BaseDocument doc, int offset) {
        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, offset);
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
            Token<?extends GsfTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == RubyTokenId.CLASS || id == RubyTokenId.MODULE || id == RubyTokenId.DEF) {
                return ts.offset();
            }
        } while (ts.movePrevious());

        return ts.offset();
    }
    
    private int getTokenBalance(BaseDocument doc, int begin, int end) {
        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, begin);
        if (ts == null) {
            return 0;
        }

        ts.move(begin);

        if (!ts.moveNext()) {
            return 0;
        }

        int balance = 0;

        do {
            Token<?extends GsfTokenId> token = ts.token();
            TokenId id = token.id();

            if (LexUtilities.isBeginToken(id, doc, ts)) {
                balance++;
            } else if (id == RubyTokenId.END) {
                balance--;
            } else if (id == RubyTokenId.LPAREN || id == RubyTokenId.LBRACKET || id == RubyTokenId.LBRACE ||
                    // In some cases, the [ shows up as an identifier, for example in this expression:
                    //  for k, v in sort{|a1, a2| a1[0].id2name <=> a2[0].id2name}
                    (id == RubyTokenId.IDENTIFIER && (token.text().toString().equals("[")))) { // NOI18N
                balance++;
            } else if (id == RubyTokenId.RPAREN || id == RubyTokenId.RBRACKET || id == RubyTokenId.RBRACE) {
                balance--;
            }
        } while (ts.moveNext() && (ts.offset() < end));

        return balance;
    }

    private int getBracketBalance(BaseDocument doc, int begin, int end) {
        // Same as getTokenBalance, but limited to parentheses, brackets and braces
        TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, begin);
        if (ts == null) {
            return 0;
        }

        ts.move(begin);

        if (!ts.moveNext()) {
            return 0;
        }

        int balance = 0;

        do {
            Token<?extends GsfTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == RubyTokenId.LPAREN || id == RubyTokenId.LBRACKET || id == RubyTokenId.LBRACE ||
                    // In some cases, the [ shows up as an identifier, for example in this expression:
                    //  for k, v in sort{|a1, a2| a1[0].id2name <=> a2[0].id2name}
                    (id == RubyTokenId.IDENTIFIER && (token.text().toString().equals("[")))) { // NOI18N
                balance++;
            } else if (id == RubyTokenId.RPAREN || id == RubyTokenId.RBRACKET || id == RubyTokenId.RBRACE) {
                balance--;
            }
        } while (ts.moveNext() && (ts.offset() < end));

        return balance;
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
            Token<?extends GsfTokenId> token = LexUtilities.getToken(doc, pos);

            if (token != null) {
                TokenId id = token.id();
                // If we're in a string literal (or regexp or documentation) leave
                // indentation alone!
                if ((id == RubyTokenId.STRING_LITERAL) ||
                        id == RubyTokenId.DOCUMENTATION ||
                        (id == RubyTokenId.QUOTED_STRING_LITERAL) ||
                        (id == RubyTokenId.REGEXP_LITERAL)) {
                    // No indentation for literal strings in Ruby, since they can
                    // contain newlines. Leave it as is.
                    return true;
                }
                
                if (id == RubyTokenId.STRING_END || id == RubyTokenId.QUOTED_STRING_END) {
                    // Possibly a heredoc
                    TokenSequence<? extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, pos);
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
                // No ruby token -- leave the formatting alone!
                // (Probably an RHTML file)
                return true;
            }
        }

        return false;
    }

    private int getEndIndent(BaseDocument doc, int offset) throws BadLocationException {
        int lineBegin = Utilities.getRowFirstNonWhite(doc, offset);

        if (lineBegin != -1) {
            Token<?extends GsfTokenId> token = LexUtilities.getToken(doc, lineBegin);
            
            if (token == null) {
                return KEEP_INDENT;
            }
            
            TokenId id = token.id();

            // If the line starts with an end-marker, such as "end", "}", "]", etc.,
            // find the corresponding opening marker, and indent the line to the same
            // offset as the beginning of that line.
            OffsetRange begin = OffsetRange.NONE;
            if ((LexUtilities.isIndentToken(id) && !LexUtilities.isBeginToken(id, doc, offset)) || id == RubyTokenId.END) {
                TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, lineBegin);
                ts.move(lineBegin);
                ts.movePrevious();
                begin = LexUtilities.findBegin(doc, ts);
            } else if ((id == RubyTokenId.RBRACE)) {
                TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, lineBegin);
                ts.move(lineBegin);
                ts.movePrevious();
                begin = LexUtilities.findBwd(doc, ts, 
                        RubyTokenId.LBRACE, RubyTokenId.RBRACE);
            } else if (id == RubyTokenId.RBRACKET) {
                TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, lineBegin);
                ts.move(lineBegin);
                ts.movePrevious();
                begin = LexUtilities.findBwd(doc, ts, 
                        RubyTokenId.LBRACKET, RubyTokenId.RBRACKET);
            } else if (id == RubyTokenId.RPAREN) {
                TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, lineBegin);
                ts.move(lineBegin);
                ts.movePrevious();
                begin = LexUtilities.findBwd(doc, ts, 
                        RubyTokenId.LPAREN, RubyTokenId.RPAREN);
            }
            if (begin != OffsetRange.NONE) {
                return LexUtilities.getLineIndent(doc, begin.getStart());
            }
        }
        
        return KEEP_INDENT;
    }
    
    private boolean isLineContinued(BaseDocument doc, int offset, int bracketBalance) throws BadLocationException {
        offset = Utilities.getRowLastNonWhite(doc, offset);
        if (offset == -1) {
            return false;
        }

        Token<?extends GsfTokenId> token = LexUtilities.getToken(doc, offset);

        if (token != null) {
            TokenId id = token.id();
            
            boolean isContinuationOperator = id == RubyTokenId.NONUNARY_OP;
            
            if (token.length() == 1 && id == RubyTokenId.IDENTIFIER && token.text().toString().equals(",")) {
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
                    if (id == RubyTokenId.DEF || id == RubyTokenId.ANY_KEYWORD && token.text().toString().equals("alias")) { // NOI18N
                        return false;
                    }
                }

                return true;
            } else if (id == RubyTokenId.ANY_KEYWORD) {
                String text = token.text().toString();
                if ("or".equals(text) || "and".equals(text)) { // NOI18N
                    return true;
                }
            }
        }

        return false;
    }

    /** @todo Rewrite to handleposition inside adef to be way off.
     * This needs to be working to pass in handing indents as well!!!
     *  I need to 
     */
    public void reindent(Document document, int startOffset, int endOffset, ParserResult result,
        FormattingPreferences preferences) {
        try {
            BaseDocument doc = (BaseDocument)document; // document.getText(0, document.getLength())

            if (endOffset > doc.getLength()) {
                endOffset = doc.getLength();
            }
            
            startOffset = Utilities.getRowStart(doc, startOffset);
            int lineStart = startOffset;//Utilities.getRowStart(doc, startOffset);
            int initialOffset = 0;
            int initialIndent = 0;
            if (startOffset > 0) {
                int prevOffset = Utilities.getRowStart(doc, startOffset-1);
                initialOffset = getFormatStableStart(doc, prevOffset);
                initialIndent = LexUtilities.getLineIndent(doc, initialOffset);
            }
            
            // Build up a set of offsets and indents for lines where I know I need
            // to adjust the offset. I will then go back over the document and adjust
            // lines that are different from the intended indent. By doing piecemeal
            // replacements in the document rather than replacing the whole thing,
            // a lot of things will work better: breakpoints and other line annotations
            // will be left in place, semantic coloring info will not be temporarily
            // damaged, and the caret will stay roughly where it belongs.
            List<Integer> offsets = new ArrayList<Integer>();
            List<Integer> indents = new ArrayList<Integer>();

            // When we're formatting sections, include whitespace on empty lines; this
            // is used during live code template insertions for example. However, when
            // wholesale formatting a whole document, leave these lines alone.
            boolean indentEmptyLines = (startOffset != 0 || endOffset != doc.getLength());
            boolean inRhtml = RubyUtils.isRhtmlDocument(document);
            TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);

            boolean includeEnd = endOffset == doc.getLength();
            
            // TODO - remove initialbalance etc.
            computeIndents(doc, initialIndent, initialOffset, endOffset, result, preferences, 
                    offsets, indents, indentEmptyLines, includeEnd);
            
            try {
                doc.atomicLock();

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
                        int actualPrevIndent = LexUtilities.getLineIndent(doc, prevOffset);

                        indent = actualPrevIndent + (indent-prevIndent);
                    }

                    // Adjust the indent at the given line (specified by offset) to the given indent
                    int currentIndent = LexUtilities.getLineIndent(doc, lineBegin);

                    if (currentIndent != indent) {
                        if (inRhtml) {
                            // Don't mess with lines that aren't in Ruby blocks, or that start with
                            // HTML stuff
                            TokenSequence<?extends GsfTokenId> ts = th.tokenSequence(RubyTokenId.language());
                            if (ts != null) {
                                ts.move(lineStart);
                                if (ts.moveNext()) {
                                    // Look at the language at this location
                                    LanguagePath path = ts.languagePath();
                                    if (!(path.innerLanguage() == RubyTokenId.language() ||
                                            path.innerLanguage().mimeType().equals(RubyInstallation.RHTML_MIME_TYPE))) {
                                        // HTML etc. - skip
                                        continue;
                                    }
                                }
                            }
                        }
                        // In RHTML files 
                        
                        doc.getFormatter().changeRowIndent(doc, lineBegin, indent);
                    }
                }
            } finally {
                doc.atomicUnlock();
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }

    public void computeIndents(BaseDocument doc, int initialIndent, int startOffset, int endOffset, ParserResult result,
        FormattingPreferences preferences,
            List<Integer> offsets,
            List<Integer> indents,
            boolean indentEmptyLines, boolean includeEnd
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
            int end = endOffset; //Utilities.getRowEnd(doc, endOffset); // The line's end
            
            int indentSize = preferences != null ? preferences.getIndentation() : 2;
            int hangingIndentSize = preferences != null ? preferences.getHangingIndentation() : indentSize;
            // XXX Look up RHTML too
            //int indentSize = EditorOptions.get(RubyInstallation.RUBY_MIME_TYPE).getSpacesPerTab();
            //int hangingIndentSize = indentSize;
            

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

            while ((!includeEnd && offset < end) || (includeEnd && offset <= end)) {
                int indent; // The indentation to be used for the current line

                int hangingIndent = continued ? (hangingIndentSize) : 0;

                if (isInLiteral(doc, offset)) {
                    // Skip this line
                    indent = LexUtilities.getLineIndent(doc, offset);
                } else if ((indent = getEndIndent(doc, offset)) != KEEP_INDENT) {
                    indent = (balance-1) * indentSize + hangingIndent + initialIndent;
                } else {
                    indent = balance * indentSize + hangingIndent + initialIndent;
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

                int endOfLine = Utilities.getRowEnd(doc, offset) + 1;

                if (lineBegin != -1) {
                    balance += getTokenBalance(doc, lineBegin, endOfLine);
                    bracketBalance += getBracketBalance(doc, lineBegin, endOfLine);
                    continued = isLineContinued(doc, offset, bracketBalance);
                }

                offset = endOfLine;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }
    
    public void reflowParagraph(Document document, int offset) {
        BaseDocument doc = (BaseDocument)document;
        try {
            offset = Utilities.getRowFirstNonWhite(doc, offset);
            if (offset == -1) {
                // XXX be smarter about empty lines -- do previous line for example? Or next?
                return;
            }
            Token<? extends GsfTokenId> token = LexUtilities.getToken(doc, offset);
            if (token == null) {
                return;
            }
            
            if (token.id() != RubyTokenId.LINE_COMMENT) {
                // Currently only reflows comments
                // In RHTML I could reflow text too...
                
                // I could compute the surrounding block (e.g. look for empty lines
                // on both sides?, or {start}/end
                // blocks, and reindent that as a "paragraph"
                return;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }
}