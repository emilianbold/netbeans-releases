/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.visualweb.insync.faces.refactoring;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.faces.AnyAttrValueScanner;
import org.netbeans.modules.visualweb.insync.faces.FacesUnit;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Document;

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

                        // Set ClasspathInfo
                        ClasspathInfo classpathInfo = FacesRefactoringUtils.getClasspathInfoFor(javaFileObject);                        
                        javaRenameRefactoring.getContext().add(classpathInfo);
                        
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
                                          newName + "." + refactoringSourcefileObject.getExt();
                    
                    // Handle references to this page
                    FacesModelSet facesModelSet = FacesModelSet.getInstance(refactoringSourcefileObject);
                    FacesModel refactoringSourceFacesModel = facesModelSet.getFacesModel(refactoringSourcefileObject);
                    if (refactoringSourceFacesModel != null && refactoringSourceFacesModel.isPageBean()) {
                        MarkupUnit refactoringSourceMarkupUnit = refactoringSourceFacesModel.getMarkupUnit();
                        if (refactoringSourceMarkupUnit != null) {
                            AnyAttrValueScanner scanner = new AnyAttrValueScanner();
                            Model[] models = facesModelSet.getModels();
                            // Rename the bean name in the value binding expressions
                            for (int i = 0; i < models.length; i++) {
                                Model iModel = models[i];
                                if (iModel instanceof FacesModel) {
                                    FacesModel iFacesModel = (FacesModel) iModel;
                                    if (!iFacesModel.isBusted()) {
                                        if (iFacesModel.isPageBean()) {
                                            FileObject iFacesModelMarkupFileObject = iFacesModel.getMarkupFile();
                                            if (iFacesModelMarkupFileObject == null) {
                                                // XXX #132694 Logging illegal state, instead of NPE.
                                                info(new IllegalStateException("The FacesModel returned null markup file, facesModel=" + iFacesModel)); // NOI18N
                                                continue;
                                            }
                                            FileObject iFacesModelMarkupParentFileObject = iFacesModelMarkupFileObject.getParent();                                            
                                            String iFacesModelMarkupParentFileObjectRelativePath = FileUtil.getRelativePath(webFolderFileobject, iFacesModelMarkupParentFileObject);
                                            String toRelativePath = FacesRefactoringUtils.computeRelativePath(iFacesModelMarkupParentFileObject.getPath(),
                                                    refactoringSourcefileObject.getPath());
                                            if (toRelativePath != null) {
                                                MarkupUnit iFacesModelMarkupUnit = iFacesModel.getMarkupUnit();
                                                if (iFacesModelMarkupUnit != null)
                                                {
                                                    Document iFacesModelMarkupDocument = iFacesModelMarkupUnit.getSourceDom();
                                                    int referenceCount = scanner.getReferenceCount(iFacesModelMarkupDocument, toRelativePath);
                                                    if (referenceCount > 0) {
                                                        String adjustedPath = FacesRefactoringUtils.computeRelativePath(
                                                                iFacesModelMarkupParentFileObjectRelativePath, // NII18N
                                                                newRelativePagePath);  // NII18N
                                                        if (adjustedPath != null) {
                                                            refactoringElements.add(getRefactoring(),
                                                                    new RenameResourceReferencesRefactoringElement(iFacesModelMarkupFileObject,
                                                                            iFacesModelMarkupUnit,
                                                                            toRelativePath,
                                                                            adjustedPath));
                                                        }
                                                    }
                                                    
                                                    referenceCount = scanner.getReferenceCount(iFacesModelMarkupDocument, FacesRefactoringUtils.FACES_SERVLET_URL_PATTERN_PREFIX + oldRelativePagePath);
                                                    if (referenceCount > 0) {
                                                        refactoringElements.add(getRefactoring(),
                                                                new RenameResourceReferencesRefactoringElement(iFacesModelMarkupFileObject,
                                                                        iFacesModelMarkupUnit,
                                                                        FacesRefactoringUtils.FACES_SERVLET_URL_PATTERN_PREFIX + oldRelativePagePath,
                                                                        FacesRefactoringUtils.FACES_SERVLET_URL_PATTERN_PREFIX + newRelativePagePath));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
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
                            List <FacesRefactoringUtils.OccurrenceItem> items = FacesRefactoringUtils.getAllFromViewIdOccurrences(webModule, "/" + oldRelativePagePath, "/" + newRelativePagePath);
                            for (FacesRefactoringUtils.OccurrenceItem item : items) {
                                refactoringElements.add(getRefactoring(), new JSFConfigRenameFromViewIdElement(item));
                            }
                            items = FacesRefactoringUtils.getAllToViewOccurrences(webModule, "/" + oldRelativePagePath, "/" + newRelativePagePath);
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

    private static void info(Exception ex) {
        Logger.getLogger(FacesJspFileRenameRefactoringPlugin.class.getName()).log(Level.INFO, null, ex);
    }
}
