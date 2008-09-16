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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
class OrigSurroundWithTryCatchFix implements Fix {

    private JavaSource javaSource;
    private List<TypeMirrorHandle> thandles;
    private TreePathHandle path;
    private List<String> fqns;

    public OrigSurroundWithTryCatchFix(JavaSource javaSource, List<TypeMirrorHandle> thandles, TreePathHandle path, List<String> fqns) {
        this.javaSource = javaSource;
        this.thandles = thandles;
        this.path = path;
        this.fqns = fqns;
    }

    public String getText() {
        return NbBundle.getMessage(MagicSurroundWithTryCatchFix.class, "LBL_SurroundStatementWithTryCatch");
    }

    public ChangeInfo implement() throws Exception {
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                
                TreePath p = path.resolve(parameter);
                
                if (p == null) {
                    return ;//XXX: log
                }
                
                p = findStatement(p);
                
                if (p == null) {
                    return ; //XXX: log
                }
                
                StatementTree leaf = (StatementTree) p.getLeaf();
                TreeMaker make = parameter.getTreeMaker();
                
                if (leaf.getKind() == Kind.VARIABLE) {
                    //may be necessary to separate variable declaration and assignment:
                    Element e = parameter.getTrees().getElement(p);
                    VariableTree vt = (VariableTree) leaf;

                    // XXX: Come up with some smart skipping solution for comma separated variables, with respect to #143232
                    if (e != null && e.getKind() == ElementKind.LOCAL_VARIABLE) {
                        TreePath block = findBlock(p);
                        
                        if (block != null) {
                            boolean sep = new FindUsages(leaf, parameter).scan(block, (VariableElement) e) == Boolean.TRUE;
                            
                            if (sep) {
                                BlockTree bt = (BlockTree) block.getLeaf();
                                int index = bt.getStatements().indexOf(leaf);
                                
                                assert index != (-1);
                                
                                StatementTree assignment = make.ExpressionStatement(make.Assignment(make.Identifier(vt.getName()), vt.getInitializer()));
                                StatementTree declaration = make.Variable(vt.getModifiers(), vt.getName(), vt.getType(), null);//XXX: mask out final
                                TryTree tryTree = make.Try(make.Block(Collections.singletonList(assignment), false), MagicSurroundWithTryCatchFix.createCatches(parameter, make, thandles, p), null);
                                List<StatementTree> nueStatements = new LinkedList<StatementTree>();
                                
                                nueStatements.addAll(bt.getStatements().subList(0, index));
                                nueStatements.add(declaration);
                                nueStatements.add(tryTree);
                                nueStatements.addAll(bt.getStatements().subList(index + 1, bt.getStatements().size()));
                                
                                parameter.rewrite(bt, make.Block(nueStatements, false));
                                return ;
                            }
                        }
                    }
                }
                
                Tree tryTree = make.Try(make.Block(Collections.singletonList(leaf), false), MagicSurroundWithTryCatchFix.createCatches(parameter, make, thandles, p), null);
                
                parameter.rewrite(leaf, tryTree);
            }
        }).commit();
        return null;
    }
    
    private TreePath findStatement(TreePath path) {
        while (path != null && !StatementTree.class.isAssignableFrom(path.getLeaf().getKind().asInterface())) {
            path = path.getParentPath();
        }
        
        return path;
    }
    
    private TreePath findBlock(TreePath path) {
        while (path != null && path.getLeaf().getKind() != Kind.BLOCK) {
            path = path.getParentPath();
        }
        
        return path;
    }
    
    private static final class FindUsages extends TreePathScanner<Boolean, VariableElement> {

        private Tree ignore;
        private CompilationInfo info;

        public FindUsages(Tree ignore, CompilationInfo info) {
            this.ignore = ignore;
            this.info = info;
        }
        
        @Override
        public Boolean visitIdentifier(IdentifierTree node, VariableElement p) {
            return p.equals(info.getTrees().getElement(getCurrentPath()));
        }

        @Override
        public Boolean scan(Tree tree, VariableElement p) {
            if (tree == ignore)
                return false;
            
            return super.scan(tree, p);
        }

        @Override
        public Boolean reduce(Boolean r1, Boolean r2) {
            return r1 == Boolean.TRUE || r2 == Boolean.TRUE;
        }
        
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OrigSurroundWithTryCatchFix other = (OrigSurroundWithTryCatchFix) obj;
        if (this.javaSource != other.javaSource && (this.javaSource == null || !this.javaSource.equals(other.javaSource))) {
            return false;
        }
        if (!this.path.equals(other.path)) {
            return false;
        }
        if (!this.fqns.equals(other.fqns)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.javaSource != null ? this.javaSource.hashCode() : 0);
        return hash;
    }
    
    
    
}
