/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.util.WeakList;
import org.netbeans.modules.cnd.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 * A queue that hold a list of files to parse.
 * @author Vladimir Kvashin
 */
public class ParserQueue {
    
    public static class Entry {
        
        private FileImpl file;
        private APTPreprocState.State ppStateState;
        private Entry prev;
        private Entry next;
        
        //        private Entry(FileImpl file) {
        //            this(file, file.getPreprocStateState(), false);
        //        }
        
        private Entry(FileImpl file, APTPreprocState.State ppStateState) {
            this.file = file;
            this.ppStateState = ppStateState;
        }
        
        public FileImpl getFile() {
            return file;
        }
        
        public APTPreprocState.State getPreprocStateState() {
            return ppStateState;
        }
        
        public String toString() {
            return toString(true);
        }
        
        public String toString(boolean detailed) {
            StringBuffer retValue = new StringBuffer();
            retValue.append("ParserQueue.Entry " + file.getAbsolutePath());
            if( detailed ) {
                retValue.append("\nwith PreprocStateState:\n"+ppStateState);
            }
            return retValue.toString();
        }
        
        public void setPreprocStateStateIfNeed(APTPreprocState.State ppStateState) {
            // TODO: IZ#87204: AssertionError on _Bvector_base opening
            // review why it could be null
            // FIXUP: remove assert checks and update if statements to prevent NPE
            //            assert (ppStateState != null) : "why do pass null snapshot?";
            //            assert (this.ppStateState != null) : "if it was already included, where is the state?";
            if (this.ppStateState != null && this.ppStateState.isStateCorrect()) {
                // do nothing
            } else if (ppStateState != null && ppStateState.isStateCorrect()) {
                // override state with new one
                this.ppStateState = ppStateState;
            }
        }
    }
    
    
    private static class Queue {
        
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
                link(tail, e);
                tail = e;
            }
            e.next = null;
        }
        
        public void remove(Entry e) {
            link(e.prev, e.next);
            if( head == e ) {
                head = e.next;
            }
            if( tail == e ) {
                tail = e.prev;
            }
        }
        
        public void removeAll(Collection/*<FileImple>*/ files) {
            for( Entry curr = head; curr != null; curr = curr.next ) {
                if( files.contains(curr.getFile()) ) {
                    remove(curr);
                }
            }
        }
        
        public void clear() {
            head = tail = null;
        }
        
        public Entry peek() {
            return head;
        }
        
        public boolean isEmpty() {
            return head == null;
        }
        
        public Entry find(FileImpl file) {
            for( Entry curr = head; curr != null; curr = curr.next ) {
                if( curr.getFile() == file ) {
                    return curr;
                }
            }
            return null;
        }
        
        public Entry poll() {
            Entry ret = head;
            if( head != null ) {
                remove(head);
            }
            return ret;
        }
    }
    
    private static class ProjectData {
        
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
    private Object suspendLock = "suspendLock";
    
    private Map/*<ProjectBase, ProjectData>*/ projectData = new HashMap()/*<ProjectBase, ProjectData>*/;
    
    private Object lock = new Object();
    
    private WeakList<CsmProgressListener> progressListeners = new WeakList<CsmProgressListener>();
    
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
    public void addLast(FileImpl file) {
        addLast(file, file.getPreprocStateState());
    }
    
    //    public void addLast(FileImpl file, APTPreprocState.State ppStateState, boolean onInclude) {
    //        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: addLast " + file.getName());
    //        synchronized ( lock ) {
    //            Set/*<FileImpl>*/ files = getProjectFiles(file.getProjectImpl());
    //            if( ! files.contains(file) ) {
    //                files.add(file);
    //                //queue.add(file);
    //                Entry entry = new Entry(file, ppStateState, onInclude);
    //                if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: added as Last with entry " + entry);
    //                queue.addLast(entry);
    //                lock.notifyAll();
    //            }
    //        }
    //    }
    
    public void addLast(FileImpl file, APTPreprocState.State ppStateState) {
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
                    entry.setPreprocStateStateIfNeed(ppStateState);
                }
            } else {
                files.add(file);
            }
            if (entry == null) {
                entry = new Entry(file, ppStateState);
                if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: added as Last with entry " + entry.toString(TraceFlags.TRACE_PARSER_QUEUE_DETAILS));
                queue.addLast(entry);
            }
            lock.notifyAll();
        }
    }
    
    /**
     * If file isn't yet enqueued, places it at the beginning of the queue,
     * otherwise moves it there
     */
    public void addFirst(FileImpl file) {
        addFirst(file, file.getPreprocStateState(), false);
    }
    
    public void addFirst(FileImpl file, APTPreprocState.State ppStateState, boolean onInclude) {
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
                    entry.setPreprocStateStateIfNeed(ppStateState);
                }
            } else {
                files.add(file);
            }
            if (entry == null) {
                entry = new Entry(file, ppStateState);
            }
            if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: added as First with entry " + entry.toString(TraceFlags.TRACE_PARSER_QUEUE_DETAILS));
            queue.addFirst(entry);
            lock.notifyAll();
        }
    }
    
    public void waitReady() throws InterruptedException {
        synchronized ( lock ) {
            while( queue.isEmpty() ) {
                if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: waitReady() ...");
                lock.wait();
                if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: waiting finished");
            }
        }
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
        
        synchronized( lock ) {
            e = queue.poll();
            if( e == null ) {
                return null;
            }
            FileImpl file = e.getFile();
            project = file.getProjectImpl();
            ProjectData data = getProjectData(project);
            data.filesInQueue.remove(file);
            data.filesBeingParsed.add(file);
            fireFileParsingStarted(file);
            lastFileInProject = data.filesInQueue.isEmpty() && data.filesBeingParsed.isEmpty();
            if( lastFileInProject) {
                projectData.remove(project);
            }
            notifyListeners = lastFileInProject && data.notifyListeners;
            if( TraceFlags.TIMING && stopWatch != null && ! stopWatch.isRunning() ) {
                stopWatch.start();
                System.err.println("=== Starting parser queue stopwatch");
            }
        }
        if( lastFileInProject ) {
            project.onParseFinish();
            if( notifyListeners ) {
                fireProjectParsingFinished(project);
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
            ProjectData data = getProjectData(project);
            if( data.filesInQueue.contains(file) ) {
                //queue.remove(file); //TODO: think over / profile, probably this line is expensive
                Entry e = queue.find(file);//TODO: think over / profile, probably this line is expensive
                if( e != null ) {
                    queue.remove(e);
                }
                data.filesInQueue.remove(file);
                lastFileInProject = data.filesInQueue.isEmpty() && data.filesBeingParsed.isEmpty();
                if( lastFileInProject) {
                    projectData.remove(project);
                }
                notifyListeners = lastFileInProject && data.notifyListeners;
            }
        }
        
        if( lastFileInProject ) {
            project.onParseFinish();
            if( notifyListeners ) {
                fireProjectParsingFinished(project);
            }
        }
    }
    
    public void shutdown() {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: clearing");
        synchronized ( lock ) {
            state = State.OFF;
            queue.clear();
            for( Iterator it = projectData.keySet().iterator(); it.hasNext(); ) {
                fireProjectParsingFinished((ProjectBase) it.next() );
            }
            lock.notifyAll();
        }
    }
    
    public void startup() {
        state = State.ON;
    }
    
    public void removeAll(ProjectBase project) {
        ProjectData data;
        boolean lastFileInProject;
        synchronized ( lock ) {
            data = getProjectData(project);
            queue.removeAll(data.filesInQueue);
            data.filesInQueue.clear();
            lastFileInProject = data.filesBeingParsed.isEmpty();
            if( lastFileInProject) {
                projectData.remove(project);
            }
        }
        if( lastFileInProject) {
            project.onParseFinish();
            if( data.notifyListeners ) {
                fireProjectParsingFinished(project);
            }
        }
    }
    
    /**
     * Determines whether any files of the given project are now being parsed
     * @return true if any files of the project are being parsed, otherwise false
     */
    public boolean isParsing(ProjectBase project) {
        synchronized ( lock ) {
            ProjectData data = (ProjectData) projectData.get(project);
            if( data != null ) {
                return ! data.filesBeingParsed.isEmpty();
            }
        }
        return false;
    }
    
    public boolean hasFiles(ProjectBase project, FileImpl skipFile) {
        synchronized ( lock ) {
            ProjectData data = getProjectData(project);
            if (data.isEmpty()) {
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
        return getProjectData(project).filesInQueue;
    }
    
    private ProjectData getProjectData(ProjectBase project) {
        ProjectData data = (ProjectData) projectData.get(project);
        if( data == null ) {
            data = new ProjectData(false);
            projectData.put(project, data);
        }
        return data;
    }
    
    public void addProgressListener(CsmProgressListener listener) {
        progressListeners.add(listener);
    }
    
    public void removeProgressListener(CsmProgressListener listener) {
        progressListeners.remove(listener);
    }
    
    public Iterator<CsmProgressListener> getProgressListeners() {
	return progressListeners.iterator();
    }
    
    public void onStartAddingProjectFiles(ProjectBase project) {
        getProjectData(project).notifyListeners = true;
        fireProjectParsingStarted(project);
    }
    
    private void fireFileParsingStarted(FileImpl file) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireFileParsingStarted " + file.getAbsolutePath());
        for( CsmProgressListener listener : progressListeners ) {
            listener.fileParsingStarted(file);
        }
    }
    
    private boolean needEnqueue(FileImpl file) {
        // we know, that each file is parsed only once =>
        // let's speed up work with queue ~75% by skipping such files
        return !file.isParsingOrParsed();
    }
    
    private void fireFileParsingFinished(FileImpl file) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireFileParsingFinished " + file.getAbsolutePath());
        for( CsmProgressListener listener : progressListeners ) {
            listener.fileParsingFinished(file);
        }
    }
    
    private void fireProjectParsingStarted(ProjectBase project) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireProjectParsingStarted " + project.getName());
        for( CsmProgressListener listener : progressListeners ) {
            listener.projectParsingStarted(project);
        }
    }
    
    private void fireProjectParsingFinished(ProjectBase project) {
        for( CsmProgressListener listener : progressListeners ) {
            listener.projectParsingFinished(project);
        }
    }
    
    private void fireIdle() {
        for( CsmProgressListener listener : progressListeners ) {
            listener.parserIdle();
        }
    }
    
    
    public void onEndAddingProjectFiles(ProjectBase project) {
        ProjectData data = getProjectData(project);
        int cnt = data.filesInQueue.size();
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireProjectFilesCounted " + project.getName() + ' ' + cnt);
        for( CsmProgressListener listener : progressListeners ) {
            listener.projectFilesCounted(project, cnt);
        }
        if( cnt == 0 ) {
            fireProjectParsingFinished(project);
        }
    }
    
    public void onFileParsingFinished(FileImpl file) {
        boolean lastFileInProject;
        boolean idle = false;
        ProjectBase project;
        ProjectData data;
        synchronized (lock) {
            project = file.getProjectImpl();
            data = getProjectData(project);
            data.filesBeingParsed.remove(file);
            lastFileInProject = data.filesInQueue.isEmpty() && data.filesBeingParsed.isEmpty();
            if( lastFileInProject ) {
                projectData.remove(project);
                idle = projectData.isEmpty();
                // this work only for a single project
                // but on the other hand in the case of multiple projects such measuring will never work
                // since project files might be shuffled in queue
                if( TraceFlags.TIMING && stopWatch != null && stopWatch.isRunning() ) {
                    stopWatch.stopAndReport("=== Stopping parser queue stopwatch: \t");
                }
            }
        }
        fireFileParsingFinished(file);
        if( lastFileInProject ) {
            project.onParseFinish();
            if( data.notifyListeners ) {
                fireProjectParsingFinished(project);
            }
            if( idle ) {
                fireIdle();
            }
        }
    }
    
    private int size() {
        int size = 0;
        for (Iterator it = projectData.values().iterator(); it.hasNext();) {
            ProjectData pd = (ProjectData) it.next();
            size += pd.size();
        }
        return size;
    }
}
