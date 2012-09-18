/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.java.source.pretty;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import static com.sun.source.tree.Tree.*;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.*;
import static com.sun.tools.javac.code.Flags.*;
import com.sun.tools.javac.code.Symbol.*;
import static com.sun.tools.javac.code.TypeTags.*;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyle.*;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;
import org.netbeans.modules.java.source.builder.CommentSetImpl;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.save.CasualDiff;
import org.netbeans.modules.java.source.save.DiffContext;
import static org.netbeans.modules.java.source.save.PositionEstimator.*;
import org.netbeans.modules.java.source.save.Reformatter;
import org.netbeans.modules.java.source.transform.FieldGroupTree;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

import org.openide.util.Exceptions;

/** Prints out a tree as an indented Java source program.
 */
public final class VeryPretty extends JCTree.Visitor {

    private static final char[] hex = "0123456789ABCDEF".toCharArray();
    private static final String REPLACEMENT = "%[a-z]*%";
    private static final String ERROR = "<error>"; //NOI18N

    private final CodeStyle cs;
    public  final CharBuffer out;

    private final Names names;
    private final CommentHandler commentHandler;
    private final Symtab symbols;
    private final Types types;
    private final TreeInfo treeinfo;
    private final WidthEstimator widthEstimator;
    private final DanglingElseChecker danglingElseChecker;

    public Name enclClassName; // the enclosing class name.
    private int indentSize;
    private int prec; // visitor argument: the current precedence level.
    private boolean printingMethodParams;
    private DiffContext diffContext;
    private CommentHandlerService comments;

    private int fromOffset = -1;
    private int toOffset = -1;
    private boolean containsError = false;
    private boolean insideAnnotation = false;

    private final Map<Tree, ?> tree2Tag;
    private final Map<Object, int[]> tag2Span;
    private final String origText;
    private int initialOffset = 0;

    public VeryPretty(DiffContext diffContext) {
        this(diffContext, diffContext.style, null, null, null);
    }

    public VeryPretty(DiffContext diffContext, CodeStyle cs) {
        this(diffContext, cs, null, null, null);
    }

    public VeryPretty(DiffContext diffContext, CodeStyle cs, Map<Tree, ?> tree2Tag, Map<?, int[]> tag2Span, String origText) {
        this(diffContext.context, cs, tree2Tag, tag2Span, origText);
        this.diffContext = diffContext;
    }

    public VeryPretty(DiffContext diffContext, CodeStyle cs, Map<Tree, ?> tree2Tag, Map<?, int[]> tag2Span, String origText, int initialOffset) {
        this(diffContext, cs, tree2Tag, tag2Span, origText);
        this.initialOffset = initialOffset; //provide intial offset of this priter
    }

    private VeryPretty(Context context, CodeStyle cs, Map<Tree, ?> tree2Tag, Map<?, int[]> tag2Span, String origText) {
	names = Names.instance(context);
	enclClassName = names.empty;
        commentHandler = CommentHandlerService.instance(context);
	symbols = Symtab.instance(context);
        types = Types.instance(context);
	treeinfo = TreeInfo.instance(context);
	widthEstimator = new WidthEstimator(context);
        danglingElseChecker = new DanglingElseChecker();
        prec = TreeInfo.notExpression;
        this.cs = cs;
        out = new CharBuffer(cs.getRightMargin(), cs.getTabSize(), cs.expandTabToSpaces());
        this.indentSize = cs.getIndentSize();
        this.tree2Tag = tree2Tag;
        this.tag2Span = (Map<Object, int[]>) tag2Span;//XXX
        this.origText = origText;
        this.comments = CommentHandlerService.instance(context);
    }

    public void setInitialOffset(int offset) {
        initialOffset = offset < 0 ? 0 : offset;
    }

    public int getInitialOffset() {
        return initialOffset;
    }

    @Override
    public String toString() {
	return out.toString();
    }

    public void toLeftMargin() {
	out.toLeftMargin();
    }

    public void reset(int margin) {
	out.setLength(0);
	out.leftMargin = margin;
    }

    public int getIndent() {
        return out.leftMargin;
    }
    
    public void setIndent(int indent) {
        out.leftMargin = indent;
    }

    /** Increase left margin by indentation width.
     */
    public int indent() {
	int old = out.leftMargin;
	out.leftMargin = old + indentSize;
	return old;
    }

    public void undent(int old) {
	out.leftMargin = old;
    }

    public void newline() {
	out.nlTerm();
    }

    public void blankline() {
        out.blanklines(1);
    }

    public int setPrec(int prec) {
        int old = this.prec;
        this.prec = prec;
        return old;
    }

    public final void print(String s) {
	if (s == null)
	    return;
        out.append(s);
    }           

    public final void print(Name n) {
        if (n == null)
            return;
	out.appendUtf8(n.getByteArray(), n.getByteOffset(), n.getByteLength());
    }

    public void print(JCTree t) {
        if (t == null) return;
        blankLines(t, true);
        toLeftMargin();
	printPrecedingComments(t, true);
        doAccept(t);
        printTrailingComments(t, true);
        blankLines(t, false);
    }

    private static int getOldPos(JCTree oldT) {
        return TreeInfo.getStartPos(oldT);
    }
    public int endPos(JCTree t) {
        return TreeInfo.getEndPos(t, diffContext.origUnit.endPositions);
    }
    public Set<Tree> oldTrees = Collections.emptySet();
    private void doAccept(JCTree t) {
        int start = toString().length();

        if (!handlePossibleOldTrees(Collections.singletonList(t), false)) {
            if (t instanceof FieldGroupTree) {
                //XXX: should be able to use handlePossibleOldTrees over the individual variables:
                FieldGroupTree fgt = (FieldGroupTree) t;
                if (fgt.isEnum()) {
                    printEnumConstants(List.from(fgt.getVariables().toArray(new JCTree[0])), !fgt.isEnum() || fgt.moreElementsFollowEnum());
                } else {
                    //XXX: this will unroll the field group (see FieldGroupTest.testMove187766)
                    //XXX: copied from visitClassDef
                    boolean firstMember = true;
                    for (JCVariableDecl var : fgt.getVariables()) {
                        oldTrees.remove(var);
                        assert !isEnumerator(var);
                        assert !isSynthetic(var);
                        toColExactly(out.leftMargin);
                        printStat(var, true, firstMember);
                        newline();
                        firstMember = false;
                    }
                }
            } else {
                t.accept(this);
            }
        }

        int end = toString().length();

//        System.err.println("t: " + t);
//        System.err.println("thr=" + System.identityHashCode(t));
        Object tag = tree2Tag != null ? tree2Tag.get(t) : null;

        if (tag != null) {
            tag2Span.put(tag, new int[]{start + initialOffset, end + initialOffset});
        }
    }

    public boolean handlePossibleOldTrees(java.util.List<? extends JCTree> toPrint, boolean includeStartingComments) {
        for (JCTree t : toPrint) {
            if (!oldTrees.contains(t)) return false;
            if (t.getKind() == Kind.ARRAY_TYPE) {
                return false;//XXX #197584: C-like array are cannot be copied as old trees.
            }
        }

        JCTree firstTree = toPrint.get(0);
        JCTree lastTree = toPrint.get(toPrint.size() - 1);

        CommentSet old = commentHandler.getComments(firstTree);
        int realStart;

        //XXX hack:
        if (includeStartingComments) {
            realStart = Math.min(getOldPos(firstTree), CasualDiff.commentStart(old, CommentSet.RelativePosition.PRECEDING));
        } else {
            realStart = getOldPos(firstTree);
        }

        final int[] realEnd = {endPos(lastTree)};
        new TreeScanner<Void, Void>() {
            @Override
            public Void scan(Tree node, Void p) {
                if (node != null) {
                    CommentSetImpl old = comments.getComments(node);
                    realEnd[0] = Math.max(realEnd[0], Math.max(CasualDiff.commentEnd(old, CommentSet.RelativePosition.INLINE), CasualDiff.commentEnd(old, CommentSet.RelativePosition.TRAILING)));
                    old.clearComments(CommentSet.RelativePosition.INLINE);
                    old.clearComments(CommentSet.RelativePosition.TRAILING);
                }
                return super.scan(node, p);
            }
        }.scan(lastTree, null);

        //XXX: handle comments!
        copyToIndented(realStart, realEnd[0]);
        return true;
    }

    private static final Logger LOG = Logger.getLogger(CasualDiff.class.getName());
    private void copyToIndented(int from, int to) {
        if (from == to) {
            return;
        } else if (from > to || from < 0 || to < 0) {
            // #104107 - log the source when this problem occurs.
            LOG.log(Level.INFO, "-----\n" + origText + "-----\n");
            LOG.log(Level.INFO, "Illegal values: from = " + from + "; to = " + to + "." +
                "Please, attach your messages.log to new issue!");
            if (to >= 0)
                eatChars(from-to);
            return;
        } else if (to > origText.length()) {
            // #99333, #97801: Debug message for the issues.
            LOG.severe("-----\n" + origText + "-----\n");
            throw new IllegalArgumentException("Copying to " + to + " is greater then its size (" + origText.length() + ").");
        }

        String text = origText.substring(from, to);
        if (text.contains("\n")) {
            int i = from - 1;
            int originalColumn = 0;
            int originalIndent = 0;
            int originalLeadingWhitespaceChars = 0;
            boolean originalIndented = true;

            while (i >= 0) {
                if (origText.charAt(i) == ' ') {
                    originalColumn++;
                    originalIndent++;
                    originalLeadingWhitespaceChars++;
                } else if (origText.charAt(i) == '\t') {
                    originalColumn += cs.getTabSize();
                    originalIndent += cs.getTabSize();
                    originalLeadingWhitespaceChars++;
                } else if (origText.charAt(i) == '\n') {
                    break;
                } else {
                    originalColumn++;
                    originalIndent = 0;
                    originalIndented= false;
                }
                i--;
            }

            int oldIndent = getIndent();
            int relativeIndent;

            if (text.charAt(0) == '{') {
                relativeIndent = oldIndent - originalIndent;
                print(text.substring(0, text.indexOf("\n") + 1));
                text = text.substring(text.indexOf("\n") + 1);
            } else if (originalIndented) {
                if (out.isWhitespaceLine()) {
                    text = origText.substring(from - originalLeadingWhitespaceChars, from) + text;
                    relativeIndent = getIndent() - originalColumn;

                    out.toLineStart();
                    setIndent(0);
                } else {
                    relativeIndent = out.col - originalColumn;
                    print(text.substring(0, text.indexOf("\n") + 1));
                    text = text.substring(text.indexOf("\n") + 1);
                }
            } else {
                if (out.isWhitespaceLine()) {
                    text = getIndent(originalColumn)+ text;
                    relativeIndent = getIndent() - originalColumn;

                    out.toLineStart();
                    setIndent(0);
                } else {
                    relativeIndent = out.col - originalColumn;
                    print(text.substring(0, text.indexOf("\n") + 1));
                    text = text.substring(text.indexOf("\n") + 1);
                }
            }

            boolean first = true;

            for (String l : text.split("\n")) {
                if (!first) {
                    print("\n");
                }
                first = false;
                if (l.isEmpty()) continue;//don't uselesly indent empty lines
                int currentIndent = 0;
                int leadingWhitespaceChars = 0;
                while (leadingWhitespaceChars < l.length()) {
                    if (l.charAt(leadingWhitespaceChars) == ' ') {
                        currentIndent++;
                    } else if (l.charAt(leadingWhitespaceChars) == '\t') {
                        currentIndent += cs.getTabSize();
                    } else {
                        break;
                    }
                    leadingWhitespaceChars++;
                }
                print(getIndent(currentIndent + relativeIndent));
                print(l.substring(leadingWhitespaceChars));
            }

            setIndent(oldIndent);
        } else {
            if (out.isWhitespaceLine()) toLeftMargin();
            print(text);
        }
    }

    private String getIndent(int indent) {
        StringBuilder sb = new StringBuilder();
        int col = 0;
        if (!cs.expandTabToSpaces()) {
            int tabSize = cs.getTabSize();
            while (col + tabSize <= indent) {
                sb.append('\t'); //NOI18N
                col += tabSize;
            }
        }
        while (col < indent) {
            sb.append(' '); //NOI18N
            col++;
        }
        return sb.toString();
    }

    public String reformat(JCTree t, int fromOffset, int toOffset, int indent) {
        reset(indent);
        this.fromOffset = fromOffset;
        this.toOffset = toOffset;
        print(t);
        return containsError ? null : out.toString();
    }

    /** Print a package declaration.
     */
    public void printPackage(JCExpression pid) {
        if (pid != null) {
            blankLines(cs.getBlankLinesBeforePackage());
            print("package ");
            printExpr(pid);
            print(';');
            blankLines(cs.getBlankLinesAfterPackage());
        }
    }

    public String getMethodHeader(MethodTree t, String s) {
        JCMethodDecl tree = (JCMethodDecl) t;
        printAnnotations(tree.mods.annotations);
        s = replace(s, UiUtils.PrintPart.ANNOTATIONS);
        printFlags(tree.mods.flags);
        s = replace(s, UiUtils.PrintPart.FLAGS);
        if (tree.name == names.init) {
            print(enclClassName);
            s = replace(s, UiUtils.PrintPart.NAME);
        } else {
            if (tree.typarams != null) {
                printTypeParameters(tree.typarams);
                needSpace();
                s = replace(s, UiUtils.PrintPart.TYPEPARAMETERS);
            }
            print(tree.restype);
            s = replace(s, UiUtils.PrintPart.TYPE);
            out.clear();
            print(tree.name);
            s = replace(s, UiUtils.PrintPart.NAME);
        }
        print('(');
        wrapTrees(tree.params, WrapStyle.WRAP_NEVER, out.col);
        print(')');
        s = replace(s, UiUtils.PrintPart.PARAMETERS);
        if (tree.thrown.nonEmpty()) {
            print(" throws ");
            wrapTrees(tree.thrown, WrapStyle.WRAP_NEVER, out.col);
            s = replace(s, UiUtils.PrintPart.THROWS);
        }
        return s.replaceAll(REPLACEMENT,"");
    }

    public String getClassHeader(ClassTree t, String s) {
        JCClassDecl tree = (JCClassDecl) t;
        printAnnotations(tree.mods.annotations);
        s = replace(s, UiUtils.PrintPart.ANNOTATIONS);
        long flags = tree.mods.flags;
        if ((flags & ENUM) != 0)
            printFlags(flags & ~(INTERFACE | FINAL));
        else
            printFlags(flags & ~(INTERFACE | ABSTRACT));
        s = replace(s, UiUtils.PrintPart.FLAGS);
        if ((flags & INTERFACE) != 0) {
            print("interface ");
            print(tree.name);
            s = replace(s, UiUtils.PrintPart.NAME);
            printTypeParameters(tree.typarams);
            s = replace(s, UiUtils.PrintPart.TYPEPARAMETERS);
            if (tree.implementing.nonEmpty()) {
                print(" extends ");
                wrapTrees(tree.implementing, WrapStyle.WRAP_NEVER, out.col);
                s = replace(s, UiUtils.PrintPart.EXTENDS);
            }
        } else {
            if ((flags & ENUM) != 0)
                print("enum ");
            else {
                if ((flags & ABSTRACT) != 0)
                    print("abstract ");
                print("class ");
            }
            print(tree.name);
            s = replace(s, UiUtils.PrintPart.NAME);
            printTypeParameters(tree.typarams);
            s = replace(s, UiUtils.PrintPart.TYPEPARAMETERS);
            if (tree.extending != null) {
                print(" extends ");
                print(tree.extending);
                s = replace(s, UiUtils.PrintPart.EXTENDS);
            }
            if (tree.implementing.nonEmpty()) {
                print(" implements ");
                wrapTrees(tree.implementing, WrapStyle.WRAP_NEVER, out.col);
                s = replace(s, UiUtils.PrintPart.IMPLEMENTS);
            }
        }
        return s.replaceAll(REPLACEMENT,"");
    }

    public String getVariableHeader(VariableTree t, String s) {
        JCVariableDecl tree = (JCVariableDecl) t;
        printAnnotations(tree.mods.annotations);
        s = replace(s, UiUtils.PrintPart.ANNOTATIONS);
	printFlags(tree.mods.flags);
        s = replace(s, UiUtils.PrintPart.FLAGS);
	print(tree.vartype);
        s = replace(s, UiUtils.PrintPart.TYPE);
	needSpace();
	print(tree.name);
        s = replace(s, UiUtils.PrintPart.NAME);
        return s.replaceAll(REPLACEMENT,"");
    }

    /**************************************************************************
     * Visitor methods
     *************************************************************************/

    @Override
    public void visitTopLevel(JCCompilationUnit tree) {
        printAnnotations(tree.getPackageAnnotations());
        printPackage(tree.pid);
        List<JCTree> l = tree.defs;
        ArrayList<JCImport> imports = new ArrayList<JCImport>();
        while (l.nonEmpty() && l.head.getTag() == JCTree.IMPORT){
            imports.add((JCImport) l.head);
            l = l.tail;
        }
        printImportsBlock(imports, !l.isEmpty());
	while (l.nonEmpty()) {
            printStat(l.head, true, false);
            newline();
            l = l.tail;
	}
    }

    @Override
    public void visitImport(JCImport tree) {
        print("import ");
        if (tree.staticImport)
            print("static ");
        print(fullName(tree.qualid));
        print(';');
    }

    @Override
    public void visitClassDef(JCClassDecl tree) {
        Name enclClassNamePrev = enclClassName;
	enclClassName = tree.name;
	toLeftMargin();
        printAnnotations(tree.mods.annotations);
	long flags = tree.mods.flags;
	if ((flags & ENUM) != 0)
	    printFlags(flags & ~(INTERFACE | FINAL));
	else
	    printFlags(flags & ~(INTERFACE | ABSTRACT));
	if ((flags & INTERFACE) != 0 || (flags & ANNOTATION) != 0) {
            if ((flags & ANNOTATION) != 0) print('@');
	    print("interface ");
	    print(tree.name);
	    printTypeParameters(tree.typarams);
	    if (tree.implementing.nonEmpty()) {
                wrap("extends ", cs.wrapExtendsImplementsKeyword());
		wrapTrees(tree.implementing, cs.wrapExtendsImplementsList(), cs.alignMultilineImplements()
                        ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	    }
	} else {
	    if ((flags & ENUM) != 0)
		print("enum ");
	    else {
		if ((flags & ABSTRACT) != 0)
		    print("abstract ");
		print("class ");
	    }
	    print(tree.name);
	    printTypeParameters(tree.typarams);
	    if (tree.extending != null) {
                wrap("extends ", cs.wrapExtendsImplementsKeyword());
		print(tree.extending);
	    }
	    if (tree.implementing.nonEmpty()) {
                wrap("implements ", cs.wrapExtendsImplementsKeyword());
		wrapTrees(tree.implementing, cs.wrapExtendsImplementsList(), cs.alignMultilineImplements()
                        ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	    }
	}
	int old = cs.indentTopLevelClassMembers() ? indent() : out.leftMargin;
	int bcol = old;
        switch(cs.getClassDeclBracePlacement()) {
        case NEW_LINE:
            newline();
            toColExactly(old);
            break;
        case NEW_LINE_HALF_INDENTED:
            newline();
	    bcol += (indentSize >> 1);
            toColExactly(bcol);
            break;
        case NEW_LINE_INDENTED:
            newline();
	    bcol = out.leftMargin;
            toColExactly(bcol);
            break;
        }
        if (cs.spaceBeforeClassDeclLeftBrace())
            needSpace();
	print('{');
        boolean emptyClass = true;
        for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
            if (!isSynthetic(l.head)) {
                emptyClass = false;
                break;
            }
        }
	if (!emptyClass) {
	    blankLines(enclClassName.isEmpty() ? cs.getBlankLinesAfterAnonymousClassHeader() : cs.getBlankLinesAfterClassHeader());
            if ((tree.mods.flags & ENUM) != 0) {
                printEnumConstants(tree.defs, false);
            }
            boolean firstMember = true;
            for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                JCTree t = l.head;
                if (!isEnumerator(t)) {
                    if (isSynthetic(t))
                        continue;
                    toColExactly(out.leftMargin);
                    printStat(t, true, firstMember);
                    newline();
                }
                firstMember = false;
            }
	    blankLines(enclClassName.isEmpty() ? cs.getBlankLinesBeforeAnonymousClassClosingBrace() : cs.getBlankLinesBeforeClassClosingBrace());
        } else {
            printEmptyBlockComments(tree, false);
        }
        toColExactly(bcol);
	undent(old);
	print('}');
	enclClassName = enclClassNamePrev;
    }

    private void printEnumConstants(List<JCTree> defs, boolean forceSemicolon) {
        boolean first = true;
        boolean hasNonEnumerator = false;
        for (List<JCTree> l = defs; l.nonEmpty(); l = l.tail) {
            if (isEnumerator(l.head)) {
                if (first) {
                    toColExactly(out.leftMargin);
                    first = false;
                } else {
                    print(cs.spaceBeforeComma() ? " ," : ",");
                    switch(cs.wrapEnumConstants()) {
                    case WRAP_IF_LONG:
                        int rm = cs.getRightMargin();
                        if (widthEstimator.estimateWidth(l.head, rm - out.col) + out.col + 1 <= rm) {
                            if (cs.spaceAfterComma())
                                print(' ');
                            break;
                        }
                    case WRAP_ALWAYS:
                        newline();
                        toColExactly(out.leftMargin);
                        break;
                    case WRAP_NEVER:
                        if (cs.spaceAfterComma())
                            print(' ');
                        break;
                    }
                }
                printStat(l.head, true, false);
            } else if (!isSynthetic(l.head))
                hasNonEnumerator = true;
        }
        if (hasNonEnumerator || forceSemicolon) {
            print(";");
            newline();
        }
    }

    @Override
    public void visitMethodDef(JCMethodDecl tree) {
	if ((tree.mods.flags & Flags.SYNTHETIC)==0 &&
		tree.name != names.init ||
		enclClassName != null) {
	    Name enclClassNamePrev = enclClassName;
	    enclClassName = null;
            printAnnotations(tree.mods.annotations);
            printFlags(tree.mods.flags);
            if (tree.typarams != null) {
                printTypeParameters(tree.typarams);
                needSpace();
            }
            if (tree.name == names.init || tree.name.contentEquals(enclClassNamePrev)) {
                print(enclClassNamePrev);
            } else {
                print(tree.restype);
                needSpace();
                print(tree.name);
            }
            print(cs.spaceBeforeMethodDeclParen() ? " (" : "(");
            if (cs.spaceWithinMethodDeclParens() && tree.params.nonEmpty())
                print(' ');
            boolean oldPrintingMethodParams = printingMethodParams;
            printingMethodParams = true;
            wrapTrees(tree.params, cs.wrapMethodParams(), cs.alignMultilineMethodParams()
                    ? out.col : out.leftMargin + cs.getContinuationIndentSize());
            printingMethodParams = oldPrintingMethodParams;
            if (cs.spaceWithinMethodDeclParens() && tree.params.nonEmpty())
                needSpace();
            print(')');
            if (tree.thrown.nonEmpty()) {
                wrap("throws ", cs.wrapThrowsKeyword());
                wrapTrees(tree.thrown, cs.wrapThrowsList(), cs.alignMultilineThrows()
                        ? out.col : out.leftMargin + cs.getContinuationIndentSize());
            }
            if (tree.body != null) {
                printBlock(tree.body, tree.body.stats, cs.getMethodDeclBracePlacement(), cs.spaceBeforeMethodDeclLeftBrace());
            } else {
                if (tree.defaultValue != null) {
                    print(" default ");
                    printExpr(tree.defaultValue);
                }
                print(';');
            }
            enclClassName = enclClassNamePrev;
	}
    }

    @Override
    public void visitVarDef(JCVariableDecl tree) {
        boolean notEnumConst = (tree.mods.flags & Flags.ENUM) == 0;
        printAnnotations(tree.mods.annotations);
        if (notEnumConst) {
            printFlags(tree.mods.flags);
            if ((tree.mods.flags & VARARGS) != 0) {
                // Variable arity method. Expecting  ArrayType, print ... instead of [].
                if (Kind.ARRAY_TYPE == tree.vartype.getKind()) {
                    printExpr(((JCArrayTypeTree) tree.vartype).elemtype);
                } else {
                    printExpr(tree.vartype);
                }
                print("...");
            } else {
                print(tree.vartype);
            }
        }
        needSpace();
        if (!ERROR.contentEquals(tree.name))
            print(tree.name);
        if (tree.init != null) {
            if (notEnumConst) {
                printVarInit(tree);
            } else {
                JCNewClass newClsTree = (JCNewClass) tree.init;
                if (newClsTree.args.nonEmpty()) {
                    print(cs.spaceBeforeMethodCallParen() ? " (" : "(");
                    if (cs.spaceWithinMethodCallParens())
                        print(' ');
                    wrapTrees(newClsTree.args,
                            cs.wrapMethodCallArgs(),
                            cs.alignMultilineCallArgs() ? out.col : out.leftMargin + cs.getContinuationIndentSize()
                    );
                    print(cs.spaceWithinMethodCallParens() ? " )" : ")");
                }
                if (newClsTree.def != null) {
                    Name enclClassNamePrev = enclClassName;
                    enclClassName = newClsTree.def.name;
                    printBlock(null, newClsTree.def.defs, cs.getOtherBracePlacement(), cs.spaceBeforeClassDeclLeftBrace());
                    enclClassName = enclClassNamePrev;
                }
            }
        }
        if ((prec == TreeInfo.notExpression) && notEnumConst) {
            print(';');
        }
    }

    public void printVarInit(JCVariableDecl tree) {
        int col = out.col;
        if (!ERROR.contentEquals(tree.name))
            col -= tree.name.getByteLength();
        if (cs.spaceAroundAssignOps())
            print(' ');
        print('=');
        int rm = cs.getRightMargin();
        switch(cs.wrapAssignOps()) {
        case WRAP_IF_LONG:
            if (widthEstimator.estimateWidth(tree.init, rm - out.col) + out.col <= cs.getRightMargin()) {
                if(cs.spaceAroundAssignOps())
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            newline();
            toColExactly(cs.alignMultilineAssignment() ? col : out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if(cs.spaceAroundAssignOps())
                print(' ');
            break;
        }
        printNoParenExpr(tree.init);
    }

    @Override
    public void visitSkip(JCSkip tree) {
	print(';');
    }

    @Override
    public void visitBlock(JCBlock tree) {
	printFlags(tree.flags, false);
	printBlock(tree, tree.stats, cs.getOtherBracePlacement(), (tree.flags & Flags.STATIC) != 0 ? cs.spaceBeforeStaticInitLeftBrace() : false);
    }

    @Override
    public void visitDoLoop(JCDoWhileLoop tree) {
	print("do");
        if (cs.spaceBeforeDoLeftBrace())
            print(' ');
	printIndentedStat(tree.body, cs.redundantDoWhileBraces(), cs.spaceBeforeDoLeftBrace(), cs.wrapDoWhileStatement());
        boolean prevblock = tree.body.getKind() == Tree.Kind.BLOCK || cs.redundantDoWhileBraces() == BracesGenerationStyle.GENERATE;
        if (cs.placeWhileOnNewLine() || !prevblock) {
            newline();
            toLeftMargin();
        } else if (cs.spaceBeforeWhile()) {
	    needSpace();
        }
	print("while");
        print(cs.spaceBeforeWhileParen() ? " (" : "(");
        if (cs.spaceWithinWhileParens())
            print(' ');
	printNoParenExpr(tree.cond);
	print(cs.spaceWithinWhileParens()? " );" : ");");
    }

    @Override
    public void visitWhileLoop(JCWhileLoop tree) {
	print("while");
        print(cs.spaceBeforeWhileParen() ? " (" : "(");
        if (cs.spaceWithinWhileParens())
            print(' ');
	printNoParenExpr(tree.cond);
	print(cs.spaceWithinWhileParens() ? " )" : ")");
	printIndentedStat(tree.body, cs.redundantWhileBraces(), cs.spaceBeforeWhileLeftBrace(), cs.wrapWhileStatement());
    }

    @Override
    public void visitForLoop(JCForLoop tree) {
	print("for");
        print(cs.spaceBeforeForParen() ? " (" : "(");
        if (cs.spaceWithinForParens())
            print(' ');
        int col = out.col;
	if (tree.init.nonEmpty()) {
	    if (tree.init.head.getTag() == JCTree.VARDEF) {
		printNoParenExpr(tree.init.head);
		for (List<? extends JCTree> l = tree.init.tail; l.nonEmpty(); l = l.tail) {
		    JCVariableDecl vdef = (JCVariableDecl) l.head;
		    print(", " + vdef.name + " = ");
		    printNoParenExpr(vdef.init);
		}
	    } else {
		printExprs(tree.init);
	    }
	}
        String sep = cs.spaceBeforeSemi() ? " ;" : ";";
	print(sep);
        if (tree.cond != null) {
            switch(cs.wrapFor()) {
            case WRAP_IF_LONG:
                int rm = cs.getRightMargin();
                if (widthEstimator.estimateWidth(tree.cond, rm - out.col) + out.col + 1 <= rm) {
                    if (cs.spaceAfterSemi())
                        print(' ');
                    break;
                }
            case WRAP_ALWAYS:
                newline();
                toColExactly(cs.alignMultilineFor() ? col : out.leftMargin + cs.getContinuationIndentSize());
                break;
            case WRAP_NEVER:
                if (cs.spaceAfterSemi())
                    print(' ');
                break;
            }
	    printNoParenExpr(tree.cond);
        }
	print(sep);
        if (tree.step.nonEmpty()) {
            switch(cs.wrapFor()) {
            case WRAP_IF_LONG:
                int rm = cs.getRightMargin();
                if (widthEstimator.estimateWidth(tree.step, rm - out.col) + out.col + 1 <= rm) {
                    if (cs.spaceAfterSemi())
                        print(' ');
                    break;
                }
            case WRAP_ALWAYS:
                newline();
                toColExactly(cs.alignMultilineFor() ? col : out.leftMargin + cs.getContinuationIndentSize());
                break;
            case WRAP_NEVER:
                if (cs.spaceAfterSemi())
                    print(' ');
                break;
            }
            printExprs(tree.step);
        }
	print(cs.spaceWithinForParens() ? " )" : ")");
	printIndentedStat(tree.body, cs.redundantForBraces(), cs.spaceBeforeForLeftBrace(), cs.wrapForStatement());
    }

    @Override
    public void visitLabelled(JCLabeledStatement tree) {
        toColExactly(cs.absoluteLabelIndent() ? 0 : out.leftMargin);
	print(tree.label);
	print(':');
        int old = out.leftMargin;
        out.leftMargin += cs.getLabelIndent();
        toColExactly(out.leftMargin);
	printStat(tree.body);
        undent(old);
    }

    @Override
    public void visitSwitch(JCSwitch tree) {
	print("switch");
        print(cs.spaceBeforeSwitchParen() ? " (" : "(");
        if (cs.spaceWithinSwitchParens())
            print(' ');
	printNoParenExpr(tree.selector);
        print(cs.spaceWithinSwitchParens() ? " )" : ")");
        int bcol = out.leftMargin;
        switch(cs.getOtherBracePlacement()) {
        case NEW_LINE:
            newline();
            toColExactly(bcol);
            break;
        case NEW_LINE_HALF_INDENTED:
            newline();
	    bcol += (indentSize >> 1);
            toColExactly(bcol);
            break;
        case NEW_LINE_INDENTED:
            newline();
	    bcol += indentSize;
            toColExactly(bcol);
            break;
        }
        if (cs.spaceBeforeSwitchLeftBrace())
            needSpace();
	print('{');
        if (tree.cases.nonEmpty()) {
            newline();
            printStats(tree.cases);
            toColExactly(bcol);
        }
	print('}');
    }

    @Override
    public void visitCase(JCCase tree) {
        int old = cs.indentCasesFromSwitch() ? indent() : out.leftMargin;
        toLeftMargin();
	if (tree.pat == null) {
	    print("default");
	} else {
	    print("case ");
	    printNoParenExpr(tree.pat);
	}
	print(':');
	newline();
	indent();
	printStats(tree.stats);
	undent(old);
    }

    @Override
    public void visitSynchronized(JCSynchronized tree) {
	print("synchronized");
        print(cs.spaceBeforeSynchronizedParen() ? " (" : "(");
        if (cs.spaceWithinSynchronizedParens())
            print(' ');
	printNoParenExpr(tree.lock);
	print(cs.spaceWithinSynchronizedParens() ? " )" : ")");
	printBlock(tree.body, cs.getOtherBracePlacement(), cs.spaceBeforeSynchronizedLeftBrace());
    }

    @Override
    public void visitTry(JCTry tree) {
	print("try");
        if (!tree.getResources().isEmpty()) {
            print(" ("); //XXX: space should be according to the code style!
            for (Iterator<? extends JCTree> it = tree.getResources().iterator(); it.hasNext();) {
                JCTree r = it.next();
                //XXX: disabling copying of original text, as the ending ';' needs to be removed in some cases.
                oldTrees.remove(r);
                printPrecedingComments(r, false);
                printExpr(r, 0);
                printTrailingComments(r, false);
                if (it.hasNext()) print(";");
            }
            print(") "); //XXX: space should be according to the code style!
        }
	printBlock(tree.body, cs.getOtherBracePlacement(), cs.spaceBeforeTryLeftBrace());
	for (List < JCCatch > l = tree.catchers; l.nonEmpty(); l = l.tail)
	    printStat(l.head);
	if (tree.finalizer != null) {
	    printFinallyBlock(tree.finalizer);
	}
    }

    @Override
    public void visitCatch(JCCatch tree) {
        if (cs.placeCatchOnNewLine()) {
            newline();
            toLeftMargin();
        } else if (cs.spaceBeforeCatch()) {
            needSpace();
        }
	print("catch");
        print(cs.spaceBeforeCatchParen() ? " (" : "(");
        if (cs.spaceWithinCatchParens())
            print(' ');
	printNoParenExpr(tree.param);
	print(cs.spaceWithinCatchParens() ? " )" : ")");
	printBlock(tree.body, cs.getOtherBracePlacement(), cs.spaceBeforeCatchLeftBrace());
    }

    @Override
    public void visitConditional(JCConditional tree) {
        printExpr(tree.cond, TreeInfo.condPrec - 1);
        switch(cs.wrapTernaryOps()) {
        case WRAP_IF_LONG:
            int rm = cs.getRightMargin();
            if (widthEstimator.estimateWidth(tree.truepart, rm - out.col) + out.col + 1 <= rm) {
                if (cs.spaceAroundTernaryOps())
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            newline();
            toColExactly(out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if (cs.spaceAroundTernaryOps())
                print(' ');
            break;
        }
        print(cs.spaceAroundTernaryOps() ? "? " : "?");
        printExpr(tree.truepart, TreeInfo.condPrec);
        switch(cs.wrapTernaryOps()) {
        case WRAP_IF_LONG:
            int rm = cs.getRightMargin();
            if (widthEstimator.estimateWidth(tree.falsepart, rm - out.col) + out.col + 1 <= rm) {
                if (cs.spaceAroundTernaryOps())
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            newline();
            toColExactly(out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if (cs.spaceAroundTernaryOps())
                print(' ');
            break;
        }
        print(cs.spaceAroundTernaryOps() ? ": " : ":");
        printExpr(tree.falsepart, TreeInfo.condPrec);
    }

    @Override
    public void visitIf(JCIf tree) {
	print("if");
        print(cs.spaceBeforeIfParen() ? " (" : "(");
        if (cs.spaceWithinIfParens())
            print(' ');
	printNoParenExpr(tree.cond);
	print(cs.spaceWithinIfParens() ? " )" : ")");
        boolean prevblock = tree.thenpart.getKind() == Tree.Kind.BLOCK && cs.redundantIfBraces() != BracesGenerationStyle.ELIMINATE || cs.redundantIfBraces() == BracesGenerationStyle.GENERATE;
	if (tree.elsepart != null && danglingElseChecker.hasDanglingElse(tree.thenpart)) {
	    printBlock(tree.thenpart, cs.getOtherBracePlacement(), cs.spaceBeforeIfLeftBrace());
	    prevblock = true;
	} else
	    printIndentedStat(tree.thenpart, cs.redundantIfBraces(), cs.spaceBeforeIfLeftBrace(), cs.wrapIfStatement());
	if (tree.elsepart != null) {
        printElse(tree, prevblock);
	}
    }

    public void printElse(JCIf tree, boolean prevblock) {
	    if (cs.placeElseOnNewLine() || !prevblock) {
                newline();
                toLeftMargin();
            } else if (cs.spaceBeforeElse()) {
		needSpace();
            }
	    print("else");
	    if (tree.elsepart.getKind() == Tree.Kind.IF && cs.specialElseIf()) {
		needSpace();
		printStat(tree.elsepart);
	    } else
		printIndentedStat(tree.elsepart, cs.redundantIfBraces(), cs.spaceBeforeElseLeftBrace(), cs.wrapIfStatement());
    }

    @Override
    public void visitExec(JCExpressionStatement tree) {
	printNoParenExpr(tree.expr);
	if (prec == TreeInfo.notExpression)
	    print(';');
    }

    @Override
    public void visitBreak(JCBreak tree) {
	print("break");
	if (tree.label != null) {
	    needSpace();
	    print(tree.label);
	}
	print(';');
    }

    @Override
    public void visitContinue(JCContinue tree) {
	print("continue");
	if (tree.label != null) {
	    needSpace();
	    print(tree.label);
	}
	print(';');
    }

    @Override
    public void visitReturn(JCReturn tree) {
	print("return");
	if (tree.expr != null) {
	    needSpace();
	    printNoParenExpr(tree.expr);
	}
	print(';');
    }

    @Override
    public void visitThrow(JCThrow tree) {
	print("throw ");
	printNoParenExpr(tree.expr);
	print(';');
    }

    @Override
    public void visitAssert(JCAssert tree) {
	print("assert ");
	printExpr(tree.cond);
	if (tree.detail != null) {
            print(cs.spaceBeforeColon() ? " :" : ":");
            switch(cs.wrapAssert()) {
            case WRAP_IF_LONG:
                int rm = cs.getRightMargin();
                if (widthEstimator.estimateWidth(tree.detail, rm - out.col) + out.col + 1 <= rm) {
                    if (cs.spaceAfterColon())
                        print(' ');
                    break;
                }
            case WRAP_ALWAYS:
                newline();
                toColExactly(out.leftMargin + cs.getContinuationIndentSize());
                break;
            case WRAP_NEVER:
                if (cs.spaceAfterColon())
                    print(' ');
                break;
            }
	    printExpr(tree.detail);
	}
	print(';');
    }

    @Override
    public void visitApply(JCMethodInvocation tree) {
        int prevPrec = this.prec;
        this.prec = TreeInfo.postfixPrec;
        printMethodSelect(tree);
        this.prec = prevPrec;
	print(cs.spaceBeforeMethodCallParen() ? " (" : "(");
        if (cs.spaceWithinMethodCallParens() && tree.args.nonEmpty())
            print(' ');
	wrapTrees(tree.args, cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs()
                ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	print(cs.spaceWithinMethodCallParens() && tree.args.nonEmpty() ? " )" : ")");
    }

    public void printMethodSelect(JCMethodInvocation tree) {
        if (tree.meth.getTag() == JCTree.SELECT) {
            JCFieldAccess left = (JCFieldAccess)tree.meth;
            printExpr(left.selected);
            print('.');
            if (left.selected.getTag() == JCTree.APPLY) {
                switch(cs.wrapChainedMethodCalls()) {
                case WRAP_IF_LONG:
                    int rm = cs.getRightMargin();
                    int estWidth = left.name.length();
                    if (tree.typeargs.nonEmpty())
                        estWidth += widthEstimator.estimateWidth(tree.typeargs, rm - out.col - estWidth) + 2;
                    estWidth += widthEstimator.estimateWidth(tree.args, rm - out.col - estWidth) + 2;
                    if (estWidth + out.col <= rm)
                        break;
                case WRAP_ALWAYS:
                    newline();
                    toColExactly(out.leftMargin + cs.getContinuationIndentSize());
                    break;
                }
            }
            if (tree.typeargs.nonEmpty())
                printTypeArguments(tree.typeargs);
            print(left.name);
        } else {
            if (tree.typeargs.nonEmpty())
                printTypeArguments(tree.typeargs);
            printExpr(tree.meth);
        }
    }
    @Override
    public void visitNewClass(JCNewClass tree) {
	if (tree.encl != null) {
	    printExpr(tree.encl);
	    print('.');
	}
	print("new ");
        if (tree.typeargs.nonEmpty()) {
            print("<");
            printExprs(tree.typeargs);
            print(">");
        }
	if (tree.encl == null)
	    print(tree.clazz);
	else if (tree.clazz.type != null)
	    print(tree.clazz.type.tsym.name);
	else
	    print(tree.clazz);
	print(cs.spaceBeforeMethodCallParen() ? " (" : "(");
        if (cs.spaceWithinMethodCallParens()  && tree.args.nonEmpty())
            print(' ');
	wrapTrees(tree.args, cs.wrapMethodCallArgs(), cs.alignMultilineCallArgs()
                ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	print(cs.spaceWithinMethodCallParens() && tree.args.nonEmpty() ? " )" : ")");
	if (tree.def != null) {
            printNewClassBody(tree);
	}
    }
    
    public void printNewClassBody(JCNewClass tree) {
        Name enclClassNamePrev = enclClassName;
        enclClassName = tree.def.name;
        printBlock(null, tree.def.defs, cs.getOtherBracePlacement(), cs.spaceBeforeClassDeclLeftBrace(), true);
        enclClassName = enclClassNamePrev;
    }

    @Override
    public void visitNewArray(JCNewArray tree) {
	if (tree.elemtype != null) {
	    print("new ");
	    int n = tree.elems != null ? 1 : 0;
	    JCTree elemtype = tree.elemtype;
	    while (elemtype.getTag() == JCTree.TYPEARRAY) {
		n++;
		elemtype = ((JCArrayTypeTree) elemtype).elemtype;
	    }
	    printExpr(elemtype);
	    for (List<? extends JCTree> l = tree.dims; l.nonEmpty(); l = l.tail) {
		print(cs.spaceWithinArrayInitBrackets() ? "[ " : "[");
		printNoParenExpr(l.head);
		print(cs.spaceWithinArrayInitBrackets() ? " ]" : "]");
	    }
	    while(--n >= 0)
                print(cs.spaceWithinArrayInitBrackets() ? "[ ]" : "[]");
	}
	if (tree.elems != null) {
            if (cs.spaceBeforeArrayInitLeftBrace())
                needSpace();
	    print('{');
            if (cs.spaceWithinBraces())
                print(' ');
	    wrapTrees(tree.elems, cs.wrapArrayInit(), cs.alignMultilineArrayInit()
                    ? out.col : out.leftMargin + cs.getContinuationIndentSize());
	    print(cs.spaceWithinBraces() ? " }" : "}");
	}
    }

    @Override
    public void visitParens(JCParens tree) {
	print('(');
        if (cs.spaceWithinParens())
            print(' ');
	printExpr(tree.expr);
	print(cs.spaceWithinParens() ? " )" : ")");
    }

    @Override
    public void visitAssign(JCAssign tree) {
        int col = out.col;
	printExpr(tree.lhs, TreeInfo.assignPrec + 1);
        boolean spaceAroundAssignOps = cs.spaceAroundAssignOps();
	if (spaceAroundAssignOps)
            print(' ');
	print('=');
	int rm = cs.getRightMargin();
        switch(cs.wrapAssignOps()) {
        case WRAP_IF_LONG:
            if (widthEstimator.estimateWidth(tree.rhs, rm - out.col) + out.col <= cs.getRightMargin()) {
                if(spaceAroundAssignOps)
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            newline();
            toColExactly(cs.alignMultilineAssignment() ? col : out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if(spaceAroundAssignOps)
                print(' ');
            break;
        }
	printExpr(tree.rhs, TreeInfo.assignPrec);
    }

    @Override
    public void visitAssignop(JCAssignOp tree) {
        int col = out.col;
	printExpr(tree.lhs, TreeInfo.assignopPrec + 1);
	if (cs.spaceAroundAssignOps())
            print(' ');
	print(treeinfo.operatorName(tree.getTag() - JCTree.ASGOffset));
        print('=');
	int rm = cs.getRightMargin();
        switch(cs.wrapAssignOps()) {
        case WRAP_IF_LONG:
            if (widthEstimator.estimateWidth(tree.rhs, rm - out.col) + out.col <= cs.getRightMargin()) {
                if(cs.spaceAroundAssignOps())
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            newline();
            toColExactly(cs.alignMultilineAssignment() ? col : out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if(cs.spaceAroundAssignOps())
                print(' ');
            break;
        }
	printExpr(tree.rhs, TreeInfo.assignopPrec);
    }

    @Override
    public void visitUnary(JCUnary tree) {
	int ownprec = TreeInfo.opPrec(tree.getTag());
	Name opname = treeinfo.operatorName(tree.getTag());
	if (tree.getTag() <= JCTree.PREDEC) {
            if (cs.spaceAroundUnaryOps()) {
                needSpace();
                print(opname);
                print(' ');
            } else {
                print(opname);
                if (   (tree.getTag() == JCTree.POS && (tree.arg.getTag() == JCTree.POS || tree.arg.getTag() == JCTree.PREINC))
                    || (tree.getTag() == JCTree.NEG && (tree.arg.getTag() == JCTree.NEG || tree.arg.getTag() == JCTree.PREDEC))) {
                    print(' ');
                }
            }
	    printExpr(tree.arg, ownprec);
	} else {
	    printExpr(tree.arg, ownprec);
            if (cs.spaceAroundUnaryOps()) {
                print(' ');
                print(opname);
                print(' ');
            } else {
                print(opname);
            }
	}
    }

    @Override
    public void visitBinary(JCBinary tree) {
	int ownprec = TreeInfo.opPrec(tree.getTag());
	Name opname = treeinfo.operatorName(tree.getTag());
        int col = out.col;
	printExpr(tree.lhs, ownprec);
	if(cs.spaceAroundBinaryOps())
            print(' ');
	print(opname);
        boolean needsSpace =    cs.spaceAroundBinaryOps()
                             || (tree.getTag() == JCTree.PLUS  && (tree.rhs.getTag() == JCTree.POS || tree.rhs.getTag() == JCTree.PREINC))
                             || (tree.getTag() == JCTree.MINUS && (tree.rhs.getTag() == JCTree.NEG || tree.rhs.getTag() == JCTree.PREDEC));
	int rm = cs.getRightMargin();
        switch(cs.wrapBinaryOps()) {
        case WRAP_IF_LONG:
            if (widthEstimator.estimateWidth(tree.rhs, rm - out.col) + out.col <= cs.getRightMargin()) {
                if(needsSpace)
                    print(' ');
                break;
            }
        case WRAP_ALWAYS:
            newline();
            toColExactly(cs.alignMultilineBinaryOp() ? col : out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            if(needsSpace)
                print(' ');
            break;
        }
	printExpr(tree.rhs, ownprec + 1);
    }

    @Override
    public void visitTypeCast(JCTypeCast tree) {
	print(cs.spaceWithinTypeCastParens() ? "( " : "(");
	print(tree.clazz);
	print(cs.spaceWithinTypeCastParens() ? " )" : ")");
        if (cs.spaceAfterTypeCast())
            needSpace();
        if (diffContext.origUnit != null && TreePath.getPath(diffContext.origUnit, tree.expr) != null) {
            int a = TreeInfo.getStartPos(tree.expr);
            int b = TreeInfo.getEndPos(tree.expr, diffContext.origUnit.endPositions);
            print(diffContext.origText.substring(a, b));
            return;
        }
	printExpr(tree.expr, TreeInfo.prefixPrec);
    }

    @Override
    public void visitTypeUnion(JCTypeUnion that) {
        boolean first = true;

        for (JCExpression c : that.getTypeAlternatives()) {
            if (!first) print(" | ");
            print(c);
            first = false;
        }
    }

    @Override
    public void visitTypeTest(JCInstanceOf tree) {
	printExpr(tree.expr, TreeInfo.ordPrec);
	print(" instanceof ");
	print(tree.clazz);
    }

    @Override
    public void visitIndexed(JCArrayAccess tree) {
	printExpr(tree.indexed, TreeInfo.postfixPrec);
	print('[');
	printExpr(tree.index);
	print(']');
    }

    @Override
    public void visitSelect(JCFieldAccess tree) {
        printExpr(tree.selected, TreeInfo.postfixPrec);
        print('.');
        print(tree.name);
    }

    @Override
    public void visitIdent(JCIdent tree) {
        print(tree.name);
    }

    @Override
    public void visitLiteral(JCLiteral tree) {
        long start, end;
        if (   diffContext != null
            && diffContext.origUnit != null
            && (start = diffContext.trees.getSourcePositions().getStartPosition(diffContext.origUnit, tree)) >= 0 //#137564
            && (end = diffContext.trees.getSourcePositions().getEndPosition(diffContext.origUnit, tree)) >= 0
            && origText != null) {
            print(origText.substring((int) start, (int) end));
            return ;
        }
        if (   diffContext != null
            && diffContext.mainUnit != null
            && (start = diffContext.trees.getSourcePositions().getStartPosition(diffContext.mainUnit, tree)) >= 0 //#137564
            && (end = diffContext.trees.getSourcePositions().getEndPosition(diffContext.mainUnit, tree)) >= 0
            && diffContext.mainCode != null) {
            print(diffContext.mainCode.substring((int) start, (int) end));
            return ;
        }
	switch (tree.typetag) {
	  case INT:
	    print(tree.value.toString());
	    break;
	  case LONG:
	    print(tree.value.toString() + "L");
	    break;
	  case FLOAT:
	    print(tree.value.toString() + "F");
	    break;
	  case DOUBLE:
	    print(tree.value.toString());
	    break;
	  case CHAR:
	    print("\'" +
		  quote(
		  String.valueOf((char) ((Number) tree.value).intValue()), '"') +
		  "\'");
	    break;
	   case CLASS:
	    print("\"" + quote((String) tree.value, '\'') + "\"");
	    break;
          case BOOLEAN:
            print(tree.getValue().toString());
            break;
          case BOT:
            print("null");
            break;
	  default:
	    print(tree.value.toString());
	}
    }

    private static String quote(String val, char keep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < val.length(); i++) {
            char c = val.charAt(i);
            if (c != keep) {
                sb.append(Convert.quote(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public void visitTypeIdent(JCPrimitiveTypeTree tree) {
	print(symbols.typeOfTag[tree.typetag].tsym.name);
    }

    @Override
    public void visitTypeArray(JCArrayTypeTree tree) {
	printExpr(tree.elemtype);
	print("[]");
    }

    @Override
    public void visitTypeApply(JCTypeApply tree) {
	printExpr(tree.clazz);
	print('<');
	printExprs(tree.arguments);
	print('>');
    }

    @Override
    public void visitTypeParameter(JCTypeParameter tree) {
	print(tree.name);
	if (tree.bounds.nonEmpty()) {
	    print(" extends ");
	    printExprs(tree.bounds, " & ");
	}
    }

    @Override
    public void visitWildcard(JCWildcard tree) {
	print("" + tree.kind.kind);
	if (tree.kind.kind != BoundKind.UNBOUND)
	    printExpr(tree.inner);
    }

    @Override
    public void visitModifiers(JCModifiers tree) {
	printAnnotations(tree.annotations);
	printFlags(tree.flags);
    }

    @Override
    public void visitAnnotation(JCAnnotation tree) {
        boolean oldInsideAnnotation = insideAnnotation;
        insideAnnotation = true;
        if (!printAnnotationsFormatted(List.of(tree))) {
            print("@");
            printExpr(tree.annotationType);
            if (tree.args.nonEmpty()) {
                print(cs.spaceBeforeAnnotationParen() ? " (" : "(");
                if (cs.spaceWithinAnnotationParens())
                    print(' ');
                printExprs(tree.args);
                print(cs.spaceWithinAnnotationParens() ? " )" : ")");
            }
        }
        insideAnnotation = oldInsideAnnotation;
    }

    @Override
    public void visitForeachLoop(JCEnhancedForLoop tree) {
	print("for");
        print(cs.spaceBeforeForParen() ? " (" : "(");
        if (cs.spaceWithinForParens())
            print(' ');
        printExpr(tree.getVariable());
        String sep = cs.spaceBeforeColon() ? " :" : ":";
        print(cs.spaceAfterColon() ? sep + " " : sep);
        printExpr(tree.getExpression());
        print(cs.spaceWithinForParens() ? " )" : ")");
	printIndentedStat(tree.getStatement(), cs.redundantForBraces(), cs.spaceBeforeForLeftBrace(), cs.wrapForStatement());
    }

    @Override
    public void visitLetExpr(LetExpr tree) {
	print("(let " + tree.defs + " in " + tree.expr + ")");
    }

    @Override
    public void visitErroneous(JCErroneous tree) {
	print("(ERROR)");
        containsError = true;
    }

    @Override
    public void visitTree(JCTree tree) {
	print("(UNKNOWN: " + tree + ")");
	newline();
    }

    /**************************************************************************
     * Private implementation
     *************************************************************************/

    private void print(char c) {
	out.append(c);
    }

    private void needSpace() {
	out.needSpace();
    }

    private void blankLines(int n) {
        out.blanklines(n);
    }

    private void blankLines(JCTree tree, boolean before) {
        if (tree == null) {
            return;
        }
        int n = 0;
        switch (tree.getKind()) {
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                n = before ? cs.getBlankLinesBeforeClass() : cs.getBlankLinesAfterClass();
        	if (((JCClassDecl) tree).defs.nonEmpty() && !before) {
                    n = 0;
                } else {
                    out.blanklines(n);
                    toLeftMargin();
                }
                return;
            case METHOD: // do not handle for sythetic things
        	if ((((JCMethodDecl) tree).mods.flags & Flags.SYNTHETIC) == 0 &&
                    ((JCMethodDecl) tree).name != names.init ||
                    enclClassName != null)
                {
                    n = before ? cs.getBlankLinesBeforeMethods() : cs.getBlankLinesAfterMethods();
                    out.blanklines(n);
        	    toLeftMargin();
                }
                return;
            case VARIABLE: // just for the fields
                if (enclClassName != null && enclClassName != names.empty && (((JCVariableDecl) tree).mods.flags & ENUM) == 0) {
                    n = before ? cs.getBlankLinesBeforeFields() : cs.getBlankLinesAfterFields();
                    out.blanklines(n);
                    if (before) toLeftMargin();
                }
                return;
        }
    }

    private void toColExactly(int n) {
	if (n < out.col) newline();
	out.toCol(n);
    }

    private void printQualified(Symbol t) {
	if (t.owner != null && t.owner.name.getByteLength() > 0
		&& !(t.type instanceof Type.TypeVar)
		&& !(t.owner instanceof MethodSymbol)) {
	    if (t.owner instanceof Symbol.PackageSymbol)
		printAllQualified(t.owner);
	    else
		printQualified(t.owner);
	    print('.');
	}
	print(t.name);
    }

    private void printAllQualified(Symbol t) {
	if (t.owner != null && t.owner.name.getByteLength() > 0) {
	    printAllQualified(t.owner);
	    print('.');
	}
	print(t.name);
    }

    private final class Linearize extends TreeScanner<Boolean, java.util.List<Tree>> {
        @Override
        public Boolean scan(Tree node, java.util.List<Tree> p) {
            p.add(node);
            super.scan(node, p);
            return tree2Tag.containsKey(node);
        }
        @Override
        public Boolean reduce(Boolean r1, Boolean r2) {
            return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
        }
    }

    private final class CopyTags extends TreeScanner<Void, java.util.List<Tree>> {
        private final CompilationUnitTree fake;
        private final SourcePositions sp;
        public CopyTags(CompilationUnitTree fake, SourcePositions sp) {
            this.fake = fake;
            this.sp = sp;
        }
        @Override
        public Void scan(Tree node, java.util.List<Tree> p) {
            if (p.isEmpty()) {
                return null;//the original tree(s) had less elements than the current trees???
            }
            Object tag = tree2Tag.get(p.remove(0));
            if (tag != null) {
                tag2Span.put(tag, new int[] {out.length() + initialOffset + (int) sp.getStartPosition(fake, node), out.length() + initialOffset + (int) sp.getEndPosition(fake, node)});
            }
            return super.scan(node, p);
        }
    }

    private void adjustSpans(Iterable<? extends Tree> original, String code) {
        if (tree2Tag == null) {
            return; //nothing to  copy
        }
        
        java.util.List<Tree> linearized = new LinkedList<Tree>();
        if (!new Linearize().scan(original, linearized) != Boolean.TRUE) {
            return; //nothing to  copy
        }
        
        try {
            ClassPath empty = ClassPathSupport.createClassPath(new URL[0]);
            ClasspathInfo cpInfo = ClasspathInfo.create(JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries(), empty, empty);
            JavacTaskImpl javacTask = JavacParser.createJavacTask(cpInfo, null, null, null, null, null, null);
            com.sun.tools.javac.util.Context ctx = javacTask.getContext();
            JavaCompiler.instance(ctx).genEndPos = true;
            CompilationUnitTree tree = javacTask.parse(FileObjects.memoryFileObject("", "", code)).iterator().next(); //NOI18N
            SourcePositions sp = JavacTrees.instance(ctx).getSourcePositions();
            ClassTree clazz = (ClassTree) tree.getTypeDecls().get(0);

            new CopyTags(tree, sp).scan(clazz.getModifiers().getAnnotations(), linearized);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private boolean reallyPrintAnnotations;

    private static String whitespace(int num) {
        StringBuilder res = new StringBuilder(num);

        while (num-- > 0) {
            res.append(' ');
        }

        return res.toString();
    }

    private boolean printAnnotationsFormatted(List<JCAnnotation> annotations) {
        if (reallyPrintAnnotations) return false;
        
        VeryPretty del = new VeryPretty(diffContext, cs, new HashMap<Tree, Object>(), new HashMap<Object, int[]>(), origText, 0);
        del.reallyPrintAnnotations = true;
        del.printingMethodParams = printingMethodParams;

        del.printAnnotations(annotations);

        String str = del.out.toString();
        int col = printingMethodParams ? out.leftMargin + cs.getContinuationIndentSize() : out.col;
        
        str = Reformatter.reformat(str + " class A{}", cs, cs.getRightMargin() - col);

        str = str.trim().replaceAll("\n", "\n" + whitespace(col));

        adjustSpans(annotations, str);

        str = str.substring(0, str.lastIndexOf("class")).trim();

        print(str);

        return true;
    }
    
    private void printAnnotations(List<JCAnnotation> annotations) {
        if (annotations.isEmpty()) return ;

        if (printAnnotationsFormatted(annotations)) {
            if (!printingMethodParams)
                toColExactly(out.leftMargin);
            else
                out.needSpace();
            return ;
        }
        
        while (annotations.nonEmpty()) {
	    printNoParenExpr(annotations.head);
            if (annotations.tail != null && annotations.tail.nonEmpty()) {
                switch(cs.wrapAnnotations()) {
                case WRAP_IF_LONG:
                    int rm = cs.getRightMargin();
                    if (widthEstimator.estimateWidth(annotations.tail.head, rm - out.col) + out.col + 1 <= rm) {
                        print(' ');
                        break;
                    }
                case WRAP_ALWAYS:
                    newline();
                    toColExactly(out.leftMargin);
                    break;
                case WRAP_NEVER:
                    print(' ');
                    break;
                }
            } else {
                if (!printingMethodParams)
                    toColExactly(out.leftMargin);
            }
            annotations = annotations.tail;
        }
    }

    public void printFlags(long flags) {
        printFlags(flags, true);
    }

    public void printFinallyBlock(JCBlock finalizer) {
        if (cs.placeFinallyOnNewLine()) {
            newline();
            toLeftMargin();
        } else if (cs.spaceBeforeFinally()) {
            needSpace();
        }
        print("finally");
        printBlock(finalizer, cs.getOtherBracePlacement(), cs.spaceBeforeFinallyLeftBrace());
    }

    public void printFlags(long flags, boolean addSpace) {
	print(TreeInfo.flagNames(flags & ~INTERFACE & ~ANNOTATION & ~ENUM));
        if ((flags & StandardFlags) != 0) {
            if (cs.placeNewLineAfterModifiers())
                toColExactly(out.leftMargin);
            else if (addSpace)
	        needSpace();
        }
    }

    public void printBlock(JCTree oldT, JCTree newT, Kind parentKind) {
        switch (parentKind) {
            case ENHANCED_FOR_LOOP:
            case FOR_LOOP:
                printIndentedStat(newT, cs.redundantForBraces(), cs.spaceBeforeForLeftBrace(), cs.wrapForStatement());
                break;
            case WHILE_LOOP:
                printIndentedStat(newT, cs.redundantWhileBraces(), cs.spaceBeforeWhileLeftBrace(), cs.wrapWhileStatement());
                break;
            case IF:
                printIndentedStat(newT, cs.redundantIfBraces(), cs.spaceBeforeIfLeftBrace(), cs.wrapIfStatement());
                break;
            case DO_WHILE_LOOP:
                printIndentedStat(newT, cs.redundantDoWhileBraces(), cs.spaceBeforeDoLeftBrace(), cs.wrapDoWhileStatement());
                if (cs.placeWhileOnNewLine()) {
                    newline();
                    toLeftMargin();
                } else if (cs.spaceBeforeWhile()) {
                    needSpace();
                }
        }
    }

    public void printImportsBlock(java.util.List<? extends JCTree> imports, boolean maybeAppendNewLine) {
        boolean hasImports = !imports.isEmpty();
        CodeStyle.ImportGroups importGroups = null;
        if (hasImports) {
            blankLines(Math.max(cs.getBlankLinesBeforeImports(), diffContext.origUnit.pid != null ? cs.getBlankLinesAfterPackage() : 0));
            if (cs.separateImportGroups())
                importGroups = cs.getImportGroups();
        }
        int lastGroup = -1;
        for (JCTree importStat : imports) {
            if (importGroups != null) {
                Name name = fullName(((JCImport)importStat).qualid);
                int group = name != null ? importGroups.getGroupId(name.toString(), ((JCImport)importStat).staticImport) : -1;
                if (lastGroup >= 0 && lastGroup != group)
                    blankline();
                lastGroup = group;
            }
            printStat(importStat);
            newline();
        }
        if (hasImports && maybeAppendNewLine) {
            blankLines(cs.getBlankLinesAfterImports());
        }
    }

    public void eatChars(int count) {
        out.eatAwayChars(count);
    }

    private void printExpr(JCTree tree) {
	printExpr(tree, TreeInfo.noPrec);
    }

    private void printNoParenExpr(JCTree tree) {
	while (tree instanceof JCParens)
	    tree = ((JCParens) tree).expr;
	printExpr(tree, TreeInfo.noPrec);
    }

    private void printExpr(JCTree tree, int prec) {
	if (tree == null) {
	    print("/*missing*/");
	} else {
	    int prevPrec = this.prec;
	    this.prec = prec;
            doAccept(tree);
	    this.prec = prevPrec;
	}
    }

    private <T extends JCTree >void printExprs(List < T > trees) {
        String sep = cs.spaceBeforeComma() ? " ," : ",";
	printExprs(trees, cs.spaceAfterComma() ? sep + " " : sep);
    }

    private <T extends JCTree >void printExprs(List < T > trees, String sep) {
	if (trees.nonEmpty()) {
	    printNoParenExpr(trees.head);
	    for (List < T > l = trees.tail; l.nonEmpty(); l = l.tail) {
		print(sep);
		printNoParenExpr(l.head);
	    }
	}
    }

    private void printStat(JCTree tree) {
        printStat(tree, false, false);
    }

    private void printStat(JCTree tree, boolean member, boolean first) {
	if(tree==null) print(';');
	else {
            if (!first)
                blankLines(tree, true);
	    printPrecedingComments(tree, !member);
            printExpr(tree, TreeInfo.notExpression);
	    int tag = tree.getTag();
	    if(JCTree.APPLY<=tag && tag<=JCTree.MOD_ASG) print(';');
            printTrailingComments(tree, !member);
            blankLines(tree, false);
	}
    }

    private void printIndentedStat(JCTree tree, BracesGenerationStyle redundantBraces, boolean spaceBeforeLeftBrace, WrapStyle wrapStat) {
        if (fromOffset >= 0 && toOffset >= 0 && (TreeInfo.getStartPos(tree) < fromOffset || TreeInfo.getEndPos(tree, diffContext.origUnit.endPositions) > toOffset))
            redundantBraces = BracesGenerationStyle.LEAVE_ALONE;
	switch(redundantBraces) {
        case GENERATE:
            printBlock(tree, cs.getOtherBracePlacement(), spaceBeforeLeftBrace);
            return;
        case ELIMINATE:
	    while(tree instanceof JCBlock) {
		List<JCStatement> t = ((JCBlock) tree).stats;
		if(t.isEmpty() || t.tail.nonEmpty()) break;
		if (t.head instanceof JCVariableDecl)
		    // bogus code has a variable declaration -- leave alone.
		    break;
		printPrecedingComments(tree, true);
		tree = t.head;
	    }
        case LEAVE_ALONE:
            if (tree instanceof JCBlock) {
                printBlock(tree, cs.getOtherBracePlacement(), spaceBeforeLeftBrace);
                return;
            }
            int old = indent();
            switch(wrapStat) {
            case WRAP_NEVER:
                if (spaceBeforeLeftBrace)
                    needSpace();
                printStat(tree);
                undent(old);
                return;
            case WRAP_IF_LONG:
                int oldhm = out.harden();
                int oldc = out.col;
                int oldu = out.used;
                int oldm = out.leftMargin;
                try {
                    if (spaceBeforeLeftBrace)
                        needSpace();
                    printStat(tree);
                    undent(old);
                    out.restore(oldhm);
                    return;
                } catch(Throwable t) {
                    out.restore(oldhm);
                    out.col = oldc;
                    out.used = oldu;
                    out.leftMargin = oldm;
                }
            case WRAP_ALWAYS:
                if (out.col > 0)
                    newline();
                toLeftMargin();
                printStat(tree);
                undent(old);
            }
	}
    }

    private <T extends JCTree >void printStats(List < T > trees) {
        printStats(trees, false);
    }

    private <T extends JCTree >void printStats(List < T > trees, boolean members) {
        java.util.List<T> filtered = new ArrayList<T>(trees.size());
	for (List < T > l = trees; l.nonEmpty(); l = l.tail) {
            T t = l.head;
            if (isSynthetic(t))
                continue;
            filtered.add(t);
        }

        if (!filtered.isEmpty() && handlePossibleOldTrees(filtered, true)) return;

        boolean first = true;
	for (T t : filtered) {
	    toColExactly(out.leftMargin);
	    printStat(t, members, first);
            first = false;
	}
    }

    private void printBlock(JCTree t, BracePlacement bracePlacement, boolean spaceBeforeLeftBrace) {
        JCTree block;
	List<? extends JCTree> stats;
	if (t instanceof JCBlock) {
            block = t;
	    stats = ((JCBlock) t).stats;
        } else {
            block = null;
	    stats = List.of(t);
        }
	printBlock(block, stats, bracePlacement, spaceBeforeLeftBrace);
    }

    private void printBlock(JCTree tree, List<? extends JCTree> stats, BracePlacement bracePlacement, boolean spaceBeforeLeftBrace) {
        printBlock(tree, stats, bracePlacement, spaceBeforeLeftBrace, false);
    }

    private void printBlock(JCTree tree, List<? extends JCTree> stats, BracePlacement bracePlacement, boolean spaceBeforeLeftBrace, boolean members) {
        printPrecedingComments(tree, true);
	int old = indent();
	int bcol = old;
        switch(bracePlacement) {
        case NEW_LINE:
            newline();
            toColExactly(old);
            break;
        case NEW_LINE_HALF_INDENTED:
            newline();
	    bcol += (indentSize >> 1);
            toColExactly(bcol);
            break;
        case NEW_LINE_INDENTED:
            newline();
	    bcol = out.leftMargin;
            toColExactly(bcol);
            break;
        }
        if (spaceBeforeLeftBrace)
            needSpace();
	print('{');
        boolean emptyBlock = true;
        for (List<? extends JCTree> l = stats; l.nonEmpty(); l = l.tail) {
            if (!isSynthetic(l.head)) {
                emptyBlock = false;
                break;
            }
        }
	if (emptyBlock) {
            printEmptyBlockComments(tree, members);
        } else {
            if (members)
                blankLines(enclClassName.isEmpty() ? cs.getBlankLinesAfterAnonymousClassHeader() : cs.getBlankLinesAfterClassHeader());
            else
                newline();
	    printStats(stats, members);
        }
        toColExactly(bcol);
	undent(old);
	print('}');
        printTrailingComments(tree, true);
    }

    private void printTypeParameters(List < JCTypeParameter > trees) {
	if (trees.nonEmpty()) {
	    print('<');
	    printExprs(trees);
	    print('>');
	}
    }

    private void printTypeArguments(List<? extends JCExpression> typeargs) {
        if (typeargs.nonEmpty()) {
            print('<');
            printExprs(typeargs);
            print('>');
        }
    }

    private void printPrecedingComments(JCTree tree, boolean printWhitespace) {
        CommentSet commentSet = commentHandler.getComments(tree);
        java.util.List<Comment> pc = commentSet.getComments(CommentSet.RelativePosition.PRECEDING);
        if (!pc.isEmpty()) {
            for (Comment c : pc)
                printComment(c, true, printWhitespace);
        }
    }

    private void printTrailingComments(JCTree tree, boolean printWhitespace) {
        CommentSet commentSet = commentHandler.getComments(tree);
        java.util.List<Comment> cl = commentSet.getComments(CommentSet.RelativePosition.INLINE);
        for (Comment comment : cl) {
            printComment(comment, true, printWhitespace);
        }
        java.util.List<Comment> tc = commentSet.getComments(CommentSet.RelativePosition.TRAILING);
        if (!tc.isEmpty()) {
            for (Comment c : tc)
                printComment(c, false, printWhitespace);
        }
    }

    private void printEmptyBlockComments(JCTree tree, boolean printWhitespace) {
//        LinkedList<Comment> comments = new LinkedList<Comment>();
//        if (cInfo != null) {
//            int pos = TreeInfo.getEndPos(tree, diffContext.origUnit.endPositions) - 1;
//            if (pos >= 0) {
//                TokenSequence<JavaTokenId> tokens = cInfo.getTokenHierarchy().tokenSequence(JavaTokenId.language());
//                tokens.move(pos);
//                moveToSrcRelevant(tokens, Direction.BACKWARD);
//                int indent = NOPOS;
//                while (tokens.moveNext() && nonRelevant.contains(tokens.token().id())) {
//                    if (tokens.index() > lastReadCommentIdx) {
//                        switch (tokens.token().id()) {
//                            case LINE_COMMENT:
//                                comments.add(Comment.create(Style.LINE, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
//                                indent = 0;
//                                break;
//                            case BLOCK_COMMENT:
//                                comments.add(Comment.create(Style.BLOCK, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
//                                indent = NOPOS;
//                                break;
//                            case JAVADOC_COMMENT:
//                                comments.add(Comment.create(Style.JAVADOC, tokens.offset(), tokens.offset() + tokens.token().length(), indent, tokens.token().toString()));
//                                indent = NOPOS;
//                                break;
//                            case WHITESPACE:
//                                String tokenText = tokens.token().toString();
//                                comments.add(Comment.create(Style.WHITESPACE, NOPOS, NOPOS, NOPOS, tokenText));
//                                int newLinePos = tokenText.lastIndexOf('\n');
//                                if (newLinePos < 0) {
//                                    if (indent >= 0)
//                                        indent += tokenText.length();
//                                } else {
//                                    indent = tokenText.length() - newLinePos - 1;
//                                }
//                                break;
//                        }
//                        lastReadCommentIdx = tokens.index();
//                    }
//                }
//            }
//        }
        java.util.List<Comment> comments = commentHandler.getComments(tree).getComments(CommentSet.RelativePosition.INNER);
        for (Comment c : comments)
            printComment(c, false, printWhitespace);
    }

    public void printComment(Comment comment, boolean preceding, boolean printWhitespace) {
        printComment(comment, preceding, printWhitespace, false);
    }

    public void printComment(Comment comment, boolean preceding, boolean printWhitespace, boolean preventClosingWhitespace) {
        boolean onlyWhitespaces = out.isWhitespaceLine();
        if (Comment.Style.WHITESPACE == comment.style()) {
            if (false && printWhitespace) {
                char[] data = comment.getText().toCharArray();
                int n = -1;
                for (int i = 0; i < data.length; i++) {
                    if (data[i] == '\n') {
                        n++;
                    }
                }
                if (n > 0) {
                    if (out.lastBlankLines > 0 && out.lastBlankLines < n)
                        n = out.lastBlankLines;
                    blankLines(n);
                    toLeftMargin();
                }
            }
            return;
        }
        String body = comment.getText();
        boolean rawBody = body.length() == 0 || body.charAt(0) != '/';
        LinkedList<CommentLine> lines = new LinkedList<CommentLine>();
        int stpos = -1;
        int limit = body.length();
        for (int i = 0; i < limit; i++) {
            char c = body.charAt(i);
            if (c == '\n') {
                lines.add(new CommentLine(stpos, stpos < 0 ? 0 : i - stpos, body));
                stpos = -1;
            } else if (c > ' ' && stpos < 0) {
                stpos = i;
            }
        }
        if (stpos >= 0 && stpos < limit)
            lines.add(new CommentLine(stpos, limit - stpos, body));
        if (comment.indent() == 0) {
            if (!preceding && out.lastBlankLines == 0 && comment.style() != Style.LINE)
                newline();
            out.toLineStart();
        } else if (comment.indent() > 0 && !preceding) {
            if (out.lastBlankLines == 0 && comment.style() != Style.LINE)
                newline();
            toLeftMargin();
        } else if (comment.indent() < 0 && !preceding) {
            if (out.lastBlankLines == 0)
                newline();
            toLeftMargin();
        } else {
            needSpace();
        }
        if (rawBody) {
            switch (comment.style()) {
                case LINE:
                    print("// ");
                    break;
                case BLOCK:
                    print("/* ");
                    break;
                case JAVADOC:
                    if (!onlyWhitespaces)
                        newline();
                    toLeftMargin();
                    print("/**");
                    newline();
                    toLeftMargin();
                    print(" * ");
            }
        }
        if (!lines.isEmpty())
            lines.removeFirst().print(out.col);
        while (!lines.isEmpty()) {
            newline();
            toLeftMargin();
            CommentLine line = lines.removeFirst();
            if (rawBody)
                print(" * ");
            else if (line.body.charAt(line.startPos) == '*')
                print(' ');
            line.print(out.col);
        }
        if (rawBody) {
            switch (comment.style()) {
                case BLOCK:
                    print(" */");
                    break;
                case JAVADOC:
                    newline();
                    toLeftMargin();
                    print(" */");
                    newline();
                    toLeftMargin();
                    break;
            }
        }
        if ((onlyWhitespaces && !preventClosingWhitespace) || comment.style() == Style.LINE) {
            newline();
            if (!preventClosingWhitespace) {
                toLeftMargin();
            }
        } else {
            if (!preventClosingWhitespace) {
                needSpace();
            }
        }
    }

    private void wrap(String s, WrapStyle wrapStyle) {
        switch(wrapStyle) {
        case WRAP_IF_LONG:
            if (s.length() + out.col <= cs.getRightMargin()) {
                print(' ');
                break;
            }
        case WRAP_ALWAYS:
            newline();
            toColExactly(out.leftMargin + cs.getContinuationIndentSize());
            break;
        case WRAP_NEVER:
            print(' ');
            break;
        }
        print(s);
    }

    private <T extends JCTree> void wrapTrees(List<T> trees, WrapStyle wrapStyle, int wrapIndent) {
        boolean first = true;
        for (List < T > l = trees; l.nonEmpty(); l = l.tail) {
            if (!first) {
                print(cs.spaceBeforeComma() ? " ," : ",");
                switch(wrapStyle) {
                case WRAP_IF_LONG:
                    int rm = cs.getRightMargin();
                    if (widthEstimator.estimateWidth(l.head, rm - out.col) + out.col + 1 <= rm) {
                        if (cs.spaceAfterComma())
                            print(' ');
                        break;
                    }
                case WRAP_ALWAYS:
                    newline();
                    toColExactly(wrapIndent);
                    break;
                case WRAP_NEVER:
                    if (cs.spaceAfterComma())
                        print(' ');
                    break;
                }
            }
            printNoParenExpr(l.head);
            first = false;
        }
    }

    public Name fullName(JCTree tree) {
	switch (tree.getTag()) {
	case JCTree.IDENT:
	    return ((JCIdent) tree).name;
	case JCTree.SELECT:
            JCFieldAccess sel = (JCFieldAccess)tree;
	    Name sname = fullName(sel.selected);
	    return sname != null && sname.getByteLength() > 0 ? sname.append('.', sel.name) : sel.name;
	default:
	    return null;
	}
    }

    // consider usage of TreeUtilities.isSynthethic() - currently tree utilities
    // is not available in printing class and method is insufficient for our
    // needs.
    private boolean isSynthetic(JCTree tree) {
        if (tree.getKind() == Kind.METHOD) {
            //filter synthetic constructors
            return (((JCMethodDecl)tree).mods.flags & Flags.GENERATEDCONSTR) != 0L;
        }
        //filter synthetic superconstructor calls
        if (tree.getKind() == Kind.EXPRESSION_STATEMENT && diffContext.origUnit != null) {
            JCExpressionStatement est = (JCExpressionStatement) tree;
            if (est.expr.getKind() == Kind.METHOD_INVOCATION) {
                JCMethodInvocation mit = (JCMethodInvocation) est.getExpression();
                if (mit.meth.getKind() == Kind.IDENTIFIER) {
                    JCIdent it = (JCIdent) mit.getMethodSelect();
                    return it.name == names._super && diffContext.syntheticTrees.contains(tree);
                }
            }
        }
	return false;
    }

    /** Is the given tree an enumerator definition? */
    private static boolean isEnumerator(JCTree tree) {
        return tree.getTag() == JCTree.VARDEF && (((JCVariableDecl) tree).mods.flags & ENUM) != 0;
    }

    private String replace(String a,String b) {
        a = a.replace(b, out.toString());
        out.clear();
        return a;
    }

    private class CommentLine {
	private int startPos;
	private int length;
        private String body;
	CommentLine(int sp, int l, String b) {
	    if((length = l)==0) {
		startPos = 0;
	    } else {
		startPos = sp;
            }
            body = b;
	}
	public void print(int col) {
	    if(length>0) {
		int limit = startPos+length;
		for(int i = startPos; i<limit; i++)
		    out.append(body.charAt(i));
	    }
	}
    }
}
