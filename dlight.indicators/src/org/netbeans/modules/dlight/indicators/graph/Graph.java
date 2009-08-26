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
public class Graph extends JComponent {

    private final GraphPainter graph;
    private int upperLimit;
    private Axis hAxis;
    private Axis vAxis;

    public Graph(int scale, ValueFormatter renderer, List<TimeSeriesDescriptor> descriptors) {
        upperLimit = scale;
        graph = new GraphPainter(renderer, descriptors);
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

    public synchronized JComponent getVerticalAxis() {
        if (vAxis == null) {
            vAxis = new Axis(AxisOrientation.VERTICAL);
        }
        return vAxis;
    }

    public synchronized void setUpperLimit(int newScale) {
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
        graph.paint(g, upperLimit, 0, 0, getWidth(), getHeight(), isEnabled());
    }

    public void addData(float... newData) {
        graph.addData(newData);
        if (isShowing()) {
            repaint();
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
        return String.format("%d", value); // NOI18N
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
                graph.paintVerticalAxis(g, 0, 0, getWidth(), getHeight(), upperLimit, getBackground());
            }
        }

    }

}
