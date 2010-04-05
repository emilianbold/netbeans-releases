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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
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
import org.netbeans.modules.javacard.JCUtil;
import org.netbeans.modules.javacard.api.AntClasspathClosureProvider;
import org.netbeans.modules.javacard.common.Utils;
import static org.netbeans.modules.javacard.common.JCConstants.RUNTIME_DESCRIPTOR;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.project.deps.ArtifactKind;
import org.netbeans.modules.javacard.project.deps.Dependencies;
import org.netbeans.modules.javacard.project.deps.DependenciesProvider;
import org.netbeans.modules.javacard.project.deps.DependenciesResolver;
import org.netbeans.modules.javacard.project.deps.Dependency;
import org.netbeans.modules.javacard.project.deps.Path;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.netbeans.modules.javacard.project.deps.ResolvedDependency;
import org.netbeans.modules.javacard.api.BadPlatformOrDevicePanel;
import org.netbeans.modules.javacard.common.GuiUtils;
import org.netbeans.modules.javacard.project.ui.UnsupportedEncodingDialog;
import org.netbeans.modules.javacard.source.JavacardAPQI;
import org.netbeans.modules.javacard.spi.Card;
import org.netbeans.modules.javacard.spi.Cards;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.PlatformAndDeviceProvider;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.support.ExtraSourceJavadocSupport;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.CacheDirectoryProvider;
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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
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
import org.xml.sax.SAXException;

/**
 *
 * @author Anki R Nelaturu, Tim Boudreau
 */
public class JCProject implements Project, AntProjectListener, PropertyChangeListener, ChangeListener {
    private final Object classpathLock = new Object();
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
    private ClassPath processorPath;
    private final UpdateHelper updateHelper;
    private final ReferenceHelper refHelper;
    private SourceRoots sourceRoots;
    private final PlatformPropertyProvider platformProperties;
    private final DevicePropertyProvider deviceProperties;
    private final Object rootsLock = new Object();
    private final ChangeSupport supp = new ChangeSupport(this);
    private final SubprojectProviderImpl subprojects = new SubprojectProviderImpl();
    private volatile Boolean cachedBadProjectOrCard;
    private ClassPath[] registeredSourceCP;
    private ClassPath[] registeredCompileCP;
    private ClassPath[] registeredBootCP;
    private ClassPath[] registeredProcessorCP;
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
        Updater updateProject = new Updater(this, antHelper, aux);
        this.updateHelper = new UpdateHelper(updateProject, antHelper);
        if (!updateProject.isCurrent() && updateProject.canUpdate()) {
            updateProject.saveUpdate(null);
        }
        cpProvider = new ClassPathProviderImpl();
        refHelper = new ReferenceHelper(antHelper, aux, eval);
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
                    new Info(),
                    new AntClasspathClosureProviderImpl(),
                    new JCLogicalViewProvider(this),
                    new JCProjectSources(this, antHelper, eval, getRoots()),
                    cpProvider,
                    new ProjectXmlSavedHookImpl(),
                    UILookupMergerSupport.createProjectOpenHookMerger(hook),
                    new RecommendedTempatesImpl(),
                    new JCProjectActionProvider(this),
                    new JCProjectOperations(this),
                    new JCCustomizerProvider(this, eval, genFilesHelper, antHelper),
                    new CacheDirectoryProviderImpl(),
                    refHelper.createSubprojectProvider(),
                    encodingQuery,
                    UILookupMergerSupport.createPrivilegedTemplatesMerger(),
                    UILookupMergerSupport.createRecommendedTemplatesMerger(),
                    LookupProviderSupport.createSourcesMerger(),
                    //PENDING replace second getRoots() with null if
                    //http://www.netbeans.org/issues/show_bug.cgi?id=162270 gets
                    //fixed
                    new ProxySourceForBinaryQuery(QuerySupport.createCompiledSourceForBinaryQuery(antHelper, evaluator(), getRoots(), getRoots()), new JCSourceForBinaryQuery(this)),
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
                    new JavacardAPQI(this),
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

        File projDir = FileUtil.toFile(getProjectDirectory());
        PropertyEvaluator result = PropertyUtils.sequentialPropertyEvaluator(
                antHelper.getStockPropertyPreprovider(),
                globals,
                PropertyUtils.userPropertiesProvider(baseEval2,
                ProjectPropertyNames.PROJECT_PROP_USER_PROPERTIES_FILE, projDir),
                //XXX part of Anki's http://hg.netbeans.org/main/rev/9fbe6ffa43fb
                //and almost certainly wrong - KEYSTORE_PASSWORD and KEYSTORE_ALIAS_PASSWORD
                //are NOT pointers to properties files.
//                PropertyUtils.userPropertiesProvider(baseEval2,
//                ProjectPropertyNames.PROJECT_PROP_KEYSTORE_PASSWORD, projDir),
//                PropertyUtils.userPropertiesProvider(baseEval2,
//                ProjectPropertyNames.PROJECT_PROP_KEYSTORE_ALIAS_PASSWORD, projDir),
                platformProperties,
                deviceProperties,
                privatePropsEval,
                publicPropsEval);
        return result;
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
            JavacardPlatform result = JCUtil.findPlatformNamed(platform);
            return result == null ? JavacardPlatform.createBrokenJavacardPlatform(platform) : result;
        }
        return null;
    }

    public Card getCard() {
        String platform = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_PLATFORM);
        String device = eval.getProperty(ProjectPropertyNames.PROJECT_PROP_ACTIVE_DEVICE);
        if (platform != null && device != null) {
            JavacardPlatform pform = getPlatform();
            if (pform != null && pform.isValid()) {
                return pform.getCards().find(device, true);
            }
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
        JavacardPlatform p = null;
        if (!badPlatform) {
            p = JCUtil.findPlatformNamed(platform);
            badPlatform = p == null || !p.isValid();
        }
        if (!badCard && !badPlatform) {
            Cards cards = p.getCards();
            Card theCard = cards.find (card, false);
            badCard = theCard == null || !theCard.isValid();
        }
        return badPlatform || badCard;
    }

    public final PropertyEvaluator evaluator() {
        return eval;
    }

    public final void propertyChange(final PropertyChangeEvent event) {
        if (event != null) {
            String propertyName = event.getPropertyName();
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
        subprojects.supp.fireChange();
    }

    void onDependenciesChanged() {
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

    private ClassPath sourcePath;
    public ClassPath getSourceClassPath() {
        synchronized (classpathLock) {
            if (sourcePath == null) {
                sourcePath = ClassPathFactory.createClassPath(new SourceRootsClasspathImpl(getRoots()));
            }
            return sourcePath;
        }
    }

    private final Object classpathClosureLock = new Object();
    private String classpathClosureString;

    public List<File> getClasspathClosure() {
        List<File> result = getClasspathClosure(new ArrayList<File>(20));
        Set<File> s = new HashSet<File>(result);
        if (s.size() != result.size()) {
            Map<File, Integer> m = new HashMap <File, Integer>();
            for (File f : result) {
                Integer i = m.get(f);
                if (i == null) {
                    i = 1;
                    m.put (f, i);
                } else {
                    i++;
                    m.put (f, i);
                }
            }
            for (Iterator<File> it=result.iterator(); it.hasNext();) {
                File f = it.next();
                int val = m.get(f);
                if (val > 1) {
                    val--;
                    m.put(f, val);
                    it.remove();
                }
            }
        }
        return result;
    }

    public String getClasspathClosureAsString() {
        synchronized (classpathClosureLock) {
            if (classpathClosureString != null) {
                return classpathClosureString;
            }
        }
        List<File> files = getClasspathClosure();
        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            if (sb.length() > 0) {
                sb.append (File.pathSeparatorChar);
            }
            sb.append (f.getAbsolutePath());
        }
        synchronized (classpathClosureLock) {
            classpathClosureString = sb.toString();
        }
        return sb.toString();
    }

    private void getClasspathClosure (Project p, List<File> l, Set<Project> visitedProjects) {
        assert p != null;
        AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
        if (prov != null) {
            for (AntArtifact a : prov.getBuildArtifacts()) {
                if (JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(a.getType())) {
                    for (URI uri : a.getArtifactLocations()) {
                        try {
                            l.add (new File(uri));
                        } catch (Exception e) {
                            File proj = FileUtil.toFile (p.getProjectDirectory());
                            String relPath = uri.toString().replace('/', File.separatorChar);
                            l.add (new File(proj, relPath));
                        }
                    }
                }
            }
        }
        JCProject jcp = p.getLookup().lookup(JCProject.class);
        if (jcp != null && !visitedProjects.contains(jcp)) {
            jcp.getClasspathClosure(l, visitedProjects);
            visitedProjects.add (jcp);
        } else {
            //XXX get subproject proivider, iterate all deps
            SubprojectProvider subs = p.getLookup().lookup(SubprojectProvider.class);
            if (subs != null) {
                for (Project sub : subs.getSubprojects()) {
                    if (!visitedProjects.contains(sub)) {
                        getClasspathClosure(sub, l, visitedProjects);
                    }
                }
            }
        }
        visitedProjects.add(p);
    }

    protected List<File> getClasspathClosure(List<File> l) {
        return getClasspathClosure(l, new HashSet<Project>());
    }

    protected List<File> getClasspathClosure(List<File> l, Set<Project> visitedProjects) {
        try {
            //Fetch the dependencies w/ resolved files and iterate
            ResolvedDependencies deps = this.syncGetResolvedDependencies();
            for (ResolvedDependency d : deps.all()) {
                String path = d.getPath(ArtifactKind.ORIGIN);
                File f = path == null ? null : new File (path);
                if (f != null) {
                    if (d.isProject()) {
                        if (f != null && f.exists()) {
                            Project p = FileOwnerQuery.getOwner(FileUtil.toFileObject(FileUtil.normalizeFile(f)));
                            if (p != null && !visitedProjects.contains(p)) {
                                getClasspathClosure(p, l, visitedProjects);
                                visitedProjects.add (p);
                            }
                        }
                    } else {
                        l.add(f);
                        if (FileUtil.isArchiveFile(f.toURI().toURL())) {
                            JarFile jf = new JarFile(f);
                            try {
                                Manifest mf = jf.getManifest();
                                if (mf != null) {
                                    String s = mf.getMainAttributes().getValue(
                                            "Class-Path"); //NOI18N

                                    if (s != null && s.trim().length() > 0) {
                                        String[] elements = s.split(" "); //NOI18N
                                        File dir = f.getParentFile();
                                        for (String el : elements) {
                                            File fel = new File (dir, 
                                                el.replace('/', //NOI18N
                                                File.separatorChar));
                                            l.add (fel);
                                        }
                                    }
                                } else {
                                    Logger log = Logger.getLogger(
                                            JCProject.class.getName());

                                    if (log.isLoggable(Level.WARNING)) {
                                        log.log(Level.WARNING, "Project at {0} dependent jar missing manifest: {1}",
                                                new Object[]{getProjectDirectory().getPath(), f.getAbsolutePath()});
                                    }
                                }
                            } finally {
                                jf.close();
                            }
                        }
                    }
                } else {
                    Logger log = Logger.getLogger(
                            JCProject.class.getName());

                    if (log.isLoggable(Level.WARNING)) {
                        log.log(Level.WARNING, "Project at {0} cannot resolve dependency path: {1}",
                                new Object[]{getProjectDirectory().getPath(), path});
                    }
                }
            }
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return l;
    }

    @Override
    public String toString() {
        return super.toString() + 
                "(kind=" + //NOI18N
                kind.name() +
                " path=" + //NOI18N
                getProjectDirectory().getPath() +
                ")"; //NOI18N
    }

    final ClassPath getLibClassPath() {
        synchronized (classpathLock) {
            if (libPath == null) {
                libPath = ClassPathFactory.createClassPath(
                        new DependenciesClasspathImpl(this));
            }
        }
        return libPath;
    }

    private final ClassPath getBootClassPath() {
        synchronized (classpathLock) {
            if (bootPath == null) {
                bootPath = ClassPathFactory.createClassPath(new BootClassPathImpl(this));
            }
            return bootPath;
        }
    }

    private final ClassPath getProcessorClassPath() {
        synchronized (classpathLock) {
            if (processorPath == null) {
                processorPath = ClassPathSupport.createClassPath(new URL[0]);
            }
            return processorPath;
        }
    }

    public static JCProject getOwnerProjectOf(FileObject obj) {
        Project p = FileOwnerQuery.getOwner(obj);
        return p == null ? null : p.getLookup().lookup(JCProject.class);
    }

    private final ClassPath getCompileTimeClassPath() {
        synchronized (classpathLock) {
            if (compileTimePath == null) {
                compileTimePath = ClassPathSupport.createProxyClassPath(getBootClassPath(), getLibClassPath());
            }
            return compileTimePath;
        }
    }

    private final void registerGlobalPaths() {
        synchronized (classpathLock) {
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

            registeredProcessorCP =
                    cpProvider.getProjectClassPaths(JavaClassPathConstants.PROCESSOR_PATH);
            if (registeredProcessorCP != null) {
                GlobalPathRegistry.getDefault().register(
                        JavaClassPathConstants.PROCESSOR_PATH, registeredProcessorCP);
            }
        }
    }

    private final void unregisterGlobalPaths() {
        synchronized (classpathLock) {
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
            if (registeredProcessorCP != null) {
                try {
                    GlobalPathRegistry.getDefault().unregister(JavaClassPathConstants.PROCESSOR_PATH,
                            registeredProcessorCP);
                } catch (IllegalArgumentException e) {
                }// Was not registered, ignore
            }
        }
    }

    public void showSelectPlatformAndDeviceDialog() {
        if (BadPlatformOrDevicePanel.isShowBrokenPlatformDialog()) {
            FIX_PLATFORM_DIALOG_QUEUE.add(this);
        }
    }

    /**
     * Recommended template types for different project types
     * @return An array of undocumented magic strings
     */
    public static String[] recommendedTemplateTypes(ProjectKind kind) {
        switch (kind) {
            case WEB:
                return new String[]{
                            "java-classes", // NOI18N
                            "java-beans", //NOI18N
                            "XML", // NOI18N
                            "web-service-clients", // NOI18N
                            "simple-files", // NOI18N
                            "ant-script", // NOI18N
                        };
            case EXTENDED_APPLET:
            case CLASSIC_APPLET:
            case EXTENSION_LIBRARY:
            case CLASSIC_LIBRARY:
                return new String[]{
                            "java-classes", // NOI18N
                            "java-beans", //NOI18N
                            "simple-files", // NOI18N
                            "XML", // NOI18N
                            "ant-script", // NOI18N
                        };
            default:
                throw new AssertionError();
        }
    }

    /**
     * Preferred templates for this project type
     * @return Array of SFS paths to the templates
     */
    public static String[] privilegedTemplates(ProjectKind kind) {
        switch (kind) {
            case WEB:
                return new String[]{
                            "Templates/javacard/Servlet.java", // NOI18N
                            "Templates/Classes/Class.java", // NOI18N
                            "Templates/Classes/Package", // NOI18N
                            "Templates/Classes/Interface.java", // NOI18N
                        };
            case EXTENDED_APPLET:
                return new String[]{
                            "Templates/javacard/ExtendedApplet.java", //NOI18N
                            "Templates/Classes/Class.java", // NOI18N
                            "Templates/Classes/Package", // NOI18N
                            "Templates/Classes/Interface.java", // NOI18N
                        };
            case CLASSIC_APPLET:
                return new String[]{
                            "Templates/javacard/Applet.java", // NOI18N
                            "Templates/Classes/Class.java", // NOI18N
                            "Templates/Classes/Interface.java", // NOI18N
                        };
            case EXTENSION_LIBRARY:
                return new String[]{
                            "Templates/Classes/Class.java", // NOI18N
                            "Templates/Classes/Package", // NOI18N
                            "Templates/Classes/Interface.java", // NOI18N
                        };
            case CLASSIC_LIBRARY:
                return new String[]{
                            "Templates/Classes/Class.java", // NOI18N
                            "Templates/Classes/Interface.java", // NOI18N
                        };
            default:
                throw new AssertionError();
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
        PlatformAndDeviceProvider prov = BadPlatformOrDevicePanel.showDialog(platform, device, cbox, kind());
        if (prov != null) {
            final String newPlatform = prov.getPlatformName();
            final String newDevice = prov.getActiveDevice();
            if (platform.equals(newPlatform) && device.equals(newDevice)) {
                return;
            }
            final ProgressHandle progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(
                JCProject.class, "MSG_SAVING_PROPERTIES")); //NOI18N
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
                        platformProperties.fire();
                        deviceProperties.fire();
                        supp.fireChange();
                    }
                }
            };
            GuiUtils.showProgressDialogAndRun(progress, r, false);
        }
    }

    private final class SubprojectProviderImpl implements SubprojectProvider {

        private final ChangeSupport supp = new ChangeSupport(this);

        public Set<? extends Project> getSubprojects() {
            Set<Project> result = new HashSet<Project>();
            try {
                for (ResolvedDependency dep : syncGetResolvedDependencies().all()) {
                    if (dep.isProject()) {
                        FileObject fo = dep.resolve(ArtifactKind.ORIGIN);
                        if (fo != null) {
                            Project p = FileOwnerQuery.getOwner(fo);
                            if (p != null) {
                                result.add(p);
                            }
                        }
                    }
                }
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
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

    private final class CacheDirectoryProviderImpl implements CacheDirectoryProvider {
        private static final String CACHE_DIR = "nbproject/private/cache";
        @Override
        public FileObject getCacheDirectory() throws IOException {
            FileObject fo = getProjectDirectory().getFileObject(CACHE_DIR);
            if (fo == null) {
                fo = FileUtil.createFolder (fo, CACHE_DIR);
            }
            return fo;
        }
    }

    private final class RecommendedTempatesImpl implements
            RecommendedTemplates, PrivilegedTemplates {

        public String[] getRecommendedTypes() {
            return recommendedTemplateTypes(kind());
        }

        public String[] getPrivilegedTemplates() {
            return privilegedTemplates(kind());
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
                        JCUtil.getBuildImplXslTemplate(),
                        true);
                genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_XML_PATH,
                        JCUtil.getBuildXslTemplate(),
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
                                        ep.put(ProjectPropertyNames.PROJECT_PROP_KEYSTORE_ALIAS_PASSWORD,
                                                "password"); //NOI18N
                                        ep.put(ProjectPropertyNames.PROJECT_PROP_KEYSTORE_PASSWORD,
                                                "password"); //NOI18N
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
                        JCUtil.getBuildImplXslTemplate(),
                        false);
                genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_XML_PATH,
                        JCUtil.getBuildXslTemplate(),
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
            } else if (type.equals(JavaClassPathConstants.PROCESSOR_PATH)) {
                return getProcessorClassPath();
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

    Dependencies syncGetDependencies() throws SAXException, IOException {
        DependenciesProviderImpl depsProv = getLookup().lookup(DependenciesProviderImpl.class);
        //PENDING:  This may need some caching - can be called frequently by
        //classpaths and trigger a re-read
        return depsProv.sync();
    }

    private final class DependenciesProviderImpl implements DependenciesProvider {

        public Cancellable requestDependencies(Receiver receiver) {
            DependenciesFinder finder = new DependenciesFinder(receiver);
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
                Element cfgRoot = antHelper.getPrimaryConfigurationData(true);
                return Dependencies.parse(cfgRoot);
            }

            public boolean cancel() {
                return finished ? false : (cancelled = true);
            }
        }
    }

    ResolvedDependencies createResolvedDependencies() throws Exception {
        //Used by project updater outside EQ
        return createResolvedDependencies (new DependenciesProviderImpl().sync());
    }

    ResolvedDependencies createResolvedDependencies (Dependencies deps) {
        DependenciesResolver resolver = new DependenciesResolver(getProjectDirectory(), evaluator());
        return new ResolvedDependenciesImpl(deps, resolver);
    }

    static final Logger LOGGER = Logger.getLogger (JCProject.class.getName());
    private class ResolvedDependenciesImpl extends ResolvedDependencies implements Mutex.ExceptionAction<Void> {

        ResolvedDependenciesImpl(Dependencies deps, DependenciesResolver resolver) {
            super(deps, resolver);
        }

        @Override
        protected void doSave() throws IOException {
            LOGGER.log (Level.FINE, "Save dependencies on {0} {1}", new Object[] { JCProject.this, this }); //NOI18N
            if (!isModified()) {
                LOGGER.log (Level.FINEST, "Abort save unmodified changes", new Object[] { JCProject.this, this }); //NOI18N
                return;
            }
            try {
                ProjectManager.mutex().writeAccess(this);
            } catch (MutexException ex) {
                IOException ioe = new IOException("Could not save dependency changes"); //NOI18N
                ioe.initCause(ex);
                throw ioe;
            } finally {
                onDependenciesChanged();
            }
        }

        public Void run() throws Exception {
            synchronized (classpathClosureLock) {
                classpathClosureString = null;
            }
            LOGGER.log (Level.FINER, "Begin save of deps {0} on {1}", new Object[] { JCProject.this, this }); //NOI18N
            Element config = antHelper.getPrimaryConfigurationData(true);
            resolver.save(JCProject.this, this, config);
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
                LOGGER.log (Level.FINER, "Clean up dependencies for {0} {1}", new Object[] { d, d.getKind() }); //NOI18N
                for (ArtifactKind k : d.getKind().supportedArtifacts()) {
                    String prop = d.getPropertyName(k);
                    if (pub.getProperty(prop) != null) {
                        pubChanged |= true;
                        LOGGER.log (Level.FINEST, "Remove public property  {0} ", new Object[] { prop }); //NOI18N
                        pub.remove(prop);
                    }
                    if (priv.getProperty(prop) != null) {
                        LOGGER.log (Level.FINEST, "Remove private property  {0} ", new Object[] { prop }); //NOI18N
                        privChanged |= true;
                        priv.remove(prop);
                    }
                }
            }
            //Update all dependencies
            for (ResolvedDependency dep : all()) {
                Dependency d = dep.getDependency();
                LOGGER.log (Level.FINEST, "Update dependency {0} ", new Object[] { d }); //NOI18N
                for (ArtifactKind kind : d.getKind().supportedArtifacts()) {
                    String propName = d.getPropertyName(kind);
                    Path path = dep.getAntPath(kind);
                    if (path != null) {
                        if (!path.isRelative()) {
                            LOGGER.log (Level.FINEST, "Set private property {0}={1}", new Object[] { propName, path }); //NOI18N
                            priv.setProperty(propName, path.toString());
                            privChanged = true;
                        } else {
                            LOGGER.log (Level.FINEST, "Set public property {0}={1}", new Object[] { propName, path }); //NOI18N
                            pub.setProperty(propName, path.toString());
                            pubChanged = true;
                        }
                    }
                }
            }

            if (privChanged) {
                LOGGER.log (Level.FINEST, "Write private properties"); //NOI18N
                antHelper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, priv);
            }
            if (pubChanged) {
                LOGGER.log (Level.FINEST, "Write public properties"); //NOI18N
                antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, pub);
            }
            
            //XXX delete use of class.path var after build scripts updated
            //to handle new-style complex dependencies
            //Must do this after the calls to putProperties to ensure all
            //paths are properly resolved.
            StringBuilder sb = new StringBuilder();
            for (ResolvedDependency dep : all()) {
                //For now, just store absolute paths as in 6.7
                File f = dep.resolveFile(ArtifactKind.ORIGIN);
                if (f != null) {
                    if (dep.getKind().isProjectDependency()) {
                        FileObject fo = dep.resolve(ArtifactKind.ORIGIN);
                        if (fo != null) {
                            Project p = FileOwnerQuery.getOwner(fo);
                            if (p != null) {
                                AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
                                if (prov != null) {
                                    for (AntArtifact a : prov.getBuildArtifacts()) {
                                        for (URI uri : a.getArtifactLocations()) {
                                            File f1;
                                            try {
                                                f1 = new File(uri);
                                            } catch (IllegalArgumentException e) { //non-absolute URI
                                                File projDir = FileUtil.toFile (p.getProjectDirectory());
                                                f1 = new File (projDir, uri.toString());
                                            }
                                            if (sb.length() > 0) {
                                                sb.append(File.pathSeparatorChar);
                                            }
                                            sb.append (f1.getAbsolutePath());
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (sb.length() > 0) {
                            sb.append(File.pathSeparatorChar);
                        }
                    }
                    sb.append (f.getAbsolutePath());
                }
            }
            pub.setProperty(ProjectPropertyNames.PROJECT_PROP_CLASS_PATH, sb.toString());
            antHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, pub);
            //end deletia
            ProjectManager.getDefault().saveProject(JCProject.this);
            return null;
        }
    }
    
    private final class AntClasspathClosureProviderImpl extends AntClasspathClosureProvider {
        @Override
        public String getClasspathClosureAsString() {
            return JCProject.this.getClasspathClosureAsString();
        }

        @Override
        public File getTargetArtifact() {
            String path = evaluator().evaluate("${" + ProjectPropertyNames.PROJECT_PROP_DIST_JAR + "}"); //NOI18N
            path.replace ('/', File.separatorChar); //NOI18N
            return new File(path).getAbsoluteFile();
        }
    }
}
