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
package org.netbeans.modules.dlight.collector.spi;

import java.util.List;
import java.util.concurrent.Future;
import org.netbeans.modules.dlight.collector.api.DataCollectorConfiguration;
import org.netbeans.modules.dlight.execution.api.DLightTarget;
import org.netbeans.modules.dlight.execution.api.DLightTargetListener;
import org.netbeans.modules.dlight.model.Validateable;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;
import org.netbeans.modules.dlight.storage.spi.DataStorage;
import org.netbeans.modules.dlight.storage.spi.DataStorageType;

/**
 * DataCollector collects data from application/system.
 * Puts data into the {@link org.netbeans.modules.dlight.core.storage.model.DataStorage}
 * it supports (see {@link #getSupportedDataStorageTypes()}) using the description
 * of data collected: {@link #getDataTablesMetadata()}.
 * Register in globa Lookup
 *
 */
public interface DataCollector<G extends DataCollectorConfiguration> extends DLightTargetListener, Validateable<DLightTarget> {

  
  /**
   * The types of storage this collector supports
   * @return returns list of {@link org.netbeans.modules.dlight.core.storage.model.DataStorageType}
   * data collector can put data into
   */
  List<DataStorageType> getSupportedDataStorageTypes();

  /**
   * The description of tables data collector will put information in.
   * @return list of {@link  org.netbeans.modules.dlight.core.storage.model.DataTableMetadata}
   * this collector gather information at.
   */
  List<? extends DataTableMetadata> getDataTablesMetadata();

  /**
   * Method init() is called BEFORE target start
   * It can be used to initialize collector database tables, etc...
   * @param storage storage this collector willput data into
   * @param target target this collector serve for
   */
  void init(DataStorage storage, DLightTarget target);


  /**
   * DataCollector can attach to the {@link org.netbeans.modules.dlight.core.execution.model.DLightTarget}.
   * @return <code>true</code> if collector is attachable, <code>false</code> otherwise
   */
 boolean isAttachable();

  /**
   * In case {@link #isAttachable()} returns <code>false</code> this method should
   * return command line to run the collector
   * @return command line to run collector
   */
  String getCmd();

  /**
   * In case {@link #isAttachable()} returns <code>false</code> this method should
   * return arguments to run data collector with command line returned by {@link #getCmd()}
   * method
   * @return aguments for running this data collector
   */
  String[] getArgs();
  
  
  Future<ValidationStatus> validate(DLightTarget targe);
  
}
