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
package org.netbeans.modules.java.source.builder;

import org.netbeans.modules.java.source.engine.TreeFinder;
import org.netbeans.modules.java.source.engine.RootTree;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.java.source.engine.RootTree;
import org.netbeans.modules.java.source.engine.TreeFinder;
import org.netbeans.modules.java.source.engine.ReattributionException;

import com.sun.source.tree.*;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.*;
import java.util.Collections;

import static com.sun.tools.javac.tree.JCTree.*;

/**
 * A javac abstract syntax tree which maps all nodes to a single root.
 */
public final class ASTService {
    
    private RootTree root;
    private RootTree oldRoot;
    private Map<JavaFileObject, Map<JCTree, Integer>> endPosTables;

    private static final Context.Key<ASTService> treeKey = new Context.Key<ASTService>();
    
    public static synchronized ASTService instance(Context context) {
        ASTService instance = context.get(treeKey);
        if (instance == null)
            instance = new ASTService(context);
        return instance;
    }
    
    /**
     * Create a new Trees, using an existing root node.
     */
    protected ASTService(Context context) {
        context.put(treeKey, this);
        endPosTables = new HashMap<JavaFileObject, Map<JCTree, Integer>>();
    }
    
    /**
     * Return the current root tree.
     */
    public Tree getRoot() {
        return root;
    }
    
    public Tree getOldRoot() {
        return oldRoot;
    }
    
    /**
     * Replace the current root tree.
     */
    @SuppressWarnings("unchecked")
    public void setRoot(final RootTree tree) throws ReattributionException {
        if (tree == root)
            return;
        oldRoot = root;
        root = tree;
    }
    
    /**
     * Return the JCCompilationUnit parent of a specified tree.
     *
     * @return the JCCompilationUnit, or null if tree is a PackageDef.
     */
    public CompilationUnitTree getTopLevel(Tree tree) {
        if (tree == null)
            return null;
        Tree[] path = makePath(root, tree);
        for (int i = 0; i < path.length; i++)
            if (path[i] instanceof CompilationUnitTree)
                return (CompilationUnitTree)path[i];
        assert tree instanceof RootTree;
        return null;
    }
    
    /**
     * Returns the element for a specified tree.  Null is returned if the
     * tree type doesn't have an associated element, or if the reference
     * is not resolved.
     */
    public Element getElement(Tree tree) {
        if (tree == null)
            return null;
        switch (tree.getKind()) {
            case COMPILATION_UNIT: return ((JCCompilationUnit)tree).packge;
            case CLASS:            return ((JCClassDecl)tree).sym;
            case METHOD:           return ((JCMethodDecl)tree).sym;
            case VARIABLE:         return ((JCVariableDecl)tree).sym;
            case MEMBER_SELECT:    return ((JCFieldAccess)tree).sym;
            case IDENTIFIER:       return ((JCIdent)tree).sym;
            case NEW_CLASS:        return ((JCNewClass)tree).constructor;
            default:
                return null;
        }
    }
    
    public TypeMirror getType(Tree tree) {
        if (tree == null || tree instanceof RootTree)
            return null;
        TypeMirror type = ((JCTree)tree).type;
        if (type == null) {
            Element e = getElement(tree);
            if (e != null)
                type = e.asType();
        }
        return type;
    }
    
    /**
     * Sets the element associated with a Tree.  This should only be done
     * either on trees created by TreeMaker or clone(), and never on original
     * trees.
     *
     * @see org.netbeans.api.java.source.TreeMaker
     * @see #clone
     */
    public void setElement(Tree tree, Element element) {
        switch (((JCTree)tree).getTag()) {
            case TOPLEVEL:
                ((JCCompilationUnit)tree).packge = (Symbol.PackageSymbol)element;
                break;
            case CLASSDEF:
                ((JCClassDecl)tree).sym = (Symbol.ClassSymbol)element;
                break;
            case METHODDEF:
                ((JCMethodDecl)tree).sym = (Symbol.MethodSymbol)element;
                break;
            case VARDEF:
                ((JCVariableDecl)tree).sym = (Symbol.VarSymbol)element;
                break;
            case SELECT:
                ((JCFieldAccess)tree).sym = (Symbol)element;
                break;
            case IDENT:
                ((JCIdent)tree).sym = (Symbol)element;
                break;
            case NEWCLASS:
                ((JCNewClass)tree).constructor = (Symbol)element;
                break;
            default:
                throw new IllegalArgumentException("invalid tree type: " + tree.getKind());
        }
    }
    
    /**
     * Sets the TypeMirror associated with a Tree.  This should only be done
     * either on trees created by TreeMaker or clone(), and never on original
     * trees.
     *
     * @see org.netbeans.api.java.source.TreeMaker
     * @see #clone
     */
    public void setType(Tree tree, TypeMirror type) {
        ((JCTree)tree).type = (Type)type;
    }
    
    /**
     * Returns true if this is an identifier for "this".
     */
    public boolean isThis(IdentifierTree t) {
	return t instanceof IdentifierTree && isThis(((JCIdent)t).name);
    }

    private static boolean isThis(Name nm) {
	return nm==nm.table._this;
    }

    public boolean isSynthetic(Tree tree) {
        if (tree == null)
            return false;
        long flags = 0L;
        JCTree t = (JCTree)tree;
        switch (t.getTag()) {
          case JCTree.CLASSDEF:
            flags = ((JCClassDecl)t).mods.flags;
            break;
          case JCTree.METHODDEF:
            flags = ((JCMethodDecl)t).mods.flags;
            break;
          case JCTree.VARDEF:
            flags = ((JCVariableDecl)t).mods.flags;
            break;
          case JCTree.BLOCK:
            if (t.pos == Position.NOPOS)
                return true;
            flags = ((JCBlock)t).flags;
            break;
          case JCTree.MODIFIERS:
            if (t.pos == Position.NOPOS)
                return true;
        }
        return (flags & Flags.SYNTHETIC) != 0L;        
    }

    /**
     * Returns a tree path from a specified root tree to a specified
     * target tree.  If the target is not a child of the root tree,
     * then a zero-length array is returned.
     */
    public Tree[] makePath(final Tree root, final Tree target) {
        final Stack<Tree> stack = new Stack<Tree>();
        root.accept(new TreeFinder(target) {
            @Override
            public Boolean scan(Tree tree, Object o) {
                super.scan(tree, o);
                if (found)
                    stack.push(tree);
                return found;
            }
        }, null);
        Tree[] path = new Tree[stack.size()];
        for (int i = 0; i < path.length; i++)
            path[i] = stack.pop();
        return path;
    }
    
    /**
     * Get the position for a tree node.  
     */
    public int getPos(Tree tree) {
        if (tree == null)
	    return Position.NOPOS;
        return ((JCTree)tree).pos;
    }

    /** 
     * Get the start position for a tree node.  
     */
    public int getStartPos(Tree tree) {
	if (tree == null)
	    return Position.NOPOS;
	switch(((JCTree)tree).getTag()) {
            case(JCTree.APPLY):
                return getStartPos(((JCMethodInvocation)tree).meth);
            case(JCTree.ASSIGN):
                return getStartPos(((JCAssign)tree).lhs);
            case(JCTree.BITOR_ASG): case(JCTree.BITXOR_ASG): case(JCTree.BITAND_ASG):
            case(JCTree.SL_ASG): case(JCTree.SR_ASG): case(JCTree.USR_ASG):
            case(JCTree.PLUS_ASG): case(JCTree.MINUS_ASG): case(JCTree.MUL_ASG):
            case(JCTree.DIV_ASG): case(JCTree.MOD_ASG):
                return getStartPos(((JCAssignOp)tree).lhs);
            case(JCTree.OR): case(JCTree.AND): case(JCTree.BITOR):
            case(JCTree.BITXOR): case(JCTree.BITAND): case(JCTree.EQ):
            case(JCTree.NE): case(JCTree.LT): case(JCTree.GT):
            case(JCTree.LE): case(JCTree.GE): case(JCTree.SL):
            case(JCTree.SR): case(JCTree.USR): case(JCTree.PLUS):
            case(JCTree.MINUS): case(JCTree.MUL): case(JCTree.DIV):
            case(JCTree.MOD):
                return getStartPos(((JCBinary)tree).lhs);
            case(JCTree.CLASSDEF): {
                JCClassDecl node = (JCClassDecl)tree;
                if (node.mods.pos != Position.NOPOS)
                    return node.mods.pos;
                break;
            }
            case(JCTree.CONDEXPR):
                return getStartPos(((JCConditional)tree).cond);
            case(JCTree.EXEC):
                return getStartPos(((JCExpressionStatement)tree).expr);
            case(JCTree.INDEXED):
                return getStartPos(((JCArrayAccess)tree).indexed);
            case(JCTree.METHODDEF): {
                JCMethodDecl node = (JCMethodDecl)tree;
                if (node.mods.pos != Position.NOPOS)
                    return node.mods.pos;
                if (node.restype != null) // true for constructors
                    return getStartPos(node.restype);
                return node.pos;
            }
            case(JCTree.SELECT):
                return getStartPos(((JCFieldAccess)tree).selected);
            case(JCTree.TYPEAPPLY):
                return getStartPos(((JCTypeApply)tree).clazz);
            case(JCTree.TYPEARRAY):
                return getStartPos(((JCArrayTypeTree)tree).elemtype);
            case(JCTree.TYPETEST):
                return getStartPos(((JCInstanceOf)tree).expr);
            case(JCTree.POSTINC):
            case(JCTree.POSTDEC):
                return getStartPos(((JCUnary)tree).arg);
            case(JCTree.VARDEF): {
                JCVariableDecl node = (JCVariableDecl)tree;
                if (node.mods.pos != Position.NOPOS)
                    return node.mods.pos;
                return getStartPos(node.vartype);
            }
	}
	return ((JCTree)tree).pos;
    }
		    
    /** 
     * Get the end position for a tree node.
     */
    public int getEndPos(Tree tree, CompilationUnitTree topLevel) {
        Map<JCTree,Integer> endPositions = 
            topLevel != null ? endPosTables.get(topLevel.getSourceFile()) : null;
	if (tree == null || endPositions == null)
	    return Position.NOPOS;

	Integer mapPos = endPositions.get(tree);
	if (mapPos != null)
	    return mapPos;

        JCTree t = (JCTree)tree;
	switch(t.getTag()) {
	    case(JCTree.BITOR_ASG): case(JCTree.BITXOR_ASG): case(JCTree.BITAND_ASG):
	    case(JCTree.SL_ASG): case(JCTree.SR_ASG): case(JCTree.USR_ASG):
	    case(JCTree.PLUS_ASG): case(JCTree.MINUS_ASG): case(JCTree.MUL_ASG):
	    case(JCTree.DIV_ASG): case(JCTree.MOD_ASG):
		return getEndPos(((JCAssignOp)tree).rhs, topLevel);
	    case(JCTree.OR): case(JCTree.AND): case(JCTree.BITOR):
	    case(JCTree.BITXOR): case(JCTree.BITAND): case(JCTree.EQ):
	    case(JCTree.NE): case(JCTree.LT): case(JCTree.GT):
	    case(JCTree.LE): case(JCTree.GE): case(JCTree.SL):
	    case(JCTree.SR): case(JCTree.USR): case(JCTree.PLUS):
	    case(JCTree.MINUS): case(JCTree.MUL): case(JCTree.DIV):
	    case(JCTree.MOD):
		return getEndPos(((JCBinary)tree).rhs, topLevel);
	    case(JCTree.CASE):
		return getEndPos(((JCCase)tree).stats.last(), topLevel);
	    case(JCTree.CATCH):
		return getEndPos(((JCCatch)tree).body, topLevel);
	    case(JCTree.EXEC):
                return getEndPos(((JCExpressionStatement)tree).expr, topLevel);
	    case(JCTree.CONDEXPR):
		return getEndPos(((JCConditional)tree).falsepart, topLevel);
	    case(JCTree.FORLOOP):
		if (tree instanceof JCForLoop) {
		    return getEndPos(((JCForLoop)tree).body, topLevel);
		} else {
		    return getEndPos(((JCEnhancedForLoop)tree).body, topLevel);
		}
	    case(JCTree.IDENT):
		return t.pos + ((JCIdent)tree).name.len;
	    case(JCTree.IF): {
		JCIf node = (JCIf)tree;
		if (node.elsepart == null) {
		    return getEndPos(node.thenpart, topLevel);
		} else {
		    return getEndPos(node.elsepart, topLevel);
		}
	    }
            case(JCTree.FOREACHLOOP):
                return getEndPos(((JCEnhancedForLoop)tree).body, topLevel);
	    case(JCTree.LABELLED):
		return getEndPos(((JCLabeledStatement)tree).body, topLevel);
	    case(JCTree.MODIFIERS):
		return getEndPos(((JCModifiers)tree).annotations.last(), topLevel);
	    case(JCTree.SELECT): {
		JCFieldAccess select = (JCFieldAccess)tree;
		return getEndPos(select.selected, topLevel) + 1 /*'.'*/ + select.name.len;
	    }
	    case(JCTree.SYNCHRONIZED):
		return getEndPos(((JCSynchronized)tree).body, topLevel);
	    case(JCTree.TOPLEVEL):
		return getEndPos(((JCCompilationUnit)tree).defs.last(), topLevel);
	    case(JCTree.TRY): {
		JCTry node = (JCTry)tree;
		if (node.finalizer == null) {
		    return getEndPos(node.catchers.last(), topLevel);
		} else {
		    return getEndPos(node.finalizer, topLevel);
		}
	    }
	    case(JCTree.TYPECAST):
		return getEndPos(((JCTypeCast)tree).expr, topLevel);
	    case(JCTree.TYPETEST):
		return getEndPos(((JCInstanceOf)tree).clazz, topLevel);
	    case(JCTree.WILDCARD):
		return getEndPos(((JCWildcard)tree).inner, topLevel);
	    case(JCTree.POS):
	    case(JCTree.NEG):
	    case(JCTree.NOT):
	    case(JCTree.COMPL):
	    case(JCTree.PREINC):
	    case(JCTree.PREDEC):
		return getEndPos(((JCUnary)tree).arg, topLevel);
	    case(JCTree.WHILELOOP):
		return getEndPos(((JCWhileLoop)tree).body, topLevel);
	}
	return Position.NOPOS;
    }
    
    public void setPos(Tree tree, int newPos) {
        ((JCTree)tree).pos = newPos;
    }

    /** sets the end position table which is generated during parsing. */
    public void setEndPosTable(JavaFileObject name, Map<JCTree, Integer> table) {
        endPosTables.put(name, table);
    }

    public java.util.List<? extends Tree> getChildren(Tree tree) {
        if (tree instanceof RootTree)
            return ((RootTree)tree).getCompilationUnits();
        if (tree instanceof CompilationUnitTree)
            return ((CompilationUnitTree)tree).getTypeDecls();
        if (tree instanceof ClassTree)
            return ((ClassTree)tree).getMembers();
        return Collections.emptyList();
    }

}
