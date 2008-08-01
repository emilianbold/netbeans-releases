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
package org.netbeans.bluej.ant.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.Redirector;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * Ant task for redirecting the output of java task to the netbeans output window.
 * @author Milos Kleint
 */
public class BlueJava extends Java  {
    
    public BlueJava() {
        redirector = new MyRedirector(this);
    }

    
    private class MyRedirector extends Redirector {
        public MyRedirector(Task task) {
            super(task);
        }
        public ExecuteStreamHandler createHandler() throws BuildException {
            createStreams();
            return new NbOutputStreamHandler(getProject().getName());
        }
        
    }
    
    private static final RequestProcessor PROCESSOR = new RequestProcessor("Netbeans-Bluej Run IO redirection", 5);
    /**
     * All tabs which were used for some process which has now ended.
     * These are closed when you start a fresh process.
     * Map from tab to tab display name.
     * @see "#43001"
     */
    private static final Map freeTabs = new WeakHashMap();
    
    
    private static class NbOutputStreamHandler implements ExecuteStreamHandler {
        private InputOutput io;
        private RequestProcessor.Task outTask;
        private RequestProcessor.Task errTask;
        private RequestProcessor.Task inTask;
        private Input input;
        private String displayName;
        public NbOutputStreamHandler(String name) {
            displayName = "Run " + name;
                // OutputWindow
//                if (AntSettings.getDefault().getAutoCloseTabs()) { // #47753
                synchronized (freeTabs) {
                    Iterator it = freeTabs.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry entry = (Map.Entry)it.next();
                        InputOutput free = (InputOutput)entry.getKey();
                        String freeName = (String)entry.getValue();
                        if (io == null && freeName.equals(displayName)) {
                            // Reuse it.
                            io = free;
                            try {
                                io.getOut().reset();
                                io.getErr().reset();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                                // useless: io.flushReader();
                        } else {
                                // Discard it.
                            free.closeInputOutput();
                        }
                    }
                    freeTabs.clear();
                }
//                }
                if (io == null) {
                    io = IOProvider.getDefault().getIO(displayName, true);
                }
            
        }
        public void stop() {
            if (input != null) {
                input.closeReader();
            }
            if (inTask != null) {
                inTask.waitFinished();
            }
            if (errTask != null) {
                errTask.waitFinished();
            }
            if (outTask != null) {
                outTask.waitFinished();
            }
            synchronized (freeTabs) {
                freeTabs.put(io, displayName);
            }
        }

        public void start() throws IOException {
            io.select();
        }

        public void setProcessOutputStream(InputStream inputStream) throws IOException {
            Output out = new Output(inputStream, io.getOut());
            outTask = PROCESSOR.post(out);
        }

        public void setProcessErrorStream(InputStream inputStream) throws IOException {
            Output err = new Output(inputStream, io.getErr());
            errTask = PROCESSOR.post(err);
        }

        public void setProcessInputStream(OutputStream outputStream) throws IOException {
            input = new Input(io.getIn(), outputStream);
            inTask = PROCESSOR.post(input);
        }
        
    }
    
    private static class Output implements Runnable {
        private InputStreamReader str;
        private OutputWriter writer;
        public Output(InputStream instream, OutputWriter out) {
            str = new InputStreamReader(instream);
            writer = out;
        }
        
        public void run() {
            try {
                long stamp = System.currentTimeMillis();
                int chr = str.read();
                StringBuffer buf = new StringBuffer();
                while (chr != -1) {
                    if (chr == (int)'\n') {
                        if (buf.length() > 0 && buf.charAt(buf.length() - 1) == '\r') {
                            // should fix issues on windows..
                            buf.setLength(buf.length() - 1);
                        }
                        writer.println(buf.toString());
                        buf.setLength(0);
                        stamp = System.currentTimeMillis();
                    } else {
                        buf.append((char)chr);
                    }
                    while (true) {
                        if (str.ready()) {
                            chr = str.read();
                            break;
                        } else {
                            if (System.currentTimeMillis() - stamp > 700) {
                                writer.print(buf.toString());
                                buf.setLength(0);
                                chr = str.read();
                                stamp = System.currentTimeMillis();
                                break;
                            }
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    str.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                closeWriter();
            }
        }
        
        public void closeWriter() {
            writer.close();
        }
    }
    
    private static class Input implements Runnable {
        private Reader ioReader;
        private BufferedReader str;
        private PrintWriter writer;
        public Input(Reader instream, OutputStream out) {
            ioReader = instream;
            str = new BufferedReader(instream);
            writer = new PrintWriter(new OutputStreamWriter(out));
        }
        
        public void closeReader() {
                try {
                    //somehow the original reader needs to be closed first..
                    ioReader.close();
                    str.close();
                    writer.close();
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
        }
        
        public void run() {
            try {
                String line = str.readLine();
                while (line != null) {
                    if (!writer.checkError()) {
                        writer.println(line);
                        writer.flush();
                    } else {
                        break;
                    }
                    line = str.readLine();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                writer.close();
            }
        }
        
    }
    
}

