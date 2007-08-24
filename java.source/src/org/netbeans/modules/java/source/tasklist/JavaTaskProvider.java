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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.source.tasklist;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.openide.util.WeakSet;

/**
 *
 * @author Stanislav Aubrecht, Jan Lahoda
 */
public final class JavaTaskProvider extends PushTaskScanner {
    
    private static final Logger LOG = Logger.getLogger(JavaTaskProvider.class.getName());
    
    private static JavaTaskProvider INSTANCE;
    
    private TaskScanningScope scope;
    private Callback callback;
    
    public JavaTaskProvider() {
        super( "Java Errors", "Java compiler errors and warnings", null);
        INSTANCE = this;
    }
    
    private synchronized void refreshImpl(FileObject file) {
        LOG.log(Level.FINE, "refresh: {0}", file);
        
        if (scope == null || callback == null)
            return ; //nothing to refresh
        if (!scope.isInScope(file)) {
            if (!file.isFolder())
                return;
            
            //the given file may be a parent of some file that is in the scope:
            for (FileObject inScope : scope.getLookup().lookupAll(FileObject.class)) {
                if (FileUtil.isParentOf(file, inScope)) {
                    enqueue(new Work(inScope, callback));
                }
            }
            
            return ;
        }
        
        LOG.log(Level.FINE, "enqueing work for: {0}", file);
        enqueue(new Work(file, callback));
    }
    
    public static void refresh(FileObject file) {
        if (INSTANCE != null) {
            INSTANCE.refreshImpl(file);
        }
    }
    
    public static void refreshAll() {
        if (INSTANCE != null) {
            synchronized (INSTANCE) {
                INSTANCE.setScope(INSTANCE.scope, INSTANCE.callback);
            }
        }
    }

    @Override
    public synchronized void setScope(TaskScanningScope scope, Callback callback) {
        //cancel all current operations:
        cancelAllCurrent();
        
        this.scope = scope;
        this.callback = callback;
        
        if (scope == null || callback == null)
            return ;
        
        for (FileObject file : scope.getLookup().lookupAll(FileObject.class)) {
            enqueue(new Work(file, callback));
        }
        
        for (Project p : scope.getLookup().lookupAll(Project.class)) {
            for (SourceGroup sg : ProjectUtils.getSources(p).getSourceGroups("java")) {
                enqueue(new Work(sg.getRootFolder(), callback));
            }
        }
    }
    
    private static final Set<RequestProcessor.Task> TASKS = new HashSet<RequestProcessor.Task>();
    private static boolean clearing;
    private static final RequestProcessor WORKER = new RequestProcessor("Java Task Provider");
    private static Map<FileObject, Set<FileObject>> root2FilesWithAttachedErrors = new WeakHashMap<FileObject, Set<FileObject>>();
    
    private static void enqueue(Work w) {
        synchronized (TASKS) {
            final RequestProcessor.Task task = WORKER.post(w);
            
            TASKS.add(task);
            task.addTaskListener(new TaskListener() {
                public void taskFinished(org.openide.util.Task task) {
                    synchronized (TASKS) {
                        if (!clearing) {
                            TASKS.remove(task);
                        }
                    }
                }
            });
            if (task.isFinished()) {
                TASKS.remove(task);
            }
        }
    }
    
    private static void cancelAllCurrent() {
        synchronized (TASKS) {
            clearing = true;
            try {
                for (RequestProcessor.Task t : TASKS) {
                    t.cancel();
                }
                TASKS.clear();
            } finally {
                clearing = false;
            }
        }
        
        synchronized (JavaTaskProvider.class) {
            root2FilesWithAttachedErrors.clear();
        }
        
    }
    
    //only for tests:
    static void waitWorkFinished() throws Exception {
        while (true) {
            RequestProcessor.Task t = null;
            synchronized (TASKS) {
                if (TASKS.isEmpty())
                    return;
                t = TASKS.iterator().next();
            }
            
            t.waitFinished();
        }
    }

    private static Set<FileObject> getFilesWithAttachedErrors(FileObject root) {
        Set<FileObject> result = root2FilesWithAttachedErrors.get(root);
        
        if (result == null) {
            root2FilesWithAttachedErrors.put(root, result = new WeakSet<FileObject>());
        }
        
        return result;
    }
    
    private static synchronized void updateErrorsInRoot(Callback callback, FileObject root) {
        Set<FileObject> filesWithErrors = getFilesWithAttachedErrors(root);
        Set<FileObject> fixedFiles = new HashSet<FileObject>(filesWithErrors);
        Set<FileObject> nueFilesWithErrors = new HashSet<FileObject>();
        
        try {
            if (TasklistSettings.isTasklistEnabled()) {
                for (URL u : TaskCache.getDefault().getAllFilesWithRecord(root.getURL())) {
                    FileObject file = URLMapper.findFileObject(u);
                    
                    if (file != null) {
                        List<Task> result = TaskCache.getDefault().getErrors(file);
                        
                        LOG.log(Level.FINE, "Setting {1} for {0}\n", new Object[] {file, result});
                        
                        callback.setTasks(file, result);
                        
                        if (!fixedFiles.remove(file)) {
                            nueFilesWithErrors.add(file);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        for (FileObject f : fixedFiles) {
            LOG.log(Level.FINE, "Clearing errors for {0}", f);
            callback.setTasks(f, Collections.<Task>emptyList());
        }
        
        filesWithErrors.addAll(nueFilesWithErrors);
    }
    
    private static final class Work implements Runnable {
        private FileObject fileOrRoot;
        private Callback callback;

        public Work(FileObject fileOrRoot, Callback callback) {
            this.fileOrRoot = fileOrRoot;
            this.callback = callback;
        }
        
        public FileObject getFileOrRoot() {
            return fileOrRoot;
        }

        public Callback getCallback() {
            return callback;
        }
        
        public void run() {
            FileObject file = getFileOrRoot();

            LOG.log(Level.FINE, "dequeued work for: {0}", file);

            ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);

            if (cp == null) {
                LOG.log(Level.FINE, "cp == null");
                return;
            }

            FileObject root = cp.findOwnerRoot(file);

            if (file.isData()) {
                List<? extends Task> tasks = TaskCache.getDefault().getErrors(file);
                Set<FileObject> filesWithErrors = getFilesWithAttachedErrors(root);

                if (tasks.isEmpty()) {
                    filesWithErrors.remove(file);
                } else {
                    filesWithErrors.add(file);
                }

                LOG.log(Level.FINE, "setting {1} for {0}", new Object[]{file, tasks});
                getCallback().setTasks(file, tasks);
            } else {
                updateErrorsInRoot(getCallback(), root);
            }
        }
    }
}
