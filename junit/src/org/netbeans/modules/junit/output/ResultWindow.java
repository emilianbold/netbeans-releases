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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import javax.accessibility.AccessibleContext;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Marian Petras
 */
public class ResultWindow extends TopComponent {
    
    /** unique ID of <code>TopComponent</code> (singleton) */
    private static final String ID = "junit-test-results";              //NOI18N
    /**
     * instance/singleton of this class
     *
     * @see  #getInstance
     */
    private static WeakReference instance = null;
    
    /**
     * Returns a singleton of this class.
     *
     * @return  singleton of this class
     */
    static synchronized ResultWindow getInstance() {
        ResultWindow window;
        window = (ResultWindow) WindowManager.getDefault().findTopComponent(ID);
        if (window == null) {
            window = getDefault();
        }
        return window;
    }
    
    /**
     * Singleton accessor reserved for the window system only.
     * The window system calls this method to create an instance of this
     * <code>TopComponent</code> from a <code>.settings</code> file.
     * <p>
     * <em>This method should not be called anywhere except from the window
     * system's code.</em>
     *
     * @return  singleton - instance of this class
     */
    public static synchronized ResultWindow getDefault() {
        ResultWindow window;
        if ((instance == null)
                || ((window = (ResultWindow) instance.get()) == null)) {
            window = new ResultWindow();
            instance = new WeakReference(window);
        }
        return window;
    }
    
    /** */
    private TopComponent view;
    /** */
    private ResultDisplayHandler viewHandler;
    /** */
    private java.util.Map methodsMap;
    
    
    
    /** */
    //private final JTabbedPane tabbedPanel;
    
    private final String indent = "   ";
    private final String[] indents = new String[] {
        "",
        "   ",
        "      ",
        "         ",
        "            ",
        "               " };
    private int callLevel = 0;
    private void logStart(final String msg) {
        StringBuffer buf = buildIndent();
        buf.append(msg);
        buf.append(" (0x");
        buf.append(Integer.toHexString(System.identityHashCode(this)));
        buf.append(") - START");
        System.out.println(buf.toString());
        callLevel++;
    }
    private void logEnd(final String msg) {
        callLevel--;
        StringBuffer buf = buildIndent();
        buf.append(msg);
        buf.append(" (0x");
        buf.append(Integer.toHexString(System.identityHashCode(this)));
        buf.append(") - END");
        System.out.println(buf.toString());
    }
    private void log(final String msg) {
        StringBuffer buf = buildIndent();
        buf.append("- ");
        buf.append(msg);
        buf.append(" (0x");
        buf.append(Integer.toHexString(System.identityHashCode(this)));
        buf.append(')');
        System.out.println(buf.toString());
    }
    private StringBuffer buildIndent() {
        StringBuffer buf = new StringBuffer(150);
        if (callLevel < indents.length) {
            buf.append(indents[callLevel]);
        } else {
            buf.append(indents[indents.length - 1]);
            final int count = callLevel - (indents.length - 1);
            for (int i = 0; i < count; i++) {
                buf.append(indent);
            }
        }
        return buf;
    }
    
    /** Creates a new instance of ResultWindow */
    public ResultWindow() {
        super();
        setLayout(new BorderLayout());
        //add(tabbedPanel = new JTabbedPane(), BorderLayout.CENTER);
        
        setName(ID);
        setDisplayName(NbBundle.getMessage(ResultWindow.class,
                                           "TITLE_TEST_RESULTS"));      //NOI18N
        setIcon(Utilities.loadImage(
                "org/netbeans/modules/junit/output/res/testResults.png",//NOI18N
	        true));
        
        AccessibleContext accessibleContext = getAccessibleContext();
        accessibleContext.setAccessibleName(
                NbBundle.getMessage(getClass(), "ACSN_TestResults"));   //NOI18N
        accessibleContext.setAccessibleDescription(
                NbBundle.getMessage(getClass(), "ACSD_TestResults"));   //NOI18N
    }
    
    /**
     */
    protected void componentOpened() {
        assert EventQueue.isDispatchThread();
        
        forwardMessage("componentOpened");                              //NOI18N
        super.componentOpened();
    }
    
    /**
     */
    protected void componentClosed() {
        assert EventQueue.isDispatchThread();
        
        closeAllViews();
        forwardMessage("componentClosed");                              //NOI18N
        super.componentClosed();
    }
    
    /**
     */
    protected void componentActivated() {
        assert EventQueue.isDispatchThread();
        
        forwardMessage("componentActivated");                           //NOI18N
        super.componentActivated();
    }
    
    /**
     */
    protected void componentDeactivated() {
        assert EventQueue.isDispatchThread();
        
        forwardMessage("componentDeactivated");                         //NOI18N
        super.componentDeactivated();
    }
    
    /**
     */
    protected void componentShowing() {
        assert EventQueue.isDispatchThread();
        
        forwardMessage("componentShowing");                             //NOI18N
        super.componentShowing();
    }
    
    /**
     */
    protected void componentHidden() {
        assert EventQueue.isDispatchThread();
        
        forwardMessage("componentHidden");                              //NOI18N
        super.componentHidden();
    }
    
    /**
     */
    private void forwardMessage(String messageName) {
        if (methodsMap != null) {
            Method method = (Method) methodsMap.get(messageName);
            if (method != null) {
                try {
                    method.invoke(view, null);
                } catch (InvocationTargetException invocationExc) {
                    ErrorManager.getDefault().notify(invocationExc);
                } catch (Exception ex) {
                    methodsMap.remove(messageName);
                    ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
                }
            }
        }
    }
    
    /**
     */
    void displayTestRunning(boolean promote) {
        display(new ReportDisplay(null), promote);
    }
    
    /**
     */
    void displayReport(final int index, final Report report, boolean promote) {
        display(new ReportDisplay(report), promote);
    }
    
    /**
     */
    private void display(ReportDisplay reportDisplay, boolean promote) {
        assert EventQueue.isDispatchThread();
        
        if (viewHandler == null) {
            
            viewHandler = new ResultDisplayHandler();
            view = viewHandler.createReportDisplay();
            
            try {
                prepareMethods();
            } catch (SecurityException ex) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            }

            if (isOpened()) {
                forwardMessage("componentOpened");                      //NOI18N
                if (isShowing()) {
                    forwardMessage("componentShowing");                 //NOI18N
                    if (WindowManager.getDefault().getRegistry().getActivated()
                            == this) {
                        forwardMessage("componentActivated");           //NOI18N
                    }
                }
            }
            add(view);
        }

        reportDisplay.run();
        
        if (promote) {
            open();
            requestVisible();
            requestActive();
        }
    }
    
    /**
     *
     */
    final class ReportDisplay implements Runnable {
        
        private final Report report;
        
        ReportDisplay(Report report) {
            this.report = report;
        }
        
        public void run() {
            assert EventQueue.isDispatchThread();
            
            if (report == null) {
                viewHandler.displayMsg(
                        NbBundle.getMessage(getClass(), "LBL_Running"));//NOI18N
            } else {
                viewHandler.displayReport(report);
            }
        }
        
    }
    
    /**
     */
    private void prepareMethods() throws SecurityException {
        assert methodsMap == null;
        assert view != null;
        
        methodsMap = new java.util.HashMap(8);
        
        final String[] methodNames = new String[] {
            "componentOpened",                                          //NOI18N
            "componentClosed",                                          //NOI18N
            "componentActivated",                                       //NOI18N
            "componentDeactivated",                                     //NOI18N
            "componentShowing",                                         //NOI18N
            "componentHidden"                                           //NOI18N
        };
        Collection methods = new ArrayList(methodNames.length);
        
        final Class viewClass = view.getClass();
        final Class[] noParams = new Class[0];
        
        for (int i = 0; i < methodNames.length; i++) {
            try {
                String methodName = methodNames[i];
                Method m = viewClass.getDeclaredMethod(methodName, noParams);
                methodsMap.put(methodName, m);
                methods.add(m);
            } catch (NoSuchMethodException ex) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            }
        }
        
        if (!methods.isEmpty()) {
            Method[] methodsArray = new Method[methods.size()];
            methods.toArray(methodsArray);
            
            Method.setAccessible(methodsArray, true);
        }
    }
    
    /**
     */
    void removeView(final int index) {
        //tabbedPanel.remove(index);
    }
    
    /**
     */
    int getViewsCount() {
        //return tabbedPanel.getTabCount();
        return 1;
    }
    
    /**
     */
    private void closeView(final int index) {
        //PENDING
    }
    
    /**
     */
    private void closeAllViews() {
        //logStart("closeAllViews()");
        //tabbedPanel.removeAll();
        //logEnd("closeAllViews()");
    }
    
    /**
     */
    protected String preferredID() {
        return ID;
    }
    
    /**
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }
    
    /**
     */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    /**
     * Resolves to the {@linkplain #getDefault default instance} of this class.
     *
     * This method is necessary for correct functinality of window system's
     * mechanism of persistence of top components.
     */
    private Object readResolve() throws java.io.ObjectStreamException {
        return ResultWindow.getDefault();
    }
    
}
