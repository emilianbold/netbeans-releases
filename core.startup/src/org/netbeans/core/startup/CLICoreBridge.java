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

package org.netbeans.core.startup;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.security.*;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;

import org.netbeans.CLIHandler;

import org.openide.util.NbBundle;

/**
 * Handler for core.jar options.
 * @author Jaroslav Tulach
 */
public class CLICoreBridge extends CLIHandler {
    /**
     * Create a default handler.
     */
    public CLICoreBridge() {
        super(WHEN_INIT);
    }
    
    protected int cli(Args arguments) {
        return CoreBridge.getDefault().cli(
            arguments.getArguments(), 
            arguments.getInputStream(), 
            arguments.getOutputStream(),
            arguments.getErrorStream(),
            arguments.getCurrentDirectory()
        );
    }

    protected void usage(PrintWriter w) {
        // #65157: Currently this is not needed, when it will be we need
        // to be more careful and initialize module system first...
        //CoreBridge.getDefault().cliUsage(w);
    }
}
