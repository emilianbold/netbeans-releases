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

import java.io.IOException;

import org.netbeans.jemmy.JemmyException;

/**
 * Allowes compares images in memory to ones stored in files 
 * and compare such images one with another.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class FileImageComparator {

    ImageLoader loader;
    ImageComparator comparator;

    /**
     * @param comparator - ImageComparator to be used for image comparision.
     * @param loader - ImageLoader to be used for image loading.
     */
    public FileImageComparator(ImageComparator comparator, ImageLoader loader) {
        this.loader = loader;
        this.comparator = comparator;
    }

    /**
     * Compares an image with one stored in file.
     * Comparision is performed by ImageComparator passed into constructor.
     * Image is loaded by ImageLoader passed into constructor.
     */
    public boolean compare(BufferedImage image, String fileName) {
        try {
            return(comparator.compare(image, loader.load(fileName)));
        } catch(IOException e) {
            throw(new JemmyException("IOException during image loading", e));
        }
    }

    /**
     * Compares two image stored in files..
     * Comparision is performed by ImageComparator passed into constructor.
     * Images are loaded by ImageLoader passed into constructor.
     */
    public boolean compare(String fileName1, String fileName2) {
        try {
            return(comparator.compare(loader.load(fileName1), 
                                      loader.load(fileName2)));
        } catch(IOException e) {
            throw(new JemmyException("IOException during image loading", e));
        }
    }
}
