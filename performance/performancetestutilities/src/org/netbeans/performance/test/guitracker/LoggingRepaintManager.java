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
 * @author  Tim Boudreau, rkubacki@netbeans.org, mmirilovic@netbeans.org
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
        long waitAfterStartup = Long.getLong("org.netbeans.performance.waitafterstartup", 5000).longValue();
        
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
    
    /**
     * Enable / disable our Repaint Manager
     * @param val true - enable, false - disable
     */
    public void setEnabled (boolean val) {
        if (isEnabled() != val) {
            if (val) {
                enable();
            } else {
                disable();
            }
        }
    }
    
    /**
     * Get an answer on question "Is Repaint Manager enabled?"
     * @return true - repaint manager is enabled, false - it's disabled
     */
    public boolean isEnabled() {
        return orig != null;
    }
    
    /**
     * Enable Repaint Manager
     */
    private void enable() {
        orig = currentManager(new JLabel()); //could be null for standard impl
        setCurrentManager(this);
    }
    
    /**
     * Disable Repaint Manager
     */
    private void disable() {
        setCurrentManager(orig);
        orig = null;
    }
    
    /**
     * Measure onle explorer
     * @param ignore true - measure only explorer, false - measure everything
     */
    public void setOnlyExplorer (boolean ignore) {
        onlyExplorer = ignore;
    }
    
    /**
     * Measure onle editor
     * @param ignore true - measure only editor, false - measure everything
     */
    public void setOnlyEditor (boolean ignore) {
        onlyEditor = ignore;
    }
    
    private boolean hasValidateMatches = false;
    private boolean hasDirtyMatches = false;
    
    
    /**
     * Log the action when region is add to dirty regions.
     *
     * @param c component which is add to this region
     * @param x point where the region starts
     * @param y point where the region starts
     * @param w width of the region
     * @param h hieght of the region
     */
    public synchronized void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
        if (w > 10 && h > 18) { // painted region isn't cursor (or painted region is greater than cursor)
            if (onlyExplorer) {  // if you want measure only explorer
                Class clz = null;
                for (clz = c.getClass(); clz != null; clz = clz.getSuperclass()) {  // some components as ProjectsView uses own class for View so we are looking for those have superclass explorer.view
                    if (clz.getPackage().getName().equals("org.openide.explorer.view")) { // if it's explorer.view log this paint event
                        tr.add (ActionTracker.TRACK_APPLICATION_MESSAGE, "addDirtyRegion " + c.getClass().getName() + ", "+ x + "," + y + "," + w + "," + h);
                        hasDirtyMatches = true;
                        break;
                    }
                }
                if (clz == null) // if you are here, you were looking for superclass of your view , but it isn't explorer.view so we ignore this paint event
                    tr.add (ActionTracker.TRACK_APPLICATION_MESSAGE, "ignored addDirtyRegion " + c.getClass().getName() + ", "+ x + "," + y + "," + w + "," + h);
            } else if (onlyEditor) { // if you want measure only editor
                if (c.getClass().getName().equals("org.openide.text.QuietEditorPane")) { // repainted class has to be QuietEditorPane
                    tr.add (ActionTracker.TRACK_APPLICATION_MESSAGE, "addDirtyRegion " + c.getClass().getName() + ", "+ x + "," + y + "," + w + "," + h);
                    hasDirtyMatches = true;
                } else // ignore paints which are not from QuietEditorPane
                    tr.add (ActionTracker.TRACK_APPLICATION_MESSAGE, "ignored addDirtyRegion " + c.getClass().getName() + ", "+ x + "," + y + "," + w + "," + h);
            }
        }
        super.addDirtyRegion (c, x, y, w, h);
    }
    
    /**
     * Log the action when dirty regions are painted.
     */
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
