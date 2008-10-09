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

/*
 * DebugProjectPanel.java
 *
 * Created on Oct 1, 2008, 5:01:54 PM
 */
package org.netbeans.modules.web.client.tools.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ButtonModel;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * Panel for the dialog that is displayed when the user selects the Debug action
 * in a project that supports JavaScript debugging
 *
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
class DebugProjectPanel extends JPanel {
    private static final String WEB_PROJECT = "org.netbeans.modules.web.project.WebProject"; // NOI18N
    private static final String RAILS_PROJECT = "org.netbeans.modules.ruby.railsprojects.RailsProject"; // NOI18N
    private static final String PHP_PROJECT = "org.netbeans.modules.php.project"; // NOI18N

    private String serverDebugMsg;
    private String serverDebugMsgWithMnemonic;
    private Project project;

    private boolean ffBrowserSupported;
    private boolean ieBrowserSupported;

    /** Creates new form DebugProjectPanel */
    public DebugProjectPanel(Project project) {
        this.project = project;
        initComponentMessages(project);
        initComponents();

        this.ffBrowserSupported = WebClientToolsProjectUtils.isFirefoxSupported();
        this.ieBrowserSupported = WebClientToolsProjectUtils.isInternetExplorerSupported();

        adjustBrowserRadioButtons(true);
        clientServerRadioButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                adjustBrowserRadioButtons(false);
            }
        });
    }

    private void initComponentMessages(Project project) {
        Project p = project.getLookup().lookup(Project.class);
        p = (p != null) ? p : project;

        // XXX There should be a better way of providing custom messages per project
        // type
        String key, keyMnem;
        String className = p.getClass().getName();
        if (className.startsWith(PHP_PROJECT)) {
            key = "DebugProjectPanel_PhpMessage"; // NOI18N
            keyMnem = "DebugProjectPanel_PhpMessageMnemonic"; // NOI18N
        } else if (className.startsWith(WEB_PROJECT)) {
            key = "DebugProjectPanel_WebMessage"; // NOI18N
            keyMnem = "DebugProjectPanel_WebMessageMnemonic"; // NOI18N
        } else if (className.startsWith(RAILS_PROJECT)) {
            key = "DebugProjectPanel_RailsMessage"; // NOI18N
            keyMnem = "DebugProjectPanel_RailsMessageMnemonic"; // NOI18N
        } else {
            key = "DebugProjectPanel_GenericMessage"; // NOI18N
            keyMnem = "DebugProjectPanel_GenericMessageMnemonic"; // NOI18N
        }
        
        this.serverDebugMsg = NbBundle.getMessage(DebugProjectPanel.class, key);
        this.serverDebugMsgWithMnemonic = NbBundle.getMessage(DebugProjectPanel.class, keyMnem);
    }

    private void adjustBrowserRadioButtons(boolean init) {
        if (init) {
            boolean isFirefox = WebClientToolsProjectUtils.isFirefox(project);
            ButtonModel bm = isFirefox ? firefoxRadioButton.getModel() : ieRadioButton.getModel();
            browserButtonGroup.setSelected(bm, true);

            if (!ffBrowserSupported && !ieBrowserSupported) {
                serverClientButtonGroup.setSelected(serverOnlyRadioButton.getModel(), true);
                clientServerRadioButton.setEnabled(false);
            } else {
                boolean clientSelected = WebClientToolsProjectUtils.getClientDebugProperty(project);
                ButtonModel model = clientSelected ? clientServerRadioButton.getModel() : serverOnlyRadioButton.getModel();
                serverClientButtonGroup.setSelected(model, true);
            }
        }

        firefoxRadioButton.setEnabled(clientServerRadioButton.isSelected() && ffBrowserSupported);
        ieRadioButton.setEnabled(clientServerRadioButton.isSelected() && ieBrowserSupported);
    }

    public ActionListener getPanelCloseHandler(final Object okButton) {
        return new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == okButton) {
                    boolean clientDebugEnabled = clientServerRadioButton.isSelected();
                    boolean showDialog = !doNotShowAgain.isSelected();
                    WebClientToolsProjectUtils.Browser browser = firefoxRadioButton.isSelected() ?
                        WebClientToolsProjectUtils.Browser.FIREFOX : WebClientToolsProjectUtils.Browser.INTERNET_EXPLORER;

                    WebClientToolsProjectUtils.setProjectProperties(project, true, clientDebugEnabled, browser);

                    Preferences projectPrefs = WebClientToolsProjectUtils.getPreferencesForProject(project);
                    projectPrefs.putBoolean(WebClientToolsProjectUtils.DIALOG_DISPLAY_CONFIG, showDialog);
                    
                    try {
                        projectPrefs.flush();
                    } catch (BackingStoreException ex) {
                        Log.getLogger().log(Level.WARNING, "Could not save preferences", ex);
                    }
                }
            }
        };
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serverClientButtonGroup = new javax.swing.ButtonGroup();
        browserButtonGroup = new javax.swing.ButtonGroup();
        debugProjectLabel = new javax.swing.JLabel();
        serverOnlyRadioButton = new javax.swing.JRadioButton();
        clientServerRadioButton = new javax.swing.JRadioButton();
        firefoxRadioButton = new javax.swing.JRadioButton();
        ieRadioButton = new javax.swing.JRadioButton();
        extensionInstallLabel = new javax.swing.JLabel();
        doNotShowAgain = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();

        debugProjectLabel.setText(org.openide.util.NbBundle.getMessage(DebugProjectPanel.class, "DebugProjectPanel.debugProjectLabel.text")); // NOI18N

        serverClientButtonGroup.add(serverOnlyRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(serverOnlyRadioButton, org.openide.util.NbBundle.getMessage(DebugProjectPanel.class, "DebugProjectPanel.serverOnlyRadioButton.text", new Object[] {this.serverDebugMsgWithMnemonic})); // NOI18N

        serverClientButtonGroup.add(clientServerRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(clientServerRadioButton, org.openide.util.NbBundle.getMessage(DebugProjectPanel.class, "DebugProjectPanel.clientServerRadioButton.text", new Object[] {this.serverDebugMsg})); // NOI18N

        browserButtonGroup.add(firefoxRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(firefoxRadioButton, org.openide.util.NbBundle.getMessage(DebugProjectPanel.class, "DebugProjectPanel.firefoxRadioButton.text")); // NOI18N

        browserButtonGroup.add(ieRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(ieRadioButton, org.openide.util.NbBundle.getMessage(DebugProjectPanel.class, "DebugProjectPanel.ieRadioButton.text")); // NOI18N

        extensionInstallLabel.setText(org.openide.util.NbBundle.getMessage(DebugProjectPanel.class, "DebugProjectPanel.extensionInstallLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(doNotShowAgain, org.openide.util.NbBundle.getMessage(DebugProjectPanel.class, "DebugProjectPanel.doNotShowAgain.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(DebugProjectPanel.class, "DebugProjectPanel.jLabel1.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(24, 24, 24)
                        .add(jLabel1))
                    .add(doNotShowAgain)
                    .add(debugProjectLabel)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(clientServerRadioButton)
                            .add(layout.createSequentialGroup()
                                .add(24, 24, 24)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(ieRadioButton)
                                    .add(firefoxRadioButton)
                                    .add(extensionInstallLabel)))
                            .add(serverOnlyRadioButton))))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(debugProjectLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serverOnlyRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(clientServerRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(firefoxRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ieRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(extensionInstallLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(doNotShowAgain)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        serverOnlyRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DebugProjectPanel.class, "DebugProjectPanel.serverOnlyRadioButton.AccessibleContext.accessibleDescription", new Object[] {})); // NOI18N
        clientServerRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DebugProjectPanel.class, "DebugProjectPanel.clientServerRadioButton.AccessibleContext.accessibleDescription", new Object[] {})); // NOI18N
        firefoxRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DebugProjectPanel.class, "DebugProjectPanel.firefoxRadioButton.AccessibleContext.accessibleDescription", new Object[] {})); // NOI18N
        ieRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DebugProjectPanel.class, "DebugProjectPanel.ieRadioButton.AccessibleContext.accessibleDescription", new Object[] {})); // NOI18N
        doNotShowAgain.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DebugProjectPanel.class, "DebugProjectPanel.doNotShowAgain.AccessibleContext.accessibleDescription", new Object[] {})); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup browserButtonGroup;
    private javax.swing.JRadioButton clientServerRadioButton;
    private javax.swing.JLabel debugProjectLabel;
    private javax.swing.JCheckBox doNotShowAgain;
    private javax.swing.JLabel extensionInstallLabel;
    private javax.swing.JRadioButton firefoxRadioButton;
    private javax.swing.JRadioButton ieRadioButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.ButtonGroup serverClientButtonGroup;
    private javax.swing.JRadioButton serverOnlyRadioButton;
    // End of variables declaration//GEN-END:variables

}
