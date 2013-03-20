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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.analysis.commands.CodeSniffer;
import org.netbeans.modules.php.analysis.options.AnalysisOptionsValidator;
import org.netbeans.modules.php.analysis.ui.CodeSnifferStandardsComboBoxModel;
import org.netbeans.modules.php.analysis.util.AnalysisUtils;
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

    final CodeSnifferStandardsComboBoxModel codeSnifferStandardsModel = new CodeSnifferStandardsComboBoxModel();

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public AnalysisOptionsPanel(@NullAllowed String selectedCodeSnifferStandard) {
        initComponents();

        init(selectedCodeSnifferStandard);
    }

    @NbBundle.Messages({
        "# {0} - short script name",
        "# {1} - long script name",
        "AnalysisOptionsPanel.codeSniffer.hint=Full path of Code Sniffer script (typically {0} or {1}).",
    })
    private void init(String selectedCodeSnifferStandard) {
        errorLabel.setText(" "); // NOI18N
        codeSnifferHintLabel.setText(Bundle.AnalysisOptionsPanel_codeSniffer_hint(CodeSniffer.NAME, CodeSniffer.LONG_NAME));

        // listeners
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        codeSnifferTextField.getDocument().addDocumentListener(defaultDocumentListener);
        codeSnifferTextField.getDocument().addDocumentListener(new CodeSnifferStandardDocumentListener());
        ItemListener defaultItemListener = new DefaultItemListener();
        codeSnifferStandardComboBox.addItemListener(defaultItemListener);

        // standards
        setStandards(selectedCodeSnifferStandard);
    }

    @NbBundle.Messages("AnalysisOptionsPanel.codeSniffer.error.standards=Standards cannot be fetched, review Output window.")
    void setStandards(final String selectedCodeSnifferStandard) {
        AnalysisUtils.initCodeSnifferStandardsComponent(codeSnifferStandardComboBox, codeSnifferStandardsModel, selectedCodeSnifferStandard, new Runnable() {
            @Override
            public void run() {
                setError(Bundle.AnalysisOptionsPanel_codeSniffer_error_standards());
            }
        });
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
            // fetching standards or some error
            return null;
        }
        return codeSnifferStandardsModel.getSelectedStandard();
    }

    public void setCodeSnifferStandard(String standard) {
        if (!codeSnifferStandardComboBox.isEnabled()) {
            // fetching standards or some error
            return;
        }
        codeSnifferStandardsModel.setSelectedItem(standard);
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
        codeSnifferStandardComboBox = new JComboBox();
        noteLabel = new JLabel();
        codeSnifferLearnMoreLabel = new JLabel();
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

        Mnemonics.setLocalizedText(noteLabel, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.noteLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(codeSnifferLearnMoreLabel, NbBundle.getMessage(AnalysisOptionsPanel.class, "AnalysisOptionsPanel.codeSnifferLearnMoreLabel.text")); // NOI18N
        codeSnifferLearnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                codeSnifferLearnMoreLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                codeSnifferLearnMoreLabelMousePressed(evt);
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
                    .addComponent(codeSnifferStandardLabel))
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(codeSnifferTextField)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(codeSnifferBrowseButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(codeSnifferSearchButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(codeSnifferStandardComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(codeSnifferHintLabel))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {codeSnifferBrowseButton, codeSnifferSearchButton});

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
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(codeSnifferLearnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel)
                .addGap(0, 0, 0))
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton codeSnifferBrowseButton;
    private JLabel codeSnifferHintLabel;
    private JLabel codeSnifferLabel;
    private JLabel codeSnifferLearnMoreLabel;
    private JButton codeSnifferSearchButton;
    private JComboBox codeSnifferStandardComboBox;
    private JLabel codeSnifferStandardLabel;
    private JTextField codeSnifferTextField;
    private JLabel errorLabel;
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

    private final class CodeSnifferStandardDocumentListener implements DocumentListener {

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
            if (!codeSnifferStandardComboBox.isEnabled()) {
                // reading standards...
                return;
            }
            // reset cached standards only if the new path is valid
            ValidationResult result = new AnalysisOptionsValidator()
                    .validateCodeSnifferPath(getCodeSnifferPath())
                    .getResult();
            if (!result.hasErrors()
                    && !result.hasWarnings()) {
                CodeSniffer.clearCachedStandards();
                setStandards(getCodeSnifferStandard());
            }
        }

    }

    private final class DefaultItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            fireChange();
        }

    }

}
