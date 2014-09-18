/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.openide.io.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.EventListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.Lookup;

/**
 * A factory for IO tabs. To create a new tab to write to, call e.g.
 * <code>BaseIOProvider.getDefault().getIO("MyTab", false)</code> (pass true if
 * there may be an existing tab with the same name and you want to write to a
 * new tab).
 *
 * @author jhavlin
 */
public abstract class BaseIOProvider {

    public static @NonNull BaseIOProvider getDefault() {
        BaseIOProvider iopb = Lookup.getDefault().lookup(BaseIOProvider.class);
        if (iopb == null) {
            iopb = new Trivial();
        }
        return iopb;
    }

    /**
     * Gets BaseIOProvider of selected name or delegates to getDefault() if none
     * was found.
     *
     * @param name ID of provider.
     * @return The instance corresponding to provided name or default instance
     * if not found.
     */
    public static @NonNull BaseIOProvider get(@NonNull String name) {
        Collection<? extends BaseIOProvider> res = Lookup.getDefault().lookupAll(
                BaseIOProvider.class);
        for (BaseIOProvider iop : res) {
            if (iop.getName().equals(name)) {
                return iop;
            }
        }
        return getDefault();
    }

    /**
     * Gets name (ID) of provider
     *
     * @return name of provider
     */
    public abstract @NonNull String getName();

    /**
     * Get a named instance of {@link BaseInputOutput}, which represents an
     * output tab in the output window. Streams for reading/writing can be
     * accessed via getters on the returned instance.
     *
     * @param name A localised display name for the tab.
     * @param newIO If <tt>true</tt>, a new {@link BaseInputOutput} is returned,
     * else an existing {@link BaseInputOutput} of the same name may be
     * returned.
     * @param context Various context objects that may be required by specific
     * implementations of the provider.
     * @param actions Objects that specify actions available in the I/O tab,
     * usually {@link javax.swing.Action} instances, possibly
     * {@link BaseOutputTag} instances, depending on what the BaseIOProvider
     * supports.
     * @return An {@link BaseInputOutput} instance for accessing the new tab.
     * @see BaseInputOutput
     */
    public abstract @NonNull
    BaseInputOutput getIO(
            @NonNull String name, boolean newIO,
            @NullAllowed Lookup context, EventListener... actions);

    /**
     * Check whether this implementation supports action type {@code cls}.
     *
     * @param cls Action type to check the support for.
     *
     * @return True if the passed type can be used for specifying actions for
     * the output tab, false if the passed type is not supported.
     *
     * @see #getIO(java.lang.String, boolean, java.util.EventListener...)
     */
    public abstract boolean isActionTypeSupported(
            @NonNull Class<? extends EventListener> cls);

    /**
     * Fallback implementation.
     */
    private static final class Trivial extends BaseIOProvider {

        private static final Reader in = new BufferedReader(
                new InputStreamReader(System.in));
        private static final PrintStream out = System.out;
        private static final PrintStream err = System.err;

        public Trivial() {
        }

        @Override
        public String getName() {
            return "Trivial";                                           //NOI18N
        }

        @Override
        public BaseInputOutput getIO(String name, boolean newIO,
                Lookup context, EventListener... actions) {
            return new TrivialIO(name);
        }

        @Override
        public boolean isActionTypeSupported(
                Class<? extends EventListener> cls) {
            return false;
        }

        @SuppressWarnings("deprecation")
        private final class TrivialIO implements BaseInputOutput {

            private final String name;

            public TrivialIO(String name) {
                this.name = name;
            }

            @Override
            public Reader getIn() {
                return in;
            }

            @Override
            public PrintWriter getOut() {
                return new TrivialOW(out, name);
            }

            @Override
            public PrintWriter getErr() {
                return new TrivialOW(err, name);
            }

            public Reader flushReader() {
                return getIn();
            }

            @Override
            public boolean isClosed() {
                return false;
            }

            @Override
            public void closeInputOutput() {
            }

            @Override
            public void reset() {
            }

            @Override
            public Lookup getLookup() {
                return Lookup.EMPTY;
            }
        }

        private static final class TrivialOW extends PrintWriter {

            private static int count = 0;
            private final String name;
            private final PrintStream stream;

            @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
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

            @Override
            public void println(float x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println(double x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println() {
                prefix(false);
                stream.println();
            }

            @Override
            public void println(Object x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println(int x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println(char x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println(long x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            @SuppressWarnings("ImplicitArrayToString")
            public void println(char[] x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println(boolean x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void println(String x) {
                prefix(false);
                stream.println(x);
            }

            @Override
            public void write(int c) {
                stream.write(c);
            }

            @Override
            public void write(char[] buf, int off, int len) {
                String s = new String(buf, off, len);
                if (s.endsWith("\n")) {
                    println(s.substring(0, s.length() - 1));
                } else {
                    try {
                        stream.write(s.getBytes());
                    } catch (IOException x) {
                    }
                }
            }

            @Override
            public void write(String s, int off, int len) {
                String sub = s.substring(off, off + len);
                if (sub.endsWith("\n")) {
                    println(sub.substring(0, sub.length() - 1));
                } else {
                    try {
                        stream.write(sub.getBytes());
                    } catch (IOException x) {
                    }
                }
            }
        }
    }
}
