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

import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * <p>
 *
 * Title: </p> IMapperNode <p>
 *
 * Description: </p> Generic functionalities describes a basic mapper node.<p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 *
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IMapperNode {
    /**
     * The property name of a change of this x coordination of the node.
     */
    public static final String X_CHANGE = "MNode.X";

    /**
     * The property name of a change of this y coordination of the node.
     */
    public static final String Y_CHANGE = "MNode.Y";

    /**
     * The property name of a change of the group node contains this node.
     */
    public static final String GROUPNODE_CHANGED = "MNode.GNodeChange";

    /**
     * The property name of adding a link that connected to this node.
     */
    public static final String LINK_ADDED = "MNode.AddLink";

    /**
     * The property name of removing a link that perviously connected to this
     * node.
     */
    public static final String LINK_REMOVED = "MNode.RemoveLink";

    /**
     * Return the x coordination of this node in a 2 dimension base view.
     *
     * @return   an integer of x of this node.
     */
    public int getX();

    /**
     * Sets the x coordination of this node in a 2 dimension base view.
     *
     * @param x  an integer of x of this node.
     */
    public void setX(int x);

    /**
     * Return the y coordination of this node in a 2 dimension base view.
     *
     * @return   an integer of y of this node.
     */
    public int getY();

    /**
     * Sets the y coordination of this node in a 2 dimension base view.
     *
     * @param y  an integer of y of this node.
     */
    public void setY(int y);

    /**
     * Return a list of links that connects to this node.
     *
     * @return   a list of link that connects to this node.
     */
    public List getLinks();

    /**
     * Add a link that is connected to this node.
     *
     * @param link  the is connected to this node.
     */
    public void addLink(IMapperLink link);

    /**
     * Remove a link that is no longer connected to this node.
     *
     * @param link  the is no longer connected to this node.
     */
    public void removeLink(IMapperLink link);

    /**
     * Retrun the number of links connected to this node.
     *
     * @return   the number of links connected to this node.
     */
    public int getLinkCount();

    /**
     * Return true if the specified link is connected to this node, false
     * otherwise.
     *
     * @param link  the specified to check
     * @return      true if specified link is one of the link connected to this
     *      node.
     */
    public boolean containsLink(IMapperLink link);

    /**
     * Return a list of nodes that connects from this node.
     *
     * @return   a list of nodes that connects from this node. The element of
     *      the list is IMapperNode.
     */
    public List getNextNodes();

    /**
     * Return a list of nodes that connects to this node.
     *
     * @return   a list of nodes that connects from this node. The element of
     *      the list is IMapperNode.
     */
    public List getPreviousNodes();

    /**
     * Return the group node associate with this node.
     *
     * @return   the group node associate with this node.
     */
    public IMapperGroupNode getGroupNode();

    /**
     * Set the group node that contains this node.
     *
     * @param groupNode  the group node contains this node
     */
    public void setGroupNode(IMapperGroupNode groupNode);

    /**
     * Return the design object of this node in object repersentation.
     *
     * @return   an object repsersenting this node in another form of object.
     */
    public Object getNodeObject();

    /**
     * Set the object of this node in another object repersentation.
     *
     * @param nodeObject  an object repsersenting this node in another form.
     */
    public void setNodeObject(Object nodeObject);

    /**
     * Adds a PropertyChangeListener to the listener list.
     *
     * @param listener  the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param listener  the PropertyChangeListener to be added
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Clone another mapper node.
     *
     * @return   the cloned copy of this mapper node.
     */
    public Object clone();
}
