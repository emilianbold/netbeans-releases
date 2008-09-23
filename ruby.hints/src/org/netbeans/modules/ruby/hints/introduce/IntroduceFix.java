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

import org.netbeans.modules.ruby.ParseTreeWalker;
import java.util.MissingResourceException;
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
import org.jruby.nb.ast.MethodDefNode;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.NodeType;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.EditList;
import org.netbeans.modules.gsf.api.PreviewableFix;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyFormatter;
import org.netbeans.modules.ruby.NbUtilities;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.ruby.RubyMimeResolver;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
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
class IntroduceFix implements PreviewableFix {
    /** Keep in sync with copy in CodeCompleter */
    private static final boolean FORCE_COMPLETION_SPACES = Boolean.getBoolean("ruby.complete.spaces"); // NOI18N
    private static final boolean COMMENT_NEW_ELEMENTS = !Boolean.getBoolean("ruby.create.nocomments"); // NOI18N

    private final RubyRuleContext context;
    private final CompilationInfo info;
    private final OffsetRange lexRange;
    private final OffsetRange astRange;
    private final IntroduceKind kind;
    private final List<Node> nodes;
    private final BaseDocument doc;
    private int commentOffset = -1;
    
    IntroduceFix(RubyRuleContext context, List<Node> nodes, OffsetRange lexRange, OffsetRange astRange, IntroduceKind kind) {
        this.context = context;
        this.nodes = nodes;
        this.lexRange = lexRange;
        this.astRange = astRange;
        this.kind = kind;
        
        this.info = context.compilationInfo;
        this.doc = context.doc;
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
        String name = null;
        EditList edits = createEdits(name);
        if (edits == null) {
            // Some kind of error
            return;
        }

        Position  commentPosition = edits.createPosition(commentOffset);
        edits.apply();

        // Warp to the inserted method and show the comment
        if (commentPosition != null && commentPosition.getOffset() != -1) {
            JTextComponent target = NbUtilities.getPaneFor(info.getFileObject());
            if (target != null) {
                int offset = commentPosition.getOffset();
                String commentText = getCommentText();
                if (offset+commentText.length() <= doc.getLength()) {
                    String s = doc.getText(offset, commentText.length());
                    if (commentText.equals(s)) {
                        target.select(offset, offset+commentText.length());
                    }
                }
            }
        }
    }

    private String getCommentText() throws MissingResourceException {
        return NbBundle.getMessage(IntroduceHint.class, "DefaultMethodComment");
    }

    public EditList getEditList() {
        String name = "new_name";
        try {
            return createEdits(name);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    private EditList createEdits(String name) throws Exception {
        String guessedName = AstUtilities.guessName(info, lexRange, astRange);
        RubyIndex index = RubyIndex.get(info.getIndex(RubyMimeResolver.RUBY_MIME_TYPE));
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
                    takenNames = AstUtilities.getUsedConstants(index, startPath);
                } else {
                    takenNames = AstUtilities.getUsedLocalNames(startPath, startPath.leaf());
                }
                IntroduceVariablePanel panel = new IntroduceVariablePanel(numDuplicates, guessedName,
                        kind == IntroduceKind.CREATE_CONSTANT, btnOk, takenNames);
                String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_" + getKeyExt()); //NOI18N
                DialogDescriptor dd = new DialogDescriptor(panel, caption, true,
                        new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null,
                        null);
                if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
                    return null;//cancel
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
                Set<String> takenNames = AstUtilities.getUsedFields(index, startPath);
                IntroduceFieldPanel panel = new IntroduceFieldPanel(guessedName, initilizeIn, numDuplicates, btnOk, takenNames);
                String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_" + getKeyExt()); //NOI18N
                DialogDescriptor dd = new DialogDescriptor(panel, caption, true,
                        new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null,
                        null);
                if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
                    return null;//cancel
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
                Set<String> takenNames = AstUtilities.getUsedMethods(index, startPath);
                IntroduceMethodPanel panel = new IntroduceMethodPanel("", takenNames); //NOI18N
                panel.setOkButton( btnOk );
                String caption = NbBundle.getMessage(IntroduceHint.class, "CAP_IntroduceMethod");
                DialogDescriptor dd = new DialogDescriptor(panel, caption, true,
                        new Object[]{btnOk, btnCancel}, btnOk, DialogDescriptor.DEFAULT_ALIGN, null,
                        null);
                if (DialogDisplayer.getDefault().notify(dd) != btnOk) {
                    return null;//cancel
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

        if (kind == IntroduceKind.CREATE_CONSTANT || kind == IntroduceKind.CREATE_VARIABLE || kind == IntroduceKind.CREATE_FIELD) {
            return introduceExp(name, duplicates);
        } else {
            assert kind == IntroduceKind.CREATE_METHOD;
            // XXX TODO
            return extractMethod(name);
        }
    }

    private EditList introduceExp(String name, List<OffsetRange> duplicates) throws BadLocationException {
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

        if (isConstant && COMMENT_NEW_ELEMENTS) {
            // TODO - insert a code template for editing the comment?
            sb.append("# ");
        }

        int commentTextDelta = sb.length();
        if (isConstant && COMMENT_NEW_ELEMENTS) {
            sb.append(getCommentText());
            sb.append("\n");
        }
        
        sb.append(name);
        sb.append(" = ");

        AstPath path = new AstPath(AstUtilities.getRoot(info), astRange.getStart());
        boolean addHash = false;
        if (path.leafGrandParent() != null && path.leafGrandParent().nodeId == NodeType.HASHNODE) {
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

        commentOffset = -1;
        if (isConstant && begin > 0 && COMMENT_NEW_ELEMENTS) {
            commentOffset = begin+commentTextDelta;
        }
        
        EditList edits = new EditList(doc);
        edits.setFormatAll(false);

        edits.replace(lexStart, lexEnd-lexStart, name, true, 1);
        edits.replace(begin, 0, sb.toString(), true, 2);

        if (duplicates != null && duplicates.size() > 1) {
            Set<Integer> starts = new HashSet<Integer>();
            starts.add(lexStart);
            starts.add(begin);
            
            for (OffsetRange range : duplicates) {
                int start = range.getStart();
                if (!starts.contains(start)) {
                    edits.replace(start, range.getLength(), name, true, 0);
                    starts.add(start);
                }
            }
        }

        return edits;
    }

    private EditList extractMethod(String name) throws BadLocationException {
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

        // TODO: Compute local and dynamic variables; pass these in to the method
        // TODO: Compute side effects (assignments to local and dynamic variables);
        // these should be "return values"
        // TODO: Worry about exceptions and control flow (yields, nexts, continues,etc)
        int prevEnd = findMethodEnd();

        // TODO - validate that the name is unique etc. so we don't accidentally rewrite it!

        StringBuilder sb = new StringBuilder();
        EditList edits = new EditList(doc);
        edits.setFormatAll(false);
        boolean isAbove = prevEnd < astRange.getStart();
        sb.append("\n");
        if (!isAbove) {
            sb.append("\n");
        }
        sb.append("# ");
        int commentTextDelta = sb.length();
        sb.append(getCommentText());
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
        if (isAbove) {
            sb.append("\n");
        }

        edits.replace(prevEnd, 0, sb.toString(), true, 0);
        commentOffset = prevEnd+commentTextDelta;

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

        edits.replace(lexStart, lexEnd-lexStart, sb.toString(), true, 0);
        
        return edits;
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
            if (n.nodeId == NodeType.NEWLINENODE) {
                if (prev != null) {
                    found = true;
                    // Peek ahead and see if we have another outer newline that is also on
                    // this line, e.g.   if (x<y)   will have a newline for the paren as well as
                    // for the actual line
                    Node p = n;
                    Node innerNewline = n;
                    while (it.hasNext()) {
                        n = it.next();
                        if (n.nodeId == NodeType.NEWLINENODE) {
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

    /** Compute the end of the current method. If we're not in a method, compute a location
     * inside the surrounding class. */
    private int findMethodEnd() throws BadLocationException {
        // TODO - I need an AST path for this!
        AstPath path = new AstPath(AstUtilities.getRoot(info), astRange.getStart());
        
        // Find the closest block node enclosing the given node
        for (Node curr : path) {
            if (curr.nodeId == NodeType.DEFNNODE || curr.nodeId == NodeType.DEFSNODE) {
                return Math.min(LexUtilities.getLexerOffset(info, curr.getPosition().getEndOffset()), doc.getLength());
            }
            if (curr.nodeId == NodeType.CLASSNODE || curr.nodeId == NodeType.SCLASSNODE ||
                    curr.nodeId == NodeType.MODULENODE) {
                // End of the class:
                //int clzEnd = LexUtilities.getLexerOffset(info, curr.getPosition().getEndOffset());
                //// Skip over "end"
                //clzEnd -= 3;
                //clzEnd = Math.min(clzEnd, doc.getLength());
                //if (clzEnd == Utilities.getRowFirstNonWhite(doc, clzEnd)) {
                //    clzEnd = Utilities.getRowEnd(doc, Utilities.getRowStart(doc, clzEnd)-1);
                //}
                //return clzEnd;
                
                // Beginning of the class:
                int clzStart = LexUtilities.getLexerOffset(info, curr.getPosition().getStartOffset());
                return Utilities.getRowEnd(doc, clzStart);
            }
        }
        
        // Not inside a method - we're in top level scope so just
        // use the end of the document
        return doc.getLength();
    }

    /** Compute the beginning of the current method */
    private int findMethodBegin() throws BadLocationException {
        AstPath path = new AstPath(AstUtilities.getRoot(info), astRange.getStart());
        MethodDefNode method = AstUtilities.findMethod(path);
        if (method != null) {
            int methodAstOffset = method.getPosition().getStartOffset();
            int lexOffset = LexUtilities.getLexerOffset(info, methodAstOffset);
            if (lexOffset != -1) {
                OffsetRange comment = LexUtilities.findRDocRange(doc, methodAstOffset);
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

    public boolean canPreview() {
        return true;
    }
}
