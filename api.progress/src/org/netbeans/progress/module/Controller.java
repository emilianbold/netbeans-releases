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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.progress.module.ui.StatusLineComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 */
final class Controller implements Runnable {
    
    private static Controller defaultInstance;
    private static Container defaultContTemp;
    
    private NewInterface component;
    private TaskModel model;
    private List eventQueue;
    private boolean dispatchRunning;
    /** Creates a new instance of Controller */
    public Controller(NewInterface comp) {
        component = comp;
        model = new TaskModel();
        eventQueue = new ArrayList();
        dispatchRunning = false;
    }

    public static synchronized Controller getDefault() {
        if (defaultInstance == null) {
            StatusLineComponent component = new StatusLineComponent();
            //TEMP figure better way of plugging into status line.
            if (SwingUtilities.isEventDispatchThread()) {
                defaultContTemp = findStatusComponent();
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            defaultContTemp = findStatusComponent();
                        }
                    });
                } catch (Exception exc) {
                    
                }
            }
            if (defaultContTemp != null) {
                defaultContTemp.add(component, BorderLayout.WEST);
//                defaultContTemp.invalidate();
//                defaultContTemp.validate();
//                defaultContTemp.repaint();
            } else {
                // this should happen just when doing tests.
//                throw new IllegalStateException();
                Thread.dumpStack();
            }
            //TEMP -end
            defaultInstance = new Controller(component);
            component.setModel(defaultInstance.getModel());
            
        }
        return defaultInstance;
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
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_START);
        postEvent(event);
    }
    
    void finish(InternalHandle handle) {
        ProgressEvent event = new ProgressEvent(handle, ProgressEvent.TYPE_FINISH);
        postEvent(event);
    }
    
    void progress(InternalHandle handle, String msg, int units, int percentage, long estimate) {
        ProgressEvent event = new ProgressEvent(handle, msg, units, percentage, estimate);
        postEvent(event);
    }
    
    void postEvent(final ProgressEvent event) {
        synchronized (this) {
            eventQueue.add(event);
            if (!dispatchRunning) {
                SwingUtilities.invokeLater(this);
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
                if (lastEvent == null || lastEvent.getType() != ProgressEvent.TYPE_FINISH) {
                    map.put(event.getSource(), event);
                } else {
                    // finish now..
                    if (justStarted.contains(event.getSource())) {
                        // if task quits really fast, ignore..
                        map.remove(event.getSource());
                    } else {
                        map.put(event.getSource(), event);
                    }
                }
                it.remove();
            }
            dispatchRunning = false;
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
        
    }
    
}
