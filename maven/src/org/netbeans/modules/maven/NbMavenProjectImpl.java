/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.execute.ActiveJ2SEPlatformProvider;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.configurations.ProjectProfileHandlerImpl;
import org.netbeans.modules.maven.cos.CopyResourcesOnSave;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.modelcache.MavenProjectCache;
import org.netbeans.modules.maven.problems.ProblemReporterImpl;
import org.netbeans.modules.maven.spi.queries.JavaLikeRootProvider;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * A Maven-based project.
 */
public final class NbMavenProjectImpl implements Project {

    //TODO remove
    public static final String PROP_PROJECT = "MavenProject"; //NOI18N
    //TODO remove
    public static final String PROP_RESOURCE = "RESOURCES"; //NOI18N

    private static final Logger LOG = Logger.getLogger(NbMavenProjectImpl.class.getName());
    
    //sequential execution might be necesary for #166919
    public static final RequestProcessor RELOAD_RP = new RequestProcessor("Maven project reloading", 1); //NOI18
    //minor optimization. In case the queue already holds the task and is not run, delay, if running reschedule.
    private final RequestProcessor.Task reloadTask = RELOAD_RP.create(new Runnable() {
        @Override
        public void run() {
            problemReporter.clearReports(); //#167741 -this will trigger node refresh?
            MavenProject prj = loadOriginalMavenProject(true);
            synchronized (NbMavenProjectImpl.this) {
                project = new SoftReference<MavenProject>(prj);
                if (hardReferencingMavenProject) {
                    hardRefProject = prj;
                }
            }
            ACCESSOR.doFireReload(watcher);
            problemReporter.doIDEConfigChecks();
        }
    });
    private FileObject fileObject;
    private FileObject folderFileObject;
    private final File projectFile;
    private final Lookup basicLookup;
    private final Lookup completeLookup;
    private final Lookup lookup;
    private final Updater projectFolderUpdater;
    private final Updater userFolderUpdater;
    
    private Reference<MavenProject> project;
    private boolean hardReferencingMavenProject = false; //only should be true when project is open.
    private MavenProject hardRefProject;
    
    private ProblemReporterImpl problemReporter;
    private final @NonNull NbMavenProject watcher;
    private final M2ConfigProvider configProvider;
    private final @NonNull MavenProjectPropsImpl auxprops;
    private ProjectProfileHandlerImpl profileHandler;
    private CopyResourcesOnSave copyResourcesOnSave;
    private final Object COPYRESOURCES_LOCK = new Object();
    @org.netbeans.api.annotations.common.SuppressWarnings("MS_SHOULD_BE_FINAL")
    public static WatcherAccessor ACCESSOR = null;

    static {
        // invokes static initializer of ModelHandle.class
        // that will assign value to the ACCESSOR field above
        Class<?> c = NbMavenProject.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "very wrong, very wrong, yes indeed", ex);
        }
    }

    //#224012
    private ProjectOpenedHookImpl hookImpl;
    private Exception ex;
    private final Object LOCK_224012 = new Object();
    boolean setIssue224012(ProjectOpenedHookImpl hook, Exception exception) {
        synchronized (LOCK_224012) {
            if (hookImpl == null) {
                hookImpl = hook;
                ex = exception;
                return true;
            } else {
                LOG.log(Level.INFO, "    first creation stacktrace", ex);
                LOG.log(Level.INFO, "    second creation stacktrace", exception);
                LOG.log(Level.WARNING, "Spotted issue 224012 (https://netbeans.org/bugzilla/show_bug.cgi?id=224012). Please report the incident wth IDE log attached.");
                return false;
            }
        }
    }


    public static abstract class WatcherAccessor {

        public abstract NbMavenProject createWatcher(NbMavenProjectImpl proj);

        public abstract void doFireReload(NbMavenProject watcher);
    }

    /**
     * Creates a new instance of MavenProject, should never be called by user code.
     * but only by MavenProjectFactory!!!
     */
    NbMavenProjectImpl(FileObject folder, FileObject projectFO, ProjectState projectState) {
        this.projectFile = FileUtil.normalizeFile(FileUtil.toFile(projectFO));
        fileObject = projectFO;
        folderFileObject = folder;
        lookup = Lookups.proxy(new Lookup.Provider() {
            @Override
            public Lookup getLookup() {
                if (completeLookup == null) {
                    //not fully initialized constructor
                    LOG.log(Level.FINE, "accessing project's lookup before the instance is fully initialized at " + projectFile, new Exception());
                    assert basicLookup != null;
                    return basicLookup;
                } else {
                    return completeLookup;
                }
            }
        });
        watcher = ACCESSOR.createWatcher(this);
        projectFolderUpdater = new Updater("nb-configuration.xml", "pom.xml"); //NOI18N
        userFolderUpdater = new Updater("settings.xml");//NOI18N
        problemReporter = new ProblemReporterImpl(this);
        M2AuxilaryConfigImpl auxiliary = new M2AuxilaryConfigImpl(folder, true);
        auxprops = new MavenProjectPropsImpl(auxiliary, this);
        profileHandler = new ProjectProfileHandlerImpl(this, auxiliary);
        configProvider = new M2ConfigProvider(this, auxiliary, profileHandler);
        // @PSP's and the like, and PackagingProvider impls, may check project lookup for e.g. NbMavenProject, so init lookup in two stages:
        basicLookup = createBasicLookup(projectState, auxiliary);
        //here we always load the MavenProject instance because we need to touch the packaging from pom.
        completeLookup = LookupProviderSupport.createCompositeLookup(basicLookup, new PackagingTypeDependentLookup(watcher));
    }

    public File getPOMFile() {
        return projectFile;
    }

    public @NonNull NbMavenProject getProjectWatcher() {
        return watcher;
    }

    public ProblemReporterImpl getProblemReporter() {
        return problemReporter;
    }

    /**
     * load a project with properties and profiles other than the current ones.
     * @param embedder embedder to use
     * @param activeProfiles
     * @param properties
     * @return
     */
    //TODO revisit usage, eventually should be only reuse MavenProjectCache
    public @NonNull MavenProject loadMavenProject(MavenEmbedder embedder, List<String> activeProfiles, Properties properties) {
        try {
            MavenExecutionRequest req = embedder.createMavenExecutionRequest();
            req.addActiveProfiles(activeProfiles);
            req.setPom(projectFile);
            req.setNoSnapshotUpdates(true);
            req.setUpdateSnapshots(false);
            Properties props = MavenProjectCache.createSystemPropsForProjectLoading(null);
            if (properties != null) {
                props.putAll(properties);
            }
            req.setUserProperties(props);
            //MEVENIDE-634 i'm wondering if this fixes the issue
            req.setInteractiveMode(false);
            req.setOffline(true);
            // recursive == false is important to avoid checking all submodules for extensions
            // that will not be used in current pom anyway..
            // #135070
            req.setRecursive(false);
            MavenExecutionResult res = embedder.readProjectWithDependencies(req, true);
            //#215159 clear the project building request, it references multiple Maven Models via the RepositorySession cache
            //is not used in maven itself, most likely used by m2e only..
            if (!res.hasExceptions()) {
                res.getProject().setProjectBuildingRequest(null);
                return res.getProject();
            } else {
                List<Throwable> exc = res.getExceptions();
                for (Throwable ex : exc) {
                    LOG.log(Level.FINE, "Exception thrown while loading maven project at " + getProjectDirectory(), ex); //NOI18N
                }
            }
        } catch (RuntimeException exc) {
            //guard against exceptions that are not processed by the embedder
            //#136184 NumberFormatException
            LOG.log(Level.INFO, "Runtime exception thrown while loading maven project at " + getProjectDirectory(), exc); //NOI18N
        }
        return MavenProjectCache.getFallbackProject(this.getPOMFile());
    }
    
    /**
     * replacement for MavenProject.getParent() which has bad long term memory behaviour. We offset it by recalculating/reparsing everything
     * therefore should not be used lightly!
     * pass a MavenProject instance and current configuration and other settings will be applied when loading the parent.
     * @param project
     * @return null or the parent mavenproject
     */
    
    public MavenProject loadParentOf(MavenEmbedder embedder, MavenProject project) throws ProjectBuildingException {

        MavenProject parent = null;
        ProjectBuilder builder = embedder.lookupComponent(ProjectBuilder.class);
        MavenExecutionRequest req = embedder.createMavenExecutionRequest();
        M2Configuration active = configProvider.getActiveConfiguration();
        req.addActiveProfiles(active.getActivatedProfiles());
        req.setNoSnapshotUpdates(true);
        req.setUpdateSnapshots(false);
        req.setInteractiveMode(false);
        req.setRecursive(false);
        req.setOffline(true);
        req.setUserProperties(MavenProjectCache.createSystemPropsForProjectLoading(active.getProperties()));

        ProjectBuildingRequest request = req.getProjectBuildingRequest();
        request.setRemoteRepositories(project.getRemoteArtifactRepositories());
        DefaultMaven maven = (DefaultMaven) embedder.lookupComponent(Maven.class);
        
        request.setRepositorySession(maven.newRepositorySession(req));

        if (project.getParentFile() != null) {
            parent = builder.build(project.getParentFile(), request).getProject();
        } else if (project.getModel().getParent() != null) {
            parent = builder.build(project.getParentArtifact(), request).getProject();
        }
        //clear the project building request, it references multiple Maven Models via the RepositorySession cache
        //is not used in maven itself, most likely used by m2e only..
        if (parent != null) {
            parent.setProjectBuildingRequest(null);
        }
        MavenEmbedder.normalizePaths(parent);
        return parent;
    }

    public List<String> getCurrentActiveProfiles() {
        List<String> toRet = new ArrayList<String>();
        toRet.addAll(configProvider.getActiveConfiguration().getActivatedProfiles());
        return toRet;
    }


    //#172952 for property expression resolution we need this to include
    // the properties of the platform to properly resolve stuff like com.sun.boot.class.path
    public Map<? extends String,? extends String> createSystemPropsForPropertyExpressions() {
        Map<String,String> props = NbCollections.checkedMapByCopy(MavenProjectCache.cloneStaticProps(), String.class, String.class, true);
        ActiveJ2SEPlatformProvider platformProvider = getLookup().lookup(ActiveJ2SEPlatformProvider.class);
        if (platformProvider != null) { // may be null inside PackagingProvider
            props.putAll(platformProvider.getJavaPlatform().getSystemProperties());
        }
        props.putAll(configProvider.getActiveConfiguration().getProperties());
        return props;
    }

    /**
     * getter for the maven's own project representation.. this instance is cached but gets reloaded
     * when one the pom files have changed.
     */
    public @NonNull synchronized MavenProject getOriginalMavenProject() {
        MavenProject mp = project == null ? null : project.get();
        if (mp == null) {
            mp = loadOriginalMavenProject(false);
            project = new SoftReference<MavenProject>(mp);
            if (hardReferencingMavenProject) {
                hardRefProject = mp;
            }
        }
        return mp;
    }
    
    /**
     * a marginally unreliable, non blocking method for figuring if the model is loaded or not.
     * @return 
     */
    public boolean isMavenProjectLoaded() {
        Reference<MavenProject> prj = project;
        if (prj != null) {
            return prj.get() != null;
        }
        return false;
    }
    
    /**
     * open projects should always hard reference the Mavenproject instance to prevent it from
     * being GCed, the instance will get reloaded almost instantly anyway
     */
    void startHardReferencingMavenPoject() {
        synchronized (this) {
            hardReferencingMavenProject = true;
            MavenProject mp = project == null ? null : project.get();
            hardRefProject = mp;
        }
    }
    /**
     * open projects should always hard reference the Mavenproject instance to prevent it from
     * being GCed, the instance will get reloaded almost instantly anyway
     */
    void stopHardReferencingMavenPoject() {
        synchronized (this) {
            hardReferencingMavenProject = false;
            hardRefProject = null;
        }
    }
    

    @Messages({
        "TXT_RuntimeException=RuntimeException occurred in Apache Maven embedder while loading",
        "TXT_RuntimeExceptionLong=RuntimeException occurred in Apache Maven embedder while loading the project. \n"
            + "This is preventing the project model from loading properly. \n"
            + "Please file a bug report with details about your project and the IDE's log file.\n\n"
    })
    private @NonNull MavenProject loadOriginalMavenProject(boolean reload) {
        MavenProject newproject;
        try {
            newproject = MavenProjectCache.getMavenProject(this.folderFileObject, reload);
            if (newproject == null) { //null when no pom.xml in project folder..
                newproject = MavenProjectCache.getFallbackProject(projectFile);
            }
            final MavenExecutionResult res = MavenProjectCache.getExecutionResult(newproject);
            final MavenProject np = newproject;
            ProblemReporterImpl.RP.post(new Runnable() {
                //#217286 doArtifactChecks can call FileOwnerQuery and attempt to aquire the project mutex. but we are under synchronization here..
                @Override
                public void run() {
                    if (res != null && res.hasExceptions()) { //res is null when there is no pom in the project folder.
                        problemReporter.reportExceptions(res);
                    } else {
                        problemReporter.doArtifactChecks(np);
                    }
                }
            });
        } finally {
            if (LOG.isLoggable(Level.FINE) && SwingUtilities.isEventDispatchThread()) {
                LOG.log(Level.FINE, "Project " + getProjectDirectory().getPath() + " loaded in AWT event dispatching thread!", new RuntimeException());
            }
        }
        assert newproject != null;
        return newproject;
    }



    public void fireProjectReload() {
        //#227101 not only AWT and project read/write mutex has to be checked, there are some additional more
        //complex scenarios that can lead to deadlock. Just give up and always fire changes in separate RP.
        if (Boolean.getBoolean("test.reload.sync")) {
            reloadTask.run();
            //for tests just do sync reload, even though silly, even sillier is to attempt to sync the threads..
            return;
        }
        reloadTask.schedule(0); //asuming here that schedule(0) will move the scheduled task in the queue if not yet executed
    }

    public static void refreshLocalRepository(NbMavenProjectImpl project) {
        File file = project.getEmbedder().getLocalRepositoryFile();
        FileUtil.refreshFor(file);
    }

    /** Begin listening to pom.xml changes. */
    void attachUpdater() {
        projectFolderUpdater.attachAll(getProjectDirectory());
        userFolderUpdater.attachAll(getHomeDirectory());
    }
   void detachUpdater() {
        projectFolderUpdater.detachAll();
        userFolderUpdater.detachAll();
    }

    /**
     * The root directory of the project where the POM resides.
     */
    @Override
    public FileObject getProjectDirectory() {
        return folderFileObject;
    }

    public FileObject getHomeDirectory() {
        File homeFile = MavenCli.userMavenConfigurationHome;

        FileObject home = null;
        try {
            home = FileUtil.createFolder(homeFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (home == null) {
            //TODO this is a problem, probably UNC path on windows - MEVENIDE-380
            // some functionality won't work
            LOG.log(Level.WARNING, "Cannot convert home dir to FileObject, some functionality won''t work. It''s usually the case on Windows and UNC paths. The path is {0}", homeFile);
        }
        return home;
    }

    public @CheckForNull String getArtifactRelativeRepositoryPath() {
        Artifact artifact = getOriginalMavenProject().getArtifact();
        if (artifact == null) {
            return null;
        }
        return getArtifactRelativeRepositoryPath(artifact);
    }

    /**
     * path of test artifact in local repository
     * @return
     */
    public @CheckForNull String getTestArtifactRelativeRepositoryPath() {
        Artifact main = getOriginalMavenProject().getArtifact();
        if (main == null) {
            return null;
        }

        ArtifactHandlerManager artifactHandlerManager = getEmbedder().lookupComponent(ArtifactHandlerManager.class);
        assert artifactHandlerManager != null : "ArtifactHandlerManager component not found in maven";

        Artifact test = new DefaultArtifact(main.getGroupId(), main.getArtifactId(), main.getVersionRange(),
                Artifact.SCOPE_TEST, "test-jar", "tests", artifactHandlerManager.getArtifactHandler("test-jar"));
        return getArtifactRelativeRepositoryPath(test);

    }

    public String getArtifactRelativeRepositoryPath(@NonNull Artifact artifact) {
        return getEmbedder().getLocalRepository().pathOf(artifact);
    }

    public MavenEmbedder getEmbedder() {
        return EmbedderFactory.getProjectEmbedder();
    }

    public @NonNull MavenProjectPropsImpl getAuxProps() {
        return auxprops;
    }

    public URI[] getSourceRoots(boolean test) {
        List<URI> uris = new ArrayList<URI>();
        for (String root : test ? getOriginalMavenProject().getTestCompileSourceRoots() : getOriginalMavenProject().getCompileSourceRoots()) {
            uris.add(FileUtilities.convertStringToUri(root));
        }
        for (JavaLikeRootProvider rp : getLookup().lookupAll(JavaLikeRootProvider.class)) {
            // XXX for a few purposes (listening) it is desirable to list these even before they exist, but usually it is just noise (cf. #196414 comment #2)
            FileObject root = getProjectDirectory().getFileObject("src/" + (test ? "test" : "main") + "/" + rp.kind());
            if (root != null && root.isFolder()) {
                uris.add(root.toURI());
            }
        }
        return uris.toArray(new URI[uris.size()]);
    }

    public URI[] getGeneratedSourceRoots(boolean test) {
        URI uri = FileUtilities.getDirURI(getProjectDirectory(), test ? "target/generated-test-sources" : "target/generated-sources"); //NOI18N
        Set<URI> uris = new HashSet<URI>();
        File[] roots = Utilities.toFile(uri).listFiles();
        if (roots != null) {
            for (File root : roots) {
                if (!VisibilityQuery.getDefault().isVisible(root)) { //#214002
                   continue;
                }
                if (!test && root.getName().startsWith("test-")) {
                    continue;
                }
                File[] kids = root.listFiles();
                if (kids != null && /* #190626 */kids.length > 0) {
                    uris.add(Utilities.toURI(root));
                } else {
                    watcher.addWatchedPath(Utilities.toURI(root));
                }
            }
        }
        if (test) { // MCOMPILER-167
            roots = Utilities.toFile(FileUtilities.getDirURI(getProjectDirectory(), "target/generated-sources")).listFiles();
            if (roots != null) {
                for (File root : roots) {
                    if (!VisibilityQuery.getDefault().isVisible(root)) { //#214002
                       continue;
                    }                    
                    if (root.getName().startsWith("test-")) {
                        File[] kids = root.listFiles();
                        if (kids != null && kids.length > 0) {
                            uris.add(Utilities.toURI(root));
                        } else {
                            watcher.addWatchedPath(Utilities.toURI(root));
                        }
                    }
                }
            }
        }

        String[] buildHelpers = PluginPropertyUtils.getPluginPropertyList(this,
                "org.codehaus.mojo", //NOI18N
                "build-helper-maven-plugin", "sources", "source", test ? "add-test-source" : "add-source"); //NOI18N
        if (buildHelpers != null && buildHelpers.length > 0) {
            File root = FileUtil.toFile(getProjectDirectory());
            for (String helper : buildHelpers) {
                uris.add(FileUtilities.getDirURI(root, helper));
            }
        }

        return uris.toArray(new URI[uris.size()]);
    }

    public URI getWebAppDirectory() {
        //TODO hack, should be supported somehow to read this..
        String prop = PluginPropertyUtils.getPluginProperty(this, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_WAR, //NOI18N
                "warSourceDirectory", //NOI18N
                "war", null); //NOI18N

        prop = prop == null ? "src/main/webapp" : prop; //NOI18N

        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI getSiteDirectory() {
        //TODO hack, should be supported somehow to read this..
        String prop = PluginPropertyUtils.getPluginProperty(this, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_SITE, //NOI18N
                "siteDirectory", //NOI18N
                "site", null); //NOI18N

        prop = prop == null ? "src/site" : prop; //NOI18N

        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI getEarAppDirectory() {
        //TODO hack, should be supported somehow to read this..
        String prop = PluginPropertyUtils.getPluginProperty(this, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_EAR, //NOI18N
                "earSourceDirectory", //NOI18N
                "ear", null); //NOI18N

        prop = prop == null ? "src/main/application" : prop; //NOI18N

        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI[] getResources(boolean test) {
        List<URI> toRet = new ArrayList<URI>();
        URI projectroot = getProjectDirectory().toURI();
        Set<URI> sourceRoots = null;
        List<Resource> res = test ? getOriginalMavenProject().getTestResources() : getOriginalMavenProject().getResources();
        LBL : for (Resource elem : res) {
            String dir = elem.getDirectory();
            if (dir == null) {
                continue; // #191742
            }
            URI uri = FileUtilities.getDirURI(getProjectDirectory(), dir);
            if (elem.getTargetPath() != null || !elem.getExcludes().isEmpty() || !elem.getIncludes().isEmpty()) {
                URI rel = projectroot.relativize(uri);
                if (rel.isAbsolute()) { //outside of project directory
                    continue;// #195928, #231517
                }
                if (sourceRoots == null) {
                    sourceRoots = new HashSet<URI>();
                    sourceRoots.addAll(Arrays.asList(getSourceRoots(true)));
                    sourceRoots.addAll(Arrays.asList(getSourceRoots(false)));
                    //should we also consider generated sources? most like not necessary
                }
                for (URI sr : sourceRoots) {
                    if (!uri.relativize(sr).isAbsolute()) {
                        continue LBL;// #195928, #231517
                    }
                }
                //hope for the best now
            }
//            if (new File(uri).exists()) {
            toRet.add(uri);
//            }
        }
        return toRet.toArray(new URI[toRet.size()]);
    }

    public File[] getOtherRoots(boolean test) {
        URI uri = FileUtilities.getDirURI(getProjectDirectory(), test ? "src/test" : "src/main"); //NOI18N
        Set<File> toRet = new HashSet<File>();
        File fil = Utilities.toFile(uri);
        if (fil.exists()) {
            File[] fls = fil.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    //TODO most probably a performance bottleneck of sorts..
                    if ("java".equalsIgnoreCase(name) ||
                            // XXX this cannot continue in a modular system! rethink "other root" system
                            "webapp".equalsIgnoreCase(name)) {
                        return false;
                    }
                    for (JavaLikeRootProvider rp : getLookup().lookupAll(JavaLikeRootProvider.class)) {
                        if (rp.kind().equalsIgnoreCase(name)) {
                            return false;
                        }
                    }
                    return VisibilityQuery.getDefault().isVisible(new File(dir, name));
                }
            });
            if (fls != null) { //#166709 listFiles() shall not return null for existing folders
                // but somehow it does, maybe IO problem? do a proper null check.
                toRet.addAll(Arrays.asList(fls));
            }
        }
        URI[] res = getResources(test);
        for (URI rs : res) {
            File fl = Utilities.toFile(rs);
            //in node view we need only the existing ones, if anything else needs all,
            // a new method is probably necessary..
            if (fl.exists()) {
                toRet.add(fl);
            }
        }
        return toRet.toArray(new File[0]);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }
    
    CopyResourcesOnSave getCopyOnSaveResources() {
        synchronized (COPYRESOURCES_LOCK) {
            if (copyResourcesOnSave == null) {
                copyResourcesOnSave = new CopyResourcesOnSave(watcher, this);
            }
            return copyResourcesOnSave;
        }
    }

    private static class PackagingTypeDependentLookup extends ProxyLookup implements PropertyChangeListener {

        private final NbMavenProject watcher;
        private String packaging;
        private final Lookup general;

        @SuppressWarnings("LeakingThisInConstructor")
        PackagingTypeDependentLookup(NbMavenProject watcher) {
            this.watcher = watcher;
            //needs to be kept around to prevent recreating instances
            general = Lookups.forPath("Projects/org-netbeans-modules-maven/Lookup"); //NOI18N
            check();
            watcher.addPropertyChangeListener(this);
        }

        private void check() {
            //this call effectively calls project.getLookup(), when called in constructor will get back to the project's baselookup only.
            // but when called from propertyChange() then will call on entire composite lookup, is it a problem?  #230469
            String newPackaging = watcher.getPackagingType(); 
            if (newPackaging == null) {
                newPackaging = NbMavenProject.TYPE_JAR;
            }
            if (!newPackaging.equals(packaging)) {
                packaging = newPackaging;
                Lookup pack = Lookups.forPath("Projects/org-netbeans-modules-maven/" + packaging + "/Lookup");
                setLookups(general, pack);
            }
        }

        public @Override void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                check();
            }
        }
    }

    private Lookup createBasicLookup(ProjectState state, M2AuxilaryConfigImpl auxiliary) {
        return Lookups.fixed(
                    this,
                    fileObject,
                    auxiliary,
                    auxiliary.getProblemProvider(),
                    auxprops,
                    new MavenProjectPropsImpl.Merger(auxprops),
                    profileHandler,
                    configProvider,
                    problemReporter,
                    watcher,
                    state,
                    UILookupMergerSupport.createProjectOpenHookMerger(null),
                    UILookupMergerSupport.createPrivilegedTemplatesMerger(),
                    UILookupMergerSupport.createRecommendedTemplatesMerger(),
                    UILookupMergerSupport.createProjectProblemsProviderMerger(),
                    LookupProviderSupport.createSourcesMerger(),
                    ProjectClassPathModifier.extenderForModifier(this),
                    LookupMergerSupport.createClassPathModifierMerger());
    }

    //MEVENIDE-448 seems to help against creation of duplicate project instances
    // no idea why, it's supposed to be ProjectManager job.. maybe related to
    // maven impl of SubProjectProvider or FileOwnerQueryImplementation
    //TODO need to investigate why it's like that..
    @Override
    public int hashCode() {
        return getProjectDirectory().hashCode() * 13;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Project) {
            return getProjectDirectory().equals(((Project) obj).getProjectDirectory());
        }
        return false;
    }

    @Override
    public String toString() {
        return "Maven[" + fileObject.getPath() + "]"; //NOI18N

    }

    private class Updater implements FileChangeListener {

        private String[] filesToWatch;
        private long lastTime = 0;
        private FileObject folder;
        private final FileChangeListener listener = FileUtil.weakFileChangeListener(this, null);

        /** Relative file paths to watch. */
        Updater(String... toWatch) {
            Arrays.sort(toWatch);
            filesToWatch = toWatch;
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
        }

        @Override
        public void fileChanged(FileEvent fileEvent) {
            if (!fileEvent.getFile().isFolder()) {
                String nameExt = fileEvent.getFile().getNameExt();
                if (Arrays.binarySearch(filesToWatch, nameExt) != -1 && lastTime < fileEvent.getTime()) {
                    lastTime = System.currentTimeMillis();
//                    System.out.println("fired based on " + fileEvent.getFile() + fileEvent.getTime());
                    NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
                }
            }
        }

        @Override
        public void fileDataCreated(FileEvent fileEvent) {
            //TODO shall also include the parent of the pom if available..
            if (fileEvent.getFile().isFolder()) {
                String nameExt = fileEvent.getFile().getNameExt();
                if (Arrays.binarySearch(filesToWatch, nameExt) != -1 && lastTime < fileEvent.getTime()) {
                    lastTime = System.currentTimeMillis();
//                    System.out.println("fired based on " + fileEvent.getFile() + fileEvent.getTime());
                    fileEvent.getFile().addFileChangeListener(this);
                    NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
                }
            }
        }

        @Override
        public void fileDeleted(FileEvent fileEvent) {
            if (!fileEvent.getFile().isFolder()) {
                lastTime = System.currentTimeMillis();
                fileEvent.getFile().removeFileChangeListener(this);
                NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
            }
        }

        @Override
        public void fileFolderCreated(FileEvent fileEvent) {
            //TODO possibly remove this fire.. watch for actual path..
//            NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
        }

        @Override
        public void fileRenamed(FileRenameEvent fileRenameEvent) {
        }

        void attachAll(FileObject fo) {
            if (fo != null) {
                folder = fo;
                fo.addFileChangeListener(listener);
                for (String file : filesToWatch) {
                    FileObject fobj = fo.getFileObject(file);
                    if (fobj != null) {
                        fobj.addFileChangeListener(listener);
                    }
                }
            }
        }

        void detachAll() {
            if (folder != null) {
                folder.removeFileChangeListener(listener);
                for (String file : filesToWatch) {
                    FileObject fobj = folder.getFileObject(file);
                    if (fobj != null) {
                        fobj.removeFileChangeListener(listener);
                    }
                }
                folder = null;
            }
        }
    }

}
