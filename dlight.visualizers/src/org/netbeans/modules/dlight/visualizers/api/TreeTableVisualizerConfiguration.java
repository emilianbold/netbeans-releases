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
package org.netbeans.modules.dlight.visualizers.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.dlight.api.dataprovider.DataModelScheme;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata.Column;
import org.netbeans.modules.dlight.api.support.DataModelSchemeProvider;
import org.netbeans.modules.dlight.api.visualizer.TableBasedVisualizerConfiguration;
import org.netbeans.modules.dlight.visualizers.api.impl.TreeTableVisualizerConfigurationAccessor;
import org.netbeans.modules.dlight.visualizers.api.impl.VisualizerConfigurationIDsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProvider;

/**
 * This configuration is used to be able to show data collected
 * in Table View. It supports "model:tree:table" scheme. Which means
 * if you would like to create own implementation of  implementation of Visualizer DataProvider which will be used by default implementation
 * of Tree Table visualizer, it should return that it supports model scheme which can be retrieved  using
 * {@link org.netbeans.modules.dlight.api.support.DataModelSchemeProvider#getScheme(java.lang.String)} where id equals to "model:tree:table".
 */
public class TreeTableVisualizerConfiguration implements TableBasedVisualizerConfiguration {

    private DataTableMetadata dataTableMetadata;
    private Column treeColumn;
    private Column[] tableColumns;
    private boolean isPlainTable = false;
    private ColumnsUIMapping columnsUIMapping;
    private NodeActionsProvider nodeActionsProvider;


    static {
        TreeTableVisualizerConfigurationAccessor.setDefault(new TreeTableVisualizerConfigurationAccessorImpl());
    }

    /**
     * Creates new instance of tree table visualizer configuration
     * @param tableName table name
     * @param treeColumn
     * @param tableColumns
     */
    private TreeTableVisualizerConfiguration(String tableName, Column treeColumn, Column[] tableColumns) {
        this.treeColumn = treeColumn;
        this.tableColumns = tableColumns;
        List<Column> columns = Arrays.asList(treeColumn);
        columns.addAll(Arrays.asList(tableColumns));
        this.dataTableMetadata = new DataTableMetadata(tableName, columns, null);
    }

    /**
     * Creates new configuration to create Tree Table Visualizer on the base of, <code>dataTableMetadata</code>
     * is table description, <code> treeColumnName</code> name of the column which is used in Tree Table View as tree column
     * @param dataTableMetadata table description
     * @param treeColumnName the name of the column which will be used as tree column
     */
    public TreeTableVisualizerConfiguration(DataTableMetadata dataTableMetadata, String treeColumnName) {
        this(dataTableMetadata, treeColumnName, false);

    }

    /**
     * Creates new configuration to create Tree Table Visualizer on the base of, <code>dataTableMetadata</code>
     * is table description, <code> treeColumnName</code> name of the column which is used in Tree Table View as tree column
     * @param dataTableMetadata table description
     * @param treeColumnName the name of the column which will be used as tree column
     * @param isPlainTable  <code>true</code> if you would liek to see plain table, <code>false</code> otherwise
     */
    public TreeTableVisualizerConfiguration(DataTableMetadata dataTableMetadata, String treeColumnName, boolean isPlainTable) {
        setDataTableMetadata(dataTableMetadata, treeColumnName);
        this.isPlainTable = isPlainTable;
    }

    /**
     *
     */
    protected TreeTableVisualizerConfiguration() {
    }

    /**
     * Sets
     * @param dataTableMetadata
     * @param treeColumnName
     */
    private void setDataTableMetadata(DataTableMetadata dataTableMetadata, String treeColumnName) {
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


    Column[] getTableColumns() {
        return tableColumns;
    }

    Column getTreeColumn() {
        return treeColumn;
    }

    NodeActionsProvider getNodeActionsProvider(){
        return nodeActionsProvider;
    }

    public void setNodeActionProvider(NodeActionsProvider nodeActionsProvider){
        this.nodeActionsProvider = nodeActionsProvider;
    }


    public final void setColumnsUIMapping(ColumnsUIMapping columnsUIMapping){
        this.columnsUIMapping = columnsUIMapping;
    }

    public DataTableMetadata getMetadata() {
        return dataTableMetadata;
    }

    public DataModelScheme getSupportedDataScheme() {
        return DataModelSchemeProvider.getInstance().getScheme("model:tree:table");//NOI18N
    }


    public String getID() {
        return VisualizerConfigurationIDsProvider.TREE_TABLE_VISUALIZER;
    }

    private static final class TreeTableVisualizerConfigurationAccessorImpl extends TreeTableVisualizerConfigurationAccessor {

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

        @Override
        public NodeActionsProvider getNodesActionProvider(TreeTableVisualizerConfiguration configuration) {
            return configuration.getNodeActionsProvider();
        }

        @Override
        public ColumnsUIMapping getColumnsUIMapping(TreeTableVisualizerConfiguration configuration) {
           return configuration.columnsUIMapping;
        }


    }

}

