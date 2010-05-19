/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.soa.ldap.browser.compact;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import org.netbeans.modules.soa.ldap.LDAPConnection;
import org.netbeans.modules.soa.ldap.browser.IconPool;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class LDAPCompactBrowser extends JPanel implements ActionListener {

    private Divider divider;

    private JPanel content;

    private IntroPanel introPanel;

    private boolean expanded = false;
    private int preferredWidth = 256;

    private JButton backButton;
    private JLabel currentConnectionLabel;
    private JToolBar toolBar;

    private BrowserContent browserContent;
    
    public LDAPCompactBrowser() {
        divider = new Divider();

        content = new JPanel(new BorderLayout());

        introPanel = new IntroPanel(this);

        setLayout(new BorderLayout());
        add(divider, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        content.add(introPanel, BorderLayout.CENTER);

        backButton = new JButton("Back");
        currentConnectionLabel = new JLabel();
        currentConnectionLabel.setMinimumSize(new Dimension(24, 16));

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(TOOLBAR_BORDER);
        toolBar.add(backButton);
        toolBar.addSeparator();
        toolBar.add(currentConnectionLabel);

        backButton.setIcon(IconPool.loadImageIcon("back")); // NOI18N
        backButton.setText(NbBundle.getMessage(LDAPCompactBrowser.class, 
                "LDAPCompactBrowser.backButton.text")); // NOI18N
        backButton.addActionListener(this);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        boolean oldExpanded = this.expanded;

        if (oldExpanded != expanded) {
            this.expanded = expanded;
            content.setVisible(expanded);
            revalidate();
            repaint();

            firePropertyChange(EXPANDED_RPOPERTY, oldExpanded, expanded);
        }
    }

    public void back() {
        if (browserContent != null) {
            browserContent.close();
            
            content.remove(browserContent);
            content.remove(toolBar);
            
            content.add(introPanel);
            
            browserContent = null;

            content.revalidate();
            content.repaint();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        int halfOfParentWidth = getParent().getWidth() / 2;

        if (halfOfParentWidth > 0) {
            size.width = Math.min(halfOfParentWidth, preferredWidth);
        } else {
            size.width = preferredWidth;
        }

        if (expanded) {
            size.width = Math.max(MIN_WIDTH, size.width);
        } else {
            size.width = COLLAPSED_WIDTH;
        }
        
        return size;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            back();
        } else { // connect button
            browserContent = new BrowserContent(this, 
                    introPanel.getLDAPConnection(),
                    introPanel.getUserNameAttribute(),
                    introPanel.getGroupNameAttribute());

            content.remove(introPanel);
            content.add(browserContent, BorderLayout.CENTER);
            content.add(toolBar, BorderLayout.NORTH);

            LDAPConnection connection = introPanel.getLDAPConnection();

            currentConnectionLabel.setText(connection.toString());

            content.revalidate();
            content.repaint();

            browserContent.open();
        }
    }

    private class Divider extends JPanel implements MouseListener,
            MouseMotionListener
    {
        int dx = 0;

        boolean drag = false;
        
        public Divider() {
            setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(DIVIDER_WIDTH, 1);
        }

        @Override
        protected void paintBorder(Graphics g) {
            int w = getWidth();
            int h = getHeight();

            Color oldColor = g.getColor();
            Color backgroundColor = getBackground();

            g.setColor(backgroundColor.brighter());
            g.drawLine(1, 0, 1, h - 1);

            g.setColor(backgroundColor.darker());
            g.drawLine(0, 0, 0, h - 1);
            g.drawLine(w - 1, 0, w - 1, h - 1);

            g.setColor(oldColor);
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            Point point = e.getPoint();
            dx = point.x;
            drag = true;
        }

        public void mouseReleased(MouseEvent e) {
            drag = false;
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            Point point = e.getPoint();
            preferredWidth = LDAPCompactBrowser.this.getParent().getWidth()
                    - (convertPoint(point).x - dx);

            if (preferredWidth < MIN_WIDTH) {
                preferredWidth = MIN_WIDTH;
                setExpanded(false);
            } else {
                setExpanded(true);
            }

            LDAPCompactBrowser.this.revalidate();
        }

        public void mouseMoved(MouseEvent e) {

        }

        private Point convertPoint(Point point) {
            point.x += LDAPCompactBrowser.this.getX();
            return point;
        }
    }

    private static final Border TOOLBAR_BORDER = new Border() {
        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height)
        {
            Color oldColor = g.getColor();
            Color background = c.getBackground();

            g.setColor(background.brighter());
            g.drawLine(x, y, x + width - 1, y);
            g.drawLine(x, y + 1, x, y + height - 2);

            g.setColor(background.darker());
            g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);

            g.setColor(oldColor);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }

        public boolean isBorderOpaque() {
            return false;
        }
    };

    private static final int MIN_WIDTH = 140;
    private static final int DIVIDER_WIDTH = 7;
    private static final int COLLAPSED_WIDTH = DIVIDER_WIDTH - 1;

    public static final String EXPANDED_RPOPERTY
            = "ExpandedcCompactLDAPBrowserProperty"; // NOI18N
}
