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

import java.util.Arrays;

import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

import com.sun.source.tree.Tree;

/**
 * <p>This is a factory for refactoring plugins that participate in Insync supported refactoring.</p>
 * <p>This factory responds to the following objects in the refactoring source lookup.
 * <ul>
 * <li>FileObject
 * <ul>
 * <li>VW Jsp file
 * <li>LifeCycle managed bean Java file
 * </ul>
 * <li>DataFolder - folder under web/
 * </ul>
 * </p> 
 */
public class FacesRefactoringsPluginFactory implements RefactoringPluginFactory {
    
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
//    	System.out.println("<-----");
//    	System.out.println("Refactoring: " + refactoring.getClass().getName());
//    	System.out.println("Context Lookup: ");
//    	for (Object o:refactoring.getContext().lookupAll(Object.class)) {
//    		if (o.getClass().isArray()) {    			
//    			System.out.println(Arrays.asList((Object[])o));
//    		} else {
//    			System.out.println(o);
//    		}
//    	}
//    	System.out.println("RefactoringSource Lookup: ");
//    	for (Object o:refactoring.getRefactoringSource().lookupAll(Object.class)) {
//    		if (o.getClass().isArray()) {    			
//    			System.out.println(Arrays.asList((Object[])o));
//    		} else {
//    			System.out.println(o);
//    		}
//    	}
//    	System.out.println("------>");
    	
        Lookup refactoringSource = refactoring.getRefactoringSource();
        // Is this refactoring for a FileObject
        FileObject refactoredFileObject = refactoringSource.lookup(FileObject.class);
        if (refactoredFileObject != null) {
            if (FacesRefactoringUtils.isVisualWebJspFile(refactoredFileObject)) {
                FacesModelSet.getInstance(refactoredFileObject);
                if (refactoring instanceof RenameRefactoring) {
                	// Ensure the modelling has happened
                	FacesModelSet.getInstance(refactoredFileObject);
                    return new FacesJspFileRenameRefactoringPlugin((RenameRefactoring)refactoring);
                } else if (refactoring instanceof MoveRefactoring) { 
                	// Ensure the modelling has happened
                	FacesModelSet.getInstance(refactoredFileObject);
                    return new FacesJspFileMoveRefactoringPlugin((MoveRefactoring)refactoring);
                }
            } else if (refactoredFileObject.isFolder() && 
            		FacesRefactoringUtils.isFileInJsfProject(refactoredFileObject)) { // folder in JsfProject 
            	if (FacesRefactoringUtils.isOnSourceClasspath(refactoredFileObject)) {            	
	            	if (FacesRefactoringUtils.isFolderParentOfPageBeanRoot(refactoredFileObject) ||
	            			FacesRefactoringUtils.isFolderPageBeanRoot(refactoredFileObject) ||             
		            		FacesRefactoringUtils.isFolderUnderPageBeanRoot(refactoredFileObject)) {
		            	if (refactoring instanceof RenameRefactoring) {
		                	// Ensure the modelling has happened
		                	FacesModelSet.getInstance(refactoredFileObject);
		                    return new FacesJavaFileMoveRefactoringPlugin((RenameRefactoring)refactoring);
		                } else if (refactoring instanceof MoveRefactoring) {
		                	// Ensure the modelling has happened
		                	FacesModelSet.getInstance(refactoredFileObject);
		                    return new FacesJavaFileMoveRefactoringPlugin((MoveRefactoring)refactoring);
		                }
	            	}
            	}
            	// else check for folder in web folder
            } else if (FacesRefactoringUtils.isFileInJsfProject(refactoredFileObject)) {
            	if (FacesRefactoringUtils.isOnSourceClasspath(refactoredFileObject)) {
            		// Ensure the modelling has happened
            		FacesModelSet.getInstance(refactoredFileObject);
	            	if (FacesRefactoringUtils.isJavaFileObjectOfInterest(refactoredFileObject)) {
		                TreePathHandle treePathHandle = refactoringSource.lookup(TreePathHandle.class);
		                if (treePathHandle == null || treePathHandle.getKind() == Tree.Kind.CLASS) {
		                    if (refactoring instanceof RenameRefactoring) {
		                        return new FacesJavaFileRenameRefactoringPlugin((RenameRefactoring)refactoring);
		                    } else if (refactoring instanceof MoveRefactoring) {
		                        return new FacesJavaFileMoveRefactoringPlugin((MoveRefactoring)refactoring);
		                    }
		                }
		            }
            	}
            }
        } else {
            // Is this refactoring for a package
            NonRecursiveFolder refactoredNonRecursiveFolder = refactoringSource.lookup(NonRecursiveFolder.class);
            if (refactoredNonRecursiveFolder != null) {
                FileObject fileObject = refactoredNonRecursiveFolder.getFolder();
                if (FacesRefactoringUtils.isFolderPageBeanRoot(fileObject) || 
	            		FacesRefactoringUtils.isFolderUnderPageBeanRoot(fileObject)) {
	            	if (refactoring instanceof RenameRefactoring) {
	                	// Ensure the modelling has happened
	                	FacesModelSet.getInstance(fileObject);
	                    return new FacesJavaFileMoveRefactoringPlugin((RenameRefactoring)refactoring);
	                }
	            	// No need to handle package move - only rename is allowed
                }
            }
        }
        return null;
    }
    
    // An instance of this class is put in the delegate AbstractRefactoring's context
    // to indicate delegation.
    static class DelegatedRefactoring {}   
    static final DelegatedRefactoring DELEGATED_REFACTORING = new DelegatedRefactoring();
}
