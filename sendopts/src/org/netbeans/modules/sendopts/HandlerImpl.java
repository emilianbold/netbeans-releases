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
package org.netbeans.modules.sendopts;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.api.sendopts.CommandLine;
import org.openide.util.NbBundle;

/**
 * Bridge between the CLIHandler that can be unit tested
 * @author Jaroslav Tulach
 */
final class HandlerImpl extends Object {
    static int execute(String[] arr, InputStream is, OutputStream os, OutputStream err, File pwd) {
        try {
            CommandLine.getDefault().process(
                arr, is, os, err, pwd
                );
            for (int i = 0; i < arr.length; i++) {
                arr[i] = null;
            }
            return 0;
        } catch (CommandException ex) {
            PrintStream ps = new PrintStream(err);
            ps.println(ex.getLocalizedMessage());
            // XXX pst is not useful, only in verbose mode
            // the question is how to turn that mode on
            // ex.printStackTrace(ps);
            return ex.getExitCode();
        }
    }
    
    static void usage(PrintWriter w) {
        w.print(NbBundle.getMessage(HandlerImpl.class, "MSG_OptionsHeader")); // NOI18N
        CommandLine.getDefault().usage(w);
        w.println();
    }
}
