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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.openide.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import junit.framework.*;
import java.lang.ref.*;
import java.util.*;

/**
 *
 * @author Radim Kubacki
 */
public class IconManagerTest extends TestCase {
    
    public IconManagerTest (String testName) {
        super (testName);
    }

    public void testMergeImages() throws Exception {
        // test if merged image preserves alpha (#90862)
        BufferedImage img1 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
//        System.out.println("img1 transparency "+img1.getTransparency());
        java.awt.Graphics2D g = img1.createGraphics();
        Color c = new Color(255, 255, 255, 128);
        g.setColor(c);
        g.fillRect(0, 0, 16, 16);
        g.dispose();
        
        BufferedImage img2 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
//        System.out.println("img2 transparency "+img2.getTransparency());
        g = img2.createGraphics();
        c = new Color(255, 255, 255);
        g.setColor(c);
        g.fillRect(0, 0, 2, 2);
        g.dispose();
        
        Image mergedImg = IconManager.mergeImages(img1, img2, 0, 0);
        if (!(mergedImg instanceof BufferedImage)) {
            fail("It is assumed that mergeImages returns BufferedImage. Need to update test");
        }
                
        BufferedImage merged = (BufferedImage)mergedImg;
//        System.out.println("pixels " + Integer.toHexString(merged.getRGB(10, 10)) +", "+ Integer.toHexString(merged.getRGB(0, 0)));
        assertNotSame("transparency has to be kept for pixel <1,1>", merged.getRGB(10, 10), merged.getRGB(0, 0));
    }
    
    public void testMergeBitmaskImages() throws Exception {
        // test if two bitmask images are merged to bitmask again to avoid use of alpha channel
        BufferedImage img1 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
//        System.out.println("img1 transparency "+img1.getTransparency());
        java.awt.Graphics2D g = img1.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 16, 16);
        g.dispose();
        
        BufferedImage img2 = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
//        System.out.println("img2 transparency "+img2.getTransparency());
        g = img2.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 2, 2);
        g.dispose();
        
        Image mergedImg = IconManager.mergeImages(img1, img2, 0, 0);
        if (!(mergedImg instanceof BufferedImage)) {
            fail("It is assumed that mergeImages returns BufferedImage. Need to update test");
        }
                
        BufferedImage merged = (BufferedImage)mergedImg;
        assertEquals("Should create bitmask image", Transparency.BITMASK, merged.getTransparency());
        assertEquals(Color.RED, new Color(merged.getRGB(1, 1)));
        assertEquals(Color.BLUE, new Color(merged.getRGB(10, 10)));
    }
    
}
