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
import java.awt.Toolkit;
import java.util.EmptyStackException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.core.startup.Main;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;

/**
 * Logging event queue that can report problems about too long execution times
 * 
 * 
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
final class TimeableEventQueue extends EventQueue 
implements Runnable {
    private static final Logger LOG = Logger.getLogger(TimeableEventQueue.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("Timeable Event Queue Watch Dog", 1, true); // NOI18N
    private static final int QUANTUM = Integer.getInteger("org.netbeans.core.TimeableEventQueue.quantum", 5000); // NOI18N
    private static final int PAUSE = Integer.getInteger("org.netbeans.core.TimeableEventQueue.pause", 60000); // NOI18N

    
    private final RequestProcessor.Task TIMEOUT;
    private volatile Map<Thread, StackTraceElement[]> stack;
    private volatile long ignoreTill;
    private volatile long start;
    

    public TimeableEventQueue() {
        TIMEOUT = RP.create(this);
        TIMEOUT.setPriority(Thread.MIN_PRIORITY);
    }

    static void initialize() {
        // #28536: make sure a JRE bug does not prevent the event queue from having
        // the right context class loader
        // and #35470: do it early, before any module-loaded AWT code might run
        // and #36820: even that isn't always early enough, so we need to push
        // a new EQ to enforce the context loader
        // XXX this is a hack!
        try {
            Mutex.EVENT.writeAccess (new Mutex.Action<Void>() {
                public Void run() {
                    Thread.currentThread().setContextClassLoader(Main.getModuleSystem().getManager().getClassLoader());
                    Toolkit.getDefaultToolkit().getSystemEventQueue().push(new TimeableEventQueue());
                    LOG.fine("Initialization done");
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void dispatchEvent(AWTEvent event) {
        try {
            tick();
            super.dispatchEvent(event);
        } finally {
            done();
        }
    }

    @Override
    public AWTEvent getNextEvent() throws InterruptedException {
        try {
            tick();
            return super.getNextEvent();
        } finally {
            done();
        }
    }

    @Override
    public synchronized AWTEvent peekEvent() {
        try {
            tick();
            return super.peekEvent();
        } finally {
            done();
        }
    }

    @Override
    public synchronized AWTEvent peekEvent(int id) {
        try {
            tick();
            return super.peekEvent(id);
        } finally {
            done();
        }
    }

    @Override
    protected void pop() throws EmptyStackException {
        try {
            tick();
            super.pop();
        } finally {
            done();
        }
    }

    @Override
    public void postEvent(AWTEvent theEvent) {
        try {
            tick();
            super.postEvent(theEvent);
        } finally {
            done();
        }
    }

    @Override
    public synchronized void push(EventQueue newEventQueue) {
        try {
            tick();
            super.push(newEventQueue);
        } finally {
            done();
        }
    }

    private void done() {
        stack = null;
        TIMEOUT.cancel();
        long time = System.currentTimeMillis() - start;
        if (time > 50) {
            LOG.log(Level.FINE, "done, timer stopped, took {0}", time);
        } else {
            LOG.log(Level.FINEST, "done, timer stopped, took {0}", time);
        }
    }

    private void tick() {
        stack = null;
        start = System.currentTimeMillis();
        if (start >= ignoreTill) {
            LOG.log(Level.FINEST, "tick, schedule a timer at {0}", start);
            TIMEOUT.schedule(QUANTUM);
        }
    }

    public void run() {
        stack = Thread.getAllStackTraces();
        LOG.log(Level.FINER, "timer running");
        for (int i = 0; i < 10; i++) {
            if (Thread.interrupted()) {
                LOG.log(Level.FINER, "timer cancelled");
                return;
            }
            Thread.yield();
            System.gc();
            System.runFinalization();
        }
        final Map<Thread, StackTraceElement[]> myStack = stack;
        if (myStack == null) {
            LOG.log(Level.FINER, "timer cancelled");
            return;
        }
        
        long now = System.currentTimeMillis();
        ignoreTill = now + PAUSE;
        long howLong = now - start;
        
//        Logger UI_LOG = Logger.getLogger("org.netbeans.ui.performance"); // NOI18N
        LogRecord rec = new LogRecord(Level.INFO, "LOG_EventQueueBlocked"); // NOI18N
        rec.setParameters(new Object[] { howLong });
        EQException eq = new EQException(myStack);
        rec.setThrown(eq);
        rec.setResourceBundleName("org.netbeans.core.Bundle"); // NOI18N
        rec.setResourceBundle(ResourceBundle.getBundle("org.netbeans.core.Bundle")); // NOI18N
//        UI_LOG.log(rec);
        LOG.log(rec);
    }
    
    private static final class EQException extends Exception {
        private volatile Map<Thread, StackTraceElement[]> stack;

        public EQException(Map<Thread, StackTraceElement[]> stack) {
            this.stack = stack;
            for (Map.Entry<Thread, StackTraceElement[]> en : stack.entrySet()) {
                if (en.getKey().getName().indexOf("AWT-EventQueue") >= 0) {
                    setStackTrace(en.getValue());
                    break;
                }
            }
        }
    
        @Override
        public String getMessage() {
            return threadDump("AWT Event Queue Thread Blocked", stack); // NOI18N
        }
        
        private static void appendThread(StringBuilder sb, String indent, Thread t, Map<Thread,StackTraceElement[]> data) {
            sb.append(indent).append("Thread ").append(t.getName()).append('\n');
            indent = indent.concat("  ");
            StackTraceElement[] arr = data.get(t);
            if (arr != null) {
                for (StackTraceElement e : arr) {
                    sb.append(indent).append(e.getClassName()).append('.').append(e.getMethodName())
                            .append(':').append(e.getLineNumber()).append('\n');
                }
            } else {
                sb.append(indent).append("no stacktrace info"); // NOI18N
            }
        }

        private static void appendGroup(StringBuilder sb, String indent, ThreadGroup tg, Map<Thread,StackTraceElement[]> data) {
            sb.append(indent).append("Group ").append(tg.getName()).append('\n');
            indent = indent.concat("  ");

            int groups = tg.activeGroupCount();
            ThreadGroup[] chg = new ThreadGroup[groups];
            tg.enumerate(chg, false);
            for (ThreadGroup inner : chg) {
                if (inner != null) appendGroup(sb, indent, inner, data);
            }

            int threads = tg.activeCount();
            Thread[] cht= new Thread[threads];
            tg.enumerate(cht, false);
            for (Thread t : cht) {
                if (t != null) appendThread(sb, indent, t, data);
            }
        }

        private static String threadDump(String msg, Map<Thread,StackTraceElement[]> all) {
            ThreadGroup root = Thread.currentThread().getThreadGroup();
            while (root.getParent() != null) root = root.getParent();

            StringBuilder sb = new StringBuilder();
            sb.append(msg).append('\n');
            appendGroup(sb, "", root, all);
            sb.append('\n').append("---");
            return sb.toString();
        }
        
    }
}
