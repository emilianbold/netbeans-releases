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

package org.netbeans;

import java.io.*;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import java.util.*;
import junit.framework.AssertionFailedError;
import org.openide.util.RequestProcessor;

/**
 * Test the command-line-interface handler.
 * @author Jaroslav Tulach
 */
public class CLIHandlerRemembersSystemInOutErrTest extends NbTestCase {
    
    private static ByteArrayInputStream in = new ByteArrayInputStream("Ahoj".getBytes());
    private static PrintStream out = new PrintStream(new ByteArrayOutputStream());
    private static PrintStream err = new PrintStream(new ByteArrayOutputStream());
    private static String[] args = { "AnArg" };
    private static String curDir = "curDir";

    static {
        System.setIn(in);
        System.setErr(err);
        System.setOut(out);
    }
    
    public CLIHandlerRemembersSystemInOutErrTest(String name) {
        super(name);
    }
    
    public void testFileExistsButItCannotBeRead() throws Exception {
        // just initialize the CLIHandler
        CLIHandler.Args a = new CLIHandler.Args(args, in, out, err, curDir);

        ArrayList arr = new ArrayList();
        arr.add(new H(H.WHEN_BOOT));
        arr.add(new H(H.WHEN_INIT));



        // now change the System values
        ByteArrayInputStream in2 = new ByteArrayInputStream("NeverBeSeen".getBytes());
        PrintStream out2 = new PrintStream(new ByteArrayOutputStream());
        PrintStream err2 = new PrintStream(new ByteArrayOutputStream());

        System.setIn(in2);
        System.setErr(err2);
        System.setOut(out2);


        CLIHandler.initialize(a, null, arr, false, true, null);
        assertEquals("One H called", 1, H.cnt);
        CLIHandler.finishInitialization(false);
        assertEquals("Both Hs called", 2, H.cnt);
    }

    private static final class H extends CLIHandler {
        static int cnt;

        public H(int w) {
            super(w);
        }

        protected int cli(CLIHandler.Args a) {
            cnt++;

            assertEquals("Same arg", Arrays.asList(args), Arrays.asList(a.getArguments()));
            assertEquals("same dir", curDir, a.getCurrentDirectory().toString());
            assertEquals("same in", in, a.getInputStream());
            assertEquals("same out", out, a.getOutputStream());
            assertEquals("same err", err, a.getErrorStream());

            return 0;
        }

        protected void usage(PrintWriter w) {
        }
    }
}
