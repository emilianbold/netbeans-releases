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
     */
    protected boolean compareColors(int rgb1, int rgb2) {
        return(rgb1 == rgb2);
    }
}
