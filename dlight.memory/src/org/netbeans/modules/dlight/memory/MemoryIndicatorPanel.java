package org.netbeans.modules.dlight.memory;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.BorderFactory;
import org.netbeans.modules.dlight.indicators.graph.GraphPanel;
import org.netbeans.modules.dlight.indicators.graph.Graph;
import org.netbeans.modules.dlight.indicators.graph.GraphColors;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.indicators.graph.GraphDetail;
import org.netbeans.modules.dlight.indicators.graph.Legend;
import org.openide.util.NbBundle;

class MemoryIndicatorPanel extends GraphPanel<Graph, Legend> {

    private static final Color GRAPH_COLOR = GraphColors.COLOR_2;
    private static final GraphDescriptor DESCRIPTOR = new GraphDescriptor(
            GRAPH_COLOR, NbBundle.getMessage(MemoryIndicatorPanel.class, "graph.description"));

    private long max;

    /*package*/ MemoryIndicatorPanel() {
        super(getTitle(), createGraph(), createLegend(), null, null);
        max = 0;
    }

    private static String getTitle() {
        return NbBundle.getMessage(MemoryIndicatorPanel.class, "indicator.title"); // NOI18N
    }

    private static Graph createGraph() {
        Graph graph = new Graph(100, DESCRIPTOR);
        graph.setBorder(BorderFactory.createLineBorder(GraphColors.BORDER_COLOR));
        graph.setMinimumSize(new Dimension(66, 32));
        graph.setPreferredSize(new Dimension(150, 80));
        return graph;
    }

    private static Legend createLegend() {
        return new Legend(Arrays.asList(DESCRIPTOR), Collections.<GraphDetail>emptyList());
    }

    public void setValue(long longValue) {
        int value = (int) (longValue / 1000 + (longValue % 1000 >= 500 ? 1 : 0));
        if (value > max) {
            max = value;
            //getLegend().setMaxValue(formatValue(value));
        }
        //getLegend().setCurValue(formatValue(value));

        if (getGraph().getUpperLimit() < value) {
            getGraph().setUpperLimit(value * 3 / 2);
        }
        getGraph().addData(value);
    }
    
}
