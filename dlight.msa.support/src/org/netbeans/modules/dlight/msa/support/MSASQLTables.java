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
package org.netbeans.modules.dlight.msa.support;

import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;

public final class MSASQLTables {

    private final static Column LWP_ID = new DataTableMetadata.Column("lwp_id", Integer.class); // NOI18N
    // Timestamp when data was collected
    private final static Column TIMESTAMP = new DataTableMetadata.Column("timestamp", Long.class); // NOI18N
    // time period for which the data is collected (?)
    private final static Column SAMPLE = new DataTableMetadata.Column("sample", Long.class); // NOI18N

    private MSASQLTables() {
    }

    public interface msa {

        public final static Column LWP_ID = MSASQLTables.LWP_ID;
        public final static Column TIMESTAMP = MSASQLTables.TIMESTAMP;
        public final static Column SAMPLE = MSASQLTables.SAMPLE;
        public final static Column LWP_MSA_USR = new DataTableMetadata.Column("lwp_msa_usr", Float.class); // NOI18N
        public final static Column LWP_MSA_SYS = new DataTableMetadata.Column("lwp_msa_sys", Float.class); // NOI18N
        public final static Column LWP_MSA_TRP = new DataTableMetadata.Column("lwp_msa_trp", Float.class); // NOI18N
        public final static Column LWP_MSA_TFL = new DataTableMetadata.Column("lwp_msa_tfl", Float.class); // NOI18N
        public final static Column LWP_MSA_DFL = new DataTableMetadata.Column("lwp_msa_dfl", Float.class); // NOI18N
        public final static Column LWP_MSA_KFL = new DataTableMetadata.Column("lwp_msa_kfl", Float.class); // NOI18N
        public final static Column LWP_MSA_LCK = new DataTableMetadata.Column("lwp_msa_lck", Float.class); // NOI18N
        public final static Column LWP_MSA_SLP = new DataTableMetadata.Column("lwp_msa_slp", Float.class); // NOI18N
        public final static Column LWP_MSA_LAT = new DataTableMetadata.Column("lwp_msa_lat", Float.class); // NOI18N
        public final static Column LWP_MSA_STP = new DataTableMetadata.Column("lwp_msa_stp", Float.class); // NOI18N
        public final List<Column> columns = Arrays.asList(
                TIMESTAMP, SAMPLE, LWP_ID,
                LWP_MSA_USR, LWP_MSA_SYS, LWP_MSA_TRP,
                LWP_MSA_TFL, LWP_MSA_DFL, LWP_MSA_KFL,
                LWP_MSA_LCK, LWP_MSA_SLP, LWP_MSA_LAT, LWP_MSA_STP);
        public final DataTableMetadata tableMetadata = new DataTableMetadata("msa", // NOI18N
                columns, null);
    }

    public interface lwps {

        public final static Column LWP_ID = MSASQLTables.LWP_ID;
        public final static Column LWP_START = new DataTableMetadata.Column("startts", Long.class); // NOI18N
        public final static Column LWP_END = new DataTableMetadata.Column("endts", Long.class); // NOI18N
        public final List<Column> columns = Arrays.asList(LWP_ID, LWP_START, LWP_END);
        public final DataTableMetadata tableMetadata = new DataTableMetadata("lwps", // NOI18N
                columns, null);
    }

    public interface prstat {

        public final static Column TIMESTAMP = MSASQLTables.TIMESTAMP;
        public final static Column SAMPLE = MSASQLTables.SAMPLE;
        public final static Column LWPS_LCOUNT = new DataTableMetadata.Column("lwps_lcount", Integer.class); // NOI18N
        public final static Column LWPS_ZCOUNT = new DataTableMetadata.Column("lwps_zcount", Integer.class); // NOI18N
        public final static Column P_SLEEP = new DataTableMetadata.Column("p_sleep", Float.class); // NOI18N
        public final static Column P_WAIT = new DataTableMetadata.Column("p_wait", Float.class); // NOI18N
        public final static Column P_BLOCKED = new DataTableMetadata.Column("p_blocked", Float.class); // NOI18N
        public final static Column P_RUNNING = new DataTableMetadata.Column("p_running", Float.class); // NOI18N
        public final List<Column> columns = Arrays.asList(TIMESTAMP, SAMPLE, LWPS_LCOUNT, LWPS_ZCOUNT, P_RUNNING, P_BLOCKED, P_WAIT, P_SLEEP);
        public final DataTableMetadata tableMetadata = new DataTableMetadata("prstat", // NOI18N
                columns, null);
    }
}
