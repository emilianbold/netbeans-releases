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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.RepaintManager;

/** A repaint manager which will logs information about interesting events.
 *
 * @author  Tim Boudreau
 */
public class LoggingRepaintManager extends RepaintManager {
    
    private static final long MAX_TIMEOUT = 60*1000L;
    
    /** Utility method used from NetBeans to measure startup time.
     * Initializes RepaintManager and associated ActionTracker and than 
     * waits until paint happens and there is 5 seconds of inactivity.
     *
     * @return time of last paint
     */
    public static long measureStartup () {
        long waitAfterStartup = Long.getLong("org.netbeans.performance.waitafterstartup", 5000);
        
        // XXX load our EQ and repaint manager
        ActionTracker tr = ActionTracker.getInstance();
        LoggingRepaintManager rm = new LoggingRepaintManager(tr);
        rm.setEnabled(true);
//        leq = new LoggingEventQueue(tr);
//        leq.setEnabled(true);
        long time = rm.waitNoEvent(waitAfterStartup, true);
        rm.setEnabled(false);
        return time;
    }
    
    private ActionTracker tr;
    
    private RepaintManager orig = null;
    
    private long lastPaint = 0L;
    
    private boolean onlyExplorer = false;
    private boolean onlyEditor = false;
    
    /** Creates a new instance of LoggingRepaintManager */
    public LoggingRepaintManager(ActionTracker tr) {
        this.tr = tr;
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
    
    public boolean isEnabled() {
        return orig != null;
    }
    
    private void enable() {
        orig = currentManager(new JLabel()); //could be null for standard impl
        setCurrentManager(this);
    }
    
    private void disable() {
        setCurrentManager(orig);
        orig = null;
    }
    
    public void setOnlyExplorer (boolean ignore) {
        onlyExplorer = ignore;
    }
    
    public void setOnlyEditor (boolean ignore) {
        onlyEditor = ignore;
    }
    
    private boolean hasValidateMatches = false;
    private boolean hasDirtyMatches = false;
    public synchronized void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
        if (w > 10 && h > 18) {
            if ((!onlyExplorer && !onlyEditor)
            ||  (onlyEditor && c.getClass().getName().equals("org.openide.text.QuietEditorPane"))) {
                tr.add (ActionTracker.TRACK_APPLICATION_MESSAGE, "addDirtyRegion " + c.getClass().getName() + ", "+ x + "," + y + "," + w + "," + h);
                hasDirtyMatches = true;
            }
            else if (onlyExplorer) {
                Class clz = null;
                for (clz = c.getClass(); clz != null; clz = clz.getSuperclass()) {
                    if (clz.getPackage().getName().equals("org.openide.explorer.view")) {
                        tr.add (ActionTracker.TRACK_APPLICATION_MESSAGE, "addDirtyRegion " + c.getClass().getName() + ", "+ x + "," + y + "," + w + "," + h);
                        hasDirtyMatches = true;
                        break;
                    }
                }
                if (clz == null) {
                    tr.add (ActionTracker.TRACK_APPLICATION_MESSAGE, "ignored addDirtyRegion " + c.getClass().getName() + ", "+ x + "," + y + "," + w + "," + h);
                }
            }
            else {
                if (onlyExplorer || onlyEditor) {
                    tr.add (ActionTracker.TRACK_APPLICATION_MESSAGE, "ignored addDirtyRegion " + c.getClass().getName() + ", "+ x + "," + y + "," + w + "," + h);
                }
            }
        }
        super.addDirtyRegion (c, x, y, w, h);
    }
    
    public void paintDirtyRegions() {
        super.paintDirtyRegions();
        if (tr != null && hasDirtyMatches) {
            lastPaint = System.currentTimeMillis();
            tr.add(tr.TRACK_PAINT, "Done painting");
            hasDirtyMatches = false;
        }
    }
    
    /** waits and returns when there is at least timeout millies without any
     * painting processing
     *
     * @return time of last painting
     */
    public long waitNoEvent (long timeout) {
        return waitNoEvent(timeout, false);
    }
    
    /** waits and returns when there is at least timeout millies without any
     * painting processing.
     *
     * @param afterPaint when set to true then this method checks if there was any paint 
     *        and measures quiet period from this time
     *
     * @return time of last painting
     */
    private long waitNoEvent (long timeout, boolean afterPaint) {
        long current = System.currentTimeMillis();
        long first = current;
        while (((current - lastPaint) < timeout) || ((lastPaint == 0L) && afterPaint)) {
            try {
                Thread.currentThread().sleep(Math.min(current - lastPaint + 20, timeout));
            }
            catch (InterruptedException e) {
                // XXX what to do here?
            }
            current = System.currentTimeMillis();
            if (current - first > MAX_TIMEOUT)
                return lastPaint;
        }
        return lastPaint;
    }

    /*
    public synchronized void addInvalidComponent(JComponent c) {
        if (filter.match(c)) {
            logger.log ("addInvalidComponent", c);
            hasValidateMatches = true;
        }
        super.addInvalidComponent(c);
    }
    
    public void validateInvalidComponents() {
        if (hasValidateMatches) {
            logger.log("validateInvalidComponents");
            hasValidateMatches = false;
        }
        super.validateInvalidComponents();
    }    
    */
}
