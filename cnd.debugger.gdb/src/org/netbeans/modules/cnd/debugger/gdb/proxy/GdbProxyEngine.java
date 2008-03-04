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
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.utils.CommandBuffer;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

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
    private GdbDebugger debugger;
    private GdbProxy gdbProxy;
    private LinkedList<CommandInfo> tokenList;
    private int nextToken = MIN_TOKEN;
    private int currentToken = MIN_TOKEN;
    private boolean active;
    private boolean timerOn = Boolean.getBoolean("gdb.proxy.timer"); // NOI18N
    
    private Logger log = Logger.getLogger("gdb.gdbproxy.logger"); // NOI18N
    
    /**
     * Create a gdb process
     *
     * @param debuggerCommand - a name of an external debugger with parameters
     * @param debuggerEnvironment - environment variables and values to be set
     * @param workingDirectory - a directory where the debugger should run
     * @param stepIntoProject - a flag to stop at first source line
     */
    public GdbProxyEngine(GdbDebugger debugger, GdbProxy gdbProxy, List debuggerCommand,
                    String[] debuggerEnvironment, String workingDirectory, String termpath) throws IOException {
        
        if (Utilities.isUnix() && termpath != null) {
            ExternalTerminal eterm = new ExternalTerminal(debugger, termpath, debuggerEnvironment);
            String tty = eterm.getTty();
            if (tty != null) {
                debuggerCommand.add("-tty"); // NOI18N
                debuggerCommand.add(tty);
            }
        }
        this.debugger = debugger;
        this.gdbProxy = gdbProxy;
        tokenList = new LinkedList<CommandInfo>();
        active = true;
        ProcessBuilder pb = new ProcessBuilder(debuggerCommand);
        
        getLogger().logMessage("Debugger Command: " + debuggerCommand); // NOI18N
        getLogger().logMessage("Env[" + debuggerEnvironment.length + "]: " + // NOI18N
                Arrays.asList(debuggerEnvironment));
        getLogger().logMessage("workingDirectory: " + workingDirectory); // NOI18N
        getLogger().logMessage("================================================"); // NOI18N
        
        Map<String, String> env = pb.environment();
        Process proc = null;
        
        for (String var : debuggerEnvironment) {
            String key, value;
            int idx = var.indexOf('=');
            if (idx != -1) {
                key = var.substring(0, idx);
                value = var.substring(idx + 1);
                env.put(key, value);
            }
        }
        env.put("PATH", Path.getPathAsString()); // NOI18N
        pb.directory(new File(workingDirectory));
        pb.redirectErrorStream(true);
        
        proc = pb.start(); // Let IOException be handled in GdbdebuggerImpl.startDebugging()...
        
        final BufferedReader fromGdb = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        new RequestProcessor("GdbReaderRP").post(new Runnable() { // NOI18N
            public void run() {
                String line;
                
                try {
                    while ((line = fromGdb.readLine()) != null) {
                        line = line.trim();
                        if (line.length() > 0) {
                            processMessage(line);
                        }
                    }
                } catch (IOException ioe) {
                }
            }
        });
        toGdb = new PrintStream(proc.getOutputStream(), true);
        
        final Process waitProc = proc;
        final GdbDebugger gdi = debugger;
        new RequestProcessor("GdbReaperThread").post(new Runnable() { // NOI18N
            public void run() {
                int rc;
                try {
                    rc = waitProc.waitFor();
                    if (rc == 0) {
                        gdi.finish(false);
                    } else {
                        gdi.unexpectedGdbExit(rc);
                    }
                    return;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    
    private int nextToken() {
        return nextToken++;
    }
    
    /**
     * Send a command to the debugger
     *
     * @param cmd - a command to be sent to the debugger
     */
    int sendCommand(String cmd) {
        return sendCommand(null, cmd, false);
    }
    
    int sendCommand(CommandBuffer cb, String cmd) {
        return sendCommand(cb, cmd, false);
    }
    
    int sendCommand(CommandBuffer cb, String cmd, boolean consoleCommand) {
        if (active) {
            String time;
            if (timerOn) {
                time = Long.toString(System.currentTimeMillis()) + ':';
            } else {
                time = "";
            }
            int token = nextToken();
            if (cb != null) {
                cb.setID(token);
            }
            if (consoleCommand) {
                token += 10000;
            } else if (cmd.charAt(0) != '-') {
                tokenList.add(new CommandInfo(token, cmd));
            }
            StringBuilder fullcmd = new StringBuilder(String.valueOf(token));
            fullcmd.append(cmd);
            fullcmd.append('\n');
            gdbProxy.getLogger().logMessage(time + fullcmd.toString());
            toGdb.print(fullcmd.toString());
            return token;
        } else {
            return -1;
        }
    }
    
    int sendConsoleCommand(String cmd) {
        return sendCommand(null, cmd, true);
    }
    
    void stopSending() {
        active = false;
    }
    
    /**
     * Process the first complete reply from the queue
     *
     * @return null if the reply is not recognized, otherwise return reply
     */
    private void processMessage(String msg) {
        String time;
        if (timerOn) {
            time = Long.toString(System.currentTimeMillis()) + ':';
        } else {
            time = "";
        }
        if (msg.equals("(gdb)")) { // NOI18N
            return; // skip prompts
        }
        int token = getToken(msg);
        if (token < 0) {
            token = getCurrentToken(msg);
            if (token != -1) {
                gdbProxy.getLogger().logMessage(time + token + msg);
            } else {
                gdbProxy.getLogger().logMessage(time + msg);
            }
        } else {
            gdbProxy.getLogger().logMessage(time + msg);
        }
        msg = stripToken(msg);
        if (msg.length() == 0) {
            log.warning("Empty message received from gdb");
            return;
        }
        
        switch (msg.charAt(0)) {
            case '^': // result-record
                if (token == currentToken && msg.equals("^done")) { // NOI18N
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
        char ch1 = msg.charAt(0);
        if (ch1 == '&') {
            CommandInfo ci = getCommandInfo(msg);
            if (ci != null) {
                tokenList.remove(ci);
                currentToken = ci.getToken();
            }
        }
        return currentToken;
    }
    
    private CommandInfo getCommandInfo(String msg) {
        msg = msg.substring(2, msg.length() - 1).replace("\\n", ""); // NOI18N
        
        for (CommandInfo ci : tokenList) {
            if (ci.getCommand().equals(msg)) {
                return ci;
            }
        }
        return null;
    }
    
    /**
     * Strip the token from the start of a command line and return the token
     *
     * @param msg The line which may or may not start with a token
     * @return token The token or -1
     */
    private int getToken(String msg) {
        int i;
        
        for (i = 0; i < msg.length(); i++) {
            if (!Character.isDigit(msg.charAt(i))) {
                break;
            }
        }
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
    private String stripToken(String msg) {
        int i;
        
        for (i = 0; i < msg.length(); i++) {
            if (!Character.isDigit(msg.charAt(i))) {
                break;
            }
        }
        char ch = i < msg.length() ? msg.charAt(i) : 0;
        if ((ch == '^' || ch == '*' || ch == '+' || ch == '=') && ch != 0) {
            return msg.substring(i);
        } else {
            return msg;
        }
    }
    
    private GdbLogger getLogger() {
        return gdbProxy.getLogger();
    }
    
    private static class CommandInfo {
        
        private int token;
        private String cmd;
        
        public CommandInfo(int token, String cmd) {
            this.token = token;
            this.cmd = cmd;
        }
        
        private String getCommand() {
            return cmd;
        }
        
        public int getToken() {
            return token;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof CommandInfo) {
                CommandInfo ci = (CommandInfo) o;
                return cmd.equals(ci.getCommand());
            } else if (o instanceof String) {
                return cmd.equals(o.toString());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return token;
        }
    }
    
} /* End of GdbProxyEngine */
