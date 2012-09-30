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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory.Caller;
import org.netbeans.modules.masterfs.watcher.Watcher;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.Exceptions;

/** Keeps list of fileobjects under given root. Adapted from Jan Lahoda's work
 * in issue 168237
 */
final class FileObjectKeeper implements FileChangeListener {
    private static final Logger LOG = Logger.getLogger(FileObjectKeeper.class.getName());
    private static final Object TIME_STAMP_LOCK = new Object();

    private Set<FolderObj> kept;
    private Collection<FileChangeListener> listeners;
    private final FolderObj root;
    //@GuardedBy("TIME_STAMP_LOCK")
    private long timeStamp;

    public FileObjectKeeper(FolderObj root) {
        this.root = root;
    }

    public synchronized void addRecursiveListener(FileChangeListener fcl) {
        if (listeners == null) {
            listeners = new CopyOnWriteArraySet<FileChangeListener>();
        }
        LOG.log(Level.FINEST, "addRecursiveListener for {0} isEmpty: {1}", new Object[]{root, listeners.isEmpty()});
        if (listeners.isEmpty()) {
            Callable<?> stop = null;
            final boolean deepClass = fcl.getClass().getName().equals("org.openide.filesystems.DeepListener"); // NOI18N
            if (fcl instanceof Callable && deepClass) {
                stop = (Callable<?>)fcl;
            }
            FileFilter filter = null;
            if (fcl instanceof FileFilter && deepClass) {
                filter = (FileFilter)fcl;
            }
            
            listenToAll(stop, filter);
        }
        listeners.add(fcl);
    }

    public synchronized void removeRecursiveListener(FileChangeListener fcl) {
        if (listeners == null) {
            return;
        }
        listeners.remove(fcl);
        LOG.log(Level.FINEST, "removeRecursiveListener for {0} isEmpty: {1}", new Object[]{root, listeners.isEmpty()});
        if (listeners.isEmpty()) {
            listenNoMore();
        }
    }
     public List<File> init(long previous, FileObjectFactory factory, boolean expected) {
         boolean recursive;
         synchronized (TIME_STAMP_LOCK) {
             recursive = timeStamp < -1;
             if (timeStamp > 0) {
                 timeStamp = -timeStamp;
             }
             if (timeStamp == 0) {
                 timeStamp = -2;
             }
         }

         File file = Watcher.wrap(root.getFileName().getFile(), root);
         LinkedList<File> arr = new LinkedList<File>();
         long ts = root.getProvidedExtensions().refreshRecursively(file, previous, arr);
         try {
             for (File f : arr) {
                 if (f.isDirectory()) {
                     continue;
                 }
                 long lm = f.lastModified();
                 LOG.log(Level.FINE, "  check {0} for {1}", new Object[] { lm, f });
                 if (lm > ts) {
                     ts = lm;
                 }
                 if (lm > previous && factory != null && !recursive) {
                     final BaseFileObj prevFO = factory.getCachedOnly(f);
                     if (prevFO == null) {
                         BaseFileObj who = factory.getValidFileObject(f, Caller.GetChildern);
                         if (who != null) {
                             LOG.log(Level.FINE, "External change detected {0}", who);  //NOI18N
                             who.fireFileChangedEvent(expected);
                          } else {
                             LOG.log(Level.FINE, "Cannot get valid FileObject. File probably removed: {0}", f);  //NOI18N
                          }
                     } else {
                         LOG.log(Level.FINE, "Do classical refresh for {0}", prevFO);  //NOI18N
                         prevFO.refresh(expected, true);
                      }
                  }
             }
         } catch (StackOverflowError ex) {
             Exceptions.attachMessage(ex, 
                "FileObjectKeeper.init for " + this.root +  // NOI18N
                 " timeStamp: " + timeStamp + " recursive: " + recursive // NOI18N
             ); 
             throw ex;
         }
         synchronized (TIME_STAMP_LOCK) {
             if (!recursive) {
                 timeStamp = ts;
             }
         }
         LOG.log(Level.FINE, "Testing {0}, time {1}", new Object[]{file, timeStamp});
         return arr;
    }
                
     

    private void listenTo(FileObject fo, boolean add, Collection<? super File> children) {
        Set<FolderObj> k;
        if (add) {
            fo.addFileChangeListener(this);
            if (fo instanceof FolderObj) {
                FolderObj folder = (FolderObj)fo;
                folder.getKeeper(children);
                folder.getChildren();
                k = kept;
                if (k != null) {
                    k.add(folder);
                }
            }
            LOG.log(Level.FINER, "Listening to {0}", fo);
        } else {
            fo.removeFileChangeListener(this);
            LOG.log(Level.FINER, "Ignoring {0}", fo);
        }
    }

    private void listenToAll(Callable<?> stop, FileFilter filter) {
        assert Thread.holdsLock(this);
        assert kept == null : "Already listening to " + kept + " now requested for " + root;
        kept = new HashSet<FolderObj>();
        LinkedList<File> it = new LinkedList<File>();
        listenTo(root, true, it);
        FileObjectFactory factory = null;
        for (;;) {
            File f = it.poll();
            LOG.log(Level.FINEST, "listenToAll, processing {0}", f);
            if (f == null || isCyclicSymlink(f)) {
                break;
            }
            if (factory == null) {
                factory = FileObjectFactory.getInstance(f);
            }
            FileObject fo = factory.getValidFileObject(f, Caller.Others);
            LOG.log(Level.FINEST, "listenToAll, check {0} for stop {1}", new Object[] { fo, stop });
            if (fo instanceof FolderObj) {
                FolderObj obj = (FolderObj) fo;
                if (filter != null && !filter.accept(obj.getFileName().getFile())) {
                    continue;
                }
                Object shallStop = null;
                if (stop != null) {
                    try {
                        shallStop = stop.call();
                    } catch (Exception ex) {
                        shallStop = Boolean.TRUE;
                    }
                }
                if (Boolean.TRUE.equals(shallStop)) {
                    LOG.log(Level.INFO, "addRecursiveListener to {0} interrupted", root); // NOI18N
                    return;
                }
                listenTo(obj, true, it);
            }
        }
    }

    private void listenNoMore() {
        assert Thread.holdsLock(this);

        listenTo(root, false, null);
        Set<FolderObj> k = kept;
        if (k != null) {
            for (FolderObj fo : k) {
                listenTo(fo, false, null);
            }
            kept = null;
        }
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        final FileObject folder = fe.getFile();
        if (folder instanceof FolderObj) {
            FolderObj obj = (FolderObj)folder;
            synchronized (this) {
                LinkedList<File> it = new LinkedList<File>();
                listenTo(obj, true, it);
                FileObjectFactory factory = null;
                for (;;) {
                    File f = it.poll();
                    if (f == null) {
                        break;
                    }
                    if (factory == null) {
                        factory = FileObjectFactory.getInstance(f);
                    }
                    FileObject fo = factory.getValidFileObject(f, Caller.Others);
                    if (fo instanceof FolderObj) {
                        listenTo((FolderObj)fo, true, it);
                    }
                }
            }
        }
        if (arr == null || kept == null) {  //#178378 - ignore queued events when no more listening (kept == null)
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileFolderCreated(fe);
        }
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        if (arr == null) {
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileDataCreated(fe);
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        if (arr == null) {
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileChanged(fe);
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        final FileObject f = fe.getFile();
        if (f.isFolder() && fe.getSource() == f && f != root) {
            // there will be another event for parent folder
            return;
        }

        if (f instanceof FolderObj) {
            FolderObj obj = (FolderObj)f;
            synchronized (this) {
                if (kept != null) {
                    kept.remove(obj);
                }
                listenTo(obj, false, null);
            }
        }
        if (arr == null) {
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileDeleted(fe);
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        if (arr == null) {
            return;
        }
        final FileObject f = fe.getFile();
        if (f.isFolder() && fe.getSource() == f && f != root) {
            // there will be another event for parent folder
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileRenamed(fe);
        }
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        if (arr == null) {
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileAttributeChanged(fe);
        }
    }

    long childrenLastModified() {
        return timeStamp == -2 ? 0 : Math.abs(timeStamp);
    }

    boolean isOn() {
        if (kept != null) {
            return true;
        }
        FolderObj obj = root.getExistingParent();
        return obj != null && obj.hasRecursiveListener();
    }

    private static boolean isCyclicSymlink(File f) {
        File p = f.getParentFile();
        for (;;) {
            if (p == null) {
                return false;
            }
            if (p.getName().equals(f.getName())) {
                try {
                    if (f.getCanonicalFile().equals(p.getCanonicalFile())) {
                        return true;
                    }
                } catch (IOException ex) {
                    LOG.log(Level.INFO, "Can't convert to cannonical files {0} and {1}", new Object[]{f, p});
                    LOG.log(Level.FINE, null, ex);
                }
            }
            p = p.getParentFile();
        }
        
    }

}
