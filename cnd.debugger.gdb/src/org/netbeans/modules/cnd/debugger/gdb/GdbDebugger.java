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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.AddressBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.BreakpointImpl;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.disassembly.Disassembly;
import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointEvent;
import org.netbeans.modules.cnd.debugger.gdb.expr.Expression;
import org.netbeans.modules.cnd.debugger.gdb.profiles.GdbProfile;
import org.netbeans.modules.cnd.debugger.gdb.proxy.GdbMiDefinitions;
import org.netbeans.modules.cnd.debugger.gdb.proxy.GdbProxy;
import org.netbeans.modules.cnd.debugger.gdb.timer.GdbTimer;
import org.netbeans.modules.cnd.debugger.gdb.utils.CommandBuffer;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

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
    public static final String          PROP_KILLTERM = "killTerm"; // NOI18N
    public static final String          PROP_SHARED_LIB_LOADED = "sharedLibLoaded"; // NOI18N

    public static final String          STATE_NONE = "state_none"; // NOI18N
    public static final String          STATE_STARTING = "state_starting"; // NOI18N
    public static final String          STATE_LOADING = "state_loading"; // NOI18N
    public static final String          STATE_LOADED = "state_loaded"; // NOI18N
    public static final String          STATE_READY = "state_ready"; // NOI18N
    public static final String          STATE_RUNNING = "state_running"; // NOI18N
    public static final String          STATE_STOPPED = "state_stopped"; // NOI18N
    public static final String          STATE_SILENT_STOP = "state_silent_stop"; // NOI18N
    public static final String          STATE_EXITED  = "state_exited"; // NOI18N
    
    private static final int            DEBUG_ATTACH = 999;
    
    /* Some breakpoint flags used only on Windows XP (with Cygwin) */
    public static final int             GDB_TMP_BREAKPOINT = GdbBreakpoint.SUSPEND_ALL + 1;
    
    /** ID of GDB Debugger Engine for C */
    public static final String          ENGINE_ID = "netbeans-cnd-GdbSession/C"; // NOI18N

    /** ID of GDB Debugger Session */
    public static final String          SESSION_ID = "netbeans-cnd-GdbSession"; // NOI18N

    /** ID of GDB Debugger SessionProvider */
    public static final String          SESSION_PROVIDER_ID = "netbeans-cnd-GdbSessionProvider"; // NOI18N
    
    /** Dis update */
    public static final String          DIS_UPDATE = "dis_update"; // NOI18N
    
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
    private double gdbVersion = 6.4;
    private boolean continueAfterFirstStop = true;
    private ArrayList<GdbVariable> localVariables = new ArrayList<GdbVariable>();
    private Map<Integer, BreakpointImpl> pendingBreakpointMap = new HashMap<Integer, BreakpointImpl>();
    private Map<String, BreakpointImpl> breakpointList = Collections.synchronizedMap(new HashMap<String, BreakpointImpl>());
    private static Map<String, TypeInfo> ticache = new HashMap<String, TypeInfo>();
    private static Logger log = Logger.getLogger("gdb.logger"); // NOI18N
    private int currentToken = 0;
    private String currentThreadID = "1"; // NOI18N
    private static final String[] emptyThreadsList = new String[0];
    private String[] threadsList = emptyThreadsList;
    private Timer startupTimer = null;
    private boolean cygwin = false;
    private boolean cplusplus = false;
    private String firstBPfullname;
    private String firstBPfile;
    private String firstBPline;
    private InputOutput iotab;
    private boolean firstOutput;
    private boolean dlopenPending;
    private String lastShare;
    private int shareToken;
    private final Disassembly disassembly;
    private GdbBreakpoint currentBreakpoint = null;
        
    public GdbDebugger(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        pcs = new PropertyChangeSupport(this);
        firstOutput = true;
        dlopenPending = false;
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
        threadsViewInit();
        disassembly = new Disassembly(this);
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
            pae = lookupProvider.lookupFirst(null, ProjectActionEvent.class);
            iotab = lookupProvider.lookupFirst(null, InputOutput.class);
            if (iotab != null) {
                iotab.setErrSeparated(false);
            }
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
//            String gdbCommand = profile.getGdbPath(profile.getGdbCommand(), pae.getProfile().getRunDirectory());
            String gdbCommand = profile.getGdbPath((MakeConfiguration)pae.getConfiguration());
            if (gdbCommand.toLowerCase().contains("cygwin")) { // NOI18N
                cygwin = true;
            }
            gdb = new GdbProxy(this, gdbCommand, pae.getProfile().getEnvironment().getenv(),
                    runDirectory, termpath);
            gdb.gdb_version();
            gdb.environment_directory(runDirectory);
            gdb.gdb_show("language"); // NOI18N
            gdb.gdb_set("print repeat",  // NOI18N
                    Integer.toString(CppSettings.getDefault().getArrayRepeatThreshold()));
            gdb.data_list_register_names("");
            if (pae.getID() == DEBUG_ATTACH) {
                programPID = lookupProvider.lookupFirst(null, Long.class);
                CommandBuffer cb = new CommandBuffer();
                gdb.target_attach(cb, Long.toString(programPID));
                cb.waitForCompletion();
                String err = cb.getError();
                if (err != null || cb.timedOut()) {
                    final String msg;
                    if (err == null) {
                        msg = NbBundle.getMessage(GdbDebugger.class, "ERR_AttachTimeout"); // NOI18N
                    } else if (err.toLowerCase().contains("no such process") || // NOI18N
                            err.toLowerCase().contains("couldn't open /proc file for process ")) { // NOI18N
                        msg = NbBundle.getMessage(GdbDebugger.class, "ERR_NoSuchProcess"); // NOI18N
                    } else {
                        msg = NbBundle.getMessage(GdbDebugger.class, "ERR_CantAttach"); // NOI18N
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                            setExited();
                            finish(false);
                        }
                    });
                } else {
                    final String path = getFullPath(runDirectory, pae.getExecutable());

                    // 1) see if path was explicitly loaded by target_attach (this is system dependent)
                    if (!symbolsRead(cb.toString(), path)) {
                        // 2) see if we can validate via /proc (or perhaps other platform specific means)
                        if (validAttachViaSlashProc(programPID, path)) { // Linux or Solaris
                            if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS) {
                                gdb.file_symbol_file(path);
                            }
                            setLoading();
                        } else {
                            // 3) send an "info files" command to gdb. Its response should say what symbols
                            // are read.
                            cb = new CommandBuffer();
                            gdb.info_files(cb);
                            cb.waitForCompletion();
                            if (symbolsReadFromInfoFiles(cb.toString(), path)) {
                                setLoading();
                            } else {
                                final String msg = NbBundle.getMessage(GdbDebugger.class, "ERR_AttachValidationFailure"); // NOI18N
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                                        setExited();
                                        finish(false);
                                    }
                                });
                            }
                        }
                    } else {
                        setLoading();
                    }
                }
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
                    lookupProvider.lookupFirst(null, Session.class).kill();
                }
                if (Utilities.isWindows()) {
                    CommandBuffer cb = new CommandBuffer();
                    gdb.info_threads(cb); // we get the PID from this...
                    String msg = cb.waitForCompletion();
                    if (msg.startsWith("* 1 thread ")) { // NOI18N
                        int pos = msg.indexOf('.');
                        if (pos > 0) {
                            try {
                                programPID = Long.valueOf(msg.substring(11, pos));
                            } catch (NumberFormatException ex) {
                                log.warning("Failed to get PID from \"info threads\""); // NOI18N
                            }
                        }
                    }
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
        
    private String getFullPath(String rundir, String path) {
        if (Utilities.isWindows() && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':') {
            return path;
        } else if (Utilities.isUnix() && path.charAt(0) == '/') {
            return path;
        } else {
            return rundir + '/' + path;
        }
    }
    
    public void showCurrentSource() {
        final CallStackFrame csf = getCurrentCallStackFrame();
        if (csf == null) {
            return;
        }
        final boolean inDis = (currentBreakpoint == null) ? Disassembly.isInDisasm() : (currentBreakpoint instanceof AddressBreakpoint);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // show current line
                EditorContextBridge.showSource(csf, inDis);
            }
        });
    }
    
    public String[] getThreadsList() {
        if (state.equals(STATE_STOPPED)) {
            if (threadsList == emptyThreadsList) {
                while (gdb == null) {
                    try {
                        Thread.sleep(100); // called before session startup had completed...
                    } catch (InterruptedException ex) {
                    }
                }
                CommandBuffer cb = new CommandBuffer();
                gdb.info_threads(cb);
                String results = cb.waitForCompletion();
                if (results.length() > 0) {
                    List<String> list = new ArrayList<String>();
                    StringBuilder sb = new StringBuilder();
                    for (String line : results.split("\\\\n")) { // NOI18N
                        if (line.startsWith("    ")) { // NOI18N
                            sb.append(" " + line.replace("\\n", "").trim()); // NOI18N
                        } else {
                            if (sb.length() > 0) {
                                list.add(sb.toString());
                                sb.delete(0, sb.length());
                            }
                            line = line.trim();
                            char ch = line.charAt(0);
                            if (ch == '*' || Character.isDigit(ch)) {
                                sb.append(line);
                            }
                        }
                    }
                    if (sb.length() > 0) {
                        list.add(sb.toString());
                    }
                    threadsList = list.toArray(new String[list.size()]);
                    return threadsList;
                }
            }
        } else {
            return new String[] {
                NbBundle.getMessage(GdbDebugger.class, "CTL_NoThreadInfoWhileRunning") // NOI18N
            };
        }
        return threadsList;
    }

    public int getThreadCount() {
        return 1;
    }
    
    private void resetThreadInfo() {
        threadsList = emptyThreadsList;
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
    
    /** Get the gdb version */
    public double getGdbVersion() {
        return gdbVersion;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PROP_STATE)) {
            if (evt.getNewValue().equals(STATE_LOADING)) {
                CommandBuffer cb = new CommandBuffer();
                shareToken = gdb.info_share();
                cb.setID(shareToken);
            } else if (evt.getNewValue().equals(STATE_READY)) {
                if (Utilities.isWindows()) {
                    gdb.break_insert("dlopen"); // NOI18N
                } else {
                    gdb.gdb_set("stop-on-solib-events", "1"); // NOI18N
                }
                if (continueAfterFirstStop) {
                    continueAfterFirstStop = false;
                    setRunning();
                } else {
                    gdb.stack_list_frames();
                    setStopped();
                }
            } else if (evt.getNewValue() == STATE_STOPPED) {
                updateLocalVariables(0);
                gdb.data_list_register_values("");
                gdb.data_list_changed_registers();
            } else if (evt.getNewValue() == STATE_SILENT_STOP) {
                interrupt();
            } else if (evt.getNewValue() == STATE_RUNNING && 
                    (evt.getOldValue() == STATE_SILENT_STOP ||
                     evt.getOldValue() == STATE_READY))  {
                gdb.exec_continue();
            } else if (evt.getNewValue() == STATE_EXITED) {
                finish(false);
            }
        } else if (evt.getPropertyName().equals(PROP_CURRENT_THREAD)) {
            updateCurrentCallStack();
            updateLocalVariables(0);
            gdb.data_list_register_values("");
            gdb.data_list_changed_registers();
        }
    }
    
    private boolean symbolsRead(String results, String exepath) {
        int pos = -1;
        for (String line : results.split("\\\\n")) { // NOI18N
            if (line.contains("Reading symbols from ") || // NOI18N
                    (Utilities.getOperatingSystem() == Utilities.OS_MAC && line.contains("Symbols from "))) { // NOI18N
                if (Utilities.isWindows() && (pos = line.indexOf("/cygdrive/")) != -1) { // NOI18N
                    line = line.substring(0, pos) +
                            line.substring(pos + 10,pos + 11).toUpperCase() + ':' + line.substring(pos + 11);
                }
                String ep = line.substring(21, line.length() - 8);
                if (ep.equals(exepath) || (Utilities.isWindows() && ep.equals(exepath + ".exe"))) { // NOI18N
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean symbolsReadFromInfoFiles(String results, String exepath) {
        for (String line : results.split("\\\\n")) { // NOI18N
            if (line.contains("Symbols from ")) { // NOI18N
                String ep = line.substring(15, line.length() - 3);
                if (ep.equals(exepath)) { // NOI18N
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check that the executable matches the pid. This is system dependent and doesn't necessarily cause
     * an attach failure if we can't validate.
     * 
     * @return true if the project matches the attached to executable
     */
    private boolean validAttachViaSlashProc(long pid, String exepath) {
        if (!Utilities.isWindows()) {
            String procdir = "/proc/" + Long.toString(pid); // NOI18N
            File pathfile = new File(procdir, "path/a.out"); // NOI18N - Solaris only?
            if (!pathfile.exists()) {
                pathfile = new File(procdir, "exe"); // NOI18N - Linux?
            }
            if (pathfile.exists()) {
                File exefile = new File(exepath);
                if (exefile.exists()) {
                    String path = getPathFromSymlink(pathfile.getAbsolutePath());
                    if (path.equals(exefile.getAbsolutePath())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private String getPathFromSymlink(String apath) {
        SymlinkCommand slink = new SymlinkCommand(apath);
        return slink.getPath();
    }
    
    private static class SymlinkCommand {
        
        private String path;
        private ProcessBuilder pb;
        private String linkline;
        
        SymlinkCommand(String path) {
            this.path = path;
            linkline = null;
            File file = new File("/bin/ls"); // NOI18N
            
            if (file.exists()) {
                List<String> list = new ArrayList<String>();
                list.add("/bin/ls"); // NOI18N
                list.add("-l"); // NOI18N
                list.add(path);
                pb = new ProcessBuilder(list);
                pb.redirectErrorStream(true);
            } else {
                pb = null;
            }
        }
        
        public String getPath() {
            if (pb != null) {
                try {
                    Process process = pb.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = br.readLine(); // just read 1st line...
                    br.close();
                    int pos = line.indexOf("->"); // NOI18N
                    if (pos > 0) {
                        return line.substring(pos + 2).trim();
                    }
                } catch (IOException ioe) {
                }
                
            }
            return linkline;
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
    public void finish(boolean killTerm) {
        if (!state.equals(STATE_NONE)) {
            if (killTerm) {
                firePropertyChange(PROP_KILLTERM, true, false);
            }
            if (gdb != null) {
                if (state.equals(STATE_RUNNING)) {
                    ProjectActionEvent pae = lookupProvider.lookupFirst(null, ProjectActionEvent.class);
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
            Disassembly.close();
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
    
    private void updateCurrentCallStack() {
        gdb.stack_list_frames();
    }
    
    /** Handle geb responses starting with '^' */
    public void resultRecord(int token, String msg) {
        CommandBuffer cb;
        Integer itok = Integer.valueOf(token);
        
        currentToken = token + 1;
        if (msg.startsWith("^done,bkpt=")) { // NOI18N (-break-insert)
            msg = msg.substring(12, msg.length() - 1);
            breakpointValidation(token, GdbUtils.createMapFromString(msg));
            if (getState().equals(STATE_SILENT_STOP) && pendingBreakpointMap.isEmpty()) {
                setRunning();
            }
        } else if (msg.startsWith("^done,stack=")) { // NOI18N (-stack-list-frames)
            if (state.equals(STATE_STOPPED)) { // Ignore data if we've resumed running
                stackUpdate(GdbUtils.createListFromString((msg.substring(13, msg.length() - 1))));
            }
        } else if (msg.startsWith("^done,locals=")) { // NOI18N (-stack-list-locals)
            if (state.equals(STATE_STOPPED)) { // Ignore data if we've resumed running
                addLocalsToLocalVariables(msg.substring(13));
            } else {
                log.finest("GD.resultRecord: Skipping results from -stack-list-locals (not stopped)");
            }
        } else if (msg.startsWith("^done,stack-args=")) { // NOI18N (-stack-list-arguments)
            if (state.equals(STATE_STOPPED)) { // Ignore data if we've resumed running
                addArgsToLocalVariables(msg.substring(17));
            } else {
                log.finest("GD.resultRecord: Skipping results from -stack-list-arguments (not stopped)");
            }
        } else if (msg.startsWith("^done,new-thread-id=")) { // NOI18N (-thread-select)
            String tid = msg.substring(21, msg.indexOf('"', 22));
            if (!tid.equals(currentThreadID)) {
                String otid = currentThreadID;
                currentThreadID = tid;
                log.finest("GD.resultRecord: Thread change, firing PROP_CURRENT_THREAD");
                firePropertyChange(PROP_CURRENT_THREAD, otid, currentThreadID);
            }
        } else if (msg.startsWith("^done,value=") && msg.contains("auto; currently c")) { // NOI18N
            if (msg.contains("auto; currently c++")) { // NOI18N
                cplusplus = true;
            }
        } else if (msg.startsWith("^done,value=")) { // NOI18N (-data-evaluate-expression)
            cb = CommandBuffer.getCommandBuffer(itok);
            if (cb != null) {
                cb.append(msg.substring(13, msg.length() - 1));
                cb.done();
            }
        } else if (msg.startsWith("^done,thread-id=") && // NOI18N
                Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            cb = CommandBuffer.getCommandBuffer(itok);
            if (cb != null) {
                cb.done();
            }
        } else if (msg.startsWith(Disassembly.RESPONSE_HEADER)) {
            disassembly.update(msg);
        } else if (msg.startsWith(Disassembly.REGISTER_NAMES_HEADER)) {
            disassembly.updateRegNames(msg);
        } else if (msg.startsWith(Disassembly.REGISTER_VALUES_HEADER)) {
            disassembly.updateRegValues(msg);
        } else if (msg.startsWith(Disassembly.REGISTER_MODIFIED_HEADER)) {
            disassembly.updateRegModified(msg);
        } else if (msg.equals("^done") && getState().equals(STATE_SILENT_STOP)) { // NOI18N
            log.fine("GD.resultRecord[" + token + "]: Got \"^done\" in silent stop");
            setRunning();
        } else if (msg.equals("^done")) { // NOI18N
            cb = CommandBuffer.getCommandBuffer(itok);
            if (cb != null) {
                cb.done();
                if (token == shareToken) {
                    lastShare = cb.toString();
                }
            } else if (pendingBreakpointMap.get(itok) != null) {
                breakpointValidation(token, null);
            }
        } else if (msg.startsWith("^running") && getState().equals(STATE_STOPPED)) { // NOI18N
            setRunning();
        } else if (msg.startsWith("^error,msg=")) { // NOI18N
            msg = msg.substring(11);
            cb = CommandBuffer.getCommandBuffer(itok);
            
            if (cb != null) {
                cb.error(msg);
            } else if (msg.equals("\"Can't attach to process.\"")) { // NOI18N
                DialogDisplayer.getDefault().notify(
                       new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                       "ERR_CantAttach"))); // NOI18N
                (lookupProvider.lookupFirst(null, Session.class)).kill();
            } else if (msg.startsWith("\"No symbol ") && msg.endsWith(" in current context.\"")) { // NOI18N
                String type = msg.substring(13, msg.length() - 23);
                log.warning("Failed type lookup for " + type);
            } else if (msg.equals("\"\\\"finish\\\" not meaningful in the outermost frame.\"")) { // NOI18N
                finish_from_main();
            } else if (msg.contains("(corrupt stack?)")) { // NOI18N
                DialogDisplayer.getDefault().notify(
                       new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                       "ERR_CorruptedStack"))); // NOI18N
                (lookupProvider.lookupFirst(null, Session.class)).kill();
            } else if (msg.contains("error reading line numbers")) { // NOI18N
                DialogDisplayer.getDefault().notify(
                       new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                       "ERR_CantReadLineNumbers"))); // NOI18N
            } else if (msg.contains("No symbol table is loaded")) { // NOI18N
                DialogDisplayer.getDefault().notify(
                       new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                       "ERR_NoSymbolTable"))); // NOI18N
                (lookupProvider.lookupFirst(null, Session.class)).kill();
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
    
    public void fireDisUpdate() {
        firePropertyChange(DIS_UPDATE, 0, 1);
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
        CommandBuffer cb = CommandBuffer.getCommandBuffer(Integer.valueOf(token));
        String msg;
        
        if (omsg.endsWith("\\n")) { // NOI18N
            msg = omsg.substring(0, omsg.length() - 2);
        } else {
            msg = omsg;
        }
        if (cb != null) {
            cb.append(omsg);
        } else if (msg.startsWith("GNU gdb ") && startupTimer != null) { // NOI18N
            // Cancel the startup timer - we've got our first response from gdb
            startupTimer.cancel();
            startupTimer = null;
            
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
            if (getState().equals(STATE_SILENT_STOP) && pendingBreakpointMap.isEmpty()) {
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
        } else if (msg.startsWith("Stopped due to shared library event")) { // NOI18N
            dlopenPending = true;
        }
    }
    
    /** Handle gdb responses starting with '&' */
    public void logStreamOutput(String msg) {
        if (msg.startsWith("&\"No source file named ")) {  // NOI18N
            breakpointValidation(currentToken, msg.substring(2, msg.length() - 3));
        } else if (msg.startsWith("&\"info proc") || // NOI18N
                msg.startsWith("&\"info threads") || // NOI18N
                msg.startsWith("&\"directory ") || // NOI18N
                msg.startsWith("&\"set new-console") || // NOI18N
                msg.startsWith("&\"whatis ") || // NOI18N
                msg.startsWith("&\"warning: Temporarily disabling breakpoints for unloaded shared library") || // NOI18N
                msg.contains("/usr/lib/ld.so")) { // NOI18N
            // ignore these messages
        } else {
            log.finest("GD.logStreamOutput: " + msg); // NOI18N
        }
    }
    
    /** Handle gdb responses starting with '+' */
    public void statusAsyncOutput(int token, String msg) {
      log.finest("GD.statusAsyncOutput[" + token + "]: " + msg); // NOI18N
    }
    
    /** Handle gdb responses starting with '=' */
    public void notifyAsyncOutput(int token, String msg) {
        log.finest("GD.notifyAsyncOutput[" + token + "]: " + msg); // NOI18N
    }
    
    /** Handle gdb responses starting with '@' */
    public void targetStreamOutput(String msg) {
       log.finest("GD.targetStreamOutput: " + msg);  // NOI18N
    }
    
    /**
     * Handle gdb output. The only tricking thing here is that most versions of gdb on
     * Solaris output some proc flags to stdout. So for Solaris, I skip the 1st output
     * if it starts with "PR_" (the proc flag header).
     */
    public void output(String msg) {
        if (iotab != null) {
            if (!(firstOutput && Utilities.getOperatingSystem() == Utilities.OS_SOLARIS &&
                    msg.startsWith("PR_"))) { // NOI18N
                firstOutput = false;
                iotab.getOut().println(msg);
            }
        }
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
            log.finest("GD.addArgsToLocalVariables: Starting to add Args to localVariables"); // NOI18N
            synchronized (localVariables) {
                localVariables.addAll(v);
            }
            log.finest("GD.addArgsToLocalVariables: Added " + v.size() + " args");
        }
    }
    
    private void addLocalsToLocalVariables(String info) {
        Collection<GdbVariable> v = GdbUtils.createLocalsList(info.substring(1, info.length() - 1));
        if (!v.isEmpty()) {
            log.finest("GD.addLocalsToLocalVariables: Starting to add locals to localVariables"); // NOI18N
            synchronized (localVariables) {
                for (GdbVariable var : v) {
                    if (!localVariables.contains(var)) {
                        localVariables.add(var);
                    }
                }
            }
            log.finest("GD.addLocalsToLocalVariables: Added " + v.size() + " locals");
        }
    }
    
    public String updateVariable(String name, String value) {
        CommandBuffer cb = new CommandBuffer();
        gdb.data_evaluate_expression(cb, name + '=' + value);
        return cb.waitForCompletion();
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
                killcmd.add((Utilities.isMac() && signal == 2) ? "TRAP" : Integer.toString(signal)); // NOI18N
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
     */
    public void stepI() {
        setState(STATE_RUNNING);
        gdb.exec_instruction();
    }
    
    /**
     * Resumes execution of the inferior program until
     * the top function is exited.
     * Note: Slight cemantic change in NB 6.1. In NB 6.0 the current
     * frame was stepped out of. In NB 6.1, the top frame is stepped
     * out of. This makes the behavior match the Java debugger in NB.
     */
    public void stepOut() {
        if (callstack.size() > 0 || isValidStackFrame(callstack.get(1))) {
            setState(STATE_RUNNING);
            gdb.stack_select_frame(0);
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
        
        log.finest("GD.stopped[" + GdbUtils.threadId() + "]:\n"); // NOI18N
        resetThreadInfo();
        if (reason != null) {
            setCurrentCallStackFrameNoFire(null);   // will be reset when stack updates
            if (reason.equals("exited-normally")) { // NOI18N
                setExited();
                finish(false);
            } else if (reason.equals("breakpoint-hit")) { // NOI18N
                String tid = map.get("thread-id"); // NOI18N
                if (tid != null && !tid.equals(currentThreadID)) {
                    currentThreadID = tid;
                }
                BreakpointImpl impl = getBreakpointList().get(map.get("bkptno")); // NOI18N
                if (impl == null) {
                    String frame = map.get("frame"); // NOI18N
                    if (frame != null && frame.contains("dlopen")) { // NOI18N
                        dlopenPending = true;
                        gdb.exec_finish();
                        return;
                    }
                } else {
                    GdbBreakpoint breakpoint = impl.getBreakpoint();
                    if (breakpoint.getSuspend() == GdbBreakpoint.SUSPEND_NONE) {
                        fireBreakpointEvent(breakpoint, new GdbBreakpointEvent(
                                    breakpoint, this, GdbBreakpointEvent.CONDITION_NONE, null));
                        gdb.exec_continue();
                    } else {
                        updateCurrentCallStack();
                        fireBreakpointEvent(breakpoint, new GdbBreakpointEvent(
                                    breakpoint, this, GdbBreakpointEvent.CONDITION_NONE, null));
                        setStopped();
                    }
                }
                if (dlopenPending) {
                    dlopenPending = false;
                    checkSharedLibs(false);
                }
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
                    String tid = map.get("thread-id"); // NOI18N
                    if (tid != null && !tid.equals(currentThreadID)) {
                        currentThreadID = tid;
                    }
                    gdb.stack_list_frames();
                    setStopped();
                }
            } else if (reason.equals("function-finished") && dlopenPending) { // NOI18N
                dlopenPending = false;
                checkSharedLibs(false);
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
        } else if (dlopenPending) {
                dlopenPending = false;
                checkSharedLibs(true);
        } else {
            gdb.stack_list_frames();
            setStopped();
        }
    }
    
    /**
     * Compare the current set of shared libraries with the previous set. Run in a
     * different thread because we're probably being called from the GdbReaderRP
     * thread and CommandBuffer.waitForCompletion() doesn't work on that thread.
     */
    private void checkSharedLibs(final boolean continueRunning) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                CommandBuffer cb = new CommandBuffer();
                gdb.info_share(cb);
                String share = cb.waitForCompletion();
                if (share.length() > 0 && !share.equals(lastShare)) {
                    if (share.length() > lastShare.length()) {
                        // dlopened a shared library
                        log.fine("GD.checkSharedLibs: Added a shared library");
                        firePropertyChange(PROP_SHARED_LIB_LOADED, lastShare, share);
                        lastShare = share;
                    } else {
                        // dlclosed a shared library
                        log.fine("GD.checkSharedLibs: Closed a shared library");
                    }
                }
                if (continueRunning) {
                    gdb.exec_continue();
                }
            }
        });
    }
    
    private void threadsViewInit() {
        Properties props = Properties.getDefault().getProperties("debugger").getProperties("views"); // NOI18N
        props.getProperties("ThreadState").setBoolean("visible", false); // NOI18N
        props.getProperties("ThreadSuspended").setBoolean("visible", false); // NOI18N
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
            String runDirectory = conf.getProfile().getRunDirectory().replace("\\", "/");  // NOI18N
            String path = runDirectory + '/' + ma.getOutput();
            if (isExecutable(conf, path)) {
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
            } else {
                final String msg = NbBundle.getMessage(GdbDebugger.class, "ERR_AttachValidationFailure"); // NOI18N
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                    }
                });
            }
        }
    }
    
    /**
     * Use various heuristics to verify that either the project produces an executable
     * or that the path is to an executable.
     * 
     * @param conf A Makefile project configuration
     * @param path The absolute pathname to the file
     * @return true iff the input parameters get an executable
     */
    private static boolean isExecutable(MakeConfiguration conf, String path) {
        File file;
        
        if (conf.isApplicationConfiguration()) {
            return true;
        } else if (conf.isMakefileConfiguration()) {
            if (Utilities.isWindows()) {
                if (path.endsWith(".dll")) { // NOI18N
                    return false;
                } else if (!path.endsWith(".exe")) { // NOI18N
                    path = path + ".exe"; // NOI18N
                }
                file = new File(path);
                if (file.exists()) {
                    return true;
                }
            }
            file = new File(path);
            if (file.exists()) {
                String mime_type = FileUtil.getMIMEType(FileUtil.toFileObject(file));
                if (mime_type != null && mime_type.startsWith("application/x-exe")) { // NOI18N
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }
    
    /**
     *  Called when GdbProxy receives the results of a -stack-list-frames command.
     */
    private void stackUpdate(List<String> stack) {
        synchronized(callstack) {
            callstack.clear();

            for (int i = 0; i < stack.size(); i++) {
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
                        log.finest("GD.stackUpdate: Setting fullname from file"); // NOI18N
                    } else {
                        fullname = runDirectory + file;
                        log.finest("GD.stackUpdate: Setting fullname from runDirectory + file"); // NOI18N
                    }
                }

                callstack.add(i, new CallStackFrame(this, func, file, fullname, lnum, addr, i));
            }
        }
        
        if (!stack.isEmpty()) {
            pcs.firePropertyChange(PROP_CURRENT_CALL_STACK_FRAME, 0, 1);
        }
    }
    
    public void setCurrentThread(String tline) {
        if (tline.length() > 0) {
            if (Character.isDigit(tline.charAt(0))) {
                int idx = tline.indexOf(' ');
                if (idx > 0) {
                    resetThreadInfo();
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
    public List<GdbVariable> getLocalVariables() {
        assert !(Thread.currentThread().getName().equals("GdbReaderRP"));
        synchronized (localVariables) {
            return (List<GdbVariable>) localVariables.clone();
        }
    }
    
    public String evaluateToolTip(String expression) {
        CommandBuffer cb = new CommandBuffer();
        
        if (expression.indexOf('(') != -1) {
            suspendBreakpointsAndSignals();
            gdb.data_evaluate_expression(cb, '"' + expression + '"'); // NOI18N
            restoreBreakpointsAndSignals();
        } else {
            gdb.data_evaluate_expression(cb, '"' + expression + '"'); // NOI18N
        }
        String response = cb.waitForCompletion();
        if (response.startsWith("@0x")) { // NOI18N
            cb = new CommandBuffer();
            gdb.print(cb, expression);
            response = cb.waitForCompletion();
            if (response.length() > 0 && response.charAt(0) == '$') {
                int pos = response.indexOf('=');
                if (pos != -1 && (pos + 2) < response.length()) {
                    response = response.substring(pos + 2, response.length()).replace("\\n", "").trim(); // NOI18N
                }
            }
        }
        return response.length() > 0 ? response : null;
    }
    
    public Map<String, TypeInfo> getTypeInfoCache() {
        return ticache;
    }
    
    public String requestValue(String name) {
        assert !Thread.currentThread().getName().equals("GdbReaderRP"); // NOI18N
        
        if (state.equals(STATE_STOPPED)) {
            CommandBuffer cb = new CommandBuffer();
            gdb.data_evaluate_expression(cb, name);
            String info = cb.waitForCompletion();
            if (info.length() == 0 || cb.getState() != CommandBuffer.STATE_OK) {
                if (cb.getState() == CommandBuffer.STATE_ERROR) {
                    log.fine("GD.requestValue[" + cb.getID() + "]: Error [" + cb.getError() + "]"); // NOI18N
                    return '>' + cb.getError() + '<';
                } else {
                    log.fine("GD.requestValue[" + cb.getID() + "]: Failure [" + // NOI18N
                            info.length() + ", " + cb.getState() + "]"); // NOI18N
                    return "";
                }
            } else {
                return info;
            }
        } else {
            return null;
        }
    }
    
    public String requestWhatis(String name) {
        assert !Thread.currentThread().getName().equals("GdbReaderRP"); // NOI18N
        
        if (state.equals(STATE_STOPPED) && name != null && name.length() > 0) {
            CommandBuffer cb = new CommandBuffer();
            gdb.whatis(cb, name);
            String info = cb.waitForCompletion();
            if (info.length() == 0 || cb.getState() != CommandBuffer.STATE_OK) {
                if (cb.getState() == CommandBuffer.STATE_ERROR) {
                    log.fine("GD.requestWhatis[" + cb.getID() + "]: Error [" + cb.getError() + "]"); // NOI18N
//                    return '>' + cb.getError() + '<'; Show error in Value field...
                    return "";
                } else {
                    log.fine("GD.requestWhatis[" + cb.getID() + "]: Failure [" + // NOI18N
                            info.length() + ", " + cb.getState() + "]"); // NOI18N
                    return "";
                }
            } else {
                return info.substring(7, info.length() - 2);
            }
        } else {
            return null;
        }
    }
    
    public String requestSymbolType(String type) {
        assert !Thread.currentThread().getName().equals("GdbReaderRP"); // NOI18N
        
        if (state.equals(STATE_STOPPED) && type != null && type.length() > 0) {
            CommandBuffer cb = new CommandBuffer();
            gdb.symbol_type(cb, type);
            String info = cb.waitForCompletion();
            if (info.length() == 0 || cb.getState() != CommandBuffer.STATE_OK) {
                if (cb.getState() == CommandBuffer.STATE_ERROR) {
                    log.fine("GD.requestSymbolType[" + cb.getID() + "]: Error [" + cb.getError() + "]"); // NOI18N
//                    return '>' + cb.getError() + '<';  Show error in Value field...
                    return "";
                } else {
                    log.fine("GD.requestSymbolType[" + cb.getID() + "]: Failure [" + // NOI18N
                            info.length() + ", " + cb.getState() + "]. Returning original type"); // NOI18N
                    return type;
                }
            } else {
		log.fine("GD.requestSymbolType[" + cb.getID() + "]: " + type + " --> [" + info + "]");
                return info.substring(7, info.length() - 2);
            }
        } else {
            return null;
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
        if (currentCallStackFrame != null) {
            return currentCallStackFrame;
        } else if (!callstack.isEmpty()) {
            return callstack.get(0);
        }
        return null;
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
    
    private CallStackFrame setCurrentCallStackFrameNoFire(CallStackFrame callStackFrame) {
        CallStackFrame old;
        
        synchronized (this) {
            old = getCurrentCallStackFrame();
            if (callStackFrame == old) {
                return callStackFrame;
            }
            currentCallStackFrame = callStackFrame;
        }
        return old;
    }
    
    public boolean isValidStackFrame(CallStackFrame csf) {
        return csf.getFileName() != null && csf.getFullname() != null && csf.getFunctionName() != null;
    }
    
    public boolean isStepOutValid() {
        return callstack.size() == 1 || 
                (callstack.size() > 1 && isValidStackFrame(callstack.get(1)));
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
    
    public int getCurrentToken() {
        return currentToken;
    }
    
    public boolean isCygwin() {
        return cygwin;
    }
    
    public boolean isCplusPlus() {
        return cplusplus;
    }

    public Disassembly getDisassembly() {
        return disassembly;
    }

    public void setCurrentBreakpoint(GdbBreakpoint currentBreakpoint) {
        this.currentBreakpoint = currentBreakpoint;
    }
}
