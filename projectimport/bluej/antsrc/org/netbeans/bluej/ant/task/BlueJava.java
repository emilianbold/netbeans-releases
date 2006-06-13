/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    
    private static class NbOutputStreamHandler implements ExecuteStreamHandler {
        private InputOutput io;
        private RequestProcessor.Task outTask;
        private RequestProcessor.Task errTask;
        private RequestProcessor.Task inTask;
        private Input input;
        public NbOutputStreamHandler(String name) {
            io = IOProvider.getDefault().getIO("Run " + name, true);
            
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
                int chr = str.read();
                while (chr != -1) {
                    if (chr == (int)'\n') {
                        writer.println();
                    } else {
                        writer.write(chr);
                    }
                    chr = str.read();
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

