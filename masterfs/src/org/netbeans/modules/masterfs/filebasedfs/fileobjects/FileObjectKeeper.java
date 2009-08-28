/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory.Caller;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/** Keeps list of fileobjects under given root. Adapted from Jan Lahoda's work
 * in issue 168237
 */
final class FileObjectKeeper implements FileChangeListener {
    private static final Logger LOG = Logger.getLogger(FileObjectKeeper.class.getName());

    private Set<FileObject> kept;
    private Collection<FileChangeListener> listeners;
    private final FolderObj root;
    private long timeStamp;

    public FileObjectKeeper(FolderObj root) {
        this.root = root;
    }

    public synchronized void addRecursiveListener(FileChangeListener fcl) {
        if (listeners == null) {
            listeners = new CopyOnWriteArraySet<FileChangeListener>();
            listenToAll();
        }
        listeners.add(fcl);
    }

    public synchronized void removeRecursiveListener(FileChangeListener fcl) {
        if (listeners == null) {
            return;
        }
        listeners.remove(fcl);
        if (listeners.isEmpty()) {
            listenNoMore();
        }
    }

    public void init(long previous, FileObjectFactory factory, boolean expected) {
        File file = root.getFileName().getFile();
        File[] arr = file.listFiles();
        long ts = 0;
        if (arr != null) {
            for (File f : arr) {
                if (f.isDirectory()) {
                    continue;
                }
                long lm = f.lastModified();
                LOG.log(Level.FINE, "  check {0} for {1}", new Object[] { lm, f });
                if (lm > ts) {
                    ts = lm;
                }
                if (lm > previous && factory != null) {
                    final BaseFileObj prevFO = factory.getCachedOnly(f);
                    if (prevFO == null) {
                        BaseFileObj who = factory.getValidFileObject(f, Caller.Others);
                        LOG.log(Level.FINE, "External change detected {0}", who);
                        who.fireFileChangedEvent(expected);
                    } else {
                        prevFO.refresh(expected, true);
                    }
                }
            }
        }
        timeStamp = ts;
        LOG.log(Level.FINE, "Testing {0}, time {1}", new Object[] { file, timeStamp });
    }

    private final void listenToAll() {
        assert Thread.holdsLock(this);
        assert kept == null;
        kept = new HashSet<FileObject>();
        root.addFileChangeListener(this);
        Enumeration<? extends FileObject> en = root.getChildren(true);
        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            if (fo instanceof FolderObj) {
                FolderObj obj = (FolderObj)fo;
                obj.addFileChangeListener(this);
                kept.add(obj);
                obj.getKeeper();
            }
        }
    }

    private final void listenNoMore() {
        assert Thread.holdsLock(this);

        root.removeFileChangeListener(this);
        Set<FileObject> k = kept;
        if (k != null) {
            for (FileObject fo : k) {
                fo.removeFileChangeListener(this);
            }
            kept = null;
        }
    }

    public void fileFolderCreated(FileEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        if (arr == null) {
            return;
        }
        final FileObject f = fe.getFile();
        if (f instanceof FolderObj) {
            synchronized (this) {
                kept.add(f);
                f.addFileChangeListener(this);
                Enumeration<? extends FileObject> en = f.getChildren(true);
                while (en.hasMoreElements()) {
                    FileObject fo = en.nextElement();
                    if (fo instanceof FolderObj) {
                        fo.addFileChangeListener(this);
                    }
                }
            }
        }
        for (FileChangeListener l : arr) {
            l.fileFolderCreated(fe);
        }
    }

    public void fileDataCreated(FileEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        if (arr == null) {
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileDataCreated(fe);
        }
    }

    public void fileChanged(FileEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        if (arr == null) {
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileChanged(fe);
        }
    }

    public void fileDeleted(FileEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        if (arr == null) {
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileDeleted(fe);
        }
        final FileObject f = fe.getFile();
        if (f instanceof FolderObj) {
            synchronized (this) {
                if (kept != null) {
                    kept.remove(f);
                }
                f.removeFileChangeListener(this);
            }
        }
    }

    public void fileRenamed(FileRenameEvent fe) {
        Collection<FileChangeListener> arr = listeners;
        if (arr == null) {
            return;
        }
        for (FileChangeListener l : arr) {
            l.fileRenamed(fe);
        }
    }

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
        return timeStamp;
    }

}
