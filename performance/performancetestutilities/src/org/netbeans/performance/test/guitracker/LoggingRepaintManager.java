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
    
    private ActionTracker tr;
    
    private RepaintManager orig = null;
    
    private long lastPaint = 0L;
    
    /** Creates a new instance of LoggingRepaintManager */
    public LoggingRepaintManager(ActionTracker tr) {
        this.tr = tr;
        lastPaint = System.currentTimeMillis();
    }
    
    /**
     * Enable / disable our Repaint Manager
     * @param val true - enable, false - disable
     */
    public void setEnabled(boolean val) {
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
    public void setOnlyExplorer(boolean ignore) {
        if (ignore) {
            setRegionFilter(EXPLORER_FILTER);
        } else {
            setRegionFilter(null);
        }
    }
    
    /**
     * Measure onle editor
     * @param ignore true - measure only editor, false - measure everything
     */
    public void setOnlyEditor(boolean ignore) {
        if (ignore) {
            setRegionFilter(EDITOR_FILTER);
        } else {
            setRegionFilter(null);
        }
    }
    
    private boolean hasValidateMatches = false;
    private boolean hasDirtyMatches = false;
    private RegionFilter regionFilter;
    
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
        String log = "addDirtyRegion " + c.getClass().getName() + ", "+ x + "," + y + "," + w + "," + h;
        
        if (w > 10 && h > 18) { // painted region isn't cursor (or painted region is greater than cursor)
            if (regionFilter != null) {
                if (regionFilter.accept(c)) {
                    tr.add(ActionTracker.TRACK_APPLICATION_MESSAGE, log);
                    hasDirtyMatches = true;
                } else {
                    tr.add(ActionTracker.TRACK_APPLICATION_MESSAGE, "ignored " + log);
                }
            } else { // no filter =>  measure everything
                tr.add(ActionTracker.TRACK_APPLICATION_MESSAGE, log);
                hasDirtyMatches = true;
            }
        }
        super.addDirtyRegion(c, x, y, w, h);
    }
    
    public interface RegionFilter {
        public boolean accept(JComponent c);
    }
    
    private static final RegionFilter EXPLORER_FILTER =
            new RegionFilter() {
        public boolean accept(JComponent c) {
            Class clz = null;
            for (clz = c.getClass(); clz != null; clz = clz.getSuperclass()) {  // some components as ProjectsView uses own class for View so we are looking for those have superclass explorer.view
                if (clz.getPackage().getName().equals("org.openide.explorer.view")) { // if it's explorer.view log this paint event
                    return true;
                }
            }
            return false;
        }
    };
    
    private static final RegionFilter EDITOR_FILTER =
            new RegionFilter() {
        public boolean accept(JComponent c) {
            Class clz = null;
            return c.getClass().getName().equals("org.openide.text.QuietEditorPane");
        }
    };
    
    public void  setRegionFilter(RegionFilter filter) {
        regionFilter = filter;
    }
    
    /**
     * Log the action when dirty regions are painted.
     */
    public void paintDirtyRegions() {
        super.paintDirtyRegions();
        lastPaint = System.currentTimeMillis();
        if (tr != null && hasDirtyMatches) {
            tr.add(tr.TRACK_PAINT, "Done painting");
            hasDirtyMatches = false;
        }
    }
    
    /** waits and returns when there is at least timeout millies without any
     * painting processing
     *
     * @return time of last painting
     */
    public long waitNoEvent(long timeout) {
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
    private long waitNoEvent(long timeout, boolean afterPaint) {
        long current = System.currentTimeMillis();
        long first = current;
        while (((current - lastPaint) < timeout) || ((lastPaint == 0L) && afterPaint)) {
            try {
                Thread.currentThread().sleep(Math.min(current - lastPaint + 20, timeout));
            } catch (InterruptedException e) {
                // XXX what to do here?
            }
            current = System.currentTimeMillis();
            if (current - first > MAX_TIMEOUT)
                return lastPaint;
        }
        return lastPaint;
    }
    
    /** Utility method used from NetBeans to measure startup time.
     * Initializes RepaintManager and associated ActionTracker and than
     * waits until paint happens and there is 5 seconds of inactivity.
     *
     * @return time of last paint
     */
    public static long measureStartup() {
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
