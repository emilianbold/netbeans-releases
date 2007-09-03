/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelimpl.platform;

import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.modelimpl.cache.CacheManager;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;
import java.util.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;

import org.netbeans.api.project.*;
import org.netbeans.api.project.ui.OpenProjects;

import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryEvent;
import org.netbeans.modules.cnd.modelimpl.options.CodeAssistanceOptions;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.spi.LowMemoryAlerter;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author Vladimir Kvashin
 */
public class ModelSupport implements PropertyChangeListener {
    
    private static ModelSupport instance = new ModelSupport();
    
    private ModelImpl model;
    
    private Set<Project> openedProjects = new HashSet<Project>();
    
    private Set sourceExtensions = new TreeSet();
    
    private FileChangeListener modifiedListener;

    private static final boolean TRACE_STARTUP = false;
    private volatile boolean postponeParse = false;
    
    private ModelSupport() {
        modifiedListener = new FileChangeListener();
        DataObject.getRegistry().addChangeListener(modifiedListener);
    }
    
    public static ModelSupport instance() {
//        if( instance == null ) {
//            instance = new ModelSupport();
//        }
        return instance;
    }
    
    public static int getTabSize() {
        return 8;
    }
    
    public File locateFile(String fileName) {
        InstalledFileLocator locator = InstalledFileLocator.getDefault();
        if( locator != null ) {
            File file = locator.locate(fileName, "com.sun.tools.swdev.parser.impl/1", false); // NOI18N
            if( file != null ) {
                return file;
            }
        }
        // the above code is mostly for debugging purposes;
        // but seems it didn't spoil anything :)
        File file = new File(fileName);
        return file.exists() ? file : null;
    }
    
    public void init(ModelImpl model) {
        
        Collection<? extends CsmProgressListener> listeners = Lookup.getDefault().lookupAll(CsmProgressListener.class);
        for (CsmProgressListener csmProgressListener : listeners) {
            model.addProgressListener(csmProgressListener);
        }
        
        //CodeModelRequestProcessor.instance().post(ProjectListenerThread.instance(), "Project Listener");
        this.model = model;
        
        openedProjects = new HashSet<Project>();
        if (TRACE_STARTUP) System.out.println("Model support: Inited"); // NOI18N

        if (TopComponent.getRegistry().getOpened().size() > 0){
            if (TRACE_STARTUP) System.out.println("Model support: Open projects in Init"); // NOI18N
            postponeParse = false;
            openProjects();
        } else {
            if (TRACE_STARTUP) System.out.println("Model support: Postpone open projects"); // NOI18N
            postponeParse = true;
        }
        
        TopComponent.getRegistry().addPropertyChangeListener(this);
        OpenProjects.getDefault().addPropertyChangeListener(this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
	try { //FIXUP #109105 OpenProjectList does not get notification about adding a project if the project is stored in the repository
	    if (TRACE_STARTUP) System.out.println("Model support event:"+evt.getPropertyName());
	    if(!postponeParse  && evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS) ) {
		if (TRACE_STARTUP) System.out.println("Model support: Open projects on OpenProjects.PROPERTY_OPEN_PROJECTS"); // NOI18N
		openProjects();
	    } else if (postponeParse && evt.getPropertyName().equals(TopComponent.Registry.PROP_ACTIVATED)){
		if (TRACE_STARTUP) System.out.println("Model support: Open projects on TopComponent.Registry.PROP_ACTIVATED"); // NOI18N
		postponeParse = false;
		TopComponent.getRegistry().removePropertyChangeListener(this);
		RequestProcessor.getDefault().post(new Runnable(){
		    public void run() {
			openProjects();
		    }
		});
	    }
	}
	catch( Exception e) {
	    e.printStackTrace(System.err); 
	}
    }
    
    private void openProjects() {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        
        synchronized (openedProjects){
            Set<Project> nowOpened = new HashSet<Project>();
            for( int i = 0; i < projects.length; i++ ) {
                nowOpened.add(projects[i]);
                if( ! openedProjects.contains(projects[i]) ) {
                    addProject(projects[i]);
                }
            }
            
            Set<Project> toClose = new HashSet<Project>();
            for (Project project : openedProjects) {
                if( ! nowOpened.contains(project) ) {
                    toClose.add(project);
                }                
            }
            
            for (Project project : toClose) {
                closeProject(project);
            }
        }
    }

    public boolean needParseOrphan(final Object platformProject) {
        if (!ModelImpl.isStandalone()) {
            Project project = ModelImpl.findProjectByNativeProject(instance().getNativeProject(platformProject));
            if (project != null) {
                return new CodeAssistanceOptions(project,true).getParseOrphanEnabled().booleanValue();
            }
        }
        return true;
    }
    
    private NativeProjectItemsListener projectItemListener = new NativeProjectItemsListener() {
        public void fileAdded(NativeFileItem fileItem) {
            onProjectItemAdded(fileItem);
        }
        
        public void filesAdded(List<NativeFileItem> fileItems) {
            for (List<NativeFileItem> list : divideByProjects(fileItems)){
                onProjectItemAdded(list);
            }
        }
        
        public void fileRemoved(NativeFileItem fileItem) {
            onProjectItemRemoved(fileItem);
        }
        
        public void filesRemoved(List<NativeFileItem> fileItems) {
            for (List<NativeFileItem> list : divideByProjects(fileItems)){
                onProjectItemRemoved(list);
            }
        }
    
        public void fileRenamed(String oldPath, NativeFileItem newFileIetm){
            onProjectItemRenamed(oldPath, newFileIetm);
        }
        
        public void filePropertiesChanged(NativeFileItem fileItem) {
            onProjectItemChanged(fileItem);
        }
        
        public void filesPropertiesChanged(final List<NativeFileItem> fileItems) {
	    // FIXUP for #109425
	    CsmModelAccessor.getModel().enqueue(new Runnable() {
		public void run() {
		    for (List<NativeFileItem> list : divideByProjects(fileItems)){
			onProjectItemChanged(list);
		    }
		}
	    }, "Applying property changes"); // NOI18N
	    
        }
        
        public void filesPropertiesChanged() {
	    // FIXUP for #109425
	    CsmModelAccessor.getModel().enqueue(new Runnable() {
		public void run() {
		    for(NativeProject project : getNativeProjects()){
			filesPropertiesChanged(project.getAllSourceFiles());
		    }
		}
	    }, "Applying property changes"); // NOI18N
        }
	
	public void projectDeleted(NativeProject nativeProject) {
	    RepositoryUtils.onProjectDeleted(nativeProject);
	}
        
        private Collection<List<NativeFileItem>> divideByProjects(List<NativeFileItem> fileItems){
            Map<NativeProject,List<NativeFileItem>> res = new HashMap<NativeProject,List<NativeFileItem>>();
            for(NativeFileItem item : fileItems){
                NativeProject nativeProject = item.getNativeProject();
                if (nativeProject != null){
                    List<NativeFileItem> list = res.get(nativeProject);
                    if (list == null){
                        list =new ArrayList<NativeFileItem>();
                        res.put(nativeProject,list);
                    }
                    list.add(item);
                }
            }
            return res.values();
        }
    };
    
    protected void onProjectItemAdded(final NativeFileItem item) {
        try {
            final ProjectBase project = getProject(item, true);
            if( project != null ) {
                project.onFileAdded(item);
            }
        } catch( Exception e ) {
            e.printStackTrace(System.err);
        }
    }
    
    protected void onProjectItemAdded(final List<NativeFileItem> items) {
        if (items.size()>0){
            try {
                final ProjectBase project = getProject(items.get(0), true);
                if( project != null ) {
                    project.onFileAdded(items);
                }
            } catch( Exception e ) {
                e.printStackTrace(System.err);
            }
        }
    }
    
    protected void onProjectItemRemoved(final NativeFileItem item) {
        try {
            final ProjectBase project = getProject(item, false);
            if( project != null ) {
                File file = item.getFile();
                project.onFileRemoved(file);
            }
        } catch( Exception e ) {
            //TODO: FIX (most likely in Makeproject: path == null in this situation,
            //this cause NPE
            e.printStackTrace(System.err);
        }
    }
    
    protected void onProjectItemRemoved(final List<NativeFileItem> items) {
        if (items.size()>0){
            try {
                final ProjectBase project = getProject(items.get(0), false);
                if( project != null ) {
                    project.onFileRemoved(items);
                }
            } catch( Exception e ) {
                e.printStackTrace(System.err);
            }
        }
    }

    protected void onProjectItemRenamed(String oldPath, NativeFileItem newFileIetm) {
        try {
            final ProjectBase project = getProject(newFileIetm, false);
            if( project != null ) {
                File file = FileUtil.normalizeFile(new File(oldPath));
                project.onFileRemoved(file);
                project.onFileAdded(newFileIetm);
            }
        } catch( Exception e ) {
            //TODO: FIX (most likely in Makeproject: path == null in this situation,
            //this cause NPE
            e.printStackTrace(System.err);
        }
    }
    
    protected void onProjectItemChanged(final NativeFileItem item) {
        // invalidate cache for this file
        if (TraceFlags.USE_AST_CACHE) {
            CacheManager.getInstance().invalidate(item.getFile().getAbsolutePath());
        } else {
            // do not need to invalidate APT, it is preprocessor neutral
        }
        try {
            final ProjectBase project = getProject(item, false);
            if( project != null ) {
                project.onFilePropertyChanged(item);
            }
        } catch( Exception e ) {
            //TODO: FIX (most likely in Makeproject: path == null in this situation,
            //this cause NPE
            e.printStackTrace(System.err);
        }
    }
    
    protected void onProjectItemChanged(final List<NativeFileItem> items) {
        if (items.size()>0){
            try {
                final ProjectBase project = getProject(items.get(0), true);
                if( project != null ) {
                    project.onFilePropertyChanged(items);
//                    try {
//                        ParserQueue.instance().onStartAddingProjectFiles(project);
//                        for(NativeFileItem item : items) {
//                            // TODO: FIX me. This code doesn't work when item include paths are changed!
//                            // onFilePropertyChanged method shouldn't add item at the beginning.
//                            if (TraceFlags.USE_AST_CACHE) {
//                                CacheManager.getInstance().invalidate(item.getFile().getAbsolutePath());
//                            } else {
//                                // do not need to invalidate APT, it is preprocessor neutral
//                            }
//                            project.onFilePropertyChanged(item);
//                            // This is right working code.
//                            //project.onFileRemoved(item);
//                            //project.onFileAdded(item);
//                        }
//                    } finally{
//                        ParserQueue.instance().onEndAddingProjectFiles(project);
//                    }
                }
            } catch( Exception e ) {
                e.printStackTrace(System.err);
            }
        }
    }
    
    private ProjectBase getProject(NativeFileItem nativeFile, boolean createIfNeeded) {
        assert nativeFile != null : "must not be null";
        assert nativeFile.getFile() != null : "must be associated with valid file";
        assert nativeFile.getNativeProject() != null : "must have container project";
        ProjectBase csmProject = null;
        try {
            NativeProject nativeProject = nativeFile.getNativeProject();
	    assert(nativeProject != null) : "NativeFileItem should never return null NativeProject";
            if (nativeProject != null) {
                csmProject = createIfNeeded ? (ProjectBase) model._getProject(nativeProject) :
                    (ProjectBase) model.findProject(nativeProject);
            }
        } catch(NullPointerException ex) {
            ex.printStackTrace();
        }
        return csmProject;
    }
    
    public void trace(NativeFileItem nativeFile) {
        try {
            Diagnostic.trace("  native file item" + nativeFile.getFile().getAbsolutePath()); // NOI18N
            Diagnostic.trace("    user includes: " + nativeFile.getUserIncludePaths()); // NOI18N
            Diagnostic.trace("    user macros: " + nativeFile.getUserMacroDefinitions()); // NOI18N
            Diagnostic.trace("    system includes: " + nativeFile.getSystemIncludePaths()); // NOI18N
            Diagnostic.trace("    system macros: " + nativeFile.getSystemMacroDefinitions()); // NOI18N
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public synchronized void registerProjectListeners(ProjectBase csmProjectImpl, Object platformProject) {
        NativeProject nativeProject = platformProject instanceof NativeProject ? (NativeProject)platformProject : null;
        if( nativeProject != null ) {
            // The following code removed. It's a project responsibility to call this method only once.
            //	// TODO: fix the problem of registering the same listener twice
            //	// now just remove then add to prevent double instance
            //	nativeProject.removeProjectItemsListener(projectItemListener);
            nativeProject.addProjectItemsListener(projectItemListener);
        }
    }
    
    public void dumpNativeProject(NativeProject nativeProject) {
        List<NativeFileItem> headers = nativeProject.getAllHeaderFiles();
        System.err.println("\n\n\nDumping project " + nativeProject.getProjectDisplayName());
        System.err.println("\nSystem include paths");
        for (Iterator it = nativeProject.getSystemIncludePaths().iterator(); it.hasNext();) System.err.println("    " + it.next());
        System.err.println("\nUser include paths");
        for (Iterator it = nativeProject.getUserIncludePaths().iterator(); it.hasNext();)   System.err.println("    " + it.next());
        System.err.println("\nSystem macros");
        for (Iterator it = nativeProject.getSystemMacroDefinitions().iterator(); it.hasNext();) System.err.println("    " + it.next());
        System.err.println("\nUser macros");
        for (Iterator it = nativeProject.getUserMacroDefinitions().iterator(); it.hasNext();)   System.err.println("    " + it.next());
        List<NativeFileItem> files;
        files = nativeProject.getAllSourceFiles();
        System.err.println("\nSources: (" + files.size() + " files )");
        for (Iterator it = files.iterator(); it.hasNext();) {
            NativeFileItem elem = (NativeFileItem) it.next();
            System.err.println(elem.getFile().getAbsolutePath());
        }
        files = nativeProject.getAllHeaderFiles();
        System.err.println("\nHeaders: (" + files.size() + " files )");
        for (Iterator it = files.iterator(); it.hasNext();) {
            NativeFileItem elem = (NativeFileItem) it.next();
            System.err.println(elem.getFile().getAbsolutePath());
        }
        
        System.err.println("End of project dump\n\n\n");
    }
    
    public NativeProject getNativeProject(Object platformProject) {
        NativeProject nativeProject = platformProject instanceof NativeProject ? (NativeProject)platformProject : null;
        if (platformProject instanceof Project ) {
            Project project = (Project) platformProject;
            nativeProject = project.getLookup().lookup(NativeProject.class);
        }    
	return nativeProject;
    }
    
    private String toString(Project project) {
        StringBuilder sb = new StringBuilder();
        ProjectInformation  pi = ProjectUtils.getInformation(project);
        if( pi != null ) {
            sb.append(" Name=" + pi.getName()); // NOI18N
            sb.append(" DisplayName=" + pi.getDisplayName()); // NOI18N
        }
//        SourceGroup[] sg = ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC);
//        for( int i = 0; i < sg.length; i++ ) {
//            sb.append(" SG DisplayName=" + sg[i].getDisplayName() + " rootFolder=" + sg[i].getRootFolder());
//        }
        return sb.toString();
    }
    
    private void addProject(Project project) {
        if( TraceFlags.DEBUG ) Diagnostic.trace("### ModelSupport.addProject: " + toString(project)); // NOI18N
        NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
        if (nativeProject != null) {
            openedProjects.add(project);
            if( TraceFlags.DEBUG ) {
                dumpProjectFiles(nativeProject);
            }
            
            boolean enableModel = new CodeAssistanceOptions(project).getCodeAssistanceEnabled().booleanValue();
            
            model.addProject(nativeProject, nativeProject.getProjectDisplayName(), enableModel);
        }
    }
    
    private void dumpProjectFiles(NativeProject nativeProject) {
        if( TraceFlags.DEBUG ) {
            Diagnostic.trace("+++ Sources:"); // NOI18N
            for (Iterator it = nativeProject.getAllSourceFiles().iterator(); it.hasNext();) {
                NativeFileItem elem = (NativeFileItem) it.next();
                trace(elem);
            }
            Diagnostic.trace("+++ Headers:"); // NOI18N
            for (Iterator<NativeFileItem> it = nativeProject.getAllHeaderFiles().iterator(); it.hasNext();) {
                NativeFileItem elem = it.next();
                trace(elem);
            }
        }
    }

    private void closeProject(Project project) {
        if( TraceFlags.DEBUG ) Diagnostic.trace("### ModelSupport.closeProject: " + toString(project)); // NOI18N
        NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
        if (nativeProject != null) {
            model.closeProject(nativeProject);
        }
        openedProjects.remove(project);
    }    
    
    private void removeProject(Project project) {
        if( TraceFlags.DEBUG ) Diagnostic.trace("### ModelSupport.removeProject: " + toString(project)); // NOI18N
        NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
        if (nativeProject != null) {
            model.removeProject(nativeProject);
        }
        openedProjects.remove(project);
    }
    
    private Collection<NativeProject> getNativeProjects() {
        Set<NativeProject> res = new HashSet<NativeProject>();
        for(CsmProject project : model.projects()){
            Object prj = project.getPlatformProject();
            if (prj instanceof NativeProject) {
                res.add((NativeProject)prj);
            }
        }
        return res;
//        Collection<NativeProject> nativeProjects = new HashSet<NativeProject>();
//        Project[] nbProjects = OpenProjects.getDefault().getOpenProjects();
//        for (int i = 0; i < nbProjects.length; i++) {
//            NativeProject nativeProject = (NativeProject) nbProjects[i].getLookup().lookup(NativeProject.class);
//            if (nativeProject != null) {
//                nativeProjects.add(nativeProject);
//            }
//        }
//        return nativeProjects;
    }

    public FileBuffer getFileBuffer(File file) {
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if( fo != null ) {
            try {
                DataObject dao = DataObject.find(fo);
                if( dao.isModified() ) {
                    EditorCookie editor = dao.getCookie(EditorCookie.class);
                    if( editor != null ) {
                        Document doc = editor.getDocument();
                        if( doc != null ) {
                            return new FileBufferDoc(file, doc);
                        }
                    }
                }
            } catch( DataObjectNotFoundException e ) {
                // nothing
            }
        }
        return new FileBufferFile(file);
    }
    
    public void onMemoryLow(LowMemoryEvent event, boolean fatal) {
        LowMemoryAlerter alerter = Lookup.getDefault().lookup(LowMemoryAlerter.class);
        if( alerter != null ) {
            alerter.alert(event, fatal);
        }
    }

    public void shutdown() {
        DataObject.getRegistry().removeChangeListener(modifiedListener);
    }
    
    private static final class BufAndProj {
        public BufAndProj(FileBuffer buffer, ProjectBase project, NativeFileItem nativeFile) {
            assert buffer != null : "null buffer";
            this.buffer = buffer;
            assert project != null : "null project";
            this.project = project;
            assert nativeFile != null : "null nativeFile";
            this.nativeFile = nativeFile;
        }
        public final FileBuffer buffer;
        public final ProjectBase project;
        public final NativeFileItem nativeFile;
    }
    
    private class FileChangeListener implements ChangeListener {
        
        private Map<DataObject, Collection<BufAndProj>> buffers = new HashMap<DataObject, Collection<BufAndProj>>();
	
	private Collection<BufAndProj> getBufNP(DataObject dao) {
	    Collection<BufAndProj> bufNPcoll = buffers.get(dao);
	    return (bufNPcoll == null) ? Collections.<BufAndProj>emptyList() : bufNPcoll;
	}
	
	private void addBufNP(DataObject dao, BufAndProj bufNP) {
	    Collection<BufAndProj> bufNPcoll = buffers.get(dao);
	    if( bufNPcoll == null ) {
		bufNPcoll = new ArrayList<BufAndProj>();
		buffers.put(dao, bufNPcoll);
	    }
	    bufNPcoll.add(bufNP);
	}
	
        // TODO: need to change implementation when ataObject will contain correct cookie
        private void editStart(DataObject curObj) {
	    if (!curObj.isValid()) {//IZ#114182
                return;
            }
	    NativeFileItemSet set = curObj.getLookup().lookup(NativeFileItemSet.class);
	    
	    if( set != null && ! set.isEmpty() ) {
		
		EditorCookie editor = curObj.getCookie(EditorCookie.class);
		Document doc = editor != null ? editor.getDocument() : null;
		FileObject primaryFile = curObj.getPrimaryFile();
		File file = FileUtil.toFile(primaryFile);
		final FileBufferDoc buffer = new FileBufferDoc(file, doc);
		
		for( NativeFileItem nativeFile : set ) {
		    ProjectBase csmProject = (ProjectBase) model.getProject(nativeFile.getNativeProject());
                    if (csmProject != null) { // this could be null when code assistance is turned off for project
                        addBufNP(curObj, new BufAndProj(buffer, csmProject, nativeFile));
                        csmProject.onFileEditStart(buffer, nativeFile);
                    }
		}
	    }
        }
        
//        private void editEnd(DataObject curObj) {
//            // TODO: some weird logic. New FileBufferFile should be created
//            // instead.
//
//	    BufAndProj bufNP = (BufAndProj) buffers.get(curObj);
//	    if( bufNP != null ) {
//                bufNP.project.onFileEditEnd(bufNP.buffer);
//            }
//        }

	private void traceStateChanged(ChangeEvent e) {
	    if( TraceFlags.DEBUG ) {
                Diagnostic.trace("state of registry changed:"); // NOI18N
                Diagnostic.indent();
                if (e != null) {
                    DataObject[] objs = DataObject.getRegistry().getModified();
                    if (objs.length == 0) {
                        Diagnostic.trace("all objects are saved"); // NOI18N
                    } else {
                        Diagnostic.trace("set of edited objects:"); // NOI18N
                        for (int i = 0; i < objs.length; i++) {
                            DataObject curObj = objs[i];
                            Diagnostic.trace("object " + i + ":" + curObj.getName()); // NOI18N
                            Diagnostic.indent();
                            Diagnostic.trace("with file: " + curObj.getPrimaryFile()); // NOI18N
			    NativeFileItemSet set = curObj.getNodeDelegate().getLookup().lookup(NativeFileItemSet.class);
			    if( set == null ) {
				Diagnostic.trace("NativeFileItemSet == null"); // NOI18N
			    }
			    else {
				Diagnostic.trace("NativeFileItemSet:"); // NOI18N
				for( NativeFileItem item : set ) {
				    Diagnostic.trace("\t" + item.getNativeProject().getProjectDisplayName()); // NOI18N
				}
			    }
                            EditorCookie editor = curObj.getCookie(EditorCookie.class);
                            Diagnostic.trace("has editor support: " + editor); // NOI18N
                            Document doc = editor != null ? editor.getDocument() : null;
                            Diagnostic.trace("with document: " + doc); // NOI18N
                            Diagnostic.unindent();
                        }
                    }
                } else {
                    Diagnostic.trace("no additional info from event object"); // NOI18N
                }
                Diagnostic.unindent();
	    }
	}
	
        public void stateChanged(ChangeEvent e) {
            if( TraceFlags.DEBUG ) {
		traceStateChanged(e);
            }
            if (e != null) {
                
                DataObject[] objs = DataObject.getRegistry().getModified();
                
                Set<DataObject> toDelete = new HashSet<DataObject>();
                
                // find all files, which stopped editing
                for( Iterator iter = buffers.keySet().iterator(); iter.hasNext(); ) {
                    DataObject dao = (DataObject) iter.next();
                    if( ! contains(objs, dao) ) {
			for( BufAndProj bufNP : getBufNP(dao) ) {
			    if( bufNP != null ) {
				// removing old doc buffer and creating new one
				bufNP.project.onFileEditEnd( getFileBuffer(bufNP.buffer.getFile()), bufNP.nativeFile );
			    }
			}
                        toDelete.add(dao);
                    }
                }
                
                // now remove these files from bufres map
                for( Iterator iter = toDelete.iterator(); iter.hasNext(); ) {
                    buffers.remove(iter.next());
                }
                
                // add new buffers
                for( int i = 0; i < objs.length; i++ ) {
                    if( ! buffers.containsKey(objs[i]) ) {
                        editStart(objs[i]);
                    }
                }
            }
        }
        
        private boolean contains(Object[] objs, Object o) {
            for( int i = 0; i < objs.length; i++ ) {
                if( objs[i].equals(o) ) {
                    return true;
                }
            }
            return false;
        }
    }
}
