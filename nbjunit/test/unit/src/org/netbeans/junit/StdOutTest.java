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

package org.netbeans.junit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Behaviour of logging with respect to console output.
 *
 * @author Jaroslav Tulach
 */
public class StdOutTest extends NbTestCase {
    static ByteArrayOutputStream os = new ByteArrayOutputStream();
    static {
        PrintStream ps = new PrintStream(os);
        System.setErr(ps);
        System.setOut(ps);
    }

    private Logger log;
    
    public StdOutTest(String testName) {
        super(testName);
    }

    protected Level logLevel() {
        String n = getName();
        int inx = n.indexOf("Level");
        if (inx == -1) {
            return null;
        }
        return Level.parse(n.substring(inx + 5));
    }
    
    protected void setUp() throws Exception {
        os.reset();

        log = Logger.getLogger(getName());
    }


    public void testNullLoggingPrintsToConsole() {
        log.warning("Ahoj");
        if (os.toString().indexOf("Ahoj") == -1) {
            fail("Should log: " + os);
        }
    }
    public void testNoConsoleOnLevelWARNING() {
        log.warning("Ahoj");
        if (os.toString().indexOf("Ahoj") != -1) {
            fail("Should log: " + os);
        }
    }
}

