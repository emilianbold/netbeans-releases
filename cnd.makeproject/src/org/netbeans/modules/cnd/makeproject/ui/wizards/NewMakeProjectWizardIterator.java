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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cnd.api.remote.SelectHostWizardProvider;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.MakeProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibrariesConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.QmakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension.ProjectKind;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Make project.
 */
public class NewMakeProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    private static final long serialVersionUID = 1L;
    public static final String APPLICATION_PROJECT_NAME = "CppApplication"; // NOI18N
    public static final String DYNAMICLIBRARY_PROJECT_NAME = "CppDynamicLibrary";  // NOI18N
    public static final String STATICLIBRARY_PROJECT_NAME = "CppStaticLibrary"; // NOI18N
    public static final String MAKEFILEPROJECT_PROJECT_NAME = "MakefileProject"; // NOI18N
    public static final String BINARY_PROJECT_NAME = "BinaryProject"; // NOI18N
    public static final String FULL_REMOTE_PROJECT_NAME = "FullRemoteProject"; // NOI18N
    public static final String QTAPPLICATION_PROJECT_NAME = "QtApplication"; // NOI18N
    public static final String QTDYNAMICLIBRARY_PROJECT_NAME = "QtDynamicLibrary"; // NOI18N
    public static final String QTSTATICLIBRARY_PROJECT_NAME = "QtStaticLibrary"; // NOI18N
    public static final String DBAPPLICATION_PROJECT_NAME = "DbApplication"; // NOI18N
    static final String PROP_NAME_INDEX = "nameIndex"; // NOI18N
    // Wizard types
    public static final int TYPE_MAKEFILE = 0;
    public static final int TYPE_APPLICATION = 1;
    public static final int TYPE_DYNAMIC_LIB = 2;
    public static final int TYPE_STATIC_LIB = 3;
    public static final int TYPE_QT_APPLICATION = 4;
    public static final int TYPE_QT_DYNAMIC_LIB = 5;
    public static final int TYPE_QT_STATIC_LIB = 6;
    public static final int TYPE_BINARY = 7;
    public static final int TYPE_DB_APPLICATION = 8;

    private final int wizardtype;
    private final boolean fullRemote;

    private Boolean lastSimpleMode = null;
    private String lastHostUid = null;
    private Boolean lastSetupHost = null;

    private SelectHostWizardProvider selectHostWizardProvider;
    private WizardDescriptor.Panel<WizardDescriptor> selectHostPanel;
    private WizardDescriptor.Panel<WizardDescriptor> selectBinaryPanel;
    private int lastNewHostPanel = -1;
    
    private SelectModeDescriptorPanel selectModePanel;
    private final PanelConfigureProject panelConfigureProjectTrue;
//    private final PanelConfigureProject panelConfigureProjectFalse;
//    private final MakefileOrConfigureDescriptorPanel makefileOrConfigureDescriptorPanel;
//    private final BuildActionsDescriptorPanel buildActionsDescriptorPanel;
//    private final SourceFoldersDescriptorPanel sourceFoldersDescriptorPanel;
//    private final ParserConfigurationDescriptorPanel parserConfigurationDescriptorPanel;
    private final List<WizardDescriptor.Panel<WizardDescriptor>> advancedPanels;

    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N

    private NewMakeProjectWizardIterator(int wizardtype, String name, String wizardTitle, String wizardACSD) {
        this(wizardtype, name, wizardTitle, wizardACSD, false);
    }

    private NewMakeProjectWizardIterator(int wizardtype, String name, String wizardTitle, String wizardACSD, boolean fullRemote) {
        this.wizardtype = wizardtype;
        name = name.replaceAll(" ", ""); // NOI18N
        this.fullRemote = fullRemote;

        panelConfigureProjectTrue = new PanelConfigureProject(name, wizardtype, wizardTitle, wizardACSD, true);

//        panelConfigureProjectFalse = new PanelConfigureProject(name, wizardtype, wizardTitle, wizardACSD, false);
//        makefileOrConfigureDescriptorPanel = new MakefileOrConfigureDescriptorPanel();
//        buildActionsDescriptorPanel = new BuildActionsDescriptorPanel();
//        sourceFoldersDescriptorPanel = new SourceFoldersDescriptorPanel();
//        parserConfigurationDescriptorPanel = new ParserConfigurationDescriptorPanel();
        advancedPanels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        advancedPanels.add(new MakefileOrConfigureDescriptorPanel());
        advancedPanels.add(new BuildActionsDescriptorPanel());
        advancedPanels.add(new SourceFoldersDescriptorPanel());
        advancedPanels.add(new ParserConfigurationDescriptorPanel());
        advancedPanels.add(new PanelConfigureProject(name, wizardtype, wizardTitle, wizardACSD, false));
    }

    private synchronized SelectModeDescriptorPanel getSelectModePanel() {
        if (selectModePanel == null) {
            selectModePanel = new SelectModeDescriptorPanel(fullRemote);
            selectModePanel.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    setupPanelsAndStepsIfNeed();
                }
            });
        }
        return selectModePanel;
    }
    
    public static NewMakeProjectWizardIterator newApplication() {
        String name = APPLICATION_PROJECT_NAME; //getString("NativeNewApplicationName"); // NOI18N
        String wizardTitle = getString("Templates/Project/Native/newApplication.xml"); // NOI18N
        String wizardACSD = getString("NativeNewLibraryACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_APPLICATION, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newDynamicLibrary() {
        String name = DYNAMICLIBRARY_PROJECT_NAME; //getString("NativeNewDynamicLibraryName"); // NOI18N
        String wizardTitle = getString("Templates/Project/Native/newDynamicLibrary.xml"); // NOI18N
        String wizardACSD = getString("NativeNewDynamicLibraryACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_DYNAMIC_LIB, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newStaticLibrary() {
        String name = STATICLIBRARY_PROJECT_NAME; //getString("NativeNewStaticLibraryName");
        String wizardTitle = getString("Templates/Project/Native/newStaticLibrary.xml");
        String wizardACSD = getString("NativeNewStaticLibraryACSD");
        return new NewMakeProjectWizardIterator(TYPE_STATIC_LIB, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newQtApplication() {
        String name = QTAPPLICATION_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/newQtApplication.xml");
        String wizardACSD = getString("NativeNewQtApplicationACSD");
        return new NewMakeProjectWizardIterator(TYPE_QT_APPLICATION, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newQtDynamicLibrary() {
        String name = QTDYNAMICLIBRARY_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/newQtDynamicLibrary.xml");
        String wizardACSD = getString("NativeNewQtDynamicLibraryACSD");
        return new NewMakeProjectWizardIterator(TYPE_QT_DYNAMIC_LIB, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newQtStaticLibrary() {
        String name = QTSTATICLIBRARY_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/newQtStaticLibrary.xml");
        String wizardACSD = getString("NativeNewQtStaticLibraryACSD");
        return new NewMakeProjectWizardIterator(TYPE_QT_STATIC_LIB, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newDBApplication() {
        String name = DBAPPLICATION_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/newDBApplication.xml");
        String wizardACSD = getString("NativeNewDBApplicationACSD");
        return new NewMakeProjectWizardIterator(TYPE_DB_APPLICATION, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator makefile() {
        String name = MAKEFILEPROJECT_PROJECT_NAME; //getString("NativeMakefileName"); // NOI18N
        String wizardTitle = getString("Templates/Project/Native/makefile.xml"); // NOI18N
        String wizardACSD = getString("NativeMakefileNameACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_MAKEFILE, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator binary() {
        String name = BINARY_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/binary.xml"); // NOI18N
        String wizardACSD = getString("NativeBinaryNameACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_BINARY, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newFullRemote() {
        String name = FULL_REMOTE_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/newFullRemote.xml"); // NOI18N
        String wizardACSD = getString("NativeFullRemoteNameACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_MAKEFILE, name, wizardTitle, wizardACSD, true);
    }

    private static boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else {
            return o1.equals(o2);
        }
    }

    private SelectHostWizardProvider getSelectHostWizardProvider() {
        if (selectHostWizardProvider == null) {
            selectHostWizardProvider = SelectHostWizardProvider.createInstance(false, new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    fireStateChanged();
                }
            });
        }
        return selectHostWizardProvider;
    }

    private synchronized void setupPanelsAndStepsIfNeed() {        
        if (wizardtype == TYPE_APPLICATION || wizardtype == TYPE_DYNAMIC_LIB || wizardtype == TYPE_STATIC_LIB || wizardtype == TYPE_QT_APPLICATION || wizardtype == TYPE_QT_DYNAMIC_LIB || wizardtype == TYPE_QT_STATIC_LIB) {
            if (panels == null) {
                panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
                panels.add(panelConfigureProjectTrue);
                String[] steps = createSteps(panels);
            }
        } else if (wizardtype == TYPE_MAKEFILE) {
            String hostUID = (wiz == null) ? null : (String) wiz.getProperty(WizardConstants.PROPERTY_HOST_UID);
            Boolean setupHost = fullRemote ? Boolean.valueOf(getSelectHostWizardProvider().isNewHost()) : null;

            if (panels != null) {
                if (equals(lastSimpleMode, isSimple())) {
                    if (fullRemote) {
                        if (equals(lastHostUid, hostUID)) {
                            if (equals(lastSetupHost, setupHost)) {
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                }
            }
            lastHostUid = hostUID;
            lastSimpleMode = Boolean.valueOf(isSimple());
            lastSetupHost = setupHost;
            lastNewHostPanel = -1;

            LOGGER.log(Level.FINE, "refreshing panels and steps");

            List<WizardDescriptor.Panel<WizardDescriptor>> panelsList = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
            final SelectModeDescriptorPanel modeSelectionPanel = getSelectModePanel();
            if (fullRemote) {
                if (selectHostPanel == null) {
                    selectHostPanel = getSelectHostWizardProvider().getSelectHostPanel();
                }
                panelsList.add(selectHostPanel);
                if (getSelectHostWizardProvider().isNewHost()) {
                    panelsList.addAll(getSelectHostWizardProvider().getAdditionalPanels());
                    lastNewHostPanel = panelsList.size() - 1;
                    panelsList.add(modeSelectionPanel);
                    if (!isSimple()) {
                        panelsList.addAll(advancedPanels);
                    }
                } else {
                    panelsList.add(modeSelectionPanel);
                    if (!isSimple()) {
                        panelsList.addAll(advancedPanels);
                    }
                }
            } else {
                panelsList.add(modeSelectionPanel);
                if (!isSimple()) {
                    panelsList.addAll(advancedPanels);
                }
            }
            panels = panelsList;
            setupSteps();
        } else if (wizardtype == TYPE_BINARY) {
            if (selectBinaryPanel == null) {
                selectBinaryPanel = new SelectBinaryPanel();
            }
            if (panels == null) {
                panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
                panels.add(selectBinaryPanel);
                //panels.add(advancedPanels.get(1)); // buildActionsDescriptorPanel
                //panels.add(advancedPanels.get(2)); // sourceFoldersDescriptorPanel
                panels.add(advancedPanels.get(4)); // panelConfigureProject
                String[] steps = createSteps(panels);
            }
        } else if(wizardtype == TYPE_DB_APPLICATION) {
            if (panels == null) {
                panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
                panelConfigureProjectTrue.setFinishPanel(false);
                panels.add(panelConfigureProjectTrue);
                WizardDescriptor.Panel<WizardDescriptor> masterPanel = createDatabaseMasterPanel();
                if(masterPanel != null) {
		    panels.add(masterPanel);
                }
                    String[] steps = createSteps(panels);
            }
        } else {
            throw new IllegalStateException("Illegal wizard type: " + wizardtype); //NOI18N
        }
    }

    @SuppressWarnings("unchecked")
    WizardDescriptor.Panel<WizardDescriptor> createDatabaseMasterPanel() {
        FileObject wizardOptions = FileUtil.getConfigFile("Templates/Project/Native/newDBApplication.xml"); //NOI18N
        Object panel = wizardOptions.getAttribute("databaseMasterPanel"); //NOI18N
        if (panel instanceof WizardDescriptor.Panel) {
            return (WizardDescriptor.Panel<WizardDescriptor>) panel;
        }
        return null;
    }

    private void setupSteps() {
        String[] steps = createSteps(panels);
        String[] advanced;
        if (fullRemote) {
            advanced = new String[]{ steps[0], steps[1], "..."}; // NOI18N
        } else {
            advanced = new String[]{ steps[0], "..."}; // NOI18N
        }
        Component c = panels.get(0).getComponent();
        if (c instanceof JComponent) { // assume Swing components
            JComponent jc = (JComponent) c;
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, advanced);
        }
    }

    private String[] createSteps(List<WizardDescriptor.Panel<WizardDescriptor>> panels) {
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            if (panels.get(i) instanceof Name) {
                steps[i] = ((Name) panels.get(i)).getName();
            } else {
                steps[i] = panels.get(i).getComponent().getName();
            }
        }
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
        return steps;
    }

    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        try {
            handle.start();
            return instantiate();
        } catch (IOException ex) {
            ex.printStackTrace(); // since caller doesn't report this
            throw ex;
        } finally {
            handle.finish();
        }
    }


    @Override
    public Set<FileObject> instantiate() throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        File dirF = (File) wiz.getProperty(WizardConstants.PROPERTY_PROJECT_FOLDER);
        String hostUID = (String) wiz.getProperty(WizardConstants.PROPERTY_HOST_UID);
        //boolean fullRemote = (wiz.getProperty(WizardConstants.PROPERTY_FULL_REMOTE) == null) ? false : ((Boolean) wiz.getProperty(WizardConstants.PROPERTY_FULL_REMOTE)).booleanValue();
        CompilerSet toolchain = (CompilerSet) wiz.getProperty(WizardConstants.PROPERTY_TOOLCHAIN);
        boolean defaultToolchain = Boolean.TRUE.equals(wiz.getProperty(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT));
        if (dirF != null) {
            dirF = CndFileUtils.normalizeFile(dirF);
        }
        String projectName = (String) wiz.getProperty(WizardConstants.PROPERTY_NAME);
        String makefileName = (String) wiz.getProperty(WizardConstants.PROPERTY_GENERATED_MAKEFILE_NAME);
        if (fullRemote) {
            getSelectHostWizardProvider().apply();
        }
        if (isSimple()) {
            IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                resultSet.addAll(extension.createProject(new SelectModeDescriptorPanel.WizardDescriptorAdapter(getSelectModePanel().getWizardStorage())));
            }
        } else if (wizardtype == TYPE_MAKEFILE) { // thp
            IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                resultSet.addAll(extension.createProject(wiz));
            }
        } else if (wizardtype == TYPE_BINARY) {
            IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                IteratorExtension.ProjectKind kind = (ProjectKind) wiz.getProperty(WizardConstants.PROPERTY_DEPENDENCY_KIND);
                if (kind == null) {
                    kind = IteratorExtension.ProjectKind.IncludeDependencies;
                }
                extension.discoverProject(wiz.getProperties(), null, kind);
                //resultSet.addAll(extension.createProject(wiz));
            }
        } else if (wizardtype == TYPE_APPLICATION || wizardtype == TYPE_DYNAMIC_LIB || wizardtype == TYPE_STATIC_LIB || wizardtype == TYPE_QT_APPLICATION || wizardtype == TYPE_QT_DYNAMIC_LIB || wizardtype == TYPE_QT_STATIC_LIB || wizardtype == TYPE_DB_APPLICATION) {
            int conftype = -1;
            if (wizardtype == TYPE_APPLICATION) {
                conftype = MakeConfiguration.TYPE_APPLICATION;
            } else if (wizardtype == TYPE_DYNAMIC_LIB) {
                conftype = MakeConfiguration.TYPE_DYNAMIC_LIB;
            } else if (wizardtype == TYPE_STATIC_LIB) {
                conftype = MakeConfiguration.TYPE_STATIC_LIB;
            } else if (wizardtype == TYPE_QT_APPLICATION) {
                conftype = MakeConfiguration.TYPE_QT_APPLICATION;
            } else if (wizardtype == TYPE_QT_DYNAMIC_LIB) {
                conftype = MakeConfiguration.TYPE_QT_DYNAMIC_LIB;
            } else if (wizardtype == TYPE_QT_STATIC_LIB) {
                conftype = MakeConfiguration.TYPE_QT_STATIC_LIB;
            } else if (wizardtype == TYPE_DB_APPLICATION) {
                conftype = MakeConfiguration.TYPE_APPLICATION;
            }
            String mainFile = null;
            if (((Boolean) wiz.getProperty("createMainFile")).booleanValue()) { // NOI18N
                String fname = (String) wiz.getProperty("mainFileName"); // NOI18N
                String template = (String) wiz.getProperty("mainFileTemplate"); // NOI18N
                mainFile = fname + "|" + template; // NOI18N
            }
            MakeConfiguration debug = new MakeConfiguration(dirF.getPath(), "Debug", conftype, hostUID, toolchain, defaultToolchain); // NOI18N
            debug.getCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getCCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getFortranCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getAssemblerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getQmakeConfiguration().getBuildMode().setValue(QmakeConfiguration.DEBUG_MODE);
            if (wizardtype == TYPE_DB_APPLICATION) {
                LinkerConfiguration linkerConfiguration = debug.getLinkerConfiguration();
                LibrariesConfiguration librariesConfiguration = linkerConfiguration.getLibrariesConfiguration();
                librariesConfiguration.add(new LibraryItem.LibItem("clntsh")); // NOI18N
                librariesConfiguration.add(new LibraryItem.LibItem("nnz11")); // NOI18N
                linkerConfiguration.setLibrariesConfiguration(librariesConfiguration);
                debug.setLinkerConfiguration(linkerConfiguration);
            }
            MakeConfiguration release = new MakeConfiguration(dirF.getPath(), "Release", conftype, hostUID, toolchain, defaultToolchain); // NOI18N
            release.getCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getCCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getFortranCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getAssemblerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getQmakeConfiguration().getBuildMode().setValue(QmakeConfiguration.RELEASE_MODE);
            if (wizardtype == TYPE_DB_APPLICATION) {
                LinkerConfiguration linkerConfiguration = release.getLinkerConfiguration();
                LibrariesConfiguration librariesConfiguration = linkerConfiguration.getLibrariesConfiguration();
                librariesConfiguration.add(new LibraryItem.LibItem("clntsh")); // NOI18N
                librariesConfiguration.add(new LibraryItem.LibItem("nnz11")); // NOI18N
                linkerConfiguration.setLibrariesConfiguration(librariesConfiguration);
                release.setLinkerConfiguration(linkerConfiguration);
            }
            MakeConfiguration[] confs = new MakeConfiguration[]{debug, release};
            ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(projectName, dirF);
            prjParams.setMakefileName(makefileName);
            prjParams.setConfigurations(confs);
            prjParams.setMainFile(mainFile);
            prjParams.setFullRemote(fullRemote);
            prjParams.setHostUID(hostUID);

            if (wizardtype == TYPE_DB_APPLICATION) {
                Object connection = wiz.getProperties().get("connectionName"); // NOI18N
                if(connection instanceof String) {
                    prjParams.setDatabaseConnection((String)connection);
                }
            }
            prjParams.setTemplateParams(new HashMap<String, Object>(wiz.getProperties()));
            
            MakeProjectGenerator.createProject(prjParams);
            ConfigurationDescriptorProvider.recordCreatedProjectMetrics(confs);
            FileObject dir = CndFileUtils.toFileObject(dirF);
            resultSet.add(dir);
        }
        return resultSet;
    }
    private transient int index;
    private transient List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    private transient WizardDescriptor wiz;

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        wiz.putProperty(WizardConstants.PROPERTY_FULL_REMOTE, Boolean.valueOf(fullRemote));
        index = 0;
        setupPanelsAndStepsIfNeed();
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(WizardConstants.PROPERTY_PROJECT_FOLDER, null);
        this.wiz.putProperty(WizardConstants.PROPERTY_NAME, null);
        this.wiz.putProperty("mainClass", null); // NOI18N
        if (wizardtype == TYPE_MAKEFILE) {
            this.wiz.putProperty("sourceRoot", null); // NOI18N
        }
        this.wiz = null;
        panels = null;
    }

    @Override
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(NewMakeProjectWizardIterator.class, "LAB_IteratorName"), // NOI18N
                new Object[]{Integer.valueOf(index + 1), Integer.valueOf(panels.size())});
    }

    private boolean isSimple() {
        return wizardtype == TYPE_MAKEFILE && wiz != null && Boolean.TRUE.equals(wiz.getProperty(WizardConstants.PROPERTY_SIMPLE_MODE));
    }

    @Override
    public boolean hasNext() {
        setupPanelsAndStepsIfNeed();
        boolean result = index < panels.size() - 1;
        LOGGER.log(Level.FINE, "hasNext()=={0} (index=={1}, panels.length=={2})", new Object[]{result, index, panels.size()});
        return result;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) { // will call setupPanelsAndStepsIfNeed();
            throw new NoSuchElementException();
        }
        if (index == lastNewHostPanel && fullRemote) {
            getSelectHostWizardProvider().apply();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        setupPanelsAndStepsIfNeed();
        return panels.get(index);
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void fireStateChanged() {
        ChangeEvent event = new ChangeEvent(this);
        ChangeListener[] listenersCopy;
        synchronized (listeners) {
            listenersCopy = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        for (ChangeListener listener : listenersCopy) {
            listener.stateChanged(event);
        }
    }

    interface Name {

        public String getName();
    }
    /** Look up i18n strings here */
    private static ResourceBundle bundle;

    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(NewMakeProjectWizardIterator.class);
        }
        return bundle.getString(s);
    }
}
