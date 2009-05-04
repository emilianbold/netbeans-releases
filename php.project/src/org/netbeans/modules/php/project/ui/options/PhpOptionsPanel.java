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
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.modules.php.project.classpath.GlobalIncludePathSupport;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.netbeans.modules.php.project.ui.IncludePathUiSupport;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * @author  Tomas Mysik
 */
public class PhpOptionsPanel extends JPanel {
    private static final long serialVersionUID = 1092136352492432078L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public PhpOptionsPanel() {
        initComponents();
        errorLabel.setText(" "); // NOI18N

        initPhpGlobalIncludePath();

        // listeners
        DocumentListener documentListener = new DefaultDocumentListener();
        phpInterpreterTextField.getDocument().addDocumentListener(documentListener);
        debuggerPortTextField.getDocument().addDocumentListener(documentListener);
        debuggerSessionIdTextField.getDocument().addDocumentListener(documentListener);
        phpUnitTextField.getDocument().addDocumentListener(documentListener);
    }

    private void initPhpGlobalIncludePath() {
        DefaultListModel listModel = IncludePathUiSupport.createListModel(
                GlobalIncludePathSupport.getInstance().itemsIterator());
        includePathList.setModel(listModel);
        includePathList.setCellRenderer(new IncludePathUiSupport.ClassPathListCellRenderer());
        IncludePathUiSupport.EditMediator.register(includePathList,
                                               addFolderButton.getModel(),
                                               removeButton.getModel(),
                                               moveUpButton.getModel(),
                                               moveDownButton.getModel());
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

    public String getPhpUnit() {
        return phpUnitTextField.getText();
    }

    public void setPhpUnit(String phpUnit) {
        phpUnitTextField.setText(phpUnit);
    }

    public String getPhpGlobalIncludePath() {
        String[] paths = GlobalIncludePathSupport.getInstance().encodeToStrings(
                IncludePathUiSupport.getIterator((DefaultListModel) includePathList.getModel()));
        StringBuilder path = new StringBuilder();
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


        commandLineLabel = new JLabel();
        commandLineSeparator = new JSeparator();
        phpInterpreterLabel = new JLabel();
        phpInterpreterTextField = new JTextField();
        phpInterpreterBrowseButton = new JButton();
        phpInterpreterSearchButton = new JButton();
        openResultInLabel = new JLabel();
        outputWindowCheckBox = new JCheckBox();
        webBrowserCheckBox = new JCheckBox();
        editorCheckBox = new JCheckBox();
        debuggingLabel = new JLabel();
        debuggingSeparator = new JSeparator();
        debuggerPortLabel = new JLabel();
        debuggerPortTextField = new JTextField();
        debuggerSessionIdLabel = new JLabel();
        debuggerSessionIdTextField = new JTextField();
        stopAtTheFirstLineCheckBox = new JCheckBox();
        unitTestingLabel = new JLabel();
        phpUnitSeparator = new JSeparator();
        phpUnitLabel = new JLabel();
        phpUnitTextField = new JTextField();
        phpUnitBrowseButton = new JButton();
        phpUnitSearchButton = new JButton();
        globalIncludePathLabel = new JLabel();
        globalIncludePathSeparator = new JSeparator();
        useTheFollowingPathByDefaultLabel = new JLabel();
        includePathScrollPane = new JScrollPane();
        includePathList = new JList();
        addFolderButton = new JButton();
        removeButton = new JButton();
        moveUpButton = new JButton();
        moveDownButton = new JButton();
        errorLabel = new JLabel();

        setFocusTraversalPolicy(new FocusTraversalPolicy() {



            public Component getDefaultComponent(Container focusCycleRoot){
                return phpUnitBrowseButton;
            }//end getDefaultComponent
            public Component getFirstComponent(Container focusCycleRoot){
                return phpUnitBrowseButton;
            }//end getFirstComponent
            public Component getLastComponent(Container focusCycleRoot){
                return moveDownButton;
            }//end getLastComponent
            public Component getComponentAfter(Container focusCycleRoot, Component aComponent){
                if(aComponent ==  phpUnitBrowseButton){
                    return phpUnitSearchButton;
                }
                if(aComponent ==  phpUnitSearchButton){
                    return includePathList;
                }
                if(aComponent ==  phpInterpreterSearchButton){
                    return outputWindowCheckBox;
                }
                if(aComponent ==  moveUpButton){
                    return moveDownButton;
                }
                if(aComponent ==  phpInterpreterBrowseButton){
                    return phpInterpreterSearchButton;
                }
                if(aComponent ==  removeButton){
                    return moveUpButton;
                }
                if(aComponent ==  phpInterpreterTextField){
                    return phpInterpreterBrowseButton;
                }
                if(aComponent ==  addFolderButton){
                    return removeButton;
                }
                if(aComponent ==  editorCheckBox){
                    return debuggerPortTextField;
                }
                if(aComponent ==  includePathList){
                    return addFolderButton;
                }
                if(aComponent ==  webBrowserCheckBox){
                    return editorCheckBox;
                }
                if(aComponent ==  outputWindowCheckBox){
                    return webBrowserCheckBox;
                }
                if(aComponent ==  debuggerPortTextField){
                    return debuggerSessionIdTextField;
                }
                if(aComponent ==  phpUnitTextField){
                    return phpUnitBrowseButton;
                }
                if(aComponent ==  debuggerSessionIdTextField){
                    return stopAtTheFirstLineCheckBox;
                }
                if(aComponent ==  stopAtTheFirstLineCheckBox){
                    return phpUnitTextField;
                }
                return phpUnitBrowseButton;//end getComponentAfter
            }
            public Component getComponentBefore(Container focusCycleRoot, Component aComponent){
                if(aComponent ==  phpUnitSearchButton){
                    return phpUnitBrowseButton;
                }
                if(aComponent ==  includePathList){
                    return phpUnitSearchButton;
                }
                if(aComponent ==  outputWindowCheckBox){
                    return phpInterpreterSearchButton;
                }
                if(aComponent ==  moveDownButton){
                    return moveUpButton;
                }
                if(aComponent ==  phpInterpreterSearchButton){
                    return phpInterpreterBrowseButton;
                }
                if(aComponent ==  moveUpButton){
                    return removeButton;
                }
                if(aComponent ==  phpInterpreterBrowseButton){
                    return phpInterpreterTextField;
                }
                if(aComponent ==  removeButton){
                    return addFolderButton;
                }
                if(aComponent ==  debuggerPortTextField){
                    return editorCheckBox;
                }
                if(aComponent ==  addFolderButton){
                    return includePathList;
                }
                if(aComponent ==  editorCheckBox){
                    return webBrowserCheckBox;
                }
                if(aComponent ==  webBrowserCheckBox){
                    return outputWindowCheckBox;
                }
                if(aComponent ==  debuggerSessionIdTextField){
                    return debuggerPortTextField;
                }
                if(aComponent ==  phpUnitBrowseButton){
                    return phpUnitTextField;
                }
                if(aComponent ==  stopAtTheFirstLineCheckBox){
                    return debuggerSessionIdTextField;
                }
                if(aComponent ==  phpUnitTextField){
                    return stopAtTheFirstLineCheckBox;
                }
                return moveDownButton;//end getComponentBefore

            }}
        );

        Mnemonics.setLocalizedText(commandLineLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_CommandLine")); // NOI18N
        phpInterpreterLabel.setLabelFor(phpInterpreterTextField);

        Mnemonics.setLocalizedText(phpInterpreterLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_PhpInterpreter"));
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

        openResultInLabel.setLabelFor(outputWindowCheckBox);





        Mnemonics.setLocalizedText(openResultInLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_OpenResultIn")); // NOI18N
        Mnemonics.setLocalizedText(outputWindowCheckBox, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_OutputWindow"));
        Mnemonics.setLocalizedText(webBrowserCheckBox, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_WebBrowser"));
        Mnemonics.setLocalizedText(editorCheckBox, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Editor"));
        Mnemonics.setLocalizedText(debuggingLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Debugging"));
        debuggerPortLabel.setLabelFor(debuggerPortTextField);

        Mnemonics.setLocalizedText(debuggerPortLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_DebuggerPort")); // NOI18N
        debuggerSessionIdLabel.setLabelFor(debuggerSessionIdTextField);



        Mnemonics.setLocalizedText(debuggerSessionIdLabel, NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggerSessionIdLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(stopAtTheFirstLineCheckBox, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_StopAtTheFirstLine"));
        Mnemonics.setLocalizedText(unitTestingLabel, NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.unitTestingLabel.text"));
        phpUnitLabel.setLabelFor(phpUnitTextField);



        Mnemonics.setLocalizedText(phpUnitLabel, NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpUnitLabel.text")); // NOI18N
        Mnemonics.setLocalizedText(phpUnitBrowseButton, NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpUnitBrowseButton.text"));
        phpUnitBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpUnitBrowseButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(phpUnitSearchButton, NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpUnitSearchButton.text"));
        phpUnitSearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                phpUnitSearchButtonActionPerformed(evt);
            }
        });
        Mnemonics.setLocalizedText(globalIncludePathLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_GlobalIncludePath"));
        useTheFollowingPathByDefaultLabel.setLabelFor(includePathList);

        Mnemonics.setLocalizedText(useTheFollowingPathByDefaultLabel, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_UseTheFollowingPathByDefault")); // NOI18N
        includePathScrollPane.setViewportView(includePathList);





        includePathList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.includePathList.AccessibleContext.accessibleName")); // NOI18N
        includePathList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.includePathList.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(addFolderButton, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_AddFolder"));
        Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_Remove"));
        Mnemonics.setLocalizedText(moveUpButton, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_MoveUp"));
        Mnemonics.setLocalizedText(moveDownButton, NbBundle.getMessage(PhpOptionsPanel.class, "LBL_MoveDown"));
        Mnemonics.setLocalizedText(errorLabel, "ERROR");

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(phpUnitLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(phpUnitTextField, GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(phpUnitBrowseButton)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(phpUnitSearchButton))
                    .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(phpInterpreterLabel)
                            .add(openResultInLabel))
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(phpInterpreterTextField, GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(phpInterpreterBrowseButton)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(phpInterpreterSearchButton))
                            .add(layout.createSequentialGroup()
                                .add(outputWindowCheckBox)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(webBrowserCheckBox)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(editorCheckBox))))
                    .add(layout.createSequentialGroup()
                        .add(commandLineLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(commandLineSeparator, GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(debuggingLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(debuggingSeparator, GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(stopAtTheFirstLineCheckBox)
                            .add(layout.createSequentialGroup()
                                .add(debuggerPortLabel)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(debuggerPortTextField, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(debuggerSessionIdLabel)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(debuggerSessionIdTextField, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE))))
                    .add(layout.createSequentialGroup()
                        .add(globalIncludePathLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(globalIncludePathSeparator, GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(GroupLayout.LEADING)
                            .add(GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(includePathScrollPane, GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                                    .add(addFolderButton)
                                    .add(removeButton)
                                    .add(moveUpButton)
                                    .add(moveDownButton)))
                            .add(useTheFollowingPathByDefaultLabel)))
                    .add(errorLabel)
                    .add(layout.createSequentialGroup()
                        .add(unitTestingLabel)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(phpUnitSeparator, GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(new Component[] {addFolderButton, moveDownButton, moveUpButton, removeButton}, GroupLayout.HORIZONTAL);

        layout.linkSize(new Component[] {phpInterpreterBrowseButton, phpInterpreterSearchButton}, GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(commandLineLabel)
                    .add(commandLineSeparator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(phpInterpreterBrowseButton)
                    .add(phpInterpreterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(phpInterpreterSearchButton)
                    .add(phpInterpreterLabel))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(outputWindowCheckBox)
                    .add(webBrowserCheckBox)
                    .add(editorCheckBox)
                    .add(openResultInLabel))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(debuggingLabel)
                    .add(debuggingSeparator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(debuggerPortLabel)
                    .add(debuggerPortTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(debuggerSessionIdLabel)
                    .add(debuggerSessionIdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(stopAtTheFirstLineCheckBox)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(unitTestingLabel)
                    .add(phpUnitSeparator, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.BASELINE)
                    .add(phpUnitLabel)
                    .add(phpUnitTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(phpUnitSearchButton)
                    .add(phpUnitBrowseButton))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(GroupLayout.TRAILING)
                    .add(globalIncludePathLabel)
                    .add(globalIncludePathSeparator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(useTheFollowingPathByDefaultLabel)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(layout.createParallelGroup(GroupLayout.LEADING)
                    .add(includePathScrollPane, GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(addFolderButton)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(removeButton)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(moveUpButton)
                        .addPreferredGap(LayoutStyle.RELATED)
                        .add(moveDownButton)))
                .addPreferredGap(LayoutStyle.RELATED)
                .add(errorLabel)
                .addContainerGap())
        );

        commandLineLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.commandLineLabel.AccessibleContext.accessibleName")); // NOI18N
        commandLineLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.commandLineLabel.AccessibleContext.accessibleDescription")); // NOI18N
        commandLineSeparator.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.commandLineSeparator.AccessibleContext.accessibleName")); // NOI18N
        commandLineSeparator.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.commandLineSeparator.AccessibleContext.accessibleDescription")); // NOI18N
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
        debuggingLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggingLabel.AccessibleContext.accessibleName")); // NOI18N
        debuggingLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggingLabel.AccessibleContext.accessibleDescription")); // NOI18N
        debuggingSeparator.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggingSeparator.AccessibleContext.accessibleName")); // NOI18N
        debuggingSeparator.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.debuggingSeparator.AccessibleContext.accessibleDescription")); // NOI18N
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
        unitTestingLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.unitTestingLabel.AccessibleContext.accessibleDescription")); // NOI18N
        phpUnitSeparator.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpUnitSeparator.AccessibleContext.accessibleName")); // NOI18N
        phpUnitSeparator.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpUnitSeparator.AccessibleContext.accessibleDescription")); // NOI18N
        phpUnitLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpUnitLabel.AccessibleContext.accessibleName")); // NOI18N
        phpUnitLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpUnitLabel.AccessibleContext.accessibleDescription")); // NOI18N
        phpUnitTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpUnitTextField.AccessibleContext.accessibleName")); // NOI18N
        phpUnitTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpUnitTextField.AccessibleContext.accessibleDescription")); // NOI18N
        phpUnitBrowseButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpUnitBrowseButton.AccessibleContext.accessibleName")); // NOI18N
        phpUnitBrowseButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpUnitBrowseButton.AccessibleContext.accessibleDescription")); // NOI18N
        phpUnitSearchButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpUnitSearchButton.AccessibleContext.accessibleName")); // NOI18N
        phpUnitSearchButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.phpUnitSearchButton.AccessibleContext.accessibleDescription")); // NOI18N
        globalIncludePathLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.globalIncludePathLabel.AccessibleContext.accessibleName")); // NOI18N
        globalIncludePathLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.globalIncludePathLabel.AccessibleContext.accessibleDescription")); // NOI18N
        globalIncludePathSeparator.getAccessibleContext().setAccessibleName(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.globalIncludePathSeparator.AccessibleContext.accessibleName")); // NOI18N
        globalIncludePathSeparator.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(PhpOptionsPanel.class, "PhpOptionsPanel.globalIncludePathSeparator.AccessibleContext.accessibleDescription")); // NOI18N
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
        SearchPanel.Strings strings = new SearchPanel.Strings(
                NbBundle.getMessage(PhpOptionsPanel.class, "LBL_PhpInterpretersTitle"),
                NbBundle.getMessage(PhpOptionsPanel.class, "LBL_PhpInterpreters"),
                NbBundle.getMessage(PhpOptionsPanel.class, "LBL_PhpInterpretersPleaseWaitPart"),
                NbBundle.getMessage(PhpOptionsPanel.class, "LBL_NoPhpInterpretersFound"));
        SearchPanel searchPanel = SearchPanel.create(strings, new SearchPanel.Detector() {
            public List<String> detect() {
                return PhpEnvironment.get().getAllPhpInterpreters();
            }
        });
        if (searchPanel.open()) {
            String phpInterpreter = searchPanel.getSelectedItem();
            if (phpInterpreter != null) {
                phpInterpreterTextField.setText(phpInterpreter);
            }
        }
    }//GEN-LAST:event_phpInterpreterSearchButtonActionPerformed

    private void phpUnitBrowseButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phpUnitBrowseButtonActionPerformed
        Utils.browsePhpUnit(this, phpUnitTextField);
    }//GEN-LAST:event_phpUnitBrowseButtonActionPerformed

    private void phpUnitSearchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_phpUnitSearchButtonActionPerformed
        SearchPanel.Strings strings = new SearchPanel.Strings(
                NbBundle.getMessage(PhpOptionsPanel.class, "LBL_PhpUnitsTitle"),
                NbBundle.getMessage(PhpOptionsPanel.class, "LBL_PhpUnits"),
                NbBundle.getMessage(PhpOptionsPanel.class, "LBL_PhpUnitsPleaseWaitPart"),
                NbBundle.getMessage(PhpOptionsPanel.class, "LBL_NoPhpUnitsFound"));
        SearchPanel searchPanel = SearchPanel.create(strings, new SearchPanel.Detector() {
            public List<String> detect() {
                return PhpEnvironment.get().getAllPhpUnits();
            }
        });
        if (searchPanel.open()) {
            String phpUnit = searchPanel.getSelectedItem();
            if (phpUnit != null) {
                phpUnitTextField.setText(phpUnit);
            }
        }
    }//GEN-LAST:event_phpUnitSearchButtonActionPerformed


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
    private JButton phpUnitBrowseButton;
    private JLabel phpUnitLabel;
    private JButton phpUnitSearchButton;
    private JSeparator phpUnitSeparator;
    private JTextField phpUnitTextField;
    private JButton removeButton;
    private JCheckBox stopAtTheFirstLineCheckBox;
    private JLabel unitTestingLabel;
    private JLabel useTheFollowingPathByDefaultLabel;
    private JCheckBox webBrowserCheckBox;
    // End of variables declaration//GEN-END:variables

    private final class DefaultDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            fireChange();
        }
    }
}
