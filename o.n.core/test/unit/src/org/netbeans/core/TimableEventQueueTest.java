/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.core;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class TimableEventQueueTest extends NbTestCase {
    static {
        System.setProperty("org.netbeans.core.TimeableEventQueue.quantum", "300");
        System.setProperty("org.netbeans.core.TimeableEventQueue.pause", "3000");
        TimableEventQueue.initialize();
    }
    private static CountingHandler handler = new CountingHandler();
    
    
    public TimableEventQueueTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINEST;
    }

    @Override
    protected void setUp() throws Exception {
        Logger l = Logger.getLogger("org.netbeans.core");
        l.setUseParentHandlers(false);
        l.removeHandler(handler);
        l.addHandler(handler);
        handler.records.clear();
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testDispatchEvent() throws Exception {
        class Slow implements Runnable {
            private int ok;
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                ok++;
            }
        }
        Slow slow = new Slow();
        
        EventQueue.invokeAndWait(slow);
        EventQueue.invokeAndWait(slow);
        
        assertEquals("called", 2, slow.ok);
        
        assertEquals("Only One report:" + handler.records, 1, handler.records.size());
        LogRecord r = handler.records.get(0);
        
        assertNotNull("Exception present", r.getThrown());
        if (r.getThrown().getMessage().indexOf("Slow") == -1) {
            fail("There should be stacktrace from slow:\n" + r.getThrown().getMessage());
        }
        boolean found = false;
        for (StackTraceElement stackTraceElement : r.getThrown().getStackTrace()) {
            found = found || stackTraceElement.getClassName().indexOf("Slow") >= 0;
        }
        assertTrue("Slow is in the stack trace", found);
    }

    private static final class CountingHandler extends Handler {
        List<LogRecord> records = Collections.synchronizedList(new LinkedList<LogRecord>());

        @Override
        public void publish(LogRecord record) {
            if (record.getLevel().intValue() >= Level.INFO.intValue()) {
                records.add(record);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
