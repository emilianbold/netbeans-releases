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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.openide.util.NbBundle;


/**
 *
 * @author Jan Lahoda
 */
public class AddParameterOrLocalFix implements Fix {
    
    private FileObject file;
    private TypeMirrorHandle type;
    private String name;
    private boolean parameter;
    
    private TreePathHandle[] tpHandle;
    
    public AddParameterOrLocalFix(CompilationInfo info,
                                  TypeMirror type, String name,
                                  boolean parameter,
                                  int /*!!!Position*/ unresolvedVariable) {
        this.file = info.getFileObject();
        if (type.getKind() == TypeKind.NULL) {
            type = info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
        }
        this.type = TypeMirrorHandle.create(type);
        this.name = name;
        this.parameter = parameter;

        TreePath treePath = info.getTreeUtilities().pathFor(unresolvedVariable + 1);
        tpHandle = new TreePathHandle[1];
        tpHandle[0] = TreePathHandle.create(treePath, info);
    }

    public String getText() {
        return parameter ? 
            NbBundle.getMessage(AddParameterOrLocalFix.class, "LBL_FIX_Create_Parameter", name) : // NOI18N
            NbBundle.getMessage(AddParameterOrLocalFix.class, "LBL_FIX_Create_Local_Variable", name); // NOI18N
    }

    public ChangeInfo implement() throws IOException {
        //use the original cp-info so it is "sure" that the proposedType can be resolved:
        JavaSource js = JavaSource.forFileObject(file);

        js.runModificationTask(new Task<WorkingCopy>() {
            public void run(final WorkingCopy working) throws IOException {
                working.toPhase(Phase.RESOLVED);

                TypeMirror proposedType = type.resolve(working);

                if (proposedType == null) {
                    ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve proposed type."); // NOI18N
                    return;
                }

                TreeMaker make = working.getTreeMaker();

                //TreePath tp = working.getTreeUtilities().pathFor(unresolvedVariable + 1);
                //Use TreePathHandle instead of position supplied as field (#143318)
                TreePath tp = tpHandle[0].resolve(working);
                if (tp.getLeaf().getKind() != Kind.IDENTIFIER)
                    return;
                assert tp.getLeaf().getKind() == Kind.IDENTIFIER;

                TreePath targetPath = findMethod(tp);
                
                if (targetPath == null) {
                    ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve target method."); // NOI18N
                    return;
                }
                
                MethodTree targetTree = (MethodTree) targetPath.getLeaf();

                if (parameter) {
                    if (targetTree == null) {
                        Logger.getLogger("global").log(Level.WARNING, "Add parameter - cannot find the method."); // NOI18N
                    }

                    Element el = working.getTrees().getElement(targetPath);
                    int index = targetTree.getParameters().size();

                    if (el != null && (el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.CONSTRUCTOR)) {
                        ExecutableElement ee = (ExecutableElement) el;

                        if (ee.isVarArgs()) {
                            index = ee.getParameters().size() - 1;
                        }
                    }

                    MethodTree result = make.insertMethodParameter(targetTree, index, make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(proposedType), null));

                    working.rewrite(targetTree, result);
                } else {
                    if (ErrorFixesFakeHint.isCreateLocalVariableInPlace()) {
                        resolveLocalVariable(working, tp, make, proposedType);
                    } else {
                        resolveLocalVariable55(working, tp, make, proposedType);
                    }
                }
            }
        }).commit();
        
        return null;
    }

    private void resolveLocalVariable55(final WorkingCopy wc, TreePath tp, TreeMaker make, TypeMirror proposedType) {
        final String name = ((IdentifierTree) tp.getLeaf()).getName().toString();
        
        //find first usage of this (undeclared) variable:
        TreePath method = findMethod(tp);

        if (method == null) {
            //TODO: probably initializer handle differently
            return;
        }
        
        int index = 0;
        MethodTree methodTree = (MethodTree) method.getLeaf();
        BlockTree block = methodTree.getBody();
        
        if (methodTree.getReturnType() == null && !block.getStatements().isEmpty()) {
            StatementTree stat = block.getStatements().get(0);
            
            if (stat.getKind() == Kind.EXPRESSION_STATEMENT) {
                Element thisMethodEl = wc.getTrees().getElement(method);
                TreePath pathToFirst = new TreePath(new TreePath(new TreePath(method, block), stat), ((ExpressionStatementTree) stat).getExpression());
                Element superCall = wc.getTrees().getElement(pathToFirst);

                if (thisMethodEl != null && superCall != null && thisMethodEl.getKind() == ElementKind.CONSTRUCTOR && superCall.getKind() == ElementKind.CONSTRUCTOR) {
                    index = 1;
                }
            }
        }
        
        VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(proposedType), null);
        
        wc.rewrite(block, wc.getTreeMaker().insertBlockStatement(block, index, vt));
    }
    
    private void resolveLocalVariable(final WorkingCopy wc, TreePath tp, TreeMaker make, TypeMirror proposedType) {
        final String name = ((IdentifierTree) tp.getLeaf()).getName().toString();
        
        final Element el = wc.getTrees().getElement(tp);
        
        //find first usage of this (undeclared) variable:
        TreePath method = findMethod(tp);
        
        if (method == null) {
            //TODO: probably initializer handle differently
            return;
        }
        
        class FirstUsage extends TreePathScanner<TreePath, Void> {
            private TreePath found;
            public @Override TreePath visitIdentifier(IdentifierTree tree, Void v) {
                if (tree.getName().contentEquals(el.getSimpleName())) {
                    if (found == null) {
                        found = getCurrentPath();
                    }
                    return findStatement(getCurrentPath());
                }
                return null;
            }
            public @Override TreePath visitBlock(BlockTree tree, Void v) {
                TreePath result = null;
                TreePath firstBranchStatementWithUsage = null;
                for (StatementTree t : tree.getStatements()) {
                    TreePath currentResult = scan(t, null);
                    
                    if (currentResult != null && result == null) {
                        result = currentResult;
                        firstBranchStatementWithUsage = new TreePath(getCurrentPath(), t);
                    }
                    
                    if (currentResult != t && result != null && result.getLeaf() != firstBranchStatementWithUsage.getLeaf()) {
                        //ie.: { x = 1; } ... { x = 1; }
                        result = firstBranchStatementWithUsage;
                    }
                }
                super.visitBlock(tree, v);
                return result;
            }
            public @Override TreePath reduce(TreePath tp1, TreePath tp2) {
                if (tp2 == null)
                    return tp1;
                
                return tp2;
                
            }
        }
        
        FirstUsage firstUsage  = new FirstUsage();
        TreePath firstUse = firstUsage.scan(method, null);
        
        if (firstUse == null || !isStatement(firstUse.getLeaf())) {
            Logger.getLogger("global").log(Level.WARNING, "Add local variable - cannot find a statement."); // NOI18N
            return;
        }
        
        StatementTree statement = (StatementTree) firstUse.getLeaf();
        
        if (statement.getKind() == Kind.EXPRESSION_STATEMENT) {
            ExpressionTree exp = ((ExpressionStatementTree) statement).getExpression();
            
            if (exp.getKind() == Kind.ASSIGNMENT) {
                //replace the expression statement with a variable declaration:
                AssignmentTree at = (AssignmentTree) exp;
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(proposedType), at.getExpression());
                
                vt = Utilities.copyComments(wc, statement, vt);
                        
                wc.rewrite(statement, vt);
                
                return;
            }
        }
        
        Tree statementParent = firstUse.getParentPath().getLeaf();
        VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(proposedType), null);
        
        if (statementParent.getKind() == Kind.BLOCK) {
            BlockTree block = (BlockTree) statementParent;
            BlockTree nueBlock = make.insertBlockStatement(block, block.getStatements().indexOf(statement), vt);
            
            wc.rewrite(block, nueBlock);
        } else {
            BlockTree block = make.Block(Arrays.asList(vt, statement), false);
            
            wc.rewrite(statement, block);
        }
    }
    
    private TreePath findStatement(TreePath tp) {
        TreePath statement = tp;
        
        while (statement.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            if (isStatement(statement.getLeaf())) {
                return statement;
            }
            
            statement = statement.getParentPath();
        }
        
        return null;
    }
    
    private TreePath findMethod(TreePath tp) {
        TreePath method = tp;
        
        while (method != null) {
            if (method.getLeaf().getKind() == Kind.METHOD) {
                return method;
            }
            
            method = method.getParentPath();
        }
        
        return null;
    }
    
    private boolean isStatement(Tree t) {
        Class intClass = t.getKind().asInterface();
        
        return StatementTree.class.isAssignableFrom(intClass);
    }
    
    String toDebugString(CompilationInfo info) {
        return "AddParameterOrLocalFix:" + name + ":" + type.resolve(info).toString() + ":" + parameter; // NOI18N
    }
    
    boolean isParameter() {
        return parameter;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AddParameterOrLocalFix other = (AddParameterOrLocalFix) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.parameter != other.parameter) {
            return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 97 * hash + (this.parameter ? 1 : 0);
        return hash;
    }
    
    
}
