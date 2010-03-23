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

/*
 * GdbProxy.java
 *
 * Note: For a description of the current state of quoting and related topics, see
 * http://sourceware.org/ml/gdb/2006-02/msg00283.html.
 *
 * @author Nik Molchanov and Gordon Prieur
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.openide.util.Utilities;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.common.breakpoints.CndBreakpoint;

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
public class GdbProxy {
    private static final boolean GDBINIT = Boolean.getBoolean("gdb.init.enable"); // NOI18N

    private final GdbDebugger debugger;
    private final GdbProxyEngine engine;
    private final GdbLogger gdbLogger;
    
    private final Map<Integer, CommandBuffer> map = Collections.synchronizedMap(new HashMap<Integer, CommandBuffer>());

    /**
     * Creates a new instance of GdbProxy
     *
     * @param debugger The GdbDebugger
     * @param debuggerCommand The gdb command to use
     * @param debuggerEnvironment The overrides to the user's environment
     * @param workingDirectory The directory to start the debugger from
     * @throws IOException Pass this on to the caller
     */
    public GdbProxy(GdbDebugger debugger, String debuggerCommand, String[] debuggerEnvironment,
            String workingDirectory, String tty, String cspath) throws IOException {
        this.debugger = debugger;

        ArrayList<String> dc = new ArrayList<String>();
        dc.add(debuggerCommand);
        if (!GDBINIT) {
            dc.add("-nx"); // NOI18N
        }
        dc.add("--nw"); // NOI18N
        dc.add("--silent"); // NOI18N
        dc.add("--interpreter=mi"); // NOI18N
        gdbLogger = new GdbLogger(debugger, this);
        engine = new GdbProxyEngine(debugger, this, dc, debuggerEnvironment, workingDirectory, tty, cspath);
    }

    public GdbProxyEngine getProxyEngine() {
        return engine;
    }

    public GdbLogger getLogger() {
        return gdbLogger;
    }
    
    public CommandBuffer getCommandBuffer(Integer id) {
        return map.get(id);
    }
    
    public void removeCB(int id) {
        map.remove(id);
    }
    
    public void putCB(int id, CommandBuffer cb) {
        map.put(id, cb);
    }

    /**
     * Load the program
     *
     * @param program - a name of an external program to debug
     */
    public void file_exec_and_symbols(String programName) {
        engine.sendCommand("-file-exec-and-symbols \"" + programName + '"'); // NOI18N
    }
    
    public void addSymbolFile(String path, String addr) {
        engine.sendCommand("add-symbol-file \"" + path + "\" " + addr); // NOI18N
    }

    public CommandBuffer core(String core) {
//        return engine.sendCommand("-target-attach " + pid); // NOI18N - no implementaion
        return engine.sendCommandEx("core " + core); // NOI18N
    }
    
    /** Attach to a running program */
    public CommandBuffer attach(String pid) {
//        return engine.sendCommand("-target-attach " + pid); // NOI18N - no implementaion
        return engine.sendCommandEx("attach " + pid); // NOI18N
    }

    /** Attach to a running remote program */
    public CommandBuffer attachRemote(String target) {
        // TODO: We may consider using -target-select remote ... (MI style)
        return engine.sendCommandEx("target remote " + target); // NOI18N
    }
    
    /** Detach from a running program */
    public void target_detach() {
//        return engine.sendCommand("-target-detach"); // NOI18N - no implementaion
        engine.sendCommand("detach"); // NOI18N
    }

    /**
     * Load the symbol table only. Used to get symbols for an attached program.
     *
     * @param program - a name of an external program to debug
     */
    public void file_symbol_file(String path) {
        engine.sendCommand("-file-symbol-file \"" + path + '"'); // NOI18N
    }

    /** Ask gdb for its version */
    public CommandBuffer gdb_version() {
        return engine.sendCommandEx("-gdb-version"); // NOI18N
    }

    /** Ask gdb about a variable (currently used to find the current language) */
    public void gdb_show(String arg) {
        engine.sendCommand("-gdb-show " + arg); // NOI18N
    }

    /**
     * Set the runtime directory. Note that this method may get called before we have
     * gdb's version. Thats why we check that its greater than 6.3. This way, if we
     * don't have the version we fallback to the non-mi command.
     *
     * @param path The directory we want to run from
     */
    public void environment_cd(String dir) {
        engine.sendCommand(debugger.getVersionPeculiarity().environmentCdCommand() +
                " \"" + dir + '"'); // NOI18N
    }

    /**
     * Set the runtime directory. Note that this method may get called before we have
     * gdb's version. Thats why we check that its greater than 6.3. This way, if we
     * don't have the version we fallback to the non-mi command.
     *
     * @param path The directory we want to run from
     */
    public void environment_directory(String dir) {
        environment_directory(Collections.singletonList(dir));
    }

    /**
     * Set the runtime directory. Note that this method may get called before we have
     * gdb's version. Thats why we check that its greater than 6.3. This way, if we
     * don't have the version we fallback to the non-mi command.
     *
     * @param path The directory we want to run from
     */
    public void environment_directory(List<String> dirs) {
        assert !dirs.isEmpty();
        StringBuilder cmd = new StringBuilder();
        
        cmd.append(debugger.getVersionPeculiarity().environmentDirectoryCommand());
        
        for (String dir : dirs) {
            cmd.append(" \""); // NOI18N
            cmd.append(dir);
            cmd.append("\""); // NOI18N
        }
        engine.sendCommand(cmd.toString());
    }

    /**
     *  Ask gdb about threads. We don't really care about the threads, but it also returns
     *  the process ID, which we do care about.
     *
     *  Note: In gdb 6.5.50 the -threads-list-all-threads command isn't implemented so we
     *  revert to the gdb command "info threads".
     */
    public CommandBuffer info_threads() {
        return engine.sendCommandEx("info threads"); // NOI18N;
    }
   
    public CommandBuffer info_files() {
        return engine.sendCommandEx("info files"); // NOI18N
    }

    /** Set the current thread */
    public void thread_select(String id) {
        engine.sendCommand("-thread-select " + id); // NOI18N
    }

    /**
     *  Ask gdb about /proc info. We don't really care about the /proc, but it also returns
     *  the process ID, which we do care about.
     */
    public void info_proc() {
        engine.sendCommand("info proc"); // NOI18N
    }
    
    public CommandBuffer info_share(boolean waitForCompletion) {
        return engine.sendCommandEx("info share", waitForCompletion); // NOI18N
    }

    /**
     *  Use this to call _CndSigInit() to initialize signals in Cygwin processes.
     */
    public CommandBuffer data_evaluate_expressionEx(String string) {
        return engine.sendCommandEx("-data-evaluate-expression " + string); // NOI18N
    }

    /**
     */
    public void data_list_register_names(String regIds) {
        engine.sendCommand("-data-list-register-names " + regIds); // NOI18N
    }
    
    /**
     */
    public void data_list_register_values(String regIds) {
        engine.sendCommand("-data-list-register-values x " + regIds); // NOI18N
    }
    
    /**
     */
    public void data_list_changed_registers() {
        engine.sendCommand("-data-list-changed-registers"); // NOI18N
    }
    
    /*
     * @param filename - source file to disassemble
     */
    public void data_disassemble(String filename, int line, boolean withSource) {
        int src = withSource ? 1 : 0;
        engine.sendCommand("-data-disassemble -f \"" + filename + "\" -l " + line + " -- " + src); // NOI18N
    }
    
    /*
     * @param size - size in bytes
     */
    public void data_disassemble(int size, boolean withSource) {
        int src = withSource ? 1 : 0;
        engine.sendCommand("-data-disassemble -s $pc -e \"$pc+" + size + "\" -- " + src); // NOI18N
    }

    public static final int MEMORY_READ_WIDTH = 16;

    /*
     * @param addr - address to read from
     */
    public CommandBuffer data_read_memory(String addr, int lines) {
        return engine.sendCommandEx("-data-read-memory " + addr + " x 1 " + lines + " " + MEMORY_READ_WIDTH + " ."); // NOI18N
    }
    
    public CommandBuffer print(String expression) {
        return engine.sendCommandEx("print " + expression); // NOI18N
    }

    /**
     * Send "-file-list-exec-source-file" to the debugger
     * This command lists the line number, the current source file,
     * and the absolute path to the current source file for the current executable.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public CommandBuffer file_list_exec_source_file() {
        return engine.sendCommandEx("-file-list-exec-source-file"); // NOI18N
    }

    /**
     * Send "-exec-run" with parameters to the debugger
     * This command starts execution of the inferior from the beginning.
     * The inferior executes until either a breakpoint is encountered or
     * the program exits.
     *
     * @param programParameters - command line options for the program
     */
    public CommandBuffer exec_run(String programParameters) {
        return engine.sendCommandEx("-exec-run " + programParameters); // NOI18N
    }

    /**
     * Send "-exec-run" to the debugger
     * This command starts execution of the inferior from the beginning.
     * The inferior executes until either a breakpoint is encountered or
     * the program exits.
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
     */
    public void exec_step() {
        debugger.setLastGo(GdbDebugger.LastGoState.STEP);
        engine.sendCommand("-exec-step"); // NOI18N
    }

    /**
     * Send "-exec-next" to the debugger (go to the next source line)
     * This command resumes execution of the inferior program, stopping
     * when the beginning of the next source line is reached.
     */
    public void exec_next() {
        debugger.setLastGo(GdbDebugger.LastGoState.NEXT);
        engine.sendCommand("-exec-next"); // NOI18N
    }
    
    /**
     * Execute single instruction
     */
    public void exec_step_instruction() {
        // TODO: don't we need to set last go state here?
        engine.sendCommand("-exec-step-instruction"); // NOI18N
    }

    /**
     * Execute next instruction
     */
    public void exec_next_instruction() {
        // TODO: don't we need to set last go state here?
        engine.sendCommand("-exec-next-instruction"); // NOI18N
    }

    /**
     * Send "-exec-finish" to the debugger (finish this function)
     * This command resumes execution of the inferior program until
     * the current function is exited.
     */
    public void exec_finish() {
        debugger.setLastGo(GdbDebugger.LastGoState.FINISH);
        engine.sendCommand("-exec-finish"); // NOI18N
    }

    /**
     * Send "-exec-continue" to the debugger
     * This command resumes execution of the inferior program, until a
     * breakpoint is encountered, or until the inferior exits.
     */
    public void exec_continue() {
        debugger.setLastGo(GdbDebugger.LastGoState.CONTINUE);
        engine.sendCommand("-exec-continue"); // NOI18N
    }

    /**
     * Send "-exec-abort" to the debugger
     * This command kills the inferior program.
     */
    public void exec_abort() {
        engine.sendCommand(debugger.getVersionPeculiarity().execAbortCommand());
    }
    

    /**
     * Send "-break-insert function" to the debugger
     * This command inserts a regular breakpoint in all functions
     * whose names match the given name.
     *
     * @param flags One or more flags aout this breakpoint
     * @param name A function name
     * @param threadID The thread number for this breakpoint
     * @return token number
     */
    public MICommand break_insertCMD(int flags, boolean temporary, String name, String threadID) {
        StringBuilder cmd = new StringBuilder();

        cmd.append("-break-insert "); // NOI18N
        if (temporary) {
            cmd.append("-t "); // NOI18N
        }
        // This will make pending breakpoint if specified location can not be parsed now
        cmd.append(debugger.getVersionPeculiarity().breakPendingFlag());

        // Temporary fix for Windows
        if (Utilities.isWindows() && name.indexOf('/') == 0 && name.indexOf(':') == 2) {
            // Remove first slash
            name = name.substring(1);
        } else if (debugger.getPlatform() == PlatformTypes.PLATFORM_MACOSX) {
            cmd.append("-l 1 "); // NOI18N - Always use 1st choice
        }
        if (flags == CndBreakpoint.SUSPEND_THREAD) {
            // FIXME - Does the Mac support -p?
            cmd.append("-p " + threadID + " "); // NOI18N
        }
        cmd.append('\"'); // NOI18N
        cmd.append(name);
        cmd.append('\"'); // NOI18N
        return engine.createMICommand(cmd.toString());
    }

    /**
     * Send "-break-insert function" to the debugger
     * This command inserts a regular breakpoint in all functions
     * whose names match the given name.
     *
     * @param name The function name or linenumber information
     * @return token number
     */
    public void break_insert(String name) {
        break_insertCMD(0, false, name, null).send();
    }

    /**
     * Insert temporary breakpoint
     */
    public void break_insert_temporary(String name) {
        break_insertCMD(0, true, name, null).send();
    }

    public CommandBuffer break_insert_temporaryEx(String name) {
        return engine.sendCommandEx(break_insertCMD(0, true, name, null).getText());
    }

    /**
     * Send "-break-delete number" to the debugger
     * This command deletes the breakpoints
     * whose number(s) are specified in the argument list.
     *
     * @param number - breakpoint's number
     */
    public MICommand break_deleteCMD(Object number) {
        return engine.createMICommand("-break-delete " + number); // NOI18N
    }

    /**
     * Send "-break-enable number" to the debugger
     * This command enables the breakpoint
     * whose number is specified by the argument
     * or all if no args specified
     *
     * @param ids - breakpoints number array
     */
    public MICommand break_enableCMD(Integer... ids) {
        StringBuilder cmd = new StringBuilder("-break-enable"); // NOI18N
        for (int id : ids) {
            cmd.append(' ');
            cmd.append(id);
        }
        return engine.createMICommand(cmd.toString());
    }

    /**
     * Send "-break-disable number" to the debugger
     * This command disables the breakpoint
     * whose number is specified by the argument
     * or all if no args specified
     *
     * @param ids - breakpoints number array
     */
    public MICommand break_disableCMD(Integer... ids) {
        StringBuilder cmd = new StringBuilder("-break-disable"); // NOI18N
        for (int id : ids) {
            cmd.append(' ');
            cmd.append(id);
        }
        return engine.createMICommand(cmd.toString());
    }
    
    public MICommand break_conditionCMD(int number, String condition) {
        return engine.createMICommand("-break-condition " + Integer.toString(number) + " " + condition); // NOI18N
    }
    
    public MICommand break_afterCMD(int number, int count) {
        return engine.createMICommand("-break-after " + Integer.toString(number) + " " + Integer.toString(count)); // NOI18N
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
        engine.sendCommand("-stack-list-locals " + printValues); // NOI18N
    }

    public void stack_list_arguments(int showValues, int low, int high) {
        engine.sendCommand("-stack-list-arguments " + showValues + " " + low + " " + high); // NOI18N
    }

    public void stack_list_arguments(int showValues) {
        engine.sendCommand("-stack-list-arguments " + showValues); // NOI18N
    }

    /**
     * Send "-stack-select-frame frameNumber" to the debugger
     * This command tells gdb to change the current frame.
     * Select a different frame frameNumber on the stack.
     */
    public void stack_select_frame(int frameNumber) {
        engine.sendCommand("-stack-select-frame " + frameNumber); // NOI18N
    }

    public void up_silently(int number) {
        engine.sendCommand("up-silently " + number); // NOI18N
    }

    /**
     * Send "-stack-info-frame " to the debugger
     * This command asks gdb to provide information about current frame.
     */
    public void stack_info_frame() {
        engine.sendCommand("-stack-info-frame "); // NOI18N
    }

    /** Request a stack dump from gdb */
    public void stack_list_frames() {
        engine.sendCommand("-stack-list-frames "); // NOI18N
    }

    /** Request a stack dump from gdb */
    public CommandBuffer stack_list_framesEx() {
        return engine.sendCommandEx("-stack-list-frames "); // NOI18N
    }
    
    public void gdb_set(String command, Object value) {
        StringBuilder sb = new StringBuilder();
        sb.append("-gdb-set "); // NOI18N
        sb.append(command);
        sb.append(' ');
        sb.append(value);
        engine.sendCommand(sb.toString());
    }

    /**
     * Send "set new-console" to the debugger
     * This command tells gdb to execute inferior program with console.
     */
    public void set_new_console() {
        engine.sendCommand("set new-console"); // NOI18N
    }

    /**
     * Send "set new-console" to the debugger
     * This command tells gdb to execute inferior program with console.
     */
    public void set_unwindonsignal(String on_off) {
        engine.sendCommand("set unwindonsignal " + on_off); // NOI18N
    }

    /**
     * Request the type of a symbol. As of gdb 6.6, this is unimplemented so we send a
     * non-mi command "ptype". We should only be called when symbol is in scope.
     */
    public CommandBuffer symbol_type(String symbol) {
        return engine.sendCommandEx("ptype " + symbol); // NOI18N
    }

    /**
     * Request the type of a symbol. As of gdb 6.6, there is no gdb/mi way of doing this
     * so we send a gdb "whatis" command. This is different from -system-type in the case
     * of abstract data structures (structs and classes). Its the same for other types.
     */
    public CommandBuffer whatis(String symbol) {
        return engine.sendCommandEx("whatis " + symbol); // NOI18N
    }

    public static enum HandleAction {
        nostop, stop, print, noprint, pass, nopass
    };

    public void handle(String signal, HandleAction action) {
        engine.sendCommand("handle " + signal + " " + action); // NOI18N
    }

    /**
     * Send "-gdb-exit" to the debugger
     * This command forces gdb to exit immediately.
     */
    public void gdb_exit() {
        engine.sendCommand("-gdb-exit "); // NOI18N
        engine.stopSending();

        // we need to finish all unfinished requests
        synchronized (map) {
            for (CommandBuffer cb : map.values()) {
                cb.error("gdb finished"); //NOI18N
            }
        }
    }
} /* End of public class GdbProxy */
