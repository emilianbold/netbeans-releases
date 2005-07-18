/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.windows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import javax.swing.Action;
import org.openide.util.Lookup;

/** A factory for IO tabs shown in the output window.  To create a new tab to
 * write to, call e.g. <code>IOProvider.getDefault().getIO("MyTab", false)</code>
 * (pass true if there may be an existing tab with the same name and you want
 * to write to a new tab).
 *
 * @author Jesse Glick
 * @since 3.14
 */
public abstract class IOProvider {

    /**
     * Get the default I/O provider.
     * <p>
     * Normally this is taken from {@link Lookup#getDefault} but if there is no
     * instance in lookup, a fallback instance is created which just uses the
     * standard system I/O streams. This is useful for unit tests and perhaps
     * for standalone usage of various libraries.
     * <p>
     * Assuming you want to ensure that a real output window implementation is actually
     * installed and enabled, you must require the token <code>org.openide.windows.IOProvider</code>
     * from the module calling this method.
     * @return the default instance (never null)
     */
    public static IOProvider getDefault() {
        IOProvider iop = (IOProvider) Lookup.getDefault().lookup(IOProvider.class);
        if (iop == null) {
            iop = new Trivial();
        }
        return iop;
    }

    /** Subclass constructor. */
    protected IOProvider() {}

    /** 
     * Get a named instance of InputOutput, which represents an output tab in
     * the output window.  Streams for reading/writing can be accessed via
     * getters on the returned instance.
     *
     * @param name A localized display name for the tab
     * @param newIO if <tt>true</tt>, a new <code>InputOutput</code> is returned, else an existing <code>InputOutput</code> of the same name may be returned
     * @return an <code>InputOutput</code> instance for accessing the new tab
     * @see InputOutput
     */
    public abstract InputOutput getIO(String name, boolean newIO);

    
    /** 
     *Gets a named instance of InputOutput with additional actions displayed in the
     * toolbar.
     * Streams for reading/writing can be accessed via
     * getters on the returned instance. 
     * Additional actions are displayed on the output's toolbar.
     *
     * @param name A localized display name for the tab
     * @param additionalActions array of actions that are added to the toolbar, Can be empty array, but not null.
     *   The number of actions should not exceed 5 and each should have the <code>Action.SMALL_ICON</code> property defined.
     * @return an <code>InputOutput</code> instance for accessing the new tab
     * @see InputOutput
     * @since 1.6 <br>
     * Note: The method is non-abstract for backward compatibility reasons only. If you are
     * extending <code>IOProvider</code> and implementing its abstract classes, you are encouraged to override
     * this method as well. The default implementation falls back to the <code>getIO(name, newIO)</code> method, ignoring the actions passed.
     */
    public InputOutput getIO(String name, Action[] additionalActions) {
        return getIO(name, true);
    }
    
    /** Support writing to the Output Window on the main tab or a similar output device.
     * @return a writer for the standard NetBeans output area
     */
    public abstract OutputWriter getStdOut();
    
    /** Fallback implementation. */
    private static final class Trivial extends IOProvider {
        
        public Trivial() {}

        public InputOutput getIO(String name, boolean newIO) {
            return new TrivialIO(name);
        }

        public OutputWriter getStdOut() {
            return new TrivialOW(System.out, "stdout"); // NOI18N
        }
        
        private final class TrivialIO implements InputOutput {
            
            private final String name;
            private Reader in;
            
            public TrivialIO(String name) {
                this.name = name;
            }

            public Reader getIn() {
                if (in == null) {
                    in = new BufferedReader(new InputStreamReader(System.in));
                }
                return in;
            }

            public OutputWriter getOut() {
                return new TrivialOW(System.out, name);
            }

            public OutputWriter getErr() {
                return new TrivialOW(System.err, name);
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
        
        private static final class TrivialOW extends OutputWriter {
            
            private static int count = 0;
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

            public void println(float x) {
                prefix(false);
                stream.println(x);
            }

            public void println(double x) {
                prefix(false);
                stream.println(x);
            }

            public void println() {
                prefix(false);
                stream.println();
            }

            public void println(Object x) {
                prefix(false);
                stream.println(x);
            }

            public void println(int x) {
                prefix(false);
                stream.println(x);
            }

            public void println(char x) {
                prefix(false);
                stream.println(x);
            }

            public void println(long x) {
                prefix(false);
                stream.println(x);
            }

            public void println(char[] x) {
                prefix(false);
                stream.println(x);
            }

            public void println(boolean x) {
                prefix(false);
                stream.println(x);
            }

            public void println(String x) {
                prefix(false);
                stream.println(x);
            }
            
        }
        
    }

}
