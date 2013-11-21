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

package org.netbeans.modules.avatar_js.project.ui.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.avatar_js.project.AvatarJSProject;
import org.netbeans.modules.avatar_js.project.NashornPlatform;
import org.netbeans.modules.avatar_js.project.NativeLibrarySearch;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Martin
 */
class PanelConfigureProjectComponent extends javax.swing.JPanel {
    
    public static final String PROP_PROJECT_NAME = "projectName";               //NOI18N
    public static final String PROP_PROJECT_LOCATION = "projectLocation";       //NOI18N
    
    private final PanelConfigureProject panel;
    private final NewAvatarJSProjectWizardIterator.WizardType type;
    private final FieldDocumentListener fieldDocumentListener;
    
    /**
     * Creates new form PanelConfigureProjectComponent
     *
    public PanelConfigureProjectComponent() {
        initComponents();
    }*/

    @NbBundle.Messages("LBL_NameAndLoc=Name and Location")
    PanelConfigureProjectComponent(PanelConfigureProject panel, NewAvatarJSProjectWizardIterator.WizardType type) {
        this.panel = panel;
        this.type = type;
        initComponents();
        setName(Bundle.LBL_NameAndLoc());
        // Register listener on the textFields to make the automatic updates
        FieldDocumentListener dl = new FieldDocumentListener();
        prjNameTextField.getDocument().addDocumentListener(dl);
        prjLocationTextField.getDocument().addDocumentListener(dl);
        libsFolderTextField.getDocument().addDocumentListener(dl);
        mainServerTextField.getDocument().addDocumentListener(dl);
        portTextField.getDocument().addDocumentListener(dl);
        fieldDocumentListener = dl;
        
        platformsComboBox.setModel(PlatformUiSupport.createPlatformComboBoxModel(
                null,
                Collections.singleton(NashornPlatform.getFilter())));
        platformsComboBox.addActionListener(new PlatformChangeListener());
    }
    
    private JavaPlatform getSelectedPlatform() {
        Object selObj = platformsComboBox.getSelectedItem();
        if (selObj == null) {
            return null;
        } else {
            return PlatformUiSupport.getPlatform(selObj);
        }
    }
    
    private void selectPlatform(JavaPlatform platform) {
        int n = platformsComboBox.getItemCount();
        for (int i = 0; i < n; i++) {
            Object obj = platformsComboBox.getItemAt(i);
            JavaPlatform p = PlatformUiSupport.getPlatform(obj);
            if (platform.equals(p)) {
                platformsComboBox.setSelectedIndex(i);
                return ;
            }
        }
    }
    
    @NbBundle.Messages({"MSG_ERR_IllegalProjectName=Project Name is not a valid folder name.",
                        "MSG_ERR_IllegalProjectLocation=Project Folder is not a valid path.",
                        "MSG_ERR_ProjectInRootNotSupported=Creating a project in the filesystem root folder is not supported.",
                        "MSG_ERR_MSG_ProjectFolderReadOnly=Project Folder cannot be created.",
                        "MSG_ERR_ProjectFolderExists=Project Folder already exists and is not empty.",
                        "MSG_ERR_ProjectMightAlreadyExist=Project might already exist (probably only in memory)"})
    private String validProject(WizardDescriptor wizardDescriptor) {
        String errorMsg = "";   // NOI18N
        File destFolder = new File (prjFolderTextField.getText()).getAbsoluteFile();
        if (WUtils.isIllegalName(prjNameTextField.getText())) {
            errorMsg = Bundle.MSG_ERR_IllegalProjectName();
        } else {
            File f = new File (prjLocationTextField.getText()).getAbsoluteFile();
            if (WUtils.getCanonicalFile(f) == null) {
                errorMsg = Bundle.MSG_ERR_IllegalProjectLocation();
            } else {
                File ff = WUtils.getCanonicalFile(destFolder);
                if (ff == null) {
                    errorMsg = Bundle.MSG_ERR_IllegalProjectLocation();
                } else if (Utilities.isUnix() && ff.getParentFile().getParent() == null) {
                    // not allow to create project on unix root folder, see #82339
                    errorMsg = Bundle.MSG_ERR_ProjectInRootNotSupported();
                } else {
                    File projLoc = FileUtil.normalizeFile(destFolder);
                    while (projLoc != null && !projLoc.exists()) {
                        projLoc = projLoc.getParentFile();
                    }
                    if (projLoc == null || !projLoc.canWrite()) {
                        errorMsg = Bundle.MSG_ERR_MSG_ProjectFolderReadOnly();
                    }
                    FileObject prjFO = FileUtil.toFileObject(projLoc);
                    if (prjFO == null) {
                        errorMsg = Bundle.MSG_ERR_IllegalProjectLocation();
                    }
                }
            }
        }
        if (errorMsg.isEmpty()) {
            File[] kids = destFolder.listFiles();
            if ( destFolder.exists() && kids != null && kids.length > 0) {
                // Folder exists and is not empty
                errorMsg = Bundle.MSG_ERR_ProjectFolderExists();
            }

            Project prj = null;
            boolean foundButBroken = false;
            FileObject prjFO = FileUtil.toFileObject(FileUtil.normalizeFile(destFolder));
            try {
                prj = ProjectManager.getDefault().findProject(prjFO);
            } catch (IOException ex) {
                foundButBroken = true;
            } catch (IllegalArgumentException ex) {
                // we have passed non-folder - should be already handled
            }
            if (prj != null || foundButBroken) {
                errorMsg = Bundle.MSG_ERR_ProjectMightAlreadyExist();
            }
        }
        return errorMsg;
    }
        
    @NbBundle.Messages({"# {0} - The minimum version of acceptable Java platform",
                        "MSG_ERR_NoNashornPlatform=No suitable Java platform is selected. The minimum version is {0}.",
                        "# {0} - Comma-separated list of missing Avatar.js libraries",
                        "MSG_ERR_MissingAvatarLibs=Avatar.js libraries {0} not found.",
                        "# {0} - The name of Avatar.js JAR file",
                        "MSG_ERR_MissingAvatarJAR={0} not found in the libraries folder."})
    private String validLibs(WizardDescriptor wizardDescriptor) {
        String errorMsg = "";   // NOI18N
        JavaPlatform platform = getSelectedPlatform();
        if (!NashornPlatform.isNashornPlatform(platform)) {
            platform = null;
        }
        if (platform == null) {
            errorMsg = Bundle.MSG_ERR_NoNashornPlatform(NashornPlatform.getMinimumVersion());
        } else {
            String libsFolderPath = libsFolderTextField.getText();
            File libsFolderFile = new File(libsFolderPath);
            String[] missingAvatarLibraries = NativeLibrarySearch.getMissingAvatarLibrariesIn(libsFolderFile);
            if (missingAvatarLibraries != null) {
                String missingLibNames = Arrays.toString(missingAvatarLibraries);
                missingLibNames = missingLibNames.substring(1, missingLibNames.length() - 1);
                errorMsg = Bundle.MSG_ERR_MissingAvatarLibs(missingLibNames);
            } else {
                File jar = new File(libsFolderFile, AvatarJSProject.AVATAR_JS_JAR_NAME);
                if (!jar.exists() || !jar.canRead()) {
                    errorMsg = Bundle.MSG_ERR_MissingAvatarJAR(AvatarJSProject.AVATAR_JS_JAR_NAME);
                }
            }
        }
        return errorMsg;
    }
    
    @NbBundle.Messages({"MSG_ERR_IllegalMainFileName=Main server file is not a valid file name."})
    private String validMainFile(WizardDescriptor wizardDescriptor) {
        String errorMsg = "";   // NOI18N
        if (mainServerCheckBox.isSelected()) {
            String file = mainServerTextField.getText().trim();
            if (file.isEmpty() ||
                WUtils.getCanonicalFile(new File(new File (prjFolderTextField.getText()).getAbsoluteFile(), file)) == null) {
                
                errorMsg = Bundle.MSG_ERR_IllegalMainFileName();
            }
        }
        if (errorMsg.isEmpty()) {
            String portStr = portTextField.getText().trim();
            int port;
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException nfex) {
                port = 0;
                errorMsg = nfex.getLocalizedMessage();
            }
        }
        return errorMsg;
    }
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        String errorMsg = validProject(wizardDescriptor);
        if (errorMsg.isEmpty()) {
            errorMsg = validLibs(wizardDescriptor);
        }
        if (errorMsg.isEmpty()) {
            errorMsg = validMainFile(wizardDescriptor);
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errorMsg);
        return errorMsg.isEmpty();
    }
    
    @NbBundle.Messages({"# {0} - name index",
                        "TXT_AvatarServer=AvatarServer{0}"})
    private void readProject(WizardDescriptor d) {
        Object lastType =  d.getProperty(WizardSettings.PROP_WTYPE);
        if (lastType == null || lastType != type) {
            //bugfix #46387 The type of project changed, reset values to defaults
            d.putProperty (WizardSettings.PROP_NAME, null);
            d.putProperty (WizardSettings.PROP_PRJ_DIR,null);
        }
        
        File projectLocation = (File) d.getProperty (WizardSettings.PROP_PRJ_DIR);
        if (projectLocation == null || projectLocation.getParentFile() == null ||
            (projectLocation.getParentFile().exists() && !projectLocation.getParentFile().isDirectory ())) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
        }
        this.prjLocationTextField.setText (projectLocation.getAbsolutePath());
        
        String projectName = (String) d.getProperty (WizardSettings.PROP_NAME);
        if (projectName == null) {
            switch (type) {
            case MAIN:
                int baseCount = WizardSettings.getNewApplicationCount() + 1;
                while ((projectName = WUtils.validFreeProjectName(projectLocation, Bundle.TXT_AvatarServer(baseCount))) == null) {
                    baseCount++;
                }
                d.putProperty (WizardSettings.PROP_NAME_INDEX, new Integer(baseCount));
                break;
            default:
                throw new IllegalStateException(type.name());
            }            
        }
        this.prjNameTextField.setText (projectName);
        this.prjNameTextField.selectAll();
        this.prjFolderTextField.setText(new File(projectLocation, projectName).getAbsolutePath());
    }
    
    private void readLibs(WizardDescriptor d) {
        JavaPlatform platform = (JavaPlatform) d.getProperty(WizardSettings.PROP_JAVA_PLATFORM);
        if (platform == null) {
            platform = NashornPlatform.getDefault();
        }
        selectPlatform(platform);
        String libsFolder = (String) d.getProperty(WizardSettings.PROP_AVATAR_LIBS);
        if (libsFolder != null) {
            libsFolderTextField.setText(libsFolder);
        } else {
            File libsDir = NativeLibrarySearch.findAvatarLibrariesFolder();
            if (libsDir == null) {
                libsFolderTextField.setText(""); // NOI18N
            } else {
                libsFolderTextField.setText(libsDir.getAbsolutePath());
            }
        }
    }
    
    private void readMainFile(WizardDescriptor d) {
        String mainFile = (String) d.getProperty(WizardSettings.PROP_MAIN_FILE);
        if (mainFile == null) {
            mainFile = prjNameTextField.getText();
        }
        mainServerTextField.setText(mainFile);
        String portStr = (String) d.getProperty(WizardSettings.PROP_SERVER_FILE_PORT);
        if (portStr == null) {
            portStr = "";   // NOI18N
        }
        portTextField.setText(portStr);
    }
    
    void read(WizardDescriptor d) {
        fieldDocumentListener.setActive(false);
        readProject(d);
        readLibs(d);
        readMainFile(d);
        fieldDocumentListener.setActive(true);
    }
    
    private void storeProject(WizardDescriptor d) {
        d.putProperty(WizardSettings.PROP_WTYPE, type);   //NOI18N
        
        String name = prjNameTextField.getText().trim();
        String folder = prjFolderTextField.getText().trim();
        
        d.putProperty(WizardSettings.PROP_PRJ_DIR, new File( folder ));
        d.putProperty(WizardSettings.PROP_NAME, name );
    }
    
    private void storeLibs(WizardDescriptor d) {
        JavaPlatform platform = getSelectedPlatform();
        d.putProperty(WizardSettings.PROP_JAVA_PLATFORM, platform);
        String libsFolder =  libsFolderTextField.getText();
        File libsDir = NativeLibrarySearch.findAvatarLibrariesFolder();
        File avatar_js_JAR = new File(libsFolder, AvatarJSProject.AVATAR_JS_JAR_NAME);
        if (libsDir != null && libsDir.equals(new File(libsFolder))) {
            libsFolder = null; // No need to specify path to libraries that can be directly loaded
        }
        d.putProperty(WizardSettings.PROP_AVATAR_LIBS, libsFolder);
        d.putProperty(WizardSettings.PROP_AVATAR_JAR, avatar_js_JAR);
    }
    
    private void storeMainFile(WizardDescriptor d) {
        d.putProperty(WizardSettings.PROP_MAIN_FILE, mainServerTextField.getText().trim());
        d.putProperty(WizardSettings.PROP_SERVER_FILE_PORT, portTextField.getText().trim());
    }
    
    void store(WizardDescriptor d) {
        storeProject(d);
        storeLibs(d);
        storeMainFile(d);
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        // Nothing to validate
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        prjNameLabel = new javax.swing.JLabel();
        prjNameTextField = new javax.swing.JTextField();
        prjLocationLabel = new javax.swing.JLabel();
        prjLocationTextField = new javax.swing.JTextField();
        prjLocationButton = new javax.swing.JButton();
        prjFolderLabel = new javax.swing.JLabel();
        prjFolderTextField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        platformLabel = new javax.swing.JLabel();
        platformsComboBox = new javax.swing.JComboBox();
        platformManageButton = new javax.swing.JButton();
        libsLabel = new javax.swing.JLabel();
        libsFolderTextField = new javax.swing.JTextField();
        libsBrowseButton = new javax.swing.JButton();
        libsDescrLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        mainServerCheckBox = new javax.swing.JCheckBox();
        mainServerTextField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portTextField = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(prjNameLabel, org.openide.util.NbBundle.getMessage(PanelConfigureProjectComponent.class, "PanelConfigureProjectComponent.prjNameLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(prjLocationLabel, org.openide.util.NbBundle.getMessage(PanelConfigureProjectComponent.class, "PanelConfigureProjectComponent.prjLocationLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(prjLocationButton, org.openide.util.NbBundle.getMessage(PanelConfigureProjectComponent.class, "PanelConfigureProjectComponent.prjLocationButton.text")); // NOI18N
        prjLocationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prjLocationButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(prjFolderLabel, org.openide.util.NbBundle.getMessage(PanelConfigureProjectComponent.class, "PanelConfigureProjectComponent.prjFolderLabel.text")); // NOI18N

        prjFolderTextField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(platformLabel, org.openide.util.NbBundle.getMessage(PanelConfigureProjectComponent.class, "PanelConfigureProjectComponent.platformLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(platformManageButton, org.openide.util.NbBundle.getMessage(PanelConfigureProjectComponent.class, "PanelConfigureProjectComponent.platformManageButton.text")); // NOI18N
        platformManageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                platformManageButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(libsLabel, org.openide.util.NbBundle.getMessage(PanelConfigureProjectComponent.class, "PanelConfigureProjectComponent.libsLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(libsBrowseButton, org.openide.util.NbBundle.getMessage(PanelConfigureProjectComponent.class, "PanelConfigureProjectComponent.libsBrowseButton.text")); // NOI18N
        libsBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                libsBrowseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(libsDescrLabel, org.openide.util.NbBundle.getMessage(PanelConfigureProjectComponent.class, "PanelConfigureProjectComponent.libsDescrLabel.text")); // NOI18N
        libsDescrLabel.setEnabled(false);

        mainServerCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(mainServerCheckBox, org.openide.util.NbBundle.getMessage(PanelConfigureProjectComponent.class, "PanelConfigureProjectComponent.mainServerCheckBox.text")); // NOI18N
        mainServerCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainServerCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(PanelConfigureProjectComponent.class, "PanelConfigureProjectComponent.portLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(prjLocationLabel)
                    .addComponent(prjFolderLabel)
                    .addComponent(prjNameLabel)
                    .addComponent(platformLabel)
                    .addComponent(libsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(libsBrowseButton, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(platformManageButton, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(prjFolderTextField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(prjNameTextField)
                            .addComponent(prjLocationTextField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(prjLocationButton))))
            .addComponent(jSeparator1)
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGap(147, 147, 147)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(libsDescrLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap(175, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(libsFolderTextField)
                        .addGap(93, 93, 93))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(platformsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(174, 174, 174))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainServerCheckBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(portLabel)))
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mainServerTextField)
                    .addComponent(portTextField)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prjNameLabel)
                    .addComponent(prjNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prjLocationLabel)
                    .addComponent(prjLocationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(prjLocationButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(prjFolderLabel)
                    .addComponent(prjFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(platformLabel)
                    .addComponent(platformsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(platformManageButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(libsLabel)
                    .addComponent(libsFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(libsBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(libsDescrLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mainServerCheckBox)
                    .addComponent(mainServerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portLabel)
                    .addComponent(portTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(39, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void prjLocationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prjLocationButtonActionPerformed
        String fchKey = PanelConfigureProjectComponent.class.getName()+"."+WizardSettings.PROP_PRJ_DIR;
        FileChooserBuilder fchb = new FileChooserBuilder(fchKey);
        fchb.setDirectoriesOnly(true);
        File f = fchb.showOpenDialog();
        if (f != null) {
            prjFolderTextField.setText(f.getAbsolutePath());
        }
    }//GEN-LAST:event_prjLocationButtonActionPerformed

    private void platformManageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformManageButtonActionPerformed
        PlatformsCustomizer.showCustomizer(null);
    }//GEN-LAST:event_platformManageButtonActionPerformed

    private void libsBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_libsBrowseButtonActionPerformed
        String fchKey = PanelConfigureProjectComponent.class.getName()+"."+WizardSettings.PROP_AVATAR_LIBS;
        FileChooserBuilder fchb = new FileChooserBuilder(fchKey);
        fchb.setDirectoriesOnly(true);
        File f = fchb.showOpenDialog();
        if (f != null) {
            libsFolderTextField.setText(f.getAbsolutePath());
        }
    }//GEN-LAST:event_libsBrowseButtonActionPerformed

    private void mainServerCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mainServerCheckBoxActionPerformed
        boolean selected = mainServerCheckBox.isSelected();
        mainServerTextField.setEnabled(selected);
        portLabel.setEnabled(selected);
        portTextField.setEnabled(selected);
        panel.fireChangeEvent();
    }//GEN-LAST:event_mainServerCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JButton libsBrowseButton;
    private javax.swing.JLabel libsDescrLabel;
    private javax.swing.JTextField libsFolderTextField;
    private javax.swing.JLabel libsLabel;
    private javax.swing.JCheckBox mainServerCheckBox;
    private javax.swing.JTextField mainServerTextField;
    private javax.swing.JLabel platformLabel;
    private javax.swing.JButton platformManageButton;
    private javax.swing.JComboBox platformsComboBox;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextField portTextField;
    private javax.swing.JLabel prjFolderLabel;
    private javax.swing.JTextField prjFolderTextField;
    private javax.swing.JButton prjLocationButton;
    private javax.swing.JLabel prjLocationLabel;
    private javax.swing.JTextField prjLocationTextField;
    private javax.swing.JLabel prjNameLabel;
    private javax.swing.JTextField prjNameTextField;
    // End of variables declaration//GEN-END:variables

    private class FieldDocumentListener implements DocumentListener {
        
        private boolean active = true;
        
        void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update(e);
        }
        
        private void update(DocumentEvent e) {
            if (!active) {
                return ;
            }
            updateTexts(e);
            Document doc = e.getDocument();
            if (doc == prjNameTextField.getDocument()) {
                firePropertyChange(PROP_PROJECT_NAME, null, prjNameTextField.getText());
            }
            if (doc == prjLocationTextField.getDocument()) {
                firePropertyChange(PROP_PROJECT_LOCATION, null, prjLocationTextField.getText());
            }
            if (doc == libsFolderTextField.getDocument()) {
                firePropertyChange(WizardSettings.PROP_AVATAR_LIBS, null, libsFolderTextField.getText());
            }
            if (doc == mainServerTextField.getDocument()) {
                firePropertyChange(WizardSettings.PROP_MAIN_FILE, null, mainServerTextField.getText());
            }
            if (doc == portTextField.getDocument()) {
                firePropertyChange(WizardSettings.PROP_SERVER_FILE_PORT, null, portTextField.getText());
            }
        }
        
        /** Handles changes in the Project name and project directory
         */
        private void updateTexts( DocumentEvent e ) {
            Document doc = e.getDocument();
            if ( doc == prjNameTextField.getDocument() || doc == prjLocationTextField.getDocument() ) {
                // Change in the project name
                String projectName = prjNameTextField.getText();
                String projectFolder = prjLocationTextField.getText();
                String projFolderPath = FileUtil.normalizeFile(new File(projectFolder)).getAbsolutePath();
                if (projFolderPath.endsWith(File.separator)) {
                    prjFolderTextField.setText(projFolderPath + projectName);
                } else {
                    prjFolderTextField.setText(projFolderPath + File.separator + projectName);
                }
            }                
            panel.fireChangeEvent(); // Notify that the panel changed
        }

    }
    
    private class PlatformChangeListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            firePropertyChange (WizardSettings.PROP_JAVA_PLATFORM, null, getSelectedPlatform());
            panel.fireChangeEvent(); // Notify that the panel changed
        }
        
    }
}
