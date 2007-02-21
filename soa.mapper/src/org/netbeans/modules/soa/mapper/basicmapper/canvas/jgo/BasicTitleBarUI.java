/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo;

import com.nwoods.jgo.JGoRectangle;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoImage;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util.AccessibleArea;
import org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util.JGoLabel;

/**
 * <p>
 *
 * Title: </p> BasicTitleBarUI <p>
 *
 * Description: </p> BasicTitleBarUI provides an implemation of visiual
 * JGoObject of the methoid canvas node title bar.<p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public class BasicTitleBarUI
     extends AccessibleArea {
    /**
     * the log instance
     */
    private static final Logger LOGGER = Logger.getLogger(BasicTitleBarUI.class.getName());


    /**
     * the default expend image file
     */
    private static final String EXPENDED_IMAGE_PATH = "expended.gif";

    /**
     * the default collapse image file
     */
    private static final String COLLAPSED_IMAGE_PATH = "collapsed.gif";

    /**
     * the default title bar background brush
     */
    private static final JGoBrush TITLE_BRUSH = 
            JGoBrush.makeStockBrush(new Color(255, 255, 255, 0)); // transparent
    
    private static final Color  COLOR_INNER_BORDER = 
            new Color(186, 205, 240); // baby blue

    /**
     * the default title bar text color
     */
    private static final Color DEFAULT_TEXT_COLOR = Color.BLACK;
    
    /**
     * the number of left margin
     */
    private int mLeftSpace = 2;

    /**
     * the space between title and icon
     */
    private int mTitleIconAndTextGap = 2;

    /**
     * the space between title and button
     */
    private int mTitleTextAndButtonsGap = 2;

    /**
     * the top margin
     */
    private int mTopSpace = 1;

    /**
     * the bottom margin
     */
    private int mBottomSpace = 1;

    /**
     * the botton space
     */
    private int mButtonsGap = 2;

    /**
     * the min width of this title bar
     */
    private int minWidth = 0;

    /**
     * the min height of this title bar
     */
    private int minHeight = 0;

    /**
     * default collapse icon
     */
    private static Image mDefaultCollapsedIcon;

    /**
     * default expend icon
     */
    private static Image mDefaultExpendedIcon;

    /**
     * the border rectangle
     */
    private JGoRectangle mBorderRect;

    /**
     * the collapse JGoImage instance
     */
    private JGoImage mCollapsedJGoImage;

    /**
     * the expand JGoImage instance
     */
    private JGoImage mExpandedJGoImage;

    /**
     * the title image JGoImage instance
     */
    private JGoImage mTitleJGoImage;

    /**
     * the title lable instance
     */
    private JGoLabel mTitleLabel;

    static {
        try {
            mDefaultExpendedIcon =
                new ImageIcon(
                BasicTitleBarUI.class.getResource(EXPENDED_IMAGE_PATH))
                .getImage();
        } catch (java.lang.Throwable t) {
            LOGGER.log(Level.SEVERE, "unable to load default expended icon", t);
        }

        try {
            mDefaultCollapsedIcon =
                new ImageIcon(
                BasicTitleBarUI.class.getResource(COLLAPSED_IMAGE_PATH))
                .getImage();
        } catch (java.lang.Throwable t) {
            LOGGER.log(Level.SEVERE, "unable to load default collapsed icon", t);
        }
    }

    /**
     * Creates a new BasicTitleBarUI object.
     */
    public BasicTitleBarUI() {
        this(null);
    }

    /**
     * Creates a new BasicTitleBarUI object, with the specified title text.
     *
     * @param title  the title text of this title bar
     */
    public BasicTitleBarUI(String title) {
        this(title, null);
    }

    /**
     * Creates a new BasicTitleBarUI object, with the specified title text and
     * icon.
     *
     * @param title      the title text of this title bar
     * @param titleIcon  the title icon of this title bar
     */
    public BasicTitleBarUI(
        String title,
        Image titleIcon) {
        this(title, titleIcon, mDefaultExpendedIcon, mDefaultCollapsedIcon);
    }

    /**
     * Creates a new BasicTitleBarUI object, with the specified title text and
     * icon, and expand and collapse button icon.
     *
     * @param title          the title text of this title bar
     * @param titleIcon      the title icon of this title bar
     * @param expendedIcon   the expended icon image
     * @param collapsedIcon  the collapse icon image
     */
    public BasicTitleBarUI(
        String title,
        Image titleIcon,
        Image expendedIcon,
        Image collapsedIcon) {
        mBorderRect = new JGoRectangle();
        mTitleJGoImage = new JGoImage();
        mExpandedJGoImage = new JGoImage();
        mCollapsedJGoImage = new JGoImage();
        mTitleLabel = new JGoLabel(title);
        mTitleLabel.setSelectable(false);
        mTitleLabel.setInsets(new Insets(0,0,0,0));
        mTitleLabel.setPen(null);
        mTitleLabel.setTextColor(DEFAULT_TEXT_COLOR);

        setTitleIcon(titleIcon);
        setExpendedIcon(expendedIcon);
        setCollapseIcon(collapsedIcon);

        mBorderRect.setSelectable(false);
        mTitleLabel.setSelectable(false);
        mTitleJGoImage.setSelectable(false);
        mExpandedJGoImage.setSelectable(false);
        mCollapsedJGoImage.setSelectable(false);

        mBorderRect.setResizable(false);
        mTitleLabel.setResizable(false);
        mTitleJGoImage.setResizable(false);
        mExpandedJGoImage.setResizable(false);
        mCollapsedJGoImage.setResizable(false);

        mTitleJGoImage.setDraggable(false);
        mTitleLabel.setDraggable(false);
        mExpandedJGoImage.setDraggable(false);
        mCollapsedJGoImage.setDraggable(false);
        mBorderRect.setDraggable(false);

        this.setResizable(false);
        this.setSelectable(false);
        this.setDraggable(true);

        mBorderRect.setBrush(TITLE_BRUSH);
        mBorderRect.setPen(JGoPen.makeStockPen(COLOR_INNER_BORDER));
        
        this.addObjectAtTail(mTitleLabel);
        this.addObjectAtTail(mTitleJGoImage);
        this.addObjectAtTail(mExpandedJGoImage);
        this.addObjectAtTail(mCollapsedJGoImage);
        this.addObjectAtHead(mBorderRect);

        expand();
        ensureSize();
    }

    /**
     * Return the collapse button icon
     *
     * @return   the collapse button icon
     */
    public Image getCollapseIcon() {
        return mCollapsedJGoImage.getImage();
    }

    /**
     * Return the expanded button icon
     *
     * @return   the expanded button icon
     */
    public Image getExpendedIcon() {
        return mExpandedJGoImage.getImage();
    }

    /**
     * Return the font to use.
     *
     * @return   the font to use.
     */
    public Font getFont() {
        return mTitleLabel.getFont();
    }

    /**
     * Return the title text
     *
     * @return   the title text
     */
    public String getTitle() {
        return mTitleLabel.getText();
    }

    /**
     * Retrun The title icon.
     *
     * @return   The title icon.
     */
    public Image getTitleIcon() {
        return mTitleJGoImage.getImage();
    }

    /**
     * Return true if the point is within the button of expand or collapse
     * button. false otherwise.
     *
     * @param point  the point ot check
     * @return       true if the point is within the button of expand or
     *      collapse button. false otherwise.
     */
    public boolean isInButton(Point point) {
        Rectangle imageRect = null;

        if (mCollapsedJGoImage.isVisible()) {
            imageRect =
                new Rectangle(
                mCollapsedJGoImage.getLocation(),
                mCollapsedJGoImage.getSize());
        } else {
            imageRect =
                new Rectangle(
                mExpandedJGoImage.getLocation(),
                mExpandedJGoImage.getSize());
        }
        return imageRect.contains(point);
    }

    /**
     * Set the collapse icon image
     *
     * @param image  the collapse icon image
     */
    public void setCollapseIcon(Image image) {
        if (image == null) {
            this.removeObject(mCollapsedJGoImage);
            mCollapsedJGoImage = new JGoImage();
            mCollapsedJGoImage.setSelectable(false);
            mCollapsedJGoImage.setDraggable(false);
            mCollapsedJGoImage.setResizable(false);
            mCollapsedJGoImage.setSize(0,0);
            this.addObjectAtTail(mCollapsedJGoImage);
        } else {
            mCollapsedJGoImage.loadImage(image,true);
            mCollapsedJGoImage.setSize(
                image.getWidth(null),
                image.getHeight(null));
        }
        ensureSize();
    }

    /**
     * Sets the expand icon image.
     *
     * @param image  the expand icon image.
     */
    public void setExpendedIcon(Image image) {
        if (image == null) {
            this.removeObject(mExpandedJGoImage);
            mExpandedJGoImage = new JGoImage();
            mExpandedJGoImage.setSelectable(false);
            mExpandedJGoImage.setDraggable(false);
            mExpandedJGoImage.setResizable(false);
            mExpandedJGoImage.setSize(0, 0);
            this.addObjectAtTail(mExpandedJGoImage);
        } else {
            mExpandedJGoImage.loadImage(image, true);
            mExpandedJGoImage.setSize(
                image.getWidth(null),
                image.getHeight(null));
        }
        ensureSize();
    }

    /**
     * Set the title text
     *
     * @param title  the title text
     */
    public void setTitle(String title) {
        mTitleLabel.setText(title);
        ensureSize();
   }

    /**
     * Set the title icon.
     *
     * @param image  the title icon.
     */
    public void setTitleIcon(Image image) {
        if (image == null) {
            this.removeObject(mTitleJGoImage);
            mTitleJGoImage = new JGoImage();
            mTitleJGoImage.setSelectable(false);
            mTitleJGoImage.setDraggable(false);
            mTitleJGoImage.setResizable(false);
            mTitleJGoImage.setSize(0,0);
            this.addObjectAtTail(mTitleJGoImage);
        } else {
            mTitleJGoImage.loadImage(image, true);
            mTitleJGoImage.setSize(image.getWidth(null), image.getHeight(null));
        }
        ensureSize();
   }

    /**
     * Collapse the title bar
     */
    public void collapse() {
        mCollapsedJGoImage.setVisible(true);
        mExpandedJGoImage.setVisible(false);
    }

    /**
     * Expand the title bar
     */
    public void expand() {
        mCollapsedJGoImage.setVisible(false);
        mExpandedJGoImage.setVisible(true);
    }

    /**
     * Invokes when the size or location of this title bar changes.
     *
     * @param prevRect  the pervious rectangle
     */
    public void geometryChange(Rectangle prevRect) {
        // see if this is just a move and not a scale
        if ((prevRect.width == getWidth()) && (prevRect.height == getHeight())) {
            // let the default JGoArea implementation do the work
            super.geometryChange(prevRect);
        } else {
            ensureSize();
        }
    }

    public int getMinimumWidth() {
        // minimum width =
        // left border and title icon gap +
        // title icon width +
        // title icon and text gap +
        // title label width +
        // title lable and button gap +
        // button width (pick the wider one between expended and collapse image) +
        // button and right border gap
        return mLeftSpace + mTitleJGoImage.getWidth() + mTitleIconAndTextGap
            + mTitleLabel.getWidth() + mTitleTextAndButtonsGap
            + Math.max(mExpandedJGoImage.getWidth(), mCollapsedJGoImage.getWidth())
            + mButtonsGap;
    }

    public int getMinimumHeight() {
        // minimum height =
        // top border space +
        // height of title icon or title label or expended icon or collapsed icon, the longer one +
        // bottom border space
        return mTopSpace + Math.max(
            Math.max(mTitleLabel.getHeight(), mTitleJGoImage.getHeight()),
            Math.max(mExpandedJGoImage.getWidth(), mCollapsedJGoImage.getWidth()))
            + mBottomSpace;
    }

    /**
     * Resize itself to the minimum size.
     */
    public void resizeToMinimum() {
        this.setSize(getMinimumWidth(), getMinimumHeight());
    }

    private void ensureSize() {
        int miniWidth = getMinimumWidth();
        if (getWidth() < miniWidth) {
            // this will trigger the JGo geometryChange framework and
            // call this method recursively
            setWidth(miniWidth);
            return;
        }
        int miniHeight = getMinimumHeight();
        if (getHeight() < miniHeight) {
            // this will trigger the JGo geometryChange framework and
            // call this method recursively
            setHeight(miniHeight);
            return;
        }
        layoutChildren();
    }

    /**
     * Layout children of this title bar.
     */
    protected void layoutChildren() {
        mTitleJGoImage.setLeft(getLeft() + mLeftSpace);
        mTitleJGoImage.setTop(getTop() + mTopSpace);

        mTitleLabel.setSpotLocation(JGoObject.TopLeft, mTitleJGoImage, JGoObject.TopRight);
        mTitleLabel.setLeft(mTitleLabel.getLeft() + mTitleIconAndTextGap);

        mExpandedJGoImage.setSpotLocation(JGoObject.TopRight,this,JGoObject.TopRight);
        mExpandedJGoImage.setLeft(mExpandedJGoImage.getLeft() - mButtonsGap);
        mExpandedJGoImage.setTop(mExpandedJGoImage.getTop() + mButtonsGap);

        mCollapsedJGoImage.setSpotLocation(
            JGoObject.TopLeft,mExpandedJGoImage,JGoObject.TopLeft);

        mBorderRect.setBoundingRect(this.getBoundingRect());
    }

    public void setTitleBarBackground(Color color) {
//        mBorderRect.setBrush(JGoBrush.makeStockBrush(color));
    }
    
    public void resetTitleBarBackground() {
//        mBorderRect.setBrush(TITLE_BRUSH);
    }
    
    public Color getTitleBarBackground() {
        return mBorderRect.getBrush().getColor();
    }
}
