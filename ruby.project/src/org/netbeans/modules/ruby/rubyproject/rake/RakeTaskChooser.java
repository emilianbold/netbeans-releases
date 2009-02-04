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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
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
import org.netbeans.modules.ruby.rubyproject.RakeParameters;
import org.netbeans.modules.ruby.rubyproject.RakeParameters.RakeParameter;
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

    /** [project directory path -&gt; (task -&gt; parameters)] */
    private static Map<String, Map<RakeTask, ParameterContainer>> prjToTask
            = new HashMap<String, Map<RakeTask, ParameterContainer>>();

    private final RubyBaseProject project;
    private final List<RakeTask> allTasks;
    private JButton runButton;

    /**
     * Show the Rake Chooser and returns the Rake task selected by the user.
     */
    static TaskDescriptor select(final RubyBaseProject project) {
        assert EventQueue.isDispatchThread() : "must be called from EDT";
        final JButton runButton = new JButton(getMessage("RakeTaskChooser.runButton"));
        final RakeTaskChooser chooserPanel = new RakeTaskChooser(project, runButton);
        String title = getMessage("RakeTaskChooser.title", ProjectUtils.getInformation(project).getDisplayName());

        runButton.getAccessibleContext().setAccessibleDescription (getMessage("RakeTaskChooser.runButton.accessibleDescription"));
        setRunButtonState(runButton, chooserPanel);
        chooserPanel.matchingTaskList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                setRunButtonState(runButton, chooserPanel);
                chooserPanel.initTaskParameters();
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
            RakeTask task = chooserPanel.getSelectedTask();
            RakeTaskChooser.debug = chooserPanel.debugCheckbox.isSelected();
            RakeTaskChooser.lastTask = task.getTask();
            chooserPanel.storeParameters();
            return new TaskDescriptor(task, chooserPanel.getParameters(), RakeTaskChooser.debug);
        }
        return null;
    }

    private void initTaskParameters() {
        RakeTask task = getSelectedTask();
        List<? super Object> params = new ArrayList<Object>();
        // no param option for convenience
        params.add(""); //NOI18N
        params.addAll(getStoredParams(task));
        params.addAll(RakeParameters.getParameters(task, project));
        taskParametersComboBox.setModel(new DefaultComboBoxModel(params.toArray()));
        preselectLastSelectedParam(task);
    }

    /**
     * Pre-selects the parameter that was last selected for the 
     * given task.
     * 
     * @param task
     */
    private void preselectLastSelectedParam(RakeTask task) {
        ParameterContainer params = getTasksToParams().get(task);
        if (params == null) {
            return;
        }
        String lastSelected = params.getLastSelected();
        if (lastSelected == null) {
            taskParametersComboBox.setSelectedItem(""); //NOI18N
            return;
        }
        for (int i = 0; i < taskParametersComboBox.getItemCount(); i++) {
            Object item = taskParametersComboBox.getItemAt(i);
            if (item instanceof RakeParameter) {
                if (((RakeParameter) item).toRakeParam().equals(lastSelected)) {
                    taskParametersComboBox.setSelectedIndex(i);
                    break;
                }
            } else if (item.equals(lastSelected)) {
                taskParametersComboBox.setSelectedIndex(i);
            }
        }
    }

    private Map<RakeTask,ParameterContainer> getTasksToParams() {
        String prjDir = project.getProjectDirectory().getPath();
        Map<RakeTask,ParameterContainer> result = prjToTask.get(prjDir);
        if (result == null) {
            result = new HashMap<RakeTask,ParameterContainer>();
            prjToTask.put(prjDir, result);
        }
        return result;
    }

    private List<String> getStoredParams(RakeTask task) {
        if (task == null) {
            return Collections.<String>emptyList();
        }
        final Map<RakeTask, ParameterContainer> tasksToParams = getTasksToParams();
        if (tasksToParams == null) {
            return Collections.<String>emptyList();
        }
        ParameterContainer stored = tasksToParams.get(task);
        if (stored == null) {
            return Collections.<String>emptyList();
        }
        List<String> result = new ArrayList<String>(stored.getParams());
        Collections.sort(result);
        return result;
    }

    private String getParameters() {
        Object selected = taskParametersComboBox.getSelectedItem();
        String result = ""; //NOI18N
        if (selected instanceof RakeParameter) {
            result = ((RakeParameter) selected).toRakeParam();
        } else {
            result = selected.toString();
        }
        return result.trim();
    }

    private static void setRunButtonState(final JButton runButton, final RakeTaskChooser chooserPanel) {
        runButton.setEnabled(chooserPanel.getSelectedTask() != null);
    }

    static class TaskDescriptor {

        private final RakeTask task;
        private final String params;
        private final boolean debug;

        TaskDescriptor(RakeTask task, String params, boolean debug) {
            this.task = task;
            this.params = params.length() == 0 ? null : params;
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

    private RakeTaskChooser(RubyBaseProject project, final JButton runButton) {
        this.runButton = runButton;
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
        initTaskParameters();
    }

    /**
     * Stores the param that the user entered in the params combo
     * box.
     */
    private void storeParameters() {
        String prjDir = project.getProjectDirectory().getPath();
        Map<RakeTask, ParameterContainer> taskToParams = prjToTask.get(prjDir);
        if (taskToParams == null) {
            taskToParams = new HashMap<RakeTask, ParameterContainer>();
            prjToTask.put(prjDir, taskToParams);
        }
        ParameterContainer params = taskToParams.get(getSelectedTask());
        if (params == null) {
            params = new ParameterContainer();
            taskToParams.put(getSelectedTask(), params);
        }
        String currentParam = getParameters();
        params.addParam(currentParam);
        params.setLastSelected(currentParam);
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
        initTaskParameters();
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
        List<RakeTask> matching = Filter.getFilteredTasks(allTasks, filter, showAllCheckbox.isSelected());

        for (RakeTask task : matching) {
            model.addElement(task);
        }
        matchingTaskList.setModel(model);
        if (model.isEmpty()) {
            model.addElement(NO_TASK_ITEM);
        }
        matchingTaskList.setSelectedIndex(0);
        initTaskParameters();
    }

    private void reloadTasks(final Runnable uiFinishAction) {
        final Object task = matchingTaskList.getSelectedValue();
        final JComponent[] comps = new JComponent[] {
            matchingTaskSP, matchingTaskLabel, matchingTaskLabel, matchingTaskList,
            rakeTaskLabel, rakeTaskField, debugCheckbox,
            taskParamLabel, taskParametersComboBox, showAllCheckbox, rakeTaskHint
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

    private RakeTask getSelectedTask() {
        Object val = matchingTaskList.getSelectedValue();
        if (val != null && !NO_TASK_ITEM.equals(val)) {
            return (RakeTask) val;
        }
        return null;
    }

    private static String getMessage(final String key, final String... args) {
        return NbBundle.getMessage(RakeTaskChooser.class, key, args);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rakeTaskLabel = new javax.swing.JLabel();
        taskParamLabel = new javax.swing.JLabel();
        matchingTaskLabel = new javax.swing.JLabel();
        matchingTaskSP = new javax.swing.JScrollPane();
        matchingTaskList = new javax.swing.JList();
        debugCheckbox = new javax.swing.JCheckBox();
        showAllCheckbox = new javax.swing.JCheckBox();
        rakeTaskFieldPanel = new javax.swing.JPanel();
        rakeTaskField = new javax.swing.JTextField();
        rakeTaskHint = new javax.swing.JLabel();
        taskParametersComboBox = new javax.swing.JComboBox();

        rakeTaskLabel.setLabelFor(rakeTaskField);
        org.openide.awt.Mnemonics.setLocalizedText(rakeTaskLabel, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.rakeTaskLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(taskParamLabel, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.taskParamLabel.text")); // NOI18N

        matchingTaskLabel.setLabelFor(matchingTaskList);
        org.openide.awt.Mnemonics.setLocalizedText(matchingTaskLabel, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.matchingTaskLabel.text")); // NOI18N

        matchingTaskList.setFont(new java.awt.Font("Monospaced", 0, 12));
        matchingTaskList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        matchingTaskList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                matchingTaskListMouseClicked(evt);
            }
        });
        matchingTaskSP.setViewportView(matchingTaskList);

        org.openide.awt.Mnemonics.setLocalizedText(debugCheckbox, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.debugCheckbox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(showAllCheckbox, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.showAllCheckbox.text")); // NOI18N
        showAllCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAllCheckboxActionPerformed(evt);
            }
        });

        rakeTaskFieldPanel.setLayout(new java.awt.BorderLayout());

        rakeTaskField.setText(org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.rakeTaskField.text")); // NOI18N
        rakeTaskField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                rakeTaskFieldKeyPressed(evt);
            }
        });
        rakeTaskFieldPanel.add(rakeTaskField, java.awt.BorderLayout.NORTH);

        org.openide.awt.Mnemonics.setLocalizedText(rakeTaskHint, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.rakeTaskHint.text")); // NOI18N
        rakeTaskFieldPanel.add(rakeTaskHint, java.awt.BorderLayout.SOUTH);

        taskParametersComboBox.setEditable(true);
        taskParametersComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rakeTaskLabel)
                            .add(taskParamLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(taskParametersComboBox, 0, 568, Short.MAX_VALUE)
                            .add(rakeTaskFieldPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(matchingTaskLabel))
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(debugCheckbox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showAllCheckbox))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(matchingTaskSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(rakeTaskLabel)
                    .add(rakeTaskFieldPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(taskParamLabel)
                    .add(taskParametersComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(matchingTaskLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(matchingTaskSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(debugCheckbox)
                    .add(showAllCheckbox))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void handleNavigationKeys(KeyEvent evt) {
        Object actionKey = matchingTaskList.getInputMap().get(KeyStroke.getKeyStrokeForEvent(evt));

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

    private void rakeTaskFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rakeTaskFieldKeyPressed
        handleNavigationKeys(evt);
    }//GEN-LAST:event_rakeTaskFieldKeyPressed

    private void showAllCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAllCheckboxActionPerformed
        refreshTaskList();
    }//GEN-LAST:event_showAllCheckboxActionPerformed

    private void matchingTaskListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_matchingTaskListMouseClicked
        if (runButton.isEnabled() && evt.getClickCount() == 2) {
            runButton.doClick();
        }
    }//GEN-LAST:event_matchingTaskListMouseClicked

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
    private javax.swing.JPanel rakeTaskFieldPanel;
    private javax.swing.JLabel rakeTaskHint;
    private javax.swing.JLabel rakeTaskLabel;
    private javax.swing.JCheckBox showAllCheckbox;
    private javax.swing.JLabel taskParamLabel;
    private javax.swing.JComboBox taskParametersComboBox;
    // End of variables declaration//GEN-END:variables

    final static class Filter {

        private final String filter;
        private final List<RakeTask> tasks;
        private final boolean showAll;

        private Filter(List<RakeTask> tasks, String filter, boolean showAll) {
            this.tasks = tasks;
            this.filter = filter;
            this.showAll = showAll;
        }

        static List<RakeTask> getFilteredTasks(List<RakeTask> allTasks, String filter, boolean showAll) {
            Filter f = new Filter(allTasks, filter, showAll);
            return f.filter();
        }

        private List<RakeTask> filter() {
            List<RakeTask> matching = new ArrayList<RakeTask>();
            Pattern pattern = getPattern();
            if (pattern != null) {
                for (RakeTask task : tasks) {
                    if (!showAll && task.getDescription() == null) {
                        continue;
                    }
                    Matcher m = pattern.matcher(task.getTask());
                    if (m.matches()) {
                        matching.add(task);
                    }
                }
            } else {
                List<RakeTask> exact = new ArrayList<RakeTask>();
                for (RakeTask task : tasks) {
                    if (!showAll && task.getDescription() == null) {
                        continue;
                    }
                    String taskLC = task.getTask().toLowerCase(Locale.US);
                    String filterLC = filter.toLowerCase(Locale.US);
                    if (taskLC.startsWith(filterLC)) {
                        // show tasks which start with the filter first
                        exact.add(task);
                    } else if (taskLC.contains(filterLC)) {
                        matching.add(task);
                    }
                }
                matching.addAll(0, exact);
            }
            return matching;
        }

        private Pattern getPattern() {
            if (filter.contains("?") || filter.contains("*")) {
                String reFilter = removeRegexpEscapes(filter);
                reFilter = reFilter.replace(".", "\\."); // NOI18N
                reFilter = reFilter.replace("?", "."); // NOI18N
                reFilter = reFilter.replace("*", ".*"); // NOI18N
                return Pattern.compile(".*" + reFilter + ".*", Pattern.CASE_INSENSITIVE); // NOI18N
            } else {
                return null;
            }
        }

        private static String removeRegexpEscapes(String text) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                switch (c) {
                    case '\\':
                        continue;
                    default:
                        sb.append(c);
                }
            }
            return sb.toString();
        }
    }

    /**
     * Holds a set of parameters and maintains info on what
     * parameter was the last one selected.
     */
    private static class ParameterContainer {

        private final Set<String> params = new HashSet<String>();
        private String lastSelected;

        public void addParam(String param) {
            params.add(param);
        }

        public String getLastSelected() {
            return lastSelected;
        }

        public void setLastSelected(String lastSelected) {
            this.lastSelected = lastSelected;
        }

        public Set<String> getParams() {
            return params;
        }

    }

}
