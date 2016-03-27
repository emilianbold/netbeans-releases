/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;

/**
 *
 * @author vkvashin
 */
public class RemoteLockSupport {

    private final Object mainLock = new Object();
    private final Map<File, WeakReference<ReadWriteLock>> directoryLocks = new HashMap<>();
    private final IdentityHashMap<RemoteFileObjectBase, RemoteFileLock> fileLocks = new IdentityHashMap();

    /** 
     * Get read-write lock related to the given file object cache
     */
    public ReadWriteLock getCacheLock(RemoteFileObjectWithCache fo) {
        final File file = fo.getCache();
        synchronized (mainLock) {
            WeakReference<ReadWriteLock> ref = directoryLocks.get(file);
            ReadWriteLock result = (ref == null) ? null : ref.get();
            if (result == null) {
                result = new ReentrantReadWriteLock();
                directoryLocks.put(file, new WeakReference<>(result));
            }
            return result;
        }
    }

    public FileLock lock(RemoteFileObjectBase fo) throws FileAlreadyLockedException {
        RemoteFileLock lock;
        synchronized (mainLock) {
            lock = fileLocks.get(fo);
            if (lock != null && lock.isValid()) {
                throw new FileAlreadyLockedException(fo.getPath());
            }
            lock = new RemoteFileLock(fo);
            fileLocks.put(fo, lock);
        }
        return lock;
    }

    public boolean isLocked(RemoteFileObjectBase fo) {
        synchronized (mainLock) {
            RemoteFileLock lock = fileLocks.get(fo);
            return lock != null && lock.isValid();
        }
    }

    public boolean checkLock(RemoteFileObjectBase fo, FileLock aLock) {
        if (aLock != null) {
            synchronized (mainLock) {
                RemoteFileLock lock = fileLocks.get(fo);
                return lock == aLock;
            }
        }
        return true;
    }

    private class RemoteFileLock extends FileLock {

        private final RemoteFileObjectBase fo;

        public RemoteFileLock(RemoteFileObjectBase fo) {
            this.fo = fo;
        }

        @Override
        public void releaseLock() {
            synchronized (mainLock) {
                super.releaseLock();
                fileLocks.remove(fo);
            }
        }
    }
}
