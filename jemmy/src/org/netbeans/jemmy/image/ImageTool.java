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
 * Contributor(s): Alexandre Iline.
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
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
 *
 *
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
     * @param rect a rectangle on screen in absolute screen coordinates.
     * @return a captured image.
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
     * @param comp a visible component.
     * @return a captured image.
     */
    public static BufferedImage getImage(Component comp) {
        return(getImage(new Rectangle(comp.getLocationOnScreen(),
                                      comp.getSize())));
    }

    /**
     * Gets the whole screen image.
     * @return a captured image.
     */
    public static BufferedImage getImage() {
        return(getImage(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize())));
    }

    /**
     * Increases image.
     * @param image an image to enlarge.
     * @param zoom A scale.
     * @return a result image.
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
     * @deprecated Use subtractImage(BufferedImage, BufferedImage) instead.
     * @param minuend an image to subtract from.
     * @param deduction an image to subtract.
     * @return a result image.
     */
    public static BufferedImage substractImage(BufferedImage minuend, BufferedImage deduction) {
        return(subtractImage(minuend, deduction));
    }

    /**
     * Subtracts second image from first one.
     * Could be used to save file difference for future analysis.
     * @param minuend an image to subtract from.
     * @param deduction an image to subtract.
     * @return a result image.
     */
    public static BufferedImage subtractImage(BufferedImage minuend, BufferedImage deduction) {
        return(subtractImage(minuend, deduction, 0, 0));
    }

    /**
     * @deprecated Use subtractImage(BufferedImage, BufferedImage, int, int) instead.
     * @param minuend an image to subtract from.
     * @param deduction an image to subtract.
     * @return a result image.
     */
    public static BufferedImage substractImage(BufferedImage minuend, BufferedImage deduction, int relativeX, int relativeY) {
        return(subtractImage(minuend, deduction, relativeX, relativeY));
    }

    /**
     * Subtracts subimage from image.
     * Could be used to save file difference for future analysis.
     * @param minuend an image to subtract from.
     * @param deduction an image to subtract.
     * @param relativeX - deduction-in-minuend X coordinate
     * @param relativeY - deduction-in-minuend Y coordinate
     * @return a result image.
     */
    public static BufferedImage subtractImage(BufferedImage minuend, BufferedImage deduction, int relativeX, int relativeY) {
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
                result.setRGB(x, y, subtractColors(mColor, dColor));
            }
        }
        return(result);
    }

    private static int subtractColors(int mRGB, int dRGB) {
        Color mColor = new Color(mRGB);
        Color dColor = new Color(dRGB);
        int red   = subtractColor(mColor.getRed()  , dColor.getRed());
        int green = subtractColor(mColor.getGreen(), dColor.getGreen());
        int blue  = subtractColor(mColor.getBlue() , dColor.getBlue());
        return(new Color(red, green, blue).getRGB());
    }

    private static int subtractColor(int mColor, int dColor) {
        if(mColor >= dColor) {
            return(mColor - dColor);
        } else {
            return(mColor - dColor + 0Xff);
        }
    }
}
