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

/*
 * GdbProxy.java
 *
 * Note: For a description of the current state of quoting and related topics, see
 * http://sourceware.org/ml/gdb/2006-02/msg00283.html.
 *
 * @author Nik Molchanov and Gordon Prieur
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Utilities;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebuggerImpl;
import org.netbeans.modules.cnd.debugger.gdb.timer.GdbTimer;

/**
 * Class GdbProxy is a Controller component of gdb driver
 * The main part of the knowledge about the debugger UI is concentrated in this class.
 * It knows the differences between the debuggers, and will eliminate these differences
 * as much as possible, presenting data to the View layer.
 * Main functions:
 *    tell low level to start the debugger
 *    translate commands from CLI to MI
 *    tell low level to send a command to the debugger
 *    receive the debugger's output from low level and parse the MI messages
 *    call the "callback" methods (upper level)
 *    tell low level to send a signal to the application to interrupt the execution
 *    tell low level to kill the debugger
 */
public class GdbProxy implements GdbMiDefinitions {
    
    private GdbDebuggerImpl debugger;
    private GdbProxyEngine engine;
    private GdbLogger gdbLogger;
    
//    private String      shortProgramName;
    private String      signalHandlerBreakpoint;
    private String      externalTerminal;
    private String      externalTerminalPID;
    private Vector      evaluatedExpressions;
    private Vector      evaluatedVariables;
    
    // SHELL commands
    private final String SH_CMD_KILL      = "kill -9 "; // NOI18N
    private final String DIR_TMP          = "/tmp"; // NOI18N
    
    // String constants
    private final String PARAM_SEPARATOR  = " "; // NOI18N - FIXME
    private final String EMPTY_STRING     = ""; // NOI18N - FIXME
    private final String VAR_STRING       = "var"; // NOI18N - FIXME
    
    // Log file
    private Logger log = Logger.getLogger("gdb.gdbproxy.logger"); // NOI18N

    /**
     * Creates a new instance of GdbProxy
     *
     * @param debugger The GdbDebuggerImpl
     * @param debuggerCommand The gdb command to use
     * @param debuggerEnvironment The overrides to the user's environment
     * @param workingDirectory The directory to start the debugger from
     * @throws IOException Pass this on to the caller
     */
    public GdbProxy(GdbDebuggerImpl debugger, String debuggerCommand,
            String[] debuggerEnvironment, String workingDirectory) throws IOException {
        this.debugger = debugger;
        
        evaluatedExpressions = new Vector();
        evaluatedVariables = new Vector();
        externalTerminalPID = null;
        log.setLevel(Level.FINE);
        
        ArrayList dc = new ArrayList();
        dc.add(debuggerCommand);
        dc.add("--nw");  // NOI18N
        dc.add("--silent");  // NOI18N
        dc.add("--interpreter=mi"); // NOI18N
        
        gdbLogger = new GdbLogger(debugger, this);
        engine = new GdbProxyEngine(debugger, this, dc, debuggerEnvironment, workingDirectory);
    }
    
    protected GdbProxyEngine getProxyEngine() {
        return engine;
    }
    
    public GdbLogger getLogger() {
        return gdbLogger;
    }
    
    /**
     * Load the program
     *
     * @param program - a name of an external program to debug
     */
    public int file_exec_and_symbols(String programName) {
        return engine.sendCommand(MI_CMD_FILE_EXEC_AND_SYMBOLS + programName.toString());
    }
    
    /** Ask gdb for its version */
    public int gdb_version() {
        return engine.sendCommand("-gdb-version"); // NOI18N
    }
    
    /**
     * Set the runtime directory. Note that this method may get called before we have
     * gdb's version. Thats why we check that its greater than 6.3. This way, if we
     * don't have the version we fallback to the non-mi command.
     *
     * @param path The directory we want to run from
     */
    public int environment_cd(String dir) {
        double ver = debugger.getGdbVersion();
        if (ver > 6.3) {
            return engine.sendCommand("-environment-cd " + dir); // NOI18N
        } else {
            return engine.sendCommand("directory " + dir); // NOI18N
        }
    }
    
    /**
     *  Do a "set environment" gdb command.
     *
     *  @param var Variable of the form "foo=value"
     */
    public int gdb_set_environment(String var) {
        return engine.sendCommand(MI_CMD_GDB_SET_ENVIRONMENT + var);
    }
    
    /**
     *  Ask gdb about threads. We don't really care about the threads, but it also returns
     *  the process ID, which we do care about.
     *
     *  Note: In gdb 6.5.50 the -threads-list-all-threads command isn't implemented so we
     *  revert to the gdb command "info threads".
     */
    public int info_threads() {
        return engine.sendCommand(MI_CMD_INFO_THREADS);
    }
    
    /**
     *  Ask gdb about /proc info. We don't really care about the /proc, but it also returns
     *  the process ID, which we do care about.
     */
    public int info_proc() {
        return engine.sendCommand(MI_CMD_INFO_PROC);
    }
    
    /**
     *  Use this to call _CndSigInit() to initialize signals in Cygwin processes.
     */
    public int data_evaluate_expression(String string) {
        return engine.sendCommand(MI_CMD_DATA_EVALUATE_EXPRESSION + string);
    }
    
    /**
     * Send "-file-list-exec-source-file" to the debugger
     * This command lists the line number, the current source file,
     * and the absolute path to the current source file for the current executable.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public int file_list_exec_source_file() {
        return engine.sendCommand(MI_CMD_FILE_LIST_EXEC_SOURCE_FILE);
    }
    
    /**
     * Send "-exec-run" with parameters to the debugger
     * This command starts execution of the inferior from the beginning.
     * The inferior executes until either a breakpoint is encountered or
     * the program exits.
     *
     * @param programParameters - command line options for the program
     */
    public int exec_run(String programParameters) {
        return engine.sendCommand(MI_CMD_EXEC_RUN + programParameters);
    }
    
    /**
     * Send "-exec-run" to the debugger
     * This command starts execution of the inferior from the beginning.
     * The inferior executes until either a breakpoint is encountered or
     * the program exits.
     */
    public int exec_run() {
        return exec_run("");
    }
    
    /**
     * Send "-exec-step" to the debugger
     * This command resumes execution of the inferior program, stopping
     * when the beginning of the next source line is reached, if the next
     * source line is not a function call.
     * If it is, stop at the first instruction of the called function.
     */
    public int exec_step() {
        return engine.sendCommand(MI_CMD_EXEC_STEP);
    }
    
    /**
     * Send "-exec-next" to the debugger (go to the next source line)
     * This command resumes execution of the inferior program, stopping
     * when the beginning of the next source line is reached.
     */
    public int exec_next() {
        return engine.sendCommand(MI_CMD_EXEC_NEXT);
    }
    
    /**
     * Send "-exec-finish" to the debugger (finish this function)
     * This command resumes execution of the inferior program until
     * the current function is exited.
     */
    public int exec_finish() {
        return engine.sendCommand(MI_CMD_EXEC_FINISH);
    }
    
    /**
     * Send "-exec-continue" to the debugger
     * This command resumes execution of the inferior program, until a
     * breakpoint is encountered, or until the inferior exits.
     */
    public int exec_continue() {
        return engine.sendCommand(MI_CMD_EXEC_CONTINUE);
    }
    
    /**
     * Interrupts execution of the inferior program.
     * This method is supposed to send "-exec-interrupt" to the debugger,
     * but this feature is not implemented in gdb yet, so it is replaced
     * with sending a signal "INT" (Unix) or signal TSTP (Windows).
     */
    public int exec_interrupt() {
        if (debugger.getState() == GdbDebugger.STATE_RUNNING ||
                debugger.getState() == GdbDebugger.STATE_SILENT_STOP) {
            if (Utilities.isWindows()) {
                debugger.kill(18);
            } else {
                debugger.kill(2);
            }
        }
        return 0;
    }
    
    /**
     * Send "-exec-abort" to the debugger
     * This command kills the inferior program.
     */
    public int exec_abort() {
        String cmd;
        
        // -exec-abort isn't implemented yet (as of gdb 6.6)
        if (debugger.getGdbVersion() > 6.6) {
            cmd = MI_CMD_EXEC_ABORT;
        } else {
            cmd = CLI_CMD_KILL;
        }
        return engine.sendCommand(cmd);
    }
    
    /**
     * Send "-break-insert function" to the debugger
     * This command inserts a regular breakpoint in all functions
     * whose names match the given name.
     *
     * @param flags One or more flags aout this breakpoint
     * @param name A function name
     * @return token number
     */
    public int break_insert(int flags, String name) {
        StringBuilder cmd = new StringBuilder(MI_CMD_BREAK_INSERT);
        
        if ((flags & GdbDebugger.GDB_TMP_BREAKPOINT) != 0) {
            cmd.append("-t "); // NOI18N
        }
        
        // Temporary fix for Windows
        if (Utilities.isWindows() && name.indexOf('/') == 0 && name.indexOf(':') == 2) {
            // Remove first slash
            name = name.substring(1);
        }
        cmd.append(name);
        return engine.sendCommand(cmd.toString());
    }
    
    /**
     * Send "-break-insert function" to the debugger
     * This command inserts a regular breakpoint in all functions
     * whose names match the given name.
     *
     * @param name The function name or linenumber information
     * @return token number
     */
    public int break_insert(String name) {
        return break_insert(0, name);
    }
    
    /**
     * Send "-break-delete number" to the debugger
     * This command deletes the breakpoints
     * whose number(s) are specified in the argument list.
     *
     * @param number - breakpoint's number
     */
    public int break_delete(int number) {
        return engine.sendCommand(MI_CMD_BREAK_DELETE + Integer.toString(number));
    }
    
    /**
     * Send "-break-enable number" to the debugger
     * This command enables the breakpoint
     * whose number is specified by the argument.
     *
     * @param number - breakpoint number
     */
    public int break_enable(int number) {
        return engine.sendCommand(MI_CMD_BREAK_ENABLE + Integer.toString(number));
    }
    
    /**
     * Send "-break-disable number" to the debugger
     * This command disables the breakpoint
     * whose number is specified by the argument.
     *
     * @param number - breakpoint number
     */
    public int break_disable(int number) {
        return engine.sendCommand(MI_CMD_BREAK_DISABLE + Integer.toString(number));
    }
    
    /** Send "-stack-list-locals" to the debugger */
    public int stack_list_locals() {
        return stack_list_locals(""); // NOI18N
    }
    
    /**
     * Send "-stack-list-locals" to the debugger
     * Display the local variable names for the selected frame.
     * If print-values is 0 or --no-values, print only the names of the variables;
     * if it is 1 or --all-values, print also their values; and if it is 2 or --simple-values,
     * print the name, type and value for simple data types and the name and type for arrays,
     * structures and unions. In this last case, a frontend can immediately display the value
     * of simple data types and create variable objects for other data types when the the user
     * wishes to explore their values in more detail.
     *
     * @param printValues defines output format
     */
    public int stack_list_locals(String printValues) {
        return engine.sendCommand(MI_CMD_STACK_LIST_LOCALS + printValues);
    }
    
    public int stack_list_arguments(int showValues, int low, int high) {
        return engine.sendCommand("-stack-list-arguments " + showValues + " " + low + " " + high); // NOI18N
    }
    
    /**
     * Send "-stack-select-frame frameNumber" to the debugger
     * This command tells gdb to change the current frame.
     * Select a different frame frameNumber on the stack.
     */
    public int stack_select_frame(int frameNumber) {
        return engine.sendCommand(MI_CMD_STACK_SELECT_FRAME + Integer.valueOf(frameNumber));
    }
    
    /**
     * Send "-stack-info-frame " to the debugger
     * This command asks gdb to provide information about current frame.
     */
    public int stack_info_frame() {
        return engine.sendCommand(MI_CMD_STACK_INFO_FRAME);
    }
    
    /** Request a stack dump from gdb */
    public int stack_list_frames() {
        return engine.sendCommand(MI_CMD_STACK_LIST_FRAMES);
    }
    
    /**
     * Request the type of a symbol. As of gdb 6.6, this is unimplemented so we send a
     * non-mi command "ptype". We should only be called when symbol is in scope.
     */
    public int symbol_type(String symbol) {
        return engine.sendCommand("ptype " + symbol);
    }
    
    /**
     * Request the type of a symbol. As of gdb 6.6, there is no gdb/mi way of doing this
     * so we send a gdb "whatis" command. This is different from -system-type in the case
     * of abstract data structures (structs and classes). Its the same for other types.
     */
    public int whatis(String symbol) {
        return engine.sendCommand("whatis " + symbol);
    }
    
    /**
     * Gets variable field.
     * Variable data contains 6 fields:
     * 0 - expression
     * 1 - gdb variable name
     * 2 - frame number
     * 3 - type
     * 4 - value
     * 5 - number fo children
     * Returns empty string if variable is not available.
     *
     * @param expression a variable name or expression
     * @param field an index in variable data
     * @return field value
     */
    // FIXME - Not a gdb/mi command
    private String getVariableField(String expression, int field) {
        String reply = ""; // NOI18N;
//        synchronized (gdbVariables) {
//            if (expression == null) return reply;
//            for (int i = gdbVariables.size() - 1; i >= 0; i--) {
//                List list = (List) gdbVariables.get(i);
//                if (list == null) continue;
//                if(expression.equals((String) list.get(0))) {
//                    reply = (String) list.get(field);
//                    break;
//                }
//            }
//        }
        return reply;
    }
    
    /**
     * Gets variable type.
     * Returns empty string if type is not available.
     *
     * @param expression a variable name or expression
     * @return variable type
     */
    // FIXME - Not a gdb/mi command
    public String getVariableType(String expression) {
        return getVariableField(expression, 3);
    }
    
//    /**
//     * Gets variable value.
//     * Returns empty string if value is not available.
//     *
//     * @param expression a variable name or expression
//     * @return variable value
//     */
//    // FIXME - Not a gdb/mi command
//    public String getVariableValue(String expression) {
//        return getVariableField(expression, 4);
//    }
    
    /**
     * Gets variable's number of children.
     * Returns empty string if number of children is not available.
     *
     * @param expression a variable name or expression
     * @return number of children as a string
     */
    // FIXME - Not a gdb/mi command
    public String getVariableNumChild(String expression) {
        return getVariableField(expression, 5);
    }
    
    /**
     * Evaluates the expression and returns its value as a string.
     *
     * @return null if value is not received, otherwise return value
     */
    private int data_evaluate_index = 0;
    
    // FIXME - Not a gdb/mi command
    public String data_evaluate(String expr) {
        String cmd;
        String name = expr;
        String reply = "";
        final String strValue = "value="; // NOI18N
        final String strQuote = "\""; // NOI18N
        int i;
        if (name == null || name.length() == 0) {
            throw new IllegalStateException("Internal error: expression is not specified");
        }
        if (name.indexOf(' ') >= 0) {
            if (name.indexOf('\"') < 0) {
                // Add double quotes
                name = '"' + name + '"';
            } else {
                if (name.startsWith(strQuote) && name.endsWith(strQuote)) {
                    // Nothing to do
                } else {
                    // Remove spaces
                    name = name.replaceAll(" ", ""); // NOI18N
                }
            }
        }
        if (debugger.getState() != GdbDebugger.STATE_STOPPED) {
            return ""; // happens with tooltip annotations...
        }
        // Save timestamp
        String ts = "Task:" + System.currentTimeMillis(); // NOI18N
        // Generate index
        data_evaluate_index++;
        String index = EMPTY_STRING + data_evaluate_index;
        // Add expression to evaluatedExpressions
        synchronized (evaluatedExpressions) {
            for (i=0; i < (evaluatedExpressions.size() - 3); i++ ) {
                if (name.equals((String) evaluatedExpressions.get(i))) {
                    break;
                }
            }
            if (i >= (evaluatedExpressions.size() - 3)) {
                evaluatedExpressions.add(name);       // Name
                evaluatedExpressions.add(index);      // Index
                evaluatedExpressions.add(null);       // Value
                evaluatedExpressions.add(ts);         // Timestamp
            } else {
                index = (String) evaluatedExpressions.get(i+1); // reuse Index
                //evaluatedExpressions.set(i+1, index); // Index
                evaluatedExpressions.set(i+2, null);  // Value
                evaluatedExpressions.set(i+3, ts);    // Timestamp
            }
        }
        // Create MI command
// FIXME        cmd = MI_CMD_DATA_EVALUATE_EXPRESSION + name;
// FIXME        engine.sendCommand(cmd);
        
        // FIXME (find a way to not force round-trips)
        // Temporary solution: wait for reply (not more than 0.5 second).
        for (int time = 0; time < 500; time += 100) {
            try {
                Thread.sleep(100);
                synchronized (evaluatedExpressions) {
                    for (i=0; i < (evaluatedExpressions.size() - 3); i+=4 ) {
                        if (name.equals((String) evaluatedExpressions.get(i))) {
                            break;
                        }
                    }
                    if (i >= (evaluatedExpressions.size() - 3)) {
                        // Name is not in the list?! No need to wait more.
                        reply = "Internal error: expression is lost"; // NOI18N
                        break;
                    }
                    // Compare timestamps
                    if (!ts.equals(evaluatedExpressions.get(i+3))) {
                        // Timestamp is updated. We are done.
                        reply = (String) evaluatedExpressions.get(i+2); // Value
                        if (reply == null) {
                            reply = "> \""  // NOI18N
                                    + name
                                    + "\" is not a known variable in current context <"; // NOI18N
                        } else {
                            i = reply.indexOf(strValue);
                            if (i > 0) {
                                reply = reply.substring(i + strValue.length());
                            }
                        }
                        break;
                    }
                }
            } catch (InterruptedException tie100) {
                // sleep 100 milliseconds
            }
        }
        GdbTimer.getTimer("Stop").stop("Stop1"); // NOI18N
        GdbTimer.getTimer("Stop").report("Stop1"); // NOI18N
        GdbTimer.getTimer("Stop").free(); // NOI18N
        return reply;
    }
    
    /**
     * Send "-gdb-exit" to the debugger
     * This command forces gdb to exit immediately.
     */
    public int gdb_exit() {
        return engine.sendCommand(MI_CMD_GDB_EXIT);
    }
    
    /**
     * Send "set new-console" to the debugger
     * This command tells gdb to execute inferior program with console.
     */
    public int set_new_console() {
        return engine.sendCommand(CLI_CMD_SET_NEW_CONSOLE);
    }
    
    /**
     * Creates external terminal for program I/O (input, output)
     *
     * @param xterm - short or full name of external terminal
     * @param env   - environment settings from project properties
     * @return null if terminal is not created, otherwise return terminal name
     */
    // FIXME - Not a gdb/mi command
    protected String openExternalProgramIOWindow(GdbProxyEngine engine, String xterm, String[] env) {
        String SessionID = "_" +  System.currentTimeMillis(); // NOI18N
        String cmd;
        String dir = DIR_TMP;
        String fn = "loop" + SessionID + ".sh"; // NOI18N
        String fnl = "loop" + SessionID + ".log"; // NOI18N
        String reply = null;
        //String shScript = "trap \"i=0;\" 1 2 3 4 5 6 7 8 9 10 12 13 14 15; while [ true ]; do sleep 10; done;"; // NOI18N
        String shScript = "while [ true ]; do sleep 10; done;"; // NOI18N
        String term = null;
        String termBinary = null;
        String termDisplay = null;
        String termOptions = " -title \"Debugging\" "; // NOI18N
        termOptions += " -background white "; // NOI18N
        termOptions += " -foreground darkblue "; // NOI18N
        termOptions += " -aw "; // NOI18N
        termOptions += " -e "; // NOI18N
        Thread t = new Thread();
        int timeout = 1000; // Wait not longer than 1000 milliseconds (1 second)
        int notimeout=0; // Don't wait
        //long t1 = System.currentTimeMillis();
        // TEMPORARY CODE. Try to create external terminal
        // /usr/dt/bin/dtterm -title "Debugging"  -background blue
        //        -foreground white  -aw -e /bin/sh /tmp/loop.sh &
        if (Utilities.isWindows()) {
            return null; // IZ 81533 (gdb will create console)
        }
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            // Check if there is xterm
            if (termBinary == null) {
                if ((xterm == null) || (xterm.endsWith("xterm"))) { // NOI18N
                    termBinary = "/usr/openwin/bin/xterm"; // NOI18N
                    File f = new File(termBinary);
                    if (!f.exists()) {
                        //System.err.println("ERROR: GdbProxy.openExternalProgramIOWindow(): /usr/openwin/bin/xterm does not exist"); // DEBUG // NOI18N
                        log.warning("/usr/openwin/bin/xterm does not exist"); // NOI18N
                        termBinary = null;
                    }
                }
            }
            // Check if there is dtterm
            if (termBinary == null) {
                termBinary = "/usr/dt/bin/dtterm"; // NOI18N
                File f = new File(termBinary);
                if (!f.exists()) {
                    //System.err.println("ERROR: GdbProxy.openExternalProgramIOWindow(): /usr/dt/bin/dtterm does not exist"); // DEBUG // NOI18N
                    log.warning("/usr/dt/bin/dtterm does not exist"); // NOI18N
                    termBinary = null;
                }
            }
            if (termBinary == null) {
                //System.err.println("ERROR: GdbProxy.openExternalProgramIOWindow(): No external terminal available"); // DEBUG // NOI18N
                log.severe("ERROR: No external terminal available"); // NOI18N
                return null;
            }
            // Generate script "fn" to get device name and process ID
            cmd = "rm -f " + fn; // NOI18N
            engine.executeExternalCommand(cmd, dir, timeout);
            cmd = "rm -f " + fnl; // NOI18N
            engine.executeExternalCommand(cmd, dir, timeout);
            cmd = "echo 'tty > " + fnl + "' > " + fn; // NOI18N
            engine.executeExternalCommand(cmd, dir, timeout);
            cmd = "echo 'echo $$ >> " + fnl + "' >> " + fn; // NOI18N
            engine.executeExternalCommand(cmd, dir, timeout);
            cmd = "echo '" + shScript + "' >> " + fn; // NOI18N
            engine.executeExternalCommand(cmd, dir, timeout);
            try {
                t.sleep(100); // Wait while script "fn" is saved.
                // Probably we shall check that it is saved.
            } catch (InterruptedException tie100) {
                // sleep 100 milliseconds
            }
            // Start external terminal
            cmd = termBinary;
            cmd += termOptions;
            cmd += " /bin/sh " + fn + " &"; // NOI18N
            engine.executeExternalCommand(cmd, dir, notimeout);
            // Get external terminal device name
            cmd = "head -1 " + fnl; // NOI18N
            for (int  i=0; i < 99; i++) {
                try {
                    t.sleep(100); // Wait while xterm is started.
                } catch (InterruptedException tie100) {
                    // sleep 100 milliseconds
                }
                term = engine.executeExternalCommand(cmd, dir, timeout);
                if (term == null) continue;
                if (term.startsWith("/dev/")) break; // NOI18N
            }
            if (term != null) {
                if (term.startsWith("/dev/")) { // NOI18N
                    //System.err.println("INFO: GdbProxy.openExternalProgramIOWindow(): external terminal device name "+term); // DEBUG // NOI18N
                    //log.info("External terminal device name "+term); // NOI18N
                } else {
                    //System.err.println("ERROR: GdbProxy.openExternalProgramIOWindow(): Invalid external terminal device name "+term); // DEBUG // NOI18N
                    log.severe("ERROR: Invalid external terminal device name "+term); // NOI18N
                    term = null;
                }
            } else {
                //System.err.println("ERROR: GdbProxy.openExternalProgramIOWindow(): Cannot get external terminal device name"); // DEBUG // NOI18N
                log.severe("ERROR: Cannot get external terminal device name"); // NOI18N
                term = null;
            }
            if (term != null) {
                if (term.endsWith("\n")) { // NOI18N
                    int l = term.length();
                    term=term.substring(0, l-1);
                }
                // Get process ID to kill terminal when debugging session is over
                cmd = "tail -1 " + fnl; // NOI18N
                externalTerminalPID = engine.executeExternalCommand(cmd, dir, timeout);
                if (externalTerminalPID.lastIndexOf(' ') > 0) {
                    // There are spaces - this is not a valid PID
                    //System.err.println("WARNING: GdbProxy.openExternalProgramIOWindow(): Cannot get external terminal process ID"); // DEBUG // NOI18N
                    log.warning("Cannot get external terminal process ID"); // NOI18N
                    externalTerminalPID = null;
                }
            }
            // Remove temporary files
            cmd = "rm -f " + fn; // NOI18N
            engine.executeExternalCommand(cmd, dir, notimeout); // Don't wait
            cmd = "rm -f " + fnl; // NOI18N
            engine.executeExternalCommand(cmd, dir, notimeout); // Don't wait
            //System.err.println("INFO: GdbProxy.openExternalProgramIOWindow(): External terminal device name "+term); // DEBUG // NOI18N
            //System.err.println("INFO: GdbProxy.openExternalProgramIOWindow(): External terminal process ID "+externalTerminalPID); // DEBUG // NOI18N
            //log.info("External terminal device name "+term); // NOI18N
            //log.info("External terminal process ID "+externalTerminalPID); // NOI18N
            return term;
        } else {
            // Linux or generic Unix
            termBinary = "/usr/bin/xterm"; // NOI18N
            File f = new File(termBinary);
            if (!f.exists()) {
                //System.err.println("ERROR: GdbProxy.openExternalProgramIOWindow(): /usr/bin/xterm does not exist"); // DEBUG // NOI18N
                log.warning("/usr/bin/xterm does not exist"); // NOI18N
                log.severe("No external terminal available"); // NOI18N
                return null;
            }
            // Generate script "fn" to get device name and process ID
            cmd = "rm -f " + fn; // NOI18N
            engine.executeExternalCommand(cmd, dir, timeout);
            cmd = "rm -f " + fnl; // NOI18N
            engine.executeExternalCommand(cmd, dir, timeout);
            cmd = "echo 'tty > " + fnl + "' > " + fn; // NOI18N
            engine.executeExternalCommand(cmd, dir, timeout);
            cmd = "echo 'echo $$ >> " + fnl + "' >> " + fn; // NOI18N
            engine.executeExternalCommand(cmd, dir, timeout);
            //cmd = "echo 'while [ true ]; do sleep 10; done' >> " + fn; // NOI18N
            cmd = "echo '" + shScript + "' >> " + fn; // NOI18N
            engine.executeExternalCommand(cmd, dir, timeout);
            try {
                t.sleep(100); // Wait while script "fn" is saved.
                // Probably we shall check that it is saved.
            } catch (InterruptedException tie100) {
                // sleep 100 milliseconds
            }
            // Start external terminal
            cmd = termBinary;
            cmd += termOptions;
            cmd += " /bin/sh " + fn + " &"; // NOI18N
            engine.executeExternalCommand(cmd, dir, notimeout); // Don't wait
            // Get external terminal device name
            cmd = "head -1 " + fnl; // NOI18N
            for (int  i=0; i < 99; i++) {
                try {
                    t.sleep(100); // Wait while xterm is started.
                } catch (InterruptedException tie100) {
                    // sleep 100 milliseconds
                }
                term = engine.executeExternalCommand(cmd, dir, timeout);
                if (term == null) continue;
                if (term.startsWith("/dev/")) break; // NOI18N
            }
            if (term != null) {
                if (term.startsWith("/dev/")) { // NOI18N
                    //System.err.println("INFO: GdbProxy.openExternalProgramIOWindow(): external terminal device name "+term); // DEBUG // NOI18N
                    //log.info("External terminal device name "+term); // NOI18N
                } else {
                    //System.err.println("ERROR: GdbProxy.openExternalProgramIOWindow(): Invalid external terminal device name "+term); // DEBUG // NOI18N
                    log.severe("ERROR: Invalid external terminal device name "+term); // NOI18N
                    term = null;
                }
            } else {
                //System.err.println("ERROR: GdbProxy.openExternalProgramIOWindow(): Cannot get external terminal device name"); // DEBUG // NOI18N
                log.severe("ERROR: Cannot get external terminal device name"); // NOI18N
                term = null;
            }
            if (term != null) {
                if (term.endsWith("\n")) { // NOI18N
                    int l = term.length();
                    term=term.substring(0, l-1);
                }
                // Get process ID to kill terminal when debugging session is over
                cmd = "tail -1 " + fnl; // NOI18N
                externalTerminalPID = engine.executeExternalCommand(cmd, dir, timeout);
                if (externalTerminalPID.lastIndexOf(' ') > 0) {
                    // There are spaces - this is not a valid PID
                    //System.err.println("WARNING: GdbProxy.openExternalProgramIOWindow(): Cannot get external terminal process ID"); // DEBUG // NOI18N
                    log.warning("Cannot get external terminal process ID"); // NOI18N
                    externalTerminalPID = null;
                }
            }
            // Remove temporary files
            cmd = "rm -f " + fn; // NOI18N
            engine.executeExternalCommand(cmd, dir, notimeout); // Don't wait
            cmd = "rm -f " + fnl; // NOI18N
            engine.executeExternalCommand(cmd, dir, notimeout); // Don't wait
            //System.err.println("INFO: GdbProxy.openExternalProgramIOWindow(): External terminal device name "+term); // DEBUG // NOI18N
            //System.err.println("INFO: GdbProxy.openExternalProgramIOWindow(): External terminal process ID "+externalTerminalPID); // DEBUG // NOI18N
            //log.info("External terminal device name "+term); // NOI18N
            //log.info("External terminal process ID "+externalTerminalPID); // NOI18N
        }
        return term;
    }
    
    /** Closes External Program I/O Window (external terminal) */
    // FIXME - Should move all PIO stuff to its own file
    public void closeExternalProgramIOWindow() {
        if (externalTerminalPID != null) {
            debugger.kill(9, Long.valueOf(externalTerminalPID.trim()));
            externalTerminalPID = null;
        }
    }
    
    /**
     * Parses message from gdb and updates name, numchild, type
     * fields in variable structure (gdbVariables element)
     * Message format: ^done,name="var1",numchild="0",type="int"
     *
     * @param info Message from gdb
     * @param objIndex String value of index in gdbVariables list
     */
    // FIXME - Not a gdb/mi command
    public void updateVariableType(String info, String objIndex) {
        String name = null;
        String numchild = null;
        String type = null;
        String pattern1 = ",name=\"";     // NOI18N
        String pattern2 = ",numchild=\""; // NOI18N
        String pattern3 = ",type=\"";     // NOI18N
        String pattern4 = "\",";     // NOI18N
        String pattern5 = "\"";     // NOI18N
        int i, j;
        Integer I = new Integer(objIndex);
        int index = I.intValue();
        // Get name
        i = info.indexOf(pattern1);
        if (i >= 0) {
            i = i + pattern1.length();
            j = info.indexOf(pattern4, i);
            if (j > i) {
                name = info.substring(i, j);
            }
        }
        // Get numchild
        i = info.indexOf(pattern2);
        if (i >= 0) {
            i = i + pattern2.length();
            j = info.indexOf(pattern4, i);
            if (j > i) {
                numchild = info.substring(i, j);
            }
        }
        // Get type
        i = info.indexOf(pattern3);
        if (i >= 0) {
            i = i + pattern3.length();
            j = info.indexOf(pattern5, i);
            if (j > i) {
                type = info.substring(i, j);
            }
        }
        // Update element in gdbVariables
//        synchronized (gdbVariables) {
//            if (index < gdbVariables.size()) {
//                List list = (List) gdbVariables.get(index);
//                if (list != null) {
//                    if (list.size() >= 6) {
//                        list.set(1, name);
//                        list.set(3, type);
//                        list.set(5, numchild);
//                        gdbVariables.set(index, list);
//                    }
//                }
//            }
//        }
    }
    
    /**
     * Merges system environment with environment from project profile
     * and returns XTERM value. Returns null if XTERM is not set
     * Used code from org.openide.execution.NbProcessDescriptor
     *
     * @param envp - environment from project profile
     * @return String xterm - value of environment variable XTERM
     */
    // FIXME - Not a gdb/mi command
    protected String getXTERMvalue(String[] envp) {
        String xterm = null;
        Iterator it = System.getProperties().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String prop = (String) entry.getKey();
            if (prop.equals("Env-XTERM")) { // NOI18N
                xterm = (String)entry.getValue();
            }
        }
        for (int i = 0; i < envp.length; i++) {
            String nameval = envp[i];
            int idx = nameval.indexOf('='); // NOI18N
            if (idx > 0) {
                String Name = nameval.substring(0, idx);
                if (Name.equals("XTERM")) { // NOI18N
                    //log.fine("DEBUG: getXTERMvalue(): "+nameval.substring(0, idx)+"="+nameval.substring(idx + 1));
                    xterm = nameval.substring(idx + 1);
                }
            }
        }
        return xterm;
    }
    
} /* End of public class GdbProxy */
