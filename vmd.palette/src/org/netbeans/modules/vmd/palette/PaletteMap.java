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

import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.spi.palette.PaletteController;
import org.openide.filesystems.*;
import org.openide.util.Lookup;

import javax.swing.*;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;

/**
 * @author David Kaspar, Anton Chechel
 */
public final class PaletteMap implements ActiveDocumentSupport.Listener, FileChangeListener {
    
    private static final PaletteMap instance = new PaletteMap();
    
    private final WeakHashMap<String, WeakReference<PaletteKit>> kitMap = new WeakHashMap<String, WeakReference<PaletteKit>>();
    private String activeProjectID;
    
    private AtomicBoolean requiresUpdate = new AtomicBoolean(false);
    
    private PaletteMap() {
        ActiveDocumentSupport.getDefault().addActiveDocumentListener(this);
        activeDocumentChanged(null, ActiveDocumentSupport.getDefault().getActiveDocument());
        registerFileSystemListener();
    }
    
    public static PaletteMap getInstance() {
        return instance;
    }
    
    public void activeDocumentChanged(DesignDocument deactivatedDocument, DesignDocument activatedDocument) {
        if (activatedDocument == null) {
            return;
        }
        
        String oldProjectID;
        synchronized (this) {
            oldProjectID = activeProjectID;
            activeProjectID = activatedDocument.getDocumentInterface().getProjectID();
        }
        
        if (!activeProjectID.equals(oldProjectID)) {
            for (WeakReference<PaletteKit> kitReference : kitMap.values()) {
                PaletteKit kit = kitReference.get();
                assert kit != null;
                kit.clearNodesStateCache();
            }
        }
        
        WeakReference<PaletteKit> kitReference = kitMap.get(activatedDocument.getDocumentInterface().getProjectType());
        if (kitReference == null) {
            return;
        }
        PaletteKit kit = kitReference.get();
        if (kit == null) {
            return;
        }
        kit.setActiveDocument(activatedDocument);
        kit.update();
        kit.refreshPalette();
    }
    
    public void activeComponentsChanged(Collection<DesignComponent> activeComponents) {
    }
    
    public synchronized PaletteController getPaletteControllerForProjectType(String projectType) {
        WeakReference<PaletteKit> reference = kitMap.get(projectType);
        PaletteKit kit = reference != null ? reference.get() : null;
        if (kit == null) {
            kit = new PaletteKit(projectType);
            kitMap.put(projectType, new WeakReference<PaletteKit> (kit));
        }
        return kit.getPaletteController();
    }
    
    void checkValidity(String projectType, Lookup lookup) {
        WeakReference<PaletteKit> kitReference = kitMap.get(projectType);
        PaletteKit kit = kitReference != null ? kitReference.get() : null;
        if (kit == null) {
            PaletteItemDataNode node = lookup.lookup(PaletteItemDataNode.class);
            assert node != null;
            node.setNeedCheck(false);
            node.setValid(true);
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
    
    /**
     * Inspired by RepositoryUpdater.registerFileSystemListener
     */
    private void registerFileSystemListener() {
        final File[] roots = File.listRoots();
        final Set<FileSystem> fss = new HashSet<FileSystem>();
        for (File root : roots) {
            final FileObject fo = FileUtil.toFileObject(root);
            if (fo == null) {
                Debug.warning("No MasterFS for file system root: " + root.getAbsolutePath()); // NOI18N
            } else {
                try {
                    final FileSystem fs = fo.getFileSystem();
                    if (!fss.contains(fs)) {
                        fs.addFileChangeListener(this);
                        fss.add(fs);
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
        synchronized (sourceGroups) {
            FileObject rootFolder;
            for (SourceGroup sg : sourceGroups) {
                rootFolder = sg.getRootFolder();
                if (rootFolder != null && FileUtil.isParentOf(rootFolder, fo)) {
                    return rootFolder;
                }
            }
        }
        return null;
    }
    
    private void schedulePaletteUpdate() {
        if (requiresUpdate.getAndSet(true)) {
            return;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                while (requiresUpdate.getAndSet(false)) {
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
    
}
