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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mercurial;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;

/**
 *
 * @author Tomas Stupka
 */
public abstract class HgProgressSupport implements Runnable, Cancellable {

    private Cancellable delegate; 
    private volatile boolean canceled;
    
    private ProgressHandle progressHandle = null;    
    private String displayName = ""; // NOI18N
    private String originalDisplayName = ""; // NOI18N
    private String repositoryRoot;
    private RequestProcessor.Task task;
    
    public RequestProcessor.Task start(RequestProcessor rp, String repositoryRoot, String displayName) {
        setDisplayName(displayName);
        this.repositoryRoot = repositoryRoot;
        startProgress();
        setProgressQueued();
        task = rp.post(this);   
        task.addTaskListener(new TaskListener() {
            public void taskFinished(org.openide.util.Task task) {
                delegate = null;
            }
        });
        return task;
    }

    public void setRepositoryRoot(String repositoryRoot) {
        this.repositoryRoot = repositoryRoot;
    }

    public void run() {        
        setProgress();
        performIntern();
    }

    protected void performIntern() {
        try {
            Mercurial.LOG.log(Level.FINE, "Start - ", displayName); // NOI18N
            if(!canceled) {
                perform();
            }
            Mercurial.LOG.log(Level.FINE, "End - ", displayName); // NOI18N
        } finally {            
            finnishProgress();
        }
    }

    protected abstract void perform();

    public synchronized boolean isCanceled() {
        return canceled;
    }

    public synchronized boolean cancel() {
        if (canceled) {
            return false;
        }        
        if(task != null) {
            task.cancel();
        }
        if(delegate != null) {
            delegate.cancel();
        }
        getProgressHandle().finish();
        canceled = true;
        return true;
    }

    void setCancellableDelegate(Cancellable cancellable) {
        this.delegate = cancellable;
    }

    public void setDisplayName(String displayName) {
        if(originalDisplayName.equals("")) { // NOI18N
            originalDisplayName = displayName;
        }
        this.displayName = displayName;
        setProgress();
    }

    private void setProgressQueued() {
        if(progressHandle!=null) {
            progressHandle.progress(NbBundle.getMessage(HgProgressSupport.class, "LBL_Queued", displayName)); // NOI18N
        }
    }
 
    private void setProgress() {
        if(progressHandle!=null) {
            progressHandle.progress(displayName);
        }
    }

    protected String getDisplayName() {
        return displayName;
    }

    protected ProgressHandle getProgressHandle() {
        if(progressHandle==null) {
            progressHandle = ProgressHandleFactory.createHandle(displayName, this);
        }
        return progressHandle;
    }

    protected void startProgress() {
        getProgressHandle().start();
    }

    protected void finnishProgress() {
        getProgressHandle().finish();
    }
    
    
    public void annotate(HgException ex) {        
        ExceptionHandler eh = new ExceptionHandler(ex);
        if(isCanceled()) {
            eh.notifyException(false);
        } else {
            eh.notifyException();    
        }
    }
}
