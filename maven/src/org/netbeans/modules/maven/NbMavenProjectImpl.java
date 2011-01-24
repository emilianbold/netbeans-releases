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

import java.util.MissingResourceException;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.classpath.ClassPathProviderImpl;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.embedder.MavenSettingsSingleton;
import org.netbeans.modules.maven.execute.AbstractMavenExecutor;
import org.netbeans.modules.maven.problems.ProblemReporterImpl;
import org.netbeans.modules.maven.queries.MavenForBinaryQueryImpl;
import org.netbeans.modules.maven.queries.MavenSharabilityQueryImpl;
import org.netbeans.modules.maven.queries.MavenSourceLevelImpl;
import org.netbeans.modules.maven.queries.MavenTestForSourceImpl;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.ErrorManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.*;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Template;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.operations.OperationsImpl;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.ProjectProfileHandlerImpl;
import org.netbeans.modules.maven.cos.CosChecker;
import org.netbeans.modules.maven.debug.DebuggerChecker;
import org.netbeans.modules.maven.debug.MavenDebuggerImpl;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.netbeans.modules.maven.execute.BackwardCompatibilityWithMevenideChecker;
import org.netbeans.modules.maven.execute.DefaultReplaceTokenProvider;
import org.netbeans.modules.maven.execute.PrereqCheckerMerger;
import org.netbeans.modules.maven.execute.ReactorChecker;
import org.netbeans.modules.maven.problems.RevalidateAction;
import org.netbeans.modules.maven.queries.MavenAnnotationProcessingQueryImpl;
import org.netbeans.modules.maven.queries.MavenBinaryForSourceQueryImpl;
import org.netbeans.modules.maven.queries.MavenFileEncodingQueryImpl;
import org.netbeans.modules.maven.queries.MavenFileLocator;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.netbeans.spi.queries.SharabilityQueryImplementation;
import org.openide.awt.HtmlBrowser;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ProxyLookup;

/**
 * the ultimate source for all maven project like. Most code in mevenide takes this
 * class as parameter, there's always just one instance per projects.
 * @author  Milos Kleint
 */
public final class NbMavenProjectImpl implements Project {

    //TODO remove
    public static final String PROP_PROJECT = "MavenProject"; //NOI18N
    //TODO remove
    public static final String PROP_RESOURCE = "RESOURCES"; //NOI18N
    private static RequestProcessor RELOAD_RP = new RequestProcessor("Maven project reloading", 1); //NOI18N
    private FileObject fileObject;
    private FileObject folderFileObject;
    private final File projectFile;
    private final Lookup lookup;
    private Updater updater1;
    private Updater updater2;
    private Reference<MavenProject> project;
    private ProblemReporterImpl problemReporter;
    private final Info projectInfo;
    private final MavenSharabilityQueryImpl sharability;
    private final SubprojectProviderImpl subs;
    private final @NonNull NbMavenProject watcher;
    private final ProjectState state;
    private final M2ConfigProvider configProvider;
    private final ClassPathProviderImpl cppProvider;
//    private ConfigurationProviderEnabler configEnabler;
    private final M2AuxilaryConfigImpl auxiliary;
    private final MavenProjectPropsImpl auxprops;
    private ProjectProfileHandlerImpl profileHandler;
    public static WatcherAccessor ACCESSOR = null;

    static {
        // invokes static initializer of ModelHandle.class
        // that will assign value to the ACCESSOR field above
        Class<?> c = NbMavenProject.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
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
    NbMavenProjectImpl(FileObject folder, FileObject projectFO, File projectFile, ProjectState projectState) throws Exception {
        this.projectFile = projectFile;
        fileObject = projectFO;
        folderFileObject = folder;
        projectInfo = new Info();
        sharability = new MavenSharabilityQueryImpl(this);
        watcher = ACCESSOR.createWatcher(this);
        subs = new SubprojectProviderImpl(this, watcher);
        lookup = new LazyLookup(this, watcher, projectInfo, sharability, subs, fileObject);
        updater1 = new Updater();
        updater2 = new Updater(USER_DIR_FILES);
        state = projectState;
        problemReporter = new ProblemReporterImpl(this);
        auxiliary = new M2AuxilaryConfigImpl(this);
        auxprops = new MavenProjectPropsImpl(auxiliary, this);
        profileHandler = new ProjectProfileHandlerImpl(this, auxiliary);
        configProvider = new M2ConfigProvider(this, auxiliary, profileHandler);
        cppProvider = new ClassPathProviderImpl(this);
//        configEnabler = new ConfigurationProviderEnabler(this, auxiliary, profileHandler);
//        if (!SwingUtilities.isEventDispatchThread()) {
//            //#155766 sor of ugly, as not all (but the majority for sure) projects need
//            // a loaded maven project. But will protect from accidental loading in AWT
//            // thread.
//            getOriginalMavenProject();
//        }
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
    public MavenProject loadMavenProject(MavenEmbedder embedder, List<String> activeProfiles, Properties properties) {
        try {
            MavenExecutionRequest req = embedder.createMavenExecutionRequest();
            req.addActiveProfiles(activeProfiles);
            req.setPom(projectFile);
            req.setNoSnapshotUpdates(true);
            req.setUpdateSnapshots(false);
            Properties props = createSystemPropsForProjectLoading();
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
            MavenExecutionResult res = embedder.readProjectWithDependencies(req);
            if (!res.hasExceptions()) {
                return res.getProject();
            } else {
                List<Throwable> exc = res.getExceptions();
                for (Throwable ex : exc) {
                    Logger.getLogger(NbMavenProjectImpl.class.getName()).log(Level.FINE, "Exception thrown while loading maven project at " + getProjectDirectory(), ex); //NOI18N
                }
            }
        } catch (RuntimeException exc) {
            //guard against exceptions that are not processed by the embedder
            //#136184 NumberFormatException
            Logger.getLogger(NbMavenProjectImpl.class.getName()).log(Level.INFO, "Runtime exception thrown while loading maven project at " + getProjectDirectory(), exc); //NOI18N
        }
        return getFallbackProject();
    }

    public List<String> getCurrentActiveProfiles() {
        List<String> toRet = new ArrayList<String>();
        toRet.addAll(configProvider.getActiveConfiguration().getActivatedProfiles());
        return toRet;
    }
    private static final Properties statics = new Properties();

    private static Properties cloneStaticProps() {
        synchronized (statics) {
            if (statics.isEmpty()) { // not yet initialized
                // Now a misnomer, but available to activate profiles only during NB project parse:
                statics.setProperty("netbeans.execution", "true"); // NOI18N
                EmbedderFactory.fillEnvVars(statics);
                statics.putAll(AbstractMavenExecutor.excludeNetBeansProperties(System.getProperties()));
            }
            Properties toRet = new Properties();
            toRet.putAll(statics);
            return toRet;
        }
    }

    //#158700
    private Properties createSystemPropsForProjectLoading() {
        Properties props = cloneStaticProps();
        props.putAll(configProvider.getActiveConfiguration().getProperties());
        //TODO the properties for java.home and maybe others shall be relevant to the project setup not ide setup.
        // we got a chicken-egg situation here, the jdk used in project can be defined in the pom.xml file.
        return props;
    }

    //#172952 for property expression resolution we need this to include
    // the properties of the platform to properly resolve stuff like com.sun.boot.class.path
    public Properties createSystemPropsForPropertyExpressions() {
        Properties props = cloneStaticProps();
        props.putAll(cppProvider.getJavaPlatform().getSystemProperties());
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
            mp = loadOriginalMavenProject(true);
        }
        project = new SoftReference<MavenProject>(mp);
        return mp;
    }

    private @NonNull MavenProject loadOriginalMavenProject(boolean fallbackOnline) {
        long startLoading = System.currentTimeMillis();
        MavenProject newproject = null;
        try {
//                ProgressTransferListener.setAggregateHandle(hndl);
//                hndl.start();
           final  MavenExecutionRequest req = getEmbedder().createMavenExecutionRequest();
                
            //#172526 have the modellineage cache reset at the same time the project cache resets
            profileHandler.clearLineageCache();
            req.addActiveProfiles(getCurrentActiveProfiles());
            req.setPom(projectFile);
            req.setNoSnapshotUpdates(true);
            req.setUpdateSnapshots(false);
            //MEVENIDE-634 i'm wondering if this fixes the issue
            req.setInteractiveMode(false);
            // recursive == false is important to avoid checking all submodules for extensions
            // that will not be used in current pom anyway..
            // #135070
            req.setRecursive(false);
            req.setOffline(true);
            req.setUserProperties(createSystemPropsForProjectLoading());
             MavenExecutionResult res = getEmbedder().readProjectWithDependencies(req);
             newproject = res.getProject();
            if (res.hasExceptions()) {
                //#189833 
                if (fallbackOnline) {
                    ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                            NbBundle.getMessage(NbMavenProjectImpl.class, "LBL_Incomplete_Problem_Report"),
                            NbBundle.getMessage(NbMavenProjectImpl.class, "LBL_Incomplete_Problem_Report_Desc"), null);
                    problemReporter.addReport(report);
                    req.setTransferListener(ProgressTransferListener.activeListener());
                    RELOAD_RP.post(new Runnable() {

                        @Override
                        public void run() {
                            req.setOffline(false);
                            AggregateProgressHandle hndl = AggregateProgressFactory.createHandle(NbBundle.getMessage(
                                    NbMavenProjectImpl.class, "LBL_Incomplete_Loading_Progress_Handle", projectFile.getParent()),
                                                new ProgressContributor[] {AggregateProgressFactory.createProgressContributor("fallbackOnline") },  //NOI18N
                                                    ProgressTransferListener.cancellable(), null);
                            try {
                                ProgressTransferListener.setAggregateHandle(hndl);
                                hndl.start();
                                MavenExecutionResult res = EmbedderFactory.getOnlineEmbedder().readProjectWithDependencies(req);
                                MavenProject newOnlinewproject = res.getProject();
                                problemReporter.clearReports();
                                if(newOnlinewproject ==null ){
                                     newOnlinewproject = getFallbackProject();
                                }
                                synchronized (NbMavenProjectImpl.this) {
                                  project = new SoftReference<MavenProject>(newOnlinewproject);
                                  if (res.hasExceptions()) {
                                       reportExceptions(res);
                                  }
                                  projectInfo.reset();
                                }
                                ACCESSOR.doFireReload(watcher);
                                doBaseProblemChecks();
                           } catch (ThreadDeath d) { // download interrupted
                           } finally {
                               hndl.finish();
                               ProgressTransferListener.clearAggregateHandle();
                           }
        
                        }
                    });
                } else {
                    reportExceptions(res);
                }

            }
        } catch (RuntimeException exc) {
            //guard against exceptions that are not processed by the embedder
            //#136184 NumberFormatException
            Logger.getLogger(NbMavenProjectImpl.class.getName()).log(Level.INFO, "Runtime exception thrown while loading maven project at " + getProjectDirectory(), exc); //NOI18N
            StringWriter wr = new StringWriter();
            PrintWriter pw = new PrintWriter(wr);
            exc.printStackTrace(pw);
            pw.flush();

            ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                    NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_RuntimeException"),
                    NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_RuntimeExceptionLong") + wr.toString(), null);
            problemReporter.addReport(report);

        } finally {
            if (newproject == null) {
                newproject = getFallbackProject();
                if(fallbackOnline){
                    newproject.setName(NbBundle.getMessage(NbMavenProjectImpl.class, "LBL_Incomplete_Project_Name"));
                    newproject.setDescription(NbBundle.getMessage(NbMavenProjectImpl.class, "LBL_Incomplete_Project_Desc"));
                }
            }
            long endLoading = System.currentTimeMillis();
            Logger logger = Logger.getLogger(NbMavenProjectImpl.class.getName());
            logger.fine("Loaded project in " + ((endLoading - startLoading) / 1000) + " s at " + getProjectDirectory().getPath());
            if (logger.isLoggable(Level.FINE) && SwingUtilities.isEventDispatchThread()) {
                logger.log(Level.FINE, "Project " + getProjectDirectory().getPath() + " loaded in AWT event dispatching thread!", new RuntimeException());
            }
        }
        assert newproject != null;
        return newproject;
    }

    private MavenProject getFallbackProject() throws AssertionError {
        MavenProject newproject;
        File fallback = InstalledFileLocator.getDefault().locate("modules/ext/maven/fallback_pom.xml", "org.netbeans.modules.maven.embedder", false); //NOI18N //NOI18N
        if (fallback != null) {
            try {
                newproject = getEmbedder().readProject(fallback);
            } catch (Exception x) {
                throw new AssertionError(x);
            }
        } else { // from a unit test
            newproject = new MavenProject();
        }
        return newproject;
    }

    private void reportExceptions(MavenExecutionResult res) throws MissingResourceException {
        for (Throwable e : res.getExceptions()) {
            Logger.getLogger(NbMavenProjectImpl.class.getName()).log(Level.FINE, "Error on loading project " + projectFile, e); //NOI18N
            String msg = e.getMessage();
            if (e instanceof ArtifactResolutionException) {
                ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                        NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_Artifact_Resolution_problem"), msg, null);
                problemReporter.addReport(report);
            } else if (e instanceof ArtifactNotFoundException) {
                ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                        NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_Artifact_Not_Found"), msg, null);
                problemReporter.addReport(report);
            } else if (e instanceof ProjectBuildingException) {
                //igonre if the problem is in the project validation codebase, we handle that later..
                problemReporter.addReport(new ProblemReport(ProblemReport.SEVERITY_HIGH,
                        NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_Cannot_Load_Project"), msg, new RevalidateAction(this)));
            } else {
                Logger.getLogger(NbMavenProjectImpl.class.getName()).log(Level.INFO, "Exception thrown while loading maven project at " + getProjectDirectory(), e); //NOI18N
                ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                        "Error reading project model", msg, null);
                problemReporter.addReport(report);
            }
        }
    }

    public void fireProjectReload() {
        //#149566 prevent project firing squads to execute under project mutex.
        if (ProjectManager.mutex().isReadAccess()
                || ProjectManager.mutex().isWriteAccess()
                || SwingUtilities.isEventDispatchThread()) {
            RELOAD_RP.post(new Runnable() {

                @Override
                public void run() {
                    fireProjectReload();
                }
            });
            return;
        }
//        clearProjectWorkspaceCache();
        problemReporter.clearReports(); //#167741 -this will trigger node refresh?
        MavenProject prj = loadOriginalMavenProject(false);
        synchronized (this) {
            project = new SoftReference<MavenProject>(prj);
        }
        ACCESSOR.doFireReload(watcher);
        projectInfo.reset();
        doBaseProblemChecks();
    }

//    public void clearProjectWorkspaceCache() {
//        //when project gets reloaded (pom.xml file changed, build finished)
//        //we need to dump the weakly referenced caches and start with a clean room
//        try {
//            MavenWorkspaceStore store = (MavenWorkspaceStore) getEmbedder().getPlexusContainer().lookup("org.apache.maven.workspace.MavenWorkspaceStore"); //NOI18N
//            if (store instanceof NbMavenWorkspaceStore) {
//                ((NbMavenWorkspaceStore)store).doManualClear();
//            }
//        } catch (ComponentLookupException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }
    public static void refreshLocalRepository(NbMavenProjectImpl project) {
        String basedir = project.getEmbedder().getLocalRepository().getBasedir();
        File file = FileUtil.normalizeFile(new File(basedir));
        FileUtil.refreshFor(file);
    }

    void doBaseProblemChecks() {
        problemReporter.doBaseProblemChecks(getOriginalMavenProject());
    }

    public String getDisplayName() {
        String displayName = projectInfo.getDisplayName();
        if (displayName == null) {
            displayName = NbBundle.getMessage(NbMavenProjectImpl.class, "LBL_NoProjectName");
        }
        return displayName;
    }

    public String getShortDescription() {
        String desc = null;
        if (desc == null) {
            desc = getOriginalMavenProject().getDescription();
        }
        if (desc == null) {
            desc = NbBundle.getMessage(NbMavenProjectImpl.class, "LBL_DefaultDescription");
        }
        return desc;
    }

    Updater getProjectFolderUpdater() {
        return updater1;
    }

    Updater getUserFolderUpdater() {
        return updater2;
    }
    private static Map<String, String> pkg2Icon = Collections.unmodifiableMap(new HashMap<String, String>() {

        {
            put(NbMavenProject.TYPE_JAR, "org/netbeans/modules/maven/resources/jaricon.png"); //NOI18N
            put(NbMavenProject.TYPE_WAR, "org/netbeans/modules/maven/resources/maven_web_application_16.png"); //NOI18N
            put(NbMavenProject.TYPE_EJB, "org/netbeans/modules/maven/resources/maven_ejb_module_16.png"); //NOI18N
            put(NbMavenProject.TYPE_EAR, "org/netbeans/modules/maven/resources/maven_enterprise_application_16.png"); //NOI18N
            put(NbMavenProject.TYPE_POM, "org/netbeans/modules/maven/resources/Maven2Icon.gif"); //NOI18N
            put(NbMavenProject.TYPE_NBM, "org/netbeans/modules/maven/resources/nbmicon.png"); //NOI18N
            put(NbMavenProject.TYPE_OSGI, "org/netbeans/modules/maven/resources/maven_osgi_16.png"); //NOI18N
            put(NbMavenProject.TYPE_NBM_APPLICATION, "org/netbeans/modules/maven/resources/suiteicon.png"); //NOI18N
        }
    });

    public static String getIconPath(String packaging) {
        return pkg2Icon.get(packaging);
    }

    public String getName() {
        return getOriginalMavenProject().getId().replace(':', '_');
    }
    /**
     * TODO move elsewhere?
     */
    private static ContextAwareAction refreshAction;

    @ActionID(id = "org.netbeans.modules.maven.refresh", category = "Project")
    @ActionRegistration(displayName = "#ACT_Reload_Project")
    @ActionReference(position = 1700, path = "Projects/org-netbeans-modules-maven/Actions")
    public static ContextAwareAction createRefreshAction() {
        if (refreshAction == null) {
            refreshAction = new RefreshAction(Lookup.EMPTY);
        }
        return refreshAction;
    }

    /**
     * the root dirtectory of the project.. that;s where the pom resides.
     */
    @Override
    public FileObject getProjectDirectory() {
        return folderFileObject;
    }

    public FileObject getHomeDirectory() {
        File homeFile = MavenSettingsSingleton.getInstance().getM2UserDir();

        FileObject home = null;
        try {
            home = FileUtil.createFolder(homeFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (home == null) {
            //TODO this is a problem, probably UNC path on windows - MEVENIDE-380
            // some functionality won't work
            ErrorManager.getDefault().log("Cannot convert home dir to FileObject, some functionality won't work. It's usually the case on Windows and UNC paths. The path is " + homeFile); //NOI18N

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
        //        embedder.setLocalRepositoryDirectory(FileUtil.toFile(getRepositoryRoot()));
        String toRet = getEmbedder().getLocalRepository().pathOf(artifact);
        return toRet;
    }

    public MavenEmbedder getEmbedder() {
        return EmbedderFactory.getProjectEmbedder();
    }

    public MavenProjectPropsImpl getAuxProps() {
        return auxprops;
    }

    public URI[] getSourceRoots(boolean test) {
        List<String> srcs = new ArrayList<String>();
        List<String> s1 = test ? getOriginalMavenProject().getTestCompileSourceRoots() : getOriginalMavenProject().getCompileSourceRoots();
        srcs.addAll(s1);
        if (!test && getProjectDirectory().getFileObject("src/main/aspect") != null) { //NOI18N
            srcs.add(FileUtil.toFile(getProjectDirectory().getFileObject("src/main/aspect")).getAbsolutePath()); //NOI18N
        }

        URI[] uris = new URI[srcs.size() + 2];
        int count = 0;
        for (String str : srcs) {
            uris[count] = FileUtilities.convertStringToUri(str);
            count = count + 1;
        }
        uris[uris.length - 2] = getScalaDirectory(test);
        uris[uris.length - 1] = getGroovyDirectory(test);
        return uris;
    }

    public URI[] getGeneratedSourceRoots() {
        //TODO more or less a hack.. should be better supported by embedder itself.
        URI uri = FileUtilities.getDirURI(getProjectDirectory(), "target/generated-sources"); //NOI18N
        Set<URI> uris = new HashSet<URI>();
        File[] roots = new File(uri).listFiles();
        if (roots != null) {
            for (File root : roots) {
                File[] kids = root.listFiles();
                if (kids != null && /* #190626 */kids.length > 0) {
                    uris.add(root.toURI());
                }
            }
        }

        String[] buildHelpers = PluginPropertyUtils.getPluginPropertyList(this,
                "org.codehaus.mojo", //NOI18N
                "build-helper-maven-plugin", "sources", "source", "add-source"); //NOI18N //TODO split for sources and test sources..
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
                "war"); //NOI18N

        prop = prop == null ? "src/main/webapp" : prop; //NOI18N

        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI getSiteDirectory() {
        //TODO hack, should be supported somehow to read this..
        String prop = PluginPropertyUtils.getPluginProperty(this, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_SITE, //NOI18N
                "siteDirectory", //NOI18N
                "site"); //NOI18N

        prop = prop == null ? "src/site" : prop; //NOI18N

        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI getEarAppDirectory() {
        //TODO hack, should be supported somehow to read this..
        String prop = PluginPropertyUtils.getPluginProperty(this, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_EAR, //NOI18N
                "earSourceDirectory", //NOI18N
                "ear"); //NOI18N

        prop = prop == null ? "src/main/application" : prop; //NOI18N

        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI getScalaDirectory(boolean test) {
        //TODO hack, should be supported somehow to read this..
        String prop = PluginPropertyUtils.getPluginProperty(getOriginalMavenProject(), "org.scala.tools",
                "scala-maven-plugin", //NOI18N
                "sourceDir", //NOI18N
                "compile"); //NOI18N

        prop = prop == null ? (test ? "src/test/scala" : "src/main/scala") : prop; //NOI18N

        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI getGroovyDirectory(boolean test) {
        String prop = test ? "src/test/groovy" : "src/main/groovy"; //NOI18N
        return FileUtilities.getDirURI(getProjectDirectory(), prop);
    }

    public URI[] getResources(boolean test) {
        List<URI> toRet = new ArrayList<URI>();
        List<Resource> res = test ? getOriginalMavenProject().getTestResources() : getOriginalMavenProject().getResources();
        for (Resource elem : res) {
            String dir = elem.getDirectory();
            if (dir == null) {
                continue; // #191742
            }
            URI uri = FileUtilities.getDirURI(getProjectDirectory(), dir);
//            if (new File(uri).exists()) {
            toRet.add(uri);
//            }
        }
        return toRet.toArray(new URI[toRet.size()]);
    }

    public File[] getOtherRoots(boolean test) {
        URI uri = FileUtilities.getDirURI(getProjectDirectory(), test ? "src/test" : "src/main"); //NOI18N
        Set<File> toRet = new HashSet<File>();
        File fil = new File(uri);
        if (fil.exists()) {
            File[] fls = fil.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    //TODO most probably a performance bottleneck of sorts..
                    return !("java".equalsIgnoreCase(name)) && //NOI18N
                            !("webapp".equalsIgnoreCase(name)) && //NOI18N
                            !("groovy".equalsIgnoreCase(name)) && //NOI18N
                            !("scala".equalsIgnoreCase(name)) //NOI18N
                            && VisibilityQuery.getDefault().isVisible(new File(dir, name));
                }
            });
            if (fls != null) { //#166709 listFiles() shall not return null for existing folders
                // but somehow it does, maybe IO problem? do a proper null check.
                toRet.addAll(Arrays.asList(fls));
            }
        }
        URI[] res = getResources(test);
        for (URI rs : res) {
            File fl = new File(rs);
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

    // in 6.5 the ProjectInformation icon is used in project open dialog.
    // however we don't want this call to initiate the comple lookup of the project
    //as that's time consuming and suboptimal to do for all projects in the filechooser.
    private class LazyLookup extends ProxyLookup {

        private Lookup lookup;
        boolean initialized = false;

        LazyLookup(Project ths, NbMavenProject watcher, ProjectInformation info,
                SharabilityQueryImplementation shara, SubprojectProvider subs, FileObject projectFO) {
            setLookups(Lookups.fixed(ths, watcher, info, shara, subs, projectFO));
        }

        protected @Override void beforeLookup(Template<?> template) {
            synchronized (NbMavenProjectImpl.this) {
            if (!initialized
                    && (!(ProjectInformation.class.equals(template.getType())
                    || NbMavenProject.class.equals(template.getType())
                    || NbMavenProjectImpl.class.equals(template.getType())
                    || Project.class.equals(template.getType())
                    || SharabilityQueryImplementation.class.equals(template.getType())
                    || SubprojectProvider.class.equals(template.getType())))) {
                initialized = true;
                lookup = createBasicLookup();
                setLookups(lookup);
                Lookup lkp = LookupProviderSupport.createCompositeLookup(new PackagingTypeDependentLookup(watcher, lookup), "Projects/org-netbeans-modules-maven/Lookup");
                assert checkForForbiddenMergers(lkp) : "Cannot have a LookupMerger for ProjectInformation or SharabilityQueryImplementation";
                setLookups(lkp); //NOI18N
            }
            }
            super.beforeLookup(template);
        }
    }

    private static class PackagingTypeDependentLookup extends ProxyLookup implements PropertyChangeListener {

        private final NbMavenProject watcher;
        private final Lookup lookup;
        private String packaging;

        @SuppressWarnings("LeakingThisInConstructor")
        PackagingTypeDependentLookup(NbMavenProject watcher, Lookup lookup) {
            this.watcher = watcher;
            this.lookup = lookup;
            check();
            watcher.addPropertyChangeListener(this);
        }

        private void check() {
            String newPackaging = watcher.getPackagingType();
            if (newPackaging == null) {
                newPackaging = NbMavenProject.TYPE_JAR;
            }
            if (!newPackaging.equals(packaging)) {
                packaging = newPackaging;
                setLookups(LookupProviderSupport.createCompositeLookup(lookup, "Projects/org-netbeans-modules-maven/" + packaging + "/Lookup"));
            }
        }

        public @Override void propertyChange(PropertyChangeEvent evt) {
            if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                check();
            }
        }
    }

    //to be called from assert,
    // chekc for items we optimize for at startup.
    private boolean checkForForbiddenMergers(Lookup lkp) {
        Collection<? extends LookupMerger> res = lkp.lookupAll(LookupMerger.class);
        for (LookupMerger lm : res) {
            if (ProjectInformation.class.equals(lm.getMergeableClass())) {
                return false;
            }
            if (SharabilityQueryImplementation.class.equals(lm.getMergeableClass())) {
                return false;
            }
            if (SubprojectProvider.class.equals(lm.getMergeableClass())) {
                return false;
            }
            if (NbMavenProject.class.equals(lm.getMergeableClass())) {
                return false;
            }
        }
        return true;
    }

    private Lookup createBasicLookup() {
        CPExtender extender = new CPExtender(this);
        Lookup staticLookup = Lookups.fixed(new Object[]{
                    projectInfo,
                    this,
                    fileObject,
                    new CacheDirProvider(this),
                    new MavenForBinaryQueryImpl(this),
                    new MavenBinaryForSourceQueryImpl(this),
                    new ActionProviderImpl(this),
                    auxiliary,
                    auxprops,
                    new MavenProjectPropsImpl.Merger(auxprops),
                    profileHandler,
                    configProvider,
                    new CustomizerProviderImpl(this),
                    new LogicalViewProviderImpl(this),
                    cppProvider,
                    sharability,
                    new MavenTestForSourceImpl(this),
                    ////            new MavenFileBuiltQueryImpl(this),
                    subs,
                    new MavenSourcesImpl(this),
                    new RecommendedTemplatesImpl(this),
                    new MavenSourceLevelImpl(this),
                    new MavenAnnotationProcessingQueryImpl(this),
                    problemReporter,
                    watcher,
                    new MavenFileEncodingQueryImpl(this),
                    new TemplateAttrProvider(this),
                    //operations
                    new OperationsImpl(this, state),
                    //                    configEnabler,
                    new MavenDebuggerImpl(this),
                    new DefaultReplaceTokenProvider(this),
                    new MavenFileLocator(this),
                    new ProjectOpenedHookImpl(this),
                    // default mergers..        
                    UILookupMergerSupport.createPrivilegedTemplatesMerger(),
                    UILookupMergerSupport.createRecommendedTemplatesMerger(),
                    LookupProviderSupport.createSourcesMerger(),
                    new CPExtenderLookupMerger(extender),
                    new CPModifierLookupMerger(extender),
                    new BackwardCompatibilityWithMevenideChecker(),
                    new DebuggerChecker(),
                    new CosChecker(this),
                    CosChecker.createResultChecker(),
                    CosChecker.createCoSHook(this),
                    new ReactorChecker(),
                    new PrereqCheckerMerger(),
                    new TestSkippingChecker(),
                    new RecommendedTemplates() {

                        public String[] getRecommendedTypes() {
                            return new String[]{"scala-classes"}; //NOI18N
                        }
                    }
                });
        return staticLookup;
    }

    public boolean isErrorPom(MavenProject pr) {
        if ("error".equals(pr.getArtifactId()) && "error".equals(pr.getGroupId())) {
            return true;
        }
        return false;
    }

    private final class Info implements ProjectInformation {

        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        Info() {
        }

        public void reset() {
            firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
            pcs.firePropertyChange(ProjectInformation.PROP_ICON, null, getIcon());
        }

        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }

        @Override
        public String getName() {
            String toReturn = NbMavenProjectImpl.this.getName();
            return toReturn;
        }

        @Override
        public String getDisplayName() {
            MavenProject pr = NbMavenProjectImpl.this.getOriginalMavenProject();
            if (isErrorPom(pr)) {
                return NbBundle.getMessage(NbMavenProjectImpl.class, "LBL_misconfigured_project", getProjectDirectory().getNameExt());
            }
            String toReturn = pr.getName();
            if (toReturn == null) {
                String grId = pr.getGroupId();
                String artId = pr.getArtifactId();
                if (grId != null && artId != null) {
                    toReturn = grId + ":" + artId; //NOI18N

                } else {
                    toReturn = NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_Maven_project_at", NbMavenProjectImpl.this.getProjectDirectory().getPath());
                }
            }
            return toReturn;
        }

        @Override
        public Icon getIcon() {
            MavenProject pr = NbMavenProjectImpl.this.getOriginalMavenProject();
            String iconPath = getIconPath(pr.getPackaging());
            if (iconPath == null) {
                iconPath = "org/netbeans/modules/maven/resources/Maven2Icon.gif"; //NOI18N
            }
            return ImageUtilities.loadImageIcon(iconPath, true);
        }

        @Override
        public Project getProject() {
            return NbMavenProjectImpl.this;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
    }
    // needs to be binary sorted;
    private static final String[] DEFAULT_FILES = new String[]{
        "nb-configuration.xml", //NOI18N
        "pom.xml",//NOI18N
        "profiles.xml"//NOI18N
    };
    private static final String[] USER_DIR_FILES = new String[]{
        "settings.xml" //NOI18N
    };

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

    class Updater implements FileChangeListener {

        //        private FileObject fileObject;
        private String[] filesToWatch;
        private long lastTime = 0;
        private FileObject folder;

        Updater() {
            this(DEFAULT_FILES);
        }

        Updater(String[] toWatch) {
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
                fo.addFileChangeListener(this);
                for (String file : filesToWatch) {
                    FileObject fobj = fo.getFileObject(file);
                    if (fobj != null) {
                        fobj.addFileChangeListener(this);
                    }
                }
            }
        }

        void detachAll() {
            if (folder != null) {
                folder.removeFileChangeListener(this);
                for (String file : filesToWatch) {
                    FileObject fobj = folder.getFileObject(file);
                    if (fobj != null) {
                        fobj.removeFileChangeListener(this);
                    }
                }
                folder = null;
            }
        }
    }

    private static final class RecommendedTemplatesImpl
            implements RecommendedTemplates, PrivilegedTemplates {

        private static final String[] JAR_APPLICATION_TYPES = new String[]{
            "java-classes", // NOI18N
            "java-main-class", // NOI18N
            "java-forms", // NOI18N
            "gui-java-application", // NOI18N
            "java-beans", // NOI18N
            "oasis-XML-catalogs", // NOI18N
            "XML", // NOI18N
            "web-service-clients", // NOI18N
            "REST-clients", // NOI18N
            "wsdl", // NOI18N
            // "servlet-types",     // NOI18N
            // "web-types",         // NOI18N
            "junit", // NOI18N
            // "MIDP",              // NOI18N
            "simple-files" // NOI18N
        };
        private static final String[] JAR_PRIVILEGED_NAMES = new String[]{
            "Templates/Classes/Class.java", // NOI18N
            "Templates/Classes/Package", // NOI18N
            "Templates/Classes/Interface.java", // NOI18N
            "Templates/GUIForms/JPanel.java", // NOI18N
            "Templates/GUIForms/JFrame.java", // NOI18N
            "Templates/WebServices/WebServiceClient" // NOI18N
        };
        private static final String[] POM_APPLICATION_TYPES = new String[]{
            "XML", // NOI18N
            "simple-files" // NOI18N
        };
        private static final String[] POM_PRIVILEGED_NAMES = new String[]{
            "Templates/XML/XMLWizard", // NOI18N
            "Templates/Other/Folder" // NOI18N
        };
        private static final String[] ALL_TYPES = new String[]{
            "java-classes", // NOI18N
            "java-main-class", // NOI18N
            "java-forms", // NOI18N
            "java-beans", // NOI18N
            "j2ee-types", // NOI18N
            "gui-java-application", // NOI18N
            "java-beans", // NOI18N
            "oasis-XML-catalogs", // NOI18N
            "XML", // NOI18N
            "ant-script", // NOI18N
            "ant-task", // NOI18N
            //            "web-services",         // NOI18N
            "web-service-clients", // NOI18N
            "REST-clients", // NOI18N
            "wsdl", // NOI18N
            "servlet-types", // NOI18N
            "web-types", // NOI18N
            "junit", // NOI18N
            // "MIDP",              // NOI18N
            "simple-files", // NOI18N
            "ear-types", // NOI18N
        };
        private static final String[] GENERIC_WEB_TYPES = new String[]{
            "java-classes", // NOI18N
            "java-main-class", // NOI18N
            "java-beans", // NOI18N
            "oasis-XML-catalogs", // NOI18N
            "XML", // NOI18N
            "wsdl", // NOI18N
            "junit", // NOI18N
            "simple-files" // NOI18N
        };
        private static final String[] GENERIC_EJB_TYPES = new String[]{
            "java-classes", // NOI18N
            "wsdl", // NOI18N
            "java-beans", // NOI18N
            "java-main-class", // NOI18N
            "oasis-XML-catalogs", // NOI18N
            "XML", // NOI18N
            "junit", // NOI18N
            "simple-files" // NOI18N
        };
        private static final String[] GENERIC_EAR_TYPES = new String[]{
            "XML", //NOPMD      // NOI18N
            "wsdl", //NOPMD       // NOI18N
            "simple-files" //NOPMD       // NOI18N
        };
        private final List<String> prohibited;
        private final NbMavenProjectImpl project;

        RecommendedTemplatesImpl(NbMavenProjectImpl proj) {
            project = proj;
            prohibited = new ArrayList<String>();
            prohibited.add(NbMavenProject.TYPE_EAR);
            prohibited.add(NbMavenProject.TYPE_EJB);
            prohibited.add(NbMavenProject.TYPE_WAR);
            prohibited.add(NbMavenProject.TYPE_NBM);
            prohibited.add(NbMavenProject.TYPE_OSGI);
        }

        @Override
        public String[] getRecommendedTypes() {
            String packaging = project.getProjectWatcher().getPackagingType();
            if (packaging == null) {
                packaging = NbMavenProject.TYPE_JAR;
            }
            packaging = packaging.trim();
            if (NbMavenProject.TYPE_POM.equals(packaging)) {
                return POM_APPLICATION_TYPES;
            }
            if (NbMavenProject.TYPE_JAR.equals(packaging)) {
                return JAR_APPLICATION_TYPES;
            }

            if (NbMavenProject.TYPE_WAR.equals(packaging)) {
                return GENERIC_WEB_TYPES;
            }
            if (NbMavenProject.TYPE_EJB.equals(packaging)) {
                return GENERIC_EJB_TYPES;
            }
            if (NbMavenProject.TYPE_EAR.equals(packaging)) {
                return GENERIC_EAR_TYPES;
            }

            if (prohibited.contains(packaging)) {
                return new String[0];
            }

            // If packaging is unknown, any type of sources is recommanded.
            //TODO in future we probably can try to guess based on what plugins are
            // defined in the lifecycle.
            return ALL_TYPES;
        }

        @Override
        public String[] getPrivilegedTemplates() {
            String packaging = project.getProjectWatcher().getPackagingType();
            if (packaging == null) {
                packaging = NbMavenProject.TYPE_JAR;
            }
            packaging = packaging.trim();
            if (NbMavenProject.TYPE_POM.equals(packaging)) {
                return POM_PRIVILEGED_NAMES;
            }
            if (prohibited.contains(packaging)) {
                return new String[0];
            }
            return JAR_PRIVILEGED_NAMES;
        }
    }

    @SuppressWarnings("serial")
    private static class RefreshAction extends AbstractAction implements ContextAwareAction {

        private Lookup context;

        public RefreshAction(Lookup lkp) {
            context = lkp;
            Collection<? extends NbMavenProjectImpl> col = context.lookupAll(NbMavenProjectImpl.class);
            if (col.size() > 1) {
                putValue(Action.NAME, NbBundle.getMessage(NbMavenProjectImpl.class, "ACT_Reload_Projects", col.size()));
            } else {
                putValue(Action.NAME, NbBundle.getMessage(NbMavenProjectImpl.class, "ACT_Reload_Project"));
            }
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent event) {
            //#166919 - need to run in RP to prevent RPing later in fireProjectReload()
            RELOAD_RP.post(new Runnable() {

                @Override
                public void run() {
                    EmbedderFactory.resetProjectEmbedder();
                    for (NbMavenProjectImpl prj : context.lookupAll(NbMavenProjectImpl.class)) {
                        NbMavenProject.fireMavenProjectReload(prj);
                    }
                }
            });

        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new RefreshAction(actionContext);
        }
    }

    private String repositoryListToString(List<ArtifactRepository> repositories) {
        String toRet = "";
        if (repositories != null) {
            for (ArtifactRepository repo : repositories) {
                toRet = toRet + "      " + repo.getId() + "  (" + repo.getUrl() + ")\n"; //NOI18N
            }
        }
        return toRet;
    }

    private static Throwable getCause(Exception exc, Class exceptionClazz) {
        Throwable t = exc;
        while (t != null) {
            if (t != null && t.getClass().equals(exceptionClazz)) {
                return t;
            }
            t = t.getCause();
        }
        return null;
    }

    @SuppressWarnings("serial")
    private static class OpenWikiPage extends AbstractAction {

        private URL url;

        public OpenWikiPage(URL url) {
            putValue(Action.NAME, "Open Wiki page");
            this.url = url;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent event) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        }
    }
}
