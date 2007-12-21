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

/**
 * <p>
 *
 * Title: Generic object repersentation of link in mapper. </p> <p>
 *
 * Description: Describe the basic functionality of a link in mapper. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IMapperLink {
    /**
     * The property name of a change of this source node of the link.
     */
    public static final String SOURCE_CHANGE = "Link.Start";

    /**
     * The property name of a change of this destination node of the link.
     */
    public static final String DESTINATION_CHANGE = "Link.End";

    /**
     * The start node of the link in an IMapperNode repersentation.
     *
     * @return   the node repersentes the start of this link.
     */
    public IMapperNode getStartNode();

    /**
     * Set the start node of this link.
     *
     * @param node  the start node of this link.
     */
    public void setStartNode(IMapperNode node);

    /**
     * The end node of the link in an IMapperNode repersentation.
     *
     * @return   the node repersentes the end of this link.
     */
    public IMapperNode getEndNode();


    /**
     * Sets the end node of this link.
     *
     * @param node  the end node of this link.
     */
    public void setEndNode(IMapperNode node);

    /**
     * Set the link user object.
     *
     * @param obj  The new user object of this link
     */
    public void setLinkObject(Object obj);

    /**
     * Gets the user object of this link
     *
     * @return   The user object of this link
     */
    public Object getLinkObject();


    /**
     * Adds a PropertyChangeListener to the listener list. The specified
     * property may be user-defined, and the following:
     * <ul>
     *   <li> this link's source node changed (<code>SOURCE_CHANGE</code> )
     *   </li>
     *   <li> this link's destination node changed ( <code> DESTINATION_CHANGE </code>
     *   ) </li>
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
