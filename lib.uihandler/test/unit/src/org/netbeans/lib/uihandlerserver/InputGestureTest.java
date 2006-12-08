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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.netbeans.lib.uihandlerserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.uihandler.LogRecords;

/**
 *
 * @author Jaroslav Tulach
 */
public class InputGestureTest extends NbTestCase {
    private Logger LOG;
    
    public InputGestureTest(String testName) {
        super(testName);
    }
    
    protected Level logLevel() {
        return Level.INFO;
    }
    
    protected int timeOut() {
        return 0; //5000;
    }

    protected void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
    }

    protected void tearDown() throws Exception {
    }

    public void testReadALogAndTestInputGestures() throws Exception {
        InputStream is = getClass().getResourceAsStream("NB1216449736.0");
        int cnt = 0;
        for (;;) {
            LOG.log(Level.INFO, "Reading {0}th record", cnt);
            LogRecord r = LogRecords.read(is);
            if (r == null) {
                break;
            }
            LOG.log(Level.INFO, "Read {0}th record", cnt);
            cnt++;
        }
        is.close();
    }
}
