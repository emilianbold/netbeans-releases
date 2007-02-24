/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.uml.core.scm;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;

/**
 * Provides creation facilities for the various ISCMItem types.
 *
 * @author Trey Spiva
 */
public interface ISCMItemFactory
{
   /**
    * Creates a basic item.
    */
    public ISCMItem createItem(String fileName);

    /**
     * Creates a workspace item.
     */
    public ISCMWorkspaceItem createWorkspaceItem(IWorkspace workspace);

    /**
     * Creates a diagram item.
     */
    public ISCMDiagramItem createDiagramItem(IProxyDiagram diagram);

    /**
     * Creates an element item.
     */
    public ISCMElementItem createElementItem(IElement element);

    /**
     * Creates the appropriate ISCMItem type based on the contents of the
     * IProjectTreeItem.
     */
    public ISCMItem createItemFromTreeItem(IProjectTreeItem item);

    /**
     * Creates the appropriate ISCMItem based on what is passed in
     */
    public ISCMItem createSCMItem(Object opaqueType);
}