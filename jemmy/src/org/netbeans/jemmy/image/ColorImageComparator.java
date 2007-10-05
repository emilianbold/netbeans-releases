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

import java.awt.image.BufferedImage;

/**
 * Compares two images with color mapping defined by <code>ColorModel</code> implementation.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class ColorImageComparator extends StrictImageComparator {

    ColorMap leftMap, rightMap;
    ImageComparator comparator = null;

    /**
     * Creates a comparator with a color maps.
     * Object created by this constructor behaves like <code>StrictImageComparator</code>.
     * Object created works faster because it does not create intermediate images
     * for another comparator.
     * @param map Map applied to both left and right images during comparision.
     */
    public ColorImageComparator(ColorMap map) {
        leftMap  = map;
        rightMap = map;
    }

    /**
     * Creates a comparator with <code>map</code> color mapping.
     * Actual comparision perfomed by <code>comparator</code> parameter.
     * @param map Map applied to both left and right images during comparision.
     * @param subComparator comporator to perform a comparision of to images with mapped colors.
     */
    public ColorImageComparator(ColorMap map, ImageComparator subComparator) {
        this(map);
        this.comparator = subComparator;
    }

    /**
     * Creates a comparator with two color maps.
     * Object created by this constructor behaves like <code>StrictImageComparator</code>.
     * Object created works faster because it does not create intermediate images
     * for another comparator.
     * @param leftMap Map applied to the left image during comparision.
     * @param rightMap Map applied to the right image during comparision.
     */
    public ColorImageComparator(ColorMap leftMap, ColorMap rightMap) {
        this.leftMap  = leftMap;
        this.rightMap = rightMap;
    }

    /**
     * Creates a comparator with two color maps.
     * Actual comparision perfomed by <code>comparator</code> parameter.
     * @param leftMap Map applied to the left image during comparision.
     * @param rightMap Map applied to the right image during comparision.
     * @param subComparator comporator to perform a comparision of to images with mapped colors.
     */
    public ColorImageComparator(ColorMap leftMap, ColorMap rightMap, ImageComparator subComparator) {
        this(leftMap, rightMap);
        this.comparator = subComparator;
    }

    /**
     * Compares images by <code>ImageComparator</code> passed into constructor,
     * or itself if no <code>ImageComparator</code> was passed, processing both images
     * by <code>ColorMap</code> instance before comparision.
     */
    public boolean compare(BufferedImage image1, BufferedImage image2) {
        if(comparator != null) {
            return(comparator.compare(recolor(image1, leftMap), recolor(image2, rightMap)));
        } else {
            return(super.compare(image1, image2));
        }
    }
    private BufferedImage recolor(BufferedImage src, ColorMap map) {
        BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        for(int x = 0; x < src.getWidth(); x++) {
            for(int y = 0; y < src.getWidth(); y++) {
                result.setRGB(x, y, map.mapColor(src.getRGB(x, y)));
            }
        }
        return(result);
    }
    protected final boolean compareColors(int rgb1, int rgb2) {
        return(leftMap.mapColor(rgb1) == rightMap.mapColor(rgb2));
    }

    /**
     * Interface to map colors during the comparision.
     */
    public static interface ColorMap {
        /**
         * Maps one color into another.
         * @param rgb an original color.
         * @return a converted color.
         */
        public int mapColor(int rgb);
    }

    /**
     * Turns <code>foreground</code> color to white, other - to black.
     */
    public static class ForegroundColorMap implements ColorMap {
        int foreground;
        /**
         * Constructs a ColorImageComparator$ForegroundColorMap object.
         * @param foreground Foreground color.
         */
        public ForegroundColorMap(int foreground) {
            this.foreground = foreground;
        }
        public int mapColor(int rgb) {
            return((rgb == foreground) ? 0xffffff : 0);
        }
    }

    /**
     * Turns <code>background</code> color to black, left others unchanged.
     */
    public static class BackgroundColorMap implements ColorMap {
        int background;
        /**
         * Constructs a ColorImageComparator$BackgroundColorMap object.
         * @param background Background color.
         */
        public BackgroundColorMap(int background) {
            this.background = background;
        }
        public int mapColor(int rgb) {
            return((rgb == background) ? 0 : rgb);
        }
    }

}
