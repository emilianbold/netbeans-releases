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
    private final Map/*<AntSession, Integer>*/ junitSessions
            = new WeakHashMap(5);

    /** */
    private ComponentListener listener;

    
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
    void targetStarted(final AntSession session, final int sessionType) {
        displayMessage(
                session,
                sessionType, 
                NbBundle.getMessage(getClass(), "LBL_PreparingTests")); //NOI18N
    }
    
    /**
     * Called when an Ant task running JUnit tests is started.
     * Displays a message in the JUnit results window.
     */
    void testStarted(final AntSession session, final int sessionType) {
        displayMessage(
                session,
                sessionType, 
                NbBundle.getMessage(getClass(), "LBL_RunningTests"));   //NOI18N
    }
    
    /**
     */
    void reportStarted(final AntSession session) {
        Object sessType = junitSessions.get(session);
        assert (sessType == null) || (sessType.getClass() == Integer.class);
        if (sessType == null) {
            return;
        }
        
        if (((Integer) sessType).intValue()
                    != AntSessionInfo.SESSION_TYPE_TEST) {
            /*
             * For non-test sessions, the result window is displayed
             * only after the session finishes.
             */
            return;
        }
        
        //<editor-fold defaultstate="collapsed" desc="ComponentListener">
        /**
         * This class detects when the JUnit Results window is hidden
         * by the text output window and makes it appear again.
         */
        class DishonourAvenger implements ComponentListener {
            private boolean activated = false;
            
            public void componentMoved(ComponentEvent e) {}
            public void componentResized(ComponentEvent e) {}
            public void componentShown(ComponentEvent e) {}
            public void componentHidden(ComponentEvent e) {
                if (!activated) {
                    activated = true;
                    
                    final ResultWindow window = ResultWindow.getInstance();
                    window.removeComponentListener(this);
                    window.requestVisible();
                }
            }
        }
        //</editor-fold>
        
        Mutex.EVENT.writeAccess(new Runnable() {
            public void run() {
                ResultWindow.getInstance().addComponentListener(
                        listener = new DishonourAvenger());
            }
        });
    }
    
    /**
     */
    void sessionFinished(final AntSession session,
                         final int sessionType,
                         final boolean initializationFailed) {
        Object o = junitSessions.remove(session);
        assert (o == null) || (o instanceof Integer);
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
        
        if (listener != null) {
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    ResultWindow.getInstance().removeComponentListener(listener);
                }
            });
            listener = null;
        }
        
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
                       final String text,
                       final boolean error) {

        /* Called from the AntLogger's thread */

        final ResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displayOutput(text, error);
        displayInWindow(session, displayHandler);
    }
    
    /**
     */
    void displayReport(final AntSession session,
                       final Report report) {

        /* Called from the AntLogger's thread */
        
        final ResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displayReport(report);
        displayInWindow(session, displayHandler);
    }
    
    /**
     * Displays a message in the JUnit results window.
     * If this is the first display in the window, it also promotes
     * (displays, activates) it.
     *
     * @param  message  message to be displayed
     */
    private void displayMessage(final AntSession session,
                                final int sessionType,
                                final String message) {

        /* Called from the AntLogger's thread */

        final boolean promote =
                (junitSessions.put(session, new Integer(sessionType)) == null)
                && (sessionType == AntSessionInfo.SESSION_TYPE_TEST);
        
        final ResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displayMessage(message);
        displayInWindow(session, displayHandler);
        
        if (promote) {
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    ResultWindow.getInstance().promote();
                }
            });
        }
        
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
                                 final ResultDisplayHandler displayHandler) {
        int displayIndex = getDisplayIndex(session);
        if (displayIndex == -1) {
            addDisplay(session);
            
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    ResultWindow.getInstance().addDisplayComponent(
                            displayHandler.getDisplayComponent());
                }
            });
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
