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
package org.netbeans.modules.cnd.modelimpl.platform;

import java.io.IOException;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;
import java.util.*;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;

import org.netbeans.api.project.*;
import org.netbeans.api.project.ui.OpenProjects;

import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.memory.LowMemoryEvent;
import org.netbeans.modules.cnd.modelimpl.options.CodeAssistanceOptions;
import org.netbeans.modules.cnd.modelimpl.spi.LowMemoryAlerter;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Vladimir Kvashin
 */
public class ModelSupport implements PropertyChangeListener {

    private static final ModelSupport instance = new ModelSupport();
    private ModelImpl theModel;
    private final Set<Project> openedProjects = new HashSet<Project>();
    private final ModifiedObjectsChangeListener modifiedListener = new ModifiedObjectsChangeListener();
    private FileChangeListener fileChangeListener;
    private static final boolean TRACE_STARTUP = false;
    private volatile boolean postponeParse = false;

    private ModelSupport() {
    }

    public static ModelSupport instance() {
        return instance;
    }

    public static int getTabSize() {
        return 8;
    }

    public File locateFile(String fileName) {
        InstalledFileLocator locator = InstalledFileLocator.getDefault();
        if (locator != null) {
            File file = locator.locate(fileName, "com.sun.tools.swdev.parser.impl/1", false); // NOI18N
            if (file != null) {
                return file;
            }
        }
        // the above code is mostly for debugging purposes;
        // but seems it didn't spoil anything :)
        File file = new File(fileName);
        return file.exists() ? file : null;
    }

    public void setModel(ModelImpl model) {
        this.theModel = model;
        synchronized (this) {
            if (fileChangeListener != null) {
                FileUtil.removeFileChangeListener(fileChangeListener);
                fileChangeListener = null;
            }
            if (model != null) {
                fileChangeListener = new ExternalUpdateListener();
                FileUtil.addFileChangeListener(fileChangeListener);
            }
        }
    }

    public void startup() {

        DataObject.getRegistry().addChangeListener(modifiedListener);

        if (!CndUtils.isStandalone()) {
            openedProjects.clear();
            if (TRACE_STARTUP) {
                System.out.println("Model support: Inited"); // NOI18N
            }
            if (TopComponent.getRegistry().getOpened().size() > 0) {
                if (TRACE_STARTUP) {
                    System.out.println("Model support: Open projects in Init"); // NOI18N
                }
                postponeParse = false;
                OpenProjects.getDefault().addPropertyChangeListener(this);
                openProjects();
            } else {
                if (TRACE_STARTUP) {
                    System.out.println("Model support: Postpone open projects"); // NOI18N
                }
                postponeParse = true;
                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

                    public void run() {
                        if (TRACE_STARTUP) {
                            System.out.println("Model support: invoked after ready UI"); // NOI18N
                        }
                        postponeParse = false;
                        Runnable task = new Runnable() {

                            public void run() {
                                OpenProjects.getDefault().addPropertyChangeListener(ModelSupport.this);
                                openProjects();
                            }
                        };
                        if (SwingUtilities.isEventDispatchThread()) {
                            RequestProcessor.getDefault().post(task);
                        } else {
                            task.run();
                        }
                    }
                });
            }
        }
    }

    public void shutdown() {
        DataObject.getRegistry().removeChangeListener(modifiedListener);
        ModelImpl model = theModel;
        if (model != null) {
            model.shutdown();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        try { //FIXUP #109105 OpenProjectList does not get notification about adding a project if the project is stored in the repository
            if (TRACE_STARTUP) {
                System.out.println("Model support event:" + evt.getPropertyName());
            }
            if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
                if (!postponeParse) {
                    if (TRACE_STARTUP) {
                        System.out.println("Model support: Open projects on OpenProjects.PROPERTY_OPEN_PROJECTS"); // NOI18N
                    }
                    RequestProcessor.getDefault().post(new Runnable() {

                        public void run() {
                            openProjects();
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void openProjects() {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();

        synchronized (openedProjects) {
            Set<Project> nowOpened = new HashSet<Project>();
            for (int i = 0; i < projects.length; i++) {
                nowOpened.add(projects[i]);
                if (!openedProjects.contains(projects[i])) {
                    addProject(projects[i]);
                }
            }

            Set<Project> toClose = new HashSet<Project>();
            for (Project project : openedProjects) {
                if (!nowOpened.contains(project)) {
                    toClose.add(project);
                }
            }

            for (Project project : toClose) {
                closeProject(project);
            }
        }
    }

    public static void trace(NativeFileItem nativeFile) {
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

    public static void dumpNativeProject(NativeProject nativeProject) {
        System.err.println("\n\n\nDumping project " + nativeProject.getProjectDisplayName());
        System.err.println("\nSystem include paths");
        for (Iterator it = nativeProject.getSystemIncludePaths().iterator(); it.hasNext();) {
            System.err.println("    " + it.next());
        }
        System.err.println("\nUser include paths");
        for (Iterator it = nativeProject.getUserIncludePaths().iterator(); it.hasNext();) {
            System.err.println("    " + it.next());
        }
        System.err.println("\nSystem macros");
        for (Iterator it = nativeProject.getSystemMacroDefinitions().iterator(); it.hasNext();) {
            System.err.println("    " + it.next());
        }
        System.err.println("\nUser macros");
        for (Iterator it = nativeProject.getUserMacroDefinitions().iterator(); it.hasNext();) {
            System.err.println("    " + it.next());
        }
        List<NativeFileItem> sources = new ArrayList<NativeFileItem>();
        List<NativeFileItem> headers = new ArrayList<NativeFileItem>();
        for (NativeFileItem item : nativeProject.getAllFiles()) {
            if (!item.isExcluded()) {
                switch (item.getLanguage()) {
                    case C:
                    case CPP:
                        sources.add(item);
                        break;
                    case C_HEADER:
                        headers.add(item);
                        break;
                    default:
                        break;
                }
            }
        }
        System.err.println("\nSources: (" + sources.size() + " files )");
        for (NativeFileItem elem : sources) {
            System.err.println(elem.getFile().getAbsolutePath());
        }
        System.err.println("\nHeaders: (" + headers.size() + " files )");
        for (NativeFileItem elem : headers) {
            System.err.println(elem.getFile().getAbsolutePath());
        }

        System.err.println("End of project dump\n\n\n");
    }

    public static NativeProject getNativeProject(Object platformProject) {
        NativeProject nativeProject = platformProject instanceof NativeProject ? (NativeProject) platformProject : null;
        if (platformProject instanceof Project) {
            Project project = (Project) platformProject;
            nativeProject = project.getLookup().lookup(NativeProject.class);
        }
        return nativeProject;
    }

    private String toString(Project project) {
        StringBuilder sb = new StringBuilder();
        ProjectInformation pi = ProjectUtils.getInformation(project);
        if (pi != null) {
            sb.append(" Name=" + pi.getName()); // NOI18N
            sb.append(" DisplayName=" + pi.getDisplayName()); // NOI18N
        }
//        SourceGroup[] sg = ProjectUtils.getSources(project).getSourceGroups(Sources.TYPE_GENERIC);
//        for( int i = 0; i < sg.length; i++ ) {
//            sb.append(" SG DisplayName=" + sg[i].getDisplayName() + " rootFolder=" + sg[i].getRootFolder());
//        }
        return sb.toString();
    }

    private void addProject(final Project project) {
        if (TraceFlags.DEBUG) {
            Diagnostic.trace("### ModelSupport.addProject: " + toString(project)); // NOI18N
        }

        final NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
        if (nativeProject != null) {

            CsmModelAccessor.getModel(); // just to ensure it's created
            final ModelImpl model = theModel;
            if (model == null) {
                return;
            }

            openedProjects.add(project);
            if (TraceFlags.DEBUG) {
                dumpProjectFiles(nativeProject);
            }

            nativeProject.runOnCodeModelReadiness(new Runnable() {

                public void run() {
                    boolean enableModel = new CodeAssistanceOptions(project).getCodeAssistanceEnabled();

                    model.addProject(nativeProject, nativeProject.getProjectDisplayName(), enableModel);
                }
            });
        }
    }

    private void dumpProjectFiles(NativeProject nativeProject) {
        if (TraceFlags.DEBUG) {
            Diagnostic.trace("+++ Sources:"); // NOI18N
            List<NativeFileItem> sources = new ArrayList<NativeFileItem>();
            List<NativeFileItem> headers = new ArrayList<NativeFileItem>();
            for (NativeFileItem item : nativeProject.getAllFiles()) {
                if (!item.isExcluded()) {
                    switch (item.getLanguage()) {
                        case C:
                        case CPP:
                            sources.add(item);
                            break;
                        case C_HEADER:
                            headers.add(item);
                            break;
                        default:
                            break;
                    }
                }
            }
            for (NativeFileItem elem : sources) {
                trace(elem);
            }
            Diagnostic.trace("+++ Headers:"); // NOI18N
            for (NativeFileItem elem : headers) {
                trace(elem);
            }
        }
    }

    private void closeProject(Project project) {
        if (TraceFlags.DEBUG) {
            Diagnostic.trace("### ModelSupport.closeProject: " + toString(project)); // NOI18N
        }
        ModelImpl model = theModel;
        if (model == null) {
            return;
        }
        NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
        if (nativeProject != null) {
            model.closeProject(nativeProject);
        }
        openedProjects.remove(project);
    }

    public static FileBuffer getFileBuffer(File file) {
        File normalizeFile = CndFileUtils.normalizeFile(file);
        FileObject fo = FileUtil.toFileObject(normalizeFile);
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                if (dao.isModified()) {
                    EditorCookie editor = dao.getCookie(EditorCookie.class);
                    if (editor != null) {
                        Document doc = editor.getDocument();
                        if (doc != null) {
                            return new FileBufferDoc(normalizeFile.getAbsolutePath(), doc);
                        }
                    }
                }
            } catch (DataObjectNotFoundException e) {
                // nothing
            }
        }
        return new FileBufferFile(normalizeFile.getAbsolutePath());
    }

    public void onMemoryLow(LowMemoryEvent event, boolean fatal) {
        LowMemoryAlerter alerter = Lookup.getDefault().lookup(LowMemoryAlerter.class);
        if (alerter != null) {
            alerter.alert(event, fatal);
        }
    }

    public static NativeProject[] getOpenNativeProjects() {
        if (CndUtils.isStandalone()) {
            return new NativeProject[0];
        }
        List<NativeProject> result = new ArrayList<NativeProject>();
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            NativeProject nativeProject = projects[i].getLookup().lookup(NativeProject.class);
            if (nativeProject != null) {
                result.add(nativeProject);
            }
        }
        return result.toArray(new NativeProject[result.size()]);
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

    private class ModifiedObjectsChangeListener implements ChangeListener {

        private Map<DataObject, Collection<BufAndProj>> buffers = new HashMap<DataObject, Collection<BufAndProj>>();

        private Collection<BufAndProj> getBufNP(DataObject dao) {
            Collection<BufAndProj> bufNPcoll = buffers.get(dao);
            return (bufNPcoll == null) ? Collections.<BufAndProj>emptyList() : bufNPcoll;
        }

        private void addBufNP(DataObject dao, BufAndProj bufNP) {
            Collection<BufAndProj> bufNPcoll = buffers.get(dao);
            if (bufNPcoll == null) {
                bufNPcoll = new ArrayList<BufAndProj>();
                buffers.put(dao, bufNPcoll);
            }
            bufNPcoll.add(bufNP);
        }

        // TODO: need to change implementation when ataObject will contain correct cookie
        private void editStart(DataObject curObj) {
            ModelImpl model = theModel;
            if (model == null) {
                return;
            }
            if (!curObj.isValid()) {//IZ#114182
                return;
            }
            NativeFileItemSet set = curObj.getLookup().lookup(NativeFileItemSet.class);
            if (set == null) {
                set = findCanonicalSet(curObj);
            }

            if (set != null && !set.isEmpty()) {

                EditorCookie editor = curObj.getCookie(EditorCookie.class);
                Document doc = editor != null ? editor.getDocument() : null;
                if (doc.getProperty("cnd.refactoring.modification.event") != Boolean.TRUE) {
                    FileObject primaryFile = curObj.getPrimaryFile();
                    File file = FileUtil.toFile(primaryFile);
                    final FileBufferDoc buffer = new FileBufferDoc(file.getAbsolutePath(), doc);

                    for (NativeFileItem nativeFile : set.getItems()) {
                        ProjectBase csmProject = (ProjectBase) model.getProject(nativeFile.getNativeProject());
                        if (csmProject != null) { // this could be null when code assistance is turned off for project
                            addBufNP(curObj, new BufAndProj(buffer, csmProject, nativeFile));
                            csmProject.onFileEditStart(buffer, nativeFile);
                        }
                    }
                } else {
//                    System.err.println("skip unnecessary switch of buffers");
                }
            }
        }

        private boolean isCndDataObject(FileObject fo) {
            String type = fo.getMIMEType();
            return MIMENames.isHeaderOrCppOrC(type);
        }

        private NativeFileItemSet findCanonicalSet(DataObject curObj) {
            FileObject fo = curObj.getPrimaryFile();
            if (fo != null && isCndDataObject(fo)) {
                File file = FileUtil.toFile(fo);
                // the file can null, for example, when we edit templates
                if (file != null) {
                    try {
                        fo = FileUtil.toFileObject(file.getCanonicalFile());
                        curObj = DataObject.find(fo);
                        return curObj.getLookup().lookup(NativeFileItemSet.class);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            return null;
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
            if (TraceFlags.DEBUG) {
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
                            if (set == null) {
                                Diagnostic.trace("NativeFileItemSet == null"); // NOI18N
                            } else {
                                Diagnostic.trace("NativeFileItemSet:"); // NOI18N
                                for (NativeFileItem item : set.getItems()) {
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
            if (TraceFlags.DEBUG) {
                traceStateChanged(e);
            }
            if (e != null) {

                DataObject[] objs = DataObject.getRegistry().getModified();

                Set<DataObject> toDelete = new HashSet<DataObject>();

                // find all files, which stopped editing
                for (Iterator iter = buffers.keySet().iterator(); iter.hasNext();) {
                    DataObject dao = (DataObject) iter.next();
                    if (!contains(objs, dao)) {
                        for (BufAndProj bufNP : getBufNP(dao)) {
                            if (bufNP != null) {
                                // removing old doc buffer and creating new one
                                bufNP.project.onFileEditEnd(getFileBuffer(bufNP.buffer.getFile()), bufNP.nativeFile);
                            } else {
                                System.err.println("no buffer for " + dao);
                            }
                        }
                        toDelete.add(dao);
                    }
                }

                // now remove these files from bufres map
                for (Iterator iter = toDelete.iterator(); iter.hasNext();) {
                    buffers.remove(iter.next());
                }

                // add new buffers
                for (int i = 0; i < objs.length; i++) {
                    if (!buffers.containsKey(objs[i])) {
                        editStart(objs[i]);
                    }
                }
            }
        }

        private boolean contains(Object[] objs, Object o) {
            for (int i = 0; i < objs.length; i++) {
                if (objs[i].equals(o)) {
                    return true;
                }
            }
            return false;
        }
    }

    private class ExternalUpdateListener extends FileChangeAdapter implements Runnable {
        
        private boolean isRunning;
        private final Map<FileObject, Boolean> changedFileObjects = new HashMap<FileObject, Boolean>();
        private final Map<FileObject, Long> eventTimes = new WeakHashMap<FileObject, Long>();

        /** FileChangeListener implementation. Fired when a file is changed. */
        @Override
        public void fileChanged(FileEvent fe) {
            if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                System.err.printf("External updates: fileChanged %s\n", fe);
            }
            ModelImpl model = theModel;
            if (model != null) {
                FileObject fo = fe.getFile();
                if (isCOrCpp(fo)) {
                    scheduleUpdate(fo, fe.getTime(), false);
                }
            }
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                System.err.printf("External updates: fileDataCreated %s\n", fe);
            }
            ModelImpl model = theModel;
            if (model != null) {
                FileObject fo = fe.getFile();
                if (isCOrCpp(fo)) {
                    scheduleUpdate(fo, fe.getTime(), true);
                }
            }
        }

        private void scheduleUpdate(FileObject fo, long eventTime, boolean isCreated) {
            ModelImpl model = theModel;
            if (model != null) {
                synchronized (this) {
                    Long lastEvent = eventTimes.get(fo);
                    if (lastEvent == null || (eventTime - lastEvent.longValue()) > 500) {
                        eventTimes.put(fo, Long.valueOf(eventTime));
                    } else {
                        if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                            System.err.printf("External updates: SKIP EVENT By oldT:%s and newT:%d\n", lastEvent, eventTime);
                        }
                        return;
                    }
                    if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                        System.err.printf("External updates: scheduling update for %s\n", fo);
                    }
                    if (!changedFileObjects.containsKey(fo)) {
                        changedFileObjects.put(fo, isCreated);
                    }
                    if (!isRunning) {
                        isRunning = true;
                        model.enqueueModelTask(this, "External File Updater"); // NOI18N
                    }
                }
            }
        }

        public void run() {
            if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                System.err.printf("External updates: running update task\n");
            }
            while (true) {
                final FileObject fo;
                final boolean created;
                synchronized (this) {
                    if (changedFileObjects.isEmpty()) {
                        isRunning = false;
                        break;
                    } else {
                        Iterator<Map.Entry<FileObject, Boolean>> it = changedFileObjects.entrySet().iterator();
                        Map.Entry<FileObject, Boolean> entry = it.next();
                        fo = entry.getKey();
                        created = entry.getValue();
                        it.remove();
                    }
                }
                if (fo != null) {
                    if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                        System.err.printf("Updating for %s\n", fo);
                    }
                    if (created) {
                        ProjectBase project = (ProjectBase)CsmUtilities.getCsmProject(fo);
                        if (project != null) {
                            project.onFileExternalCreate(fo);
                        }
                   } else {
                        CsmFile[] files = CsmUtilities.getCsmFiles(fo);
                        for (int i = 0; i < files.length; ++i) {
                            FileImpl file = (FileImpl) files[i];
                            ProjectBase project = file.getProjectImpl(true);
                            project.onFileExternalChange(file);
                        }
                    }
                }
            }
            if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                System.err.printf("External updates: update task finished\n");
            }
        }

        private boolean isCOrCpp(FileObject fo) {
            String mime = fo.getMIMEType();
            if (mime == null) {
                mime = FileUtil.getMIMEType(fo);
                if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                    System.err.printf("MIME resolved: %s\n", mime);
                }
            }
            return MIMENames.isHeaderOrCppOrC(mime);
        }
    }
}
