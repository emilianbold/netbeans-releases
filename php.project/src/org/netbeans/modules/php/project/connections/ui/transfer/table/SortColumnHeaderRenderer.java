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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.php.project.connections.ui.transfer.table;

import java.awt.Component;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.php.project.PhpPreferences;
import org.openide.util.ImageUtilities;

/**
 * @author Radek Matous
 */
public final class SortColumnHeaderRenderer implements TableCellRenderer {
    private static final String SORTING_COLUMN_INDEX = "SortingColumnIndex"; // NOI18N
    private static final String SORTING_ASCENDING = "SortingAscending"; // NOI18N
    private static ImageIcon sortDescIcon;
    private static ImageIcon sortAscIcon;

    private final TransferFileTableModel model;
    private final TableCellRenderer textRenderer;
    private int sortColumnIndex;
    private boolean sortAscending;

    public SortColumnHeaderRenderer(TransferFileTableModel model, TableCellRenderer textRenderer) {
        assert model != null;
        assert textRenderer != null;

        this.model = model;
        this.textRenderer = textRenderer;
        sortColumnIndex = getPreferences().getInt(SORTING_COLUMN_INDEX, getDefaultSortingColumn());
        sortAscending = getPreferences().getBoolean(SORTING_ASCENDING, true);
        model.sort(sortColumnIndex, sortAscending);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component text = textRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (text instanceof JLabel) {
            JLabel label = (JLabel) text;
            if (column == sortColumnIndex) {
                label.setIcon(sortAscending ? getSortAscIcon() : getSortDescIcon());
                label.setHorizontalTextPosition(SwingConstants.LEFT);
            } else {
                label.setIcon(null);
            }
        }
        return text;
    }

    public void setDefaultSorting() {
        setSorting(getDefaultSortingColumn());
    }

    private int getDefaultSortingColumn() {
        return 1;
    }

    public void setSorting(int columnIndex) {
        if (columnIndex != sortColumnIndex) {
            sortColumnIndex = columnIndex;
            sortAscending = true;
        } else {
            sortAscending = !sortAscending;
        }
        getPreferences().putInt(SORTING_COLUMN_INDEX, sortColumnIndex);
        getPreferences().putBoolean(SORTING_ASCENDING, sortAscending);
        model.sort(columnIndex, sortAscending);
    }

    public void sort() {
        model.sort(sortColumnIndex, sortAscending);
    }

    private ImageIcon getSortAscIcon() {
        if (sortAscIcon == null) {
            sortAscIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/php/project/ui/resources/columnsSortedDesc.gif", false); // NOI18N
        }
        return sortAscIcon;
    }

    private ImageIcon getSortDescIcon() {
        if (sortDescIcon == null) {
            sortDescIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/php/project/ui/resources/columnsSortedAsc.gif", false); // NOI18N
        }
        return sortDescIcon;
    }

    private static Preferences getPreferences() {
        return PhpPreferences.getPreferences(false);
    }
}
