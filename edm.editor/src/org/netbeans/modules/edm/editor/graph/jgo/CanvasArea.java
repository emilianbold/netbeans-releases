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
package org.netbeans.modules.edm.editor.graph.jgo;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;


import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoLink;
import com.nwoods.jgo.JGoListPosition;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class CanvasArea extends JGoArea implements ICanvasInterface {

    private String toolTip;

    private ArrayList listeners = new ArrayList();

    /**
     * The insets around this area
     */
    protected Insets insets = new Insets(0, 0, 0, 0);

    /** Creates a new instance of CanvasArea */
    public CanvasArea() {
        super();
        //by default any canvas area is not selectable
        //not resizeable and also does not grab selection of non selectable
        //children
        this.setSelectable(false);
        this.setResizable(false);
    }

    /**
     * set the tool tip
     * 
     * @param toolTip
     */
    public void setToolTipText(String tTip) {
        this.toolTip = tTip;
    }

    /**
     * get the tooltip text
     * 
     * @return tooltip text
     */
    @Override
    public String getToolTipText() {
        return toolTip;
    }

    /**
     * set the insets of this area
     * 
     * @param insets -
     */
    public void setInsets(Insets insets) {
        this.insets = insets;
    }

    /**
     * get the insets
     * 
     * @return insets
     */
    public Insets getInsets() {
        return this.insets;
    }

    /**
     * remove all the children from this area
     */
    public void removeAll() {
        JGoListPosition pos = this.getFirstObjectPos();
        while (pos != null) {
            JGoObject obj = this.getObjectAtPos(pos);
            if (obj instanceof CanvasArea) {
                ((CanvasArea) obj).removeAll();
            }
            this.removeObject(obj);
            pos = this.getNextObjectPos(pos);
        }

    }

    /**
     * handle geometry change
     * 
     * @param prevRect previous bounds rectangle
     */
    @Override
    protected void geometryChange(Rectangle prevRect) {
        // handle any size changes by repositioning all the items
        if (prevRect.width != getWidth() || prevRect.height != getHeight()) {
            layoutChildren();
        } else {
            super.geometryChange(prevRect);
        }
    }

    /**
     * get maximum height
     * 
     * @return max height
     */
    public int getMaximumHeight() {
        return this.getHeight();
    }

    /**
     * get the maximum width
     * 
     * @return max width
     */
    public int getMaximumWidth() {
        return this.getWidth();
    }

    /**
     * get the minimum height
     * 
     * @return min height
     */
    public int getMinimumHeight() {
        return this.getHeight();
    }

    /**
     * get the minimum width
     * 
     * @return min width
     */
    public int getMinimumWidth() {
        return this.getWidth();
    }

    /**
     * layout the children
     */
    public void layoutChildren() {
    }

    /**
     * add a property change listener
     * 
     * @param l listener
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        listeners.add(l);
    }

    /**
     * remove a property change listener
     * 
     * @param l listener
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.remove(l);
    }

    protected synchronized void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {

        PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldValue, newValue);

        for (int i = 0; i < listeners.size(); i++) {
            PropertyChangeListener l = (PropertyChangeListener) listeners.get(i);
            l.propertyChange(evt);
        }
    }

    /**
     * add link of a port in the list
     * 
     * @param gPort port
     * @param list list
     */
    protected void addLinks(IGraphPort gPort, List<JGoLink> list) {
        if (gPort == null) {
            return;
        }

        JGoPort port = (JGoPort) gPort;
        JGoListPosition pos = port.getFirstLinkPos();
        while (pos != null) {
            JGoLink link = port.getLinkAtPos(pos);
            list.add(link);
            pos = port.getNextLinkPos(pos);
        }
    }

    /**
     * Overrides default implementation of handleResize to enforce minimum dimensions when
     * user attempts to resize an object.
     * 
     * @param g the graphics context to draw on
     * @param view the view we're being resizing in
     * @param origRect the object's original bounding rectangle
     * @param newPoint the location of the new point
     * @param whichHandle the handle number of the point being moved
     * @param event one of: JGoView.EventMouseUp, JGoView.EventMouseMove,
     *        JGoView.EventMouseDown
     * @param minWidth the minimum width of the object (defaults to zero)
     * @param minHeight the minimum height of the object (defaults to zero)
     * @return Rectangle representing location and dimension of resize outline; null if
     *         outline is to be handled by application
     */
    protected Rectangle handleResize(Graphics2D g, JGoView view, Rectangle origRect, Point newPoint, int whichHandle, int event, int minWidth,
            int minHeight) {
        return super.handleResize(g, view, origRect, newPoint, whichHandle, event, getMinimumWidth(), getMaximumHeight());
    }
}

