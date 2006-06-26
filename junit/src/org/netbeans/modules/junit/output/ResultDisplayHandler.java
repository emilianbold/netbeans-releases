/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.output;

import java.awt.EventQueue;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


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
    private MultiViewHandler handler;
    /** */
    private MultiViewPerspective treeViewHandle;
    /** */
    private MultiViewPerspective outputViewHandle;
    /** */
    private ResultPanelTree treePanel;
    /** */
    private ResultPanelOutput outputListener;
    /** */
    private TopComponent displayComp;
    
    
    /** Creates a new instance of ResultDisplayHandler */
    ResultDisplayHandler() {
    }
    
    /**
     */
    TopComponent getDisplayComponent() {
        if (displayComp == null) {
            displayComp = createDisplayComp();
        }
        return displayComp;
    }
    
    /**
     */
    private TopComponent createDisplayComp() {
        AbstractResultViewDesc descTree
                = new ResultTreeViewDesc(ID_TREE,
                                         "LBL_resultTreeView");         //NOI18N
        AbstractResultViewDesc descOutput
                = new ResultOutputViewDesc(ID_OUTPUT,
                                           "LBL_resultOutputView");     //NOI18N
        TopComponent tc = MultiViewFactory.createMultiView(
                    new MultiViewDescription[] { descTree, descOutput },
                    descTree);
        setUpNavigation(tc);
                    
        handler = MultiViews.findMultiViewHandler(tc);
        
        MultiViewPerspective[] viewHandles = handler.getPerspectives();
        treeViewHandle = viewHandles[0];
        outputViewHandle = viewHandles[1];
        
        return tc;
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
    
    /**
     */
    private void setUpNavigation(JComponent c) {
        final String actionName = "toggleView";                         //NOI18N
        
        final int keyModifiers = InputEvent.ALT_DOWN_MASK
                                 | InputEvent.SHIFT_DOWN_MASK;

        final InputMap inputMap = c.getInputMap(
                            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,  keyModifiers),
                     actionName);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, keyModifiers),
                     actionName);
        
        c.getActionMap().put(actionName, new ToggleViewAction());
    }
    
    /**
     *
     */
    final class ToggleViewAction extends AbstractAction {
        
        /*
         * PENDING
         * This is a hack! The default mechanism of MultiView should be used!
         */
        
        /**
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            MultiViewPerspective currView = handler.getSelectedPerspective();
            
            if (currView == treeViewHandle) {
                handler.requestActive(outputViewHandle);
            } else if (currView == outputViewHandle) {
                handler.requestActive(treeViewHandle);
            } else {
                assert false;
            }
        }
        
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
    private String runningSuite;
    private List reports;
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
                    reports = new ArrayList(10);
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
    private Map/*<String, Method>*/ methodsMap;
    
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
            methodsMap = new HashMap/*<String, Method>*/(4);
            method = null;
        } else {
            method = (Method) methodsMap.get(methodName);
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
        }
    }

    //-----------------------------------------------------------//
    
    /**
     * Partial implementation of a class describing single view of the multiview
     * result window.
     *
     * @author  Marian Petras
     */
    private abstract static class AbstractResultViewDesc
                                            implements MultiViewDescription {
        
        /**
         * this view's ID part 
         * - ID of this view among all views of the result window
         *
         * @serial  this view's ID part - ID of this view among all views
         *          of the result window
         */
        private final String idPart;
        private transient String id;
        /**
         * bundle key for this view's display name
         *
         * @serial  bundle key for this view's display name
         *          - the corresponding entry must exist in the module's bundle
         */
        private final String displayNameKey;
        private transient String displayName;
        
        private AbstractResultViewDesc(String id,
                                       String displayNameKey) {
            this.idPart = id;
            this.displayNameKey = displayNameKey;
        }

        public String preferredID() {
            //PENDING:
            return idPart;
        }

        public String getDisplayName() {
            if (displayName == null) {
                try {
                    displayName = NbBundle.getMessage(ResultDisplayHandler.class,
                                                      displayNameKey);
                } catch (MissingResourceException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,
                                                     ex);
                    displayName = "??? (" + displayNameKey + ')';       //NOI18N
                }
            }
            return displayName;
        }

        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_NEVER;
        }

    }
    
    /**
     * Descriptor of the tree view of the result window multiview.
     *
     * @author  Marian Petras
     */
    private final class ResultTreeViewDesc extends AbstractResultViewDesc
                                           implements Serializable {
        
        static final long serialVersionUID = -4369549644387784567L;
        
        private ResultTreeViewDesc(final String id,
                                   final String displayNameKey) {
            super(id, displayNameKey);
        }

        public java.awt.Image getIcon() {
            return null;
        }

        public MultiViewElement createElement() {
            return new ResultViewTree(ResultDisplayHandler.this);
        }
        
        public HelpCtx getHelpCtx() {
            //PENDING:
            return HelpCtx.DEFAULT_HELP;
        }

    }
    
    /**
     * Descriptor of the output view of the result window multiview.
     *
     * @author  Marian Petras
     */
    private final class ResultOutputViewDesc extends AbstractResultViewDesc
                                             implements Serializable {
        
        static final long serialVersionUID = -6007918504501341013L;
        
        private ResultOutputViewDesc(final String id,
                                     final String displayNameKey) {
            super(id, displayNameKey);
        }

        public java.awt.Image getIcon() {
            return null;
        }

        public MultiViewElement createElement() {
            return new ResultViewOutput(ResultDisplayHandler.this);
        }
        
        public HelpCtx getHelpCtx() {
            //PENDING:
            return HelpCtx.DEFAULT_HELP;
        }

    }
    
}
