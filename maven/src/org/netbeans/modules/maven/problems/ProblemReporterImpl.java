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
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.PluginArtifactsCache;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.api.problem.ProblemReporter;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import static org.netbeans.modules.maven.problems.Bundle.*;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mkleint
 */
public final class ProblemReporterImpl implements ProblemReporter, Comparator<ProblemReport> {
    private static final String MISSINGJ2EE = "MISSINGJ2EE"; //NOI18N
    private static final Logger LOG = Logger.getLogger(ProblemReporterImpl.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(ProblemReporterImpl.class);

    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private final Set<ProblemReport> reports;
    private final Set<Artifact> missingArtifacts;
    private final RequestProcessor.Task reloadTask = RP.create(new Runnable() {
        @Override public void run() {
            LOG.log(Level.FINE, "actually reloading {0}", nbproject.getPOMFile());
            nbproject.fireProjectReload();
        }
    });
    private final FileChangeListener fcl = new FileChangeAdapter() {
        @Override public void fileDataCreated(FileEvent fe) {
            LOG.log(Level.FINE, "due to {0} scheduling reload of {1}", new Object[] {fe.getFile(), nbproject.getPOMFile()});
            reloadTask.schedule(1000);
        }
    };
    private final NbMavenProjectImpl nbproject;
    private ModuleInfo j2eeInfo;
    private PropertyChangeListener listener = new PropertyChangeListener() {
        @Override public void propertyChange(PropertyChangeEvent evt) {
            if (ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {
                ProblemReport rep = getReportWithId(MISSINGJ2EE);
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
        missingArtifacts = new HashSet<Artifact>();
        nbproject = proj;
    }
    
    public void addChangeListener(ChangeListener list) {
        listeners.add(list);
    }
    
    public void removeChangeListener(ChangeListener list) {
        listeners.remove(list);
    }
    
    @Override public void addReport(ProblemReport report) {
        assert report != null;
        synchronized (reports) {
            reports.add(report);
        }
        fireChange();
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
    }
    
    @Override public void removeReport(ProblemReport report) {
        synchronized (reports) {
            reports.remove(report);
        }
        fireChange();
    }
    
    private void fireChange() {
        for (ChangeListener list : listeners) {
            list.stateChanged(new ChangeEvent(this));
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
    public void addMissingArtifact(Artifact a) {
        synchronized (reports) {
            if (missingArtifacts.add(a)) {
                File f = a.getFile();
                if (f == null) {
                    f = EmbedderFactory.getProjectEmbedder().getLocalRepository().find(a).getFile();
                }
                LOG.log(Level.FINE, "listening to {0} from {1}", new Object[] {f, nbproject.getPOMFile()});
                FileUtil.addFileChangeListener(fcl, f);
            }
        }
    }

    Set<Artifact> getMissingArtifacts() {
        synchronized (reports) {
            return new TreeSet<Artifact>(missingArtifacts);
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
            Iterator<Artifact> as = missingArtifacts.iterator();
            while (as.hasNext()) {
                File f = as.next().getFile();
                if (f != null) {
                    LOG.log(Level.FINE, "ceasing to listen to {0} from {1}", new Object[] {f, nbproject.getPOMFile()});
                    FileUtil.removeFileChangeListener(fcl, f);
                }
                as.remove();
            }
            missingArtifacts.clear();
        }
        if (hasAny) {
            fireChange();
        }
        nbproject.getEmbedder().lookupComponent(PluginArtifactsCache.class).flush(); // helps with #195440
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
        "ERR_MissingJ2eeModule=Maven J2EE support module missing",
        "MSG_MissingJ2eeModule=You are missing the Maven J2EE support module in your installation. "
            + "This means that all J2EE related functionality (for example, Deployment, File templates) is missing. "
            + "The most probable cause is that part of the general J2EE support is missing as well. "
            + "Please go to Tools/Plugins and install the plugins related to J2EE.",
        "ERR_MissingApisupportModule=Maven NetBeans Development support module missing",
        "MSG_MissingApisupportModule=You are missing the Maven APIsupport module in your installation. "
            + "This means that all NetBeans development related functionality (for example, File templates, running platform application) is missing. "
            + "The most probable cause is that part of the general API support is missing as well. "
            + "Please go to Tools/Plugins and install the plugins related to NetBeans development."
    })
    public void doBaseProblemChecks(@NonNull MavenProject project) {
        String packaging = nbproject.getProjectWatcher().getPackagingType();
        if (NbMavenProject.TYPE_WAR.equals(packaging) ||
            NbMavenProject.TYPE_EAR.equals(packaging) ||
            NbMavenProject.TYPE_EJB.equals(packaging)) {
            if (j2eeInfo == null) {
                j2eeInfo = findJ2eeModule();
            }
            boolean foundJ2ee = j2eeInfo != null && j2eeInfo.isEnabled();
            if (!foundJ2ee) {
                if (!hasReportWithId(MISSINGJ2EE)) {
                    ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_MEDIUM,
                        ERR_MissingJ2eeModule(),
                        MSG_MissingJ2eeModule(),
                        null);
                    report.setId(MISSINGJ2EE);
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
                    null);
                addReport(report);
            }
        }   

        MavenProject parent = project;
        while (parent != null) {
            parent = checkParent(parent);
        }

        doArtifactChecks(project);

        // XXX undeclared Java platform
    }

    @Messages({
        "ERR_SystemScope=A 'system' scope dependency was not found. Code completion is affected.",
        "MSG_SystemScope=There is a 'system' scoped dependency in the project but the path to the binary is not valid.\n"
            + "Please check that the path is absolute and points to an existing binary.",
        "ERR_NonLocal=Some dependency artifacts are not in the local repository.",
        "MSG_NonLocal=Your project has dependencies that are not resolved locally. "
            + "Code completion in the IDE will not include classes from these dependencies or their transitive dependencies (unless they are among the open projects).\n"
            + "Please download the dependencies, or install them manually, if not available remotely.\n\n"
            + "The artifacts are:\n {0}"
    })
    private void doArtifactChecks(@NonNull MavenProject project) {
        boolean missingNonSibling = false;
        List<Artifact> missingJars = new ArrayList<Artifact>();
        for (Artifact art : project.getArtifacts()) {
            File file = art.getFile();
            if (file == null || !file.exists()) {
                addMissingArtifact(art);
                if(Artifact.SCOPE_SYSTEM.equals(art.getScope())){
                    //TODO create a correction action for this.
                    ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_MEDIUM,
                            ERR_SystemScope(),
                            MSG_SystemScope(),
                            new OpenPomAction(nbproject));
                    addReport(report);
                } else {
                    if (file == null) {
                        missingNonSibling = true;
                    } else {
                        SourceForBinaryQuery.Result2 result = SourceForBinaryQuery.findSourceRoots2(FileUtil.urlForArchiveOrDir(file));
                        if (!result.preferSources() || /* SourceForBinaryQuery.EMPTY_RESULT2.preferSources() so: */ result.getRoots().length == 0) {
                            missingNonSibling = true;
                        } // else #189442: typically a snapshot dep on another project
                    }
                    missingJars.add(art);
                }
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
            addReport(report);
        }
    }

    // XXX does this still do anything? ProblemReporterImplTest.testMissingParent suggests not
    @Messages({
        "ERR_NoParent=Parent POM file is not accessible. Project might be improperly setup.",
        "MSG_NoParent=The parent POM with id {0}  was not found in sources or local repository. "
            + "Please check that <relativePath> tag is present and correct, the version of parent POM in sources matches the version defined. \n"
            + "If parent is only available through a remote repository, please check that the repository hosting it is defined in the current POM."
    })
    private @CheckForNull MavenProject checkParent(@NonNull MavenProject project) {
        MavenProject parentDecl;
        try {
            parentDecl = project.getParent();
        } catch (IllegalStateException x) { // #197994
            parentDecl = null;
        }
        Artifact art = project.getParentArtifact();
        if (art != null ) {
            
            if (parentDecl != null) {
                File parent = parentDecl.getFile();
                if (parent != null && parent.exists()) {
                    return parentDecl;
                }
            }
           
            
            if (art.getFile() != null && !art.getFile().exists()) {
                ProblemReport report = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                        ERR_NoParent(),
                        MSG_NoParent(art.getId()),
                        new SanityBuildAction(nbproject));
                addReport(report);
            }

        }
        return parentDecl;
    }

    
    static class OpenPomAction extends AbstractAction {
        
        private NbMavenProjectImpl project;
        private String filepath;
        
        @Messages("ACT_OpenPom=Open pom.xml")
        OpenPomAction(NbMavenProjectImpl proj) {
            putValue(Action.NAME, ACT_OpenPom());
            project = proj;
        }
        
        OpenPomAction(NbMavenProjectImpl project, String filePath) {
            this(project);
            filepath = filePath;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            FileObject fo = null;
            if (filepath != null) {
                fo = FileUtil.toFileObject(FileUtil.normalizeFile(new File(filepath)));
            } else {
                fo = FileUtil.toFileObject(project.getPOMFile());
            }
            if (fo != null) {
                try {
                    DataObject dobj = DataObject.find(fo);
                    EditCookie edit = dobj.getCookie(EditCookie.class);
                    edit.edit();
                } catch (DataObjectNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    
}
