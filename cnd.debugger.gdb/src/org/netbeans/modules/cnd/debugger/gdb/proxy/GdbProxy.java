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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.proxy;

/*
 * GdbProxy.java
 *
 * @author Nik Molchanov
 *
 * Originally this class was in org.netbeans.modules.cnd.debugger.gdb package.
 * Later a new "proxy" package was created and this class was moved, that's how
 * it lost its history. To view the history look at the previous location.
 */

import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.debugger.gdb.*;

/**
 * Class GdbProxy represents the public API with gdb driver
 */
public class GdbProxy {
    
    protected GdbProxyListener gdbSupport = null;
    protected GdbProxyVL gdbProxyVL;
    protected GdbProxyCL gdbProxyCL;
    protected GdbProxyML gdbProxyML;
    
    protected String debuggerCommand;
    protected String[] debuggerEnvironment;
    protected String workingDirectory;
    protected String debugger;
    protected String debuggerParameters;
    protected String environment;
    protected String program;
    protected String programParameters;

    //Performance measurements
    //public long globalStartTimeSetBreakpoint = System.currentTimeMillis(); // DEBUG
    
    /**
     * Creates a new instance of GdbProxy
     *
     * @param listener - gdbSupport listener
     */
    public GdbProxy(GdbProxyListener listener) {
        this.gdbSupport = listener;
        gdbProxyVL = new GdbProxyVL(this);
        gdbProxyCL = new GdbProxyCL(this);
        gdbProxyML = new GdbProxyML(this);
    }
    
    /**
     * Starts the debugger
     *
     * @param debuggerCommand - a name of an external debugger with parameters
     * @param workingDirectory - a directory where the debugger should run
     * @param debuggerEnvironment - environment variables and values to be set
     * @param stepInProject - a flag to stop at first source line
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String startDebugging(String debuggerCommand, String[] debuggerEnvironment,
                String workingDirectory, boolean stepInProject) {
        this.debuggerCommand = debuggerCommand;
        this.workingDirectory = workingDirectory;
        this.debuggerEnvironment = debuggerEnvironment;
        
        // Start debugging
        return gdbProxyCL.startDebugging(debuggerCommand,  debuggerEnvironment, workingDirectory, stepInProject);
    }
    
    /**
     *  We can do some path mapping if we know that gdb is a cygwin version.
     */
    public boolean isCygwin() {
	return gdbProxyCL.isCygwin();
    }
    
    /**
     * Transforms gdb message to a string of arguments and returns this string.
     * This string is saved in private cache (saveArgs). It is used
     * by reportStackUpdate() method, which is called later.
     *
     * @param args a string from gdb with function arguments
     *
     * @return string of arguments
     */
    public String getArguments(String args) {
        return gdbProxyVL.getArguments(args);
    }
    
    /**
     *  Parse the input string for key/value pairs. Each key should be unique so
     *  results can be stored in a map.
     *
     *  @param info A string of key/value pairs where each key/value
     *  @return A HashMap containing each key/value
     */
    public Map createMapFromString(String info) {
        return gdbProxyVL.createMapFromString(info);
    }
    
    /**
     *  Parse the input string for key=value pairs. The keys are not unique
     *  and the order is very important. Each value is extracted and stored
     *  in an ArrayList as a 3-Strings entry for each variable (name, type, value)
     *  Input formats:
     *   {name="A",type="int",value="1"},{name="A",type="int",value="1"},...
     *   {name="a",value="{fa = 1}"},{name="b",value="{fs1 = {fa = 2}}"},...
     *
     *  @param info A string of key=value pairs. Format {name="A",type="int",value="1"},...
     *
     *  @return ArrayList with 3-Strings entry for each variable (name, type, value)
     */
    public List createListFromLocalsStrings(String simpleValues, String allValues) {
        return gdbProxyVL.createListFromLocalsStrings(simpleValues, allValues);
    }
    
    /**
     * Gets variable type.
     * Returns empty string if type is not available.
     *
     * @param expression a variable name or expression
     * @return null if action is accepted, otherwise return error message
     */
    public String getVariableType(String expression) {
        return gdbProxyCL.getVariableType(expression);
    }
    
    /**
     * Gets variable's number of children.
     * Returns empty string if number of children is not available.
     *
     * @param expression a variable name or expression
     * @return number of children as a string
     */
    public String getVariableNumChild(String expression) {
        return gdbProxyCL.getVariableNumChild(expression);
    }
    
    /**
     * Loads the program
     *
     * @param program - a name of an external program to debug
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String file_exec_and_symbols(String program) {
        this.program = program;
        return gdbProxyCL.file_exec_and_symbols(program);
    }
    
    /**
     *  Do a "set environment" gdb command.
     *
     *  @param var Variable of the form "foo=value"
     */
    public String gdb_set_environment(String var) {
        return gdbProxyCL.gdb_set_environment(var);
    }
    
    /**
     * Runs the program
     *
     * @param programParameters - command line options for the program
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String exec_run(String programParameters) {
        String reply;
        reply = gdbProxyCL.exec_run(programParameters);
        return (reply);
    }
    
    /**
     * Runs the program
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String exec_run() {
        String reply;
        reply = gdbProxyCL.exec_run();
        return (reply);
    }
    
    /**
     * Resumes execution of the inferior program, until a
     * breakpoint is encountered, or until the inferior exits.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String exec_continue() {
        String reply;
        reply = gdbProxyCL.exec_continue();
        return (reply);
    }
    
    /**
     * Interrupts execution of the inferior program.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String exec_interrupt() {
        String reply;
        reply = gdbProxyCL.exec_interrupt();
        return (reply);
    }
    
    /**
     * Resumes execution of the inferior program, stopping
     * when the beginning of the next source line is reached.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String exec_next() {
        String reply;
        reply = gdbProxyCL.exec_next();
        return (reply);
    }
    
    /**
     * Resumes execution of the inferior program, stopping when the beginning of the
     * next source line is reached, if the next source line is not a function call.
     * If it is, stop at the first instruction of the called function.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String exec_step() {
        String reply;
        reply = gdbProxyCL.exec_step();
        return (reply);
    }
    
    /**
     * Resumes the execution of the inferior program until the current
     * function is exited.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String exec_finish() {
        return gdbProxyCL.exec_finish();
    }
    
    /**
     * Inserts a regular breakpoint in all functions
     * whose names match the given name.
     *
     * @param name - function name
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String break_insert(int id, String name) {
        return gdbProxyCL.break_insert(0, id, name);
    }
    
    /**
     * Inserts a regular breakpoint in all functions
     * whose names match the given name.
     *
     * @param name - flags (Currently GDB_TMP_BREAKPOINT or 0)
     * @param name - function name
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String break_insert(int flags, int id, String name) {
        return gdbProxyCL.break_insert(flags, id, name);
    }
    
    /**
     * Deletes the breakpoints
     * whose number(s) are specified in the argument list.
     *
     * @param number - breakpoint's number
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String break_delete(int number) {
        String reply;
        reply = gdbProxyCL.break_delete(number);
        return reply;
    }
    
    /**
     * Enable the breakpoint.
     *
     * @param bpnum The breakpoint number (from gdb)
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String break_enable(int bpnum) {
        return gdbProxyCL.break_enable(bpnum);
    }
    
    /**
     * Disable the breakpoint.
     *
     * @param bpnum The breakpoint number (from gdb)
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String break_disable(int bpnum) {
        return gdbProxyCL.break_disable(bpnum);
    }
    
    /**
     * Deletes a previously created variable object and all of its children. 
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String var_create(String name, String frame, String expression) {
        return gdbProxyCL.var_create(name, frame, expression);
    }
    
    /**
     * Deletes a previously created variable object and all of its children. 
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String var_delete(String name) {
        return gdbProxyCL.var_delete(name);
    }
    
    /**
     * Assigns the value of expression to the variable object specified by name. 
     * The object must be `editable'. If the variable's value is altered by the 
     * assign, the variable will show up in any subsequent -var-update list.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String var_assign(String name, String expression) {
        return gdbProxyCL.var_assign(name, expression);
    }
    
    /**
     * Evaluate variable.
     *
     * @param name A variable name
     *
     * @return value as String value="...", otherwise return error message
     */
    public String var_evaluate(String name) {
        return gdbProxyCL.var_evaluate(name);
    }

    /**
     * Evaluates the expression and returns its value as a string.
     * The expression could contain an inferior function call. 
     * The function call will execute synchronously. 
     * If the expression contains spaces, it must be enclosed in double quotes.
     *
     * @param expr An expression
     *
     * @return null if value is not received, otherwise return value
     */
    public String data_evaluate(String expr) {
        return gdbProxyCL.data_evaluate(expr);
    }
    
    /**
     * Display the local variable names for the selected frame.
     * If print-values is 0 or --no-values, print only the names of the variables;
     * if it is 1 or --all-values, print also their values; and if it is 2 or --simple-values,
     * print the name, type and value for simple data types and the name and type for arrays,
     * structures and unions. In this last case, a frontend can immediately display the value
     * of simple data types and create variable objects for other data types when the the user
     * wishes to explore their values in more detail.
     *
     * @param printValues defines output format
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String stack_list_locals(String printValues) {
        String reply;
        reply = gdbProxyCL.stack_list_locals(printValues);
        return reply;
    }
    
    /**
     * Request a stack dump from gdb.
     */
    public String stack_list_frames() {
        String reply;
        reply = gdbProxyCL.stack_list_frames();
        return reply;
    }
    
    /**
     * Select the specified frame on the stack.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String stack_select_frame(String frameNumber) {
        return gdbProxyCL.stack_select_frame(frameNumber);
    }
    
    /**
     * Sends request to get local variables for specified frame
     *
     * @param frameNumber Frame number in the Call Stack.
     */
    public String updateLocalVariables(String frameNumber) {
        return gdbProxyCL.updateLocalVariables(frameNumber);
    }
    
    /**
     * Finish debugging
     * This action interrupts the execution of the inferior program,
     * and tells debugger to exit inferior program, and to exit debugger.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public String finishDebugging() {
        return gdbProxyCL.finishDebugging();
    }

    /**
     *  Ask gdb about threads. We don't really care about the threads, but it also returns
     *  the process ID, which we do care about.
     */
    public String info_threads() {
        return gdbProxyCL.info_threads();
    }

    /**
     *  Ask gdb about /proc info. We don't really care about the /proc, but it also returns
     *  the process ID, which we do care about.
     */
    public String info_proc() {
        return gdbProxyCL.info_proc();
    }

    /**
     *  Use this to call _CndSigInit() to initialize signals in Cygwin processes.
     */
    public void data_evaluate_expression(String string) {
        gdbProxyCL.data_evaluate_expression(string);
    }
    
} /* End of public class GdbProxy */
