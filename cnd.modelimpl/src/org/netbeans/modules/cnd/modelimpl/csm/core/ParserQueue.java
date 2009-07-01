/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * A queue that hold a list of files to parse.
 * @author Vladimir Kvashin
 */
public final class ParserQueue {

    /**
     * Entry position in the parser queue.
     */
    public static enum Position {

        /**
         * <code>IMMEDIATE</code> entries are parsed first, before
         * <code>HEAD</code> and <code>TAIL</code> entries. If there are
         * several <code>IMMEDIATE</code> entries, they are parsed
         * in the order of their insertion to the queue.
         */
        IMMEDIATE,
        /**
         * <code>HEAD</code> entries are parsed after <code>IMMEDIATE</code>,
         * and before <code>TAIL</code> entries. The are parsed in the
         * <em>reversed</em> order of their insertion to the queue.
         */
        HEAD,
        /**
         * <code>TAIL</code> entries are parsed last, after
         * <code>IMMEDIATE</code> and <code>HEAD</code> entries.
         * They are parsed in the order of their insertion to the queue.
         */
        TAIL
    }

    public static final class Entry implements Comparable<Entry> {

        private final FileImpl file;
        /** either APTPreprocHandler.State or Collection<APTPreprocHandler.State> */
        private Object ppState;
        private final Position position;
        private final int serial;

        private Entry(FileImpl file, Collection<APTPreprocHandler.State> ppStates, Position position, int serial) {
            if (TraceFlags.TRACE_PARSER_QUEUE) {
                System.err.println("creating entry for " + file.getAbsolutePath() + // NOI18N
                        " as " + tracePreprocStates(ppStates)); // NOI18N
            }
            this.file = file;
            if (ppStates.size() == 1) {
                this.ppState = ppStates.iterator().next();
            } else {
                this.ppState = new ArrayList<APTPreprocHandler.State>(ppStates);
            }

            this.position = position;
            this.serial = serial;
        }

        public FileImpl getFile() {
            return file;
        }

        @SuppressWarnings("unchecked")
        public Collection<APTPreprocHandler.State> getPreprocStates() {
            Object state = ppState;
            if (state instanceof APTPreprocHandler.State || state == null) {
                return Collections.singleton((APTPreprocHandler.State) state);
            } else {
                return (Collection<APTPreprocHandler.State>) state;
            }
        }

        public Position getPosition() {
            return position;
        }

        @Override
        public String toString() {
            return toString(true);
        }

        public String toString(boolean detailed) {
            StringBuilder retValue = new StringBuilder();
            retValue.append("ParserQueue.Entry " + file + " of project " + file.getProject()); // NOI18N
            if (detailed) {
                retValue.append("\nposition: ").append(position); // NOI18N
                retValue.append(", serial: ").append(serial); // NOI18N
                retValue.append("\nwith PreprocStates:"); // NOI18N
                for (APTPreprocHandler.State state : getPreprocStates()) {
                    retValue.append('\n');
                    retValue.append(state);
                }
            }
            return retValue.toString();
        }

        @SuppressWarnings("unchecked")
        private synchronized void addStates(Collection<APTPreprocHandler.State> ppStates) {
            if (this.ppState instanceof APTPreprocHandler.State) {
                APTPreprocHandler.State oldState = (APTPreprocHandler.State) ppState;
                this.ppState = new ArrayList<APTPreprocHandler.State>();
                ((Collection<APTPreprocHandler.State>) this.ppState).add(oldState);
            }
            Collection<APTPreprocHandler.State> states = (Collection<APTPreprocHandler.State>) this.ppState;
            for (APTPreprocHandler.State state : ppStates) {
                if (state != FileImpl.DUMMY_STATE) {
                    if (!states.contains(state)) {
                        states.add(state);
                    } else {
                        if (TraceFlags.TIMING_PARSE_PER_FILE_FLAT) {
                            System.err.println("array already has the state " + state);
                        }
                    }
                } else {
                    if (TraceFlags.TIMING_PARSE_PER_FILE_FLAT) {
                        System.err.println("skip adding dummy state");
                    }
                }
            }
        }

        private synchronized void setStates(Collection<APTPreprocHandler.State> ppStates) {
            // TODO: IZ#87204: AssertionError on _Bvector_base opening
            // review why it could be null
            // FIXUP: remove assert checks and update if statements to prevent NPE
            //            assert (ppState != null) : "why do pass null snapshot?";
            //            assert (this.ppState != null) : "if it was already included, where is the state?";

            if (TraceFlags.TRACE_PARSER_QUEUE) {
                System.err.println("setPreprocStateIfNeed for " + file.getAbsolutePath() + // NOI18N
                        " as " + tracePreprocStates(ppStates) + " with current " + tracePreprocStates(getPreprocStates())); // NOI18N
            }
            // we don't need check here - all logic is in ProjectBase.onFileIncluded            
            this.ppState = new ArrayList<APTPreprocHandler.State>(ppStates);
        }

        public int compareTo(Entry that) {
            int cmp = this.position.compareTo(that.position);
            if (cmp == 0) {
                cmp = this.serial - that.serial;
                return this.position == Position.HEAD ? -cmp : cmp;
            } else {
                return cmp;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Entry) {
                return compareTo((Entry) obj) == 0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + (this.position != null ? this.position.ordinal() : 0);
            hash = 97 * hash + this.serial;
            return hash;
        }
    }

    /**
     * Determines what to do with a file that is being added to the queue
     */
    public static enum FileAction {

        /**
         * Nothing should be done
         */
        NOTHING,
        /**
         * File should be marked as "addition parse needed", i.e.
         * FileImpl.markMoreParseNeeded() should be called
         */
        MARK_MORE_PARSE,
        /**
         * File should be marked as "reparse needed", 
         * without invalidating the APT cache, i.e.
         * FileImpl.markReparseNeeded(false) should be called
         */
        MARK_REPARSE,
        /**
         * File should be marked as "reparse needed", 
         * and APT cache should be invalidates, i.e.
         * FileImpl.markReparseNeeded(true) should be called
         */
        MARK_REPARSE_AND_INVALIDATE
    }

    /*package*/
    static String tracePreprocStates(Collection<APTPreprocHandler.State> ppStates) {
        StringBuilder sb = new StringBuilder('('); //NOI18N
        boolean first = false;
        for (APTPreprocHandler.State state : ppStates) {
            sb.append('(');
            if (!first) {
                sb.append(';');
            }
            first = false;
            sb.append(tracePreprocState(state));
            sb.append(')');
        }
        sb.append(')');
        return sb.toString();
    }

    /*package*/
    static String tracePreprocState(APTPreprocHandler.State ppState) {
        if (ppState == null) {
            return "null"; // NOI18N
        } else {
            StringBuilder msg = new StringBuilder("["); // NOI18N
            if (!ppState.isCleaned()) {
                msg.append("not"); // NOI18N
            }
            msg.append(" cleaned, "); // NOI18N
            if (!ppState.isValid()) {
                msg.append("not"); // NOI18N
            }
            msg.append(" valid, "); // NOI18N
            if (!ppState.isCompileContext()) {
                msg.append("not"); // NOI18N
            }
            msg.append(" correct State]"); // NOI18N
            return msg.toString();
        }

    }

    private static final class ProjectData {

        private final Set<FileImpl> filesInQueue = new HashSet<FileImpl>();

        // there are no more simultaneously parsing files than threads, so LinkedList suites even better
        private final Collection<FileImpl> filesBeingParsed = new LinkedHashSet<FileImpl>();
        private volatile boolean notifyListeners;

        ProjectData(boolean notifyListeners) {
            this.notifyListeners = notifyListeners;
        }

        public boolean isEmpty() {
            return filesInQueue.isEmpty() && filesBeingParsed.isEmpty();
        }

        public int size() {
            return filesInQueue.size();
        }
    }

    private static enum State {

        ON, OFF, SUSPENDED
    }
    private static ParserQueue instance = new ParserQueue(false);
    private final PriorityQueue<Entry> queue = new PriorityQueue<Entry>();
    private volatile State state;
    private static final class SuspendLock {}
    private final Object suspendLock = new SuspendLock();

    // do not need UIDs for ProjectBase in parsing data collection
    private final Map<ProjectBase, ProjectData> projectData = new HashMap<ProjectBase, ProjectData>();
    private final Map<CsmProject, Object> projectLocks = new HashMap<CsmProject, Object>();
    private final AtomicInteger serial = new AtomicInteger(0);
    private static final class Lock {}
    private final Object lock = new Lock();
    private final boolean addAlways;
    private final Diagnostic.StopWatch stopWatch = TraceFlags.TIMING ? new Diagnostic.StopWatch(false) : null;

    private ParserQueue(boolean addAlways) {
        this.addAlways = addAlways;
    }

    public static ParserQueue instance() {
        return instance;
    }

    public static ParserQueue testInstance() {
        return new ParserQueue(true);
    }

    private String traceState4File(FileImpl file, Set<FileImpl> files) {
        StringBuilder builder = new StringBuilder(" "); // NOI18N
        builder.append(file);
        builder.append("\n of project ").append(file.getProjectImpl(true)); // NOI18N
        builder.append("\n content of projects files set:\n"); // NOI18N
        if (files != null) {
            builder.append(files);
            builder.append("\nqueue content is:\n"); // NOI18N
            builder.append(toString(queue, false));
            builder.append("\nprojectData content is:\n"); // NOI18N
            builder.append(projectData);
        }
        return builder.toString();
    }

    /**
     * If file isn't yet enqueued, places it at the beginning of the queue,
     * otherwise moves it there
     */
    public void add(FileImpl file, APTPreprocHandler.State ppState, Position position) {
        add(file, Collections.singleton(ppState), position, true, FileAction.NOTHING);
    }

    /**
     * If file isn't yet enqueued, places it at the beginning of the queue,
     * otherwise moves it there
     * @return true if file was added into queue
     */
    public boolean add(FileImpl file, Collection<APTPreprocHandler.State> ppStates, Position position,
            boolean clearPrevState, FileAction fileAction) {

        if (ppStates.isEmpty()) {
            Utils.LOG.severe("Adding a file with an emty preprocessor state set"); //NOI18N
        }
        assert state != null;
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ParserQueue: add " + file.getAbsolutePath() + " as " + position); // NOI18N
        }
        boolean newEntry = false;
        synchronized (lock) {
            if (state == State.OFF) {
                return false;
            }
            switch (fileAction) {
                case MARK_MORE_PARSE:
                    file.markMoreParseNeeded();
                    break;
                case MARK_REPARSE:
                    file.markReparseNeeded(false);
                    break;
                case MARK_REPARSE_AND_INVALIDATE:
                    file.markReparseNeeded(true);
                    break;
            }
            if (!needEnqueue(file)) {
                if (TraceFlags.TRACE_PARSER_QUEUE) {
                    System.err.println("ParserQueue: do not add parsing or parsed " + file.getAbsolutePath()); // NOI18N
                }
                return false;
            }
            if (queue.isEmpty()) {
                serial.set(0);
            }
            Set<FileImpl> files = getProjectFiles(file.getProjectImpl(true));
            Entry entry = null;
            boolean addEntry = false;
            if (files.contains(file)) {
                entry = findEntry(file); //TODO: think over / profile, probably this line is expensive
                if (entry == null) {
                    FileImpl findFile = null;
                    for (FileImpl aFile : files) {
                        if (aFile.equals(file)) {
                            findFile = aFile;
                        }
                    }
                    if (findFile == file) {
                        CndUtils.assertTrue(false, "ProjectData contains file " + file + ", but there is no matching entry in the queue"); // NOI18N
                    } else {
                        CndUtils.assertTrue(false, "ProjectData contains another instance of file " + file + ", so there is no matching entry in the queue"); // NOI18N
                    }
                    System.err.println(traceState4File(file, files));
                    System.err.println(traceState4File(findFile, null));
                } else {
                    if (clearPrevState) {
                        entry.setStates(ppStates);
                    } else {
                        entry.addStates(ppStates);
                    }
                    if (file != entry.file) {
                        // Replace old file instance by new
                        queue.remove(entry);
                        entry = new Entry(file, entry.getPreprocStates(), position, serial.incrementAndGet());
                        addEntry = true;
                    } else if (position.compareTo(entry.getPosition()) < 0) {
                        queue.remove(entry);
                        entry = new Entry(file, entry.getPreprocStates(), position, serial.incrementAndGet());
                        addEntry = true;
                    }
                }
            } else {
                assert (findEntry(file) == null) : "The queue should not contain the file " + traceState4File(file, files); // NOI18N
                files.add(file);
                newEntry = true;
            }
            if (entry == null) {
                entry = new Entry(file, ppStates, position, serial.incrementAndGet());
                addEntry = true;
            }
            if (addEntry) {
                queue.add(entry);
                if (TraceFlags.TRACE_PARSER_QUEUE) {
                    System.err.println("ParserQueue: added entry " + entry.toString(TraceFlags.TRACE_PARSER_QUEUE_DETAILS)); // NOI18N
                }
            }
            lock.notifyAll();
        }
        ProgressSupport.instance().fireFileInvalidated(file);
        if (newEntry) {
            ProgressSupport.instance().fireFileAddedToParse(file);
        }
        return true;
    }

    public void waitReady() throws InterruptedException {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ParserQueue: waitReady() ..."); // NOI18N
        }
        synchronized (lock) {
            while (findFirstNotBeeingParsedEntry(false) == null && state != State.OFF) {
                lock.wait();
            }
        }
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ParserQueue: waiting finished"); // NOI18N
        }
    }

    public void suspend() {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ParserQueue: suspending"); // NOI18N
        }
        synchronized (suspendLock) {
            state = State.SUSPENDED;
        }
    }

    public void resume() {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ParserQueue: resuming"); // NOI18N
        }
        synchronized (suspendLock) {
            state = State.ON;
            suspendLock.notifyAll();
        }
    }

    private Entry findFirstNotBeeingParsedEntry(boolean removeFoundEntryFromQueue) {
        Entry e = null;
        FileImpl file = null;
        ProjectData data = null;
        ProjectBase project = null;
        Iterator<Entry> iterator = queue.iterator();
        // 'poll' that filters out files that are being parsed.
        // Used to prevent parsing the same file from different threads at the same time.
        while (true) {
            if (!iterator.hasNext()) {
                return null;
            }
            e = iterator.next();
            file = e.getFile();
            project = file.getProjectImpl(true);
            data = getProjectData(project, true);
            if (data.filesBeingParsed.contains(file)) {
                if (TraceFlags.TRACE_PARSER_QUEUE) {
                    System.err.println(Thread.currentThread().getName() + ": beeing parsed by another thread " + file); // NOI18N
                }
            } else {
                if (removeFoundEntryFromQueue) {
                    iterator.remove();
                }
                break;
            }
        }
        return e;
    }

    public Entry poll() throws InterruptedException {

        synchronized (suspendLock) {
            while (state == State.SUSPENDED) {
                if (TraceFlags.TRACE_PARSER_QUEUE) {
                    System.err.println("ParserQueue: waiting for resume"); // NOI18N
                }
                suspendLock.wait();
            }
        }

        Entry e = null;

        ProjectBase project;
        boolean lastFileInProject;
        boolean notifyListeners;

        FileImpl file = null;

        synchronized (lock) {
            ProjectData data = null;
            e = findFirstNotBeeingParsedEntry(true);
            if (e == null) {
                return null;
            }
            file = e.getFile();
            project = file.getProjectImpl(true);
            data = getProjectData(project, true);
            data.filesInQueue.remove(file);
            data.filesBeingParsed.add(file);
            lastFileInProject = data.filesInQueue.isEmpty() && data.filesBeingParsed.isEmpty();
            if (lastFileInProject) {
                removeProjectData(project);
            }
            notifyListeners = lastFileInProject && data.notifyListeners;
            if (TraceFlags.TIMING && stopWatch != null && !stopWatch.isRunning()) {
                stopWatch.start();
                System.err.println("=== Starting parser queue stopwatch"); // NOI18N
            }
        }
        // TODO: think over, whether this should be under if( notifyListeners
        ProgressSupport.instance().fireFileParsingStarted(file);
        if (lastFileInProject) {
            project.onParseFinish(false);
            if (notifyListeners) {
                ProgressSupport.instance().fireProjectParsingFinished(project);
            }
        }
        if (TraceFlags.TRACE_PARSER_QUEUE_POLL) {
            System.err.printf("ParserQueue: polling %s with %d states in thread %s\n", // NOI18N
                    e.getFile().getAbsolutePath(), e.getPreprocStates().size(), Thread.currentThread().getName());
        }
        return e;
    }

    public void remove(FileImpl file) {

        ProjectBase project;
        boolean lastFileInProject = false;
        boolean notifyListeners = false;

        synchronized (lock) {
            project = file.getProjectImpl(true);
            ProjectData data = getProjectData(project, true);
            if (data.filesInQueue.contains(file)) {
                //queue.remove(file); //TODO: think over / profile, probably this line is expensive
                Entry e = findEntry(file);//TODO: think over / profile, probably this line is expensive
                if (e != null) {
                    queue.remove(e);
                }
                data.filesInQueue.remove(file);
                lastFileInProject = data.filesInQueue.isEmpty() && data.filesBeingParsed.isEmpty();
                if (lastFileInProject) {
                    removeProjectData(project);
                }
                notifyListeners = lastFileInProject && data.notifyListeners;
            }
        }

        if (lastFileInProject) {
            project.onParseFinish(false);
            if (notifyListeners) {
                ProgressSupport.instance().fireProjectParsingFinished(project);
            }
        }
    }

    public void shutdown() {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ParserQueue: clearing"); // NOI18N
        }
        Collection<ProjectBase> copiedProjects = null;
        synchronized (lock) {
            state = State.OFF;
            queue.clear();
            copiedProjects = new ArrayList<ProjectBase>(projectData.keySet());
            lock.notifyAll();
        }
        for (ProjectBase prj : copiedProjects) {
            ProgressSupport.instance().fireProjectParsingFinished(prj);
        }
    }

    public void startup() {
        state = State.ON;
    }

    public void removeAll(ProjectBase project) {
        ProjectData data;
        boolean lastFileInProject;
        synchronized (lock) {
            data = _clean(project);
            lastFileInProject = data.filesBeingParsed.isEmpty();
            if (lastFileInProject) {
                removeProjectData(project);
            }
        }
        if (lastFileInProject) {
            project.onParseFinish(false);
            if (data.notifyListeners) {
                ProgressSupport.instance().fireProjectParsingFinished(project);
            }
        }
    }

    /**
     * Clean query without any notifications.
     * Used for recreate project query after error recovery.
     */
    public void clean(ProjectBase project) {
        synchronized (lock) {
            _clean(project);
        }
    }

    private ProjectData _clean(ProjectBase project) {
        ProjectData data = getProjectData(project, true);
        for (Object file : data.filesInQueue) {
            Entry e = findEntry((FileImpl) file);
            queue.remove(e);
        }
        data.filesInQueue.clear();
        return data;
    }
    /**
     * Determines whether any files of the given project are now being parsed
     * @return true if any files of the project are being parsed, otherwise false
     */
    public boolean isParsing(ProjectBase project) {
        synchronized (lock) {
            ProjectData data = getProjectData(project, false);
            if (data != null) {
                return !data.filesBeingParsed.isEmpty();
            }
        }
        return false;
    }

    public boolean hasFiles(ProjectBase project, FileImpl skipFile) {
        return hasFiles(project, skipFile, true);
    }

    private boolean hasFiles(ProjectBase project, FileImpl skipFile, boolean create) {
        synchronized (lock) {
            ProjectData data = getProjectData(project, create);
            if (data == null || data.isEmpty()) {
                // nothing in queue and nothing in progress => no files
                return false;
            } else {
                if (skipFile == null) {
                    // not empty, but nothing to skip => has files
                    return true;
                } else {
                    if (data.filesBeingParsed.contains(skipFile) ||
                            data.filesInQueue.contains(skipFile)) {
                        return data.filesBeingParsed.size() + data.filesInQueue.size() > 1;
                    }
                    return !data.isEmpty();
                }
            }
        }
    }

    private Set<FileImpl> getProjectFiles(ProjectBase project) {
        return getProjectData(project, true).filesInQueue;
    }

    private ProjectData getProjectData(ProjectBase project, boolean create) {
        // must be in synchronized( lock ) block
        synchronized (lock) {
            ProjectBase key = project;
            ProjectData data = projectData.get(key);
            if (data == null && create) {
                data = new ProjectData(false);
                projectData.put(key, data);
            }
            return data;
        }
    }

    private void removeProjectData(ProjectBase project) {
        // must be in synchronized( lock ) block
        synchronized (lock) {
            projectData.remove(project);
        }
    }

    private boolean needEnqueue(FileImpl file) {
        // with multiple parse we can not check parsed state
        return !file.getProjectImpl(true).isDisposing() || addAlways;
    }

    public void onStartAddingProjectFiles(ProjectBase project) {
        getProjectData(project, true).notifyListeners = true;
        ProgressSupport.instance().fireProjectParsingStarted(project);
    }

    public void onEndAddingProjectFiles(ProjectBase project) {
        int cnt = getProjectFiles(project).size();
        ProgressSupport.instance().fireProjectFilesCounted(project, cnt);
        if (cnt == 0) {
            ProgressSupport.instance().fireProjectParsingFinished(project);
        }
    }

    /*package*/ void onFileParsingFinished(FileImpl file) {
        boolean lastFileInProject;
        boolean idle = false;
        ProjectBase project;
        ProjectData data;
        synchronized (lock) {
            project = file.getProjectImpl(true);
            data = getProjectData(project, true);
            data.filesBeingParsed.remove(file);
            lastFileInProject = data.filesInQueue.isEmpty() && data.filesBeingParsed.isEmpty();
            if (lastFileInProject) {
                removeProjectData(project);
                idle = projectData.isEmpty();
                // this work only for a single project
                // but on the other hand in the case of multiple projects such measuring will never work
                // since project files might be shuffled in queue
                if (TraceFlags.TIMING && stopWatch != null && stopWatch.isRunning()) {
                    stopWatch.stopAndReport("=== Stopping parser queue stopwatch: \t"); // NOI18N
                }
            }
            lock.notifyAll();
        }
        ProgressSupport.instance().fireFileParsingFinished(file);
        if (lastFileInProject) {
            if (TraceFlags.TRACE_CLOSE_PROJECT) {
                System.err.println("Last file in project " + project.getName()); // NOI18N
            }
            project.onParseFinish(false);
            if (data.notifyListeners) {
                ProgressSupport.instance().fireProjectParsingFinished(project);
            }
            if (idle) {
                ProgressSupport.instance().fireIdle();
            }
            // notify all "wait" empty listeners
            notifyWaitEmpty(project);
        }
    }

    private void notifyWaitEmpty(ProjectBase project) {
        Object prjWaitEmptyLock;
        synchronized (projectLocks) {
            prjWaitEmptyLock = projectLocks.remove(project);
        }
        if (prjWaitEmptyLock != null) {
            synchronized (prjWaitEmptyLock) {
                prjWaitEmptyLock.notifyAll();
            }
        }
    }

    private static final class ProjectWaitLock {}

    /*package*/ void waitEmpty(ProjectBase project) {
        if (TraceFlags.TRACE_CLOSE_PROJECT) {
            System.err.println("Waiting Empty Project " + project.getName()); // NOI18N
        }
        while (hasFiles(project, null, false)) {
            if (TraceFlags.TRACE_CLOSE_PROJECT) {
                System.err.println("Waiting Empty Project 2 " + project.getName()); // NOI18N
            }
            Object prjWaitEmptyLock;
            synchronized (projectLocks) {
                prjWaitEmptyLock = projectLocks.get(project);
                if (prjWaitEmptyLock == null) {
                    prjWaitEmptyLock = new ProjectWaitLock();
                    projectLocks.put(project, prjWaitEmptyLock);
                }
            }
            synchronized (prjWaitEmptyLock) {
                try {
                    prjWaitEmptyLock.wait();
                } catch (InterruptedException ex) {
                    // nothing
                }
            }
        }
        if (TraceFlags.TRACE_CLOSE_PROJECT) {
            System.err.println("Finished waiting on Empty Project " + project.getName()); // NOI18N
        }
    }

    public long getStopWatchTime() {
        return TraceFlags.TIMING ? stopWatch.getTime() : -1;
    }

    private String toString(PriorityQueue<Entry> queue, boolean detailed) {
        StringBuilder builder = new StringBuilder();
        for (Entry e : queue) {
            builder.append(e.toString(detailed)).append("\n"); // NOI18N
        }
        return builder.toString();
    }

    private Entry findEntry(FileImpl file) {
//        return fileEntry.get(file);
        int fileHashCode = file.hashCode();
        for (Entry e : queue) {
            FileImpl f = e.getFile();
            if (f == file) {
                return e;
            }
            if (fileHashCode == f.hashCode()) {
                if (file.equals(f)) {
                    return e;
                }
            }
        }
        return null;
    }
}
