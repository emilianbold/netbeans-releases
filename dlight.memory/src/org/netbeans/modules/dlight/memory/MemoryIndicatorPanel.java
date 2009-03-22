package org.netbeans.modules.dlight.memory;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import javax.swing.BorderFactory;
import org.netbeans.modules.dlight.indicators.graph.GraphPanel;
import org.netbeans.modules.dlight.indicators.graph.Graph;
import org.netbeans.modules.dlight.indicators.graph.GraphColors;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.indicators.graph.GraphDetail;
import org.netbeans.modules.dlight.indicators.graph.Legend;
import org.openide.util.NbBundle;

/*package*/ class MemoryIndicatorPanel {

    private static final Color GRAPH_COLOR = GraphColors.COLOR_2;
    private static final GraphDescriptor DESCRIPTOR = new GraphDescriptor(
            GRAPH_COLOR, NbBundle.getMessage(MemoryIndicatorPanel.class, "graph.description")); // NOI18N
    private static final int ORDER = 1024;
    private static final String[] SIFFIXES = {"b", "Kb", "Mb", "Gb", "Tb"};

    private final Graph graph;
    private final GraphPanel<Graph, Legend> panel;
    private long max;

    /*package*/ MemoryIndicatorPanel() {
        graph = createGraph();
        panel = new GraphPanel<Graph, Legend>(getTitle(), graph, createLegend(), null, null);
        max = 0;
    }

    public GraphPanel getPanel() {
        return panel;
    }

    private static String getTitle() {
        return NbBundle.getMessage(MemoryIndicatorPanel.class, "indicator.title"); // NOI18N
    }

    private static Graph createGraph() {
        Graph graph = new Graph(100, DESCRIPTOR);
        graph.setBorder(BorderFactory.createLineBorder(GraphColors.BORDER_COLOR));
        graph.setMinimumSize(new Dimension(80, 60));
        graph.setPreferredSize(new Dimension(80, 60));
        return graph;
    }

    private Legend createLegend() {
        return new Legend(Arrays.asList(DESCRIPTOR), Arrays.<GraphDetail>asList(new MaxHeapDetail()));
    }

    public void setValue(long longValue) {
        int value = (int) (longValue / 1000 + (longValue % 1000 >= 500 ? 1 : 0));
        if (value > max) {
            max = value;
        }
        if (graph.getUpperLimit() < value) {
            graph.setUpperLimit(value * 3 / 2);
        }
        graph.addData(value);
    }

    private static String formatValue(long value) {
        int i = 0;
        while (ORDER <= value && i + 1 < SIFFIXES.length) {
            value /= ORDER;
            ++i;
        }
        return Long.toString(value) + " " + SIFFIXES[i];
    }

    private class MaxHeapDetail extends GraphDetail {

        public MaxHeapDetail() {
            super("Max:");
        }

        public String getValue() {
            return formatValue(max);
        }
    }
}
