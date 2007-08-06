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
package org.netbeans.modules.visualweb.insync;

import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.visualweb.classloaderprovider.CommonClassloaderProvider;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.netbeans.modules.visualweb.insync.java.ReadTaskWrapper;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
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
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.OperationEvent;
import org.openide.loaders.OperationListener;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.Lookup.Result;

//NB60 import org.netbeans.modules.visualweb.insync.faces.refactoring.MdrInSyncSynchronizer;

/**
 * A ModelSet is a collection of Models that are organized together in a single project. The
 * ModelSet serves to coordinate the Models with each other and with the containing project. <p/>
 * The Models are divided into two groups: regular or source Models with many instances, and
 * specific configuration Models with one instance per type. <p/>There is always exactly one
 * ModelSet per project. <p/>
 *
 * @author cquinn
 */
public abstract class ModelSet implements FileChangeListener {
    
    protected static class OpenProjectsListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent event) {
            // The list of open projects has changed, clean up any old projects I may be holding
            // on to.
            // TODO: Should we look for opened ones as well, dont think so, better to create it
            // when needed ?
            if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(event.getPropertyName())) {
                Project[] openProjectsArray = OpenProjects.getDefault().getOpenProjects();
                IdentityHashMap openProjects = new IdentityHashMap();
                for (int i=0; i < openProjectsArray.length; i++)
                    openProjects.put(openProjectsArray[i], openProjectsArray[i]);
                ArrayList toRemove = new ArrayList();
                synchronized (sets) {
                    for (Iterator i=sets.keySet().iterator(); i.hasNext(); ) {
                        Project project = (Project) i.next();
                        if (!openProjects.containsKey(project)) {
                            ModelSet modelSet = (ModelSet) sets.get(project);
                            toRemove.add(modelSet);
                        }
                    }
                }
                for (Iterator i=toRemove.iterator(); i.hasNext(); ) {
                    ModelSet modelSet = (ModelSet) i.next();
                    modelSet.destroy();
                    fireModelSetRemoved(modelSet);
                }
            }
        }
        
    }
    
    // The following code was cloned from WriteLockUtils.hasActiveLockFileSigns
    // When NB fixes ISSUE #59514, we can get rid of this code
    static final String WriteLock_PREFIX = ".LCK"; //NOI18N    
    static final String WriteLock_SUFFIX = "~"; //NOI18N

    public static boolean hasActiveLockFileSigns(FileObject fileObject) {
        String name = fileObject.getNameExt();
        boolean hasSigns = name.endsWith(WriteLock_SUFFIX) && name.startsWith(WriteLock_PREFIX);
        return hasSigns;
    }

    static {
//        WindowManager.getDefault().getRegistry().addPropertyChangeListener(new WindowManagerPropertyRegistry());
        OpenProjects.getDefault().addPropertyChangeListener(new OpenProjectsListener());
    }

    protected static ArrayList modelSetsListeners = new ArrayList();
    
    public static void addModelSetsListener(ModelSetsListener listener) {
        modelSetsListeners.add(listener);
    }
    
    public static void removeModelSetsListener(ModelSetsListener listener) {
        modelSetsListeners.remove(listener);
    }
    
    protected static void fireModelSetAdded(ModelSet modelSet) {
        // !EAT
        // How to have an iteration safe collection ?
        // I think there may be a better way to do dependencies ?
        Object[] listeners = modelSetsListeners.toArray();
        for (int i = 0; i < listeners.length; i++) {
            ModelSetsListener listener = (ModelSetsListener) listeners[i];
            listener.modelSetAdded(modelSet);
        }
    }
    
    protected static void fireModelSetRemoved(ModelSet modelSet) {
        // !EAT
        // How to have an iteration safe collection ?
        // I think there may be a better way to do dependencies ?
        Object[] listeners = modelSetsListeners.toArray();
        for (int i = 0; i < listeners.length; i++) {
            ModelSetsListener listener = (ModelSetsListener) listeners[i];
            listener.modelSetRemoved(modelSet);
        }
    }
    
    //------------------------------------------------------------------------------ Model Factories

    protected static IdentityHashMap sets = new IdentityHashMap();

    protected static java.util.Collection getFactories() {
        return Lookup.getDefault().lookup(new Lookup.Template(Model.Factory.class)).allInstances();
    }
    
    private static Map<Project, Runnable> projectToRunnable = new HashMap<Project, Runnable>();

    protected static ModelSet startModeling(final Project project, final Class ofType) {
        if (project == null)
            return null;
        ModelSet set = getModelSet(project);
        if (set == null) {
        	synchronized (projectToRunnable) {
				Runnable modelingRunnable = projectToRunnable.get(project);
				if (modelingRunnable == null) {
					modelingRunnable = new Runnable() {
		                public void run() {
		                	try {
			                    getInstance(project, ofType);			                    
		                	} finally {
		                		synchronized (projectToRunnable) {
		                			projectToRunnable.remove(project);
		                		}
		                	}
		                }};
	                projectToRunnable.put(project, modelingRunnable);
		            new Thread(modelingRunnable, "Loading ModelSet for " + project.getProjectDirectory().getName()).start(); // NOI18N
				}
			}        	
        } else {
        	return set;
        }
        return null;
    }
    
    protected static ModelSet getInstance(FileObject file, Class ofType) {
        Project project = FileOwnerQuery.getOwner(file);
        return getInstance(project, ofType);
    }

    /**
     * Helper method for sub-classes to be able to get access to a ModelSet of their own specific type.
     * @param project
     * @return
     */
    synchronized protected static ModelSet getInstance(final Project project, final Class ofType) {
        if (project == null)
            return null;
        ModelSet set = null;
        synchronized (sets) {
            set = (ModelSet) sets.get(project);
        }
        if (set == null && ofType != null) {
        	FileObject sourceRootFileObject = JsfProjectUtils.getSourceRoot(project);
            Enumeration<? extends FileObject> sourceFileObjects = sourceRootFileObject.getChildren(true);
            FileObject anyJavaFile = null;
            while (sourceFileObjects.hasMoreElements()) {
                FileObject aSourceFileObject = sourceFileObjects.nextElement();
                if (aSourceFileObject.getMIMEType().equals("text/x-java")) { // NOI18N
                    anyJavaFile = aSourceFileObject;
                    break;
                }
            }
            if (anyJavaFile == null) {
                // Can't use wrapper task to ensure background scaning
                // stopped while modeling.
            	set = createInstance(project, ofType);
            } else {
                set = (ModelSet) ReadTaskWrapper.execute(
	                    new ReadTaskWrapper.Read() {
	                        public Object run(CompilationInfo cinfo){
	                        	return createInstance(project, ofType);
	                        }
	                    }, anyJavaFile);             
            }
        }
        return set;
    }
    
    private static ModelSet createInstance(Project project, Class ofType) {
    	try {
            Constructor constructor = ofType.getConstructor(new Class[] {Project.class});
            ModelSet set = (ModelSet) constructor.newInstance(new Object[] {project});
            set.setInitialized();
            // This should be fired after the ModelSet is fully constructed
            // and initial syncall has happened.  
            fireModelSetAdded(set);
            return set;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    protected static ModelSet getModelSet(FileObject file) {
        return getModelSet(FileOwnerQuery.getOwner(file));
    }
    
    protected static ModelSet getModelSet(Project project) {
        ModelSet set = null;
        synchronized (sets) {
            set = (ModelSet)sets.get(project);
        }
        return set;
    }
    
    private boolean initialized;
        
    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized() {
        this.initialized = true;
    }
    
    //--------------------------------------------------------------------------------- Construction

    protected final ClassLoader parentClassLoader;  // classloader to parent the project classloader to
    protected final Project project;
    protected URLClassLoader classLoader;   // current derived project classloader
    protected ClassPath classPath; // needed since we add ourseleves as a dependent
    protected ClassPathListener classPathListener;
    protected FileSystem fileSystem;
    protected Model configModel = null;  // config model

    protected final IdentityHashMap models = new IdentityHashMap();        // general models
    protected final IdentityHashMap modelSetListeners = new IdentityHashMap();

    private final OperationListener operationListener = new ModelSetOperationListener();
    
    /**
     * Construct a ModelSet for a given project.
     * <p>
     * This will throw a <code>RuntimeException</code> if a suitable Common ClassLoader Provider is not found.
     *
     * @param project The project that this ModelSet is to be associated with,
     */
    protected ModelSet(Project project) {
        synchronized (sets) {
            sets.put(project, this);
        }
        this.project = project;
        
        CommonClassloaderProvider commonClassloaderProvider = null;
        
        Properties capabilities = new Properties();
        capabilities.put(CommonClassloaderProvider.J2EE_PLATFORM, JsfProjectUtils.getJ2eePlatformVersion(project));
        Result result = Lookup.getDefault().lookup(new Lookup.Template(CommonClassloaderProvider.class));
        for (Iterator iterator = result.allInstances().iterator(); iterator.hasNext();) {
            CommonClassloaderProvider aCommonClassloaderProvider = (CommonClassloaderProvider) iterator.next();
            if (aCommonClassloaderProvider.isCapableOf(capabilities)) {
                commonClassloaderProvider = aCommonClassloaderProvider;
                break;       		
            }
        }

        if (commonClassloaderProvider == null) {
            throw new RuntimeException("No Common Classloader Provider found."); // TODO I18N
        }
        
        parentClassLoader = commonClassloaderProvider.getClassLoader();
		
        // Run thru all the items and create models for those which are ours
        if (project != null) {
            getProjectClassLoader();
            assert Trace.trace("insync.model", "MS.ModelSet ModelCreateVisitor visiting project items in " + Thread.currentThread());
            ModelCreateVisitor visitor = new ModelCreateVisitor();
            for (Iterator i=getSourceRoots().iterator(); i.hasNext(); ) {
                FileObject root = (FileObject) i.next();
                visitor.traverse(root);
            }
            try {
                fileSystem = project.getProjectDirectory().getFileSystem();
            } catch (FileStateInvalidException e) {
            }
            if (fileSystem != null) {
                fileSystem.addFileChangeListener(this);
            }
        }
        // XXX NB issue #81746.
 	    DataLoaderPool.getDefault().addOperationListener(
 	    (OperationListener)WeakListeners.create(OperationListener.class, operationListener, DataLoaderPool.getDefault()));
    }

    protected List getSourceRoots() {
        ArrayList list = new ArrayList();
        FileObject root = JsfProjectUtils.getDocumentRoot(project);
        if (root != null)
            list.add(root);
        root = JsfProjectUtils.getPageBeanRoot(project);
        if (root != null)
            list.add(root);
        return list;
    }
    
    /**
     * Destroy this ModelSet and all its contained Models and release their resources. This ModelSet
     * and the contained Models must never be used after destroy is called.
     */
    public void destroy() {
        if (fileSystem != null) {
            fileSystem.removeFileChangeListener(this);
        }
        releaseProjectClassLoader();
        Model[] ms = getModels();
        models.clear();
        // Make sure that none of the models are valid, such that if outline or any other view
        // wishes to update, they will get an empty list of contexts
//        for (int i = 0; i < ms.length; i++)
//            ms[i].resetOwner();
        for (int i = 0; i < ms.length; i++)
            ms[i].destroy();

        configModel.destroy();
        
        synchronized (sets) {
            sets.remove(project);
        }
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * Get the project that this ModelSet is associated with.
     * 
     * @return The project that this ModelSet is associated with.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Get the per-project class loader for this ModelSet. The class loader may change as project
     * settings and libraries are changed by the user.
     * 
     * @return The class loader.
     */
    public URLClassLoader getProjectClassLoader() {
        if (classLoader == null) {
            List urls1List = new ArrayList();

			// Add design time jars from COMPLIBS
            LibraryManager libraryManager = LibraryManager.getDefault();
            Library[] libraries = libraryManager.getLibraries();
            
            for (int i = 0; i < libraries.length; i++) {
                Library library = libraries[i];
                if (JsfProjectUtils.hasLibraryReference(project, library, ClassPath.COMPILE)) {
                    // TODO The following hardcoded constants are defined in
                    // org.netbeans.modules.visualweb.project.jsf.libraries.provider.ComponentLibraryTypeProvider
                    // However this class is not part of a public package.
                    if (library.getType().equals("complib")) { // NOI18N
                        List urls = library.getContent("visual-web-designtime");
                        List normalizedUrls = new ArrayList();
                        
                        for (Iterator it = urls.iterator(); it.hasNext();) {
                            URL url = (URL) it.next();
                            FileObject fileObject = URLMapper.findFileObject (url);
                            
                            //file inside library is broken
                            if (fileObject == null)
                                continue;
                            
                            if ("jar".equals(url.getProtocol())) {  //NOI18N
                                fileObject = FileUtil.getArchiveFile (fileObject);
                            }
                            File f = FileUtil.toFile(fileObject);                            
                            if (f != null) {
                                try {
                                    URL entry = f.toURI().toURL();
                                    if (FileUtil.isArchiveFile(entry)) {
                                        entry = FileUtil.getArchiveRoot(entry);
                                    } else if (!f.exists()) {
                                        // if file does not exist (e.g. build/classes folder
                                        // was not created yet) then corresponding File will
                                        // not be ended with slash. Fix that.
                                        assert !entry.toExternalForm().endsWith("/") : f; // NOI18N
                                        entry = new URL(entry.toExternalForm() + "/"); // NOI18N
                                    }
                                    normalizedUrls.add(entry);
                                } catch (MalformedURLException mue) {
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue);
                                }
                            }
                        }
                        urls1List.addAll(normalizedUrls); // NOI18N
                    }
                }
            }
            
            // !EAT TODO: Is this really the correct way to build the class loader ???
            FileObject pageBeanRoot = JsfProjectUtils.getPageBeanRoot(project);
            classPath = ClassPath.getClassPath(pageBeanRoot, ClassPath.COMPILE);
            FileObject docRoot = JsfProjectUtils.getDocumentRoot(project);
            URLClassLoader projectClassLoader = (URLClassLoader) classPath.getClassLoader(true);
            URL urls[] = projectClassLoader.getURLs();
            
            urls1List.addAll(Arrays.asList(urls));
            
            //Add <project>\build\web\WEB-INF\classes directory into project 
            //classloader classpath.
            //TODO: We need to consider a better approach to achieve this 
            //This may not be required if insync models all the source code
            File docPath = FileUtil.toFile(docRoot);
            File buildClassPath = new File(docPath.getParentFile(), "build" + File.separator + 
                    "web" + File.separator + "WEB-INF" + File.separator + "classes");
            URL buildClassURL = null;
            try {
                buildClassURL = buildClassPath.toURI().toURL();
                urls1List.add(buildClassURL);
            } catch(MalformedURLException mue) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue);
            }
            
            URL [] urls1 = (URL[]) urls1List.toArray(new URL[0]);
                
//            classLoader = new URLClassLoader(urls1, parentClassLoader);
            classLoader = new ProjectClassLoader(urls1, parentClassLoader);
            classPathListener = new ClassPathListener();
            classPath.addPropertyChangeListener(classPathListener);
        }
        return classLoader;
    }
    
    // XXX To be able to distinguish our specific project classloader during debugging.
    private static class ProjectClassLoader extends URLClassLoader {
        private final URL[] urls;
        
        public ProjectClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
            this.urls = urls;
        }
        
        public String toString() {
            return super.toString() + "[urls=" + (urls == null ? null : Arrays.asList(urls)) + "]"; // NOI18N
        }
    }

    /**
     * Get an array of all of the source Models in this set.
     * @return An array of all of the source Models in this set.
     */
    public Model[] getModels() {
        return (Model[]) getModelsMap().values().toArray(Model.EMPTY_ARRAY);
    }
    
    protected Map getModelsMap() {
        return models;
    }

    /**
     * Get the corresponding source model for a NB file object.
     * 
     * @param file  The NB file object
     * @return The corresponding model.
     */
    public Model getModel(FileObject file) {
        Model model = (Model) getModelsMap().get(file);
        return model;
    }
    
    /**
     * Get the configuration model
     * 
     * @return th configuration model.
     */
    public Model getConfigModel() {
        return configModel;
    }
    
    /**
     * Set the configuration model
     * 
     * @return The configuration model.
     */
    public void setConfigModel(Model configModel) {
        this.configModel = configModel;
    }    

    public void addModelSetListener(ModelSetListener listener) {
        modelSetListeners.put(listener, "");
    }
    
    public void removeModelSetListener(ModelSetListener listener) {
        modelSetListeners.remove(listener);
    }
    
    protected void fireModelAdded(Model model) {
        // !EAT
        // How to have an iteration safe collection ?
        // I think there may be a better way to do dependencies ?
        for (Iterator iterator = modelSetListeners.keySet().iterator(); iterator.hasNext(); ) {
            ModelSetListener listener = (ModelSetListener) iterator.next();
            listener.modelAdded(model);
        }
    }
    
    protected void fireModelChanged(Model model) {
        // !EAT
        // How to have an iteration safe collection ?
        // I think there may be a better way to do dependencies ?
        for (Iterator iterator = modelSetListeners.keySet().iterator(); iterator.hasNext(); ) {
            ModelSetListener listener = (ModelSetListener) iterator.next();
            listener.modelChanged(model);
        }
    }
    
    protected void fireModelProjectChanged() {
        // !EAT
        // How to have an iteration safe collection ?
        // I think there may be a better way to do dependencies ?
        for (Iterator iterator = modelSetListeners.keySet().iterator(); iterator.hasNext(); ) {
            ModelSetListener listener = (ModelSetListener) iterator.next();
            listener.modelProjectChanged();
        }
    }
    
    protected void fireModelRemoved(Model model) {
        // !EAT
        // How to have an iteration safe collection ?
        // I think there may be a better way to do dependencies ?
        for (Iterator iterator = modelSetListeners.keySet().iterator(); iterator.hasNext(); ) {
            ModelSetListener listener = (ModelSetListener) iterator.next();
            listener.modelRemoved(model);
        }
    }
    
    //---------------------------------------------------------------------------------------- Model

    /**
     * Add a new file/model pair to the correct map.
     * @param file  The source or config file object
     * @param m  The source or config model
     */
    protected void addModel(FileObject file, Model m) {
        models.put(file, m);
        fireModelAdded(m);
    }
    
    private Set modelsToSync;
    
    void addToModelsToSync(Model model) {
        if (modelsToSync == null) {
            modelsToSync = new HashSet();
        }
        modelsToSync.add(model);
    }
    
    public void removeFromModelsToSync(Model model) {
        if (modelsToSync != null) {
            modelsToSync.remove(model);
        }
    }

    /**
     * Synchronize all source and config models with their underlying buffers.
     */
    protected void syncAll() {
        ArrayList errorAccumulator = new ArrayList();
        if (modelsToSync == null) {
            modelsToSync = new HashSet();
            // Due to the fact that there is some resetting of errors and such happening on each sync,
            // we need to gather up the errors and present them at the end
            Model model = configModel;
            if (model.isValid()) {
                ParserAnnotation[] errors = model.getErrors();
                if (errors.length > 0) {
                    for (int j=0, max=errors.length; j < max; j++) {
                        errorAccumulator.add(errors[j]);
                    }
                }
            }
            
            Collection orderedModels = evalOrderModels(getModelsMap().values());
            for (Iterator i = orderedModels.iterator(); i.hasNext(); ) {
                model = (Model)i.next();
                if (model.isValid()) {
                    model.sync();
                    ParserAnnotation[] errors = model.getErrors();
                    if (errors.length > 0) {
                        for (int j=0, max=errors.length; j < max; j++) {
                            errorAccumulator.add(errors[j]);
                        }
                    }
                }
            }
        }else {
            for (Iterator it = modelsToSync.iterator(); it.hasNext();) {
                Model model = (Model)it.next();
                it.remove();
                model.sync();
                ParserAnnotation[] errors = model.getErrors();
                if (errors.length > 0) {
                    for (int j=0, max=errors.length; j < max; j++) {
                        errorAccumulator.add(errors[j]);
                    }
                }
            }
        }
        if (errorAccumulator.size() > 0) {
            showSyncErrors(errorAccumulator, true);
        }
    }

    protected void releaseProjectClassLoader() {
        if (classLoader != null) {
            classLoader = null;
        }
        if (classPath != null) {
            classPath.removePropertyChangeListener(classPathListener);
            classPathListener = null;
            classPath = null;
        }
    }
    
    protected void showSyncErrors(ArrayList errors, boolean printPreface) {
        if (errors.size() ==0)
            return;
        if (printPreface) {
            InSyncServiceProvider.get().getRaveErrorHandler().displayError(
                    NbBundle.getMessage(ModelSet.class, "TXT_ErrorsOnOpenProject1")); // NOI18N
            InSyncServiceProvider.get().getRaveErrorHandler().displayError(
                    NbBundle.getMessage(ModelSet.class, "TXT_ErrorsOnOpenProject2")); // NOI18N
        }
        for (Iterator i = errors.iterator(); i.hasNext();) {
            StringBuffer sb = new StringBuffer(200);
            final ParserAnnotation err = (ParserAnnotation) i.next();
            // TODO We should find out why err.getFileObject() returns null, but Tor needs this to fix bug 6349268, even though I could
            // not reproduce to identify the source of the null :(
            if (err.getFileObject() == null) {
                sb.append("unknown");
            } else {
                sb.append(err.getFileObject().getNameExt());
            }
            sb.append(':');
            sb.append(err.getLine());
            sb.append(':');
            sb.append(err.getColumn());
            sb.append(':');
            sb.append(' ');
            sb.append(err.getMessage());
//            // XXX Todo - add output listener suitable for this location
//            OutputListener listener = new OutputListener() {
//                    public void outputLineSelected(OutputEvent ev) {
//                    }
//                    public void outputLineAction(OutputEvent ev) {
//                        // <markup_separation>
////                        Util.show(null, err.getFileObject(), err.getLine(),
////                                  0, true);
//                        // ====
//                        MarkupService.show(err.getFileObject(), err.getLine(), 0, true);
//                        // </markup_separation>
//                    }
//                    public void outputLineCleared (OutputEvent ev) {
//                    }
//                };
//            MarkupService.displayError(sb.toString(), listener);
            InSyncServiceProvider.get().getRaveErrorHandler().displayErrorForFileObject(sb.toString(), err.getFileObject(), err.getLine(), err.getColumn());
        }
        InSyncServiceProvider.get().getRaveErrorHandler().selectErrors();
    }

    /**
     * Flush all source and config models to their underlying buffers.
     */
    protected void flushAll() {
        for (Iterator i = getModelsMap().values().iterator(); i.hasNext(); )
            ((Model)i.next()).flush();
    }
    
    /**
     * Save all source and config models to their underlying buffers.
     */
    protected void saveAll() {
        for (Iterator i = getModelsMap().values().iterator(); i.hasNext(); )
            ((Model)i.next()).saveUnits();
    }
    

    class ClassPathListener  implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            classPathChanged();
        }
    }
    
    class ModelCreateVisitor extends FileObjectVisitor {
        protected ArrayList modelsAdded = new ArrayList();
        
        protected void visitImpl(FileObject file) {
            if (!models.containsKey(file)) {
                for (Iterator i = getFactories().iterator(); i.hasNext(); ) {
                    Model.Factory mf = (Model.Factory)i.next();
                    Model m = mf.newInstance(ModelSet.this, file);
                    if (m != null) {
                        //FacesModel may be created because of a java file during
                        //CVS update, therefore use primary file from the model
                        //to add into models map
                        addModel(m.getFile(), m);
                        modelsAdded.add(m);
                        break;
                    }
                }
            }
        }
        
        public List getModelsAdded() {
            return modelsAdded;
        }
    }

    /**
     * Respond to changes in the project class path by updating the models.
     *
     * @see com.sun.rave.project.model.ProjectContentChangeListener#classPathChanged(com.sun.rave.project.model.ProjectContentChangeEvent)
     */
    public void classPathChanged() {
        releaseProjectClassLoader();
        getProjectClassLoader();
    }   

    /**
     * Provide models in such a way as to cause higher scoped models to be ordered first.
     * This only works if we assume that lower scoped models references values from higher scoped models.
     * If higher scoped models references lower ones, then this will not help much.
     * 
     * @param modelsToOrder
     * @return
     */
    protected Collection evalOrderModels(Collection modelsToOrder) {
        return modelsToOrder;
    }
    
    protected FileObject getLocalFileObject(FileObject fileObject) {
        if(fileObject == null) {
            return null;
        }
        
        // What does virtual actually mean, should I handle these as well ?
        if (fileObject.isVirtual())
            return null;

        File file = FileUtil.toFile(fileObject);
        if (file == null) {
            return null;
        }
        //Check if the file is non sharable
        if (SharabilityQuery.getSharability(file) ==
                SharabilityQuery.NOT_SHARABLE) {
            return null;
        }
        if (hasActiveLockFileSigns(fileObject)) {
            return null;
        }
        FileObject projectRoot = getProject().getProjectDirectory();
        if (projectRoot == null || !FileUtil.isParentOf(projectRoot, fileObject)) {
            return null;
        }
        return fileObject;
    }

    public void fileAttributeChanged (FileAttributeEvent fe) {
    }
    
    public void fileFolderCreated (FileEvent fe) {
        // Dont really care about folders do we ?
    }

    public void fileChanged (FileEvent fe) {
        // Do we need to listen to these, dont think so ?
    }

    public void fileDataCreated(FileEvent fe) {
        FileObject fileObject = getLocalFileObject(fe.getFile());
        if (fileObject == null){
            return;
        }
        
        if (fileObject.getAttribute("NBIssue81746Workaround") == Boolean.TRUE) { // NOI18N
            try {
                fileObject.setAttribute("NBIssue81746Workaround", null); // NOI18N
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
            }
            // XXX NB issue #81746
            // This will be handled in the createFromTemplate listener, see the issue.
            return;
        }
        
        processFileDataCreated(fileObject);
    }
    
    private FileObject getOurFileObject(FileObject fileObject) {
        fileObject = getLocalFileObject(fileObject);
        if (fileObject == null)
            return null;
        // we should create Model only if the file is under document root or source root
        if (!FileUtil.isParentOf(JsfProjectUtils.getDocumentRoot(getProject()), fileObject) &&
                !FileUtil.isParentOf(JsfProjectUtils.getSourceRoot(getProject()), fileObject)) {
            return null;
        }
        return fileObject;
    }
    
    // XXX NB issue #
    private void processFileDataCreated(final FileObject fileObject) {  
        // we should create Model only if the file is under document root or source root
        if (!FileUtil.isParentOf(JsfProjectUtils.getDocumentRoot(getProject()), fileObject) &&
               !FileUtil.isParentOf(JsfProjectUtils.getSourceRoot(getProject()), fileObject)) {
            return;
        }
        
        // Do this outside of refactoring session, as we cannot guarantee when the Java or the JSP file will be
        // "added", this way we wait until everything is done and we have all the files already moved prior
        // to building the models
/*//NB6.0
        MdrInSyncSynchronizer.get().doOutsideOfRefactoringSession(new Runnable() {
            public void run() {
 */
        ModelCreateVisitor visitor = new ModelCreateVisitor();
        visitor.visit(fileObject);
        Collection modelsAdded = visitor.getModelsAdded();
        for (Iterator i = modelsAdded.iterator(); i.hasNext(); ) {
            Model model = (Model) i.next();
            // We do a sync here to make sure that the model REALLY is a valid one
            // The visitor above can create models that should not really be models
            // but we can only find out once we perform a sync.  If as a result
            // of the sync, the model has no owner, this indicates that sync destroy'ed
            // the model and that it should not be a model after all
            FileObject file = model.getFile();
            try {
                //Set an attribute to indicate the file is newly created which is
                //used to decide the addition of cross referencing accessors
                file.setAttribute("NewFile", Boolean.TRUE); //NOI18N
                model.sync();
            }catch(IOException ioe) {
                assert Trace.trace("insync.model", "Failed to set the attribute: " + model.getFile());  //NOI18N
            }finally {
                try {
                    file.setAttribute("NewFile", null); //NOI18N
                }catch(IOException ioe) {
                    assert Trace.trace("insync.model", "Failed to reset the attribute: " + model.getFile());  //NOI18N
                }
            }
            if (model.isValid()) {
                model.saveUnits();
            } else {
                models.remove(file);
                model = null;
            }
        }
/*
            }
        });
//*/
    }
    
    public void fileDeleted (final FileEvent event) {
    }
    
    public void fileRenamed(final FileRenameEvent event) {
        final FileObject fileObject = event.getFile();
        final String oldName = event.getName();
        final String newName = fileObject.getName();
        final String extension = fileObject.getExt();
        //If the file is renamed to non sharable file(for example during cvs conflicts)
        //it is necessary to remove the model
        final boolean needToRemove = getLocalFileObject(fileObject) == null ? true : false;
        final Model[] models = getModels();

        for (int i=0; i < models.length; i++) {
            Model model = models[i];
            model.fileRenamed(oldName, newName, extension, fileObject, needToRemove);
        }
    }
    
    private /*static*/ class ModelSetOperationListener implements OperationListener {
        public void operationPostCreate(OperationEvent ev) {}
        
        public void operationCopy(OperationEvent.Copy ev) {}
        
        public void operationMove(OperationEvent.Move ev) {}
        
        public void operationDelete(OperationEvent ev) {
            final FileObject fileObject = getLocalFileObject(ev.getObject().getPrimaryFile());
            if (fileObject == null) {
                return;
            }
            
            final Model model = getModel(fileObject);
            if (model != null) {                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ModelSet modelSet = model.getOwner();
                        if (modelSet != null) {
                            modelSet.removeModel(model);
                        }
                    }
                });
            }
        }
        
        public void operationRename(OperationEvent.Rename ev) {}
        
        public void operationCreateShadow(OperationEvent.Copy ev) {}
        
        public void operationCreateFromTemplate(OperationEvent.Copy ev) {
            FileObject fileObject = getOurFileObject(ev.getObject().getPrimaryFile());
            if (fileObject == null) {
                return;
            }
            
            // XXX NB issue #81746.
            processFileDataCreated(fileObject);
        }
    } // End of ModelSetOperationListener.

    public void removeModel(Model model) {
        FileObject fileObject = model.getFile();
        models.remove(fileObject);
        fireModelRemoved(model);
        model.destroy();
    }

}
