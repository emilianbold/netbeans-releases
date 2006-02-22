/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
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
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
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
    
    public static final Comparator PROJECT_BY_DISPLAYNAME = new ProjectByDisplayNameComparator();
    
    // Property names
    public static final String PROPERTY_OPEN_PROJECTS = "OpenProjects";
    public static final String PROPERTY_MAIN_PROJECT = "MainProject";
    public static final String PROPERTY_RECENT_PROJECTS = "RecentProjects";
    
    private static OpenProjectList INSTANCE;
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(OpenProjectList.class.getName());
    private static final RequestProcessor OPENING_RP = new RequestProcessor("Opening projects", 1);
    
    /** List which holds the open projects */
    private List/*<Project>*/ openProjects;
    
    /** Main project */
    private Project mainProject;
    
    /** List of recently closed projects */
    private final RecentProjectList recentProjects;

    /** LRU List of recently used templates */
    private List /*<String>*/ recentTemplates;
    
    /** Property change listeners */
    private final PropertyChangeSupport pchSupport;
    
    
    OpenProjectList() {
        openProjects = new ArrayList();
        pchSupport = new PropertyChangeSupport( this );
        recentProjects = new RecentProjectList(10); // #47134
    }
    
           
    // Implementation of the class ---------------------------------------------
    
    public static OpenProjectList getDefault() {
        boolean needNotify = false;
        
        synchronized ( OpenProjectList.class ) {
            if ( INSTANCE == null ) {
                needNotify = true;
                INSTANCE = new OpenProjectList();
                INSTANCE.openProjects = loadProjectList();                
                INSTANCE.recentTemplates = new ArrayList( OpenProjectListSettings.getInstance().getRecentTemplates() );
                URL mainProjectURL = OpenProjectListSettings.getInstance().getMainProjectURL();
                // Load recent project list
                INSTANCE.recentProjects.load();
                for( Iterator it = INSTANCE.openProjects.iterator(); it.hasNext(); ) {
                    Project p = (Project)it.next();
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
            for( Iterator it = new ArrayList(INSTANCE.openProjects).iterator(); it.hasNext(); ) {
                Project p = (Project)it.next();
                notifyOpened(p);             
            }
            
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
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "opening projects took: " + (end - start) + "ms");
        }
    }
    
    private void doOpen(Project[] projects, boolean openSubprojects, ProgressHandle handle, OpeningProjectPanel panel) {
        assert !Arrays.asList(projects).contains(null) : "Projects can't be null";
            
        boolean recentProjectsChanged = false;
        int  maxWork = 1000;
        int  workForSubprojects = maxWork / 2;
        double currentWork = 0;
        Collection projectsToOpen = new LinkedHashSet();
        
	if (handle != null) {
	    handle.start(maxWork);
	    handle.progress(0);
	}
        
        if (panel != null) {
            assert projects.length > 0 : "at least one project to open";
            
            panel.setProjectName(ProjectUtils.getInformation(projects[0]).getDisplayName());
        }
        
        Map/*<Project,Set<Project>>*/ subprojectsCache = new HashMap(); // #59098

        List toHandle = new LinkedList(Arrays.asList(projects));
        
        while (!toHandle.isEmpty()) {
            Project p = (Project) toHandle.remove(0);
            Set/*<Project>*/ subprojects = openSubprojects ? (Set) subprojectsCache.get(p) : Collections.EMPTY_SET;
            
            if (subprojects == null) {
                SubprojectProvider spp = (SubprojectProvider) p.getLookup().lookup(SubprojectProvider.class);
                if (spp != null) {
                    subprojects = spp.getSubprojects();
                } else {
                    subprojects = Collections.EMPTY_SET;
                }
                subprojectsCache.put(p, subprojects);
            }
            
            projectsToOpen.add(p);
            
            for (Iterator i = subprojects.iterator(); i.hasNext(); ) {
                Project sub = (Project) i.next();
                
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
        
        for (Iterator i = projectsToOpen.iterator(); i.hasNext(); ) {
            Project p = (Project) i.next();
            
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
            saveProjectList( openProjects );
            if ( recentProjectsChanged ) {
                recentProjects.save();
            }
        }
        
	if (handle != null) {
	    handle.finish();
	}
        
        final boolean recentProjectsChangedCopy = recentProjectsChanged;
        
        Mutex.EVENT.readAccess(new Action() {
            public Object run() {
                pchSupport.firePropertyChange( PROPERTY_OPEN_PROJECTS, null, null );
                if ( recentProjectsChangedCopy ) {
                    pchSupport.firePropertyChange( PROPERTY_RECENT_PROJECTS, null, null );
                }
                
                return null;
            }
        });
    }
       
    public void close( Project projects[] ) {
        boolean mainClosed = false;
        boolean someClosed = false;
        synchronized ( this ) {
            for( int i = 0; i < projects.length; i++ ) {
                if ( !openProjects.contains( projects[i] ) ) {
                    continue; // Nothing to remove
                }
                if ( !mainClosed ) {
                    mainClosed = isMainProject( projects[i] );
                }
                openProjects.remove( projects[i] );
                recentProjects.add( projects[i] );
                notifyClosed( projects[i] );
                someClosed = true;
            }
            if ( someClosed ) {
                saveProjectList( openProjects );
            }
            if ( mainClosed ) {
                this.mainProject = null;
                saveMainProject( mainProject );
            }
            if ( someClosed ) {
                recentProjects.save();
            }
        }
        if ( someClosed ) {
            pchSupport.firePropertyChange( PROPERTY_OPEN_PROJECTS, null, null );
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
        synchronized ( this ) {
            if (!openProjects.contains(mainProject)) {
                throw new IllegalArgumentException("Project " + ProjectUtils.getInformation(mainProject).getDisplayName() + " is not open and cannot be set as main.");
            }
        
            this.mainProject = mainProject;
            saveMainProject( mainProject );
        }
        pchSupport.firePropertyChange( PROPERTY_MAIN_PROJECT, null, null );
    }
    
    public synchronized List getRecentProjects() {
        return recentProjects.getProjects();
    }
    
    public synchronized boolean isRecentProjectsEmpty() {
        return recentProjects.isEmpty();
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
    public List /*<DataObject>*/ getTemplatesLRU( Project project ) {
        List pLRU = getTemplateNamesLRU( project );
        List templates = new ArrayList();
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        for( Iterator it = pLRU.iterator(); it.hasNext(); ) {
            FileObject fo = (FileObject)it.next();
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
        
        OpenProjectListSettings.getInstance().setRecentTemplates( new ArrayList( recentTemplates ) );
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
    
    private static List URLs2Projects( Collection /*<URL>*/ URLs ) {
        ArrayList result = new ArrayList( URLs.size() );
            
        for( Iterator it = URLs.iterator(); it.hasNext(); ) {
            URL url = (URL)it.next();
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
    
    private static List projects2URLs( Collection /*<Project>*/ projects ) {
        ArrayList URLs = new ArrayList( projects.size() );
        for( Iterator it = projects.iterator(); it.hasNext(); ) {
            Project p = (Project)it.next();
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
        Lookup.Result result = p.getLookup().lookup(new Lookup.Template(ProjectOpenedHook.class));
        
        for (Iterator i = result.allInstances().iterator(); i.hasNext(); ) {
            ProjectOpenedHook hook = (ProjectOpenedHook) i.next();
            
            try {
                ProjectOpenedTrampoline.DEFAULT.projectOpened(hook);
            } catch (RuntimeException e) {
                ErrorManager.getDefault().notify(e);
                // Do not try to call its close hook if its open hook already failed:
                INSTANCE.openProjects.remove(p);
            } catch (Error e) {
                ErrorManager.getDefault().notify(e);
                INSTANCE.openProjects.remove(p);
            }
        }
    }
    
    private static void notifyClosed(Project p) {
        Lookup.Result result = p.getLookup().lookup(new Lookup.Template(ProjectOpenedHook.class));
        
        for (Iterator i = result.allInstances().iterator(); i.hasNext(); ) {
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
        
        synchronized (this) {
            if (openProjects.contains(p)) {
                return false;
            }
            openProjects.add(p);
            
            recentProjectsChanged = recentProjects.remove(p);
        }
        
        // Notify projects opened
        notifyOpened(p);
        
        Mutex.EVENT.readAccess(new Action() {
            public Object run() {
                // Open project files
                ProjectUtilities.openProjectFiles(p);
                
                return null;
            }
        });
        
        return recentProjectsChanged;
    }
    
    private static List loadProjectList() {               
        List URLs = OpenProjectListSettings.getInstance().getOpenProjectsURLs();
        List projects = URLs2Projects( URLs );
        return projects;        
    }
    
  
    private static void saveProjectList( List projects ) {        
        List /*<URL>*/ URLs = projects2URLs( projects );
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
        
    private ArrayList /*<FileObject>*/ getTemplateNamesLRU( Project project ) {
        // First take recently used templates and try to find those which
        // are supported by the project.
        
        ArrayList result = new ArrayList( 10 );        
        
        RecommendedTemplates rt = (RecommendedTemplates)project.getLookup().lookup( RecommendedTemplates.class );
        String rtNames[] = rt == null ? new String[0] : rt.getRecommendedTypes();
        PrivilegedTemplates pt = (PrivilegedTemplates)project.getLookup().lookup( PrivilegedTemplates.class );
        String ptNames[] = pt == null ? null : pt.getPrivilegedTemplates();
        ArrayList privilegedTemplates = new ArrayList( Arrays.asList( pt == null ? new String[0]: ptNames ) );
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();            
                
        Iterator it = recentTemplates.iterator();
        for( int i = 0; i < 10 && it.hasNext(); i++ ) {
            String templateName = (String)it.next();
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
        for( int i = result.size(); i < 10 && it.hasNext(); i++ ) {
            String path = (String)it.next();
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
        RecommendedTemplates rt = (RecommendedTemplates)project.getLookup().lookup( RecommendedTemplates.class );
        return rt == null ? null :rt.getRecommendedTypes();
    }
    
    private static List getCategories (String source) {
        ArrayList categories = new ArrayList ();
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
       
        private List /*<ProjectReference>*/ recentProjects;
        
        private int size;
        
        /**
         *@size Max number of the project list.
         */
        public RecentProjectList( int size ) {
            this.size = size;
            recentProjects = new ArrayList( size );
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log("created a RecentProjectList: size=" + size);
            }
        }
        
        public void add( Project p ) {
            int index = getIndex( p );
            
            if ( index == -1 ) {
                // Project not in list
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log("add new recent project: " + p);
                }
                if ( recentProjects.size() == size ) {
                    // Need some space for the newly added project
                    recentProjects.remove( size - 1 ); 
                }
                recentProjects.add( 0, new ProjectReference( p ) );
            }
            else {
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log("re-add recent project: " + p);
                }
                // Project is in list => just move it to first place
                recentProjects.remove( index );
                recentProjects.add( 0, new ProjectReference( p ) );
            }
        }
        
        public boolean remove( Project p ) {
            int index = getIndex( p );
            if ( index != -1 ) {
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log("remove recent project: " + p);
                }
                recentProjects.remove( index );
                return true;
            }
            return false;
        }
        
        
        public List/*<Project>*/ getProjects() {
            List/*<Project>*/ result = new ArrayList( recentProjects.size() );
            // Copy the list
            List/*<ProjectReference>*/ references = new ArrayList( recentProjects );
            for ( Iterator it = references.iterator(); it.hasNext(); ) {
                ProjectReference pRef = (ProjectReference)it.next(); 
                Project p = pRef.getProject();
                if ( p == null || !p.getProjectDirectory().isValid() ) {
                    remove( p );        // Folder does not exist any more => remove from
                    if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                        ERR.log("removing dead recent project: " + p);
                    }
                }
                else {
                    result.add( p );
                }
            }
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log("recent projects: " + result);
            }
            return result;
        }
        
        
        public boolean isEmpty() {
            boolean empty = recentProjects.isEmpty();
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log("recent projects empty? " + empty);
            }
            return empty;
        }
        
        public void load() {
            List/*<URL>*/ URLs = OpenProjectListSettings.getInstance().getRecentProjectsURLs();
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log("recent project list load: " + URLs);
            }
            recentProjects.clear(); 
            for ( Iterator it = URLs.iterator(); it.hasNext(); ) {
                recentProjects.add( new ProjectReference( (URL)it.next() ) );
            }
        }
        
        public void save() {
            List /*<URL>*/ URLs = new ArrayList( recentProjects.size() );
            for ( Iterator it = recentProjects.iterator(); it.hasNext(); ) {
                ProjectReference pRef = (ProjectReference)it.next(); 
                URL pURL = pRef.getURL();
                if ( pURL != null ) {
                    URLs.add( pURL );
                }
            }
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log("recent project list save: " + URLs);
            }
            OpenProjectListSettings.getInstance().setRecentProjectsURLs( URLs );
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
        
        private static class ProjectReference {
            
            private WeakReference projectReference;
            private URL projectURL;
            
            public ProjectReference( URL url ) {                
                this.projectURL = url;
            }
            
            public ProjectReference( Project p ) {
                this.projectReference = new WeakReference( p );
                try {
                    projectURL = p.getProjectDirectory().getURL();                
                }
                catch( FileStateInvalidException e ) {
                    if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                        ERR.log("FSIE getting URL for project: " + p.getProjectDirectory());
                    }
                }
            }
            
            public Project getProject() {
                
                Project p = null; 
                
                if ( projectReference != null ) { // Reference to project exists
                    p = (Project)projectReference.get();
                    if ( p != null ) {
                        // And refers to some project, check for validity:
                        if ( ProjectManager.getDefault().isValid( p ) )
                            return p; 
                        else
                            return null;
                    }
                }
                
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log("no active project reference for " + projectURL);
                }
                if ( projectURL != null ) {                    
                    FileObject dir = URLMapper.findFileObject( projectURL );
                    if ( dir != null && dir.isFolder() ) {
                        try {
                            p = ProjectManager.getDefault().findProject( dir );
                            if ( p != null ) {
                                projectReference = new WeakReference( p ); 
                                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                                    ERR.log("found " + p);
                                }
                                return p;
                            }
                        }       
                        catch ( IOException e ) {
                            // Ignore invalid folders
                            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                                ERR.log("could not load recent project from " + projectURL);
                            }
                        }
                    }
                }
                
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log("no recent project in " + projectURL);
                }
                return null; // Empty reference                
            }
            
            public URL getURL() {
                return projectURL;
            }
            
        }
        
    }
    
    public static class ProjectByDisplayNameComparator implements Comparator {
        
        private static Comparator COLLATOR = Collator.getInstance();
        
        public int compare(Object o1, Object o2) {
            
            if ( !( o1 instanceof Project ) ) {
                return 1;
            }
            if ( !( o2 instanceof Project ) ) {
                return -1;
            }
            
            Project p1 = (Project)o1;
            Project p2 = (Project)o2;
            
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
                ERR.log(ErrorManager.WARNING, p1 + ": ProjectInformation.getDisplayName() should not return null!");
                return -1;
            } else if (n1 != null && n2 == null) {
                ERR.log(ErrorManager.WARNING, p2 + ": ProjectInformation.getDisplayName() should not return null!");
                return 1;
            }
            return 0; // both null
            
        }
        
    }
    
}
