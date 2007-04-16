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

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * 
 * @author
 */
public class FacesJspFileMoveRefactoringPlugin extends FacesRefactoringPlugin {
    
    private static final Logger LOGGER = Logger.getLogger(FacesJspFileMoveRefactoringPlugin.class.getName());

    /**
     * 
     * @param refactoring 
     */
    public FacesJspFileMoveRefactoringPlugin(MoveRefactoring refactoring) {
        super(refactoring);
    }

    private MoveRefactoring getMoveRefactoring() {
        return (MoveRefactoring) getRefactoring();
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }
    
    @Override
    public Problem fastCheckParameters() {
        FileObject fileObject = getMoveRefactoring().getRefactoringSource().lookup(FileObject.class);
        if (fileObject != null) {
            URL targetURL = getMoveRefactoring().getTarget().lookup(URL.class);
            if (FacesRefactoringUtils.isFileUnderDocumentRoot(fileObject)) {
                if (fileObject.isFolder()) {
                    return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_FolderMoveNotImplementedYet")); // NOI18N
                } else {
                    if (targetURL == null) {
                        return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetLocationCannotBeNull")); // NOI18N
                    } 
                    
                    if (FacesRefactoringUtils.isVisualWebJspFile(fileObject)) {
                        Project project = FileOwnerQuery.getOwner(fileObject);
                        
                        FileObject targetFileObject = URLMapper.findFileObject(targetURL);
                        if (targetFileObject == null) {
                            File file = null;
                            try {
                                file = new File(targetURL.toURI());
                            } catch (URISyntaxException e) {
                                Exceptions.printStackTrace(e);
                            }
                            if (file == null) {
                                return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                            }
                            targetFileObject = FileUtil.toFileObject(file);
                            if (targetFileObject == null) {
                                return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                            }
                        }
                        Project targetFileObjectProject = FileOwnerQuery.getOwner(targetFileObject);
                        if (targetFileObjectProject == null) {
                            return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetLocationIsNotInAnOpenProject")); // NOI18N                            
                        }
                                               
                        if (!project.equals(targetFileObjectProject)) {
                            return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetLocationIsNotInSameProject")); // NOI18N
                        }
                        
                        if (!targetFileObject.isFolder()) {
                            return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetLocationIsNotAFolder")); // NOI18N
                        }
                        
                        if (targetFileObject.equals(fileObject.getParent())) {
                            return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_CannotMoveToSameLocation")); // NOI18N
                        }
                        
                        FileObject targetFileObjectWithSameName = targetFileObject.getFileObject(fileObject.getName(), fileObject.getExt());
                        if (targetFileObjectWithSameName != null && targetFileObjectWithSameName.isValid()) {
                            return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetJspFileAlreadyExists")); // NOI18N
                        }
                        
                        Project fileObjectProject = FileOwnerQuery.getOwner(fileObject);
                        if (fileObjectProject.equals(targetFileObjectProject)) {
//                            if (FacesRefactoringUtils.isSpecialFolderName(targetFileObject.getNameExt()) {
//                                return new Problem(false, NbBundle.getMessage(FacesRenameRefactoringPlugin.class, "WRN_TargetLocationIsASpecialFolder")); // NOI18N
//                            }
                        } else {
                            boolean isTargetProjectAVisualWebProject = JsfProjectUtils.isJsfProject(targetFileObjectProject);
                            if (isTargetProjectAVisualWebProject) {
                                
                            }
                            boolean isStartPage = JsfProjectUtils.isStartPage(fileObject);
                            if (isStartPage) {
                                return new Problem(false, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "WRN_MovingProjectStartPage")); // NOI18N
                            }
                        }
                    }
                }
            }
        }       
        return null;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        RefactoringSession refactoringSession = refactoringElements.getSession();
        FileObject refactoringSourcefileObject = getRefactoring().getRefactoringSource().lookup(FileObject.class);
        if (refactoringSourcefileObject != null) {
            if (FacesRefactoringUtils.isFileUnderDocumentRoot(refactoringSourcefileObject)) {
                if (FacesRefactoringUtils.isVisualWebJspFile(refactoringSourcefileObject)) {
                    Project project = FileOwnerQuery.getOwner(refactoringSourcefileObject);
                    if (project == null) {
                        return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_")); // NOI18N
                    }
                    
                    URL targetURL = getMoveRefactoring().getTarget().lookup(URL.class);
                    
                    if (targetURL == null) {
                        return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetLocationCannotBeNull")); // NOI18N
                    }
                    
                    FileObject targetFileObject = URLMapper.findFileObject(targetURL);
                    if (targetFileObject == null) {
                        File file = null;
                        try {
                            file = new File(targetURL.toURI());
                        } catch (URISyntaxException e) {
                            Exceptions.printStackTrace(e);
                        }
                        if (file == null) {
                            return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                        }
                        targetFileObject = FileUtil.toFileObject(file);
                        if (targetFileObject == null) {
                            return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                        }
                    }
                    Project targetFileObjectProject = FileOwnerQuery.getOwner(targetFileObject);
                    if (targetFileObjectProject == null) {
                        return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetLocationIsNotInAnOpenProject")); // NOI18N                            
                    }
                    
                    if (!project.equals(targetFileObjectProject)) {
                        return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetLocationIsNotInSameProject")); // NOI18N
                    }
                    
                    if (!targetFileObject.isFolder()) {
                        return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetLocationIsNotAFolder")); // NOI18N
                    }
                    
                    // Invoke Java refactoring (first) only if original refactoring was invoked on JSP.
                    if (!isDelegatedRefactoring(getRefactoring())) {
                        FileObject javaFileObject = FacesModel.getJavaForJsp(refactoringSourcefileObject);
                        if (javaFileObject == null) {
                            // TODO
                        }
                        
                        FileObject targetDocumentRoot = JsfProjectUtils.getDocumentRoot(targetFileObjectProject);
                        if (targetDocumentRoot == null) {
                            // TODO
                        }
                        
                        // Compute relative path of target folder to document root.
                        String targetRelativePath = FileUtil.getRelativePath(targetDocumentRoot, targetFileObject); // NOI18N
                        
                        // Deleggate to Java refactoring to rename the backing bean
                        MoveRefactoring javaMoveRefactoring = new MoveRefactoring(Lookups.singleton(javaFileObject));
                        
                        // new folder
                        
                        FileObject targetPageBeanRoot = JsfProjectUtils.getPageBeanRoot(targetFileObjectProject);
                        if (targetPageBeanRoot == null) {
                            // TODO
                        }
                        
                        FileObject targetJavaFolder = targetPageBeanRoot.getFileObject(targetRelativePath);
                        if (targetJavaFolder == null) {
                            // TODO
                        }
                        
                        URL url = URLMapper.findURL(targetJavaFolder, URLMapper.EXTERNAL);
                        try {
                            javaMoveRefactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm())));
                        } catch (MalformedURLException ex) {
                            // TODO return problem
                            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
                        }
                        
                        // Indicate delegation
                        javaMoveRefactoring.getContext().add(FacesRefactoringsPluginFactory.DELEGATED_REFACTORING);
                        Problem problem = javaMoveRefactoring.prepare(refactoringSession);
                        if (problem != null) {
                            return problem;
                        }
                    }
                    
                    // Add a refactoring element to set the start page
                    if (JsfProjectUtils.isStartPage(refactoringSourcefileObject)) {
                        FileObject webFolderFileobject = JsfProjectUtils.getDocumentRoot(project);
                        FileObject parentObject = refactoringSourcefileObject.getParent();
                        String parentRelativePath = FileUtil.getRelativePath(webFolderFileobject, parentObject);
                        String oldStartPage = parentRelativePath +
                                              (parentRelativePath.length() > 0 ? "/" : "") + // NOI18N
                                              refactoringSourcefileObject.getNameExt();
                        
                        FileObject targetDocumentRoot = JsfProjectUtils.getDocumentRoot(targetFileObjectProject);
                        if (targetDocumentRoot == null) {
                            // TODO
                        }
                        
                        String targetRelativePath = FileUtil.getRelativePath(targetDocumentRoot, targetFileObject);
                        String newStartPage = targetRelativePath +
                                              (targetRelativePath.length() > 0 ? "/" : "") + // NOI18N
                                              refactoringSourcefileObject.getNameExt();
                        refactoringElements.addFileChange(getRefactoring(), new SetProjectStartPageRefactoringElement(project, oldStartPage, newStartPage));
                    }
                }
            }
        }

        return null;
    }
}
