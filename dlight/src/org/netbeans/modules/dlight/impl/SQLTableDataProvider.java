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
package org.netbeans.modules.dlight.impl;

import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.spi.impl.TableDataProvider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.openide.util.Exceptions;

/**
 * Implements default SQLTableDataProvider for {@link org.netbeans.modules.dlight.core.storage.model.SQLDataStorage}
 */
public class SQLTableDataProvider implements TableDataProvider {

    private SQLDataStorage storage;

    public SQLTableDataProvider() {
    }

//  public String getID() {
//    return "TableDataProvider";
//  }

//  /**
//   * Returns {@link org.netbeans.modules.dlight.core.dataprovider.model.TableDataModel} as
//   * provided data model scheme
//   * @return
//   */
//  public List<? extends DataModelScheme> getProvidedDataModelScheme() {
//    return Arrays.asList(TableDataModel.instance);
//  }
//
//  public final boolean provides(DataModelScheme dataModel) {
//    return getProvidedDataModelScheme().contains(dataModel);
//  }
//
//  public List<DataStorageType> getSupportedDataStorageTypes() {
//    return Arrays.asList(DataStorageTypeFactory.getInstance().getDataStorageType(SQLDataStorage.SQL_DATA_STORAGE_TYPE));
//  }
    /**
     * Attaches DataProvider to the <param>storage</param>.
     * All data requested by {@link org.netbeans.modules.dlight.core.visualizer.model.Visualizer} will
     * be extracted from this storage. This method is invoked at the time Visualizer
     * need to be displayed. See {@link org.netbeans.modules.dlight.core.model.DLightManager#openVisualizer(org.netbeans.modules.dlight.core.model.DLightTool, java.lang.String, org.netbeans.modules.dlight.core.visualizer.model.VisualizerConfiguration) } for more detailes
     * @param storage {@link org.netbeans.modules.dlight.core.storage.model.DataStorage}
     */
    public final void attachTo(DataStorage storage) {
        this.storage = (SQLDataStorage) storage;
    }

//  /**
//   * Returns new instance of SQLTableDataProvider
//   * @return new instance of SQLTableDataProvider
//   */
//  public DataProvider newInstance() {
//    return new SQLTableDataProvider();
//  }
    /**
     * Returns table view to visualize
     * @param tableMetadata table description to get data from
     * @return list of {@link org.netbeans.modules.dlight.core.storage.model.DataRow}
     */
    public List<DataRow> queryData(DataTableMetadata tableMetadata) {
        if (tableMetadata == null) {
            return null;
        }

        List<Column> columns = tableMetadata.getColumns();
        List<DataRow> result = new ArrayList<DataRow>();
        
        try {
            ResultSet rs = storage.select(tableMetadata.getName(), columns, tableMetadata.getViewStatement());
            if (rs == null) {
                return Collections.emptyList();
            }
            List<String> colnames = new ArrayList<String>();
            for (Column c : columns) {
                colnames.add(c.getColumnName());
            }
            while (rs.next()) {
                ArrayList<Object> data = new ArrayList<Object>();
                for (Column c : columns) {
                    data.add(rs.getObject(c.getColumnName()));
                }
                DataRow dataRow = new DataRow(colnames, data);
                result.add(dataRow);
            }

        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    public void attachTo(ServiceInfoDataStorage serviceInfoDataStorage) {
    }

    public void dataFiltersChanged(List<DataFilter> newSet) {
    }
}
