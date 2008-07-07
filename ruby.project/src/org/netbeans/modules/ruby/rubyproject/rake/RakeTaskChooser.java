/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.rubyproject.rake;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public final class RakeTaskChooser extends JPanel {

    private final static Object NO_TASK_ITEM = getMessage("RakeTaskChooser.no.matching.task");

    /** Remember checkbox state per IDE sessions. */
    private static boolean debug;

    /** Preselect lastly used task for more convenience. */
    private static String lastTask;

    private final RubyBaseProject project;
    private final List<RakeTask> allTasks;

    /**
     * Show the Rake Chooser and returns the Rake task selected by the user.
     */
    static TaskDescriptor select(final RubyBaseProject project) {
        assert EventQueue.isDispatchThread() : "must be called from EDT";
        final RakeTaskChooser chooserPanel = new RakeTaskChooser(project);
        String title = getMessage("RakeTaskChooser.title", ProjectUtils.getInformation(project).getDisplayName());

        final JButton runButton = new JButton(getMessage("RakeTaskChooser.runButton"));
        runButton.getAccessibleContext().setAccessibleDescription (getMessage("RakeTaskChooser.runButton.accessibleDescription"));
        setRunButtonState(runButton, chooserPanel);
        chooserPanel.matchingTaskList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                setRunButtonState(runButton, chooserPanel);
            }
        });

        final JButton refreshButton = new JButton();
        Mnemonics.setLocalizedText(refreshButton, getMessage("RakeTaskChooser.refreshButton"));
        refreshButton.getAccessibleContext().setAccessibleDescription (getMessage("RakeTaskChooser.refreshButton.accessibleDescription"));
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshButton.setEnabled(false);
                runButton.setEnabled(false);
                chooserPanel.reloadTasks(new Runnable() {
                    public void run() {
                        assert EventQueue.isDispatchThread() : "is EDT";
                        refreshButton.setEnabled(true);
                        setRunButtonState(runButton, chooserPanel);
                    }
                });
            }
        });

        Object[] options = new Object[]{
            refreshButton,
            runButton,
            DialogDescriptor.CANCEL_OPTION
        };

        DialogDescriptor descriptor = new DialogDescriptor(chooserPanel, title, true,
                options, runButton, DialogDescriptor.DEFAULT_ALIGN, null, null);
        descriptor.setClosingOptions(new Object[] { runButton, DialogDescriptor.CANCEL_OPTION });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleName(getMessage("RakeTaskChooser.accessibleName"));
        dialog.getAccessibleContext().setAccessibleDescription(getMessage("RakeTaskChooser.accessibleDescription"));
        dialog.setVisible(true);

        if (descriptor.getValue() == runButton) {
            RakeTask task = (RakeTask) chooserPanel.matchingTaskList.getSelectedValue();
            RakeTaskChooser.debug = chooserPanel.debugCheckbox.isSelected();
            RakeTaskChooser.lastTask = task.getTask();
            return new TaskDescriptor(task,
                    chooserPanel.taskParamsField.getText().trim(),
                    RakeTaskChooser.debug);
        }
        return null;
    }

    private static void setRunButtonState(final JButton runButton, final RakeTaskChooser chooserPanel) {
        Object val = chooserPanel.matchingTaskList.getSelectedValue();
        runButton.setEnabled(val != null && !NO_TASK_ITEM.equals(val));
    }

    static class TaskDescriptor {

        private final RakeTask task;
        private final String params;
        private final boolean debug;

        TaskDescriptor(RakeTask task, String params, boolean debug) {
            this.task = task;
            this.params = params;
            this.debug = debug;
        }

        RakeTask getRakeTask() {
            return task;
        }

        String getTaskParams() {
            return params;
        }

        boolean isDebug() {
            return debug;
        }
    }

    private RakeTaskChooser(RubyBaseProject project) {
        this.allTasks = new ArrayList<RakeTask>();
        this.project = project;
        initComponents();
        matchingTaskList.setCellRenderer(new RakeTaskChooser.RakeTaskRenderer());
        debugCheckbox.setSelected(debug);
        reloadAllTasks();
        refreshTaskList();
        rakeTaskField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { refreshTaskList(); }
            public void insertUpdate(DocumentEvent e) { refreshTaskList(); }
            public void removeUpdate(DocumentEvent e) { refreshTaskList(); }
        });
        preselectLastlySelected();
    }

    private void preselectLastlySelected() {
        if (lastTask == null) {
            return;
        }
        for (RakeTask task : allTasks) {
            if (lastTask.equals(task.getTask())) {
                matchingTaskList.setSelectedValue(task, true);
                break;
            }
        }
    }

    /** Reloads all tasks for the current project. */
    private void reloadAllTasks() {
        allTasks.clear();
        allTasks.addAll(RakeSupport.getRakeTasks(project, false));
    }

    /** Refreshes Rake tasks list view. */
    private void refreshTaskList() {
        String filter = rakeTaskField.getText().trim();
        DefaultListModel model = new DefaultListModel();
        List<RakeTask> matching = new ArrayList<RakeTask>();
        for (RakeTask task : allTasks) {
            if (!showAllCheckbox.isSelected() && task.getDescription() == null) {
                continue;
            }
            String taskLC = task.getTask().toLowerCase(Locale.US);
            String filterLC = filter.toLowerCase(Locale.US);
            if (taskLC.startsWith(filterLC)) {
                // show tasks which start with the filter first
                model.addElement(task);
            } else if (taskLC.contains(filterLC)) {
                matching.add(task);
            }
        }
        // append non-starting-with tasks
        for (RakeTask task : matching) {
            model.addElement(task);
        }
        matchingTaskList.setModel(model);
        if (model.isEmpty()) {
            model.addElement(NO_TASK_ITEM);
        }
        matchingTaskList.setSelectedIndex(0);
    }

    private void reloadTasks(final Runnable uiFinishAction) {
        final Object task = matchingTaskList.getSelectedValue();
        final JComponent[] comps = new JComponent[] {
            matchingTaskSP, matchingTaskLabel, matchingTaskLabel, matchingTaskList,
            rakeTaskLabel, rakeTaskField, debugCheckbox,
            taskParamLabel, taskParamsField, showAllCheckbox
        };
        setEnabled(comps, false);
        matchingTaskList.setListData(new Object[]{getMessage("RakeTaskChooser.reloading.tasks")});
        new Thread(new Runnable() {
            public void run() {
                RakeSupport.refreshTasks(project);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        reloadAllTasks();
                        refreshTaskList();
                        matchingTaskList.setSelectedValue(task, true);
                        uiFinishAction.run();
                        setEnabled(comps, true);
                        rakeTaskField.requestFocus();
                    }
                });
            }
        }, "Rake Tasks Refresher").start(); // NOI18N
    }

    private void setEnabled(final JComponent[] comps, final boolean enabled) {
        for (JComponent comp : comps) {
            comp.setEnabled(enabled);
        }

    }

    private static String getMessage(final String key, final String... args) {
        return NbBundle.getMessage(RakeTaskChooser.class, key, args);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rakeTaskLabel = new javax.swing.JLabel();
        rakeTaskField = new javax.swing.JTextField();
        taskParamLabel = new javax.swing.JLabel();
        taskParamsField = new javax.swing.JTextField();
        matchingTaskLabel = new javax.swing.JLabel();
        matchingTaskSP = new javax.swing.JScrollPane();
        matchingTaskList = new javax.swing.JList();
        debugCheckbox = new javax.swing.JCheckBox();
        showAllCheckbox = new javax.swing.JCheckBox();

        rakeTaskLabel.setLabelFor(rakeTaskField);
        org.openide.awt.Mnemonics.setLocalizedText(rakeTaskLabel, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.rakeTaskLabel.text")); // NOI18N

        rakeTaskField.setText(org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.rakeTaskField.text")); // NOI18N
        rakeTaskField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                rakeTaskFieldKeyPressed(evt);
            }
        });

        taskParamLabel.setLabelFor(taskParamsField);
        org.openide.awt.Mnemonics.setLocalizedText(taskParamLabel, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.taskParamLabel.text")); // NOI18N

        taskParamsField.setText(org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.taskParamsField.text")); // NOI18N

        matchingTaskLabel.setLabelFor(matchingTaskList);
        org.openide.awt.Mnemonics.setLocalizedText(matchingTaskLabel, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.matchingTaskLabel.text")); // NOI18N

        matchingTaskList.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        matchingTaskList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        matchingTaskSP.setViewportView(matchingTaskList);

        org.openide.awt.Mnemonics.setLocalizedText(debugCheckbox, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.debugCheckbox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(showAllCheckbox, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.showAllCheckbox.text")); // NOI18N
        showAllCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAllCheckboxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(matchingTaskSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 713, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rakeTaskLabel)
                            .add(taskParamLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(taskParamsField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 629, Short.MAX_VALUE)
                            .add(rakeTaskField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 629, Short.MAX_VALUE)))
                    .add(matchingTaskLabel)
                    .add(layout.createSequentialGroup()
                        .add(debugCheckbox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showAllCheckbox)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rakeTaskLabel)
                    .add(rakeTaskField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(taskParamLabel)
                    .add(taskParamsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(matchingTaskLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(matchingTaskSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(debugCheckbox)
                    .add(showAllCheckbox))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rakeTaskFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rakeTaskFieldKeyPressed
        Object actionKey = matchingTaskList.getInputMap().get(KeyStroke.getKeyStrokeForEvent(evt));//GEN-HEADEREND:event_rakeTaskFieldKeyPressed

        // see JavaFastOpen.boundScrollingKey()
        boolean isListScrollAction =
                "selectPreviousRow".equals(actionKey) || // NOI18N
                "selectPreviousRowExtendSelection".equals(actionKey) || // NOI18N
                "selectNextRow".equals(actionKey) || // NOI18N
                "selectNextRowExtendSelection".equals(actionKey) || // NOI18N
                // "selectFirstRow".equals(action) || // NOI18N
                // "selectLastRow".equals(action) || // NOI18N
                "scrollUp".equals(actionKey) || // NOI18N
                "scrollUpExtendSelection".equals(actionKey) || // NOI18N
                "scrollDown".equals(actionKey) || // NOI18N
                "scrollDownExtendSelection".equals(actionKey); // NOI18N


        int selectedIndex = matchingTaskList.getSelectedIndex();
        ListModel model = matchingTaskList.getModel();
        int modelSize = model.getSize();

        // Wrap around
        if ("selectNextRow".equals(actionKey) && selectedIndex == modelSize - 1) { // NOI18N
            matchingTaskList.setSelectedIndex(0);
            matchingTaskList.ensureIndexIsVisible(0);
            return;
        } else if ("selectPreviousRow".equals(actionKey) && selectedIndex == 0) { // NOI18N
            int last = modelSize - 1;
            matchingTaskList.setSelectedIndex(last);
            matchingTaskList.ensureIndexIsVisible(last);
            return;
        }

        if (isListScrollAction) {
            Action action = matchingTaskList.getActionMap().get(actionKey);
            action.actionPerformed(new ActionEvent(matchingTaskList, 0, (String) actionKey));
            evt.consume();
        }
    }                                        

    private void showAllCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-LAST:event_rakeTaskFieldKeyPressed
        refreshTaskList();//GEN-FIRST:event_showAllCheckboxActionPerformed
    }//GEN-HEADEREND:event_showAllCheckboxActionPerformed
//GEN-LAST:event_showAllCheckboxActionPerformed
    private static class RakeTaskRenderer extends JLabel implements ListCellRenderer {

        public RakeTaskRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                Color bgColor = list.getBackground();
                Color bgColorDarker = new Color(
                        Math.abs(bgColor.getRed() - 10),
                        Math.abs(bgColor.getGreen() - 10),
                        Math.abs(bgColor.getBlue() - 10));
                setBackground(index % 2 == 0 ? bgColor : bgColorDarker);
                setForeground(list.getForeground());
            }
            setFont(list.getFont());

            if (value instanceof RakeTask) {
                RakeTask task = ((RakeTask) value);
                String descripton = task.getDescription();
                if (descripton == null) {
                    setForeground(Color.GRAY);
                }
                StringBuilder text = new StringBuilder("<html>"); // NOI18N
                text.append("<b>").append(task.getTask()).append("</b>"); // NOI18N
                if (descripton != null) {
                    text.append(" : ").append(descripton); // NOI18N
                }
                text.append("</html>"); // NOI18N
                setText(text.toString());
            } else {
                setText(value.toString());
            }

            return this;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox debugCheckbox;
    private javax.swing.JLabel matchingTaskLabel;
    private javax.swing.JList matchingTaskList;
    private javax.swing.JScrollPane matchingTaskSP;
    private javax.swing.JTextField rakeTaskField;
    private javax.swing.JLabel rakeTaskLabel;
    private javax.swing.JCheckBox showAllCheckbox;
    private javax.swing.JLabel taskParamLabel;
    private javax.swing.JTextField taskParamsField;
    // End of variables declaration//GEN-END:variables

}
