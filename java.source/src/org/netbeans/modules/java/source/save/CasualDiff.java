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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.source.save;

import com.sun.source.tree.*;
import static com.sun.source.tree.Tree.*;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.ASTService;
import org.netbeans.modules.java.source.builder.UndoListService;
import org.netbeans.api.java.source.transform.UndoList;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.query.CommentHandler;
import org.netbeans.api.java.source.query.CommentSet;
import org.netbeans.modules.java.source.engine.ASTModel;
import org.netbeans.api.java.source.query.Query;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.Pretty;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Position;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.java.source.engine.SourceRewriter;
import org.netbeans.modules.java.source.engine.StringSourceRewriter;
import org.netbeans.modules.java.source.engine.JavaFormatOptions;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.pretty.VeryPretty;
import org.netbeans.modules.java.source.save.ListMatcher;
import static org.netbeans.modules.java.source.save.TreeDiff.LineInsertionType.*;
import static org.netbeans.modules.java.source.save.ListMatcher.*;
import static com.sun.tools.javac.code.Flags.*;
import static org.netbeans.modules.java.source.save.TreeDiff.*;
import static org.netbeans.modules.java.source.save.PositionEstimator.*;

public class CasualDiff {
    protected ListBuffer<Diff> diffs;
    protected CommentHandler comments;
    protected ASTModel model;
    protected UndoList undo;
    protected JCTree oldParent;
    protected JCTree newParent;
    protected JCCompilationUnit oldTopLevel;
    
    private WorkingCopy workingCopy;
    private TokenSequence<JavaTokenId> tokenSequence;
    private SourceRewriter output;
    private String origText;
    private VeryPretty printer;
    private int pointer;
    private Context context;

    private Map<Integer, String> diffInfo = new HashMap<Integer, String>();
    
    /** provided for test only */
    public CasualDiff() {
    }

    protected CasualDiff(Context context, WorkingCopy workingCopy) {
        diffs = new ListBuffer<Diff>();
        comments = CommentHandlerService.instance(context);
        model = ASTService.instance(context);
        undo = UndoListService.instance(context);
        this.workingCopy = workingCopy;
        this.tokenSequence = workingCopy.getTokenHierarchy().tokenSequence();
        this.output = new StringSourceRewriter();
        this.origText = workingCopy.getText();
        this.context = context;
        printer = new VeryPretty(context, JavaFormatOptions.getDefault());
    }
    
    public com.sun.tools.javac.util.List<Diff> getDiffs() {
        return diffs.toList();
    }
    
    public static com.sun.tools.javac.util.List<Diff> diff(Context context,
            WorkingCopy copy,
            JCTree oldTree,
            JCTree newTree) 
    {
        CasualDiff td = new CasualDiff(context, copy);
        try {
            td.diffTree(oldTree, newTree, new int[] { -1, -1});
            String resultSrc = td.output.toString();
            td.makeListMatch(td.workingCopy.getText(), resultSrc);
            JavaSourceAccessor.INSTANCE.getCommandEnvironment(td.workingCopy).setResult(td.diffInfo, "user-info");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return td.getDiffs();
    }
    
    private void append(Diff diff) {
        // check if diff already found -- true for variables that share 
        // fields, such as the mods for "public int foo, bar;"
        for (Diff d : diffs)
            if (d.equals(diff))
                return;
        diffs.append(diff);
    }
    
    // todo (#pf): Is this really needed? -- Seems it duplicates the work
    // in SourcePositions, but in different way. Uses map of endPositions.
    // Look into the implementation and try to use SourcePositions!
    private int endPos(JCTree t) {
        return model.getEndPos(t, oldTopLevel);
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
        JCTree tree;
        return endPos(trees.get(trees.size()-1));
    }

    protected void diffTopLevel(JCCompilationUnit oldT, JCCompilationUnit newT) {
        oldTopLevel = oldT;
        // todo (#pf): make package annotation diffing correctly
        // diffList(oldT.packageAnnotations, newT.packageAnnotations, LineInsertionType.NONE, 0);
        int posHint = 0;
        try {
            posHint = diffPackageStatement(oldT, newT, pointer);
            PositionEstimator est = EstimatorFactory.imports(((CompilationUnitTree) oldT).getImports(), ((CompilationUnitTree) newT).getImports(), workingCopy);
            pointer = diffListImports(oldT.getImports(), newT.getImports(), posHint, est, Measure.DEFAULT, printer);
            if (oldT.getTypeDecls().nonEmpty()) {
                posHint = getOldPos(oldT.getTypeDecls().head);
            } else {
                // todo (#pf): this has to be fixed, missing code here.
            }
            output.writeTo(printer.toString());
            printer.reset(0);
            est = EstimatorFactory.toplevel(((CompilationUnitTree) oldT).getTypeDecls(), ((CompilationUnitTree) newT).getTypeDecls(), workingCopy);
            int[] pos = diffList(oldT.getTypeDecls(), newT.getTypeDecls(), posHint, est, Measure.DEFAULT, printer);
            if (pointer < pos[0])
                output.writeTo(origText.substring(pointer, pos[0]));
            if (pos[1] > pointer) {
                pointer = pos[1];
            }
            output.writeTo(printer.toString());
            output.writeTo(origText.substring(pointer));
        } catch (Exception e) {
            // report an exception and do not any change in source to prevent deletion of code!
            Logger.getLogger("global").log(Level.SEVERE, "Error during generating!", e);
            this.output = new StringSourceRewriter();
            try {
                this.output.writeTo(origText);
            } catch (BadLocationException ex) {
            } catch (IOException ex) {
            }
        }
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
                printer.print("package ");
                printer.print(newT.pid);
                printer.print(";");
                printer.newline();
                break;
                
            // package statement was deleted.    
            case DELETE:
                TokenUtilities.movePrevious(tokenSequence, oldT.pid.getStartPosition());
                copyTo(localPointer, tokenSequence.offset());
                TokenUtilities.moveNext(tokenSequence, endPos(oldT.pid));
                localPointer = tokenSequence.offset() + 1;
                // todo (#pf): check the declaration:
                // package org.netbeans /* aa */;
                break;
    
            // package statement was modified.
            case MODIFY:
                copyTo(localPointer, getOldPos(oldT.pid));
                localPointer = endPos(oldT.pid);
                printer.print(newT.pid);
                diffInfo.put(getOldPos(oldT.pid), "Update package statement");
                break;
        }
        return localPointer;
    }
    
    protected void diffImport(JCImport oldT, JCImport newT) {
        if (TreeInfo.fullName(oldT.qualid) != TreeInfo.fullName(newT.qualid))
            append(Diff.modify(oldT, getOldPos(oldT), newT));  // includes possible staticImport change
        else if (oldT.staticImport != newT.staticImport)
            append(Diff.flags(oldT.pos, endPos(oldT), 
                              oldT.staticImport ? Flags.STATIC : 0L,
                              newT.staticImport ? Flags.STATIC : 0L));
    }

    // need by visitMethodDef - in case of renaming class, we do not know
    // this name in constructor matcher - save it in diffClassDef() and use
    // in diffMethodDef(). Probably better to write method with additional
    // parameter which delegates to original one visitMethodDef().
    private Name origClassName = null;
    
    protected int diffClassDef(JCClassDecl oldT, JCClassDecl newT, int[] bounds) {
        int localPointer =  bounds[0];
        int insertHint = localPointer;
        JCTree opar = oldParent;
        oldParent = oldT;
        JCTree npar = newParent;
        newParent = newT;
        // skip the section when printing anonymous class
        if (anonClass == false) {
        tokenSequence.move(oldT.pos);
        tokenSequence.moveNext(); // First skip as move() does not position to token directly
        tokenSequence.moveNext();
        insertHint = TokenUtilities.moveNext(tokenSequence, tokenSequence.offset());
        localPointer = diffModifiers(oldT.mods, newT.mods, oldT, localPointer);
        if (nameChanged(oldT.name, newT.name)) {
            printer.print(origText.substring(localPointer, insertHint));
            printer.print(newT.name);
            diffInfo.put(insertHint, "Change class name");
            localPointer = insertHint += oldT.name.length();
            origClassName = oldT.name;
        } else {
            insertHint += oldT.name.length();
        }
        localPointer = diffParameterList(oldT.typarams, newT.typarams, localPointer);
        if (oldT.typarams.nonEmpty()) {
            // if type parameters exists, compute correct end of type parameters.
            // ! specifies the offset for insertHint var.
            // public class Yerba<E, M>! { ...
            insertHint = endPos(oldT.typarams.last());
            TokenUtilities.moveFwdToToken(tokenSequence, insertHint, JavaTokenId.GT);
            insertHint = tokenSequence.offset() + JavaTokenId.GT.fixedText().length();
        }
        switch (getChangeKind(oldT.extending, newT.extending)) {
            case NOCHANGE:
                insertHint = oldT.extending != null ? endPos(oldT.extending) : insertHint;
                printer.print(origText.substring(localPointer, localPointer = insertHint));
                break;
            case MODIFY:
                copyTo(localPointer, getOldPos(oldT.extending));
                localPointer = diffTree(oldT.extending, newT.extending, getBounds(oldT.extending));
                break;
                
            case INSERT:
                printer.print(origText.substring(localPointer, insertHint));
                printer.print(" extends ");
                printer.print(newT.extending);
                localPointer = insertHint;
                break;
            case DELETE:
                printer.print(origText.substring(localPointer, insertHint));
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
            EstimatorFactory.implementz(((ClassTree) oldT).getImplementsClause(), ((ClassTree) newT).getImplementsClause(), workingCopy) : 
            EstimatorFactory.extendz(((ClassTree) oldT).getImplementsClause(), ((ClassTree) newT).getImplementsClause(), workingCopy);
        if (!newT.implementing.isEmpty())
            printer.print(origText.substring(localPointer, insertHint));
        localPointer = diffList2(oldT.implementing, newT.implementing, insertHint, estimator);
        insertHint = endPos(oldT) - 1;

        if (oldT.defs.isEmpty()) {
            // if there is nothing in class declaration, use position
            // before the closing curly.
            // TODO (#pf): optimize new lines, this will look ugly. --
            // do before the last new line character before the closing curly.
            insertHint = endPos(oldT) - 1;
        } else {
            // XXX hack: be careful, syntetic constructor in head has the
            // same position as class declaration too - go to the next feature.
            // do not be upset to me for the next line, I will replace it
            // with some better and final solution soon, hopefully :-).
            JCTree t = oldT.defs.head.pos == oldT.pos ? oldT.defs.tail.head : oldT.defs.head;
            if (t != null) insertHint = t.getStartPosition();
        }
        } else {
            insertHint = TokenUtilities.moveFwdToToken(tokenSequence, getOldPos(oldT), JavaTokenId.LBRACE);
            tokenSequence.moveNext();
            insertHint = tokenSequence.offset();
        }
        int old = printer.indent();
        VeryPretty mujPrinter = new VeryPretty(context, JavaFormatOptions.getDefault());
        mujPrinter.reset(old);
        mujPrinter.indent();
        mujPrinter.enclClassName = newT.getSimpleName();
        PositionEstimator est = EstimatorFactory.members(filterHidden(oldT.defs), filterHidden(newT.defs), workingCopy);
        int[] pos = diffList(filterHidden(oldT.defs), filterHidden(newT.defs), insertHint, est, Measure.DEFAULT, mujPrinter);
        if (localPointer < pos[0])
            printer.print(origText.substring(localPointer, pos[0]));
        printer.print(mujPrinter.toString());
        if (pos[1] != -1)
            printer.print(origText.substring(pos[1], bounds[1]));
        else
            printer.print(origText.substring(localPointer, bounds[1]));
        //pointer = bounds[1];
        oldParent = opar;
        newParent = npar;
        // the reference is no longer needed.
        origClassName = null;
        printer.undent(old);
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
                printer.print(origText.substring(localPointer, oldPos));
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
            VeryPretty locBuf = new VeryPretty(context);
            localPointer = diffParameterList(oldT.typarams,
                    newT.typarams,
                    oldT.typarams.isEmpty() || newT.typarams.isEmpty(),
                    pos,
                    locBuf
            );
            printer.print(locBuf.toString());
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
                printer.print(origText.substring(localPointer, oldT.pos));
                // use orig class name in case of constructor
                if (oldT.sym.isConstructor() && (origClassName != null)) {
                    printer.print(newT.name);
                    localPointer = oldT.pos + origClassName.length();
                }
                else {
                    printer.print(newT.name);
                    diffInfo.put(oldT.pos, "Rename method " + oldT.name);
                    localPointer = oldT.pos + oldT.name.length();
                }
            } else {
                printer.print(origText.substring(localPointer, localPointer = (oldT.pos + oldT.name.length())));
            }
        }
        if (oldT.params.isEmpty()) {
            // compute the position. Find the parameters closing ')', its
            // start position is important for us. This is used when 
            // there was not any parameter in original tree.
            int startOffset = oldT.restype != null ? oldT.restype.getStartPosition() : oldT.getStartPosition();
            
            TokenUtilities.moveFwdToToken(tokenSequence, startOffset, JavaTokenId.RPAREN);
            posHint = tokenSequence.offset();
        } else {
            // take the position of the first old parameter
            posHint = oldT.params.iterator().next().getStartPosition();
        }
        if (!listsMatch(oldT.params, newT.params)) {
            copyTo(localPointer, posHint);
            VeryPretty locBuf = new VeryPretty(context);
            locBuf.setPrec(TreeInfo.noPrec);
            localPointer = diffParameterList(oldT.params, newT.params, false, posHint, locBuf);
            printer.print(locBuf.toString());
        }
        // temporary
        tokenSequence.moveNext();
        posHint = tokenSequence.offset();
        if (localPointer < posHint)
            printer.print(origText.substring(localPointer, localPointer = posHint));
        // if abstract, hint is before ending semi-colon, otherwise before method body
        if (oldT.thrown.isEmpty()) {
            posHint = (oldT.body == null ? endPos(oldT) : oldT.body.pos) - 1;
            // now check, that there is a whitespace. It is not mandatory, we
            // have to ensure. If whitespace is not present, take body beginning
            tokenSequence.move(posHint);
            tokenSequence.moveNext();
            if (tokenSequence.token().id() != JavaTokenId.WHITESPACE) {
                ++posHint;
            }
        } else {
            posHint = oldT.thrown.iterator().next().getStartPosition();
        }
        //if (!newT.thrown.isEmpty())
            printer.print(origText.substring(localPointer, localPointer = posHint));
        PositionEstimator est = EstimatorFactory.throwz(((MethodTree) oldT).getThrows(), ((MethodTree) newT).getThrows(), workingCopy);
        localPointer = diffList2(oldT.thrown, newT.thrown, posHint, est);
        posHint = endPos(oldT) - 1;
        localPointer = diffTree(oldT.body, newT.body, localPointer);
        //diffTree(oldT.defaultValue, newT.defaultValue);
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffVarDef(JCVariableDecl oldT, JCVariableDecl newT, int[] bounds) {
       int localPointer = bounds[0];
        if (!matchModifiers(oldT.mods, newT.mods)) {
            // if new tree has modifiers, print them
            if (hasModifiers(newT.mods)) {
                localPointer = diffModifiers(oldT.mods, newT.mods, oldT, localPointer);
            } else {
                int oldPos = getOldPos(oldT.mods);
                copyTo(localPointer, oldPos);
                localPointer = getOldPos(oldT.vartype);
            }
        }
        int[] vartypeBounds = getBounds(oldT.vartype);
        copyTo(localPointer, vartypeBounds[0]);
        localPointer = diffTree(oldT.vartype, newT.vartype, vartypeBounds);
        if (nameChanged(oldT.name, newT.name)) {
            printer.print(origText.substring(localPointer, oldT.pos));
            printer.print(newT.name);
            diffInfo.put(oldT.pos, "Rename variable " + oldT.name);
            localPointer = oldT.pos + oldT.name.length();
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
                copyTo(localPointer, localPointer = endPos(oldT.init));
                printer.print(newT.init);
            }
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffBlock(JCBlock oldT, JCBlock newT, int lastPrinted) {
        int localPointer = lastPrinted;
        if (oldT.flags != newT.flags)
            append(Diff.flags(oldT.pos, endPos(oldT), oldT.flags, newT.flags));
        VeryPretty bodyPrinter = new VeryPretty(context, JavaFormatOptions.getDefault());
        int oldIndent = printer.indent();
        bodyPrinter.reset(oldIndent);
        bodyPrinter.indent();
        // syntetic super() found, skip it
        if (oldT.stats.head != null && oldT.stats.head.pos == oldT.pos) {
            oldT.stats = oldT.stats.tail;
        }
        if (newT.stats.head != null && newT.stats.head.pos == oldT.pos) {
            newT.stats = newT.stats.tail;
        }
        PositionEstimator est = EstimatorFactory.members(((BlockTree) oldT).getStatements(), ((BlockTree) newT).getStatements(), workingCopy); 
        int[] pos = diffList(oldT.stats, newT.stats, oldT.pos + 1, est, Measure.DEFAULT, bodyPrinter); // hint after open brace
        if (localPointer < pos[0]) {
            printer.print(origText.substring(localPointer, pos[0]));
        }
        localPointer = pos[1];
        printer.print(bodyPrinter.toString());
        if (localPointer < endPos(oldT)) {
            printer.print(origText.substring(localPointer, localPointer = endPos(oldT)));
        }
        printer.undent(oldIndent);
        return localPointer;
    }

    protected int diffDoLoop(JCDoWhileLoop oldT, JCDoWhileLoop newT, int[] bounds) {
        int localPointer = bounds[0];
        
        int[] bodyBounds = getBounds(oldT.body);
        copyTo(localPointer, bodyBounds[0]);
        localPointer = diffTree(oldT.body, newT.body, bodyBounds);
        
        int[] condBounds = getBounds(oldT.cond);
        copyTo(localPointer, condBounds[0]);
        localPointer = diffTree(oldT.cond, newT.cond, condBounds);
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
        int[] bodyPos = getBounds(oldT.body);
        copyTo(localPointer, bodyPos[0]);
        localPointer = diffTree(oldT.body, newT.body, bodyPos);
        
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffForLoop(JCForLoop oldT, JCForLoop newT, int[] bounds) {
        int localPointer = bounds[0];
        int initListHint = oldT.cond != null ? oldT.cond.pos - 1 : Query.NOPOS;
        int stepListHint = oldT.cond != null ? endPos(oldT.cond) + 1 : Query.NOPOS;
        copyTo(bounds[0], getOldPos(oldT.init.head));
        localPointer = diffList(oldT.init, newT.init, LineInsertionType.NONE, initListHint);
        copyTo(localPointer, getOldPos(oldT.cond));
        localPointer = diffTree(oldT.cond, newT.cond, getBounds(oldT.cond));
        if (oldT.step.nonEmpty()) 
            copyTo(localPointer, getOldPos(oldT.step.head));
        else
            copyTo(localPointer, stepListHint);
        localPointer = diffList(oldT.step, newT.step, LineInsertionType.NONE, stepListHint);
        copyTo(localPointer, getOldPos(oldT.body));
        localPointer = diffTree(oldT.body, newT.body, getBounds(oldT.body));
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
        int[] bodyBounds = getBounds(oldT.body);
        copyTo(localPointer, bodyBounds[0]);
        localPointer = diffTree(oldT.body, newT.body, bodyBounds);
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
        
        int castListHint = oldT.cases.size() > 0 ? oldT.cases.head.pos : Query.NOPOS;
        diffList(oldT.cases, newT.cases, LineInsertionType.BEFORE, castListHint);
        
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected void diffCase(JCCase oldT, JCCase newT) {
        diffTree(oldT.pat, newT.pat, getBounds(oldT.pat));
        diffList(oldT.stats, newT.stats, LineInsertionType.BEFORE, endPos(oldT) + 1); // after colon
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
        int pos = oldT.catchers.head != null ? getOldPos(oldT.catchers.head) : oldT.body.endpos + 1;
        VeryPretty locPrint = new VeryPretty(context);
        PositionEstimator est = EstimatorFactory.members(((TryTree) oldT).getCatches(), ((TryTree) newT).getCatches(), workingCopy);
        int[] retPos = diffList(oldT.catchers, newT.catchers, pos, est, Measure.DEFAULT, locPrint);
        if (localPointer < retPos[0]) {
            copyTo(localPointer, retPos[0]);
        }
        printer.print(locPrint.toString());
        localPointer = oldT.catchers.head != null ? endPos(oldT.catchers) : pos;
        if (oldT.finalizer != null) {
            int[] finalBounds = getBounds(oldT.finalizer);
            copyTo(localPointer, finalBounds[0]);
            localPointer = diffTree(oldT.finalizer, newT.finalizer, finalBounds);
            copyTo(localPointer, bounds[1]);
        } else if (retPos[1] < bounds[1]) {
            copyTo(localPointer, bounds[1]);
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
        if (oldT.elsepart == null && newT.elsepart != null ||
            oldT.elsepart != null && newT.elsepart == null) {
            // mark the whole if statement to be reformatted, which Commit will refine.
            append(Diff.modify(oldT, getOldPos(oldT), newT));
        } else {
            int[] condBounds = getBounds(oldT.cond);
            copyTo(localPointer, condBounds[0]);
            localPointer = diffTree(oldT.cond, newT.cond, condBounds);
            int[] thenpartBounds = getBounds(oldT.thenpart);
            copyTo(localPointer, thenpartBounds[0]);
            localPointer = diffTree(oldT.thenpart, newT.thenpart, thenpartBounds);
            if (oldT.elsepart != null) {
                thenpartBounds = new int[] { getOldPos(oldT.elsepart), endPos(oldT.elsepart) };
                copyTo(localPointer, thenpartBounds[0]);
                localPointer = diffTree(oldT.elsepart, newT.elsepart, thenpartBounds);
            }
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffExec(JCExpressionStatement oldT, JCExpressionStatement newT, int[] bounds) {
        int localPointer = getOldPos(oldT);
        copyTo(bounds[0], localPointer);
        localPointer = diffTree(oldT.expr, newT.expr, bounds);
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffBreak(JCBreak oldT, JCBreak newT, int[] bounds) {
        int localPointer = bounds[0];
        if (nameChanged(oldT.label, newT.label)) {
            copyTo(localPointer, localPointer = getOldPos(oldT));
            printer.print("break ");
            printer.print(newT.label);
            localPointer += 6 + oldT.label.length();
        }
//        int[] targetBounds = getBounds(oldT.target);
//        copyTo(localPointer, targetBounds[0]);
//        localPointer = diffTree(oldT.target, newT.target, targetBounds);
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }
        
    protected int diffContinue(JCContinue oldT, JCContinue newT, int[] bounds) {
        int localPointer = bounds[0];
        if (nameChanged(oldT.label, newT.label)) {
            copyTo(localPointer, localPointer = getOldPos(oldT));
            printer.print("continue ");
            printer.print(newT.label);
            localPointer += 9 + oldT.label.length();
        }
//        int[] targetBounds = getBounds(oldT.target);
//        copyTo(localPointer, targetBounds[0]);
//        localPointer = diffTree(oldT.target, newT.target, targetBounds);
        copyTo(localPointer, bounds[1]);
        
        return bounds[1];
    }

    protected int diffReturn(JCReturn oldT, JCReturn newT, int[] bounds) {
        int[] exprBounds = getBounds(oldT.expr);
        copyTo(bounds[0], exprBounds[0]);
        int localPointer = diffTree(oldT.expr, newT.expr, exprBounds);
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
        int[] detailBounds = getBounds(oldT.detail);
        copyTo(localPointer, detailBounds[0]);
        localPointer = diffTree(oldT.detail, newT.detail, detailBounds);
        copyTo(localPointer, bounds[1]);
        
        return bounds[1];
    }

    protected int diffApply(JCMethodInvocation oldT, JCMethodInvocation newT, int[] bounds) {
        int localPointer = bounds[0];
        diffParameterList(oldT.typeargs, newT.typeargs, localPointer);
        int[] methBounds = getBounds(oldT.meth);
        localPointer = diffTree(oldT.meth, newT.meth, methBounds);
        if (!listsMatch(oldT.args, newT.args)) {
            if (oldT.args.nonEmpty()) {
                copyTo(localPointer, localPointer = getOldPos(oldT.args.head));
            } else {
                int rParen = TokenUtilities.moveFwdToToken(tokenSequence, getOldPos(oldT.meth), JavaTokenId.RPAREN);
                copyTo(localPointer, localPointer = rParen);
            }
            VeryPretty buf = new VeryPretty(context, JavaFormatOptions.getDefault());
            localPointer = diffParameterList(oldT.args, newT.args, false, localPointer, buf);
            printer.print(buf.toString());
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
        diffParameterList(oldT.typeargs, newT.typeargs, localPointer);
        int[] clazzBounds = getBounds(oldT.clazz);
        copyTo(localPointer, clazzBounds[0]);
        localPointer = diffTree(oldT.clazz, newT.clazz, clazzBounds );
        localPointer = diffParameterList(oldT.args, newT.args, localPointer);
        // let diffClassDef() method notified that anonymous class is printed.
        if (oldT.def != null) {
            copyTo(localPointer, getOldPos(oldT.def));
            if (newT.def != null) {
                anonClass = true;
                localPointer = diffTree(oldT.def, newT.def, getBounds(oldT.def));
                anonClass = false;
            } else {
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
//        diffParameterList(oldT.dims, newT.dims, endPos(oldT.dims));
        if (oldT.elems != null && oldT.elems.head != null) {
            copyTo(localPointer, getOldPos(oldT.elems.head));
            localPointer = diffParameterList(oldT.elems, newT.elems, getOldPos(oldT.elems.head));
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffParens(JCParens oldT, JCParens newT, int[] bounds) {
        int localPointer = bounds[0];
        copyTo(localPointer, getOldPos(oldT.expr));
        localPointer = diffTree(oldT.expr, newT.expr, getBounds(oldT.expr));
        return localPointer;
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
        if (oldT.tag != newT.tag) { // todo (#pf): operatorName() does not work
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
        int localPointer = bounds[0];
        int[] argBounds = getBounds(oldT.arg);
        copyTo(localPointer, argBounds[0]);
        localPointer = diffTree(oldT.arg, newT.arg, argBounds);
        if (oldT.tag != newT.tag) {
            copyTo(localPointer, oldT.pos);
            printer.print(operatorName(newT.tag));
            localPointer = oldT.pos + operatorName(oldT.tag).length();
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffBinary(JCBinary oldT, JCBinary newT, int[] bounds) {
        int localPointer = bounds[0];
        
        int[] lhsBounds = getBounds(oldT.lhs);
        copyTo(localPointer, lhsBounds[0]);
        localPointer = diffTree(oldT.lhs, newT.lhs, lhsBounds);
        if (oldT.tag != newT.tag) {
            copyTo(localPointer, oldT.pos);
            printer.print(operatorName(newT.tag));
            localPointer = oldT.pos + operatorName(oldT.tag).toString().length();
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

    protected int diffSelect(JCFieldAccess oldT, JCFieldAccess newT, int[] bounds) {
        int localPointer = bounds[0];
        copyTo(localPointer, getOldPos(oldT.selected));
        localPointer = diffTree(oldT.selected, newT.selected, getBounds(oldT.selected));
        if (nameChanged(oldT.name, newT.name)) {
            copyTo(localPointer, endPos(oldT.selected));
            printer.print(".");
            printer.print(newT.name);
            diffInfo.put(endPos(oldT.selected) + 1, "Update reference to " + oldT.name);
            localPointer = endPos(oldT.selected) + 1 +oldT.name.length();
        }
        copyTo(localPointer, bounds[1]);
        return bounds[1];
    }

    protected int diffIdent(JCIdent oldT, JCIdent newT, int lastPrinted) {
        if (nameChanged(oldT.name, newT.name)) {
            copyTo(lastPrinted, oldT.pos);
            printer.print(newT.name);
            diffInfo.put(oldT.pos, "Update reference to " + oldT.name);
            return endPos(oldT);
        }
        return lastPrinted;
    }

    protected int diffLiteral(JCLiteral oldT, JCLiteral newT, int[] bounds) {
        if (oldT.typetag != newT.typetag || !oldT.value.equals(newT.value)) {
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

    protected void diffTypeIdent(JCPrimitiveTypeTree oldT, JCPrimitiveTypeTree newT) {
        if (oldT.typetag != newT.typetag)
            append(Diff.modify(oldT, getOldPos(oldT), newT));
    }

    protected int diffTypeArray(JCArrayTypeTree oldT, JCArrayTypeTree newT, int[] bounds) {
        int localPointer = bounds[0];
        copyTo(localPointer, bounds[0]);
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
            VeryPretty locBuf = new VeryPretty(context);
            localPointer = diffParameterList(
                    oldT.arguments,
                    newT.arguments,
                    oldT.arguments.isEmpty() || newT.arguments.isEmpty(),
                    pos,
                    locBuf
            );
            printer.print(locBuf.toString());
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
        // todo: finish parameters!
        // diffParameterList(oldT.bounds, newT.bounds, -1);
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
        copyTo(localPointer,annotationBounds[0]);
        localPointer = diffTree(oldT.annotationType, newT.annotationType, annotationBounds);
        diffParameterList(oldT.args, newT.args, -1);
        copyTo(localPointer, bounds[1]);
        
        return bounds[1];
    }
    
    protected int diffModifiers(JCModifiers oldT, JCModifiers newT, JCTree parent, int lastPrinted) {
        if (oldT == newT) {
            // modifiers wasn't changed, return the position lastPrinted.
            return lastPrinted;
        }
        int result = endPos(oldT.annotations);
        int oldPos = oldT.pos != Position.NOPOS ? getOldPos(oldT) : getOldPos(parent);
        if (listsMatch(oldT.annotations, newT.annotations)) {
            copyTo(lastPrinted, lastPrinted = oldPos);
            if (result > 0) {
                copyTo(lastPrinted, lastPrinted = result);
            } else {
            }
        } else {
            if (oldT.annotations.isEmpty()) copyTo(lastPrinted, oldPos);
            PositionEstimator est = EstimatorFactory.members(((ModifiersTree) oldT).getAnnotations(), ((ModifiersTree) newT).getAnnotations(), workingCopy);
            int[] res = diffList(oldT.annotations, newT.annotations, oldPos, est, Measure.DEFAULT, printer);
            lastPrinted = res[1];
            //printer.printAnnotations(newT.annotations);
//            if (result > 0) {
//                copyTo(lastPrinted, lastPrinted = result);
//            }
        }
        if (oldT.flags != newT.flags) {
            int endPos = endPos(oldT);
            if (endPos > 0) {
                printer.print(newT.toString().trim());
                lastPrinted = endPos;
            } else {
                printer.print(newT.toString());
            }
        }
        if (endPos(oldT) > 0) {
            copyTo(lastPrinted, endPos(oldT));
            return endPos(oldT);
        } else {
            return lastPrinted;
        }
    }
    
    protected void diffLetExpr(LetExpr oldT, LetExpr newT) {
        diffList(oldT.defs, newT.defs, LineInsertionType.NONE, Query.NOPOS);
        diffTree(oldT.expr, newT.expr, getBounds(oldT.expr));
    }
    
    protected void diffErroneous(JCErroneous oldT, JCErroneous newT) {
        diffList(oldT.errs, newT.errs, LineInsertionType.BEFORE, Query.NOPOS);
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
        if (t1.tag != t2.tag)
            return false;
        if (!deepMatch)
            return true;
        
        // don't use visitor, since we want fast-fail behavior
        switch (t1.tag) {
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
              return ((JCIdent)t1).sym == ((JCIdent)t2).sym;
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

    /**
     * Diff an unordered list, which may contain insertions and deletions.
     * REMOVE IT WHEN CORRECT LIST MATCHING WILL BE IMPLEMENTED
     */
    protected int diffList(List<? extends JCTree> oldList, 
                            List<? extends JCTree> newList, 
                            LineInsertionType newLine, int insertHint) {
        int lastPrinted = insertHint;
        if (oldList == newList)
            return insertHint;
        assert oldList != null && newList != null;
        int lastOldPos = insertHint;
        Iterator<? extends JCTree> oldIter = oldList.iterator();
        Iterator<? extends JCTree> newIter = newList.iterator();
        JCTree oldT = safeNext(oldIter);
        JCTree newT = safeNext(newIter);
        while (oldT != null && newT != null) {
            if (oldTopLevel != null) {
                int endPos = model.getEndPos(oldT, oldTopLevel);

                if (endPos != Position.NOPOS)
                    lastOldPos = endPos;
            }
            if (treesMatch(oldT, newT, false)) {
                lastPrinted  = diffTree(oldT, newT, new int[] { getOldPos(oldT), endPos(oldT) });
                oldT = safeNext(oldIter);
                newT = safeNext(newIter);
            }
            else if (!listContains(newList, oldT) && !listContains(oldList, newT)) {
                append(Diff.modify(oldT, getOldPos(oldT), newT));
                oldT = safeNext(oldIter);
                newT = safeNext(newIter);
            }
            else if (!listContains(newList, oldT)) {
                if (!isHidden(oldT, oldParent))
                    append(Diff.delete(oldT, getOldPos(oldT)));
                oldT = safeNext(oldIter);
            }
            else {
                if (!isHidden(newT, newParent))
                    append(Diff.insert(newT, getOldPos(oldT), newLine, null));
                newT = safeNext(newIter);
            }
        }
        while (oldT != null) {
            if (!isHidden(oldT, oldParent))
                append(Diff.delete(oldT, getOldPos(oldT)));
            if (oldTopLevel != null)
                lastOldPos = model.getEndPos(oldT, oldTopLevel);
            oldT = safeNext(oldIter);
        }
        while (newT != null) {
            if (!isHidden(newT, newParent))
                append(Diff.insert(newT, lastOldPos, newLine, null));
            newT = safeNext(newIter);
        }
        return lastPrinted;
    }
    
    private JCTree safeNext(Iterator<? extends JCTree> iter) {
        return iter.hasNext() ? iter.next() : null;
    }

    // XXX: this method should be removed later when all call will be
    // refactored and will use new list matching
    protected int diffParameterList(List<? extends JCTree> oldList, List<? extends JCTree> newList, int localPointer) {
        if (oldList == newList)
            return localPointer;
        assert oldList != null && newList != null;
        int lastOldPos = Query.NOPOS;
        Iterator<? extends JCTree> oldIter = oldList.iterator();
        Iterator<? extends JCTree> newIter = newList.iterator();
        while (oldIter.hasNext() && newIter.hasNext()) {
            JCTree oldT = oldIter.next();
            printer.print(origText.substring(localPointer, localPointer = getOldPos(oldT)));
            localPointer = diffTree(oldT, newIter.next(), new int[] { localPointer, endPos(oldT) });
            if (oldTopLevel != null)
                lastOldPos = model.getEndPos(oldT, oldTopLevel);
        }
        while (oldIter.hasNext()) {
            JCTree oldT = oldIter.next();
            append(Diff.delete(oldT, getOldPos(oldT)));
        }
        while (newIter.hasNext()) {
            append(Diff.insert(newIter.next(), lastOldPos, LineInsertionType.BEFORE));
        }
        return localPointer;
    }
    
    /**
     * Diff a ordered list for differences.
     */
    protected int diffList2(
        List<? extends JCTree> oldList, List<? extends JCTree> newList,
        int initialPos, PositionEstimator estimator) 
    {
        if (oldList == newList)
            return initialPos;
        assert oldList != null && newList != null;
        int lastOldPos = initialPos;
        
        ListMatcher<JCTree> matcher = ListMatcher.<JCTree>instance(
                (List<JCTree>) oldList, 
                (List<JCTree>) newList
        );
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
                        lastOldPos = endPos(oldT);
                    }
                    break;
                }
                case INSERT: {
                    String prec = s.head(j) ? estimator.head() : s.prev(j) ? estimator.sep() : null;
                    String tail = s.next(j) ? estimator.sep() : null;
                    if (estimator.getIndentString() != null && !estimator.getIndentString().equals(" ")) {
                        prec += estimator.getIndentString();
                    }
                    printer.print(origText.substring(lastOldPos, testPos));
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
                    printer.print(origText.substring(lastOldPos, lastOldPos = endPos(oldT)));
                    break;
                }
            }
        }
        return lastOldPos;
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
            boolean printParen,
            int pos,
            VeryPretty buf)
    {
        if (oldList == newList || (oldList.isEmpty() && newList.isEmpty()))
            return pos; // they match perfectly or no need to do anything
        
        assert oldList != null && newList != null;
        
        if (newList.isEmpty()) {
            int endPos = endPos(oldList);
            if (printParen) {
                tokenSequence.move(endPos);
                TokenUtilities.moveFwdToToken(tokenSequence, endPos, JavaTokenId.GT);
                tokenSequence.moveNext();
                endPos = tokenSequence.offset();
                if (!PositionEstimator.nonRelevant.contains(tokenSequence.token()))
                    buf.print(" "); // use options, if mods should be at new line
            }
            return endPos;
        }
        ListMatcher<JCTree> matcher = ListMatcher.<JCTree>instance(
                (List<JCTree>) oldList, 
                (List<JCTree>) newList
        );
        if (!matcher.match()) {
            // nothing in the list, no need to print and nothing was printed
            return pos; 
        }
        ResultItem<JCTree>[] result = matcher.getResult();
        if (printParen && oldList.isEmpty()) {
            buf.print(JavaTokenId.LT.fixedText());
        }
        JCTree lastDeleted = null;
        for (int index = 0, j = 0; j < result.length; j++) {
            ResultItem<JCTree> item = result[j];
            switch (item.operation) {
                // insert new element
                case INSERT:
                    if (index++ > 0) buf.print(",");
                    if (lastDeleted != null && treesMatch(lastDeleted, item.element, false)) {
                        VeryPretty mainPrint = this.printer;
                        this.printer = buf;
                        diffTree(lastDeleted, item.element, getBounds(lastDeleted));
                        this.printer = mainPrint;
                    } else {
                        buf.print(item.element);
                    }
                    lastDeleted = null;
                    break;
                case DELETE:
                    lastDeleted = item.element;
                    break;
                // just copy existing element
                case NOCHANGE:
                    if (index++ > 0) buf.print(",");
                    int[] bounds = getBounds(item.element);
                    tokenSequence.move(bounds[0]);
                    TokenUtilities.movePrevious(tokenSequence, bounds[0]);
                    tokenSequence.moveNext();
                    int start = tokenSequence.offset();
                    TokenUtilities.moveNext(tokenSequence, bounds[1]);
                    int end = tokenSequence.offset();
                    copyTo(start, end, buf);
                    lastDeleted = null;
                    break;
                default: 
                    break;
            }
        }
        if (printParen && oldList.isEmpty()) {
            buf.print(JavaTokenId.GT.fixedText());
            buf.print(" "); // part of options?
        }
        return oldList.isEmpty() ? pos : endPos(oldList);
    }
    
    /**
     * Used for diffing lists which does not contain any separator.
     * (Currently for imports and members diffing.)
     */
    private int[] diffList(
            List<? extends JCTree> oldList, 
            List<? extends JCTree> newList,
            int initialPos, 
            PositionEstimator estimator,
            Measure measure, 
            VeryPretty printer)
    {
        int[] ret = new int[] { -1, -1 };
        if (oldList == newList) {
            return ret;
        }
        assert oldList != null && newList != null;
        
        ListMatcher<JCTree> matcher = ListMatcher.instance(
                oldList, 
                newList,
                measure
        );
        if (!matcher.match()) {
            return ret;
        }
        JCTree lastdel = null; // last deleted element
        ResultItem<JCTree>[] result = matcher.getResult();
        int posHint = initialPos;
        int i = 0;
        for (int j = 0; j < result.length; j++) {
            ResultItem<JCTree> item = result[j];
            switch (item.operation) {
                case MODIFY: {
                    assert true : "Modify is no longer operated!";
                    break;
                }
                case INSERT: {
                    int pos = estimator.getInsertPos(i);
                    // estimator couldn't compute the position - probably
                    // first element is inserted to the collection
                    String head = "", tail = "";
                    if (pos < 0 && oldList.isEmpty() && i == 0) {
                        pos = initialPos;
                        StringBuilder aHead = new StringBuilder(), aTail = new StringBuilder();
                        pos = estimator.prepare(initialPos, aHead, aTail);
                        if (j+1 == result.length) {
                            tail = aTail.toString();
                        }
                        head = aHead.toString();
                        posHint = pos;
                        if (ret[0] < 0) ret[0] = posHint;
                        if (ret[1] < 0) ret[1] = posHint;
                    } else {
                        if (ret[0] < 0) ret[0] = posHint;
                        if (ret[1] < 0) ret[1] = posHint;
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
                                this.printer = new VeryPretty(context, JavaFormatOptions.getDefault());
                                this.printer.reset(old);
                                int index = oldList.indexOf(oldT);
                                int[] poss = estimator.getPositions(index);
                                diffTree(oldT, item.element, poss);
                                printer.print(this.printer.toString());
//                                if (pointer < poss[1])
//                                    printer.print(origText.substring(pointer, pointer = poss[1]));
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
                            this.printer = new VeryPretty(context, JavaFormatOptions.getDefault());
                            this.printer.reset(old);
                            int index = oldList.indexOf(lastdel);
                            int[] poss = estimator.getPositions(index);
                            diffTree(lastdel, item.element, poss);
                            printer.print(this.printer.toString());
                            this.printer = oldPrinter;
                            this.printer.undent(old);
                            break;
                        }
                        if (j == 0 && !oldList.isEmpty()) {
                            posHint = estimator.getPositions(0)[0];
                        }
                        printer.print(head);
                        int old = printer.indent();
                        VeryPretty inPrint = new VeryPretty(context, JavaFormatOptions.getDefault());
                        inPrint.reset(old);
                        inPrint.enclClassName = printer.enclClassName;
//                        inPrint.indent();
                        inPrint.print(item.element);
                            inPrint.newline();
                        printer.print(inPrint.toString());
                        printer.undent(old);
                        //printer.print(tail);
//                        if (j+1 != result.length) {
//                            printer.toColExactly(currentIndentLevel);
//                        }
                    }
                    break;
                }
                case DELETE: {
                    int[] pos = estimator.getPositions(i);
                    lastdel = oldList.get(i);
                    if (ret[0] < 0) {
                        ret[0] = pos[0];
                    }
                     ++i;
                    ret[1] = pos[1];
                    posHint = pos[1];
                    break;
                }
                case NOCHANGE: {
                    int[] pos = estimator.getPositions(i);
                    if (ret[0] < 0) {
                        ret[0] = pos[0];
                    }
                    printer.print(origText.substring(pos[0], pos[1]));
                    ret[1] = pos[1];
                    ++i;
                    break;
                }
            }
        }
        if (!oldList.isEmpty()) {
            Iterator<? extends JCTree> it = oldList.iterator();
            for (i = 0; it.hasNext(); i++, it.next()) ;
            int[] pos = estimator.getPositions(i);
            ret[1] = pos[1];
        }
        return ret;
    }
    
    private List filterHidden(List<JCTree> list) {
        List<JCTree> result = new ArrayList<JCTree>(); // todo (#pf): capacity?
        for (JCTree tree : list) {
            if (Kind.METHOD == tree.getKind()) {
                // filter syntetic constructors, i.e. constructors which are in
                // the tree, but not available in the source.
                if ((((JCMethodDecl)tree).mods.flags & Flags.GENERATEDCONSTR) != 0)
                    continue;
            }
            result.add(tree);
        }
        return result;
    }
    
    
    /**
     * Used for diffing lists which does not contain any separator, currently
     * just for imports.
     */
    private int diffListImports(
            List<? extends JCTree> oldList, 
            List<? extends JCTree> newList,
            int localPointer, 
            PositionEstimator estimator,
            Measure measure, 
            VeryPretty printer)
    {
        if (oldList == newList) {
            return localPointer;
        }
        assert oldList != null && newList != null;
        
        ListMatcher<JCTree> matcher = ListMatcher.instance(
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
            int pos = estimator.prepare(0, aHead, aTail);
            copyTo(localPointer, pos, printer);
            printer.print(aHead.toString());
            for (JCTree item : newList) {
                if (BEFORE == estimator.lineInsertType()) printer.newline();
                printer.printExpr(item);
                if (AFTER == estimator.lineInsertType()) printer.newline();
            }
            // this should be uncommented! -- support of empty line when
            // first import is added and there is only one empty line between
            // package statement and type decl 0.
            // printer.print(aTail.toString());
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
        copyTo(localPointer, localPointer = estimator.getInsertPos(0), printer);
        // go on, match it!
        for (int j = 0; j < result.length; j++) {
            ResultItem<JCTree> item = result[j];
            switch (item.operation) {
                case INSERT: {
                    int pos = estimator.getInsertPos(i);
                    // estimator couldn't compute the position - probably
                    // first element is inserted to the collection
                    String head = "", tail = "";
                    if (pos < 0 && oldList.isEmpty() && i == 0) {
                        pos = localPointer;
                        StringBuilder aHead = new StringBuilder(), aTail = new StringBuilder();
                        pos = estimator.prepare(localPointer, aHead, aTail);
                        if (j+1 == result.length) {
                            tail = aTail.toString();
                        }
                        head = aHead.toString();
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
                                this.printer = new VeryPretty(context, JavaFormatOptions.getDefault());
                                this.printer.reset(old);
                                int index = oldList.indexOf(oldT);
                                int[] poss = estimator.getPositions(index);
                                diffTree(oldT, item.element, poss);
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
                            this.printer = new VeryPretty(context, JavaFormatOptions.getDefault());
                            this.printer.reset(old);
                            int index = oldList.indexOf(lastdel);
                            int[] poss = estimator.getPositions(index);
                            diffTree(lastdel, item.element, poss);
                            printer.print(this.printer.toString());
                            this.printer = oldPrinter;
                            this.printer.undent(old);
                            break;
                        }
                        printer.print(head);
                        int old = printer.indent();
                        printer.enclClassName = printer.enclClassName;
                        if (LineInsertionType.BEFORE == estimator.lineInsertType()) printer.newline();
                        printer.print(item.element);
                        if (LineInsertionType.AFTER == estimator.lineInsertType()) printer.newline();
                        printer.undent(old);
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
    
    private boolean isHidden(JCTree t, JCTree parent) {
        if (parent == null)
            return false;
        //TODO: the test was originaly: t.pos == parent.pos, which caused problems when adding
        //member into class without non-syntetic constructors. See ConstructorTest.
        if (t.pos == Query.NOPOS)
            return true;
        return model.isSynthetic(t);
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
                    append(Diff.modify(oldT, newT, oldC, newC));
                    oldC = safeNext(oldIter);
                    newC = safeNext(newIter);
                } else {
                    append(Diff.delete(oldT, newT, oldC));
                    oldC = safeNext(oldIter);
                }
            }
            else {
                append(Diff.insert(lastPos, LineInsertionType.BEFORE, oldT, newT, newC, trailing));
                newC = safeNext(newIter);
            }
        }
        while (oldC != null) {
            append(Diff.delete(oldT, newT, oldC));
            oldC = safeNext(oldIter);
        }
        while (newC != null) {
            append(Diff.insert(lastPos, LineInsertionType.BEFORE, oldT, newT, newC, trailing));
            lastPos += newC.endPos() - newC.pos();
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
        switch (tree.tag) {
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
    
    private int getOldPos(JCTree oldT) {
        return getOldPos(oldT, model, undo);
    }
    
    static int getOldPos(JCTree oldT, ASTModel model, UndoList undo) {
        int oldPos = model.getStartPos(oldT);
        if (oldPos == Query.NOPOS) {
            // see if original tree is available for position
            JCTree t = (JCTree)undo.getOld(leftMostTree(oldT));
            if (t != null && t != oldT)
                // recurse in case there are multiple changes to this tree
                oldPos = getOldPos(t, model, undo);
        }
        if (oldPos == Query.NOPOS)
            oldPos = oldT.pos == Query.NOPOS ? leftMostTree(oldT).pos : oldT.pos;
        return oldPos;
    }
    
    /**
     * Create differences between trees. Old tree has to exist, i.e.
     * <code>oldT != null</code>. There is a one exception - when both
     * <code>oldT</code> and <code>newT</code> are null, then method
     * just returns.
     * 
     * @param  oldT  original tree in source code
     * @param  newT  tree to repace the original tree
     * @return position in original source
     */
    protected int diffTree(JCTree oldT, JCTree newT, int[] elementBounds) {
        if (oldT == null && newT != null)
            throw new IllegalArgumentException("Null is not allowed in parameters.");
 
        if (oldT == newT)
            return elementBounds[0];
        diffPrecedingComments(oldT, newT);
        int retVal = -1;
        int oldPos = getOldPos(oldT);

        if (oldT.tag != newT.tag) {
            if (((compAssign.contains(oldT.getKind()) && compAssign.contains(newT.getKind())) == false) &&
                ((binaries.contains(oldT.getKind()) && binaries.contains(newT.getKind())) == false) &&
                ((unaries.contains(oldT.getKind()) && unaries.contains(newT.getKind())) == false)) {
                // different kind of trees found, print the whole new one.
                printer.print(newT);
                return endPos(oldT);
            }
        }

        switch (oldT.tag) {
          case JCTree.TOPLEVEL:
              diffTopLevel((JCCompilationUnit)oldT, (JCCompilationUnit)newT);
              break;
          case JCTree.IMPORT:
              diffImport((JCImport)oldT, (JCImport)newT);
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
              break;
          case JCTree.BLOCK:
              retVal = diffBlock((JCBlock)oldT, (JCBlock)newT, elementBounds[0]);
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
              diffCase((JCCase)oldT, (JCCase)newT);
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
              retVal = diffIdent((JCIdent)oldT, (JCIdent)newT, elementBounds[0]);
              break;
          case JCTree.LITERAL:
              retVal = diffLiteral((JCLiteral)oldT, (JCLiteral)newT, elementBounds);
              break;
          case JCTree.TYPEIDENT:
              diffTypeIdent((JCPrimitiveTypeTree)oldT, (JCPrimitiveTypeTree)newT);
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
          default:
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

    private int diffTree(JCTree oldT, JCTree newT, int lastPrinted) {
        return diffTree(oldT, newT, new int[] { lastPrinted, -1 });
    }
    
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
        return t1.typetag == t2.typetag && t1.value == t2.value;
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
            throw new IllegalArgumentException("Illegal values: from = " + from + "; to = " + to + ".");
        }
        loc.print(origText.substring(from, to));
    }
    
    private static class Line {
        Line(String data, int start, int end) {
            this.start = start;
            this.end = end;
            this.data = data;
        }
        
        @Override
        public String toString() {
            return data.toString();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof Line) {
                return data.equals(((Line) o).data);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return data.hashCode();
        }
        
        String data;
        int end;
        int start;
    }
    
    private List<Line> getLines(String text) {
        char[] chars = text.toCharArray();
        List<Line> list = new ArrayList<Line>();
        int pointer = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\n') {
                list.add(new Line(new String(chars, pointer, i-pointer+1), pointer, i+1));
                pointer = i+1;
            }
        }
        if (pointer < chars.length) {
            list.add(new Line(new String(chars, pointer, chars.length-pointer), pointer, chars.length));
        }
        return list;
    }
    
    public List<Diff> makeListMatch(String text1, String text2) {
        List<Line> list1 = getLines(text1);
        List<Line> list2 = getLines(text2);
        Line[] lines1 = list1.toArray(new Line[list1.size()]);
        Line[] lines2 = list2.toArray(new Line[list2.size()]);
        
        List diffs = new ComputeDiff(lines1, lines2).diff();
        for (Object o : diffs) {
            Difference diff     = (Difference)o; // generify
            int delStart = diff.getDeletedStart();
            int delEnd   = diff.getDeletedEnd();
            int addStart = diff.getAddedStart();
            int addEnd   = diff.getAddedEnd();
            
            String from = toString(delStart, delEnd);
            String to = toString(addStart, addEnd);
            char type = delEnd != Difference.NONE && addEnd != Difference.NONE ? 'c' : (delEnd == Difference.NONE ? 'a' : 'd');

            if (logDiffs) {
                System.out.println(from + type + to);
                if (delEnd != Difference.NONE) {
                    printLines(delStart, delEnd, "<", lines1);
                    if (addEnd != Difference.NONE) {
                        System.out.println("---");
                    }
                }
                if (addEnd != Difference.NONE) {
                    printLines(addStart, addEnd, ">", lines2);
                }
            }
            // addition
            if (type == 'a') {
                StringBuilder builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                append(Diff.insert(delEnd == Difference.NONE ? lines1[delStart].start : lines1[delEnd].end,
                        builder.toString(), null, "", LineInsertionType.NONE));
            }
            
            // deletion
            else if (type == 'd') {
                append(Diff.delete(lines1[delStart].start, lines1[delEnd].end));
            }
            
            // change
            else { // type == 'c'
                StringBuilder builder = new StringBuilder();
                for (int i = delStart; i <= delEnd; i++) {
                    builder.append(lines1[i].data);
                }
                String match1 = builder.toString();
                builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                String match2 = builder.toString();
                makeTokenListMatch(match1, match2, lines1[delStart].start);
            }
        }
        return null;
    }
    
    /**
     * Temporary logging variable - just for debugging reason during development.
     */
    private static boolean logDiffs = false;
    
    public List<Diff> makeTokenListMatch(String text1, String text2, int currentPos) {
        if (logDiffs) System.out.println("----- token match for change -");
        TokenSequence<JavaTokenId> seq1 = TokenHierarchy.create(text1, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
        TokenSequence<JavaTokenId> seq2 = TokenHierarchy.create(text2, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
        List<Line> list1 = new ArrayList<Line>();
        List<Line> list2 = new ArrayList<Line>();
        while (seq1.moveNext()) {
            String data = seq1.token().text().toString();
            list1.add(new Line(data, seq1.offset(), seq1.offset() + data.length()));
        }
        while (seq2.moveNext()) {
            String data = seq2.token().text().toString();
            list2.add(new Line(data, seq2.offset(), seq2.offset() + data.length()));
        }
        Line[] lines1 = list1.toArray(new Line[list1.size()]);
        Line[] lines2 = list2.toArray(new Line[list2.size()]);
        
        List diffs = new ComputeDiff(lines1, lines2).diff();
        for (Object o : diffs) {
            Difference diff     = (Difference)o; // generify
            int delStart = diff.getDeletedStart();
            int delEnd   = diff.getDeletedEnd();
            int addStart = diff.getAddedStart();
            int addEnd   = diff.getAddedEnd();
            
            String from = toString(delStart, delEnd);
            String to = toString(addStart, addEnd);
            char type = delEnd != Difference.NONE && addEnd != Difference.NONE ? 'c' : (delEnd == Difference.NONE ? 'a' : 'd');

            if (logDiffs) {
                System.out.println(from + type + to);
                if (delEnd != Difference.NONE) {
                    printTokens(delStart, delEnd, "<", lines1);
                    if (addEnd != Difference.NONE) {
                        System.out.println("---");
                    }
                }
                if (addEnd != Difference.NONE) {
                    printTokens(addStart, addEnd, ">", lines2);
                }
            }
            // addition
            if (type == 'a') {
                StringBuilder builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                append(Diff.insert(currentPos + (delEnd == Difference.NONE ? lines1[delStart].start : lines1[delEnd].end),
                        builder.toString(), null, "", LineInsertionType.NONE));
            }
            
            // deletion
            else if (type == 'd') {
                append(Diff.delete(currentPos + lines1[delStart].start, currentPos + lines1[delEnd].end));
            }
            
            // change
            else { // type == 'c'
                StringBuilder builder = new StringBuilder();
                /*for (int i = delStart; i <= delEnd; i++) {
                    builder.append(lines1[i].data);
                }*/
                append(Diff.delete(currentPos + lines1[delStart].start, currentPos + lines1[delEnd].end));
                //builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                append(Diff.insert(currentPos + (delEnd == Difference.NONE ? lines1[delStart].start : lines1[delEnd].end),
                        builder.toString(), null, "", LineInsertionType.NONE));
            }
                    
        }
        if (logDiffs) System.out.println("----- end token match -");
        return null;
    }
    
    protected String toString(int start, int end) {
        // adjusted, because file lines are one-indexed, not zero.
        
        StringBuffer buf = new StringBuffer();
        
        // match the line numbering from diff(1):
        buf.append(end == Difference.NONE ? start : (1 + start));
        
        if (end != Difference.NONE && start != end) {
            buf.append(",").append(1 + end);
        }
        return buf.toString();
    }
    
    private void printLines(int start, int end, String ind, Line[] lines) {
        for (int lnum = start; lnum <= end; ++lnum) {
            System.out.print(ind + " " + lines[lnum]);
        }
    }
    
    private void printTokens(int start, int end, String ind, Line[] lines) {
        for (int lnum = start; lnum <= end; ++lnum) {
            System.out.println(ind + " '" + lines[lnum] + "'");
        }
    }
    
}
