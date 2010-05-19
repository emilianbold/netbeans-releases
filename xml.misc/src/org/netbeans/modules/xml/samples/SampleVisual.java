/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.xml.samples;

import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileUtil;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.06.14
 */
final class SampleVisual extends JPanel implements DocumentListener {

    SampleVisual(String name) {
        initComponents();
        myProjectNameTextField.getDocument().addDocumentListener(this);
        myProjectLocationTextField.getDocument().addDocumentListener(this);
        myName = name;
    }

    protected void setPanel(SamplePanel panel) {
        myPanel = panel;
    }

    public String getProjectName() {
        return myProjectNameTextField.getText();
    }

    private void initComponents() {
        GridBagConstraints c;

        JLabel projectNameLabel = new JLabel();
        myProjectNameTextField = new JTextField();
        JLabel projectLocationLabel = new JLabel();
        myProjectLocationTextField = new JTextField();
        myBrowseButton = new JButton();
        JLabel label = new JLabel();
        myCreatedFolderTextField = new JTextField();

        setLayout(new GridBagLayout());

        projectNameLabel.setLabelFor(myProjectNameTextField);
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, LARGE_SIZE, 0);
        Mnemonics.setLocalizedText(projectNameLabel, i18n(SampleVisual.class, "LBL_ProjectName")); // NOI18N
        add(projectNameLabel, c);
        a11y(myProjectNameTextField, i18n(SampleVisual.class, "ACS_ProjectName")); // NOI18N

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.insets = new Insets(0, LARGE_SIZE, LARGE_SIZE, 0);
        add(myProjectNameTextField, c);

        projectLocationLabel.setLabelFor(myProjectLocationTextField);
        a11y(myProjectLocationTextField, i18n(SampleVisual.class, "ACS_ProjectLocation")); // NOI18N

        c = new GridBagConstraints();
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, MEDIUM_SIZE, 0);
        Mnemonics.setLocalizedText(projectLocationLabel, i18n(SampleVisual.class, "LBL_ProjectLocation")); // NOI18N
        add(projectLocationLabel, c);

        c = new GridBagConstraints();
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.insets = new Insets(0, LARGE_SIZE, MEDIUM_SIZE, 0);
        add(myProjectLocationTextField, c);

        myBrowseButton.setActionCommand(BROWSE);
        myBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                browseLocationAction(evt);
            }
        });
        c = new GridBagConstraints();
        c.gridy = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(0, MEDIUM_SIZE, MEDIUM_SIZE, 0);
        Mnemonics.setLocalizedText(myBrowseButton, i18n(SampleVisual.class, "LBL_BrowseLocation")); // NOI18N
        a11y(myBrowseButton, i18n(SampleVisual.class, "ACS_BrowseLocation")); // NOI18N
        add(myBrowseButton, c);
        label.setLabelFor(myCreatedFolderTextField);

        c = new GridBagConstraints();
        c.gridheight = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.NORTHWEST;
        Mnemonics.setLocalizedText(label, i18n(SampleVisual.class, "LBL_CreatedProjectFolder")); // NOI18N
        add(label, c);

        myCreatedFolderTextField.setEditable(false);
        a11y(myCreatedFolderTextField, i18n(SampleVisual.class, "ACS_CreatedProjectFolder")); // NOI18N

        c = new GridBagConstraints();
        c.gridy = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, LARGE_SIZE, LARGE_SIZE, 0);
        add(myCreatedFolderTextField, c);

        c.gridx = 0;
        c.gridy = 2 + 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2 + 1/*GridBagConstraints.REMAINDER*/;
        c.insets = new Insets(MEDIUM_SIZE, 0, MEDIUM_SIZE, 0);
        add(new JSeparator(), c);

        c.gridy++;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        myAsMainProject = createCheckBox(
            new ButtonAction(i18n(SampleVisual.class, "LBL_As_Main_Project")) { // NOI18N
                public void actionPerformed(ActionEvent event) {
                }
            }
        );
        myAsMainProject.setSelected(true);
        add(myAsMainProject, c);

        initAdditionalComponents();
    }

    protected void initAdditionalComponents() {
        JLabel specialLabel = new JLabel();
        specialLabel.setFocusable(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1 + 2;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(0, MEDIUM_SIZE, 0, 0);
        add(specialLabel, c);
    }

    private void browseLocationAction(ActionEvent event) {
        if (!BROWSE.equals(event.getActionCommand())) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(i18n(SampleVisual.class, "LBL_SelectProjectLocation")); // NOI18N
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String path = myProjectLocationTextField.getText();

        if (path.length() > 0) {
            File f = new File(path);

            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File projectDir = chooser.getSelectedFile();
            myProjectLocationTextField.setText(FileUtil.normalizeFile(projectDir).getAbsolutePath());
        }
        myPanel.fireChangeEvent();
    }

    public void addNotify() {
        super.addNotify();
        myProjectNameTextField.requestFocus();
    }

    boolean valid(WizardDescriptor wizardDescriptor) {
//out();
        String projectName = myProjectNameTextField.getText().trim();
//out("projectName: " + projectName + " " + isValidName(projectName));

        if (projectName.length() == 0 || projectName.indexOf('/') >= 0 || projectName.indexOf("\\") >= 0 || projectName.indexOf(':') >= 0 || !isValidName(projectName)) { // NOI18N
//out(" 1: " + projectName + " " + isValidName(projectName));
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, i18n(SampleVisual.class, "LBL_Invalid_ProjectName")); // NOI18N
            return false;
        }
        if (projectName.indexOf(' ') >= 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, i18n(SampleVisual.class, "LBL_Invalid_ProjectNameWithWhiteSpace")); // NOI18N
            return false;
        }
        File f = new File(myProjectLocationTextField.getText().trim()).getAbsoluteFile();

        if (getCanonicalFile(f) == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, i18n(SampleVisual.class, "LBL_Invalid_ProjectLocation")); // NOI18N
            return false;
        }
        File destFolder = new File(myCreatedFolderTextField.getText().trim()).getAbsoluteFile();

        if (getCanonicalFile(destFolder) == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, i18n(SampleVisual.class, "LBL_Invalid_ProjectName")); // NOI18N
            return false;
        }
        File projLoc = FileUtil.normalizeFile(destFolder);

        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, i18n(SampleVisual.class, "LBL_ProjectFolderReadOnly")); // NOI18N
            return false;
        }
        if (FileUtil.toFileObject(projLoc) == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, i18n(SampleVisual.class, "LBL_Invalid_ProjectLocation")); // NOI18N
            return false;
        }
        File[] kids = destFolder.listFiles();

        if (destFolder.exists() && kids != null && kids.length > 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, i18n(SampleVisual.class, "LBL_ProjectFolderExists")); // NOI18N
            return false;
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N

        return true;
    }

    private boolean isValidName(String fileName) {
        try {
            boolean isValid = true;
            File tempFile = new File(fileName);
            String tempFileName = "$$" + fileName; // NOI18N
            File actualTempFile = File.createTempFile(tempFileName, null);

            if (!FileUtil.normalizeFile(tempFile).equals(tempFile.getCanonicalFile())) {
                isValid = false;
            }
            actualTempFile.delete();
            actualTempFile = null;
            tempFile = null;
            return isValid;
        } catch (IOException e) {
            return false;
        }
    }

    protected void store(WizardDescriptor d) {
        String name = myProjectNameTextField.getText().trim();

        d.putProperty(PROJECT_DIR, new File(myCreatedFolderTextField.getText().trim()));
        d.putProperty(PROJECT_NAME, name);

        File projectsDir = new File(myProjectLocationTextField.getText().trim());

        if (projectsDir.isDirectory()) {
            ProjectChooser.setProjectsFolder(projectsDir);
        }
        d.putProperty(SET_AS_MAIN, myAsMainProject.isSelected() ? Boolean.TRUE : Boolean.FALSE);
    }

    protected void read(WizardDescriptor d) {
        File projectLocation = (File) d.getProperty(PROJECT_DIR);

        if (projectLocation == null || projectLocation.getParentFile() == null || !projectLocation.getParentFile().isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        myProjectLocationTextField.setText(projectLocation.getAbsolutePath());
        String projectName = (String) d.getProperty(PROJECT_NAME);

        if (projectName == null) {
            int index = 1;
            projectName = myName;
            File file = new File(projectLocation, projectName);

            if (file.exists()) {
                while (file.exists()) {
                    file = new File(projectLocation, projectName + String.valueOf(index));
                    index++;
                }
                projectName = projectName + String.valueOf(index - 1);
            }
        }
        myProjectNameTextField.setText(projectName);
        myProjectNameTextField.selectAll();
    }

    protected void validate(WizardDescriptor d) throws WizardValidationException {
    }

    private static JFileChooser createChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setName(i18n(SampleVisual.class, "LBL_SelectProjectDirectory")); // NOI18N
        return chooser;
    }

    public void changedUpdate(DocumentEvent event) {
        updateTexts(event);

        if (myProjectNameTextField.getDocument() == event.getDocument()) {
            firePropertyChange(PROJECT_NAME, null, myProjectNameTextField.getText().trim());
        }
    }

    public void insertUpdate(DocumentEvent event) {
        updateTexts(event);

        if (myProjectNameTextField.getDocument() == event.getDocument()) {
            firePropertyChange(PROJECT_NAME, null, myProjectNameTextField.getText().trim());
        }
    }

    public void removeUpdate(DocumentEvent event) {
        updateTexts(event);

        if (myProjectNameTextField.getDocument() == event.getDocument()) {
            firePropertyChange(PROJECT_NAME, null, myProjectNameTextField.getText().trim());
        }
    }

    private void updateTexts(DocumentEvent event) {
        Document doc = event.getDocument();

        if (doc == myProjectNameTextField.getDocument() || doc == myProjectLocationTextField.getDocument()) {
            String projectName = myProjectNameTextField.getText().trim();
            String projectFolder = myProjectLocationTextField.getText().trim();
            String projFolderPath = FileUtil.normalizeFile(new File(projectFolder)).getAbsolutePath();

            if (projFolderPath.endsWith(File.separator)) {
                myCreatedFolderTextField.setText(projFolderPath + projectName);
            } else {
                myCreatedFolderTextField.setText(projFolderPath + File.separator + projectName);
            }
        }
        myPanel.fireChangeEvent();
    }

    private File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return null;
        }
    }


    private int myType;
    private SamplePanel myPanel;
    private String myName;
    private JButton myBrowseButton;
    private JCheckBox myAsMainProject;
    private JTextField myProjectNameTextField;
    private JTextField myCreatedFolderTextField;
    private JTextField myProjectLocationTextField;

    private static final String BROWSE = "browse"; // NOI18N
    private static final String SET_AS_MAIN = "setAsMain"; // NOI18N

    protected static final String PROJECT_DIR = "project.dir"; // NOI18N
    protected static final String PROJECT_NAME = "project.name"; // NOI18N
}
