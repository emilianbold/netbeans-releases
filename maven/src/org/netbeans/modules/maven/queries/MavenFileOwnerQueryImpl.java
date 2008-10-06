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

package org.netbeans.modules.maven.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex.Action;


/**
 * A global implementation of FileOwnerQueryImplementation, is required to link together the maven project
 * and it's artifact in the maven repository. any other files shall be handled by the
 * default netbeans implementation.
 *
 * @author  Milos Kleint
 */
public class MavenFileOwnerQueryImpl implements FileOwnerQueryImplementation {
    
    private Set<NbMavenProjectImpl> set;
    private final Object lock = new Object();
    private final Object cacheLock = new Object();
    private final List<ChangeListener> listeners;
    private Set cachedProjects;
    private PropertyChangeListener projectListener;
    private static final Logger LOG = Logger.getLogger(MavenFileOwnerQueryImpl.class.getName());
    
    /** Creates a new instance of MavenFileBuiltQueryImpl */
    public MavenFileOwnerQueryImpl() {
        set = new HashSet<NbMavenProjectImpl>();
        listeners = new ArrayList<ChangeListener>();
        cachedProjects = null;
        projectListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
                    synchronized (cacheLock) {
                        cachedProjects = null;
                    }
                }
            }
        };
    }
    
    public static MavenFileOwnerQueryImpl getInstance() {
        Lookup.Result<FileOwnerQueryImplementation> implementations = 
                Lookup.getDefault().lookup(new Lookup.Template<FileOwnerQueryImplementation>(FileOwnerQueryImplementation.class));
        Iterator<? extends FileOwnerQueryImplementation> it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            FileOwnerQueryImplementation obj = it.next();
            if (obj instanceof MavenFileOwnerQueryImpl) {
                return (MavenFileOwnerQueryImpl)obj;
            }
        }
        return null;
    }
    
    public void addMavenProject(NbMavenProjectImpl project) {
        synchronized (lock) {
            if (!set.contains(project)) {
                LOG.fine("Adding Maven project:" + project.getArtifactRelativeRepositoryPath());
                set.add(project);
                NbMavenProject.addPropertyChangeListener(project, projectListener);
            }
        }
        synchronized (cacheLock) {
            cachedProjects = null;
        }
        
        fireChange();
    }
    public void removeMavenProject(NbMavenProjectImpl project) {
        synchronized (lock) {
            if (set.contains(project)) {
                LOG.fine("Removing Maven project:" + project.getArtifactRelativeRepositoryPath());
                set.remove(project);
                NbMavenProject.removePropertyChangeListener(project, projectListener);
            }
        }
        synchronized (cacheLock) {
            cachedProjects = null;
        }
        fireChange();
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
    
    private void fireChange() {
        List<ChangeListener> lst = new ArrayList<ChangeListener>();
        synchronized (listeners) {
            lst.addAll(listeners);
        }
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener change : lst) {
            change.stateChanged(event);
        }
    }
    
    /**
     * get the list of currently opened maven projects.. kind of hack, but well..
     */
    public Set<Project> getOpenedProjects() {
        synchronized (lock) {
            return new HashSet<Project>(set);
        }
    }
    
    public Set<FileObject> getOpenedProjectRoots() {
        Set<FileObject> toRet = new HashSet<FileObject>();
        synchronized (lock) {
            for (NbMavenProjectImpl prj : set) {
                //TODO have generic and other source roots included to cater for projects with external source roots
                toRet.add(prj.getProjectDirectory());
            }
        }
        return toRet;
    }
    
    public Project getOwner(URI uri) {
        LOG.finest("getOwner of uri=" + uri);
        if (uri.getScheme() != null && "file".equals(uri.getScheme())) { //NOI18N
            File file = new File(uri);
            return getOwner(file);
        }
        // for some reason nbinst:// protocol can be used as well?? WTF.
        return null;
    }
    
    public Project getOwner(FileObject fileObject) {
        LOG.finest("getOwner of fileobject=" + fileObject);
        File file = FileUtil.toFile(fileObject);
        if (file != null) {
            //logger.fatal("getOwner of fileobject=" + fileObject.getNameExt());
            return getOwner(file);
        }
        return null;
    }
    
    private Project getOwner(File file) {
        //TODO check if the file is from local repo ??
        LOG.fine("Looking for owner of " + file.getAbsolutePath());
        boolean passBasicCheck = false;
        String nm = file.getName();
        File parentVer = file.getParentFile();
        if (parentVer != null) {
            File parentArt = parentVer.getParentFile();
            if (parentArt != null) {
                if (nm.startsWith(parentArt.getName() + "-" + parentVer.getName())) {
                    passBasicCheck = true;
                }
            }
        }
        if (!passBasicCheck) {
            LOG.fine(" exiting early, not from local repository.");
            return null;
        }
        Set<NbMavenProjectImpl> currentProjects = getAllKnownProjects();
        
        Iterator<NbMavenProjectImpl> it = currentProjects.iterator();
        String filepath = file.getAbsolutePath().replace('\\', '/');
        while (it.hasNext()) {
            NbMavenProjectImpl project = it.next();
            String path = project.getArtifactRelativeRepositoryPath();
            LOG.finest("matching againts known project " + path);
            if (filepath.endsWith(path)) {
                return project;
            }
        }
        return null;
        
    }
    
     
    private Set<NbMavenProjectImpl> getAllKnownProjects() {
        return ProjectManager.mutex().readAccess(new Action<Set<NbMavenProjectImpl>>() {
            public Set<NbMavenProjectImpl> run() {
                synchronized (cacheLock) {
                    Set currentProjects;
                    List iterating;
                    if (cachedProjects != null) {
                        return new HashSet(cachedProjects);
                    }
                    synchronized (lock) {
                        currentProjects = new HashSet<NbMavenProjectImpl>(set);
                        iterating = new ArrayList(set);
                    }
                    int index = 0;
                    // iterate all opened projects and figure their subprojects.. consider these as well. do so recursively.
                    //TODO performance.. this could be expensive, maybe cache somehow
                    while (index < iterating.size()) {
                        NbMavenProjectImpl prj = (NbMavenProjectImpl) iterating.get(index);
                        SubprojectProvider sub = prj.getLookup().lookup(SubprojectProvider.class);
                        if (sub != null) {
                            Set<? extends Project> subs = sub.getSubprojects();
                            subs.removeAll(currentProjects);
                            currentProjects.addAll(subs);
                            iterating.addAll(subs);
                        }
                        index = index + 1;
                    }
                    cachedProjects = currentProjects;
                    return new HashSet<NbMavenProjectImpl>(cachedProjects);
                }
            }
        });
    }
}
