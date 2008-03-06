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
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 * A queue that hold a list of files to parse.
 * @author Vladimir Kvashin
 */
public final class ParserQueue {
    
    public static class Entry {
        
        private FileImpl file;
        private APTPreprocHandler.State ppState;
        private Entry prev;
        private Entry next;
        
        private Entry(FileImpl file, APTPreprocHandler.State ppState) {
            if( TraceFlags.TRACE_PARSER_QUEUE ) {
                System.err.println("creating entry for " + file.getAbsolutePath() +
                        " as " + tracePreprocState(ppState)); // NOI18N
            }
            this.file = file;
            this.ppState = ppState;
        }
        
        public FileImpl getFile() {
            return file;
        }
        
        public APTPreprocHandler.State getPreprocState() {
            return ppState;
        }
        
        @Override
        public String toString() {
            return toString(true);
        }
        
        public String toString(boolean detailed) {
            StringBuilder retValue = new StringBuilder();
            retValue.append("ParserQueue.Entry " + file + " of project " + file.getProject()); // NOI18N
            if( detailed ) {
                retValue.append("\nwith PreprocState:\n"+ppState); // NOI18N
            }
            return retValue.toString();
        }
        
        public void setPreprocStateIfNeed(APTPreprocHandler.State ppState) {
            // TODO: IZ#87204: AssertionError on _Bvector_base opening
            // review why it could be null
            // FIXUP: remove assert checks and update if statements to prevent NPE
            //            assert (ppState != null) : "why do pass null snapshot?";
            //            assert (this.ppState != null) : "if it was already included, where is the state?";

            if( TraceFlags.TRACE_PARSER_QUEUE ) {
                System.err.println("setPreprocStateIfNeed for " + file.getAbsolutePath() +
                        " as " + tracePreprocState(ppState) + " with current " + tracePreprocState(this.ppState)); // NOI18N
            }
            if (file.isNeedReparse(this.ppState, ppState)){
                this.ppState = ppState;                    
            }
        }
    }
    
    /*package*/static String tracePreprocState(APTPreprocHandler.State ppState) {
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
    
    private static class Queue {
	
        private Map<FileImpl,Entry> storage = new HashMap<FileImpl,Entry>();
        private Entry head;
        private Entry tail;
        
        private void link(Entry e1, Entry e2) {
            if( e1 != null ) {
                e1.next = e2;
            }
            if( e2 != null ) {
                e2.prev = e1;
            }
        }
        
        public void addFirst(Entry e) {
            Entry old = storage.put(e.file,e);
            assert old == null : "only one entry per file must be in queue " + e.file;
            link(e, head);
            head = e;
            if( tail == null ) {
                tail = head;
            }
            e.prev = null;
        }
        
        public void addLast(Entry e) {
            if( tail == null ) {
                assert head == null;
                addFirst(e);
            } else {
                Entry old = storage.put(e.file,e);
                assert old == null : "only one entry per file must be in queue " + e.file;
                link(tail, e);
                tail = e;
            }
            e.next = null;
        }
        
        public void remove(Entry e) {
            Entry old = storage.remove(e.file);
            assert old != null : "there were no entry in queue for file " + e.file;
            link(e.prev, e.next);
            if( head == e ) {
                head = e.next;
            }
            if( tail == e ) {
                tail = e.prev;
            }
	    // clear enthry in case someone holds a reference to it
	    e.prev = e.next = null;
        }
        
        public void removeAll(Collection/*<FileImple>*/ files) {
//            for( Entry curr = head; curr != null; curr = curr.next ) {
//                if( files.contains(curr.getFile()) ) {
//                    remove(curr);
//                }
//            }
	    Entry curr = head;
	    while(curr != null) {
		Entry next = curr.next; // after removal curr.next will be null!
                if( files.contains(curr.getFile()) ) {
                    remove(curr);
                }
		curr = next;
	    }
        }
        
        public void clear() {
            storage.clear();
            head = tail = null;
        }
        
        public Entry peek() {
            return head;
        }
        
        public boolean isEmpty() {
            return head == null;
        }
        
        public Entry find(FileImpl file) {
            return storage.get(file);
//            for( Entry curr = head; curr != null; curr = curr.next ) {
//                if( curr.getFile() == file ) {
//                    return curr;
//                }
//            }
//            return null;
        }
        
        public Entry poll() {
            Entry ret = head;
            if( head != null ) {
                remove(head);
            }
            return ret;
        }

        public String toString(boolean detailed) {
            StringBuilder builder = new StringBuilder();
            Entry cur = head;
            while (cur != null) {
                builder.append(cur.toString(detailed)).append("\n"); // NOI18N
                cur = cur.next;
            }
            return builder.toString();
        }
    }
    
    private static final class ProjectData {
        
        public Set/*<FileImpl>*/ filesInQueue = new HashSet/*<FileImpl>*/();
        
        // there are no more simultaneously parsing files than threads, so LinkedList suites even better
        public Collection/*<FileImpl>*/ filesBeingParsed = new LinkedList/*<FileImpl>*/();
        
        public boolean notifyListeners;
        
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
    
    private static ParserQueue instance = new ParserQueue();
    
    private Queue queue = new Queue();
    
    private State state;
    private Object suspendLock = new String("suspendLock"); // NOI18N
    
    // do not need UIDs for ProjectBase in parsing data collection
    private Map<ProjectBase, ProjectData> projectData = new HashMap<ProjectBase, ProjectData>();
    private Map<CsmProject, Object> projectLocks = new HashMap<CsmProject, Object>();
    
    private Object lock = new Object();
    
    //private WeakList<CsmProgressListener> progressListeners = new WeakList<CsmProgressListener>();
    
    private Diagnostic.StopWatch stopWatch = TraceFlags.TIMING ? new Diagnostic.StopWatch(false) : null;
    
    private ParserQueue() {
    }
    
    public static ParserQueue instance() {
        return instance;
    }
    
    /**
     * Puts the given file at the end of the queue
     * (In the case it isn't already enqueued;
     * if it already is, does nothing)
     */
//    public void addLast(FileImpl file) {
//        addLast(file, file.getPreprocState());
//    }
    
    //    public void addLast(FileImpl file, APTPreprocHandler.State ppState, boolean onInclude) {
    //        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: addLast " + file.getName());
    //        synchronized ( lock ) {
    //            Set/*<FileImpl>*/ files = getProjectFiles(file.getProjectImpl());
    //            if( ! files.contains(file) ) {
    //                files.add(file);
    //                //queue.add(file);
    //                Entry entry = new Entry(file, ppState, onInclude);
    //                if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: added as Last with entry " + entry);
    //                queue.addLast(entry);
    //                lock.notifyAll();
    //            }
    //        }
    //    }
    
    public void addLast(FileImpl file, APTPreprocHandler.State ppState) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: addLast " + file.getAbsolutePath());
        synchronized ( lock ) {
            if( state == State.OFF ) return;
            if (!needEnqueue(file)) {
                if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: do not addLast for parsing or parsed " + file.getAbsolutePath());
                return;
            }
            Set/*<FileImpl>*/ files = getProjectFiles(file.getProjectImpl());
            Entry entry = null;
            if( files.contains(file) ) {
                entry = queue.find(file); //TODO: think over / profile, probably this line is expensive
                if( entry != null ) {
                    entry.setPreprocStateIfNeed(ppState);
                }
            } else {
		assert (queue.find(file) == null) : "The queue should not contain the file " + traceState4File(file, files);// NOI18N
                files.add(file);
            }
            if (entry == null) {
                entry = new Entry(file, ppState);
                if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: added as Last with entry " + entry.toString(TraceFlags.TRACE_PARSER_QUEUE_DETAILS));
                queue.addLast(entry);
            }
            lock.notifyAll();
        }
	ProgressSupport.instance().fireFileInvalidated(file);
    }

    private String traceState4File(FileImpl file, Set/*<FileImpl>*/ files) {
        StringBuilder builder = new StringBuilder(" "); // NOI18N
        builder.append(file);
        builder.append("\n of project ").append(file.getProjectImpl()); // NOI18N
        builder.append("\n content of projects files set:\n"); // NOI18N
        builder.append(files);
        builder.append("\nqueue content is:\n"); // NOI18N
        builder.append(queue.toString(false));
        builder.append("\nprojectData content is:\n"); // NOI18N
        builder.append(projectData);
        return builder.toString();
    }
    
    /**
     * If file isn't yet enqueued, places it at the beginning of the queue,
     * otherwise moves it there
     */
//    public void addFirst(FileImpl file) {
//        addFirst(file, file.getPreprocState(), false);
//    }
    
    public void addFirst(FileImpl file, APTPreprocHandler.State ppState, boolean onInclude) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: addFirst " + file.getAbsolutePath());
        synchronized ( lock ) {
            if( state == State.OFF  ) return;
            if (!needEnqueue(file)) {
                if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: do not addFirst for parsing or parsed " + file.getAbsolutePath());
                return;
            }
            Set/*<FileImpl>*/ files = getProjectFiles(file.getProjectImpl());
            Entry entry = null;
            if( files.contains(file) ) {
                entry = queue.find(file); //TODO: think over / profile, probably this line is expensive
                if( entry != null ) {
                    queue.remove(entry);
                    entry.setPreprocStateIfNeed(ppState);
                }
            } else {
		assert (queue.find(file) == null) : "The queue should not contain the file " + traceState4File(file, files); // NOI18N
                files.add(file);
            }
            if (entry == null) {
                entry = new Entry(file, ppState);
            }
            if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: added as First with entry " + entry.toString(TraceFlags.TRACE_PARSER_QUEUE_DETAILS));
            queue.addFirst(entry);
            lock.notifyAll();
        }
	ProgressSupport.instance().fireFileInvalidated(file);
    }
    
    public void waitReady() throws InterruptedException {
	if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: waitReady() ...");
        synchronized ( lock ) {
            while( queue.isEmpty() && state != State.OFF ) {
                lock.wait();
            }
        }
	if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: waiting finished");
    }
    
    public void suspend() {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: suspending");
        synchronized (suspendLock) {
            state = State.SUSPENDED;
        }
    }
    
    public void resume() {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: resuming");
        synchronized (suspendLock) {
            state = State.ON;
            suspendLock.notifyAll();
        }
    }
    
    public Entry poll() throws InterruptedException {
        
        synchronized (suspendLock) {
            while( state == State.SUSPENDED ) {
                if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: waiting for resume");
                suspendLock.wait();
            }
        }
        
        Entry e = null;
        
        ProjectBase project;
        boolean lastFileInProject;
        boolean notifyListeners;
        
	FileImpl file = null;
	
        synchronized( lock ) {
            e = queue.poll();
            if( e == null ) {
                return null;
            }
            file = e.getFile();
            project = file.getProjectImpl();
            ProjectData data = getProjectData(project, true);
            data.filesInQueue.remove(file);
            data.filesBeingParsed.add(file);
            lastFileInProject = data.filesInQueue.isEmpty() && data.filesBeingParsed.isEmpty();
            if( lastFileInProject) {
                removeProjectData(project);
            }
            notifyListeners = lastFileInProject && data.notifyListeners;
            if( TraceFlags.TIMING && stopWatch != null && ! stopWatch.isRunning() ) {
                stopWatch.start();
                System.err.println("=== Starting parser queue stopwatch");
            }
        }
	// TODO: think over, whether this should be under if( notifyListeners
	ProgressSupport.instance().fireFileParsingStarted(file);
        if( lastFileInProject ) {
            project.onParseFinish();
            if( notifyListeners ) {
                ProgressSupport.instance().fireProjectParsingFinished(project);
            }
        }
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: polling " + e.getFile().getAbsolutePath());
        return e;
    }
    
    public void remove(FileImpl file) {
        
        ProjectBase project;
        boolean lastFileInProject = false;
        boolean notifyListeners = false;
        
        synchronized ( lock ) {
            project = file.getProjectImpl();
            ProjectData data = getProjectData(project, true);
            if( data.filesInQueue.contains(file) ) {
                //queue.remove(file); //TODO: think over / profile, probably this line is expensive
                Entry e = queue.find(file);//TODO: think over / profile, probably this line is expensive
                if( e != null ) {
                    queue.remove(e);
                }
                data.filesInQueue.remove(file);
                lastFileInProject = data.filesInQueue.isEmpty() && data.filesBeingParsed.isEmpty();
                if( lastFileInProject) {
                    removeProjectData(project);
                }
                notifyListeners = lastFileInProject && data.notifyListeners;
            }
        }
        
        if( lastFileInProject ) {
            project.onParseFinish();
            if( notifyListeners ) {
                ProgressSupport.instance().fireProjectParsingFinished(project);
            }
        }
    }
    
    public void shutdown() {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: clearing");
	Collection<ProjectBase> copiedProjects = null; 
        synchronized ( lock ) {
            state = State.OFF;
            queue.clear();
	    copiedProjects = new ArrayList<ProjectBase>(projectData.keySet());
            lock.notifyAll();
        }
	for( ProjectBase prj  : copiedProjects ) {
	    ProgressSupport.instance().fireProjectParsingFinished( prj );
	}
    }
    
    public void startup() {
        state = State.ON;
    }
    
    public void removeAll(ProjectBase project) {
        ProjectData data;
        boolean lastFileInProject;
        synchronized ( lock ) {
            data = getProjectData(project, true);
            queue.removeAll(data.filesInQueue);
            data.filesInQueue.clear();
            lastFileInProject = data.filesBeingParsed.isEmpty();
            if( lastFileInProject) {
                removeProjectData(project);
            }
        }
        if( lastFileInProject) {
            project.onParseFinish();
            if( data.notifyListeners ) {
                ProgressSupport.instance().fireProjectParsingFinished(project);
            }
        }
    }
    
    /**
     * Determines whether any files of the given project are now being parsed
     * @return true if any files of the project are being parsed, otherwise false
     */
    public boolean isParsing(ProjectBase project) {
        synchronized ( lock ) {
            ProjectData data = getProjectData(project, false);
            if( data != null ) {
                return ! data.filesBeingParsed.isEmpty();
            }
        }
        return false;
    }
    
    public boolean hasFiles(ProjectBase project, FileImpl skipFile) {
        return hasFiles(project, skipFile, true);
    }

    private boolean hasFiles(ProjectBase project, FileImpl skipFile, boolean create) {
        synchronized ( lock ) {
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
                    return ! data.isEmpty();
                }
            }
        }
    }
    
    private Set/*<FileImpl>*/ getProjectFiles(ProjectBase project) {
        return getProjectData(project, true).filesInQueue;
    }
    
    private ProjectData getProjectData(ProjectBase project, boolean create) {
        // must be in synchronized( lock ) block
        synchronized (lock) {
            ProjectBase key = project;
            ProjectData data = projectData.get(key);
            if( data == null && create) {
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
        // we know, that each file is parsed only once =>
        // let's speed up work with queue ~75% by skipping such files
        // Also check that file project was not closed
        return !file.isParsed() && !file.getProjectImpl().isDisposing();
    }
    
    public void onStartAddingProjectFiles(ProjectBase project) {
        getProjectData(project, true).notifyListeners = true;
        ProgressSupport.instance().fireProjectParsingStarted(project);
    }

    public void onEndAddingProjectFiles(ProjectBase project) {
        int cnt = getProjectFiles(project).size();
        ProgressSupport.instance().fireProjectFilesCounted(project, cnt);
        if( cnt == 0 ) {
            ProgressSupport.instance().fireProjectParsingFinished(project);
        }
    }
    
    /*package*/ void onFileParsingFinished(FileImpl file) {
        boolean lastFileInProject;
        boolean idle = false;
        ProjectBase project;
        ProjectData data;
        synchronized (lock) {
            project = file.getProjectImpl();
            data = getProjectData(project, true);
            data.filesBeingParsed.remove(file);
            lastFileInProject = data.filesInQueue.isEmpty() && data.filesBeingParsed.isEmpty();
            if( lastFileInProject ) {
                removeProjectData(project);
                idle = projectData.isEmpty();
                // this work only for a single project
                // but on the other hand in the case of multiple projects such measuring will never work
                // since project files might be shuffled in queue
                if( TraceFlags.TIMING && stopWatch != null && stopWatch.isRunning() ) {
                    stopWatch.stopAndReport("=== Stopping parser queue stopwatch: \t"); // NOI18N
                }
            }
        }
        ProgressSupport.instance().fireFileParsingFinished(file);
        if( lastFileInProject ) {
            if (TraceFlags.TRACE_CLOSE_PROJECT) System.err.println("Last file in project " + project.getName());
            project.onParseFinish();
            if( data.notifyListeners ) {
                ProgressSupport.instance().fireProjectParsingFinished(project);
            }
            if( idle ) {
                ProgressSupport.instance().fireIdle();
            }
            // notify all "wait" empty listeners
            notifyWaitEmpty(project);
        }
    }
    
    private void notifyWaitEmpty(ProjectBase project){
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

    /*package*/ void waitEmpty(ProjectBase project) {
        if (TraceFlags.TRACE_CLOSE_PROJECT) System.err.println("Waiting Empty Project " + project.getName());
        while (hasFiles(project, null, false)) {
            if (TraceFlags.TRACE_CLOSE_PROJECT) System.err.println("Waiting Empty Project 2 " + project.getName());
            Object prjWaitEmptyLock;
            synchronized (projectLocks) {
                prjWaitEmptyLock = projectLocks.get(project);
                if (prjWaitEmptyLock == null) {
                    prjWaitEmptyLock = new String(project.getName().toString());
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
        if (TraceFlags.TRACE_CLOSE_PROJECT) System.err.println("Finished waiting on Empty Project " + project.getName());
    }
    
    public long getStopWatchTime() {
        return TraceFlags.TIMING ? stopWatch.getTime() : -1;
    }
}
