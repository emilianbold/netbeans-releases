/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.mobility.svgcore.export;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.WritableRaster;

public final class Quantizer {
    private static final int MAXCOLORS         = 255; 
    private static final int TRANSPARENT_COLOR = 255;
    private static final int HSIZE             = 32768;

    private static final class ColorCube {
        private int lower;
        private int upper;
        private int count;
        private int level;
        private int rmin;
        private int rmax;
        private int gmin;
        private int gmax;
        private int bmin;
        private int bmax;

        ColorCube() {
            count = 0;
        }
    }
    
    private final int[]           m_pixels32;
    private final int             m_width;
    private final int             m_height;
    private final int[]           m_hist;
    private       int[]           m_histPtr;
    private       ColorCube[]     m_list;
    private       IndexColorModel m_cm;

    public Quantizer(int[] pixels, int width, int height) {
        m_pixels32 = pixels;
        m_width    = width;
        m_height   = height;

        m_hist = new int[HSIZE];
        for (int i = 0; i < width * height; i++) {
            m_hist[rgb(m_pixels32[i])]++;
        }
    }

    public int getColorCount() {
        int count = 0;
        
        for (int i = 0; i < HSIZE; i++) {
            if (m_hist[i] > 0) {
                count++;
            }
        }
        
        return count;
    }

    public Color getModalColor() {
        int max = 0;
        int c   = 0;
        
        for (int i = 0; i < HSIZE; i++) {
            if (m_hist[i] > max) {
                max = m_hist[i];
                c = i;
            }
        }
        return new Color(red(c), green(c), blue(c));
    }

    public BufferedImage toImage() {
        int lr, lg, lb;
        int i, median, color;
        int count;
        int k, level, ncubes, splitpos;
        int longdim = 0; 
        ColorCube cube, cubeA, cubeB;

        m_list = new ColorCube[MAXCOLORS];
        m_histPtr = new int[HSIZE];
        ncubes = 0;
        cube = new ColorCube();
        for (i = 0, color = 0; i <= HSIZE - 1; i++) {
            if (m_hist[i] != 0) {
                m_histPtr[color++] = i;
                cube.count = cube.count + m_hist[i];
            }
        }
        cube.lower = 0;
        cube.upper = color - 1;
        cube.level = 0;
        reduce(cube);
        m_list[ncubes++] = cube;

        while (ncubes < MAXCOLORS) {
            level = 255;
            splitpos = -1;
            for (k = 0; k <= ncubes - 1; k++) {
                if (m_list[k].lower == m_list[k].upper)
                    ;  // single color; cannot be split
                else if (m_list[k].level < level) {
                    level = m_list[k].level;
                    splitpos = k;
                }
            }
            if (splitpos == -1)
                break;

            cube = m_list[splitpos];
            lr = cube.rmax - cube.rmin;
            lg = cube.gmax - cube.gmin;
            lb = cube.bmax - cube.bmin;
            if (lr >= lg && lr >= lb) longdim = 0;
            if (lg >= lr && lg >= lb) longdim = 1;
            if (lb >= lr && lb >= lg) longdim = 2;

            changeColorOrder(m_histPtr, cube.lower, cube.upper, longdim);
            quickSort(m_histPtr, cube.lower, cube.upper);
            resetColorOrder(m_histPtr, cube.lower, cube.upper, longdim);

            count = 0;
            for (i = cube.lower; i <= cube.upper - 1; i++) {
                if (count >= cube.count / 2) break;
                color = m_histPtr[i];
                count = count + m_hist[color];
            }
            median = i;

            cubeA = new ColorCube();
            cubeA.lower = cube.lower;
            cubeA.upper = median - 1;
            cubeA.count = count;
            cubeA.level = cube.level + 1;
            reduce(cubeA);
            m_list[splitpos] = cubeA;

            cubeB = new ColorCube();
            cubeB.lower = median;
            cubeB.upper = cube.upper;
            cubeB.count = cube.count - count;
            cubeB.level = cube.level + 1;
            reduce(cubeB);
            m_list[ncubes++] = cubeB;
        }

        invertMap(m_hist, ncubes);
        return makeBufferedImage();
    }

    private void reduce(ColorCube cube) {
        int r, g, b;
        int color;
        int rmin, rmax, gmin, gmax, bmin, bmax;

        rmin = 255;
        rmax = 0;
        gmin = 255;
        gmax = 0;
        bmin = 255;
        bmax = 0;
        for (int i = cube.lower; i <= cube.upper; i++) {
            color = m_histPtr[i];
            r = red(color);
            g = green(color);
            b = blue(color);
            if (r > rmax) rmax = r;
            if (r < rmin) rmin = r;
            if (g > gmax) gmax = g;
            if (g < gmin) gmin = g;
            if (b > bmax) bmax = b;
            if (b < bmin) bmin = b;
        }
        cube.rmin = rmin;
        cube.rmax = rmax;
        cube.gmin = gmin;
        cube.gmax = gmax;
        cube.gmin = gmin;
        cube.gmax = gmax;
    }

    private void invertMap(int[] hist, int ncubes) {
        ColorCube cube;
        int r, g, b;
        float rsum, gsum, bsum;
        int color;
        byte[] rLUT = new byte[MAXCOLORS+1];
        byte[] gLUT = new byte[MAXCOLORS+1];
        byte[] bLUT = new byte[MAXCOLORS+1];

        for (int k = 0; k <= ncubes - 1; k++) {
            cube = m_list[k];
            rsum = gsum = bsum = (float) 0.0;
            for (int i = cube.lower; i <= cube.upper; i++) {
                color = m_histPtr[i];
                r = red(color);
                rsum += (float) r * (float) hist[color];
                g = green(color);
                gsum += (float) g * (float) hist[color];
                b = blue(color);
                bsum += (float) b * (float) hist[color];
            }

            r = (int) (rsum / (float) cube.count);
            g = (int) (gsum / (float) cube.count);
            b = (int) (bsum / (float) cube.count);
            if (r == 248 && g == 248 && b == 248)
                r = g = b = 255;
            rLUT[k] = (byte) r;
            gLUT[k] = (byte) g;
            bLUT[k] = (byte) b;
        }
        m_cm = new IndexColorModel(8, MAXCOLORS+1, rLUT, gLUT, bLUT, TRANSPARENT_COLOR);

        for (int k = 0; k <= ncubes - 1; k++) {
            cube = m_list[k];
            for (int i = cube.lower; i <= cube.upper; i++) {
                color = m_histPtr[i];
                hist[color] = k;
            }
        }
    }

    private void changeColorOrder(int[] a, int lo, int hi, int longDim) {
        int c, r, g, b;
        switch (longDim) {
            case 0:
                for (int i = lo; i <= hi; i++) {
                    c = a[i];
                    r = c & 31;
                    a[i] = (r << 10) | (c >> 5);
                }
                break;
            case 1:
                for (int i = lo; i <= hi; i++) {
                    c = a[i];
                    r = c & 31;
                    g = (c >> 5) & 31;
                    b = c >> 10;
                    a[i] = (g << 10) | (b << 5) | r;
                }
                break;
            case 2:
                break;
        }
    }


    void resetColorOrder(int[] a, int lo, int hi, int longDim) {
        int c, r, g, b;
        switch (longDim) {
            case 0:
                for (int i = lo; i <= hi; i++) {
                    c = a[i];
                    r = c >> 10;
                    a[i] = ((c & 1023) << 5) | r;
                }
                break;
            case 1:
                for (int i = lo; i <= hi; i++) {
                    c = a[i];
                    r = c & 31;
                    g = c >> 10;
                    b = (c >> 5) & 31;
                    a[i] = (b << 10) | (g << 5) | r;
                }
                break;
            case 2:
                break;
        }
    }

    void quickSort(int a[], int lo0, int hi0) {
        int lo = lo0;
        int hi = hi0;
        int mid, t;

        if (hi0 > lo0) {
            mid = a[(lo0 + hi0) / 2];
            while (lo <= hi) {
                while ((lo < hi0) && (a[lo] < mid))
                    ++lo;
                while ((hi > lo0) && (a[hi] > mid))
                    --hi;
                if (lo <= hi) {
                    t = a[lo];
                    a[lo] = a[hi];
                    a[hi] = t;
                    ++lo;
                    --hi;
                }
            }
            if (lo0 < hi)
                quickSort(a, lo0, hi);
            if (lo < hi0)
                quickSort(a, lo, hi0);

        }
    }

    byte [] toByteArray() {
        byte[] pixels8;
        int color16;
        pixels8 = new byte[m_width * m_height];
        for (int i = 0; i < m_width * m_height; i++) {
            color16 = rgb(m_pixels32[i]);
            pixels8[i] = (byte) m_hist[color16];
        }
        return pixels8;
    }
    
    BufferedImage makeBufferedImage() {
        byte [] pixels8 = toByteArray();    
        BufferedImage bufferedImage = new BufferedImage(m_cm, 
            m_cm.createCompatibleWritableRaster(m_width, m_height), false, null);        
        
        WritableRaster raster = bufferedImage.getRaster();
        int[] pixelArray = new int[1];
        int i = 0;
        for (int x = 0; x < m_width; x++) {
            for (int y = 0; y < m_height; y++) {
                int pixel = m_pixels32[i];
                if ( (pixel >>> 24) > 127) {
                    pixelArray[0] = pixels8[i++];
                } else {
                    pixelArray[0] = 255;
                    i++;
                }
                raster.setPixel(x, y, pixelArray);
            }
        }
        return bufferedImage;        
    }
    
    Image makeImage() {
        byte [] pixels8 = toByteArray();    
        Image img8 = Toolkit.getDefaultToolkit().createImage(
                new MemoryImageSource(m_width, m_height,
                        m_cm, pixels8, 0, m_width));
        return img8;
    }

    private final int rgb(int c) {
        int r = (c & 0xf80000) >> 19;
        int g = (c & 0xf800) >> 6;
        int b = (c & 0xf8) << 7;
        return b | g | r;
    }

    private final int red(int x) {
        return (x & 31) << 3;
    }

    private final int green(int x) {
        return (x >> 2) & 0xf8;
    }

    private final int blue(int x) {
        return (x >> 7) & 0xf8;
    }    
}
