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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.maven.embedder.MavenEmbedderLogger;
import org.apache.maven.monitor.event.EventMonitor;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.embedder.exec.MyLifecycleExecutor;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.openide.execution.ExecutorTask;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.io.NullOutputStream;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * handling of output coming from maven builds.
 * @author Milos Kleint 
 */
class JavaOutputHandler extends AbstractOutputHandler implements EventMonitor, MavenEmbedderLogger {
    private static final String SEC_MOJO_EXEC = "mojo-execute";//NOI18N
    private static final String SEC_PRJ_EXEC = "project-execute";//NOI18N
    private static final String SEC_REAC_EXEC = "reactor-execute";//NOI18N
    
    private InputOutput inputOutput;
    
    private OutputWriter stdOut, stdErr;
    
    private StreamBridge out, err;
    
    private InputStream in;
    
    private int threshold = MavenEmbedderLogger.LEVEL_INFO;
    

    private AggregateProgressHandle handle;

    private boolean doCancel = false;

    private ExecutorTask task;
    
    
    private List<ProgressContributor> progress = new ArrayList<ProgressContributor>();
    private boolean isReactor = false;
    private ProgressContributor cont;
    private int total = 10;
    private int count = 0;

    private static final RequestProcessor PROCESSOR = new RequestProcessor("Maven Embedded Input Redirection", 5); //NOI18N

    
    JavaOutputHandler() {
    }
    
    /**
     * @deprecated for tests only..
     */
    void setup(HashMap procs, OutputWriter std, OutputWriter err) {
        processors = procs;
        stdErr = err;
        stdOut = std;
    }
    
    public JavaOutputHandler(InputOutput io, Project proj, AggregateProgressHandle hand, RunConfig config)    {
        this();
        inputOutput = io;
        handle = hand;
        stdOut = inputOutput.getOut();
        stdErr = inputOutput.getErr();
        
        initProcessorList(proj, config);
    }
    
    public void errorEvent(String eventName, String target, long l, Throwable throwable) {
        processFail(getEventId(eventName, target), stdErr);
    }
    
    public void startEvent(String eventName, String target, long l)    {
        processStart(getEventId(eventName, target), stdOut);
        if (handle != null) {
            if (SEC_REAC_EXEC.equals(eventName)) { //NOI18N
                isReactor = true;
            }
            if (isReactor && SEC_PRJ_EXEC.equals(eventName)) { //NOI18N
                isReactor = false;
                int bufferSize = MyLifecycleExecutor.getAffectedProjects().size();
                for (int i = 0; i < bufferSize; i++) {
                    ProgressContributor contr = AggregateProgressFactory. createProgressContributor("project" + i); //NOI18N
                    handle.addContributor(contr);
                    progress.add(contr);
                }
            }
            if (SEC_PRJ_EXEC.equals(eventName)) { //NOI18N
                if (progress.size() > 0) {
                    cont = progress.remove(0);
                    cont.start(1);
                } else {
                    cont = AggregateProgressFactory. createProgressContributor("project"); //NOI18N
                }
                // instead of one, possibly try to guess the number of steps in project build..
                count = 0;
                cont.start(total);
            }
            if (SEC_MOJO_EXEC.equals(eventName)) {
                count = count + 1;
                if (count < total) {
                    cont.progress(target, count);
                }
            }
        }
        if (cont != null) {
            cont.progress(target);
        }
    }
    
    public void endEvent(String eventName, String target, long l)    {
        processEnd(getEventId(eventName, target), stdOut);
        if (SEC_PRJ_EXEC.equals(eventName) &&  cont != null) { //NOI18N
            total = count;
            cont.finish();
        }
        if (doCancel) {
            assert task != null;
            task.stop();
        }
    }
    
    public void debug(String string) {
        if (isDebugEnabled()) {
            processMultiLine(string, stdOut, "DEBUG");//NOI18N
        }
    }
    
    public void debug(String string, Throwable throwable) {
        if (isDebugEnabled()) {
            processMultiLine(string, stdOut, "DEBUG");//NOI18N
            throwable.printStackTrace(stdOut);
        }
    }
    
    public boolean isDebugEnabled()    {
        return threshold == MavenEmbedderLogger.LEVEL_DEBUG;
    }
    
    public void info(String string)    {
        processMultiLine(string, stdOut, /*"INFO"*/ "");//NOI18N
    }
    
    public void info(String string, Throwable throwable)    {
        processMultiLine( string, stdOut, /*"INFO"*/ "");//NOI18N
        throwable.printStackTrace(stdOut);
    }
    
    public boolean isInfoEnabled()    {
        return true;
    }
    
    public void warn(String string)    {
        if (string.startsWith("Unable to get resource from repository")) { //NOI18N
            if (isDebugEnabled()) {
                processMultiLine(string, stdOut, "DEBUG");//NOI18N
            }
            return;
        }
        //TEMPORARY - only relevant when 2.1 gets out
        if (string.startsWith("The <pluginRepositories/> section of the POM has been deprecated")) { //NOI18N
            if (isDebugEnabled()) {
                processMultiLine(string, stdOut, "DEBUG");//NOI18N
            }
            return;
        }
        
        processMultiLine(string, stdOut, "WARN");//NOI18N
    }
    
    public void warn(String string, Throwable throwable)    {
        processMultiLine(string, stdOut, "WARN");//NOI18N
        throwable.printStackTrace(stdOut);
    }
    
    public boolean isWarnEnabled()    {
        return true;
    }
    
    public void error(String string)    {
        processMultiLine(string, stdErr, "ERROR");//NOI18N
    }
    
    public void error(String string, Throwable throwable)    {
        processMultiLine(string, stdErr, "ERROR");//NOI18N
        throwable.printStackTrace(stdErr);
    }
    
    public boolean isErrorEnabled()    {
        return true;
    }
    
    public void fatalError(String string)    {
        processMultiLine(string, stdErr, "FATAL");//NOI18N
    }
    
    public void fatalError(String string, Throwable throwable)    {
        processMultiLine(string, stdErr, "FATAL");//NOI18N
        throwable.printStackTrace(stdErr);
    }
    
    public boolean isFatalErrorEnabled()    {
        return true;
    }
    
    public void setThreshold(int i)    {
        threshold = i;
    }
    
    public int getThreshold()    {
        return threshold;
    }
 
    
    PrintStream getErr() {
        if (err == null) {
            err =  new StreamBridge(stdErr);
        }
        return err;
    }

    InputStream getIn() {
        if (in == null) {
            try {
                PipedInputStream inS = new PipedInputStream();
                PipedOutputStream ouS = new PipedOutputStream();
                inS.connect(ouS);
                CommandLineOutputHandler.Input inp = new CommandLineOutputHandler.Input(ouS, inputOutput);
                PROCESSOR.post(inp);
                in = inS;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return in;
    }

    PrintStream getOut() {
        if (out == null) {
            out = new StreamBridge(stdOut);
        }
        return out;
    }

    void requestCancel(ExecutorTask task) {
        doCancel = true;
        this.task = task;
    }
    
            
    
    private static RequestProcessor PRCS = new RequestProcessor();
    
    private class StreamBridge extends PrintStream implements Runnable {
        StringBuffer buff = new StringBuffer();
        private OutputWriter writer;
        RequestProcessor.Task task;
        public StreamBridge(OutputWriter wr) {
            super(new NullOutputStream());
            writer = wr;
            task = PRCS.create(this);
        }

        public synchronized void run() {
            if (buff.length() > 0) {
                writer.print(buff.toString());
                buff.setLength(0);
            }
        }

        
        @Override
        public synchronized void flush() {
            if (buff.length() > 0) {
                doPrint();
            }
        }
        
        @Override
        public synchronized void print(long l) {
            buff.append(l);
            task.schedule(500);
        }
        
        @Override
        public synchronized void print(char[] s) {
            buff.append(s);
            task.schedule(500);
        }
        
        @Override
        public synchronized void print(int i) {
            buff.append(i);
            task.schedule(500);
        }
        
        @Override
        public synchronized void print(boolean b) {
            buff.append(b);
            task.schedule(500);
        }
        
        @Override
        public synchronized void print(char c) {
            buff.append(c);
            task.schedule(500);
        }
        
        @Override
        public synchronized void print(float f) {
            buff.append(f);
            task.schedule(500);
        }
        
        @Override
        public synchronized void print(double d) {
            buff.append(d);
            task.schedule(500);
        }
        
        @Override
        public synchronized void print(Object obj) {
            buff.append(obj.toString());
            task.schedule(500);
        }
        
        @Override
        public synchronized void print(String s) {
            buff.append(s);
            task.schedule(500);
        }
        
        @Override
        public synchronized void println(double x) {
            buff.append(x);
            doPrint();
        }
        
        @Override
        public synchronized void println(Object x) {
            buff.append(x.toString());
            doPrint();
        }
        
        @Override
        public synchronized void println(float x) {
            buff.append(x);
            doPrint();
        }
        
        @Override
        public synchronized void println(int x) {
            buff.append(x);
            doPrint();
        }

        @Override
        public synchronized void println(char x) {
            buff.append(x);
            doPrint();
        }
        
        @Override
        public synchronized void println(boolean x) {
            buff.append(x);
            doPrint();
        }
        
        @Override
        public synchronized void println(String x) {
            buff.append(x);
            doPrint();
        }
        
        @Override
        public synchronized void println(char[] x) {
            buff.append(x);
            doPrint();
        }
        
        @Override
        public synchronized void println() {
            doPrint();
        }
        
        @Override
        public synchronized void println(long x) {
            buff.append(x);
            doPrint();
        }
        
        @Override
        public synchronized void write(int b) {
            buff.append((char)b);
            task.schedule(500);
        }
        
        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }
        
        @Override
        public synchronized void write(byte[] b, int off, int len) {
            ByteArrayInputStream bais = new ByteArrayInputStream(b, off, len);
            Reader read = new InputStreamReader(bais);
            try {
                while (read.ready()) {
                    buff.append((char)read.read());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            task.schedule(500);
        }
        
        private void doPrint() {
            assert Thread.holdsLock(this);
            processMultiLine(buff.toString(), writer, "");//NOI18N
            buff.setLength(0);
        }

    }

    @Override
    MavenEmbedderLogger getLogger() {
        return this;
    }

    public void close() {
    }
    
}
