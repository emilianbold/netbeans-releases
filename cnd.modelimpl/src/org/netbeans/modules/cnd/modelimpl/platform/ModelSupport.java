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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.platform;

import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
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
import org.netbeans.modules.cnd.modelimpl.spi.LowMemoryAlerter;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;

/**
 *
 * @author Vladimir Kvashin
 */
public class ModelSupport implements PropertyChangeListener {
    
    private static ModelSupport instance = new ModelSupport();
    
    private ModelImpl model;
    
    private Set openedProjects = new HashSet();
    
    private Set sourceExtensions = new TreeSet();
    
    private FileChangeListener modifiedListener;
    
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
        
        Collection listeners = Lookup.getDefault().lookup(new Lookup.Template(CsmProgressListener.class)).allInstances();
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            model.addProgressListener((CsmProgressListener) it.next());
        }
        
        //CodeModelRequestProcessor.instance().post(ProjectListenerThread.instance(), "Project Listener");
        this.model = model;
        
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        openedProjects = new HashSet();
        for( int i = 0; i < projects.length; i++ ) {
            addProject(projects[i]);
        }
        
        OpenProjects.getDefault().addPropertyChangeListener(this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if( evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS) ) {
            
            Project[] projects = OpenProjects.getDefault().getOpenProjects();
            synchronized (openedProjects){
                Set nowOpened = new HashSet();
                for( int i = 0; i < projects.length; i++ ) {
                    nowOpened.add(projects[i]);
                    if( ! openedProjects.contains(projects[i]) ) {
                        addProject(projects[i]);
                    }
                }

                Set toClose = new HashSet();
                for( Iterator iter = openedProjects.iterator(); iter.hasNext(); ) {
                    Object o = iter.next();
                    if( ! nowOpened.contains(o) ) {
                        toClose.add(o);
                    }
                }

                for( Iterator iter = toClose.iterator(); iter.hasNext(); ) {
                    removeProject((Project) iter.next());
                }
            }
        }
    }
    
    private NativeProjectItemsListener projectItemListener = new NativeProjectItemsListener() {
        public void fileAdded(NativeFileItem file) {
            onProjectItemAdded(file);
        }
        public void filePropertiesChanged(NativeFileItem file) {
            onProjectItemChanged(file);
        }
        
        public void fileRemoved(NativeFileItem file) {
            onProjectItemRemoved(file);
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
    
    protected void onProjectItemRemoved(final NativeFileItem item) {
        try {
            final ProjectBase project = getProject(item, false);
            if( project != null ) {
                project.onFileRemoved(item);
            }
        } catch( Exception e ) {
            //TODO: FIX (most likely in Makeproject: path == null in this situation,
            //this cause NPE
            e.printStackTrace(System.err);
        }
    }
    
    protected void onProjectItemChanged(final NativeFileItem item) {
        // invalidate cache for this file
        CacheManager.getInstance().invalidate(item.getFile().getAbsolutePath());
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

//    // FIXUP: used only in old project api
//    private ProjectBase getProject(File file) {
//	FileObject fo = FileUtil.toFileObject(file);
//	if( fo != null ) {
//	    Project platformProject = FileOwnerQuery.getOwner(fo);
//	    ProjectBase csmProject = (ProjectBase) model.getProject(platformProject);
//	    return csmProject;
//	}
//	return null;
//    }
    
    private ProjectBase getProject(NativeFileItem nativeFile, boolean createIfNeeded) {
        assert nativeFile != null : "must not be null";
        assert nativeFile.getFile() != null : "must be associated with valid file";
        assert nativeFile.getNativeProject() != null : "must have container project";
        ProjectBase csmProject = null;
        try {
            NativeProject nativeProject = nativeFile.getNativeProject();
            if (nativeProject == null) {                
                // TODO: temporary use old approach with query
                FileObject fo = FileUtil.toFileObject(nativeFile.getFile());
                Project platformProject = FileOwnerQuery.getOwner(fo);
                nativeProject = (NativeProject) platformProject.getLookup().lookup(NativeProject.class);
            }
            if (nativeProject != null) {
                csmProject = createIfNeeded ? (ProjectBase) model.getProject(nativeProject) :
                                              (ProjectBase) model.findProject(nativeProject);
            }
        } catch(NullPointerException ex) {
            ex.printStackTrace();
        }
        return csmProject;
    }
    
    private void trace(NativeFileItem nativeFile) {
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
    
    private void visitNativeFileItems(List/*<NativeFileItem>*/ items, FileVisitor visitor, boolean isSourceFile) throws FileVisitor.StopException {
        for (Iterator it = items.iterator(); it.hasNext();) {
            NativeFileItem elem = (NativeFileItem) it.next();
            File file = elem.getFile();
            assert (file != null) : "native file item must have valid File object";
            if( TraceFlags.DEBUG ) {
                trace(elem);
            }
            visitor.visit(elem, isSourceFile);
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
    
    private void dumpNativeProject(NativeProject nativeProject) {
        List/*<NativeFileItem>*/ headers = nativeProject.getAllHeaderFiles();
        System.err.println("\n\n\nDumping project " + nativeProject.getProjectDisplayName());
        System.err.println("\nSystem include paths");
        for (Iterator it = nativeProject.getSystemIncludePaths().iterator(); it.hasNext();) System.err.println("    " + it.next());
        System.err.println("\nUser include paths");
        for (Iterator it = nativeProject.getUserIncludePaths().iterator(); it.hasNext();)   System.err.println("    " + it.next());
        System.err.println("\nSystem macros");
        for (Iterator it = nativeProject.getSystemMacroDefinitions().iterator(); it.hasNext();) System.err.println("    " + it.next());
        System.err.println("\nUser macros");
        for (Iterator it = nativeProject.getUserMacroDefinitions().iterator(); it.hasNext();)   System.err.println("    " + it.next());
        List/*<NativeFileItem>*/ files;
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
    
    public void visitProjectFiles(ProjectBase csmProjectImpl, Object platformProject, FileVisitor visitor) {
        if( TraceFlags.DEBUG ) Diagnostic.trace("ModelSupport.visitProjectFiles for " + csmProjectImpl.getName()); // NOI18N
        NativeProject nativeProject = platformProject instanceof NativeProject ? (NativeProject)platformProject : null;
        if (platformProject instanceof Project ) {
            Project project = (Project) platformProject;
            nativeProject = (NativeProject)project.getLookup().lookup(NativeProject.class);
        }
        if( nativeProject != null ) {
            try {
                if( TraceFlags.DEBUG ) Diagnostic.trace("Using new NativeProject API"); // NOI18N
                // first of all visit sources, then headers
		
		if( TraceFlags.TIMING ) System.err.println("Getting files from project system");
		long time = System.currentTimeMillis();
		
                List/*<NativeFileItem>*/ sources = nativeProject.getAllSourceFiles();
                List/*<NativeFileItem>*/ headers = nativeProject.getAllHeaderFiles();
		
		
                if( TraceFlags.TIMING ) {
		    time = System.currentTimeMillis() - time;
		    System.err.println("Got files from project system. Time = " + time);
                    System.err.println("FILES COUNT:\nSource files:\t" + sources.size() + "\nHeader files:\t" + headers.size() + "\nTotal files:\t" + (sources.size() + headers.size()));
                }
                if(TraceFlags.DUMP_PROJECT_ON_OPEN ) {
                    dumpNativeProject(nativeProject);
                }
                visitNativeFileItems(sources, visitor, true);
                visitNativeFileItems(headers, visitor, false);
               
                // in fact if visitor used for parsing => visitor will parse all included files
                // recursively starting from current source file
                // so, when we visit headers, they should not be reparsed if already were parsed
                
            } catch( FileVisitor.StopException e ) {
            }
        }
    }
    
    private String toString(Project project) {
        StringBuffer sb = new StringBuffer();
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
        NativeProject nativeProject = (NativeProject) project.getLookup().lookup(NativeProject.class);
        if (nativeProject != null) {
            openedProjects.add(project);
            if( TraceFlags.DEBUG ) {
                dumpProjectFiles(nativeProject);
            }
            model.addProject(nativeProject, nativeProject.getProjectDisplayName());
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
            for (Iterator it = nativeProject.getAllHeaderFiles().iterator(); it.hasNext();) {
                NativeFileItem elem = (NativeFileItem) it.next();
                trace(elem);
            }
        }
    }
    
    private void removeProject(Project project) {
        if( TraceFlags.DEBUG ) Diagnostic.trace("### ModelSupport.removeProject: " + toString(project)); // NOI18N
        NativeProject nativeProject = (NativeProject) project.getLookup().lookup(NativeProject.class);
        if (nativeProject != null) {
            model.removeProject(nativeProject);
        }
        openedProjects.remove(project);
    }
    
    public Collection<NativeProject> getNativeProjects() {
        Collection<NativeProject> nativeProjects = new HashSet<NativeProject>();
        Project[] nbProjects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < nbProjects.length; i++) {
            NativeProject nativeProject = (NativeProject) nbProjects[i].getLookup().lookup(NativeProject.class);
            if (nativeProject != null) {
                nativeProjects.add(nativeProject);
            }
        }
	return nativeProjects;
    }
    
    /** gets a key, which uniquely identifies the project */
    public String getProjectKey(CsmProject project) {
        Object o = project.getPlatformProject();
        if( o instanceof Project ) {
            Project p = (Project) o;
            return p.getProjectDirectory().getPath();
        } else if (o instanceof NativeProject) {
            return ((NativeProject)o).getProjectRoot();
        } else if( o instanceof File ) {
            return ((File) o).getAbsolutePath();
        } else {
            return project.getName();
        }
    }
    
    
    public FileBuffer getFileBuffer(File file) {
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if( fo != null ) {
            try {
                DataObject dao = DataObject.find(fo);
                if( dao.isModified() ) {
                    EditorCookie editor = (EditorCookie) dao.getCookie(EditorCookie.class);
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
	LowMemoryAlerter alerter = (LowMemoryAlerter) Lookup.getDefault().lookup(LowMemoryAlerter.class);
	if( alerter != null ) {
	    alerter.alert(event, fatal);
	}
    }

    private static class BufAndProj {
        public BufAndProj(FileBuffer buffer, ProjectBase project, NativeFileItem nativeFile) {
            this.buffer = buffer;
            this.project = project;
	    this.nativeFile = nativeFile;
        }
        public FileBuffer buffer;
        public ProjectBase project;
	public NativeFileItem nativeFile;
    }    
    
    private class FileChangeListener implements ChangeListener {
        
        private Map/*<DataObject, BufAndProj>*/ buffers = new HashMap/*<DataObject, BufAndProj>*/();
        
        // TODO: need to change implementation when ataObject will contain correct cookie
        private void editStart(DataObject curObj) {
            FileObject primaryFile = curObj.getPrimaryFile();
            Project platformProject = FileOwnerQuery.getOwner(primaryFile);
            if( model != null && platformProject != null ) {
                NativeProject nativeProject = (NativeProject)platformProject.getLookup().lookup(NativeProject.class);
                final ProjectBase csmProject = (ProjectBase) model.getProject(nativeProject);
                if( csmProject != null && nativeProject != null) {
                    File file = FileUtil.toFile(primaryFile);
                    NativeFileItem nativeFile = nativeProject.findFileItem(file);
                    if(nativeFile != null ) {
                        EditorCookie editor = (EditorCookie) curObj.getCookie(EditorCookie.class);
                        Document doc = editor != null ? editor.getDocument() : null;
                        final FileBufferDoc buffer = new FileBufferDoc(file, doc);
                        buffers.put(curObj, new BufAndProj(buffer, csmProject, nativeFile));
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
        
        public void stateChanged(ChangeEvent e) {
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
                            Project prj = FileOwnerQuery.getOwner(curObj.getPrimaryFile());
                            Diagnostic.trace("from project: " + prj); // NOI18N
                            EditorCookie editor = (EditorCookie) curObj.getCookie(EditorCookie.class);
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
            if (e != null) {
                
                DataObject[] objs = DataObject.getRegistry().getModified();
                
                Set/*<DataObject>*/ toDelete = new HashSet/*<DataObject>*/();
                
                // find all files, which stopped editing
                for( Iterator iter = buffers.keySet().iterator(); iter.hasNext(); ) {
                    DataObject dao = (DataObject) iter.next();
                    if( ! contains(objs, dao) ) {
                        final BufAndProj bufNP = (BufAndProj) buffers.get(dao);
                        if( bufNP != null ) {
                            // removing old doc buffer and creating new one
                            bufNP.project.onFileEditEnd( getFileBuffer(bufNP.buffer.getFile()), bufNP.nativeFile );
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
