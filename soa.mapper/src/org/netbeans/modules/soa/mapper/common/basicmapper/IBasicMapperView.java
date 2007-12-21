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

import java.awt.Component;

import org.netbeans.modules.soa.mapper.common.IMapperView;

/**
 * <p>
 *
 * Title: IBasicMapperView </p> <p>
 *
 * Description: IBasicMapperView provides more functionailities to work under
 * the basic mapper. </p> <p>
 *
 * @author    Un Seng Leong
 * @created   December 26, 2002
 */

public interface IBasicMapperView
     extends IMapperView {

    /**
     * Return the view manager handles this view.
     *
     * @return   the view manager handles this view.
     */
    public IBasicViewManager getViewManager();

    /**
     * Return a java visual object repersents this view.
     *
     * @return   a java visual object repersents this view.
     */
    public Component getViewComponent();

    /**
     * Return true if this view is mapping enable, false otherwise.
     *
     * @return   The droppable value
     */
    public boolean isMapable();

    /**
     * Set if this view is mapping enable.
     *
     * @param droppable  the flag indicates if this view is mapping enable.
     */
    public void setIsMapable(boolean mapable);
}
