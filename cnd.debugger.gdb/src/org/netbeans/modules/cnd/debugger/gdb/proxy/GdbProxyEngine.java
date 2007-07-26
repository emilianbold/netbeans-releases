/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.proxy;


// Imported classes for ShellCommand() class
import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebuggerImpl;
import org.netbeans.modules.cnd.settings.CppSettings;
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
    private GdbDebuggerImpl debugger;
    private GdbProxy gdbProxy;
    private int nextToken = MIN_TOKEN;
    
    private Logger log = Logger.getLogger("gdb.gdbproxy.logger"); // NOI18N
    
    /**
     * Create a gdb process
     *
     * @param debuggerCommand - a name of an external debugger with parameters
     * @param debuggerEnvironment - environment variables and values to be set
     * @param workingDirectory - a directory where the debugger should run
     * @param stepIntoProject - a flag to stop at first source line
     */
    public GdbProxyEngine(GdbDebuggerImpl debugger, GdbProxy gdbProxy, List debuggerCommand,
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
        env.put("PATH", CppSettings.getDefault().getPath()); // NOI18N
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
        final GdbDebuggerImpl gdi = debugger;
        new RequestProcessor("GdbReaperThread").post(new Runnable() { // NOI18N
            public void run() {
                int rc;
                try {
                    rc = waitProc.waitFor();
                    if (rc == 0) {
                        gdi.finish();
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
        int token = nextToken();
        StringBuilder fullcmd = new StringBuilder(String.valueOf(token));
        fullcmd.append(cmd);
        fullcmd.append('\n');
        gdbProxy.getLogger().logMessage(fullcmd.toString());
        toGdb.print(fullcmd.toString());
        return token;
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
        String reply = null;
        String s1, s2;
        int id = 0;
        int i = 0;
        int token = getToken(msg);
        if (token < 0) {
            token = debugger.getCurrentToken();
            gdbProxy.getLogger().logMessage(token + msg);
        } else {
            gdbProxy.getLogger().logMessage(msg);
        }
        msg = stripToken(msg);
        
        switch (msg.charAt(0)) {
            case '^': // result-record
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
        }
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
        int count = 0;
        int i;
        
        for (i = 0; i < msg.length(); i++) {
            if (!Character.isDigit(msg.charAt(i))) {
                break;
            }
        }
        return msg.substring(i);
    }
    
    private GdbLogger getLogger() {
        return gdbProxy.getLogger();
    }
    
    /**
     * Execute external command
     */
    public String executeExternalCommand(String cmd, String dir, int timeout) {
        String reply=null;
        int time=0;
        // String OSName = System.getProperty("os.name"); // NOI18N
        // String OSArch = System.getProperty("os.arch"); // NOI18N
        // Supported combinations:
        //  SunOS x86
        //  SunOS sparc
        //  Linux i386
        //  Windows x86
        // Create external process
        ShellCommand scm = new ShellCommand();
        try {
            scm.run(dir, cmd);
        } catch (Exception e) {
            return(reply);
        }
        if (0 == timeout) return null; // Don't wait at all.'
        Thread t = new Thread();
        while (scm.isRunning() == true) {
            try {
                t.sleep(100);
                if ((time+=100) > timeout) {
                    // Timeout. No need to wait more.
                    break;
                }
            } catch (InterruptedException tie100) {
                // sleep 100 milliseconds
            }
        }        
        if (scm.isRunning() == false) {
            try {
                reply=scm.readOutput(false);
                String r = scm.readOutput(false);
                while (r.length() > 0) {
                    reply += r;
                    r = scm.readOutput(false);
                }
                //log.severe("DEBUG: executeExternalCommand() reply="+reply); // DEBUG
            } catch (Exception e) {
                //reply=null; // Ignore exception
            }
            //if(scm.exitValue() != 0) {
                //log.severe("DEBUG: executeExternalCommand() cmd="+cmd+" exitValue()="+scm.exitValue()); // DEBUG
            //}
        }
        return reply;
    }

    /**
     * Execute external command without shell
     */
    public String executeExternalCommandWithoutShell(String[] cmda, String[] env, String dir, int timeout) {
        String reply=null;
        int time=0;
        // String OSName = System.getProperty("os.name"); // NOI18N
        // String OSArch = System.getProperty("os.arch"); // NOI18N
        // Supported combinations:
        //  SunOS x86
        //  SunOS sparc
        //  Linux i386
        //  Windows x86
        // Create external process
        ShellCommand scm = new ShellCommand();
        try {
            scm.run(cmda, env, dir);
        } catch (Exception e) {
            return(reply);
        }
        if (0 == timeout) return null; // Don't wait at all.
        Thread t = new Thread();
        while (scm.isRunning() == true) {
            try {
                t.sleep(100);
                if ((time+=100) > timeout) {
                    // Timeout. No need to wait more.
                    break;
                }
            } catch (InterruptedException tie100) {
                // sleep 100 milliseconds
            }
        }        
        if (scm.isRunning() == false) {
            try {
                reply=scm.readOutput(false);
                String r = scm.readOutput(false);
                while (r.length() > 0) {
                    reply += r;
                    r = scm.readOutput(false);
                }
            } catch (Exception e) {
                //reply=null; // Ignore exception
            }
            //if(scm.exitValue() != 0) {
                //log.severe("DEBUG: executeExternalCommand() cmd="+cmd+" exitValue()="+scm.exitValue()); // DEBUG
            //}
        }
        return reply;
    }

    /**
     * Class ShellCommand executes an external program and supports input/output functions
     *
     * This class will be replaced.
     *
     * Main functions:
     *    run a program
     *    write to stdin
     *    read from stdout
     *    read from stderr
     *    return exit status
     */
    class ShellCommand {

        private BufferedReader	processOutput;
        private BufferedReader	processError;
        private PrintStream         processInput;
        private Runtime		rt;
        protected boolean           interrupted;
        protected Process           thisProcess;
        protected String            shArgs;
        protected String            errors;
        protected String            output;

        /**
         * Creates a new instance of ShellCommand
         */
        public ShellCommand() {
            processOutput = null;
            processError = null;
            thisProcess = null;
            rt = Runtime.getRuntime();
            interrupted = false;
            shArgs = null;
            errors = ""; // NOI18N
            output = ""; // NOI18N
        }

        /**
         * Set the arguments
         *
         * @param args - arguments
         */
        public void setShellArgs(String args) {
            shArgs = new String(args);
        }

        /**
         * Interrupt and destroy the process
         */
        public void interrupt() {
            interrupted = true;
            thisProcess.destroy();
            thisProcess = null;
        }

        /**
         * Return exit status
         */
        public int exitValue() {
            if(interrupted) {
                return(-1);
            }
            try {
                thisProcess.waitFor();
            } catch (Exception e) {
                return(-2);
            }
            return(thisProcess.exitValue());
        }

        /**
         * Check if the process is running
         */
        public boolean isRunning() {
            try {
                int ev = thisProcess.exitValue();
            } catch (IllegalThreadStateException ie) {
                return(true);
            } catch (Exception ie) {
                return(false);
            }
            return(false);
        }

        /**
         * Read from a stream
         *
         * @param reader - a stream
         */
        private String readStream(BufferedReader reader) throws Exception {
            String ret = null;
            try {
                if(!reader.ready()) {
                    int ev = thisProcess.exitValue();
                    ret = reader.readLine();
                    if(ret != null) {
                        ret += "\n"; // NOI18N
                    }
                    return(ret);
                }
                ret = reader.readLine();
                if(ret != null) {
                    ret += "\n"; // NOI18N
                }
            } catch (IllegalThreadStateException ie) {
                return(""); // NOI18N
            }
            return(ret);
        }

        /**
         * Read from standard output stream
         *
         * @param shouldWait - a flag (if true, wait while read is done)
         */
        public String readOutput(boolean shouldWait) throws Exception {
            String ret;
            if(shouldWait) {
                ret = processOutput.readLine();
            } else {
                ret = readStream(processOutput);
            }
            if(ret != null) {
                output += ret;
            }
            return(ret);
        }

        /**
         * Read from standard error stream
         *
         * @param shouldWait - a flag (if true, wait while read is done)
         */
        public String readError(boolean shouldWait) throws Exception {
            String ret;
            if(shouldWait) {
                ret = processError.readLine();
            } else {
                ret = readStream(processError);
            }
            return(ret);
        }

        /**
         * Write to standard input stream
         *
         * @param str - a message
         */
        public void writeInput(String str) throws Exception {
            processInput.print(str);
            processInput.flush();
    //    
        }

        /**
         * Run a program
         *
         * @param dirname - working directory
         * @param cmnd - command line
         */
        public void run(String dirname, String cmnd) throws Exception {
            cmnd = "cd " + dirname + "; " + cmnd; // NOI18N
            run(cmnd);
        }

        /**
         * Run a program
         *
         * @param cmnd - command line
         */
        public void run(String cmnd) throws Exception {
            String [] ss = new String[3];
            ss[0] = "sh"; //NM ss[0] = "/bin/sh"; // NOI18N
            if(shArgs == null) {
                ss[1] = new String("-ec"); // NOI18N
            } else {
                ss[1] = new String(shArgs);
            }
            ss[2] = new String(cmnd + " 2>&1"); // NOI18N

            interrupted = false;

            try {
                // log.severe("DEBUG: rt.exec() "+cmnd); // DEBUG (perf test)
                thisProcess = rt.exec(ss);
                InputStream os = thisProcess.getInputStream();
                InputStream os_err = thisProcess.getErrorStream();
                OutputStream is = thisProcess.getOutputStream();
                processOutput = new BufferedReader(
                        new InputStreamReader(os)
                        );
                processError = new BufferedReader(
                        new InputStreamReader(os_err)
                        );
                processInput = new PrintStream(is, true);
            } catch (Exception ee) {
                String msg = "Command \"" + cmnd + "\" failed:\n" + ee.toString(); // NOI18N
                throw new Exception(msg);
            }
            return;
        }


        /**
         * Run a program
         *
         * @param cmdarray - name of an external program with parameters
         * @param envp - environment variables and values to be set
         * @param directory - directory where the program should run
         */
        public void run(String[] cmdarray,
                String[] envp,
                String directory
                ) throws Exception {

            interrupted = false;

            try {
                //String s="  cmd="; // DEBUG
                //for (int i=0; i<cmdarray.length; i++) s+=cmdarray[i]+" "; // DEBUG
                //s+="  envp="; // DEBUG
                //for (int i=0; i<envp.length; i++) s+=envp[i]+" "; // DEBUG
                //s+="  directory="+directory; // DEBUG
                //log.severe("DEBUG: run() "+s); // DEBUG
                File dir = new File(directory);
                thisProcess = rt.exec(cmdarray, envp, dir);
                InputStream os = thisProcess.getInputStream();
                InputStream os_err = thisProcess.getErrorStream();
                OutputStream is = thisProcess.getOutputStream();
                processOutput = new BufferedReader(
                        new InputStreamReader(os)
                        );
                processError = new BufferedReader(
                        new InputStreamReader(os_err)
                        );
                processInput = new PrintStream(is, true);
            } catch (Exception ee) {
                String cmnd = cmdarray[0];
                for (int i=1; i < cmdarray.length; i++) {
                    cmnd += cmdarray[i];
                }
                String msg = "Command \"" + cmnd + "\" failed:\n" + ee.toString(); // NOI18N
                throw new Exception(msg);
            }
            return;
        }

    } /* End of class ShellCommand */

  
} /* End of GdbProxyThread */
