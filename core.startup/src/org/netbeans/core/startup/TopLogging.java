/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
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

    /** Initializes the logging configuration. Invoked by <code>LogManager.readConfiguration</code> method.
     */
    public TopLogging() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);

        Iterator it = System.getProperties().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry)it.next();

            String key = (String)e.getKey();

            if ("sun.os.patch.level".equals(key)) { // NOI18N
                // skip this property as it does not mean level of logging
                continue;
            }

            String v = (String)e.getValue();

            if (key.endsWith(".level")) {
                ps.print(key);
                ps.print("=");
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

        /* TBD:
        for (Handler h : Lookup.getDefault().lookupAll(Handler.class)) {
            logger.addHandler(h);
        }
         */
    }

    private static String previousUser;
    static final void initialize() {
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

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        printSystemInfo(ps);
        ps.close();
        try {
            Logger logger = Logger.getLogger (TopLogging.class.getName()); // NOI18N
            logger.log(Level.INFO, os.toString("utf-8"));
        } catch (UnsupportedEncodingException ex) {
            assert false;
        }

        if (!(System.err instanceof LgStream)) {
            System.setErr(new LgStream(Logger.getLogger("stderr"))); // NOI18N
        }
    }
    

    private static void printSystemInfo(PrintStream ps) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.US);
        Date date = new Date();

        ps.println("-------------------------------------------------------------------------------"); // NOI18N
        ps.println(">Log Session: "+df.format (date)); // NOI18N
        ps.println(">System Info: "); // NOI18N
        
        String buildNumber = System.getProperty ("netbeans.buildnumber"); // NOI18N
        String currentVersion = NbBundle.getMessage(TopLogging.class, "currentVersion", buildNumber );
        ps.println("  Product Version         = " + currentVersion); // NOI18N
        ps.println("  Operating System        = " + System.getProperty("os.name", "unknown")
                   + " version " + System.getProperty("os.version", "unknown")
                   + " running on " +  System.getProperty("os.arch", "unknown"));
        ps.println("  Java; VM; Vendor; Home  = " + System.getProperty("java.version", "unknown") + "; " +
                   System.getProperty("java.vm.name", "unknown") + " " + System.getProperty("java.vm.version", "") + "; " +
                   System.getProperty("java.vendor", "unknown") + "; " +
                   System.getProperty("java.home", "unknown"));
        ps.print(  "  System Locale; Encoding = " + Locale.getDefault()); // NOI18N
        String branding = NbBundle.getBranding ();
        if (branding != null) {
            ps.print(" (" + branding + ")"); // NOI18N
        }
        ps.println("; " + System.getProperty("file.encoding", "unknown")); // NOI18N
        ps.println("  Home Dir.; Current Dir. = " + System.getProperty("user.home", "unknown") + "; " +
                   System.getProperty("user.dir", "unknown"));
        ps.print(  "  Installation; User Dir. = "); // NOI18N
        String nbdirs = System.getProperty("netbeans.dirs");
        if (nbdirs != null) { // noted in #67862: should show all clusters here.
            StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                ps.print(FileUtil.normalizeFile(new File(tok.nextToken())));
                ps.print(File.pathSeparatorChar);
            }
        }
        ps.println(CLIOptions.getHomeDir() + "; " + CLIOptions.getUserDir()); // NOI18N
        ps.println("  Boot & Ext. Classpath   = " + createBootClassPath()); // NOI18N
        ps.println("  Application Classpath   = " + System.getProperty("java.class.path", "unknown")); // NOI18N
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

    /** Non closing handler.
     */
    private static final class NonClose extends Handler
    implements Runnable {
        private static RequestProcessor RP = new RequestProcessor("Logging Flush"); // NOI18N

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
            flush.schedule(delay);
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

        public Formatter getFormatter() {
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

    /** Modified formater for use in NetBeans.
     */
    private static final class NbFormatter extends java.util.logging.Formatter {
        private static String lineSeparator = System.getProperty ("line.separator"); // NOI18N
        static java.util.logging.Formatter FORMATTER = new NbFormatter ();

        
        public String format(java.util.logging.LogRecord record) {
            StringBuilder sb = new StringBuilder();
            print(sb, record, new HashSet<Throwable>());
            return sb.toString();
        }


        private void print(StringBuilder sb, LogRecord record, Set<Throwable> beenThere) {
            // XXX this is copied from NbErrorManager? why are we maintaining two copies? -jglick
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
                    // First try to find where the throwable was caught.
                    StackTraceElement[] tStack = record.getThrown().getStackTrace();
                    StackTraceElement[] hereStack = new Throwable().getStackTrace();
                    int idx = -1;
                    for (int i = 1; i <= Math.min(tStack.length, hereStack.length); i++) {
                        if (!tStack[tStack.length - i].equals(hereStack[hereStack.length - i])) {
                            idx = tStack.length - i + 1;
                            break;
                        }
                    }
                    String[] tLines = decompose(record.getThrown());
                    for (int i = 0; i < tLines.length; i++) {
                        if (i == idx) {
                            pw.print("[catch]"); // NOI18N
                            // Also translate following tab -> space since formatting is bad in
                            // Output Window (#8104) and some mail agents screw it up etc.
                            if (tLines[i].charAt(0) == '\t') {
                                pw.print(' ');
                                tLines[i] = tLines[i].substring(1);
                            }
                        }
                        pw.println(tLines[i]);
                    }
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
        /** Get a throwable's stack trace, decomposed into individual lines. */
        private static String[] decompose(Throwable t) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            return sw.toString().split("(\r\n?|\n)($|(?=\\s*at ))"); // NOI18N
        }
    } // end of NbFormater

    /** a stream to delegate to logging.
     */
    private static final class LgStream extends PrintStream {
        private Logger log;
        private StringBuffer sb = new StringBuffer();

        public LgStream(Logger log) {
            super(new ByteArrayOutputStream());
            this.log = log;
        }

        public void write(byte[] buf, int off, int len) {
            sb.append(new String(buf, off, len));
            checkFlush();
        }

        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        public void write(int b) {
            sb.append((char)b);
            checkFlush();
        }

        private void checkFlush() {
            boolean justNewLine = false;
            for (;;) {
                int first = sb.indexOf("\n"); // NOI18N
                if (first < 0) {
                    break;
                }
                justNewLine = false;
                if (first == 0) {
                    justNewLine = true;
                    sb.delete(0, 1);
                    continue;
                }
                log.log(Level.INFO, sb.substring(0, first + 1));
                sb.delete(0, first + 1);
            }
            
            if (justNewLine) {
                log.log(Level.INFO, "\n");
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
        /** The name MUST be handle and MUST be public */
        public static void handle(Throwable t) {
            // Either org.netbeans or org.netbeans.core.execution pkgs:
            if (t.getClass().getName().endsWith(".ExitSecurityException")) { // NOI18N
                return;
            }
            Logger.global.log(Level.SEVERE, null, t);
        }
    } // end of AWTHandler

}
