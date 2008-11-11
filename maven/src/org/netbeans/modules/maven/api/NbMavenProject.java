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
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.FileChangeSupport;
import org.netbeans.modules.maven.FileChangeSupportEvent;
import org.netbeans.modules.maven.FileChangeSupportListener;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * an instance resides in project lookup, allows to get notified on project and 
 * relative path changes.
 * @author mkleint
 */
//TODO rename to something else doesn't describe correctly what it does..
public final class NbMavenProject {

    /**
     * TODO comment
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

    public URI getEarAppDirectory() {
        return project.getEarAppDirectory();
    }

    
    private class FCHSL implements FileChangeSupportListener {

        public void fileCreated(FileChangeSupportEvent event) {
            fireChange(event.getPath().toURI());
        }

        public void fileDeleted(FileChangeSupportEvent event) {
            fireChange(event.getPath().toURI());
        }

        public void fileModified(FileChangeSupportEvent event) {
            fireChange(event.getPath().toURI());
        }
        
    }
    
    
    /** Creates a new instance of NbMavenProject */
    private NbMavenProject(NbMavenProjectImpl proj) {
        project = proj;
        //TODO oh well, the sources is the actual project instance not the watcher.. a problem?
        support = new PropertyChangeSupport(proj);
        task = RequestProcessor.getDefault().create(new Runnable() {
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
                    try {
                        ProgressTransferListener.setAggregateHandle(hndl);
                        hndl.start();
                        MavenExecutionRequest req = new DefaultMavenExecutionRequest();
                        req.setPom(pomFile);
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
    
    
    public static final String TYPE_JAR = "jar"; //NOI18N
    public static final String TYPE_WAR = "war"; //NOI18N
    public static final String TYPE_EAR = "ear"; //NOI18N
    public static final String TYPE_EJB = "ejb"; //NOI18N
    public static final String TYPE_NBM = "nbm"; //NOI18N
    public static final String TYPE_POM = "pom"; //NOI18N
    
    /**
     * get the user level packaging type for the project, allows to get the same UI support
     *  of user's custom lifecycles.
     * @return 
     */
    public String getPackagingType() {
        AuxiliaryProperties props = project.getAuxProps();
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
            FileChangeSupport.DEFAULT.addListener(listener, fil);
        }
    } 
    
    public synchronized void triggerDependencyDownload() {
        task.schedule(1000);
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
            FileChangeSupport.DEFAULT.removeListener(listener, fil);
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
