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

package org.netbeans.api.fileinfo;

import org.openide.filesystems.FileObject;

/**
 * Marker interface for representation of a folder without subfolders.
 * When an implementation of this interface is contained in the
 * lookup of a node, actions on that node should not process the subfolders
 * of this folder.
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
