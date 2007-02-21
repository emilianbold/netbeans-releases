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

import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicController;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicViewController;
import org.netbeans.modules.soa.mapper.common.basicmapper.dnd.IBasicDragController;
import org.netbeans.modules.soa.mapper.common.basicmapper.dnd.IDnDHandler;
import org.netbeans.modules.soa.mapper.common.IMapperEvent;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperLinkFromLinkRequest;
import org.netbeans.modules.soa.mapper.common.IMapperLinkFromNodeRequest;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.IMapperView;
import org.netbeans.modules.soa.mapper.common.IMapperViewModel;
import org.netbeans.modules.soa.mapper.common.basicmapper.dnd.IDnDCustomizer;

/**
 * <p>
 *
 * Title: </p> MapperViewController<p>
 *
 * Description: </p> Provides basic implementation of IMapperViewController. The
 * default request methods will directly calls cooresponding IViewModel method.
 * <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 19, 2002
 * @version   1.0
 */
public class BasicViewController
     implements IBasicViewController {

    /**
     * the mapper main controller
     */
    private IBasicController mMapperController;
    
    /**
     * the mapper drag controller
     */
    private IBasicDragController mMapperDragController;

    /**
     * this view for this controller to handle.
     */
    private IMapperView mView;

    /**
     * the dnd handler of this controller.
     */
    private IDnDHandler mDnDHandler;

    /**
     * the dnd customizer of this controller.
     */
    private IDnDCustomizer mDnDCustomizer;
    
    
    /**
     * Creates a new BasicViewController object.
     */
    public BasicViewController() {
    }

    /**
     * Set the mapper controller.
     *
     * @return   the mapper controller.
     */
    public IBasicController getMapperController() {
        return mMapperController;
    }

    /**
     * Set the mapper controller.
     *
     * @param controller  the mapper controller
     */
    public void setMapperController(IBasicController controller) {
        mMapperController = controller;
    }

    /**
     * Set the mapper controller.
     *
     * @return   the mapper controller.
     */
    public IBasicDragController getMapperDragController() {
        return mMapperDragController;
    }

    /**
     * Set the mapper controller.
     *
     * @param controller  the mapper controller
     */
    public void setMapperDragController(IBasicDragController controller) {
        mMapperDragController = controller;
    }
    
    /**
     * Return the view of this controller to handle.
     *
     * @return   the view of this controller to handle.
     */
    public IMapperView getView() {
        return mView;
    }

    /**
     * Return the model of this controller to handle.
     *
     * @return   the model of this controller to handle.
     */
    public IMapperViewModel getViewModel() {
        return mView.getViewModel();
    }

    /**
     * Set the dnd handler for this view to hanlder dnd operations.
     *
     * @param handler the dnd handler for this mapper to hanlder dnd operations.
     */
    public void setDnDHandler (IDnDHandler handler) {
        mDnDHandler = handler;
    }

    /**
     * Returns the dnd handler for this view to hanlder dnd operations.
     *
     * @return the dnd handler for this mapper to hanlder dnd operations.
     */
    public IDnDHandler getDnDHandler() {
        return mDnDHandler;
    }

    /**
     * Set the dnd customizer for this view to customize dnd operations.
     *
     * @param handler  the dnd handler for this mapper to hanlder dnd
     *      operations.
     */
    public void setDnDCustomizer(IDnDCustomizer customizer) {
        mDnDCustomizer = customizer;
    }

    /**
     * Returns the dnd customizer for this view to customize dnd operations.
     *
     * @return   the dnd handler for this mapper to hanlder dnd operations.
     */
    public IDnDCustomizer getDnDCustomizer() {
        return mDnDCustomizer;
    }
    
    /**
     * Set the view of this controller to handle.
     *
     * @param view  the view of this controller to handle.
     */
    public void setView(IMapperView view) {
        mView = view;
    }

    /**
     * Set the model of this controller to handle.
     *
     * @param viewModel  the model of this controller to handle.
     */
    public void setViewModel(IMapperViewModel viewModel) {
        mView.setViewModel(viewModel);
    }

    /**
     * Requesting the specified link to be added to the model.
     *
     * @param link  the specified link to be added to the model.
     */
    public void requestNewLink(IMapperLink link) {
        mMapperController.getViewManager()
            .postMapperEvent(
            MapperUtilities.getMapperEvent(
            mView,
            link,
            IMapperEvent.REQ_NEW_LINK,
            mView.getViewName() + " requesting new link: " + link));
    }

    /**
     * Requesting the specified link to be added to the model.
     *
     * @param link  the specified link to be added to the model.
     */
    public void requestNewLink(IMapperLinkFromNodeRequest link) {
        mMapperController.getViewManager()
            .postMapperEvent(
            MapperUtilities.getMapperEvent(
            mView,
            link,
            IMapperEvent.REQ_NEW_LINK_FROM_NODE_AT_LOCATION,
            mView.getViewName() + " requesting new link: " + link));
    }
    
    /**
     * Requesting the specified link to be added to the model.
     *
     * @param link  the specified link to be added to the model.
     */
    public void requestNewLink(IMapperLinkFromLinkRequest link) {
        mMapperController.getViewManager()
            .postMapperEvent(
            MapperUtilities.getMapperEvent(
            mView,
            link,
            IMapperEvent.REQ_NEW_LINK_FROM_LINK_AT_LOCATION,
            mView.getViewName() + " requesting new link: " + link));
    }
    
    /**
     * Requesting the specified node to be added to a model.
     *
     * @param node  the specified node to be added to a model.
     */
    public void requestNewNode(IMapperNode node) {
        mMapperController.getViewManager()
            .postMapperEvent(
            MapperUtilities.getMapperEvent(
            mView,
            node,
            IMapperEvent.REQ_NEW_NODE,
            mView.getViewName() + " requesting new node: " + node));
    }

    /**
     * Requesting the specified link to be removed from a model.
     *
     * @param link  the specified link to be removed from a model.
     */
    public void requestRemoveLink(IMapperLink link) {
        mMapperController.getViewManager()
            .postMapperEvent(
            MapperUtilities.getMapperEvent(
            mView,
            link,
            IMapperEvent.REQ_DEL_LINK,
            mView.getViewName() + " requesting remove link: " + link));
    }

    /**
     * Requesting the specified node to be removed from a model.
     *
     * @param node  the specified node to be removed from a model.
     */
    public void requestRemoveNode(IMapperNode node) {
        mMapperController.getViewManager()
            .postMapperEvent(
            MapperUtilities.getMapperEvent(
            mView,
            node,
            IMapperEvent.REQ_DEL_NODE,
            mView.getViewName() + " requesting remove node: " + node));
    }
}
