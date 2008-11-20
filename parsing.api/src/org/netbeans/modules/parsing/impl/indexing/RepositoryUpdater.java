/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Tomas Zezula
 */
public class RepositoryUpdater implements PathRegistryListener, FileChangeListener {

    private static RepositoryUpdater instance;

    private static final Logger LOGGER = Logger.getLogger(RepositoryUpdater.class.getName());

    private final Set<URL>scannedRoots = Collections.synchronizedSet(new HashSet<URL>());
    private final Set<URL>scannedBinaries = Collections.synchronizedSet(new HashSet<URL>());
    private final Set<URL>scannedUnknown = Collections.synchronizedSet(new HashSet<URL>());
    private final PathRegistry regs = PathRegistry.getDefault();
    private final PathRecognizerRegistry recognizers = PathRecognizerRegistry.getDefault();
    private volatile State state;
    private volatile Task worker;

    private RepositoryUpdater () {
        init ();
    }

    private synchronized void init () {
        if (state == State.CREATED) {
            regs.addPathRegistryListener(this);
            registerFileSystemListener();
//            submitBatch();
            state = State.INITIALIZED;
        }
    }
    //where
    private void registerFileSystemListener  () {
        FileUtil.addFileChangeListener(this);
    }

    public void close () {
        state = State.CLOSED;
        this.regs.removePathRegistryListener(this);
        this.unregisterFileSystemListener();
    }
    //where
    private void unregisterFileSystemListener () {
        FileUtil.removeFileChangeListener(this);
    }


    private void submit (final Work  work) {
        Task t = getWorker ();
        assert t != null;
        t.schedule (work);
    }
    //where
    private Task getWorker () {
        Task t = this.worker;
        if (t == null) {
            synchronized (this) {
                if (this.worker == null) {
                    this.worker = new Task ();
                }
                t = this.worker;
            }
        }
        return t;
    }

    public void pathsChanged(PathRegistryEvent event) {
        assert event != null;
        final EventKind eventKind = event.getEventKind();
        assert eventKind != null;
        final PathKind pathKind = event.getPathKind();
        assert pathKind != null;
        final Collection<? extends ClassPath> affected = event.getAffectedPaths();
        assert affected != null;
        switch (eventKind) {
            case PATHS_ADDED:
                handlePathsAdded (affected, pathKind);
                break;
            case PATHS_REMOVED:
                handlePathsRemoved (affected, pathKind);
                break;
            case PATHS_CHANGED:
                handlePathsChanged (affected, pathKind);
                break;
            case INCLUDES_CHANGED:
                handleIncludesChanged (affected, pathKind);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }
    
    private void handlePathsAdded (final Collection<? extends ClassPath> affected, final PathKind kind) {
        final Work w = new RootsWork(WorkType.COMPILE_BATCH, affected, kind);
        submit(w);
    }

    private void handlePathsRemoved (final Collection<? extends ClassPath> affected, final PathKind kind) {
        switch (kind) {
            case BINARY:
                removeAll (affected,this.scannedBinaries);
                break;
            case SOURCE:
                removeAll (affected,this.scannedRoots);
                break;
            case UNKNOWN_SOURCE:
                removeAll (affected,this.scannedUnknown);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    //where
    private void removeAll (Collection<? extends ClassPath> affected, Set<URL> from) {
        assert affected != null;
        assert from != null;
        synchronized (from) {
            for (ClassPath cp : affected) {
                for (ClassPath.Entry e : cp.entries()) {
                    from.remove(e.getURL());
                }
            }
        }
    }

    private void handlePathsChanged (final Collection<? extends ClassPath> affected, final PathKind kind) {
        final Work w = new RootsWork(WorkType.COMPILE_BATCH, affected, kind);
        submit(w);
    }

    private void handleIncludesChanged (final Collection<? extends ClassPath> affected, final PathKind kind) {

    }




    public static synchronized RepositoryUpdater getDefault() {
        if (instance == null) {
            instance = new RepositoryUpdater();
        }
        return instance;
    }

    public void fileFolderCreated(FileEvent fe) {
        //In ideal case this should do nothing,
        //but in Netbeans newlly created folder may
        //already contain files
        final FileObject fo = fe.getFile();
        final URL root = getOwningSourceRoot(fo);
        if ( root != null && VisibilityQuery.getDefault().isVisible(fo)) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Folder created: "+FileUtil.getFileDisplayName(fo)+" Owner: " + root);
            }
            final Work w = new SingleFileWork(WorkType.COMPILE,root,fo);
            submit(w);
        }
    }
    
    public void fileDataCreated(FileEvent fe) {
        final FileObject fo = fe.getFile();
        final URL root = getOwningSourceRoot (fo);
        if (root != null &&  VisibilityQuery.getDefault().isVisible(fo) && FileUtil.getMIMEType(fo, recognizers.getMimeTypes())!=null) {
            final Work w = new SingleFileWork(WorkType.COMPILE,root,fo);
            submit(w);
        }
    }

    public void fileChanged(FileEvent fe) {
        final FileObject fo = fe.getFile();
        final URL root = getOwningSourceRoot (fo);
        if (root != null &&  VisibilityQuery.getDefault().isVisible(fo) && FileUtil.getMIMEType(fo, recognizers.getMimeTypes())!=null) {
            final Work w = new SingleFileWork(WorkType.COMPILE,root,fo);
            submit(w);
        }
    }

    public void fileDeleted(FileEvent fe) {
    }

    public void fileRenamed(FileRenameEvent fe) {
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    private URL getOwningSourceRoot (final FileObject fo) {
        if (fo == null) {
            return null;
        }
        List<URL> clone = new ArrayList<URL> (this.scannedRoots);
        for (URL root : clone) {
            FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo != null && FileUtil.isParentOf(rootFo,fo)) {
                return root;
            }
        }
        return null;
    }

    private enum State {CREATED, INITIALIZED, CLOSED};

    private enum WorkType {COMPILE_BATCH, COMPILE};

    private static class Work {
        
        private final WorkType type;

        public Work (final WorkType type) {
            assert type != null;
            this.type = type;
        }

        public WorkType getType () {
            return this.type;
        }
        
    }

    private static class SingleFileWork extends Work {

        private final URL root;
        private final FileObject file;

        public SingleFileWork (final WorkType type, final URL root, final FileObject file) {
            super (type);
            assert root != null;
            assert file != null;
            this.root = root;
            this.file = file;
        }

        public URL getRoot () {
            return root;
        }

        public FileObject getFile () {
            return this.file;
        }

    }

    private static class RootsWork extends Work {
        private final Collection<? extends ClassPath> cps;
        private final PathKind kind;

        
        public RootsWork (final WorkType type, final Collection<? extends ClassPath> cps, final PathKind kind) {
            super (type);
            assert cps != null;
            this.cps = cps;
            assert kind != null;
            this.kind = kind;
        }

        public Collection<? extends ClassPath> getClassPaths() {
            return this.cps;
        }

        public PathKind getPathKind () {
            return this.kind;
        }

    }

    private class Task extends ParserResultTask {

        private final List<Work> todo = new LinkedList<Work>();
        private boolean active;
        
        public synchronized void schedule (Work work) {
            assert work != null;
            todo.add(work);
            if (!active) {
                active = true;
                Utilities.scheduleSpecialTask(this);
            }
        }

        public synchronized int getSubmittedCount () {
            return this.todo.size();
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public Class<? extends Scheduler> getSchedulerClass() {
            return null;
        }

        @Override
        public void cancel() {
            
        }

        @Override
        public void run(Result nil) {
            do {
                final Work work = getWork();
                final WorkType type = work.getType();
                switch (type) {
                    case COMPILE_BATCH:
                        
                        break;
                    case COMPILE:
                        
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            } while (removeWork());
        }
        //where
        private synchronized Work getWork () {
            return todo.get(0);
        }

        private synchronized boolean  removeWork () {
            todo.remove(0);
            final boolean empty = todo.isEmpty();
            if (empty) {
                active = false;
            }
            return !empty;
        }

        private void findDependencies(final URL rootURL, final Stack<URL> cycleDetector, final Map<URL, List<URL>> depGraph, String[] binaryClassPathIds) {
            if (depGraph.containsKey(rootURL)) {
                return;
            }
            final FileObject rootFo = URLMapper.findFileObject(rootURL);
            if (rootFo == null) {
                return;
            }
            cycleDetector.push(rootURL);
            final List<ClassPath> pathToResolve = new ArrayList<ClassPath>(binaryClassPathIds.length);
            for (String binaryClassPathId : binaryClassPathIds) {
                ClassPath cp = ClassPath.getClassPath(rootFo, binaryClassPathId);
                if (cp != null) {
                    pathToResolve.add(cp);
                }
            }
            final List<URL> deps = new LinkedList<URL>();
            for (ClassPath cp : pathToResolve) {
                for (ClassPath.Entry entry : cp.entries()) {
                    final URL url = entry.getURL();
                    final URL[] sourceRoots = regs.sourceForBinaryQuery(url, cp, false);
                    if (sourceRoots != null) {
                        for (URL sourceRoot : sourceRoots) {
                            if (!sourceRoot.equals(rootURL) && !cycleDetector.contains(sourceRoot)) {
                                deps.add(sourceRoot);
                                findDependencies(sourceRoot, cycleDetector, depGraph, binaryClassPathIds);
                            }
                        }
                    }
                }
            }
            depGraph.put(rootURL, deps);
            cycleDetector.pop();
        }

    }

}
