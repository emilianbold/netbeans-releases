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

package org.netbeans.modules.project.ui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.api.UnloadedProjectInformation;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.ModuleInfo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.WindowManager;

/**
 * List of projects open in the GUI.
 * @author Petr Hrebejk
 */
public final class OpenProjectList {
    
    public static final Comparator<Project> PROJECT_BY_DISPLAYNAME = new ProjectByDisplayNameComparator();
    
    // Property names
    public static final String PROPERTY_OPEN_PROJECTS = "OpenProjects";
    public static final String PROPERTY_MAIN_PROJECT = "MainProject";
    public static final String PROPERTY_RECENT_PROJECTS = "RecentProjects";
    public static final String PROPERTY_REPLACE = "ReplaceProject";
    
    private static OpenProjectList INSTANCE;
    
    // number of templates in LRU list
    private static final int NUM_TEMPLATES = 15;
    
    static final RequestProcessor OPENING_RP = new RequestProcessor("Opening projects", 1);

    static final Logger LOGGER = Logger.getLogger(OpenProjectList.class.getName());
    static StringBuffer details;
    static {
        boolean ea = false;
        assert ea = true;
        if (ea) {
            details = new StringBuffer();
        }
    }
    static void log(LogRecord r) {
        LOGGER.log(r);
        printMsg(r.getMessage(), r.getParameters());
    }
    static void log(Level l, String msg, Object... params) {
        LOGGER.log(l, msg, params);
        printMsg(msg, params);
    }
    static void log(Level l, String msg, Throwable e) {
        LOGGER.log(l, msg, e);
        printMsg(msg, e);
    }
    private static void printMsg(String msg, Object... params) {
        StringBuffer sb = details;
        if (sb != null) {
            sb.append(msg);
            for (Object p : params) {
                sb.append("\n  ").append(p);
            }
            sb.append("\n");
        }
    }


    /** List which holds the open projects */
    private List<Project> openProjects;
    private final HashMap<ModuleInfo, List<Project>> openProjectsModuleInfos;
    
    /** Main project */
    private Project mainProject;
    
    /** List of recently closed projects */
    private final RecentProjectList recentProjects;

    /** LRU List of recently used templates */
    private final List<String> recentTemplates;
    
    /** Property change listeners */
    private final PropertyChangeSupport pchSupport;
    
    private ProjectDeletionListener deleteListener = new ProjectDeletionListener();
    private NbProjectDeletionListener nbprojectDeleteListener = new NbProjectDeletionListener();
    
    private PropertyChangeListener infoListener;
    private final LoadOpenProjects LOAD;
    
    OpenProjectList() {
        LOAD = new LoadOpenProjects(0);
        openProjects = new ArrayList<Project>();
        openProjectsModuleInfos = new HashMap<ModuleInfo, List<Project>>();
        infoListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evn) {
                if (ModuleInfo.PROP_ENABLED.equals(evn.getPropertyName())) {
                    checkModuleInfo((ModuleInfo)evn.getSource());
                }
            }
        };
        pchSupport = new PropertyChangeSupport( this );
        recentProjects = new RecentProjectList(10); // #47134
        recentTemplates = new ArrayList<String>();
    }
    
           
    // Implementation of the class ---------------------------------------------
    
    public static OpenProjectList getDefault() {
        synchronized ( OpenProjectList.class ) {
            if ( INSTANCE == null ) {
                INSTANCE = new OpenProjectList();
                INSTANCE.openProjects = loadProjectList();
                // Load recent project list
                INSTANCE.recentProjects.load();
                WindowManager.getDefault().invokeWhenUIReady(INSTANCE.LOAD);
            }
        }
        return INSTANCE;
    }
    
    static void waitProjectsFullyOpen() {
        getDefault().LOAD.waitFinished();
    }

    static void preferredProject(Project lazyP) {
        if (lazyP != null) {
            getDefault().LOAD.preferredProject(lazyP);
        }
    }
    
    Future<Project[]> openProjectsAPI() {
        return LOAD;
    }

    final Project unwrapProject(Project wrap) {
        Project[] now = getOpenProjects();

        if (wrap instanceof LazyProject) {
            LazyProject lp = (LazyProject)wrap;
            for (Project p : now) {
                if (lp.getProjectDirectory().equals(p.getProjectDirectory())) {
                    return p;
                }
            }
        }
        return wrap;
    }

    /** Modifications to the recentTemplates variables shall be done only 
     * when hodling a lock.
     * @return the list
     */
    private List<String> getRecentTemplates() {
        assert Thread.holdsLock(this);
        return recentTemplates;
    }
    
    private final class LoadOpenProjects implements Runnable, LookupListener, Future<Project[]> {
        final RequestProcessor RP = new RequestProcessor("Load Open Projects"); // NOI18N
        final RequestProcessor.Task TASK = RP.create(this);
        private int action;
        private final LinkedList<Project> toOpenProjects = new LinkedList<Project>();
        private List<Project> lazilyOpenedProjects;
        private List<String> recentTemplates;
        private Project lazyMainProject;
        private Lookup.Result<FileObject> currentFiles;
        private int entered;
        private final Lock enteredGuard = new ReentrantLock();
        private final Condition enteredZeroed = enteredGuard.newCondition();
        private final ProgressHandle progress;
        
        public LoadOpenProjects(int a) {
            action = a;
            currentFiles = Utilities.actionsGlobalContext().lookupResult(FileObject.class);
            currentFiles.addLookupListener(WeakListeners.create(LookupListener.class, this, currentFiles));
            progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(OpenProjectList.class, "CAP_Opening_Projects"));
        }

        final void waitFinished() {
            log(Level.FINER, "waitFinished, action {0}", action); // NOI18N
            if (action == 0) {
                run();
            }
            log(Level.FINER, "waitFinished, before wait"); // NOI18N
            TASK.waitFinished();
            log(Level.FINER, "waitFinished, after wait"); // NOI18N
        }
        
        public void run() {
            log(Level.FINE, "LoadOpenProjects.run: {0}", action); // NOI18N
            switch (action) {
                case 0: 
                    action = 1;
                    TASK.schedule(0);
                    resultChanged(null);
                    return;
                case 1:
                    if (!RP.isRequestProcessorThread()) {
                        return;
                    }
                    action = 2;
                    try {
                        progress.start();
                        loadOnBackground();
                    } finally {
                        progress.finish();
                    }
                    updateGlobalState();
                    StringBuffer os = details;
                    boolean verify = false;
                    assert verify = true;
                    if (verify) {
                        ProjectsRootNode.checkNoLazyNode(os);
                    }
                    details = null;
                    os = null;
                    return;
                case 2:
                    // finished, oK
                    return;
                default:
                    throw new IllegalStateException("unknown action: " + action);
            }
        }

        final void preferredProject(Project lazyP) {
            synchronized (toOpenProjects) {
                for (Project p : toOpenProjects) {
                    if (p.getProjectDirectory().equals(lazyP.getProjectDirectory())) {
                        toOpenProjects.remove(p);
                        toOpenProjects.addFirst(p);
                        return;
                    }
                }
            }
        }

        private void updateGlobalState() {
            log(Level.FINER, "updateGlobalState"); // NOI18N
            synchronized (INSTANCE) {
                INSTANCE.openProjects = lazilyOpenedProjects;
                log(Level.FINER, "openProjects changed: {0}", lazilyOpenedProjects); // NOI18N
                if (lazyMainProject != null) {
                    INSTANCE.mainProject = lazyMainProject;
                }
                INSTANCE.mainProject = unwrapProject(INSTANCE.mainProject);
                INSTANCE.getRecentTemplates().addAll(recentTemplates);
                log(Level.FINER, "updateGlobalState, applied"); // NOI18N
            }
            
            INSTANCE.pchSupport.firePropertyChange(PROPERTY_OPEN_PROJECTS, new Project[0], lazilyOpenedProjects.toArray(new Project[0]));
            INSTANCE.pchSupport.firePropertyChange(PROPERTY_MAIN_PROJECT, null, INSTANCE.mainProject);

            log(Level.FINER, "updateGlobalState, done, notified"); // NOI18N
        }
            
        private void loadOnBackground() {
            lazilyOpenedProjects = new ArrayList<Project>();
            List<URL> URLs = OpenProjectListSettings.getInstance().getOpenProjectsURLs();
            toOpenProjects.addAll(URLs2Projects(URLs));
            Project[] inital;
            synchronized (toOpenProjects) {
                log(Level.FINER, "loadOnBackground {0}", toOpenProjects); // NOI18N
                inital = toOpenProjects.toArray(new Project[0]);
            }
            recentTemplates = new ArrayList<String>( OpenProjectListSettings.getInstance().getRecentTemplates() );
            URL mainProjectURL = OpenProjectListSettings.getInstance().getMainProjectURL();
            synchronized (toOpenProjects) {
                for( Iterator it = toOpenProjects.iterator(); it.hasNext(); ) {
                    Project p = (Project)it.next();
                    INSTANCE.addModuleInfo(p);
                    // Set main project
                    try {
                        if ( mainProjectURL != null && 
                             mainProjectURL.equals( p.getProjectDirectory().getURL() ) ) {
                            lazyMainProject = p;
                        }
                    }
                    catch( FileStateInvalidException e ) {
                        // Not a main project
                    }
                }          
            }
            int max = toOpenProjects.size();
            progress.switchToDeterminate(max);
            for (;;) {
                Project p;
                synchronized (toOpenProjects) {
                    if (toOpenProjects.isEmpty()) {
                        break;
                    }
                    p = toOpenProjects.remove();
                    log(Level.FINER, "after remove {0}", toOpenProjects); // NOI18N
                }
                log(Level.FINE, "about to open a project {0}", p); // NOI18N
                if (notifyOpened(p)) {
                    lazilyOpenedProjects.add(p);
                    log(Level.FINE, "notify opened {0}", p); // NOI18N
                    PropertyChangeEvent ev = new PropertyChangeEvent(this, PROPERTY_REPLACE, null, p);
                    try {
                        pchSupport.firePropertyChange(ev);
                    } catch (Throwable t) {
                        log(Level.WARNING, "broken node for {0}", t);
                    }
                    log(Level.FINE, "property change notified {0}", p); // NOI18N
                } else {
                    // opened failed, remove main project if same.
                    if (lazyMainProject == p) {
                        lazyMainProject = null;
                    }
                }
                progress.progress(max - toOpenProjects.size());
            }

            if (inital != null) {
                log(createRecord("UI_INIT_PROJECTS", inital),"org.netbeans.ui.projects");
                log(createRecordMetrics("USG_PROJECT_OPEN", inital),"org.netbeans.ui.metrics.projects");
            }

        }

        public void resultChanged(LookupEvent ev) {
            for (FileObject fileObject : currentFiles.allInstances()) {
                Project p = FileOwnerQuery.getOwner(fileObject);
                OpenProjectList.preferredProject(p);
            }

        }

        final void enter() {
            try {
                enteredGuard.lock();
                entered++;
            } finally {
                enteredGuard.unlock();
            }
        }
    
        final void exit() {
            try {
                enteredGuard.lock();
                if (--entered == 0) {
                    enteredZeroed.signalAll();
                }
            } finally {
                enteredGuard.unlock();
            }
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        public boolean isCancelled() {
            return false;
        }

        public boolean isDone() {
            return TASK.isFinished() && entered == 0;
        }

        public Project[] get() throws InterruptedException, ExecutionException {
            TASK.waitFinished();
            try {
                enteredGuard.lock();
                while (entered > 0) {
                    enteredZeroed.await();
                }
            } finally {
                enteredGuard.unlock();
            }
            return getDefault().getOpenProjects();
        }

        public Project[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            long ms = unit.convert(timeout, TimeUnit.MILLISECONDS);
            if (!TASK.waitFinished(timeout)) {
                throw new TimeoutException();
            } 
            try {
                enteredGuard.lock();
                if (entered > 0) {
                    if (!enteredZeroed.await(ms, TimeUnit.MILLISECONDS)) {
                        throw new TimeoutException();
                    }
                }
            } finally {
                enteredGuard.unlock();
            }
            return getDefault().getOpenProjects();
        }
    }
    
    public void open( Project p ) {
        open( new Project[] {p}, false );
    }

    public void open (Project p, boolean openSubprojects ) {
        open( new Project[] {p}, openSubprojects );
    }

    public void open( Project[] projects, boolean openSubprojects ) {
	open(projects, openSubprojects, false);
    }
    
    public void open(final Project[] projects, final boolean openSubprojects, final boolean asynchronously) {
        open(projects, openSubprojects, asynchronously, null);
    }
    
    public void open(final Project[] projects, final boolean openSubprojects, final boolean asynchronously, final Project/*|null*/ mainProject) {
        if (projects.length == 0) {
            //nothing to do:
            return ;
        }
        
        long start = System.currentTimeMillis();
        
	if (asynchronously) {
            if (!EventQueue.isDispatchThread()) { // #89935
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        open(projects, openSubprojects, asynchronously, mainProject);
                    }
                });
                return;
            }
	    final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(OpenProjectList.class, "CAP_Opening_Projects"));
	    final Frame mainWindow = WindowManager.getDefault().getMainWindow();
	    final JDialog dialog = new JDialog(mainWindow, NbBundle.getMessage(OpenProjectList.class, "LBL_Opening_Projects_Progress"), true);
            final OpeningProjectPanel panel = new OpeningProjectPanel(handle);
            
	    dialog.getContentPane().add(panel);
	    dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); //make sure the dialog is not closed during the project open
	    dialog.pack();
	    
	    Rectangle bounds = mainWindow.getBounds();
	    
	    int middleX = bounds.x + bounds.width / 2;
	    int middleY = bounds.y + bounds.height / 2;
	    
	    Dimension size = dialog.getPreferredSize();
	    
	    dialog.setBounds(middleX - size.width / 2, middleY - size.height / 2, size.width, size.height);
	    
	    OPENING_RP.post(new Runnable() {
		public void run() {
		    try {
			doOpen(projects, openSubprojects, handle, panel);
                        if (mainProject != null && Arrays.asList(projects).contains(mainProject) && openProjects.contains(mainProject)) {
                            setMainProject(mainProject);
                        }
		    } finally {
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
                                //fix for #67114:
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    // ignored
                                }
                                dialog.setVisible(false);
                                dialog.dispose();
			    }
			});
		    }
		}
	    });
	    
	    dialog.setVisible(true);
	} else {
	    doOpen(projects, openSubprojects, null, null);
            if (mainProject != null && Arrays.asList(projects).contains(mainProject) && openProjects.contains(mainProject)) {
                setMainProject(mainProject);
            }
	}
        
        long end = System.currentTimeMillis();
        
        if (LOGGER.isLoggable(Level.FINE)) {
            log(Level.FINE, "opening projects took: " + (end - start) + "ms");
        }
    }
    
    private void doOpen(Project[] projects, boolean openSubprojects, ProgressHandle handle, OpeningProjectPanel panel) {
        assert !Arrays.asList(projects).contains(null) : "Projects can't be null";
        LOAD.waitFinished();
            
            
        try {
            LOAD.enter();
        boolean recentProjectsChanged = false;
        int  maxWork = 1000;
        int  workForSubprojects = maxWork / 2;
        double currentWork = 0;
        Collection<Project> projectsToOpen = new LinkedHashSet<Project>();
        
	if (handle != null) {
	    handle.start(maxWork);
	    handle.progress(0);
	}
        
        if (panel != null) {
            assert projects.length > 0 : "at least one project to open";
            
            panel.setProjectName(ProjectUtils.getInformation(projects[0]).getDisplayName());
        }
        
        Map<Project,Set<? extends Project>> subprojectsCache = new HashMap<Project,Set<? extends Project>>(); // #59098

        List<Project> toHandle = new LinkedList<Project>(Arrays.asList(projects));
        
        while (!toHandle.isEmpty()) {
            Project p = toHandle.remove(0);
            Set<? extends Project> subprojects = openSubprojects ? subprojectsCache.get(p) : Collections.<Project>emptySet();
            
            if (subprojects == null) {
                SubprojectProvider spp = p.getLookup().lookup(SubprojectProvider.class);
                if (spp != null) {
                    subprojects = spp.getSubprojects();
                } else {
                    subprojects = Collections.emptySet();
                }
                subprojectsCache.put(p, subprojects);
            }
            
            projectsToOpen.add(p);
            
            for (Project sub : subprojects) {
                if (!projectsToOpen.contains(sub) && !toHandle.contains(sub)) {
                    toHandle.add(sub);
                }
            }
            
            double workPerOneProject = (workForSubprojects - currentWork) / (toHandle.size() + 1);
            int lastState = (int) currentWork;
            
            currentWork += workPerOneProject;
            
            if (handle != null && lastState < (int) currentWork) {
                handle.progress((int) currentWork);
            }
        }
        
        double workPerProject = (maxWork - workForSubprojects) / projectsToOpen.size();
        
        final List<Project> oldprjs = new ArrayList<Project>();
        final List<Project> newprjs = new ArrayList<Project>();
        synchronized (this) {
            oldprjs.addAll(openProjects);
        }
        
        for (Project p: projectsToOpen) {
            
            if (panel != null) {
                panel.setProjectName(ProjectUtils.getInformation(p).getDisplayName());
            }
            
            recentProjectsChanged |= doOpenProject(p);
            
            int lastState = (int) currentWork;
            
            currentWork += workPerProject;
            
            if (handle != null && lastState < (int) currentWork) {
                handle.progress((int) currentWork);
            }
        }
        
        synchronized ( this ) {
            newprjs.addAll(openProjects);
            saveProjectList( openProjects );
            if ( recentProjectsChanged ) {
                recentProjects.save();
            }
        }
        
	if (handle != null) {
	    handle.finish();
	}
        
        final boolean recentProjectsChangedCopy = recentProjectsChanged;
        
        LogRecord[] addedRec = createRecord("UI_OPEN_PROJECTS", projectsToOpen.toArray(new Project[0])); // NOI18N
        log(addedRec,"org.netbeans.ui.projects");
        addedRec = createRecordMetrics("USG_PROJECT_OPEN", projectsToOpen.toArray(new Project[0])); // NOI18N
        log(addedRec,"org.netbeans.ui.metrics.projects");
        
        Mutex.EVENT.readAccess(new Action<Void>() {
            public Void run() {
                pchSupport.firePropertyChange( PROPERTY_OPEN_PROJECTS, oldprjs.toArray(new Project[oldprjs.size()]), 
                                                                       newprjs.toArray(new Project[newprjs.size()]) );
                if ( recentProjectsChangedCopy ) {
                    pchSupport.firePropertyChange( PROPERTY_RECENT_PROJECTS, null, null );
                }
                
                return null;
            }
        });
        } finally {
            LOAD.exit();
    }
    }
       
    public void close( Project someProjects[], boolean notifyUI ) {
        LOAD.waitFinished();
        
        Project[] projects = new Project[someProjects.length];
        for (int i = 0; i < someProjects.length; i++) {
            projects[i] = unwrapProject(someProjects[i]);
        }
        
        
        if (!ProjectUtilities.closeAllDocuments (projects, notifyUI )) {
            return;
        }
        
        try {
            LOAD.enter();

        logProjects("close(): closing project: ", projects);
        boolean mainClosed = false;
        boolean someClosed = false;
        List<Project> oldprjs = new ArrayList<Project>();
        List<Project> newprjs = new ArrayList<Project>();
        List<Project> notifyList = new ArrayList<Project>();
        synchronized ( this ) {
            oldprjs.addAll(openProjects);
            for( int i = 0; i < projects.length; i++ ) {
                Iterator<Project> it = openProjects.iterator();
                boolean found = false;
                while (it.hasNext()) {
                    if (it.next().equals(projects[i])) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue; // Nothing to remove
                }
                if ( !mainClosed ) {
                    mainClosed = isMainProject( projects[i] );
                }
                // remove the project from openProjects
                it.remove();
                removeModuleInfo(projects[i]);
                
                projects[i].getProjectDirectory().removeFileChangeListener(deleteListener);
                
                recentProjects.add( projects[i] );
                notifyList.add(projects[i]);
                
                someClosed = true;
            }
            if ( someClosed ) {
                newprjs.addAll(openProjects);
                saveProjectList(openProjects);
            }
            if ( mainClosed ) {
                this.mainProject = null;
                saveMainProject( mainProject );
            }
            if ( someClosed ) {
                recentProjects.save();
            }
        }
        //#125750 not necessary to call notifyClosed() under synchronized lock.
        for (Project closed : notifyList) {
            notifyClosed( closed );
        }
        logProjects("close(): openProjects == ", openProjects.toArray(new Project[0])); // NOI18N
        if ( someClosed ) {
            pchSupport.firePropertyChange( PROPERTY_OPEN_PROJECTS, 
                            oldprjs.toArray(new Project[oldprjs.size()]), newprjs.toArray(new Project[newprjs.size()]) );
        }
        if ( mainClosed ) {
            pchSupport.firePropertyChange( PROPERTY_MAIN_PROJECT, null, null );
        }
        if ( someClosed ) {
            pchSupport.firePropertyChange( PROPERTY_RECENT_PROJECTS, null, null );
        }
        // Noticed in #72006: save them, in case e.g. editor stored bookmarks when receiving PROPERTY_OPEN_PROJECTS.
        for (int i = 0; i < projects.length; i++) {
            if (projects[i] instanceof LazyProject) {
                //#147819 we need to ignore lazyProjects when saving, oh well.
                continue;
            }
            try {
                ProjectManager.getDefault().saveProject(projects[i]);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        LogRecord[] removedRec = createRecord("UI_CLOSED_PROJECTS", projects); // NOI18N
        log(removedRec,"org.netbeans.ui.projects");
        removedRec = createRecordMetrics("USG_PROJECT_CLOSE", projects); // NOI18N
        log(removedRec,"org.netbeans.ui.metrics.projects");
        } finally {
            LOAD.exit();
    }
    }
        
    public synchronized Project[] getOpenProjects() {
        Project projects[] = new Project[ openProjects.size() ];
        openProjects.toArray( projects );
        return projects;
    }
    
    public synchronized boolean isOpen( Project p ) {
        // XXX shouldn't this just use openProjects.contains(p)?
        for( Iterator it = openProjects.iterator(); it.hasNext(); ) {
            Project cp = (Project)it.next();
            if ( p.getProjectDirectory().equals( cp.getProjectDirectory() ) ) { 
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isMainProject( Project p ) {

        if ( mainProject != null && p != null &&
             mainProject.getProjectDirectory().equals( p.getProjectDirectory() ) ) {
            return true;
        }
        else {
            return false;
        }
        
    }
    
    public synchronized Project getMainProject() {
        return mainProject;
    }
    
    public void setMainProject( Project mainProject ) {
        LOGGER.finer("Setting main project: " + mainProject); // NOI18N
        logProjects("setMainProject(): openProjects == ", openProjects.toArray(new Project[0])); // NOI18N
        synchronized ( this ) {
            if (mainProject != null && !openProjects.contains(mainProject)) {
                //#139965 the project passed in here can be different from the current one.
                // eg when the ManProjectAction shows a list of opened projects, it lists the "non-loaded skeletons"
                // but when the user eventually selects one, the openProjects list already might hold the 
                // correct loaded list.
                try {
                    mainProject = ProjectManager.getDefault().findProject(mainProject.getProjectDirectory());
                    if (mainProject != null) {
                        boolean fail = true;
                        for (Project p : openProjects) {
                            if (p.equals(mainProject)) {
                                fail = false;
                                break;
                            }
                            if (p instanceof LazyProject) {
                                if (p.getProjectDirectory().equals(mainProject.getProjectDirectory())) {
                                    mainProject = p;
                                    fail = false;
                                    break;
                                }
                            }
                        }
                        if (fail) {
                            logProjects("setMainProject(): openProjects == ", openProjects.toArray(new Project[0])); // NOI18N
                            throw new IllegalArgumentException("NB_REPORTER_IGNORE: Project " + ProjectUtils.getInformation(mainProject).getDisplayName() + " is not open and cannot be set as main.");
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        
            this.mainProject = mainProject;
            saveMainProject( mainProject );
        }
        pchSupport.firePropertyChange( PROPERTY_MAIN_PROJECT, null, null );
    }
    
    public List<Project> getRecentProjects() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<List<Project>>() {
            public List<Project> run() {
                synchronized (OpenProjectList.class) {
                    return recentProjects.getProjects();
                }
            }
        });
    }
    
    public boolean isRecentProjectsEmpty() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Boolean>() {
            public Boolean run() {
                synchronized (OpenProjectList.class) {
                    return recentProjects.isEmpty();
                }
            }
        });         
    }
    
    public List<UnloadedProjectInformation> getRecentProjectsInformation() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<List<UnloadedProjectInformation>>() {
            public List<UnloadedProjectInformation> run() {
                synchronized (OpenProjectList.class) {
                    return recentProjects.getRecentProjectsInfo();
                }
            }
        });
    }
    
    /** As this class is singletnon, which is not GCed it is good idea to 
     *add WeakListeners or remove the listeners properly.
     */
    
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        pchSupport.addPropertyChangeListener( l );        
    }
    
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        pchSupport.removePropertyChangeListener( l );        
    }

               
    // Used from NewFile action        
    public List<DataObject> getTemplatesLRU( Project project,  PrivilegedTemplates priv ) {
        List<FileObject> pLRU = getTemplateNamesLRU( project,  priv );
        List<DataObject> templates = new ArrayList<DataObject>();
        for( Iterator<FileObject> it = pLRU.iterator(); it.hasNext(); ) {
            FileObject fo = it.next();
            if ( fo != null ) {
                try {
                    DataObject dobj = DataObject.find( fo );                    
                    templates.add( dobj );
                }
                catch ( DataObjectNotFoundException e ) {
                    it.remove();
                    org.openide.ErrorManager.getDefault().notify( org.openide.ErrorManager.INFORMATIONAL, e );
                }
            }
            else {
                it.remove();
            }
        }
        
        return templates;
    }
        
    
    // Used from NewFile action    
    public synchronized void updateTemplatesLRU( FileObject template ) {
        
        String templateName = template.getPath();
        
        if ( getRecentTemplates().contains( templateName ) ) {
            getRecentTemplates().remove( templateName );
        }
        getRecentTemplates().add( 0, templateName );
        
        if ( getRecentTemplates().size() > 100 ) {
            getRecentTemplates().remove( 100 );
        }
        
        OpenProjectListSettings.getInstance().setRecentTemplates( new ArrayList<String>( getRecentTemplates() )  );
    }
    
    
    // Package private methods -------------------------------------------------

    // Used from ProjectUiModule
    static void shutdown() {
        if (INSTANCE != null) {
            Iterator it = INSTANCE.openProjects.iterator();
            while (it.hasNext()) {
                Project p = (Project)it.next();
                notifyClosed(p);
            }
        }
    }
        
    // Used from OpenProjectAction
    public static Project fileToProject( File projectDir ) {
        
        try {
            
            FileObject fo = FileUtil.toFileObject(projectDir);
            if (fo != null && /* #60518 */ fo.isFolder()) {
                return ProjectManager.getDefault().findProject(fo);
            } else {
                return null;
            }
                        
        }
        catch ( IOException e ) {
            /* Ignore; will be reported e.g. by ProjectChooserAccessory:
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
             */
            return null;
        }
        
    }
    
    
    
    // Private methods ---------------------------------------------------------
    
    private static LinkedList<Project> URLs2Projects( Collection<URL> URLs ) {
        LinkedList<Project> result = new LinkedList<Project>();
            
        for(URL url: URLs) {
            FileObject dir = URLMapper.findFileObject( url );
            if ( dir != null && dir.isFolder() ) {
                try {
                    Project p = ProjectManager.getDefault().findProject( dir );
                    if ( p != null ) {
                        result.add( p );
                    }
                }       
                catch ( Throwable t ) {
                    //something bad happened during loading the project.
                    //log the problem, but allow the other projects to be load
                    //see issue #65900
                    if (t instanceof ThreadDeath) {
                        throw (ThreadDeath) t;
                    }
                    
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
                }
            }
        }
        
        return result;
    }
    
    private static List<URL> projects2URLs( Collection<Project> projects ) {
        ArrayList<URL> URLs = new ArrayList<URL>( projects.size() );
        for(Project p: projects) {
            try {
                URL root = p.getProjectDirectory().getURL();
                if ( root != null ) {
                    URLs.add( root );
                }
            }
            catch( FileStateInvalidException e ) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }        
        
        return URLs;
    }
    
    
    private static boolean notifyOpened(Project p) {
        boolean ok = true;
        for (Iterator i = p.getLookup().lookupAll(ProjectOpenedHook.class).iterator(); i.hasNext(); ) {
            ProjectOpenedHook hook = (ProjectOpenedHook) i.next();
            
            try {
                ProjectOpenedTrampoline.DEFAULT.projectOpened(hook);
            } catch (RuntimeException e) {
                log(Level.WARNING, null, e);
                // Do not try to call its close hook if its open hook already failed:
                INSTANCE.openProjects.remove(p);
                INSTANCE.removeModuleInfo(p);
                ok = false;
            } catch (Error e) {
                log(Level.WARNING, null, e);
                INSTANCE.openProjects.remove(p);
                INSTANCE.removeModuleInfo(p);
                ok = false;
            }
        }
        return ok;
    }
    
    private static void notifyClosed(Project p) {
        for (Iterator i = p.getLookup().lookupAll(ProjectOpenedHook.class).iterator(); i.hasNext(); ) {
            ProjectOpenedHook hook = (ProjectOpenedHook) i.next();
            
            try {
                ProjectOpenedTrampoline.DEFAULT.projectClosed(hook);
            } catch (RuntimeException e) {
                log(Level.WARNING, null, e);
            } catch (Error e) {
                log(Level.WARNING, null, e);
            }
        }
    }
    
    private boolean doOpenProject(final Project p) {
        boolean recentProjectsChanged;
        LOGGER.finer("doOpenProject(): opening project " + p.toString());
        synchronized (this) {
            log(Level.FINER, "already opened: {0} ", openProjects);
            for (Project existing : openProjects) {
                if (p.equals(existing) || existing.equals(p)) {
                    return false;
                }
            }
            openProjects.add(p);
            addModuleInfo(p);
            
            p.getProjectDirectory().addFileChangeListener(deleteListener);
            p.getProjectDirectory().addFileChangeListener(nbprojectDeleteListener);
            
            recentProjectsChanged = recentProjects.remove(p);
        }
        logProjects("doOpenProject(): openProjects == ", openProjects.toArray(new Project[0])); // NOI18N
        // Notify projects opened
        notifyOpened(p);
        
        Mutex.EVENT.readAccess(new Action<Void>() {
            public Void run() {
                // Open project files
                ProjectUtilities.openProjectFiles(p);
                
                return null;
            }
        });
        
        return recentProjectsChanged;
    }
    
    private static List<Project> loadProjectList() {               
        List<URL> URLs = OpenProjectListSettings.getInstance().getOpenProjectsURLs();
        List<String> names = OpenProjectListSettings.getInstance().getOpenProjectsDisplayNames();
        List<ExtIcon> icons = OpenProjectListSettings.getInstance().getOpenProjectsIcons();
        List<Project> projects = new ArrayList<Project>();
        
        Iterator<URL> urlIt = URLs.iterator();
        Iterator<String> namesIt = names.iterator();
        Iterator<ExtIcon> iconIt = icons.iterator();
        
        while(urlIt.hasNext() && namesIt.hasNext() && iconIt.hasNext()) {
            projects.add(new LazyProject(urlIt.next(), namesIt.next(), iconIt.next()));
        }
        
        //List<Project> projects = URLs2Projects( URLs );
        
        return projects;
    }
    
  
    private static void saveProjectList( List<Project> projects ) {        
        List<URL> URLs = projects2URLs( projects );
        OpenProjectListSettings.getInstance().setOpenProjectsURLs( URLs );
        List<String> names = new ArrayList<String>();
        List<ExtIcon> icons = new ArrayList<ExtIcon>();
        for (Iterator<Project> it = projects.iterator(); it.hasNext(); ) {
            ProjectInformation prjInfo = ProjectUtils.getInformation(it.next());
            names.add(prjInfo.getDisplayName());
            ExtIcon extIcon = new ExtIcon();
            extIcon.setIcon(prjInfo.getIcon());
            icons.add(extIcon);
        }
        OpenProjectListSettings.getInstance().setOpenProjectsDisplayNames(names);
        OpenProjectListSettings.getInstance().setOpenProjectsIcons(icons);
    }
    
    private static void saveMainProject( Project mainProject ) {        
        try {
            URL mainRoot = mainProject == null ? null : mainProject.getProjectDirectory().getURL(); 
            OpenProjectListSettings.getInstance().setMainProjectURL( mainRoot );
        }
        catch ( FileStateInvalidException e ) {
            OpenProjectListSettings.getInstance().setMainProjectURL( null );
        }
    }
        
    private ArrayList<FileObject> getTemplateNamesLRU( Project project, PrivilegedTemplates priv ) {
        // First take recently used templates and try to find those which
        // are supported by the project.
        
        ArrayList<FileObject> result = new ArrayList<FileObject>(NUM_TEMPLATES);        
        
        RecommendedTemplates rt = project.getLookup().lookup( RecommendedTemplates.class );
        String rtNames[] = rt == null ? new String[0] : rt.getRecommendedTypes();
        PrivilegedTemplates pt = priv != null ? priv : project.getLookup().lookup( PrivilegedTemplates.class );
        String ptNames[] = pt == null ? null : pt.getPrivilegedTemplates();        
        ArrayList<String> privilegedTemplates = new ArrayList<String>( Arrays.asList( pt == null ? new String[0]: ptNames ) );
        
        if (priv == null) {
            // when the privileged templates are part of the active lookup,
            // do not mix them with the recent templates, but use only the privileged ones.
            // eg. on Webservices node, one is not interested in a recent "jsp" file template..
            
            synchronized (this) {
                Iterator<String> it = getRecentTemplates().iterator();
                for( int i = 0; i < NUM_TEMPLATES && it.hasNext(); i++ ) {
                    String templateName = it.next();
                    FileObject fo = FileUtil.getConfigFile( templateName );
                    if ( fo == null ) {
                        it.remove(); // Does not exists remove
                    }
                    else if ( isRecommended( project, fo ) ) {
                        result.add( fo );
                        privilegedTemplates.remove( templateName ); // Not to have it twice
                    }
                    else {
                        continue;
                    }
                }
            }
        }
        
        // If necessary fill the list with the rest of privileged templates
        Iterator<String> it = privilegedTemplates.iterator();
        for( int i = result.size(); i < NUM_TEMPLATES && it.hasNext(); i++ ) {
            String path = it.next();
            FileObject fo = FileUtil.getConfigFile( path );
            if ( fo != null ) {
                result.add( fo );
            }
        }
                
        return result;
               
    }
    
    static boolean isRecommended (Project p, FileObject primaryFile) {
        if (getRecommendedTypes (p) == null || getRecommendedTypes (p).length == 0) {
            // if no recommendedTypes are supported (i.e. freeform) -> disaply all templates
            return true;
        }
        
        Object o = primaryFile.getAttribute ("templateCategory"); // NOI18N
        if (o != null) {
            assert o instanceof String : primaryFile + " attr templateCategory = " + o;
            Iterator categoriesIt = getCategories ((String)o).iterator ();
            boolean ok = false;
            while (categoriesIt.hasNext ()) {
                String category = (String)categoriesIt.next ();
                if (Arrays.asList (getRecommendedTypes (p)).contains (category)) {
                    ok = true;
                    break;
                }
            }
            return ok;
        } else {
            // issue 44871, if attr 'templateCategorized' is not set => all is ok
            // no category set, ok display it
            return true;
        }
    }

    private static String[] getRecommendedTypes (Project project) {
        RecommendedTemplates rt = project.getLookup().lookup(RecommendedTemplates.class);
        return rt == null ? null :rt.getRecommendedTypes();
    }
    
    private static List<String> getCategories (String source) {
        ArrayList<String> categories = new ArrayList<String> ();
        StringTokenizer cattok = new StringTokenizer (source, ","); // NOI18N
        while (cattok.hasMoreTokens ()) {
            categories.add (cattok.nextToken ().trim ());
        }
        return categories;
    }
    
    // Private innerclasses ----------------------------------------------------
    
    /** Maintains recent project list
     */    
    private class RecentProjectList {
       
        private List<ProjectReference> recentProjects;
        private List<UnloadedProjectInformation> recentProjectsInfos;
        
        private int size;
        
        /**
         *@size Max number of the project list.
         */
        public RecentProjectList( int size ) {
            this.size = size;
            recentProjects = new ArrayList<ProjectReference>( size );
            recentProjectsInfos = new ArrayList<UnloadedProjectInformation>(size);
            if (LOGGER.isLoggable(Level.FINE)) {
                log(Level.FINE, "created a RecentProjectList: size=" + size);
            }
        }
        
        public synchronized void add( Project p ) {
            int index = getIndex( p );
            
            if ( index == -1 ) {
                // Project not in list
                if (LOGGER.isLoggable(Level.FINE)) {
                    log(Level.FINE, "add new recent project: " + p);
                }
                if ( recentProjects.size() == size ) {
                    // Need some space for the newly added project
                    recentProjects.remove( size - 1 );
                    recentProjectsInfos.remove(size - 1);
                }
                recentProjects.add( 0, new ProjectReference( p ) );
                try {
                    recentProjectsInfos.add(0, ProjectInfoAccessor.DEFAULT.getProjectInfo(
                        ProjectUtils.getInformation(p).getDisplayName(),
                        ProjectUtils.getInformation(p).getIcon(),
                        p.getProjectDirectory().getURL()));
                } catch(FileStateInvalidException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
            else {
                if (LOGGER.isLoggable(Level.FINE)) {
                    log(Level.FINE, "re-add recent project: " + p);
                }
                // Project is in list => just move it to first place
                recentProjects.remove( index );
                recentProjects.add( 0, new ProjectReference( p ) );
                recentProjectsInfos.remove(index);
                try {
                    recentProjectsInfos.add(0, ProjectInfoAccessor.DEFAULT.getProjectInfo(
                        ProjectUtils.getInformation(p).getDisplayName(),
                        ProjectUtils.getInformation(p).getIcon(),
                        p.getProjectDirectory().getURL()));
                } catch(FileStateInvalidException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
        
        public synchronized boolean remove( Project p ) {
            int index = getIndex( p );
            if ( index != -1 ) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    log(Level.FINE, "remove recent project: " + p);
                }
                recentProjects.remove( index );
                recentProjectsInfos.remove(index);
                return true;
            }
            return false;
        }
        
        public synchronized void refresh() {
            assert recentProjects.size() == recentProjectsInfos.size();
            boolean refresh = false;
            Iterator<ProjectReference> recentProjectsIter = recentProjects.iterator();
            Iterator<UnloadedProjectInformation> recentProjectsInfosIter = recentProjectsInfos.iterator();
            while (recentProjectsIter.hasNext() && recentProjectsInfosIter.hasNext()) {
                ProjectReference prjRef = recentProjectsIter.next();
                recentProjectsInfosIter.next();
                URL url = prjRef.getURL();
                FileObject prjDir = null;
                try {
                    File file = FileUtil.normalizeFile(new File(url.toURI()));
                    prjDir = FileUtil.toFileObject(file);
                } catch (URISyntaxException use) {
                    // invalid projectdir URL saved?
                }
                Project prj = null;
                if (prjDir != null && prjDir.isFolder()) {
                    try {
                        prj = ProjectManager.getDefault().findProject(prjDir);
                    } catch ( IOException ioEx ) {
                        // Ignore invalid folders
                    }
                }
                
                if (prj == null) { // externally deleted project probably
                    refresh = true;
                    if (prjDir != null && prjDir.isFolder()) {
                        prjDir.removeFileChangeListener(nbprojectDeleteListener);
                    }
                    recentProjectsIter.remove();
                    recentProjectsInfosIter.remove();
                }
            }
            if (refresh) {
                pchSupport.firePropertyChange(PROPERTY_RECENT_PROJECTS, null, null);
                save();
            }
        }
        
        public List<Project> getProjects() {
            List<Project> result = new ArrayList<Project>( recentProjects.size() );
            // Copy the list
            List<ProjectReference> references = new ArrayList<ProjectReference>( recentProjects );
            for ( Iterator<ProjectReference> it = references.iterator(); it.hasNext(); ) {
                ProjectReference pRef = it.next(); 
                Project p = pRef.getProject();
                if ( p == null || !p.getProjectDirectory().isValid() ) {
                    remove( p );        // Folder does not exist any more => remove from
                    if (LOGGER.isLoggable(Level.FINE)) {
                        log(Level.FINE, "removing dead recent project: " + p);
                    }
                }
                else {
                    result.add( p );
                }
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                log(Level.FINE, "recent projects: " + result);
            }
            return result;
        }
        
        public boolean isEmpty() {
            boolean empty = recentProjects.isEmpty();
            if (LOGGER.isLoggable(Level.FINE)) {
                log(Level.FINE, "recent projects empty? " + empty);
            }
            return empty;
        }
        
        public synchronized void load() {
            List<URL> URLs = OpenProjectListSettings.getInstance().getRecentProjectsURLs();
            List<String> names = OpenProjectListSettings.getInstance().getRecentProjectsDisplayNames();
            List<ExtIcon> icons = OpenProjectListSettings.getInstance().getRecentProjectsIcons();
            if (LOGGER.isLoggable(Level.FINE)) {
                log(Level.FINE, "recent project list load: " + URLs);
            }
            recentProjects.clear();
            for ( Iterator it = URLs.iterator(); it.hasNext(); ) {
                recentProjects.add( new ProjectReference( (URL)it.next() ) );
            }
            recentProjectsInfos.clear();
            for (Iterator iterNames = names.iterator(), iterURLs = URLs.iterator(), iterIcons = icons.iterator(); 
                    (iterNames.hasNext() && iterURLs.hasNext() && iterIcons.hasNext()); ) {
                String name = (String) iterNames.next();
                URL url = (URL) iterURLs.next();
                Icon icon = ((ExtIcon) iterIcons.next()).getIcon();
                recentProjectsInfos.add(ProjectInfoAccessor.DEFAULT.getProjectInfo(name, icon, url));
            }
            // if following is true then there was either some problem with serialization
            // or user started new IDE on userdir with only partial information saved - only URLs
            // then both list should be cleared - recent project information will be lost
            if (recentProjects.size() != recentProjectsInfos.size()) {
                recentProjects.clear();
                recentProjectsInfos.clear();
            }
            // register project delete listener to all open projects
            synchronized (this) {
                for (Project p : openProjects) {
                    assert p != null : "There is null in " + openProjects;
                    assert p.getProjectDirectory() != null : "Project " + p + " has null project directory";
                    p.getProjectDirectory().addFileChangeListener(nbprojectDeleteListener);
                }
            }
        }
        
        public void save() {
            List<URL> URLs = new ArrayList<URL>( recentProjects.size() );
            for (ProjectReference pRef: recentProjects) {
                URL pURL = pRef.getURL();
                if ( pURL != null ) {
                    URLs.add( pURL );
                }
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                log(Level.FINE, "recent project list save: " + URLs);
            }
            OpenProjectListSettings.getInstance().setRecentProjectsURLs( URLs );
            int listSize = recentProjectsInfos.size();
            List<String> names = new ArrayList<String>(listSize);
            List<ExtIcon> icons = new ArrayList<ExtIcon>(listSize);
            for (Iterator it = recentProjectsInfos.iterator(); it.hasNext(); ) {
                UnloadedProjectInformation prjInfo = (UnloadedProjectInformation) it.next();
                names.add(prjInfo.getDisplayName());
                ExtIcon extIcon = new ExtIcon();
                extIcon.setIcon(prjInfo.getIcon());
                icons.add(extIcon);
            }
            OpenProjectListSettings.getInstance().setRecentProjectsDisplayNames(names);
            OpenProjectListSettings.getInstance().setRecentProjectsIcons(icons);
        }
        
        private int getIndex( Project p ) {
            
            URL pURL;
            try {
                if ( p == null || p.getProjectDirectory() == null ) {
                    return -1;
                }
                pURL = p.getProjectDirectory().getURL();                
            }
            catch( FileStateInvalidException e ) {
                return -1;
            }
            
            int i = 0;
            
            for( Iterator it = recentProjects.iterator(); it.hasNext(); i++) {
                URL p2URL = ((ProjectReference)it.next()).getURL();
                if ( pURL.equals( p2URL ) ) {
                    return i;
                }
            }
            
            return -1;
        }
        
        private List<UnloadedProjectInformation> getRecentProjectsInfo() {
            refresh();
            return recentProjectsInfos;
        }
        
        private class ProjectReference {
            
            private WeakReference<Project> projectReference;
            private URL projectURL;
            
            public ProjectReference( URL url ) {                
                this.projectURL = url;
            }
            
            public ProjectReference( Project p ) {
                this.projectReference = new WeakReference<Project>( p );
                try {
                    projectURL = p.getProjectDirectory().getURL();                
                }
                catch( FileStateInvalidException e ) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        log(Level.FINE, "FSIE getting URL for project: " + p.getProjectDirectory());
                    }
                }
            }
            
            public Project getProject() {
                
                Project p = null; 
                
                if ( projectReference != null ) { // Reference to project exists
                    p = projectReference.get();
                    if ( p != null ) {
                        // And refers to some project, check for validity:
                        if ( ProjectManager.getDefault().isValid( p ) )
                            return p; 
                        else
                            return null;
                    }
                }
                
                if (LOGGER.isLoggable(Level.FINE)) {
                    log(Level.FINE, "no active project reference for " + projectURL);
                }
                if ( projectURL != null ) {                    
                    FileObject dir = URLMapper.findFileObject( projectURL );
                    if ( dir != null && dir.isFolder() ) {
                        try {
                            p = ProjectManager.getDefault().findProject( dir );
                            if ( p != null ) {
                                projectReference = new WeakReference<Project>( p ); 
                                if (LOGGER.isLoggable(Level.FINE)) {
                                    log(Level.FINE, "found " + p);
                                }
                                return p;
                            }
                        }       
                        catch ( IOException e ) {
                            // Ignore invalid folders
                            if (LOGGER.isLoggable(Level.FINE)) {
                                log(Level.FINE, "could not load recent project from " + projectURL);
                            }
                        }
                    }
                }
                
                if (LOGGER.isLoggable(Level.FINE)) {
                    log(Level.FINE, "no recent project in " + projectURL);
                }
                return null; // Empty reference                
            }
            
            public URL getURL() {
                return projectURL;
            }
            
        }
        
    }
    
    public static class ProjectByDisplayNameComparator implements Comparator<Project> {
        
	private static Comparator<Object> COLLATOR = Collator.getInstance();
        
        public int compare(Project p1, Project p2) {
//            Uncoment to make the main project be the first one
//            but then needs to listen to main project change
//            if ( OpenProjectList.getDefault().isMainProject( p1 ) ) {
//                return -1;
//            }
//            
//            if ( OpenProjectList.getDefault().isMainProject( p2 ) ) {
//                return 1;
//            }
            
            String n1 = ProjectUtils.getInformation(p1).getDisplayName();
            String n2 = ProjectUtils.getInformation(p2).getDisplayName();
            if (n1 != null && n2 != null) {
                return COLLATOR.compare(n1, n2);
            } else if (n1 == null && n2 != null) {
                log(Level.WARNING, p1 + ": ProjectInformation.getDisplayName() should not return null!");
                return -1;
            } else if (n1 != null && n2 == null) {
                log(Level.WARNING, p2 + ": ProjectInformation.getDisplayName() should not return null!");
                return 1;
            }
            return 0; // both null
            
        }
        
    }
    
    private final class NbProjectDeletionListener extends FileChangeAdapter {
        
        public NbProjectDeletionListener() {}
        
        @Override
        public void fileDeleted(FileEvent fe) {
            recentProjects.refresh();
        }
        
    }
    
    /**
     * Closesdeleted projects.
     */
    private final class ProjectDeletionListener extends FileChangeAdapter {
        
        public ProjectDeletionListener() {}

        public @Override void fileDeleted(FileEvent fe) {
            synchronized (OpenProjectList.this) {
                Project toRemove = null;
                for (Project prj : openProjects) {
                    if (fe.getFile().equals(prj.getProjectDirectory())) {
                        toRemove = prj;
                        break;
                    }
                }
                final Project fRemove = toRemove;
                if (fRemove != null) {
                    //#108376 avoid deadlock in org.netbeans.modules.project.ui.ProjectUtilities$1.close(ProjectUtilities.java:106)
                    // alternatively removing the close() metod from synchronized block could help as well..
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run () {
                            close(new Project[] {fRemove}, false);
                        }
                    });
                }
            }
        }
        
    }
    
    
    private static ModuleInfo findModuleForProject(Project prj) {
        Collection<? extends ModuleInfo> instances = Lookup.getDefault().lookupAll(ModuleInfo.class);
        ModuleInfo info = null;
        for (ModuleInfo cur : instances) {
            if (!cur.isEnabled()) {
                continue;
            }
            if (cur.getClassLoader() == prj.getClass().getClassLoader()) {
                info = cur;
                break;
            }
        }
        return info;
    }
    
    private void addModuleInfo(Project prj) {
        ModuleInfo info = findModuleForProject(prj);
        if (info != null) {
            // is null in tests..
            synchronized (openProjectsModuleInfos) {
                if (!openProjectsModuleInfos.containsKey(info)) {
                    openProjectsModuleInfos.put(info, new ArrayList<Project>());
                    info.addPropertyChangeListener(infoListener);
                }
                openProjectsModuleInfos.get(info).add(prj);
            }
        }
    }
    
    private void removeModuleInfo(Project prj) {
        ModuleInfo info = findModuleForProject(prj);
        removeModuleInfo(prj, info);
    }
    
    private void removeModuleInfo(Project prj, ModuleInfo info) {
        // info can be null in case we are closing a project from disabled module
        if (info != null) {
            synchronized (openProjectsModuleInfos) {
                List<Project> prjlist = openProjectsModuleInfos.get(info);
                if (prjlist != null) {
                    prjlist.remove(prj);
                    if (prjlist.size() == 0) {
                        info.removePropertyChangeListener(infoListener);
                        openProjectsModuleInfos.remove(info);
                    }
                }
            }
        }
    }

    private void checkModuleInfo(ModuleInfo info) {
        if (info.isEnabled())  {
            return;
        }
        Collection<Project> toRemove = new ArrayList<Project>(openProjectsModuleInfos.get(info));
        if (toRemove != null && toRemove.size() > 0) {
            for (Project prj : toRemove) {
                removeModuleInfo(prj, info);
            }
            close(toRemove.toArray(new Project[toRemove.size()]), false);
        }
    }
    
    private static LogRecord[] createRecord(String msg, Project[] projects) {
        if (projects.length == 0) {
            return null;
        }
        
        Map<String,int[]> counts = new HashMap<String,int[]>();
        for (Project p : projects) {
            String n = p.getClass().getName();
            int[] cnt = counts.get(n);
            if (cnt == null) {
                cnt = new int[1];
                counts.put(n, cnt);
            }
            cnt[0]++;
        }
        
        Logger logger = Logger.getLogger("org.netbeans.ui.projects"); // NOI18N
        LogRecord[] arr = new LogRecord[counts.size()];
        int i = 0;
        for (Map.Entry<String,int[]> entry : counts.entrySet()) {
            LogRecord rec = new LogRecord(Level.CONFIG, msg);
            rec.setParameters(new Object[] { entry.getKey(), afterLastDot(entry.getKey()), entry.getValue()[0] });
            rec.setLoggerName(logger.getName());
            rec.setResourceBundle(NbBundle.getBundle(OpenProjectList.class));
            rec.setResourceBundleName(OpenProjectList.class.getPackage().getName()+".Bundle");
            
            arr[i++] = rec;
        }
        
        return arr;
    }

   private static LogRecord[] createRecordMetrics (String msg, Project[] projects) {
        if (projects.length == 0) {
            return null;
        }

        Logger logger = Logger.getLogger("org.netbeans.ui.metrics.projects"); // NOI18N

        LogRecord[] arr = new LogRecord[projects.length];
        int i = 0;
        for (Project p : projects) {
            LogRecord rec = new LogRecord(Level.INFO, msg);
            rec.setParameters(new Object[] { p.getClass().getName() });
            rec.setLoggerName(logger.getName());

            arr[i++] = rec;
        }

        return arr;
    }
    
    private static void log(LogRecord[] arr, String loggerName) {
        if (arr == null) {
            return;
        }
        Logger logger = Logger.getLogger(loggerName); // NOI18N
        for (LogRecord r : arr) {
            logger.log(r);
        }
    }
    
    private static String afterLastDot(String s) {
        int index = s.lastIndexOf('.');
        if (index == -1) {
            return s;
        }
        return s.substring(index + 1);
    }
    
    private static void logProjects(String message, Project[] projects) {
        if (projects.length == 0) {
            return;
        }
        for (Project p : projects) {
            LOGGER.finer(message + p.toString());
        }
    }
    
}
