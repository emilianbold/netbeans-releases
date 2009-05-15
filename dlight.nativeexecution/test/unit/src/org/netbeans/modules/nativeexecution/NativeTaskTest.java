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
package org.netbeans.modules.nativeexecution;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcess.State;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminalProvider;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;

/**
 *
 * @author ak119685
 */
public class NativeTaskTest extends NativeExecutionTest {

    public NativeTaskTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    static int count = 0;

    @Test
    public void fake() {
    }

    @Test
    public void simpleTest() {
        ExternalTerminal term = ExternalTerminalProvider.getTerminal(ExecutionEnvironmentFactory.getLocal(), "gnome-terminal"); // NOI18N
        NativeProcessBuilder npb = new NativeProcessBuilder(
                ExecutionEnvironmentFactory.getLocal(), "ls",false).useExternalTerminal(term); // NOI18N
        StringWriter result = new StringWriter();
        ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(InputOutput.NULL).outProcessorFactory(new InputRedirectorFactory(result));
        ExecutionService execService = ExecutionService.newService(
                npb, descriptor, "Demangling function "); // NOI18N

        Future<Integer> res = execService.run();

        try {
            System.out.println("Result: " + res.get()); // NOI18N
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println(result.toString());

    }

//    @Test
    public void testDemangle() {

        System.out.println("STart-TestDEmangle"); // NOI18N
        int threadsNum = 100;
        CountDownLatch latch = new CountDownLatch(threadsNum);
        CountDownLatch start = new CountDownLatch(1);

        for (int i = 0; i < threadsNum; i++) {
            new Thread(new Demangler("fractal_2`_Z10Mandelbrotv+0x602", latch, start)).start(); // NOI18N
        }

        System.out.println("GO!"); // NOI18N
        start.countDown();

        try {
            latch.await();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println("Done"); // NOI18N
    }

    private static class Demangler implements Runnable {

        private final String str;
        private CountDownLatch start;
        private CountDownLatch latch;

        public Demangler(String str, CountDownLatch latch, CountDownLatch start) {
            this.str = str;
            this.start = start;
            this.latch = latch;
        }

        private String demangle() {
            String nameToDemangle;
            final ExecutionEnvironment env = ExecutionEnvironmentFactory.getLocal();
            final String dem_util_path = "/usr/sfw/bin/gc++filt"; // NOI18N

            if (str.indexOf("`") != -1 && str.indexOf("+") != -1) { // NOI18N
                nameToDemangle = str.substring(str.indexOf("`") + 1, str.indexOf("+")); //NOI18N;
            } else {
                nameToDemangle = str;
            }

            NativeProcessBuilder npb = new NativeProcessBuilder(env, dem_util_path,false).setArguments(nameToDemangle);
            StringWriter result = new StringWriter();
            ExecutionDescriptor descriptor = new ExecutionDescriptor().inputOutput(InputOutput.NULL).outProcessorFactory(new InputRedirectorFactory(result));
            ExecutionService execService = ExecutionService.newService(
                    npb, descriptor, "Demangling function " + nameToDemangle); // NOI18N

            Future<Integer> res = execService.run();

            try {
                res.get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }

            return result.toString().trim();
        }

        public void run() {
            try {
                start.await();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }

            System.out.println(str + " == " + demangle()); // NOI18N

            latch.countDown();
        }
    }

    private static class InputRedirectorFactory implements ExecutionDescriptor.InputProcessorFactory {

        private final Writer writer;

        public InputRedirectorFactory(Writer writer) {
            this.writer = writer;
        }

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.copying(writer);
        }
    }

    /**
     * Test of run method, of class NativeTask.
     */
//    @Test
    public void testRun() {
        System.out.println("run"); // NOI18N

        final ExecutionEnvironment ee =
                ExecutionEnvironmentFactory.createNew(System.getProperty("user.name"), "localhost", 22); // NOI18N

//        MacroExpander macroExpander = MacroExpanderFactory.getExpander(ee);
//        try {
//            String path = macroExpander.expandMacros("$osname-$platform"); // NOI18N
//            System.out.println("PATH IS " + path);
//        } catch (ParseException ex) {
//            System.out.println("Parse exception! Pos = " + ex.getErrorOffset());
//        }

        final String cmd = "/export/home/ak119685/welcome.sh"; // NOI18N

        ChangeListener l = new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                NativeProcess process = (NativeProcess) e.getSource();
                State newState = process.getState();

                if (newState == State.STARTING) {
                    return;
                }

                if (newState == State.ERROR) {
                    System.out.println("Unable to start process!"); // NOI18N
                    return;
                }
                try {
                    System.out.println("Process " + process.toString() + " [" + process.getPID() + "] -> " + newState); // NOI18N
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };

        ExternalTerminal term = ExternalTerminalProvider.getTerminal(ExecutionEnvironmentFactory.getLocal(), "gnome-terminal").setTitle("My favorite title"); // NOI18N
        NativeProcessBuilder npb = new NativeProcessBuilder(ee, cmd,false).setArguments("1", "2").addEnvironmentVariable("MY_VAR", "/temp/xx/$platform").setWorkingDirectory("/tmp").addNativeProcessListener(l).useExternalTerminal(term); // NOI18N
        ExecutionDescriptor descr = new ExecutionDescriptor().outLineBased(true).outProcessorFactory(new ExecutionDescriptor.InputProcessorFactory() {

            public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                return InputProcessors.copying(new OutputStreamWriter(System.out));
            }
        });

        ExecutionService service = ExecutionService.newService(npb, descr, "test"); // NOI18N

        Future<Integer> result = service.run();
        Integer res = null;
        try {
            res = result.get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        System.out.println("RESULT == " + res); // NOI18N

//
//        final NativeTaskListener l = new NativeTaskListener() {
//
//            public void taskStarted(NativeTask task) {
//                System.out.println(task.getPID() + " - started");
//            }
//
//            public void taskFinished(NativeTask task, Integer result) {
//                System.out.println(task.getPID() + " - finished");
//            }
//
//            public void taskCancelled(NativeTask task, CancellationException cex) {
//                System.out.println(task.getPID() + " - cancelled");
//            }
//
//            public void taskError(NativeTask task, Throwable t) {
//                System.out.println(task.getPID() + " - error");
//            }
//        };
//
//        StringBuffer outBuffer = new StringBuffer();
//        final ExecutionEnvironment ee = new ExecutionEnvironment(null, null);
//
//        NativeTask nt = new NativeTask(ee, "/bin/uname", new String[]{"-s"});
//        nt.redirectOutTo(new StringBufferWriter(outBuffer));
//        nt.addListener(l);
//        nt.submit();
//        try {
//            nt.get();
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (ExecutionException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//        System.out.println(outBuffer.toString());
//
//
//        Thread[] ts = new Thread[10];
//        for (int i = 0; i < 10; i++) {
//            ts[i] = new Thread(new Runnable() {
//                public void run() {
//                    StringBuffer outBuffer = new StringBuffer();
////                    NativeTask nt = new NativeTask(ee, "/bin/uname", new String[]{"-s"});
//                    NativeTask nt = new NativeTask("/tmp/qq");
//                    nt.redirectOutTo(new StringBufferWriter(outBuffer));
//                    nt.addListener(l);
//                    nt.submit();
//                    try {
//                        nt.get();
//                    } catch (InterruptedException ex) {
//                        Exceptions.printStackTrace(ex);
//                    } catch (ExecutionException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//
//                    System.out.println("" + (++count) + ") " + nt.getPID() + " - " + outBuffer.toString());
//                }
//            });
//        }
//
//        for (int i = 0; i < 10; i++) {
//            ts[i].start();
//        }
//
//        for (int i = 0; i < 10; i++) {
//            try {
//                ts[i].join();
//            } catch (InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
//
//        final NativeTaskListener l = new NativeTaskListener() {
//
//            public void taskStarted(NativeTask task) {
//                System.out.println(task.getPID() + " - started");
//            }
//
//            public void taskFinished(NativeTask task, Integer result) {
//                System.out.println(task.getPID() + " - finished");
//            }
//
//            public void taskCancelled(NativeTask task, CancellationException cex) {
//                System.out.println(task.getPID() + " - cancelled");
//            }
//
//            public void taskError(NativeTask task, Throwable t) {
//                System.out.println(task.getPID() + " - error");
//            }
//        };
//
//        StringBuffer outBuffer = new StringBuffer();
//        final ExecutionEnvironment ee = new ExecutionEnvironment(null, null);
//
//        NativeTask nt = new NativeTask(ee, "/bin/uname", new String[]{"-s"});
//        nt.redirectOutTo(new StringBufferWriter(outBuffer));
//        nt.addListener(l);
//        nt.submit();
//        try {
//            nt.get();
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (ExecutionException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//        System.out.println(outBuffer.toString());
//
//
//        Thread[] ts = new Thread[10];
//        for (int i = 0; i < 10; i++) {
//            ts[i] = new Thread(new Runnable() {
//                public void run() {
//                    StringBuffer outBuffer = new StringBuffer();
////                    NativeTask nt = new NativeTask(ee, "/bin/uname", new String[]{"-s"});
//                    NativeTask nt = new NativeTask("/tmp/qq");
//                    nt.redirectOutTo(new StringBufferWriter(outBuffer));
//                    nt.addListener(l);
//                    nt.submit();
//                    try {
//                        nt.get();
//                    } catch (InterruptedException ex) {
//                        Exceptions.printStackTrace(ex);
//                    } catch (ExecutionException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//
//                    System.out.println("" + (++count) + ") " + nt.getPID() + " - " + outBuffer.toString());
//                }
//            });
//        }
//
//        for (int i = 0; i < 10; i++) {
//            ts[i].start();
//        }
//
//        for (int i = 0; i < 10; i++) {
//            try {
//                ts[i].join();
//            } catch (InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }

//        NativeTask instance = new NativeTask("");
//        instance.run();
        // TODO review the generated test code and remove the default call to fail.

        final CharArrayWriter errWriter = new CharArrayWriter();
//
//        int tcount = 1;
//        Thread[] threads = new Thread[tcount];
//        for (int i = 0; i < tcount; i++) {
//            threads[i] = new Thread(new Runnable() {
//
//                public void run() {
//                    final NativeTask task = new NativeTask("/bin/lsss /");
//                    task.redirectErrTo(errWriter);
//                    task.submit(true, false);
//                    System.out.println("PID is " + task.getPID());
//
//                    try {
//                        System.out.println("Result: " + task.get());
//                    } catch (InterruptedException ex) {
//                        Exceptions.printStackTrace(ex);
//                    } catch (ExecutionException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//
//                    System.out.println("ERROR: '" + errWriter.toString() + "'");
//
//                    try {
//                        System.out.println(task.invoke(false));
//                    } catch (Exception ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//
//                }
//            });
//        }
//
//        for (int i = 0; i < tcount; i++) {
//            threads[i].start();
//        }
//
//        for (int i = 0; i < tcount; i++) {
//            try {
//                threads[i].join();
//            } catch (InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }

        System.out.println("Here we are!"); // NOI18N

//        fail("The test case is a prototype.");
    }
}
