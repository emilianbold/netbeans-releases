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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.core.startup.Main;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * Logging event queue that can report problems about too long execution times
 * 
 * 
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
final class TimableEventQueue extends EventQueue 
implements Runnable {
    private static final Logger LOG = Logger.getLogger(TimableEventQueue.class.getName());
    private static final RequestProcessor RP = new RequestProcessor("Timeable Event Queue Watch Dog", 1, true); // NOI18N
    private static final int QUANTUM = Integer.getInteger("org.netbeans.core.TimeableEventQueue.quantum", 100); // NOI18N
    private static final int REPORT = Integer.getInteger("org.netbeans.core.TimeableEventQueue.report", 3000); // NOI18N
    private static final int PAUSE = Integer.getInteger("org.netbeans.core.TimeableEventQueue.pause", 15000); // NOI18N
    private static final int CLEAR = Integer.getInteger("org.netbeans.core.TimeableEventQueue.clear", 60000); // NOI18N

    private final RequestProcessor.Task TIMEOUT;
    private volatile long ignoreTill;
    private volatile long start;
    private volatile ActionListener stoppable;
    private volatile boolean isWaitCursor;

    public TimableEventQueue() {
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
                    Toolkit.getDefaultToolkit().getSystemEventQueue().push(new TimableEventQueue());
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
            tick("dispatchEvent"); // NOI18N
            super.dispatchEvent(event);
        } finally {
            done();
        }
    }

    private void done() {
        TIMEOUT.cancel();
        LOG.log(Level.FINE, "isWait cursor {0}", isWaitCursor); // NOI18N
        long r = isWaitCursor ? REPORT * 10 : REPORT;
        isWaitCursor = false;
        long time = System.currentTimeMillis() - start;
        if (time > QUANTUM) {
            LOG.log(Level.FINE, "done, timer stopped, took {0}", time); // NOI18N
            if (time > r) {
                LOG.log(Level.WARNING, "too much time in AWT thread {0}", stoppable); // NOI18N
                ActionListener ss = stoppable;
                if (ss != null) {
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(out);
                        ss.actionPerformed(new ActionEvent(dos, 0, "write")); // NOI18N
                        dos.close();
                        if (dos.size() > 0) {
                            Object[] params = new Object[]{out.toByteArray(), time};
                            Logger.getLogger("org.netbeans.ui.performance").log(Level.CONFIG, "Slowness detected", params);
                        } else {
                            LOG.log(Level.WARNING, "no snapshot taken"); // NOI18N
                        }
                        stoppable = null;
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    ignoreTill = System.currentTimeMillis() + PAUSE;
                }
            }
        } else {
            LOG.log(Level.FINEST, "done, timer stopped, took {0}", time);
        }
        ActionListener ss = stoppable;
        if (ss != null) {
            ss.actionPerformed(new ActionEvent(this, 0, "cancel")); // NOI18N
            stoppable = null;
        }
        return;
    }

    private void tick(String name) {
        start = System.currentTimeMillis();
        if (start >= ignoreTill && WindowManager.getDefault().getMainWindow().isShowing()) {
            LOG.log(Level.FINEST, "tick, schedule a timer for {0}", name);
            TIMEOUT.schedule(QUANTUM);
        }
    }

    public void run() {
        if (stoppable != null) {
            LOG.log(Level.WARNING, "Still previous controller {0}", stoppable);
            return;
        }
        Runnable selfSampler = (Runnable)createSelfSampler();
        if (selfSampler != null) {
            selfSampler.run();
            stoppable = (ActionListener)selfSampler;
        }
        isWaitCursor |= isWaitCursor();
    }

    private static Object createSelfSampler() {
        FileObject fo = FileUtil.getConfigFile("Actions/Profile/org-netbeans-modules-profiler-actions-SelfSamplerAction.instance");
        if (fo == null) {
            return null;
        }
        Action a = (Action)fo.getAttribute("delegate"); // NOI18N
        if (a == null) {
            return null;
        }
        return a.getValue("logger-awt"); // NOI18N
    }

    private static boolean isWaitCursor() {
        Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focus != null) {
            if (focus.getCursor().getType() == Cursor.WAIT_CURSOR) {
                LOG.finer("wait cursor on focus owner"); // NOI18N
                return true;
            }
            Window w = SwingUtilities.windowForComponent(focus);
            if (w != null && w.getCursor().getType() == Cursor.WAIT_CURSOR) {
                LOG.finer("wait cursor on window"); // NOI18N
                return true;
            }
        }
        for (Frame f : Frame.getFrames()) {
            if (f.getCursor().getType() == Cursor.WAIT_CURSOR) {
                LOG.finer("wait cursor on frame"); // NOI18N
                return true;
            }
        }
        LOG.finest("no wait cursor"); // NOI18N
        return false;
    }

        /*
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
    */
}
