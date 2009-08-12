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
package org.netbeans.modules.maven;

import java.net.MalformedURLException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.extension.ExtensionManagerException;
import org.apache.maven.extension.ExtensionScanningException;
import org.apache.maven.model.Resource;
import org.apache.maven.project.InvalidProjectModelException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.reactor.MissingModuleException;
import org.apache.maven.workspace.MavenWorkspaceStore;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.classpath.ClassPathProviderImpl;
import org.netbeans.modules.maven.customizer.CustomizerProviderImpl;
import org.netbeans.modules.maven.embedder.MavenSettingsSingleton;
import org.netbeans.modules.maven.execute.JarPackagingRunChecker;
import org.netbeans.modules.maven.execute.UserActionGoalProvider;
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
import org.netbeans.modules.maven.api.ProjectProfileHandler;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.ErrorManager;
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
import org.netbeans.modules.maven.cos.CosChecker;
import org.netbeans.modules.maven.debug.DebuggerChecker;
import org.netbeans.modules.maven.debug.MavenDebuggerImpl;
import org.netbeans.modules.maven.embedder.NbMavenWorkspaceStore;
import org.netbeans.modules.maven.execute.BackwardCompatibilityWithMevenideChecker;
import org.netbeans.modules.maven.execute.DefaultReplaceTokenProvider;
import org.netbeans.modules.maven.execute.PrereqCheckerMerger;
import org.netbeans.modules.maven.execute.ReactorChecker;
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
    private File projectFile;
    private Image icon;
    private final Lookup lookup;
    private Updater updater1;
    private Updater updater2;
    private MavenProject project;
    private ProblemReporterImpl problemReporter;
    private final Info projectInfo;
    private final MavenSharabilityQueryImpl sharability;
    private final SubprojectProviderImpl subs;
    private final NbMavenProject watcher;
    private final ProjectState state;
    private final M2ConfigProvider configProvider;

//    private ConfigurationProviderEnabler configEnabler;
    private final M2AuxilaryConfigImpl auxiliary;
    private final MavenProjectPropsImpl auxprops;
    private ProjectProfileHandler profileHandler;
    public static WatcherAccessor ACCESSOR = null;
    

    static {
        // invokes static initializer of ModelHandle.class
        // that will assign value to the ACCESSOR field above
        Class c = NbMavenProject.class;
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
        auxprops = new MavenProjectPropsImpl(auxiliary, watcher);
        profileHandler = new ProjectProfileHandlerImpl(this,auxiliary);
        configProvider = new M2ConfigProvider(this, auxiliary, profileHandler);
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

    public NbMavenProject getProjectWatcher() {
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
    public synchronized MavenProject loadMavenProject(MavenEmbedder embedder, List<String> activeProfiles, Properties properties) {
//        AggregateProgressHandle hndl = createDownloadHandle();
        try {
//            ProgressTransferListener.setAggregateHandle(hndl);
//            hndl.start();
            MavenExecutionRequest req = new DefaultMavenExecutionRequest();
//            ProgressTransferListener ptl = new ProgressTransferListener();
//            req.setTransferListener(ptl);

            req.addActiveProfiles(activeProfiles);
            req.setPomFile(projectFile.getAbsolutePath());
            req.setNoSnapshotUpdates(true);
            req.setUpdateSnapshots(false);
            Properties props = new Properties();
            if (properties != null) {
                props.putAll(properties);
                req.setUserProperties(props);
            }
            //MEVENIDE-634 i'm wondering if this fixes the issue
            req.setInteractiveMode(false);
            // recursive == false is important to avoid checking all submodules for extensions
            // that will not be used in current pom anyway..
            // #135070
            req.setRecursive(false);
            req.setProperties(createSystemPropsForProjectLoading());
            MavenExecutionResult res = embedder.readProjectWithDependencies(req);
            if (!res.hasExceptions()) {
                return res.getProject();
            } else {
                @SuppressWarnings("unchecked")
                List<Exception> exc = res.getExceptions();
                //TODO how to report to the user?
                for (Exception ex : exc) {
                    Logger.getLogger(NbMavenProjectImpl.class.getName()).log(Level.INFO, "Exception thrown while loading maven project at " + getProjectDirectory(), ex); //NOI18N
                }
            }
        } catch (RuntimeException exc) {
            //guard against exceptions that are not processed by the embedder
            //#136184 NumberFormatException
            Logger.getLogger(NbMavenProjectImpl.class.getName()).log(Level.INFO, "Runtime exception thrown while loading maven project at " + getProjectDirectory(), exc); //NOI18N
        } finally {
//            hndl.finish();
//            ProgressTransferListener.clearAggregateHandle();
        }
        File fallback = InstalledFileLocator.getDefault().locate("maven2/fallback_pom.xml", null, false); //NOI18N
        try {
            return embedder.readProject(fallback);
        } catch (Exception x) {
            // oh well..
            //NOPMD
        }
        return null;
    }

    public List<String> getCurrentActiveProfiles() {
        List<String> toRet = new ArrayList<String>();
        toRet.addAll(configProvider.getActiveConfiguration().getActivatedProfiles());
        return toRet;
    }

    //#158700
    private Properties createSystemPropsForProjectLoading() {
        Properties props = new Properties();
        props.setProperty("netbeans.execution", "true"); //NOI18N
        EmbedderFactory.fillEnvVars(props);
        props.putAll(AbstractMavenExecutor.excludeNetBeansProperties(System.getProperties()));
        //TODO the properties for java.home and maybe others shall be relevant to the project setup not ide setup.
        // we got a chicken-egg situation here, the jdk used in project can be defined in the pom.xml file.
        return props;
    }

//    private AggregateProgressHandle createDownloadHandle() {
//        AggregateProgressHandle hndl = AggregateProgressFactory.createSystemHandle(NbBundle.getMessage(NbMavenProject.class, "Progress_Download"),
//                            new ProgressContributor[] {
//                                AggregateProgressFactory.createProgressContributor("zaloha") },  //NOI18N
//                            null, null);
//        hndl.setInitialDelay(2000);
//        return hndl;
//    }

    /**
     * getter for the maven's own project representation.. this instance is cached but gets reloaded
     * when one the pom files have changed.
     */
    public synchronized MavenProject getOriginalMavenProject() {
        if (project == null) {
            project = loadOriginalMavenProject();
        }
        return project;
    }

    private MavenProject loadOriginalMavenProject() {
        long startLoading = System.currentTimeMillis();
        MavenProject newproject = null;
        try {
//                ProgressTransferListener.setAggregateHandle(hndl);
//                hndl.start();
            MavenExecutionRequest req = new DefaultMavenExecutionRequest();
//                ProgressTransferListener ptl = new ProgressTransferListener();
//                req.setTransferListener(ptl);
            req.addActiveProfiles(getCurrentActiveProfiles());
            req.setPomFile(projectFile.getAbsolutePath());
            req.setNoSnapshotUpdates(true);
            req.setUpdateSnapshots(false);
            //MEVENIDE-634 i'm wondering if this fixes the issue
            req.setInteractiveMode(false);
            // recursive == false is important to avoid checking all submodules for extensions
            // that will not be used in current pom anyway..
            // #135070
            req.setRecursive(false);
            req.setProperties(createSystemPropsForProjectLoading());
            MavenExecutionResult res = getEmbedder().readProjectWithDependencies(req);
            newproject = res.getProject();
            if (res.hasExceptions()) {
                for (Object e : res.getExceptions()) {
                    Logger.getLogger(NbMavenProjectImpl.class.getName()).log(Level.INFO, "Error on loading project " + projectFile.getAbsolutePath(), (Throwable) e); //NOI18N
                    if (e instanceof ArtifactResolutionException) {
                        ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                                NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_Artifact_Resolution_problem"),
                                ((Exception) e).getMessage(), null);
                        problemReporter.addReport(report);
                    } else if (e instanceof ArtifactNotFoundException) {
                        ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                                NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_Artifact_Not_Found"),
                                ((Exception) e).getMessage(), null);
                        problemReporter.addReport(report);
                    } else if (e instanceof InvalidProjectModelException) {
                        //validation failure..
                        problemReporter.addValidatorReports((InvalidProjectModelException) e);
                    } else if (e instanceof ProjectBuildingException) {
                        //igonre if the problem is in the project validation codebase, we handle that later..
                        problemReporter.addReport(new ProblemReport(ProblemReport.SEVERITY_HIGH,
                                NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_Cannot_Load_Project"),
                                ((Exception) e).getMessage(), null));
                    } else if (e instanceof MissingModuleException) {
                        MissingModuleException exc = (MissingModuleException) e;
                        ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                                NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_MissingSubmodule", exc.getModuleName()),
                                ((Exception) e).getMessage(), null);
                        problemReporter.addReport(report);
                    } else if (e instanceof ExtensionScanningException) {
                        ExtensionScanningException exc = (ExtensionScanningException) e;
                        String message = null;
                        String name = null;
                        String urlString = null;
                        Throwable cause = exc.getCause();
                        if (cause != null && cause instanceof ProjectBuildingException) {
                            //parent pom
                            name = NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_MissingParentPOM");
                            urlString = "http://wiki.netbeans.org/MavenMissingParentPomError"; //NOI18N
                            ProjectBuildingException pbe = (ProjectBuildingException) cause;
                            ArtifactNotFoundException anfe = (ArtifactNotFoundException) getCause(pbe, ArtifactNotFoundException.class);
                            if (anfe != null) {
                                message = NbBundle.getMessage(NbMavenProjectImpl.class, "DESC_MissingParentPOM",
                                        new String[]{
                                            anfe.getGroupId(),
                                            anfe.getArtifactId(),
                                            anfe.getVersion(),
                                            repositoryListToString(anfe.getRemoteRepositories())
                                        });
                            }
                        }
                        if (cause != null && cause instanceof ExtensionManagerException) {
                            //extension
                            name = NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_MissingExtensionOrPlugin");
                            urlString = "http://wiki.netbeans.org/MavenMissingExtensionPluginError"; //NOI18N
                            ExtensionManagerException eme = (ExtensionManagerException) cause;
                            ArtifactNotFoundException anfe = (ArtifactNotFoundException) getCause(eme, ArtifactNotFoundException.class);
                            if (anfe != null) {
                                message = NbBundle.getMessage(NbMavenProjectImpl.class, "DESC_MissingExtensionOrPlugin",
                                        new String[]{
                                            anfe.getGroupId(),
                                            anfe.getArtifactId(),
                                            anfe.getVersion(),
                                            repositoryListToString(anfe.getRemoteRepositories())
                                        });
                            }
                        }

                        if (name == null) {
                            name = NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_MissingSomething");
                        }
                        if (message == null) {
                            message = exc.getMessage();
                        }
                        if (urlString == null) {
                            urlString = "http://wiki.netbeans.org/MavenBadlyFormedProjectErrors"; //NOI18N
                            }
                        Action act;
                        try {
                            act = new OpenWikiPage(new URL(urlString)); //NOI18N
                            } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                            act = null;
                        }
                        ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                                name,
                                message, act);
                        problemReporter.addReport(report);
                    } else {
                        Logger.getLogger(NbMavenProjectImpl.class.getName()).log(Level.INFO, "Exception thrown while loading maven project at " + getProjectDirectory(), (Exception) e); //NOI18N
                        ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                                "Error reading project model",
                                ((Exception) e).getMessage(), null);
                        problemReporter.addReport(report);

                    }
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
//                hndl.finish();
//                ProgressTransferListener.clearAggregateHandle();

            if (newproject == null) {
                File fallback = InstalledFileLocator.getDefault().locate("maven2/fallback_pom.xml", null, false); //NOI18N
                try {
                    newproject = getEmbedder().readProject(fallback);
                } catch (Exception x) {
                    // oh well..
                    //NOPMD
                    }
            }
            long endLoading = System.currentTimeMillis();
            Logger logger = Logger.getLogger(NbMavenProjectImpl.class.getName());
            logger.fine("Loaded project in " + ((endLoading - startLoading) / 1000) + " s at " + getProjectDirectory().getPath());
            if (logger.isLoggable(Level.FINE) && SwingUtilities.isEventDispatchThread()) {
                logger.log(Level.FINE, "Project " + getProjectDirectory().getPath() + " loaded in AWT event dispatching thread!", new RuntimeException());
            }

            return newproject;

        }
    }

    public void fireProjectReload() {
        //#149566 prevent project firing squads to execute under project mutex.
        if (ProjectManager.mutex().isReadAccess() ||
            ProjectManager.mutex().isWriteAccess() ||
            SwingUtilities.isEventDispatchThread()) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    fireProjectReload();
                }

            });
            return;
        }
        clearProjectWorkspaceCache();
        problemReporter.clearReports(); //#167741 -this will trigger node refresh?
        MavenProject prj = loadOriginalMavenProject();
        synchronized (this) {
            project = prj;
        }
        ACCESSOR.doFireReload(watcher);
        projectInfo.reset();
        doBaseProblemChecks();
    }

    public void clearProjectWorkspaceCache() {
        //when project gets reloaded (pom.xml file changed, build finished)
        //we need to dump the weakly referenced caches and start with a clean room
        try {
            MavenWorkspaceStore store = (MavenWorkspaceStore) getEmbedder().getPlexusContainer().lookup("org.apache.maven.workspace.MavenWorkspaceStore"); //NOI18N
            if (store instanceof NbMavenWorkspaceStore) {
                ((NbMavenWorkspaceStore)store).doManualClear();
            }
        } catch (ComponentLookupException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    
    public static void refreshLocalRepository(NbMavenProjectImpl project) {
        String basedir = project.getEmbedder().getLocalRepository().getBasedir();
        File file = FileUtil.normalizeFile(new File(basedir));
        FileUtil.refreshFor(file);
    }
    
    

    void doBaseProblemChecks() {
        problemReporter.doBaseProblemChecks(project);
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


    private Image getIcon() {
        if (icon == null) {
            icon = ImageUtilities.loadImage("org/netbeans/modules/maven/Maven2Icon.gif");//NOI18N
        }
        return icon;
    }

    public String getName() {
        String toReturn = null;
        MavenProject pr = getOriginalMavenProject();
        if (pr != null) {
            toReturn = pr.getId();
        }
        if (toReturn == null) {
            toReturn = getProjectDirectory().getName() + " _No Project ID_"; //NOI18N

        }
        toReturn = toReturn.replace(":", "_");
        return toReturn;
    }
    /**
     * TODO move elsewhere?
     */
    private static Action refreshAction;

    public static Action createRefreshAction() {
        if (refreshAction == null) {
            refreshAction = new RefreshAction(Lookup.EMPTY);
        }
        return refreshAction;
    }

    /**
     * the root dirtectory of the project.. that;s where the pom resides.
     */
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

    public String getArtifactRelativeRepositoryPath() {
        return getArtifactRelativeRepositoryPath(getOriginalMavenProject().getArtifact());
    }
    /**
     * path of test artifact in local repository
     * @return
     */
    public String getTestArtifactRelativeRepositoryPath() {
        Artifact main = getOriginalMavenProject().getArtifact();
        try {
            ArtifactHandlerManager artifactHandlerManager = (ArtifactHandlerManager) getEmbedder().getPlexusContainer().lookup( ArtifactHandlerManager.ROLE );
            Artifact test = new DefaultArtifact(main.getGroupId(), main.getArtifactId(), main.getVersionRange(),
                            Artifact.SCOPE_TEST, "test-jar", "tests", artifactHandlerManager.getArtifactHandler("test-jar"));
            return getArtifactRelativeRepositoryPath(test);
        } catch (ComponentLookupException ex) {
            throw new IllegalStateException("Cannot lookup ArtifactHandlerManager, broken plexus container.", ex);
        }
    }

    public String getArtifactRelativeRepositoryPath(Artifact artifact) {
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
        @SuppressWarnings("unchecked")
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
        uris[uris.length - 2 ] = getScalaDirectory(test);
        uris[uris.length - 1] = getGroovyDirectory(test);
        return uris;
    }

    public URI[] getGeneratedSourceRoots() {
        
        //TODO more or less a hack.. should be better supported by embedder itself.
        URI uri = FileUtilities.getDirURI(getProjectDirectory(), "target/generated-sources"); //NOI18N
        Set<URI> uris = new HashSet<URI>();
        
        File fil = new File(uri);
        if (fil.exists() && fil.isDirectory()) {
            File[] fils = fil.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            if (fils != null) { //#163842 maybe if the dir was deleted right before listFiles()
                for (int i = 0; i < fils.length; i++) {
                    uris.add(fils[i].toURI());
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
        @SuppressWarnings("unchecked")
        List<Resource> res = test ? getOriginalMavenProject().getTestResources() : getOriginalMavenProject().getResources();
        for (Resource elem : res) {
            URI uri = FileUtilities.getDirURI(getProjectDirectory(), elem.getDirectory());
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
                public boolean accept(File dir, String name) {
                    //TODO most probably a performance bottleneck of sorts..
                    return !("java".equalsIgnoreCase(name)) && //NOI18N
                           !("webapp".equalsIgnoreCase(name)) && //NOI18N
                           !("groovy".equalsIgnoreCase(name)) && //NOI18N
                           !("scala".equalsIgnoreCase(name)) //NOI18N
                       && VisibilityQuery.getDefault().isVisible(FileUtil.toFileObject(new File(dir, name))); //NOI18N
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

        @Override
        protected synchronized void beforeLookup(Template<?> template) {
            if (!initialized && 
                (! (ProjectInformation.class.equals(template.getType()) ||
                    NbMavenProject.class.equals(template.getType()) ||
                    NbMavenProjectImpl.class.equals(template.getType()) ||
                    Project.class.equals(template.getType()) ||
                    SharabilityQueryImplementation.class.equals(template.getType()) ||
                    SubprojectProvider.class.equals(template.getType())))) {
                initialized = true;
                lookup = createBasicLookup();
                setLookups(lookup);
                Lookup lkp = LookupProviderSupport.createCompositeLookup(lookup, "Projects/org-netbeans-modules-maven/Lookup");
                assert checkForForbiddenMergers(lkp) : "Cannot have a LookupMerger for ProjectInformation or SharabilityQueryImplementation";
                setLookups(lkp); //NOI18N
                
            }
            super.beforeLookup(template);
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
        @SuppressWarnings("deprecation")
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
                    new ClassPathProviderImpl(this),
                    sharability,
                    new MavenTestForSourceImpl(this),
                    ////            new MavenFileBuiltQueryImpl(this),
                    subs,
                    new MavenSourcesImpl(this),
                    new RecommendedTemplatesImpl(this),
                    new MavenSourceLevelImpl(this),
                    problemReporter,
                    new UserActionGoalProvider(this),
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
                    new JarPackagingRunChecker(),
                    new DebuggerChecker(),
                    new CosChecker(this),
                    CosChecker.createResultChecker(),
                    CosChecker.createCoSHook(this),
                    new ReactorChecker(),
                    new PrereqCheckerMerger(),
                    new TestSkippingChecker(),
                    new RecommendedTemplates() {
                        public String[] getRecommendedTypes() {
                            return new String[] { "scala-classes" }; //NOI18N
                        }
                    }
                });
        return staticLookup;
    }

    public boolean isErrorPom(MavenProject pr) {
        if ("error".equals(pr.getArtifactId()) && "error".equals(pr.getGroupId()) && "unknown".equals(pr.getVersion())) {
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

        public String getName() {
            String toReturn = NbMavenProjectImpl.this.getName();
            return toReturn;
        }


        public String getDisplayName() {
            MavenProject pr = NbMavenProjectImpl.this.getOriginalMavenProject();
            if (isErrorPom(pr)) {
                return NbBundle.getMessage(NbMavenProjectImpl.class, "TXT_FailedLoadingProject");
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
            toReturn = toReturn + " (" + pr.getPackaging() + ")"; //NOI18N

            return toReturn;
        }

        public Icon getIcon() {
            return ImageUtilities.image2Icon(NbMavenProjectImpl.this.getIcon());
        }

        public Project getProject() {
            return NbMavenProjectImpl.this;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

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

        public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
        }

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

        public void fileDeleted(FileEvent fileEvent) {
            if (!fileEvent.getFile().isFolder()) {
                lastTime = System.currentTimeMillis();
                fileEvent.getFile().removeFileChangeListener(this);
                NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
            }
        }

        public void fileFolderCreated(FileEvent fileEvent) {
            //TODO possibly remove this fire.. watch for actual path..
//            NbMavenProject.fireMavenProjectReload(NbMavenProjectImpl.this);
        }

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
            "web-service-clients",  // NOI18N
            "wsdl", // NOI18N
            // "servlet-types",     // NOI18N
            // "web-types",         // NOI18N
            "junit", // NOI18N
            // "MIDP",              // NOI18N
            "simple-files"          // NOI18N

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
            "simple-files"          // NOI18N

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
            "web-service-clients",  // NOI18N
            "wsdl", // NOI18N
            "servlet-types", // NOI18N
            "web-types", // NOI18N
            "junit", // NOI18N
            // "MIDP",              // NOI18N
            "simple-files", // NOI18N
            "ear-types",            // NOI18N

        };
        private static final String[] GENERIC_WEB_TYPES = new String[]{
            "java-classes", // NOI18N
            "java-main-class", // NOI18N
            "java-beans", // NOI18N
            "oasis-XML-catalogs", // NOI18N
            "XML", // NOI18N
            "wsdl", // NOI18N
            "junit", // NOI18N
            "simple-files"          // NOI18N

        };
        private static final String[] GENERIC_EJB_TYPES = new String[]{
            "java-classes", // NOI18N
            "wsdl", // NOI18N
            "java-beans", // NOI18N
            "java-main-class", // NOI18N
            "oasis-XML-catalogs", // NOI18N
            "XML", // NOI18N
            "junit", // NOI18N
            "simple-files"          // NOI18N

        };
        private static final String[] GENERIC_EAR_TYPES = new String[]{
            "XML", //NOPMD      // NOI18N
            "wsdl", //NOPMD       // NOI18N
            "simple-files"   //NOPMD       // NOI18N

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
        }

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
            //TODO when apisupport module becomes 'non-experimental', delete this block..
            //NBM also fall under this I guess..
            if (NbMavenProject.TYPE_NBM.equals(packaging)) {
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
            Collection col = context.lookupAll(NbMavenProjectImpl.class);
            if (col.size() > 1) {
                putValue(Action.NAME, NbBundle.getMessage(NbMavenProjectImpl.class, "ACT_Reload_Projects", col.size()));
            } else {
                putValue(Action.NAME, NbBundle.getMessage(NbMavenProjectImpl.class, "ACT_Reload_Project"));
            }
        }

        public void actionPerformed(java.awt.event.ActionEvent event) {
            //#166919 - need to run in RP to prevent RPing later in fireProjectReload()
            RELOAD_RP.post(new Runnable() {
                public void run() {
                    EmbedderFactory.resetProjectEmbedder();
                    for (NbMavenProjectImpl prj : context.lookupAll(NbMavenProjectImpl.class)) {
                        NbMavenProject.fireMavenProjectReload(prj);
                    }
                }
            });

        }

        public Action createContextAwareInstance(Lookup actionContext) {
            return new RefreshAction(actionContext);
        }
    }

    private String repositoryListToString(List repositories) {
        String toRet = "";
        if (repositories != null) {
            for (Object r : repositories) {
                ArtifactRepository repo = (ArtifactRepository)r;
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
        public void actionPerformed(java.awt.event.ActionEvent event) {
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        }
    }
}
