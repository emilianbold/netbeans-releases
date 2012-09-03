/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.gsf.testrunner.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.testrunner.api.TestSession.SessionResult;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.*;
import org.openide.windows.InputOutput;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 * This class gets informed about started and finished JUnit test sessions
 * and manages that the result windows and reports in them are appropriately
 * displayed, closed etc.
 * <p/>
 * <i>This is a modified copy of <code>o.n.m.junit.output.Manager</code></i>.
 * @author Marian Petras, Erno Mononen
 */
public final class Manager {

    /**
     * reference to the singleton of this class.
     * Strong references to the singleton are kept in instances of
     * {@link JUnitOutputReader JUnitOutputReader}.
     */
    private static Reference<Manager> instanceRef;

    /**
     * The current test sessions.
     */
    private final Set<TestSession> testSessions = new WeakSet<TestSession>(5);

    /**
     * if {@code true}, the window will only be promoted
     * at the end of Ant session
     */
    private final boolean lateWindowPromotion;

    private static final Logger LOGGER = Logger.getLogger(Manager.class.getName());
    
    public static final String JUNIT_TF = "junit"; // NOI18N
    public static final String TESTNG_TF = "testng"; // NOI18N
    private String testingFramework = ""; // NOI18N
    private Notification bubbleNotification = null;
    private final RequestProcessor.Task bubbleTask;
    private final RequestProcessor RP = new RequestProcessor(Manager.class.getName(), 1, true);
    
    public void setTestingFramework(String testingFramework) {
        this.testingFramework = testingFramework;
    }

    public String getTestingFramework() {
        return testingFramework;
    }

    /**
     * Returns a singleton instance of this class.
     * If no instance exists at the moment, a new instance is created.
     *
     * @return  singleton of this class
     */
    public static Manager getInstance() {
        if (instanceRef != null && instanceRef.get() != null) {
            return instanceRef.get();
        }

        final Manager instance = new Manager();

        ResultWindow.getInstance().addAncestorListener(new AncestorListener() {

            public void ancestorAdded(AncestorEvent event) {
                instance.updateDisplayHandlerLayouts();
            }

            public void ancestorRemoved(AncestorEvent event) {
                instance.updateDisplayHandlerLayouts();
            }

            public void ancestorMoved(AncestorEvent event) {
                instance.updateDisplayHandlerLayouts();
            }
        });
        instanceRef = new WeakReference<Manager>(instance);
        return instance;
    }

    /**
     * Updates the layout orientation of the test result window based on the
     * dimensions of the ResultWindow in its position.
     */
    private void updateDisplayHandlerLayouts() {
        int x = ResultWindow.getInstance().getWidth();
        int y = ResultWindow.getInstance().getHeight();

        int orientation = x > y
                ? JSplitPane.HORIZONTAL_SPLIT
                : JSplitPane.VERTICAL_SPLIT;

        ResultWindow.getInstance().setOrientation(orientation);
    }

    private Manager() {
        lateWindowPromotion = true;
        bubbleTask = RP.create(new Runnable() {

            @Override
            public void run() {
                bubbleNotification.clear();
            }
        });
    }

    public synchronized void emptyTestRun(TestSession session) {
        testStarted(session);
        sessionFinished(session);
    }
    /**
     * Called when an Ant task running JUnit tests is started.
     * Displays a message in the JUnit results window.
     */
    public synchronized void testStarted(final TestSession session) {
        displayMessage(
                session,
                NbBundle.getMessage(getClass(), "LBL_RunningTests"));   //NOI18N

        if (session.getStartingMsg() != null) {
            displayOutput(session, session.getStartingMsg(), true);
        }
    }

    /**
     */
    public synchronized void sessionFinished(final TestSession session) {
        if (!testSessions.contains(session)) {
            /* This session did not run the "junit" task. */
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Finishing an unknown session: " + session);
            }
            return;
        }

        displayMessage(session, null, true);  //updates the display

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Finishing session: " + session);
        }

        testSessions.remove(session);   //must be after displayMessage(...)
                                         //otherwise the window would get
                                         //activated
    }

    /**
     */
    public synchronized void displayOutput(final TestSession session,
                       final String text,
                       final boolean error) {

        final ResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displayOutput(text, error);
        displayInWindow(session, displayHandler);
    }

    /**
     *
     * @param  suiteName  name of the running suite; or {@code null} in the case
     *                    of anonymous suite
     */
    public synchronized void displaySuiteRunning(final TestSession session,
                             final String suiteName) {

        final ResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displaySuiteRunning(suiteName);
        displayInWindow(session, displayHandler);
    }

    /**
     * @param  suite  running suite
     */
    public synchronized void displaySuiteRunning(final TestSession session,
                             final TestSuite suite) {

        final ResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayHandler.displaySuiteRunning(suite);
        displayInWindow(session, displayHandler);
    }

    public void displayReport(final TestSession session,
                       final Report report) {
        displayReport(session, report, true);
    }

    /**
     */
    public synchronized void displayReport(final TestSession session,
                       final Report report, boolean completed) {

        /* Called from the AntLogger's thread */
        report.completed = completed;
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
    private void displayMessage(final TestSession session,
                                final String message) {
        displayMessage(session, message, false);
    }

    /**
     * Displays a message in the JUnit results window.
     * If this is the first display in the window, it also promotes
     * (displays, activates) it.
     *
     * @param  message  message to be displayed
     */
    private void displayMessage(final TestSession session,
                                final String message,
                                final boolean sessionEnd) {

        /* Called from the AntLogger's thread */

        final ResultDisplayHandler displayHandler = getDisplayHandler(session);
        displayInWindow(session, displayHandler, sessionEnd);
        if (!sessionEnd) {
            displayHandler.displayMessage(message);
        } else {
            displayHandler.displayMessageSessionFinished(message);
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
    private void displayInWindow(final TestSession session,
                                 final ResultDisplayHandler displayHandler) {
         displayInWindow(session, displayHandler, false);
    }

    /**
     */
    @NbBundle.Messages({"# {0} - project", "LBL_NotificationDisplayer_title=Tests finished successfully for project: {0}",
        "LBL_NotificationDisplayer_detailsText=Open Test Results Window"})
    private void displayInWindow(final TestSession session,
                                 final ResultDisplayHandler displayHandler,
                                 final boolean sessionEnd) {
        final boolean firstDisplay = (testSessions.add(session) == true);

        final boolean promote = session.getSessionType() == TestSession.SessionType.TEST
                ? firstDisplay || sessionEnd
                : sessionEnd;

        SessionResult sessionResult = session.getSessionResult();
        if (sessionResult.getErrors() + sessionResult.getFailed() > 0) {
            int displayIndex = getDisplayIndex(session);
            if (displayIndex == -1) {
                addDisplay(session);
                Mutex.EVENT.writeAccess(new Displayer(displayHandler, promote));
            } else if (promote) {
                Mutex.EVENT.writeAccess(new Displayer(null, promote));
            }
        } else {
            if (sessionEnd) {
                Mutex.EVENT.writeAccess(new Runnable() {

                    @Override
                    public void run() {
                        final ResultWindow window = ResultWindow.getInstance();
                        Mode mode = WindowManager.getDefault().findMode(window);
                        boolean isInSlidingMode = mode != null && mode.getName().contains("SlidingSide");   //NOI18N
                        if (window.isOpened() && !isInSlidingMode) {
                            window.promote();
                        } else if (!window.isOpened() || (window.isOpened() && !window.isShowing() && isInSlidingMode)) {
                            Icon icon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/gsf/testrunner/resources/testResults.png"));   //NOI18N
                            String projectname = ProjectUtils.getInformation(session.getProject()).getDisplayName();
                            
                            if(bubbleTask.cancel()) {
                                bubbleTask.schedule(0);
                            }
                            bubbleNotification = NotificationDisplayer.getDefault().notify(Bundle.LBL_NotificationDisplayer_title(projectname), icon,
                                    Bundle.LBL_NotificationDisplayer_detailsText(), new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    window.promote();
                                    bubbleTask.cancel();
                                }
                            });
                            bubbleTask.schedule(15000);
                        }
                    }
                });
            }
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
            if (promote) {
                window.promote();
            }
        }
    }

    /** singleton of the <code>ResultDisplayHandler</code> */
    // the ResultDisplayHandler holds TestSession and is referenced from other
    // places so we use WeakReference, otherwise there would be memory leak
    private Map<TestSession,WeakReference<ResultDisplayHandler>> displayHandlers;
    private Semaphore lock;
    /**
     */
    private synchronized ResultDisplayHandler getDisplayHandler(final TestSession session) {
        ResultDisplayHandler displayHandler = null;
        if (displayHandlers != null) {
            WeakReference<ResultDisplayHandler> reference = displayHandlers.get(session);
            if (reference != null) {
                displayHandler = reference.get();
            }
        } else {
            displayHandlers = new WeakHashMap<TestSession,WeakReference<ResultDisplayHandler>>(7);
        }

        if (displayHandler == null) {
            displayHandler = new ResultDisplayHandler(session);
            createIO(displayHandler);
            displayHandlers.put(session, new WeakReference<ResultDisplayHandler>(displayHandler));
            final ResultDisplayHandler dispHandler = displayHandler;
            lock = new Semaphore(1);
            try {
                lock.acquire(1);
            } catch (InterruptedException e) {
                LOGGER.log(Level.FINE, "Current thread was interrupted while acquiring a permit: {0}", e);
            }
            Mutex.EVENT.writeAccess(new Runnable() {

                @Override
                public void run() {
                    StatisticsPanel comp = (StatisticsPanel) dispHandler.getDisplayComponent().getLeftComponent();
                    dispHandler.setTreePanel(comp.getTreePanel());
                    lock.release();
                }
            });
            try {
                lock.acquire(1);
            } catch (InterruptedException e) {
                LOGGER.log(Level.FINE, "Current thread was interrupted while acquiring a permit: {0}", e);
            }
        }
        return displayHandler;
    }

    /**
     * Creates an <code>IOContainer</code> for the given <code>displayHandler</code>.
     *
     * @param displayHandler
     */
    private void createIO(final ResultDisplayHandler displayHandler) {
        try {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    final ResultWindow window = ResultWindow.getInstance();
                    window.setOutputComp(displayHandler.getOutputComponent());
                    InputOutput io = displayHandler.createIO(window.getIOContainer());
                    window.addDisplayComponent(displayHandler.getDisplayComponent(), io);
                }
            };
            if (SwingUtilities.isEventDispatchThread()){
                r.run();
            }else{
                SwingUtilities.invokeAndWait(r);
            }
        } catch (InterruptedException ex) {
            // The thread can be interrupted by pressing the Stop button.
            // Do nothing. #167514
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** */
    private Map<TestSession,Boolean> displaysMap;

    /**
     */
    private int getDisplayIndex(final TestSession session) {
        if (displaysMap == null) {
            return -1;
        }
        Boolean o = displaysMap.get(session);
        return (o != null) ? 0 : -1;
    }

    /**
     */
    private void addDisplay(final TestSession session) {
        if (displaysMap == null) {
            displaysMap = new WeakHashMap<TestSession,Boolean>(4);
        }
        displaysMap.put(session, Boolean.TRUE);
    }

}
