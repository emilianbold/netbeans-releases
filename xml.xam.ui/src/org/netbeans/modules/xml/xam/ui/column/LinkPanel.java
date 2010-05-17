/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.xml.xam.ui.column;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Contains and manages the link buttons that act as bread crumbs
 * for quickly browsing an instance of ColumnView.
 *
 * @author  Nathan Fiedler
 */
public class LinkPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;
    /** Occupies space to the right of the buttons. */
    private Component layoutFiller;
    /** Mapping of buttons to columns. */
    private Map<LinkButton, Column> buttonColumnMap;
    /** Scroll pane to manage, if non-null. */
    private JScrollPane scrollPane;
    /** The column view to scroll. */
    private ColumnView columnView;
    /** The container for the links. */
    private JPanel linkPanel;
    private static final int ICON_WIDTH = 11;
    private static final int ICON_HEIGHT = 11;
    private static final int[] xpoints = new int[20];
    private static final int[] ypoints = new int[20];

    /**
     * Creates a new instance of LinkPanel.
     *
     * @param  view  the column view to scroll.
     */
    public LinkPanel(ColumnView view) {
        super(new BorderLayout());
        linkPanel = new JPanel(new GridBagLayout());
        scrollPane = new JScrollPane(linkPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        columnView = view;
        // Try to use the toolbar border defined in NetBeans core.
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        if (b == null) {
            // But, fall back on having something rather than nothing.
            b = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK);
        }
        setBorder(b);
        buttonColumnMap = new HashMap<LinkButton, Column>();

        // Configure the scrolling buttons.
        JButton left = new TimerButton(new ScrollLeftAction(scrollPane));
        JButton right = new TimerButton(new ScrollRightAction(scrollPane));
        configureButton(left, new LeftIcon());
        configureButton(right, new RightIcon());
        left.setPreferredSize(new Dimension(17, 17));
        right.setPreferredSize(new Dimension(17, 17));
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
        buttonPanel.setBorder(new EmptyBorder(0, 3, 1, 2));
        buttonPanel.add(left);
        buttonPanel.add(right);
        add(buttonPanel, BorderLayout.EAST);
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof LinkButton) {
            LinkButton button = (LinkButton) src;
            // Scroll the link button to the center, which is in
            // addition to the default scroll pane behavior of making
            // the focused component visible.
            if (scrollPane != null) {
                JViewport vp = scrollPane.getViewport();
                Rectangle visRect = vp.getViewRect();
                Rectangle compRect = button.getBounds();
                Component view = vp.getView();
                visRect.x = Math.max(0, Math.min(compRect.x -
                        (visRect.width - compRect.width) / 2,
                        view.getWidth() - visRect.width));
                vp.scrollRectToVisible(visRect);
            }
            // Scroll to the corresponding column.
            Column column = buttonColumnMap.get(button);
            columnView.scrollToColumn(column, true);
        }
    }

    /**
     * Adds a link button to the panel for the given column.
     *
     * @param  column  Column for which to add button.
     */
    public void appendLink(Column column) {
        if (linkPanel.getComponentCount() > 0) {
            // There are other links, we need to add '>' now.
            GridBagConstraints gbc = new GridBagConstraints();
            // Pad for five pixels on either side, taking account
            // of the 3 pixel inset of the button (below). Note that
            // > character has two blank pixels on the left.
            gbc.insets = new Insets(0, 3, 2, 0);
            linkPanel.add(new JLabel(">"), gbc); // NOI18N
        }
        LinkButton button = new LinkButton(column.getTitle());
        AccessibleContext ac = button.getAccessibleContext();
        ac.setAccessibleName(column.getTitle());
        ac.setAccessibleDescription(column.getDescription());
        button.addActionListener(this);
        buttonColumnMap.put(button, column);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 3, 0, 0);
        linkPanel.add(button, gbc);
        // Fill the space to the right so the links will be left-aligned.
        if (layoutFiller != null) {
            linkPanel.remove(layoutFiller);
        } else {
            layoutFiller = Box.createHorizontalGlue();
        }
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0d;
        linkPanel.add(layoutFiller, gbc);
        linkPanel.revalidate();
        linkPanel.repaint();
    }

    /**
     * Removes all of the links from the panel.
     */
    public void clearLinks() {
        linkPanel.removeAll();
        buttonColumnMap.clear();
        linkPanel.revalidate();
        linkPanel.repaint();
    }

    /**
     * Configure a button for the link panel.
     *
     * @param  button  the button.
     * @param  icon    icon for the button.
     */
    private static void configureButton(JButton button, Icon icon) {
        button.setIcon(icon);
        button.setMargin(null);
        button.setText(null);
        button.setFocusable(false);
    }

    /**
     * Remove the links from the panel, starting at the given offset.
     *
     * @param  index  link index from which to start removing.
     */
    public void truncateLinks(int index) {
        // Account for the layout filler, which we want to preserve.
        int count = linkPanel.getComponentCount() - 1;
        // Account for the separators between the buttons.
        index = index * 2 - 1;
        while (count > index) {
            Component child = linkPanel.getComponent(index);
            if (child instanceof LinkButton) {
                buttonColumnMap.remove((LinkButton) child);
            }
            linkPanel.remove(index);
            count--;
        }
        linkPanel.revalidate();
        linkPanel.repaint();
    }

    /**
     * Update the link title to reflect a change in the column.
     *
     * @param  column  Column for which to update link text.
     */
    public void updateLink(Column column) {
        Set<Entry<LinkButton, Column>> entries = buttonColumnMap.entrySet();
        Iterator<Entry<LinkButton, Column>> iter = entries.iterator();
        while (iter.hasNext()) {
            Entry<LinkButton, Column> entry = iter.next();
            if (entry.getValue().equals(column)) {
                LinkButton button = entry.getKey();
                button.setText(column.getTitle());
            }
        }
    }

    /**
     * Scrolls the link panel to the left.
     */
    private static class ScrollLeftAction extends AbstractAction implements
            ChangeListener {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;
        /** The pane to be scrolled. */
        private JScrollPane pane;

        /**
         * Creates a new instance of ScrollLeftAction.
         *
         * @param  pane  the scroll pane to manage.
         */
        public ScrollLeftAction(JScrollPane pane) {
            super();
            this.pane = pane;
            pane.getViewport().addChangeListener(this);
        }
        
        public void actionPerformed(ActionEvent e) {
            JViewport vp = pane.getViewport();
            Dimension size = vp.getExtentSize();
            Point p = vp.getViewPosition();
            p.x -= (size.width / 10);
            if (p.x < 0) {
                p.x = 0;
            }
            vp.setViewPosition(p);
        }

        public void stateChanged(ChangeEvent e) {
            JViewport vp = pane.getViewport();
            Point p = vp.getViewPosition();
            setEnabled(p.x > 0);
        }
    }

    /**
     * Scrolls the link panel to the right.
     */
    private static class ScrollRightAction extends AbstractAction implements
            ChangeListener {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;
        /** The pane to be scrolled. */
        private JScrollPane pane;

        /**
         * Creates a new instance of ScrollRightAction.
         *
         * @param  pane  the scroll pane to manage.
         */
        public ScrollRightAction(JScrollPane pane) {
            super();
            this.pane = pane;
            pane.getViewport().addChangeListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            JViewport vp = pane.getViewport();
            Dimension size = vp.getExtentSize();
            Point p = vp.getViewPosition();
            p.x += (size.width / 10);
            int max = vp.getViewSize().width - size.width;
            if (p.x > max) {
                p.x = max;
            }
            vp.setViewPosition(p);
        }

        public void stateChanged(ChangeEvent e) {
            JViewport vp = pane.getViewport();
            Dimension size = vp.getExtentSize();
            Point p = vp.getViewPosition();
            int max = vp.getViewSize().width - size.width;
            setEnabled(p.x < max);
        }
    }

    /**
     * Copied from core/swing/tabcontrol; paints a left arrow.
     */
    private static class LeftIcon implements Icon {

        public int getIconHeight() {
            return ICON_HEIGHT;
        }

        public int getIconWidth() {
            return ICON_WIDTH;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            y -= 2;
            g.setColor(c.isEnabled() ? c.getForeground() :
                UIManager.getColor("controlShadow")); //NOI18N
            int wid = getIconWidth();
            int hi = getIconHeight() + 1;
            xpoints[0] = x + (wid - 4);
            ypoints[0] = y + 2;
            xpoints[1] = xpoints[0];
            ypoints[1] = y + hi + 1;
            xpoints[2] = x + 2;
            ypoints[2] = y + (hi / 2) + 1;
            g.fillPolygon(xpoints, ypoints, 3);
        }
    }

    /**
     * Copied from core/swing/tabcontrol; paints a right arrow.
     */
    private static class RightIcon implements Icon {

        public int getIconWidth() {
            return ICON_WIDTH;
        }

        public int getIconHeight() {
            return ICON_HEIGHT - 2;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            y -= 2;
            g.setColor(c.isEnabled() ? c.getForeground() :
                UIManager.getColor("controlShadow")); //NOI18N
            int wid = getIconWidth();
            int hi = getIconHeight() + 1;
            xpoints[0] = x + 3;
            ypoints[0] = y + 1;
            xpoints[1] = x + 3;
            ypoints[1] = y + hi + 1;
            xpoints[2] = x + (wid - 4) + 1;
            ypoints[2] = y + (hi / 2) + 1;
            g.fillPolygon(xpoints, ypoints, 3);
        }
    }
}
