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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.java.Utilities;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.ErrorManager;

/**
 *
 * @author Jan Lahoda
 */
public final class UncaughtException implements ErrorRule<Void> {
    
    /**
     * Creates a new instance of UncaughtExceptionCreator
     */
    public UncaughtException() {
    }

    private List<? extends TypeMirror> findUncauchedExceptions(CompilationInfo info, TreePath path, List<? extends TypeMirror> exceptions) {
        List<TypeMirror> result = new ArrayList<TypeMirror>();
        
        result.addAll(exceptions);
        
        while (path != null) {
            Element currentElement = info.getTrees().getElement(path);
            
            if (currentElement != null && EXECUTABLE_ELEMENTS.contains(currentElement.getKind())) {
                ExecutableElement ee = (ExecutableElement) currentElement;
                
                result.removeAll(ee.getThrownTypes());
                break;
            }
            
            Tree currentTree = path.getLeaf();
            
            if (currentTree.getKind() == Kind.TRY) {
                TryTree tt = (TryTree) currentTree;
                
                for (CatchTree c : tt.getCatches()) {
                    TreePath catchPath = new TreePath(new TreePath(path, c), c.getParameter());
                    VariableElement variable = (VariableElement) info.getTrees().getElement(catchPath);
                    
                    result.remove(variable.asType());
                }
            }
            
            path = path.getParentPath();
        }
        
        return result;
    }
    
    public Set<String> getCodes() {
        return Collections.singleton("compiler.err.unreported.exception.need.to.catch.or.throw");
    }
    
    @SuppressWarnings("fallthrough")
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        List<Fix> result = new ArrayList<Fix>();
        TreePath path = info.getTreeUtilities().pathFor(offset + 1);
        List<? extends TypeMirror> uncauched = null;
        boolean disableSurroundWithTryCatch = false;
        Element el;
        
        OUTTER: while (path != null) {
            Tree leaf = path.getLeaf();
            
            switch (leaf.getKind()) {
                case METHOD_INVOCATION:
                    //check for super/this constructor call (and disable surround with try-catch):
                    MethodInvocationTree mit = (MethodInvocationTree) leaf;
                    
                    if (mit.getMethodSelect().getKind() == Kind.IDENTIFIER) {
                        String ident = ((IdentifierTree) mit.getMethodSelect()).getName().toString();
                        
                        if ("super".equals(ident) || "this".equals(ident)) {
                            Element element = info.getTrees().getElement(path);
                            
                            disableSurroundWithTryCatch = element != null && element.getKind() == ElementKind.CONSTRUCTOR;
                        }
                    }
                case NEW_CLASS:
                    el = info.getTrees().getElement(path);
                    if (el != null && EXECUTABLE_ELEMENTS.contains(el.getKind())) {
                        uncauched = ((ExecutableElement) el).getThrownTypes();
                    }
                    path = path.getParentPath();
                    break OUTTER;
                case THROW:
                    TypeMirror uncaughtException = info.getTrees().getTypeMirror(new TreePath(path, ((ThrowTree) leaf).getExpression()));
                    uncauched = Collections.singletonList(uncaughtException);
                    break OUTTER;
            }
            
            path = path.getParentPath();
        }
        
        if (uncauched != null) {
            uncauched = findUncauchedExceptions(info, path, uncauched);
            
            TreePath pathRec = path;
            
            while (pathRec != null && pathRec.getLeaf().getKind() != Kind.METHOD) {
                pathRec = pathRec.getParentPath();
            }
            
            ExecutableElement method = pathRec != null ? (ExecutableElement) info.getTrees().getElement(pathRec)  : null;
            
            if (method != null) {
                //if the method header is inside a guarded block, do nothing:
                if (!org.netbeans.modules.java.hints.errors.Utilities.isMethodHeaderInsideGuardedBlock(info, (MethodTree) pathRec.getLeaf())) {
                    for (TypeMirror tm : uncauched) {
                        if (tm.getKind() != TypeKind.ERROR) {
                            result.add(new AddThrowsClauseHintImpl(info.getJavaSource(), Utilities.getTypeName(tm, true).toString(), TypeMirrorHandle.create(tm), ElementHandle.create(method)));
                        }
                    }
                }
            }
            
            if (!uncauched.isEmpty() && !disableSurroundWithTryCatch) {
                List<TypeMirrorHandle> thandles = new ArrayList<TypeMirrorHandle>();
                
                for (TypeMirror tm : uncauched) {
                    if (tm.getKind() != TypeKind.ERROR)
                        thandles.add(TypeMirrorHandle.create(tm));
                }
                result.add(new MagicSurroundWithTryCatchFix(info.getJavaSource(), thandles, offset));
            }
        }
        
        return result;
    }
    
    public void cancel() {
        //XXX: not done yet
    }
    
    public String getId() {
        return UncaughtException.class.getName();
    }
    
    public String getDisplayName() {
        return "Add Throws Clause and Surround With try-catch Fixes";
    }
    
    public String getDescription() {
        return "Add Throws Clause and Surround With try-catch Fixes";
    }
    
    private static final Set<ElementKind> EXECUTABLE_ELEMENTS = EnumSet.of(ElementKind.CONSTRUCTOR, ElementKind. METHOD);
    
    private static final class AddThrowsClauseHintImpl implements Fix {
        
        private JavaSource js;
        private String fqn;
        private TypeMirrorHandle thandle;
        private ElementHandle<ExecutableElement> method;
        
        public AddThrowsClauseHintImpl(JavaSource js, String fqn, TypeMirrorHandle thandle, ElementHandle<ExecutableElement> method) {
            this.js = js;
            this.fqn = fqn;
            this.thandle = thandle;
            this.method = method;
        }
        
        public String getText() {
            return "Add throws clause for " + fqn;
        }
        
        public ChangeInfo implement() {
            try {
                js.runModificationTask(new Task<WorkingCopy>() {

                    public void run(WorkingCopy wc) throws Exception {
                        wc.toPhase(Phase.RESOLVED);
                        Tree tree = wc.getTrees().getTree(method.resolve(wc));
                        
                        assert tree != null;
                        assert tree.getKind() == Kind.METHOD;
                        
                        MethodTree nue = wc.getTreeMaker().addMethodThrows((MethodTree) tree, (ExpressionTree) wc.getTreeMaker().Type(thandle.resolve(wc)));
                        
                        wc.rewrite(tree, nue);
                    }
                }).commit();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            return null;
        }
        
    }
    
    private static final class SurroundWithTryCatch implements Fix {
        
        private JavaSource js;
        private List<TypeMirrorHandle> thandles;
        private int offset;
        
        public SurroundWithTryCatch(JavaSource js, List<TypeMirrorHandle> thandles, int offset) {
            this.js = js;
            this.thandles = thandles;
            this.offset = offset;
        }
        
        public String getText() {
            return "Surround with try-catch";
        }
        
        public ChangeInfo implement() {
            try {
                js.runModificationTask(new Task<WorkingCopy>() {

                    public void run(WorkingCopy wc) throws Exception {
                        wc.toPhase(Phase.RESOLVED);
                        TreePath currentPath = wc.getTreeUtilities().pathFor(offset + 1);
                        
                        //find statement:
                        while (currentPath != null && !STATEMENT_KINDS.contains(currentPath.getLeaf().getKind()))
                            currentPath = currentPath.getParentPath();
                        
                        TreeMaker make = wc.getTreeMaker();
                        Tree t = currentPath.getLeaf();
                        BlockTree bt = make.Block(Collections.singletonList((StatementTree) t), false);
                        List<CatchTree> catches = new ArrayList<CatchTree>();
                        
                        for (TypeMirrorHandle th : thandles) {
                            catches.add(make.Catch(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "ex", make.Type(th.resolve(wc)), null), make.Block(Collections.<StatementTree>emptyList(), false)));
                        }
                        wc.rewrite(t, make.Try(bt, catches, null));
                    }
                }).commit();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
            return null;
        }
        
    }
    
    static final Set<Kind> STATEMENT_KINDS;
    
    static {
        Set<Kind> kinds = new HashSet<Kind>();
        
        for (Kind k : Kind.values()) {
            Class c = k.asInterface();
            
            if (c != null && StatementTree.class.isAssignableFrom(c)) {
                kinds.add(k);
            }
        }
        
        STATEMENT_KINDS = Collections.unmodifiableSet(EnumSet.copyOf(kinds));
    }
    
}
