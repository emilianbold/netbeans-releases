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
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.dlight.indicators.ValueFormatter;
import org.netbeans.modules.dlight.indicators.TimeSeriesDescriptor;
import java.awt.Graphics;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.dlight.extras.api.ViewportAware;
import org.netbeans.modules.dlight.extras.api.ViewportModel;
import org.netbeans.modules.dlight.util.Range;
import org.netbeans.modules.dlight.extras.api.support.DefaultViewportModel;

/**
 * Displays a graph
 * @author Vladimir Kvashin
 * @author Alexey Vladykin
 */
public class TimeSeriesPlot extends JComponent implements ViewportAware, ChangeListener {

    private static final long EXTENT = 20000; // 20 seconds

    private final GraphPainter graph;
    private ViewportModel viewportModel;
    private int upperLimit;
    private Axis hAxis;
    private Axis vAxis;
    private AxisMarksProvider timeMarksProvider;
    private AxisMarksProvider valueMarksProvider;

    public TimeSeriesPlot(int scale, ValueFormatter formatter, List<TimeSeriesDescriptor> series) {
        upperLimit = scale;
        graph = new GraphPainter(series);
        graph.addData(new float[series.size()]); // 0th tick - all zeros
        timeMarksProvider = AxisMarksProviderFactory.newTimeMarksProvider();
        valueMarksProvider = AxisMarksProviderFactory.newValueMarksProvider(formatter);
        ViewportModel model = new DefaultViewportModel();
        model.setLimits(new Range<Long>(0L, 0L));
        model.setViewport(new Range<Long>(0L, EXTENT));
        setViewportModel(model);
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
        Range<Long> viewport = viewportModel.getViewport();
        int viewportStart = (int)TimeUnit.MILLISECONDS.toSeconds(viewport.getStart());
        int viewportEnd = (int)TimeUnit.MILLISECONDS.toSeconds(viewport.getEnd());
        List<AxisMark> timeMarks = timeMarksProvider.getAxisMarks(viewportStart, viewportEnd, getWidth(), fm);
        List<AxisMark> valueMarks = valueMarksProvider.getAxisMarks(0, upperLimit, getHeight() - fm.getAscent() / 2, fm);
        graph.paint(g, upperLimit, valueMarks, viewportStart, viewportEnd, timeMarks, 0, 0, getWidth(), getHeight(), isEnabled());
    }

    public void addData(float... newData) {
        graph.addData(newData);
        viewportModel.setLimits(new Range<Long>(0L, TimeUnit.SECONDS.toMillis(graph.getDataSize())));
        repaintAll();
    }

    public ViewportModel getViewportModel() {
        return viewportModel;
    }

    public void setViewportModel(ViewportModel viewportModel) {
        if (this.viewportModel != null) {
            this.viewportModel.removeChangeListener(this);
        }
        this.viewportModel = viewportModel;
        this.viewportModel.addChangeListener(this);
        repaintAll();
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == viewportModel) {
            repaintAll();
        }
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
                        Range<Long> viewport = viewportModel.getViewport();
                        int viewportStart = (int)(viewport.getStart() / 1000);
                        int viewportEnd = (int)(viewport.getEnd() / 1000);
                        List<AxisMark> timeMarks = timeMarksProvider.getAxisMarks(viewportStart, viewportEnd, getWidth(), fm);
                        graph.paintHorizontalAxis(g, 0, 0, getWidth(), getHeight(), timeMarks, getBackground());
                        break;
                }
            }
        }
    }
}
