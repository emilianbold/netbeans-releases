/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.glassfish.common;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.common.actions.DebugAction;
import org.netbeans.modules.glassfish.common.actions.RefreshAction;
import org.netbeans.modules.glassfish.common.actions.RestartAction;
import org.netbeans.modules.glassfish.common.actions.StartServerAction;
import org.netbeans.modules.glassfish.common.actions.StopServerAction;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.Recognizer;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * This class is capable of tailing the specified file or input stream. It
 * checks for changes at the specified intervals and outputs the changes to
 * the given I/O panel in NetBeans.
 *
 * It is now particularly tuned, in the case of files, for GlassFish V3 log
 * files.
 *
 * FIXME Refactor: LogViewMgr should be a special case of SimpleIO
 * 
 * @author Peter Williams
 * @author Michal Mocnak
 */
public class LogViewMgr {

    private static final Logger LOGGER = Logger.getLogger("glassfish"); //  NOI18N

    private static final boolean strictFilter = Boolean.getBoolean("glassfish.logger.strictfilter");

    /**
     * Amount of time in milliseconds to wait between checks of the input
     * stream
     */
    private static final int DELAY = 1000;
    
    /**
     * Singleton model pattern
     */
    private static final Map<String, WeakReference<LogViewMgr>> instances =
            new HashMap<String, WeakReference<LogViewMgr>>();

    /**
     * Server URI for this log view
     */
    private final String uri;

    /**
     * The I/O window where to output the changes
     */
    private InputOutput io;

    /**
     * Active readers for this log view.  This list contains references either
     * to nothing (which means log is not active), a single file reader to
     * monitor server.log if the server is running outside the IDE, or two
     * stream readers for servers started within the IDE.
     *
     * !PW not sure this complexity is worth it.  Reading server.log correctly
     * is a major pain compared to reading server I/O streams directly.  But we
     * don't have that luxury for servers created outside the IDE, so this is a
     * feeble attempt to have our cake and eat it too :)  I'll probably regret
     * it later.
     */
    private final List<WeakReference<LoggerRunnable>> readers =
            Collections.synchronizedList(new ArrayList<WeakReference<LoggerRunnable>>());

    /**
     * Creates and starts a new instance of LogViewMgr
     * 
     * @param uri the uri of the server
     */
    private LogViewMgr(final String uri) {
        this.uri = uri;
        io = getServerIO(uri);
        
        if (io == null) {
            return; // finish, it looks like this server instance has been unregistered
        }
        
        // clear the old output
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            // no op
        }
    }
    
    /**
     * Returns uri specific instance of LogViewMgr
     * 
     * @param uri the uri of the server
     * @return uri specific instamce of LogViewMgr
     */
    public static LogViewMgr getInstance(String uri) {
        LogViewMgr logViewMgr = null;
        synchronized (instances) {
            WeakReference<LogViewMgr> viewRef = instances.get(uri);
            logViewMgr = viewRef != null ? viewRef.get() : null;
            if(logViewMgr == null) {
                logViewMgr = new LogViewMgr(uri);
                instances.put(uri, new WeakReference<LogViewMgr>(logViewMgr));
            }
        }
        return logViewMgr;
    }
    
    public void ensureActiveReader(List<Recognizer> recognizers, File serverLog) {
        synchronized (readers) {
            boolean activeReader = false;
            for(WeakReference<LoggerRunnable> ref: readers) {
                LoggerRunnable logger = ref.get();
                if(logger != null) {
                    activeReader = true;
                    break;
                }
            }

            if(!activeReader && serverLog != null) {
                readFiles(recognizers, serverLog);
            }
        }
    }

    /**
     * Reads a newly included InputSreams
     *
     * @param inputStreams InputStreams to read
     */
    public void readInputStreams(List<Recognizer> recognizers, InputStream... inputStreams) {
        synchronized (readers) {
            stopReaders();

            RequestProcessor rp = RequestProcessor.getDefault();
            for(InputStream inputStream : inputStreams){
                // LoggerRunnable will close the stream if necessary.
                LoggerRunnable logger = new LoggerRunnable(recognizers, inputStream, false);
                readers.add(new WeakReference<LoggerRunnable>(logger));
                rp.post(logger);
            }
        }
    }
    
    /**     
     * Reads a newly included Files
     * 
     * @param files Files to read
     */
    public void readFiles(List<Recognizer> recognizers, File... files) {
        synchronized (readers) {
            stopReaders();
            
            RequestProcessor rp = RequestProcessor.getDefault();
            for(File file : files) {
                try {
                    // LoggerRunnable will close the stream.
                    LoggerRunnable logger = new LoggerRunnable(recognizers, new FileInputStream(file), true);
                    readers.add(new WeakReference<LoggerRunnable>(logger));
                    rp.post(logger);
                } catch (FileNotFoundException ex) {
                    LOGGER.log(Level.FINE, ex.getLocalizedMessage());
                }
            }
        }
    }
    
    public void stopReaders() {
        synchronized (readers) {
            for(WeakReference<LoggerRunnable> ref: readers) {
                LoggerRunnable logger = ref.get();
                if(logger != null) {
                    logger.stop();
                }
            }
            readers.clear();
        }
    }
    
    private void removeReader(LoggerRunnable logger) {
        synchronized (readers) {
            int size = readers.size();
            for(int i = 0; i < size; i++) {
                WeakReference<LoggerRunnable> ref = readers.get(i);
                if(logger == ref.get()) {
                    readers.remove(i);
                    break;
                }
            }
        }
    }
    
    /**
     * Writes a message into output
     * 
     * @param s message to write
     */
    public synchronized void write(String s, boolean error) {
        OutputWriter writer = getWriter(error);
        if(writer != null) {
            writer.print(s);
        }
    }

    /**
     * Writes a message into output, including a link to a portion of the
     * content being written.
     * 
     * @param s message to write
     */
    public synchronized void write(String s, OutputListener link, boolean important, boolean error) {
        try {
            OutputWriter writer = getWriter(error);
            if(writer != null) {
                writer.println(s, link, important);
            }
        } catch(IOException ex) {
            LOGGER.log(Level.FINE, ex.getLocalizedMessage(), ex);
        }
    }

    private OutputWriter getWriter(boolean error) {
        OutputWriter writer = error ? io.getErr() : io.getOut();
        if(LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "getIOWriter: closed = " + io.isClosed() + " [ " + (error ? "STDERR" : "STDOUT") + " ]" + ", output error flag = " + writer.checkError()); // NOI18N
        }
        if(writer.checkError() == true) {
            InputOutput newIO = getServerIO(uri);
            if(newIO == null) {
                if(LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.log(Level.INFO, "Unable to recreate I/O for " + uri + ", still in error state"); // NOI18N
                }
                writer = null;
            } else {
                io = newIO;
                writer = error ? io.getErr() : io.getOut();
            }
        }
        return writer;
    }

    private final Locale logLocale = getLogLocale();
    private final String logBundleName = getLogBundle();
    private final String localizedWarning = getLocalized(Level.WARNING.getName());
    private final String localizedSevere = getLocalized(Level.SEVERE.getName());
    private final Map<String, String> localizedLevels = getLevelMap();
    
    private Locale getLogLocale() {
        // XXX detect and use server language/country/variant instead of IDE's.
        String language = System.getProperty("user.language"); // NOI18N
        if(language != null) {
            return new Locale(language, System.getProperty("user.country", ""), System.getProperty("user.variant", "")); // NOI18N
        }
        return Locale.getDefault();
    }
    
    private String getLogBundle() {
        return Level.INFO.getResourceBundleName();
    }
    
    private String getLocalized(String text) {
        ResourceBundle bundle = ResourceBundle.getBundle(logBundleName, logLocale);
        String localized = bundle.getString(text);
        return localized != null ? localized : text;
    }

    private Map<String, String> getLevelMap() {
        Map<String, String> levelMap = new HashMap<String, String>();
        for(Level l: new Level [] { Level.ALL, Level.CONFIG, Level.FINE,
                Level.FINER, Level.FINEST, Level.INFO, Level.SEVERE, Level.WARNING } ) {
            String name = l.getName();
            levelMap.put(name, getLocalized(name));
        }
        return levelMap;
    }

    private String getLocalizedLevel(String level) {
        String localizedLevel = localizedLevels.get(level);
        return localizedLevel != null ? localizedLevel : level;
    }

    /**
     * Selects output panel
     */
    public synchronized void selectIO() {
        if(LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "selectIO: closed = " + io.isClosed() + ", output error flag = " + io.getOut().checkError()); // NOI18N
        }

        // Only select the output window if it's closed.  This makes sure it's
        // created properly and displayed.  However, if the user minimizes the
        // output window or switches to another one, we don't switch back.
        if(io.isClosed()) {
            io.select();

            // Horrible hack.  closed flag is never reset, so reset it after reopening.
            invokeSetClosed(io, false);
        }

        // If the user happened to close the OutputWindow TopComponent, reopen it.
        // Don't this check too often, but often enough.
        if(System.currentTimeMillis() > lastVisibleCheck + VISIBILITY_CHECK_DELAY) {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    if(visibleCheck.getAndSet(true)) {
                        try {
                            TopComponent tc = null;
                            if(outputTCRef != null) {
                                tc = outputTCRef.get();
                            }
                            if(tc == null) {
                                tc = WindowManager.getDefault().findTopComponent(OUTPUT_WINDOW_TCID);
                                if(tc != null) {
                                    outputTCRef = new WeakReference<TopComponent>(tc);
                                }
                            }
                            if(tc != null && !tc.isOpened()) {
                                tc.open();
                            }
                            lastVisibleCheck = System.currentTimeMillis();
                        } finally {
                            visibleCheck.set(false);
                        }
                    }
                }
            });
        }
    }

    private AtomicBoolean visibleCheck = new AtomicBoolean();
    private final long VISIBILITY_CHECK_DELAY = 60 * 1000;
    private final String OUTPUT_WINDOW_TCID = "output"; // NOI18N
    private volatile long lastVisibleCheck = 0;
    private WeakReference<TopComponent> outputTCRef =
            new WeakReference<TopComponent>(null);
    private volatile Method setClosedMethod;

    private void invokeSetClosed(InputOutput io, boolean closed) {
        if(setClosedMethod == null) {
            setClosedMethod = initSetClosedMethod(io);
        }
        if(setClosedMethod != null) {
            try {
                setClosedMethod.invoke(io, Boolean.valueOf(closed));
            } catch (Exception ex) {
                if(LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "invokeSetClosed", ex); // NOI18N
                }
            }
        }
    }

    private Method initSetClosedMethod(InputOutput io) {
        Method method = null;
        try {
            Class clazz = io.getClass();
            method = clazz.getDeclaredMethod("setClosed", boolean.class); // NOI18N
            method.setAccessible(true);
        } catch(Exception ex) {
            if(LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "initSetClosedMethod", ex); // NOI18N
            }
        }
        return method;
    }

    private class LoggerRunnable implements Runnable {

        private final List<Recognizer> recognizers;
        private final InputStream inputStream;
        private final boolean ignoreEof;
        private volatile boolean shutdown;
        
        public LoggerRunnable(List<Recognizer> recognizers, InputStream inputStream, boolean ignoreEof) {
            this.recognizers = recognizers;
            this.inputStream = inputStream;
            this.ignoreEof = ignoreEof;
            this.shutdown = false;
        }

        public void stop() {
            shutdown = true;
        }
        
        /**
         * Implementation of the Runnable interface. Here all tailing is
         * performed
         */
        @Override
        public void run() {
            final String originalName = Thread.currentThread().getName();
            BufferedReader reader = null;
            
            try {
                Thread.currentThread().setName(this.getClass().getName() + " - " + inputStream); // NOI18N

                reader = new BufferedReader(new InputStreamReader(inputStream));

                // ignoreEof is true for log files and false for process streams.
                // FIXME Should differentiate filter types more cleanly.
                Filter filter = ignoreEof ? new LogFileFilter(localizedLevels) : 
                    (uri.contains("]deployer:gfv3ee6:") ? new LogFileFilter(localizedLevels) :new StreamFilter());
                
                // read from the input stream and put all the changes to the I/O window
                char [] chars = new char[1024];
                int len = 0;

                while(!shutdown && len != -1) {
                    if(ignoreEof) {
                        // For file streams, only read if there is something there.
                        while(!shutdown && reader.ready()) {
                            String text = filter.process((char) reader.read());
                            if(text != null) {
                                processLine(text);
                            }
                        }
                    } else {
                        // For process streams, check for EOF every <DELAY> interval.
                        // We don't use readLine() here because it blocks.
                        while(!shutdown && (len = reader.read(chars)) != -1) {
                            for(int i = 0; i < len; i++) {
                                String text = filter.process(chars[i]);
                                if(text != null) {
                                    processLine(text);
                                }
                            }
                            if(!reader.ready()) {
                                break;
                            }
                        }
                    }
                    
                    // sleep for a while when the stream is empty
                    try {
                        if (ignoreEof) {
                            // read from file case... not associated with a process...
                            //     make sure there is no star
                            io.getErr().close();
                            io.getOut().close();
                        }
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "I/O exception reading server log", ex); // NOI18N
            } finally {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "I/O exception closing server log", ex); // NOI18N
                }
                
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "I/O exception closing stream buffer", ex); // NOI18N
                    }
                }
                
                removeReader(this);
                
                Thread.currentThread().setName(originalName);
            }
            io.getErr().close();
            io.getOut().close();
        }

        private void processLine(String line) {
            if(LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "processing text: '" + line + "'"); // NOI18N
            }
            // XXX sort of a hack to eliminate specific glassfish messages that
            // ought not to be printed at their current level (INFO vs FINE+).
            if(strictFilter && filter(line)) {
                return;
            }
            // Track level, color, listener
            Message message = new Message(line);
            message.process(recognizers);
                message.print();
                selectIO();
            if (shutdown) {
                // some messages get processed after the server has 'stopped'
                //    prevent new stars on the output caption.
                io.getErr().close();
                io.getOut().close();
            }
        }
    }

    private static Pattern colorPattern = Pattern.compile(
            "\\033\\[([\\d]{1,3})(?:;([\\d]{1,3}))?(?:;([\\d]{1,3}))?(?:;([\\d]{1,3}))?(?:;([\\d]{1,3}))?m"); // NOI18N

    private static final Color LOG_RED = new Color(204, 0, 0);
    private static final Color LOG_GREEN = new Color(0, 192, 0);
    private static final Color LOG_YELLOW = new Color(204, 204, 0);
    private static final Color LOG_BLUE = Color.BLUE;
    private static final Color LOG_MAGENTA = new Color(204, 0, 204);
    private static final Color LOG_CYAN = new Color(0, 153, 255);

    private static Color [] colorTable = {
        Color.BLACK, LOG_RED, LOG_GREEN, LOG_YELLOW, LOG_BLUE, LOG_MAGENTA, LOG_CYAN,
    };

    private class Message {

        private String message;
        private int level;
        private Color color;
        private OutputListener listener;

        public Message(String line) {
            message = line;
        }

        void process(List<Recognizer> recognizers) {
            processLevel();
            processColors();
            processRecognizers(recognizers);
        }

        private void processLevel() {
            level = 0;
            int colon = message.substring(0, Math.min(message.length(), 15)).indexOf(':');
            if(colon != -1) {
                try {
                    String levelPrefix = message.substring(0, colon);
                    level = Level.parse(levelPrefix).intValue();
                } catch(IllegalArgumentException ex) {
                }
            }
        }

        private void processColors() {
            try {
                Matcher matcher = colorPattern.matcher(message);
                boolean result = matcher.find();
                if(result) {
                    StringBuffer sb = new StringBuffer(message.length());
                    do {
                        int count = matcher.groupCount();
                        for(int i = 1; i < count && matcher.group(i) != null; i++) {
                            int code = Integer.parseInt(matcher.group(i));
                            if(code >= 30 && code <= 36 && color == null) {
                                color = colorTable[code - 30];
                            }
                        }
                        matcher.appendReplacement(sb, "");
                        result = matcher.find();
                    } while(result);
                    matcher.appendTail(sb);
                    message = sb.toString();
                }
            } catch(Exception ex) {
                Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
            }
            if(color == null && level > 0) {
                if(level <= Level.FINE.intValue()) {
                    color = LOG_GREEN;
                } else if(level <= Level.INFO.intValue()) {
                    color = Color.GRAY;
                }
            }
        }

        private void processRecognizers(List<Recognizer> recognizers) {
            // Don't run recognizers on excessively long lines
            if(message.length() > 500) {
                return;
            }
            Iterator<Recognizer> iterator = recognizers.iterator();
            while(iterator.hasNext() && listener == null) {
                Recognizer r = iterator.next();
                try {
                    listener = r.processLine(message);
                } catch (Exception ex) {
                    Logger.getLogger("glassfish").log(Level.INFO, "Recognizer " + r.getClass().getName() + " generated an exception.", ex);
                }
            }
        }

        void print() {
            OutputWriter writer = getWriter(level >= 900);
            try {
                if(color != null && listener == null && IOColorLines.isSupported(io)) {
                    message = stripNewline(message);
                    IOColorLines.println(io, message, color);
                } else if(writer != null) {
                    if(listener != null) {
                        message = stripNewline(message);
                        writer.println(message, listener, false);
                    } else {
                        writer.print(message);
                    }
                }
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, ex.getLocalizedMessage(), ex);
            }
        }

    }

    private boolean filter(String line) {
        return line.startsWith("INFO: Started bundle ")
                || line.startsWith("INFO: Stopped bundle ")
                || line.startsWith("INFO: ### ")
                || line.startsWith("felix.")
                || line.startsWith("log4j:")
                ;
    }

    private static final String stripNewline(String s) {
        int len = s.length();
        if(len > 0 && '\n' == s.charAt(len-1)) {
            s = s.substring(0, len-1);
        }
        return s;
    }
    
    private boolean isWarning(String line) {
        return line.startsWith(localizedWarning) || line.startsWith(localizedSevere);
    }

    private static interface Filter {
        
        public String process(char c);
        
    }
    
    private static abstract class StateFilter implements Filter {
        
        protected String message;
        
        protected int state;
        protected StringBuilder msg;
        
        StateFilter() {
            state = 0;
            msg = new StringBuilder(128);
        }
        
        protected void reset() {
            message = ""; // NOI18N
        }
        
        @Override
        public abstract String process(char c);
        
    }
    
    private static final class StreamFilter extends StateFilter {

        private static final Pattern messagePattern = Pattern.compile("([\\p{Lu}]{0,16}?):|([^\\r\\n]{0,24}?\\d\\d?:\\d\\d?:\\d\\d?)"); // NOI18N
        
        private String line;
        
        public StreamFilter() {
            reset();
        }

        @Override
        protected void reset() {
            super.reset();
            line = ""; // NOI18N
        }

        /**
         * GlassFish server log format, when read from process stream:
         *
         * Aug 13, 2008 3:01:49 PM com.sun.enterprise.glassfish.bootstrap.ASMain main
         * INFO: Launching GlassFish on Apache Felix OSGi platform
         * Aug 13, 2008 3:01:50 PM com.sun.enterprise.glassfish.bootstrap.ASMainHelper setUpOSGiCache
         * INFO: Removing Felix cache profile dir /space/tools/v3Aug7/domains/domain1/.felix/gf left from a previous run
         * 
         * Welcome to Felix.
         * =================
         * 
         * Aug 13, 2008 3:01:51 PM HK2Main start
         * INFO: contextRootDir = /space/tools/v3Aug7/modules
         * ...
         * Aug 13, 2008 3:02:14 PM
         * SEVERE: Exception in thread "pool-6-thread-1"
         * Aug 13, 2008 3:02:14 PM org.glassfish.scripting.rails.RailsDeployer load
         * INFO: Loading application RailsGFV3 at /RailsGFV3
         * Aug 13, 2008 3:02:14 PM
         * SEVERE: /...absolute.path.../connection_specification.rb:232:in `establish_connection':
         *
         * !PW FIXME This parser should be checked for I18N stability.
         */
        @Override
        public String process(char c) {
            String result = null;

            if(c == '\n') {
                if(msg.length() > 0) {
                    msg.append(c);
                    line = msg.toString();
                    msg.setLength(0);

                    Matcher matcher = messagePattern.matcher(line);
                    if(matcher.find() && matcher.start() == 0 && matcher.groupCount() > 1 && matcher.group(2) != null) {
                        result = null;
                    } else {
                        result = line;
                    }
                }
            } else if(c != '\r') {
                msg.append(c);
            }

            return result;
        }

    }

    private static final class LogFileFilter extends StateFilter {
        
        private String time;
        private String type;
        private String version;
        private String classinfo;
        private String threadinfo;
        private boolean multiline;
        private final Map<String, String> typeMap;

        public LogFileFilter(Map<String, String> typeMap) {
            this.typeMap = typeMap;
            reset();
        }

        @Override
        protected void reset() {
            super.reset();
            time = type = version = classinfo = threadinfo = ""; // NOI18N
            multiline = false;
        }
        
        private String getLocalizedType(String type) {
            String localizedType = typeMap.get(type);
            return localizedType != null ? localizedType : type;
        }

        /**
         * GlassFish server log entry format (unformatted), when read from file:
         *
         * [#|
         *    2008-07-20T16:59:11.738-0700|
         *    INFO|
         *    GlassFish10.0|
         *    org.jvnet.hk2.osgiadapter|
         *    _ThreadID=11;_ThreadName=Thread-6;org.glassfish.admin.config-api [1794];|
         *    Started bundle org.glassfish.admin.config-api [1794]
         * |#]
         *
         * !PW FIXME This parser should be checked for I18N stability.
         */
        @Override
        public String process(char c) {
            String result = null;

            switch(state) {
                case 0:
                    if(c == '[') {
                        state = 1;
                    } else {
                        if(c == '\n') {
                            if(msg.length() > 0) {
                                msg.append(c);
                                result = msg.toString();
                                msg.setLength(0);
                            }
                        } else if(c != '\r') {
                            msg.append(c);
                        }
                    }
                    break;
                case 1:
                    if(c == '#') {
                        state = 2;
                    } else {
                        state = 0;
                        if(c == '\n') {
                            if(msg.length() > 0) {
                                msg.append(c);
                                result = msg.toString();
                                msg.setLength(0);
                            }
                        } else if(c != '\r') {
                            msg.append('[');
                            msg.append(c);
                        }
                    }
                    break;
                case 2:
                    if(c == '|') {
                        state = 3;
                        msg.setLength(0);
                    } else {
                        if(c == '\n') {
                            if(msg.length() > 0) {
                                msg.append(c);
                                result = msg.toString();
                                msg.setLength(0);
                            }
                        } else if(c != '\r') {
                            state = 0;
                            msg.append('[');
                            msg.append('#');
                            msg.append(c);
                        }
                    }
                    break;
                case 3:
                    if(c == '|') {
                        state = 4;
                        time = msg.toString();
                        msg.setLength(0);
                    } else {
                        msg.append(c);
                    }
                    break;
                case 4:
                    if(c == '|') {
                        state = 5;
                        type = getLocalizedType(msg.toString());
                        msg.setLength(0);
                    } else {
                        msg.append(c);
                    }
                    break;
                case 5:
                    if(c == '|') {
                        state = 6;
                        version = msg.toString();
                        msg.setLength(0);
                    } else {
                        msg.append(c);
                    }
                    break;
                case 6:
                    if(c == '|') {
                        state = 7;
                        classinfo = msg.toString();
                        msg.setLength(0);
                    } else {
                        msg.append(c);
                    }
                    break;
                case 7:
                    if(c == '|') {
                        state = 8;
                        threadinfo = msg.toString();
                        msg.setLength(0);
                    } else {
                        msg.append(c);
                    }
                    break;
                case 8:
                    if(c == '|') {
                        state = 9;
                        message = msg.toString();
                    } else if(c == '\n') {
                        if(msg.length() > 0) { // suppress blank lines in multiline messages
                            msg.append('\n');
                            result = !multiline ? type + ": " + msg.toString() : msg.toString(); // NOI18N
                            multiline = true;
                            msg.setLength(0);
                        }
                    } else if(c != '\r') {
                        msg.append(c);
                    }
                    break;
                case 9:
                    if(c == '#') {
                        state = 10;
                    } else {
                        state = 8;
                        msg.append('|');
                        msg.append(c);
                    }
                    break;
                case 10:
                    if(c == ']') {
                        state = 0;
                        msg.setLength(0);
                        result = (multiline ? message : type + ": " + message) + '\n'; // NOI18N
                        reset();
                    } else {
                        state = 8;
                        msg.append('|');
                        msg.append('#');
                        msg.append(c);
                    }
                    break;
            }
            return result;
        }
    }
    
    private static final WeakHashMap<ServerInstance, InputOutput> ioWeakMap = 
            new WeakHashMap<ServerInstance, InputOutput>();
    
    public static InputOutput getServerIO(String uri) {

        ServerInstance si = null;
        Iterator<GlassfishInstanceProvider> iterator = GlassfishInstanceProvider.getProviders(true).iterator();
        while(si == null && iterator.hasNext()) {
            GlassfishInstanceProvider provider = iterator.next();
            si = provider.getInstance(uri);
        }
        if(null == si) {
            return null;
        }

        synchronized (ioWeakMap) {
            // look in the cache
            InputOutput serverIO = ioWeakMap.get(si);
            if(serverIO != null) {
                boolean valid = true;
                if(serverIO.isClosed()) {
                    if(LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Output window for " + uri + " is closed."); // NOI18N
                    }
                }
                if(serverIO.getOut().checkError()) {
                    if(LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Standard out for " + uri + " is in error state."); // NOI18N
                    }
                    valid = false;
                }
                if(serverIO.getErr().checkError()) {
                    if(LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Standard error for " + uri + " is in error state."); // NOI18N
                    }
                    valid = false;
                }

                if(valid) {
                    return serverIO;
                } else {
                    if(!serverIO.isClosed()) {
                        serverIO.closeInputOutput();
                    }
                    ioWeakMap.put(si, null);
                }
            }
        }

        // look up the node that belongs to the given server instance
        Node node = si.getFullNode();

        // it looks like that the server instance has been removed
        if (node == null) {
            return null;
        }

        // No server control interface...
        GlassfishModule commonSupport = node.getLookup().lookup(GlassfishModule.class);
        if(commonSupport == null) {
            return null;
        }

        Action[] actions = new Action[] {
            new StartServerAction.OutputAction(commonSupport),
            new DebugAction.OutputAction(commonSupport),
            new RestartAction.OutputAction(commonSupport),
            new StopServerAction.OutputAction(commonSupport),
            new RefreshAction.OutputAction(commonSupport)
        };

        InputOutput newIO = null;
        synchronized (ioWeakMap) {
            newIO = ioWeakMap.get(si);
            if(newIO == null) {
                newIO = IOProvider.getDefault().getIO(si.getDisplayName(), actions);
                ioWeakMap.put(si, newIO);
            }
        }
        return newIO;
    }
}
