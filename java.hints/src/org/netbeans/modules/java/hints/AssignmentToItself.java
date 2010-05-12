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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 */
public class AssignmentToItself extends AbstractHint {

    private final AtomicBoolean cancel = new AtomicBoolean();
    
    private Set<Kind> KINDS = Collections.<Tree.Kind>singleton(Tree.Kind.ASSIGNMENT);
    
    public AssignmentToItself() {
        super( true, true, HintSeverity.WARNING, "SillyAssignment");
    }

    public Set<Kind> getTreeKinds() {
        return KINDS;
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        cancel.set(false);
        
        Tree node = treePath.getLeaf();

        if ( node.getKind() != Tree.Kind.ASSIGNMENT ) {
            return null;
        }
        
        AssignmentTree tree = (AssignmentTree)node;
        
        if ( ignore( treePath, tree, info.getTrees() ) ) {
            return null;
        }
        
        TreePath tpVar = new TreePath( treePath, tree.getVariable() );
        TreePath tpExp = new TreePath( treePath, tree.getExpression() );
        
        Element eVar = info.getTrees().getElement(tpVar);
        Element eExp = info.getTrees().getElement(tpExp);

        if ( eVar != null && eExp != null && eVar.equals( eExp ) ) {
            
            List<Fix> fixes = new ArrayList<Fix>();
            
            ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(
                        getSeverity().toEditorSeverity(), 
                        getDisplayName(), 
                        fixes, 
                        info.getFileObject(),
                        (int)info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree ),
                        (int)info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree ) );
                    
            return Collections.<ErrorDescription>singletonList(ed);            
        }
        
        return null;
    }

    public void cancel() {
        cancel.set(true);
    }

    public String getId() {
        return "AssignmentToItself"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI"); // NOI18N
    }
    
    public String getDescription() {
        return NbBundle.getMessage(AssignmentToItself.class, "DSC_ATI"); // NOI18N
    }
    
    private boolean ignore(TreePath tp, AssignmentTree at, Trees trees ) {
        
        ExpressionTree var = at.getVariable();
        ExpressionTree exp = at.getExpression();
        
        List<Element> varElements = new ArrayList<Element>();
        List<Element> expElements = new ArrayList<Element>();
        
        // System.out.println(at);
        CancellableTreePathScanner<Boolean, List<Element>> scanner = new MethodCallScanner(cancel, trees);
        Boolean varMI = scanner.scan(new TreePath( tp, var), varElements);
        varMI = varMI == null ? false : varMI;
        // System.out.println("  ------------");
        scanner = new MethodCallScanner(cancel, trees);
        Boolean expMI = scanner.scan(new TreePath( tp, exp), expElements);
        expMI = expMI == null ? false : expMI;
        // System.out.println("VE" + varMI + " " + expMI);
        
        if ( varMI || expMI ) {
            return true;
        } 
        else {
            boolean equal = varElements.equals(expElements);
            //compare array indexes in case of array access
            if (equal && var.getKind() == Kind.MEMBER_SELECT && exp.getKind() == Kind.MEMBER_SELECT) {
                ExpressionTree varExp = ((MemberSelectTree) var).getExpression();
                ExpressionTree expExp = ((MemberSelectTree) exp).getExpression();
                if (varExp.getKind() == Kind.ARRAY_ACCESS && expExp.getKind() == Kind.ARRAY_ACCESS) {
                    if (!((ArrayAccessTree) varExp).getIndex().toString().equals(((ArrayAccessTree) expExp).getIndex().toString()))
                            return true;
                }
            }
            return !equal;
        }
    }
    
    private static class MethodCallScanner extends CancellableTreePathScanner<Boolean, List<Element>> {

        private Trees trees;
        
        public MethodCallScanner(AtomicBoolean cancel, Trees trees ) {
            super(cancel);
            this.trees = trees;
        }

                
        @Override
        public Boolean visitMemberSelect(MemberSelectTree t, List<Element> l) {
            l.add(trees.getElement(getCurrentPath()));
            return super.visitMemberSelect(t, l);
        }

        @Override
        public Boolean visitIdentifier(IdentifierTree t, List<Element> l) {
            if ( !"this".equals(t.getName().toString())) { // NOI18N
                l.add(trees.getElement(getCurrentPath()));
            }
            return super.visitIdentifier(t, l);
        }

        @Override
        public Boolean visitMethodInvocation(MethodInvocationTree arg0, List<Element> arg1) {
            return true;
        }                       
        
    }


    private static class ATIFix implements Fix, Task<WorkingCopy> {

        private static final int REMOVE = 0;
        private static final int QUALIFY = 1;
        private static final int NEW_PARAMETER = 2;
        private static final int NEW_FIELD = 3;
        
        private int kind;
        private TreePath treePath;
        private FileObject file;

        public ATIFix(int kind, TreePath treePath, FileObject file) {
            this.kind = kind;
            this.treePath = treePath;
            this.file = file;
        }
        
        public String getText() {
            
            switch( kind ) {
                case REMOVE:
                    return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI_Remove_FIX"); // NOI18N
                case QUALIFY:
                    return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI_Qualify_FIX"); // NOI18N
                case NEW_PARAMETER:
                    return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI_NewParameter_FIX"); // NOI18N
                case NEW_FIELD:
                    return NbBundle.getMessage(AssignmentToItself.class, "LBL_ATI_NewField_FIX"); // NOI18N
            } 
            
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public ChangeInfo implement() throws Exception {
            JavaSource js = JavaSource.forFileObject(file);
            try {
                js.runModificationTask(this).commit();
            }
            catch( IOException e ) {
                Exceptions.printStackTrace(e);
            }
            return null;
        }

        public void run(WorkingCopy workingCopy) throws Exception {
            /*
            workingCopy.toPhase(Phase.RESOLVED);
            TreeMaker treeMaker = workingCopy.getTreeMaker();

            TreeUtilities treeUtilities = workingCopy.getTreeUtilities();

            AssignmentTree assignmentTree = (AssignmentTree)getEnclosingTreeOfKind(treePath, Tree.Kind.ASSIGNMENT);

            TreePath tpVar = new TreePath( treePath, assignmentTree.getVariable() );
            Element eVar = workingCopy.getTrees().getElement(tpVar);
            VariableTree vt = (VariableTree) workingCopy.getTrees().getTree(eVar); // XXX test iof


            VariableElement var = (VariableElement)eVar; // XXX test iof 

            MethodTree methodTree = (MethodTree)getEnclosingTreeOfKind(treePath, Tree.Kind.METHOD);

            MethodTree newMethod = treeMaker.addMethodParameter(methodTree, treeMaker.Variable(
                                    treeMaker.Modifiers(
                                        Collections.<Modifier>emptySet(),
                                        Collections.<AnnotationTree>emptyList()
                                    ),
                                    eVar.getSimpleName().toString(),
                                    vt.getType(),
                                    null
                                ));

            workingCopy.rewrite(methodTree, newMethod);
        
        */
        }
    }

    
}
