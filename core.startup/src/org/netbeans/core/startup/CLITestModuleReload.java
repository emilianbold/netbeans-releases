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
