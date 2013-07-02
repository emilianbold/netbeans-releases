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

package org.netbeans.modules.maven.problems;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.plugin.PluginArtifactsCache;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.netbeans.modules.maven.NbArtifactFixer;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.actions.OpenPOMAction;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.api.problem.ProblemReporter;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.modelcache.MavenProjectCache;
import static org.netbeans.modules.maven.problems.Bundle.*;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.ModuleInfo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkleint
 */

@ActionReferences({
    @ActionReference(
        id=@ActionID(id="org.netbeans.modules.project.ui.problems.BrokenProjectActionFactory",category="Project"),
        position = 3100,
        path = "Projects/org-netbeans-modules-maven/Actions")
})
public final class ProblemReporterImpl implements ProblemReporter, Comparator<ProblemReport>, ProjectProblemsProvider {
    private static final String MISSING_J2EE = "MISSINGJ2EE"; //NOI18N
    private static final String MISSING_APISUPPORT = "MISSINGAPISUPPORT"; //NOI18N
    private static final String MISSING_DEPENDENCY = "MISSING_DEPENDENCY";//NOI18N
    private static final String BUILD_PARTICIPANT = "BUILD_PARTICIPANT";//NOI18N
    private static final String MISSING_PARENT = "MISSING_PARENT";//NOI18N
    
    private static final Logger LOG = Logger.getLogger(ProblemReporterImpl.class.getName());
    public static final RequestProcessor RP = new RequestProcessor(ProblemReporterImpl.class);

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private final Set<ProblemReport> reports;
    private final Set<File> missingArtifacts;
    private final File projectPOMFile;
    private final RequestProcessor.Task reloadTask = RP.create(new Runnable() {
        @Override public void run() {
            LOG.log(Level.FINE, "actually reloading {0}", projectPOMFile);
            nbproject.fireProjectReload();
        }
    });
    private final FileChangeListener fcl = new FileChangeAdapter() {
        @Override public void fileDataCreated(FileEvent fe) {
            LOG.log(Level.FINE, "due to {0} scheduling reload of {1}", new Object[] {fe.getFile(), projectPOMFile});
            reloadTask.schedule(1000);
            File f = FileUtil.toFile(fe.getFile());
            if (f != null) {
                BatchProblemNotifier.resolved(f);
            } else {
                LOG.log(Level.FINE, "no java.io.File from {0}", fe);
            }
        }
    };
    private final NbMavenProjectImpl nbproject;
    private ModuleInfo j2eeInfo;
    private PropertyChangeListener listener = new PropertyChangeListener() {
        @Override public void propertyChange(PropertyChangeEvent evt) {
            if (ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {
                ProblemReport rep = getReportWithId(MISSING_J2EE);
                if (rep != null) {
                    boolean hasj2ee = j2eeInfo != null && j2eeInfo.isEnabled();
                    if (hasj2ee) {
                        removeReport(rep);
                        j2eeInfo.removePropertyChangeListener(this);
                    }
                }
            }
        }
    };
    
    /** Creates a new instance of ProblemReporter */
    public ProblemReporterImpl(NbMavenProjectImpl proj) {
        reports = new TreeSet<ProblemReport>(this);
        missingArtifacts = new HashSet<File>();
        nbproject = proj;
        projectPOMFile = nbproject.getPOMFile();
    }
    
    public void addChangeListener(ChangeListener list) {
        synchronized (listeners) {
            listeners.add(list);
        }
    }
    
    public void removeChangeListener(ChangeListener list) {
         synchronized (listeners) {
             listeners.remove(list);
         }
    }
    
    @Override public void addReport(ProblemReport report) {
        assert report != null;
        synchronized (reports) {
            reports.add(report);
        }
        fireChange();
        firePropertyChange();
    }
    
    @Override public void addReports(ProblemReport[] report) {
        assert report != null;
        synchronized (reports) {
            for (int i = 0; i < report.length; i++) {
                assert report[i] != null;
                reports.add(report[i]);
            }
        }
        fireChange();
        firePropertyChange();
    }
    
    @Override public void removeReport(ProblemReport report) {
        synchronized (reports) {
            reports.remove(report);
        }
        fireChange();
        firePropertyChange();
    }
    
    private void fireChange() {
        ArrayList<ChangeListener> list;
        synchronized (listeners) {        
            list = new ArrayList<ChangeListener>(listeners);
        }
        for (ChangeListener li : list) {
            li.stateChanged(new ChangeEvent(this));
        }
    }

    /** @return true if {@link #getReports} is nonempty */
    public boolean isBroken() {
        synchronized (reports) {
            for (ProblemReport report : reports) {
                if (report.getSeverityLevel() < ProblemReport.SEVERITY_LOW) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override public Collection<ProblemReport> getReports() {
        synchronized (reports) {
            return new ArrayList<ProblemReport>(reports);
        }
    }

    /**
     * Note an artifact whose absence in the local repository is implicated among the problems.
     * Note that some problems are not caused by missing artifacts,
     * and some problems encapsulate several missing artifacts.
     * @param a an artifact (scope permitted but ignored)
     */
    private void addMissingArtifact(Artifact a) {
        synchronized (reports) {
            a = EmbedderFactory.getProjectEmbedder().getLocalRepository().find(a);
            //a.getFile should be already normalized but the find() method can pull tricks on us.
            //#225008
            File f = FileUtil.normalizeFile(a.getFile());
            if (missingArtifacts.add(f)) {                
                LOG.log(Level.FINE, "listening to {0} from {1}", new Object[] {f, projectPOMFile});                
                FileUtil.addFileChangeListener(fcl, f);
            }
        }
    }

    public Set<File> getMissingArtifactFiles() {
        synchronized (reports) {
            return new TreeSet<File>(missingArtifacts);
        }
    }

    public boolean hasReportWithId(String id) {
        return getReportWithId(id) != null;
    }

    public ProblemReport getReportWithId(String id) {
        assert id != null;
        synchronized (reports) {
            for (ProblemReport rep : reports) {
                if (id.equals(rep.getId())) {
                    return rep;
                }
            }
        }
        return null;
    }
    
    public void clearReports() {
        boolean hasAny;
        synchronized (reports) {
            hasAny = !reports.isEmpty();
            reports.clear();
            Iterator<File> as = missingArtifacts.iterator();
            while (as.hasNext()) {
                File f = as.next();
                if (f != null) {
                    LOG.log(Level.FINE, "ceasing to listen to {0} from {1}", new Object[] {f, projectPOMFile});
                    // a.getFile() should be normalized
                    FileUtil.removeFileChangeListener(fcl, f);
                    if (f.isFile()) {
                        BatchProblemNotifier.resolved(f);
                    }
                }
                as.remove();
            }
            missingArtifacts.clear();
        }
        if (hasAny) {
            fireChange();
            firePropertyChange();
        }
        EmbedderFactory.getProjectEmbedder().lookupComponent(PluginArtifactsCache.class).flush(); // helps with #195440
    }
    
    @Override public int compare(ProblemReport o1, ProblemReport o2) {
        int ret = o1.getSeverityLevel() - o2.getSeverityLevel();
        if (ret != 0) {
            return ret;
        }
        return o1.hashCode() - o2.hashCode();
        
    }

    private ModuleInfo findJ2eeModule() {
        Collection<? extends ModuleInfo> infos = Lookup.getDefault().lookupAll(ModuleInfo.class);
        for (ModuleInfo info : infos) {
            if ("org.netbeans.modules.maven.j2ee".equals(info.getCodeNameBase())) {
                return info;
            }
        }
        return null;
    }
    
    @Messages({
        "ERR_MissingJ2eeModule=Maven Java EE support missing",
        "MSG_MissingJ2eeModule=You are missing the Maven Java EE support module in your installation. "
            + "This means that all EE-related functionality (for example, Deployment, File templates) is missing. "
            + "The most probable cause is that part of the general Java EE support is missing as well. "
            + "Please go to Tools/Plugins and install the plugins related to Java EE.",
        "ERR_MissingApisupportModule=Maven NetBeans Module Projects support missing",
        "MSG_MissingApisupportModule=You are missing the Maven NetBeans Module Projects module in your installation. "
            + "This means that all NetBeans Platform functionality (for example, API wizards, running Platform applications) is missing. "
            + "The most probable cause is that part of the general Platform development support is missing as well. "
            + "Please go to Tools/Plugins and install the plugins related to NetBeans development."
    })
    public void doIDEConfigChecks() {
        String packaging = nbproject.getProjectWatcher().getPackagingType();
        if (NbMavenProject.TYPE_WAR.equals(packaging) ||
            NbMavenProject.TYPE_EAR.equals(packaging) ||
            NbMavenProject.TYPE_EJB.equals(packaging)) {
            if (j2eeInfo == null) {
                j2eeInfo = findJ2eeModule();
            }
            boolean foundJ2ee = j2eeInfo != null && j2eeInfo.isEnabled();
            if (!foundJ2ee) {
                if (!hasReportWithId(MISSING_J2EE)) {
                    ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_MEDIUM,
                        ERR_MissingJ2eeModule(),
                        MSG_MissingJ2eeModule(),
                        new InstallJ2eeModulesAction());
                    report.setId(MISSING_J2EE);
                    addReport(report);
                    if (j2eeInfo != null) {
                        j2eeInfo.addPropertyChangeListener(listener);
                    }
                }
            } else {
                if (j2eeInfo != null) {
                    j2eeInfo.removePropertyChangeListener(listener);
                }
            }
        } else if (NbMavenProject.TYPE_NBM.equals(packaging)) {
            Collection<? extends ModuleInfo> infos = Lookup.getDefault().lookupAll(ModuleInfo.class);
            boolean foundApisupport = false;
            for (ModuleInfo info : infos) {
                if ("org.netbeans.modules.maven.apisupport".equals(info.getCodeNameBase()) && //NOI18N
                        info.isEnabled()) {
                    foundApisupport = true;
                    break;
                }
            }
            if (!foundApisupport) {
                ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_MEDIUM,
                    ERR_MissingApisupportModule(),
                    MSG_MissingApisupportModule(),
                    new InstallApisupportModulesAction());
                report.setId(MISSING_APISUPPORT);
                addReport(report);
            }
        }   


        // XXX undeclared Java platform
    }

    @Messages({
        "ERR_SystemScope=A 'system' scope dependency was not found. Code completion is affected.",
        "MSG_SystemScope=There is a 'system' scoped dependency in the project but the path to the binary is not valid.\n"
            + "Please check that the path is absolute and points to an existing binary.",
        "ERR_NonLocal=Some dependency artifacts are not in the local repository.",
        "# {0} - list of artifacts", "MSG_NonLocal=Your project has dependencies that are not resolved locally. "
            + "Code completion in the IDE will not include classes from these dependencies or their transitive dependencies (unless they are among the open projects).\n"
            + "Please download the dependencies, or install them manually, if not available remotely.\n\n"
            + "The artifacts are:\n {0}",
        "ERR_Participant=Custom build participant(s) found",
        "MSG_Participant=The IDE will not execute any 3rd party extension code during Maven project loading.\nThese can have significant influence on performance of the Maven model (re)loading or interfere with IDE's own codebase. "
            + "On the other hand the model loaded can be incomplete without their participation. In this project "
            + "we have discovered the following external build participants:\n{0}"
    })
    public void doArtifactChecks(@NonNull MavenProject project) {
        
        if (MavenProjectCache.unknownBuildParticipantObserved(project)) {
            StringBuilder sb = new StringBuilder();
            for (String s : MavenProjectCache.getUnknownBuildParticipantsClassNames(project)) {
                sb.append(s).append("\n");
            }
            ProblemReport report = new ProblemReport(
                    ProblemReport.SEVERITY_MEDIUM,
                    ERR_Participant(),
                    MSG_Participant(sb.toString()),
                    null
                    /**new EnableParticipantsBuildAction(nbproject)**/);
            report.setId(BUILD_PARTICIPANT);
            addReport(report);
        }
        checkParents(project);
        
        boolean missingNonSibling = false;
        List<Artifact> missingJars = new ArrayList<Artifact>();
        for (Artifact art : project.getArtifacts()) {
            File file = art.getFile();
            if (file == null || !file.exists()) {                
                if(Artifact.SCOPE_SYSTEM.equals(art.getScope())){
                    //TODO create a correction action for this.
                    ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_MEDIUM,
                            ERR_SystemScope(),
                            MSG_SystemScope(),
                            OpenPOMAction.instance().createContextAwareInstance(Lookups.fixed(nbproject)));
                    addReport(report);
                } else {
                    addMissingArtifact(art);
                    if (file == null) {
                        missingNonSibling = true;
                    } else {
                        //a.getFile should be already normalized
                        SourceForBinaryQuery.Result2 result = SourceForBinaryQuery.findSourceRoots2(FileUtil.urlForArchiveOrDir(file));
                        if (!result.preferSources() || /* SourceForBinaryQuery.EMPTY_RESULT2.preferSources() so: */ result.getRoots().length == 0) {
                            missingNonSibling = true;
                        } // else #189442: typically a snapshot dep on another project
                    }
                    missingJars.add(art);
                }
            } else if (NbArtifactFixer.isFallbackFile(file)) {
                addMissingArtifact(art);
                missingJars.add(art);
                missingNonSibling = true;
            }
        }
        if (!missingJars.isEmpty()) {
            StringBuilder mess = new StringBuilder();
            for (Artifact art : missingJars) {
                mess.append(art.getId()).append('\n');
            }
            ProblemReport report = new ProblemReport(
                    missingNonSibling ? ProblemReport.SEVERITY_MEDIUM : ProblemReport.SEVERITY_LOW,
                    ERR_NonLocal(),
                    MSG_NonLocal(mess),
                    new SanityBuildAction(nbproject));
            report.setId(MISSING_DEPENDENCY);
            addReport(report);
        }
    }

    @Messages({
        "ERR_NoParent=Parent POM file is not accessible. Project might be improperly setup.",
        "# {0} - Maven coordinates", "MSG_NoParent=The parent POM with id {0} was not found in sources or local repository. "
            + "Please check that <relativePath> tag is present and correct, the version of parent POM in sources matches the version defined. \n"
            + "If parent is only available through a remote repository, please check that the repository hosting it is defined in the current POM."
    })
    private void checkParents(@NonNull MavenProject project) {
        List<MavenEmbedder.ModelDescription> mdls = MavenEmbedder.getModelDescriptors(project);
        boolean first = true;
        if (mdls == null) { //null means just about broken project..
            return;
        }
        for (MavenEmbedder.ModelDescription m : mdls) {
            if (first) {
                first = false;
                continue;
            }
            if (NbArtifactFixer.FALLBACK_NAME.equals(m.getName())) {
                ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                        ERR_NoParent(),
                        MSG_NoParent(m.getId()),
                        new SanityBuildAction(nbproject));
                report.setId(MISSING_PARENT);
                addReport(report);
                addMissingArtifact(EmbedderFactory.getProjectEmbedder().createArtifact(m.getGroupId(), m.getArtifactId(), m.getVersion(), "pom"));
            }
        }
    }

    
    @Messages({
        "TXT_Artifact_Resolution_problem=Artifact Resolution problem",
        "TXT_Artifact_Not_Found=Artifact Not Found",
        "TXT_Cannot_Load_Project=Unable to properly load project"
    })
    public void reportExceptions(MavenExecutionResult res) throws MissingResourceException {
        for (Throwable e : res.getExceptions()) {
            LOG.log(Level.FINE, "Error on loading project " + projectPOMFile, e);
            String msg = e.getMessage();
            if (e instanceof ArtifactResolutionException) { // XXX when does this occur?
                ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                        TXT_Artifact_Resolution_problem(), msg, null);
                addReport(report);
                addMissingArtifact(((ArtifactResolutionException) e).getArtifact());
            } else if (e instanceof ArtifactNotFoundException) { // XXX when does this occur?
                ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                        TXT_Artifact_Not_Found(), msg, null);
                addReport(report);
                addMissingArtifact(((ArtifactNotFoundException) e).getArtifact());
            } else if (e instanceof ProjectBuildingException) {
                addReport(new ProblemReport(ProblemReport.SEVERITY_HIGH,
                        TXT_Cannot_Load_Project(), msg, new SanityBuildAction(nbproject)));
                if (e.getCause() instanceof ModelBuildingException) {
                    ModelBuildingException mbe = (ModelBuildingException) e.getCause();
                    for (ModelProblem mp : mbe.getProblems()) {
                        LOG.log(Level.FINE, mp.toString(), mp.getException());
                        if (mp.getException() instanceof UnresolvableModelException) {
                            // Probably obsoleted by ProblemReporterImpl.checkParent, but just in case:
                            UnresolvableModelException ume = (UnresolvableModelException) mp.getException();
                            addMissingArtifact(EmbedderFactory.getProjectEmbedder().createProjectArtifact(ume.getGroupId(), ume.getArtifactId(), ume.getVersion()));
                        } else if (mp.getException() instanceof PluginResolutionException) {
                            Plugin plugin = ((PluginResolutionException) mp.getException()).getPlugin();
                            // XXX this is not actually accurate; should rather pick out the ArtifactResolutionException & ArtifactNotFoundException inside
                            addMissingArtifact(EmbedderFactory.getProjectEmbedder().createArtifact(plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion(), "jar"));
                        }
                    }
                }
            } else {
                LOG.log(Level.INFO, "Exception thrown while loading maven project at " + projectPOMFile, e); //NOI18N
                ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                        "Error reading project model", msg, null);
                addReport(report);
            }
        }
    }
    
    public static Action createOpenFileAction(FileObject fo) {
        return new OpenActions(fo);
    }
    
    private static class OpenActions extends AbstractAction {

        private FileObject fo;

        @Messages({"TXT_OPEN_FILE=Open File",
            "ACT_OPEN_FILE_START=Affected file was opened."
        })
        OpenActions(FileObject file) {
            putValue(Action.NAME, TXT_OPEN_FILE());
            putValue(ProblemReporterImpl.ACT_START_MESSAGE, ACT_OPEN_FILE_START());
            fo = file;
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            if (fo != null) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    EditCookie edit = dobj.getLookup().lookup(EditCookie.class);
                    edit.edit();
                } catch (DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    

    
    //---------------------------------------
    //projectproblem provider related methods

    private final PropertyChangeSupport chs = new PropertyChangeSupport(this);
    //constant for action.getValue() holding the text to show to users..
    public static final String ACT_START_MESSAGE = "START_MESSAGE";
    
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        chs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        chs.removePropertyChangeListener(listener);
    }

    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        List<ProjectProblem> toRet = new ArrayList<ProjectProblem>();
        for (ProblemReport pr : getReports()) {
            ProjectProblemResolver res = new MavenProblemResolver(pr.getCorrectiveAction(), pr.getId() + "|" + this.nbproject.getPOMFile());
            ProjectProblem pp = pr.getSeverityLevel() == ProblemReport.SEVERITY_HIGH ? 
                    ProjectProblem.createError(pr.getShortDescription(), pr.getLongDescription(), res) :
                    ProjectProblem.createWarning(pr.getShortDescription(), pr.getLongDescription(), res);
            toRet.add(pp);
        }
        return toRet;
    }

    
    private void firePropertyChange() {
        chs.firePropertyChange(ProjectProblemsProvider.PROP_PROBLEMS, null, null);
    }

    public static class MavenProblemResolver implements ProjectProblemResolver {
        private final Action action;
        private final String id;

        public MavenProblemResolver(Action correctiveAction, String id) {
            this.action = correctiveAction;
            this.id = id;
        }

        @Override
        @Messages("TXT_No_Res=No resolution for the problem")
        public Future<ProjectProblemsProvider.Result> resolve() {
            FutureTask<Result> toRet = new FutureTask<ProjectProblemsProvider.Result>(new Callable<ProjectProblemsProvider.Result>() {

                                   @Override
                                   public ProjectProblemsProvider.Result call() throws Exception {
                                       if (action != null) {
                                           action.actionPerformed(null);
                                           String text = (String) action.getValue(ACT_START_MESSAGE);
                                           if (text != null) {
                                               return ProjectProblemsProvider.Result.create(Status.RESOLVED, text);
                                           } else {
                                               return ProjectProblemsProvider.Result.create(Status.RESOLVED);
                                           }
                                       } else {
                                           return ProjectProblemsProvider.Result.create(Status.UNRESOLVED, TXT_No_Res());
                                       }
                                       
                                   }
                               });
            RP.post(toRet);
            return toRet;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + (this.id != null ? this.id.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MavenProblemResolver other = (MavenProblemResolver) obj;
            if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
                return false;
            }
            return true;
        }
        
        
    }

    private static class InstallJ2eeModulesAction extends AbstractAction {

        public InstallJ2eeModulesAction() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                //ideally we would use InvokeAndWait otherwise the dialog ui gets messed up.
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        PluginManager.install(Collections.singleton("org.netbeans.modules.j2ee.kit"));
                    }
                });
            } catch (InterruptedException ex) {
            } catch (InvocationTargetException ex) {
            }
        }
    }
    
    private static class InstallApisupportModulesAction extends AbstractAction {

        public InstallApisupportModulesAction() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                //ideally we would use InvokeAndWait otherwise the dialog ui gets messed up.
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        PluginManager.install(Collections.singleton("org.netbeans.modules.apisupport.kit"));
                    }
                });
            } catch (InterruptedException ex) {
            } catch (InvocationTargetException ex) {
            }
        }
    }
   
    
}
