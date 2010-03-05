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

package org.netbeans.modules.java.hints.threading;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author lahvac
 */
public class TinyTest extends TestBase {

    public TinyTest(String name) {
        super(name, Tiny.class);
    }

    public void testNotifyOnCondition1() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private void n(java.util.concurrent.locks.Condition c) {\n" +
                       "         c.notify();\n" +
                       "     }\n" +
                       "}\n",
                       "3:11-3:17:verifier:ERR_NotifyOnCondition(notify)",
                       "FIX_NotifyOnConditionFix(signal)",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private void n(java.util.concurrent.locks.Condition c) {\n" +
                        "         c.signal();\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testNotifyOnCondition2() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private void n(java.util.concurrent.locks.Condition c) {\n" +
                       "         c.notifyAll();\n" +
                       "     }\n" +
                       "}\n",
                       "3:11-3:20:verifier:ERR_NotifyOnCondition(notifyAll)",
                       "FIX_NotifyOnConditionFix(signalAll)",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private void n(java.util.concurrent.locks.Condition c) {\n" +
                        "         c.signalAll();\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testWaitOnCondition1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private void n(java.util.concurrent.locks.Condition c) {\n" +
                            "         c.wait();\n" +
                            "     }\n" +
                            "}\n",
                            "3:11-3:15:verifier:ERR_WaitOnCondition");
    }

    public void testWaitOnCondition2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private void n(java.util.concurrent.locks.Condition c) {\n" +
                            "         c.wait(1L);\n" +
                            "     }\n" +
                            "}\n",
                            "3:11-3:15:verifier:ERR_WaitOnCondition");
    }

    public void testWaitOnCondition3() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private void n(java.util.concurrent.locks.Condition c) {\n" +
                            "         c.wait(1L, 1);\n" +
                            "     }\n" +
                            "}\n",
                            "3:11-3:15:verifier:ERR_WaitOnCondition");
    }

    public void testThreadRun() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private void n(Thread t) {\n" +
                       "         t.run();\n" +
                       "     }\n" +
                       "}\n",
                       "3:11-3:14:verifier:ERR_ThreadRun",
                       "FIX_ThreadRun",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private void n(Thread t) {\n" +
                        "         t.start();\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testThreadStartInConstructor1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private void n(Thread t) {\n" +
                            "         t.start();\n" +
                            "     }\n" +
                            "}\n");
    }

    public void testThreadStartInConstructor2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     protected Test() {\n" +
                            "         Thread t = null;\n" +
                            "         t.start();\n" +
                            "     }\n" +
                            "}\n",
                            "4:11-4:16:verifier:ERR_ThreadStartInConstructor");
    }
    

    public void testThreadYield() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private void n() {\n" +
                            "         Thread.yield();\n" +
                            "     }\n" +
                            "}\n",
                            "3:16-3:21:verifier:ERR_ThreadYield");
    }

    public void testThreadSuspend() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private void n(Thread t) {\n" +
                            "         t.suspend();\n" +
                            "     }\n" +
                            "}\n",
                            "3:11-3:18:verifier:ERR_ThreadSuspend(suspend)");
    }

    public void testNestedSynchronized1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private void n(Object o1, Object o2) {\n" +
                            "         synchronized(o1) {\n" +
                            "             if (o2 != null) {\n" +
                            "                 synchronized(o2) {\n" +
                            "                     System.err.println(1);\n" +
                            "                 }\n" +
                            "             }\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n",
                            "3:9-3:21:verifier:ERR_NestedSynchronized");
    }

    public void testNestedSynchronized2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private synchronized void n(Object o1, Object o2) {\n" +
                            "         synchronized(o1) {\n" +
                            "             if (o2 != null) {\n" +
                            "                 synchronized(o2) {\n" +
                            "                     System.err.println(1);\n" +
                            "                 }\n" +
                            "             }\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n",
                            "3:9-3:21:verifier:ERR_NestedSynchronized");
    }

    public void testNestedSynchronized3() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private synchronized void n(Object o1, Object o2) {\n" +
                            "         synchronized(o1) {\n" +
                            "             if (o2 != null) {\n" +
                            "             }\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n",
                            "3:9-3:21:verifier:ERR_NestedSynchronized");
    }

    public void testNestedSynchronized4() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private void n(Object o1, Object o2) {\n" +
                            "         synchronized(o1) {\n" +
                            "             if (o2 != null) {\n" +
                            "             }\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n");
    }

    public void testNestedSynchronized5() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private void n(Object o1, Object o2) {\n" +
                            "         synchronized(o1) {\n" +
                            "             if (o2 != null) {\n" +
                            "                 synchronized(o2) {\n" +
                            "                      if (o2 != null) {\n" +
                            "                          synchronized(o2) {\n" +
                            "                               System.err.println(1);\n" +
                            "                          }\n" +
                            "                      }\n" +
                            "                 }\n" +
                            "             }\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n",
                            "3:9-3:21:verifier:ERR_NestedSynchronized");
    }

    public void testEmptySynchronized() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private void n(Object o1, Object o2) {\n" +
                            "         synchronized(o1) {}\n" +
                            "     }\n" +
                            "}\n",
                            "3:9-3:21:verifier:ERR_EmptySynchronized");
    }

    public void testSynchronizedOnLock() throws Exception {
        performFixTest("test/Test.java",
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private void n(java.util.concurrent.locks.Lock l) {\n" +
                       "         synchronized(l) {\n" +
                       "             System.err.println(1);\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n",
                       "3:9-3:21:verifier:ERR_SynchronizedOnLock",
                       "FIX_SynchronizedOnLock",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "     private void n(java.util.concurrent.locks.Lock l) {\n" +
                        "         l.lock();\n" +
                        "         try {\n" +
                        "             System.err.println(1);\n" +
                        "         } finally {\n" +
                        "              l.unlock();\n" +
                        "         }\n" +
                        "     }\n" +
                        "}\n").replaceAll("[\t\n ]+", " "));
    }

    public void testVolatileArray1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private volatile String[] arr;\n" +
                            "}\n",
                            "2:31-2:34:verifier:ERR_VolatileArrayField");
    }

    public void testVolatileArray2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private volatile int[] arr = {0};\n" +
                            "}\n",
                            "2:28-2:31:verifier:ERR_VolatileArrayField");
    }

    public void testVolatileArray3() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private String[] arr;\n" +
                            "}\n");
    }

    public void testVolatileArray4() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "public class Test {\n" +
                            "     private volatile String arr;\n" +
                            "}\n");
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }


}