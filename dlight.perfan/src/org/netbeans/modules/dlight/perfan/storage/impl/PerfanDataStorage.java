/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.perfan.storage.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

public final class PerfanDataStorage extends DataStorage {

    private final Map<String, String> serviceInfoMap = new ConcurrentHashMap<String, String>();
    private Erprint erprint;

    public PerfanDataStorage() {
        super();
    }

    public final Map<String, String> getInfo() {
        return serviceInfoMap;
    }

    public final String getValue(String name) {
        return serviceInfoMap.get(name);
    }

    public final String put(String name, String value) {
        return serviceInfoMap.put(name, value);
    }

    public void init(ExecutionEnvironment execEnv, String sproHome, String experimentDirectory) {
        synchronized (this) {
            if (erprint != null) {
                erprint.stop();
            }

            erprint = new Erprint(execEnv, sproHome, experimentDirectory);
        }
    }

    // TODO: implement!
    public Object[] getCallees(long ref) {
        return null;
    }

    // TODO: implement!
    public Object[] getCallers(long ref) {
        return null;
    }

    /**
     * For now assume that getTopFunctions is a method that forces er_print restart...
     * TODO: change the behavior later...
     */
    public String[] getTopFunctions(Metrics metrics, int limit) {
        return erprint.getHotFunctions(metrics, limit, true);
    }

    public ExperimentStatistics fetchSummaryData() {
        return erprint.getExperimentStatistics(true);
    }

    @Override
    public Collection<DataStorageType> getStorageTypes() {
        return PerfanDataStorageFactory.supportedTypes;
    }

    public void shutdown() {
        erprint.stop();
    }

    @Override
    protected boolean createTablesImpl(List<DataTableMetadata> tableMetadatas) {
        return true;
    }

    @Override
    public void addData(String tableName, List<DataRow> data) {
        // Adding data is not supported for er_print
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
}
