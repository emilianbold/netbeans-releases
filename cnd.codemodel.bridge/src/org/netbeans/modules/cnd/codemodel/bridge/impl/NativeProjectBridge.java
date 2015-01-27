/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.codemodel.bridge.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.codemodel.CMDiagnostic;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.api.codemodel.query.CMProgressListener;
import org.netbeans.modules.cnd.api.codemodel.query.CMUtilities;
import org.netbeans.modules.cnd.api.codemodel.visit.CMDeclaration;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntityReference;
import org.netbeans.modules.cnd.api.codemodel.visit.CMInclude;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.api.codemodel.storage.CMStorage;
import org.netbeans.modules.cnd.api.codemodel.storage.CMStorageManager;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMCompilationDataBase;
import org.netbeans.modules.cnd.spi.codemodel.support.SPIUtilities;
import org.netbeans.modules.cnd.spi.codemodel.trace.CMTraceUtils;
import org.openide.modules.OnStart;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;

/**
 *
 * @author Vladimir Kvashin
 */
public class NativeProjectBridge implements PropertyChangeListener {
    public static final boolean ENABLED = false;  
    
    private static final CMVisitQuery.VisitOptions INDEX_OPTIONS = CMVisitQuery.VisitOptions.valueOf(
            CMVisitQuery.VisitOptions.SuppressWarnings,
            //CMVisitQuery.VisitOptions.SkipParsedBodiesInSession
            CMVisitQuery.VisitOptions.None);

    private static final String STORAGE_SCHEME = System.getProperty("cnd.codemodel.db.scheme", "jdbc:h2"); //NOI18N

    private static final NativeProjectBridge INSTANCE = new NativeProjectBridge();
    private static final Set<CMProgressListener> listeners = new WeakSet<>();

    private final RequestProcessor.Task openProjectsTask =
        new RequestProcessor("NewModelSupport processor", 1).create( // NOI18N
            new Runnable() {
                @Override
                public void run() {
                    openProjectsIfNeeded();
                }

            });

    /** guarded by openProjectsLock */
    private final Set<NativeProject> openProjects = new HashSet<>();
    
    /** guarded by openProjectsLock */
    private final Map<NativeProject, CMStorage> storages = new HashMap<>();

    private final Object openProjectsLock = new Object();

    public static NativeProjectBridge getInstance() {
        return INSTANCE;
    }

    private void init() {
        NativeProjectRegistry.getDefault().addPropertyChangeListener(this);
        openProjectsTask.schedule(0);
    }

    public Collection<CMIndex> getIndices(NativeProject project) {
        CMIndex index = SPIUtilities.getIndex(project);
        return (index == null) ? Collections.<CMIndex>emptyList() : Collections.<CMIndex>singleton(index);
    }

    NativeProject getProject(CMIndex index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @OnStart
    public static class Start implements Runnable {        
        @Override
        public void run() {
          if (ENABLED) {
            NativeProjectBridge.getInstance().init();
          }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(NativeProjectRegistry.PROPERTY_OPEN_NATIVE_PROJECT)) {
            openProjectsTask.schedule(0);
        }
    }

    public void addProgressListener(CMProgressListener l) {
        synchronized(listeners) {
            listeners.add(l);
        }
    }

    public void removeProgressListener(CMProgressListener l) {
        synchronized(listeners) {
            listeners.remove(l);
        }
    }
    
    private void openProjectsIfNeeded() {
        Set<NativeProject> toOpen = new HashSet<>();
        Set<NativeProject> toClose = new HashSet<>();
        synchronized (openProjectsLock) {
            Collection<NativeProject> currentlyOpen = NativeProjectRegistry.getDefault().getOpenProjects();
            for (NativeProject isOpen : currentlyOpen) {
                if (!openProjects.contains(isOpen)) {
                    toOpen.add(isOpen);
                }
            }
            for (NativeProject wasOpen : openProjects) {
                if (!currentlyOpen.contains(wasOpen)) {
                    toClose.add(wasOpen);
                }
            }
            openProjects.addAll(toOpen);
            openProjects.removeAll(toClose);
            for (NativeProject p : toClose) {
                close(p);
            }
            for (NativeProject p : toOpen) {
                open(p);
            }
        }
    }

    public void open(NativeProject p) {
        final ProgressTracker progress = ProgressTracker.start("Open&Parse Project " + p.getProjectDisplayName()); // NOI18N
        try {
            // assert Thread.holdsLock(openProjectsLock);
            StopWatch sw = new StopWatch("creating storage for %s ...", p.getProjectDisplayName());
            CMStorage storage = getOrCreateStorage(p);
            sw.stop();
            NativeProjectCompilationDataBase cdb = new NativeProjectCompilationDataBase(p);
            CMIndex index;
            if (!storage.needsIndexing(null)) {
                CMUtilities.getLogger().log(Level.INFO, "Project {0} does not need indexing", p); //NOI18N
                //TODO: the below is a workaround
                index = SPIUtilities.parse(Collections.<CMCompilationDataBase.Entry>emptyList());
                SPIUtilities.registerIndex(p, index);
            } else if (Boolean.getBoolean("cnd.codemodel.noindex")) { //NOI18N
                index = SPIUtilities.parse(Collections.<CMCompilationDataBase.Entry>emptyList());
                SPIUtilities.registerIndex(p, index);
                sw = new StopWatch("Parsing and indexing by file %s", p.getProjectDisplayName());
                for (CMCompilationDataBase.Entry entry : cdb.getEntries()) {
                    CMIndex partialIndex = SPIUtilities.parse(Arrays.asList(entry));
                    index(storage, partialIndex);
                }
                sw.stop();

                sw = new StopWatch("flushing %s ...", index);
                storage.flush();
                sw.stop();
            } else if (CMTraceUtils.INDEX_ON_PARSE) {
                sw = new StopWatch("Indexing at parse %s", p.getProjectDisplayName());
                progress.log("Parse & Index ProjectCompilationDataBase for " + p.getProjectDisplayName()); // NOI18N
                progress.switchToDeteterminate(cdb.getEntries().size());
                index = SPIUtilities.createIndex(cdb, new CallbackImpl(storage, progress), INDEX_OPTIONS);
                if (index != null) {
                    SPIUtilities.registerIndex(p, index);
                }
                sw.stop();
                sw = new StopWatch("flushing %s ...", index);
                storage.flush();
                sw.stop();
            } else {
                sw = new StopWatch("parsing %s", p);
                progress.log("Start: Parsing ProjectCompilationDataBase for " + p.getProjectDisplayName()); // NOI18N
                index = SPIUtilities.parse(cdb);
                progress.log("Done: Parsing ProjectCompilationDataBase for " + p.getProjectDisplayName()); // NOI18N
                sw.stop();

                if (index != null) {
                    // TODO: better error processing (occurs in full remote so far)
                    SPIUtilities.registerIndex(p, index);
                    sw = new StopWatch("indexing %s", p);
                    progress.log("Start: Indexing " + p.getProjectDisplayName()); // NOI18N                
                    index(storage, index);
                    progress.log("Done: Indexing " + p.getProjectDisplayName()); // NOI18N
                    sw.stop();
                    sw = new StopWatch("flushing %s ...", index);
                    storage.flush();
                    sw.stop();
                }
            }
            List<CMProgressListener> copy;
            synchronized (listeners) {
                copy = new ArrayList<>(listeners);
            }
            for (CMProgressListener listener : copy) {
                listener.indexingFinished(index);
                listener.projectIndexingFinished(p);
            }
        } finally {
            progress.stop();
        }
    }
    
    void reIndex(NativeProject p, Collection<CMIndex> indices, CMCompilationDataBase.Entry fileEntry) {
        CMStorage storage = getOrCreateStorage(p);
        SPIUtilities.reindexFile(indices, fileEntry, new CallbackImpl(storage, null), INDEX_OPTIONS);
        storage.flush();
    }

    private CMStorage getOrCreateStorage(NativeProject p) {
        synchronized (openProjectsLock) {
            CMStorage storage = storages.get(p);
            if (storage == null) {
                CharSequence id = p.getProjectDisplayName(); //new FSPath(p.getFileSystem(), p.getProjectRoot()).getURL();
                StopWatch sw;
                sw = new StopWatch("creating storage for %s", id); //NOI18N
                storage = CMStorageManager.getInstance(id.toString(), STORAGE_SCHEME);
                assert storage != null;
                storages.put(p, storage);
                sw.stop();
            }
            return storage;
        }
    }

    private void index(CMStorage storage, CMIndex index) {
        StopWatch sw;
        sw = new StopWatch("visiting %s ...", index);
        //CMVisitQuery.visitReferences(null, Arrays.asList(index), new CallbackImpl(storage));
        CMVisitQuery.visitIndex(index, new CallbackImpl(storage), INDEX_OPTIONS);
        sw.stop();
    }

    private void close(NativeProject p) {
        assert Thread.holdsLock(openProjectsLock);
        SPIUtilities.unregisterIndex(p);
        CMStorage storage = storages.remove(p);
        if (storage != null) {
            storage.shutdown();
        }
    }

    private static class CallbackImpl implements CMVisitQuery.IndexCallback {

        private final AtomicInteger counter = new AtomicInteger(0);
        private final CMStorage storage;
        private final ProgressTracker progress;

        public CallbackImpl(CMStorage storage, ProgressTracker progress) {
            this.storage = storage;
            this.progress = progress;
        }
                
        public CallbackImpl(CMStorage storage) {
            this(storage, null);
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void onDiagnostics(Iterable<CMDiagnostic> diagnostics) {}

        @Override
        public void onIndclude(CMInclude include) {
            counter.incrementAndGet();
            storage.addInclude(include);
        }

        @Override
        public void onTranslationUnit() {
            if (progress != null) {
                progress.progress("");
            }
        }

        @Override
        public void onDeclaration(CMDeclaration declaration) {
            counter.incrementAndGet();
            storage.addDeclaration(declaration);
        }

        @Override
        public void onReference(CMEntityReference entityReference) {
            counter.incrementAndGet();
            storage.addEntityReference(entityReference);
        }
    }

    private static class StopWatch {

        private long time;
        private final String name;

        public StopWatch(String nameTemplate, Object... nameTemplateArgs) {
            this.name = String.format(nameTemplate, nameTemplateArgs);
            report(true);
            time = System.currentTimeMillis();
        }

        private void report(boolean start) {
            StringBuilder sb = new StringBuilder(getClass().getSimpleName());
            sb.append(' ').append(name);
            sb.append(start ? " started..." : " took "); //NOI18N
            if (!start) {
                sb.append(time/1000).append(" seconds"); //NOI18N
            }
            CMUtilities.getLogger().log(Level.INFO, sb.toString());
        }

        public void stop() {
            time = System.currentTimeMillis() - time;
            report(false);
        }
    }
}
