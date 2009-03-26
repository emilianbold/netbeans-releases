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
    private static final GraphDescriptor DESCRIPTOR = new GraphDescriptor(GRAPH_COLOR, NbBundle.getMessage(SyncIndicatorPanel.class, "graph.description.locks")); // NOI18N

    private final Graph graph;
    private final GraphPanel<Graph, Legend> panel;

    /*package*/ SyncIndicatorPanel() {
        graph = createGraph();
        panel = new GraphPanel<Graph, Legend>(getTitle(), graph, createLegend(), null, graph.getVerticalAxis());
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
        graph.setMinimumSize(new Dimension(80, 60));
        graph.setPreferredSize(new Dimension(80, 60));
        graph.getVerticalAxis().setMinimumSize(new Dimension(30, 60));
        graph.getVerticalAxis().setPreferredSize(new Dimension(30, 60));
        return graph;
    }

    private static Legend createLegend() {
        return new Legend(Arrays.asList(DESCRIPTOR), Collections.<String, String>emptyMap());
    }

    public void addData(int value) {
        graph.addData(value);
    }
}
