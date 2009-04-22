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
package org.netbeans.modules.dlight.cpu.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.BorderFactory;
import org.netbeans.modules.dlight.indicators.graph.GraphPanel;
import org.netbeans.modules.dlight.indicators.graph.GraphConfig;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.indicators.graph.Legend;
import org.netbeans.modules.dlight.indicators.graph.PercentageGraph;
import org.openide.util.NbBundle;

/**
 * @author Alexey Vladykin
 */
public class CpuIndicatorPanel {

    private static final Color COLOR_SYS = GraphConfig.COLOR_3;
    private static final Color COLOR_USR = GraphConfig.COLOR_1;
    private static final GraphDescriptor SYS_DESCRIPTOR = new GraphDescriptor(
            COLOR_SYS, NbBundle.getMessage(CpuIndicatorPanel.class, "graph.description.system"), GraphDescriptor.Kind.REL_SURFACE);//NOI18N
    private static final GraphDescriptor USR_DESCRIPTOR = new GraphDescriptor(
            COLOR_USR, NbBundle.getMessage(CpuIndicatorPanel.class, "graph.description.user"), GraphDescriptor.Kind.REL_SURFACE);//NOI18N
    private static final String TIME_DETAIL_ID = "elapsed-time"; // NOI18N
    private static final int SECONDS_PER_MINUTE = 60;

    private final PercentageGraph graph;
    private final Legend legend;
    private final GraphPanel<PercentageGraph, Legend> panel;

    /*package*/ CpuIndicatorPanel() {
        graph = createGraph();
        legend = createLegend();
        panel = new GraphPanel<PercentageGraph, Legend>(getTitle(), graph, legend, null, graph.getVerticalAxis());
    }

    public GraphPanel getPanel() {
        return panel;
    }

    private static String getTitle() {
        return NbBundle.getMessage(CpuIndicatorPanel.class, "indicator.title"); // NOI18N
    }

    private static PercentageGraph createGraph() {
        PercentageGraph graph = new PercentageGraph(SYS_DESCRIPTOR, USR_DESCRIPTOR);
        graph.setBorder(BorderFactory.createLineBorder(GraphConfig.BORDER_COLOR));
        Dimension graphSize = new Dimension(GraphConfig.GRAPH_WIDTH, GraphConfig.GRAPH_HEIGHT);
        graph.setMinimumSize(graphSize);
        graph.setPreferredSize(graphSize);
        Dimension axisSize = new Dimension(GraphConfig.VERTICAL_AXIS_WIDTH, GraphConfig.VERTICAL_AXIS_HEIGHT);
        graph.getVerticalAxis().setMinimumSize(axisSize);
        graph.getVerticalAxis().setPreferredSize(axisSize);
        return graph;
    }

    private static Legend createLegend() {
        Legend legend = new Legend(Arrays.asList(USR_DESCRIPTOR, SYS_DESCRIPTOR),
                Collections.singletonMap(TIME_DETAIL_ID, NbBundle.getMessage(CpuIndicatorPanel.class, "label.time"))); // NOI18N
        legend.updateDetail(TIME_DETAIL_ID, formatTime(0));
        return legend;
    }

    /*package*/ void addData(int sys, int usr) {
        graph.addData(sys, usr);
    }

    /*package*/ void setSysValue(int v) {
        //getLegend().setSysValue(formatValue(v));
    }

    /*package*/ void setUsrValue(int v) {
        //getLegend().setUsrValue(formatValue(v));
    }

    /*package*/ void setTime(int seconds) {
        legend.updateDetail(TIME_DETAIL_ID, formatTime(seconds));
    }

    private static String formatTime(int seconds) {
        return String.format("%d:%02d", seconds / SECONDS_PER_MINUTE, seconds % SECONDS_PER_MINUTE); // NOI18N
    }
}
