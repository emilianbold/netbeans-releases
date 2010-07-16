/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.modules.progress.spi;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.progress.module.*;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 * Instances provided by the ProgressHandleFactory allow the users of the API to
 * notify the progress bar UI about changes in the state of the running task.
 * @author Milos Kleint (mkleint@netbeans.org)
 * @since org.netbeans.api.progress/1 1.18
 */
public final class InternalHandle {

    private static final Logger LOG = Logger.getLogger(InternalHandle.class.getName());
    
    private String displayName;
    private boolean customPlaced1 = false;
    private boolean customPlaced2 = false;
    private boolean customPlaced3 = false;
    private int state;
    private int totalUnits;
    private int currentUnit;
    private long initialEstimate;
    private long timeStarted;
    private long timeLastProgress;
    private long timeSleepy = 0;
    private String lastMessage;
    private final Cancellable cancelable;
    private final Action viewAction;
    private final boolean userInitiated;
    private int initialDelay = Controller.INITIAL_DELAY;
    private Controller controller;
    private ExtractedProgressUIWorker component;
    
    public static final int STATE_INITIALIZED = 0;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_FINISHED = 2;
    public static final int STATE_REQUEST_STOP = 3;

    public static final int NO_INCREASE = -2;
    

    
    /** Creates a new instance of ProgressHandle */
    public InternalHandle(String displayName, 
                   Cancellable cancel,
                   boolean userInitiated,
                   Action view) {
        this.displayName = displayName;
        this.userInitiated = userInitiated;
        state = STATE_INITIALIZED;
        totalUnits = 0;
        lastMessage = null;
        cancelable = cancel;
        viewAction = view;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * XXX - called from UI, threading
     */
    public synchronized int getState() {
        return state;
    }
    
    public boolean isAllowCancel() {
        return cancelable != null && !isCustomPlaced();
    }
    
    public boolean isAllowView() {
        return viewAction != null && !isCustomPlaced();
    }
    
    
    public boolean isCustomPlaced() {
        return component != null;
    }
    
    public boolean isUserInitialized() {
        return userInitiated;
    }
    
    private int getCurrentUnit() {
        return currentUnit;
    }
    
    public int getTotalUnits() {
        return totalUnits;
    }
    
    public void setInitialDelay(int millis) {
        if (state != STATE_INITIALIZED) {
            LOG.warning("Setting ProgressHandle.setInitialDelay() after the task is started has no effect"); //NOI18N
            return;
        }
        initialDelay = millis;
    }
    
    public int getInitialDelay() {
        return initialDelay;
    }
    
    public synchronized void toSilent(String message) {
        if (state != STATE_RUNNING && state != STATE_REQUEST_STOP) {
            assert false : "cannot switch to silent mode when not running";
        }
        timeLastProgress = System.currentTimeMillis();
        timeSleepy = timeLastProgress;
        if (message != null) {
            lastMessage = message;
        }
        controller.toSilent(this, message);
    }
    
    public boolean isInSleepMode() {
        return timeSleepy == timeLastProgress;
    }
    
    public synchronized void toIndeterminate() {
        if (state != STATE_RUNNING && state != STATE_REQUEST_STOP) {
            assert false : "cannot switch to indeterminate mode when not running: " + state;
        }
        totalUnits = 0;
        currentUnit = 0;
        initialEstimate = -1;
        timeLastProgress = System.currentTimeMillis();
        controller.toIndeterminate(this);
    }
    
    public synchronized void toDeterminate(int workunits, long estimate) {
        if (state != STATE_RUNNING && state != STATE_REQUEST_STOP) {
            assert false : "cannot switch to determinate mode when not running";
        }
        if (workunits < 0) {
            throw new IllegalArgumentException("number of workunits cannot be negative");
        }        
        totalUnits = workunits;
        currentUnit = 0;
        initialEstimate = estimate;
        timeLastProgress = System.currentTimeMillis();
        controller.toDeterminate(this);
    }
    
    /**
     * start the progress indication for a task with known number of steps and known
     * time estimate for completing the task.
     * 
     * @param message 
     * @param workunits 
     * @param estimate estimated time to process the task in seconds
     */
    public synchronized void start(String message, int workunits, long estimate) {
        if (state != STATE_INITIALIZED) {
            throw new IllegalStateException("Cannot call start twice on a handle");
        }
        if (workunits < 0) {
            throw new IllegalArgumentException("number of workunits cannot be negative");
        }
        totalUnits = workunits;
        currentUnit = 0;
        if (message != null) {
            lastMessage = message;
        }
        if (controller == null) {
            controller = Controller.getDefault();
        }
        state = STATE_RUNNING;
        initialEstimate = estimate;
        timeStarted = System.currentTimeMillis();
        timeLastProgress = timeStarted;

        
        controller.start(this);
    }

    /**
     * finish the task, remove the task's component from the progress bar UI.
     */
    public synchronized void finish() {
        if (state == STATE_INITIALIZED) {
            throw new IllegalStateException("Cannot finish not a started task");
        }
        if (state == STATE_FINISHED) {
            return;
        }
        state = STATE_FINISHED;
        currentUnit = totalUnits;
        
        controller.finish(this);
    }
    
    
    /**
     * 
     * @param message 
     * @param workunit 
     */
    public synchronized void progress(String message, int workunit) {
        if (state != STATE_RUNNING && state != STATE_REQUEST_STOP) {
            return;
        }

        if (workunit != NO_INCREASE) {
            if (workunit < currentUnit) {
                throw new IllegalArgumentException("Cannot decrease processed workunit count (" + workunit + ") to lower value than before (" + currentUnit + ")");
            }
            if (workunit > totalUnits) {
                // seems to be the by far most frequently abused contract. Record it to log file and safely handle the case
                //#96921 - WARNING -> INFO to prevent users reporting the problem automatically.
                LOG.log(Level.INFO,
                    "Cannot process more work than scheduled. " +
                    "Progress handle with name \"" + getDisplayName() + "\" has requested progress to workunit no." + workunit + 
                    " but the total number of workunits is " + totalUnits + ". That means the progress bar UI will not display real progress and will stay at 100%.",
                    new IllegalArgumentException()
                );
                workunit = totalUnits;
            }
            currentUnit = workunit;
        }
        if (message != null) {
            lastMessage = message;
        }
        timeLastProgress = System.currentTimeMillis();
        
        controller.progress(this, message, currentUnit, 
                            totalUnits > 0 ? getPercentageDone() : -1, 
                            (initialEstimate == -1 ? -1 : calculateFinishEstimate()));
    }
    
    
  // XXX - called from UI, threading

    public void requestCancel() {
        if (!isAllowCancel()) {
            return;
        }
        synchronized (this) {
            state = STATE_REQUEST_STOP;
        }
        // do not call in synchronized block because it can take a long time to process, 
        ///  and it could slow down UI.
        //TODO - call in some other thread, not AWT? what is the cancel() contract?
        cancelable.cancel();
        synchronized (this) {
            requestStateSnapshot();
        }
    }
    
   ///XXX - called from UI, threading
    public void requestView() {
        if (!isAllowView()) {
            return;
        }
        viewAction.actionPerformed(new ActionEvent(viewAction, ActionEvent.ACTION_PERFORMED, "performView"));
    }
    
   // XXX - called from UI, threading
    public synchronized void requestExplicitSelection() {
        if (!isInSleepMode()) {
            timeLastProgress = System.currentTimeMillis();
        }
        controller.explicitSelection(this);
    }
    
    public synchronized void requestDisplayNameChange(String newDisplayName) {
        displayName = newDisplayName;
        if (state == STATE_INITIALIZED) {
            return;
        }
        timeLastProgress = System.currentTimeMillis();
        controller.displayNameChange(this, currentUnit, 
                            totalUnits > 0 ? getPercentageDone() : -1, 
                            (initialEstimate == -1 ? -1 : calculateFinishEstimate()), newDisplayName);
    }
    
// XXX - called from UI, threading 
    public synchronized ProgressEvent requestStateSnapshot() {
        if (!isInSleepMode()) {
            timeLastProgress = System.currentTimeMillis();
        }
        return controller.snapshot(this, lastMessage, currentUnit, 
                            totalUnits > 0 ? getPercentageDone() : -1, 
                            (initialEstimate == -1 ? -1 : calculateFinishEstimate()));
    }
    
    private void createExtractedWorker() {
        if (component == null) {
            ProgressUIWorkerProvider prov = Lookup.getDefault().lookup(ProgressUIWorkerProvider.class);
            if (prov == null) {
                LOG.log(Level.CONFIG, "Using fallback trivial progress implementation");
                prov = new TrivialProgressUIWorkerProvider();
            }
            component = prov.getExtractedComponentWorker();
            controller = new Controller(component);
        }
    }
    /**
     * have the component in custom location, don't include in the status bar.
     */
    public synchronized JComponent extractComponent() {
        if (customPlaced1) {
            throw new IllegalStateException("Cannot retrieve progress component multiple times");
        }
        if (state != STATE_INITIALIZED) {
            throw new IllegalStateException("You can request custom placement of progress component only before starting the task");
        }
        customPlaced1 = true;
        createExtractedWorker();
        return component.getProgressComponent();
    }
    
    public synchronized JLabel extractDetailLabel() {
        if (customPlaced2) {
            throw new IllegalStateException("Cannot retrieve progress detail label component multiple times");
        }
        if (state != STATE_INITIALIZED) {
            throw new IllegalStateException("You can request custom placement of progress component only before starting the task");
        }
        customPlaced2 = true;
        createExtractedWorker();
        return component.getDetailLabelComponent();
    }

    public synchronized JLabel extractMainLabel() {
        if (customPlaced3) {
            throw new IllegalStateException("Cannot retrieve progress main label component multiple times");
        }
        if (state != STATE_INITIALIZED) {
            throw new IllegalStateException("You can request custom placement of progress component only before starting the task");
        }
        customPlaced3 = true;
        createExtractedWorker();
        return component.getMainLabelComponent();
    }

    long calculateFinishEstimate() {
        
        // we are interested in seconds only
        double durationSoFar = ((double)(System.currentTimeMillis() - timeStarted)) / 1000;
        if (initialEstimate == -1) {
            // we don't have an initial estimate, calculate by real-life data only
            return (long)(durationSoFar *  (totalUnits - currentUnit) / totalUnits);
        } else {
            // in the begining give the initial estimate more weight than in the end.
            // should give us more smooth estimates 
            long remainingUnits = (totalUnits - currentUnit);
            double remainingPortion = (double)remainingUnits / (double)totalUnits;
            double currentEstimate = durationSoFar / (double)currentUnit * totalUnits;
            long retValue = (long)(((initialEstimate * remainingUnits * remainingPortion) 
                         + (currentEstimate * remainingUnits * (1 - remainingPortion)))
                       / totalUnits); 
            return retValue;
        }
    }
    /**
     *public because of tests.
     */
    public double getPercentageDone() {
        return ((double)currentUnit * 100 / (double)totalUnits); 
    }

    public long getTimeStampStarted() {
        return timeStarted;
    }



}
