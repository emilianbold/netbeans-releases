/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.perfan.storage.impl;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class ErprintSessionTest {

    public ErprintSessionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        String dirs = System.getProperty("netbeans.dirs", ""); // NOI18N
        System.setProperty("netbeans.dirs", "/export/home/ak119685/netbeans-src/main/nbbuild/netbeans/dlight1:" + dirs);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFake() {
        
    }
    /**
     * Test of restart method, of class ErprintSession.
     */
//    @Test
    public void setMetricsTest() throws Exception {
        final ErprintSession session = ErprintSession.createNew(ExecutionEnvironmentFactory.getLocal(),
                "/", "/var/tmp/dlightExperiment_31.er/", null);
        String[] funcs = session.getHotFunctions(null, 10, 0, false);
        for (String f : funcs) {
            System.out.println(f);
        }
        session.close();
    }

//    @Test
    public void testGetExperimentStatistics() throws Exception {
        final ErprintSession session = ErprintSession.createNew(ExecutionEnvironmentFactory.getLocal(),
                "/", "/var/tmp/dlightExperiment_31.er/", null);
        int threadsNum = 20;

        final CyclicBarrier startSignal = new CyclicBarrier(threadsNum + 1);
        final CountDownLatch doneSignal = new CountDownLatch(threadsNum);

        Thread[] threads = new Thread[threadsNum];
        final AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < threadsNum; i++) {
            threads[i] = new Thread(new Runnable() {

                final int id = counter.incrementAndGet();

                public void run() {
                    try {
                        startSignal.await();
                    } catch (BrokenBarrierException ex) {
                        ex.printStackTrace(System.err);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace(System.err);
                    }

                    Metrics m = new Metrics("e.user:name", "e.user");
                    
                    try {
                        ExperimentStatistics stat = null;
                        try {
                            for (int j = 0; j < 5; j++) {
                                String[] funcs = session.getHotFunctions(m, 10, 0, j%3 == 0);
                                for (String f : funcs) {
                                    System.out.println(f);
                                }
//                                stat = session.getExperimentStatistcs(j % 3 == 0);
//                                stat.dump();
//                                session.getExperimentLeaks(j % 2 == 0);
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } finally {
                        doneSignal.countDown();
                    }
                }
            });

            threads[i].start();
        }

        try {
            startSignal.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace(System.err);
        } catch (BrokenBarrierException ex) {
            ex.printStackTrace(System.err);
        }

        try {
            doneSignal.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace(System.err);
        }

        session.close();
        System.out.println("Done");
    }
}