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

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.visualweb.insync.faces.FacesUnit;
import org.netbeans.modules.visualweb.insync.faces.refactoring.FacesRefactoringUtils.ManagedBeanNameItem;
import org.netbeans.modules.visualweb.insync.faces.refactoring.FacesRefactoringUtils.NavigationToViewIdItem;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

public class FacesJspFileRenameRefactoringPlugin extends FacesRefactoringPlugin {
    
    private static final Logger LOGGER = Logger.getLogger(FacesJspFileRenameRefactoringPlugin.class.getName());

    public FacesJspFileRenameRefactoringPlugin(RenameRefactoring refactoring) {
        super(refactoring);
    }
    
    private RenameRefactoring getRenameRefactoring() {
        return (RenameRefactoring) getRefactoring();
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }

    @Override
    public Problem fastCheckParameters() {
        // Do the checking only if not a delegated refactoring
        if (!isDelegatedRefactoring(getRefactoring())) {
            FileObject fileObject = getRenameRefactoring().getRefactoringSource().lookup(FileObject.class);
            String newName = getRenameRefactoring().getNewName();
            if (fileObject != null) {
                if (FacesRefactoringUtils.isFileUnderDocumentRoot(fileObject)) {                
                    if (newName == null) {
                        return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_NewNameCannotBeNull")); // NOI18N
                    } else {
                        newName = newName.trim();
                        if (newName.length() == 0) {
                            return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_NewNameCannotBeEmpty")); // NOI18N
                        }
                        if (newName.equals(fileObject.getName())) {
                            return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_NewNameSameAsOldName")); // TODO I18N
                        }
                        if (!Utilities.isJavaIdentifier(newName)) {
                            return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_NewNameIsNotAValidJavaIdentifier")); // TODO I18N
                        }
                        if (FacesUnit.isImplicitBeanName(newName)) {
                            return new Problem(true,  NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_ReservedIdentifier", newName)); //NOI18N
                        }
                        if (!Character.isUpperCase(newName.charAt(0))) {
                            return new Problem(false, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "WRN_NewNameDoesNotStartWithUppercaseLetter")); // NOI18N
                        }
                    }
                    FileObject siblingWithNewName = fileObject.getParent().getFileObject(newName, fileObject.getExt());
                    if (siblingWithNewName != null && siblingWithNewName.isValid()) {
                        return new Problem(false, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_NewNameJspFileAlreadyExists")); // NOI18N
                    }
                    FileObject javaFileObject = FacesModel.getJavaForJsp(fileObject);
                    if (javaFileObject != null) {
                        siblingWithNewName = javaFileObject.getParent().getFileObject(newName, javaFileObject.getExt());
                        if (siblingWithNewName != null && siblingWithNewName.isValid()) {
                            return new Problem(false, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_NewNameJavaFileAlreadyExists")); // NOI18N
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements){
        RefactoringSession refactoringSession = refactoringElements.getSession();
        FileObject refactoringSourcefileObject = getRenameRefactoring().getRefactoringSource().lookup(FileObject.class);
        if (refactoringSourcefileObject != null) {
            if (FacesRefactoringUtils.isFileUnderDocumentRoot(refactoringSourcefileObject)) {
                if (FacesRefactoringUtils.isVisualWebJspFile(refactoringSourcefileObject)) {
                    Project project = FileOwnerQuery.getOwner(refactoringSourcefileObject);
                    if (project == null) {
                        return null;
                    }
                    
                    String newName = getRenameRefactoring().getNewName();
                    if (newName == null) {
                        return new Problem(true, NbBundle.getMessage(FacesJspFileRenameRefactoringPlugin.class, "ERR_NewNameCannotBeNull")); // NOI18N
                    }
                    
                    // Invoke Java refactoring (first) only if original refactoring was invoked on JSP.
                    if (!isDelegatedRefactoring(getRefactoring())) {
                        FileObject javaFileObject = FacesModel.getJavaForJsp(refactoringSourcefileObject);
                        if (javaFileObject == null) {
                            // TODO
                        }
                        
                        // Deleggate to Java refactoring to rename the backing bean
                        RenameRefactoring javaRenameRefactoring = new RenameRefactoring(Lookups.singleton(javaFileObject));
                        javaRenameRefactoring.setNewName(newName);
                        javaRenameRefactoring.setSearchInComments(getRenameRefactoring().isSearchInComments());
                        // Indicate delegation
                        javaRenameRefactoring.getContext().add(FacesRefactoringsPluginFactory.DELEGATED_REFACTORING);
                        Problem problem = javaRenameRefactoring.prepare(refactoringSession);
                        if (problem != null) {
                            return problem;
                        }
                    }
                    
                    FileObject webFolderFileobject = JsfProjectUtils.getDocumentRoot(project);
                    FileObject parentObject = refactoringSourcefileObject.getParent();
                    String parentRelativePath = FileUtil.getRelativePath(webFolderFileobject, parentObject);
                    String oldRelativePagePath = parentRelativePath +
                                          (parentRelativePath.length() > 0 ? "/" : "") + // NOI18N
                                          refactoringSourcefileObject.getNameExt();
                    String newRelativePagePath = parentRelativePath +
                                          (parentRelativePath.length() > 0 ? "/" : "") + // NOI18N
                                          newName + "." +refactoringSourcefileObject.getExt();
                    
                    // Add a refactoring element to set the start page
                    if (JsfProjectUtils.isStartPage(refactoringSourcefileObject)) {                        
                        refactoringElements.addFileChange(getRefactoring(), new SetProjectStartPageRefactoringElement(project, oldRelativePagePath, newRelativePagePath));
                    }
                    
                    // Handle navigation view ids
                    WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
                    if (webModule != null){
                        // find all jsf configuration files in the web module
                        FileObject[] configs = ConfigurationUtils.getFacesConfigFiles(webModule);
                        
                        if (configs != null){
                            List <FacesRefactoringUtils.OccurrenceItem> items = FacesRefactoringUtils.getAllFromViewIdOccurrences(webModule, oldRelativePagePath, newRelativePagePath);
                            for (FacesRefactoringUtils.OccurrenceItem item : items) {
                                refactoringElements.add(getRefactoring(), new JSFConfigRenameFromViewIdElement(item));
                            }
                            items = FacesRefactoringUtils.getAllToViewOccurrences(webModule, oldRelativePagePath, newRelativePagePath);
                            for (FacesRefactoringUtils.OccurrenceItem item : items) {
                                refactoringElements.add(getRefactoring(), new JSFConfigRenameToViewIdElement(item));
                            }
                        }
                    }
                }
            }
        }

        return null;
    }
}