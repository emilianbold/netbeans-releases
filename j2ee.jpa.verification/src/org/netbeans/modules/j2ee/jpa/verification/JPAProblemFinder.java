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

package org.netbeans.modules.j2ee.jpa.verification;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.j2ee.jpa.model.JPAHelper;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JPAProblemFinder implements CancellableTask<CompilationInfo> {
    private boolean cancelled = false;
    private FileObject file = null;
    public final static Logger LOG = Logger.getLogger(JPAProblemFinder.class.getName());
    
    public JPAProblemFinder(FileObject file){
        this.file = file;
    }
    
    public void run(CompilationInfo info) throws Exception{
        List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
        
        for (Tree tree : info.getCompilationUnit().getTypeDecls()){
            if (isCancelled()){
                break;
            }
            
            if (tree.getKind() == Tree.Kind.CLASS){
                TreePath path = info.getTrees().getPath(info.getCompilationUnit(), tree);
                TypeElement javaClass = (TypeElement) info.getTrees().getElement(path);
                LOG.info("processing class" + javaClass.getSimpleName());
                JPAProblemContext context = findProblemContext(info, javaClass);
                JPARulesEngine rulesEngine = new JPARulesEngine();
                javaClass.accept(rulesEngine, context);
                problemsFound.addAll(rulesEngine.getProblemsFound());
            }
        }
        
        HintsController.setErrors(file, "JPA Verification", problemsFound); //NOI18N
    }
    
    private JPAProblemContext findProblemContext(CompilationInfo info, TypeElement javaClass){
        JPAProblemContext context = new JPAProblemContext();
        context.setEntity(JPAHelper.isEntityClass(javaClass));
        context.setFileObject(file);
        context.setCompilationInfo(info);
        return context;
    }
    
    public void cancel(){
        cancelled = true;
    }
    
    public boolean isCancelled(){
        return cancelled;
    }
}
