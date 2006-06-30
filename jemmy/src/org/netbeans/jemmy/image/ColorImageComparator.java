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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
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
