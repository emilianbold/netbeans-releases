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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiLicense;
import org.netbeans.modules.kenai.ui.nodes.AddInstanceAction;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Milan Kubec
 */
public class NameAndLicenseWizardPanelGUI extends JPanel {
    
    private RequestProcessor errorChecker = new RequestProcessor("Error Checker"); // NOI18N

    private WizardDescriptor settings;

    private NameAndLicenseWizardPanel panel;
    private Pattern prjNamePattern;

    private static final String PRJ_NAME_REGEXP = "[a-z]{1}[a-z0-9-]+"; // NOI18N

    private static final String EMPTY_ELEMENT = "";

    private List<KenaiLicense> licensesList = null;

    private String prjNameCheckMessage = null;

    private boolean licensesLoaded = true;

    private PropertyChangeListener kenaiListener;

    public static final String getPreviewPrefix(Kenai kenai) {
        if (kenai==null) {
            return "";
        }
        return kenai.getUrl().toString() +  "/projects/"; //NOI18N
    } // NOI18N

    private String getPreviewPrefix() {
        return getPreviewPrefix(panel.getKenai());
    }

    public NameAndLicenseWizardPanelGUI(NameAndLicenseWizardPanel pnl) {

        panel = pnl;
        initComponents();

        kenaiCombo.setSelectedItem(panel.getKenai());
        kenaiCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                if (kenaiCombo.getSelectedItem() instanceof Kenai) {
                    panel.setKenai(((Kenai) kenaiCombo.getSelectedItem()));
                    refreshUsername();
                    updatePrjNamePreview();
                } else if (kenaiCombo.getSelectedItem() instanceof String) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            new AddInstanceAction().actionPerformed(e);
                            panel.setKenai(((Kenai) kenaiCombo.getSelectedItem()));
                            refreshUsername();
                            updatePrjNamePreview();
                        }
                    });
                } 
                setupLicensesListModel();
            }
        });


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
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                projectNameTextField.requestFocus();
            }
        });


        setupLicensesListModel();
        setPreferredSize(new Dimension(Math.max(700, getPreferredSize().width), 450));

        kenaiListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (Kenai.PROP_LOGIN.equals(evt.getPropertyName())) {
                    if (panel.getKenai().getPasswordAuthentication() != null) {
                        loginButton.setEnabled(false);
                    } else {
                        loginButton.setEnabled(true);
                    }
                }
            }
        };
        if (panel.getKenai()!=null) {
            panel.getKenai().addPropertyChangeListener(WeakListeners.propertyChange(kenaiListener, panel.getKenai()));
        }

    }

    private void setChildrenEnabled(Component root, boolean enabled) {
        root.setEnabled(enabled);
        if (root instanceof java.awt.Container) {
            for (Component c : ((java.awt.Container) root).getComponents()) {
                if (c != kenaiCombo) {
                    setChildrenEnabled(c, enabled);
                }
            }
        }
    }

    private void setupLicensesListModel() {
        if (panel.getKenai()==null)
            return;

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                Collection<KenaiLicense> licenses = null;
                try {
                        licenses = panel.getKenai().getLicenses();
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
                    licensesLoaded = true;
                } else {
                    // XXX this item needs to be selected
                    model.addElement(NbBundle.getMessage(NameAndLicenseWizardPanel.class,
                            "NameAndLicenseWizardPanelGUI.noLicensesError")); // NOI18N
                    licensesLoaded = false;
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
            kenaiURLPreviewLabel.setText(getPreviewPrefix() + prjName);
        } else {
            kenaiURLPreviewLabel.setText(getPreviewPrefix() + "..."); // NOI18N
        }
    }

    private boolean checkPrjName(String prjName) {
        Matcher matcher = prjNamePattern.matcher(prjName);
        return matcher.matches();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.panelName"); // NOI18N
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
        licenseDescription = new JLabel();
        kenaiCombo = new KenaiCombo(true);

        setLayout(new GridBagLayout());
        Mnemonics.setLocalizedText(loggedInLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.loggedInLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(loggedInLabel, gridBagConstraints);

        loggedInLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.loggedInLabel.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(usernameLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.notLoggedIn"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new Insets(0, 4, 5, 0);
        add(usernameLabel, gridBagConstraints);

        usernameLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.usernameLabel.AccessibleContext.accessibleName")); // NOI18N
        usernameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.usernameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(loginButton, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.loginButton.text"));
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loginButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        add(loginButton, gridBagConstraints);
        loginButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.loginButton.AccessibleContext.accessibleDescription")); // NOI18N

        projectNameLabel.setLabelFor(projectNameTextField);
        Mnemonics.setLocalizedText(projectNameLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectNameLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 4);
        add(projectNameLabel, gridBagConstraints);

        projectNameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectNameLabel.AccessibleContext.accessibleDescription")); // NOI18N
        projectNameTextField.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectNameTextField.text")); // NOI18N
        projectNameTextField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent evt) {
                projectNameTextFieldFocusLost(evt);
            }
        });
        projectNameTextField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                projectNameTextFieldKeyPressed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(projectNameTextField, gridBagConstraints);

        projectNameTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectNameTextField.AccessibleContext.accessibleName")); // NOI18N
        projectNameTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectNameTextField.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(kenaiURLPreviewLabel, getPreviewPrefix() + "...");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 6, 16, 0);
        add(kenaiURLPreviewLabel, gridBagConstraints);
        kenaiURLPreviewLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.kenaiURLPreviewLabel.AccessibleContext.accessibleDescription")); // NOI18N

        projectTitleLabel.setLabelFor(projectTitleTextField);
        Mnemonics.setLocalizedText(projectTitleLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectTitleLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 5);
        add(projectTitleLabel, gridBagConstraints);

        projectTitleLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectTitleLabel.AccessibleContext.accessibleDescription")); // NOI18N
        projectTitleTextField.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectTitleTextField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(projectTitleTextField, gridBagConstraints);
        projectTitleTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectTitleTextField.AccessibleContext.accessibleName")); // NOI18N
        projectTitleTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectTitleTextField.AccessibleContext.accessibleDescription")); // NOI18N

        projectDescLabel.setLabelFor(projectDescTextField);
        Mnemonics.setLocalizedText(projectDescLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectDescLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 4);
        add(projectDescLabel, gridBagConstraints);

        projectDescLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectDescLabel.AccessibleContext.accessibleDescription")); // NOI18N
        projectDescTextField.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectDescTextField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        add(projectDescTextField, gridBagConstraints);
        projectDescTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectDescTextField.AccessibleContext.accessibleName")); // NOI18N
        projectDescTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectDescTextField.AccessibleContext.accessibleDescription")); // NOI18N

        projectLicenseLabel.setLabelFor(projectLicenseComboBox);
        Mnemonics.setLocalizedText(projectLicenseLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectLicenseLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 4);
        add(projectLicenseLabel, gridBagConstraints);
        projectLicenseLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectLicenseLabel.AccessibleContext.accessibleDescription")); // NOI18N

        projectLicenseComboBox.setEnabled(false);
        projectLicenseComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                projectLicenseComboBoxActionPerformed(evt);
            }
        });
        projectLicenseComboBox.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
                projectLicenseComboBoxPopupMenuWillBecomeVisible(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        add(projectLicenseComboBox, gridBagConstraints);

        projectLicenseComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectLicenseComboBox.AccessibleContext.accessibleName")); // NOI18N
        projectLicenseComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.projectLicenseComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(multiLicensesLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.multiLicensesLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 6, 0, 0);
        add(multiLicensesLabel, gridBagConstraints);

        multiLicensesLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.multiLicensesLabel.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(proxyConfigButton, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.proxyConfigButton.text"));
        proxyConfigButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                proxyConfigButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(proxyConfigButton, gridBagConstraints);

        proxyConfigButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.proxyConfigButton.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(lowercaseLabel, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.lowercaseLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        add(lowercaseLabel, gridBagConstraints);

        lowercaseLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.lowercaseLabel.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(licenseDescription, NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.licenseDescription.text", new Object[]{}));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(14, 0, 0, 0);
        add(licenseDescription, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        add(kenaiCombo, gridBagConstraints);

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, "NameAndLicenseWizardPanelGUI.AccessibleContext.accessibleDescription")); // NOI18N
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

        if (panel.getKenai()==null)
            return;
        if (getProjectName().length()<2) {
            prjNameCheckMessage = NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.prjNameLengthErrMsg"); // NOI18N
            panel.fireChangeEvent();
            return;
        }
        prjNameCheckMessage = null;
        panel.fireChangeEvent();
        errorChecker.post(new Runnable() {
            public void run() {
                try {
                    prjNameCheckMessage = panel.getKenai().checkProjectName(getProjectName());
                } catch (KenaiException ex) {
                    String msg = ex.getAsString();
                    if (msg==null) {
                        msg = ex.getLocalizedMessage();
                    }
                    prjNameCheckMessage = msg;
                }
                panel.fireChangeEvent();
            }
        });

    }//GEN-LAST:event_projectNameTextFieldFocusLost

    private void projectNameTextFieldKeyPressed(KeyEvent evt) {//GEN-FIRST:event_projectNameTextFieldKeyPressed
        if (prjNameCheckMessage!=null) {
            prjNameCheckMessage = null;
            panel.fireChangeEvent();
        }
    }//GEN-LAST:event_projectNameTextFieldKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox kenaiCombo;
    private JLabel kenaiURLPreviewLabel;
    private JLabel licenseDescription;
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

    public void validateWizard() throws WizardValidationException {
    }

    // - not all errors are checked!
    private String checkForErrors() {
        String prjName = getProjectName();

        if (prjName.length()>20) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.prjNameLengthErrMsg"); // NOI18N
        } else if (prjName.length() > 2 && !checkPrjName(prjName)) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.invalidPrjName"); // NOI18N
        }  else if (prjNameCheckMessage!=null) {
            return prjNameCheckMessage;
        } else if (getProjectTitle().length() == 1 || getProjectTitle().length() > 40) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.prjTitleLengthErrMsg"); // NOI18N
        } else if (getProjectDesc().length() > 500) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.prjDescLengthErrMsg"); // NOI18N
        } else if (!licensesLoaded) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.noLicensesErrMsg"); // NOI18N
        }
        return null;
    }

    private String checkForWarnings() {
        // No warnings so far
        return null;
    }

    private String checkForInfos() {
        if (panel.getKenai()==null || panel.getKenai().getStatus() == Kenai.Status.OFFLINE) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.needLogin"); // NOI18N
        } else if (getProjectName().trim().equals("")) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.prjNameRequired"); // NOI18N
        } else if (getProjectTitle().trim().equals("")) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class, 
                    "NameAndLicenseWizardPanelGUI.prjTitleRequired"); // NOI18N
        } else if (getProjectDesc().trim().equals("")) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.prjDescRequired"); // NOI18N
        } else if (projectLicenseComboBox.getSelectedItem() == null ||
                   projectLicenseComboBox.getSelectedItem().equals(EMPTY_ELEMENT)) {
            return NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.prjLicenseRequired"); // NOI18N
        }
        return null;
    }

    public void read(WizardDescriptor settings) {
        this.settings = settings;
        String prjName = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_PRJ_NAME);
        List<NewKenaiProjectWizardIterator.SharedItem> items = panel.getInitialItems();
        if (prjName == null || "".equals(prjName.trim())) {
            if (items.size() == 1) {
                setProjectName(items.get(0).getRoot().getName().toLowerCase());
            } else {
                setProjectName(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                        "NameAndLicenseWizardPanelGUI.defaultPrjName")); // NOI18N
            }
        } else {
            setProjectName(prjName);
        }
        String prjTitle = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_PRJ_TITLE);
        if (prjTitle == null || "".equals(prjTitle.trim())) {
            if (items.size() == 1) {
                Project prj = FileOwnerQuery.getOwner(FileUtil.toFileObject(items.get(0).getRoot()));
                if (prj != null) {
                    setProjectTitle(ProjectUtils.getInformation(prj).getDisplayName());
                } else {
                    setProjectTitle(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                            "NameAndLicenseWizardPanelGUI.defaultPrjTitle")); // NOI18N
                }
            } else {
                setProjectTitle(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                        "NameAndLicenseWizardPanelGUI.defaultPrjTitle")); // NOI18N
            }
        } else {
            setProjectTitle(prjTitle);
        }

        String prjDesc = (String) this.settings.getProperty(NewKenaiProjectWizardIterator.PROP_PRJ_DESC);
        if (prjDesc == null || "".equals(prjDesc.trim())) {
            setProjectDescription(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.defaultPrjDesc")); // NOI18N
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
        setChildrenEnabled(this,panel.getKenai()!=null);
        PasswordAuthentication passwdAuth = panel.getKenai()==null?null:panel.getKenai().getPasswordAuthentication();
        if (passwdAuth != null) {
            setUsername(passwdAuth.getUserName());
            loginButton.setEnabled(false);
        } else {
            setUsername(null);
            loginButton.setEnabled(true);
        }
    }

    private void setUsername(String uName) {
        if (uName != null) {
            usernameLabel.setText(uName);
            usernameLabel.setForeground(new Color(0, 102, 0));
            usernameLabel.setEnabled(true);
        } else {
            usernameLabel.setText(NbBundle.getMessage(NameAndLicenseWizardPanelGUI.class,
                    "NameAndLicenseWizardPanelGUI.notLoggedIn")); // NOI18N
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
        return ""; // NOI18N
    }

}
