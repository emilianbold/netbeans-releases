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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.extension.openide.util;

import java.io.*;
import java.util.*;

/**
 * Used for logging output to help debug netbeans modules.  Debug messages
 * are logged into a specific category which must be "enabled" in order for
 * the output to make it to the console or logger.
 * <p>
 * The trace messages all return true (always). This makes them suitable
 * as assertions - to take advantage of the conditional processing of
 * assertions. E.g  "assert Trace.trace(compute());". Here the Trace call
 * will only be processed (and its arguments evaluated) when assertions
 * are enabled. Obviously you must run with assertions enabled if you
 * use the Trace library this way.
 *
 * @author Joe Nuxoll
 */
public class Trace extends TraceBase {

    public static PrintStream out = System.err;

    /**
     * This is a global counter variable that is handy to use when tracing
     * repetitive tasks.
     */
    public static int counter = 0;

    /**
     * turns on/off tracing for all categories
     *
     * @param enabled true to enable tracing, false to disable it
     */
    public static void setOutputEnabled(boolean enabled) {
        out = enabled ? System.err : nullPS;
    }
    public static boolean isOutputEnabled() {
        return !(nullPS == out);
    }

    /**
     * Sets the output stream to be used for tracing.  This will
     * assign the static Trace.out variable.
     *
     * @param log The PrintStream to use for Trace.out
     */
    public static void setOutputStream(PrintStream log) {
        out = log;
    }

    /**
     * This enables tracing for the specified category of messages
     *
     * @param category Enable tracing of this category
     */
    public static void enableTraceCategory(String category) {
        traceCategories.add(category);
    }

    /**
     * This enables tracing for the specified category of messages
     *
     * @param category Enable tracing of this category
     */
    public static void enableTraceCategory(Class category) {
        traceCategories.add(category.getName());
    }

    /**
     * Disables tracing for the specified category of messages
     *
     * @param category Remove specified category to disable tracing
     */
    public static void disableTraceCategory(String category) {
        traceCategories.remove(category);
    }

    /**
     * Disables tracing for the specified category of messages
     *
     * @param category Remove specified category to disable tracing
     */
    public static void disableTraceCategory(Class category) {
        traceCategories.remove(category.getName());
    }

    /**
     * Sends a message to Trace.out if the category is enabled and global output is enabled
     *
     * @param category Category to which this message belongs
     * @param message Message to print if the category is enabled
     * @return true
     */
    public static boolean trace(String category, String message) {
        if (traceCategories.contains(category))
            out.println("[" + category + "] " + message);
        return true;
    }

    /**
     * Sends a message to Trace.out if the category is enabled and global output is enabled
     *
     * @param category Category to which this message belongs
     * @param message Message to print if the category is enabled
     * @return true
     */
    public static boolean trace(Class category, String message) {
        trace(category.getName(), message);
        return true;
    }

    /**
     * Prints a stack trace to Trace.out if the category is enabled and global output is enabled
     *
     * @param category Category to which this message belongs
     * @param t Throwable for stack trace to print
     * @return true
     */
    public static boolean trace(String category, Throwable t) {
        if (traceCategories.contains(category))
            t.printStackTrace(out);
        return true;
    }

    /**
     * Prints a stack trace to Trace.out if the category is enabled and global output is enabled
     *
     * @param category Category to which this message belongs
     * @param t Throwable for stack trace to print
     * @return true
     */
    public static boolean trace(Class category, Throwable t) {
        trace(category.getName(), t);
        return true;
    }

    /**
     * Print the specified text to Trace.out if the specified category is enabled, and specified
     * condition evaluates to true, and global output is enabled
     *
     * @param category Category to which this message belongs
     * @param condition If this evaluates to true, the message will be printed
     * @param message Message to print if the category is enabled and the condition evals to true
     * @return true
     */
    public static boolean warnTrace(String category, boolean condition, String message) {
        if (condition && traceCategories.contains(category))
            out.println("warn[" + category + "] " + message);
        return true;
    }

    /**
     * Prints the specified message to Trace.out if the specified category is enabled and specified
     * condition evaluates to true, and global output is enabled
     *
     * @param category Category to which this message belongs
     * @param condition If this evaluates to true, the message will be printed
     * @param message Message to print if the category is enabled and the condition evals to true
     * @return true
     */
    public static boolean warnTrace(Class category, boolean condition, String message) {
        warnTrace(category.getName(), condition, message);
        return true;
    }

    /**
     * Prints the specified message to Trace.out if the specified category is enabled and global
     * output is enabled
     *
     * @param category Category to which this message belongs
     * @param message Message to print if the category is enabled
     * @return true
     */
    static public boolean warnTrace(String category, String message) {
        warnTrace(category, true, message);
        return true;
    }

    /**
     * Prints the specified message to Trace.out if the specified category is enabled and global
     * output is enabled
     *
     * @param category Category to which this message belongs
     * @param message Message to print if the category is enabled
     * @return true
     */
    static public boolean warnTrace(Class category, String message) {
        warnTrace(category.getName(), true, message);
        return true;
    }

    /**
     * Prints a stack trace of the current thread to the Trace.out stream.
     * @return true
     */
    public static boolean printStackTrace() {
        new Exception("Trace.printStackTrace()").printStackTrace(out);
        return true;
    }

    /**
     * Prints the specified exception stack trace to the Trace.out stream.
     *
     * @param t Source Throwable to print a stack trace from
     * @return true
     */
    public static boolean printStackTrace(Throwable t) {
        out.println("Trace.printStackTrace(Thowable) ----------------------8<");
        t.printStackTrace(out);
        out.println("Trace.printStackTrace(Thowable) ----------------------8<");
        return true;
    }

    /**
     * Flushes the Trace.out stream
     * @return true
     */
    public static boolean flush() {
        out.flush();
        return true;
    }

    private static final Trace.NullPrintStream nullPS = new Trace.NullPrintStream();
    private static ArrayList traceCategories = new ArrayList();

    //------------------------------------------------------------------------------
    // NullPrintStream - for blank output
    //------------------------------------------------------------------------------
    private static class NullPrintStream extends PrintStream {
        public NullPrintStream() { super(System.err); }
        public void write(int b) {}
        public void write(byte[] b, int off, int len) {}
        public void flush() {}
        public void close() {}
        public boolean checkError() { return false; }
        public void print(Object obj) {}
        public void print(String s) {}
        public void print(char[] s) {}
        public void print(char c) {}
        public void print(int i) {}
        public void print(long l) {}
        public void print(float f) {}
        public void print(double d) {}
        public void print(boolean b) {}
        public void println() {}
        public void println(Object obj) {}
        public void println(String s) {}
        public void println(char[] s) {}
        public void println(char c) {}
        public void println(int i) {}
        public void println(long l) {}
        public void println(float f) {}
        public void println(double d) {}
        public void println(boolean b) {}
    }
}
