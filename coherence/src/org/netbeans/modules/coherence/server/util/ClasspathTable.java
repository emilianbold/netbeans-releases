/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.server.util;

import java.awt.Component;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.coherence.server.CoherenceProperties;
import org.openide.util.ChangeSupport;

/**
 * JTable for specifying Coherence server libraries on classpath.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@SuppressWarnings("serial")
public final class ClasspathTable extends JTable {

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final ClasspathTable.TableModel model = new ClasspathTable.TableModel();
    public static final Set<String> COHERENCE_SERVER_JARS = new HashSet<String>(Arrays.asList(new String[] {
        "coherence-jpa.jar", //NOI18N
        "coherence-hibernate.jar", //NOI18N
        "coherence-toplink.jar", //NOI18N
        "coherence-web.jar", //NOI18N
        "coherence-work.jar" //NOI18N
    }));

    public ClasspathTable() {
        ClasspathTable.TableCellRenderer renderer = new ClasspathTable.TableCellRenderer();
        renderer.setBooleanRenderer(this.getDefaultRenderer(Boolean.class));
        this.setDefaultRenderer(Boolean.class, renderer);
        refreshClasspathEntries(""); //NOI18N
        initVisualProperties();
    }

    public void setSelectedEntries(List<String> jars) {
        for (int i = 0; i < model.getRowCount(); i++) {
            TableModelItem item = model.getItem(i);
            for (String jarName : jars) {
                if (jarName.contains(item.getName())) {
                    item.setSelected(true);
                }
            }
        }
    }

    public void refreshClasspathEntries(String serverLocation) {
        model.removeAllItems();
        File serverRoot = new File(serverLocation);
        File libDir = new File(serverRoot, CoherenceProperties.PLATFORM_LIB_DIR);
        if (libDir.exists()) {
            File[] jars = libDir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File file, String name) {
                    return name.endsWith(".jar") && COHERENCE_SERVER_JARS.contains(name); //NOI18N
                }
            });

            for (File file : jars) {
                model.addItem(new ClasspathTable.TableModelItem(Boolean.FALSE, file.getName()));
            }
            model.fireTableDataChanged();
            initVisualProperties();
        }
    }

    public TableModel getTableModel() {
        return model;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    private void fireChange() {
        changeSupport.fireChange();
    }

    private void initVisualProperties() {
        this.setColumnSelectionAllowed(false);
        this.setRowSelectionAllowed(true);
        this.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setTableHeader(null);
        this.setRowHeight(this.getRowHeight() + 1);
        this.setIntercellSpacing(new java.awt.Dimension(0, 0));
        this.setShowHorizontalLines(false);
        this.setShowVerticalLines(false);
        if (this.getColumnModel().getColumnCount() > 0) {
            this.getColumnModel().getColumn(0).setMaxWidth(30);
        }
    }

    @SuppressWarnings("serial")
    public static class TableModelItem {

        private Boolean selected;
        private String name;

        public TableModelItem(Boolean selected, String name) {
            this.selected = selected;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getSelected() {
            return selected;
        }

        public void setSelected(Boolean selected) {
            this.selected = selected;
        }
    }

    @SuppressWarnings("serial")
    public static class TableCellRenderer extends DefaultTableCellRenderer {

        private javax.swing.table.TableCellRenderer booleanRenderer;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Boolean && booleanRenderer != null) {
                return booleanRenderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            } else {
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }

        public void setBooleanRenderer(javax.swing.table.TableCellRenderer booleanRenderer) {
            this.booleanRenderer = booleanRenderer;
        }
    }

    @SuppressWarnings("serial")
    public class TableModel extends AbstractTableModel {

        private final Class<?>[] columnTypes = new Class<?>[]{Boolean.class, String.class};
        private final DefaultListModel listModel = new DefaultListModel();

        @Override
        public int getRowCount() {
            return listModel.getSize();
        }

        @Override
        public int getColumnCount() {
            return columnTypes.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ClasspathTable.TableModelItem item = getItem(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item.getSelected();
                case 1:
                    return item.getName();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            ClasspathTable.TableModelItem item = getItem(rowIndex);
            switch (columnIndex) {
                case 0:
                    item.setSelected((Boolean) aValue);
                    fireChange();
                    break;
                case 1:
                    item.setName((String) aValue);
                    break;
                default:
                    break;
            }
            super.setValueAt(aValue, rowIndex, columnIndex);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnTypes[columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        public ClasspathTable.TableModelItem getItem(int row) {
            return (ClasspathTable.TableModelItem) listModel.get(row);
        }

        public void addItem(ClasspathTable.TableModelItem item) {
            listModel.addElement(item);
        }

        public void removeAllItems() {
            listModel.clear();
        }
    }
}
