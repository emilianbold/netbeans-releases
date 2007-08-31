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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.palette;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.openide.filesystems.*;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openide.util.RequestProcessor;

/**
 * @author David Kaspar, Anton Chechel
 */
public final class PaletteMap implements ActiveDocumentSupport.Listener, FileChangeListener, DescriptorRegistryListener, PropertyChangeListener {

    private static final PaletteMap instance = new PaletteMap();

    private final WeakHashMap<String, WeakReference<PaletteKit>> kitMap = new WeakHashMap<String, WeakReference<PaletteKit>>();
    private String activeProjectID;

    private DescriptorRegistry registeredRegistry;
    private final AtomicBoolean requiresPaletteUpdate = new AtomicBoolean(false);
    private boolean isFileListenerRegistered;

    private final Set<String> registeredProjects = new HashSet<String>();

    private PaletteMap() {
        ActiveDocumentSupport.getDefault().addActiveDocumentListener(this);
        //activeDocumentChanged(null, ActiveDocumentSupport.getDefault().getActiveDocument());
    }

    public static PaletteMap getInstance() {
        return instance;
    }

    public void activeDocumentChanged(DesignDocument deactivatedDocument, DesignDocument activatedDocument) {
        if (activatedDocument == null) {
            return;
        }

        synchronized (this) {
            if (!isFileListenerRegistered) {
                registerFileSystemListener();
                isFileListenerRegistered = true;
            }
        }

        DescriptorRegistry currentRegistry = activatedDocument.getDescriptorRegistry();
        if (registeredRegistry != currentRegistry) {
            if (registeredRegistry != null) {
                registeredRegistry.removeRegistryListener(this);
            }
            registeredRegistry = currentRegistry;
            if (registeredRegistry != null) {
                registeredRegistry.addRegistryListener(this);
            }
        }

        String oldProjectID;
        synchronized (this) {
            oldProjectID = activeProjectID;
            activeProjectID = activatedDocument.getDocumentInterface().getProjectID();
        }

        boolean isProjectIDChanged = !activeProjectID.equals(oldProjectID);
        if (isProjectIDChanged) {
            registerClassPathListener(activatedDocument);
        }

        updatePalette(activatedDocument, isProjectIDChanged);
    }

    public void activeComponentsChanged(Collection<DesignComponent> activeComponents) {
    }

    public void descriptorRegistryUpdated() {
        updatePalette(ActiveDocumentSupport.getDefault().getActiveDocument(), false);
    }

    private void updatePalette(DesignDocument document, boolean isProjectIDChanged) {
        if (isProjectIDChanged) {
            for (WeakReference<PaletteKit> kitReference : kitMap.values()) {
                PaletteKit kit = kitReference.get();
                if (kit != null) {
                    kit.clearNodesStateCache();
                }
            }
        }

        if (document == null) {
            return;
        }
        WeakReference<PaletteKit> kitReference = kitMap.get(document.getDocumentInterface().getProjectType());
        if (kitReference == null) {
            return;
        }
        PaletteKit kit = kitReference.get();
        if (kit == null) {
            return;
        }
        kit.setActiveDocument(document);
        if (isProjectIDChanged) {
            scheduleUpdateAfteCPScanned(document, kit);
        } else {
            kit.update();
        }
    }

    public synchronized PaletteKit getPaletteKitForProjectType(String projectType) {
        WeakReference<PaletteKit> reference = kitMap.get(projectType);
        PaletteKit kit = reference != null ? reference.get() : null;
        if (kit == null) {
            kit = new PaletteKit(projectType);
            kitMap.put(projectType, new WeakReference<PaletteKit>(kit));
        }
        return kit;
    }

    void checkValidity(String projectType, Lookup lookup) {
        WeakReference<PaletteKit> kitReference = kitMap.get(projectType);
        PaletteKit kit = kitReference != null ? kitReference.get() : null;
        if (kit == null) {
            PaletteItemDataNode node = lookup.lookup(PaletteItemDataNode.class);
            if (node != null) {
                node.setNeedCheck(false);
                node.setValid(true);
            }
        } else {
            kit.checkValidity(lookup);
        }
    }

    private void checkNeedUpdate(final FileEvent fe) {
        final FileObject root = getOwningRoot(fe.getFile());
        if (root != null) {
            schedulePaletteUpdate();
        }
    }

    private void checkNeedUpdate(final PropertyChangeEvent evt) {
        // TODO add conditions for checking CP
        schedulePaletteUpdate();
    }

    public void fileFolderCreated(FileEvent fe) {
        checkNeedUpdate(fe);
    }

    public void fileDataCreated(FileEvent fe) {
        checkNeedUpdate(fe);
    }

    public void fileChanged(FileEvent fe) {
        checkNeedUpdate(fe);
    }

    public void fileDeleted(FileEvent fe) {
        checkNeedUpdate(fe);
    }

    public void fileRenamed(FileRenameEvent fe) {
        checkNeedUpdate(fe);
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    public void propertyChange(PropertyChangeEvent evt) {
        checkNeedUpdate(evt);
    }

    private void scheduleUpdateAfteCPScanned(DesignDocument document, final PaletteKit kit) {
        final Project project = ProjectUtils.getProject(document);
        final ClasspathInfo info = getClasspathInfo(project);
        if (info == null) {
            return;
        }

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    JavaSource.create(info).runWhenScanFinished(new Task<CompilationController>() {
                        public void run(CompilationController controller) throws Exception {
                            kit.update();
                        }
                    }, true);
                } catch (IOException ex) {
                    Debug.warning(ex);
                }
            }
        });
    }

    private void registerClassPathListener(DesignDocument document) {
        final Project project = ProjectUtils.getProject(document);
        final ClasspathInfo info = getClasspathInfo(project);
        if (info == null) {
            return;
        }

        String projID = document.getDocumentInterface().getProjectID();
        if (!registeredProjects.contains(projID)) {
            Task<CompilationController> ct = new ListenerCancellableTask(info);
            try {
                JavaSource.create(info).runUserActionTask(ct, true);
                registeredProjects.add(projID);
            } catch (IOException ex) {
                Debug.warning(ex);
            }
        }
    }

    private ClasspathInfo getClasspathInfo(Project project) {
        if (project == null) {
            return null;
        }
        SourceGroup group = getSourceGroup(project);
        if (group == null) {
            return null;
        }
        FileObject fileObject = group.getRootFolder();
        return ClasspathInfo.create(fileObject);
    }

    private SourceGroup getSourceGroup(Project project) {
        SourceGroup[] sourceGroups = org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups == null || sourceGroups.length < 1) {
            return null;
        }
        return sourceGroups[0];
    }

    /**
     * Inspired by RepositoryUpdater.registerFileSystemListener
     */
    private void registerFileSystemListener() {
        final File[] roots = File.listRoots();
        final Set<FileSystem> fsSet = new HashSet<FileSystem>();
        for (File root : roots) {
            final FileObject fo = FileUtil.toFileObject(root);
            if (fo == null) {
                Debug.warning("No MasterFS for file system root: " + root.getAbsolutePath()); // NOI18N
            } else {
                try {
                    final FileSystem fileSystem = fo.getFileSystem();
                    if (!fsSet.contains(fileSystem)) {
                        FileChangeListener fileChangeListener = WeakListeners.create(FileChangeListener.class, this, fileSystem);
                        fileSystem.addFileChangeListener(fileChangeListener);
                        fsSet.add(fileSystem);
                    }
                } catch (FileStateInvalidException e) {
                    Debug.warning(e);
                }
            }
        }
    }

    /**
     * Inspired by RepositoryUpdater.getOwningRoot
     */
    private FileObject getOwningRoot(final FileObject fo) {
        if (fo == null || activeProjectID == null) {
            return null;
        }
        List<SourceGroup> sourceGroups = ProjectUtils.getSourceGroups(activeProjectID);
        if (sourceGroups == null) {
            return null;
        }

        FileObject rootFolder;
        for (SourceGroup sg : sourceGroups) {
            rootFolder = sg.getRootFolder();
            if (rootFolder != null && FileUtil.isParentOf(rootFolder, fo)) {
                return rootFolder;
            }
        }
        return null;
    }

    private void schedulePaletteUpdate() {
        if (requiresPaletteUpdate.getAndSet(true)) {
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                while (requiresPaletteUpdate.getAndSet(false)) {
                    for (WeakReference<PaletteKit> kitReference : kitMap.values()) {
                        PaletteKit kit = kitReference.get();
                        if (kit == null) {
                            continue;
                        }
                        kit.clearNodesStateCache();
                        // HINT refresh only visible palette
                        kit.refreshPalette();
                    }
                }
            }
        });
    }

    private final class ListenerCancellableTask implements Task<CompilationController> {

        private ClasspathInfo info;

        public ListenerCancellableTask(ClasspathInfo info) {
            this.info = info;
        }

        public void run(CompilationController controller) throws Exception {
            ClassPath cp = info.getClassPath(ClasspathInfo.PathKind.BOOT);
            PropertyChangeListener wcl = WeakListeners.propertyChange(PaletteMap.this, cp);
            cp.addPropertyChangeListener(wcl);
        }
    }
}
