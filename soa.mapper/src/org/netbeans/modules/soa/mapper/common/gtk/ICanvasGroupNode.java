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

package org.netbeans.modules.soa.mapper.common.gtk;

import java.util.Collection;
import java.util.List;

/**
 * @author    Charles Zhu
 * @created   December 3, 2002
 */

public interface ICanvasGroupNode
     extends ICanvasNode {

    /**
     * Retrieves all the nodes this gorup node contains
     *
     * @return Collection
     */
     Collection getNodes();

    /**
     * Removes the node from the group
     *
     * @param node  - the node bo be removed
     */
     void removeNode(ICanvasNode node);

    /**
     * Adds a node to the group
     *
     * @param node  - the node to be added
     */
     void addNode(ICanvasNode node);

    /**
     * Collapse the group node
     *
     * @return   - the the collapsed node representation
     */
     ICanvasNode collapse();

    /**
     * Expands the group node
     *
     * @return   - the expanded node representation
     */
     ICanvasNode expand();


    /**
     * Retireves a list of nodes which are addable to this parent node
     *
     * @return List
     */
     List getAddableNodes();

    /**
     * sets the addable nodes
     *
     * @param nodes - list of nodes
     */
     void setAddableNodes(List nodes);

    /**
     * Retreives whether the group node is expanded
     *
     * @return boolean
     */
     boolean isExpanded();

    /**
     * Sets the corresponding folder for the grouped components
     *
     * @param folder - the group folder
     */
     void setGroupFolder(Object folder);

    /**
     * Gets the corresponding folder of the grouped components
     *
     * @return   folder
     */
     Object getGroupFolder();
}
