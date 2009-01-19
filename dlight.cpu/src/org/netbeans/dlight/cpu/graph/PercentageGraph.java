/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.dlight.cpu.graph;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Displays a percentage graph
 * @author Vladimir Kvashin
 */
public class PercentageGraph {

    private boolean optimize = Boolean.getBoolean("percentage.graph.optimize");

    public static class Descriptor {
        public final Color color;
        public final String description;

        public Descriptor(Color color, String description) {
            this.color = color;
            this.description = description;
        }
    }

    private Color gridColor = Color.LIGHT_GRAY;
    private Color backgroundColor = Color.WHITE;

    private final Descriptor[] descriptors;
    private final int seriesCount;

    private CyclicArray<short[]> data;
    private final Object dataLock = new Object();
    private boolean paintAll = true;

    private int width;
    private int height;

    private int paintedDataCount = 0;
    private int arrivedDataCount = 0;
    private int dataWindowScroll = 0;

//    private BufferedImage cachedImage;

    private static final boolean TRACE = Boolean.getBoolean("PercentageGraph.trace");

    public PercentageGraph(Descriptor[] descriptors, int width, int height) {
        //setBackground(new Color(128, 128, 128, 0)); // transparent
        this.descriptors = descriptors;
        this.width = width;
        this.height = height;
        seriesCount = descriptors.length;
        data = new CyclicArray<short[]>(width);
//        initCacheImage();
    }

//    private void initCacheImage() {
//        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        if (cachedImage != null) {
//            Graphics g = newImage.getGraphics();
//            g.drawImage(cachedImage, 0, 0, null);
//            g.dispose();
//        }
//        cachedImage = newImage;
//    }

    public PercentageGraph(Descriptor... descriptors) {
        this(descriptors, 32, 32);
    }

    public void setGridColor(Color color) {
        this.gridColor = color;
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
    }

    public void setSize(int width, int height) {
        synchronized (dataLock) {
            data.setCapacity(width);
            paintAll = true;
            this.height = height;
            this.width = width;
            invalidate();
//            initCacheImage();
        }
        if (TRACE) System.err.printf("PercentareGraph.setSize %d %d\n", width, height);
    }

    public void invalidate() {
        synchronized (dataLock) {
            paintAll = true;
            paintedDataCount = dataWindowScroll;
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void addData(short... newData) {
        if (getWidth() == 0) {
            return;
        }
        synchronized (dataLock) {
            if (newData.length != seriesCount) {
                throw new IllegalArgumentException(
                        String.format("New data size %d differs from series count %d", //NOI18N
                        newData.length, seriesCount));
            }
            data.setCapacity(getWidth());

            if (data.add(newData.clone())) {
                dataWindowScroll++;
            }
            arrivedDataCount++;
            if (TRACE) System.err.printf("addData; size=%d capacity=%d width=%d dataWindowScroll=%d arrivedDataCount=%d paintedDataCount=%d\n",
                    data.size(), data.capacity(), getWidth(), dataWindowScroll,  arrivedDataCount, paintedDataCount);
        }
     }

    public void draw(Graphics g, int x, int y) {
        drawImpl(g, x, x);
//        Graphics g2 = cachedImage.getGraphics();
//        updateGraphics(g2, 0, 0);
//        g2.dispose();
//        g.drawImage(cachedImage, x, y, null);
    }

    private void drawImpl(Graphics g, int x, int y) {
        int w = getWidth();
        int h = getHeight();
        synchronized (dataLock) {
            if (paintAll) {
                if (optimize) {
                    paintAll = false;
                }
                g.setColor(getBackgroundColor());
                g.fillRect(x, y, w, h);
                paintGraph(g, x, y, w, h);
                paintGrid(g, x, y, w, h);
            } else {
                updateGraph(g, x, y, w, h);
            }
        }
    }

    public Descriptor[] getDescriptors() {
        return descriptors.clone();
    }

    public short[] getLastData() {
        short[] last;
        synchronized (dataLock) {
            if (data.size() == 0) {
                last = new short[descriptors.length];
                for (int i = 0; i < last.length; i++) {
                    last[i] = 0;
                }
            } else {
                last = data.get(data.size()-1);
            }
        }
        return last;
    }

    /** gets grid size in pixels */
    private int getGridSize() {
        return 8;
    }

    /** Should be called under synchronized (dataLock) */
    private int scrolled() {
        if (paintedDataCount > data.size()) {
            return  paintedDataCount - data.size();
        } else {
            return 0;
        }
    }

    /**
     * Paints the entire grid.
     * Should be called under synchronized (dataLock)
     */
    private void paintGrid(Graphics g, int x, int y, int w, int h) {
        int gridSize = getGridSize();
        g.setColor(gridColor);
        int scrolled = scrolled();
        int dx = (scrolled > 0) ? gridSize - scrolled%gridSize : 0;
        for (int gridX = x+dx; gridX < x+w; gridX += gridSize) {
            g.drawLine(gridX, y, gridX, y+h-1);
        }
        for (int gridY = y+h-1; gridY >= 0; gridY -= gridSize) {
            g.drawLine(x, gridY, x+w-1, gridY);
        }
    }


    /**
     * Updates graph.
     * Should be called under synchronized (dataLock)
     */
    private void updateGraph(Graphics g, int left, int top, int width, int height) {
        if (height < 1) {
            return;
        }
        if (data.size() == 0) {
            return;
        }
        if (arrivedDataCount > paintedDataCount) {

            int scrolled = scrolled();
            if (scrolled > 0) {
                int w2scroll = width - 1;
                g.copyArea(left+1, top, w2scroll, height, -(arrivedDataCount - paintedDataCount), 0);
                if (TRACE) System.err.printf("copyArea %d %d %d %d clip=%s\n", left+1, top, w2scroll, height, g.getClip());
            }

            int gridSize = getGridSize();

            int paintWidth = Math.min(arrivedDataCount - paintedDataCount, data.size());
            for (int dx = paintWidth; dx > 0; dx--) {
                int x = left + Math.min(width, data.size()) - dx;
                if (TRACE) System.err.printf("updateGraph x=%d scrolled=%d\n", x, scrolled);
                if ((x+scrolled)%gridSize == 0) {
                    g.setColor(gridColor);
                    g.drawLine(x, top, x, top+height-1);
                    paintedDataCount++;
                } else {
                    short[] currData = data.get(x - left);
                    paintVLine(currData, x, top, height, g);
                    paintedDataCount++;
                }
            }
        }
    }

    /**
     * Paints the entire graph.
     * Should be called under synchronized (dataLock)
     */
    private void paintGraph(Graphics g, int left, int top, int width, int height) {
        if (TRACE) System.err.printf("\npaintGraph: %d %d %d %d data:\n%s\n", left, top, width, height, data);
        if (height < 1) {
            return;
        }
        int xLimit = left + Math.min(width, data.size()) - 1;        
        for (int x = left; x < xLimit; x++) {
            short[] currData = data.get(x - left);
            paintVLine(currData, x, top, height, g);
            //paintedDataCount++;
        }
        paintedDataCount = arrivedDataCount;
    }

    private void paintVLine(short[] currData, int x, int top, int height, Graphics g) {
        if (TRACE) {
            System.err.printf("\tx=%d currData=%s\n", x, toString(currData));
        }
        assert currData.length == seriesCount;
        // painting graph
        int yEnd = top + height - 1;
        for (int ser = 0; ser < seriesCount; ser++) {
            short value = currData[ser];
            value = (short) (value * height / 100);
            Color color = descriptors[ser].color;
            int yStart = yEnd - value;
            if (TRACE) {
                System.err.printf("\t\tser=%d value=%x yStart=%d yEnd=%d color=%s\n", ser, value, yStart, yEnd, color);
            }
            g.setColor(color);
            g.drawLine(x, yStart, x, yEnd);
            yEnd = yStart - 1;
        }
        if (top < yEnd-1) {
            g.setColor(getBackgroundColor());
            g.drawLine(x, top, x, yEnd-1);
        }
        // painting grid
        int gridSize = getGridSize();
        g.setColor(gridColor);
        for (int gridY = top+height-1; gridY >= 0; gridY -= gridSize) {
            g.drawLine(x, gridY, x, gridY);
        }        
    }


    /** for tracing/debugging purposes */
    private static String toString(short[] a) {
        if (a==null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < a.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(Integer.toString(a[i]));
        }
        sb.append(']');
        return sb.toString();
    }

}
