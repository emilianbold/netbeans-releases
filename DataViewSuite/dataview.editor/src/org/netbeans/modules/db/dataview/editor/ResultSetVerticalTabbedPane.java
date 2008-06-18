/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.db.dataview.editor;

import org.netbeans.modules.db.dataview.output.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.awt.MouseUtils;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.Utilities;

/**
 * Top component which displays various output panel
 * 
 * @author Ahimanikya Satapathy
 * @author Nithya Radhakrishnan
 */
public final class ResultSetVerticalTabbedPane extends JPanel {

    private JTabbedPane tabbedPane = TabbedPaneFactory.createCloseButtonTabbedPane();
    private PopupListener listener;
    private ChangeListener listen;
    private JPopupMenu pop;
    private CloseListener closeL;
    private DataViewOutputPanel lastKnownSelection = null;
    private DataViewOutputPanel newSelection;
    private JToolBar verticalBar;

    private ResultSetVerticalTabbedPane() {
        initComponents();
        setLayout(new BorderLayout());

        setFocusable(true);
        setBackground(UIManager.getColor("text")); //NOI18N

        // create it but don't add it yet...
        verticalBar = new JToolBar(JToolBar.VERTICAL);
        verticalBar.setLayout(new BoxLayout(verticalBar, BoxLayout.Y_AXIS));
        verticalBar.setFloatable(false);

        Insets ins = verticalBar.getMargin();
        JButton sample = new JButton();
        sample.setBorderPainted(false);
        sample.setOpaque(false);
        sample.setText(null);
        sample.setIcon(new Icon() {

            public int getIconHeight() {
                return 16;
            }

            public int getIconWidth() {
                return 16;
            }

            public void paintIcon(Component c, Graphics g, int x, int y) {
            }
        });
        verticalBar.add(sample);
        Dimension buttonPref = sample.getPreferredSize();
        Dimension minDim = new Dimension(buttonPref.width + ins.left + ins.right, buttonPref.height + ins.top + ins.bottom);
        verticalBar.setMinimumSize(minDim);
        verticalBar.setPreferredSize(minDim);
        verticalBar.remove(sample);
        verticalBar.setBorder(new VariableRightBorder(tabbedPane));
        verticalBar.setBorderPainted(true);

        pop = new JPopupMenu();
        pop.add(new Close());
        pop.add(new CloseAll());
        pop.add(new CloseAllButCurrent());
        listener = new PopupListener();
        closeL = new CloseListener();
        listen = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof JTabbedPane) {
                    JTabbedPane jp = ((JTabbedPane) e.getSource());
                    newSelection = (DataViewOutputPanel) jp.getSelectedComponent();
                    fire(lastKnownSelection, newSelection);
                }
            }
        };

    }
    String nbBundle1 = "Close Tab";
    String nbBundle2 = "Close All Tabs";
    String nbBundle3 = "Close Other Tabs";

    private class Close extends AbstractAction {

        public Close() {
            super(nbBundle1);
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getComponentCount() > 0) {
                removePanel(tabbedPane.getSelectedComponent());
            }

        }
    }

    private final class CloseAll extends AbstractAction {

        public CloseAll() {
            super(nbBundle2);
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getComponentCount() > 0) {
                closeAll(tabbedPane);
            }
            removeAll();
        }
    }

    private class CloseAllButCurrent extends AbstractAction {

        public CloseAllButCurrent() {
            super(nbBundle3);
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getComponentCount() > 0) {
                closeAllButCurrent(tabbedPane);
            }
        }
    }

    void closeAllButCurrent(JTabbedPane tabs) {
        Component current = tabs.getSelectedComponent();
        for (Component comp : tabs.getComponents()) {
            if (comp != current) {
                removePanel(comp);
            }
        }
    }

    void closeAll(JTabbedPane tabs) {
        for (Component comp : tabs.getComponents()) {
            removePanel(comp);
        }
        revalidate();
    }

    private class CloseListener implements PropertyChangeListener {

        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                removePanel((Component) evt.getNewValue());
            }
        }
    }

    private class PopupListener extends MouseUtils.PopupMouseAdapter {

        /**
         * Called when the sequence of mouse events should lead to actual showing popup menu
         */
        @Override
        protected void showPopup(MouseEvent e) {
            pop.show(ResultSetVerticalTabbedPane.this, e.getX(), e.getY());
        }
    }

    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(0, 300, Short.MAX_VALUE));
    }

    public void removePanel(Component panel) {
        if (tabbedPane.getComponentCount() == 0) {
            remove(panel);
        } else {
            tabbedPane.remove(panel);
            if (tabbedPane.getComponentCount() == 1) {
                Component c = tabbedPane.getSelectedComponent();
                lastKnownSelection = (DataViewOutputPanel) c;
                tabbedPane.removeMouseListener(listener);
                tabbedPane.removePropertyChangeListener(closeL);
                remove(tabbedPane);
                add(c, BorderLayout.CENTER);
            }
        }
        revalidate();
    }

    public void addPanel(Component panel) {
        if (getComponentCount() == 0) {
            add(panel, BorderLayout.CENTER);
            if (panel instanceof DataViewOutputPanel) {
                lastKnownSelection = (DataViewOutputPanel) panel;
                verticalBar.removeAll();
                JButton[] btns = ((DataViewOutputPanel) panel).getVerticalToolBar();
                for (JButton btn : btns) {
                    if (btn != null) {
                        verticalBar.add(btn);
                    }
                }
                add(verticalBar, BorderLayout.WEST);
            }
        } else if (tabbedPane.getComponentCount() == 0 && lastKnownSelection != panel) {
            Component comp = (Component) lastKnownSelection;
            remove(comp);
            tabbedPane.addMouseListener(listener);
            tabbedPane.addPropertyChangeListener(closeL);
            tabbedPane.addChangeListener(listen);
            add(tabbedPane, BorderLayout.CENTER);

            tabbedPane.addTab(comp.getName().substring(0, 20) + "...", null, comp, comp.getName()); //NOI18N

            tabbedPane.addTab(panel.getName().substring(0, 20) + "...", null, panel, panel.getName()); //NOI18N

            tabbedPane.setSelectedComponent(panel);
            tabbedPane.validate();
        } else if (lastKnownSelection != panel) {
            tabbedPane.addTab(panel.getName().substring(0, 20) + "...", null, panel, panel.getName()); //NOI18N

            tabbedPane.setSelectedComponent(panel);
            tabbedPane.validate();
        }
        validate();
    }

    private void fire(DataViewOutputPanel formerSelection, DataViewOutputPanel selection) {
        if (formerSelection != selection && selection != null) {
            lastKnownSelection = selection;
            setToolbarButtons(selection.getVerticalToolBar());
        } else if (lastKnownSelection != null) {
            setToolbarButtons(lastKnownSelection.getVerticalToolBar());
        }
    }

    private void setToolbarButtons(JButton[] buttons) {
        verticalBar.removeAll();
        for (JButton btn : buttons) {
            if (btn != null) {
                verticalBar.add(btn);
            }
        }
        verticalBar.repaint();
        verticalBar.validate();
    }

    private class VariableRightBorder implements Border {

        private JTabbedPane pane;

        public VariableRightBorder(JTabbedPane pane) {
            this.pane = pane;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color old = g.getColor();
            g.setColor(getColor());
            g.drawLine(x + width - 1, y, x + width - 1, y + height);
            g.setColor(old);
        }

        public Color getColor() {
            if (Utilities.isMac()) {
                Color c1 = UIManager.getColor("controlShadow");
                Color c2 = UIManager.getColor("control");
                return new Color((c1.getRed() + c2.getRed()) / 2, (c1.getGreen() + c2.getGreen()) / 2, (c1.getBlue() + c2.getBlue()) / 2);
            } else {
                return UIManager.getColor("controlShadow");
            }
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 2);
        }

        public boolean isBorderOpaque() {
            return true;
        }
    }
}
