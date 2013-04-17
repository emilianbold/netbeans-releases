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

package org.netbeans;

import java.io.*;
import java.util.logging.Level;
import org.netbeans.junit.*;
import java.util.*;
import java.util.logging.Logger;
import org.openide.util.RequestProcessor;
import static org.netbeans.CLIHandlerTest.*;

/**
 * Test the command-line-interface handler.
 * @author Jaroslav Tulach
 */
public class CLIHandlerServerIsNotBlockedTest extends NbTestCase {

    final static ByteArrayInputStream nullInput = new ByteArrayInputStream(new byte[0]);
    final static ByteArrayOutputStream nullOutput = new ByteArrayOutputStream();
    
    private Logger LOG;

    public CLIHandlerServerIsNotBlockedTest(String name) {
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

    @Override
    protected void tearDown() throws Exception {
        CLIHandler.stopServer();
    }
    
    protected @Override Level logLevel() {
        return Level.FINEST;
    }

    protected @Override int timeOut() {
        return 50000;
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
    
}
