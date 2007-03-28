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

package org.netbeans.modules.refactoring.java;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 *
 */
public class TreePathResolver implements CancellableTask<CompilationController>{
    
    public static interface TreePathHandleSelector {
        TreePathHandle select(CompilationController compilationController);
    }
    
    public TreePathResolver(TreePathHandleSelector selector) {
        this.selector = selector;
    }
    
    private TreePathHandleSelector selector;
    
    public TreePathHandle tph;
    
    public CompilationInfo info;
    
    public void cancel() {
        // not implemented
    }
        
   
    public void run(CompilationController parameter) throws Exception {
        parameter.toPhase(Phase.RESOLVED);
        info = parameter;        
        tph = selector.select(parameter);
        
        
        
        /*
        List<? extends Tree> typeDecls = parameter.getCompilationUnit().getTypeDecls();
        TreePath cuPath = new TreePath(parameter.getCompilationUnit());
         
        for (Tree t : typeDecls) {
            TreePath p = new TreePath( cuPath, t );
            Element e = parameter.getTrees().getElement(p);
            List<? extends Element> elems = e.getEnclosedElements();
            for (Element element : elems) {
                System.out.println(element.getSimpleName().toString());
                System.out.println(element.getKind().toString());
                if(element.getKind()==ElementKind.METHOD) {
                    Tree tt = parameter.getTrees().getTree(element);
                    System.out.println(tt.getKind());
                    System.out.println(tt.getClass().getName());
                    MethodTree mt = (MethodTree) tt;
                    List<? extends VariableTree> vars = mt.getParameters();
                    System.out.println(vars.size());
                    System.out.println(vars.get(0).getName().toString());
                    System.out.println(vars.get(0).getType().toString());
                    TreePath path = TreePath.getPath(cuPath,vars.get(0));
                    tph = TreePathHandle.create(path, parameter);
                }
            }
        }*/
    }
}
