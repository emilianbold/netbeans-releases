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

import java.awt.Point;

import java.awt.image.BufferedImage;

/**
 * Interface for all classes performing image lookup.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public interface ImageFinder {

    /**
     * Should return location if image lays inside an image represented by this object.
     * @param image an image to search.
     * @param index an ordinal image location index. If equal to 1, for example,
     * second appropriate location will be found.
     * @return Image location coordinates if image was found, null otherwise.
     */
    public Point findImage(BufferedImage image, int index);
}
