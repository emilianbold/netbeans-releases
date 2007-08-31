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
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
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
    
    private GdbDebugger debugger;
    private GdbProxyEngine engine;
    private GdbLogger gdbLogger;
    
    private String      signalHandlerBreakpoint;
    private String      externalTerminal;
    private String      externalTerminalPID;
    private Vector      evaluatedExpressions;
    private Vector      evaluatedVariables;
    
    // SHELL commands
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
     * @param debugger The GdbDebugger
     * @param debuggerCommand The gdb command to use
     * @param debuggerEnvironment The overrides to the user's environment
     * @param workingDirectory The directory to start the debugger from
     * @throws IOException Pass this on to the caller
     */
    public GdbProxy(GdbDebugger debugger, String debuggerCommand,
            String[] debuggerEnvironment, String workingDirectory, String termpath) throws IOException {
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
        engine = new GdbProxyEngine(debugger, this, dc, debuggerEnvironment, workingDirectory, termpath);
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
    
    /** Ask gdb about a variable (currently used to find the current language) */
    public int gdb_show(String arg) {
        return engine.sendCommand("-gdb-show " + arg); // NOI18N
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
            return engine.sendCommand("-environment-cd  \"" + dir + "\""); // NOI18N
        } else {
            return engine.sendCommand("directory \"" + dir + "\""); // NOI18N
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
     * Send "set new-console" to the debugger
     * This command tells gdb to execute inferior program with console.
     */
    public int set_new_console() {
        return engine.sendCommand(CLI_CMD_SET_NEW_CONSOLE);
    }
    
    /**
     * Request the type of a symbol. As of gdb 6.6, this is unimplemented so we send a
     * non-mi command "ptype". We should only be called when symbol is in scope.
     */
    public int symbol_type(String symbol) {
        return engine.sendCommand("ptype " + symbol); // NOI18N
    }
    
    /**
     * Request the type of a symbol. As of gdb 6.6, there is no gdb/mi way of doing this
     * so we send a gdb "whatis" command. This is different from -system-type in the case
     * of abstract data structures (structs and classes). Its the same for other types.
     */
    public int whatis(String symbol) {
        return engine.sendCommand("whatis " + symbol); // NOI18N
    }
    
    /**
     * Send "-gdb-exit" to the debugger
     * This command forces gdb to exit immediately.
     */
    public int gdb_exit() {
        return engine.sendCommand(MI_CMD_GDB_EXIT);
    }
    
} /* End of public class GdbProxy */
