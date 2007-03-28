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
import org.netbeans.api.java.source.TreePathHandle;
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
    
    
    
    
    
}
