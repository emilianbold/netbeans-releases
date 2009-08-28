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
package org.netbeans.modules.dlight.indicators.graph;

import java.awt.FontMetrics;
import org.netbeans.modules.dlight.indicators.ValueFormatter;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import java.awt.Graphics;
import java.util.List;
import javax.swing.JComponent;

/**
 * Displays a graph
 * @author Vladimir Kvashin
 * @author Alexey Vladykin
 */
public class TimeSeriesPlot extends JComponent {

    private final GraphPainter graph;
    private int upperLimit;
    private Axis hAxis;
    private Axis vAxis;
    private int viewportStart;
    private int viewportEnd;
    private AxisMarksProvider timeMarksProvider;
    private AxisMarksProvider valueMarksProvider;

    public TimeSeriesPlot(int scale, ValueFormatter formatter, List<TimeSeriesDescriptor> series) {
        upperLimit = scale;
        graph = new GraphPainter(series);
        timeMarksProvider = AxisMarksProviderFactory.newTimeMarksProvider();
        valueMarksProvider = AxisMarksProviderFactory.newValueMarksProvider(formatter);
        setOpaque(true);
//        ToolTipManager.sharedInstance().registerComponent(this);
//        addAncestorListener(new AncestorListener() {
//            public void ancestorAdded(AncestorEvent event) {
//                graph.setSize(getWidth(), getHeight());
//            }
//            public void ancestorRemoved(AncestorEvent event) {}
//            public void ancestorMoved(AncestorEvent event) {}
//        });
    }

    public JComponent getVerticalAxis() {
        if (vAxis == null) {
            vAxis = new Axis(AxisOrientation.VERTICAL);
        }
        return vAxis;
    }

    public JComponent getHorizontalAxis() {
        if (hAxis == null) {
            hAxis = new Axis(AxisOrientation.HORIZONTAL);
        }
        return hAxis;
    }

    public void setUpperLimit(int newScale) {
        if (newScale != upperLimit) {
            upperLimit = newScale;
            repaint();
            if (vAxis != null) {
                vAxis.repaint();
            }
        }
    }

    public int getUpperLimit() {
        return upperLimit;
    }

    public int calculateUpperLimit(float... data) {
        return graph.calculateUpperLimit(data);
    }

    @Override
    protected void paintComponent(Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        List<AxisMark> timeMarks = timeMarksProvider.getAxisMarks(viewportStart, viewportEnd, getWidth(), fm);
        List<AxisMark> valueMarks = valueMarksProvider.getAxisMarks(0, upperLimit, getHeight() - fm.getAscent() / 2, fm);
        graph.paint(g, upperLimit, valueMarks, viewportStart, viewportEnd, timeMarks, 0, 0, getWidth(), getHeight(), isEnabled());
    }

    public void addData(float... newData) {
        graph.addData(newData);
        repaintAll();
    }

    public Range<Integer> getViewport() {
        return new Range<Integer>(viewportStart, viewportEnd);
    }

    public void setViewport(Range<Integer> viewport) {
        boolean changed = false;
        if (viewport.getStart() != null) {
            int newViewportStart = viewport.getStart();
            if (viewportStart != newViewportStart) {
                viewportStart = newViewportStart;
                changed = true;
            }
        }
        if (viewport.getEnd() != null) {
            int newViewportEnd = viewport.getEnd();
            if (viewportEnd != newViewportEnd) {
                viewportEnd = newViewportEnd;
                changed = true;
            }
        }
        if (changed) {
            repaintAll();
        }
    }

    public Range<Integer> getSelection() {
        return null;
    }

    public void setSelection(Range<Integer> selection) {
        repaintAll();
    }

    private void repaintAll() {
        repaint();
        if (hAxis != null) {
            hAxis.repaint();
        }
        if (vAxis != null) {
            vAxis.repaint();
        }
    }

    private static enum AxisOrientation {
        HORIZONTAL,
        VERTICAL
    }

    private class Axis extends JComponent {

        private final AxisOrientation orientation;

        public Axis(AxisOrientation orientation) {
            this.orientation = orientation;
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isEnabled()) {
                FontMetrics fm = g.getFontMetrics();
                switch (orientation) {
                    case VERTICAL:
                        List<AxisMark> valueMarks = valueMarksProvider.getAxisMarks(0, upperLimit, getHeight() - fm.getAscent() / 2, fm);
                        graph.paintVerticalAxis(g, 0, 0, getWidth(), getHeight(), valueMarks, getBackground());
                        break;
                    case HORIZONTAL:
                        List<AxisMark> timeMarks = timeMarksProvider.getAxisMarks(viewportStart, viewportEnd, getWidth(), fm);
                        graph.paintHorizontalAxis(g, 0, 0, getWidth(), getHeight(), timeMarks, getBackground());
                        break;
                }
            }
        }
    }
}
