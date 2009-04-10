package org.netbeans.modules.dlight.sync;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.BorderFactory;
import org.netbeans.modules.dlight.indicators.graph.GraphPanel;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.indicators.graph.Graph;
import org.netbeans.modules.dlight.indicators.graph.GraphConfig;
import org.netbeans.modules.dlight.indicators.graph.Legend;
import org.openide.util.NbBundle;

/*package*/ class SyncIndicatorPanel {

    private static final Color GRAPH_COLOR = GraphConfig.COLOR_4;
    private static final GraphDescriptor DESCRIPTOR = new GraphDescriptor(
            GRAPH_COLOR, NbBundle.getMessage(SyncIndicatorPanel.class, "graph.description.locks"), GraphDescriptor.Kind.LINE); // NOI18N
    private static final String THREADS_DETAIL_ID = "thread-count";//NOI18N

    private final Graph graph;
    private final Legend legend;
    private final GraphPanel<Graph, Legend> panel;

    /*package*/ SyncIndicatorPanel() {
        graph = createGraph();
        legend = createLegend();
        panel = new GraphPanel<Graph, Legend>(getTitle(), graph, legend, null, graph.getVerticalAxis());
    }

    public GraphPanel getPanel() {
        return panel;
    }

    private static String getTitle() {
        return NbBundle.getMessage(SyncIndicatorPanel.class, "indicator.title"); // NOI18N
    }

    private static Graph createGraph() {
        Graph graph = new Graph(100, null, DESCRIPTOR);
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
        Legend legend = new Legend(Arrays.asList(DESCRIPTOR), Collections.<String, String>singletonMap(THREADS_DETAIL_ID, NbBundle.getMessage(SyncIndicatorPanel.class, "SyncTool.Legend.Threads")));//NOI18N
        legend.updateDetail(THREADS_DETAIL_ID, Integer.toString(0));
        return legend;
    }

    public void addData(int locks, int threads) {
        graph.addData(locks);
        legend.updateDetail(THREADS_DETAIL_ID, Integer.toString(threads));
    }
}
