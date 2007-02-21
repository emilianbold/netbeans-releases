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

package org.netbeans.modules.soa.mapper.common.basicmapper;

import java.beans.PropertyChangeListener;
import java.util.Collection;

/**
 * <p>
 *
 * Title: IMapperModel </p> <p>
 *
 * Description: This manager contains all the view model available for the
 * mapper. It also provide the currect selected view model for mapper view to
 * display. Mapper can only display one view at a time.</p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IBasicMapperModel {

    /**
     * The property name of a new view model added to this model.
     */
    public static final String VIEWMODEL_ADDED = "MapperVM.Add";

    /**
     * The property name of a view model remove from this model.
     */
    public static final String VIEWMODEL_REMOVED = "MapperVM.Remove";

    /**
     * The property name of a change to the selected view model.
     */
    public static final String SELECTED_VIEWMODEL_CHANGED = "MapperVM.VMChange";

    /**
     * Add a view model to a mapper.
     *
     * @param viewModel  the view model to be added
     */
    public void addViewModel(IBasicViewModel viewModel);

    /**
     * Remove a view model from a mapper
     *
     * @param viewModel  the view model to be removed.
     */
    public void removeViewModel(IBasicViewModel viewModel);

    /**
     * Retrun the currect selected view model to display.
     *
     * @return   the display view model
     */
    public IBasicViewModel getSelectedViewModel();

    /**
     * Set the selected view model to display.
     *
     * @param viewModel  the view model to be display.
     */
    public void setSelectedViewModel(IBasicViewModel viewModel);

    /**
     * Return all available view models of this manager.
     *
     * @return   all the view model of this manager.
     */
    public Collection getAllViewModel();

    /**
     * Return the number of view models in this manager.
     *
     * @return   the number of view models in this manager.
     */
    public int getViewModelCount();

    /**
     * Remove all the view models from this mapper model.
     */
    public void removeAllViewModel();

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
}
