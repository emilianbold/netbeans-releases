/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.tools.impl;

import java.math.BigInteger;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Helper class for dealing with <code>/proc</code> contents on Linux.
 *
 * @author Alexey Vladykin
 */
public final class LinuxProcfsSupport {

    private LinuxProcfsSupport() {
    }

    /**
     * Represents one CPU line from <code>/proc/stat</code>.
     * See <code>man proc</code>.
     *
     * <p>All times are measured in units of USER_HZ.
     * Using {@link BigInteger} as unsigned long long does not always fit
     * into Java <code>long</code>.
     */
    public static final class CpuStat {

        private CpuStat() {
        }

        /**
         * Time spent in user mode.
         */
        public BigInteger user() {
            return user;
        }
        private BigInteger user;

        /**
         * Time spent in user mode with low priority (nice).
         */
        public BigInteger nice() {
            return nice;
        }
        private BigInteger nice;

        /**
         * Time spent in system mode.
         */
        public BigInteger system() {
            return system;
        }
        private BigInteger system;

        /**
         * Time spent idle.
         */
        public BigInteger idle() {
            return idle;
        }
        private BigInteger idle;

        /**
         * Time waiting for I/O to complete (since Linux 2.5.41).
         */
        public BigInteger iowait() {
            return iowait;
        }
        private BigInteger iowait;

        /**
         * Time servicing interrupts (since Linux 2.6.0-test4).
         */
        public BigInteger irq() {
            return irq;
        }
        private BigInteger irq;

        /**
         * Time servicing softirqs (since Linux 2.6.0-test4).
         */
        public BigInteger softirq() {
            return softirq;
        }
        private BigInteger softirq;

        /**
         * Time spent in other operating systems when running in a virtualized
         * environment (since Linux 2.6.11).
         */
        public BigInteger steal() {
            return steal;
        }
        private BigInteger steal;

        /**
         * Time spent running a virtual CPU for guest operating systems
         * under the control of the Linux kernel (since Linux 2.6.24).
         */
        public BigInteger guest() {
            return guest;
        }
        private BigInteger guest;

        /**
         * Sums all CPU tick kinds.
         *
         * @return sum of all kinds of CPU ticks
         */
        public BigInteger all() {
            BigInteger sum = user.add(nice).add(system).add(idle);
            if (iowait != null) {
                sum = sum.add(iowait);
            }
            if (irq != null) {
                sum = sum.add(irq);
            }
            if (softirq != null) {
                sum = sum.add(softirq);
            }
            if (steal != null) {
                sum = sum.add(steal);
            }
            if (guest != null) {
                sum = sum.add(guest);
            }
            return sum;
        }
    }

    /**
     * Parses CPU line and returns result in a {@link CpuStat} structure.
     *
     * @param line  line to parse
     * @return parsed line as structure, missing optional fields are set to <code>null</code>
     * @throws NullPointerException  if line is <code>null</code>
     * @throws IllegalArgumentException  if line is not recognized
     */
    public static CpuStat parseCpuStat(final String line) {
        StringTokenizer t = new StringTokenizer(line);
        if (!t.hasMoreTokens() || !t.nextToken().equals("cpu")) { // NOI18N
            throw new IllegalArgumentException("CPU line must start with \"cpu\""); // NOI18N
        }
        final CpuStat cpu = new CpuStat();
        boolean gotMandatoryFields = false;
        try {
            cpu.user = new BigInteger(t.nextToken());
            cpu.nice = new BigInteger(t.nextToken());
            cpu.system = new BigInteger(t.nextToken());
            cpu.idle = new BigInteger(t.nextToken());
            gotMandatoryFields = true;
            cpu.iowait = new BigInteger(t.nextToken());
            cpu.irq = new BigInteger(t.nextToken());
            cpu.softirq = new BigInteger(t.nextToken());
            cpu.steal = new BigInteger(t.nextToken());
            cpu.guest = new BigInteger(t.nextToken());
        } catch (NoSuchElementException ex) {
            if (!gotMandatoryFields) {
                throw new IllegalArgumentException(
                        "CPU line must have at least 4 counters: user, nice, system, idle", ex); // NOI18N
            }
        }
        return cpu;
    }

    /**
     * Represents data from <code>/proc/pid/stat</code>.
     * See <code>man proc</code>.
     *
     * <p>{@link BigInteger} is used for fields declared as unsigned long,
     * as Java <code>long</code> is not enough for them.
     *
     * <p>Some fields are omitted to save memory and parsing time.
     * Uncomment them when needed.
     */
    public static final class ProcessStat {

        private ProcessStat() {
        }

        /**
         * The process ID.
         */
        public int pid() {
            return pid;
        }
        private int pid;

        /**
         * The filename of the executable.
         */
        public String comm() {
            return comm;
        }
        private String comm;

        /**
         * The process state. R is running, S is sleeping in an interruptible
         * wait, D is waiting in uninterruptible disk sleep, Z is zombie,
         * T is traced or stopped (on a signal), and W is paging.
         */
        public char state() {
            return state;
        }
        private char state;

        /**
         * The PID of the parent.
         */
        public int ppid() {
            return ppid;
        }
        private int ppid;

//        /**
//         * The process group ID of the process.
//         */
//        public int pgrp() {
//            return pgrp;
//        }
//        private int pgrp;
//
//        /**
//         * The session ID of the process.
//         */
//        public int session() {
//            return session;
//        }
//        private int session;
//
//        /**
//         * The controlling terminal of the process.
//         */
//        public int tty_nr() {
//            return tty_nr;
//        }
//        private int tty_nr;
//
//        /**
//         * The ID of the foreground process group of the controlling terminal of the process.
//         */
//        public int tpgid() {
//            return tpgid;
//        }
//        private int tpgid;
//
//        /**
//         * The kernel flags word of the process.
//         */
//        public BigInteger flags() {
//            return flags;
//        }
//        private BigInteger flags;
//
//        /**
//         * The number of minor faults the process has made which
//         * have not required loading a memory page from disk.
//         */
//        public BigInteger minflt() {
//            return minflt;
//        }
//        private BigInteger minflt;
//
//        /**
//         * The number of minor faults that the process's waited-for
//         * children have made.
//         */
//        public BigInteger cminflt() {
//            return cminflt;
//        }
//        private BigInteger cminflt;
//
//        /**
//         * The number of major faults the process has made which
//         * have required loading a memory page from disk.
//         */
//        public BigInteger majflt() {
//            return majflt;
//        }
//        private BigInteger majflt;
//
//        /**
//         * The number of major faults that the process's waited-for
//         * children have made.
//         */
//        public BigInteger cmajflt() {
//            return cmajflt;
//        }
//        private BigInteger cmajflt;

        /**
         * Amount of time that this process has been scheduled in user mode.
         */
        public BigInteger utime() {
            return utime;
        }
        private BigInteger utime;

        /**
         * Amount of time that this process has been scheduled in kernel mode,
         */
        public BigInteger stime() {
            return stime;
        }
        private BigInteger stime;

        /**
         * Amount of time that this process's waited-for children
         * have been scheduled in user mode.
         */
        public BigInteger cutime() {
            return cutime;
        }
        private BigInteger cutime;

        /**
         * Amount of time that this process's waited-for children
         * have been scheduled in kernel mode.
         */
        public BigInteger cstime() {
            return cstime;
        }
        private BigInteger cstime;

        /**
         * The priority value.
         */
        public long priority() {
            return priority;
        }
        private long priority;

        /**
         * The nice value.
         */
        public long nice() {
            return nice;
        }
        private long nice;

        /**
         * Number of threads in this process (since Linux 2.6).
         * Before kernel 2.6, this field was hard coded to 0 as a placeholder
         * for an earlier removed field.
         */
        public long num_threads() {
            return num_threads;
        }
        private long num_threads;

//        /**
//         * The time in jiffies before the next SIGALRM is sent to the process
//         * due to an interval timer. Since kernel 2.6.17, this field is no
//         * longer maintained, and is hard coded as 0.
//         */
//        public long itrealvalue() {
//            return itrealvalue;
//        }
//        private long itrealvalue;

        /**
         * The time in jiffies the process started after system boot.
         */
        public BigInteger starttime() {
            return starttime;
        }
        private BigInteger starttime;

//        /**
//         * Virtual memory size in bytes.
//         */
//        public BigInteger vsize() {
//            return vsize;
//        }
//        private BigInteger vsize;
//
//        /**
//         * Resident Set Size: number of pages the process has in real memory.
//         */
//        public long rss() {
//            return rss;
//        }
//        private long rss;
//
//        /**
//         * Current soft limit in bytes on the rss of the process.
//         */
//        public BigInteger rsslim() {
//            return rsslim;
//        }
//        private BigInteger rsslim;
//
//        /**
//         * The address above which program text can run.
//         */
//        public BigInteger startcode() {
//            return startcode;
//        }
//        private BigInteger startcode;
//
//        /**
//         * The address below which program text can run.
//         */
//        public BigInteger endcode() {
//            return endcode;
//        }
//        private BigInteger endcode;
//
//        /**
//         * The address of the start (i.e., bottom) of the stack.
//         */
//        public BigInteger startstack() {
//            return startstack;
//        }
//        private BigInteger startstack;
//
//        /**
//         * The current value of ESP (stack pointer).
//         */
//        public BigInteger kstkesp() {
//            return kstkesp;
//        }
//        private BigInteger kstkesp;
//
//        /**
//         * The current EIP (instruction pointer).
//         */
//        public BigInteger kstkeip() {
//            return kstkeip;
//        }
//        private BigInteger kstkeip;
//
//        /**
//         * The bitmap of pending signals, displayed as a decimal number. Obsolete.
//         */
//        public BigInteger signal() {
//            return signal;
//        }
//        private BigInteger signal;
//
//        /**
//         * The bitmap of blocked signals, displayed as a decimal number. Obsolete.
//         */
//        public BigInteger blocked() {
//            return blocked;
//        }
//        private BigInteger blocked;
//
//        /**
//         * The bitmap of ignored signals, displayed as a decimal number. Obsolete.
//         */
//        public BigInteger sigignore() {
//            return sigignore;
//        }
//        private BigInteger sigignore;
//
//        /**
//         * The bitmap of caught signals, displayed as a decimal number. Obsolete.
//         */
//        public BigInteger sigcatch() {
//            return sigcatch;
//        }
//        private BigInteger sigcatch;
//
//        /**
//         * This is the "channel" in which the process is waiting.
//         */
//        public BigInteger wchan() {
//            return wchan;
//        }
//        private BigInteger wchan;
//
//        /**
//         * Number of pages swapped (not maintained).
//         */
//        public BigInteger nswap() {
//            return nswap;
//        }
//        private BigInteger nswap;
//
//        /**
//         * Cumulative nswap for child processes (not maintained).
//         */
//        public BigInteger cnswap() {
//            return cnswap;
//        }
//        private BigInteger cnswap;
//
//        /**
//         * Signal to be sent to parent when we die (since Linux 2.1.22).
//         */
//        public int exit_signal() {
//            return exit_signal;
//        }
//        private int exit_signal;
//
//        /**
//         * CPU number last executed on (since Linux 2.2.8).
//         */
//        public int processor() {
//            return processor;
//        }
//        private int processor;
//
//        /**
//         * Real-time scheduling priority (since Linux 2.5.19).
//         */
//        public BigInteger rt_priority() {
//            return rt_priority;
//        }
//        private BigInteger rt_priority;
//
//        /**
//         * Scheduling policy (since Linux 2.5.19).
//         */
//        public BigInteger policy() {
//            return policy;
//        }
//        private BigInteger policy;

        /**
         * Aggregated block I/O delays (since Linux 2.6.18).
         */
        public BigInteger delayacct_blkio_ticks() {
            return delayacct_blkio_ticks;
        }
        private BigInteger delayacct_blkio_ticks;

        /**
         * Guest time of the process - time spent running a virtual CPU
         * for a guest operating system (since Linux 2.6.24).
         */
        public BigInteger guest_time() {
            return guest_time;
        }
        private BigInteger guest_time;

        /**
         * Guest time of the process's children (since Linux 2.6.24).
         */
        public BigInteger cguest_time() {
            return cguest_time;
        }
        private BigInteger cguest_time;
    }

    /**
     * Parses process info and returns result in {@link ProcessStat} structure.
     *
     * @param line  line to parse
     * @return parsed line as structure, missing optional fields are set
     *      to <code>null</code> or <code>0</code>
     * @throws NullPointerException if line is null
     * @throws IllegalArgumentException if line is not recognized
     */
    public static ProcessStat parseProcessStat(final String line) {
        final ProcessStat proc = new ProcessStat();
        boolean gotMandatoryFields = false;
        try {
            extractPid(proc, line);
            int commEndIdx = extractComm(proc, line);
            StringTokenizer t = new StringTokenizer(line.substring(commEndIdx));
            proc.state = t.nextToken().charAt(0);
            proc.ppid = Integer.parseInt(t.nextToken());
            /*proc.pgrp = Integer.parseInt(*/t.nextToken();//);
            /*proc.session = Integer.parseInt(*/t.nextToken();//);
            /*proc.tty_nr = Integer.parseInt(*/t.nextToken();//);
            /*proc.tpgid = Integer.parseInt(*/t.nextToken();//);
            /*proc.flags = Long.parseLong(*/t.nextToken();//);
            /*proc.minflt = Long.parseLong(*/t.nextToken();//);
            /*proc.cminflt = Long.parseLong(*/t.nextToken();//);
            /*proc.majflt = Long.parseLong(*/t.nextToken();//);
            /*proc.cmajflt = Long.parseLong(*/t.nextToken();//);
            proc.utime = new BigInteger(t.nextToken());
            proc.stime = new BigInteger(t.nextToken());
            proc.cutime = new BigInteger(t.nextToken());
            proc.cstime = new BigInteger(t.nextToken());
            proc.priority = Long.parseLong(t.nextToken());
            proc.nice = Long.parseLong(t.nextToken());
            proc.num_threads = Long.parseLong(t.nextToken());
            /*proc.itrealvalue = Long.parseLong(*/t.nextToken();//);
            proc.starttime = new BigInteger(t.nextToken());
            /*proc.vsize = new BigInteger(*/t.nextToken();//);
            /*proc.rss = Long.parseLong(*/t.nextToken();//);
            /*proc.rsslim = Long.parseLong(*/t.nextToken();//);
            /*proc.startcode = Long.parseLong(*/t.nextToken();//);
            /*proc.endcode = Long.parseLong(*/t.nextToken();//);
            /*proc.startstack = Long.parseLong(*/t.nextToken();//);
            /*proc.kstkesp = Long.parseLong(*/t.nextToken();//);
            /*proc.kstkeip = Long.parseLong(*/t.nextToken();//);
            /*proc.signal = Long.parseLong(*/t.nextToken();//);
            /*proc.blocked = Long.parseLong(*/t.nextToken();//);
            /*proc.sigignore = Long.parseLong(*/t.nextToken();//);
            /*proc.sigcatch = Long.parseLong(*/t.nextToken();//);
            /*proc.wchan = Long.parseLong(*/t.nextToken();//);
            /*proc.nswap = Long.parseLong(*/t.nextToken();//);
            /*proc.cnswap = Long.parseLong(*/t.nextToken();//);
            gotMandatoryFields = true;
            /*proc.exit_signal = Integer.parseInt(*/t.nextToken();//);
            /*proc.processor = Integer.parseInt(*/t.nextToken();//);
            /*proc.rt_priority = Long.parseLong(*/t.nextToken();//);
            /*proc.policy = Long.parseLong(*/t.nextToken();//);
            proc.delayacct_blkio_ticks = new BigInteger(t.nextToken());
            proc.guest_time = new BigInteger(t.nextToken());
            proc.cguest_time = new BigInteger(t.nextToken());
        } catch (NoSuchElementException ex) {
            if (!gotMandatoryFields) {
                throw new IllegalArgumentException("Fields up to cnswap must be here", ex); // NOI18N
            }
        }
        return proc;
    }

    private static int extractPid(final ProcessStat proc, final String line) {
        int pidEndIdx = line.indexOf(' ');
        if (pidEndIdx < 0) {
            pidEndIdx = line.length();
        }
        proc.pid = Integer.parseInt(line.substring(0, pidEndIdx));
        return pidEndIdx;
    }

    private static int extractComm(final ProcessStat proc, final String line) {
        int lparenIdx = line.indexOf('(');
        int rparenIdx = line.lastIndexOf(')');
        if (lparenIdx < 0 || rparenIdx < 0) {
            throw new IllegalArgumentException("Failed to parse comm"); // NOI18N
        } else {
            proc.comm = line.substring(lparenIdx + 1, rparenIdx);
            return rparenIdx + 1;
        }
    }
}
