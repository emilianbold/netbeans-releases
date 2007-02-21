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

package org.netbeans.modules.soa.mapper.basicmapper;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Vector;

import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperModel;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicViewModel;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.IMapperViewModel;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;

/**
 * <p>
 *
 * Title: </p> MapperViewModel <p>
 *
 * Description: </p> MapperViewModel provides basic implementation of
 * IMapperViewModel<p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 19, 2002
 * @version   1.0
 */
public class BasicViewModel
     implements IBasicViewModel {

    /**
     * the mapper model that contains this view model
     */
    private IBasicMapperModel mMapperModel;

    /**
     * storge of links object in this model
     */
    private List mNodes;

    /**
     * storge of PropertyChangeListener of this model
     */
    private List mPropertyListeners;

    /**
     * Construct a view model with no node initially.
     */
    public BasicViewModel() {
        mNodes = new Vector();
        mPropertyListeners = new Vector();
    }

    /**
     * Return the mapper model that contains this view model
     *
     * @return   a reference to mapper model that contains this view model.
     */
    public IBasicMapperModel getMapperModel() {
        return mMapperModel;
    }

    /**
     * Return the number of nodes in this model.
     *
     * @return   the number of nodes in this model.
     */
    public int getNodeCount() {
        return mNodes.size();
    }


    /**
     * Return all the nodes in this model in a Set repersentation.
     *
     * @return   all the nodes in this model.
     */
    public List getNodes() {
        return mNodes;
    }


    /**
     * Set the mapper model that contains this view model.
     *
     * @param model  the mapper model contains this view model.
     */
    public void setMapperModel(IBasicMapperModel model) {
        mMapperModel = model;
    }


    /**
     * Add a new node to this model. And fired node added property change event.
     *
     * @param node  the node to be added.
     */
    public void addNode(IMapperNode node) {
        // there can only be 1 tree node allow to exist
        // in the same view model.
        if (node instanceof IMapperTreeNode && mNodes.contains(node)) {
            return;
        }
        mNodes.add(node);
        firePropertyChange(
            IMapperViewModel.NODE_ADDED,
            node,
            null);
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
     * Return true if the specified node is in this model, false otherwise.
     *
     * @param node  the specified node to check
     * @return      true if the node is in this model, false otherwise.
     */
    public boolean containsNode(IMapperNode node) {
        return mNodes.contains(node);
    }


    /**
     * Remove a node from this model. And fired node removed property change
     * event.
     *
     * @param node  the node to be removed.
     */
    public void removeNode(IMapperNode node) {
        if (mNodes.remove(node)) {
            firePropertyChange(
                IMapperViewModel.NODE_REMOVED,
                null,
                node);
        }
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
     * Fire a specified property change event of this model.
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

    /**
     * Gets the paramString attribute of the MapperViewModel object
     *
     * @return   The paramString value
     */
    public String getParamString() {
        return super.toString() + "[node list=" + mNodes;
    }
}
