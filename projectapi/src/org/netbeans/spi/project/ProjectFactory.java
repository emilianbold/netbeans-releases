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

package org.netbeans.spi.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/**
 * Create in-memory projects from disk directories.
 * Instances should be registered into default lookup.
 * @author Jesse Glick
 */
public interface ProjectFactory {
    
    /**
     * Test whether a given directory probably refers to a project recognized by this factory
     * without actually trying to create it.
     * <p>Should be as fast as possible as it might be called sequentially on a
     * lot of directories.</p>
     * <p>Need not be definite; it is permitted to return null or throw an exception
     * from {@link #loadProject} even when returning <code>true</code> from this
     * method, in case the directory looked like a project directory but in fact
     * had something wrong with it.</p>
     * <p>Will be called inside read access.</p>
     * @param projectDirectory a directory which might refer to a project
     * @return true if this factory recognizes it
     */
    boolean isProject(FileObject projectDirectory);
    
    /**
     * Create a project that resides on disk.
     * If this factory does not
     * in fact recognize the directory, it should just return null.
     * <p>Will be called inside read access.
     * <p>Do not do your own caching! The project manager caches projects for you, properly.
     * <p>Do not attempt to recognize subdirectories of your project directory (just return null),
     * unless they are distinct nested projects.
     * @param projectDirectory some directory on disk
     * @param state a callback permitting the project to indicate when it is modified
     * @return a matching project implementation, or null if this factory does not recognize it
     */
    Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException;

    /**
     * Save a project to disk.
     * <p>Will be called inside write access.
     * @param project a project created with this factory's {@link #loadProject} method
     * @throws IOException if there is a problem saving
     * @throws ClassCastException if this factory did not create this project
     */
    void saveProject(Project project) throws IOException, ClassCastException;
    
}
