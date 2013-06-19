/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.prep.ui.customizer;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.css.prep.CssPreprocessorType;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public class OptionsPanel extends JPanel {

    private static final long serialVersionUID = 16987546576769L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    // @GuardedBy("EDT")
    private final MappingsTableModel mappingsTableModel;
    // we must be thread safe
    private final List<Pair<String, String>> mappings = new CopyOnWriteArrayList<>();

    // we must be thread safe
    volatile boolean configured = false;
    volatile boolean enabled;


    public OptionsPanel(CssPreprocessorType type, boolean initialEnabled, List<Pair<String, String>> initialMappings) {
        assert EventQueue.isDispatchThread();

        mappingsTableModel = new MappingsTableModel(mappings);
        enabled = initialEnabled;

        initComponents();
        init(type, initialEnabled, initialMappings);
    }

    @NbBundle.Messages({
        "# {0} - preprocessor name",
        "OptionsPanel.compilationEnabled.label=Co&mpile {0} Files on Save",
    })
    private void init(CssPreprocessorType type, boolean initialEnabled, List<Pair<String, String>> initialMappings) {
        assert EventQueue.isDispatchThread();
        configureExecutablesButton.setVisible(false);
        Mnemonics.setLocalizedText(enabledCheckBox, Bundle.OptionsPanel_compilationEnabled_label(type.getDisplayName()));
        // values
        mappingsTable.setModel(mappingsTableModel);
        setCompilationEnabled(initialEnabled);
        setMappings(initialMappings);
        // ui
        enablePanel(initialEnabled);
        enableRemoveButton();
        // listeners
        enabledCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                enabled = e.getStateChange() == ItemEvent.SELECTED;
                configured = true;
                enablePanel(enabled);
                fireChange();
            }
        });
        mappingsTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                fireChange();
            }
        });
        mappingsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                enableRemoveButton();
            }
        });
    }

    public void showConfigureExecutableButton() {
        configureExecutablesButton.setVisible(true);
    }

    public boolean isConfigured() {
        return configured;
    }

    public boolean isCompilationEnabled() {
        return enabled;
    }

    public void setCompilationEnabled(boolean enabled) {
        assert EventQueue.isDispatchThread();
        enabledCheckBox.setSelected(enabled);
    }

    public List<Pair<String, String>> getMappings() {
        return mappings;
    }

    public void setMappings(List<Pair<String, String>> mappings) {
        assert EventQueue.isDispatchThread();
        this.mappings.clear();
        this.mappings.addAll(mappings);
        mappingsTableModel.fireMappingsChange();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    void enablePanel(boolean enabled) {
        watchLabel.setEnabled(enabled);
        mappingsTable.setEnabled(enabled);
        addButton.setEnabled(enabled);
        configureExecutablesButton.setEnabled(enabled);
        if (enabled) {
            enableRemoveButton();
        } else {
            removeButton.setEnabled(false);
        }
    }

    void enableRemoveButton() {
        removeButton.setEnabled(mappingsTable.getSelectedRowCount() > 0);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        placeholderPanel = new JPanel();
        enabledCheckBox = new JCheckBox();
        configureExecutablesButton = new JButton();
        watchLabel = new JLabel();
        mappingsScrollPane = new JScrollPane();
        mappingsTable = new JTable();
        addButton = new JButton();
        removeButton = new JButton();

        Mnemonics.setLocalizedText(enabledCheckBox, "COMPILATION_ON_SAVE"); // NOI18N

        Mnemonics.setLocalizedText(configureExecutablesButton, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.configureExecutablesButton.text")); // NOI18N
        configureExecutablesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                configureExecutablesButtonActionPerformed(evt);
            }
        });

        GroupLayout placeholderPanelLayout = new GroupLayout(placeholderPanel);
        placeholderPanel.setLayout(placeholderPanelLayout);
        placeholderPanelLayout.setHorizontalGroup(
            placeholderPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(placeholderPanelLayout.createSequentialGroup()
                .addComponent(enabledCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(configureExecutablesButton))
        );
        placeholderPanelLayout.setVerticalGroup(
            placeholderPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(placeholderPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(enabledCheckBox)
                .addComponent(configureExecutablesButton))
        );

        Mnemonics.setLocalizedText(watchLabel, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.watchLabel.text")); // NOI18N

        mappingsScrollPane.setViewportView(mappingsTable);

        Mnemonics.setLocalizedText(addButton, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.removeButton.text")); // NOI18N
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mappingsScrollPane, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(removeButton)
                            .addComponent(addButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(watchLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(placeholderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {addButton, removeButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(placeholderPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(watchLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mappingsScrollPane, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        assert EventQueue.isDispatchThread();
        mappings.add(Pair.of("", "")); // NOI18N
        mappingsTableModel.fireMappingsChange();
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        assert EventQueue.isDispatchThread();
        int[] selectedRows = mappingsTable.getSelectedRows();
        assert selectedRows.length > 0;
        for (int i = selectedRows.length - 1; i >= 0; --i) {
            mappings.remove(selectedRows[i]);
        }
        mappingsTableModel.fireMappingsChange();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void configureExecutablesButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_configureExecutablesButtonActionPerformed
        OptionsDisplayer.getDefault().open(CssPreprocessors.OPTIONS_PATH);
    }//GEN-LAST:event_configureExecutablesButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton addButton;
    private JButton configureExecutablesButton;
    private JCheckBox enabledCheckBox;
    private JScrollPane mappingsScrollPane;
    private JTable mappingsTable;
    private JPanel placeholderPanel;
    private JButton removeButton;
    private JLabel watchLabel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class MappingsTableModel extends AbstractTableModel {

        private static final long serialVersionUID = -65325657686411L;

        private final List<Pair<String, String>> mappings;


        public MappingsTableModel(List<Pair<String, String>> mappings) {
            assert mappings != null;
            this.mappings = mappings;
        }

        @Override
        public int getRowCount() {
            return mappings.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Pair<String, String> pair = mappings.get(rowIndex);
            if (columnIndex == 0) {
                return pair.first();
            }
            if (columnIndex == 1) {
                return pair.second();
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            String path = (String) aValue;
            Pair<String, String> pair = mappings.get(rowIndex);
            if (columnIndex == 0) {
                mappings.set(rowIndex, Pair.of(path, pair.second()));
            } else if (columnIndex == 1) {
                mappings.set(rowIndex, Pair.of(pair.first(), path));
            } else {
                throw new IllegalStateException("Unknown column index: " + columnIndex);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @NbBundle.Messages({
            "MappingsTableModel.column.input.title=Input",
            "MappingsTableModel.column.output.title=Output",
        })
        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return Bundle.MappingsTableModel_column_input_title();
            }
            if (columnIndex == 1) {
                return Bundle.MappingsTableModel_column_output_title();
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0
                    || columnIndex == 1) {
                return String.class;
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0
                    || columnIndex == 1) {
                return true;
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        public void fireMappingsChange() {
            assert EventQueue.isDispatchThread();
            fireTableDataChanged();
        }

    }

}
