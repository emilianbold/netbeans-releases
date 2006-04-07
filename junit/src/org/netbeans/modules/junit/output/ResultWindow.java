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
public final class ResultWindow extends TopComponent {
    
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
    private java.util.Map topCompMethodsMap;
    
    
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
        
        if (view != null) {
            forwardMessage(view, "componentOpened");                    //NOI18N
        }
        super.componentOpened();
    }
    
    /**
     */
    protected void componentClosed() {
        assert EventQueue.isDispatchThread();
        
        if (view != null) {
            forwardMessage(view, "componentClosed");                    //NOI18N
        }
        super.componentClosed();
    }
    
    /**
     */
    protected void componentActivated() {
        assert EventQueue.isDispatchThread();
        
        if (view != null) {
            forwardMessage(view, "componentActivated");                 //NOI18N
        }
        super.componentActivated();
    }
    
    /**
     */
    protected void componentDeactivated() {
        assert EventQueue.isDispatchThread();
        
        if (view != null) {
            forwardMessage(view, "componentDeactivated");               //NOI18N
        }
        super.componentDeactivated();
    }
    
    /**
     */
    protected void componentShowing() {
        assert EventQueue.isDispatchThread();
        
        if (view != null) {
            forwardMessage(view, "componentShowing");                   //NOI18N
        }
        super.componentShowing();
    }
    
    /**
     */
    protected void componentHidden() {
        assert EventQueue.isDispatchThread();
        
        if (view != null) {
            forwardMessage(view, "componentHidden");                    //NOI18N
        }
        super.componentHidden();
    }
    
    /**
     */
    private void forwardMessage(TopComponent tc, String messageName) {
        ensureMethodsPrepared();
        
        Method method = (Method) topCompMethodsMap.get(messageName);
        if (method != null) {
            try {
                method.invoke(tc, (Object[]) null);
            } catch (InvocationTargetException invocationExc) {
                ErrorManager.getDefault().notify(invocationExc);
            } catch (Exception ex) {
                topCompMethodsMap.remove(messageName);
                ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            }
        }
    }
    
    /**
     */
    private void ensureMethodsPrepared() {
        if (topCompMethodsMap == null) {
            prepareMethods();
        }
    }
    
    /**
     */
    private void prepareMethods() throws SecurityException {
        assert topCompMethodsMap == null;
        assert view != null;
        
        topCompMethodsMap = new java.util.HashMap(8);
        
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
                topCompMethodsMap.put(methodName, m);
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
    void addDisplayComponent(TopComponent displayComp) {
        assert EventQueue.isDispatchThread();
        
        removeAll();
        addView(displayComp);
    }
    
    /**
     */
    private void addView(final TopComponent view) {
        assert EventQueue.isDispatchThread();
        
        this.view = view;
        if (isOpened()) {
            forwardMessage(view, "componentOpened");                    //NOI18N
            if (isShowing()) {
                forwardMessage(view, "componentShowing");               //NOI18N
                if (isActivated()) {
                    forwardMessage(view, "componentActivated");         //NOI18N
                }
            }
        }
        add(view);
    }
    
    /**
     */
    private boolean isActivated() {
        return TopComponent.getRegistry().getActivated() == this;
    }
    
    /**
     */
    void promote() {
        assert EventQueue.isDispatchThread();
        
        open();
        requestVisible();
        requestActive();
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
