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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.modules.javacard.GuiUtils;
import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.api.Card;
import org.netbeans.modules.javacard.api.JavacardPlatform;
import org.netbeans.modules.javacard.api.ProjectKind;
import static org.netbeans.modules.javacard.constants.JCConstants.RUNTIME_DESCRIPTOR;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.platform.BrokenJavacardPlatform;
import org.netbeans.modules.javacard.project.deps.ArtifactKind;
import org.netbeans.modules.javacard.project.deps.Dependencies;
import org.netbeans.modules.javacard.project.deps.DependenciesProvider;
import org.netbeans.modules.javacard.project.deps.DependenciesResolver;
import org.netbeans.modules.javacard.project.deps.Dependency;
import org.netbeans.modules.javacard.project.deps.Path;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.netbeans.modules.javacard.project.deps.ResolvedDependency;
import org.netbeans.modules.javacard.project.libraries.LibrariesManager;
import org.netbeans.modules.javacard.project.ui.BadPlatformOrDevicePanel;
import org.netbeans.modules.javacard.project.ui.ProblemPanel;
import org.netbeans.modules.javacard.project.ui.UnsupportedEncodingDialog;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.support.ExtraSourceJavadocSupport;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Cancellable;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Anki R Nelaturu, Tim Boudreau
 */
public class JCProject implements Project, AntProjectListener, PropertyChangeListener, ChangeListener {

    private final ProjectKind kind;
    private final AuxiliaryConfiguration aux;
    protected final PropertyEvaluator eval;
    private final GeneratedFilesHelper genFilesHelper;
    private final AntProjectHelper antHelper;
    private final Lookup lookup;
    private final ClassPathProviderImpl cpProvider;
    private ClassPath bootPath;
    private ClassPath libPath;
    private ClassPath compileTimePath;
    private final UpdateHelper updateHelper;
    private final ReferenceHelper refHelper;
    private SourceRoots sourceRoots;
    private final PlatformPropertyProvider platformProperties;
    private final DevicePropertyProvider deviceProperties;
    private final Object rootsLock = new Object();
    private final PlatformModificationListener pml = new PlatformModificationListener();
    private final ChangeSupport supp = new ChangeSupport(this);
    private final SubprojectProviderImpl subprojects = new SubprojectProviderImpl();
    private volatile Boolean cachedBadProjectOrCard;
    private ClassPath[] registeredSourceCP;
    private ClassPath[] registeredCompileCP;
    private ClassPath[] registeredBootCP;
    private final LibrariesManager libMgr;
    //package private for unit tests
    final ProjectOpenedHookImpl hook = new ProjectOpenedHookImpl();

    protected JCProject(ProjectKind kind, AntProjectHelper antHelper) throws IOException {
        Parameters.notNull("kind", kind); //NOI18N
        platformProperties = new PlatformPropertyProvider(antHelper);
        deviceProperties = new DevicePropertyProvider(antHelper);
        this.kind = kind;
        this.antHelper = antHelper;
        eval = createEvaluator();
        eval.addPropertyChangeListener(this);
        genFilesHelper = new GeneratedFilesHelper(antHelper);
        aux = antHelper.createAuxiliaryConfiguration();
        cpProvider = new ClassPathProviderImpl();
        refHelper = new ReferenceHelper(antHelper, aux, eval);
        Updater updateProject = new Updater(this, antHelper, aux);
        this.updateHelper = new UpdateHelper(updateProject, antHelper);
        libMgr = new LibrariesManager(this);
        libMgr.addChangeListener(this);
        lookup = createLookup();
        for (int v = 3; v < 10; v++) {
            if (aux.getConfigurationFragment("data", //NOI18N
                    "http://www.netbeans.org/ns/j2se-project/" + v, true) != null) { // NOI18N
                throw Exceptions.attachLocalizedMessage(new IOException("too new"), // NOI18N
                        NbBundle.getMessage(JCProject.class,
                        "JCProject.too_new", //NOI18N
                        FileUtil.getFileDisplayName(
                        antHelper.getProjectDirectory())));
            }
        }
    }

    public ReferenceHelper refHelper() {
        return refHelper;
    }

    private final Lookup createLookup() {
        FileEncodingQueryImplementation encodingQuery =
                QuerySupport.createFileEncodingQuery(evaluator(),
                ProjectPropertyNames.PROJECT_PROP_SOURCE_ENCODING);
        Lookup lkp = Lookups.fixed(new Object[]{
                    JCProject.this,
                    kind(),
                    subprojects,
                    updateHelper,
                    aux,
                    libMgr,
                    new Info(),
                    new JCLogicalViewProvider(this),
                    new JCProjectSources(this, antHelper, eval, getRoots()),
                    cpProvider,
                    new ProjectXmlSavedHookImpl(),
                    UILookupMergerSupport.createProjectOpenHookMerger(hook),
                    new RecommendedTempatesImpl(),
                    new JCProjectActionProvider(this),
                    new JCProjectOperations(this),
                    new JCCustomizerProvider(this, eval, genFilesHelper, antHelper),
                    refHelper.createSubprojectProvider(),
                    encodingQuery,
                    UILookupMergerSupport.createPrivilegedTemplatesMerger(),
                    UILookupMergerSupport.createRecommendedTemplatesMerger(),
                    LookupProviderSupport.createSourcesMerger(),
                    //PENDING replace second getRoots() with null if
                    //http://www.netbeans.org/issues/show_bug.cgi?id=162270 gets
                    //fixed
                    QuerySupport.createCompiledSourceForBinaryQuery(antHelper, evaluator(), getRoots(), getRoots()),
                    QuerySupport.createTemplateAttributesProvider(antHelper, encodingQuery),
                    //PENDING replace second getRoots() with null if
                    //http://www.netbeans.org/issues/show_bug.cgi?id=162270 gets
                    //fixed
                    QuerySupport.createSharabilityQuery(antHelper, evaluator(), getRoots(), getRoots()),
                    QuerySupport.createJavadocForBinaryQuery(antHelper, evaluator()),
                    QuerySupport.createFileBuiltQuery(antHelper, eval, sourceRoots, sourceRoots),
                    ExtraSourceJavadocSupport.createExtraSourceQueryImplementation(this, antHelper, eval),
                    LookupMergerSupport.createSFBLookupMerger(),
                    ExtraSourceJavadocSupport.createExtraJavadocQueryImplementation(this, antHelper, eval),
                    LookupMergerSupport.createJFBLookupMerger(),
                    QuerySupport.createBinaryForSourceQueryImplementation(sourceRoots, sourceRoots, antHelper, this.eval), //Does not use APH to get/put properties/cfgdata
                    new AntArtifactProviderImpl(),
                    new DependenciesProviderImpl(),});
        return LookupProviderSupport.createCompositeLookup(new ProxyLookup(lkp),
                kind.getLookupMergerPath());
    }

    PropertyEvaluator createEvaluator() {
        PropertyProvider publicPropsEval = antHelper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        PropertyProvider privatePropsEval = antHelper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        PropertyProvider globals = PropertyUtils.globalPropertyProvider();
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                antHelper.getStockPropertyPreprovider(),
                antHelper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));

        PropertyEvaluator result = PropertyUtils.sequentialPropertyEvaluator(
                antHelper.getStockPropertyPreprovider(),
                globals,
                PropertyUtils.userPropertiesProvider(baseEval2,
                "user.properties.file", FileUtil.toFile(getProjectDirectory())), // NOI18N
                platformProperties,
                deviceProperties,
                privatePropsEval,
                publicPropsEval);
//        System.err.println("KEYSTORE PATH: " + result.getProperty(ProjectPropertyNames.PROJECT_PROP_KEYSTORE_PATH));
        return result;
//        return new WrapperPropertyEvaluator(result);
    }

    public ReferenceHelper getReferenceHelper() {
        return refHelper;
    }

    /**
     * Listen for changes in platform or device.  Listeners will be weakly
     * referenced
     * @param cl A change listener
     */
    public void addChangeListener(ChangeListener cl) {
        supp.addChangeListener(WeakListeners.change(cl, supp));
    }

    public JavacardPlatform getPlatform() {
        String platform = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
        if (platform != null) {
            JavacardPlatform result = Utils.findPlatformNamed(platform);
            return result == null ? new BrokenJavacardPlatform(platform) : result;
        }
        return null;
    }

    public Card getCard() {
        String platform = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
        String device = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE);
        if (platform != null && device != null) {
            DataObject result = Utils.findDeviceForPlatform(platform, device);
            return result == null ? null : result.getLookup().lookup(Card.class);
        }
        return null;
    }

    public SourceRoots getRoots() {
        synchronized (rootsLock) {
            if (sourceRoots == null) { //Local caching, no project metadata access
                sourceRoots = SourceRoots.create(updateHelper, eval, refHelper,
                        JCProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                        "source-roots", false, "src.{0}{1}.dir"); //NOI18N
            }

            return sourceRoots;
        }
    }

    final void displayNameMayBeChanged() {
        Info info = lookup.lookup(Info.class);
        info.checkDisplayName();
    }

    public final void setName(final String name) {
        ProjectRenamer renamer = new ProjectRenamer(name, antHelper, kind);
        renamer.doRename();
    }

    public boolean isBadPlatformOrCard() {
        Boolean val;
        synchronized (this) {
            val = cachedBadProjectOrCard;
        }
        if (val == null) {
            String platform = eval.getProperty(
                    ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
            String card = eval.getProperty(
                    ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE);
            val = isBadPlatformOrCard(platform, card);
            //Technically another thread could change the value of
            //cachedBadProjectOrCard while we were fetching properties, but not
            //going to worry about it
            synchronized (this) {
                cachedBadProjectOrCard = val;
            }
        }
        return val;
    }

    private boolean isBadPlatformOrCard(String platform, String card) {
        boolean badPlatform = platform == null || "".equals(platform);
        boolean badCard = card == null || "".equals(card);
        if (!badPlatform) {
            JavacardPlatform p = Utils.findPlatformNamed(platform);
            badPlatform = p == null || !p.isValid();
        }
        if (!badCard) {
            DataObject dob = Utils.findDeviceForPlatform(platform, card);
            Card device = dob == null ? null : dob.getLookup().lookup(Card.class);
            badCard = device == null || !device.isValid();
        }
        return badPlatform || badCard;
    }

    public final PropertyEvaluator evaluator() {
        return eval;
    }

    public final void propertyChange(final PropertyChangeEvent event) {
        if (event != null) {
            String propertyName = event.getPropertyName();
            if (ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM.equals(propertyName)) {
                updateGlobalClassPaths();
            }
            if (ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM.equals(propertyName) ||
                    ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE.equals(propertyName)) {
                synchronized (this) {
                    cachedBadProjectOrCard = null;
                }
                //Notify the node that it may need to update
                supp.fireChange();
            }
        } else {
            // Update all
            updateRuntimeDescriptor();
        }
    }

    public void stateChanged(ChangeEvent e) {
        //Called by LibraryManager if the library classpath changes
        updateGlobalClassPaths();
        subprojects.supp.fireChange();
    }

    void onDependenciesChanged() {
        updateGlobalClassPaths();
        //Get out of the way of ProjectManager.mutex() + Children.MUTEX
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                depsChanges.fireChange();
            }
        });
    }
    private final ChangeSupport depsChanges = new ChangeSupport(this);

    public void addDependencyChangeListener(ChangeListener l) {
        depsChanges.addChangeListener(l);
    }

    public void removeDependencyChangeListener(ChangeListener l) {
        depsChanges.removeChangeListener(l);
    }

    private void updateRuntimeDescriptor() {
        if (kind() != ProjectKind.WEB) {
            return;
        }
        ProjectManager.mutex().postWriteRequest(new Runnable() {

            public void run() {
                InputStream in = null;
                OutputStream out = null;

                try {
                    String runtimeDescriptorPath = eval.getProperty(RUNTIME_DESCRIPTOR);
                    if (runtimeDescriptorPath == null) {
                        return;
                    }

                    FileObject rdFO =
                            FileUtil.createData(new File(runtimeDescriptorPath));

                    in = rdFO.getInputStream();
                    Manifest manifest = new Manifest();
                    try {
                        manifest.read(in);
                    } finally {
                        in.close();
                    }
                    if (kind() == ProjectKind.WEB) {
                        String newWebContextPath =
                                eval.getProperty(ProjectPropertyNames.PROJECT_PROP_WEB_CONTEXT_PATH);
                        String oldWebContextPath = manifest.getMainAttributes().putValue("Web-Context-Path", newWebContextPath);
                        // Write only if the values were changed
                        if (oldWebContextPath == null || !oldWebContextPath.equals(newWebContextPath)) {
                            out = rdFO.getOutputStream();
                            try {
                                manifest.write(out);
                            } finally {
                                out.close();
                            }
                        }
                    }
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        }

                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        }

                    }
                }
            }
        });
    }

    public final Lookup getLookup() {
        return lookup;
    }

    public final FileObject getProjectDirectory() {
        return antHelper.getProjectDirectory();
    }

    public final AntProjectHelper getAntProjectHelper() {
        return antHelper;
    }

    public final ProjectKind kind() {
        return kind;
    }

    public final void configurationXmlChanged(
            AntProjectEvent arg0) {
        Info info = getLookup().lookup(Info.class);
        info.checkDisplayName();
    }

    public final void propertiesChanged(AntProjectEvent event) {
        //do nothing
    }

    public ClassPath getSourceClassPath() {
        return ClassPathSupport.createClassPath(getRoots().getRoots());
    }

    private final ClassPath getLibClassPath() {
        synchronized (pml) {
            if (libPath == null) {
                try {
                    ResolvedDependencies deps = this.syncGetResolvedDependencies();
                    List <URL> urls = new ArrayList<URL>();
                    for (ResolvedDependency d : deps.all()) {
                        if (!d.isValid()) {
                            continue;
                        }
                        File f = d.resolveFile(ArtifactKind.ORIGIN);
                        if (d.getKind().isProjectDependency()) {
                            FileObject fo = FileUtil.toFileObject(f);
                            if (fo != null) {
                                Project p = FileOwnerQuery.getOwner(fo);
                                if (p != null) {
                                    URL url = p.getProjectDirectory().getURL();
                                    AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
                                    for (AntArtifact a : prov.getBuildArtifacts()) {
                                        if (JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(a.getType())) {
                                            URI[] uris = a.getArtifactLocations();
                                            for (URI u : uris) {
                                                //XXX either the docs are wrong, or our AntArtifactProvider impl is wrong
//                                                url = new URL (url.toString() + u.toString());
                                                System.err.println("URI: " + u);
                                                url = new URL(u.toString());
                                                if (FileUtil.isArchiveFile(url)) {
                                                    url = FileUtil.getArchiveRoot(url);
                                                    urls.add (url);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (f != null) {
                                URL url = f.toURI().toURL();
                                if (FileUtil.isArchiveFile(url)) {
                                    url = FileUtil.getArchiveRoot(url);
                                }
                                if (url != null) {
                                    urls.add (url);
                                }
                            }
                        }
                    }
                    libPath = ClassPathSupport.createClassPath(
                            urls.toArray(new URL[urls.size()]));
                    System.err.println("Class Path:");
                    for (URL url : urls) {
                        System.err.println("   " + url);
                    }
                } catch (SAXException ex) {
                    libPath = ClassPathSupport.createClassPath("");
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    libPath = ClassPathSupport.createClassPath("");
                    Exceptions.printStackTrace(ex);
                }
            }
            return libPath;
        }
    }

    private final ClassPath getBootClassPath() {
        synchronized (pml) {
            if (bootPath == null) {
                String platformName = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
                if (platformName != null) {
                    JavacardPlatform platform = Utils.findPlatformNamed(platformName);
                    if (platform != null && platform.isValid()) {
                        bootPath = platform.getBootstrapLibraries(kind);
                    } else {
                        return ClassPathSupport.createClassPath("");
                    }

                }
            }
            return bootPath;
        }
    }

    public static JCProject getOwnerProjectOf(FileObject obj) {
        Project p = FileOwnerQuery.getOwner(obj);
        return p == null ? null : p.getLookup().lookup(JCProject.class);
    }

    private final ClassPath getCompileTimeClassPath() {
        synchronized (pml) {
            if (compileTimePath == null) {
                compileTimePath = ClassPathSupport.createProxyClassPath(getBootClassPath(), getLibClassPath());
            }
            return compileTimePath;
        }
    }

    private final void registerGlobalPaths() {
        Utils.sfsFolderForRegisteredJavaPlatforms().addFileChangeListener(pml);
        synchronized (pml) {
            registeredBootCP = cpProvider.getProjectClassPaths(ClassPath.BOOT);
            if (registeredBootCP != null) {
                GlobalPathRegistry.getDefault().register(
                        ClassPath.BOOT, registeredBootCP);
            }

            registeredSourceCP = cpProvider.getProjectClassPaths(ClassPath.SOURCE);
            if (registeredSourceCP != null) {
                GlobalPathRegistry.getDefault().register(
                        ClassPath.SOURCE, registeredSourceCP);
            }

            registeredCompileCP =
                    cpProvider.getProjectClassPaths(ClassPath.COMPILE);
            if (registeredCompileCP != null) {
                GlobalPathRegistry.getDefault().register(
                        ClassPath.COMPILE, registeredCompileCP);
            }
        }
    }

    private final void unregisterGlobalPaths() {
        Utils.sfsFolderForRegisteredJavaPlatforms().removeFileChangeListener(pml);
        synchronized (pml) {
            if (registeredBootCP != null) {
                try {
                    GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT,
                            registeredBootCP);
                } catch (IllegalArgumentException e) {
                }// Was not registered, ignore
            }
            if (registeredSourceCP != null) {
                try {
                    GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE,
                            registeredSourceCP);
                } catch (IllegalArgumentException e) {
                }// Was not registered, ignore
            }
            if (registeredCompileCP != null) {
                try {
                    GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE,
                            registeredCompileCP);
                } catch (IllegalArgumentException e) {
                }// Was not registered, ignore
            }
        }
    }

    private final void updateGlobalClassPaths() {
        synchronized (pml) {
            unregisterGlobalPaths();
            bootPath =
                    null;
            libPath =
                    null;
            compileTimePath =
                    null;
            registerGlobalPaths();
        }
    }

    public void showSelectPlatformAndDeviceDialog() {
        if (BadPlatformOrDevicePanel.isShowBrokenPlatformDialog()) {
            FIX_PLATFORM_DIALOG_QUEUE.add(this);
        }
    }
    private static PlatformAndDeviceDialogQueue FIX_PLATFORM_DIALOG_QUEUE = new PlatformAndDeviceDialogQueue();

    private static class PlatformAndDeviceDialogQueue implements ActionListener {

        private final Set<Item> items = Collections.synchronizedSet(new HashSet<Item>());
        private Timer timer;
        boolean firstUse = true;

        private synchronized void initTimer() {
            if (timer == null) {
                timer = new Timer(firstUse ? 7000 : 200, this);
                timer.setRepeats(false);
                timer.start();
            }
        }

        void add(JCProject project) {
            Item nue = new Item(project);
            items.add(nue);
            initTimer();
        }

        public void actionPerformed(ActionEvent e) {
            firstUse = false;
            while (!items.isEmpty()) {
                Set<Item> copy = new HashSet<Item>(items);
                items.removeAll(copy);
                for (Item item : copy) {
                    System.err.println("DLG FOR " + item.project.getProjectDirectory().getPath());
                    if (item.project.isBadPlatformOrCard()) {
                        String platform = item.project.evaluator().getProperty(
                                ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
                        String card = item.project.evaluator().getProperty(
                                ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE);
                        item.project.showSelectPlatformAndDeviceDialog(platform, card, true);
                        if (!BadPlatformOrDevicePanel.isShowBrokenPlatformDialog()) {
                            return;
                        }
                    }
                }
            }
            synchronized (this) {
                timer = null;
            }
        }

        private static class Item {

            JCProject project;

            public Item(JCProject project) {
                this.project = project;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final Item other = (Item) obj;
                if (this.project != other.project && (this.project == null || !this.project.equals(other.project))) {
                    return false;
                }
                return true;
            }

            @Override
            public int hashCode() {
                int hash = 5;
                hash = 67 * hash + (this.project != null ? this.project.hashCode() : 0);
                return hash;
            }
        }
    }

    private void showSelectPlatformAndDeviceDialog(String platform, String device, boolean cbox) {
        if (platform == null) {
            platform = ""; //NOI18N
        }
        if (device == null) {
            device = ""; //NOI18N
        }
        final BadPlatformOrDevicePanel pnl = new BadPlatformOrDevicePanel(platform, device, cbox);
        ProblemPanel problemPanel = new ProblemPanel();
        problemPanel.setInnerComponent(pnl);
        pnl.setProblemHandler(problemPanel);
        String name = getLookup().lookup(ProjectInformation.class).getDisplayName();
        final DialogDescriptor dd = new DialogDescriptor(problemPanel,
                NbBundle.getMessage(
                JCProject.class, "TTL_BAD_PLATFORM_OR_DEVICE", name)); //NOI18N
        dd.setValid(false);
        ChangeListener cl = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                dd.setValid(pnl.getProblem() == null);
            }
        };
        pnl.addChangeListener(cl);
        if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
            final String newPlatform = pnl.getPlatform();
            final String newDevice = pnl.getDevice();
            final ProgressHandle progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(
                    JCProject.class, "MSG_SAVING_PROPERTIES")); //NOI18N
            if (!platform.equals(newPlatform) || !device.equals(newDevice)) {
                Runnable r = new Runnable() {

                    boolean first = true;

                    public void run() {
                        if (!EventQueue.isDispatchThread()) {
                            if (first) {
                                progress.start(6);
                                first = false;
                                progress.progress(1);
                                ProjectManager.mutex().writeAccess(this);
                            } else {
                                progress.progress(2);
                                EditableProperties props = antHelper.getProperties(
                                        AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                progress.progress(3);
                                props.setProperty(
                                        ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM,
                                        newPlatform);
                                props.setProperty(
                                        ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE,
                                        newDevice);
                                progress.progress(4);
                                antHelper.putProperties(
                                        AntProjectHelper.PROJECT_PROPERTIES_PATH,
                                        props);
                                progress.progress(5);
                                try {
                                    ProjectManager.getDefault().saveProject(JCProject.this);
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                } catch (IllegalArgumentException ex) {
                                    Exceptions.printStackTrace(ex);
                                } finally {
                                    EventQueue.invokeLater(this);
                                }
                            }
                        } else {
                            synchronized (JCProject.this) {
                                cachedBadProjectOrCard = null;
                            }
                            supp.fireChange();
                        }
                    }
                };
                GuiUtils.showProgressDialogAndRun(progress, r, false);
            }
        }
    }

    private final class SubprojectProviderImpl implements SubprojectProvider {

        private final ChangeSupport supp = new ChangeSupport(this);

        public Set<? extends Project> getSubprojects() {
            Set<Project> result = new HashSet<Project>();
            String cp = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_CLASS_PATH);
            if (cp != null && cp.length() > 0) {
                String[] jars = cp.trim().split(File.pathSeparator);
                for (String jar : jars) {
                    File f = new File(jar);
                    if (!f.exists()) {
                        File projDir = FileUtil.toFile(getProjectDirectory());
                        f = new File(projDir, jar);
                        if (!f.exists()) {
                            Logger.getLogger(JCProject.class.getName()).log(Level.INFO,
                                    "Non-existent JAR on classpath of " + //NOI18N
                                    getProjectDirectory().getPath() +
                                    " - '" + jar + "'"); //NOI18N
                            continue;
                        }
                    }
                    Project project = FileOwnerQuery.getOwner(f.toURI());
                    if (project != null) {
                        result.add(project);
                    }
                }
            }
            return result;
        }

        public void addChangeListener(ChangeListener cl) {
            supp.addChangeListener(cl);
        }

        public void removeChangeListener(ChangeListener cl) {
            supp.removeChangeListener(cl);
        }
    }

    private final class RecommendedTempatesImpl implements
            RecommendedTemplates, PrivilegedTemplates {

        public String[] getRecommendedTypes() {
            return kind().recommendedTemplateTypes();
        }

        public String[] getPrivilegedTemplates() {
            return kind().privilegedTemplates();
        }
    }

    private final class PlatformModificationListener extends FileChangeAdapter {

        @Override
        public void fileDataCreated(FileEvent fe) {
            String platformName = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
            if (fe.getFile().getName().equals(platformName)) {
                updateGlobalClassPaths();
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            fileDataCreated(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            fileDataCreated(fe);
        }
    }

    private final class AntArtifactProviderImpl implements AntArtifactProvider {

        public AntArtifact[] getBuildArtifacts() {
            List<AntArtifact> l = new ArrayList<AntArtifact>(4);
            AntArtifact distJar = antHelper.createSimpleAntArtifact(
                    JavaProjectConstants.ARTIFACT_TYPE_JAR,
                    ProjectPropertyNames.PROJECT_PROP_DIST_JAR,
                    evaluator(), "build", "clean", // NOI18N
                    ProjectPropertyNames.PROJECT_PROP_BUILD_SCRIPT);
            l.add(distJar);

            String[] rootProps = getRoots().getRootProperties();
            for (String s : rootProps) {
                AntArtifact a = antHelper.createSimpleAntArtifact(
                        JavaProjectConstants.SOURCES_TYPE_JAVA, s,
                        evaluator(), "", "clean", ProjectPropertyNames.PROJECT_PROP_BUILD_SCRIPT); // NOI18N
                l.add(a);
            }
            return l.toArray(new AntArtifact[l.size()]);
        }
    }

    final class ProjectOpenedHookImpl extends ProjectOpenedHook { //pkg private for unit tests

        @Override
        protected void projectOpened() {
            final String[] platformAndDevice = new String[2];
            try {
                //Disable dialog in unit tests
                if (!Boolean.getBoolean("JCProjectTest") && updateHelper.requestUpdate()) { //NOI18N
                    displayNameMayBeChanged();
                }
                genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        Utils.getBuildImplXslTemplate(),
                        true);
                genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_XML_PATH,
                        Utils.getBuildXslTemplate(),
                        true);
                String prop = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_SOURCE_ENCODING);
                Charset c = null;
                boolean updated = false;
                if (prop != null) {
                    try {
                        c = Charset.forName(prop);
                    } catch (IllegalCharsetNameException e) {
                        //Broken property, log & ignore
                        Logger LOG = Logger.getLogger(JCProject.class.getName());
                        LOG.warning("Illegal charset: " + prop + " in project: " + FileUtil.getFileDisplayName(getProjectDirectory())); //NOI18N
                    } catch (UnsupportedCharsetException e) {
                        //todo: Needs UI notification like broken references.
                        Logger LOG = Logger.getLogger(JCProject.class.getName());
                        LOG.warning("Unsupported charset: " + prop + " in project: " + FileUtil.getFileDisplayName(getProjectDirectory())); //NOI18N
                    }
                    if (c == null) {
                        String name = getLookup().lookup(ProjectInformation.class).getDisplayName();
                        c = UnsupportedEncodingDialog.showDialog(name, prop);
                        updated = true;
                    }
                }
                final Charset updatedCharset = updated ? c : null;
                getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {

                    public void run() throws IOException {
                        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {

                            public Void run() {
                                EditableProperties ep = antHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                                String ud = System.getProperty("netbeans.user");
                                if (ud != null) { //unit test
                                    File userDir = new File(ud);
                                    File buildProperties = new File(userDir, "build.properties"); // NOI18N
                                    if (buildProperties != null) { //unit test
                                        ep.setProperty(ProjectPropertyNames.PROJECT_PROP_USER_PROPERTIES_FILE,
                                                buildProperties.getAbsolutePath()); //NOI18N
                                    }
                                }
                                // Synchronize the options with actual values
                                JCProject.this.propertyChange(null);
                                antHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                                EditableProperties pubProps = antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

                                platformAndDevice[0] =
                                        pubProps.getProperty(
                                        ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
                                platformAndDevice[1] =
                                        pubProps.getProperty(
                                        ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE);

                                if (updatedCharset != null) {
                                    pubProps.setProperty(ProjectPropertyNames.PROJECT_PROP_SOURCE_ENCODING, updatedCharset.name());
                                    antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, pubProps);
                                }
                                try {
                                    ProjectManager.getDefault().saveProject(JCProject.this);
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                } catch (IllegalArgumentException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                                return null;
                            }
                        });
                    }
                });
            } catch (IOException e) {
                Logger.getLogger(JCProject.class.getName()).log(
                        Level.INFO, null, e);
            }
            if (BadPlatformOrDevicePanel.isShowBrokenPlatformDialog()) {
                final String platform = platformAndDevice[0];
                final String card = platformAndDevice[1];
                boolean bad = isBadPlatformOrCard(platform, card);
                synchronized (this) {
                    cachedBadProjectOrCard = bad;
                }
                if (bad) {
                    FIX_PLATFORM_DIALOG_QUEUE.add(JCProject.this);
                }
            }

            // register project's classpaths to GlobalPathRegistry
            registerGlobalPaths();
        }

        @Override
        protected void projectClosed() {
            // unregister project's classpaths to GlobalPathRegistry
            unregisterGlobalPaths();
        }
    }

    private final class Info implements ProjectInformation {

        private String cachedName;

        public String getName() {
            return PropertyUtils.getUsablePropertyName(getDisplayName());
        }

        public void checkDisplayName() {
            String old;
            synchronized (pcs) {
                old = cachedName;
                cachedName = null;
            }
            String nue = getDisplayName();
            if ((old == null && nue != null) || (old != null && !old.equals(nue))) {
                pcs.firePropertyChange(PROP_DISPLAY_NAME, old, nue);
            }
        }

        public String getDisplayName() {
            synchronized (pcs) {
                if (cachedName != null) {
                    return cachedName;
                }
            }
            String dn = ProjectManager.mutex().readAccess(new Mutex.Action<String>() {

                public String run() {
                    Element data = antHelper.getPrimaryConfigurationData(true);
                    // XXX replace by XMLUtil when that has findElement, findText, etc.
                    NodeList nl = data.getElementsByTagNameNS(
                            JCProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                            "name"); // NOI18N
                    if (nl.getLength() == 1) {
                        nl = nl.item(0).getChildNodes();
                        if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                            return nl.item(0).getNodeValue();
                        }
                    }
                    return "???"; // NOI18N
                }
            });
            synchronized (pcs) {
                cachedName = dn;
            }
            return dn;
        }

        public Icon getIcon() {
            return ImageUtilities.image2Icon(kind().icon());
        }

        public Project getProject() {
            return JCProject.this;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(
                PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        private final PropertyChangeSupport pcs =
                new PropertyChangeSupport(this);
    }

    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {

        @Override
        protected void projectXmlSaved() throws IOException {
            try {
                genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        Utils.getBuildImplXslTemplate(),
                        false);
                genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_XML_PATH,
                        Utils.getBuildXslTemplate(),
                        false);
            } catch (IOException e) {
                Logger.getLogger(JCProject.class.getName()).log(Level.INFO,
                        null, e);
            }
        }
    }

    private final class ClassPathProviderImpl implements ClassPathProvider {

        public ClassPath findClassPath(FileObject file, String type) {
            if (type.equals(ClassPath.COMPILE)) {
                return getCompileTimeClassPath();
            } else if (type.equals(ClassPath.EXECUTE)) {
                return getBootClassPath();
            } else if (type.equals(ClassPath.SOURCE)) {
                return getSourceClassPath();
            } else if (type.equals(ClassPath.BOOT)) {
                return getBootClassPath();
            } else {
                return null;
            }
        }

        /**
         * Returns array of all classpaths of the given type in the project.
         * The result is used for example for GlobalPathRegistry registrations.
         */
        public ClassPath[] getProjectClassPaths(String type) {
            ClassPath cp = null;

            if (ClassPath.SOURCE.equals(type)) {
                cp = ClassPathSupport.createClassPath(getRoots().getRoots());
            } else if (ClassPath.COMPILE.equals(type)) {
                cp = getCompileTimeClassPath();
            } else {
                cp = getBootClassPath();
            }
            return cp == null ? null : new ClassPath[]{cp};
        }
    }

    /**
     * This method should be used VERY rarely and never from the event thread.
     * It may resolve the entire transitive closure of all project dependencies,
     * their projects and files.  Use getLookup().lookup(DependencyProvider.class)
     * to do this asynchronously from the event thread.
     * @return A set of resolved dependencies
     * @throws SAXException
     * @throws IOException
     */
    public ResolvedDependencies syncGetResolvedDependencies() throws SAXException, IOException {
        DependenciesProviderImpl depsProv = getLookup().lookup(DependenciesProviderImpl.class);
        assert depsProv != null;
        Dependencies d = depsProv.sync();
        ResolvedDependencies result = new ResolvedDependenciesImpl(d, new DependenciesResolver(getProjectDirectory(), evaluator()));
        return result;
    }

    private final class DependenciesProviderImpl implements DependenciesProvider {

        public Cancellable requestDependencies(Receiver receiver) {
            DependenciesFinder finder = new DependenciesFinder(receiver);
            System.err.println("Posting dependencies finder");
            RequestProcessor.getDefault().post(finder);
            return finder;
        }

        Dependencies sync() throws SAXException, IOException {
            return new DependenciesFinder(null).collectDependencies();
        }

        private final class DependenciesFinder implements Runnable, Cancellable {

            private volatile boolean cancelled;
            private volatile boolean finished;
            private boolean inMutex = false;
            private final DependenciesProvider.Receiver receiver;

            public DependenciesFinder(DependenciesProvider.Receiver receiver) {
                this.receiver = receiver;
            }

            public void run() {
                if (cancelled) {
                    return;
                }
                if (!inMutex) {
                    inMutex = true;
                    ProjectManager.mutex().readAccess(this);
                } else {
                    if (cancelled) {
                        return;
                    }
                    try {
                        Dependencies deps = collectDependencies();
                        if (cancelled) {
                            return;
                        }
                        DependenciesResolver resolver = new DependenciesResolver(getProjectDirectory(), evaluator());
                        if (cancelled) {
                            return;
                        }
                        ResolvedDependencies resolved = new ResolvedDependenciesImpl(deps, resolver);
                        if (cancelled) {
                            return;
                        }
                        receiver.receive(resolved);
                    } catch (Exception e) {
                        receiver.failed(e);
//                        if (!receiver.failed(e)) {
                        Exceptions.printStackTrace(e);
//                        }
                    } finally {
                        synchronized (this) {
                            notifyAll();
                        }
                    }
                }
            }

            private Dependencies collectDependencies() throws SAXException, IOException {
                FileObject fo = getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_XML_PATH);
                InputStream in = new BufferedInputStream(fo.getInputStream());
                try {
                    InputSource src = new InputSource(in);
                    Dependencies result = Dependencies.parse(src, eval);
                    return result;
                } finally {
                    in.close();
                }
            }

            public boolean cancel() {
                return finished ? false : (cancelled = true);
            }
        }
    }

    ResolvedDependencies createResolvedDependencies() throws Exception {
        //Used by project updater outside EQ
        DependenciesResolver resolver = new DependenciesResolver(getProjectDirectory(), evaluator());
        return new ResolvedDependenciesImpl(new DependenciesProviderImpl().sync(), resolver);
    }

    private class ResolvedDependenciesImpl extends ResolvedDependencies implements Mutex.ExceptionAction<Void> {

        ResolvedDependenciesImpl(Dependencies deps, DependenciesResolver resolver) {
            super(deps, resolver);
        }

        @Override
        protected void doSave() throws IOException {
            if (!isModified()) {
                return;
            }
            try {
                ProjectManager.mutex().writeAccess(this);
            } catch (MutexException ex) {
                IOException ioe = new IOException("Could not save dependency changes");
                ioe.initCause(ex);
                throw ioe;
            } finally {
                onDependenciesChanged();
            }
        }

        public Void run() throws Exception {
            Element config = antHelper.getPrimaryConfigurationData(true);
            NodeList nl = config.getElementsByTagNameNS(JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, "dependencies");
            if (nl.getLength() == 0) {
                throw new IOException("<dependencies> section missing from project.xml");
            }
            if (nl.getLength() > 1) {
                throw new IOException("project.xml contains multiple <dependencies> sections in " + JCProject.this.getProjectDirectory().getPath());
            }
            Element el = (Element) nl.item(0);
            resolver.save(JCProject.this, this, el);
            antHelper.putPrimaryConfigurationData(config, true);
            EditableProperties pub = antHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            EditableProperties priv = antHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            boolean pubChanged = false;
            boolean privChanged = false;
            Dependencies curr = getDependencies();
            Dependencies orig = getOriginalDependencies();
            //Clean up properties for deleted dependencies
            orig.removeAll(curr);
            for (Dependency d : orig.all()) {
                for (ArtifactKind k : d.getKind().supportedArtifacts()) {
                    String prop = d.getPropertyName(k);
                    if (pub.getProperty(prop) != null) {
                        pubChanged |= true;
                        pub.remove(prop);
                    }
                    if (priv.getProperty(prop) != null) {
                        privChanged |= true;
                        priv.remove(prop);
                    }
                }
            }
            for (ResolvedDependency dep : all()) {
                Dependency d = dep.getDependency();
                for (ArtifactKind kind : d.getKind().supportedArtifacts()) {
                    String propName = d.getPropertyName(kind);
                    Path path = dep.getAntPath(kind);
                    if (path != null) {
                        if (!path.isRelative()) {
                            priv.setProperty(propName, path.toString());
                            privChanged = true;
                        } else {
                            pub.setProperty(propName, path.toString());
                            pubChanged = true;
                        }
                    }
                }
            }

            if (privChanged) {
                antHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, priv);
            }
            if (pubChanged) {
                antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, pub);
            }
            ProjectManager.getDefault().saveProject(JCProject.this);
            return null;
        }
    }
}
