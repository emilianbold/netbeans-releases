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

package org.netbeans.modules.javascript.editing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OccurrencesFinder;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tor Norbye
 */
public class JsOccurrenceFinder implements OccurrencesFinder {
    private boolean cancelled;
    private int caretPosition;
    private Map<OffsetRange, ColoringAttributes> occurrences;
    private FileObject file;

    public JsOccurrenceFinder() {
    }

    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return occurrences;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    public final synchronized void cancel() {
        cancelled = true;
    }

    public void setCaretPosition(int position) {
        this.caretPosition = position;
    }
    
    public void run(CompilationInfo info) {
        resume();

        if (isCancelled()) {
            return;
        }

        FileObject currentFile = info.getFileObject();
        if (currentFile != file) {
            // Ensure that we don't reuse results from a different file
            occurrences = null;
            file = currentFile;
        }

        JsParseResult rpr = AstUtilities.getParseResult(info);
        if (rpr == null) {
            return;
        }

        Node root = rpr.getRootNode();
        if (root == null) {
            return;
        }

        Map<OffsetRange, ColoringAttributes> highlights =
            new HashMap<OffsetRange, ColoringAttributes>(100);

        int astOffset = AstUtilities.getAstOffset(info, caretPosition);
        if (astOffset == -1) {
            return;
        }

        AstPath path = new AstPath(root, astOffset);
        Node closest = path.leaf();

        VariableVisitor v = rpr.getVariableVisitor();

        // When we sanitize the line around the caret, occurrences
        // highlighting can get really ugly
        OffsetRange blankRange = rpr.getSanitizedRange();

        if (blankRange.containsInclusive(astOffset)) {
            closest = null;
        }

        // JRuby sometimes gives me some "weird" sections. For example,
        // if you have
        //    obj.|
        //
        //    Scanf
        // rather than give a parse error on obj, it marks the whole region from
        // . to the end of Scanf as a CallNode, which is a weird highlight.
        // We don't want occurrences highlights that span lines.
        if (closest != null) {
            BaseDocument doc = (BaseDocument)info.getDocument();
            if (doc == null) {
                // Document was just closed
                return;
            }
            try {
                doc.readLock();
                int length = doc.getLength();
                OffsetRange astRange = AstUtilities.getRange(closest);
                OffsetRange lexRange = LexUtilities.getLexerOffsets(info, astRange);
                int lexStartPos = lexRange.getStart();
                int lexEndPos = lexRange.getEnd();

                // If the buffer was just modified where a lot of text was deleted,
                // the parse tree positions could be pointing outside the valid range
                if (lexStartPos > length) {
                    lexStartPos = length;
                }
                if (lexEndPos > length) {
                    lexEndPos = length;
                }

                // One special case I care about: highlighting method exit points. In
                // this case, the full def node is selected, which typically spans
                // lines. This should trigger if you put the caret on the method definition
                // line, unless it's in a comment there.
                org.netbeans.api.lexer.Token<?extends JsTokenId> token = LexUtilities.getToken(doc, caretPosition);
                boolean isFunctionKeyword = (token != null) && token.id() == JsTokenId.FUNCTION;
                boolean isMethodName = closest.getType() == Token.FUNCNAME;
                boolean isReturn = closest.getType() == Token.RETURN && astOffset < closest.getSourceStart() + "return".length();

                if (isFunctionKeyword || isMethodName || isReturn) {
                    // Highlight exit points
                    Node func = closest;
                    if (isFunctionKeyword) {
                        // Look inside the method - the offsets for function doesn't include the "function" keyword yet
                        // Hmm, perhaps it's easier to just fix that? XXX TODO
                        int offset =  astOffset+"function".length();
                        path = new AstPath(root, offset);
                        func = path.leaf();
                        if (func.getType() == Token.PARAMETER) {
                            func = func.getParentNode();
                        }
                    } else if (func.getType() == Token.FUNCNAME) {
                        func = func.getParentNode();
                    } else if (isReturn) {
                        Node f = func.getParentNode();
                        while (f != null && f.getType() != Token.FUNCTION) {
                            f = f.getParentNode();
                        }
                        if (f != null) {
                            func = f;
                        }
                    }
                    highlightExits(func, highlights, info);
                    // Fall through and set closest to null such that I don't do other highlighting
                    closest = null;
                } else if (closest.getType() == Token.CALL && 
                        lexStartPos != -1 && lexEndPos != -1 && 
                            Utilities.getRowStart(doc, lexStartPos) != Utilities.getRowStart(doc, lexEndPos)) {
                    // Some nodes may span multiple lines, but the range we care about is only
                    // on a single line because we're pulling out the lvalue - for example,
                    // a method call may span multiple lines because of a long parameter list,
                    // but we only highlight the methodname itself
                    closest = null;
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            } finally {
                doc.readUnlock();
            }
        }

        if (closest != null) {
            if (AstUtilities.isNameNode(closest)) {
                String name = closest.getString();
                List<Node> nodes = v.getVarOccurrences(closest);
                if (nodes != null) {
                    for (Node node : nodes) {
                        OffsetRange range = AstUtilities.getNameRange(node);
                        highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
                    }
                } else {
                    Node scopeNode = v.getDefiningScope(closest);
                    addNodes(scopeNode != null ? scopeNode : root, name, highlights);
                }
                closest = null;
            }
        }

        if (isCancelled()) {
            return;
        }

        if (highlights.size() > 0) {
            if (rpr.getTranslatedSource() != null) {
                Map<OffsetRange, ColoringAttributes> translated = new HashMap<OffsetRange,ColoringAttributes>(2*highlights.size());
                for (Map.Entry<OffsetRange,ColoringAttributes> entry : highlights.entrySet()) {
                    OffsetRange range = LexUtilities.getLexerOffsets(info, entry.getKey());
                    if (range != OffsetRange.NONE) {
                        translated.put(range, entry.getValue());
                    }
                }
                
                highlights = translated;
            }

            this.occurrences = highlights;
        } else {
            this.occurrences = null;
        }
    }

    private void addNodes(Node node, String name, Map<OffsetRange, ColoringAttributes> highlights) {
        if (AstUtilities.isNameNode(node)) {
            // TODO - don't get confused about method names versus variables that we're looking for
            if (name.equals(node.getString())) {
                OffsetRange range = AstUtilities.getNameRange(node);
                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
            }
        } 
        
        if (node.hasChildren()) {
            Node child = node.getFirstChild();

            for (; child != null; child = child.getNext()) {
                addNodes(child, name, highlights);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void highlightExits(Node node,
        Map<OffsetRange, ColoringAttributes> highlights, CompilationInfo info) {

        if (node.hasChildren()) {
            Node child = node.getFirstChild();

            for (; child != null; child = child.getNext()) {
                highlightExitPoints(child, highlights, info);
            }
        }

        // TODO: Find the last statement, and highlight it.
        // Be careful not to highlight the entire statement (which could be a giant if
        // statement spanning the whole screen); just pick the first line.
        
// Highlighting the last statement is disabled because statement offsets are generally
// not valid yet. Fix that first.
//        Node last = node.getLastChild();
//        if (last == null || last.getType() == Token.PARAMETER || last.getType() == Token.FUNCNAME) {
//            return;
//        }
//        
//        try {
//            BaseDocument doc = (BaseDocument)info.getDocument();
//
//            if (Utilities.getRowStart(doc, last.getSourceStart()) != Utilities.getRowStart(doc,
//                        last.getSourceEnd())) {
//                // Highlight the first line - where the nonwhitespace is
//                int begin = Utilities.getRowFirstNonWhite(doc, last.getSourceStart());
//                int end = Utilities.getRowLastNonWhite(doc, last.getSourceEnd());
//
//                if ((begin != -1) && (end != -1)) {
//                    OffsetRange range = new OffsetRange(begin, end + 1);
//                    highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
//                }
//            } else {
//                OffsetRange range = AstUtilities.getRange(last);
//                highlights.put(range, ColoringAttributes.MARK_OCCURRENCES);
//            }
//        } catch (BadLocationException ble) {
//            Exceptions.printStackTrace(ble);
//        } catch (IOException ioe) {
//            Exceptions.printStackTrace(ioe);
//        }
    }
// first: 3rd, 2nd, 5th, 1st, 2nd
    @SuppressWarnings("unchecked")
    private void highlightExitPoints(Node node, Map<OffsetRange, ColoringAttributes> highlights,
        CompilationInfo info) {
        int type = node.getType();
        
        if (type == Token.THROW || type == Token.RETHROW ||
                // There are many RETURN nodes for implicit returns (e.g. end of the
                // function) - these have zero size, and we skip these for occurrence highlighting
                (type == Token.RETURN && node.getSourceEnd() > node.getSourceStart())) {
            OffsetRange astRange = AstUtilities.getRange(node);
            BaseDocument doc = (BaseDocument)info.getDocument();
            if (doc != null) {
                try {
                    OffsetRange lexRange = LexUtilities.getLexerOffsets(info, astRange);
                    if (lexRange != OffsetRange.NONE) {
                        int lineStart = Utilities.getRowStart(doc, lexRange.getStart());
                        int endLineStart = Utilities.getRowStart(doc, lexRange.getEnd());
                        if (lineStart != endLineStart) {
                            lexRange = new OffsetRange(lexRange.getStart(), Utilities.getRowEnd(doc, lexRange.getStart()));
                            astRange = AstUtilities.getAstOffsets(info, lexRange);
                        }
                    }
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
                if (astRange != OffsetRange.NONE) {
                    highlights.put(astRange, ColoringAttributes.MARK_OCCURRENCES);
                }
            }
        } else if (type == Token.FUNCTION || type == Token.SCRIPT) {
            // Don't go into sub methods
            return;
        }

        if (node.hasChildren()) {
            Node child = node.getFirstChild();

            for (; child != null; child = child.getNext()) {
                highlightExitPoints(child, highlights, info);
            }
        }
    }
}
