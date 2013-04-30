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
package org.netbeans.modules.php.project.ui.actions.tests;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public final class SelectProviderPanel extends JPanel {

    private static final long serialVersionUID = -418400216571234657L;

    // @GuardedBy("EDT")
    private final List<PhpTestingProvider> providers;
    // @GuardedBy("EDT")
    private final ProvidersListModel providersListModel;

    private DialogDescriptor dialogDescriptor;
    private NotificationLineSupport notificationLineSupport;


    private SelectProviderPanel(List<PhpTestingProvider> providers) {
        assert EventQueue.isDispatchThread();
        assert providers != null;

        this.providers = providers;
        providersListModel = new ProvidersListModel(providers);

        initComponents();
        init();
    }

    @NbBundle.Messages("SelectProviderPanel.title=Select Testing Provider")
    @CheckForNull
    public static PhpTestingProvider open(List<PhpTestingProvider> providers) {
        final SelectProviderPanel panel = new SelectProviderPanel(providers);
        panel.dialogDescriptor = new DialogDescriptor(
                panel,
                Bundle.SelectProviderPanel_title(),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        panel.notificationLineSupport = panel.dialogDescriptor.createNotificationLineSupport();
        panel.validateSelection();
        if (DialogDisplayer.getDefault().notify(panel.dialogDescriptor) == DialogDescriptor.OK_OPTION) {
            return panel.getSelectedProvider();
        }
        return null;
    }

    private void init() {
        assert EventQueue.isDispatchThread();
        providersList.setModel(providersListModel);
        providersList.setCellRenderer(new ProviderRenderer());
        providersList.setSelectedIndex(0);
        // listeners
        providersList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                validateSelection();
            }
        });
    }

    private PhpTestingProvider getSelectedProvider() {
        return providersList.getSelectedValue();
    }

    @NbBundle.Messages("SelectProviderPanel.noneSelected=No provider selected.")
    void validateSelection() {
        assert notificationLineSupport != null;

        if (getSelectedProvider() == null) {
            notificationLineSupport.setErrorMessage(Bundle.SelectProviderPanel_noneSelected());
            dialogDescriptor.setValid(false);
            return;
        }
        notificationLineSupport.clearMessages();
        dialogDescriptor.setValid(true);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectProviderLabel = new JLabel();
        providersScrollPane = new JScrollPane();
        providersList = new JList<PhpTestingProvider>();

        selectProviderLabel.setLabelFor(providersList);
        Mnemonics.setLocalizedText(selectProviderLabel, NbBundle.getMessage(SelectProviderPanel.class, "SelectProviderPanel.selectProviderLabel.text")); // NOI18N

        providersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        providersList.setVisibleRowCount(4);
        providersScrollPane.setViewportView(providersList);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(selectProviderLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(providersScrollPane))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectProviderLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(providersScrollPane))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JList<PhpTestingProvider> providersList;
    private JScrollPane providersScrollPane;
    private JLabel selectProviderLabel;
    // End of variables declaration//GEN-END:variables

    private static final class ProvidersListModel extends AbstractListModel<PhpTestingProvider> {

        private static final long serialVersionUID = 1468733546876574L;

        // @GuardedBy("EDT)
        private final List<PhpTestingProvider> providers;


        public ProvidersListModel(List<PhpTestingProvider> providers) {
            assert EventQueue.isDispatchThread();
            this.providers = providers;
        }


        @Override
        public int getSize() {
            assert EventQueue.isDispatchThread();
            return providers.size();
        }

        @Override
        public PhpTestingProvider getElementAt(int index) {
            assert EventQueue.isDispatchThread();
            return providers.get(index);
        }

    }

    private static final class ProviderRenderer implements ListCellRenderer<PhpTestingProvider> {

        private final ListCellRenderer<Object> originalCellRenderer = new DefaultListCellRenderer();


        @Override
        public Component getListCellRendererComponent(JList<? extends PhpTestingProvider> list, PhpTestingProvider value, int index, boolean isSelected, boolean cellHasFocus) {
            return originalCellRenderer.getListCellRendererComponent(list, value.getDisplayName(), index, isSelected, cellHasFocus);
        }

    }

}
