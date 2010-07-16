/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.dlight.procfs.api;

import java.io.PrintStream;

public interface PUsage {

    public SamplingData getSamplingData();

    public UsageInfo getUsageInfo();

    public MSAInfo getMSAInfo(); // MSA deltas for this sampilg period

    public abstract class UsageInfo {
        // All times in nanoseconds!

        public final int pr_lwpid;
        public final int pr_count;
        public final long pr_tstamp;
        public final long pr_create;
        public final long pr_term;
        public final long pr_rtime;

        public UsageInfo(int pr_lwpid, int pr_count, long pr_tstamp, long pr_create, long pr_term, long pr_rtime) {
            this.pr_lwpid = pr_lwpid;
            this.pr_count = pr_count;
            this.pr_tstamp = pr_tstamp;
            this.pr_create = pr_create;
            this.pr_term = pr_term;
            this.pr_rtime = pr_rtime;
        }
    }

    public abstract class MSAInfo {

        public final long sum_states;
        public final long pr_utime;
        public final long pr_stime;
        public final long pr_ttime;
        public final long pr_tftime;
        public final long pr_dftime;
        public final long pr_kftime;
        public final long pr_ltime;
        public final long pr_slptime;
        public final long pr_wtime;
        public final long pr_stoptime;

        public MSAInfo(
                long pr_utime,
                long pr_stime,
                long pr_ttime,
                long pr_tftime,
                long pr_dftime,
                long pr_kftime,
                long pr_ltime,
                long pr_slptime,
                long pr_wtime,
                long pr_stoptime) {
            this.pr_utime = pr_utime;
            this.pr_stime = pr_stime;
            this.pr_ttime = pr_ttime;
            this.pr_tftime = pr_tftime;
            this.pr_dftime = pr_dftime;
            this.pr_kftime = pr_kftime;
            this.pr_ltime = pr_ltime;
            this.pr_slptime = pr_slptime;
            this.pr_wtime = pr_wtime;
            this.pr_stoptime = pr_stoptime;
            sum_states = pr_utime + pr_stime + pr_ttime +
                    pr_tftime + pr_dftime + pr_kftime +
                    pr_ltime + pr_slptime + pr_wtime +
                    pr_stoptime;
        }

        public void dump(PrintStream out) {
            out.println("USR SYS TRP TFL DFL KFL LCK SLP LAT STP"); // NOI18N
            out.printf("%d  %d  %d  %d  %d  %d  %d  %d  %d  %d \n", // NOI18N
                    pr_utime,
                    pr_stime,
                    pr_ttime,
                    pr_tftime,
                    pr_dftime,
                    pr_kftime,
                    pr_ltime,
                    pr_slptime,
                    pr_wtime,
                    pr_stoptime); // NOI18N
        }
    }
}
