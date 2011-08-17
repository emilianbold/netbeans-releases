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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.progress.module.TrivialProgressUIWorkerProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 * @since org.netbeans.api.progress/1 1.18
 */
public /* final - because of tests */ class Controller {
    
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
        timer = new Timer(TIMER_QUANTUM, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runNow();
            }
        });
        timer.setRepeats(false);
    }

    public static synchronized Controller getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new Controller(null);
        }
        return defaultInstance;
    }
    
    // to be called on the default instance only..
    public Component getVisualComponent() {
        if (component == null) {
            getProgressUIWorker();
        }
        if (component instanceof Component) {
            return (Component)component;
        }
        return null;
    }
    
    ProgressUIWorker getProgressUIWorker()
    {
        if (component == null)
        {
            ProgressUIWorkerProvider prov = Lookup.getDefault().lookup(ProgressUIWorkerProvider.class);
            if (prov == null) {
                Logger.getLogger(Controller.class.getName()).log(Level.CONFIG, "Using fallback trivial progress implementation");
                prov = new TrivialProgressUIWorkerProvider();
            }
            ProgressUIWorkerWithModel prgUIWorker = prov.getDefaultWorker();
            prgUIWorker.setModel(defaultInstance.getModel());
            component = prgUIWorker;
        }
        return component;
    }

    public TaskModel getModel() {
        return model;
    }
    
    void start(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_START, isWatched(handle));
        if (this == getDefault() && handle.getInitialDelay() > 100) {
            // default controller
            postEvent(event, true);
        } else {
            runImmediately(Collections.singleton(event));
        }
    }
    
    void finish(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_FINISH, isWatched(handle));
        postEvent(event);
    }
    
    void toIndeterminate(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_SWITCH, isWatched(handle));
        model.updateSelection();
        postEvent(event);
    }
    
    void toSilent(InternalHandle handle, String message) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_SILENT, isWatched(handle), message);
        model.updateSelection();
        postEvent(event);
    }
    
    
    void toDeterminate(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_SWITCH, isWatched(handle));
        model.updateSelection();
        postEvent(event);
    }    
    
    void progress(InternalHandle handle, String msg, 
                  int units, double percentage, long estimate) {
        ProgressEvent event = new ProgressEvent(handle, msg, units, percentage, estimate, isWatched(handle));
        postEvent(event);
    }
    
    ProgressEvent snapshot(InternalHandle handle, String msg, 
                  int units, double percentage, long estimate) {
        if (handle.isInSleepMode()) {
            return new ProgressEvent(handle, ProgressEvent.TYPE_SILENT, isWatched(handle), msg);
        }
        return new ProgressEvent(handle, msg, units, percentage, estimate, isWatched(handle));
    }
    
    
    void explicitSelection(InternalHandle handle) {
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
    
    void displayNameChange(InternalHandle handle, int units, double percentage, long estimate, String display) {
        Collection<ProgressEvent> evnts = new ArrayList<ProgressEvent>();
        evnts.add(new ProgressEvent(handle, null, units, percentage, estimate, isWatched(handle), display));
        runImmediately(evnts);
    }
    
    private boolean isWatched(InternalHandle hndl) {
        return model.getExplicitSelection() == hndl;
    }
    
    void runImmediately(Collection<ProgressEvent> events) {
        synchronized (this) {
            // need to add to queue immediately in the current thread
            eventQueue.addAll(events);
            dispatchRunning = true;
        }
        // trigger ui update as fast as possible.
        if (SwingUtilities.isEventDispatchThread()) {
           runNow();
        } else {
           SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    runNow();
                }
            });
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
     
    
    public void runNow() {
        // not true in tests: assert EventQueue.isDispatchThread();
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
                ProgressEvent lastEvent = map.get(event.getSource());
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
                    ProgressEvent evnt = map.remove(hndl);
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
        if (component == null) {
            getProgressUIWorker();
        }
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
                resetTimer(TIMER_QUANTUM, !eventQueue.isEmpty());
            }
        }
    }

}
