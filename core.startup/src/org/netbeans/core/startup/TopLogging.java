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

package org.netbeans.core.startup;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.TopSecurityManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.xml.sax.SAXParseException;

/**
 * Class that sets the java.util.logging.LogManager configuration to log into
 * the right file and put there the right content. Does nothing if
 * either <code>java.util.logging.config.file</code> or
 * <code>java.util.logging.config.class</code> is specified.
 */
public final class TopLogging {
    private static boolean disabledConsole = ! Boolean.getBoolean("netbeans.logger.console"); // NOI18N
    /** reference to the old error stream */
    private static final PrintStream OLD_ERR = System.err;
    static {
        System.setProperty("sun.awt.exception.handler", "org.netbeans.core.startup.TopLogging$AWTHandler"); // NOI18N
    }

    private static final PrintStream DEBUG;
    private static final Pattern unwantedMessages;
    static {
        PrintStream _D = null;
        String uMS = System.getProperty("TopLogging.unwantedMessages"); // NOI18N
        if (uMS != null || Boolean.getBoolean("TopLogging.DEBUG")) { // NOI18N
            try {
                File debugLog = new File(System.getProperty("java.io.tmpdir"), "TopLogging.log"); // NOI18N
                System.err.println("Logging sent to: " + debugLog); // NOI18N
                _D = new PrintStream(new FileOutputStream(debugLog), true);
            } catch (FileNotFoundException x) {
                x.printStackTrace();
            }
        }
        DEBUG = _D;
        Pattern uMP = null;
        if (uMS != null) {
            try {
                uMP = Pattern.compile(uMS);
                DEBUG.println("On the lookout for log messages matching: " + uMS); // NOI18N
            } catch (PatternSyntaxException x) {
                x.printStackTrace();
            }
        }
        unwantedMessages = uMP;
    }

    /** Initializes the logging configuration. Invoked by <code>LogManager.readConfiguration</code> method.
     */
    public TopLogging() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);

        for (Map.Entry<?, ?> e: System.getProperties().entrySet()) {
            String key = (String)e.getKey();

            if ("sun.os.patch.level".equals(key)) { // NOI18N
                // skip this property as it does not mean level of logging
                continue;
            }

            String v = (String)e.getValue();

            if (key.endsWith(".level")) {
                ps.print(key);
                ps.print('=');
                ps.println(v);
            }
        }
        ps.close();
        try {
            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(os.toByteArray()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        Logger logger = Logger.getLogger (""); // NOI18N

        Handler[] old = logger.getHandlers();
        for (int i = 0; i < old.length; i++) {
            logger.removeHandler(old[i]);
        }
        logger.addHandler(defaultHandler ());
        if (!disabledConsole) { // NOI18N
            logger.addHandler (streamHandler ());
        }
        logger.addHandler(new LookupDel());

        StartLog.register();
    }

    /**
     * For use from NbErrorManagerTest.
     */
    public static void initializeQuietly() {
        initialize(false);
    }
    private static String previousUser;
    static final void initialize() {
        initialize(true);
    }
    private static void initialize(boolean verbose) {
        if (previousUser == null || previousUser.equals(System.getProperty("netbeans.user"))) {
            // useful from tests
            streamHandler = null;
            defaultHandler = null;
        }

        if (System.getProperty("java.util.logging.config.file") != null) { // NOI18N
            return;
        }
        String v = System.getProperty("java.util.logging.config.class"); // NOI18N
        String p = TopLogging.class.getName();
        if (v != null && !v.equals(p)) {
            return;
        }

        // initializes the properties
        new TopLogging();
        // next time invoke the constructor of TopLogging itself please
        System.setProperty("java.util.logging.config.class", p);

        if (verbose) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(os);
            printSystemInfo(ps);
            ps.close();
            try {
                Logger logger = Logger.getLogger(TopLogging.class.getName()); // NOI18N
                logger.log(Level.INFO, os.toString("utf-8"));
            } catch (UnsupportedEncodingException ex) {
                assert false;
            }
        }

        if (!(System.err instanceof LgStream)) {
            System.setErr(new LgStream(Logger.getLogger("stderr"))); // NOI18N
            if (DEBUG != null) DEBUG.println("initializing stderr"); // NOI18N
        }
        if (!(System.out instanceof LgStream)) {
            System.setOut(new LgStream(Logger.getLogger("stderr"))); // NOI18N
            if (DEBUG != null) DEBUG.println("initializing stdout"); // NOI18N
        }
    }
    

    private static void printSystemInfo(PrintStream ps) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.US);
        Date date = new Date();

        ps.println("-------------------------------------------------------------------------------"); // NOI18N
        ps.println(">Log Session: "+df.format (date)); // NOI18N
        ps.println(">System Info: "); // NOI18N

        List<File> clusters = new ArrayList<File>();
        String nbdirs = System.getProperty("netbeans.dirs");
        if (nbdirs != null) { // noted in #67862: should show all clusters here.
            StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                File dir = FileUtil.normalizeFile(new File(tok.nextToken()));
                if (dir.isDirectory()) {
                    clusters.add(dir);
                }
            }
        }
        
        String buildNumber = System.getProperty ("netbeans.buildnumber"); // NOI18N
        String currentVersion = NbBundle.getMessage(TopLogging.class, "currentVersion", buildNumber );
        ps.print("  Product Version         = " + currentVersion); // NOI18N
        for (File cluster : clusters) { // also print Hg ID if available; more precise
            File buildInfo = new File(cluster, "build_info"); // NOI18N
            if (buildInfo.isFile()) {
                try {
                    Reader r = new FileReader(buildInfo);
                    try {
                        BufferedReader b = new BufferedReader(r);
                        String line;
                        Pattern p = Pattern.compile("Hg ID:    ([0-9a-f]{12})"); // NOI18N
                        while ((line = b.readLine()) != null) {
                            Matcher m = p.matcher(line);
                            if (m.matches()) {
                                ps.print(" (#" + m.group(1) + ")"); // NOI18N
                                break;
                            }
                        }
                    } finally {
                        r.close();
                    }
                } catch (IOException x) {
                    x.printStackTrace(ps);
                }
                break;
            }
        }
        ps.println();
        ps.println("  Operating System        = " + System.getProperty("os.name", "unknown")
                   + " version " + System.getProperty("os.version", "unknown")
                   + " running on " +  System.getProperty("os.arch", "unknown"));
        ps.println("  Java; VM; Vendor        = "
                + System.getProperty("java.version", "unknown") + "; "
                + System.getProperty("java.vm.name", "unknown") + " "
                + System.getProperty("java.vm.version", "") + "; "
                + System.getProperty("java.vendor", "unknown"));
        ps.println("  Runtime                 = "
                + System.getProperty("java.runtime.name", "unknown") + " "
                + System.getProperty("java.runtime.version", ""));
        ps.println("  Java Home               = " + System.getProperty("java.home", "unknown"));
        ps.print(  "  System Locale; Encoding = " + Locale.getDefault()); // NOI18N
        String branding = NbBundle.getBranding ();
        if (branding != null) {
            ps.print(" (" + branding + ")"); // NOI18N
        }
        ps.println("; " + System.getProperty("file.encoding", "unknown")); // NOI18N
        ps.println("  Home Directory          = " + System.getProperty("user.home", "unknown"));
        ps.println("  Current Directory       = " + System.getProperty("user.dir", "unknown"));
        ps.print(  "  User Directory          = "); // NOI18N
        ps.println(CLIOptions.getUserDir()); // NOI18N
        ps.print(  "  Installation            = "); // NOI18N
        for (File cluster : clusters) {
            ps.print(cluster + "\n                            "); // NOI18N
        }
        ps.println(CLIOptions.getHomeDir()); // platform cluster is separate
        ps.println("  Boot & Ext. Classpath   = " + createBootClassPath()); // NOI18N
        String cp;
        ClassLoader l = Lookup.class.getClassLoader();
        if (l == ClassLoader.getSystemClassLoader()) {
            cp = System.getProperty("java.class.path", "unknown"); // NOI18N
        } else {
            StringBuilder sb = new StringBuilder("loaded by "); // NOI18N
            if (l instanceof URLClassLoader) {
                sb.append("URLClassLoader "); // NOI18N
                String pref = "";
                for (URL u : ((URLClassLoader)l).getURLs()) {
                    sb.append(pref);
                    sb.append(u.toExternalForm());
                    pref = File.pathSeparator;
                }
            } else {
                sb.append(l);
            }
            cp = sb.toString();
        }
        ps.println("  Application Classpath   = " + cp); // NOI18N
        ps.println("  Startup Classpath       = " + System.getProperty("netbeans.dynamic.classpath", "unknown")); // NOI18N
        ps.println("-------------------------------------------------------------------------------"); // NOI18N
    }

    // Copied from NbClassPath:
    private static String createBootClassPath() {
        // boot
        String boot = System.getProperty("sun.boot.class.path"); // NOI18N
        StringBuffer sb = (boot != null ? new StringBuffer(boot) : new StringBuffer());
        
        // std extensions
        findBootJars(System.getProperty("java.ext.dirs"), sb);
        findBootJars(System.getProperty("java.endorsed.dirs"), sb);
        return sb.toString();
    }

    /** Scans path list for something that can be added to classpath.
     * @param extensions null or path list
     * @param sb buffer to put results to
     */
    private static void findBootJars(final String extensions, final StringBuffer sb) {
        if (extensions != null) {
            for (StringTokenizer st = new StringTokenizer(extensions, File.pathSeparator); st.hasMoreTokens();) {
                File dir = new File(st.nextToken());
                File[] entries = dir.listFiles();
                if (entries != null) {
                    for (int i = 0; i < entries.length; i++) {
                        String name = entries[i].getName().toLowerCase(Locale.US);
                        if (name.endsWith(".zip") || name.endsWith(".jar")) { // NOI18N
                            if (sb.length() > 0) {
                                sb.append(File.pathSeparatorChar);
                            }
                            sb.append(entries[i].getPath());
                        }
                    }
                }
            }
        }
    }

    /** Logger for test purposes.
     */
    static Handler createStreamHandler (PrintStream pw) {
        StreamHandler s = new StreamHandler (
            pw, NbFormatter.FORMATTER
        );
        return s;
    }
    
    private static java.util.logging.Handler streamHandler;
    private static synchronized java.util.logging.Handler streamHandler () {
        if (streamHandler == null) {
            StreamHandler sth = new StreamHandler (OLD_ERR, NbFormatter.FORMATTER);
            sth.setLevel(Level.ALL);
            streamHandler = new NonClose(sth, 500);
        }
        return streamHandler;
    }
    
    private static java.util.logging.Handler defaultHandler;
    private static synchronized java.util.logging.Handler defaultHandler () {
        if (defaultHandler != null) return defaultHandler;

        String home = System.getProperty("netbeans.user");
        if (home != null && !"memory".equals(home) && !CLIOptions.noLogging) {
            try {
                File dir = new File(new File(new File(home), "var"), "log");
                dir.mkdirs ();
                File f = new File(dir, "messages.log");
                File f1 = new File(dir, "messages.log.1");
                File f2 = new File(dir, "messages.log.2");

                if (f2.exists()) {
                    f2.delete();
                }
                if (f1.exists()) {
                    f1.renameTo(f2);
                }
                if (f.exists()) {
                    f.renameTo(f1);
                }
                
                FileOutputStream fout = new FileOutputStream(f, false);
                Handler h = new StreamHandler(fout, NbFormatter.FORMATTER);
                h.setLevel(Level.ALL);
                h.setFormatter(NbFormatter.FORMATTER);
                defaultHandler = new NonClose(h, 5000);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        if (defaultHandler == null) {
            defaultHandler = streamHandler();
            disabledConsole = true;
        }
        return defaultHandler;
    }

    /** Allows tests to flush all standard handlers */
    static void flush(boolean clear) {
        System.err.flush();
        
        
        Handler s = streamHandler;
        if (s != null) {
            s.flush();
        }

        Handler d = defaultHandler;
        if (d != null) {
            d.flush();
        }

        if (clear) {
            streamHandler = null;
            defaultHandler = null;
        }
    }
    static void close() {
        Handler s = streamHandler;
        if (s instanceof NonClose) {
            NonClose ns = (NonClose)s;
            ns.doClose();
        }

        Handler d = defaultHandler;
        if (d != null) {
            NonClose nd = (NonClose)d;
            nd.doClose();
        }
    }
    static void exit(int exit) {
        flush(false);
        TopSecurityManager.exit(exit);
    }

    /** Non closing handler.
     */
    private static final class NonClose extends Handler
    implements Runnable {
        private static RequestProcessor RP = new RequestProcessor("Logging Flush"); // NOI18N
        private static ThreadLocal<Boolean> FLUSHING = new ThreadLocal<Boolean>();

        private final Handler delegate;
        private RequestProcessor.Task flush;
        private int delay;

        public NonClose(Handler h, int delay) {
            delegate = h;
            flush = RP.create(this, true);
            flush.setPriority(Thread.MIN_PRIORITY);
            this.delay = delay;
        }

        public void publish(LogRecord record) {
            delegate.publish(record);
            if (!Boolean.TRUE.equals(FLUSHING.get())) {
                try {
                    FLUSHING.set(true);
                    flush.schedule(delay);
                } finally {
                    FLUSHING.set(false);
                }
            }
        }

        public void flush() {
            flush.cancel();
            flush.waitFinished();
            delegate.flush();
        }

        public void close() throws SecurityException {
            flush();
            delegate.flush();
        }

        public void doClose() throws SecurityException {
            flush();
            delegate.close();
        }

        public @Override Formatter getFormatter() {
            return delegate.getFormatter();
        }

        static Handler getInternal(Handler h) {
            if (h instanceof NonClose) {
                return ((NonClose)h).delegate;
            }
            return h;
        }

        public void run() {
            delegate.flush();
        }
    }

    /**
     * For use also from NbErrorManager.
     * @param t throwable to print
     * @param pw the destination
     */
    public static void printStackTrace(Throwable t, PrintWriter pw) {
        // First try to find where the throwable was caught.
        StackTraceElement[] tStack = t.getStackTrace();
        StackTraceElement[] hereStack = new Throwable().getStackTrace();
        int idx = -1;
        for (int i = 1; i <= Math.min(tStack.length, hereStack.length); i++) {
            if (!tStack[tStack.length - i].equals(hereStack[hereStack.length - i])) {
                idx = tStack.length - i;
                break;
            }
        }
        doPrintStackTrace(pw, t, null, idx);
    }

    /**
     * #91541: show stack traces in a more natural order.
     */
    private static void doPrintStackTrace(PrintWriter pw, Throwable t, Throwable higher, int caughtIndex) {
        //if (t != null) {t.printStackTrace(pw);return;}//XxX
        try {
            if (t.getClass().getMethod("printStackTrace", PrintWriter.class).getDeclaringClass() != Throwable.class) { // NOI18N
                // Hmm, overrides it, we should not try to bypass special logic here.
                //System.err.println("using stock printStackTrace from " + t.getClass());
                t.printStackTrace(pw);
                return;
            }
            //System.err.println("using custom printStackTrace from " + t.getClass());
        } catch (NoSuchMethodException e) {
            assert false : e;
        }
        Throwable lower = t.getCause();
        if (lower != null) {
            doPrintStackTrace(pw, lower, t, -1);
            pw.print("Caused: "); // NOI18N
        }
        String summary = t.toString();
        if (lower != null) {
            String suffix = ": " + lower;
            if (summary.endsWith(suffix)) {
                summary = summary.substring(0, summary.length() - suffix.length());
            }
        }
        pw.println(summary);
        StackTraceElement[] trace = t.getStackTrace();
        int end = trace.length;
        if (higher != null) {
            StackTraceElement[] higherTrace = higher.getStackTrace();
            while (end > 0) {
                int higherEnd = end + higherTrace.length - trace.length;
                if (higherEnd <= 0 || !higherTrace[higherEnd - 1].equals(trace[end - 1])) {
                    break;
                }
                end--;
            }
        }
        for (int i = 0; i < end; i++) {
            if (i == caughtIndex) {
                // Translate following tab -> space since formatting is bad in
                // Output Window (#8104) and some mail agents screw it up etc.
                pw.print("[catch] at "); // NOI18N
            } else {
                pw.print("\tat "); // NOI18N
            }
            pw.println(trace[i]);
        }
    }
    
    /** Modified formater for use in NetBeans.
     */
    private static final class NbFormatter extends java.util.logging.Formatter {
        private static String lineSeparator = System.getProperty ("line.separator"); // NOI18N
        static java.util.logging.Formatter FORMATTER = new NbFormatter ();

        public String format(java.util.logging.LogRecord record) {
            StringBuilder sb = new StringBuilder();
            print(sb, record, new HashSet<Throwable>());
            String r = sb.toString();
            if (DEBUG != null) DEBUG.print("received: " + r); // NOI18N
            if (unwantedMessages != null && unwantedMessages.matcher(r).find()) {
                new Exception().printStackTrace(DEBUG);
            }
            return r;
        }


        private void print(StringBuilder sb, LogRecord record, Set<Throwable> beenThere) {
            String message = formatMessage(record);
            if (message != null && message.indexOf('\n') != -1 && record.getThrown() == null) {
                // multi line messages print witout any wrappings
                sb.append(message);
                return;
            }
            if ("stderr".equals(record.getLoggerName()) && record.getLevel() == Level.INFO) { // NOI18N
                // do not prefix stderr logging...
                sb.append(message);
                return;
            }

            sb.append(record.getLevel().getLocalizedName());
            addLoggerName (sb, record);
            if (message != null) {
                sb.append(": ");
                sb.append(message);
            }
            sb.append(lineSeparator);
            if (record.getThrown() != null && record.getLevel().intValue() != 1973) { // 1973 signals ErrorManager.USER
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    // All other kinds of throwables we check for a stack trace.
                    printStackTrace(record.getThrown(), pw);
                    pw.close();
                    sb.append(sw.toString());
                } catch (Exception ex) {
                }

                LogRecord[] arr = extractDelegates(sb, record.getThrown(), beenThere);
                if (arr != null) {
                    for (LogRecord r : arr) {
                        print(sb, r, beenThere);
                    }
                }

                specialProcessing(sb, record.getThrown(), beenThere);
            }
        }
        
        private static void addLoggerName (StringBuilder sb, java.util.logging.LogRecord record) {
            String name = record.getLoggerName ();
            if (!"".equals (name)) {
                sb.append(" [");
                sb.append(name);
                sb.append(']');
            }
        }

        private static LogRecord[] extractDelegates(StringBuilder sb, Throwable t, Set<Throwable> beenThere) {
            if (!beenThere.add(t)) {
                sb.append("warning: cyclic dependency between annotated throwables"); // NOI18N
                return null;
            }

            if (t instanceof Callable) {
                Object rec = null;
                try {
                    rec = ((Callable) t).call();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (rec instanceof LogRecord[]) {
                    return (LogRecord[])rec;
                }
            }
            if (t == null) {
                return null;
            }
            return extractDelegates(sb, t.getCause(), beenThere);
        }


        private void specialProcessing(StringBuilder sb, Throwable t, Set<Throwable> beenThere) {
            // MissingResourceException should be printed nicely... --jglick
            if (t instanceof MissingResourceException) {
                MissingResourceException mre = (MissingResourceException) t;
                String cn = mre.getClassName();
                if (cn != null) {
                    LogRecord rec = new LogRecord(Level.CONFIG, null);
                    rec.setResourceBundle(NbBundle.getBundle(TopLogging.class));
                    rec.setMessage("EXC_MissingResourceException_class_name");
                    rec.setParameters(new Object[] { cn });
                    print(sb, rec, beenThere);
                }
                String k = mre.getKey();
                if (k != null) {
                    LogRecord rec = new LogRecord(Level.CONFIG, null);
                    rec.setResourceBundle(NbBundle.getBundle(TopLogging.class));
                    rec.setMessage("EXC_MissingResourceException_key");
                    rec.setParameters(new Object[] { k });
                    print(sb, rec, beenThere);
                }
            }
            if (t instanceof SAXParseException) {
                // For some reason these fail to come with useful data, like location.
                SAXParseException spe = (SAXParseException)t;
                String pubid = spe.getPublicId();
                String sysid = spe.getSystemId();
                if (pubid != null || sysid != null) {
                    int col = spe.getColumnNumber();
                    int line = spe.getLineNumber();
                    String msg;
                    Object[] param;
                    if (col != -1 || line != -1) {
                        msg = "EXC_sax_parse_col_line"; // NOI18N
                        param = new Object[] {String.valueOf(pubid), String.valueOf(sysid), new Integer(col), new Integer(line)};
                    } else {
                        msg = "EXC_sax_parse"; // NOI18N
                        param = new Object[] { String.valueOf(pubid), String.valueOf(sysid) };
                    }
                    LogRecord rec = new LogRecord(Level.CONFIG, null);
                    rec.setResourceBundle(NbBundle.getBundle(TopLogging.class));
                    rec.setMessage(msg);
                    rec.setParameters(param);
                    print(sb, rec, beenThere);
                }
            }
        }
    } // end of NbFormater

    /** a stream to delegate to logging.
     */
    private static final class LgStream extends PrintStream implements Runnable {
        private Logger log;
        private final StringBuffer sb = new StringBuffer();
        private static RequestProcessor RP = new RequestProcessor("StdErr Flush");
        private RequestProcessor.Task flush = RP.create(this, true);

        public LgStream(Logger log) {
            super(new ByteArrayOutputStream());
            this.log = log;
        }

        public @Override void write(byte[] buf, int off, int len) {
            if (RP.isRequestProcessorThread()) {
                return;
            }
            String s = new String(buf, off, len);
            if (unwantedMessages != null && unwantedMessages.matcher(s).find()) {
                new Exception().printStackTrace(DEBUG);
            }
            sb.append(s);
            checkFlush();
        }

        public @Override void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        public @Override void write(int b) {
            if (RP.isRequestProcessorThread()) {
                return;
            }
            sb.append((char)b);
            checkFlush();
        }

        @Override
        public void flush() {
            try {
                flush.waitFinished(500);
            } catch (InterruptedException ex) {
                // ok, flush failed, do not even print
                // as we are inside the System.err code
            }
            super.flush();
        }

        
        
        private void checkFlush() {
            //if (DEBUG != null) DEBUG.println("checking flush; buffer: " + sb); // NOI18N
            try {
                flush.schedule(100);
            } catch (IllegalStateException ex) {
                /* can happen during shutdown:
                    Nested Exception is:
                    java.lang.IllegalStateException: Timer already cancelled.
                            at java.util.Timer.sched(Timer.java:354)
                            at java.util.Timer.schedule(Timer.java:170)
                            at org.openide.util.RequestProcessor$Task.schedule(RequestProcessor.java:621)
                            at org.netbeans.core.startup.TopLogging$LgStream.checkFlush(TopLogging.java:679)
                            at org.netbeans.core.startup.TopLogging$LgStream.write(TopLogging.java:650)
                            at sun.nio.cs.StreamEncoder.writeBytes(StreamEncoder.java:202)
                            at sun.nio.cs.StreamEncoder.implWrite(StreamEncoder.java:263)
                            at sun.nio.cs.StreamEncoder.write(StreamEncoder.java:106)
                            at java.io.OutputStreamWriter.write(OutputStreamWriter.java:190)
                            at java.io.BufferedWriter.flushBuffer(BufferedWriter.java:111)
                            at java.io.PrintStream.write(PrintStream.java:476)
                            at java.io.PrintStream.print(PrintStream.java:619)
                            at java.io.PrintStream.println(PrintStream.java:773)
                            at java.lang.Throwable.printStackTrace(Throwable.java:461)
                            at java.lang.Throwable.printStackTrace(Throwable.java:451)
                            at org.netbeans.insane.impl.LiveEngine.trace(LiveEngine.java:180)
                            at org.netbeans.insane.live.LiveReferences.fromRoots(LiveReferences.java:110)

                 * just ignore it, we cannot print it at this situation anyway...                
                 */
            }
        }
        
        public void run() {
            for (;;) {
                String toLog;
                synchronized (sb) {
                    int first = sb.indexOf("\n"); // NOI18N
                    if (first < 0) {
                        break;
                    }
                    if (first == 0) {
                        sb.delete(0, 1);
                        toLog = "\n";
                    } else {
                        toLog = sb.substring(0, first + 1);
                        sb.delete(0, first + 1);
                    }
                }
                //if (DEBUG != null) DEBUG.println("delegating: " + toLog); // NOI18N
                log.log(Level.INFO, toLog);
            }
        }
    } // end of LgStream

    private static final class LookupDel extends Handler 
    implements LookupListener {
        private Lookup.Result<Handler> handlers;
        private Collection<? extends Handler> instances;
        
        
        public LookupDel() {
            handlers = Lookup.getDefault().lookupResult(Handler.class);
            instances = handlers.allInstances();
            handlers.addLookupListener(this);
        }
        
        
        public void publish(LogRecord record) {
            for (Handler h : instances) {
                h.publish(record);
            }
        }

        public void flush() {
            for (Handler h : instances) {
                h.flush();
            }
        }

        public void close() throws SecurityException {
            for (Handler h : instances) {
                h.close();
            }
        }

        public void resultChanged(LookupEvent ev) {
            instances = handlers.allInstances();
        }
    } // end of LookupDel

    /** Instances are created in awt.EventDispatchThread */
    public static final class AWTHandler {
        /** The name MUST be handle and MUST be public 
         * @param t the throwable to print
         */
        public static void handle(Throwable t) {
            // Either org.netbeans or org.netbeans.core.execution pkgs:
            if (t.getClass().getName().endsWith(".ExitSecurityException")) { // NOI18N
                return;
            }
            Logger g = Logger.getLogger("global");
            g.log(Level.SEVERE, null, t);
        }
    } // end of AWTHandler

}
