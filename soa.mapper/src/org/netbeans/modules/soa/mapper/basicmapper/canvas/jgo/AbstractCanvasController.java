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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo;

import java.util.List;
import java.awt.dnd.DropTargetDragEvent;
import org.netbeans.modules.soa.mapper.basicmapper.BasicViewController;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.IMapperCanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.dnd.IDnDHandler;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvas;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasController;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasNode;

/**
 * <p>
 *
 * Title: </p> AbstractCanvasController <p>
 *
 * Description: </p> AbstractCanvasController provides common implemenation of a
 * mapper canvas controller.<p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 31, 2002
 * @version   1.0
 */
public abstract class AbstractCanvasController
     extends BasicViewController
     implements ICanvasController {

    /**
     * Constructor for the AbstractCanvasController object.
     */
    public AbstractCanvasController() { }

    /**
     * Return the canvas of this controller handles.
     *
     * @return   the canvas of this controller handles.
     */
    public ICanvas getCanvas() {
        return ((IMapperCanvasView) getView()).getCanvas();
    }

    /**
     * Return the data model this controller handles. This method calls
     * getView().getViewModel()
     *
     * @return   the data model, IMapperViewModel, this controller handles.
     */
    public Object getDataModel() {
        return getView().getViewModel();
    }

    /**
     * Set the canvas of this controller handles. This method is not applicable,
     * it returns immediately.
     *
     * @param canvas  the canvas
     */
    public void setCanvas(ICanvas canvas) { }

    /**
     * Set the data model of this controller handles. This method is not
     * applicable, it returns immediately.
     *
     * @param model  the model
     */
    public void setDataModel(Object model) { }

    /**
     * Set the view manager for the canvas controller. This method is not
     * applicable, it returns immediately.
     *
     * @param viewManager  the view Manager
     */
    public void setViewManager(Object viewManager) { }

    /**
     * Return the view manager for the canvas controller. This method is not
     * applicable, it always returns null.
     *
     * @return   always null.
     */
    public Object getViewManager() {
        return null;
    }

    /**
     * Set the dnd handler for this view to hanlder dnd operations. Overrides
     * IBasicViewController.setDnDHandler(IDnDHanlder).
     *
     * @param handler  the dnd handler for this mapper to hanlder dnd
     *      operations.
     */
    public void setDnDHandler(IDnDHandler handler) {
        super.setDnDHandler(handler);
        if (getCanvas() instanceof AbstractCanvasView) {
            ((AbstractCanvasView) getCanvas()).setDnDHandler(handler);
        }
    }

    /**
     * Handles node name change. This method is not applicable, it returns
     * immediately.
     *
     * @param dataList  the list of data changes
     */
    public void handleNodeNameChange(List dataList) { }

    /**
     * Return true if the canvas updates with the specified action (id) and
     * specified data successfully, false otherwise. This method is not
     * applicable, always returns false.
     *
     * @param id        the id
     * @param dataList  the data that changed
     * @return          always false.
     */
    public boolean handleCanvasUpdates(int id, List dataList) {
        return false;
    }

    /**
     * Handles update links. Not use, always return false.
     *
     * @param fromNode - the from canvas node
     * @param toNode - the to canvas node
     * @param isComponentNode - signifies if component is a node
     * @param isWithBinding  - signifies if component is with a binding
     * @return false
     */
    public boolean updateLink(ICanvasNode fromNode, ICanvasNode toNode,
        ICanvasNode mSourceNode, boolean isWithBinding) {
        return false;
    }

    /**
     * Overrides the dragOver method to drag and drop the selected
     * icon to the canvas
     *
     * @param event - the drag source drop event
     * @return boolean
     */
    public boolean handleDragOver(DropTargetDragEvent event) {
        return false;
    }
}
