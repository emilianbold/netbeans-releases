/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows;


import java.io.IOException;

import org.netbeans.core.NbTopManager;
import org.netbeans.core.windows.frames.ShortcutAndMenuKeyEventProcessor;
import org.netbeans.core.windows.util.WindowUtils;

import org.openide.ErrorManager;


/**
 *
 * @author  Peter Zavadsky
 */
public class WindowSystemImpl implements NbTopManager.WindowSystem {
    
    /** Creates a new instance of WindowSystemImpl */
    public WindowSystemImpl() {
    }
    
    
    public void show() {
        ShortcutAndMenuKeyEventProcessor.install();
        MainWindow.getDefault().showWindow();
    }
    
    public void hide() {
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        //Bugfix #30281
        wmi.setExitingIDE(true);
        WindowUtils.hideAllFrames();
        ShortcutAndMenuKeyEventProcessor.uninstall();
    }
    
    public void load() {
        MainWindow.getDefault().initialize();
    }
    
    public void save() {
        try {
            PersistenceManager.getDefault().writeXMLWaiting ();
        } catch(IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
   
}
