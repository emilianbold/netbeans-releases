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

import org.netbeans.modules.java.source.engine.ASTModel;
import org.netbeans.modules.java.source.engine.TreeFinder;
import org.netbeans.modules.java.source.engine.RootTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.java.source.engine.RootTree;
import org.netbeans.modules.java.source.engine.TreeFinder;
import org.netbeans.modules.java.source.engine.ReattributionException;

import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.*;
import java.util.Collections;
import org.netbeans.api.java.source.transform.UndoEntry;
import org.netbeans.api.java.source.transform.UndoList;

import static com.sun.tools.javac.tree.JCTree.*;

/**
 * A javac abstract syntax tree which maps all nodes to a single root.
 */
public final class ASTService implements ASTModel {
    
    private RootTree root;
    private UndoList undoList;
    private TreeFactory treeFactory;
    private Name.Table names;
    private Symtab symtab;
    private ElementsService elements;
    private Source source;
    private Map<JavaFileObject, Map<JCTree, Integer>> endPosTables;
    
    // Debugging flag that verifies tree changes are correct after translation.
    private boolean reattribute = Boolean.getBoolean("jackpot.always.attribute"); //NOI18N

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
        undoList = UndoListService.instance(context);
        treeFactory = TreeFactory.instance(context);
        names = Name.Table.instance(context);
        symtab = Symtab.instance(context);
        elements = ElementsService.instance(context);
        source = Source.instance(context);
        endPosTables = new HashMap<JavaFileObject, Map<JCTree, Integer>>();
    }
    
    /**
     * Return the current root tree.
     */
    public Tree getRoot() {
        return root;
    }
    
    /**
     * Replace the current root tree.
     */
    public void setRoot(final RootTree tree) throws ReattributionException {
        if (tree == root)
            return;
        
        /* FIXME: port reattribution
        try {
            if (reattribute) {
                if (env instanceof ModifiableCommandEnvironment) {
                    Context newContext = Reattributer.reattribute(env, newRoot);
                    if (false) // turn off attribution resetting.
                        setContext(newContext);
                    else
                        model.setRoot((RootTree)newRoot);
                }
                else
                    throw new UnsupportedOperationException("Unmodifiable environment context");
            } else
                model.setRoot((RootTree)newRoot);
            env.getUndoList().setEndCommand(true);
        } catch (ReattributionException e) {
            UndoList undoList = env.getUndoList();
            if (!undoList.atEndCommand())
                // undo changes made by this transformer
                undoList.undo();
            error(e);
        }
         */
        
        undoList.addAndApply(new UndoEntry() {
            private final RootTree old = root;
            @Override
            public void undo() {
                root = old;
            }
            @Override
            public void redo() {
                root = tree;
            }
            @Override
            public <T> T getOld(T o) {
                return (o == tree) ? (T)old : null;
            }
        });
    }

    /*FIXME: reattribution support
    private void setContext(final Context newContext) {
        final Context oldContext = getContext();
        if (newContext == oldContext)
            return;
        UndoEntry u = new UndoEntry() {
            private final Context old = oldContext;
            @Override
            public void undo() {
                setContextImpl(old);
            }
            @Override
            public void redo() {
                setContextImpl(newContext);
            }
            @Override
            public <T> T getOld(T o) {
                return (o == newContext) ? (T)old : null;
            }
        };
	undoList.add(u); // UndoLists are shared between contexts
	u.redo();  // actually set the context
    }
    
    private void setContextImpl(Context newContext) {
        ((ModifiableCommandEnvironment)env).setContext(newContext);
    }
     */
    
    /**
     * Finds the defining tree for a symbol.
     */
    public Tree find(final Element s) {
        if (s == null)
            return null;
        final JCTree[] rtn = new JCTree[1];
        new TreeScanner<Void,Object>() {
            boolean found = false;
            @Override
                    public Void scan(Tree tree, Object p) {
                if(!found && tree != null) {
                    found = TreeInfo.symbolFor((JCTree)tree) == s;
                    if (found)
                        rtn[0] = (JCTree)tree;
                    else
                        super.scan(tree, null);
                }
                return null;
            }
        }.scan(root, null);
        return rtn[0];
    }
    
    /**
     * Finds the tree associated with a specified source file name.
     */
    public CompilationUnitTree findTopLevel(final String sourceFile) {
        final CompilationUnitTree[] rtn = new CompilationUnitTree[1];
        new TreeScanner<Void,Object>() {
            @Override
                    public Void visitCompilationUnit(CompilationUnitTree t, Object p) {
                if (rtn[0] == null && t.getSourceFile().toUri().getPath().endsWith(sourceFile))
                    rtn[0] = (JCCompilationUnit)t;
                else
                    super.visitCompilationUnit(t, p);
                return null;
            }
        }.scan(root, null);
        return rtn[0];
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
        switch (((JCTree)tree).tag) {
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

    /**
     * Returns true if this is an element for "this".
     */
    public boolean isThis(Element e) {
	return e != null ? isThis(((Symbol)e).name) : false;
    }

    public boolean isSynthetic(Tree tree) {
        if (tree == null)
            return false;
        long flags = 0L;
        JCTree t = (JCTree)tree;
        switch (t.tag) {
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
	switch(((JCTree)tree).tag) {
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
	switch(t.tag) {
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

    // Private methods
    
    private Tree findChild(Tree tree, Symbol s) {
        for (Tree child : getChildren(tree)) {
            if (getElement(child) == s)
                return child;
        }
        return null;
    }
    
    /**
     * Find a child symbol based on name and optional parameter types.
     *
     * @param s the parent symbol
     * @param child the name of the child
     * @param parameters the parameter type list, if the child is a method; 
     *                   if it is not, then this parameter is null.
     */
    private Symbol findChild(Symbol s, String child, String[] parameters) {
        com.sun.tools.javac.code.Scope scope = s.members();
        com.sun.tools.javac.code.Scope.Entry e = scope.lookup(names.fromString(child));
        if (e != null && parameters != null) {
            // search for method with same parameter types
            while (e.scope == scope) {
                if (e.sym instanceof MethodSymbol) {
                    MethodSymbol meth = (MethodSymbol)e.sym;
                    if (compareParams(meth.params(), parameters))
                        return meth;
                }
                e = e.next();
            }
            return null;
        }
	return e != null ? e.sym : null;
    }
       
    private boolean compareParams(List<VarSymbol> p1, String[] p2) {
        if (p1.size() != p2.length)
            return false;
        int i = 0;
        for (VarSymbol var : p1) {
            String s1 = var.type.toString();
            String s2 = p2[i++];
            if (!s1.equals(s2))
                return false;
        }
        return true;
    }
    
    public boolean isStatic(Tree tree) {
        if (tree == null)
            return false;
        Symbol sym = (Symbol)getElement(tree);
        return sym != null ? (sym.flags() & Flags.STATIC) != 0 : false;
    }
    
    /**
     * Returns how many references to instance variables or methods there are
     * in a given tree.
     */
    public int getInstanceReferenceCount(Tree t) {
        if (t == null)
            return 0;
        return elements.getCharacterization(getElement(t)).getThisUseCount();
    }
    
    public void setPos(Tree tree, int newPos) {
        ((JCTree)tree).pos = newPos;
    }
    
    public SourceVersion sourceVersion() {
        return Source.toSourceVersion(source);
    }
    
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
