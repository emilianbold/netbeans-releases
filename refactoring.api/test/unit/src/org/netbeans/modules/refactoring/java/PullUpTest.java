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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.net.URL;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PullUpRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jiri Prox
 */
public class PullUpTest extends RefactoringTestCase {
    
    public PullUpTest(String name) {
        super(name);
    }
    
    public void testPullUpField() throws Exception{
        FileObject test = getFileInProject("default","src/pulluppkg/PullUpField.java" );
        JavaSource js = JavaSource.forFileObject(test);
        FileObject dest = getFileInProject("default","src/pulluppkg/PullUpFieldSuper.java" );
        JavaSource jsDest = JavaSource.forFileObject(dest);
        final TreePathResolver fieldSelector = new TreePathResolver(new FieldSelector());
        final TreePathResolver srcClassSelector = new TreePathResolver(new TopClassSelector(0));
        final TreePathResolver destClassSelector = new TreePathResolver(new TopClassSelector(0));
        js.runUserActionTask(fieldSelector,true);
        js.runUserActionTask(srcClassSelector,true);
        jsDest.runUserActionTask(destClassSelector,true);
        final PullUpRefactoring pullUp = new PullUpRefactoring(Lookups.fixed(test,destClassSelector.tph));
        perform(pullUp,new ParameterSetter() {
            public void setParameters() {
                MemberInfo[] mi = new MemberInfo[]{new MemberInfo(fieldSelector.tph.resolveElement(fieldSelector.info),fieldSelector.info)};
                pullUp.setMembers(mi);
                ElementHandle el = ElementHandle.create(srcClassSelector.tph.resolveElement(srcClassSelector.info));
                pullUp.setTargetType(el);                                
            }
        });        
    }
    
    class FieldSelector implements TreePathResolver.TreePathHandleSelector {
        
        public TreePathHandle select(CompilationController compilationController) {
            TreePath cuPath = new TreePath(compilationController.getCompilationUnit());
            List<? extends Tree> typeDecls = compilationController.getCompilationUnit().getTypeDecls();
            Tree t = typeDecls.get(0);
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
            
            return null;
        }
        
    }
    
    class TopClassSelector implements TreePathResolver.TreePathHandleSelector {
        
        int classNum;
        
        public TopClassSelector(int classNum) {
            this.classNum = classNum;
        }
        
        public TreePathHandle select(CompilationController compilationController) {
            TreePath cuPath = new TreePath(compilationController.getCompilationUnit());
            List<? extends Tree> typeDecls = compilationController.getCompilationUnit().getTypeDecls();
            Tree t = typeDecls.get(classNum);
            TreePathHandle pathHandle = TreePathHandle.create(TreePath.getPath(cuPath, t), compilationController);
            return pathHandle;
        }                       
    }
    
    
}
