/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.analysis.ui.options;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.analysis.commands.CodeSniffer;
import org.netbeans.modules.php.analysis.commands.MessDetector;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.ui.CodeSnifferStandardsComboBoxModel;
import org.netbeans.modules.php.analysis.ui.MessDetectorRuleSetsListModel;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

@NbBundle.Messages("AnalysisOptionsPanel.keywords.analysis=analysis")
@OptionsPanelController.Keywords(keywords={"php", "code", "analysis", "code analysis", "#AnalysisOptionsPanel.keywords.analysis"},
        location=UiUtils.OPTIONS_PATH, tabTitle= "#AnalysisOptionsPanel.name")
public class AnalysisOptionsPanel extends JPanel {

    private static final long serialVersionUID = -895132465784564654L;

    private static final String CODE_SNIFFER_LAST_FOLDER_SUFFIX = ".codeSniffer"; // NOI18N
    private static final String MESS_DETECTOR_LAST_FOLDER_SUFFIX = ".messDetector"; // NOI18N

    final CodeSnifferStandardsComboBoxModel codeSnifferStandardsModel = new CodeSnifferStandardsComboBoxModel();

    private final MessDetectorRuleSetsListModel ruleSetsListModel = new MessDetectorRuleSetsListModel();
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public AnalysisOptionsPanel() {
        initComponents();

        init();
    }

    private void init() {
        errorLabel.setText(" "); // NOI18N
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        initCodeSniffer(defaultDocumentListener);
        initMessDetector(defaultDocumentListener);
    }

    @NbBundle.Messages({
        "# {0} - short script name",
        "# {1} - long script name",
        "AnalysisOptionsPanel.codeSniffer.hint=Full path of Code Sniffer script (typically {0} or {1}).",
    })
    private void initCodeSniffer(DocumentListener defaultDocumentListener) {
        codeSnifferHintLabel.setText(Bundle.AnalysisOptionsPanel_codeSniffer_hint(CodeSniffer.NAME, CodeSniffer.LONG_NAME));
        codeSnifferStandardComboBox.setModel(codeSnifferStandardsModel);

        // listeners
        codeSnifferTextField.getDocument().addDocumentListener(defaultDocumentListener);
        codeSnifferTextField.getDocument().addDocumentListener(new CodeSnifferPathDocumentListener());
        ItemListener defaultItemListener = new DefaultItemListener();
        codeSnifferStandardComboBox.addItemListener(defaultItemListener);
    }

    @NbBundle.Messages({
        "# {0} - short script name",
        "# {1} - long script name",
        "AnalysisOptionsPanel.messDetector.hint=Full path of Mess Detector script (typically {0} or {1}).",
    })
    private void initMessDetector(DocumentListener defaultDocumentListener) {
        messDetectorHintLabel.setText(Bundle.AnalysisOptionsPanel_messDetector_hint(MessDetector.NAME, MessDetector.LONG_NAME));

        // listeners
        messDetectorTextField.getDocument().addDocumentListener(defaultDocumentListener);
        messDetectorRuleSetsList.addListSelectionListener(new DefaultListSelectionListener());

        // rulesets
        messDetectorRuleSetsList.setModel(ruleSetsListModel);
    }

    void setStandards(final String selectedCodeSnifferStandard, String customCodeSnifferPath) {
        codeSnifferStandardsModel.fetchStandards(codeSnifferStandardComboBox, customCodeSnifferPath);
        codeSnifferStandardsModel.setSelectedItem(selectedCodeSnifferStandard);
    }

    public String getCodeSnifferPath() {
        return codeSnifferTextField.getText();
    }

    public void setCodeSnifferPath(String path) {
        codeSnifferTextField.setText(path);
    }

    @CheckForNull
    public String getCodeSnifferStandard() {
        if (!codeSnifferStandardComboBox.isEnabled()) {
            // fetching standards
            return null;
        }
        return codeSnifferStandardsModel.getSelectedStandard();
    }

    public void setCodeSnifferStandard(String standard) {
        codeSnifferStandardsModel.setSelectedItem(standard);
    }

    public String getMessDetectorPath() {
        return messDetectorTextField.getText();
    }

    public void setMessDetectorPath(String path) {
        messDetectorTextField.setText(path);
    }

    public List<String> getMessDetectorRuleSets() {
        return getSelectedRuleSets();
    }

    public void setMessDetectorRuleSets(List<String> ruleSets) {
        selectRuleSets(ruleSets);
    }

    public void setError(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.errorForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void setWarning(String message) {
        errorLabel.setText(" "); // NOI18N
        errorLabel.setForeground(UIManager.getColor("nb.warningForeground")); // NOI18N
        errorLabel.setText(message);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    List<String> getSelectedRuleSets() {
        return messDetectorRuleSetsList.getSelectedValuesList();
    }

    void selectRuleSets(List<String> ruleSets) {
        messDetectorRuleSetsList.clearSelection();
        for (String ruleSet : ruleSets) {
            int indexOf = MessDetectorRuleSetsListModel.getAllRuleSets().indexOf(ruleSet);
            assert indexOf != -1 : "Rule set not found: " + ruleSet;
            messDetectorRuleSetsList.addSelectionInterval(indexOf, indexOf);
        }
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        codeSnifferLabel = new JLabel();
        codeSnifferTextField = new JTextField();
        codeSnifferBrowseButton = new JButton();
        codeSnifferSearchButton = new JButton();
        codeSnifferHintLabel = new JLabel();
        codeSnifferStandardLabel = new JLabel();
        codeSnifferStandardComboBox = new JComboBox<String>();
        messDetectorLabel = new JLabel();
        messDetectorTextField = new JTextField();
        messDetectorBrowseButton = new JButton();
        messDetectorSearchButton = new JButton();
        messDetectorHintLabel = new JLabel();
        messDetectorRuleSetsLabel = new JLabel();
        messDetectorRuleSetsScrollPane = new JScrollPane();
        messDetectorRuleSetsList = new JList<String>();
        noteLabel = new JLabel();
        minVersionInfoLabel = new JLabel();
        codeSnifferLearnMoreLabel = new JLabel();
        messDetectorLearnMoreLabel = new JLabel();
        errorLabel = new JLabel();

        codeSnifferLabel.setLabelFor(codeSnifferTextField);
        Mnemonics.setLocalizedText(codeSnifferLabel, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.codeSnifferLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(codeSnifferBrowseButton, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.codeSnifferBrowseButton.text")); // NOI18N
        codeSnifferBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                codeSnifferBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(codeSnifferSearchButton, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.codeSnifferSearchButton.text")); // NOI18N
        codeSnifferSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                codeSnifferSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(codeSnifferHintLabel, "HINT"); // NOI18N

        codeSnifferStandardLabel.setLabelFor(codeSnifferStandardComboBox);
        Mnemonics.setLocalizedText(codeSnifferStandardLabel, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.codeSnifferStandardLabel.text")); // NOI18N

        messDetectorLabel.setLabelFor(messDetectorTextField);
        Mnemonics.setLocalizedText(messDetectorLabel, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.messDetectorLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(messDetectorBrowseButton, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.messDetectorBrowseButton.text")); // NOI18N
        messDetectorBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                messDetectorBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(messDetectorSearchButton, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.messDetectorSearchButton.text")); // NOI18N
        messDetectorSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                messDetectorSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(messDetectorHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(messDetectorRuleSetsLabel, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.messDetectorRuleSetsLabel.text")); // NOI18N

        messDetectorRuleSetsScrollPane.setViewportView(messDetectorRuleSetsList);

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.noteLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(minVersionInfoLabel, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.minVersionInfoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(codeSnifferLearnMoreLabel, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.codeSnifferLearnMoreLabel.text")); // NOI18N
        codeSnifferLearnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                codeSnifferLearnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                codeSnifferLearnMoreLabelMousePressed(evt);
            }
        });

        Mnemonics.setLocalizedText(messDetectorLearnMoreLabel, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.messDetectorLearnMoreLabel.text")); // NOI18N
        messDetectorLearnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                messDetectorLearnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                messDetectorLearnMoreLabelMousePressed(evt);
            }
        });

        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(errorLabel)
                    .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(codeSnifferLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(codeSnifferLabel)
                    .addComponent(codeSnifferStandardLabel)
                    .addComponent(messDetectorLabel)
                    .addComponent(messDetectorRuleSetsLabel))
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(codeSnifferTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(codeSnifferBrowseButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(codeSnifferSearchButton))
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(messDetectorRuleSetsScrollPane, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(messDetectorTextField))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(messDetectorBrowseButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(messDetectorSearchButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(messDetectorHintLabel)
                            .addComponent(codeSnifferHintLabel)
                            .addComponent(codeSnifferStandardComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(messDetectorLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(minVersionInfoLabel))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {codeSnifferBrowseButton, codeSnifferSearchButton, messDetectorBrowseButton, messDetectorSearchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(codeSnifferLabel)
                    .addComponent(codeSnifferTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(codeSnifferSearchButton)
                    .addComponent(codeSnifferBrowseButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(codeSnifferHintLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(codeSnifferStandardComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(codeSnifferStandardLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(messDetectorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(messDetectorSearchButton)
                    .addComponent(messDetectorBrowseButton)
                    .addComponent(messDetectorLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(messDetectorHintLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(messDetectorRuleSetsScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(messDetectorRuleSetsLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(minVersionInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(codeSnifferLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(messDetectorLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("AnalysisOptionsPanel.codeSniffer.browse.title=Select Code Sniffer")
    private void codeSnifferBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_codeSnifferBrowseButtonActionPerformed
        File file = new FileChooserBuilder(AnalysisOptionsPanel.class.getName() + CODE_SNIFFER_LAST_FOLDER_SUFFIX)
                .setFilesOnly(true)
                .setTitle(Bundle.AnalysisOptionsPanel_codeSniffer_browse_title())
                .showOpenDialog();
        if (file != null) {
            codeSnifferTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_codeSnifferBrowseButtonActionPerformed

    @NbBundle.Messages({
        "AnalysisOptionsPanel.codeSniffer.search.title=Code Sniffer scripts",
        "AnalysisOptionsPanel.codeSniffer.search.scripts=Co&de Sniffer scripts:",
        "AnalysisOptionsPanel.codeSniffer.search.pleaseWaitPart=Code Sniffer scripts",
        "AnalysisOptionsPanel.codeSniffer.search.notFound=No Code Sniffer scripts found."
    })
    private void codeSnifferSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_codeSnifferSearchButtonActionPerformed
        String codeSniffer = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {

            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(CodeSniffer.NAME, CodeSniffer.LONG_NAME);
            }

            @Override
            public String getWindowTitle() {
                return Bundle.AnalysisOptionsPanel_codeSniffer_search_title();
            }

            @Override
            public String getListTitle() {
                return Bundle.AnalysisOptionsPanel_codeSniffer_search_scripts();
            }

            @Override
            public String getPleaseWaitPart() {
                return Bundle.AnalysisOptionsPanel_codeSniffer_search_pleaseWaitPart();
            }

            @Override
            public String getNoItemsFound() {
                return Bundle.AnalysisOptionsPanel_codeSniffer_search_notFound();
            }
        });
        if (codeSniffer != null) {
            codeSnifferTextField.setText(codeSniffer);
        }
    }//GEN-LAST:event_codeSnifferSearchButtonActionPerformed

    private void codeSnifferLearnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_codeSnifferLearnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_codeSnifferLearnMoreLabelMouseEntered

    private void codeSnifferLearnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_codeSnifferLearnMoreLabelMousePressed
        try {
            URL url = new URL("http://pear.php.net/package/PHP_CodeSniffer"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_codeSnifferLearnMoreLabelMousePressed

    private void messDetectorLearnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_messDetectorLearnMoreLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_messDetectorLearnMoreLabelMouseEntered

    private void messDetectorLearnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_messDetectorLearnMoreLabelMousePressed
        try {
            URL url = new URL("http://phpmd.org/"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_messDetectorLearnMoreLabelMousePressed

    @NbBundle.Messages("AnalysisOptionsPanel.messDetector.browse.title=Select Mess Detector")
    private void messDetectorBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_messDetectorBrowseButtonActionPerformed
        File file = new FileChooserBuilder(AnalysisOptionsPanel.class.getName() + MESS_DETECTOR_LAST_FOLDER_SUFFIX)
        .setFilesOnly(true)
        .setTitle(Bundle.AnalysisOptionsPanel_messDetector_browse_title())
        .showOpenDialog();
        if (file != null) {
            messDetectorTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_messDetectorBrowseButtonActionPerformed

    @NbBundle.Messages({
        "AnalysisOptionsPanel.messDetector.search.title=Mess Detector scripts",
        "AnalysisOptionsPanel.messDetector.search.scripts=M&ess Detector scripts:",
        "AnalysisOptionsPanel.messDetector.search.pleaseWaitPart=Mess Detector scripts",
        "AnalysisOptionsPanel.messDetector.search.notFound=No Mess Detector scripts found."
    })
    private void messDetectorSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_messDetectorSearchButtonActionPerformed
        String messDetector = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {

            @Override
            public List<String> detect() {
                return FileUtils.findFileOnUsersPath(MessDetector.NAME, MessDetector.LONG_NAME);
            }

            @Override
            public String getWindowTitle() {
                return Bundle.AnalysisOptionsPanel_messDetector_search_title();
            }

            @Override
            public String getListTitle() {
                return Bundle.AnalysisOptionsPanel_messDetector_search_scripts();
            }

            @Override
            public String getPleaseWaitPart() {
                return Bundle.AnalysisOptionsPanel_messDetector_search_pleaseWaitPart();
            }

            @Override
            public String getNoItemsFound() {
                return Bundle.AnalysisOptionsPanel_messDetector_search_notFound();
            }
        });
        if (messDetector != null) {
            messDetectorTextField.setText(messDetector);
        }
    }//GEN-LAST:event_messDetectorSearchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton codeSnifferBrowseButton;
    private JLabel codeSnifferHintLabel;
    private JLabel codeSnifferLabel;
    private JLabel codeSnifferLearnMoreLabel;
    private JButton codeSnifferSearchButton;
    private JComboBox<String> codeSnifferStandardComboBox;
    private JLabel codeSnifferStandardLabel;
    private JTextField codeSnifferTextField;
    private JLabel errorLabel;
    private JButton messDetectorBrowseButton;
    private JLabel messDetectorHintLabel;
    private JLabel messDetectorLabel;
    private JLabel messDetectorLearnMoreLabel;
    private JLabel messDetectorRuleSetsLabel;
    private JList<String> messDetectorRuleSetsList;
    private JScrollPane messDetectorRuleSetsScrollPane;
    private JButton messDetectorSearchButton;
    private JTextField messDetectorTextField;
    private JLabel minVersionInfoLabel;
    private JLabel noteLabel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            fireChange();
        }

    }

    private final class CodeSnifferPathDocumentListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            String codeSnifferPath = getCodeSnifferPath();
            // reset cached standards only if the new path is valid
            ValidationResult result = new AnalysisOptionsValidator()
                    .validateCodeSnifferPath(codeSnifferPath)
                    .getResult();
            if (!result.hasErrors()
                    && !result.hasWarnings()) {
                CodeSniffer.clearCachedStandards();
                setStandards(getCodeSnifferStandard(), codeSnifferPath);
            }
        }

    }

    private final class DefaultItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            fireChange();
        }

    }

    private final class DefaultListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            fireChange();
        }

    }

}
