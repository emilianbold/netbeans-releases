/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 * Ability for a project to permit other modules to store arbitrary cache
 * data associated with the project.
 * @see org.netbeans.api.project.Project#getLookup
 * @author Jesse Glick
 */
public interface CacheDirectoryProvider {
    
    /**
     * Get a directory in which modules may store disposable cached information
     * about the project, such as an index of classes it contains.
     * This directory should be considered non-sharable by
     * {@link org.netbeans.api.queries.SharabilityQuery}.
     * Modules are responsible for preventing name clashes in this directory by
     * using sufficiently unique names for child files and folders.
     * @return a cache directory
     * @throws IOException if it cannot be created or loaded
     */
    FileObject getCacheDirectory() throws IOException;

}
