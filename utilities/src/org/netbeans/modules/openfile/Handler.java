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

package org.netbeans.modules.openfile;

import java.io.File;
import java.io.PrintWriter;
import org.netbeans.CLIHandler;
import org.openide.util.Lookup;

/**
 * A CLI handler for Open File.
 * @author Jesse Glick
 */
public class Handler extends CLIHandler {
    
    /**
     * Create a handler. Called by core.
     */
    public Handler() {
        super(WHEN_EXTRA);
    }
    
    private File findFile (File curDir, String name) {
        File f = new File(name);
        if (!f.isAbsolute()) {
            f = new File(curDir, name);
        }
        return f;
    }
    
    private int openFile (File curDir, CLIHandler.Args args, String[] argv, int i) {
        String s = argv[i];
        if (s == null) {
            log("Missing argument to --open", args);
            return 2;
        }
        argv[i] = null;
        int line = -1;
        File f = findFile (curDir, s);
        if (!f.exists()) {
            // Check if it is file:line syntax.
            int idx = s.lastIndexOf(':'); // NOI18N
            if (idx != -1) {
                try {
                    line = Integer.parseInt(s.substring(idx + 1)) - 1;
                    f = findFile (curDir, s.substring(0, idx));
                } catch (NumberFormatException e) {
                    // OK, leave as a filename
                }
            }
        }
        // Just make sure it was opened, then exit.
        boolean success = OpenFile.openFile(f, line);
        return success ? 0 : 1;
    }
    
    protected int cli(CLIHandler.Args args) {
        String[] argv = args.getArguments();
        File curDir = args.getCurrentDirectory ();
        for (int i = 0; i < argv.length; i++) {
            if (argv[i] == null) {
                continue;
            }
            if (argv[i].equals("--open") || argv[i].equals("-open")) { // NOI18N
                argv[i] = null;
                if (i == argv.length - 1) {
                    log("Missing argument to --open", args);
                    return 2;
                }
                i++;
                while (i < argv.length && !argv[i].startsWith ("-")) {
                    int res = openFile (curDir, args, argv, i++);
                    if (res != 0) {
                        return res;
                    }
                }
            } 
        }
        // No problems.
        return 0;
    }
    
    private static void log(String msg, CLIHandler.Args args) {
        PrintWriter w = new PrintWriter(args.getOutputStream());
        w.println(msg);
        w.flush();
        // don't close however - might be another user
    }
    
    protected void usage(PrintWriter w) {
        w.println("OpenFile module options:");
        w.println("  --open FILE           open FILE.");
        w.println("  --open FILE:LINE      open FILE at line LINE (starting from 1).");
        w.println("");
    }
}
