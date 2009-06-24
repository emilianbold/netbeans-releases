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

package org.netbeans.modules.php.symfony.ui.commands;

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
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public final class SymfonyCommandChooser extends JPanel {
    private static final long serialVersionUID = 2405531380316402L;

    private final static Object NO_TASK_ITEM = getMessage("SymfonyCommandChooser.no.matching.task"); // NOI18N

    /** Remember checkbox state per IDE sessions. */
    private static boolean debug;

    /** Preselect lastly used task for more convenience. */
    private static String lastTask;

    /** [project directory path -&gt; (task -&gt; parameters)] */
    private static final Map<String, Map<SymfonyCommand, ParameterContainer>> PROJECT_TO_TASK
            = new HashMap<String, Map<SymfonyCommand, ParameterContainer>>();

    private final PhpModule phpModule;

    private final List<SymfonyCommand> allTasks = new ArrayList<SymfonyCommand>();

    private JButton runButton;
    private boolean refreshNeeded;

    /**
     * Show the Rake Chooser and returns the Rake task selected by the user.
     */
    public static CommandDescriptor select(final PhpModule phpModule) {
        assert EventQueue.isDispatchThread() : "must be called from EDT";
        final JButton runButton = new JButton(getMessage("SymfonyCommandChooser.runButton"));
        final SymfonyCommandChooser chooserPanel = new SymfonyCommandChooser(phpModule, runButton);
        String title = getMessage("SymfonyCommandChooser.title", phpModule.getDisplayName());

        runButton.getAccessibleContext().setAccessibleDescription (getMessage("SymfonyCommandChooser.runButton.accessibleDescription"));
        setRunButtonState(runButton, chooserPanel);
        chooserPanel.matchingTaskList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                setRunButtonState(runButton, chooserPanel);
                chooserPanel.initTaskParameters();
            }
        });

        final JButton refreshButton = new JButton();
        Mnemonics.setLocalizedText(refreshButton, getMessage("SymfonyCommandChooser.refreshButton"));
        refreshButton.getAccessibleContext().setAccessibleDescription (getMessage("SymfonyCommandChooser.refreshButton.accessibleDescription"));
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

        Object[] options = new Object[] {
            refreshButton,
            runButton,
            DialogDescriptor.CANCEL_OPTION
        };

        DialogDescriptor descriptor = new DialogDescriptor(chooserPanel, title, true,
                options, runButton, DialogDescriptor.DEFAULT_ALIGN, null, null);
        descriptor.setClosingOptions(new Object[] { runButton, DialogDescriptor.CANCEL_OPTION });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleName(getMessage("SymfonyCommandChooser.accessibleName"));
        dialog.getAccessibleContext().setAccessibleDescription(getMessage("SymfonyCommandChooser.accessibleDescription"));

        if (chooserPanel.refreshNeeded) {
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

        dialog.setVisible(true);
        if (descriptor.getValue() == runButton) {
            SymfonyCommand task = chooserPanel.getSelectedTask();
            SymfonyCommandChooser.debug = chooserPanel.debugCheckbox.isSelected();
            SymfonyCommandChooser.lastTask = task.getCommand();
            chooserPanel.storeParameters();
            return new CommandDescriptor(task, chooserPanel.getParameters(), SymfonyCommandChooser.debug);
        }
        return null;
    }

    private void initTaskParameters() {
        SymfonyCommand task = getSelectedTask();
        List<? super Object> params = new ArrayList<Object>();
        // no param option for convenience
        params.add(""); //NOI18N
        params.addAll(getStoredParams(task));
        // FIXME from ruby
        //params.addAll(RakeParameters.getParameters(task, project));
        taskParametersComboBox.setModel(new DefaultComboBoxModel(params.toArray()));
        preselectLastSelectedParam(task);
    }

    /**
     * Pre-selects the parameter that was last selected for the
     * given task.
     *
     * @param task
     */
    private void preselectLastSelectedParam(SymfonyCommand task) {
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
            if (item.equals(lastSelected)) {
                taskParametersComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private Map<SymfonyCommand,ParameterContainer> getTasksToParams() {
        String prjDir = phpModule.getSourceDirectory().getPath();
        Map<SymfonyCommand,ParameterContainer> result = PROJECT_TO_TASK.get(prjDir);
        if (result == null) {
            result = new HashMap<SymfonyCommand,ParameterContainer>();
            PROJECT_TO_TASK.put(prjDir, result);
        }
        return result;
    }

    private List<String> getStoredParams(SymfonyCommand task) {
        if (task == null) {
            return Collections.<String>emptyList();
        }
        final Map<SymfonyCommand, ParameterContainer> tasksToParams = getTasksToParams();
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
        return selected.toString().trim();
    }

    private static void setRunButtonState(final JButton runButton, final SymfonyCommandChooser chooserPanel) {
        runButton.setEnabled(chooserPanel.getSelectedTask() != null);
    }

    public static class CommandDescriptor {

        private final SymfonyCommand task;
        private final String[] params;
        private final boolean debug;

        private CommandDescriptor(SymfonyCommand task, String params, boolean debug) {
            assert task != null;
            assert params != null;

            this.task = task;
            this.params = Utilities.parseParameters(params.trim());
            this.debug = debug;
        }

        public SymfonyCommand getSymfonyCommand() {
            return task;
        }

        public String[] getCommandParams() {
            return params;
        }

        public boolean isDebug() {
            return debug;
        }
    }

    private SymfonyCommandChooser(PhpModule phpModule, final JButton runButton) {
        this.runButton = runButton;
        this.phpModule = phpModule;
        initComponents();
        matchingTaskList.setCellRenderer(new SymfonyCommandChooser.SymfonyCommandRenderer());
        debugCheckbox.setSelected(debug);
        refreshNeeded = reloadAllTasks();
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
        String prjDir = phpModule.getSourceDirectory().getPath();
        Map<SymfonyCommand, ParameterContainer> taskToParams = PROJECT_TO_TASK.get(prjDir);
        if (taskToParams == null) {
            taskToParams = new HashMap<SymfonyCommand, ParameterContainer>();
            PROJECT_TO_TASK.put(prjDir, taskToParams);
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
        for (SymfonyCommand task : allTasks) {
            if (lastTask.equals(task.getCommand())) {
                matchingTaskList.setSelectedValue(task, true);
                break;
            }
        }
        initTaskParameters();
    }

    /** Reloads all tasks for the current project. */
    private boolean reloadAllTasks() {
        allTasks.clear();
        List<SymfonyCommand> commands = SymfonyCommandSupport.forPhpModule(phpModule).getSymfonyCommands();
        if (commands != null) {
            allTasks.addAll(commands);
            return false;
        }
        return true;
    }

    /** Refreshes Rake tasks list view. */
    private void refreshTaskList() {
        String filter = rakeTaskField.getText().trim();
        DefaultListModel model = new DefaultListModel();
        List<SymfonyCommand> matching = Filter.getFilteredTasks(allTasks, filter);

        for (SymfonyCommand task : matching) {
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
            taskParamLabel, taskParametersComboBox, rakeTaskHint
        };
        setEnabled(comps, false);
        matchingTaskList.setListData(new Object[]{getMessage("SymfonyCommandChooser.reloading.tasks")});

        SymfonyCommandSupport.forPhpModule(phpModule).refreshSymfonyCommandsLater(new Runnable() {
            public void run() {
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
        });
    }

    private void setEnabled(final JComponent[] comps, final boolean enabled) {
        for (JComponent comp : comps) {
            comp.setEnabled(enabled);
        }

    }

    private SymfonyCommand getSelectedTask() {
        Object val = matchingTaskList.getSelectedValue();
        if (val != null && !NO_TASK_ITEM.equals(val)) {
            return (SymfonyCommand) val;
        }
        return null;
    }

    private static String getMessage(final String key, final String... args) {
        return NbBundle.getMessage(SymfonyCommandChooser.class, key, args);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        debugCheckbox = new javax.swing.JCheckBox();
        rakeTaskLabel = new javax.swing.JLabel();
        taskParamLabel = new javax.swing.JLabel();
        matchingTaskLabel = new javax.swing.JLabel();
        matchingTaskSP = new javax.swing.JScrollPane();
        matchingTaskList = new javax.swing.JList();
        rakeTaskFieldPanel = new javax.swing.JPanel();
        rakeTaskField = new javax.swing.JTextField();
        rakeTaskHint = new javax.swing.JLabel();
        taskParametersComboBox = new javax.swing.JComboBox();

        org.openide.awt.Mnemonics.setLocalizedText(debugCheckbox, org.openide.util.NbBundle.getMessage(SymfonyCommandChooser.class, "SymfonyCommandChooser.debugCheckbox.text")); // NOI18N

        rakeTaskLabel.setLabelFor(rakeTaskField);
        org.openide.awt.Mnemonics.setLocalizedText(rakeTaskLabel, org.openide.util.NbBundle.getMessage(SymfonyCommandChooser.class, "SymfonyCommandChooser.rakeTaskLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(taskParamLabel, org.openide.util.NbBundle.getMessage(SymfonyCommandChooser.class, "SymfonyCommandChooser.taskParamLabel.text")); // NOI18N

        matchingTaskLabel.setLabelFor(matchingTaskList);
        org.openide.awt.Mnemonics.setLocalizedText(matchingTaskLabel, org.openide.util.NbBundle.getMessage(SymfonyCommandChooser.class, "SymfonyCommandChooser.matchingTaskLabel.text")); // NOI18N

        matchingTaskList.setFont(new java.awt.Font("Monospaced", 0, 12));
        matchingTaskList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        matchingTaskList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                matchingTaskListMouseClicked(evt);
            }
        });
        matchingTaskSP.setViewportView(matchingTaskList);

        rakeTaskFieldPanel.setLayout(new java.awt.BorderLayout());

        rakeTaskField.setText(org.openide.util.NbBundle.getMessage(SymfonyCommandChooser.class, "SymfonyCommandChooser.rakeTaskField.text")); // NOI18N
        rakeTaskField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                rakeTaskFieldKeyPressed(evt);
            }
        });
        rakeTaskFieldPanel.add(rakeTaskField, java.awt.BorderLayout.NORTH);

        org.openide.awt.Mnemonics.setLocalizedText(rakeTaskHint, org.openide.util.NbBundle.getMessage(SymfonyCommandChooser.class, "SymfonyCommandChooser.rakeTaskHint.text")); // NOI18N
        rakeTaskFieldPanel.add(rakeTaskHint, java.awt.BorderLayout.SOUTH);

        taskParametersComboBox.setEditable(true);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, matchingTaskSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rakeTaskLabel)
                            .add(taskParamLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(taskParametersComboBox, 0, 575, Short.MAX_VALUE)
                            .add(rakeTaskFieldPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, matchingTaskLabel))
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
                .add(matchingTaskSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
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

    private void matchingTaskListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_matchingTaskListMouseClicked
        if (runButton.isEnabled() && evt.getClickCount() == 2) {
            runButton.doClick();
        }
    }//GEN-LAST:event_matchingTaskListMouseClicked

    private static class SymfonyCommandRenderer extends JLabel implements ListCellRenderer {
        private static final long serialVersionUID = -6180208903089211882L;

        public SymfonyCommandRenderer() {
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

            if (value instanceof SymfonyCommand) {
                SymfonyCommand task = (SymfonyCommand) value;
                String descripton = task.getDescription();
                StringBuilder text = new StringBuilder("<html>"); // NOI18N
                text.append("<b>").append(task.getCommand()).append("</b>"); // NOI18N
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
    private javax.swing.JLabel taskParamLabel;
    private javax.swing.JComboBox taskParametersComboBox;
    // End of variables declaration//GEN-END:variables

    static final class Filter {

        private final String filter;
        private final List<SymfonyCommand> tasks;

        private Filter(List<SymfonyCommand> tasks, String filter) {
            this.tasks = tasks;
            this.filter = filter;
        }

        static List<SymfonyCommand> getFilteredTasks(List<SymfonyCommand> allTasks, String filter) {
            Filter f = new Filter(allTasks, filter);
            return f.filter();
        }

        private List<SymfonyCommand> filter() {
            List<SymfonyCommand> matching = new ArrayList<SymfonyCommand>();
            Pattern pattern = getPattern();
            if (pattern != null) {
                for (SymfonyCommand task : tasks) {
                    Matcher m = pattern.matcher(task.getCommand());
                    if (m.matches()) {
                        matching.add(task);
                    }
                }
            } else {
                List<SymfonyCommand> exact = new ArrayList<SymfonyCommand>();
                for (SymfonyCommand task : tasks) {
                    String taskLC = task.getCommand().toLowerCase(Locale.ENGLISH);
                    String filterLC = filter.toLowerCase(Locale.ENGLISH);
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
