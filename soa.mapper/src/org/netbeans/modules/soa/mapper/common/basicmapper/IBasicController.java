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
