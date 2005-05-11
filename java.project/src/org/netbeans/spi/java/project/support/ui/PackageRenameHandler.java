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

package org.netbeans.spi.java.project.support.ui;

import org.openide.nodes.Node;

/**
 * This handler is used by PackageViewChildren.PackageNode.setName() method. 
 * PackageNode.setName() uses  Lookup.getDefault() to lookup for instances of 
 * packageRenameHandler. If there is one instance found, it's handleRename(...) 
 * method is called to handle rename request. More than one instance of 
 * PackageRenameHandler is not allowed.
 * 
 * @since 1.5
 * @author Jan Becicka
 */
public interface PackageRenameHandler {
    /**
     * @param node on this node rename was requested
     * @param newName new name of node
     * @throws java.lang.IllegalArgumentException thrown if rename cannot be performed
     */
    void handleRename(Node node, String newName) throws IllegalArgumentException;
}
