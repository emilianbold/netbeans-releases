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

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.api.phpmodule.PhpFrameworks;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;

/**
 * List of frameworks is "copied" from web project.
 * @author Tomas Mysik
 */
public class PhpFrameworksPanelVisual extends JPanel implements TableModelListener, ListSelectionListener {
    private static final int STEP_INDEX = 2;
    private static final long serialVersionUID = 158602680330133653L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final FrameworksTableModel model;

    public PhpFrameworksPanelVisual(PhpFrameworksPanel wizardPanel) {
        // Provide a name in the title bar.
        setName(wizardPanel.getSteps()[STEP_INDEX]);
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, STEP_INDEX);
        // Step name (actually the whole list for reference).
        putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, wizardPanel.getSteps());

        initComponents();

        // frameworks
        model = new FrameworksTableModel();
        frameworksTable.setModel(model);
        createFrameworksList();

        FrameworksTableCellRenderer renderer = new FrameworksTableCellRenderer();
        renderer.setBooleanRenderer(frameworksTable.getDefaultRenderer(Boolean.class));
        frameworksTable.setDefaultRenderer(PhpFrameworkProvider.class, renderer);
        frameworksTable.setDefaultRenderer(Boolean.class, renderer);
        initTableVisualProperties();

        changeDescription();
    }

    public void addPhpFrameworksListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removePhpFrameworksListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public List<PhpFrameworkProvider> getSelectedFrameworks() {
        List<PhpFrameworkProvider> frameworks = new LinkedList<PhpFrameworkProvider>();
        for (int i = 0; i < model.getRowCount(); ++i) {
            FrameworkModelItem item = model.getItem(i);
            if (item.isSelected()) {
                PhpFrameworkProvider framework = item.getFramework();
                assert framework != null;
                frameworks.add(framework);
            }
        }

        return frameworks;
    }

    public void tableChanged(TableModelEvent e) {
        changeDescription();
    }

    public void valueChanged(ListSelectionEvent e) {
        changeDescription();
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    private void createFrameworksList() {
        for (PhpFrameworkProvider provider : PhpFrameworks.getFrameworks()) {
            model.addItem(new FrameworkModelItem(provider));
        }
    }

    private void initTableVisualProperties() {
        frameworksTable.getModel().addTableModelListener(this);
        frameworksTable.getSelectionModel().addListSelectionListener(this);

        frameworksTable.setRowHeight(frameworksTable.getRowHeight() + 4);
        frameworksTable.setIntercellSpacing(new Dimension(0, 0));
        // set the color of the table's JViewport
        frameworksTable.getParent().setBackground(frameworksTable.getBackground());
        frameworksTable.getColumnModel().getColumn(0).setMaxWidth(30);
    }

    private void changeDescription() {
        if (frameworksTable.getSelectedRow() == -1) {
            descriptionLabel.setText(" "); // NOI18N
        } else {
            FrameworkModelItem item = model.getItem(frameworksTable.getSelectedRow());
            descriptionLabel.setText(item.getFramework().getDescription());
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        frameworksScrollPane = new JScrollPane();
        frameworksTable = new JTable();
        descriptionLabel = new JLabel();

        frameworksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        frameworksTable.setShowHorizontalLines(false);
        frameworksTable.setShowVerticalLines(false);
        frameworksTable.setTableHeader(null);
        frameworksScrollPane.setViewportView(frameworksTable);

        descriptionLabel.setText("DUMMY"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(frameworksScrollPane, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(descriptionLabel)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(frameworksScrollPane, GroupLayout.PREFERRED_SIZE, 145, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(descriptionLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel descriptionLabel;
    private JScrollPane frameworksScrollPane;
    private JTable frameworksTable;
    // End of variables declaration//GEN-END:variables

    private static final class FrameworksTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 22495101047716943L;

        private TableCellRenderer booleanRenderer;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof PhpFrameworkProvider) {
                PhpFrameworkProvider item = (PhpFrameworkProvider) value;
                return super.getTableCellRendererComponent(table, item.getName(), isSelected, false, row, column);
            } else if (value instanceof Boolean && booleanRenderer != null) {
                return booleanRenderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
        }

        public void setBooleanRenderer(TableCellRenderer booleanRenderer) {
            this.booleanRenderer = booleanRenderer;
        }
    }

    private static final class FrameworksTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 8082636013224696L;

        private final DefaultListModel model;

        public FrameworksTableModel() {
            model = new DefaultListModel();
        }

        public int getColumnCount() {
            return 2;
        }

        public int getRowCount() {
            return model.size();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Boolean.class;
                case 1:
                    return PhpFrameworkProvider.class;
                default:
                    assert false : "Unknown column index: " + columnIndex;
                    break;
            }
            return super.getColumnClass(columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        public Object getValueAt(int row, int column) {
            FrameworkModelItem item = getItem(row);
            switch (column) {
                case 0:
                    return item.isSelected();
                case 1:
                    return item.getFramework();
                default:
                    assert false : "Unknown column index: " + column;
                    break;
            }
            return "";
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            FrameworkModelItem item = getItem(row);
            switch (column) {
                case 0:
                    item.setSelected((Boolean) value);
                    break;
                case 1:
                    item.setFramework((PhpFrameworkProvider) value);
                    break;
                default:
                    assert false : "Unknown column index: " + column;
                    break;
            }
            fireTableCellUpdated(row, column);
        }

        FrameworkModelItem getItem(int index) {
            return (FrameworkModelItem) model.get(index);
        }

        void addItem(FrameworkModelItem item) {
            model.addElement(item);
        }
    }

    private static final class FrameworkModelItem {
        private PhpFrameworkProvider framework;
        private Boolean selected;

        public FrameworkModelItem(PhpFrameworkProvider framework) {
            setFramework(framework);
            setSelected(Boolean.FALSE);
        }

        public PhpFrameworkProvider getFramework() {
            return framework;
        }

        public void setFramework(PhpFrameworkProvider framework) {
            this.framework = framework;
        }

        public Boolean isSelected() {
            return selected;
        }

        public void setSelected(Boolean selected) {
            this.selected = selected;
        }
    }
}
