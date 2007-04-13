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
import java.net.URISyntaxException;
import java.net.URL;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.visualweb.insync.faces.FacesUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * 
 * @author
 */
public class FacesJavaFileMoveRefactoringPlugin extends FacesRefactoringPlugin {

    /**
     * 
     * @param refactoring 
     */
    public FacesJavaFileMoveRefactoringPlugin(MoveRefactoring refactoring) {
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
        URL targetURL = getMoveRefactoring().getTarget().lookup(URL.class);
        if (fileObject != null) {
            if (FacesRefactoringUtils.isFileUnderDocumentRoot(fileObject)) {
                if (fileObject.isFolder()) {
                    return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_FolderMoveNotImplementedYet")); // NOI18N
                } else {
                    if (targetURL == null) {
                        return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_TargetLocationCannotBeNull")); // NOI18N
                    } 
                    
                    if (FacesRefactoringUtils.isVisualWebJspFile(fileObject)) {
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
            } else if (FacesRefactoringUtils.isJavaFileObjectOfInterest(fileObject)) {
                if (targetURL != null) {                   
                    
                }                
            }
        }       
        return null;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {

        return null;
    }
}
