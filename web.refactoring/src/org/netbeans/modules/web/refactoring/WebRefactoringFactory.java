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
package org.netbeans.modules.web.refactoring;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.refactoring.rename.TldMove;
import org.netbeans.modules.web.refactoring.rename.TldPackageRename;
import org.netbeans.modules.web.refactoring.rename.TldRename;
import org.netbeans.modules.web.refactoring.rename.WebXmlMove;
import org.netbeans.modules.web.refactoring.rename.WebXmlPackageRename;
import org.netbeans.modules.web.refactoring.rename.WebXmlRename;
import org.netbeans.modules.web.refactoring.safedelete.TldSafeDelete;
import org.netbeans.modules.web.refactoring.safedelete.WebXmlSafeDelete;
import org.netbeans.modules.web.refactoring.whereused.TldWhereUsed;
import org.netbeans.modules.web.refactoring.whereused.WebXmlWhereUsed;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * A refactoring factory for Web related refactorings.
 *
 * @author Erno Mononen
 */
public class WebRefactoringFactory implements RefactoringPluginFactory{
    
    public WebRefactoringFactory() {
    }
    
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        
        NonRecursiveFolder pkg = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
        FileObject sourceFO = refactoring.getRefactoringSource().lookup(FileObject.class);
        TreePathHandle handle = resolveTreePathHandle(refactoring);
        
        boolean javaPackage = pkg != null && RefactoringUtil.isOnSourceClasspath(pkg.getFolder());
        boolean folder = sourceFO != null && sourceFO.isFolder();
        
        if (sourceFO == null){
            if (handle != null){
                sourceFO = handle.getFileObject();
            } else if (pkg != null){
                sourceFO = pkg.getFolder();
            }
        }
        
        if (sourceFO == null){
            return null;
        }

        boolean javaFile = sourceFO != null && RefactoringUtil.isJavaFile(sourceFO);

        WebModule wm = WebModule.getWebModule(sourceFO);
        if (wm == null){
            return null;
        }
        FileObject ddFile = wm.getDeploymentDescriptor();
        WebApp webApp = getWebApp(ddFile);
        if (webApp == null){
            return null;
        }
        String clazz = resolveClass(handle);
        
        List<WebRefactoring> refactorings = new ArrayList<WebRefactoring>();
        
        if (refactoring instanceof RenameRefactoring){
            RenameRefactoring rename = (RenameRefactoring) refactoring;
            if (javaPackage || folder){
                refactorings.add(new WebXmlPackageRename(ddFile, webApp, sourceFO, rename));
                refactorings.add(new TldPackageRename(rename, sourceFO));
            } else if (javaFile) {
                refactorings.add(new WebXmlRename(clazz, rename, webApp, ddFile));
                refactorings.add(new TldRename(clazz, rename, sourceFO));
            }
        } 
        
        if (refactoring instanceof SafeDeleteRefactoring && javaFile){
            SafeDeleteRefactoring safeDelete = (SafeDeleteRefactoring) refactoring;
            refactorings.add(new WebXmlSafeDelete(ddFile, webApp, clazz, safeDelete));
            refactorings.add(new TldSafeDelete(clazz, safeDelete, sourceFO));
        }
        
        if (refactoring instanceof WhereUsedQuery && javaFile){
            WhereUsedQuery whereUsedQuery = (WhereUsedQuery) refactoring;
            refactorings.add(new WebXmlWhereUsed(ddFile, webApp, clazz, whereUsedQuery));
            refactorings.add(new TldWhereUsed(clazz,sourceFO, whereUsedQuery));
        } 
        
        if (refactoring instanceof MoveRefactoring && javaFile){
            MoveRefactoring move = (MoveRefactoring) refactoring;
            refactorings.add(new WebXmlMove(ddFile, webApp, clazz, move));
            refactorings.add(new TldMove(move, sourceFO, clazz));
        }
        
        return refactorings.isEmpty() ? null : new WebRefactoringPlugin(refactorings);
    }
    
    private WebApp getWebApp(FileObject ddFile){
        if (ddFile == null){
            return null;
        }
        try{
            return DDProvider.getDefault().getDDRoot(ddFile);
        }catch(IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        return null;
    }
    
    private TreePathHandle resolveTreePathHandle(final AbstractRefactoring refactoring){
        
        TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (tph != null) {
            return tph;
        }
        
        FileObject sourceFO = refactoring.getRefactoringSource().lookup(FileObject.class);
        if (sourceFO == null || !RefactoringUtil.isJavaFile(sourceFO)){
            return null;
        }
        final TreePathHandle[] result = new TreePathHandle[1];
        try{
            
            JavaSource source = JavaSource.forFileObject(sourceFO);
            
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                public void cancel() {
                }
                public void run(CompilationController co) throws Exception {
                    co.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cut = co.getCompilationUnit();
                    if (cut.getTypeDecls().isEmpty()){
                        return;
                    }
                    result[0] = TreePathHandle.create(TreePath.getPath(cut, cut.getTypeDecls().get(0)), co);
                }
                
            }, true);
        }catch(IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        
        return result[0];
    }
    
    
    /**
     * @return the fully qualified name of the class that the given
     * TreePathHandle represents or null if the FQN could not be resolved.
     */
    private String resolveClass(final TreePathHandle treePathHandle){
        if(treePathHandle == null){
            return null;
        }
        
        final String[] result = new String[1];
        
        try{
            JavaSource source = JavaSource.forFileObject(treePathHandle.getFileObject());
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void cancel() {
                }
                
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(JavaSource.Phase.RESOLVED);
                    Element element = treePathHandle.resolveElement(parameter);
                    result[0] = element.asType().toString();
                }
            }, true);
        }catch(IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        return result[0];
    }
    
    
}
