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
package org.netbeans.modules.dlight.perfan;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.perfan.impl.SunStudioDCConfigurationAccessor;
import org.netbeans.modules.dlight.perfan.spi.SunStudioIDsProvider;


/**
 * This class is configuration classs to create {@link @org-netbeans-modules-dlight@/org/netbeans/modules/dlight/spi/collector/DataCollector.html}
 *  based onbn SunStudio Performance Analyzer.
 * This collector can collect the following information for Java and Native code:
 * <ul>
 * <li>Functions list with metrics: Incl. Time and Excl.Time</li>
 * <li>Functions list with the synchronization metrics:  Sync. Wait Time and Sync. Wait Count</li>
 * <li>Functoins list containing memory leaks</li>
 * </ul>
 */
public final class SunStudioDCConfiguration implements DataCollectorConfiguration {

  static{
    SunStudioDCConfigurationAccessor.setDefault(new SunStudioDCConfigurationAccessorImpl());
  }
  /**
   * Types of information to be collected by SunStudio Performance Analyzer
   */
  public enum CollectedInfo {
    /**
     * Collects Functions Info with the metrics : Inclusive Time  and Exclusive Time
     */
    FUNCTIONS_LIST,
    /**
     * Collects synchronization information: Sync. Wait Time and Sync. Wait Count
     */
    SYNCHRONIZARION,
    /**
     * Collects information about memory leaks
     */
    MEMORY
  }
  private final List<CollectedInfo> collectedInfoList;

  /**
   * Creates new SunStudio Data Collector Configuration which should collect information <code>info</code>
   * @param info information to be collected
   */
  public SunStudioDCConfiguration(List<CollectedInfo> info) {
    collectedInfoList = info;
  }

   /**
   * Returns {@link @org-netbeans-modules-dlight@/org/netbeans/modules/dlight/api/storage/DataTableMetadata.html}
   * for types of information collected
   * @param collectedInfo information to be collected
   * @return virtual table description
   */
  public static final DataTableMetadata getDataTableMetaDataFor(List<CollectedInfo> collectedInfo) {
    List<Column> columns = new ArrayList<Column>();
    columns.add(new Column("name", String.class, "Function Name", null));
    //e.user:i.user:i.sync:i.syncn:name
    if (collectedInfo.contains(CollectedInfo.FUNCTIONS_LIST)) {
      columns.add(new Column("e.user", Double.class, "Exclusive User CPU Time", null));
      columns.add(new Column("i.user", Double.class, "Inclusive User CPU Time", null));
    }
    if (collectedInfo.contains(CollectedInfo.SYNCHRONIZARION)) {
      columns.add(new Column("i.sync", Double.class, "Sync. Wait Time", null));
      columns.add(new Column("i.syncn", Long.class, "Wait Count", null));
    }


    DataTableMetadata result = new DataTableMetadata("idbe", columns);
    return result;
  }

   /**
    * Return name of the column which represents Function name
    * @return name of the column which represents Function name
    */
  public static final String getFunctionNameColumnName() {
    return "name";
  }


  public String getID() {
    return SunStudioIDsProvider.DATA_COLLECTOR_ID;
  }

  List<CollectedInfo> getCollectedInfoList() {
    return collectedInfoList;
  }

  private static final class SunStudioDCConfigurationAccessorImpl extends SunStudioDCConfigurationAccessor{

    @Override
    public List<CollectedInfo> getCollectedInfo(SunStudioDCConfiguration configuration) {
      return configuration.getCollectedInfoList();
    }

  }
}
