/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.config;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 * Abstract deep directory file listener on list of files, existent or not.
 *
 * @author nn136682
 * @author Andrei Badea
 */
public abstract class AbstractFilesListener {
    protected J2eeModuleProvider provider;
    private HashMap fileListeners = new HashMap();
    
    private FileChangeListener listener = new FileListener();
    
    /** Creates a new instance of AbstractFilesListener */
    public AbstractFilesListener(J2eeModuleProvider provider) {
        this.provider = provider;
        startListening();
    }
    
    protected abstract File[] getTargetFiles();
    protected abstract boolean isTarget(FileObject fo);
    protected abstract boolean isTarget(String fileName);
    protected abstract void targetCreated(FileObject fo);
    protected abstract void targetDeleted(FileObject fo);
    protected abstract void targetChanged(FileObject fo);
    
    private synchronized void startListening() {
        File[] targets = getTargetFiles();
        for (int i=0; i<targets.length; i++) {
            startListening(targets[i]);
        }
    }
    public synchronized void stopListening() {
        for (Iterator i = fileListeners.keySet().iterator(); i.hasNext();) {
            FileObject fo = (FileObject) i.next();
            removeFileListenerFrom(fo);
        }
    }
    private void startListening(File target) {
        FileObject targetFO = FileUtil.toFileObject(target);
        while (targetFO == null) {
            target = target.getParentFile();
            if (target == null)
                return;
            targetFO = FileUtil.toFileObject(target);
        }
        if (!fileListeners.containsKey(targetFO)) {
            addFileListenerTo(targetFO);
        }
    }
    
    private void addFileListenerTo(FileObject fo) {
        FileChangeListener l = FileUtil.weakFileChangeListener(listener, fo);
        fileListeners.put(fo, l);
        fo.addFileChangeListener(l);
        
    }
    
    private void removeFileListenerFrom(FileObject fo) {
        FileChangeListener l = (FileChangeListener)fileListeners.remove(fo);
        if (l != null) {
            fo.removeFileChangeListener(l);
        }
    }
    
    private final class FileListener implements FileChangeListener {
        
        public void fileFolderCreated(FileEvent e) {
            startListening();
        }
        public void fileDeleted(FileEvent e) {
            FileObject fo = e.getFile();
            if (isTarget(fo)) {
                synchronized(fileListeners) {
                    removeFileListenerFrom(fo);
                }
                targetDeleted(fo);
            }
            startListening();
        }
        public void fileDataCreated(FileEvent e) {
            FileObject fo = e.getFile();
            if (isTarget(fo)) {
                synchronized(fileListeners) {
                    addFileListenerTo(fo);
                }
                targetCreated(fo);
            }
        }
        public void fileRenamed(FileRenameEvent e) {
            FileObject fo = e.getFile();
            if (isTarget(fo)) {
                synchronized(fileListeners) {
                    if (!fileListeners.containsKey(fo)) {
                        addFileListenerTo(fo);
                    }
                }
                targetCreated(fo);
            } else {
                if (isTarget(e.getName() + "." + e.getExt())) {
                    synchronized(fileListeners) {
                        removeFileListenerFrom(fo);
                    }
                    targetDeleted(fo);
                }
            }
            startListening();
        }

        public void fileAttributeChanged(FileAttributeEvent e) {};

        public void fileChanged(FileEvent e) {
            FileObject fo = e.getFile();
            if (isTarget(fo)) {
                fo.refresh(true);
                targetChanged(fo);
            }
        }
    }
}
