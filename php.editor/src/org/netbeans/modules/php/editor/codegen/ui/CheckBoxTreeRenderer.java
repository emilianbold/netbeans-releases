/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.php.editor.codegen.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

/**
 *
 * @author kuba
 */
public class CheckBoxTreeRenderer extends JPanel implements TreeCellRenderer {

    protected JCheckBox check;
    protected JLabel label;
    private static final JList LIST_FOR_COLORS = new JList();

    public CheckBoxTreeRenderer() {
        setLayout(new BorderLayout());
        setOpaque(true);
        this.check = new JCheckBox();
        this.label = new JLabel();
        add(check, BorderLayout.WEST);
        add(label, BorderLayout.CENTER);
        check.setOpaque(false);
        label.setOpaque(false);
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean isSelected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        String stringValue = tree.convertValueToText(value, isSelected,
                expanded, leaf, row, hasFocus);
        setEnabled(tree.isEnabled());
        if (value instanceof CheckNode) {
            CheckNode n = (CheckNode) value;
            check.setSelected(n.isSelected());
            label.setIcon(new ImageIcon(n.getIcon())); // XXX Ask description directly
        }
        if (isSelected) {
            label.setForeground(LIST_FOR_COLORS.getSelectionForeground());
            setOpaque(true);
            setBackground(LIST_FOR_COLORS.getSelectionBackground());
        } else {
            label.setForeground(tree.getForeground());
            setOpaque(false);
        }
        label.setText(stringValue);
        return this;
    }
}  