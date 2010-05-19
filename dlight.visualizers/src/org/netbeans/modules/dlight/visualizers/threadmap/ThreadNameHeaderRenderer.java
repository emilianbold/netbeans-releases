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
package org.netbeans.modules.dlight.visualizers.threadmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Alexander Simon
 */
public class ThreadNameHeaderRenderer extends JPanel implements TableCellRenderer, Serializable {

    private static final boolean CHANGE_BUTTON = false;

    private ThreadsPanel viewManager; // view manager for this header
    private JLabel label;
    private JLabel shift;
    private Rectangle shiftRect;

    public ThreadNameHeaderRenderer(ThreadsPanel aViewManager) {
        viewManager = aViewManager;
        setLayout(new BorderLayout());
        label = new JLabel(ThreadsPanel.THREAD_NAME_ID);
        add(label, BorderLayout.CENTER);
        shiftRect = null;
        if (CHANGE_BUTTON) {
            shift = new JLabel(" > "); //NOI18N
            shift.setForeground(Color.blue);
            add(shift, BorderLayout.EAST);
        }
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JPanel component = this;
        component.setBackground(Color.WHITE);
        label.setFont(table.getFont().deriveFont(Font.BOLD));
        if (CHANGE_BUTTON) {
            shift.setFont(table.getFont().deriveFont(Font.BOLD));
        }

        component.setBorder(new javax.swing.border.EmptyBorder(0, 3, 0, 3));
        switch (viewManager.getThreadNameFormat()){
            case 1:
                label.setText(ThreadsPanel.THREAD_NAME_CALLEE);
                break;
            case 2:
                label.setText(ThreadsPanel.THREAD_NAME_CALLEE_CALLEE);
                break;
            default:
                label.setText(ThreadsPanel.THREAD_NAME_ID);
                break;
        }
        if (column == viewManager.getSortedColumn() && viewManager.getSortedOrder() != 0) {
            label.setIcon(getProperIcon(viewManager.getSortedOrder() == -1));
        } else {
            label.setIcon(null);
        }
        return this;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (shiftRect != null) {
            if (shiftRect.getX() <= event.getX() && event.getX() <= shiftRect.getX()+shiftRect.getWidth()) {
                return ThreadsPanel.THREAD_NAME_FORMAT_TOOL_TIP;
            }
        }
        return super.getToolTipText(event);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (CHANGE_BUTTON) {
            shiftRect = shift.getBounds();
        }
    }

    Rectangle getFormatRectangle() {
        return shiftRect;
    }

    private ImageIcon getProperIcon(boolean descending) {
        if (descending) {
            return new ImageIcon(ThreadsPanel.class.getResource("/org/netbeans/modules/dlight/visualizers/threadmap/resources/columnsSortedDesc.png")); // NOI18N
        } else {
            return new ImageIcon(ThreadsPanel.class.getResource("/org/netbeans/modules/dlight/visualizers/threadmap/resources/columnsSortedAsc.png")); // NOI18N
        }
    }
}
