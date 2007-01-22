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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.uihandler.LogRecords;

/**
 *
 * @author Jaroslav Tulach
 */
public class ReadBigDataTest extends NbTestCase {
    private Logger LOG;
    
    public ReadBigDataTest(String testName) {
        super(testName);
    }
    
    protected Level logLevel() {
        return null; //Level.FINEST;
    }

    protected void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
    }

    protected void tearDown() throws Exception {
    }

    public void testWriteAndRead() throws Exception {
        File dir = new File(new File(System.getProperty("user.dir")), "ui");
        if (!dir.exists()) {
            return;
        }

        File[] arr = dir.listFiles();
        if (arr == null) {
            return;
        }
        
        int[] cnts = new int[arr.length];
        int err1 = readAsAStream(cnts, arr, 0);
        int err2 = readAsSAX(cnts, 0, arr);
        
        assertEquals("No errors: " + err1 + " and no " + err2, 0, err1 + err2);
    }

    private int readAsSAX(final int[] cnts, int err, final File[] arr) throws IOException, FileNotFoundException {
        class H extends Handler {
            int cnt;

            public void publish(LogRecord record) {
                cnt++;
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        
        int i = -1;
        for (File f : arr) {
            LOG.log(Level.WARNING, "scanning {0}", f.getPath());
            i++;
            InputStream is = new BufferedInputStream(new FileInputStream(f));
            H h = new H();
            try {
                LogRecords.scan(is, h);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
                err++;
                continue;
            } finally {
                is.close();
            }
            
            assertEquals("The same amount for " + f, h.cnt, cnts[i]);
        }
        return err;
    }

    private int readAsAStream(final int[] cnts, final File[] arr, int err) throws IOException, FileNotFoundException {
        int i = -1;
        for (File f : arr) {
            LOG.log(Level.WARNING, "reading {0}", f.getPath());
            i++;
            InputStream is = new BufferedInputStream(new FileInputStream(f));
            int cnt = 0;
            try {
                while (LogRecords.read(is) != null) {
                    cnt++;
                }
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
                err++;
                continue;
            } finally {
                cnts[i] = cnt;
                is.close();
            }
            is.close();
        }
        return err;
    }
}
