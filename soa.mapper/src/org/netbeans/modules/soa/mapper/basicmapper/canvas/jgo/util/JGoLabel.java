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

package org.netbeans.modules.soa.mapper.basicmapper.canvas.jgo.util;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.VolatileImage;
import com.nwoods.jgo.JGoBrush;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoPen;
import com.nwoods.jgo.JGoRectangle;
import com.nwoods.jgo.JGoText;
import com.nwoods.jgo.JGoView;
import javax.swing.JLabel;

/**
 * <p>
 *
 * Title: </p> <p>
 *
 * Description: </p> <p>
 *
 * Copyright: Copyright (c) 2002 </p> <p>
 *
 * Company: </p>
 *
 * @author    Un Seng Leong
 * @created   December 4, 2002
 * @version   1.0
 */
public class JGoLabel
     extends AccessibleArea {

    public static final int CENTER = 0;
    public static final int LEFT   = 1;
    public static final int RIGHT  = 2;
    public static final int TOP    = 3;
    public static final int BOTTOM = 4;
    
    static {
        Font FONT_DEFAULT = new JLabel().getFont();
        JGoText.setDefaultFontFaceName(FONT_DEFAULT.getFontName());
        JGoText.setDefaultFontSize(FONT_DEFAULT.getSize());
    }
    
    private int mVerticalAlignment = CENTER;
    private int mHorizonalAligment = CENTER;
    private Insets mBorderSpace = new Insets(0, 8, 0, 5);
    private JGoRectangle mBoundRect;
    private JGoText mTextLabel;

    /**
     * this image holds the screen graphic to calculate the text width hight for this label.
     */
    private static VolatileImage mGraphicHolder = createGraphicImage();

    /**
     * the maximum pixel length that our text can span
     */
    private static final int MAX_TEXT_WIDTH = 250;
    
    /**
     * what gets catenated on to the end of a string that exceeds the MAX_TEXT_WIDTH
     */
    private static final String ELLIPSES = "...";
    
    
    /**
     * Creates a new JGoLabel object.
     */
    public JGoLabel() {
        super();
        initialize();
    }

    /**
     * Creates a new JGoLabel object.
     *
     * @param text  DOCUMENT ME!
     */
    public JGoLabel(java.lang.String text) {
        this();
        this.setText(text);
    }


    public JGoBrush getBrush() {
        return mBoundRect.getBrush();
    }

    public Font getFont() {
        return mTextLabel.getFont();
    }

    public int getFontSize() {
        return mTextLabel.getFontSize();
    }

    public int getHorizonalAligment() {
        return mHorizonalAligment;
    }

    public Insets getInsets() {
        return mBorderSpace;
    }

    public JGoPen getPen() {
        return mBoundRect.getPen();
    }

    public String getText() {
        return mTextLabel.getText();
    }

    public Color getTextColor() {
        return mTextLabel.getTextColor();
    }

    public int getVerticalAligment() {
        return mVerticalAlignment;
    }

    public boolean isUnderline() {
        return mTextLabel.isUnderline();
    }

    public void setBrush(JGoBrush brush) {
        mBoundRect.setBrush(brush);
    }

    public void setBold(boolean isBold) {
        mTextLabel.setBold(isBold);
    }
    
    public void setItalic(boolean isItalic) {
        mTextLabel.setItalic(isItalic);
    }
    
    public void setFontSize(int fontSize) {
        mTextLabel.setFontSize(fontSize);
        ensureLabelSize();
    }

    public void setHorizonalAligment(int horizonalAligment) {
        if (
            (horizonalAligment != CENTER)
            && (horizonalAligment != LEFT)
            && (horizonalAligment != RIGHT)) {
            throw new java.lang.IllegalArgumentException(
                "Invalid horizonal aligment; either LEFT, CENTER, RIGHT");
        }

        mHorizonalAligment = horizonalAligment;
        layoutLabel();
    }

    public void setInsets(Insets border) {
        mBorderSpace = border;
        ensureLabelSize();
    }

    public void setPen(JGoPen pen) {
        mBoundRect.setPen(pen);
    }

    public void setText(String text) {
        String restrictedText = restrictTextWidth(text);
        mTextLabel.setText(restrictedText);
        ensureLabelSize();
        layoutChildren();
    }

    private String restrictTextWidth(String text) {
        if (mGraphicHolder.validate(getDefaultConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE) {
            mGraphicHolder = createGraphicImage();
        }
        Graphics g = mGraphicHolder.getGraphics();
        g.setFont(mTextLabel.getFont());
        int desiredTextWidth = g.getFontMetrics().stringWidth(ELLIPSES);
        StringBuffer buf = new StringBuffer();
        char[] chars = text.toCharArray();
        int charSize = 0;
        for (int i=0, size=chars.length; i < size; i++) {
            charSize = g.getFontMetrics().stringWidth(String.valueOf(chars[i]));
            if (desiredTextWidth + charSize <= MAX_TEXT_WIDTH) {
                buf.append(chars[i]);
                desiredTextWidth += charSize;
            } else {
                buf.append(ELLIPSES);
                break;
            }
        }
        return buf.toString();
    }

    public void setTextColor(Color textColor) {
        mTextLabel.setTextColor(textColor);
    }

    public void setUnderline(boolean underline) {
        mTextLabel.setUnderline(underline);
    }

    public void setVerticalAligment(int verticalAligment) {
        if (
            (verticalAligment != CENTER)
            && (verticalAligment != TOP)
            && (verticalAligment != BOTTOM)) {
            throw new java.lang.IllegalArgumentException(
                "Invalid vertical aligment; either TOP, CENTER, BOTTOM");
        }

        mVerticalAlignment = verticalAligment;
        layoutLabel();
    }

    public void geometryChange(Rectangle prevRect) {
        // see if this is just a move and not a scale
        if ((prevRect.width == getWidth()) && (prevRect.height == getHeight())) {
            // let the default JGoArea implementation do the work
            super.geometryChange(prevRect);
        } else {
            ensureSize();
        }
    }

    public void doStartEditing(JGoView view, Point poc) {
        mTextLabel.doStartEdit(view, poc);
    }

    /**
     * Return the minimum width of this label. It depends on the text width, and
     * the border left and right space.
     *
     * @return   return the minimum width of this label
     */
    public int getMinimumWidth() {
        return mTextLabel.getWidth() + mBorderSpace.left + mBorderSpace.right;
    }

    /**
     * Return the minimum height of this label. It depends on the text height,
     * and border top and bottom space.
     *
     * @return   The minimumHeight value
     */
    public int getMinimumHeight() {
        return mTextLabel.getHeight() + mBorderSpace.top + mBorderSpace.bottom;
    }

    /**
     * Resize itself to the minimum size.
     */
    public void resizeToMinimum() {
        this.setSize(getMinimumWidth(), getMinimumHeight());
    }

    // relocate the children due to the location change
    /**
     * Description of the Method
     */
    protected void layoutChildren() {
        layoutLabel();
        mBoundRect.setBoundingRect(getBoundingRect());
    }

    /**
     * Return the system default graphics configuration.
     *
     * @return GraphicsConfiguration the os system default graphics configuration
     */
    private static GraphicsConfiguration getDefaultConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    /**
     * Return the image for calculating the text width and height.
     *
     * @return VolatileImage the image is created by the default graphics configuration.
     */
    private static VolatileImage createGraphicImage() {
        return getDefaultConfiguration().createCompatibleVolatileImage(1,1);
    }

    private void ensureLabelSize() {
        if (mGraphicHolder.validate(getDefaultConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE) {
            mGraphicHolder = createGraphicImage();
        }

        Graphics g = mGraphicHolder.getGraphics();

        g.setFont(mTextLabel.getFont());

        int textWidth = g.getFontMetrics().stringWidth(mTextLabel.getText());
        int textHeight = g.getFontMetrics(mTextLabel.getFont()).getHeight();

        if (textWidth != mTextLabel.getWidth() || textHeight != mTextLabel.getHeight()) {
            mTextLabel.setSize (new Dimension(textWidth, textHeight));
            ensureSize();
        }
    }

    private void ensureSize() {

        // the width and height must be separated compare.
        int requireWidth = this.getMinimumWidth();
        if (getWidth() < requireWidth) {
            // this will trigger the JGo geometryChange framework and
            // call this method recursively
            this.setWidth(requireWidth);
            return;
        }

        int requireHeight = this.getMinimumHeight();
        if (getHeight() < requireHeight) {
            // this will trigger the JGo geometryChange framework and
            // call this method recursively
            this.setHeight(requireHeight);
            return;
        }

        layoutChildren();
    }

    private void initialize() {
        mTextLabel = new JGoText();
        mTextLabel.setSelectable(false);
        mTextLabel.setDraggable(false);
        mTextLabel.setAutoResize(false);
        mTextLabel.setEditable(false);
        mTextLabel.setResizable(false);
        mTextLabel.setTransparent(true);
        mTextLabel.setAlignment(JGoText.ALIGN_CENTER);

        mBoundRect = new JGoRectangle();
        mBoundRect.setSelectable(false);
        mBoundRect.setDraggable(false);
        mBoundRect.setResizable(false);
        mBoundRect.setPen(JGoPen.make(JGoPen.SOLID, 1, Color.BLACK));

        addObjectAtTail(mBoundRect);
        addObjectAtTail(mTextLabel);
    }

    private void layoutLabel() {
        switch (mVerticalAlignment) {
        case CENTER:

            switch (mHorizonalAligment) {
            case CENTER:
                mTextLabel.setSpotLocation(
                    JGoObject.Center,
                    this,
                    JGoObject.Center);

                if (mBorderSpace.left > 0) {
                    int diff = mTextLabel.getLeft() - this.getLeft();

                    if (diff < mBorderSpace.left) {
                        mTextLabel.setLeft(
                            mTextLabel.getLeft()
                            + (mBorderSpace.left - diff));
                    }
                }

                if (mBorderSpace.right > 0) {
                    int leftdiff =
                        mTextLabel.getLeft() - this.getLeft();
                    int rightdiff =
                        this.getWidth() - mTextLabel.getWidth()
                        - leftdiff;

                    if (rightdiff < mBorderSpace.right) {
                        mTextLabel.setLeft(
                            mTextLabel.getLeft()
                            - (mBorderSpace.right - rightdiff));
                    }
                }

                if (mBorderSpace.top > 0) {
                    int diff = mTextLabel.getTop() - this.getTop();

                    if (diff < mBorderSpace.top) {
                        mTextLabel.setTop(
                            mTextLabel.getTop()
                            + (mBorderSpace.top - diff));
                    }
                }

                if (mBorderSpace.bottom > 0) {
                    int topdiff =
                        mTextLabel.getTop() - this.getTop();
                    int bottomdiff =
                        this.getHeight() - mTextLabel.getHeight()
                        - topdiff;

                    if (bottomdiff < mBorderSpace.bottom) {
                        mTextLabel.setTop(
                            mTextLabel.getTop()
                            - (mBorderSpace.bottom - bottomdiff));
                    }
                }

                break;
            case LEFT:
                mTextLabel.setSpotLocation(
                    JGoObject.LeftCenter,
                    this,
                    JGoObject.LeftCenter);

                if (mBorderSpace.left > 0) {
                    mTextLabel.setLeft(
                        mTextLabel.getLeft() + mBorderSpace.left);
                }

                break;
            case RIGHT:
                mTextLabel.setSpotLocation(
                    JGoObject.RightCenter,
                    this,
                    JGoObject.RightCenter);

                if (mBorderSpace.right > 0) {
                    mTextLabel.setLeft(
                        mTextLabel.getLeft() - mBorderSpace.left);
                }

                break;
            }

            break;
        case TOP:

            switch (mHorizonalAligment) {
            case CENTER:
                mTextLabel.setSpotLocation(
                    JGoObject.TopCenter,
                    this,
                    JGoObject.TopCenter);

                break;
            case LEFT:
                mTextLabel.setSpotLocation(
                    JGoObject.TopLeft,
                    this,
                    JGoObject.TopLeft);

                if (mBorderSpace.left > 0) {
                    mTextLabel.setLeft(
                        mTextLabel.getLeft() + mBorderSpace.left);
                }

                break;
            case RIGHT:
                mTextLabel.setSpotLocation(
                    JGoObject.TopRight,
                    this,
                    JGoObject.TopRight);

                if (mBorderSpace.right > 0) {
                    mTextLabel.setLeft(
                        mTextLabel.getLeft() - mBorderSpace.left);
                }

                break;
            }

            if (mBorderSpace.top > 0) {
                mTextLabel.setTop(mTextLabel.getTop() + mBorderSpace.top);
            }

            break;
        case BOTTOM:

            switch (mHorizonalAligment) {
            case CENTER:
                mTextLabel.setSpotLocation(
                    JGoObject.BottomCenter,
                    this,
                    JGoObject.BottomCenter);

                break;
            case LEFT:
                mTextLabel.setSpotLocation(
                    JGoObject.BottomLeft,
                    this,
                    JGoObject.BottomLeft);

                if (mBorderSpace.left > 0) {
                    mTextLabel.setLeft(
                        mTextLabel.getLeft() + mBorderSpace.left);
                }

                break;
            case RIGHT:
                mTextLabel.setSpotLocation(
                    JGoObject.BottomRight,
                    this,
                    JGoObject.BottomRight);

                if (mBorderSpace.right > 0) {
                    mTextLabel.setLeft(
                        mTextLabel.getLeft() - mBorderSpace.left);
                }

                break;
            }

            if (mBorderSpace.bottom > 0) {
                mTextLabel.setTop(
                    mTextLabel.getTop() - mBorderSpace.bottom);
            }
        }
    }
}
