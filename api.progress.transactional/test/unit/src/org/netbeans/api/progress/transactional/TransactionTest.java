
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.spi.progress.transactional.TransactionHandler;

/**
 *
 * @author Tim Boudreau
 */
public class TransactionTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        Logger.getLogger(TransactionHandler.class.getName()).setLevel(Level.ALL);
    }

    @Test
    public void testAdd() throws Exception {
        LoadPropertiesTransaction lpt = new LoadPropertiesTransaction();
        Transaction<Properties, File> cft = new WritePropertiesTransaction();
        Transaction<URL, File> copyFileTransaction = lpt.add(cft);
        TransactionLauncher<URL,File> l = copyFileTransaction.createLauncher("X");
        Future<? extends File> fu = l.launch(getClass().getResource("testproperties.properties"), UIMode.BACKGROUND);
        assertNotNull(fu);
        File f = fu.get();
        assertNotNull(f);
        System.out.println(f.getAbsolutePath());
        assertEquals(2, copyFileTransaction.contents().size());
        assertSame(lpt, copyFileTransaction.contents().get(0));
        assertSame(cft, copyFileTransaction.contents().get(1));
        assertEquals(2, copyFileTransaction.size());
        assertEquals(1, lpt.size());
        assertEquals(1, cft.size());
        assertEquals(2, copyFileTransaction.size());
        assertEquals(0, copyFileTransaction.indexOf(lpt));
        assertEquals(1, copyFileTransaction.indexOf(cft));
        Future<? extends Boolean> rb = l.rollback(FailureHandler.getDefault(), UIMode.BACKGROUND);
        assertNotNull(rb);
        Boolean val = rb.get();
        for (int i = 0; i < 10 && f.exists(); i++) {
            //Windows file system needs time to really delete it
            Thread.sleep(200);
        }
        assertFalse(f.exists());
        assertEquals(Boolean.TRUE, val);
        assertTrue(lpt.wasRolledBack);
    }

    @Test
    public void testNesting() throws Exception {
        One one = new One();
        Two two = new Two();
        Three three = new Three();
        Four four = new Four();
        Five five = new Five();
        Transaction<Void, Void> t = one.add(two.add(three.add(four.add(five))));
        assertEquals(5, t.size());
        assertEquals(0, t.indexOf(one));
        assertEquals(1, t.indexOf(two));
        assertEquals(2, t.indexOf(three));
        assertEquals(3, t.indexOf(four));
        assertEquals(4, t.indexOf(five));
    }

    @Test
    public void testNoRunsAfterSoftFail() throws Exception {
        One one = new One();
        Two two = new Two();
        Three three = new Three();
        Four four = new Four();
        Five five = new Five();
        Transaction<Void, Void> t = one.add(two.add(three.add(four.add(five))));
        four.failSoft = true;
        Future<? extends Void> f = t.launch(null, "Stuff", UIMode.BLOCKING);
        Future<? extends Object> x = f;
        f.get();
        assertTrue(one.ran());
        assertTrue(two.ran());
        assertTrue(three.ran());
        assertFalse(four.ran());
        assertFalse(five.ran());
        assertFalse(four.rolledBack());
        assertTrue(three.rolledBack());
        assertTrue(two.rolledBack());
        assertTrue(one.rolledBack());
    }

    @Test
    public void testNoRunsAfterHardFail() throws Exception {
        One one = new One();
        Two two = new Two();
        Three three = new Three();
        Four four = new Four();
        Five five = new Five();
        Transaction<Void, Void> t = one.add(two.add(three.add(four.add(five))));
        four.failHard = true;
        Future<? extends Void> f = t.launch(null, "Stuff", UIMode.BLOCKING);
        f.get();
        assertTrue(one.ran());
        assertTrue(two.ran());
        assertTrue(three.ran());
        assertFalse(four.ran());
        assertTrue(three.rolledBack());
        assertTrue(two.rolledBack());
        assertTrue(one.rolledBack());
        assertFalse(five.ran());
        assertFalse(four.rolledBack());
    }

    @Test
    public void testSeparateRollback() throws Exception {
        One one = new One();
        Two two = new Two();
        Three three = new Three();
        Four four = new Four();
        Five five = new Five();
        Transaction<Void, Void> t = one.add(two.add(three.add(four.add(five))));
        TransactionLauncher<Void, Void> r = t.createLauncher("X");
        Future<? extends Void> f = r.launch(null, UIMode.BACKGROUND);
        f.get();
        assertTrue(one.ran());
        assertTrue(two.ran());
        assertTrue(three.ran());
        assertTrue(four.ran());
        assertTrue(five.ran());
        Future<? extends Boolean> rb = r.rollback(UIMode.BACKGROUND);
        Boolean result = rb.get();
        assertTrue(one.rolledBack());
        assertTrue(two.rolledBack());
        assertTrue(three.rolledBack());
        assertTrue(four.rolledBack());
        assertTrue(five.rolledBack());
        assertTrue (result.booleanValue());
    }

    @Test
    public void testCancellation() throws Exception {
        One one = new One();
        Two two = new Two();
        Three three = new Three();
        Four four = new Four();
        Five five = new Five();
        Transaction<Void, Void> t = one.add(two.add(three.add(four.add(five))));
        three.blockInRun = true;
        Future<? extends Void> f = t.launch(null, "X", UIMode.BACKGROUND);
        three.waitForRun();
        f.cancel(false);
        three.releaseForRun();
        assertTrue(one.ran());
        assertTrue(two.ran());
        three.waitForRunExit();
        Thread.yield();
        assertTrue(three.ran());
        one.waitForRollback();
        assertFalse(four.ran());
        assertFalse(three.rolledBack());
        assertTrue(two.rolledBack());
        assertTrue(one.rolledBack());
    }

    @Test
    public void testNotRunTransactionsNotRolledBack() throws Exception {
        One one = new One();
        Two two = new Two();
        Three three = new Three();
        Four four = new Four();
        Five five = new Five();

        four.failHard = true;
        two.failRollbackHard = true;

        Transaction<Void, Void> t = one.add(two.add(three.add(four.add(five))));
        Future<? extends Void> f = t.launch(null, "X", UIMode.BACKGROUND);
        f.get();
        assertTrue (one.ran());
        assertTrue (two.ran());
        assertTrue (three.ran());
        assertFalse (four.ran());
        assertTrue (three.rolledBack());
        assertFalse (two.rolledBack());
        assertTrue (one.rolledBack());
        assertFalse (five.ran());
        assertFalse (five.rolledBack());
        assertTrue(one.controller.get().failed());
    }

    @Test
    public void testParallelTransactions() throws InterruptedException, ExecutionException {
        String in = "123456789";
        String out = "987654321";
        ReverseStringTransaction r1 = new ReverseStringTransaction();
        ReverseStringTransaction r2 = new ReverseStringTransaction();
        Transaction<String,String> aX = r1.add(r1.add(r1));
        Transaction<String,String> bX = r2.add(r2.add(r2));
        ParallelTransaction<String,String,String,String> t = new ParallelTransaction<String,String,String,String>("x", aX, bX);
        assertNotNull (t);
        Future<? extends ParallelValue<String,String>> f = t.launch(new ParallelValue<String,String>(in, in), "Parallel Reverse Strings", UIMode.BACKGROUND);
        ParallelValue<String,String> results = f.get();
        assertNotSame (r1.thread, r2.thread);
        assertNotNull (results.a());
        assertNotNull (results.b());
        assertEquals (out, results.a());
        assertEquals (out, results.b());
        assertEquals (3, r1.runCount.get());
        assertEquals (3, r2.runCount.get());
        assertEquals (3, aX.size());
        assertEquals (3, bX.size());
        assertEquals (3, t.size());
        assertEquals (3, t.a().size());
        assertEquals (3, t.b().size());
    }

    @Test
    public void testCancellingParallelTransactionOneTransactionCancelsBoth() throws Exception {
        One oneA = new One();
        Two twoA = new Two();
        Three threeA = new Three();
        Four fourA = new Four();
        Five fiveA = new Five();

        One oneB = new One();
        Two twoB = new Two();
        Three threeB = new Three();
        Four fourB = new Four();
        Five fiveB = new Five();

        Transaction<Void, Void> a = oneA.add(twoA.add(threeA.add(fourA.add(fiveA))));
        Transaction<Void, Void> b = oneB.add(twoB.add(threeB.add(fourB.add(fiveB))));
        Transaction<ParallelValue<Void,Void>,ParallelValue<Void,Void>> t = Transaction.createParallelTransaction(a, b);

        threeB.blockInRun = true;
        threeA.blockInRun = true;
        Future<?> f = t.launch(null, "Foo", UIMode.NONE);

        threeB.waitForRun();
        twoA.waitForPreRun();
        f.cancel(true);
        threeA.releaseForRun();
        int ct = 0;
        while (ct++ < 50 && !oneA.controller.get().inRollback()) {
            Thread.sleep(100);
            System.err.println("Check controller " + oneA.controller.get().inRollback() + " b controller " + oneB.controller.get().inRollback());
        }
        threeB.releaseForRun();
        while (ct++ < 50 && !oneB.controller.get().inRollback()) {
            Thread.sleep(100);
            System.err.println("Check B controller " + oneA.controller.get().inRollback() + " b controller " + oneB.controller.get().inRollback());
        }
        f.get();

        assertTrue (oneA.ran());
        assertTrue (twoA.ran());
        assertTrue (oneB.ran());
        assertTrue (twoB.ran());

        assertTrue (oneA.rolledBack());
        assertTrue (twoA.rolledBack());
        assertTrue (oneB.rolledBack());
        assertTrue (twoB.rolledBack());
    }

    @Test
    public void testParallelContains () throws Exception {
        AtomicInteger in = new AtomicInteger();
        Transaction<Void, Void> x = new X(in);
        Transaction<Void, Void> y = new Y(in);
        Set<Transaction<Void,Void>> xs = new HashSet<Transaction<Void,Void>>();
        Set<Transaction<Void,Void>> ys = new HashSet<Transaction<Void,Void>>();
        for (int i = 0; i < 13; i++) {
            Y yy = new Y(in);
            X xx = new X(in);
            xx.add(x);
            x = xx;
            yy.add(y);
            y = yy;
        }
        System.err.println("X is " + x);
        System.err.println("Y is " + y);
        System.err.println("X size " + x.size());
        System.err.println("Y size " + y.size());

        ParallelTransaction pt = new ParallelTransaction("Boo", x, y);
        for (Transaction<Void, Void> transaction : ys) {
            assertTrue (transaction + " missing from " + y, y.indexOf(transaction) >= 0);
            assertTrue (transaction + " missing from " + pt, pt.indexOf(transaction) >= 0);
        }
        for (Transaction<Void, Void> transaction : xs) {
            assertTrue (transaction + " missing from " + x, x.indexOf(transaction) >= 0);
            assertTrue (transaction + " missing from " + pt, pt.indexOf(transaction) >= 0);
        }
        assertTrue (x + " missing from self ", x.indexOf(x) >= 0);
        assertTrue (y + " missing from self ", y.indexOf(y) >= 0);
        assertTrue (x + " missing from " + pt, pt.indexOf(x) >= 0);
        assertTrue (y + " missing from " + pt, pt.indexOf(y) >= 0);
    }

    @Test
    public void testAddWorks() throws Exception {
        AtomicInteger i = new AtomicInteger();
        Transaction<Void, Void> v1 = new X(i).add(new X(i).add(new X(i).add(new X(i).add(new X(i).add(new X(i).add(new X(i).add(new X(i))))))));
        Transaction<Void, Void> v2 = new X(i).add(new X(i).add(new X(i).add(new X(i).add(new X(i).add(new X(i).add(new X(i).add(new X(i))))))));
        for (Transaction<?,?> t : v1.contents()) {
            assertTrue (v1.contains(t));
            assertFalse (-1 == v1.indexOf(t));
        }
        Transaction<?,?> pt = new ParallelTransaction<Void,Void,Void,Void>("Boo", v1, v2);
        for (Transaction<?,?> t : v1.contents()) {
            assertTrue (pt.contains(t));
            assertFalse (-1 == pt.indexOf(t));
        }
        for (Transaction<?,?> t : v1.contents()) {
            assertFalse (t + " has -1 index in " + pt.contents() + " of " + pt,-1 == pt.indexOf(t));
            assertTrue (t + " not found in " + pt.contents() + " of " + pt, pt.contains(t));
        }
        for (Transaction<?,?> t : v2.contents()) {
            assertFalse (t + " has -1 index in " + pt.contents() + " of " + pt,-1 == pt.indexOf(t));
            assertTrue (t + " not found in " + pt.contents() + " of " + pt, pt.contains(t));
        }
        for (Transaction<?,?> t : pt.contents()) {
            assertTrue (pt.contains(t));
            assertFalse (-1 == pt.indexOf(t));
        }
        assertTrue (pt.contains(v1));
        assertTrue (pt.contains(v2));
    }

    @Test
    public void testFailingOneParallelTransactionFailsBoth() throws Exception {
        One oneA = new One("oneA");
        Two twoA = new Two("twoA");
        Three threeA = new Three("threeA");
        Four fourA = new Four("fourA");
        Five fiveA = new Five("fiveA");

        One oneB = new One("oneB");
        Two twoB = new Two("twoB");
        Three threeB = new Three("threeB");
        Four fourB = new Four("fourB");
        Five fiveB = new Five("fiveB");

        Transaction<Void, Void> a = oneA.add(twoA.add(threeA.add(fourA.add(fiveA))));
        Transaction<Void, Void> b = oneB.add(twoB.add(threeB.add(fourB.add(fiveB))));
        Transaction<ParallelValue<Void,Void>,ParallelValue<Void,Void>> t = Transaction.createParallelTransaction(a, b);

        fourA.failHard = true;
        threeA.blockInRun = true;
        threeB.blockInRun = true;
        Future<?> f = t.launch(null, "Foo", UIMode.NONE);

        fourA.waitForPreRun();
        threeB.waitForPreRun();
        threeA.releaseForRun();
        threeB.releaseForRun();
        oneA.waitForRollback(3000);
        oneB.waitForRollback(3000);
        f.get();

        assertTrue (oneA.ran());
        assertTrue (twoA.ran());
        assertTrue (threeA.ran());
        assertTrue (oneB.ran());
        assertTrue (twoB.ran());
        assertTrue (threeB.ran());

        assertTrue (oneA.rolledBack());
        assertTrue (twoA.rolledBack());
        assertTrue (threeA.rolledBack());
        assertTrue (oneB.rolledBack());
        assertTrue (twoB.rolledBack());
        assertTrue (threeB.rolledBack());
    }

    private static final class X extends Transaction<Void,Void> {

        X(AtomicInteger in) {
            super (X.class.getSimpleName() + (in.incrementAndGet()), Void.class, Void.class);
        }

        @Override
        protected Void run(TransactionController controller, Void argument) throws TransactionException {
            return null;
        }
    }


    private static final class Y extends Transaction<Void,Void> {

        Y(AtomicInteger in) {
            super (Y.class.getSimpleName() + (in.incrementAndGet()), Void.class, Void.class);
        }

        @Override
        protected Void run(TransactionController controller, Void argument) throws TransactionException {
            return null;
        }
    }


    private static final class LoadPropertiesTransaction extends Transaction<URL, Properties> {

        boolean wasRolledBack;

        LoadPropertiesTransaction() {
            super("Load properties", URL.class, Properties.class);
        }

        @Override
        protected Properties run(TransactionController controller, URL argument) {
            Properties result = new Properties();
            try {
                result.load(new BufferedInputStream(argument.openStream()));
            } catch (IOException ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
            return result;
        }

        @Override
        protected boolean rollback(TransactionController controller, URL argument, Properties resultType) {
            wasRolledBack = true;
            return true;
        }
    }

    private static final class WritePropertiesTransaction extends Transaction<Properties, File> {

        private String path;

        WritePropertiesTransaction() {
            super("Write properties", Properties.class, File.class);
        }

        @Override
        protected File run(TransactionController controller, Properties props) {
            try {
                File result = File.createTempFile("props" + System.currentTimeMillis(), ".properties");
                OutputStream out = new FileOutputStream(result);
                try {
                    props.store(out, path);
                } finally {
                    out.close();
                }
                return result;
            } catch (IOException ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        }

        @Override
        protected boolean rollback(TransactionController controller, Properties argument, File file) throws TransactionException {
            if (file != null && file.exists()) {
                if (!file.delete()) {
                    throw new TransactionException("Could not delete " + file.getAbsolutePath());
                }
            }
            return true;
        }
    }
    static TransactionController controller;

    private static class One extends Transaction<Void, Void> {

        volatile boolean failHard;
        volatile boolean failSoft;
        volatile boolean failRollbackSoft;
        volatile boolean failRollbackHard;
        volatile AtomicBoolean rolledback = new AtomicBoolean(false);
        volatile AtomicBoolean ran = new AtomicBoolean(false);
        private CountDownLatch runLatch = new CountDownLatch(1);
        private CountDownLatch runExitLatch = new CountDownLatch(1);
        private CountDownLatch rollbackLatch = new CountDownLatch(1);
        private CountDownLatch preRunLatch = new CountDownLatch(1);
        final CountDownLatch runLock = new CountDownLatch(1);
        final CountDownLatch rollbackLock = new CountDownLatch(1);
        volatile boolean blockInRun;
        volatile boolean blockInRollback;
        AtomicReference<TransactionController> controller = new AtomicReference<TransactionController>();
        One() {
            this(null);
        }
        private final String name;
        One(String name) {
            super(name == null ? "X" : name, Void.class, Void.class);
            this.name = name;
        }

        void waitForRun() throws InterruptedException {
            System.err.println("WaitForRun " + getClass().getSimpleName()  + " on " + Thread.currentThread() + " - " + name);
            runLatch.await();
        }

        void waitForRunExit() throws InterruptedException {
            System.err.println("WaitForRunExit " + getClass().getSimpleName()  + " on " + Thread.currentThread() + " - " + name);
            runExitLatch.await();
        }

        void waitForRollback() throws InterruptedException {
            System.err.println("WaitForRollback " + getClass().getSimpleName()  + " on " + Thread.currentThread() + " - " + name);
            rollbackLatch.await();
        }

        void waitForRollback(long time) throws InterruptedException {
            System.err.println("WaitForRollback - timed - " + getClass().getSimpleName()  + " on " + Thread.currentThread() + " - " + name);
            rollbackLatch.await(time, TimeUnit.MILLISECONDS);
        }

        void waitForPreRun() throws InterruptedException {
            System.err.println("WaitForPreRun " + getClass().getSimpleName()  + " on " + Thread.currentThread() + " - " + name);
            preRunLatch.await();
        }

        void waitForPreRun(long ms) throws InterruptedException {
            System.err.println("WaitForPreRun - timed" + getClass().getSimpleName()  + " on " + Thread.currentThread() + " - " + name);
            preRunLatch.await(ms, TimeUnit.MILLISECONDS);
        }

        void releaseForRun() {
            runLock.countDown();
        }

        void releaseForRollback() {
            rollbackLock.countDown();
        }

        @Override
        protected Void run(TransactionController controller, Void argument) throws TransactionException {
            System.err.println("Run " + getClass().getSimpleName()  + " on " + Thread.currentThread() + " - " + name);
            this.controller.set(controller);
            preRunLatch.countDown();
            System.err.println("Post-preRun " + getClass().getSimpleName()  + " on " + Thread.currentThread() + " - " + name);
            if (failHard) {
                throw new IllegalStateException("Fail hard");
            }
            if (failSoft) {
                throw new TransactionException("Fail mid");
            }
            runLatch.countDown();
            System.err.println("Released for run "+ getClass().getSimpleName()  + " on " + Thread.currentThread() + " - " + name);
            if (blockInRun) {
                try {
                    runLock.await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            ran.set(true);
            runExitLatch.countDown();
            System.err.println("Run Exit "+ getClass().getSimpleName()  + " on " + Thread.currentThread() + " - " + name);
//            System.err.println("Exit run " + getClass().getSimpleName() + " ran=" + ran  + " on " + " on " + Thread.currentThread());
            return null;
        }

        boolean ran() {
            return ran.get();
        }

        boolean rolledBack() {
            return rolledback.get();
        }

        @Override
        protected boolean rollback(TransactionController controller, Void argument, Void prevResult) throws TransactionException {
            System.err.println("Rollback " + getClass().getSimpleName() + " on " + Thread.currentThread() + " - " + name);
            if (failRollbackHard) {
                throw new IllegalStateException("Fail hard");
            }
            if (failRollbackSoft) {
                throw new TransactionException("Fail mid");
            }
            rollbackLatch.countDown();
            if (blockInRollback) {
                try {
                    rollbackLock.await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            rolledback.set(true);
//            System.err.println("Exit rollback " + getClass().getSimpleName() + " rolledback=" + rolledback + " on " + Thread.currentThread());
            return !controller.failed();
        }
    }

    private static class Two extends One {
        Two(String name) {
            super (name);
        }
        Two() {
            super (null);
        }
    }

    private static class Three extends One {
        Three(String name) {
            super (name);
        }
        Three() {
            super (null);
        }
    }

    private static class Four extends One {
        Four(String name) {
            super (name);
        }
        Four() {
            super (null);
        }

    }

    private static class Five extends One {
        Five(String name) {
            super (name);
        }
        Five() {
            super (null);
        }
    }

    private static final class ReverseStringTransaction extends Transaction<String,String> {
        AtomicInteger runCount = new AtomicInteger();
        AtomicReference<Thread> thread = new AtomicReference<Thread>();
        ReverseStringTransaction() {
            super ("Reverse string", String.class, String.class);
        }

        @Override
        protected String run(TransactionController controller, String argument) throws TransactionException {
            thread.set(Thread.currentThread());
            runCount.incrementAndGet();
            return new StringBuilder(argument).reverse().toString();
        }
    }
}
