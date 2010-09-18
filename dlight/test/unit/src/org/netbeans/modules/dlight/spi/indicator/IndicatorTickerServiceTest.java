/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.spi.indicator;

import java.text.MessageFormat;
import java.util.logging.LogRecord;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public class IndicatorTickerServiceTest {

    public IndicatorTickerServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        final Logger log = DLightLogger.getLogger(DLightExecutorService.class);
        log.setLevel(Level.ALL);
        log.addHandler(new Handler() {

            @Override
            public void publish(LogRecord record) {
                // Log if parent cannot log the message ONLY.
                if (!log.getParent().isLoggable(record.getLevel())) {
                    String message;
                    Object[] params = record.getParameters();
                    if (params == null || params.length == 0) {
                        message = record.getMessage();
                    } else {
                        message = MessageFormat.format(record.getMessage(), record.getParameters());
                    }
                    System.err.printf("%s: %s\n", record.getLevel(), message); // NOI18N
                    if (record.getThrown() != null) {
                        record.getThrown().printStackTrace(System.err);
                    }
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
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

    /**
     * Test of getInstance method, of class IndicatorTickerService.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        IndicatorTickerService expResult = IndicatorTickerService.getInstance();
        IndicatorTickerService result = IndicatorTickerService.getInstance();
        assertNotNull(result);
        assertEquals(expResult, result);
    }

    /**
     * Test of subsribe method, of class IndicatorTickerService.
     */
    @Test
    public void testService() {
        final AtomicInteger count1 = new AtomicInteger(0);
        final AtomicInteger count2 = new AtomicInteger(0);

        TickerListener l1 = new TickerListener() {

            @Override
            public void tick() {
                count1.incrementAndGet();
            }
        };

        TickerListener l2 = new TickerListener() {

            @Override
            public void tick() {
                count2.incrementAndGet();
            }
        };

        IndicatorTickerService instance = IndicatorTickerService.getInstance();
        instance.subsribe(l1);
        instance.subsribe(l2);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        IndicatorTickerService.getInstance().unsubscribe(l1);

        int a1 = count1.get();
        int a2 = count2.get();
        System.out.println("count1 == " + a1);
        System.out.println("count2 == " + a2);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        IndicatorTickerService.getInstance().unsubscribe(l2);

        int b1 = count1.get();
        int b2 = count2.get();
        System.out.println("count1 == " + b1);
        System.out.println("count2 == " + b2);
        assertTrue(b1 <= a1 + 1); // one extra tick is still possible
        assertTrue(b2 >= a2);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        IndicatorTickerService.getInstance().unsubscribe(l1);
        IndicatorTickerService.getInstance().unsubscribe(l2);

        int c1 = count1.get();
        int c2 = count2.get();
        System.out.println("count1 == " + c1);
        System.out.println("count2 == " + c2);
        assertTrue(c1 == b1);
        assertTrue(c2 == b2);

        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
