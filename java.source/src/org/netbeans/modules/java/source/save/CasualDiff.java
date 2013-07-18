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
package org.netbeans.modules.java.source.save;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import static com.sun.source.doctree.DocTree.Kind.RETURN;
import org.netbeans.api.java.source.WorkingCopy;
import com.sun.tools.javac.util.Names;
import java.util.*;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.util.logging.Logger;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.source.transform.FieldGroupTree;
import static com.sun.source.tree.Tree.*;
import com.sun.source.util.DocSourcePositions;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.api.JavacTrees;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.api.java.source.Comment;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.DCTree;
import com.sun.tools.javac.tree.DCTree.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.Pretty;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Position;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.modules.java.source.pretty.VeryPretty;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import static java.util.logging.Level.*;

import static org.netbeans.modules.java.source.save.ListMatcher.*;
import static com.sun.tools.javac.code.Flags.*;
import java.util.Map.Entry;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Indent;

import static org.netbeans.modules.java.source.save.PositionEstimator.*;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

public class CasualDiff {

    public static boolean OLD_TREES_VERBATIM = Boolean.parseBoolean(System.getProperty(WorkingCopy.class.getName() + ".keep-old-trees", "true"));

    protected final Collection<Diff> diffs;
    protected CommentHandler comments;
    protected JCCompilationUnit oldTopLevel;
    protected final DiffContext diffContext;

    private TokenSequence<JavaTokenId> tokenSequence;
    private String origText;
    private VeryPretty printer;
    private final Context context;
    private final Names names;
    private static final Logger LOG = Logger.getLogger(CasualDiff.class.getName());

    private Map<Integer, String> diffInfo = new HashMap<Integer, String>();
    private final Map<Tree, ?> tree2Tag;
    private final Map<Object, int[]> tag2Span;
    private final Set<Tree> oldTrees;
    private final Map<Tree, DocCommentTree> tree2Doc;

    // used for diffing var def, when parameter is printed, annotation of
    // such variable should not provide new line at the end.
    private boolean parameterPrint = false;
    private boolean enumConstantPrint = false;

    protected CasualDiff(Context context, DiffContext diffContext, Map<Tree, ?> tree2Tag, Map<Tree, DocCommentTree> tree2Doc, Map<?, int[]> tag2Span, Set<Tree> oldTrees) {
        diffs = new LinkedHashSet<Diff>();
        comments = CommentHandlerService.instance(context);
        this.diffContext = diffContext;
        this.tokenSequence = diffContext.tokenSequence;
        this.origText = diffContext.origText;
        this.context = context;
        this.names = Names.instance(context);
        this.tree2Tag = tree2Tag;
        this.tree2Doc = tree2Doc;
        this.tag2Span = (Map<Object, int[]>) tag2Span;//XXX
        printer = new VeryPretty(diffContext, diffContext.style, tree2Tag, tree2Doc, tag2Span, origText);
        printer.oldTrees = oldTrees;
        this.oldTrees = oldTrees;
    }

    private Collection<Diff> getDiffs() {
        return diffs;
    }

    public static Collection<Diff> diff(Context context,
            DiffContext diffContext,
            TreePath oldTreePath,
            JCTree newTree,
            Map<Integer, String> userInfo,
            Map<Tree, ?> tree2Tag,
            Map<Tree, DocCommentTree> tree2Doc,
            Map<?, int[]> tag2Span,
            Set<Tree> oldTrees)
    {
        final CasualDiff td = new CasualDiff(context, diffContext, tree2Tag, tree2Doc, tag2Span, oldTrees);
        JCTree oldTree = (JCTree) oldTreePath.getLeaf();
        td.oldTopLevel =  (JCCompilationUnit) (oldTree.getKind() == Kind.COMPILATION_UNIT ? oldTree : diffContext.origUnit);

        for (Tree t : oldTreePath) {
            if (t == oldTree) continue;
            
            List<? extends Tree> embeddedElements;
            
            if (TreeUtilities.CLASS_TREE_KINDS.contains(t.getKind())) {
                embeddedElements = ((ClassTree) t).getMembers();
            } else if (t.getKind() == Kind.BLOCK) {
                embeddedElements = ((BlockTree) t).getStatements();
            } else {
                continue;
            }
            
            embeddedElements = td.filterHidden(NbCollections.checkedListByCopy(embeddedElements, JCTree.class, false));
            
            if (embeddedElements.isEmpty()) {
                int indent = getOldIndent(diffContext, t);

                if (indent < 0) {
                    td.printer.indent();
                } else {
                    td.printer.setIndent(indent);
                    td.printer.indent();
                    break;
                }
            } else {
                int indent = getOldIndent(diffContext, embeddedElements.get(0));

                if (indent < 0) {
                    td.printer.indent();
                } else {
                    td.printer.setIndent(indent);
                    break;
                }
            }
        }

        if (org.netbeans.api.java.source.TreeUtilities.CLASS_TREE_KINDS.contains(oldTree.getKind()) && oldTreePath.getParentPath().getLeaf().getKind() == Kind.NEW_CLASS) {
            td.anonClass = true;
        }

        int[] bounds = td.getCommentCorrectedBounds(oldTree);
        boolean isCUT = oldTree.getKind() == Kind.COMPILATION_UNIT;
        int start = isCUT ? 0 : bounds[0];
        String origText = td.origText;
        int end   = isCUT ? origText.length() : bounds[1];

        //#177660: LineMap is probably not updated correctly for partial reparse, workaround:
        int lineStart = start;

        while (lineStart > 0 && origText.charAt(lineStart - 1) != '\n') {
            lineStart--;
        }
        //was:
//        int ln = td.oldTopLevel.lineMap.getLineNumber(start);
//        int lineStart = td.oldTopLevel.lineMap.getStartPosition(ln);
        
        td.printer.setInitialOffset(lineStart);

        Tree current = oldTree;

        for (Tree t : oldTreePath) {
            if (t.getKind() == Kind.METHOD) {
                MethodTree mt = (MethodTree) t;

                for (Tree p : mt.getParameters()) {
                    if (p == current) {
                        td.parameterPrint = true;
                    }
                }
                break;
            } else if (t.getKind() == Kind.VARIABLE) {
                JCVariableDecl vt = (JCVariableDecl) t;
                if ((vt.mods.flags & ENUM) != 0 && vt.init == current)
                    td.enumConstantPrint = true;
            }

            current = t;
        }

        if (oldTree.getKind() == Kind.MODIFIERS || oldTree.getKind() == Kind.METHOD || (!td.parameterPrint && oldTree.getKind() == Kind.VARIABLE)) {
            td.tokenSequence.move(start);
            if (td.tokenSequence.movePrevious() && td.tokenSequence.token().id() == JavaTokenId.WHITESPACE) {
                String text = td.tokenSequence.token().text().toString();
                int index = text.lastIndexOf('\n');
                start = td.tokenSequence.offset();
                if (index > -1) {
                    start += index + 1;
                }
            }
        }

        td.printer.print(origText.substring(lineStart, start));
        td.diffTree(oldTree, newTree, (JCTree) (oldTreePath.getParentPath() != null ? oldTreePath.getParentPath().getLeaf() : null), new int[] {start, bounds[1]});
        String resultSrc = td.printer.toString().substring(start - lineStart);
        if (!td.printer.reindentRegions.isEmpty()) {
            try {
                String toParse = origText.substring(0, start) + resultSrc + origText.substring(end);
                BaseDocument doc = new BaseDocument(false, "text/x-java");
                doc.insertString(0, toParse, null);
                doc.putProperty(Language.class, JavaTokenId.language());
                doc.putProperty(Document.StreamDescriptionProperty, diffContext.file);
                javax.swing.text.Position startPos = doc.createPosition(start);
                javax.swing.text.Position endPos = doc.createPosition(start + resultSrc.length());
                Map<Object, javax.swing.text.Position[]> spans = new IdentityHashMap<>(td.tag2Span.size());
                for (Entry<Object, int[]> e : td.tag2Span.entrySet()) {
                    spans.put(e.getKey(), new javax.swing.text.Position[] {
                        doc.createPosition(e.getValue()[0]),
                        doc.createPosition(e.getValue()[1])
                    });
                }
                final Indent i = Indent.get(doc);
                i.lock();
                try {
                    doc.runAtomic(new Runnable() {
                        @Override public void run() {
                            for (int[] region : td.printer.reindentRegions) {
                                try {
                                    i.reindent(region[0], region[1]);
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    });
                } finally {
                    i.unlock();
                }
                resultSrc = doc.getText(startPos.getOffset(), endPos.getOffset() - startPos.getOffset());
                for (Entry<Object, javax.swing.text.Position[]> e : spans.entrySet()) {
                    int[] span = td.tag2Span.get(e.getKey());
                    span[0] = e.getValue()[0].getOffset();
                    span[1] = e.getValue()[1].getOffset();
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        String originalText = isCUT ? origText : origText.substring(start, end);
        userInfo.putAll(td.diffInfo);

        return DiffUtilities.diff(originalText, resultSrc, start);
    }

    public static Collection<Diff> diff(Context context,
            DiffContext diffContext,
            List<? extends ImportTree> original,
            List<? extends ImportTree> nue,
            Map<Integer, String> userInfo,
            Map<Tree, ?> tree2Tag,
            Map<Tree, DocCommentTree> tree2Doc,
            Map<?, int[]> tag2Span,
            Set<Tree> oldTrees)
    {
        CasualDiff td = new CasualDiff(context, diffContext, tree2Tag, tree2Doc, tag2Span, oldTrees);
        td.oldTopLevel = diffContext.origUnit;
        int start = td.oldTopLevel.getPackageName() != null ? td.endPos(td.oldTopLevel.getPackageName()) : 0;

        List<JCImport> originalJC = new LinkedList<JCImport>();
        List<JCImport> nueJC = new LinkedList<JCImport>();

        for (ImportTree i : original) {
            originalJC.add((JCImport) i);
        }

        for (ImportTree i : nue) {
            nueJC.add((JCImport) i);
        }

        PositionEstimator est = EstimatorFactory.imports(originalJC, nueJC, td.diffContext);
        int end = td.diffList(originalJC, nueJC, start, est, Measure.DEFAULT, td.printer);

        String resultSrc = td.printer.toString();
        String originalText = td.diffContext.origText.substring(start, end);
        userInfo.putAll(td.diffInfo);

        return DiffUtilities.diff(originalText, resultSrc, start);
    }

    public int endPos(JCTree t) {
        return TreeInfo.getEndPos(t, oldTopLevel.endPositions);
        }

    private int endPos(com.sun.tools.javac.util.List<? extends JCTree> trees) {
        int result = -1;
	if (trees.nonEmpty()) {
	    result = endPos(trees.head);
	    for (com.sun.tools.javac.util.List <? extends JCTree> l = trees.tail; l.nonEmpty(); l = l.tail) {
		result = endPos(l.head);
	    }
	}
        return result;
    }

    private int endPos(List<? extends JCTree> trees) {
        if (trees.isEmpty())
            return -1;
        return endPos(trees.get(trees.size()-1));
    }

    protected void diffTopLevel(JCCompilationUnit oldT, JCCompilationUnit newT) {
        int packageKeywordStart = 0;
        if (oldT.pid != null) {
            tokenSequence.move(oldT.pid.getStartPosition());
            moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
            packageKeywordStart = tokenSequence.offset();
        }
        //when adding first annotation, skip initial comments (typically a license):
        int localPointer = oldT.packageAnnotations.isEmpty() && !newT.packageAnnotations.isEmpty() ? packageKeywordStart : 0;
        oldTopLevel = oldT;
        localPointer = diffAnnotationsLists(oldT.packageAnnotations, newT.packageAnnotations, localPointer, 0);
        localPointer = diffPackageStatement(oldT, newT, packageKeywordStart, localPointer);
        PositionEstimator est = EstimatorFactory.imports(oldT.getImports(), newT.getImports(), diffContext);
        localPointer = diffList(oldT.getImports(), newT.getImports(), localPointer, est, Measure.DEFAULT, printer);
        est = EstimatorFactory.toplevel(oldT.getTypeDecls(), newT.getTypeDecls(), diffContext);
        localPointer = diffList(oldT.getTypeDecls(), newT.getTypeDecls(), localPointer, est, Measure.REAL_MEMBER, printer);
        printer.print(origText.substring(localPointer));
    }
    
    private static int getOldIndent(DiffContext diffContext, Tree t) {
        int offset = (int) diffContext.trees.getSourcePositions().getStartPosition(diffContext.origUnit, t);
        
        if (offset < 0) return -1;
        
        while (offset > 0 && diffContext.origText.charAt(offset - 1) != '\n')
            offset--;
        
        int indent = 0;
        
        while (offset < diffContext.origText.length()) {
            char c = diffContext.origText.charAt(offset++);
            
            if (c == '\t') {
                indent += diffContext.style.getTabSize();
            } else if (c == '\n' || !Character.isWhitespace(c)) {
                break;
            } else {
                indent++;
            }
        }
        
        return indent;
    }
    
    private boolean needStar(int localPointer) {
        if (localPointer <= 0) {
            return false;
        }

        while (localPointer > 0) {
            char c = diffContext.origText.charAt(--localPointer);

            if (c == '\n') {
                return false;
            } else if(!Character.isWhitespace(c)) {
                if(localPointer > 3 && diffContext.origText.substring(localPointer-2, localPointer+1).equals("/**")) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    private int adjustToPreviousNewLine(int oldPos, int localPointer) {
        int offset = oldPos;

        if (offset < 0) {
            return localPointer;
        }

        while (offset > localPointer) {
            char c = diffContext.origText.charAt(--offset);

            if (c == '\n') {
                break;
            } else if(c != '*' && !Character.isWhitespace(c)) {
                return oldPos;
            }
        }

        return offset;
    }

    private static enum ChangeKind {
        INSERT,
        DELETE,
        MODIFY,
        NOCHANGE;
    }

    private ChangeKind getChangeKind(Tree oldT, Tree newT) {
        if (oldT == newT) {
            return ChangeKind.NOCHANGE;
        }
        if (oldT != null && newT != null) {
            return ChangeKind.MODIFY;
        }
        if (oldT != null) {
            return ChangeKind.DELETE;
        } else {
            return ChangeKind.INSERT;
        }
    }

    private int diffPackageStatement(JCCompilationUnit oldT, JCCompilationUnit newT, int packageKeywordStart, int localPointer) {
        ChangeKind change = getChangeKind(oldT.pid, newT.pid);
        switch (change) {
            // packages are the same or not available, i.e. both are null
            case NOCHANGE:
                break;

            // package statement is new, print the keyword and semicolon
            case INSERT:
                printer.printPackage(newT.pid);
                break;

            // package statement was deleted.
            case DELETE:
                copyTo(localPointer, packageKeywordStart);
                tokenSequence.move(endPos(oldT.pid));
                moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                localPointer = tokenSequence.offset() + 1;
                // todo (#pf): check the declaration:
                // package org.netbeans /* aa */;
                break;

            // package statement was modified.
            case MODIFY:
                copyTo(localPointer, getOldPos(oldT.pid));
                localPointer = endPos(oldT.pid);
                printer.print(newT.pid);
                diffInfo.put(getOldPos(oldT.pid), NbBundle.getMessage(CasualDiff.class,"TXT_UpdatePackageStatement"));
                break;
        }
        return localPointer;
    }

    protected int diffImport(JCImport oldT, JCImport newT, int[] bounds) {
        int localPointer = bounds[0];

        int[] qualBounds = getBounds(oldT.getQualifiedIdentifier());
        if (oldT.staticImport == newT.staticImport) {
            copyTo(localPointer, qualBounds[0]);
        } else {
            if (oldT.staticImport) {
                //removing "static":
                moveFwdToToken(tokenSequence, localPointer, JavaTokenId.STATIC);
                copyTo(localPointer, tokenSequence.offset());
            } else {
                copyTo(localPointer, qualBounds[0]);
                printer.print("static ");
            }
        }
        localPointer = diffTree(oldT.getQualifiedIdentifier(), newT.getQualifiedIdentifier(), qualBounds);
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    // TODO: should be here printer.enclClassName be used?
    private Name origClassName = null;
    private Name newClassName = null;

    protected int diffClassDef(JCClassDecl oldT, JCClassDecl newT, int[] bounds) {
        int localPointer = bounds[0];
        final Name origOuterClassName = origClassName;
        final Name newOuterClassName = newClassName;
        int insertHint = localPointer;
        List<JCTree> filteredOldTDefs = filterHidden(oldT.defs);
        List<JCTree> filteredNewTDefs = filterHidden(newT.defs);
        // skip the section when printing anonymous class
        if (anonClass == false) {
        tokenSequence.move(oldT.pos);
        tokenSequence.moveNext(); // First skip as move() does not position to token directly
        tokenSequence.moveNext();
        int afterKindHint = tokenSequence.offset();
        moveToSrcRelevant(tokenSequence, Direction.FORWARD);
        insertHint = tokenSequence.offset();
        localPointer = diffModifiers(oldT.mods, newT.mods, oldT, localPointer);
        if (kindChanged(oldT.mods.flags, newT.mods.flags)) {
            int pos = oldT.pos;
            if ((oldT.mods.flags & Flags.ANNOTATION) != 0) {
                tokenSequence.move(pos);
                tokenSequence.moveNext();
                moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                pos = tokenSequence.offset();
            }
            if ((newT.mods.flags & Flags.ANNOTATION) != 0) {
                copyTo(localPointer, pos);
                printer.print("@interface"); //NOI18N
            } else if ((newT.mods.flags & Flags.ENUM) != 0) {
                copyTo(localPointer, pos);
                printer.print("enum"); //NOI18N
            } else if ((newT.mods.flags & Flags.INTERFACE) != 0) {
                copyTo(localPointer, pos);
                printer.print("interface"); //NOI18N
            } else {
                copyTo(localPointer, pos);
                printer.print("class"); //NOI18N
            }
            localPointer = afterKindHint;
        }
        if (nameChanged(oldT.name, newT.name)) {
            copyTo(localPointer, insertHint);
            printer.print(newT.name);
            diffInfo.put(insertHint, NbBundle.getMessage(CasualDiff.class,"TXT_ChangeClassName"));
            localPointer = insertHint += oldT.name.length();
        } else {
            insertHint += oldT.name.length();
            copyTo(localPointer, localPointer = insertHint);
        }
        origClassName = oldT.name;
        newClassName = newT.name;
        if (oldT.typarams.nonEmpty() && newT.typarams.nonEmpty()) {
            copyTo(localPointer, localPointer = oldT.typarams.head.pos);
        }
        boolean parens = oldT.typarams.isEmpty() && newT.typarams.nonEmpty();
        localPointer = diffParameterList(oldT.typarams, newT.typarams,
                parens ? new JavaTokenId[] { JavaTokenId.LT, JavaTokenId.GT } : null,
                localPointer, Measure.ARGUMENT);
        if (oldT.typarams.nonEmpty()) {
            // if type parameters exists, compute correct end of type parameters.
            // ! specifies the offset for insertHint var.
            // public class Yerba<E, M>! { ...
            insertHint = endPos(oldT.typarams.last());
            tokenSequence.move(insertHint);
            moveToSrcRelevant(tokenSequence, Direction.FORWARD);
            // it can be > (GT) or >> (SHIFT)
            insertHint = tokenSequence.offset() + tokenSequence.token().length();
        }
        switch (getChangeKind(oldT.extending, newT.extending)) {
            case NOCHANGE:
                insertHint = oldT.extending != null ? endPos(oldT.extending) : insertHint;
                copyTo(localPointer, localPointer = insertHint);
                break;
            case MODIFY:
                copyTo(localPointer, getOldPos(oldT.extending));
                localPointer = diffTree(oldT.extending, newT.extending, getBounds(oldT.extending));
                break;

            case INSERT:
                copyTo(localPointer, insertHint);
                printer.print(" extends ");
                printer.print(newT.extending);
                localPointer = insertHint;
                break;
            case DELETE:
                copyTo(localPointer, insertHint);
                localPointer = endPos(oldT.extending);
                break;
        }
        // TODO (#pf): there is some space for optimization. If the new list
        // is also empty, we can skip this computation.
        if (oldT.implementing.isEmpty()) {
            // if there is not any implementing part, we need to adjust position
            // from different place. Look at the examples in all if branches.
            // | represents current adjustment and ! where we want to point to
            if (oldT.extending != null)
                // public class Yerba<E>| extends Object! { ...
                insertHint = endPos(oldT.extending);
            else {
                // currently no need to adjust anything here:
                // public class Yerba<E>|! { ...
            }
        } else {
            // we already have any implements, adjust position to first
            // public class Yerba<E>| implements !Mate { ...
            // Note: in case of all implements classes are removed,
            // diffing mechanism will solve the implements keyword.
            insertHint = oldT.implementing.iterator().next().getStartPosition();
        }
        long flags = oldT.sym != null ? oldT.sym.flags() : oldT.mods.flags;
        PositionEstimator estimator = (flags & INTERFACE) == 0 ?
            EstimatorFactory.implementz(oldT.getImplementsClause(), newT.getImplementsClause(), diffContext) :
            EstimatorFactory.extendz(oldT.getImplementsClause(), newT.getImplementsClause(), diffContext);
        if (!newT.implementing.isEmpty())
            copyTo(localPointer, insertHint);
        localPointer = diffList2(oldT.implementing, newT.implementing, insertHint, estimator);
        insertHint = endPos(oldT) - 1;

        if (filteredOldTDefs.isEmpty()) {
            // if there is nothing in class declaration, use position
            // before the closing curly.
            insertHint = endPos(oldT) - 1;
        } else {
            insertHint = filteredOldTDefs.get(0).getStartPosition()-1;
        }
        tokenSequence.move(insertHint);
        tokenSequence.moveNext();
        insertHint = moveBackToToken(tokenSequence, insertHint, JavaTokenId.LBRACE) + 1;
        } else {
            insertHint = moveFwdToToken(tokenSequence, getOldPos(oldT), JavaTokenId.LBRACE);
            tokenSequence.moveNext();
            insertHint = tokenSequence.offset();
        }
        int old = printer.indent();
        Name origName = printer.enclClassName;
        printer.enclClassName = newT.getSimpleName();
        PositionEstimator est = EstimatorFactory.members(filteredOldTDefs, filteredNewTDefs, diffContext);
        if (localPointer < insertHint)
            copyTo(localPointer, insertHint);
        if ((newT.mods.flags & Flags.ENUM) != 0 && filteredOldTDefs.isEmpty() && !filteredNewTDefs.isEmpty() && !isEnum(filteredNewTDefs.get(0)) && !newT.getSimpleName().isEmpty()) {
            printer.blankline();
            printer.toLeftMargin();
            printer.print(";"); //NOI18N
            printer.newline();
        }
        localPointer = diffList(filteredOldTDefs, filteredNewTDefs, insertHint, est, Measure.REAL_MEMBER, printer);
        printer.enclClassName = origName;
        origClassName = origOuterClassName;
        newClassName = newOuterClassName;
        printer.undent(old);
        if (localPointer != -1 && localPointer < origText.length()) {
            if (origText.charAt(localPointer) == '}') {
                // another stupid hack
                printer.toLeftMargin();
            }
            copyTo(localPointer, bounds[1]);
        }
        return bounds[1];
    }
        
    private boolean isEnum(Tree tree) {
        if (tree instanceof FieldGroupTree) return ((FieldGroupTree) tree).isEnum();
        if (tree instanceof VariableTree) return (((JCVariableDecl) tree).getModifiers().flags & Flags.ENUM) != 0;
        if (tree instanceof ClassTree) return (((JCClassDecl) tree).getModifiers().flags & Flags.ENUM) != 0;
        return false;
    }

    private boolean hasModifiers(JCModifiers mods) {
        return mods != null && (!mods.getFlags().isEmpty() || !mods.getAnnotations().isEmpty());
    }

    protected int diffMethodDef(JCMethodDecl oldT, JCMethodDecl newT, int[] bounds) {
        int localPointer = bounds[0];
        // match modifiers and annotations
        if (!matchModifiers(oldT.mods, newT.mods)) {
            // if new tree has modifiers, print them
            if (hasModifiers(newT.mods)) {
                localPointer = diffModifiers(oldT.mods, newT.mods, oldT, localPointer);
            } else {
                // there are no new modifiers, just skip after existing. --
                // endPos of modifiers are not usable, we have to remove also
                // space. Go to return type in case of method, in case of
                // constructor use whole tree start.
                int oldPos = getOldPos(oldT.mods);
                copyTo(localPointer, oldPos);
                localPointer = oldT.restype != null ? getOldPos(oldT.restype) : oldT.pos;
            }
        }
        // compute the position for type parameters - if type param is empty,
        // use in case of
        // i) method - start position of return type,
        // ii) constructor - start position of tree - i.e. first token after
        //                   modifiers.
        int pos = oldT.typarams.isEmpty() ?
            oldT.restype != null ?
                getOldPos(oldT.restype) :
                oldT.pos :
            getOldPos(oldT.typarams.head);

        if (!listsMatch(oldT.typarams, newT.typarams)) {
            if (newT.typarams.nonEmpty())
                copyTo(localPointer, pos);
            else
                if (hasModifiers(oldT.mods))
                    copyTo(localPointer, endPos(oldT.mods));
            boolean parens = oldT.typarams.isEmpty() || newT.typarams.isEmpty();
            localPointer = diffParameterList(oldT.typarams,
                    newT.typarams,
                    parens ? new JavaTokenId[] { JavaTokenId.LT, JavaTokenId.GT } : null,
                    pos,
                    Measure.ARGUMENT
            );
            if (parens && oldT.typarams.isEmpty()) {
                printer.print(" "); // print the space after type parameter
            }
        }
        if (oldT.restype != null) { // means constructor, skip return type gen.
            int[] restypeBounds = getBounds(oldT.restype);
            copyTo(localPointer, restypeBounds[0]);
            localPointer = diffTree(oldT.restype, newT.restype, restypeBounds);
            if (restypeBounds[1] > localPointer)
                copyTo(localPointer, localPointer = restypeBounds[1]);
        } else if(oldT.restype == null && newT.restype != null) {
            copyTo(localPointer, localPointer = oldT.pos);
            printer.print(newT.restype);
            printer.print(" "); // print the space after return type
        }
        
        int posHint;
        if (oldT.typarams.isEmpty()) {
            posHint = oldT.restype != null ? oldT.restype.getStartPosition() : oldT.getStartPosition();
        } else {
            posHint = oldT.typarams.iterator().next().getStartPosition();
        }
        if ((oldT.name != names.init || origClassName != null) && (newT.name != names.init || newClassName != null)) {
            int origLength = (oldT.name == names.init && origClassName != null ? origClassName.length() : oldT.name.length());
            if (nameChanged(oldT.name, newT.name)) {
                copyTo(localPointer, oldT.pos);
                // use orig class name in case of constructor
                if (newT.name == names.init && (newClassName != null)) {
                    printer.print(newClassName);
                    localPointer = oldT.pos + origLength;
                }
                else {
                    printer.print(newT.name);
                    diffInfo.put(oldT.pos, NbBundle.getMessage(CasualDiff.class,"TXT_RenameMethod",oldT.name));
                    localPointer = oldT.pos + origLength;
                }
            } else {
                copyTo(localPointer, localPointer = (oldT.pos + origLength));
            }
        }
        if (oldT.params.isEmpty()) {
            // compute the position. Find the parameters closing ')', its
            // start position is important for us. This is used when 
            // there was not any parameter in original tree.
            int startOffset = oldT.pos;

            moveFwdToToken(tokenSequence, startOffset, JavaTokenId.RPAREN);
            posHint = tokenSequence.offset();
        } else {
            // take the position of the first old parameter
            posHint = oldT.params.iterator().next().getStartPosition();
        }
        if (!listsMatch(oldT.params, newT.params)) {
            copyTo(localPointer, posHint);
            int old = printer.setPrec(TreeInfo.noPrec);
            parameterPrint = true;
            Name oldEnclClassName = printer.enclClassName;
            printer.enclClassName = null;
            localPointer = diffParameterList(oldT.params, newT.params, null, posHint, Measure.MEMBER);
            printer.enclClassName = oldEnclClassName;
            parameterPrint = false;
            printer.setPrec(old);
        }
        //make sure the ')' is printed:
        moveFwdToToken(tokenSequence, oldT.params.isEmpty() ? posHint : endPos(oldT.params.last()), JavaTokenId.RPAREN);
        tokenSequence.moveNext();
        posHint = tokenSequence.offset();
        if (localPointer < posHint)
            copyTo(localPointer, localPointer = posHint);
        // if abstract, hint is before ending semi-colon, otherwise before method body
        if (oldT.thrown.isEmpty()) {
            if (oldT.body != null) {
                tokenSequence.move(oldT.body.pos);
                moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                tokenSequence.moveNext();
                posHint = tokenSequence.offset();
            } else {
                if (oldT.defaultValue != null) {
                    tokenSequence.move(getOldPos(oldT.defaultValue));

                    while (tokenSequence.movePrevious() && tokenSequence.token().id() != JavaTokenId.DEFAULT)
                        ;

                    moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                    tokenSequence.moveNext();
                    posHint = tokenSequence.offset();
                } else {
                    posHint = endPos(oldT) - 1;
                }
            }
        } else {
            posHint = oldT.thrown.iterator().next().getStartPosition();
        }
        if (!newT.thrown.isEmpty()) //do not copy the "throws" keyword:
            copyTo(localPointer, localPointer = posHint);
        PositionEstimator est = EstimatorFactory.throwz(oldT.getThrows(), newT.getThrows(), diffContext);
        localPointer = diffList2(oldT.thrown, newT.thrown, posHint, est);
        if (oldT.defaultValue != newT.defaultValue) {
            if (oldT.defaultValue == null) {
                printer.print(" default ");
                printer.print(newT.defaultValue);
            } else {
                if (newT.defaultValue == null) {
                    localPointer = endPos(oldT.defaultValue);
                } else {
                    int[] restypeBounds = getBounds(oldT.defaultValue);
                    copyTo(localPointer, restypeBounds[0]);
                    localPointer = diffTree(oldT.defaultValue, newT.defaultValue, restypeBounds);
                    copyTo(localPointer, localPointer = restypeBounds[1]);
                }
            }
        }
        if (newT.body == null && oldT.body != null) {
            localPointer = endPos(oldT.body);
            printer.print(";");
        } else {
            if (oldT.body != null && newT.body != null) {
                int[] bodyBounds = getBounds(oldT.body);
                copyTo(localPointer, bodyBounds[0]);
                localPointer = diffTree(oldT.body, newT.body, bodyBounds);
            }
        }
        // TODO: Missing implementation - default value matching!
        // diffTree(oldT.defaultValue, newT.defaultValue);
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffVarDef(JCVariableDecl oldT, JCVariableDecl newT, int pos) {
        int localPointer = oldT.pos;
        copyTo(pos, localPointer);
        if (nameChanged(oldT.name, newT.name)) {
            copyTo(localPointer, oldT.pos);
            printer.print(newT.name);
            diffInfo.put(oldT.pos, NbBundle.getMessage(CasualDiff.class,"TXT_RenameVariable",oldT.name));
            localPointer = oldT.pos + oldT.name.length();
        }
        if (newT.init != null && oldT.init != null) {
            copyTo(localPointer, localPointer = getOldPos(oldT.init));
            localPointer = diffTree(oldT.init, newT.init, new int[] { localPointer, endPos(oldT.init) });
        } else {
            if (oldT.init != null && newT.init == null) {
                // remove initial value
                pos = getOldPos(oldT.init);
                tokenSequence.move(pos);
                moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                tokenSequence.moveNext();
                int to = tokenSequence.offset();
                copyTo(localPointer, to);
                localPointer = endPos(oldT.init);
            }
            if (oldT.init == null && newT.init != null) {
                int end = endPos(oldT);
                tokenSequence.move(end);
                moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                copyTo(localPointer, localPointer = tokenSequence.offset());
                printer.printVarInit(newT);
            }
        }
        copyTo(localPointer, localPointer = endPos(oldT));
        return localPointer;
    }

    private int diffVarDef(JCVariableDecl oldT, JCVariableDecl newT, int[] bounds) {
        int localPointer = bounds[0];
        // check that it is not enum constant. If so, match it in special way
        if ((oldT.mods.flags & Flags.ENUM) != 0) {
            if (nameChanged(oldT.name, newT.name)) {
                copyTo(localPointer, oldT.pos);
                printer.print(newT.name);
                diffInfo.put(oldT.pos, NbBundle.getMessage(CasualDiff.class,"TXT_RenameEnumConstant",oldT.name));
                localPointer = oldT.pos + oldT.name.length();
            }
            JCNewClass oldInit = (JCNewClass) oldT.init;
            JCNewClass newInit = (JCNewClass) newT.init;
            if (oldInit.args.nonEmpty() && newInit.args.nonEmpty()) {
                copyTo(localPointer, localPointer = getOldPos(oldInit.args.head));
                localPointer = diffParameterList(oldInit.args, newInit.args, null, localPointer, Measure.ARGUMENT);
            }
            if (oldInit.def != null && newInit.def != null) {
                anonClass = true;
                int[] defBounds = new int[] { localPointer, endPos(oldInit.def) } ;
                localPointer = diffTree(oldInit.def, newInit.def, defBounds);
                anonClass = false;
            }
            copyTo(localPointer, bounds[1]);
            return bounds[1];
        }
        if (!matchModifiers(oldT.mods, newT.mods)) {
            // if new tree has modifiers, print them
            if (hasModifiers(newT.mods)) {
                localPointer = diffModifiers(oldT.mods, newT.mods, oldT, localPointer);
            } else {
                if (hasModifiers(oldT.mods)) {
                    int oldPos = getOldPos(oldT.mods);
                    copyTo(localPointer, oldPos);
                    localPointer = getOldPos(oldT.vartype);
                }
            }
        }
        boolean cLikeArray = false, cLikeArrayChange = false;
        int addDimensions = 0;
        if (diffContext.syntheticTrees.contains(oldT.vartype)) {
            if (!diffContext.syntheticTrees.contains(newT.vartype)) {
                copyTo(localPointer, localPointer = oldT.pos);
                printer.print(newT.vartype);
                printer.print(" ");
            }
        } else {
            if (newT.vartype == null) {
                throw new UnsupportedOperationException();
            } else {
                int[] vartypeBounds = getBounds(oldT.vartype);
                addDimensions = dimension(newT.vartype, -1);
                cLikeArray = vartypeBounds[1] > oldT.pos;
                cLikeArrayChange =  cLikeArray && dimension(oldT.vartype, oldT.pos) > addDimensions;
                copyTo(localPointer, vartypeBounds[0]);
                localPointer = diffTree(oldT.vartype, newT.vartype, vartypeBounds);
            }
        }
        if (nameChanged(oldT.name, newT.name)) {
            boolean isOldError = oldT.name == Names.instance(context).error;
            if (!isOldError) {
                copyTo(localPointer, oldT.pos);
            } else {
                printer.print(" ");
            }
            if (cLikeArray) {
                printer.eatChars(1);
                for (int i=0; i< addDimensions; i++) {
                    printer.print("[]");    //NOI18N
                }
                printer.print(" "); //NOI18N
            }
            printer.print(newT.name);
            diffInfo.put(oldT.pos, NbBundle.getMessage(CasualDiff.class,"TXT_RenameVariable",oldT.name));
            if (!isOldError) {
                if (cLikeArray) {
                    int[] clab = getBounds(oldT.vartype);
                    localPointer = clab[1];
                } else {
                    localPointer = oldT.pos + oldT.name.length();
                }
            }
        } else if (cLikeArrayChange) {
            for (int i=0; i< addDimensions; i++) {
                printer.print("[]");    //NOI18N
            }
            printer.print(" "); //NOI18N
            printer.print(newT.name);
        }
        if (newT.init != null && oldT.init != null) {
            copyTo(localPointer, localPointer = getOldPos(oldT.init));
            localPointer = diffTree(oldT.init, newT.init, new int[] { localPointer, endPos(oldT.init) });
        } else {
            if (oldT.init != null && newT.init == null) {
                // remove initial value
                int pos = getOldPos(oldT.init);
                tokenSequence.move(pos);
                moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                tokenSequence.moveNext();
                int to = tokenSequence.offset();
                copyTo(localPointer, to);
                localPointer = endPos(oldT.init);
            }
            if (oldT.init == null && newT.init != null) {
                int end = endPos(oldT);
                tokenSequence.move(end);
                tokenSequence.moveNext();
                if (!JavaTokenId.COMMA.equals(tokenSequence.token().id()) &&
                    !JavaTokenId.SEMICOLON.equals(tokenSequence.token().id()))
                {
                    tokenSequence.movePrevious();
                }
                moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                tokenSequence.moveNext();
                copyTo(localPointer, localPointer = tokenSequence.offset());
                printer.printVarInit(newT);
            }
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffBlock(JCBlock oldT, JCBlock newT, int[] blockBounds) {
        int localPointer = blockBounds[0];
        if (oldT.flags != newT.flags) {
            int sp = getOldPos(oldT);
            copyTo(localPointer, localPointer = sp);
            if ((oldT.flags & STATIC) == 0 && (newT.flags & STATIC) != 0) {
                printer.print("static");
                if (diffContext.style.spaceBeforeStaticInitLeftBrace()) {
                    printer.print(" ");
                }
            } else if ((oldT.flags & STATIC) != 0 && (newT.flags & STATIC) == 0) {
                tokenSequence.move(sp);
                if (tokenSequence.moveNext() && tokenSequence.token().id() == JavaTokenId.STATIC) {
                    localPointer = tokenSequence.offset() + tokenSequence.token().length();
                    if (tokenSequence.moveNext() && tokenSequence.token().id() == JavaTokenId.WHITESPACE) {
                        localPointer = tokenSequence.offset() + tokenSequence.token().length();
                    }
                }
            }
        } else {
            copyTo(localPointer, localPointer = oldT.pos + 1);
        }
        PositionEstimator est = EstimatorFactory.statements(
                filterHidden(oldT.stats),
                filterHidden(newT.stats),
                diffContext
        );
        int old = printer.indent();
        Name oldEnclosing = printer.enclClassName;
        printer.enclClassName = null;
        List<JCTree> oldstats = filterHidden(oldT.stats);
        localPointer = diffList(oldstats, filterHidden(newT.stats), localPointer, est, Measure.MEMBER, printer);
        printer.enclClassName = oldEnclosing;
        if (localPointer < endPos(oldT)) {
/*
            JCTree tree = oldstats.get(oldstats.size() - 1);
            localPointer = adjustLocalPointer(localPointer, comments.getComments(oldT), CommentSet.RelativePosition.INNER);
            CommentSet cs = comments.getComments(tree);
            localPointer = adjustLocalPointer(localPointer, cs, CommentSet.RelativePosition.INLINE);            
            localPointer = adjustLocalPointer(localPointer, cs, CommentSet.RelativePosition.TRAILING);            
*/
            copyTo(localPointer, localPointer = endPos(oldT));
        }
        printer.undent(old);
        return localPointer;
    }

    private int adjustLocalPointer(int localPointer, CommentSet cs, CommentSet.RelativePosition position) {
        if (cs == null) return localPointer;
        List<Comment> cl = cs.getComments(position);
        if (!cl.isEmpty()) {
            for (Comment comment : cl) {
                localPointer = Math.max(comment.endPos(), localPointer);
            }
        }
        return localPointer;
    }

    private boolean isComment(JavaTokenId tid) {
        switch (tid) {
            case LINE_COMMENT:
            case BLOCK_COMMENT:
            case JAVADOC_COMMENT:
                return true;
            default:
                return false;
        }
    }

    private int dimension(JCTree t, int afterPos) {
        if (t.getKind() != Kind.ARRAY_TYPE) {
            return 0;
        }
        int add;
        if (afterPos >= 0) {
            final int[] bounds =  getBounds(t);
            add = afterPos < bounds[1] ? 1 : 0;
        } else {
            add = 1;
        }
        return add + dimension (((JCTree.JCArrayTypeTree)t).getType(), afterPos);
    }

    protected int diffDoLoop(JCDoWhileLoop oldT, JCDoWhileLoop newT, int[] bounds) {
        int localPointer = bounds[0];

        int[] bodyBounds = new int[] { localPointer, endPos(oldT.body) };
        localPointer = diffTree(oldT.body, newT.body, bodyBounds, oldT.getKind());
        int[] condBounds = getBounds(oldT.cond);
        if (oldT.body.getKind() != Kind.BLOCK && newT.body.getKind() == Kind.BLOCK) {
            moveBackToToken(tokenSequence, condBounds[0], JavaTokenId.WHILE);
            localPointer = tokenSequence.offset();
        } else {
            copyTo(localPointer, condBounds[0]);
            localPointer = diffTree(oldT.cond, newT.cond, condBounds);
        }
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffWhileLoop(JCWhileLoop oldT, JCWhileLoop newT, int[] bounds) {
        int localPointer = bounds[0];
        // condition
        int[] condPos = getBounds(oldT.cond);
        copyTo(localPointer, condPos[0]);
        localPointer = diffTree(oldT.cond, newT.cond, condPos);
        // body
        int[] bodyPos = new int[] { localPointer, endPos(oldT.body) };
        localPointer = diffTree(oldT.body, newT.body, bodyPos, oldT.getKind());

        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffForLoop(JCForLoop oldT, JCForLoop newT, int[] bounds) {
        int localPointer;

        // initializer
        if (oldT.init.nonEmpty()) {
            // there is something in the init section, using start offset
            localPointer = getOldPos(oldT.init.head);
        } else {
            moveFwdToToken(tokenSequence, bounds[0], JavaTokenId.SEMICOLON);
            localPointer = tokenSequence.offset();
        }
        copyTo(bounds[0], localPointer);
        if (!listsMatch(oldT.init, newT.init)) {
            boolean oldVariable = containsVariable(oldT.init);
            boolean newVariable = containsVariable(newT.init);

            if (oldVariable ^ newVariable) {
                int oldPrec = printer.setPrec(TreeInfo.noPrec);
                localPointer = diffParameterList(oldT.init, newT.init, null, localPointer, Measure.ARGUMENT);
                printer.setPrec(oldPrec);
            } else {
                if (oldVariable) {
                    List<JCVariableDecl> oldInit = NbCollections.checkedListByCopy(oldT.init, JCVariableDecl.class, false);
                    FieldGroupTree old = new FieldGroupTree(oldInit);
                    List<JCVariableDecl> newInit = NbCollections.checkedListByCopy(newT.init, JCVariableDecl.class, false);
                    FieldGroupTree nue = new FieldGroupTree(newInit);
                    int[] initBounds = getBounds(oldT.init.head);

                    JCTree last = oldT.init.get(oldT.init.size() - 1);

                    long endPos = diffContext.trees.getSourcePositions().getEndPosition(oldTopLevel, last);

                    initBounds[1] = (int) endPos;
                    localPointer = diffTree(old, nue, initBounds);
                } else {
                    localPointer = diffParameterList(oldT.init, newT.init, null, localPointer, Measure.ARGUMENT);
                }
            }
        }

        // condition
        if (oldT.cond != null) {
            copyTo(localPointer, localPointer = getOldPos(oldT.cond));
            localPointer = diffTree(oldT.cond, newT.cond, getBounds(oldT.cond));
        } else {
            moveFwdToToken(tokenSequence, localPointer, JavaTokenId.SEMICOLON);
            copyTo(localPointer, localPointer = tokenSequence.offset());
        }

        // steps
        if (oldT.step.nonEmpty())
            copyTo(localPointer, localPointer = getOldPos(oldT.step.head));
        else {
            moveFwdToToken(tokenSequence, localPointer, JavaTokenId.SEMICOLON);
            tokenSequence.moveNext();
            copyTo(localPointer, localPointer = tokenSequence.offset());
        }
        localPointer = diffParameterList(oldT.step, newT.step, null, localPointer, Measure.ARGUMENT);

        // body
        int[] bodyBounds = new int[] { localPointer, endPos(oldT.body) };
        localPointer = diffTree(oldT.body, newT.body, bodyBounds, oldT.getKind());

        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    private static boolean containsVariable(List<JCStatement> statements) {
        for (JCStatement s : statements) {
            if (s.getKind() == Kind.VARIABLE) {
                return true;
            }
        }

        return false;
    }

    protected int diffForeachLoop(JCEnhancedForLoop oldT, JCEnhancedForLoop newT, int[] bounds) {
        int localPointer = bounds[0];
        // variable
        int[] varBounds = getBounds(oldT.var);
        copyTo(localPointer, varBounds[0]);
        localPointer = diffTree(oldT.var, newT.var, varBounds);
        // expression
        int[] exprBounds = getBounds(oldT.expr);
        copyTo(localPointer, exprBounds[0]);
        localPointer = diffTree(oldT.expr, newT.expr, exprBounds);
        // body
        int[] bodyBounds = new int[] { localPointer, endPos(oldT.body) };
        localPointer = diffTree(oldT.body, newT.body, bodyBounds, oldT.getKind());
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffLabelled(JCLabeledStatement oldT, JCLabeledStatement newT, int[] bounds) {
        int localPointer = bounds[0];
        if (nameChanged(oldT.label, newT.label)) {
            copyTo(localPointer, localPointer = getOldPos(oldT));
            printer.print(newT.label);
            localPointer += oldT.label.length();
        }
        int[] bodyBounds = getBounds(oldT.body);
        copyTo(localPointer, bodyBounds[0]);
        localPointer = diffTree(oldT.body, newT.body, bodyBounds);
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffSwitch(JCSwitch oldT, JCSwitch newT, int[] bounds) {
        int localPointer = bounds[0];

        // rename in switch
        int[] selectorBounds = getBounds(oldT.selector);
        copyTo(localPointer, selectorBounds[0]);
        localPointer = diffTree(oldT.selector, newT.selector, selectorBounds);

        tokenSequence.move(selectorBounds[1]);
        do { } while (tokenSequence.moveNext() && JavaTokenId.LBRACE != tokenSequence.token().id());
        tokenSequence.moveNext();
        copyTo(localPointer, localPointer = tokenSequence.offset());
        PositionEstimator est = EstimatorFactory.cases(oldT.getCases(), newT.getCases(), diffContext);
        localPointer = diffList(oldT.cases, newT.cases, localPointer, est, Measure.MEMBER, printer);

        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffCase(JCCase oldT, JCCase newT, int[] bounds) {
        int localPointer = bounds[0];
        if (oldT.pat != null) {
            int[] patBounds = getBounds(oldT.pat);
            copyTo(localPointer, patBounds[0]);
            localPointer = diffTree(oldT.pat, newT.pat, patBounds);
            tokenSequence.move(patBounds[1]);
            do { } while (tokenSequence.moveNext() && JavaTokenId.COLON != tokenSequence.token().id());
            tokenSequence.moveNext();
            copyTo(localPointer, localPointer = tokenSequence.offset());
        }
        // todo (#pf): hot-fix of #113313, think about correct matching later
        if (oldT.pat == null && newT.pat != null) {
            printer.print(newT);
            printer.newline();
            return bounds[1];
        }
        PositionEstimator est = EstimatorFactory.statements(
                oldT.getStatements(),
                newT.getStatements(),
                diffContext
        );
        localPointer = diffList(oldT.stats, newT.stats, localPointer, est, Measure.MEMBER, printer);

        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffSynchronized(JCSynchronized oldT, JCSynchronized newT, int[] bounds) {
        int localPointer = bounds[0];
        // lock
        int[] lockBounds = getBounds(oldT.lock);
        copyTo(localPointer, lockBounds[0]);
        localPointer = diffTree(oldT.lock, newT.lock, lockBounds);
        // body
        int[] bodyBounds = getBounds(oldT.body);
        copyTo(localPointer, bodyBounds[0]);
        localPointer = diffTree(oldT.body, newT.body, bodyBounds);
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffTry(JCTry oldT, JCTry newT, int[] bounds) {
        int localPointer = bounds[0];
        int[] bodyPos = getBounds(oldT.body);

        if (!listsMatch(oldT.resources, newT.resources)) {
            if (oldT.resources.nonEmpty() && newT.resources.isEmpty()) {
                tokenSequence.move(getOldPos(oldT.resources.head));
                moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                assert tokenSequence.token().id() == JavaTokenId.LPAREN;
                copyTo(localPointer, tokenSequence.offset());
                localPointer = bodyPos[0];
            } else {
                int pos = oldT.resources.isEmpty() ? pos = bodyPos[0] : getOldPos(oldT.resources.head);
                copyTo(localPointer, pos);
                boolean parens = oldT.resources.isEmpty() || newT.resources.isEmpty();
                int oldPrec = printer.setPrec(TreeInfo.noPrec);
                if (newT.resources.nonEmpty()) {
                    //Remove all stms from oldTrees to force it to be reprinted by VeryPretty
                    com.sun.tools.javac.util.List<JCTree> l = newT.resources;
                    for (Tree t = l.head; t!= null; l = l.tail, t = l.head) {
                        printer.oldTrees.remove(t);
                    }
                }  
                localPointer = diffParameterList(oldT.resources,
                        newT.resources,
                        parens ? new JavaTokenId[] { JavaTokenId.LPAREN, JavaTokenId.RPAREN } : null,
                        pos,
                        Measure.ARGUMENT,
                        diffContext.style.spaceBeforeSemi(),
                        diffContext.style.spaceAfterSemi(),
                        false,
                        ";" //NOI18N
                );
                printer.setPrec(oldPrec);
                if (parens && oldT.resources.isEmpty()) {
                    printer.print(" "); // print the space after type parameter
                }
            }
        }
        
        copyTo(localPointer, bodyPos[0]);
        localPointer = diffTree(oldT.body, newT.body, bodyPos);
        copyTo(localPointer, localPointer = bodyPos[1]);
        PositionEstimator est = EstimatorFactory.catches(oldT.getCatches(), newT.getCatches(), oldT.finalizer != null, diffContext);
        localPointer = diffList(oldT.catchers, newT.catchers, localPointer, est, Measure.DEFAULT, printer);

        if (oldT.finalizer != null) {
            int[] finalBounds = getBounds(oldT.finalizer);
            if (newT.finalizer != null) {
                copyTo(localPointer, finalBounds[0]);
                localPointer = diffTree(oldT.finalizer, newT.finalizer, finalBounds);
            } else {
                int endetHier = oldT.catchers.isEmpty() ? Math.max(endPos(oldT.body), localPointer) : endPos(oldT.catchers);
                copyTo(localPointer, endetHier);
                localPointer = finalBounds[1];
            }
            copyTo(localPointer, bounds[1]);
        } else {
            if (newT.finalizer != null) {
                int catchEnd = oldT.catchers.isEmpty() ? bounds[1] : endPos(oldT.catchers.reverse().head);
                copyTo(localPointer, localPointer = catchEnd);
                printer.printFinallyBlock(newT.finalizer);
                copyTo(localPointer, bounds[1]);
            } else {
                copyTo(localPointer, bounds[1]);
            }
        }

        return bounds[1];
    }

    protected int diffCatch(JCCatch oldT, JCCatch newT, int[] bounds) {
        int localPointer = bounds[0];
        // param
        int[] paramBounds = getBounds(oldT.param);
        copyTo(localPointer, paramBounds[0]);
        localPointer = diffTree(oldT.param, newT.param, paramBounds);
        // body
        int[] bodyBounds = getBounds(oldT.body);
        copyTo(localPointer, bodyBounds[0]);
        localPointer = diffTree(oldT.body, newT.body, bodyBounds);
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffConditional(JCConditional oldT, JCConditional newT, int[] bounds) {
        int localPointer = bounds[0];
        // cond
        int[] condBounds = getBounds(oldT.cond);
        copyTo(localPointer, condBounds[0]);
        localPointer = diffTree(oldT.cond, newT.cond, condBounds);
        // true
        int[] trueBounds = getBounds(oldT.truepart);
        copyTo(localPointer, trueBounds[0]);
        localPointer = diffTree(oldT.truepart, newT.truepart, trueBounds);
        // false
        int[] falseBounds = getBounds(oldT.falsepart);
        copyTo(localPointer, falseBounds[0]);
        localPointer = diffTree(oldT.falsepart, newT.falsepart, falseBounds);
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffIf(JCIf oldT, JCIf newT, int[] bounds) {
        int localPointer = bounds[0];

        int start = printer.toString().length();
        int[] condBounds = getCommentCorrectedBounds(oldT.cond);
        copyTo(localPointer, condBounds[0]);
        localPointer = diffTree(oldT.cond, newT.cond, null, condBounds);
        copyTo(localPointer, localPointer = condBounds[1]);
        int[] partBounds = new int[] { localPointer, endPos(oldT.thenpart) };
        printer.conditionStartHack = start;
        localPointer = diffTree(oldT.thenpart, newT.thenpart, partBounds, oldT.getKind());
        printer.conditionStartHack = (-1);
        if (oldT.elsepart == null && newT.elsepart != null) {
            copyTo(localPointer, localPointer = partBounds[1]);
            printer.printElse(newT, newT.thenpart.getKind() == Kind.BLOCK);
        } else if (oldT.elsepart != null && newT.elsepart == null) {
            // remove else part
            copyTo(localPointer, partBounds[1]);
            copyTo(getBounds(oldT.elsepart)[1], bounds[1]);
            return bounds[1];
        } else {
            if (oldT.elsepart != null) {
                if (oldT.thenpart.getKind() != newT.thenpart.getKind() && newT.thenpart.getKind() == Kind.BLOCK) {
                    tokenSequence.move(localPointer);
                    moveToDifferentThan(tokenSequence, Direction.FORWARD, EnumSet.of(JavaTokenId.WHITESPACE));
                    if (localPointer != tokenSequence.offset()) {
                        if (diffContext.style.spaceBeforeElse()) {
                            printer.print(" ");
                        }
                    }
                    localPointer = tokenSequence.offset();
                }
                partBounds = new int[] { localPointer, endPos(oldT.elsepart) };
                localPointer = diffTree(oldT.elsepart, newT.elsepart, partBounds, oldT.getKind());
                tokenSequence.move(localPointer);
                if (tokenSequence.movePrevious() && tokenSequence.token().id() == JavaTokenId.LINE_COMMENT) {
                    printer.newline();
                }
            }
        }
        if (localPointer < bounds[1])
            copyTo(localPointer, localPointer = bounds[1]);
        return localPointer;
    }

    protected int diffExec(JCExpressionStatement oldT, JCExpressionStatement newT, int[] bounds) {
        int localPointer = bounds[0];
        // expr
        int[] exprBounds = getBounds(oldT.expr);
        copyTo(localPointer, exprBounds[0]);
        localPointer = diffTree(oldT.expr, newT.expr, exprBounds);

        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffBreak(JCBreak oldT, JCBreak newT, int[] bounds) {
        final Name oldTLabel = oldT.label;
        final Name newTlabel = newT.label;
        return printBreakContinueTree(bounds, oldTLabel, newTlabel, oldT);
    }

    protected int diffContinue(JCContinue oldT, JCContinue newT, int[] bounds) {
        final Name oldTLabel = oldT.label;
        final Name newTlabel = newT.label;
        return printBreakContinueTree(bounds, oldTLabel, newTlabel, oldT);
    }

    protected int diffReturn(JCReturn oldT, JCReturn newT, int[] bounds) {
        int localPointer = bounds[0];
        if (oldT.expr != newT.expr) {
            if (oldT.expr == null) {
                tokenSequence.move(endPos(oldT));
                tokenSequence.movePrevious();
                copyTo(localPointer, localPointer = tokenSequence.offset());
                if (tokenSequence.token().id() == JavaTokenId.SEMICOLON) {
                    tokenSequence.movePrevious();
                }
                if (tokenSequence.token().id() != JavaTokenId.WHITESPACE) {
                    printer.print(" ");
                }
                printer.print(newT.expr);
            } else if (newT.expr == null) {
                copyTo(localPointer, localPointer = getOldPos(oldT) + "return".length());
                localPointer = endPos(oldT.expr);
            } else {
                int[] exprBounds = getBounds(oldT.expr);
                copyTo(bounds[0], exprBounds[0]);
                localPointer = diffTree(oldT.expr, newT.expr, exprBounds);
            }
        }
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffThrow(JCThrow oldT, JCThrow newT, int[] bounds) {
        int localPointer = bounds[0];
        // expr
        int[] exprBounds = getBounds(oldT.expr);
        copyTo(localPointer, exprBounds[0]);
        localPointer = diffTree(oldT.expr, newT.expr, exprBounds);
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffAssert(JCAssert oldT, JCAssert newT, int[] bounds) {
        int localPointer = bounds[0];
        // cond
        int[] condBounds = getBounds(oldT.cond);
        copyTo(localPointer, condBounds[0]);
        localPointer = diffTree(oldT.cond, newT.cond, condBounds);
        // detail
        if (oldT.detail != newT.detail) {
            if (oldT.detail == null) {
                copyTo(localPointer, condBounds[1]);
                localPointer = condBounds[1];
                printer.print(" : ");
                printer.print(newT.detail);
            } else {
                int[] detailBounds = getBounds(oldT.detail);
                if (newT.detail == null) {
                    copyTo(localPointer, condBounds[1]);
                    localPointer = detailBounds[1];
                } else {
                    copyTo(localPointer, detailBounds[0]);
                    localPointer = diffTree(oldT.detail, newT.detail, detailBounds);
                }
            }
        }
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffApply(JCMethodInvocation oldT, JCMethodInvocation newT, int[] bounds) {
        int localPointer = bounds[0];
        int[] methBounds = getBounds(oldT.meth);
        if (Kind.MEMBER_SELECT == oldT.meth.getKind() && oldT.meth.getKind() == newT.meth.getKind()) {
            localPointer = diffSelect((JCFieldAccess) oldT.meth, (JCFieldAccess) newT.meth, methBounds, oldT.typeargs, newT.typeargs);
        } else if (oldT.typeargs.isEmpty() && newT.typeargs.isEmpty()) {
            localPointer = diffTree(oldT.meth, newT.meth, methBounds);
        } else {
            copyTo(localPointer, methBounds[0]);
            printer.printMethodSelect(newT);
            localPointer = methBounds[1];
        }
        if (!listsMatch(oldT.args, newT.args)) {
            if (oldT.args.nonEmpty()) {
                copyTo(localPointer, localPointer = getCommentCorrectedOldPos(oldT.args.head));
            } else {
                copyTo(localPointer, localPointer = methBounds[1]);
                tokenSequence.move(localPointer);
                moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                tokenSequence.moveNext();
                copyTo(localPointer, localPointer = tokenSequence.offset());
            }
            localPointer = diffParameterList(oldT.args, newT.args, null, localPointer, Measure.ARGUMENT);
        }
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    boolean anonClass = false;

    protected int diffNewClass(JCNewClass oldT, JCNewClass newT, int[] bounds) {
        int localPointer = bounds[0];
        if (oldT.encl != null) {
            int[] enclBounds = getBounds(oldT.encl);

            if (newT.encl == null) {
                moveFwdToToken(tokenSequence, enclBounds[1], JavaTokenId.DOT);
                tokenSequence.moveNext();
                localPointer = tokenSequence.offset();
            } else {
                localPointer = diffTree(oldT.encl, newT.encl, enclBounds);
            }
        }
        diffParameterList(oldT.typeargs, newT.typeargs, null, localPointer, Measure.ARGUMENT);
        if (!enumConstantPrint) {
            int[] clazzBounds = getBounds(oldT.clazz);
            copyTo(localPointer, clazzBounds[0]);
            localPointer = diffTree(oldT.clazz, newT.clazz, clazzBounds);
        }
        if (oldT.args.nonEmpty()) {
            copyTo(localPointer, localPointer = getOldPos(oldT.args.head));
        } else if (!enumConstantPrint) {
            moveFwdToToken(tokenSequence, oldT.pos, JavaTokenId.LPAREN);
            tokenSequence.moveNext();
            copyTo(localPointer, localPointer = tokenSequence.offset());
        }
        localPointer = diffParameterList(oldT.args, newT.args, null, localPointer, Measure.ARGUMENT);
        // let diffClassDef() method notified that anonymous class is printed.
        if (oldT.def != newT.def) {
            if (oldT.def != null && newT.def != null) {
                copyTo(localPointer, getOldPos(oldT.def));
                anonClass = true;
                localPointer = diffTree(oldT.def, newT.def, getBounds(oldT.def));
                anonClass = false;
            } else if (newT.def == null) {
                if (endPos(oldT.args) > localPointer) {
                    copyTo(localPointer, endPos(oldT.args));
                }
                printer.print(")");
                localPointer = endPos(oldT.def);
            } else {
                copyTo(localPointer, localPointer = endPos(oldT));
                printer.printNewClassBody(newT);
            }
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffNewArray(JCNewArray oldT, JCNewArray newT, int[] bounds) {
        int localPointer = bounds[0];
        // elemtype
        if (newT.elemtype != null) {
            if (oldT.elemtype != null) {
                int[] elemtypeBounds = getBounds(oldT.elemtype);
                copyTo(localPointer, elemtypeBounds[0]);
                localPointer = diffTree(oldT.elemtype, newT.elemtype, elemtypeBounds);
            }
            if (!listsMatch(oldT.dims, newT.dims) && !newT.dims.isEmpty()) {
                // solved just for the change, not insert and delete
                for (com.sun.tools.javac.util.List<JCExpression> l1 = oldT.dims, l2 = newT.dims;
                    l1.nonEmpty(); l1 = l1.tail, l2 = l2.tail) {
                    int[] span = getBounds(l1.head);
                    copyTo(localPointer, span[0]);
                    localPointer = diffTree(l1.head, l2.head, span);
                }
            }
        } else if (oldT.elemtype != null) {
            //remove new <type><dimensions>
            copyTo(localPointer, getOldPos(oldT));
            if (oldT.elems != null) {
                localPointer = oldT.dims != null && !oldT.dims.isEmpty() ? endPos(oldT.dims) : endPos(oldT.elemtype);
                moveFwdToToken(tokenSequence, localPointer, JavaTokenId.LBRACE);
                localPointer = tokenSequence.offset();
            } else {
                localPointer = endPos(oldT);
            }
        }
        if (oldT.elems != null) {
            if (oldT.elems.head != null) {
                copyTo(localPointer, getOldPos(oldT.elems.head));
                localPointer = diffParameterList(oldT.elems, newT.elems, null, getOldPos(oldT.elems.head), Measure.ARGUMENT);
            } else if (newT.elems != null && !newT.elems.isEmpty()) {
                //empty initializer array, adding the first element to it
                //find {:
                moveFwdToToken(tokenSequence, localPointer, JavaTokenId.LBRACE);
                tokenSequence.moveNext();
                copyTo(localPointer, localPointer = tokenSequence.offset());
                localPointer = diffParameterList(oldT.elems, newT.elems, null, localPointer, Measure.ARGUMENT);
            }
        } else if (newT.elems != null && !newT.elems.isEmpty()) {
            //empty initializer array, adding the first element to it
            //find {:
            if (newT.elemtype != null) printer.print("[]");
            printer.print("{");
            localPointer = diffParameterList(Collections.<JCTree>emptyList(), newT.elems, null, localPointer, Measure.ARGUMENT);
            printer.print("}");
            moveFwdToToken(tokenSequence, localPointer, JavaTokenId.SEMICOLON);
            tokenSequence.moveNext();
            localPointer = bounds[1];
//            copyTo(localPointer, localPointer = tokenSequence.offset());
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffParens(JCParens oldT, JCParens newT, int[] bounds) {
        int localPointer = bounds[0];
        copyTo(localPointer, getOldPos(oldT.expr));
        localPointer = diffTree(oldT.expr, newT.expr, getBounds(oldT.expr));
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffAssign(JCAssign oldT, JCAssign newT, JCTree parent, int[] bounds) {
        int localPointer = bounds[0];
        // lhs
        int[] lhsBounds = getBounds(oldT.lhs);
        copyTo(localPointer, lhsBounds[0]);
        localPointer = diffTree(oldT.lhs, newT.lhs, lhsBounds);
        int[] rhsBounds = getBounds(oldT.rhs);

        //#174552: '=' may be missing if this is a synthetic annotation attribute assignment (of attribute name "value"):
        if (   oldT.lhs.getKind() == Kind.IDENTIFIER
            && newT.lhs.getKind() == Kind.IDENTIFIER
            && !((JCIdent) oldT.lhs).name.equals(((JCIdent) newT.lhs).name)) {
            tokenSequence.move(rhsBounds[0]);
            moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
            if (tokenSequence.token().id() != JavaTokenId.EQ) {
                boolean spaceAroundAssignOps = (parent.getKind() == Kind.ANNOTATION || parent.getKind() == Kind.TYPE_ANNOTATION) ? diffContext.style.spaceAroundAnnotationValueAssignOps() : diffContext.style.spaceAroundAssignOps();
                if (spaceAroundAssignOps)
                    printer.print(" = ");
                else
                    printer.print("=");
            }
        }
        //#174552 end
        
        // rhs
        copyTo(localPointer, rhsBounds[0]);
        localPointer = diffTree(oldT.rhs, newT.rhs, rhsBounds);

        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffAssignop(JCAssignOp oldT, JCAssignOp newT, int[] bounds) {
        int localPointer = bounds[0];
        // lhs
        int[] lhsBounds = getBounds(oldT.lhs);
        copyTo(localPointer, lhsBounds[0]);
        localPointer = diffTree(oldT.lhs, newT.lhs, lhsBounds);
        if (oldT.getTag() != newT.getTag()) { // todo (#pf): operatorName() does not work
            copyTo(localPointer, oldT.pos);
            printer.print(getAssignementOperator(newT));
            localPointer = oldT.pos + getAssignementOperator(oldT).length();
        }
        // rhs
        int[] rhsBounds = getBounds(oldT.rhs);
        copyTo(localPointer, rhsBounds[0]);
        localPointer = diffTree(oldT.rhs, newT.rhs, rhsBounds);

        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    String getAssignementOperator(Tree t) {
        String name;
        switch (t.getKind()) {
            case MULTIPLY_ASSIGNMENT:    return "*=";
            case DIVIDE_ASSIGNMENT:      return "/=";
            case REMAINDER_ASSIGNMENT:   return "%=";
            case PLUS_ASSIGNMENT:        return "+=";
            case MINUS_ASSIGNMENT:       return "-=";
            case LEFT_SHIFT_ASSIGNMENT:  return "<<=";
            case RIGHT_SHIFT_ASSIGNMENT: return ">>=";
            case AND_ASSIGNMENT:         return "&=";
            case XOR_ASSIGNMENT:         return "^=";
            case OR_ASSIGNMENT:          return "|=";
            case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT: return ">>>=";
            default:
                throw new IllegalArgumentException("Illegal kind " + t.getKind());
        }
    }

    protected int diffUnary(JCUnary oldT, JCUnary newT, int[] bounds) {
        int[] argBounds = getBounds(oldT.arg);
        boolean newOpOnLeft = newT.getKind() != Kind.POSTFIX_DECREMENT && newT.getKind() != Kind.POSTFIX_INCREMENT;
        if (newOpOnLeft) {
            if (oldT.getTag() != newT.getTag()) {
                printer.print(operatorName(newT.getTag()));
            } else {
                copyTo(bounds[0], argBounds[0]);
            }
        }
        int localPointer = diffTree(oldT.arg, newT.arg, argBounds);
        copyTo(localPointer, argBounds[1]);
        if (!newOpOnLeft) {
            if (oldT.getTag() != newT.getTag()) {
                printer.print(operatorName(newT.getTag()));
            } else {
                copyTo(argBounds[1], bounds[1]);
            }
        }
        return bounds[1];
    }

    protected int diffBinary(JCBinary oldT, JCBinary newT, int[] bounds) {
        int localPointer = bounds[0];

        int[] lhsBounds = getBounds(oldT.lhs);
        copyTo(localPointer, lhsBounds[0]);
        localPointer = diffTree(oldT.lhs, newT.lhs, lhsBounds);
        if (oldT.getTag() != newT.getTag()) {
            copyTo(localPointer, oldT.pos);
            printer.print(operatorName(newT.getTag()));
            localPointer = oldT.pos + operatorName(oldT.getTag()).toString().length();
        }
        int[] rhsBounds = getBounds(oldT.rhs);
        copyTo(localPointer, rhsBounds[0]);
        localPointer = diffTree(oldT.rhs, newT.rhs, rhsBounds);
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    private String operatorName(Tag tag) {
        // dummy instance, just to access a public method which should be static
        return new Pretty(null, false).operatorName(tag);
    }

    protected int diffTypeCast(JCTypeCast oldT, JCTypeCast newT, int[] bounds) {
        int localPointer = bounds[0];
        // indexed
        int[] clazzBounds = getBounds(oldT.clazz);
        copyTo(localPointer, clazzBounds[0]);
        localPointer = diffTree(oldT.clazz, newT.clazz, clazzBounds);
        // expression
        int[] exprBounds = getBounds(oldT.expr);
        copyTo(localPointer, exprBounds[0]);
        localPointer = diffTree(oldT.expr, newT.expr, exprBounds);
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffTypeTest(JCInstanceOf oldT, JCInstanceOf newT, int[] bounds) {
        int localPointer = bounds[0];
        // expr
        int[] exprBounds = getBounds(oldT.expr);
        copyTo(localPointer, exprBounds[0]);
        localPointer = diffTree(oldT.expr, newT.expr, exprBounds);
        // clazz
        int[] clazzBounds = getBounds(oldT.clazz);
        copyTo(localPointer, clazzBounds[0]);
        localPointer = diffTree(oldT.clazz, newT.clazz, clazzBounds);
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffIndexed(JCArrayAccess oldT, JCArrayAccess newT, int[] bounds) {
        int localPointer = bounds[0];
        // indexed
        int[] indexedBounds = getBounds(oldT.indexed);
        copyTo(localPointer, indexedBounds[0]);
        localPointer = diffTree(oldT.indexed, newT.indexed, indexedBounds);
        // index
        int[] indexBounds = getBounds(oldT.index);
        copyTo(localPointer, indexBounds[0]);
        localPointer = diffTree(oldT.index, newT.index, indexBounds);
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffSelect(JCFieldAccess oldT, JCFieldAccess newT,
            int[] bounds,
            com.sun.tools.javac.util.List<JCExpression> oldTypePar,
            com.sun.tools.javac.util.List<JCExpression> newTypePar)
    {
        int localPointer = bounds[0];
        int[] selectedBounds = getBounds(oldT.selected);
        copyTo(localPointer, selectedBounds[0]);
        localPointer = diffTree(oldT.selected, newT.selected, selectedBounds);
        if (oldTypePar != null && newTypePar != null) {
            int insertHint;
            if (oldTypePar.nonEmpty() && newTypePar.nonEmpty()) {
                insertHint = oldTypePar.head.pos;
            } else {
                tokenSequence.move(selectedBounds[1]);
                moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                tokenSequence.moveNext();
                insertHint = tokenSequence.offset();
            }
            copyTo(localPointer, localPointer = insertHint);
            boolean parens = oldTypePar.isEmpty() && newTypePar.nonEmpty();
            localPointer = diffParameterList(oldTypePar, newTypePar,
                    parens ? new JavaTokenId[] { JavaTokenId.LT, JavaTokenId.GT } : null,
                    localPointer, Measure.ARGUMENT);
            if (oldTypePar.nonEmpty()) {
                tokenSequence.move(endPos(oldTypePar.last()));
                moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                moveToSrcRelevant(tokenSequence, Direction.FORWARD);//skips > and any subsequent unimportant tokens
                int end = tokenSequence.offset();
                if (newTypePar.nonEmpty())
                    copyTo(localPointer, end);
                localPointer = end;
            }
        } else {
            tokenSequence.move(selectedBounds[1]);
            if (oldT.name != Names.instance(context).error) {
                moveToSrcRelevant(tokenSequence, Direction.FORWARD); // go to dot (.)
                moveToSrcRelevant(tokenSequence, Direction.FORWARD); // go to oldT.name token
                copyTo(localPointer, localPointer = tokenSequence.offset());
            }
        }
        if (nameChanged(oldT.name, newT.name)) {
            printer.print(newT.name);
            diffInfo.put(localPointer, NbBundle.getMessage(CasualDiff.class,"TXT_UpdateReferenceTo",oldT.name));
            localPointer = localPointer + oldT.name.length();
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }
    
    protected int diffMemberReference(JCMemberReference oldT, JCMemberReference newT, int[] bounds) {
        int localPointer = bounds[0];
        int[] exprBounds = getBounds(oldT.expr);
        copyTo(localPointer, exprBounds[0]);
        localPointer = diffTree(oldT.expr, newT.expr, exprBounds);
        tokenSequence.move(exprBounds[1]);
        moveToSrcRelevant(tokenSequence, Direction.FORWARD);
        if (tokenSequence.token() != null && tokenSequence.token().id() == JavaTokenId.COLONCOLON) {
            moveToSrcRelevant(tokenSequence, Direction.FORWARD);
            copyTo(localPointer, localPointer = tokenSequence.offset());
        }
        com.sun.tools.javac.util.List<JCExpression> oldTypePar = oldT.typeargs != null ? oldT.typeargs : com.sun.tools.javac.util.List.<JCExpression>nil();
        com.sun.tools.javac.util.List<JCExpression> newTypePar = newT.typeargs != null ? newT.typeargs : com.sun.tools.javac.util.List.<JCExpression>nil();
        if (!listsMatch(oldTypePar, newTypePar)) {
            int insertHint;
            if (oldTypePar.nonEmpty() && newTypePar.nonEmpty()) {
                insertHint = oldTypePar.head.pos;
            } else {
                insertHint = localPointer;
            }
            copyTo(localPointer, localPointer = insertHint);
            boolean parens = oldTypePar.isEmpty() && newTypePar.nonEmpty();
            localPointer = diffParameterList(oldTypePar, newTypePar,
                    parens ? new JavaTokenId[] { JavaTokenId.LT, JavaTokenId.GT } : null,
                    localPointer, Measure.ARGUMENT);
            if (oldTypePar.nonEmpty()) {
                tokenSequence.move(endPos(oldTypePar.last()));
                moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                moveToSrcRelevant(tokenSequence, Direction.FORWARD);//skips > and any subsequent unimportant tokens
                int end = tokenSequence.offset();
                if (newTypePar.nonEmpty())
                    copyTo(localPointer, end);
                localPointer = end;
            }
        }
        if (nameChanged(oldT.name, newT.name)) {
            printer.print(newT.name);
            diffInfo.put(localPointer, NbBundle.getMessage(CasualDiff.class,"TXT_UpdateReferenceTo",oldT.name));
            localPointer = localPointer + oldT.name.length();
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffSelect(JCFieldAccess oldT, JCFieldAccess newT, int[] bounds) {
        return diffSelect(oldT, newT, bounds, null, null);
    }

    protected int diffIdent(JCIdent oldT, JCIdent newT, int[] bounds) {
        if (nameChanged(oldT.name, newT.name)) {
            copyTo(bounds[0], oldT.pos);
            printer.print(newT.name);
            diffInfo.put(oldT.pos, NbBundle.getMessage(CasualDiff.class,"TXT_UpdateReferenceTo",oldT.name));
        } else {
            copyTo(bounds[0], bounds[1]);
        }
        return bounds[1];
    }

    protected int diffLiteral(JCLiteral oldT, JCLiteral newT, int[] bounds) {
        if (oldT.typetag != newT.typetag ||
           (oldT.value != null && !oldT.value.equals(newT.value)))
        {
            int localPointer = bounds[0];
            // literal
            int[] literalBounds = getBounds(oldT);
            copyTo(localPointer, literalBounds[0]);
            printer.print(newT);
            copyTo(literalBounds[1], bounds[1]);
        } else {
            copyTo(bounds[0], bounds[1]);
        }
        return bounds[1];
    }

    protected int diffTypeIdent(JCPrimitiveTypeTree oldT, JCPrimitiveTypeTree newT, int[] bounds) {
        if (oldT.typetag != newT.typetag) {
            printer.print(newT);
        } else {
            copyTo(bounds[0], bounds[1]);
        }
        return bounds[1];
    }

    protected int diffTypeArray(JCArrayTypeTree oldT, JCArrayTypeTree newT, int[] bounds) {
        int localPointer = bounds[0];
        int[] elemtypeBounds = getBounds(oldT.elemtype);
        localPointer = diffTree(oldT.elemtype, newT.elemtype, elemtypeBounds);
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffTypeApply(JCTypeApply oldT, JCTypeApply newT, int[] bounds) {
        int localPointer = bounds[0];
        int[] clazzBounds = getBounds(oldT.clazz);
        copyTo(localPointer, clazzBounds[0]);
        localPointer = diffTree(oldT.clazz, newT.clazz, clazzBounds);
        if (!listsMatch(oldT.arguments, newT.arguments)) {
            int pos = oldT.arguments.nonEmpty() ? getOldPos(oldT.arguments.head) : endPos(oldT.clazz);
            copyTo(localPointer, pos);
            boolean printBrace = false;
            localPointer = diffParameterList(
                    oldT.arguments,
                    newT.arguments,
                    printBrace ? new JavaTokenId[] { JavaTokenId.LT, JavaTokenId.GT } : null,
                    pos,
                    Measure.ARGUMENT
            );
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }
    
    protected int diffAnnotatedType(JCAnnotatedType oldT, JCAnnotatedType newT, int[] bounds) {
        int localPointer = bounds[0];
        if (!listsMatch(oldT.annotations, newT.annotations)) {
            int pos = oldT.annotations.nonEmpty() ? getOldPos(oldT.annotations.head) : bounds[0];
            copyTo(localPointer, pos);
            localPointer = diffParameterList(
                    oldT.annotations,
                    newT.annotations,
                    null,
                    null,
                    pos,
                    Measure.ARGUMENT,
                    true, //TODO: should read the code style configuration
                    false,
                    false,
                    ""
            );
        }
        int[] underlyingBounds = getBounds(oldT.underlyingType);
        copyTo(localPointer, underlyingBounds[0]);
        localPointer = diffTree(oldT.underlyingType, newT.underlyingType, underlyingBounds);
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffTypeParameter(JCTypeParameter oldT, JCTypeParameter newT, int[] bounds) {
        int localPointer = bounds[0];
        copyTo(localPointer, getOldPos(oldT));
        if (nameChanged(oldT.name, newT.name)) {
            printer.print(newT.name);
            localPointer += oldT.name.length();
        }
        if (!listsMatch(oldT.bounds, newT.bounds)) {
            // todo (#pf): match it for rename only, other matching will be
            // finished later.
            PositionEstimator est = EstimatorFactory.implementz(oldT.getBounds(), newT.getBounds(), diffContext);
            int pos = oldT.bounds.nonEmpty() ? getOldPos(oldT.bounds.head) : -1;
            if (pos > -1) {
                copyTo(localPointer, pos);
                localPointer = diffList2(oldT.bounds, newT.bounds, pos, est);
            }
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffWildcard(JCWildcard oldT, JCWildcard newT, int[] bounds) {
        int localPointer = bounds[0];
        if (oldT.kind != newT.kind) {
            copyTo(localPointer, oldT.pos);
            printer.print(newT.kind.toString());
            localPointer = oldT.pos + oldT.kind.toString().length();
        }
        JCTree oldBound = oldT.kind.kind != BoundKind.UNBOUND ? oldT.inner : null;
        JCTree newBound = newT.kind.kind != BoundKind.UNBOUND ? newT.inner : null;
        if (oldBound == newBound && oldBound == null) return localPointer;
        int[] innerBounds = getBounds(oldBound);
        copyTo(localPointer, innerBounds[0]);
        localPointer = diffTree(oldBound, newBound, innerBounds);
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffTypeBoundKind(TypeBoundKind oldT, TypeBoundKind newT, int[] bounds) {
        int localPointer = bounds[0];
        if (oldT.kind != newT.kind) {
            copyTo(localPointer, oldT.pos);
            printer.print(newT.kind.toString());
            localPointer = oldT.pos + oldT.kind.toString().length();
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffAnnotation(JCAnnotation oldT, JCAnnotation newT, int[] bounds) {
        int localPointer = bounds[0];
        int[] annotationBounds = getBounds(oldT.annotationType);
        copyTo(localPointer, annotationBounds[0]);
        localPointer = diffTree(oldT.annotationType, newT.annotationType, annotationBounds);
        JavaTokenId[] parens = null;
        if (oldT.args.nonEmpty()) {
            copyTo(localPointer, localPointer = getOldPos(oldT.args.head));
        } else {
            // check, if there are already written parenthesis
            int endPos = endPos(oldT);
            tokenSequence.move(endPos);
            tokenSequence.movePrevious();
            if (JavaTokenId.RPAREN != tokenSequence.token().id()) {
                parens = new JavaTokenId[] { JavaTokenId.LPAREN, JavaTokenId.RPAREN };
            } else {
                endPos -= 1;
            }
            copyTo(localPointer, localPointer = endPos);
        }
        localPointer = diffParameterList(oldT.args, newT.args, oldT, parens, localPointer, Measure.ARGUMENT);
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffModifiers(JCModifiers oldT, JCModifiers newT, JCTree parent, int localPointer) {
        if (oldT == newT) {
            // modifiers wasn't changed, return the position lastPrinted.
            return localPointer;
        }

        int startPos = oldT.pos != Position.NOPOS ? getOldPos(oldT) : getOldPos(parent);
        int firstAnnotationPos = !oldT.getAnnotations().isEmpty() ? getOldPos(oldT.getAnnotations().head) : -1;
        int endOffset = endPos(oldT);

        //TODO: cannot currently match intermixed annotations and flags/keywords (#196053)
        //but at least handle case where annotations are after the keywords:
        if (startPos < firstAnnotationPos) {
            //first modifiers, then annotations:
            if (oldT.flags != newT.flags) {
                copyTo(localPointer, startPos);
                printer.printFlags(newT.flags & ~Flags.INTERFACE, oldT.getFlags().isEmpty() ? true : false);
                tokenSequence.move(firstAnnotationPos);
                moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                tokenSequence.moveNext();
                localPointer = tokenSequence.offset();
            }
        }
        
        localPointer = diffAnnotationsLists(oldT.getAnnotations(), newT.getAnnotations(), startPos, localPointer);

        if ((oldT.flags & Flags.ANNOTATION) != 0) {
            tokenSequence.move(endOffset);
            tokenSequence.movePrevious();
            moveToSrcRelevant(tokenSequence, Direction.BACKWARD);

            tokenSequence.moveNext();

            endOffset = tokenSequence.offset();
        }
        if (oldT.flags != newT.flags && !(startPos < firstAnnotationPos)) {
            if (localPointer == startPos) {
                // no annotation printed, do modifiers print immediately
                if ((newT.flags & ~Flags.INTERFACE) != 0) {
                    printer.printFlags(newT.flags & ~Flags.INTERFACE, oldT.getFlags().isEmpty() ? true : false);
                    localPointer = endOffset > 0 ? endOffset : localPointer;
                } else {
                    if (endOffset > 0) {
                        tokenSequence.move(endOffset);
                        while (tokenSequence.moveNext() && JavaTokenId.WHITESPACE == tokenSequence.token().id()) ;
                        localPointer = tokenSequence.offset();
                    }
                }
            } else {
                tokenSequence.move(localPointer);
                moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                copyTo(localPointer, localPointer = tokenSequence.offset());
                localPointer = tokenSequence.offset();
                if (!oldT.getFlags().isEmpty()) localPointer = endOffset;
                printer.printFlags(newT.flags, oldT.getFlags().isEmpty());
            }
        } else {
            if (endOffset > localPointer) {
                if(localPointer == startPos) {
                    printer.toLeftMargin();
                }
                copyTo(localPointer, localPointer = endOffset);
            }
        }
        return localPointer;
    }

    private int diffAnnotationsLists(com.sun.tools.javac.util.List<JCAnnotation> oldAnnotations, com.sun.tools.javac.util.List<JCAnnotation> newAnnotations, int startPos, int localPointer) {
        int annotationsEnd = oldAnnotations.nonEmpty() ? endPos(oldAnnotations) : localPointer;
        
        if (listsMatch(oldAnnotations, newAnnotations)) {
            copyTo(localPointer, localPointer = (annotationsEnd != localPointer ? annotationsEnd : startPos));
        } else {
            tokenSequence.move(startPos);
            if (tokenSequence.movePrevious() && JavaTokenId.WHITESPACE == tokenSequence.token().id()) {
                String text = tokenSequence.token().text().toString();
                int index = text.lastIndexOf('\n');
                startPos = tokenSequence.offset();
                if (index > -1) {
                    startPos += index + 1;
                }
                if (startPos < localPointer) startPos = localPointer;
            }
            copyTo(localPointer, startPos);
            PositionEstimator est = EstimatorFactory.annotations(oldAnnotations,newAnnotations, diffContext, parameterPrint);
            localPointer = diffList(oldAnnotations, newAnnotations, startPos, est, Measure.ARGUMENT, printer);
        }

        return localPointer;
    }

    protected void diffLetExpr(LetExpr oldT, LetExpr newT) {
        // TODO: perhaps better to throw exception here. Should be never
        // called.
    }

    protected void diffErroneous(JCErroneous oldT, JCErroneous newT, int[] bounds) {
        JCTree oldTident = oldT.getErrorTrees().get(0);
        JCTree newTident = newT.getErrorTrees().get(0);
        if (oldTident.getKind() == Kind.IDENTIFIER && newTident.getKind() == Kind.IDENTIFIER) {
            diffIdent((JCIdent) oldTident, (JCIdent) newTident, bounds);
        }
    }
    
    protected int diffLambda(JCLambda oldT, JCLambda newT, int[] bounds) {
        int localPointer = bounds[0];
        int posHint;
        if (oldT.params.isEmpty()) {
            // compute the position. Find the parameters closing ')', its
            // start position is important for us. This is used when 
            // there was not any parameter in original tree.
            int startOffset = oldT.pos;

            moveFwdToToken(tokenSequence, startOffset, JavaTokenId.RPAREN);
            posHint = tokenSequence.offset();
        } else {
            // take the position of the first old parameter
            posHint = oldT.params.iterator().next().getStartPosition();
        }
        if (!listsMatch(oldT.params, newT.params)) {
            copyTo(localPointer, posHint);
            int old = printer.setPrec(TreeInfo.noPrec);
            parameterPrint = true;
            Name oldEnclClassName = printer.enclClassName;
            printer.enclClassName = null;
            localPointer = diffParameterList(oldT.params, newT.params, null, posHint, Measure.MEMBER);
            printer.enclClassName = oldEnclClassName;
            parameterPrint = false;
            printer.setPrec(old);
        }
        //make sure the ')' is printed:
        moveFwdToToken(tokenSequence, oldT.params.isEmpty() ? posHint : endPos(oldT.params.last()), JavaTokenId.RPAREN);
        tokenSequence.moveNext();
        posHint = tokenSequence.offset();
        if (localPointer < posHint)
            copyTo(localPointer, localPointer = posHint);
        if (oldT.body != null && newT.body != null) {
            int[] bodyBounds = getBounds(oldT.body);
            copyTo(localPointer, bodyBounds[0]);
            localPointer = diffTree(oldT.body, newT.body, bodyBounds);
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffFieldGroup(FieldGroupTree oldT, FieldGroupTree newT, int[] bounds) {
        if (!listsMatch(oldT.getVariables(), newT.getVariables())) {
            copyTo(bounds[0], oldT.getStartPosition());
            if (oldT.isEnum()) {
                int pos = diffParameterList(oldT.getVariables(), newT.getVariables(), null, oldT.getStartPosition(), Measure.ARGUMENT, diffContext.style.spaceBeforeComma(), diffContext.style.spaceAfterComma(), true, ",");  //NOI18N
                copyTo(pos, bounds[1]);
                return bounds[1];
            } else {
                int pos = diffVarGroup(oldT.getVariables(), newT.getVariables(), null, oldT.getStartPosition(), Measure.GROUP_VAR_MEASURE);
                copyTo(pos, bounds[1]);
                return bounds[1];
            }
        } else {
            tokenSequence.move(oldT.endPos());
            moveToSrcRelevant(tokenSequence, Direction.FORWARD);
            tokenSequence.moveNext();
            return tokenSequence.offset();
        }
    }

    protected boolean listContains(List<? extends JCTree> list, JCTree tree) {
        for (JCTree t : list)
            if (treesMatch(t, tree))
                return true;
        return false;
    }

    protected boolean treesMatch(JCTree t1, JCTree t2) {
        return treesMatch(t1, t2, true);
    }

    public boolean treesMatch(JCTree t1, JCTree t2, boolean deepMatch) {
        if (t1 == t2)
            return true;
        if (t1 == null || t2 == null)
            return false;
        if (t1.getTag() != t2.getTag())
            return false;
        if (!deepMatch)
            return true;

        // don't use visitor, since we want fast-fail behavior
        switch (t1.getTag()) {
          case TOPLEVEL:
              return ((JCCompilationUnit)t1).sourcefile.equals(((JCCompilationUnit)t2).sourcefile);
          case IMPORT:
              return matchImport((JCImport)t1, (JCImport)t2);
          case CLASSDEF:
              return ((JCClassDecl)t1).sym == ((JCClassDecl)t2).sym;
          case METHODDEF:
              return ((JCMethodDecl)t1).sym == ((JCMethodDecl)t2).sym;
          case VARDEF:
              return ((JCVariableDecl)t1).sym == ((JCVariableDecl)t2).sym;
          case SKIP:
              return true;
          case BLOCK:
              return matchBlock((JCBlock)t1, (JCBlock)t2);
          case DOLOOP:
              return matchDoLoop((JCDoWhileLoop)t1, (JCDoWhileLoop)t2);
          case WHILELOOP:
              return matchWhileLoop((JCWhileLoop)t1, (JCWhileLoop)t2);
          case FORLOOP:
              return matchForLoop((JCForLoop)t1, (JCForLoop)t2);
          case FOREACHLOOP:
              return matchForeachLoop((JCEnhancedForLoop)t1, (JCEnhancedForLoop)t2);
          case LABELLED:
              return matchLabelled((JCLabeledStatement)t1, (JCLabeledStatement)t2);
          case SWITCH:
              return matchSwitch((JCSwitch)t1, (JCSwitch)t2);
          case CASE:
              return matchCase((JCCase)t1, (JCCase)t2);
          case SYNCHRONIZED:
              return matchSynchronized((JCSynchronized)t1, (JCSynchronized)t2);
          case TRY:
              return matchTry((JCTry)t1, (JCTry)t2);
          case CATCH:
              return matchCatch((JCCatch)t1, (JCCatch)t2);
          case CONDEXPR:
              return matchConditional((JCConditional)t1, (JCConditional)t2);
          case IF:
              return matchIf((JCIf)t1, (JCIf)t2);
          case EXEC:
              return treesMatch(((JCExpressionStatement)t1).expr, ((JCExpressionStatement)t2).expr);
          case BREAK:
              return matchBreak((JCBreak)t1, (JCBreak)t2);
          case CONTINUE:
              return matchContinue((JCContinue)t1, (JCContinue)t2);
          case RETURN:
              return treesMatch(((JCReturn)t1).expr, ((JCReturn)t2).expr);
          case THROW:
              return treesMatch(((JCThrow)t1).expr, ((JCThrow)t2).expr);
          case ASSERT:
              return matchAssert((JCAssert)t1, (JCAssert)t2);
          case APPLY:
              return matchApply((JCMethodInvocation)t1, (JCMethodInvocation)t2);
          case NEWCLASS:
              // #97501: workaround. Not sure about comparing symbols and their
              // copying in ImmutableTreeTranslator, making workaround with
              // minimal impact - issue has to be fixed correctly in the future.
              if (((JCNewClass)t2).def != null) ((JCNewClass)t2).def.sym = null;
              return matchNewClass((JCNewClass)t1, (JCNewClass)t2);
          case NEWARRAY:
              return matchNewArray((JCNewArray)t1, (JCNewArray)t2);
          case PARENS:
              return treesMatch(((JCParens)t1).expr, ((JCParens)t2).expr);
          case ASSIGN:
              return matchAssign((JCAssign)t1, (JCAssign)t2);
          case TYPECAST:
              return matchTypeCast((JCTypeCast)t1, (JCTypeCast)t2);
          case TYPETEST:
              return matchTypeTest((JCInstanceOf)t1, (JCInstanceOf)t2);
          case INDEXED:
              return matchIndexed((JCArrayAccess)t1, (JCArrayAccess)t2);
          case SELECT:
              return matchSelect((JCFieldAccess) t1, (JCFieldAccess) t2);
          case IDENT:
              return ((JCIdent)t1).getName().contentEquals(((JCIdent)t2).getName());
          case LITERAL:
              return matchLiteral((JCLiteral)t1, (JCLiteral)t2);
          case TYPEIDENT:
              return ((JCPrimitiveTypeTree)t1).typetag == ((JCPrimitiveTypeTree)t2).typetag;
          case TYPEARRAY:
              return treesMatch(((JCArrayTypeTree)t1).elemtype, ((JCArrayTypeTree)t2).elemtype);
          case TYPEAPPLY:
              return matchTypeApply((JCTypeApply)t1, (JCTypeApply)t2);
          case TYPEPARAMETER:
              return matchTypeParameter((JCTypeParameter)t1, (JCTypeParameter)t2);
          case WILDCARD:
              return matchWildcard((JCWildcard)t1, (JCWildcard)t2);
          case TYPEBOUNDKIND:
              return ((TypeBoundKind)t1).kind == ((TypeBoundKind)t2).kind;
          case ANNOTATION: case TYPE_ANNOTATION:
              return matchAnnotation((JCAnnotation)t1, (JCAnnotation)t2);
          case LETEXPR:
              return matchLetExpr((LetExpr)t1, (LetExpr)t2);
          case POS:
          case NEG:
          case NOT:
          case COMPL:
          case PREINC:
          case PREDEC:
          case POSTINC:
          case POSTDEC:
          case NULLCHK:
              return matchUnary((JCUnary)t1, (JCUnary)t2);
          case OR:
          case AND:
          case BITOR:
          case BITXOR:
          case BITAND:
          case EQ:
          case NE:
          case LT:
          case GT:
          case LE:
          case GE:
          case SL:
          case SR:
          case USR:
          case PLUS:
          case MINUS:
          case MUL:
          case DIV:
          case MOD:
              return matchBinary((JCBinary)t1, (JCBinary)t2);
          case BITOR_ASG:
          case BITXOR_ASG:
          case BITAND_ASG:
          case SL_ASG:
          case SR_ASG:
          case USR_ASG:
          case PLUS_ASG:
          case MINUS_ASG:
          case MUL_ASG:
          case DIV_ASG:
          case MOD_ASG:
              return matchAssignop((JCAssignOp)t1, (JCAssignOp)t2);
          case ANNOTATED_TYPE:
              return matchAnnotatedType((JCAnnotatedType) t1, (JCAnnotatedType) t2);
          case LAMBDA:
              return matchLambda((JCLambda)t1, (JCLambda)t2);
          default:
              String msg = ((com.sun.source.tree.Tree)t1).getKind().toString() +
                      " " + t1.getClass().getName();
              throw new AssertionError(msg);
        }
    }

    private boolean kindChanged(long oldFlags, long newFlags) {
        return (oldFlags & (Flags.INTERFACE | Flags.ENUM | Flags.ANNOTATION))
                != (newFlags & (Flags.INTERFACE | Flags.ENUM | Flags.ANNOTATION));
    }

    protected boolean nameChanged(Name oldName, Name newName) {
        if (oldName == newName)
            return false;
        if (oldName == null || newName == null)
            return true;
        byte[] arr1 = oldName.toUtf();
        byte[] arr2 = newName.toUtf();
        int len = arr1.length;
        if (len != arr2.length)
            return true;
        for (int i = 0; i < len; i++)
            if (arr1[i] != arr2[i])
                return true;
        return false;
    }

    protected int diffList2(
        List<? extends JCTree> oldList, List<? extends JCTree> newList,
        int initialPos, PositionEstimator estimator)
    {
        if (oldList == newList)
            return initialPos;
        assert oldList != null && newList != null;
        int lastOldPos = initialPos;

        ListMatcher<JCTree> matcher = ListMatcher.<JCTree>instance(oldList, newList);
        if (!matcher.match()) {
            return initialPos;
        }
        Iterator<? extends JCTree> oldIter = oldList.iterator();
        ResultItem<JCTree>[] result = matcher.getTransformedResult();
        Separator s = matcher.separatorInstance();
        s.compute();
        int[][] matrix = estimator.getMatrix();
        int testPos = initialPos;
        int i = 0;
        int newIndex = 0;
        boolean firstNewItem = true;
        for (int j = 0; j < result.length; j++) {
            JCTree oldT;
            ResultItem<JCTree> item = result[j];
            switch (item.operation) {
                case MODIFY: {
                    // perhaps I shouldn't support this!
                    tokenSequence.moveIndex(matrix[i][4]);
                    if (tokenSequence.moveNext()) {
                        testPos = tokenSequence.offset();
                        if (JavaTokenId.COMMA == tokenSequence.token().id())
                            testPos += JavaTokenId.COMMA.fixedText().length();
                    }
                    oldT = oldIter.next(); ++i;
                    if (!firstNewItem)
                        copyTo(lastOldPos, getOldPos(oldT));

                    if (treesMatch(oldT, item.element, false)) {
                        lastOldPos = diffTree(oldT, item.element, getBounds(oldT));
                    } else {
                        printer.print(item.element);
                        lastOldPos = Math.max(testPos, endPos(oldT));
                    }
                    firstNewItem = false;
                    newIndex++;
                    break;
                }
                case INSERT: {
                    String prec = s.head(j) ? estimator.head() : s.prev(j) ? estimator.sep() : null;
                    String tail = s.next(j) ? estimator.sep() : null;
                    if (estimator.getIndentString() != null && !estimator.getIndentString().equals(" ")) {
                        prec += estimator.getIndentString();
                    }
                    copyTo(lastOldPos, testPos);
                    printer.print(prec);
                    printer.print(item.element);
                    printer.print(tail);
                    //append(Diff.insert(testPos, prec, item.element, tail, LineInsertionType.NONE));
                    firstNewItem = false;
                    newIndex++;
                    break;
                }
                case DELETE: {
                    // compute offsets for removal (tree bounds are not enough
                    // in this case, we have to remove also tokens around like
                    // preceding keyword, i.e. throws, implements etc. and also
                    // separators like commas.

                    // this is a hack: be careful when removing the first:
                    // throws Exception,IOException... do not remove the space
                    // after throws keyword, there is not space after comma!
                    int delta = 0;
                    if (i == 0 && matrix[i+1][2] != -1 && matrix[i+1][2] == matrix[i+1][3]) {
                        ++delta;
                    }
                    int startOffset = toOff(s.head(j) || s.prev(j) ? matrix[i][1] : matrix[i][2+delta]);
                    int endOffset = toOff(s.tail(j) || s.next(j) ? matrix[i+1][2] : matrix[i][4]);
                    assert startOffset != -1 && endOffset != -1 : "Invalid offset!";
                    //printer.print(origText.substring(lastOldPos, startOffset));
                    //append(Diff.delete(startOffset, endOffset));
                    tokenSequence.moveIndex(matrix[i][4]);
                    if (tokenSequence.moveNext()) {
                        testPos = tokenSequence.offset();
                        if (JavaTokenId.COMMA == tokenSequence.token().id()) {
                            moveToDifferentThan(tokenSequence, Direction.FORWARD, EnumSet.of(JavaTokenId.WHITESPACE));
                            testPos = tokenSequence.offset();
                        }
                    }
                    if (i == 0 && !newList.isEmpty()) {
                        lastOldPos = endOffset;
                    } else {
                        lastOldPos = Math.max(testPos, endPos(item.element));
                    }
                    oldT = oldIter.next(); ++i;
                    break;
                }
                case NOCHANGE: {
                    tokenSequence.moveIndex(matrix[i][4]);
                    if (tokenSequence.moveNext()) {
                        testPos = tokenSequence.offset();
                        if (JavaTokenId.COMMA == tokenSequence.token().id()) {
                            moveToDifferentThan(tokenSequence, Direction.FORWARD, EnumSet.of(JavaTokenId.WHITESPACE));
                            testPos = tokenSequence.offset();
                        }
                    }
                    oldT = oldIter.next(); ++i;
                    newIndex++;
                    int copyTo;
                    if (newIndex < newList.size()) {
                        copyTo = Math.max(testPos, endPos(oldT));
                    } else {
                        copyTo = endPos(oldT);
                    }
                    copyTo(lastOldPos, lastOldPos = copyTo);
                    firstNewItem = false;
                    break;
                }
            }
        }
        return lastOldPos;
    }

    /**
     * Rewrites <code>break</code> or <code>continue</code> tree.
     * @param bounds original bounds
     * @param oldTLabel old label
     * @param newTlabel new label
     * @param oldT the tree to be rewritten
     * @return new bounds
     */
    private int printBreakContinueTree(int[] bounds, final Name oldTLabel, final Name newTlabel, JCStatement oldT) {
        int localPointer = bounds[0];
        String stmt = oldT.getKind() == Kind.BREAK ? "break" : "continue"; //NOI18N
        if (nameChanged(oldTLabel, newTlabel)) {
            copyTo(localPointer, localPointer = getOldPos(oldT));
            printer.print(stmt);
            localPointer += stmt.length();
            if (oldTLabel != null && oldTLabel.length() > 0) {
                // XXX could be arbitrary whitespace between break/continue and its label
                localPointer += 1;
            }
            if (newTlabel != null && newTlabel.length() > 0) {
                printer.print(" ");
                printer.print(newTlabel);
            }
            if (oldTLabel != null) {
                localPointer += oldTLabel.length();
            }
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    private int toOff(int tokenIndex) {
        if (tokenIndex == -1) {
            return -1;
        }
        tokenSequence.moveIndex(tokenIndex);
        tokenSequence.moveNext();
        return tokenSequence.offset();
    }

    /**
     * Diff two lists of parameters separated by comma. It is used e.g.
     * from type parameters and method parameters.
     *
     */
    private int diffParameterList(
            List<? extends JCTree> oldList,
            List<? extends JCTree> newList,
            JavaTokenId[] makeAround,
            int pos,
            Comparator<JCTree> measure)
    {
        return diffParameterList(oldList, newList, null, makeAround, pos, measure);
    }
    private int diffParameterList(
            List<? extends JCTree> oldList,
            List<? extends JCTree> newList,
            JCTree parent,
            JavaTokenId[] makeAround,
            int pos,
            Comparator<JCTree> measure)
    {
        return diffParameterList(oldList, newList, parent, makeAround, pos, measure, diffContext.style.spaceBeforeComma(), diffContext.style.spaceAfterComma(), false, ",");   //NOI18N
    }
    private int diffParameterList(
            List<? extends JCTree> oldList,
            List<? extends JCTree> newList,
            JavaTokenId[] makeAround,
            int pos,
            Comparator<JCTree> measure,
            boolean spaceBefore,
            boolean spaceAfter,
            boolean isEnum,
            String separator)
    {
        return diffParameterList(oldList, newList, null, makeAround, pos, measure, spaceBefore, spaceAfter, isEnum, separator);
    }
    
    private int diffParameterList(
            List<? extends JCTree> oldList,
            List<? extends JCTree> newList,
            JCTree parent,
            JavaTokenId[] makeAround,
            int pos,
            Comparator<JCTree> measure,
            boolean spaceBefore,
            boolean spaceAfter,
            boolean isEnum,
            String separator)
    {
        assert oldList != null && newList != null;
        if (oldList == newList || oldList.equals(newList))
            return pos; // they match perfectly or no need to do anything

        boolean printParens = makeAround != null && makeAround.length != 0;
        if (newList.isEmpty()) {
            int endPos = endPos(oldList);
            if (printParens) {
                tokenSequence.move(endPos);
                moveFwdToToken(tokenSequence, endPos, makeAround[1]);
                tokenSequence.moveNext();
                endPos = tokenSequence.offset();
                if (!nonRelevant.contains(tokenSequence.token()))
                    printer.print(" "); // use options, if mods should be at new line
            }
            return endPos;
        }
        ListMatcher<JCTree> matcher = ListMatcher.<JCTree>instance(oldList, newList, measure);
        if (!matcher.match()) {
            // nothing in the list, no need to print and nothing was printed
            return pos;
        }
        ResultItem<JCTree>[] result = matcher.getResult();
        if (printParens && oldList.isEmpty()) {
            printer.print(makeAround[0].fixedText());
        }
        int oldIndex = 0;
        boolean wasLeadingDelete = false;
        boolean wasComma = false;
        for (int j = 0; j < result.length; j++) {
            ResultItem<JCTree> item = result[j];
            switch (item.operation) {
                case MODIFY: {
                    JCTree tree = oldList.get(oldIndex++);
                    int[] bounds = getBounds(tree);
                    tokenSequence.move(bounds[0]);
                    if (oldIndex != 1 && !separator.isEmpty()) {
                        moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                    }
                    tokenSequence.moveNext();
                    int start = Math.max(tokenSequence.offset(), pos);
                    copyTo(start, bounds[0], printer);
                    diffTree(tree, item.element, parent, bounds);
                    tokenSequence.move(bounds[1]);
                    moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                    if (!commaNeeded(result, item) &&
                        isEnum &&
                        tokenSequence.token().id() == JavaTokenId.RBRACKET) {
                        printer.print(";");
                    }
                    copyTo(bounds[1], pos = Math.max(tokenSequence.offset(), bounds[1]), printer);
                    wasLeadingDelete = false;
                    break;
                }
                // insert new element
                case INSERT: {
                    if (wasComma) {
                        if (spaceAfter) {
                            printer.print(" ");
                        }
                    }
                    printer.print(item.element);
                    wasLeadingDelete = false;
                    break;
                }
                case DELETE:
                    wasLeadingDelete |= oldIndex++ == 0;
                    int endPos = getBounds(item.element)[1];
                    tokenSequence.move(endPos);
                    moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                    if (tokenSequence.token().id() == JavaTokenId.COMMA) {
                        if (tokenSequence.moveNext()) {
                            moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                        }
                    }
                    pos = Math.max(tokenSequence.offset(), endPos);
                    break;
                // just copy existing element
                case NOCHANGE:
                    if (oldIndex++ == 0 && wasComma) {
                        if (spaceAfter) {
                            printer.print(" ");
                        }
                    }
                    int[] bounds = getCommentCorrectedBounds(item.element);
                    tokenSequence.move(bounds[0]);
                    if (oldIndex != 1 && !wasLeadingDelete && !separator.isEmpty()) {
                        moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                    }
                    tokenSequence.moveNext();
                    int start = tokenSequence.offset();
                    tokenSequence.move(bounds[1]);
                    moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                    int end;
                    if (isEnum) {
                        if ((tokenSequence.token().id() == JavaTokenId.SEMICOLON || tokenSequence.token().id() == JavaTokenId.COMMA)) {
                            end = tokenSequence.offset();
                        } else {
                            end = bounds[1];
                        }
                    } else if (oldIndex < oldList.size()) {
                        end = tokenSequence.offset();
                    } else {
                        end = bounds[1];
                    }
                    copyTo(start, pos = end, printer);
                    wasLeadingDelete = false;
                    break;
                default:
                    break;
            }
            if (commaNeeded(result, item)) {
                if ((item.operation == Operation.INSERT || (oldIndex == oldList.size() && j + 1 < result.length && result[j + 1].operation == Operation.INSERT)) && spaceBefore) {
                    printer.print(" ");
                }
                printer.print(separator);
                wasComma = true;
            } else {
                if (item.operation != Operation.DELETE) {
                    wasComma = false;
                }
//                printer.print(";");
            }
        }
        if (printParens && oldList.isEmpty()) {
            printer.print(makeAround[1].fixedText());
        }
        if (oldList.isEmpty()) {
            return pos;
        } else {
            int endPos2 = endPos(oldList);
            tokenSequence.move(endPos2);
            moveToSrcRelevant(tokenSequence, Direction.FORWARD);
            if (isEnum &&
                (tokenSequence.token().id() == JavaTokenId.SEMICOLON || tokenSequence.token().id() == JavaTokenId.COMMA)) {
                return tokenSequence.offset();
            }
            return pos;
        }
    }

    /**
     * Diff two lists of parameters separated by comma. It is used e.g.
     * from type parameters and method parameters.
     *
     */
    private int diffVarGroup(
            List<? extends JCTree> oldList,
            List<? extends JCTree> newList,
            JavaTokenId[] makeAround,
            int pos,
            Comparator<JCTree> measure)
    {
        assert oldList != null && newList != null;
        if (oldList == newList || oldList.equals(newList))
            return pos; // they match perfectly or no need to do anything

        boolean printParens = makeAround != null && makeAround.length != 0;
        if (newList.isEmpty()) {
            int endPos = endPos(oldList);
            if (printParens) {
                tokenSequence.move(endPos);
                moveFwdToToken(tokenSequence, endPos, makeAround[1]);
                tokenSequence.moveNext();
                endPos = tokenSequence.offset();
                if (!nonRelevant.contains(tokenSequence.token()))
                    printer.print(" "); // use options, if mods should be at new line
            }
            return endPos;
        }
        ListMatcher<JCTree> matcher = ListMatcher.<JCTree>instance(oldList, newList, measure);
        if (!matcher.match()) {
            // nothing in the list, no need to print and nothing was printed
            return pos;
        }
        ResultItem<JCTree>[] result = matcher.getResult();
        if (printParens && oldList.isEmpty()) {
            printer.print(makeAround[0].fixedText());
        }
        int oldIndex = 0;
        boolean skipWhitespaces = false;
        for (int j = 0; j < result.length; j++) {
            ResultItem<JCTree> item = result[j];
            switch (item.operation) {
                case MODIFY: {
                    JCTree tree = oldList.get(oldIndex++);
                    int[] bounds = getBounds(tree);
                    if (oldIndex != 1) {
                        bounds[0] = tree.pos;
                    }
                    tokenSequence.move(bounds[1]);
                    tokenSequence.movePrevious();
                    if (tokenSequence.token().id() == JavaTokenId.COMMA || tokenSequence.token().id() == JavaTokenId.SEMICOLON) {
                        bounds[1] = tokenSequence.offset();
                    }
                    tokenSequence.move(bounds[0]);
                    if (oldIndex != 1 && !skipWhitespaces) {
                        moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                    }
                    tokenSequence.moveNext();
                    int start = tokenSequence.offset();
                    copyTo(start, start = bounds[0], printer);
                    CommentSet old = comments.getComments(tree);
                    CommentSet cs = comments.getComments(item.element);
                    List<Comment> oldPrecedingComments = old.getComments(CommentSet.RelativePosition.PRECEDING);
                    List<Comment> newPrecedingComments = cs.getComments(CommentSet.RelativePosition.PRECEDING);
                    int indentReset = -1;
                    if (oldPrecedingComments.isEmpty() && !newPrecedingComments.isEmpty()) {
                        if (printer.out.isWhitespaceLine()) {
                            indentReset = printer.getIndent();
                            printer.setIndent(printer.out.getCol());
                        } else {
                            printer.newline();
                            printer.toLeftMargin();
                        }
                    }
                    start = diffPrecedingComments(tree, item.element, bounds[0], start);
                    if (indentReset != (-1)) {
                        printer.setIndent(indentReset);
                    }
                    int localPointer;
                    if (oldIndex != 1) {
                        localPointer = diffVarDef((JCVariableDecl) tree, (JCVariableDecl) item.element, bounds[0]);
                    } else {
                        localPointer = diffVarDef((JCVariableDecl) tree, (JCVariableDecl) item.element, bounds);
                    }
                    copyTo(localPointer, pos = bounds[1], printer);
                    skipWhitespaces = false;
                    break;
                }
                case INSERT: {
                    JCVariableDecl decl = (JCVariableDecl) item.element;
                    
                    if (oldIndex == 0) {
                        int oldPrec = printer.setPrec(TreeInfo.noPrec);
                        printer.visitVarDef(decl);
                        printer.setPrec(oldPrec);
                    } else {
                        if (diffContext.style.spaceAfterComma()) {
                            printer.print(" ");
                        }
                        printer.print(decl.name);
                        printer.printVarInit(decl);
                    }
                    skipWhitespaces = false;
                    break;
                }
                // just copy existing element
                case NOCHANGE: {
                    oldIndex++;
                    int[] bounds = getBounds(item.element);
                    if (j != 0) {
                        bounds[0] = item.element.pos;
                    }
                    tokenSequence.move(bounds[0]);
                    if (j != 0 && !skipWhitespaces) {
                        moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                    }
                    tokenSequence.moveNext();
                    int start = tokenSequence.offset();
                    int end = bounds[1];
                    tokenSequence.move(end);
                    tokenSequence.movePrevious();
                    if (tokenSequence.token().id() == JavaTokenId.COMMA || tokenSequence.token().id() == JavaTokenId.SEMICOLON) {
                        end = tokenSequence.offset();
                    }
                    copyTo(start, pos = end, printer);
                    skipWhitespaces = false;
                    break;
                }
                case DELETE: {
                    skipWhitespaces = false;
                    if (j == 0) {
                        //deleting the very first variable, diff the modifiers and type explicitly:
                        JCVariableDecl oldEl = (JCVariableDecl) oldList.get(0);
                        JCVariableDecl newEl = (JCVariableDecl) newList.get(0);
                        int[] bounds = getBounds(oldEl.getModifiers());
                        copyTo(pos, bounds[0]);
                        pos = diffTree(oldEl.getModifiers(), newEl.getModifiers(), bounds);
                        bounds = getBounds(oldEl.getType());
                        copyTo(pos, pos = bounds[0]);
                        pos = diffTree(oldEl.getType(), newEl.getType(), bounds);
                        copyTo(pos, item.element.pos);
                        skipWhitespaces = true;
                    }
                    int[] bounds = getBounds(item.element);
                    tokenSequence.move(bounds[1]);
                    tokenSequence.movePrevious();
                    if (tokenSequence.token().id() == JavaTokenId.COMMA || tokenSequence.token().id() == JavaTokenId.SEMICOLON) {
                        bounds[1] = tokenSequence.offset();
                    }
                    pos = bounds[1];
                    break;
                }
                default:
                    break;
            }
            if (commaNeeded(result, item)) {
                printer.print(",");
            }
        }
        if (printParens && oldList.isEmpty()) {
            printer.print(makeAround[1].fixedText());
        }
        return pos;
    }

    protected int diffUnionType(JCTypeUnion oldT, JCTypeUnion newT, int[] bounds) {
        int localPointer = bounds[0];
        return diffParameterList(oldT.alternatives, newT.alternatives, null, localPointer, Measure.MEMBER, diffContext.style.spaceAroundBinaryOps(), diffContext.style.spaceAroundBinaryOps(), false, "|");
    }

    private boolean commaNeeded(ResultItem[] arr, ResultItem item) {
        if (item.operation == Operation.DELETE) {
            return false;
        }
        boolean result = false;
        for (int i = 0; i < arr.length; i++) {
            if (item == arr[i]) {
                result = true;
            } else if (result && arr[i].operation != Operation.DELETE) {
                return true;
            }
        }
        return false;
    }

    private List<JCTree> filterHidden(List<? extends JCTree> list) {
        return filterHidden(diffContext, list);
    }
    
    public static List<JCTree> filterHidden(DiffContext diffContext, List<? extends JCTree> list) {
        LinkedList<JCTree> result = new LinkedList<JCTree>(); // todo (#pf): capacity?
        List<JCVariableDecl> fieldGroup = new ArrayList<JCVariableDecl>();
        List<JCVariableDecl> enumConstants = new ArrayList<JCVariableDecl>();
        for (JCTree tree : list) {
            if (tree.pos == (-1)) continue;
            if (diffContext.syntheticTrees.contains(tree)) continue;
            else if (Kind.VARIABLE == tree.getKind()) {
                JCVariableDecl var = (JCVariableDecl) tree;
                if ((var.mods.flags & Flags.ENUM) != 0) {
                    // collect enum constants, make a field group from them
                    // and set the flag.
                    enumConstants.add(var);
                } else {
                    if (!fieldGroup.isEmpty()) {
                        int oldPos = getOldPos(fieldGroup.get(0));

                        if (oldPos != (-1) && oldPos != NOPOS && oldPos == getOldPos(var) && fieldGroup.get(0).getModifiers() == var.getModifiers()) {
                            //seems like a field group:
                            fieldGroup.add(var);
                        } else {
                            if (fieldGroup.size() > 1) {
                                result.add(new FieldGroupTree(fieldGroup));
                            } else {
                                result.add(fieldGroup.get(0));
                            }
                            fieldGroup = new ArrayList<JCVariableDecl>();

                            fieldGroup.add(var);
                        }
                    } else {
                        fieldGroup.add(var);
                    }
                }
                continue;
            }

            if (!fieldGroup.isEmpty()) {
                if (fieldGroup.size() > 1) {
                    result.add(new FieldGroupTree(fieldGroup));
                } else {
                    result.add(fieldGroup.get(0));
                }
                fieldGroup = new ArrayList<JCVariableDecl>();
            }

            if (Kind.METHOD == tree.getKind()) {
                // filter syntetic constructors, i.e. constructors which are in
                // the tree, but not available in the source.
                if (tree.pos == (-1) || (((JCMethodDecl)tree).mods.flags & Flags.GENERATEDCONSTR) != 0)
                    continue;
            } else if (Kind.BLOCK == tree.getKind()) {
                JCBlock block = (JCBlock) tree;
                if (block.stats.isEmpty() && block.pos == -1 && block.flags == 0)
                    // I believe this is an sythetic block
                    continue;
            }

            result.add(tree);
        }
        if (!fieldGroup.isEmpty()) {
            if (fieldGroup.size() > 1) {
                result.add(new FieldGroupTree(fieldGroup));
            } else {
                result.add(fieldGroup.get(0));
            }
        }
        if (!enumConstants.isEmpty()) {
            result.addFirst(new FieldGroupTree(enumConstants, !result.isEmpty()));
        }
        return result;
    }

    private int diffList(
            List<? extends JCTree> oldList,
            List<? extends JCTree> newList,
            int localPointer,
            PositionEstimator estimator,
            Comparator<JCTree> measure,
            VeryPretty printer)
    {
        if (oldList == newList || oldList.equals(newList)) {
            return localPointer;
        }
        assert oldList != null && newList != null;

        ListMatcher<JCTree> matcher = ListMatcher.<JCTree>instance(
                oldList,
                newList,
                measure
        );
        if (!matcher.match()) {
            return localPointer;
        }
        JCTree lastdel = null; // last deleted element
        ResultItem<JCTree>[] result = matcher.getResult();

        // if there hasn't been import but at least one is added
        if (oldList.isEmpty() && !newList.isEmpty()) {
            // such a situation needs special handling. It is difficult to
            // obtain a correct position.
            StringBuilder aHead = new StringBuilder(), aTail = new StringBuilder();
            int pos = estimator.prepare(localPointer, aHead, aTail);
            copyTo(localPointer, pos, printer);

            if (newList.get(0).getKind() == Kind.IMPORT) {
                printer.printImportsBlock(newList, true);
            } else {
                printer.print(aHead.toString());
                for (JCTree item : newList) {
                    if (LineInsertionType.BEFORE == estimator.lineInsertType()) printer.newline();
                    printer.print(item);
                    if (LineInsertionType.AFTER == estimator.lineInsertType()) printer.newline();
                }
                printer.print(aTail.toString());
            }
            return pos;
        }

        // if there has been imports which is removed now
        if (newList.isEmpty() && !oldList.isEmpty()) {
            int[] removalBounds = estimator.sectionRemovalBounds(null);
            copyTo(localPointer, removalBounds[0]);
            return removalBounds[1];
        }
        CodeStyle.ImportGroups importGroups = newList.get(0).getKind() == Kind.IMPORT && diffContext.style.separateImportGroups()
                ? diffContext.style.getImportGroups() : null;
        int lastGroup = -1;
        int i = 0;
        // copy to start position
        int insertPos = estimator.getInsertPos(0);
        if (insertPos > localPointer) {
            copyTo(localPointer, localPointer = estimator.getInsertPos(0), printer);
        } else {
            insertPos = localPointer;
        }
        // go on, match it!
        for (int j = 0; j < result.length; j++) {
            ResultItem<JCTree> item = result[j];
            int group = -1;
            if (importGroups != null) {
                Name name = printer.fullName(((JCImport)item.element).qualid);
                group = importGroups != null && name != null ? importGroups.getGroupId(name.toString(), ((JCImport)item.element).staticImport) : -1;
            }
            switch (item.operation) {
                case MODIFY: {
                    lastGroup = group;
                    int[] bounds = estimator.getPositions(i);
                    bounds[0] = Math.min(bounds[0], getCommentCorrectedOldPos(oldList.get(i)));
                    copyTo(localPointer, bounds[0], printer);
                    localPointer = diffTree(oldList.get(i), item.element, bounds);
                    ++i;
                    break;
                }
                case INSERT: {
                    boolean insetBlankLine = lastGroup >= 0 && lastGroup != group;
                    lastGroup = group;
                    int pos = importGroups != null ? i == 0 || insetBlankLine && i < oldList.size() ? estimator.getPositions(i)[0] : estimator.getPositions(i-1)[2]
                            : estimator.getInsertPos(i);
                    if (pos > localPointer) {
                        copyTo(localPointer, localPointer = pos);
                    }
                    if (insetBlankLine)
                        printer.blankline();
                    int oldPos = item.element.getKind() != Kind.VARIABLE ? getOldPos(item.element) : item.element.pos;
                    boolean found = false;
                    if (oldPos > 0) {
                        for (JCTree oldT : oldList) {
                            int oldNodePos = oldT.getKind() != Kind.VARIABLE ? getOldPos(oldT) : oldT.pos;
                            if (oldPos == oldNodePos) {
                                found = true;
                                VeryPretty oldPrinter = this.printer;
                                int old = oldPrinter.indent();
                                this.printer = new VeryPretty(diffContext, diffContext.style, tree2Tag, tree2Doc, tag2Span, origText, oldPrinter.toString().length() + oldPrinter.getInitialOffset());//XXX
                                this.printer.reset(old);
                                this.printer.oldTrees = oldTrees;
                                int index = oldList.indexOf(oldT);
                                int[] poss = estimator.getPositions(index);
                                int end = diffTree(oldT, item.element, poss);
                                copyTo(end, poss[1]);
                                printer.print(this.printer.toString());
                                printer.reindentRegions.addAll(this.printer.reindentRegions);
                                this.printer = oldPrinter;
                                this.printer.undent(old);
                                break;
                            }
                        }
                    }
                    if (!found) {
                        if (lastdel != null) {
                            boolean wasInFieldGroup = false;
                            if(lastdel instanceof FieldGroupTree) {
                                FieldGroupTree fieldGroupTree = (FieldGroupTree) lastdel;
                                for (JCVariableDecl var : fieldGroupTree.getVariables()) {
                                    if(treesMatch(item.element, var, false)) {
                                        wasInFieldGroup = true;
                                        oldTrees.remove(item.element);
                                        break;
                                    }
                                }
                            }
                            if(wasInFieldGroup || treesMatch(item.element, lastdel, false)) {
                                VeryPretty oldPrinter = this.printer;
                                int old = oldPrinter.indent();
                                this.printer = new VeryPretty(diffContext, diffContext.style, tree2Tag, tree2Doc, tag2Span, origText, oldPrinter.toString().length() + oldPrinter.getInitialOffset());//XXX
                                this.printer.reset(old);
                                this.printer.oldTrees = oldTrees;
                                int index = oldList.indexOf(lastdel);
                                int[] poss = estimator.getPositions(index);
                                //TODO: should the original text between the return position of the following method and poss[1] be copied into the new text?
                                localPointer = diffTree(lastdel, item.element, poss);
                                printer.print(this.printer.toString());
                                printer.reindentRegions.addAll(this.printer.reindentRegions);
                                this.printer = oldPrinter;
                                this.printer.undent(old);
                                lastdel = null;
                                break;
                            }
                        }
                        if (LineInsertionType.BEFORE == estimator.lineInsertType()) printer.newline();
                        printer.print(item.element);
                        if (LineInsertionType.AFTER == estimator.lineInsertType()) printer.newline();
                    }
                    break;
                }
                case DELETE: {
                    int[] pos = estimator.getPositions(i);
                    if (localPointer < pos[0] && lastdel == null) {
                        copyTo(localPointer, pos[0], printer);
                    }
                    lastdel = oldList.get(i);
                    ++i;
                    CommentSet ch = comments.getComments(lastdel);
                    localPointer = Math.max(pos[1], Math.max(commentEnd(ch, CommentSet.RelativePosition.INLINE), commentEnd(ch, CommentSet.RelativePosition.TRAILING)));
                    break;
                }
                case NOCHANGE: {
                    lastGroup = group;
                    int[] pos = estimator.getPositions(i);
                    if (pos[0] > localPointer && i != 0) {
                        // print fill-in
                        copyTo(localPointer, pos[0], printer);
                    }
                    if (pos[0] >= localPointer) {
                        localPointer = pos[0];
                        if (pos.length > 3 && pos[3] != (-1) && j + 1 < result.length) {
                            copyTo(localPointer, localPointer = pos[3], printer);
                            printer.print(estimator.append(i));
                        }
                    }
                    copyTo(localPointer, localPointer = pos[1], printer);
                    lastdel = null;
                    ++i;
                    break;
                }
            }
        }
        return localPointer;
    }

    protected int diffPrecedingComments(JCTree oldT, JCTree newT, int localPointer) {
        return diffPrecedingComments(oldT, newT, getOldPos(oldT), localPointer);
    }
    
    protected int diffPrecedingComments(JCTree oldT, JCTree newT, int oldTreeStartPos, int localPointer) {
        CommentSet cs = comments.getComments(newT);
        CommentSet old = comments.getComments(oldT);
        List<Comment> oldPrecedingComments = old.getComments(CommentSet.RelativePosition.PRECEDING);
        List<Comment> newPrecedingComments = cs.getComments(CommentSet.RelativePosition.PRECEDING);
        DocCommentTree newD = tree2Doc.get(newT);
        if (sameComments(oldPrecedingComments, newPrecedingComments) && newD == null)
            return localPointer;
        DocCommentTree oldD = oldTopLevel.docComments.getCommentTree(oldT);
        return diffCommentLists(oldTreeStartPos, oldPrecedingComments, newPrecedingComments, oldD, newD, false, true, localPointer);
    }

    protected int diffTrailingComments(JCTree oldT, JCTree newT, int localPointer) {
        CommentSet cs = comments.getComments(newT);
        CommentSet old = comments.getComments(oldT);
        List<Comment> oldInlineComments = old.getComments(CommentSet.RelativePosition.INLINE);
        List<Comment> newInlineComments = cs.getComments(CommentSet.RelativePosition.INLINE);
        List<Comment> oldTrailingComments = old.getComments(CommentSet.RelativePosition.TRAILING);
        List<Comment> newTrailingComments = cs.getComments(CommentSet.RelativePosition.TRAILING);
        if (sameComments(oldInlineComments, newInlineComments) && sameComments(oldTrailingComments, newTrailingComments))
            return localPointer;

        //XXX: hack: the upper diff might already add '\n' to the result, need to skip it if diffing inline comments
        if (!sameComments(oldInlineComments, newInlineComments)) {
            while (printer.out.isWhitespaceLine())
                printer.eatChars(1);
        }
        
        localPointer = diffCommentLists(getOldPos(oldT), oldInlineComments, newInlineComments, null, null, false, false, localPointer);

        boolean containedEmbeddedNewLine = false;
        boolean containsEmbeddedNewLine = false;

        for (Comment oldComment : oldInlineComments) {
            if (oldComment.style() == Style.LINE) containedEmbeddedNewLine = true;
        }
        
        for (Comment nueComment : newInlineComments) {
            if (nueComment.style() == Style.LINE) containsEmbeddedNewLine = true;
        }

        if (containedEmbeddedNewLine  && !containsEmbeddedNewLine) {
            printer.print("\n");
        }

        return diffCommentLists(getOldPos(oldT), oldTrailingComments, newTrailingComments, null, null, true, false, localPointer);
    }

    private boolean sameComments(List<Comment> oldList, List<Comment> newList) {
        Iterator<Comment> oldIter = oldList.iterator();
        Iterator<Comment> newIter = newList.iterator();
        Comment oldC = safeNext(oldIter);
        Comment newC = safeNext(newIter);
        
        while (oldC != null && newC != null) {
            if (!commentsMatch(oldC, newC)) return false;
            oldC = safeNext(oldIter);
            newC = safeNext(newIter);
        }

        return !((oldC == null) ^ (newC == null));
    }
    
    // refactor it! make it better
    private int diffCommentLists(int oldTreeStartPos, List<Comment> oldList,
            List<Comment> newList, DocCommentTree oldDoc, DocCommentTree newDoc, boolean trailing, boolean preceding, int localPointer) {
        Comment javadoc = null;
        for (Comment comment : oldList) {
            if(comment.style() == Style.JAVADOC) {
                javadoc = comment;
            }
        }
        Iterator<Comment> oldIter = oldList.iterator();
        Iterator<Comment> newIter = newList.iterator();
        Comment oldC = safeNext(oldIter);
        Comment newC = safeNext(newIter);
        boolean first = true;
        boolean firstNewCommentPrinted = false;
        while (oldC != null && newC != null) {
            int cStart = commentStartCorrect(oldC);
            if (first && trailing && localPointer < cStart) {
                copyTo(localPointer, cStart);
            }
            first = false;
            int nextTarget = Math.max(localPointer, oldC.endPos());
            if (commentsMatch(oldC, newC)) {
                if(preceding && oldC == javadoc && oldDoc != null) {
                    localPointer = diffDocTree((DCDocComment)oldDoc, (DCTree)oldDoc, (DCTree)newDoc, new int[]{localPointer, oldC.endPos()});
                }
                if (nextTarget > localPointer) {
                    copyTo(localPointer, nextTarget);
                }
                oldC = safeNext(oldIter);
                newC = safeNext(newIter);
                firstNewCommentPrinted = true;
            } else if (!listContains(newList, oldC)) {
                if  (!listContains(oldList, newC)) {
//                    append(Diff.modify(oldT, newT, oldC, newC));
                    copyTo(localPointer, localPointer = oldC.pos());
                    printer.printComment(newC, !trailing, false, true);
                    oldC = safeNext(oldIter);
                    newC = safeNext(newIter);
                } else {
//                    append(Diff.delete(oldT, newT, oldC));
                    oldC = safeNext(oldIter);
                }
            } else {
                if (!firstNewCommentPrinted && preceding) {
                    copyTo(localPointer, localPointer = oldTreeStartPos);
                }
                printer.print(newC.getText());
                newC = safeNext(newIter);
                firstNewCommentPrinted = true;
            }
            localPointer = nextTarget;
        }
        while (oldC != null) {
//            append(Diff.delete(oldT, newT, oldC));
            int cStart = commentStartCorrect(oldC);
            if (first && trailing && localPointer < cStart) {
                copyTo(localPointer, cStart);
            }
            first = false;
            localPointer = Math.max(localPointer, oldC.endPos());
            oldC = safeNext(oldIter);
        }
        while (newC != null) {
            if (Style.WHITESPACE != newC.style()) {
//                printer.print(newC.getText());                
                if (!firstNewCommentPrinted && preceding) {
                    copyTo(localPointer, localPointer = oldTreeStartPos);
                }
                printer.printComment(newC, !trailing, false, !preceding && !trailing);
                firstNewCommentPrinted = true;
            }
            newC = safeNext(oldIter);
        }
        if(preceding && javadoc == null && newDoc != null) {
            if (!firstNewCommentPrinted && preceding) {
                copyTo(localPointer, localPointer = oldTreeStartPos);
            }
            printer.print((DCTree) newDoc);
        }
        return localPointer;
    }
    
    private int diffDocTree(DCDocComment doc, DCTree oldT, DCTree newT, int[] elementBounds) {
        if (oldT == null && newT != null) {
            throw new IllegalArgumentException("Null is not allowed in parameters.");
        }

        if (oldT == newT) {
            return elementBounds[0];
        }

        if (newT == null) {
            tokenSequence.move(elementBounds[1]);
            if (!tokenSequence.moveNext()) {
                return elementBounds[1];
            }
            while (tokenSequence.token().id() == JavaTokenId.WHITESPACE && tokenSequence.moveNext()) {
                // Skip whitespace
            }
            return tokenSequence.offset();
        }
        
        int localpointer = elementBounds[0];
        
        if (oldT.getKind() != newT.getKind()) {
            // different kind of trees found, print the whole new one.
            int[] oldBounds = getBounds(oldT, doc);
            if (oldBounds[0] > elementBounds[0]) {
                copyTo(elementBounds[0], oldBounds[0]);
            }
            printer.print(newT);
            return oldBounds[1];
        }
        
        switch(oldT.getKind()) {
            case ATTRIBUTE:
                localpointer = diffAttribute(doc, (DCAttribute) oldT, (DCAttribute) newT, elementBounds);
                break;
            case DOC_COMMENT:
                localpointer = diffDocComment(doc, (DCDocComment) oldT, (DCDocComment) newT, elementBounds);
                break;
            case PARAM:
                localpointer = diffParam(doc, (DCParam) oldT, (DCParam) newT, elementBounds);
                break;
            case RETURN:
                localpointer = diffReturn(doc, (DCReturn) oldT, (DCReturn) newT, elementBounds);
                break;
            case IDENTIFIER:
                localpointer = diffIdentifier(doc, (DCIdentifier) oldT, (DCIdentifier) newT, elementBounds);
                break;
            case SEE:
                localpointer = diffSee(doc, (DCSee) oldT, (DCSee) newT, elementBounds);
                break;
            /**
             * Used for instances of {@link LinkTree} representing an @linkplain tag.
             */
            case LINK_PLAIN:
            case LINK:
                localpointer = diffLink(doc, (DCLink)oldT, (DCLink)newT, elementBounds);
                break;
            case TEXT:
                localpointer = diffText(doc, (DCText)oldT, (DCText)newT, elementBounds);
                break;
            case AUTHOR:
                localpointer = diffAuthor(doc, (DCAuthor)oldT, (DCAuthor)newT, elementBounds);
                break;
            case COMMENT:
                localpointer = diffComment(doc, (DCComment)oldT, (DCComment)newT, elementBounds);
                break;
            case DEPRECATED:
                localpointer = diffDeprecated(doc, (DCDeprecated)oldT, (DCDeprecated)newT, elementBounds);
                break;
            case DOC_ROOT:
                localpointer = diffDocRoot(doc, (DCDocRoot)oldT, (DCDocRoot)newT, elementBounds);
                break;
            case ENTITY:
                localpointer = diffEntity(doc, (DCEntity)oldT, (DCEntity)newT, elementBounds);
                break;
            case ERRONEOUS:
                localpointer = diffErroneous(doc, (DCErroneous)oldT, (DCErroneous)newT, elementBounds);
                break;
            /**
             * Used for instances of {@link ThrowsTree} representing an
             *
             * @exception tag.
             */
            case EXCEPTION:
            case THROWS:
                localpointer = diffThrows(doc, (DCThrows)oldT, (DCThrows)newT, elementBounds);
                break;
            case INHERIT_DOC:
                localpointer = diffInheritDoc(doc, (DCInheritDoc)oldT, (DCInheritDoc)newT, elementBounds);
                break;
            /**
             * Used for instances of {@link LiteralTree} representing an @code tag.
             */
            case CODE:
            case LITERAL:
                localpointer = diffLiteral(doc, (DCLiteral)oldT, (DCLiteral)newT, elementBounds);
                break;
            case REFERENCE:
                localpointer = diffReference(doc, (DCReference)oldT, (DCReference)newT, elementBounds);
                break;
            case SERIAL:
                localpointer = diffSerial(doc, (DCSerial)oldT, (DCSerial)newT, elementBounds);
                break;
            case SERIAL_DATA:
                localpointer = diffSerialData(doc, (DCSerialData)oldT, (DCSerialData)newT, elementBounds);
                break;
            case SERIAL_FIELD:
                localpointer = diffSerialField(doc, (DCSerialField)oldT, (DCSerialField)newT, elementBounds);
                break;
            case SINCE:
                localpointer = diffSince(doc, (DCSince)oldT, (DCSince)newT, elementBounds);
                break;
            /**
             * Used for instances of {@link EndElementTree} representing the
             * start of an HTML element.
             */
            case START_ELEMENT:
                localpointer = diffStartElement(doc, (DCStartElement)oldT, (DCStartElement)newT, elementBounds);
                break;
            case END_ELEMENT:
                localpointer = diffEndElement(doc, (DCEndElement)oldT, (DCEndElement)newT, elementBounds);
                break;
            case UNKNOWN_BLOCK_TAG:
                localpointer = diffUnknownBlockTag(doc, (DCUnknownBlockTag)oldT, (DCUnknownBlockTag)newT, elementBounds);
                break;
            case UNKNOWN_INLINE_TAG:
                localpointer = diffUnknownInlineTag(doc, (DCUnknownInlineTag)oldT, (DCUnknownInlineTag)newT, elementBounds);
                break;
            case VALUE:
                localpointer = diffValue(doc, (DCValue)oldT, (DCValue)newT, elementBounds);
                break;
            case VERSION:
                localpointer = diffVersion(doc, (DCVersion)oldT, (DCVersion)newT, elementBounds);
                break;
            default:
                // handle special cases like field groups and enum constants
                if (oldT.getKind() == DocTree.Kind.OTHER) {
//                  if (oldT instanceof FieldGroupTree) {
//                      return diffFieldGroup((FieldGroupTree) oldT, (FieldGroupTree) newT, elementBounds);
//                  }
                    break;
                }
                String msg = "Diff not implemented: "
                        + ((com.sun.source.doctree.DocTree) oldT).getKind().toString()
                        + " " + oldT.getClass().getName();
                throw new AssertionError(msg);
        }

        return localpointer;
    }
    
    private int diffAttribute(DCDocComment doc, DCAttribute oldT, DCAttribute newT, int[] elementBounds) {
        return elementBounds[1];
    }
    
    private int diffDocComment(DCDocComment doc, DCDocComment oldT, DCDocComment newT, int[] elementBounds) {
        tokenSequence.move(elementBounds[0]);
        if (!tokenSequence.moveNext()) {
            return elementBounds[1];
        }
        while (tokenSequence.token().id() == JavaTokenId.WHITESPACE && tokenSequence.moveNext()) {
            // Skip whitespace
        }
        int localpointer = tokenSequence.offset() + 3; // copy the first characters of the javadoc comment /**;
//        int localpointer = getOldPos((DCTree)oldT.getFirstSentence().head, doc);
        copyTo(elementBounds[0], localpointer);
        if(oldT.firstSentence.isEmpty() && newT.firstSentence.nonEmpty()) {
            printer.newline();
            printer.toLeftMargin();
            printer.print(" * ");
        }
        localpointer = diffList(doc, oldT.firstSentence, newT.firstSentence, localpointer, Measure.TAGS);
        localpointer = diffList(doc, oldT.body, newT.body, localpointer, Measure.TAGS);
        if(oldT.tags.isEmpty()) {
            int commentEnd = commentEnd(doc);
            if(localpointer < commentEnd) {
                copyTo(localpointer, localpointer = commentEnd);
            }
        }
        localpointer = diffList(doc, oldT.tags, newT.tags, localpointer, Measure.TAGS);
//        localpointer = endPos(oldT.tags, doc);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }
    
    private int diffParam(DCDocComment doc, DCParam oldT, DCParam newT, int[] elementBounds) {
        int localpointer;
        if(oldT.isTypeParameter != newT.isTypeParameter) {
            if(oldT.isTypeParameter) {
                localpointer = getOldPos(oldT.name, doc);
                copyTo(elementBounds[0], localpointer - 1);
            } else {
                localpointer = getOldPos(oldT.name, doc);
                copyTo(elementBounds[0], localpointer);
                printer.print("<");
            }
        } else {
            localpointer = getOldPos(oldT.name, doc);
            copyTo(elementBounds[0], localpointer);
        }
        localpointer = diffDocTree(doc, oldT.name, newT.name, new int[] {localpointer, endPos(oldT.name, doc)});
        if(oldT.isTypeParameter) {
            localpointer++;
        }
        if(newT.isTypeParameter) {
            printer.print(">");
        }
        localpointer = diffList(doc, oldT.description, newT.description, localpointer, Measure.TAGS);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }
    
    private int diffReturn(DCDocComment doc, DCReturn oldT, DCReturn newT, int[] elementBounds) {
        int localpointer = oldT.description.isEmpty()? elementBounds[1] : getOldPos(oldT.description.head, doc);
        copyTo(elementBounds[0], localpointer);
        localpointer = diffList(doc, oldT.description, newT.description, localpointer, Measure.TAGS);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }
    
    private int diffIdentifier(DCDocComment doc, DCIdentifier oldT, DCIdentifier newT, int[] elementBounds) {
        if(oldT.name.equals(newT.name)) {
            copyTo(elementBounds[0], elementBounds[1]);
        } else {
            printer.print(newT.name);
        }
        return elementBounds[1];
    }
    
    private int diffLink(DCDocComment doc, DCLink oldT, DCLink newT, int[] elementBounds) {
        int localpointer = getOldPos(oldT.ref, doc);
        copyTo(elementBounds[0], localpointer);
        
        localpointer = diffDocTree(doc, oldT.ref, newT.ref, new int[] {localpointer, endPos(oldT.ref, doc)});
        localpointer = diffList(doc, oldT.label, newT.label, localpointer, Measure.TAGS);
        
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }
    
    private int diffSee(DCDocComment doc, DCSee oldT, DCSee newT, int[] elementBounds) {
        int localpointer;
        localpointer = getOldPos(oldT.reference.head, doc);
        copyTo(elementBounds[0], localpointer);
        localpointer = diffList(doc, oldT.reference, newT.reference, localpointer, Measure.DOCTREE);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }
    
    private int diffText(DCDocComment doc, DCText oldT, DCText newT, int[] elementBounds) {
        if(oldT.text.equals(newT.text)) {
            copyTo(elementBounds[0], elementBounds[1]);
        } else {
            printer.print(newT.text);
        }
        return elementBounds[1];
    }
    
    private int diffAuthor(DCDocComment doc, DCAuthor oldT, DCAuthor newT, int[] elementBounds) {
        int localpointer = oldT.name.isEmpty()? elementBounds[1] : getOldPos(oldT.name.head, doc);
        copyTo(elementBounds[0], localpointer);
        localpointer = diffList(doc, oldT.name, newT.name, localpointer, Measure.DOCTREE);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }

    private int diffComment(DCDocComment doc, DCComment oldT, DCComment newT, int[] elementBounds) {
        if(oldT.body.equals(newT.body)) {
            copyTo(elementBounds[0], elementBounds[1]);
        } else {
            printer.print(newT.body);
        }
        return elementBounds[1];
    }

    private int diffDeprecated(DCDocComment doc, DCDeprecated oldT, DCDeprecated newT, int[] elementBounds) {
        int localpointer = oldT.body.isEmpty()? elementBounds[1] : getOldPos(oldT.body.head, doc);
        copyTo(elementBounds[0], localpointer);
        localpointer = diffList(doc, oldT.body, newT.body, localpointer, Measure.DOCTREE);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }

    private int diffDocRoot(DCDocComment doc, DCDocRoot oldT, DCDocRoot newT, int[] elementBounds) {
        copyTo(elementBounds[0], elementBounds[1]);
        return elementBounds[1];
    }

    private int diffEntity(DCDocComment doc, DCEntity oldT, DCEntity newT, int[] elementBounds) {
        if(oldT.name.equals(newT.name)) {
            copyTo(elementBounds[0], elementBounds[1]);
        } else {
            printer.print(newT);
        }
        return elementBounds[1];
    }

    private int diffErroneous(DCDocComment doc, DCErroneous oldT, DCErroneous newT, int[] elementBounds) {
        if(oldT.body.equals(newT.body)) {
            copyTo(elementBounds[0], elementBounds[1]);
        } else {
            printer.print(newT.body);
        }
        return elementBounds[1];
    }

    private int diffThrows(DCDocComment doc, DCThrows oldT, DCThrows newT, int[] elementBounds) {
        int localpointer;
        localpointer = getOldPos(oldT.name, doc);
        copyTo(elementBounds[0], localpointer);
        int endPos = endPos(oldT.name, doc);
        localpointer = diffDocTree(doc, oldT.name, newT.name, new int[] {localpointer, endPos});
        if(localpointer < endPos) {
            copyTo(localpointer, localpointer = endPos);
        }
        localpointer = diffList(doc, oldT.description, newT.description, localpointer, Measure.TAGS);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }

    private int diffInheritDoc(DCDocComment doc, DCInheritDoc oldT, DCInheritDoc newT, int[] elementBounds) {
        copyTo(elementBounds[0], elementBounds[1]);
        return elementBounds[1];
    }

    private int diffLiteral(DCDocComment doc, DCLiteral oldT, DCLiteral newT, int[] elementBounds) {
        int localpointer;
        localpointer = getOldPos(oldT.body, doc);
        copyTo(elementBounds[0], localpointer);
        localpointer = diffDocTree(doc, oldT.body, newT.body, new int[] {localpointer, endPos(oldT.body, doc)});
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }

    private int diffReference(DCDocComment doc, DCReference oldT, DCReference newT, int[] elementBounds) {
        printer.print(newT);
        return elementBounds[1];
    }

    private int diffSerial(DCDocComment doc, DCSerial oldT, DCSerial newT, int[] elementBounds) {
        int localpointer = getOldPos(oldT.description.head, doc);
        copyTo(elementBounds[0], localpointer);
        localpointer = diffList(doc, oldT.description, newT.description, localpointer, Measure.TAGS);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }

    private int diffSerialData(DCDocComment doc, DCSerialData oldT, DCSerialData newT, int[] elementBounds) {
        int localpointer = getOldPos(oldT.description.head, doc);
        copyTo(elementBounds[0], localpointer);
        localpointer = diffList(doc, oldT.description, newT.description, localpointer, Measure.TAGS);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }

    private int diffSerialField(DCDocComment doc, DCSerialField oldT, DCSerialField newT, int[] elementBounds) {
        int localpointer;
        localpointer = getOldPos(oldT.name, doc);
        copyTo(elementBounds[0], localpointer);
        localpointer = diffDocTree(doc, oldT.name, newT.name, new int[] {localpointer, endPos(oldT.name, doc)});
        localpointer = diffDocTree(doc, oldT.type, newT.type, new int[] {localpointer, endPos(oldT.type, doc)});
        localpointer = diffList(doc, oldT.description, newT.description, localpointer, Measure.TAGS);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }

    private int diffSince(DCDocComment doc, DCSince oldT, DCSince newT, int[] elementBounds) {
        int localpointer = oldT.body.isEmpty()? elementBounds[1] : getOldPos(oldT.body.head, doc);
        copyTo(elementBounds[0], localpointer);
        localpointer = diffList(doc, oldT.body, newT.body, localpointer, Measure.DOCTREE);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }
    
    private int diffStartElement(DCDocComment doc, DCStartElement oldT, DCStartElement newT, int[] elementBounds) {
        int localpointer = oldT.attrs.isEmpty()? elementBounds[1] - 1 : getOldPos(oldT.attrs.head, doc);
        if(oldT.name.equals(newT.name)) {
            copyTo(elementBounds[0], localpointer);
        } else {
            printer.print("<");
            printer.print(newT.name);
        }
        localpointer = diffList(doc, oldT.attrs, newT.attrs, localpointer, Measure.DOCTREE);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }

    private int diffEndElement(DCDocComment doc, DCEndElement oldT, DCEndElement newT, int[] elementBounds) {
        if(oldT.name.equals(newT.name)) {
            copyTo(elementBounds[0], elementBounds[1]);
        } else {
            printer.print(newT);
        }
        return elementBounds[1];
    }

    private int diffUnknownBlockTag(DCDocComment doc, DCUnknownBlockTag oldT, DCUnknownBlockTag newT, int[] elementBounds) {
        int localpointer = oldT.content.isEmpty()? elementBounds[1] : getOldPos(oldT.content.head, doc);
        if(oldT.name.equals(newT.name)) {
            copyTo(elementBounds[0], localpointer);
        } else {
            printer.print("@"); //NOI18N
            printer.print(newT.name);
            printer.out.needSpace();
        }
        localpointer = diffList(doc, oldT.content, newT.content, localpointer, Measure.DOCTREE);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }

    private int diffUnknownInlineTag(DCDocComment doc, DCUnknownInlineTag oldT, DCUnknownInlineTag newT, int[] elementBounds) {
        int localpointer = oldT.content.isEmpty()? elementBounds[1] : getOldPos(oldT.content.head, doc);
        if(oldT.name.equals(newT.name)) {
            copyTo(elementBounds[0], localpointer);
        } else {
            printer.print("{@"); //NOI18N
            printer.print(newT.name);
            printer.out.needSpace();
        }
        localpointer = diffList(doc, oldT.content, newT.content, localpointer, Measure.DOCTREE);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }

    private int diffValue(DCDocComment doc, DCValue oldT, DCValue newT, int[] elementBounds) {
        int localpointer;
        localpointer = getOldPos(oldT.ref, doc);
        copyTo(elementBounds[0], localpointer);
        localpointer = diffDocTree(doc, oldT.ref, newT.ref, new int[] {localpointer, endPos(oldT.ref, doc)});
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }

    private int diffVersion(DCDocComment doc, DCVersion oldT, DCVersion newT, int[] elementBounds) {
        int localpointer;
        localpointer = oldT.body.head != null? getOldPos(oldT.body.head, doc) : elementBounds[1];
        copyTo(elementBounds[0], localpointer);
        localpointer = diffList(doc, oldT.body, newT.body, localpointer, Measure.DOCTREE);
        if(localpointer < elementBounds[1]) {
            copyTo(localpointer, elementBounds[1]);
        }
        return elementBounds[1];
    }
    
    private int diffList(
            DCDocComment doc,
            List<? extends DCTree> oldList,
            List<? extends DCTree> newList,
            int localPointer,
            Comparator<DCTree> measure)
    {
        assert oldList != null && newList != null;
        
        if (oldList == newList || oldList.equals(newList)) {
            return localPointer;
        }

        ListMatcher<DCTree> matcher = ListMatcher.<DCTree>instance(
                oldList,
                newList,
                measure
        );
        if (!matcher.match()) {
            return localPointer;
        }
        DCTree lastdel = null; // last deleted element
        ResultItem<DCTree>[] result = matcher.getResult();

        if (oldList.isEmpty() && !newList.isEmpty()) {
            // such a situation needs special handling. It is difficult to
            // obtain a correct position.
            StringBuilder aHead = new StringBuilder(), aTail = new StringBuilder();
//            int pos = estimator.prepare(localPointer, aHead, aTail);
//            copyTo(localPointer, pos, printer);
//
//            printer.print(aHead.toString());
            printer.out.needSpace();
            for (DCTree item : newList) {
//                if (LineInsertionType.BEFORE == estimator.lineInsertType()) printer.newline();
                printer.print(item);
//                if (LineInsertionType.AFTER == estimator.lineInsertType()) printer.newline();
            }
//            printer.print(aTail.toString());
            return localPointer;
        }

        // if there has been imports which is removed now
        if (newList.isEmpty() && !oldList.isEmpty()) {
            int oldPos = adjustToPreviousNewLine(getOldPos(oldList.get(0), doc), localPointer);
            copyTo(localPointer, oldPos);
            return endPos(oldList, doc);
        }

        // copy to start position
        int insertPos = adjustToPreviousNewLine(getOldPos(oldList.get(0), doc), localPointer);
        if (insertPos > localPointer) {
            copyTo(localPointer, localPointer = insertPos);
        }
        // go on, match it!
        int i = 0;
        for (int j = 0; j < result.length; j++) {
            ResultItem<DCTree> item = result[j];
            switch (item.operation) {
                case MODIFY: {
                    DCTree oldT = oldList.get(i);
                    int[] pos = getBounds(oldT, doc);
                    copyTo(localPointer, pos[0]);
                    localPointer = diffDocTree(doc, oldT, item.element, pos);
                    ++i;
                    break;
                }
                case INSERT: {
                    int oldPos = item.element.pos;
                    boolean found = false;
                    if (oldPos > 0) {
                        for (DCTree oldT : oldList) {
                            int oldNodePos = oldT.pos;
                            if (oldPos == oldNodePos) {
                                found = true;
                                VeryPretty oldPrinter = this.printer;
                                int old = oldPrinter.indent();
                                this.printer = new VeryPretty(diffContext, diffContext.style, tree2Tag, tree2Doc, tag2Span, origText, oldPrinter.toString().length() + oldPrinter.getInitialOffset());//XXX
                                this.printer.reset(old);
                                this.printer.oldTrees = oldTrees;
                                int[] poss = getBounds(oldT, doc);
                                int end = diffDocTree(doc, oldT, item.element, poss);
                                copyTo(end, poss[1]);
                                printer.print(this.printer.toString()); //XXX: this appears to copy this.printer's content into the same printer?
                                printer.reindentRegions.addAll(this.printer.reindentRegions);
                                this.printer = oldPrinter;
                                this.printer.undent(old);
                                break;
                            }
                        }
                    }
                    if (!found) {
//                        if (lastdel != null) {
//                            if(treesMatch(item.element, lastdel, false)) {
//                                VeryPretty oldPrinter = this.printer;
//                                int old = oldPrinter.indent();
//                                this.printer = new VeryPretty(diffContext, diffContext.style, tree2Tag, tag2Span, origText, oldPrinter.toString().length() + oldPrinter.getInitialOffset());//XXX
//                                this.printer.reset(old);
//                                this.printer.oldTrees = oldTrees;
//                                int index = oldList.indexOf(lastdel);
//                                int[] poss = estimator.getPositions(index);
//                                //TODO: should the original text between the return position of the following method and poss[1] be copied into the new text?
//                                localPointer = diffTree(lastdel, item.element, poss);
//                                printer.print(this.printer.toString());
//                                this.printer = oldPrinter;
//                                this.printer.undent(old);
//                                lastdel = null;
//                                break;
//                            }
//                        }
                        printer.print(item.element);
                    }
                    break;
                }
                case DELETE: {
                    DCTree oldT = oldList.get(i);
                    lastdel = oldT;
                    int[] pos = getBounds(oldT, doc);
//                    if (localPointer < pos[0] && lastdel == null) {
//                        copyTo(localPointer, pos[0], printer);
//                    }
                    localPointer = pos[1];
                    ++i;
                    break;
                }
                case NOCHANGE: {
                    DCTree oldT = oldList.get(i);
                    int[] pos = getBounds(oldT, doc);
//                    if (pos[0] > localPointer && i != 0) {
//                        // print fill-in
//                        copyTo(localPointer, pos[0], printer);
//                    }
//                    if (pos[0] >= localPointer) {
//                        localPointer = pos[0];
//                    }
                    if(needStar(pos[0])) {
                        printer.print(" * ");
                    }
                    copyTo(localPointer, localPointer = pos[1], printer);
                    lastdel = null;
                    ++i;
                    break;
                }
            }
        }
        return localPointer;
    }

    private Comment safeNext(Iterator<Comment> iter) {
        return iter.hasNext() ? iter.next() : null;
    }

    private boolean commentsMatch(Comment oldC, Comment newC) {
        if (oldC == null && newC == null)
            return true;
        if (oldC == null || newC == null)
            return false;
        return oldC.equals(newC);
    }

    private boolean listContains(List<Comment>list, Comment comment) {
        for (Comment c : list)
            if (c.equals(comment))
                return true;
        return false;
    }
    
    private int commentStartCorrect(Comment c) {
        tokenSequence.move(c.pos());

        boolean wasPrevious = false;

        while (tokenSequence.movePrevious()) {
            if (tokenSequence.token().id() != JavaTokenId.WHITESPACE) {
                return tokenSequence.offset() + tokenSequence.token().length();
            }

            int lastNewLine = tokenSequence.token().text().toString().lastIndexOf('\n');

            if (lastNewLine != (-1)) {
                return tokenSequence.offset() + lastNewLine + 1;
            }

            wasPrevious = true;
        }

        if (wasPrevious)
            return tokenSequence.offset();
        else
            return c.pos();
    }

    public static int commentStart(DiffContext diffContext, CommentSet comments, CommentSet.RelativePosition pos, int limit) {
        List<Comment> list = comments.getComments(pos);

        if (list.isEmpty()) {
            return Integer.MAX_VALUE;
        } else {
            diffContext.tokenSequence.move(limit);
            moveToSrcRelevant(diffContext.tokenSequence, Direction.BACKWARD);
            limit = diffContext.tokenSequence.offset() + diffContext.tokenSequence.token().length();
            int start = Integer.MAX_VALUE;
            for (Comment c : list) {
                if (c.pos() >= limit) start = Math.min(start, c.pos());
            }
            return start;
        }
    }

    public static int commentEnd(CommentSet comments, CommentSet.RelativePosition pos) {
        List<Comment> list = comments.getComments(pos);

        if (list.isEmpty()) {
            return -1;
        } else {
            return list.get(list.size() - 1).endPos();
        }
    }
    
    private static int commentEnd(DCDocComment doc) {
        int length = doc.comment.getText().length();
        return doc.comment.getSourcePos(length-1);
    }

    private static int getOldPos(JCTree oldT) {
        return TreeInfo.getStartPos(oldT);
    }
    
    private int getOldPos(DCTree oldT, DCDocComment doc) {
        return (int) oldT.getSourcePosition(doc);
    }
    
    public int endPos(DCTree oldT, DCDocComment doc) {
        DocSourcePositions sp = JavacTrees.instance(context).getSourcePositions();
        return (int) sp.getEndPosition(null, doc, oldT);
    }
    
    private int endPos(List<? extends DCTree> trees, DCDocComment doc) {
        if (trees.isEmpty())
            return -1;
        return endPos(trees.get(trees.size()-1), doc);
    }

    /**
     * Create differences between trees. Old tree has to exist, i.e.
     * <code>oldT != null</code>. There is a one exception - when both
     * <code>oldT</code> and <code>newT</code> are null, then method
     * just returns.
     *
     * @param  oldT  original tree in source code
     * @param  newT  tree to replace the original tree
     * @return position in original source
     */
    protected int diffTree(JCTree oldT, JCTree newT, int[] elementBounds) {
        return diffTree(oldT, newT, null, elementBounds);
    }

    protected int diffTree(JCTree oldT, JCTree newT, JCTree parent /*used only for modifiers*/, int[] elementBounds) {
        Object t = tree2Tag.get(newT);
        int result;
        if (t != null) {
            int start = printer.toString().length();
            result = diffTreeImpl(oldT, newT, parent, elementBounds);
            int end = printer.toString().length();
            tag2Span.put(t, new int[]{start + printer.getInitialOffset(), end + printer.getInitialOffset()});
        } else {
            result = diffTreeImpl(oldT, newT, parent, elementBounds);
        }
        return result;
    }

    protected int diffTreeImpl(JCTree oldT, JCTree newT, JCTree parent /*used only for modifiers*/, int[] elementBounds) {
        if (oldT == null && newT != null)
            throw new IllegalArgumentException("Null is not allowed in parameters.");

        if (oldT == newT)
            return elementBounds[0];

        if (newT == null) {
            tokenSequence.move(elementBounds[1]);
            if (!tokenSequence.moveNext()) {
                return elementBounds[1];
            }
            while (tokenSequence.token().id() == JavaTokenId.WHITESPACE && tokenSequence.moveNext())
                ;
            return tokenSequence.offset();
        }

        if (printer.handlePossibleOldTrees(Collections.singletonList(newT), true)) {
            return getCommentCorrectedEndPos(oldT);
        }

        elementBounds[0] = diffPrecedingComments(oldT, newT, elementBounds[0]);
        int retVal = -1;

        if (oldT.getTag() != newT.getTag()) {
            if (((compAssign.contains(oldT.getKind()) && compAssign.contains(newT.getKind())) == false) &&
                ((binaries.contains(oldT.getKind()) && binaries.contains(newT.getKind())) == false) &&
                ((unaries.contains(oldT.getKind()) && unaries.contains(newT.getKind())) == false)) {
                // different kind of trees found, print the whole new one.
                int[] oldBounds = getBounds(oldT);
                if (oldBounds[0] > elementBounds[0]) {
                    copyTo(elementBounds[0], oldBounds[0]);
                }
                    printer.print(newT);
                return oldBounds[1];
            }
        }
        
        int commentsStart = Math.min(commentStart(diffContext, comments.getComments(oldT), CommentSet.RelativePosition.INLINE, endPos(oldT)), commentStart(diffContext, comments.getComments(oldT), CommentSet.RelativePosition.TRAILING, endPos(oldT)));
        if (commentsStart < elementBounds[1]) {
            int lastIndex;
            tokenSequence.move(commentsStart);
            elementBounds = Arrays.copyOf(elementBounds, elementBounds.length);
            elementBounds[1] = tokenSequence.movePrevious() && tokenSequence.token().id() == JavaTokenId.WHITESPACE &&
                    (lastIndex = tokenSequence.token().text().toString().lastIndexOf('\n')) > -1 ?
                    tokenSequence.offset() + lastIndex + 1 : commentsStart;
        }

        switch (oldT.getTag()) {
          case TOPLEVEL:
              diffTopLevel((JCCompilationUnit)oldT, (JCCompilationUnit)newT);
              break;
          case IMPORT:
              retVal = diffImport((JCImport)oldT, (JCImport)newT, elementBounds);
              break;
          case CLASSDEF:
              retVal = diffClassDef((JCClassDecl)oldT, (JCClassDecl)newT, elementBounds);
              break;
          case METHODDEF:
              retVal = diffMethodDef((JCMethodDecl)oldT, (JCMethodDecl)newT, elementBounds);
              break;
          case VARDEF:
              retVal = diffVarDef((JCVariableDecl)oldT, (JCVariableDecl)newT, elementBounds);
              break;
          case SKIP:
              copyTo(elementBounds[0], elementBounds[1]);
              retVal = elementBounds[1];
              break;
          case BLOCK:
              retVal = diffBlock((JCBlock)oldT, (JCBlock)newT, elementBounds);
              break;
          case DOLOOP:
              retVal = diffDoLoop((JCDoWhileLoop)oldT, (JCDoWhileLoop)newT, elementBounds);
              break;
          case WHILELOOP:
              retVal = diffWhileLoop((JCWhileLoop)oldT, (JCWhileLoop)newT, elementBounds);
              break;
          case FORLOOP:
              retVal = diffForLoop((JCForLoop)oldT, (JCForLoop)newT, elementBounds);
              break;
          case FOREACHLOOP:
              retVal = diffForeachLoop((JCEnhancedForLoop)oldT, (JCEnhancedForLoop)newT, elementBounds);
              break;
          case LABELLED:
              retVal = diffLabelled((JCLabeledStatement)oldT, (JCLabeledStatement)newT, elementBounds);
              break;
          case SWITCH:
              retVal = diffSwitch((JCSwitch)oldT, (JCSwitch)newT, elementBounds);
              break;
          case CASE:
              retVal = diffCase((JCCase)oldT, (JCCase)newT, elementBounds);
              break;
          case SYNCHRONIZED:
              retVal = diffSynchronized((JCSynchronized)oldT, (JCSynchronized)newT, elementBounds);
              break;
          case TRY:
              retVal = diffTry((JCTry)oldT, (JCTry)newT, elementBounds);
              break;
          case CATCH:
              retVal = diffCatch((JCCatch)oldT, (JCCatch)newT, elementBounds);
              break;
          case CONDEXPR:
              retVal = diffConditional((JCConditional)oldT, (JCConditional)newT, elementBounds);
              break;
          case IF:
              retVal = diffIf((JCIf)oldT, (JCIf)newT, elementBounds);
              break;
          case EXEC:
              retVal = diffExec((JCExpressionStatement)oldT, (JCExpressionStatement)newT, elementBounds);
              break;
          case BREAK:
              retVal = diffBreak((JCBreak)oldT, (JCBreak)newT, elementBounds);
              break;
          case CONTINUE:
              retVal = diffContinue((JCContinue)oldT, (JCContinue)newT, elementBounds);
              break;
          case RETURN:
              retVal = diffReturn((JCReturn)oldT, (JCReturn)newT, elementBounds);
              break;
          case THROW:
              retVal = diffThrow((JCThrow)oldT, (JCThrow)newT,elementBounds);
              break;
          case ASSERT:
              retVal = diffAssert((JCAssert)oldT, (JCAssert)newT, elementBounds);
              break;
          case APPLY:
              retVal = diffApply((JCMethodInvocation)oldT, (JCMethodInvocation)newT, elementBounds);
              break;
          case NEWCLASS:
              retVal = diffNewClass((JCNewClass)oldT, (JCNewClass)newT, elementBounds);
              break;
          case NEWARRAY:
              retVal = diffNewArray((JCNewArray)oldT, (JCNewArray)newT, elementBounds);
              break;
          case PARENS:
              retVal = diffParens((JCParens)oldT, (JCParens)newT, elementBounds);
              break;
          case ASSIGN:
              retVal = diffAssign((JCAssign)oldT, (JCAssign)newT, parent, elementBounds);
              break;
          case TYPECAST:
              retVal = diffTypeCast((JCTypeCast)oldT, (JCTypeCast)newT, elementBounds);
              break;
          case TYPETEST:
              retVal = diffTypeTest((JCInstanceOf)oldT, (JCInstanceOf)newT, elementBounds);
              break;
          case INDEXED:
              retVal = diffIndexed((JCArrayAccess)oldT, (JCArrayAccess)newT, elementBounds);
              break;
          case SELECT:
              retVal = diffSelect((JCFieldAccess)oldT, (JCFieldAccess)newT, elementBounds);
              break;
          case IDENT:
              retVal = diffIdent((JCIdent)oldT, (JCIdent)newT, elementBounds);
              break;
          case LITERAL:
              retVal = diffLiteral((JCLiteral)oldT, (JCLiteral)newT, elementBounds);
              break;
          case TYPEIDENT:
              retVal = diffTypeIdent((JCPrimitiveTypeTree)oldT, (JCPrimitiveTypeTree)newT, elementBounds);
              break;
          case TYPEARRAY:
              retVal = diffTypeArray((JCArrayTypeTree)oldT, (JCArrayTypeTree)newT, elementBounds);
              break;
          case TYPEAPPLY:
              retVal = diffTypeApply((JCTypeApply)oldT, (JCTypeApply)newT, elementBounds);
              break;
          case TYPEPARAMETER:
              retVal = diffTypeParameter((JCTypeParameter)oldT, (JCTypeParameter)newT, elementBounds);
              break;
          case WILDCARD:
              retVal = diffWildcard((JCWildcard)oldT, (JCWildcard)newT, elementBounds);
              break;
          case TYPEBOUNDKIND:
              retVal = diffTypeBoundKind((TypeBoundKind)oldT, (TypeBoundKind)newT, elementBounds);
              break;
          case ANNOTATION: case TYPE_ANNOTATION:
              retVal = diffAnnotation((JCAnnotation)oldT, (JCAnnotation)newT, elementBounds);
              break;
          case LETEXPR:
              diffLetExpr((LetExpr)oldT, (LetExpr)newT);
              break;
          case POS:
          case NEG:
          case NOT:
          case COMPL:
          case PREINC:
          case PREDEC:
          case POSTINC:
          case POSTDEC:
          case NULLCHK:
              retVal = diffUnary((JCUnary)oldT, (JCUnary)newT, elementBounds);
              break;
          case OR:
          case AND:
          case BITOR:
          case BITXOR:
          case BITAND:
          case EQ:
          case NE:
          case LT:
          case GT:
          case LE:
          case GE:
          case SL:
          case SR:
          case USR:
          case PLUS:
          case MINUS:
          case MUL:
          case DIV:
          case MOD:
              retVal = diffBinary((JCBinary)oldT, (JCBinary)newT, elementBounds);
              break;
          case BITOR_ASG:
          case BITXOR_ASG:
          case BITAND_ASG:
          case SL_ASG:
          case SR_ASG:
          case USR_ASG:
          case PLUS_ASG:
          case MINUS_ASG:
          case MUL_ASG:
          case DIV_ASG:
          case MOD_ASG:
              retVal = diffAssignop((JCAssignOp)oldT, (JCAssignOp)newT, elementBounds);
              break;
          case ERRONEOUS:
              diffErroneous((JCErroneous)oldT, (JCErroneous)newT, elementBounds);
              break;
          case MODIFIERS:
              retVal = diffModifiers((JCModifiers) oldT, (JCModifiers) newT, parent, elementBounds[0]);
              copyTo(retVal, elementBounds[1]);
              break;
          case TYPEUNION:
              retVal = diffUnionType((JCTypeUnion) oldT, (JCTypeUnion) newT, elementBounds);
              break;
          case LAMBDA:
              retVal = diffLambda((JCLambda) oldT, (JCLambda) newT, elementBounds);
              break;
          case REFERENCE:
              retVal = diffMemberReference((JCMemberReference) oldT, (JCMemberReference) newT, elementBounds);
              break;
          case ANNOTATED_TYPE:
              retVal = diffAnnotatedType((JCAnnotatedType)oldT, (JCAnnotatedType)newT, elementBounds);
              break;
          default:
              // handle special cases like field groups and enum constants
              if (oldT.getKind() == Kind.OTHER) {
                  if (oldT instanceof FieldGroupTree) {
                      return diffFieldGroup((FieldGroupTree) oldT, (FieldGroupTree) newT, elementBounds);
                  }
                  break;
              }
              String msg = "Diff not implemented: " +
                  ((com.sun.source.tree.Tree)oldT).getKind().toString() +
                  " " + oldT.getClass().getName();
              throw new AssertionError(msg);
        }
        return diffTrailingComments(oldT, newT, retVal);
    }

    /**
     * Three sets representing different kind which can be matched. No need
     * to rewrite whole expression. Ensure that CompoundAssignementTrees,
     * UnaryTrees and BinaryTrees are matched, i.e. diff method is used
     * instead of priting whole new tree.
     */
    private static final EnumSet<Kind> compAssign = EnumSet.of(
        Kind.MULTIPLY_ASSIGNMENT,
        Kind.DIVIDE_ASSIGNMENT,
        Kind.REMAINDER_ASSIGNMENT,
        Kind.PLUS_ASSIGNMENT,
        Kind.MINUS_ASSIGNMENT,
        Kind.LEFT_SHIFT_ASSIGNMENT,
        Kind.RIGHT_SHIFT_ASSIGNMENT,
        Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT,
        Kind.AND_ASSIGNMENT,
        Kind.XOR_ASSIGNMENT,
        Kind.OR_ASSIGNMENT
    );

    private static final EnumSet<Kind> binaries = EnumSet.of(
        Kind.MULTIPLY,
        Kind.DIVIDE,
        Kind.REMAINDER,
        Kind.PLUS,
        Kind.MINUS,
        Kind.LEFT_SHIFT,
        Kind.RIGHT_SHIFT,
        Kind.UNSIGNED_RIGHT_SHIFT,
        Kind.LESS_THAN,
        Kind.GREATER_THAN,
        Kind.LESS_THAN_EQUAL,
        Kind.GREATER_THAN_EQUAL,
        Kind.EQUAL_TO,
        Kind.NOT_EQUAL_TO,
        Kind.AND,
        Kind.XOR,
        Kind.OR,
        Kind.CONDITIONAL_AND,
        Kind.CONDITIONAL_OR
    );

    private static final EnumSet<Kind> unaries = EnumSet.of(
        Kind.POSTFIX_INCREMENT,
        Kind.POSTFIX_DECREMENT,
        Kind.PREFIX_INCREMENT,
        Kind.PREFIX_DECREMENT,
        Kind.UNARY_PLUS,
        Kind.UNARY_MINUS,
        Kind.BITWISE_COMPLEMENT,
        Kind.LOGICAL_COMPLEMENT
    );

    protected boolean listsMatch(List<? extends JCTree> oldList, List<? extends JCTree> newList) {
        if (oldList == newList)
            return true;
        int n = oldList.size();
        if (newList.size() != n)
            return false;
        for (int i = 0; i < n; i++)
            if (!treesMatch(oldList.get(i), newList.get(i)))
                return false;
        return true;
    }

    private boolean matchImport(JCImport t1, JCImport t2) {
        return t1.staticImport == t2.staticImport && treesMatch(t1.qualid, t2.qualid);
    }

    private boolean matchBlock(JCBlock t1, JCBlock t2) {
        return t1.flags == t2.flags && listsMatch(t1.stats, t2.stats);
    }

    private boolean matchDoLoop(JCDoWhileLoop t1, JCDoWhileLoop t2) {
        return treesMatch(t1.cond, t2.cond) && treesMatch(t1.body, t2.body);
    }

    private boolean matchWhileLoop(JCWhileLoop t1, JCWhileLoop t2) {
        return treesMatch(t1.cond, t2.cond) && treesMatch(t1.body, t2.body);
    }

    private boolean matchForLoop(JCForLoop t1, JCForLoop t2) {
        return listsMatch(t1.init, t2.init) && treesMatch(t1.cond, t2.cond) &&
               listsMatch(t1.step, t2.step) && treesMatch(t1.body, t2.body);
    }

    private boolean matchForeachLoop(JCEnhancedForLoop t1, JCEnhancedForLoop t2) {
        return treesMatch(t1.var, t2.var) && treesMatch(t1.expr, t2.expr) &&
               treesMatch(t1.body, t2.body);
    }

    private boolean matchLabelled(JCLabeledStatement t1, JCLabeledStatement t2) {
        return t1.label == t2.label && treesMatch(t1.body, t2.body);
    }

    private boolean matchSwitch(JCSwitch t1, JCSwitch t2) {
        return treesMatch(t1.selector, t2.selector) && listsMatch(t1.cases, t2.cases);
    }

    private boolean matchCase(JCCase t1, JCCase t2) {
        return treesMatch(t1.pat, t2.pat) && listsMatch(t1.stats, t2.stats);
    }

    private boolean matchSynchronized(JCSynchronized t1, JCSynchronized t2) {
        return treesMatch(t1.lock, t2.lock) && treesMatch(t1.body, t2.body);
    }

    private boolean matchTry(JCTry t1, JCTry t2) {
        return treesMatch(t1.finalizer, t2.finalizer) &&
                listsMatch(t1.catchers, t2.catchers) &&
                treesMatch(t1.body, t2.body);
    }

    private boolean matchCatch(JCCatch t1, JCCatch t2) {
        return treesMatch(t1.param, t2.param) && treesMatch(t1.body, t2.body);
    }

    private boolean matchConditional(JCConditional t1, JCConditional t2) {
        return treesMatch(t1.cond, t2.cond) && treesMatch(t1.truepart, t2.truepart) &&
               treesMatch(t1.falsepart, t2.falsepart);
    }

    private boolean matchIf(JCIf t1, JCIf t2) {
        return treesMatch(t1.cond, t2.cond) && treesMatch(t1.thenpart, t2.thenpart) &&
               treesMatch(t1.elsepart, t2.elsepart);
    }

    private boolean matchBreak(JCBreak t1, JCBreak t2) {
        return t1.label == t2.label && treesMatch(t1.target, t2.target);
    }

    private boolean matchContinue(JCContinue t1, JCContinue t2) {
        return t1.label == t2.label && treesMatch(t1.target, t2.target);
    }

    private boolean matchAssert(JCAssert t1, JCAssert t2) {
        return treesMatch(t1.cond, t2.cond) && treesMatch(t1.detail, t2.detail);
    }

    private boolean matchApply(JCMethodInvocation t1, JCMethodInvocation t2) {
        return t1.varargsElement == t2.varargsElement &&
               listsMatch(t1.typeargs, t2.typeargs) &&
               treesMatch(t1.meth, t2.meth) &&
               listsMatch(t1.args, t2.args);
    }

    private boolean matchNewClass(JCNewClass t1, JCNewClass t2) {
        return t1.constructor == t2.constructor &&
               treesMatch(t1.getIdentifier(), t2.getIdentifier()) &&
               listsMatch(t1.typeargs, t2.typeargs) &&
               listsMatch(t1.args, t2.args) &&
               (t1.varargsElement == t2.varargsElement) &&
               treesMatch(t1.def, t2.def);
    }

    private boolean matchNewArray(JCNewArray t1, JCNewArray t2) {
        return treesMatch(t1.elemtype, t2.elemtype) &&
               listsMatch(t1.dims, t2.dims) && listsMatch(t1.elems, t2.elems);
    }

    private boolean matchAssign(JCAssign t1, JCAssign t2) {
        return treesMatch(t1.lhs, t2.lhs) && treesMatch(t1.rhs, t2.rhs);
    }

    private boolean matchAssignop(JCAssignOp t1, JCAssignOp t2) {
        return t1.operator == t2.operator &&
               treesMatch(t1.lhs, t2.lhs) && treesMatch(t1.rhs, t2.rhs);
    }

    private boolean matchUnary(JCUnary t1, JCUnary t2) {
        return t1.operator == t2.operator && treesMatch(t1.arg, t2.arg);
    }

    private boolean matchBinary(JCBinary t1, JCBinary t2) {
        return t1.operator == t2.operator &&
               treesMatch(t1.lhs, t2.lhs) && treesMatch(t1.rhs, t2.rhs);
    }

    private boolean matchTypeCast(JCTypeCast t1, JCTypeCast t2) {
        return treesMatch(t1.clazz, t2.clazz) && treesMatch(t1.expr, t2.expr);
    }

    private boolean matchTypeTest(JCInstanceOf t1, JCInstanceOf t2) {
        return treesMatch(t1.clazz, t2.clazz) && treesMatch(t1.expr, t2.expr);
    }

    private boolean matchIndexed(JCArrayAccess t1, JCArrayAccess t2) {
        return treesMatch(t1.indexed, t2.indexed) && treesMatch(t1.index, t2.index);
    }

    private boolean matchSelect(JCFieldAccess t1, JCFieldAccess t2) {
        return treesMatch(t1.selected, t2.selected) && t1.name == t2.name;
    }

    private boolean matchLiteral(JCLiteral t1, JCLiteral t2) {
        return t1.typetag == t2.typetag && (t1.value == t2.value || (t1.value != null && t1.value.equals(t2.value)));
    }

    private boolean matchTypeApply(JCTypeApply t1, JCTypeApply t2) {
        return treesMatch(t1.clazz, t2.clazz) &&
               listsMatch(t1.arguments, t2.arguments);
    }
    
    private boolean matchAnnotatedType(JCAnnotatedType t1, JCAnnotatedType t2) {
        return treesMatch(t1.underlyingType, t2.underlyingType) &&
               listsMatch(t1.annotations, t2.annotations);
    }

    private boolean matchTypeParameter(JCTypeParameter t1, JCTypeParameter t2) {
        return t1.name == t2.name && listsMatch(t1.bounds, t2.bounds);
    }

    private boolean matchWildcard(JCWildcard t1, JCWildcard t2) {
        return t1.kind == t2.kind && treesMatch(t1.inner, t2.inner);
    }

    private boolean matchAnnotation(JCAnnotation t1, JCAnnotation t2) {
        return treesMatch(t1.annotationType, t2.annotationType) &&
               listsMatch(t1.args, t2.args);
    }

    private boolean matchModifiers(JCModifiers t1, JCModifiers t2) {
        return t1.flags == t2.flags && listsMatch(t1.annotations, t2.annotations);
    }

    private boolean matchLetExpr(LetExpr t1, LetExpr t2) {
        return listsMatch(t1.defs, t2.defs) && treesMatch(t1.expr, t2.expr);
    }

    private boolean matchLambda(JCLambda t1, JCLambda t2) {
        return listsMatch(t1.params, t2.params) && treesMatch(t1.body, t2.body);
    }
    
    private boolean isCommaSeparated(JCVariableDecl oldT) {
        if (getOldPos(oldT) <= 0 || oldT.pos <= 0) {
            return false;
        }
        tokenSequence.move(oldT.pos);
        moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
        if (tokenSequence.token() == null) {
            return false;
        }
        if (JavaTokenId.COMMA == tokenSequence.token().id()) {
            return true;
        }
        if (oldT.getInitializer() != null && (oldT.mods.flags & Flags.ENUM) == 0) {
            tokenSequence.move(endPos(oldT.getInitializer()));
        } else {
            tokenSequence.move(oldT.pos);
            tokenSequence.moveNext();
        }
        moveToSrcRelevant(tokenSequence, Direction.FORWARD);
        if (tokenSequence.token() == null) {
            return false;
        }
        if (JavaTokenId.COMMA == tokenSequence.token().id()) {
            return true;
        }
        return false;
    }

    private int getCommentCorrectedOldPos(JCTree tree) {
        CommentSet ch = comments.getComments(tree);
        return Math.min(getOldPos(tree), commentStart(diffContext, ch, CommentSet.RelativePosition.PRECEDING, getOldPos(tree)));
    }

    private int getCommentCorrectedEndPos(JCTree tree) {
        final int[] res = new int[] {endPos(tree)};
        new TreeScanner<Void, Void>() {
            @Override public Void scan(Tree node, Void p) {
                if (node != null) {
                    CommentSet ch = comments.getComments(node);
                    res[0] = Math.max(res[0], Math.max(commentEnd(ch, CommentSet.RelativePosition.INLINE), commentEnd(ch, CommentSet.RelativePosition.TRAILING)));
                }
                return super.scan(node, p);
            }
        }.scan(tree, null);
        return res[0];
    }

    private int[] getCommentCorrectedBounds(JCTree tree) {
        return new int[] {
            getCommentCorrectedOldPos(tree),
            getCommentCorrectedEndPos(tree)
        };
    }

    private int[] getBounds(JCTree tree) {
        return new int[] { getOldPos(tree), endPos(tree) };
    }
    
    private int[] getBounds(DCTree tree, DCDocComment doc) {
        return new int[] { getOldPos(tree, doc), endPos(tree, doc) };
    }

    private void copyTo(int from, int to) {
        copyTo(from, to, printer);
    }

    public static boolean noInvalidCopyTos = false;
    
    public void copyTo(int from, int to, VeryPretty loc) {
        if (from == to) {
            return;
        } else if (from > to || from < 0 || to < 0) {
            // #104107 - log the source when this problem occurs.
            LOG.log(INFO, "-----\n" + origText + "-----\n");
            LOG.log(INFO, "Illegal values: from = " + from + "; to = " + to + "." +
                "Please, attach your messages.log to new issue!");
            if (noInvalidCopyTos)
                throw new IllegalStateException("Illegal values: from = " + from + "; to = " + to + ".");
            if (to >= 0)
                printer.eatChars(from-to);
            return;
        } else if (to > origText.length()) {
            // #99333, #97801: Debug message for the issues.
            LOG.severe("-----\n" + origText + "-----\n");
            throw new IllegalArgumentException("Copying to " + to + " is greater then its size (" + origText.length() + ").");
        }
        loc.print(origText.substring(from, to));
    }

    // temporary method
    private int diffTree(JCTree oldT, JCTree newT, int[] elementBounds, Kind parentKind) {
        if (oldT.getKind() != newT.getKind() && newT.getKind() == Kind.BLOCK) {
            tokenSequence.move(getOldPos(oldT));
            moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
            tokenSequence.moveNext();
            if (elementBounds[0] < tokenSequence.offset())
                copyTo(elementBounds[0], tokenSequence.offset());
            printer.printBlock(oldT, newT, parentKind);
            return getCommentCorrectedEndPos(oldT);
        } else {
            // next statement can to seem redundant, but is not, see 117774
            copyTo(elementBounds[0], elementBounds[0] = getBounds(oldT)[0]);
            return diffTree(oldT, newT, elementBounds);
        }
    }

    // ---- TreeDiff inner classes - need refactoring.

    public static enum DiffTypes {
        /**
         * The tree has been modified; that is, different versions
         * of it exist in the old and new parent trees.
         */
        MODIFY("modify"),

        /**
         * The tree is an insertion; that is, it exists in the
         * new tree, but not the old.
         */
        INSERT("insert"),

        /**
         * The tree was deleted; which means that it exists in the
         * old parent tree, but not the new one.
         */
        DELETE("delete");

        DiffTypes(String name) {
            this.name = name;
        }
        public final String name;
    }

    public static enum LineInsertionType {
        BEFORE, AFTER, NONE
    }

    public static class Diff {
        public DiffTypes type;
        int pos;
        int endOffset;
        protected JCTree oldTree;
        protected JCTree newTree;
        protected Comment oldComment;
        protected Comment newComment;
        private String text;
        boolean trailing;

        public static Diff insert(int pos, String text) {
            return new Diff(DiffTypes.INSERT, pos, Position.NOPOS /* does not matter */, text);
        }

        public static Diff delete(int startOffset, int endOffset) {
            return new Diff(DiffTypes.DELETE, startOffset, endOffset, null);
        }

        Diff(DiffTypes type, int pos, int endOffset, String text) {
            this.type = type;
            this.pos = pos;
            this.endOffset = endOffset;
            this.text = text;
        }

        Diff(DiffTypes type, int pos, JCTree oldTree, JCTree newTree,
             Comment oldComment, Comment newComment, boolean trailing) {
            this(type, pos, -1, null);
            assert pos >= 0 : "invalid source offset";
            this.oldTree = oldTree;
            this.newTree = newTree;
            this.oldComment = oldComment;
            this.newComment = newComment;
            this.trailing = trailing;
        }

        public JCTree getOld() {
            return oldTree;
        }

        public JCTree getNew() {
            return newTree;
        }

        public int getPos() {
            return pos;
        }

        public int getEnd() {
            return endOffset;
        }

        public String getText() {
            return text;
        }
        public Comment getOldComment() {
            return oldComment;
        }

        public Comment getNewComment() {
            return newComment;
        }

        public boolean isTrailingComment() {
            return trailing;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Diff))
                return false;
            Diff d2 = (Diff)obj;
            return type != d2.type &&
                   pos != d2.pos &&
                   oldTree != d2.oldTree &&
                   newTree != d2.newTree &&
                   oldComment != d2.oldComment &&
                   newComment != d2.newComment &&
                   trailing != d2.trailing;
        }

        @Override
        public int hashCode() {
            return type.hashCode() + pos +
                   (oldTree != null ? oldTree.hashCode() : 0) +
                   (newTree != null ? newTree.hashCode() : 0) +
                   (oldComment != null ? oldComment.hashCode() : 0) +
                   (newComment != null ? newComment.hashCode() : 0) +
                   Boolean.valueOf(trailing).hashCode();
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("tree (");
            sb.append(type.toString());
            sb.append(") pos=");
            sb.append(pos);
            if (trailing)
                sb.append(" trailing comment");
            sb.append("\n");

            if (type == DiffTypes.DELETE || type == DiffTypes.INSERT || type == DiffTypes.MODIFY)
                addDiffString(sb, oldTree, newTree);
            else
                addDiffString(sb, oldComment, newComment);
            return sb.toString();
        }

        private void addDiffString(StringBuffer sb, Object o1, Object o2) {
            if (o1 != null) {
                sb.append("< ");
                sb.append(o1.toString());
                sb.append((o2 != null) ? "\n---\n> " : "\n");
            } else
                sb.append("> ");
            if (o2 != null) {
                sb.append(o2.toString());
                sb.append('\n');
            }
        }
    }
}
