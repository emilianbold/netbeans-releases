/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.util.Vector;
import java.util.regex.Pattern;
import java.util.logging.*;

import java.io.*;

import org.openide.ErrorManager;


import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import java.util.List;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;


/**
 * A service for accessing 'ps'.
 *
 * <pre>
 * Typical usage:
 * 	PsProvider psProvider = PsProvider.getDefault(host);
 * 	jtableModel.setDataVector(psData.processes(filter), psData.header());
 * </pre>
 */

public abstract class PsProvider {

    private static final boolean DISABLE_PARGS = Boolean.getBoolean("attach.pargs.disable"); //NOI18N

    private static final Logger logger =
	Logger.getLogger(PsProvider.class.getName());

    public final class PsData {

	private Vector<Vector<String>> processes = new Vector<Vector<String>>();

	private Vector<String> header = null;

	/**
	 * Translated header names in the table
	 */
	public Vector<String> header() {
	    return header;
	}
        
        void setHeader(Vector<String> header) {
            this.header = header;
        }

        public int commandColumnIdx() {
            return commandColumnIndex();
        }

        public int pidColumnIdx() {
            return pidColumnIndex();
        }
        
        public FileMapper getFileMapper() {
            return PsProvider.this.getFileMapper();
        }

	/**
	 * filter lines and convert to columns
	 */
	public Vector<Vector<String>> processes(Pattern re) {
            Vector<Vector<String>> res = new Vector<Vector<String>>();
            // Do filtering
            outer: for (Vector<String> proc : processes) {
                for (String field : proc) {
                    if (re.matcher(field).find()) {
                        res.add(proc);
                        continue outer;
                    }
                }
            }
            return res;
	}

        void addProcess(String line) {
            int offset = 0;
            
            Vector<String> columns = new Vector<String>(headerStr().length-3);
            for (int cx = 0; cx < headerStr().length; cx++) {
                String s = null;
                if (cx == 7) {
                    s = line.substring(offset + fields[cx][0]);
                } else {
                    // extra check for UID, see IZ 199423
                    if (headerStr()[cx].contains("UID")) { //NOI18N
                        assert offset == 0;
                        int end = fields[cx][1];
                        while (line.charAt(end+1) != ' ') {
                            end++;
                        }
                        s = line.substring(fields[cx][0], end+1);
                        offset = end - fields[cx][1];
                    } else {
                        s = line.substring(offset + fields[cx][0], offset + fields[cx][1]+1);
                    }
                }

                if (cx !=3 && cx != 5 && cx != 6) { // No "C", "TIME" and "TTY" columns
                    columns.add(s.trim());
                }
            }
            processes.add(columns);
        }

        private void updateCommand(String pid, String command) {
            for (Vector<String> proc : processes) {
                if (pid.equals(proc.get(pidColumnIdx()))) {
                    proc.set(commandColumnIdx(), command);
                }
            }
        }
    }

    // can't be static, for format of ps output is different from host to host
    private Vector<String> parsedHeader = null;

    protected static final String zero = "0";	// NOI18N
    private String uid = null;

    private int fields[][] = new int[8][2];

    /**
     * Specialization of PsProvider for Solaris
     */
    static class SolarisPsProvider extends PsProvider {

	private final static String header_str_solaris[] = {
	    "UID", // NOI18N
	    "PID", // NOI18N
	    "PPID", // NOI18N
	    "C",		// skipped // NOI18N
	    "STIME", // NOI18N
	    "TTY    ",		// skipped // NOI18N
	    " TIME",            // skipped // NOI18N
	    "CMD", // NOI18N
	};

	public SolarisPsProvider(Host host) {
	    super(host);
	}

	public int commandColumnIndex() {
	    return 4;
	}

        public int pidColumnIndex() {
	    return 1;
	}

	public String[] headerStr() {
	    return header_str_solaris;
	}

	/* 
	 * for executor, not used now, 
	 * in the future if we want to get uid from remote host
	 * this is will be used.
	 */
//	protected String[] uidCommand1() {
//	    String [] args = new String[2];
//	    args[0] = "/usr/xpg4/bin/id";
//	    args[1] = "-u";
//	    return args;
//	}

	protected String uidCommand() {
	    return "/usr/xpg4/bin/id -u";	// NOI18N
	}
	
	/* OLD
	protected String[] psCommand1(String uid) {

	    if (Log.Ps.null_uid) 
		uid = null;

	    if ( (uid == null) || (uid.equals(zero)) ) {
		// uid=0 => root; use ps -ef
		// OLD return "LANG=C /bin/ps -www -o pid,tty,time,cmd";
		String [] args = new String[2];
		args[0] = "/usr/bin/ps";
		args[1] = "-ef" ;
		return args;
		// return "LANG=C /usr/bin/ps -ef";	// NOI18N
	    } else {
		String [] args = new String[3];
		args[0] = "/usr/bin/ps";
		args[1] = "-fu";
		args[2] = uid;
		return args;
		// return "LANG=C /usr/bin/ps -fu " + uid;	// NOI18N
	    }
	}
	*/
	
	protected String psCommand(String uid) {
	    // SHOULD set LC_ALL=C here since we're depending
	    // on column widths to get to the individual ps items!
            // (moved to getData)

	    if (Log.Ps.null_uid) 
		uid = null;

	    if ( (uid == null) || (uid.equals(zero)) ) {
		// uid=0 => root; use ps -ef
		return "/usr/bin/ps -ef";	// NOI18N
	    } else {
		return "/usr/bin/ps -fu " + uid;	// NOI18N
	    }
	}

        @Override
        public PsData getData(boolean allProcesses) {
            PsData res = super.getData(allProcesses);

            // pargs call if needed
            if (res != null && !DISABLE_PARGS && !res.processes.isEmpty()) {
                NativeProcessBuilder pargsBuilder = NativeProcessBuilder.newProcessBuilder(exEnv);
                pargsBuilder.setExecutable("/usr/bin/pargs").redirectError(); // NOI18N
                pargsBuilder.getEnvironment().put("LC_ALL", "C"); // NOI18N
                String[] pargs_args = new String[res.processes.size()+1];
                pargs_args[0] = "-Fl"; // NOI18N
                int idx = 1;
                for (Vector<String> proc : res.processes) {
                    pargs_args[idx++] = proc.get(pidColumnIndex());
                }
                pargsBuilder.setArguments(pargs_args);

                try {
                    List<String> pargsOutput = ProcessUtils.readProcessOutput(pargsBuilder.call());
                    updatePargsData(res, pargs_args, pargsOutput);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            
            return res;
        }
    }
    
    static void updatePargsData(PsData res, String[] pargs_args, List<String> pargsOutput) {
        int idx = 1;
        for (String procArgs : pargsOutput) {
            if (procArgs.isEmpty() ||
                    procArgs.startsWith("pargs: Warning") || // NOI18N
                    procArgs.startsWith("pargs: Couldn't determine locale of target process") || // NOI18N
                    procArgs.startsWith("pargs: Some strings may not be displayed properly")) { // NOI18N
                continue;
            }
            if (!procArgs.startsWith("pargs:")) { // NOI18N
                res.updateCommand(pargs_args[idx], procArgs);
            }
            idx++;
        }
    }

    /**
     * Specialization of PsProvider for Linux
     */
    static class LinuxPsProvider extends PsProvider {

	private final static String header_str_linux[] = {
	    "UID     ", // NOI18N
	    "PID", // NOI18N
	    "PPID", // NOI18N
	    "C",		// skipped // NOI18N
	    "STIME", // NOI18N
	    "TTY   ",		// skipped // NOI18N
	    "    TIME",         // skipped // NOI18N
	    "CMD", // NOI18N
	};

	public LinuxPsProvider(Host host) {
	    super(host);
	}

	public int commandColumnIndex() {
	    return 4;
	}

        public int pidColumnIndex() {
	    return 1;
	}

	public String[] headerStr() {
	    return header_str_linux;
	}

	/* 
	 * for executor, not used,
	 * in the future if we want to get uid from remote host
	 * this is will be used.
	 */
//	protected String [] uidCommand1() {
//	    String [] args = new String[2];
//	    args[0] = "/usr/bin/id";
//	    args[1] = "-u";
//	    return args;
//	}

	protected String uidCommand() {
	    return "/usr/bin/id -u";	// NOI18N
	}

	/* OLD
	protected String[] psCommand1(String uid) {

	    if (Log.Ps.null_uid) 
		uid = null;

	    if ( (uid == null) || (uid.equals(zero)) ) {
		// uid=0 => root; use ps -ef
		// OLD return "LANG=C /bin/ps -www -o pid,tty,time,cmd";
		String [] args = new String[2];
		args[0] = "/bin/ps";
		args[1] = "-ef";
		return args;
	    } else {
		String [] args = new String[3];
		args[0] = "/bin/ps";
		args[1] = "-fu";
		args[2] = uid;
		return args;
	    }
	}
	*/

	protected String psCommand(String uid) {
	    // SHOULD set LC_ALL=C here since we're depending
	    // on column widths to get to the individual ps items!
            // (moved to getData)

	    if (Log.Ps.null_uid) 
		uid = null;

	    if ( (uid == null) || (uid.equals(zero)) ) {
		// uid=0 => root; use ps -ef
		// OLD return "LANG=C /bin/ps -www -o pid,tty,time,cmd";
		return "/bin/ps -ef";	// NOI18N
	    } else {
		return "/bin/ps -fu " + uid + " --width 1024";		// NOI18N
	    }
	}
    }
    
    static class MacOSPsProvider extends LinuxPsProvider {
        private final static String header_str_mac[] = {
	    "  UID", // NOI18N
	    "   PID", // NOI18N
	    "  PPID", // NOI18N
	    "   C",		// skipped // NOI18N
	    "STIME", // NOI18N
	    "TTY     ",		// skipped // NOI18N
	    "    TIME",         // skipped // NOI18N
	    "CMD", // NOI18N
	};

        @Override
        public String[] headerStr() {
            return header_str_mac;
        }
        
        public MacOSPsProvider(Host host) {
            super(host);
        }

        @Override
        protected String psCommand(String uid) {
            if ( (uid == null) || (uid.equals(zero)) ) {
		return "/bin/ps -ef";	// NOI18N
	    } else {
		return "/bin/ps -fu " + uid;		// NOI18N
	    }
        }
    }

    /**
     * Specialization of PsProvider for Windows
     */
    static class WindowsPsProvider extends PsProvider {
        private FileMapper fileMapper = FileMapper.getDefault();

	private final static String header_str_windows[] = {
	    "PID", // NOI18N
	    "PPID", // NOI18N
            "PGID", // NOI18N
            "WINPID", // NOI18N
	    "TTY",		// skipped // NOI18N
            "UID", // NOI18N
            "STIME", // NOI18N
	    "COMMAND", // NOI18N
	};

	public WindowsPsProvider(Host host) {
	    super(host);
	}

	public int commandColumnIndex() {
	    return 4;
	}
        
        // see IZ 193741 - skip status column
        @Override
        protected int firstPosition() {
            return 1;
        }

        public int pidColumnIndex() {
	    return 0;
	}

	public String[] headerStr() {
	    return header_str_windows;
	}

	protected String uidCommand() {
	    return getUtilityPath("id") + " -u";	// NOI18N
	}

	protected String psCommand(String uid) {
	    // SHOULD set LC_ALL=C here since we're depending
	    // on column widths to get to the individual ps items!
            // (moved to getData)

	    if (Log.Ps.null_uid)
		uid = null;

            // Always show all processes on Windows (-W option), see IZ 193743
	    if ( (uid == null) || (uid.equals(zero)) ) {
		// uid=0 => root; use ps -ef
		return getUtilityPath("ps") + " -W";	// NOI18N
	    } else {
		return getUtilityPath("ps") + " -u " + uid + " -W";	// NOI18N
	    }
	}
        
        private String getUtilityPath(String util) {
            File file = new File(CompilerSetUtils.getCygwinBase() + "/bin", util + ".exe"); // NOI18N
            if (file.exists()) {
                fileMapper = FileMapper.getByType(FileMapper.Type.CYGWIN);
            } else {
                fileMapper = FileMapper.getByType(FileMapper.Type.MSYS);
                file = new File(CompilerSetUtils.getCommandFolder(null), util + ".exe"); // NOI18N
            }
            if (file.exists()) {
                return file.getAbsolutePath();
            }
            return util;
        }

        @Override
        public FileMapper getFileMapper() {
            return fileMapper;
        }
    }

    public static synchronized PsProvider getDefault(Host host) {
        PsProvider psProvider = host.getResource(PsProvider.class);
        if (psProvider == null) {
            try {
                ExecutionEnvironment exEnv = host.executionEnvironment();
                if (!ConnectionManager.getInstance().isConnectedTo(exEnv)) {
                    ConnectionManager.getInstance().connectTo(exEnv);
                }
                HostInfo hostInfo = HostInfoUtils.getHostInfo(exEnv);
                switch (hostInfo.getOSFamily()) {
                    case LINUX:
                        psProvider = new LinuxPsProvider(host);
                        break;
                    case WINDOWS:
                        psProvider = new WindowsPsProvider(host);
                        break;
                    case MACOSX:
                        psProvider = new MacOSPsProvider(host);
                        break;
                    default:
                        psProvider = new SolarisPsProvider(host);
                }
            } catch (CancellationException e) {
                // user cancelled connection attempt
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            host.putResource(PsProvider.class, psProvider);
        }
        return psProvider;
    }

    /**
     * Return index of the CMD column.
     */
    protected abstract int commandColumnIndex();

    protected abstract int pidColumnIndex();

    protected abstract String[] headerStr();

//    protected abstract String[] uidCommand1(); // for executor, not used
    protected abstract String psCommand(String root);

    // OLD protected abstract String[] psCommand1(String root); // for executor
    protected abstract String uidCommand(); // for Runtime.exe
    
    protected int firstPosition() {
        return 0;
    }
    
    // return file mapper (important only on Windows)
    public FileMapper getFileMapper() {
        return FileMapper.getDefault();
    }
    
    protected final ExecutionEnvironment exEnv;

    private PsProvider(Host host) {
        exEnv = host.executionEnvironment();
    }

    // "host" for getUid is usually "localhost"
    private String getUid() {
        if (uid == null) {
            try {
                NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(exEnv);
                npb.setCommandLine(uidCommand());
                NativeProcess process;
                try {
                    process = npb.call();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to exec id command", e);
                    return exEnv.getUser();
                }

                String res = ProcessUtils.readProcessOutputLine(process);

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    String msg = "id command failed with " + exitCode; // NOI18N
                    logger.log(Level.WARNING, msg);
                    return exEnv.getUser();
                }
                if (!res.isEmpty()) {
                    uid = res;
                } else {
                    uid = exEnv.getUser();
                }
            } catch (Exception e) {
                ErrorManager.getDefault().annotate(e, "Failed to parse OutputStream of uid command"); // NOI18N
                ErrorManager.getDefault().notify(e);
            }
        }
        return uid;
    }

    /**
     * Debugging method for printing column boundries as discovered by
     * parseHeader().
     */

    private void printFields(String str) {
	System.out.printf("------------------------------------------------\n"); // NOI18N
	System.out.printf("%s\n", str); // NOI18N

	for (int sx = 0; sx < str.length(); sx++) {
	    boolean found = false;
	    for (int cx = 0; cx < headerStr().length; cx++) {
		if (fields[cx][0] == sx) {
		    System.out.printf("%d", cx); // NOI18N
		    found = true;
		} else if (fields[cx][1] == sx) {
		    System.out.printf("%d", cx); // NOI18N
		    found = true;
		}
	    }
	    if (!found)
		System.out.printf(" "); // NOI18N
	}
	System.out.printf("\n"); // NOI18N
	System.out.printf("------------------------------------------------\n"); // NOI18N
    }

    /**
     * Return a Vector of headers based on the first line emitted by 'ps' and
     * populate 'fields' as a side-effect.
     *
     * First lines look like this on solaris:
     *
     *	|     UID   PID  PPID  C    STIME TTY      TIME CMD
     *	|    ivan  1501  1483  0   Nov 26 console  0:07 xterm -name edit-left
     *
     * and like this on linux:
     *
     *	|UID        PID  PPID  C STIME TTY          TIME CMD
     *	|ivan     11585 11583  0 12:39 pts/2    00:00:00 -csh
     *
     * Field justifications are as follows:
     *
     * field	solaris		linux
     * ------------------------------
     * UID	right		left
     * PID	right		right
     * PPID	right		right
     * C	1 character	1 
     * STIME	right		left
     * TTY	left		left
     * TIME	right		right
     * CMD	left		left
     *
     * The current column boundry determination is as follows: for solaris
     *|------------------------------------------------
     *|     UID   PID  PPID  C    STIME TTY      TIME CMD
     *|0      01    12    23 34       45      56    67  7
     *|------------------------------------------------
     * ... and linux ...:
     *|------------------------------------------------
     *|UID        PID  PPID  C STIME TTY          TIME CMD
     *|0      01    12    23 34    45     56         67  7
     *|------------------------------------------------
     *
     * The left side of left-aligned columns is one column too much to the
     * left (STIME, C, TTY and CMD). This is no problem as long as the left
     * edge of this columns is next to a right-aligned column. It
     * should just be a space and get eaten up by 'trim'.
     * TTY on linux is the only one which doesn't abide by this. But I'm going
     * to wing it for now and postpone making this column discovery even more
     * involved.
     */
    Vector<String> parseHeader(String str) {

	/* OLD 
	// parsedHeader is static so we only do this once
	if (parsedHeader != null)
	    return parsedHeader;
	*/


	if (Log.Ps.debug) 
	    System.out.printf("parseHeader: '%s'\n", str); // NOI18N

	parsedHeader = new Vector<String>(headerStr().length-3);
	for (int cx = 0; cx < headerStr().length; cx++) {
	    String s = null;
	    int i;

	    i = str.indexOf(headerStr()[cx]); 

	    // fields[cx][0] the begining of this column
	    // fields[cx][1] the end of this column

	    if (i >= 0) { // found
		if (cx == 0) // first column
		    fields[cx][0] = firstPosition();
		fields[cx][1] = i + headerStr()[cx].length() - 1;
	    }

	    if (cx == 7) {
		// last one
		s = str.substring(fields[cx][0]);
	    } else {
		s = str.substring(fields[cx][0], fields[cx][1]+1);
		fields[cx+1][0] = i + headerStr()[cx].length();
	    }
	if (Log.Ps.debug) 
	    System.out.println("fields : " + fields[cx][0] + " " + fields[cx][1]); // NOI18N

	    if (cx !=3 && cx != 5 && cx != 6) // No "C", "TIME" and "TTY" columns
		parsedHeader.add(s.trim());
	}

	if (Log.Ps.debug)
	    printFields(str);

	// translate header
	for (int hx = 0; hx < parsedHeader.size(); hx++) {
	    String h = parsedHeader.get(hx);
	    parsedHeader.set(hx, Catalog.get("PS_HDR_" + h)); // NOI18N
	}

	return parsedHeader;
    }

    /**
     * Execute a ps command and return the data.
     *
     * Executes a ps command, captures the output, remembers the first line
     * as the 'parsedHeader', stuffs the rest of the lines into 'PsData.lines'.
     * PsData will columnize lines later.
     */

    public PsData getData(boolean allProcesses) {
	PsData psData = new PsData();
        String luid = allProcesses ? null : getUid();
	
	try {
            //FIXME
            NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(exEnv);
            npb.setCommandLine(psCommand(luid));
            npb.getEnvironment().put("LANG", "C"); //NOI18N

            NativeProcess process;
	    try {
		process = npb.call();
	    } catch (Exception e) {
		logger.log(Level.WARNING, "Failed to exec ps command", e);
		return null;
	    } 

	    int lineNo = 0;
	    for (String line : ProcessUtils.readProcessOutput(process)) {
		if (Log.Ps.debug) 
		    System.out.printf("PsOutput: '%s'\n", line); // NOI18N

		if (line.indexOf("UID", 0) != -1) { // NOI18N
		    // first line
		    psData.setHeader(parseHeader(line));
		    lineNo++;
		} else {
		    if (lineNo++ > 0) {
			psData.addProcess(line);
                    }
		}
	    }
            
            int exitCode = process.waitFor();
	    if (exitCode != 0) {
		String msg = "ps command failed with " + exitCode; // NOI18N
		logger.log(Level.WARNING, msg);
		return null;
	    }

	} catch (Exception e) {
	    ErrorManager.getDefault().annotate(e, "Failed to parse OutputStream of ps command"); // NOI18N
	    ErrorManager.getDefault().notify(e);
	} 

	if (psData.processes.isEmpty()) {
	    ErrorManager.getDefault().log(ErrorManager.EXCEPTION, 
		"No lines from "); // NOI18N
	}
	
	return psData;
    }
}
