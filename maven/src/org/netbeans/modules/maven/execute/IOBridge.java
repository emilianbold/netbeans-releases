/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.maven.execute;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.io.NullOutputStream;

/**
 *
 * @author jglick - copied from netbeans.org ant module by mkleint
 */
public class IOBridge {
    
    // I/O redirection impl. Keyed by thread group (each Ant process has its own TG).
    // Various Ant tasks (e.g. <java fork="false" output="..." ...>) need the system
    // I/O streams to be redirected to the demux streams of the project so they can
    // be handled properly. Ideally nothing would try to read directly from stdin
    // or print directly to stdout/stderr but in fact some tasks do.
    // Could also pass a custom InputOutput to ExecutionEngine, perhaps, but this
    // seems a lot simpler and probably has the same effect.

    private static int delegating = 0;
    private static InputStream origIn;
    private static PrintStream origOut, origErr;
    private static Map delegateIns = new HashMap();
    private static Map delegateOuts = new HashMap();
    private static Map delegateErrs = new HashMap();
    /** list, not set, so can be reentrant - treated as a multiset */
    private static List suspendedDelegationTasks = new ArrayList();
    
    /**
     * Handle I/O scoping for overlapping project runs.
     * You must call {@link #restoreSystemInOutErr} in a finally block.
     * @param in new temporary input stream for this thread group
     * @param out new temporary output stream for this thread group
     * @param err new temporary error stream for this thread group
     * @see "#36396"
     */
    static synchronized void pushSystemInOutErr(JavaOutputHandler ioput) {
        if (delegating++ == 0) {
            origIn = System.in;
            origOut = System.out;
            origErr = System.err;
            
            System.setIn(new MultiplexInputStream());
            System.setOut(new MultiplexPrintStream(false));
            System.setErr(new MultiplexPrintStream(true));
        }
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        delegateIns.put(tg, ioput.getIn());
        delegateOuts.put(tg, ioput.getOut());
        delegateErrs.put(tg, ioput.getErr());
    }
    
    /**
     * Restore original I/O streams after a call to {@link #pushSystemInOutErr}.
     */
    public static synchronized void restoreSystemInOutErr() {
        assert delegating > 0;
        if (--delegating == 0) {
            System.setIn(origIn);
            System.setOut(origOut);
            System.setErr(origErr);
            origIn = null;
            origOut = null;
            origErr = null;
        }
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        delegateIns.remove(tg);
        delegateOuts.remove(tg);
        delegateErrs.remove(tg);
    }

    /**
     * Temporarily suspend delegation of system I/O streams for the current thread.
     * Useful when running callbacks to IDE code that might try to print to stderr etc.
     * Must be matched in a finally block by {@link #resumeDelegation}.
     * Safe to call when not actually delegating; in that case does nothing.
     * Safe to call in reentrant but not overlapping fashion.
     */
    public static synchronized void suspendDelegation() {
        Thread t = Thread.currentThread();
        //assert delegateOuts.containsKey(t.getThreadGroup()) : "Not currently delegating in " + t;
        // #58394: do *not* check that it does not yet contain t. It is OK if it does; need to
        // be able to call suspendDelegation reentrantly.
        suspendedDelegationTasks.add(t);
    }
    
    /**
     * Resume delegation of system I/O streams for the current thread group
     * after a call to {@link #suspendDelegation}.
     */
    public static synchronized void resumeDelegation() {
        Thread t = Thread.currentThread();
        //assert delegateOuts.containsKey(t.getThreadGroup()) : "Not currently delegating in " + t;
        // This is still valid: suspendedDelegationTasks must have *at least one* copy of t.
        assert suspendedDelegationTasks.contains(t) : "Have not suspended delegation in " + t; //NOI18N
        suspendedDelegationTasks.remove(t);
    }

    
    private static final class MultiplexInputStream extends InputStream {
        
        public MultiplexInputStream() {}
        
        private InputStream delegate() {
            Thread t = Thread.currentThread();
            ThreadGroup tg = t.getThreadGroup();
            while (tg != null && !delegateIns.containsKey(tg)) {
                tg = tg.getParent();
            }
            InputStream is = (InputStream)delegateIns.get(tg);
            if (is != null && !suspendedDelegationTasks.contains(t)) {
                return is;
            } else if (delegating > 0) {
                assert origIn != null;
                return origIn;
            } else {
                // Probably should not happen? But not sure.
                return System.in;
            }
        }
        
        public int read() throws IOException {
            return delegate().read();
        }        
        
        @Override
        public int read(byte[] b) throws IOException {
            return delegate().read(b);
        }
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate().read(b, off, len);
        }
        
        @Override
        public int available() throws IOException {
            return delegate().available();
        }
        
        @Override
        public boolean markSupported() {
            return delegate().markSupported();
        }        
        
        @Override
        public void mark(int readlimit) {
            delegate().mark(readlimit);
        }
        
        @Override
        public void close() throws IOException {
            delegate().close();
        }
        
        @Override
        public long skip(long n) throws IOException {
            return delegate().skip(n);
        }
        
        @Override
        public void reset() throws IOException {
            delegate().reset();
        }
        
    }
    
    private static final class MultiplexPrintStream extends PrintStream {
        
        private final boolean err;
        
        public MultiplexPrintStream(boolean err) {
            this(new NullOutputStream(), err);
        }
        
        private MultiplexPrintStream(NullOutputStream nos, boolean err) {
            super(nos);
            nos.throwException = true;
            this.err = err;
        }
        
        private PrintStream delegate() {
            Thread t = Thread.currentThread();
            ThreadGroup tg = t.getThreadGroup();
            Map delegates = err ? delegateErrs : delegateOuts;
            while (tg != null && !delegates.containsKey(tg)) {
                tg = tg.getParent();
            }
            PrintStream ps = (PrintStream)delegates.get(tg);
            if (ps != null && !suspendedDelegationTasks.contains(t)) {
                return ps;
            } else if (delegating > 0) {
                PrintStream orig = err ? origErr : origOut;
                assert orig != null;
                return orig;
            } else {
                // Probably should not happen? But not sure.
                return err ? System.err : System.out;
            }
        }
        
        @Override
        public boolean checkError() {
            return delegate().checkError();
        }
        
        @Override
        public void close() {
            delegate().close();
        }
        
        @Override
        public void flush() {
            delegate().flush();
        }
        
        @Override
        public void print(long l) {
            delegate().print(l);
        }
        
        @Override
        public void print(char[] s) {
            delegate().print(s);
        }
        
        @Override
        public void print(int i) {
            delegate().print(i);
        }
        
        @Override
        public void print(boolean b) {
            delegate().print(b);
        }
        
        @Override
        public void print(char c) {
            delegate().print(c);
        }
        
        @Override
        public void print(float f) {
            delegate().print(f);
        }
        
        @Override
        public void print(double d) {
            delegate().print(d);
        }
        
        @Override
        public void print(Object obj) {
            delegate().print(obj);
        }
        
        @Override
        public void print(String s) {
            delegate().print(s);
        }
        
        @Override
        public void println(double x) {
            delegate().println(x);
        }
        
        @Override
        public void println(Object x) {
            delegate().println(x);
        }
        
        @Override
        public void println(float x) {
            delegate().println(x);
        }
        
        @Override
        public void println(int x) {
            delegate().println(x);
        }

        @Override
        public void println(char x) {
            delegate().println(x);
        }
        
        @Override
        public void println(boolean x) {
            delegate().println(x);
        }
        
        @Override
        public void println(String x) {
            delegate().println(x);
        }
        
        @Override
        public void println(char[] x) {
            delegate().println(x);
        }
        
        @Override
        public void println() {
            delegate().println();
        }
        
        @Override
        public void println(long x) {
            delegate().println(x);
        }
        
        @Override
        public void write(int b) {
            delegate().write(b);
        }
        
        @Override
        public void write(byte[] b) throws IOException {
            delegate().write(b);
        }
        
        @Override
        public void write(byte[] b, int off, int len) {
            delegate().write(b, off, len);
        }
    }
    
}
