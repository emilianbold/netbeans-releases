package org.netbeans.modules.dlight.sync;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.indicators.graph.Graph;
import org.openide.util.NbBundle;

class SyncIndicatorPanel extends JPanel {

    private final Graph graph;

    private final JLabel lblLocksLabel;
    private final JLabel lblLocksValue;

    private Color locksColor = new Color(128, 0, 128);

    SyncIndicatorPanel() {

        GraphDescriptor descriptorLocks = new GraphDescriptor(locksColor, NbBundle.getMessage(getClass(), "graph.deccription.locks")); // NOI18N

        Color borderColor = new Color(0x77, 0x88, 0x88);

        graph = new Graph(100, descriptorLocks); // , descriptorSleep, descriptorLatency);
        graph.setBorder(BorderFactory.createLineBorder(borderColor));
        graph.setMinimumSize(new Dimension(66, 32));
        graph.setPreferredSize(new Dimension(150, 80));

        lblLocksLabel = new JLabel(NbBundle.getMessage(getClass(), "label.locks")); // NOI18N
        lblLocksValue = new JLabel("00%"); // NOI18N
        lblLocksValue.setHorizontalAlignment(SwingConstants.RIGHT);

        Color lblColor = locksColor.darker();
        lblLocksLabel.setForeground(lblColor);
        lblLocksValue.setForeground(lblColor);

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(graph);

        JPanel legend = new JPanel(new GridBagLayout());
        legend.setBackground(Color.WHITE);
        legend.setBorder(BorderFactory.createLineBorder(borderColor));
        legend.setMinimumSize(new Dimension(120, 32));
        legend.setMaximumSize(new Dimension(120, Integer.MAX_VALUE));
        legend.setPreferredSize(new Dimension(100, 80));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        legend.add(lblLocksLabel, c);
        c.gridx = 1;
        c.insets = new Insets(0, 6, 0, 0);
        legend.add(lblLocksValue, c);

        add(legend);
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
