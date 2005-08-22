/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.spi.project.ui.support;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.modules.project.uiapi.DefaultProjectOperationsImplementation;

/**Support class to allow the project type implementors to perform {@link ProjectOperations}
 * by simply calling a method in this class. Each method in this class provides a default
 * confirmation dialog and default behavior.
 *
 * If the project type requires a different behavior of an operation, it is required to provide its
 * own implementation of the operation.
 *
 * @since 1.10
 * @author Jan Lahoda
 */
public final class DefaultProjectOperations {
    
    /**
     * Creates a new instance of DefaultProjectOperations
     */
    private DefaultProjectOperations() {
    }
    
    /**Perform default delete operation. Gathers all necessary data, shows a confirmation
     * dialog and deletes the project (if confirmed by the user).
     *
     * @since 1.10
     *
     * @param p project to delete
     * @throws IllegalArgumentException if
     * <code>p == null</code> or
     * if {@link org.netbeans.api.projects.ProjectOperations.isDeleteOperationSupported}
     * returns false for this project.
     */
    public static void performDefaultDeleteOperation(Project p) throws IllegalArgumentException {
        if (p == null) {
            throw new IllegalArgumentException("Project is null");
        }
        
        if (!ProjectOperations.isDeleteOperationSupported(p)) {
            throw new IllegalStateException("Attempt to delete project that does not support deletion.");
        }
        
        DefaultProjectOperationsImplementation.deleteProject(p);
    }
    
    /**Perform default copy operation. Gathers all necessary data, shows a confirmation
     * dialog and copies the project (if confirmed by the user).
     *
     * @since 1.10
     *
     * @param p project to copy
     * @throws IllegalArgumentException if
     * <code>p == null</code> or
     * {@link org.netbeans.api.projects.ProjectOperations.isCopyOperationSupported}
     * returns false for this project.
     */
    public static void performDefaultCopyOperation(Project p) throws IllegalArgumentException {
        if (p == null) {
            throw new IllegalArgumentException("Project is null");
        }
        
        if (!ProjectOperations.isCopyOperationSupported(p)) {
            throw new IllegalStateException("Attempt to delete project that does not support copy.");
        }
        
        DefaultProjectOperationsImplementation.copyProject(p);
    }
    
    /**Perform default move operation. Gathers all necessary data, shows a confirmation
     * dialog and moves the project (if confirmed by the user).
     *
     * @since 1.10
     *
     * @param p project to move
     * @throws IllegalArgumentException if
     * <code>p == null</code> or
     * {@link org.netbeans.api.projects.ProjectOperations.ismoveOperationSupported}
     * returns false for this project.
     */
    public static void performDefaultMoveOperation(Project p) throws IllegalArgumentException {
        if (p == null) {
            throw new IllegalArgumentException("Project is null");
        }
        
        if (!ProjectOperations.isMoveOperationSupported(p)) {
            throw new IllegalStateException("Attempt to delete project that does not support move.");
        }
        
        DefaultProjectOperationsImplementation.moveProject(p);
    }
    
    /**Perform default rename operation. Gathers all necessary data, shows a confirmation
     * dialog and renames the project (if confirmed by the user).
     *
     * @since 1.10
     *
     * @param p project to move
     * @param newName new project's name or null
     * @throws IllegalArgumentException if
     * <code>p == null</code> or
     * {@link org.netbeans.api.projects.ProjectOperations.ismoveOperationSupported}
     * returns false for this project.
     */
    public static void performDefaultRenameOperation(Project p, String newName) throws IllegalStateException {
        if (p == null) {
            throw new IllegalArgumentException("Project is null");
        }
        
        if (!ProjectOperations.isMoveOperationSupported(p)) {
            throw new IllegalStateException("Attempt to delete project that does not support move.");
        }
        
        DefaultProjectOperationsImplementation.renameProject(p, newName);
    }
    
}
