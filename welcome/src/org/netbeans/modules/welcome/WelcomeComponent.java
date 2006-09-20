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

package org.netbeans.modules.welcome;

import java.lang.ref.WeakReference;
import org.netbeans.modules.welcome.content.ContentPanel;
import org.openide.util.NbBundle;
import org.openide.windows.*;
import java.awt.*;
import javax.swing.*;
import org.netbeans.modules.welcome.content.ContentFactory;
import org.openide.ErrorManager;
import org.openide.nodes.Node;

/**
 * The welcome screen.
 * @author  Richard Gregor
 */
public class WelcomeComponent extends TopComponent {
    static final long serialVersionUID=6021472310161712674L;
    private static WeakReference/*<WelcomeComponent>*/ component =
                new WeakReference(null); 
    private JComponent content;

    private boolean initialized = false;
    
    private WelcomeComponent(){
        setLayout(new BorderLayout());
        setName(NbBundle.getMessage(WelcomeComponent.class, "LBL_Tab_Title"));   //NOI18N             
        content = null;
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
        
        content = ContentFactory.createContentPane();
        if( null == content )
            return;

        add( content, BorderLayout.CENTER );
        setFocusable( false );
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
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    private void initAccessibility(){
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(WelcomeComponent.class, "ACS_Welcome_DESC")); // NOI18N
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
        setActivatedNodes( new Node[] {} );
    }

    private static boolean firstTimeOpen = true;
    protected void componentOpened() {
        super.componentOpened();
        if( firstTimeOpen ) {
            firstTimeOpen = false;
            if( !WelcomeOptions.getDefault().isShowOnStartup() ) {
                close();
            }
        }
    }

    protected void componentActivated() {
        super.componentActivated();
        focusAnyContentPanel( content );
    }

    private boolean focusAnyContentPanel( Container comp ) {
        Component[] children = comp.getComponents();
        for( int i=0; i<children.length; i++ ) {
            if( children[i] instanceof ContentPanel ) {
                ((ContentPanel)children[i]).switchFocus();
                return true;
            } else if( children[i] instanceof Container 
                    && focusAnyContentPanel( (Container)children[i] ) ) {
                return true;
            }
        }
        return false;
    }
}

