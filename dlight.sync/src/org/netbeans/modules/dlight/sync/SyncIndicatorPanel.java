package org.netbeans.modules.dlight.sync;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.modules.dlight.indicators.graph.AbstractIndicatorPanel;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.indicators.graph.Graph;
import org.netbeans.modules.dlight.indicators.graph.GraphColors;
import org.openide.util.NbBundle;

class SyncIndicatorPanel extends AbstractIndicatorPanel {

    private Graph graph;
    private JPanel legend;

    private JLabel lblLocksLabel;
    private JLabel lblLocksValue;

    private static final Color GRAPH_COLOR = GraphColors.COLOR_1;

    /*package*/ SyncIndicatorPanel() {
    }

    @Override
    protected String getTitle() {
        return NbBundle.getMessage(SyncIndicatorPanel.class, "indicator.title"); // NOI18N
    }

    protected JComponent createGraph() {
        if (graph == null) {
            GraphDescriptor descriptorLocks = new GraphDescriptor(GRAPH_COLOR, NbBundle.getMessage(getClass(), "graph.description.locks")); // NOI18N
            graph = new Graph(100, descriptorLocks);
            graph.setBorder(BorderFactory.createLineBorder(GraphColors.BORDER_COLOR));
            graph.setMinimumSize(new Dimension(66, 32));
            graph.setPreferredSize(new Dimension(150, 80));
        }
        return graph;
    }

    @Override
    protected JComponent createLegend() {
        if (legend == null) {
            lblLocksLabel = new JLabel(NbBundle.getMessage(getClass(), "label.locks")); // NOI18N
            lblLocksValue = new JLabel("00%"); // NOI18N
            lblLocksValue.setHorizontalAlignment(SwingConstants.RIGHT);

            lblLocksLabel.setForeground(GraphColors.TEXT_COLOR);
            lblLocksValue.setForeground(GraphColors.TEXT_COLOR);

            legend = new JPanel(new GridBagLayout());
            legend.setBackground(Color.WHITE);
            legend.setBorder(BorderFactory.createLineBorder(GraphColors.BORDER_COLOR));
            legend.setMinimumSize(new Dimension(100, 80));
            legend.setPreferredSize(new Dimension(100, 80));

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            legend.add(lblLocksLabel, c);
            c.gridx = 1;
            c.insets = new Insets(0, 6, 0, 0);
            legend.add(lblLocksValue, c);
        }
        return legend;
    }

    public void updated(int[][] values) {
        for( int i = 0; i < values.length; i++) {
            //graph.addData(values[i]);
            int lck = values[i][0];
            graph.addData(new int[] { lck } );
            lblLocksValue.setText(String.format("%02d%%", lck)); // NOI18N
        }
    }
}
