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

package org.netbeans.performance.test.guitracker;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author  Tim Boudreau
 */
public class LoggingEventQueue extends EventQueue {

    private static Method popMethod = null;
    static {
        try {
            popMethod = EventQueue.class.getDeclaredMethod ("pop", (Class)null);
            popMethod.setAccessible(true);
        } catch (Exception e) {
            // XXX exception handling
            e.printStackTrace(System.err);
        }
    }
    
    private ActionTracker tr;

    private EventQueue orig = null;
    
    /** Creates a new instance of LoggingEventQueue */
    public LoggingEventQueue(ActionTracker tr) {
        this.tr = tr;
    }

    public void postEvent(AWTEvent e) {
        tr.add(e);
        super.postEvent (e);
    }
    
    public boolean isEnabled() {
        return orig != null;
    }
    
    public void setEnabled (boolean val) {
        if (isEnabled() != val) {
            if (val) {
                enable();
            } else {
                disable();
            }
        }
    }
    
    private void enable() {
        if (!isEnabled()) {
            orig = Toolkit.getDefaultToolkit().getSystemEventQueue();
            orig.push (this);
            System.err.println("Installed logging event queue"); // XXX use logger?
        }
    }
    
    private void disable() {
        try {
            if (isEnabled()) {
                Stack stack = new Stack();
                EventQueue curr = Toolkit.getDefaultToolkit().getSystemEventQueue();
                while (curr != this) {
                    curr = popQ();
                    if (curr != this) {
                        stack.push(curr);
                    }
                }
                pop();
                curr = orig;
                assert Toolkit.getDefaultToolkit().getSystemEventQueue() == orig;
                while (!stack.isEmpty()) {
                    EventQueue next = (EventQueue) stack.pop();
                    curr.push(next);
                    curr = next;
                }
            System.err.println("Uninstalled logging event queue"); // use logger?
            }
        } finally {
            orig = null;
        }
    }
    
    public synchronized void push(EventQueue newEventQueue) {
        if (newEventQueue instanceof LoggingEventQueue) {
            return;
        }
    }
    
    private EventQueue popQ() { 
        try {
            if (popMethod == null) {
                throw new IllegalStateException("Can't access EventQueue.pop");
            }
            EventQueue result = Toolkit.getDefaultToolkit().getSystemEventQueue();
            popMethod.invoke(result, (Class) null);
            return result;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new IllegalStateException ("Can't invoke EventQueue.pop"); 
            }
        }
        
    }
    
}
