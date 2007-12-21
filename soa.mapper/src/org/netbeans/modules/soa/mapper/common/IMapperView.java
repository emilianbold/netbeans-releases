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
 * Title: Generic mapper view </p> <p>
 *
 * Description: Provides a basic functionalities of all generic mapper views. In
 * design, the view model is for displaying only. Subclasses should normally not
 * modify the view model. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IMapperView {

    /**
     * The property name of a change of this model.
     */
    public static final String MODEL_CHANGE = "MapperV.Model";

    /**
     * The property name of a change of this view name.
     */
    public static final String NAME_CHANGE = "MapperV.Name";
    
    /**
     * Set the view model of this view should display.
     *
     * @param model  the link mode to display
     */
    public void setViewModel(IMapperViewModel model);

    /**
     * Return the current mapper view model of this view.
     *
     * @return   the mapper view model of this view.
     */
    public IMapperViewModel getViewModel();

    /**
     * Return a name of this view.
     *
     * @return   a String repersentation of this view name.
     */
    public String getViewName();

    /**
     * Set a name of this view.
     *
     * @param name   a String repersentation of this view name.
     */
    public void setViewName (String name);

    /**
     * Set the auto layout object of this view.
     *
     * @param autoLayout  the auto layout of this view.
     */
    public void setAutoLayout(IMapperAutoLayout autoLayout);

    /**
     * Return the auto layout object of this view.
     *
     * @return   the auto layout object of this view.
     */
    public IMapperAutoLayout getAutoLayout();

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
     * Adds a IMapperListener to the listener list.
     *
     * @param listener  the IMapperListener to be added
     */
    public void addMapperListener (IMapperListener listener);

    /**
     * Removes a IMapperListener from the listener list.
     *
     * @param listener  the IMapperListener to be added
     */
    public void removeMapperListener (IMapperListener listener);
}
