/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.localhistory;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.localhistory.store.LocalHistoryStore;
import org.netbeans.modules.localhistory.store.LocalHistoryStoreFactory;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/** 
 * 
 * A singleton Local History manager class, center of the Local History module. 
 * Use {@link #getInstance()} to get access to Local History module functionality.
 * @author Tomas Stupka
 */  
public class LocalHistory {    
      
    private static LocalHistory instance;
    private VCSInterceptor vcsInterceptor;
    private VCSAnnotator vcsAnnotator;
    private LocalHistoryStore store;

    private ListenersSupport listenerSupport = new ListenersSupport(this);
    
    private Set<File> userDefinedRoots;
    private Set<File> roots = new HashSet<File>();
       
    private Pattern includeFiles = null;
    private Pattern excludeFiles = null;

    // XXX hotfix - issue 119042
    private final Pattern metadataPattern = Pattern.compile(".*\\" + File.separatorChar + "((\\.|_)svn|.hg|CVS)(\\" + File.separatorChar + ".*|$)");
        
    public final static Object EVENT_FILE_CREATED = new Object();
    final static Object EVENT_PROJECTS_CHANGED = new Object();
        
    /** default logger for whole module */
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.localhistory"); // NOI18N
    
    public LocalHistory() {
        String include = System.getProperty("netbeans.localhistory.includeFiles");
        if(include != null && !include.trim().equals("")) {
            this.includeFiles = Pattern.compile(include);    
        }
        String exclude = System.getProperty("netbeans.localhistory.excludeFiles");
        if(exclude != null && !exclude.trim().equals("")) {
            this.excludeFiles = Pattern.compile(exclude);    
        }                                
        
        String rootPaths = System.getProperty("netbeans.localhistory.historypath");
        if(rootPaths == null || rootPaths.trim().equals("")) {            
            userDefinedRoots = Collections.EMPTY_SET;               
        } else {
            String[] paths = rootPaths.split(";");
            userDefinedRoots = new HashSet<File>(paths.length);
            for(String root : paths) {
                addRootFile(userDefinedRoots, new File(root));   
            }            
        }    
    }    

    void init() {
        LocalHistoryStore s = getLocalHistoryStore(false);
        if(s != null) {
            getLocalHistoryStore().cleanUp(LocalHistorySettings.getInstance().getTTLMillis());
        }
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {                       
                setRoots(OpenProjects.getDefault().getOpenProjects());                                
                OpenProjects.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(openProjectsListener, null));                                  
            }
        });        
    }

    private void setRoots(Project[] projects) {        
        Set<File> newRoots = new HashSet<File>();
        for(Project project : projects) {
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            for(SourceGroup group : groups) {
                FileObject fo = group.getRootFolder();
                File root = FileUtil.toFile(fo); 
                if( root == null ) {
                    LOG.warning("source group" + group.getDisplayName() + " returned null root folder" );
                } else {
                    addRootFile(newRoots, root);    
                }                
            }
            File root = FileUtil.toFile(project.getProjectDirectory()); 
            if( root == null ) {
                LOG.warning("project " + project.getProjectDirectory() + " returned null root folder" );
            } else {
                addRootFile(newRoots, root);    
            }
        }                
        synchronized(roots) {
            roots = newRoots;
        }        
        fireFileEvent(EVENT_PROJECTS_CHANGED, null);
    }
    
    private void addRootFile(Set<File> set, File file) {
        if(file == null) {
            return;
        }
        LOG.fine("adding root folder " + file);
        set.add(file);
        return;        
    }
    
    public static synchronized LocalHistory getInstance() {
        if(instance == null) {
            instance = new LocalHistory();  
        }
        return instance;
    }
    
    VCSInterceptor getVCSInterceptor() {
        if(vcsInterceptor == null) {
            vcsInterceptor = new LocalHistoryVCSInterceptor();
        }
        return vcsInterceptor;
    }    
    
    VCSAnnotator getVCSAnnotator() {
        if(vcsAnnotator == null) {
            vcsAnnotator = new LocalHistoryVCSAnnotator();
        } 
        return vcsAnnotator;
    }    

    /**
     * Creates the LocalHistoryStore
     * @return
     */
    public LocalHistoryStore getLocalHistoryStore() {
        return getLocalHistoryStore(true);
    }

    /**
     * Creates LocalHistoryStore if the storage already exists, otherwise return null
     * @param force - force creation
     * @return
     */
    public LocalHistoryStore getLocalHistoryStore(boolean force) {
        if(store == null) {
            store = LocalHistoryStoreFactory.getInstance().createLocalHistoryStorage(force);
        }
        return store;
    }
    
    File isManagedByParent(File file) {
        if(roots == null) {
            // init not finnished yet 
            return file;
        }        
        File parent = null;
        while(file != null) {
            synchronized(roots) {
                if(roots.contains(file) || userDefinedRoots.contains(file)) {
                    parent = file;
                }            
            }                        
            file = file.getParentFile();            
        }        
        return parent;    
    }
    
    boolean isManaged(File file) {
        log("isManaged() " + file);

        if(file == null) {
            return false;
        }
        String path = file.getAbsolutePath();        
        if(metadataPattern.matcher(path).matches()) {
            return false;
        }
        
        if(includeFiles != null) {
            return includeFiles.matcher(path).matches();        
        }

        if(excludeFiles != null) {
            return !excludeFiles.matcher(path).matches();        
        }                
        
        return true;
    }        
      
    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }

    void fireFileEvent(Object id, File file) {
        listenerSupport.fireVersioningEvent(id, new Object[]{file});
    }    
    
    PropertyChangeListener openProjectsListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS) ) {
                final Project[] projects = (Project[]) evt.getNewValue();
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {               
                        setRoots(projects);
                    }
                });                               
            }                    
        }            
    };

    public static void logCreate(File file, File storeFile, long ts, String  from, String to) {
        if(!LOG.isLoggable(Level.FINE)) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("create");
        sb.append('\t');
        sb.append(file.getAbsolutePath());
        sb.append('\t');        
        sb.append(storeFile.getAbsolutePath());
        sb.append('\t');        
        sb.append(ts);
        sb.append('\t');        
        sb.append(from);
        sb.append('\t');        
        sb.append(to);               
        log(sb.toString());
    }    
    
    public static void logChange(File file, File storeFile, long ts) {
        if(!LOG.isLoggable(Level.FINE)) {
            return;
        }        
        StringBuffer sb = new StringBuffer();
        sb.append("change");
        sb.append('\t');
        sb.append(file.getAbsolutePath());
        sb.append('\t');        
        sb.append(storeFile.getAbsolutePath());
        sb.append('\t');        
        sb.append(ts);        
        log(sb.toString());
    }

    public static void logDelete(File file, File storeFile, long ts) {
        if(!LOG.isLoggable(Level.FINE)) {
            return;
        }  
        StringBuffer sb = new StringBuffer();
        sb.append("delete");
        sb.append('\t');
        sb.append(file.getAbsolutePath());
        sb.append('\t');        
        sb.append(storeFile.getAbsolutePath());
        sb.append('\t');        
        sb.append(ts);        
        log(sb.toString());
    }
    
    public static void logFile(String msg, File file) {
        if(!LOG.isLoggable(Level.FINE)) {
            return;
        }        
        StringBuffer sb = new StringBuffer();        
        sb.append(msg);
        sb.append('\t');
        sb.append(file.getAbsolutePath());            
        log(sb.toString()); 
    }        
    
    public static void log(String msg) {
        if(!LOG.isLoggable(Level.FINE)) {
            return;
        }        
        StringBuffer sb = new StringBuffer();
        SimpleDateFormat defaultFormat = new SimpleDateFormat("dd-MM-yyyy:HH-mm-ss.S");
        sb.append(defaultFormat.format(new Date(System.currentTimeMillis())));
        sb.append(":");
        sb.append(msg);
        sb.append('\t');
        sb.append(Thread.currentThread().getName());            
        LocalHistory.LOG.fine(sb.toString()); // NOI18N
    }       
    
}
