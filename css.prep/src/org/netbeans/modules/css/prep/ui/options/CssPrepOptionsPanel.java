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
package org.netbeans.modules.css.prep.ui.options;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.css.prep.less.LessExecutable;
import org.netbeans.modules.css.prep.sass.SassExecutable;
import org.netbeans.modules.css.prep.util.FileUtils;
import org.netbeans.modules.css.prep.util.UiUtils;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

@NbBundle.Messages("CssPrepOptionsPanel.keywords.preprocessing=preprocessing")
@OptionsPanelController.Keywords(keywords={"css", "preprocessors", "sass", "less", "#CssPrepOptionsPanel.keywords.preprocessing"},
        location=UiUtils.OPTIONS_PATH, tabTitle= "#CssPrepOptionsPanel.name")
public final class CssPrepOptionsPanel extends JPanel {

    private static final long serialVersionUID = 268356546654654L;

    private static final Logger LOGGER = Logger.getLogger(CssPrepOptionsPanel.class.getName());

    private static final String SASS_LAST_FOLDER_SUFFIX = ".sass"; // NOI18N
    private static final String LESS_LAST_FOLDER_SUFFIX = ".less"; // NOI18N

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public CssPrepOptionsPanel() {
        initComponents();
        init();
    }

    private void init() {
        errorLabel.setText(" "); // NOI18N
        DocumentListener defaultDocumentListener = new DefaultDocumentListener();
        initSass(defaultDocumentListener);
        initLess(defaultDocumentListener);
    }

    @NbBundle.Messages({
        "# {0} - short script name",
        "CssPrepOptionsPanel.sass.path.hint=Full path of Sass executable (typically {0}).",
    })
    private void initSass(DocumentListener defaultDocumentListener) {
        sassPathHintLabel.setText(Bundle.CssPrepOptionsPanel_sass_path_hint(SassExecutable.EXECUTABLE_NAME));

        // listeners
        sassPathTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    @NbBundle.Messages({
        "# {0} - short script name",
        "CssPrepOptionsPanel.less.path.hint=Full path of LESS executable (typically {0}).",
    })
    private void initLess(DocumentListener defaultDocumentListener) {
        lessPathHintLabel.setText(Bundle.CssPrepOptionsPanel_less_path_hint(LessExecutable.EXECUTABLE_NAME));

        // listeners
        lessPathTextField.getDocument().addDocumentListener(defaultDocumentListener);
    }

    public String getSassPath() {
        return sassPathTextField.getText();
    }

    public void setSassPath(String path) {
        sassPathTextField.setText(path);
    }

    public String getLessPath() {
        return lessPathTextField.getText();
    }

    public void setLessPath(String path) {
        lessPathTextField.setText(path);
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

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sassPathLabel = new JLabel();
        sassPathTextField = new JTextField();
        sassPathBrowseButton = new JButton();
        sassPathSearchButton = new JButton();
        sassPathHintLabel = new JLabel();
        installSassLabel = new JLabel();
        lessPathLabel = new JLabel();
        lessPathTextField = new JTextField();
        lessPathBrowseButton = new JButton();
        lessPathSearchButton = new JButton();
        lessPathHintLabel = new JLabel();
        installLessLabel = new JLabel();
        errorLabel = new JLabel();

        sassPathLabel.setLabelFor(sassPathTextField);
        Mnemonics.setLocalizedText(sassPathLabel, NbBundle.getMessage(CssPrepOptionsPanel.class, "CssPrepOptionsPanel.sassPathLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(sassPathBrowseButton, NbBundle.getMessage(CssPrepOptionsPanel.class, "CssPrepOptionsPanel.sassPathBrowseButton.text")); // NOI18N
        sassPathBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sassPathBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(sassPathSearchButton, NbBundle.getMessage(CssPrepOptionsPanel.class, "CssPrepOptionsPanel.sassPathSearchButton.text")); // NOI18N
        sassPathSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sassPathSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(sassPathHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(installSassLabel, NbBundle.getMessage(CssPrepOptionsPanel.class, "CssPrepOptionsPanel.installSassLabel.text")); // NOI18N
        installSassLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                installSassLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                installSassLabelMousePressed(evt);
            }
        });

        lessPathLabel.setLabelFor(lessPathTextField);
        Mnemonics.setLocalizedText(lessPathLabel, NbBundle.getMessage(CssPrepOptionsPanel.class, "CssPrepOptionsPanel.lessPathLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(lessPathBrowseButton, NbBundle.getMessage(CssPrepOptionsPanel.class, "CssPrepOptionsPanel.lessPathBrowseButton.text")); // NOI18N
        lessPathBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                lessPathBrowseButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(lessPathSearchButton, NbBundle.getMessage(CssPrepOptionsPanel.class, "CssPrepOptionsPanel.lessPathSearchButton.text")); // NOI18N
        lessPathSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                lessPathSearchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(lessPathHintLabel, "HINT"); // NOI18N

        Mnemonics.setLocalizedText(installLessLabel, NbBundle.getMessage(CssPrepOptionsPanel.class, "CssPrepOptionsPanel.installLessLabel.text")); // NOI18N
        installLessLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                installLessLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                installLessLabelMousePressed(evt);
            }
        });

        Mnemonics.setLocalizedText(errorLabel, "ERROR"); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(errorLabel)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(sassPathLabel)
                    .addComponent(lessPathLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lessPathHintLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(installLessLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sassPathHintLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(installSassLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sassPathTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sassPathBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sassPathSearchButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lessPathTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lessPathBrowseButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lessPathSearchButton))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {sassPathBrowseButton, sassPathSearchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sassPathLabel)
                    .addComponent(sassPathBrowseButton)
                    .addComponent(sassPathSearchButton)
                    .addComponent(sassPathTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sassPathHintLabel)
                    .addComponent(installSassLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lessPathTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lessPathLabel))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lessPathSearchButton)
                        .addComponent(lessPathBrowseButton)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(installLessLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lessPathHintLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    @NbBundle.Messages("CssPrepOptionsPanel.sass.browse.title=Select Sass")
    private void sassPathBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sassPathBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CssPrepOptionsPanel.class.getName() + SASS_LAST_FOLDER_SUFFIX)
                .setFilesOnly(true)
                .setTitle(Bundle.CssPrepOptionsPanel_sass_browse_title())
                .showOpenDialog();
        if (file != null) {
            sassPathTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_sassPathBrowseButtonActionPerformed

    private void sassPathSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_sassPathSearchButtonActionPerformed
        List<String> sassPaths = FileUtils.findFileOnUsersPath(SassExecutable.EXECUTABLE_NAME);
        if (!sassPaths.isEmpty()) {
            sassPathTextField.setText(sassPaths.get(0));
        }
    }//GEN-LAST:event_sassPathSearchButtonActionPerformed

    private void installSassLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_installSassLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_installSassLabelMouseEntered

    private void installSassLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_installSassLabelMousePressed
        try {
            URL url = new URL("http://sass-lang.com/"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_installSassLabelMousePressed

    @NbBundle.Messages("CssPrepOptionsPanel.less.browse.title=Select LESS")
    private void lessPathBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_lessPathBrowseButtonActionPerformed
        File file = new FileChooserBuilder(CssPrepOptionsPanel.class.getName() + LESS_LAST_FOLDER_SUFFIX)
                .setFilesOnly(true)
                .setTitle(Bundle.CssPrepOptionsPanel_less_browse_title())
                .showOpenDialog();
        if (file != null) {
            lessPathTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_lessPathBrowseButtonActionPerformed

    private void lessPathSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_lessPathSearchButtonActionPerformed
        List<String> lessPaths = FileUtils.findFileOnUsersPath(LessExecutable.EXECUTABLE_NAME);
        if (!lessPaths.isEmpty()) {
            lessPathTextField.setText(lessPaths.get(0));
        }
    }//GEN-LAST:event_lessPathSearchButtonActionPerformed

    private void installLessLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_installLessLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_installLessLabelMouseEntered

    private void installLessLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_installLessLabelMousePressed
        try {
            URL url = new URL("http://lesscss.org/"); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_installLessLabelMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel errorLabel;
    private JLabel installLessLabel;
    private JLabel installSassLabel;
    private JButton lessPathBrowseButton;
    private JLabel lessPathHintLabel;
    private JLabel lessPathLabel;
    private JButton lessPathSearchButton;
    private JTextField lessPathTextField;
    private JButton sassPathBrowseButton;
    private JLabel sassPathHintLabel;
    private JLabel sassPathLabel;
    private JButton sassPathSearchButton;
    private JTextField sassPathTextField;
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

}
