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

package org.netbeans.modules.junit.output;

import org.apache.tools.ant.module.spi.AntSession;
import org.openide.util.Mutex;

/**
 * This class gets informed about started and finished JUnit test sessions
 * and manages that the result windows and reports in them are appropriately
 * displayed, closed etc.
 *
 * @author Marian Petras
 */
final class Manager {
    
    /** singleton */
    private static Manager instance;
    
    /** list of sessions without windows displayed */
    //private List/*<AntSession>*/ pendingSessions;
    /** list of sessions with windows displayed */
    //private List/*<AntSession>*/ displayedSessions;
    /** */
    //private List/*<Report>*/ displayedReports;

    /**
     */
    static Manager getInstance() {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }
    
    /**
     */
    synchronized void taskStarted(final AntSession session) {
        /*
        if ((pendingSessions != null) && pendingSessions.contains(session)) {
            return;
        }
        if ((displayedSessions != null)
                && displayedSessions.contains(session)) {
            return;
        }
         */
        sessionStarted(session);
    }
    
    /**
     */
    private void sessionStarted(final AntSession session) {
        /*
         * This method is called only from method taskStarted(AntSession)
         * which is synchronized.
         */
        
        /*
        if (pendingSessions == null) {
            pendingSessions = new ArrayList(4);
        }
        pendingSessions.add(session);
         */
        
        /* Close all windows with reports displayed: */
        /*
        assert (displayedSessions == null) == (displayedReports == null);
        if (displayedSessions != null) {
            assert displayedReports.size() == displayedSessions.size();
            
            ListIterator iDispRep
                    = displayedReports.listIterator(displayedReports.size());
            ListIterator iDispSes
                    = displayedSessions.listIterator(displayedSessions.size());
            final List indexes
                    = new ArrayList(displayedReports.size());
            while (iDispRep.hasPrevious()) {
                int index = iDispSes.previousIndex();
                Object r = iDispRep.previous();
                Object s = iDispSes.previous();
                if (r == null) {
                    indexes.add(new Integer(index));
                    iDispRep.remove();
                    iDispSes.remove();
                }
            }

            assert displayedSessions.size() == displayedReports.size();

            if (displayedSessions.isEmpty()) {
                displayedSessions = null;
                displayedReports = null;
            }
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    ResultWindow win = ResultWindow.getInstance();
                    for (Iterator i = indexes.iterator(); i.hasNext(); ) {
                        win.removeView(((Integer) i.next()).intValue());
                    }
                }
            });
        }
         */
    }
    
    /**
     */
    synchronized void sessionFinished(final AntSession session,
                                      final Report report) {
        /*
        assert ((pendingSessions != null
                        && pendingSessions.contains(session)))
               ^ ((displayedSessions != null)
                        && (displayedSessions.contains(session)));
        
        int indexDisp = -1;
        if (displayedSessions != null) {
            indexDisp = displayedSessions.indexOf(session);
        }
        int indexPend = -1;
        if (indexDisp == -1) {
            indexPend = pendingSessions.indexOf(session);
        }
         */
        
        /*
         * Display windows for pending sessions started earlier,
         * display the report in a new window ...
         */
        /*
        final int index = (indexDisp != -1) 
                          ? indexDisp
                          : (displayedSessions == null)
                            ? indexPend
                            : displayedSessions.size() + indexPend;
         */
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                final ResultWindow win = ResultWindow.getInstance();
                /*
                final int emptyViewsToOpen
                        = Math.max(0, index - win.getViewsCount());
                for (int i = 0; i < emptyViewsToOpen; i++) {
                    win.openEmptyView();
                }
                 */
                //win.displayReport(index, report);
                win.displayReport(0, report);
            }
        });
        /* ... and update information about displayed and pending sessions: */
        /*
        if (indexDisp != -1) {
            displayedReports.set(indexDisp, report);
        } else {     //(indexPend != -1)
            if (displayedSessions == null) {
                displayedSessions = new ArrayList(4);
            }
            if (indexPend == pendingSessions.size() - 1) {
                displayedSessions.addAll(pendingSessions);
                pendingSessions = null;
            } else {
                displayedSessions.addAll(
                        pendingSessions.subList(0,
                                                indexPend + 1));
                pendingSessions = new ArrayList(
                        pendingSessions.subList(indexPend + 1,
                                                pendingSessions.size()));
            }
            
            if (displayedReports == null) {
                displayedReports = new ArrayList(4);
            }
            for (int i = 0; i < indexPend; i++) {
                displayedReports.add(null);
            }
            displayedReports.add(report);
        }
         */
    }
    
}
