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
package org.netbeans.modules.dlight.perfan.dataprovider;

import java.util.HashMap;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric;
import org.netbeans.modules.dlight.core.stack.api.FunctionMetric.FunctionMetricConfiguration;
import org.netbeans.modules.dlight.core.stack.api.support.FunctionMetricsFactory;
import org.openide.util.NbBundle;

public final class SSMetrics {

    private final static HashMap<String, FunctionMetric> hash = new HashMap<String, FunctionMetric>();

    public final static FunctionMetric getMetric(Column dataColumn) {
        return hash.get(dataColumn.getColumnName());
    }

    public final static class MemoryMetric {

        private static String GID = "MemoryMetric"; // NOI18N
        public static final FunctionMetric LeakBytesMetric = fm(GID, "e.bleak", Integer.class); // NOI18N
        public static final FunctionMetric LeaksCountMetric = fm(GID, "e.leak", Integer.class); // NOI18N
    }

    public final static class THAMetric {

        private static String GID = "THAMetric"; // NOI18N
        public static final FunctionMetric DeadlockMetric = fm(GID, "e.deadlocks", Integer.class); // NOI18N
        public static final FunctionMetric RaceMetric = fm(GID, "e.raccess", Integer.class); // NOI18N
    }

    public final static class TimeMetric {

        private static String GID = "TimeMetric"; // NOI18N
        static public final FunctionMetric UserFuncTimeInclusive = fm(GID, "i.user", Double.class); // NOI18N
        static public final FunctionMetric UserFuncTimeExclusive = fm(GID, "e.user", Double.class); // NOI18N
        static public final FunctionMetric SyncWaitTimeInclusive = fm(GID, "i.sync", Double.class); // NOI18N
        static public final FunctionMetric SyncWaitCallInclusive = fm(GID, "i.syncn", Integer.class); // NOI18N
        static public final FunctionMetric SyncWaitTimeExclusive = fm(GID, "e.sync", Double.class); // NOI18N
        static public final FunctionMetric SyncWaitCallExclusive = fm(GID, "e.syncn", Integer.class); // NOI18N
    }

    static private FunctionMetric fm(String groupID, String id, Class clazz) {
        FunctionMetric m = FunctionMetricsFactory.getInstance().getFunctionMetric(
                new FunctionMetricConfiguration(id,
                NbBundle.getMessage(SSMetrics.class,
                groupID + "." + id + ".uname"), clazz)); // NOI18N
        hash.put(id, m);
        return m;
    }
}
