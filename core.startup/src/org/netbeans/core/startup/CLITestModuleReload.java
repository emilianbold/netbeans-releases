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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.netbeans.CLIHandler;

/**
 * Handles the --reload command-line option.
 * @author Jesse Glick
 */
public final class CLITestModuleReload extends CLIHandler {
    
    public CLITestModuleReload() {
        super(CLIHandler.WHEN_INIT);
    }

    protected int cli(CLIHandler.Args args) {
        String[] argv = args.getArguments();
        for (int i = 0; i < argv.length; i++) {
            if (argv[i] == null) {
                continue;
            }
            if (argv[i].equals("--reload")) { // NOI18N
                argv[i++] = null;
                if (i == argv.length || argv[i].startsWith("--")) { // NOI18N
                    log("Argument --reload must be followed by a file name", args); // NOI18N
                    return 2;
                }
                File module = new File(argv[i]);
                argv[i] = null;
                try {
                    TestModuleDeployer.deployTestModule(module);
                } catch (IOException e) {
                    e.printStackTrace(new PrintStream(args.getOutputStream()));
                    return 2;
                }
            }
        }
        // OK.
        return 0;
    }
    
    private static void log(String msg, CLIHandler.Args args) {
        PrintWriter w = new PrintWriter(args.getOutputStream());
        w.println(msg);
        w.flush();
    }
    
    protected void usage(PrintWriter w) {
        w.println("Module reload options:"); // NOI18N
        w.println("  --reload /path/to/module.jar  Installs or reinstalls a module JAR file."); // NOI18N
    }

}
