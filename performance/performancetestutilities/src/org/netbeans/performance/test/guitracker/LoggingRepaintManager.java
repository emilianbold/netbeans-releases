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
        // lastPaint = System.nanoTime();
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
     * Measure only explorer
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
     * Measure only editor
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
        String log = "Dirty Region " + c.getClass().getName() + ", "+ x + "," + y + "," + w + "," + h;
        
        // fix for issue 73361, It looks like the biggest cursor is on Sol 10 (10,19) in textfields
        // of some dialogs
        if (w > 10 || h > 19) { // painted region isn't cursor (or painted region is greater than cursor)
            if (regionFilter != null && !regionFilter.accept(c)) {
                tr.add(ActionTracker.TRACK_APPLICATION_MESSAGE, "IGNORED - " + log);
            } else { // no filter || accepted by filter =>  measure it
                tr.add(ActionTracker.TRACK_APPLICATION_MESSAGE, "ADD - " + log);
                hasDirtyMatches = true;
            }
        }
        //System.out.println(log);
        super.addDirtyRegion(c, x, y, w, h);
    }
    
    public interface RegionFilter {
        public boolean accept(JComponent c);
        public String getFilterName();
    }
    
    private static final RegionFilter EXPLORER_FILTER =
            new RegionFilter() {

                public boolean accept(JComponent c) {
                    Class clz = null;

                    for (clz = c.getClass(); clz != null; clz = clz.getSuperclass()) {
                        if (clz.getPackage().getName().equals("org.openide.explorer.view")) {
                            return true;
                        }
                    }
                    return false;
                }

                public String getFilterName() {
                    return "Accept paints from package: org.openide.explorer.view";
                }
            };
    
    private static final RegionFilter EDITOR_FILTER =
            new RegionFilter() {

                public boolean accept(JComponent c) {
                    return c.getClass().getName().equals("org.openide.text.QuietEditorPane");
                }

                public String getFilterName() {
                    return "Accept paints from org.openide.text.QuietEditorPane";
                }
            };
    
    public void  setRegionFilter(RegionFilter filter) {
        if(filter != null)
            tr.add(ActionTracker.TRACK_CONFIG_APPLICATION_MESSAGE, "FILTER : " + filter.getFilterName());
        else
            tr.add(ActionTracker.TRACK_CONFIG_APPLICATION_MESSAGE, "FILTER : reset");
        
        regionFilter = filter;
    }
    
    /**
     * Log the action when dirty regions are painted.
     */
    public void paintDirtyRegions() {
        super.paintDirtyRegions();
        //System.out.println("Done superpaint ("+tr+","+hasDirtyMatches+").");
        if (tr != null && hasDirtyMatches) {
            lastPaint = System.nanoTime();
            tr.add(tr.TRACK_PAINT, "PAINTING - done");
            //System.out.println("Done painting - " +tr);
            hasDirtyMatches = false;
        }
    }
    
    /**
     * @deprecated use waitNoPaintEvent instead
     */
    public long waitNoEvent(long timeout) {
        return waitNoPaintEvent(timeout, false);
    }
    
    /** waits and returns when there is at least timeout millies without any
     * painting processing
     *
     * @return time of last painting
     */
    public long waitNoPaintEvent(long timeout) {
        return waitNoPaintEvent(timeout, false);
    }
    
    /** waits and returns when there is at least timeout millies without any
     * painting processing.
     *
     * @param afterPaint when set to true then this method checks if there was any paint
     *        and measures quiet period from this time
     *
     * @return time of last painting
     */
    private long waitNoPaintEvent(long timeout, boolean afterPaint) {
        long current = System.nanoTime();
        long first = current;
        while ((ActionTracker.nanoToMili(current - lastPaint) < timeout) || ((lastPaint == 0L) && afterPaint)) {
            try {
                Thread.currentThread().sleep(Math.min(ActionTracker.nanoToMili(current - lastPaint) + 20, timeout));
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            current = System.nanoTime();
            if (ActionTracker.nanoToMili(current - first) > MAX_TIMEOUT)
                return ActionTracker.nanoToMili(lastPaint);
        }
        return ActionTracker.nanoToMili(lastPaint);
    }
    
    /** Utility method used from NetBeans to measure startup time.
     * Initializes RepaintManager and associated ActionTracker and than
     * waits until paint happens and there is 5 seconds of inactivity.
     *
     * @return time of last paint
     */
    public static long measureStartup() {
        // load our EQ and repaint manager
        ActionTracker tr = ActionTracker.getInstance();
        LoggingRepaintManager rm = new LoggingRepaintManager(tr);
        rm.setEnabled(true);
        
        tr.startNewEventList("Startup time measurement");
        
        long waitAfterStartup = Long.getLong("org.netbeans.performance.waitafterstartup", 10000).longValue();
        long time = rm.waitNoPaintEvent(waitAfterStartup, true);
        
        String fileName = System.getProperty( "org.netbeans.log.startup.logfile" );
        java.io.File logFile = new java.io.File(fileName.substring(0,fileName.lastIndexOf('.')) + ".xml");
        
        tr.stopRecording();
        try {
            tr.exportAsXML(new java.io.PrintStream(logFile));
        }catch(Exception exc){
            System.err.println("Exception rises during writing log from painting of the main window :");
            exc.printStackTrace(System.err);
        }
        
        rm.setEnabled(false);
        return time;
    }
    
}
