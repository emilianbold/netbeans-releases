/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;

/**
 * Callback permitting {@link Project}s to inform the {@link ProjectManager}
 * of important lifecycle events.
 * Currently the only available events are modification of the project metadata
 * and project deletion notification.
 * However in the future other events may be added, such as moving
 * the project, which the project manager would need to be informed of.
 * <p>
 * This interface may only be implemented by the project manager. A
 * {@link ProjectFactory} will receive an instance in
 * {@link ProjectFactory#loadProject}.
 * </p>
 * @author Jesse Glick
 */
public interface ProjectState {
    
    /**
     * Inform the manager that the project's in-memory state has been modified
     * and that a call to {@link ProjectFactory#saveProject} may be needed.
     * May not be called during {@link ProjectFactory#loadProject}.
     * <p>Acquires write access.
     */
    void markModified();
    
    /**
     * <p>Inform the manager that the project has been deleted. The project will
     * be removed from any {@link ProjectManager}'s  mappings.
     * If {@link ProjectManager#findProject} is called on the project directory,
     * the {@link ProjectFactory ProjectFactories} are asked again to recognize
     * the project.</p>
     *
     * <p>The project is no longer recognized as created by the {@link ProjectManager}.</p>
     *
     * <p>Acquires write access.</p>
     *
     * @throws IllegalStateException if notifyDeleted is called more than once for a project.
     * @since 1.6
     */
    void notifyDeleted() throws IllegalStateException ;
    
}
