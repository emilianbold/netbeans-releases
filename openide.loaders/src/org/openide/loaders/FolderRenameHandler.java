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

package org.openide.loaders;

import org.openide.nodes.Node;

/**
 * This handler is used by DataFolder.FolderNode.setName() method. 
 * FolderNode.setName() uses  Lookup.getDefault() to lookup for instances of 
 * FolderRenameHandler. If there is one instance found, it's handleRename(...) 
 * method is called to handle rename request. More than one instance of 
 * FolderRenameHandler is not allowed.
 * 
 * @since 5.4
 * @author Jan Becicka
 */
public interface FolderRenameHandler {
    /**
     * @param folder on this folder rename was requested
     * @param newName new name of folder
     * @throws java.lang.IllegalArgumentException thrown if rename cannot be performed
     */
    void handleRename(DataFolder folder, String newName) throws IllegalArgumentException ;
}
