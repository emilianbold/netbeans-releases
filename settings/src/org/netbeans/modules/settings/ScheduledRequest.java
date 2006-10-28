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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.*;

/** Provides support for async storing of a setting objects.
 * @author  Jan Pokorsky
 */
public final class ScheduledRequest implements Runnable {
    private final static RequestProcessor PROCESSOR =
        new RequestProcessor("Settings Processor"); //NOI18N
    private final static int DELAY = 2000;
    
    private Object inst;
    private Task task;
    private FileLock lock;
    private final FileObject fobj;
    private final FileSystem.AtomicAction run;
    private boolean running = false;
    /** counter of scheduled tasks */
    private int counter = 0;
    
    /** Creates a new instance of ScheduledRequest
     * @param fobj file containing a persistent setting object
     * @param run impl performing the storing task
     */
    public ScheduledRequest(FileObject fobj, FileSystem.AtomicAction run) {
        this.fobj = fobj;
        this.run = run;
    }
    
    /** Is the request being executed? */
    public boolean isRunning() {
        return running;
    }
    
    /** Provide a file lock; can be null if it is not invoked from the impl of
     * run.run() (see {@link #ScheduledRequest}). Do not release the lock,
     * it is the ScheduledRequest responsibility!
     */
    public FileLock getFileLock() {
        return lock;
    }
    
    /** Schedule the request, lock the file and keep the obj reference.
     * @param obj a setting object. ScheduledRequest prevents object to be GCed
     * before running the request task
     */
    public synchronized void schedule(Object obj) {
        schedule(obj, DELAY);
    }
    
    private void schedule(Object obj, int delay) {
        if (obj == null) return;
        if (inst != null && inst != obj)
            throw new IllegalStateException("Inconsistant state! File: " + fobj); //NOI18N
        
        try {
            if (lock == null) lock = fobj.lock();
        } catch (IOException ex) {
            Logger.getLogger(ScheduledRequest.class.getName()).log(Level.WARNING, null, ex);
            return;
        }
        
        counter++;
        if (task == null) {
            task = PROCESSOR.post(this, delay);
        } else {
            task.schedule(delay);
        }
        
        // prevent object to be GCed before running the task
        this.inst = obj;
    }
    
    /** Try to cancel the scheduled request if exists and perform the request task.
     * @exception IOException if the storing fails
     */
    public synchronized void runAndWait() throws IOException {
        if (task != null) {
            // if anything was scheduled cancel it
            task.cancel();
            counter = 0;
            releaseResources();
        }
        lock = fobj.lock();
        performRequest();
    }
    
    /** force to finish the scheduled request */
    public void forceToFinish() {
        Task t = null;
        synchronized (this) {
            if (task != null) {
                t = task;
                task.schedule(0);
            }
        }
        if (t != null) t.waitFinished();
    }
    
    /** cancel the scheduled request task */
    public void cancel() {
        RequestProcessor.Task t = task;
        if (t == null) return ;
        
        if (isRunning()) {
            t.waitFinished();
        } else {
            synchronized (this) {
                t.cancel();
                counter = 0;
                releaseResources();
            }
        }
    }
    
    public void run() {
        synchronized (this) {
            counter = 0;
        }
        
        try {
            performRequest();
        } catch (IOException ex) {
            Exceptions.attachLocalizedMessage(ex, fobj.toString());
            Logger.getLogger(ScheduledRequest.class.getName()).log(Level.WARNING, null, ex);
        }
    }
    
    /** perform the storing task, release the file lock and reset
     * ScheduledRequest */
    private void performRequest() throws IOException {
        boolean isAlreadyRunning = false;
        Task runningTask;
        
        synchronized (this) {
            isAlreadyRunning = running;
            runningTask = task;
            running = true;
        }
        
        if (isAlreadyRunning) {
            runningTask.waitFinished();
            return;
        }
        
        try {
            run.run();
        } finally {
            synchronized (this) {
                running = false;
                if (task == null || counter == 0) {
                    releaseResources();
                }
            }
        }
    }
    
    private void releaseResources() {
        if (lock != null) {
            lock.releaseLock();
            lock = null;
        }
        inst = null;
        task = null;
    }
    
}
