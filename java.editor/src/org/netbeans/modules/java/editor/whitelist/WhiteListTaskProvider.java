/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.whitelist;

//import com.sun.istack.internal.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.Queries.QueryKind;
//import org.netbeans.modules.parsing.spi.indexing.support.IndexFolder;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.openide.util.WeakSet;

/**
 *
 * @author Tomas Zezula
 */
public class WhiteListTaskProvider extends  PushTaskScanner {

    private static final RequestProcessor WORKER = new RequestProcessor(WhiteListTaskProvider.class);
    private static final Logger LOG = Logger.getLogger(WhiteListTaskProvider.class.getName());
    private static final Set<RequestProcessor.Task> TASKS = new HashSet<RequestProcessor.Task>();
    private static final Map<FileObject, Set<FileObject>> root2FilesWithAttachedErrors = new WeakHashMap<FileObject, Set<FileObject>>();
    private static boolean clearing;


    private volatile TaskScanningScope scope;
    private volatile Callback callback;

    @NbBundle.Messages({
        "LBL_ProviderName=White lists violations",
        "LBL_ProviderDescription=Lists violations of white list rules"
    })
    public WhiteListTaskProvider() {
        super(Bundle.LBL_ProviderName(),
              Bundle.LBL_ProviderDescription(),
              null);
    }

    @Override
    public void setScope(TaskScanningScope scope, Callback callback) {
        cancelAllCurrent();
        this.scope = scope;
        this.callback = callback;

        if (scope == null || callback == null)
            return ;

        for (FileObject file : scope.getLookup().lookupAll(FileObject.class)) {
            enqueue(new Work(file, callback));
        }

        for (Project p : scope.getLookup().lookupAll(Project.class)) {
            for (SourceGroup javaSG : ProjectUtils.getSources(p).getSourceGroups("java")) {    //NOI18N
                enqueue(new Work(javaSG.getRootFolder(), callback));
            }
        }
    }

    private static void enqueue(Work w) {
        synchronized (TASKS) {
            final RequestProcessor.Task task = WORKER.post(w);
            TASKS.add(task);
            task.addTaskListener(new TaskListener() {
                @Override
                public void taskFinished(org.openide.util.Task task) {
                    synchronized (TASKS) {
                        if (!clearing) {
                            TASKS.remove(task);
                        }
                    }
                }
            });
        }
    }

    private static void cancelAllCurrent() {
        synchronized (TASKS) {
            clearing = true;
            try {
                for (final Iterator<RequestProcessor.Task> it =  TASKS.iterator();
                     it.hasNext();) {
                    final RequestProcessor.Task t = it.next();
                    t.cancel();
                    it.remove();
                }
            } finally {
                clearing = false;
            }
        }
        synchronized (root2FilesWithAttachedErrors) {
            root2FilesWithAttachedErrors.clear();
        }
    }

    private static Set<FileObject> getFilesWithAttachedErrors(FileObject root) {
        synchronized (root2FilesWithAttachedErrors) {
            Set<FileObject> result = root2FilesWithAttachedErrors.get(root);
            if (result == null) {
                root2FilesWithAttachedErrors.put(root, result = new WeakSet<FileObject>());
            }
            return result;
        }
    }

    @CheckForNull
    private static Map.Entry<FileObject,Task> createTask(
            @NonNull final FileObject root,
            @NonNull final IndexDocument doc) {
        final FileObject file = root.getFileObject(doc.getPrimaryKey());
        if (file == null) {
            return null;
        }
        final Task task = Task.create(
            file,
            "nb-tasklist-warning",
            doc.getValue(WhiteListIndexerPlugin.MSG),
            Integer.parseInt(doc.getValue(WhiteListIndexerPlugin.LINE)));
        return new Map.Entry<FileObject, Task>() {
            @Override
            public FileObject getKey() {
                return file;
            }
            @Override
            public Task getValue() {
                return task;
            }
            @Override
            public Task setValue(Task value) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @CheckForNull
    private static DocumentIndex getIndex(@NonNull final FileObject root) {
        try {
            final FileObject indexFolder = null; //todo: IndexFolder.getIndexFolderForSourceRoot(root.getURL());
            if (indexFolder != null) {
                final File indexDir = FileUtil.toFile(indexFolder);
                if (indexDir != null) {
                    final File whiteListFolder = new File (indexDir,WhiteListIndexerPlugin.getWhiteListPath());
                    return IndexManager.createDocumentIndex(whiteListFolder);
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return null;
    }

    private static synchronized void updateErrorsInRoot(
            /*@NotNull*/ final Callback callback,
            @NonNull final FileObject root) {
        Set<FileObject> filesWithErrors = getFilesWithAttachedErrors(root);
        Set<FileObject> fixedFiles = new HashSet<FileObject>(filesWithErrors);
        Set<FileObject> nueFilesWithErrors = new HashSet<FileObject>();
        final Map<FileObject,List<Task>> filesToTasks = new HashMap<FileObject,List<Task>>();
        final DocumentIndex index = getIndex(root);
        try {
            for (IndexDocument doc : index.findByPrimaryKey("", QueryKind.PREFIX)) {    //NOI18N
                final Map.Entry<FileObject,Task> task = createTask(root, doc);
                if (task != null) {
                    List<Task> tasks = filesToTasks.get(task.getKey());
                    if (tasks == null) {
                        tasks = new ArrayList<Task>();
                        filesToTasks.put(task.getKey(), tasks);
                    }
                    tasks.add(task.getValue());
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        } catch (InterruptedException e) {
            Exceptions.printStackTrace(e);
        } finally {
            try {
                index.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        for (Map.Entry<FileObject,List<Task>> e : filesToTasks.entrySet()) {
            LOG.log(Level.FINE, "Setting {1} for {0}\n",
                    new Object[] {e.getKey(), e.getValue()});
                    callback.setTasks(e.getKey(), e.getValue());
            if (!fixedFiles.remove(e.getKey())) {
                nueFilesWithErrors.add(e.getKey());
            }
        }
        for (FileObject f : fixedFiles) {
            LOG.log(Level.FINE, "Clearing errors for {0}", f);
            callback.setTasks(f, Collections.<Task>emptyList());
        }
        filesWithErrors.addAll(nueFilesWithErrors);
    }

    private static final class Work implements Runnable {
        private final FileObject fileOrRoot;
        private final Callback callback;

        public Work(FileObject fileOrRoot, Callback callback) {
            this.fileOrRoot = fileOrRoot;
            this.callback = callback;
        }

        @Override
        public void run() {

            LOG.log(Level.FINE, "dequeued work for: {0}", fileOrRoot);
            final ClassPath cp = ClassPath.getClassPath(fileOrRoot, ClassPath.SOURCE);
            if (cp == null) {
                LOG.log(Level.FINE, "cp == null");
                return;
            }
            FileObject root = cp.findOwnerRoot(fileOrRoot);

            if (root == null) {
                Project p = FileOwnerQuery.getOwner(fileOrRoot);

                LOG.log(Level.WARNING,
                        "file: {0} is not on its own source classpath: {1}, project: {2}",
                        new Object[] {
                            FileUtil.getFileDisplayName(fileOrRoot),
                            cp.toString(ClassPath.PathConversionMode.PRINT),
                            p != null ? p.getClass() : "null"
                        });

                return ;
            }

            if (fileOrRoot.isData()) {
                final DocumentIndex index = getIndex(root);
                final List<Task> tasks = new ArrayList<Task>();
                try {
                    for (IndexDocument doc : index.findByPrimaryKey(FileUtil.getRelativePath(root, fileOrRoot), QueryKind.PREFIX)) {    //NOI18N
                        final Map.Entry<FileObject,Task> task = createTask(root, doc);
                        if (task != null) {
                            tasks.add(task.getValue());
                        }
                    }
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                } catch (InterruptedException e) {
                    Exceptions.printStackTrace(e);
                } finally {
                    try {
                        index.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                Set<FileObject> filesWithErrors = getFilesWithAttachedErrors(root);
                if (tasks.isEmpty()) {
                    filesWithErrors.remove(fileOrRoot);
                } else {
                    filesWithErrors.add(fileOrRoot);
                }

                LOG.log(Level.FINE, "setting {1} for {0}", new Object[]{fileOrRoot, tasks});
                callback.setTasks(fileOrRoot, tasks);
            } else {
                updateErrorsInRoot(callback, root);
            }
        }
    }

}
