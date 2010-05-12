/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans;

import java.io.*;
import java.util.logging.Level;
import org.netbeans.junit.*;
import java.util.*;
import java.util.logging.Logger;
import org.openide.util.RequestProcessor;

/**
 * Test the command-line-interface handler.
 * @author Jaroslav Tulach
 */
public class CLIHandlerTest extends NbTestCase {

    private static ByteArrayInputStream nullInput = new ByteArrayInputStream(new byte[0]);
    private static ByteArrayOutputStream nullOutput = new ByteArrayOutputStream();
    
    private Logger LOG;

    public CLIHandlerTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
        
        super.setUp();

        // all handlers shall be executed immediatelly
        CLIHandler.finishInitialization (false);
        
        // setups a temporary file
        String p = getWorkDirPath ();
        if (p == null) {
            p = System.getProperty("java.io.tmpdir");
        }
        String tmp = p;
        assertNotNull(tmp);
        System.getProperties().put("netbeans.user", tmp);
        
        File f = new File(tmp, "lock");
        if (f.exists()) {
            assertTrue("Clean up previous mess", f.delete());
            assertTrue(!f.exists());
        }
    }
    
    protected @Override Level logLevel() {
        return Level.FINEST;
    }
    
    public void testFileExistsButItCannotBeRead() throws Exception {
        // just creates the file and blocks
        InitializeRunner runner = new InitializeRunner(10);
        
        // blocks when operation fails
        InitializeRunner second = new InitializeRunner(85);
        
        for (int i = 0; i < 3; i++) {
            second.next();
        }
        
        // finishes the code
        runner.next();
        
        assertNotNull("Runner succeeded to allocate the file", runner.resultFile());
        
        // let the second code go on as well
        second.next();
        
        assertEquals("The same file has been allocated", runner.resultFile(), second.resultFile());
    }
    
    public void testFileExistsButTheServerCannotBeContacted() throws Exception {
        // start the server and block
        InitializeRunner runner = new InitializeRunner(65);
        
        assertNotNull("File created", runner.resultFile());
        assertTrue("Port allocated", runner.resultPort() != 0);
        
        // blocks when operation fails
        InitializeRunner second = new InitializeRunner(85);
        // second try should be ok
        second.next();
        
        assertNotNull("Previous file deleted and new one created", second.resultFile());
        assertTrue("Another port allocated", second.resultPort() != runner.resultPort());
    }
    
    public void testFileExistsHasPortButNotTheKey() throws Exception {
        // start the server and block
        Integer block = new Integer(97);
        InitializeRunner runner;
        synchronized (block) {
            runner = new InitializeRunner(block, true);
            // the initialization code can finish without reaching 97
            runner.waitResult();
        }
        
        assertTrue("Port allocated", runner.resultPort() != 0);
        
        // blocks after read the keys from the file
        InitializeRunner second = new InitializeRunner(94);

        // let the CLI Secure Handler finish
        synchronized (block) {
            block.notifyAll();
        }
        // let the test go beyond 97 to the end of file
        assertNotNull("File created", runner.resultFile());
        
        
        // let the second finish
        second.next();
        
        assertEquals("Still the same file", runner.resultFile(), second.resultFile());
        assertEquals("Another port allocated", second.resultPort(), runner.resultPort());
    }

    public void testFileExistsHasPortButPortIsNotActive() throws Exception {
        String tmp = System.getProperty("netbeans.user");

        File f = new File(tmp, "lock");
        if (f.exists()) {
            assertTrue("Clean up previous mess", f.delete());
            assertTrue(!f.exists());
        }

        // write down stupid port number
        FileOutputStream os = new FileOutputStream(f);
        os.write(0);
        os.write(0);
        os.write(80);
        os.write(26);
        os.close();

        // blocks after read the keys from the file
        InitializeRunner second = new InitializeRunner(94);

        // let the second finish
        second.next();

        assertEquals("Still the same file", f, second.resultFile());
        assertTrue("finished", second.waitResult());
    }
    
    public void testHelpIsPrinted() throws Exception {
        class UserDir extends CLIHandler {
            private int cnt;
            private boolean doCheck;
            
            public UserDir() {
                super(WHEN_BOOT);
            }
            
            protected int cli(Args args) {
                if (!doCheck) {
                    return 0;
                }
                
                cnt++;
                
                for (String a : args.getArguments()) {
                    if ("--help".equals(a)) {
                        return 0;
                    }
                }
                return 5;
            }
            
            protected void usage(PrintWriter w) {
                w.println("this is a help");
            }
        }
        UserDir ud = new UserDir();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        CLIHandler.Status res = cliInitialize(new String[] { "--help" }, new CLIHandler[] { ud }, nullInput, os, nullOutput);
        assertEquals("Help returns 2", 2, res.getExitCode());
        
        if (os.toString().indexOf("help") == -1) {
            fail("There should be some help text:\n" + os);
        }
    }

    public void testHelpIsPassedToRunningServer() throws Exception {
        class UserDir extends CLIHandler implements Runnable {
            private int cnt;
            private int usage;
            private boolean doCheck;
            private CLIHandler.Status res;
            
            public UserDir() {
                super(WHEN_BOOT);
            }
            
            protected int cli(Args args) {
                if (!doCheck) {
                    return 0;
                }
                
                cnt++;
                
                for (String a : args.getArguments()) {
                    if ("--help".equals(a)) {
                        return 0;
                    }
                }
                return 5;
            }
            
            protected void usage(PrintWriter w) {
                usage++;
            }
            
            public void run() {
                res = cliInitialize(new String[] { }, new CLIHandler[] { this }, nullInput, nullOutput, nullOutput);
            }
        }
        UserDir ud = new UserDir();
        
        RequestProcessor.getDefault().post(ud).waitFinished();
        assertNotNull(ud.res);
        
        assertNotNull("File created", ud.res.getLockFile());
        assertTrue("Port allocated", ud.res.getServerPort() != 0);
        
        ud.doCheck = true;
        CLIHandler.Status res = cliInitialize(new String[] { "--help" }, new CLIHandler[0], nullInput, nullOutput, nullOutput);
        
        assertEquals("Ok exec of help", 2, res.getExitCode());
        
        assertEquals("No cli called", 0, ud.cnt);
        assertEquals("Usage called", 1, ud.usage);
        
    }
    
    public void testFileExistsButTheServerCannotBeContactedAndWeDoNotWantToCleanTheFileOnSameHost() throws Exception {
        // start the server and block
        InitializeRunner runner = new InitializeRunner(65);
        
        assertNotNull("File created", runner.resultFile());
        assertTrue("Port allocated", runner.resultPort() != 0);
        
        CLIHandler.Status res = CLIHandler.initialize(
            new CLIHandler.Args(new String[0], nullInput, nullOutput, nullOutput, ""), 
            null, Collections.<CLIHandler>emptyList(), false, false, null
        );
        
        assertNotNull("Previous file deleted and new one created", res.getLockFile());
        assertTrue("Another port allocated", res.getServerPort() != runner.resultPort());
    }
    
    public void testFileExistsButTheServerCannotBeContactedAndWeDoNotWantToCleanTheFileOnOtherHost() throws Exception {
        // start the server and block
        InitializeRunner runner = new InitializeRunner(65);
        
        assertNotNull("File created", runner.resultFile());
        assertTrue("Port allocated", runner.resultPort() != 0);
        
        File f = runner.resultFile();
        byte[] arr = new byte[(int)f.length()];
        int len = arr.length;
        assertTrue("We know that the size of the file should be int + key_length + 4 for ip address: ", len >=14 && len <= 18);
        FileInputStream is = new FileInputStream(f);
        assertEquals("Fully read", arr.length, is.read(arr));
        is.close();
        
        byte[] altarr = new byte[18];
        for (int i = 0; i < 18; i++) {
            altarr[i] = i<14? arr[i]: 1;
        }
        
        // change the IP at the end of the file
        FileOutputStream os = new FileOutputStream(f);
        os.write(altarr);
        os.close();
        
        CLIHandler.Status res = CLIHandler.initialize(
            new CLIHandler.Args(new String[0], nullInput, nullOutput, nullOutput, ""), 
            null, Collections.<CLIHandler>emptyList(), false, false, null
        );
        
        assertEquals ("Cannot connect because the IP is different", CLIHandler.Status.CANNOT_CONNECT, res.getExitCode());
    }
    
    public void testFileExistsButTheKeyIsNotRecognized() throws Exception {
        // start the server be notified when it accepts connection
        InitializeRunner runner = new InitializeRunner(65);
        
        assertNotNull("File created", runner.resultFile());
        assertTrue("Port allocated", runner.resultPort() != 0);
        
        int s = (int)runner.resultFile().length();
        byte[] copy = new byte[s];
        FileInputStream is = new FileInputStream(runner.resultFile());
        assertEquals("Read fully", s, is.read(copy));
        is.close();
        
        // change one byte in the key
        copy[4 + 2]++;
        
        FileOutputStream os = new FileOutputStream(runner.resultFile());
        os.write(copy);
        os.close();
        
        // try to connect to previous server be notified as soon as it
        // sends request
        InitializeRunner second = new InitializeRunner(30);
        
        // handle the request, say NO
        runner.next();
        
        // read the reply and allocate new port
        second.next();
        
        assertNotNull("Previous file deleted and new one created", second.resultFile());
        assertTrue("Another port allocated", second.resultPort() != runner.resultPort());
    }
    
    public void testCLIHandlersCanChangeLocationOfLockFile() throws Exception {
        clearWorkDir();
        final File dir = getWorkDir();
        
        class UserDir extends CLIHandler {
            private int cnt;
            
            public UserDir() {
                super(WHEN_BOOT);
            }
            
            protected int cli(Args args) {
                cnt++;
                System.setProperty("netbeans.user", dir.toString());
                return 0;
            }
            
            protected void usage(PrintWriter w) {}
        }
        UserDir ud = new UserDir();
        
        CLIHandler.Status res = cliInitialize(new String[0], ud, nullInput, nullOutput, nullOutput, null);
        assertNotNull("res: ", res);
        assertEquals("Our command line handler is called once", 1, ud.cnt);
        assertEquals("Lock file is created in dir", dir, res.getLockFile().getParentFile());
    }
    
    public void testCLIHandlerCanStopEvaluation() throws Exception {
        class H extends CLIHandler {
            private int cnt;
            
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                cnt++;
                return 1;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        H h2 = new H();
        
        
        CLIHandler.Status res = cliInitialize(new String[0], new H[] {
            h1, h2
        }, nullInput, nullOutput, nullOutput);
        
        assertEquals("CLI evaluation failed with return code of h1", 1, res.getExitCode());
        assertEquals("First one executed", 1, h1.cnt);
        assertEquals("Not the second one", 0, h2.cnt);
    }
    
    public void testWhenInvokedTwiceParamsGoToTheFirstHandler() throws Exception {
        final String[] template = { "Ahoj", "Hello" };
        final String currentDir = "MyDir";
        
        class H extends CLIHandler {
            private int cnt;
            
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                String[] a = args.getArguments();
                String[] t = template;
                
                assertEquals("Same length", t.length, a.length);
                assertEquals("First is same", t[0], a[0]);
                assertEquals("Second is same", t[1], a[1]);
                assertEquals("Current dir is fine", currentDir, args.getCurrentDirectory().toString());
                return ++cnt;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        
        
        CLIHandler.Status res = cliInitialize(template, h1, nullInput, nullOutput, nullOutput, null, currentDir);
        
        assertEquals("First one executed", 1, h1.cnt);
        assertEquals("CLI evaluation failed with return code of h1", 1, res.getExitCode());
        
        res = cliInitialize(template, java.util.Collections.<CLIHandler>emptyList(), nullInput, nullOutput, nullOutput, null, currentDir);
        assertEquals("But again executed h1", 2, h1.cnt);
        assertEquals("Now the result is 2 as cnt++ was increased", 2, res.getExitCode());
        
    }

    public void testServerWaitsBeforeFinishInitializationIsCalledOn () throws Exception {
        // this tests will not execute handlers immediatelly
        CLIHandler.finishInitialization (true);
        
        class H extends CLIHandler implements Runnable {
            public volatile int cnt;
            public volatile int afterFinish = -1;
            
            public H () {
                super (CLIHandler.WHEN_INIT);
            }
            
            protected int cli (Args args) {
                cnt++;
                LOG.info("Increased cnt to: " + cnt + " by thread " + Thread.currentThread());
                return afterFinish;
            }
            
            public void run () {
                // cnt will be two as once the first cliInitialize will
                // invoke the handler and once the second cliInitialize 
                // using the Server            
                afterFinish = 2;
                CLIHandler.finishInitialization (false);
            }
            
            protected void usage (PrintWriter w) {}
        }
        H h = new H ();
        
        CLIHandler.Status res = cliInitialize (new String[0], h, nullInput, nullOutput, nullOutput, null);
        assertEquals ("Returns 0 as no finishInitialization is called", 0, res.getExitCode ());
        // after two seconds it calls finishInitialization
        RequestProcessor.Task task = RequestProcessor.getDefault ().post (h, 7000); // 7s is higher than socket timeout
        res = cliInitialize (new String[0], h, nullInput, nullOutput, nullOutput, null);
        
        assertEquals ("Returns 2 as afterFinish needed to be set to 2 before" +
        " calling finishInitialization", 2, res.getExitCode ());

        long time = System.currentTimeMillis ();
        task.waitFinished ();
        time = System.currentTimeMillis () - time;
        
        if (time > 1000) {
            fail ("The waitFinished should return almost immediatelly. But was: " + time);
        }
        
        if (h.afterFinish != h.cnt) {
            // in order to find out whether the failures in issue #44833
            // are not caused just by threading issues, let's wait another
            // few seconds and print the results of h.afterFinish and h.cnt
            // if they will be the same then just replace the initial condition
            // by say that h.afterFinish == 2 and h.cnt > 1 is ok
            Thread.sleep (5000);
            fail ("H is not executed before finishInitialization is called :" + h.afterFinish + " cnt: " + h.cnt);
        }
        
    }
    
    public void testServerIsNotBlockedByLongRequests() throws Exception {
        class H extends CLIHandler {
            private int cnt = -1;
            public int toReturn;
            
            public H() {
                super(CLIHandler.WHEN_INIT);
            }
            
            protected synchronized int cli(Args args) {
                try {
                    // this simulates really slow, but computing task
                    Thread.sleep (6555);
                } catch (InterruptedException ex) {
                    throw new IllegalStateException ();
                }
                notifyAll();
                cnt++;
                return toReturn;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h = new H();
        
        h.toReturn = 7;
        final Integer blockOn = new Integer(99);
        CLIHandler.Status res = cliInitialize(new String[0], h, nullInput, nullOutput, nullOutput, blockOn);
        assertEquals("Called once, increased -1 to 0", 0, h.cnt);
        assertEquals("Result is provided by H", 7, res.getExitCode());
        
        // blocks after connection established, before returning the result
        class R implements Runnable {
            CLIHandler.Status res;
            public void run() {
                res = cliInitialize(new String[0], Collections.<CLIHandler>emptyList(), nullInput, nullOutput, nullOutput, blockOn);
            }
        }
        R r = new R();
        RequestProcessor.Task task;
        synchronized (h) {
            h.toReturn = 5;
            task = new org.openide.util.RequestProcessor("Blocking request").post(r);
            h.wait();
            assertEquals("Connects to the h", 1, h.cnt);
            if (r.res != null) {
                fail ("The handler should not be finished, as it blocks in '99' but it is and the result is " + r.res.getExitCode ());
            }
        }
        
        // while R is blocked, run another task
        h.toReturn = 0;
        res = cliInitialize(new String[0], Collections.<CLIHandler>emptyList(), nullInput, nullOutput, nullOutput, null);
        assertEquals("Called once, increased to 2", 2, h.cnt);
        assertEquals("Result is provided by H, H gives 0, changes into -1 right now", -1, res.getExitCode());
        
        synchronized (blockOn) {
            // let the R task go on
            blockOn.notifyAll();
        }
        task.waitFinished();
        assertNotNull("Now it is finished", r.res);
        assertEquals("Result is -1, if this fails: this usually means that the server is blocked by some work and the task R started new server to handle its request",
            5, r.res.getExitCode());
        assertEquals("H called three times (but counting from -1)", 2, h.cnt);
    }
    
    public void testReadingOfInputWorksInHandler() throws Exception {
        final byte[] template = { 1, 2, 3, 4 };
        
        class H extends CLIHandler {
            private byte[] arr;
            
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                try {
                    InputStream is = args.getInputStream();
                    arr = new byte[is.available() / 2];
                    if (arr.length > 0) {
                        assertEquals("Read amount is the same", arr.length, is.read(arr));
                    }
                    is.close();
                } catch (IOException ex) {
                    fail("There is an exception: " + ex);
                }
                return 0;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        H h2 = new H();
        
        // why twice? first attempt is direct, second thru the socket server
        for (int i = 0; i < 2; i++) {
            CLIHandler.Status res = cliInitialize(
                new String[0], new H[] { h1, h2 }, new ByteArrayInputStream(template), nullOutput, nullOutput);
            
            assertNotNull("Attempt " + i + ": " + "Can be read", h1.arr);
            assertEquals("Attempt " + i + ": " + "Read two bytes", 2, h1.arr.length);
            assertEquals("Attempt " + i + ": " + "First is same", template[0], h1.arr[0]);
            assertEquals("Attempt " + i + ": " + "Second is same", template[1], h1.arr[1]);
            
            assertNotNull("Attempt " + i + ": " + "Can read as well", h2.arr);
            assertEquals("Attempt " + i + ": " + "Just one char", 1, h2.arr.length);
            assertEquals("Attempt " + i + ": " + "And is the right one", template[2], h2.arr[0]);
            
            h1.arr = null;
            h2.arr = null;
        }
    }
    
    public void testReadingMoreThanAvailableIsOk () throws Exception {
        final byte[] template = { 1, 2, 3, 4 };
        
        class H extends CLIHandler {
            private byte[] arr;
            Exception error;
            
            public H() {
                super(WHEN_INIT);
            }
            
            @Override
            protected int cli(Args args) {
                try {
                    InputStream is = args.getInputStream();
                    arr = new byte[8];
                    assertEquals("Read amount is the maximum of template", template.length, is.read(arr));
                    assertEquals("EOF", -1, is.read());
                    is.close();
                } catch (Exception ex) {
                    error = ex;
                }
                return 0;
            }
            
            @Override
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        
        // why twice? first attempt is direct, second thru the socket server
        for (int i = 0; i < 2; i++) {
            CLIHandler.Status res = cliInitialize(
                new String[0], new H[] { h1 }, new ByteArrayInputStream(template), nullOutput, nullOutput);
            
            assertNotNull("Attempt " + i + ": " + "Can be read", h1.arr);
            assertEquals("Attempt " + i + ": " + "First is same", template[0], h1.arr[0]);
            assertEquals("Attempt " + i + ": " + "Second is same", template[1], h1.arr[1]);
            assertEquals("Attempt " + i + ": " + "3rd is same", template[2], h1.arr[2]);
            assertEquals("Attempt " + i + ": " + "4th is same", template[3], h1.arr[3]);
            
            h1.arr = null;
        }
        if (h1.error != null) {
            throw h1.error;
        }
    }
    
    
    public void testWritingToOutputIsFine() throws Exception {
        final byte[] template = { 1, 2, 3, 4 };
        
        class H extends CLIHandler {
            public H() {
                super(WHEN_INIT);
            }
            
            protected int cli(Args args) {
                try {
                    OutputStream os = args.getOutputStream();
                    os.write(template);
                    os.close();
                    
                    os = args.getErrorStream();
                    os.write(0); 
                    os.write(1);
                    os.write(2);
                    os.write(3);
                    os.write(4);
                    os.close();
                } catch (IOException ex) {
                    fail("There is an exception: " + ex);
                }
                return 0;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h1 = new H();
        H h2 = new H();
        
        // why twice? first attempt is direct, second thru the socket server
        for (int i = 0; i < 2; i++) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            
            CLIHandler.Status res = cliInitialize(
                new String[0], new H[] { h1, h2 }, nullInput, os, err);
            
            byte[] arr = os.toByteArray();
            assertEquals("Double size of template", template.length * 2, arr.length);
            
            for (int pos = 0; pos < arr.length; pos++) {
                assertEquals(pos + ". is the same", template[pos % template.length], arr[pos]);
            }
            
            byte[] errArr = err.toByteArray();
            assertEquals("5 bytes", 10, errArr.length);
            for (int x = 0; x < errArr.length; x++) {
                assertEquals(errArr[x], x % 5);
            }
        }
    }

    public void testGetInetAddressDoesNotBlock () throws Exception {
        CLIHandler.Status res = cliInitialize(new String[0], Collections.<CLIHandler>emptyList(), nullInput, nullOutput, nullOutput, Integer.valueOf(27));
        assertEquals("CLIHandler init finished" ,0, res.getExitCode());
    }
    
    public void testServerCanBeStopped () throws Exception {
        class H extends CLIHandler {
            private int cnt = -1;
            public int toReturn;
            
            public H() {
                super(CLIHandler.WHEN_INIT);
            }
            
            protected synchronized int cli(Args args) {
                notifyAll();
                cnt++;
                return toReturn;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h = new H();
        
        h.toReturn = 7;
        CLIHandler.Status res = cliInitialize(new String[0], h, nullInput, nullOutput, nullOutput, null);
        assertEquals("Called once, increased -1 to 0", 0, h.cnt);
        assertEquals("Result is provided by H", 7, res.getExitCode());
        
        CLIHandler.stopServer ();
        
        h.toReturn = 5;
        h.cnt = -1;
        res = cliInitialize(new String[0], Collections.<CLIHandler>emptyList(), nullInput, nullOutput, nullOutput, null);
        assertEquals("Not called -1", -1, h.cnt);
        // right now the handler will not be called, if there is anything else
        // to do, let's wait for such requirements
    }

    public void testHostNotFound64004 () throws Exception {
        class H extends CLIHandler {
            private int cnt;
            public int toReturn;
            
            public H() {
                super(CLIHandler.WHEN_INIT);
            }
            
            protected synchronized int cli(Args args) {
                notifyAll();
                cnt++;
                return toReturn;
            }
            
            protected void usage(PrintWriter w) {}
        }
        H h = new H();
        
        // handlers shall not be executed immediatelly
        CLIHandler.finishInitialization(true);
        
        h.toReturn = 7;
        CLIHandler.Status res = cliInitialize(new String[0], h, nullInput, nullOutput, nullOutput, new Integer(667));
        // finish all calls
        int newRes = CLIHandler.finishInitialization(false);
        
        assertEquals("Called once, increased", 1, h.cnt);
        assertEquals("Result is provided by H", 7, newRes);
    }
    
    //
    // Utility methods
    //
    
    private static CLIHandler.Status cliInitialize(String[] args, CLIHandler handler, InputStream is, OutputStream os, OutputStream err, Integer lock) {
        return cliInitialize(args, handler, is, os, err, lock, System.getProperty ("user.dir"));
    }
    private static CLIHandler.Status cliInitialize(String[] args, CLIHandler handler, InputStream is, OutputStream os, OutputStream err, Integer lock, String currentDir) {
        return cliInitialize(args, Collections.nCopies(1, handler), is, os, err, lock, currentDir);
    }
    private static CLIHandler.Status cliInitialize(String[] args, CLIHandler[] arr, InputStream is, OutputStream os, OutputStream err) {
        return cliInitialize(args, Arrays.asList(arr), is, os, err, null);
    }
    private static CLIHandler.Status cliInitialize(String[] args, List<? extends CLIHandler> coll, InputStream is, OutputStream os, java.io.OutputStream err, Integer lock) {
        return cliInitialize (args, coll, is, os, err, lock, System.getProperty ("user.dir"));
    }
    private static CLIHandler.Status cliInitialize(String[] args, List<? extends CLIHandler> coll, InputStream is, OutputStream os, java.io.OutputStream err, Integer lock, String currentDir) {
        return CLIHandler.initialize(new CLIHandler.Args(args, is, os, err, currentDir), lock, coll, false, true, null);
    }
    
    private static final class InitializeRunner extends Object implements Runnable {
        private final Integer block;
        private String[] args;
        private CLIHandler handler;
        private CLIHandler.Status result;
        private boolean noEnd;
        
        public InitializeRunner(int till) throws InterruptedException {
            this(new String[0], null, till);
        }

        public InitializeRunner(int till, boolean noEnd) throws InterruptedException {
            this(new String[0], null, till, noEnd);
        }
        
        public InitializeRunner(Integer till, boolean noEnd) throws InterruptedException {
            this(new String[0], null, till, noEnd);
        }
        public InitializeRunner(String[] args, CLIHandler h, int till) throws InterruptedException {
            this(args, h, new Integer(till));
        }
        public InitializeRunner(String[] args, CLIHandler h, Integer till) throws InterruptedException {
            this(args, h, till, false);
        }

        private InitializeRunner(String[] args, CLIHandler h, Integer till, boolean noEnd) throws InterruptedException {
            this.args = args;
            this.block = till;
            this.handler = h;
            this.noEnd = noEnd;
            
            synchronized (block) {
                new RequestProcessor("InitializeRunner blocks on " + till).post(this);
                block.wait();
            }
        }
        
        public void run() {
            synchronized (block) {
                result = CLIHandler.initialize(
                    new CLIHandler.Args(args, nullInput, nullOutput, nullOutput, ""),
                    block,
                    handler == null ? Collections.<CLIHandler>emptyList() : Collections.nCopies(1, handler),
                    false,
                    true,
                    null
                );
                if (!noEnd) {
                    // we are finished, wake up guys in next() if any
                    block.notifyAll();
                }
            }
            synchronized (this) {
                notifyAll();
            }
        }
        
        /** Executes the code to next invocation */
        public void next() throws InterruptedException {
            synchronized (block) {
                block.notify();
                block.wait();
            }
        }
        
        /** Has already the resutl?
         */
        public boolean hasResult() {
            return result != null;
        }
        
        public boolean waitResult() throws InterruptedException {
            synchronized (this) {
                for (int i = 0; i < 10; i++) {
                    if (result != null) {
                        return true;
                    }
                    wait(1000);
                }
            }
            fail("No result produced: " + result);
            return true;
        }
        
        /** Gets the resultFile, if there is some,
         */
        public File resultFile() {
            if (result == null) {
                fail("No result produced");
            }
            return result.getLockFile();
        }
        
        /** Gets the port, if there is some,
         */
        public int resultPort() {
            if (result == null) {
                fail("No result produced");
            }
            return result.getServerPort();
        }
    } // end of InitializeRunner
    
}
