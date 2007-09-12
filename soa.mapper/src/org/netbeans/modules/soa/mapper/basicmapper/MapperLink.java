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

package org.netbeans.modules.soa.mapper.basicmapper;

import java.beans.PropertyChangeListener;
import java.util.Vector;

import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;

/**
 * <p>
 *
 * Title: BasicLink </p> <p>
 *
 * Description: A basic link provides IMapperLink functionalites. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 19, 2002
 * @version   1.0
 */
public class MapperLink
     implements IMapperLink {

    /**
     * the end node of this link
     */
    private IMapperNode mEndNode;

    /**
     * the storge of PropertyChangeListeners
     */
    private Vector mPropertyListeners;

    /**
     * the start node of this link
     */
    private IMapperNode mStartNode;

    /**
     * the user link object.
     */
    private Object mLinkObj;


    /**
     * Construct an empty link with no start and end node. This constructor
     * calls another constructor as MapperLink (null, null).
     */
    public MapperLink() {
        this(null, null);
    }

    /**
     * Construct a new link with specified start and end node.
     *
     * @param startNode  the start node of this link
     * @param endNode    the end node of this link
     */
    public MapperLink(
        IMapperNode startNode,
        IMapperNode endNode) {
        mStartNode = startNode;
        mEndNode = endNode;
        mPropertyListeners = new Vector();
    }


    /**
     * Return the end node of this link.
     *
     * @return   the end node of this link
     */
    public IMapperNode getEndNode() {
        return mEndNode;
    }


    /**
     * Return the start node of the link.
     *
     * @return   the start node of the link.
     */
    public IMapperNode getStartNode() {
        return mStartNode;
    }

    /**
     * Set the end node of this link.
     *
     * @param end  the end node of this link.
     */
    public void setEndNode(IMapperNode end) {
        IMapperNode oldNode = mEndNode;
        mEndNode = end;
        firePropertyChange(SOURCE_CHANGE, end, oldNode);
    }

    /**
     * Set the start node of this link.
     *
     * @param start  the start node of this link.
     */
    public void setStartNode(IMapperNode start) {
        IMapperNode oldNode = mStartNode;
        mStartNode = start;
        firePropertyChange(DESTINATION_CHANGE, start, oldNode);
    }

    /**
     * Set the link user object.
     *
     * @param obj  The new user object of this link
     */
    public void setLinkObject(Object obj) {
        mLinkObj = obj;
    }

    /**
     * Gets the user object of this link
     *
     * @return   The user object of this link
     */
    public Object getLinkObject() {
        return mLinkObj;
    }

    /**
     * Adds a PropertyChangeListener to the listener list.
     *
     * @param listener  the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        mPropertyListeners.add(listener);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param listener  the PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        mPropertyListeners.remove(listener);
    }

    /**
     * Fire a specified property change event of this node.
     *
     * @param propertyName  the name of this property has changed
     * @param newValue      the new value of the property
     * @param oldValue      the old value of the property
     */
    protected void firePropertyChange(
        String propertyName,
        Object newValue,
        Object oldValue) {

        if (mPropertyListeners.size() > 0) {
            MapperUtilities.firePropertyChanged(
                (PropertyChangeListener[]) mPropertyListeners.toArray(
                new PropertyChangeListener[mPropertyListeners.size()]), this, propertyName, newValue, oldValue);
        }
    }

}
