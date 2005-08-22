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

import java.util.List;
import org.openide.filesystems.FileObject;

/**
 * Base for various Project Operations, allows to gather metadata and data files
 * for a project.
 *
 * @author Jan Lahoda
 * @since 1.7
 */
public interface DataFilesProviderImplementation {
    
    /**
     * Returns list of {@link FileObject}s the are considered to be metadata files
     * and folders belonging into this project.
     * See {@link ProjectOperations#getMetadataFiles()} for more information.
     *
     * @return list of {@link FileObject}s that are considered metadata files and folders.
     */
    public List/*<FileObject>*/ getMetadataFiles();
    
    /**
     * Returns list of {@link FileObject}s the are considered to be data files and folders
     * belonging into this project.
     * See {@link ProjectOperations#getDataFiles()} for more information.
     *
     * @return list of {@link FileObject}s that are considered data files and folders.
     */
    public List/*<FileObject>*/ getDataFiles();
    
}
