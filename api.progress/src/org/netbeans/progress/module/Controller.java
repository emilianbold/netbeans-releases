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


package org.netbeans.progress.module;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.progress.module.ui.StatusLineComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 */
final class Controller implements Runnable, ActionListener {
    
    private static Controller defaultInstance;
    private static Container defaultContTemp;
    
    private NewInterface component;
    private TaskModel model;
    private List eventQueue;
    private boolean dispatchRunning;
    private Timer timer;
    /** Creates a new instance of Controller */
    public Controller(NewInterface comp) {
        component = comp;
        model = new TaskModel();
        eventQueue = new ArrayList();
        dispatchRunning = false;
        timer = new Timer(400, this);
        timer.setRepeats(false);
    }

    public static synchronized Controller getDefault() {
        if (defaultInstance == null) {
            StatusLineComponent component = new StatusLineComponent();
            defaultInstance = new Controller(component);
            component.setModel(defaultInstance.getModel());
        }
        return defaultInstance;
    }
    
    Component getVisualComponent() {
        if (component instanceof Component) {
            return (Component)component;
        }
        return null;
    }
    
    /**
     * temporary way of finding status line component.
     */
    private static Container findStatusComponent() {
        Frame frm = WindowManager.getDefault().getMainWindow();
        return find(frm.getComponents());
        
    }
    
    /**
     * temporary way of finding status line component.
     */
    private static Container find(Component[] comps) {
        if (comps == null || comps.length == 0) {
            return null;
        }
        for (int i = 0; i < comps.length; i++) {
            if (! (comps[i] instanceof Container)) {
                continue;
            }
            if ("statusLine".equals(comps[i].getName())) {
                return (Container)comps[i];
            }
            Container child =find(((Container)comps[i]).getComponents());
            if (child != null) {
                return child;
            }
        }
        return null;
    }
    
    TaskModel getModel() {
        return model;
    }
    
    void start(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_START, isWatched(handle));
        postEvent(event);
    }
    
    void finish(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_FINISH, isWatched(handle));
        postEvent(event);
    }
    
    void progress(InternalHandle handle, String msg, 
                  int units, int percentage, long estimate) {
        ProgressEvent event = new ProgressEvent(handle, msg, units, percentage, estimate, isWatched(handle));
        postEvent(event);
    }
    
    ProgressEvent snapshot(InternalHandle handle, String msg, 
                  int units, int percentage, long estimate) {
        return new ProgressEvent(handle, msg, units, percentage, estimate, isWatched(handle));
    }
    
    
    void explicitSelection(InternalHandle handle, int units, int percentage, long estimate) {
        InternalHandle old = model.getExplicitSelection();
        model.explicitlySelect(handle);
        Collection evnts = new ArrayList();
        evnts.add(new ProgressEvent(handle, null, units, percentage, estimate, isWatched(handle)));
        if (old != null && old != handle) {
            // refresh the old one, results in un-bodling the text.
            evnts.add(old.requestStateSnapshot());
        }
        runImmediately(evnts);
    }
    
    private boolean isWatched(InternalHandle hndl) {
        return model.getExplicitSelection() == hndl;
    }
    
    void runImmediately(Collection events) {
        synchronized (this) {
            eventQueue.addAll(events);
            dispatchRunning = true;
            run();
        }
    }
    
    void postEvent(final ProgressEvent event) {
        synchronized (this) {
            eventQueue.add(event);
            if (!dispatchRunning) {
                timer.start();
                dispatchRunning = true;
            }
        }
    }
     
    
    /**
     * can be run from awt only.
     */
    public void run() {
        assert SwingUtilities.isEventDispatchThread();
        HashMap map = new HashMap();
        InternalHandle oldSelected = model.getSelectedHandle();
        synchronized (this) {
            Iterator it = eventQueue.iterator();
            Collection justStarted = new ArrayList();
            while (it.hasNext()) {
                ProgressEvent event = (ProgressEvent)it.next();
                if (event.getType() == ProgressEvent.TYPE_START) {
                    justStarted.add(event.getSource());
                    model.addHandle(event.getSource());
                }
                if (event.getType() == ProgressEvent.TYPE_FINISH) {
                    model.removeHandle(event.getSource());
                }
                ProgressEvent lastEvent = (ProgressEvent)map.get(event.getSource());
                if (lastEvent != null && lastEvent.getType() == ProgressEvent.TYPE_FINISH && 
                        justStarted.contains(event.getSource())) {
                    // if task quits really fast, ignore..
                    map.remove(event.getSource());
                } else {
                    if (lastEvent != null) {
                        // preserve last message
                        event.copyMessageFromEarlier(lastEvent);
                    }
                    map.put(event.getSource(), event);
                }
                it.remove();
            }
        }
        InternalHandle selected = model.getSelectedHandle();
        selected = selected == null ? oldSelected : selected;
        Iterator it = map.values().iterator();
        while (it.hasNext()) {
            ProgressEvent event = (ProgressEvent)it.next();
            if (selected == event.getSource()) {
                component.processSelectedProgressEvent(event);
            }
            component.processProgressEvent(event);
        }
        timer.stop();
        dispatchRunning = false;
    }

    /**
     * used by Timer
     */
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        run();
    }
    

}
