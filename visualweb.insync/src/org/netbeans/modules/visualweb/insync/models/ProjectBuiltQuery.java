/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.insync.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.FileBuiltQuery;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * A query to check and monitor wheather a project can be considered to be built 
 * (up to date). This is analogous to <code>org.netbeans.api.queries.FileBuiltQuery</code>.
 * 
 * The basic idea behind this is to consider a project built if all it's file are built. This
 * implementation monitors the status of files under <code>org.netbeans.api.project.SourceGroup</code>
 * returned by <code>org.netbeans.api.project.Sources</code> using the <code>FileBuiltQuery</code>.
 * It returns a <code>ProjectBuiltQuery.Status</code> object for a specified project. Using this
 * the client code can query if the project is built. The dynamic changes to the project's built
 * state can be monitored using a <code>javax.swing.event.ChangeListener</code>.
 * 
 * NOTE: Currently only <b>java</b> <code>org.netbeans.api.project.SourceGroup<code>s are monitored.
 * 
 * @see FileBuiltQuery
 * @author Sandip Chitale
 */
public class ProjectBuiltQuery {
    
    /**
     * This returns a <code>ProjectBuiltQuery.Status</code> object which can be 
     * used to query and monitor the built status of the project. 
     * 
     * This will throw a <code>NullPointerException</code> if a <code>null</code>
     * <code>project</code> is passed in.
     * This will throw a <code>IllegalArgumentException</code> if the 
     * <code>project</code> does not have any <code>org.netbeans.api.project.SourceGroup<code>s.
     * 
     * @param project - the project for whose built status is to be monitor.
     * @return Staus - an object to monitor 
     */
    public static Status getStatus(Project project) {
        return new StatusImpl(project);
    }
    
    /**
     * An interface to monitor and query the built state of a project.
     */
    public static interface Status {
        
        /**
         * Returns the project being monitored.
         * 
         * @return project being monitored.
         */
        Project getProject();
        
        /**
         * Check whether the project is currently built.
         * 
         * This will throw a <code>IllegalStateException</code> if the 
         * <code>project</code> is not open.
         * 
         * @return true if the project is in built state, false if it may need to be built
         */
        boolean isBuilt();
        
        /**
         * Add a listener to monitor changes in the built status.
         * 
         * @param l a listener to add
         */
        void addChangeListener(ChangeListener l);
        
        /**
         * Stop listening to changes.
         * @param l a listener to remove
         */
        void removeChangeListener(ChangeListener l);
    }
    
    private static class StatusImpl implements Status, FileChangeListener, PropertyChangeListener {
        private Project project;
        
        private boolean built = false;
        
        private Map<String, Boolean> fileObjectBuiltStatusMap;
        private Map<String, FileObjectStatusChangeListener> fileObjectStatusChangeListenerMap;
        
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        
        // A simple class to monitor change in built status of a file path
        private class FileObjectStatusChangeListener implements ChangeListener {
            private final String fileObjectPath;
            private final FileBuiltQuery.Status status;
            
            FileObjectStatusChangeListener(FileBuiltQuery.Status status, String fileObjectPath) {
                this.fileObjectPath = fileObjectPath;
                this.status = status;
                this.status.addChangeListener(this);
            }
            
            public void stateChanged(ChangeEvent e) {
                FileBuiltQuery.Status status = (FileBuiltQuery.Status) e.getSource();
                if (status != null) {
                    synchronized(StatusImpl.this) {
                        if (fileObjectBuiltStatusMap == null) {
                            dispose();
                        } else {
                            fileObjectBuiltStatusMap.put(fileObjectPath, status.isBuilt());
                            update();
                        }
                    }
                }
            }
            
            void dispose() {
                status.removeChangeListener(this);                
            }
        }
        
        /**
         * Returns the project being monitored.
         * 
         * @return project being monitored.
         */
        StatusImpl(Project project) {
            this.project = project;
            
            init();
        }
       
        private void init() {
            if (project == null) {
                throw new NullPointerException();
            }
            
            Sources sources = project.getLookup().lookup(Sources.class);
            if (sources == null) {
                throw new IllegalArgumentException(NbBundle.getMessage(ProjectBuiltQuery.class, "ERROR_ProjectHasNoSources"));
            } 
            try {                
                fileObjectBuiltStatusMap = new HashMap<String, Boolean>();
                fileObjectStatusChangeListenerMap = new HashMap<String, FileObjectStatusChangeListener>();
                SourceGroup[] sourceGroups = sources.getSourceGroups("java"); // TODO Support other source groups
                for (SourceGroup group : sourceGroups) {
                    FileObject rootFileObject = group.getRootFolder();
                    if (rootFileObject != null && rootFileObject.isFolder()) {
                        Enumeration<? extends FileObject> fileObjects = rootFileObject.getChildren(true);
                        while (fileObjects.hasMoreElements()) {
                            FileObject fileObject = fileObjects.nextElement();
                            FileBuiltQuery.Status status = FileBuiltQuery.getStatus(fileObject);
                            if (status != null) {
                                String fileObjectPath = fileObject.getPath();
                                synchronized(this) {
                                    fileObjectBuiltStatusMap.put(fileObjectPath, status.isBuilt());
                                    fileObjectStatusChangeListenerMap.put(fileObjectPath, new FileObjectStatusChangeListener(status, fileObjectPath));
                                }
                            }
                        }
                    }
                }
                update();
                FileSystem fileSystem = project.getProjectDirectory().getFileSystem();
				// monitor file addition, deletion, rename 
                fileSystem.addFileChangeListener(WeakListeners.create(FileChangeListener.class, this, fileSystem));
                // Monitor project close.
                OpenProjects.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(this, OpenProjects.getDefault()));
            } catch (FileStateInvalidException e) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, e);
            }
        }
                
        public void propertyChange(PropertyChangeEvent evt) {
            if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
        		checkProjectOpen();
            }
        }
        
        public Project getProject() {
            return project;
        }

        public boolean isBuilt() {
            if (!checkProjectOpen()) {
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
            	    NbBundle.getMessage(ProjectBuiltQuery.class, "ERROR_ProjectIsNotOpen"));
            }
            return built;
        }
        
        public void addChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.add(l);
            }
        }
        
        public void removeChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.remove(l);
            }
        }
        
        private void fireChange() {
            ChangeListener[] _listeners;
            synchronized (listeners) {
                if (listeners.isEmpty()) {
                    return;
                }
                _listeners = listeners.toArray(new ChangeListener[listeners.size()]);
            }
            ChangeEvent ev = new ChangeEvent(this);
            for (ChangeListener l : _listeners) {
                l.stateChanged(ev);
            }
        }       

        public void fileAttributeChanged(FileAttributeEvent fe) {
            // Ignore
        }

        public void fileChanged(FileEvent fe) {
            // Ignore
        }

        public void fileDataCreated(FileEvent fe) {
            FileObject fileObject = fe.getFile();
            Project owner = FileOwnerQuery.getOwner(fileObject);
            if (owner == project) {
                FileBuiltQuery.Status status = FileBuiltQuery.getStatus(fileObject);
                if (status != null) {
                    String fileObjectPath = fileObject.getPath();
                    synchronized(this) {
                        fileObjectBuiltStatusMap.put(fileObjectPath, status.isBuilt());
                        fileObjectStatusChangeListenerMap.put(fileObjectPath, new FileObjectStatusChangeListener(status, fileObjectPath));
                    }
                    update();
                }
            }
        }

        public void fileDeleted(FileEvent fe) {
            FileObject fileObject = fe.getFile();
            Project owner = FileOwnerQuery.getOwner(fileObject);
            if (owner == project) {
                synchronized (this) {
                    if (fileObjectBuiltStatusMap == null) {
                        return;
                    }
                    String fileObjectPath = fileObject.getPath();
					fileObjectBuiltStatusMap.remove(fileObjectPath);
					FileObjectStatusChangeListener fileObjectStatusChangeListener = 
						fileObjectStatusChangeListenerMap.remove(fileObjectPath);
					if (fileObjectStatusChangeListener != null) {
						fileObjectStatusChangeListener.dispose();
					}
                }
                update();
            }
        }

        public void fileFolderCreated(FileEvent fe) {
            // Ignore
        }

        public void fileRenamed(FileRenameEvent fe) {
            FileObject fileObject = fe.getFile();
            Project owner = FileOwnerQuery.getOwner(fileObject);
            if (owner == project) {
                String ext = fe.getExt();                
                String fileObjectPathBeforeRename = 
                	fileObject.getParent().getPath() + "/" + fe.getName() + (ext.length() == 0 ? ext : "." + ext);
                FileBuiltQuery.Status status = FileBuiltQuery.getStatus(fileObject);
                String fileObjectPath = fileObject.getPath();
                synchronized (this) {
                    fileObjectBuiltStatusMap.remove(fileObjectPathBeforeRename);
                    FileObjectStatusChangeListener fileObjectStatusChangeListener =
                    	fileObjectStatusChangeListenerMap.remove(fileObjectPathBeforeRename);
                    if (fileObjectStatusChangeListener != null) {
						fileObjectStatusChangeListener.dispose();
					}
                    if (status != null) {
                        fileObjectBuiltStatusMap.put(fileObjectPath, status.isBuilt());
                        fileObjectStatusChangeListenerMap.put(fileObjectPath, new FileObjectStatusChangeListener(status, fileObjectPath));
                    }
                }
                update();
            }
        }
        
        private void update() {
            synchronized (this) {                
                boolean newBuiltStatus = true;
                for (String fileObjectPath : fileObjectBuiltStatusMap.keySet()) {
                    boolean fileObjectBuilt = fileObjectBuiltStatusMap.get(fileObjectPath);
                    if (!fileObjectBuilt) {
                        newBuiltStatus = false;
                        break;
                    }
                }
                
                if (built != newBuiltStatus) {
                    built = newBuiltStatus;
                    fireChange();
                }
            }
        }
        
        private boolean isProjectOpen() {
            if (project == null) {
                return false;
            }
            return Arrays.asList(OpenProjects.getDefault().getOpenProjects()).contains(project);
        }

		private boolean checkProjectOpen() {
            boolean open = isProjectOpen();
            if (!open) {
                dispose();
            }
            return open;
        }
        
        private void dispose() {
        	if (project == null) {
        		return;
        	}
            project = null;
            fileObjectBuiltStatusMap = null;
            synchronized(this) {
                for (FileObjectStatusChangeListener fileObjectStatusChangeListener: fileObjectStatusChangeListenerMap.values()) {
                    fileObjectStatusChangeListener.dispose();
                }
            }
            fileObjectStatusChangeListenerMap = null;
            listeners = null;
        }
    }
}

