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
