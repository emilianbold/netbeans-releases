/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jumpto.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.search.provider.SearchFilter;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.modules.jumpto.common.Models;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.netbeans.spi.jumpto.file.FileProvider;
import org.netbeans.spi.jumpto.file.FileProviderFactory;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
final class Worker implements Runnable {

    private static final Logger LOG = Logger.getLogger(Worker.class.getName());

    private final Request request;
    private final Strategy strategy;
    private final Collector collector;
    private final long createTime;
    private volatile boolean cancelled;

    private Worker(
            @NonNull final Request request,
            @NonNull final Strategy strategy,
            @NonNull final Collector collector) {
        Parameters.notNull("request", request);     //NOI18N
        Parameters.notNull("strategy", strategy);   //NOI18N
        Parameters.notNull("collector", collector); //NOI18N
        this.request = request;
        this.strategy = strategy;
        this.collector = collector;
        this.createTime = System.currentTimeMillis();
        this.collector.configure(this);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(
                Level.FINE,
                "Worker: {0} for: {1} handled by: {2} created after: {3}ms.",    //NOI18N
                        new Object[]{
                            System.identityHashCode(this),
                            request,
                            strategy,
                            this.createTime - this.collector.startTime
                });
        }
    }

    public void cancel() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(
                Level.FINE,
                "Worker: {0} canceled after {1} ms.",   //NOI18N
                new Object[]{
                    System.identityHashCode(this),
                    System.currentTimeMillis() - createTime
                });
        }
        this.cancelled = true;
        this.strategy.cancel();
    }

    @Override
    public void run() {
        this.collector.start(this);
        try {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(
                    Level.FINE,
                    "Worker: {0} started after {1} ms.", //NOI18N
                    new Object[]{
                        System.identityHashCode(this),
                        System.currentTimeMillis() - createTime
                    });
            }
            this.strategy.execute(this.request, this);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(
                    Level.FINE,
                    "Worker: {0} finished after cancel {1} ms.",  //NOI18N
                    new Object[]{
                        System.identityHashCode(this),
                        System.currentTimeMillis() - createTime
                    });
            }
        } finally {
            this.collector.done(this);
        }
    }

    @Override
    public String toString() {
        return String.format(
            "%s (%d) [request: %s, strategy: %s]",  //NOI18N
            getClass().getSimpleName(),
            System.identityHashCode(this),
            this.request,
            this.strategy);
    }

    private void emit(@NonNull final List<? extends FileDescriptor> files) {
        if (!this.cancelled) {
            this.collector.emit(this, files);
        }
    }

    @NonNull
    static Request newRequest(
        @NonNull final String text,
        @NonNull final QuerySupport.Kind searchType,
        @NullAllowed final Project project,
        final int lineNr) {
        return new Request(text, searchType, project, lineNr);
    }


    @NonNull
    static Collector newCollector(
        @NonNull final Models.MutableListModel<FileDescriptor> model,
        @NonNull final Runnable updateCallBack,
        @NonNull final Runnable doneCallBack,
        final long startTime) {
        return new Collector(model, updateCallBack, doneCallBack, startTime);
    }

    @NonNull
    static Worker newWorker(
        @NonNull final Request request,
        @NonNull final Collector collector,
        @NonNull final Type type) {
        Parameters.notNull("request", request); //NOI18N
        Parameters.notNull("collector", collector); //NOI18N
        Parameters.notNull("type", type);   //NOI18N
        final Strategy strategy = type.createStrategy();
        return new Worker(request, strategy, collector);
    }

    static enum Type {
        PROVIDER {
            @NonNull
            @Override
            Strategy createStrategy() {
                return new ProviderStrategy();
            }
        },
        INDEX {
            @NonNull
            @Override
            Strategy createStrategy() {
                return new IndexStrategy();
            }
        },
        FS {
            @NonNull
            @Override
            Strategy createStrategy() {
                return new FSStrategy();
            }
        };
        @NonNull
        abstract Strategy createStrategy();
    }

    static final class Request {
        private final String text;
        private final QuerySupport.Kind searchType;
        private final Project currentProject;
        private final int lineNr;
        private final Set<FileObject> excludes;
        //@GuardedBy("this")
        private Collection<? extends FileObject> sgRoots;

        private Request(
            @NonNull final String text,
            @NonNull final QuerySupport.Kind searchType,
            @NullAllowed final Project currentProject,
            final int lineNr) {
            Parameters.notNull("text", text);   //NOI18N
            Parameters.notNull("searchType", searchType);   //NOI18N
            this.text = text;
            this.searchType = searchType;
            this.currentProject = currentProject;
            this.lineNr = lineNr;
            this.excludes = Collections.newSetFromMap(new ConcurrentHashMap<FileObject, Boolean>());
        }

        @NonNull
        String getText() {
            return text;
        }

        @NonNull
        QuerySupport.Kind getSearchKind() {
            return searchType;
        }

        @CheckForNull
        Project getCurrentProject() {
            return currentProject;
        }

        int getLine() {
            return lineNr;
        }

        @Override
        public String toString() {
            return String.format(
                "%s[text: %s, search kind: %s, project: %s, line: %d]", //NOI18N
                getClass().getSimpleName(),
                text,
                searchType,
                currentProject,
                lineNr);
        }

        private synchronized Collection<? extends FileObject> getSourceRoots() {
            if (sgRoots == null) {
                final Project[] openedProjects = OpenProjects.getDefault().getOpenProjects();
                final List<Project> projects;
                if (currentProject == null) {
                    projects = Arrays.asList(openedProjects);
                } else {
                    projects = new ArrayList<>();
                    projects.add(currentProject);
                    for (Project openedProject : openedProjects) {
                        if (currentProject.equals(openedProject)){
                            continue;
                        }
                        projects.add(openedProject);
                    }
                }
                final List<FileObject> newSgRoots = new ArrayList<>();
                for (Project p : projects) {
                    for (SourceGroup group : ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC)) {
                        newSgRoots.add(group.getRootFolder());
                    }
                }
                sgRoots = Collections.unmodifiableCollection(newSgRoots);
            }
            return sgRoots;
        }

        private boolean isExcluded(@NonNull final FileObject file) {
            return excludes.contains(file);
        }

        private void exclude(@NonNull final FileObject file) {
            excludes.add(file);
        }
    }

    static final class Collector {
        private final Models.MutableListModel<FileDescriptor> model;
        private final Runnable updateCallBack;
        private final Runnable doneCallBack;
        private final long startTime;
        private final Set<Worker> active = Collections.newSetFromMap(new ConcurrentHashMap<Worker, Boolean>());
        private volatile boolean frozen;

        private Collector(
            @NonNull final Models.MutableListModel<FileDescriptor> model,
            @NonNull final Runnable updateCallBack,
            @NonNull final Runnable doneCallBack,
            final long startTime) {
            Parameters.notNull("model", model); //NOI18N
            Parameters.notNull("updateCallBack", updateCallBack);   //NOI18N
            Parameters.notNull("doneCallBack", doneCallBack);   //NOI18N
            this.model = model;
            this.updateCallBack = updateCallBack;
            this.doneCallBack = doneCallBack;
            this.startTime = startTime;
        }

        @Override
        public String toString() {
            return String.format(
                "%s (%d) [frozen: %s, active: %s]", //NOI18N
                getClass().getSimpleName(),
                System.identityHashCode(this),
                frozen,
                active);
        }

        boolean isDone() {
            return frozen && active.isEmpty();
        }

        private void configure(@NonNull final Worker worker) {
            Parameters.notNull("worker", worker);   //NOI18N
            if (frozen) {
                throw new IllegalStateException(String.format(
                    "Adding worker: %s to already frozen collector: %s",    //NOI18N
                    worker,
                    this));
            }
            if (!active.add(worker)) {
                throw new IllegalArgumentException(String.format(
                    "Adding already added worker: %s to collector: %s",
                    worker,
                    this
                ));
            }
        }

        private void start(@NonNull final Worker worker) {
            Parameters.notNull("worker", worker);   //NOI18N
            frozen = true;
        }

        private void emit(
            @NonNull final Worker worker,
            @NonNull final List<? extends FileDescriptor> files) {
            Parameters.notNull("worker", worker);   //NOI18N
            Parameters.notNull("files", files); //NOI18N
            model.add(files);
            updateCallBack.run();
        }

        private void done(@NonNull final Worker worker) {
            Parameters.notNull("worker", worker);   //NOI18N
            if (!active.remove(worker)) {
                throw new IllegalStateException(String.format(
                    "Trying to removed unknown worker: %s from collector: %s",  //NOI18N
                    worker,
                    this));
            }
            if (active.isEmpty()) {
                doneCallBack.run();
            }
        }
    }

    private static abstract class Strategy {
        private volatile boolean cancelled;

        @CheckForNull
        abstract  void execute(@NonNull Request request, @NonNull final Worker worker);

        void cancel() {
            cancelled = true;
        }

        final boolean isCancelled() {
            return cancelled;
        }

        static SearchType toJumpToSearchType(final QuerySupport.Kind searchType) {
            switch (searchType) {
                case CAMEL_CASE:
                case CASE_INSENSITIVE_CAMEL_CASE:
                    return org.netbeans.spi.jumpto.type.SearchType.CAMEL_CASE;
                case CASE_INSENSITIVE_PREFIX:
                    return org.netbeans.spi.jumpto.type.SearchType.CASE_INSENSITIVE_PREFIX;
                case CASE_INSENSITIVE_REGEXP:
                    return org.netbeans.spi.jumpto.type.SearchType.CASE_INSENSITIVE_REGEXP;
                case EXACT:
                    return org.netbeans.spi.jumpto.type.SearchType.EXACT_NAME;
                case PREFIX:
                    return org.netbeans.spi.jumpto.type.SearchType.PREFIX;
                case REGEXP:
                    return org.netbeans.spi.jumpto.type.SearchType.REGEXP;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private static final class ProviderStrategy extends Strategy {

        //@GuardedBy("this")
        private List<? extends FileProvider> providers;
        private volatile FileProvider currentProvider;

        @Override
        void execute(@NonNull final Request request, @NonNull final Worker worker) {
            if (isCancelled()) {
                return;
            }
            final List<FileDescriptor> files = new ArrayList<>();
            final SearchType jumpToSearchType = toJumpToSearchType(request.getSearchKind());
            final FileProvider.Context ctx = FileProviderAccessor.getInstance().createContext(
                request.getText(),
                jumpToSearchType,
                request.getLine(),
                request.getCurrentProject());
            final FileProvider.Result fpR = FileProviderAccessor.getInstance().createResult(
                files,
                new String[1],
                ctx);
            for (FileObject root : request.getSourceRoots()) {
                if (request.isExcluded(root)) {
                    continue;
                }
                FileProviderAccessor.getInstance().setRoot(ctx, root);
                boolean recognized = false;
                for (FileProvider provider : getProviders()) {
                    if (isCancelled()) {
                        return;
                    }
                    currentProvider = provider;
                    try {
                        recognized = provider.computeFiles(ctx, fpR);
                        if (recognized) {
                            break;
                        }
                    } finally {
                        currentProvider = null;
                    }
                }
                if (recognized) {
                    request.exclude(root);
                }
                if (!files.isEmpty()) {
                    worker.emit(files);
                    files.clear();
                }
            }
        }

        @Override
        void cancel() {
            super.cancel();
            FileProvider fp = currentProvider;
            if (fp != null) {
                fp.cancel();
            }
        }

        private Iterable<? extends FileProvider> getProviders() {
            synchronized (this) {
                if (providers != null) {
                    return providers;
                }
            }
            final List<FileProvider> result = new ArrayList<FileProvider>();
            for (FileProviderFactory fpf : Lookup.getDefault().lookupAll(FileProviderFactory.class)) {
                result.add(fpf.createFileProvider());
            }
            synchronized (this) {
                if (providers == null) {
                    providers = Collections.unmodifiableList(result);
                }
                return providers;
            }
        }
    }

    private static final class IndexStrategy extends Strategy {

        @Override
        void execute(@NonNull final Request request, @NonNull final Worker worker) {
            if (isCancelled()) {
                return;
            }
            final Pair<String,String> query = createQuery(request);
            try {
                final Project currentProject = request.getCurrentProject();
                if (currentProject != null) {
                    doQuery(
                        query,
                        request,
                        worker,
                        collectRoots(request, currentProject));
                }
                doQuery(
                    query,
                    request,
                    worker,
                    collectRoots(request, null));
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        @NonNull
        private Collection<? extends FileObject> collectRoots(
            @NonNull final Request request,
            @NullAllowed final Project project) {
            final Set<FileObject> roots = new LinkedHashSet<>();
            for (FileObject fo : QuerySupport.findRoots(
                project,
                null,
                Collections.<String>emptyList(),
                Collections.<String>emptyList())) {
                if (!request.isExcluded(fo)) {
                    roots.add(fo);
                }
            }
            return roots;
        }

        private boolean doQuery(
            @NonNull final Pair<String,String> query,
            @NonNull final Request request,
            @NonNull final Worker worker,
            @NonNull final Collection<? extends FileObject> roots) throws IOException {
            if (isCancelled()) {
                return false;
            }
            final QuerySupport q = QuerySupport.forRoots(
                FileIndexer.ID,
                FileIndexer.VERSION,
                roots.toArray(new FileObject[roots.size()]));
            if (isCancelled()) {
                return false;
            }
            final List<FileDescriptor> files = new ArrayList<>();
            final Collection<? extends IndexResult> results = q.query(
                query.first(),
                query.second(),
                request.getSearchKind());
            for (IndexResult r : results) {
                FileObject file = r.getFile();
                if (file == null || !file.isValid()) {
                    // the file has been deleted in the meantime
                    continue;
                }
                final Project project = FileOwnerQuery.getOwner(file);
                FileDescriptor fd = new FileDescription(
                        file,
                        r.getRelativePath().substring(0, Math.max(r.getRelativePath().length() - file.getNameExt().length() - 1, 0)),
                        project,
                        request.getLine());
                boolean preferred = project != null && request.getCurrentProject() != null ?
                        project.getProjectDirectory() == request.getCurrentProject().getProjectDirectory() :
                        false;
                FileProviderAccessor.getInstance().setFromCurrentProject(fd, preferred);
                files.add(fd);
                LOG.log(
                    Level.FINER,
                    "Found: {0}, project={1}, currentProject={2}, preferred={3}",   //NOI18N
                    new Object[]{
                        file.getPath(),
                        project,
                        request.getCurrentProject(),
                        preferred
                    });
            }
            for (FileObject root : roots) {
                request.exclude(root);
            }
            worker.emit(files);
            return true;
        }

        @NonNull
        private Pair<String,String> createQuery(@NonNull final Request request) {
            String searchField;
            String indexQueryText;
            switch (request.getSearchKind()) {
                case CASE_INSENSITIVE_PREFIX:
                    searchField = FileIndexer.FIELD_CASE_INSENSITIVE_NAME;
                    indexQueryText = request.getText();
                    break;
                case CASE_INSENSITIVE_REGEXP:
                    searchField = FileIndexer.FIELD_CASE_INSENSITIVE_NAME;
                    indexQueryText = NameMatcherFactory.wildcardsToRegexp(request.getText(),true);
                    Pattern.compile(indexQueryText);    //Verify the pattern
                    break;
                case REGEXP:
                    searchField = FileIndexer.FIELD_NAME;
                    indexQueryText = NameMatcherFactory.wildcardsToRegexp(request.getText(),true);
                    Pattern.compile(indexQueryText);    //Verify the pattern
                    break;
                default:
                    searchField = FileIndexer.FIELD_NAME;
                    indexQueryText = request.getText();
                    break;
            }
            return Pair.<String,String>of(searchField, indexQueryText);
        }
    }

    private static final class FSStrategy extends Strategy {

        @CheckForNull
        @Override
        void execute(@NonNull final Request request, @NonNull final Worker worker) {
            if (isCancelled()) {
                return;
            }
            final SearchType jumpToSearchType = toJumpToSearchType(request.getSearchKind());
            //Looking for matching files in all found folders
            final NameMatcher matcher = NameMatcherFactory.createNameMatcher(
                    request.getText(),
                    jumpToSearchType);
            final List<FileDescriptor> files = new ArrayList<FileDescriptor>();
            final Collection <FileObject> allFolders = new HashSet<FileObject>();
            List<SearchFilter> filters = SearchInfoUtils.DEFAULT_FILTERS;
            for (FileObject root : request.getSourceRoots()) {
                allFolders.clear();
                for (FileObject folder : searchSources(
                        root,
                        allFolders,
                        request,
                        filters)) {
                    if (isCancelled()) {
                        return;
                    }
                    assert folder.isFolder();
                    Enumeration<? extends FileObject> filesInFolder = folder.getData(false);
                    while (filesInFolder.hasMoreElements()) {
                        FileObject file = filesInFolder.nextElement();
                        if (file.isFolder()) continue;

                        if (matcher.accept(file.getNameExt())) {
                            Project project = FileOwnerQuery.getOwner(file);
                            boolean preferred = false;
                            String relativePath = null;
                            if(project != null) { // #176495
                               FileObject pd = project.getProjectDirectory();
                               preferred = request.getCurrentProject() != null ?
                                 pd == request.getCurrentProject().getProjectDirectory() :
                                 false;
                                relativePath = FileUtil.getRelativePath(pd, file);
                            }
                            if (relativePath == null)
                                relativePath ="";   //NOI18N
                            FileDescriptor fd = new FileDescription(
                                file,
                                relativePath,
                                project,
                                request.getLine());
                            FileProviderAccessor.getInstance().setFromCurrentProject(fd, preferred);
                            files.add(fd);
                        }
                    }
                    request.exclude(folder);
                }
                if (!files.isEmpty()) {
                    worker.emit(files);
                    files.clear();
                }
            }
        }

        @NonNull
        private Collection<FileObject> searchSources(
                @NonNull final FileObject root,
                @NonNull final Collection<FileObject> result,
                @NonNull final Request  request,
                @NonNull final List<SearchFilter> filters) {
            if (isCancelled() ||
                root.getChildren().length == 0 ||
                request.isExcluded(root) ||
                !checkAgainstFilters(root, filters)) {
                return result;
            } else {
                    result.add(root);
                    final Enumeration<? extends FileObject> subFolders = root.getFolders(false);
                    while (subFolders.hasMoreElements()) {
                        searchSources(subFolders.nextElement(), result, request, filters);
                    }
            }
            return result;
        }

        private boolean checkAgainstFilters(FileObject folder, List<SearchFilter> filters) {
            assert folder.isFolder();
            for (SearchFilter filter: filters) {
                if (filter.traverseFolder(folder) == SearchFilter.FolderResult.DO_NOT_TRAVERSE) {
                    return false;
                }
            }
            return true;
        }
    }
}
