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


package org.netbeans.core.windows;


import org.netbeans.core.NbTopManager;
import org.netbeans.core.windows.persistence.PersistenceManager;
import org.netbeans.core.windows.services.DialogDisplayerImpl;


/**
 * Implementation of WindowSystem interface (declared in core NbTopManager).
 *
 * @author  Peter Zavadsky
 */
public class WindowSystemImpl implements NbTopManager.WindowSystem {

    /** Creates a new instance of WindowSystemImpl */
    public WindowSystemImpl() {
    }


    // Persistence
    /** Implements <code>NbTopManager.WindowSystem</code> interface method.
     * Loads window system persistent data. */
    public void load() {
        WindowManagerImpl.assertEventDispatchThread();
        
        PersistenceHandler.getDefault().load();
    }
    /** Implements <code>NbTopManager.WindowSystem</code> interface method. 
     * Saves window system persistent data. */
    public void save() {
        WindowManagerImpl.assertEventDispatchThread();
        
        PersistenceHandler.getDefault().save();
    }
    
    // GUI
    /** Implements <code>NbTopManager.WindowSystem</code> interface method. 
     * Shows window system. */
    public void show() {
        WindowManagerImpl.assertEventDispatchThread();
        
        DialogDisplayerImpl.runDelayed();
        ShortcutAndMenuKeyEventProcessor.install();
        WindowManagerImpl.getInstance().setVisible(true);
    }
    /** Implements <code>NbTopManager.WindowSystem</code> interface method. 
     * Hides window system. */
    public void hide() {
        WindowManagerImpl.assertEventDispatchThread();
        
        WindowManagerImpl.getInstance().setVisible(false);
        ShortcutAndMenuKeyEventProcessor.uninstall();
    }
    
    /**
     * Implements <code>NbTopManager.WindowSystem</code> interface method. 
     * Clears the window system model - does not delete the configuration
     * under Windows2Local! You have to delete the folder before calling
     * this method to really reset the window system state.
     */
    public void clear() {
        WindowManagerImpl.assertEventDispatchThread();
        hide();
        WindowManagerImpl.getInstance().resetModel();
        PersistenceManager.getDefault().clear();
        PersistenceHandler.getDefault().clear();
        load();
        show();        
    }
    
}
