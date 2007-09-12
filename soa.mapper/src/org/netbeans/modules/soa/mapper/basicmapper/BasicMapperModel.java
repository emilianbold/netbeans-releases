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
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.netbeans.modules.soa.mapper.basicmapper.util.MapperUtilities;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapperModel;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicViewModel;
import org.netbeans.modules.soa.mapper.common.IMapperViewModel;

/**
 * <p>
 *
 * Title: Basic Mapper Model </p> <p>
 *
 * Description: Provide basic Mapper model functionalities defined in
 * IMapperModel interface. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    unascribed
 * @created   January 2, 2003
 * @version   1.0
 */
public class BasicMapperModel
     implements IBasicMapperModel {

    /**
     * the storage of PropertyChangeListener
     */
    private List mPropertyListeners;

    /**
     * the current display view model.
     */
    private IBasicViewModel mSelectedViewModel;

    /**
     * a storage of view models
     */
    private List mViewModels;

    /**
     * Construct a mapper model with no view models in it.
     */
    public BasicMapperModel() {
        mViewModels = new Vector();
        mPropertyListeners = new Vector();
    }

    /**
     * Return all available view models of this manager.
     *
     * @return   all the view model of this manager.
     */
    public Collection getAllViewModel() {
        return mViewModels;
    }

    /**
     * Return the current selected view model of this mapper model.
     *
     * @return   the selected view model.
     */
    public IBasicViewModel getSelectedViewModel() {
        return mSelectedViewModel;
    }

    /**
     * Return the number of view models in this manager.
     *
     * @return   the number of view models in this manager.
     */
    public int getViewModelCount() {
        return mViewModels.size();
    }

    /**
     * Set the selected view model. If the view model is not in this mapper
     * model.
     *
     * @param viewModel  the view model to be displayed.
     */
    public void setSelectedViewModel(IBasicViewModel viewModel) {
        if (!mViewModels.contains(viewModel)) {
            addViewModel(viewModel);
        }

        IMapperViewModel oldModel = mSelectedViewModel;
        mSelectedViewModel = viewModel;

        firePropertyChange(IBasicMapperModel.SELECTED_VIEWMODEL_CHANGED, viewModel, oldModel);
    }

    /**
     * Adds a PropertyChangeListener to the listener list.
     *
     * @param listener  the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!mPropertyListeners.contains(listener)) {
            mPropertyListeners.add(listener);
        }
    }

    /**
     * Add a view model to this mapper model. And fire property event indicating
     * a new view model is added.
     *
     * @param viewModel  the view model to be added.
     */
    public void addViewModel(IBasicViewModel viewModel) {
        mViewModels.add(viewModel);
        viewModel.setMapperModel(this);
        firePropertyChange(
            IBasicMapperModel.VIEWMODEL_ADDED,
            viewModel,
            null);
    }

    /**
     * Remove all the view model from this mapper model. This method calls
     * removeViewModel() for each view model to firing the removeal event.
     */
    public void removeAllViewModel() {
        for ( int i=0; i < mViewModels.size(); i++ ) {
            removeViewModel((IBasicViewModel)mViewModels.get(i));
        }
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param listener  the PropertyChangeListener to be added
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        mPropertyListeners.remove(listener);
    }

    /**
     * Remove a view model from this mapper model. And fire property event
     * indicating a view model is removed.
     *
     * @param viewModel  Description of the Parameter
     */
    public void removeViewModel(IBasicViewModel viewModel) {
        if (mViewModels.remove(viewModel)) {
            viewModel.setMapperModel(null);
            firePropertyChange(
                IBasicMapperModel.VIEWMODEL_REMOVED,
                null,
                viewModel);
        }
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
        MapperUtilities.firePropertyChanged(
            (PropertyChangeListener[]) mPropertyListeners.toArray(
            new PropertyChangeListener[0]),
            this,
            propertyName,
            newValue,
            oldValue);
    }
}
