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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.mappercore.expressionbuilder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 *
 * @author anjeleevich
 */
class LeftPanel extends JPanel {
    private int preferredWidth = 256;

    private JComponent content;
    private JPanel divider;

    public LeftPanel(JComponent content) {
        this.content = content;

        divider = new JPanel();
        divider.setPreferredSize(new Dimension(6, 1));
        divider.setBorder(DIVIDER_BORDER);
        divider.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));

        MouseAdapter mouseAdapter = new MouseAdapter() {
            int initialX;

            @Override
            public void mousePressed(MouseEvent e) {
                initialX = e.getX();
            }

//            @Override
//            public void mouseDragged(MouseEvent e) {
//                preferredWidth = getWidth() + e.getX() - initialX;
//                revalidate();
//                repaint();
//            }
        };

        divider.addMouseListener(mouseAdapter);
//        divider.addMouseMotionListener(mouseAdapter);

        setLayout(new BorderLayout());
        add(content, BorderLayout.CENTER);
        add(divider, BorderLayout.EAST);
    }

    public JComponent getContent() {
        return content;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();

        size.width = Math.max(64, Math.min(preferredWidth,
                getParentContentWidth() - 128));

        return size;
    }

    private int getParentContentWidth() {
        Container parent = getParent();
        if (parent == null) {
            return 0;
        }

        Insets parentInsets = parent.getInsets();
        return parent.getWidth() - parentInsets.left - parentInsets.right;
    }

    private static final Border DIVIDER_BORDER = new Border() {
        public void paintBorder(Component c, Graphics g, int x, int y,
                int width, int height)
        {
            int x2 = x + width - 1;
            int y2 = y + height - 1;

            g.setColor(new Color(0xFFFFFF));
            g.drawLine(x + 1, y, x + 1, y2);

            g.setColor(new Color(0xCCCCCC));
            g.drawLine(x2 - 1, y, x2 - 1, y2);

            g.setColor(c.getBackground().darker());
            g.drawLine(x, y, x, y2);
            g.drawLine(x2, y, x2, y2);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 1, 0, 1);
        }

        public boolean isBorderOpaque() {
            return true;
        }
    };
}
