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
package org.netbeans.modules.visualweb.dataconnectivity.project.datasource;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignProperty;

import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.ModelSet;
import org.netbeans.modules.visualweb.insync.ModelSetListener;
import org.netbeans.modules.visualweb.insync.ModelSetsListener;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.visualweb.project.jsf.services.RefreshService;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DesignTimeDataSource;
import org.netbeans.modules.visualweb.dataconnectivity.sql.DesignTimeDataSourceHelper;
import org.netbeans.modules.visualweb.dataconnectivity.explorer.ProjectDataSourceNode;
import org.netbeans.modules.visualweb.dataconnectivity.model.JdbcDriverInfoManager;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;

import org.netbeans.modules.visualweb.api.j2ee.common.RequestedJdbcResource;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.spi.project.AuxiliaryConfiguration;

import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;


/**
 * Manage, persists, and supply (on request) DataSource usage in multiple
 * NB 4 projects.
 *
 * @author Joel Brown
 */
public class ProjectDataSourceTracker{
    
    // static private final String DATASOURCE_NAMES = com.sun.rave.designtime.Constants.ContextData.DATASOURCE_NAMES;
    
    static private final String DATASOURCE_PREFIX = "java:comp/env/"; // NOI18N
    static private final String NON_DEFAULT_DATASOURCE_PREFIX = "java:/"; // NOI18N
    
    static private final String HC_ELEMENT_NAME = "hardcoded-datasource-names" ; // NOI18N
    static private final String HC_ELEMENT_NAMESPACE = "http://creator.sun.com/project/datasources" ; // NOI18N
    static private final boolean HC_ELEMENT_SHARED = true ;
    
    static private final String HC_ATTRIBUTE_NAME = "value" ; // NOI18N
    /**
     * list of DSTracker objects, one for each project
     */
    private HashMap<Project, DSTracker> trackers = new HashMap<Project, DSTracker>() ;
    
    protected ProjectTrackerContextListener insyncDataSourceListener = new ProjectTrackerContextListener() ;
//    protected OpenProjectsListener openProjectsListener = new OpenProjectsListener();
    
    /****
     * This class is meant to be a singleton service
     * instantiated via the dataconnectivity file's layer.xml.
     */
    private ProjectDataSourceTracker() {
        // logInfo( this.toString() ) ;
        
        // TODO:  add listener to insync here for Project openings
        //    and closings.
        ModelSet.addModelSetsListener(insyncDataSourceListener) ;
//        OpenProjects.getDefault().addPropertyChangeListener(openProjectsListener);
    }
    
    private static ProjectDataSourceTracker thisOne = new ProjectDataSourceTracker() ;
    public static ProjectDataSourceTracker getInstance() {
        return thisOne ;
    }
    
    public static RequestedJdbcResource[] getProjectDataSourceInfo(Project project)
        throws NamingException {
        return getDSTracker(project).getProjectDataSourceInfo() ;
    }
    
    public static void addListener(Project project, ProjectDataSourceListener listener) {
        getDSTracker(project).addListener(listener) ;
    }
    
    public static void removeListener(Project project, ProjectDataSourceListener listener) {
        getDSTracker(project).removeListener(listener) ;
    }
    
    public static void addListener(Project project, ProjectDataSourcesListener listener) {
        getDSTracker(project).addListener(listener) ;
    }
    
    public static void removeListener(Project project, ProjectDataSourcesListener listener) {
        getDSTracker(project).removeListener(listener) ;
    }
    
    public static void removeHardcodedDataSource(Project project, String dataSourceNames) {
        getDSTracker(project).removeDataSource(dataSourceNames) ;
    }
    
    public static void addHardcodedDataSource(Project project, String dataSourceNames) {
        getDSTracker(project).addHardcodedDataSource(dataSourceNames) ;
    }
    
    public static String[] getDynamicDataSources(Project project) {
        return getDSTracker(project).getDynamicDataSources() ;
    }
    
    public static String[] getHardcodedDataSources(Project project) {
        return getDSTracker(project).getHardcodedDataSources() ;
    }
    
    public static boolean isHardcodedDataSource(Project project, String datasourceName) {
        return getDSTracker(project).isHardcodedDataSource(datasourceName) ;
    }
    
    public static String[] getFileDataSourceList(Project project) {
        return getDSTracker(project).getFileDataSourceList() ;
    }
    
    // For saving the resolved data source, project import feature for Shortfin
    public static void setResolvedDataSource(Project project, String resolvedDataSource) {
        getDSTracker(project).setResolvedDataSource(resolvedDataSource);
    }
    
    public static String getResolvedDataSource(Project project) {
        return getDSTracker(project).getResolvedDataSource();
    }
    
    
    
    public static org.openide.nodes.Node getNode(Project project) {
        return getDSTracker(project).getNode() ;
    }
    
    public static Project[] getProjects() {
        HashMap<Project, DSTracker> tt = getInstance().trackers ;
        return tt.keySet().toArray(new Project[tt.size()]);
    }
    
    /***
     * for a datasource, refresh designers that use that datasource.
     * CURRENTLY:  datasources is ignored, all projects are refreshed.
     */
    public static void refreshDesignerForDataSources(String datasources) {
        // refreshes the designer panes for all the projects.
        RefreshService designerRefresher = RefreshService.getDefault() ;
        if ( designerRefresher != null ) {
            Project[] projs = ProjectDataSourceTracker.getProjects() ;
            for ( int i = 0 ; i < projs.length ; i++ ) {
                // Ideally, we should check to see if this project uses the
                // imported datasource - but assume not many are open so
                // just refresh them ALL.
                designerRefresher.refresh( projs[i]) ;
            }
        }
    }
    
    public static synchronized DSTracker getDSTracker(Project project) {
        DSTracker tracker = null;
        tracker = getInstance().trackers.get(project);
        
        if (tracker == null ) {
            tracker = getInstance().addDSTracker(project) ;
        }
        return tracker ;
    }
    
    // Refresh data source nodes
    public static void refreshDataSources(Project project) {
        getDSTracker(project).fireChangeEvent();
    }
    
    // Refresh data source references node
    public static void refreshDataSourceReferences(Project project) {
        getDSTracker(project).fireProjectDSReferencesChangeEvent();
    }
    
    /**
     * Used for migrating Creator 2 projects, since a source file must be modeled, not the project
     */
    public static boolean isProjectModeled(final Project project) {
        if (FacesModelSet.getFacesModelIfAvailable(retrieveFileToModel(project)) == null)
            return false;
        else
            return true;
    }
    
    private DSTracker addDSTracker(Project project) {
        DSTracker newTracker = new DSTracker(project) ;
        trackers.put(project, newTracker) ;
        
        return newTracker ;
    }
    
    /**
     *  Used to retrieve a source file from a Creator 2 project for modeling
     */ 
    private static FileObject retrieveFileToModel(Project project) {        
        FileObject[] projContents = project.getProjectDirectory().getChildren();
        FileObject fileToModel = null;
        for (FileObject projItem : projContents) {
            if (projItem.isFolder()) {
                if (projItem.getName().equals("src")) {
                    Enumeration folders = projItem.getFolders(true);
                    // Locate the source file to model
                    while (folders.hasMoreElements()) {
                        FileObject projSrc = (FileObject) folders.nextElement();
                        for (FileObject srcFile : projSrc.getChildren()) {
                            if (srcFile.existsExt("java")) {
                                fileToModel = srcFile;
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return fileToModel;
    }
    
    /***
     * Listens to context changes from insync.
     */
    public class ProjectTrackerContextListener implements ModelSetListener, ModelSetsListener {
        
        /*---------- ModelSetsListener------------*/
        public void modelSetAdded(ModelSet modelSet) {
            // add a listener to the modelSet
            logInfo("adding listener for project " + modelSet.getProject() ) ;
            modelSet.addModelSetListener(insyncDataSourceListener) ;
        }
        
        public void modelSetRemoved(ModelSet modelSet) {
            // don't care for now.
            modelSet.removeModelSetListener(insyncDataSourceListener) ;
        }
        
        public void modelProjectChanged() {
            // EAT Sorry Joel I added this and you may want to do something else ???
        }
        
        /*---------- ModelSetListener------------*/
        public void modelAdded(Model model) {
            changeFileDataSources( model, false  ) ;
        }
        
        public void modelChanged(Model model) {
            changeFileDataSources( model, false ) ;
            
        }
        
        public void modelRemoved(Model model) {
            changeFileDataSources( model, true ) ;
        }
        /*---------- end of interface implements ------------*/
        
        public void changeFileDataSources(Model model, boolean wasRemoved ) {
            logInfo("** received project DS name change event:" ) ; // NOI18N
            FacesModel fModel ;
            if (model instanceof FacesModel) {
                fModel = (FacesModel)model ;
                if ( fModel.isBusted() ) return ;
            } else {
                // others are possible, like ConfigModel.
                // ignore these.
                return ;
            }
            
            FileObject file = model.getFile() ;
            String fname = file.getPath() ;
            Project nb4Proj = FileOwnerQuery.getOwner(file) ;
            
            // trim the name down to the relative path (to the project dir)
            String projRoot = nb4Proj.getProjectDirectory().getPath() ;
            if ( projRoot.length() < fname.length() ) fname = fname.substring(projRoot.length()) ;
            
            String newValue = null  ;
            // get the new value if it was changed or added
            if ( ! wasRemoved ) {
                LiveUnit daUnit = fModel.getLiveUnit() ;
                if ( daUnit != null ) {
                    newValue = getDSNames(daUnit) ;
                }
            }
            
            if ( isLoggable() ) {
                logInfo("    project:  " + nb4Proj ) ; // NOI18N
                logInfo("        dir:  " + projRoot ) ; // NOI18N
                logInfo("    file:     " + fname ) ; // NOI18N
                logInfo("    DSnames:: " + newValue) ; // NOI18N
            }
            getDSTracker(nb4Proj).changeDataSourcesForFile(fname, newValue) ;
        }
    }
    
    // look for dataSourceName property in the beans on this page.
    private String getDSNames( LiveUnit daUnit) {
        DesignBean[] beans = daUnit.getBeans();
        StringBuffer names = new StringBuffer();
        boolean doneFirst = false;
        for (int i = 0; i < beans.length; i++) {
            Class beanClass = beans[i].getBeanInfo().getBeanDescriptor().getBeanClass();
            //TODO: "dataSourceName" within Rowset class will always be a valid datasource.
            //TODO:  However, a "dataSourceName" could be in *any* class, we just
            // don't have a guarentee it represents a jdbc datasource from our server nav.
            // HACK - check for RowSet of if system prop rave.cached is set.
            if (javax.sql.RowSet.class.isAssignableFrom(beanClass)
                || System.getProperty("rave.cached") != null ) { // NOI18N
                DesignProperty[] lps = beans[i].getProperties();
                for (int j = 0; j < lps.length; j++) {
                    if (lps[j].getPropertyDescriptor().getName().equals("dataSourceName")) {  //NOI18N
                        Object v = lps[j].getValue();
                        if (v instanceof String) {
                            if (doneFirst) {
                                names.append(",");
                            } else {
                                doneFirst = true;
                            }
                            names.append((String)v);
                        }
                    }
                }
            }
        }
        return names.toString();
    }
    
             
    /*********
     * this class tracks the data sources for a single project
     */
    private class DSTracker {
        public DSTracker(Project proj) {
            this.project = proj ;
            projectAux = (AuxiliaryConfiguration)project.getLookup().lookup(AuxiliaryConfiguration.class) ;
            if ( projectAux == null ) {
                logErrorInfo("no AuxiliaryConfiguration.class implementation for Project "+ project ) ; // NOI18N
            } else {
                logInfo("we have an AuxiliaryConfiguration") ; // NOI18N
            }
            restoreHardcodedDataSources() ;
        }
        
        private ProjectDataSourceNode projectNode ;  // used in the project navigator
        
        private Project project = null ;
        private HashSet listeners = new HashSet();
        private HashSet listenersForDSContainer = new HashSet();
        
        AuxiliaryConfiguration projectAux = null ;
        
        /*** these array lists (fnames,fnameDataSources, and dynamicDataSourceSet)
         * must be kept consistent.
         * We do this my synchronizing access to all three via dynamicDataSourceSet.
         */
        // This is the list of filenames in this project
        ArrayList fnames = new ArrayList() ; // of String
        // this is the list of datasources for each filename
        ArrayList fnameDataSources = new ArrayList() ;
        // this is the unique list of dynamic data sources in the project
        TreeSet dynamicDataSouceSet = new TreeSet() ; // of String.
        // the "hardCoded" ones are added via the proj nav - not from insync/pages.
        TreeSet hardCodedDataSourceSet = new TreeSet() ; // of String
        Element hcdsElement = null ; // for storing in the project
        String resolvedDataSource = null;
        
        public Project getProject() {
            return project ;
        }
        
        /* the projectNode is displayed in the project Navigator
         * We create a new one for every call.  If the project is closed and
         * then re-opened, we can't return the same node (can't reparent).
         **/
        public org.openide.nodes.Node getNode() {
            projectNode = new  ProjectDataSourceNode(project) ;
            return projectNode ;
        }
        
        public String[] getDynamicDataSources() {
            String[] retVal ;
            synchronized(dynamicDataSouceSet) {
                retVal = (String[])dynamicDataSouceSet.toArray(new String[dynamicDataSouceSet.size()]);
            }
            return retVal ;
        }
        
        public boolean isHardcodedDataSource(String dataSourceName){
            return ( hardCodedDataSourceSet.contains(dataSourceName)) ;
        }
        
        public String[] getHardcodedDataSources() {
            return (String[])hardCodedDataSourceSet.toArray(new String[hardCodedDataSourceSet.size()]);
        }
        
        
        public void addHardcodedDataSource(String dataSourceNames) {
            String[] names = dataSourceNames.split(","); // NOI18N
            for (int j = 0; j < names.length; j++) {
                String newDs = names[j].trim() ;
                if ( newDs.length() > 0 ) {
                    boolean added = hardCodedDataSourceSet.add(newDs);
                    if ( added ) {
                        persistHardcodedDataSources( ) ;
                    }
                }
            }
        }
        
        public void removeHardcodedDataSource(String dataSourceName) {
            if ( this.isHardcodedDataSource(dataSourceName)) {
                hardCodedDataSourceSet.remove(dataSourceName);
                logInfo("removed hard " + dataSourceName) ; // NOI18N
                persistHardcodedDataSources() ;
            }
        }

        public void removeDataSource(String dataSourceName) {
            hardCodedDataSourceSet.remove(dataSourceName);
            logInfo("removed" + dataSourceName) ; // NOI18N
            persistHardcodedDataSources() ;            
        }

        /***
         * save the hard coded data source list and tell listeners
         */
        private void persistHardcodedDataSources( ) {
            
            if ( projectAux == null ) {
                return ;
            }
            
            if ( hcdsElement == null ) {
                restoreHardcodedDataSources() ;
                if ( hcdsElement == null ) {
                    // things seriously screwed up, should never ever be here.
                    return ;
                }
            }
            String cvsString = this.composeCsv(hardCodedDataSourceSet) ;
            logInfo("persisting hard data sources " + cvsString ) ;
            hcdsElement.setAttribute(HC_ATTRIBUTE_NAME,cvsString ) ; // NOI18N
            
            projectAux.putConfigurationFragment(hcdsElement, HC_ELEMENT_SHARED ) ;
            
            //TODO:  force a save of the project.
            try {
                ProjectManager.getDefault().saveProject(project) ;
            } catch( java.io.IOException ioe) {
                // should really never be here!
                logErrorInfo("Could not save project!") ;
            }
            
            // tell everybody.
            fireChangeEvent();
        }
        
        /***
         * restore the list of hard coded listeners.
         * If it's a new project, create the Element to store them.
         * The element (either created or retrieved from the project)
         * will be reused when saving.
         */
        private void restoreHardcodedDataSources( ) {
            
            if ( projectAux == null ) return ;
            
            // Ask the project if it has an exisiting Element
            hcdsElement = projectAux.getConfigurationFragment(HC_ELEMENT_NAME, HC_ELEMENT_NAMESPACE, HC_ELEMENT_SHARED) ;
            if ( hcdsElement != null ) {
                String hcds = hcdsElement.getAttribute(HC_ATTRIBUTE_NAME) ; // NOI18N
                logInfo("Restoring hard DS for " + this.project ) ;
                
                if ( hcds != null ) {
                    TreeSet newSet = new TreeSet() ;
                    String[] names = hcds.split(","); // NOI18N
                    for (int j = 0; j < names.length; j++) {
                        String newDs = names[j].trim() ;
                        if ( newDs.length() > 0 ) {
                            boolean added = newSet.add(names[j]) ;
                            logInfo("restored hard DS " +names[j]) ;
                        }
                    }
                    this.hardCodedDataSourceSet = newSet ;
                    // tell everybody.
                    fireChangeEvent();
                }
            } else {
                // element not currently in the project, so create a new Element
                try {
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = docFactory.newDocumentBuilder();
                    hcdsElement = db.newDocument().createElementNS(HC_ELEMENT_NAMESPACE, HC_ELEMENT_NAME) ;
                    logInfo("new proj element for hard datasources created.") ;
                } catch ( javax.xml.parsers.ParserConfigurationException pce ) {
                    // seriously screwed up.
                    hcdsElement = null ;
                    logErrorInfo("could not create the document element for saving HC DS") ; // NOI18N
                }
            }
        }
        
        // For saving the resolved data source, project import feature for Shortfin
        private void setResolvedDataSource(String resolvedDataSource) {
            this.resolvedDataSource = resolvedDataSource;
        }
        
        private String getResolvedDataSource() {
            return resolvedDataSource;
        }
        
        // notify listeners of change.
        private void fireChangeEvent() {
            if (!listeners.isEmpty()) {
                String[] dynamicDataSources = getDynamicDataSources();
                String[] hardcodedDataSources = getHardcodedDataSources();
                for (Iterator i = listeners.iterator(); i.hasNext();) {
                    ProjectDataSourceListener pair = (ProjectDataSourceListener)i.next();
                    pair.dataSourceChange(
                        new ProjectDataSourceChangeEvent(dynamicDataSources, hardcodedDataSources));
                }
            }
        }
        
        private void fireProjectDSReferencesChangeEvent() {
            if (!listenersForDSContainer.isEmpty()) {
                String resolvedDataSource = getResolvedDataSource();
                for (Iterator i = listenersForDSContainer.iterator(); i.hasNext();) {
                    ProjectDataSourcesListener pair = (ProjectDataSourcesListener)i.next();
                    pair.dataSourcesChange(
                        new ProjectDataSourcesChangeEvent(resolvedDataSource));
                }
            }
        }
        
        private String composeCsv(SortedSet set) {
            String val = ""; // NOI18N
            for (Iterator i = set.iterator(); i.hasNext();) {
                if (val.length() != 0) {
                    val += ","; //NOI18N
                }
                val += (String)i.next();
            }
            return val;
        }
        
        /***
         * compose a list of jdbc info used in deployment.
         */
        public RequestedJdbcResource[] getProjectDataSourceInfo()
            throws NamingException {
            
            // make sure insync has processed all files for dynamic datasources.
            if (Boolean.getBoolean("vwp.designer.jsf.loadModelSync")) { // NOI18N
                FacesModelSet.getInstance(project);
            } else {
                FacesModelSet.startModeling(project);
            }
            
            TreeSet namesList = new TreeSet();
            synchronized( dynamicDataSouceSet ) {
                /* get all DATASOURCE_NAMES properties associated with this project */
                namesList.addAll( dynamicDataSouceSet ) ;
            }
            namesList.addAll( hardCodedDataSourceSet ) ;
            
            ArrayList retList = new ArrayList(); // unique list of data sources.
            DesignTimeDataSourceHelper dsh = new DesignTimeDataSourceHelper();
            
            for (Iterator i = namesList.iterator(); i.hasNext();) {
                String dsName = (String)i.next();
                try {
                    DesignTimeDataSource ds = dsh.getDataSourceFromFullName(dsName) ;
                    
                    String validationTable = ds.getValidationTable() ;
                    
                     // stripDATASOURCE_PREFIX is a hack for JBoss and other application servers due to differences in JNDI string format
                     // for issue 101812
                    retList.add( new RequestedJdbcResource( stripDATASOURCE_PREFIX(dsName),
                                                            ds.getDriverClassName(), ds.getUrl(), ds.getValidationQuery(),
                                                            ds.getUsername(), ds.getPassword(), JdbcDriverInfoManager.getInstance().findDriverJarNames(ds.getDriverClassName()), validationTable));
                } catch (NamingException e) {
                     // stripDATASOURCE_PREFIX is a hack for JBoss and other application servers due to differences in JNDI string format
                     // for issue 101812
                    retList.add( new RequestedJdbcResource(stripDATASOURCE_PREFIX(dsName), null, null,
                                                           null, null, null, null));
                }
            }
            
            logInfo("returning RequestedJdbcResource array of length " + retList.size()); // NOI18N
            for (int j = 0; j < retList.size(); j++) {
                RequestedJdbcResource rjr = (RequestedJdbcResource) retList.get(j) ;
                logInfo("resourceName     : " + rjr.getResourceName() + " ");    // NOI18N
                logInfo("  driverClassName: " + rjr.getDriverClassName() + " "); // NOI18N
                logInfo("  url            : " + rjr.getUrl() + " ");             // NOI18N
                logInfo("  username       : " + rjr.getUsername() + " ");        // NOI18N
                logInfo("  password       : " + DesignTimeDataSource.encryptPassword(  // NOI18N
                            rjr.getPassword()) + " ");        // NOI18N
                logInfo("  validatonTable : " + rjr.getValidationTable());       // NOI18N
            }
            
            return (RequestedJdbcResource[])retList.toArray(new RequestedJdbcResource[retList.size()]);
        }
        
        private String stripDATASOURCE_PREFIX(String orig) {
            if ( orig == null) return orig ;
            if ( orig.startsWith(NON_DEFAULT_DATASOURCE_PREFIX)) return orig.substring( NON_DEFAULT_DATASOURCE_PREFIX.length());
            if ( ! orig.startsWith(DATASOURCE_PREFIX)) return orig ;
            if ( orig.equals(DATASOURCE_PREFIX)) return orig ;
            return orig.substring( DATASOURCE_PREFIX.length() ) ;
        }
        
        private  void addListener( ProjectDataSourceListener listener) {
            listeners.add( listener );
        }
        
        private void removeListener( ProjectDataSourceListener listener) {
            listeners.remove( listener );
        }
        
        private  void addListener( ProjectDataSourcesListener listener) {
            listenersForDSContainer.add( listener );
        }
        
        private void removeListener( ProjectDataSourcesListener listener) {
            listenersForDSContainer.remove( listener );
        }
        
        public synchronized void changeDataSourcesForFile( String fname, String newValue ) {
            
            synchronized( dynamicDataSouceSet ) {
                int origSize = fnames.size() ;
                boolean removeIt = false ;
                if ( newValue == null || "".equals(newValue.trim()) ) { // NOI18N
                    removeIt = true ;
                }
                
                int positionInList = -1 ;
                for (int icnt = 0  ; icnt < origSize ; icnt++ ) {
                    if ( fname.equals((String)fnames.get(icnt))) {
                        positionInList = icnt ;
                        break ;
                    }
                }
                if ( positionInList >= 0 ) {
                    if ( removeIt ) {
                        if (isLoggable() ) {
                            logInfo("  removing file from list:" ) ; // NOI18N
                            logInfo("    file:     " + fname ) ; // NOI18N
                            logInfo("    DSnames:: " + newValue) ; // NOI18N
                        }
                        fnames.remove(positionInList) ;
                        fnameDataSources.remove(positionInList) ;
                        updateDesignDataSourceList() ;
                    } else {
                        String oldValue = (String)fnameDataSources.get(positionInList) ;
                        if ( ! oldValue.equals(newValue)) {
                            // change the value
                            if (isLoggable() ) {
                                logInfo("  changing file:" ) ; // NOI18N
                                logInfo("    file:     " + fname ) ; // NOI18N
                                logInfo("    DSnames:: " + newValue) ; // NOI18N
                            }
                            fnameDataSources.set(positionInList, newValue ) ;
                            updateDesignDataSourceList() ;
                        } else {
                            if (isLoggable() ) {
                                logInfo("  no change to file:" ) ; // NOI18N
                                logInfo("    file:     " + fname ) ; // NOI18N
                                logInfo("    DSnames:: " + newValue) ; // NOI18N
                            }
                            
                        }
                    }
                }
                if ( positionInList < 0 && ! removeIt ) {
                    // add it to the list.
                    fnames.add( fname ) ;
                    fnameDataSources.add( newValue ) ;
                    updateDesignDataSourceList() ;
                }
            }
        }
        
        /****
         * calculate the dynamic datasources in this project by merging the
         * datasources used in each file.
         */
        private void updateDesignDataSourceList() {
            if ( isLoggable() ) {
                logInfo("-----possible update of list of dynamic data sources") ; // NOI18N
            }
            
            synchronized( dynamicDataSouceSet ) {
                TreeSet newSet = new TreeSet() ;
                for ( int icnt = 0 ; icnt < fnameDataSources.size() ; icnt++ ) {
                    String wholeList = (String)fnameDataSources.get(icnt) ;
                    if ( wholeList != null ) {
                        String[] names = wholeList.split(",");  // NOI18N
                        for (int j = 0; j < names.length; j++) {
                            boolean added = newSet.add(names[j]) ;
                        }
                    }
                }
                /***
                 * since user could have just added a data source that was
                 * previously "hard coded", check for hard coded with the same name
                 * name remove if found.
                 **/
                if ( ! newSet.isEmpty()) {
                    for ( Iterator ii = newSet.iterator() ; ii.hasNext() ; ) {
                        String name = (String)ii.next() ;
                        if ( isHardcodedDataSource(name)) {
                            removeDataSource(name) ;
                            logInfo("  removing hard "+name) ; // NOI18N
                        }
                    }
                }
                
                if (isLoggable() ) {
                    logInfo("  calculation of ds in project list:" ) ;  // NOI18N
                    logInfo("        old:: " + dynamicDataSouceSet) ;  // NOI18N
                    logInfo("        new:: " + newSet) ;  // NOI18N
                }
                // compare newSet to old Set
                if ( ! newSet.equals(dynamicDataSouceSet) ) {
                    // data source list has changed.
                    dynamicDataSouceSet = newSet ;
                    fireChangeEvent() ;
                }
            }
        }
        
        /**
         * get the list of files with datasources for each.
         * Used for debugging.
         */
        public String[] getFileDataSourceList() {
            ArrayList retVal = new ArrayList() ;
            StringBuffer sb ;
            synchronized( dynamicDataSouceSet ) {
                for ( int icnt = 0 ; icnt < fnames.size() ; icnt++ ) {
                    sb =  new StringBuffer((String)fnames.get(icnt)) ;
                    sb.append(" :: ").append( (String)fnameDataSources.get(icnt)) ; // NOI18N
                    retVal.add(sb.toString()) ;
                }
                for (Iterator i = hardCodedDataSourceSet.iterator(); i.hasNext();) {
                    sb =  new StringBuffer("Manually added :: ") ; // NOI18N
                    sb.append( (String)i.next()) ;
                    retVal.add(sb.toString()) ;
                }
            }
            return (String[])retVal.toArray(new String[retVal.size()]) ;
        }
    }    
  
    private static final String LOGPREFIX = "PDST: " ; // NOI18N
    private static ErrorManager err = ErrorManager.getDefault().getInstance("rave.dbdatasource"); // NOI18N
    private static void logInfo(String msg) {
        err.log(ErrorManager.INFORMATIONAL, LOGPREFIX + msg);
    }
    private boolean isLoggable() {
        return err.isLoggable(ErrorManager.INFORMATIONAL);
    }
    private static void logErrorInfo(String msg) {
        err.log(ErrorManager.ERROR, LOGPREFIX + msg);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(111) ;
        sb.append("Data Source Tracker (" + trackers.size() +" projects) \n") ; // NOI18N
        for (int icnt = 0 ; icnt < trackers.size() ; icnt++ ) {
            DSTracker dst = (DSTracker)trackers.get(icnt) ;
            sb.append( "*" +  icnt + "*Project " ) ; // NOI18N
            sb.append( dst.project.getProjectDirectory() ).append(" [Dynamic: ") ; // NOI18N
            sb.append( dst.getDynamicDataSources()).append(" | Hard: ") ; // NOI18N
            sb.append( dst.getHardcodedDataSources() ).append(" ] \n") ; // NOI18N
        }
        return sb.toString() ;
    }
    
}
