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
package org.netbeans.modules.collab.ui;

import org.openide.*;
import org.openide.windows.*;

import java.awt.*;
import java.awt.event.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class IDEWindowListener extends WindowAdapter {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static IDEWindowListener INSTANCE;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private boolean active;

    /**
     *
     *
     */
    protected IDEWindowListener() {
        super();
    }

    /**
     *
     *
     */
    public void windowActivated(WindowEvent event) {
        setActive(true);
    }

    /**
     *
     *
     */
    public void windowDeactivated(WindowEvent event) {
        setActive(false);
    }

    /**
     *
     *
     */
    protected boolean isActive() {
        return active;
    }

    /**
     *
     *
     */
    protected void setActive(boolean value) {
        active = value;
        Debug.out.println("Active: " + active);
    }

    /**
     *
     *
     */
    public static synchronized void install() {
        INSTANCE = new IDEWindowListener();
        WindowManager.getDefault().getMainWindow().addWindowListener(INSTANCE);
    }

    /**
     *
     *
     */
    public static synchronized void uninstall() {
        if (INSTANCE != null) {
            WindowManager.getDefault().getMainWindow().removeWindowListener(INSTANCE);
        }
    }

    /**
     *
     *
     */
    public static synchronized boolean isWindowActive() {
        if (INSTANCE != null) {
            return INSTANCE.isActive();
        } else {
            return true;
        }
    }
}
