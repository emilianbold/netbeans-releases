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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Helper class for dealing with <code>/proc</code> contents on Solaris.
 *
 * @author Alexey Vladykin
 */
public final class SolarisProcfsSupport {

    /**
     * timespec structure, part of prusage.
     */
    public static final class Timespec {

        private Timespec() {
        }

        /**
         * Seconds.
         */
        public long sec() {
            return sec;
        }
        private long sec;

        /**
         * Nanoseconds.
         */
        public long nsec() {
            return nsec;
        }
        private long nsec;
    }

    /**
     * Represents <code>/proc/pid/usage</code> file.
     * See prusage structure in <code>sys/procfs.h</code>.
     *
     * <p>Some fields are omitted to save memory and parsing time.
     * Uncomment them when needed.
     */
    public static final class Prusage {

        private Prusage() {
        }

        /**
         * LWP id. 0: process or defunct.
         */
        public long lwpid() {
            return lwpid;
        }
        private long lwpid;

        /**
         * Number of contributing LWPs.
         */
        public int count() {
            return count;
        }
        private int count;

        /**
         * Current time stamp.
         */
        public Timespec tstamp() {
            return tstamp;
        }
        private Timespec tstamp;

//        /**
//         * Process/LWP creation time stamp.
//         */
//        public Timespec create() {
//            return create;
//        }
//        private Timespec create;
//
//        /**
//         * Process/LWP termination time stamp.
//         */
//        public Timespec term() {
//            return term;
//        }
//        private Timespec term;
        /**
         * Total LWP real (elapsed) time.
         */
        public Timespec rtime() {
            return rtime;
        }
        private Timespec rtime;

        /**
         * User level CPU time.
         */
        public Timespec utime() {
            return utime;
        }
        private Timespec utime;

        /**
         * System call CPU time.
         */
        public Timespec stime() {
            return stime;
        }
        private Timespec stime;

//        /**
//         * Other system trap CPU time.
//         */
//        public Timespec ttime() {
//            return ttime;
//        }
//        private Timespec ttime;
//
//        /**
//         * Text page fault sleep time.
//         */
//        public Timespec tftime() {
//            return tftime;
//        }
//        private Timespec tftime;
//
//        /**
//         * Data page fault sleep time.
//         */
//        public Timespec dftime() {
//            return dftime;
//        }
//        private Timespec dftime;
//
//        /**
//         * Kernel page fault sleep time.
//         */
//        public Timespec kftime() {
//            return kftime;
//        }
//        private Timespec kftime;
//
//        /**
//         * User lock wait sleep time.
//         */
//        public Timespec ltime() {
//            return ltime;
//        }
//        private Timespec ltime;
//
//        /**
//         * All other sleep time.
//         */
//        public Timespec slptime() {
//            return slptime;
//        }
//        private Timespec slptime;
//
//        /**
//         * Wait-CPU (latency) time.
//         */
//        public Timespec wtime() {
//            return wtime;
//        }
//        private Timespec wtime;
//
//        /**
//         * Stopped time.
//         */
//        public Timespec stoptime() {
//            return stoptime;
//        }
//        private Timespec stoptime;
//
//        /**
//         * Minor page faults.
//         */
//        public long minf() {
//            return minf;
//        }
//        private long minf;
//
//        /**
//         * Major page faults.
//         */
//        public long majf() {
//            return majf;
//        }
//        private long majf;
//
//        /**
//         * Swaps.
//         */
//        public long nswap() {
//            return nswap;
//        }
//        private long nswap;
//
//        /**
//         * Input blocks.
//         */
//        public long inblk() {
//            return inblk;
//        }
//        private long inblk;
//
//        /**
//         * Output blocks.
//         */
//        public long oublk() {
//            return oublk;
//        }
//        private long oublk;
//
//        /**
//         * Messages sent.
//         */
//        public long msnd() {
//            return msnd;
//        }
//        private long msnd;
//
//        /**
//         * Messages received.
//         */
//        public long mrcv() {
//            return mrcv;
//        }
//        private long mrcv;
//
//        /**
//         * Signals received.
//         */
//        public long sigs() {
//            return sigs;
//        }
//        private long sigs;
//
//        /**
//         * Voluntary context switches.
//         */
//        public long vctx() {
//            return vctx;
//        }
//        private long vctx;
//
//        /**
//         * Involuntary context switches.
//         */
//        public long ictx() {
//            return ictx;
//        }
//        private long ictx;
//
//        /**
//         * System calls.
//         */
//        public long sysc() {
//            return sysc;
//        }
//        private long sysc;
//
//        /**
//         * Chars read and written.
//         */
//        public long ioch() {
//            return ioch;
//        }
//        private long ioch;
    }

    /**
     * Parses prusage structure from hex dump, as printed by <code>od -v -t x4</code>.
     * Invoke this method with each line of the dump (in proper order)
     * and single Prusage structure.
     *
     * <p>Here is an idea of how the hex dump look like:
     * <pre>
     * 0000000 00000000 00000002 00011964 26bfb1c0
     * 0000020 00011923 26fdb58a 00000000 00000000
     * 0000040 00000081 20cd90ca 0000007e 2b4e8e04
     * 0000060 00000000 14c34ae9 00000000 060272d8
     * 0000100 ...
     * </pre>
     *
     * @param line  one line of the dump to parse
     * @param prusage  Prusage structure that will be updated with parsed data,
     *          pass <code>null</code> to create new instance
     * @return same instance as passed to this method, or new instance if
     *          <code>null</code> was passed
     * @throws NullPointerException if line is <code>null</code>
     * @throws IllegalArgumentException if line is not recognized
     */
    public static Prusage parsePrusage(final String line, final Prusage prusage) {
        final Prusage proc = prusage == null ? new Prusage() : prusage;
        StringTokenizer t = new StringTokenizer(line);
        try {
            String lineNumber = t.nextToken();
            if ("0000000".equals(lineNumber)) { // NOI18N
                proc.lwpid = parseHex(t.nextToken());
                proc.count = (int)parseHex(t.nextToken());
                Timespec tstamp = new Timespec();
                tstamp.sec = parseHex(t.nextToken());
                tstamp.nsec = parseHex(t.nextToken());
                proc.tstamp = tstamp;
            }
        } catch (NoSuchElementException ex) {
            throw new IllegalArgumentException("Too few elements in line", ex); // NOI18N
        }
        return proc;
    }

    private static long parseHex(String value) {
        return Long.parseLong(value, 16);
    }
}
