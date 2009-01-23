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
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.indicators.graph.Graph;
import org.openide.util.NbBundle;

class SyncIndicatorPanel extends JPanel {

    private final Graph graph;

    private final JLabel lblLocksLabel;
    private final JLabel lblLocksValue;

    private Color locksColor = new Color(128, 0, 128);

    SyncIndicatorPanel() {

        GraphDescriptor descriptorLocks = new GraphDescriptor(locksColor, NbBundle.getMessage(getClass(), "graph.deccription.locks"));

        graph = new Graph(100, descriptorLocks); // , descriptorSleep, descriptorLatency);
        graph.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        Dimension d = new Dimension(66, 32);
        graph.setPreferredSize(d);
        graph.setMaximumSize(d);
        graph.setMinimumSize(d);

        lblLocksLabel = new JLabel(NbBundle.getMessage(getClass(), "label.locks"));
        lblLocksValue = new JLabel("0");
        lblLocksValue.setHorizontalAlignment(SwingConstants.RIGHT);

        Color lblColor = locksColor.darker();
        lblLocksLabel.setForeground(lblColor);
        lblLocksValue.setForeground(lblColor);
                
        setLayout(new GridBagLayout());
        GridBagConstraints c;

        // graph

        c = new GridBagConstraints();
        c.gridheight = 1; // 3;
        c.gridx = 0;
        c.gridy = 0;
        add(graph, c);

        int row = 0;

        //  locks
        c = new GridBagConstraints();
        c.insets = new Insets(0, 6, 0, 0);
        c.gridy = row++;
        c.gridx = 1;
        add(lblLocksLabel, c);
        c.gridx = 2;
        add(lblLocksValue, c);

    }

    public void updated(int[][] values) {
        for( int i = 0; i < values.length; i++) {
            //graph.addData(values[i]);
            int lck = values[i][0];
            graph.addData(new int[] { lck } );
            lblLocksValue.setText(String.format("%02d%%", lck));
        }
    }
}
