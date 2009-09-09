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
package org.netbeans.modules.dlight.procfs.impl;

import java.io.IOException;
import java.io.InputStream;

public final class UsageStatistics {

    private static final int s_int = 4;
    private static final int s_timestruc_t = s_int * 2;
    private static final int s_total = s_int * 2 + s_timestruc_t * 14;
    private static final byte[] sharedBuffer = new byte[s_total];
    private static final DataReader reader = new DataReader(sharedBuffer);
    public final int pr_lwpid;
    public final int pr_count;
    public final Timestruc pr_tstamp;
    public final Timestruc pr_create;
    public final Timestruc pr_term;
    public final Timestruc pr_rtime;
    public final Timestruc pr_utime;
    public final Timestruc pr_stime;
    public final Timestruc pr_ttime;
    public final Timestruc pr_tftime;
    public final Timestruc pr_dftime;
    public final Timestruc pr_kftime;
    public final Timestruc pr_ltime;
    public final Timestruc pr_slptime;
    public final Timestruc pr_wtime;
    public final Timestruc pr_stoptime;

    private UsageStatistics() {
        reader.seek(0);
        pr_lwpid = reader._int();
        pr_count = reader._int();
        pr_tstamp = reader._time();
        pr_create = reader._time();
        pr_term = reader._time();
        pr_rtime = reader._time();
        pr_utime = reader._time();
        pr_stime = reader._time();
        pr_ttime = reader._time();
        pr_tftime = reader._time();
        pr_dftime = reader._time();
        pr_kftime = reader._time();
        pr_ltime = reader._time();
        pr_slptime = reader._time();
        pr_wtime = reader._time();
        pr_stoptime = reader._time();
    }

    static synchronized UsageStatistics get(final InputStream inputStream) throws IOException {
        try {
            int read = inputStream.read(sharedBuffer, 0, s_total);
        } finally {
            inputStream.close();
        }

        return new UsageStatistics();
    }
}
