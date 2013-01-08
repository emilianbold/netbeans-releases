/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.output2;

import java.io.Reader;
import junit.framework.TestCase;
import org.openide.util.Exceptions;
import org.openide.windows.OutputWriter;

/**
 *
 * @author mkleint
 */
public class NbIOTest extends TestCase {

    public NbIOTest(String testName) {
        super(testName);
    }

    public void test54117() throws Exception {
        NbIO io = new NbIO("test");
        assertFalse(io.isClosed());
        Reader str = io.getIn();
        assertNotNull(str);
        assertEquals(NbIO.IOReader.class, str.getClass());
        writeText(str);
        int read = str.read(new char[100]);
        // not eof..
        assertTrue(read != -1);
        writeEof(str);
        read = str.read(new char[100]);
        assertTrue(read == -1);
        //reseting
        io.getOut().close();
        io.getErr().close();
        io.dispose();
        io.getOut().reset();
        io.getErr().reset();
        
        str = io.getIn();
        writeText(str);
        read = str.read(new char[100]);
        // not eof..
        assertTrue(read != -1);
        writeEof(str);
        read = str.read(new char[100]);
        assertTrue(read == -1);
        
    }
    
    private void writeText(final Reader reader) {
              NbIO.IOReader rdr = (NbIO.IOReader)reader;
              rdr.pushText("hello");

    }
    private void writeEof(final Reader reader) {
              NbIO.IOReader rdr = (NbIO.IOReader)reader;
              rdr.eof();
    }
    
    public void testSynchronization223370SeveralTimes() {
        for (int i = 0; i < 10; i++) {
            try {
                checkSynchronization223370();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void checkSynchronization223370() throws InterruptedException {
        final NbIO nbio = new NbIO("test223370");
        final int[] nullOuts = new int[1];
        final int[] nullIns = new int[1];
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    OutputWriter out = nbio.getOut();
                    Reader in = nbio.getIn();
                    if (out == null) {
                        synchronized (nullOuts) {
                            nullOuts[0]++;
                        }
                    }
                    if (in == null) {
                        synchronized (nullIns) {
                            nullIns[0]++;
                        }
                    }
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                for (int i = 0; i < 10000; i++) {
                    nbio.closeInputOutput();
                }
            }
        });
        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                for (int i = 0; i < 10000; i++) {
                    nbio.dispose();
                }
            }
        });

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();
        synchronized (nullIns) {
            assertEquals(0, nullIns[0]);
        }
        synchronized (nullOuts) {
            assertEquals(0, nullOuts[0]);
        }
    }
}
