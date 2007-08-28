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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.lang.ref.WeakReference;
import javax.accessibility.AccessibleContext;
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
    private static WeakReference<ResultWindow> instance = null;
    
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
        ResultWindow window = (instance != null) ? instance.get() : null;
        if (window == null) {
            window = new ResultWindow();
            instance = new WeakReference<ResultWindow>(window);
        }
        return window;
    }
    
    /** */
    private Component view;
    
    
    /** Creates a new instance of ResultWindow */
    public ResultWindow() {
        super();
        setFocusable(true);
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
    void addDisplayComponent(Component displayComp) {
        assert EventQueue.isDispatchThread();
        
        removeAll();
        addView(displayComp);
    }
    
    /**
     */
    private void addView(final Component view) {
        assert EventQueue.isDispatchThread();
        
        this.view = view;
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
