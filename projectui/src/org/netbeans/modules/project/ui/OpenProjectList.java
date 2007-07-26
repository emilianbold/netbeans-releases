/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
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
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
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
    
    private static OpenProjectList INSTANCE;
    
    // number of templates in LRU list
    private static final int NUM_TEMPLATES = 15;
    
    private static final Logger LOGGER = Logger.getLogger(OpenProjectList.class.getName());
    private static final Level LOG_LEVEL = Level.FINE;
    
    private static final RequestProcessor OPENING_RP = new RequestProcessor("Opening projects", 1);
    
    /** List which holds the open projects */
    private List<Project> openProjects;
    private HashMap<ModuleInfo, List<Project>> openProjectsModuleInfos;
    
    /** Main project */
    private Project mainProject;
    
    /** List of recently closed projects */
    private final RecentProjectList recentProjects;

    /** LRU List of recently used templates */
    private List<String> recentTemplates;
    
    /** Property change listeners */
    private final PropertyChangeSupport pchSupport;
    
    private ProjectDeletionListener deleteListener = new ProjectDeletionListener();
    
    private PropertyChangeListener infoListener;
    
    OpenProjectList() {
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
    }
    
           
    // Implementation of the class ---------------------------------------------
    
    public static OpenProjectList getDefault() {
        boolean needNotify = false;
        
        Project[] inital = null;
        synchronized ( OpenProjectList.class ) {
            if ( INSTANCE == null ) {
                needNotify = true;
                INSTANCE = new OpenProjectList();
                INSTANCE.openProjects = loadProjectList();                
                inital = INSTANCE.openProjects.toArray(new Project[0]);
                INSTANCE.recentTemplates = new ArrayList<String>( OpenProjectListSettings.getInstance().getRecentTemplates() );
                URL mainProjectURL = OpenProjectListSettings.getInstance().getMainProjectURL();
                // Load recent project list
                INSTANCE.recentProjects.load();
                for( Iterator it = INSTANCE.openProjects.iterator(); it.hasNext(); ) {
                    Project p = (Project)it.next();
                    INSTANCE.addModuleInfo(p);
                    // Set main project
                    try {
                        if ( mainProjectURL != null && 
                             mainProjectURL.equals( p.getProjectDirectory().getURL() ) ) {
                            INSTANCE.mainProject = p;
                        }
                    }
                    catch( FileStateInvalidException e ) {
                        // Not a main project
                    }
                }          
            }
        }
        if ( needNotify ) {
            //#68738: a project may open other projects in its ProjectOpenedHook:
            for(Project p: new ArrayList<Project>(INSTANCE.openProjects)) {
                notifyOpened(p);             
            }
            
        }
        if (inital != null) {
            log(createRecord("UI_INIT_PROJECTS", inital));
        }
        
        return INSTANCE;
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
    
    public void open(final Project[] projects, final boolean openSubprojects, final boolean asynchronously ) {
        if (projects.length == 0) {
            //nothing to do:
            return ;
        }
        
        long start = System.currentTimeMillis();
        
	if (asynchronously) {
            if (!EventQueue.isDispatchThread()) { // #89935
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        open(projects, openSubprojects, asynchronously);
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
		    } finally {
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
                                //fix for #67114:
                                try {
                                    Thread.currentThread().sleep(50);
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
	}
        
        long end = System.currentTimeMillis();
        
        if (LOGGER.isLoggable(LOG_LEVEL)) {
            LOGGER.log(LOG_LEVEL, "opening projects took: " + (end - start) + "ms");
        }
    }
    
    private void doOpen(Project[] projects, boolean openSubprojects, ProgressHandle handle, OpeningProjectPanel panel) {
        assert !Arrays.asList(projects).contains(null) : "Projects can't be null";
            
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
        log(addedRec);
        
        
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
    }
       
    public void close( Project projects[], boolean notifyUI ) {
        if (!ProjectUtilities.closeAllDocuments (projects, notifyUI )) {
            return;
        }
        logProjects("close(): closing project: ", projects);
        boolean mainClosed = false;
        boolean someClosed = false;
        List<Project> oldprjs = new ArrayList<Project>();
        List<Project> newprjs = new ArrayList<Project>();
        synchronized ( this ) {
            oldprjs.addAll(openProjects);
            for( int i = 0; i < projects.length; i++ ) {
                if ( !openProjects.contains( projects[i] ) ) {
                    continue; // Nothing to remove
                }
                if ( !mainClosed ) {
                    mainClosed = isMainProject( projects[i] );
                }
                openProjects.remove( projects[i] );
                removeModuleInfo(projects[i]);
                
                projects[i].getProjectDirectory().removeFileChangeListener(deleteListener);
                
                recentProjects.add( projects[i] );
                notifyClosed( projects[i] );
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
            try {
                ProjectManager.getDefault().saveProject(projects[i]);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        LogRecord[] removedRec = createRecord("UI_CLOSED_PROJECTS", projects); // NOI18N
        log(removedRec);
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
                logProjects("setMainProject(): openProjects == ", openProjects.toArray(new Project[0])); // NOI18N
                throw new IllegalArgumentException("Project " + ProjectUtils.getInformation(mainProject).getDisplayName() + " is not open and cannot be set as main.");
            }
        
            this.mainProject = mainProject;
            saveMainProject( mainProject );
        }
        pchSupport.firePropertyChange( PROPERTY_MAIN_PROJECT, null, null );
    }
    
    public synchronized List<Project> getRecentProjects() {
        return recentProjects.getProjects();
    }
    
    public synchronized boolean isRecentProjectsEmpty() {
        return recentProjects.isEmpty();
    }
    
    public synchronized List<UnloadedProjectInformation> getRecentProjectsInformation() {
        return recentProjects.getRecentProjectsInfo();
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
    public List<DataObject> getTemplatesLRU( Project project ) {
        List<FileObject> pLRU = getTemplateNamesLRU( project );
        List<DataObject> templates = new ArrayList<DataObject>();
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
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
    public void updateTemplatesLRU( FileObject template ) {
        
        String templateName = template.getPath();
        
        if ( recentTemplates.contains( templateName ) ) {
            recentTemplates.remove( templateName );
        }
        recentTemplates.add( 0, templateName );
        
        if ( recentTemplates.size() > 100 ) {
            recentTemplates.remove( 100 );
        }
        
        OpenProjectListSettings.getInstance().setRecentTemplates( new ArrayList<String>( recentTemplates ) );
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
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
        
    }
    
    
    
    // Private methods ---------------------------------------------------------
    
    private static List<Project> URLs2Projects( Collection<URL> URLs ) {
        ArrayList<Project> result = new ArrayList<Project>( URLs.size() );
            
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
    
    
    private static void notifyOpened(Project p) {
        for (Iterator i = p.getLookup().lookupAll(ProjectOpenedHook.class).iterator(); i.hasNext(); ) {
            ProjectOpenedHook hook = (ProjectOpenedHook) i.next();
            
            try {
                ProjectOpenedTrampoline.DEFAULT.projectOpened(hook);
            } catch (RuntimeException e) {
                ErrorManager.getDefault().notify(e);
                // Do not try to call its close hook if its open hook already failed:
                INSTANCE.openProjects.remove(p);
                INSTANCE.removeModuleInfo(p);
            } catch (Error e) {
                ErrorManager.getDefault().notify(e);
                INSTANCE.openProjects.remove(p);
                INSTANCE.removeModuleInfo(p);
            }
        }
    }
    
    private static void notifyClosed(Project p) {
        for (Iterator i = p.getLookup().lookupAll(ProjectOpenedHook.class).iterator(); i.hasNext(); ) {
            ProjectOpenedHook hook = (ProjectOpenedHook) i.next();
            
            try {
                ProjectOpenedTrampoline.DEFAULT.projectClosed(hook);
            } catch (RuntimeException e) {
                ErrorManager.getDefault().notify(e);
            } catch (Error e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
    
    private boolean doOpenProject(final Project p) {
        boolean recentProjectsChanged;
        LOGGER.finer("doOpenProject(): opening project " + p.toString());
        synchronized (this) {
            if (openProjects.contains(p)) {
                return false;
            }
            openProjects.add(p);
            addModuleInfo(p);
            
            p.getProjectDirectory().addFileChangeListener(deleteListener);
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
        List<Project> projects = URLs2Projects( URLs );
        
        return projects;
    }
    
  
    private static void saveProjectList( List<Project> projects ) {        
        List<URL> URLs = projects2URLs( projects );
        OpenProjectListSettings.getInstance().setOpenProjectsURLs( URLs );
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
        
    private ArrayList<FileObject> getTemplateNamesLRU( Project project ) {
        // First take recently used templates and try to find those which
        // are supported by the project.
        
        ArrayList<FileObject> result = new ArrayList<FileObject>(NUM_TEMPLATES);        
        
        RecommendedTemplates rt = project.getLookup().lookup( RecommendedTemplates.class );
        String rtNames[] = rt == null ? new String[0] : rt.getRecommendedTypes();
        PrivilegedTemplates pt = project.getLookup().lookup( PrivilegedTemplates.class );
        String ptNames[] = pt == null ? null : pt.getPrivilegedTemplates();
        ArrayList<String> privilegedTemplates = new ArrayList<String>( Arrays.asList( pt == null ? new String[0]: ptNames ) );
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();            
                
        Iterator<String> it = recentTemplates.iterator();
        for( int i = 0; i < NUM_TEMPLATES && it.hasNext(); i++ ) {
            String templateName = it.next();
            FileObject fo = sfs.findResource( templateName );
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
        
        // If necessary fill the list with the rest of privileged templates
        it = privilegedTemplates.iterator();
        for( int i = result.size(); i < NUM_TEMPLATES && it.hasNext(); i++ ) {
            String path = it.next();
            FileObject fo = sfs.findResource( path );
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
            // issue 43958, if attr 'templateCategorized' is not set => all is ok
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
    private static class RecentProjectList {
       
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
            if (LOGGER.isLoggable(LOG_LEVEL)) {
                LOGGER.log(LOG_LEVEL, "created a RecentProjectList: size=" + size);
            }
        }
        
        public void add( Project p ) {
            int index = getIndex( p );
            
            if ( index == -1 ) {
                // Project not in list
                if (LOGGER.isLoggable(LOG_LEVEL)) {
                    LOGGER.log(LOG_LEVEL, "add new recent project: " + p);
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
                if (LOGGER.isLoggable(LOG_LEVEL)) {
                    LOGGER.log(LOG_LEVEL, "re-add recent project: " + p);
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
        
        public boolean remove( Project p ) {
            int index = getIndex( p );
            if ( index != -1 ) {
                if (LOGGER.isLoggable(LOG_LEVEL)) {
                    LOGGER.log(LOG_LEVEL, "remove recent project: " + p);
                }
                recentProjects.remove( index );
                recentProjectsInfos.remove(index);
                return true;
            }
            return false;
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
                    if (LOGGER.isLoggable(LOG_LEVEL)) {
                        LOGGER.log(LOG_LEVEL, "removing dead recent project: " + p);
                    }
                }
                else {
                    result.add( p );
                }
            }
            if (LOGGER.isLoggable(LOG_LEVEL)) {
                LOGGER.log(LOG_LEVEL, "recent projects: " + result);
            }
            return result;
        }
        
        public boolean isEmpty() {
            boolean empty = recentProjects.isEmpty();
            if (LOGGER.isLoggable(LOG_LEVEL)) {
                LOGGER.log(LOG_LEVEL, "recent projects empty? " + empty);
            }
            return empty;
        }
        
        public void load() {
            List<URL> URLs = OpenProjectListSettings.getInstance().getRecentProjectsURLs();
            List<String> names = OpenProjectListSettings.getInstance().getRecentProjectsDisplayNames();
            List<ExtIcon> icons = OpenProjectListSettings.getInstance().getRecentProjectsIcons();
            if (LOGGER.isLoggable(LOG_LEVEL)) {
                LOGGER.log(LOG_LEVEL, "recent project list load: " + URLs);
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
        }
        
        public void save() {
            List<URL> URLs = new ArrayList<URL>( recentProjects.size() );
            for (ProjectReference pRef: recentProjects) {
                URL pURL = pRef.getURL();
                if ( pURL != null ) {
                    URLs.add( pURL );
                }
            }
            if (LOGGER.isLoggable(LOG_LEVEL)) {
                LOGGER.log(LOG_LEVEL, "recent project list save: " + URLs);
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
            return recentProjectsInfos;
        }
        
        private static class ProjectReference {
            
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
                    if (LOGGER.isLoggable(LOG_LEVEL)) {
                        LOGGER.log(LOG_LEVEL, "FSIE getting URL for project: " + p.getProjectDirectory());
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
                
                if (LOGGER.isLoggable(LOG_LEVEL)) {
                    LOGGER.log(LOG_LEVEL, "no active project reference for " + projectURL);
                }
                if ( projectURL != null ) {                    
                    FileObject dir = URLMapper.findFileObject( projectURL );
                    if ( dir != null && dir.isFolder() ) {
                        try {
                            p = ProjectManager.getDefault().findProject( dir );
                            if ( p != null ) {
                                projectReference = new WeakReference<Project>( p ); 
                                if (LOGGER.isLoggable(LOG_LEVEL)) {
                                    LOGGER.log(LOG_LEVEL, "found " + p);
                                }
                                return p;
                            }
                        }       
                        catch ( IOException e ) {
                            // Ignore invalid folders
                            if (LOGGER.isLoggable(LOG_LEVEL)) {
                                LOGGER.log(LOG_LEVEL, "could not load recent project from " + projectURL);
                            }
                        }
                    }
                }
                
                if (LOGGER.isLoggable(LOG_LEVEL)) {
                    LOGGER.log(LOG_LEVEL, "no recent project in " + projectURL);
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
                LOGGER.log(Level.WARNING, p1 + ": ProjectInformation.getDisplayName() should not return null!");
                return -1;
            } else if (n1 != null && n2 == null) {
                LOGGER.log(Level.WARNING, p2 + ": ProjectInformation.getDisplayName() should not return null!");
                return 1;
            }
            return 0; // both null
            
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
            if (!openProjectsModuleInfos.containsKey(info)) {
                openProjectsModuleInfos.put(info, new ArrayList<Project>());
                info.addPropertyChangeListener(infoListener);
            }
            openProjectsModuleInfos.get(info).add(prj);
        }
    }
    
    private void removeModuleInfo(Project prj) {
        ModuleInfo info = findModuleForProject(prj);
        removeModuleInfo(prj, info);
    }
    
    private void removeModuleInfo(Project prj, ModuleInfo info) {
        // info can be null in case we are closing a project from disabled module
        if (info != null) {
            openProjectsModuleInfos.get(info).remove(prj);
            if (openProjectsModuleInfos.get(info).size() == 0) {
                info.removePropertyChangeListener(infoListener);
                openProjectsModuleInfos.remove(info);
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
    
    private static void log(LogRecord[] arr) {
        if (arr == null) {
            return;
        }
        Logger logger = Logger.getLogger("org.netbeans.ui.projects"); // NOI18N
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
