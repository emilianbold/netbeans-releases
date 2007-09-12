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
