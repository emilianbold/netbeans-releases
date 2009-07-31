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
package org.netbeans.modules.dlight.tools;

import java.util.Arrays;
import org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.tools.impl.LLDataCollectorConfigurationAccessor;

/**
 * @author Alexey Vladykin
 */
public final class LLDataCollectorConfiguration
        implements DataCollectorConfiguration,
        IndicatorDataProviderConfiguration {

    public static enum CollectedData {
        CPU, MEM, SYNC
    }

    private static final String NAME = "LLTool";//NOI18N

    public static final Column LOCKS_COUNT = new DataTableMetadata.Column("locks", Integer.class); // NOI18N
    public static final Column threads_count = new DataTableMetadata.Column("threads", Integer.class); // NOI18N

    public static final DataTableMetadata CPU_TABLE = new DataTableMetadata(
            "lltool_cpu", Arrays.asList( // NOI18N
            new DataTableMetadata.Column("utime", Float.class), // NOI18N
            new DataTableMetadata.Column("stime", Float.class)), // NOI18N
            null);

    public static final DataTableMetadata MEM_TABLE = new DataTableMetadata(
            "lltool_mem", Arrays.asList( // NOI18N
            new DataTableMetadata.Column("total", Integer.class)), // NOI18N
            null);

    public static final DataTableMetadata SYNC_TABLE = new DataTableMetadata(
            "lltool_sync", Arrays.asList( // NOI18N
            LOCKS_COUNT,
            threads_count),
            null);

    private final CollectedData dataType;

    public LLDataCollectorConfiguration(CollectedData dataType) {
        this.dataType = dataType;
    }

    public String getID() {
        return ID;
    }
    public static final String ID = "LLDataCollectorConfiguration_ID"; // NOI18N

    private static class LLDataCollectorConfigurationAccessorImpl extends LLDataCollectorConfigurationAccessor {

        @Override
        public CollectedData getCollectedData(LLDataCollectorConfiguration conf) {
            return conf.dataType;
        }

        @Override
        public String getName() {
            return NAME;
        }
    }

    static {
        LLDataCollectorConfigurationAccessor.setDefault(new LLDataCollectorConfigurationAccessorImpl());
    }
}
