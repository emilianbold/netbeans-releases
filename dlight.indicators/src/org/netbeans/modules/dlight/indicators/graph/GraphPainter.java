/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.indicators.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import org.netbeans.modules.dlight.indicators.graph.Graph.AxisOrientation;

/**
 * A delegate that is responsible for painting,
 * but not for other interaction with Swing stuff.
 *
 * The idea is to separate Swing interactions (mouse listening,
 * overriding setSize to rescale properly, etc)
 * with logic that is relevant to graph painting only.
 *
 * @author Vladimir Kvashin
 */
//package-local
class GraphPainter {
    private static final int PIXELS_PER_SAMPLE = 5;
    private static final Stroke BALL_STROKE = new BasicStroke(1.0f);
    private static final Stroke LINE_STROKE = new BasicStroke(3.0f);

    private Color gridColor = GraphColors.GRID_COLOR;
    private Color backgroundTopColor = GraphColors.GRADIENT_TOP_COLOR;
    private Color backgroundBottomColor = GraphColors.GRADIENT_BOTTOM_COLOR;

    private final GraphDescriptor[] descriptors;
    private final int seriesCount;

    private CyclicArray<int[]> data;
    private final Object dataLock = new Object();
    private boolean paintAll = true;

    private int scale;

    private int width;
    private int height;

    private int paintedDataCount = 0;
    private int arrivedDataCount = 0;
    private int dataWindowScroll = 0;

//    private BufferedImage cachedImage;

    private static final boolean TRACE = Boolean.getBoolean("PercentageGraph.trace");

    public GraphPainter(int  scale, GraphDescriptor[] descriptors, int width, int height) {
        //setBackground(new Color(128, 128, 128, 0)); // transparent
        this.scale = scale;
        this.descriptors = descriptors;
        this.width = width;
        this.height = height;
        seriesCount = descriptors.length;
        data = new CyclicArray<int[]>(width);
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

    public GraphPainter(int scale, GraphDescriptor... descriptors) {
        this(scale, descriptors, 32, 32);
    }

    public void setGridColor(Color color) {
        this.gridColor = color;
    }

    public void setBackgroundColor(Color color) {
        this.backgroundBottomColor = color;
    }

    public void setSize(int width, int height) {
        synchronized (dataLock) {
            data.setCapacity(width / PIXELS_PER_SAMPLE);
            paintAll = true;
            this.height = height;
            this.width = width;
            invalidate();
//            initCacheImage();
        }
        if (TRACE) System.err.printf("PercentareGraph.setSize %d %d\n", width, height);
    }

    public void setUpperLimit(int newScale) {
        if (newScale != scale) {
            this.scale = newScale;
            invalidate();
        }
    }

    public int getUpperLimit() {
        return scale;
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
        return backgroundTopColor;
    }

    public void addData(int... newData) {
        if (getWidth() == 0) {
            return;
        }
        synchronized (dataLock) {
            if (newData.length != seriesCount) {
                throw new IllegalArgumentException(
                        String.format("New data size %d differs from series count %d", //NOI18N
                        newData.length, seriesCount));
            }
            data.setCapacity(getWidth() / PIXELS_PER_SAMPLE);

            if (data.add(newData.clone())) {
                dataWindowScroll++;
            }
            arrivedDataCount++;
            if (TRACE) System.err.printf("addData; size=%d capacity=%d width=%d dataWindowScroll=%d arrivedDataCount=%d paintedDataCount=%d\n",
                    data.size(), data.capacity(), getWidth(), dataWindowScroll,  arrivedDataCount, paintedDataCount);
        }
     }

    public void draw(Graphics g, int x, int y) {
        drawImpl(g, x, y);
//        Graphics g2 = cachedImage.getGraphics();
//        updateGraphics(g2, 0, 0);
//        g2.dispose();
//        g.drawImage(cachedImage, x, y, null);
    }

    private void drawImpl(Graphics g, int x, int y) {
        int w = getWidth();
        int h = getHeight();
        synchronized (dataLock) {
            paintGradient(g, x, y, w, h);
            paintGrid(g, x, y, w, h);
            paintGraph(g, x, y, w, h);
        }
    }

    public GraphDescriptor[] getDescriptors() {
        return descriptors.clone();
    }

    public int[] getLastData() {
        int[] last;
        synchronized (dataLock) {
            if (data.size() == 0) {
                last = new int[descriptors.length];
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

    private void paintGradient(Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D)g;

        GradientPaint gradient = new GradientPaint(0, 0, backgroundTopColor, 0, height, backgroundBottomColor);
        g2.setPaint(gradient);
        g2.fillRect(x, y, w, h);
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
     * Paints the entire graph.
     * Should be called under synchronized (dataLock)
     */
    private void paintGraph(Graphics g, int left, int top, int width, int height) {
        Graphics2D g2 = ((Graphics2D)g);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Stroke oldStroke = g2.getStroke();

        if (TRACE) System.err.printf("\npaintGraph: %d %d %d %d data:\n%s\n", left, top, width, height, data);
        if (height < 1) {
            return;
        }
        int sampleLimit = Math.min(width / PIXELS_PER_SAMPLE, data.size()) - 1;
        if (0 < sampleLimit) {
            for (int ser = 0; ser < seriesCount; ++ser) {
                g2.setStroke(LINE_STROKE);
                g2.setColor(descriptors[ser].getColor());
                int lastx = 0;
                int lasty = 0;
                for (int sample = 1; sample < sampleLimit; ++sample) {
                    int prevValue = data.get(sample - 1)[ser] * height / scale;
                    int currValue = data.get(sample)[ser] * height / scale;
                    g.drawLine(left + PIXELS_PER_SAMPLE * (sample - 1) , top + height - 1 - prevValue,
                               lastx = left + PIXELS_PER_SAMPLE * sample, lasty = top + height - 1 - currValue);
                }
                g2.setStroke(BALL_STROKE);
                g2.setColor(Color.WHITE);
                g2.fillOval(lastx - 2, lasty - 2, 4, 4);
                g2.setColor(descriptors[ser].getColor());
                g2.drawOval(lastx - 2, lasty - 2, 4, 4);
            }
        }
        paintedDataCount = arrivedDataCount;
        g2.setStroke(oldStroke);
    }

    public void drawAxis(Graphics g, AxisOrientation orientation, Dimension size) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(g2.getFont().deriveFont(10f));
        g2.setColor(GraphColors.TEXT_COLOR);
        g2.drawString(Integer.toString(getUpperLimit()), 0, 10);
        g2.drawString(Integer.toString(0), 0, (int)size.getHeight());
    }

    /** for tracing/debugging purposes */
    private static String toString(int[] a) {
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
