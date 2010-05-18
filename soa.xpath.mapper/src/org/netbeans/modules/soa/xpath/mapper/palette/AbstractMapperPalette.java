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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.soa.xpath.mapper.palette;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Graphics;

import java.awt.event.MouseMotionAdapter;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.netbeans.modules.soa.xpath.mapper.context.MapperStaticContext;
import org.netbeans.modules.soa.xpath.mapper.utils.XPathMapperUtils;


/**
 * @author Vladimir Yaroslavskiy
 * @author Nikita Krjukov
 * @version 2009.09.17
 */
public abstract class AbstractMapperPalette {

    public AbstractMapperPalette(MapperStaticContext staticContext) {
        mStaticContext = staticContext;
    }

    public JPanel getPanel() {
        initContent();
        return mContent;
    }

    public JMenuBar getMenuBar() {
        initContent();
        return mBar;
    }

    protected void initContent() {
        if (mContent != null) {
            return;
        }
        //
        mContent = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 0.0;

        c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        mBar = createMenuBar();
        mContent.add(mBar, c);

        mContent.setBorder(new Border());
        mContent.setOpaque(true);
        mContent.addMouseMotionListener(new MouseMotionAdapter() {
        });
    }

    protected abstract JMenuBar createMenuBar();

    public void hideMenu() {
        for (int i = 0; i < mBar.getMenuCount(); i++) {
            JMenu menu = mBar.getMenu(i);

            if (menu.isSelected()) {
                menu.setSelected(false);
                menu.getPopupMenu().setVisible(false);
                return;
            }
        }
    }

    public MapperStaticContext getStaticContext() {
        return mStaticContext;
    }

    // ---------------------------------------------------------------
    private static class Border implements javax.swing.border.Border {

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color color = g.getColor();
            g.setColor(c.getBackground().darker());
            y += height - 1;
            g.drawLine(x, y, x + width - 1, y);
            g.setColor(color);
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 1, 0);
        }

        public boolean isBorderOpaque() {
            return true;
        }
    }

    protected JMenu createMenu(Class bundleOownerClass, String key, String icon) {
        String name = XPathMapperUtils.i18n(bundleOownerClass, key);
        JMenu menu = new MyMenu(name);
        menu.setToolTipText(name);
        menu.setIcon(XPathMapperUtils.icon(bundleOownerClass, icon));
        return menu;
    }

    // ----------------------------------------
    private static class MyMenu extends JMenu {

        MyMenu(String name) {
            super(name);
        }

        @Override
        public String getToolTipText() {
            Dimension size = getPreferredSize();

            if (getWidth() < size.width) {
                return super.getToolTipText();
            }
            return null;
        }
    }

    private JPanel mContent;
    private JMenuBar mBar;
    private MapperStaticContext mStaticContext;
}
