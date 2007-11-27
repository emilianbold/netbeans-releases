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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.jruby.ast.IArgumentNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.YieldNode;
import org.jruby.lexer.yacc.ISourcePosition;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.ParserFile;
import org.netbeans.api.gsf.ParserResult;
import org.netbeans.api.gsf.SourceFileReader;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.Formatter;
import org.netbeans.modules.ruby.RubyParser;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.spi.gsf.DefaultParseListener;
import org.netbeans.spi.gsf.DefaultParserFile;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Offer to convert a {}-style block into do-end, or vice versa
 *
 * @author Tor Norbye
 */
public class ConvertBlockType implements AstRule {

    public ConvertBlockType() {
    }

    public boolean appliesTo(CompilationInfo info) {
        // Skip for RHTML files for now - isn't implemented properly
        return info.getFileObject().getMIMEType().equals("text/x-ruby");
    }

    public Set<Integer> getKinds() {
        return Collections.singleton(NodeTypes.ITERNODE);
    }

    public void run(CompilationInfo info, Node node, AstPath path, int caretOffset, List<Description> result) {
        assert (node.nodeId == NodeTypes.ITERNODE);
        try {
            int astOffset = node.getPosition().getStartOffset();
            int lexOffset = LexUtilities.getLexerOffset(info, astOffset);
            BaseDocument doc = (BaseDocument) info.getDocument();
            if (lexOffset == -1 || lexOffset > doc.getLength() - 1) {
                return;
            }

            // Limit the hint to the -opening- line of the block
            boolean caretOnStart = true;
            final int beginRowEnd = Utilities.getRowEnd(doc, lexOffset);
            final int caretRowEnd = Utilities.getRowEnd(doc, caretOffset);
            boolean caretLine = beginRowEnd == caretRowEnd;
            int endLexOffset = -1;
            if (!caretLine) {
                // ...or the -ending- line of the block
                int endAstOffset = node.getPosition().getEndOffset();
                endLexOffset = LexUtilities.getLexerOffset(info, endAstOffset);
                if (endLexOffset == -1) {
                    return;
                }
                int endRowEnd = endLexOffset;
                if (endRowEnd < doc.getLength()) {
                    endRowEnd = Utilities.getRowEnd(doc, endLexOffset);
                }
                caretLine = endRowEnd == caretRowEnd;
                if (!caretLine) {
                    return;
                }
                if (endRowEnd != beginRowEnd) {
                    caretOnStart = false;
                }
            }

            Token<? extends GsfTokenId> token = LexUtilities.getToken(doc, lexOffset);
            if (token == null) {
                return;
            }
            
            TokenId id = token.id();
            if (id == RubyTokenId.LBRACE || id == RubyTokenId.DO) {
                OffsetRange range;
                if (caretOnStart) {
                    range = new OffsetRange(lexOffset, lexOffset + token.length());
                } else {
                    assert endLexOffset != -1;
                    int len = (id == RubyTokenId.LBRACE) ? 1 : 3; // }=1, end=3
                    range = new OffsetRange(endLexOffset-len, endLexOffset);
                }
                List<Fix> fixList = new ArrayList<Fix>(1);
                boolean convertFromBrace = id == RubyTokenId.LBRACE;

                int endOffset = node.getPosition().getEndOffset();
                if (endOffset > doc.getLength()) {
                    endOffset = doc.getLength();
                }

                // See if we should offer to collapse
                String text = doc.getText(lexOffset, endOffset - lexOffset);
                int nonspaceChars = 0;
                for (int i = 0; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if (!Character.isWhitespace(c)) {
                        nonspaceChars++;
                    }
                }
                int startColumn = lexOffset - Utilities.getRowStart(doc, lexOffset);
                // Not yet exposed from the Ruby module
                //int rightMargin = org.netbeans.modules.ruby.options.CodeStyle.getDefault(null).getRightMargin();
                // #119151: This should be available for a lot of hints that don't neatly fit.
                // So only suppress it for -really- large blocks.
                int rightMargin = 350;
                boolean offerCollapse = rightMargin > startColumn + nonspaceChars;

                // TODO - in an RHTML page, make sure there are no "gaps" (non Ruby code) between the do and the end,
                // since we can't handle those for collapse
                // TODO
                
                boolean sameLine = Utilities.getRowEnd(doc, lexOffset) == Utilities.getRowEnd(doc, endOffset);
                if (sameLine && convertFromBrace) {
                    fixList.add(new ConvertTypeFix(info, node, convertFromBrace, !convertFromBrace, true, false));
                } else if (!sameLine && !convertFromBrace && offerCollapse) {
                    fixList.add(new ConvertTypeFix(info, node, convertFromBrace, !convertFromBrace, false, true));
                } // else: Should I let you expand a single line do-end to a multiline {}, or vice versa? Naeh,
                // they can do this in two steps; it's not common
                fixList.add(new ConvertTypeFix(info, node, convertFromBrace, !convertFromBrace, false, false));
                if (sameLine || (!sameLine && offerCollapse)) {
                    fixList.add(new ConvertTypeFix(info, node, false, false, sameLine, !sameLine));
                }
                Description desc = new Description(this, getDisplayName(), info.getFileObject(), range, fixList, 500);
                result.add(desc);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public String getId() {
        return "Convert_Blocktype"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ConvertBlockType.class, "ConvertBlockType");
    }

    public String getDescription() {
        return NbBundle.getMessage(ConvertBlockType.class, "ConvertBlockTypeDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    public boolean showInTasklist() {
        return false;
    }

    private static class ConvertTypeFix implements Fix {

        private final CompilationInfo info;
        private final boolean convertToDo;
        private final boolean convertToBrace;
        private final Node node;
        private final boolean expand;
        private final boolean collapse;

        ConvertTypeFix(CompilationInfo info, Node node, 
                boolean convertToDo, boolean convertToBrace,
                boolean expand, boolean collapse) {
            this.info = info;
            this.node = node;
            this.convertToDo = convertToDo;
            this.convertToBrace = convertToBrace;
            this.expand = expand;
            this.collapse = collapse;
        }

        public String getDescription() {
            String key;
            if (convertToDo) {
                if (expand) {
                    key = "ConvertBraceToDoMulti"; // NOI18N
                } else if (collapse) {
                    key = "ConvertBraceToDoSingle"; // NOI18N
                } else {
                    key = "ConvertBraceToDo"; // NOI18N
                }
            } else if (convertToBrace) {
                if (expand) {
                    key = "ConvertDoToBraceMulti"; // NOI18N
                } else if (collapse) {
                    key = "ConvertDoToBraceSingle"; // NOI18N
                } else {
                    key = "ConvertDoToBrace"; // NOI18N
                }
            } else {
                if (expand) {
                    key = "ChangeBlockToMulti"; // NOI18N
                } else {
                    assert collapse;
                    key = "ChangeBlockToSingle"; // NOI18N
                }
            }
            return NbBundle.getMessage(ConvertBlockType.class, key);
        }

        public void implement() throws Exception {
            ISourcePosition pos = node.getPosition();
            int startOffset = pos.getStartOffset();
            int endOffset;
            if (convertToDo) {
                endOffset = pos.getEndOffset() - 1;
            } else if (convertToBrace) {
                endOffset = pos.getEndOffset() - 3;
            } else {
                endOffset = pos.getEndOffset();
            }
            BaseDocument doc = (BaseDocument) info.getDocument();
            if (startOffset > doc.getLength() - 1 || endOffset > doc.getLength()) {
                return;
            }

            if (convertToDo) {
                if (doc.getText(startOffset, 1).charAt(0) == '{' && doc.getText(endOffset, 1).charAt(0) == '}') {
                    try {
                        String end;
                        if (endOffset > 0 && !Character.isWhitespace(doc.getText(endOffset - 1, 1).charAt(0))) {
                            end = " end"; // NOI18N
                        } else {
                            end = "end"; // NOI18N
                        }
                        doc.atomicLock();
                        doc.replace(endOffset, 1, end, null); // NOI18N
                        int newEnd = endOffset + end.length() - 1;

                        boolean spaceBefore = true;
                        boolean spaceAfter = true;
                        int newStart = startOffset;
                        if (startOffset > 0) {
                            String s = doc.getText(startOffset - 1, 3);
                            spaceBefore = Character.isWhitespace(s.charAt(0));
                            spaceAfter = Character.isWhitespace(s.charAt(2));
                        }
                        String insert = "do";
                        if (!spaceAfter) {
                            insert = insert + " ";
                        }
                        if (!spaceBefore) {
                            insert = " " + insert;
                            newStart++;
                            newEnd++;
                        }
                        doc.replace(startOffset, 1, insert, null); // NOI18N
                        newEnd += insert.length() - 1;
                        newEnd++;

                        int blockBegin = newStart;
                        if (expand) {
                            Node newNode = updateParse(doc, blockBegin);
                            if (newNode != null) {
                                expand(doc, newNode, newStart, newEnd);
                            }
                        } else if (collapse) {
                            Node newNode = updateParse(doc, blockBegin);
                            if (newNode != null) {
                                collapse(doc, newNode, newStart, newEnd);
                            }
                        }
                    } finally {
                        doc.atomicUnlock();
                    }
                }
            } else if (convertToBrace) {
                if (doc.getText(startOffset, 2).equals("do") && endOffset <= doc.getLength() - 3 && // NOI18N
                        doc.getText(endOffset, 3).equals("end")) { // NOI18N
                    try {
                        // TODO - make sure there is whitespace next to these tokens!!!
                        // They are optional around {} but not around do/end!
                        
                        AstPath path = new AstPath(AstUtilities.getRoot(info), node);
                        assert path.leaf() == node;
                        boolean parenIsNecessary = isArgParenNecessary(path, doc);
                        
                        doc.atomicLock();
                        doc.replace(endOffset, 3, "}", null); // NOI18N
                        doc.replace(startOffset, 2, "{", null); // NOI18N
                        int newStart = startOffset;
                        int newEnd = endOffset + 3;
                        newEnd -= (3 - 1);
                        newEnd -= (2 - 1);

                        if (parenIsNecessary) {
                            // Insert parentheses
                            assert AstUtilities.isCall(path.leafParent());
                            OffsetRange range = AstUtilities.getCallRange(path.leafParent());
                            int insertPos = range.getEnd();
                            // Check if I should remove a space; e.g. replace "foo arg" with "foo(arg"
                            if (Character.isWhitespace(doc.getText(insertPos, 1).charAt(0))) {
                                doc.replace(insertPos, 1, "(", null); // NOI18N
                            } else {
                                doc.insertString(insertPos, "(", null); // NOI18N
                                newStart++;
                                newEnd++;
                            }
                            
                            // Insert )
                            doc.insertString(newStart-1, ")", null); // NOI18N
                            newStart++;
                            newEnd++;
                            
                            if (!Character.isWhitespace(doc.getText(newStart-1, 1).charAt(0))) {
                                doc.insertString(newStart-1, " ", null); // NOI18N
                                newStart++;
                                newEnd++;
                            }
                        }

                        if (expand) {
                            Node newNode = updateParse(doc, newStart);
                            if (newNode != null) {
                                expand(doc, newNode, newStart, newEnd);
                            }
                        } else if (collapse) {
                            Node newNode = updateParse(doc, newStart);
                            if (newNode != null) {
                                collapse(doc, newNode, newStart, newEnd);
                            }
                        }
                    } finally {
                        doc.atomicUnlock();
                    }
                }
            } else {
                assert collapse || expand;
                try {
                    // TODO - make sure there is whitespace next to these tokens!!!
                    // They are optional around {} but not around do/end!
                    doc.atomicLock();

                    if (expand) {
                        expand(doc, node, startOffset, endOffset);
                    } else {
                        collapse(doc, node, startOffset, endOffset);
                    }
                } finally {
                    doc.atomicUnlock();
                }
            }
        }
        
        /** JRuby sometimes has wrong AST offsets. For example, for 
         * this IterNode
         * sort{|a1, a2| a1[0].id2name <=> a2[0].id2name}
         * the NewlineNode inside the iter will be here: a1^[0] instead of ^a1[0].
         * To work around this problem, look at the left most children of a NewlineNode
         * and find the TRUE starting range of the newline node.
         * @todo File JRuby issue
         */
        private int findRealStart(Node node) {
            int min = Integer.MAX_VALUE;
            while (true) {
                int start = node.getPosition().getStartOffset();
                if (node.nodeId == NodeTypes.YIELDNODE) {
                    // Yieldnodes sometimes have the wrong offsets - see testHintFix19
                    // as well as highlightExitPoints in OccurrencesFinder for more
                    try {
                        OffsetRange range = AstUtilities.getYieldNodeRange((YieldNode)node, 
                                (BaseDocument)info.getDocument());
                        if (range != OffsetRange.NONE) {
                            start = range.getStart();
                        }
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }

                if (start < min) {
                    min = start;
                }

                @SuppressWarnings(value = "unchecked")
                List<Node> list = node.childNodes();

                if (list != null && list.size() > 0) {
                    node = list.get(0);
                } else {
                    return min;
                }
            }
        }

        private void findLineBreaks(Node node, Set<Integer> offsets) {
            if (node.nodeId == NodeTypes.NEWLINENODE) {
                // Doesn't work, need above workaround
                //int start = node.getPosition().getStartOffset();
                int start = findRealStart(node);
                offsets.add(start);
            }

            @SuppressWarnings(value = "unchecked")
            List<Node> list = node.childNodes();

            for (Node child : list) {
                if (child.nodeId == NodeTypes.EVSTRNODE) {
                    // Don't linebreak inside a #{} expression
                    continue;
                }
                findLineBreaks(child, offsets);
            }
        }

        /** NOTE - document should be under atomic lock when this is called */
        private void expand(BaseDocument doc, Node node, int startOffset, int endOffset) {
            assert doc.isAtomicLock();
            assert endOffset <= doc.getLength();

            // Look through the document and find the statement separators (;);
            // at these locations I'll replace the ; with a newline and then
            // apply a formatter
            Set<Integer> offsetSet = new HashSet<Integer>();
            findLineBreaks(node, offsetSet);

            // Add in ; replacements
            TokenSequence<? extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, endOffset);
            if (ts != null) {
                // Traverse sequence in reverse order such that my offset list is in decreasing order
                ts.move(endOffset);
                while (ts.movePrevious() && ts.offset() > startOffset) {
                    Token<? extends GsfTokenId> token = ts.token();
                    TokenId id = token.id();

                    if (id == RubyTokenId.IDENTIFIER && ";".equals(token.text().toString())) { // NOI18N
                        //offsetSet.add(ts.offset());
                    } else if (id == RubyTokenId.END || id == RubyTokenId.RBRACE) {
                        offsetSet.add(ts.offset());
                    }
                }
            }

            List<Integer> offsets = new ArrayList<Integer>(offsetSet);
            Collections.sort(offsets);
            // Ensure that we go in high to lower order such that I edit the
            // document from bottom to top (so offsets don't have to be adjusted
            // to account for our own edits along the way)
            Collections.reverse(offsets);

            if (offsets.size() > 0) {
                // TODO: Create a ModificationResult here and process it
                // The following is the WRONG way to do it...
                // I've gotta use a ModificationResult instead!
                try {
                    doc.atomicLock();
                    // Process offsets from back to front such that I can
                    // modify the document without worrying that the other offsets
                    // need to be adjusted
                    int prev = -1;
                    int added = 0;
                    for (int offset : offsets) {
                        // We might get some dupes since we add offsets from both
                        // the AST newline nodes and semicolons discovered in the lexical token hierarchy
                        if (offset == prev) {
                            continue;
                        }
                        prev = offset;
                        doc.insertString(offset, "\n", null); // NOI18N
                        added++;
                    }

                    // Remove trailing semicolons
                    int newEnd = endOffset + added;
                    for (int offset = startOffset; offset < newEnd && offset < doc.getLength(); offset = Utilities.getRowEnd(doc, offset) + 1) {
                        int lineEnd = Utilities.getRowLastNonWhite(doc, offset);
                        if (lineEnd != -1 && doc.getText(lineEnd, 1).charAt(0) == ';') {
                            doc.remove(lineEnd, 1);
                            added--;
                            newEnd--;
                        }
                    }

                    // Remove trailing whitespace
                    stripTrailingWhitespace(doc, startOffset, newEnd);
                    
                    // Finally, reformat
                    new Formatter().reindent(doc, startOffset, newEnd, null, null);
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                } finally {
                    doc.atomicUnlock();
                }
            }
        }
        
        /** Remove trailing whitespace for lines in the given range */
        private void stripTrailingWhitespace(BaseDocument doc, int startOffset, int endOffset) throws BadLocationException {
            startOffset = Utilities.getRowStart(doc, startOffset);
            endOffset = Utilities.getRowEnd(doc, endOffset);
            for (int offset = endOffset; offset > startOffset; offset = Utilities.getRowStart(doc, offset)-1 ) {
                if (Utilities.isRowEmpty(doc, offset) || (Utilities.isRowWhite(doc, offset))) {
                    continue;
                }
                
                int spaceBegin = Utilities.getRowLastNonWhite(doc, offset)+1;
                int rowEnd = Utilities.getRowEnd(doc, offset); // Should always equal offset? Check on Windows with \r\n stuff
                if (spaceBegin < rowEnd) {
                    doc.remove(spaceBegin, rowEnd-spaceBegin);
                }
            }
        }

        private void collapse(BaseDocument doc, Node node, int startOffset, int endOffset) {
            assert doc.isAtomicLock();
            assert endOffset <= doc.getLength();

            // Look through the document and find the statement separators (;);
            // at these locations I'll replace the ; with a newline and then
            // apply a formatter
            Set<Integer> offsetSet = new HashSet<Integer>();
            findLineBreaks(node, offsetSet);

            Token<? extends TokenId> t = LexUtilities.getToken(doc, startOffset);
            TokenId tid = t.id();
            assert tid == RubyTokenId.LBRACE || tid == RubyTokenId.DO;
            boolean isDoBlock = tid == RubyTokenId.DO;

            // Add in ; replacements
            TokenSequence<? extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, endOffset);
            if (ts != null) {
                // Traverse sequence in reverse order such that my offset list is in decreasing order
                ts.move(endOffset);
                while (ts.movePrevious() && ts.offset() > startOffset) {
                    Token<? extends GsfTokenId> token = ts.token();
                    TokenId id = token.id();

                    if (id == RubyTokenId.END || id == RubyTokenId.RBRACE) {
                        offsetSet.add(ts.offset());
                    }
                }
            }

            List<Integer> offsets = new ArrayList<Integer>(offsetSet);
            Collections.sort(offsets);
            // Ensure that we go in high to lower order such that I edit the
            // document from bottom to top (so offsets don't have to be adjusted
            // to account for our own edits along the way)
            //Collections.reverse(offsets);
            if (offsets.size() > 0) {
                // TODO: Create a ModificationResult here and process it
                // The following is the WRONG way to do it...
                // I've gotta use a ModificationResult instead!
                try {
                    doc.atomicLock();
                    // Process offsets from back to front such that I can
                    // modify the document without worrying that the other offsets
                    // need to be adjusted
                    int prev = -1;
                    int added = 0;
                    //int posDelta; // Amount to add to offsets to account for our
                    for (int i = offsets.size() - 1; i >= 0; i--) {
                        int offset = offsets.get(i);
                        // We might get some dupes since we add offsets from both
                        // the AST newline nodes and semicolons discovered in the lexical token hierarchy
                        if (offset == prev) {
                            continue;
                        }
                        prev = offset;
                        int prevOffset = i > 0 ? offsets.get(i - 1) : 0;

                        int segmentOffset = offset;
                        // TODO - use an editor-finder which can do this efficiently
                        // See also DocumentUtilities.getText() which can do it efficiently
                        int s = segmentOffset;
                        while (s > prevOffset) {
                            s--;
                            char c = doc.getText(s, 1).charAt(0);
                            if (Character.isWhitespace(c)) {
                                segmentOffset = s;
                            } else {
                                break;
                            }
                        }
                        int segmentLength = offset - segmentOffset;
                        s = offset - 1;
                        while (s < doc.getLength()) {
                            s++;
                            char c = doc.getText(s, 1).charAt(0);
                            if (Character.isWhitespace(c)) {
                                segmentLength++;
                            } else {
                                break;
                            }
                        }

                        // Collapse all whitespace around this offset and replace with a single "; "
                        char prevChar = '?';
                        if (segmentOffset > 0) {
                            prevChar = doc.getText(segmentOffset-1, 1).charAt(0);
                        }
                        if (prevChar == '|' || (isDoBlock && (segmentOffset <= startOffset + 3) || (!isDoBlock && (segmentOffset <= startOffset + 1)))) {
                            doc.replace(segmentOffset, segmentLength, " ", null); // NOI18N
                            added += 1 - segmentLength;
                        } else {
                            doc.replace(segmentOffset, segmentLength, "; ", null); // NOI18N
                            added += 2 - segmentLength;
                        }
                    }

                    // Remove final ";" prior to "end"
                    endOffset += added;
                    if (isDoBlock) {
                        // Back up over "end"
                        endOffset -= 3;
                    } else {
                        // Back up over "}
                        endOffset -= 1;
                    }
                    // Back up over "; "
                    endOffset -= 2;
                    if (endOffset < doc.getLength()) {
                        char c = doc.getText(endOffset, 1).charAt(0);
                        if (c == ';') {
                            doc.remove(endOffset, 1);
                        }
                    }
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                } finally {
                    doc.atomicUnlock();
                }
            }
        }

        private Node updateParse(BaseDocument doc, int blockBegin) {
            Node root = reparse(info.getFileObject(), doc);
            if (root != null) {
                // Find the same node
                AstPath path = new AstPath(root, blockBegin);
                Node leaf = path.leaf();
                if (leaf != null && leaf.nodeId == NodeTypes.ITERNODE) {
                    return leaf;
                }
            }

            return null;
        }
        
        /** Determine whether parentheses are necessary around the call
         * corresponding to a block call.
         * For example, in 
         * <pre>
         *  b.create_menu :name => 'default_menu' do |d| ...
         * </pre>
         * parens are necessary if you want to switch to a brace block.
         */
        private boolean isArgParenNecessary(AstPath path, BaseDocument doc) throws BadLocationException {
            // Look at the surrounding CallNode and see if it has arguments.
            // If so, see if it has parens. If not, return true.
            assert path.leaf().nodeId == NodeTypes.ITERNODE;
            Node n = path.leafParent();
            if (n != null && AstUtilities.isCall(n) && n instanceof IArgumentNode && 
                    ((IArgumentNode)n).getArgsNode() != null) {
                // Yes, call has args - check parens
                int end = node.getPosition().getStartOffset(); // Start of do/{ - end of args
                for (int i = end-1; i >= 0 && i < doc.getLength(); i--) {
                    // XXX Use a more performant document content iterator!
                    char c = doc.getText(i, 1).charAt(0);
                    if (Character.isWhitespace(c)) {
                        continue;
                    }
                    if (c == ')') {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
            
            return false;
        }

        public boolean isSafe() {
            // Different precedence rules apply for do and {}
            return !convertToBrace && !convertToDo;
        }

        public boolean isInteractive() {
            return false;
        }

        // TODO - move to AstUtilities
        public static Node reparse(final FileObject fo, final BaseDocument doc) {
            ParserFile file = new DefaultParserFile(fo, null, false);

            if (file == null) {
                return null;
            }

            List<ParserFile> files = Collections.singletonList(file);
            SourceFileReader reader = new SourceFileReader() {

                public CharSequence read(ParserFile file) throws IOException {

                    if (doc == null) {
                        return "";
                    }

                    try {
                        return doc.getText(0, doc.getLength());
                    } catch (BadLocationException ble) {
                        IOException ioe = new IOException();
                        ioe.initCause(ble);
                        throw ioe;
                    }
                }

                public int getCaretOffset(ParserFile fileObject) {
                    return -1;
                }
            };

            DefaultParseListener listener = new DefaultParseListener();
            new RubyParser().parseFiles(files, listener, reader);

            ParserResult result = listener.getParserResult();

            if (result == null) {
                return null;
            }

            Node root = AstUtilities.getRoot(result);

            return root;
        }
    }
}
