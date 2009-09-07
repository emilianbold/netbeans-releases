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

import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.dlight.util.DLightMath;

/**
 * A delegate that is responsible for painting,
 * but not for other interaction with Swing stuff.
 *
 * The idea is to separate Swing interactions (mouse listening,
 * overriding setSize to rescale properly, etc)
 * with logic that is relevant to graph painting only.
 *
 * @author Vladimir Kvashin
 * @author Alexey Vladykin
 */
//package-local
class GraphPainter {
    private static final Stroke BALL_STROKE = new BasicStroke(1.0f);
    private static final Stroke LINE_STROKE = new BasicStroke(GraphConfig.LINE_WIDTH);

    private final List<TimeSeriesDescriptor> descriptors;
    private final int seriesCount;

    private List<float[]> data;
    private final Object dataLock = new Object();

//    private BufferedImage cachedImage;

    public GraphPainter(List<TimeSeriesDescriptor> descriptors) {
        this.descriptors = descriptors;
        seriesCount = descriptors.size();
        data = new ArrayList<float[]>(1000);
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

    public void addData(float... newData) {
        synchronized (dataLock) {
            if (newData.length != seriesCount) {
                throw new IllegalArgumentException(
                        String.format("New data size %d differs from series count %d", //NOI18N
                        newData.length, seriesCount));
            }
            data.add(newData.clone());
        }
     }

    public int getDataSize() {
        return data.size();
    }

    public int calculateUpperLimit(float... data) {
        float absLimit = 0;
        float relLimit = 0;
        for (int i = 0; i < data.length; ++i) {
            float value = data[i];
            TimeSeriesDescriptor descriptor = descriptors.get(i);
            switch (descriptor.getKind()) {
                case ABS_SURFACE:
                case LINE:
                    absLimit = Math.max(absLimit, value);
                    break;
                case REL_SURFACE:
                    relLimit += value;
                    break;
            }
        }
        return (int)Math.max(absLimit, relLimit);
    }

//    public GraphDescriptor[] getDescriptors() {
//        return descriptors.clone();
//    }

//    public int[] getLastData() {
//        int[] last;
//        synchronized (dataLock) {
//            if (data.size() == 0) {
//                last = new int[descriptors.length];
//                for (int i = 0; i < last.length; i++) {
//                    last[i] = 0;
//                }
//            } else {
//                last = data.get(data.size()-1);
//            }
//        }
//        return last;
//    }

// graph painting //////////////////////////////////////////////////////////////

    /**
     * Paints graph including background gradient and grid.
     *
     * @param g  graphics
     * @param x  left coordinate of drawing area
     * @param y  upper coordinate of drawing area
     * @param w  width of drawing area
     * @param h  height of drawing area
     * @param ticks  whether to draw ticks on graph
     */
    public void paint(Graphics g, int scale, List<AxisMark> yMarks, int viewportStart, int viewportEnd, List<AxisMark> xMarks, int filterStart, int filterEnd, int x, int y, int w, int h, boolean ticks) {
        synchronized (dataLock) {
            paintGradient(g, x, y, w, h);
            if (GraphConfig.GRID_SIZE < w && GraphConfig.GRID_SIZE < h) {
                paintGrid(g, x, y, w, h, xMarks, yMarks, ticks);
                paintGraph(g, scale, viewportStart, viewportEnd, x, y, w, h);
                dimInactiveRegions(g, viewportStart, viewportEnd, filterStart, filterEnd, x, y, w, h);
            }
        }
    }

    /**
     * Paints background gradient.
     */
    private static void paintGradient(Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D)g;
        Paint oldPaint = g2.getPaint();
        GradientPaint gradient = new GradientPaint(0, 0, GraphConfig.GRADIENT_TOP_COLOR, 0, h, GraphConfig.GRADIENT_BOTTOM_COLOR);
        g2.setPaint(gradient);
        g2.fillRect(x, y, w, h);
        g2.setPaint(oldPaint);
    }

    /**
     * Paints background grid.
     */
    private static void paintGrid(Graphics g, int x, int y, int w, int h, List<AxisMark> xMarks, List<AxisMark> yMarks, boolean ticks) {
        // vertical lines
        for (AxisMark xMark : xMarks) {
            g.setColor(adjustAlpha(GraphConfig.GRID_COLOR, xMark.getOpacity()));
            g.drawLine(x + xMark.getPosition(), y, x + xMark.getPosition(), y + h - 1);
        }
        if (ticks) {
            for (AxisMark xMark : xMarks) {
                g.setColor(adjustAlpha(GraphConfig.BORDER_COLOR, xMark.getOpacity()));
                g.drawLine(x + xMark.getPosition(), y + h - 5, x + xMark.getPosition(), y + h - 1);
            }
        }
        // horizontal lines
        for (AxisMark yMark : yMarks) {
            g.setColor(adjustAlpha(GraphConfig.GRID_COLOR, yMark.getOpacity()));
            g.drawLine(x, y + h - 1 - yMark.getPosition(), x + w - 1, y + h - 1 - yMark.getPosition());
        }
        if (ticks) {
            for (AxisMark yMark : yMarks) {
                g.setColor(adjustAlpha(GraphConfig.BORDER_COLOR, yMark.getOpacity()));
                g.drawLine(x, y + h - 1 - yMark.getPosition(), x + 5, y + h - 1 - yMark.getPosition());
            }
        }
    }

    /**
     * Paints the entire graph.
     * Should be called under synchronized (dataLock)
     */
    private void paintGraph(Graphics g, int scale, int viewportStart, int viewportEnd, int x, int y, int w, int h) {
        Graphics2D g2 = ((Graphics2D)g);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Stroke oldStroke = g2.getStroke();

        int sampleCount = Math.min(viewportEnd + 1, data.size()) - viewportStart;
        if (0 < sampleCount) {
            int[] xx = new int[sampleCount + 2];
            int[] yy = new int[sampleCount + 2];
            int effectiveHeight = (int)(h - 2 - GraphConfig.FONT_SIZE / 2);
            for (int ser = 0; ser < seriesCount; ++ser) {
                int lastx = 0;
                int lasty = 0;
                for (int i = 0; i < sampleCount; ++i) {
                    float[] values = data.get(viewportStart + i);
                    float value = values[ser];
                    int bonus = 0;
                    if (descriptors.get(ser).getKind() == TimeSeriesDescriptor.Kind.REL_SURFACE) {
                        for (int j = ser + 1; j < seriesCount; ++j) {
                            if (descriptors.get(j).getKind() == TimeSeriesDescriptor.Kind.REL_SURFACE) {
                                value += values[j];
                            }
                        }
                    } else if (descriptors.get(ser).getKind() == TimeSeriesDescriptor.Kind.ABS_SURFACE) {
                        for (int j = ser + 1; j < seriesCount; ++j) {
                            if (descriptors.get(j).getKind() == TimeSeriesDescriptor.Kind.ABS_SURFACE) {
                                bonus += 2;
                            }
                        }
                    }
                    xx[i] = lastx = DLightMath.map(viewportStart + i, viewportStart, viewportEnd, x, x + w);
                    yy[i] = lasty = (int)(y + h - 2 - value * effectiveHeight / scale) - bonus;
                }
                g2.setColor(descriptors.get(ser).getColor());
                switch (descriptors.get(ser).getKind()) {
                    case LINE:
                        g2.setStroke(LINE_STROKE);
                        g2.drawPolyline(xx, yy, sampleCount);
                        g2.setStroke(BALL_STROKE);
                        g2.setColor(Color.WHITE);
                        g2.fillOval(lastx - GraphConfig.BALL_SIZE / 2, lasty - GraphConfig.BALL_SIZE / 2, GraphConfig.BALL_SIZE - 1, GraphConfig.BALL_SIZE - 1);
                        g2.setColor(descriptors.get(ser).getColor());
                        g2.drawOval(lastx - GraphConfig.BALL_SIZE / 2, lasty - GraphConfig.BALL_SIZE / 2, GraphConfig.BALL_SIZE - 1, GraphConfig.BALL_SIZE - 1);
                        break;
                    case ABS_SURFACE:
                    case REL_SURFACE:
                        xx[sampleCount] = lastx;
                        xx[sampleCount + 1] = x;
                        yy[sampleCount] = yy[sampleCount + 1] = y + h;
                        g2.fillPolygon(xx, yy, sampleCount + 2);
                        break;
                    default:
                        System.err.println("Uknown graph kind: " + descriptors.get(ser).getKind()); // NOI18N
                }
            }
        }
        g2.setStroke(oldStroke);
    }

    private void dimInactiveRegions(Graphics g, int viewportStart, int viewportEnd, int filterStart, int filterEnd, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(GraphConfig.DIM_COLOR);
        if (viewportStart <= filterStart ) {
            int xx = DLightMath.map(filterStart, viewportStart, viewportEnd, 0, w);
            g2.fillRect(x, y, xx, h);
        }
        if (filterEnd <= viewportEnd) {
            int xx = DLightMath.map(filterEnd, viewportStart, viewportEnd, 0, w);
            g2.fillRect(x + xx, y, w - xx, h);
        }
    }

// axes painting ///////////////////////////////////////////////////////////////

    public void paintHorizontalAxis(Graphics g, int x, int y, int w, int h, List<AxisMark> marks, Color bg) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(bg);
        g2.fillRect(x, y, w, h);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(g2.getFont().deriveFont(GraphConfig.FONT_SIZE));

        FontMetrics fm = g2.getFontMetrics();
        for (AxisMark mark : marks) {
            if (mark.getText() != null) {
                int length = fm.stringWidth(mark.getText());
                g.setColor(adjustAlpha(GraphConfig.TEXT_COLOR, mark.getOpacity()));
                g.drawString(mark.getText(), x + mark.getPosition() - length / 2, y + fm.getAscent());
            }
        }
    }

    /**
     * Paints vertical axis (currently only labels).
     *
     * @param g  graphics
     * @param x  left coordinate of drawing area
     * @param y  upper coordinate of drawing area
     * @param w  width of drawing area
     * @param h  height of drawing area
     * @param bg  background color
     */
    public void paintVerticalAxis(Graphics g, int x, int y, int w, int h, List<AxisMark> marks, Color bg) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(bg);
        g2.fillRect(x, y, w, h);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(g2.getFont().deriveFont(GraphConfig.FONT_SIZE));

        FontMetrics fm = g2.getFontMetrics();
        for (AxisMark mark : marks) {
            if (mark.getText() != null) {
                int length = fm.stringWidth(mark.getText());
                g.setColor(adjustAlpha(GraphConfig.TEXT_COLOR, mark.getOpacity()));
                g.drawString(mark.getText(), x + w - length - fm.getAscent() / 2, y + h - 1 - mark.getPosition() + fm.getAscent() / 2);
            }
        }
    }

// common math /////////////////////////////////////////////////////////////////

    /**
     * Returns a copy of passed color with adjusted alpha value.
     *
     * @param orig  original color
     * @param alpha  new alpha value
     * @return copy of color with adjusted alpha
     */
    private static Color adjustAlpha(Color orig, int alpha) {
        if (orig.getAlpha() == alpha) {
            return orig;
        } else {
            return new Color(orig.getRed(), orig.getGreen(), orig.getBlue(), alpha);
        }
    }
}
