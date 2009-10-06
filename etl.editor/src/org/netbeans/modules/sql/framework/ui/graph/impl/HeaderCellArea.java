/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.awt.Insets;
import java.awt.Rectangle;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class HeaderCellArea extends CanvasArea {

    private BasicText text;

    private JGoRectangle rect;

    /**
     * Creates a new instance of HeaderCellArea
     * 
     * @param columnName name of the column
     */
    public HeaderCellArea(String columnName) {
        super();
        this.setSelectable(false);
        this.setResizable(false);

        text = new BasicText(columnName);
        text.setSelectable(false);
        text.setResizable(false);
        text.setDraggable(false);

        //make it transparent so that we can see the effect of JGo3DRect
        text.setTransparent(true);
        //align the text to center
        text.setAlignment(JGoText.ALIGN_CENTER);
        //make the text bold
        text.setBold(true);
        this.addObjectAtTail(text);

        rect = new JGoRectangle();
        rect.setSelectable(false);
        rect.setResizable(false);

        rect.setPen(JGoPen.makeStockPen(Color.lightGray));
        rect.setBrush(JGoBrush.makeStockBrush(new Color(254, 253, 235)));
        addObjectAtHead(rect);

        //set the insets on header cell
        layoutChildren();
    }

    /**
     * get the minimum size of the area
     * 
     * @return minimum size
     */
    public Dimension getMinimumSize() {

        int w = text.getWidth() + insets.left + insets.right;
        int h = text.getHeight() + insets.top + insets.bottom;
        return new Dimension(w, h);
    }

    /**
     * paint this area
     * 
     * @param g Graphics2D
     * @param view view
     */
    public void paint(java.awt.Graphics2D g, JGoView view) {
        super.paint(g, view);

        Insets insets1 = getInsets();

        //get the bounding rectangle of this table area
        int width = getWidth() - insets1.left - insets1.right;

        int textWidth = text.getWidth();
        if (width < textWidth) {

        }
    }

    /**
     * override this method to handle the changes in the geometry of this area we will lay
     * out all the cell again
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
     * layout the children of this cell area
     */
    public void layoutChildren() {
        Rectangle rectangle = this.getBoundingRect();
        text.setBoundingRect(rectangle);
        rect.setBoundingRect(rectangle);

    }

}

