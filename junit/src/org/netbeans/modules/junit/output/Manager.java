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

package org.netbeans.modules.junit.output;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.tools.ant.module.spi.AntSession;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * This class gets informed about started and finished JUnit test sessions
 * and manages that the result windows and reports in them are appropriately
 * displayed, closed etc.
 *
 * @author Marian Petras
 */
final class Manager {
    
    /**
     * reference to the singleton of this class.
     * Strong references to the singleton are kept in instances of
     * {@link JUnitOutputReader JUnitOutputReader}.
     */
    private static Reference instanceRef;
    
    /** list of sessions without windows displayed */
    //private List/*<AntSession>*/ pendingSessions;
    /** list of sessions with windows displayed */
    //private List/*<AntSession>*/ displayedSessions;
    /** */
    //private List/*<Report>*/ displayedReports;
    /**
     * registry of Ant sessions.
     * Each entry has a value of <code>Integer</code> whose value is
     * holds information about type of the session
     * (see {@link AntSessionInfo#sessionType}).
     * If the value is negative (opposite to the <code>AntSessionInfo</code>
     * constant), it means that method {@link #reportStarted} method
     * has not yet been called for the session.
     */
    private final Map<AntSession, TaskType> junitSessions
            = new WeakHashMap<AntSession, TaskType>(5);

    
    /**
     * Returns a singleton instance of this class.
     * If no instance exists at the moment, a new instance is created.
     *
     * @return  singleton of this class
     */
    static Manager getInstance() {
        Manager instance;
        Object inst = (instanceRef != null) ? instanceRef.get() : null;
        if (inst != null) {
            instance = (Manager) inst;
        } else {
            instance = new Manager();
            instanceRef = new WeakReference(instance);
        }
        return instance;
    }
    
    /**
     * Called when it is detected that the current session was initiated
     * by a test target. Displays a message in the JUnit results window.
     */
    void targetStarted(final AntSession session, final TaskType sessionType) {
        displayMessage(
                session,
                sessionType, 
                NbBundle.getMessage(getClass(), "LBL_PreparingTests")); //NOI18N
    }
    
    /**
     * Called when an Ant task running JUnit tests is started.
     * Displays a message in the JUnit results window.
     */
    void testStarted(final AntSession session, final TaskType sessionType) {
        displayMessage(
                session,
                sessionType, 
                NbBundle.getMessage(getClass(), "LBL_RunningTests"));   //NOI18N
    }
    
    /**
     */
    void sessionFinished(final AntSession session,
                         final TaskType sessionType,
                         final boolean initializationFailed) {
        Object o = junitSessions.get(session);
        if (o == null) {
            /* This session did not run the "junit" task. */
            return;
        }
        
        String message = initializationFailed
                         ? NbBundle.getMessage(
                                        getClass(),
                                        "LBL_TestBuildInitFailed")      //NOI18N
                         : null;
        displayMessage(session, sessionType, message);
        junitSessions.remove(session);   //must be after displayMessage(...)
                                         //otherwise the window would get
                                         //activated
        
        //<editor-fold defaultstate="collapsed" desc="disabled code">
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
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="disabled code">
//        Mutex.EVENT.writeAccess(new Runnable() {
//            public void run() {
//                final ResultWindow win = ResultWindow.getInstance();
//                //<editor-fold defaultstate="collapsed" desc="disabled code">
//                /*
//                final int emptyViewsToOpen
//                        = Math.max(0, index - win.getViewsCount());
//                for (int i = 0; i < emptyViewsToOpen; i++) {
//                    win.openEmptyView();
//                }
//                 */
//                //win.displayReport(index, report);
//                //</editor-fold>
//                win.displayReport(
//                        0,
//                        report,
//                        sessionType != AntSessionInfo.SESSION_TYPE_TEST);
//            }
//        });
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="disabled code">
        
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
        //</editor-fold>
    }
    
    /**
     */
    void displayOutput(final AntSession session,
                       final TaskType sessionType,
                       final String text,
                       final boolean error) {

        /* Called from the AntLogger's thread */

        final ResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displayOutput(text, error);
        displayInWindow(session, sessionType, displayHandler);
    }
    
    /**
     *
     * @param  suiteName  name of the running suite; or {@code null} in the case
     *                    of anonymous suite
     */
    void displaySuiteRunning(final AntSession session,
                             final TaskType sessionType,
                             final String suiteName) {

        /* Called from the AntLogger's thread */
        
        final ResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displaySuiteRunning(suiteName);
        displayInWindow(session, sessionType, displayHandler);
    }
    
    /**
     */
    void displayReport(final AntSession session,
                       final TaskType sessionType,
                       final Report report) {

        /* Called from the AntLogger's thread */
        
        final ResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displayReport(report);
        displayInWindow(session, sessionType, displayHandler);
    }
    
    /**
     * Displays a message in the JUnit results window.
     * If this is the first display in the window, it also promotes
     * (displays, activates) it.
     *
     * @param  message  message to be displayed
     */
    private void displayMessage(final AntSession session,
                                final TaskType sessionType,
                                final String message) {

        /* Called from the AntLogger's thread */

        final ResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displayMessage(message);
        displayInWindow(session, sessionType, displayHandler);
        
        //<editor-fold defaultstate="collapsed" desc="disabled code">
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
        //</editor-fold>
    }
    
    /**
     */
    private void displayInWindow(final AntSession session,
                                 final TaskType sessionType,
                                 final ResultDisplayHandler displayHandler) {
        final boolean promote =
                (junitSessions.put(session, sessionType) == null)
                && (sessionType == TaskType.TEST_TASK);
        
        int displayIndex = getDisplayIndex(session);
        if (displayIndex == -1) {
            addDisplay(session);
            
            Mutex.EVENT.writeAccess(new Displayer(displayHandler, promote));
        } else if (promote) {
            Mutex.EVENT.writeAccess(new Displayer(null, promote));
        }
    }
    
    /**
     *
     */
    private class Displayer implements Runnable {
        private final ResultDisplayHandler displayHandler;
        private final boolean promote;
        Displayer(final ResultDisplayHandler displayHandler,
                  final boolean promote) {
            this.displayHandler = displayHandler;
            this.promote = promote;
        }
        public void run() {
            final ResultWindow window = ResultWindow.getInstance();
            if (displayHandler != null) {
               window.addDisplayComponent(displayHandler.getDisplayComponent());
            }
            if (promote) {
               window.promote();
            }
        }
    }
    
    /** singleton of the <code>ResultDisplayHandler</code> */
    private Map/*<AntSession, ResultDisplayHandler>*/ displayHandlers;
    
    /**
     */
    private ResultDisplayHandler getDisplayHandler(final AntSession session) {
        ResultDisplayHandler displayHandler;
        
        Object o = (displayHandlers != null)
                   ? displayHandlers.get(session)
                   : null;
        if (o != null) {
            displayHandler = (ResultDisplayHandler) o;
        } else {
            if (displayHandlers == null) {
                displayHandlers = new WeakHashMap(7);
            }
            displayHandler = new ResultDisplayHandler();
            displayHandlers.put(session, displayHandler);
        }
        return displayHandler;
    }
    
    /** */
    private Map/*<AntSession, Boolean>*/ displaysMap;
    
    /**
     */
    private int getDisplayIndex(final AntSession session) {
        if (displaysMap == null) {
            return -1;
        }
        Object o = displaysMap.get(session);
        return (o != null) ? 0 : -1;
    }
    
    /**
     */
    private void addDisplay(final AntSession session) {
        if (displaysMap == null) {
            displaysMap = new WeakHashMap(4);
        }
        displaysMap.put(session, Boolean.TRUE);
    }
    
}
