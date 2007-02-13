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
     * Returns list of {@link FileObject}s that are considered to be metadata files
     * and folders belonging into this project.
     * See {@link org.netbeans.spi.project.support.ProjectOperations#getMetadataFiles(Project)} for more information.
     *
     * @return list of metadata files and folders
     */
    List<FileObject> getMetadataFiles();
    
    /**
     * Returns list of {@link FileObject}s that are considered to be data files and folders
     * belonging into this project.
     * See {@link org.netbeans.spi.project.support.ProjectOperations#getDataFiles(Project)} for more information.
     *
     * @return list of data files and folders
     */
    List<FileObject> getDataFiles();
    
}
