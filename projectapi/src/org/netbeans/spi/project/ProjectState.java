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
 * Callback permitting {@link Project}s to communicate with the {@link ProjectManager}.
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
    
}
