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
import org.netbeans.modules.web.refactoring.rename.WebXmlMove;
import org.netbeans.modules.web.refactoring.rename.WebXmlPackageRename;
import org.netbeans.modules.web.refactoring.rename.WebXmlRename;
import org.netbeans.modules.web.refactoring.safedelete.WebXmlSafeDelete;
import org.netbeans.modules.web.refactoring.whereused.WebXmlWhereUsed;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * A refactoring plugin for Web related refactorings.
 *
 * @author Erno Mononen
 */
public class WebRefactoringFactory implements RefactoringPluginFactory{
    
    private static final String JAVA_MIME_TYPE = "text/x-java"; //NO18N
    
    public WebRefactoringFactory() {
    }
    
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        
        NonRecursiveFolder folder = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class);
        TreePathHandle handle = resolveTreePathHandle(refactoring);
        
        boolean javaPackage = folder != null && RefactoringUtil.isOnSourceClasspath(folder.getFolder());
        
        FileObject sourceFO = null;
        if (handle == null){
            sourceFO = refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class).getFolder();
        } else {
            sourceFO = handle.getFileObject();
        }
        
        FileObject ddFile = WebModule.getWebModule(sourceFO).getDeploymentDescriptor();
        WebApp webApp = getWebApp(ddFile);
        String clazz = resolveClass(handle);
        
        if (refactoring instanceof RenameRefactoring){
            RenameRefactoring rename = (RenameRefactoring) refactoring;
            if (javaPackage){
                return new WebXmlPackageRename(ddFile, webApp, sourceFO, rename);
            }
            return new WebXmlRename(clazz, rename, webApp, ddFile);
        } if (refactoring instanceof SafeDeleteRefactoring){
            SafeDeleteRefactoring safeDelete = (SafeDeleteRefactoring) refactoring;
            return new WebXmlSafeDelete(ddFile, webApp, clazz, safeDelete);
        } if (refactoring instanceof WhereUsedQuery){
            WhereUsedQuery whereUsedQuery = (WhereUsedQuery) refactoring;
            return new WebXmlWhereUsed(ddFile, webApp, clazz, whereUsedQuery);
        } if (refactoring instanceof MoveRefactoring){
            MoveRefactoring move = (MoveRefactoring) refactoring;
            return new WebXmlMove(ddFile, webApp, clazz, move);
        }
        
        return null;
    }
    
    private WebApp getWebApp(FileObject ddFile){
        try{
            return DDProvider.getDefault().getDDRoot(ddFile);
        }catch(IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        //XXX
        return null;
    }
    
    private TreePathHandle resolveTreePathHandle(final AbstractRefactoring refactoring){
        
        TreePathHandle tph = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
        if (tph != null) {
            return tph;
        }
        
        FileObject sourceFO = refactoring.getRefactoringSource().lookup(FileObject.class);
        if (sourceFO == null){
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
