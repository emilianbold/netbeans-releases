/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo.FileType;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.filesystems.FileChangeListener;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vladimir Kvashin
 */
public class RemoteFileObjectFactory {

    private final ExecutionEnvironment env;
    private final RemoteFileSystem fileSystem;

    private final WeakCache<String, RemoteFileObjectBase> fileObjectsCache = new WeakCache<String, RemoteFileObjectBase>();

    /** lock for both fileObjectsCache and pendingListeners */
    private final Object lock = new Object();

    private final Map<String, List<FileChangeListener>> pendingListeners = 
            new HashMap<String, List<FileChangeListener>>();
    
    private final RequestProcessor.Task cleaningTask;
    private static RequestProcessor RP = new RequestProcessor("File objects cache dead entries cleanup", 1); //NOI18N
    private static final int CLEAN_INTERVAL = Integer.getInteger("rfs.cache.cleanup.interval", 10000); //NOI18N

    private int cacheRequests = 0;
    private int cacheHits = 0;

    public RemoteFileObjectFactory(RemoteFileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.env = fileSystem.getExecutionEnvironment();
        cleaningTask = RP.create(new Runnable() {
            @Override
            public void run() {
                cleanDeadEntries();
            }
        });
        scheduleCleanDeadEntries();
    }

    /*package*/ Collection<RemoteFileObjectBase> getCachedFileObjects() {
        return fileObjectsCache.values(); // WeakCache returns a copy => no need to copy here
    }
    
    public RemoteFileObjectBase getCachedFileObject(String path) {
        return fileObjectsCache.get(path);
    }

    private void scheduleCleanDeadEntries() {
        cleaningTask.schedule(CLEAN_INTERVAL);
    }

    private void cleanDeadEntries() {
        boolean trace = RemoteLogger.getInstance().isLoggable(Level.FINEST);
        if (trace)         {
            int size = fileObjectsCache.size();
            if (RemoteLogger.getInstance().isLoggable(Level.FINEST)) {
                RemoteLogger.getInstance().log(Level.FINEST, "Cleaning file objects dead entries for {0} ... {1} entries and {2}% ({3} of {4}) hits so far",
                        new Object[] {env, size, (cacheRequests == 0) ? 0 : ((cacheHits*100)/cacheRequests), cacheHits, cacheRequests});
            }
        }

        fileObjectsCache.cleanDeadEntries();

        if (trace)         {
            int size = fileObjectsCache.size();
            RemoteLogger.getInstance().log(Level.FINEST, "Cleaning file objects dead entries for {0} ... {1} entries left", new Object[] {env, size});
        }

        if (fileObjectsCache.size() > 0) {
            scheduleCleanDeadEntries();
        }
    }

    public RemoteDirectory createRemoteDirectory(RemoteFileObjectBase parent, String remotePath, File cacheFile) {
        cacheRequests++;
        if (fileObjectsCache.size() == 0) {
            scheduleCleanDeadEntries(); // schedule on 1-st request
        }
        RemoteFileObjectBase fo = fileObjectsCache.get(remotePath);
        if (fo instanceof RemoteDirectory && fo.isValid() && fo.getCache().equals(cacheFile)) {
            if (fo.getParent() == parent) {
                cacheHits++;
                return (RemoteDirectory) fo;
            }
            fo = null;
        }
        if (fo != null && parent.isValid()) {
            fo.invalidate();
            fileObjectsCache.remove(remotePath, fo);
        }
        fo = RemoteDirectory.createNew(fileSystem, env, parent, remotePath, cacheFile);
        if (fo.isValid()) {
            RemoteFileObjectBase result = putIfAbsent(remotePath, fo);
            if (result instanceof RemoteDirectory && result.getParent() == parent) {
                return (RemoteDirectory)result;
            }
        }
        return (RemoteDirectory) fo;
    }

    public RemotePlainFile createRemotePlainFile(RemoteDirectory parent, String remotePath, File cacheFile, FileType fileType) {
        cacheRequests++;
        if (fileObjectsCache.size() == 0) {
            scheduleCleanDeadEntries(); // schedule on 1-st request
        }
        RemoteFileObjectBase fo = fileObjectsCache.get(remotePath);
        if (fo instanceof RemotePlainFile && fo.isValid() && fo.getCache().equals(cacheFile)) {
            if (fo.getParent() == parent) {
                cacheHits++;
                return (RemotePlainFile) fo;
            }
            fo = null;
        }
        if (fo != null && parent.isValid()) {
            fo.invalidate();
            fileObjectsCache.remove(remotePath, fo);
        }
        fo = RemotePlainFile.createNew(fileSystem, env, parent, remotePath, cacheFile, fileType);
        if (fo.isValid()) {
            RemoteFileObjectBase result = putIfAbsent(remotePath, fo);
            if (result instanceof RemotePlainFile && result.getParent() == parent) {
                return (RemotePlainFile)result;
            }
        }
        return (RemotePlainFile) fo;
    }

    SpecialRemotePlainFileObject createUnsupportedFile(RemoteDirectory parent, String remotePath, File cacheFile, FileType fileType) {
        cacheRequests++;
        if (fileObjectsCache.size() == 0) {
            scheduleCleanDeadEntries(); // schedule on 1-st request
        }
        RemoteFileObjectBase fo = fileObjectsCache.get(remotePath);
        if (fo instanceof SpecialRemotePlainFileObject && fo.isValid()) {
            if (fo.getParent() == parent) {
                cacheHits++;
                return (SpecialRemotePlainFileObject) fo;
            }
            fo = null;
        }
        if (fo != null && parent.isValid()) {
            fo.invalidate();
            fileObjectsCache.remove(remotePath, fo);
        }
        fo = SpecialRemotePlainFileObject.createNew(fileSystem, env, parent, remotePath, fileType);
        if (fo.isValid()) {
            RemoteFileObjectBase result = putIfAbsent(remotePath, fo);
            if (result instanceof SpecialRemotePlainFileObject && result.getParent() == parent) {
                return (SpecialRemotePlainFileObject)result;
            }
        }
        return (SpecialRemotePlainFileObject) fo;
    }


    public RemoteLink createRemoteLink(RemoteFileObjectBase parent, String remotePath, String link) {
// It was never cached because of incorrect instanceof check                
//        cacheRequests++;
//        if (fileObjectsCache.size() == 0) {
//            scheduleCleanDeadEntries(); // schedule on 1-st request
//        }
//        RemoteFileObjectBase fo = fileObjectsCache.get(remotePath);
//        if (fo instanceof RemotePlainFile && fo.isValid() && fo.getType() == FileType.Symlink) {
//            cacheHits++;
//            return (RemoteLink) fo;
//        }
//        if (fo != null) {
//            fo.invalidate();
//        }
        RemoteLink fo = RemoteLink.createNew(fileSystem, env, parent, remotePath, link);        
        RemoteFileObjectBase result = putIfAbsent(remotePath, fo);
        if (result instanceof RemoteLink) {
            // (result == fo) means that result was placed into cache => we need to init listeners,
            // otherwise there already was an object in cache => listener has been already initialized
            if (result == fo) { 
                ((RemoteLink) result).initListeners();
            }
        }
        return (RemoteLink) result;
    }

    private RemoteFileObjectBase putIfAbsent(String remotePath, RemoteFileObjectBase fo) {
        synchronized (lock) {
            RemoteFileObjectBase prev = fileObjectsCache.get(remotePath);
            if (prev == null) {
                List<FileChangeListener> listeners = pendingListeners.remove(remotePath);
                if (listeners != null) {
                    for (FileChangeListener l : listeners) {
                        fo.addFileChangeListener(l);
                    }
                }
                fileObjectsCache.put(remotePath, fo);
                return fo;
            } else {
                return prev;
            }
        }
    }
    
    public void addFileChangeListener(String path, FileChangeListener listener) {
        RemoteFileObjectBase fo = getCachedFileObject(path);
        if (fo == null) {
            synchronized (lock) {
                fo = getCachedFileObject(path);
                if (fo != null) {
                    List<FileChangeListener> listeners = pendingListeners.get(path);
                    if (listeners == null) {
                        listeners = new ArrayList<FileChangeListener>();
                        pendingListeners.put(path, listeners);
                    }
                    listeners.add(listener);
                }
            }
        } else {
            fo.addFileChangeListener(listener);
        }
    }
    
    public void remoteFileChangeListener(String path, FileChangeListener listener) {
        RemoteFileObjectBase fo = getCachedFileObject(path);
        if (fo == null) {
            synchronized (lock) {
                fo = getCachedFileObject(path);
                if (fo != null) {
                    List<FileChangeListener> listeners = pendingListeners.get(path);
                    if (listeners != null) {
                        listeners.remove(listener);
                    }
                }
            }
        } else {
            fo.removeFileChangeListener(listener);
        }
    }

    /** 
     * Removes file object from cache and invalidates it.
     * @return an invalidated object or null
     */
    public RemoteFileObjectBase invalidate(String remotePath) {
        RemoteFileObjectBase fo = fileObjectsCache.remove(remotePath);
        if (fo != null) {
            fo.invalidate();
        }
        return fo;
    }
    
    public void rename(String path2Rename, String newPath, RemoteFileObjectBase fo2Rename) {
        Collection<RemoteFileObjectBase> toRename = new HashSet<RemoteFileObjectBase>();
        addAllExistingChildren(fo2Rename, toRename);
        for (RemoteFileObjectBase fo : toRename) {
            String curPath = fo.getPath();
            String changedPath = curPath.replaceFirst(path2Rename, newPath);
            fileObjectsCache.remove(curPath, fo);
            fo.renamePath(changedPath);
            putIfAbsent(changedPath, fo);
        }
    }
        
    private void addAllExistingChildren(RemoteFileObjectBase fo, Collection<RemoteFileObjectBase> bag) {
        bag.add(fo);
        if (fo.isFolder()) {
            for (RemoteFileObjectBase child : fo.getExistentChildren()) {
                addAllExistingChildren(child, bag);
            }
        }
    }
    
    public void setLink(RemoteDirectory parent, String linkRemotePath, String linkTarget) {
        RemoteFileObjectBase fo = fileObjectsCache.get(linkRemotePath);
        if (fo != null) {
            if (fo instanceof RemoteLink) {
                ((RemoteLink) fo).setLink(linkTarget, parent);
            } else {
                RemoteLogger.getInstance().log(Level.FINE, "Called setLink on {0} - invalidating", fo.getClass().getSimpleName());
                fo.invalidate();
            }
        }
    }
}
