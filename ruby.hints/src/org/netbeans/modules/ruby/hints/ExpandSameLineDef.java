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
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.NodeTypes;
import org.jruby.lexer.yacc.ISourcePosition;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.GsfTokenId;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.Formatter;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


/**
 * Hint which adds a fix to lines containing a "single-line" definition
 * of a method or a class, and offers to expand it into a multi-line
 * definition, e.g. replacing
 * <pre>
 *    def foo; bar; end
 * </pre>
 * with
 * <pre>
 *    def foo
 *      bar
 *    end
 * </pre>
 * <p>
 * NOTE - this hint is only activated for the line under the caret!
 *
 * @todo Filter out the case where you have a def inside a class on the same line!
 * 
 * @author Tor Norbye
 */
public class ExpandSameLineDef implements AstRule {

    public Set<Integer> getKinds() {
        Set<Integer> integers = new HashSet<Integer>();
        integers.add(NodeTypes.CLASSNODE);
        integers.add(NodeTypes.DEFNNODE);
        integers.add(NodeTypes.DEFSNODE);
        return integers;
    }

    public void run(CompilationInfo info, Node node, AstPath path, int caretOffset, List<Description> result) {
        // Look for use of deprecated fields
        if (node.nodeId == NodeTypes.DEFNNODE || node.nodeId == NodeTypes.DEFSNODE || node.nodeId == NodeTypes.CLASSNODE) {
            ISourcePosition pos = node.getPosition();
            try {
                BaseDocument doc = (BaseDocument)info.getDocument();
                
                if (doc == null) {
                    // Run on a file that was just closed
                    return;
                }
                
                if (Utilities.getRowEnd(doc, pos.getStartOffset()) == Utilities.getRowEnd(doc, pos.getEndOffset())) {
                    // Block is on a single line
                    // TODO - add a hint to turn off this hint?
                    // Should be a utility or infrastructure option!
                    Node root = AstUtilities.getRoot(info);
                    if (path.leaf() != node) {
                        path = new AstPath(root, node);
                    }
                    List<Fix> fixList = Collections.<Fix>singletonList(new ExpandLineFix(info, path));

                    OffsetRange range = new OffsetRange(pos.getStartOffset(), pos.getEndOffset());
                    Description desc = new Description(this, getDisplayName(), info.getFileObject(), range, fixList);
                    result.add(desc);
                    
                    // Exit; don't process children such that a def inside a class all
                    // on the same line only produces a single suggestion for the outer block
                    return;
                }
            }
            catch (BadLocationException ex){
                Exceptions.printStackTrace(ex);
            }
            catch (IOException ex){
                Exceptions.printStackTrace(ex);
            }
        }
    }


    public void cancel() {
        // Does nothing
    }

    public String getId() {
        return "Expand_Same_Line_Def"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ExpandSameLineDef.class, "ExpandLine");
    }

    public String getDescription() {
        return NbBundle.getMessage(ExpandSameLineDef.class, "ExpandLineDesc");
    }

    private static class ExpandLineFix implements Fix {

        private CompilationInfo info;

        private AstPath path;

        ExpandLineFix(CompilationInfo info, AstPath path) {
            this.info = info;
            this.path = path;
        }

        public String getDescription() {
            String code = path.leaf().nodeId == NodeTypes.DEFNNODE ? "def" : "class";
            return NbBundle.getMessage(ExpandSameLineDef.class, "ExpandLineFix", code);
        }
        
        private void findLineBreaks(Node node, Set<Integer> offsets) {
            if (node.nodeId == NodeTypes.NEWLINENODE) {
                offsets.add(node.getPosition().getStartOffset());
            }
            @SuppressWarnings(value = "unchecked")
            List<Node> list = node.childNodes();

            for (Node child : list) {
                findLineBreaks(child, offsets);
            }
        }

        /** 
         * Try to split a line like
         *   class FooController; def rescue_action(e) raise e end; end 
         * into multiple lines. We can use lexical tokens like ";" as a clue
         * to where to put newlines, but we want to use the AST too such that
         * we see that we need a newline between the argument (e) and raise in the
         * above line.
         * <p>
         * By using both we'll get some offsets in the same area so we'll need
         * to be careful when applying our ;-to-\n replacements and our \n insertions
         * so we don't get multiple newlines for places where both the AST and
         * the semicolons suggest we need newlines.
         */
        public void implement() throws Exception {
            BaseDocument doc = (BaseDocument)info.getDocument();
            ISourcePosition pos = path.leaf().getPosition();
            int startOffset = pos.getStartOffset();
            int endOffset = pos.getEndOffset();
            if (endOffset > doc.getLength()) {
                if (startOffset > doc.getLength()) {
                    startOffset = doc.getLength();
                }
                endOffset = doc.getLength();
            }
            
            // Look through the document and find the statement separators (;);
            // at these locations I'll replace the ; with a newline and then
            // apply a formatter
            Set<Integer> offsetSet = new HashSet<Integer>();
            findLineBreaks(path.leaf(), offsetSet);
            
            // Add in ; replacements
            TokenSequence<?extends GsfTokenId> ts = LexUtilities.getRubyTokenSequence(doc, endOffset);
            if (ts != null) {
                // Traverse sequence in reverse order such that my offset list is in decreasing order
                ts.move(endOffset);
                while (ts.movePrevious() && ts.offset() > startOffset) {
                    Token<?extends GsfTokenId> token = ts.token();
                    TokenId id = token.id();

                    if (id == RubyTokenId.IDENTIFIER && ";".equals(token.text().toString())) { // NOI18N
                        offsetSet.add(ts.offset());
                    } else if (id == RubyTokenId.CLASS || id == RubyTokenId.DEF || id == RubyTokenId.END) {
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
                    for (int offset : offsets) {
                        // We might get some dupes since we add offsets from both
                        // the AST newline nodes and semicolons discovered in the lexical token hierarchy
                        if (offset == prev) {
                            continue;
                        }
                        prev = offset;

                        if (";".equals(doc.getText(offset, 1))) { // NOI18N
                            doc.remove(offset, 1);
                        }
                        if (offset < doc.getLength()-2) {
                            char c = doc.getText(offset+1,1).charAt(0);
                            if (c == '\n') {
                                continue;
                            } else if (Character.isWhitespace(c)) {
                                if (offset < doc.getLength()-3) {
                                    c = doc.getText(offset+2,1).charAt(0);
                                    if (c == '\n') {
                                        continue;
                                    }
                                }                                
                            }
                        }
                        doc.insertString(offset, "\n", null); // NOI18N
                    }
                    // Finally, reformat
                    new Formatter().reindent(doc, startOffset, endOffset, null, null);
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                } finally {
                    doc.atomicUnlock();
                }
            }
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return false;
        }
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
}