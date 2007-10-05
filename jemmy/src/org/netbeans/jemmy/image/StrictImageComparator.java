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
