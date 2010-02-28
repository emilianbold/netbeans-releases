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

package org.netbeans.modules.cnd.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 *
 * @author Vladimir Kvashin
 */
@ServiceProvider(service=org.openide.windows.IOProvider.class, position=0)
public class CndTestIOProvider extends IOProvider {

    public interface Listener {
        public void linePrinted(String line);
    }

    private static final Reader in = new BufferedReader(new InputStreamReader(System.in));
    private static final PrintStream out = System.out;
    private static final PrintStream err = System.err;
    private List<Listener> listeners = new ArrayList();

    public CndTestIOProvider() {
    }

    @Override
    public String getName() {
        return "CndTestIOProvider";
    }


    public InputOutput getIO(String name, boolean newIO) {
        return new TrivialIO(name);
    }

    public OutputWriter getStdOut() {
        return new TrivialOW(out, "stdout"); // NOI18N
    }

    public void addListener(Listener listener) {
        synchronized (this) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        synchronized (this) {
            listeners.remove(listener);
        }
    }

    private void fireLinePrinted(String line) {
        Listener[] la = null;
        synchronized (this) {
            if (!listeners.isEmpty()) {
                la = new Listener[listeners.size()];
                listeners.toArray(la);
            }
        }
        if (la != null) {
            for (int i = 0; i < la.length; i++) {
                la[i].linePrinted(line);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private final class TrivialIO implements InputOutput {

        private final String name;

        public TrivialIO(String name) {
            this.name = name;
        }

        public Reader getIn() {
            return in;
        }

        public OutputWriter getOut() {
            return new TrivialOW(out, name);
        }

        public OutputWriter getErr() {
            return new TrivialOW(err, name);
        }

        public Reader flushReader() {
            return getIn();
        }

        public boolean isClosed() {
            return false;
        }

        public boolean isErrSeparated() {
            return false;
        }

        public boolean isFocusTaken() {
            return false;
        }

        public void closeInputOutput() {}

        public void select() {}

        public void setErrSeparated(boolean value) {}

        public void setErrVisible(boolean value) {}

        public void setFocusTaken(boolean value) {}

        public void setInputVisible(boolean value) {}

        public void setOutputVisible(boolean value) {}

    }

    private static int count = 0;

    private final class TrivialOW extends OutputWriter {
        
        private final String name;
        private final PrintStream stream;

        public TrivialOW(PrintStream stream, String name) {
            // XXX using super(new PrintWriter(stream)) does not seem to work for some reason!
            super(new StringWriter());
            this.stream = stream;
            if (name != null) {
                this.name = name;
            } else {
                this.name = "anon-" + ++count; // NOI18N
            }
        }

        private void prefix(boolean hyperlink) {
            if (hyperlink) {
                stream.print("[" + name + "]* "); // NOI18N
            } else {
                stream.print("[" + name + "]  "); // NOI18N
            }
        }

        public void println(String s, OutputListener l) throws IOException {
            prefix(l != null);
            stream.println(s);
        }

        public void reset() throws IOException {}

        @Override
        public void println(float x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(double x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println() {
            fireLinePrinted("");
            prefix(false);
            stream.println();
        }

        @Override
        public void println(Object x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(int x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(char x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(long x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(char[] x) {
            fireLinePrinted(String.copyValueOf(x));
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(boolean x) {
            fireLinePrinted("" + x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void println(String x) {
            fireLinePrinted(x);
            prefix(false);
            stream.println(x);
        }

        @Override
        public void write(int c) {
            fireLinePrinted("" + c);
            stream.write(c);
        }

        @Override
        public void write(char[] buf, int off, int len) {
            String s = new String(buf, off, len);
            if (s.endsWith("\n")) {
                println(s.substring(0, s.length() - 1));
            } else {
                try {
                    fireLinePrinted(s); // is it worth to write something smarter?
                    stream.write(s.getBytes());
                } catch (IOException x) {}
            }
        }

        @Override
        public void write(String s, int off, int len) {
            s = s.substring(off, off + len);
            if (s.endsWith("\n")) {
                println(s.substring(0, s.length() - 1));
            } else {
                try {
                    fireLinePrinted(s); // is it worth to write something smarter?
                    stream.write(s.getBytes());
                } catch (IOException x) {}
            }
        }

    }


}
