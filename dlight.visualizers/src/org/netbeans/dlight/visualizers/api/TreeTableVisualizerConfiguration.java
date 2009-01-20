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
package org.netbeans.dlight.visualizers.api;

import org.netbeans.modules.dlight.dataprovider.api.DataModelScheme;
import org.netbeans.modules.dlight.visualizer.api.VisualizerConfiguration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.dlight.visualizers.api.impl.TreeTableVisualizerConfigurationAccessor;
import org.netbeans.dlight.visualizers.api.impl.VisualizerConfigurationIDsProvider;
import org.netbeans.modules.dlight.spi.dataprovider.support.TreeTableDataModel;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata.Column;

public class TreeTableVisualizerConfiguration implements VisualizerConfiguration {
  private DataTableMetadata dataTableMetadata;
  private Column treeColumn;
  private Column[] tableColumns;
  private boolean isPlainTable = false;

  static{
    TreeTableVisualizerConfigurationAccessor.setDefault(new TreeTableVisualizerConfigurationAccessorImpl());
  }

  public TreeTableVisualizerConfiguration(String tableName, Column treeColumn, Column[] tableColumns) {
    this.treeColumn = treeColumn;
    this.tableColumns = tableColumns;
    List<Column> columns = Arrays.asList(treeColumn);
    columns.addAll(Arrays.asList(tableColumns));
    this.dataTableMetadata = new DataTableMetadata(tableName, columns);
  }

  public TreeTableVisualizerConfiguration(DataTableMetadata dataTableMetadata, String treeColumnName) {
    this(dataTableMetadata, treeColumnName, false);

  }

  public TreeTableVisualizerConfiguration(DataTableMetadata dataTableMetadata, String treeColumnName, boolean isPlainTable) {
    setDataTableMetadata(dataTableMetadata, treeColumnName);
    this.isPlainTable = isPlainTable;
  }

  protected TreeTableVisualizerConfiguration() {
  }

  protected void setDataTableMetadata(DataTableMetadata dataTableMetadata, String treeColumnName) {
    this.dataTableMetadata = dataTableMetadata;
    this.treeColumn = dataTableMetadata.getColumnByName(treeColumnName);
    List<Column> columns = dataTableMetadata.getColumns();
    List<Column> tableColumnsList = new ArrayList<Column>();
    for (Column c : columns) {
      if (!c.getColumnName().equals(treeColumnName)) {
        tableColumnsList.add(c);
      }
    }
    this.tableColumns = tableColumnsList.toArray(new Column[0]);

  }

  boolean isTableView() {
    return isPlainTable;
  }

  protected void setTreeColumn(Column treeColumn) {
    this.treeColumn = treeColumn;
  }

  public void setTableColumns(Column[] tableColumns) {
    this.tableColumns = tableColumns;
  }

  Column[] getTableColumns() {
    return tableColumns;
  }

  Column getTreeColumn() {
    return treeColumn;
  }

  public DataTableMetadata getMetadata() {
    return dataTableMetadata;
  }

  public DataModelScheme getSupportedDataScheme() {
    return TreeTableDataModel.instance;
  }

  public String getID() {
    return VisualizerConfigurationIDsProvider.TREE_TABLE_VISUALIZER;
  }

  private static final class TreeTableVisualizerConfigurationAccessorImpl extends TreeTableVisualizerConfigurationAccessor{

    @Override
    public Column[] getTableColumns(TreeTableVisualizerConfiguration configuration) {
      return configuration.getTableColumns();
    }

    @Override
    public Column getTreeColumn(TreeTableVisualizerConfiguration configuration) {
      return configuration.getTreeColumn();
    }

    @Override
    public boolean isTableView(TreeTableVisualizerConfiguration configuration) {
      return configuration.isTableView();
    }
    
  }
}

