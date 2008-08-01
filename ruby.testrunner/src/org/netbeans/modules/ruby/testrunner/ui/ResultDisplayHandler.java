/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.ruby.testrunner.ui;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.ruby.testrunner.TestRunnerSettings;
import org.openide.ErrorManager;
import org.netbeans.modules.ruby.testrunner.TestRunnerSettings.DividerSettings;
/**
 *
 * @author Marian Petras. Erno Mononen
 */
final class ResultDisplayHandler {

    private static final Logger LOGGER = Logger.getLogger(ResultDisplayHandler.class.getName());
    
    /** */
    private static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(
            ResultDisplayHandler.class);
    /** */
    private ResultPanelTree treePanel;
    /** */
    private ResultPanelOutput outputListener;
    /** */
    private JSplitPane displayComp;
    
    /** Creates a new instance of ResultDisplayHandler */
    ResultDisplayHandler() {
    }

    /**
     */
    JSplitPane getDisplayComponent() {
        if (displayComp == null) {
            displayComp = createDisplayComp();
        }
        return displayComp;
    }

    /**
     */
    private JSplitPane createDisplayComp() {
        DividerSettings dividerSettings = TestRunnerSettings.getDefault().getDividerSettings(null);
        return createDisplayComp(new StatisticsPanel(this), new ResultPanelOutput(this), dividerSettings.getOrientation(), dividerSettings.getLocation());
    }

    private JSplitPane createDisplayComp(Component left, Component right, int orientation, final int location) {
        
        JSplitPane splitPane = new JSplitPane(orientation, left, right) {
            @Override
            public void addNotify() {
                super.addNotify();
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        setDividerLocation(location);
                    }
                });
            }

            @Override
            public void removeNotify() {
                DividerSettings newSettings = new DividerSettings(getOrientation(), getDividerLocation());
                TestRunnerSettings.getDefault().setDividerSettings(newSettings);
                super.removeNotify();
            }
        };
        splitPane.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ResultPanelTree"));
        splitPane.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ResultPanelTree"));
        return splitPane;
    }

    /**
     */
    void displayShown() {
        //
        //PENDING
        //
    }

    /**
     */
    void displayHidden() {
        //
        //PENDING
        //
    }
    //------------------ DISPLAYING OUTPUT ----------------------//
    static final Object[] EMPTY_QUEUE = new Object[0];
    private final Object queueLock = new Object();
    private volatile Object[] outputQueue;
    private volatile int outputQueueSize = 0;
    private int outputQueueAvailSpace;

    /**
     */
    Object getOutputQueueLock() {
        return queueLock;
    }

    /**
     */
    void setOutputListener(ResultPanelOutput outputPanel) {
        synchronized (queueLock) {
            this.outputListener = outputPanel;
        }
    }

    /**
     */
    void displayOutput(final String text, final boolean error) {

        /* Called from the AntLogger's thread */

        synchronized (queueLock) {
            if (outputQueue == null) {
                outputQueue = new Object[40];
                outputQueueAvailSpace = outputQueue.length - 1;
                outputQueueSize = 0;
            }
            final int itemSpace = error ? 2 : 1;
            if ((outputQueueAvailSpace -= itemSpace) < 0) {
                int newCapacity = (outputQueue.length < 640)
                        ? outputQueue.length * 2
                        : (outputQueue.length * 3) / 2;
                Object[] oldQueue = outputQueue;
                outputQueue = new Object[newCapacity];
                System.arraycopy(oldQueue, 0, outputQueue, 0, outputQueueSize);

                outputQueueAvailSpace += outputQueue.length - oldQueue.length;
            }
            if (error) {
                outputQueue[outputQueueSize++] = Boolean.TRUE;
            }
            outputQueue[outputQueueSize++] = text;

            if (outputListener != null) {
                outputListener.outputAvailable();
            }
        }
    }

    /**
     */
    Object[] consumeOutput() {
        synchronized (queueLock) {
            if (outputQueueSize == 0) {
                return EMPTY_QUEUE;
            }
            Object[] passedQueue = outputQueue;
            outputQueue = null;
            outputQueueSize = 0;
            return passedQueue;
        }
    }
    //-----------------------------------------------------------//
    //------------------- DISPLAYING TREE -----------------------//
    static final String ANONYMOUS_SUITE = new String();
    /**
     * name of the currently running suite - to be passed to the
     * {@link #treePanel} once it is initialized
     */
    private String runningSuite;
    private final List<Report> reports = new ArrayList<Report>();
    private String message;
    private boolean sessionFinished;

    /**
     *
     * @param  suiteName  name of the running suite; or {@code null} in the case
     *                    of anonymous suite
     */
    void displaySuiteRunning(String suiteName) {

        synchronized (this) {

            assert runningSuite == null;

            suiteName = (suiteName != null) ? suiteName : ANONYMOUS_SUITE;

            if (treePanel == null) {
                runningSuite = suiteName;
                return;
            }
        }

        displayInDispatchThread("displaySuiteRunning", suiteName);      //NOI18N

    }

    /**
     */
    void displayReport(final Report report) {

        synchronized (this) {
            if (treePanel == null) {
                reports.add(report);
                runningSuite = null;
                return;
            }
            assert runningSuite == null;
        }

        displayInDispatchThread("displayReport", report);               //NOI18N

    }

    /**
     */
    void displayMessage(final String msg) {

        /* Called from the AntLogger's thread */

        synchronized (this) {
            if (treePanel == null) {
                message = msg;
                return;
            }
        }

        displayInDispatchThread("displayMsg", msg);                     //NOI18N

    }

    /**
     */
    void displayMessageSessionFinished(final String msg) {

        /* Called from the AntLogger's thread */

        synchronized (this) {
            if (treePanel == null) {
                sessionFinished = true;
                message = msg;
                return;
            }
        }

        displayInDispatchThread("displayMsgSessionFinished", msg);        //NOI18N

    }
    /** */
    private Map<String, Method> methodsMap;

    /**
     * Calls a given display-method of class {@code ResutlPanelTree}
     * in the AWT event queue thread.
     *
     * @param  methodName  name of the {@code ResultPanelTree} method
     * @param  param  argument to be passed to the method
     */
    private void displayInDispatchThread(final String methodName,
            final Object param) {
        assert methodName != null;
        
        Method method = null;
        synchronized (this) {
            assert treePanel != null;

            method = prepareMethod(methodName);
            if (method == null) {
                LOGGER.log(Level.WARNING, "No such method: " + methodName);
                return;
            }
        }

        final Method finalMethod = method;
        
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                try {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Invoking: " + methodName + " with param: " + param);
                    }
                    finalMethod.invoke(treePanel, new Object[]{param});
                } catch (InvocationTargetException ex) {
                    ErrorManager.getDefault().notify(ex.getTargetException());
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
                }
            }
        });
    }

    /**
     */
    private Method prepareMethod(final String methodName) {
        Method method;

        if (methodsMap == null) {
            methodsMap = new HashMap<String, Method>(4);
            method = null;
        } else {
            method = methodsMap.get(methodName);
        }

        if ((method == null) && !methodsMap.containsKey(methodName)) {
            final Class paramType;
            if (methodName.equals("displayReport")) {                   //NOI18N

                paramType = Report.class;
            } else {
                assert methodName.equals("displayMsg") //NOI18N
                        || methodName.equals("displayMsgSessionFinished")//NOI18N
                        || methodName.equals("displaySuiteRunning");     //NOI18N

                paramType = String.class;
            }
            try {
                method = ResultPanelTree.class.getDeclaredMethod(methodName, new Class[]{paramType});
            } catch (Exception ex) {
                method = null;
                ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            }
            methodsMap.put(methodName, method);
        }

        return method;
    }

    /**
     */
    synchronized void setTreePanel(final ResultPanelTree treePanel) {
        assert EventQueue.isDispatchThread();

        /* Called from the EventDispatch thread */

        if (this.treePanel != null) {
            return;
        }

        this.treePanel = treePanel;

        if (message != null) {
            treePanel.displayMsg(message);
            message = null;
        }
        if (!reports.isEmpty()) {
            treePanel.displayReports(reports);
            reports.clear();
        }
        if (runningSuite != null) {
            treePanel.displaySuiteRunning(runningSuite != ANONYMOUS_SUITE
                    ? runningSuite
                    : null);
            runningSuite = null;
        }
        if (sessionFinished) {
            treePanel.displayMsgSessionFinished(message);
        }
    }
}
