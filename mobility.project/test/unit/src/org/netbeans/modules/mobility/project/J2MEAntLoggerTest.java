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

/*
 * J2MEAntLoggerTest.java
 * JUnit based test
 *
 * Created on April 19, 2005, 5:55 PM
 */
package org.netbeans.modules.mobility.project;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.*;
import org.apache.tools.ant.module.run.LoggerTrampoline;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.WeakSet;
import org.openide.windows.OutputListener;
//import org.openide.windows.OutputListener;


/**
 * Tests J2MEAntLogger functionality
 *
 * @author Michal Skvor
 */
public class J2MEAntLoggerTest extends NbTestCase {
    static final Object syncObj=new Object();
    static
    {
        TestUtil.setLookup( new Object[] {            
        }, J2MEAntLoggerTest.class.getClassLoader());
        
        Logger.getLogger("org.openide.util.RequestProcessor").addHandler(new Handler() {
                public void publish(LogRecord record) {
                    String s=record.getMessage();
                    if (s==null)
                        return;
                    if (s.startsWith("Work finished") &&
                            s.indexOf("J2MEProject$6")!=-1 &&
                            s.indexOf("RequestProcessor")!=-1) {
                        synchronized (syncObj) {
                            syncObj.notify();
                        }
                    }
                }
                public void flush() {}
                public void close() throws SecurityException {}
            });
    }
    
    void waitFinished()
    {
        while (true)
        {
            try   {
                J2MEAntLoggerTest.syncObj.wait();
                break;
            }
            catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public J2MEAntLoggerTest(String testName) {
        super(testName);
        
        TestUtil.setEnv();
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(J2MEAntLoggerTest.class);
        
        return suite;
    }
    
    /**
     * Test of messageLogged method, of class org.netbeans.modules.mobility.project.J2MEAntLogger.
     */
    public void testMessageLogged() {
        String translation=File.separator+"my"+File.separator+"path"+File.separator+"to"+File.separator+"rule"+File.separator;
        //this funny stuff is just to escape backslashes and dollar characters in a replacement path string using String.replaceAll(regexp, regexp)
        String CHARSTOESCAPE = "([\\\\\\$])"; //NOI18N
        String ESCAPESEQUENCE = "\\\\$1"; //NOI18N
        String separator = File.separatorChar == '\\' ? "\\\\" : "/"; //NOI18N
        
        AntLogger[] loggers = new AntLogger[] { new J2MEAntLogger(), new BogusAntLogger() };
        BogusAntSession session = new BogusAntSession(loggers, AntEvent.LOG_INFO);
        AntSession realSession = LoggerTrampoline.ANT_SESSION_CREATOR.makeAntSession(session);
        
        //Simulate the way J2ME logger use putCustomData
        realSession.putCustomData(loggers[0], new File(translation).getPath().replaceAll(CHARSTOESCAPE, ESCAPESEQUENCE)+separator);
        
        session.sendMessageLogged(
                makeAntEvent(realSession, File.separator+"this"+File.separator+"is"+File.separator+"some"+File.separator+"path"+File.separator+"to"+File.separator+"something", AntEvent.LOG_WARN, null, null, null));
        session.sendMessageLogged(
                makeAntEvent(realSession, File.separator+"path"+File.separator+"build"+File.separator+"preprocessed"+File.separator+"something", AntEvent.LOG_WARN, null, null, null));
        session.sendMessageLogged(
                makeAntEvent(realSession, File.separator+"build"+File.separator+"preprocessed"+File.separator+"something", AntEvent.LOG_WARN, null, null, null));
        session.sendMessageLogged(
                makeAntEvent(realSession, File.separator+"build"+File.separator+"preprocessed"+File.separator+"config"+File.separator+"something", AntEvent.LOG_WARN, null, null, null));
        
        List/*Message*/ expectedMessages = Arrays.asList(new Message[] {
            new Message(File.separator+"this"+File.separator+"is"+File.separator+"some"+File.separator+"path"+File.separator+"to"+File.separator+"something", false),
            new Message(translation+"something", false),
            new Message(translation+"something", false),
            new Message(translation+"config"+File.separator+"something", false)
        });
        
        assertEquals("correct text printed", expectedMessages, session.messages);
    }
    
    public void testInterestedInScript() throws Exception {
        File workDir = getWorkDir();
        File proj = new File(workDir, "testProject");
        
        TestUtil.makeScratchDir(this);
        
        TestUtil.setLookup( new Object[] {
            TestUtil.testProjectFactory(),
            TestUtil.testProjectChooserFactory()
        }, J2MEAntLoggerTest.class.getClassLoader());
 
        synchronized(J2MEAntLoggerTest.syncObj) {
            J2MEProjectGenerator.createNewProject(proj, "testProject", null, null,null);
            waitFinished();
        }
        
        AntLogger[] loggers = new AntLogger[] { new J2MEAntLogger() };
        BogusAntSession session = new BogusAntSession(loggers, AntEvent.LOG_INFO);
        AntSession realSession = LoggerTrampoline.ANT_SESSION_CREATOR.makeAntSession(session);
        
        assertFalse( "is false", loggers[0].interestedInScript( new File(File.separator),  realSession ));
        assertFalse( "is false", loggers[0].interestedInScript( new File(proj,"bogus"),  realSession ));
        
        assertTrue( "is true", loggers[0].interestedInScript(
                new File( new File(proj, "nbproject"), "build-impl.xml"),  realSession ));
        
    }
    
    /**
     * Create an event to be delivered.
     */
    private static AntEvent makeAntEvent(AntSession realSession, String message, int level, Throwable exc, String target, String task) {
        return LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new BogusAntEvent(realSession, message, level, exc, target, task));
    }
    
    /**
     * Struct representing a message printed to an output stream.
     */
    private static final class Message {
        
        public final String message;
        public final boolean err;
        
        public Message(String message, boolean err) {
            this.message = message;
            this.err = err;
        }
        
        public boolean equals(Object o) {
            if (o instanceof Message) {
                Message m = (Message) o;
                return m.message.equals(message) && m.err == err;
            } else {
                return false;
            }
        }
        
        public String toString() {
            return message;
        }
        
    }
    
    /**
     * Just Ant session for testing purpose
     */
    private static final class BogusAntSession implements LoggerTrampoline.AntSessionImpl {
        
        private final AntLogger[] loggers;
        private final int verbosity;
        private final Map/*<AntLogger,Object>*/ customData = new WeakHashMap();
        private final Set/*<Throwable>*/ consumedExceptions = new WeakSet();
        public final List/*<Message>*/ messages = new ArrayList();
        
        public BogusAntSession(AntLogger[] loggers, int verbosity) {
            this.loggers = loggers;
            this.verbosity = verbosity;
        }
        
        public void sendMessageLogged(AntEvent event) {
            for (int i = 0; i < loggers.length; i++) {
                int[] levels = loggers[i].interestedInLogLevels(event.getSession());
                Arrays.sort(levels);
                if (Arrays.binarySearch(levels, event.getLogLevel()) >= 0) {
                    loggers[i].messageLogged(event);
                }
            }
        }
        
        public void sendBuildStarted(AntEvent event) {
            for (int i = 0; i < loggers.length; i++) {
                loggers[i].buildStarted(event);
            }
        }
        
        public void sendBuildFinished(AntEvent event) {
            for (int i = 0; i < loggers.length; i++) {
                loggers[i].buildFinished(event);
            }
        }
        
        public void sendBuildInitializationFailed(AntEvent event) {
            for (int i = 0; i < loggers.length; i++) {
                loggers[i].buildInitializationFailed(event);
            }
        }
        
        public void sendTargetStarted(AntEvent event) {
            for (int i = 0; i < loggers.length; i++) {
                loggers[i].targetStarted(event);
            }
        }
        
        public void sendTargetFinished(AntEvent event) {
            for (int i = 0; i < loggers.length; i++) {
                loggers[i].targetFinished(event);
            }
        }
        
        public void sendTaskStarted(AntEvent event) {
            for (int i = 0; i < loggers.length; i++) {
                loggers[i].taskStarted(event);
            }
        }
        
        public void sendTaskFinished(AntEvent event) {
            for (int i = 0; i < loggers.length; i++) {
                loggers[i].taskFinished(event);
            }
        }
        
        public void deliverMessageLogged(AntEvent originalEvent, String message, int level) {
            sendMessageLogged(makeAntEvent(originalEvent.getSession(), message, level, null, originalEvent.getTargetName(), originalEvent.getTaskName()));
        }
        
        public OutputListener createStandardHyperlink(URL file, String message, int line1, int column1, int line2, int column2) {
            // Our Ant logger does not support hyperlinks
            return null;
        }
        
        public void println(String message, boolean err, OutputListener listener) {
            messages.add(new Message(message, err));
        }
        
        public void putCustomData(AntLogger logger, Object data) {
            customData.put(logger, data);
        }
        
        public boolean isExceptionConsumed(Throwable t) { // copied from NbBuildLogger
            if (consumedExceptions.contains(t)) {
                return true;
            }
            Throwable nested = t.getCause();
            if (nested != null && isExceptionConsumed(nested)) {
                consumedExceptions.add(t);
                return true;
            }
            return false;
        }
        
        public void consumeException(Throwable t) throws IllegalStateException { // copied from NbBuildLogger
            if (isExceptionConsumed(t)) {
                throw new IllegalStateException();
            }
            consumedExceptions.add(t);
        }
        
        public Object getCustomData(AntLogger logger) {
            return customData.get(logger);
        }
        
        public int getVerbosity() {
            return verbosity;
        }
        
        public String[] getOriginatingTargets() {
            return new String[] {"mock-target"};
        }
        
        public File getOriginatingScript() {
            return new File(System.getProperty("java.io.tmpdir"), "mock-script");
        }
        
        public String getDisplayName() {
            return "Mock Session";
        }
    }
    
    /**
     * An event class holding events
     */
    private static final class BogusAntEvent implements LoggerTrampoline.AntEventImpl {
        
        private final AntSession session;
        private final String message;
        private final int level;
        private final Throwable exc;
        private final String target;
        private final String task;
        private boolean consumed = false;
        
        public BogusAntEvent(AntSession session, String message, int level, Throwable exc, String target, String task) {
            this.session = session;
            this.message = message;
            this.level = level;
            this.exc = exc;
            this.target = target;
            this.task = task;
        }
        
        public String evaluate(String text) {
            return null;
        }
        
        public String getProperty(String name) {
            return null;
        }
        
        public boolean isConsumed() {
            return consumed;
        }
        
        public void consume() throws IllegalStateException {
            if (consumed) {
                throw new IllegalStateException();
            } else {
                consumed = true;
            }
        }
        
        public Throwable getException() {
            return exc;
        }
        
        public int getLine() {
            return -1;
        }
        
        public int getLogLevel() {
            return level;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Set/*<String>*/ getPropertyNames() {
            return Collections.EMPTY_SET;
        }
        
        public File getScriptLocation() {
            return null;
        }
        
        public AntSession getSession() {
            return session;
        }
        
        public String getTargetName() {
            return target;
        }
        
        public String getTaskName() {
            return task;
        }
        
        public TaskStructure getTaskStructure() {
            return null;
        }
    }
    
    /**
     * Simple logger which delivers all messages to the session
     */
    private static final class BogusAntLogger extends AntLogger {
        
        public BogusAntLogger() {}
        
        public void messageLogged(AntEvent event) {
            if (event.isConsumed()) {
                return;
            }
            AntSession session = event.getSession();
            session.println(event.getMessage(), false, null);
        }
        
        public String[] interestedInTasks(AntSession session) {
            return AntLogger.ALL_TASKS;
        }
        
        public String[] interestedInTargets(AntSession session) {
            return AntLogger.ALL_TARGETS;
        }
        
        public boolean interestedInSession(AntSession session) {
            return true;
        }
        
        public int[] interestedInLogLevels(AntSession session) {
            return new int[] {
                AntEvent.LOG_INFO,
                AntEvent.LOG_WARN,
                AntEvent.LOG_ERR,
            };
        }
        
        public boolean interestedInAllScripts(AntSession session) {
            return true;
        }
        
        public boolean interestedInScript(File script, AntSession session) {
            return true;
        }
    }
}
