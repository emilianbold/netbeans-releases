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
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ItemListener;

import org.netbeans.modules.sql.framework.ui.graph.impl.BasicText;
import org.netbeans.modules.sql.framework.ui.graph.impl.CanvasArea;
import org.netbeans.modules.sql.framework.ui.graph.impl.CheckBoxArea;

import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Extension of CanvasArea to show toggle control for parentheses surrounding a given operator.
 * 
 * @author Ritesh Adval
 * @author Jonathan Giron
 * @version $Revision$
 */
public class ParenthesisCheckBoxArea extends CanvasArea {
    private JGoRectangle rect;

    private CheckBoxArea cbArea;

    private BasicText title;

    private int checkboxTextGap = 1;

    private static transient final Logger mLogger = Logger.getLogger(ParenthesisCheckBoxArea.class.getName());
    
    private static transient final Localizer mLoc = Localizer.get();
    /**
     * Constructs a default instance of ParenthesisCheckBoxArea.
     */
    ParenthesisCheckBoxArea() {
        this.setSelectable(false);
        this.setResizable(false);

        //add the bounding display rectangle
        rect = new JGoRectangle();

        rect.setPen(JGoPen.makeStockPen(Color.WHITE));
        rect.setBrush(JGoBrush.makeStockBrush(new Color(241, 249, 253)));
        rect.setSelectable(false);
        rect.setResizable(false);
        addObjectAtHead(rect);

        // Add check box
        cbArea = new CheckBoxArea();
        String nbBundle1 = mLoc.t("BUND424: Display or hide parentheses in SQL");
        String toolTipText = nbBundle1.substring(15);
        cbArea.setToolTipText(toolTipText);
        cbArea.setBackground(new Color(221, 235, 246));
        addObjectAtTail(cbArea);

        //add text of title
        String nbBundle2 = mLoc.t("BUND425: Use parentheses ( )");
        String titleText = nbBundle2.substring(15);
        title = new BasicText(titleText);
        title.setEditable(false);
        title.setSelectable(false);
        title.setResizable(false);
        title.setTransparent(true);
        title.setBold(false);

        title.setShowDot(false);
        addObjectAtTail(title);

        this.insets = new Insets(1, 3, 1, 3);
    }

    /**
     * layout the children of this cell area
     */
    public void layoutChildren() {
        Rectangle rectangle = this.getBoundingRect();
        rect.setBoundingRect(rectangle);

        Insets insets1 = getInsets();

        int x = this.getLeft() + insets1.left;
        int y = this.getTop() + insets1.top;
        int width = this.getWidth() - insets1.left - insets1.right;
        int height = this.getHeight() - insets1.top - insets1.bottom;

        cbArea.setBoundingRect(x, y, cbArea.getWidth(), height);
        int yCenteringAdjustment = Math.max(0, (cbArea.getHeight() - title.getHeight()) / 2);
        title.setBoundingRect(x + cbArea.getWidth() + checkboxTextGap, y + yCenteringAdjustment, width - cbArea.getWidth() - checkboxTextGap, height
            - (yCenteringAdjustment * 2));
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.ICanvasInterface#getMinimumWidth()
     */
    public int getMinimumWidth() {
        Insets insets1 = getInsets();
        int minWidth = insets1.left + insets1.right;
        minWidth += cbArea.getWidth();
        minWidth += checkboxTextGap;
        minWidth += title.getMaximumWidth();

        return minWidth;
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.ICanvasInterface#getMinimumHeight()
     */
    public int getMinimumHeight() {
        int minHeight = cbArea.getHeight();

        if (title.getHeight() > minHeight) {
            minHeight = title.getHeight();
        }

        minHeight += getInsets().top + getInsets().bottom;
        return minHeight;
    }

    /**
     * @see org.netbeans.modules.sql.framework.ui.graph.ICanvasInterface#getMaximumHeight()
     */
    public int getMaximumHeight() {
        // For now, fix this check box area to be uniform in height regardless of the
        // whether its parent JGoArea is resized.
        return getMinimumHeight();
    }

    /**
     * Change the cursor at the port
     * 
     * @param flags
     */
    public boolean doUncapturedMouseMove(int flags, Point dc, Point vc, JGoView view) {
        if (getLayer() != null && getLayer().isModifiable()) {
            view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return true;
        }
        return false;
    }

    /**
     * Adds the given item listener to this control.
     * 
     * @param l ItemListener to be added
     */
    public void addItemListener(ItemListener l) {
        this.cbArea.addItemListener(l);
    }

    /**
     * Removes the given item listener from this control.
     * 
     * @param l ItemListener to be removed
     */
    public void removeItemListener(ItemListener l) {
        this.cbArea.removeItemListener(l);
    }

    /**
     * whether to select or deselect check box ui
     */
    public void setShowParenthesis(boolean select) {
        this.cbArea.setSelected(select);
    }
    
    /**
     * Sets background color. 
     * 
     * @param c new background color
     */
    public void setBackgroundColor(Color c) {
        rect.setBrush(JGoBrush.makeStockBrush(c));
        cbArea.setBackground(c);
        title.setTransparent(true);
    }
    
    /**
     * Sets color of text label associated with the check box.
     * 
     * @param c new text color
     */
    public void setTextColor(Color c) {
        title.setTextColor(c);
    }
}
