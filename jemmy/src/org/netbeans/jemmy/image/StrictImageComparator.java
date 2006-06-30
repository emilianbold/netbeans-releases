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

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;

import java.awt.image.BufferedImage;

/**
 * Compares two images strictly (i.e. all the pixel colors should match).
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class StrictImageComparator implements ImageComparator {
    /**
     * Checks images sizes and pixels.
     * Compares one pixel after another untill one will be different.
     * @param image1 an image to compare.
     * @param image2 an image to compare.
     * @return True if all the pixels match, false otherwise.
     */
    public boolean compare(BufferedImage image1, BufferedImage image2) {
        if(image1.getWidth()  != image2.getWidth() ||
           image1.getHeight() != image2.getHeight()) {
            return(false);
        }
        for(int x = 0; x < image1.getWidth(); x++) {
            for(int y = 0; y < image1.getHeight(); y++) {
                if(!compareColors(image1.getRGB(x, y), image2.getRGB(x, y))) {
                    return(false);
                }
            }
        }
        return(true);
    }
    /**
     * Could be used to override the way of comparing colors.
     * @param rgb1 a color to compare.
     * @param rgb2 a color to compare.
     * @return true if colors are equal.
     */
    protected boolean compareColors(int rgb1, int rgb2) {
        return(rgb1 == rgb2);
    }
}
