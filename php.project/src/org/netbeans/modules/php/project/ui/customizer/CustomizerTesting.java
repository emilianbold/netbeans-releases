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
package org.netbeans.modules.php.project.ui.customizer;

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import org.netbeans.modules.php.api.testing.PhpTesting;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;


public class CustomizerTesting extends JPanel {

    private static final long serialVersionUID = -654768735165768L;

    private final ProjectCustomizer.Category category;
    private final PhpProjectProperties uiProps;
    final Map<String, TestingProviderPanel> testingPanels;
    // @GuardedBy("EDT")
    final Set<String> selectedTestingProviders = new TreeSet<>();


    CustomizerTesting(ProjectCustomizer.Category category, PhpProjectProperties uiProps, Map<String, TestingProviderPanel> testingPanels) {
        assert category != null;
        assert uiProps != null;
        assert testingPanels != null;

        this.category = category;
        this.uiProps = uiProps;
        this.testingPanels = testingPanels;

        initComponents();
        init();
    }

    private void init() {
        initProvidersPanel();
    }

    @NbBundle.Messages("CustomizerTesting.testingProviders.noneInstalled=No PHP testing provider found, install one via Plugins (e.g. PHPUnit).")
    private void initProvidersPanel() {
        List<PhpTestingProvider> allTestingProviders = PhpTesting.getTestingProviders();
        if (allTestingProviders.isEmpty()) {
            category.setErrorMessage(Bundle.CustomizerTesting_testingProviders_noneInstalled());
            category.setValid(true);
            return;
        }
        List<String> currentTestingProviders = uiProps.getTestingProviders();
        GroupLayout providersPanelLayout = new GroupLayout(providersPanel);
        GroupLayout.ParallelGroup horizontalGroup = providersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup verticalGroup = providersPanelLayout.createSequentialGroup();
        boolean first = true;
        for (PhpTestingProvider testingProvider : allTestingProviders) {
            String identifier = testingProvider.getIdentifier();
            JCheckBox checkBox = new JCheckBox(testingProvider.getDisplayName());
            checkBox.addItemListener(new TestingProviderListener(identifier));
            if (currentTestingProviders.contains(identifier)) {
                checkBox.setSelected(true);
            }
            horizontalGroup.addComponent(checkBox);
            verticalGroup.addComponent(checkBox);
            if (first) {
                first = false;
            } else {
                verticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
            }
        }
        providersPanel.setLayout(providersPanelLayout);
        providersPanelLayout.setHorizontalGroup(
            providersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(providersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(horizontalGroup)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        providersPanelLayout.setVerticalGroup(
            providersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(verticalGroup)
        );
        // set initial message (if any)
        validateAndStore();
    }

    void validateAndStore() {
        validateData();
        storeData();
    }

    @NbBundle.Messages("CustomizerTesting.error.none=For running tests, at least one testing provider must be selected.")
    private void validateData() {
        assert EventQueue.isDispatchThread();
        if (selectedTestingProviders.isEmpty()) {
            category.setErrorMessage(Bundle.CustomizerTesting_error_none());
            category.setValid(true);
            return;
        }
        // everything ok
        category.setErrorMessage(" "); // NOI18N
        category.setValid(true);
    }

    private void storeData() {
        assert EventQueue.isDispatchThread();
        uiProps.setTestingProviders(new ArrayList<>(selectedTestingProviders));
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        providersLabel = new JLabel();
        providersPanel = new JPanel();

        Mnemonics.setLocalizedText(providersLabel, NbBundle.getMessage(CustomizerTesting.class, "CustomizerTesting.providersLabel.text")); // NOI18N

        GroupLayout providersPanelLayout = new GroupLayout(providersPanel);
        providersPanel.setLayout(providersPanelLayout);
        providersPanelLayout.setHorizontalGroup(
            providersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        providersPanelLayout.setVerticalGroup(
            providersPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 46, Short.MAX_VALUE)
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(providersLabel)
            .addComponent(providersPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(providersLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(providersPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel providersLabel;
    private JPanel providersPanel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class TestingProviderListener implements ItemListener {

        private final String testingProvider;


        public TestingProviderListener(String testingProvider) {
            this.testingProvider = testingProvider;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            assert EventQueue.isDispatchThread();
            if (e.getStateChange() == ItemEvent.SELECTED) {
                boolean added = selectedTestingProviders.add(testingProvider);
                assert added : "Provider " + testingProvider + " already present in " + selectedTestingProviders;
                TestingProviderPanel panel = testingPanels.get(testingProvider);
                if (panel != null) {
                    panel.showProviderPanel();
                }
            } else {
                boolean removed = selectedTestingProviders.remove(testingProvider);
                assert removed : "Provider " + testingProvider + " not present in " + selectedTestingProviders;
                TestingProviderPanel panel = testingPanels.get(testingProvider);
                if (panel != null) {
                    panel.hideProviderPanel();
                }
            }
            validateAndStore();
        }

    }

}
