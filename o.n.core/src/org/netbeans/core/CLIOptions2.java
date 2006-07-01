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
        
    }
    
    
    protected void usage(java.io.PrintWriter w) {
        //w.println(NonGui.getString("TEXT_help"));
    }
    
}
