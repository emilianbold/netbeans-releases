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

package org.netbeans.modules.cnd.debugger.gdb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.BreakpointImpl;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointEvent;
import org.netbeans.modules.cnd.debugger.gdb.expr.Expression;
import org.netbeans.modules.cnd.debugger.gdb.models.AbstractVariable;
import org.netbeans.modules.cnd.debugger.gdb.models.GdbWatchVariable;
import org.netbeans.modules.cnd.debugger.gdb.profiles.GdbProfile;
import org.netbeans.modules.cnd.debugger.gdb.proxy.GdbMiDefinitions;
import org.netbeans.modules.cnd.debugger.gdb.proxy.GdbProxy;
import org.netbeans.modules.cnd.debugger.gdb.timer.GdbTimer;
import org.netbeans.modules.cnd.debugger.gdb.utils.CommandBuffer;
import org.netbeans.modules.cnd.debugger.gdb.utils.FieldTokenizer;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

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
    public static final String          PROP_KILLTERM = "killTerm"; // NOI18N

    public static final String          STATE_NONE = "state_none"; // NOI18N
    public static final String          STATE_STARTING = "state_starting"; // NOI18N
    public static final String          STATE_ATTACHING = "state_attaching"; // NOI18N
    public static final String          STATE_LOADING = "state_loading"; // NOI18N
    public static final String          STATE_LOADED = "state_loaded"; // NOI18N
    public static final String          STATE_READY = "state_ready"; // NOI18N
    public static final String          STATE_RUNNING = "state_running"; // NOI18N
    public static final String          STATE_STOPPED = "state_stopped"; // NOI18N
    public static final String          STATE_SILENT_STOP = "state_silent_stop"; // NOI18N
    public static final String          STATE_EXITED  = "state_exited"; // NOI18N
    
    private static final int            DEBUG_ATTACH = 999;
    
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
    private ArrayList<CallStackFrame> callstack = new ArrayList<CallStackFrame>();
    private GdbEngineProvider gdbEngineProvider;
    private CallStackFrame currentCallStackFrame;
    public final Object LOCK = new Object();
    private long programPID = 0;
    private double gdbVersion = 0.0;
    private boolean continueAfterFirstStop = true;
    private ArrayList<GdbVariable> localVariables = new ArrayList<GdbVariable>();
    private Map<Integer, GdbVariable> symbolCompletionTable = new HashMap<Integer, GdbVariable>();
    private Map<Integer, StringBuilder> typeCompletionTable = new HashMap<Integer, StringBuilder>();
    private Set<String> typePendingTable = new HashSet<String>();
    private Map<Integer, AbstractVariable> derefValueMap = new HashMap<Integer, AbstractVariable>();
    private Map<Integer, GdbWatchVariable> watchTypeMap = new HashMap<Integer, GdbWatchVariable>();
    private Map<Integer, GdbWatchVariable> watchValueMap = new HashMap<Integer, GdbWatchVariable>();
    private Map<Integer, BreakpointImpl> pendingBreakpointMap = new HashMap<Integer, BreakpointImpl>();
    private Map<Integer, AbstractVariable> updateVariablesMap = new HashMap<Integer, AbstractVariable>();
    private Map<String, BreakpointImpl> breakpointList = Collections.synchronizedMap(new HashMap<String, BreakpointImpl>());
    private Map<String, String> varToTypeMap = new HashMap<String, String>();
    private Logger log = Logger.getLogger("gdb.logger"); // NOI18N
    private int currentToken = 0;
    private String currentThread = "1"; // NOI18N
    private int ttToken = 0;
    private ToolTipAnnotation ttAnnotation = null;
    private Timer startupTimer = null;
    private boolean cygwin = false;
    private boolean cplusplus = false;
    private int tcwait = 0; // counter for type completion
    private String firstBPfullname;
    private String firstBPfile;
    private String firstBPline;
        
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
            if (!Utilities.isWindows() && conType != RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW &&
                    pae.getID() != DEBUG_ATTACH) {
                termpath = pae.getProfile().getTerminalPath();
            }
            if (!Boolean.getBoolean("gdb.suppress-timeout")) {
                startupTimer = new Timer("GDB Startup Timer Thread"); // NOI18N
                startupTimer.schedule(new TimerTask() {
                        public void run() {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                                   "ERR_StartupTimeout"))); // NOI18N
                            setExited();
                            finish(true);
                        }
                }, 30000);
            }
            String gdbCommand = profile.getGdbPath(profile.getGdbCommand(), pae.getProfile().getRunDirectory());
            if (gdbCommand.toLowerCase().contains("cygwin")) { // NOI18N
                cygwin = true;
            }
            gdb = new GdbProxy(this, gdbCommand, pae.getProfile().getEnvironment().getenv(),
                    runDirectory, termpath);
            gdb.gdb_version();
            gdb.environment_directory(runDirectory);
            gdb.gdb_show("language"); // NOI18N
            if (pae.getID() == DEBUG_ATTACH) {
                programPID = (Long) lookupProvider.lookupFirst(null, Long.class);
                gdb.target_attach(Long.toString(programPID));
//                gdb.file_symbol_file(pae.getExecutable());
                setAttaching();
            } else {
                gdb.file_exec_and_symbols(getProgramName(pae.getExecutable()));
        
                if (Utilities.isWindows()) {
                    if (conType != RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW) {
                        gdb.set_new_console();
                    }
                }
                if (pae.getID() == ProjectActionEvent.DEBUG_STEPINTO) {
                    continueAfterFirstStop = false; // step into project
                }
                gdb.break_insert(GDB_TMP_BREAKPOINT, "main"); // NOI18N
                if (Utilities.isWindows()) {
                    // WinAPI apps don't have a "main" function. Use "WinMain" if Windows.
                    gdb.break_insert(GDB_TMP_BREAKPOINT, "WinMain"); // NOI18N
                }
                try {
                    gdb.exec_run(pae.getProfile().getArgsFlat());
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                    ((Session) lookupProvider.lookupFirst(null, Session.class)).kill();
                }
                if (Utilities.isWindows()) {
                    gdb.info_threads(); // we get the PID from this...
                } else if (Utilities.getOperatingSystem() != Utilities.OS_MAC) {
                    gdb.info_proc(); // we get the PID from this...
                }
            }
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
            finish(false);
        }
    }

    public String[] getThreadInformation() {
        while (gdb == null) {
            try {
                System.err.println("GD.getThreadInformation: null gdb...");
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
        CommandBuffer cb = new CommandBuffer(gdb.info_threads());
        String results = cb.postAndWait();
        if (results.length() > 0) {
            List<String> list = new ArrayList<String>();
            for (String line : results.split("\\\\n")) { // NOI18N
                if (!line.trim().startsWith("from ")) { // NOI18N
                    list.add(line);
                }
            }
            return list.toArray(new String[list.size()]);
        }
        return new String[0];
    }

    public int getThreadCount() {
        return 1;
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
    
    /** If the type definition contains { ... }, strip it. It will be picked up elsewhere */
    private String stripFields(String info) {
        StringBuilder buf = new StringBuilder();
        int pos1 = info.indexOf('{');
        int pos2 = GdbUtils.findMatchingCurly(info, pos1);
        
        if (pos1 != -1 && pos2 != -1) {
            buf.append(info.substring(0, pos1).trim());
            buf.append(info.substring(pos2 + 1).trim());
        } else {
            buf.append(info);
        }
        
        return buf.toString();
    }
    
    /** Get the gdb version */
    public double getGdbVersion() {
        return gdbVersion;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PROP_STATE)) {
            if (evt.getNewValue().equals(STATE_READY)) {
                if (continueAfterFirstStop) {
                    continueAfterFirstStop = false;
                    setRunning();
                } else {
                    gdb.stack_list_frames();
                    setStopped();
                }
            } else if (evt.getNewValue() == STATE_STOPPED) {
                tcwait = 0;
                updateLocalVariables(0);
            } else if (evt.getNewValue() == STATE_SILENT_STOP) {
                interrupt();
            } else if (evt.getNewValue() == STATE_ATTACHING) {
                if (validAttach()) { // FIXME - Is this needed? Is the need platform specific?
                    setLoading();
                } else {
                    finish(false);
                }
            } else if (evt.getNewValue() == STATE_RUNNING && 
                    (evt.getOldValue() == STATE_SILENT_STOP ||
                     evt.getOldValue() == STATE_READY))  {
                gdb.exec_continue();
            } else if (evt.getNewValue() == STATE_RUNNING) {
                synchronized (LOCK) {
                    typeCompletionTable.clear();
                    typePendingTable.clear();
                    symbolCompletionTable.clear();
                }
            } else if (evt.getNewValue() == STATE_EXITED) {
                finish(false);
            }
        }
    }
    
    /**
     * Check that the executable from the selected project matches the attached process. Gdb itself
     * doesn't do this validation and its non-trivial and the methods of validation are system
     * dependent.
     * 
     * @return true if the project matches the attached to executable
     */
    private boolean validAttach() {
        assert !Thread.currentThread().getName().equals("GdbReaderRP") : // NOI18N
            "Internal Error: Cannot call validAttach from GdbReaderRP thread"; // NOI18N
        if (!Utilities.isWindows()) {
            File dir = new File("/proc/" + Long.toString(programPID));
            if (dir.exists() && dir.isDirectory()) {
                // various system dependent checks...
            }
        }
        ProjectActionEvent pae = (ProjectActionEvent) lookupProvider.lookupFirst(null, ProjectActionEvent.class);
        String info = getInfoFiles();
        if (checkInfoFiles(pae, info)) {
            return true;
        }
        return true;  // FIXME - this is a debug response!
//        return false;
    }
    
    private String getInfoFiles() {
        return null;
    }
    
    private boolean checkInfoFiles(ProjectActionEvent pae, String info) {
        String executable = pae.getExecutable();
//        String[] lines = info.split("\\n");
//        for (String line : lines) {
//            if (line.contains(executable)) {
//                return true;
//            }
//        }
        return false;
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
    public void finish(boolean killTerm) {
        if (!state.equals(STATE_NONE)) {
            if (killTerm) {
                firePropertyChange(PROP_KILLTERM, true, false);
            }
            if (gdb != null) {
                if (state.equals(STATE_RUNNING)) {
                    ProjectActionEvent pae = (ProjectActionEvent) lookupProvider.lookupFirst(
                            null, ProjectActionEvent.class);
                    gdb.exec_interrupt();
                    if (pae.getID() == DEBUG_ATTACH) {
                        gdb.target_detach();
                    } else {
                        gdb.exec_abort();
                    }
                }
                gdb.gdb_exit();
            }

            stackUpdate(new ArrayList<String>());
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
        finish(false);
    }
    
    /** Sends request to get arguments and local variables */
    private void updateLocalVariables(int frame) {
        synchronized (LOCK) {
            synchronized (localVariables) {
                localVariables.clear(); // clear old variables so we can store new ones here
            }
            gdb.stack_select_frame(frame);
            gdb.stack_list_arguments(1, frame, frame);
            gdb.stack_list_locals(ALL_VALUES);
        }
    }
    
    /** Handle geb responses starting with '^' */
    public void resultRecord(int token, String msg) {
        GdbWatchVariable watch;
        AbstractVariable avar;
        Integer itok = Integer.valueOf(token);
        
        currentToken = token + 1;
        if (msg.startsWith("^done,bkpt=")) { // NOI18N (-break-insert)
            msg = msg.substring(12, msg.length() - 1);
            breakpointValidation(token, GdbUtils.createMapFromString(msg));
            if (getState().equals(STATE_SILENT_STOP)) {
                setRunning();
            }
        } else if (msg.startsWith("^done,stack=")) { // NOI18N (-stack-list-frames)
            if (state.equals(STATE_STOPPED)) { // Ignore data if we've resumed running
                stackUpdate(GdbUtils.createListFromString((msg.substring(13, msg.length() - 1))));
            }
        } else if (msg.startsWith("^done,locals=")) { // NOI18N (-stack-list-locals)
            if (state.equals(STATE_STOPPED)) { // Ignore data if we've resumed running
                addLocalsToLocalVariables(msg.substring(13));
                completeLocalVariables();
            }
        } else if (msg.startsWith("^done,stack-args=")) { // NOI18N (-stack-list-arguments)
            if (state.equals(STATE_STOPPED)) { // Ignore data if we've resumed running
                addArgsToLocalVariables(msg.substring(17));
            }
        } else if (msg.startsWith("^done,new-thread-id=")) { // NOI18N (-thread-select)
//            firePropertyChange(PROP_CURRENT_THREAD, new)
        } else if (msg.startsWith("^done,value=") && msg.contains("auto; currently c++")) { // NOI18N
            cplusplus = true;
        } else if (msg.startsWith("^done,value=")) { // NOI18N (-data-evaluate-expression)
            if (token == ttToken) {
                ttAnnotation.postToolTip(msg.substring(13, msg.length() - 1));
                ttToken = 0;
            } else if ((avar = derefValueMap.remove(itok)) != null) {
                avar.setDerefValue(msg.substring(13, msg.length() - 1));
            } else if ((watch = watchValueMap.remove(itok)) != null) {
                watch.setWatchValue(msg.substring(13, msg.length() - 1));
            } else if ((avar = updateVariablesMap.remove(itok)) != null) {
                avar.setModifiedValue(msg.substring(13, msg.length() - 1));
            }
        } else if (msg.equals("^done") && getState().equals(STATE_SILENT_STOP)) { // NOI18N
            setRunning();
        } else if (msg.equals("^done")) { // NOI18N
            CommandBuffer cb = CommandBuffer.getCommandBuffer(itok);
            StringBuilder typebuf;
            if (cb != null) {
                cb.done();
                cb.callback();
                cb.dispose();
            } else if ((typebuf = typeCompletionTable.get(itok)) != null) { // complete response of ptype command
                String tbuf = typebuf.toString();
                int pos = tbuf.indexOf('='); // its guaranteed to have '='
                String type = tbuf.substring(0, pos);
                if (tbuf.endsWith("]")) { // NOI18N
                    addType(type, stripFields(tbuf.substring(pos + 1)));
                } else if (tbuf.indexOf('{') == -1) { // NOI18N
                    addType(type, tbuf.substring(pos + 1));
                } else if (type.startsWith("enum ")) { // NOI18N
                    List list = getEnumList(tbuf.substring(pos + 1));
                    addType(type, list);
                } else {
                    String t;
                    Map map = getCSUFieldMap(tbuf.substring(pos + 1));
                    if (type.equals("this") && isCplusPlus()) { // NOI18N
                        int pos1 = tbuf.indexOf(' ');
                        int pos2 = tbuf.indexOf(' ', pos1 + 1);
                        t = tbuf.substring(pos1, pos2).trim();
                        addType(t, map);
                    } else {
                        t = varToTypeMap.remove(type);
                        String n = map.get("<name>").toString(); // NOI18N
                        if (t == null || n.equals(t)) {
                            addType(type, map);
                        } else {
                            addType(n, map);
                            addType(t, n);
                        }
                    }
                    if (map.size() > 0) { // NOI18N
                        checkForUnknownTypes(map);
                    }
                }
                typePendingTable.remove(type);
                typeCompletionTable.remove(itok);
                
                if (tbuf.indexOf(':') != -1) {
                    checkForSuperClass(tbuf.substring(pos + 1));
                }
            } else if ((watch = watchTypeMap.remove(itok)) != null) {
                watch.setWatchType(watch.getTypeBuf());
            } else if (pendingBreakpointMap.get(itok) != null) {
                breakpointValidation(token, null);
            }
        } else if (msg.startsWith("^running") && getState().equals(STATE_STOPPED)) { // NOI18N
            setRunning();
        } else if (msg.startsWith("^error,msg=")) { // NOI18N
            msg = msg.substring(11);
            
            if (typeCompletionTable.get(itok) != null) {
                StringBuilder typebuf = typeCompletionTable.remove(itok);
                String tbuf = typebuf.toString();
                int pos = tbuf.indexOf('='); // its guaranteed to have '='
                String type = tbuf.substring(0, pos);
                typePendingTable.remove(type);
                if (msg.startsWith("\"No symbol \\\"")) { // NOI18N
                    int pos1 = msg.substring(13).indexOf("\""); // NOI18N
                    if (pos1 != -1 && msg.substring(13, pos1 + 12).equals(type)) {
                        String var = varToTypeMap.get(type);
                        if (var != null) {
                            addTypeCompletion(var);
                        }
                    }
                }
            } else if (token == ttToken) { // invalid tooltip request
                ttAnnotation.postToolTip('>' + msg.substring(1, msg.length() - 1) + '<');
                ttToken = 0;
            } else if (derefValueMap.get(itok) != null) {
                avar = derefValueMap.remove(itok);
                avar.setDerefValue(msg.substring(1, msg.length() - 1));
            } else if (watchValueMap.get(itok) != null) {
                watch = watchValueMap.remove(itok);
                if (msg.startsWith("\"The program being debugged was signaled while in a function called from GDB.")) { // NOI18N
                   watch.setValueToError(NbBundle.getMessage(GdbDebugger.class,
                           "ERR_WatchedFunctionAborted")); // NOI18N
                } else {
                    watch.setValueToError(msg.substring(1, msg.length() - 1));
                }
            } else if (watchTypeMap.get(itok) != null) {
                watch = watchTypeMap.remove(itok);
                watch.setTypeToError(msg.substring(1, msg.length() - 1));
            } else if ((avar = updateVariablesMap.remove(itok)) != null) {
                avar.restoreOldValue();
            } else if (msg.equals("\"Can't attach to process.\"")) { // NOI18N
                DialogDisplayer.getDefault().notify(
                       new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                       "ERR_CantAttach"))); // NOI18N
                ((Session) lookupProvider.lookupFirst(null, Session.class)).kill();
            } else if (msg.startsWith("\"No symbol ") && msg.endsWith(" in current context.\"")) { // NOI18N
                String type = msg.substring(13, msg.length() - 23);
                if (type.equals("string") || type.equals("std::string")) { // NOI18N
                    // work-around for gdb bug (in *all* versions)
                    addTypeCompletion("std::basic_string<char,std::char_traits<char>,std::allocator<char> >"); // NOI18N
                    addType("string", "std::basic_string<char,std::char_traits<char>,std::allocator<char> >"); // NOI18N
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
            } else if (msg.contains("mi_cmd_break_insert: Garbage following <location>")) { // NOI18N
                // ignore - probably a breakpoint from another project
            } else if (msg.contains("Undefined mi command: ") && msg.contains("(missing implementation")) { // NOI18N
                // ignore - gdb/mi defines commands which haven't been implemented yet
            } else if (pendingBreakpointMap.remove(Integer.valueOf(token)) != null) {
                if (pendingBreakpointMap.isEmpty() && state.equals(STATE_LOADING)) {
                    setReady();
                }
            } else if (!state.equals(STATE_NONE)) {
                // ignore errors after we've terminated (they could have been in the input queue)
                log.warning("Unexpected gdb error: " + msg);
            }
        }
    }
    
    /** Handle gdb responses starting with '*' */
    public void execAsyncOutput(int token, String msg) {
        if (msg.startsWith("*stopped")) { // NOI18N
            Map<String, String> map = GdbUtils.createMapFromString(msg.substring(9));
            stopped(token, map);
        }
    }
    
    /** Handle gdb responses starting with '~' */
    public void consoleStreamOutput(int token, String omsg) {
        StringBuilder typebuf;
        GdbVariable var;
        GdbWatchVariable watch;
        String type;
        CommandBuffer cb = CommandBuffer.getCommandBuffer(Integer.valueOf(token));
        String msg;
        
        if (omsg.endsWith("\\n")) { // NOI18N
            msg = omsg.substring(0, omsg.length() - 2);
        } else {
            msg = omsg;
        }
        if (cb != null) {
            cb.append(omsg);
        } else if (msg.startsWith("type = ")) { // NOI18N
            if ((var = symbolCompletionTable.remove(Integer.valueOf(token))) != null) { // whatis
                type = msg.substring(7);
                var.setType(type);
                if (!GdbUtils.isSimple(type) && !GdbUtils.isSimplePointer(type)) {
                    if (type.endsWith("{...}")) { // NOI18N
                        addTypeCompletion('$' + var.getName()); // unnamed type
                    } else {
                        varToTypeMap.put(var.getName(), type);
                        addTypeCompletion(var.getName());
                    }
                }
            } else if ((typebuf = typeCompletionTable.get(Integer.valueOf(token))) != null) { // ptype
                typebuf.append(msg.substring(7));
            } else if ((watch = watchTypeMap.get(Integer.valueOf(token))) != null) {
                watch.appendTypeBuf(msg.substring(7));
            }
        } else if ((typebuf = typeCompletionTable.get(Integer.valueOf(token))) != null) {
            typebuf.append(msg);
        } else if ((watch = watchTypeMap.get(Integer.valueOf(token))) != null) {
            watch.appendTypeBuf(msg);
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
        } else if (msg.startsWith("Breakpoint ") && msg.contains(" at 0x")) { // NOI18N
            // Due to a gdb bug (6.6 and earlier) we use a "break" command for multi-byte filenames
            int pos = msg.indexOf(' ', 12);
            String num = msg.substring(11, pos);
            Map<String, String> map = new HashMap<String, String>();
            map.put("number", num); // NOI18N
            breakpointValidation(token, map);
            if (getState().equals(STATE_SILENT_STOP)) {
                setRunning();
            }
        } else if (msg.startsWith("Copyright ") || // NOI18N
                msg.startsWith("GDB is free software,") || // NOI18N
                msg.startsWith("welcome to change it and") || // NOI18N
                msg.contains("show copying") || // NOI18N
                msg.startsWith("There is absolutely no warranty for GDB") || // NOI18N
                msg.startsWith("Source directories searched: ") || // NOI18N
                msg.startsWith("This GDB was configured as")) { // NOI18N
            // do nothing (but don't print these expected messages)
        } else if (programPID == 0 && msg.startsWith("process ")) { // NOI18N (Unix method)
            int pos = msg.indexOf(' ', 8);
            String text;
            if (pos > 0) {
                text = msg.substring(8, pos);
            } else {
                text = msg.substring(8);
            }
            try {
                programPID = Long.parseLong(text);
            } catch (NumberFormatException ex) {
                log.warning("Failed to get PID from \"info proc\""); // NOI18N
            }
        } else if (programPID == 0) {
            if (msg.startsWith("* 1 thread ")) { // NOI18N
                int pos = msg.indexOf('.');
                if (pos > 0) {
                    try {
                        programPID = Long.valueOf(msg.substring(11, pos));
                    } catch (NumberFormatException ex) {
                log.warning("Failed to get PID from \"info threads\""); // NOI18N
                    }
                }
            } else if (msg.startsWith("[Switching to process ")) { // NOI18N
                int pos = msg.indexOf(' ', 22);
                if (pos > 0) {
                    try {
                        programPID = Long.valueOf(msg.substring(22, pos));
                    } catch (NumberFormatException ex) {
                    }
                }
            }
        }
    }
    
    /** Handle gdb responses starting with '&' */
    public void logStreamOutput(int token, String msg) {
        if (pendingBreakpointMap.get(Integer.valueOf(token)) != null) {
            breakpointValidation(token, msg.substring(2, msg.length() - 3));
        } else if (msg.startsWith("&\"info proc") || // NOI18N
                msg.startsWith("&\"info threads") || // NOI18N
                msg.startsWith("&\"directory ") || // NOI18N
                msg.startsWith("&\"set new-console") || // NOI18N
                msg.startsWith("&\"whatis ") || // NOI18N
                msg.startsWith("&\"warning: Temporarily disabling breakpoints for unloaded shared library") || // NOI18N
                msg.contains("/usr/lib/ld.so")) { // NOI18N
            // ignore these messages
        } else {
            log.finest("GDI.logStreamOutput: " + msg); // NOI18N
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
    
    /** Handle gdb responses starting with '@' */
    public void targetStreamOutput(String msg) {
       log.fine("GDI.targetStreamOutput: " + msg);  // NOI18N
    }
    
    private Map<String, Object> addSuperclassEntries(Map<String, Object> map, String info) {
        char c;
        int pos;
        int start = 0;
        int scount = 1;
        
        for (int i = 0; i < info.length(); i++) {
            if (info.substring(i).startsWith("public ")) { // NOI18N
                i += 7;
                start = i;
            } else if (info.substring(i).startsWith("private ")) { // NOI18N
                i += 8;
                start = i;
            } else if (info.substring(i).startsWith("protected ")) { // NOI18N
                i += 10;
                start = i;
            }
            if (i < info.length()) {
                c = info.charAt(i);
                if (c == '<') {
                    pos = GdbUtils.findMatchingLtGt(info, i);
                    if (pos != -1) {
                        i = pos;
                    }
                } else if (c == ',') {
                    map.put("<super" + scount++ + ">", info.substring(start, i).trim()); // NOI18N
                    if ((i + 1) < info.length()) {
                    info = info.substring(i + 1);
                    i = 0;
                    start = 0;
                    }
                }
            }
        }
        map.put("<super" + scount++ + ">", info.substring(start).trim()); // NOI18N
        
        return map;
    }
    
    /**
     * Find the first ":" which isn'f part of a "::".
     * @param info The string to check
     * @return The index (if found) or -1
     */
    private int getSuperclassColon(String info) {
        char lastc = 0;
        char nextc;
        char ch;
        
        for (int i = 0; i < info.length(); i++) {
            ch = info.charAt(i);
            nextc = (i + 1) < info.length() ? info.charAt(i + 1) : 0;
            if (ch == ':' && nextc != ':' && lastc != ':') {
                return i;
            } else if (ch == '<') {
                i = GdbUtils.findMatchingLtGt(info, i);
            }
            lastc = ch;
        }
        return -1;
    }
    
    private Map getCSUFieldMap(String info) {
        Map<String, Object> map = new HashMap<String, Object>();
        int pos0;
        int pos1 = info.indexOf('{');
        int pos2 = GdbUtils.findMatchingCurly(info, pos1);
        String fields = null;
        String n;
        
        if (pos1 != -1) {
            if ((pos0 = getSuperclassColon(info.substring(0, pos1))) != -1) {
                map = addSuperclassEntries(map, info.substring(pos0 + 1, pos1));
            }
            if (pos0 == -1) {
                n = info.substring(0, pos1).trim();
            } else {
                n = info.substring(0, pos0).trim();
            }
            map.put("<name>", n.startsWith("class ") ? n.substring(5).trim() : n); // NOI18N
        }
        
        if (pos1 == -1 && pos2 == -1) {
            if (GdbUtils.isPointer(info)) {
                info = info.replace('*', ' ').trim();
            }
            Object o = getType(info);
            if (o != null) { // t can be null if stepping into a macro in a header file
                String t = o.toString();
                pos1 = t.indexOf('{');
                pos2 = GdbUtils.findMatchingCurly(t, pos1);
                if (pos1 != -1 && pos2 != -1) {
                    fields = t.substring(pos1 + 1, pos2);
                }
            }
        } else if (pos1 != -1 && pos2 != -1 && pos2 > (pos1 + 1)) {
            fields = info.substring(pos1 + 1, pos2 - 1);
        }
        if (fields != null) {
            map = parseFields(map, fields);
            if (map.isEmpty()) {
                map.put("<" + info.substring(0, pos1) + ">", "<No data fields>"); // NOI18N
            }
        }
        return map;
    }
        
    private Map<String, Object> parseFields(Map<String, Object> map, String info) {
        if (info != null) {
            int pos, pos2;
            FieldTokenizer tok = new FieldTokenizer(info);
            while (tok.hasMoreFields()) {
                String[] field = tok.nextField();
                if (field[0] != null) {
                    if (isNonAnonymousCSUDef(field)) {
                        pos = field[0].indexOf('{');
                        pos2 = field[0].lastIndexOf('}');
                        Map<String, Object> m;
                        m = parseFields(new HashMap<String, Object>(), field[0].substring(pos + 1, pos2).trim());
                        m.put("<typename>", shortenType(field[0])); // NOI18N
                        map.put(field[1], m);
                    } else if (field[1].startsWith("<anonymous")) { // NOI18N
                        // replace string def with Map
                        pos = field[0].indexOf('{');
                        String frag = field[0].substring(pos + 1, field[0].length() - 1).trim();
                        Map<String, Object> m = parseFields(new HashMap<String, Object>(), frag);
                        map.put(field[1], m);
                    } else {
                        pos = field[1].indexOf('[');
                        if (pos == -1) {
                            map.put(field[1], field[0]);
                        } else {
                            map.put(field[1].substring(0, pos), field[0] + field[1].substring(pos));
                        }
                    }
                }
            }
        }
        return map;
    }
    
    private List getEnumList(String info) {
        List<String> list = new ArrayList<String>();
        int pos1 = info.indexOf('{');
        int pos2 = info.indexOf('}');
        if (pos1 != -1 && pos2 != -1) {
            StringTokenizer tok = new StringTokenizer(info.substring(pos1+ 1, pos2), ","); // NOI18N
            while (tok.hasMoreTokens()) {
                list.add(tok.nextToken().trim());
            }
            return list;
        } else {
            return null;
        }
    }
    
    private String shortenType(String type) {
        if (type.startsWith("class {")) { // NOI18N
            return "class {...}"; // NOI18N
        } else if (type.startsWith("struct {")) { // NOI18N
            return "struct {...}"; // NOI18N
        } else if (type.startsWith("union {")) { // NOI18N
            return "union {...}"; // NOI18N
        } else {
            return type;
        }
    }
    
    /**
     * See if the info string defines an embedded class/struct/union which is <b>not</b> an
     * anonymous c/s/u (those don't get typenames).
     *
     * @param info The string to check for a non-anonymous class/struct/union definition
     * @returns True for a non-anonymous class/struct/union definition
     */
    private boolean isNonAnonymousCSUDef(String[] field) {
        String name = field[1];
        String info = field[0];
        if (!name.startsWith("<anonymous") && // NOI18N
                (info.startsWith("class {") || info.startsWith("struct {") || info.startsWith("union {"))) { // NOI18N
            int start = info.indexOf('{');
            int end = GdbUtils.findMatchingCurly(info, start) + 1;
            if (start != -1 && end != 0 && !info.substring(start, end).equals("{...}")) { // NOI18N
                return true;
            }
        }
        return false;
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
            int start = 0;
            for (int i = 0; i < info.length(); i++) {
                if (info.substring(i).startsWith("public ")) { // NOI18N
                    i += 7;
                    start = i;
                } else if (info.substring(i).startsWith("private ")) { // NOI18N
                    i += 8;
                    start = i;
                } else if (info.substring(i).startsWith("protected ")) { // NOI18N
                    i += 10;
                    start = i;
                }
                if (i < info.length()) {
                    c = info.charAt(i);
                    if (c == '<') {
                        pos = GdbUtils.findMatchingLtGt(info, i);
                        if (pos != -1) {
                            i = pos;
                        }
                    } else if (c == ',') {
                        addTypeCompletion(info.substring(start, i).trim());
                        if ((i + 1) < info.length()) {
                            info = info.substring(i + 1);
                            i = 0;
                            start = 0;
                        }
                    }
                }
            }
            addTypeCompletion(info.substring(start).trim());
        }
    }
    
    private void checkForUnknownTypes(Map map) {
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object o = entry.getValue();
            if (o instanceof String) {
                String type = o.toString();
                if (!GdbUtils.isSimple(type) && !type.equals("<No data fields>") && !isUnnamedType(type)) { // NOI18N
                    addTypeCompletion(o.toString());
                }
            } else if (o instanceof Map) {
                checkForUnknownTypes((Map) o);
            }
        }
    }
    
    private boolean isUnnamedType(String type) {
        int pos = type.indexOf('{');
        if (pos != -1) {
            String tmp = null;
            if (type.startsWith("class ")) { // NOI18N
                tmp = type.substring(5, pos + 1).trim();
            } else if (type.startsWith("struct ")) { // NOI18N
                tmp = type.substring(6, pos + 1).trim();
            } else if (type.startsWith("union ")) { // NOI18N
                tmp = type.substring(5, pos + 1).trim();
            } else {
                log.warning("Unexpected type information [" + type + "]");
                tmp = "  "; // NOI18N - this makes this method return false...
            }
            return tmp.length() == 1;
        } else {
            return false;
        }
    }
    
    public void addTypeCompletion(String key) {
        key = trimKey(key);
        assert key != null && key.length() > 0;
        if (!GdbUtils.isSimple(key) && !typePendingTable.contains(key) &&
                !(isCplusPlus() && key.equals("bool")) && getType(key) == null) { // NOI18N
            int token = gdb.symbol_type(key.replace('$', ' ').trim());
            typeCompletionTable.put(Integer.valueOf(token), new StringBuilder(key + '='));
            typePendingTable.add(key);
        }
    }
    
    public void waitForTypeCompletionCompletion() {
        waitForTypeCompletionCompletion(40);
    }
    
    private void waitForTypeCompletionCompletion(int maxwait) {
        if (!isTypeCompletionComplete()) {
            if (Thread.currentThread().getName().equals("GdbReaderRP")) { // NOI18N
                log.warning("Attempting to wait for type completion on GDB Reader thread"); // NOI18N
            } else {
                while (!isTypeCompletionComplete() && state.equals(STATE_STOPPED)) {
                    if (maxwait > 0 && tcwait >= maxwait) {
                        return;
                    }
                    if (tcwait > 5) {
                        log.warning("Waiting for type completion - " + tcwait +
                                " [TCT: " + typeCompletionTable.size() + // NOI18N
                                ", TPT: " + typePendingTable.size() + "]"); // NOI18N
                    }
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
        return typeCompletionTable.isEmpty() && typePendingTable.isEmpty();
    }
    
    /**
     * Trim off all pointer and array info (including function pointer stuff). If its a Template,
     * ignore the <> parts.
     */
    public String trimKey(String key) {
        int pos;
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
        } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC &&
                info.startsWith("{frame={level=") && (pos = info.indexOf(",args={")) > 0 && info.endsWith("}}}")) { // NOI18N
            info = info.substring(pos + 7, info.length() - 3);
        }
        Collection<GdbVariable> v = GdbUtils.createArgumentList(info);
        if (!v.isEmpty()) {
            synchronized (localVariables) {
                localVariables.addAll(v);
            }
        }
    }
    
    private void addLocalsToLocalVariables(String info) {
        Collection<GdbVariable> v = GdbUtils.createLocalsList(info.substring(1, info.length() - 1));
        if (!v.isEmpty()) {
            synchronized (localVariables) {
                localVariables.addAll(v);
            }
        }
    }
    
    private void completeLocalVariables() {
        synchronized (localVariables) {
            if (!localVariables.isEmpty()) {
                for (GdbVariable var : localVariables) {
                    int token = gdb.whatis(var.getName());
                    symbolCompletionTable.put(Integer.valueOf(token), var);
                    if (var.getName().equals("this") && isCplusPlus()) { // NOI18N
                        addTypeCompletion(var.getName());
                    }
                }
            }
        }
        
        final GdbDebugger dbg = this;
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                dbg.waitForTypeCompletionCompletion(-1);
                dbg.firePropertyChange(PROP_LOCALS_VIEW_UPDATE, 0, 1);
            }
        });
    }
    
    public void updateVariable(AbstractVariable var, String name, String value) {
        int token = gdb.data_evaluate_expression(name + '=' + value);
        updateVariablesMap.put(new Integer(token), var);
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
        if (programPID > 0) { // Never send a kill if PID is 0
            kill(signal, programPID);
        }
    }
    
    /**
     * Send a kill command to the debuggee.
     *
     * @param signal The signal to send (as defined by "kill -l")
     * @param pid The process ID to send the signal to
     */
    public void kill(int signal, long pid) {
        if (pid > 0) {
            ArrayList<String> killcmd = new ArrayList<String>();
            File f;

            if (Utilities.isWindows()) {
                f = InstalledFileLocator.getDefault().locate("bin/GdbKillProc.exe", null, false); // NOI18N
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
        int idx = getCurrentCallStackIndex();
        if (isValidStackFrame(callstack.get(idx + 1))) {
            setState(STATE_RUNNING);
            gdb.exec_finish();
        } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                           "ERR_InvalidCallStackFrame"))); // NOI18N
        }
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
        if (state.equals(this.state)) {
            return;
        }
        String oldState = this.state;
        this.state = state;
        firePropertyChange(PROP_STATE, oldState, state);
    }
    
    public void setStarting() {
        setState(STATE_STARTING);
    }
    
    public void setAttaching() {
        setState(STATE_ATTACHING);
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
     * Called from GdbProxy when the target debuggee is stopped.
     * 
     * Note: The token parameter isn't used but is useful for conditional
     * breakpoints during debugging...
     *
     * @param token The token responsible for this stop
     * @param reason A reason why program is stopped
     */
    public void stopped(int token, Map<String, String> map) {
        String reason = map.get("reason"); // NOI18N
        
        if (state.equals(STATE_STARTING)) {
            setLoading();
            return;
        }
        if (!state.equals(STATE_RUNNING)) {
            log.warning("GdbDebugger.stopped while not in STATE_RUNNING");
            return;
        }
        
        log.fine("GD.stopped[" + Thread.currentThread().getName() + "]:\n"); // NOI18N
        if (reason != null) {
            setCurrentCallStackFrameNoFire(null);   // will be reset when stack updates
            if (reason.equals("exited-normally")) { // NOI18N
                setExited();
                finish(false);
            } else if (reason.equals("breakpoint-hit")) { // NOI18N
                GdbBreakpoint breakpoint;
                gdb.stack_list_frames();
                BreakpointImpl impl = getBreakpointList().get(map.get("bkptno")); // NOI18N
                if (impl != null && (breakpoint = impl.getBreakpoint()) != null) {
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
                    finish(false);
                }
            } else if (reason.equals("end-stepping-range")) { // NOI18N
                gdb.stack_list_frames();
                setStopped();
                if (GdbTimer.getTimer("Step").getSkipCount() == 0) { // NOI18N
                    GdbTimer.getTimer("Step").stop("Step1");// NOI18N
                    GdbTimer.getTimer("Step").report("Step1");// NOI18N
                }
            } else if (reason.equals("signal-received")) { // NOI18N
                if (getState().equals(STATE_RUNNING)) {
                    String thread_id = map.get("thread-id"); // NOI18N
                    if (thread_id != null && !thread_id.equals(currentThread)) {
                        gdb.thread_select(currentThread);
                    }
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
    
    public void addPendingBreakpoint(int token, BreakpointImpl impl) {
        pendingBreakpointMap.put(new Integer(token), impl);
    }
    
    /**
     * Callback method for break_insert Gdb/MI command.
     *
     * @param reason a reason why program is stopped
     */
    private void breakpointValidation(int token, Object o) {
        BreakpointImpl impl = pendingBreakpointMap.get(Integer.valueOf(token));
        
        if (impl != null) { // impl is null for the temporary bp set at main during startup
            if (o instanceof String) {
                impl.addError((String) o);
            } else if (o instanceof Map || o == null) {
                pendingBreakpointMap.remove(Integer.valueOf(token));
                impl.completeValidation((Map<String, String>) o);
                if (o != null && impl.getBreakpoint().isEnabled()) {
                    Map<String, String> map = (Map) o;
                    String fullname = map.get("fullname"); // NOI18N
                    String file = map.get("file"); // NOI18N
                    String line = map.get("line"); // NOI18N
                    if (firstBPfullname != null && firstBPfullname.equals(fullname) &&
                            firstBPline != null && firstBPline.equals(line)) {
                        continueAfterFirstStop = false;
                    } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC &&
                            firstBPfile != null && firstBPfile.equals(file) &&
                            firstBPline != null && firstBPline.equals(line)) {
                        continueAfterFirstStop = false;
                    }
                }
            }
            if (pendingBreakpointMap.isEmpty() && state.equals(STATE_LOADING)) {
                setReady();
            }
        } else if (o instanceof Map) { // first breakpoint
            Map<String, String> map = (Map) o;
            String number = map.get("number"); // NOI18N
            String fullname = map.get("fullname"); // NOI18N
            String file = map.get("file"); // NOI18N
            String line = map.get("line"); // NOI18N
            String func = map.get("func"); // NOI18N
            if (number != null && ((number.equals("1")) || // NOI18N
                   (number.equals("2") && func != null && func.equals("WinMain") && Utilities.isWindows()))) { // NOI18N
                firstBPfullname = fullname;
                firstBPfile = file;
                firstBPline = line;
            }
        }
    }
     
    /**
     * This utility method helps to start a new Cnd debugger session. 
     *
     * @param hostName a name of computer to attach to
     * @param portNumber a port number
     */
    public static void attach(String pid, ProjectInformation pinfo) throws DebuggerStartException {
        Project project = pinfo.getProject();
        ConfigurationDescriptorProvider cdp =
                (ConfigurationDescriptorProvider) project.getLookup().
                lookup(ConfigurationDescriptorProvider.class);
        if (cdp != null) {
            MakeConfigurationDescriptor mcd =
                    (MakeConfigurationDescriptor) cdp.getConfigurationDescriptor();
            MakeConfiguration conf = (MakeConfiguration) mcd.getConfs().getActive();
            MakeArtifact ma = new MakeArtifact(mcd, conf);
            String path = ma.getOutput();
            ProjectActionEvent pae = new ProjectActionEvent(
                    project,
                    DEBUG_ATTACH,
                    pinfo.getDisplayName(),
                    path,
                    conf,
                    null,
                    false);
            DebuggerEngine[] es = DebuggerManager.getDebuggerManager().startDebugging(
                    DebuggerInfo.create(SESSION_PROVIDER_ID, new Object[] { pae, Long.valueOf(pid)}));
            if (es == null) {
               throw new DebuggerStartException(new InternalError()); 
            }
        }
    }
    
    /**
     *  Called when GdbProxy receives the results of a -stack-list-frames command.
     */
    private void stackUpdate(List<String> stack) {
        CallStackFrame frame;
        int i;
        
        for (i = 0; i < stack.size(); i++) {
            String line = stack.get(i);
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
                frame = callstack.get(i);
                frame.setFrameNumber(i);
                frame.set(func, file, fullname, lnum, addr);
            } else {
                frame = new CallStackFrame(this, func, file, fullname, lnum, addr);
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
    
    public void makeThreadActive(String tline) {
        if (tline.length() > 0) {
            if (Character.isDigit(tline.charAt(0))) {
                int idx = tline.indexOf(' ');
                if (idx > 0) {
                    gdb.thread_select(tline.substring(0, idx));
                }
            }
        }
    }
    
    /**
     * Returns list of cached local variables for this debugger. This typically gets
     * called from an evaluator thread. If we don't have the type, it should be coming
     * on the GdbReaderRP thread so we wait for it.
     *
     * @return list of local variables
     */
    @SuppressWarnings("unchecked")
    public List<GdbVariable> getLocalVariables() {
        assert !(Thread.currentThread().getName().equals("GdbReaderRP"));
        synchronized (localVariables) {
            for (GdbVariable var : localVariables) {
                if (var.getType() == null) {
                    for (int i = 0; i < 40; i++) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                        }
                        if (var.getType() != null) {
                            break;
                        }
                    }
                }
            }
            return (List<GdbVariable>) localVariables.clone();
        }
    }
    
    public void requestDerefValue(AbstractVariable var, String name) {
        if (state.equals(STATE_STOPPED)) {
            int token = gdb.data_evaluate_expression(name);
            derefValueMap.put(new Integer(token), var);
        } else {
            var.setDerefValue("");
        }
    }
    
    public void requestWatchValue(GdbWatchVariable var) {
        if (state.equals(STATE_STOPPED) || !watchValueMap.isEmpty()) {
            int token = evaluate(var.getName());
            watchValueMap.put(new Integer(token), var);
        }
    }
    
    public int evaluate(String expression) {
        int token;
        if (expression.indexOf('(') != -1) {
            suspendBreakpointsAndSignals();
            token = gdb.data_evaluate_expression('"' + expression + '"'); // NOI18N
            restoreBreakpointsAndSignals();
        } else {
            token = gdb.data_evaluate_expression('"' + expression + '"'); // NOI18N
        }
        return token;
    }
    
    public void requestWatchType(GdbWatchVariable var) {
        if (state.equals(STATE_STOPPED) && var.getName().length() > 0) {
            int token = gdb.whatis(var.getName());
            var.clearTypeBuf();
            watchTypeMap.put(new Integer(token), var);
        }
    }
    
    /**
     * Suspend all breakpoints. This is used to suspend breakpoints during Watch
     * updates so functions called don't stop.
     */
    private void suspendBreakpointsAndSignals() {
        for (BreakpointImpl impl : getBreakpointList().values()) {
            if (impl.getBreakpoint().isEnabled()) {
                gdb.break_disable(impl.getBreakpointNumber());
            }
        }
        gdb.set_unwindonsignal("on"); // NOI18N
    }
    
    /**
     * Resume all breakpoints. This is used to re-enable breakpoints after a Watch
     * update.
     */
    private void restoreBreakpointsAndSignals() {
        gdb.set_unwindonsignal("off"); // NOI18N
        for (BreakpointImpl impl : getBreakpointList().values()) {
            if (impl.getBreakpoint().isEnabled()) {
                gdb.break_enable(impl.getBreakpointNumber());
            }
        }
    }
    
    /**
     * Returns call stack for this debugger.
     *
     * @return call stack
     */
    public ArrayList<CallStackFrame> getCallStack() {
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
                frames[i] = callstack.get(from + i);
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
                currentCallStackFrame = callstack.get(0);
            }
        } else {
            currentCallStackFrame = null;
        }
        return currentCallStackFrame;
    }
    
    private Object getType(String key) {
        CallStackFrame csf = getCurrentCallStackFrame();
        if (csf != null) {
            return csf.getType(key);
        }
        return null;
    }
    
    private void addType(String key, Object o) {
        CallStackFrame csf = getCurrentCallStackFrame();
        if (csf != null) {
            csf.addType(key, o);
        }
    }
    
    /**
     * Sets a stack frame current.
     *
     * @param Frame to make current (or null)
     */
    public void setCurrentCallStackFrame(CallStackFrame callStackFrame){
        if (isValidStackFrame(callStackFrame)) {
            CallStackFrame old = setCurrentCallStackFrameNoFire(callStackFrame);
            updateLocalVariables(callStackFrame.getFrameNumber());
            if (old == callStackFrame) {
                return;
            }
            pcs.firePropertyChange(PROP_CURRENT_CALL_STACK_FRAME, old, callStackFrame);
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                           "ERR_InvalidCallStackFrame"))); // NOI18N
            
        }
    }
    
    public boolean isValidStackFrame(CallStackFrame csf) {
        return csf.getFileName() != null && csf.getFullname() != null && csf.getFunctionName() != null;
    }
    
    private int getCurrentCallStackIndex() {
        int idx = 0;
        for (CallStackFrame f : callstack) {
            if (f == currentCallStackFrame) {
                return idx;
            }
            idx++;
        }
        return idx;
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
    
    public void popTopmostCall() {
        if (callstack.size() > 0 && isValidStackFrame(callstack.get(1))) {
            gdb.stack_select_frame(0);
            gdb.exec_finish();
        } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                           "ERR_InvalidCallStackFrame"))); // NOI18N
        }
    }
    
    public Map<String, BreakpointImpl> getBreakpointList() {
        return breakpointList;
    }
    
    /**
     *  Gdb/mi doesn't handle spaces in paths (see http://sourceware.org/ml/gdb/2006-02/msg00283.html
     *  for more details). So try an alternate if the path has embedded spaces.
     *
     *  @param path The absolute path to convert
     *  @return The possibly modified path
     */
    public String getBestPath(String path) {
        if (path.indexOf(' ') == -1 && Utilities.getOperatingSystem() != Utilities.OS_MAC) {
            return path;
        } else if (path.startsWith(runDirectory)) {
            return (path.substring(runDirectory.length()));
        } else {
            int pos = path.lastIndexOf('/');
            if (pos != -1) {
                return path.substring(pos + 1);
            }
            // Don't delete the following code yet. Neet to understand breakpoints outside the current
            // project better! This might still be relevant.
//            String rdir;
//            if (runDirectory.endsWith("/")) { // NOI18N
//                rdir = runDirectory.substring(0, runDirectory.length() - 1);
//            } else {
//                rdir = runDirectory;
//            }
//            int rdir_pos = rdir.indexOf('/');
//            int path_pos = path.indexOf('/');
//            int match = -1;
//            while (rdir_pos == path_pos && rdir_pos != -1) {
//                if (path.substring(0, rdir_pos).equals(rdir.substring(0, rdir_pos))) {
//                    match = rdir_pos;
//                }
//                rdir_pos = rdir.indexOf('/', rdir_pos + 1);
//                path_pos = path.indexOf('/', path_pos + 1);
//            }
//            if (match != -1) {
//                path_pos = path.substring(0, path_pos).lastIndexOf('/'); // we want the previous path_pos
//                int count = 1;
//                while (rdir_pos != -1) {
//                    count++;
//                    rdir_pos = rdir.indexOf('/', rdir_pos + 1);
//                }
//                StringBuilder rpath = new StringBuilder();
//                while (count-- > 0) {
//                    rpath.append("../");
//                }
//                return rpath.toString()  + path.substring(path_pos + 1);
//            }
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
        return true;
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
