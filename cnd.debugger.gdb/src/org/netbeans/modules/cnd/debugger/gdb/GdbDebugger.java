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

package org.netbeans.modules.cnd.debugger.gdb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.FunctionBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointEvent;
import org.netbeans.modules.cnd.debugger.gdb.expr.Expression;
import org.netbeans.modules.cnd.debugger.gdb.models.LocalsTreeModel;
import org.netbeans.modules.cnd.debugger.gdb.models.WatchesModel;
import org.netbeans.modules.cnd.debugger.gdb.profiles.GdbProfile;
import org.netbeans.modules.cnd.debugger.gdb.proxy.GdbMiDefinitions;
import org.netbeans.modules.cnd.debugger.gdb.proxy.GdbProxy;
import org.netbeans.modules.cnd.debugger.gdb.timer.GdbTimer;
import org.netbeans.modules.cnd.debugger.gdb.utils.FieldTokenizer;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

/**
 * Represents one GDB debugger session.
 *
 * <br><br>
 * <b>How to obtain it from DebuggerEngine:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    GdbDebugger cndDebugger = (GdbDebugger) debuggerEngine.lookup
 *        (GdbDebugger.class);</pre>
 */
public class GdbDebugger implements PropertyChangeListener, GdbMiDefinitions {

    public static final String          PROP_STATE = "state"; // NOI18N
    public static final String          PROP_CURRENT_THREAD = "currentThread"; // NOI18N
    public static final String          PROP_CURRENT_CALL_STACK_FRAME = "currentCallStackFrame"; // NOI18N
    public static final String          PROP_SUSPEND = "suspend"; // NOI18N
    public static final String          PROP_LOCALS_VIEW_UPDATE = "localsViewUpdate"; // NOI18N

    public static final String          STATE_NONE = "state_none"; // NOI18N
    public static final String          STATE_STARTING = "state_starting"; // NOI18N
    public static final String          STATE_LOADING = "state_loading"; // NOI18N
    public static final String          STATE_READY = "state_ready"; // NOI18N
    public static final String          STATE_RUNNING = "state_running"; // NOI18N
    public static final String          STATE_STOPPED = "state_stopped"; // NOI18N
    public static final String          STATE_SILENT_STOP = "state_silent_stop"; // NOI18N
    public static final String          STATE_EXITED  = "state_exited"; // NOI18N
    
    /* Some breakpoint flags used only on Windows XP (with Cygwin) */
    public static final int             GDB_TMP_BREAKPOINT = 1;
    public static final int             GDB_INVISIBLE_BREAKPOINT = 2;
    
    /** ID of GDB Debugger Engine for C */
    public static final String          ENGINE_ID = "netbeans-cnd-GdbSession/C"; // NOI18N

    /** ID of GDB Debugger Session */
    public static final String          SESSION_ID = "netbeans-cnd-GdbSession"; // NOI18N

    /** ID of GDB Debugger SessionProvider */
    public static final String          SESSION_PROVIDER_ID = "netbeans-cnd-GdbSessionProvider"; // NOI18N
    
    private GdbProxy gdb;
    private ContextProvider lookupProvider;
    private String state = STATE_NONE;
    private PropertyChangeSupport pcs;
    private String runDirectory;
    private ArrayList callstack = new ArrayList();
    private GdbEngineProvider gdbEngineProvider;
    private CallStackFrame currentCallStackFrame;
    private boolean firstStop = true;
    public final Object LOCK = new Object();
    private long programPID = 0;
    private double gdbVersion = 0.0;
    private boolean continueAfterFirstStop = true;
    private ArrayList<GdbVariable> localVariables = new ArrayList();
    private Map<Integer, GdbVariable> symbolCompletionTable = new HashMap();
    private Map<Integer, StringBuilder> typeCompletionTable = new HashMap();
    private Set<String> typePendingTable = new HashSet();
    private Map<Integer, GdbVariable> valueCompletionTable = new HashMap();
    private Logger log = Logger.getLogger("gdb.logger"); // NOI18N
    private int currentToken = 0;
    private int ttToken = 0;
    private ToolTipAnnotation ttAnnotation = null;
    private Timer startupTimer = null;
    private boolean cygwin = false;
    private boolean cplusplus = false;
    private Map<String, Object> stlMap = new HashMap();
        
    public GdbDebugger(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        pcs = new PropertyChangeSupport(this);
        addPropertyChangeListener(this);
        List l = lookupProvider.lookup(null, DebuggerEngineProvider.class);
        int i, k = l.size();
        for (i = 0; i < k; i++) {
            if (l.get(i) instanceof GdbEngineProvider) {
                gdbEngineProvider = (GdbEngineProvider) l.get(i);
            }
        }
        if (gdbEngineProvider == null) {
            throw new IllegalArgumentException(
                    "GdbEngineProvider must be used to start GdbDebugger!"); // NOI18N
        }
    }
    
    public ContextProvider getLookup() {
        return lookupProvider;
    }
    
    public void startDebugger() {
        ProjectActionEvent pae;
        GdbProfile profile;
        String termpath = null;
        GdbTimer.getTimer("Startup").start("Startup1"); // NOI18N
        GdbTimer.getTimer("Stop").start("Stop1"); // NOI18N
        
        setStarting();
        try {
            pae = (ProjectActionEvent) lookupProvider.lookupFirst(null, ProjectActionEvent.class);
            runDirectory = pae.getProfile().getRunDirectory().replace("\\", "/") + "/";  // NOI18N
            profile = (GdbProfile) pae.getConfiguration().getAuxObject(GdbProfile.GDB_PROFILE_ID);
            int conType = pae.getProfile().getConsoleType().getValue();
            if (!Utilities.isWindows() && conType != RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW) {
                termpath = pae.getProfile().getTerminalPath();
            }
            startupTimer = new Timer("GDB Startup Timer Thread"); // NOI18N
            startupTimer.schedule(new TimerTask() {
                    public void run() {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                               "ERR_StartupTimeout"))); // NOI18N
                        setExited();
                        finish();
                    }
            }, 30000);
            String gdbCommand = profile.getGdbPath(profile.getGdbCommand(), pae.getProfile().getRunDirectory());
            if (gdbCommand.toLowerCase().contains("cygwin")) { // NOI18N
                cygwin = true;
            }
            gdb = new GdbProxy(this, gdbCommand, pae.getProfile().getEnvironment().getenv(),
                    runDirectory, termpath);
            gdb.gdb_version();
            gdb.file_exec_and_symbols(getProgramName(pae.getExecutable()));
            gdb.environment_cd(getProgramDirectory(pae.getExecutable()));
            gdb.gdb_show("language"); // NOI18N
        
            if (Utilities.isWindows()) {
                if (conType != RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW) {
                    gdb.set_new_console();
                }
                File cnd_dll = InstalledFileLocator.getDefault().locate("bin/cnd.dll", null, false); // NOI18N
                gdb.gdb_set_environment("LD_PRELOAD=" + cygpath(cnd_dll.getAbsolutePath())); // NOI18N
            }
            int id;
            if (pae.getID() == ProjectActionEvent.DEBUG_STEPINTO) {
                continueAfterFirstStop = false; // step into project
            }
            gdb.break_insert(GDB_TMP_BREAKPOINT, "main"); // NOI18N
            setLoading(); // This triggers persistent breakpoint loading
        } catch (Exception ex) {
            if (startupTimer != null) {
                startupTimer.cancel();
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                msg = ex.getMessage();
            }
            if (msg == null || msg.length() == 0) {
                msg = NbBundle.getMessage(GdbDebugger.class, "ERR_UnSpecifiedStartError");
            }
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
            setExited();
            finish();
        }
    }
    
    private String getProgramName(String program) {
        StringBuilder programName = new StringBuilder();
        
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
        return programName.toString();
    }
    
    private String getProgramDirectory(String program) {
        if (Utilities.isWindows()) {
            if (isCygwin()) {
                program = cygpath(program);
            } else if (program.indexOf('\\') != -1) {
                program = convertToWindowsPath(program);
            }
        }
        int i = program.lastIndexOf('/');
        assert i > -1;
        if (i > 0) {
            return program.substring(0, i);
        } else {
            return program;
        }
    }
    
    /** If the type definition contains { ... }, strip it. It will be picked up elsewhere */
    private String stripFields(String info) {
        StringBuilder buf = new StringBuilder();
        int pos1 = info.indexOf('{');
        int pos2 = GdbUtils.findMatchingCurly(info, pos1);
        
        if (pos1 != -1 && pos2 != -1) {
            buf.append(info.substring(0, pos1).trim());
            buf.append(info.substring(pos2).trim());
        } else {
            buf.append(info);
        }
        
        return buf.toString();
    }
    
    private static String convertToWindowsPath(String orig) {
        String nue = orig.replace('\\', '/');
        return orig.replace("\\", "/");
    }
    
    private String cygpath(String path) {
        if (path.charAt(1) == ':') {
            path = "/cygdrive/" + path.substring(0, 1) + path.substring(2); // NOI18N
        }
        return convertToWindowsPath(path);
    }
    
    /** Get the gdb version */
    public double getGdbVersion() {
        return gdbVersion;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == PROP_STATE) {
            if (evt.getNewValue() == STATE_READY) {
                ProjectActionEvent pae = (ProjectActionEvent) lookupProvider.lookupFirst(null, ProjectActionEvent.class);
                try {
                    gdb.exec_run(pae.getProfile().getArgsFlat());
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                    ((Session) lookupProvider.lookupFirst(null, Session.class)).kill();
                }
            } else if (evt.getNewValue() == STATE_STOPPED) {
                updateLocalVariables(0);
                updateWatchesModel();
            } else if (evt.getNewValue() == STATE_SILENT_STOP) {
                interrupt();
            } else if (evt.getNewValue() == STATE_RUNNING && evt.getOldValue() == STATE_SILENT_STOP) {
                gdb.exec_continue();
            } else if (evt.getNewValue() == STATE_RUNNING) {
                synchronized (LOCK) {
                    typeCompletionTable.clear();
                    typePendingTable.clear();
                    symbolCompletionTable.clear();
                }
            } else if (evt.getNewValue() == STATE_EXITED) {
                finish();
            }
        }
    }
    
    public GdbProxy getGdbProxy() {
        return gdb;
    }
    
    /**
     * Finish debugging session. Terminates execution of the inferior program, exits debugger,
     * closes terminal and console.
     * 
     * Note: gdb can be null if we get an exception while starting a debug session.
     */
    public void finish() {
        if (state != STATE_NONE ) {
            if (gdb != null) {
                if (state == STATE_RUNNING) {
                    gdb.exec_interrupt(); // Does this work? (Don't think so)
                    gdb.exec_abort();
                }
                gdb.gdb_exit();
            }

            stackUpdate(new ArrayList());
            setState(STATE_NONE);
            programPID = 0;
            gdbEngineProvider.getDestructor().killEngine();
            GdbTimer.getTimer("Step").reset(); // NOI18N
        }
    }
    
    /**
     * The user has pressed the stop-out button while in the topmost function (main). gdb/mi
     * doesn't allow this and we've received an error. Set a temporary breakpoint in exit and
     * continue to the breakpoint. This will perform the action the user requested.
     */
    private void finish_from_main() {
       gdb.break_insert(GDB_TMP_BREAKPOINT, "exit"); // NOI18N
       gdb.exec_continue();
    }
    
    public long getProcessID() {
        return programPID;
    }
    
    public void unexpectedGdbExit(int rc) {
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
        finish();
    }
    
    /** Sends request to get arguments and local variables */
    private void updateLocalVariables(int frame) {
        synchronized (LOCK) {
            localVariables.clear(); // clear old variables so we can store new ones here
            gdb.stack_select_frame(frame);
            gdb.stack_list_arguments(1, frame, frame);
            gdb.stack_list_locals(ALL_VALUES);
        }
    }
    
    /** Handle geb responses starting with '^' */
    public void resultRecord(int token, String msg) {
        GdbVariable var;
        Integer itok = Integer.valueOf(token);
        
        currentToken = token + 1;
        if (msg.startsWith("^done,bkpt=")) { // NOI18N (-break-insert)
            msg = msg.substring(12, msg.length() - 1);
            breakpointValidation(token, GdbUtils.createMapFromString(msg));
            if (getState() == STATE_SILENT_STOP) {
                setRunning();
            }
        } else if (msg.startsWith("^done,stack=")) { // NOI18N (-stack-list-frames)
            if (state == STATE_STOPPED) { // Ignore data if we've resumed running
                stackUpdate(GdbUtils.createListFromString((msg.substring(13, msg.length() - 1))));
            }
        } else if (msg.startsWith("^done,locals=")) { // NOI18N (-stack-list-locals)
            if (state == STATE_STOPPED) { // Ignore data if we've resumed running
                addLocalsToLocalVariables(msg.substring(13));
                completeLocalVariables();
            }
        } else if (msg.startsWith("^done,stack-args=")) { // NOI18N (-stack-list-arguments)
            if (state == STATE_STOPPED) { // Ignore data if we've resumed running
                addArgsToLocalVariables(msg.substring(17));
            }
        } else if (msg.startsWith("^done,value=") && msg.contains("auto; currently c++")) { // NOI18N
            cplusplus = true;
        } else if (msg.startsWith("^done,value=")) { // NOI18N (-data-evaluate-expression)
            if (token == ttToken) {
                ttAnnotation.postToolTip(msg.substring(13, msg.length() - 1));
                ttToken = 0;
            } else if ((var = valueCompletionTable.remove(itok)) != null) {
                if (GdbUtils.isPointer(var.getType())) {
                    if (var.getType().replace(" ", "").indexOf("**") != -1) { // NOI18N
                        String val = msg.substring(13, msg.length() - 1);
                        List<GdbVariable> children = var.getChildren();
                        if (!val.equals("0x0")) { // NOI18N
                            int size = children.size();
                            children.add(new GdbVariable(var.getName() + "[" + size + "]", // NOI18N
                                    GdbUtils.getBaseType(var.getType()) + " *", val)); // NOI18N
                            int tok = gdb.data_evaluate_expression(var.getName() + '[' + ++size + ']');
                            valueCompletionTable.put(Integer.valueOf(tok), var);
                        }
                    } else {
                        var.setDerefedValue(msg.substring(13, msg.length() - 1));
                    }
                } else {
                    var.setValue(msg.substring(13, msg.length() - 1));
                }
            }
        } else if (msg.equals("^done") && getState() == STATE_SILENT_STOP) { // NOI18N
            setRunning();
        } else if (msg.equals("^done")) { // NOI18N
            StringBuilder typebuf;
            if ((typebuf = typeCompletionTable.get(itok)) != null) { // complete response of ptype command
                String tbuf = typebuf.toString();
                int pos = tbuf.indexOf('='); // its guaranteed to have '='
                String type = tbuf.substring(0, pos);
                if (tbuf.equals("string=int (void)")) { // NOI18N
                    // work-around for gdb bug (in *all* versions)
                    addTypeCompletion("std::basic_string<char,std::char_traits<char>,std::allocator<char> >"); // NOI18N
                    getCurrentCallStackFrame().addType("string", "std::basic_string<char,std::char_traits<char>,std::allocator<char> >"); // NOI18N
                } else if (tbuf.endsWith("]")) { // NOI18N
                    getCurrentCallStackFrame().addType(type, stripFields(tbuf.substring(pos + 1)));
                } else if (tbuf.indexOf('{') == -1) { // NOI18N
                    getCurrentCallStackFrame().addType(type, tbuf.substring(pos + 1));
                } else {
                    Map map = getFieldMap(tbuf.substring(pos + 1));
                    getCurrentCallStackFrame().addType(type, map);
                    if (map.size() > 0) { // NOI18N
                        checkForUnknownTypes(map);
                    }
                }
                typePendingTable.remove(type);
                typeCompletionTable.remove(itok);
                
                if (type.indexOf(':') != -1) { // NOI18N
                    checkForSuperClass(tbuf.substring(pos + 1));
                }
                firePropertyChange(PROP_LOCALS_VIEW_UPDATE, 0, 1);
            }
        } else if (msg.startsWith("^running")) { // NOI18N
            setRunning();
        } else if (msg.startsWith("^error,msg=")) { // NOI18N
            msg = msg.substring(11);
            
            if (typeCompletionTable.get(itok) != null) {
                typeCompletionTable.remove(itok);
                typePendingTable.remove(itok);
            } else if (valueCompletionTable.get(itok) != null) {
                valueCompletionTable.remove(itok);
            }
            if (token == ttToken) { // invalid tooltip request
                ttAnnotation.postToolTip(" >" + msg.substring(1, msg.length() - 1) + "<"); // NOI18N
                ttToken = 0;
            } else if (msg.startsWith("\"No symbol ") && msg.endsWith(" in current context.\"")) { // NOI18N
                String type = msg.substring(13, msg.length() - 23);
                if (type.equals("string") || type.equals("std::string")) {
                    // work-around for gdb bug (in *all* versions)
                    addTypeCompletion("std::basic_string<char,std::char_traits<char>,std::allocator<char> >"); // NOI18N
                    getCurrentCallStackFrame().addType("string", "std::basic_string<char,std::char_traits<char>,std::allocator<char> >"); // NOI18N
                } else {
                    log.warning("Failed type lookup for " + type);
                }
            } else if (msg.equals("\"\\\"finish\\\" not meaningful in the outermost frame.\"")) { // NOI18N
                finish_from_main();
            } else if (msg.contains("(corrupt stack?)")) { // NOI18N
                DialogDisplayer.getDefault().notify(
                       new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                       "ERR_CorruptedStack"))); // NOI18N
                ((Session) lookupProvider.lookupFirst(null, Session.class)).kill();
            } else if (msg.contains("error reading line numbers")) { // NOI18N
                DialogDisplayer.getDefault().notify(
                       new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                       "ERR_CantReadLineNumbers"))); // NOI18N
            } else if (msg.contains("No symbol table is loaded")) { // NOI18N
                DialogDisplayer.getDefault().notify(
                       new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                       "ERR_NoSymbolTable"))); // NOI18N
                ((Session) lookupProvider.lookupFirst(null, Session.class)).kill();
            } else if (msg.contains("Cannot access memory at address")) { // NOI18N
                // ignore - probably dereferencing an uninitialized pointer
            } else if (state != STATE_NONE) {
                // ignore errors after we've terminated (they could have been in the input queue)
                log.warning("Unexpected gdb error: " + msg);
            }
        }
    }
    
    /** Handle geb responses starting with '*' */
    public void execAsyncOutput(int token, String msg) {
        if (msg.startsWith("*stopped")) { // NOI18N
            Map<String, String> map = GdbUtils.createMapFromString(msg.substring(9));
            if (map.get("reason") == null && firstStop) { // NOI18N
                firstStop(); // temporary breakpoint from initial -exec-run command
            } else {
                if (firstStop) { // got a user-set breakpoint first
                    continueAfterFirstStop = false; // never continues after this first stop
                    firstStop();
                }
                stopped(token, map);
            }
        }
    }
    
    /** Handle gdb responses starting with '+' */
    public void statusAsyncOutput(int token, String msg) {
      log.fine("GDI.statusAsyncOutput[" + token + "]: " + msg); // NOI18N
    }
    
    /** Handle gdb responses starting with '=' */
    public void notifyAsyncOutput(int token, String msg) {
        log.fine("GDI.notifyAsyncOutput[" + token + "]: " + msg); // NOI18N
    }
    
    /** Handle gdb responses starting with '~' */
    public void consoleStreamOutput(int token, String msg) {
        StringBuilder typebuf;
        GdbVariable var;
        String type;
        
        if (msg.endsWith("\\n")) { // NOI18N
            msg = msg.substring(0, msg.length() - 2);
        }
        if (msg.startsWith("type = ")) { // NOI18N
            if ((var = symbolCompletionTable.remove(Integer.valueOf(token))) != null) { // whatis
                type = msg.substring(7);
                var.setType(type);
                if (!GdbUtils.isSimple(type) && !GdbUtils.isSimplePointer(type)) {
                    if (type.endsWith("{...}")) { // NOI18N
                        addTypeCompletion('$' + var.getName()); // unnamed type
                    } else {
                        addTypeCompletion(type);
                    }
                }
            } else if ((typebuf = typeCompletionTable.get(Integer.valueOf(token))) != null) { // ptype
                typebuf.append(msg.substring(7));
            }
        } else if ((typebuf = typeCompletionTable.get(Integer.valueOf(token))) != null) {
            typebuf.append(msg);
        } else if (gdbVersion < 1.0 && msg.startsWith("GNU gdb ")) { // NOI18N
            // Cancel the startup timer - we've got our first response from gdb
            if (startupTimer != null) {
                startupTimer.cancel();
                startupTimer = null;
            }
            
            // Now process the version information
            int first = msg.indexOf('.');
            int last = msg.lastIndexOf('.');
            try {
                if (first == last) {
                    gdbVersion = Double.parseDouble(msg.substring(8));
                } else {
                    gdbVersion = Double.parseDouble(msg.substring(8, last));
                }
            } catch (NumberFormatException ex) {
            }
            if (msg.contains("cygwin")) { // NOI18N
                cygwin = true;
            }
        } else if (msg.startsWith("Copyright ") || // NOI18N
                msg.startsWith("GDB is free software,") || // NOI18N
                msg.startsWith("welcome to change it and") || // NOI18N
                msg.contains("show copying") || // NOI18N
                msg.startsWith("There is absolutely no warranty for GDB") || // NOI18N
                msg.startsWith("Source directories searched: ") || // NOI18N
                msg.startsWith("This GDB was configured as")) { // NOI18N
            ; // do nothing (but don't print these expected messages)
        } else if (programPID == 0 && msg.startsWith("process ")) { // NOI18N (Unix method)
            int pos = msg.indexOf(' ', 8);
            if (pos > 0) {
                try {
                    programPID = Long.parseLong(msg.substring(8, pos));
                } catch (NumberFormatException ex) {
                }
            }
        } else if (programPID == 0) {
            if (msg.startsWith("* 1 thread ")) { // NOI18N
                int pos = msg.indexOf('.');
                if (pos > 0) {
                    try {
                        programPID = Long.valueOf(msg.substring(11, pos));
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    
    /** Handle gdb responses starting with '@' */
    public void targetStreamOutput(String msg) {
       log.fine("GDI.targetStreamOutput: " + msg);  // NOI18N
    }
    
    /** Handle gdb responses starting with '&' */
    public void logStreamOutput(String msg) {
        if (msg.startsWith("&\"info proc") || // NOI18N
                msg.startsWith("&\"info threads") || // NOI18N
                msg.startsWith("&\"directory ") || // NOI18N
                msg.startsWith("&\"set new-console") || // NOI18N
                msg.startsWith("&\"warning: Temporarily disabling breakpoints for unloaded shared library") || // NOI18N
                msg.contains("/usr/lib/ld.so")) { // NOI18N
            ; // ignore these messages
        } else {
            log.fine("GDI.logStreamOutput: " + msg); // NOI18N
        }
    }
    
    private Map getFieldMap(String info) {
        HashMap map = new HashMap();
        int pos1 = info.indexOf('{');
        int pos2 = info.indexOf('}');
        String fields = null;
        
        if (pos1 == -1 && pos2 == -1) {
            if (GdbUtils.isPointer(info)) {
                info = info.replace('*', ' ').trim();
            }
            Object o = getCurrentCallStackFrame().getType(info);
            if (o != null) { // t can be null if stepping into a macro in a header file
                String t = o.toString();
                pos1 = t.indexOf('{');
                pos2 = t.indexOf('}');
                if (pos1 != -1 && pos2 != -1) {
                    fields = t.substring(pos1 + 1, pos2);
                }
            }
        } else if (pos1 != -1 && pos2 != -1) {
            fields = info.substring(pos1 + 1, pos2);
        }
        if (fields != null) {
            FieldTokenizer tok = new FieldTokenizer(fields);
            while (tok.hasMoreFields()) {
                String[] field = tok.nextField();
                pos1 = field[1].indexOf('[');
                if (pos1 == -1) {
                    map.put(field[1], field[0]);
                } else {
                    map.put(field[1].substring(0, pos1), field[0] + field[1].substring(pos1));
                }
            }
            if (map.isEmpty()) {
                map.put("<" + info.substring(0, pos1) + ">", "<no data fields>");
            }
        }
        return map;
    }
    
    /** Parse a substring from a ptype class response to see if we have any superclasses */
    private void checkForSuperClass(String info) {
        boolean hasSuperClass = false;
        int pos;
        char c;
        
        for (int i = 0; i < info.length(); i++) {
            c = info.charAt(i);
            if (c == '<') {
                pos = GdbUtils.findMatchingLtGt(info, i);
                if (pos != -1) {
                    i = pos - 1;
                }
            } else if (c == ':') {
                if (info.charAt(i + 1) == ':') { // Got ::
                    i++;
                } else {
                    pos = info.indexOf('{', i);
                    if (pos != -1) {
                        info = info.substring(i + 1, pos).trim();
                        hasSuperClass = true;
                    } else {
                        return; // invalid info (possible gdb error?)
                    }
                    break;
                }
            } else if (c == '{') {
                return; // no superclass
            }
        }
        
        if (hasSuperClass) {
            if (info.startsWith("public ")) {
                info = info.substring(7);
            } else if (info.startsWith("private ")) {
                info = info.substring(8);
            } else if (info.startsWith("protected ")) {
                info = info.substring(10);
            }
            for (int i = 0; i < info.length(); i++) {
                c = info.charAt(i);
                if (c == '<') {
                    pos = GdbUtils.findMatchingLtGt(info, i);
                    if (pos != -1) {
                        i = pos;
                    }
                } else if (c == ',') {
                    addTypeCompletion(info.substring(0, i - 1));
                    info.substring(i + 1);
                }
            }
            addTypeCompletion(info.substring(0));
        }
    }
    
    private void checkForUnknownTypes(Map map) {
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object o = entry.getValue();
            if (o instanceof String) {
                String type = o.toString();
                if (!GdbUtils.isSimple(type) && !type.equals("<no data fields>")) {
                    addTypeCompletion(o.toString());
                }
            }
        }
    }
    
    public void addTypeCompletion(String key) {
        key = trimKey(key);
        assert key != null && key.length() > 0;
        if (!GdbUtils.isSimple(key) && !typePendingTable.contains(key) && getCurrentCallStackFrame().getType(key) == null) {
            int token = gdb.symbol_type(key.replace('$', ' ').trim());
            typeCompletionTable.put(Integer.valueOf(token), new StringBuilder(key + '='));
            typePendingTable.add(key);
        }
    }
    
    public void waitForTypeCompletionCompletion() {
        if (!isTypeCompletionComplete()) {
            if (Thread.currentThread().getName().equals("GdbReaderRP")) { // NOI18N
                log.warning("Attempting to wait for type completion on GDB Reader thread"); // NOI18N
            } else {
                int i = 0;
                while (!isTypeCompletionComplete() && state == STATE_STOPPED && i++ < 40) {
//                    System.err.println("GDI.waitForTypeCompletion: [" +
//                            symbolCompletionTable.size() + ", " +
//                            typeCompletionTable.size() + ", " +
//                            typePendingTable.size() + ", " +
//                            valueCompletionTable.size() + "]"); // NOI18N
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            }
        }
    }
    
    public boolean isTypeCompletionComplete() {
        return typeCompletionTable.isEmpty() && typePendingTable.isEmpty() &&
                valueCompletionTable.isEmpty();
    }
    
    /**
     * Trim off all pointer and array info (including function pointer stuff). If its a Template,
     * ignore the <> parts.
     */
    public String trimKey(String key) {
        int pos, lt;
        char c;
        
        assert key != null;
        for (int i = 0; i < key.length(); i++) {
            c = key.charAt(i);
            switch (key.charAt(i)) {
                case '<':
                    pos = GdbUtils.findMatchingLtGt(key, i);
                    if (pos != -1) {
                        i = pos;
                    }
                    break;
                    
                case '*':
                case '[':
                case '(':
                    String tmp = key.substring(0, i);
                    return key.substring(0, i).trim();
            }
        }
        return key.trim();
    }
    
    private void addArgsToLocalVariables(String info) {
        int pos;
        if (info.startsWith("[frame={level=") && (pos = info.indexOf(",args=[")) > 0 && info.endsWith("]}]")) { // NOI18N
            info = info.substring(pos + 7, info.length() - 3);
        }
        Collection v = GdbUtils.createArgumentList(info);
        if (!v.isEmpty()) {
            localVariables.addAll(v);
        }
    }
    
    private void addLocalsToLocalVariables(String info) {
        Collection v = GdbUtils.createLocalsList(info.substring(1, info.length() - 1));
        if (!v.isEmpty()) {
            localVariables.addAll(v);
        }
    }
    
    private void completeLocalVariables() {
        if (!localVariables.isEmpty()) {
            for (GdbVariable var : localVariables) {
                int token = gdb.whatis(var.getName());
                symbolCompletionTable.put(Integer.valueOf(token), var);
                
                String value = var.getValue();
                if (value.charAt(0) == '(') {
                    int pos = GdbUtils.findMatchingParen(value, 0);
                    if (pos > 0) {
                        String cast = value.substring(0, pos);
                        if (cast.indexOf("*)") > 0) { // NOI18N
                            token = gdb.data_evaluate_expression('*' + var.getName());
                            valueCompletionTable.put(Integer.valueOf(token), var);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * We set a temporary breakpoint at main so we can complete initialization. Some things
     * (like breakpoints) can't be set before this point.
     */
    private void firstStop() {
        firstStop = false;
        if (Utilities.isWindows()) {
            gdb.data_evaluate_expression("_CndSigInit()"); // NOI18N
            gdb.break_insert(GDB_INVISIBLE_BREAKPOINT, "_CndSigHandler"); // NOI18N
            gdb.info_threads(); // we get the PID from this...
        } else {
            gdb.info_proc(); // we get the PID from this...
        }
        
        if (continueAfterFirstStop) {
            gdb.exec_continue();
        } else {
            gdb.stack_list_frames();
            setStopped();
        }
    }
    
    public void completeToolTip(int token, ToolTipAnnotation tt) {
        ttToken = token;
        ttAnnotation = tt;
    }
    
    // currently not called - should do more than set state (see JPDADebuggerImpl)
    public void suspend() {
        setState(STATE_STOPPED);
    }

    /**
     * Interrupts execution of the inferior program. 
     * This method is called when "Pause" button is pressed.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public void interrupt() {
        gdb.exec_interrupt();
    }
    
    /**
     * Send a kill command to the debuggee.
     *
     * @param signal The signal to send (as defined by "kill -l")
     */
    public void kill(int signal) {
        kill(signal, programPID);
    }
    
    /**
     * Send a kill command to the debuggee.
     *
     * @param signal The signal to send (as defined by "kill -l")
     * @param pid The process ID to send the signal to
     */
    public void kill(int signal, long pid) {
        ArrayList<String> killcmd = new ArrayList();
        File f;
        
        if (Utilities.isWindows()) {
            f = new File("C:/Cygwin/bin/kill.exe"); // NOI18N
            if (f.exists()) {
                killcmd.add(f.getAbsolutePath());
            }
        } else {
            f = new File("/usr/bin/kill"); // NOI18N
            if (f.exists()) {
                killcmd.add(f.getAbsolutePath());
            } else {
                f = new File("/bin/kill"); // NOI18N
                if (f.exists()) {
                    killcmd.add(f.getAbsolutePath());
                }
            }
        }
        if (killcmd.size() > 0) {
            killcmd.add("-s"); // NOI18N
            killcmd.add(Integer.toString(signal));
            killcmd.add(Long.toString(pid));
            ProcessBuilder pb = new ProcessBuilder(killcmd);
            try {
                pb.start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            gdb.getLogger().logMessage("External Command: " + killcmd.toString()); // NOI18N
        }
    }
    
    /**
     * Resumes execution of the inferior program, until a
     * breakpoint is encountered, or until the inferior exits.
     */
    public void resume() {
        setState(STATE_RUNNING);
        gdb.exec_continue();
    }
    
    /**
     * Resumes execution of the inferior program, stopping when the beginning of the
     * next source line is reached, if the next source line is not a function call.
     * If it is, stop at the first instruction of the called function.
     */
    public void stepInto() {
        GdbTimer.getTimer("Step").start("Step1", 10); // NOI18N
        setState(STATE_RUNNING);
        gdb.exec_step();
    }
    
    /**
     * Resumes execution of the inferior program, stopping
     * when the beginning of the next source line is reached.
     */
    public void stepOver() {
        setState(STATE_RUNNING);
        gdb.exec_next();
    }
    
    /**
     * Resumes execution of the inferior program until
     * the current function is exited.
     */
    public void stepOut() {
        setState(STATE_RUNNING);
        gdb.exec_finish();
    }
    
    /**
     * Returns current state of gdb debugger.
     *
     * @return current state of gdb debugger
     * @see #STATE_STARTING
     * @see #STATE_RUNNING
     * @see #STATE_STOPPED
     * @see #STATE_DISCONNECTED
     */
    public String getState() {
        return state;
    }
    
    private void setState(String state) {
        if (state == this.state) {
            return;
        }
        String oldState = this.state;
        this.state = state;
        firePropertyChange(PROP_STATE, oldState, state);
    }
    
    public void setStarting() {
        setState(STATE_STARTING);
    }
    
    public void setLoading() {
        setState(STATE_LOADING);
    }
    
    public void setReady() {
        setState(STATE_READY);
    }
    
    public void setRunning() {
        setState(STATE_RUNNING);
    }
    
    public void setStopped() {
        setState(STATE_STOPPED);
    }
    
    public void setSilentStop() {
        setState(STATE_SILENT_STOP);
    }
    
    public void setExited() {
        setState(STATE_EXITED);
    }
    
    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public int getSuspend() {
        return 0;
    }
    
    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public void setSuspend(int s) {
    }
    
    public Boolean evaluateIn(Expression expression, final Object frame) {
        return Boolean.FALSE;
    }
    
    public Variable getVariable(Object value) {
        return null;
    }
    
    /**
     * Helper method that fires JPDABreakpointEvent on JPDABreakpoints.
     *
     * @param breakpoint a breakpoint to be changed
     * @param event a event to be fired
     */
    public void fireBreakpointEvent(GdbBreakpoint breakpoint, GdbBreakpointEvent event) {
        breakpoint.fireGdbBreakpointChange(event);
    }
    
    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *
     * @return current value of given expression
     */
    public Variable evaluate(String expression) throws InvalidExpressionException {
        String sName = expression;
        // Unfortunately type is unknown
        String sType = ""; // NOI18N
        String sValue = gdb.data_evaluate(expression); // FIXME - No longer synchronous...
        LocalVariableImpl lvi =  new LocalVariableImpl(sName, sType, sValue);
        return lvi;
    }
    
    /**
     * Call back method
     * This method is called from GdbProxy when the target debuggee is stopped
     *
     * @param token The token responsible for this stop
     * @param reason A reason why program is stopped
     */
    public void stopped(int token, Map<String, String> map) {
        String reason = map.get("reason"); // NOI18N
        
        if (reason != null) {
            setCurrentCallStackFrameNoFire(null);   // will be reset when stack updates
            if (reason.equals("exited-normally")) { // NOI18N
                setExited();
                finish();
            } else if (reason.equals("breakpoint-hit")) { // NOI18N
                String frame = map.get("frame"); // NOI18N
                gdb.stack_list_frames();
                GdbBreakpoint breakpoint = GdbBreakpoint.get(map.get("bkptno")); // NOI18N
                if (breakpoint != null) {
                    fireBreakpointEvent(breakpoint, new GdbBreakpointEvent(
                            breakpoint, this, GdbBreakpointEvent.CONDITION_NONE, null));
                } else {
                    log.fine("No Breakpoints Found"); // NOI18N
                }
                setStopped();
                GdbTimer.getTimer("Startup").stop("Startup1"); // NOI18N
                GdbTimer.getTimer("Startup").report("Startup1"); // NOI18N
                GdbTimer.getTimer("Startup").free(); // NOI18N
                GdbTimer.getTimer("Stop").mark("Stop1");// NOI18N
            } else if (reason.equals("exited-signalled")) { // NOI18N
                String signal = map.get("signal-name"); // NOI18N
                if (signal != null) {
                    DialogDisplayer.getDefault().notify(
                           new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                           "ERR_ExitedFromSignal", signal))); // NOI18N
                    setExited();
                    finish();
                }
            } else if (reason.equals("end-stepping-range")) { // NOI18N
                String frame = map.get("frame"); // NOI18N
                if (frame == null) {
                    // didn't change method so we get "line" and "file" properties
                    try {
                        int lineNumber = Integer.parseInt(map.get("line")); // NOI18N
                        CallStackFrame csf = getCurrentCallStackFrame();
                        csf.setLineNumber(lineNumber);
                    } catch (Exception ex) {
                    }
                } else {
                    // changed method (and possibly file) so we get a full frame and need to
                    // update the entire stack (unless we're stopped in signal handling code)
                    gdb.stack_list_frames();
                }
                setStopped();
                if (GdbTimer.getTimer("Step").getSkipCount() == 0) { // NOI18N
                    GdbTimer.getTimer("Step").stop("Step1");// NOI18N
                    GdbTimer.getTimer("Step").report("Step1");// NOI18N
                }
            } else if (reason.equals("signal-received")) { // NOI18N
                if (getState() == STATE_RUNNING) {
                    gdb.stack_list_frames();
                    setStopped();
                }
            } else {
                if (!reason.startsWith("exited")) { // NOI18N
                    gdb.stack_list_frames();
                    setStopped();
                } else {
                    setStopped();
                    // Disable debugging buttons
                    setExited();
                }
            }
        } else {
            gdb.stack_list_frames();
            setStopped();
        }
    }
    
    /**
     * Callback method for break_insert Gdb/MI command.
     *
     * @param reason a reason why program is stopped
     */
    public void breakpointValidation(int id, Map<String, String> map) {
        String number = map.get("number"); // NOI18N
        GdbBreakpoint breakpoint = GdbBreakpoint.getPending(id);
        if (breakpoint != null) {
            if (breakpoint instanceof FunctionBreakpoint) {
                FunctionBreakpoint fb = (FunctionBreakpoint) breakpoint;
                try {
                    // Set URL
                    String fullname = map.get("fullname"); // NOI18N
                    if (fullname == null) {
                        String file = map.get("file"); // NOI18N
                        if (file != null) {
                            if (file.charAt(0) == '/') {
                                fullname = file;
                            } else {
                                fullname = runDirectory + file;
                            }
                        }
                    }
                    if (fullname != null) {
                        fb.setURL(fullname);
                    }
                    // Set line number
                    String line = map.get("line"); // NOI18N                        
                    fb.setLineNumber(Integer.parseInt(line));
                } catch (Exception e) {
                    // Fullname may not be valid for this system.
                }
            }
            breakpoint.setValidationResult(id, number);
        }
    }
     
    /**
     * This utility method helps to start a new Cnd debugger session. 
     *
     * @param hostName a name of computer to attach to
     * @param portNumber a port number
     */
    public static GdbDebugger attach(String hostName, int pid, Object[] services)
		    throws DebuggerStartException {
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager().startDebugging(
                DebuggerInfo.create(SESSION_ID, null));
	int k = es.length;

        for (int i = 0; i < k; i++) {
            GdbDebugger d = (GdbDebugger) es [i].lookupFirst(null, GdbDebugger.class);
            if (d == null) {
		continue;
	    }
            d.waitRunning();
            return d;
        }
        throw new DebuggerStartException(new InternalError());
    }
    
    /**
     *  Called when GdbProxy receives the results of a -stack-list-frames command.
     */
    private void stackUpdate(List stack) {
        CallStackFrame frame;
        String key;
        int i;
        
        for (i = 0; i < stack.size(); i++) {
            String line = (String) stack.get(i);
            Map<String, String> map = GdbUtils.createMapFromString(line.substring(6));
            
            String func = map.get("func"); // NOI18N
            String file = map.get("file"); // NOI18N
            String fullname = map.get("fullname"); // NOI18N
            String lnum = map.get("line"); // NOI18N
            String addr = map.get("addr"); // NOI18N
            if (fullname == null && file != null) {
                if (file.charAt(0) == '/') {
                    fullname = file;
                    log.fine("GDI.stackUpdate: Setting fullname from file"); // NOI18N
                } else {
                    fullname = runDirectory + file;
                    log.fine("GDI.stackUpdate: Setting fullname from runDirectory + file"); // NOI18N
                }
            }
            
            if (i < callstack.size()) {
                frame = (CallStackFrame) callstack.get(i);
                frame.setFrameNumber(i);
                frame.set(func, file, fullname, lnum, addr);
            } else {
                frame = new CallStackFrameImpl(this, func, file, fullname, lnum, addr);
                frame.setFrameNumber(i);
                callstack.add(i, frame);
            }
        }
        
        int k = i;
        synchronized (callstack) {
            while (i++ < callstack.size()) {
                callstack.remove(k);
            }
        }
        
        pcs.firePropertyChange(PROP_CURRENT_CALL_STACK_FRAME, 0, 1);
    }
    
    /**
     * Returns list of cached local variables for this debugger.
     *
     * @return list of local variables
     */
    public List<GdbVariable> getLocalVariables() {
        return (List) localVariables.clone();
    }
    
    /*
     * Locals View Model
     */
    private LocalsTreeModel localsTreeModel;
    private LocalsTreeModel getLocalsTreeModel() {
        if (localsTreeModel == null)
            localsTreeModel = (LocalsTreeModel) lookupProvider.
                    lookupFirst("LocalsView", TreeModel.class); // NOI18N
        return localsTreeModel;
    }
    
    // ---------------------- Watches View support ---------------- //
    /*
     * Interface variables between "Watches View" and GdbDebugger
     */
    public ModelListener watchesViewListener = null;
    private WatchesModel currentWatchesModel = null;
    /**
     * Registers WatchesModel in debugger.
     *
     * @parameter wm Current WatchesModel
     */
    public void registerWatchesModel(WatchesModel wm) {
        currentWatchesModel = wm;
    }
    
    /**
     * Refreshes Watches View
     *
     */
    public void updateWatchesModel() {
        if (watchesViewListener == null) return;
        watchesViewListener.modelChanged(new ModelEvent.TreeChanged(currentWatchesModel));
    }
    // ---------------------- End Of Watches ---------------------- //
    
    // ---------------------- Variables -------------------------- //
    
    private int varIndex = 0;
    private final String varPrefix = "Variable_"; // NOI18N
    /**
     * Sets variable value
     *
     * @parameter name Variable name
     * @parameter value Variable value
     */
    public void setVariableValue(String name, String value) {
        // FIXME
//        String gdbVarName = varPrefix + (varIndex++);
//        String varName = name;
//        if (varName.indexOf(' ') >= 0) varName = "\"" + name + "\""; // NOI18N
//        synchronized (currentLocals) {
//            gdb.var_delete(gdbVarName);
//            gdb.var_create(gdbVarName, "*", varName); // NOI18N
//            gdb.var_assign(gdbVarName, value);
//        }
    }

    /**
     * Returns variable type as String.
     *
     * @param expression A variable name or an expression
     * @return variable type
     */
    public String getVariableType(String expression) {
//        gdb.var_create(null, null, expression);
        String sType = gdb.getVariableType(expression);
        return sType;
    }

//    /**
//     * Returns variable value as String.
//     *
//     * @param expression A variable name or an expression
//     * @return variable value
//     */
//    public String getVariableValue(String expression) {
//        String sValue = gdb.data_evaluate(expression); // FIXUP! Use gdb.var_evaluate(expression)
//        return sValue;
//    }
 
    /**
     * Returns variable's number of children as String.
     *
     * @param expression A variable name or an expression
     * @return number of children
     */
    public String getVariableNumChild(String expression) {
//        gdb.var_create(null, null, expression);
        return gdb.getVariableNumChild(expression);
    }

    /**
     * Evaluates expression and returns its value as String.
     *
     * @param expression A variable name or an expression
     * @return expression value
     */
    public String getExpressionValue(String expression) {
        String sValue = gdb.data_evaluate(expression);
        return sValue;
    }   
    // ---------------------- End Of Variables ------------------- //
        
    /**
     * Waits till the Virtual Machine is started and returns
     * {@link DebuggerStartException} if some problem occurres.
     *
     * @throws DebuggerStartException is some problems occurres during debugger
     *         start
     *
     * @see AbstractDICookie#getVirtualMachine()
     */
    public void waitRunning() throws DebuggerStartException {
        throw new DebuggerStartException("tmp - Not fully implemented..."); // XXX - Debug // NOI18N
    }
    
    /**
     * Returns call stack for this debugger.
     *
     * @return call stack
     */
    public ArrayList getCallStack() {
        return callstack;
    }
    
    /**
     * Returns call stack for this debugger.
     *
     * @param from Starting frame
     * @param to Ending frame (one beyond what we want)
     * @return call stack
     */
    public CallStackFrame[] getCallStackFrames(int from, int to) {
        int cnt = to - from;
        
        if ((from + cnt) <= getStackDepth()) {
            CallStackFrame[] frames = new CallStackFrame[cnt];
            for (int i = 0; i < cnt; i++) {
                frames[i] = (CallStackFrame) callstack.get(from + i);
            }
            return frames;
        } else {
            return new CallStackFrame[0];
        }
    }
    
    public int getStackDepth() {
        return callstack.size();
    }
    
    /**
     * Returns current stack frame or null.
     *
     * @return current stack frame or null
     */
    public synchronized CallStackFrame getCurrentCallStackFrame() {
        if (callstack.size() > 0) {
            if (currentCallStackFrame == null) {
                currentCallStackFrame = (CallStackFrame) callstack.get(0);
            }
        } else {
            currentCallStackFrame = null;
        }
        return currentCallStackFrame;
    }
    
    /**
     * Sets a stack frame current.
     *
     * @param Frame to make current (or null)
     */
    public void setCurrentCallStackFrame(CallStackFrame callStackFrame){
        CallStackFrame old = setCurrentCallStackFrameNoFire(callStackFrame);
        updateLocalVariables(callStackFrame.getFrameNumber());
        if (old == callStackFrame) {
            return;
        }
        pcs.firePropertyChange(PROP_CURRENT_CALL_STACK_FRAME, old, callStackFrame);
    }
    
    private CallStackFrame setCurrentCallStackFrameNoFire(CallStackFrame callStackFrame) {
        CallStackFrame old;
        
        synchronized (this) {
            if (callStackFrame == currentCallStackFrame) {
                return callStackFrame;
            }
            old = currentCallStackFrame;
            currentCallStackFrame = callStackFrame;
        }
        return old;
    }
    
    /**
     *  Convert an absolute path to a project-relative path.
     *
     *  @param path The absolute path to convert
     *  @return The project-relative path or the unchanged path
     */
    public String getProjectRelativePath(String path) {
        if (path.startsWith(runDirectory)) {
            return (path.substring(runDirectory.length()));
        }
        return path;
    }
    
    /**
     *  Get the directory we run in.
     */
    public String getRunDirectory() {
        return runDirectory;
    }
    
    /**
     * Returns <code>true</code> if this debugger supports fix & continue
     * (HotSwap).
     *
     * @return <code>true</code> if this debugger supports fix & continue
     */
    public boolean canFixClasses() {
        return false;
    }
    
    /**
     * Returns <code>true</code> if this debugger supports Pop action.
     *
     * @return <code>true</code> if this debugger supports Pop action
     */
    public boolean canPopFrames() {
        return false;
    }
    
    /**
     * Determines if the target debuggee can be modified.
     *
     * @return <code>true</code> if the target debuggee can be modified or when
     *         this information is not available (on JDK 1.4).
     * @since 2.3
     */
    public boolean canBeModified() {
        return true;
    }
    
    /**
     * Implements fix & continue (HotSwap). Map should contain class names
     * as a keys, and byte[] arrays as a values.
     *
     * @param classes a map from class names to be fixed to byte[]
     */
    public void fixClasses(Map classes) {
    }
    
    /**
     * Returns instance of SmartSteppingFilter.
     *
     * @return instance of SmartSteppingFilter
     */
    //public SmartSteppingFilter getSmartSteppingFilter();
    
    /**
     * Adds property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l new listener.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        pcs.addPropertyChangeListener(propertyName, l);
    }
    
    /**
     *  Adds property change listener.
     *
     * @param l new listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    /**
     * Removes property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l removed listener.
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        pcs.removePropertyChangeListener(propertyName, l);
    }
    
    /**
     *  Removes property change listener.
     *
     * @param l removed listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    private void firePropertyChange(String name, Object o, Object n) {
        pcs.firePropertyChange(name, o, n);
    }
    
    public void break_disable(int breakpointNumber) {
        gdb.break_disable(breakpointNumber);
    }
    
    public int getCurrentToken() {
        return currentToken;
    }
    
    public boolean isCygwin() {
        return cygwin;
    }
    
    public boolean isCplusPlus() {
        return cplusplus;
    }
}
