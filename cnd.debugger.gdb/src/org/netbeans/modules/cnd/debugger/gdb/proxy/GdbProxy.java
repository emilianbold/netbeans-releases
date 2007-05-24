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
    
    private String      shortProgramName;
    private String      signalHandlerBreakpoint;
    private String      stepIntoProjectBreakpoint;
    private String      externalTerminal;
    private String      externalTerminalPID;
    private Vector      evaluatedExpressions;
    private Vector      evaluatedVariables;
    private int nextToken = 100;
    
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
        stepIntoProjectBreakpoint = null;
        log.setLevel(Level.FINE);
        
        ArrayList dc = new ArrayList();
        dc.add(debuggerCommand);
        dc.add("--nw");  // NOI18N
        dc.add("--interpreter=mi"); // NOI18N
        
        gdbLogger = new GdbLogger(debugger, this);
        engine = new GdbProxyEngine(debugger, this, dc, debuggerEnvironment, workingDirectory);
        
        if (Utilities.isWindows()) {
            set_new_console();
        }
    }
    
    protected GdbProxyEngine getProxyEngine() {
        return engine;
    }
    
    public GdbLogger getLogger() {
        return gdbLogger;
    }
    
    private int nextToken() {
        return nextToken++;
    }
    
    /**
     * Load the program
     *
     * @param program - a name of an external program to debug
     */
    public void file_exec_and_symbols(String program) {
        StringBuilder programName = new StringBuilder();
        int token = nextToken();
        
        for (int i = 0; i < program.length(); i++) {
            if (program.charAt(i) == '\\') {
                programName.append('/');
            } else {
                if (program.charAt(i) == ' ') {
                    programName.append("\\ "); // NOI18N
                } else {
                    programName.append(program.charAt(i));
                }
            }
        }
        
        // Tell low level to send the command to the debugger
        engine.sendCommand(String.valueOf(token) + MI_CMD_FILE_EXEC_AND_SYMBOLS + programName);
        
        // Set search path directory to look for sources (for gdb 6.1 and gdb 6.2)
        int i = programName.lastIndexOf("/"); // NOI18N
        if (!Utilities.isWindows() && i >= 0 && debugger.getGdbVersion() < 6.3) {
            // Note: We're checking gdb's version before its set...
            engine.sendCommand("directory " + programName.substring(0, i));
        }
        // Set shortProgramName to find its PID
        shortProgramName = programName.toString();
        if (i > 0) {
            shortProgramName = programName.substring(i + 1);
        }
        // Remove quotes
        if (programName.charAt(0) == '\"') {
            if (shortProgramName.charAt(0) == '\"') {
                shortProgramName = shortProgramName.substring(1);
            }
            int len = shortProgramName.length()-1;
            if (shortProgramName.charAt(len) == '\"') {
                shortProgramName = shortProgramName.substring(0, len);
            }
        }
        // Set external terminal (not implemented yet)
        // -inferior-tty-set /dev/pts/1
        // -inferior-tty-show
        // reply = engine.sendCommand(cmd);
        // Set breakpoint
        //break_insert("main"); // DEBUG
        //engine.sendCommand(cmd);
    }
    
    /**
     *  Do a "set environment" gdb command.
     *
     *  @param var Variable of the form "foo=value"
     */
    public void gdb_set_environment(String var) {
        engine.sendCommand(nextToken() + MI_CMD_GDB_SET_ENVIRONMENT + var);
    }
    
    /**
     *  Ask gdb about threads. We don't really care about the threads, but it also returns
     *  the process ID, which we do care about.
     *
     *  Note: In gdb 6.5.50 the -threads-list-all-threads command isn't implemented so we
     *  revert to the gdb command "info threads".
     */
    public void info_threads() {
        engine.sendCommand(nextToken() + MI_CMD_INFO_THREADS);
    }
    
    /**
     *  Ask gdb about /proc info. We don't really care about the /proc, but it also returns
     *  the process ID, which we do care about.
     */
    public void info_proc() {
        engine.sendCommand(nextToken() + MI_CMD_INFO_PROC);
    }
    
    /**
     *  Use this to call _CndSigInit() to initialize signals in Cygwin processes.
     */
    public void data_evaluate_expression(String string) {
        engine.sendCommand(nextToken() + MI_CMD_DATA_EVALUATE_EXPRESSION + string);
    }
    
    /**
     * Send "-file-list-exec-source-file" to the debugger
     * This command lists the line number, the current source file,
     * and the absolute path to the current source file for the current executable.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public void file_list_exec_source_file() {
        engine.sendCommand(nextToken() + MI_CMD_FILE_LIST_EXEC_SOURCE_FILE);
    }
    
    /**
     * Send "-exec-run" with parameters to the debugger
     * This command starts execution of the inferior from the beginning.
     * The inferior executes until either a breakpoint is encountered or
     * the program exits.
     *
     * @param programParameters - command line options for the program
     *
     * @return null if action is accepted, otherwise return error message
     */
    public int exec_run(String programParameters) {
        int token = nextToken();
        
        engine.sendCommand(String.valueOf(token) + MI_CMD_EXEC_RUN + programParameters);
        return token;
    }
    
    /**
     * Send "-exec-run" to the debugger
     * This command starts execution of the inferior from the beginning.
     * The inferior executes until either a breakpoint is encountered or
     * the program exits.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public void exec_run() {
        exec_run("");
    }
    
    /**
     * Send "-exec-step" to the debugger
     * This command resumes execution of the inferior program, stopping
     * when the beginning of the next source line is reached, if the next
     * source line is not a function call.
     * If it is, stop at the first instruction of the called function.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public void exec_step() {
        engine.sendCommand(nextToken() + MI_CMD_EXEC_STEP);
    }
    
    /**
     * Send "-exec-next" to the debugger (go to the next source line)
     * This command resumes execution of the inferior program, stopping
     * when the beginning of the next source line is reached.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public void exec_next() {
        engine.sendCommand(nextToken() + MI_CMD_EXEC_NEXT);
    }
    
    /**
     * Send "-exec-finish" to the debugger (finish this function)
     * This command resumes execution of the inferior program until
     * the current function is exited.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public void exec_finish() {
        engine.sendCommand(nextToken() + MI_CMD_EXEC_FINISH);
    }
    
    /**
     * Send "-exec-continue" to the debugger
     * This command resumes execution of the inferior program, until a
     * breakpoint is encountered, or until the inferior exits.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public void exec_continue() {
        engine.sendCommand(nextToken() + MI_CMD_EXEC_CONTINUE);
    }
    
    /**
     * Interrupts execution of the inferior program.
     * This method is supposed to send "-exec-interrupt" to the debugger,
     * but this feature is not implemented in gdb yet, so it is replaced
     * with sending a signal "INT" (Unix) or signal TSTP (Windows).
     */
    public void exec_interrupt() {
        if (debugger.getState() == GdbDebugger.STATE_RUNNING) {
            if (Utilities.isWindows()) {
                debugger.kill(18);
            } else {
                debugger.kill(2);
            }
        }
    }
    
    /**
     * Send "-exec-abort" to the debugger
     * This command kills the inferior program.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public void exec_abort() {
        String cmd;
        
        // -exec-abort isn't implemented yet (as of gdb 6.6)
        if (debugger.getGdbVersion() > 6.6) {
            cmd = MI_CMD_EXEC_ABORT;
        } else {
            cmd = CLI_CMD_KILL;
        }
        engine.sendCommand(nextToken() + cmd);
    }
    
    /**
     * Send "-break-insert function" to the debugger
     * This command inserts a regular breakpoint in all functions
     * whose names match the given name.
     *
     * @param name - a function name
     * @return token number
     */
    public int break_insert(int flags, String name) {
        StringBuilder cmd = new StringBuilder();
        int token = nextToken();
        
        cmd.append(String.valueOf(token));
        cmd.append(MI_CMD_BREAK_INSERT);
        if ((flags & GdbDebugger.GDB_TMP_BREAKPOINT) != 0) {
            cmd.append("-t "); // NOI18N
        }
        
        // Temporary fix for Windows
        if (Utilities.isWindows()) {
            if(name.indexOf('/') == 0) {
                if(name.indexOf(':') == 2) {
                    // Remove first slash
                    name = name.substring(1);
                }
            }
        }
        cmd.append(name);
        engine.sendCommand(cmd.toString());
        return token;
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
    public void break_delete(int number) {
        engine.sendCommand(nextToken() + MI_CMD_BREAK_DELETE + Integer.toString(number));
    }
    
    /**
     * Send "-break-enable number" to the debugger
     * This command enables the breakpoint
     * whose number is specified by the argument.
     *
     * @param number - breakpoint number
     */
    public void break_enable(int number) {
        engine.sendCommand(nextToken() + MI_CMD_BREAK_ENABLE + Integer.toString(number));
    }
    
    /**
     * Send "-break-disable number" to the debugger
     * This command disables the breakpoint
     * whose number is specified by the argument.
     *
     * @param number - breakpoint number
     */
    public void break_disable(int number) {
        engine.sendCommand(nextToken() + MI_CMD_BREAK_DISABLE + Integer.toString(number));
    }
    
    /** Send "-stack-list-locals" to the debugger */
    public void stack_list_locals() {
        stack_list_locals(""); // NOI18N
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
    public void stack_list_locals(String printValues) {
        engine.sendCommand(nextToken() + MI_CMD_STACK_LIST_LOCALS + printValues);
    }
    
    public void stack_list_arguments(int showValues, int low, int high) {
        engine.sendCommand(nextToken() + "-stack-list-arguments " + showValues + " " + low + " " + high); // NOI18N
    }
    
    /**
     * Send "-stack-select-frame frameNumber" to the debugger
     * This command tells gdb to change the current frame.
     * Select a different frame frameNumber on the stack.
     */
    public void stack_select_frame(String frameNumber) {
        engine.sendCommand(nextToken() + MI_CMD_STACK_SELECT_FRAME + frameNumber);
    }
    
    /**
     * Send "-stack-info-frame " to the debugger
     * This command asks gdb to provide information about current frame.
     */
    public void stack_info_frame() {
        engine.sendCommand(nextToken() + MI_CMD_STACK_INFO_FRAME);
    }
    
    /**
     * Request a stack dump from gdb.
     */
    public void stack_list_frames() {
        engine.sendCommand(nextToken() + MI_CMD_STACK_LIST_FRAMES);
    }
    
    /**
     * This method creates a variable object, which allows the monitoring of a
     * variable, the result of an expression, a memory cell or a CPU register.
     * The name parameter is the string by which the object can be referenced.
     * It must be unique. If "-" is specified, the varobj system will generate
     * a string "varNNNNNN" automatically. It will be unique provided that one
     * does not specify name on that format. The command fails if a duplicate
     * name is found. The frame under which the expression should be evaluated
     * can be specified by frame-addr. A "*" indicates that the current frame
     * should be used. A "0" indicates that top frame should be used. The
     * "expression" parameter is any expression valid on the current language
     * set (must not begin with a `*'), or one of the following:
     * `*addr', where addr is the address of a memory cell
     * `*addr-addr' -- a memory address range (TBD)
     * `$regname' -- a CPU register name
     *
     * Result
     * This command returns the name, number of children and the type of the
     * object created.
     * Type is returned as a string as the ones generated by the GDB CLI:
     *  name="name",numchild="N",type="type"
     */
    private int varindex = 0;
    private ArrayList gdbVariables = new ArrayList();
    
    public void var_create(String name, String frame, String expression) {
        String cmd;
        
        synchronized (gdbVariables) {
            if (frame == null) frame = "*"; // NOI18N
            for (int i = gdbVariables.size() - 1; i >= 0; i--) {
                List l = (List) gdbVariables.get(i);
                if (l == null) continue;
                if (l.size() < 3) continue;
                if(expression.equals((String) l.get(0))) {
                    if(frame.equals((String) l.get(2))) {
                        if (name == null || name.equals((String) l.get(1))) {
                            return; // Found. Nothing to do.
                        }
                    }
                }
            }
            if (name == null) name = VAR_STRING + varindex;
            List list = new ArrayList();
            list.add(expression);
            list.add(name);
            list.add(frame);
            list.add(null); // type
            list.add(null); // value
            list.add(null); // numchild
            gdbVariables.add(list);
            cmd = MI_CMD_VAR_CREATE + PARAM_SEPARATOR + frame + PARAM_SEPARATOR + expression;
            varindex++; // Must be unique
        }
// FIXME        engine.sendCommand(nextToken() + cmd);
    }
    
    /**
     * Deletes a previously created variable object and all of its children.
     * Returns an error if the object name is not found.
     */
    public void var_delete(String name) {
        if (name != null) {
            String cmd = MI_CMD_VAR_DELETE + name;
            synchronized (gdbVariables) {
                for (int i = gdbVariables.size() - 1; i > 0; i--) {
                    List list = (List) gdbVariables.get(i);
                    if (list == null) continue;
                    if(name.equals(list.get(1))) {
                        if (gdbVariables.size() - 1 == i) {
                            // If it is the last one, remove and adjust varindex
                            gdbVariables.remove(i);
                            varindex--; // Must be unique
                        } else {
                            gdbVariables.set(i, null);
                        }
                    }
                }
            }
// FIXME            engine.sendCommand(nextToken() + cmd);
        }
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
    public String getVariableField(String expression, int field) {
        String reply = ""; // NOI18N;
        synchronized (gdbVariables) {
            if (expression == null) return reply;
            for (int i = gdbVariables.size() - 1; i >= 0; i--) {
                List list = (List) gdbVariables.get(i);
                if (list == null) continue;
                if(expression.equals((String) list.get(0))) {
                    reply = (String) list.get(field);
                    break;
                }
            }
        }
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
    
    /**
     * Gets variable value.
     * Returns empty string if value is not available.
     *
     * @param expression a variable name or expression
     * @return variable value
     */
    // FIXME - Not a gdb/mi command
    public String getVariableValue(String expression) {
        return getVariableField(expression, 4);
    }
    
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
     * Assigns the value of expression to the variable object specified by name.
     * The object must be `editable'. If the variable's value is altered by the
     * assign, the variable will show up in any subsequent -var-update list.
     */
    public void var_assign(String name, String expression) {
        if (name != null) {
// FIXME            engine.sendCommand(nextToken() + MI_CMD_VAR_ASSIGN +
// FIXME                    name + PARAM_SEPARATOR + expression);
        }
    }
    
    /**
     * Evaluates the expression that is represented by the specified variable object
     * and returns its value as a string.
     *
     * NOTE: This method is currently unused (as of cnd 5.5.1 FCS)
     */
    private int var_evaluate_index = 0;
    public void var_evaluate(String name) {
//        String cmd;
//        String reply;
//        int i;
//        if (name == null || name.length() == 0) {
//            throw new IllegalStateException("Internal error: variable name is not specified");
//        }
//        
//        if (programStatus != STOPPED) {
//            throw new IllegalStateException("Internal error: Program is not stopped");
//        }
//        // Save timestamp
//        String ts = "Task:" + System.currentTimeMillis(); // NOI18N
//        // Generate index
//        var_evaluate_index++;
//        String index = Integer.toString(var_evaluate_index);
//        // Add expression to evaluatedVariables
//        synchronized (evaluatedVariables) {
//            for (i=0; i < (evaluatedVariables.size() - 3); i++ ) {
//                if (name.equals((String) evaluatedVariables.get(i))) {
//                    break;
//                }
//            }
//            if (i >= (evaluatedVariables.size() - 3)) {
//                evaluatedVariables.add(name);       // Name
//                evaluatedVariables.add(index);      // Index
//                evaluatedVariables.add(null);       // Value
//                evaluatedVariables.add(ts);         // Timestamp
//            } else {
//                evaluatedVariables.set(i+1, index); // Index
//                evaluatedVariables.set(i+2, null);  // Value
//                evaluatedVariables.set(i+3, ts);    // Timestamp
//            }
//        }
//        // Create MI command
//        cmd = MI_TOKEN_VAR_EVALUATE_EXPR + index;
//        cmd += MI_CMD_VAR_EVALUATE_EXPR + name;
//        reply = engine.sendCommand(cmd);
//        if (reply != null) return reply;
//        // Temporary solution: wait for reply (not more than 0.5 second).
//        Thread t = new Thread();
//        for (int time=0; time < 500; time+=100) {
//            try {
//                t.sleep(100);
//                synchronized (evaluatedVariables) {
//                    for (i=0; i < (evaluatedVariables.size() - 3); i++ ) {
//                        if (name.equals((String) evaluatedVariables.get(i))) {
//                            break;
//                        }
//                    }
//                    if (i >= (evaluatedVariables.size() - 3)) {
//                        // Name is not in the list?! No need to wait more.
//                        reply = "Internal error: expression is lost"; // NOI18N
//                        break;
//                    }
//                    // Compare timestamps
//                    if (!ts.equals(evaluatedVariables.get(i+3))) {
//                        // Timestamp is updated. We are done.
//                        reply = (String) evaluatedVariables.get(i+2); // Value
//                        break;
//                    }
//                }
//            } catch (InterruptedException tie100) {
//                // sleep 100 milliseconds
//            }
//        }
//        return reply;
    }
    
    /**
     * Evaluates the expression and returns its value as a string.
     *
     * @return null if value is not received, otherwise return value
     */
    private int data_evaluate_index = 0;
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
// FIXME        engine.sendCommand(nextToken() + cmd);
        
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
    public void gdb_exit() {
        engine.sendCommand(nextToken() + MI_CMD_GDB_EXIT);
    }
    
    /**
     * Send "set new-console" to the debugger
     * This command tells gdb to execute inferior program with console.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public void set_new_console() {
        engine.sendCommand(nextToken() + CLI_CMD_SET_NEW_CONSOLE);
    }
    
//    /**
//     * Replace "/cygdrive/c/" with "c:/" in fullname
//     *
//     * @param info - information from debugger
//     *               Format:  key1="value1",key2="value2",...
//     */
//    // FIXME - Not a gdb/mi command
//    public String adjustFullname(String info) {
//        final String pattern = "\",fullname=\"/cygdrive/"; //NOI18N
//        final String colon = ":"; //NOI18N
//        String s = info;
//        int i = s.indexOf(pattern);
//        while (i >= 0) {
//            // IZ 81889 replace "/cygdrive/c/" with "c:/"
//            if (s.length() > (i+23)) {
//                s = s.substring(0, i+12) // i+12 points to /cygdrive/
//                + s.substring(i+22, i+23)
//                + colon + s.substring(i+23);
//            } else {
//                // Truncated line?
//                break;
//            }
//            i = s.indexOf(pattern);
//        }
//        final String beginpattern = "\",fullname=\"/"; //NOI18N
//        i = s.indexOf(beginpattern);
//        if (i >= 0) {
//            s = cygpathToWindows(info);
//        }
//        return s;
//    }
//    
//    /**
//     * Translate  fullname from Cygwin format to Windows
//     *
//     * @param info - information from debugger
//     *               Format:  key1="value1",key2="value2",...
//     */
//    // FIXME - Not a gdb/mi command
//    public String cygpathToWindows(String info) {
//        final String beginpattern = "\",fullname=\"/"; //NOI18N
//        final String endpattern = "\","; //NOI18N
//        String cmd = "cygpath -m "; //NOI18N
//        String s = info;
//        String cfilename;
//        String wfilename;
//        //long t1 = System.currentTimeMillis();
//        int i = s.indexOf(beginpattern);
//        while (i >= 0) {
//            // IZ 81889 Use cygpath -m to translate Cygwin format to Windows
//            int j = s.indexOf(endpattern, i + 12);
//            if (j > (i+13)) {
//                cfilename = s.substring(i + 12, j);
//                wfilename = engine.executeExternalCommand(cmd + cfilename, "/", 1000); //NOI18N
//                if (wfilename != null) {
//                    if (wfilename.charAt(1) == ':') { // Windows filename
//                        if (wfilename.endsWith("\n")) { // NOI18N
//                            int l = wfilename.length();
//                            wfilename=wfilename.substring(0, l-1);
//                        }
//                        s = s.substring(0, i+12) // i+12 points to first /
//                        + wfilename + s.substring(j);
//                    }
//                }
//            } else {
//                // Truncated line?
//                break;
//            }
//            i = s.indexOf(beginpattern);
//        }
//        //t1 = System.currentTimeMillis() - t1;
//        return s;
//    }
    
    /**
     * Creates external terminal for program I/O (input, output)
     *
     * @param xterm - short or full name of external terminal
     * @param env   - environment settings from project properties
     * @return null if terminal is not created, otherwise return terminal name
     */
    // FIXME - Not a gdb/mi command
    private String openExternalProgramIOWindow(String xterm, String[] env) {
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
//        if (Utilities.isWindows()) {
//            // By default use Windows "native" window
//            String ttybat = "tty" + SessionID + ".bat"; // NOI18N
//            String windir = "C:\\Cygwin\\tmp"; // NOI18N
//            dir = "C:/Cygwin/tmp"; // NOI18N
//            cmd = "df -k . "; // NOI18N
//            reply=engine.executeExternalCommand(cmd, DIR_TMP, timeout);
//            if (reply.startsWith("Filesystem")) { // NOI18N
//                int i = reply.indexOf('\n');
//                if (i > 0) {
//                    reply = reply.substring(i+1);
//                    if (reply.endsWith(" /\n")) { // NOI18N
//                        i = reply.indexOf(' ');
//                        if (i > 0) {
//                            reply = reply.substring(0, i);
//                            windir = reply + "\\tmp"; // NOI18N
//                            dir = DIR_TMP;
//                            //log.fine("GdbProxy.openExternalProgramIOWindow() windir="+windir+" dir="+dir); // DEBUG
//                        }
//                    }
//                }
//            }
//            termBinary = null;
//            if (xterm != null) {
//                if ( xterm.equals("xterm")) { // NOI18N
//                    // Check if there is xterm
//                    termBinary = xterm;
//                    cmd = "which " + termBinary; // NOI18N
//                    reply=engine.executeExternalCommand(cmd, dir, timeout);
//                    if (reply == null) {
//                        termBinary = null;
//                    } else if (!reply.startsWith("/")) { // NOI18N
//                        //log.fine("ERROR: GdbProxy.openExternalProgramIOWindow() which xterm\n"+reply); // DEBUG
//                        termBinary = null;
//                    }
//                    if (termBinary != null) {
//                        //log.fine("GdbProxy.openExternalProgramIOWindow() termBinary="+termBinary); // DEBUG
//                        termDisplay = " -display 127.0.0.1:0.0"; // NOI18N
//                        termOptions = termDisplay + termOptions;
//                        // Start X-Windows
//                        // this is not necessary if XWin is already running,
//                        // but it is hard to verify, so we start it again.
//                        // cmd = "sh " + DIR_TMP + "/startxwin.sh"; // NOI18N
//                        cmd = "sh startxwin.sh"; // NOI18N
//                        reply = engine.executeExternalCommand(cmd, dir, notimeout); // Don't wait
//                        if (reply != null) {
//                            termBinary = null;
//                        }
//                    }
//                }
//            }
//            // Create temporary directory
//            cmd = "mkdir -p " + dir; // NOI18N
//            engine.executeExternalCommand(cmd, dir, timeout);
//            cmd = "rm -f " + fn; // NOI18N
//            engine.executeExternalCommand(cmd, dir, timeout);
//            cmd = "rm -f " + fnl; // NOI18N
//            engine.executeExternalCommand(cmd, dir, timeout);
//            // Create a shell script, which will print tty, pid, and then hang
//            cmd = "touch " + fn; // NOI18N
//            engine.executeExternalCommand(cmd, dir, timeout);
//            cmd = "chmod 700 " + fn; // NOI18N
//            engine.executeExternalCommand(cmd, dir, timeout);
//            cmd = "echo 'cd " + dir + "' >> " + fn; // NOI18N
//            engine.executeExternalCommand(cmd, dir, timeout);
//            cmd = "echo 'tty > " + fnl + "' >> " + fn; // NOI18N
//            engine.executeExternalCommand(cmd, dir, timeout);
//            //try {
//            //   t.sleep(100); // Wait while file is updated.
//            //} catch (InterruptedException tie100) {
//            // sleep 100 milliseconds
//            //}
//            cmd = "echo 'echo $$ >> " + fnl + "' >> " + fn; // NOI18N
//            engine.executeExternalCommand(cmd, dir, timeout);
//            //try {
//            //    t.sleep(100); // Wait while file is updated.
//            //} catch (InterruptedException tie100) {
//            // sleep 100 milliseconds
//            //}
//            //cmd = "echo 'while [ true ]; do sleep 10; done' >> " + fn; // NOI18N
//            cmd = "echo '" + shScript + "' >> " + fn; // NOI18N
//            engine.executeExternalCommand(cmd, dir, timeout);
//            //try {
//            //    t.sleep(100); // Wait while file is updated.
//            //} catch (InterruptedException tie100) {
//            // sleep 100 milliseconds
//            //}
//            if (termBinary != null) {
//                cmd = termBinary;
//                cmd += termOptions;
//                cmd += " sh " + fn + " &"; // NOI18N
//                //timeout = 0; // Only 0. Otherwise it hangs till xterm exits.
//                //log.fine("GdbProxy.openExternalProgramIOWindow() cmd="+cmd); // DEBUG
//                reply=engine.executeExternalCommand(cmd, dir, notimeout); // Don't wait
//                if (reply != null) termBinary = null;
//            }
//            if (termBinary == null) {
//                // Use Windows "native" window
//                cmd = "rm -f " + ttybat; // NOI18N
//                engine.executeExternalCommand(cmd, dir, timeout);
//                cmd = "touch " + ttybat; // NOI18N
//                engine.executeExternalCommand(cmd, dir, timeout);
//                // Create a batch file, which will run bash with tty
//                cmd = "echo '@echo off' >> " + ttybat; // NOI18N
//                engine.executeExternalCommand(cmd, dir, timeout);
//                cmd = "echo 'set CYGWIN=tty binmode' >> " + ttybat; // NOI18N
//                engine.executeExternalCommand(cmd, dir, timeout);
//                cmd = "echo 'bash --login -i -c " + dir + "/" + fn + "' >> " + ttybat; // NOI18N
//                engine.executeExternalCommand(cmd, dir, timeout);
//                String[] cmda = new String[] {
//                    "cmd.exe",  // NOI18
//                    "/c", // NOI18N
//                    ttybat
//                };
//                String[] mergedEnv = mergeEnv(env);
//                reply = engine.executeExternalCommandWithoutShell(
//                        cmda,
//                        mergedEnv,
//                        windir,
//                        notimeout); // Don't wait
//                /* Test 1
//                cmda = new String[] {
//                    "cmd.exe",  // NOI18
//                };
//                String reply1 = engine.executeExternalCommandWithoutShell(
//                            cmda,
//                            mergedEnv,
//                            windir,
//                            notimeout); // Don't wait
//                 */
//                /* Test 2
//                cmda = new String[] {
//                    "notepad.exe",  // NOI18
//                    ttybat
//                };
//                String reply2 = engine.executeExternalCommandWithoutShell(
//                            cmda,
//                            mergedEnv,
//                            windir,
//                            notimeout); // Don't wait
//                 */
//            }
//            if (reply != null) return null;
//            for (int  i=0; i < 99; i++) {
//                try {
//                    t.sleep(100); // Wait while xterm is started.
//                } catch (InterruptedException tie100) {
//                    // sleep 100 milliseconds
//                }
//                //if ((i/10)*10 == i) {
//                //cmd = "ps"; // | grep " + fn; // NOI18N
//                //reply=engine.executeExternalCommand(cmd, dir, timeout);
//                //log.fine("GdbProxy.createExternalProgramIOWindow() i="+i+"  ps | grep \n"+reply); // DEBUG
//                //}
//                cmd = "head -1 " + fnl; // NOI18N
//                term = engine.executeExternalCommand(cmd, dir, timeout);
//                if (term == null) continue;
//                if (term.startsWith("/dev/")) break; // NOI18N
//                if (term.startsWith("not a tty")) break; // NOI18N
//            }
//            cmd = "head -1 " + fnl; // NOI18N
//            String term2 = engine.executeExternalCommand(cmd, dir, timeout);
//            //log.fine("GdbProxy.openExternalProgramIOWindow() term="+term2); // DEBUG
//            // Get process ID to kill terminal when debugging session is over
//            cmd = "tail -1 " + fnl; // NOI18N
//            externalTerminalPID = engine.executeExternalCommand(cmd, dir, timeout);
//            if(externalTerminalPID != null) {
//                if(externalTerminalPID.lastIndexOf(' ') > 0) {
//                    // There are spaces - this is not a valid PID
//                    externalTerminalPID = null;
//                }
//            }
//            // Remove temporary files
//            cmd = "rm -f " + ttybat; // NOI18N
//            engine.executeExternalCommand(cmd, dir, notimeout); // Don't wait
//            cmd = "rm -f " + fn; // NOI18N
//            engine.executeExternalCommand(cmd, dir, notimeout); // Don't wait
//            cmd = "rm -f " + fnl; // NOI18N
//            engine.executeExternalCommand(cmd, dir, notimeout); // Don't wait
//            if (term2 == null) {
//                // Could not create external terminal
//                externalTerminalPID = null;
//                return null;
//            }
//            if (!term2.equals(term)) term = term2;
//            if (term.endsWith("\n")) { // NOI18N
//                int l = term.length();
//                term=term.substring(0, l-1);
//            }
//            if (!term.startsWith("/dev/")) { // NOI18N
//                // Could not create external terminal
//                externalTerminalPID = null;
//                return null;
//            }
//            //log.fine("GdbProxy.openExternalProgramIOWindow() externalTerminalPID="+externalTerminalPID); // DEBUG
//            //log.fine("GdbProxy.openExternalProgramIOWindow() externalTerminal="+term); // DEBUG
//            return term;
//        }
        if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
            // Check if there is a gnome-terminal
            // The code below is not good. It is using /bin/which,
            // which is slow. Better to check if file exists:
            //    /usr/bin/gnome-terminal
            //    /usr/openwin/bin/xterm
            //    /usr/dt/bin/dtterm
            //timeout = 100;
            // IZ 80510 Problem 6: /bin/gnome-terminal does not start in some cases
            /* IZ 80531 Program I/O Window not reliable enough
//            if ((xterm != null) && (xterm.equals("gnome-terminal"))) {
//                termBinary = null;
//                cmd = "which gnome-terminal"; // NOI18N
//                reply=engine.executeExternalCommand(cmd, dir, timeout);
//                if (reply != null) {
//                    if (reply.startsWith("/")) { // NOI18N
//                        if (reply.endsWith("\n")) { // NOI18N
//                            int l = reply.length();
//                            termBinary=reply.substring(0, l-1);
//                        } else {
//                            termBinary = reply;
//                        }
//                        termOptions = " --title \"Debugging\" "; // NOI18N
//                        termOptions += " --show-menubar "; // NOI18N
//                        termOptions += " --execute "; // NOI18N
//                        //log.fine("GdbProxy.openExternalProgramIOWindow() gnome-terminal\n"+reply); // DEBUG
//                    }
//                }
//                if (termBinary == null) {
//                    log.fine("ERROR: GdbProxy.openExternalProgramIOWindow() cannot find gnome-terminal\n"); // DEBUG
//                }
//            }
             **/
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
    private void closeExternalProgramIOWindow() {
        if (externalTerminalPID != null) {
            debugger.kill(9, Long.valueOf(externalTerminalPID));
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
        synchronized (gdbVariables) {
            if (index < gdbVariables.size()) {
                List list = (List) gdbVariables.get(index);
                if (list != null) {
                    if (list.size() >= 6) {
                        list.set(1, name);
                        list.set(3, type);
                        list.set(5, numchild);
                        gdbVariables.set(index, list);
                    }
                }
            }
        }
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
    private String getXTERMvalue(String[] envp) {
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
