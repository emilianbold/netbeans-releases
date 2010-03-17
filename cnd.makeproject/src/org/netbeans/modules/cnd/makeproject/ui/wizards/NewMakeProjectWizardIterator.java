/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cnd.makeproject.MakeProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.QmakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Make project.
 */
public class NewMakeProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    private static final long serialVersionUID = 1L;
    public static final String APPLICATION_PROJECT_NAME = "CppApplication"; // NOI18N
    public static final String DYNAMICLIBRARY_PROJECT_NAME = "CppDynamicLibrary";  // NOI18N
    public static final String STATICLIBRARY_PROJECT_NAME = "CppStaticLibrary"; // NOI18N
    public static final String MAKEFILEPROJECT_PROJECT_NAME = "MakefileProject"; // NOI18N
    public static final String QTAPPLICATION_PROJECT_NAME = "QtApplication"; // NOI18N
    public static final String QTDYNAMICLIBRARY_PROJECT_NAME = "QtDynamicLibrary"; // NOI18N
    public static final String QTSTATICLIBRARY_PROJECT_NAME = "QtStaticLibrary"; // NOI18N
    static final String PROP_NAME_INDEX = "nameIndex"; // NOI18N
    // Wizard types
    public static final int TYPE_MAKEFILE = 0;
    public static final int TYPE_APPLICATION = 1;
    public static final int TYPE_DYNAMIC_LIB = 2;
    public static final int TYPE_STATIC_LIB = 3;
    public static final int TYPE_QT_APPLICATION = 4;
    public static final int TYPE_QT_DYNAMIC_LIB = 5;
    public static final int TYPE_QT_STATIC_LIB = 6;
    private int wizardtype;
    private String name;
    private String wizardTitle;
    private String wizardACSD;

    private NewMakeProjectWizardIterator(int wizardtype, String name, String wizardTitle, String wizardACSD) {
        this.wizardtype = wizardtype;
        this.name = name;
        this.wizardTitle = wizardTitle;
        this.wizardACSD = wizardACSD;
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

    public static NewMakeProjectWizardIterator makefile() {
        String name = MAKEFILEPROJECT_PROJECT_NAME; //getString("NativeMakefileName"); // NOI18N
        String wizardTitle = getString("Templates/Project/Native/makefile.xml"); // NOI18N
        String wizardACSD = getString("NativeMakefileNameACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_MAKEFILE, name, wizardTitle, wizardACSD);
    }

    private WizardDescriptor.Panel[] createPanels(String name) {
        if (wizardtype == TYPE_APPLICATION || wizardtype == TYPE_DYNAMIC_LIB || wizardtype == TYPE_STATIC_LIB || wizardtype == TYPE_QT_APPLICATION || wizardtype == TYPE_QT_DYNAMIC_LIB || wizardtype == TYPE_QT_STATIC_LIB) {
            return new WizardDescriptor.Panel[]{
                        new PanelConfigureProject(name, wizardtype, wizardTitle, wizardACSD, true)
                    };
        } else if (wizardtype == TYPE_MAKEFILE) {
            return new WizardDescriptor.Panel[]{
                        new SelectModeDescriptorPanel(),
                        new MakefileOrConfigureDescriptorPanel(),
                        new BuildActionsDescriptorPanel(),
                        new SourceFoldersDescriptorPanel(),
                        new ParserConfigurationDescriptorPanel(),
                        new PanelConfigureProject(name, wizardtype, wizardTitle, wizardACSD, false),};
        }
        return null; // FIXUP
    }

    private String[] createSteps(WizardDescriptor.Panel[] panels) {
        String[] steps = new String[panels.length];
        for (int i = 0; i < panels.length; i++) {
            if (panels[i] instanceof Name) {
                steps[i] = ((Name) panels[i]).getName();
            } else {
                steps[i] = panels[i].getComponent().getName();
            }
        }
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
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
    public Set instantiate(ProgressHandle handle) throws IOException {
        try {
            handle.start();
            return instantiate();
        } finally {
            handle.finish();
        }
    }


    @Override
    public Set<FileObject> instantiate() throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        File dirF = (File) wiz.getProperty("projdir"); // NOI18N
        if (dirF != null) {
            dirF = CndFileUtils.normalizeFile(dirF);
        }
        String projectName = (String) wiz.getProperty("name"); // NOI18N
        String makefileName = (String) wiz.getProperty("makefilename"); // NOI18N
        if (isSimple()) {
            IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                SelectModeDescriptorPanel importPanel = (SelectModeDescriptorPanel) simplePanels[0];
                resultSet.addAll(extension.createProject(new SelectModeDescriptorPanel.WizardDescriptorAdapter(importPanel.getWizardStorage())));
            }
        } else if (wizardtype == TYPE_MAKEFILE) { // thp
            IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                resultSet.addAll(extension.createProject(wiz));
            }
        } else if (wizardtype == TYPE_APPLICATION || wizardtype == TYPE_DYNAMIC_LIB || wizardtype == TYPE_STATIC_LIB || wizardtype == TYPE_QT_APPLICATION || wizardtype == TYPE_QT_DYNAMIC_LIB || wizardtype == TYPE_QT_STATIC_LIB) {
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
            }
            String mainFile = null;
            if (((Boolean) wiz.getProperty("createMainFile")).booleanValue()) { // NOI18N
                String fname = (String) wiz.getProperty("mainFileName"); // NOI18N
                String template = (String) wiz.getProperty("mainFileTemplate"); // NOI18N
                mainFile = fname + "|" + template; // NOI18N
            }
            MakeConfiguration debug = new MakeConfiguration(dirF.getPath(), "Debug", conftype); // NOI18N
            debug.getCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getCCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getFortranCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getQmakeConfiguration().getBuildMode().setValue(QmakeConfiguration.DEBUG_MODE);
            MakeConfiguration release = new MakeConfiguration(dirF.getPath(), "Release", conftype); // NOI18N
            release.getCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getCCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getFortranCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getQmakeConfiguration().getBuildMode().setValue(QmakeConfiguration.RELEASE_MODE);
            MakeConfiguration[] confs = new MakeConfiguration[]{debug, release};
            MakeProjectGenerator.createProject(dirF, projectName, makefileName, confs, null, null, null, null, mainFile);
            ConfigurationDescriptorProvider.recordCreatedProjectMetrics(confs);
            FileObject dir = FileUtil.toFileObject(dirF);
            resultSet.add(dir);
        }
        return resultSet;
    }
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor.Panel[] simplePanels;
    private transient WizardDescriptor wiz;

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels(name.replaceAll(" ", "")); // NOI18N
        // Make sure list of steps is accurate.
        String[] steps = createSteps(panels);
        if (wizardtype == TYPE_MAKEFILE) {
            simplePanels = new WizardDescriptor.Panel[]{
                        panels[0]
                    //,new ImportProjectDescriptorPanel()
                    };
            steps = createSteps(simplePanels);
            String[] advanced = new String[]{steps[0], "..."}; // NOI18N
            Component c = panels[0].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, advanced);
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir", null); // NOI18N
        this.wiz.putProperty("name", null); // NOI18N
        this.wiz.putProperty("mainClass", null); // NOI18N
        if (wizardtype == TYPE_MAKEFILE) {
            this.wiz.putProperty("sourceRoot", null); // NOI18N
        }
        this.wiz = null;
        panels = null;
        simplePanels = null;
    }

    @Override
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(NewMakeProjectWizardIterator.class, "LAB_IteratorName"), // NOI18N
                new Object[]{Integer.valueOf(index + 1), Integer.valueOf(panels.length)});
    }

    private boolean isSimple() {
        return wizardtype == TYPE_MAKEFILE && Boolean.TRUE.equals(wiz.getProperty("simpleMode")); // NOI18N
    }

    @Override
    public boolean hasNext() {
        if (isSimple()) {
            return index < simplePanels.length - 1;
        } else {
            return index < panels.length - 1;
        }
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
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
    public WizardDescriptor.Panel current() {
        if (isSimple()) {
            return simplePanels[index];
        } else {
            return panels[index];
        }
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
