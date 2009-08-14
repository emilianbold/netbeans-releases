/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.project;

import org.netbeans.modules.php.project.util.CopySupport;
import org.netbeans.modules.php.project.ui.logicalview.PhpLogicalViewProvider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.api.PhpSeleniumProvider;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.php.project.classpath.IncludePathClassPathProvider;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.actions.support.ConfigAction;
import org.netbeans.modules.php.project.ui.codecoverage.PhpCoverageProvider;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.IgnorePathSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.util.PhpUnit;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleIgnoredFilesExtender;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.FilterPropertyProvider;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * @author ads, Tomas Mysik
 */
@AntBasedProjectRegistration(
    type=PhpProjectType.TYPE,
    iconResource="org/netbeans/modules/php/project/ui/resources/phpProject.png",
    sharedNamespace=PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE,
    privateNamespace=PhpProjectType.PRIVATE_CONFIGURATION_NAMESPACE
)
public class PhpProject implements Project {

    public static final String USG_LOGGER_NAME = "org.netbeans.ui.metrics.php"; //NOI18N
    private static final Icon PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/php/project/ui/resources/phpProject.png", false); // NOI18N

    final AntProjectHelper helper;
    final UpdateHelper updateHelper;
    private final ReferenceHelper refHelper;
    private final PropertyEvaluator eval;
    private final Lookup lookup;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private final SourceRoots seleniumRoots;

    // all next properties are guarded by PhpProject.this lock as well so it could be possible to break this lock to individual locks
    // #165136
    // @GuardedBy(ProjectManager.mutex())
    volatile FileObject sourcesDirectory;
    // @GuardedBy(ProjectManager.mutex())
    volatile FileObject testsDirectory;
    // @GuardedBy(ProjectManager.mutex())
    volatile FileObject seleniumDirectory;

    // @GuardedBy(ProjectManager.mutex())
    volatile Set<BasePathSupport.Item> ignoredFolders;
    final Object ignoredFoldersLock = new Object();
    final ChangeSupport ignoredFoldersChangeSupport = new ChangeSupport(this);
    private final PropertyChangeListener ignoredFoldersListener = new IgnoredFoldersListener();

    public PhpProject(AntProjectHelper helper) {
        assert helper != null;

        this.helper = helper;
        updateHelper = new UpdateHelper(UpdateImplementation.NULL, helper);
        AuxiliaryConfiguration configuration = helper.createAuxiliaryConfiguration();
        eval = createEvaluator();
        refHelper = new ReferenceHelper(helper, configuration, getEvaluator());
        sourceRoots = SourceRoots.create(updateHelper, eval, refHelper, SourceRoots.Type.SOURCES);
        testRoots = SourceRoots.create(updateHelper, eval, refHelper, SourceRoots.Type.TESTS);
        seleniumRoots = SourceRoots.create(updateHelper, eval, refHelper, SourceRoots.Type.SELENIUM);
        lookup = createLookup(configuration);

        addWeakPropertyEvaluatorListener(ignoredFoldersListener);
    }

    public Lookup getLookup() {
        return lookup;
    }

    PropertyEvaluator getEvaluator() {
        return eval;
    }

    void addWeakPropertyEvaluatorListener(PropertyChangeListener listener) {
        eval.addPropertyChangeListener(WeakListeners.propertyChange(listener, eval));
    }

    void addWeakIgnoredFilesListener(ChangeListener listener) {
        ignoredFoldersChangeSupport.addChangeListener(WeakListeners.change(listener, ignoredFoldersChangeSupport));

        VisibilityQuery visibilityQuery = VisibilityQuery.getDefault();
        visibilityQuery.addChangeListener(WeakListeners.change(listener, visibilityQuery));
    }

    private PropertyEvaluator createEvaluator() {
        // It is currently safe to not use the UpdateHelper for PropertyEvaluator; UH.getProperties() delegates to APH
        // Adapted from APH.getStandardPropertyEvaluator (delegates to ProjectProperties):
        PropertyEvaluator baseEval1 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(PhpConfigurationProvider.CONFIG_PROPS_PATH));
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        return PropertyUtils.sequentialPropertyEvaluator(
                helper.getStockPropertyPreprovider(),
                helper.getPropertyProvider(PhpConfigurationProvider.CONFIG_PROPS_PATH),
                new ConfigPropertyProvider(baseEval1, "nbproject/private/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                helper.getProjectLibrariesPropertyProvider(),
                PropertyUtils.userPropertiesProvider(baseEval2,
                    "user.properties.file", FileUtil.toFile(getProjectDirectory())), // NOI18N
                new ConfigPropertyProvider(baseEval1, "nbproject/configs", helper), // NOI18N
                helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
    }

    public FileObject getProjectDirectory() {
        return getHelper().getProjectDirectory();
    }

    public SourceRoots getSourceRoots() {
        return sourceRoots;
    }

    public SourceRoots getTestRoots() {
        return testRoots;
    }

    public SourceRoots getSeleniumRoots() {
        return seleniumRoots;
    }

    FileObject getSourcesDirectory() {
        if (sourcesDirectory == null) {
            ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
                public Void run() {
                    synchronized (PhpProject.this) {
                        if (sourcesDirectory == null) {
                            sourcesDirectory = resolveSourcesDirectory();
                        }
                    }
                    return null;
                }
            });
        }
        assert sourcesDirectory != null : "Sources directory cannot be null";
        return sourcesDirectory;
    }

    private FileObject resolveSourcesDirectory() {
        String srcDirProperty = eval.getProperty(PhpProjectProperties.SRC_DIR);
        // # 168390 - more logging
        if (srcDirProperty == null) {
            Logger.getLogger(PhpProject.class.getName()).info("Property for Sources must be defined [" + eval.getProperties() + "]");
        }
        assert srcDirProperty != null : "Property for Sources must be defined";
        FileObject srcDir = helper.resolveFileObject(srcDirProperty);
        if (srcDir != null) {
            return srcDir;
        }
        return restoreDirectory(PhpProjectProperties.SRC_DIR, "MSG_SourcesFolderRestored", "MSG_SourcesFolderTemporaryToProjectDirectory");
    }

    /**
     * @return tests directory or <code>null</code>
     */
    FileObject getTestsDirectory() {
        if (testsDirectory == null) {
            ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
                public Void run() {
                    synchronized (PhpProject.this) {
                        if (testsDirectory == null) {
                            testsDirectory = resolveTestsDirectory();
                        }
                    }
                    return null;
                }
            });
        }
        return testsDirectory;
    }

    void setTestsDirectory(FileObject testsDirectory) {
        assert this.testsDirectory == null : "Project test directory already set to " + this.testsDirectory;
        assert testsDirectory != null && testsDirectory.isValid();
        this.testsDirectory = testsDirectory;
    }

    private FileObject resolveTestsDirectory() {
        // similar to source directory
        String testsProperty = eval.getProperty(PhpProjectProperties.TEST_SRC_DIR);
        if (testsProperty == null) {
            // test directory not set yet
            return null;
        }
        FileObject testDir = helper.resolveFileObject(testsProperty);
        if (testDir != null) {
            return testDir;
        }
        return restoreDirectory(PhpProjectProperties.TEST_SRC_DIR, "MSG_TestsFolderRestored", "MSG_TestsFolderTemporaryToProjectDirectory");
    }

    /**
     * @return selenium tests directory or <code>null</code>
     */
    FileObject getSeleniumDirectory() {
        if (seleniumDirectory == null) {
            ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
                public Void run() {
                    synchronized (PhpProject.this) {
                        if (seleniumDirectory == null) {
                            seleniumDirectory = resolveSeleniumDirectory();
                        }
                    }
                    return null;
                }
            });
        }
        return seleniumDirectory;
    }

    void setSeleniumDirectory(FileObject seleniumDirectory) {
        assert this.seleniumDirectory == null : "Project selenium directory already set to " + this.seleniumDirectory;
        assert seleniumDirectory != null && seleniumDirectory.isValid();
        this.seleniumDirectory = seleniumDirectory;
    }

    private FileObject resolveSeleniumDirectory() {
        // similar to source directory
        String testsProperty = eval.getProperty(PhpProjectProperties.SELENIUM_SRC_DIR);
        if (testsProperty == null) {
            // test directory not set yet
            return null;
        }
        FileObject testDir = helper.resolveFileObject(testsProperty);
        if (testDir != null) {
            return testDir;
        }
        return restoreDirectory(PhpProjectProperties.SELENIUM_SRC_DIR, "MSG_SeleniumFolderRestored", "MSG_SeleniumFolderTemporaryToProjectDirectory");
    }

    private FileObject restoreDirectory(String propertyName, String infoMessageKey, String errorMessageKey) {
        // #144371 - source folder probably deleted => so:
        //  1. try to restore it - if it fails, then
        //  2. just return the project directory & warn user about impossibility of creating src dir
        String projectName = getName();
        File dir = FileUtil.normalizeFile(new File(helper.resolvePath(eval.getProperty(propertyName))));
        if (dir.mkdirs()) {
            // original sources restored
            informUser(projectName, NbBundle.getMessage(PhpProject.class, infoMessageKey, dir.getAbsolutePath()), NotifyDescriptor.INFORMATION_MESSAGE);
            return FileUtil.toFileObject(dir);
        }
        // temporary set sources to project directory, do not store it anywhere
        informUser(projectName, NbBundle.getMessage(PhpProject.class, errorMessageKey, dir.getAbsolutePath()), NotifyDescriptor.ERROR_MESSAGE);
        return helper.getProjectDirectory();
    }

    private void informUser(String title, String message, int type) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                message,
                title,
                NotifyDescriptor.DEFAULT_OPTION,
                type,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION));
    }

    public PhpModule getPhpModule() {
        PhpModule phpModule = getLookup().lookup(PhpModule.class);
        assert phpModule != null;
        return phpModule;
    }

    boolean isVisible(File file) {
        if (getIgnoredFiles().contains(file)) {
            return false;
        }
        return VisibilityQuery.getDefault().isVisible(file);
    }

    boolean isVisible(FileObject file) {
        return isVisible(FileUtil.toFile(file));
    }

    public Set<File> getIgnoredFiles() {
        Set<File> ignored = new HashSet<File>();
        putIgnoredProjectFiles(ignored);
        putIgnoredFrameworkFiles(ignored);
        return ignored;
    }

    private void putIgnoredProjectFiles(Set<File> ignored) {
        if (ignoredFolders == null) {
            ProjectManager.mutex().readAccess(new Mutex.Action<Void>() {
                public Void run() {
                    synchronized (ignoredFoldersLock) {
                        if (ignoredFolders == null) {
                            ignoredFolders = resolveIgnoredFolders();
                        }
                    }
                    return null;
                }
            });
        }
        assert ignoredFolders != null : "Ignored folders cannot be null";

        File projectDir = FileUtil.toFile(getProjectDirectory());
        for (BasePathSupport.Item item : ignoredFolders) {
            if (item.isBroken()) {
                continue;
            }
            File file = new File(item.getFilePath());
            if (!file.isAbsolute()) {
                file = PropertyUtils.resolveFile(projectDir, item.getFilePath());
            }
            ignored.add(file);
        }
    }

    // XXX should somehow listen on newly added frameworks to project
    // add set of _classes_ of framework providers and check them every time while calling ProjectPropertiesSupport.getFrameworks()
    private void putIgnoredFrameworkFiles(Set<File> ignored) {
        PhpModule phpModule = getPhpModule();
        for (PhpFrameworkProvider provider : ProjectPropertiesSupport.getFrameworks(this)) {
            PhpModuleIgnoredFilesExtender ignoredFilesExtender = provider.getIgnoredFilesExtender(phpModule);
            if (ignoredFilesExtender == null) {
                continue;
            }
            for (File file : ignoredFilesExtender.getIgnoredFiles()) {
                assert file != null : "Ignored file = null found in " + provider.getName();
                assert file.isAbsolute() : "Not absolute file found in " + provider.getName();

                ignored.add(file);
            }
        }
    }

    private Set<BasePathSupport.Item> resolveIgnoredFolders() {
        IgnorePathSupport ignorePathSupport = new IgnorePathSupport(eval, refHelper, helper);
        Set<BasePathSupport.Item> ignored = new HashSet<BasePathSupport.Item>();
        EditableProperties properties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Iterator<BasePathSupport.Item> itemsIterator = ignorePathSupport.itemsIterator(properties.getProperty(PhpProjectProperties.IGNORE_PATH));
        while (itemsIterator.hasNext()) {
            ignored.add(itemsIterator.next());
        }
        return ignored;
    }

    /*
     * Copied from MakeProject.
     */
    public String getName() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
            public String run() {
                Element data = getHelper().getPrimaryConfigurationData(true);
                NodeList nl = data.getElementsByTagNameNS(PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1
                            && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "???"; // NOI18N
            }
        });
    }

    /*
     * Copied from MakeProject.
     */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                Element data = getHelper().getPrimaryConfigurationData(true);
                NodeList nl = data.getElementsByTagNameNS(PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(
                            PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                getHelper().putPrimaryConfigurationData(data, true);
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(200);
        buffer.append(getClass().getName());
        buffer.append(" [ project directory: ");
        buffer.append(getProjectDirectory());
        buffer.append(", source directory: ");
        buffer.append(sourcesDirectory);
        buffer.append(" ]");
        return buffer.toString();
    }

    public AntProjectHelper getHelper() {
        return helper;
    }

    CopySupport getCopySupport() {
        return getLookup().lookup(CopySupport.class);
    }

    private Lookup createLookup(AuxiliaryConfiguration configuration) {
        PhpProjectEncodingQueryImpl phpProjectEncodingQueryImpl = new PhpProjectEncodingQueryImpl(getEvaluator());
        return Lookups.fixed(new Object[] {
                this,
                CopySupport.getInstance(),
                new SeleniumProvider(),
                new PhpCoverageProvider(this),
                new Info(),
                configuration,
                new PhpOpenedHook(),
                new PhpProjectXmlSavedHook(),
                new PhpActionProvider(this),
                new PhpConfigurationProvider(this),
                new PhpModuleImpl(this),
                helper.createCacheDirectoryProvider(),
                helper.createAuxiliaryProperties(),
                new ClassPathProviderImpl(this, getSourceRoots(), getTestRoots(), getSeleniumRoots()),
                new PhpLogicalViewProvider(this),
                new CustomizerProviderImpl(this),
                new PhpSharabilityQuery(helper, getEvaluator(), getSourceRoots(), getTestRoots(), getSeleniumRoots()),
                new PhpProjectOperations(this) ,
                phpProjectEncodingQueryImpl,
                new TemplateAttributesProviderImpl(getHelper(), phpProjectEncodingQueryImpl),
                new PhpTemplates(),
                new PhpSources(this, getHelper(), getEvaluator(), getSourceRoots(), getTestRoots(), getSeleniumRoots()),
                getHelper(),
                getEvaluator()
                // ?? getRefHelper()
        });
    }

    public ReferenceHelper getRefHelper() {
        return refHelper;
    }

    private final class Info implements ProjectInformation {
        private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public void addPropertyChangeListener(PropertyChangeListener  listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        public String getDisplayName() {
            return PhpProject.this.getName();
        }

        public Icon getIcon() {
            return PROJECT_ICON;
        }

        public String getName() {
            return PropertyUtils.getUsablePropertyName(getDisplayName());
        }

        public Project getProject() {
            return PhpProject.this;
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }

        void firePropertyChange(String prop) {
            propertyChangeSupport.firePropertyChange(prop , null, null);
        }
    }

    private final class PhpOpenedHook extends ProjectOpenedHook {
        protected void projectOpened() {
            // #165494 - moved from projectClosed() to projectOpened()
            // clear references to ensure that all the dirs are read again
            sourcesDirectory = null;
            testsDirectory = null;
            seleniumDirectory = null;
            ignoredFolders = null;

            // #139159 - we need to hold sources FO to prevent gc
            getSourcesDirectory();
            // do it in a background thread
            getIgnoredFiles();

            ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
            ClassPath[] bootClassPaths = cpProvider.getProjectClassPaths(PhpSourcePath.BOOT_CP);
            GlobalPathRegistry.getDefault().register(PhpSourcePath.BOOT_CP, bootClassPaths);
            GlobalPathRegistry.getDefault().register(PhpSourcePath.SOURCE_CP, cpProvider.getProjectClassPaths(PhpSourcePath.SOURCE_CP));
            for (ClassPath classPath : bootClassPaths) {
                IncludePathClassPathProvider.addProjectIncludePath(classPath);
            }

            // ensure that code coverage is initialized in case it's enabled...
            PhpCoverageProvider coverageProvider = getLookup().lookup(PhpCoverageProvider.class);
            if (coverageProvider.isEnabled()) {
                PhpCoverageProvider.notifyProjectOpened(PhpProject.this);
            }

            final CopySupport copySupport = getCopySupport();
            if (copySupport != null) {
                copySupport.projectOpened(PhpProject.this);
            }

            // #164073 - for the first time, let's do it not in AWT thread
            PhpUnit phpUnit = CommandUtils.getPhpUnit(false);
            if (phpUnit != null) {
                phpUnit.supportedVersionFound();
            }
        }

        protected void projectClosed() {
            ClassPathProviderImpl cpProvider = lookup.lookup(ClassPathProviderImpl.class);
            GlobalPathRegistry.getDefault().unregister(PhpSourcePath.BOOT_CP, cpProvider.getProjectClassPaths(PhpSourcePath.BOOT_CP));
            GlobalPathRegistry.getDefault().unregister(PhpSourcePath.SOURCE_CP, cpProvider.getProjectClassPaths(PhpSourcePath.SOURCE_CP));

            final CopySupport copySupport = getCopySupport();
            if (copySupport != null) {
                copySupport.projectClosed(PhpProject.this);
            }

            try {
                ProjectManager.getDefault().saveProject(PhpProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    private static final class ConfigPropertyProvider extends FilterPropertyProvider implements PropertyChangeListener {
        private final PropertyEvaluator baseEval;
        private final String prefix;
        private final AntProjectHelper helper;
        public ConfigPropertyProvider(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            super(computeDelegate(baseEval, prefix, helper));
            this.baseEval = baseEval;
            this.prefix = prefix;
            this.helper = helper;
            baseEval.addPropertyChangeListener(this);
        }
        public void propertyChange(PropertyChangeEvent ev) {
            if (PhpConfigurationProvider.PROP_CONFIG.equals(ev.getPropertyName())) {
                setDelegate(computeDelegate(baseEval, prefix, helper));
            }
        }
        private static PropertyProvider computeDelegate(PropertyEvaluator baseEval, String prefix, AntProjectHelper helper) {
            String config = baseEval.getProperty(PhpConfigurationProvider.PROP_CONFIG);
            if (config != null) {
                return helper.getPropertyProvider(prefix + "/" + config + ".properties"); // NOI18N
            }
            return PropertyUtils.fixedPropertyProvider(Collections.<String, String>emptyMap());
        }
    }

    public final class PhpProjectXmlSavedHook extends ProjectXmlSavedHook {

        protected void projectXmlSaved() throws IOException {
            Info info = getLookup().lookup(Info.class);
            assert info != null;
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }

    private final class SeleniumProvider implements PhpSeleniumProvider {
        public FileObject getTestDirectory(boolean showCustomizer) {
            return ProjectPropertiesSupport.getSeleniumDirectory(PhpProject.this, showCustomizer);
        }

        public void runAllTests() {
            ConfigAction.get(ConfigAction.Type.SELENIUM, PhpProject.this).runProject();
        }
    }

    private final class IgnoredFoldersListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (PhpProjectProperties.IGNORE_PATH.equals(evt.getPropertyName())) {
                ignoredFolders = null;
                ignoredFoldersChangeSupport.fireChange();
            }
        }
    }
}
