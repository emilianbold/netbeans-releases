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

package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.Icon;

import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;

import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDrawable;
import com.nwoods.jgo.JGoEllipse;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoPort;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class PortArea extends JGoArea {

    private JGoRectangle rect;
    private GraphPort port;
    private JGoPen arrowPen;
    private JGoBrush arrowBrush;

    private JGoPen currentArrowPen;
    private JGoBrush currentArrowBrush;

    private boolean drawBoundingRect = false;

    /**
     * the out port for link
     */
    private JGoEllipse mShape;

    private Dimension portDrawableSize = new Dimension(7, 7);

    private boolean arrowPort = true;

    private boolean drawArrowPort = true;

    /** Creates a new instance of PortArea */
    public PortArea() {
        super();
        this.setSelectable(false);
        this.setResizable(false);

        if (arrowPort) {
            initializeArrowPort();
        } else {
            initializeWithoutArrowPort();
        }

    }

    private void initializeWithoutArrowPort() {
        port = new GraphPort();
        port.setStyle(JGoPort.StyleHidden);
        port.setValidDestination(true);
        port.setValidSource(true);
        this.addObjectAtTail(port);

        mShape = new JGoEllipse();
        mShape.setSize(new Dimension(5, 5));
        mShape.setDraggable(false);
        mShape.setSelectable(false);
        mShape.setResizable(false);
        mShape.setPen(JGoPen.makeStockPen(Color.lightGray));
        mShape.setBrush(JGoBrush.makeStockBrush(Color.lightGray));

        this.addObjectAtHead(mShape);
    }

    private void initializeArrowPort() {
        //add bounding rectangle
        rect = new JGoRectangle();
        rect.setSelectable(false);
        rect.setResizable(false);

        rect.setPen(JGoPen.makeStockPen(Color.lightGray));
        rect.setBrush(JGoBrush.makeStockBrush(new Color(254, 253, 235)));
        //rect.setSize(20, 16);
        this.addObjectAtHead(rect);

        //add port which will be hidden
        port = new GraphPort();
        port.setStyle(JGoPort.StyleHidden);
        //default port can be a source and destination of a link
        port.setValidSource(true);
        port.setValidDestination(true);
        this.addObjectAtTail(port);

        //set the default pen and brush for arrows
        this.setDefaultArrowPen(JGoPen.makeStockPen(new Color(73, 117, 183)));
        this.setDefaultArrowBrush(JGoBrush.makeStockBrush(Color.lightGray));

        this.setSize(20, 20);
    }

    /**
     * set whether to draw a bounding rectangle
     * 
     * @param draw draw
     */
    public void setDrawBoundingRect(boolean draw) {
        this.drawBoundingRect = draw;
    }

    /**
     * is there a bounding rectangle drawn around this area
     * 
     * @return whether a bounding rectangle is drawn
     */
    public boolean isDrawBoundingRect() {
        return drawBoundingRect;
    }

    /**
     * user should call this method if this port area needs to be a source only of a link
     * 
     * @param source boolean
     */
    public void setValidSource(boolean source) {
        port.setValidSource(source);
    }

    /**
     * user should call this method if this port area needs to be a destination only of a
     * link
     * 
     * @param destination boolean
     */
    public void setValidDestination(boolean destination) {
        port.setValidDestination(destination);
    }

    /**
     * set the from spot
     * 
     * @param s from spot constants
     */
    public void setFromSpot(int s) {
        port.setFromSpot(s);
    }

    /**
     * set the to spot
     * 
     * @param s to spot constants
     */
    public void setToSpot(int s) {
        port.setToSpot(s);
    }

    /**
     * get the graph port area
     * 
     * @return graph port area
     */
    public IGraphPort getGraphPort() {
        return port;
    }

    /**
     * set the icon in the area
     * 
     * @param icon icon
     */
    public void setIcon(Icon icon) {
    }

    public void setDefaultArrowPen(JGoPen pen) {
        this.arrowPen = pen;
        this.setCurrentArrowPen(pen);
    }

    public void setDefaultArrowBrush(JGoBrush brush) {
        this.arrowBrush = brush;
        this.setCurrentArrowBrush(brush);
    }

    public JGoPen getDefaultArrowPen() {
        return this.arrowPen;
    }

    public JGoBrush getDefaultArrowBrush() {
        return this.arrowBrush;
    }

    public void setCurrentArrowPen(JGoPen pen) {
        this.currentArrowPen = pen;
    }

    public void setCurrentArrowBrush(JGoBrush brush) {
        this.currentArrowBrush = brush;
    }

    public JGoPen getCurrentArrowPen() {
        return this.currentArrowPen;
    }

    public JGoBrush getCurrentArrowBrush() {
        return this.currentArrowBrush;
    }

    /**
     * paint this area and draw arrow for port
     * 
     * @param g graphics
     * @param view view
     */
    public void paint(java.awt.Graphics2D g, JGoView view) {
        super.paint(g, view);

        if (!drawArrowPort) {
            return;
        }

        int wGap = this.getWidth() - portDrawableSize.width;
        int hGap = this.getHeight() - portDrawableSize.height;

        //triangles right side point
        // ...>.
        int x1 = this.getLeft() + this.getWidth() - wGap / 2;
        int y1 = this.getTop() + this.getHeight() / 2;

        //triangles' top left point
        int x2 = this.getLeft() + wGap / 2;
        int y2 = this.getTop() + hGap / 2;

        //triangles' bottom left point
        int x3 = x2;
        int y3 = this.getTop() + this.getWidth() - hGap / 2;

        int[] xPoints = { x1, x2, x3};
        int[] yPoints = { y1, y2, y3};

        JGoDrawable.drawPolygon(g, getCurrentArrowPen(), getCurrentArrowBrush(), xPoints, yPoints, 3);

    }

    /**
     * override this method to handle the changes in the geometry of this area we will lay
     * out all the children again again
     * 
     * @param prevRect previous rectangle bounds
     */
    protected void geometryChange(Rectangle prevRect) {
        // handle any size changes by repositioning all the items
        if (prevRect.width != getWidth() || prevRect.height != getHeight()) {
            layoutChildren();
        } else {
            super.geometryChange(prevRect);
        }
    }

    /**
     * layout the children of this cell area
     */
    protected void layoutChildren() {
        if (arrowPort) {
            layoutChildrenForArrowPort();
        } else {
            layoutChildrenWithoutArrowPort();
        }

    }

    private void layoutChildrenForArrowPort() {
        Rectangle rectangle = this.getBoundingRect();
        if (isDrawBoundingRect()) {
            rect.setBoundingRect(rectangle);
        }
        port.setBoundingRect(rectangle);
    }

    private void layoutChildrenWithoutArrowPort() {
        Rectangle rectangle = this.getBoundingRect();
        port.setBoundingRect(rectangle);
        mShape.setSpotLocation(JGoObject.Center, this, JGoObject.Center);
    }

    public boolean doMouseEntered(int modifiers, Point dc, Point vc, JGoView view) {
        this.setCurrentArrowPen(JGoPen.makeStockPen(Color.orange));
        this.update();
        return true;
    }

    public boolean doMouseExited(int modifiers, Point dc, Point vc, JGoView view) {
        this.setCurrentArrowPen(this.getDefaultArrowPen());
        this.update();
        return true;
    }

    public void setDrawPortArrow(boolean draw) {
        this.drawArrowPort = draw;
    }

    public void setBackgroundColor(Color c) {
        if (rect != null) {
            rect.setBrush(JGoBrush.makeStockBrush(c));
        }
    }
}

