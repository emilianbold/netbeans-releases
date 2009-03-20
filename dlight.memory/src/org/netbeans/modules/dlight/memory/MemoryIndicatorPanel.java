package org.netbeans.modules.dlight.memory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.modules.dlight.indicators.graph.GraphPanel;
import org.netbeans.modules.dlight.indicators.graph.Graph;
import org.netbeans.modules.dlight.indicators.graph.GraphColors;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.openide.util.NbBundle;

class MemoryIndicatorPanel extends GraphPanel<Graph, MemoryIndicatorPanel.LegendPanel> {

    private long max;
    private static final Color GRAPH_COLOR = GraphColors.COLOR_2;

    /*package*/ MemoryIndicatorPanel() {
        super(getTitle(), createGraph(), createLegend(), null, null);
        max = 0;
    }

    private static String getTitle() {
        return NbBundle.getMessage(MemoryIndicatorPanel.class, "indicator.title"); // NOI18N
    }

    private static Graph createGraph() {
        Graph graph = new Graph(100, new GraphDescriptor(GRAPH_COLOR, NbBundle.getMessage(MemoryIndicatorPanel.class, "graph.description")));
        graph.setBorder(BorderFactory.createLineBorder(GraphColors.BORDER_COLOR));
        graph.setMinimumSize(new Dimension(66, 32));
        graph.setPreferredSize(new Dimension(150, 80));
        return graph;
    }

    private static LegendPanel createLegend() {
        return new LegendPanel();
    }

    public void setValue(long longValue) {
        int value = (int) (longValue / 1000 + (longValue % 1000 >= 500 ? 1 : 0));
        if (value > max) {
            max = value;
            getLegend().setMaxValue(formatValue(value));
        }
        getLegend().setCurValue(formatValue(value));

        if (getGraph().getUpperLimit() < value) {
            getGraph().setUpperLimit(value * 3 / 2);
        }
        getGraph().addData(value);
    }
    
    private static String formatValue(long value) {
        return String.format("%02d", value);
    }

    protected static final class LegendPanel extends JPanel {

        private final JLabel lblCurrValue;
        private final JLabel lblMaxValue;

        private LegendPanel() {
            super(new GridBagLayout());
            String measureKb = NbBundle.getMessage(MemoryIndicatorPanel.class, "measure.kb");

            JLabel lblCurrLabel = new JLabel(NbBundle.getMessage(MemoryIndicatorPanel.class, "label.curr"));
            lblCurrValue = new JLabel(formatValue(0));
            lblCurrValue.setHorizontalAlignment(SwingConstants.RIGHT);
            JLabel lblCurrMeasure = new JLabel(measureKb);

            JLabel lblMaxLabel = new JLabel(NbBundle.getMessage(MemoryIndicatorPanel.class, "label.max"));
            lblMaxValue = new JLabel(formatValue(0));
            lblMaxValue.setHorizontalAlignment(SwingConstants.RIGHT);
            JLabel lblMaxMeasure = new JLabel(measureKb);

            lblCurrLabel.setForeground(GraphColors.TEXT_COLOR);
            lblCurrMeasure.setForeground(GraphColors.TEXT_COLOR);
            lblCurrValue.setForeground(GraphColors.TEXT_COLOR);
            lblMaxLabel.setForeground(GraphColors.TEXT_COLOR);
            lblMaxMeasure.setForeground(GraphColors.TEXT_COLOR);
            lblMaxValue.setForeground(GraphColors.TEXT_COLOR);

            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(GraphColors.BORDER_COLOR));
            setMinimumSize(new Dimension(100, 80));
            setPreferredSize(new Dimension(100, 80));

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(0, 6, 0, 0);
            c.gridy = 0;
            c.gridx = 0;
            add(lblCurrLabel, c);
            c.gridx = 1;
            add(lblCurrValue, c);
            c.gridx = 2;
            add(lblCurrMeasure, c);

            c.gridy = 1;
            c.gridx = 0;
            add(lblMaxLabel, c);
            c.gridx = 1;
            add(lblMaxValue, c);
            c.gridx = 2;
            add(lblMaxMeasure, c);
        }

        public void setCurValue(String value) {
            lblCurrValue.setText(value);
        }

        public void setMaxValue(String value) {
            lblMaxValue.setText(value);
        }
    }

}
