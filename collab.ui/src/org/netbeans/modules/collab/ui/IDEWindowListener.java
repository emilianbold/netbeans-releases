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
package org.netbeans.modules.collab.ui;

import java.awt.event.*;

import org.openide.windows.WindowManager;

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
