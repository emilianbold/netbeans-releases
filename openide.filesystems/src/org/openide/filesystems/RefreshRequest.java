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

package org.openide.filesystems;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;

/** Request for parsing of an filesystem. Can be stopped.
*
* @author Jaroslav Tulach
*/
final class RefreshRequest extends Object implements Runnable {
    /** how much folders refresh at one request */
    private static final int REFRESH_COUNT = 30;
    private static RequestProcessor REFRESHER = new RequestProcessor("FS refresher"); // NOI18N

    /** fs to work on */
    private Reference<AbstractFileSystem> system;

    /** enumeration of folders to process */
    private Enumeration<AbstractFolder> en;

    /** how often invoke itself */
    private int refreshTime;

    /** task to call us */
    private RequestProcessor.Task task;

    /** Constructor
    * @param fs file system to refresh
    * @param ms refresh time
    */
    public RefreshRequest(AbstractFileSystem fs, int ms) {
        system = new WeakReference<AbstractFileSystem>(fs);
        refreshTime = ms;
        task = REFRESHER.post(this, ms, Thread.MIN_PRIORITY);
    }

    /** Getter for the time.
    */
    public int getRefreshTime() {
        return refreshTime;
    }

    /** Stops the task.
    */
    public synchronized void stop() {
        refreshTime = 0;

        if (task == null) {
            // null task means that the request processor is running =>
            // wait for end of task execution
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
    }

    /** Refreshes the system.
    */
    public void run() {
        // this code is executed only in RequestProcessor thread
        int ms;
        RequestProcessor.Task t;

        synchronized (this) {
            // the synchronization is here to be sure
            // that 
            ms = refreshTime;

            if (ms <= 0) {
                // finish silently if already stopped
                return;
            }

            t = task;

            // by setting task to null we indicate that we are currently processing
            // files and that any stop should wait till the processing is over
            task = null;
        }

        try {
            doLoop(ms);
        } finally {
            synchronized (this) {
                // reseting task variable back to indicate that 
                // the processing is over
                task = t;

                notifyAll();
            }

            // plan the task for next execution
            if ((system != null) && (system.get() != null)) {
                t.schedule(ms);
            } else {
                refreshTime = 0;
            }
        }
    }

    private void doLoop(int ms) {
        AbstractFileSystem system = this.system.get();

        if (system == null) {
            // end for ever the fs does not exist no more
            return;
        }

        if ((en == null) || !en.hasMoreElements()) {
            // start again from root
            en = NbCollections.checkedEnumerationByFilter(existingFolders(system), AbstractFolder.class, true);
        }

        for (int i = 0; (i < REFRESH_COUNT) && en.hasMoreElements(); i++) {
            AbstractFolder fo = en.nextElement();

            if ((fo != null) && (!fo.isFolder() || fo.isInitialized())) {
                fo.refresh();
            }

            if (refreshTime <= 0) {
                // after each refresh check the current value of refreshTime
                // again and if it goes to zero exit as fast a you can
                return;
            }
        }

        // clear the queue
        if (!en.hasMoreElements()) {
            en = null;
        }
    }

    /** Existing folders for abstract file objects.
    */
    private static Enumeration<? extends FileObject> existingFolders(AbstractFileSystem fs) {
        return fs.existingFileObjects(fs.getAbstractRoot());
    }

    /**
     * Overridden for debugging/logging purposes.
     */
    public String toString() {
        AbstractFileSystem fs = system.get();

        return "RefreshRequest for " + // NOI18N
        ((fs == null) ? "gone FS" : fs); // NOI18N
    }
}
