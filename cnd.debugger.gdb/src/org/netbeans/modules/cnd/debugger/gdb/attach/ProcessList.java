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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb.attach;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.regex.Pattern;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.cnd.debugger.gdb.actions.AttachTableColumn;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Get a process list for the GdbAttachPanel. This needs to be platform neutral and
 * provide enough information so we can attach on <b>all</b> platforms.
 * 
 * @author gordonp
 */
final class ProcessList {
    private static enum PTYPE {
        UNINITIALIZED,
        NONE,
        STD,
        WINDOWS;
    }

    private PTYPE ptype = PTYPE.UNINITIALIZED;
    private String executable;
    private final List<String> argsSimple = new ArrayList<String>();
    private final ExecutionEnvironment exEnv;

    private final RequestProcessor RP = new RequestProcessor("Process list", 5); //NOI18N

    protected ProcessList(ExecutionEnvironment exEnv) {
        this.exEnv = exEnv;
    }

    private void init() throws IllegalStateException {
        String exec = "";
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(exEnv);
            if (!hostInfo.getOSFamily().isUnix()) {
                if (exEnv.isRemote()) {
                    throw new IllegalStateException("Remote windows machines are not supported yet"); // NOI18N
                }
                File file = new File(CompilerSetUtils.getCygwinBase() + "/bin", "ps.exe"); // NOI18N
                if (!file.exists()) {
                    file = new File(CompilerSetUtils.getCommandFolder(null), "ps.exe"); // NOI18N
                }
                if (file.exists()) {
                    exec = file.getAbsolutePath();
                    ptype = PTYPE.WINDOWS;
                } else {
                    ptype = PTYPE.NONE;
                }
            } else {
                if (HostInfoUtils.fileExists(exEnv, "/bin/ps")) { // NOI18N
                    exec = "/bin/ps"; // NOI18N
                } else if (HostInfoUtils.fileExists(exEnv, "/usr/bin/ps")) { // NOI18N
                    exec = "/usr/bin/ps"; // NOI18N
                } else {
                    ptype = PTYPE.NONE;
                }
                if (exec.length() > 0) {
                    String user = exEnv.getUser();
                    // Request only user's processes (see IZ 176371)
                    if (user != null && user.length() > 0) {
                        argsSimple.add("-u"); // NOI18N
                        argsSimple.add(user);
                    } else {
                        argsSimple.add("-a"); // NOI18N
                    }
                    argsSimple.add("-o"); // NOI18N
                    // Used only localy, so we can use Utilities.getOperatingSystem()
                    if (hostInfo.getOSFamily() == HostInfo.OSFamily.MACOSX) {
                        argsSimple.add("user,pid,ppid,stime,time,command"); // NOI18N
                    } else {
                        argsSimple.add("user,pid,ppid,stime,time,args"); // NOI18N
                    }
                    ptype = PTYPE.STD;
                }
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            ptype = PTYPE.NONE;
        } catch (CancellationException caex) {
            ptype = PTYPE.NONE;
        } catch (InterruptedException ex) {
            // don't log InterruptedException
            ptype = PTYPE.NONE;
        }
        executable = exec;
    }

    private void request(final Pattern filter, final ProcessListReader plr, final boolean full) {
        if (ptype != PTYPE.NONE) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ptype == PTYPE.UNINITIALIZED) {
                            init();
                        }
                        if (ptype == PTYPE.NONE) {
                            plr.processListCallback(Collections.<Vector<String>>emptyList());
                            return;
                        }
                        List<String> args = new ArrayList<String>(argsSimple);
                        if (full) {
                            if (ptype == PTYPE.WINDOWS) {
                                args.add("-W"); // NOI18N
                            } else if (ptype == PTYPE.STD) {
                                args.add("-A"); // NOI18N
                            }
                        }
                        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(exEnv);
                        npb.setExecutable(executable);
                        npb.setArguments(args.toArray(new String[args.size()]));
                        npb.redirectError();
                        NativeProcess process = npb.call();
                        BufferedReader reader = ProcessUtils.getReader(process.getInputStream(), exEnv.isRemote());
                        String line = reader.readLine(); // read and ignore header line...
                        List<Vector<String>> proclist = new ArrayList<Vector<String>>();
                        while ((line = reader.readLine()) != null) {
                            if (filter == null || filter.matcher(line).find()) {
                                proclist.add(parseLine(line));
                            }
                        }
                        plr.processListCallback(proclist);
                    } catch (IOException ioe) {
                        //do nothing
                    }
                }
            });
        } else {
            plr.processListCallback(Collections.<Vector<String>>emptyList());
        }
    }

    private Vector<String> parseLine(String line) {
        Vector<String> row = new Vector<String>();
        StringTokenizer tokenizer = new StringTokenizer(line);
        if (isWindowsPsFound()) {
            while (tokenizer.hasMoreTokens()) {
                row.add(tokenizer.nextToken());
            }
            return reorderWindowsProcLine(row);
        } else if (isStd()) {
            int i = 0;
            StringBuilder sb = new StringBuilder();
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                // Combine args
                if (++i <= 5) {
                    row.add(token);
                } else {
                    sb.append(token);
                    sb.append(' ');
                }
            }
            if (sb.length() > 0) {
                row.add(sb.toString());
            }
            return row;
        }
        return row;
    }

    private static Vector<String> reorderWindowsProcLine(Vector<String> oldrow) {
        StringBuilder tmp = new StringBuilder();
        Vector<String> nurow = new Vector<String>(oldrow.size() - 2);
        String status = oldrow.get(0);
        String stime;
        int i;

        if (status.length() == 1 && (status.equals("I") ||  status.equals("C") || status.equals("O"))) { // NOI18N
            // The status field is optional...
            nurow.add(0, oldrow.get(6));  // UID
            nurow.add(1, oldrow.get(4));  // WINPID
            nurow.add(2, oldrow.get(1));  // PID
            nurow.add(3, oldrow.get(2));  // PPID
            nurow.add(4, oldrow.get(7));  // STIME
            stime = oldrow.get(7);
            if (Character.isDigit(stime.charAt(0))) {
                i = 8;
            } else {
                stime = stime + ' ' + oldrow.get(8);
                i = 9;
            }
            nurow.add(4, stime);  // STIME
            while (i < oldrow.size()) {
                tmp.append(oldrow.get(i++));
            }
        } else {
            nurow.add(0, oldrow.get(5));  // UID
            nurow.add(1, oldrow.get(3));  // WINPID
            nurow.add(2, oldrow.get(0));  // PID
            nurow.add(3, oldrow.get(1));  // PPID
            stime = oldrow.get(6);
            if (Character.isDigit(stime.charAt(0))) {
                i = 7;
            } else {
                stime = stime + ' ' + oldrow.get(7);
                i = 8;
            }
            nurow.add(4, stime);  // STIME
            while (i < oldrow.size()) {
                tmp.append(oldrow.get(i++));
            }
        }
        nurow.add(5, tmp.toString());  // CMD
        return nurow;
    }

    void requestSimple(Pattern filter, ProcessListReader plr) {
        request(filter, plr, false);
    }

    void requestFull(Pattern filter, ProcessListReader plr) {
        request(filter, plr, true);
    }

    private boolean isStd() {
        return ptype == PTYPE.STD;
    }
    
    private boolean isWindowsPsFound() {
        return ptype == PTYPE.WINDOWS;
    }

    protected List<AttachTableColumn> getColumnHeaders() {
        List<AttachTableColumn> headers = new ArrayList<AttachTableColumn>();
        
        if (ptype == PTYPE.STD) {
            headers.add(new AttachTableColumn("user",  // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_USER"))); // NOI18N
            headers.add(new AttachTableColumn("pid", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_PID"))); // NOI18N
            headers.add(new AttachTableColumn("ppid", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_PPID"))); // NOI18N
            headers.add(new AttachTableColumn("stime", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_STIME"))); // NOI18N
            headers.add(new AttachTableColumn("time", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_TIME"))); // NOI18N
            headers.add(new AttachTableColumn("args", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_ARGS"))); // NOI18N
        } else if (ptype == PTYPE.WINDOWS) {
            headers.add(new AttachTableColumn("uid",  // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_UID"))); // NOI18N
            headers.add(new AttachTableColumn("winpid", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_WINPID"))); // NOI18N
            headers.add(new AttachTableColumn("pid", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_PID"))); // NOI18N
            headers.add(new AttachTableColumn("ppid", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_PPID"))); // NOI18N
            headers.add(new AttachTableColumn("stime", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_STIME"))); // NOI18N
            headers.add(new AttachTableColumn("args", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_ARGS"))); // NOI18N
        }
        return headers;
    }
    
    protected String getArgsHeader() {
        return NbBundle.getMessage(ProcessList.class, "HDR_ARGS");
    }
}
