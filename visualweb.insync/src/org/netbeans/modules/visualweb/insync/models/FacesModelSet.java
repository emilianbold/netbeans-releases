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
package org.netbeans.modules.visualweb.insync.models;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.faces.FacesDesignProject;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.event.DesignProjectListener;
import org.netbeans.modules.visualweb.insync.Model;
import org.netbeans.modules.visualweb.insync.ModelSet;
import org.netbeans.modules.visualweb.insync.SourceUnit;
import org.netbeans.modules.visualweb.insync.faces.ElAttrUpdater;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.jsfsupport.container.FacesContainer;

import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;

/**
 * A specific concrete ModelSet class that knows all about JSF.
 * @author cquinn
 */
public class FacesModelSet extends ModelSet implements FacesDesignProject {

    PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    protected static final String PROJECTDATA_ELEMENT_NAMESPACE = "http://creator.sun.com/project/DesignTimeApiProjectData";
    protected static final String PROJECTDATA_ELEMENT_KEY_PREFIX = "designtimeapi-projectdata-";
    // TODO When done with this type of debugging remove this var and all references
    public static final boolean LOG_SYNC_ALLS = false;

    protected static IdentityHashMap openProjects = new IdentityHashMap();
    
    FacesConfigModel facesConfigModel;

    private ProjectBuiltQuery.Status projectBuiltQueryStatus;
    private ChangeListener projectBuiltQueryStatusChangeListener;

    // Listener to monitor file system changes. This is used to keep the folder structure under the
    // document root folder synchronized with the folder structure under the page bean root folder.
    private static class FolderStructureFileChangeListener implements FileChangeListener {

        public void fileAttributeChanged(FileAttributeEvent fe) {
            // Don't care
        }

        public void fileChanged(FileEvent fe) {
            // Don't care
        }

        public void fileDataCreated(FileEvent fe) {
            // Don't care
        }

        public void fileDeleted(FileEvent fe) {
            // Don't care
        }

        public void fileFolderCreated(FileEvent fe) {
            FileObject fileObject = fe.getFile();

            FileObject documentRootFolder = null;            
            Project project = null;            
            // Search in open projects list if any of them own the folder
            // Note: We cannot use FileOwnerQuery.getOwner() API because
            // the folder may be created during the process of project Creation
            // and FileOwnerQuery.getOwner() throws an exception in such cases.
            Project[] projects = OpenProjects.getDefault().getOpenProjects();
            for (int i = 0; i < projects.length; i++) {
                Project aProject = projects[i];
                if (JsfProjectUtils.isJsfProject(aProject)) {
                    documentRootFolder = JsfProjectUtils.getDocumentRoot(aProject);
                    if (documentRootFolder == null) {
                        continue;
                    }
                    if (FileUtil.isParentOf(documentRootFolder, fileObject)) {
                        project = aProject;
                        break;
                    }
                }
            }
            
            if (project == null || documentRootFolder == null) {
                return;
            }

            String relativePath = FileUtil.getRelativePath(documentRootFolder, fileObject);
            if (relativePath.startsWith("WEB-INF") || relativePath.startsWith("META-INF") || relativePath.startsWith("resources")) { // NOI18N Skip folders names WEB-INF
                return;
            }
            FileObject pageBeanRootFolder = JsfProjectUtils.getPageBeanRoot(project);
            if (pageBeanRootFolder == null) {
                return;
            }
            FileObject destFolder = pageBeanRootFolder.getFileObject(relativePath);
            if (destFolder == null || !destFolder.isValid()) {
                // try to create it
                try {
                    destFolder = FileUtil.createFolder(pageBeanRootFolder, relativePath);
                } catch (IOException e) {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, e.toString());
                }
            }
        }

        public void fileRenamed(FileRenameEvent fe) {
            // Don't care TODO
        }        
    }
    
    private static FolderStructureFileChangeListener folderStructureFileChangeListener = new FolderStructureFileChangeListener();
    
    // for recording the filesystems to which the listener has been added
    private static ArrayList fileChangeListenerAddedTo = new ArrayList();    
    
    static {
        // Go through initial set of project that were opened
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            Project project = projects[i];
            if (JsfProjectUtils.isJsfProject(project)) {
                ensureJspJavaFolderStructure(project);
                openProjects.put(project, project);
                // now add a listener to project's file system - if not already added
                FileSystem aFileSystem;
                try {
                    aFileSystem = project.getProjectDirectory().getFileSystem();
                    if (!fileChangeListenerAddedTo.contains(aFileSystem)) {
                        aFileSystem.addFileChangeListener(folderStructureFileChangeListener);
                        fileChangeListenerAddedTo.add(aFileSystem);
                    }
                } catch (FileStateInvalidException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
    }
    

    private static void ensureJspJavaFolderStructure(Project project) {
        if (project == null) {
            return;
        }
        FileObject documentRootFolder = JsfProjectUtils.getDocumentRoot(project);
        if (documentRootFolder == null) {
            return;
        }
        FileObject pageBeanRootFolder = JsfProjectUtils.getPageBeanRoot(project);
        if (pageBeanRootFolder == null) {
            return;
        }
        ensureJspJavaFolderStructure(documentRootFolder, pageBeanRootFolder, 0);
    }
    
    private static void ensureJspJavaFolderStructure(FileObject srcParentFolder, FileObject destParentFolder, int depth) {
        assert srcParentFolder != null;
        assert destParentFolder != null;
        
        // Bug Fix# 108800
        // Do not copy folder structure if the src folder is under page bean root.
        Project project = FileOwnerQuery.getOwner(srcParentFolder);
        if (project == null) {
        	return;
        }
        
        FileObject pageBeanRootFolder = JsfProjectUtils.getPageBeanRoot(project);
        if (pageBeanRootFolder == null) {
            return;
        }
        
        ClassPath classPath = ClassPath.getClassPath(pageBeanRootFolder, ClassPath.SOURCE);
        if (classPath == null) {
        	return;        	
        }
        
		FileObject srcRoot = classPath.findOwnerRoot(pageBeanRootFolder);
		
		if (srcRoot == null) {
			return;
		}
        
        if (FileUtil.isParentOf(srcRoot, srcParentFolder) || srcRoot.equals(srcParentFolder)) {
        	return;
        }
        
        FileObject[] fileObjects = srcParentFolder.getChildren();
        for (int i = 0; i < fileObjects.length; i++) {
            FileObject fileObject = fileObjects[i];
            if (fileObject.isFolder()) {
                String nameExt = fileObject.getNameExt();
                if (depth == 0 && (nameExt.equals("WEB-INF") || nameExt.equals("META-INF") || nameExt.equals("resources"))) { // NOI18N Skip folders names WEB-INF
                    continue;
                }
                FileObject destFolder = destParentFolder.getFileObject(nameExt);
                if (destFolder == null || !destFolder.isValid()) {
                    // try to create it
                    try {
                        destFolder = destParentFolder.createFolder(nameExt);
                    } catch (IOException e) {
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, e.toString());
                    }
                }
                if (destFolder != null) {
                    // recurse
                    ensureJspJavaFolderStructure(fileObject, destFolder, (depth+1));
                }
            }                        
        }
    }
    
    // monitor project open and close
    protected static class OpenProjectsListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            // The list of open projects has changed
            if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(event.getPropertyName())) {
                // Remove fileChangeListeners from all known filesystems
                for (Iterator iter = fileChangeListenerAddedTo.iterator(); iter.hasNext();) {
                    FileSystem aFileSystem = (FileSystem) iter.next();
                    aFileSystem.removeFileChangeListener(folderStructureFileChangeListener);
                }
                                
                fileChangeListenerAddedTo.clear();
                
                Project[] projects = OpenProjects.getDefault().getOpenProjects();
                for (int i = 0; i < projects.length; i++) {
                    Project project = projects[i];
                    if (JsfProjectUtils.isJsfProject(project)) {
                        // now add a listener to project's file system - if not already added
                        FileSystem aFileSystem;
                        try {
                            aFileSystem = project.getProjectDirectory().getFileSystem();
                            if (!fileChangeListenerAddedTo.contains(aFileSystem)) {
                                aFileSystem.addFileChangeListener(folderStructureFileChangeListener);
                                fileChangeListenerAddedTo.add(aFileSystem);
                            }
                        } catch (FileStateInvalidException e) {
                            ErrorManager.getDefault().notify(e);
                        }
                    }
                    if (!openProjects.containsKey(project)) {
                        if (JsfProjectUtils.isJsfProject(project)) {
                            ensureJspJavaFolderStructure(project);                            
                        }
                    }                    
                }
                
                openProjects.clear();

                // remember the new list of open projects
                for (int i = 0; i < projects.length; i++) {
                    Project project = projects[i];
                    openProjects.put(project, project);                    
                }
            }
            
        }
    }
    
    static {
        OpenProjects.getDefault().addPropertyChangeListener(new OpenProjectsListener());
    }        
    
    //--------------------------------------------------------------------------------- Construction
    
    public static FacesModelSet startModeling(FileObject file) {
        Project project = FileOwnerQuery.getOwner(file);
        if (project == null) {
            return null;
        }
        return startModeling(project);
    }
    
    public static FacesModelSet startModeling(Project project) {
        return (FacesModelSet) ModelSet.startModeling(project, FacesModelSet.class);
    }

    public static FacesModelSet getInstance(FileObject file) {
        return (FacesModelSet) ModelSet.getInstance(file, FacesModelSet.class);
    }

    /**
     *
     * @param project
     * @return
     */
    public static FacesModelSet getInstance(Project project) {
        return (FacesModelSet) ModelSet.getInstance(project, FacesModelSet.class);
    }
    
    public static FacesModel getFacesModelIfAvailable(FileObject fileObject) {
    	ModelSet modelSet = getModelSet(fileObject);   
        if (modelSet instanceof FacesModelSet && modelSet.isInitialized()) {
           return ((FacesModelSet)modelSet).getFacesModel(fileObject);
        }
        return null;
    }

    private FacesContainer facesContainer;

    /**
     * @param project
     */
    public FacesModelSet(Project project) {
        super(project);
        getFacesContainer();
        
        facesConfigModel = new FacesConfigModel(this);
        setConfigModel(facesConfigModel);

        //In case of new project, we need to model all the managed beans in order
        //to generate the cross referencing accessors
        Object newProject = project.getProjectDirectory().getAttribute("NewProject"); //NOI18N
        if(newProject instanceof Boolean && (Boolean)newProject) {
            //Sync all models
            try {
                syncAll();
            }finally {
                try {
                    //Reset the attribute to prevent initial modeling during the startup
                    project.getProjectDirectory().setAttribute("NewProject", null); //NOI18N
                }catch(IOException ioe) {
                    assert Trace.trace(this.getClass(), "Failed to reset the attribute: " + project);  //NOI18N
                }
            }
            //Remove any dead ones that aborted opening.
            for (Iterator i = models.values().iterator(); i.hasNext(); ) {
                Model m = (Model)i.next();
                if (!m.isValid())
                    i.remove();
            }
            //We cannot add accessors when each managed bean is modeled during project creation
            //because we do not know the order of creation of managed beans. Therefore adding
            //the accessors after syncAll()
            for (Iterator i = models.values().iterator(); i.hasNext(); ) {
                Model m = (Model)i.next();
                if (m instanceof FacesModel) {
                    ((FacesModel)m).addXRefAccessors();
                }
            }
            
            // run flush once in case any models self-adjusted themselves
            flushAll();
            // save the files to prevent them being open unsaved
            saveAll();       
        }
        
        projectBuiltQueryStatus = ProjectBuiltQuery.getStatus(project);
        projectBuiltQueryStatusChangeListener  = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                try {
                    if (projectBuiltQueryStatus != null && projectBuiltQueryStatus.isBuilt()) {
                        classPathChanged();
                    }
                } catch (IllegalStateException ise) {
                    ErrorManager.getDefault().notify(ise);
                }               
            }
        };
        projectBuiltQueryStatus.addChangeListener(projectBuiltQueryStatusChangeListener);
    }

    /**
     *
     */
    public void destroy() {
    	// Prevent NPE
    	if (facesContainer != null) {
    		facesContainer.destroy();
    	}
        if (projectBuiltQueryStatus != null) {
            projectBuiltQueryStatus.removeChangeListener(projectBuiltQueryStatusChangeListener);
        }
        projectBuiltQueryStatusChangeListener = null;
        projectBuiltQueryStatus = null;
        super.destroy();
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * @return
     */
    public FacesContainer getFacesContainer() {
        if (facesContainer == null) {
            //long start = System.currentTimeMillis();
            boolean isPortlet = false;
            if (project != null && JsfProjectUtils.getPortletSupport(project) != null) {
                isPortlet = true;
            }
            facesContainer = new FacesContainer(getProjectClassLoader(), isPortlet);
            //long duration = System.currentTimeMillis() - start;
            //System.err.println("FMS: FacesContainer initialize time: " + duration + "ms");
        }
        return facesContainer;
    }

    /**
     * @return
     */
    public FacesConfigModel getFacesConfigModel() {
        return facesConfigModel;
    }

    /**
     * @return
     */
    public FacesModel[] getFacesModels() {
        //!CQ TODO: later, these might not all be FacesModels. Will need to use an iterator
        return (FacesModel[])getModelsMap().values().toArray(FacesModel.EMPTY_ARRAY);
    }

    /*
     * Override in order to ensure that if a .java file object is passed, that
     * we will be able to return the model which is keyed under the .jsp file.
     */
    public Model getModel(FileObject file) {
        Model model = super.getModel(file);
        if (model == null) {
            FileObject jspFile = FacesModel.getJspForJava(file);
            if (jspFile != null)
                model = super.getModel(jspFile);
        }
        return model;
    }

    /**
     * Get the FacesModel for a given FileObject iff the file is modeled. Allow for either file to
     * be passed to look up a paired file model.
     *
     * @param file object
     * @return the FacesModel for a given FileObject
     */
    public FacesModel getFacesModel(FileObject file) {
        FacesModel model = (FacesModel) getModel(file);
        if(model != null) {
            model.sync();
        }
        return model;
    }
    
    /**
     * Get the FacesModel for a given FileObject iff the file is modeled. Allow for either file to
     * be passed to look up a paired file model.
     *
     * @param file object
     * @return the FacesModel for a given FileObject
     */
    public FacesModel getFacesModel(String beanName) {
        if (beanName == null)
            return null;
        for (Iterator iterator=getModelsMap().values().iterator(); iterator.hasNext(); ) {
            Model model = (Model) iterator.next();
            if (model instanceof FacesModel) {
                FacesModel facesModel = (FacesModel) model;
                if (beanName.equals(facesModel.getBeanName()))
                        return facesModel;
            }
        }
        return null;
    }
    
    //----------------------------------------------------------------------------- Model processing

    /**
     * @param oldname
     * @param newname
     */
    public void updateBeanElReferences(String oldname, String newname) {
        FacesModel[] fms = getFacesModels();
        for (int mi = 0; mi < fms.length; mi++) {
            LiveUnit lu = fms[mi].getLiveUnit();
            if (lu != null) {
                ArrayList beans = lu.getBeansList();
                for (int bi = 0, n = beans.size(); bi < n; bi++) {
                    DesignProperty[] props = ((DesignBean)beans.get(bi)).getProperties();
                    for (int pi = 0; pi < props.length; pi++) {
                        String ps = props[pi].getValueSource();
                        if (ps != null) {
                            String newps = ElAttrUpdater.update(ps, oldname, newname);
                            if (newps != null)
                                props[pi].setValueSource(newps);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param oldname
     */
    public void removeBeanElReferences(String oldname) {
        String match = "#{" + oldname;
        FacesModel[] fms = getFacesModels();
        for (int mi = 0; mi < fms.length; mi++) {
            LiveUnit lu = fms[mi].getLiveUnit();
            if (lu != null) {
                DesignBean[] beans = lu.getBeans();
                ArrayList beansList = lu.getBeansList();
                for (int bi = 0; bi < beans.length; bi++) {
                    //Make sure the bean is not removed from the list as a result of unset
                    if(beansList.indexOf(beans[bi]) != -1) {
                        DesignProperty[] props = beans[bi].getProperties();
                        for (int pi = 0; pi < props.length; pi++) {
                            String ps = props[pi].getValueSource();
                            if (ps != null && ps.startsWith(match))
                                props[pi].unset();
                        }
                    }
                }
            }
        }
    }

    public Collection getBeanNamesToXRef(ManagedBean.Scope scope, FacesModel facesModel) {
        ArrayList list = new ArrayList();
        if (scope == null)
            return list;
        FacesModel[] bModels = getFacesModels();
        for (int i = 0; i < bModels.length; i++) {
            if (isModelToXRef(facesModel, scope, bModels[i])) {
                list.add(bModels[i].getBeanName());
            }
        }
        return list;
    }
    
    private boolean isModelToXRef(FacesModel fromModel, ManagedBean.Scope fromScope, FacesModel toModel) {
        // TODO add asserts       
        if (!toModel.isBusted()) {
            String toName = toModel.getBeanName();
            if (toName != null) {
                ManagedBean toMb = getFacesConfigModel().getManagedBean(toName);
                if (toMb != null) {
                    ManagedBean.Scope toScope = toMb.getManagedBeanScope();
                    boolean include = false;
                    //Page and Request beans are of same scope, but we still
                    //need request bean accessors in page bean
                    if(fromModel.isPageBean())
                        include = toScope.compareTo(fromScope) >= 0;
                    else
                        include = toScope.compareTo(fromScope) > 0;
                    //No page bean accessors are allowed
                    if (include && !toModel.isPageBean()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    //------------------------------------- DesignProject & FacesDesignProject implementation
     /**
     * Add a property change listener to the design project so that it could fire
     * events such as classloader changed.
     */
    public void addPropertyChangeListener(PropertyChangeListener propChangeListener){
        propertyChangeSupport.addPropertyChangeListener(propChangeListener);
    }
     /**
     * Remove property change listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener propChangeListener){
        propertyChangeSupport.removePropertyChangeListener(propChangeListener);
    }

    public ClassLoader getContextClassLoader(){
        return getProjectClassLoader();
    } 

    /*
     * @see com.sun.rave.designtime.DesignProject#getDesignContexts()
     */
    public DesignContext[] getDesignContexts() {
        syncAll();
        FacesModel[] models = getFacesModels();
        if (models != null && models.length > 0) {
            ArrayList units = new ArrayList();
            for (int i = 0; i < models.length; i++) {
                FacesModel model = models[i];
                if (model.isValid() && !model.isBusted()) {
                    LiveUnit lu = models[i].getLiveUnit();
                    if (lu != null)
                        units.add(lu);
                }
            }
            return (DesignContext[])units.toArray(new LiveUnit[units.size()]);
        }
        return new DesignContext[0];
    }
    
    public DesignContext findDesignContext(String beanName) {
        ManagedBean mb = getFacesConfigModel().getManagedBean(beanName);
        if(mb != null) {
            return findDesignContext(mb, false);
        }
        return null;
    }
    
    public DesignContext findDesignContext(ManagedBean mb, boolean ignorePage) {
        FacesModel facesModel = null;
        //getFacesModel(String beanName) relies on java file, in case of
        //page bean, java file may not be set if the model is not synced yet
        //Therefore, try to get the model by bean name, if it fails then the
        //managed bean should be a page and it is handled next
        facesModel = getFacesModel(mb.getManagedBeanName());
        if(facesModel != null && facesModel.isPageBean() && ignorePage) {
            return null;
        }
        
        if(!ignorePage && facesModel == null) {
            String javaFileName = mb.getManagedBeanClass().replace('.', '/') + ".java";  //NOI18N;
            Sources sources = ProjectUtils.getSources(getProject());
            SourceGroup groups[] = sources.getSourceGroups("java");
            FileObject javaFile = null;
            for (int i=0; i < groups.length; i++) {
                SourceGroup group = groups[i];
                FileObject sourceFolder = group.getRootFolder();
                javaFile = sourceFolder.getFileObject(javaFileName);
                if (javaFile != null) {
                    //Check if it is a page bean if it is to be ignored
                    FileObject jspFile = JsfProjectUtils.getJspForJava(javaFile);
                    if( jspFile != null) {
                        facesModel = getFacesModel(jspFile);
                        break;
                    }
                }
            }
        }
        
        if(facesModel != null) {
            facesModel.sync();
            if(facesModel.isValid() && !facesModel.isBusted()) {
                return facesModel.getLiveUnit();
            }
        }
        return null;
    }
    
    public DesignContext[] findDesignContexts(String[] scopes) {
        Collection scopeList = Arrays.asList(scopes);
        FacesConfigModel facesConfigModel = getFacesConfigModel();
        ManagedBean[] mbs = facesConfigModel.getManagedBeans();
        List dcs = new ArrayList();
        for (int i = 0; i < mbs.length; i++) {
            if(scopeList.contains(mbs[i].getManagedBeanScope().toString())) {
                boolean ignorePage = false;
                if(mbs[i].getManagedBeanScope().equals(ManagedBean.Scope.REQUEST)) {
                    ignorePage = true;
                }
                DesignContext dc = findDesignContext(mbs[i], ignorePage);
                if(dc != null)
                    dcs.add(dc);
            }
        }
        
        return (DesignContext[])dcs.toArray(new LiveUnit[dcs.size()]);
    }

    /**
     * Creates a new DesignContext (backing file) in this project.
     *
     * @param className The desired fully-qualified class name for the file
     * @param baseClass The desired base class for the file
     * @param contextData A Map of context data to apply to the newly created context file
     * @return The newly created DesignContext, or null if the operation was unsuccessful
     */
    public DesignContext createDesignContext(String className, Class baseClass, Map contextData) {
        //!JOE: TODO: create a new managed bean (managed-beans.xml) and put in the scope
        //      defined by user:  contextData.get(Constants.ContextData.SCOPE)
        // !EAT TODO: discussions on this with Joe
        return null;
    }

    protected boolean removeUnits(final SourceUnit[] units) {
        boolean result = true;
        boolean removeFile = true;

        //If any of the files are marked non sharable(for example if there are 
        //conflicts during CVS update), mark a flag to indicate that the 
        //DataObjects should not be deleted
        for (int i=0; i < units.length; i++) {
            SourceUnit unit = units[i];
            if(unit != null) {
                DataObject object = unit.getDataObject();
                if(object != null) {
                    FileObject fObj = object.getPrimaryFile();
                    if(fObj != null && SharabilityQuery.getSharability(FileUtil.toFile(fObj))
                        == SharabilityQuery.NOT_SHARABLE) {
                        removeFile = false;
                        break;
                    }
                }
            }
        }
        
        for (int i=0; i < units.length; i++) {
            SourceUnit unit = units[i];
            if (unit != null) {
                // save before the delete, to avoid FileAlreadyLockedException (IZ 101853)
                unit.save();
                unit.destroy();
                DataObject object = unit.getDataObject();
                if (removeFile && object != null && object.isValid()) {
                    try {
                        object.delete();
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Removes an existing DesignContext (backing file) from this project.
     *
     * @param context The desired DesignContext to remove from the project
     * @return true if the operation was successful, false if not
     */
    public boolean removeDesignContext(DesignContext context) {
        // !EAT TODO: need to look into issues of deleting the file actually being worked on ?
        // Should delete be done aync, but then how to handle return result
        // remove a managed bean from the project (and managed-beans.xml)
        if(context == null)
            return false;
        
        FacesModel model = ((LiveUnit) context).getModel();
        boolean removed = removeUnits(new SourceUnit[] {model.getJavaUnit(), model.getMarkupUnit()});
        if (!removed)
            return false;
        String beanName = model.getBeanName();
        if (beanName != null) {
            FacesConfigModel facesConfigModel = ((FacesModelSet)model.getOwner()).getFacesConfigModel();
            ManagedBean mb = facesConfigModel.getManagedBean(beanName);
            if (mb != null)
                facesConfigModel.removeManagedBean(mb);
        }
        models.remove(model);
        return true;
    }

    //----------------------------------------------------------------------------- Resource Methods

    /**
     * <p>Returns the set of top-level resources in this project as an array of local resource
     * identifiers.  The URIs are paths from the project root, including folder hiearchy within the
     * project.  Use <code>getResourceFile(URI)</code> to retrieve a File object for a particular
     * resource in the project.</p>
     *
     * @param rootPath The root path to fetch resources underneath.  Passing <code>null</code> will
     *        start at the root of the project.
     * @param recurseFolders <code>true</code> to include the sub-resources inside of any folders
     * @return A URI[] representing all the resource files under the specified root path
     */
    public URI[] getResources(URI folderUri, boolean recurseFolders) {
        if (folderUri == null)
            folderUri = getProjectDirectoryUri();
        FileObject folder = resolveToFileObject(folderUri);
        if (folder == null)
            return new URI[0];
        Enumeration folderEnum = folder.getChildren(recurseFolders);
        ArrayList list = new ArrayList();
        while (folderEnum.hasMoreElements()) {
            FileObject fileObject = (FileObject) folderEnum.nextElement();
            URI uri = relativize(fileObject);
            if (uri != null)
                list.add(uri);
        }
        URI[] result = new URI[list.size()];
        list.toArray(result);
        return result;
    }

    /**
     * Returns a File object containing the specified resource.
     *
     * @param resourceUri The desired project relative resource uri to fetch a file object
     * @return A File object containing the project resource
     */
    public File getResourceFile(URI resourceUri) {
        FileObject fileObject = resolveToFileObject(resourceUri);
        if (fileObject == null)
            return null;
        return FileUtil.toFile(fileObject);
    }

    /**
     * Copies a resource into this project, and converts the external URL into a local URI
     * (resource identifier string).
     *
     * @param sourceUrl A URL pointing to the desired external resource
     * @param targetUri The desired resource URI (path) within the project directory
     * @return The resulting project relative resource uri (resourceUri)
     * @throws IOException if the resource cannot be copied
     */
    public URI addResource(URL sourceUrl, URI targetUri) throws IOException {
        FileObject source = resolveToFileObject(sourceUrl);
        if (source == null)
            return null;
        URI resolvedUri = resolveToUri(targetUri);
        File targetFile = new File(resolvedUri);
        File parentFile = targetFile.getParentFile();
        if (! parentFile.exists())
            parentFile.mkdirs();
        FileObject parentFileObject = FileUtil.toFileObject(parentFile);
        if (parentFileObject == null)
            return null;
        // Extract the extension part to fit the NB API below
        String name = targetFile.getName();
        String extension = "";
        int index = name.lastIndexOf ('.');
        if (index > 0) {
            extension = name.substring(index + 1);
            name = name.substring(0, index);
        }
        FileObject copy = source.copy(parentFileObject, name, extension);
        URI uri = relativize(copy);
        return uri;
    }

    public FileObject getProjectDirectory() {
        return getProject().getProjectDirectory();
    }
    
    public FileObject getDocumentDirectory() {
        return JsfProjectUtils.getDocumentRoot(getProject());
    }
    
    protected URI getProjectDirectoryUri() {
        try {
            URI projectDirectoryUri = new URI(getProject().getProjectDirectory().getURL().toExternalForm());
            return projectDirectoryUri;
        } catch (URISyntaxException e) {
            assert Trace.trace(this.getClass(), e);
            return null;
        } catch (FileStateInvalidException e) {
            assert Trace.trace(this.getClass(), e);
            return null;
        }
    }

    public URI relativize(FileObject file) {
        URI projectDirectoryUri = getProjectDirectoryUri();
        if (projectDirectoryUri == null)
            return null;
        try {
            URI fileUri = new URI(file.getURL().toExternalForm());
            URI uri = projectDirectoryUri.relativize(fileUri);
            return uri;
        } catch (FileStateInvalidException e) {
            assert Trace.trace(this.getClass(), e);
        } catch (URISyntaxException e) {
            assert Trace.trace(this.getClass(), e);
        }
        return null;
    }

    /**
     * Return a URI which provides a path to get to file, from relativeTo.
     * 
     * @param file
     * @param relativeTo
     * @return
     */
    public URI relativize(FileObject file, FileObject relativeTo) {
        if (file == null || relativeTo == null)
            return null;
        try {
            URI fileUri = new URI(file.getURL().toExternalForm());
            URI relativeToUri = new URI(relativeTo.getURL().toExternalForm());
            return relativeToUri.relativize(fileUri);
        } catch (URISyntaxException e) {
            assert Trace.trace(this.getClass(), e);
            return null;
        } catch (FileStateInvalidException e) {
            assert Trace.trace(this.getClass(), e);
            return null;
        }
    }
    
    protected FileObject resolveToFileObject(URI uri) {
        if (uri == null)
            return null;
        URL url = resolveToUrl(uri);
        if (url == null)
            return null;
        FileObject file = resolveToFileObject(url);
        return file;
    }

    protected FileObject resolveToFileObject(URL url) {
        if (url == null)
            return null;
        FileObject file = URLMapper.findFileObject(url);
        return file;
    }

    protected URI resolveToUri(URI uri) {
        URL url = resolveToUrl(uri);
        if (url == null)
            return null;
        try {
            URI result = new URI(url.toExternalForm());
            return result;
        } catch (URISyntaxException e) {
            assert Trace.trace(this.getClass(), e);
            return null;
        }
    }

    protected URL resolveToUrl(URI uri) throws IllegalArgumentException {
        if (uri == null)
            return null;
        try {
            URI projectDirectoryUri = getProjectDirectoryUri();
            URI resolvedUri = projectDirectoryUri.resolve(uri);
            URL url = resolvedUri.toURL();
            return url;
        } catch (MalformedURLException e) {
            assert Trace.trace(this.getClass(), e);
            return null;
        }
    }

    /**
     * Removes a resource from the project directory.
     *
     * @param resourceUri The desired resource to remove from the project
     * @return boolean <code>true</code> if the resource was successfully removed,
     *         <code>false</code> if not
     */
    public boolean removeResource(URI resourceUri) {
        //!EAT TODO: we need to add some security to this ?  Any file can be removed ???
        FileObject file = resolveToFileObject(resourceUri);
        if (file == null)
            return false;
        try {
            file.delete();
            return true;
        } catch (IOException e) {
            assert Trace.trace(this.getClass(), e);
            return false;
        }
    }


    HashMap userData = new HashMap();

    /*
     * @see com.sun.rave.designtime.DesignProject#setProjectData(java.lang.String, java.lang.Object)
     */
    public void setProjectData(String key, Object data) {
        userData.put(key, data);
        // PROJECTTODO2: Still need to put this on project save not on set.
        AuxiliaryConfiguration config = (AuxiliaryConfiguration) project.getLookup().lookup(AuxiliaryConfiguration.class);
        assert config != null : "project has no AuxiliaryConfiguration";
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            // !EAT TODO move this to flushProjectData()
            DocumentBuilder builder = docFactory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element element = doc.createElementNS(PROJECTDATA_ELEMENT_NAMESPACE, PROJECTDATA_ELEMENT_KEY_PREFIX + key) ;
            String string = data != null ? data.toString() : null;
            Node node = doc.createCDATASection(string);
            element.appendChild(node);
            doc.appendChild(element);
            config.putConfigurationFragment(element, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return;
    }

    /*
     * @see com.sun.rave.designtime.DesignProject#getProjectData(java.lang.String)
     */
    public Object getProjectData(String key) {
        if (userData.containsKey(key)) {
            Object result = userData.get(key);
            return String.valueOf(result);
        }
//        if (JsfProjectUtils.isEnabled()) {
            AuxiliaryConfiguration config = (AuxiliaryConfiguration) project.getLookup().lookup(AuxiliaryConfiguration.class);
            assert config != null : "project has no AuxiliaryConfiguration";
            Element element = config.getConfigurationFragment(PROJECTDATA_ELEMENT_KEY_PREFIX + key, PROJECTDATA_ELEMENT_NAMESPACE, false);
            if (element == null)
                return null;
            String result = element.getFirstChild().getNodeValue();
            userData.put(key, result);
            return result;
//        }
//        return project.getProperty(key);
    }

    /**
     *
     */
    protected void flushProjectData() {
        // PROJECTTODO2: cleanup, this needs to get called somehow
        // no-op until I move the code from setProjectData to here and have this called somehow
// This is code that existed prior to new project API
//          for (Iterator i = userData.entrySet().iterator(); i.hasNext(); ) {
//              Map.Entry entry = (Map.Entry)i.next();
//              Object data = entry.getValue();
//              Util.updateItemProperty(project, (String)entry.getKey(),
//                      data != null ? data.toString() : null);
//          }
    }

    /*
     * @see com.sun.rave.designtime.DesignProject#setGlobalData(String, Object)
     * EAT: TODO: implement this!  (was Ide.getIdeData())
     */
    public void setGlobalData(String key, Object data) {}

    /*
     * @see com.sun.rave.designtime.DesignProject#getGlobalData(String)
     * EAT: TODO: implement this!  (was Ide.setIdeData())
     */
    public Object getGlobalData(String key) { return null; }


    //---------------------------------------------------------------------------------- DisplayInfo

    /*
     * @see com.sun.rave.designtime.DisplayInfo#getDisplayName()
     */
    public String getDisplayName() {
        return ProjectUtils.getInformation(project).getDisplayName();
    }

    /*
     * @see com.sun.rave.designtime.DisplayInfo#getDescription()
     */
    public String getDescription() {
        return getDisplayName();
    }

    public Image getLargeIcon() { return null; }
    public Image getSmallIcon() { return null; }
    public String getHelpKey() { return null; }

    /**
     * Respond to changes in the project class path by updating the models.
     * @see com.sun.rave.project.model.ProjectContentChangeListener#classPathChanged(com.sun.rave.project.model.ProjectContentChangeEvent)
     * PROJECTTODO2: I dont get this event anymore
     */
    public void classPathChanged() {
        ClassLoader oldClassLoader = classLoader; 
        super.classPathChanged();
        
        // XXX why called again. Already called in super.classPathChanged()
        ClassLoader classLoader = getProjectClassLoader();
        for (Iterator i = getModelsMap().values().iterator(); i.hasNext(); )
            ((FacesModel)i.next()).updateClassLoader(classLoader);
        if (facesContainer != null) {
            facesContainer.setClassLoader(classLoader);
        }
        propertyChangeSupport.firePropertyChange(FacesDesignProject.CONTEXT_CLASS_LOADER, oldClassLoader, classLoader); //NOI18N 
    }

    //--------------------------------------------------------------------------- DesignProject Events

    protected final ArrayList listeners = new ArrayList();

    /*
     * @see com.sun.rave.designtime.DesignProject#addDesignProjectListener(com.sun.rave.designtime.DesignProjectListener)
     */
    public void addDesignProjectListener(DesignProjectListener listener) {
        listeners.add(listener);
    }

    /*
     * @see com.sun.rave.designtime.DesignProject#removeDesignProjectListener(com.sun.rave.designtime.DesignProjectListener)
     */
    public void removeDesignProjectListener(DesignProjectListener listener) {
        listeners.remove(listener);
    }

    /*
     * @see com.sun.rave.designtime.DesignProject#getDesignProjectListeners()
     */
    public DesignProjectListener[] getDesignProjectListeners() {
        return (DesignProjectListener[])listeners.toArray(new DesignProjectListener[listeners.size()]);
    }

    /**
     * A small hack to prevent the creation of contexts objects if there are no listeners,
     * such as project open time.
     * 
     * @return
     */
    public boolean hasDesignProjectListeners() {
        return !listeners.isEmpty();
    }
    
    /**
     * @param context
     */
    protected void fireContextOpened(DesignContext context) {
        DesignProjectListener[] listeners = getDesignProjectListeners();
        for (int i = 0; i< listeners.length; i++) {
            listeners[i].contextOpened(context);
        }
    }

    /**
     * @param context
     */
    protected void fireContextClosed(DesignContext context) {
        DesignProjectListener[] listeners = getDesignProjectListeners();
        for (int i = 0; i< listeners.length; i++) {
            listeners[i].contextClosed(context);
        }
    }

    protected Collection evalOrderModels(Collection modelsToOrder) {
        FacesConfigModel facesConfigModel = getFacesConfigModel();
        if (facesConfigModel == null)
            return modelsToOrder;
        final HashMap modelsByName = new HashMap();
        for (Iterator iterator=modelsToOrder.iterator(); iterator.hasNext(); ) {
            FacesModel model = (FacesModel) iterator.next();
            String name = model.getBeanName();
            // this is to ensure that even models without names are included in modelsByName
            if (name == null)
                name = "### NO NAME ###";
            ArrayList list = (ArrayList) modelsByName.get(name);
            if (list == null) {
                list = new ArrayList();
                modelsByName.put(name, list);
            }
            list.add(model);
        }
        ArrayList orderedModels = new ArrayList(modelsToOrder.size());
        ManagedBean[] managedBeans = facesConfigModel.getManagedBeans();
        Arrays.sort(managedBeans, new Comparator() {
            public int compare(Object object1, Object object2) {
                ManagedBean managedBean1 = (ManagedBean) object1;
                ManagedBean managedBean2 = (ManagedBean) object2;
                int compare = managedBean1.getManagedBeanScope().compareTo(managedBean2.getManagedBeanScope());
                if (compare != 0)
                    return compare;
                if (managedBean1.getManagedBeanScope() != ManagedBean.Scope.REQUEST)
                    return 0;
                ArrayList list1  = (ArrayList) modelsByName.get(managedBean1.getManagedBeanName());
                ArrayList list2 = (ArrayList) modelsByName.get(managedBean2.getManagedBeanName());
                if (list1 == null || list2 == null)
                    return 0;
                FacesModel model1 = (FacesModel) list1.get(0);
                FacesModel model2 = (FacesModel) list2.get(0);
                if (model1.isPageBean() && model2.isPageBean())
                    return 0;
                if (model1.isPageBean())
                    return 1;
                return -1;
            }
        });
        for (int i=0; i < managedBeans.length; i++) {
            ManagedBean ManagedBean = managedBeans[i];
            ArrayList list = (ArrayList) modelsByName.get(ManagedBean.getManagedBeanName());
            if (list != null) {
                orderedModels.addAll(list);
                modelsByName.remove(ManagedBean.getManagedBeanName());
            }
        }
        // Add non-pages first, in order to make sure ensurexref is done properly
        for (Iterator iterator=modelsByName.values().iterator(); iterator.hasNext(); ) {
            ArrayList list = (ArrayList) iterator.next();
            FacesModel model = (FacesModel) list.get(0);
            if (!model.isPageBean())
                orderedModels.addAll(list);
        }
        for (Iterator iterator=modelsByName.values().iterator(); iterator.hasNext(); ) {
            ArrayList list = (ArrayList) iterator.next();
            FacesModel model = (FacesModel) list.get(0);
            if (model.isPageBean())
                orderedModels.addAll(list);
        }
        return orderedModels;
    }

    public void removeModel(Model model) {
        LiveUnit liveUnit = null;
        if (model instanceof FacesModel) {
            FacesModel facesModel = (FacesModel) model;
            liveUnit = facesModel.getLiveUnit();
            removeDesignContext((DesignContext) liveUnit);
        }
        super.removeModel(model);
    }

    public FileObject getJavaRootFolder() {
        return JsfProjectUtils.getSourceRoot(getProject());
    }
    
    public FileObject getPageJavaRootFolder() {
        return JsfProjectUtils.getPageBeanRoot(getProject());
    }
    
    public FileObject getPageJspRootFolder() {
        return JsfProjectUtils.getDocumentRoot(getProject());
    }
    
    /**
     * Other can be a file in my web or java folder tree.  Either
     * way I return the appropriate java one.
     * If in neither tree, return null.
     * 
     * @param other
     * @return
     */
    public FileObject getJavaFolderFor(FileObject other, boolean tryHard) {
        if (other == null)
            return null;
        FileObject jspRoot = getPageJspRootFolder();
        String relativePath = FileUtil.getRelativePath(jspRoot, other);
        if (relativePath != null) {
            // File is in jsp root
            FileObject javaRoot = getPageJavaRootFolder();
            FileObject result = javaRoot.getFileObject(relativePath);
            if (result != null)
                return result;
        } else {
            FileObject javaRoot = getJavaRootFolder();
            if (FileUtil.isParentOf(javaRoot, other))
                return other;
        }
        if (!tryHard)
            return null;
        // Try a little harder to handle in-place edit where file object was renamed, but data objects not
        FileObject result = null;
        for (Iterator i=getModelsMap().values().iterator(); i.hasNext(); ) {
            Model model = (Model) i.next();
            if (model instanceof FacesModel) {
                FacesModel facesModel = (FacesModel) model;
                if (facesModel.getMarkupFile() != null && facesModel.getMarkupFile().getParent() == other) {
                    return getJavaFolderFor(facesModel.getJavaFile().getParent(), false);
                }
                if (facesModel.getJavaFile() != null && facesModel.getJavaFile().getParent() == other) {
                    return getJavaFolderFor(facesModel.getJavaFile().getParent(), false);
                }
            }
        }
        return result;
    }

    int counter = LOG_SYNC_ALLS?0:0; // Done this way to make sure it gets removed when the var is removed
    long startTime = LOG_SYNC_ALLS?0l:0l; // Done this way to make sure it gets removed when the var is removed
    protected void syncAll() {
        if (LOG_SYNC_ALLS) {
            if (counter == 0) {
                startTime = System.currentTimeMillis();
            }
            counter++;
            System.out.println("SyncAll: " + counter);
        }
        super.syncAll();
        if (LOG_SYNC_ALLS) {
            System.out.println("   done SyncAll: time=" + (System.currentTimeMillis() - startTime));
        }
    }

    
}
