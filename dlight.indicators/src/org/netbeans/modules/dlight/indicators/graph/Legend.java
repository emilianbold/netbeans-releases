/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.indicators.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Graph legend.
 *
 * @author Alexey Vladykin
 */
public class Legend extends JPanel {

    public Legend(List<GraphDescriptor> graphs, List<DetailDescriptor> details) {
        super(new GridBagLayout());

        setBackground(GraphConfig.LEGEND_COLOR);
        setBorder(BorderFactory.createLineBorder(GraphConfig.BORDER_COLOR));
        setSize(new Dimension(80, 60));
        setMinimumSize(new Dimension(80, 60));
        setPreferredSize(new Dimension(80, 60));
        setOpaque(true);
        GridBagConstraints c;

        for (GraphDescriptor graph : graphs) {
            JLabel label = new JLabel(graph.getDisplayName(), new ColorIcon(graph.getColor()), SwingConstants.LEADING);
            label.setForeground(GraphConfig.TEXT_COLOR);
            label.setFont(label.getFont().deriveFont(10f));
            c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1.0;
            c.insets = new Insets(4, 4, 0, 4);
            add(label, c);
        }

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(Box.createVerticalStrut(4), c);

        if (details != null) {
            for (DetailDescriptor detail : details) {
                JLabel name = new JLabel(detail.getDisplayName());
                name.setForeground(GraphConfig.TEXT_COLOR);
                name.setFont(name.getFont().deriveFont(10f));
                c = new GridBagConstraints();
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.insets = new Insets(0, 4, 4, 4);
                add(name, c);
                JLabel value = new JLabel(detail.getDefaultValue());
                value.setName(detail.getName());
                value.setForeground(GraphConfig.TEXT_COLOR);
                value.setFont(value.getFont().deriveFont(Font.BOLD, 10f));
                c = new GridBagConstraints();
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.weightx = 1.0;
                c.insets = new Insets(0, 0, 4, 4);
                add(value, c);
            }
        }
    }

    public void updateDetail(final String name, final String value) {
        if (SwingUtilities.isEventDispatchThread()) {
            updateDetailImpl(name, value);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateDetailImpl(name, value);
                }
            });
        }
    }

    private void updateDetailImpl(String name, String value) {
        for (int i = 0; i < getComponentCount(); ++i) {
            Component comp = getComponent(i);
            if (comp instanceof JLabel && name.equals(comp.getName())) {
                ((JLabel)comp).setText(value);
                repaint();
                break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (isEnabled()) {
            super.paintComponent(g);
        } else {
            g.setColor(GraphConfig.LEGEND_COLOR);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    protected void paintChildren(Graphics g) {
        if (isEnabled()) {
            super.paintChildren(g);
        }
    }

    private static class ColorIcon implements Icon {

        private static final int WIDTH = 10;
        private static final int HEIGHT = 10;
        private final Color color;

        public ColorIcon(Color color) {
            this.color = color;
        }

        public int getIconWidth() {
            return WIDTH;
        }

        public int getIconHeight() {
            return HEIGHT;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(color);
            g2.fillRect(x, y, WIDTH - 1, HEIGHT - 1);
            g2.setPaint(GraphConfig.BORDER_COLOR);
            g2.drawRect(x, y, WIDTH - 1, HEIGHT - 1);
        }
    }
}
