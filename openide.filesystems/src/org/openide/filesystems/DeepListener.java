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

package org.openide.filesystems;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class DeepListener extends WeakReference<FileChangeListener>
implements FileChangeListener, Runnable, Callable<Boolean> {
    private final File path;
    private FileObject watching;
    private boolean removed;
    private final Callable<Boolean> stop;
    private static List<DeepListener> keep = new ArrayList<DeepListener>();

    DeepListener(FileChangeListener listener, File path, Callable<Boolean> stop) {
        super(listener, Utilities.activeReferenceQueue());
        this.path = path;
        this.stop = stop;
        relisten();
        keep.add(this);
    }

    public void run() {
        FileObject fo = FileUtil.toFileObject(path);
        if (fo != null) {
            fo.removeRecursiveListener(this);
        }
        removed = true;
        keep.remove(this);
    }

    private synchronized void relisten() {
        FileObject fo = FileUtil.toFileObject(path);
        if (fo == watching) {
            return;
        }
        if (watching != null) {
            watching.removeRecursiveListener(this);
            watching = null;
        }
        if (fo != null) {
            watching = fo;
            fo.addRecursiveListener(this);
        }
    }

    public void fileRenamed(FileRenameEvent fe) {
        fileRenamed(fe, false);
    }
    public void fileRenamed(FileRenameEvent fe, boolean fromHolder) {
        relisten();
        FileChangeListener listener = get(fe, fromHolder);
        if (listener == null) {
            return;
        }
        listener.fileRenamed(fe);
    }

    public void fileFolderCreated(FileEvent fe) {
        relisten();
        fileFolderCreated(fe, false);
    }
    public void fileFolderCreated(FileEvent fe, boolean fromHolder) {
        relisten();
        FileChangeListener listener = get(fe, fromHolder);
        if (listener == null) {
            return;
        }
        listener.fileFolderCreated(fe);
    }

    public void fileDeleted(FileEvent fe) {
        fileDeleted(fe, false);
    }
    public void fileDeleted(FileEvent fe, boolean fromHolder) {
        relisten();
        FileChangeListener listener = get(fe, fromHolder);
        if (listener == null) {
            return;
        }
        listener.fileDeleted(fe);
    }

    public void fileDataCreated(FileEvent fe) {
        fileDataCreated(fe, false);
    }
    public void fileDataCreated(FileEvent fe, boolean fromHolder) {
        relisten();
        FileChangeListener listener = get(fe, fromHolder);
        if (listener == null) {
            return;
        }
        listener.fileDataCreated(fe);
    }

    public void fileChanged(FileEvent fe) {
        fileChanged(fe, false);
    }
    public void fileChanged(FileEvent fe, boolean fromHolder) {
        FileChangeListener listener = get(fe, fromHolder);
        if (listener == null) {
            return;
        }
        listener.fileChanged(fe);
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        FileChangeListener listener = get(fe, false);
        if (listener == null) {
            return;
        }
        listener.fileAttributeChanged(fe);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DeepListener other = (DeepListener) obj;
        FileChangeListener thisListener = get();
        FileChangeListener otherListener = other.get();
        if (thisListener != otherListener && (thisListener == null || !thisListener.equals(otherListener))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        FileChangeListener thisListener = get();
        int hash = 7;
        hash = 11 * hash + (thisListener != null ? thisListener.hashCode() : 0);
        return hash;
    }

    private Set<FileEvent> delivered = Collections.synchronizedSet(new WeakSet<FileEvent>());
    private FileChangeListener get(FileEvent fe, boolean fromHolder) {
        if (removed) {
            return null;
        }
        if (fromHolder) {
            if (fe.getFile() != fe.getSource()) {
                return null;
            }
        }
        if (!delivered.add(fe)) {
            return null;
        }
        return get();
    }

    @Override
    public Boolean call() throws Exception {
        return stop != null ? stop.call() : null;
    }
}
