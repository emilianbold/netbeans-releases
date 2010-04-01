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

package org.netbeans.modules.php.project.ui.options;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.classpath.GlobalIncludePathSupport;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.netbeans.modules.php.project.ui.LastUsedFolders;
import org.netbeans.modules.php.project.ui.PathUiSupport;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author  Tomas Mysik
 */
public class PhpOptionsPanel extends JPanel {
    private static final long serialVersionUID = 10985641247986428L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final WatchesAndEvalListener watchesAndEvalListener = new WatchesAndEvalListener();

    public PhpOptionsPanel() {
        initComponents();
        errorLabel.setText(" "); // NOI18N

        initPhpGlobalIncludePath();

        // listeners
        DocumentListener documentListener = new DefaultDocumentListener();
        phpInterpreterTextField.getDocument().addDocumentListener(documentListener);
        debuggerPortTextField.getDocument().addDocumentListener(documentListener);
        debuggerSessionIdTextField.getDocument().addDocumentListener(documentListener);
        watchesAndEvalCheckBox.addItemListener(watchesAndEvalListener);
    }

    private void initPhpGlobalIncludePath() {
        DefaultListModel listModel = PathUiSupport.createListModel(
                GlobalIncludePathSupport.getInstance().itemsIterator());
        PathUiSupport.EditMediator.FileChooserDirectoryHandler directoryHandler = new PathUiSupport.EditMediator.FileChooserDirectoryHandler() {
            @Override
            public File getCurrentDirectory() {
                return LastUsedFolders.getIncludePath();
            }
            @Override
            public void setCurrentDirectory(File currentDirectory) {
                LastUsedFolders.setIncludePath(currentDirectory);
            }
        };

        includePathList.setModel(listModel);
        includePathList.setCellRenderer(new PathUiSupport.ClassPathListCellRenderer());
        PathUiSupport.EditMediator.register(includePathList,
                                               addFolderButton.getModel(),
                                               removeButton.getModel(),
                                               moveUpButton.getModel(),
                                               moveDownButton.getModel(),
                                               directoryHandler);
    }

    public String getPhpInterpreter() {
        return phpInterpreterTextField.getText();
    }

    public void setPhpInterpreter(String phpInterpreter) {
        phpInterpreterTextField.setText(phpInterpreter);
    }

    public boolean isOpenResultInOutputWindow() {
        return outputWindowCheckBox.isSelected();
    }

    public void setOpenResultInOutputWindow(boolean openResultInOutputWindow) {
        outputWindowCheckBox.setSelected(openResultInOutputWindow);
    }

    public boolean isOpenResultInBrowser() {
        return webBrowserCheckBox.isSelected();
    }

    public void setOpenResultInBrowser(boolean openResultInBrowser) {
        webBrowserCheckBox.setSelected(openResultInBrowser);
    }

    public boolean isOpenResultInEditor() {
        return editorCheckBox.isSelected();
    }

    public void setOpenResultInEditor(boolean openResultInEditor) {
        editorCheckBox.setSelected(openResultInEditor);
    }

    public Integer getDebuggerPort() {
        Integer port = null;
        try {
            port = Integer.parseInt(debuggerPortTextField.getText());
        } catch (NumberFormatException exc) {
            // ignored
        }
        return port;
    }

    public String getDebuggerSessionId() {
        return debuggerSessionIdTextField.getText();
    }

    public void setDebuggerSessionId(String sessionId) {
        debuggerSessionIdTextField.setText(sessionId);
    }

    public void setDebuggerPort(int debuggerPort) {
        debuggerPortTextField.setText(String.valueOf(debuggerPort));
    }

    public boolean isDebuggerStoppedAtTheFirstLine() {
        return stopAtTheFirstLineCheckBox.isSelected();
    }

    public void setDebuggerStoppedAtTheFirstLine(boolean debuggerStoppedAtTheFirstLine) {
        stopAtTheFirstLineCheckBox.setSelected(debuggerStoppedAtTheFirstLine);
    }

    public boolean isDebuggerWatchesAndEval() {
        return watchesAndEvalCheckBox.isSelected();
    }

    public void setDebuggerWatchesAndEval(boolean debuggerWatchesAndEval) {
        watchesAndEvalCheckBox.removeItemListener(watchesAndEvalListener);
        watchesAndEvalCheckBox.setSelected(debuggerWatchesAndEval);
        watchesAndEvalCheckBox.addItemListener(watchesAndEvalListener);
    }

    public String getPhpGlobalIncludePath() {
        String[] paths = GlobalIncludePathSupport.getInstance().encodeToStrings(
                PathUiSupport.getIterator((DefaultListModel) includePathList.getModel()));
        StringBuilder path = new StringBuilder(200);
        for (String s : paths) {
            path.append(s);
        }
        return path.toString();
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

        commandLineSeparator = new JSeparator();
        commandLineLabel = new JLabel();
        phpInterpreterLabel = new JLabel();
        phpInterpreterTextField = new JTextField();
        phpInterpreterBrowseButton = new JButton();
        phpInterpreterSearchButton = new JButton();
        openResultInLabel = new JLabel();
        outputWindowCheckBox = new JCheckBox();
        webBrowserCheckBox = new JCheckBox();
        editorCheckBox = new JCheckBox();
        debuggingSeparator = new JSeparator();
        debuggingLabel = new JLabel();
        debuggerPortLabel = new JLabel();
        debuggerPortTextField = new JTextField();
        debuggerSessionIdLabel = new JLabel();
        debuggerSessionIdTextField = new JTextField();
        stopAtTheFirstLineCheckBox = new JCheckBox();
        watchesAndEvalCheckBox = new JCheckBox();
        globalIncludePathSeparator = new JSeparator();
        globalIncludePathLabel = new JLabel();
        useTheFollowingPathByDefaultLabel = new JLabel();
        includePathScrollPane = new JScrollPane();
        includePathList = new JList();
        addFolderButton = new JButton();
        removeButton = new JButton();
        moveUpButton = new JButton();
        moveDownButton = new JButton();
        errorLabel = new JLabel();

        setFocusTraversalPolicy(null);

        commandLineLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(commandLineLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_CommandLine")); // NOI18N

        phpInterpreterLabel.setLabelFor(phpInterpreterTextField);




        Mnemonics.setLocalizedText(phpInterpreterLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_PhpInterpreter")); // NOI18N
        Mnemonics.setLocalizedText(phpInterpreterBrowseButton, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Browse"));
        phpInterpreterBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpInterpreterBrowseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(phpInterpreterSearchButton, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Search"));
        phpInterpreterSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpInterpreterSearchButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(openResultInLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_OpenResultIn"));
        Mnemonics.setLocalizedText(outputWindowCheckBox, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_OutputWindow"));
        Mnemonics.setLocalizedText(webBrowserCheckBox, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_WebBrowser"));
        Mnemonics.setLocalizedText(editorCheckBox, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Editor"));

        debuggingLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(debuggingLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Debugging")); // NOI18N

        debuggerPortLabel.setLabelFor(debuggerPortTextField);
        Mnemonics.setLocalizedText(debuggerPortLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_DebuggerPort")); // NOI18N

        debuggerSessionIdLabel.setLabelFor(debuggerSessionIdTextField);


        Mnemonics.setLocalizedText(debuggerSessionIdLabel, NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggerSessionIdLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(stopAtTheFirstLineCheckBox, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_StopAtTheFirstLine"));
        Mnemonics.setLocalizedText(watchesAndEvalCheckBox, NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.watchesAndEvalCheckBox.text"));

        globalIncludePathLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(globalIncludePathLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_GlobalIncludePath")); // NOI18N

        useTheFollowingPathByDefaultLabel.setLabelFor(includePathList);
        Mnemonics.setLocalizedText(useTheFollowingPathByDefaultLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_UseTheFollowingPathByDefault")); // NOI18N

        includePathScrollPane.setViewportView(includePathList);




        includePathList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.includePathList.AccessibleContext.accessibleName")); // NOI18N
        includePathList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.includePathList.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(addFolderButton, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_AddFolder")); // NOI18N
        Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Remove"));
        Mnemonics.setLocalizedText(moveUpButton, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_MoveUp"));
        Mnemonics.setLocalizedText(moveDownButton, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_MoveDown"));

        errorLabel.setLabelFor(this);
        Mnemonics.setLocalizedText(errorLabel, "ERROR");

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(phpInterpreterLabel)
                    .addComponent(openResultInLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(phpInterpreterTextField, GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(phpInterpreterBrowseButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(phpInterpreterSearchButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(outputWindowCheckBox)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(webBrowserCheckBox)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(editorCheckBox))))
            .addGroup(layout.createSequentialGroup()
                .addComponent(commandLineLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(commandLineSeparator, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(debuggingLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(debuggingSeparator, GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(stopAtTheFirstLineCheckBox)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(watchesAndEvalCheckBox))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(debuggerPortLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(debuggerPortTextField, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(debuggerSessionIdLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(debuggerSessionIdTextField, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE))))
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(includePathScrollPane, GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                            .addComponent(addFolderButton)
                            .addComponent(removeButton)
                            .addComponent(moveUpButton)
                            .addComponent(moveDownButton)))
                    .addComponent(useTheFollowingPathByDefaultLabel)))
            .addComponent(errorLabel)
            .addGroup(layout.createSequentialGroup()
                .addComponent(globalIncludePathLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(globalIncludePathSeparator, GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {addFolderButton, moveDownButton, moveUpButton, removeButton});

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {phpInterpreterBrowseButton, phpInterpreterSearchButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(commandLineLabel)
                    .addComponent(commandLineSeparator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(phpInterpreterBrowseButton)
                    .addComponent(phpInterpreterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(phpInterpreterSearchButton)
                    .addComponent(phpInterpreterLabel))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(outputWindowCheckBox)
                    .addComponent(webBrowserCheckBox)
                    .addComponent(editorCheckBox)
                    .addComponent(openResultInLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(debuggingLabel)
                    .addComponent(debuggingSeparator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(debuggerPortLabel)
                    .addComponent(debuggerPortTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(debuggerSessionIdLabel)
                    .addComponent(debuggerSessionIdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(stopAtTheFirstLineCheckBox)
                    .addComponent(watchesAndEvalCheckBox))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(globalIncludePathLabel)
                    .addComponent(globalIncludePathSeparator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(useTheFollowingPathByDefaultLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(includePathScrollPane, GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addFolderButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(moveUpButton)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(moveDownButton)))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(errorLabel))
        );

        commandLineSeparator.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.commandLineSeparator.AccessibleContext.accessibleName_1")); // NOI18N
        commandLineSeparator.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.commandLineSeparator.AccessibleContext.accessibleDescription_1")); // NOI18N
        commandLineLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.commandLineLabel.AccessibleContext.accessibleName")); // NOI18N
        commandLineLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.commandLineLabel.AccessibleContext.accessibleDescription")); // NOI18N
        phpInterpreterLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpInterpreterLabel.AccessibleContext.accessibleName")); // NOI18N
        phpInterpreterLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpInterpreterLabel.AccessibleContext.accessibleDescription")); // NOI18N
        phpInterpreterTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpInterpreterTextField.AccessibleContext.accessibleName")); // NOI18N
        phpInterpreterTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpInterpreterTextField.AccessibleContext.accessibleDescription")); // NOI18N
        phpInterpreterBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.browseButton.AccessibleContext.accessibleName")); // NOI18N
        phpInterpreterBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.browseButton.AccessibleContext.accessibleDescription")); // NOI18N
        phpInterpreterSearchButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.searchButton.AccessibleContext.accessibleName")); // NOI18N
        phpInterpreterSearchButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.searchButton.AccessibleContext.accessibleDescription")); // NOI18N
        openResultInLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.openResultInLabel.AccessibleContext.accessibleName")); // NOI18N
        openResultInLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.openResultInLabel.AccessibleContext.accessibleDescription")); // NOI18N
        outputWindowCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.outputWindowCheckBox.AccessibleContext.accessibleName")); // NOI18N
        outputWindowCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.outputWindowCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        webBrowserCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.webBrowserCheckBox.AccessibleContext.accessibleName")); // NOI18N
        webBrowserCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.webBrowserCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        editorCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.editorCheckBox.AccessibleContext.accessibleName")); // NOI18N
        editorCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.editorCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        debuggingSeparator.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggingSeparator.AccessibleContext.accessibleName_1")); // NOI18N
        debuggingSeparator.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggingSeparator.AccessibleContext.accessibleDescription_1")); // NOI18N
        debuggingLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggingLabel.AccessibleContext.accessibleName")); // NOI18N
        debuggingLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggingLabel.AccessibleContext.accessibleDescription")); // NOI18N
        debuggerPortLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggerPortLabel.AccessibleContext.accessibleName")); // NOI18N
        debuggerPortLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggerPortLabel.AccessibleContext.accessibleDescription")); // NOI18N
        debuggerPortTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggerPortTextField.AccessibleContext.accessibleName")); // NOI18N
        debuggerPortTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggerPortTextField.AccessibleContext.accessibleDescription")); // NOI18N
        debuggerSessionIdLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggerSessionIdLabel.AccessibleContext.accessibleName")); // NOI18N
        debuggerSessionIdLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggerSessionIdLabel.AccessibleContext.accessibleDescription")); // NOI18N
        debuggerSessionIdTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggerSessionIdTextField.AccessibleContext.accessibleName")); // NOI18N
        debuggerSessionIdTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggerSessionIdTextField.AccessibleContext.accessibleDescription")); // NOI18N
        stopAtTheFirstLineCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.stopAtTheFirstLineCheckBox.AccessibleContext.accessibleName")); // NOI18N
        stopAtTheFirstLineCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.stopAtTheFirstLineCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        globalIncludePathSeparator.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.globalIncludePathSeparator.AccessibleContext.accessibleName_1")); // NOI18N
        globalIncludePathSeparator.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.globalIncludePathSeparator.AccessibleContext.accessibleDescription_1")); // NOI18N
        globalIncludePathLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.globalIncludePathLabel.AccessibleContext.accessibleName")); // NOI18N
        globalIncludePathLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.globalIncludePathLabel.AccessibleContext.accessibleDescription")); // NOI18N
        useTheFollowingPathByDefaultLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.useTheFollowingPathByDefaultLabel.AccessibleContext.accessibleName")); // NOI18N
        useTheFollowingPathByDefaultLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.useTheFollowingPathByDefaultLabel.AccessibleContext.accessibleDescription")); // NOI18N
        includePathScrollPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.includePathScrollPane.AccessibleContext.accessibleName")); // NOI18N
        includePathScrollPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.includePathScrollPane.AccessibleContext.accessibleDescription")); // NOI18N
        addFolderButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.addFolderButton.AccessibleContext.accessibleName")); // NOI18N
        addFolderButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.addFolderButton.AccessibleContext.accessibleDescription")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.removeButton.AccessibleContext.accessibleName")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.removeButton.AccessibleContext.accessibleDescription")); // NOI18N
        moveUpButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.moveUpButton.AccessibleContext.accessibleName")); // NOI18N
        moveUpButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.moveUpButton.AccessibleContext.accessibleDescription")); // NOI18N
        moveDownButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.moveDownButton.AccessibleContext.accessibleName")); // NOI18N
        moveDownButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.moveDownButton.AccessibleContext.accessibleDescription")); // NOI18N
        errorLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.errorLabel.AccessibleContext.accessibleName")); // NOI18N
        errorLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.errorLabel.AccessibleContext.accessibleDescription")); // NOI18N
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void phpInterpreterBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phpInterpreterBrowseButtonActionPerformed
        Utils.browsePhpInterpreter(this, phpInterpreterTextField);
    }//GEN-LAST:event_phpInterpreterBrowseButtonActionPerformed

    private void phpInterpreterSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phpInterpreterSearchButtonActionPerformed
        String phpInterpreter = UiUtils.SearchWindow.search(new UiUtils.SearchWindow.SearchWindowSupport() {
            @Override
            public List<String> detect() {
                return PhpEnvironment.get().getAllPhpInterpreters();
            }

            @Override
            public String getWindowTitle() {
                return NbBundle.getMessage(PhpOptionsPanel.class, "LBL_PhpInterpretersTitle");
            }

            @Override
            public String getListTitle() {
                return NbBundle.getMessage(PhpOptionsPanel.class, "LBL_PhpInterpreters");
            }

            @Override
            public String getPleaseWaitPart() {
                return NbBundle.getMessage(PhpOptionsPanel.class, "LBL_PhpInterpretersPleaseWaitPart");
            }

            @Override
            public String getNoItemsFound() {
                return NbBundle.getMessage(PhpOptionsPanel.class, "LBL_NoPhpInterpretersFound");
            }
        });
        if (phpInterpreter != null) {
            phpInterpreterTextField.setText(phpInterpreter);
        }
    }//GEN-LAST:event_phpInterpreterSearchButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton addFolderButton;
    private JLabel commandLineLabel;
    private JSeparator commandLineSeparator;
    private JLabel debuggerPortLabel;
    private JTextField debuggerPortTextField;
    private JLabel debuggerSessionIdLabel;
    private JTextField debuggerSessionIdTextField;
    private JLabel debuggingLabel;
    private JSeparator debuggingSeparator;
    private JCheckBox editorCheckBox;
    private JLabel errorLabel;
    private JLabel globalIncludePathLabel;
    private JSeparator globalIncludePathSeparator;
    private JList includePathList;
    private JScrollPane includePathScrollPane;
    private JButton moveDownButton;
    private JButton moveUpButton;
    private JLabel openResultInLabel;
    private JCheckBox outputWindowCheckBox;
    private JButton phpInterpreterBrowseButton;
    private JLabel phpInterpreterLabel;
    private JButton phpInterpreterSearchButton;
    private JTextField phpInterpreterTextField;
    private JButton removeButton;
    private JCheckBox stopAtTheFirstLineCheckBox;
    private JLabel useTheFollowingPathByDefaultLabel;
    private JCheckBox watchesAndEvalCheckBox;
    private JCheckBox webBrowserCheckBox;
    // End of variables declaration//GEN-END:variables

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

    private static final class WatchesAndEvalListener implements ItemListener {
        private static boolean warningShown = false;
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (warningShown) {
                return;
            }
            if (e.getStateChange() == ItemEvent.SELECTED) {
                NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                        NbBundle.getMessage(PhpOptionsPanel.class, "MSG_WatchesAndEval"),
                        NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(descriptor);
                warningShown = true;
            }
        }
    }
}
