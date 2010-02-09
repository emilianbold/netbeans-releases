/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.api.progress.transactional;

import java.util.concurrent.CountDownLatch;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Administrator
 */
public class OncePerThreadBooleanTest {

    public OncePerThreadBooleanTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    private OncePerThreadBoolean b;
    @Before
    public void setUp() {
        b = new OncePerThreadBoolean();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testClear() {
        b.set();
        b.clear();
        assertFalse(b.get());
    }

    @Test
    public void testSet() {
        b.set();
        assertTrue(b.get());
    }

    @Test
    public void testGet() {
        b.set();
        assertTrue(b.get());
        assertFalse(b.get());
        assertFalse(b.get());
        b.set();
        assertFalse(b.get());
        assertFalse(b.get());
        b.clear();
        assertFalse(b.get());
        b.set();
        b.set();
        assertTrue(b.get());
        assertFalse(b.get());
        assertFalse(b.get());
    }


    private static final int COUNT = 30;
    @Test
    public void testMultiThreadAccess() throws InterruptedException {
        b.set();
        final CountDownLatch latch = new CountDownLatch(COUNT);
        final CountDownLatch startLatch = new CountDownLatch(COUNT);
        class R implements Runnable {
            final int ix;
            R(int ix) {
                this.ix = ix;
            }

            @Override
            public String toString() {
                return "Run " + ix;
            }
            volatile boolean hasRun;
            volatile boolean firstResult;
            volatile boolean secondResult;
            volatile boolean thirdResult;
            volatile boolean fourthResult;
            @Override
            public void run() {
                startLatch.countDown();
                hasRun = true;
                firstResult = b.get();
                secondResult = b.get();
                thirdResult = b.get();
                b.set();
                latch.countDown();
            }

            void assertOk() {
                assertTrue (this + " did not run", hasRun);
                assertTrue (this + " did get true on first call on its thread", firstResult);
                assertFalse (this + " did get false on second call on its thread", secondResult);
                assertFalse (this + " did get false on third call on its thread", thirdResult);
                assertFalse (this + " call to set() from thread that already called get should not reset value to true", fourthResult);
            }
        }
        Set<R> runs = new HashSet<R>();
        for (int i= 0; i < COUNT; i++) {
            R r = new R(i);
            Thread t = new Thread(r);
            runs.add(r);
            t.start();
        }
        
        latch.await();
        for (R r : runs) {
            r.assertOk();
        }
    }
}