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

package org.netbeans.core;

import org.netbeans.CLIHandler;

/**
 * Shows the main window, so it is fronted when second instance of
 * NetBeans tries to start.
 *
 * @author Jaroslav Tulach
 */
public class CLIOptions2 extends CLIHandler implements Runnable {
    /** number of invocations */
    private int cnt;

    /**
     * Create a default handler.
     */
    public CLIOptions2 () {
        super(WHEN_INIT);
    }
    
    protected int cli(Args arguments) {
        return cli(arguments.getArguments());
    }
    
    final int cli(String[] args) {
        if (cnt++ == 0) return 0;
        
        /*
        for (int i = 0; i < args.length; i++) {
            if ("--nofront".equals (args[i])) {
                return 0;
            }
        }
         */
        javax.swing.SwingUtilities.invokeLater (this);
        
        return 0;
    }
    
    public void run () {
        java.awt.Frame f = org.openide.windows.WindowManager.getDefault ().getMainWindow ();

        // makes sure the frame is visible
        f.setVisible(true);
        // uniconifies the frame if it is inconified
        if ((f.getExtendedState () & java.awt.Frame.ICONIFIED) != 0) {
            f.setExtendedState (~java.awt.Frame.ICONIFIED & f.getExtendedState ());
        }
        // moves it to front and requests focus
        f.toFront ();
        
        java.awt.Component c = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager ().getFocusOwner ();
        if (c != null) {
            c.requestFocus ();
        }
    }
    
    
    protected void usage(java.io.PrintWriter w) {
        //w.println(NonGui.getString("TEXT_help"));
    }
    
}
