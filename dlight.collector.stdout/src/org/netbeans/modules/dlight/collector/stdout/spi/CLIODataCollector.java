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
package org.netbeans.modules.dlight.collector.stdout.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.modules.dlight.api.execution.AttachableTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget.State;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.collector.stdout.api.CLIODCConfiguration;
import org.netbeans.modules.dlight.collector.stdout.api.CLIOParser;
import org.netbeans.modules.dlight.collector.stdout.api.impl.CLIODCConfigurationAccessor;
import org.netbeans.modules.dlight.management.api.DLightManager;
import org.netbeans.modules.dlight.spi.collector.DataCollector;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.impl.SQLDataStorage;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.netbeans.modules.nativeexecution.api.NativeTask;
import org.netbeans.modules.nativeexecution.api.ObservableAction;
import org.netbeans.modules.nativeexecution.api.ObservableActionListener;
import org.netbeans.modules.nativeexecution.util.HostInfo;
import org.netbeans.modules.nativeexecution.util.HostNotConnectedException;
import org.openide.util.NbBundle;

/**
 * Command Line output data collector.
 * Implements both {@link org.netbeans.modules.dlight.spi.collectorDataCollector}
 * and {@link org.netbeans.modules.dlight.spi.collector.DataCollector} via invocation of a command-line tool and parsing its output.
 */
public final class CLIODataCollector extends IndicatorDataProvider<CLIODCConfiguration> implements 
    DataCollector<CLIODCConfiguration> {

  private static final Logger log = DLightLogger.getLogger(CLIODataCollector.class);
  private String command;
  private  String argsTemplate;
  private Thread outProcessingThread;
  private DataStorage storage;
  private NativeTask collectorTask;
  private CLIOParser parser;
  private List<DataTableMetadata> dataTablesMetadata;
  private ValidationStatus validationStatus = ValidationStatus.initialStatus;
  private List<ValidationListener> validationListeners = Collections.synchronizedList(new ArrayList<ValidationListener>());

  /**
   *
   * @param command command to invoke (without arguments)
   * @param arguments command arguments
   * @param parser a {@link org.netbeans.modules.dlight.collector.stdout.api.CLIOParser} to parse command output with
   * @param dataTablesMetadata describes the tables to store parsed data in
   */
  CLIODataCollector(CLIODCConfiguration configuration) {
    this.command = CLIODCConfigurationAccessor.getDefault().getCommand(configuration);
    this.argsTemplate = CLIODCConfigurationAccessor.getDefault().getArguments(configuration);
    this.parser = CLIODCConfigurationAccessor.getDefault().getParser(configuration);
    this.dataTablesMetadata = CLIODCConfigurationAccessor.getDefault().getDataTablesMetadata(configuration);
  }



  

  /**
   * The types of storage this collector supports
   * @return returns list of {@link org.netbeans.modules.dlight.core.storage.model.DataStorageType}
   * data collector can put data into
   */
  public Collection<DataStorageType> getSupportedDataStorageTypes() {
    return Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
  }

  public void init(DataStorage storage, DLightTarget target) {
    this.storage = storage;
    log.info("Do INIT for " + storage.toString());
  }

  protected void processLine(String line) {
    DataRow dataRow = parser.process(line);
    if (dataRow != null) {
      if (dataTablesMetadata != null && !dataTablesMetadata.isEmpty() && storage != null) {
        storage.addData(dataTablesMetadata.iterator().next().getName()/*"prstat"*/, Arrays.asList(dataRow));
      }
      notifyIndicators(Arrays.asList(dataRow));
    }
  }

 

  protected NativeTask getCollectorTaskFor(DLightTarget target) {
    String cmd = command;
    if (target instanceof AttachableTarget) {
      AttachableTarget at = (AttachableTarget) target;
      cmd = cmd.concat(" ").concat(argsTemplate.replaceAll("@PID", "" + at.getPID()));
    } else {
      cmd = cmd.concat(" " + argsTemplate);
    }
    return new NativeTask(target.getExecEnv(), cmd, null);
  }

  private void targetStarted(DLightTarget target) {
    resetIndicators();
    collectorTask = getCollectorTaskFor(target);

    outProcessingThread = new Thread(new Runnable() {

      public void run() {
        try {
          String line;
          InputStream is = collectorTask.getInputStream();

          if (is == null) {
            return;
          }

          BufferedReader reader = new BufferedReader(Channels.newReader(Channels.newChannel(is), "UTF-8")); // NOI18N

          while ((line = reader.readLine()) != null) {
            processLine(line);
          }

          Thread.sleep(10);

        } catch (InterruptedException ex) {
          log.fine(Thread.currentThread().getName() + " interrupted. Stop it.");
        } catch (InterruptedIOException ex) {
          log.fine(Thread.currentThread().getName() + " interrupted. Stop it.");
        } catch (ClosedByInterruptException ex) {
          log.fine(Thread.currentThread().getName() + " interrupted. Stop it.");
        } catch (IOException ex) {
          log.fine(Thread.currentThread().getName() + " io. Stop it.");
        //Logger.getLogger(CLIODataCollector.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }, "CLI Data Collector Output Redirector");

    collectorTask.submit();
    outProcessingThread.start();
  }

  private void targetFinished(DLightTarget target) {
    log.fine("Stopping CLIODataCollector: " + collectorTask.getCommand());
    collectorTask.cancel();
    outProcessingThread.interrupt();
  }

  /** {@inheritDoc */
  public List<DataTableMetadata> getDataTablesMetadata() {
    return dataTablesMetadata;
  }

  /** {@inheritDoc */
  public boolean isAttachable() {
    return true;
  }

  /** {@inheritDoc */
  public String getCmd() {
    return command;
  }

  /** {@inheritDoc */
  public String[] getArgs() {
    return null;
  }

  /**
   * Registers a validation listener
   * @param listener a validation listener to add
   */
  public void addValidationListener(ValidationListener listener) {
    if (!validationListeners.contains(listener)) {
      validationListeners.add(listener);
    }
  }

  /**
   * Removes a validation listener
   * @param listener a listener to remove
   */
  public void removeValidationListener(ValidationListener listener) {
    validationListeners.remove(listener);
  }

  protected void notifyStatusChanged(ValidationStatus oldStatus, ValidationStatus newStatus) {
      if (oldStatus.equals(newStatus)) {
          return;
      }
      
    for (ValidationListener validationListener : validationListeners) {
      validationListener.validationStateChanged(this, oldStatus, newStatus);
    }
  }

  private static String loc(String key, Object ... params) {
    return NbBundle.getMessage(CLIODataCollector.class, key, params);
  }

  public Future<ValidationStatus> validate(final DLightTarget target) {
    return DLightExecutorService.service.submit(new Callable<ValidationStatus>() {

      public ValidationStatus call() throws Exception {
        if (validationStatus.isValid()) {
          return validationStatus;
        }

        ValidationStatus oldStatus = validationStatus;
        ValidationStatus newStatus = doValidation(target);

        notifyStatusChanged(oldStatus, newStatus);

        validationStatus = newStatus;
        return newStatus;
      }
    });
  }

  public void invalidate() {
    validationStatus = ValidationStatus.initialStatus;
  }

  private ValidationStatus doValidation(final DLightTarget target) {
    DLightLogger.assertNonUiThread();

    ValidationStatus result = null;
    boolean fileExists = false;
    boolean connected = true;

    try {
      fileExists = HostInfo.fileExists(target.getExecEnv(), command);
    } catch (HostNotConnectedException ex) {
      connected = false;
    }

    if (connected) {
      if (fileExists) {
        result = ValidationStatus.validStatus;
      } else {
        result = ValidationStatus.invalidStatus(
                loc("ValidationStatus.CommandNotFound", // NOI18N
                command));
      }
    } else {
      ObservableAction<Boolean> connectAction = target.getExecEnv().getConnectToAction();

      connectAction.addObservableActionListener(new ObservableActionListener<Boolean>() {
          public void actionCompleted(Action source, Boolean result) {
              DLightManager.getDefault().revalidateSessions();
          }
          public void actionStarted(Action source) {
          }
      });

      result = ValidationStatus.unknownStatus(
              loc("ValidationStatus.HostNotConnected"), // NOI18N
              connectAction);
    }

    return result;
  }

  public ValidationStatus getValidationStatus() {
    return validationStatus;
  }

  public void targetStateChanged(DLightTarget source, State oldState, State newState) {
    switch (newState){
      case RUNNING :
        targetStarted(source);
        break;
      case FAILED:
        targetFinished(source);
        break;
      case TERMINATED:
        targetFinished(source);
        break;
      case DONE:
        targetFinished(source);
        break;
    }
  }
  
  
}
