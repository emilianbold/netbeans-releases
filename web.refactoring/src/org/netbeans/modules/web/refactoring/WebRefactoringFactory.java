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

import java.io.IOException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.refactoring.rename.WebXmlRename;
import org.netbeans.modules.web.refactoring.safedelete.WebXmlSafeDelete;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * A refactoring plugin for Web related refactorings.
 *
 * @author Erno Mononen
 */
public class WebRefactoringFactory implements RefactoringPluginFactory{
    
    public WebRefactoringFactory() {
    }
    
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {

        FileObject sourceFO = refactoring.getRefactoringSource().lookup(FileObject.class);
        if (sourceFO == null){
            return null;
        }
        FileObject ddFile = WebModule.getWebModule(sourceFO).getDeploymentDescriptor();
        Project project = FileOwnerQuery.getOwner(sourceFO);
        ClassPathProvider classPathProvider = project.getLookup().lookup(ClassPathProvider.class);
        String oldFqn = classPathProvider.findClassPath(sourceFO, ClassPath.SOURCE).getResourceName(sourceFO, '.', false);
        
        WebApp webApp = null;
        try{
            webApp = DDProvider.getDefault().getDDRoot(ddFile);
        }catch(IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        
        if (refactoring instanceof RenameRefactoring){
            RenameRefactoring rename = (RenameRefactoring) refactoring;
            return new WebXmlRename(oldFqn, rename, webApp, ddFile);
        } if (refactoring instanceof SafeDeleteRefactoring){
            SafeDeleteRefactoring safeDelete = (SafeDeleteRefactoring) refactoring;
            return new WebXmlSafeDelete(ddFile, webApp, oldFqn, safeDelete);
        }
        
        return null;
    }
    
}
