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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class RakeTaskChooser extends JPanel {

    private final List<RakeTask> allTasks;

    /**
     * Show the Rake Chooser and returns the Rake task selected by the user.
     */
    static TaskDescriptor select(final RubyBaseProject project) {
        RakeTaskChooser panel = new RakeTaskChooser(project);
        String title = NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.title");
        DialogDescriptor descriptor = new DialogDescriptor(panel, title);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.accessibleName"));
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.accessibleDescription"));
        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    dialog.setVisible(true);
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        if (descriptor.getValue() == NotifyDescriptor.OK_OPTION) {
            RakeTask task = (RakeTask) panel.matchingTaskList.getSelectedValue();
            return new TaskDescriptor(task, panel.debugCheckbox.isSelected());
        }
        return null;
    }

    static class TaskDescriptor {
        
        private final RakeTask task;
        private final boolean debug;

        TaskDescriptor(RakeTask task, boolean debug) {
            this.task = task;
            this.debug = debug;
        }

        RakeTask getRakeTask() {
            return task;
        }

        boolean isDebug() {
            return debug;
        }
    }
    
    private RakeTaskChooser(RubyBaseProject project) {
        initComponents();
        List<RakeTask> taskTree = RakeSupport.getRakeTaskTree(project);
        this.allTasks = new ArrayList<RakeTask>();
        addTasks(allTasks, taskTree);
        refreshTaskList();
        matchingTaskList.setCellRenderer(new RakeTaskChooser.RakeTaskRenderer());
        rakeTaskField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { refreshTaskList(); }
            public void insertUpdate(DocumentEvent e) { refreshTaskList(); }
            public void removeUpdate(DocumentEvent e) { refreshTaskList(); }
        });
    }

    private static void addTasks(final List<RakeTask> flatAccumulator, final List<RakeTask> taskTree) {
        for (RakeTask task : taskTree) {
            if (task.isNameSpace()) {
                addTasks(flatAccumulator, task.getChildren());
            } else {
                flatAccumulator.add(task);
            }
        }
    }

    private void refreshTaskList() {
        String filter = rakeTaskField.getText().trim();
        DefaultListModel model = new DefaultListModel();
        for (RakeTask task : allTasks) {
            if (task.getTask().toLowerCase(Locale.US).contains(filter.toLowerCase(Locale.US))) {
                model.addElement(task);
            }
        }
        matchingTaskList.setModel(model);
        matchingTaskList.setSelectedIndex(0);
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
        matchingTaskLabel = new javax.swing.JLabel();
        matchingTaskSP = new javax.swing.JScrollPane();
        matchingTaskList = new javax.swing.JList();
        debugCheckbox = new javax.swing.JCheckBox();

        rakeTaskLabel.setLabelFor(rakeTaskField);
        org.openide.awt.Mnemonics.setLocalizedText(rakeTaskLabel, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.rakeTaskLabel.text")); // NOI18N

        rakeTaskField.setText(org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.rakeTaskField.text")); // NOI18N
        rakeTaskField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                rakeTaskFieldKeyPressed(evt);
            }
        });

        matchingTaskLabel.setLabelFor(matchingTaskList);
        org.openide.awt.Mnemonics.setLocalizedText(matchingTaskLabel, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.matchingTaskLabel.text")); // NOI18N

        matchingTaskSP.setViewportView(matchingTaskList);

        org.openide.awt.Mnemonics.setLocalizedText(debugCheckbox, org.openide.util.NbBundle.getMessage(RakeTaskChooser.class, "RakeTaskChooser.debugCheckbox.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, matchingTaskSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 612, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, rakeTaskLabel)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, matchingTaskLabel)
                    .add(layout.createSequentialGroup()
                        .add(rakeTaskField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(debugCheckbox)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(rakeTaskLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(debugCheckbox)
                    .add(rakeTaskField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(matchingTaskLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(matchingTaskSP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rakeTaskFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rakeTaskFieldKeyPressed
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
        if ("selectNextRow".equals(actionKey) && selectedIndex == modelSize - 1) {
            matchingTaskList.setSelectedIndex(0);
            matchingTaskList.ensureIndexIsVisible(0);
            return;
        } else if ("selectPreviousRow".equals(actionKey) && selectedIndex == 0) {
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
    }//GEN-LAST:event_rakeTaskFieldKeyPressed

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
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            RakeTask task = ((RakeTask) value);
            StringBuilder text = new StringBuilder("<html>");
            text.append("<b>").append(task.getTask()).append("</b>");
            if (task.getDescription() != null) {
                text.append(" : ").append(task.getDescription());
            }
            text.append("</html>");
            setText(text.toString());

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
    // End of variables declaration//GEN-END:variables

}
