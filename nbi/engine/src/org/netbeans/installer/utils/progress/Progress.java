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
 *
 * $Id$
 */
package org.netbeans.installer.utils.progress;

import java.util.Vector;

/**
 *
 * @author ks152834
 */
public class Progress {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final int START    = 0;
    public static final int COMPLETE = 100;
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    protected String  title      = "";
    protected String  detail     = "";
    protected int     percentage = START;
    
    protected boolean canceled   = false;
    
    private ProgressListener synchronizer = null;
    private Progress         source       = null;
    
    private Vector<ProgressListener> listeners = new Vector<ProgressListener>();
    
    // constructors /////////////////////////////////////////////////////////////////
    public Progress() {
        // does nothing
    }
    
    public Progress(ProgressListener initialListener) {
        this();
        
        addProgressListener(initialListener);
    }
    
    // properties accessors/mutators ////////////////////////////////////////////////
    public String getTitle() {
        return title;
    }
    
    public void setTitle(final String title) {
        this.title = title;
        
        notifyListeners();
    }
    
    public String getDetail() {
        return detail;
    }
    
    public void setDetail(final String detail) {
        this.detail = detail;
        
        notifyListeners();
    }
    
    public int getPercentage() {
        return percentage;
    }
    
    public void setPercentage(final int percentage) {
        if ((percentage < START) || (percentage > COMPLETE)) {
            throw new IllegalArgumentException("The percentage should be between 0 and 100 inclusive");
        }
        
        this.percentage = percentage;
        
        notifyListeners();
    }
    
    public void addPercentage(final int addition) {
        int result = percentage + addition;
        
        if ((result < START) || (result> COMPLETE)) {
            throw new IllegalArgumentException("The percentage should be between 0 and 100 inclusive");
        }
        
        percentage = result;
        
        notifyListeners();
    }
    
    public void setCanceled(final boolean canceled) {
        this.canceled = canceled;
        
        if (source != null) {
            source.setCanceled(canceled);
        }
    }
    
    public boolean isCanceled() {
        return canceled;
    }
    
    // syncronization ///////////////////////////////////////////////////////////////
    public void synchronizeFrom(Progress progress) {
        if (source != null) {
            source.removeProgressListener(synchronizer);
        }
        
        synchronizer = new ProgressListener() {
            public void progressUpdated(Progress progress) {
                setTitle(progress.getTitle());
                setDetail(progress.getDetail());
                setPercentage(progress.getPercentage());
            }
        };
        
        source = progress;
        source.addProgressListener(synchronizer);
    }
    
    public void reverseSynchronizeFrom(Progress progress) {
        if (source != null) {
            source.removeProgressListener(synchronizer);
        }
        
        synchronizer = new ProgressListener() {
            public void progressUpdated(Progress progress) {
                setTitle(progress.getTitle());
                setDetail(progress.getDetail());
                setPercentage(Progress.COMPLETE - progress.getPercentage());
            }
        };
        
        source = progress;
        source.addProgressListener(synchronizer);
    }
    
    public void synchronizeTo(Progress progress) {
        progress.synchronizeFrom(this);
    }
    
    public void reverseSynchronizeTo(Progress progress) {
        progress.reverseSynchronizeFrom(this);
    }
    
    // listeners ////////////////////////////////////////////////////////////////////
    public void addProgressListener(ProgressListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public void removeProgressListener(ProgressListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    protected void notifyListeners() {
        ProgressListener[] clone = listeners.toArray(new ProgressListener[0]);
        
        for (ProgressListener listener: clone) {
            listener.progressUpdated(this);
        }
    }
}
