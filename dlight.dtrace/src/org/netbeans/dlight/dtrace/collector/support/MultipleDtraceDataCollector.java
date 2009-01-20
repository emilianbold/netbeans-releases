/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.dlight.dtrace.collector.support;

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
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.dlight.dtrace.collector.MultipleDTDCConfiguration;
import org.netbeans.dlight.dtrace.collector.impl.MultipleDTDCConfigurationAccessor;
import org.netbeans.modules.dlight.execution.api.DLightTarget;
import org.netbeans.modules.dlight.model.Validateable.ValidationStatus;
import org.netbeans.modules.dlight.model.ValidationListener;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.DataStorageTypeFactory;
import org.netbeans.modules.dlight.spi.storage.support.SQLDataStorage;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;
import org.netbeans.modules.dlight.util.DLightLogger;


/**
 *
 * @author Alexey Vladykin
 */
public final class MultipleDtraceDataCollector implements DataCollector<MultipleDTDCConfiguration> {

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

  public void addConfiguration(MultipleDTDCConfiguration configuration) {
    DtraceDataCollector slaveCollector = new DtraceDataCollector(
            MultipleDTDCConfigurationAccessor.getDefault().getDTDCConfiguration(configuration));
    slaveCollector.setSlave(true);
    slaveCollectors.put(slaveCollector.getOutputPrefix(), slaveCollector);
  }

//  @Override
  public List<DataStorageType> getSupportedDataStorageTypes() {
    return Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
  }

//  @Override
  public List<? extends DataTableMetadata> getDataTablesMetadata() {
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

//  @Override
  public void targetStarted(DLightTarget target) {
    collector.targetStarted(target);
    for (DtraceDataCollector ddc : slaveCollectors.values()) {
      ddc.targetStarted(target);
    }
  }

//  @Override
  public void targetFinished(DLightTarget target, int result) {
    collector.targetFinished(target, result);
    for (DtraceDataCollector ddc : slaveCollectors.values()) {
      ddc.targetFinished(target, result);
    }
  }

  private File mergeScripts() {
    try {
      File output = File.createTempFile("dlight", ".d");
      BufferedWriter w = new BufferedWriter(new FileWriter(output));
      try {
        w.write("#!/usr/sbin/dtrace -Cs\n");
        for (DtraceDataCollector ddc : slaveCollectors.values()) {
          BufferedReader r = new BufferedReader(new FileReader(ddc.getLocalScriptPath()));
          try {
            for (String line = r.readLine(); line != null; line = r.readLine()) {
              if (!line.startsWith("#!")) {
                w.write(line.replaceAll("(printf\\(\")", "$1" + ddc.getOutputPrefix()));
                w.write('\n');
              }
            }
            w.write('\n');
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

  public DataCollector<MultipleDTDCConfiguration> create(MultipleDTDCConfiguration configuration) {
    return MultipleDtraceDataCollectorSupport.getInstance().getCollector(configuration);
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

  public String getID() {
    return MultipleDTDCConfigurationAccessor.getDefault().getID();
  }

  public Future<ValidationStatus> validate(DLightTarget target) {
    return collector.validate(target);
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
