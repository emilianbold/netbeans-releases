/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.soa.mapper.common;

/**
 * <p>
 *
 * Title: IMapperGroupNode </p> <p>
 *
 * Description: By using this node group, model knows the connection of the
 * mutiple links. Since mutiple links do not connect by nodes ( 1 node only can
 * associated with 1 link), using this class to join links together, in
 * specified order. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IMapperGroupNode
     extends IMapperNode {

    /**
     * The property name of a new link added to this model.
     */
    public static final String NODE_INSERTED = "MapperGN.InsertNode";

    /**
     * The property name of a link remove from this model.
     */
    public static final String NODE_REMOVED = "MapperGN.RemoveNode";

    /**
     * Return the next node of the specified node.
     *
     * @param node  the start node to search for.
     * @return      the next node of the specified start node
     */
    public IMapperNode getNextNode(IMapperNode node);

    /**
     * Return the previous node of the specified node.
     *
     * @param prev  the previous node to search for.
     * @return      the previous node of the specified start node
     */
    public IMapperNode getPreviousNode(IMapperNode prev);

    /**
     * Return the last position node of this group node.
     *
     * @return   the last position node of this group node.
     */
    public IMapperNode getLastNode();

    /**
     * Return the first position node of this group node.
     *
     * @return   the first position node of this group node.
     */
    public IMapperNode getFirstNode();

    /**
     * Adds a node to be the first child of the IMapperGroupNode object
     *
     * @param node  the first child of the IMapperGroupNode object
     */
    public void addToFirst(IMapperNode node);

    /**
     * Adds a node to be the Last child of the IMapperGroupNode object
     *
     * @param node  be the Last child of the IMapperGroupNode object
     */
    public void addToLast(IMapperNode node);

    /**
     * Add a node to be the next one of the specified child node.
     *
     * @param childNode  the childNode to be match
     * @param newNode    the new node to be added next to child
     */
    public void addNextNode(IMapperNode childNode, IMapperNode newNode);

    /**
     * Add a node to be the previous one of the specified child node.
     *
     * @param childNode  the childNode to be match
     * @param newNode    the new node to be added previous to child
     */
    public void addPrevNode(IMapperNode childNode, IMapperNode newNode);

    /**
     * Return the start node that contains by the specifed link and also is one
     * of the node in this group node.
     *
     * @param link  the specified link
     * @return      the node in this group node that connected to the start
     *      point of the specified link.
     */
    public IMapperNode getLinkStartNode(IMapperLink link);

    /**
     * Return the end node that contains by the specifed link and also is one of
     * the node in this group node.
     *
     * @param link  the specified link
     * @return      the node in this group node that connected end point to the
     *      of the specified link.
     */
    public IMapperNode getLinkEndNode(IMapperLink link);

    /**
     * Find and return the mapper node that contains the specified node object.
     *
     * @param nodeObj  the specifed node object to be matched.
     * @return         the mapper node that contains the specified node object.
     */
    public IMapperNode findNodeByNodeObject(Object nodeObj);

    /**
     * Remove the sepecified from this group node.
     *
     * @param node  the node to be removed.
     */
    public void removeNode(IMapperNode node);

    /**
     * Return true if the specifed node is in this group node, false otherwise.
     *
     * @param node  the specifed node to find.
     * @return      true if the specifed node is in this group node, false
     *      otherwise.
     */
    public boolean containsNode(IMapperNode node);

    /**
     * Return the number of nodes in this group node.
     *
     * @return   the number of nodes in this group node.
     */
    public int getNodeCount();

}
