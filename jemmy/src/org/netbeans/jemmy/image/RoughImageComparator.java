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

import java.awt.image.BufferedImage;

/**
 * Compares two images roughly (i.e. not all of the pixel colors should match).
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class RoughImageComparator implements ImageComparator {
    double roughness = .0;

    /**
     * Creates a comparator with <code>roughness</code> allowed roughness.
     * @param roughness Allowed comparision roughness.
     */
    public RoughImageComparator(double roughness) {
        this.roughness = roughness;
    }

    /**
     * Compares two images with allowed roughness.
     * @param image1 an image to compare.
     * @param image2 an image to compare.
     * @return true if images have the same sizes and 
     * number of unmatching pixels less or equal to
     * <code>image1.getWidth() * image1.getHeight() * roughness<code>
     */
    public boolean compare(BufferedImage image1, BufferedImage image2) {
        if(image1.getWidth()  != image2.getWidth() ||
           image1.getHeight() != image2.getHeight()) {
            return(false);
        }
        double maxRoughPixels = (double)(image1.getWidth() * image1.getHeight()) * roughness;
        int errorCount = 0;
        for(int x = 0; x < image1.getWidth(); x++) {
            for(int y = 0; y < image1.getHeight(); y++) {
                if(image1.getRGB(x, y) != image2.getRGB(x, y)) {
                    errorCount++;
                    if(errorCount > maxRoughPixels) {
                        return(false);
                    }
                }
            }
        }
        return(true);
    }
}
