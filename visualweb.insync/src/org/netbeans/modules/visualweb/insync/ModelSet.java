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
package org.netbeans.modules.visualweb.insync;

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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.visualweb.classloaderprovider.CommonClassloaderProvider;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
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
        if (set != null) {
            return set;
        }
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
                    }
                };
                projectToRunnable.put(project, modelingRunnable);
                new Thread(modelingRunnable, "Loading ModelSet for " + project.getProjectDirectory().getName()).start(); // NOI18N
            }
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
            set = createInstance(project, ofType);
            if(set != null) {
                synchronized (sets) {
                    sets.put(project, set);
                }
                fireModelSetAdded(set);
            }
        }
       
        return set;
    }
    
    private static ModelSet createInstance(Project project, Class ofType) {
    	try {
            Constructor constructor = ofType.getConstructor(new Class[] {Project.class});
            ModelSet set = (ModelSet) constructor.newInstance(new Object[] {project});
            set.setInitialized();
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

    
    private static final String WEBSERVICE_CLIENTS_SUB_DIR = "webservice_clients"; // NOI18N
    private static final String EJB_DATA_SUB_DIR = "ejb-sources"; // NOI18N
    
    /**
     * Get the per-project class loader for this ModelSet. The class loader may change as project
     * settings and libraries are changed by the user.
     * 
     * @return The class loader.
     */
    public URLClassLoader getProjectClassLoader() {
        if (classLoader == null) {
            Set<URL> urlSet = new LinkedHashSet<URL>();

            // Add design time and run time jars from COMPLIBS and themes
            LibraryManager libraryManager = JsfProjectUtils.getProjectLibraryManager(project);
            for (Library library : libraryManager.getLibraries()) {
                if (library.getType().equals("complib") &&  // NOI18N
                        JsfProjectUtils.hasLibraryReference(project, library, ClassPath.COMPILE)) {
                    urlSet.addAll(library.getContent("visual-web-designtime")); // NOI18N
                }
            }
            
            // Special handling of jaxrpc16Library (J2EE 1.3 and J2EE 1.4)
            // For JAVA EE 5 projects the jaxws APIs are available thorugh a module dependency in
            // org.netbeans.modules.visualweb.j2ee15classloaderprovider module
            String platformVersion = JsfProjectUtils.getJ2eePlatformVersion(project);
            if (platformVersion.equals(JsfProjectUtils.J2EE_1_3) ||
                    platformVersion.equals(JsfProjectUtils.J2EE_1_4)) {
                    FileObject wsClientsSubDir = project.getProjectDirectory().getFileObject(
                        JsfProjectConstants.PATH_LIBRARIES + '/'+ WEBSERVICE_CLIENTS_SUB_DIR);
                    if (wsClientsSubDir != null && hasJarFiles(wsClientsSubDir)) {
                        Library jaxrpc16Library = getJAXRPCLibrary(libraryManager);
                        // Hmmm...project does not have a reference to the jaxrpc16 
                        // library so we need to get it from global library manager                    
                        if (jaxrpc16Library == null) {
                            jaxrpc16Library = getJAXRPCLibrary(LibraryManager.getDefault());
                        }
                        if (jaxrpc16Library != null) {
                            // Add the jars from jaxrpc16Library
                            urlSet.addAll(jaxrpc16Library.getContent("classpath")); // NOI18N
                        }
                    }
            }
            
            // Check if project uses ejbs.
            boolean hasEjbClients = false;
            FileObject ejbClientsSubDir = project.getProjectDirectory().getFileObject(
                    JsfProjectConstants.PATH_LIBRARIES + '/'+ EJB_DATA_SUB_DIR);
            hasEjbClients = (ejbClientsSubDir != null && hasJarFiles(ejbClientsSubDir));
            
            //"classpath/packaged" gives us all the jars excluding app server jars in case of maven project
            //It is not known if such a classpath will be supported in case of web project, therefore we remove
            //the app server jars ourselves
            classPath = ClassPath.getClassPath(JsfProjectUtils.getPageBeanRoot(project), "classpath/packaged"); // NOI18N
            if(classPath == null) {
                classPath = ClassPath.getClassPath(JsfProjectUtils.getPageBeanRoot(project), ClassPath.COMPILE);
                URLClassLoader classPathClassLoader = (URLClassLoader) classPath.getClassLoader(true);
                URL urls[] = classPathClassLoader.getURLs();
                urlSet.addAll(Arrays.asList(urls));                
                //Remove the J2ee Classpath jars from the classpath to improve design time performance
                String[] j2eeJars = JsfProjectUtils.getJ2eeClasspathEntries(project);
                List<String> j2eeJarsList = Arrays.asList(j2eeJars);
                for (URL url : urls) {
                    URL fileURL = FileUtil.getArchiveFile(url);
                    FileObject fileObj = URLMapper.findFileObject(fileURL != null ? fileURL : url);
                    if (fileObj != null) {
                        File file = FileUtil.toFile(fileObj);
                        if (file == null) {
                            continue;
                        }
                        if (hasEjbClients) {
                            if (file.isFile() && file.getName().endsWith(".jar")) {
                                try {
                                    JarFile jarFile = new JarFile(file);
                                    try {
                                        // Found one of the ejb20 classes - use this jar file
                                        if (jarFile.getEntry("javax/ejb/CreateException.class") != null) {
                                            // We need to keep this jar in designtime classpath
                                            continue;                                            
                                        }
                                    } finally {
                                        jarFile.close();
                                    }
                                } catch (IOException e) {
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                                } 
                            }
                        }
                        // Is this a URL from J2ee Classpath
                        if (j2eeJarsList.contains(file.getAbsolutePath())) {
                            // Remove it from designtime classpath
                            urlSet.remove(url);                            
                        }
                    }
                }
            }else {
                for(ClassPath.Entry entry: classPath.entries()) {
                    urlSet.add(entry.getURL());
                }
            }
            
            //Add <project>\build\web\WEB-INF\classes directory into project 
            //classloader classpath.
            //TODO: We need to consider a better approach to achieve this 
            //This may not be required if insync models all the source code
            FileObject docRoot = JsfProjectUtils.getDocumentRoot(project);
            File docPath = FileUtil.toFile(docRoot);
            File buildClassPath = new File(docPath.getParentFile(), "build" + File.separator + // NOI18N
                    "web" + File.separator + "WEB-INF" + File.separator + "classes"); // NOI18N
            URL buildClassURL = null;
            try {
                buildClassURL = buildClassPath.toURI().toURL();
                urlSet.add(buildClassURL);
            } catch(MalformedURLException mue) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue);
            }
            
            classLoader = new ProjectClassLoader(normalizeURLs(urlSet), parentClassLoader);
            classPathListener = new ClassPathListener();
            classPath.addPropertyChangeListener(classPathListener);
        }
        return classLoader;
    }
    
    private Library getJAXRPCLibrary(LibraryManager libraryManager) {
        for (Library library : libraryManager.getLibraries()) {
            if (library.getType().equals("j2se")) { // NOI18N
                if (library.getName().equals("jaxrpc16")) { // NOI18N
                    return library;
                }
            }
        }
        return null;
    }
    
    private static URL[] normalizeURLs(Collection<URL> urls) {
        List<URL> urlList = new LinkedList<URL>();
        for(URL url : urls) {
            FileObject fileObject = URLMapper.findFileObject(url);

            //file inside library is broken
            if (fileObject != null) {
                if ("jar".equals(url.getProtocol())) {  //NOI18N
                    fileObject = FileUtil.getArchiveFile(fileObject);
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
                        urlList.add(entry);
                        continue;
                    } catch (MalformedURLException mue) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue);
                    }
                }
            }
            urlList.add(url);
        }
        return (URL[])urlList.toArray(new URL[0]);
    }
    
    private static boolean hasJarFiles(FileObject folder) {
        Enumeration<? extends FileObject> children = folder.getChildren(true);
        while (children.hasMoreElements()) {
            FileObject child = children.nextElement();
            if (child.isData() && child.getExt().equals("jar")) {
                return true;
            }
        }
        return false;
    }
    
    // XXX To be able to distinguish our specific project classloader during debugging.
    private static class ProjectClassLoader extends URLClassLoader {
        private final URL[] urls;
        
        // Memory leak probing
        private static final Logger TIMERS = Logger.getLogger("TIMER.visualweb"); // NOI18N
        
        public ProjectClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
            this.urls = urls;
            
            if (TIMERS.isLoggable(Level.FINER)) {
                LogRecord rec = new LogRecord(Level.FINER, "ModelSet$ProjectClassLoader"); // NOI18N
                rec.setParameters(new Object[]{ this });
                TIMERS.log(rec);
            }
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
    public void processFileDataCreated(final FileObject fileObject) {  
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
        if (fileObject.isFolder()) {
            visitor.traverse(fileObject);
        } else {
            visitor.visit(fileObject);            
        }
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
