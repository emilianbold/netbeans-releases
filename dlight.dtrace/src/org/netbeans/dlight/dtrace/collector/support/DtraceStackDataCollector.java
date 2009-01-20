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
package org.netbeans.dlight.dtrace.collector.support;

import org.netbeans.dlight.dtrace.collector.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.dlight.core.stack.model.FunctionMetric;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata.Column;
import org.netbeans.dlight.core.stack.storage.StackDataStorage;
import org.netbeans.dlight.dtrace.collector.impl.DTSTDCConfigurationAccessor;
import org.netbeans.modules.dlight.execution.api.DLightTarget;
import org.netbeans.modules.dlight.model.Validateable.ValidationStatus;
import org.netbeans.modules.dlight.model.ValidationListener;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageTypeFactory;

/**
 * This class represents collector which collects data using DTrace.
 * ustack() probe is used to store data into the
 * {@link org.netbeans.dlight.core.stack.storage.StackDataStorage}.
 */
public final class DtraceStackDataCollector
        //extends DtraceDataCollector{
        implements DataCollector<DTSTDCConfiguration>{
        
  private static final String UNKNOWN = "???";
  private static final Pattern FIRST_LINE = Pattern.compile("cpu=(\\d+), thread=(\\d+), time=(\\d+), pc=([0-9a-f]+)");
  private static final Pattern NEXT_LINE_MFO = Pattern.compile("              (.+`.+)\\+0x([0-9a-f]+)");
  private static final Pattern NEXT_LINE_MO = Pattern.compile("              (.+`)0x([0-9a-f]+)");
  private static final Pattern NEXT_LINE_MF = Pattern.compile("              (.+`.+)");
  private static final Pattern NEXT_LINE_O = Pattern.compile("              0x[0-9a-f]+");
  private  List<CharSequence> stack;
  private  Map<FunctionMetric, Object> metrics;
  private int cpuId;
  private int threadId;
  private long curTimestamp;
  private long prevTimestamp;
  private final List<DataStorageType> supportedStorageTypes = Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(StackDataStorage.STACK_DATA_STORAGE_TYPE_ID));
  private DtraceDataCollector dtraceDataCollector = null;

  public DtraceStackDataCollector() {
    
  }

  private DtraceStackDataCollector(DTSTDCConfiguration configuration){
   // super(configuration);
    dtraceDataCollector = new DtraceDataCollector(DTSTDCConfigurationAccessor.getDefault().getDTDCConfiguration(configuration));
    dtraceDataCollector.setProcessLineCallback(new ProcessLineCallbackImpl());
    this.stack = new ArrayList<CharSequence>();
    this.metrics = new HashMap<FunctionMetric, Object>();
    this.curTimestamp = 0;
    this.prevTimestamp = 0;
  }


//  cre
//  @Override
//  public DtraceDataCollector create(DTDCConfiguration configuration) {
////    if (configuration instanceof )
//    return new DtraceStackDataCollector((DTSTDCConfiguration)configuration);
//  }

  private final DataTableMetadata getMetadaData() {
    List<Column> columns = new ArrayList<Column>();
    columns.add(new Column("name", String.class, "Function Name", null));
    //e.user:i.user:i.sync:i.syncn:name
//    List<Column> tableColumns = new ArrayList<Column>();
    if (((StackDataStorage)dtraceDataCollector.getStorage()) == null) {
      DataTableMetadata result = new DataTableMetadata(StackDataStorage.STACK_METADATA_VIEW_NAME, columns);
      return result;
    }
    List<FunctionMetric> metricsList = ((StackDataStorage) dtraceDataCollector.getStorage()).getMetricsList();
    for (FunctionMetric metric : metricsList) {
      columns.add(new Column(metric.getMetricID(), metric.getMetricValueClass(), metric.getMetricDisplayedName(), null));
    }
    DataTableMetadata result = new DataTableMetadata(StackDataStorage.STACK_METADATA_VIEW_NAME, columns);
    return result;
  }


  /**
   * 
   * @return
   */
//  @Override
  public List<? extends DataTableMetadata> getDataTablesMetadata() {
    return Arrays.asList(getMetadaData());
  }


  

  
  

//  @Override
  public List<DataStorageType> getSupportedDataStorageTypes() {
    return supportedStorageTypes;
  }

  private void storeStackIfNotEmpty() {
    if (!stack.isEmpty()) {
      long sampleDuration = prevTimestamp == 0? 0 : curTimestamp - prevTimestamp;
	  Collections.reverse(stack);
      ((StackDataStorage) dtraceDataCollector.getStorage()).putStack(stack, cpuId, threadId, curTimestamp, sampleDuration);
      stack.clear();
      prevTimestamp = curTimestamp;
    }
  }

//  @Override
  public String getID() {
    return DTSTDCConfigurationAccessor.getDefault().getID();
  }

//  @Override
  public DtraceStackDataCollector create(DTSTDCConfiguration configuration) {
    return new DtraceStackDataCollector(configuration);
  }

  



  public void init(DataStorage storage, DLightTarget target) {
    dtraceDataCollector.init(storage, target);
  }

  public boolean isAttachable() {
    return dtraceDataCollector.isAttachable();
  }

  public String getCmd() {
    return dtraceDataCollector.getCmd();
  }

  public String[] getArgs() {
    return dtraceDataCollector.getArgs();
  }

  public Future<ValidationStatus> validate(DLightTarget targe) {
    return dtraceDataCollector.validate(targe);
  }

  public void targetStarted(DLightTarget target) {
    dtraceDataCollector.targetStarted(target);
  }

  public void targetFinished(DLightTarget target, int result) {
    dtraceDataCollector.targetFinished(target, result);
  }

  public void invalidate() {
    dtraceDataCollector.invalidate();
  }

  public ValidationStatus getValidationStatus() {
    return dtraceDataCollector.getValidationStatus();
  }

  public void addValidationListener(ValidationListener listener) {
    dtraceDataCollector.addValidationListener(listener);
  }

  public void removeValidationListener(ValidationListener listener) {
    dtraceDataCollector.removeValidationListener(listener);
  }

 private final class ProcessLineCallbackImpl implements ProcessLineCallback{
   public void processLine(String line) {
    if (line.length() == 0) {
      storeStackIfNotEmpty();
      return;
    }

    Matcher m = NEXT_LINE_MFO.matcher(line);
    if (m.matches()) {
      stack.add(m.group(1));
      return;
    }

    m = NEXT_LINE_MO.matcher(line);
    if (m.matches()) {
      stack.add(m.group(1) + UNKNOWN);
      return;
    }

    m = NEXT_LINE_MF.matcher(line);
    if (m.matches()) {
      stack.add(m.group(1));
      return;
    }

    m = NEXT_LINE_O.matcher(line);
    if (m.matches()) {
      stack.add(UNKNOWN);
      return;
    }

    // this is least frequent case, keep it last
    m = FIRST_LINE.matcher(line);
    if (m.matches()) {
      cpuId = Integer.parseInt(m.group(1));
      threadId = Integer.parseInt(m.group(2));
      curTimestamp = Long.parseLong(m.group(3));
      return;
    }

    // unexpected input!
  }
 }

  
}
