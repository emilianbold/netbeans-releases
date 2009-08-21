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
package org.netbeans.modules.dlight.core.stack.datacollector;

import java.net.URL;
import java.util.Arrays;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.openide.util.NbBundle;

/**
 * @author Alexey Vladykin
 */
public final class CpuSamplingSupport {

    private CpuSamplingSupport() {
    }

    public static final URL CPU_SAMPLING_SCRIPT_URL =
            CpuSamplingSupport.class.getResource("/org/netbeans/modules/dlight/core/stack/resources/calls.d"); // NOI18N

    public static final Column TIMESTAMP_COLUMN =
            new Column("time_stamp", Long.class, getMessage("CpuSampling.Column.time_stamp"), null); // NOI18N

    public static final Column CPU_COLUMN =
            new Column("cpu_id", Integer.class, getMessage("CpuSampling.Column.cpu_id"), null); // NOI18N

    public static final Column THREAD_COLUMN =
            new Column("thread_id", Integer.class, getMessage("CpuSampling.Column.thread_id"), null); // NOI18N

    public static final Column STATE_COLUMN =
            new Column("state", Byte.class, getMessage("CpuSampling.Column.state"), null); // NOI18N

    public static final Column MICROSTATE_COLUMN =
            new Column("mstate", Byte.class, getMessage("CpuSampling.Column.mstate"), null); // NOI18N

    public static final Column DURATION_COLUMN =
            new Column("duration", Integer.class, getMessage("CpuSampling.Column.duration"), null); // NOI18N

    public static final Column STACK_COLUMN =
            new Column("leaf_id", Integer.class, getMessage("CpuSampling.Column.leaf_id"), null); // NOI18N

    public static final DataTableMetadata CPU_SAMPLE_TABLE =
            new DataTableMetadata("CallStack", // NOI18N
            Arrays.asList(TIMESTAMP_COLUMN, CPU_COLUMN, THREAD_COLUMN, STATE_COLUMN, MICROSTATE_COLUMN, DURATION_COLUMN, STACK_COLUMN), null);

    private static String getMessage(String key) {
        return NbBundle.getMessage(CpuSamplingSupport.class, key);
    }
}
