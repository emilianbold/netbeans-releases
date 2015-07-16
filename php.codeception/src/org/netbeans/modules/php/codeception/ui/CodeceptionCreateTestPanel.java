/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.codeception.ui;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.codeception.commands.Codecept;
import org.netbeans.modules.php.codeception.commands.Codecept.GenerateCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public class CodeceptionCreateTestPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = -7767663312729593009L;
    private final List<GenerateCommand> commands;
    private final Set<String> suites;

    /**
     * Creates new form CodeceptionSuitesPanel
     */
    private CodeceptionCreateTestPanel(List<GenerateCommand> commands, List<String> suites) {
        assert EventQueue.isDispatchThread();
        assert suites != null;
        assert commands != null;

        this.commands = new ArrayList<>(commands);
        this.suites = new HashSet<>(suites);
        initComponents();
        init();
    }

    private void init() {
        for (String suite : this.suites) {
            suitesComboBox.addItem(suite);
        }
        for (GenerateCommand command : this.commands) {
            generateComboBox.addItem(command);
        }
    }

    @NbBundle.Messages("CodeceptionCreateTestPanel.dialog.title=Create Test")
    @CheckForNull
    public static Pair<GenerateCommand, String> showDialog(List<GenerateCommand> commands, List<String> suites) {
        final List<String> suitesCopy = new CopyOnWriteArrayList<>(suites);
        final List<GenerateCommand> commandsCopy = new CopyOnWriteArrayList<>(commands);
        return Mutex.EVENT.readAccess(new Mutex.Action<Pair<GenerateCommand, String>>() {
            @Override
            public Pair<GenerateCommand, String> run() {
                assert EventQueue.isDispatchThread();
                CodeceptionCreateTestPanel panel = new CodeceptionCreateTestPanel(commandsCopy, suitesCopy);
                NotifyDescriptor descriptor = new NotifyDescriptor(
                        panel,
                        Bundle.CodeceptionCreateTestPanel_dialog_title(),
                        NotifyDescriptor.OK_CANCEL_OPTION,
                        NotifyDescriptor.PLAIN_MESSAGE,
                        null,
                        NotifyDescriptor.OK_OPTION);
                if (DialogDisplayer.getDefault().notify(descriptor) != NotifyDescriptor.OK_OPTION) {
                    return null;
                }
                GenerateCommand selectedCommand = panel.getSelectedCommand();
                String selectedSuite = panel.getSelectedSuite();
                if (selectedCommand == null || selectedSuite == null) {
                    return null;
                }
                return Pair.of(panel.getSelectedCommand(), panel.getSelectedSuite());
            }
        });
    }

    public String getSelectedSuite() {
        return (String) suitesComboBox.getSelectedItem();
    }

    public GenerateCommand getSelectedCommand() {
        return (GenerateCommand) generateComboBox.getSelectedItem();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        generateComboBox = new javax.swing.JComboBox<Codecept.GenerateCommand>();
        suitesComboBox = new javax.swing.JComboBox<String>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CodeceptionCreateTestPanel.class, "CodeceptionCreateTestPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CodeceptionCreateTestPanel.class, "CodeceptionCreateTestPanel.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(suitesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(generateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 12, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(generateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(suitesComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<Codecept.GenerateCommand> generateComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox<String> suitesComboBox;
    // End of variables declaration//GEN-END:variables
}
