/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.client;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.subversion.Diagnostics;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public abstract class SvnProgressSupport implements Runnable, Cancellable {

    private Cancellable delegate; 
    private boolean canceled;
    private Thread interruptibleThread;

    private final RequestProcessor rp;
    private ProgressHandle progressHandle = null;    
    private String displayName = "";
    
    public SvnProgressSupport(RequestProcessor rp, Cancellable cancellable) {
        this.rp = rp;
        this.delegate = cancellable;
    }

    public void start(String displayName) {
        this.displayName = displayName;
        rp.post(this);        
    }

    protected void setDisplayName(String displayName) {
        this.displayName = displayName;
        if(progressHandle!=null) {
            progressHandle.progress(displayName);
        }
    }
    
    public void run() {
        progressHandle = ProgressHandleFactory.createHandle(displayName, this);
        progressHandle.start();         
        try {
            interruptibleThread = Thread.currentThread();
            Diagnostics.println("Start - " + displayName);
            perform();
            Diagnostics.println("End - " + displayName);
        } finally {
            progressHandle.finish();            
        }        
    }

    public abstract void perform();

    public synchronized boolean isCanceled() {
        return canceled;
    }

    public synchronized boolean cancel() {
        if (canceled) {
            return false;
        }        
        if(delegate != null) {
            delegate.cancel();
        }
        if (interruptibleThread != null) {
            interruptibleThread.interrupt();  
        }
        canceled = true;
        return true;
    }

}
