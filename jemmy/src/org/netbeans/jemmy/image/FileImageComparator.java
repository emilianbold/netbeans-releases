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
     * Constructs a FileImageComparator object.
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
     * @param image an image to compare.
     * @param fileName a file containing an image to compare.
     * @return true if images match each other.
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
     * @param fileName1 a file containing an image to compare.
     * @param fileName2 a file containing an image to compare.
     * @return true if images match each other.
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
