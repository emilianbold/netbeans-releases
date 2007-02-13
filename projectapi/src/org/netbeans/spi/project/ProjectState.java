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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project;

/**
 * Callback permitting {@link org.netbeans.api.project.Project}s to inform the
 * {@link org.netbeans.api.project.ProjectManager}
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
     * be removed from any {@link org.netbeans.api.project.ProjectManager}'s  mappings.
     * If {@link org.netbeans.api.project.ProjectManager#findProject} is called on the project directory,
     * the {@link ProjectFactory ProjectFactories} are asked again to recognize
     * the project.</p>
     *
     * <p>The project is no longer recognized as created by the {@link org.netbeans.api.project.ProjectManager}.</p>
     *
     * <p>Acquires write access.</p>
     *
     * @throws IllegalStateException if notifyDeleted is called more than once for a project.
     * @since 1.6
     */
    void notifyDeleted() throws IllegalStateException;
    
}
