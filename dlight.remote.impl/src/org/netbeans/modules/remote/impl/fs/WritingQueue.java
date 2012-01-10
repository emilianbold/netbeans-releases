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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Vladimir Kvashin
 */
public class WritingQueue {

    private static final Map<ExecutionEnvironment, WritingQueue> instances = new HashMap<ExecutionEnvironment, WritingQueue>();
    private static final Logger LOGGER = Logger.getLogger("cnd.remote.writing.queue.logger"); // NOI18N

    private final ExecutionEnvironment execEnv;
    private final Progress progress;
    
    /** maps remote file name to entry */
    private final Map<String, Entry> entries = new HashMap<String, Entry>();

    private final Object lock = new Object();

    private final Set<String> failed = new HashSet<String>();
    private final Object monitor = new Object();
    
    public WritingQueue(ExecutionEnvironment env) {
        this.execEnv = env;
        this.progress = new Progress(env);
    }

    public static WritingQueue getInstance(ExecutionEnvironment env) {
        WritingQueue instance;
        synchronized (WritingQueue.class) {
            instance = instances.get(env);
            if (instance == null)  {
                instance = new WritingQueue(env);
                instances.put(env, instance);
            }
        }
        return instance;
    }

    public void add(RemotePlainFile fo) {
        String dstFileName = fo.getPath();
        LOGGER.log(Level.FINEST, "WritingQueue: adding file {0}", dstFileName); //NOI18N
        synchronized (lock) {
            Entry entry = entries.get(dstFileName);
            if (entry == null) {
                entry = new Entry(fo);
                entries.put(dstFileName, entry);
            }
            entry.scheduleUpload();
            progress.entryAdded(entries.size());
        }
    }

    // TODO: persistence! - otherwise after IDE restart we can forget about not synchronized files
    // TODO: where should the storage be? probably somewhere in in rfs caches
    // TODO: re-queue failed files?
    // TODO: what if a file changed on remote host?

    private boolean entriesEmpty(Collection<FileObject> filesToWait, Collection<String> failedFiles) {
        synchronized (lock) {
            if(entries.isEmpty()) {
                failedFiles.clear();
                failedFiles.addAll(failed);
                return true;
            } else {
                if (filesToWait.isEmpty()) {
                    return false;
                } else {
                    for (FileObject fo : filesToWait) {
                        if (entries.containsKey(fo.getPath())) {
                            return false;
                        }
                    }
                    return true;
                }                
            }
        }
    }

    public boolean isBusy() {
        synchronized (lock) {
            return ! entries.isEmpty();
        }
    }

    public boolean waitFinished(Collection<String> failedFiles) throws InterruptedException {
        if (failedFiles == null) {
            failedFiles = new ArrayList<String>();
        }
        while (true) {
            if (entriesEmpty(Collections.<FileObject>emptyList(), failedFiles)) {
                if (entries.isEmpty()) {
                    break;
                }
            }
            synchronized (monitor) {
                monitor.wait();
            }
        }
        return failedFiles.isEmpty();
    }

    public boolean waitFinished(Collection<FileObject> filesToWait, Collection<String> failedFiles) throws InterruptedException {
        if (failedFiles == null) {
            failedFiles = new ArrayList<String>();
        }
        while (true) {
            if (entriesEmpty(filesToWait, failedFiles)) {
                if (entries.isEmpty()) {
                    break;
                }
            }
            synchronized (monitor) {
                monitor.wait();
            }
        }
        return failedFiles.isEmpty();
    }
    
    private class Entry implements ChangeListener {

        private volatile Future<UploadStatus> currentTask;
        private boolean reschedule;
        
        private final RemotePlainFile fo;

        public Entry(RemotePlainFile fo) {
            this.reschedule = false;
            this.fo = fo;
        }
        
        private void scheduleUpload() {
            synchronized (lock) {
                if (currentTask == null) {
                    CommonTasksSupport.UploadParameters params = new CommonTasksSupport.UploadParameters(
                            fo.getCache(), execEnv, fo.getPath(), -1, false, this);
                    currentTask = CommonTasksSupport.uploadFile(params);
                } else {
                    // cancel does not work with jsch sftp, reasons to be investigated                    
                    RemoteLogger.getInstance().log(Level.FINE, "Will reschedule previous upload task for {0}", fo);
                    //currentTask.cancel(true);
                    reschedule = true;
                }
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            Object source = e.getSource();
            if (!(source instanceof Future)) {
                RemoteLogger.assertTrue(false, "Wrong class, should be Future<Integer>: " + (source == null ? "null" : source.getClass())); //NOI18N
                return;
            }
            try {
                taskFinished((Future<UploadStatus>) source);
            } finally {
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            }
        }

        private void taskFinished(Future<UploadStatus> finishedTask) {
            LOGGER.log(Level.FINEST, "WritingQueue: Task {0} at {1} finished", new Object[]{finishedTask, execEnv});
            synchronized (lock) {
                if (currentTask != null && currentTask != finishedTask) {
                    // currentTask can contain either null or the last task
                    // so the finishedTask is one of previous tasks - ignore
                    return;
                }
                currentTask = null;
                if (reschedule) {
                    synchronized (lock) {
                        reschedule = false;
                        scheduleUpload();
                    }
                    return;
                }
                try {
                    UploadStatus uploadStatus = finishedTask.get();
                    if (uploadStatus.isOK()) {
                        LOGGER.log(Level.FINEST, "WritingQueue: uploading {0} succeeded", fo);
                        failed.remove(fo.getPath()); // paranoia                        
                        fo.getParent().updateStat(fo, uploadStatus.getStatInfo());
                    } else {
                        LOGGER.log(Level.FINEST, "WritingQueue: uploading {0} failed", fo);
                        failed.add(fo.getPath());
                        fo.setPendingRemoteDelivery(false);
                    }
                    fo.getParent();                                        
                } catch (InterruptedException ex) {
                    // don't report InterruptedException
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex); // should never be the case - the task is done
                } finally {
                    entries.remove(fo.getPath());
                }
                progress.entryDone(entries.size());
            }
        }
    }
    
    private static class Progress {

        private ProgressHandle progressHandle;
        private int progressCurrent;
        private int progressTotal;

        private final ExecutionEnvironment env;

        public Progress(ExecutionEnvironment env) {
            this.env = env;
            String msg=NbBundle.getMessage(WritingQueue.class, "WritingQueueProgressTitle", env.getDisplayName());
        }

        public synchronized  void entryAdded(int entriesCount) {
            if (progressHandle == null) {
                progressHandle = createProgress();
                progressTotal =  0;
                progressCurrent = 0;
                progressHandle.start();
            } else {
                if (progressTotal < entriesCount/2) {                    
                    if (progressTotal == 0) {
                        progressTotal = entriesCount;
                        progressHandle.switchToDeterminate(progressCurrent);
                    } else {
                        progressTotal = entriesCount;
                        progressHandle.finish();
                        progressHandle = createProgress();
                        progressHandle.start(progressTotal);
                    }
                }
            }
        }
                
        public synchronized void entryDone(int entriesCount) {
            progressCurrent++;
            if (progressHandle != null) { // paranoya
                if (progressTotal > 0 && progressCurrent <= progressTotal) {
                    progressHandle.progress(Math.min(progressCurrent, progressTotal));
                }
                if (entriesCount == 0) {
                    progressHandle.finish();
                    progressHandle = null;
                }
            }
        }
        
        private ProgressHandle createProgress() {
            String msg=NbBundle.getMessage(WritingQueue.class, "WritingQueueProgressTitle", env.getDisplayName());                
            return ProgressHandleFactory.createHandle(msg);
        }
    }        
}
