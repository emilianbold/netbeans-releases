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

package org.netbeans.modules.ruby.modules.project.rake;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;
import org.openide.filesystems.FileObject;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

// XXX current implementation is not efficient for listening to a large # of files

/**
 * Utility class to notify clients of changes in the existence or timestamp
 * of a named file or directory.
 * Unlike the Filesystems API, permits you to listen to a file which does not
 * yet exist, or continue listening to it after it is deleted and recreated, etc.
 * @author Jesse Glick
 * @see "Blockers: #44213, #44628, #42147, etc."
 * @see "#33162: hierarchical listeners"
 */
public final class FileChangeSupport {
    
    public static FileChangeSupport DEFAULT = new FileChangeSupport();
    
    private FileChangeSupport() {}
    
    private final Map<FileChangeSupportListener,Map<File,Holder>> holders = new WeakHashMap<FileChangeSupportListener,Map<File,Holder>>();
    
    /**
     * Add a listener to changes in a given path.
     * Can only add a given listener x path pair once.
     * However a listener can listen to any number of paths.
     * Note that listeners are always held weakly - if the listener is collected,
     * it is quietly removed.
     */
    public void addListener(FileChangeSupportListener listener, File path) {
        assert path.equals(FileUtil.normalizeFile(path)) : "Need to normalize " + path + " before passing to FCS!";
        Map<File,Holder> f2H = holders.get(listener);
        if (f2H == null) {
            f2H = new HashMap<File,Holder>();
            holders.put(listener, f2H);
        }
        if (f2H.containsKey(path)) {
            throw new IllegalArgumentException("Already listening to " + path); // NOI18N
        }
        f2H.put(path, new Holder(listener, path));
    }
    
    /**
     * Remove a listener to changes in a given path.
     */
    public void removeListener(FileChangeSupportListener listener, File path) {
        assert path.equals(FileUtil.normalizeFile(path)) : "Need to normalize " + path + " before passing to FCS!";
        Map<File,Holder> f2H = holders.get(listener);
        if (f2H == null) {
            throw new IllegalArgumentException("Was not listening to " + path); // NOI18N
        }
        if (!f2H.containsKey(path)) {
            throw new IllegalArgumentException("Was not listening to " + path); // NOI18N
        }
        f2H.remove(path);
    }
    
    private static final class Holder extends WeakReference<FileChangeSupportListener> implements FileChangeListener, Runnable {
        
        private final File path;
        private FileObject current;
        private File currentF;
        
        public Holder(FileChangeSupportListener listener, File path) {
            super(listener, Utilities.activeReferenceQueue());
            assert path != null;
            this.path = path;
            locateCurrent();
        }
        
        private void locateCurrent() {
            FileObject oldCurrent = current;
            currentF = path;
            while (true) {
                try {
                    current = FileUtil.toFileObject(currentF);
                } catch (IllegalArgumentException x) {
                    // #73526: was originally normalized, but now is not. E.g. file changed case.
                    currentF = FileUtil.normalizeFile(currentF);
                    current = FileUtil.toFileObject(currentF);
                }
                if (current != null) {
                    break;
                }
                currentF = currentF.getParentFile();
                if (currentF == null) {
                    // #47320: can happen on Windows in case the drive does not exist.
                    // (Inside constructor for Holder.) In that case skip it.
                    return;
                }
            }
            // XXX what happens with UNC paths?
            assert current != null;
            if (current != oldCurrent) {
                if (oldCurrent != null) {
                    oldCurrent.removeFileChangeListener(this);
                }
                current.addFileChangeListener(this);
                current.getChildren();//to get events about children
            }
        }

        private void someChange(FileObject modified) {
            FileChangeSupportListener listener;
            FileObject oldCurrent, nueCurrent;
            File oldCurrentF, nueCurrentF;
            synchronized (this) {
                if (current == null) {
                    return;
                }
                listener = get();
                if (listener == null) {
                    return;
                }
                oldCurrent = current;
                oldCurrentF = currentF;
                locateCurrent();
                nueCurrent = current;
                nueCurrentF = currentF;
            }
            if (modified != null && modified == nueCurrent) {
                FileChangeSupportEvent event = new FileChangeSupportEvent(DEFAULT, FileChangeSupportEvent.EVENT_MODIFIED, path);
                listener.fileModified(event);
            } else {
                boolean oldWasCorrect = path.equals(oldCurrentF);
                boolean nueIsCorrect = path.equals(nueCurrentF);
                if (oldWasCorrect && !nueIsCorrect) {
                    FileChangeSupportEvent event = new FileChangeSupportEvent(DEFAULT, FileChangeSupportEvent.EVENT_DELETED, path);
                    listener.fileDeleted(event);
                } else if (nueIsCorrect && !oldWasCorrect) {
                    FileChangeSupportEvent event = new FileChangeSupportEvent(DEFAULT, FileChangeSupportEvent.EVENT_CREATED, path);
                    listener.fileCreated(event);
                }
            }
        }

        public void fileChanged(FileEvent fe) {
            someChange(fe.getFile());
        }
        
        public void fileDeleted(FileEvent fe) {
            someChange(null);
        }

        public void fileDataCreated(FileEvent fe) {
            someChange(null);
        }

        public void fileFolderCreated(FileEvent fe) {
            someChange(null);
        }

        public void fileRenamed(FileRenameEvent fe) {
            someChange(null);
        }
        
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }
        
        public synchronized void run() {
            if (current != null) {
                current.removeFileChangeListener(this);
                current = null;
            }
        }

    }
    
}
