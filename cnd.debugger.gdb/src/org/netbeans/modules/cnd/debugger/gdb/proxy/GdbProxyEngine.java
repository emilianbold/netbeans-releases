/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.debugger.gdb.proxy;


// Imported classes for ShellCommand() class
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.api.util.Path;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.Signal;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Class GdbProxyEngine implements the communication with gdb (low level)
 * Main functions:
 *    start the debugger
 *    send a message (ascii text) to the debugger (via stdin)
 *    read the debugger's stdout and report this event to the upper level
 *    send a signal to the application to interrupt the execution
 *    kill the debugger
 */
public class GdbProxyEngine {
    
    private static final int MIN_TOKEN = 100;
    
    private PrintStream toGdb;
    private final GdbDebugger debugger;
    private final GdbProxy gdbProxy;
    private int debuggerPid = -1;

    private final MICommand[] commandList = new MICommand[20];
    private int nextCommandPos = 0;
    
    //TODO: int may not be enough here, consider using long
    private int nextToken = MIN_TOKEN;
    private int currentToken = MIN_TOKEN;
    private boolean active;
    private RequestProcessor.Task gdbReader = null;

    // This queue was created due to the issue 156138
    private final RequestProcessor sendQueue = new RequestProcessor("sendQueue"); // NOI18N
    private final boolean timerOn = Boolean.getBoolean("gdb.proxy.timer"); // NOI18N
    
    private static final Logger log = Logger.getLogger("gdb.gdbproxy.logger"); // NOI18N

    /**
     * Create a gdb process
     *
     * @param debuggerCommand - a name of an external debugger with parameters
     * @param debuggerEnvironment - environment variables and values to be set
     * @param workingDirectory - a directory where the debugger should run
     * @param stepIntoProject - a flag to stop at first source line
     */
    public GdbProxyEngine(GdbDebugger debugger, GdbProxy gdbProxy, List<String> debuggerCommand,
                    String[] debuggerEnvironment, String workingDirectory, String tty,
                    String cspath) throws IOException {

        if (tty != null) {
            debuggerCommand.add("-tty"); // NOI18N
            debuggerCommand.add(tty);
        }
        this.debugger = debugger;
        this.gdbProxy = gdbProxy;
        active = true;
        
        getLogger().logMessage("Debugger Command: " + debuggerCommand); // NOI18N
        getLogger().logMessage("Env[" + debuggerEnvironment.length + "]: " + // NOI18N
                Arrays.asList(debuggerEnvironment));
        getLogger().logMessage("workingDirectory: " + workingDirectory); // NOI18N
        getLogger().logMessage("NB version: " + System.getProperty("netbeans.buildnumber")); // NOI18N
        getLogger().logMessage("================================================"); // NOI18N

        startDebugger(debuggerCommand, workingDirectory, debuggerEnvironment, cspath);
    }
    
    private void startDebugger(List<String> debuggerCommand,
                               String workingDirectory,
                               String[] debuggerEnvironment,
                               String cspath) throws IOException {
        ExecutionEnvironment execEnv = debugger.getHostExecutionEnvironment();
        String[] args = debuggerCommand.subList(1, debuggerCommand.size()).toArray(new String[debuggerCommand.size()-1]);
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(execEnv);
        npb.setExecutable(debuggerCommand.get(0)).setArguments(args);

        npb.setWorkingDirectory(workingDirectory);

        final MacroMap environment = npb.getEnvironment();

        if (debuggerEnvironment != null) {
            environment.putAll(debuggerEnvironment);
        }

        if (execEnv.isLocal()) {
            String pathname = Path.getPathName();
            environment.appendPathVariable(pathname, cspath);
        }

        final NativeProcess proc = npb.call();
        debuggerPid = proc.getPID();
        // for remote execution we need to convert encoding
        toGdb = toGdbWriter(proc.getInputStream(), proc.getOutputStream(), execEnv.isRemote());

        new RequestProcessor("GdbReaperThread").post(new Runnable() { // NOI18N
            public void run() {
                try {
                    int rc = proc.waitFor();
                    if (rc == 0) {
                        debugger.finish(false);
                    } else {
                        unexpectedGdbExit(rc);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    // Interrupts the debugger
    public void interrupt() {
        CommonTasksSupport.sendSignal(
                debugger.getHostExecutionEnvironment(),
                debuggerPid,
                Signal.SIGINT,
                null);
    }

    private void unexpectedGdbExit(int rc) {
        String msg;

        if (rc < 0) {
            msg = NbBundle.getMessage(GdbDebugger.class, "ERR_UnexpectedGdbExit");  // NOI18N
        } else {
            msg = NbBundle.getMessage(GdbDebugger.class, "ERR_UnexpectedGdbExitRC", rc);  // NOI18N
        }

        NotifyDescriptor nd = new NotifyDescriptor(msg,
                NbBundle.getMessage(GdbDebugger.class, "TITLE_UnexpectedGdbFailure"), // NOI18N
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.ERROR_MESSAGE,
                new Object[] { NotifyDescriptor.OK_OPTION },
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(nd);
        debugger.finish(false);
    }

    private static PrintStream getPrintStream(final OutputStream os, boolean remote) {
        if (remote) {
            // set charset
            try {
                return new PrintStream(os, true, ProcessUtils.getRemoteCharSet());
            } catch (UnsupportedEncodingException ex) {
                // this is possible situation
            }
        }
        return new PrintStream(os, true);
    }

    private PrintStream toGdbWriter(InputStream is, OutputStream os, boolean remote) {
        PrintStream togdb = getPrintStream(os, remote);
        final BufferedReader fromGdb = ProcessUtils.getReader(is, remote);

        gdbReader = new RequestProcessor("GdbReaderRP").post(new Runnable() { // NOI18N
            public void run() {
                String line;

                try {
                    while ((line = fromGdb.readLine()) != null) {
                        line = line.trim();
                        if (line.length() > 0) {
                            try {
                                processMessage(line);
                            } catch (Exception e) {
                                log.log(Level.SEVERE, "Exception in processMessage", e); // NOI18N
                            }
                        }
                    }
                } catch (IOException ioe) {
                    log.log(Level.WARNING, "IOException in gdbReader", ioe); // NOI18N
                }
            }
        });
        return togdb;
    }
    
    public void finish() {
        if (gdbReader != null) {
            gdbReader.cancel();
        }
    }
    
    private synchronized int nextToken() {
        return nextToken++;
    }
    
    /**
     * Send a command to the debugger
     *
     * @param cmd - a command to be sent to the debugger
     */
    int sendCommand(String cmd) {
        MICommand command = createMICommand(cmd);
        sendCommand(command);
        return command.getToken();
    }

    private void sendCommand(int token, String cmd) {
        sendCommand(new MICommandImpl(token, cmd));
    }


    private void sendCommand(final MICommand command) {
        if (active) {
            sendQueue.post(new Runnable() {
                public void run() {
                    if (command.getText().charAt(0) != '-') {
                        addCommand(command);
                    }
                    String fullcmd = Integer.toString(command.getToken()) + command.getText();
                    gdbProxy.getLogger().logMessage(CommandBuffer.addTimePrefix(timerOn, fullcmd));
                    toGdb.println(fullcmd);
                }
            });
        }
    }

    CommandBuffer sendCommandEx(String cmd) {
        return sendCommandEx(cmd, true);
    }

    CommandBuffer sendCommandEx(String cmd, boolean waitForCompletion) {
        int token = nextToken();
        CommandBuffer cb = new CommandBuffer(gdbProxy, token);
        gdbProxy.putCB(token, cb);
        sendCommand(token, cmd);
        if (waitForCompletion) {
            cb.waitForCompletion();
        }
        return cb;
    }
    
    void sendConsoleCommand(String cmd) {
        int token = nextToken() + 10000;
        sendCommand(token, cmd);
    }
    
    void stopSending() {
        active = false;
    }

    private void addCommand(MICommand command) {
        synchronized (commandList) {
            commandList[nextCommandPos] = command;
            if (++nextCommandPos >= commandList.length) {
                nextCommandPos = 0;
            }
        }
    }
    
    /**
     * Process the first complete reply from the queue
     *
     * @return null if the reply is not recognized, otherwise return reply
     */
    private void processMessage(String msg) {
        if (msg.equals("(gdb)")) { // NOI18N
            return; // skip prompts
        }
        int token = getToken(msg);
        if (token < 0) {
            token = getCurrentToken(msg);
            if (token != -1) {
                gdbProxy.getLogger().logMessage(CommandBuffer.addTimePrefix(timerOn, token) + msg);
            } else {
                gdbProxy.getLogger().logMessage(CommandBuffer.addTimePrefix(timerOn, msg));
            }
        } else {
            gdbProxy.getLogger().logMessage(CommandBuffer.addTimePrefix(timerOn,msg));
        }
        msg = stripToken(msg);

        // bugfix for IZ:142454
        // ('-enable-timings no' does not turn it off sometimes)
        msg = stripTiming(msg);

        if (msg.length() == 0) {
            log.warning("Empty message received from gdb");
            return;
        }

        switch (msg.charAt(0)) {
            case '^': // result-record
                if (token == currentToken && msg.equals(GdbDebugger.DONE_PREFIX)) { // NOI18N
                    currentToken = -1;
                }
                debugger.resultRecord(token, msg);
                break;
                
            case '*': // exec-async-output
                debugger.execAsyncOutput(token, msg);
                break;
                
            case '+': // status-async-output
                debugger.statusAsyncOutput(token, msg);
                break;
                
            case '=': // notify-async-output
                debugger.notifyAsyncOutput(token, msg);
                break;
                
            case '~': // console-stream-output
                debugger.consoleStreamOutput(token, msg.substring(2, msg.length() - 1));
                break;
                
            case '@': // target-stream-output
                debugger.targetStreamOutput(msg);
                break;
                
            case '&': // log-stream-output
                debugger.logStreamOutput(msg);
                break;
                
            default:
                debugger.output(msg);
        }
    }
    
    private int getCurrentToken(String msg) {
        if (msg.charAt(0) == '&') {
            msg = msg.substring(2, msg.length() - 1).replace("\\n", ""); // NOI18N
            synchronized (commandList) {
                for (int i = nextCommandPos-1;;i--) {
                    if (i < 0) {
                        i = commandList.length-1;
                    }
                    if (i == nextCommandPos) {
                        break;
                    }
                    MICommand command = commandList[i];
                    if (command != null && command.getText().equals(msg)) {
                        commandList[i] = null;
                        currentToken = command.getToken();
                        break;
                    }
                }
            }
        }
        return currentToken;
    }

    /**
     * Returns the position of the first non-digit symbol
     */
    private static int getFirstNonDigit(String msg) {
        for (int i = 0; i < msg.length(); i++) {
            if (!Character.isDigit(msg.charAt(i))) {
                return i;
            }
        }
        return 0;
    }
    
    /**
     * Strip the token from the start of a command line and return the token
     *
     * @param msg The line which may or may not start with a token
     * @return token The token or -1
     */
    private static int getToken(String msg) {
        int i = getFirstNonDigit(msg);
        if (i > 0) {
            return Integer.parseInt(msg.substring(0, i));
        } else {
            return -1;
        }
    }
    
    /**
     * Strip the token from the start of a command line
     *
     * @param msg The line which may or may not start with a token
     * @return msg The message without a leading integer token
     */
    private static String stripToken(String msg) {
        int i = getFirstNonDigit(msg);
        char ch = (i < msg.length()) ? msg.charAt(i) : 0;
        if ((ch == '^' || ch == '*' || ch == '+' || ch == '=') && ch != 0) {
            return msg.substring(i);
        } else {
            return msg;
        }
    }

    private static final String TIME_PREFIX = ",time="; // NOI18N
    /**
     * Cut timing information if any
     * @param msg
     */
    private String stripTiming(String msg) {
        int pos = msg.indexOf(TIME_PREFIX);
        if (pos != -1) {
            // time= prefix may appear not only in the end of the message, see issue 147938
            int endPos = GdbUtils.findMatchingCurly(msg, pos + TIME_PREFIX.length());
            if (endPos != -1) {
                return msg.substring(0, pos) + msg.substring(endPos+1);
            } else {
                log.warning("Matching curly not found in timing info: " + msg); // NOI18N
            }
        }
        return msg;
    }
   
    private GdbLogger getLogger() {
        return gdbProxy.getLogger();
    }

    public MICommand createMICommand(String cmd) {
        return new MICommandImpl(nextToken(), cmd);
    }
    
    private class MICommandImpl implements MICommand {
        private final int token;
        private final String cmd;
        private boolean sent = false;
        
        public MICommandImpl(int token, String cmd) {
            this.token = token;
            this.cmd = cmd;
        }
        
        public String getText() {
            return cmd;
        }
        
        public int getToken() {
            return token;
        }

        public synchronized void send() {
            assert !sent : "sending command " + this + " twice"; // NOI18N
            sendCommand(this);
            sent = true;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof MICommand) {
                return getToken() == ((MICommand) o).getToken();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getToken();
        }

        @Override
        public String toString() {
            return token + cmd;
        }
    }
    
} /* End of GdbProxyEngine */
