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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.perfan.dbe.ConnectorListener;
import org.netbeans.modules.dlight.perfan.dbe.DbeConnector;
import org.netbeans.modules.dlight.perfan.dbe.IDBEInterface;
import org.netbeans.modules.dlight.perfan.spi.SunStudioDataCollector;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.support.DataStorageTypeFactory;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Exceptions;


// TODO: implement SessionListener - kill idbe on Session closure
public class PerfanDataStorage extends DataStorage implements ConnectorListener {
  private static final Logger log = DLightLogger.getLogger(PerfanDataStorage.class);
  private IDBEInterface idbe = null;
  private SunStudioDataCollector collector = null;
  private volatile boolean connecting = false;

  // TODO: PROBLEM! FIXME
  // Workaround of a problem!!!
  // If idbe crashes, an attempt to restart it takes place.
  // Problem: something gets initialized in getTopFunctions only (on native side)
  //          so any call of getCallers/getCallees crashes idbe
  //          Workaround - push 'Refresh' on table.
  public PerfanDataStorage() {
    super();
  }

  public Object[] getCallees(long ref) {
    connect();
    return idbe == null ? null : idbe.getCallees(ref);
  }

  public Object[] getCallers(long ref) {
    connect();
    return idbe == null ? null : idbe.getCallers(ref);
  }

  public Object[] getTopFunctions(String mspec, String msort, int limit) {
    connect();
    return idbe == null ? null : idbe.getTopFunctions(mspec, msort, limit);
  }

  public void setCollector(SunStudioDataCollector collector) {
    this.collector = collector;
  }

  private void connect() {
    if (idbe != null || connecting) {
      return;
    }

    if (collector == null) {
      throw new IllegalStateException("Cannot connect - no collector yet");
    }

    connecting = true;
//TODO: check if it is really should be run in separate thread as
// now it is just doesn't work
//    new Thread(new Runnable() {
//      public void run() {
        DbeConnector connector = new DbeConnector(collector.getExecEnv(), collector.getExperimentDirectory());

        try {
          connector.connect(PerfanDataStorage.this);
        } catch (IOException ex) {
          Exceptions.printStackTrace(ex);
        } finally {
          connecting = false;
        }
      //}
    //}, "DBE connection thread").start();
  }


  @Override
  public Collection<DataStorageType> getStorageTypes() {
    return PerfanDataStorageFactory.supportedTypes;
  }


  @Override
  protected boolean createTablesImpl(List<DataTableMetadata> tableMetadatas) {
    return true;
  }

  @Override
  public void addData(String tableName, List<DataRow> data) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void connected(IDBEInterface idbeInterface) {
    log.info("Connected to experiment with " + idbeInterface);
    this.idbe = idbeInterface;
  }
}
