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

package org.netbeans.modules.soa.mapper.common;

import java.beans.PropertyChangeListener;
import java.util.List;


/**
 * <p>
 *
 * Title: IMapperViewModel </p> <p>
 *
 * Description: A view model manages nodes. MapperModel is able to switch to
 * display difference view model on run time. All the nodes are contained by
 * this model should be all the nodes of a mapper in a layer. The current design
 * will provide one view model for mutiple views. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IMapperViewModel {
    /**
     * The property name of a new link added to this model.
     */
    public static final String NODE_ADDED = "MapperVM.AddNode";

    /**
     * The property name of a link remove from this model.
     */
    public static final String NODE_REMOVED = "MapperVM.RemoveNode";

    /**
     * Add a Node to this view.
     *
     * @param node  The feature to be added to the Node attribute
     */
    public void addNode(IMapperNode node);

    /**
     * Remove a node from this view.
     *
     * @param node  the node to be removed
     */
    public void removeNode(IMapperNode node);

    /**
     * Return all the nodes of this view. The element in the List should be
     * IMapperNode.
     *
     * @return   all the nodes of this view.
     */
    public List getNodes();

    /**
     * Return the number of nodes of this view.
     *
     * @return   the number of nodes in this view.
     */
    public int getNodeCount();

    /**
     * Return true if the specified node is in this model, false otherwise.
     *
     * @param node  the specified node to check
     * @return      true if the node is in this model, false otherwise.
     */
    public boolean containsNode(IMapperNode node);

    /**
     * Adds a PropertyChangeListener to the listener list. The specified
     * property may be user-defined, and the following:
     * <ul>
     *   <li> a new link added to this model (<code>LINK_ADDED</code>) </li>
     *
     *   <li> a link removed from this model (<code>LINK_REMOVED</code>) </li>
     *
     * </ul>
     *
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
}

