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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.localhistory.store.LocalHistoryStore;
import org.netbeans.modules.localhistory.store.LocalHistoryStoreFactory;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.ui.history.HistorySettings;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.*;
import org.openide.util.Lookup.Result;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

/** 
 * 
 * A singleton Local History manager class, center of the Local History module. 
 * Use {@link #getInstance()} to get access to Local History module functionality.
 * @author Tomas Stupka
 */  
public class LocalHistory {    
      
    private static LocalHistory instance;
    private LocalHistoryVCSInterceptor vcsInterceptor;
    private VCSAnnotator vcsAnnotator;
    private VCSHistoryProvider vcsHistoryProvider;
    private LocalHistoryStore store;

    private ListenersSupport listenerSupport = new ListenersSupport(this);
    
    private Set<File> userDefinedRoots;
    private final Set<File> roots = new HashSet<File>();
       
    private Pattern includeFiles = null;
    private Pattern excludeFiles = null;

    public static final String LH_TMP_FILE_SUFFIX = ".nblh~";                   // NOI18N
    
    // XXX hotfix - issue 119042
    private final Pattern metadataPattern = Pattern.compile(".*\\" + File.separatorChar + "((\\.|_)svn|.hg|CVS)(\\" + File.separatorChar + ".*|$)");
    private final Pattern lhTmpFilePattern = Pattern.compile(".*\\.\\d+?\\" + LH_TMP_FILE_SUFFIX);
        
    public final static Object EVENT_FILE_CREATED = new Object();
    final static Object EVENT_PROJECTS_CHANGED = new Object();
        
    /** default logger for whole module */
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.localhistory"); // NOI18N

    /** holds all files which are actually opened */
    private final Set<String> openedFiles = new HashSet<String>();
    /** holds all files which where opened at some time during this nb session and changed */
    private final Set<String> touchedFiles = new HashSet<String>();
    
    private LocalHistoryVCS lhvcs;
    private RequestProcessor parallelRP;

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

        WindowManager.getDefault().addWindowSystemListener(new WindowSystemListener() {
            @Override public void beforeLoad(WindowSystemEvent event) {}
            @Override public void afterLoad(WindowSystemEvent event) {
                WindowManager.getDefault().removeWindowSystemListener(this);
                WindowManager.getDefault().getRegistry().addPropertyChangeListener(new OpenedFilesListener());
            }
            @Override public void beforeSave(WindowSystemEvent event) {}
            @Override public void afterSave(WindowSystemEvent event) {}
        });
    }

    private synchronized LocalHistoryVCS getLocalHistoryVCS() {
        if (lhvcs == null) {
            lhvcs = org.openide.util.Lookup.getDefault().lookup(LocalHistoryVCS.class);
        }
        return lhvcs;
    }

    void init() {
        if(!HistorySettings.getInstance().getKeepForever()) {
            LocalHistoryStore s = getLocalHistoryStore(false);
            if(s != null) {
                getLocalHistoryStore().cleanUp(HistorySettings.getInstance().getTTLMillis());
            }
        }
        getParallelRequestProcessor().post(new Runnable() {
            @Override
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
                    LOG.log(Level.WARNING, "source group{0} returned null root folder", group.getDisplayName());
                } else {
                    addRootFile(newRoots, root);    
                }                
            }
            File root = FileUtil.toFile(project.getProjectDirectory()); 
            if( root == null ) {
                LOG.log(Level.WARNING, "project {0} returned null root folder", project.getProjectDirectory());
            } else {
                addRootFile(newRoots, root);    
            }
        }                
        synchronized(roots) {
            roots.clear();
            roots.addAll(newRoots);
        }        
        fireFileEvent(EVENT_PROJECTS_CHANGED, null);
    }
    
    private void addRootFile(Set<File> set, File file) {
        if(file == null) {
            return;
        }
        LOG.log(Level.FINE, "adding root folder {0}", file);
        set.add(file);
    }
    
    public static synchronized LocalHistory getInstance() {
        if(instance == null) {
            instance = new LocalHistory();  
        }
        return instance;
    }
    
    LocalHistoryVCSInterceptor getVCSInterceptor() {
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

    VCSHistoryProvider getVCSHistoryProvider() {
        if(vcsHistoryProvider == null) {
            vcsHistoryProvider = new LocalHistoryProvider();
        } 
        return vcsHistoryProvider;
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
    
    void touch(File file) {
        if(!isOpened(file)) {
            return;
        }
        synchronized(touchedFiles) {
            touchedFiles.add(file.getAbsolutePath());
        }
        synchronized(openedFiles) {
            openedFiles.remove(file.getAbsolutePath());
        }
    }

    private boolean isOpened(File file) {
        boolean opened;
        synchronized(openedFiles) {
            opened = openedFiles.contains(file.getAbsolutePath());
        }
        if(LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, " file {0} {1}", new Object[]{file, opened ? "is opened" : "isn't opened"});
        }
        return opened;
    }

    boolean isOpenedOrTouched(File file) {
        if(isOpened(file)) {
            return true;
        }
        
        boolean touched;
        synchronized(touchedFiles) {
            touched = touchedFiles.contains(file.getAbsolutePath());
        }
        if(LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, " file {0} {1}", new Object[]{file, touched ? "is touched" : "isn't touched"});
        }
        return touched;
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
        
        if(lhTmpFilePattern.matcher(path).matches()) {
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
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS) ) {
                final Project[] projects = (Project[]) evt.getNewValue();
                getParallelRequestProcessor().post(new Runnable() {
                    @Override
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
        StringBuilder sb = new StringBuilder();
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
        StringBuilder sb = new StringBuilder();
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
        StringBuilder sb = new StringBuilder();
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
        StringBuilder sb = new StringBuilder();        
        sb.append(msg);
        sb.append('\t');
        sb.append(file.getAbsolutePath());            
        log(sb.toString()); 
    }        
    
    public static void log(String msg) {
        if(!LOG.isLoggable(Level.FINE)) {
            return;
        }        
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat defaultFormat = new SimpleDateFormat("dd-MM-yyyy:HH-mm-ss.S");
        sb.append(defaultFormat.format(new Date(System.currentTimeMillis())));
        sb.append(":");
        sb.append(msg);
        sb.append('\t');
        sb.append(Thread.currentThread().getName());            
        LocalHistory.LOG.fine(sb.toString()); // NOI18N
    }

    public RequestProcessor getParallelRequestProcessor() {
        if (parallelRP == null) {
            parallelRP = new RequestProcessor("LocalHistory.ParallelTasks", 5, true); //NOI18N
        }
        return parallelRP;
    }

    private class OpenedFilesListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (Registry.PROP_TC_OPENED.equals(evt.getPropertyName())) {
                Object obj = evt.getNewValue();
                if (obj instanceof TopComponent) {
                    TopComponent tc = (TopComponent) obj;
                    addOpenedFiles(getFiles(tc));
                }
            } else if (Registry.PROP_TC_CLOSED.equals(evt.getPropertyName())) {
                Object obj = evt.getNewValue();
                if (obj instanceof TopComponent) {
                    TopComponent tc = (TopComponent) obj;
                    removeOpenedFiles(getFiles(tc));
                    removeLookupListeners(tc);
                }
            }
        }

        private void addLookupListener(TopComponent tc) {
            Result<DataObject> r = tc.getLookup().lookupResult(DataObject.class);
            L l = new L(new WeakReference<TopComponent>(tc), r);
            synchronized(lookupListeners) {
                lookupListeners.add(l);
            }
            r.addLookupListener(l);
        }

        private void removeLookupListeners(TopComponent tc) {
            synchronized(lookupListeners) {
                Iterator<L> it = lookupListeners.iterator();
                synchronized(lookupListeners) {
                    while(it.hasNext()) {
                        L l = it.next();
                        if(l.ref.get() == null) {
                            l.r.removeLookupListener(l);
                            it.remove();
                        }
                        if(l.ref.get() == tc) {
                            l.r.removeLookupListener(l);
                            it.remove();
                        }
                    }
                }                    
            }
        }

        private void addOpenedFiles(List<File> files) {
            if(files == null) {
                return;
            }
            synchronized (openedFiles) {
                for (File file : files) {
                    LOG.log(Level.FINE, " adding to opened files : ", new Object[]{file});
                    openedFiles.add(file.getAbsolutePath());
                }
                for (File file : files) {
                    if (handleManaged(file)) {
                        break;
                    }
                }
            }
        }
        
        private void removeOpenedFiles(List<File> files) {
            if(files == null) {
                return;
            }
            synchronized (openedFiles) {
                for (File file : files) {
                    LOG.log(Level.FINE, " removing from opened files {0} ", new Object[]{file});
                    openedFiles.remove(file.getAbsolutePath());
                }
            }
        }
        
        private List<File> getFiles(TopComponent tc) {
            LOG.log(Level.FINER, " looking up files in tc {0} ", new Object[]{tc});
            DataObject tcDataObject = tc.getLookup().lookup(DataObject.class);
            if(tcDataObject == null) {
                boolean alreadyListening = false;
                Iterator<L> it = lookupListeners.iterator();
                synchronized(lookupListeners) {
                    while(it.hasNext()) {
                        L l = it.next();
                        if(l.ref.get() == null) {
                            l.r.removeLookupListener(l);
                            it.remove();
                        }
                        if(l.ref.get() == tc) {
                           alreadyListening = true;
                           break;
                        }
                    }
                }
                if(!alreadyListening) {
                    addLookupListener(tc);
                }
                return Collections.EMPTY_LIST;
            } else {
                try {
                    return hasOpenedEditorPanes(tcDataObject) ? getFiles(tcDataObject) : Collections.EMPTY_LIST;
                } catch (InterruptedException ex) {
                    LOG.log(Level.WARNING, null, ex);
                } catch (InvocationTargetException ex) {
                    LOG.log(Level.WARNING, null, ex);
                }
            }
            return Collections.EMPTY_LIST;
        }

        private List<File> getFiles(DataObject tcDataObject) {
            List<File> ret = new ArrayList<File>();
            LOG.log(Level.FINER, "  looking up files in dataobject {0} ", new Object[]{tcDataObject});
            Set<FileObject> fos = tcDataObject.files();
            if(fos != null) {
                for (FileObject fo : fos) {
                    LOG.log(Level.FINER, "   found file {0}", new Object[]{fo});
                    File f = FileUtil.toFile(fo);
                    if (f != null && !openedFiles.contains(f.getAbsolutePath()) && !touchedFiles.contains(f.getAbsolutePath())) {
                        ret.add(f);
                    }
                }
            }
            if(LOG.isLoggable(Level.FINER)) {
                for (File f : ret) {
                    LOG.log(Level.FINER, "   returning file {0} ", new Object[]{f});
                }
            }
            return ret;
        }
        private boolean handleManaged(File file) {
            if (isManagedByParent(file) != null) {
                return false;
            }
            LocalHistoryVCS lh = getLocalHistoryVCS();
            if(lh == null) {
                return false;
            }
            lh.managedFilesChanged();
            return true;
        }
        
        private final List<L> lookupListeners = new ArrayList<L>();
        
        private class L implements LookupListener {
            private final Reference<TopComponent> ref;
            private final Result<DataObject> r;

            public L(Reference<TopComponent> ref, Result<DataObject> r) {
                this.ref = ref;
                this.r = r;
            }
            
            @Override
            public void resultChanged(LookupEvent ev) {
                TopComponent tc = ref.get();
                if(tc == null) {
                    r.removeLookupListener(this);
                    synchronized(lookupListeners) {
                        lookupListeners.remove(this);
                    }                    
                    return;
                }
                if(LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "  looking result changed for {0} ", new Object[]{ref.get()});
                }
                DataObject tcDataObject = tc.getLookup().lookup(DataObject.class);
                if(tcDataObject != null) {
                    try {
                        if(hasOpenedEditorPanes(tcDataObject)) {
                            addOpenedFiles(getFiles(tcDataObject));
                        }
                    } catch (InterruptedException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    } catch (InvocationTargetException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    }
                    r.removeLookupListener(this);
                    synchronized(lookupListeners) {
                        lookupListeners.remove(this);
                    }
                }
            }
        }
        
        /**
         * Determines if the given DataObject has an opened editor
         * @param dataObject
         * @return true if the given DataObject has an opened editor. Otherwise false.
         * @throws InterruptedException
         * @throws InvocationTargetException 
         */
        private boolean hasOpenedEditorPanes(final DataObject dataObject) throws InterruptedException, InvocationTargetException {
            final boolean[] hasEditorPanes = new boolean[] {false};
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    final EditorCookie cookie = dataObject.getLookup().lookup(EditorCookie.class);
                    if(cookie != null) {
                        // hack - care only about dataObjects with opened editors.
                        // otherwise we won't assume it's file were opened to be edited
                        JEditorPane pane = NbDocument.findRecentEditorPane(cookie);
                        if(pane == null) {
                            if(cookie instanceof EditorCookie.Observable) {
                                final EditorCookie.Observable o = (EditorCookie.Observable) cookie;
                                PropertyChangeListener l = new PropertyChangeListener() {
                                    @Override
                                    public void propertyChange(PropertyChangeEvent evt) {
                                        if(EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
                                            addOpenedFiles(getFiles(dataObject));
                                            o.removePropertyChangeListener(this);
                                        }
                                    }
                                };
                                o.addPropertyChangeListener(l);
                                pane = NbDocument.findRecentEditorPane(cookie);
                                if(pane != null) {
                                    hasEditorPanes[0] = true;
                                    o.removePropertyChangeListener(l);
                                }
                            } else {
                                JEditorPane[] panes = cookie.getOpenedPanes();
                                hasEditorPanes[0] = panes != null && panes.length > 0;
                            }
                        } else {
                            hasEditorPanes[0] = true;
                        }
                    }
                }
            };
            if(SwingUtilities.isEventDispatchThread()) { 
                r.run();
            } else {
                SwingUtilities.invokeAndWait(r);
            }
            return hasEditorPanes[0];
        }          

    }
    
}
