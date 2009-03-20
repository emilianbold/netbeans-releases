package org.netbeans.modules.dlight.memory;

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
import org.netbeans.modules.dlight.indicators.graph.Graph;
import org.netbeans.modules.dlight.indicators.graph.GraphColors;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.openide.util.NbBundle;

class MemoryIndicatorPanel extends AbstractIndicatorPanel {

    private Graph graph;
    private JLabel lblMaxLabel;
    private JLabel lblMaxValue;
    private JLabel lblMaxMeasure;
    private JLabel lblCurrLabel;
    private JLabel lblCurrValue;
    private JLabel lblCurrMeasure;
    private String measureKb;
    private JPanel legend;
    private long max;
    private static final Color GRAPH_COLOR = GraphColors.COLOR_2;

    /*package*/ MemoryIndicatorPanel() {
        max = 0;
    }

    protected String getTitle() {
        return NbBundle.getMessage(MemoryIndicatorPanel.class, "indicator.title"); // NOI18N
    }

    protected JComponent createGraph() {
        if (graph == null) {
            graph = new Graph(100, new GraphDescriptor(GRAPH_COLOR, NbBundle.getMessage(getClass(), "graph.description")));
            graph.setBorder(BorderFactory.createLineBorder(GraphColors.BORDER_COLOR));
            graph.setMinimumSize(new Dimension(66, 32));
            graph.setPreferredSize(new Dimension(150, 80));
        }
        return graph;
    }

    protected JComponent createLegend() {
        if (legend == null) {
            measureKb = NbBundle.getMessage(getClass(), "measure.kb");
    //        measureMb = NbBundle.getMessage(getClass(), "measure.mb");

    //        JLabel tmpLabel = new JLabel();
    //        float fs = ((float) tmpLabel.getFont().getSize()) * 8f / 10f;
    //        Font smallFont = tmpLabel.getFont().deriveFont(fs);

            lblCurrLabel = new JLabel(NbBundle.getMessage(getClass(), "label.curr"));
    //        lblCurrLabel.setFont(smallFont);
            lblCurrValue = new JLabel(formatValue(0));
            lblCurrValue.setHorizontalAlignment(SwingConstants.RIGHT);
    //        lblCurrValue.setFont(smallFont);
            lblCurrMeasure = new JLabel(measureKb);
    //        lblCurrMeasure.setFont(smallFont);

            lblMaxLabel = new JLabel(NbBundle.getMessage(getClass(), "label.max"));
    //        lblMaxLabel.setFont(smallFont);
            lblMaxValue = new JLabel(formatValue(0));
    //        lblMaxValue.setFont(smallFont);
            lblMaxValue.setHorizontalAlignment(SwingConstants.RIGHT);
            lblMaxMeasure = new JLabel(measureKb);
    //        lblMaxMeasure.setFont(smallFont);

            lblCurrLabel.setForeground(GraphColors.TEXT_COLOR);
            lblCurrMeasure.setForeground(GraphColors.TEXT_COLOR);
            lblCurrValue.setForeground(GraphColors.TEXT_COLOR);
            lblMaxLabel.setForeground(GraphColors.TEXT_COLOR);
            lblMaxMeasure.setForeground(GraphColors.TEXT_COLOR);
            lblMaxValue.setForeground(GraphColors.TEXT_COLOR);

            legend = new JPanel(new GridBagLayout());
            legend.setBackground(Color.WHITE);
            legend.setBorder(BorderFactory.createLineBorder(GraphColors.BORDER_COLOR));
            legend.setMinimumSize(new Dimension(100, 80));
            legend.setPreferredSize(new Dimension(100, 80));

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(0, 6, 0, 0);
            c.gridy = 0;
            c.gridx = 0;
            legend.add(lblCurrLabel, c);
            c.gridx = 1;
            legend.add(lblCurrValue, c);
            c.gridx = 2;
            legend.add(lblCurrMeasure, c);

            c.gridy = 1;
            c.gridx = 0;
            legend.add(lblMaxLabel, c);
            c.gridx = 1;
            legend.add(lblMaxValue, c);
            c.gridx = 2;
            legend.add(lblMaxMeasure, c);
        }
        return legend;
    }

    public void setValue(long longValue) {
        int value = (int) (longValue / 1000 + (longValue % 1000 >= 500 ? 1 : 0));
        if (value > max) {
            max = value;
            lblMaxValue.setText(formatValue(value));
        }
        lblCurrValue.setText(formatValue(value));

        if (graph.getUpperLimit() < value) {
            graph.setUpperLimit(value * 3 / 2);
        }
        graph.addData(value);
    }
    
    private String formatValue(long value) {
        return String.format("%02d", value);
    }
}
