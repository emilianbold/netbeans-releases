/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.gulp.ui.customizer;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.gulp.GulpBuildTool;
import org.netbeans.modules.javascript.gulp.preferences.GulpPreferences;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public class GulpCustomizerPanel extends JPanel {

    private final ProjectCustomizer.Category category;
    private final GulpPreferences preferences;
    private final List<GulpTask> gulpTasks = new CopyOnWriteArrayList<>();


    GulpCustomizerPanel(ProjectCustomizer.Category category, Project project) {
        assert EventQueue.isDispatchThread();
        assert category != null;
        assert project != null;

        this.category = category;
        preferences = GulpBuildTool.forProject(project).getGulpPreferences();

        initComponents();
        init();
    }

    private void init() {
        gulpTasks.add(new GulpTask(ActionProvider.COMMAND_BUILD, "build", buildCheckBox, buildTextField)); // NOI18N
        gulpTasks.add(new GulpTask(ActionProvider.COMMAND_CLEAN, "clean", cleanCheckBox, cleanTextField)); // NOI18N
        gulpTasks.add(new GulpTask(ActionProvider.COMMAND_REBUILD, "clean build", rebuildCheckBox, rebuildTextField)); // NOI18N
        // default values
        for (GulpTask gulpTask : gulpTasks) {
            gulpTask.setText(preferences.getTask(gulpTask.getCommandId()));
        }
        // listeners
        category.setStoreListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveData();
            }
        });
    }

    @NbBundle.Messages("GulpCustomizerPanel.error.task.empty=Gulp task cannot be empty")
    void validateData() {
        assert EventQueue.isDispatchThread();
        for (GulpTask gulpTask : gulpTasks) {
            String text = gulpTask.getText();
            if (text != null
                    && text.isEmpty()) {
                category.setErrorMessage(Bundle.GulpCustomizerPanel_error_task_empty());
                category.setValid(false);
                return;
            }
        }
        category.setErrorMessage(" "); // NOI18N
        category.setValid(true);
    }

    void saveData() {
        assert !EventQueue.isDispatchThread();
        for (GulpTask gulpTask : gulpTasks) {
            preferences.setTask(gulpTask.getCommandId(), gulpTask.getText());
        }
    }


    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        assignLabel = new JLabel();
        buildCheckBox = new JCheckBox();
        cleanCheckBox = new JCheckBox();
        cleanTextField = new JTextField();
        rebuildCheckBox = new JCheckBox();
        rebuildTextField = new JTextField();
        buildTextField = new JTextField();

        Mnemonics.setLocalizedText(assignLabel, NbBundle.getMessage(GulpCustomizerPanel.class, "GulpCustomizerPanel.assignLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(buildCheckBox, NbBundle.getMessage(GulpCustomizerPanel.class, "GulpCustomizerPanel.buildCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(cleanCheckBox, NbBundle.getMessage(GulpCustomizerPanel.class, "GulpCustomizerPanel.cleanCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(rebuildCheckBox, NbBundle.getMessage(GulpCustomizerPanel.class, "GulpCustomizerPanel.rebuildCheckBox.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(assignLabel)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(cleanCheckBox)
                    .addComponent(rebuildCheckBox)
                    .addComponent(buildCheckBox))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(buildTextField)
                    .addComponent(rebuildTextField)
                    .addComponent(cleanTextField)))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(assignLabel)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(buildCheckBox)
                    .addComponent(buildTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(cleanCheckBox)
                    .addComponent(cleanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(rebuildCheckBox)
                    .addComponent(rebuildTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel assignLabel;
    private JCheckBox buildCheckBox;
    private JTextField buildTextField;
    private JCheckBox cleanCheckBox;
    private JTextField cleanTextField;
    private JCheckBox rebuildCheckBox;
    private JTextField rebuildTextField;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class GulpTask {

        private final String commandId;
        private final String defaultValue;
        private final JCheckBox checkBox;
        private final JTextField textField;


        GulpTask(String commandId, String defaultValue, JCheckBox checkBox, JTextField textField) {
            assert commandId != null;
            assert defaultValue != null;
            assert checkBox != null;
            assert textField != null;
            this.commandId = commandId;
            this.defaultValue = defaultValue;
            this.checkBox = checkBox;
            this.textField = textField;
            init();
        }

        private void init() {
            checkBox.addItemListener(new DefaultItemListener(textField));
            textField.getDocument().addDocumentListener(new DefaultDocumentListener());
        }

        public String getCommandId() {
            return commandId;
        }

        @CheckForNull
        public String getText() {
            if (!checkBox.isSelected()) {
                return null;
            }
            return textField.getText().trim();
        }

        public void setText(@NullAllowed String text) {
            boolean hasText = text != null;
            checkBox.setSelected(hasText);
            textField.setText(hasText ? text : defaultValue);
            textField.setEnabled(hasText);
        }

    }

    private final class DefaultItemListener implements ItemListener {

        private final JTextField textField;


        public DefaultItemListener(JTextField textField) {
            assert textField != null;
            this.textField = textField;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            textField.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            validateData();
        }

    }

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processChange();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processChange();
        }

        private void processChange() {
            validateData();
        }

    }

}
