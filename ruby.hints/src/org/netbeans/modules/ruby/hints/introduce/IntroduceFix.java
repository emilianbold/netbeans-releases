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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.hints.introduce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.jruby.ast.ClassNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.Formatter;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * The actual fix-implementation for introducing methods, fields, etc.
 * 
 * @author Tor Norbye
 */
class IntroduceFix implements Fix {    
    /** Keep in sync with copy in CodeCompleter */
    private static final boolean FORCE_COMPLETION_SPACES = Boolean.getBoolean("ruby.complete.spaces"); // NOI18N
    private static final boolean COMMENT_NEW_ELEMENTS = !Boolean.getBoolean("ruby.create.nocomments"); // NOI18N

    private final CompilationInfo info;
    private final OffsetRange lexRange;
    private final OffsetRange astRange;
    private final IntroduceKind kind;
    private final List<Node> nodes;
    private BaseDocument doc;

    IntroduceFix(CompilationInfo info, List<Node> nodes, OffsetRange lexRange, OffsetRange astRange, IntroduceKind kind) {
        this.info = info;
        this.nodes = nodes;
        this.lexRange = lexRange;
        this.astRange = astRange;
        this.kind = kind;
    }

    public String getKeyExt() {
        switch (kind) {
        case CREATE_CONSTANT:
            return "IntroduceConstant"; //NOI18N 
        case CREATE_VARIABLE:
            return "IntroduceVariable"; //NOI18N
        case CREATE_METHOD:
            return "IntroduceMethod"; //NOI18N
        case CREATE_FIELD:
            return "IntroduceField"; //NOI18N
        default:
            throw new IllegalStateException(kind.toString());
        }
    }

    public String getDescription() {
        return NbBundle.getMessage(IntroduceHint.class, "FIX_" + getKeyExt()); //NOI18N
    }

    public void implement() throws Exception {
        try {
            doc = (BaseDocument) info.getDocument();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return;
        }

        String guessedName = HintUtilities.guessName(info, lexRange, astRange);
        String name = IntroduceHint.testName;
        RubyIndex index = RubyIndex.get(info.getIndex());
        AstPath startPath = new AstPath(AstUtilities.getRoot(info), astRange.getStart());
        List<OffsetRange> duplicates = null;

        if (name == null) {
            switch (kind) {
            case CREATE_VARIABLE:
            case CREATE_CONSTANT: {
                Node startNode = nodes.get(0);
                Node endNode = nodes.get(nodes.size()-1);
                Node top = AstUtilities.getRoot(info);
                int numDuplicates = 1;
                if (kind == IntroduceKind.CREATE_CONSTANT) {
                    Node cls = AstUtilities.findClass(startPath);
                    if (cls != null) {
                        top = cls;
                    }
                    duplicates = DuplicateDetector.findDuplicates(info, doc, top, nodes, startNode, endNode);
                    numDuplicates = duplicates == null ? 1 : duplicates.size();
                }
                JButton btnOk = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Ok"));
                JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Cancel"));
                Set<String> takenNames;
                if (kind == IntroduceKind.CREATE_CONSTANT) {
                    takenNames = HintUtilities.getUsedConstants(index, startPath);
                } else {
                    takenNames = HintUtilities.getUsedLocalNames(startPath, startPath.leaf());
                }
                IntroduceVariablePanel panel = new IntroduceVariablePanel(numDuplicates, guessedName,
                        kind == IntroduceKind.CREATE_CONSTANT, btnOk, takenNames);
                String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_" + getKeyExt()); //NOI18N
                DialogDescriptor dd = new DialogDescriptor(panel, caption, true,
                        new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null,
                        null);
                if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
                    return;//cancel
                }
                name = panel.getVariableName();
                if (!panel.isReplaceAll()) {
                    duplicates = Collections.emptyList();
                }
                break;
            }
            case CREATE_FIELD: {
                int numDuplicates = 1;
                JButton btnOk = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Ok"));
                JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Cancel"));
                // TODO Allow choice between inserting in constructor, in method, etc.
                int[] initilizeIn = new int[1];
                Set<String> takenNames = HintUtilities.getUsedFields(index, startPath);
                IntroduceFieldPanel panel = new IntroduceFieldPanel(guessedName, initilizeIn, numDuplicates, btnOk, takenNames);
                String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_" + getKeyExt()); //NOI18N
                DialogDescriptor dd = new DialogDescriptor(panel, caption, true,
                        new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null,
                        null);
                if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
                    return;//cancel
                }
                name = panel.getFieldName();
                if (!panel.isReplaceAll()) {
                    duplicates = Collections.emptyList();
                }
                break;
            }
            case CREATE_METHOD: {
                JButton btnOk = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Ok"));
                JButton btnCancel = new JButton(NbBundle.getMessage(IntroduceHint.class, "LBL_Cancel"));
                Set<String> takenNames = HintUtilities.getUsedMethods(index, startPath);
                IntroduceMethodPanel panel = new IntroduceMethodPanel("", takenNames); //NOI18N
                panel.setOkButton( btnOk );
                String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_IntroduceMethod");
                DialogDescriptor dd = new DialogDescriptor(panel, caption, true,
                        new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null,
                        null);
                if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
                    return;//cancel
                }
                name = panel.getMethodName();
                break;
            }
        }
        }
        if (kind == IntroduceKind.CREATE_FIELD) {
            name = "@" + name;
        } else if (kind == IntroduceKind.CREATE_CONSTANT) {
            //name = RubyUtils.underlinedNameToCamel(name);
            name = name.toUpperCase();
        }

        try {
            doc.atomicLock();
            if (kind == IntroduceKind.CREATE_CONSTANT || kind == IntroduceKind.CREATE_VARIABLE || kind == IntroduceKind.CREATE_FIELD) {
                introduceExp(name, duplicates);
            } else {
                assert kind == IntroduceKind.CREATE_METHOD;
                // XXX TODO
                extractMethod(name);
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        } finally {
            doc.atomicUnlock();
        }
    }

    private void introduceExp(String name, List<OffsetRange> duplicates) throws BadLocationException {
        assert doc.isAtomicLock();
        boolean isConstant = kind == IntroduceKind.CREATE_CONSTANT;

        int begin;
        if (isConstant) {
            begin = findClassBegin();
            if (begin == -1) {
                begin = findMethodBegin();
            }
            if (begin == -1) {
                // Not in a method - just place it before the method
                begin = findStatementBegin();
            } else {
                // Jump to the beginning of the line
                begin = Utilities.getRowStart(doc, begin);
            }
        } else {
            begin = findStatementBegin();
        }

        int lexStart = lexRange.getStart();
        int lexEnd = lexRange.getEnd();
        
        assert begin <= lexStart;
        StringBuilder sb = new StringBuilder();

        String commentText = NbBundle.getMessage(IntroduceHint.class, "DefaultMethodComment");
        if (isConstant && COMMENT_NEW_ELEMENTS) {
            // TODO - insert a code template for editing the comment?
            sb.append("# ");
        }
        int commentTextDelta = sb.length();
        if (isConstant && COMMENT_NEW_ELEMENTS) {
            sb.append(commentText);
            sb.append("\n");
        }
        
        sb.append(name);
        sb.append(" = ");

        AstPath path = new AstPath(AstUtilities.getRoot(info), astRange.getStart());
        boolean addHash = false;
        if (path.leafGrandParent() != null && path.leafGrandParent().nodeId == NodeTypes.HASHNODE) {
            addHash = true;
        }
        if (addHash) {
            sb.append("{ ");
        }

        sb.append(doc.getText(lexStart, lexEnd - lexStart));
        if (addHash) {
            sb.append(" }");
        }
        sb.append("\n");
        if (isConstant) {
            sb.append("\n");
        }

        Position commentPos = null;
        if (isConstant && begin > 0 && COMMENT_NEW_ELEMENTS) {
            commentPos = doc.createPosition(begin-1); // -1: want the position -before- the inserts
        }
        
        if (duplicates != null && duplicates.size() > 1) {
            // Gotta process the edits in order

            List<Edit> edits = new ArrayList<Edit>();
            Set<Integer> starts = new HashSet<Integer>();
            edits.add(new Edit(lexStart, lexEnd-lexStart, name));
            starts.add(lexStart);
            edits.add(new Edit(begin, 0, sb.toString()));
            starts.add(begin);
            
            for (OffsetRange range : duplicates) {
                int start = range.getStart();
                if (!starts.contains(start)) {
                    edits.add(new Edit(start, range.getLength(), name));
                    starts.add(start);
                }
            }
            Collections.sort(edits);
            Collections.reverse(edits);
            
            // Apply edits in reverse order (to keep offsets accurate)
            for (Edit edit : edits) {
                if (edit.removeLen > 0) {
                    doc.remove(edit.offset, edit.removeLen);
                }
                doc.insertString(edit.offset, edit.insertText, null);
            }
        } else {
            doc.remove(lexStart, lexEnd - lexStart);
            doc.insertString(lexStart, name, null);
            doc.insertString(begin, sb.toString(), null);
        }
        // Finally, reformat - ugh, these offsets can be all bogus now!
        int newEnd = lexEnd + sb.length() - (lexEnd-lexStart);
        new Formatter().reindent(doc, begin, newEnd, null, null);

            // Warp to the inserted method and show the comment
        if (commentPos != null && isConstant) {
            JTextComponent target = CopiedCode.getPaneFor(info.getFileObject());
            if (target != null) {
                int offset = Utilities.getRowFirstNonWhite(doc, commentPos.getOffset()+1);
                if (offset != -1) {
                    offset += commentTextDelta;
                    if (offset+commentText.length() <= doc.getLength()) {
                        String s = doc.getText(offset, commentText.length());
                        if (commentText.equals(s)) {
                            target.select(offset, offset+commentText.length());
                        }
                    }
                }
            }
        }
    }
    
    private class Edit implements Comparable<Edit> {
        int offset;
        int removeLen;
        String insertText;

        public Edit(int offset, int removeLen, String insertText) {
            this.offset = offset;
            this.removeLen = removeLen;
            this.insertText = insertText;
        }

        public int compareTo(IntroduceFix.Edit other) {
            return offset-other.offset;
        }
    }

    private void extractMethod(String name) throws BadLocationException {
        Node startNode = nodes.get(0);
        Node endNode = nodes.get(nodes.size()-1);
        //AstPath startPath = new AstPath(AstUtilities.getRoot(info), start);
        //AstPath endPath = new AstPath(AstUtilities.getRoot(info), end);
        //Node startNode = startPath.leaf();
        //Node endNode = endPath.leaf();

        // Somewhere in the middle to ensure we pick everything up
        // XXX That ain't right either - it might filter out blocks within the code fragment!
        AstPath startPath = new AstPath(AstUtilities.getRoot(info), astRange.getStart()+astRange.getLength()/2);

        List<Node> applicableBlocks = AstUtilities.getApplicableBlocks(startPath, true);

        InputOutputVarFinder varFinder = new InputOutputVarFinder(startNode, endNode, applicableBlocks);
        ParseTreeWalker walker = new ParseTreeWalker(varFinder);
        Node method = AstUtilities.findLocalScope(startPath.leaf(), startPath);
        walker.walk(method);

        Set<String> inputs = varFinder.getInputVars();
        Set<String> outputs = varFinder.getOutputVars();
        List<String> inputVars = new ArrayList<String>(inputs);
        Collections.sort(inputVars);
        List<String> outputVars = new ArrayList<String>(outputs);
        Collections.sort(outputVars);

        assert doc.isAtomicLock();
        // TODO: Compute local and dynamic variables; pass these in to the method
        // TODO: Compute side effects (assignments to local and dynamic variables);
        // these should be "return values"
        // TODO: Worry about exceptions and control flow (yields, nexts, continues,etc)
        int prevEnd = findMethodEnd();

        // TODO - validate that the name is unique etc. so we don't accidentally rewrite it!

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n");
        sb.append("# ");
        int commentTextDelta = sb.length();
        String commentText = NbBundle.getMessage(IntroduceHint.class, "DefaultMethodComment");
        sb.append(commentText);
        sb.append("\n");
        sb.append("def ");
        sb.append(name);
        if (inputVars.size() > 0) {
            if (FORCE_COMPLETION_SPACES) {
                appendCommaList(sb, inputVars, " ", "");
            } else {
                appendCommaList(sb, inputVars, "(", ")");
            }
        }
        sb.append('\n');
        int lexStart = lexRange.getStart();
        int lexEnd = lexRange.getEnd();
        
        sb.append(doc.getText(lexStart, lexEnd-lexStart));
        sb.append('\n');
        if (outputVars.size() > 0) {
            // Don't emit "return" unless we have multiple parameters
            appendCommaList(sb, outputVars, outputVars.size() == 1 ? "" : "return ", "\n");
        }
        sb.append("end");

        doc.insertString(prevEnd, sb.toString(), null);
        Position methodPos = doc.createPosition(prevEnd+commentTextDelta); // Position of the comment
        new Formatter().reindent(doc, prevEnd+1, prevEnd+1+sb.length(), null, null);
        // TODO: Format the method

        doc.remove(lexStart, lexEnd-lexStart);
        sb = new StringBuilder();
        if (outputVars.size() > 0) {
            appendCommaList(sb, outputVars, null, null);
            sb.append(" = ");
        }
        sb.append(name);
        if (inputVars.size() > 0) {
            if (FORCE_COMPLETION_SPACES) {
                appendCommaList(sb, inputVars, " ", "");
            } else {
                appendCommaList(sb, inputVars, "(", ")");
            }
        }
        doc.insertString(lexStart, sb.toString(), null);

        new Formatter().reindent(doc, lexStart, lexStart+sb.length(), null, null);

        // Warp to the inserted method and show the comment
        JTextComponent target = CopiedCode.getPaneFor(info.getFileObject());
        if (target != null) {
            int offset = methodPos.getOffset();
            if (offset+commentText.length() <= doc.getLength()) {
                String s = doc.getText(offset, commentText.length());
                if (commentText.equals(s)) {
                    target.select(offset, offset+commentText.length());
                }
            }
        }
    }

    private void appendCommaList(StringBuilder sb, List<String> items, String pre, String post) {
        if (pre != null) {
            sb.append(pre);
        }
        boolean first = true;
        for (String item : items) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(item);
        }
        if (post != null) {
            sb.append(post);
        }
    }

    private int findClassBegin() throws BadLocationException {
        // Find the location of the beginning of the current statement
        AstPath path = new AstPath(AstUtilities.getRoot(info), astRange.getStart());

        Node cls = AstUtilities.findClass(path);
        if (cls != null) {
            int astPos = Utilities.getRowEnd(doc, cls.getPosition().getStartOffset())+1;
            return Math.min(LexUtilities.getLexerOffset(info, astPos), doc.getLength());
        }
        
        return -1;
    }
    
    private int findStatementBegin() throws BadLocationException {
        //if (RubyUtils.isRhtmlDocument(doc)) {
        //    // Special handling here?
        //}
        // Find the location of the beginning of the current statement
        AstPath path = new AstPath(AstUtilities.getRoot(info), astRange.getStart());
        Iterator<Node> it = path.leafToRoot();
        Node prev = null;
        boolean found = false;
        while (it.hasNext()) {
            Node n = it.next();
            if (n.nodeId == NodeTypes.NEWLINENODE) {
                if (prev != null) {
                    found = true;
                    // Peek ahead and see if we have another outer newline that is also on
                    // this line, e.g.   if (x<y)   will have a newline for the paren as well as
                    // for the actual line
                    Node p = n;
                    Node innerNewline = n;
                    while (it.hasNext()) {
                        n = it.next();
                        if (n.nodeId == NodeTypes.NEWLINENODE) {
                            int prevNewline = Math.min(LexUtilities.getLexerOffset(info, innerNewline.getPosition().getStartOffset()), doc.getLength());
                            int newLine = Math.min(LexUtilities.getLexerOffset(info, n.getPosition().getStartOffset()), doc.getLength());
                            if (p != null && newLine != -1 && prevNewline != -1 && Utilities.getRowStart(doc, prevNewline) == Utilities.getRowStart(doc, newLine)) {
                                prev = p;
                            }
                            break;
                        }
                        p = n;
                    }
                }
                break;
            }
            prev = n;
        }
        if (found) {
            return Math.min(LexUtilities.getLexerOffset(info, prev.getPosition().getStartOffset()), doc.getLength());
        } else {
            // This is not right but a reasonable fallback
            return Utilities.getRowFirstNonWhite(doc, lexRange.getStart());
        }
    }

    /** Compute the end of the current method */
    private int findMethodEnd() throws BadLocationException {
        // TODO - I need an AST path for this!
        AstPath path = new AstPath(AstUtilities.getRoot(info), astRange.getStart());
        MethodDefNode method = AstUtilities.findMethod(path);
        if (method != null) {
            return Math.min(LexUtilities.getLexerOffset(info, method.getPosition().getEndOffset()), doc.getLength());
        } else {
            // Not inside a method - we're in top level scope so just
            // use the end of the document
            return doc.getLength();
        }
    }

    /** Compute the beginning of the current method */
    private int findMethodBegin() throws BadLocationException {
        AstPath path = new AstPath(AstUtilities.getRoot(info), astRange.getStart());
        MethodDefNode method = AstUtilities.findMethod(path);
        if (method != null) {
            int methodAstOffset = method.getPosition().getStartOffset();
            int lexOffset = LexUtilities.getLexerOffset(info, methodAstOffset);
            if (lexOffset != -1) {
                OffsetRange comment = HintUtilities.findRDocRange(doc, methodAstOffset);
                if (comment != OffsetRange.NONE) {
                    return comment.getStart();
                }
            }
            return LexUtilities.getLexerOffset(info, methodAstOffset);
        } else {
            return -1;
        }
    }

    public boolean isSafe() {
        return true;
    }

    public boolean isInteractive() {
        return true;
    }
}
