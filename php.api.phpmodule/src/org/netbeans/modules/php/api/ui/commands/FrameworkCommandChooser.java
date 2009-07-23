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

package org.netbeans.modules.php.api.ui.commands;

import java.awt.event.ItemEvent;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.php.spi.commands.FrameworkCommand;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.spi.commands.FrameworkCommandSupport.CommandDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public final class FrameworkCommandChooser extends JPanel {
    private static final long serialVersionUID = 24055317521316402L;

    private static final Object NO_TASK_ITEM = getMessage("FrameworkCommandChooser.no.task"); // NOI18N
    private static final Object NO_MATCHING_TASK_ITEM = getMessage("FrameworkCommandChooser.no.matching.task"); // NOI18N
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    /** Remember checkbox state per IDE sessions. */
    private static boolean debug;

    /** Preselect lastly used task for more convenience. */
    private static String lastTask;

    private static boolean keepOpened = false;

    /** [project directory path -&gt; (task -&gt; parameters)] */
    private static final Map<String, Map<FrameworkCommand, ParameterContainer>> PROJECT_TO_TASK
            = new HashMap<String, Map<FrameworkCommand, ParameterContainer>>();

    private final PhpModule phpModule;

    private final List<FrameworkCommand> allTasks = new ArrayList<FrameworkCommand>();
    private final String frameworkName;
    private final JTextField taskParametersComboBoxEditor;

    private JButton runButton;
    private boolean refreshNeeded;

    private FrameworkCommandChooser(PhpModule phpModule, final JButton runButton, final String frameworkName) {
        assert phpModule != null;
        assert runButton != null;
        assert frameworkName != null;

        this.phpModule = phpModule;
        this.runButton = runButton;
        this.frameworkName = frameworkName;

        initComponents();
        taskParametersComboBoxEditor = (JTextField) taskParametersComboBox.getEditor().getEditorComponent();
        matchingTaskList.setCellRenderer(new FrameworkCommandChooser.FrameworkCommandRenderer());
        debugCheckbox.setSelected(debug);
        keepOpenedCheckBox.setSelected(keepOpened);
        keepOpenedCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                keepOpened = e.getStateChange() == ItemEvent.SELECTED;
            }
        });
        refreshNeeded = reloadAllTasks();
        refreshTaskList();
        taskField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { refreshTaskList(); }
            public void insertUpdate(DocumentEvent e) { refreshTaskList(); }
            public void removeUpdate(DocumentEvent e) { refreshTaskList(); }
        });
        preselectLastlySelected();
        initTaskParameters();
        updateHelp();
        updatePreview();
    }

    public static void open(final PhpModule phpModule, String frameworkName, final FrameworkCommandSupport.RunCommandListener runCommandListener) {
        assert runCommandListener != null;
        assert EventQueue.isDispatchThread() : "must be called from EDT";

        final JButton runButton = new JButton(getMessage("FrameworkCommandChooser.runButton")); // NOI18N
        final FrameworkCommandChooser chooserPanel = new FrameworkCommandChooser(phpModule, runButton, frameworkName);
        String title = getMessage("FrameworkCommandChooser.title", frameworkName, phpModule.getDisplayName()); // NOI18N

        runButton.getAccessibleContext().setAccessibleDescription(getMessage("FrameworkCommandChooser.runButton.accessibleDescription", frameworkName)); // NOI18N
        setRunButtonState(runButton, chooserPanel);
        chooserPanel.matchingTaskList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                setRunButtonState(runButton, chooserPanel);
                chooserPanel.initTaskParameters();
                chooserPanel.updateHelp();
                chooserPanel.updatePreview();
            }
        });

        final JButton refreshButton = new JButton();
        Mnemonics.setLocalizedText(refreshButton, getMessage("FrameworkCommandChooser.refreshButton")); // NOI18N
        refreshButton.getAccessibleContext().setAccessibleDescription(getMessage("FrameworkCommandChooser.refreshButton.accessibleDescription", frameworkName));  // NOI18N
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

        final DialogDescriptor descriptor = new DialogDescriptor(chooserPanel, title, true,
                options, runButton, DialogDescriptor.DEFAULT_ALIGN, null, null);
        descriptor.setClosingOptions(new Object[] {DialogDescriptor.CANCEL_OPTION});
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleName(getMessage("FrameworkCommandChooser.accessibleName", frameworkName)); // NOI18N
        dialog.getAccessibleContext().setAccessibleDescription(getMessage("FrameworkCommandChooser.accessibleDescription", frameworkName)); // NOI18N

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

        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!chooserPanel.keepOpenedCheckBox.isSelected()) {
                    dialog.setVisible(false);
                }
                FrameworkCommand task = chooserPanel.getSelectedTask();
                FrameworkCommandChooser.debug = chooserPanel.debugCheckbox.isSelected();
                FrameworkCommandChooser.lastTask = task.getCommand();
                chooserPanel.storeParameters();

                runCommandListener.runCommand(new CommandDescriptor(task, chooserPanel.getParameters(), FrameworkCommandChooser.debug));
            }
        });

        try {
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }
    }

    void initTaskParameters() {
        FrameworkCommand task = getSelectedTask();
        List<? super Object> params = new ArrayList<Object>();
        // no param option for convenience
        params.add(""); //NOI18N
        params.addAll(getStoredParams(task));
        // FIXME from ruby
        //params.addAll(RakeParameters.getParameters(task, project));
        taskParametersComboBox.setModel(new DefaultComboBoxModel(params.toArray()));
        preselectLastSelectedParam(task);
        taskParametersComboBoxEditor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                processUpdate();
            }
            public void removeUpdate(DocumentEvent e) {
                processUpdate();
            }
            public void changedUpdate(DocumentEvent e) {
                processUpdate();
            }
            private void processUpdate() {
                updatePreview();
            }
        });
    }

    void updateHelp() {
        final FrameworkCommand task = getSelectedTask();
        if (task == null) {
            updateHelp(null);
        } else if (task.hasHelp()) {
            updateHelp(task.getHelp());
        } else {
            updateHelp(getMessage("LBL_PleaseWait")); // NOI18N
            EXECUTOR.submit(new Runnable() {
                public void run() {
                    final String help = task.getHelp();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            updateHelp(help);
                        }
                    });
                }
            });
        }
    }

    void updateHelp(String help) {
        assert SwingUtilities.isEventDispatchThread() : "must be run in EDT";
        helpTextArea.setText(help);
        helpTextArea.setCaretPosition(0);
    }

    void updatePreview() {
        FrameworkCommand task = getSelectedTask();
        String preview = null;
        if (task != null) {
            preview = task.getPreview() + " " + taskParametersComboBoxEditor.getText(); // NOI18N
        }
        previewTextField.setText(preview);
    }

    /**
     * Pre-selects the parameter that was last selected for the
     * given task.
     *
     * @param task
     */
    private void preselectLastSelectedParam(FrameworkCommand task) {
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

    private Map<FrameworkCommand, ParameterContainer> getTasksToParams() {
        String prjDir = phpModule.getSourceDirectory().getPath();
        Map<FrameworkCommand, ParameterContainer> result = PROJECT_TO_TASK.get(prjDir);
        if (result == null) {
            result = new HashMap<FrameworkCommand, ParameterContainer>();
            PROJECT_TO_TASK.put(prjDir, result);
        }
        return result;
    }

    private List<String> getStoredParams(FrameworkCommand task) {
        if (task == null) {
            return Collections.<String>emptyList();
        }
        final Map<FrameworkCommand, ParameterContainer> tasksToParams = getTasksToParams();
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

    private static void setRunButtonState(final JButton runButton, final FrameworkCommandChooser chooserPanel) {
        runButton.setEnabled(chooserPanel.getSelectedTask() != null);
    }

    /**
     * Stores the param that the user entered in the params combo
     * box.
     */
    private void storeParameters() {
        String prjDir = phpModule.getSourceDirectory().getPath();
        Map<FrameworkCommand, ParameterContainer> taskToParams = PROJECT_TO_TASK.get(prjDir);
        if (taskToParams == null) {
            taskToParams = new HashMap<FrameworkCommand, ParameterContainer>();
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
        for (FrameworkCommand task : allTasks) {
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
        List<FrameworkCommand> commands = FrameworkCommandSupport.forPhpModule(phpModule).getFrameworkCommands();
        if (commands != null) {
            allTasks.addAll(commands);
            return false;
        }
        return true;
    }

    private void refreshTaskList() {
        String filter = taskField.getText().trim();
        DefaultListModel model = new DefaultListModel();
        List<FrameworkCommand> matching = Filter.getFilteredTasks(allTasks, filter);

        for (FrameworkCommand task : matching) {
            model.addElement(task);
        }
        matchingTaskList.setModel(model);
        if (model.isEmpty()) {
            if (allTasks.isEmpty()) {
                model.addElement(NO_TASK_ITEM);
            } else {
                model.addElement(NO_MATCHING_TASK_ITEM);
            }
        }
        matchingTaskList.setSelectedIndex(0);
        initTaskParameters();
    }

    private void reloadTasks(final Runnable uiFinishAction) {
        final Object task = matchingTaskList.getSelectedValue();
        final JComponent[] comps = new JComponent[] {
            matchingTaskSP, matchingTaskLabel, matchingTaskLabel, matchingTaskList,
            taskLabel, taskField, debugCheckbox,
            taskParamLabel, taskParametersComboBox, taskHint
        };
        setEnabled(comps, false);
        matchingTaskList.setListData(new Object[]{getMessage("FrameworkCommandChooser.reloading.tasks", frameworkName)}); // NOI18N

        FrameworkCommandSupport.forPhpModule(phpModule).refreshFrameworkCommandsLater(new Runnable() {
            public void run() {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        reloadAllTasks();
                        refreshTaskList();
                        matchingTaskList.setSelectedValue(task, true);
                        uiFinishAction.run();
                        setEnabled(comps, true);
                        taskField.requestFocus();
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

    private FrameworkCommand getSelectedTask() {
        Object val = matchingTaskList.getSelectedValue();
        if (val != null
                && !NO_MATCHING_TASK_ITEM.equals(val)
                && !NO_TASK_ITEM.equals(val)) {
            return (FrameworkCommand) val;
        }
        return null;
    }

    private static String getMessage(final String key, final String... args) {
        return NbBundle.getMessage(FrameworkCommandChooser.class, key, args);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        debugCheckbox = new javax.swing.JCheckBox();
        taskLabel = new javax.swing.JLabel();
        taskFieldPanel = new javax.swing.JPanel();
        taskField = new javax.swing.JTextField();
        taskHint = new javax.swing.JLabel();
        taskParamLabel = new javax.swing.JLabel();
        taskParametersComboBox = new javax.swing.JComboBox();
        matchingTaskLabel = new javax.swing.JLabel();
        splitPane = new javax.swing.JSplitPane();
        matchingTaskSP = new javax.swing.JScrollPane();
        matchingTaskList = new javax.swing.JList();
        helpScrollPane = new javax.swing.JScrollPane();
        helpTextArea = new javax.swing.JTextArea();
        previewTextField = new javax.swing.JTextField();
        previewLabel = new javax.swing.JLabel();
        keepOpenedCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(debugCheckbox, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.debugCheckbox.text")); // NOI18N

        taskLabel.setLabelFor(taskField);
        org.openide.awt.Mnemonics.setLocalizedText(taskLabel, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskLabel.text")); // NOI18N

        taskFieldPanel.setLayout(new java.awt.BorderLayout());

        taskField.setText(org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskField.text")); // NOI18N
        taskField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                taskFieldKeyPressed(evt);
            }
        });
        taskFieldPanel.add(taskField, java.awt.BorderLayout.NORTH);

        org.openide.awt.Mnemonics.setLocalizedText(taskHint, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskHint.text")); // NOI18N
        taskFieldPanel.add(taskHint, java.awt.BorderLayout.SOUTH);

        org.openide.awt.Mnemonics.setLocalizedText(taskParamLabel, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.taskParamLabel.text")); // NOI18N

        taskParametersComboBox.setEditable(true);

        matchingTaskLabel.setLabelFor(matchingTaskList);
        org.openide.awt.Mnemonics.setLocalizedText(matchingTaskLabel, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.matchingTaskLabel.text")); // NOI18N

        splitPane.setBorder(null);
        splitPane.setDividerLocation(150);
        splitPane.setDividerSize(5);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        matchingTaskList.setFont(new java.awt.Font("Monospaced", 0, 12));
        matchingTaskList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        matchingTaskList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                matchingTaskListMouseClicked(evt);
            }
        });
        matchingTaskSP.setViewportView(matchingTaskList);

        splitPane.setTopComponent(matchingTaskSP);

        helpTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        helpTextArea.setColumns(20);
        helpTextArea.setEditable(false);
        helpTextArea.setRows(5);
        helpScrollPane.setViewportView(helpTextArea);

        splitPane.setRightComponent(helpScrollPane);

        previewTextField.setEditable(false);

        previewLabel.setLabelFor(previewTextField);
        org.openide.awt.Mnemonics.setLocalizedText(previewLabel, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.previewLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keepOpenedCheckBox, org.openide.util.NbBundle.getMessage(FrameworkCommandChooser.class, "FrameworkCommandChooser.keepOpenedCheckBox.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(keepOpenedCheckBox)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(taskLabel)
                            .add(taskParamLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(taskParametersComboBox, 0, 575, Short.MAX_VALUE)
                            .add(taskFieldPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, matchingTaskLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(previewLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(previewTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(taskLabel)
                    .add(taskFieldPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(taskParamLabel)
                    .add(taskParametersComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(matchingTaskLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(previewTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(previewLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(keepOpenedCheckBox))
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

    private void taskFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_taskFieldKeyPressed
        handleNavigationKeys(evt);
    }//GEN-LAST:event_taskFieldKeyPressed

    private void matchingTaskListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_matchingTaskListMouseClicked
        if (runButton.isEnabled() && evt.getClickCount() == 2) {
            runButton.doClick();
        }
    }//GEN-LAST:event_matchingTaskListMouseClicked

    private static class FrameworkCommandRenderer extends JLabel implements ListCellRenderer {
        private static final long serialVersionUID = -6180208903089211882L;

        public FrameworkCommandRenderer() {
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

            if (value instanceof FrameworkCommand) {
                FrameworkCommand task = (FrameworkCommand) value;
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
    private javax.swing.JScrollPane helpScrollPane;
    private javax.swing.JTextArea helpTextArea;
    private javax.swing.JCheckBox keepOpenedCheckBox;
    private javax.swing.JLabel matchingTaskLabel;
    private javax.swing.JList matchingTaskList;
    private javax.swing.JScrollPane matchingTaskSP;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JTextField previewTextField;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JTextField taskField;
    private javax.swing.JPanel taskFieldPanel;
    private javax.swing.JLabel taskHint;
    private javax.swing.JLabel taskLabel;
    private javax.swing.JLabel taskParamLabel;
    private javax.swing.JComboBox taskParametersComboBox;
    // End of variables declaration//GEN-END:variables

    static final class Filter {

        private final String filter;
        private final List<FrameworkCommand> tasks;

        private Filter(List<FrameworkCommand> tasks, String filter) {
            this.tasks = tasks;
            this.filter = filter;
        }

        static List<FrameworkCommand> getFilteredTasks(List<FrameworkCommand> allTasks, String filter) {
            Filter f = new Filter(allTasks, filter);
            return f.filter();
        }

        private List<FrameworkCommand> filter() {
            List<FrameworkCommand> matching = new ArrayList<FrameworkCommand>();
            Pattern pattern = StringUtils.getPattern(filter);
            if (pattern != null) {
                for (FrameworkCommand task : tasks) {
                    Matcher m = pattern.matcher(task.getCommand());
                    if (m.matches()) {
                        matching.add(task);
                    }
                }
            } else {
                List<FrameworkCommand> exact = new ArrayList<FrameworkCommand>();
                for (FrameworkCommand task : tasks) {
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
