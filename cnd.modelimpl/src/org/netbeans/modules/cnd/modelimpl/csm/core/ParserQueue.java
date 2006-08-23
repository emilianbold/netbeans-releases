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
import org.netbeans.modules.cnd.modelimpl.antlr2.PPCallback;
import org.netbeans.modules.cnd.modelimpl.apt.support.APTPreprocState;
import org.netbeans.modules.cnd.modelimpl.csm.Diagnostic;

/**
 * A queue that hold a list of files to parse.
 * @author Vladimir Kvashin
 */
public class ParserQueue {

    public static class Entry {
        
        private FileImpl file;
        private PPCallback callback;
        private APTPreprocState.State ppStateState;
        private Entry prev;
        private Entry next;
        
        private Entry(FileImpl file) {
            this(file, file.getPreprocStateState());
        }

        private Entry(FileImpl file, APTPreprocState.State ppStateState) {
            this.file = file;
            this.ppStateState = ppStateState;            
        }
        
        public FileImpl getFile() {
            return file;
        }
        
        public PPCallback getCallback() {
            return callback;
        }
        
        public APTPreprocState.State getPreprocStateState() {
            return ppStateState;
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
            }
            else {
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
    
    private class ProjectData {
        
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
    
    private static ParserQueue instance = new ParserQueue();
    
    private Queue queue = new Queue();
    
    private Map/*<ProjectBase, ProjectData>*/ projectData = new HashMap()/*<ProjectBase, ProjectData>*/;
    
    private Object lock = new Object();
    
    private Collection/*<CsmProgressListener>*/ progressListeners = new  LinkedList/*<CsmProgressListener>*/();
    
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
    
    public void addLast(FileImpl file, APTPreprocState.State ppStateState) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: addLast " + file.getName());
        synchronized ( lock ) {
            Set/*<FileImpl>*/ files = getProjectFiles(file.getProjectImpl());
            if( ! files.contains(file) ) {
                files.add(file);
                //queue.add(file);
                queue.addLast(new Entry(file, ppStateState));
                lock.notifyAll();
            }
        }
    }
    
    /**
     * If file isn't yet enqueued, places it at the beginning of the queue,
     * otherwise moves it there
     */
    public void addFirst(FileImpl file) {
        addFirst(file, file.getPreprocStateState());
    }   
    
    public void addFirst(FileImpl file, APTPreprocState.State ppStateState) {
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: addFirst " + file.getName());
        synchronized ( lock ) {
            Set/*<FileImpl>*/ files = getProjectFiles(file.getProjectImpl());
            if( files.contains(file) ) {
                if( queue.peek() != null && queue.peek().getFile() == file ) {
                    return;
                }
                Entry e = queue.find(file); //TODO: think over / profile, probably this line is expensive
                if( e != null ) {
                    queue.remove(e);
                }
            }
            else {
                files.add(file);
            }
            queue.addFirst(new Entry(file, ppStateState));
            lock.notifyAll();
        }
    }
    
    public void waitReady() throws InterruptedException {
        synchronized ( lock ) {
            if( queue.isEmpty() ) {
                if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: waitReady() ...");
                lock.wait();
                if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: waiting finished");
            }
        }
    }
    
    public Entry poll() {
        
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
        if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: polling " + e.getFile().getName());
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
    
    public boolean hasFiles(ProjectBase project) {
        synchronized ( lock ) {
            return ! getProjectData(project).isEmpty();
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
            
    private  CsmProgressListener[] getProgressListeners() {
        synchronized ( progressListeners ) {
            return (CsmProgressListener[]) progressListeners.toArray(new CsmProgressListener[progressListeners.size()]);
        }
    }
    
    public void addProgressListener(CsmProgressListener listener) {
        synchronized ( progressListeners ) {
            progressListeners.add(listener);
        }
    }
    
    public void removeProgressListener(CsmProgressListener listener) {
        synchronized ( progressListeners ) {
            progressListeners.remove(listener);
        }
    }
    
    public void onStartAddingProjectFiles(ProjectBase project) {
        getProjectData(project).notifyListeners = true;
        fireProjectParsingStarted(project);
    }
    
    private void fireFileParsingStarted(FileImpl file) {
	if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireFileParsingStarted " + file.getName());
        CsmProgressListener[] listeners = getProgressListeners(); 
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].fileParsingStarted(file);
        }
    }
   
    private void fireFileParsingFinished(FileImpl file) {
	if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireFileParsingFinished " + file.getName());
        CsmProgressListener[] listeners = getProgressListeners(); 
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].fileParsingFinished(file);
        }
    }

    private void fireProjectParsingStarted(ProjectBase project) {
	if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireProjectParsingStarted " + project.getName());
        CsmProgressListener[] listeners = getProgressListeners(); 
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].projectParsingStarted(project);
        }
    }
    
    private void fireProjectParsingFinished(ProjectBase project) {
        CsmProgressListener[] listeners = getProgressListeners(); 
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].projectParsingFinished(project);
        }
    }

    public void onEndAddingProjectFiles(ProjectBase project) {
        ProjectData data = getProjectData(project);
        int cnt = data.filesInQueue.size();
	if( cnt == 0 ) {
	    fireProjectParsingFinished(project);
	}
	if( TraceFlags.TRACE_PARSER_QUEUE ) System.err.println("ParserQueue: fireProjectFilesCounted " + project.getName() + ' ' + cnt);
        CsmProgressListener[] listeners = getProgressListeners();
        for (int i = 0; i < listeners.length; i++) {
	    listeners[i].projectFilesCounted(project, cnt);
        }
    }
    
    public void onFileParsingFinished(FileImpl file) {
        boolean lastFileInProject;
        ProjectBase project;
        ProjectData data;
        synchronized (lock) {
            project = file.getProjectImpl();
            data = getProjectData(project);
            data.filesBeingParsed.remove(file);
            lastFileInProject = data.filesInQueue.isEmpty() && data.filesBeingParsed.isEmpty();
            if( lastFileInProject ) {
                projectData.remove(project);
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
