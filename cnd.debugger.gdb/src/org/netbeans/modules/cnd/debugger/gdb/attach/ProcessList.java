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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.utils.CppUtils;
import org.netbeans.modules.cnd.debugger.gdb.actions.AttachTableColumn;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Get a process list for the GdbAttachPanel. This needs to be platform neutral and
 * provide enough information so we can attach on <b>all</b> platforms.
 * 
 * @author gordonp
 */
final class ProcessList implements Runnable {
    
    public final static int PTYPE_UNINITIALIZED = -1;
    public final static int PTYPE_STD = 0;
    public final static int PTYPE_CYGWIN = 1;
    
    private int ptype = PTYPE_UNINITIALIZED;
    private final List<String> proclist;
    private final ProcessBuilder pb;
    private ProcessListReader plr;

    protected ProcessList(ProcessListReader plr) {
        this.plr = plr;
        List<String> args = getProcessCommand();
        pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        proclist = new ArrayList<String>();
        if (args != null && !args.isEmpty()) {
            RequestProcessor.getDefault().post(this);
        }
    }
    
    public void run() {
        String line;
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            reader.readLine(); // read and ignore header line...
            while ((line = reader.readLine()) != null) {
                proclist.add(line);
            }
            plr.processListCallback(proclist);
        } catch (IOException ioe) {
        }
    }
    
    protected int getPType() {
        return ptype;
    }
    
    protected boolean isStd() {
        return ptype == PTYPE_STD;
    }
    
    protected boolean isCygwin() {
        return ptype == PTYPE_CYGWIN;
    }
    
    protected List<AttachTableColumn> getColumnHeaders() {
        List<AttachTableColumn> headers = new ArrayList<AttachTableColumn>();
        
        if (ptype == PTYPE_STD) {
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
        } else if (ptype == PTYPE_CYGWIN) {
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
    
    private List<String> getProcessCommand() {
        List alist = new ArrayList<String>();
        
        if (Utilities.isWindows()) {
            File file = new File(CppUtils.getCygwinBase() + "/bin", "ps.exe"); // NOI18N
            if (file.exists()) {
                alist.add(file.getAbsolutePath());
                ptype = PTYPE_CYGWIN;
            } else {
                file = new File(CppUtils.getMSysBase() + "/bin", "ps.exe"); // NOI18N
                if (file.exists()) {
                    alist.add(file.getAbsolutePath());
                    ptype = PTYPE_CYGWIN;
                }
            }
        } else {
            if (new File("/bin/ps").exists()) { // NOI18N
                alist.add("/bin/ps"); // NOI18N
                alist.add("-a"); // NOI18N
                alist.add("-o"); // NOI18N
                if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                    alist.add("user,pid,ppid,stime,time,command"); // NOI18N
                } else {
                    alist.add("user,pid,ppid,stime,time,args"); // NOI18N
                }
                ptype = PTYPE_STD;
            } else {
                if (new File("/usr/bin/ps").exists()) { // NOI18N
                    alist.add("/usr/bin/ps"); // NOI18N
                    alist.add("-a"); // NOI18N
                    alist.add("-o"); // NOI18N
                    alist.add("user,pid,ppid,stime,time,args"); // NOI18N
                    ptype = PTYPE_STD;
                }
            }
        }
        return alist;
    }
}
