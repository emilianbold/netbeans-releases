/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.welcome;

import java.lang.ref.WeakReference;
import org.openide.util.NbBundle;
import org.openide.*;
import org.openide.windows.*;

import java.io.*;
import java.awt.*;
import java.lang.reflect.Method;
import javax.swing.*;
import javax.accessibility.*;
import org.openide.ErrorManager;

/**
 * The welcome screen.
 * @author  Richard Gregor
 */
class WelcomeComponent extends TopComponent{
    static final long serialVersionUID=6021472310161712674L;
    private static WeakReference/*<WelcomeComponent>*/ component = 
                new WeakReference(null); 
    private JComponent panel;

    private boolean initialized = false;
    
    private WelcomeComponent(){
        setLayout(new BorderLayout());
        setName(NbBundle.getMessage(WelcomeComponent.class, "LBL_Tab_Title"));   //NOI18N             
        panel = null;
        initialized = false;
    }
    
    protected String preferredID(){
        return "WelcomeComponent";    //NOI18N
    }
    
    /**
     * #38900 - lazy addition of GUI components
     */    
    
    private void doInitialize() {
        initAccessibility();
        
        try{
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            panel =(JComponent)Class.forName(NbBundle.getMessage(
                  WelcomeComponent.class,"CLASS_content_panel"), true, cl).newInstance();
        }catch(Exception e){
            ErrorManager.getDefault().notify(e);
        }
        if(panel == null)
            return;

        add(panel);
        setFocusable(true);
    }
        
    /* Singleton accessor. As WelcomeComponent is persistent singleton this
     * accessor makes sure that WelcomeComponent is deserialized by window system.
     * Uses known unique TopComponent ID "Welcome" to get WelcomeComponent instance
     * from window system. "Welcome" is name of settings file defined in module layer.
     */
    public static WelcomeComponent findComp() {
        WelcomeComponent wc = (WelcomeComponent)component.get();
        if (wc == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent("Welcome"); // NOI18N
            if (tc != null) {
                if (tc instanceof WelcomeComponent) {
                    wc = (WelcomeComponent)tc;
                    component = new WeakReference(wc); 
                } else {
                    //Incorrect settings file?
                    IllegalStateException exc = new IllegalStateException
                    ("Incorrect settings file. Unexpected class returned." // NOI18N
                    + " Expected:" + WelcomeComponent.class.getName() // NOI18N
                    + " Returned:" + tc.getClass().getName()); // NOI18N
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exc);
                    //Fallback to accessor reserved for window system.
                    wc = WelcomeComponent.createComp();
                }
            } else {
                //WelcomeComponent cannot be deserialized
                //Fallback to accessor reserved for window system.
                wc = WelcomeComponent.createComp();
            }
        }       
        return wc;
    }
    
    /* Singleton accessor reserved for window system ONLY. Used by window system to create
     * WelcomeComponent instance from settings file when method is given. Use <code>findComp</code>
     * to get correctly deserialized instance of WelcomeComponent. */
    public static WelcomeComponent createComp() {
        WelcomeComponent wc = (WelcomeComponent)component.get();
        if(wc == null) {
            wc = new WelcomeComponent();
            component = new WeakReference(wc);
        }
        return wc;
    }
    
    /** Overriden to explicitely set persistence type of WelcomeComponent
     * to PERSISTENCE_ALWAYS */
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    static void clearRef(){
        component.clear();
    }
    
    private void initAccessibility(){
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WelcomeComponent.class, "ACS_Welcome_DESC")); // NOI18N
    }
    
    public void requestFocus(){
        super.requestFocus();
        panel.requestFocus();
    }
    
    public boolean requestFocusInWindow(){
        super.requestFocusInWindow();
        return panel.requestFocusInWindow();
    }
    

    /**
     * #38900 - lazy addition of GUI components
     */    
    public void addNotify() {
        if (!initialized) {
            initialized = true;
            doInitialize();
        }
        super.addNotify();
    }
    
    /**
     * Called when <code>TopComponent</code> is about to be shown.
     * Shown here means the component is selected or resides in it own cell
     * in container in its <code>Mode</code>. The container is visible and not minimized.
     * <p><em>Note:</em> component
     * is considered to be shown, even its container window
     * is overlapped by another window.</p>
     * @since 2.18
     *
     * #38900 - lazy addition of GUI components
     *
     */
    protected void componentShowing() {
        if (!initialized) {
            initialized = true;
            doInitialize();
        }
        super.componentShowing();
    }
    
}

