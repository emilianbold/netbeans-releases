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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.netbeans.jemmy.util.PNGEncoder;

/**
 * Allowes to process PNF image format.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class PNGImageSaver implements ImageSaver {

    /**
     * Saves an image into a PNG image file.
     */
    public void save(BufferedImage image, String fileName) throws IOException{
        new PNGEncoder(new BufferedOutputStream(new FileOutputStream(fileName)),
                       PNGEncoder.COLOR_MODE).
            encode(image);
    }
}
