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
package org.netbeans.modules.dlight.visualizers.ui;

import java.awt.Component;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.visualizers.FunctionsListSheetCell;
import org.netbeans.modules.dlight.visualizers.api.AdvancedTableViewVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.AdvancedTableViewVisualizerConfigurationAccessor;
import org.netbeans.swing.outline.Outline;
import org.openide.nodes.Node;

/**
 *
 * @author ak119685
 */
public final class AdvancedDataRowTableOutline extends AbstractListViewTable {

    private final AdvancedTableViewVisualizerConfiguration configuration;
    private final String nodeColumnName;

    public AdvancedDataRowTableOutline(String nodeColumnName, AdvancedTableViewVisualizerConfiguration configuration) {
        super(nodeColumnName);
        this.nodeColumnName = nodeColumnName;
        this.configuration = configuration;

        AdvancedTableViewVisualizerConfigurationAccessor accessor =
                AdvancedTableViewVisualizerConfigurationAccessor.getDefault();

        List<String> hiddenColumns = accessor.getHiddenColumnNames(configuration);
        DataTableMetadata metadata = configuration.getMetadata();
        String nodeRowColumnID = accessor.getRowNodeColumnName(configuration);

        Outline outline = getOutline();

        List<Column> columns = new ArrayList<Column>();

        for (String columnName : metadata.getColumnNames()) {
            if (nodeColumnName.equals(columnName) || nodeRowColumnID.equals(columnName) || hiddenColumns.contains(columnName)) {
                continue;
            }

            Column c = metadata.getColumnByName(columnName);
            columns.add(c);
            addPropertyColumn(c.getColumnName(), c.getColumnUName(), c.getColumnLongUName());
        }

        String iconColumnID = accessor.getIconColumnID(configuration);

        if (iconColumnID == null || configuration.getMetadata().getColumnByName(iconColumnID) == null) {
            outline.setDefaultRenderer(Object.class, new ExtendedTableCellRendererForNode());
        }

        outline.setDefaultRenderer(Node.Property.class, new FunctionsListSheetCell.OutlineSheetCell(outline, columns));

        initActionMap();
    }

    private class ExtendedTableCellRendererForNode extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (column != 0) {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }

            PropertyEditor editor = PropertyEditorManager.findEditor(configuration.getMetadata().getColumnByName(nodeColumnName).getColumnClass());
            
            if (editor != null && value != null && !(value + "").trim().equals("")) { // NOI18N
                editor.setAsText(value.toString());
                return super.getTableCellRendererComponent(table, editor.getAsText(), isSelected, hasFocus, row, column);
            }

            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
