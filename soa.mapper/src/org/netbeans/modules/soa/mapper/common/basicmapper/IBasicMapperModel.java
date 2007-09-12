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
