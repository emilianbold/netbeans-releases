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
package org.netbeans.modules.dlight.toolsui;

import java.awt.Component;
import java.awt.Image;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.dlight.api.tool.DLightToolUIWrapper;
import org.openide.util.ImageUtilities;

/**
 *
 * @author thp
 */
public class ToolsTable extends JTable {

    private List<DLightToolUIWrapper> allDLightTools = null;

    public ToolsTable(List<DLightToolUIWrapper> allDLightTools, ListSelectionListener listSelectionListener) {
        this.allDLightTools = allDLightTools;

        if (getRowHeight() < 20) {
            setRowHeight(20);
        }

        setModel(new MyTableModel());
        
        getAccessibleContext().setAccessibleDescription(""); // NOI18N
        getAccessibleContext().setAccessibleName(""); // NOI18N

        getColumnModel().getColumn(0).setPreferredWidth(30);
        getColumnModel().getColumn(0).setMaxWidth(30);
        setTableHeader(null);

        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        getSelectionModel().addListSelectionListener(listSelectionListener);
    }

    public void initSelection() {
        int i = 0;
        for (DLightToolUIWrapper dlightTool : allDLightTools) {
            if (dlightTool.isEnabled()) {
                getSelectionModel().setSelectionInterval(i, i);
                return;
            }
            i++;
        }
    }

    @Override
    public boolean getShowHorizontalLines() {
        return false;
    }

    @Override
    public boolean getShowVerticalLines() {
        return false;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return new MyTableCellRenderer();
    }

    @Override
    public TableCellEditor getCellEditor(int row, int col) {
        if (col == 0) {
            DLightToolUIWrapper dlightTool = allDLightTools.get(row);
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(dlightTool.isEnabled());
            return new DefaultCellEditor(checkBox);
        } else {
            return super.getCellEditor(row, col);
        }
    }

    class MyTableCellRenderer extends DefaultTableCellRenderer {

        Image emptyImage = ImageUtilities.loadImage("/org/openide/resources/actions/empty.gif"); // NOI18N

        @Override
        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, col);
            final DLightToolUIWrapper dlightTool = allDLightTools.get(row);
            if (col == 0) {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(dlightTool.isEnabled());
                checkBox.setBackground(label.getBackground());
                return checkBox;
            } else {
                label.setText(dlightTool.getdLightTool().getName()); // NOI18N
                Image image = null;
                if (dlightTool.getdLightTool().hasIcon()) {
                    image = ImageUtilities.loadImage(dlightTool.getdLightTool().getIconPath());
                }
                if (image == null) {
                    image = emptyImage;
                }
                label.setIcon(ImageUtilities.image2Icon(image));
                label.setToolTipText(null);
            }
            return label;
        }
    }

    class MyTableModel extends DefaultTableModel {

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return allDLightTools.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            return allDLightTools.get(row);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == 0) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 0) {
                DLightToolUIWrapper dlightTool = allDLightTools.get(row);
                dlightTool.setEnabled(!dlightTool.isEnabled());
            }
        }
    }
}
