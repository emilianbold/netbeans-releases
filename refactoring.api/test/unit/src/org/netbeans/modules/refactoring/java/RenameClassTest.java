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

package org.netbeans.modules.refactoring.java;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import junit.framework.Test;
import org.junit.runners.Suite;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.junit.NbModuleSuite;
/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class RenameClassTest extends RefactoringTestCase {
    
    /** Creates a new instance of RenameClassTest */
    public RenameClassTest(String name) {
        super(name);
        
    }
    
    public void testRenameClass() throws Exception {
        FileObject test = getFileInProject("default","src/defaultpkg/Foo.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("newName");
            }
        });
    }
    
    public void testRenameEnum() throws Exception {
        FileObject test = getFileInProject("default","src/defaultpkg/RenameEnum.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("NewEnumName");
            }
        });
    }
    
    public void testRenameAnnotation() throws Exception {
        FileObject test = getFileInProject("default","src/defaultpkg/RenameAnnot.java" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("NewAnnotName");
            }
        });
    }
    
    public void testRenamePackage() throws Exception {
        FileObject test = getFileInProject("default","src/renamepkg" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("newpkgname");
            }
        });
    }
    
    public void testRenamePackage2() throws Exception {
        FileObject test = getFileInProject("default","src/renamepkg2" );
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("newpkgname2");
            }
        });
        
    }
        
    private class ParamSelector implements TreePathResolver.TreePathHandleSelector {
        
        public TreePathHandle select(CompilationController compilationController) {
            TreePath cuPath = new TreePath(compilationController.getCompilationUnit());
            List<? extends Tree> typeDecls = compilationController.getCompilationUnit().getTypeDecls();
            for (Tree t : typeDecls) {
                TreePath p = new TreePath( cuPath, t );
                Element e = compilationController.getTrees().getElement(p);
                List<? extends Element> elems = e.getEnclosedElements();
                for (Element element : elems) {
                    if(element.getKind()==ElementKind.METHOD) {
                        Tree tt = compilationController.getTrees().getTree(element);
                        MethodTree mt = (MethodTree) tt;
                        List<? extends VariableTree> vars = mt.getParameters();
                        TreePath path = TreePath.getPath(cuPath,vars.get(0));
                        return TreePathHandle.create(path, compilationController);
                    }
                }
            }            
            return null;
        }
    }
    
    
    public void testRenameParam() throws Exception {
        FileObject test = getFileInProject("default","src/defaultpkg/RenameParam.java" );
        JavaSource js = JavaSource.forFileObject(test);
        TreePathResolver res = new TreePathResolver(new ParamSelector());
        js.runUserActionTask(res,true);
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(res.tph));
        renameRefactoring.getContext().add(res.info);
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("newParamName");
            }
        });
    }
    
    private class FieldSelector implements TreePathResolver.TreePathHandleSelector {
        
        public TreePathHandle select(CompilationController compilationController) {
            TreePath cuPath = new TreePath(compilationController.getCompilationUnit());
            List<? extends Tree> typeDecls = compilationController.getCompilationUnit().getTypeDecls();
            for (Tree t : typeDecls) {
                TreePath p = new TreePath(cuPath, t );
                Element e = compilationController.getTrees().getElement(p);
                List<? extends Element> elems = e.getEnclosedElements();
                for (Element element : elems) {
                    if(element.getKind()==ElementKind.FIELD) {
                        Tree tt = compilationController.getTrees().getTree(element);
                        TreePath path = TreePath.getPath(cuPath,tt);
                        return TreePathHandle.create(path, compilationController);
                    }
                }
            }
            return null;
        }
        
    }    
    
    public void testRenameField() throws Exception {
        FileObject test = getFileInProject("default","src/defaultpkg/RenameField.java" );
        JavaSource js = JavaSource.forFileObject(test);
        TreePathResolver res = new TreePathResolver(new FieldSelector());
        js.runUserActionTask(res,true);
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(res.tph));
        renameRefactoring.getContext().add(res.info);
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("newFieldName");
            }
        });
    }
    
    private class VariableSelector implements TreePathResolver.TreePathHandleSelector {
        
        public TreePathHandle select(CompilationController compilationController) {
            TreePath cuPath = new TreePath(compilationController.getCompilationUnit());
            List<? extends Tree> typeDecls = compilationController.getCompilationUnit().getTypeDecls();
            Tree t = typeDecls.get(0);
            TreePath p = new TreePath(cuPath, t );
            Element e = compilationController.getTrees().getElement(p);
            List<? extends Element> elems = e.getEnclosedElements();
            
            for (Element element : elems) {
                if(element.getKind()==ElementKind.METHOD) {
                    Tree tt = compilationController.getTrees().getTree(element);
                    MethodTree mt = (MethodTree) tt;
                    BlockTree bt = mt.getBody();
                    List<? extends StatementTree> sts =  bt.getStatements();
                    StatementTree st = sts.get(0);                    
                    TreePath path = TreePath.getPath(cuPath,st);
                    return TreePathHandle.create(path, compilationController);
                }
            }                        
            return null;
        }
        
    }
    
    public void testRenameLocalVar() throws Exception {
        FileObject test = getFileInProject("default","src/defaultpkg/RenameLocal.java" );
        JavaSource js = JavaSource.forFileObject(test);
        TreePathResolver res = new TreePathResolver(new VariableSelector());
        js.runUserActionTask(res,true);
        final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(res.tph));
        renameRefactoring.getContext().add(res.info);
        perform(renameRefactoring,new ParameterSetter() {
            public void setParameters() {
                renameRefactoring.setNewName("newVarName");
            }
        });
    }

    public static Test suite() {
        return NbModuleSuite.create(RenameClassTest.class, ".*", ".*", 
                "testRenameClass",
                "testRenameEnum",
                "testRenameField",
                "testRenameLocalVar",
                "testRenamePackage",
                "testRenamePackage2",
                "testRenameParam",
                "testRenameAnnotation");
        
    }
    
    
    
    
}
