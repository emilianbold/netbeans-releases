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
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoImage;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ToolBarItemArea extends CanvasArea {

    private JGoImage itemImg;

    private JGoRectangle rect;

    private Action action;

    private String actionCommand;

    /** Creates a new instance of ToolBarItemArea */
    public ToolBarItemArea() {

        this.setPickableBackground(false);
        rect = new JGoRectangle();
        rect = new JGoRectangle();
        rect.setSelectable(false);
        rect.setResizable(false);

        rect.setPen(JGoPen.lightGray);
        rect.setBrush(JGoBrush.makeStockBrush(Color.white));
        addObjectAtHead(rect);

        this.insets = new Insets(1, 1, 1, 1);
    }

    /**
     * initialize the toolbar item
     * 
     * @param icon icon
     */
    public void initialize(Icon icon) {
        //add title image
        itemImg = new JGoImage();
        ImageIcon imgIcon = (ImageIcon) icon;
        itemImg.loadImage(imgIcon.getImage(), false);

        itemImg.setSize(imgIcon.getImage().getWidth(null), imgIcon.getImage().getHeight(null));
        itemImg.setSelectable(false);
        itemImg.setResizable(false);
        addObjectAtTail(itemImg);

    }

    /**
     * create a new instance of toolbar item
     * 
     * @param action action
     */
    public ToolBarItemArea(Action action) {
        this();
        this.action = action;
        ItemPropertyChangeListener listener = new ItemPropertyChangeListener();
        action.addPropertyChangeListener(listener);

        Icon icon = (Icon) action.getValue(Action.SMALL_ICON);
        //String toolTip = (String) action.getValue(Action.SHORT_DESCRIPTION);
        actionCommand = (String) action.getValue(Action.ACTION_COMMAND_KEY);
        initialize(icon);
    }

    /**
     * handle mouse click
     * 
     * @param modifiers mouse event modifiers
     * @param dc document point
     * @param vc view point
     * @param view view
     * @return bool
     */
    public boolean doMouseClick(int modifiers, Point dc, Point vc, JGoView view) {

        ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand);

        action.actionPerformed(actionEvent);
        return false;
    }

    /**
     * override this method to handle the changes in the geometry of this area we will lay
     * out all images again
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
     * layout all the children of this table area
     */
    public void layoutChildren() {

        rect.setBoundingRect(this.getBoundingRect());
        Insets insets1 = getInsets();

        //get the bounding rectangle of this table area
        int x = getLeft() + insets1.left;
        int y = getTop() + insets1.top;
        int width = getWidth() - insets1.left - insets1.right;
        int height = getHeight() - insets1.top - insets1.bottom;

        itemImg.setBoundingRect(x, y, width, height);
    }

    class ItemPropertyChangeListener implements PropertyChangeListener {

        /**
         * listen for property change event
         * 
         * @param evt PropertyChangeEvent
         */
        public void propertyChange(PropertyChangeEvent evt) {

        }

    }
}

