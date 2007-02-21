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

import org.netbeans.modules.soa.mapper.common.IMapperEvent;
import org.netbeans.modules.soa.mapper.common.IMapperListener;

/**
 * <p>
 *
 * Title: Mapper Controller </p> <p>
 *
 * Description: Describe a mapper controller, which is a holder of
 * MapperListener. IMapper should give MapperListener control to this class.
 * </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IBasicController {
    /**
     * Return the view manager of the mapper.
     *
     * @return   the view manager of the mapper.
     */
    public IBasicViewManager getViewManager();

    /**
     * Return the mapper model of the mapper.
     *
     * @return   the mapper model of the mapper.
     */
    public IBasicMapperModel getMapperModel();

    /**
     * Return the destinated tree view controller.
     *
     * @return   the destinated tree view controller.
     */
    public IBasicViewController getDestViewController();

    /**
     * Return the source tree view controller.
     *
     * @return   the source tree view controller.
     */
    public IBasicViewController getSourceViewController();

    /**
     * Return the canvas view controller.
     *
     * @return   the canvas view controller.
     */
    public IBasicViewController getCanvasViewController();

    /**
     * Add a mapper listener to listening to mapper events. Available mapper
     * Event is defined in IMapperEvent.
     *
     * @param listener  the mapper listener to be added.
     */
    public void addMapperListener(IMapperListener listener);

    /**
     * Remove a mapper listener from this mapper. This method should delgate to
     * <code>IMapperController.removeMapperListener</code>.
     *
     * @param listener  the mapper listener to be removed.
     */
    public void removeMapperListener(IMapperListener listener);

    /**
     * Add a mapper listener to listening to mapper events of a specified event
     * type.
     *
     * @param listener   the mapper listener to be added
     * @param eventType  the specified event type to listen to
     */
    public void addMapperListener(IMapperListener listener, String eventType);

    /**
     * Remove a mapper listener that listen to a specified event type.
     *
     * @param listener   the mapper listener to be removed
     * @param eventType  the specified event type object to listen to
     */
    public void removeMapperListener(IMapperListener listener, String eventType);

    /**
     * Dispatch the specified mapper event to register listener.
     *
     * @param e  the mapper event to be dispatched.
     */
    public void dispatchEvent(IMapperEvent e);

    /**
     * Unregister the control for any mapper views, and release system resource.
     */
    public void releaseControl();
}
