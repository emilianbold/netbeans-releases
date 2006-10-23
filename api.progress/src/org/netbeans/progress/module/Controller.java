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


package org.netbeans.progress.module;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.progress.spi.InternalHandle;
import org.netbeans.progress.spi.ProgressEvent;
import org.netbeans.progress.spi.ProgressUIWorker;
import org.netbeans.progress.spi.ProgressUIWorkerProvider;
import org.netbeans.progress.spi.ProgressUIWorkerWithModel;
import org.netbeans.progress.spi.TaskModel;
import org.openide.util.Lookup;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public /* final - because of tests */ class Controller implements Runnable, ActionListener {
    
    // non-private so that it can be accessed from the tests
    public static Controller defaultInstance;
    
    private ProgressUIWorker component;
    private TaskModel model;
    private List<ProgressEvent> eventQueue;
    private boolean dispatchRunning;
    protected Timer timer;
    private long timerStart = 0;
    private static final int TIMER_QUANTUM = 400;
    
    /**
     * initial delay for ading progress indication into the UI. if finishes earlier,
     * not shown at all, applies just to the status line (default) comtroller.
     */
    public static final int INITIAL_DELAY = 500;
    
    /** Creates a new instance of Controller */
    public Controller(ProgressUIWorker comp) {
        component = comp;
        model = new TaskModel();
        eventQueue = new ArrayList<ProgressEvent>();
        dispatchRunning = false;
        timer = new Timer(TIMER_QUANTUM, this);
        timer.setRepeats(false);
    }

    public static synchronized Controller getDefault() {
        if (defaultInstance == null) {
            ProgressUIWorkerProvider prov = Lookup.getDefault().lookup(ProgressUIWorkerProvider.class); 
            assert prov != null : "Cannot find implementation for ProgressUIWorkerProvider, please make sure you have the org.netbeans.modules.progress.ui module enabled.";
            ProgressUIWorkerWithModel component = prov.getDefaultWorker();
            defaultInstance = new Controller(component);
            component.setModel(defaultInstance.getModel());
        }
        return defaultInstance;
    }
    
    // to be called on the default instance only..
    Component getVisualComponent() {
        if (component instanceof Component) {
            return (Component)component;
        }
        return null;
    }
    
    
    public TaskModel getModel() {
        return model;
    }
    
    public void start(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_START, isWatched(handle));
        if (this == getDefault() && handle.getInitialDelay() > 100) {
            // default controller
            postEvent(event, true);
        } else {
            runImmediately(Collections.singleton(event));
        }
    }
    
    public void finish(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_FINISH, isWatched(handle));
        postEvent(event);
    }
    
    public void toIndeterminate(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_SWITCH, isWatched(handle));
        postEvent(event);
    }
    
    public void toSilent(InternalHandle handle, String message) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_SILENT, isWatched(handle), message);
        postEvent(event);
    }
    
    
    public void toDeterminate(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_SWITCH, isWatched(handle));
        postEvent(event);
    }    
    
    public void progress(InternalHandle handle, String msg, 
                  int units, int percentage, long estimate) {
        ProgressEvent event = new ProgressEvent(handle, msg, units, percentage, estimate, isWatched(handle));
        postEvent(event);
    }
    
    public ProgressEvent snapshot(InternalHandle handle, String msg, 
                  int units, int percentage, long estimate) {
        if (handle.isInSleepMode()) {
            return new ProgressEvent(handle, ProgressEvent.TYPE_SILENT, isWatched(handle), msg);
        }
        return new ProgressEvent(handle, msg, units, percentage, estimate, isWatched(handle));
    }
    
    
    public void explicitSelection(InternalHandle handle) {
        InternalHandle old = model.getExplicitSelection();
        model.explicitlySelect(handle);
        Collection<ProgressEvent> evnts = new ArrayList<ProgressEvent>();
        evnts.add(handle.requestStateSnapshot());
        if (old != null && old != handle) {
            // refresh the old one, results in un-bodling the text.
            evnts.add(old.requestStateSnapshot());
        }
        runImmediately(evnts);
    }
    
    public void displayNameChange(InternalHandle handle, int units, int percentage, long estimate, String display) {
        Collection<ProgressEvent> evnts = new ArrayList<ProgressEvent>();
        evnts.add(new ProgressEvent(handle, null, units, percentage, estimate, isWatched(handle), display));
        runImmediately(evnts);
    }
    
    private boolean isWatched(InternalHandle hndl) {
        return model.getExplicitSelection() == hndl;
    }
    
    /**
     * 
     */ 
    void runImmediately(Collection<ProgressEvent> events) {
        synchronized (this) {
            // need to add to queue immediately in the current thread
            eventQueue.addAll(events);
            dispatchRunning = true;
        }
        // trigger ui update as fast as possible.
        if (SwingUtilities.isEventDispatchThread()) {
           run();
        } else {
           SwingUtilities.invokeLater(this);
        }
        
    }
    
    void postEvent(final ProgressEvent event) {
        postEvent(event, false);
    }
    
    void postEvent(final ProgressEvent event, boolean shortenPeriod) {
        synchronized (this) {
            eventQueue.add(event);
            if (!dispatchRunning) {
                timerStart = System.currentTimeMillis();
                int delay = timer.getInitialDelay();
                // period of timer is longer than required by the handle -> shorten it.
                if (shortenPeriod && timer.getInitialDelay() > event.getSource().getInitialDelay()) {
                    delay = event.getSource().getInitialDelay();
                } 
                dispatchRunning = true;
                resetTimer(delay, true);
            } else if (shortenPeriod) {
                // time remaining is longer than required by the handle's initial delay.
                // restart with shorter time.
                if (System.currentTimeMillis() - timerStart > event.getSource().getInitialDelay()) {
                    resetTimer(event.getSource().getInitialDelay(), true);
                }
            }
        }
    }
    
    protected void resetTimer(int initialDelay, boolean restart) {
        timer.setInitialDelay(initialDelay);
        if (restart) {
            timer.restart();
        }
    }
     
    
    /**
     * can be run from awt only.
     */
    public void run() {
        HashMap<InternalHandle, ProgressEvent> map = new HashMap<InternalHandle, ProgressEvent>();
        boolean hasShortOne = false;
        long minDiff = TIMER_QUANTUM;
        
        InternalHandle oldSelected = model.getSelectedHandle();
        long stamp = System.currentTimeMillis();
        synchronized (this) {
            Iterator<ProgressEvent> it = eventQueue.iterator();
            Collection<InternalHandle> justStarted = new ArrayList<InternalHandle>();
            while (it.hasNext()) {
                ProgressEvent event = it.next();
                boolean isShort = (stamp - event.getSource().getTimeStampStarted()) < event.getSource().getInitialDelay();
                if (event.getType() == ProgressEvent.TYPE_START) {
                    if (event.getSource().isCustomPlaced() || !isShort) {
                        model.addHandle(event.getSource());
                    } else {
                        justStarted.add(event.getSource());
                    }
                }
                else if (event.getType() == ProgressEvent.TYPE_FINISH &&
                       (! justStarted.contains(event.getSource()))) 
                {
                    model.removeHandle(event.getSource());
                }
                ProgressEvent lastEvent = (ProgressEvent)map.get(event.getSource());
                if (lastEvent != null && event.getType() == ProgressEvent.TYPE_FINISH && 
                        justStarted.contains(event.getSource()) && isShort)
                {
                    // if task quits really fast, ignore..
                    // defined 'really fast' as being shorter than initial delay
                    map.remove(event.getSource());
                    justStarted.remove(event.getSource());
                } else {
                    if (lastEvent != null) {
                        // preserve last message
                        event.copyMessageFromEarlier(lastEvent);
                        // preserve the switched state
                        if (lastEvent.isSwitched()) {
                            event.markAsSwitched();
                        }
                    }
                    map.put(event.getSource(), event);
                }
                it.remove();
            }
            // now re-add the just started events into queue
            // if they don't last longer than the initial delay of the task.
            // applies just for status bar items
            Iterator<InternalHandle> startIt = justStarted.iterator();
            while (startIt.hasNext()) {
                InternalHandle hndl = startIt.next();
                long diff = stamp - hndl.getTimeStampStarted();
                if (diff >= hndl.getInitialDelay()) {
                    model.addHandle(hndl);
                } else {
                    eventQueue.add(new ProgressEvent(hndl, ProgressEvent.TYPE_START, isWatched(hndl)));
                    ProgressEvent evnt = (ProgressEvent)map.remove(hndl);
                    if (evnt.getType() != ProgressEvent.TYPE_START) {
                        eventQueue.add(evnt);
                    }
                    hasShortOne = true;
                    minDiff = Math.min(minDiff, hndl.getInitialDelay() - diff);
                }
            }
        }
        InternalHandle selected = model.getSelectedHandle();
        selected = selected == null ? oldSelected : selected;
        Iterator<ProgressEvent> it = map.values().iterator();
        while (it.hasNext()) {
            ProgressEvent event = it.next();
            if (selected == event.getSource()) {
                component.processSelectedProgressEvent(event);
            }
            component.processProgressEvent(event);
        }
        synchronized (this) {
            timer.stop();
            if (hasShortOne) {
                timerStart = System.currentTimeMillis();
                resetTimer((int)Math.max(100, minDiff), true);
            } else {
                dispatchRunning = false;
                resetTimer(TIMER_QUANTUM, false);
            }
        }
    }

    /**
     * used by Timer
     */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        run();
    }

    

}
