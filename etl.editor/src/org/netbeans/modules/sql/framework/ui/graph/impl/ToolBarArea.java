/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ToolBarArea extends CanvasArea {

    private JGoRectangle rect;
    private ArrayList items;

    private int hGap = 2;

    /** Creates a new instance of TableToolBarArea */
    public ToolBarArea() {
        items = new ArrayList();

        rect = new JGoRectangle();
        rect = new JGoRectangle();
        rect.setSelectable(false);
        rect.setPen(JGoPen.lightGray);
        rect.setBrush(JGoBrush.makeStockBrush(new Color(254, 253, 235)));
        addObjectAtHead(rect);

        this.insets = new Insets(4, 2, 2, 4);

        this.setResizable(false);
        this.setSelectable(false);
    }

    /**
     * add a toolbar item
     * 
     * @param item toolbar item
     */
    public void addToolBarItem(ToolBarItemArea item) {
        this.addObjectAtTail(item);
        items.add(item);
    }

    /**
     * remove a tool bar item
     * 
     * @param item toolbar item
     */
    public void removeToolBarItem(ToolBarItemArea item) {
        this.removeObject(item);
        items.remove(item);
    }

    /**
     * initialize the tool bar area
     * 
     * @param loc the location of toolbar
     */
    public void initialize(Point loc) {

        this.setBoundingRect(loc.x, loc.y, getMaximumWidth(), getMaximumHeight());

    }

    /**
     * get the maximum width
     * 
     * @return maximum width
     */
    public int getMaximumWidth() {
        int maxWidth = insets.left + insets.right;

        Iterator it = items.iterator();
        while (it.hasNext()) {
            ToolBarItemArea item = (ToolBarItemArea) it.next();
            maxWidth += item.getWidth();
        }

        return maxWidth;
    }

    /**
     * get the maximum height
     * 
     * @return maximum height
     */
    public int getMaximumHeight() {
        int maxHeight = insets.top + insets.bottom;

        Iterator it = items.iterator();
        while (it.hasNext()) {
            ToolBarItemArea item = (ToolBarItemArea) it.next();
            maxHeight += item.getHeight();
            break;
        }

        return maxHeight;
    }

    /**
     * override this method to handle the changes in the geometry of this area we will lay
     * out all toolbar items
     * 
     * @param prevRect previous bounds rectangle
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
     * layout all the children of this toolbar area
     */
    public void layoutChildren() {

        rect.setBoundingRect(this.getBoundingRect());
        Insets insets1 = getInsets();

        //get the bounding rectangle of this table area
        int x = getLeft() + insets1.left;
        int y = getTop() + insets1.top;
        int nextLeft = x;

        Iterator it = items.iterator();
        while (it.hasNext()) {
            ToolBarItemArea item = (ToolBarItemArea) it.next();
            item.setBoundingRect(nextLeft, y, item.getWidth(), item.getHeight());

            nextLeft += item.getWidth() + hGap;
        }
    }

}

