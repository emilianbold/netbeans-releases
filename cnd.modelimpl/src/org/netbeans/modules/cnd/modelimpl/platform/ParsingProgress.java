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

package org.netbeans.modules.cnd.modelimpl.platform;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.openide.util.NbBundle;


/**
 * provides progress bar in status bar
 */
final class ParsingProgress {
    
    private ProgressHandle handle;
    private int curWorkedUnits = 0;
    private int maxWorkUnits = 0; // for testing only
    private boolean started = false;
    
    /**  
     * Delay amount of miliseconds 
     * that shall pass before the progress appears in status bar
     */
    private static final int INITIAL_DELAY = 1000; // ms
    
    /**
     * Constructs progress information for native project
     */
    public ParsingProgress(NativeProject project) {
        String msg=NbBundle.getMessage(ModelSupport.class, "MSG_ParsingProgress", project.getProjectDisplayName());
        handle = ProgressHandleFactory.createHandle(msg);
    }
    
    /**
     * Constructs progress information for project
     */
    public ParsingProgress(CsmProject project) {
        String msg=NbBundle.getMessage(ModelSupport.class, "MSG_ParsingProgress", project.getName());
        handle = ProgressHandleFactory.createHandle(msg);
    }
    
    /**
     * Start the progress indication for indeterminate task.
     * it will be visualized by a progress bar in indeterminate mode.
     */
    public void start() {
        synchronized (handle) {
            started = true;
            handle.setInitialDelay(INITIAL_DELAY);
            handle.start();
        }
    }
    
    /**
     * finish the task, remove the task's component from the progress bar UI.
     */        
    public void finish() {
        synchronized (handle) {
            if( started ) {
                handle.finish();
            }
        }
    }

    /**
     * inform about starting handling next file item
     */
    public void nextCsmFile(CsmFile file) {
        synchronized (handle) {
            if( ! started ) {
                return;
            }
            if( curWorkedUnits >= maxWorkUnits ) {
                return;
            }
            try {
                handle.progress(file.getName(), curWorkedUnits++);
                //assert(curWorkedUnits <= maxWorkUnits);
            } catch (NullPointerException ex) {
                // very strange... but do not interrupt process
                //assert(false);
                ex.printStackTrace(System.err);
            }
        }
    }

    /**
     * Currently indeterminate task can be switched to show percentage completed.
     * A common usecase is to calculate the amount of work in the beginning showing
     * in indeterminate mode and later switch to the progress with known steps
     */
    public void switchToDeterminate(int maxWorkUnits) {
        synchronized (handle) {
            if( ! started ) {
                return;
            }
            this.maxWorkUnits = maxWorkUnits;
            handle.switchToDeterminate(maxWorkUnits);
        }
    }
}   
