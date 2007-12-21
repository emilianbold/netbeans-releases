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

/**
 * <p>
 *
 * Title: Mapper view controller </p> <p>
 *
 * Description: Generic interface describe a view controller. Evey view
 * controller should know about how to request a new link and node, and
 * how to request removing a link and group node. Hence, view controller should
 * not added new link and node to view model directly. In design, the four
 * request methods should generate a mapper event of that event type and post
 * the event to the IMapperViewManager. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IMapperViewController {

    /**
     * Set the view that this controller is handling.
     *
     * @param view  the view that this controller is handling.
     */
    public void setView(IMapperView view);


    /**
     * Return the view that this controller is handling.
     *
     * @return   the view that this controller is handling.
     */
    public IMapperView getView();


    /**
     * Set the model of the view.
     *
     * @param viewModel  the model of the view
     */
    public void setViewModel(IMapperViewModel viewModel);


    /**
     * Return the model of the view.
     *
     * @return   the model of the view
     */
    public IMapperViewModel getViewModel();


    /**
     * Requesting the specified link to be added to a model.
     *
     * @param link  the specified link to be added to a model.
     */
    public void requestNewLink(IMapperLink link);


    /**
     * Requesting the specified node to be added to a model.
     *
     * @param node  the specified node to be added to a model.
     */
    public void requestNewNode(IMapperNode node);


    /**
     * Requesting the specified link to be removed from a model.
     *
     * @param link  the specified link to be removed from a model.
     */
    public void requestRemoveLink(IMapperLink link);


    /**
     * Requesting the specified node to be removed from a model.
     *
     * @param node  the specified node to be removed from a model.
     */
    public void requestRemoveNode(IMapperNode node);
}
