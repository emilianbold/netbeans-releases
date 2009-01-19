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
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.netbeans.dlight.dtrace.collector.MultipleDTDCConfiguration;
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
 * @author masha
 */
public final class MultipleDtraceDataCollector implements DataCollector<MultipleDTDCConfiguration> {

  private  Map<String, DtraceDataCollector> collectors;
  private DtraceDataCollector lastCollector;

  public MultipleDtraceDataCollector() {
  }

  public MultipleDtraceDataCollector(DtraceDataCollector collector) {
    //TODO: uncomment
//    super(new DTDCConfiguration(null, Collections.<DataTableMetadata>emptyList()));
//    collectors = new HashMap<String, DtraceDataCollector>();
//    addCollector(collector);
//    lastCollector = null;
  }

  public void addCollector(DtraceDataCollector collector) {
    //TODO: uncomment
//    collector.setSlave(true);
//    collectors.put(collector.getOutputPrefix(), collector);
  }

//  @Override
  public List<DataStorageType> getSupportedDataStorageTypes() {
    return Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
  }

//  @Override
  public List<? extends DataTableMetadata> getDataTablesMetadata() {
    List<DataTableMetadata> ret = new ArrayList<DataTableMetadata>(collectors.size());
    for (DtraceDataCollector ddc : collectors.values()) {
      ret.addAll(ddc.getDataTablesMetadata());
    }
    return ret;
  }

//  @Override
  public void init(DataStorage storage, DLightTarget target) {
    for (DtraceDataCollector ddc : collectors.values()) {
      ddc.init(storage, target);
    }
    //TODO: uncomment
//    localScriptPath = mergeScripts().getAbsolutePath();
//    super.init(storage, target);
  }

  
  public void processLine(String line) {
    DtraceDataCollector target = lastCollector;
    for (Map.Entry<String, DtraceDataCollector> entry : collectors.entrySet()) {
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
    lastCollector = target;
  }

//  @Override
  public void targetStarted(DLightTarget target) {
    //TODO: uncomment
   // super.targetStarted(target);
    for (DtraceDataCollector ddc : collectors.values()) {
      ddc.targetStarted(target);
    }
  }

//  @Override
  public void targetFinished(DLightTarget target, int result) {
    //TODO: uncomment
  //  super.targetFinished(target, result);
    for (DtraceDataCollector ddc : collectors.values()) {
      ddc.targetFinished(target, result);
    }
  }

  private File mergeScripts() {
    try {
      File output = File.createTempFile("dlight", ".d");
      BufferedWriter w = new BufferedWriter(new FileWriter(output));
      try {
        w.write("#!/usr/sbin/dtrace -Cs\n");
        for (DtraceDataCollector ddc : collectors.values()) {
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
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean isAttachable() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public String getCmd() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public String[] getArgs() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public String getID() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Future<ValidationStatus> validate(DLightTarget targe) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void invalidate() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public ValidationStatus getValidationStatus() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void addValidationListener(ValidationListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void removeValidationListener(ValidationListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
