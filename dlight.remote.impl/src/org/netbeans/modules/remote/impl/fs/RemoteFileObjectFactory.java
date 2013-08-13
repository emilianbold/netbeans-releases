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
import org.netbeans.modules.dlight.libs.common.PathUtilities;
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

    /** lockImpl for both fileObjectsCache and pendingListeners */
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
    
    /**
     * Path <code>path</path> will be normalized as we will keep in cache only normalized paths as a key
     * @param path
     * @return 
     */
    public RemoteFileObjectBase getCachedFileObject(String path) {
        String normalizedPath = PathUtilities.normalizeUnixPath(path);
        return fileObjectsCache.get(normalizedPath);
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

    public void changeImplementor(RemoteDirectory parent, DirEntry oldEntry, DirEntry newEntry) {        
        String path = parent.getPath() + '/' + oldEntry.getName();
        synchronized (lock) {
            RemoteFileObject owner = invalidate(path);
            RemoteFileObjectBase newImpl = createFileObject(parent, newEntry, owner);
        }
    }

    public RemoteFileObjectBase createFileObject(RemoteDirectory parent, DirEntry entry) {
        return createFileObject(parent, entry, null);
    }
    
    public RemoteFileObjectBase createFileObject(RemoteDirectory parent, DirEntry entry, RemoteFileObject owner) {
        File childCache = new File(parent.getCache(), entry.getCache());
        String childPath = parent.getPath() + '/' + entry.getName();
        RemoteFileObjectBase fo;
        if (entry.isDirectory()) {
            fo = createRemoteDirectory(parent, childPath, childCache, owner);
        }  else if (entry.isLink()) {
            fo = createRemoteLink(parent, childPath, entry.getLinkTarget(), owner);
        } else if (entry.isPlainFile()) {
            fo = createRemotePlainFile(parent, childPath, childCache, FileType.Regular, owner);
        } else {
            fo = createSpecialFile(parent, childPath, childCache, entry.getFileType(), owner);
        }
        return fo;
    }

    public RemoteFileObjectBase register(RemoteFileObjectBase fo) {
        return putIfAbsent(fo.getPath(), fo);
    }

    private RemoteFileObjectBase createRemoteDirectory(RemoteFileObjectBase parent, String remotePath, File cacheFile, RemoteFileObject owner) {
        cacheRequests++;
        if (fileObjectsCache.size() == 0) {
            scheduleCleanDeadEntries(); // schedule on 1-st request
        }
        String normalizedRemotePath = PathUtilities.normalizeUnixPath(remotePath);
        RemoteFileObjectBase fo = fileObjectsCache.get(normalizedRemotePath);
        if (fo instanceof RemoteDirectory && fo.isValid() && fo.getCache().equals(cacheFile)) {
            if (fo.getParent() == parent) {
                cacheHits++;
                return (RemoteDirectory) fo;
            }
            fo = null;
        }
        if (fo != null && parent.isValid()) {
            fo.invalidate();
            fileObjectsCache.remove(normalizedRemotePath, fo);
        }
        if (owner == null) {
            owner = new RemoteFileObject(fileSystem);
        }
        fo = new RemoteDirectory(owner, fileSystem, env, parent, normalizedRemotePath, cacheFile);
        if (fo.isValid()) {
            RemoteFileObjectBase result = putIfAbsent(normalizedRemotePath, fo);
            if (result instanceof RemoteDirectory && result.getParent() == parent) {
                return (RemoteDirectory)result;
            }
        }
        return fo;
    }

    private RemoteFileObjectBase createRemotePlainFile(RemoteDirectory parent, String remotePath, File cacheFile, FileType fileType, RemoteFileObject owner) {
        cacheRequests++;
        if (fileObjectsCache.size() == 0) {
            scheduleCleanDeadEntries(); // schedule on 1-st request
        }
        String normalizedRemotePath = PathUtilities.normalizeUnixPath(remotePath);
        RemoteFileObjectBase fo = fileObjectsCache.get(normalizedRemotePath);
        if (fo instanceof RemotePlainFile && fo.isValid() && fo.getCache().equals(cacheFile)) {
            if (fo.getParent() == parent) {
                cacheHits++;
                return (RemotePlainFile) fo;
            }
            fo = null;
        }
        if (fo != null && parent.isValid()) {
            fo.invalidate();
            fileObjectsCache.remove(normalizedRemotePath, fo);
        }
        if (owner == null) {
            owner = new RemoteFileObject(fileSystem);
        }
        fo = new RemotePlainFile(owner, fileSystem, env, parent, normalizedRemotePath, cacheFile, fileType);
        if (fo.isValid()) {
            RemoteFileObjectBase result = putIfAbsent(normalizedRemotePath, fo);
            if (result instanceof RemotePlainFile && result.getParent() == parent) {
                return (RemotePlainFile)result;
            }
        }
        return fo;
    }

    private RemoteFileObjectBase createSpecialFile(RemoteDirectory parent, String remotePath, File cacheFile, FileType fileType, RemoteFileObject owner) {
        cacheRequests++;
        if (fileObjectsCache.size() == 0) {
            scheduleCleanDeadEntries(); // schedule on 1-st request
        }
        String normalizedRemotePath = PathUtilities.normalizeUnixPath(remotePath);
        RemoteFileObjectBase fo = fileObjectsCache.get(normalizedRemotePath);
        if (fo instanceof SpecialRemoteFileObject && fo.isValid()) {
            if (fo.getParent() == parent) {
                cacheHits++;
                return (SpecialRemoteFileObject) fo;
            }
            fo = null;
        }
        if (fo != null && parent.isValid()) {
            fo.invalidate();
            fileObjectsCache.remove(normalizedRemotePath, fo);
        }
        if (owner == null) {
            owner = new RemoteFileObject(fileSystem);
        }
        fo = new SpecialRemoteFileObject(owner, fileSystem, env, parent, normalizedRemotePath, fileType);
        if (fo.isValid()) {
            RemoteFileObjectBase result = putIfAbsent(normalizedRemotePath, fo);
            if (result instanceof SpecialRemoteFileObject && result.getParent() == parent) {
                return (SpecialRemoteFileObject)result;
            }
        }
        return fo;
    }


    private RemoteFileObjectBase createRemoteLink(RemoteFileObjectBase parent, String remotePath, String link, RemoteFileObject owner) {
        if (owner == null) {
            owner = new RemoteFileObject(fileSystem);
        }
        String normalizedRemotePath = PathUtilities.normalizeUnixPath(remotePath);
        RemoteLink fo = new RemoteLink(owner, fileSystem, env, parent, normalizedRemotePath, link);
        RemoteFileObjectBase result = putIfAbsent(normalizedRemotePath, fo);
        if (result instanceof RemoteLink) {
            // (result == fo) means that result was placed into cache => we need to init listeners,
            // otherwise there already was an object in cache => listener has been already initialized
            if (result == fo) { 
                ((RemoteLink) result).initListeners(true);
            }
        }
        return result;
    }

    public RemoteFileObjectBase createRemoteLinkChild(RemoteLinkBase parent, String remotePath, RemoteFileObjectBase delegate) {
        String normalizedRemotePath = PathUtilities.normalizeUnixPath(remotePath);
        RemoteLinkChild fo = new RemoteLinkChild(new RemoteFileObject(fileSystem), fileSystem, env, parent, normalizedRemotePath, delegate);
        RemoteFileObjectBase result = putIfAbsent(normalizedRemotePath, fo);
        if (result instanceof RemoteLinkChild) {
            // (result == fo) means that result was placed into cache => we need to init listeners,
            // otherwise there already was an object in cache => listener has been already initialized
            if (result == fo) { 
                ((RemoteLinkChild) result).initListeners(true);
            } else {
                RemoteFileObjectBase oldDelegate = ((RemoteLinkChild) result).getCanonicalDelegate();
                if (oldDelegate != delegate) {
                    // delegate has changed     
                    RemoteFileObject ownerFileObject = result.getOwnerFileObject();
                    result.invalidate();
                    fileObjectsCache.remove(normalizedRemotePath, result);
                    // recreate
                    fo = new RemoteLinkChild(ownerFileObject, fileSystem, env, parent, normalizedRemotePath, delegate);
                    result = putIfAbsent(normalizedRemotePath, fo);
                    if (result == fo) {
                        ((RemoteLinkChild) result).initListeners(true); // fo.initListeners() is quite the same :)
                    }
                    // TODO: is it possible that somebody has just placed another one? of different kind?
                }
            }
        }
        return result;
    }

    /**
     *
     * @param remotePath the path should be normalized
     * @param fo
     * @return
     */
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
        String normalizedPath = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase fo = getCachedFileObject(normalizedPath);
        if (fo == null) {
            synchronized (lock) {
                fo = getCachedFileObject(normalizedPath);
                if (fo != null) {
                    List<FileChangeListener> listeners = pendingListeners.get(normalizedPath);
                    if (listeners == null) {
                        listeners = new ArrayList<FileChangeListener>();
                        pendingListeners.put(normalizedPath, listeners);
                    }
                    listeners.add(listener);
                }
            }
        } else {
            fo.addFileChangeListener(listener);
        }
    }
    
    public void removeFileChangeListener(String path, FileChangeListener listener) {
        String normalizedPath = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase fo = getCachedFileObject(normalizedPath);
        if (fo == null) {
            synchronized (lock) {
                fo = getCachedFileObject(normalizedPath);
                if (fo != null) {
                    List<FileChangeListener> listeners = pendingListeners.get(normalizedPath);
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
    public RemoteFileObject invalidate(String remotePath) {
        String normalizedRemotePath = PathUtilities.normalizeUnixPath(remotePath);
        RemoteFileObjectBase fo = fileObjectsCache.remove(normalizedRemotePath);
        if (fo != null) {
            fo.invalidate();
            return fo.getOwnerFileObject();
        }
        return null;
    }
    
    public void rename(String path2Rename, String newPath, RemoteFileObjectBase fo2Rename) {
        String normalizedPath2Rename = PathUtilities.normalizeUnixPath(path2Rename);
        String normalizedNewPath = PathUtilities.normalizeUnixPath(newPath);
        Collection<RemoteFileObjectBase> toRename = new HashSet<RemoteFileObjectBase>();
        addAllExistingChildren(fo2Rename, toRename);
        for (RemoteFileObjectBase fo : toRename) {
            String curPath = fo.getPath();
            String changedPath = curPath.replaceFirst(normalizedPath2Rename, normalizedNewPath);
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
        String normalizedPath = PathUtilities.normalizeUnixPath(linkRemotePath);
        RemoteFileObjectBase fo = fileObjectsCache.get(normalizedPath);
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
