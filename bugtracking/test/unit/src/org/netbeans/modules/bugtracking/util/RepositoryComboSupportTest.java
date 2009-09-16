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

package org.netbeans.modules.bugtracking.util;

import java.awt.EventQueue;
import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.bugtracking.dummies.DummyBugtrackingConnector;
import org.netbeans.modules.bugtracking.dummies.DummyKenaiRepositories;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.RepositoryComboSupport.Progress;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import static org.junit.Assert.*;
import static org.netbeans.modules.bugtracking.util.RepositoryComboSupportTest.ThreadType.AWT;
import static org.netbeans.modules.bugtracking.util.RepositoryComboSupportTest.ThreadType.NON_AWT;

/**
 *
 * @author Marian Petras
 */
public class RepositoryComboSupportTest {

    private volatile JComboBox comboBox;
    private volatile RepositoryComboSupport comboSupport;

    @BeforeClass
    public static void setLookup() {
        System.setProperty("org.openide.util.Lookup", TestLookup.class.getName());
    }

    @Before
    public void createJComboBox() {
        comboBox = new JComboBox();
    }

    @After
    public void tidyUp() {
        comboBox = null;
        comboSupport = null;
        Lookup.getDefault().lookup(DummyBugtrackingConnector.class).reset();
    }

    abstract class AbstractRepositoryComboTest {

        protected final DummyBugtrackingConnector connector = Lookup.getDefault().lookup(DummyBugtrackingConnector.class);

        protected Repository repository1;
        protected Repository repository2;
        protected Repository repository3;

        protected void setUp() {
            //the default implementation does nothing
        }

        protected void createRepositories() {
            repository1 = connector.createRepository("alpha");
            repository2 = connector.createRepository("beta");
            repository3 = connector.createRepository("gamma");
        }

        abstract RepositoryComboSupport setupComboSupport(JComboBox comboBox);

        protected void scheduleTests(ProgressTester progressTester) {
            progressTester.scheduleTest          (Progress.STARTED, AWT,
                                                    new ComboBoxItemsTest(
                                                            RepositoryComboSupport.LOADING_REPOSITORIES));
            progressTester.scheduleTest          (Progress.WILL_LOAD_REPOS, NON_AWT);
            progressTester.scheduleTest          (Progress.LOADED_REPOS, NON_AWT);
            progressTester.scheduleTest          (Progress.WILL_SCHEDULE_DISPLAY_OF_REPOS, NON_AWT);
            progressTester.scheduleSuspendingTest(Progress.SCHEDULED_DISPLAY_OF_REPOS, NON_AWT);
        }

    }

    abstract class NoRepositoryComboTest extends AbstractRepositoryComboTest {
        @Override
        protected void createRepositories() {
            //do not create any repository
        }
        @Override
        protected void scheduleTests(ProgressTester progressTester) {
            super.scheduleTests(progressTester);
            progressTester.scheduleTest          (Progress.WILL_DISPLAY_REPOS, AWT);
            progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                    new ComboBoxItemsTest(
                                                            RepositoryComboSupport.NO_REPOSITORIES),
                                                    new SelectedItemTest(
                                                            RepositoryComboSupport.NO_REPOSITORIES));
        }
    }

    @Test
    public void testNoRepositoryAvailableTrue() throws InterruptedException {
        runRepositoryComboTest(new NoRepositoryComboTest() {
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, true);
            }
        });
    }

    @Test
    public void testNoRepositoryAvailableFalse() throws InterruptedException {
        runRepositoryComboTest(new NoRepositoryComboTest() {
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, false);
            }
        });
    }

    @Test
    public void testNoRepositoryAvailableRefFileGiven() throws InterruptedException {
        runRepositoryComboTest(new NoRepositoryComboTest() {
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox,
                                                    new File(System.getProperty("java.home")));
            }
        });
    }

    @Test
    public void testDefaultRepoNotSet() throws InterruptedException {
        runRepositoryComboTest(new AbstractRepositoryComboTest() {
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, true);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleTest          (Progress.WILL_DISPLAY_REPOS, AWT);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                RepositoryComboSupport.SELECT_REPOSITORY,
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                RepositoryComboSupport.SELECT_REPOSITORY));
            }
        });
    }

    @Test
    public void testDefaultRepoExplicitlySet() throws InterruptedException {
        runRepositoryComboTest(new AbstractRepositoryComboTest() {
            @Override
            RepositoryComboSupport setupComboSupport(JComboBox comboBox) {
                return RepositoryComboSupport.setup(null, comboBox, repository2);
            }
            @Override
            protected void scheduleTests(ProgressTester progressTester) {
                super.scheduleTests(progressTester);
                progressTester.scheduleTest          (Progress.WILL_DISPLAY_REPOS, AWT);
                progressTester.scheduleResumingTest  (Progress.DISPLAYED_REPOS, AWT,
                                                        new ComboBoxItemsTest(
                                                                repository1,
                                                                repository2,
                                                                repository3),
                                                        new SelectedItemTest(
                                                                repository2));
            }
        });
    }

    private void runRepositoryComboTest(AbstractRepositoryComboTest test)
                                                throws InterruptedException {
        test.createRepositories();
        comboSupport = test.setupComboSupport(comboBox);
        assertNotNull(comboSupport);
        assertSame(Progress.INITIALIZED, comboSupport.getProgress());

        final Object testLock = new Object();
        ProgressTester progressListener = new ProgressTester(testLock);
        test.scheduleTests(progressListener);
        progressListener.startListening();
        synchronized (testLock) {
            /*
             * testLock.notify() in ProgressTester must not be called
             * before testLock.wait() here - so we must schedule start
             * of the RepositoryComboSupport while we are holding the testLock,
             * i.e. in the synchronized block.
             */
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    comboSupport.start();
                }
            });
            testLock.wait();
        }

        if (progressListener.failure != null) {
            if (progressListener.failure instanceof Error) {
                throw (Error) progressListener.failure;
            }
            if (progressListener.failure instanceof RuntimeException) {
                throw (RuntimeException) progressListener.failure;
            }
            assert false;
            fail(progressListener.failure.getClass().getName() + ": "
                 + progressListener.failure.getMessage());
        }
    }

    public static final class TestLookup extends AbstractLookup {
        public TestLookup() {
            this(new InstanceContent());
        }
        private TestLookup(InstanceContent ic) {
            super(ic);
            ic.add(new DummyKenaiRepositories());
            ic.add(new DummyBugtrackingConnector());
        }
    }

    static enum ThreadType {
        AWT(true),
        NON_AWT(false);

        private final boolean boolValue;
        ThreadType(boolean boolValue) {
            this.boolValue = boolValue;
        }
        boolean booleanValue() {
            return boolValue;
        }
        static ThreadType forBoolean(boolean booleanValue) {
            return (booleanValue == AWT.boolValue) ? AWT : NON_AWT;
        }
    }

    final class ComboBoxItemsTest implements Runnable {

        private final Object[] expectedItems;

        ComboBoxItemsTest(Object... expectedItems) {
            this.expectedItems = expectedItems;
        }

        public void run() {
            System.out.println("Comparing combo-box items.");
            Object[] actualItems = getActualItems();
            assertArrayEquals("Different content of the combo-box expected",
                              expectedItems, actualItems);
        }

        private Object[] getActualItems() {
            Object[] result = new Object[comboBox.getItemCount()];
            for (int i = 0; i < result.length; i++) {
                result[i] = comboBox.getItemAt(i);
            }
            return result;
        }

    }

    final class SelectedItemTest implements Runnable {

        private final Object expectedSelectedItem;

        SelectedItemTest(Object expectedSelectedItem) {
            this.expectedSelectedItem = expectedSelectedItem;
        }

        public void run() {
            System.out.println("Checking selected combo-box item.");
            assertSame("Different item should be selected in the combo-box",
                       expectedSelectedItem, comboBox.getSelectedItem());
        }

    }

    static final class ProgressTest {
        static final Boolean SUSPENDING = Boolean.TRUE;
        static final Boolean RESUMING = Boolean.FALSE;
        private final Progress progressState;
        private final ThreadType threadType;
        private final Runnable[] additionalTests;
        private final Boolean synchrType;
        private final StackTraceElement[] callStack;
        ProgressTest(Progress progressState,
                     ThreadType threadType) {
            this(progressState, threadType, (Runnable[]) null);
        }
        ProgressTest(Progress progressState,
                     ThreadType threadType,
                     Runnable... additionalTests) {
            this(null, progressState, threadType, additionalTests);
        }
        ProgressTest(Boolean synchronizationType,
                     Progress progressState,
                     ThreadType threadType,
                     Runnable... additionalTests) {
            this.progressState = progressState;
            this.threadType = threadType;
            this.additionalTests
                    = (additionalTests != null) && (additionalTests.length != 0)
                      ? additionalTests
                      : null;
            this.synchrType = synchronizationType;
            this.callStack = Thread.currentThread().getStackTrace();
        }
        boolean isSuspending() {
            return synchrType == SUSPENDING;
        }
        boolean isResuming() {
            return synchrType == RESUMING;
        }
        @Override
        public String toString() {
            return "ProgressTest(" + getParamString() + ')';
        }

        private String getParamString() {
            StringBuilder buf = new StringBuilder(100);
            buf.append(progressState).append(", ").append(threadType);
            if (additionalTests != null) {
                for (Runnable additionalTest : additionalTests) {
                    buf.append(", ");
                    buf.append(additionalTest.getClass().getSimpleName());
                }
            }
            if (isSuspending()) {
                buf.append(", suspending");
            } else if (isResuming()) {
                buf.append(", resuming");
            }
            return buf.toString();
        }
    }

    private final class ProgressTester implements ChangeListener {

        private final Object testLock;
        private final Object suspendedThreadLock = new Object();
        private final Queue<ProgressTest> progressTestQueue
                                  = new ConcurrentLinkedQueue<ProgressTest>();
        private volatile boolean listening;
        private volatile Throwable failure;
        private ProgressTest pendingSuspendingProgressTest;
        private boolean pendingSuspendingProgressTestResumeExecuted;

        /**
         * {@code ThreadType} of the last unpaired suspending test.
         * &quot;unpaired suspending test&quot; means a suspending test
         * for which a corresponding resuming test has not been scheduled yet.
         */
        private ThreadType unpairedSuspendingTestType;

        ProgressTester(Object testLock) {
            this.testLock = testLock;
        }

        private void scheduleTest(Progress progressState,
                                  ThreadType threadType) {
            scheduleTest(progressState, threadType, (Runnable[]) null);
        }

        private void scheduleTest(Progress progressState,
                                  ThreadType threadType,
                                  Runnable... additionalTests) {
            checkNonSuspendingTestType(threadType);
            scheduleTest(new ProgressTest(progressState, threadType,
                                          additionalTests));
        }

        /**
         * Schedules a progress test which, after executed, suspends the current
         * thread (unless the test failed). This allows that other tests
         * being executed in other threads finish before next tests
         * for the current are started.
         * 
         * @param  progressState
         * @param  threadType
         * @see  #scheduleResumingTest
         */
        private void scheduleSuspendingTest(Progress progressState,
                                            ThreadType threadType) {
            scheduleSuspendingTest(progressState, threadType, (Runnable[]) null);
        }

        /**
         * Schedules a progress test which, after executed, suspends the current
         * thread (unless the test failed). This allows that other tests
         * being executed in other threads finish before next tests
         * for the current are started. If the third parameter is specified
         * (non-null), the given routine is executed after the given test
         * passes (and before the current thread is suspended, of course).
         *
         * @param  progressState
         * @param  threadType
         * @param  additionalTest
         * @see  #scheduleResumingTest
         */
        private void scheduleSuspendingTest(Progress progressState,
                                            ThreadType threadType,
                                            Runnable... additionalTests) {
            if (unpairedSuspendingTestType != null) {
                throw new IllegalStateException(
                        "Cannot schedule a suspending test until a resuming" +
                        " test is scheduled for the previously scheduled" +
                        " suspending test");
            }
            scheduleTest(new ProgressTest(ProgressTest.SUSPENDING,
                                          progressState, threadType,
                                          additionalTests));
            unpairedSuspendingTestType = threadType;
        }

        /**
         * 
         * @param progressState
         * @param threadType
         */
        private void scheduleResumingTest(Progress progressState,
                                          ThreadType threadType) {
            scheduleResumingTest(progressState, threadType, (Runnable[]) null);
        }

        /**
         * 
         * @param progressState
         * @param threadType
         * @param additionalTest
         */
        private void scheduleResumingTest(Progress progressState,
                                          ThreadType threadType,
                                          Runnable... additionalTests) {
            if (unpairedSuspendingTestType == null) {
                throw new IllegalStateException(
                        "Cannot schedule a resuming test because no" +
                        " corresponding suspending test has been scheduled.");
            }
            checkNonSuspendingTestType(threadType);
            scheduleTest(new ProgressTest(ProgressTest.RESUMING,
                                          progressState, threadType,
                                          additionalTests));
            unpairedSuspendingTestType = null;
        }

        private void scheduleTest(ProgressTest progressTest) {
            progressTestQueue.add(progressTest);
        }

        private void checkNonSuspendingTestType(ThreadType threadType) {
            if (unpairedSuspendingTestType == null) {
                return;
            }

            if (threadType == unpairedSuspendingTestType) {
                throw new IllegalStateException(
                        "Cannot schedule a ProgressTest of the same thread" +
                        " type as that of the unpaired suspending thread.");
            }
        }

        public void stateChanged(ChangeEvent e) {
            ProgressTest progressTest;
            boolean isPendingSuspendingTest = false;
            boolean shouldSuspend = false;
            boolean shouldResume = false;
            final boolean isLastTest;

            synchronized (suspendedThreadLock) {
                if ((pendingSuspendingProgressTest != null)
                        && (getCurrThreadType() == pendingSuspendingProgressTest.threadType)) {
                    isPendingSuspendingTest = true;
                    progressTest = pendingSuspendingProgressTest;
                    System.out.println("Hit the stored suspending test (" + pendingSuspendingProgressTest + ')');
                    pendingSuspendingProgressTest = null;
                } else {
                    progressTest = progressTestQueue.poll();
                    System.out.println("Polled a test (" + progressTest + ')');
                    if (progressTest == null) {
                        System.out.println(" - it's <null> - quit");
                        return;
                    }
                    if (progressTest.isSuspending()) {
                        assert pendingSuspendingProgressTest == null;
                        if (getCurrThreadType() != progressTest.threadType) {
                            pendingSuspendingProgressTest = progressTest;
                            pendingSuspendingProgressTestResumeExecuted = false;
                            System.out.println(" - it's not a test for the current thread - test stored");
                            progressTest = progressTestQueue.poll();  //poll the next test
                            System.out.println("Polled a test (" + progressTest + ')');
                        }
                    }
                }

                if (progressTest.isSuspending()) {
                    if (isPendingSuspendingTest && pendingSuspendingProgressTestResumeExecuted) {
                        System.out.println(
                                " - it's a suspending thread but the corresponding"
                                + " resuming test has been already executed"
                                + " so we will not suspend");
                    } else if (progressTestQueue.isEmpty()) {
                        System.out.println(" - it's a suspending thread but"
                                           + " it is the last remaining test"
                                           + " so we will not suspend");
                    } else {
                        shouldSuspend = true;
                    }
                } else if (progressTest.isResuming()) {
                    shouldResume = true;
                    if (pendingSuspendingProgressTest != null) {
                        pendingSuspendingProgressTestResumeExecuted = true;
                    }
                }

                isLastTest = progressTestQueue.isEmpty() && (pendingSuspendingProgressTest == null);

                performTest(progressTest);

                if (isLastTest || (failure != null)) {
                    System.out.println("   - IS LAST TEST - WILL RESUME (if there is a test thread suspended)");
                    stopListening();
                    resumeSuspendedThread();
                    synchronized (testLock) {
                        testLock.notify();      //resumes the thread running the unit test
                    }
                } else if (shouldSuspend) {
                    System.out.println("   - WILL SUSPEND");
                    suspendCurrentThread(progressTest);
                } else if (shouldResume) {
                    System.out.println("   - WILL RESUME");
                    resumeSuspendedThread();
                }
            }
        }

        private void performTest(ProgressTest progressTest) {
            try {
                assertSame(progressTest.progressState, comboSupport.getProgress());
                assertSame(progressTest.threadType,    getCurrThreadType());
                if (progressTest.additionalTests != null) {
                    for (Runnable additionalTest : progressTest.additionalTests) {
                        additionalTest.run();
                    }
                }
            } catch (Throwable t) {
                handleException(t, progressTest.callStack);
            }
        }

        private ThreadType getCurrThreadType() {
            return ThreadType.forBoolean(EventQueue.isDispatchThread());
        }

        private void suspendCurrentThread(ProgressTest progressTest) {
            System.out.println("Suspending the current thread.");
            try {
                suspendedThreadLock.wait();
            } catch (InterruptedException ex) {
                handleException(ex, progressTest.callStack);
            }
        }

        private void resumeSuspendedThread() {
            System.out.println("Resuming the suspended thread (if any).");
            suspendedThreadLock.notify();
        }

        private void handleException(Throwable t,
                                     StackTraceElement[] callersStackTrace) {
            synchronized (this) {
                if (failure == null) {
                    failure = addCallersCallStack(t, callersStackTrace); //store information about the exception
                }
            }
        }

        private Throwable addCallersCallStack(Throwable t,
                                              StackTraceElement[] callersCallStack) {
            t.setStackTrace(concatArrays(t.getStackTrace(), callersCallStack));
            return t;
        }

        private void startListening() {
            if (unpairedSuspendingTestType != null) {
                throw new IllegalStateException(
                        "There is a suspending test scheduled for which" +
                        " there is no corresponding resuming test scheduled.");
            }

            if (listening) {
                throw new IllegalStateException("already listening");
            }
            listening = true;

            comboSupport.setProgressListener(this);
        }

        private void stopListening() {
            comboSupport.setProgressListener(null);
        }

    }

    static <T> T[] concatArrays(T[] a, T[] b) {
        if (b.length == 0) {
            return a;
        }
        if (a.length == 0) {
            return b;
        }
        T[] result = (T[]) java.lang.reflect.Array.newInstance(
                                            a.getClass().getComponentType(),
                                            a.length + b.length);
        System.arraycopy(a, 0, result,        0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

}