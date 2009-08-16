/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.tools.javac.util.Names;
import java.util.*;
import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.util.logging.Logger;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.modules.java.source.transform.FieldGroupTree;
import static com.sun.source.tree.Tree.*;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.api.java.source.Comment;
import org.netbeans.modules.java.source.query.CommentHandler;
import org.netbeans.modules.java.source.query.CommentSet;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.Pretty;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Position;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.source.pretty.VeryPretty;
import org.openide.util.NbBundle;
import static java.util.logging.Level.*;
import static org.netbeans.modules.java.source.save.ListMatcher.*;
import static com.sun.tools.javac.code.Flags.*;
import static org.netbeans.modules.java.source.save.PositionEstimator.*;

public class CasualDiff {
    protected ListBuffer<Diff> diffs;
    protected CommentHandler comments;
    protected JCCompilationUnit oldTopLevel;
    protected WorkingCopy workingCopy;

    private TokenSequence<JavaTokenId> tokenSequence;
    private String origText;
    private VeryPretty printer;
    private Context context;
    private static final Logger LOG = Logger.getLogger(CasualDiff.class.getName());

    private Map<Integer, String> diffInfo = new HashMap<Integer, String>();
    private final Map<Tree, ?> tree2Tag;
    private final Map<Object, int[]> tag2Span;

    // used for diffing var def, when parameter is printed, annotation of
    // such variable should not provide new line at the end.
    private boolean parameterPrint = false;
    private boolean enumConstantPrint = false;

    protected CasualDiff(Context context, WorkingCopy workingCopy, Map<Tree, ?> tree2Tag, Map<?, int[]> tag2Span) {
        diffs = new ListBuffer<Diff>();
        comments = CommentHandlerService.instance(context);
        this.workingCopy = workingCopy;
        this.tokenSequence = workingCopy.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        this.origText = workingCopy.getText();
        this.context = context;
        this.tree2Tag = tree2Tag;
        this.tag2Span = (Map<Object, int[]>) tag2Span;//XXX
        printer = new VeryPretty(workingCopy, VeryPretty.getCodeStyle(workingCopy), tree2Tag, tag2Span);
    }

    public com.sun.tools.javac.util.List<Diff> getDiffs() {
        return diffs.toList();
    }

    public static com.sun.tools.javac.util.List<Diff> diff(Context context,
            WorkingCopy copy,
            TreePath oldTreePath,
            JCTree newTree,
            Map<Integer, String> userInfo,
            Map<Tree, ?> tree2Tag,
            Map<?, int[]> tag2Span)
    {
        CasualDiff td = new CasualDiff(context, copy, tree2Tag, tag2Span);
        JCTree oldTree = (JCTree) oldTreePath.getLeaf();
        td.oldTopLevel =  (JCCompilationUnit) (oldTree.getKind() == Kind.COMPILATION_UNIT ? oldTree : copy.getCompilationUnit());

        for (Tree t : oldTreePath) {
            if (t != oldTree && (t.getKind() == Kind.CLASS || t.getKind() == Kind.BLOCK)) {
                td.printer.indent();
            }
        }

        if (oldTree.getKind() == Kind.CLASS && oldTreePath.getParentPath().getLeaf().getKind() == Kind.NEW_CLASS) {
            td.anonClass = true;
        }

        int[] bounds = td.getBounds(oldTree);
        boolean isCUT = oldTree.getKind() == Kind.COMPILATION_UNIT;
        if (!isCUT) {  // set up tagging offset for non top level classes
            int start = bounds[0];
            if (oldTree.getKind() == Kind.NEW_CLASS) { //find the proper offset for annonymous class
                td.tokenSequence.move(bounds[0]);
                if (td.tokenSequence.movePrevious() && td.tokenSequence.token().id() == JavaTokenId.WHITESPACE) {
                    String text = td.tokenSequence.token().text().toString();
                    int index = text.lastIndexOf('\n');
                    start = td.tokenSequence.offset();
                    if (index > -1) {
                        start += index + 1;
                    }
                }
            }
            td.printer.setInitialOffset(start);
        }

        int start = isCUT ? 0 : bounds[0];
        int end   = isCUT ? td.workingCopy.getText().length() : bounds[1];

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

        if (oldTree.getKind() == Kind.METHOD || (!td.parameterPrint && oldTree.getKind() == Kind.VARIABLE)) {
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

        int ln = td.oldTopLevel.lineMap.getLineNumber(start);
        int lineStart = td.oldTopLevel.lineMap.getStartPosition(ln);

        td.printer.print(td.workingCopy.getText().substring(lineStart, start));
        td.diffTree(oldTree, newTree, (JCTree) (oldTreePath.getParentPath() != null ? oldTreePath.getParentPath().getLeaf() : null), new int[] {start, bounds[1]});
        String resultSrc = td.printer.toString().substring(start - lineStart);
        String originalText = isCUT ? td.workingCopy.getText() : td.workingCopy.getText().substring(start, end);
        new DiffFacility(td).makeListMatch(originalText, resultSrc, start);
        userInfo.putAll(td.diffInfo);

        return td.getDiffs();
    }

    public static com.sun.tools.javac.util.List<Diff> diff(Context context,
            WorkingCopy copy,
            List<? extends ImportTree> original,
            List<? extends ImportTree> nue,
            Map<Integer, String> userInfo,
            Map<Tree, ?> tree2Tag,
            Map<?, int[]> tag2Span)
    {
        CasualDiff td = new CasualDiff(context, copy, tree2Tag, tag2Span);
            td.oldTopLevel = (JCCompilationUnit) copy.getCompilationUnit();
        int start = td.oldTopLevel.getPackageName() != null ? td.endPos(td.oldTopLevel.getPackageName()) : 0;

        List<JCImport> originalJC = new LinkedList<JCImport>();
        List<JCImport> nueJC = new LinkedList<JCImport>();

        for (ImportTree i : original) {
            originalJC.add((JCImport) i);
        }

        for (ImportTree i : nue) {
            nueJC.add((JCImport) i);
        }

        PositionEstimator est = EstimatorFactory.imports(originalJC, nueJC, td.workingCopy);
        int end = td.diffList(originalJC, nueJC, start, est, Measure.DEFAULT, td.printer);

        String resultSrc = td.printer.toString();
        String originalText = td.workingCopy.getText().substring(start, end);
        new DiffFacility(td).makeListMatch(originalText, resultSrc, start);
        userInfo.putAll(td.diffInfo);

        return td.getDiffs();
    }

    protected void append(Diff diff) {
        // check if diff already found -- true for variables that share 
        // fields, such as the mods for "public int foo, bar;"
        for (Diff d : diffs)
            if (d.equals(diff))
                return;
        diffs.append(diff);
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
        int localPointer = 0;
        oldTopLevel = oldT;
        // todo (#pf): make package annotation diffing correctly
        // diffList(oldT.packageAnnotations, newT.packageAnnotations, LineInsertionType.NONE, 0);
        localPointer = diffPackageStatement(oldT, newT, localPointer);
        PositionEstimator est = EstimatorFactory.imports(oldT.getImports(), newT.getImports(), workingCopy);
        localPointer = diffList(oldT.getImports(), newT.getImports(), localPointer, est, Measure.DEFAULT, printer);
        est = EstimatorFactory.toplevel(oldT.getTypeDecls(), newT.getTypeDecls(), workingCopy);
        localPointer = diffList(oldT.getTypeDecls(), newT.getTypeDecls(), localPointer, est, Measure.MEMBER, printer);
        printer.print(origText.substring(localPointer));
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

    private int diffPackageStatement(JCCompilationUnit oldT, JCCompilationUnit newT, int localPointer) {
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
                tokenSequence.move(oldT.pid.getStartPosition());
                moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                copyTo(localPointer, tokenSequence.offset());
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

    protected int diffClassDef(JCClassDecl oldT, JCClassDecl newT, int[] bounds) {
        int localPointer = bounds[0];
        final Name origOuterClassName = origClassName;
        int insertHint = localPointer;
        // skip the section when printing anonymous class
        if (anonClass == false) {
        tokenSequence.move(oldT.pos);
        tokenSequence.moveNext(); // First skip as move() does not position to token directly
        tokenSequence.moveNext();
        moveToSrcRelevant(tokenSequence, Direction.FORWARD);
        insertHint = tokenSequence.offset();
        localPointer = diffModifiers(oldT.mods, newT.mods, oldT, localPointer);
        if (nameChanged(oldT.name, newT.name)) {
            copyTo(localPointer, insertHint);
            printer.print(newT.name);
            diffInfo.put(insertHint, NbBundle.getMessage(CasualDiff.class,"TXT_ChangeClassName"));
            localPointer = insertHint += oldT.name.length();
            origClassName = oldT.name;
        } else {
            insertHint += oldT.name.length();
            copyTo(localPointer, localPointer = insertHint);
        }
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
            EstimatorFactory.implementz(oldT.getImplementsClause(), newT.getImplementsClause(), workingCopy) :
            EstimatorFactory.extendz(oldT.getImplementsClause(), newT.getImplementsClause(), workingCopy);
        if (!newT.implementing.isEmpty())
            copyTo(localPointer, insertHint);
        localPointer = diffList2(oldT.implementing, newT.implementing, insertHint, estimator);
        insertHint = endPos(oldT) - 1;

        if (filterHidden(oldT.defs).isEmpty()) {
            // if there is nothing in class declaration, use position
            // before the closing curly.
            insertHint = endPos(oldT) - 1;
        } else {
            insertHint = filterHidden(oldT.defs).get(0).getStartPosition()-1;
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
        PositionEstimator est = EstimatorFactory.members(filterHidden(oldT.defs), filterHidden(newT.defs), workingCopy);
        if (localPointer < insertHint)
            copyTo(localPointer, insertHint);
        localPointer = diffList(filterHidden(oldT.defs), filterHidden(newT.defs), insertHint, est, Measure.MEMBER, printer);
        printer.enclClassName = origName;
        origClassName = origOuterClassName;
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
            copyTo(localPointer, localPointer = restypeBounds[1]);
        }
        int posHint;
        if (oldT.typarams.isEmpty()) {
            posHint = oldT.restype != null ? oldT.restype.getStartPosition() : oldT.getStartPosition();
        } else {
            posHint = oldT.typarams.iterator().next().getStartPosition();
        }
        if (!oldT.sym.isConstructor() || origClassName != null) {
            if (nameChanged(oldT.name, newT.name)) {
                copyTo(localPointer, oldT.pos);
                // use orig class name in case of constructor
                if (oldT.sym.isConstructor() && (origClassName != null)) {
                    printer.print(newT.name);
                    localPointer = oldT.pos + origClassName.length();
                }
                else {
                    printer.print(newT.name);
                    diffInfo.put(oldT.pos, NbBundle.getMessage(CasualDiff.class,"TXT_RenameMethod") + " " + oldT.name);
                    localPointer = oldT.pos + oldT.name.length();
                }
            } else {
                copyTo(localPointer, localPointer = (oldT.pos + oldT.name.length()));
            }
        }
        if (oldT.params.isEmpty()) {
            // compute the position. Find the parameters closing ')', its
            // start position is important for us. This is used when 
            // there was not any parameter in original tree.
            int startOffset = oldT.restype != null ? oldT.restype.getStartPosition() : oldT.getStartPosition();

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
            localPointer = diffParameterList(oldT.params, newT.params, null, posHint, Measure.MEMBER);
            parameterPrint = false;
            printer.setPrec(old);
        }
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
        copyTo(localPointer, localPointer = posHint);
        PositionEstimator est = EstimatorFactory.throwz(oldT.getThrows(), newT.getThrows(), workingCopy);
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
            diffInfo.put(oldT.pos, NbBundle.getMessage(CasualDiff.class,"TXT_RenameVariable") + " " + oldT.name);
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
        copyTo(localPointer, localPointer = endPos(oldT)-1);
        return localPointer;
    }

    private int diffVarDef(JCVariableDecl oldT, JCVariableDecl newT, int[] bounds) {
        int localPointer = bounds[0];
        // check that it is not enum constant. If so, match it in special way
        if ((oldT.mods.flags & Flags.ENUM) != 0) {
            if (nameChanged(oldT.name, newT.name)) {
                copyTo(localPointer, oldT.pos);
                printer.print(newT.name);
                diffInfo.put(oldT.pos, NbBundle.getMessage(CasualDiff.class,"TXT_RenameEnumConstant") + " " + oldT.name);
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
        int[] vartypeBounds = getBounds(oldT.vartype);
        copyTo(localPointer, vartypeBounds[0]);
        localPointer = diffTree(oldT.vartype, newT.vartype, vartypeBounds);
        if (nameChanged(oldT.name, newT.name)) {
            boolean isOldError = oldT.name == Names.instance(context).error;
            if (!isOldError) {
                copyTo(localPointer, oldT.pos);
            } else {
                printer.print(" ");
            }
            printer.print(newT.name);
            diffInfo.put(oldT.pos, NbBundle.getMessage(CasualDiff.class,"TXT_RenameVariable") + " " + oldT.name);
            if (!isOldError) {
                localPointer = oldT.pos + oldT.name.length();
            }
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
            // TODO: Missing implementation
            // used for changing from/to static initializer
        }
        // syntetic super() found, skip it
        if (oldT.stats.head != null && oldT.stats.head.pos == oldT.pos) {
            oldT.stats = oldT.stats.tail;
        }
        if (newT.stats.head != null && newT.stats.head.pos == oldT.pos) {
            newT.stats = newT.stats.tail;
        }
        PositionEstimator est = EstimatorFactory.statements(
                filterHidden(oldT.stats),
                filterHidden(newT.stats),
                workingCopy
        );
        copyTo(localPointer, oldT.pos + 1);
        int old = printer.indent();
        Name oldEnclosing = printer.enclClassName;
        printer.enclClassName = null;
        List<JCTree> oldstats = filterHidden(oldT.stats);
        localPointer = diffList(oldstats, filterHidden(newT.stats), oldT.pos + 1, est, Measure.MEMBER, printer);
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
        localPointer = diffParameterList(oldT.init, newT.init, null, localPointer, Measure.ARGUMENT);

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
        PositionEstimator est = EstimatorFactory.cases(oldT.getCases(), newT.getCases(), workingCopy);
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
                workingCopy
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
        copyTo(localPointer, bodyPos[0]);
        localPointer = diffTree(oldT.body, newT.body, bodyPos);
        copyTo(localPointer, localPointer = bodyPos[1]);
        PositionEstimator est = EstimatorFactory.catches(oldT.getCatches(), newT.getCatches(), workingCopy);
        localPointer = diffList(oldT.catchers, newT.catchers, localPointer, est, Measure.DEFAULT, printer);

        if (oldT.finalizer != null) {
            int[] finalBounds = getBounds(oldT.finalizer);
            if (newT.finalizer != null) {
                copyTo(localPointer, finalBounds[0]);
                localPointer = diffTree(oldT.finalizer, newT.finalizer, finalBounds);
            } else {
                int endetHier = oldT.catchers.isEmpty() ? endPos(oldT.body) : endPos(oldT.catchers);
                copyTo(localPointer, endetHier);
                localPointer = finalBounds[1];
            }
        }
        copyTo(localPointer, bounds[1]);

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

        int[] condBounds = getBounds(oldT.cond);
        copyTo(localPointer, condBounds[0]);
        localPointer = diffTree(oldT.cond, newT.cond, condBounds);
        int[] partBounds = new int[] { localPointer, endPos(oldT.thenpart) };
        localPointer = diffTree(oldT.thenpart, newT.thenpart, partBounds, oldT.getKind());
        if (oldT.elsepart == null && newT.elsepart != null) {
            printer.printElse(newT, newT.thenpart.getKind() == Kind.BLOCK);
        } else if (oldT.elsepart != null && newT.elsepart == null) {
            // remove else part
            copyTo(localPointer, partBounds[1]);
            copyTo(getBounds(oldT.elsepart)[1], bounds[1]);
            return bounds[1];
        } else {
            if (oldT.elsepart != null) {
                partBounds = new int[] { localPointer, endPos(oldT.elsepart) };
                localPointer = diffTree(oldT.elsepart, newT.elsepart, partBounds, oldT.getKind());
            }
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
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
        if (oldT.expr != null && newT.expr != null) {
            int[] exprBounds = getBounds(oldT.expr);
            copyTo(bounds[0], exprBounds[0]);
            localPointer = diffTree(oldT.expr, newT.expr, exprBounds);
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
        if (oldT.typeargs.nonEmpty() && newT.typeargs.nonEmpty() && Kind.MEMBER_SELECT == oldT.meth.getKind()) {
            localPointer = diffSelect((JCFieldAccess) oldT.meth, (JCFieldAccess) newT.meth, methBounds, oldT.typeargs, newT.typeargs);
        } else {
            localPointer = diffParameterList(oldT.typeargs, newT.typeargs, null, localPointer, Measure.ARGUMENT);
            localPointer = diffTree(oldT.meth, newT.meth, methBounds);
        }
        if (!listsMatch(oldT.args, newT.args)) {
            if (oldT.args.nonEmpty()) {
                copyTo(localPointer, localPointer = getOldPos(oldT.args.head));
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
            localPointer = diffTree(oldT.encl, newT.encl, enclBounds);
        }
        diffParameterList(oldT.typeargs, newT.typeargs, null, localPointer, Measure.ARGUMENT);
        if (!enumConstantPrint) {
            int[] clazzBounds = getBounds(oldT.clazz);
            copyTo(localPointer, clazzBounds[0]);
            localPointer = diffTree(oldT.clazz, newT.clazz, clazzBounds);
        }
        if (oldT.args.nonEmpty()) {
            copyTo(localPointer, localPointer = getOldPos(oldT.args.head));
        } else {
            moveFwdToToken(tokenSequence, oldT.pos, JavaTokenId.LPAREN);
            tokenSequence.moveNext();
            copyTo(localPointer, localPointer = tokenSequence.offset());
        }
        localPointer = diffParameterList(oldT.args, newT.args, null, localPointer, Measure.ARGUMENT);
        // let diffClassDef() method notified that anonymous class is printed.
        if (oldT.def != null) {
            if (newT.def != null) {
                copyTo(localPointer, getOldPos(oldT.def));
                anonClass = true;
                localPointer = diffTree(oldT.def, newT.def, getBounds(oldT.def));
                anonClass = false;
            } else {
                if (endPos(oldT.args) > localPointer) {
                    copyTo(localPointer, endPos(oldT.args));
                }
                printer.print(")");
                localPointer = endPos(oldT.def);
            }
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffNewArray(JCNewArray oldT, JCNewArray newT, int[] bounds) {
        int localPointer = bounds[0];
        // elemtype
        if (oldT.elemtype != null) {
            int[] elemtypeBounds = getBounds(oldT.elemtype);
            copyTo(localPointer, elemtypeBounds[0]);
            localPointer = diffTree(oldT.elemtype, newT.elemtype, elemtypeBounds);
        }
        if (!listsMatch(oldT.dims, newT.dims) && !newT.dims.isEmpty()) {
            // solved just for the change, not insert and delete
            for (com.sun.tools.javac.util.List<JCExpression> l1 = oldT.dims, l2 = newT.dims;
                l1.nonEmpty(); l1 = l1.tail, l2 = l2.tail) {
                localPointer = diffTree(l1.head, l2.head, new int[] { localPointer, getBounds(l1.head)[1] });
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
            printer.print("[]{");
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

    protected int diffAssign(JCAssign oldT, JCAssign newT, int[] bounds) {
        int localPointer = bounds[0];
        // lhs
        int[] lhsBounds = getBounds(oldT.lhs);
        copyTo(localPointer, lhsBounds[0]);
        localPointer = diffTree(oldT.lhs, newT.lhs, lhsBounds);
        // rhs
        int[] rhsBounds = getBounds(oldT.rhs);
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

    private String operatorName(int tag) {
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
            int[] parBounds = getBounds(oldTypePar.head);
            copyTo(localPointer, parBounds[0]);
            localPointer = diffParameterList(oldTypePar, newTypePar, null, parBounds[0], Measure.ARGUMENT);
            parBounds[1] = endPos(oldTypePar);
            tokenSequence.move(parBounds[1]);
            moveToSrcRelevant(tokenSequence, Direction.FORWARD);
            moveToSrcRelevant(tokenSequence, Direction.FORWARD);//skips > and any subsequent unimportant tokens
            copyTo(localPointer, localPointer = tokenSequence.offset());
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
            diffInfo.put(localPointer, NbBundle.getMessage(CasualDiff.class,"TXT_UpdateReferenceTo") + " " + oldT.name);
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
            diffInfo.put(oldT.pos, NbBundle.getMessage(CasualDiff.class,"TXT_UpdateReferenceTo") + " " + oldT.name);
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
            if (newT.arguments.nonEmpty())
                copyTo(localPointer, pos);
            boolean printBrace = oldT.arguments.isEmpty() || newT.arguments.isEmpty();
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
            PositionEstimator est = EstimatorFactory.implementz(oldT.getBounds(), newT.getBounds(), workingCopy);
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
        int[] innerBounds = getBounds(oldT.inner);
        copyTo(localPointer, innerBounds[0]);
        localPointer = diffTree(oldT.inner, newT.inner, innerBounds);
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
        localPointer = diffParameterList(oldT.args, newT.args, parens, localPointer, Measure.ARGUMENT);
        copyTo(localPointer, bounds[1]);

        return bounds[1];
    }

    protected int diffModifiers(JCModifiers oldT, JCModifiers newT, JCTree parent, int localPointer) {
        if (oldT == newT) {
            // modifiers wasn't changed, return the position lastPrinted.
            return localPointer;
        }
        int annotationsEnd = oldT.annotations.nonEmpty() ? endPos(oldT.annotations) : localPointer;
        int startPos = oldT.pos != Position.NOPOS ? getOldPos(oldT) : getOldPos(parent);
        if (listsMatch(oldT.annotations, newT.annotations)) {
            copyTo(localPointer, localPointer = (annotationsEnd != localPointer ? annotationsEnd : startPos));
        } else {
            tokenSequence.move(startPos);
            tokenSequence.movePrevious();
            if (JavaTokenId.WHITESPACE == tokenSequence.token().id()) {
                String text = tokenSequence.token().text().toString();
                int index = text.lastIndexOf('\n');
                startPos = tokenSequence.offset();
                if (index > -1) {
                    startPos += index + 1;
                }
                if (startPos < localPointer) startPos = localPointer;
            }
            copyTo(localPointer, startPos);
            PositionEstimator est = EstimatorFactory.annotations(oldT.getAnnotations(),newT.getAnnotations(), workingCopy, parameterPrint);
            localPointer = diffList(oldT.annotations, newT.annotations, startPos, est, Measure.DEFAULT, printer);
        }

        int endOffset = endPos(oldT);

        if ((oldT.flags & Flags.ANNOTATION) != 0) {
            tokenSequence.move(endOffset);
            tokenSequence.movePrevious();
            moveToSrcRelevant(tokenSequence, Direction.BACKWARD);

            tokenSequence.moveNext();

            endOffset = tokenSequence.offset();
        }
        if (oldT.flags != newT.flags) {
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
                copyTo(localPointer, localPointer = endOffset);
            }
        }
        return localPointer;
    }

    protected void diffLetExpr(LetExpr oldT, LetExpr newT) {
        // TODO: perhaps better to throw exception here. Should be never
        // called.
    }

    protected void diffErroneous(JCErroneous oldT, JCErroneous newT) {
    }

    protected int diffFieldGroup(FieldGroupTree oldT, FieldGroupTree newT, int[] bounds) {
        if (!listsMatch(oldT.getVariables(), newT.getVariables())) {
            copyTo(bounds[0], oldT.getStartPosition());
            if (oldT.isEnum()) {
                int pos = diffParameterList(oldT.getVariables(), newT.getVariables(), null, oldT.getStartPosition(), Measure.ARGUMENT);
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
          case JCTree.TOPLEVEL:
              return ((JCCompilationUnit)t1).sourcefile.equals(((JCCompilationUnit)t2).sourcefile);
          case JCTree.IMPORT:
              return matchImport((JCImport)t1, (JCImport)t2);
          case JCTree.CLASSDEF:
              return ((JCClassDecl)t1).sym == ((JCClassDecl)t2).sym;
          case JCTree.METHODDEF:
              return ((JCMethodDecl)t1).sym == ((JCMethodDecl)t2).sym;
          case JCTree.VARDEF:
              return ((JCVariableDecl)t1).sym == ((JCVariableDecl)t2).sym;
          case JCTree.SKIP:
              return true;
          case JCTree.BLOCK:
              return matchBlock((JCBlock)t1, (JCBlock)t2);
          case JCTree.DOLOOP:
              return matchDoLoop((JCDoWhileLoop)t1, (JCDoWhileLoop)t2);
          case JCTree.WHILELOOP:
              return matchWhileLoop((JCWhileLoop)t1, (JCWhileLoop)t2);
          case JCTree.FORLOOP:
              return matchForLoop((JCForLoop)t1, (JCForLoop)t2);
          case JCTree.FOREACHLOOP:
              return matchForeachLoop((JCEnhancedForLoop)t1, (JCEnhancedForLoop)t2);
          case JCTree.LABELLED:
              return matchLabelled((JCLabeledStatement)t1, (JCLabeledStatement)t2);
          case JCTree.SWITCH:
              return matchSwitch((JCSwitch)t1, (JCSwitch)t2);
          case JCTree.CASE:
              return matchCase((JCCase)t1, (JCCase)t2);
          case JCTree.SYNCHRONIZED:
              return matchSynchronized((JCSynchronized)t1, (JCSynchronized)t2);
          case JCTree.TRY:
              return matchTry((JCTry)t1, (JCTry)t2);
          case JCTree.CATCH:
              return matchCatch((JCCatch)t1, (JCCatch)t2);
          case JCTree.CONDEXPR:
              return matchConditional((JCConditional)t1, (JCConditional)t2);
          case JCTree.IF:
              return matchIf((JCIf)t1, (JCIf)t2);
          case JCTree.EXEC:
              return treesMatch(((JCExpressionStatement)t1).expr, ((JCExpressionStatement)t2).expr);
          case JCTree.BREAK:
              return matchBreak((JCBreak)t1, (JCBreak)t2);
          case JCTree.CONTINUE:
              return matchContinue((JCContinue)t1, (JCContinue)t2);
          case JCTree.RETURN:
              return treesMatch(((JCReturn)t1).expr, ((JCReturn)t2).expr);
          case JCTree.THROW:
              return treesMatch(((JCThrow)t1).expr, ((JCThrow)t2).expr);
          case JCTree.ASSERT:
              return matchAssert((JCAssert)t1, (JCAssert)t2);
          case JCTree.APPLY:
              return matchApply((JCMethodInvocation)t1, (JCMethodInvocation)t2);
          case JCTree.NEWCLASS:
              // #97501: workaround. Not sure about comparing symbols and their
              // copying in ImmutableTreeTranslator, making workaround with
              // minimal impact - issue has to be fixed correctly in the future.
              if (((JCNewClass)t2).def != null) ((JCNewClass)t2).def.sym = null;
              return matchNewClass((JCNewClass)t1, (JCNewClass)t2);
          case JCTree.NEWARRAY:
              return matchNewArray((JCNewArray)t1, (JCNewArray)t2);
          case JCTree.PARENS:
              return treesMatch(((JCParens)t1).expr, ((JCParens)t2).expr);
          case JCTree.ASSIGN:
              return matchAssign((JCAssign)t1, (JCAssign)t2);
          case JCTree.TYPECAST:
              return matchTypeCast((JCTypeCast)t1, (JCTypeCast)t2);
          case JCTree.TYPETEST:
              return matchTypeTest((JCInstanceOf)t1, (JCInstanceOf)t2);
          case JCTree.INDEXED:
              return matchIndexed((JCArrayAccess)t1, (JCArrayAccess)t2);
          case JCTree.SELECT:
              return matchSelect((JCFieldAccess) t1, (JCFieldAccess) t2);
          case JCTree.IDENT:
              return ((JCIdent)t1).getName().contentEquals(((JCIdent)t2).getName());
          case JCTree.LITERAL:
              return matchLiteral((JCLiteral)t1, (JCLiteral)t2);
          case JCTree.TYPEIDENT:
              return ((JCPrimitiveTypeTree)t1).typetag == ((JCPrimitiveTypeTree)t2).typetag;
          case JCTree.TYPEARRAY:
              return treesMatch(((JCArrayTypeTree)t1).elemtype, ((JCArrayTypeTree)t2).elemtype);
          case JCTree.TYPEAPPLY:
              return matchTypeApply((JCTypeApply)t1, (JCTypeApply)t2);
          case JCTree.TYPEPARAMETER:
              return matchTypeParameter((JCTypeParameter)t1, (JCTypeParameter)t2);
          case JCTree.WILDCARD:
              return matchWildcard((JCWildcard)t1, (JCWildcard)t2);
          case JCTree.TYPEBOUNDKIND:
              return ((TypeBoundKind)t1).kind == ((TypeBoundKind)t2).kind;
          case JCTree.ANNOTATION:
              return matchAnnotation((JCAnnotation)t1, (JCAnnotation)t2);
          case JCTree.LETEXPR:
              return matchLetExpr((LetExpr)t1, (LetExpr)t2);
          case JCTree.POS:
          case JCTree.NEG:
          case JCTree.NOT:
          case JCTree.COMPL:
          case JCTree.PREINC:
          case JCTree.PREDEC:
          case JCTree.POSTINC:
          case JCTree.POSTDEC:
          case JCTree.NULLCHK:
              return matchUnary((JCUnary)t1, (JCUnary)t2);
          case JCTree.OR:
          case JCTree.AND:
          case JCTree.BITOR:
          case JCTree.BITXOR:
          case JCTree.BITAND:
          case JCTree.EQ:
          case JCTree.NE:
          case JCTree.LT:
          case JCTree.GT:
          case JCTree.LE:
          case JCTree.GE:
          case JCTree.SL:
          case JCTree.SR:
          case JCTree.USR:
          case JCTree.PLUS:
          case JCTree.MINUS:
          case JCTree.MUL:
          case JCTree.DIV:
          case JCTree.MOD:
              return matchBinary((JCBinary)t1, (JCBinary)t2);
          case JCTree.BITOR_ASG:
          case JCTree.BITXOR_ASG:
          case JCTree.BITAND_ASG:
          case JCTree.SL_ASG:
          case JCTree.SR_ASG:
          case JCTree.USR_ASG:
          case JCTree.PLUS_ASG:
          case JCTree.MINUS_ASG:
          case JCTree.MUL_ASG:
          case JCTree.DIV_ASG:
          case JCTree.MOD_ASG:
              return matchAssignop((JCAssignOp)t1, (JCAssignOp)t2);
          default:
              String msg = ((com.sun.source.tree.Tree)t1).getKind().toString() +
                      " " + t1.getClass().getName();
              throw new AssertionError(msg);
        }
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
                    copyTo(lastOldPos, getOldPos(oldT));

                    if (treesMatch(oldT, item.element, false)) {
                        lastOldPos = diffTree(oldT, item.element, getBounds(oldT));
                    } else {
                        printer.print(item.element);
                        lastOldPos = Math.max(testPos, endPos(oldT));
                    }
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
                        if (JavaTokenId.COMMA == tokenSequence.token().id())
                            testPos += JavaTokenId.COMMA.fixedText().length();
                    }
                    if (i == 0 && !newList.isEmpty()) {
                        lastOldPos = endOffset;
                    } else {
                        lastOldPos = endPos(item.element);
//                        lastOldPos = Math.max(testPos, endPos(item.element));
                    }
                    oldT = oldIter.next(); ++i;
                    break;
                }
                case NOCHANGE: {
                    tokenSequence.moveIndex(matrix[i][4]);
                    if (tokenSequence.moveNext()) {
                        testPos = tokenSequence.offset();
                        if (JavaTokenId.COMMA == tokenSequence.token().id())
                            testPos += JavaTokenId.COMMA.fixedText().length();
                    }
                    oldT = oldIter.next(); ++i;
                    copyTo(lastOldPos, lastOldPos = endPos(oldT));
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
        boolean wasComma = false;
        for (int j = 0; j < result.length; j++) {
            ResultItem<JCTree> item = result[j];
            switch (item.operation) {
                case MODIFY: {
                    JCTree tree = oldList.get(oldIndex++);
                    int[] bounds = getBounds(tree);
                    tokenSequence.move(bounds[0]);
                    if (oldIndex != 1) {
                        moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                    }
                    tokenSequence.moveNext();
                    int start = tokenSequence.offset();
                    copyTo(start, bounds[0], printer);
                    diffTree(tree, item.element, bounds);
                    tokenSequence.move(bounds[1]);
                    moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                    copyTo(bounds[1], pos = tokenSequence.offset(), printer);
                    break;
                }
                // insert new element
                case INSERT: {
                    if (wasComma) {
                        if (VeryPretty.getCodeStyle(workingCopy).spaceAfterComma()) {
                            printer.print(" ");
                        }
                    }
                    printer.print(item.element);
                    break;
                }
                case DELETE:
                    oldIndex++;
                    tokenSequence.move(getBounds(item.element)[1]);
                    moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                    pos = tokenSequence.offset();
                    break;
                // just copy existing element
                case NOCHANGE:
                    oldIndex++;
                    int[] bounds = getBounds(item.element);
                    tokenSequence.move(bounds[0]);
                    if (oldIndex != 1) {
                        moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                    }
                    tokenSequence.moveNext();
                    int start = tokenSequence.offset();
                    tokenSequence.move(bounds[1]);
                    moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                    int end = tokenSequence.offset();
                    copyTo(start, pos = end, printer);
                    break;
                default:
                    break;
            }
            if (commaNeeded(result, item)) {
                printer.print(",");
                wasComma = true;
            } else {
                if (item.operation != Operation.DELETE) {
                    wasComma = false;
                }
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
            return tokenSequence.offset();
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
        for (int j = 0; j < result.length; j++) {
            ResultItem<JCTree> item = result[j];
            switch (item.operation) {
                case MODIFY: {
                    JCTree tree = oldList.get(oldIndex++);
                    int[] bounds = getBounds(tree);
                    if (oldIndex != 1) {
                        bounds[0] = tree.pos;
                    }
                    bounds[1]--;
                    tokenSequence.move(bounds[0]);
                    if (oldIndex != 1) {
                        moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                    }
                    tokenSequence.moveNext();
                    int start = tokenSequence.offset();
                    copyTo(start, bounds[0], printer);
                    if (oldIndex != 1) {
                        diffVarDef((JCVariableDecl) tree, (JCVariableDecl) item.element, bounds[0]);
                    } else {
                        diffVarDef((JCVariableDecl) tree, (JCVariableDecl) item.element, bounds);
                    }
                    tokenSequence.move(bounds[1]);
                    moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                    copyTo(bounds[1], pos = tokenSequence.offset(), printer);
                    break;
                }
                // just copy existing element
                case NOCHANGE: {
                    oldIndex++;
                    int[] bounds = getBounds(item.element);
                    if (oldIndex != 1) {
                        bounds[0] = item.element.pos;
                    }
                    bounds[1]--;
                    tokenSequence.move(bounds[0]);
                    if (oldIndex != 1) {
                        moveToSrcRelevant(tokenSequence, Direction.BACKWARD);
                    }
                    tokenSequence.moveNext();
                    int start = tokenSequence.offset();
                    tokenSequence.move(bounds[1]);
                    moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                    int end = tokenSequence.offset();
                    copyTo(start, pos = end, printer);
                    break;
                }
                case DELETE: {
                    oldIndex++;
                    int[] bounds = getBounds(item.element);
                    tokenSequence.move(bounds[1] - 1);
                    moveToSrcRelevant(tokenSequence, Direction.FORWARD);
                    pos = tokenSequence.offset();
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
        List<JCTree> result = new ArrayList<JCTree>(); // todo (#pf): capacity?
        List<JCVariableDecl> fieldGroup = new ArrayList<JCVariableDecl>();
        boolean enumConstants = false;
        for (JCTree tree : list) {
            if (Kind.VARIABLE == tree.getKind()) {
                JCVariableDecl var = (JCVariableDecl) tree;
                if ((var.mods.flags & Flags.ENUM) != 0) {
                    // collect enum constants, make a field group from them
                    // and set the flag.
                    fieldGroup.add(var);
                    enumConstants = true;
                } else {
                    if (!fieldGroup.isEmpty()) {
                        int oldPos = getOldPos(fieldGroup.get(0));

                        if (oldPos != (-1) && oldPos != NOPOS && oldPos == getOldPos(var) && fieldGroup.get(0).getModifiers() == var.getModifiers()) {
                            //seems like a field group:
                            fieldGroup.add(var);
                        } else {
                            if (fieldGroup.size() > 1) {
                                result.add(new FieldGroupTree(fieldGroup, enumConstants));
                            } else {
                                result.add(fieldGroup.get(0));
                            }
                            fieldGroup = new ArrayList<JCVariableDecl>();
                            enumConstants = false;

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
                    result.add(new FieldGroupTree(fieldGroup, enumConstants));
                } else {
                    result.add(fieldGroup.get(0));
                }
                fieldGroup = new ArrayList<JCVariableDecl>();
                enumConstants = false;
            }

            if (Kind.METHOD == tree.getKind()) {
                // filter syntetic constructors, i.e. constructors which are in
                // the tree, but not available in the source.
                if ((((JCMethodDecl)tree).mods.flags & Flags.GENERATEDCONSTR) != 0)
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
                result.add(new FieldGroupTree(fieldGroup, enumConstants));
            } else {
                result.add(fieldGroup.get(0));
            }
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
                printer.printImportsBlock(newList);
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
            switch (item.operation) {
                case MODIFY: {
                    int[] bounds = estimator.getPositions(i);
                    copyTo(localPointer, bounds[0], printer);
                    localPointer = diffTree(oldList.get(i), item.element, bounds);
                    ++i;
                    break;
                }
                case INSERT: {
                    int pos = estimator.getInsertPos(i);
                    if (pos > localPointer) {
                        copyTo(localPointer, localPointer = pos);
                    }
                    int oldPos = item.element.getKind() != Kind.VARIABLE ? getOldPos(item.element) : item.element.pos;
                    boolean found = false;
                    if (oldPos > 0) {
                        for (JCTree oldT : oldList) {
                            int oldNodePos = oldT.getKind() != Kind.VARIABLE ? getOldPos(oldT) : oldT.pos;
                            if (oldPos == oldNodePos) {
                                found = true;
                                VeryPretty oldPrinter = this.printer;
                                int old = oldPrinter.indent();
                                this.printer = new VeryPretty(workingCopy, VeryPretty.getCodeStyle(workingCopy), tree2Tag, tag2Span, oldPrinter.toString().length() + oldPrinter.getInitialOffset());//XXX
                                this.printer.reset(old);
                                int index = oldList.indexOf(oldT);
                                int[] poss = estimator.getPositions(index);
                                int end = diffTree(oldT, item.element, poss);
                                copyTo(end, poss[1]);
                                printer.print(this.printer.toString());
                                this.printer = oldPrinter;
                                this.printer.undent(old);
                                break;
                            }
                        }
                    }
                    if (!found) {
                        if (lastdel != null && treesMatch(item.element, lastdel, false)) {
                            VeryPretty oldPrinter = this.printer;
                            int old = oldPrinter.indent();
                            this.printer = new VeryPretty(workingCopy, VeryPretty.getCodeStyle(workingCopy), tree2Tag, tag2Span, oldPrinter.toString().length() + oldPrinter.getInitialOffset());//XXX
                            this.printer.reset(old);
                            int index = oldList.indexOf(lastdel);
                            int[] poss = estimator.getPositions(index);
                            //TODO: should the original text between the return position of the following method and poss[1] be copied into the new text?
                            diffTree(lastdel, item.element, poss);
                            printer.print(this.printer.toString());
                            this.printer = oldPrinter;
                            this.printer.undent(old);
                            lastdel = null;
                            break;
                        }
                        if (LineInsertionType.BEFORE == estimator.lineInsertType()) printer.newline();
                        printer.print(item.element);
                        if (LineInsertionType.AFTER == estimator.lineInsertType()) printer.newline();
                    }
                    break;
                }
                case DELETE: {
                    int[] pos = estimator.getPositions(i);
                    if (localPointer < pos[0]) {
                        copyTo(localPointer, pos[0], printer);
                    }
                    lastdel = oldList.get(i);
                    ++i;
                    localPointer = pos[1];
                    break;
                }
                case NOCHANGE: {
                    int[] pos = estimator.getPositions(i);
                    if (pos[0] > localPointer && i != 0) {
                        // print fill-in
                        copyTo(localPointer, pos[0], printer);
                    }
                    copyTo(pos[0], localPointer = pos[1], printer);
                    lastdel = null;
                    ++i;
                    break;
                }
            }
        }
        return localPointer;
    }

    protected void diffPrecedingComments(JCTree oldT, JCTree newT) {
        CommentSet cs = comments.getComments(newT);
        if (!cs.hasChanges())
            return;
        List<Comment> oldComments = comments.getComments(oldT).getPrecedingComments();
        List<Comment> newComments = cs.getPrecedingComments();
        diffCommentLists(oldT, newT, oldComments, newComments, false);
    }

    protected void diffTrailingComments(JCTree oldT, JCTree newT) {
        CommentSet cs = comments.getComments(newT);
        if (!cs.hasChanges())
            return;
        List<Comment> oldComments = comments.getComments(oldT).getTrailingComments();
        List<Comment> newComments = cs.getTrailingComments();
        diffCommentLists(oldT, newT, oldComments, newComments, true);
    }

    // refactor it! make it better
    private void diffCommentLists(JCTree oldT, JCTree newT, List<Comment>oldList,
                                  List<Comment>newList, boolean trailing) {
        int lastPos = getOldPos(oldT);
        Iterator<Comment> oldIter = oldList.iterator();
        Iterator<Comment> newIter = newList.iterator();
        Comment oldC = safeNext(oldIter);
        Comment newC = safeNext(newIter);
        while (oldC != null && newC != null) {
            lastPos = oldC.pos();
            if (commentsMatch(oldC, newC)) {
                oldC = safeNext(oldIter);
                newC = safeNext(newIter);
            }
            else if (!listContains(newList, oldC)) {
                if  (!listContains(oldList, newC)) {
//                    append(Diff.modify(oldT, newT, oldC, newC));
                    oldC = safeNext(oldIter);
                    newC = safeNext(newIter);
                } else {
//                    append(Diff.delete(oldT, newT, oldC));
                    oldC = safeNext(oldIter);
                }
            }
            else {
                printer.print(newC.getText());
                newC = safeNext(newIter);
            }
        }
        while (oldC != null) {
//            append(Diff.delete(oldT, newT, oldC));
            oldC = safeNext(oldIter);
        }
        while (newC != null) {
            if (Style.WHITESPACE != newC.style()) {
//                printer.print(newC.getText());                
                printer.printComment(newC, !trailing, false);
                lastPos += newC.endPos() - newC.pos();
            }
            newC = safeNext(oldIter);
        }
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

    // from TreesService
    private static JCTree leftMostTree(JCTree tree) {
        switch (tree.getTag()) {
            case(JCTree.APPLY):
                return leftMostTree(((JCMethodInvocation)tree).meth);
            case(JCTree.ASSIGN):
                return leftMostTree(((JCAssign)tree).lhs);
            case(JCTree.BITOR_ASG): case(JCTree.BITXOR_ASG): case(JCTree.BITAND_ASG):
            case(JCTree.SL_ASG): case(JCTree.SR_ASG): case(JCTree.USR_ASG):
            case(JCTree.PLUS_ASG): case(JCTree.MINUS_ASG): case(JCTree.MUL_ASG):
            case(JCTree.DIV_ASG): case(JCTree.MOD_ASG):
                return leftMostTree(((JCAssignOp)tree).lhs);
            case(JCTree.OR): case(JCTree.AND): case(JCTree.BITOR):
            case(JCTree.BITXOR): case(JCTree.BITAND): case(JCTree.EQ):
            case(JCTree.NE): case(JCTree.LT): case(JCTree.GT):
            case(JCTree.LE): case(JCTree.GE): case(JCTree.SL):
            case(JCTree.SR): case(JCTree.USR): case(JCTree.PLUS):
            case(JCTree.MINUS): case(JCTree.MUL): case(JCTree.DIV):
            case(JCTree.MOD):
                return leftMostTree(((JCBinary)tree).lhs);
            case(JCTree.CLASSDEF): {
                JCClassDecl node = (JCClassDecl)tree;
                if (node.mods.pos != Position.NOPOS)
                    return node.mods;
                break;
            }
            case(JCTree.CONDEXPR):
                return leftMostTree(((JCConditional)tree).cond);
            case(JCTree.EXEC):
                return leftMostTree(((JCExpressionStatement)tree).expr);
            case(JCTree.INDEXED):
                return leftMostTree(((JCArrayAccess)tree).indexed);
            case(JCTree.METHODDEF): {
                JCMethodDecl node = (JCMethodDecl)tree;
                if (node.mods.pos != Position.NOPOS)
                    return node.mods;
                if (node.restype != null) // true for constructors
                    return leftMostTree(node.restype);
                return node;
            }
            case(JCTree.SELECT):
                return leftMostTree(((JCFieldAccess)tree).selected);
            case(JCTree.TYPEAPPLY):
                return leftMostTree(((JCTypeApply)tree).clazz);
            case(JCTree.TYPEARRAY):
                return leftMostTree(((JCArrayTypeTree)tree).elemtype);
            case(JCTree.TYPETEST):
                return leftMostTree(((JCInstanceOf)tree).expr);
            case(JCTree.POSTINC):
            case(JCTree.POSTDEC):
                return leftMostTree(((JCUnary)tree).arg);
            case(JCTree.VARDEF): {
                JCVariableDecl node = (JCVariableDecl)tree;
                if (node.mods.pos != Position.NOPOS)
                    return node.mods;
                return leftMostTree(node.vartype);
            }
            case(JCTree.TOPLEVEL): {
                JCCompilationUnit node = (JCCompilationUnit)tree;
                assert node.defs.size() > 0;
                return node.pid != null ? node.pid : node.defs.head;
            }
        }
        return tree;
    }

    private static int getOldPos(JCTree oldT) {
        return TreeInfo.getStartPos(oldT);
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

        diffPrecedingComments(oldT, newT);
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

        switch (oldT.getTag()) {
          case JCTree.TOPLEVEL:
              diffTopLevel((JCCompilationUnit)oldT, (JCCompilationUnit)newT);
              break;
          case JCTree.IMPORT:
              retVal = diffImport((JCImport)oldT, (JCImport)newT, elementBounds);
              break;
          case JCTree.CLASSDEF:
              retVal = diffClassDef((JCClassDecl)oldT, (JCClassDecl)newT, elementBounds);
              break;
          case JCTree.METHODDEF:
              retVal = diffMethodDef((JCMethodDecl)oldT, (JCMethodDecl)newT, elementBounds);
              break;
          case JCTree.VARDEF:
              return diffVarDef((JCVariableDecl)oldT, (JCVariableDecl)newT, elementBounds);
          case JCTree.SKIP:
              copyTo(elementBounds[0], elementBounds[1]);
              retVal = elementBounds[1];
              break;
          case JCTree.BLOCK:
              retVal = diffBlock((JCBlock)oldT, (JCBlock)newT, elementBounds);
              break;
          case JCTree.DOLOOP:
              retVal = diffDoLoop((JCDoWhileLoop)oldT, (JCDoWhileLoop)newT, elementBounds);
              break;
          case JCTree.WHILELOOP:
              retVal = diffWhileLoop((JCWhileLoop)oldT, (JCWhileLoop)newT, elementBounds);
              break;
          case JCTree.FORLOOP:
              retVal = diffForLoop((JCForLoop)oldT, (JCForLoop)newT, elementBounds);
              break;
          case JCTree.FOREACHLOOP:
              retVal = diffForeachLoop((JCEnhancedForLoop)oldT, (JCEnhancedForLoop)newT, elementBounds);
              break;
          case JCTree.LABELLED:
              retVal = diffLabelled((JCLabeledStatement)oldT, (JCLabeledStatement)newT, elementBounds);
              break;
          case JCTree.SWITCH:
              retVal = diffSwitch((JCSwitch)oldT, (JCSwitch)newT, elementBounds);
              break;
          case JCTree.CASE:
              retVal = diffCase((JCCase)oldT, (JCCase)newT, elementBounds);
              break;
          case JCTree.SYNCHRONIZED:
              retVal = diffSynchronized((JCSynchronized)oldT, (JCSynchronized)newT, elementBounds);
              break;
          case JCTree.TRY:
              retVal = diffTry((JCTry)oldT, (JCTry)newT, elementBounds);
              break;
          case JCTree.CATCH:
              retVal = diffCatch((JCCatch)oldT, (JCCatch)newT, elementBounds);
              break;
          case JCTree.CONDEXPR:
              retVal = diffConditional((JCConditional)oldT, (JCConditional)newT, elementBounds);
              break;
          case JCTree.IF:
              retVal = diffIf((JCIf)oldT, (JCIf)newT, elementBounds);
              break;
          case JCTree.EXEC:
              retVal = diffExec((JCExpressionStatement)oldT, (JCExpressionStatement)newT, elementBounds);
              break;
          case JCTree.BREAK:
              retVal = diffBreak((JCBreak)oldT, (JCBreak)newT, elementBounds);
              break;
          case JCTree.CONTINUE:
              retVal = diffContinue((JCContinue)oldT, (JCContinue)newT, elementBounds);
              break;
          case JCTree.RETURN:
              retVal = diffReturn((JCReturn)oldT, (JCReturn)newT, elementBounds);
              break;
          case JCTree.THROW:
              retVal = diffThrow((JCThrow)oldT, (JCThrow)newT,elementBounds);
              break;
          case JCTree.ASSERT:
              retVal = diffAssert((JCAssert)oldT, (JCAssert)newT, elementBounds);
              break;
          case JCTree.APPLY:
              retVal = diffApply((JCMethodInvocation)oldT, (JCMethodInvocation)newT, elementBounds);
              break;
          case JCTree.NEWCLASS:
              retVal = diffNewClass((JCNewClass)oldT, (JCNewClass)newT, elementBounds);
              break;
          case JCTree.NEWARRAY:
              retVal = diffNewArray((JCNewArray)oldT, (JCNewArray)newT, elementBounds);
              break;
          case JCTree.PARENS:
              retVal = diffParens((JCParens)oldT, (JCParens)newT, elementBounds);
              break;
          case JCTree.ASSIGN:
              retVal = diffAssign((JCAssign)oldT, (JCAssign)newT, elementBounds);
              break;
          case JCTree.TYPECAST:
              retVal = diffTypeCast((JCTypeCast)oldT, (JCTypeCast)newT, elementBounds);
              break;
          case JCTree.TYPETEST:
              retVal = diffTypeTest((JCInstanceOf)oldT, (JCInstanceOf)newT, elementBounds);
              break;
          case JCTree.INDEXED:
              retVal = diffIndexed((JCArrayAccess)oldT, (JCArrayAccess)newT, elementBounds);
              break;
          case JCTree.SELECT:
              retVal = diffSelect((JCFieldAccess)oldT, (JCFieldAccess)newT, elementBounds);
              break;
          case JCTree.IDENT:
              retVal = diffIdent((JCIdent)oldT, (JCIdent)newT, elementBounds);
              break;
          case JCTree.LITERAL:
              retVal = diffLiteral((JCLiteral)oldT, (JCLiteral)newT, elementBounds);
              break;
          case JCTree.TYPEIDENT:
              retVal = diffTypeIdent((JCPrimitiveTypeTree)oldT, (JCPrimitiveTypeTree)newT, elementBounds);
              break;
          case JCTree.TYPEARRAY:
              retVal = diffTypeArray((JCArrayTypeTree)oldT, (JCArrayTypeTree)newT, elementBounds);
              break;
          case JCTree.TYPEAPPLY:
              retVal = diffTypeApply((JCTypeApply)oldT, (JCTypeApply)newT, elementBounds);
              break;
          case JCTree.TYPEPARAMETER:
              retVal = diffTypeParameter((JCTypeParameter)oldT, (JCTypeParameter)newT, elementBounds);
              break;
          case JCTree.WILDCARD:
              retVal = diffWildcard((JCWildcard)oldT, (JCWildcard)newT, elementBounds);
              break;
          case JCTree.TYPEBOUNDKIND:
              retVal = diffTypeBoundKind((TypeBoundKind)oldT, (TypeBoundKind)newT, elementBounds);
              break;
          case JCTree.ANNOTATION:
              retVal = diffAnnotation((JCAnnotation)oldT, (JCAnnotation)newT, elementBounds);
              break;
          case JCTree.LETEXPR:
              diffLetExpr((LetExpr)oldT, (LetExpr)newT);
              break;
          case JCTree.POS:
          case JCTree.NEG:
          case JCTree.NOT:
          case JCTree.COMPL:
          case JCTree.PREINC:
          case JCTree.PREDEC:
          case JCTree.POSTINC:
          case JCTree.POSTDEC:
          case JCTree.NULLCHK:
              retVal = diffUnary((JCUnary)oldT, (JCUnary)newT, elementBounds);
              break;
          case JCTree.OR:
          case JCTree.AND:
          case JCTree.BITOR:
          case JCTree.BITXOR:
          case JCTree.BITAND:
          case JCTree.EQ:
          case JCTree.NE:
          case JCTree.LT:
          case JCTree.GT:
          case JCTree.LE:
          case JCTree.GE:
          case JCTree.SL:
          case JCTree.SR:
          case JCTree.USR:
          case JCTree.PLUS:
          case JCTree.MINUS:
          case JCTree.MUL:
          case JCTree.DIV:
          case JCTree.MOD:
              retVal = diffBinary((JCBinary)oldT, (JCBinary)newT, elementBounds);
              break;
          case JCTree.BITOR_ASG:
          case JCTree.BITXOR_ASG:
          case JCTree.BITAND_ASG:
          case JCTree.SL_ASG:
          case JCTree.SR_ASG:
          case JCTree.USR_ASG:
          case JCTree.PLUS_ASG:
          case JCTree.MINUS_ASG:
          case JCTree.MUL_ASG:
          case JCTree.DIV_ASG:
          case JCTree.MOD_ASG:
              retVal = diffAssignop((JCAssignOp)oldT, (JCAssignOp)newT, elementBounds);
              break;
          case JCTree.ERRONEOUS:
              diffErroneous((JCErroneous)oldT, (JCErroneous)newT);
              break;
          case JCTree.MODIFIERS:
              retVal = diffModifiers((JCModifiers) oldT, (JCModifiers) newT, parent, elementBounds[0]);
              copyTo(retVal, elementBounds[1]);
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
        diffTrailingComments(oldT, newT);
        return retVal;
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
        return treesMatch(t1.selected, t2.selected) && t1.sym == t2.sym;
    }

    private boolean matchLiteral(JCLiteral t1, JCLiteral t2) {
        return t1.typetag == t2.typetag && (t1.value == t2.value || (t1.value != null && t1.value.equals(t2.value)));
    }

    private boolean matchTypeApply(JCTypeApply t1, JCTypeApply t2) {
        return treesMatch(t1.clazz, t2.clazz) &&
               listsMatch(t1.arguments, t2.arguments);
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

    private int[] getBounds(JCTree tree) {
        return new int[] { getOldPos(tree), endPos(tree) };
    }

    private void copyTo(int from, int to) {
        copyTo(from, to, printer);
    }

    private void copyTo(int from, int to, VeryPretty loc) {
        if (from == to) {
            return;
        } else if (from > to || from < 0 || to < 0) {
            // #104107 - log the source when this problem occurs.
            LOG.log(INFO, "-----\n" + origText + "-----\n");
            LOG.log(INFO, "Illegal values: from = " + from + "; to = " + to + "." +
                "Please, attach your messages.log to new issue!");
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
            copyTo(elementBounds[0], tokenSequence.offset());
            printer.printBlock(oldT, newT, parentKind);
            return endPos(oldT);
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
