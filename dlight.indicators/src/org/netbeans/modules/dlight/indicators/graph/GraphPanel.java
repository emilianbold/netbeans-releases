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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.netbeans.modules.dlight.util.ui.DLightUIPrefs;
import org.openide.awt.Actions;

/**
 * Convenient base class for indicator components.
 *
 * @author Alexey Vladykin
 */
public class GraphPanel<G extends JComponent, L extends JComponent> extends JLayeredPane {

    private static final Color BORDER_COLOR = DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_BORDER_COLOR);
    private static final int PADDING = DLightUIPrefs.getInt(DLightUIPrefs.INDICATOR_PADDING);
    private final G graph;
    private final L legend;
    private final JComponent hAxis;
    private final JComponent vAxis;
    private final JButton button;
    private List<Action> actions;
    private final JComponent graphPanel;
    private JComponent overlay;

    public GraphPanel(String title, G graph, L legend, JComponent hAxis, JComponent vAxis, JButton button) {
        this.graph = graph;
        this.legend = legend;
        this.hAxis = hAxis;
        this.vAxis = vAxis;
        this.button = button;
        this.graphPanel = createGraphPanel(title, graph, legend, hAxis, vAxis, button);
        graphPanel.addMouseListener(new PopupMenuListener());
        setOpaque(true); // otherwise background is white
        setMinimumSize(graphPanel.getMinimumSize());
        setPreferredSize(graphPanel.getPreferredSize());
        add(graphPanel, Integer.valueOf(0));
    }

    public void setPopupActions(List<Action> actions) {
        this.actions = actions;
    }

    @Override
    public void setToolTipText(String text) {
        graphPanel.setToolTipText(text);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        return graphPanel.getToolTipText();
    }

    private static JPanel createGraphPanel(String title, JComponent graph,
            JComponent legend, JComponent hAxis, JComponent vAxis, JButton button) {
        JPanel graphPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c;

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(title);
        label.setFont(DLightUIPrefs.getFont(DLightUIPrefs.INDICATOR_TITLE_FONT));
        label.setForeground(DLightUIPrefs.getColor(DLightUIPrefs.INDICATOR_TITLE_FONT_COLOR));
        c = new GridBagConstraints();
        topPanel.add(label, BorderLayout.CENTER);

        if (button != null) {
            topPanel.add(button, BorderLayout.EAST);
        }

        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(PADDING, PADDING, 0, PADDING);
        graphPanel.add(topPanel, c);

        if (vAxis != null) {
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.VERTICAL;
            c.weighty = 1.0;
            c.insets = new Insets(PADDING / 2, PADDING, hAxis == null ? PADDING : 0, 0);
            graphPanel.add(vAxis, c);
        }

        graph.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(PADDING / 2, vAxis == null ? PADDING : 0, hAxis == null ? PADDING : 0, 0);
        graphPanel.add(graph, c);

        legend.setBackground(Color.WHITE);
        legend.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 1.0;
        c.insets = new Insets(PADDING / 2, -1, hAxis == null ? PADDING : 0, PADDING);
        graphPanel.add(legend, c);

        if (hAxis != null) {
            c = new GridBagConstraints();
            c.gridx = vAxis == null ? 0 : 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.insets = new Insets(0, vAxis == null ? PADDING : 0, PADDING, 0);
            graphPanel.add(hAxis, c);
        }



        return graphPanel;
    }

    private JPopupMenu createPopupMenu() {
        if (actions == null || actions.isEmpty()) {
            return null;
        }
        JPopupMenu pm = new JPopupMenu();
        for (Action action : actions) {
            JMenuItem menuItem = new JMenuItem();
            Actions.connect(menuItem, action, true); // Actions.connect() takes care of mnemonics
            pm.add(menuItem);
        }
        return pm;

    }

    private class PopupMenuListener extends MouseAdapter{

        PopupMenuListener() {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JPopupMenu pm = createPopupMenu();
                if (pm != null) {
                    pm.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JPopupMenu pm = createPopupMenu();
                if (pm != null) {
                    pm.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }




    }

    protected final G getGraph() {
        return graph;
    }

    protected final L getLegend() {
        return legend;
    }

    protected final JComponent getHorizontalAxis() {
        return hAxis;
    }

    protected final JComponent getVerticalAxis() {
        return vAxis;
    }

    @Override
    public void doLayout() {
        Insets insets = getInsets();
        graphPanel.setBounds(insets.left, insets.top,
                getWidth() - insets.left - insets.right,
                getHeight() - insets.top - insets.bottom);
        synchronized (this) {
            if (overlay != null) {
                graphPanel.validate();
                Rectangle rect = graph.getBounds();
                overlay.setBounds(
                        insets.left + rect.x + PADDING / 2,
                        insets.top + rect.y + PADDING / 2,
                        rect.width - PADDING, rect.height - PADDING);
            }
        }
    }

    @Override
    public synchronized void setEnabled(boolean enabled) {
        graph.setEnabled(enabled);
        legend.setEnabled(enabled);
        if (vAxis != null) {
            vAxis.setEnabled(enabled);
        }
        if (hAxis != null) {
            hAxis.setEnabled(enabled);
        }
        if (button != null) {
            button.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    public final synchronized void setOverlay(JComponent overlay) {
        if (this.overlay == overlay) {
            return;
        }
        if (this.overlay != null) {
            remove(this.overlay);
        }
        if (overlay != null) {
            add(overlay, Integer.valueOf(1));
        }
        this.overlay = overlay;
        revalidate();
        repaint();
    }
}
