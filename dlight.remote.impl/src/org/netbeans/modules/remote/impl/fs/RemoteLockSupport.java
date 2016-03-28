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
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;

/**
 *
 * @author vkvashin
 */
public class RemoteLockSupport {

    private final Object mainLock = new Object();
    private final Map<File, WeakReference<ReadWriteLock>> cacheLocks = new HashMap<>();
    private final IdentityHashMap<RemoteFileObjectBase, RemoteFileLock> fileLocks = new IdentityHashMap();

    private final Object plainFileMainLock = new Object();
    private final Map<RemotePlainFile, SimpleRWLock> plainFileRWLocks = new IdentityHashMap<>();

    private static final int RW_LOCK_TIMEOUT = Integer.getInteger("remote.rwlock.timeout", 4); // NOI18N
    private static final boolean TRACE_LOCKERS;
    static {
        boolean trace = RemoteLogger.isDebugMode();
        String setting = System.getProperty("remote.rwlock.trace.lockers"); //NNOI18N
        if (setting != null) {
            trace = Boolean.parseBoolean(setting);
        }
        TRACE_LOCKERS = trace;
    }

    private AtomicInteger readLocksCount = new AtomicInteger();
    private AtomicInteger writeLocksCount = new AtomicInteger();

    /** 
     * Get read-write lock related to the given file object cache
     */
    public ReadWriteLock getCacheLock(RemoteFileObjectWithCache fo) {
        final File file = fo.getCache();
        synchronized (mainLock) {
            WeakReference<ReadWriteLock> ref = cacheLocks.get(file);
            ReadWriteLock result = (ref == null) ? null : ref.get();
            if (result == null) {
                result = new ReentrantReadWriteLock();
                cacheLocks.put(file, new WeakReference<>(result));
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

    private SimpleRWLock getPlainFileRWLock(RemotePlainFile fo, boolean create) {
        SimpleRWLock lock;
        synchronized (plainFileMainLock) {
            lock = plainFileRWLocks.get(fo);
            if (lock == null && create) {
                lock = TRACE_LOCKERS ? new SimpleRWLockWithTrace(fo) : new SimpleRWLock(fo);
                plainFileRWLocks.put(fo, lock);
            }
        }
        return lock;
    }

    public boolean tryReadLock(RemotePlainFile fo) throws InterruptedException {
        return getPlainFileRWLock(fo, true).tryReadLock();
    }

    public void readUnlock(RemotePlainFile fo) {
        SimpleRWLock lock = getPlainFileRWLock(fo, false);
        if (lock != null) {
            lock.readUnlock();
        }
    }

    public boolean tryWriteLock(RemotePlainFile fo) throws InterruptedException {
        return getPlainFileRWLock(fo, true).tryWriteLock();
    }

    public void writeUnlock(RemotePlainFile fo) {
        SimpleRWLock lock = getPlainFileRWLock(fo, false);
        if (lock != null) {
            lock.writeUnlock();
        }
    }

    void printStatistics(RemoteFileSystem fs) {
        System.err.println("RemoteLockSupport statistics for " + fs);
        System.err.println("Read locks count:  " + readLocksCount.get());
        System.err.println("Write locks count: " + writeLocksCount.get());
    }

    // This homemade Read-Write lock is used instead of ReentrantReadWriteLock to support unlocking from
    // the thread other when one acquired the lock. This is required by FileObjectTestHid.testBigFileAndAsString test.
    // In brief the problem is the following: testBigFileAndAsString checks that if FileObject's InputStream is not closed
    // properly it will be closed in the finalizer. But it is not possible to unlock ReentrantReadWriteLock read lock
    // from the finalizer as it is executed in separate thread: the exception will happen if you try. And this homemade lock
    // do not have this restriction.
    // Some facts about RWL implementation can be found here: http://java.dzone.com/news/java-concurrency-read-write-lo
    private class SimpleRWLock {

        private int activeReaders = 0;
        private int waiters = 0;
        private Thread writer = null;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition readable = lock.newCondition();
        private final Condition writtable = lock.newCondition();
        private final RemotePlainFile owner;

        public SimpleRWLock(RemotePlainFile owner) {
            this.owner = owner;
        }

        private boolean writeCondition() {
            assert lock.isLocked();
            return activeReaders == 0 && writer == null;
        }

        // should support lock's downgrading
        private boolean readCondition() {
            assert lock.isLocked();
            return writer == null || writer == Thread.currentThread();
        }

        private void removeIfPossible() {
            assert lock.isLocked();
            if (activeReaders == 0 && writer == null && waiters == 0) {
                plainFileRWLocks.remove(owner);
            }
        }

        protected void onReadLock(boolean success) {
        }

        protected void onReadUnlock() {
        }

        protected void onWriteLock(boolean success) {
        }

        protected void onWriteUnlock() {
        }

        public Thread getWriter() {
            return writer;
        }

        public RemotePlainFile getOwner() {
            return owner;
        }

        public boolean tryReadLock() throws InterruptedException {
            lock.lock();
            try {
                waiters++;
                while (!readCondition()) {
                    if (!readable.await(RW_LOCK_TIMEOUT, TimeUnit.SECONDS)) {                        
                        onReadLock(false);
                        return false;
                    }
                }
                activeReaders++;
                if (writer == Thread.currentThread()) {
                    writer = null;
                }
                readLocksCount.incrementAndGet();
                onReadLock(true);
                return true;
            } finally {
                waiters--;
                lock.unlock();
            }
        }

        public void readUnlock() {
            lock.lock();
            try {
                waiters++;
                if (activeReaders > 0) {
                    activeReaders--;
                    if (activeReaders == 0) {
                        writtable.signalAll();
                    }
                }
                onReadUnlock();
            } finally {
                waiters--;
                removeIfPossible();
                lock.unlock();
            }
        }

        public boolean tryWriteLock() throws InterruptedException {
            lock.lock();
            try {
                while (!writeCondition()) {
                    if (!writtable.await(RW_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                        onWriteLock(false);
                        return false;
                    }
                }
                writer = Thread.currentThread();
                writeLocksCount.incrementAndGet();
                onWriteLock(true);
                return true;
            } finally {                
                lock.unlock();
            }
        }

        public void writeUnlock() {
            lock.lock();
            try {
                if (writer != null) {
                    writer = null;
                    writtable.signal();
                    readable.signalAll();
                }
            } finally {
                removeIfPossible();
                lock.unlock();
            }
        }
    }

    private static class ReaderInfo {
        public final Thread thread;
        public final StackTraceElement[] lockedStack;
        public final long timestamp;
        public ReaderInfo next;
        public ReaderInfo(ReaderInfo next) {
            this.next = next;
            this.thread = Thread.currentThread();
            this.timestamp = System.currentTimeMillis();
            StackTraceElement[] stack = thread.getStackTrace();
            this.lockedStack = new StackTraceElement[stack.length-4];
            System.arraycopy(stack, 4, lockedStack, 0, stack.length-4);
        }
    }

    private class SimpleRWLockWithTrace extends SimpleRWLock {

        // Is accessed from onReadLock, onReadUnLock, onWriteLock, onWriteUnlock -
        // all these are called under parent's lock => no need to sync
        private ReaderInfo lastReader;

        public SimpleRWLockWithTrace(RemotePlainFile owner) {
            super(owner);
        }

        private void addReader() {
            lastReader = new ReaderInfo(lastReader);
        }

        private void removeReader(ReaderInfo reader) {
            if (reader == lastReader) {
                lastReader = lastReader.next;
            } else {
                ReaderInfo prev = lastReader;
                while (prev != null) {
                    ReaderInfo next = prev.next;
                    if (next == reader) {
                        prev.next = next.next;
                    }
                    prev = next;
                }
            }
        }

        private ReaderInfo findReader() {
            ReaderInfo curr = lastReader;
            while (curr != null) {
                if (curr.thread.getId() == Thread.currentThread().getId()) {
                    return curr;
                }
                curr = curr.next;
            }
            return lastReader;
        }

        @Override
        protected void onReadLock(boolean success) {
            if (success) {
                addReader();
            }
        }

        @Override
        protected void onReadUnlock() {
            ReaderInfo reader = findReader();
            if (reader != null) {
                removeReader(reader);
            }
        }

        @Override
        protected void onWriteLock(boolean success) {
            if (!success) {
                Exception e = new Exception("Failed to lock " + getOwner() + " at " + new Date()); // NOI18N
                e.printStackTrace(System.err);
                System.err.println("Readers are:");
                ReaderInfo reader = lastReader;
                while (reader != null) {
                    System.err.println("Locked at " + new Date(reader.timestamp) + " by thread " + reader.thread.getName());
                    for (StackTraceElement ste : reader.lockedStack) {
                        System.err.println("    at " + ste.toString());
                    }                    
                    StackTraceElement[] stack = reader.thread.getStackTrace();
                    if (stack.length > 0) {
                        System.err.println("    Now the reader thead stack is");
                        for (StackTraceElement ste : stack) {
                            System.err.println("        at " + ste.toString());
                        }
                    } else {
                        System.err.println("    No reader stack at the moment");
                    }
                    reader = reader.next;
                }
            }
        }
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
