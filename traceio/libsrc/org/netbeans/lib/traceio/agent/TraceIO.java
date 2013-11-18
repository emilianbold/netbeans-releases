/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.lib.traceio.agent;

import java.awt.EventQueue;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author Tomas Hurka
 */
public class TraceIO {

    public static final String IO_TRACE = "sun.misc.IoTrace";   // NOI18N
    public static final String USERDIR = "userdir";     // NOI18N
    public static final String CACHEDIR = "cachedir";   // NOI18N
    public static final String ACTION = "action";       // NOI18N
    public static final String SIZE_EQ = "sizeEq";      // NOI18N
    public static final String SIZE = "size";           // NOI18N
    public static final String THREAD_AWT = "thread_awt";   // NOI18N
    private static final String IO_TRACE_RESOURCE = "sun/misc/IoTrace.class";   // NOI18N
    private static final String TRACE_IO_RESOURCE = "org/netbeans/lib/traceio/agent/TraceIO.class"; // NOI18N
    private static final int INITIAL_TIMEOUT = 5000;
    private static final boolean DEBUG = false;

    public enum TRACE_ACTION {LOG,LOG_EXCEPTION,THROW_EXCEPTION};
    public enum TRACE_EQ {LOWER,EQUAL,GREATER};
    
    public static ThreadLocal<Boolean> map = new ThreadLocal();
    public static long start = System.currentTimeMillis();
    private static Properties args;
    private static UserdirChecker userdir;
    private static Reporter reporter;
    private static boolean checkAWT;
    private static SizeChecker size;
    
    public static void agentmain(final String agentArgs, final Instrumentation inst) {
        System.err.println("Agent TraceIO loaded"); // NOI18N
        if (DEBUG) System.err.println("Agent TraceIO Args: " + agentArgs);  // NOI18N
        if (DEBUG) System.err.println("IAgent TraceIO - is redefine supported: " + inst.isRedefineClassesSupported());  // NOI18N
        args = deserializeProperties(agentArgs);
        System.err.println(args);
        userdir = new UserdirChecker(args);
        reporter = new Reporter(args);
        checkAWT = Boolean.valueOf(args.getProperty(THREAD_AWT));
        size = new SizeChecker(args);
        redefineIoTrace(inst);
        System.err.println("Agent TraceIO - installation finished");   // NOI18N
    }

    private static void redefineIoTrace(Instrumentation inst) {
        try {
            Class iotraceClass = Class.forName(IO_TRACE);
            InputStream is = getStreamForClass(IO_TRACE_RESOURCE);
            if (is != null) {
                try {
                    ClassDefinition iotrace = new ClassDefinition(iotraceClass, readInputStream(is));
                    inst.redefineClasses(iotrace);
                } finally {
                    is.close();
                }
            } else {
                System.err.println(IO_TRACE_RESOURCE + " not found");   // NOI18N
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace(System.err);
        } catch (UnmodifiableClassException ex) {
            ex.printStackTrace(System.err);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private static byte[] readInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream bou = new ByteArrayOutputStream(8192);
        while (true) {
            int b = is.read();
            if (b == -1) {
                break;
            }
            bou.write(b);
        }
        bou.close();
        if (DEBUG) System.err.println("Input stream size: " + bou.toByteArray().length);    // NOI18N
        return bou.toByteArray();
    }

    private static File getArchiveFile(URL url) {
        String protocol = url.getProtocol();

        if ("jar".equals(protocol)) { //NOI18N            
            String path = url.getPath();
            int index = path.indexOf("!/"); //NOI18N

            if (index >= 0) {
                try {
                    return new File(new URI(path.substring(0, index)));
                } catch (URISyntaxException ex) {
                    throw new IllegalArgumentException(url.toString());
                }
            }
        }
        throw new IllegalArgumentException(url.toString());
    }

    private static InputStream getStreamForClass(String classResourceName) throws IOException {
        URL selfURL = ClassLoader.getSystemClassLoader().getResource(TRACE_IO_RESOURCE);
        if (DEBUG) System.err.println(selfURL);
        File archive = getArchiveFile(selfURL);
        System.err.println("Agent TraceIO "+archive.getAbsolutePath()); // NOI18N
        JarFile jarFile = new JarFile(archive);
        JarEntry iotraceEntry = jarFile.getJarEntry(classResourceName);
        if (iotraceEntry != null) {
            if (DEBUG) System.err.println(iotraceEntry.getName());
            return jarFile.getInputStream(iotraceEntry);
        }
        return null;
    }

    private static int getTime() {
        return (int)(System.currentTimeMillis() - start);
    }
    
    /**
     * Called before data is read from a socket.
     *
     * @return a context object
     */
    public static Object socketReadBegin() {
        if (isRecursive()) return null;
        try {
            int t = getTime();

            if (t < INITIAL_TIMEOUT) return null;
            if (!checkTread()) return null;

            System.err.println(t + " socketReadBegin"); // NOI18N
        } finally {
            map.set(Boolean.FALSE);
        }
        return null;
    }

    /**
     * Called after data is read from the socket.
     *
     * @param context
     *            the context returned by the previous call to socketReadBegin()
     * @param address
     *            the remote address the socket is bound to
     * @param port
     *            the remote port the socket is bound to
     * @param timeout
     *            the SO_TIMEOUT value of the socket (in milliseconds) or 0 if
     *            there is no timeout set
     * @param bytesRead
     *            the number of bytes read from the socket, 0 if there was an
     *            error reading from the socket
     */
    public static void socketReadEnd(Object context, InetAddress address, int port,
            int timeout, long bytesRead) {
        if (isRecursive()) return;
        try {
            int t = getTime();

            if (t < INITIAL_TIMEOUT) return;
            if (!checkTread()) return;
            if (!size.checkSize(bytesRead)) return;
            
            String text = t + " socketReadEnd " + address + ":" + port + " timeout " + timeout + " bytes:" + bytesRead; // NOI18N
            reporter.reportException(text);
        } finally {
            map.set(Boolean.FALSE);
        }
    }

    /**
     * Called before data is written to a socket.
     *
     * @return a context object
     */
    public static Object socketWriteBegin() {
        if (isRecursive()) return null;
        try {
            int t = getTime();
            
            if (t < INITIAL_TIMEOUT) return null;
            if (!checkTread()) return null;

            System.err.println(t + " socketWriteBegin ");   // NOI18N
        } finally {
            map.set(Boolean.FALSE);
        }
        return null;
    }

    /**
     * Called after data is written to a socket.
     *
     * @param context
     *            the context returned by the previous call to
     *            socketWriteBegin()
     * @param address
     *            the remote address the socket is bound to
     * @param port
     *            the remote port the socket is bound to
     * @param bytesWritten
     *            the number of bytes written to the socket, 0 if there was an
     *            error writing to the socket
     */
    public static void socketWriteEnd(Object context, InetAddress address, int port, long bytesWritten) {
        if (isRecursive()) return;
        try {
            int t = getTime();
 
            if (t < INITIAL_TIMEOUT) return;
            if (!checkTread()) return;
            if (!size.checkSize(bytesWritten)) return;
            
            String text = t + " socketWriteEnd write " + address + ":" + port + " bytes:" + bytesWritten;   // NOI18N
            reporter.reportException(text);
        } finally {
            map.set(Boolean.FALSE);
        }
    }

    /**
     * Called before data is read from a file.
     *
     * @param path
     *            the path of the file
     * @return a context object
     */
    public static Object fileReadBegin(String path) {
        return new Context(path, getTime());
    }

    /**
     * Called after data is read from a file.
     *
     * @param context
     *            the context returned by the previous call to fileReadBegin()
     * @param bytesRead
     *            the number of bytes written to the file, 0 if there was an
     *            error writing to the file
     */
    public static void fileReadEnd(Object context, long bytesRead) {
        if (isRecursive()) return;
        try {
            Context c = (Context) context;
            String name = c.name;
            int t = getTime();

            if (t < INITIAL_TIMEOUT) return;
            if (!checkTread()) return;
            if (!checkFileName(name)) return;
            if (!size.checkSize(bytesRead)) return;
            
            String text = t + " file " + name + " ReadEnd read bytes:" + bytesRead+ " time:"+(t-c.time);    // NOI18N
            reporter.reportException(text);
        } finally {
            map.set(Boolean.FALSE);
        }
    }

    /**
     * Called before data is written to a file.
     *
     * @param path
     *            the path of the file
     * @return a context object
     */
    public static Object fileWriteBegin(String path) {
        return new Context(path, getTime());
    }

    /**
     * Called after data is written to a file.
     *
     * @param context
     *            the context returned by the previous call to fileReadBegin()
     * @param bytesWritten
     *            the number of bytes written to the file, 0 if there was an
     *            error writing to the file
     */
    public static void fileWriteEnd(Object context, long bytesWritten) {
        if (isRecursive()) return;
        try {
            Context c = (Context) context;
            String name = c.name;
            int t = getTime();

            if (t < INITIAL_TIMEOUT) return;
            if (!checkTread()) return;
            if (!checkFileName(name)) return;
            if (!size.checkSize(bytesWritten)) return;
            
            String text = t + " file " + name + " WriteEnd write bytes:" + bytesWritten+ " time:"+(t-c.time);   // NOI18N
            reporter.reportException(text);
        } finally {
            map.set(Boolean.FALSE);
        }
    }
 
    private static boolean isRecursive() {
        Boolean r = map.get();
        if (r != null && r.booleanValue()) {
            return true;
        }
        map.set(Boolean.TRUE);
        return false;
    }

    private static Properties deserializeProperties(String agentArgs) {
        StringBuilder buf = new StringBuilder();
        Properties p = new Properties();
        List<String> strings = new ArrayList();
        for (char c : agentArgs.toCharArray()) {
            if (c == '\1') {
                strings.add(buf.toString());
                buf = new StringBuilder();
            } else {
                buf.append(c);
            }
        }
        Iterator<String> sit = strings.iterator();
        while(sit.hasNext()) {
            p.setProperty(sit.next(), sit.next());
        }
        return p;
    }
    
    private static boolean isTTFont(String path) {
        if (path == null) {
            return false;
        }
        if (path.endsWith(".ttf") || path.endsWith(".TTF")) {   // NOI18N
            return true;
        }
        return false;
    }

    private static boolean isTTCFont(String path) {
        if (path == null) {
            return false;
        }
        if (path.endsWith(".ttc") || path.endsWith(".TTC")) {   // NOI18N
            return true;
        }
        return false;
    }

    private static boolean checkTread() {
        if (checkAWT) {
            return EventQueue.isDispatchThread();
        }
        return true;
    }

    private static boolean checkFileName(String name) {
        if (userdir.isUserdir(name)) return false;
        if (isTTFont(name)) return false;
        if (isTTCFont(name)) return false;
        return true;
    }

    private static class Context {
        private String name;
        private int time;
        
        Context(String n, int t) {
            name = n;
            time = t;
        }
    }
}
