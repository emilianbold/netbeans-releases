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

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

import org.openide.ErrorManager;
import org.netbeans.modules.junit.JUnitSettings;

/**
 *
 * @author Marian Petras
 */
final class ResultDisplayHandler {
    
    /** */
    private static final String ID_TREE = "tree";                       //NOI18N
    /** */
    private static final String ID_OUTPUT = "output";                   //NOI18N
    
    /** */
    private ResultPanelTree treePanel;
    /** */
    private ResultPanelOutput outputListener;
    /** */
    private Component displayComp;
    
    
    /** Creates a new instance of ResultDisplayHandler */
    ResultDisplayHandler() {
    }
    
    /**
     */
    Component getDisplayComponent() {
        if (displayComp == null) {
            displayComp = createDisplayComp();
        }
        return displayComp;
    }
    
    /**
     */
    private Component createDisplayComp() {
        Component left = new StatisticsPanel(this);
        Component right = new ResultPanelOutput(this);
        JSplitPane splitPane
                = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right) {

            public void addNotify() {
                super.addNotify();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setDividerLocation(JUnitSettings.getDefault().getResultsSplitPaneDivider());
                    }
                });
            }

            public void removeNotify() {
                double proportionalLocation = (double)getDividerLocation() / (getWidth() - getDividerSize());
                if (proportionalLocation >= 0.0 && proportionalLocation <= 1.0) {
                    JUnitSettings.getDefault().setResultsSplitPaneDivider(proportionalLocation);
                }
                super.removeNotify();
            }
        };

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
    private List<Report> reports;
    private String message;
    
    /**
     *
     * @param  suiteName  name of the running suite; or {@code null} in the case
     *                    of anonymous suite
     */
    void displaySuiteRunning(String suiteName) {
        
        /* Called from the AntLogger's thread */
        
        assert runningSuite == null;
        
        suiteName = (suiteName != null) ? suiteName : ANONYMOUS_SUITE;
        
        synchronized (this) {
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
        
        /* Called from the AntLogger's thread */
        
        synchronized (this) {
            if (treePanel == null) {
                if (reports == null) {
                    reports = new ArrayList<Report>(10);
                }
                reports.add(report);
                runningSuite = null;
                return;
            }
        }
        
        displayInDispatchThread("displayReport", report);               //NOI18N
        
        assert runningSuite == null;
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
    
    /** */
    private Map<String,Method> methodsMap;
    
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
        assert treePanel != null;
        
        final Method method = prepareMethod(methodName);
        if (method == null) {
            return;
        }
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    method.invoke(treePanel, new Object[] {param});
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
            methodsMap = new HashMap<String,Method>(4);
            method = null;
        } else {
            method = methodsMap.get(methodName);
        }
        
        if ((method == null) && !methodsMap.containsKey(methodName)) {
            final Class paramType;
            if (methodName.equals("displayReport")) {                   //NOI18N
                paramType = Report.class;
            } else {
                assert methodName.equals("displayMsg")                  //NOI18N
                       || methodName.equals("displaySuiteRunning");     //NOI18N
                paramType = String.class;
            }
            try {
                method = ResultPanelTree.class
                         .getDeclaredMethod(methodName, new Class[] {paramType});
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
    void setTreePanel(final ResultPanelTree treePanel) {
        assert EventQueue.isDispatchThread();
        
        /* Called from the EventDispatch thread */
        
        synchronized (this) {
            if (this.treePanel != null) {
                return;
            }

            this.treePanel = treePanel;
        }
        
        if (message != null) {
            treePanel.displayMsg(message);
            message = null;
        }
        if (reports != null) {
            treePanel.displayReports(reports);
            reports = null;
        }
        if (runningSuite != null) {
            treePanel.displaySuiteRunning(runningSuite != ANONYMOUS_SUITE
                                          ? runningSuite
                                          : null);
            runningSuite = null;
        }
    }

}
