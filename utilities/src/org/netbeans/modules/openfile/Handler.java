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

package org.netbeans.modules.openfile;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.NbBundle;

/**
 * Processor for command line options.
 * @author Jesse Glick, Jaroslav Tulach
 */
public class Handler extends OptionProcessor {
    private Option open;
    private Option defaultOpen;

    public Handler() {
    }

    protected Set<Option> getOptions() {
        if (open == null) {
            defaultOpen = Option.defaultArguments();
            Option o = Option.additionalArguments(Option.NO_SHORT_NAME, "open"); // NOI18N
            String bundle = "org.netbeans.modules.openfile.Bundle"; // NOI18N
            o = Option.shortDescription(o, bundle, "MSG_OpenOptionDescription"); // NOI18N
            o = Option.displayName(o, bundle, "MSG_OpenOptionDisplayName"); // NOI18N            
            open = o;
            
            assert open != null;
            assert defaultOpen != null;
        }
        
        HashSet<Option> set = new HashSet<Option>();
        set.add(open);
        set.add(defaultOpen);
        
        return set;
    }

    protected void process(Env env, Map<Option, String[]> optionValues) throws CommandException {
        String[] argv = optionValues.get(open);
        if (argv == null) {
            argv = optionValues.get(defaultOpen);
        }
        if (argv == null || argv.length == 0) {
            throw new CommandException(2, NbBundle.getMessage(Handler.class, "EXC_MissingArgOpen")); 
        }
        
        File curDir = env.getCurrentDirectory ();

        for (int i = 0; i < argv.length; i++) {
            int res = openFile (curDir, env, argv[i]);
            if (res != 0) {
                throw new CommandException(res);
            }
        }
    }

    private File findFile (File curDir, String name) {
        File f = new File(name);
        if (!f.isAbsolute()) {
            f = new File(curDir, name);
        }
        return f;
    }
    
    private int openFile (File curDir, Env args, String s) {
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
}
