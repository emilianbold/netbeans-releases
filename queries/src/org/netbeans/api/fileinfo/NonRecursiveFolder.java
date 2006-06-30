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

package org.netbeans.api.fileinfo;

import org.openide.filesystems.FileObject;

/**
 * Marker interface for representation of a folder without subfolders.
 * When an implementation of this interface is contained in the
 * lookup of a node, actions on that node should not process the subfolders
 * of this folder.
 * It is permitted for a Node to have both the {@link org.openide.loaders.DataFolder}
 * and NonRecursiveFolder in its lookup. In this case the {@link NonRecursiveFolder#getFolder} has to
 * return the same {@link FileObject} as the {@link org.openide.loaders.DataFolder#getPrimaryFile} method.
 * Any action which checks for the NonRecursiveFolder at all must prefer it to
 * the DataFolder since it is considered to be more specific information.
 *
 * @author  Martin Entlicher
 * @since 1.4
 */
public interface NonRecursiveFolder {
    
    /**
     * Get the folder file object, which represents the non-recursive folder.
     * Only direct children should be processed, no sub-folders.
     * @return The file object that represents non-recursive folder.
     */
    FileObject getFolder();
    
}
