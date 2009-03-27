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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JComponent;

/**
 * Displays a percentage graph
 * @author Vladimir Kvashin
 */
public class Graph extends JComponent {

    public interface LabelRenderer {
        String render(int value);
    }

    private static final boolean TRACE = Boolean.getBoolean("PercentageGraph.trace");
    private final GraphPainter graph;
    private Axis hAxis;
    private Axis vAxis;

    public Graph(int scale, LabelRenderer renderer, GraphDescriptor ... descriptors) {
        setOpaque(true);
        graph = new GraphPainter(scale, renderer, descriptors);
//        ToolTipManager.sharedInstance().registerComponent(this);
//        addAncestorListener(new AncestorListener() {
//            public void ancestorAdded(AncestorEvent event) {
//                graph.setSize(getWidth(), getHeight());
//            }
//            public void ancestorRemoved(AncestorEvent event) {}
//            public void ancestorMoved(AncestorEvent event) {}
//        });
    }

    public synchronized JComponent getVerticalAxis() {
        if (vAxis == null) {
            vAxis = new Axis(AxisOrientation.VERTICAL, 0, graph.getUpperLimit());
        }
        return vAxis;
    }

    @Override
    public void setSize(Dimension d) {
        if (TRACE) System.err.printf("setSize %s\n", d);
        checkSize(d.width, d.height);
        super.setSize(d);
    }

    @Override
    public void setSize(int width, int height) {
        if (TRACE) System.err.printf("setSize %d %d\n", width, height);
        checkSize(width, height);
        super.setSize(width, height);
    }

    @Override
    public void setBounds(Rectangle r) {
        if (TRACE) System.err.printf("setBounds %s\n", r);
        checkSize(r.width, r.height);
        super.setBounds(r);
    }

    public void setUpperLimit(int newScale) {
        if (newScale != graph.getUpperLimit()) {
            if (vAxis != null) {
                vAxis.setUpperLimit(newScale);
            }
            graph.setUpperLimit(newScale);
            repaint();
        }
    }

    public int getUpperLimit() {
        return graph.getUpperLimit();
    }
//    @Override
//    public void invalidate() {
//        super.invalidate();
//        graph.invalidate();
//    }

//    @Override
//    @SuppressWarnings("deprecation") // should override this deprecated method
//    public void reshape(int x, int y, int w, int h) {
//        if (TRACE) System.err.printf("reshape %d %d %d %d\n", x, y, w, h);
//        checkSize(w, h);
//        super.reshape(x, y, w, h);
//    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        if (TRACE) System.err.printf("setBounds %d %d %d %d\n", x, y, width, height);
        checkSize(width, height);
        super.setBounds(x, y, width, height);
    }

    private void checkSize(int width, int height) {
        if ((this.getWidth() != width) || (this.getHeight() != height)) {
            graph.setSize(width, height);
        }
    }

    private static int paintCount = 0;

    @Override
    protected void paintComponent(Graphics g) {
        graph.draw(g, 0, 0);
    }


//    private Rectangle getUpdateRectangle() {
//        int left = 0; // getX();
//        int top = getY();
//        int width = getWidth();
//        int height = getHeight();
//        Rectangle r;
//        int x = left + Math.min(width, data.size()) - 1;
//        r = new Rectangle(x, top, 1, height);
//        return r;
//    }



    public void addData(int... newData) {
        graph.addData(newData);
        if (isShowing()) {
            repaint();
//            repaint(getUpdateRectangle());
        }
     }

//    @Override
//    public String getToolTipText() {
//        int[] last = graph.getLastData();
//        GraphDescriptor[] descriptors = graph.getDescriptors();
//        StringBuilder sb = new StringBuilder();
//        sb.append("<html>");
//        for (int i = 0; i < descriptors.length; i++) {
//            Color color = descriptors[i].color;
//            String strColor = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
//            String font = String.format("<font color=\"%s\"/>", strColor);
//            String strValue = formatValue(last[i]);
//            sb.append(String.format("<tr><td>%s%s</td><td>%s %s</td></tr>\n", font, descriptors[i].description, font, strValue));
//        }
//        sb.append("</html>");
//        System.err.printf("TOOLTIP:\n%s\n", sb.toString());
//        return sb.toString();
//    }

    protected String formatValue(int value) {
        return String.format("%d", value);
    }

    private static enum AxisOrientation {
        HORIZONTAL,
        VERTICAL
    }

    private class Axis extends JComponent {

        private final AxisOrientation orientation;
        private int min;
        private int max;

        public Axis(AxisOrientation orientation, int min, int max) {
            this.orientation = orientation;
            this.min = min;
            this.max = max;
            setOpaque(true);
            setMinimumSize(new Dimension(20, 80));
            setPreferredSize(new Dimension(20, 80));
        }

        public void setUpperLimit(int limit) {
            this.max = limit;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            graph.drawVerticalAxis(g, getWidth(), getHeight(), getBackground());
        }

    }

}
