/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * NameAndLicenseWizardPanelGUI.java
 *
 * Created on Feb 6, 2009, 11:15:27 AM
 */

package org.netbeans.modules.kenai.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiLicense;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * TODO:
 * - all text from bundles
 * - all possible error states and messages
 * - check project name against AJAX API
 * - fix regexp
 * - check for isUnique(prjName)
 *
 * @author Milan Kubec
 */
public class NameAndLicenseWizardPanelGUI extends JPanel {

    private WizardDescriptor settings;

    private NameAndLicenseWizardPanel panel;
    private Pattern prjNamePattern;

    private static final String PRJ_NAME_REGEXP = "[a-z]{1}[a-z0-9_-]+";
    private static final String PRJ_NAME_PREVIEW_PREFIX = "http://kenai.com/projects/";

    private static final String EMPTY_ELEMENT = "";

    private List<KenaiLicense> licensesList = null;

    private String prjNameCheckMessage = null;

    public NameAndLicenseWizardPanelGUI(NameAndLicenseWizardPanel pnl) {

        panel = pnl;
        initComponents();
        refreshUsername();

        prjNamePattern = Pattern.compile(PRJ_NAME_REGEXP);

        DocumentListener firingDocListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                panel.fireChangeEvent();
            }
            public void removeUpdate(DocumentEvent e) {
                panel.fireChangeEvent();
            }
            public void changedUpdate(DocumentEvent e) {
                panel.fireChangeEvent();
            }
        };
        
        DocumentListener updatingDocListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updatePrjNamePreview();
            }
            public void removeUpdate(DocumentEvent e) {
                updatePrjNamePreview();
            }
            public void changedUpdate(DocumentEvent e) {
                updatePrjNamePreview();
            }
        };

        projectNameTextField.getDocument().addDocumentListener(updatingDocListener);
        projectNameTextField.getDocument().addDocumentListener(firingDocListener);
        projectTitleTextField.getDocument().addDocumentListener(firingDocListener);
        projectDescTextField.getDocument().addDocumentListener(firingDocListener);

        setupLicensesListModel();

    }

    private void setupLicensesListModel() {

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                Collection<KenaiLicense> licenses = null;
                try {
                    licenses = Kenai.getDefault().getLicenses();
                } catch (KenaiException ex) {
                    // OK, list of licenses will be null
                    // XXX or show message that "Cannot connect to Kenai.com server" ???
                }
                final DefaultComboBoxModel model = new DefaultComboBoxModel();
                ArrayList<KenaiLicense> licenseList = new ArrayList<KenaiLicense>();
                if (licenses != null) {
                    model.addElement(EMPTY_ELEMENT);
                    Iterator<KenaiLicense> iter = licenses.iterator();
                    while (iter.hasNext()) {
                        KenaiLicense license = iter.next();
                        model.addElement(license.getDisplayName());
                        licenseList.add(license);
                    }
                } else {
                    // XXX this item needs to be selected
                    model.addElement(NbBundle.getMessage(NameAndLicenseWizardPanel.class,
                            "NameAndLicenseWizardPanelGUI.noLicensesError"));
                }
                if (!licenseList.isEmpty()) {
                    setLicenses(licenseList);
                }
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        projectLicenseComboBox.setModel(model);
                        projectLicenseComboBox.setSelectedItem(EMPTY_ELEMENT);
                        projectLicenseComboBox.setEnabled(true);
                    }
                });
            }
        });

    }

    // ----------

    private synchronized void setLicenses(List<KenaiLicense> list) {
        licensesList = list;
    }

    private synchronized List<KenaiLicense> getLicenses() {
        return licensesList;
    }

    // ----------

    private void updatePrjNamePreview() {
        String prjName = getProjectName();
        if (checkPrjName(prjName)) {
            kenaiURLPreviewLabel.setText(PRJ_NAME_PREVIEW_PREFIX + prjName);
        } else {
            kenaiURLPreviewLabel.setText(PRJ_NAME_PREVIEW_PREFIX + "..."); // NOI18N
        }
    }

    private boolean checkPrjName(String prjName) {
        Matcher matcher = prjNamePattern.matcher(prjName);
        return matcher.matches();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                "NameAndLicenseWizardPanelGUI.panelName");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        loggedInLabel = new JLabel();
        usernameLabel = new JLabel();
        loginButton = new JButton();
        projectNameLabel = new JLabel();
        projectNameTextField = new JTextField();
        kenaiURLPreviewLabel = new JLabel();
        projectTitleLabel = new JLabel();
        projectTitleTextField = new JTextField();
        projectDescLabel = new JLabel();
        projectDescTextField = new JTextField();
        projectLicenseLabel = new JLabel();
        projectLicenseComboBox = new JComboBox();
        multiLicensesLabel = new JLabel();
        proxyConfigButton = new JButton();
        lowercaseLabel = new JLabel();

        setPreferredSize(new Dimension(700, 450));
        setLayout(new GridBagLayout());

        loggedInLabel.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.loggedInLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 4, 4);
        add(loggedInLabel, gridBagConstraints);

        usernameLabel.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.notLoggedIn")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new Insets(0, 4, 4, 0);
        add(usernameLabel, gridBagConstraints);

        loginButton.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.loginButton.text")); // NOI18N
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(0, 0, 4, 0);
        add(loginButton, gridBagConstraints);

        projectNameLabel.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectNameLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        add(projectNameLabel, gridBagConstraints);

        projectNameTextField.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectNameTextField.text")); // NOI18N
        projectNameTextField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent evt) {
                projectNameTextFieldFocusLost(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(projectNameTextField, gridBagConstraints);

        kenaiURLPreviewLabel.setText(PRJ_NAME_PREVIEW_PREFIX + "...");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 6, 16, 0);
        add(kenaiURLPreviewLabel, gridBagConstraints);

        projectTitleLabel.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectTitleLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 5);
        add(projectTitleLabel, gridBagConstraints);

        projectTitleTextField.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectTitleTextField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(projectTitleTextField, gridBagConstraints);

        projectDescLabel.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectDescLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 4);
        add(projectDescLabel, gridBagConstraints);

        projectDescTextField.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectDescTextField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        add(projectDescTextField, gridBagConstraints);

        projectLicenseLabel.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectLicenseLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 4);
        add(projectLicenseLabel, gridBagConstraints);

        projectLicenseComboBox.setEnabled(false);
        projectLicenseComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                projectLicenseComboBoxActionPerformed(evt);
            }
        });
        projectLicenseComboBox.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
                projectLicenseComboBoxPopupMenuWillBecomeVisible(evt);
            }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
            }
            public void popupMenuCanceled(PopupMenuEvent evt) {
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        add(projectLicenseComboBox, gridBagConstraints);

        multiLicensesLabel.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.multiLicensesLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new Insets(0, 6, 0, 0);
        add(multiLicensesLabel, gridBagConstraints);

        proxyConfigButton.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.proxyConfigButton.text")); // NOI18N
        proxyConfigButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                proxyConfigButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
        add(proxyConfigButton, gridBagConstraints);

        lowercaseLabel.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.lowercaseLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(lowercaseLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void loginButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
        boolean loginSuccess = UIUtils.showLogin();
        if (loginSuccess) {
            panel.fireChangeEvent();
            refreshUsername();
        } else {
            // login failed, do nothing
        }
}//GEN-LAST:event_loginButtonActionPerformed

    private void proxyConfigButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_proxyConfigButtonActionPerformed
        OptionsDisplayer.getDefault().open("General"); // NOI18N
    }//GEN-LAST:event_proxyConfigButtonActionPerformed

    private void projectLicenseComboBoxPopupMenuWillBecomeVisible(PopupMenuEvent evt) {//GEN-FIRST:event_projectLicenseComboBoxPopupMenuWillBecomeVisible
        if (projectLicenseComboBox.getSelectedItem().equals(EMPTY_ELEMENT)) {
            projectLicenseComboBox.removeItem(EMPTY_ELEMENT);
        }
    }//GEN-LAST:event_projectLicenseComboBoxPopupMenuWillBecomeVisible

    private void projectLicenseComboBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_projectLicenseComboBoxActionPerformed
        panel.fireChangeEvent();
    }//GEN-LAST:event_projectLicenseComboBoxActionPerformed

    private void projectNameTextFieldFocusLost(FocusEvent evt) {//GEN-FIRST:event_projectNameTextFieldFocusLost

        // XXX 
        new Thread(new Runnable() {
            public void run() {
                String message = null;
                try {
                    message = KenaiProject.checkName(getProjectName());
                    System.out.println("checkName message: " + message);
                } catch (KenaiException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (message != null) {
                    prjNameCheckMessage = message;
                    panel.fireChangeEvent();
                }
            }
        });

    }//GEN-LAST:event_projectNameTextFieldFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel kenaiURLPreviewLabel;
    private JLabel loggedInLabel;
    private JButton loginButton;
    private JLabel lowercaseLabel;
    private JLabel multiLicensesLabel;
    private JLabel projectDescLabel;
    private JTextField projectDescTextField;
    private JComboBox projectLicenseComboBox;
    private JLabel projectLicenseLabel;
    private JLabel projectNameLabel;
    private JTextField projectNameTextField;
    private JLabel projectTitleLabel;
    private JTextField projectTitleTextField;
    private JButton proxyConfigButton;
    private JLabel usernameLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addNotify() {
        super.addNotify();
        panel.fireChangeEvent();
    }

    public boolean valid() {

        String message = checkForErrors();
        if (message != null) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
            return false;
        } else {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        }

        message = checkForWarnings();
        if (message != null) {
            settings.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, message);
            return false;
        } else {
            settings.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);
        }

        message = checkForInfos();
        if (message != null) {
            settings.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, message);
            return false;
        } else {
            settings.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        }

        return true;

    }

    // XXX
    public void validateWizard() throws WizardValidationException {
//        if (getProjectName().equals(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
//                    "NameAndLicenseWizardPanelGUI.defaultName"))) {
//            throw new WizardValidationException(this, "M - Please provide some other project name than default",
//                    "LM - Please provide some other project name than default");
//        }
    }

    // XXX All messages from bundle
    // - not all errors are checked!
    private String checkForErrors() {
        String prjName = getProjectName();
        if (prjName.length() > 0 && !checkPrjName(prjName)) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.invalidPrjName");
        } else if (/*getProjectTitle().length() < 2 ||*/ getProjectTitle().length() > 40) {
            return "Project Title length must be between 2 and 40 characters.";
        } else if (getProjectDesc().length() > 500) {
            return "Project Description must to be shorter than 500 characters.";
        }
        return null;
    }

    private String checkForWarnings() {
        // No warnings so far
        return null;
    }

    private String checkForInfos() {
        if (!Utilities.isUserLoggedIn()) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.needLogin");
        } else if (getProjectName().trim().equals("")) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.prjNameRequired");
        } else if (getProjectTitle().trim().equals("")) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, 
                    "NameAndLicenseWizardPanelGUI.prjTitleRequired");
        } else if (getProjectDesc().trim().equals("")) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.prjDescRequired");
        } else if (projectLicenseComboBox.getSelectedItem() == null ||
                   projectLicenseComboBox.getSelectedItem().equals(EMPTY_ELEMENT)) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.prjLicenseRequired");
        }
        return null;
    }

    public void read(WizardDescriptor settings) {
        this.settings = settings;
        String prjName = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_PRJ_NAME);
        if (prjName == null || "".equals(prjName.trim())) {
            setProjectName(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.defaultPrjName"));
        } else {
            setProjectName(prjName);
        }
        String prjTitle = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_PRJ_TITLE);
        if (prjTitle == null || "".equals(prjTitle.trim())) {
            setProjectTitle(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.defaultPrjTitle"));
        } else {
            setProjectTitle(prjTitle);
        }
        String prjDesc = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_PRJ_DESC);
        if (prjDesc == null || "".equals(prjDesc.trim())) {
            setProjectDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.defaultPrjDesc"));
        } else {
            setProjectDescription(prjDesc);
        }
        String prjLicense = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_PRJ_LICENSE);
        setProjectLicense(prjLicense);
    }

    public void store(WizardDescriptor settings) {
        settings.putProperty(NewKenaiProjectWizardIterator.PROP_PRJ_NAME, getProjectName());
        settings.putProperty(NewKenaiProjectWizardIterator.PROP_PRJ_TITLE, getProjectTitle());
        settings.putProperty(NewKenaiProjectWizardIterator.PROP_PRJ_DESC, getProjectDesc());
        settings.putProperty(NewKenaiProjectWizardIterator.PROP_PRJ_LICENSE, getProjectLicense());
    }

    // ----------

    private void refreshUsername() {
        PasswordAuthentication passwdAuth = Kenai.getDefault().getPasswordAuthentication();
        if (passwdAuth != null) {
            setUsername(passwdAuth.getUserName());
        } else {
            setUsername(null);
        }
    }

    private void setUsername(String uName) {
        if (uName != null) {
            usernameLabel.setText(uName);
            usernameLabel.setForeground(Color.BLUE);
            usernameLabel.setEnabled(true);
        } else {
            usernameLabel.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.notLoggedIn"));
            usernameLabel.setForeground(Color.BLACK);
            usernameLabel.setEnabled(false);
        }
    }

    // ----------

    private void setProjectName(String prjName) {
        projectNameTextField.setText(prjName);
    }
    
    private String getProjectName() {
        return projectNameTextField.getText();
    }

    private void setProjectTitle(String prjTitle) {
        projectTitleTextField.setText(prjTitle);
    }

    private String getProjectTitle() {
        return projectTitleTextField.getText();
    }

    private void setProjectDescription(String prjDesc) {
        projectDescTextField.setText(prjDesc);
    }

    private String getProjectDesc() {
        return projectDescTextField.getText();
    }

    private void setProjectLicense(String licenseName) {
        if (licenseName == null) {
            return;
        }
        List<KenaiLicense> list = getLicenses();
        if (list != null) {
            Iterator<KenaiLicense> iter = list.iterator();
            while (iter.hasNext()) {
                KenaiLicense lic = iter.next();
                if(licenseName.equals(lic.getName())) {
                    projectLicenseComboBox.setSelectedItem(lic.getDisplayName());
                }
            }

        }
    }

    private String getProjectLicense() {
        int index = projectLicenseComboBox.getSelectedIndex();
        List<KenaiLicense> list = getLicenses();
        if (list != null) {
            return getLicenses().get(index).getName();
        }
        return ""; // XXX or null ???
    }

}
