package org.netbeans.modules.dlight.sync;

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
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.indicators.graph.Graph;
import org.netbeans.modules.dlight.indicators.graph.GraphColors;
import org.openide.util.NbBundle;

/*package*/ class SyncIndicatorPanel extends GraphPanel<Graph, SyncIndicatorPanel.LegendPanel> {

    private static final Color GRAPH_COLOR = GraphColors.COLOR_1;

    /*package*/ SyncIndicatorPanel() {
        super(getTitle(), createGraph(), createLegend(), null, null);
    }

    private static String getTitle() {
        return NbBundle.getMessage(SyncIndicatorPanel.class, "indicator.title"); // NOI18N
    }

    private static Graph createGraph() {
        GraphDescriptor descriptorLocks = new GraphDescriptor(GRAPH_COLOR, NbBundle.getMessage(SyncIndicatorPanel.class, "graph.description.locks")); // NOI18N
        Graph graph = new Graph(100, descriptorLocks);
        graph.setBorder(BorderFactory.createLineBorder(GraphColors.BORDER_COLOR));
        graph.setMinimumSize(new Dimension(66, 32));
        graph.setPreferredSize(new Dimension(150, 80));
        return graph;
    }

    private static LegendPanel createLegend() {
        return new LegendPanel();
    }

    public void updated(int[][] values) {
        for (int i = 0; i < values.length; i++) {
            int lck = values[i][0];
            getGraph().addData(new int[]{lck});
            getLegend().setLocksValue(String.format("%02d%%", lck)); // NOI18N
        }
    }

    protected static final class LegendPanel extends JPanel {

        private final JLabel lblLocksValue;

        private LegendPanel() {
            super(new GridBagLayout());

            JLabel lblLocksLabel = new JLabel(NbBundle.getMessage(getClass(), "label.locks")); // NOI18N
            lblLocksValue = new JLabel("00%"); // NOI18N
            lblLocksValue.setHorizontalAlignment(SwingConstants.RIGHT);

            lblLocksLabel.setForeground(GraphColors.TEXT_COLOR);
            lblLocksValue.setForeground(GraphColors.TEXT_COLOR);

            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(GraphColors.BORDER_COLOR));
            setMinimumSize(new Dimension(100, 80));
            setPreferredSize(new Dimension(100, 80));

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            add(lblLocksLabel, c);
            c.gridx = 1;
            c.insets = new Insets(0, 6, 0, 0);
            add(lblLocksValue, c);
        }

        private void setLocksValue(String value) {
            lblLocksValue.setText(value);
        }
    }
}
