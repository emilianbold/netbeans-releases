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

package org.netbeans.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.startup.StartLog;
import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.cookies.InstanceCookie;
import org.openide.util.RequestProcessor;

/**
 * This class controls "warm-up" initialization after IDE startup (some time
 * after main window is shown). It scans WarmUp folder for individual tasks
 * to be performed. The tasks should be instance objects implementing Runnable.
 *
 * The tasks may be provided by modules via xml layer.
 *
 * @author Tomas Pavek
 */

class WarmUpSupport implements Runnable {

    static final String WARMUP_FOLDER = "WarmUp"; // NOI18N
    static final int WARMUP_DELAY = 1500; // 1.5 sec after main window is shown
    
    static boolean finished = false;    // usefull for testability

    private Logger err = Logger.getLogger("org.netbeans.core.WarmUpSupport");

    static void warmUp() {
        RequestProcessor.getDefault().post(new WarmUpSupport(), WARMUP_DELAY);
    }

    // -------

    public void run() {
        boolean willLog = err.isLoggable(Level.FINE) || StartLog.willLog();
        if (willLog){
            err.fine("Warmup starting..."); // NOI18N
            StartLog.logStart("Warmup"); // NOI18N
        }

        FileObject fo = Repository.getDefault().getDefaultFileSystem()
                                                 .findResource(WARMUP_FOLDER);
        DataObject[] warmObjects =
            fo != null ? DataFolder.findFolder(fo).getChildren() : new DataObject[0];

        if (warmObjects.length == 0) {
            if (willLog) {
                err.fine("no warmp up task"); // NOI18N
            }
        }
        else {
            for (int i = 0; i < warmObjects.length; i++) {
                try {
                    InstanceCookie ic = (InstanceCookie) warmObjects[i].getCookie(InstanceCookie.class);

                    if (willLog) {
                        StartLog.logProgress("Warmup running " +
                                             ic.instanceName());
                    }
                    Object warmer = ic.instanceCreate();

                    if (warmer instanceof Runnable) {
                        ((Runnable) warmer).run();
                    }
                }
                catch (Exception ex) {
                    Logger.global.log(Level.WARNING, null, ex);
                }
            }
        }
        if (willLog){
            err.fine("Warmup done."); // NOI18N
            StartLog.logEnd("Warmup"); // NOI18N
        }
        
        finished = true;
    }
}
