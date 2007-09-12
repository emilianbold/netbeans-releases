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

import java.util.List;

import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.IMapperCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.palette.IPaletteView;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeView;
import org.netbeans.modules.soa.mapper.common.IMapperEvent;

/**
 * <p>
 *
 * Title: IMapperViewManager </p> <p>
 *
 * Description: Contains all mapper viewable object instance. It also contains a
 * reference to <code>IMapperModelManager</code>. This view manager should be a
 * singleton that a mapper can only contains a view manager. </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public interface IBasicViewManager {
    /**
     * Return the source view of this manager.
     *
     * @return   the source view of this manager.
     */
    public IMapperTreeView getSourceView();

    /**
     * Return the destination view of this manager.
     *
     * @return   the destination view of this manager.
     */
    public IMapperTreeView getDestView();

    /**
     * Return the transform canvas view of this mapper.
     *
     * @return   the transform canvas view of this mapper
     */
    public IMapperCanvasView getCanvasView();

    /**
     * Return the palette of this mapper.
     *
     * @return   the palette of this mapper.
     */
    public IPaletteView getPaletteView();

    /**
     * Return the mapper model of this mapper.
     *
     * @return   the mapper model manager of this mapper.
     */
    public IBasicMapperModel getMapperModel();

    /**
     * Post a mapper event to mapper event queue.
     *
     * @param e  the mapper event invoked.
     */
    public void postMapperEvent(IMapperEvent e);

    /**
     * Return the event queue of this mapper.
     *
     * @return   the event queue in a list object repersentation.
     */
    public List getEventQueue();
    
    /**
     * set flag to indicate whether to highlight a link.
     * @param highlight flag
     */
    public void setHighlightLink(boolean highlight);
    
    /**
     * check if link needs to highlighted.
     * @return true if link needs to be highlighed.
     */
    public boolean isHighlightLink();
    
    /**
     * flag to set if highlighting needs to be toggled.
     * internally set by control H key action.
     * @param toggle flag
     */
    public void setToggleHighlighting(boolean toggle);
    
    /**
     * check if highlighting is toggled
     * @return true then highlighting needs to be enabled.
     */
    public boolean isToggleHighlighting();
}
