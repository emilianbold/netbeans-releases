/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.image;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;

import java.awt.image.BufferedImage;

import org.netbeans.jemmy.JemmyException;

/**
 * Contains util methods to work with images.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class ImageTool {

    /**
     * Gets an image from a rectange on screen.
     */
    public static BufferedImage getImage(Rectangle rect) {
        try {
            return(new Robot().createScreenCapture(rect));
        } catch(AWTException e) {
            throw(new JemmyException("Exception during screen capturing", e));
        }
    }

    /**
     * Gets an image from a component.
     */
    public static BufferedImage getImage(Component comp) {
        return(getImage(new Rectangle(comp.getLocationOnScreen(),
                                      comp.getSize())));
    }

    /**
     * Gets the whole screen image.
     */
    public static BufferedImage getImage() {
        return(getImage(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize())));
    }

    /**
     * Increases image.
     */
    public static BufferedImage enlargeImage(BufferedImage image, int zoom) {
        int wight  = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(wight  * zoom,
                                                 height * zoom,
                                                 image.getType());
        int rgb;
        for(int x = 0; x < wight; x++) {
            for(int y = 0; y < height; y++) {
                rgb = image.getRGB(x, y);
                for(int i = 0; i < zoom; i++) {
                    for(int j = 0; j < zoom; j++) {
                        result.setRGB(x * zoom + i,
                                      y * zoom + j,
                                      rgb);
                    }
                }
            }
        }
        return(result);
    }

    /**
     * Substracts second image from first one.
     * Could be used to save file difference for future analysis.
     */
    public static BufferedImage substractImage(BufferedImage minuend, BufferedImage deduction) {
        return(substractImage(minuend, deduction, 0, 0));
    }

    /**
     * Substracts subimage from image.
     * Could be used to save file difference for future analysis.
     * @param minuend - image
     * @param deduction - subimage
     * @param relativeX - deduction-in-minuend X coordinate
     * @param relativeY - deduction-in-minuend Y coordinate
     */
    public static BufferedImage substractImage(BufferedImage minuend, BufferedImage deduction, int relativeX, int relativeY) {
        int mWidth  = minuend.getWidth();
        int mHeight = minuend.getHeight();
        int dWidth  = deduction.getWidth();
        int dHeight = deduction.getHeight();

        int maxWidth  = (mWidth  > relativeX + dWidth ) ? mWidth  : (relativeX + dWidth);
        int maxHeight = (mHeight > relativeY + dHeight) ? mHeight : (relativeY + dHeight);

        BufferedImage result = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
        int mColor, dColor;
        for(int x = 0; x < maxWidth; x++) {
            for(int y = 0; y < maxHeight; y++) {
                if(x >= mWidth || 
                   y >= mHeight) {
                    mColor = 0;
                } else {
                    mColor = minuend.getRGB(x, y);
                }
                if(x >= dWidth  + relativeX || 
                   y >= dHeight + relativeY || 
                   x < relativeX || 
                   y < relativeY) {
                    dColor = 0;
                } else {
                    dColor = deduction.getRGB(x - relativeX, y - relativeY);
                }
                result.setRGB(x, y, substractColors(mColor, dColor));
            }
        }
        return(result);
    }

    private static int substractColors(int mRGB, int dRGB) {
        Color mColor = new Color(mRGB);
        Color dColor = new Color(dRGB);
        int red   = substractColor(mColor.getRed()  , dColor.getRed());
        int green = substractColor(mColor.getGreen(), dColor.getGreen());
        int blue  = substractColor(mColor.getBlue() , dColor.getBlue());
        return(new Color(red, green, blue).getRGB());
    }

    private static int substractColor(int mColor, int dColor) {
        if(mColor >= dColor) {
            return(mColor - dColor);
        } else {
            return(mColor - dColor + 0Xff);
        }
    }
}
