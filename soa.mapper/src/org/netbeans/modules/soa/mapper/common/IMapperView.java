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
