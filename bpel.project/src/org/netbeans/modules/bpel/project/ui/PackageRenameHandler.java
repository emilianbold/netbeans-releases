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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.project.ui;

import org.openide.nodes.Node;

/**
 * This handler is used by PackageViewChildren.PackageNode.setName() method.
 * PackageNode.setName() uses  Lookup.getDefault() to lookup for instances of
 * packageRenameHandler. If there is one instance found, it's handleRename(...)
 * method is called to handle rename request.
 * 
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
