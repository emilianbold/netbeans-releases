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
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.faces.ElBindingScanner;
import org.netbeans.modules.visualweb.insync.faces.FacesUnit;
import org.netbeans.modules.visualweb.insync.java.JavaClass;
import org.netbeans.modules.visualweb.insync.java.JavaUnit;
import org.netbeans.modules.visualweb.insync.java.Method;
import org.netbeans.modules.visualweb.insync.markup.MarkupUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Document;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;

/**
 * 
 * @author
 */
public class FacesJavaFileMoveRefactoringPlugin extends FacesRefactoringPlugin {
	
    private List<FileObject> filesToMove = new ArrayList<FileObject>();
    private Map<FileObject, String> packagePostfix = new HashMap<FileObject, String>();

    /**
     * 
     * @param refactoring 
     */
    public FacesJavaFileMoveRefactoringPlugin(MoveRefactoring refactoring) {
        super(refactoring);
        // File or folder move
    	setup(refactoring.getRefactoringSource().lookupAll(FileObject.class), "", true);
    }
    
    public FacesJavaFileMoveRefactoringPlugin(RenameRefactoring refactoring) {
        super(refactoring);
        if (!isDelegatedRefactoring(getRefactoring())) {
	        FileObject fo = refactoring.getRefactoringSource().lookup(FileObject.class);
	        if (fo!=null) {
	        	// Folder rename
	            setup(Collections.singletonList(fo), "", true);
	        } else {
	        	// Package rename
	            setup(Collections.singletonList((refactoring.getRefactoringSource().lookup(NonRecursiveFolder.class)).getFolder()), "", false);
	        }
        }
    }
    
    private RenameRefactoring getRenameRefactoring() {
        return (RenameRefactoring) getRefactoring();
    }
    
    private MoveRefactoring getMoveRefactoring() {
        return (MoveRefactoring) getRefactoring();
    }
    
    private void setup(Collection fileObjects, String postfix, boolean recursively) {
        for (Iterator i = fileObjects.iterator(); i.hasNext(); ) {
            FileObject fo = (FileObject) i.next();
            if (FacesRefactoringUtils.isJavaFileObjectOfInterest(fo)) {
                packagePostfix.put(fo, postfix.replace('/', '.'));
                filesToMove.add(fo);
            } else if (!(fo.isFolder())) {
                packagePostfix.put(fo, postfix.replace('/', '.'));
            } else if (VisibilityQuery.getDefault().isVisible(fo)) {
                //o instanceof DataFolder
                //CVS folders are ignored
                boolean addDot = !"".equals(postfix);
                Collection<FileObject> col = new ArrayList<FileObject>();
                for (FileObject fo2: fo.getChildren()) {
                    if (!fo2.isFolder() || (fo2.isFolder() && recursively)) 
                        col.add(fo2);
                }
                setup(col, postfix +(addDot?".":"") +fo.getName(), recursively); // NOI18N
            }
        }
    }
    
    @Override
    public Problem preCheck() {
        return null;
    }
    
    @Override
    public Problem fastCheckParameters() {
        // Don't do the checking only if this is a delegated refactoring
        if (isDelegatedRefactoring(getRefactoring())) {
            return null;
        }
        
        if (getRefactoring() instanceof MoveRefactoring) {
	        FileObject fileObject = getMoveRefactoring().getRefactoringSource().lookup(FileObject.class);
	        URL targetURL = getMoveRefactoring().getTarget().lookup(URL.class);
	        if (fileObject != null) {
        		if (targetURL == null) {
                    return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeNull")); // NOI18N
                }
	        	if (FacesRefactoringUtils.isFolderParentOfPageBeanRoot(fileObject) ||
	        			FacesRefactoringUtils.isFolderPageBeanRoot(fileObject)) {
	        		return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_CannotMovePackageBeanRoot")); // NOI18N
	        	} else if (FacesRefactoringUtils.isFolderUnderPageBeanRoot(fileObject)) {
                    FileObject targetFileObject = URLMapper.findFileObject(targetURL);
                    if (targetFileObject == null) {
                        File file = null;
                        try {
                            file = new File(targetURL.toURI());
                        } catch (URISyntaxException e) {
                            Exceptions.printStackTrace(e);
                        }
                        if (file == null) {
                            return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                        }
                        targetFileObject = FileUtil.toFileObject(file);
                        if (targetFileObject == null) {
                            return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                        }
                    }
                    Project targetFileObjectProject = FileOwnerQuery.getOwner(targetFileObject);
                    if (targetFileObjectProject == null) {
                        return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotInAnOpenProject")); // NOI18N                            
                    }
                    if (!targetFileObject.isFolder()) {
                        return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotAFolder")); // NOI18N
                    }
                    if (targetFileObject.equals(fileObject.getParent())) {
                        return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_CannotMoveToSameLocation")); // NOI18N
                    }
                                        
                    Project fileObjectProject = FileOwnerQuery.getOwner(fileObject);
                    if (fileObjectProject.equals(targetFileObjectProject)) {
                    	// Same project move
                    	FileObject pageBeanRoot = JsfProjectUtils.getPageBeanRoot(fileObjectProject);
                    	String pageBeanRootPackageName = FacesRefactoringUtils.getPackageName(pageBeanRoot);
                    	String targetPackageName = FacesRefactoringUtils.getPackageName(targetFileObject);
                    	if (!targetPackageName.startsWith(pageBeanRootPackageName)) {
                    		return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_CannotMoveFolderOutsideDefaultPackageFolder")); // NOI18N                    		
                    	}

                    } else {
                        if (JsfProjectUtils.isJsfProject(targetFileObjectProject)) {
                            
                        } else {

                        }
                    }
	        	} else {
	        		FileObject targetFileObject = URLMapper.findFileObject(targetURL);
                    if (targetFileObject == null) {
                        File file = null;
                        try {
                            file = new File(targetURL.toURI());
                        } catch (URISyntaxException e) {
                            Exceptions.printStackTrace(e);
                        }
                        if (file == null) {
                            return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                        }
                        targetFileObject = FileUtil.toFileObject(file);
                        if (targetFileObject == null) {
                            return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                        }
                    }
                    Project targetFileObjectProject = FileOwnerQuery.getOwner(targetFileObject);
                    if (targetFileObjectProject == null) {
                        return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotInAnOpenProject")); // NOI18N                            
                    }
                    if (!targetFileObject.isFolder()) {
                        return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotAFolder")); // NOI18N
                    }
	                for (FileObject fileToMove : filesToMove) {
		                if (FacesRefactoringUtils.isJavaFileObjectOfInterest(fileToMove)) {
	                        if ((!FacesRefactoringUtils.isFolderPageBeanRoot(targetFileObject)) && 
	                        		(!FacesRefactoringUtils.isFileUnderPageBeanRoot(targetFileObject))) {
	                            return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotInPageBeanRoot")); // NOI18N
	                        }
		                	// TODO
//		                    FileObject targetFileObjectWithSameName = targetFileObject.getFileObject(fileObject.getName(), fileObject.getExt());
//		                    if (targetFileObjectWithSameName != null && targetFileObjectWithSameName.isValid()) {
//		                        return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetJspFileAlreadyExists")); // NOI18N
//		                    }
		                    
		                    Project fileObjectProject = FileOwnerQuery.getOwner(fileToMove);
		                    if (fileObjectProject.equals(targetFileObjectProject)) {
		
		                    } else {
		                        if (JsfProjectUtils.isJsfProject(targetFileObjectProject)) {
		                            
		                        }
		                        // TODO - Convert to jsp file object
		                        // TODO
//		                        boolean isStartPage = JsfProjectUtils.isStartPage(fileToMove);
//		                        if (isStartPage) {
//		                            return new Problem(false, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "WRN_MovingProjectStartPage")); // NOI18N
//		                        }
		                    }
		                }
	                }
	            }
	        }
        } else if (getRefactoring() instanceof RenameRefactoring) {
            FileObject fileObject = getRefactoring().getRefactoringSource().lookup(FileObject.class);
            if (fileObject != null) {
            	if (FacesRefactoringUtils.isFolderPageBeanRoot(fileObject)) {
            		return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_CannotRenamePackageBeanRootFolder")); // NOI18N
            	}
            } else {
            	NonRecursiveFolder nonRecursiveFolder = getRefactoring().getRefactoringSource().lookup(NonRecursiveFolder.class);
            	if (nonRecursiveFolder != null) {
            		fileObject = nonRecursiveFolder.getFolder();
            		if (FacesRefactoringUtils.isFolderPageBeanRoot(fileObject)) {
                		return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_CannotRenamePackageBeanRoot")); // NOI18N
                	}
            		// Now check if they are renaming a package under default package to outside 
            		if (FacesRefactoringUtils.isFolderUnderPageBeanRoot(fileObject)) {
                        String newName = getRenameRefactoring().getNewName();
                        Project fileObjectProject = FileOwnerQuery.getOwner(fileObject);
                    	FileObject pageBeanRoot = JsfProjectUtils.getPageBeanRoot(fileObjectProject);
                    	String pageBeanRootPackageName = FacesRefactoringUtils.getPackageName(pageBeanRoot);
                    	if (!newName.startsWith(pageBeanRootPackageName)) {
                    		return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_CannotMovePackageOutsideDefaultPackage")); // NOI18N                    		
                    	}
            		}
            	}
            }
        }  
        return null;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
    	// File and folder move
    	if (getRefactoring() instanceof MoveRefactoring) {
    		URL targetURL = null;
            FileObject targetFileObject = null;
            
        	targetURL = getMoveRefactoring().getTarget().lookup(URL.class);
        
            if (targetURL == null) {
                return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeNull")); // NOI18N
            }
            
            targetFileObject = URLMapper.findFileObject(targetURL);
            if (targetFileObject == null) {
                File file = null;
                try {
                    file = new File(targetURL.toURI());
                } catch (URISyntaxException e) {
                    Exceptions.printStackTrace(e);
                }
                if (file == null) {
                    return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                }
                targetFileObject = FileUtil.toFileObject(file);
                if (targetFileObject == null) {
                    return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationCannotBeResolved")); // NOI18N
                }
            }

            Project targetFileObjectProject = FileOwnerQuery.getOwner(targetFileObject);
            if (targetFileObjectProject == null) {
                return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotInAnOpenProject")); // NOI18N                            
            }
            
            if (!targetFileObject.isFolder()) {
                return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotAFolder")); // NOI18N
            }
            
	        for (FileObject refactoringSourcefileObject : filesToMove) {
	            Project project = FileOwnerQuery.getOwner(refactoringSourcefileObject);
            	if (!project.equals(targetFileObjectProject)) {
                    return new Problem(true, NbBundle.getMessage(FacesJavaFileMoveRefactoringPlugin.class, "ERR_TargetLocationIsNotInSameProject")); // NOI18N
                }
            	if (FacesRefactoringUtils.isJavaFileObjectOfInterest(refactoringSourcefileObject)) {                
	                
	                String oldName = refactoringSourcefileObject.getName();
	                FileObject pageBeanRoot = JsfProjectUtils.getPageBeanRoot(project);
	                if (pageBeanRoot == null) {
	                    // TODO
	                }
	                
	                FileObject fileObjectParent = refactoringSourcefileObject.getParent();
	                
	                String packagePostfixForFileToMove = (String) packagePostfix.get(refactoringSourcefileObject);
	                
	                // Compute relative path of parent folder to bean root.
	                String parentsRelativePathWithDollars = FileUtil.getRelativePath(pageBeanRoot, fileObjectParent).replace('/', '$'); // NOI18N
	                String targetRelativePath = FileUtil.getRelativePath(pageBeanRoot, targetFileObject); // NOI18N
	                String targetRelativePathWithDollars = targetRelativePath.replace('/', '$'); // NOI18N
	
	                if (packagePostfixForFileToMove != null && packagePostfixForFileToMove.length() > 0) {
	                	if (targetRelativePathWithDollars.length() > 0) {
	                		targetRelativePathWithDollars += "$" + packagePostfixForFileToMove.replace('.', '$');
	                	} else {
	                		targetRelativePathWithDollars = packagePostfixForFileToMove.replace('.', '$');                		
	                	}
	                }
	                
	                String oldBeanName = parentsRelativePathWithDollars 
	                    + (parentsRelativePathWithDollars.length() == 0 ? "" : "$") // NOI18N
	                    + oldName;
	                String newBeanName = targetRelativePathWithDollars
	                    + (targetRelativePathWithDollars.length() == 0 ? "" : "$") // NOI18N TODO compute target folder relative path
	                    + oldName;                
	                // Fix up
	                newBeanName = FacesUnit.fixPossiblyImplicitBeanName(newBeanName);
	                
	                String oldValueBindingPrefix = "#{" + oldBeanName + ".";
	                String newValueBindingPrefix = "#{" + newBeanName + ".";

	                FacesModelSet facesModelSet = FacesModelSet.getInstance(refactoringSourcefileObject);
	                
	                boolean isRefactoringSourcePageBean = false;
	                String refactoringSourceFQN = null;
	                Model refactoringSourceModel = facesModelSet.getModel(refactoringSourcefileObject);
	                if (refactoringSourceModel instanceof FacesModel) {
	                    FacesModel refactoringSourceFacesModel = (FacesModel) refactoringSourceModel;
	                    isRefactoringSourcePageBean = refactoringSourceFacesModel.isPageBean();
	                    if (!refactoringSourceFacesModel.isBusted()) {
	                        JavaUnit javaUnit = refactoringSourceFacesModel.getJavaUnit();
	                        if (javaUnit != null) {
	                            refactoringSourceFQN = javaUnit.getJavaClass().getName();
	                        }
	                    }
	                }
	                
	                Model[] models = facesModelSet.getModels();
	                // Rename the bean name in the value binding expressions
	                for (int i=0; i < models.length; i++) {
	                    Model iModel = models[i];
	                    if (iModel instanceof FacesModel) {
	                        FacesModel iFacesModel = (FacesModel) iModel;
	                        if (!iFacesModel.isBusted()) {
	                            FileObject javaFileObject = iFacesModel.getJavaFile();
	                            if (javaFileObject != null) {
	                                try {
	                                    // Replace #{oldBeanName. ...} with #{newBeanName....} in string literals in all java files of FacesModels (JavaUnits)
	                                    JavaSource javaSource = JavaSource.forFileObject(javaFileObject);
	                                    ModificationResult modificationResult = javaSource.runModificationTask(
	                                            new StringLiteralTask(oldValueBindingPrefix, newValueBindingPrefix, StringLiteralTransformer.MatchKind.PREFIX)
	                                            );
	                                    refactoringElements.registerTransaction(new RetoucheCommit(Collections.singleton(modificationResult)));
	                                    for (FileObject jfo : modificationResult.getModifiedFileObjects()) {
	                                        for (Difference dif: modificationResult.getDifferences(jfo)) {
	                                            String old = dif.getOldText();
	                                            if (old!=null) {
	                                                //TODO: workaround
	                                                //generator issue?
	                                                refactoringElements.add(getRefactoring(),DiffElement.create(dif, jfo, modificationResult));
	                                            }
	                                        }
	                                    }
	                                } catch (IOException ex) {
	                                    throw (RuntimeException) new RuntimeException().initCause(ex);
	                                }
	                            }
	                            
	                            JavaUnit javaUnit = iFacesModel.getJavaUnit();
	                            if (refactoringSourceFQN != null && javaUnit != null && !isRefactoringSourcePageBean) {
	                                final String finalRefactoringSourceFQN = refactoringSourceFQN;
	                                final JavaClass javaClass = javaUnit.getJavaClass();                                
	                                if (javaClass != null) {
	                                    Method method = javaClass.getMethod("get" + oldBeanName, new Class[0]);
	                                    if (method != null) {
	                                        final ElementHandle<ExecutableElement> elementHandle = method.getElementHandle();
	                                        if (elementHandle != null) {
	                                            FileObject facesJavaFileObject = iFacesModel.getJavaFile();
	                                            JavaSource javaSource = JavaSource.forFileObject(facesJavaFileObject);
	                                            if (javaSource != null) {
	                                                // Rename accessor methods
	                                                final RenameRefactoring[] methodRenameRefactoring = new RenameRefactoring[1];
	                                                try {
	                                                    javaSource.runUserActionTask(new CancellableTask<CompilationController>() {
	                                                        public void cancel() {
	                                                        }
	
	                                                        public void run(CompilationController compilationController)
	                                                            throws Exception {
	                                                            compilationController.toPhase(Phase.RESOLVED);
	                                                            Element element = elementHandle.resolve(compilationController);
	                                                            if (element != null && element.getKind() ==  ElementKind.METHOD) {
	                                                                ExecutableElement executableElement = (ExecutableElement) element;
	                                                                TypeMirror typeMirror = executableElement.getReturnType();
	                                                                if (typeMirror != null && typeMirror.getKind() == TypeKind.DECLARED) {
	                                                                    DeclaredType declaredType = (DeclaredType) typeMirror;
	                                                                    TypeElement typeElement = (TypeElement) declaredType.asElement();
	                                                                    
	                                                                    if (typeElement.getQualifiedName().toString().equals(finalRefactoringSourceFQN)) {
	                                                                        TreePathHandle treePathHandle = TreePathHandle.create(compilationController.getTrees().getPath(element), compilationController);
	                                                                        methodRenameRefactoring[0] = new RenameRefactoring(Lookups.fixed(treePathHandle));
	                                                                        methodRenameRefactoring[0].getContext().add(compilationController);
	                                                                    }
	                                                                }
	                                                            }
	                                                        }
	                                                    }, true);                                                    
	                                                    
	                                                    if (methodRenameRefactoring[0] != null) {                                                        
	                                                        methodRenameRefactoring[0].setNewName("get" + newBeanName);
	                                                        
	                                                        // Flag delegation
	                                                        methodRenameRefactoring[0].getContext().add(FacesRefactoringsPluginFactory.DELEGATED_REFACTORING);
	                                                        
	                                                        Problem problem = methodRenameRefactoring[0].prepare(refactoringElements.getSession());
	                                                        if (problem != null) {
	                                                            return problem;
	                                                        }
	                                                    }
	                                                } catch (IOException ex) {
	                                                }
	                                                
	                                                try {
	                                                    ModificationResult modificationResult = javaSource.runModificationTask(
	                                                            new StringLiteralTask(oldBeanName, newBeanName, StringLiteralTransformer.MatchKind.EXACT)
	                                                            );
	                                                    refactoringElements.registerTransaction(new RetoucheCommit(Collections.singleton(modificationResult)));
	                                                    for (FileObject jfo : modificationResult.getModifiedFileObjects()) {
	                                                        for (Difference dif: modificationResult.getDifferences(jfo)) {
	                                                            String old = dif.getOldText();
	                                                            if (old!=null) {
	                                                                //TODO: workaround
	                                                                //generator issue?
	                                                                refactoringElements.add(getRefactoring(),DiffElement.create(dif, jfo, modificationResult));
	                                                            }
	                                                        }
	                                                    }
	                                                } catch (IOException ex) {
	                                                    throw (RuntimeException) new RuntimeException().initCause(ex);
	                                                }
	                                            }
	                                        }
	                                    }
	                                }
	                            }
	                        }
	                    }
	                }
	                
	                // Replace #{oldBeanName. ...} with #{newBeanName....} in VBEs in all jsp files of FacesModels (MarkupUnits) 
	                for (int i=0; i < models.length; i++) {
	                    Model iModel = models[i];
	                    if (iModel instanceof FacesModel) {
	                        FacesModel iFacesModel = (FacesModel) iModel;
	                        if (!iFacesModel.isBusted()) {
	                            MarkupUnit markupUnit = iFacesModel.getMarkupUnit();
	                            if (markupUnit == null)
	                                continue;
	                            Document markupDocument = markupUnit.getSourceDom();
	                            ElBindingScanner scanner = new ElBindingScanner();
	                            int referenceCount = scanner.getReferenceCount(markupDocument, oldBeanName);
	                            if (referenceCount > 0) {
	                                refactoringElements.add(getRefactoring(), new RenameElExpressionReferencesRefactoringElement(iFacesModel.getFile(), markupUnit, oldBeanName, newBeanName));
	                            }
	                        }
	                    }
	                }
	                
	                // The renaming of bean class name is handled by NetBeans Web project JSF refcatoring.
	                // Rename the managed bean name. 
	                WebModule webModule = WebModule.getWebModule(refactoringSourcefileObject);
	                if (webModule != null){
	                    List <FacesRefactoringUtils.OccurrenceItem> items =
	                        FacesRefactoringUtils.getAllBeanNameOccurrences(webModule, oldBeanName, newBeanName);
	                    for (FacesRefactoringUtils.OccurrenceItem item : items) {
	                        refactoringElements.add(getRefactoring(), new JSFConfigRenameBeanNameElement(item));
	                    }
	                }
	                
	                if (!isDelegatedRefactoring(getRefactoring())) {
	                    FileObject jspFileObject = FacesModel.getJspForJava(refactoringSourcefileObject);
	                    if (jspFileObject != null) {
	                        MoveRefactoring jspMoveRefactoring = new MoveRefactoring(Lookups.singleton(jspFileObject));
	                        FileObject targetDocumentRoot = JsfProjectUtils.getDocumentRoot(targetFileObjectProject);
	                        if (targetDocumentRoot == null) {
	                            // TODO
	                        }
	                        String targetRelativePathWithSlashes = targetRelativePathWithDollars.replace("$", "/");
	                        FileObject targetJspFolder = targetDocumentRoot.getFileObject(targetRelativePathWithSlashes);
	                        if (targetJspFolder == null) {
	                        	try {
									targetJspFolder = FileUtil.createFolder(targetDocumentRoot, targetRelativePathWithSlashes);
								} catch (IOException ioe) {
		                            // TODO return problem
		                            ErrorManager.getDefault().notify(ErrorManager.ERROR, ioe);
								}
	                        }
	                        
	                        URL url = URLMapper.findURL(targetJspFolder, URLMapper.EXTERNAL);
	                        try {
	                            jspMoveRefactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm())));
	                        } catch (MalformedURLException ex) {
	                            // TODO return problem
	                            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
	                        }
	                        
	                        // Flag delegation
	                        jspMoveRefactoring.getContext().add(FacesRefactoringsPluginFactory.DELEGATED_REFACTORING);
	  
	                        Problem problem = jspMoveRefactoring.prepare(refactoringElements.getSession());
	                        if (problem != null) {
	                            return problem;
	                        }
	                    }
	                }
	            }
	        }
        } else if (getRefactoring() instanceof RenameRefactoring) { // Package or folder rename
        	// compute the new target and make sure it exists
        	String newName = getRenameRefactoring().getNewName();
        	
        	String oldPackageName = null;
        	String newPackageName = null;
        	
        	// Is this a package rename?
        	NonRecursiveFolder nonRecursiveFolder = getRefactoring().getRefactoringSource().lookup(NonRecursiveFolder.class);
        	if (nonRecursiveFolder != null) { // Package rename
        		FileObject nonRecursiveFolderFileObject = nonRecursiveFolder.getFolder();
        		if (nonRecursiveFolderFileObject != null) {
        			oldPackageName = FacesRefactoringUtils.getPackageName(nonRecursiveFolderFileObject);
        			newPackageName = newName;
        		}
        	} else {
        		// Is this a folder rename
        		FileObject folder = getRefactoring().getRefactoringSource().lookup(FileObject.class);
        		if (folder != null) { // Folder rename
        			assert folder.isFolder();
        			oldPackageName = FacesRefactoringUtils.getPackageName(folder);
        			String oldParentPackageName = FacesRefactoringUtils.getPackageName(folder.getParent());
        			if (oldParentPackageName.length() == 0) {
        				newPackageName = newName;        				
        			} else {
        				newPackageName = oldParentPackageName + "." + newName;
        			}
        		}        		
        	}
        	if (oldPackageName != null || newPackageName != null) {
    	        for (FileObject refactoringSourcefileObject : filesToMove) {  	        	
                	if (FacesRefactoringUtils.isJavaFileObjectOfInterest(refactoringSourcefileObject)) {                   	                
        	        	Project project = FileOwnerQuery.getOwner(refactoringSourcefileObject);        	        	
        	        	assert project != null;
        	        	
        	        	FileObject pageBeanRoot = JsfProjectUtils.getPageBeanRoot(project);
                        if (pageBeanRoot == null) {
                            // TODO
                        }
                        String pageBeanRootPackageName = FacesRefactoringUtils.getPackageName(pageBeanRoot);
                        int pageBeanRootPackageNameLength = pageBeanRootPackageName.length();
                        String oldPageBeanRootRelativePackageName = (pageBeanRootPackageNameLength == 0) ? oldPackageName : oldPackageName.substring(pageBeanRootPackageNameLength + 1);
                        String oldPageBeanRootRelativePackageNameWithDollars = oldPageBeanRootRelativePackageName.replace('.', '$');
                        String newPageBeanRootRelativePackageName = (pageBeanRootPackageNameLength == 0) ? newPackageName : newPackageName.substring(pageBeanRootPackageNameLength + 1);
                        String newPageBeanRootRelativePackageNameWithDollars = newPageBeanRootRelativePackageName.replace('.', '$');
    	                
    	                String oldName = refactoringSourcefileObject.getName();

    	                String packageName = FacesRefactoringUtils.getPackageName(refactoringSourcefileObject.getParent());
    	                String relativePackageName = "";
    	                if (!packageName.equals(oldPackageName)) {
    	                	relativePackageName = packageName.substring(oldPackageName.length() + 1);
    	                }
    	                
    	                String relativePackageNameWithDollars = relativePackageName.replace('.', '$');
    	                
    	                String oldBeanName = oldPageBeanRootRelativePackageNameWithDollars 
    	                    + (oldPageBeanRootRelativePackageNameWithDollars.length() == 0 ? "" : "$") // NOI18N
    	                    + (relativePackageNameWithDollars.length() == 0 ? "" : relativePackageNameWithDollars + "$")
    	                    + oldName;
    	                String newBeanName = newPageBeanRootRelativePackageNameWithDollars
    	                    + (newPageBeanRootRelativePackageNameWithDollars.length() == 0 ? "" : "$") // NOI18N TODO compute target folder relative path
    	                    + (relativePackageNameWithDollars.length() == 0 ? "" : relativePackageNameWithDollars + "$")
    	                    + oldName;                
    	                // Fix up
    	                newBeanName = FacesUnit.fixPossiblyImplicitBeanName(newBeanName);
    	                
    	                String oldValueBindingPrefix = "#{" + oldBeanName + ".";
    	                String newValueBindingPrefix = "#{" + newBeanName + ".";

    	                FacesModelSet facesModelSet = FacesModelSet.getInstance(refactoringSourcefileObject);
    	                
    	                boolean isRefactoringSourcePageBean = false;
    	                String refactoringSourceFQN = null;
    	                Model refactoringSourceModel = facesModelSet.getModel(refactoringSourcefileObject);
    	                if (refactoringSourceModel instanceof FacesModel) {
    	                    FacesModel refactoringSourceFacesModel = (FacesModel) refactoringSourceModel;
    	                    isRefactoringSourcePageBean = refactoringSourceFacesModel.isPageBean();
    	                    if (!refactoringSourceFacesModel.isBusted()) {
    	                        JavaUnit javaUnit = refactoringSourceFacesModel.getJavaUnit();
    	                        if (javaUnit != null) {
    	                            refactoringSourceFQN = javaUnit.getJavaClass().getName();
    	                        }
    	                    }
    	                }
    	                
    	                Model[] models = facesModelSet.getModels();
    	                // Rename the bean name in the value binding expressions
    	                for (int i=0; i < models.length; i++) {
    	                    Model iModel = models[i];
    	                    if (iModel instanceof FacesModel) {
    	                        FacesModel iFacesModel = (FacesModel) iModel;
    	                        if (!iFacesModel.isBusted()) {
    	                            FileObject javaFileObject = iFacesModel.getJavaFile();
    	                            if (javaFileObject != null) {
    	                                try {
    	                                    // Replace #{oldBeanName. ...} with #{newBeanName....} in string literals in all java files of FacesModels (JavaUnits)
    	                                    JavaSource javaSource = JavaSource.forFileObject(javaFileObject);
    	                                    ModificationResult modificationResult = javaSource.runModificationTask(
    	                                            new StringLiteralTask(oldValueBindingPrefix, newValueBindingPrefix, StringLiteralTransformer.MatchKind.PREFIX)
    	                                            );
    	                                    refactoringElements.registerTransaction(new RetoucheCommit(Collections.singleton(modificationResult)));
    	                                    for (FileObject jfo : modificationResult.getModifiedFileObjects()) {
    	                                        for (Difference dif: modificationResult.getDifferences(jfo)) {
    	                                            String old = dif.getOldText();
    	                                            if (old!=null) {
    	                                                //TODO: workaround
    	                                                //generator issue?
    	                                                refactoringElements.add(getRefactoring(),DiffElement.create(dif, jfo, modificationResult));
    	                                            }
    	                                        }
    	                                    }
    	                                } catch (IOException ex) {
    	                                    throw (RuntimeException) new RuntimeException().initCause(ex);
    	                                }
    	                            }
    	                            
    	                            JavaUnit javaUnit = iFacesModel.getJavaUnit();
    	                            if (refactoringSourceFQN != null && javaUnit != null && !isRefactoringSourcePageBean) {
    	                                final String finalRefactoringSourceFQN = refactoringSourceFQN;
    	                                final JavaClass javaClass = javaUnit.getJavaClass();                                
    	                                if (javaClass != null) {
    	                                    Method method = javaClass.getMethod("get" + oldBeanName, new Class[0]);
    	                                    if (method != null) {
    	                                        final ElementHandle<ExecutableElement> elementHandle = method.getElementHandle();
    	                                        if (elementHandle != null) {
    	                                            FileObject facesJavaFileObject = iFacesModel.getJavaFile();
    	                                            JavaSource javaSource = JavaSource.forFileObject(facesJavaFileObject);
    	                                            if (javaSource != null) {
    	                                                // Rename accessor methods
    	                                                final RenameRefactoring[] methodRenameRefactoring = new RenameRefactoring[1];
    	                                                try {
    	                                                    javaSource.runUserActionTask(new CancellableTask<CompilationController>() {
    	                                                        public void cancel() {
    	                                                        }
    	
    	                                                        public void run(CompilationController compilationController)
    	                                                            throws Exception {
    	                                                            compilationController.toPhase(Phase.RESOLVED);
    	                                                            Element element = elementHandle.resolve(compilationController);
    	                                                            if (element != null && element.getKind() ==  ElementKind.METHOD) {
    	                                                                ExecutableElement executableElement = (ExecutableElement) element;
    	                                                                TypeMirror typeMirror = executableElement.getReturnType();
    	                                                                if (typeMirror != null && typeMirror.getKind() == TypeKind.DECLARED) {
    	                                                                    DeclaredType declaredType = (DeclaredType) typeMirror;
    	                                                                    TypeElement typeElement = (TypeElement) declaredType.asElement();
    	                                                                    
    	                                                                    if (typeElement.getQualifiedName().toString().equals(finalRefactoringSourceFQN)) {
    	                                                                        TreePathHandle treePathHandle = TreePathHandle.create(compilationController.getTrees().getPath(element), compilationController);
    	                                                                        methodRenameRefactoring[0] = new RenameRefactoring(Lookups.fixed(treePathHandle));
    	                                                                        methodRenameRefactoring[0].getContext().add(compilationController);
    	                                                                    }
    	                                                                }
    	                                                            }
    	                                                        }
    	                                                    }, true);                                                    
    	                                                    
    	                                                    if (methodRenameRefactoring[0] != null) {                                                        
    	                                                        methodRenameRefactoring[0].setNewName("get" + newBeanName);
    	                                                        
    	                                                        // Flag delegation
    	                                                        methodRenameRefactoring[0].getContext().add(FacesRefactoringsPluginFactory.DELEGATED_REFACTORING);
    	                                                        
    	                                                        Problem problem = methodRenameRefactoring[0].prepare(refactoringElements.getSession());
    	                                                        if (problem != null) {
    	                                                            return problem;
    	                                                        }
    	                                                    }
    	                                                } catch (IOException ex) {
    	                                                }
    	                                                
    	                                                try {
    	                                                    ModificationResult modificationResult = javaSource.runModificationTask(
    	                                                            new StringLiteralTask(oldBeanName, newBeanName, StringLiteralTransformer.MatchKind.EXACT)
    	                                                            );
    	                                                    refactoringElements.registerTransaction(new RetoucheCommit(Collections.singleton(modificationResult)));
    	                                                    for (FileObject jfo : modificationResult.getModifiedFileObjects()) {
    	                                                        for (Difference dif: modificationResult.getDifferences(jfo)) {
    	                                                            String old = dif.getOldText();
    	                                                            if (old!=null) {
    	                                                                //TODO: workaround
    	                                                                //generator issue?
    	                                                                refactoringElements.add(getRefactoring(),DiffElement.create(dif, jfo, modificationResult));
    	                                                            }
    	                                                        }
    	                                                    }
    	                                                } catch (IOException ex) {
    	                                                    throw (RuntimeException) new RuntimeException().initCause(ex);
    	                                                }
    	                                            }
    	                                        }
    	                                    }
    	                                }
    	                            }
    	                        }
    	                    }
    	                }
    	                
    	                // Replace #{oldBeanName. ...} with #{newBeanName....} in VBEs in all jsp files of FacesModels (MarkupUnits) 
    	                for (int i=0; i < models.length; i++) {
    	                    Model iModel = models[i];
    	                    if (iModel instanceof FacesModel) {
    	                        FacesModel iFacesModel = (FacesModel) iModel;
    	                        if (!iFacesModel.isBusted()) {
    	                            MarkupUnit markupUnit = iFacesModel.getMarkupUnit();
    	                            if (markupUnit == null)
    	                                continue;
    	                            Document markupDocument = markupUnit.getSourceDom();
    	                            ElBindingScanner scanner = new ElBindingScanner();
    	                            int referenceCount = scanner.getReferenceCount(markupDocument, oldBeanName);
    	                            if (referenceCount > 0) {
    	                                refactoringElements.add(getRefactoring(), new RenameElExpressionReferencesRefactoringElement(iFacesModel.getFile(), markupUnit, oldBeanName, newBeanName));
    	                            }
    	                        }
    	                    }
    	                }
    	                
    	                // The renaming of bean class name is handled by NetBeans Web project JSF refcatoring.
    	                // Rename the managed bean name. 
    	                WebModule webModule = WebModule.getWebModule(refactoringSourcefileObject);
    	                if (webModule != null){
    	                    List <FacesRefactoringUtils.OccurrenceItem> items =
    	                        FacesRefactoringUtils.getAllBeanNameOccurrences(webModule, oldBeanName, newBeanName);
    	                    for (FacesRefactoringUtils.OccurrenceItem item : items) {
    	                        refactoringElements.add(getRefactoring(), new JSFConfigRenameBeanNameElement(item));
    	                    }
    	                }
    	                
    	                if (!isDelegatedRefactoring(getRefactoring())) {
    	                    FileObject jspFileObject = FacesModel.getJspForJava(refactoringSourcefileObject);
    	                    if (jspFileObject != null) {
    	                        MoveRefactoring jspMoveRefactoring = new MoveRefactoring(Lookups.singleton(jspFileObject));
    	                        FileObject targetDocumentRoot = JsfProjectUtils.getDocumentRoot(project);
    	                        if (targetDocumentRoot == null) {
    	                            // TODO
    	                        }    	                            	                        
    	                        String targetRelativePathWithSlashes = "";
    	                        if (!newPackageName.equals(pageBeanRootPackageName)) {
    	                        	targetRelativePathWithSlashes = newPackageName.substring(pageBeanRootPackageNameLength + 1).replace('.', '/');
    	                        }
    	                        FileObject targetJspFolder = targetDocumentRoot.getFileObject(targetRelativePathWithSlashes);
    	                        if (targetJspFolder == null) {
    	                        	try {
    									targetJspFolder = FileUtil.createFolder(targetDocumentRoot, targetRelativePathWithSlashes);
    								} catch (IOException ioe) {
    		                            // TODO return problem
    		                            ErrorManager.getDefault().notify(ErrorManager.ERROR, ioe);
    								}
    	                        }
    	                        
    	                        URL url = URLMapper.findURL(targetJspFolder, URLMapper.EXTERNAL);
    	                        try {
    	                            jspMoveRefactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm())));
    	                        } catch (MalformedURLException ex) {
    	                            // TODO return problem
    	                            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
    	                        }
    	                        
    	                        // Flag delegation
    	                        jspMoveRefactoring.getContext().add(FacesRefactoringsPluginFactory.DELEGATED_REFACTORING);
    	  
    	                        Problem problem = jspMoveRefactoring.prepare(refactoringElements.getSession());
    	                        if (problem != null) {
    	                            return problem;
    	                        }
    	                    }
    	                }
    	            }
    	        }
        	}
        }

        return null;
    }
    
    private class StringLiteralTask implements CancellableTask<WorkingCopy> {
        private final String oldString;
        private final String newString;
        private final StringLiteralTransformer.MatchKind matchKind;
        
        public StringLiteralTask(String oldString, String newString, StringLiteralTransformer.MatchKind matchKind) {
            super();
            this.oldString = oldString;
            this.newString = newString;
            this.matchKind = matchKind;
        }
        
        public void cancel() {
        }
        
        public void run(WorkingCopy compiler) throws IOException {
            compiler.toPhase(JavaSource.Phase.RESOLVED);
            CompilationUnitTree cu = compiler.getCompilationUnit();
            if (cu == null) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, "compiler.getCompilationUnit() is null " + compiler);
                return;
            }
           
            StringLiteralTransformer stringLiteralTransformer = new StringLiteralTransformer(compiler, oldString, newString, matchKind);
            stringLiteralTransformer.scan(compiler.getCompilationUnit(), null);
            
            for (TreePath tree : stringLiteralTransformer.getUsages()) {
                ElementGripFactory.getDefault().put(compiler.getFileObject(), tree, compiler);
            }
            fireProgressListenerStep();
        }
    }
}
