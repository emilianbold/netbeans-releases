package org.netbeans.modules.dlight.memory;

import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import javax.swing.BorderFactory;
import org.netbeans.modules.dlight.indicators.graph.GraphPanel;
import org.netbeans.modules.dlight.indicators.graph.Graph;
import org.netbeans.modules.dlight.indicators.graph.Graph.LabelRenderer;
import org.netbeans.modules.dlight.indicators.graph.GraphConfig;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.indicators.graph.Legend;
import org.openide.util.NbBundle;

/*package*/ class MemoryIndicatorPanel {

    private static final Color GRAPH_COLOR = GraphConfig.COLOR_2;
    private static final GraphDescriptor DESCRIPTOR = new GraphDescriptor(
            GRAPH_COLOR, NbBundle.getMessage(MemoryIndicatorPanel.class, "graph.description")); // NOI18N
    private static final String MAX_HEAP_DETAIL_ID = "max-heap"; // NOI18N
    private static final int BINARY_ORDER = 1024;
    private static final int DECIMAL_ORDER = 1000;
    private static final String[] SIFFIXES = {"b", "K", "M", "G", "T"};

    private static final NumberFormat INT_FORMAT = NumberFormat.getIntegerInstance(Locale.US);
    private static final NumberFormat FRAC_FORMAT = NumberFormat.getNumberInstance(Locale.US);
    static {
        FRAC_FORMAT.setMaximumFractionDigits(1);
    }

    private final Graph graph;
    private final Legend legend;
    private final GraphPanel<Graph, Legend> panel;
    private long max;

    /*package*/ MemoryIndicatorPanel() {
        graph = createGraph();
        legend = createLegend();
        panel = new GraphPanel<Graph, Legend>(getTitle(), graph, legend, null, graph.getVerticalAxis());
        max = 0;
    }

    public GraphPanel getPanel() {
        return panel;
    }

    private static String getTitle() {
        return NbBundle.getMessage(MemoryIndicatorPanel.class, "indicator.title"); // NOI18N
    }

    private static Graph createGraph() {
        Graph graph = new Graph(BINARY_ORDER, new LabelRenderer() {
            public String render(int value) {
                return formatValue(value);
            }
        }, DESCRIPTOR);
        graph.setBorder(BorderFactory.createLineBorder(GraphConfig.BORDER_COLOR));
        graph.setMinimumSize(new Dimension(80, 60));
        graph.setPreferredSize(new Dimension(80, 60));
        graph.getVerticalAxis().setMinimumSize(new Dimension(30, 60));
        graph.getVerticalAxis().setPreferredSize(new Dimension(30, 60));
        return graph;
    }

    private static Legend createLegend() {
        Legend legend = new Legend(Arrays.asList(DESCRIPTOR), Collections.singletonMap(MAX_HEAP_DETAIL_ID, "Max:"));
        legend.updateDetail(MAX_HEAP_DETAIL_ID, formatValue(0));
        return legend;
    }

    public void addData(long value) {
        if (graph.getUpperLimit() < value) {
            graph.setUpperLimit((int)value * 3 / 2);
        }
        graph.addData((int)value);
        if (value > max) {
            max = value;
            legend.updateDetail(MAX_HEAP_DETAIL_ID, formatValue(max));
        }
    }

    private static String formatValue(long value) {
        double dbl = value;
        int i = 0;
        while (BINARY_ORDER <= dbl && i + 1 < SIFFIXES.length) {
            dbl /= BINARY_ORDER;
            ++i;
        }
        if (DECIMAL_ORDER <= dbl && i + 1 < SIFFIXES.length) {
            dbl /= BINARY_ORDER;
            ++i;
        }
        NumberFormat nf = dbl < 10? FRAC_FORMAT : INT_FORMAT;
        return nf.format(dbl) + SIFFIXES[i];
    }

}
