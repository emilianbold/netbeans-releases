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
import java.io.PrintStream;
import java.io.PrintWriter;
import org.netbeans.CLIHandler;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.modules.sendopts.*;
import org.openide.util.Lookup;

/**
 * A CLI handler to delegate to CommandLine.getDefault().
 * @author Jaroslav Tulach
 */
public class Handler extends CLIHandler {
    /**
     * Create a handler. Called by core.
     */
    public Handler() {
        super(WHEN_EXTRA);
    }

    protected int cli(CLIHandler.Args args) {
        return HandlerImpl.execute(
            args.getArguments(),
            args.getInputStream(),
            args.getOutputStream(),
            args.getErrorStream(),
            args.getCurrentDirectory()
        );
    }

    protected void usage(PrintWriter w) {
        HandlerImpl.usage(w);
    }
}
