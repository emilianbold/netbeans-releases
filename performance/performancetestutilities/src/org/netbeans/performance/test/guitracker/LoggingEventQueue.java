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

package org.netbeans.performance.test.guitracker;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.Toolkit;

import java.lang.reflect.Method;

import java.util.Stack;

/**
 *
 * @author  Tim Boudreau
 */
public class LoggingEventQueue extends EventQueue {

    private static Method popMethod = null;
    static {
        try {
            popMethod = EventQueue.class.getDeclaredMethod ("pop", new Class [] {} );
            popMethod.setAccessible(true);
        } catch (Exception e) {
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
                Stack<EventQueue> stack = new Stack<EventQueue>();
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
                    EventQueue next = stack.pop();
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
            popMethod.invoke(result, new Object[] {});
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
