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

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.Icon;

import com.nwoods.jgo.JGoBrush;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util.JGoLabel;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.canvas.gtk.ICanvasView;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvas;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasAction;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasComponentNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasControlNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasGroupNode;
import org.netbeans.modules.soa.mapper.common.gtk.ICanvasNodePopupBox;

/**
 * <p>
 *
 * Title: </p> AbstractCanvasFieldNode <p>
 *
 * Description: </p> AbstractCanvasFieldNode provides the basic implemenation of
 * a ICanvasFieldNode providing the basci view of a canvas field node. <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public abstract class AbstractCanvasFieldNode
     extends JGoLabel
     implements ICanvasFieldNode {

    /**
     * the canvas that contains this node
     */
    private ICanvas mCanvas;

    /**
     * the mapper field node that this field repersents.
     */
    protected IFieldNode mFieldNode;

    /**
     * the group node that contains this node
     */
    private ICanvasGroupNode mGroup;

    /**
     * the canvas that contains this node.
     */
    private ICanvasView mMapperCanvas;


    /**
     * Creates a new AbstractCanvasFieldNode object.
     *
     * @param node  the mapper field node of this canvas node.
     */
    public AbstractCanvasFieldNode(IFieldNode node) {
        super();
        mFieldNode = node;

        this.setSelectable(true);
        this.setDraggable(true);
        this.setBrush(JGoBrush.makeStockBrush(Color.WHITE));

        setName(mFieldNode.getName());
    
    }

    /**
     * Return the bounding rectangle of this node.
     *
     * @return the bounding rectangle of this node.
     */
    public Rectangle getBounding() {
        return getBoundingRect();
    }

    /**
     * Retrieves the action, this method is not applicable, always returns null.
     *
     * @return   always null
     */
    public ICanvasAction getAction() {
        return null;
    }

    /**
     * Retrieves the aux popup, this method is not applicable, always returns
     * null.
     *
     * @return   the aux popup box
     */
    public ICanvasNodePopupBox getAuxPopup() {
        return null;
    }

    /**
     * Retrieves the cavnas
     *
     * @return   the canvas or null.
     */
    public ICanvas getCanvas() {
        return mMapperCanvas;
    }

    /**
     * This method is not applicable, always returns null.
     *
     * @return   always null
     */
    public ICanvasComponentNode getComponentNode() {
        return null;
    }

    /**
     * Retrieves the container
     *
     * @return   contain group of this node
     */
    public ICanvasGroupNode getContainer() {
        return mGroup;
    }

    /**
     * Retrieves the control node associated with the canvas node. Return null
     * is nothing associated, this method is not applicable, always returns
     * null.
     *
     * @return   always null
     */
    public ICanvasControlNode getControlNode() {
        return null;
    }

    /**
     * Retrieves the data object, calls getFieldNode().
     *
     * @return   - underlying data object represented by this node
     */
    public Object getDataObject() {
        return getFieldNode();
    }

    public IFieldNode getFieldNode() {
        return mFieldNode;
    }

    /**
     * Gets the Icon which representing this node, , this method is not
     * applicable, always returns null.
     *
     * @return   always null.
     */
    public Icon getIcon() {
        return null;
    }

    /**
     * Retrun the mapper canvas that displays this node.
     *
     * @return   the canvas displays this node.
     */
    public ICanvasView getMapperCanvas() {
        return mMapperCanvas;
    }

    /**
     * Retrieves the name of the node
     *
     * @return   - name
     */
    public String getName() {
        return mFieldNode.getName();
    }

    /**
     * Retrieves the location
     *
     * @return   location
     */
    public Point getNodeLocation() {
        return this.getLocation();
    }

    /**
     * Retrieves the popup, this method is not applicable, always returns null.
     *
     * @return   always null
     */
    public ICanvasNodePopupBox getPopup() {
        return null;
    }

    /**
     * Retrieves the underlying ui component
     *
     * @return   this node
     */
    public Object getUIComponent() {
        return this;
    }

    /**
     * Sets the action
     *
     * @param action  the action to be set
     */
    public void setAction(ICanvasAction action) { }

    /**
     * Sets the aux popup this node is associated with, this method is not
     * applicable, always returns null.
     *
     * @param popup  the aux popup box
     */
    public void setAuxPopup(ICanvasNodePopupBox popup) { }

    /**
     * Sets the canvas this node belongs to
     *
     * @param canvas  the canvas that handles this cnode
     */
    public void setCanvas(ICanvas canvas) { }

    /**
     * Sets the component node associated with this canvas node
     *
     * @param node  the component node of this node
     */
    public void setComponentNode(ICanvasComponentNode node) { }

    /**
     * Sets the container this node belongs to
     *
     * @param group  the group that contains this node
     */
    public void setContainer(ICanvasGroupNode group) {
        mGroup = group;
    }

    /**
     * Sets the control node associated with this canvas node
     *
     * @param node  the control node of this node
     */
    public void setControlNode(ICanvasControlNode node) { }

    /**
     * sets the data object
     *
     * @param obj  - then object to be represented by this node
     */
    public void setDataObject(Object obj) { }

    /**
     * Sets the icon associated with this node
     *
     * @param icon  the icon
     */
    public void setIcon(Icon icon) { }

    /**
     * Set the mapper canvas that displays this node.
     *
     * @param canvas  the canvas displays this node.
     */
    public void setMapperCanvas(ICanvasView canvas) {
        mMapperCanvas = canvas;
    }

    /**
     * Sets the name of the name
     *
     * @param name  - new name
     */
    public void setName(String name) {
        updateLabel(name);
    }

    /**
     * Sets the location
     *
     * @param p  - location
     */
    public void setNodeLocation(Point p) {
        this.setLocation(p);
    }

    /**
     * Sets the abstract canvas node popup
     *
     * @param popup  the popup box of this node
     */
    public void setPopup(ICanvasNodePopupBox popup) { }

    /**
     * Refresh the node image.
     *
     * @param icon  the icon image of this node.
     */
    public void refreshNodeImage(javax.swing.Icon icon) { }

    /**
     * update the text of this node.
     *
     * @param text  update the text of this node.
     */
    public void updateLabel(String text) {
        setText(text);
    }

    /**
     * Return the toolTipText of this CanvasFieldNode, overrides JGoObject.getToolTipText.
     *
     * @return   The toolTipText of this field node.
     */
    public String getToolTipText() {
        return mFieldNode.getToolTipText();
    }
}

