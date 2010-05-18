/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.faces.AnyAttrValueScanner;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Document;

/**
 * 
 * @author
 */
public class FacesJspFileMoveRefactoringPlugin extends FacesRefactoringPlugin {
    
    private static final Logger LOGGER = Logger.getLogger(FacesJspFileMoveRefactoringPlugin.class.getName());
	
    private List<FileObject> filesToMove = new ArrayList<FileObject>();
    private Map<FileObject, String> folderPostfix = new HashMap<FileObject, String>();

    /**
     * 
     * @param refactoring 
     */
    public FacesJspFileMoveRefactoringPlugin(MoveRefactoring refactoring) {
        super(refactoring);
        // File or folder move
        setup(refactoring.getRefactoringSource().lookupAll(FileObject.class), "", true);
    }
    
    /**
     * 
     * @param refactoring 
     */
    public FacesJspFileMoveRefactoringPlugin(RenameRefactoring refactoring) {
        super(refactoring);
        // Folder rename
        setup(Collections.singleton(refactoring.getRefactoringSource().lookup(FileObject.class)), "", true);
    }

    private MoveRefactoring getMoveRefactoring() {
        return (MoveRefactoring) getRefactoring();
    }
    
    private RenameRefactoring getRenameRefactoring() {
    	return (RenameRefactoring) getRefactoring();
    }
    
    private void setup(Collection fileObjects, String postfix, boolean recursively) {
        for (Iterator i = fileObjects.iterator(); i.hasNext(); ) {
            FileObject fo = (FileObject) i.next();
            if (FacesRefactoringUtils.isVisualWebJspFile(fo)) {
            	folderPostfix.put(fo, postfix);
                filesToMove.add(fo);
            } else if (!(fo.isFolder())) {
            	folderPostfix.put(fo, postfix);
            } else if (VisibilityQuery.getDefault().isVisible(fo)) {
                //o instanceof DataFolder
                //CVS folders are ignored
                boolean addSlash = !"".equals(postfix);
                Collection col = new ArrayList();
                for (FileObject fo2: fo.getChildren()) {
                    if (!fo2.isFolder() || (fo2.isFolder() && recursively)) 
                        col.add(fo2);
                }
                setup(col, postfix +(addSlash?"/":"") +fo.getName(), recursively); // NOI18N
            }
        }
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }
    
    @Override
    public Problem fastCheckParameters() {    	
    	if (getRefactoring() instanceof MoveRefactoring) {
    		FileObject fileObject = getMoveRefactoring().getRefactoringSource().lookup(FileObject.class);
    		URL targetURL = getMoveRefactoring().getTarget().lookup(URL.class);
	        if (fileObject != null) {
                if (targetURL == null) {
                    return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeNull")); // NOI18N
                } 
                if (FacesRefactoringUtils.isFileUnderDocumentRoot(fileObject)) {
	                if (fileObject.isFolder()) {
	                	if (FacesRefactoringUtils.isSpecialFolderName(fileObject.getNameExt())) {
	                		return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_CannotMoveSpecialFolders")); // NOI18N
	                	}
	                } else {	                    
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
	                                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
	                            }
	                            targetFileObject = FileUtil.toFileObject(file);
	                            if (targetFileObject == null) {
	                                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
	                            }
	                        }
	                        Project targetFileObjectProject = FileOwnerQuery.getOwner(targetFileObject);
	                        if (targetFileObjectProject == null) {
	                            return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotInAnOpenProject")); // NOI18N                            
	                        }
	                                               
	                        if (!project.equals(targetFileObjectProject)) {
	                            return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotInSameProject")); // NOI18N
	                        }
	                        
	                        if (!targetFileObject.isFolder()) {
	                            return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotAFolder")); // NOI18N
	                        }
	                        
	                        if (targetFileObject.equals(fileObject.getParent())) {
	                            return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_CannotMoveToSameLocation")); // NOI18N
	                        }
	                        
	                        FileObject targetFileObjectWithSameName = targetFileObject.getFileObject(fileObject.getName(), fileObject.getExt());
	                        if (targetFileObjectWithSameName != null && targetFileObjectWithSameName.isValid()) {
	                            return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetJspFileAlreadyExists")); // NOI18N
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
	                                return new Problem(false, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "WRN_MovingProjectStartPage")); // NOI18N
	                            }
	                        }
	                    }
	                }
	            }
	        }
    	} else if (getRefactoring() instanceof RenameRefactoring) {
    		FileObject fileObject = getRefactoring().getRefactoringSource().lookup(FileObject.class);	
            String newName = getRenameRefactoring().getNewName();
            if (fileObject != null) {
            	if (FacesRefactoringUtils.isFileUnderDocumentRoot(fileObject)) {
            		if (fileObject.isFolder()) {
            			if (FacesRefactoringUtils.isSpecialFolderName(fileObject.getNameExt())) {
            				return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_CannotRenameSpecialFolders")); // NOI18N
            			}
            			if (newName == null) {
                            return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_NewNameCannotBeNull")); // NOI18N
                        } else {
                            newName = newName.trim();
                            if (newName.length() == 0) {
                                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_NewNameCannotBeEmpty")); // NOI18N
                            }
                            if (newName.equals(fileObject.getName())) {
                                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_NewNameSameAsOldName")); // TODO I18N
                            }
                            if (!Utilities.isJavaIdentifier(newName)) {
                                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_NewNameIsNotAValidJavaIdentifier")); // TODO I18N
                            }
                        }
                        FileObject siblingWithNewName = fileObject.getParent().getFileObject(newName, fileObject.getExt());
                        if (siblingWithNewName != null && siblingWithNewName.isValid()) {
                            return new Problem(false, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_NewNameFolderAlreadyExists")); // NOI18N
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
        if (getRefactoring() instanceof MoveRefactoring) {
        	URL targetURL = getMoveRefactoring().getTarget().lookup(URL.class);
            
            if (targetURL == null) {
                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeNull")); // NOI18N
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
                    return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                }
                targetFileObject = FileUtil.toFileObject(file);
                if (targetFileObject == null) {
                    return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                }
            }
            Project targetFileObjectProject = FileOwnerQuery.getOwner(targetFileObject);
            if (targetFileObjectProject == null) {
                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotInAnOpenProject")); // NOI18N                            
            }           
            
            if (!targetFileObject.isFolder()) {
                return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotAFolder")); // NOI18N
            }
            
            FileObject targetPageBeanRoot = JsfProjectUtils.getPageBeanRoot(targetFileObjectProject);
            if (targetPageBeanRoot == null) {
                // TODO
            }
            
            FileObject targetDocumentRoot = JsfProjectUtils.getDocumentRoot(targetFileObjectProject);
            if (targetDocumentRoot == null) {
                // TODO
            }
            
            // Compute relative path of target folder to document root.
            String targetRelativePath = FileUtil.getRelativePath(targetDocumentRoot, targetFileObject); // NOI18N
            
	        for (FileObject refactoringSourcefileObject : filesToMove) {
	            if (FacesRefactoringUtils.isFileUnderDocumentRoot(refactoringSourcefileObject)) {
	                if (FacesRefactoringUtils.isVisualWebJspFile(refactoringSourcefileObject)) {
	                    Project project = FileOwnerQuery.getOwner(refactoringSourcefileObject);
	                    if (project == null) {
	                        return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_SourceIsNotInAnOpenProject")); // NOI18N
	                    }
	                    
	                    if (!project.equals(targetFileObjectProject)) {
	                        return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotInSameProject")); // NOI18N
	                    }
	                    
	                    String folderPostfixForFileToMoveWithDots = ((String) folderPostfix.get(refactoringSourcefileObject));
	                    
	                    // Invoke Java refactoring (first) only if original refactoring was invoked on JSP.
	                    if (!isDelegatedRefactoring(getRefactoring())) {
	                        FileObject javaFileObject = FacesModel.getJavaForJsp(refactoringSourcefileObject);
	                        if (javaFileObject == null) {
	                            // TODO
	                        }
	                        
	                        // Delegate to Java refactoring to rename the backing bean
	                        MoveRefactoring javaMoveRefactoring = new MoveRefactoring(Lookups.singleton(javaFileObject));	                        	                       
	                        
	                        String targetJavaFolderPath = targetRelativePath;	    	                	                        
	    	                
	    	                if (folderPostfixForFileToMoveWithDots != null && folderPostfixForFileToMoveWithDots.length() > 0) {
	    	                	if (targetJavaFolderPath.length() > 0) {
	    	                		targetJavaFolderPath += "/" + folderPostfixForFileToMoveWithDots;
	    	                	} else {
	    	                		targetJavaFolderPath = folderPostfixForFileToMoveWithDots;                		
	    	                	}
	    	                }
	    	                
	                        FileObject targetJavaFolder = targetPageBeanRoot.getFileObject(targetJavaFolderPath);
	                        if (targetJavaFolder == null) {
	                        	try {
	                        		targetJavaFolder = FileUtil.createFolder(targetPageBeanRoot, targetJavaFolderPath);
								} catch (IOException ioe) {
		                            // TODO return problem
		                            ErrorManager.getDefault().notify(ErrorManager.ERROR, ioe);
								}
	                        }
	                        
	                        if (targetJavaFolder == null) {
	                        	return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetPackageDoesNotExist", targetJavaFolderPath));
	                        }
	                        
	                        URL url = URLMapper.findURL(targetJavaFolder, URLMapper.EXTERNAL);
	                        try {
	                            javaMoveRefactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm())));
	                        } catch (MalformedURLException ex) {
	                        	return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetPackageDoesNotExist", targetJavaFolderPath));
	                        }
	                        
	                        // Set ClasspathInfo
	                        ClasspathInfo classpathInfo = FacesRefactoringUtils.getClasspathInfoFor(javaFileObject);                        
	                        javaMoveRefactoring.getContext().add(classpathInfo);
	                        
	                        // Indicate delegation
	                        javaMoveRefactoring.getContext().add(FacesRefactoringsPluginFactory.DELEGATED_REFACTORING);
	                        Problem problem = javaMoveRefactoring.prepare(refactoringSession);
	                        if (problem != null) {
	                            return problem;
	                        }
	                    }
	                    
	                    FileObject webFolderFileobject = JsfProjectUtils.getDocumentRoot(project);
	                    FileObject parentFileObject = refactoringSourcefileObject.getParent();
	                    String parentRelativePath = FileUtil.getRelativePath(webFolderFileobject, parentFileObject);
	                    String oldRelativePagePath = parentRelativePath +
	                                          (parentRelativePath.length() > 0 ? "/" : "") + // NOI18N
	                                          refactoringSourcefileObject.getNameExt();
                    
	                    String newRelativePagePath = targetRelativePath +
	                                          (targetRelativePath.length() > 0 ? "/" : "") + // NOI18N
	                                          ((folderPostfixForFileToMoveWithDots != null && folderPostfixForFileToMoveWithDots.length() > 0) ?
	                                        		  folderPostfixForFileToMoveWithDots + "/": "") + 
	                                          refactoringSourcefileObject.getNameExt();
	                    
	                    // Handle from and to relative references to this page
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
                                                MarkupUnit iFacesModelMarkupUnit = iFacesModel.getMarkupUnit();
                                                if (iFacesModelMarkupUnit != null)
                                                {
                                                    Document iFacesModelMarkupDocument = iFacesModelMarkupUnit.getSourceDom();
                                                    int referenceCount = scanner.getReferenceCount(iFacesModelMarkupDocument,
                                                            FacesRefactoringUtils.FACES_SERVLET_URL_PATTERN_PREFIX + oldRelativePagePath);
                                                    if (referenceCount > 0) {
                                                        refactoringElements.add(getRefactoring(),
                                                                new RenameResourceReferencesRefactoringElement(iFacesModelMarkupFileObject,
                                                                        iFacesModelMarkupUnit,
                                                                        FacesRefactoringUtils.FACES_SERVLET_URL_PATTERN_PREFIX + oldRelativePagePath,
                                                                        FacesRefactoringUtils.FACES_SERVLET_URL_PATTERN_PREFIX + newRelativePagePath));
                                                    }
                                                }
                                                if (iFacesModelMarkupFileObject == null || refactoringSourcefileObject.equals(iFacesModelMarkupFileObject)) {
                                                    continue;
                                                }
                                                if (!filesToMove.contains(iFacesModelMarkupFileObject)) {
                                                    FileObject refactoringSourceParentfileObject = refactoringSourcefileObject.getParent();
                                                    FileObject iFacesModelMarkupParentFileObject = iFacesModelMarkupFileObject.getParent();
                                                    String iFacesModelMarkupFileObjectRelativePath = FileUtil.getRelativePath(webFolderFileobject, iFacesModelMarkupFileObject);
                                                    String iFacesModelMarkupParentFileObjectRelativePath = FileUtil.getRelativePath(webFolderFileobject, iFacesModelMarkupParentFileObject);
                                                    String toRelativePath = FacesRefactoringUtils.computeRelativePath(iFacesModelMarkupParentFileObject.getPath(),
                                                            refactoringSourcefileObject.getPath());
                                                    if (toRelativePath != null) {
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
                                                        }
                                                    }
        
                                                    String fromRelativePath = FacesRefactoringUtils.computeRelativePath(refactoringSourceParentfileObject.getPath(),
                                                            iFacesModelMarkupFileObject.getPath());
                                                    if (fromRelativePath != null) {                                                    
                                                        Document refactoringSourceMarkupDocument = refactoringSourceMarkupUnit.getSourceDom();
                                                        int referenceCount = scanner.getReferenceCount(refactoringSourceMarkupDocument, fromRelativePath);
                                                        if (referenceCount > 0) {
                                                            String adjustedPath = FacesRefactoringUtils.computeRelativePath(
                                                                    targetRelativePath, // NII18N
                                                                    iFacesModelMarkupFileObjectRelativePath);  // NII18N
                                                            if (adjustedPath != null) {
                                                                refactoringElements.add(getRefactoring(),
                                                                        new RenameResourceReferencesRefactoringElement(iFacesModelMarkupFileObject,
                                                                                refactoringSourceMarkupUnit,
                                                                                fromRelativePath,
                                                                                adjustedPath));
                                                            }
                                                        }
                                                    }                                        
                                                }                                                
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Handle outgoing references to other resources
	                    
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
        } else if (getRefactoring() instanceof RenameRefactoring) {
        	String newName = getRenameRefactoring().getNewName();
        	
        	String targetRelativePath = null;
        	
        	// Is this a folder rename
    		FileObject folder = getRefactoring().getRefactoringSource().lookup(FileObject.class);
    		if (folder != null) { // Folder rename
    			assert folder.isFolder();
    			Project project = FileOwnerQuery.getOwner(folder);
    			if (project == null) {
                    return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_SourceIsNotInAnOpenProject")); // NOI18N
                }
    			
                FileObject documentRoot = JsfProjectUtils.getDocumentRoot(project);
                if (documentRoot == null) {
                    // TODO
                }
                
                String parentRelativePath = FileUtil.getRelativePath(documentRoot, folder.getParent());
                if (parentRelativePath.length() == 0) { 
                	targetRelativePath = newName;
                } else {
                	targetRelativePath = parentRelativePath + "/" + newName;
                }
    		}
        	
    		// compute the new target and make sure it exists
	        for (FileObject refactoringSourcefileObject : filesToMove) {
	            if (FacesRefactoringUtils.isFileUnderDocumentRoot(refactoringSourcefileObject)) {
	                if (FacesRefactoringUtils.isVisualWebJspFile(refactoringSourcefileObject)) {
	                    Project project = FileOwnerQuery.getOwner(refactoringSourcefileObject);
	                    if (project == null) {
	                        return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_SourceIsNotInAnOpenProject")); // NOI18N
	                    }
	                    
	                    String folderRelativePath = FileUtil.getRelativePath(folder, refactoringSourcefileObject.getParent());
	                    
	                    // Invoke Java refactoring (first) only if original refactoring was invoked on JSP.
	                    if (!isDelegatedRefactoring(getRefactoring())) {
	                        FileObject javaFileObject = FacesModel.getJavaForJsp(refactoringSourcefileObject);
	                        if (javaFileObject == null) {
	                            // TODO
	                        }
	                        
	                        // Delegate to Java refactoring to rename the backing bean
	                        MoveRefactoring javaMoveRefactoring = new MoveRefactoring(Lookups.singleton(javaFileObject));	                        	                       
	                        
	                        String targetJavaFolderPath = targetRelativePath;	    	                	                        
	    	                
	    	                if (folderRelativePath != null && folderRelativePath.length() > 0) {
	    	                	if (targetJavaFolderPath.length() > 0) {
	    	                		targetJavaFolderPath += "/" + folderRelativePath;
	    	                	} else {
	    	                		targetJavaFolderPath = folderRelativePath;                		
	    	                	}
	    	                }
	    	                
	    	                FileObject pageBeanRoot = JsfProjectUtils.getPageBeanRoot(project);
	    	                
	                        FileObject targetJavaFolder = pageBeanRoot.getFileObject(targetJavaFolderPath);
	                        if (targetJavaFolder == null) {
	                        	try {
	                        		targetJavaFolder = FileUtil.createFolder(pageBeanRoot, targetJavaFolderPath);
								} catch (IOException ioe) {
		                            // TODO return problem
		                            ErrorManager.getDefault().notify(ErrorManager.ERROR, ioe);
								}
	                        }
	                        
	                        if (targetJavaFolder == null) {
	                        	return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetPackageDoesNotExist", targetJavaFolderPath));
	                        }
	                        
	                        URL url = URLMapper.findURL(targetJavaFolder, URLMapper.EXTERNAL);
	                        try {
	                            javaMoveRefactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm())));
	                        } catch (MalformedURLException ex) {
	                        	return new Problem(true, NbBundle.getMessage(FacesJspFileMoveRefactoringPlugin.class, "ERR_TargetPackageDoesNotExist", targetJavaFolderPath));
	                        }
	                        
	                        // Set ClasspathInfo
	                        ClasspathInfo classpathInfo = FacesRefactoringUtils.getClasspathInfoFor(javaFileObject);                        
	                        javaMoveRefactoring.getContext().add(classpathInfo);
	                        
	                        // Indicate delegation
	                        javaMoveRefactoring.getContext().add(FacesRefactoringsPluginFactory.DELEGATED_REFACTORING);
	                        Problem problem = javaMoveRefactoring.prepare(refactoringSession);
	                        if (problem != null) {
	                            return problem;
	                        }
	                    }
	                    
	                    FileObject webFolderFileobject = JsfProjectUtils.getDocumentRoot(project);
	                    FileObject parentFileObject = refactoringSourcefileObject.getParent();
	                    String parentRelativePath = FileUtil.getRelativePath(webFolderFileobject, parentFileObject);
	                    String oldRelativePagePath = parentRelativePath +
	                                          (parentRelativePath.length() > 0 ? "/" : "") + // NOI18N
	                                          refactoringSourcefileObject.getNameExt();
                    
	                    String newRelativePagePath = targetRelativePath +
	                                          (targetRelativePath.length() > 0 ? "/" : "") + // NOI18N
	                                          ((folderRelativePath != null && folderRelativePath.length() > 0) ?
	                                        		  folderRelativePath + "/": "") + 
	                                          refactoringSourcefileObject.getNameExt();
	                    
                        // Handle from and to relative references to this page
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
                                                if (iFacesModelMarkupFileObject == null || refactoringSourcefileObject.equals(iFacesModelMarkupFileObject)) {
                                                    continue;
                                                }
                                                if (!filesToMove.contains(iFacesModelMarkupFileObject)) {
                                                    FileObject refactoringSourceParentfileObject = refactoringSourcefileObject.getParent();
                                                    FileObject iFacesModelMarkupParentFileObject = iFacesModelMarkupFileObject.getParent();
                                                    String iFacesModelMarkupFileObjectRelativePath = FileUtil.getRelativePath(webFolderFileobject, iFacesModelMarkupFileObject);
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
                                                        }
                                                    }
        
                                                    String fromRelativePath = FacesRefactoringUtils.computeRelativePath(refactoringSourceParentfileObject.getPath(),
                                                            iFacesModelMarkupFileObject.getPath());
                                                    if (fromRelativePath != null) {                                                    
                                                        Document refactoringSourceMarkupDocument = refactoringSourceMarkupUnit.getSourceDom();
                                                        int referenceCount = scanner.getReferenceCount(refactoringSourceMarkupDocument, fromRelativePath);
                                                        if (referenceCount > 0) {
                                                            String adjustedPath = FacesRefactoringUtils.computeRelativePath(
                                                                    targetRelativePath, // NII18N
                                                                    iFacesModelMarkupFileObjectRelativePath);  // NII18N
                                                            if (adjustedPath != null) {
                                                                refactoringElements.add(getRefactoring(),
                                                                        new RenameResourceReferencesRefactoringElement(iFacesModelMarkupFileObject,
                                                                                refactoringSourceMarkupUnit,
                                                                                fromRelativePath,
                                                                                adjustedPath));
                                                            }
                                                        }
                                                    }                                        
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Handle outgoing references to other resources
                        
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
	        
            // Perform modeling of all children after folder rename
            refactoringElements.addFileChange(getRefactoring(), new PostProcessRenamedFolderRefactoringElement(folder, newName));
    	}

        return null;
    }
}
