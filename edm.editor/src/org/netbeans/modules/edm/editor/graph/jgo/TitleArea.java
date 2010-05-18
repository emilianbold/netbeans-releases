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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.nwoods.jgo.JGoArea;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoDocumentChangedEdit;
import com.nwoods.jgo.JGoDocumentEvent;
import com.nwoods.jgo.JGoImage;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TitleArea extends CanvasArea {

    /**
     * expansion state of this area
     */
    public static final int EXPANDED = 0;

    /**
     * collapse state of this area
     */
    public static final int COLLAPSED = 1;

    //Events hints
    /**
     * a CHANGED JGoDocumentEvent or JGoViewEvent hint, by default sent to the Parent of
     * the TableTitleArea. the new value sent event will be either
     * 
     * @see com.nwoods.jgo.examples.sql.table.TableTitleArea#EXPANDED or
     * @see com.nwoods.jgo.examples.sql.table.TableTitleArea#COLLAPSED toggle between
     *      expansion and collapse state
     */
    public static final int EXPANSION_STATE_CHANGED = JGoDocumentEvent.LAST + 50001;

    private static final Color DEFAULT_BG_COLOR = new Color(254, 253, 235);

    private int state = EXPANDED;

    private boolean showExpImage = true;
    
    private BasicText title;
    private JGoImage titleImg;
    private ImageArea expandedImg;
    private ImageArea collapsedImg;
    private JGoRectangle rect;

    private URL expandedImgUrl = getClass().getResource("/org/netbeans/modules/edm/editor/resources/expanded.gif");
    private URL collapsedImgUrl = getClass().getResource("/org/netbeans/modules/edm/editor/resources/collapsed.gif");
    private URL titleImgUrl = getClass().getResource("/org/netbeans/modules/edm/editor/resources/Table32.gif");

    // some constants for arranging various children
    private int titleIconAndTextGap = 2;
    private int textAndExpandedImgGap = 2;
    //private int textAndCheckBoxGap = 2;
    private int textTopGap = 0;
    private int minWidth = 0;

    /**
     * Creates a new instance of TableTitleArea
     * 
     * @param titleStr title string
     */
    public TitleArea(String titleStr) {
        this.setSelectable(false);
        this.setResizable(false);

        // add the bounding display rectangle
        rect = new InsetsRectangle();
        rect.setPen(JGoPen.makeStockPen(Color.WHITE));
        rect.setBrush(JGoBrush.makeStockBrush(DEFAULT_BG_COLOR));
        rect.setSelectable(false);
        rect.setResizable(false);
        addObjectAtHead(rect);

        // add text of title
        title = new BasicText(titleStr);
        title.setEditable(false);
        title.setSelectable(false);
        title.setResizable(false);
        title.setBold(true);
        title.setTransparent(true);
        addObjectAtTail(title);

        // add title image
        titleImg = new JGoImage();

        titleImg.loadImage(titleImgUrl, true);
        titleImg.setSize(titleImg.getImage().getWidth(null), titleImg.getImage().getHeight(null));
        titleImg.setSelectable(false);
        titleImg.setResizable(false);

        addObjectAtTail(titleImg);

        // add expansion image
        expandedImg = new ImageArea(expandedImgUrl);

        addObjectAtTail(expandedImg);

        // add collapse image
        collapsedImg = new ImageArea(collapsedImgUrl);
        collapsedImg.setVisible(false);
        addObjectAtTail(collapsedImg);

        this.insets = new Insets(3, 3, 3, 3);
    }

    /**
     * set the title image
     * 
     * @param icon icon
     */
    public void setTitleImage(Icon icon) {
        ImageIcon imgIcon = (ImageIcon) icon;
        titleImg.loadImage(imgIcon.getImage(), false);
        titleImg.setSize(imgIcon.getImage().getWidth(null), imgIcon.getImage().getHeight(null));
        
        // Force recalculation of minimum width.
        minWidth = 0;
        getMinimumWidth();
        this.update();
    }

    /**
     * set the expanded image
     * 
     * @param icon icon
     */
    public void setExpandedImage(Icon icon) {
        expandedImg.setIcon(icon);
        
        minWidth = 0;
        getMinimumWidth();
        update();        
    }

    /**
     * set the flag so that this text are will calculate for ...
     * 
     * @param show whether a calculation for ... is to be done
     */
    public void setShowDot(boolean show) {
        title.setShowDot(show);
    }

    /**
     * set the gap between text and expanded or collapsed images
     * 
     * @param gap the gap
     */
    public void setTextAndExpandedImgGap(int gap) {
        textAndExpandedImgGap = gap;
        
        minWidth = 0;
        getMinimumWidth();
        update();
    }

    /**
     * set the collapsed image
     * 
     * @param icon icon
     */
    public void setCollapsedImage(Icon icon) {
        collapsedImg.setIcon(icon);
        
        minWidth = 0;
        getMinimumWidth();
        update();
    }

    /**
     * initialize the title area
     * 
     * @param loc location
     */
    public void initialize(Point loc) {
        this.setBoundingRect(loc, new Dimension(getMaximumWidth(), getMaximumHeight()));
    }

    /**
     * get the maximum width
     * 
     * @return maximum width
     */
    public int getMaximumWidth() {
        int maxWidth = 0;
        if (title != null) {
            maxWidth += title.getMaximumWidth();
            maxWidth += titleIconAndTextGap;
        }

        if (titleImg != null) {
            maxWidth += titleImg.getWidth();
        }

        if (getState() == EXPANDED) {
            if (expandedImg != null) {
                maxWidth += expandedImg.getWidth();
                maxWidth += textAndExpandedImgGap;
            }
        } else {
            if (collapsedImg != null) {
                maxWidth += collapsedImg.getWidth();
                maxWidth += textAndExpandedImgGap;
            }
        }

        maxWidth += getInsets().left + getInsets().right;

        return maxWidth;
    }

    /**
     * minimum width will be width of title image plus width of title plus width of expand
     * image
     * 
     * @return minimum width
     */
    public int getMinimumWidth() {
        //calculate min width only at once
        if (minWidth != 0) {
            return minWidth;
        }

        minWidth = getMaximumWidth();
        return minWidth;
    }

    /**
     * get the maximum height
     * 
     * @return maximum height
     */
    public int getMaximumHeight() {
        int maxHeight = 0;

        if (title != null) {
            maxHeight = title.getHeight();
        }

        if (titleImg != null && maxHeight < titleImg.getHeight()) {
            maxHeight = titleImg.getHeight();
        }

        if (getState() == EXPANDED) {
            if (expandedImg != null && maxHeight < expandedImg.getHeight()) {
                maxHeight = expandedImg.getHeight();
            }
        } else {
            if (collapsedImg != null && maxHeight < collapsedImg.getHeight()) {
                maxHeight = collapsedImg.getHeight();
            }
        }

        maxHeight += getInsets().top + getInsets().bottom;

        return maxHeight;
    }

    /**
     * get minimum height. This will be equal to maximum height
     * 
     * @return minimum height
     */
    public int getMinimumHeight() {
        return getMaximumHeight();
    }

    /**
     * set the text of the title
     * 
     * @param text text
     */
    public void setTitle(String text) {
        title.setOriginalText(text);
        
        minWidth = 0;
        getMinimumWidth();
        update();
    }

    /**
     * toggle the state
     * 
     * @param point point
     * @return boolean
     */
    public boolean toggleState(Point point) {
        boolean returnFlag = false;

        Rectangle imageRect = null;
        if (collapsedImg.isVisible()) {
            imageRect = new Rectangle(collapsedImg.getLocation(), collapsedImg.getSize());
        } else {
            imageRect = new Rectangle(expandedImg.getLocation(), expandedImg.getSize());
        }

        //if user clicked on expansion or collapse images
        if (imageRect.contains(point)) {
            if (getState() == EXPANDED) {
                setState(COLLAPSED);
            } else {
                setState(EXPANDED);
            }
            returnFlag = true;
        }

        return returnFlag;
    }

    /**
     * get the state
     * 
     * @return state
     */
    public int getState() {
        return state;
    }

    /**
     * set the state
     * 
     * @param newState newState
     */
    public void setState(int newState) {
        if (this.state == newState) {
            return;
        }

        int oldState = this.state;
        this.state = newState;

        if (!showExpImage) {
            return;
        }

        //change the image
        if (newState == EXPANDED) {
            expandedImg.setVisible(true);
            collapsedImg.setVisible(false);
        } else {
            collapsedImg.setVisible(true);
            expandedImg.setVisible(false);
        }

        //fire CHANGED JGoDocumentEvent or JGoViewEvent hint
        update(EXPANSION_STATE_CHANGED, oldState, null);

        //by default notify parent of this area
        JGoArea parent = this.getParent();
        if (parent != null) {
            parent.update(EXPANSION_STATE_CHANGED, oldState, null);
        }
    }

    /**
     * copy new value for redo
     * 
     * @param e JGoDocumentChangedEdit
     */
    public void copyNewValueForRedo(JGoDocumentChangedEdit e) {
        if (e.getFlags() == EXPANSION_STATE_CHANGED) {
        } else {
            super.copyNewValueForRedo(e);
        }
    }

    /**
     * Change the cursor at the port
     * 
     * @param flags
     */
    public boolean doUncapturedMouseMove(int flags, Point dc, Point vc, JGoView view) {
        if (getLayer() != null && getLayer().isModifiable()) {
            view.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            return true;
        }
        return false;
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

        // arrange title image
        titleImg.setBoundingRect(x, y, titleImg.getWidth(), titleImg.getHeight());

        // arrange title
        int nonTitleWidth = titleImg.getWidth() + titleIconAndTextGap;

        if (getState() == EXPANDED) {
            if (expandedImg != null) {
                nonTitleWidth += expandedImg.getWidth();
                nonTitleWidth += textAndExpandedImgGap;
            }
        } else {
            if (collapsedImg != null) {
                nonTitleWidth += collapsedImg.getWidth();
                nonTitleWidth += textAndExpandedImgGap;
            }
        }

        title.setBoundingRect(x + titleImg.getWidth() + titleIconAndTextGap, y + textTopGap, width - nonTitleWidth, height);

        if (expandedImg != null && collapsedImg != null) {
            //arrange expansion image
            expandedImg.setLocation(x + width - expandedImg.getWidth(), y);

            //arrange collapse image
            collapsedImg.setSpotLocation(JGoObject.TopLeft, expandedImg, JGoObject.TopLeft);
        }
    }

    class ImageArea extends CanvasArea {
        JGoImage image;

        public ImageArea(URL imgUrl) {
            super();
            this.setSelectable(true);
            this.setResizable(false);
            this.setPickableBackground(false);

            image = new JGoImage();
            image.loadImage(imgUrl, true);
            image.setSize(image.getImage().getWidth(null), image.getImage().getHeight(null));
            image.setSelectable(false);
            image.setResizable(false);
            addObjectAtTail(image);

        }

        public void setIcon(Icon icon) {
            ImageIcon imgIcon = (ImageIcon) icon;
            image.loadImage(imgIcon.getImage(), false);
            image.setSize(imgIcon.getImage().getWidth(null), imgIcon.getImage().getHeight(null));

        }

        /**
         * redirect selction to a differnt object
         * 
         * @return object
         */
        public JGoObject redirectSelection() {
            //temporary change the selection color to white
            //JGoView.setDefaultPrimarySelectionColor(Color.RED);
            return this;
        }

        /**
         * called when this object is selected
         */
        public void gainedSelection() {
            //now change the seletion color back
            //JGoView.setDefaultPrimarySelectionColor(JGoView.getDefaultSecondarySelectionColor());
        }

        /**
         * layout the children of this cell area
         */
        public void layoutChildren() {
            Rectangle rectangle = this.getBoundingRect();
            image.setBoundingRect(rectangle);
        }

        /**
         * handle mouse click and do expansion or collapse
         * 
         * @param modifiers mouse modifers
         * @param dc document point
         * @param vc view point
         * @param view view
         * @return boolean
         */
        public boolean doMouseClick(int modifiers, Point dc, Point vc, JGoView view) {
            return ((TitleArea) this.getParent()).toggleState(dc);
        }

        /**
         * Change the cursor at the port
         * 
         * @param flags
         */
        public boolean doUncapturedMouseMove(int flags, Point dc, Point vc, JGoView view) {
            //System.out.println("image bounding rect " + this.getBoundingRect() + "dc "+
            // dc + "vc " + vc);
            if (getLayer() != null && getLayer().isModifiable()) {
                view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                return true;
            }
            return false;
        }
    }

    public void showExpansionImage(boolean show) {
        this.showExpImage = show;
        expandedImg.setVisible(show);
        collapsedImg.setVisible(show);
        this.layoutChildren();
    }

    public void setBackgroundColor(Color c) {
        if (this.rect != null) {
            this.rect.setBrush(JGoBrush.makeStockBrush(c));
        }
    }

    public void setBackgroundPaint(Paint p) {
        if (this.rect != null) {
            this.rect.setBrush(new JGoBrush(p));
        }
    }

    public void setPen(JGoPen newPen) {
        rect.setPen(newPen);
    }
    
    public JGoPen getPen() {
        return rect.getPen();
    }
    
    public void setBrush(JGoBrush newBrush) {
        rect.setBrush(newBrush);
    }
}

