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

package org.netbeans.modules.maven.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.modules.maven.MavenProjectPropsImpl;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.nodes.DependenciesNode;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * an instance resides in project lookup, allows to get notified on project and 
 * relative path changes.
 * @author mkleint
 */
public final class NbMavenProject {

    /**
     * the only property change fired by the class, means that the pom file
     * has changed.
     */
    public static final String PROP_PROJECT = "MavenProject"; //NOI18N
    /**
     * TODO comment
     * 
     */
    public static final String PROP_RESOURCE = "RESOURCES"; //NOI18N
    
    private NbMavenProjectImpl project;
    private PropertyChangeSupport support;
    private FCHSL listener = new FCHSL();
    private final List<File> files = new ArrayList<File>();
    
    static {
        AccessorImpl impl = new AccessorImpl();
        impl.assign();
    }
    private RequestProcessor.Task task;
    private static RequestProcessor BINARYRP = new RequestProcessor("Maven projects Binary Downloads", 1);
    private static RequestProcessor NONBINARYRP = new RequestProcessor("Maven projects Source/Javadoc Downloads", 1);
    
    
    static class AccessorImpl extends NbMavenProjectImpl.WatcherAccessor {
        
        
         public void assign() {
             if (NbMavenProjectImpl.ACCESSOR == null) {
                 NbMavenProjectImpl.ACCESSOR = this;
             }
         }
    
        public NbMavenProject createWatcher(NbMavenProjectImpl proj) {
            return new NbMavenProject(proj);
        }
        
        public void doFireReload(NbMavenProject watcher) {
            watcher.doFireReload();
        }

    }


    
    private class FCHSL implements FileChangeListener {


        public void fileFolderCreated(FileEvent fe) {
            fireChange(FileUtil.toFile(fe.getFile()).toURI());
        }

        public void fileDataCreated(FileEvent fe) {
            fireChange(FileUtil.toFile(fe.getFile()).toURI());
        }

        public void fileChanged(FileEvent fe) {
            fireChange(FileUtil.toFile(fe.getFile()).toURI());
        }

        public void fileDeleted(FileEvent fe) {
            fireChange(FileUtil.toFile(fe.getFile()).toURI());
        }

        public void fileRenamed(FileRenameEvent fe) {
            fireChange(FileUtil.toFile(fe.getFile()).toURI());
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
        
    }
    
    
    /** Creates a new instance of NbMavenProject */
    private NbMavenProject(NbMavenProjectImpl proj) {
        project = proj;
        //TODO oh well, the sources is the actual project instance not the watcher.. a problem?
        support = new PropertyChangeSupport(proj);
        task = createBinaryDownloadTask(BINARYRP);
    }

    private RequestProcessor.Task createBinaryDownloadTask(RequestProcessor rp) {
        return rp.create(new Runnable() {
            public void run() {
                    //#146171 try the hardest to avoid NPE for files/directories that
                    // seemed to have been deleted while the task was scheduled.
                    FileObject fo = project.getProjectDirectory();
                    if (fo == null || !fo.isValid()) {
                        return;
                    }
                    fo = fo.getFileObject("pom.xml"); //NOI18N
                    if (fo == null) {
                        return;
                    }
                    File pomFile = FileUtil.toFile(fo);
                    if (pomFile == null) {
                        return;
                    }
                    MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
                    AggregateProgressHandle hndl = AggregateProgressFactory.createHandle(NbBundle.getMessage(NbMavenProject.class, "Progress_Download"),
                            new ProgressContributor[] {
                                AggregateProgressFactory.createProgressContributor("zaloha") },  //NOI18N
                            null, null);

                    boolean ok = true;
                    ProgressTransferListener ptl = new ProgressTransferListener();
                    try {
                        ProgressTransferListener.setAggregateHandle(hndl);
                        hndl.start();
                        MavenExecutionRequest req = new DefaultMavenExecutionRequest();
                        req.setPom(pomFile);
                        req.setTransferListener(ptl);
                        MavenExecutionResult res = online.readProjectWithDependencies(req); //NOI18N
                        if (res.hasExceptions()) {
                            ok = false;
                            Exception ex = (Exception)res.getExceptions().get(0);
                            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbMavenProject.class, "MSG_Failed", ex.getLocalizedMessage()));
                        }
                    } finally {
                        hndl.finish();
                        ProgressTransferListener.clearAggregateHandle();
                    }
                    if (ok) {
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbMavenProject.class, "MSG_Done"));
                    }
                    NbMavenProject.fireMavenProjectReload(project);
            }
        });
    }
    
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener(propertyChangeListener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener(propertyChangeListener);
    }
    
    /**
     * Returns the current maven project model from the embedder.
     * Should never be kept around for long but always reloaded from here, on 
     * a project change the correct instance changes as the embedder reloads it.
     * 
     */ 
    public MavenProject getMavenProject() {
        return project.getOriginalMavenProject();
    }

    /**
     *
     * @param embedder
     * @param activeProfiles
     * @param properties
     * @return
     */
    public MavenProject loadAlternateMavenProject(MavenEmbedder embedder, List<String> activeProfiles, Properties properties) {
        return project.loadMavenProject(embedder, activeProfiles, properties);
    }

    /**
     * 
     * @param test are test resources requested, if false, resources for base sources are returned
     * @return
     */
    public URI[] getResources(boolean test) {
        return project.getResources(test);
    }

    /**
     * 
     * @return
     */
    public URI getWebAppDirectory() {
        return project.getWebAppDirectory();
    }

    /**
     * Return uris of source roots generated during the build.
     * @return
     */
    public URI[] getGeneratedSourceRoots() {
        return project.getGeneratedSourceRoots();
    }
    
    public URI getEarAppDirectory() {
        return project.getEarAppDirectory();
    }
    
    public static final String TYPE_JAR = "jar"; //NOI18N
    public static final String TYPE_WAR = "war"; //NOI18N
    public static final String TYPE_EAR = "ear"; //NOI18N
    public static final String TYPE_EJB = "ejb"; //NOI18N
    public static final String TYPE_NBM = "nbm"; //NOI18N
    public static final String TYPE_NBM_APPLICATION = "nbm-application"; //NOI18N
    public static final String TYPE_POM = "pom"; //NOI18N
    
    /**
     * get the user level packaging type for the project, allows to get the same UI support
     *  of user's custom lifecycles.
     * @return 
     */
    public String getPackagingType() {
        MavenProjectPropsImpl props = project.getAuxProps();
        String custom = props.get(Constants.HINT_PACKAGING, true);
        MavenProject orig = project.getOriginalMavenProject();
// ignore the old solution. getRawMappings() is expensive in this context..
//        if (custom == null) {
//            // fallback to previous old solution. 
//            custom = project.getLookup().lookup(UserActionGoalProvider.class).getRawMappings().getPackaging();
//        }
        return custom != null ? custom : orig.getPackaging();
    }
    
    
    public synchronized void addWatchedPath(String relPath) {
        addWatchedPath(FileUtilities.getDirURI(project.getProjectDirectory(), relPath));
    } 
    
    public synchronized void addWatchedPath(URI uri) {
        //#110599
        boolean addListener = false;
        File fil = new File(uri);
        synchronized (files) {
            if (!files.contains(fil)) {
                addListener = true;
            }
            files.add(fil);
        }
        if (addListener) {
            FileUtil.addFileChangeListener(listener, fil);
        }
    } 

    /**
     * asynchronous dependency download, scheduled to some time in the future. Useful
     * for cases when a 3rd party codebase calls maven classes and can do so repeatedly in one sequence.
     */
    public synchronized void triggerDependencyDownload() {
        task.schedule(1000);
    }

    /**
     * Not to be called from AWT, will wait til the project binary dependency resolution finishes.
     */
    public synchronized void synchronousDependencyDownload() {
        assert !SwingUtilities.isEventDispatchThread() : " Not to be called from AWT, can take significant amount ot time to download dependencies from the network."; //NOI18N
        task.schedule(0);
        task.waitFinished();
    }

    /**
     * synchronously download binaries and the trigger dependency javadoc/source download (in async mode)
     * Not to be called from AWT thread. The current thread will continue after downloading binaries and firing project change event.
     *
     */
    public void downloadDependencyAndJavadocSource() {
        synchronousDependencyDownload();
        triggerSourceJavadocDownload(true);
        triggerSourceJavadocDownload(false);
    }


    public void triggerSourceJavadocDownload(final boolean javadoc) {
        NONBINARYRP.post(new Runnable() {
            public void run() {
                MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
                @SuppressWarnings("unchecked")
                Set<Artifact> arts = project.getOriginalMavenProject().getArtifacts();
                ProgressContributor[] contribs = new ProgressContributor[arts.size()];
                for (int i = 0; i < arts.size(); i++) {
                    contribs[i] = AggregateProgressFactory.createProgressContributor("multi-" + i); //NOI18N
                }
                String label = javadoc ? NbBundle.getMessage(NbMavenProject.class, "Progress_Javadoc") : NbBundle.getMessage(NbMavenProject.class, "Progress_Source");
                AggregateProgressHandle handle = AggregateProgressFactory.createHandle(label,
                        contribs, null, null);
                handle.start();
                try {
                    ProgressTransferListener.setAggregateHandle(handle);
                    int index = 0;
                    for (Artifact a : arts) {
                        downloadOneJavadocSources(online, contribs[index], project, a, javadoc);
                        index++;
                    }
                } finally {
                    handle.finish();
                    ProgressTransferListener.clearAggregateHandle();
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run() {
                            fireProjectReload();
                        }
                    });
                }
            }
        });
    }


    private static void downloadOneJavadocSources(MavenEmbedder online, ProgressContributor progress,
                                               NbMavenProjectImpl project, Artifact art, boolean isjavadoc) {
        progress.start(2);
        if ( Artifact.SCOPE_SYSTEM.equals(art.getScope())) {
            progress.finish();
            return;
        }
        try {
            if (isjavadoc) {
                Artifact javadoc = project.getEmbedder().createArtifactWithClassifier(
                    art.getGroupId(),
                    art.getArtifactId(),
                    art.getVersion(),
                    art.getType(),
                    "javadoc"); //NOI18N
                progress.progress(NbBundle.getMessage(NbMavenProject.class, "MSG_Checking_Javadoc", art.getId()), 1);
                online.resolve(javadoc, project.getOriginalMavenProject().getRemoteArtifactRepositories(), project.getEmbedder().getLocalRepository());
            } else {
                Artifact sources = project.getEmbedder().createArtifactWithClassifier(
                    art.getGroupId(),
                    art.getArtifactId(),
                    art.getVersion(),
                    art.getType(),
                    "sources"); //NOI18N
                progress.progress(NbBundle.getMessage(NbMavenProject.class, "MSG_Checking_Sources",art.getId()), 1);
                online.resolve(sources, project.getOriginalMavenProject().getRemoteArtifactRepositories(), project.getEmbedder().getLocalRepository());
            }
        } catch (ArtifactNotFoundException ex) {
            // just ignore..ex.printStackTrace();
        } catch (ArtifactResolutionException ex) {
            // just ignore..ex.printStackTrace();
        } finally {
            progress.finish();
        }
    }

    
    public synchronized void removeWatchedPath(String relPath) {
        removeWatchedPath(FileUtilities.getDirURI(project.getProjectDirectory(), relPath));
    }
    public synchronized void removeWatchedPath(URI uri) {
        //#110599
        boolean removeListener = false;
        File fil = new File(uri);
        synchronized (files) {
            boolean rem = files.remove(fil);
            if (rem && !files.contains(fil)) {
                removeListener = true;
            }
        }
        if (removeListener) {
            FileUtil.removeFileChangeListener(listener, fil);
        }
    } 
    
    
    //TODO better do in ReqProcessor to break the listener chaining??
    private void fireChange(URI uri) {
        support.firePropertyChange(PROP_RESOURCE, null, uri);
    }
    
    /**
     * 
     */ 
    private void fireProjectReload() {
        project.fireProjectReload();
    }
    
    private void doFireReload() {
        FileUtil.refreshFor(FileUtil.toFile(project.getProjectDirectory()));
        NbMavenProjectImpl.refreshLocalRepository(project);
        support.firePropertyChange(PROP_PROJECT, null, null);
    }
    
    /**
     * utility method for triggering a maven project reload. 
     * if the project passed in is a Maven based project, will
     * fire reload of the project, otherwise will do nothing.
     */ 
    
    public static void fireMavenProjectReload(Project prj) {
        if (prj != null) {
            NbMavenProject watcher = prj.getLookup().lookup(NbMavenProject.class);
            if (watcher != null) {
                watcher.fireProjectReload();
            }
        }
    }

    public static void addPropertyChangeListener(Project prj, PropertyChangeListener listener) {
        if (prj != null && prj instanceof NbMavenProjectImpl) {
            // cannot call getLookup() -> stackoverflow when called from NbMavenProjectImpl.createBasicLookup()..
            NbMavenProject watcher = ((NbMavenProjectImpl)prj).getProjectWatcher();
            watcher.addPropertyChangeListener(listener);
        } else {
            assert false : "Attempted to add PropertyChangeListener to project " + prj; //NOI18N
        }
    }
    
    public static void removePropertyChangeListener(Project prj, PropertyChangeListener listener) {
        if (prj != null && prj instanceof NbMavenProjectImpl) {
            // cannot call getLookup() -> stackoverflow when called from NbMavenProjectImpl.createBasicLookup()..
            NbMavenProject watcher = ((NbMavenProjectImpl)prj).getProjectWatcher();
            watcher.removePropertyChangeListener(listener);
        } else {
            assert false : "Attempted to remove PropertyChangeListener from project " + prj; //NOI18N
        }
    }
}
