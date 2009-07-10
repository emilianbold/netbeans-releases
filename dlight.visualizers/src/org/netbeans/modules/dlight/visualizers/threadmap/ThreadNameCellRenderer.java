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
package org.netbeans.modules.dlight.visualizers.threadmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;

/**
 * A table cell renderer that knows how to display thread names with their state icon
 *
 * @author Jiri Sedlacek
 * @author Ian Formanek
 * @author Alexander Simon (adapted for CND)
 */
public class ThreadNameCellRenderer extends EnhancedTableCellRenderer {
    private JLabel label;
    private ThreadsPanel viewManager; // view manager for this cell

    /**
     * Creates a new instance of ThreadNameCellRenderer
     */
    public ThreadNameCellRenderer(ThreadsPanel viewManager) {
        setHorizontalAlignment(JLabel.LEADING);
        label = new JLabel("", JLabel.LEADING); //NOI18N

        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
        this.viewManager = viewManager;
    }

    @Override
    protected void setRowForeground(Color c) {
        super.setRowForeground(c);
        label.setForeground(c);
    }

    public Component getTableCellRendererComponentPersistent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        return new ThreadNameCellRenderer(viewManager).getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                column);
    }

    protected void setValue(JTable table, Object value, int row, int column) {
        if (table != null) {
            setFont(table.getFont());
        }

        if (value == null) {
            label.setText(""); // NOI18N
        } else {
            int index = ((Integer) value).intValue();
            label.setText(viewManager.getThreadName(index));
        }
    }
}
