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
package org.netbeans.modules.dlight.dtrace.collector.support;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.modules.dlight.dtrace.collector.impl.MultipleDTDCConfigurationAccessor;
import org.netbeans.modules.dlight.dtrace.collector.support.DtraceDataCollector.IndicatorDataProvideHandler;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.util.DLightLogger;

/**
 *
 * @author Alexey Vladykin
 */
public final class MultipleDtraceDataCollector extends IndicatorDataProvider<MultipleDTDCConfiguration>
        implements DataCollector<MultipleDTDCConfiguration>, IndicatorDataProvideHandler {

    private DtraceDataCollector collector;
    private Map<String, DtraceDataCollector> slaveCollectors;
    private DtraceDataCollector lastSlaveCollector;

    public MultipleDtraceDataCollector() {
    }

    public MultipleDtraceDataCollector(MultipleDTDCConfiguration configuration) {
        collector = new DtraceDataCollector(new DTDCConfiguration(null, Collections.<DataTableMetadata>emptyList()));
        collector.setProcessLineCallback(new ProcessLineCallbackImpl());
        slaveCollectors = new HashMap<String, DtraceDataCollector>();
        lastSlaveCollector = null;
        addConfiguration(configuration);
    }

    public String getName() {
        return collector.getName();
    }

    public void addConfiguration(MultipleDTDCConfiguration configuration) {
        DtraceDataCollector slaveCollector = new DtraceDataCollector(
                MultipleDTDCConfigurationAccessor.getDefault().getDTDCConfiguration(configuration));
        slaveCollector.setSlave(true);
        slaveCollector.setIndicatorDataProviderHanlder(this);
        slaveCollectors.put(MultipleDTDCConfigurationAccessor.getDefault().getOutputPrefix(configuration), slaveCollector);
    }

//  @Override
    public List<DataStorageType> getSupportedDataStorageTypes() {
        return Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
    }

//  @Override
    public List<DataTableMetadata> getDataTablesMetadata() {
        List<DataTableMetadata> ret = new ArrayList<DataTableMetadata>(slaveCollectors.size());
        for (DtraceDataCollector ddc : slaveCollectors.values()) {
            ret.addAll(ddc.getDataTablesMetadata());
        }
        return ret;
    }

//  @Override
    public void init(DataStorage storage, DLightTarget target) {
        for (DtraceDataCollector ddc : slaveCollectors.values()) {
            ddc.init(storage, target);
        }
        collector.setLocalScriptPath(mergeScripts().getAbsolutePath());
            collector.init(storage, target);
    }

    private File mergeScripts() {
        try {
            File output = File.createTempFile("dlight", ".d"); // NOI18N
            BufferedWriter w = new BufferedWriter(new FileWriter(output));
            try {
                w.write("#!/usr/sbin/dtrace -ZCqs\n"); // NOI18N
                for (Map.Entry<String, DtraceDataCollector> entry : slaveCollectors.entrySet()) {
                    DtraceDataCollector ddc = entry.getValue();
                    BufferedReader r = new BufferedReader(new FileReader(ddc.getLocalScriptPath()));
                    try {
                        for (String line = r.readLine(); line != null; line = r.readLine()) {
                            if (!line.startsWith("#!")) { // NOI18N
                                w.write(line.replaceAll("(print[af]\\(\")", "$1" + entry.getKey())); // NOI18N
                                w.write('\n'); // NOI18N
                            }
                        }
                        w.write('\n'); // NOI18N
                    } finally {
                        r.close();
                    }
                }
            } finally {
                w.close();
            }
            return output;
        } catch (IOException ex) {
            DLightLogger.getLogger(MultipleDtraceDataCollector.class).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public boolean isAttachable() {
        return collector.isAttachable();
    }

    public String getCmd() {
        return collector.getCmd();
    }

    public String[] getArgs() {
        return collector.getArgs();
    }

    public ValidationStatus validate(DLightTarget target) {
        return collector.validate(target, this, true);
    }

    public void invalidate() {
        collector.invalidate();
    }

    public ValidationStatus getValidationStatus() {
        return collector.getValidationStatus();
    }

    public void addValidationListener(ValidationListener listener) {
        collector.addValidationListener(listener);
    }

    public void removeValidationListener(ValidationListener listener) {
        collector.removeValidationListener(listener);
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        collector.targetStateChanged(event);

        for (DtraceDataCollector ddc : slaveCollectors.values()) {
            ddc.targetStateChanged(event);
        }
    }

    public void notify(List<DataRow> list) {
        notifyIndicators(list);
    }

    public void dataFiltersChanged(List<DataFilter> newSet) {
    }

    private class ProcessLineCallbackImpl implements ProcessLineCallback {

        public void processLine(String line) {
            DtraceDataCollector target = lastSlaveCollector;
            for (Map.Entry<String, DtraceDataCollector> entry : slaveCollectors.entrySet()) {
                String prefix = entry.getKey();
                if (line.startsWith(prefix)) {
                    line = line.substring(prefix.length());
                    target = entry.getValue();
                    break;
                }
            }
            if (target != null) {
                target.getProcessLineCallback().processLine(line);
            }
            lastSlaveCollector = target;
        }
    }
}
