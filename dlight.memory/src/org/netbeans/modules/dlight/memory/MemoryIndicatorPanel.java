package org.netbeans.modules.dlight.memory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.modules.dlight.indicators.graph.Graph;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.openide.util.NbBundle;

class MemoryIndicatorPanel extends JPanel {

    private final Graph graph;
    private final JLabel lblMaxLabel;
    private final JLabel lblMaxValue;
    private final JLabel lblMaxMeasure;
    private final JLabel lblCurrLabel;
    private final JLabel lblCurrValue;
    private final JLabel lblCurrMeasure;
    private final String measureKb;
//    private final String measureMb;
    private long max;
    private Color graphColor = new Color(0, 128, 128);

    MemoryIndicatorPanel() {

        max = 0;

        Color borderColor = new Color(0x77, 0x88, 0x88);

        measureKb = NbBundle.getMessage(getClass(), "measure.kb");
//        measureMb = NbBundle.getMessage(getClass(), "measure.mb");

        graph = new Graph(100, new GraphDescriptor(graphColor, NbBundle.getMessage(getClass(), "graph.deccription")));
        graph.setBorder(BorderFactory.createLineBorder(borderColor));
        graph.setMinimumSize(new Dimension(66, 32));
        graph.setPreferredSize(new Dimension(150, 80));

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

        Color lblColor = graphColor.darker();
        lblCurrLabel.setForeground(lblColor);
        lblCurrMeasure.setForeground(lblColor);
        lblCurrValue.setForeground(lblColor);
        lblMaxLabel.setForeground(lblColor);
        lblMaxMeasure.setForeground(lblColor);
        lblMaxValue.setForeground(lblColor);

//        lblCurrLabel.setBorder(BorderFactory.createLineBorder(Color.RED));
//        lblCurrMeasure.setBorder(BorderFactory.createLineBorder(Color.BLUE));
//        lblCurrValue.setBorder(BorderFactory.createLineBorder(Color.CYAN));
//        lblMaxLabel.setBorder(BorderFactory.createLineBorder(Color.RED));
//        lblMaxMeasure.setBorder(BorderFactory.createLineBorder(Color.BLUE));
//        lblMaxValue.setBorder(BorderFactory.createLineBorder(Color.CYAN));

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(graph);

        JPanel legend = new JPanel(new GridBagLayout());
        legend.setBackground(Color.WHITE);
        legend.setBorder(BorderFactory.createLineBorder(borderColor));
        legend.setMinimumSize(new Dimension(120, 32));
        legend.setMaximumSize(new Dimension(120, Integer.MAX_VALUE));
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

        add(legend);
    }

    private void add(JComponent parent, JComponent child, GridBagConstraints c, int gridx, int gridy) {
        c.gridx = gridx;
        c.gridy = gridy;
        parent.add(child, c);
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
