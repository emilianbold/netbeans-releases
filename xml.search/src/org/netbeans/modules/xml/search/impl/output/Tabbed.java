/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.xml.search.impl.output;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import static org.netbeans.modules.xml.util.UI.*;

// import java.awt.BasicStroke;
// import java.awt.Dimension;
// import java.awt.FlowLayout;
// import java.awt.Graphics;
// import java.awt.Graphics2D;
// import javax.swing.AbstractButton;
// import javax.swing.BorderFactory;
// import javax.swing.JButton;
// import javax.swing.JLabel;
// import javax.swing.JPanel;
// import javax.swing.plaf.basic.BasicButtonUI;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2008.02.21
 */
final class Tabbed extends JTabbedPane {

    Tabbed() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    showPopupMenu(event, event.getX(), event.getY());
                }
            }
        });
    }

    void addTrees(Tree list, Tree tree) {
        String title = tree.toString();
        addTab(title, new Panel(list, tree));
        // JDK 6: setTabComponentAt(getTabCount() - 1, new Tab(title));
        setSelectedIndex(getTabCount() - 1);
    }

    private void showPopupMenu(ComponentEvent event, int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;

        item = createItem("LBL_Close_Tab", new ActionListener() { // NOI18N
            public void actionPerformed(ActionEvent event) {
                closeTab();
            }
        });
        popup.add(item);

        item = createItem("LBL_Close_All_Tabs", new ActionListener() { // NOI18N
            public void actionPerformed(ActionEvent event) {
                closeAllTabs();
            }
        });
        popup.add(item);

        item = createItem("LBL_Close_Other_Tabs", new ActionListener() { // NOI18N
            public void actionPerformed(ActionEvent event) {
                closeOtherTabs();
            }
        });
        popup.add(item);
        popup.show(event.getComponent(), x, y);
    }

    private void closeTab() {
        int i = getSelectedIndex();

        if (i != -1) {
            remove(i);
        }
    }

    public void closeAllTabs() {
        removeAll();
    }

    private void closeOtherTabs() {
        Component current = getSelectedComponent();
        Component[] other = getComponents();

        for (int i = 0; i < other.length; i++) {
            if (other[i] != current) {
                remove(other[i]);
            }
        }
    }

    private JMenuItem createItem(String name, ActionListener listener) {
        JMenuItem item = new JMenuItem(i18n(Tabbed.class, name));
        item.addActionListener(listener);
        return item;
    }

/* TO DO JDK 6
    // -------------------------------
    private class Tab extends JPanel {

        Tab(final String title) {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            setBorder(BorderFactory.createEmptyBorder(TINY_SIZE, 0, 0, 0));
            setOpaque(false);

            JLabel label = new JLabel(title);
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, MEDIUM_SIZE));
            add(label);
            add(new TabButton());
        }

        // ----------------------------------------------------------------
        private class TabButton extends JButton implements ActionListener {
            public TabButton() {
                final int size = 17;
                setPreferredSize(new Dimension(size, size));
                setToolTipText(org.netbeans.modules.xml.util.UI.i18n(Tabbed.class, "LBL_Close_Tab")); // NOI18N
                setUI(new BasicButtonUI());
                setContentAreaFilled(false);
                setFocusable(false);
                setBorder(BorderFactory.createEtchedBorder());
                setBorderPainted(false);

                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent event) {
                        Component component = event.getComponent();

                        if (component instanceof AbstractButton) {
                            ((AbstractButton) component).setBorderPainted(true);
                        }
                    }

                    public void mouseExited(MouseEvent event) {
                        Component component = event.getComponent();

                        if (component instanceof AbstractButton) {
                            ((AbstractButton) component).setBorderPainted(false);
                        }
                    }
                });

                setRolloverEnabled(true);
                addActionListener(this);
            }

            public void actionPerformed(ActionEvent e) {
                int i = Tabbed.this.indexOfTabComponent(Tab.this);

                if (i != -1) {
                    Tabbed.this.remove(i);
                }
            }

            public void updateUI() {}

            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();

                if (getModel().isPressed()) {
                    g.translate(1, 1);
                }
                g.setStroke(new BasicStroke(2));
                final int delta = 6;

                g.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
                g.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
                g.dispose();
            }
        }
    }
*/
}
